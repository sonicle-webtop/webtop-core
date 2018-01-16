/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.shiro;

import com.sonicle.commons.web.ContextUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.io.Serializable;
import java.util.Locale;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WTSessionManager extends DefaultWebSessionManager {
	private static final Logger logger = LoggerFactory.getLogger(WTSessionManager.class);
	
	public static final String COOKIE_WEBTOP_CLIENTID = "CID";
	public static final String ATTRIBUTE_CONTEXT_NAME = "contextName";
	public static final String ATTRIBUTE_CSRF_TOKEN = "csrf";
	public static final String ATTRIBUTE_WEBTOP_CLIENTID = "clientId";
	public static final String ATTRIBUTE_CLIENT_IP = "clientIp";
	public static final String ATTRIBUTE_CLIENT_URL = "clientUrl";
	public static final String ATTRIBUTE_REFERER_URI = "refererUri";
	public static final String ATTRIBUTE_CLIENT_LOCALE = "clientLocale";
	public static final String ATTRIBUTE_CLIENT_USERAGENT = "clientUA";
	public static final String ATTRIBUTE_WEBTOP_SESSION = "wts";

	/*
	@Override
	protected Session createSession(SessionContext context) throws AuthorizationException {
		if (WebUtils.isHttp(context)) {
			HttpServletRequest request = WebUtils.getHttpRequest(context);
			HttpSession httpSession = request.getSession(false);
			if (httpSession == null) {
				logger.debug("Session non exist");
				httpSession = request.getSession();
				logger.debug("Session created {}", httpSession.getId());
			} else {
				logger.debug("Session already exist {}", httpSession.getId());
			}
		}
		
		return super.createSession(context);
	}
	*/
	
	@Override
	protected Session newSessionInstance(SessionContext context) {
		Session session = super.newSessionInstance(context);
		final HttpServletRequest request = WebUtils.getHttpRequest(context);
		
		String clientId = ServletUtils.getCookie(request, COOKIE_WEBTOP_CLIENTID);
		if (StringUtils.isBlank(clientId)) {
			clientId = IdentifierUtils.getUUIDTimeBased();
			ServletUtils.setCookie(WebUtils.getHttpResponse(context), COOKIE_WEBTOP_CLIENTID, clientId, 60*60*24*365*10);
		}
		
		session.setAttribute(ATTRIBUTE_CONTEXT_NAME, ContextUtils.getWebappName(request.getServletContext()));
		session.setAttribute(ATTRIBUTE_CSRF_TOKEN, IdentifierUtils.getCRSFToken());
		session.setAttribute(ATTRIBUTE_WEBTOP_CLIENTID, clientId);
		session.setAttribute(ATTRIBUTE_REFERER_URI, ServletUtils.getReferer(request));
		session.setAttribute(ATTRIBUTE_CLIENT_LOCALE, ServletHelper.homogenizeLocale(request));
		session.setAttribute(ATTRIBUTE_CLIENT_USERAGENT, ServletUtils.getUserAgent(request));
		
		//session.setAttribute(WTSessionIdGenerator.SID_SUFFIX, ContextUtils.getWebappVersion(request.getServletContext()));
		return session;
	}

	@Override
	protected void onStart(Session session, SessionContext context) {
		super.onStart(session, context);
		session.setAttribute(ATTRIBUTE_WEBTOP_SESSION, new WebTopSession(session));
		
		//final String clientId = (String)session.getAttribute(ATTRIBUTE_WEBTOP_CLIENTID);
		//ServletUtils.setCookie(WebUtils.getHttpResponse(context), COOKIE_WEBTOP_CLIENTID, clientId, 60*60*24*365*10);
	}

	@Override
	protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
		Serializable ser = super.getSessionId(request, response);
		//logger.trace("getSessionId(): {}", ser);
		String id = getSessionIdCookieValue(request, response);
		//logger.trace("getSessionIdCookieValue(): {}", id);
		Object refSesId = request.getAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID);
		//logger.trace("refSesId: {}", refSesId);
		return ser;
	}
	
	private String getSessionIdCookieValue(ServletRequest request, ServletResponse response) {
        if (!isSessionIdCookieEnabled()) {
            //log.debug("Session ID cookie is disabled - session id will not be acquired from a request cookie.");
            return null;
        }
        if (!(request instanceof HttpServletRequest)) {
            //log.debug("Current request is not an HttpServletRequest - cannot get session ID cookie.  Returning null.");
            return null;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        return getSessionIdCookie().readValue(httpRequest, WebUtils.toHttp(response));
    }
	
	
	
	public static String getCSRFToken(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_CSRF_TOKEN);
	}
	
	public static String getWebTopClientID(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_WEBTOP_CLIENTID);
	}
	
	public static String getClientIP(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_CLIENT_IP);
	}
	
	public static void setClientUrl(Session session, String clientUrl) {
		logger.trace("ClientUrl: {}", clientUrl); //TODO: rimuovere logging
		session.setAttribute(ATTRIBUTE_CLIENT_URL, clientUrl);
	}
	
	public static String getRefererUri(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_REFERER_URI);
	}
	
	public static Locale getClientLocale(Session session) {
		return (Locale)session.getAttribute(ATTRIBUTE_CLIENT_LOCALE);
	}
	
	public static String getClientUserAgent(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_CLIENT_USERAGENT);
	}
	
	public static WebTopSession getWebTopSession(Session session) {
		return (WebTopSession)session.getAttribute(ATTRIBUTE_WEBTOP_SESSION);
	}
}
