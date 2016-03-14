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
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.webtop.core.bol.ActivityGrid;
import com.sonicle.webtop.core.bol.CausalGrid;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OSnoozedReminder;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsReminderInApp;
import com.sonicle.webtop.core.bol.model.AuthResource;
import com.sonicle.webtop.core.bol.model.AuthResourceShare;
import com.sonicle.webtop.core.bol.model.SharePermsElements;
import com.sonicle.webtop.core.bol.model.SharePermsFolder;
import com.sonicle.webtop.core.bol.model.IncomingShareRoot;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.bol.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DomainDAO;
import com.sonicle.webtop.core.dal.SnoozedReminderDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.ServiceStoreEntryDAO;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.userinfo.UserInfoProviderBase;
import com.sonicle.webtop.core.util.ZPushManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
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
public class CoreManager extends BaseManager {
	private WebTopApp wta = null;
	
	public CoreManager(RunContext context, WebTopApp wta) {
		super(context);
		this.wta = wta;
	}
	
	public CoreManager(RunContext context, UserProfile.Id targetProfileId, WebTopApp wta) {
		super(context, targetProfileId);
		this.wta = wta;
	}
	
	@Override
	protected Locale findLocale() {
		try {
			return getUserData(getTargetProfileId()).getLocale();
		} catch(Exception ex) {
			return Locale.ENGLISH;
		}
	}
	
	public ServiceManager getServiceManager() {
		ensureService(CoreManifest.ID, "getServiceManager");
		return wta.getServiceManager();
	}
	
	public OTPManager getOTPManager() {
		ensureService(CoreManifest.ID, "getOTPManager");
		return wta.getOTPManager();
	}
	
	public UserInfoProviderBase getUserInfoProvider() throws WTException {
		return wta.getUserManager().getUserInfoProvider();
	}
	
	public boolean isUserInfoProviderWritable() {
		return wta.getUserManager().isUserInfoProviderWritable();
	}
	
