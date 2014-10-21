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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ResourceRequestOLD extends HttpServlet {
	
	protected static final int DEFLATE_THRESHOLD = 4*1024;
	protected static final int BUFFER_SIZE = 4*1024;
	
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

	protected LookupResult lookupNoCache(HttpServletRequest req) {
		
		String path = getPath(req);
                System.out.println("path="+path);
		if (isForbidden(path)) {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		}

		URL clurl = null;

		if (path.endsWith(".js") && !path.endsWith("-compressed.js")) {
			Properties props = System.getProperties();
			boolean wtdebug = false;
			if (props.containsKey("com.sonicle.webtop.wtdebug")) {
				wtdebug = true;
			}
			if (!wtdebug) {
				String dpath = path.substring(0, path.length() - 3) + "-compressed.js";
				clurl = this.getClass().getResource(dpath);
				if (clurl != null) {
					path = dpath;
				}
			}
		} else {
			if (path.equals("/com/sonicle/webtop/core/images/login.png")) {
				try {
					String host = req.getRemoteHost();
					URL requrl = new URL(req.getRequestURL().toString());
					System.out.println("requrl=" + requrl);
					host = requrl.getHost();
					System.out.println("host=" + host);
					int ix1 = host.indexOf('.');
					int ix2 = host.lastIndexOf('.');
					String hdomain = "";
					if (ix1 == ix2) {
						hdomain = host;
					} else {
						hdomain = host.substring(ix1 + 1);
					}
					System.out.println("hdomain=" + hdomain);
					File file = null;
					File dfile = new File(getServletContext().getRealPath("/images/" + hdomain + ".png"));
					if (dfile.exists()) {
						file = dfile;
					} else {
						file = new File(getServletContext().getRealPath("/images/login.png"));
					}
					if (file.exists()) {
						clurl = file.toURI().toURL();
					}
				} catch (MalformedURLException exc) {
					exc.printStackTrace();
				}
			} else if (path.equals("/com/sonicle/webtop/core/license.html")) {
				try {
					File file = new File(getServletContext().getRealPath("/images/license.html"));
					if (file.exists()) {
						clurl = file.toURI().toURL();
					}
				} catch (MalformedURLException exc) {
					exc.printStackTrace();
				}
			}
		}

		if (clurl == null) {
			clurl = this.getClass().getResource(path);
		}

		if (clurl == null) {

			//first try to switch gif to png
			if (path.endsWith(".gif")) {
				String oldpath = path;
				path = path.substring(0, path.length() - 4) + ".png";
				clurl = this.getClass().getResource(path);
				if (clurl == null) {
					path = oldpath;
				}
			}

			if (clurl == null && path.startsWith("/webtop/themes/")) { //try default
				String relpath = path.substring(15);
				int ix = relpath.indexOf("/");
				String reltheme = relpath.substring(ix + 1);
				path = "/webtop/themes/win/" + reltheme;
				clurl = this.getClass().getResource(path);
			}

			if (clurl == null) {
				return new Error(HttpServletResponse.SC_NOT_FOUND, "Not found");
			}
		}

		final String mimeType = getMimeType(path);
		String prot = clurl.getProtocol();
		String surl = clurl.toString();
		if (prot.equals("file")) {
			try {
				String realpath = clurl.getPath();
				URI realuri = clurl.toURI();
				// Try as an ordinary file
				File f = new File(realuri);
				if (!f.isFile()) {
					return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
				} else {
					try {
						return new StaticFile(surl, f.lastModified(), mimeType, (int) f.length(), acceptsDeflate(req), new FileInputStream(f));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
					}
				}
			} catch (URISyntaxException uriexc) {
				uriexc.printStackTrace();
				return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
			}
		} else if (prot.equals("jar")) {
			int ix = surl.lastIndexOf("!/");
			if (ix < 0) {
				return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
			}
			try {
				String jarfilename = java.net.URLDecoder.decode(surl.substring(4 + 5, ix), "UTF-8");
				String jarentryname = surl.substring(ix + 2);
				//System.out.println("Opening "+jarfilename+" entry "+jarentryname);
				File file = new File(jarfilename);
				JarFile jarfile = new JarFile(file);
				final ZipEntry ze = jarfile.getEntry(jarentryname);
				if (ze != null) {
					if (ze.isDirectory()) {
						return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
					} else {
						return new StaticFile(surl, ze.getTime(), mimeType, (int) ze.getSize(), acceptsDeflate(req), jarfile.getInputStream(ze));
					}
				} else {
					return new StaticFile(surl, -1, mimeType, -1, acceptsDeflate(req), clurl.openStream());
				}
			} catch (ClassCastException e) {
				// Unknown resource type
				try {
					return new StaticFile(surl, -1, mimeType, -1, acceptsDeflate(req), clurl.openStream());
				} catch (IOException exc) {
					return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
			} catch (IOException e) {
				e.printStackTrace();
				return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
			}
		} else {
			return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		}

	}
	
	protected String getPath(HttpServletRequest req) {
		//String servletPath = req.getServletPath();
		String pathInfo = coalesce(req.getPathInfo(), "");
		//System.out.println("pathinfo: "+pathInfo);
		//return servletPath + pathInfo;
		return pathInfo;
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
	
	public static class StaticFile implements LookupResult {
		protected final String url;
		protected final long lastModified;
		protected final String mimeType;
		protected final int contentLength;
		protected final boolean acceptsDeflate;
		protected final InputStream is;
		
		public StaticFile(String url, long lastModified, String mimeType, int contentLength, boolean acceptsDeflate, InputStream is) {
			this.url = url;
			this.lastModified = lastModified;
			this.mimeType = mimeType;
			this.contentLength = contentLength;
			this.acceptsDeflate = acceptsDeflate;
			this.is = is;
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
			transferStreams(is, os);
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
