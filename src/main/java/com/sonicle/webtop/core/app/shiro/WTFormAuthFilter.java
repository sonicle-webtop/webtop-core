/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.shiro;

import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.net.IPUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.app.AuditLogManager;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.PushEndpoint;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.app.servlet.Login;
import com.sonicle.webtop.core.app.servlet.PrivateRequest;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.app.shiro.filter.DeviceCookie;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.IdentifierUtils;
import inet.ipaddr.IPAddress;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author malbinola
 */
public class WTFormAuthFilter extends FormAuthenticationFilter {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WTFormAuthFilter.class);
	public static final String COOKIE_DEVICEID = "DID";
	public static final String COOKIE_WEBTOP_CLIENTID = "CID";
	
	private final KeyedReentrantLocks<String> lockSessionId = new KeyedReentrantLocks<>();
	
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		WTRealm wtRealm = (WTRealm)ShiroUtils.getRealmByName(WTRealm.NAME);
		if (wtRealm == null) {
			LOGGER.error("Realm not available [{}]", WTRealm.NAME);
			setFailureAttribute(request, new AuthenticationException("Realm not available"));
			return true;
		}
		
		try {
			wtRealm.checkUser((Principal)subject.getPrincipal());
		} catch(WTException ex) {
			LOGGER.error("User check error", ex);
			writeAuthLog((UsernamePasswordDomainToken)token, httpRequest, LogbackHelper.Level.ERROR, "LOGIN_FAILURE");
			setFailureAttribute(request, new AuthenticationException(ex));
			return true;
		}
		
		UserProfileId profileId = new UserProfileId(((UsernamePasswordDomainToken)token).getDomain(), ((UsernamePasswordDomainToken)token).getUsername());
		WebTopSession webtopSession = SessionContext.getCurrent();
		if (webtopSession != null) {
			final String sessionId = webtopSession.getId();
			try {
				lockSessionId.tryLock(sessionId, 60, TimeUnit.SECONDS);
				initDeviceId(profileId, httpRequest, httpResponse, webtopSession.getSession());
				
			} catch (InterruptedException ex) {
				// Do nothing...
			} finally {
				lockSessionId.unlock(sessionId);
			}
			
			// Legacy ClientID, in future this will be replaced by DeviceID!
			prepareClientId(httpRequest, httpResponse, webtopSession.getSession());
			
			String location = ServletUtils.getStringParameter(request, "location", null);
			if (location != null) {
				String url = ServletHelper.sanitizeBaseUrl(location);
				webtopSession.getSession().setAttribute(SessionManager.ATTRIBUTE_CLIENT_URL, url);
				LOGGER.trace("[{}] Location: {}", webtopSession.getId(), url);
			}
			
			doKnownDeviceVerification(profileId, httpRequest, webtopSession.getSession());
		}
		
		writeAuthLog((UsernamePasswordDomainToken)token, httpRequest, LogbackHelper.Level.INFO, "LOGIN_SUCCESS");
		return super.onLoginSuccess(token, subject, request, response);
	}
	
	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
		writeAuthLog((UsernamePasswordDomainToken)token, (HttpServletRequest)request, LogbackHelper.Level.ERROR, "LOGIN_FAILURE");
		return super.onLoginFailure(token, e, request, response);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		String ctxRequestUrl = ServletUtils.getContextRelativeRequestURIString((HttpServletRequest)request);
		if (StringUtils.startsWithIgnoreCase(ctxRequestUrl, PrivateRequest.URL)
				|| StringUtils.startsWithIgnoreCase(ctxRequestUrl, PrivateRequest.URL_LEGACY) // for compatibility purpose only!
				|| StringUtils.startsWithIgnoreCase(ctxRequestUrl, PushEndpoint.URL)) {
			ServletUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return false;
		} else {
			return super.onAccessDenied(request, response);
		}
	}
	
	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {		
		try {
			// Do a forward instead of classic redirect. It avoids ugly URL suffixes.
			ServletUtils.forwardRequest((HttpServletRequest)request, (HttpServletResponse)response, getLoginUrl());
		} catch(ServletException ex) {
			throw new IOException(ex);
		}
	}
	
	@Override
	protected AuthenticationToken createToken(String username, String password, ServletRequest request, ServletResponse response) {
		boolean rememberMe = isRememberMe(request);
		return createToken(username, password, getDomain(request), rememberMe, getHost(request));
	}
	
	protected AuthenticationToken createToken(String username, String password, String domain, boolean rememberMe, String host) {
		return new UsernamePasswordDomainToken(username, password, domain, rememberMe, host);
	}
	
	protected String getDomain(ServletRequest request) {
		return WebUtils.getCleanParam(request, "wtdomain");
	}

	@Override
	protected void setFailureAttribute(ServletRequest request, AuthenticationException ae) {
		String value = null;
		if(ae instanceof UnknownAccountException) {
			value = Login.LOGINFAILURE_INVALID;
		} else if(ae instanceof IncorrectCredentialsException) {
			value = Login.LOGINFAILURE_INVALID;
		} else if(ae instanceof LockedAccountException) {
			value = Login.LOGINFAILURE_INVALID;
		} else { // AuthenticationException
			value = Login.LOGINFAILURE_INVALID;
		}
		request.setAttribute(getFailureKeyAttribute(), value);
		//String message = ae.getMessage();
		//request.setAttribute(getFailureKeyAttribute(), message);
	}
	
	private void doKnownDeviceVerification(UserProfileId profileId, HttpServletRequest request, HttpSession session) {
		String remoteIp = SessionContext.getClientRemoteIP(session);
		IPAddress ip = IPUtils.toIPAddress(remoteIp);
		
		boolean impersonating = RunContext.isImpersonated();
		AuditLogManager audMgr = WebTopApp.getInstance().getAuditLogManager();

		WT.runPrivileged(() -> {
			boolean enabled = audMgr.isKnownDeviceVerificationEnabled(profileId);
			if (enabled) {
				String deviceId = SessionContext.getWebTopDeviceID(session);
				String ua = SessionContext.getClientPlainUserAgent(session);

				try {
					if (LOGGER.isDebugEnabled()) LOGGER.debug("Checking known device... [{}, {}] ", profileId, deviceId);
					if (!audMgr.testAndSaveKnownDevice(profileId, deviceId)) {
						if (enabled) {
							if (LOGGER.isDebugEnabled()) LOGGER.debug("Verifying whitelist... [{}, {}] ", profileId, deviceId);
							List<String> nets = audMgr.getKnownDeviceVerificationNetWhiletist(profileId);
							if (nets.isEmpty() || !IPUtils.isAddressInRange(ip, nets.toArray(new String[nets.size()]))) {
								if (LOGGER.isDebugEnabled()) LOGGER.debug("Sending notice... [{}, {}] ", profileId, deviceId);
								audMgr.sendDeviceVerificationNotice(profileId, impersonating, deviceId, ua, ip);
							} else {
								if (LOGGER.isDebugEnabled()) LOGGER.debug("Address '{}' whitelisted. Skipping... [{}, {}]", remoteIp, profileId, deviceId);
							}
						}
					}
				} catch(Throwable t) {
					LOGGER.error("Error checking known-device [{}, {}, {}]", profileId, deviceId, remoteIp, t);
				}
			}
		});
	}
	
	private void writeAuthLog(UsernamePasswordDomainToken token, HttpServletRequest request, LogbackHelper.Level level, String action) {
		String domainId = StringUtils.defaultIfBlank(token.getDomain(), "?");
		String userId = StringUtils.defaultIfBlank(token.getUsername(), "?");
		UserProfileId pid = new UserProfileId(domainId, userId);
		String sessionId = SessionContext.getCurrentId();
		String clientIp = ServletUtils.getClientIP(request);
		
		AuditLogManager.logAuth(level, clientIp, sessionId, pid, action);
		WebTopApp.getInstance().getAuditLogManager()
				.write(pid, null, sessionId, CoreManifest.ID, "AUTH", action, null, JsonResult.gson().toJson(new MapItem().add("ip", clientIp)));
	}
	
	private void prepareClientId(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		String clientId = ServletUtils.getCookie(request, COOKIE_WEBTOP_CLIENTID);
		if (StringUtils.isBlank(clientId)) {
			clientId = IdentifierUtils.getUUIDTimeBased();
			ServletUtils.setCookie(response, COOKIE_WEBTOP_CLIENTID, clientId, 60*60*24*365*10);
		}
		session.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID, clientId);
	}
	
	private void initDeviceId(UserProfileId profileId, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		String deviceId = SessionContext.getWebTopDeviceID(session);
		if (StringUtils.isBlank(deviceId)) {
			String secret = getUserSecret(profileId);
			if (!StringUtils.isBlank(secret)) {
				DeviceCookie cookie = readDeviceCookie(profileId, secret, request);
				if (cookie == null) {
					cookie = createDeviceCookie(SessionContext.getClientPlainUserAgent(session));
					writeDeviceCookie(profileId, secret, cookie, response);
				}
				session.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_DEVICEID, cookie.getDeviceId());
			}
		}
	}
	
	private DeviceCookie readDeviceCookie(UserProfileId profileId, String secret, HttpServletRequest request) {
		return ServletUtils.getEncryptedCookie(secret, request, buildDeviceCookieName(profileId), DeviceCookie.class);
	}
	
	private void writeDeviceCookie(UserProfileId profileId, String secret, DeviceCookie cookie, HttpServletResponse response) {
		int duration = 60*60*24*365*2; // 2 years
		ServletUtils.setEncryptedCookie(secret, response, buildDeviceCookieName(profileId), cookie, DeviceCookie.class, duration);
	}
	
	private String buildDeviceCookieName(UserProfileId profileId) {
		return COOKIE_DEVICEID + "-" +DigestUtils.md5Hex(profileId.toString());
	}
	
	private String buildDeviceId(String uuid, String userAgentHeader) {
		String md5UA = DigestUtils.md5Hex(StringUtils.defaultIfBlank(userAgentHeader, "useragent/missing"));
		return DigestUtils.sha1Hex(uuid + "|" + md5UA);
	}
	
	private DeviceCookie createDeviceCookie(String userAgentHeader) {
		String uuid = IdentifierUtils.getUUIDTimeBased();
		String deviceId = buildDeviceId(uuid, userAgentHeader);
		long timestamp = DateTimeUtils.now().getMillis();
		return new DeviceCookie(uuid, deviceId, timestamp);
	}
	
	private String getUserSecret(UserProfileId profileId) {
		try {
			return WebTopApp.getInstance().getWebTopManager()
				.getUserSecret(profileId.getDomainId(), profileId.getUserId());

		} catch (Throwable t) {
			LOGGER.error("[DeviceId] Unable to get secret for '{}'", profileId, t);
			return null;
		}
	}
	
	/* 
		This is useful only if welcome page is different from successUrl.
		In our case they are equivalent.
	
	@Override
	protected void issueSuccessRedirect(ServletRequest request, ServletResponse response) throws Exception {
		// In order to avoid the ugly /Start URL suffix after a succesful
		// login we has overridden this method in order to always do a redirect
		// (that changes browser displayed URL) to the landing/default 
		// webapp URL, since our welcome page is precisely configured to the
		// success servlet /Start
		WebUtils.redirectToSavedRequest(request, response, "");
	}
	*/
 }
