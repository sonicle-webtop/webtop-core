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
package com.sonicle.webtop.core.app;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.EntryException;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory.UserEntry;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.OUserInfo;
import com.sonicle.webtop.core.bol.UserUid;
import com.sonicle.webtop.core.bol.model.DirectoryUser;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DomainDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.dal.UserInfoDAO;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.shiro.WTRealm;
import com.sonicle.webtop.core.userinfo.UserInfoProviderBase;
import com.sonicle.webtop.core.userinfo.UserInfoProviderFactory;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class UserManager {
	private static final Logger logger = WT.getLogger(UserManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	static synchronized UserManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		UserManager usem = new UserManager(wta);
		initialized = true;
		logger.info("UserManager initialized");
		return usem;
	}
	
	private WebTopApp wta = null;
	
	private final Object lock1 = new Object();
	private final HashMap<UserProfile.Id, UserUidBag> cacheUserToUidBag = new HashMap<>();
	private final HashMap<String, UserProfile.Id> cacheUserUidToUser = new HashMap<>();
	private final HashMap<String, UserProfile.Id> cacheRoleUidToUser = new HashMap<>();
	private final HashMap<UserProfile.Id, UserPersonalInfo> cacheUserToPersonalInfo = new HashMap<>();
	private final HashMap<UserProfile.Id, UserProfile.Data> cacheUserToData = new HashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private UserManager(WebTopApp wta) {
		this.wta = wta;
		initUidCache();
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		cleanupUidCache();
		cleanupUserCache();
		logger.info("UserManager destroyed");
	}
	
	public static String generateSecretKey() {
		return StringUtils.defaultIfBlank(IdentifierUtils.generateSecretKey(), "0123456789101112");
	}
	
	public UserInfoProviderBase getUserInfoProvider() throws WTException {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
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
	
	public List<ODomain> listDomains(boolean enabledOnly) throws WTException {
		DomainDAO dao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			if(enabledOnly) {
				return dao.selectEnabled(con);
			} else {
				return dao.selectAll(con);
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<ODomain> listByInternetDomain(String internetDomain) throws WTException {
		DomainDAO dao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.selectEnabledByInternetDomain(con, internetDomain);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ODomain getDomain(String domainId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return getDomain(con, domainId);
			
		} catch(SQLException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ODomain getDomain(Connection con, String domainId) throws WTException {
		DomainDAO dao = DomainDAO.getInstance();
		
		try {
			return dao.selectById(con, domainId);
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		}
	}	
	
	public List<OUser> listUsers(String domainId, boolean enabledOnly) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			if(enabledOnly) {
				return dao.selectEnabledByDomain(con, domainId);
			} else {
				return dao.selectByDomain(con, domainId);
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OUser getUser(UserProfile.Id pid) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public UserEntity getUserEntity(UserProfile.Id pid) throws WTException {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return getUserEntity(con, pid);
			
		} catch(SQLException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private UserEntity getUserEntity(Connection con, UserProfile.Id pid) throws WTException {
		AuthManager authm = wta.getAuthManager();
		UserDAO dao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		
		try {
			OUser ouser = dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			if(ouser == null) throw new WTException("User not found [{0}]", pid.toString());
			
			OUserInfo ouseri = uidao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			if(ouseri == null) throw new WTException("User info not found [{0}]", pid.toString());
			
			AuthManager.EntityPermissions perms = authm.extractPermissions(con, ouser.getRoleUid());
			UserEntity user = new UserEntity(ouser, ouseri);
			user.setPermissions(perms.others);
			user.setServicesPermissions(perms.services);
			
			return user;
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		}
	}
	
	
	
	public void addUser(UserEntity user) throws WTException {
		addUser(false, user, null);
	}
	
	public void addUser(boolean updateDirectory, UserEntity user, char[] password) throws WTException {
		AuthManager authm = wta.getAuthManager();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			ODomain domain = getDomain(user.getDomainId());
			if(domain == null) throw new WTException("Domain not found [{0}]", user.getDomainId());
			
			OUser ouser = null;
			if(updateDirectory) {
				AuthenticationDomain ad = new AuthenticationDomain(domain);
				AbstractDirectory directory = authm.getAuthDirectory(ad.getAuthUri());
				DirectoryOptions opts = WTRealm.createDirectoryOptions(wta, ad);
				
				if(directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
					if(!directory.validateUsername(opts, user.getUserId())) {
						throw new WTException("Username does not satisfy directory requirements [{0}]", ad.getAuthUri().getScheme());
					}
				}
				if(directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
					if(domain.getWebtopAdvSecurity() && !directory.validatePasswordPolicy(opts, password)) {
						throw new WTException("Password does not satisfy directory requirements [{0}]", ad.getAuthUri().getScheme());
					}
				}
				
				ouser = doUserInsert(con, domain, user);
				
				// Insert user in directory (if necessary)
				if(directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
					logger.debug("Adding user into directory");
					try {
						directory.addUser(opts, domain.getDomainId(), createUserEntry(user));
					} catch(EntryException ex1) {
						logger.debug("Skipped: already exists!");
					}
				}
				if(directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
					logger.debug("Updating its password");
					directory.updateUserPassword(opts, domain.getDomainId(), user.getUserId(), password);
				}
				
			} else {
				ouser = doUserInsert(con, domain, user);
			}
			
			DbUtils.commitQuietly(con);
			
			// Update cache
			addToUidCache(new UserUid(ouser.getDomainId(), user.getUserId(), ouser.getUserUid(), ouser.getRoleUid()));

			// Explicitly sets some important (locale & timezone) user settings to their defaults
			UserProfile.Id pid = new UserProfile.Id(ouser.getDomainId(), ouser.getUserId());
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, ouser.getDomainId());
			CoreUserSettings cus = new CoreUserSettings(pid);
			cus.setLanguageTag(css.getDefaultLanguageTag());
			cus.setTimezone(css.getDefaultTimezone());
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid domain auth URI");
		} catch(DirectoryException ex) {
			throw new WTException(ex, "DirectoryException error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public CheckUserResult checkUser(UserProfile.Id pid) throws WTException {
		return checkUser(pid.getDomainId(), pid.getUserId());
	}
	
	public CheckUserResult checkUser(String domainId, String userId) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			OUser o = dao.selectByDomainUser(con, domainId, userId);
			return new CheckUserResult(o != null, o != null ? o.getEnabled() : false);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateUser(UserEntity user) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			doUserUpdate(con, user);
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean updateUser(UserProfile.Id pid, boolean enabled) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return dao.updateEnabledByDomainUser(con, pid.getDomainId(), pid.getUserId(), enabled) == 1;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateUserPassword(UserProfile.Id pid, char[] oldPassword, char[] newPassword) throws WTException {
		AuthManager authm = wta.getAuthManager();
		
		try {
			ODomain domain = getDomain(pid.getDomainId());
			if(domain == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			
			AuthenticationDomain ad = new AuthenticationDomain(domain);
			AbstractDirectory directory = authm.getAuthDirectory(ad.getAuthUri());
			DirectoryOptions opts = WTRealm.createDirectoryOptions(wta, ad);
			
			if(directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
				if(oldPassword != null) {
					directory.updateUserPassword(opts, pid.getDomainId(), pid.getUserId(), oldPassword, newPassword);
				} else {
					directory.updateUserPassword(opts, pid.getDomainId(), pid.getUserId(), newPassword);
				}
			}
			
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid URI");
		} catch(DirectoryException ex) {
			throw new WTException(ex, "Directory error");
		}
	}
	
	public void deleteUser(UserProfile.Id pid, boolean cleanupDirectory) throws WTException {
		AuthManager authm = wta.getAuthManager();
		UserDAO udao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			OUser user = udao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			if(user == null) throw new WTException("User not found [{0}]", pid.toString());
			
			authm.deletePermission(con, user.getRoleUid());
			uidao.deleteByDomainUser(con, pid.getDomainId(), pid.getUserId());
			udao.deleteByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
			if(cleanupDirectory) {
				ODomain domain = getDomain(pid.getDomainId());
				if(domain == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());

				AuthenticationDomain ad = new AuthenticationDomain(domain);
				AbstractDirectory directory = authm.getAuthDirectory(ad.getAuthUri());
				DirectoryOptions opts = WTRealm.createDirectoryOptions(wta, ad);
				
				if(directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
					directory.deleteUser(opts, pid.getDomainId(), pid.getUserId());
				}
			}
			
			DbUtils.commitQuietly(con);
			
			// Update cache
			removeFromUidCache(pid);
			removeFromUserCache(pid);
			
			// Cleanup all user settings ?????????????????????????????????????????????????
			//wta.getSettingsManager().clearUserSettings(pid.getDomainId(), pid.getUserId());
			
			// TODO: chiamare controller per gestire pulizia utente sui servizi
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(URISyntaxException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "Invalid URI");
		} catch(DirectoryException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "Directory error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DirectoryUser> listDirectoryUsers(ODomain domain) throws WTException {
		AuthManager authm = wta.getAuthManager();
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			AuthenticationDomain ad = new AuthenticationDomain(domain);
			AbstractDirectory directory = authm.getAuthDirectory(ad.getAuthUri());
			DirectoryOptions opts = WTRealm.createDirectoryOptions(wta, ad);
			
			con = wta.getConnectionManager().getConnection();
			Map<String, OUser> wtUsers = dao.selectByDomain2(con, domain.getDomainId());
			
			ArrayList<DirectoryUser> items = new ArrayList<>();
			
			if(directory.hasCapability(DirectoryCapability.USERS_READ)) {
				for(UserEntry userEntry : directory.listUsers(opts, domain.getDomainId())) {
					items.add(new DirectoryUser(domain.getDomainId(), userEntry, wtUsers.get(userEntry.userId)));
				}
				
			} else {
				for(OUser ouser : wtUsers.values()) {
					final AbstractDirectory.UserEntry userEntry = new AbstractDirectory.UserEntry(ouser.getUserId(), null, null, ouser.getDisplayName(), null);
					items.add(new DirectoryUser(domain.getDomainId(), userEntry, ouser));
				}
			}
			
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid URI");
		} catch(DirectoryException ex) {
			throw new WTException(ex, "Directory error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public UserPersonalInfo getUserPersonalInfo(UserProfile.Id pid) throws WTException {
		UserInfoDAO dao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			OUserInfo oui = dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			return (oui == null) ? null : new UserPersonalInfo(oui);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean updateUserPersonalInfo(UserProfile.Id pid, UserPersonalInfo userPersonalInfo) throws WTException {
		UserInfoDAO dao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			OUserInfo oui = createUserInfo(userPersonalInfo);
			oui.setDomainId(pid.getDomainId());
			oui.setUserId(pid.getUserId());
			return dao.update(con, oui) == 1;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
	
	
	
	/*
	public UserProfile.Data userData999(UserProfile.Id pid) throws WTException {
		synchronized(cacheUserToData) {
			if(!cacheUserToData.containsKey(pid)) {
				try {
					OUser user = getUser(pid);
					if(user == null) throw new WTException("User not found [{0}]", pid.toString());
					
					CoreUserSettings cus = new CoreUserSettings(pid);
					UserPersonalInfo info = userPersonalInfo(pid);
					InternetAddress ia = MailUtils.buildInternetAddress(info.getEmail(), user.getDisplayName());
					UserProfile.Data data = new UserProfile.Data(user.getDisplayName(), cus.getLanguageTag(), cus.getTimezone(), ia);
					cacheUserToData.put(pid, data);
					return data;
				} catch(WTException ex) {
					logger.error("Unable to find user [{}]", pid);
					throw ex;
				}
			} else {
				return cacheUserToData.get(pid);
			}
		}
	}
	
	public UserPersonalInfo userPersonalInfo999(UserProfile.Id pid) throws WTException {
		synchronized(lock2) {
			if(!cacheUserToPersonalInfo.containsKey(pid)) {
				try {
					UserInfoProviderBase uip = getUserInfoProvider();
					UserPersonalInfo info = uip.getInfo(pid.getDomainId(), pid.getUserId());
					cacheUserToPersonalInfo.put(pid, info);
					return info;
				} catch(WTException ex) {
					logger.error("Unable to find personal info for user [{}]", pid);
					throw ex;
				}	
			} else {
				return cacheUserToPersonalInfo.get(pid);
			}
		}
	}
	*/
	
	public UserPersonalInfo userPersonalInfo(UserProfile.Id pid) throws WTException {
		synchronized(cacheUserToPersonalInfo) {
			if(!cacheUserToPersonalInfo.containsKey(pid)) {
				UserPersonalInfo upi = getUserPersonalInfo(pid);
				if(upi == null) throw new WTException("UserPersonalInfo not found [{0}]", pid.toString());
				cacheUserToPersonalInfo.put(pid, upi);
				return upi;
			} else {
				return cacheUserToPersonalInfo.get(pid);
			}
		}
	}
	
	public UserProfile.Data userData(UserProfile.Id pid) throws WTException {
		synchronized(cacheUserToData) {
			if(!cacheUserToData.containsKey(pid)) {
				UserProfile.Data ud = getUserData(pid);
				cacheUserToData.put(pid, ud);
				return ud;
			} else {
				return cacheUserToData.get(pid);
			}
		}
	}
	
	public String userToUid(UserProfile.Id pid) {
		synchronized(lock1) {
			if(!cacheUserToUidBag.containsKey(pid)) throw new WTRuntimeException("[userToSidCache] Cache miss on key {0}", pid.toString());
			return cacheUserToUidBag.get(pid).userUid;
		}
	}
	
	public String userToRoleUid(UserProfile.Id pid) {
		synchronized(lock1) {
			if(!cacheUserToUidBag.containsKey(pid)) throw new WTRuntimeException("[userToUidCache] Cache miss on key {0}", pid.toString());
			return cacheUserToUidBag.get(pid).roleUid;
		}
	}
	
	public UserProfile.Id uidToUser(String uid) {
		synchronized(lock1) {
			if(!cacheUserUidToUser.containsKey(uid)) throw new WTRuntimeException("[uidToUserCache] Cache miss on key {0}", uid);
			return cacheUserUidToUser.get(uid);
		}
	}
	
	public UserProfile.Id roleUidToUser(String uid) {
		synchronized(lock1) {
			if(!cacheRoleUidToUser.containsKey(uid)) throw new WTRuntimeException("[roleUidToUserCache] Cache miss on key {0}", uid);
			return cacheRoleUidToUser.get(uid);
		}
	}
	
	public String getInternetUserId(UserProfile.Id pid) throws WTException {
		ODomain domain = getDomain(pid.getDomainId());
		return new UserProfile.Id(domain.getDomainName(), pid.getUserId()).toString();
	}
	
	public String getDomainInternetName(String domainId) throws WTException {
		ODomain domain = getDomain(domainId);
		return domain.getDomainName();
	}
	
	
	
	
	
	
	
	
	private UserEntry createUserEntry(UserEntity user) {
		return new UserEntry(user.getUserId(), user.getFirstName(), user.getLastName(), user.getDisplayName(), null);
	}
	
	private void doUserUpdate(Connection con, UserEntity user) throws DAOException, WTException {
		AuthManager authm = wta.getAuthManager();
		UserDAO udao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		
		UserEntity oldUser = getUserEntity(con, user.getProfileId());
		if(oldUser == null) throw new WTException("User not found [{0}]", user.getProfileId().toString());
		
		logger.debug("Updating User");
		OUser ouser = new OUser();
		ouser.setDomainId(user.getDomainId());
		ouser.setUserId(user.getUserId());
		ouser.setEnabled(user.getEnabled());
		ouser.setDisplayName(user.getDisplayName());
		udao.updateEnabledDisplayName(con, ouser);

		logger.debug("Updating UserInfo");
		OUserInfo ouseri = new OUserInfo();
		ouseri.setDomainId(user.getDomainId());
		ouseri.setUserId(user.getUserId());
		ouseri.setFirstName(user.getFirstName());
		ouseri.setLastName(user.getLastName());
		uidao.updateFirstLastName(con, ouseri);

		logger.debug("Updating permissions");
		LangUtils.CollectionChangeSet<ORolePermission> changeSet1 = LangUtils.getCollectionChanges(oldUser.getPermissions(), user.getPermissions());
		for(ORolePermission perm : changeSet1.deleted) {
			authm.deletePermission(con, perm.getRolePermissionId());
		}
		for(ORolePermission perm : changeSet1.inserted) {
			authm.addPermission(con, oldUser.getRoleUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
		}

		LangUtils.CollectionChangeSet<ORolePermission> changeSet2 = LangUtils.getCollectionChanges(oldUser.getServicesPermissions(), user.getServicesPermissions());
		for(ORolePermission perm : changeSet2.deleted) {
			authm.deletePermission(con, perm.getRolePermissionId());
		}
		for(ORolePermission perm : changeSet2.inserted) {
			authm.addPermission(con, oldUser.getRoleUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
		}
	}
	
	private OUser doUserInsert(Connection con, ODomain domain, UserEntity user) throws DAOException, WTException {
		AuthManager authm = wta.getAuthManager();
		UserDAO udao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		
		InternetAddress email = MailUtils.buildInternetAddress(user.getUserId(), domain.getDomainName(), null);
		if(email == null) throw new WTException("Cannot create a valid email address [{0}, {1}]", user.getUserId(), domain.getDomainName());
		
		// Insert User record
		logger.debug("Inserting User");
		OUser ouser = new OUser();
		ouser.setDomainId(user.getDomainId());
		ouser.setUserId(user.getUserId());
		ouser.setEnabled(user.getEnabled());
		ouser.setUserUid(IdentifierUtils.getUUID());
		ouser.setRoleUid(IdentifierUtils.getUUID());
		ouser.setDisplayName(user.getDisplayName());
		ouser.setSecret(generateSecretKey());
		udao.insert(con, ouser);
		
		// Insert UserInfo record
		logger.debug("Inserting UserInfo");
		OUserInfo oui = new OUserInfo();
		oui.setDomainId(user.getDomainId());
		oui.setUserId(user.getUserId());
		oui.setFirstName(user.getFirstName());
		oui.setLastName(user.getLastName());
		oui.setEmail(email.getAddress());
		uidao.insert(con, oui);
		
		// Insert permissions
		logger.debug("Inserting permissions");
		for(ORolePermission perm : user.getPermissions()) {
			authm.addPermission(con, ouser.getRoleUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
		}
		for(ORolePermission perm : user.getServicesPermissions()) {
			authm.addPermission(con, ouser.getRoleUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
		}
		
		return ouser;
	}
	
	private UserProfile.Data getUserData(UserProfile.Id pid) throws WTException {
		CoreUserSettings cus = new CoreUserSettings(pid);
		UserPersonalInfo upi = userPersonalInfo(pid);
		OUser ouser = getUser(pid);
		if(ouser == null) throw new WTException("User not found [{0}]", pid.toString());

		InternetAddress ia = MailUtils.buildInternetAddress(upi.getEmail(), ouser.getDisplayName());
		return new UserProfile.Data(ouser.getDisplayName(), cus.getLanguageTag(), cus.getTimezone(), ia);
	}
	
	private OUserInfo createUserInfo(UserPersonalInfo upi) {
		OUserInfo oui = new OUserInfo();
		oui.setTitle(upi.getTitle());
		oui.setFirstName(upi.getFirstName());
		oui.setLastName(upi.getLastName());
		oui.setNickname(upi.getNickname());
		oui.setGender(upi.getGender());
		oui.setEmail(upi.getEmail());
		oui.setTelephone(upi.getTelephone());
		oui.setFax(upi.getFax());
		oui.setPager(upi.getPager());
		oui.setMobile(upi.getMobile());
		oui.setAddress(upi.getAddress());
		oui.setCity(upi.getCity());
		oui.setPostalCode(upi.getPostalCode());
		oui.setState(upi.getState());
		oui.setCountry(upi.getCountry());
		oui.setCompany(upi.getCompany());
		oui.setFunction(upi.getFunction());
		oui.setCustom1(upi.getCustom01());
		oui.setCustom2(upi.getCustom02());
		oui.setCustom3(upi.getCustom03());
		return oui;
	}
	
	private void cleanupUserCache() {
		synchronized(cacheUserToData) {
			cacheUserToData.clear();
		}
		synchronized(cacheUserToPersonalInfo) {
			cacheUserToPersonalInfo.clear();
		}
	}
	
	private void addToUserCache(UserProfile.Id pid, UserProfile.Data userData) {
		synchronized(cacheUserToData) {
			cacheUserToData.put(pid, userData);
		}
	}
	
	private void addToUserCache(UserProfile.Id pid, UserPersonalInfo userPersonalInfo) {
		synchronized(cacheUserToPersonalInfo) {
			cacheUserToPersonalInfo.put(pid, userPersonalInfo);
		}
	}
	
	private void removeFromUserCache(UserProfile.Id pid) {
		synchronized(cacheUserToData) {
			cacheUserToData.remove(pid);
		}
		synchronized(cacheUserToPersonalInfo) {
			cacheUserToPersonalInfo.remove(pid);
		}
	}
	
	private void initUidCache() {
		Connection con = null;
		
		try {
			synchronized(lock1) {
				UserDAO dao = UserDAO.getInstance();
				
				con = wta.getConnectionManager().getConnection();
				List<UserUid> uids = dao.selectAllUids(con);
				cleanupUidCache();
				for(UserUid uid : uids) {
					addToUidCache(uid);
				}
			}
		} catch(SQLException ex) {
			throw new WTRuntimeException(ex, "Unable to init UID cache");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void cleanupUidCache() {
		synchronized(lock1) {
			cacheUserToUidBag.clear();
			cacheUserUidToUser.clear();
			cacheRoleUidToUser.clear();
		}
	}
	
	private void addToUidCache(UserUid uid) {
		synchronized(lock1) {
			UserProfile.Id pid = new UserProfile.Id(uid.getDomainId(), uid.getUserId());
			cacheUserToUidBag.put(pid, new UserUidBag(uid.getUserUid(), uid.getRoleUid()));
			cacheUserUidToUser.put(uid.getUserUid(), pid);
			cacheRoleUidToUser.put(uid.getRoleUid(), pid);
		}
	}
	
	private void removeFromUidCache(UserProfile.Id pid) {
		synchronized(lock1) {
			if(cacheUserToUidBag.containsKey(pid)) {
				UserUidBag bag = cacheUserToUidBag.remove(pid);
				cacheUserUidToUser.remove(bag.userUid);
				cacheRoleUidToUser.remove(bag.roleUid);
			}
		}
	}
	
	public static class CheckUserResult {
		public boolean exist;
		public boolean enabled;
		
		public CheckUserResult(boolean exist, boolean enabled) {
			this.exist = exist;
			this.enabled = enabled;
		}
	}
	
	public static class UserUidBag {
		public String userUid;
		public String roleUid;
		
		public UserUidBag() {}
		
		public UserUidBag(String uid, String roleUid) {
			this.userUid = uid;
			this.roleUid = roleUid;
		}
	}
}
