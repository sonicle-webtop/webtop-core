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
package com.sonicle.webtop.core.servlet;

import com.sonicle.webtop.core.LocaleKey;
import com.sonicle.webtop.core.Manifest;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malbinola
 */
public class ServletHelper {
	
	/**
	 * Returns context's root path name, equals to current webapp context name.
	 * @param context The servlet context
	 * @return Webapp's name
	 */
	public static String getWebAppName(ServletContext context) {
		return new File(context.getRealPath("/")).getName();
	}
	
	/**
	 * Retrieve the ID of the current http session.
	 * @param request The http request
	 * @return Session's ID
	 */
	public static String getSessionID(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return getSessionID(session);
	}
	
	/**
	 * Retrieve the ID of the passed http session.
	 * @param session The http session
	 * @return Session's ID
	 */
	public static String getSessionID(HttpSession session) {
		return (session != null) ? session.getId() : "";
	}
	
	/*
	public static void logout(HttpServletRequest request) {
		WebTopSession wts = getWebTopSession(request);
		wts.clear();
		SecurityUtils.getSubject().logout();
	}
	*/
	
	static void setCacheControl(HttpServletResponse response) {
		response.setHeader("Cache-Control", "private");
	}
	
	static void setPageContentType(HttpServletResponse response) {
		response.setContentType("text/html;charset=UTF-8");
	}
	
	public static Locale homogenizeLocale(HttpServletRequest request) {
		Locale locale = request.getLocale();
		if(locale.getLanguage().equals("it")) {
			return new Locale("it", "IT");
		} else {
			return new Locale("en", "EN");
		}
	}
	
	public static boolean isPublic(HttpServletRequest request) {
		return request.getServletPath().equals("/public");
	}
	
	public static String getUserAgent(HttpServletRequest request) {
		return request.getHeader("user-agent");
	}
	
	public static String getReferer(HttpServletRequest request) {
		return request.getHeader("referer");
	}
	
	public static void fillPageVars(Map tplMap, Locale locale, WebTopApp wta) {
		ServiceManifest manifest = wta.getServiceManager().getManifest(Manifest.ID);
		String title = wta.getCustomProperty("webtop.title");
		if (StringUtils.isEmpty(title)) {
			title = wta.lookupAndFormatResource(locale, LocaleKey.LOGIN_TITLE, true, manifest.getVersion().getMajor());
		}
		tplMap.put("title", title);
		tplMap.put("version", manifest.getVersion());
	}
	
	public static void fillSystemInfoVars(Map tplMap, Locale locale, WebTopApp wta) {
		tplMap.put("systemInfo", wta.getSystemInfo());
		tplMap.put("serverInfo", wta.getServerInfo());
		tplMap.put("jdk", System.getProperty("java.version"));
		tplMap.put("appName", wta.getWebAppName());
	}
}
