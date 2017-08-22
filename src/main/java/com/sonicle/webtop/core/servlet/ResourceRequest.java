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
import com.sonicle.commons.PropertiesEx;
import com.sonicle.commons.RegexUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.io.Resource;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ResourceRequest extends HttpServlet {
	public static final String URL = "resources"; // This must reflect web.xml!
	private static final Logger logger = WT.getLogger(ResourceRequest.class);
	protected static final int DEFLATE_THRESHOLD = 4*1024;
	protected static final int BUFFER_SIZE = 4*1024;
	private static final Pattern PATTERN_VIRTUAL_URL = Pattern.compile("^"
			+ RegexUtils.MATCH_URL_SEPARATOR
			+ RegexUtils.capture(RegexUtils.MATCH_JAVA_PACKAGE)
			+ RegexUtils.MATCH_URL_SEPARATOR
			+ RegexUtils.capture(RegexUtils.MATCH_SW_VERSION)
			+ RegexUtils.MATCH_URL_SEPARATOR
			+ RegexUtils.capture(RegexUtils.MATCH_ANY)
			+ "$");
	private static final Pattern PATTERN_LAF_PATH = Pattern.compile("^laf\\/([\\w\\-\\.]+)\\/(.*)$");
	private static final Pattern PATTERN_LOCALE_FILE = Pattern.compile("^(Locale_(\\w*)).js$");
	private static final ConcurrentHashMap<String, Long> lastModifiedCache = new ConcurrentHashMap<>();
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		lookup(request).respondGet(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		lookup(request).respondGet(request, response);
	}
	
	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			lookup(request).respondHead(request, response);
		} catch(UnsupportedOperationException ex) {
			super.doHead(request, response);
		}
	}
	
	@Override
	protected long getLastModified(HttpServletRequest request) {
		return lookup(request).getLastModified();
		/*
		String pathInfo = request.getPathInfo();
		if(lastModifiedCache.containsKey(pathInfo)) {
			return lastModifiedCache.get(pathInfo);
		} else {
			return lookup(request).getLastModified();
		}
		*/
	}
	
	protected LookupResult lookup(HttpServletRequest req) {
		LookupResult r = (LookupResult) req.getAttribute("lookupResult");
		if (r == null) {
			String pathInfo = req.getPathInfo();
			r = lookupNoCache(req, pathInfo);
			//if (r.cacheLastModified()) lastModifiedCache.put(pathInfo, r.getLastModified());
			req.setAttribute("lookupResult", r);
		}
		return r;
	}
	
	private String[] splitPath(String pathInfo) throws MalformedURLException {
		String[] tokens = StringUtils.split(pathInfo, "/", 2);
		if(tokens.length != 2) throw new MalformedURLException("URL does not esplicitate service ID");
		return tokens;
	}
	
	protected LookupResult lookupNoCache(HttpServletRequest req, String reqPath) {
		String subject = null, subjectPath = null, jsPath = null, path = null, targetPath = null;
		boolean isVirtualUrl = false, jsPathFound = false;
		URL targetUrl = null;
		
		// Builds a convenient URL for the servlet relative URL
		try {
			//String reqPath = req.getPathInfo();
			//logger.trace("Requested path [{}]", reqPath);
			
			Matcher matcher = PATTERN_VIRTUAL_URL.matcher(reqPath);
			if(matcher.matches()) {
				// Matches URLs like: /{service.id}/{service.version}/{remaining.url.part}
				// Eg. /com.sonicle.webtop.core/5.1.1/laf/default/service.css
				//	{service.id} -> com.sonicle.webtop.core
				//	{service.version} -> 5.1.1
				//	{remaining.url.part} -> laf/default/service.css
				isVirtualUrl = true;
				subject = matcher.group(1);
				path = matcher.group(3);
				
				if (!WebTopApp.get(req).getServiceManager().hasService(subject)) {
					return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
				}
				targetUrl = new URL("http://fake/client/"+subject+"/"+path);
				
			} else {
				isVirtualUrl = false;
				String[] urlParts = splitPath(reqPath);
				subject = urlParts[0];
				jsPath = WebTopApp.get(req).getServiceManager().getServiceJsPath(subject);
				jsPathFound = (jsPath != null);
				subjectPath = (jsPathFound) ? jsPath : urlParts[0];
				path = urlParts[1];
				
				targetUrl = new URL("http://fake/"+subjectPath+"/"+path);
			}
			targetPath = targetUrl.getPath();
			
			//logger.trace("Translated path [{}]", translPath);
			if (isForbidden(targetPath)) return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			
		} catch(MalformedURLException ex) {
			return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
		}
		
		if (!isVirtualUrl && path.startsWith("images")) {
			// Addresses domain public images
			// URLs like "/{domainPublicName}/images/{relativePathToFile}"
			// Eg.	"/1bbc048f/images/login.png"
			//		"/1bbc048f/images/sub/login.png"
			WebTopApp wta = WebTopApp.get(req);
			WebTopManager wtMgr = wta.getWebTopManager();
			
			String domainId = wtMgr.publicNameToDomainId(subject);
			if (StringUtils.isBlank(domainId)) {
				// We must support old-style URL using {domainInternetName}
				// instead of {domainPublicName}
				// Eg.	"/sonicle.com/images/login.png"
				domainId = wtMgr.internetNameToDomain(subject);
			}
			if (StringUtils.isBlank(domainId)) {
				return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
			}
			
			return lookupDomainImage(req, targetUrl, domainId);
			
		} else if (isVirtualUrl && subject.equals(CoreManifest.ID) && path.equals("resources/images/login.png")) {
			// Addresses login image
			// URLs like "/{serviceId}/{serviceVersion}/resources/images/login.png"
			// Eg.	"/com.sonicle.webtop.core/5.0.0/images/login.png"
			return lookupLoginImage(req, targetUrl);
			
		} else if (isVirtualUrl && subject.equals(CoreManifest.ID) && path.equals("resources/license.html")) {
			// Addresses licence page
			// URLs like "/{serviceId}/{serviceVersion}/resources/license.html"
			return lookupLicense(req, targetUrl);
			
		} else {
			if (StringUtils.endsWith(targetPath, ".js")) {
				WebTopApp wta = WebTopApp.get(req);
				String sessionId = ServletHelper.getSessionID(req);
				if (StringUtils.startsWith(path, "resources/libs")) {
					// If targets lib folder, simply return requested file without handling debug versions
					return lookupJs(req, targetUrl, false);
					
				} else if (StringUtils.startsWith(path, "boot/")) {
					return lookupJs(req, targetUrl, isDebug(wta, sessionId));
					
				} else if (StringUtils.startsWith(FilenameUtils.getBaseName(path), "Locale")) {
					return lookupLocaleJs(req, targetUrl, subject);
					
				} else {
					return lookupJs(req, targetUrl, isDebug(wta, sessionId));
				}
				
			} else if (StringUtils.startsWith(path, "laf")) {
				return lookupLAF(req, targetUrl, path, subject, subjectPath);
				
			} else {
				return lookupDefault(req, isVirtualUrl ? ClientCaching.YES : ClientCaching.AUTO, targetUrl);
			}
		}
	}
	
	private boolean isDebug(WebTopApp wta, String sessionId) {
		if(StringUtils.isBlank(sessionId)) return false;
		WebTopSession wts = wta.getSessionManager().getWebTopSession(sessionId);
		if(wts == null) {
			return wta.getStartupProperties().getDebugMode();
		} else {
			return wts.getDebugMode();
		}
	}
	
	private URL getResURL(String name) {
		//logger.trace("Try getting resource [{}]", name);
		return this.getClass().getResource(name);
	}
	
	private LookupResult lookupDomainImage(HttpServletRequest request, URL targetUrl, String domainId) {
		WebTopApp wta = WebTopApp.get(request);
		URL fileUrl = null;
		
		try {
			String targetPath = targetUrl.getPath();
			String remainingPath = StringUtils.substringAfter(targetPath, "images/");
			if (StringUtils.isBlank(remainingPath)) throw new NotFoundException();
			String imagesPath = wta.getImagesPath(domainId);
			File file = new File(imagesPath + remainingPath);
			if (!file.exists()) throw new NotFoundException();
			fileUrl = file.toURI().toURL();
			
			Resource resFile = getFile(wta, fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(targetPath), ClientCaching.NO, resFile);
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(MalformedURLException | NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLoginImage(HttpServletRequest request, URL targetUrl) {
		WebTopApp wta = WebTopApp.get(request);
		URL fileUrl = null;
		
		try {
			String targetPath = targetUrl.getPath();
			//String internetName = ServletUtils.getInternetName(request);
			String internetName = ServletUtils.getHost(request);
			String domainId = WT.findDomainIdByInternetName(internetName);
			if (!StringUtils.isBlank(domainId)) {
				String pathname = wta.getImagesPath(domainId) + "login.png";
				File file = new File(pathname);
				if (file.exists()) {
					fileUrl = file.toURI().toURL();
				}
			}
			
			if (fileUrl == null) {
				fileUrl = getResURL(targetPath);
			}
			
			Resource resFile = getFile(wta, fileUrl);
			StaticFile sf = new StaticFile(fileUrl.toString(), getMimeType(targetPath), ClientCaching.NO, resFile);
			sf.cacheLastModified = false;
			return sf;
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(MalformedURLException | NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLicense(HttpServletRequest request, URL targetUrl) {
		WebTopApp wta = WebTopApp.get(request);
		URL fileUrl = null;
		
		try {
			String targetPath = targetUrl.getPath();
			WebTopManager wtMgr = wta.getWebTopManager();
			if (wtMgr != null) {
				String internetName = ServletUtils.getInternetName(request);
				String domainId = wtMgr.internetNameToDomain(internetName);
				if (!StringUtils.isBlank(domainId)) {
					String pathname = wta.getHomePath(domainId) + "license.html";
					File file = new File(pathname);
					if (file.exists()) {
						fileUrl = file.toURI().toURL();
					}
				}
			}
			
			if (fileUrl == null) {
				fileUrl = getResURL(targetPath);
			}
			
			Resource resFile = getFile(wta, fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(targetPath), ClientCaching.NO, resFile);
			
		} catch (MalformedURLException | ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupJs(HttpServletRequest request, URL targetUrl, boolean debugVersion) {
		WebTopApp wta = WebTopApp.get(request);
		URL fileUrl = null;
		
		try {
			String targetPath = targetUrl.getPath();
			if(debugVersion) {
				String dpath = targetPath.substring(0, targetPath.length() - 3) + "-debug.js";
				fileUrl = getResURL(dpath);
				if (fileUrl != null) targetPath = dpath;
			}
			if (fileUrl == null) {
				fileUrl = getResURL(targetPath);
			}
			
			Resource resFile = getFile(wta, fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(targetPath), ClientCaching.YES, resFile);
		
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLocaleJs(HttpServletRequest request, URL targetUrl, String serviceId) {
		WebTopApp wta = WebTopApp.get(request);
		String targetPath = targetUrl.getPath();
		URL fileUrl = null;
		
		try {
			String fileName = FilenameUtils.getName(targetPath);
			String baseTargetPath = StringUtils.substringBefore(targetPath, fileName);
			Matcher matcher = PATTERN_LOCALE_FILE.matcher(fileName);
			if (!matcher.matches()) throw new InternalServerException();
			String nameBase = matcher.group(1);
			String nameLoc = matcher.group(2);
			String[] tokens = StringUtils.split(nameLoc, "_", 2);
			
			// Try to get the properties file that match the requested locale...
			// If not found, look for the basic english locale (en)
			String[] suffixes = null;
			if (tokens.length == 2) {
				suffixes = new String[]{nameLoc, tokens[0], "en"};
			} else {
				suffixes = new String[]{nameLoc, "en"};
			}
			for (String suffix : suffixes) {
				fileUrl = getResURL(baseTargetPath + "locale_" + suffix + ".properties");
				if(fileUrl != null) break;
			}
			if (fileUrl == null) throw new NotFoundException();
			
			// Defines specific params
			ServiceManager svcm = wta.getServiceManager();
			ServiceManifest manifest = svcm.getManifest(serviceId);
			String clazz = manifest.getJsPackageName() + "." + nameBase;
			String override = manifest.getPrivateServiceJsClassName(true);
			
			//logger.trace("Class: {} - Override: {}", clazz, override);
			Resource resFile = getFile(wta, fileUrl);
			return new LocaleJsFile(clazz, override, fileUrl.toString(), ClientCaching.YES, resFile);
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLAF(HttpServletRequest request, URL targetUrl, String path, String serviceId, String subjectPath) {
		URL fileUrl = null;
		
		try {
			String baseTargetPath = StringUtils.substringBefore(targetUrl.getPath(), path);
			Matcher lafm = PATTERN_LAF_PATH.matcher(path);
			if(!lafm.matches()) return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
			String pathLaf = lafm.group(1);
			String remainingPath = lafm.group(2);
			
			// Try to get resource in folder related to the requested look&feel...
			// If not found, look for the default one (default)
			String[] lafs = new String[]{pathLaf, "default"};
			for (String laf : lafs) {
				fileUrl = getResURL(baseTargetPath + "laf/" + laf + "/" + remainingPath);
				if(fileUrl != null) break;
			}
			if (fileUrl == null) throw new NotFoundException();
			
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(remainingPath), ClientCaching.YES, resFile);
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NO_CONTENT, "Not Content");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupDefault(HttpServletRequest request, ClientCaching clientCaching, URL url) {
		//logger.trace("Looking-up file as default");
		String path = url.getPath();
		URL fileUrl = null;
		
		try {
			fileUrl = this.getClass().getResource(path);
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), clientCaching, resFile);
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private Resource getFile(WebTopApp wta, URL url) throws ResourceRequest.ForbiddenException, ResourceRequest.NotFoundException, ResourceRequest.InternalServerException {
		Resource resource = null;
		if(url == null) throw new ResourceRequest.NotFoundException();
		
		String protocol = url.getProtocol();
		if(protocol.equals("file")) {
			try {
				resource = wta.getFileResource(url);
			} catch(URISyntaxException | MalformedURLException | WTRuntimeException ex) {
				throw new ResourceRequest.InternalServerException();
			}
		} else if(protocol.equals("jar")) {
			try {
				resource = wta.getJarResource(url);
			} catch(URISyntaxException | MalformedURLException | WTRuntimeException ex) {
				throw new ResourceRequest.InternalServerException();
			} catch(IOException ex) {
				throw new ResourceRequest.NotFoundException();
			}
		} else {
			throw new ResourceRequest.InternalServerException();
		}
		
		if(resource == null) throw new ResourceRequest.NotFoundException();
		return resource;
	}
	
	public class NotFoundException extends Exception {
		public NotFoundException() {
			super();
		}
	}
	
	public class InternalServerException extends Exception {
		public InternalServerException() {
			super();
		}
	}
	
	public class ForbiddenException extends Exception {
		public ForbiddenException() {
			super();
		}
	}
	
	protected String getPath(HttpServletRequest request) {
		return StringUtils.defaultString(request.getPathInfo());
	}

	protected boolean isForbidden(String path) {
		String lpath = path.toLowerCase();
		return lpath.startsWith("/web-inf/") || lpath.startsWith("/meta-inf/");
	}

	protected String getMimeType(String path) {
		return LangUtils.coalesce(getServletContext().getMimeType(path), "application/octet-stream");
	}

	public static interface LookupResult {
		public boolean cacheLastModified();
		public long getLastModified();
		public void respondGet(HttpServletRequest request, HttpServletResponse response) throws IOException;
		public void respondHead(HttpServletRequest request, HttpServletResponse response);
	}
	
	public enum ClientCaching {
		YES, AUTO, NO
	}
	
	public static class StaticFile implements LookupResult {
		protected boolean cacheLastModified;
		protected final String url;
		protected final String mimeType;
		protected final ClientCaching clientCaching;
		protected final String charset;
		protected final Resource resourceFile;
		
		public StaticFile(String url, String mimeType, ClientCaching clientCaching, Resource resourceFile) {
			this(url, mimeType, clientCaching, null, resourceFile);
		}
		
		public StaticFile(String url, String mimeType, ClientCaching clientCaching, String charset, Resource resourceFile) {
			this.cacheLastModified = true;
			this.url = url;
			this.mimeType = mimeType;
			this.clientCaching = clientCaching;
			this.charset = charset;
			this.resourceFile = resourceFile;
		}
		
		@Override
		public boolean cacheLastModified() {
			return cacheLastModified;
		}
		
		@Override
		public long getLastModified() {
			return resourceFile.getLastModified();
		}

		@Override
		public void respondGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
			OutputStream os = null;
			InputStream is = null;
			
			try {
				prepareContent();
				os = ServletUtils.prepareForStreamCopy(request, response, mimeType, getContentLength(), DEFLATE_THRESHOLD);
				ServletUtils.setContentTypeHeader(response, mimeType);
				if (clientCaching.equals(ClientCaching.YES)) {
					ServletUtils.setCacheControlPrivateMaxAge(response, 60*60*24*365); // long (365 days)
				} else {
					if (clientCaching.equals(ClientCaching.NO)) {
						ServletUtils.setCacheControlPrivateNoCache(response);
					} else {
						if (StringUtils.startsWith(mimeType, "image") || StringUtils.startsWith(mimeType, "text/css")) {
							ServletUtils.setCacheControlPrivateMaxAge(response, 60*60*24); // 1 day
						} else {
							ServletUtils.setCacheControlPrivateNoCache(response);
						}
					}
				}
					
				is = getInputStream();
				ServletUtils.transferStreams(is, os);
				os.flush();
				response.setStatus(HttpServletResponse.SC_OK);
			} finally {
				IOUtils.closeQuietly(os);
				IOUtils.closeQuietly(is);
			}
		}

		@Override
		public void respondHead(HttpServletRequest request, HttpServletResponse response) {
			
		}
		
		protected void prepareContent() throws IOException {
			// Do nothing...
		}
		
		protected InputStream getInputStream() throws IOException {
			return resourceFile.getInputStream();
		}
		
		protected int getContentLength() {
			return (int)resourceFile.getSize();
		}
	}
	
	public static class LocaleJsFile extends StaticFile {
		protected String clazz;
		protected String override;
		protected String json = null;
		protected int contentLength = -1;
		
		public LocaleJsFile(String clazz, String override, String url, ClientCaching clientCaching, Resource resourceFile) {
			super(url, "application/javascript", clientCaching, "utf-8", resourceFile);
			this.clazz = clazz;
			this.override = override;
		}
		
		@Override
		protected void prepareContent() throws IOException {
			InputStream is = resourceFile.getInputStream();
			String strings = loadProperties(is);
			IOUtils.closeQuietly(is);
			is = null;
			
			json = buildLocaleJson(clazz, strings);
			contentLength = json.getBytes().length;
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			if(json == null) throw new WTRuntimeException("This method needs to be called after prepareContent()");
			return IOUtils.toInputStream(json, WT.getSystemCharset());
		}
		
		@Override
		protected int getContentLength() {
			if(contentLength == -1) throw new WTRuntimeException("This method needs to be called after prepareContent()");
			return contentLength;
		}
		
		private String buildLocaleJson(String clazz, String strings) {
			return "Ext.define('"
				+ clazz
				+ "',{"
				+ "strings:"
				+ strings
				+ "});";
		}
		
		private String loadProperties(InputStream is) {
			ArrayList<String> strings = new ArrayList();
			BufferedReader br = null;
			
			try {
				/*
				Properties properties = new Properties();
				properties.load(new InputStreamReader(is, "UTF-8"));
				for(final String name: properties.stringPropertyNames()) {
					final String s = "\"" + name + "\"" + ":" + "\"" + properties.getProperty(name) + "\"";
					strings.add(s);
				}
				*/
				
				PropertiesEx properties = new PropertiesEx();
				properties.load(is, true); // Important! True to preserve unicode escapes found in properties
				for(final String name: properties.stringPropertyNames()) {
					//TODO: Si può forse applicare? LangUtils.escapeJsonDoubleQuote -> StringEscapeUtils.escapeJson
					final String s = "\"" + name + "\"" + ":" + "\"" + LangUtils.escapeJsonDoubleQuote(properties.getProperty(name)) + "\"";
					strings.add(s);
				}
				
				// Alternative way to preserve escapes...
				/*
				br = new BufferedReader(new InputStreamReader(is, Charset.forName("ISO-8859-1")));
				String line = null, key, value, json;
				int firstEqual = -1;
				while((line = br.readLine()) != null) {
					firstEqual = line.indexOf("=");
					if(firstEqual > 0) {
						key = line.substring(0, firstEqual);
						value = line.substring(firstEqual+1);
						json = "\"" + key + "\"" + ":" + "\"" + value + "\"";
						strings.add(json);
					}
				}
				*/
				
			} catch(IOException ex) {
				throw new RuntimeException(ex);
			} finally {
				IOUtils.closeQuietly(br);
			}
				
			return "{" + StringUtils.join(strings, ",") + "}";
		}
	}
	
	public static class Error implements LookupResult {
		protected final int statusCode;
		protected final String message;

		public Error(int statusCode, String message) {
			this.statusCode = statusCode;
			this.message = message;
		}
		
		@Override
		public boolean cacheLastModified() {
			return false;
		}
		
		@Override
		public long getLastModified() {
			return -1;
		}
		
		@Override
		public void respondGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.sendError(statusCode, message);
		}
		
		@Override
		public void respondHead(HttpServletRequest request, HttpServletResponse response) {
			throw new UnsupportedOperationException();
		}
	}
}
