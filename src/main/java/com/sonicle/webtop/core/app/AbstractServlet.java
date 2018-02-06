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

import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.util.LoggerUtils;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class AbstractServlet extends HttpServlet {
	
	protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	
	protected WebTopApp getWebTopApp(HttpServletRequest request) throws ServletException {
		return WebTopApp.get(request);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch(Throwable t) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		} finally {
			LoggerUtils.clearDC();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch(Throwable t) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		} finally {
			LoggerUtils.clearDC();
		}
	}
	
	public static void fillSystemVars(Map vars, WebTopApp wta, Locale locale, boolean showSystemInfo, boolean showWebappInfo) {
		String osInfo = wta.getOSInfo();
		String appServerInfo = wta.getAppServerInfo();
		String jdk = System.getProperty("java.version");
		String webappName = wta.getWebappName();
		vars.put("osInfo", osInfo);
		vars.put("appServerInfo", appServerInfo);
		vars.put("jdk", jdk);
		vars.put("webappName", webappName);
		String serverInfo = "";
		if (showSystemInfo) serverInfo += "Hosted on " + osInfo + " - " + appServerInfo + " - Java " + jdk;
		if (showWebappInfo) {
			if (!StringUtils.isBlank(serverInfo)) serverInfo += " - ";
			serverInfo += webappName;
		}
		vars.put("serverInfo", StringUtils.defaultIfBlank(serverInfo, null));
	}
	
	public static void fillPageVars(Map vars, Locale locale, String baseUrl) {
		fillPageVars(vars, locale, null, baseUrl);
	}
	
	public static void fillPageVars(Map vars, Locale locale, String userTitle, String baseUrl) {
		ServiceVersion version = WT.getManifest(CoreManifest.ID).getVersion();
		String title = WT.getPlatformName() + " " + version.getMajor();
		if (!StringUtils.isBlank(userTitle)) title += " [" + userTitle + "]";
		vars.put("title", title);
		vars.put("version", version);
		vars.put("baseUrl", baseUrl);
	}
}
