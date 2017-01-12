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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.beans.VirtualAddress;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.webtop.core.app.CoreAdminManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.OTPManager;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.provider.RecipientsProviderBase;
import com.sonicle.webtop.core.bol.VActivity;
import com.sonicle.webtop.core.bol.CausalGrid;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OAutosave;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.OSnoozedReminder;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OShareData;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.ServiceSharePermission;
import com.sonicle.webtop.core.bol.model.SharePermsElements;
import com.sonicle.webtop.core.bol.model.SharePermsFolder;
import com.sonicle.webtop.core.bol.model.IncomingShareRoot;
import com.sonicle.webtop.core.bol.model.InternetRecipient;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.bol.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.AutosaveDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.SnoozedReminderDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.ServiceStoreEntryDAO;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.ShareDataDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.interfaces.IRecipientsProvidersSource;
import com.sonicle.webtop.core.util.ZPushManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreManager extends BaseManager {
	private static final Logger logger = WT.getLogger(CoreManager.class);
	private WebTopApp wta = null;
	
	private final HashSet<String> cacheReady = new HashSet<>();
	private final ArrayList<String> cacheAllowedServices = new ArrayList<>();
	private final LinkedHashMap<String, RecipientsProviderBase> cacheProfileRecipientsProvider = new LinkedHashMap<>();
	
	public CoreManager(WebTopApp wta, boolean fastInit, UserProfile.Id targetProfileId) {
		super(fastInit, targetProfileId);
		this.wta = wta;
		
		if(!fastInit) {
			//initAllowedServices();
		}
	}
	
	private void initAllowedServices() {
		synchronized(cacheAllowedServices) {
			cacheAllowedServices.addAll(doListAllowedServices());
			cacheReady.add("cacheAllowedServices");
		}
	}

	public boolean hasService(String serviceId) {
		return wta.getServiceManager().hasService(serviceId);
	}
	
	public ServiceManager getServiceManager() {
		ensureCallerService(SERVICE_ID, "getServiceManager");
		return wta.getServiceManager();
	}
	
	public OTPManager getOTPManager() {
		ensureCallerService(SERVICE_ID, "getOTPManager");
		return wta.getOTPManager();
	}
	
	public List<JsSimple> listThemes() throws WTException {
		ArrayList<JsSimple> items = new ArrayList<>();
		//TODO: gestire i temi dinamicamente
		items.add(new JsSimple("aria", "Aria"));
		items.add(new JsSimple("classic", "Classic"));
		items.add(new JsSimple("crisp", "Crisp"));
		items.add(new JsSimple("crisp-touch", "Crisp Touch"));
		items.add(new JsSimple("gray", "Gray"));
		items.add(new JsSimple("neptune", "Neptune"));
		items.add(new JsSimple("neptune-touch", "Neptune Touch"));
		return items;
	}
	
	public List<JsSimple> listLayouts() throws WTException {
		ArrayList<JsSimple> items = new ArrayList<>();
		items.add(new JsSimple("default", WT.getPlatformName()));
		items.add(new JsSimple("stacked", "Outlook 2007/2003"));
		items.add(new JsSimple("queued", "Mozilla"));
		return items;
	}
	
	public List<JsSimple> listLAFs() throws WTException {
		ArrayList<JsSimple> items = new ArrayList<>();
		//TODO: gestire i look&feel (lafs) dinamicamente
		items.add(new JsSimple("default", WT.getPlatformName()));
		return items;
	}
	
	public AbstractDirectory getAuthDirectory(String domainId) throws WTException {
		ODomain domain = getDomain(domainId);
		if(domain == null) throw new WTException("Domain not found [{0}]", domainId);
		
		return getAuthDirectory(domain);
	}
	
	public AbstractDirectory getAuthDirectory(ODomain domain) throws WTException {
		return wta.getWebTopManager().getAuthDirectory(domain.getAuthUri());
	}
	
	public List<ODomain> listDomains(boolean enabledOnly) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.listDomains(enabledOnly);
		} else {
			ODomain domain = wtmgr.getDomain(RunContext.getRunProfileId().getDomain());
			return domain.getEnabled() ? Arrays.asList(domain) : new ArrayList<ODomain>();
		}
	}
	
	public ODomain getDomain() throws WTException {
		return getDomain(getTargetProfileId().getDomainId());
	}
	
	public ODomain getDomain(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.getDomain(domainId);
		} else {
			ensureUserDomain(domainId);
			return wtmgr.getDomain(domainId);
		}
	}
	
	/**
	 * Lists domain real roles (those defined as indipendent role).
	 * Target domain ID is taken from manager targetProfile's domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	public List<Role> listRoles() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		WebTopManager wtmgr = wta.getWebTopManager();
		try {
			return wtmgr.listRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list roles [{0}]", domainId);
		}
	}
	
	/**
	 * Lists domain users roles (those coming from a user).
	 * Target domain ID is taken from manager targetProfile's domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	public List<Role> listUsersRoles() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		WebTopManager wtmgr = wta.getWebTopManager();
		try {
			return wtmgr.listUsersRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list users roles [{0}]", domainId);
		}
	}
	
	/**
	 * Lists domain groups roles (those coming from a group).
	 * Target domain ID is taken from manager targetProfile's domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	public List<Role> listGroupsRoles() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		WebTopManager wtmgr = wta.getWebTopManager();
		try {
			return wtmgr.listGroupsRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups roles [{0}]", domainId);
		}
	}
	
	public List<OUser> listUsers(boolean enabledOnly) throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		WebTopManager wtmgr = wta.getWebTopManager();
		
		try {
			return wtmgr.listUsers(domainId, enabledOnly);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list users [{0}]", domainId);
		}
	}
	
	public OUser getUser() throws WTException {
		return getUser(getTargetProfileId());
	}
	
	public OUser getUser(UserProfile.Id pid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.getUser(pid);
		} else {
			//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
			ensureUserDomain(pid.getDomainId());
			return wtmgr.getUser(pid);
		}
	}
	
	public UserProfile.Data getUserData() throws WTException {
		return wta.getWebTopManager().userData(getTargetProfileId());
	}
	
	public UserProfile.PersonalInfo getUserPersonalInfo() throws WTException {
		return getUserPersonalInfo(getTargetProfileId());
	}
	
	public UserProfile.PersonalInfo getUserPersonalInfo(UserProfile.Id pid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.getUserPersonalInfo(pid);
		} else {
			ensureUserDomain(pid.getDomainId());
			return wtmgr.getUserPersonalInfo(pid);
		}
	}
	
	public boolean updateUserDisplayName(String displayName) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.updateUserDisplayName(getTargetProfileId(), displayName);
		} else {
			//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
			ensureUser();
			return wtmgr.updateUserDisplayName(getTargetProfileId(), displayName);
		}
	}
	
	public boolean updateUserPersonalInfo(UserProfile.PersonalInfo userPersonalInfo) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.updateUserPersonalInfo(getTargetProfileId(), userPersonalInfo);
		} else {
			//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
			ensureUser();
			return wtmgr.updateUserPersonalInfo(getTargetProfileId(), userPersonalInfo);
		}
	}
	
	public void cleanUserProfileCache() {
		ensureCallerService(SERVICE_ID, "cleanupUserProfileCache");
		wta.getWebTopManager().cleanUserProfileCache(getTargetProfileId());
	}
	
	public List<OGroup> listGroups() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		WebTopManager wtmgr = wta.getWebTopManager();
		
		try {
			return wtmgr.listGroups(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups [{0}]", domainId);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public List<String> listInstalledServices() {
		ServiceManager svcm = wta.getServiceManager();
		return svcm.listRegisteredServices();
	}
	
	public List<String> listAllowedServices() {
		ArrayList<String> ids = new ArrayList<>();
		
		ensureUserDomain();
		if(RunContext.isSysAdmin()) {
			ids.add(CoreManifest.ID);
			ids.add(CoreAdminManifest.ID);
			ids.add("com.sonicle.webtop.vfs");
		} else {
			ServiceManager svcm = wta.getServiceManager();
			for(String id : svcm.listRegisteredServices()) {
				if(RunContext.isPermitted(SERVICE_ID, "SERVICE", "ACCESS", id)) ids.add(id);
			}
		}
		return ids;
		/*
		synchronized(cacheAllowedServices) {
			if(!cacheReady.contains("cacheAllowedServices")) {
				initAllowedServices();
			}
			return cacheAllowedServices;
		}
		*/
	}
	
	public List<ServicePermission> listServicePermissions(String serviceId) throws WTException {
		ServiceManager svcm = wta.getServiceManager();
		ServiceManifest manifest = svcm.getManifest(serviceId);
		if(manifest == null) throw new WTException("Service not found [{0}]", serviceId);
		return manifest.getDeclaredPermissions();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private List<String> doListAllowedServices() {
		ArrayList<String> ids = new ArrayList<>();
		
		ensureUserDomain();
		if(RunContext.isSysAdmin()) {
			ids.add(CoreManifest.ID);
			ids.add(CoreAdminManifest.ID);
		} else {
			ServiceManager svcm = wta.getServiceManager();
			for(String id : svcm.listRegisteredServices()) {
				if(RunContext.isPermitted(SERVICE_ID, "SERVICE", "ACCESS", id)) ids.add(id);
			}
		}
		return ids;
	}
	
	

	
	/*
	private LinkedHashMap<String, RecipientsProviderBase> getDomainRecipientsProviders() {
		
	}
	*/
	
	private void buildProfileRecipientsProviderCache() {
		for(String serviceId : listAllowedServices()) {
			BaseManager manager = WT.getServiceManager(serviceId, getTargetProfileId());
			if(manager instanceof IRecipientsProvidersSource) {
				List<RecipientsProviderBase> providers = ((IRecipientsProvidersSource)manager).returnRecipientsProviders();
				if(providers == null) continue;
				
				for(RecipientsProviderBase provider : providers) {
					final CompositeId cid = new CompositeId().setTokens(serviceId, provider.getId());
					cacheProfileRecipientsProvider.put(cid.toString(), provider);
				}
			}
		}
	}
	
	private LinkedHashMap<String, RecipientsProviderBase> getProfileRecipientsProviders() {
		synchronized(cacheProfileRecipientsProvider) {
			if(!cacheReady.contains("cacheProfileRecipientsProvider")) {
				buildProfileRecipientsProviderCache();
				cacheReady.add("cacheProfileRecipientsProvider");
			}
			return cacheProfileRecipientsProvider;
		}
	}
	
	@Override
	protected Locale findLocale() {
		try {
			return getUserData().getLocale();
		} catch(Exception ex) {
			return Locale.ENGLISH;
		}
	}
	
	
	
	/*
	public boolean writeLog(String action, String remoteIp, String userAgent, String sessionId, String data) {
		//TODO: trovare modo di completare serviceId (ora a "")
		return wta.getLogManager().write(RunContext.getProfileId(), "", action, getSoftwareName(), remoteIp, userAgent, sessionId, data);
	}
	
	public boolean writeLog(String action, String data) {
		//TODO: trovare modo di completare serviceId (ora a "")
		return wta.getLogManager().write(RunContext.getProfileId(), "", action, getSoftwareName(), null, null, null, data);
	}
	*/
	
	
	
	
	
	
	public UserProfile.Id userUidToProfileId(String userUid) {
		return wta.getWebTopManager().uidToUser(userUid);
	}
	
	
	
	
	
	public String getInternetUserId(UserProfile.Id pid) throws WTException {
		return wta.getWebTopManager().getInternetUserId(pid);
	}
	
	
	/*
	public String getUserCompleteEmailAddress(UserProfile.Id pid) throws Exception {
		String address = getUserEmailAddress(pid);
		String displayName = getUserDisplayName(pid);
		return new InternetAddress(address, displayName).toString();
	}
	*/
	
	public List<UserProfile.Id> listProfilesWithSetting(String serviceId, String key, Object value) throws WTException {
		SettingsManager setm = wta.getSettingsManager();
		return setm.listProfilesWith(serviceId, key, value);
	}
	
	public List<VActivity> listLiveActivities(Collection<String> domainIds) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			return dao.viewLiveByDomains(con, domainIds);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OActivity> listLiveActivities(UserProfile.Id profileId) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			return dao.selectLiveByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OActivity getActivity(int activityId) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			return dao.select(con, activityId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int addActivity(OActivity item) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		try {
			RunContext.ensureIsPermitted(SERVICE_ID, "ACTIVITIES", "MANAGE");
			con = WT.getCoreConnection();
			item.setActivityId(dao.getSequence(con).intValue());
			return dao.insert(con, item);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int updateActivity(OActivity item) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		try {
			RunContext.ensureIsPermitted(SERVICE_ID, "ACTIVITIES", "MANAGE");
			con = WT.getCoreConnection();
			return dao.update(con, item);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deleteActivity(int activityId) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		try {
			RunContext.ensureIsPermitted(SERVICE_ID, "ACTIVITIES", "MANAGE");
			con = WT.getCoreConnection();
			return dao.delete(con, activityId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<CausalGrid> listLiveCausals(Collection<String> domainIds) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			return dao.viewLiveByDomains(con, domainIds);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OCausal> listLiveCausals(UserProfile.Id profileId, String customerId) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			return dao.selectLiveByDomainUserCustomer(con, profileId.getDomainId(), profileId.getUserId(), customerId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCausal getCausal(int causalId) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			return dao.select(con, causalId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int addCausal(OCausal item) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		try {
			RunContext.ensureIsPermitted(SERVICE_ID, "CAUSALS", "MANAGE");
			con = WT.getCoreConnection();
			item.setCausalId(dao.getSequence(con).intValue());
			return dao.insert(con, item);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int updateCausal(OCausal item) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		try {
			RunContext.ensureIsPermitted(SERVICE_ID, "CAUSALS", "MANAGE");
			con = WT.getCoreConnection();
			return dao.update(con, item);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deleteCausal(int causalId) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		
		try {
			RunContext.ensureIsPermitted(SERVICE_ID, "CAUSALS", "MANAGE");
			con = WT.getCoreConnection();
			return dao.delete(con, causalId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OCustomer> listCustomersByLike(String like) throws WTException {
		CustomerDAO dao = CustomerDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.viewByLike(con, like);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCustomer getCustomer(String customerId) throws WTException {
		CustomerDAO dao = CustomerDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.viewById(con, customerId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OTPManager.EmailConfig otpConfigureUsingEmail(String emailAddress) throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		return otp.configureEmail(getTargetProfileId(), emailAddress);
	}
	
	public OTPManager.GoogleAuthConfig otpConfigureUsingGoogleAuth(int qrCodeSize) throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		return otp.configureGoogleAuth(getTargetProfileId(), qrCodeSize);
	}
	
	public boolean otpActivate(OTPManager.Config config, int code) throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		return otp.activate(getTargetProfileId(), config, code);
	}
	
	public void otpDeactivate() throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		otp.deactivate(getTargetProfileId());
	}
	
	public OTPManager.Config otpPrepareVerifyCode() throws WTException {
		ensureCallerService(SERVICE_ID, "otpPrepareVerifyCode");
		OTPManager otp = getOTPManager();
		return otp.prepareCheckCode(getTargetProfileId());
	}
	
	public boolean otpVerifyCode(OTPManager.Config params, int code) throws WTException {
		ensureCallerService(SERVICE_ID, "otpVerifyCode");
		OTPManager otp = getOTPManager();
		return otp.checkCode(getTargetProfileId(), params, code);
	}
	
	public List<IncomingShareRoot> listIncomingShareRoots(String serviceId, String groupName) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		UserDAO usedao = UserDAO.getInstance();
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			String profileUid = wtmgr.userToUid(targetPid);
			List<String> roleUids = wtmgr.getComputedRolesAsStringByUser(targetPid, true, true);
			
			String rootKey = OShare.buildRootKey(groupName);
			String folderKey = OShare.buildFolderKey(groupName);
			String rootPermissionKey = ServiceSharePermission.buildRootPermissionKey(groupName);
			String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
			String elementsPermissionKey = ServiceSharePermission.buildElementsPermissionKey(groupName);
			
			con = WT.getCoreConnection();
			
			// In order to find incoming root, we need to pass through folders
			// that have at least a permission, getting incoming uids.
			// We look into permission returning each share instance that have 
			// "*@FOLDER" as key and satisfies a set of roles. Then we can
			// get a list of unique uids (from shares table) that owns the share.
			List<String> permissionKeys = Arrays.asList(rootPermissionKey, folderPermissionKey, elementsPermissionKey);
			List<String> originUids = shadao.viewOriginByRoleServiceKey(con, roleUids, serviceId, folderKey, permissionKeys);
			ArrayList<IncomingShareRoot> roots = new ArrayList<>();
			for(String uid : originUids) {
				if(uid.equals(profileUid)) continue; // Skip self role
				
				// Foreach incoming uid we have to find the root share and then
				// test if READ right is allowed
				
				OShare root = shadao.selectByUserServiceKeyInstance(con, uid, serviceId, rootKey, OShare.INSTANCE_ROOT);
				if(root == null) continue;
				OUser user = usedao.selectByUid(con, uid);
				if(user == null) continue;
				
				roots.add(new IncomingShareRoot(root.getShareId().toString(), wtmgr.uidToUser(root.getUserUid()), user.getDisplayName()));
			}
			return roots;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "Unable to list share roots for {0}", targetPid.toString());
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Lists incoming share folders (level 1, eg: Calendars, Categories, etc) 
	 * for the targetProfile.
	 * @param rootShareId The root share ID
	 * @param groupName The permission groupName
	 * @return
	 * @throws WTException 
	 */
	public List<OShare> listIncomingShareFolders(String rootShareId, String groupName) throws WTException {
		ShareDAO shadao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OShare rootShare = shadao.selectById(con, Integer.valueOf(rootShareId));
			if(rootShare == null) throw new WTException("Unable to find root share [{0}]", rootShareId);
			
			String folderShareKey = OShare.buildFolderKey(groupName);
			String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
			
			ArrayList<OShare> folders = new ArrayList<>();
			List<OShare> shares = shadao.selectByUserServiceKey(con, rootShare.getUserUid(), rootShare.getServiceId(), folderShareKey);
			for(OShare share : shares) {
				if(RunContext.isPermitted(getTargetProfileId(), rootShare.getServiceId(), folderPermissionKey, ServicePermission.ACTION_READ, share.getShareId().toString())) {
					folders.add(share);
				}
			}
			return folders;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public <T>T getIncomingShareFolderData(String shareId, Class<T> type) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		ShareDataDAO shddao = ShareDataDAO.getInstance();
		Connection con = null;
		
		try {
			String profileUid = usrm.userToUid(getTargetProfileId());
			con = WT.getCoreConnection();
			
			OShare share = shadao.selectById(con, Integer.valueOf(shareId));
			if(share == null) throw new WTException("Unable to find share [{0}]", shareId);
			if(!areActionsPermittedOnShare(share, ServiceSharePermission.TARGET_FOLDER, new String[]{ServicePermission.ACTION_READ})[0]) {
				throw new WTException("Share not accessible [{0}]", shareId);
			}
			
			OShareData data = shddao.selectByShareUser(con, Integer.valueOf(shareId), profileUid);
			if(data != null) {
				return LangUtils.value(data.getValue(), null, type);
			} else {
				return null;
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean[] areActionsPermittedOnShare(String shareId, String permissionTarget, String[] actions) throws WTException {
		ShareDAO shadao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OShare share = shadao.selectById(con, Integer.valueOf(shareId));
			if(share == null) throw new WTException("Unable to find share [{0}]", shareId);
			return areActionsPermittedOnShare(share, permissionTarget, actions);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean[] areActionsPermittedOnShare(OShare share, String permissionTarget, String[] actions) throws WTException {
		String instance = String.valueOf(share.getShareId());
		String groupName = OShare.extractGroupNameFromKey(share.getKey());
		String permKey = ServiceSharePermission.buildPermissionKey(permissionTarget, groupName);
		
		UserProfile.Id targetPid = getTargetProfileId();
		boolean[] perms = new boolean[actions.length];
		for(int i=0; i<actions.length; i++) {
			perms[i] = RunContext.isPermitted(targetPid, share.getServiceId(), permKey, actions[i], instance);
		}
		return perms;
	}
	
	public boolean isShareRootPermitted(String shareId, String action) throws WTException {
		return areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ROOT, new String[]{action})[0];
	}
	
	public boolean isShareFolderPermitted(String shareId, String action) throws WTException {
		return areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_FOLDER, new String[]{action})[0];
	}
	
	public boolean isShareElementsPermitted(String shareId, String action) throws WTException {
		return areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ELEMENTS, new String[]{action})[0];
	}
	
	public SharePermsRoot getShareRootPermissions(String shareId) throws WTException {
		boolean[] bools = areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ROOT, SharePermsRoot.ACTIONS);
		return new SharePermsRoot(SharePermsRoot.ACTIONS, bools);
	}
	
	public SharePermsFolder getShareFolderPermissions(String shareId) throws WTException {
		boolean[] bools = areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_FOLDER, SharePermsFolder.ACTIONS);
		return new SharePermsFolder(SharePermsFolder.ACTIONS, bools);
	}
	
	public SharePermsElements getShareElementsPermissions(String shareId) throws WTException {
		boolean[] bools = areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ELEMENTS, SharePermsElements.ACTIONS);
		return new SharePermsElements(SharePermsElements.ACTIONS, bools);
	}
	
	public Sharing getSharing(String serviceId, String groupName, String shareId) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		String rootShareKey = OShare.buildRootKey(groupName);
		String folderShareKey = OShare.buildFolderKey(groupName);
		String rootPermissionKey = ServiceSharePermission.buildRootPermissionKey(groupName);
		String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
		String elementsPermissionKey = ServiceSharePermission.buildElementsPermissionKey(groupName);
		
		try {
			CompositeId cid = new CompositeId().parse(shareId);
			int level = cid.getSize()-1;
			String rootId = cid.getToken(0);
			
			con = WT.getCoreConnection();
			
			// Retrieves the root share
			OShare rootShare = null;
			if(rootId.equals("0")) {
				String puid = usrm.userToUid(getTargetProfileId());
				rootShare = shadao.selectByUserServiceKeyInstance(con, puid, serviceId, rootShareKey, OShare.INSTANCE_ROOT);
			} else {
				rootShare = shadao.selectById(con, Integer.valueOf(rootId));
			}
			
			Sharing outshare = new Sharing();
			outshare.setId(shareId);
			outshare.setLevel(level);
			
			if(rootShare != null) { // A rootShare must be defined in order to continue...
				if(level == 0) {
					LinkedHashSet<String> roleUids = new LinkedHashSet<>();
					roleUids.addAll(listRoles(serviceId, rootPermissionKey, rootShare.getShareId().toString()));
					
					OShare folderShare = shadao.selectByUserServiceKeyInstance(con, rootShare.getUserUid(), serviceId, folderShareKey, OShare.INSTANCE_WILDCARD);
					if(folderShare != null) roleUids.addAll(listRoles(serviceId, folderPermissionKey, folderShare.getShareId().toString()));

					for(String roleUid : roleUids) {
						// Root...
						SharePermsRoot rperms = new SharePermsRoot();
						for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, rootPermissionKey, rootShare.getShareId().toString())) {
							rperms.parse(perm.getAction());
						}
						// Folder...
						SharePermsFolder fperms = new SharePermsFolder();
						if(folderShare != null) {
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, folderPermissionKey, folderShare.getShareId().toString())) {
								fperms.parse(perm.getAction());
							}
						}
							
						// Elements...
						SharePermsElements eperms = new SharePermsElements();
						if(folderShare != null) {
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, elementsPermissionKey, folderShare.getShareId().toString())) {
								eperms.parse(perm.getAction());
							}
						}
						outshare.getRights().add(new Sharing.RoleRights(roleUid, rperms, fperms, eperms));
					}


				} else if(level == 1) {
					String folderId = cid.getToken(1);
					OShare folderShare = shadao.selectByUserServiceKeyInstance(con, rootShare.getUserUid(), serviceId, folderShareKey, folderId);

					if(folderShare != null) {
						List<String> roleUids = listRoles(serviceId, folderPermissionKey, folderShare.getShareId().toString());
						for(String roleUid : roleUids) {
							// Folder...
							SharePermsFolder fperms = new SharePermsFolder();
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, folderPermissionKey, folderShare.getShareId().toString())) {
								fperms.parse(perm.getAction());
							}
							// Elements...
							SharePermsElements eperms = new SharePermsElements();
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, elementsPermissionKey, folderShare.getShareId().toString())) {
								eperms.parse(perm.getAction());
							}
							outshare.getRights().add(new Sharing.RoleRights(roleUid, null, fperms, eperms));
						}
					}
				}
			}
			
			return outshare;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateSharing(String serviceId, String groupName, Sharing sharing) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			String puid = usrm.userToUid(getTargetProfileId());
			CompositeId cid = new CompositeId().parse(sharing.getId());
			int level = cid.getSize()-1;
			String rootId = cid.getToken(0);
			
			String rootKey = OShare.buildRootKey(groupName);
			String folderKey = OShare.buildFolderKey(groupName);
			String rootPermRes = ServiceSharePermission.buildRootPermissionKey(groupName);
			String folderPermRes = ServiceSharePermission.buildFolderPermissionKey(groupName);
			String elementsPermRes = ServiceSharePermission.buildElementsPermissionKey(groupName);
			
			con = WT.getCoreConnection();
			
			// Retrieves the root share
			OShare rootShare = null;
			if(rootId.equals("0")) {
				rootShare = shadao.selectByUserServiceKeyInstance(con, puid, serviceId, rootKey, OShare.INSTANCE_ROOT);
			} else {
				rootShare = shadao.selectById(con, Integer.valueOf(rootId));
			}
			if(rootShare == null) rootShare = addRootShare(con, puid, serviceId, rootKey);
			
			if(level == 0) {
				OShare folderShare = shadao.selectByUserServiceKeyInstance(con, rootShare.getUserUid(), serviceId, folderKey, OShare.INSTANCE_WILDCARD);
				
				if(!sharing.getRights().isEmpty()) {
					removeRootSharePermissions(con, rootShare.getShareId().toString(), serviceId, groupName);
					if(folderShare == null) {
						folderShare = addFolderShare(con, rootShare.getUserUid(), serviceId, folderKey, OShare.INSTANCE_WILDCARD);
					} else { // Folder isn't new (and we have some rights)...
						// Removes all rights belonging to this folder share
						removeFolderSharePermissions(con, folderShare.getShareId().toString(), serviceId, groupName);
					}
					
					// Adds permissions according to specified rights...
					for(Sharing.RoleRights rr : sharing.getRights()) {
						if(rr.rootManage) addSharePermission(con, rr.roleUid, serviceId, rootPermRes, "MANAGE", rootShare.getShareId().toString());
						if(rr.folderRead) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "READ", folderShare.getShareId().toString());
						if(rr.folderUpdate) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.folderDelete) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "DELETE", folderShare.getShareId().toString());
						if(rr.elementsCreate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "CREATE", folderShare.getShareId().toString());
						if(rr.elementsUpdate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.elementsDelete) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "DELETE", folderShare.getShareId().toString());
					}
					
				} else {
					// If defines, removes folder share and its rights
					if(folderShare != null) removeFolderShare(con, folderShare.getShareId().toString(), serviceId, groupName);
				}
				
			} else if(level == 1) {
				String folderId = cid.getToken(1);
				OShare folderShare = shadao.selectByUserServiceKeyInstance(con, rootShare.getUserUid(), serviceId, folderKey, folderId);
				
				if(!sharing.getRights().isEmpty()) {
					if(folderShare == null) {
						folderShare = addFolderShare(con, rootShare.getUserUid(), serviceId, folderKey, folderId);
					} else { // Folder isn't new (and we have some rights)...
						// Removes all rights belonging to this folder share
						removeFolderSharePermissions(con, folderShare.getShareId().toString(), serviceId, groupName);
					}

					// Adds permissions according to specified rights...
					for(Sharing.RoleRights rr : sharing.getRights()) {
						//if(rr.rootManage) addSharePermission(con, rr.roleUid, serviceId, rootPermRes, "MANAGE", rootShare.getShareId().toString());
						if(rr.folderRead) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "READ", folderShare.getShareId().toString());
						if(rr.folderUpdate) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.folderDelete) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "DELETE", folderShare.getShareId().toString());
						if(rr.elementsCreate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "CREATE", folderShare.getShareId().toString());
						if(rr.elementsUpdate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.elementsDelete) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "DELETE", folderShare.getShareId().toString());
					}

				} else { // No rights specified for any role...
					// If defines, removes folder share and its rights
					if(folderShare != null) removeFolderShare(con, folderShare.getShareId().toString(), serviceId, groupName);
				}
			}
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "Unable to update share rights");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ORolePermission addSharePermission(Connection con, String roleUid, String service, String key, String action, String instance) throws DAOException {
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		ORolePermission rp = new ORolePermission();
		rp.setRoleUid(roleUid);
		rp.setServiceId(service);
		rp.setKey(key);
		rp.setAction(action);
		rp.setInstance(instance);
		rp.setRolePermissionId(rpedao.getSequence(con).intValue());
		rpedao.insert(con, rp);
		return rp;
	}
	
	private void removeRootSharePermissions(Connection con, String shareId, String serviceId, String groupName) throws DAOException {
		String rootPermissionKey = ServiceSharePermission.buildRootPermissionKey(groupName);
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		rpedao.deleteByServiceKeyInstance(con, serviceId, rootPermissionKey, shareId);
	}
	
	private void removeFolderShare(Connection con, String shareId, String serviceId, String groupName) throws DAOException {
		ShareDAO shadao = ShareDAO.getInstance();
		
		// 1 - Deletes main folder share record
		shadao.deleteById(con, Integer.valueOf(shareId));
		
		// 2 - Deletes any permission related to folder share
		removeFolderSharePermissions(con, shareId, serviceId, groupName);
	}
	
	private void removeFolderSharePermissions(Connection con, String shareId, String serviceId, String groupName) throws DAOException {
		String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
		String elementsPermissionKey = ServiceSharePermission.buildElementsPermissionKey(groupName);
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		rpedao.deleteByServiceKeyInstance(con, serviceId, folderPermissionKey, shareId);
		rpedao.deleteByServiceKeyInstance(con, serviceId, elementsPermissionKey, shareId);
	}
	
	private OShare addRootShare(Connection con, String userUid, String serviceId, String shareKey) throws DAOException {
		ShareDAO dao = ShareDAO.getInstance();
		OShare share = new OShare();
		share.setUserUid(userUid);
		share.setServiceId(serviceId);
		share.setKey(shareKey);
		share.setInstance(OShare.INSTANCE_ROOT);
		share.setShareId(dao.getSequence(con).intValue());
		dao.insert(con, share);
		return share;
	}
	
	private OShare addFolderShare(Connection con, String userUid, String serviceId, String shareKey, String instance) throws DAOException {
		ShareDAO dao = ShareDAO.getInstance();
		OShare share = new OShare();
		share.setUserUid(userUid);
		share.setServiceId(serviceId);
		share.setKey(shareKey);
		share.setInstance(instance);
		share.setShareId(dao.getSequence(con).intValue());
		dao.insert(con, share);
		return share;
	}
			
	public List<OShare> listShareByOwner(UserProfile.Id pid, String serviceId, String shareKey) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO dao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			String uuid = usrm.userToUid(pid);
			return dao.selectByUserServiceKey(con, uuid, serviceId, shareKey);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> listRoles(String serviceId, String permissionKey, String instance) throws WTException {
		RolePermissionDAO dao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.selectRolesByServiceKeyInstance(con, serviceId, permissionKey, instance);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<UserOptionsServiceData> listUserOptionServices() {
		ServiceManager svcm = wta.getServiceManager();
		ArrayList<UserOptionsServiceData> items = new ArrayList<>();
		UserOptionsServiceData uos = null;
		//TODO: se admin allora targetprofileSenza problemi altrimenti controllo che corrisponda
		UserProfile.Id pid = getTargetProfileId();
		List<String> ids = svcm.listUserOptionServices();
		for(String id : ids) {
			// Checks user rights on service...
			if(RunContext.isPermitted(SERVICE_ID, "SERVICE", "ACCESS", id)) {
				uos = new UserOptionsServiceData(svcm.getManifest(id));
				uos.name = wta.lookupResource(id, Locale.ITALIAN, CoreLocaleKey.SERVICE_NAME);
				items.add(uos);
			}
		}
		return items;
	}
	
	public OSnoozedReminder snoozeReminder(ReminderInApp reminder, DateTime remindOn) throws WTException {
		SnoozedReminderDAO dao = SnoozedReminderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OSnoozedReminder item = new OSnoozedReminder();
			item.setDomainId(reminder.getProfileId().getDomain());
			item.setUserId(reminder.getProfileId().getUserId());
			item.setServiceId(reminder.getServiceId());
			item.setType(reminder.getType());
			item.setInstanceId(reminder.getInstanceId());
			item.setRemindOn(remindOn);
			item.setTitle(reminder.getTitle());
			item.setDate(reminder.getDate());
			item.setTimezone(reminder.getTimezone());
			
			item.setSnoozedReminderId(dao.getSequence(con).intValue());
			dao.insert(con, item);
			return item;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB Error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OSnoozedReminder> listExpiredSnoozedReminders(DateTime greaterInstant) throws WTException {
		SnoozedReminderDAO dao = SnoozedReminderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			con.setAutoCommit(false);
			List<OSnoozedReminder> items = dao.selectExpiredForUpdateByInstant(con, greaterInstant);
			for(OSnoozedReminder item : items) {
				dao.delete(con, item.getSnoozedReminderId());
			}
			DbUtils.commitQuietly(con);
			return items;
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB Error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OServiceStoreEntry> listServiceStoreEntriesByQuery(String serviceId, String context, String query, int max) {
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			if(query == null) {
				return sedao.selectKeyValueByLimit(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, max);
			} else {
				return sedao.selectKeyValueByLikeKeyLimit(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, "%"+query+"%", max);
			}
		
		} catch(SQLException | DAOException ex) {
			logger.error("Error querying servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, query, ex);
			return new ArrayList<>();
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addServiceStoreEntry(String serviceId, String context, String key, String value) {
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			OServiceStoreEntry entry = sedao.select(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, key);
			
			DateTime now = DateTime.now(DateTimeZone.UTC);
			if(entry != null) {
				entry.setValue(value);
				entry.setFrequency(entry.getFrequency()+1);
				entry.setLastUpdate(now);
				sedao.update(con, entry);
			} else {
				entry = new OServiceStoreEntry();
				entry.setDomainId(targetPid.getDomainId());
				entry.setUserId(targetPid.getUserId());
				entry.setServiceId(serviceId);
				entry.setContext(context);
				entry.setKey(StringUtils.upperCase(key));
				entry.setValue(value);
				entry.setFrequency(1);
				entry.setLastUpdate(now);
				sedao.insert(con, entry);
			}
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error adding servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry() {
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.deleteByDomainUser(con, targetPid.getDomainId(), targetPid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting servicestore entry [{}]", targetPid, ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(String serviceId) {
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.deleteByDomainUserService(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId);
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting servicestore entry [{}, {}]", targetPid, serviceId, ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(String serviceId, String context, String key) {
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.delete(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, key);
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, key, ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateAutosaveData(String serviceId, String context, String key, String value) {
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			OAutosave data = asdao.select(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, key);
			
			if(data != null) {
				data.setValue(value);
				asdao.update(con, data);
			} else {
				data = new OAutosave();
				data.setDomainId(targetPid.getDomainId());
				data.setUserId(targetPid.getUserId());
				data.setServiceId(serviceId);
				data.setContext(context);
				data.setKey(StringUtils.upperCase(key));
				data.setValue(value);
				asdao.insert(con, data);
			}
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error adding servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteAutosaveData(String serviceId, String context, String key) {
		UserProfile.Id targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			asdao.delete(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, key);
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> listInternetRecipientsSources() throws WTException {
		return new ArrayList<>(getProfileRecipientsProviders().keySet());
	}
	
	public List<InternetRecipient> listInternetRecipients(String queryText, int max) throws WTException {
		List<String> list=listInternetRecipientsSources();
		list.add(0,"");
		return listInternetRecipients(list, queryText, max);
	}
	
	public List<InternetRecipient> listInternetRecipients(List<String> sourceIds, String queryText, int max) throws WTException {
		ArrayList<InternetRecipient> items = new ArrayList<>();
		
		int remaining = max;
		for(String soid : sourceIds) {
			final List<InternetRecipient> recipients;
			//zero length source id means auto learnt
			if (soid.length() == 0) {
				List<OServiceStoreEntry> entries = listServiceStoreEntriesByQuery(SERVICE_ID, "recipients", queryText, remaining);
				recipients = new ArrayList<>();
				for(OServiceStoreEntry entry: entries) {
					InternetAddress ia = MailUtils.buildInternetAddress(entry.getValue());
					if (ia != null) {
						recipients.add(new InternetRecipient("auto", lookupResource(locale, "word.automatic"), "auto", ia.getPersonal(), ia.getAddress()));
					}
				}
				
			} else {
				RecipientsProviderBase provider = getProfileRecipientsProviders().get(soid);
				if(provider == null) continue;
				
				recipients = provider.getRecipients(queryText, remaining);
				if(recipients == null) continue;
			}
			
			for(InternetRecipient recipient : recipients) {
				remaining--;
				if(remaining < 0) break; 
				recipient.setSource(soid); // Force composed id
				items.add(recipient);
			}
			if(remaining <= 0) break;
			/*
			if(recipients != null) {
				if(recipients.size() > remaining) {
					items.addAll(recipients.subList(0, remaining-1));
					break;
				} else {
					remaining -=  recipients.size();
					items.addAll(recipients);
				}
			}
			*/
		}
		
		return items;
	}
	
	public List<InternetRecipient> expandToInternetRecipients(String virtualRecipient) throws WTException {
		ArrayList<InternetRecipient> items = new ArrayList<>();
		VirtualAddress virtAdd = new VirtualAddress(virtualRecipient);
		
		for (String srcId : listInternetRecipientsSources()) {
			RecipientsProviderBase provider = getProfileRecipientsProviders().get(srcId);
			if (provider == null) continue;
			
			if (!StringUtils.isBlank(virtAdd.getDomain()) && !StringUtils.startsWith(srcId, virtAdd.getDomain())) {
				continue;
			}
			final List<InternetRecipient> recipients = provider.expandToRecipients(virtAdd.getLocal());
			if (recipients == null) continue;
			for (InternetRecipient recipient : recipients) {
				recipient.setSource(srcId);
				items.add(recipient);
			}
		}
		return items;
	}
	
	public void learnInternetRecipient(String email) {
		addServiceStoreEntry(SERVICE_ID, "recipients", email, email);
	}
	
	/*
	public List<InternetRecipient> listDomainInternetRecipients(List<String> sourceIds, String queryText, int max) throws WTException {
		
		
	}
	*/
	
	/*
	public List<InternetRecipient> listInternetRecipients(List<String> providerIds, boolean incGlobal, String incDomainId, UserProfile.Id incProfileId, String text) throws WTException {
		ArrayList<InternetRecipient> items = new ArrayList<>();
		
		for(String proid : providerIds) {
			for(RecipientsProviderBase provider : getProfileRecipientsProviders()) {
				if(provider != null) {
					if(incGlobal && (provider instanceof IGlobalRecipientsProvider)) {
						try {
							items.addAll(((IGlobalRecipientsProvider)provider).getRecipients(text));
						} catch(Throwable t) {
							logger.error("Error querying provider", t);
						}	
					}
					if(!StringUtils.isBlank(incDomainId) && (provider instanceof IDomainRecipientsProvider)) {
						try {
							items.addAll(((IDomainRecipientsProvider)provider).getRecipients(incDomainId, text));
						} catch(Throwable t) {
							logger.error("Error querying provider", t);
						}
					}
					if((incProfileId != null) && (provider instanceof IProfileRecipientsProvider)) {
						try {
							items.addAll(((IProfileRecipientsProvider)provider).getRecipients(incProfileId, text));
						} catch(Throwable t) {
							logger.error("Error querying provider", t);
						}
					}

				}
			}
		}
		return items;
	}
	*/
	
	public List<SyncDevice> listZPushDevices() throws WTException {
		try {
			ZPushManager zpush = createZPushManager();
			
			boolean sysadmin = RunContext.isSysAdmin();
			String internetId = (sysadmin) ? null : getInternetUserId(getTargetProfileId());

			ArrayList<SyncDevice> devices = new ArrayList<>();
			List<ZPushManager.LastsyncRecord> recs = zpush.listDevices();
			for(ZPushManager.LastsyncRecord rec : recs) {
				if(sysadmin || StringUtils.equalsIgnoreCase(rec.syncronizedUser, internetId)) {
					devices.add(new SyncDevice(rec.device, rec.syncronizedUser, rec.lastSyncTime));
				}
			}

			return devices;
		} catch(Exception ex) {
			throw new WTException(ex);
		}
	}
	
	public void deleteZPushDevice(String device, String user) throws WTException {
		try {
			ZPushManager zpush = createZPushManager();
			zpush.removeUserDevice(user, device);
		} catch(Exception ex) {
			throw new WTException(ex);
		}
	}
	
	public String getZPushDetailedInfo(String device, String user, String lineSep) throws WTException {
		try {
			ZPushManager zpush = createZPushManager();
			return zpush.getDetailedInfo(device, user, lineSep);
		} catch(Exception ex) {
			throw new WTException(ex);
		}	
	}
	
	private ZPushManager createZPushManager() throws WTException {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
		try {
			URI uri = new URI(css.getDevicesSyncShellUri());
			return new ZPushManager(css.getPhpPath(), css.getZPushPath(), uri);
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid URI");
		}
	}
}
