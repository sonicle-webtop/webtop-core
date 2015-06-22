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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.BaseRequestServlet;
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.io.FileResource;
import com.sonicle.webtop.core.io.JarFileResource;
import com.sonicle.webtop.core.io.Resource;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class PublicServiceRequest extends BaseRequestServlet {
	
	private static final Logger logger = WT.getLogger(PublicServiceRequest.class);
	public static final String PUBLIC_RESOURCES = "publicresources";
	
	public Resource getPublicFile(WebTopApp wta, String serviceId, String relativePath) {
		try {
			Resource resource = getExternalPublicFile(wta, serviceId, relativePath);
			if(resource == null) resource = getInternalPublicFile(wta, serviceId, relativePath);
			return resource;
			
		} catch(Exception ex) {
			return null;
		}
	}
	
	public FileResource getExternalPublicFile(WebTopApp wta, String serviceId, String relativePath) throws URISyntaxException, MalformedURLException {
		CoreServiceSettings css = new CoreServiceSettings("*", CoreManifest.ID);
		String pathname = LangUtils.joinPaths(css.getSystemPublicPath(), serviceId, relativePath);
		return wta.getFileResource(new File(pathname).toURI().toURL());
	}
	
	public JarFileResource getInternalPublicFile(WebTopApp wta, String serviceId, String relativePath) throws URISyntaxException, IOException {
		if(!StringUtils.startsWith(relativePath, PUBLIC_RESOURCES)) return null;
		String pathname = LangUtils.joinPaths(LangUtils.packageToPath(serviceId), relativePath);
		return wta.getJarResource(this.getClass().getResource("/" + pathname));
	}
	
	public static void writeFile(HttpServletRequest request, HttpServletResponse response, Resource resource) throws Exception {
		InputStream is = null;
		OutputStream os = null;

		try {
			String mimeType = ServletUtils.guessMimeType(resource.getFilename());
			os = ServletUtils.prepareForStreamCopy(request, response, mimeType, (int)resource.getSize(), 4*1024);
			ServletUtils.setContentTypeHeader(response, mimeType);
			if(StringUtils.startsWith(mimeType, "image")) {
				ServletUtils.setCacheControlHeaderPrivateMaxAge(response, 60*60*24);
			} else {
				ServletUtils.setCacheControlHeaderPrivateNoCache(response);
			}
			is = resource.getInputStream();
			ServletUtils.transferStreams(is, os);
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(is);
		}
	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		
		try {
			String[] urlParts = splitPath(request.getPathInfo());
			String publicName = urlParts[0];
			String service = wta.getServiceManager().getServiceIdByPublicName(publicName);
			if(service == null) throw new WTRuntimeException("Unknown public service [{0}]", publicName);
			
			// Returns direct stream if pathInfo points to a real file
			Resource resource = getPublicFile(wta, service, urlParts[1]);
			if(resource != null) {
				writeFile(request, response, resource);
				
			} else {
				// Retrieves instantiated service
				BasePublicService instance = wta.getServiceManager().getPublicService(service);

				// Gets method and invokes it...
				Method method = getMethod(instance.getClass(), service, "Default", true);
				invokeMethod(instance, method, service, true, request, response);
			}
			
		} catch(Exception ex) {
			logger.warn("Error processing publicService request", ex);
			throw new ServletException(ex.getMessage());
		} finally {
			WebTopApp.clearLoggerDC();
		}
	}
	
	@Override
	protected String[] splitPath(String pathInfo) throws MalformedURLException {
		String[] tokens = StringUtils.split(pathInfo, "/", 2);
		if(tokens.length < 1) throw new MalformedURLException("No service provided");
		return new String[]{tokens[0], (tokens.length == 2) ? tokens[1] : ""};
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
}
