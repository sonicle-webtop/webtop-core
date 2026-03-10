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

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.model.RMeTokenConsumed;
import com.sonicle.webtop.core.app.model.RMeTokenInfo;
import com.sonicle.webtop.core.app.model.RMeTokenIssued;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.servlet.RMeCookieValue;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.app.servlet.UIPrivate;
import com.sonicle.webtop.core.app.shiro.AuthTokenRMe;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class AuthRMe extends PathMatchingFilter {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AuthRMe.class);
	
	@Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		final HttpServletRequest httpRequest = WebUtils.toHttp(request);
		final HttpServletResponse httpResponse = WebUtils.toHttp(response);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onPreHandle", ServletUtils.getRequestID(httpRequest));
		
		Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
			if (ServletHelper.isSubmissionRequestToLogin(httpRequest)) {
				// If the request refers to a sumbit from login form, 
				// directly return true to continue to page rendering (usually the login page)
				if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Detected POST to login, continuing chain...", ServletUtils.getRequestID(httpRequest));
				return true; // Continue filter chain...
				
			} else if (ServletUtils.isSpeculativeRequest(httpRequest)) {
				if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Detected speculative request, continuing chain...", ServletUtils.getRequestID(httpRequest));
				return true; // Continue filter chain...
			}
			
			try {
				final WebTopApp wta = WebTopApp.getInstance();
				if (!wta.isInMaintenance()) {
					if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Subject NOT authenticated, try reading RMe cookie...", ServletUtils.getRequestID(httpRequest));
					RMeCookieValue rmeCookie = ServletHelper.readRememberMeCookie(httpRequest);
					if (rmeCookie != null) {
						final WebTopManager wtMgr = wta.getWebTopManager();
						final CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
						
						if (css.getLoginRememberMeEnabled()) {
							if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Validating RMe cookie... [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
							Result<RMeTokenInfo> result = wtMgr.validateRememberMeToken(rmeCookie.getSelector(), rmeCookie.getValidator(), null);
							if (result.getObject() == null) {
								if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Invalid RMe cookie, erasing it... [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
								ServletUtils.eraseCookie(httpResponse, ServletHelper.COOKIE_REMEMBERME);

							} else {
								if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] RMe cookie OK, performing login... [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
								if (executeLogin(httpRequest, httpResponse, result.getObject())) {
									if (result.getObject() instanceof RMeTokenIssued) {
										if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Login done, writing RMe cookie... [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
										ServletHelper.writeRememberMeCookie(httpResponse, (RMeTokenIssued)result.getObject());
									} else if (result.getObject() instanceof RMeTokenConsumed) {
										if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Login done, updating RMe cookie expiration [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
										ServletHelper.writeRememberMeCookie(httpResponse, rmeCookie, ((RMeTokenConsumed)result.getObject()).getTTL());
									} else {
										if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Login done [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
									}
									ServletUtils.forwardRequest(httpRequest, httpResponse, UIPrivate.URL);
									return false;

								} else {
									if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Login failed, erasing RMe cookie... [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
									ServletUtils.eraseCookie(httpResponse, ServletHelper.COOKIE_REMEMBERME);
								}
							}
							
						} else {
							if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] RMe disabled, erasing RMe cookie... [{}]", ServletUtils.getRequestID(httpRequest), rmeCookie.getSelector());
							ServletUtils.eraseCookie(httpResponse, ServletHelper.COOKIE_REMEMBERME);
							wtMgr.revokeRememberMeToken(rmeCookie.getSelector());
						}
						
					} else {
						if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] No RMe cookie found!", ServletUtils.getRequestID(httpRequest));
					}
					
				} else {
					if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Maintenance detected", ServletUtils.getRequestID(httpRequest));
				}	
				
			} catch (Exception ex) {
				LOGGER.error("[{}] Error onPreHandle", ServletUtils.getRequestID(httpRequest), ex);
			}
		}
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Continuing chain...", ServletUtils.getRequestID(httpRequest));
		return true; // Continue filter chain...
	}
	
	protected boolean executeLogin(HttpServletRequest request, HttpServletResponse response, RMeTokenInfo rmeToken) {
		AuthenticationToken token = createToken(request, response, rmeToken);
		if (token == null) {
			throw new IllegalStateException("createToken method implementation returned null. A valid non-null AuthenticationToken must be created in order to execute a login attempt.");
		}
		
		try {
			Subject subject = SecurityUtils.getSubject();
			subject.login(token);
			return true;
			
		} catch (AuthenticationException ex) {
			return false;
		}
	}
	
	protected AuthenticationToken createToken(HttpServletRequest request, HttpServletResponse response, RMeTokenInfo rmeToken) {
		return new AuthTokenRMe(rmeToken.getProfileId().toString());
	}
}

