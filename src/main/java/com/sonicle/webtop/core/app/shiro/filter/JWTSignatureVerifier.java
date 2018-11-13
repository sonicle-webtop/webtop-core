/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.shiro.filter;

import groovy.json.internal.Charsets;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class JWTSignatureVerifier extends PathMatchingFilter {
	private final static Logger logger = (Logger)LoggerFactory.getLogger(JWTSignatureVerifier.class);
	public static final String SECRET_CONTEXT_ATTRIBUTE = "jwtsignatureverifierfilter.secret";
	protected static final String AUTHORIZATION_HEADER = "Authorization";
	protected static final String AUTHORIZATION_SCHEME_BEARER = "Bearer ";

	@Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		// Retrieve signing key
		SecretKey signingKey = getSigningKey(request);
		if (signingKey == null) {
			logger.warn("Missing JWT secret. Please check '{}' context attribute.", SECRET_CONTEXT_ATTRIBUTE);
			return true;
		}
		
		// Extracts the JWT string
		String authz = getAuthzHeader(request);
		if (!StringUtils.startsWith(authz, AUTHORIZATION_SCHEME_BEARER)) {
			if (logger.isTraceEnabled()) {
				logger.trace("Authorization header is missing or malformed");
				logger.trace("{}: {}", AUTHORIZATION_HEADER, authz);
			}
			WebUtils.toHttp(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization header is missing or malformed");
			return false;
		}
		String jwts = authz.substring(authz.indexOf(" ")+1).trim();
		
		try {
			Jwts.parser()
				.setSigningKey(signingKey)
				.parseClaimsJws(jwts);
			return true;
			
		} catch(JwtException ex) {
			logger.trace("Unable to parse JWT string [{}]", ex, jwts);
			WebUtils.toHttp(response).sendError(HttpServletResponse.SC_FORBIDDEN, "JWT token not signed correctly");
			return false;
		}
	}
	
	protected SecretKey getSigningKey(ServletRequest request) {
		SignatureAlgorithm keyAlgorithm = SignatureAlgorithm.HS256;
		String secret = String.valueOf(request.getServletContext().getAttribute(SECRET_CONTEXT_ATTRIBUTE));
		return StringUtils.isBlank(secret) ? null : new SecretKeySpec(secret.getBytes(Charsets.UTF_8), keyAlgorithm.getJcaName());
	}
	
	protected String getAuthzHeader(ServletRequest request) {
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		return httpRequest.getHeader(AUTHORIZATION_HEADER);
	}
}
