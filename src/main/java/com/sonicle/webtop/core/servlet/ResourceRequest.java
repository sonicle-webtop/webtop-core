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
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.ServiceManager;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.sdk.Resource;
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
import java.nio.charset.Charset;
import java.text.MessageFormat;
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
		String pathInfo = request.getPathInfo();
		if(lastModifiedCache.containsKey(pathInfo)) {
			return lastModifiedCache.get(pathInfo);
		} else {
			return lookup(request).getLastModified();
		}
		//return lookup(req).getLastModified();
	}
	
	protected LookupResult lookup(HttpServletRequest req) {
		LookupResult r = (LookupResult) req.getAttribute("lookupResult");
		if (r == null) {
			String pathInfo = req.getPathInfo();
			r = lookupNoCache(req, pathInfo);
			lastModifiedCache.put(pathInfo, r.getLastModified());
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
		String subject = null, subjectPath = null, jsPath = null, path = null, translPath = null;
		boolean isVirtualUrl = false, jsPathFound = false;
		URL translUrl = null;
		
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
				jsPath = WebTopApp.get(req).getServiceManager().getServiceJsPath(subject);
				jsPathFound = (jsPath != null);
				subjectPath = (jsPathFound) ? jsPath : subject;
				path = matcher.group(3);
			} else {
				isVirtualUrl = false;
				String[] urlParts = splitPath(reqPath);
				subject = urlParts[0];
				jsPath = WebTopApp.get(req).getServiceManager().getServiceJsPath(subject);
				jsPathFound = (jsPath != null);
				subjectPath = (jsPathFound) ? jsPath : urlParts[0];
				path = urlParts[1];
			}
			
			//String[] urlParts = splitPath(reqPath);
			//subject = urlParts[0];
			//System.out.println("subject: "+subject);
			//jsPath = WebTopApp.get(req).getServiceManager().getServiceJsPath(subject);
			//System.out.println("jsPath: "+jsPath);
			//subjectPath = (jsPath == null) ? urlParts[0] : jsPath;
			//System.out.println("subjectPath: "+subjectPath);
			//isService = (jsPath != null);
			////subjectPath = StringUtils.replace(paths[0], ".", "/");
			//path = urlParts[1];
			//System.out.println("path: "+path);
			////logger.trace("{}, {}", subject, path);
			translUrl = new URL("http://fake/"+subjectPath+"/"+path);
			translPath = translUrl.getPath();
			//System.out.println("translPath: "+translPath);
			
			//logger.trace("Translated path [{}]", translPath);
			if (isForbidden(translPath)) return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			
		} catch(MalformedURLException ex) {
			return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
		}
		
		if(subject.equals(CoreManifest.ID) && path.equals("images/login.png")) {
			return lookupLoginImage(req, isVirtualUrl, translUrl);
			
		} else if(subject.equals(CoreManifest.ID) && path.equals("license.html")) {
			return lookupLicense(req, isVirtualUrl, translUrl);
			
		} else {
			if(StringUtils.endsWith(translPath, ".js")) {
				String baseName = FilenameUtils.getBaseName(translPath);
				if(StringUtils.startsWith(baseName, "Locale")) {
					if(!jsPathFound) return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
					return lookupLocaleJs(req, isVirtualUrl, subject, subjectPath, path, translUrl);
				
				} else {
					//TODO: usare i file compressi se il debug è disattivato
					return lookupJs(req, isVirtualUrl, translUrl, false);
					//return lookupJs(req, isVirtualUrl, translUrl, !WebTopApp.systemIsDebug());
				}
			} else if(StringUtils.startsWith(path, "laf")) {
				if(!jsPathFound) return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
				return lookupLAF(req, isVirtualUrl, subject, subjectPath, path, translUrl);
			
			} else {
				return lookupDefault(req, isVirtualUrl, translUrl);
			}
		}
	}
	
	private URL getResURL(String name) {
		//logger.trace("Try getting resource [{}]", name);
		return this.getClass().getResource(name);
	}
	
	private LookupResult lookupLoginImage(HttpServletRequest request, boolean forceCaching, URL url) {
		//logger.trace("Looking-up login image");
		String path = url.getPath();
		URL fileUrl = null;
		
		try { // TODO: rewiew this!
			String host = request.getRemoteHost();
			URL requrl = new URL(request.getRequestURL().toString());
			host = requrl.getHost();
			int ix1 = host.indexOf('.');
			int ix2 = host.lastIndexOf('.');
			String hdomain = "";
			if (ix1 == ix2) {
				hdomain = host;
			} else {
				hdomain = host.substring(ix1 + 1);
			}
			
			File file = null;
			File dfile = new File(getServletContext().getRealPath("/images/" + hdomain + ".png"));
			if (dfile.exists()) {
				file = dfile;
			} else {
				file = new File(getServletContext().getRealPath("/images/login.png"));
			}
			if (file.exists()) {
				fileUrl = file.toURI().toURL();
			} else {
				fileUrl = this.getClass().getResource(path);
			}
			
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), forceCaching, resFile);
			
		} catch (MalformedURLException | ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLicense(HttpServletRequest request, boolean forceCaching, URL reqUrl) {
		//logger.trace("Looking-up license");
		String path = reqUrl.getPath();
		URL fileUrl = null;
		
		try {
			File file = new File(getServletContext().getRealPath("/images/license.html"));
			if (file.exists()) {
				fileUrl = file.toURI().toURL();
			} else {
				fileUrl = this.getClass().getResource(path);
			}
			
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), forceCaching, resFile);
			
		} catch (MalformedURLException | ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLAF(HttpServletRequest request, boolean forceCaching, String serviceId, String subjectPath, String path, URL url) {
		final String LOOKUP_URL = "/{0}/laf/{1}/{2}";
		URL fileUrl = null;
		
		try {
			Matcher lafm = PATTERN_LAF_PATH.matcher(path);
			if(!lafm.matches()) return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
			String laf = lafm.group(1);
			String lastPath = lafm.group(2);
			
			// First, try to get the resource in folder related to the specified
			// look&feel, then if not found looks into the default laf.
			fileUrl = getResURL(MessageFormat.format(LOOKUP_URL, subjectPath, laf, lastPath));
			if(fileUrl == null) {
				fileUrl = getResURL(MessageFormat.format(LOOKUP_URL, subjectPath, "default", lastPath));
			}
			
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(lastPath), forceCaching, resFile);
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NO_CONTENT, "Not Content");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLocaleJs(HttpServletRequest request, boolean forceCaching, String serviceId, String subjectPath, String path, URL url) {
		final String LOOKUP_URL = "/{0}/locale_{1}.properties";
		//String path = url.getPath();
		URL fileUrl = null;
		
		try {
			Matcher locm = PATTERN_LOCALE_FILE.matcher(path);
			if(!locm.matches()) return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
			String baseName = locm.group(1);
			String locale = locm.group(2);
			
			// First try to get the properties file that match the requested
			// locale, if not found it searches for the en locale
			fileUrl = getResURL(MessageFormat.format(LOOKUP_URL, subjectPath, locale));
			if(fileUrl == null) {
				fileUrl = getResURL(MessageFormat.format(LOOKUP_URL, subjectPath, "en_EN"));
			}
			
			// Defines specific params
			ServiceManager svcm = WebTopApp.get(request).getServiceManager();
			ServiceManifest manifest = svcm.getManifest(serviceId);
			String clazz = manifest.getJsPackageName() + "." + baseName;
			String override = manifest.getServiceJsClassName(true);
			
			//logger.trace("Class: {} - Override: {}", clazz, override);
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new LocaleJsFile(clazz, override, fileUrl.toString(), forceCaching, resFile);
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupJs(HttpServletRequest request, boolean forceCaching, URL url, boolean minified) {
		//logger.trace("Looking-up js file");
		String path = url.getPath();
		URL fileUrl = null;
		
		try {
			String dpath = null;
			if(minified) {
				dpath = path.substring(0, path.length() - 3) + "-compressed.js";
				fileUrl = this.getClass().getResource(dpath);
			}
			if (fileUrl != null) {
				path = dpath;
			} else {
				fileUrl = this.getClass().getResource(path);
			}
			
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), forceCaching, resFile);
		
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupDefault(HttpServletRequest request, boolean forceCaching, URL url) {
		//logger.trace("Looking-up file as default");
		String path = url.getPath();
		URL fileUrl = null;
		
		try {
			fileUrl = this.getClass().getResource(path);
			Resource resFile = getFile(WebTopApp.get(request), fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), forceCaching, resFile);
			
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
		public void respondGet(HttpServletRequest request, HttpServletResponse response) throws IOException;
		public void respondHead(HttpServletRequest request, HttpServletResponse response);
		public long getLastModified();
	}
	
	public static class StaticFile implements LookupResult {
		protected final String url;
		protected final String mimeType;
		protected final boolean forceCaching;
		protected final String charset;
		protected final Resource resourceFile;
		
		public StaticFile(String url, String mimeType, boolean forceCaching, Resource resourceFile) {
			this(url, mimeType, forceCaching, null, resourceFile);
		}
		
		public StaticFile(String url, String mimeType, boolean forceCaching, String charset, Resource resourceFile) {
			this.url = url;
			this.mimeType = mimeType;
			this.forceCaching = forceCaching;
			this.charset = charset;
			this.resourceFile = resourceFile;
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

		@Override
		public void respondGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
			OutputStream os = null;
			InputStream is = null;
			
			try {
				prepareContent();
				os = ServletUtils.prepareForStreamCopy(request, response, mimeType, getContentLength(), DEFLATE_THRESHOLD);
				ServletUtils.setContentTypeHeader(response, mimeType);
				if(forceCaching) {
					ServletUtils.setCacheControlHeaderPrivateMaxAge(response, 60*60*24*365); // infinite
				} else {
					if(StringUtils.startsWith(mimeType, "image") || StringUtils.startsWith(mimeType, "text/css")) {
						ServletUtils.setCacheControlHeaderPrivateMaxAge(response, 60*60*24); // 1 day
					} else {
						ServletUtils.setCacheControlHeaderPrivateNoCache(response);
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

		@Override
		public long getLastModified() {
			return resourceFile.getLastModified();
		}
	}
	
	public static class LocaleJsFile extends StaticFile {
		protected String clazz;
		protected String override;
		protected String json = null;
		protected int contentLength = -1;
		
		public LocaleJsFile(String clazz, String override, String url, boolean forceCaching, Resource resourceFile) {
			super(url, "application/javascript", forceCaching, "utf-8", resourceFile);
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
			return IOUtils.toInputStream(json, Charset.forName("utf-8"));
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
				PropertiesEx properties = new PropertiesEx();
				properties.load(is, true); // Important! True to preserve unicode escapes found in properties
				String json;
				for(final String name: properties.stringPropertyNames()) {
					json = "\"" + name + "\"" + ":" + "\"" + properties.getProperty(name) + "\"";
					strings.add(json);
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
