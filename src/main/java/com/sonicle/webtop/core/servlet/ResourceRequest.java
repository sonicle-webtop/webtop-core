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

import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.ServiceManager;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ResourceRequest extends HttpServlet {
	
	private static final Logger logger = WebTopApp.getLogger(ResourceRequest.class);
	protected static final int DEFLATE_THRESHOLD = 4*1024;
	protected static final int BUFFER_SIZE = 4*1024;
	private static final Pattern PATTERN_LAF_PATH = Pattern.compile("^laf\\/([\\w\\-\\.]+)\\/(.*)$");
	private static final Pattern PATTERN_LOCALE_FILE = Pattern.compile("^(Locale_(\\w*)).js$");
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		lookup(req).respondGet(resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		lookup(req).respondGet(resp);
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			lookup(req).respondHead(resp);
		} catch(UnsupportedOperationException ex) {
			super.doHead(req, resp);
		}
	}
	
	@Override
	protected long getLastModified(HttpServletRequest req) {
		return lookup(req).getLastModified();
	}
	
	protected LookupResult lookup(HttpServletRequest req) {
		LookupResult r = (LookupResult) req.getAttribute("lookupResult");
		if (r == null) {
			r = lookupNoCache(req);
			req.setAttribute("lookupResult", r);
		}
		return r;
	}
	
	private String[] splitPath(String pathInfo) throws MalformedURLException {
		String[] tokens = StringUtils.split(pathInfo, "/", 2);
		if(tokens.length != 2) throw new MalformedURLException("Path does not esplicitate service ID");
		return tokens;
	}
	
	protected LookupResult lookupNoCache(HttpServletRequest req) {
		String subject = null, subjectPath = null, jsPath = null, path = null, translPath = null;
		boolean isService = false;
		URL translUrl = null;
		
		// Builds a convenient URL for the servlet relative URL
		try {
			String reqPath = req.getPathInfo();
			logger.trace("Requested path [{}]", reqPath);
			String[] paths = splitPath(reqPath);
			subject = paths[0];
			jsPath = WebTopApp.get(req).getServiceManager().getServiceJsPath(subject);
			subjectPath = (jsPath == null) ? paths[0] : jsPath;
			isService = (jsPath != null);
			//subjectPath = StringUtils.replace(paths[0], ".", "/");
			path = paths[1];
			logger.trace("{}, {}", subject, path);
			translUrl = new URL("http://fake/"+subjectPath+"/"+path);
			translPath = translUrl.getPath();
			logger.trace("Translated path [{}]", translPath);
			if (isForbidden(translPath)) return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			
		} catch(MalformedURLException ex) {
			return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
		}
		
		if(subject.equals(CoreManifest.ID) && path.equals("images/login.png")) {
			return lookupLoginImage(req, translUrl);
			
		} else if(subject.equals(CoreManifest.ID) && path.equals("license.html")) {
			return lookupLicense(req, translUrl);
			
		} else {
			if(StringUtils.endsWith(translPath, ".js")) {
				String baseName = FilenameUtils.getBaseName(translPath);
				if(StringUtils.startsWith(baseName, "Locale")) {
					if(!isService) return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
					return lookupLocaleJs(req, subject, subjectPath, path, translUrl);
				
				} else {
					boolean debug = System.getProperties().containsKey("com.sonicle.webtop.wtdebug");
					debug = false;
					return lookupJs(req, translUrl, debug);
				}
			} else if(StringUtils.startsWith(path, "laf")) {
				if(!isService) return new Error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
				return lookupLAF(req, subject, subjectPath, path, translUrl);
			
			} else {
				return lookupDefault(req, translUrl);
			}
		}
	}
	
	private URL getResURL(String name) {
		logger.trace("Try getting resource [{}]", name);
		return this.getClass().getResource(name);
	}
	
	private LookupResult lookupLoginImage(HttpServletRequest request, URL url) {
		logger.trace("Looking-up login image");
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
			
			LookupFile lf = getFile(fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), lf, acceptsDeflate(request));
			
		} catch (MalformedURLException | ForbiddenException ex) {
			ex.printStackTrace();
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			ex.printStackTrace();
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLicense(HttpServletRequest request, URL reqUrl) {
		logger.trace("Looking-up license");
		String path = reqUrl.getPath();
		URL fileUrl = null;
		
		try {
			File file = new File(getServletContext().getRealPath("/images/license.html"));
			if (file.exists()) {
				fileUrl = file.toURI().toURL();
			} else {
				fileUrl = this.getClass().getResource(path);
			}
			
			LookupFile lf = getFile(fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), lf, acceptsDeflate(request));
			
		} catch (MalformedURLException | ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLAF(HttpServletRequest request, String serviceId, String subjectPath, String path, URL url) {
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
			
			LookupFile lf = getFile(fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(lastPath), lf, acceptsDeflate(request));
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NO_CONTENT, "Not Content");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupLocaleJs(HttpServletRequest request, String serviceId, String subjectPath, String path, URL url) {
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
			
			logger.trace("Class: {} - Override: {}", clazz, override);
			LookupFile lf = getFile(fileUrl);
			return new LocaleJsFile(clazz, override, fileUrl.toString(), lf, acceptsDeflate(request));
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupJs(HttpServletRequest request, URL url, boolean debug) {
		logger.trace("Looking-up js file");
		String path = url.getPath();
		URL fileUrl = null;
		
		try {
			String dpath = null;
			if(!debug) {
				dpath = path.substring(0, path.length() - 3) + "-compressed.js";
				fileUrl = this.getClass().getResource(dpath);
			}
			if (fileUrl != null) {
				path = dpath;
			} else {
				fileUrl = this.getClass().getResource(path);
			}
			
			LookupFile lf = getFile(fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), lf, acceptsDeflate(request));
		
		} catch (ForbiddenException ex) {
			ex.printStackTrace();
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			ex.printStackTrace();
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupResult lookupDefault(HttpServletRequest request, URL url) {
		logger.trace("Looking-up file as default");
		String path = url.getPath();
		URL fileUrl = null;
		
		try {
			fileUrl = this.getClass().getResource(path);
			LookupFile lf = getFile(fileUrl);
			return new StaticFile(fileUrl.toString(), getMimeType(path), lf, acceptsDeflate(request));
			
		} catch (ForbiddenException ex) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		} catch(NotFoundException ex) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "Not Found");
		} catch(InternalServerException ex) {
			return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}
	
	private LookupFile getFile(URL url) throws ResourceRequest.ForbiddenException, ResourceRequest.NotFoundException, ResourceRequest.InternalServerException {
		if(url == null) throw new ResourceRequest.NotFoundException();
		
		String protocol = url.getProtocol();
		logger.trace("protocol: {}", protocol);
		if(protocol.equals("file")) {
			try {
				File file = new File(url.toURI());
				if (!file.isFile()) throw new ResourceRequest.ForbiddenException();
				return new LookupFile(file.lastModified(), file.length(), new FileInputStream(file));
				
			} catch(URISyntaxException ex) {
				throw new ResourceRequest.InternalServerException();
			} catch(FileNotFoundException ex) {
				throw new ResourceRequest.NotFoundException();
			}
		} else if(protocol.equals("jar")) {
			
			try {
				String surl = url.toString();
				int ix = surl.lastIndexOf("!/");
				if (ix < 0) throw new ResourceRequest.InternalServerException();

				String jarFileName = URLDecoder.decode(surl.substring(4 + 5, ix), "UTF-8");
				String jarEntryName = surl.substring(ix + 2);
				logger.trace("jarFileName: {} - jarEntryName: {}", jarFileName, jarEntryName);
				
				File file = new File(jarFileName);
				JarFile jarFile = new JarFile(file);
				final ZipEntry ze = jarFile.getEntry(jarEntryName);
				
				if (ze != null) {
					if (ze.isDirectory()) throw new ResourceRequest.ForbiddenException();
					return new LookupFile(ze.getTime(), ze.getSize(), jarFile.getInputStream(ze));
				} else {
					return new LookupFile(-1, -1, url.openStream());
				}
				
			} catch(UnsupportedEncodingException ex) {
				throw new ResourceRequest.InternalServerException();
			} catch(IOException ex) {
				throw new ResourceRequest.NotFoundException();
			}
		} else {
			throw new ResourceRequest.InternalServerException();
		}
	}
	
	public class LookupFile {
		public long lastModified;
		public long contentLength;
		public InputStream inputStream;
		
		public LookupFile(long lastModified, long contentLength, InputStream is) {
			this.lastModified = lastModified;
			this.contentLength = contentLength;
			this.inputStream = is;
		}
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
		return coalesce(getServletContext().getMimeType(path), "application/octet-stream");
	}

	protected static boolean acceptsDeflate(HttpServletRequest req) {
		final String ae = req.getHeader("Accept-Encoding");
		return ae != null && ae.contains("gzip");
	}

	protected static boolean deflatable(String mimetype) {
		return mimetype.startsWith("text/")
			|| mimetype.equals("application/postscript")
			|| mimetype.startsWith("application/ms")
			|| mimetype.startsWith("application/vnd")
			|| mimetype.endsWith("xml");
	}

	protected static <T> T coalesce(T... ts) {
		for (T t : ts) {
			if (t != null) return t;
		}
		return null;
	}

	public static interface LookupResult {
		public void respondGet(HttpServletResponse resp) throws IOException;
		public void respondHead(HttpServletResponse resp);
		public long getLastModified();
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
		public void respondGet(HttpServletResponse resp) throws IOException {
			resp.sendError(statusCode, message);
		}
		
		@Override
		public void respondHead(HttpServletResponse resp) {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class LocaleJsFile extends StaticFile {
		protected String clazz;
		protected String override;
		
		public LocaleJsFile(String clazz, String override, String url, LookupFile lf, boolean acceptsDeflate) {
			super(url, "application/javascript", lf.lastModified, -1, lf.inputStream, acceptsDeflate);
			this.clazz = clazz;
			this.override = override;
		}
		
		@Override
		public InputStream getInputStream() {
			
			// Converts properties file into an hashmap
			HashMap<String, String> hm = new HashMap<>();
			try {
				Properties properties = new Properties();
				properties.load(inputStream);
				for (final String name: properties.stringPropertyNames()) {
					hm.put(name, properties.getProperty(name));
				}
			} catch(IOException ex) {
				
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
			
			// Builds js class structure
			String json = buildLocaleJson(clazz, override, hm);
			contentLength = json.getBytes().length;
			inputStream = null;
			return IOUtils.toInputStream(json);
		}
		
		private String buildLocaleJson(String clazz, String override, HashMap<String, String> props) {
			String strings = JsonResult.gsonWoNulls.toJson(props);
			//String strings = JsonResult.gsonWoNullsNoEscape.toJson(props);
			return "Ext.define('"
				+ clazz
				+ "',{"
				//+ "override:'"
				//+ override
				//+ "',"
				+ "strings:"
				+ strings
				+ "});";
		}
	}
	
	public static class StaticFile implements LookupResult {
		protected final String url;
		protected long lastModified;
		protected final String mimeType;
		protected int contentLength;
		protected final boolean acceptsDeflate;
		protected InputStream inputStream;
		
		public StaticFile(String url, String mimeType, LookupFile lf, boolean acceptsDeflate) {
			this(url, mimeType, lf.lastModified, (int)lf.contentLength, lf.inputStream, acceptsDeflate);
		}
		
		public StaticFile(String url, String mimeType, long lastModified, int contentLength, InputStream is, boolean acceptsDeflate) {
			this.url = url;
			this.lastModified = lastModified;
			this.mimeType = mimeType;
			this.contentLength = contentLength;
			this.acceptsDeflate = acceptsDeflate;
			this.inputStream = is;
		}
		
		protected InputStream getInputStream() {
			return inputStream;
		}

		@Override
		public void respondGet(HttpServletResponse resp) throws IOException {
			setHeaders(resp);
			final OutputStream os;
			
			if(willDeflate()) {
				resp.setHeader("Content-Encoding", "gzip");
				os = new GZIPOutputStream(resp.getOutputStream(), BUFFER_SIZE);
			} else {
				os = resp.getOutputStream();
			}
			//TODO: why this is not working
			//IOUtils.copy(is, os);
			transferStreams(getInputStream(), os);
		}

		@Override
		public void respondHead(HttpServletResponse resp) {
			
		}

		@Override
		public long getLastModified() {
			return lastModified;
		}
		
		protected boolean willDeflate() {
			return acceptsDeflate && deflatable(mimeType) && contentLength >= DEFLATE_THRESHOLD;
		}
		
		protected void setHeaders(HttpServletResponse resp) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType(mimeType);
			if (contentLength >= 0 && !willDeflate()) {
				resp.setContentLength(contentLength);
			}
		}
		
	}
	
	// TODO: replaced with IOUtils.copy
	protected static void transferStreams(InputStream is, OutputStream os) throws IOException {
		try {
			byte[] buf = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1) {
				os.write(buf, 0, bytesRead);
			}
		} finally {
			is.close();
			os.close();
		}
	}
}
