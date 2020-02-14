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
package com.sonicle.webtop.core.app.shiro.filter;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.web.ServletUtils;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author malbinola
 * @deprecated File robots.txt is not seved anymore. Now meta tags and headers are used.
 */
public class Anonymous extends AnonymousFilter {
	public static final String ROBOTS_FILE = "robots.txt";

	@Override
	public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		String requestPath = getPathWithinApplication(request);
		if ("/robots.txt".equalsIgnoreCase(requestPath)) {
			doWriteRobots(request, response);
		} else {
			super.doFilterInternal(request, response, chain);
		}
	}
	
	protected void doWriteRobots(ServletRequest request, ServletResponse response) throws IOException {
		ClassLoader cl = LangUtils.findClassLoader(this.getClass());
		HttpServletResponse httpResponse = WebUtils.toHttp(response);
		
		InputStream is = null;
		try {
			is = cl.getResourceAsStream(ROBOTS_FILE);
			ServletUtils.setCharacterEncoding(httpResponse, "UTF-8");
			ServletUtils.setCacheControl(httpResponse, 504);
			ServletUtils.writeContent(httpResponse, is, "text/plain");
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
