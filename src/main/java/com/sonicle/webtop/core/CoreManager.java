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

import com.sonicle.webtop.core.sdk.CoreLocaleKey;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.js.JsWTStartup;
import com.sonicle.webtop.core.bol.js.JsWhatsnew;
import com.sonicle.webtop.core.dal.DomainDAO;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class CoreManager {
	
	private WebTopApp wta = null;
	
	CoreManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	public List<ODomain> getDomains() {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			DomainDAO dao = DomainDAO.getInstance();
			return dao.selectAll(con);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public JsWTStartup.Service getServiceJsDescriptor(String serviceId, Locale locale) {
		ServiceManager svcm = wta.getServiceManager();
		ServiceDescriptor sdesc = svcm.getService(serviceId);
		ServiceManifest manifest = sdesc.getManifest();
		
		JsWTStartup.Service js = new JsWTStartup.Service();
		js.id = manifest.getId();
		js.xid = manifest.getXId();
		js.name = wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_NAME);
		js.description = wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_DESCRIPTION);
		js.version = manifest.getVersion().toString();
		js.build = manifest.getBuildDate();
		js.company = manifest.getCompany();
		js.className = manifest.getJsClassName();
		
		return js;
	}
	
	public List<String> getUserServices(UserProfile profile) {
		return getUserServices(profile.getDomainId(), profile.getUserId());
	}
	
	public List<String> getUserServices(String domainId, String userId) {
		ServiceManager svcm = wta.getServiceManager();
		ArrayList<String> result = new ArrayList<>();
		
		List<String> ids = svcm.getServices();
		for(String id : ids) {
			//TODO: check if service is allowed for user
			result.add(id);
		}
		return result;
	}
	
	public boolean needWhatsnew(String serviceId, UserProfile profile) {
		ServiceManager svcm = wta.getServiceManager();
		return svcm.needWhatsnew(serviceId, profile);
	}
	
	public String getWhatsnewHtml(String serviceId, UserProfile profile, boolean full) {
		ServiceManager svcm = wta.getServiceManager();
		return svcm.getWhatsnew(serviceId, profile, full);
	}
	
	/*
	public List<JsWhatsnew> getUserWhatsnew(UserProfile profile, boolean full, List<String> serviceIds) {
		ArrayList<JsWhatsnew> items = new ArrayList<>();
		String html = null;
		JsWhatsnew js = null;
		ServiceManager svcm = wta.getServiceManager();
		
		if(serviceIds == null) serviceIds = getUserServices(profile);
		for(String id: serviceIds) {
			if(full || svcm.needWhatsnew(id, profile)) {
				html = svcm.getWhatsnew(id, profile, full);
				if(!StringUtils.isEmpty(html)) {
					js = new JsWhatsnew(id);
					js.title = wta.lookupResource(id, profile.getLocale(), CoreLocaleKey.SERVICE_NAME);
					js.html = html;
					items.add(js);
				}
			}
		}
		return items;
	}
	*/
}
