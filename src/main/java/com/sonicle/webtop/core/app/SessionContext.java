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
package com.sonicle.webtop.core.app;

import java.util.Locale;
import javax.servlet.http.HttpSession;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author malbinola
 */
public class SessionContext {
	
	public static WebTopSession getCurrent() {
		return getCurrent(false);
	}
	
	public static WebTopSession getCurrent(boolean createIfNotAvail) {
		Session session = getShiroSession(RunContext.getSubject(), createIfNotAvail);
		return getWebTopSession(session);
	}
	
	public static Session getShiroSession() {
		return getShiroSession(false);
	}
	
	public static Session getShiroSession(boolean createIfNotAvail) {
		return getShiroSession(RunContext.getSubject(), createIfNotAvail);
	}
	
	public static Session getShiroSession(Subject subject, boolean createIfNotAvail) {
		return (subject == null) ? null : (Session)subject.getSession(createIfNotAvail);
	}
	
	public static String getWebTopClientID(HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID);
	}
	
	public static String getClientRemoteIP(HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_IP);
	}
	
	public static String getClientUrl(HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_URL);
	}
	
	public static String getRefererUri(HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_REFERER_URI);
	}
	
	public static Locale getClientLocale(HttpSession session) {
		return (session == null) ? null : (Locale)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_LOCALE);
	}
	
	public static String getClientPlainUserAgent(HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_USERAGENT);
	}
	
	public static WebTopSession getWebTopSession(HttpSession session) {
		return (session == null) ? null : (WebTopSession)session.getAttribute(SessionManager.ATTRIBUTE_WEBTOP_SESSION);
	}
	
	public static WebTopSession getWebTopSession(Session session) {
		return (session == null) ? null : (WebTopSession)session.getAttribute(SessionManager.ATTRIBUTE_WEBTOP_SESSION);
	}
}