	public List<ODomain> listDomains(boolean enabledOnly) throws Exception {
		Connection con = null;
		try {
			con = WT.getCoreConnection();
			DomainDAO dao = DomainDAO.getInstance();
			return (enabledOnly) ? dao.selectEnabled(con) : dao.selectAll(con);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ODomain getDomain(String domainId) throws Exception {
		UserManager usrm = wta.getUserManager();
		
		try {
			return usrm.getDomain(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to get domain [{0}]", domainId);
		}
	}
	
	public UserProfile.Id userUidToProfileId(String userUid) {
		return wta.getUserManager().uidToUser(userUid);
	}
	
	public UserProfile.Data getUserData(UserProfile.Id pid) throws WTException {
		return wta.getUserManager().userData(pid);
	}
	
	public List<OUser> listUsers(boolean enabledOnly) {
		Connection con = null;
		//TODO: gestire gli utenti abilitati
		try {
			con = WT.getCoreConnection();
			UserDAO dao = UserDAO.getInstance();
			return dao.selectAll(con);
			
		} catch(SQLException | DAOException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OUser> listUsers(String domainId, boolean enabledOnly) {
		Connection con = null;
		//TODO: gestire gli utenti abilitati
		try {
			con = WT.getCoreConnection();
			UserDAO dao = UserDAO.getInstance();
			return dao.selectByDomain(con, domainId);
			
		} catch(SQLException | DAOException ex) {
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<Role> listRoles(String domainId, boolean fromUsers, boolean fromGroups) throws WTException {
		AuthManager authm = wta.getAuthManager();
		
		try {
			return authm.listRoles(domainId, fromUsers, fromGroups);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list roles [{0}, {1}, {2}]", domainId, fromUsers, fromGroups);
		}
	}
	
	public OUser getUser(UserProfile.Id pid) throws WTException {
		UserManager usrm = wta.getUserManager();
		
		try {
			return usrm.getUser(pid);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to get User [{0}]", pid);
		}
	}
	
	public String getInternetUserId(UserProfile.Id pid) throws WTException {
		return wta.getUserManager().getInternetUserId(pid);
	}
	
	public UserPersonalInfo getUserPersonalInfo(UserProfile.Id pid) throws WTException {
		return wta.getUserManager().userPersonalInfo(pid);
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
		UserInfoProviderBase uip = wta.getUserManager().getUserInfoProvider();
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
	
	public List<UserProfile.Id> listProfilesWithSetting(String serviceId, String key, Object value) throws WTException {
		SettingsManager setm = wta.getSettingsManager();
		return setm.listProfilesWith(serviceId, key, value);
	}
	
	public List<ActivityGrid> listLiveActivities(Collection<String> domainIds) throws WTException {
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
			WT.ensureIsPermitted(getRunProfileId(), SERVICE_ID, "ACTIVITIES", "MANAGE");
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
			WT.ensureIsPermitted(getRunProfileId(), SERVICE_ID, "ACTIVITIES", "MANAGE");
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
			WT.ensureIsPermitted(getRunProfileId(), SERVICE_ID, "ACTIVITIES", "MANAGE");
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
			WT.ensureIsPermitted(getRunProfileId(), SERVICE_ID, "CAUSALS", "MANAGE");
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
			WT.ensureIsPermitted(getRunProfileId(), SERVICE_ID, "CAUSALS", "MANAGE");
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
			WT.ensureIsPermitted(getRunProfileId(), SERVICE_ID, "CAUSALS", "MANAGE");
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
		ensureOwnerUser();
		OTPManager otp = getOTPManager();
		return otp.configureEmail(getTargetProfileId(), emailAddress);
	}
	
	public OTPManager.GoogleAuthConfig otpConfigureUsingGoogleAuth(int qrCodeSize) throws WTException {
		//TODO: controllo accessi
		ensureOwnerUser();
		OTPManager otp = getOTPManager();
		return otp.configureGoogleAuth(getTargetProfileId(), qrCodeSize);
	}
	
	public boolean otpActivate(OTPManager.Config config, int code) throws WTException {
		//TODO: controllo accessi
		ensureOwnerUser();
		OTPManager otp = getOTPManager();
		return otp.activate(getTargetProfileId(), config, code);
	}
	
	public void otpDeactivate() throws WTException {
		//TODO: controllo accessi
		ensureOwnerUser();
		OTPManager otp = getOTPManager();
		otp.deactivateOTP(getTargetProfileId());
	}
	
	public OTPManager.Config otpPrepareVerifyCode() throws WTException {
		ensureService(CoreManifest.ID, "otpPrepareVerifyCode");
		OTPManager otp = getOTPManager();
		return otp.prepareCheckCode(getTargetProfileId());
	}
	
	public boolean otpVerifyCode(OTPManager.Config params, int code) throws WTException {
		ensureService(CoreManifest.ID, "otpVerifyCode");
		OTPManager otp = getOTPManager();
		return otp.checkCode(getTargetProfileId(), params, code);
	}
	
	public List<IncomingShareRoot> listIncomingShareRoots(UserProfile.Id pid, String serviceId, String resource) throws WTException {
		String rootShareRes = OShare.buildRootResource(resource);
		String folderShareRes = OShare.buildFolderResource(resource);
		String rootPermRes = AuthResourceShare.buildRootPermissionResource(resource);
		String folderPermRes = AuthResourceShare.buildFolderPermissionResource(resource);
		String elementsPermRes = AuthResourceShare.buildElementsPermissionResource(resource);
		UserManager usrm = wta.getUserManager();
		AuthManager authm = wta.getAuthManager();
		Connection con = null;
		
		try {
			
			String puid = usrm.userToUid(pid);
			List<String> roleUids = authm.getRolesAsString(pid, true, true);
			
			con = WT.getCoreConnection();
			ShareDAO shadao = ShareDAO.getInstance();
			UserDAO usedao = UserDAO.getInstance();
			
			// In order to find incoming root, we need to pass throught folders
			// that have at least a permission, getting incoming uids.
			// We look into permission returning each share instance that have 
			// "*_FOLDER" as resource and satisfies a set of roles. Then we can
			// get a list of unique uids (from shares table) that owns the share.
			List<String> permRes = Arrays.asList(rootPermRes, folderPermRes, elementsPermRes);
			List<String> originUids = shadao.viewOriginByRoleServiceResource(con, roleUids, serviceId, folderShareRes, permRes);
			ArrayList<IncomingShareRoot> roots = new ArrayList<>();
			for(String uid : originUids) {
				if(uid.equals(puid)) continue; // Skip self role
				
				// Foreach incoming uid we have to find the root share and then
				// test if READ right is allowed
				OShare root = shadao.selectByUserServiceResourceInstance(con, uid, serviceId, rootShareRes, OShare.ROOT_INSTANCE);
				if(root == null) continue;
				
				OUser user = usedao.selectByUid(con, uid);
				if(user == null) continue;
				roots.add(new IncomingShareRoot(root.getShareId().toString(), usrm.uidToUser(root.getUserUid()), user.getDisplayName()));
			}
			return roots;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "Unable to list share roots for {0}", pid.toString());
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OShare> listIncomingShareFolders(UserProfile.Id pid, String rootShareId, String serviceId, String resource) throws WTException {
		Connection con = null;
		String folderShareRes = OShare.buildFolderResource(resource);
		String folderPermRes = AuthResourceShare.buildFolderPermissionResource(resource);
		
		try {
			AuthManager auth = wta.getAuthManager();
			
			con = WT.getCoreConnection();
			ShareDAO shadao = ShareDAO.getInstance();
			
			OShare rootShare = shadao.selectById(con, Integer.valueOf(rootShareId));
			if(rootShare == null) throw new WTException("Unable to find root share [{0}]", rootShareId);
			
			ArrayList<OShare> folders = new ArrayList<>();
			List<OShare> shares = shadao.selectByUserServiceResource(con, rootShare.getUserUid(), serviceId, folderShareRes);
			for(OShare share : shares) {
				if(auth.isPermitted(pid, AuthResource.namespacedName(serviceId, folderPermRes), AuthResource.ACTION_READ, share.getShareId().toString())) {
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
	
	public boolean[] isSharePermitted(UserProfile.Id pid, String serviceId, String permResource, String[] actions, String shareId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ShareDAO shadao = ShareDAO.getInstance();
			OShare share = shadao.selectById(con, Integer.valueOf(shareId));
			if(share == null) throw new WTException("Unable to find share [{0}]", shareId);
			return isSharePermitted(pid, share, permResource, actions);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean[] isSharePermitted(UserProfile.Id pid, OShare share, String permResource, String[] actions) throws WTException {
		AuthManager auth = wta.getAuthManager();
		String authRes = AuthResource.namespacedName(share.getServiceId(), permResource);
		String instance = String.valueOf(share.getShareId());
		
		boolean[] perms = new boolean[actions.length];
		for(int i=0; i<actions.length; i++) {
			perms[i] = auth.isPermitted(pid, authRes, actions[i], instance);
		}
		return perms;
	}
	
	public boolean isShareRootPermitted(UserProfile.Id pid, String serviceId, String resource, String action, String shareId) throws WTException {
		String permRes = AuthResourceShare.buildRootPermissionResource(resource);
		return isSharePermitted(pid, serviceId, permRes, new String[]{action}, shareId)[0];
	}
	
	public boolean isShareFolderPermitted(UserProfile.Id pid, String serviceId, String resource, String action, String shareId) throws WTException {
		String permRes = AuthResourceShare.buildFolderPermissionResource(resource);
		return isSharePermitted(pid, serviceId, permRes, new String[]{action}, shareId)[0];
	}
	
	public boolean isShareElementsPermitted(UserProfile.Id pid, String serviceId, String resource, String action, String shareId) throws WTException {;
		String permRes = AuthResourceShare.buildElementsPermissionResource(resource);
		return isSharePermitted(pid, serviceId, permRes, new String[]{action}, shareId)[0];
	}
	
	public SharePermsRoot getShareRootPermissions(UserProfile.Id pid, String serviceId, String resource, String shareId) throws WTException {
		String permRes = AuthResourceShare.buildRootPermissionResource(resource);
		boolean[] bools = isSharePermitted(pid, serviceId, permRes, SharePermsRoot.ACTIONS, shareId);
		return new SharePermsRoot(SharePermsRoot.ACTIONS, bools);
	}
	
	public SharePermsFolder getShareFolderPermissions(UserProfile.Id pid, String serviceId, String resource, String shareId) throws WTException {
		String permRes = AuthResourceShare.buildFolderPermissionResource(resource);
		boolean[] bools = isSharePermitted(pid, serviceId, permRes, SharePermsFolder.ACTIONS, shareId);
		return new SharePermsFolder(SharePermsFolder.ACTIONS, bools);
	}
	
	public SharePermsElements getShareElementsPermissions(UserProfile.Id pid, String serviceId, String resource, String shareId) throws WTException {
		String permRes = AuthResourceShare.buildElementsPermissionResource(resource);
		boolean[] bools = isSharePermitted(pid, serviceId, permRes, SharePermsElements.ACTIONS, shareId);
		return new SharePermsElements(SharePermsElements.ACTIONS, bools);
	}
	
	public Sharing getSharing(UserProfile.Id pid, String serviceId, String resource, String shareId) throws WTException {
		String rootShareRes = OShare.buildRootResource(resource);
		String rootPermRes = AuthResourceShare.buildRootPermissionResource(resource);
		String folderShareRes = OShare.buildFolderResource(resource);
		String folderPermRes = AuthResourceShare.buildFolderPermissionResource(resource);
		String elementsPermRes = AuthResourceShare.buildElementsPermissionResource(resource);
		UserManager usrm = wta.getUserManager();
		AuthManager authm = wta.getAuthManager();
		ShareDAO shadao = ShareDAO.getInstance();
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			CompositeId cid = new CompositeId().parse(shareId);
			int level = cid.getHowManyTokens()-1;
			String rootId = cid.getToken(0);
			
			con = WT.getCoreConnection();
			
			// Retrieves the root share
			OShare rootShare = null;
			if(rootId.equals("0")) {
				String puid = usrm.userToUid(pid);
				rootShare = shadao.selectByUserServiceResourceInstance(con, puid, serviceId, rootShareRes, OShare.ROOT_INSTANCE);
			} else {
				rootShare = shadao.selectById(con, Integer.valueOf(rootId));
			}
			
			Sharing outshare = new Sharing();
			outshare.setId(shareId);
			outshare.setLevel(level);
			
			if(rootShare != null) { // A rootShare must be defined in order to continue...
				if(level == 0) {
					LinkedHashSet<String> roleUids = new LinkedHashSet<>();
					roleUids.addAll(listRoles(serviceId, rootPermRes, rootShare.getShareId().toString()));
					
					OShare folderShare = shadao.selectByUserServiceResourceInstance(con, rootShare.getUserUid(), serviceId, folderShareRes, OShare.INSTANCE_WILDCARD);
					if(folderShare != null) roleUids.addAll(listRoles(serviceId, folderPermRes, folderShare.getShareId().toString()));

					for(String roleUid : roleUids) {
						// Root...
						SharePermsRoot rperms = new SharePermsRoot();
						for(ORolePermission perm : rpedao.selectByRoleServiceResourceInstance(con, roleUid, serviceId, rootPermRes, rootShare.getShareId().toString())) {
							rperms.parse(perm.getAction());
						}
						// Folder...
						SharePermsFolder fperms = new SharePermsFolder();
						for(ORolePermission perm : rpedao.selectByRoleServiceResourceInstance(con, roleUid, serviceId, folderPermRes, folderShare.getShareId().toString())) {
							fperms.parse(perm.getAction());
						}
						// Elements...
						SharePermsElements eperms = new SharePermsElements();
						for(ORolePermission perm : rpedao.selectByRoleServiceResourceInstance(con, roleUid, serviceId, elementsPermRes, folderShare.getShareId().toString())) {
							eperms.parse(perm.getAction());
						}
						outshare.getRights().add(new Sharing.RoleRights(roleUid, rperms, fperms, eperms));
					}


				} else if(level == 1) {
					String folderId = cid.getToken(1);
					OShare folderShare = shadao.selectByUserServiceResourceInstance(con, rootShare.getUserUid(), serviceId, folderShareRes, folderId);

					if(folderShare != null) {
						List<String> roleUids = listRoles(serviceId, folderPermRes, folderShare.getShareId().toString());
						for(String roleUid : roleUids) {
							// Folder...
							SharePermsFolder fperms = new SharePermsFolder();
							for(ORolePermission perm : rpedao.selectByRoleServiceResourceInstance(con, roleUid, serviceId, folderPermRes, folderShare.getShareId().toString())) {
								fperms.parse(perm.getAction());
							}
							// Elements...
							SharePermsElements eperms = new SharePermsElements();
							for(ORolePermission perm : rpedao.selectByRoleServiceResourceInstance(con, roleUid, serviceId, elementsPermRes, folderShare.getShareId().toString())) {
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
	
	public void updateSharing(UserProfile.Id targetPid, String serviceId, String resource, Sharing sharing) throws WTException {
		String rootShareRes = OShare.buildRootResource(resource);
		String folderShareRes = OShare.buildFolderResource(resource);
		String rootPermRes = AuthResourceShare.buildRootPermissionResource(resource);
		String folderPermRes = AuthResourceShare.buildFolderPermissionResource(resource);
		String elementsPermRes = AuthResourceShare.buildElementsPermissionResource(resource);
		UserManager usrm = wta.getUserManager();
		AuthManager authm = wta.getAuthManager();
		ShareDAO shadao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			String puid = usrm.userToUid(targetPid);
			CompositeId cid = new CompositeId().parse(sharing.getId());
			int level = cid.getHowManyTokens()-1;
			String rootId = cid.getToken(0);
			
			con = WT.getCoreConnection();
			
			// Retrieves the root share
			OShare rootShare = null;
			if(rootId.equals("0")) {
				rootShare = shadao.selectByUserServiceResourceInstance(con, puid, serviceId, rootShareRes, OShare.ROOT_INSTANCE);
			} else {
				rootShare = shadao.selectById(con, Integer.valueOf(rootId));
			}
			if(rootShare == null) rootShare = addRootShare(con, puid, serviceId, rootShareRes);
			
			if(level == 0) {
				OShare folderShare = shadao.selectByUserServiceResourceInstance(con, rootShare.getUserUid(), serviceId, folderShareRes, OShare.INSTANCE_WILDCARD);
				
				if(!sharing.getRights().isEmpty()) {
					removeRootSharePermissions(con, rootShare.getShareId().toString(), serviceId, resource);
					if(folderShare == null) {
						folderShare = addFolderShare(con, rootShare.getUserUid(), serviceId, folderShareRes, OShare.INSTANCE_WILDCARD);
					} else { // Folder isn't new (and we have some rights)...
						// Removes all rights belonging to this folder share
						removeFolderSharePermissions(con, folderShare.getShareId().toString(), serviceId, resource);
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
					if(folderShare != null) removeFolderShare(con, folderShare.getShareId().toString(), serviceId, resource);
				}
				
			} else if(level == 1) {
				String folderId = cid.getToken(1);
				OShare folderShare = shadao.selectByUserServiceResourceInstance(con, rootShare.getUserUid(), serviceId, folderShareRes, folderId);
				
				if(!sharing.getRights().isEmpty()) {
					if(folderShare == null) {
						folderShare = addFolderShare(con, rootShare.getUserUid(), serviceId, folderShareRes, folderId);
					} else { // Folder isn't new (and we have some rights)...
						// Removes all rights belonging to this folder share
						removeFolderSharePermissions(con, folderShare.getShareId().toString(), serviceId, resource);
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
					if(folderShare != null) removeFolderShare(con, folderShare.getShareId().toString(), serviceId, resource);
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
	
	private ORolePermission addSharePermission(Connection con, String roleUid, String service, String permResource, String action, String instance) throws DAOException {
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		ORolePermission rp = new ORolePermission();
		rp.setRoleUid(roleUid);
		rp.setServiceId(service);
		rp.setResource(permResource);
		rp.setAction(action);
		rp.setInstance(instance);
		rp.setRolePermissionId(rpedao.getSequence(con).intValue());
		rpedao.insert(con, rp);
		return rp;
	}
	
	private void removeRootSharePermissions(Connection con, String shareId, String serviceId, String resource) throws DAOException {
		String rootPermRes = AuthResourceShare.buildRootPermissionResource(resource);
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		rpedao.deleteByServiceResourceInstance(con, serviceId, rootPermRes, shareId);
	}
	
	private void removeFolderShare(Connection con, String shareId, String serviceId, String resource) throws DAOException {
		ShareDAO shadao = ShareDAO.getInstance();
		
		// 1 - Deletes main folder share record
		shadao.deleteById(con, Integer.valueOf(shareId));
		
		// 2 - Deletes any permission related to folder share
		removeFolderSharePermissions(con, shareId, serviceId, resource);
	}
	
	private void removeFolderSharePermissions(Connection con, String shareId, String serviceId, String resource) throws DAOException {
		String folderPermRes = AuthResourceShare.buildFolderPermissionResource(resource);
		String elementsPermRes = AuthResourceShare.buildElementsPermissionResource(resource);
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		rpedao.deleteByServiceResourceInstance(con, serviceId, folderPermRes, shareId);
		rpedao.deleteByServiceResourceInstance(con, serviceId, elementsPermRes, shareId);
	}
	
	private OShare addRootShare(Connection con, String userUid, String serviceId, String shareResource) throws DAOException {
		ShareDAO dao = ShareDAO.getInstance();
		OShare share = new OShare();
		share.setUserUid(userUid);
		share.setServiceId(serviceId);
		share.setResource(shareResource);
		share.setInstance(OShare.ROOT_INSTANCE);
		share.setShareId(dao.getSequence(con).intValue());
		dao.insert(con, share);
		return share;
	}
	
	private OShare addFolderShare(Connection con, String userUid, String serviceId, String shareResource, String instance) throws DAOException {
		ShareDAO dao = ShareDAO.getInstance();
		OShare share = new OShare();
		share.setUserUid(userUid);
		share.setServiceId(serviceId);
		share.setResource(shareResource);
		share.setInstance(instance);
		share.setShareId(dao.getSequence(con).intValue());
		dao.insert(con, share);
		return share;
	}
			
	public List<OShare> listShareByOwner(UserProfile.Id pid, String serviceId, String shareResource) throws WTException {
		UserManager usrm = wta.getUserManager();
		ShareDAO dao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			String uuid = usrm.userToUid(pid);
			return dao.selectByUserServiceResource(con, uuid, serviceId, shareResource);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> listRoles(String serviceId, String permResource, String instance) throws WTException {
		RolePermissionDAO dao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.selectRolesByServiceResourceInstance(con, serviceId, permResource, instance);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
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
			if(WT.isPermitted(pid, CoreManifest.ID, "SERVICE", "ACCESS", id)) {
				items.add(id);
			}
		}
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
			if(WT.isPermitted(pid, CoreManifest.ID, "SERVICE", "ACCESS", id)) {
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
	
	public OSnoozedReminder snoozeReminder(UserProfile.Id profileId, JsReminderInApp reminder, DateTime remindOn) throws WTException {
		SnoozedReminderDAO dao = SnoozedReminderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OSnoozedReminder item = new OSnoozedReminder();
			item.setDomainId(profileId.getDomainId());
			item.setUserId(profileId.getUserId());
			item.setServiceId(reminder.serviceId);
			item.setType(reminder.type);
			item.setInstanceId(reminder.instanceId);
			item.setRemindOn(remindOn);
			item.setTitle(reminder.title);
			item.setDate(reminder.date);
			item.setTimezone(reminder.timezone);
			
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
	
	public void deleteServiceStoreEntry(UserProfile.Id pid) {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ServiceStoreEntryDAO sedao = ServiceStoreEntryDAO.getInstance();
			sedao.deleteByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			WebTopApp.logger.error("Error deleting servicestore entry [{}]", pid, ex);
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
	
	public List<SyncDevice> listZPushDevices() throws WTException {
		try {
			UserProfile.Id runPid = getRunContext().getProfileId();
			ZPushManager zpush = createZPushManager();
			
			boolean sysadmin = WT.isSysAdmin(runPid);
			String internetId = (sysadmin) ? null : getInternetUserId(runPid);

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
