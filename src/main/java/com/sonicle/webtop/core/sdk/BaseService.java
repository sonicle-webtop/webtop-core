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
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.js.JsValue;
import com.sonicle.webtop.core.sdk.interfaces.IServiceUploadStreamListener;
import com.sonicle.webtop.core.sdk.interfaces.IServiceUploadListener;
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class BaseService extends BaseAbstractService {
	private boolean configured = false;
	private Environment env;
	private final HashMap<String, IServiceUploadListener> uploadListeners = new HashMap<>();
	private final HashMap<String, IServiceUploadStreamListener> uploadStreamListeners = new HashMap<>();
	
	public final void configure(Environment env) {
		if(configured) return;
		configured = true;
		this.env = env;
	}
	
	public final Environment getEnv() {
		return env;
	}
	
	public ClientOptions returnClientOptions() {
		return null;
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param key The resource key.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(String key) {
		return lookupResource(env.getProfile().getLocale(), key);
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param key The resource key.
	 * @param escapeHtml True to apply HTML escaping.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(String key, boolean escapeHtml) {
		return lookupResource(env.getProfile().getLocale(), key, escapeHtml);
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
		return env.wts.hasUploadedFile(uploadId);
	}
	
	public final UploadedFile getUploadedFile(String uploadId) {
		return env.wts.getUploadedFile(uploadId);
	}
	
	public final void removeUploadedFile(String uploadId) {
		env.wts.removeUploadedFile(uploadId, true);
	}
	
	public final void removeUploadedFileByTag(String tag) {
		env.wts.removeUploadedFileByTag(tag);
	}
	
	public void processSetToolComponentWidth(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer width = ServletUtils.getIntParameter(request, "width", true);
			
			UserProfile up = env.getProfile();
			CoreUserSettings cusx = new CoreUserSettings(SERVICE_ID, up.getId());
			cusx.setViewportToolWidth(width);
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			//logger.error("Error executing action SetToolComponentWidth", ex);
			new JsonResult(false, "Unable to save Tool width").printTo(out);
		}
	}
	
	public void processManageSuggestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<String[]> items = null;
		CoreManager core = WT.getCoreManager();
		
		try {
			String cntx = ServletUtils.getStringParameter(request, "context", true);	
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String query = ServletUtils.getStringParameter(request, "query", null);
				
				items = new ArrayList<>();
				List<OServiceStoreEntry> entries = core.listServiceStoreEntriesByQuery(SERVICE_ID, cntx, query, 50);
				for(OServiceStoreEntry entry : entries) {
					items.add(new String[]{entry.getValue()});
				}
				
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsValue> pl = ServletUtils.getPayload(request, JsValue.class);
				
				core.deleteServiceStoreEntry(SERVICE_ID, cntx, pl.data.id);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			WebTopApp.logger.error("Error executing action ManageSuggestions", ex);
			new JsonResult(false, "Error").printTo(out); //TODO: error message
		}	
	}
	
	public void processUpload(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ServletFileUpload upload = null;
		UploadedFile uploadedFile = null;
		
		try {
			String service = ServletUtils.getStringParameter(request, "service", true);
			String cntx = ServletUtils.getStringParameter(request, "context", true);
			String tag = ServletUtils.getStringParameter(request, "tag", null);
			if(!ServletFileUpload.isMultipartContent(request)) throw new Exception("No upload request");
			
			IServiceUploadStreamListener istream = getUploadStreamListener(cntx);
			if(istream != null) {
				try {
					MapItem data = new MapItem(); // Empty response data
					
					// Defines the upload object
					upload = new ServletFileUpload();
					FileItemIterator it = upload.getItemIterator(request);
					while(it.hasNext()) {
						FileItemStream fis = it.next();
						if(fis.isFormField()) continue; // Skip until first non-field item...
						
						// Creates uploaded object
						uploadedFile = new UploadedFile(true, service, IdentifierUtils.getUUID(), tag, fis.getName(), -1, findMediaType(fis));
						
						// Fill response data
						data.add("virtual", uploadedFile.isVirtual());
						
						// Handle listener, its implementation can stop
						// file upload throwing a UploadException.
						InputStream is = null;
						try {
							env.wts.addUploadedFile(uploadedFile);
							is = fis.openStream();
							istream.onUpload(cntx, request, uploadedFile, is, data);
						} finally {
							IOUtils.closeQuietly(is);
							env.wts.removeUploadedFile(uploadedFile, false);
						}
						
						// Plupload component (client-side) will upload multiple  
						// file each in its own request. So we can skip loop!
						break;
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
					while(it.hasNext()) {
						FileItem fi = (FileItem)it.next();
						if(fi.isFormField()) continue; // Skip until first non-field item...
						
						// Writes content into a temp file
						File file = WT.createTempFile();
						fi.write(file);
						
						// Creates uploaded object
						uploadedFile = new UploadedFile(false, service, file.getName(), tag, fi.getName(), fi.getSize(), findMediaType(fi));
						env.wts.addUploadedFile(uploadedFile);
						
						// Fill response data
						data.add("virtual", uploadedFile.isVirtual());
						data.add("uploadId", uploadedFile.getUploadId());
						
						// Handle listener (if present), its implementation can stop
						// file upload throwing a UploadException.
						if(iupload != null) {
							try {
								iupload.onUpload(cntx, request, uploadedFile, data);
							} catch(UploadException ex2) {
								env.wts.removeUploadedFile(uploadedFile, true);
								throw ex2;
							}
						}
						
						// Plupload component (client-side) will upload multiple  
						// file each in its own request. So we can skip loop!
						break;
					}	
					new JsonResult(data).printTo(out);
					
				} catch(UploadException ex1) {
					new JsonResult(false, ex1.getMessage()).printTo(out);
				}
			}
			
		} catch (Exception ex) {
			WebTopApp.logger.error("Error uploading", ex);
			new JsonResult(false, "Error uploading").printTo(out);
		}
	}
	
	public void processCleanupUploadedFiles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String tag = ServletUtils.getStringParameter(request, "tag", true);
			removeUploadedFileByTag(tag);
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			WebTopApp.logger.error("Error in CleanupUploadedFiles", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public UploadedFile addAsUploadedFile(String tag, String filename, String mediaType, InputStream is) throws IOException, WTException {
		String mtype = !StringUtils.isBlank(mediaType) ? mediaType : ServletHelper.guessMediaType(filename, true);
		File file = WT.createTempFile();
		FileOutputStream fos = null;
		long size = -1;
		try {
			fos = new FileOutputStream(file);
			size = IOUtils.copy(is, fos);
		} finally {
			IOUtils.closeQuietly(fos);
		}
		UploadedFile uploadedFile = new UploadedFile(false, SERVICE_ID, file.getName(), tag, filename, size, mtype);
		env.wts.addUploadedFile(uploadedFile);
		return uploadedFile;
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
	
	public static class ClientOptions extends HashMap<String, Object> {
		public ClientOptions() {
			super();
		}
	}
}
