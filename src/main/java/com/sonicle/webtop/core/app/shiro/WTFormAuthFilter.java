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

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.PushEndpoint;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.app.servlet.Login;
import com.sonicle.webtop.core.app.servlet.PrivateRequest;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
	private static final Logger logger = (Logger) LoggerFactory.getLogger(WTFormAuthFilter.class);
	public static final String COOKIE_WEBTOP_CLIENTID = "CID";
	
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
		WebTopSession webtopSession = SessionContext.getCurrent();
		if (webtopSession != null) {
			String clientId = ServletUtils.getCookie((HttpServletRequest)request, COOKIE_WEBTOP_CLIENTID);
			if (StringUtils.isBlank(clientId)) {
				clientId = IdentifierUtils.getUUIDTimeBased();
				ServletUtils.setCookie((HttpServletResponse)response, COOKIE_WEBTOP_CLIENTID, clientId, 60*60*24*365*10);
			}
			webtopSession.getSession().setAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID, clientId);
			
			String location = ServletUtils.getStringParameter(request, "location", null);
			if (location != null) {
				String url = ServletHelper.sanitizeBaseUrl(location);
				webtopSession.getSession().setAttribute(SessionManager.ATTRIBUTE_CLIENT_URL, url);
				logger.trace("[{}] Location: {}", webtopSession.getId(), url);
			}
		}
		
		WTRealm wtRealm = (WTRealm)ShiroUtils.getRealmByName(WTRealm.NAME);
		if (wtRealm != null) {
			try {
				wtRealm.checkUser((Principal)subject.getPrincipal());
			} catch(WTException ex) {
				logger.error("User check error", ex);
				writeAuthLog((UsernamePasswordDomainToken)token, (HttpServletRequest)request, "LOGIN_FAILURE");
				setFailureAttribute(request, new AuthenticationException(ex));
				return true;
			}
		}
		
		writeAuthLog((UsernamePasswordDomainToken)token, (HttpServletRequest)request, "LOGIN");
		return super.onLoginSuccess(token, subject, request, response);
	}

	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
		writeAuthLog((UsernamePasswordDomainToken)token, (HttpServletRequest)request, "LOGIN_FAILURE");
		return super.onLoginFailure(token, e, request, response);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		String ctxRequestUrl = ServletUtils.getContextRequestURI((HttpServletRequest)request);
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
	
	private void writeAuthLog(UsernamePasswordDomainToken token, HttpServletRequest request, String action) {
		WebTopApp wta = WebTopApp.getInstance();
		if(wta != null) {
			String domainId = StringUtils.defaultIfBlank(token.getDomain(), "?");
			String userId = StringUtils.defaultIfBlank(token.getUsername(), "?");
			UserProfileId pid = new UserProfileId(domainId, userId);
			wta.getAuditLogManager().write(pid, CoreManifest.ID, "AUTH", action, null, request.getRequestedSessionId(), null);
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
