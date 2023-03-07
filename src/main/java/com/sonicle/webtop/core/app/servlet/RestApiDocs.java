/*
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.MapItemList;
import com.sonicle.commons.web.servlets.assets.AssetsServlet;
import com.sonicle.webtop.core.app.ServiceDescriptor;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.WebTopApp;
import java.io.IOException;
import java.util.List;
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
public class RestApiDocs extends AssetsServlet {
	public static final String URL = "/api-docs"; // Shiro.ini must reflect this URI!
	private static final Logger LOGGER = LoggerFactory.getLogger(RestApiDocs.class);
	private static final String RESOURCE_OPENAPI = "openapi.json";
	private static final String RESOURCE_SWAGGERCONFIG = "swagger-config.json";
	private static final String RESOURCE_INDEX = "index.html";

	@Override
	protected String buildAssetPath(HttpServletRequest request) {
		String path = super.buildAssetPath(request);
		path = StringUtils.removeStartIgnoreCase(path, URL);
		return "/" + StringUtils.substringAfter(StringUtils.removeStart(path, "/"), "/");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			final String reqUrl = ServletUtils.getRequestURLString(req);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No docs available here: please navigate to a URL like '" + reqUrl + "/<service-id>' to see documentation page.");
			
		} else if (!StringUtils.equals(pathInfo, "/") && StringUtils.countMatches(pathInfo, "/") < 2) {
			final String redirectUrl = ServletUtils.getRequestURLString(req) + "/" + RESOURCE_INDEX;
			ServletUtils.redirectRequest(req, resp, redirectUrl);
			
		} else if (StringUtils.endsWithIgnoreCase(pathInfo, "/" + RESOURCE_SWAGGERCONFIG)) {
			ServiceManager svcMgr = getServiceManager(WebTopApp.get(req));
			if (svcMgr == null) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			
			final String serviceId = StringUtils.substringBefore(StringUtils.removeStart(pathInfo, "/"), "/");
			List<ServiceDescriptor.OpenApiDefinition> defs = svcMgr.getOpenApiDefinitions(serviceId);
			if (defs == null || defs.isEmpty()) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			String specBaseUrl = ServletUtils.getRequestURLString(req);
			specBaseUrl = StringUtils.replaceOnce(specBaseUrl, URL, RestApi.URL);
			specBaseUrl = StringUtils.substringBeforeLast(specBaseUrl, "/") + "/" + RESOURCE_OPENAPI;
			MapItemList urls = new MapItemList();
			for (ServiceDescriptor.OpenApiDefinition def : defs) {
				urls.add(new MapItem()
						.add("name", def.context)
						.add("url", specBaseUrl)
					);
			}
			ServletUtils.writeJsonResponse(resp, new MapItem().add("urls", urls));
			
		} else {
			super.doGet(req, resp);
		}
	}
	
	private ServiceManager getServiceManager(WebTopApp wta) {
		return wta != null ? wta.getServiceManager() : null;
	}
}
