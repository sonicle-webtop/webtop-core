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
package com.sonicle.webtop.core.servlet.old;

import com.sonicle.commons.web.ServletUtils;
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
public class GZipResponseWrapper extends HttpServletResponseWrapper implements ClosableServletResponse {
	private GZipServletOutputStream outputStream = null;
	private PrintWriter writer = null;
	
	public GZipResponseWrapper(HttpServletResponse response) {
		super(response);
		ServletUtils.setCompressedContentHeader(response);
	}
	
	@Override
	public void close() throws IOException {
		if (writer != null) writer.close();
		if (outputStream != null) outputStream.close();
	}
	
	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) writer.flush();
		IOException ex1 = null;
		try {
			if (outputStream != null) outputStream.flush();
		} catch(IOException ex) {
			ex1 = ex;
		}
		IOException ex2 = null;
		try {
			super.flushBuffer();
		} catch(IOException ex) {
			ex2 = ex;
		}
		if (ex1 != null) throw ex1;
		if (ex2 != null) throw ex2;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("PrintWriter obtained already - cannot get OutputStream");
		}
		if (outputStream == null) {
			outputStream = new GZipServletOutputStream(getResponse().getOutputStream());
		}
		return outputStream;
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer == null && outputStream != null) {
			throw new IllegalStateException("OutputStream obtained already - cannot get PrintWriter");
		}
		if (writer == null) {
			outputStream = new GZipServletOutputStream(getResponse().getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(outputStream, getResponse().getCharacterEncoding()));
		}
		return writer;
	}
	
	@Override
	public void setContentLength(int len) {
		// Ignore this, content length of zipped response does not match the original content length
	}
}
