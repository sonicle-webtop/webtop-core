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
package com.sonicle.webtop.core.app.shiro.filter;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WebTopSession;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class CSRFCheck extends PathMatchingFilter {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CSRFCheck.class);

	@Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		final HttpServletRequest httpRequest = WebUtils.toHttp(request);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onPreHandle", ServletUtils.getRequestID(httpRequest));
		
		HttpSession session = httpRequest.getSession(false);
		if (session == null) return true;
		WebTopSession webtopSession = SessionContext.getWTSession(session);
		if (webtopSession == null) return true;
		
		String token = ServletUtils.getStringParameter(request, "csrf", null);
		if (webtopSession.getCSRFToken().equals(token)) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] CSRF token is valid [{}]", ServletUtils.getRequestID(httpRequest), token);
			return true;
			
		} else {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] CSRF token is invalid, sending error... [{}]", ServletUtils.getRequestID(httpRequest), token);
			WebUtils.toHttp(response).sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF security token not valid");
			return false;
		}
	}
}
