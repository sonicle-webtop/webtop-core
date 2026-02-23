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

import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.RMeTokenIssued;
import com.sonicle.webtop.core.app.model.TDTokenIssued;
import com.sonicle.webtop.core.app.shiro.filter.DeviceCookie;
import com.sonicle.webtop.core.app.shiro.filter.Logout;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author malbinola
 */
public class ServletHelper {
	
	public static final String COOKIE_CLIENTID = "CID";
	public static final String COOKIE_KNOWNDEVICE_PREFIX = "KD_";
	@Deprecated public static final String COOKIE_DEVICEID = "DID";
	public static final String COOKIE_TRUSTEDDEVICE_PREFIX = "TD_";
	public static final String COOKIE_REMEMBERME = "RME";
	
	public static boolean uriPathBelongsTo(final String relativeURI, final String basePath) {
		return StringUtils.equalsIgnoreCase(relativeURI, basePath) || StringUtils.startsWithIgnoreCase(relativeURI, basePath + "/");
	}
	
	public static boolean isRequestToLogin(final HttpServletRequest request) {
		return uriPathBelongsTo(ServletUtils.getContextRelativeRequestURIString(request), Login.URL);
	}
	
	public static boolean isSubmissionRequestToLogin(final HttpServletRequest request) {
		return ServletUtils.isPost(request) && uriPathBelongsTo(ServletUtils.getContextRelativeRequestURIString(request), Login.URL);
	}
	
	public static String readClientIDCookieLegacy(final HttpServletRequest request) {
		return ServletUtils.getCookie(request, COOKIE_CLIENTID);
	}
	
	public static CIDCookieValue readClientIDCookie(final HttpServletRequest request) {
		return CIDCookieValue.parse(ServletUtils.getCookie(request, COOKIE_CLIENTID));
	}
	
	public static void writeClientIDCookie(final HttpServletResponse response, final CIDCookieValue value) {
		ServletUtils.setCookie(response, COOKIE_CLIENTID, value.print(), 10 * 365 * 24 * 60 * 60 /* 10y -> unlimited */);
	}
	
	public static String buildTrustedDeviceCookieName(final String suffix) {
		return COOKIE_TRUSTEDDEVICE_PREFIX + suffix;
	}
	
	public static String readTrustedDeviceCookie(final String nameSuffix, final HttpServletRequest request) {
		if (StringUtils.isBlank(nameSuffix)) return null;
		return ServletUtils.getCookie(request, buildTrustedDeviceCookieName(nameSuffix));
	}
	
	public static void writeTrustedDeviceCookie(final String nameSuffix, final HttpServletResponse response, final TDTokenIssued token) {
		if (StringUtils.isBlank(nameSuffix)) return;
		// Set duration to unlimited, expiration is effectively checked server-side in backend!
		ServletUtils.setCookie(response, buildTrustedDeviceCookieName(nameSuffix), token.getTokenPlain(), 10 * 365 * 24 * 60 * 60 /* 10y -> unlimited */);
	}
	
	public static RMeCookieValue readRememberMeCookie(final HttpServletRequest request) {
		String value = ServletUtils.getCookie(request, COOKIE_REMEMBERME);
		String tokens[] = StringUtils.splitByWholeSeparator(value, ".", 2);
		if (tokens != null && tokens.length == 2 && !StringUtils.isEmpty(tokens[0]) && !StringUtils.isEmpty(tokens[1])) {
			return new RMeCookieValue(tokens[0], tokens[1]);
		} else {
			return null;
		}
	}
	
	public static void writeRememberMeCookie(final HttpServletResponse response, final RMeTokenIssued token) {
		writeRememberMeCookie(response, new RMeCookieValue(token.getSelector(), token.getValidatorPlain()), token.getTtl().toStandardSeconds().getSeconds());
	}
	
	public static void writeRememberMeCookie(final HttpServletResponse response, final RMeCookieValue value, final int duration) {
		ServletUtils.setCookie(response, COOKIE_REMEMBERME, value.print(), duration);
	}
	
	// Deprecated ------>
	
	public static DeviceCookie readDeviceCookie(final HttpServletRequest request, final UserProfileId profileId, final String secret) {
		return ServletUtils.getEncryptedCookie(secret, request, buildDeviceCookieName(profileId), DeviceCookie.class);
	}
	
	public static void writeDeviceCookie(final HttpServletResponse response, final UserProfileId profileId, final String secret, final DeviceCookie cookie) {
		int duration = 60*60*24*365*2; // 2 years
		ServletUtils.setEncryptedCookie(secret, response, buildDeviceCookieName(profileId), cookie, DeviceCookie.class, duration);
	}
	
	public static String buildDeviceCookieName(final UserProfileId profileId) {
		return COOKIE_DEVICEID + "-" +DigestUtils.md5Hex(profileId.toString());
	}
	
	private static String generateDeviceId(final String uuid, final String userAgentHeader) {
		String md5UA = DigestUtils.md5Hex(StringUtils.defaultIfBlank(userAgentHeader, "useragent/missing"));
		return DigestUtils.sha1Hex(uuid + "|" + md5UA);
	}
	
	public static DeviceCookie createDeviceCookie(final String userAgentHeader) {
		String uuid = IdentifierUtils.getUUIDTimeBased();
		return new DeviceCookie(uuid, generateDeviceId(uuid, userAgentHeader), JodaTimeUtils.now().getMillis());
	}
	
	// <----- Deprecated
	
	public static String[] lookupClientParams(final Subject subject, final HttpServletRequest request) {
		Session subjectSession = SessionContext.getSubjectSession(subject, true);
		if (subjectSession != null) {
			return new String[]{
				SessionContext.getClientRemoteIP(subjectSession),
				SessionContext.getClientPlainUA(subjectSession)
			};
		} else {
			return new String[]{
				ServletUtils.getClientIP(request),
				ServletUtils.getUserAgent(request)
			};
		}
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
	
	public static String guessMediaType(final String filename) {
		return guessMediaType(filename, false);
	}
	
	public static String guessMediaType(final String filename, final boolean fallback) {
		return guessMediaType(filename, fallback ? "application/octet-stream" : null);
	}
	
	public static String guessMediaType(final String filename, final String defaultMediaType) {
		String ext = FilenameUtils.getExtension(filename);
		if (!StringUtils.isBlank(ext)) {
			String mtype = WT.getOverriddenMediaType(ext);
			if (mtype != null) return mtype;
			mtype = ServletUtils.guessMediaType(filename);
			if (mtype != null) return mtype;
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
