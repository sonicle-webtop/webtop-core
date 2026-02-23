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
	
	public static String getClientRemoteIP(Session session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_IP);
	}
	
	public static String getClientRemoteIP(HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_IP);
	}
	
	public static String getClientPlainUA(final Session session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_USERAGENT);
	}
	
	public static String getClientPlainUA(final HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_USERAGENT);
	}
	
	public static Locale getClientLocale(final Session session) {
		return (session == null) ? null : (Locale)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_LOCALE);
	}
	
	public static Locale getClientLocale(final HttpSession session) {
		return (session == null) ? null : (Locale)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_LOCALE);
	}
	
	public static String getClientColorScheme(final Session session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_COLORSCHEME);
	}
	
	public static String getClientColorScheme(final HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_COLORSCHEME);
	}
	
	public static String getClientLocation(final Session session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_LOCATION);
	}
	
	public static String getClientLocation(final HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_CLIENT_LOCATION);
	}
	
	public static String getRefererUri(final HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_REFERER_URI);
	}
	
	public static String getRefererUri(final Session session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_REFERER_URI);
	}
	
	public static String getWTClientID(final Session session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID);
	}
	
	public static String getWTClientID(final HttpSession session) {
		return (session == null) ? null : (String)session.getAttribute(SessionManager.ATTRIBUTE_WEBTOP_CLIENTID);
	}
	
	public static WebTopSession getWTSession(final HttpSession session) {
		return (session == null) ? null : (WebTopSession)session.getAttribute(SessionManager.ATTRIBUTE_WEBTOP_SESSION);
	}
	
	public static WebTopSession getWTSession(final Session session) {
		return (session == null) ? null : (WebTopSession)session.getAttribute(SessionManager.ATTRIBUTE_WEBTOP_SESSION);
	}
	
	/**
	 * Returns the Session object associated to the current executing Subject.
	 * @return the Shiro session object, or null
	 */
	public static Session getSubjectSession() {
		return getSubjectSession(RunContext.getSubject());
	}
	
	/**
	 * Returns the Session object associated to the passed Subject.
	 * No session will be created if there is no existing one.
	 * @param subject The Subject whose session to return to.
	 * @return the Shiro session object, or null
	 */
	public static Session getSubjectSession(final Subject subject) {
		return getSubjectSession(subject, false);
	}
	
	/**
	 * Returns the Session object associated to the current executing Subject.
	 * If there is no existing session a new one will be created according to 
	 * the dedicated boolean parameter.
	 * @param createIfNotAvail Set to `true` to create a new session if not existing.
	 * @return the Shiro session object, or null
	 */
	public static Session getSubjectSession(final boolean createIfNotAvail) {
		return getSubjectSession(RunContext.getSubject(), createIfNotAvail);
	}
	
	/**
	 * Returns the Session object associated to the passed Subject.
	 * If there is no existing session a new one will be created according to 
	 * the dedicated boolean parameter.
	 * @param subject The Subject whose session to return to.
	 * @param createIfNotAvail Set to `true` to create a new session if not existing.
	 * @return the Shiro session object, or null
	 */
	public static Session getSubjectSession(final Subject subject, final boolean createIfNotAvail) {
		return (subject == null) ? null : (Session)subject.getSession(createIfNotAvail);
	}
	
	/**
	 * Returns the Session ID associated to the current executing Subject.
	 * @return the Shiro session ID or null not already there
	 */
	public static String getCurrentId() {
		Session session = getSubjectSession();
		return (session != null) ? session.getId().toString() : null;
	}
	
	/**
	 * Returns the current WebTop Session object.
	 * @return WebTop Session
	 */
	public static WebTopSession getCurrentWTSession() {
		return getCurrentWTSession(false);
	}
	
	/**
	 * Returns the current WebTop Session object.
	 * If there is no existing session a new one will be created according to 
	 * the dedicated boolean parameter.
	 * @param createIfNotAvail Set to `true` to create a new session if not existing.
	 * @return WebTop Session
	 */
	public static WebTopSession getCurrentWTSession(final boolean createIfNotAvail) {
		Session session = getSubjectSession(createIfNotAvail);
		return getWTSession(session);
	}
	
	/**
	 * @deprecated use getCurrentWTSession() instead
	 */
	@Deprecated public static WebTopSession getCurrent() {
		return getCurrent(false);
	}
	
	/**
	 * @deprecated use getCurrentWTSession(boolean) instead
	 */
	@Deprecated public static WebTopSession getCurrent(boolean createIfNotAvail) {
		Session session = getSubjectSession(RunContext.getSubject(), createIfNotAvail);
		return getWTSession(session);
	}
}
