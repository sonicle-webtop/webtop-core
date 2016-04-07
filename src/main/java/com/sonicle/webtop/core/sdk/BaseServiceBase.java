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
import com.sonicle.webtop.core.app.WT;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

/**
 *
 * @author malbinola
 */
public abstract class BaseServiceBase {
	public static final String RESOURCE_SERVICE_NAME = "service.name";
	public static final String RESOURCE_SERVICE_DESCRIPTION = "service.name";
	public final String SERVICE_ID;
	
	public abstract void initialize() throws Exception;
	public abstract void cleanup() throws Exception;
	public abstract RunContext getRunContext();
	
	public BaseServiceBase() {
		SERVICE_ID = WT.findServiceId(this.getClass());
	}
	
	/**
	 * Gets WebTop Service manifest class.
	 * @return The manifest.
	 */
	public final ServiceManifest getManifest() {
		return WT.findManifest(this.getClass());
	}
	
	/**
	 * Gets WebTop Service's db connection.
	 * @return The db connection.
	 * @throws SQLException 
	 */
    public final Connection getConnection() throws SQLException {
		return WT.getConnection(SERVICE_ID);
    }
	
	/**
	 * Returns the localized name.
	 * @param locale The requested locale.
	 * @return The localized string.
	 */
	public final String getName(Locale locale) {
		return WT.lookupResource(SERVICE_ID, locale, RESOURCE_SERVICE_NAME);
	}
	
	/**
	 * Returns the localized description.
	 * @param locale The requested locale.
	 * @return The localized string.
	 */
	public final String getDescription(Locale locale) {
		return WT.lookupResource(SERVICE_ID, locale, RESOURCE_SERVICE_DESCRIPTION);
	}
	
	/**
	 * Returns the localized string associated to the key.
	 * @param locale The requested locale.
	 * @param key The resource key.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(Locale locale, String key) {
		return WT.lookupResource(SERVICE_ID, locale, key);
	}
    
	/**
	 * Returns the localized string associated to the key.
	 * @param locale The requested locale.
	 * @param key The resource key.
	 * @param escapeHtml True to apply HTML escaping.
	 * @return The translated string, or null if not found.
	 */
	public final String lookupResource(Locale locale, String key, boolean escapeHtml) {
		return WT.lookupResource(SERVICE_ID, locale, key, escapeHtml);
	}
}
