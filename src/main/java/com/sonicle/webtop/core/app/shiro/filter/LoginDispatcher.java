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
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.app.servlet.UIPrivate;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class LoginDispatcher extends PathMatchingFilter {
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LoginDispatcher.class);
	
	@Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		final HttpServletRequest httpRequest = WebUtils.toHttp(request);
		final HttpServletResponse httpResponse = WebUtils.toHttp(response);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] onPreHandle", ServletUtils.getRequestID(httpRequest));
		
		// If the request is related to the submission of the login Form, 
		// directly return true to continue to page rendering (usually the login page)
		if (ServletHelper.isSubmissionRequestToLogin(httpRequest)) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Detected POST to login, continuing chain...", ServletUtils.getRequestID(httpRequest));
			return true; // Continue filter chain...
		}
		
		// If the request points to directly retrieving the login page, redirects to / to keep URL clean
		if (ServletHelper.isRequestToLogin(httpRequest) && !ServletUtils.isForwarded(httpRequest) && ServletUtils.isGet(httpRequest)) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Direct GET /login detected, redirecting to '/' to keep URL clean...", ServletUtils.getRequestID(httpRequest));
			ServletUtils.redirectRequest(httpRequest, httpResponse, "/", true);
			return false;
		}
		
		// If the request is already authenticated, forward any to the UI servlet...
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated() || subject.isRemembered()) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Subject already authenticated, forwarding to UI servlet... [{}]", ServletUtils.getRequestID(httpRequest), RunContext.getRunProfileId(subject));
			ServletUtils.forwardRequest(httpRequest, httpResponse, UIPrivate.URL);
			return false;
		}
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Subject NOT authenticated, continuing chain...", ServletUtils.getRequestID(httpRequest));
		return true; // Continue filter chain...
	}
}
