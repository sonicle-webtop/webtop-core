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
import com.sonicle.webtop.core.bol.ActivityGrid;
import com.sonicle.webtop.core.bol.CausalGrid;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.DomainDAO;
import com.sonicle.webtop.core.dal.ServiceStoreEntryDAO;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class CoreManager {
	
	private WebTopApp wta = null;
	
	CoreManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	public List<ODomain> listDomains(boolean enabledOnly) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			DomainDAO ddao = DomainDAO.getInstance();
			return (enabledOnly) ? ddao.selectEnabled(con) : ddao.selectAll(con);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ODomain getDomain(String domainId) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			DomainDAO ddao = DomainDAO.getInstance();
			return ddao.selectById(con, domainId);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OUser> listUsers(boolean enabledOnly) {
		Connection con = null;
		//TODO: gestire gli abilitati
		try {
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			return udao.selectAll(con);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OUser> listUsers(String domainId, boolean enabledOnly) {
		Connection con = null;
		//TODO: gestire gli abilitati
		try {
			con = WT.getCoreConnection();
			UserDAO dao = UserDAO.getInstance();
			return dao.selectByDomain(con, domainId);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<ActivityGrid> listLiveActivities(Collection<String> domainIds) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ActivityDAO dao = ActivityDAO.getInstance();
			return dao.viewLiveByDomains(con, domainIds);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OActivity> listLiveActivities(UserProfile.Id profileId) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ActivityDAO dao = ActivityDAO.getInstance();
			return dao.selectLiveByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OActivity getActivity(int activityId) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ActivityDAO dao = ActivityDAO.getInstance();
			return dao.select(con, activityId);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int insertActivity(OActivity item) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ActivityDAO dao = ActivityDAO.getInstance();
			item.setActivityId(dao.getSequence(con).intValue());
			return dao.insert(con, item);
			
		} catch(SQLException ex) {
			return -1;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int updateActivity(OActivity item) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ActivityDAO dao = ActivityDAO.getInstance();
			return dao.update(con, item);
			
		} catch(SQLException ex) {
			return -1;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deleteActivity(int activityId) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ActivityDAO dao = ActivityDAO.getInstance();
			return dao.delete(con, activityId);
			
		} catch(SQLException ex) {
			return -1;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<CausalGrid> listLiveCausals(Collection<String> domainIds) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			CausalDAO dao = CausalDAO.getInstance();
			return dao.viewLiveByDomains(con, domainIds);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OCausal> listLiveCausals(UserProfile.Id profileId, String customerId) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			CausalDAO dao = CausalDAO.getInstance();
			return dao.selectLiveByDomainUserCustomer(con, profileId.getDomainId(), profileId.getUserId(), customerId);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCausal getCausal(int causalId) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			CausalDAO dao = CausalDAO.getInstance();
			return dao.select(con, causalId);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int insertCausal(OCausal item) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			CausalDAO dao = CausalDAO.getInstance();
			item.setCausalId(dao.getSequence(con).intValue());
			return dao.insert(con, item);
			
		} catch(SQLException ex) {
			return -1;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int updateCausal(OCausal item) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			CausalDAO dao = CausalDAO.getInstance();
			return dao.update(con, item);
			
		} catch(SQLException ex) {
			return -1;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deleteCausal(int causalId) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			CausalDAO dao = CausalDAO.getInstance();
			return dao.delete(con, causalId);
			
		} catch(SQLException ex) {
			return -1;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OShare> listIncomingSharesForUser(String serviceId, UserProfile.Id profileId, String resource) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ShareDAO shaDao = ShareDAO.getInstance();
			return shaDao.selectIncomingByServiceDomainUserResource(con, serviceId, profileId.getDomainId(), profileId.getUserId(), resource);
			
		} catch(SQLException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> getPrivateServicesForUser(UserProfile profile) {
		return getPrivateServicesForUser(profile.getDomainId(), profile.getUserId());
	}
	
	public List<String> getPrivateServicesForUser(String domainId, String userId) {
		ServiceManager svcm = wta.getServiceManager();
		ArrayList<String> result = new ArrayList<>();
		
		if(UserProfile.isWebTopAdmin(domainId, userId)) {
			// Apply a predefined set of services if the user is WebTop admin
			result.add(CoreManifest.ID);
			//TODO: aggiungere il servizio di amministrazione
		} else {
			List<String> ids = svcm.listPrivateServices();
			for(String id : ids) {
				// Checks user rights on service...
				// Remember that permission for core service is automatically pushed by the realm.
				if(WT.isPermitted(CoreManifest.ID, CoreAuthKey.RES_SERVICE, CoreAuthKey.ACT_SERVICE_ACCESS, id)) {
					result.add(id);
				}
			}
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
	
	public void resetWhatsnew(String serviceId, UserProfile profile) {
		ServiceManager svcm = wta.getServiceManager();
		svcm.resetWhatsnew(serviceId, profile);
	}
	
	public List<OServiceStoreEntry> getServiceStoreEntriesByQuery(UserProfile.Id profileId, String serviceId, String context, String query) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			return sedao.selectKeyValueByLikeKey(con, profileId.getDomainId(), profileId.getUserId(), serviceId, context, "%"+query+"%");
		
		} catch(SQLException ex) {
			WebTopApp.logger.error("Error querying servicestore entry [{}, {}, {}, {}]", profileId, serviceId, context, query, ex);
			return new ArrayList<>();
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addServiceStoreEntry(UserProfile.Id profileId, String serviceId, String context, String key, String value) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			OServiceStoreEntry entry = sedao.select(con, profileId.getDomainId(), profileId.getUserId(), serviceId, context, key);
			
			DateTime now = DateTime.now(DateTimeZone.UTC);
			if(entry == null) {
				entry.setValue(value);
				entry.setFrequency(entry.getFrequency()+1);
				entry.setLastUpdate(now);
				
			} else {
				entry = new OServiceStoreEntry();
				entry.setDomainId(profileId.getDomainId());
				entry.setUserId(profileId.getUserId());
				entry.setServiceId(serviceId);
				entry.setContext(context);
				entry.setKey(StringUtils.upperCase(key));
				entry.setValue(value);
				entry.setFrequency(1);
				entry.setLastUpdate(now);
			}
			
		} catch(SQLException ex) {
			WebTopApp.logger.error("Error adding servicestore entry [{}, {}, {}, {}]", profileId, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(UserProfile.Id profileId) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.deleteByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			
		} catch(SQLException ex) {
			WebTopApp.logger.error("Error deleting servicestore entry [{}]", profileId, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(UserProfile.Id profileId, String serviceId) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.deleteByDomainUserService(con, profileId.getDomainId(), profileId.getUserId(), serviceId);
			
		} catch(SQLException ex) {
			WebTopApp.logger.error("Error deleting servicestore entry [{}, {}]", profileId, serviceId, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(UserProfile.Id profileId, String serviceId, String context, String key) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.delete(con, profileId.getDomainId(), profileId.getUserId(), serviceId, context, key);
			
		} catch(SQLException ex) {
			WebTopApp.logger.error("Error deleting servicestore entry [{}, {}, {}, {}]", profileId, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
}
