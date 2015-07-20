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
import com.sonicle.webtop.core.CoreEnvironment;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.js.JsValue;
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
public abstract class BaseService extends BaseBaseService {
	
	private boolean configured = false;
	private Environment env;
	private CoreEnvironment coreEnv;
	
	public final void configure(Environment env, CoreEnvironment coreEnv) {
		if(configured) return;
		configured = true;
		this.env = env;
		this.coreEnv = coreEnv;
	}
	
	public final BasicEnvironment getEnv() {
		return env;
	}
	
	public final SuperEnvironment getSuperEnv() {
		if(coreEnv == null) throw new InsufficientRightsException("Insufficient rigths to access super environment");
		return coreEnv;
	}
	
	public HashMap<String, Object> returnClientOptions() {
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
	
	public void processSetToolComponentWidth(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer width = ServletUtils.getIntParameter(request, "width", true);
			
			UserProfile up = env.getProfile();
			CoreUserSettings cusx = new CoreUserSettings(up.getDomainId(), up.getUserId(), getId());
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
		CoreManager corem = env.wta.getManager();
		
		try {
			String context = ServletUtils.getStringParameter(request, "context", true);	
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String query = ServletUtils.getStringParameter(request, "query", null);
				
				items = new ArrayList<>();
				if(query != null) {
					List<OServiceStoreEntry> entries = corem.getServiceStoreEntriesByQuery(up.getId(), getId(), context, query);
					for(OServiceStoreEntry entry : entries) {
						items.add(new String[]{entry.getValue()});
					}
				}
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsValue> pl = ServletUtils.getPayload(request, JsValue.class);
				
				corem.deleteServiceStoreEntry(up.getId(), getId(), context, pl.data.id);
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
			String context = ServletUtils.getStringParameter(request, "context", true);
			if(!ServletFileUpload.isMultipartContent(request)) throw new Exception("No upload request");
			
			Method streamMethod = getUploadStreamMethod(context);
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
						uploadedFile = new UploadedFile(WT.generateUUID(), fis.getName(), true);
						env.wts.addUploadedFile(uploadedFile);
						data = streamMethod.invoke(this, request, fis.openStream());
						env.wts.removeUploadedFile(uploadedFile);
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
				
				// Defines the upload object
				DiskFileItemFactory factory = new DiskFileItemFactory();
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
						uploadedFile = new UploadedFile(file.getName(), fi.getName(), false);
						env.wts.addUploadedFile(uploadedFile);
						fi.write(file);
						items.add(uploadedFile.id);
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
			if(uploadedFile != null) env.wts.removeUploadedFile(uploadedFile);
			new JsonResult(false, "Error uploading").printTo(out);
		}
	}
	
	protected final Method getUploadStreamMethod(String context) {
		String methodName = MessageFormat.format("process{0}UploadStream", context);
		try {
			return getClass().getMethod(methodName, HttpServletRequest.class, InputStream.class);
		} catch(NoSuchMethodException ex) {
			return null;
		}
	}
}
