/*
 * Copyright (C) 2026 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.rest.v1;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.model.AuthSession;
import com.sonicle.webtop.core.app.model.AuthTokenPair;
import com.sonicle.webtop.core.app.model.AuthTokenValidated;
import com.sonicle.webtop.core.app.shiro.AuthTokenUsernamePasswordDomain;
import com.sonicle.webtop.core.app.shiro.MaintenanceException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.swagger.v1.api.AuthApi;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthLoginRequest;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthLogoutRequest;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthRefreshRequest;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthSessionInfo;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthTokenPair;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthUserInfo;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Auth extends AuthApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(Auth.class);
	private static final String BEARER_PREFIX = "Bearer ";

	// Rate-limit buckets, single-node in-memory. Caffeine handles eviction so
	// unique-IP/unique-username bursts cannot grow the maps unboundedly.
	// NOTE: these limits are per process; if WebTop is deployed clustered,
	// each node enforces independently. Replace with a shared store
	// (Bucket4j-Hazelcast/Redis) if/when clustering is in scope.
	private static final Bandwidth LOGIN_PER_IP_LIMIT =
		Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(5)));
	private static final Bandwidth LOGIN_PER_USER_LIMIT =
		Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(5)));
	private static final Bandwidth REFRESH_PER_IP_LIMIT =
		Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(5)));
	private static final Cache<String, Bucket> loginIpBuckets = Caffeine.newBuilder()
		.expireAfterAccess(1, TimeUnit.HOURS).maximumSize(100_000).build();
	private static final Cache<String, Bucket> loginUserBuckets = Caffeine.newBuilder()
		.expireAfterAccess(1, TimeUnit.HOURS).maximumSize(100_000).build();
	private static final Cache<String, Bucket> refreshIpBuckets = Caffeine.newBuilder()
		.expireAfterAccess(1, TimeUnit.HOURS).maximumSize(100_000).build();

	@Context
	private HttpServletRequest httpRequest;

	private WebTopManager wtMgr() {
		return WebTopApp.getInstance().getWebTopManager();
	}

	private String clientIp() {
		return (httpRequest != null) ? httpRequest.getRemoteAddr() : null;
	}

	private String clientUserAgent() {
		return (httpRequest != null) ? httpRequest.getHeader("User-Agent") : null;
	}

	private String currentBearerToken() {
		if (httpRequest == null) return null;
		final String h = httpRequest.getHeader("Authorization");
		if (h == null || !h.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) return null;
		return h.substring(BEARER_PREFIX.length()).trim();
	}

	private ApiAuthTokenPair toApiPair(final AuthTokenPair pair, final boolean includeUser) {
		final ApiAuthTokenPair api = new ApiAuthTokenPair()
			.tokenType("Bearer")
			.accessToken(pair.getAccessToken())
			.refreshToken(pair.getRefreshToken())
			.accessTokenExpiresAt(pair.getAccessTokenExpiresAt().toDate())
			.refreshTokenExpiresAt(pair.getRefreshTokenExpiresAt().toDate());
		if (includeUser) {
			final UserProfileId pid = pair.getProfileId();
			final UserProfile.Data pdata = WT.getProfileData(pid);
			final String displayName = (pdata != null && !StringUtils.isBlank(pdata.getDisplayName()))
				? pdata.getDisplayName() : pid.getUserId();
			api.user(new ApiAuthUserInfo()
				.profileId(pid.toString())
				.profileUsername(pid.toString())
				.displayName(displayName)
			);
		}
		return api;
	}

	private Response respInvalidGrant() {
		return Response.status(Response.Status.UNAUTHORIZED)
			.header("WWW-Authenticate", "Bearer error=\"invalid_grant\"")
			.entity(createErrorEntity(Response.Status.UNAUTHORIZED, "Invalid credentials"))
			.build();
	}

	/**
	 * Returns {@code null} if the (key, bandwidth) bucket admits one more
	 * request, or a 429 Response with Retry-After if it doesn't.
	 */
	private Response checkRateLimit(final Cache<String, Bucket> buckets, final String key, final Bandwidth limit) {
		if (StringUtils.isBlank(key)) return null;
		final Bucket bucket = buckets.get(key, k -> Bucket.builder().addLimit(limit).build());
		final ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
		if (probe.isConsumed()) return null;
		final long retryAfterSec = Math.max(1L, probe.getNanosToWaitForRefill() / 1_000_000_000L);
		return Response.status(429)
			.header("Retry-After", Long.toString(retryAfterSec))
			.entity(new ApiError().code(429).description("Too many requests"))
			.build();
	}

	@Override
	public Response authLogin(ApiAuthLoginRequest body) {
		if (body == null || StringUtils.isBlank(body.getUsername()) || StringUtils.isBlank(body.getPassword())) {
			return respErrorBadRequest("username and password are required");
		}

		final String ipKey = StringUtils.defaultIfBlank(clientIp(), "unknown");
		final String userKey = StringUtils.lowerCase(StringUtils.trim(body.getUsername()))
			+ "|" + StringUtils.defaultString(body.getDomain());
		Response throttled = checkRateLimit(loginIpBuckets, ipKey, LOGIN_PER_IP_LIMIT);
		if (throttled != null) return throttled;
		throttled = checkRateLimit(loginUserBuckets, userKey, LOGIN_PER_USER_LIMIT);
		if (throttled != null) return throttled;

		final UsernamePasswordToken upt;
		if (!StringUtils.isBlank(body.getDomain())) {
			upt = new AuthTokenUsernamePasswordDomain(body.getUsername(), body.getPassword(), body.getDomain(), false, clientIp());
		} else {
			upt = new UsernamePasswordToken(body.getUsername(), body.getPassword(), false, clientIp());
		}

		// Authenticate via Shiro without creating a session: we want a stateless
		// REST surface, so the access/refresh token pair is the only continuation.
		final Subject subject = new Subject.Builder().sessionCreationEnabled(false).buildSubject();
		try {
			subject.login(upt);
		} catch (DisabledAccountException ex) {
			LOGGER.debug("authLogin: disabled account [{}]", body.getUsername(), ex);
			// 423 Locked â not present in javax.ws.rs Response.Status enum, build manually.
			return Response.status(423)
				.entity(new ApiError().code(423).description("Account or domain disabled"))
				.build();
		} catch (AuthenticationException ex) {
			if (ex.getCause() instanceof MaintenanceException) {
				return respError(Response.Status.SERVICE_UNAVAILABLE, ex.getCause().getMessage());
			}
			LOGGER.debug("authLogin: authentication failed [{}]", body.getUsername(), ex);
			return respInvalidGrant();
		} catch (Throwable t) {
			LOGGER.error("authLogin: unexpected error [{}]", body.getUsername(), t);
			return respError(t);
		}

		try {
			final Principal principal = (Principal) subject.getPrincipal();
			final UserProfileId pid = UserProfileId.from(principal);

			final AuthTokenPair pair = wtMgr().issueAuthSession(
				pid.getDomainId(),
				pid.getUserId(),
				body.getDeviceLabel(),
				clientIp(),
				clientUserAgent()
			);
			return respOk(toApiPair(pair, true));

		} catch (Throwable t) {
			LOGGER.error("authLogin: token issue failed [{}]", body.getUsername(), t);
			return respError(t);
		} finally {
			subject.logout();
		}
	}

	@Override
	public Response authRefresh(ApiAuthRefreshRequest body) {
		if (body == null || StringUtils.isBlank(body.getRefreshToken())) {
			return respErrorBadRequest("refreshToken is required");
		}
		
		final String ipKey = StringUtils.defaultIfBlank(clientIp(), "unknown");
		final Response throttled = checkRateLimit(refreshIpBuckets, ipKey, REFRESH_PER_IP_LIMIT);
		if (throttled != null) return throttled;

		try {
			final AuthTokenPair pair = wtMgr().rotateAuthRefresh(
				body.getRefreshToken(),
				null,
				clientIp(),
				clientUserAgent()
			);
			if (pair == null) return respInvalidGrant();
			return respOk(toApiPair(pair, false));

		} catch (Throwable t) {
			LOGGER.error("authRefresh: rotation failed", t);
			return respError(t);
		}
	}

	@Override
	public Response authLogout(ApiAuthLogoutRequest body) {
		final UserProfileId pid = RunContext.getRunProfileId();
		if (pid == null) return respInvalidGrant();
		try {
			Long refreshIdToRevoke = null;

			if (body != null && !StringUtils.isBlank(body.getRefreshToken())) {
				refreshIdToRevoke = wtMgr().lookupOwnedRefreshTokenId(pid.getDomainId(), pid.getUserId(), body.getRefreshToken());
				// Unknown / not-owned / revoked â idempotent OK (do not leak existence).
			} else {
				// No body: revoke the chain of the access token used for this call.
				final String bearer = currentBearerToken();
				if (!StringUtils.isBlank(bearer)) {
					final AuthTokenValidated me = wtMgr().validateAccessToken(bearer);
					if (me != null) refreshIdToRevoke = me.getParentRefreshTokenId();
				}
			}

			if (refreshIdToRevoke != null) {
				wtMgr().revokeAuthSession(pid.getDomainId(), pid.getUserId(), refreshIdToRevoke);
			}
			return respOkNoContent();

		} catch (Throwable t) {
			LOGGER.error("authLogout: failed [{}]", pid, t);
			return respError(t);
		}
	}

	@Override
	public Response authLogoutAll() {
		final UserProfileId pid = RunContext.getRunProfileId();
		if (pid == null) return respInvalidGrant();
		try {
			wtMgr().revokeAllAuthForUser(pid.getDomainId(), pid.getUserId());
			return respOkNoContent();

		} catch (Throwable t) {
			LOGGER.error("authLogoutAll: failed [{}]", pid, t);
			return respError(t);
		}
	}

	@Override
	public Response authListSessions() {
		final UserProfileId pid = RunContext.getRunProfileId();
		if (pid == null) return respInvalidGrant();
		try {
			Long currentRefreshId = null;
			final String bearer = currentBearerToken();
			if (!StringUtils.isBlank(bearer)) {
				final AuthTokenValidated me = wtMgr().validateAccessToken(bearer);
				if (me != null) currentRefreshId = me.getParentRefreshTokenId();
			}

			final List<AuthSession> sessions = wtMgr().listActiveAuthSessions(pid.getDomainId(), pid.getUserId());
			final List<ApiAuthSessionInfo> out = new ArrayList<>(sessions.size());
			for (AuthSession s : sessions) {
				final ApiAuthSessionInfo api = new ApiAuthSessionInfo()
					.sessionId(Long.toString(s.getId()))
					.deviceLabel(s.getDeviceLabel())
					.createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toDate() : null)
					.lastUsedAt(s.getLastUsedAt() != null ? s.getLastUsedAt().toDate() : null)
					.expiresAt(s.getExpiresAt() != null ? s.getExpiresAt().toDate() : null)
					.clientIpAddress(s.getClientIpAddress())
					.clientUserAgent(s.getClientUserAgent())
					.current(currentRefreshId != null && currentRefreshId == s.getId());
				out.add(api);
			}
			return respOk(out);

		} catch (Throwable t) {
			LOGGER.error("authListSessions: failed [{}]", pid, t);
			return respError(t);
		}
	}

	@Override
	public Response authRevokeSession(String sessionId) {
		final UserProfileId pid = RunContext.getRunProfileId();
		if (pid == null) return respInvalidGrant();

		final long id;
		try {
			id = Long.parseLong(sessionId);
		} catch (NumberFormatException ex) {
			return respErrorNotFound();
		}

		try {
			final boolean revoked = wtMgr().revokeAuthSession(pid.getDomainId(), pid.getUserId(), id);
			return revoked ? respOkNoContent() : respErrorNotFound();

		} catch (Throwable t) {
			LOGGER.error("authRevokeSession: failed [{}, {}]", pid, sessionId, t);
			return respError(t);
		}
	}

	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
			.code(status.getStatusCode())
			.description(message);
	}
}
