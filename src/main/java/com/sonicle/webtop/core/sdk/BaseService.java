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

import com.sonicle.webtop.core.app.PrivateEnvironment;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.AbstractEnvironmentService;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.js.JsValue;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author malbinola
 */
public abstract class BaseService extends AbstractEnvironmentService<PrivateEnvironment> {
	
	private boolean auditSetup=false;
	private boolean auditEnabled=false;
	
	public ServiceVars returnServiceVars() {
		return null;
	}
	
	public final String clientResTplString(String key) {
		return "{" + key + "@" + SERVICE_ID + "}";
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param key The resource key.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(String key) {
		return lookupResource(getEnv().getProfile().getLocale(), key);
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param key The resource key.
	 * @param escapeHtml True to apply HTML escaping.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(String key, boolean escapeHtml) {
		return lookupResource(getEnv().getProfile().getLocale(), key, escapeHtml);
	}
	
	public void processSetToolComponentWidth(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer width = ServletUtils.getIntParameter(request, "width", true);
			
			UserProfile up = getEnv().getProfile();
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
	
	public void processManageAutosave(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<String[]> items = null;
		CoreManager core = WT.getCoreManager();
		String cid = getEnv().getClientTrackingID();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			String cntx = ServletUtils.getStringParameter(request, "context", true);	
			String key = ServletUtils.getStringParameter(request, "key", true);
			if (crud.equals(Crud.READ)) {
			} else if (crud.equals(Crud.UPDATE)) {
				String value = ServletUtils.getStringParameter(request, "value", true);
				core.updateMyAutosaveData(cid, SERVICE_ID, cntx, key, value);
				new JsonResult().printTo(out);
			} else if (crud.equals(Crud.DELETE)) {
				core.deleteMyAutosaveData(cid, SERVICE_ID, cntx, key);
				new JsonResult().printTo(out);
			}
		} catch(Exception ex) {
			WebTopApp.logger.error("Error executing action AutoSave", ex);
			new JsonResult(false, "Error").printTo(out); //TODO: error message
		}	
	}
	
	public static class ServiceVars extends HashMap<String, Object> {
		public ServiceVars() {
			super();
		}
	}
	
	public void writeAuditLog(String context, String action, String referenceId, String data) {
		if (isAuditEnabled()) WT.writeAuditLog(SERVICE_ID, context, action, referenceId, data);
	}
	
	public boolean isAuditEnabled() {
		if (!auditSetup) {
			String domainId=this.getEnv().getProfileId().getDomainId();
			boolean coreAuditEnabled=new CoreServiceSettings(CoreManifest.ID, domainId).isAuditEnabled();
			auditEnabled = coreAuditEnabled;
			if (coreAuditEnabled) {
				CoreServiceSettings scss=new CoreServiceSettings(SERVICE_ID, domainId);
				//if we have an entry for this service, use this
				if (scss.hasAuditEnabled())
					auditEnabled = scss.isAuditEnabled();
				else {
					//user main core setup
				}
			}
			
			auditSetup=true;
		}

		return auditEnabled;
	}
}
