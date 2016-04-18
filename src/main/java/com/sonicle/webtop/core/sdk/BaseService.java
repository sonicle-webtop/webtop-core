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

import com.sonicle.webtop.core.app.RunContext;
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
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.MessageFormat;
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

/**
 *
 * @author malbinola
 */
public abstract class BaseService extends BaseServiceBase {
	private boolean configured = false;
	private RunContext context;
	private Environment env;
	
	public final void configure(RunContext context, Environment env) {
		if(configured) return;
		configured = true;
		this.context = context;
		this.env = env;
	}
	
	public final Environment getEnv() {
		return env;
	}
	
	@Override
	public RunContext getRunContext() {
		return context;
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
	
	public final boolean hasUploadedFile(String id) {
		return env.wts.hasUploadedFile(id);
	}
	
	public final UploadedFile getUploadedFile(String id) {
		return env.wts.getUploadedFile(id);
	}
	
	public final void clearUploadedFile(String id) {
		env.wts.clearUploadedFile(id);
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
		UserProfile up = env.getProfile();
		CoreManager core = WT.getCoreManager(getRunContext());
		
		try {
			String cntx = ServletUtils.getStringParameter(request, "context", true);	
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String query = ServletUtils.getStringParameter(request, "query", null);
				
				items = new ArrayList<>();
				List<OServiceStoreEntry> entries = core.listServiceStoreEntriesByQuery(up.getId(), SERVICE_ID, cntx, query, 50);
				for(OServiceStoreEntry entry : entries) {
					items.add(new String[]{entry.getValue()});
				}
				
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsValue> pl = ServletUtils.getPayload(request, JsValue.class);
				
				core.deleteServiceStoreEntry(up.getId(), SERVICE_ID, cntx, pl.data.id);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			WebTopApp.logger.error("Error executing action ManageSuggestions", ex);
			new JsonResult(false, "Error").printTo(out); //TODO: error message
		}	
	}
	
	private String findMediaType(FileItemStream fileItem) {
		String mtype = ServletHelper.guessMediaType(fileItem.getName());
		if(mtype == null) return mtype;
		mtype = fileItem.getContentType();
		if(mtype == null) return mtype;
		return "application/octet-stream";
	}
	
	private String findMediaType(FileItem fileItem) {
		String mtype = ServletHelper.guessMediaType(fileItem.getName());
		if(mtype == null) return mtype;
		mtype = fileItem.getContentType();
		if(mtype == null) return mtype;
		return "application/octet-stream";
	}
	
	public void processUpload(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ServletFileUpload upload = null;
		UploadedFile uploadedFile = null;
		
		try {
			String cntx = ServletUtils.getStringParameter(request, "context", true);
			if(!ServletFileUpload.isMultipartContent(request)) throw new Exception("No upload request");
			
			Method streamMethod = getUploadStreamMethod(cntx);
			if(streamMethod != null) {
				// Defines the upload object
				upload = new ServletFileUpload();
				
				// Process files...
				Object data = null;
				boolean succedeed = false;
				FileItemIterator fit = upload.getItemIterator(request);
				while(fit.hasNext()) {
					FileItemStream fis = fit.next();
					if(!fis.isFormField()) {
						uploadedFile = new UploadedFile(IdentifierUtils.getUUID(), fis.getName(), findMediaType(fis), true);
						env.wts.addUploadedFile(uploadedFile);
						data = streamMethod.invoke(this, request, fis.openStream());
						env.wts.clearUploadedFile(uploadedFile);
						succedeed = true;
						// Plupload client-side will upload multiple file each in its own
						// request; we can skip fileItems looping.
						break;
					}
				}
				
				if(!succedeed) throw new Exception("No file has been uploaded");
				new JsonResult(data).printTo(out);
				
			} else {
				ArrayList<String> items = new ArrayList<>();
				Method uploadMethod = getUploadMethod(cntx);
				
				// Defines the upload object
				DiskFileItemFactory factory = new DiskFileItemFactory();
				//TODO: valutare come imporre i limiti
				//factory.setSizeThreshold(yourMaxMemorySize);
				//factory.setRepository(yourTempDirectory);
				upload = new ServletFileUpload(factory);
				
				// Process files...
				boolean succedeed = false;
				List<FileItem> files = upload.parseRequest(request);
				Iterator it = files.iterator();
				while(it.hasNext()) {
					FileItem fi = (FileItem)it.next();
					if(!fi.isFormField()) {
						File file = WT.createTempFile();
						uploadedFile = new UploadedFile(file.getName(), fi.getName(), findMediaType(fi), false);
						env.wts.addUploadedFile(uploadedFile);
						fi.write(file);
						
						items.add(uploadedFile.id);
						if(uploadMethod != null) {
							try {
								uploadMethod.invoke(this, request, uploadedFile);
							} catch(Throwable t) {
								//TODO: aggiungere logging
								t.printStackTrace();
							}
						}
						succedeed = true;
						// Plupload client-side will upload multiple file each in its own
						// request; we can skip fileItems looping.
						break;
					}
				}
				
				if(!succedeed) throw new Exception("No file has been uploaded");
				MapItem mi = new MapItem().add("temp", true).add("uploadId", items.get(0));
				new JsonResult(mi).printTo(out);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			if(uploadedFile != null) env.wts.clearUploadedFile(uploadedFile);
			new JsonResult(false, "Error uploading").printTo(out);
		}
	}
	
	private Method getUploadMethod(String context) {
		String methodName = MessageFormat.format("processUpload{0}", context);
		try {
			return getClass().getMethod(methodName, HttpServletRequest.class, UploadedFile.class);
		} catch(NoSuchMethodException ex) {
			return null;
		}
	}
	
	private Method getUploadStreamMethod(String context) {
		String methodName = MessageFormat.format("processUploadStream{0}", context);
		try {
			return getClass().getMethod(methodName, HttpServletRequest.class, InputStream.class);
		} catch(NoSuchMethodException ex) {
			return null;
		}
	}
	
	public static class ClientOptions extends HashMap<String, Object> {
		public ClientOptions() {
			super();
		}
	}
}
