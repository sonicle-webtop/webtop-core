/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.shiro.filter;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.shiro.AuthTokenAccessToken;
import com.sonicle.webtop.core.app.shiro.AuthTokenBearerApiKey;
import com.sonicle.webtop.core.app.shiro.MaintenanceException;
import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class AuthBearer extends BearerHttpAuthenticationFilter {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AuthBearer.class);
	private static final String X_AUTH_USERNAME = "X-Auth-Username";
	/**
	 * App-launch signal header ("app kill/restart = the app world's re-login"):
	 * an access-token authenticated app sends an opaque per-launch id here (any
	 * WebTop API call — processed at this auth layer, so it is service-agnostic);
	 * core rebuilds the user's shared app-side managers, deduped by the id and
	 * debounced. Ignored on non-access-token auth.
	 */
	public static final String HEADER_APP_LAUNCH = "X-WT-App-Launch";

	protected String getXAuthUsernameHeader(ServletRequest request) {
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		return httpRequest.getHeader(X_AUTH_USERNAME);
	}

	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, org.apache.shiro.subject.Subject subject, ServletRequest request, ServletResponse response) throws Exception {
		if (token instanceof AuthTokenAccessToken) {
			try {
				String launchId = WebUtils.toHttp(request).getHeader(HEADER_APP_LAUNCH);
				if (launchId != null && !launchId.isEmpty()) {
					com.sonicle.security.Principal principal = (com.sonicle.security.Principal)subject.getPrincipal();
					if (principal != null) {
						com.sonicle.webtop.core.app.WT.notifyAppLaunch(
							com.sonicle.webtop.core.sdk.UserProfileId.from(principal), launchId);
					}
				}
			} catch (Throwable t) {
				//the launch signal must never break authentication
				LOGGER.warn("app-launch signal handling failed", t);
			}
		}
		return super.onLoginSuccess(token, subject, request, response);
	}
	
	@Override
	protected AuthenticationToken createBearerToken(String token, ServletRequest request) {
		String xAuthUsernameHeader = getXAuthUsernameHeader(request);
		if (xAuthUsernameHeader == null || xAuthUsernameHeader.length() == 0) {
			// New user-session access-token flow: token alone identifies the
			// user via DB lookup (see WTRealm.authenticateAccessToken).
			return new AuthTokenAccessToken(token, request.getRemoteHost());
		} else {
			// Existing API-key flow: token is paired with X-Auth-Username and
			// authenticated via WebTopManager.authenticateApiKey.
			return new AuthTokenBearerApiKey(token, xAuthUsernameHeader, request.getRemoteHost());
		}
	}
	
	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
		final HttpServletRequest httpRequest = WebUtils.toHttp(request);
		final HttpServletResponse httpResponse = WebUtils.toHttp(response);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onLoginFailure", ServletUtils.getRequestID(httpRequest));
		
		// Breaks the default flow in case of MaintenanceException:
		// in this case send a SERVICE_UNAVAILABLE (503) error in order to allow
		// clients to get informed to the temporary condition.
		if (e instanceof MaintenanceException) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Maintenance detected, sending a 503 error...", ServletUtils.getRequestID(httpRequest));
			try {
				httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
				return false;

			} catch (IOException ex) {
				return super.onLoginFailure(token, e, request, response);
			}
		} else if (token instanceof AuthTokenAccessToken) {
			// User-session access-token failure: signal the client to attempt
			// a refresh by responding 401 with WWW-Authenticate: Bearer error="invalid_token".
			httpResponse.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
			try {
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return false;

			} catch (IOException ex) {
				return super.onLoginFailure(token, e, request, response);
			}
		} else {
			return super.onLoginFailure(token, e, request, response);
		}
	}
}
