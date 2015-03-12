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
import com.sonicle.webtop.core.Service;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.WebTopSession;
import com.sonicle.webtop.core.bol.OContentType;
import com.sonicle.webtop.core.dal.ContentTypeDAO;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import net.sf.uadetector.ReadableUserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Environment implements BasicEnvironment, ManagerEnvironment {

	protected final static Logger logger = (Logger) LoggerFactory.getLogger(Environment.class);
	private static HashMap<String,ServiceManifest> manifestMap=new HashMap<>();
	
	protected final WebTopApp wta;
	protected final WebTopSession wts;

	public Environment(WebTopApp wta, WebTopSession wts) {
		this.wta = wta;
		this.wts = wts;
	}
	
	@Override
	public List<AppLocale> getLocales() {
		return wta.getI18nManager().getLocales();
	}
	
	@Override
	public List<TimeZone> getTimezones() {
		return wta.getI18nManager().getTimezones();
	}
	
	@Override
	public Connection getCoreConnection() throws SQLException {
		return wta.getConnectionManager().getConnection();
	}

	@Override
	public CoreServiceSettings getCoreServiceSettings() {
		return wts.getCoreServiceSettings();
	}

	@Override
	public CoreUserSettings getCoreUserSettings() {
		return wts.getCoreUserSettings();
	}

	@Override
	public UserProfile getProfile() {
		return wts.getUserProfile();
	}

	@Override
	public ReadableUserAgent getUserAgent() {
		return wts.getUserAgent();
	}

	@Override
	public String lookupResource(String serviceId, Locale locale, String key) {
		return wta.lookupResource(serviceId, locale, key);
	}

	@Override
	public String lookupResource(String serviceId, Locale locale, String key, boolean escapeHtml) {
		return wta.lookupResource(serviceId, locale, key, escapeHtml);
	}

	@Override
	public String lookupCoreResource(Locale locale, String key) {
		return wta.lookupResource(CoreManifest.ID, locale, key);
	}

	@Override
	public String getSessionRefererUri() {
		return wts.getRefererURI();
	}

	@Override
	public String getContentType(String extension) {
		extension=extension.toLowerCase();
		String ctype=null;
		Connection con=null;
        try {
            con=getCoreConnection();
			OContentType oct=ContentTypeDAO.getInstance().selectByExtension(con, extension);
            if (oct!=null) {
                ctype=oct.getContentType();
                logger.debug("Got content-type from db: {}={} ",extension,ctype);
            }
        } catch(SQLException exc) {
			logger.error("Error looking up content type for extension {}",extension,exc);
        } finally {
            DbUtils.closeQuietly(con);
        }
        return ctype;
	}
	
	@Override
	public String getExtension(String ctype) {
		ctype=ctype.toLowerCase();
		String extension=null;
		Connection con=null;
		try {
			con=getCoreConnection();
			OContentType oct=ContentTypeDAO.getInstance().selectByContentType(con, ctype);
			if (oct!=null) {
				extension=oct.getExtension();
			}
		} catch(SQLException exc) {
		} finally {
			DbUtils.closeQuietly(con);
		}
		return extension;
	}
	
	@Override
	public void sendWebSocketMessage(ServiceMessage message) {
		wts.send(message);
	}
	
	public static void addManifestMap(String classname, ServiceManifest manifest) {
//		logger.debug("adding manifest map {} -> {}",classname,manifest.getId());
		manifestMap.put(classname, manifest);
	}
	
	public static ServiceManifest getManifest(Class clazz) {
//		logger.debug("getting manifest for {}",clazz.getName());
		return manifestMap.get(clazz.getName());
	}
	
	public static String getServiceId(Class clazz) {
//		logger.debug("getting service id for {}",clazz.getName());
		return manifestMap.get(clazz.getName()).getId();
	}
	
	
	
}
