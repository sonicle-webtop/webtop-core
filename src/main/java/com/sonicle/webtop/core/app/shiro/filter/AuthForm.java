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
package com.sonicle.webtop.core.app.shiro.filter;

import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.net.IPUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.app.AuditLogManager;
import com.sonicle.webtop.core.app.AuditLogManager.KnownDeviceEvalResult;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.atmosphere.AtmosphereServlet;
import com.sonicle.webtop.core.app.servlet.Login;
import com.sonicle.webtop.core.app.servlet.PrivateRequest;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.app.shiro.ShiroUtils;
import com.sonicle.webtop.core.app.shiro.AuthTokenUsernamePasswordDomain;
import com.sonicle.webtop.core.app.shiro.WTRealm;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import inet.ipaddr.IPAddress;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class AuthForm extends FormAuthenticationFilter {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AuthForm.class);
	//private final KeyedReentrantLocks<String> lockSessionId = new KeyedReentrantLocks<>();
	
	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Redirecting to '{}'...", ServletUtils.getRequestID(WebUtils.toHttp(request)), getLoginUrl());
		//super:
		//WebUtils.issueRedirect(request, response, getLoginUrl());
		super.redirectToLogin(request, response);
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		Subject subject = getSubject(request, response);
		// Consider allowed both isAuthenticated and isRemembered
		return (subject.isAuthenticated() || subject.isRemembered()) && subject.getPrincipal() != null;
	}
	
	@Override
	protected void saveRequest(ServletRequest request) {
		// Saving the request, internally forces HTTP session creation.
		// We do NOT want this, so disable request saving!
		//   WebUtils.saveRequest(request);
		//TODO: instead of saving request into session, we can define a session-attribute instead...
	}
	
	@Override
	protected void saveRequestAndRedirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] saveRequestAndRedirectToLogin", ServletUtils.getRequestID(WebUtils.toHttp(request)));
		//super:
		//saveRequest(request);
		//redirectToLogin(request, response);
		super.saveRequestAndRedirectToLogin(request, response);
	}
	
	@Override
	protected void issueSuccessRedirect(ServletRequest request, ServletResponse response) throws Exception {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] issueSuccessRedirect", ServletUtils.getRequestID(WebUtils.toHttp(request)));
		//super:
		//WebUtils.redirectToSavedRequest(request, response, getSuccessUrl());
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Redirecting to '{}'...", ServletUtils.getRequestID(WebUtils.toHttp(request)), getSuccessUrl());
		WebUtils.issueRedirect(request, response, getSuccessUrl());
		//super.issueSuccessRedirect(request, response);
	}
	
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onAccessDenied", ServletUtils.getRequestID(WebUtils.toHttp(request)));
		// If the AccessDenied belongs to a request targetting an endpoint that 
		// does NOT need to redirect its clients to login page, simply block 
		// the process chain and return an HTTP error
		if (isBackendRequest(WebUtils.toHttp(request))) {
			ServletUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return false;
			
		} else {
			return super.onAccessDenied(request, response);
		}
	}
	
	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
		final HttpServletRequest httpRequest = WebUtils.toHttp(request);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onLoginFailure", ServletUtils.getRequestID(httpRequest));
		
		writeAuthLog((AuthTokenUsernamePasswordDomain)token, (HttpServletRequest)request, LogbackHelper.Level.ERROR, "LOGIN_FAILURE");
		return super.onLoginFailure(token, e, request, response);
	}
	
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
		final HttpServletRequest httpRequest = WebUtils.toHttp(request);
		final HttpServletResponse httpResponse = WebUtils.toHttp(response);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onLoginSuccess", ServletUtils.getRequestID(httpRequest));
		
		/*
		WTRealm wtRealm = (WTRealm)ShiroUtils.getRealmByName(WTRealm.NAME);
		if (wtRealm == null) {
			LOGGER.error("Realm not available [{}]", WTRealm.NAME);
			setFailureAttribute(request, new AuthenticationException("Realm not available"));
			return true;
		}
		
		try {
			wtRealm.checkUser((Principal)subject.getPrincipal());
		} catch (WTException ex) {
			LOGGER.error("User check error", ex);
			writeAuthLog((AuthTokenUsernamePasswordDomain)token, httpRequest, LogbackHelper.Level.ERROR, "LOGIN_FAILURE");
			setFailureAttribute(request, new AuthenticationException(ex));
			return true;
		}
		*/
		
		UserProfileId profileId = new UserProfileId(((AuthTokenUsernamePasswordDomain)token).getDomain(), ((AuthTokenUsernamePasswordDomain)token).getUsername());
		Session subjectSession = SessionContext.getSubjectSession(subject, true);
		if (subjectSession != null) {
			/*
			final String sid = subjectSession.getId().toString();
			try {
				lockSessionId.tryLock(sid, 60, TimeUnit.SECONDS);
				initDeviceID(RunContext.getRunProfileId(subject), subjectSession, httpRequest, httpResponse);

			} catch (InterruptedException ex) {
				// Do nothing...
			} finally {
				lockSessionId.unlock(sid);
			}
			*/
			
			/*
			// Stores Client colorScheme as session's attribute
			String colorScheme = ServletUtils.getStringParameter(request, "colorscheme", null);
			if (colorScheme != null && !("light".equals(colorScheme) || "dark".equals(colorScheme))) colorScheme = null;
			subjectSession.setAttribute(SessionManager.ATTRIBUTE_CLIENT_COLORSCHEME, StringUtils.defaultIfBlank(colorScheme, "light"));
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] ColorScheme: {}", subjectSession.getId(), colorScheme);
			
			// Stores Client location as session's attribute
			String location = ServletUtils.getStringParameter(request, "location", null);
			if (location != null) {
				String url = ServletHelper.sanitizeBaseUrl(location);
				subjectSession.setAttribute(SessionManager.ATTRIBUTE_CLIENT_URL, url);
				if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Location: {}", subjectSession.getId(), url);
			}
			*/
			
			////doKnownDeviceVerification(profileId, subjectSession, httpRequest);
		}
		
		doKnownDeviceVerification(subject, httpRequest);
		
		writeAuthLog((AuthTokenUsernamePasswordDomain)token, httpRequest, LogbackHelper.Level.INFO, "LOGIN_SUCCESS");
		return super.onLoginSuccess(token, subject, request, response);
	}
	
	@Override
	protected AuthenticationToken createToken(String username, String password, ServletRequest request, ServletResponse response) {
		boolean rememberMe = isRememberMe(request);
		final String localUsername = WTRealm.toAuthLocalUsername(username);
		if (WTRealm.isSysAdmin(WTRealm.toAuthInternetDomain(username), localUsername) || WTRealm.isAdminImpersonate(localUsername)) {
			rememberMe = false; // Do NOT allow RememberMe activation for SysAdmin or during impersonation
		}
		return createAuthTokenUsernamePasswordDomain(username, password, getDomain(request), rememberMe, getHost(request));
	}

	@Override
	protected void setFailureAttribute(ServletRequest request, AuthenticationException ae) {
		String value = null;
		if(ae instanceof UnknownAccountException) {
			value = Login.LOGINFAILURE_INVALID;
		} else if (ae instanceof IncorrectCredentialsException) {
			value = Login.LOGINFAILURE_INVALID;
		} else if (ae instanceof DisabledAccountException) {
			value = Login.LOGINFAILURE_DISABLED;
		} else if (ae instanceof LockedAccountException) {
			value = Login.LOGINFAILURE_INVALID;
		} else { // AuthenticationException
			value = Login.LOGINFAILURE_INVALID;
		}
		request.setAttribute(getFailureKeyAttribute(), value);
	}
	
	private AuthTokenUsernamePasswordDomain createAuthTokenUsernamePasswordDomain(String username, String password, String domain, boolean rememberMe, String host) {
		return new AuthTokenUsernamePasswordDomain(username, password, domain, rememberMe, host);
	}
	
	private void doKnownDeviceVerification(final Subject subject, final HttpServletRequest request) {
		final Session subjectSession = SessionContext.getSubjectSession(subject, true);
		final UserProfileId subjectPid = RunContext.getRunProfileId(subject);
		final boolean impersonating = RunContext.isImpersonated();
		final AuditLogManager audMgr = WebTopApp.getInstance().getAuditLogManager();
		
		boolean enabled = audMgr.isKnownDeviceVerificationEnabled(subjectPid);
		if (enabled) {
			final String clientIdentifier = SessionContext.getWTClientID(subjectSession);
			final boolean isCIDJustGenerated = SessionContext.isWTClientIDNew(subjectSession);
			final String[] clientParams = ServletHelper.lookupClientParams(subject, request);
			final IPAddress ip = IPUtils.toIPAddress(clientParams[0]);
			
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Checking known device... [{}, {}] ", subjectPid, clientIdentifier);
			AuditLogManager.KnownDeviceEvalResult result = audMgr.evalAndRememberKnownDevice(subjectPid, clientIdentifier, clientParams[0], clientParams[1]);
			if (KnownDeviceEvalResult.UNKNOWN.equals(result) || (KnownDeviceEvalResult.UNKNOWN_INITIAL.equals(result) && !isCIDJustGenerated)) {
				//TODO: run in new thread to decouple login flow
				WT.runPrivileged(() -> {
					try {
						if (LOGGER.isDebugEnabled()) LOGGER.debug("Verifying whitelist... [{}, {}] ", subjectPid, clientIdentifier);
						List<String> nets = audMgr.getKnownDeviceVerificationNetWhiletist(subjectPid);
						if (nets.isEmpty() || !IPUtils.isAddressInRange(ip, nets.toArray(new String[nets.size()]))) {
							if (LOGGER.isDebugEnabled()) LOGGER.debug("Sending notice... [{}, {}] ", subjectPid, clientIdentifier);
							audMgr.sendUnknownDeviceNotice(subjectPid, impersonating, clientIdentifier, ip, clientParams[1]);
						} else {
							if (LOGGER.isDebugEnabled()) LOGGER.debug("Address '{}' whitelisted. Skipping... [{}, {}]", clientParams[0], subjectPid, clientIdentifier);
						}
					} catch (Exception ex) {
						LOGGER.error("Error evaluating known-device [{}, {}, {}]", subjectPid, clientIdentifier, clientParams[0], ex);
					}	
				});
			}
		}
	}
	
	private void writeAuthLog(final AuthTokenUsernamePasswordDomain token, final HttpServletRequest request, final LogbackHelper.Level level, final String action) {
		final String domainId = StringUtils.defaultIfBlank(token.getDomain(), "?");
		final String userId = StringUtils.defaultIfBlank(token.getUsername(), "?");
		final UserProfileId pid = new UserProfileId(domainId, userId);
		final String sessionId = SessionContext.getCurrentId();
		final String clientIp = ServletUtils.getClientIP(request);
		
		AuditLogManager.logAuth(level, clientIp, sessionId, pid, action);
		WebTopApp.getInstance().getAuditLogManager()
			.write(pid, null, sessionId, CoreManifest.ID, "AUTH", action, null, JsonResult.gson().toJson(new MapItem().add("ip", clientIp)));
	}
	
	private boolean isBackendRequest(final HttpServletRequest request) {
		String ctxRequestUrl = ServletUtils.getContextRelativeRequestURIString(request);
		return ServletHelper.uriPathBelongsTo(ctxRequestUrl, PrivateRequest.URL)
			|| ServletHelper.uriPathBelongsTo(ctxRequestUrl, PrivateRequest.URL_LEGACY) // for backward compatibility only!
			|| ServletHelper.uriPathBelongsTo(ctxRequestUrl, AtmosphereServlet.URL);
	}
	
	private String getDomain(ServletRequest request) {
		return WebUtils.getCleanParam(request, "wtdomain");
	}
}
