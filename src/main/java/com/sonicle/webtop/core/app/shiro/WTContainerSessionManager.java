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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author malbinola
 */
public class WTContainerSessionManager extends ServletContainerSessionManager {
		
	@Override
	public Session getSession(SessionKey key) throws SessionException {
		// This override roughly refers to upstream implementation except the
		// createSession call. We need to access to the original servlet request
		// that originating the session. Unfortunately we do not have any other
		// options except this way.
		
		if (!WebUtils.isHttp(key)) {
			String msg = "SessionKey must be an HTTP compatible implementation.";
			throw new IllegalArgumentException(msg);
		}
		
		HttpServletRequest request = WebUtils.getHttpRequest(key);
		
		Session session = null;
		
		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			session = createSession(httpSession, request);
		}
		
		return session;
	}
	
	@Override
	protected Session createSession(SessionContext sessionContext) throws AuthorizationException {
		// This override roughly refers to upstream implementation except the
		// createSession call. We need to access to the original servlet request
		// that originating the session. Unfortunately we do not have any other
		// options except this way.
		
		if (!WebUtils.isHttp(sessionContext)) {
			String msg = "SessionContext must be an HTTP compatible implementation.";
			throw new IllegalArgumentException(msg);
		}
		
		HttpServletRequest request = WebUtils.getHttpRequest(sessionContext);
		
		HttpSession httpSession = request.getSession();
		
		String host = getHost(sessionContext);
		
		return createSession(httpSession, request, host);
	}

	@Override
	protected Session createSession(HttpSession httpSession, String host) {
		// Simply make sure that this method is no longer used!
		throw new IllegalArgumentException("No more supported");
	}
	
	protected Session createSession(HttpSession httpSession, HttpServletRequest request) {
		return createSession(httpSession, request, request.getRemoteHost());
	}
	
	protected Session createSession(HttpSession httpSession, HttpServletRequest request, String host) {
		return new WTHttpServletSession(httpSession, request, host);
	}
	
	private String getHost(SessionContext context) {
		String host = context.getHost();
		if (host == null) {
			ServletRequest request = WebUtils.getRequest(context);
			if (request != null) {
				host = request.getRemoteHost();
			}
		}
		return host;
	}
}
