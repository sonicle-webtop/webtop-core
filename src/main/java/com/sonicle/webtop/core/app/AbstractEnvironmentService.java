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
package com.sonicle.webtop.core.app;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.sdk.UploadException;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.interfaces.IServiceUploadListener;
import com.sonicle.webtop.core.sdk.interfaces.IServiceUploadStreamListener;
import com.sonicle.webtop.core.app.servlet.ServletHelper;
import com.sonicle.webtop.core.app.servlet.js.BlobInfoPayload;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class AbstractEnvironmentService<E extends AbstractEnvironment> extends AbstractService {
	public static final String UPLOAD_TEMPFILE_PREFIX = "upload-";
	private boolean configured = false;
	private E env;
	private final HashMap<String, IServiceUploadListener> uploadListeners = new HashMap<>();
	private final HashMap<String, IServiceUploadStreamListener> uploadStreamListeners = new HashMap<>();
	private boolean documentServerEnabled;
	
	public abstract void ready() throws WTException;
	
	public final void configure(E env) {
		if(configured) return;
		configured = true;
		this.env = env;
		
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, env.getSession().getProfileDomainId());
		documentServerEnabled=css.getDocumentServerEnabled();
	}
	
	public E getEnv() {
		return env;
	}
	
	public boolean getDocumentServerEnabled() {
		return documentServerEnabled;
	}
	
	public final void registerUploadListener(String context, IServiceUploadListener listener) {
		synchronized(uploadListeners) {
			uploadListeners.put(context, listener);
		}
	}
	
	public final void registerUploadListener(String context, IServiceUploadStreamListener listener) {
		synchronized(uploadStreamListeners) {
			uploadStreamListeners.put(context, listener);
		}
	}
	
	private IServiceUploadListener getUploadListener(String context) {
		synchronized(uploadListeners) {
			return uploadListeners.get(context);
		}
	}
	
	private IServiceUploadStreamListener getUploadStreamListener(String context) {
		synchronized(uploadStreamListeners) {
			return uploadStreamListeners.get(context);
		}
	}
	
	public final boolean hasUploadedFile(String uploadId) {
		return getEnv().getSession().hasUploadedFile(uploadId);
	}
	
	public final WebTopSession.UploadedFile getUploadedFile(String uploadId) {
		return getEnv().getSession().getUploadedFile(uploadId);
	}
	
	public final WebTopSession.UploadedFile getUploadedFileOrThrow(String uploadId) throws WTException {
		WebTopSession.UploadedFile upFile = getUploadedFile(uploadId);
		if (upFile == null) throw new WTException("Uploaded file not found [{}]", uploadId);
		return upFile;
	}
	
	public final void removeUploadedFile(String uploadId) {
		getEnv().getSession().removeUploadedFile(uploadId, true);
	}
	
	public final void removeUploadedFileByTag(String tag) {
		getEnv().getSession().removeUploadedFileByTag(tag);
	}
	
	public void processUpload(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ServletFileUpload upload = null;
		WebTopSession.UploadedFile uploadedFile = null;
		HashMap<String, String> multipartParams = new HashMap<>();
		
		try {
			String service = ServletUtils.getStringParameter(request, "service", true);
			String cntx = ServletUtils.getStringParameter(request, "context", true);
			String tag = ServletUtils.getStringParameter(request, "tag", null);
			if (!ServletFileUpload.isMultipartContent(request)) throw new Exception("No upload request");
			
			IServiceUploadStreamListener istream = getUploadStreamListener(cntx);
			if (istream != null) {
				try {
					MapItem data = new MapItem(); // Empty response data
					
					// Defines the upload object
					upload = new ServletFileUpload();
					FileItemIterator it = upload.getItemIterator(request);
					while (it.hasNext()) {
						FileItemStream fis = it.next();
						if (fis.isFormField()) {
							InputStream is = null;
							try {
								is = fis.openStream();
								String key = fis.getFieldName();
								String value = IOUtils.toString(is, "UTF-8");
								multipartParams.put(key, value);
							} finally {
								IOUtils.closeQuietly(is);
							}
						} else {
							// Creates uploaded object
							uploadedFile = new WebTopSession.UploadedFile(true, service, IdentifierUtils.getUUID(), tag, fis.getName(), -1, findMediaType(fis));

							// Fill response data
							data.add("virtual", uploadedFile.isVirtual());
							data.add("editable", isFileEditableInDocEditor(fis.getName()));

							// Handle listener, its implementation can stop
							// file upload throwing a UploadException.
							InputStream is = null;
							try {
								getEnv().getSession().addUploadedFile(uploadedFile);
								is = fis.openStream();
								istream.onUpload(cntx, request, multipartParams, uploadedFile, is, data);
							} finally {
								IOUtils.closeQuietly(is);
								getEnv().getSession().removeUploadedFile(uploadedFile, false);
							}
						}
					}
					new JsonResult(data).printTo(out);
					
				} catch(UploadException ex1) {
					new JsonResult(false, ex1.getMessage()).printTo(out);
				} catch(Exception ex1) {
					throw ex1;
				}
				
			} else {
				try {
					MapItem data = new MapItem(); // Empty response data
					IServiceUploadListener iupload = getUploadListener(cntx);
					
					// Defines the upload object
					DiskFileItemFactory factory = new DiskFileItemFactory();
					//TODO: valutare come imporre i limiti
					//factory.setSizeThreshold(yourMaxMemorySize);
					//factory.setRepository(yourTempDirectory);
					upload = new ServletFileUpload(factory);
					List<FileItem> files = upload.parseRequest(request);
					
					// Plupload component (client-side) will upload multiple file 
					// each in its own request. So we can skip loop on files.
					Iterator it = files.iterator();
					while (it.hasNext()) {
						FileItem fi = (FileItem)it.next();
						if (fi.isFormField()) {
							InputStream is = null;
							try {
								is = fi.getInputStream();
								String key = fi.getFieldName();
								String value = IOUtils.toString(is, "UTF-8");
								multipartParams.put(key, value);
							} finally {
								IOUtils.closeQuietly(is);
							}
						} else {
							// Writes content into a temp file
							File file = WT.createTempFile(UPLOAD_TEMPFILE_PREFIX);
							fi.write(file);
							
							// Creates uploaded object
							uploadedFile = new WebTopSession.UploadedFile(false, service, file.getName(), tag, fi.getName(), fi.getSize(), findMediaType(fi));
							getEnv().getSession().addUploadedFile(uploadedFile);
							
							// Fill response data
							data.add("virtual", uploadedFile.isVirtual());
							data.add("uploadId", uploadedFile.getUploadId());
							data.add("editable", isFileEditableInDocEditor(fi.getName()));

							// Handle listener (if present), its implementation can stop
							// file upload throwing a UploadException.
							if (iupload != null) {
								try {
									iupload.onUpload(cntx, request, multipartParams, uploadedFile, data);
								} catch(UploadException ex2) {
									getEnv().getSession().removeUploadedFile(uploadedFile, true);
									throw ex2;
								}
							}
						}
					}	
					new JsonResult(data).printTo(out);
					
				} catch(UploadException ex1) {
					new JsonResult(ex1).printTo(out);
				}
			}
			
		} catch (Exception ex) {
			WebTopApp.logger.error("Error uploading", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processCleanupUploadedFiles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String tag = ServletUtils.getStringParameter(request, "tag", true);
			removeUploadedFileByTag(tag);
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			WebTopApp.logger.error("Error in CleanupUploadedFiles", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public WebTopSession.UploadedFile addAsUploadedFile(String tag, String filename, String mediaType, InputStream is) throws IOException, WTException {
		return addAsUploadedFile(SERVICE_ID, tag, filename, mediaType, is);
	}
	
	public WebTopSession.UploadedFile addAsUploadedFile(String tag, String filename, String mediaType, UploadedFileStreamWriter writer) throws IOException, WTException {
		return addAsUploadedFile(SERVICE_ID, tag, filename, mediaType, writer);
	}
	
	public WebTopSession.UploadedFile addAsUploadedFile(String tag, BlobInfoPayload payload) throws IOException, WTException {
		Base64InputStream b64is = null;
		try {
			b64is = new Base64InputStream(IOUtils.toInputStream(payload.base64, Charsets.UTF_8));
			return addAsUploadedFile(SERVICE_ID, tag, payload.filename, payload.mediaType, b64is);
		} finally {
			IOUtils.closeQuietly(b64is);
		}
	}
	
	public WebTopSession.UploadedFile addAsUploadedFile(String serviceId, String tag, String filename, String mediaType, InputStream is) throws IOException, WTException {
		return addAsUploadedFile(serviceId, tag, filename, mediaType, (out) -> {
			return IOUtils.copy(is, out);
		});
	}
	
	public WebTopSession.UploadedFile addAsUploadedFile(String serviceId, String tag, String filename, String mediaType, UploadedFileStreamWriter writer) throws IOException, WTException {
		String mtype = !StringUtils.isBlank(mediaType) ? mediaType : ServletHelper.guessMediaType(filename, true);
		File file = WT.createTempFile(UPLOAD_TEMPFILE_PREFIX);
		
		try {
			long size = -1;
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				size = writer.write(fos);
			} finally {
				IOUtils.closeQuietly(fos);
			}
			
			if (size == -1) size = file.length();
			WebTopSession.UploadedFile uploadedFile = new WebTopSession.UploadedFile(false, serviceId, file.getName(), tag, filename, size, mtype);
			getEnv().getSession().addUploadedFile(uploadedFile);
			return uploadedFile;
			
		} catch (Throwable t) {
			FileUtils.deleteQuietly(file);
			throw t;
		}
	}
	
	protected boolean isFileEditableInDocEditor(String fileName) {
		if ("pdf".equalsIgnoreCase(FilenameUtils.getExtension(fileName))) return false;
		return documentServerEnabled && DocEditorManager.isEditable(fileName);
	}
	
	private String findMediaType(FileItemStream fileItem) {
		String mtype = ServletHelper.guessMediaType(fileItem.getName());
		if(!StringUtils.isBlank(mtype)) return mtype;
		mtype = fileItem.getContentType();
		if(!StringUtils.isBlank(mtype)) return mtype;
		return "application/octet-stream";
	}
	
	private String findMediaType(FileItem fileItem) {
		String mtype = ServletHelper.guessMediaType(fileItem.getName());
		if(!StringUtils.isBlank(mtype)) return mtype;
		mtype = fileItem.getContentType();
		if(!StringUtils.isBlank(mtype)) return mtype;
		return "application/octet-stream";
	}
	
	public static interface UploadedFileStreamWriter {
		public long write(OutputStream out) throws IOException;
	}
}
