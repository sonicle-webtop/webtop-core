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
package com.sonicle.webtop.core.servlet.response;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.lucene.util.IOUtils;

/**
 *
 * @author malbinola
 */
public class GzippableResponseWrapper extends HttpServletResponseWrapper {
	protected ServletOutputStream gzipOutputStream;
	protected PrintWriter writer;
	protected int gzipMinTreshold = 860;
	
	public GzippableResponseWrapper(HttpServletResponse response) {
		super(response);
		// explicitly reset content length, as the size of zipped stream is unknown
		response.setContentLength(-1);
	}
	
	public void setGzipMinThreshold(int gzipMinTreshold) {
		this.gzipMinTreshold = gzipMinTreshold;
	}
	
	public void finishResponse() throws IOException {
		IOUtils.close(writer);
		IOUtils.close(gzipOutputStream);
	}
	
	protected ServletOutputStream createOutputStream() throws IOException {
		GZippableOutputStream stream = new GZippableOutputStream((HttpServletResponse)getResponse());
		stream.setGzipMinThreshold(gzipMinTreshold);
		return stream;
	}
	
	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) writer.flush();
		
		IOException exception1 = null;
		try {
			if (gzipOutputStream != null) gzipOutputStream.flush();
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
		if (writer != null) throw new IllegalStateException("PrintWriter obtained already. Cannot get OutputStream");
		if (gzipOutputStream == null) {
			gzipOutputStream = createOutputStream();
		}
		return gzipOutputStream;
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer != null) return writer;
		if (gzipOutputStream != null) throw new IllegalStateException("getOutputStream() has already been called for this response");
		gzipOutputStream = createOutputStream();
		String charEnc = getResponse().getCharacterEncoding();
		if (charEnc != null) {
			writer = new PrintWriter(new OutputStreamWriter(gzipOutputStream, charEnc));
		} else {
			writer = new PrintWriter(gzipOutputStream);
		}
		return writer;
	}
	
	@Override
	public void setContentLength(int length) {}
	
	@Override
	public void setContentLengthLong(long length) {}
}
