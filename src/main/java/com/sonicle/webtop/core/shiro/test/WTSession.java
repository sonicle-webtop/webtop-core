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
package com.sonicle.webtop.core.shiro.test;

import com.sonicle.commons.web.ServletUtils;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base32;
import org.apache.shiro.session.mgt.SimpleSession;

/**
 *
 * @author malbinola
 */
public class WTSession extends SimpleSession {
	public static final String ATTRIBUTE_CSRF = "csrfToken";
	public static final String ATTRIBUTE_USER_AGENT = "userAgent";
	
	public WTSession() {
		super();
		setAttribute(ATTRIBUTE_CSRF, createCsrfToken());
	}
	
	public WTSession(String host) {
		super(host);
		setAttribute(ATTRIBUTE_CSRF, createCsrfToken());
	}
	
	public WTSession(String host, String userAgent) {
		this(host);
		setAttribute(ATTRIBUTE_USER_AGENT, userAgent);
	}
	
	public String getCsrfToken() {
		return (String)getAttribute(ATTRIBUTE_CSRF);
	}
	
	public String getUserAgent() {
		return (String)getAttribute(ATTRIBUTE_USER_AGENT);
	}
	
	private String createCsrfToken() {
		String csrf = "";
		try {
			csrf = generateSecurityToken();
		} catch(NoSuchAlgorithmException ex) { /* Do nothing... */ }
		return csrf;
	}
	
	private String generateSecurityToken() throws NoSuchAlgorithmException {
		byte[] buffer = new byte[80/8];
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.nextBytes(buffer);
		byte[] secretKey = Arrays.copyOf(buffer, 80/8);
		byte[] encodedKey = new Base32().encode(secretKey);
		return new String(encodedKey).toLowerCase();
	}
}
