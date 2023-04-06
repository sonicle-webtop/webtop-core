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
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.PathUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.WT;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ServletHelper {
	
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
	
	public static String guessMediaType(String filename) {
		return guessMediaType(filename, false);
	}
	
	public static String guessMediaType(String filename, boolean fallback) {
		String defaultMediaType=fallback?"application/octet-stream":null;
		return guessMediaType(filename,defaultMediaType);
	}
	
	public static String guessMediaType(String filename, String defaultMediaType) {
		String ext = FilenameUtils.getExtension(filename);
		if(!StringUtils.isBlank(ext)) {
			String mtype = WT.getOverriddenMediaType(ext);
			if(mtype != null) return mtype;
			mtype = ServletUtils.guessMediaType(filename);
			if(mtype != null) return mtype;
		}
		return defaultMediaType;
	}
	
	/*
	public static void logout(HttpServletRequest request) {
		WebTopSession wts = getWebTopSession(request);
		wts.clear();
		SecurityUtils.getSubject().logout();
	}
	*/
	
	public static Locale homogenizeLocale(HttpServletRequest request) {
		Locale locale = request.getLocale();
		if (locale.getLanguage().equals("it")) {
			return new Locale("it", "IT");
		} else {
			return new Locale("en", "EN");
		}
	}
	
	public static String sanitizeBaseUrl(String url) {
		url = StringUtils.substringBefore(url, "?");
		url = StringUtils.substringBefore(url, Login.URL);
		url = StringUtils.substringBefore(url, Logout.URL);
		url = StringUtils.substringBefore(url, UIPrivate.URL);
		url = StringUtils.substringBefore(url, Otp.URL);
		url = StringUtils.substringBefore(url, ResourceRequest.URL);
		url = StringUtils.substringBefore(url, PrivateRequest.URL);
		url = StringUtils.substringBefore(url, PublicRequest.URL);
		url = StringUtils.substringBefore(url, RestApi.URL);
		return PathUtils.ensureTrailingSeparator(url);
	}
	
	public static boolean isPublic(HttpServletRequest request) {
		return request.getServletPath().equals(PublicRequest.URL);
	}
}
