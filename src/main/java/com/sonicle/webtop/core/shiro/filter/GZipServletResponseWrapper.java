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
package com.sonicle.webtop.core.shiro.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author malbinola
 */
public class GZipServletResponseWrapper extends HttpServletResponseWrapper {
	private GZipServletOutputStream gzipOutputStream = null;
	private PrintWriter printWriter;
	
	public GZipServletResponseWrapper(HttpServletResponse response) {
		super(response);
		response.addHeader("Content-Encoding", "gzip");
	}
	
	public void finish() throws IOException {
		if (printWriter != null) printWriter.close();
		if (gzipOutputStream != null) gzipOutputStream.close();
	}
	
	@Override
	public void flushBuffer() throws IOException {
		if (printWriter != null) printWriter.flush();
		
		IOException exception1 = null;
		try {
			if (this.gzipOutputStream != null) this.gzipOutputStream.flush();
		} catch(IOException ex) {
			exception1 = ex;
		}
		
		IOException exception2 = null;
		try {
			super.flushBuffer();
		} catch(IOException ex) {
			exception2 = ex;
		}
		
		if (exception1 != null) throw exception1;
		if (exception2 != null) throw exception2;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (this.printWriter != null) throw new IllegalStateException("PrintWriter obtained already. Cannot get OutputStream");
		if (this.gzipOutputStream == null) this.gzipOutputStream = new GZipServletOutputStream(getResponse().getOutputStream());
		return this.gzipOutputStream;
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		if (this.printWriter != null) throw new IllegalStateException("PrintWriter obtained already. Cannot get OutputStream");
		if (this.printWriter == null) {
			this.gzipOutputStream = new GZipServletOutputStream(getResponse().getOutputStream());
			this.printWriter = new PrintWriter(new OutputStreamWriter(this.gzipOutputStream, getResponse().getCharacterEncoding()));
		}
		return this.printWriter;
	}
	
	@Override
	public void setContentLength(int len) {
		// Ignore this, content length of zipped response does not match the original content length
	}
}
