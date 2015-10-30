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
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.model.AuthResourceShareElement;
import com.sonicle.webtop.core.bol.model.AuthResourceShareFolder;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DomainDAO;
import com.sonicle.webtop.core.dal.ServiceStoreEntryDAO;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseServiceManager;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.userinfo.UserInfoProviderBase;
import com.sonicle.webtop.core.userinfo.UserInfoProviderFactory;
import com.sonicle.webtop.core.util.ZPushManager;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class CoreManager extends BaseServiceManager {
	private WebTopApp wta = null;
	
	public CoreManager(RunContext context, WebTopApp wta) {
		super(CoreManifest.ID, context);
		this.wta = wta;
	}
	
	public TFAManager getTFAManager() {
		return wta.getTFAManager();
	}
	
	public UserInfoProviderBase getUserInfoProvider() throws WTException {
		CoreServiceSettings css = new CoreServiceSettings("*", CoreManifest.ID);
		String providerName = css.getUserInfoProvider();
		return UserInfoProviderFactory.getProvider(providerName, wta.getConnectionManager(), wta.getSettingsManager());
	}
	
	public boolean isUserInfoProviderWritable() {
		try {
			return getUserInfoProvider().canWrite();
		} catch(WTException ex) {
			//TODO: logging?
			return false;
		}
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
			
		} catch(SQLException | DAOException ex) {
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
			UserDAO useDao = UserDAO.getInstance();
			return useDao.selectByDomain(con, domainId);
			
		} catch(SQLException | DAOException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OUser getUser(UserProfile.Id pid) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			UserDAO useDao = UserDAO.getInstance();
			return useDao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public String getInternetUserId(UserProfile.Id pid) throws Exception {
		ODomain domain = getDomain(pid.getDomainId());
		return new UserProfile.Id(domain.getDomainName(), pid.getUserId()).toString();
	}
	
	public UserPersonalInfo getUserPersonalInfo(UserProfile.Id pid) throws Exception {
		UserInfoProviderBase uip = getUserInfoProvider();
		return uip.getInfo(pid.getDomainId(), pid.getUserId());
	}
	
	public String getUserDisplayName(UserProfile.Id pid) throws Exception {
		OUser user = getUser(pid);
		if(user == null) throw new WTException("Unable to get user [{0}, {1}]", pid.getDomainId(), pid.getUserId());
		return user.getDisplayName();
	}
	
	public String getUserEmailAddress(UserProfile.Id pid) throws Exception {
		UserPersonalInfo info = getUserPersonalInfo(pid);
		if(info == null) throw new WTException("Unable to get personal info for user [{0}, {1}]", pid.getDomainId(), pid.getUserId());
		return info.getEmail();
	}
	
	public String getUserCompleteEmailAddress(UserProfile.Id pid) throws Exception {
		String address = getUserEmailAddress(pid);
		String displayName = getUserDisplayName(pid);
		return new InternetAddress(address, displayName).toString();
	}
	
	public OUser addUser(OUser item) throws Exception {
		UserInfoProviderBase uip = getUserInfoProvider();
		Connection con = null;
		int ret;
		
		try {
			con = WT.getCoreConnection();
			UserDAO useDao = UserDAO.getInstance();
			
			ret = useDao.insert(con, item);
			if(ret != 1) throw new WTException("Unable to insert user");
			// Performs user info insertion
			if(uip.canWrite()) {
				if(!uip.addUser(item.getDomainId(), item.getUserId())) {
					throw new WTException("Unable to insert user info");
				}
			}
			
			return item;
			
		} catch(Exception ex) {
			// Rollsback user info insertion
			if(uip.canWrite()) {
				uip.deleteUser(item.getDomainId(), item.getUserId());
			}
			
			throw ex;
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	//TODO: remove user
	
	public List<ActivityGrid> listLiveActivities(Collection<String> domainIds) {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			ActivityDAO dao = ActivityDAO.getInstance();
			return dao.viewLiveByDomains(con, domainIds);
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
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
			
		} catch(SQLException | DAOException ex) {
			return -1;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OCustomer> listCustomersByLike(String like) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			CustomerDAO dao = CustomerDAO.getInstance();
			return dao.viewByLike(con, like);
			
		} catch(SQLException | DAOException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCustomer getCustomer(String customerId) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			CustomerDAO dao = CustomerDAO.getInstance();
			return dao.viewById(con, customerId);
			
		} catch(SQLException | DAOException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean shareIsPermitted(OShare share, UserProfile.Id pid, String serviceId, String resource, String action) {
		//UserProfile.Id pid = new UserProfile.Id(share.getDomainId(), share.getTargetUserId());
		return WT.isPermitted(pid, serviceId, resource, action, share.getShareId());
	}
	
	public List<OShare> shareListIncoming(String serviceId, UserProfile.Id pid, String resource) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ShareDAO dao = ShareDAO.getInstance();
			return dao.selectByDomainTargetServiceResource(con, pid.getDomainId(), pid.getUserId(), serviceId, resource);
			
		} catch(SQLException | DAOException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	public OShare shareGet(String id) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ShareDAO dao = ShareDAO.getInstance();
			return dao.selectById(con, id);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException();
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	public OShare shareGet(UserProfile.Id sharingProfileId, String targetUserId, String serviceId, String resource, String instanceId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ShareDAO dao = ShareDAO.getInstance();
			return dao.selectByDomainUserTargetServiceResourceInstance(con, sharingProfileId.getDomainId(), sharingProfileId.getUserId(), targetUserId, serviceId, resource, instanceId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException();
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void shareAdd(UserProfile.Id sharingProfileId, String targetUserId, String serviceId, String resource, String instance, String name, String params, List<String> actions) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			con.setAutoCommit(false);
			ShareDAO dao = ShareDAO.getInstance();
			
			// 1 - Ensures we have a ready share on folder container
			// (resource name ends with '_SHARE_FOLDER' suffix)
			String foldResource = AuthResourceShareFolder.buildName(resource);
			OShare foldShare = dao.selectByDomainUserTargetServiceResourceInstance(con, sharingProfileId.getDomainId(), sharingProfileId.getUserId(), targetUserId, serviceId, foldResource, sharingProfileId.toString());
			if(foldShare == null) {
				//TODO: recuperare il corretto displayName di chi condivide
				//TODO: che succede se sharingProfileId è un gruppo?
				String foldName = sharingProfileId.toString();
				
				foldShare = new OShare();
				foldShare.setDomainId(sharingProfileId.getDomainId());
				foldShare.setUserId(sharingProfileId.getUserId());
				foldShare.setTargetUserId(targetUserId);
				foldShare.setServiceId(serviceId);
				foldShare.setResource(foldResource);
				foldShare.setInstance(sharingProfileId.toString());
				foldShare.setName(foldName);
				foldShare.setParameters(null);
				foldShare.setShareId(String.valueOf(dao.getSequence(con).intValue()));
				dao.insert(con, foldShare);
			}
			
			// 2 - Add the shared element
			// (resource name ends with '_SHARE_ELEMENT' suffix)
			String elemResource = AuthResourceShareElement.buildName(resource);
			OShare elemShare = new OShare();
			elemShare.setDomainId(sharingProfileId.getDomainId());
			elemShare.setUserId(sharingProfileId.getUserId());
			elemShare.setTargetUserId(targetUserId);
			elemShare.setServiceId(serviceId);
			elemShare.setResource(elemResource);
			elemShare.setInstance(instance);
			elemShare.setName(name);
			elemShare.setParameters(params);
			elemShare.setShareId(String.valueOf(dao.getSequence(con).intValue()));
			dao.insert(con, elemShare);
			
			AuthManager auth = wta.getAuthManager();
			for(String action : actions) {
				//TODO: che succede se sharingProfileId è un gruppo?
				auth.addPermission(con, sharingProfileId, serviceId, elemResource, action, instance);
			}
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException();
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> getPrivateServicesForUser(UserProfile profile) {
		return getPrivateServicesForUser(profile.getId());
	}
	
	public List<String> getPrivateServicesForUser(UserProfile.Id pid) {
		ServiceManager svcm = wta.getServiceManager();
		ArrayList<String> items = new ArrayList<>();
		
		List<String> ids = svcm.listPrivateServices();
		for(String id : ids) {
			// Checks user rights on service...
			if(WT.isPermitted(pid, CoreManifest.ID, CoreAuthKey.RES_SERVICE, CoreAuthKey.ACT_SERVICE_ACCESS, id)) {
				items.add(id);
			}
		}
		
		/*
		if(WT.hasRole(pid, AuthManager.ROLE_SYSADMIN)) {
			// Apply a predefined set of services if the user is WebTop admin
			result.add(CoreManifest.ID);
			//TODO: aggiungere il servizio di amministrazione
		} else {
			List<String> ids = svcm.listPrivateServices();
			for(String id : ids) {
				// Checks user rights on service...
				// Remember that permission for core service is automatically pushed by the realm.
				if(WT.isPermitted(pid, CoreManifest.ID, CoreAuthKey.RES_SERVICE, CoreAuthKey.ACT_SERVICE_ACCESS, id)) {
					result.add(id);
				}
			}
		}
		*/
		return items;
	}
	
	public List<UserOptionsServiceData> getUserOptionServicesForUser(UserProfile profile) {
		return getUserOptionServicesForUser(profile.getId());
	}
	
	public List<UserOptionsServiceData> getUserOptionServicesForUser(UserProfile.Id pid) {
		ServiceManager svcm = wta.getServiceManager();
		ArrayList<UserOptionsServiceData> items = new ArrayList<>();
		UserOptionsServiceData uos = null;
		
		List<String> ids = svcm.listUserOptionServices();
		for(String id : ids) {
			// Checks user rights on service...
			if(WT.isPermitted(pid, CoreManifest.ID, CoreAuthKey.RES_SERVICE, CoreAuthKey.ACT_SERVICE_ACCESS, id)) {
				uos = new UserOptionsServiceData(svcm.getManifest(id));
				uos.name = wta.lookupResource(id, Locale.ITALIAN, CoreLocaleKey.SERVICE_NAME);
				items.add(uos);
			}
		}
		return items;
	}
	
	public boolean hasPrivateService(UserProfile profile, String serviceId) {
		return hasPrivateService(profile.getId(), serviceId);
	}
	
	public boolean hasPrivateService(UserProfile.Id pid, String serviceId) {
		List<String> services = getPrivateServicesForUser(pid);
		return services.contains(serviceId);
	}
	
	public List<OServiceStoreEntry> listServiceStoreEntriesByQuery(UserProfile.Id profileId, String serviceId, String context, String query, int limit) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			if(query == null) {
				return sedao.selectKeyValueByLimit(con, profileId.getDomainId(), profileId.getUserId(), serviceId, context, limit);
			} else {
				return sedao.selectKeyValueByLikeKeyLimit(con, profileId.getDomainId(), profileId.getUserId(), serviceId, context, "%"+query+"%", limit);
			}
		
		} catch(SQLException | DAOException ex) {
			WebTopApp.logger.error("Error querying servicestore entry [{}, {}, {}, {}]", profileId, serviceId, context, query, ex);
			return new ArrayList<>();
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addServiceStoreEntry(UserProfile.Id profileId, String serviceId, String context, String key, String value) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
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
			
		} catch(SQLException | DAOException ex) {
			WebTopApp.logger.error("Error adding servicestore entry [{}, {}, {}, {}]", profileId, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(UserProfile.Id profileId) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.deleteByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			
		} catch(SQLException | DAOException ex) {
			WebTopApp.logger.error("Error deleting servicestore entry [{}]", profileId, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(UserProfile.Id pid, String serviceId) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.deleteByDomainUserService(con, pid.getDomainId(), pid.getUserId(), serviceId);
			
		} catch(SQLException | DAOException ex) {
			WebTopApp.logger.error("Error deleting servicestore entry [{}, {}]", pid, serviceId, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(UserProfile.Id pid, String serviceId, String context, String key) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.delete(con, pid.getDomainId(), pid.getUserId(), serviceId, context, key);
			
		} catch(SQLException | DAOException ex) {
			WebTopApp.logger.error("Error deleting servicestore entry [{}, {}, {}, {}]", pid, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SyncDevice> listZPushDevices() throws Exception {
		UserProfile.Id runPid = getRunContext().getProfileId();
		ZPushManager zpush = createZPushManager();
		
		boolean sysadmin = WT.hasRole(runPid, AuthManager.ROLE_SYSADMIN);
		String internetId = (sysadmin) ? null : getInternetUserId(runPid);
		
		ArrayList<SyncDevice> devices = new ArrayList<>();
		List<ZPushManager.LastsyncRecord> recs = zpush.listDevices();
		for(ZPushManager.LastsyncRecord rec : recs) {
			if(sysadmin || StringUtils.equalsIgnoreCase(rec.syncronizedUser, internetId)) {
				devices.add(new SyncDevice(rec.device, rec.syncronizedUser, rec.lastSyncTime));
			}
		}
		
		return devices;
	}
	
	public void deleteZPushDevice(String device, String user) throws Exception {
		ZPushManager zpush = createZPushManager();
		zpush.removeUserDevice(user, device);
	}
	
	public String getZPushDetailedInfo(String device, String user, String lineSep) throws Exception {
		ZPushManager zpush = createZPushManager();
		return zpush.getDetailedInfo(device, user, lineSep);
	}
	
	private ZPushManager createZPushManager() throws Exception {
		CoreServiceSettings css = new CoreServiceSettings("*", CoreManifest.ID);
		URI uri = new URI(css.getSyncDevicesShellUri());
		return new ZPushManager(css.getPhpPath(), css.getZPushPath(), uri);
	}
}
