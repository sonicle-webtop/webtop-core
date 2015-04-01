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
package com.sonicle.webtop.core;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.bol.OContentType;
import com.sonicle.webtop.core.dal.ContentTypeDAO;
import com.sonicle.webtop.core.sdk.AppLocale;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author malbinola
 */
public class WT {
	
	private static WebTopApp getWTA() {
		return WebTopApp.getInstance();
	}
	
	public static List<AppLocale> getInstalledLocales() {
		return getWTA().getI18nManager().getLocales();
	}
	
	public static List<TimeZone> getTimezones() {
		return getWTA().getI18nManager().getTimezones();
	}
	
	public static Connection getCoreConnection() throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getConnection();
	}
	
	public static Connection getConnection(String serviceId) throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getConnection(serviceId);
	}
	
	public static Connection getConnection(ServiceManifest manifest) throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getConnection(manifest.getId());
	}
	
	public static String lookupCoreResource(Locale locale, String key) {
		return getWTA().lookupResource(CoreManifest.ID, locale, key);
	}
	
	public static String lookupResource(String serviceId, Locale locale, String key) {
		return getWTA().lookupResource(serviceId, locale, key);
	}
	
	public static String lookupResource(String serviceId, Locale locale, String key, boolean escapeHtml) {
		return getWTA().lookupResource(serviceId, locale, key, escapeHtml);
	}
	
	public static String getContentType(String extension) {
		String ctype = null;
		Connection con = null;
		
        try {
			extension = extension.toLowerCase();
            con=getCoreConnection();
			OContentType oct = ContentTypeDAO.getInstance().selectByExtension(con, extension);
            if (oct!=null) {
                ctype=oct.getContentType();
                //logger.debug("Got content-type from db: {}={} ",extension,ctype);
            }
        } catch(SQLException exc) {
			//logger.error("Error looking up content type for extension {}",extension,exc);
        } finally {
            DbUtils.closeQuietly(con);
        }
        return ctype;
	}
	
	public static String getExtension(String ctype) {
		String extension = null;
		Connection con = null;
		
		try {
			ctype = ctype.toLowerCase();
			con=getCoreConnection();
			OContentType oct = ContentTypeDAO.getInstance().selectByContentType(con, ctype);
			if (oct!=null) {
				extension = oct.getExtension();
			}
		} catch(SQLException exc) {
			
		} finally {
			DbUtils.closeQuietly(con);
		}
		return extension;
	}
}
