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
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.util.LoggerUtils;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Logout extends PathMatchingFilter {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Logout.class);
	public static final String URL = "/logout"; // Shiro.ini must reflect this URI!
	public static final String WTSPROP_LOGOUT_DONE = "LOGOUTDONE";
	
	@Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		final HttpServletRequest httpRequest = WebUtils.toHttp(request);
		final HttpServletResponse httpResponse = WebUtils.toHttp(response);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onPreHandle", ServletUtils.getRequestID(httpRequest));
		
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated() || subject.isRemembered()) {
			LoggerUtils.setContextDC(RunContext.getRunProfileId());
			WebTopSession webtopSession = SessionContext.getCurrentWTSession();
			webtopSession.setProperty(CoreManifest.ID, WTSPROP_LOGOUT_DONE, true);
			httpRequest.getSession().setAttribute(SessionManager.ATTRIBUTE_CLIENT_IP, ServletUtils.getClientIP(httpRequest));
			
			if (LOGGER.isDebugEnabled()) LOGGER.debug("[{}] Performing subject logout... [{}]", ServletUtils.getRequestID(httpRequest), RunContext.getRunProfileId(subject));
			try {
				subject.logout();
			} catch (SessionException ex) {
				LOGGER.debug("[{}] Encountered session exception during logout. This can generally safely be ignored. [{}]", ServletUtils.getRequestID(httpRequest), RunContext.getRunProfileId(subject));
			}
		}
		ServletUtils.redirectRequest(httpRequest, httpResponse, "/", true);
		return false;
	}
}
