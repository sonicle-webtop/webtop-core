/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class MyIP extends AbstractServlet {
	public static final String URL = "/myip"; // Shiro.ini must reflect this URI!
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(MyIP.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletUtils.sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	@Override
	protected void processGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = getWebTopApp(request);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] processGetOrPost", ServletUtils.getRequestID(request));
		
		try {
			int raw = ServletUtils.getIntParameter(request, "raw", 0);
			if (raw == 1) {
				ServletUtils.setCachingNotAllowed(response);
				writePlain(ServletUtils.getClientIP(request), response);
				
			} else {
				Locale locale = getLocale(request);
				WebTopSession wts = SessionContext.getCurrentWTSession(false);
				if (wts != null) locale = wts.getLocale();
				
				ServletUtils.setCachingNotAllowed(response);
				writePage(wta, locale, ServletUtils.getClientIP(request), response);
			}
			
		} catch (Exception ex) {
			LOGGER.error("[{}] Error processGetOrPost", ServletUtils.getRequestID(request), ex);
			if (!(ex instanceof ServletException) && !(ex instanceof IOException)) {
				throw new ServletException(ex);
			}
		}
	}
	
	private void writePlain(final String ipAddress, final HttpServletResponse response) throws IOException {
		ServletUtils.setContentTypeHeader(response, "text/plain");
		ServletUtils.setCachingNotAllowed(response);
		response.getWriter().write(StringUtils.defaultIfBlank(ipAddress, ""));
	}
	
	private void writePage(final WebTopApp wta, final Locale locale, final String ipAddress, final HttpServletResponse response) throws IOException, TemplateException {
		MapItem vars = new MapItem();
		AbstractServlet.fillPageVars(vars, locale, null, null, null);
		AbstractServlet.fillSystemVars(vars, wta, locale, false, false);
		
		Map data = new HashMap();
		data.put("ipAddress", StringUtils.defaultIfBlank(ipAddress, ""));
		vars.put("data", data);
		
		Map i18n = new HashMap();
		i18n.put("boxTitle", wta.lookupResource(locale, CoreLocaleKey.TPL_MYIP_HELPTITLE));
		vars.put("i18n", i18n);
		
		ServletUtils.setHtmlContentType(response);
		ServletUtils.setCachingNotAllowed(response);
		
		WT.writeTemplate(CoreManifest.ID, "page/myip.html", vars, response.getWriter());
	}
}
