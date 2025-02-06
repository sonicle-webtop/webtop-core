/*
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.jaxrs;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;

/**
 *
 * @author malbinola
 */
public class MoreHttpHeaders implements HttpHeaders {
	public static final String X_REQUEST_URL = "x-f5008f1c5dca9f363f8dc37f03f7e897";
	private final HttpHeaders httpHeaders;
	private final String requestUrl;

	public MoreHttpHeaders(HttpHeaders httpHeaders, String requestUrl) {
		this.httpHeaders = httpHeaders;
		this.requestUrl = requestUrl;
	}

	@Override
	public List<String> getRequestHeader(String string) {
		if (X_REQUEST_URL.equalsIgnoreCase(X_REQUEST_URL)) {
			return Arrays.asList(requestUrl);
		} else {
			return httpHeaders.getRequestHeader(string);
		}
	}

	@Override
	public String getHeaderString(String string) {
		return httpHeaders.getHeaderString(string);
	}

	@Override
	public MultivaluedMap<String, String> getRequestHeaders() {
		MultivaluedMap<String, String> origMap = httpHeaders.getRequestHeaders();
		MultivaluedMap<String, String> map = new StringKeyIgnoreCaseMultivaluedMap<>();
		for (Map.Entry<String, List<String>> entry : origMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		map.add(X_REQUEST_URL, requestUrl);
		return map;
	}

	@Override
	public List<MediaType> getAcceptableMediaTypes() {
		return httpHeaders.getAcceptableMediaTypes();
	}

	@Override
	public List<Locale> getAcceptableLanguages() {
		return httpHeaders.getAcceptableLanguages();
	}

	@Override
	public MediaType getMediaType() {
		return httpHeaders.getMediaType();
	}

	@Override
	public Locale getLanguage() {
		return httpHeaders.getLanguage();
	}

	@Override
	public Map<String, Cookie> getCookies() {
		return httpHeaders.getCookies();
	}

	@Override
	public Date getDate() {
		return httpHeaders.getDate();
	}

	@Override
	public int getLength() {
		return httpHeaders.getLength();
	}
}
