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

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.servlet.ServletHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.web.session.HttpServletSession;

/**
 *
 * @author malbinola
 */
public class WTHttpServletSession extends HttpServletSession {
	
	public WTHttpServletSession(HttpSession httpSession, HttpServletRequest request, String host) {
		super(httpSession, host);
		httpSession.setAttribute(SessionManager.ATTRIBUTE_REQUEST_DUMPED, "true");
		httpSession.setAttribute(SessionManager.ATTRIBUTE_REFERER_URI, ServletUtils.getReferer(request));
		httpSession.setAttribute(SessionManager.ATTRIBUTE_CLIENT_IP, ServletUtils.getClientIP(request));
		httpSession.setAttribute(SessionManager.ATTRIBUTE_CLIENT_LOCALE, ServletHelper.homogenizeLocale(request));
		httpSession.setAttribute(SessionManager.ATTRIBUTE_CLIENT_USERAGENT, ServletUtils.getUserAgent(request));
		httpSession.setAttribute(SessionManager.ATTRIBUTE_GUESSING_LOCALE, request.getLocale());
	}
}
