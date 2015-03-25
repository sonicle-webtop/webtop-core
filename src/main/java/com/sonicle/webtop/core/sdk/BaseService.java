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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsPayload;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.CoreEnvironment;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.js.JsValue;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public abstract class BaseService {
	
	public static final String RESOURCE_SERVICE_NAME = "service.name";
	public static final String RESOURCE_SERVICE_DESCRIPTION = "service.name";
	private boolean configured = false;
	private Environment env;
	private CoreEnvironment coreEnv;
	
	public abstract void initialize();
	public abstract void cleanup();
	
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
	
	public final ManagerEnvironment getManagerEnv() {
		return env;
	}
	
	public final ServiceManifest getManifest() {
		return Environment.getManifest(this.getClass());
	}
	
	public final String getId() {
		return Environment.getServiceId(this.getClass());
	}
	
	public HashMap<String, Object> returnClientOptions() {
		return null;
	}
	
	/**
	 * Returns the localized name.
	 * @param locale The requested locale.
	 * @return The localized string.
	 */
	public final String getName(Locale locale) {
		return env.lookupResource(getId(), locale, RESOURCE_SERVICE_NAME);
	}
	
	/**
	 * Returns the localized description.
	 * @param locale The requested locale.
	 * @return The localized string.
	 */
	public final String getDescription(Locale locale) {
		return env.lookupResource(getId(), locale, RESOURCE_SERVICE_DESCRIPTION);
	}
	
	/**
	 * Returns a valid logger instance properly configured by WebTop 
	 * environment. Logger name is computed starting from specified class name.
	 * @param clazz A class.
	 * @return A logger instance.
	 */
	public static Logger getLogger(Class clazz) {
		return (Logger) LoggerFactory.getLogger(clazz);
	}
	
	/**
	 * (logger) Apply a custom diagnostic context (DC) to the default one.
	 * Passed value is associated to the key 'custom' of current DC.
	 * @param diagnosticContext Custom diagnostic context string value to append.
	 */
	public static void applyLoggerDC(String diagnosticContext) {
		WebTopApp.setServiceCustomLoggerDC(diagnosticContext);
	}
	
	/**
	 * (logger) Removes custom diagnostic context restoring the default one.
	 * Same behaviour calling: applyLoggerDC(null)
	 */
	public static void clearLoggerDC() {
		WebTopApp.unsetServiceCustomLoggerDC();
	}
    
    /**
	 * Gets service's db connection.
	 * @return The db connection.
	 * @throws SQLException 
	 */
    public final Connection getConnection() throws SQLException {
		//TODO: update return in order to get service connection
        return env.getCoreConnection();
    }
    
	/**
	 * Returns the localized string associated to the key.
	 * @param key The resource key.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(String key) {
		return env.lookupResource(getId(), env.getProfile().getLocale(), key);
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param key The resource key.
	 * @param escapeHtml True to apply HTML escaping.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(String key, boolean escapeHtml) {
		return env.lookupResource(getId(), env.getProfile().getLocale(), key, escapeHtml);
	}
	
	public void processManageSuggestions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsValue> items = null;
		UserProfile up = env.getProfile();
		CoreManager corem = env.wta.getManager();
		
		try {
			String context = ServletUtils.getStringParameter(request, "context", true);	
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String query = ServletUtils.getStringParameter(request, "query", true);
				
				items = new ArrayList<>();
				List<OServiceStoreEntry> entries = corem.getServiceStoreEntriesByQuery(up.getId(), getId(), context, query);
				for(OServiceStoreEntry entry : entries) {
					items.add(new JsValue(entry.getValue()));
				}
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				JsPayload<JsValue> pl = ServletUtils.getPayload(request, JsValue.class);
				
				corem.deleteServiceStoreEntry(up.getId(), getId(), context, pl.data.id);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			WebTopApp.logger.error("Error executing action ManageSuggestions", ex);
			new JsonResult(false, "Error").printTo(out); //TODO: error message
		}	
	}
}
