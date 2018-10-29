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
package com.sonicle.webtop.core.app.servlet.response;

import com.sonicle.commons.web.ServletUtils;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class GZippableOutputStream extends ServletOutputStream {
	private final AtomicBoolean opened = new AtomicBoolean(true);
	protected HttpServletResponse response;
	protected ServletOutputStream outputStream;
	protected GZIPOutputStream gzipOutputStream;
	protected Boolean skipCompress = null;
	protected int gzipMinThreshold;
	protected byte[] buffer;
	protected int bufferCount = 0;
	protected int length = -1;
	
	public GZippableOutputStream(HttpServletResponse response) throws IOException {
		super();
		this.response = response;
		outputStream = response.getOutputStream();
	}
	
	public void setGzipMinThreshold(int gzipMinThreshold) {
		this.gzipMinThreshold = gzipMinThreshold;
		buffer = new byte[gzipMinThreshold];
	}
	
	protected boolean isSkipCompress() {
		if (skipCompress == null) {
			skipCompress = !ServletUtils.isCompressible(ServletUtils.getContentTypeHeader(response))
					|| StringUtils.containsIgnoreCase(response.getHeader("X-Skip-Compress"), "1");
		}
		return skipCompress;
	}

	@Override
	public boolean isReady() {
		return outputStream.isReady();
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		outputStream.setWriteListener(writeListener);
	}
	
	@Override
	public void close() throws IOException {
		if (opened.compareAndSet(true, false)) {
			if (gzipOutputStream != null) {
				flushToGZip();
				gzipOutputStream.close();
				gzipOutputStream = null;
			} else if (bufferCount > 0) {
				outputStream.write(buffer, 0, bufferCount);
				bufferCount = 0;
			}
			outputStream.close();
		}
	}
	
	@Override
	public void flush() throws IOException {
		if (!opened.get()) return;
		if (isSkipCompress()) return;
		if (gzipOutputStream != null) {
			gzipOutputStream.flush();
		}
	}
	
	@Override
	public void write(int b) throws IOException {
		if (!opened.get()) throw new IOException("Cannot write to a closed output stream");
		if (isSkipCompress()) {
			outputStream.write(b);
		} else {
			if (bufferCount >= buffer.length) {
				flushToGZip();
			}
			buffer[bufferCount++] = (byte)b;
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (!opened.get()) throw new IOException("Cannot write to a closed output stream");
		if (isSkipCompress()) {
			outputStream.write(b, off, len);
		} else {
			if (len == 0) return;

			// Can we write into buffer ?
			if (len <= (buffer.length - bufferCount)) {
				System.arraycopy(b, off, buffer, bufferCount, len);
				bufferCount += len;
				return;
			}

			// There is not enough space in buffer. Flush it ...
			flushToGZip();

			// ... and try again. Note, that bufferCount = 0 here !
			if (len <= (buffer.length - bufferCount)) {
				System.arraycopy(b, off, buffer, bufferCount, len);
				bufferCount += len;
				return;
			}

			// write direct to gzip
			writeToGZip(b, off, len);
		}
	}
	
	protected void flushToGZip() throws IOException {
		if (bufferCount > 0) {
			writeToGZip(buffer, 0, bufferCount);
			bufferCount = 0;
		}
	}
	
	protected void writeToGZip(byte[] b, int off, int len) throws IOException {
		if (gzipOutputStream == null) {
			gzipOutputStream = new GZIPOutputStream(outputStream);
			ServletUtils.setCompressedContentHeader(response);
		}
		gzipOutputStream.write(b, off, len);
	}
}
