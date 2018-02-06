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
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.io.FileResource;
import com.sonicle.webtop.core.io.JarFileResource;
import com.sonicle.webtop.core.io.Resource;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class PublicRequest extends BaseRequest {
	public static final String URL = "public"; // This must reflect web.xml!
	private static final Logger logger = WT.getLogger(PublicRequest.class);
	public static final String PUBLIC_RESOURCES = "publicresources";
	
	public Resource getPublicFile(WebTopApp wta, String serviceId, String relativePath) {
		try {
			if (StringUtils.isBlank(relativePath)) return null;
			Resource resource = getExternalPublicFile(wta, serviceId, relativePath);
			if (resource == null) resource = getInternalPublicFile(wta, serviceId, relativePath);
			return resource;
			
		} catch(Exception ex) {
			return null;
		}
	}
	
	public FileResource getExternalPublicFile(WebTopApp wta, String serviceId, String relativePath) throws URISyntaxException, MalformedURLException {
		//TODO: recuperare la path dalla getPublicPath() per dominio
		String pathname = LangUtils.joinPaths("", serviceId, relativePath);
		return wta.getFileResource(new File(pathname).toURI().toURL());
	}
	
	public JarFileResource getInternalPublicFile(WebTopApp wta, String serviceId, String relativePath) throws URISyntaxException, IOException {
		if (!StringUtils.startsWith(relativePath, PUBLIC_RESOURCES)) return null;
		String pathname = LangUtils.joinPaths(LangUtils.packageToPath(serviceId), relativePath);
		return wta.getJarResource(this.getClass().getResource("/" + pathname));
	}
	
	public static void writeFile(HttpServletRequest request, HttpServletResponse response, Resource resource) throws Exception {
		InputStream is = null;
		OutputStream os = null;

		try {
			String mimeType = ServletUtils.guessMimeType(resource.getFilename());
			ServletUtils.setContentTypeHeader(response, mimeType);
			if (StringUtils.startsWith(mimeType, "image")) {
				ServletUtils.setCacheControlPrivateMaxAge(response, 60*60*24);
			} else {
				ServletUtils.setCacheControlPrivateNoCache(response);
			}
			os = ServletUtils.prepareForStreamCopy(request, response, mimeType, (int)resource.getSize(), ServletUtils.GZIP_MIN_THRESHOLD);
			is = resource.getInputStream();
			ServletUtils.transferStreams(is, os);
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(is);
		}
	}
	
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		WebTopSession wts = SessionContext.getCurrent(true);
		
		try {
			String serviceId = ServletUtils.getStringParameter(request, "service", null);
			
			String relativePath = null;
			if (serviceId != null) { // Checks if service ID is valid
				wts.initPublicEnvironment(request, serviceId);
				
			} else { // Retrieves public service ID using its public name
				String[] urlParts = splitPath(request.getPathInfo());
				serviceId = wta.getServiceManager().getServiceIdByPublicName(urlParts[1]);
				if (serviceId == null) throw new WTRuntimeException("Public name not known [{0}]", urlParts[1]);
				wts.initPublicEnvironment(request, serviceId);
				relativePath = urlParts[2];
			}
			
			// Returns direct stream if pathInfo points to a real file
			Resource resource = getPublicFile(wta, serviceId, relativePath);
			if (resource != null) {
				writeFile(request, response, resource);
				
			} else {
				String action = ServletUtils.getStringParameter(request, "action", false);
				Boolean nowriter = ServletUtils.getBooleanParameter(request, "nowriter", false);
				
				// Retrieves instantiated service
				BasePublicService instance = wts.getPublicServiceById(serviceId);

				// Gets method and invokes it...
				MethodInfo meinfo = getMethod(instance.getClass(), serviceId, action, nowriter);
				invokeMethod(instance, meinfo, serviceId, request, response);
			}
			
		} catch(Exception ex) {
			logger.warn("Error processing publicService request", ex);
			throw new ServletException(ex.getMessage());
		}
	}
	
	@Override
	protected String[] splitPath(String pathInfo) throws MalformedURLException {
		/*
			URL path is something like this:
			.../{domainPublicName}/{servicePublicName}/remaining/path/
			
			- domainPublicName: the hashed (adler32) name of the domain ID
			- servicePublicName: the public name of the service
		*/
		
		String[] tokens = StringUtils.split(pathInfo, "/", 3);
		if (tokens.length < 1) throw new MalformedURLException("No domain provided");
		if (tokens.length < 2) throw new MalformedURLException("No service provided");
		return new String[]{tokens[0], tokens[1], (tokens.length == 3) ? tokens[2] : ""};
	}
}
