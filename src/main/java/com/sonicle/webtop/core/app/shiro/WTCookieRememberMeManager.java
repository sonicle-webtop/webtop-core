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
package com.sonicle.webtop.core.app.shiro;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.model.CIDTokenIssued;
import com.sonicle.webtop.core.app.model.RMeTokenIssued;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.servlet.CIDCookieValue;
import com.sonicle.webtop.core.app.servlet.RMeCookieValue;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.sdk.UserProfileId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WTCookieRememberMeManager implements RememberMeManager {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WTCookieRememberMeManager.class);
	
	@Override
	public PrincipalCollection getRememberedPrincipals(SubjectContext subjectContext) {
		return null;
	}

	@Override
	public void forgetIdentity(SubjectContext subjectContext) {
		forgetIdentity(subjectContext.getSubject());
	}

	@Override
	public void onSuccessfulLogin(Subject subject, AuthenticationToken token, AuthenticationInfo info) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("onSuccessfulLogin({})", UserProfileId.from((Principal)subject.getPrincipal()));
		
		// After the introduction of CookieRememberMeManager, this onSuccessfulLogin 
		// is an hook-point that is called before any other success methods in 
		// filters (see onLoginSuccess in autc).
		// So, to make sure to initialize things properly, some of statements of 
		// original onLoginSuccess was moved here!
		
		final HttpServletRequest request = WebUtils.getHttpRequest(subject);
		final HttpServletResponse response = WebUtils.getHttpResponse(subject);
		
		if (isAuthC(token)) { // Is authenticated through authc?
			Session subjectSession = SessionContext.getSubjectSession(subject, true);
			if (subjectSession != null) {
				final UserProfileId subjectPid = RunContext.getRunProfileId(subject);
				initSessionAttributes(subjectPid, subjectSession, request, response);
				initClientID(subjectPid, subjectSession, request, response);
			}
			
		} else if (isAuthRMe(token)) { // Is authenticated through rme?
			Session subjectSession = SessionContext.getSubjectSession(subject);
			if (subjectSession != null) {
				final UserProfileId subjectPid = RunContext.getRunProfileId(subject);
				initSessionAttributes(subjectPid, subjectSession, request, response);
				initClientID(subjectPid, subjectSession, request, response);
			}
		}
		
		if (isRememberMeActivation(token)) {
			rememberIdentity(subject, request, response);
			
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("AuthenticationToken did not indicate RememberMe activation is requested. Functionality ignored.");
			}
		}
	}
	
	@Override
	public void onFailedLogin(Subject subject, AuthenticationToken token, AuthenticationException ae) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("onFailedLogin({})", UserProfileId.from((Principal)subject.getPrincipal()));
		forgetIdentity(subject);
	}

	@Override
	public void onLogout(Subject subject) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("onLogout({})", UserProfileId.from((Principal)subject.getPrincipal()));
		forgetIdentity(subject);
	}
	
	private void initSessionAttributes(final UserProfileId subjectPid, final Session subjectSession, final HttpServletRequest request, final HttpServletResponse response) {
		// Stores Client colorScheme as session's attribute
		String colorScheme = ServletUtils.getStringParameter(request, "colorscheme", null);
		if (colorScheme != null && !("light".equals(colorScheme) || "dark".equals(colorScheme))) colorScheme = null;
		subjectSession.setAttribute(SessionManager.ATTRIBUTE_CLIENT_COLORSCHEME, StringUtils.defaultIfBlank(colorScheme, "light"));
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] ColorScheme: {}", subjectSession.getId(), colorScheme);
		
		// Stores Client location as session's attribute
		String location = ServletUtils.getStringParameter(request, "location", null);
		if (location != null) {
			String url = ServletHelper.sanitizeBaseUrl(location);
			subjectSession.setAttribute(SessionManager.ATTRIBUTE_CLIENT_LOCATION, url);
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Location: {}", subjectSession.getId(), url);
		}
	}
	
	private void initClientID(final UserProfileId subjectPid, final Session subjectSession, final HttpServletRequest request, final HttpServletResponse response) {
		final String clientId = SessionContext.getWTClientID(subjectSession);
		if (StringUtils.isBlank(clientId)) {
			boolean valueIsNew = false;
			CIDCookieValue value = ServletHelper.readClientIDCookie(request);
			//String legacyValue = ServletHelper.readClientIDCookieLegacy(request);
			if (value == null/* && StringUtils.isBlank(legacyValue)*/) {
				value = generateNewCIDCookieValue(subjectPid);
				ServletHelper.writeClientIDCookie(response, value);
				subjectSession.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID_ISNEW, true);
				valueIsNew = true;
			}
			
			if (value != null) { // Cookie parsing ok, now we must verify signature...
				if (!valueIsNew && !verifyCIDCookieValue(value)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Signature invalid: CID cookie may be tampered, generating new one...");
					}
					value = generateNewCIDCookieValue(subjectPid);
					ServletHelper.writeClientIDCookie(response, value);
					subjectSession.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID_ISNEW, true);
				}
 				subjectSession.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID, value.getClientIdentifier());
			}/* else if (legacyValue != null) {
				subjectSession.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID, legacyValue);
			}*/
		}
	}
	
	private CIDCookieValue generateNewCIDCookieValue(final UserProfileId subjectPid) {
		final WebTopManager wtMgr = WebTopApp.getInstance().getWebTopManager();
		
		try {
			CIDTokenIssued result = wtMgr.issueClientIDToken(subjectPid.getDomainId(), subjectPid.getUserId(), CIDCookieValue.VERSION);
			return new CIDCookieValue(result.getVersion(), result.getClientIdentifier(), result.getSignature());
			
		} catch (Exception ex) {
			LOGGER.error("Error generating CIDCookieValue", ex);
			return null;
		}
	}
	
	private boolean verifyCIDCookieValue(final CIDCookieValue value) {
		final WebTopManager wtMgr = WebTopApp.getInstance().getWebTopManager();
		
		try {
			return wtMgr.validateClientIDToken(value.getVersion(), value.getClientIdentifier(), value.getSignature());
			
		} catch (Exception ex) {
			LOGGER.error("Error verifyng CIDCookieValue", ex);
			return false;
		}
	}
	
	private void rememberIdentity(final Subject subject, final HttpServletRequest request, final HttpServletResponse response) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("rememberIdentity [{}]", subject.getPrincipal());
		
		final WebTopManager wtMgr = WebTopApp.getInstance().getWebTopManager();
		final UserProfileId profileId = RunContext.getRunProfileId(subject);
		final String clientIdentifier = SessionContext.getWTClientID(SessionContext.getSubjectSession(subject));
		final String[] clientParams = ServletHelper.lookupClientParams(subject, request);
		
		try {
			// Revoke any previous token...
			RMeCookieValue rmeCookie = ServletHelper.readRememberMeCookie(request);
			if (rmeCookie != null) {
				wtMgr.revokeRememberMeToken(rmeCookie.getSelector());
			}
			
			// Then, issue a new token...
			Result<RMeTokenIssued> result = wtMgr.issueRememberMeToken(profileId.getDomainId(), profileId.getUserId(), clientIdentifier, clientParams[0], clientParams[1]);
			if (result.getObject() != null) {
				if (LOGGER.isTraceEnabled()) LOGGER.trace("Writing RMe cookie... [{}, {}]", subject.getPrincipal(), result.getObject().getSelector());
				ServletHelper.writeRememberMeCookie(response, result.getObject());
			}
			
		} catch (Exception ex) {
			LOGGER.error("Error remembering new identity", ex);
		}
	}
	
	private void forgetIdentity(final Subject subject) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("Forgetting identity... [{}]", subject.getPrincipal());
		final HttpServletRequest request = WebUtils.getHttpRequest(subject);
		final HttpServletResponse response = WebUtils.getHttpResponse(subject);
		
		final WebTopManager wtMgr = WebTopApp.getInstance().getWebTopManager();
		try {
			// Revoke any previous token...
			RMeCookieValue rmeCookie = ServletHelper.readRememberMeCookie(request);
			if (rmeCookie != null) {
				wtMgr.revokeRememberMeToken(rmeCookie.getSelector());
			}
			
			// Then, force cookie cleanup
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Cleaning RMe cookie... [{}]", subject.getPrincipal());
			ServletUtils.eraseCookie(response, ServletHelper.COOKIE_REMEMBERME);
			
		} catch (Exception ex) {
			LOGGER.error("Error forgetting remembered identity", ex);
		}
	}
	
	private boolean isAuthC(final AuthenticationToken token) {
		return token != null && (token instanceof UsernamePasswordToken);
	}
	
	private boolean isAuthRMe(final AuthenticationToken token) {
		return token != null && (token instanceof AuthTokenRMe);
	}
	
	private boolean isRememberMeActivation(final AuthenticationToken token) {
		return token != null && (token instanceof RememberMeAuthenticationToken) && ((RememberMeAuthenticationToken) token).isRememberMe();
	}
}
