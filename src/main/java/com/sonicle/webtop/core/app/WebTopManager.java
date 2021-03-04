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

import com.sonicle.commons.AlgoUtils;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.cache.AbstractBulkCache;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.ConnectionSecurity;
import com.sonicle.security.DomainAccount;
import com.sonicle.security.PasswordUtils;
import com.sonicle.security.Principal;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.EntryException;
import com.sonicle.security.auth.directory.ADDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory.AuthUser;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.security.auth.directory.ImapDirectory;
import com.sonicle.security.auth.directory.LdapDirectory;
import com.sonicle.security.auth.directory.LdapNethDirectory;
import com.sonicle.security.auth.directory.SftpDirectory;
import com.sonicle.security.auth.directory.SmbDirectory;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.auth.LdapWebTopDirectory;
import com.sonicle.webtop.core.app.auth.WebTopDirectory;
import com.sonicle.webtop.core.app.sdk.WTMultiCauseWarnException;
import com.sonicle.webtop.core.app.sdk.WTPwdPolicyException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.bol.AssignedGroup;
import com.sonicle.webtop.core.bol.AssignedRole;
import com.sonicle.webtop.core.bol.AssignedUser;
import com.sonicle.webtop.core.bol.GroupUid;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.ORole;
import com.sonicle.webtop.core.bol.ORoleAssociation;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.OUserAssociation;
import com.sonicle.webtop.core.bol.OUserInfo;
import com.sonicle.webtop.core.bol.UserId;
import com.sonicle.webtop.core.bol.UserUid;
import com.sonicle.webtop.core.bol.model.DirectoryUser;
import com.sonicle.webtop.core.model.DomainEntity;
import com.sonicle.webtop.core.bol.model.GroupEntity;
import com.sonicle.webtop.core.model.ParamsLdapDirectory;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleEntity;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.AutosaveDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DomainDAO;
import com.sonicle.webtop.core.dal.DomainSettingDAO;
import com.sonicle.webtop.core.dal.GroupDAO;
import com.sonicle.webtop.core.dal.MessageQueueDAO;
import com.sonicle.webtop.core.dal.RoleAssociationDAO;
import com.sonicle.webtop.core.dal.RoleDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.ServiceStoreEntryDAO;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.ShareDataDAO;
import com.sonicle.webtop.core.dal.SnoozedReminderDAO;
import com.sonicle.webtop.core.dal.AuditLogDAO;
import com.sonicle.webtop.core.dal.UserAssociationDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.dal.UserInfoDAO;
import com.sonicle.webtop.core.dal.UserSettingDAO;
import com.sonicle.webtop.core.model.PublicImage;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import com.sonicle.webtop.core.sdk.BaseServiceSettings;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTCyrusException;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.Rights;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class WebTopManager {
	private static final Logger logger = WT.getLogger(WebTopManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	static synchronized WebTopManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		WebTopManager instance = new WebTopManager(wta);
		initialized = true;
		logger.info("Initialized");
		return instance;
	}
	
	private WebTopApp wta = null;
	public static final String INTERNETNAME_LOCAL = "local";
	public static final String ROLEUID_SYSADMIN = "SYSADMIN";
	public static final String ROLEUID_WTADMIN = "WTADMIN";
	public static final String ROLEUID_IMPERSONATED_USER = "IMPERSONATED_USER";
	//public static final String SYSADMIN_PSTRING = ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "SYSADMIN"), "ACCESS", "*");
	//public static final String WTADMIN_PSTRING = ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), "ACCESS", "*");
	public static final String SYSADMIN_DOMAINID = "*";
	public static final String SYSADMIN_USERID = "admin";
	public static final String DOMAINADMIN_USERID = "admin";
	public static final String GROUPID_ADMINS = "admins";
	public static final String GROUPID_USERS = "users";
	public static final String GROUPID_PEC_ACCOUNTS = "pec-accounts";
	
	private final CacheDomainInfo domainCache = new CacheDomainInfo();
	private final KeyedReentrantLocks lockSecretGet = new KeyedReentrantLocks<String>();
	private final Map<String, ProductLicense> productLicenseCache = new ConcurrentHashMap<>();
	
	private final Object lock1 = new Object();
	private final HashMap<UserProfileId, String> cacheUserToUserUid = new HashMap<>();
	private final HashMap<String, UserProfileId> cacheUserUidToUser = new HashMap<>();
	private final Object lock2 = new Object();
	private final HashMap<UserProfileId, String> cacheGroupToGroupUid = new HashMap<>();
	private final HashMap<String, UserProfileId> cacheGroupUidToGroup = new HashMap<>();
	
	private final HashMap<UserProfileId, UserProfile.PersonalInfo> cacheUserToPersonalInfo = new HashMap<>();
	private final HashMap<UserProfileId, UserProfile.Data> cacheUserToData = new HashMap<>();
	private final Object lock3 = new Object();
	
	/**
	 * Creates UserProfileId for SysAdmin.
	 * @return UserProfileId
	 */
	public static UserProfileId sysAdminProfileId() {
		return new UserProfileId(SYSADMIN_DOMAINID, SYSADMIN_USERID);
	}
	
	/**
	 * Checks if passed profileId is the SysAdmin.
	 * @param profileId The profileId to check.
	 * @return True if passed profileId is the SysAdmin.
	 */
	public static boolean isSysAdmin(UserProfileId profileId) {
		return SYSADMIN_DOMAINID.equals(profileId.getDomainId()) && SYSADMIN_USERID.equals(profileId.getUserId());
	}
	
	public static String generateSecretKey() {
		return StringUtils.defaultIfBlank(IdentifierUtils.generateSecretKey(), "0123456789101112");
	}
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private WebTopManager(WebTopApp wta) {
		this.wta = wta;
		domainCache.init();
		initGroupUidCache();
		initUserUidCache();
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		cleanupUserUidCache();
		cleanupGroupUidCache();
		cleanupUserCache();
		domainCache.clear();
		wta = null;
		logger.info("Cleaned up");
	}
	
	public void initDomainCache() {
		domainCache.init();
	}
	
	public void cleanUserProfileCache(UserProfileId pid) {
		removeFromUserCache(pid);
	}
	
	public AbstractDirectory getAuthDirectory(String authUri) throws WTException {
		try {
			return getAuthDirectoryByScheme(new URI(authUri).getScheme());
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid authentication URI [{0}]", authUri);
		}
	}
	
	public AbstractDirectory getAuthDirectory(URI authUri) throws WTException {
		return getAuthDirectoryByScheme(authUri.getScheme());
	}
	
	public AbstractDirectory getAuthDirectoryByScheme(String scheme) throws WTException {
		DirectoryManager dirManager = DirectoryManager.getManager();
		AbstractDirectory directory = dirManager.getDirectory(scheme);
		if (directory == null) throw new WTException("Directory not supported [{}]", scheme);
		return directory;
	}
	
	public AbstractDirectory getAuthDirectory(UserProfileId profileId) throws WTException {
		AuthenticationDomain ad = createAuthenticationDomain(profileId);
		return getAuthDirectory(ad.getDirUri());
	}
	
	private AuthenticationDomain createAuthenticationDomain(UserProfileId profileId) throws WTException {
		try {
			if (Principal.xisAdmin(profileId.getDomainId(), profileId.getUserId())) {
				return createSysAdminAuthenticationDomain();

			} else {
				ODomain odom = getDomain(profileId.getDomainId());
				if (odom == null) throw new WTException("Domain not found [{0}]", profileId.getDomainId());
				return createAuthenticationDomain(odom);
			}
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid URI");
		}
	}
	
	public AuthenticationDomain createAuthenticationDomain(ODomain domain) throws URISyntaxException {
		return new AuthenticationDomain(domain.getDomainId(), 
				domain.getInternetName(), 
				domain.getDirUri(), 
				domain.getDirCaseSensitive(),
				domain.getDirAdmin(), 
				(domain.getDirPassword() != null) ? domain.getDirPassword().toCharArray() : null, 
				EnumUtils.getEnum(ConnectionSecurity.class, domain.getDirConnectionSecurity()),
				domain.getDirParameters()
		);
	}
	
	public AuthenticationDomain createSysAdminAuthenticationDomain() throws URISyntaxException {
		return new AuthenticationDomain("*", null, createSysAdminAuthDirectoryUri(), false, null, null, null, null);
	}
	
	public String createSysAdminAuthDirectoryUri() throws URISyntaxException {
		DirectoryManager dirManager = DirectoryManager.getManager();
		return dirManager.getDirectory(WebTopDirectory.SCHEME).buildUri("localhost", null, null).toString();
	}
	
	public String domainIdToPublicName(String domainId) {
		return AlgoUtils.adler32Hex(domainId);
	}
	
	public String publicNameToDomainId(String domainPublicName) {
		return domainCache.publicNameToDomainId(domainPublicName);
	}
	
	public UserProfileId authProfile(UserProfileId profile) throws WTException {
		return new UserProfileId(domainIdToDomainInternetName(profile.getDomainId()), profile.getUserId());
	}
	
	public String domainIdToDomainInternetName(String domainId) {
		if (StringUtils.equals(domainId, SYSADMIN_DOMAINID)) {
			return INTERNETNAME_LOCAL;
		} else {
			return domainCache.domainIdToDomainInternetName(domainId);
		}
	}
	
	public String domainInternetNameToDomainId(String domainInternetName) {
		return domainCache.domainInternetNameToDomainId(domainInternetName);
	}
	
	public String publicInternetNameToDomainId(String publicInternetName, boolean strict) {
		if (strict) {
			return domainCache.publicInternetNameToDomainId(publicInternetName);
		} else {
			for (int i=0; i<255; i++) {
				String name = publicInternetName;
				if (i > 0) {
					final int iOfNDot = StringUtils.ordinalIndexOf(publicInternetName, ".", i);
					if (iOfNDot == -1) break;
					name = StringUtils.substring(publicInternetName, iOfNDot+1);
				}
				final String domainId = domainCache.publicInternetNameToDomainId(name);
				if (domainId != null) return domainId;
				/*
				final int iOfNDot = StringUtils.ordinalIndexOf(publicInternetName, ".", i);
				if (i > 0 && iOfNDot == -1) break;
				final String domainId = domainCache.publicInternetNameToDomainId(StringUtils.substring(publicInternetName, iOfNDot+1));
				if (domainId != null) return domainId;
				*/
			}
			return null;
		}
	}
	
	/**
	 * @deprecated
	 * Left here to satisfy compatibility with images lookup in ResourceRequest servlet!
	 * @param internetName
	 * @return
	 */
	@Deprecated
	public String legacyInternetNameToDomain(String internetName) {
		if (domainCache.getSize() == 1) {
			// If we have only one domain in cache, simply returns it...
			Map.Entry<String, String> entry = domainCache.domainInternetNameToDomainId.entrySet().iterator().next();
			return entry.getValue();
			
		} else {
			for(int i=2; i<255; i++) {
				final int iOfNDot = StringUtils.lastOrdinalIndexOf(internetName, ".", i);
				final String key = StringUtils.substring(internetName, iOfNDot+1);
				String domainId = domainCache.domainInternetNameToDomainId(key);
				if (domainId != null) return domainId;
				if (iOfNDot == -1) break; 
			}
			return null;
		}
	}
	
	public void checkDomains() {
		DomainDAO dao = DomainDAO.getInstance();
		Connection con = null;
		boolean needsCacheReload = false;
		
		try {
			logger.debug("Checking domains...");
			con = wta.getConnectionManager().getConnection();
			for (ODomain odomain : dao.selectAll(con)) {
				try {
					if (doPrepareDomain(con, odomain)) needsCacheReload = true;
				} catch(WTException ex1) {
					logger.error("Unable to verify domain [{}]", odomain.getDomainId(), ex1);
				}
			}
			
		} catch(SQLException ex) {
			throw new WTRuntimeException(ex, "Unable to verify domains");
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (needsCacheReload) {
			initGroupUidCache();
			initUserUidCache();
		}
	}
	
	public List<ODomain> listDomains(boolean enabledOnly) throws WTException {
		DomainDAO dao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
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
			con = wta.getConnectionManager().getConnection();
			return dao.selectEnabledByInternetName(con, internetDomain);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ODomain getDomain(String domainId) throws WTException {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return getDomain(con, domainId);
			
		} catch(SQLException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ODomain getDomain(Connection con, String domainId) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		
		try {
			return domDao.selectById(con, domainId);
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		}
	}
	
	public DomainEntity getDomainEntity(String domainId) throws WTException {
		ODomain odomain = getDomain(domainId);
		try {
			if (odomain == null) return null;
			DomainEntity domain = AppManagerUtils.fillDomainEntity(new DomainEntity(), odomain);
			domain.setDirPassword(decDirPassword(domain.getDirPassword()));
			return domain;
			
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid directory URI");
		}
	}
	
	public DomainEntity.PasswordPolicies getDomainPasswordPolicies(String domainId) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			if (Principal.xisAdminDomain(domainId)) {
				return new DomainEntity.PasswordPolicies(null, null, null, null, null, null, null);
				
			} else {
				con = wta.getConnectionManager().getConnection();
				return AppManagerUtils.createDomainPasswordPolicies(domDao.selectPasswordPoliciesById(con, domainId));
			}	
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void initDomainWithDefaults(String domainId) throws WTException {
		ODomain odom = getDomain(domainId);
		if (odom == null) throw new WTException("Domain not found [{}]", domainId);
		
		Connection con = null;
		try {
			con = wta.getConnectionManager().getConnection();
			doPrepareDomain(con, odom);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean doPrepareDomain(Connection con, ODomain domain) throws WTException {
		GroupDAO grpDao = GroupDAO.getInstance();
		UserDAO usrDao = UserDAO.getInstance();
		boolean changed = false;
		
		final List<String> BUILT_IN_GROUPS = Arrays.asList(GROUPID_ADMINS, GROUPID_USERS, GROUPID_PEC_ACCOUNTS);
		Map<String, OGroup> groups = grpDao.selectByDomainIn(con, domain.getDomainId(), BUILT_IN_GROUPS);
		
		// Prepare built-in groups
		logger.debug("[{}] Checking built-in groups...", domain.getDomainId());
		if (!groups.containsKey(GROUPID_ADMINS)) {
			OGroup ogroup = doGroupInsert(con, domain.getDomainId(), GROUPID_ADMINS, "Admins");
			addToGroupUidCache(new GroupUid(ogroup.getDomainId(), ogroup.getUserId(), ogroup.getUserUid()));
			changed = true;
		}
		if (!groups.containsKey(GROUPID_USERS)) {
			OGroup ogroup = doGroupInsert(con, domain.getDomainId(), GROUPID_USERS, "Users");
			addToGroupUidCache(new GroupUid(ogroup.getDomainId(), ogroup.getUserId(), ogroup.getUserUid()));
			changed = true;
		}
		if (!groups.containsKey(GROUPID_PEC_ACCOUNTS)) {
			OGroup ogroup = doGroupInsert(con, domain.getDomainId(), GROUPID_PEC_ACCOUNTS, "PEC Accounts");
			addToGroupUidCache(new GroupUid(ogroup.getDomainId(), ogroup.getUserId(), ogroup.getUserUid()));
			changed = true;
		}
		
		// Prepare built-in admin(for domain) user
		logger.debug("[{}] Checking built-in domain admin...", domain.getDomainId());
		if (!usrDao.existByDomainUser(con, domain.getDomainId(), DOMAINADMIN_USERID)) {
			UserEntity ue = new UserEntity();
			ue.setDomainId(domain.getDomainId());
			ue.setUserId(DOMAINADMIN_USERID);
			ue.setEnabled(true);
			ue.setFirstName("DomainAdmin");
			ue.setLastName(domain.getDescription());
			ue.setDisplayName(ue.getFirstName() + " [" + domain.getDescription() + "]");
			ue.getAssignedGroups().add(new AssignedGroup(WebTopManager.GROUPID_ADMINS));
			addUser(true, ue, true, null);
			changed = true;
		}
		return changed;
	}
	
	public void initDomainHomeFolder(String domainId) throws SecurityException {
		synchronized(lock3) {
			// Main folder (/domains/{domainId})
			File domainDir = new File(wta.getHomePath(domainId));
			if (!domainDir.exists()) domainDir.mkdir();
			
			// Internal folders...
			File tempDir = new File(wta.getTempPath(domainId));
			if (!tempDir.exists()) tempDir.mkdir();
			
			File imagesDir = new File(wta.getImagesPath(domainId));
			if (!imagesDir.exists()) imagesDir.mkdir();
			
			for (String sid : wta.getServiceManager().listRegisteredServices()) {
				File svcDir = new File(wta.getServiceHomePath(domainId, sid));
				if (!svcDir.exists()) svcDir.mkdir();
			}
		}	
	}
	
	public ODomain addDomain(DomainEntity domain) throws WTException {
		DomainDAO dodao = DomainDAO.getInstance();
		Connection con = null;
		ODomain odomain = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			logger.debug("Inserting domain");
			odomain = new ODomain();
			fillDomain(odomain, domain);
			dodao.insert(con, odomain);
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Update cache
		domainCache.init();
		
		return odomain;
	}
	
	public void updateDomain(DomainEntity domain) throws WTException {
		DomainDAO dodao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			logger.debug("Updating domain");
			ODomain odomain = new ODomain();
			fillDomain(odomain, domain);
			dodao.update(con, odomain);
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Update cache
		domainCache.init();
	}
	
	public void deleteDomain(String domainId) throws WTException {
		DomainDAO domdao = DomainDAO.getInstance();
		Connection con = null;
		ODomain odomain = null;
		List<OUser> ousers = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			odomain = domdao.selectById(con, domainId);
			if(odomain == null) throw new WTException("Domain not found [{0}]", odomain);
			ousers = listUsers(domainId, false);
			
			logger.debug("Deleting domain");
			ActivityDAO.getInstance().deleteByDomain(con, domainId);
			CausalDAO.getInstance().deleteByDomain(con, domainId);
			
			AutosaveDAO.getInstance().deleteByDomain(con, domainId);
			ServiceStoreEntryDAO.getInstance().deleteByDomain(con, domainId);
			SnoozedReminderDAO.getInstance().deleteByDomain(con, domainId);
			MessageQueueDAO.getInstance().deleteByDomain(con, domainId);
			AuditLogDAO.getInstance().deleteByDomain(con, domainId);
			
			DomainSettingDAO.getInstance().deleteByDomain(con, domainId);
			UserSettingDAO.getInstance().deleteByDomain(con, domainId);
			
			RoleAssociationDAO.getInstance().deleteByDomain(con, domainId);
			RolePermissionDAO.getInstance().deleteByDomain(con, domainId);
			RoleDAO.getInstance().deleteByDomain(con, domainId);
			ShareDAO.getInstance().deleteByDomain(con, domainId);
			ShareDataDAO.getInstance().deleteByDomain(con, domainId);
			
			UserAssociationDAO.getInstance().deleteByDomain(con, domainId);
			UserInfoDAO.getInstance().deleteByDomain(con, domainId);
			UserDAO.getInstance().deleteByDomain(con, domainId);
			GroupDAO.getInstance().deleteByDomain(con, domainId);
			domdao.deleteById(con, domainId);
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Update cache
		domainCache.init();
		initUserUidCache();
		initGroupUidCache();
		cleanupUserCache();
		
		try {
			AuthenticationDomain ad = createAuthenticationDomain(odomain);
			AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
			DirectoryOptions opts = wta.createDirectoryOptions(ad);
			
			if(directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
				for(OUser ouser : ousers) {
					final UserProfileId pid = new UserProfileId(ouser.getDomainId(), ouser.getUserId());
					directory.deleteUser(opts, pid.getDomainId(), pid.getUserId());
				}
			}
			
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid domain auth URI");
		} catch(DirectoryException ex) {
			throw new WTException(ex, "DirectoryException error");
		}

		//TODO: chiamare controller per eliminare dominio per i servizi
	}
	
	public List<PublicImage> listDomainPublicImages(String domainId) throws WTException {
		String path = WT.getDomainImagesPath(domainId);
		String baseUrl = WT.getPublicImagesUrl(domainId);
		File dir = new File(path);
		ArrayList<PublicImage> items = new ArrayList<>();
		for(File file : dir.listFiles()) {
			String name = file.getName();
			String url = PathUtils.concatPathParts(baseUrl, name);
			items.add(new PublicImage(name, url));
		}
		return items;
	}
	
	public List<OUser> listUsers(String domainId, boolean enabledOnly) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
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
	
	public List<UserProfileId> listUserProfileIdsByEmail(String emailAddress) throws WTException {
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			List<UserId> uids = uidao.viewByEmail(con, emailAddress);
			ArrayList<UserProfileId> items = new ArrayList<>();
			for(UserId uid : uids) {
				items.add(new UserProfileId(uid.getDomainId(), uid.getUserId()));
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OUser getUser(UserProfileId pid) throws WTException {
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
	
	public UserEntity getUserEntity(UserProfileId pid) throws WTException {
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
	
	private UserEntity getUserEntity(Connection con, UserProfileId pid) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		UserAssociationDAO uassdao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		
		try {
			OUser ouser = dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			if(ouser == null) throw new WTException("User not found [{0}]", pid.toString());
			OUserInfo ouseri = uidao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			if(ouseri == null) throw new WTException("User info not found [{0}]", pid.toString());
			
			List<AssignedGroup> assiGroups = uassdao.viewAssignedByUser(con, ouser.getUserUid());
			List<AssignedRole> assiRoles = rolassdao.viewAssignedByUser(con, ouser.getUserUid());
			EntityPermissions perms = extractPermissions(con, ouser.getUserUid());
			
			UserEntity user = new UserEntity(ouser, ouseri);
			user.setAssignedGroups(assiGroups);
			user.setAssignedRoles(assiRoles);
			user.setPermissions(perms.others);
			user.setServicesPermissions(perms.services);
			
			return user;
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		}
	}
	
	public String getUserSecret(UserProfileId profileId) throws WTException {
		UserDAO useDao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			try (KeyedReentrantLocks.KeyedLock lock = lockSecretGet.acquire(profileId.toString())) {
				con = wta.getConnectionManager().getConnection();
				OUser ouser = useDao.selectSecretByProfile(con, profileId.getDomainId(), profileId.getUserId());
				if (ouser == null) throw new WTException("User not found [{}]", profileId);

				String secret = ouser.getSecret();
				if (StringUtils.isBlank(secret)) {
					secret = generateSecretKey();				
					if (useDao.updateSecretByProfile(con, profileId.getDomainId(), profileId.getUserId(), secret) != 1) {
						throw new WTException("Unable to set generated secret [{}]", profileId);
					}
				}
				return secret;
			}
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addUser(boolean updateDirectory, UserEntity user, char[] password) throws WTException {
		addUser(updateDirectory, user, true, password);
	}
	
	public void addUser(boolean updateDirectory, UserEntity user, boolean updatePassword, char[] password) throws WTException {
		UserProfileId addedPid = null;
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			ODomain domain = getDomain(user.getDomainId());
			if (domain == null) throw new WTException("Domain not found [{}]", user.getDomainId());
			AuthenticationDomain ad = createAuthenticationDomain(domain);
			
			user.ensureCoherence();
			user.getAssignedGroups().add(new AssignedGroup(WebTopManager.GROUPID_USERS));
			
			OUser ouser = null;
			if (updateDirectory) {
				AbstractDirectory authDir = getAuthDirectory(ad.getDirUri());
				DirectoryOptions opts = wta.createDirectoryOptions(ad);
				
				if (authDir.hasCapability(DirectoryCapability.USERS_WRITE)) {
					if (!authDir.validateUsername(opts, user.getUserId())) {
						throw new WTException("Username does not satisfy directory requirements [{}]", ad.getDirUri().getScheme());
					}
				}
				if (updatePassword && authDir.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
					wta.setDirectoryOptionsPasswordPolicies(ad, opts, AppManagerUtils.createDomainPasswordPolicies(domain));
					if (password == null) {
						password = authDir.generatePassword(opts);
					} else {
						int ret = authDir.validatePasswordPolicy(opts, user.getUserId(), password);
						if (ret != 0) {
							throw new WTPwdPolicyException(ret, "Password does not satisfy directory policy [{}, {}]", ad.getDirUri().getScheme(), ret);
						}
					}
				}
				
				ouser = doUserInsert(con, domain, user);
				addedPid = new UserProfileId(ouser.getDomainId(), ouser.getUserId());
				
				// Insert user in directory (if necessary)
				if (authDir.hasCapability(DirectoryCapability.USERS_WRITE)) {
					logger.debug("Adding user into directory...");
					try {
						authDir.addUser(opts, domain.getDomainId(), createAuthUser(user));
					} catch(EntryException ex1) {
						logger.warn("Insertion skipped: user already exists [{}]", user.getUserId());
					}
				}
				if (updatePassword && authDir.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
					logger.debug("Updating its password");
					authDir.updateUserPassword(opts, addedPid.getDomainId(), addedPid.getUserId(), password);
					new CoreUserSettings(wta.getSettingsManager(), addedPid).setPasswordLastChange(DateTimeUtils.now());
				}
				
			} else {
				ouser = doUserInsert(con, domain, user);
				addedPid = new UserProfileId(ouser.getDomainId(), ouser.getUserId());
			}
			
			DbUtils.commitQuietly(con);
			
			// Update cache
			addToUserUidCache(new UserUid(addedPid.getDomainId(), addedPid.getUserId(), ouser.getUserUid()));

			// Explicitly sets some important (locale & timezone) user settings to their defaults
			CoreServiceSettings css = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, addedPid.getDomainId());
			CoreUserSettings cus = new CoreUserSettings(wta.getSettingsManager(), addedPid);
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
		
		// Performs some actions after the add operation
		if (addedPid != null) {
			List<Throwable> errors = wta.getServiceManager().invokeOnUserAdded(addedPid);
			if (!errors.isEmpty()) throw new WTMultiCauseWarnException(errors, "Errors in user related listeners");
		}		
	}
	
	public CheckUserResult checkUser(UserProfileId pid) throws WTException {
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
			
			user.ensureCoherence();
			doUserUpdate(con, user);
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean updateUser(UserProfileId pid, boolean enabled) throws WTException {
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
	
	public int updateUserEmailDomain(List<UserProfileId> pids, String newEmailDomain) throws WTException {
		UserInfoDAO uiDao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			if (pids.isEmpty()) return -1;
			con = wta.getConnectionManager().getConnection(false);
			
			String domainId = pids.get(0).getDomainId();
			List<String> userIds = pids.stream().map(pid -> pid.getUserId()).collect(Collectors.toList());
			String emailDomain = "@" + StringUtils.removeStart(newEmailDomain, "@");
			int ret = uiDao.updateEmailDomainByProfiles(con, domainId, userIds, emailDomain);
			DbUtils.commitQuietly(con);
			
			// Clean-up cache!
			for (UserProfileId pid : pids) cleanUserProfileCache(pid);
			
			return ret;
			
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateUserPassword(UserProfileId profileId, char[] oldPassword, char[] newPassword) throws WTException, EntryException {
		AuthenticationDomain ad = createAuthenticationDomain(profileId);
		
		try {
			AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
			if (!directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
				throw new WTException("Directory has no write capability");
			}
			
			DirectoryOptions opts = wta.createDirectoryOptions(ad);
			DomainEntity.PasswordPolicies pwdPolicies = getDomainPasswordPolicies(profileId.getDomainId());
			wta.setDirectoryOptionsPasswordPolicies(ad, opts, pwdPolicies);
			int ret = directory.validatePasswordPolicy(opts, profileId.getUserId(), newPassword);
			if (ret == 0 && oldPassword != null && pwdPolicies.getAvoidOldSimilarity()) {
				int similarityThres = 5;
				if (StringUtils.getLevenshteinDistance(new String(oldPassword), new String(newPassword)) < similarityThres) ret = 41;
			}
			if (ret != 0) {
				throw new WTPwdPolicyException(ret, "Password does not satisfy directory policy [{}]", ret);
			}
			
			if (oldPassword != null) {
				directory.updateUserPassword(opts, profileId.getDomainId(), profileId.getUserId(), oldPassword, newPassword);
			} else {
				directory.updateUserPassword(opts, profileId.getDomainId(), profileId.getUserId(), newPassword);
			}

			CoreUserSettings cus = new CoreUserSettings(profileId);
			cus.setPasswordLastChange(DateTimeUtils.now());
			cus.setPasswordForceChange(false);
			
		} catch(DirectoryException ex) {
			throw new WTException(ex, "Directory error");
		}
	}
	
	public boolean isUserPasswordChangeNeeded(UserProfileId profileId, char[] password) throws WTException {
		if (Principal.xisAdmin(profileId.getDomainId(), profileId.getUserId())) return false;
		
		AuthenticationDomain ad = createAuthenticationDomain(profileId);
		AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
		if (!directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) return false;
		
		DomainEntity.PasswordPolicies pwdPolicies = getDomainPasswordPolicies(profileId.getDomainId());
		if (pwdPolicies.getVerifyAtLogin() || pwdPolicies.getExpiration() != null) {
			if (pwdPolicies.getVerifyAtLogin()) {
				DirectoryOptions opts = wta.createDirectoryOptions(ad);
				wta.setDirectoryOptionsPasswordPolicies(ad, opts, pwdPolicies);
				int ret = directory.validatePasswordPolicy(opts, profileId.getUserId(), password);
				if (ret != 0) return true;
			}
			
			if (pwdPolicies.getExpiration() != null) {
				CoreUserSettings cus = new CoreUserSettings(profileId);
				DateTime lastChange = cus.getPasswordLastChange();
				// NB: No last-change timestamp means password change needed!
				if (lastChange == null || DateTimeUtils.datesBetween(lastChange, DateTimeUtils.now().toDateTime(DateTimeZone.UTC)) > pwdPolicies.getExpiration()) return true;
			}
		}
		
		CoreUserSettings cus = new CoreUserSettings(profileId);
		if (cus.getPasswordForceChange()) return true;
		
		return false;
	}
	
	public void deleteUser(UserProfileId pid, boolean cleanupDirectory) throws WTException {
		UserDAO udao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		UserAssociationDAO uadao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			OUser user = udao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			if (user == null) throw new WTException("User not found [{0}]", pid.toString());
			
			logger.debug("[{}] Clearing user licenses...", user.getUserUid());
			wta.getLicenseManager().revokeLicenseLease(pid);
			
			logger.debug("[{}] Clearing permissions...", user.getUserUid());
			rpdao.deleteByRole(con, user.getUserUid());
			logger.debug("[{}] Clearing groups associations...", user.getUserUid());
			uadao.deleteByUser(con, user.getUserUid());
			logger.debug("[{}] Clearing roles associations...", user.getUserUid());
			rolassdao.deleteByUser(con, user.getUserUid());
			logger.debug("[{}] Clearing userInfo...", pid.toString());
			uidao.deleteByDomainUser(con, pid.getDomainId(), pid.getUserId());
			logger.debug("[{}] Clearing user...", pid.toString());
			udao.deleteByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
			if (cleanupDirectory) {
				ODomain domain = getDomain(pid.getDomainId());
				if (domain == null) throw new WTException("Domain not found [{}]", pid.getDomainId());
				
				AuthenticationDomain ad = createAuthenticationDomain(domain);
				AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
				DirectoryOptions opts = wta.createDirectoryOptions(ad);
				
				if (directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
					directory.deleteUser(opts, pid.getDomainId(), pid.getUserId());
				}
			}
			
			DbUtils.commitQuietly(con);
			
		} catch(URISyntaxException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "Invalid URI");
		} catch(DirectoryException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "Directory error");
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Performs some actions after the remove operation
		List<Throwable> errors = wta.getServiceManager().invokeOnUserRemoved(pid);
		wta.getSettingsManager().deleteUserSettings(pid);
		
		// Update cache
		removeFromUserUidCache(pid);
		removeFromUserCache(pid);
		
		if (!errors.isEmpty()) throw new WTMultiCauseWarnException(errors, "Errors in user related listeners");
	}
	
	/**
	 * Returns the internal UID associated to specified group; 
	 * usually applied to defaults/built-in groups.
	 * @param domainId The domain ID.
	 * @param groupId The group ID.
	 * @return The group's UID if found; null otherwise
	 */
	public String getGroupUid(String domainId, String groupId) {
		UserProfileId pid = new UserProfileId(domainId, groupId);
		try {
			return groupToUid(pid);
		} catch(WTRuntimeException ex) {
			logger.debug("Unable to get group's UID [{}]", pid.toString(), ex);
			return null;
		}
	}
	
	/**
	 * Returns the internal UID associated to specified user.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @return The user's UID if found; null otherwise
	 */
	public String getUserUid(String domainId, String userId) {
		UserProfileId pid = new UserProfileId(domainId, userId);
		try {
			return userToUid(pid);
		} catch(WTRuntimeException ex) {
			logger.debug("Unable to get user's UID [{}]", pid.toString(), ex);
			return null;
		}
	}
	
	public List<DirectoryUser> listDirectoryUsers(ODomain domain) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			AuthenticationDomain ad = createAuthenticationDomain(domain);
			AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
			DirectoryOptions opts = wta.createDirectoryOptions(ad);
			
			con = wta.getConnectionManager().getConnection();
			Map<String, OUser> wtUsers = dao.selectByDomain2(con, domain.getDomainId());
			
			ArrayList<DirectoryUser> items = new ArrayList<>();
			
			if(directory.hasCapability(DirectoryCapability.USERS_READ)) {
				for(AuthUser userEntry : directory.listUsers(opts, domain.getDomainId())) {
					items.add(new DirectoryUser(domain.getDomainId(), userEntry, wtUsers.get(userEntry.userId)));
				}
				
			} else {
				for(OUser ouser : wtUsers.values()) {
					final AbstractDirectory.AuthUser userEntry = new AbstractDirectory.AuthUser(ouser.getUserId(), ouser.getDisplayName(), null, null, null);
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
	
	public boolean updateUserDisplayName(UserProfileId pid, String displayName) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return dao.updateDisplayNameByDomainUser(con, pid.getDomainId(), pid.getUserId(), displayName) == 1;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public UserProfile.PersonalInfo getUserPersonalInfo(UserProfileId pid) throws WTException {
		UserInfoDAO dao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			OUserInfo oui = dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			return (oui == null) ? null : new UserProfile.PersonalInfo(oui);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean updateUserPersonalInfo(UserProfileId pid, UserProfile.PersonalInfo userPersonalInfo) throws WTException {
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
	
	public List<OGroup> listGroups(String domainId) throws WTException {
		GroupDAO dao = GroupDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return dao.selectByDomain(con, domainId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public GroupEntity getGroupEntity(UserProfileId pid) throws WTException {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return getGroupEntity(con, pid);
			
		} catch(SQLException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private GroupEntity getGroupEntity(Connection con, UserProfileId pid) throws WTException {
		GroupDAO dao = GroupDAO.getInstance();
		UserAssociationDAO uassdao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		
		try {
			OGroup ogroup = dao.selectByDomainGroup(con, pid.getDomainId(), pid.getUserId());
			if(ogroup == null) throw new WTException("Group not found [{0}]", pid.toString());
			
			List<AssignedUser> assiUsers = uassdao.viewAssignedByGroup(con, ogroup.getGroupUid());
			List<AssignedRole> assiRoles = rolassdao.viewAssignedByGroup(con, ogroup.getGroupUid());
			EntityPermissions perms = extractPermissions(con, ogroup.getGroupUid());
			
			GroupEntity group = new GroupEntity(ogroup);
			group.setAssignedUsers(assiUsers);
			group.setAssignedRoles(assiRoles);
			group.setPermissions(perms.others);
			group.setServicesPermissions(perms.services);
			
			return group;
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		}
	}
	
	public void addGroup(GroupEntity group) throws WTException {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			ODomain domain = getDomain(group.getDomainId());
			if(domain == null) throw new WTException("Domain not found [{0}]", group.getDomainId());
			
			OGroup ogroup = doGroupInsert(con, domain.getDomainId(), group);
			
			DbUtils.commitQuietly(con);
			
			// Update cache
			addToGroupUidCache(new GroupUid(ogroup.getDomainId(), group.getGroupId(), ogroup.getGroupUid()));
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateGroup(GroupEntity group) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			doGroupUpdate(con, group);
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteGroup(UserProfileId pid) throws WTException {
		GroupDAO udao = GroupDAO.getInstance();
		UserAssociationDAO uadao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			OGroup group = udao.selectByDomainGroup(con, pid.getDomainId(), pid.getUserId());
			if(group == null) throw new WTException("Group not found [{0}]", pid.toString());
			
			logger.debug("Deleting permissions");
			rpdao.deleteByRole(con, group.getGroupUid());
			logger.debug("Deleting groups associations");
			uadao.deleteByGroup(con, group.getGroupUid());
			logger.debug("Deleting roles associations");
			rolassdao.deleteByUser(con, group.getGroupUid());
			logger.debug("Deleting group");
			udao.deleteByDomainGroup(con, pid.getDomainId(), pid.getUserId());
			
			DbUtils.commitQuietly(con);
			
			// Update cache
			removeFromGroupUidCache(pid);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean forgetProductLicense(String serviceId, String productId, String internetName) {
		String key = new CompositeId(serviceId, productId, internetName).toString();
		return productLicenseCache.remove(key) != null;
	}
	
	public ProductLicense getProductLicense(BaseServiceProduct product) {
		if (product == null) return null;
		/*
		String key = new CompositeId(product.SERVICE_ID, product.getProductId(), product.getInternetName()).toString();
		ProductLicense plic = productLicenseCache.computeIfAbsent(key, value -> {
			try {
				ServiceLicense rlic = getServiceLicense(product.SERVICE_ID, product.getProductId(), product.getInternetName());
				if (rlic != null) {
					ProductLicense plicNew = new ProductLicense(
						ProductLicense.LicenseType.LICENSE_TEXT,
						ProductLicense.ActivationLicenseType.OFF_NO_ACTIVATION,
						product,
						rlic.getLicenseText()
					);
					plicNew.validate();
					return plicNew;
				} else {
					logger.debug("License is missing, creating a dummy one! [{}]", key);
					return new DummyProductLicense();
					
				}
			} catch(Throwable t) {
				logger.error("Error retrieving registered license [{}]", key, t);
			}
			return null;
		});
		return (plic instanceof DummyProductLicense) ? null : plic;
		*/
		return null;
	}
	
	/*
	public List<ServiceLicense2> listServiceLicenses(String domainId) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			ArrayList<ServiceLicense2> items = new ArrayList<>();
			for (VLicense vlic : licDao.viewByDomain(con, domainId)) {
				Set<String> users = new LinkedHashSet(new CId(vlic.getUserIds()).getTokens());
				items.add(AppManagerUtils.createServiceLicense(vlic, users));
			}
			return items;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ServiceLicense2 getServiceLicense(String domainId, ProductId product) throws WTException {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doServiceLicenseGet(con, domainId, product, true);
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ServiceLicense2 doServiceLicenseGet(Connection con, String domainId, ProductId product, boolean processLeases) throws DAOException, WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		
		OLicense2 olic = licDao.select(con, domainId, product.getServiceId(), product.getProductCode());
		if (olic == null) return null;
		
		ServiceLicense2 lic = AppManagerUtils.createServiceLicense(olic);
		if (processLeases) {
			Set<String> userIds = lleaDao.selectUsersByDomainServiceProduct(con, domainId, product.getServiceId(), product.getProductCode());
			lic.setLeasedUsers(userIds);
		}
		return lic;
	}
	
	private <T extends OLicense2> T fillOLicenseWithDefaults(T tgt) {
		if ((tgt != null)) {
			if (tgt.getAutoLease() == null) tgt.setAutoLease(true);
		}
		return tgt;
	}
	*/

	
	/*
	public boolean assignServiceLicenseLease(String domainId, ProductId product, String userId, String activationString) throws WTException {
		ServiceLicense2 license = getServiceLicense(domainId, product);
		if (license == null) throw new WTException("TODO exception");
		
		return internalAssignServiceLicense(domainId, license, userId, activationString);
	}
	
	public boolean revokeServiceLicenseLease(String domainId, ProductId product, String userId) throws WTException {
		ServiceLicense2 license = getServiceLicense(domainId, product);
		if (license == null) throw new WTException("TODO exception");
		
		return internalRevokeServiceLicenseLease(domainId, license, userId);
	}
	
	private boolean internalRevokeServiceLicenseLease(String domainId, ServiceLicense2 license, String userId) throws WTException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			ProductLicense prodLic = checkProductLicense(license);
			//skip validity check???
			
			con = wta.getConnectionManager().getConnection();
			String aString = lleaDao.selectActivationStringByDomainServiceProductUser(con, domainId, license.getProductId().getServiceId(), productCode, userId);
			if (aString == null) throw new WTException("TODO exception");
			
			boolean ret = false;
			try {
				prodLic.setLicenseActivationString(aString);
				prodLic.autoDeactivate();
			} finally {
				ret = lleaDao.delete(con, domainId, license.getProductId().getServiceId(), license.getProductId().getProductCode(), userId) == 1;
			}
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean internalAssignServiceLicense(String domainId, ServiceLicense2 license, String userId, String activationString) throws WTException {
		LicenseLeaseDAO lleaDao = LicenseLeaseDAO.getInstance();
		Connection con = null;
		
		try {
			ProductLicense prodLic = checkProductLicense(license);
			
			if (StringUtils.isBlank(activationString)) {
				logger.debug("Activation string not provided. Trying automatic activation...");
				prodLic.autoActivate();
			} else {
				prodLic.setLicenseActivationString(activationString);
			}
			if (!prodLic.getLicenseInfo().isActivated()) throw new WTException();
			
			OLicenseLease ollea = new OLicenseLease();
			ollea.setDomainId(domainId);
			ollea.setServiceId(license.getProductId().getServiceId());
			ollea.setProductCode(license.getProductId().getProductCode());
			ollea.setUserId(userId);
			ollea.setActivationString(prodLic.getLicenseActivationString());
			
			con = wta.getConnectionManager().getConnection();
			boolean ret = lleaDao.insert(con, ollea) == 1;
			return ret;
		
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ProductLicense checkProductLicense(ServiceLicense2 license) throws WTException {
		return checkProductLicense(license.getDomainId(), license.getProductId(), license.getString());
	}
	
	private ProductLicense checkProductLicense(String domainId, ProductId productId, String licenseString) throws WTException {
		String internetName = WT.getDomainInternetName(domainId);
		ProductLicense prodLic = ProductUtils.getProductLicense(internetName, productId, licenseString);
		if (prodLic == null) throw new WTException("Unknown product '{}'", productId.getProductCode());
		if (!prodLic.getLicenseInfo().isValid()) throw new WTException("License provided for '{}' is not valid", productId.getProductCode());
		return prodLic;
	}
	
	public ServiceLicense2 addServiceLicense(ServiceLicense2 license) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			ProductLicense prodLic = checkProductLicense(license);
			
			con = wta.getConnectionManager().getConnection();
			OLicense2 olic = AppManagerUtils.createOLicense(license);
			fillOLicenseWithDefaults(olic);
			boolean ret = licDao.insert(con, olic) == 1;
			if (ret) {
				// Cleanup cached dummy ProductLicense
				//forgetProductLicense(license.getServiceId(), license.getProductId(), license.getInternetName());
				return license;
			} else {
				return null;
			}
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean deleteServiceLicense(String domainId, ProductId product) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			boolean ret = licDao.delete(con, domainId, product.getServiceId(), product.getProductCode()) == 1;
			//TODO delete lease
			if (ret) {
				// Cleanup cached ProductLicense
				//forgetProductLicense(domainId, serviceId, productId);
			}
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	
	
	
	/*
	public List<ServiceLicense> listServiceLicenses(String interneName) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			ArrayList<ServiceLicense> items = new ArrayList<>();
			for (OLicense olic : licDao.selectByInternetName(con, interneName)) {
				items.add(AppManagerUtils.createServiceLicense(olic));
			}
			return items;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ServiceLicense getServiceLicense(String serviceId, String productId, String interneName) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return AppManagerUtils.createServiceLicense(licDao.select(con, serviceId, productId, interneName));
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ServiceLicense addServiceLicense(ServiceLicense license) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			String domainId = domainCache.internetNameToDomainId(license.getInternetName());
			if (domainId == null) throw new WTException("Unable to get domain associated to provided internet-name [{}]", license.getInternetName());
			
			con = wta.getConnectionManager().getConnection();
			OLicense olic = AppManagerUtils.createOLicense(license);
			boolean ret = licDao.insert(con, olic) == 1;
			if (ret) {
				// Cleanup cached dummy ProductLicense
				forgetProductLicense(license.getServiceId(), license.getProductId(), license.getInternetName());
				return license;
			} else {
				return null;
			}
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean deleteServiceLicense(String serviceId, String productId, String interneName) throws WTException {
		LicenseDAO licDao = LicenseDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			boolean ret = licDao.delete(con, serviceId, productId, interneName) == 1;
			if (ret) {
				// Cleanup cached ProductLicense
				forgetProductLicense(serviceId, productId, interneName);
			}
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	/**
	 * Lists domain real roles (those defined as indipendent role).
	 * @param domainId The domain ID.
	 * @return
	 * @throws WTException 
	 */
	public List<Role> listRoles(String domainId) throws WTException {
		RoleDAO dao = RoleDAO.getInstance();
		ArrayList<Role> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			List<ORole> roles = dao.selectByDomain(con, domainId);
			for(ORole erole : roles) items.add(new Role(erole));
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return items;
	}
	
	/**
	 * Lists domain users roles (those coming from a user).
	 * @param domainId The domain ID.
	 * @return
	 * @throws WTException 
	 */
	public List<Role> listUsersRoles(String domainId) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		ArrayList<Role> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			List<OUser> users = dao.selectEnabledByDomain(con, domainId);
			for(OUser user: users) items.add(new Role(user));
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return items;
	}
	
	/**
	 * Lists domain groups roles (those coming from a group).
	 * @param domainId The domain ID.
	 * @return
	 * @throws WTException 
	 */
	public List<Role> listGroupsRoles(String domainId) throws WTException {
		GroupDAO dao = GroupDAO.getInstance();
		ArrayList<Role> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			List<OGroup> groups = dao.selectByDomain(con, domainId);
			for(OGroup group: groups) items.add(new Role(group));
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return items;
	}
	
	public List<AssignedRole> listAssignedRoles(String userUid) throws WTException {
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			return rolassdao.viewAssignedByUser(con, userUid);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Retrieves the domain ID for the specified role.
	 * @param uid
	 * @return
	 * @throws WTException 
	 */
	public String getRoleDomain(String uid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			RoleDAO roldao = RoleDAO.getInstance();
			ORole role = roldao.selectByUid(con, uid);
			if(role != null) return role.getDomainId();
			
			UserDAO usedao = UserDAO.getInstance();
			OUser user = usedao.selectByUid(con, uid);
			if(user != null) return user.getDomainId();
			
			GroupDAO grpdao = GroupDAO.getInstance();
			OGroup group = grpdao.selectByUid(con, uid);
			if(group != null) return group.getDomainId();
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return null;
	}
	
	public RoleEntity getRole(String uid) throws WTException {
		RoleDAO roldao = RoleDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			ORole orole = roldao.selectByUid(con, uid);
			if(orole == null) throw new WTException("Role not found [{0}]", uid);
			
			EntityPermissions perms = extractPermissions(con, uid);
			RoleEntity role = new RoleEntity(orole);
			role.setPermissions(perms.others);
			role.setServicesPermissions(perms.services);
			
			return role;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addRole(RoleEntity role) throws WTException {
		RoleDAO roldao = RoleDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			ORole orole = new ORole();
			orole.setRoleUid(IdentifierUtils.getUUID());
			orole.setDomainId(role.getDomainId());
			orole.setName(role.getName());
			orole.setDescription(role.getDescription());
			roldao.insert(con, orole);
			
			for(ORolePermission perm : role.getPermissions()) {
				doInsertPermission(con, orole.getRoleUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
			}
			for(ORolePermission perm : role.getServicesPermissions()) {
				doInsertPermission(con, orole.getRoleUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
			}
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateRole(RoleEntity role) throws WTException {
		RoleDAO roldao = RoleDAO.getInstance();
		RolePermissionDAO rolperdao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			RoleEntity oldRole = getRole(role.getRoleUid());
			if(oldRole == null) throw new WTException("Role not found [{0}]", role.getRoleUid());
			
			con = WT.getConnection(CoreManifest.ID, false);
			
			ORole orole = new ORole();
			orole.setRoleUid(role.getRoleUid());
			orole.setName(role.getName());
			orole.setDescription(role.getDescription());
			roldao.update(con, orole);
			
			LangUtils.CollectionChangeSet<ORolePermission> changeSet1 = LangUtils.getCollectionChanges(oldRole.getPermissions(), role.getPermissions());
			for(ORolePermission perm : changeSet1.deleted) {
				rolperdao.deleteById(con, perm.getRolePermissionId());
			}
			for(ORolePermission perm : changeSet1.inserted) {
				doInsertPermission(con, oldRole.getRoleUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
			}
			
			LangUtils.CollectionChangeSet<ORolePermission> changeSet2 = LangUtils.getCollectionChanges(oldRole.getServicesPermissions(), role.getServicesPermissions());
			for(ORolePermission perm : changeSet2.deleted) {
				rolperdao.deleteById(con, perm.getRolePermissionId());
			}
			for(ORolePermission perm : changeSet2.inserted) {
				doInsertPermission(con, oldRole.getRoleUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
			}
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteRole(String uid) throws WTException {
		RoleDAO roldao = RoleDAO.getInstance();
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		RolePermissionDAO rolperdao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			roldao.deleteByUid(con, uid);
			rolassdao.deleteByRole(con, uid);
			rolperdao.deleteByRole(con, uid);
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> getComputedRolesAsStringByUser(UserProfileId pid, boolean self, boolean transitive) throws WTException {
		ArrayList<String> uids = new ArrayList<>();
		Set<RoleWithSource> roles = getComputedRolesByUser(pid, self, transitive);
		for(RoleWithSource role : roles) {
			uids.add(role.getRoleUid());
		}
		return uids;
	}
	
	public Set<RoleWithSource> getComputedRolesByUser(UserProfileId pid, boolean self, boolean transitive) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		Connection con = null;
		HashSet<String> roleMap = new HashSet<>();
		LinkedHashSet<RoleWithSource> roles = new LinkedHashSet<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			String userUid = usrm.userToUid(pid);
			
			if(self) {
				UserDAO usedao = UserDAO.getInstance();
				OUser user = usedao.selectByUid(con, userUid);
				roles.add(new RoleWithSource(RoleWithSource.SOURCE_USER, userUid, user.getDomainId(), pid.getUserId(), user.getDisplayName()));
			}
			
			RoleDAO roldao = RoleDAO.getInstance();
			
			// Gets by group
			List<ORole> groles = roldao.selectFromGroupsByUser(con, userUid);
			for(ORole role : groles) {
				if(roleMap.contains(role.getRoleUid())) continue; // Skip duplicates
				roleMap.add(role.getRoleUid());
				roles.add(new RoleWithSource(RoleWithSource.SOURCE_GROUP, role.getRoleUid(), role.getDomainId(), role.getName(), role.getDescription()));
			}
			
			// Gets direct assigned roles
			List<ORole> droles = roldao.selectDirectByUser(con, userUid);
			for(ORole role : droles) {
				if(roleMap.contains(role.getRoleUid())) continue; // Skip duplicates
				roleMap.add(role.getRoleUid());
				roles.add(new RoleWithSource(RoleWithSource.SOURCE_ROLE, role.getRoleUid(), role.getDomainId(), role.getName(), role.getDescription()));
			}
			
			// Get transivite roles (belonging to groups)
			if(transitive) {
				List<ORole> troles = roldao.selectTransitiveFromGroupsByUser(con, userUid);
				for(ORole role : troles) {
					if(roleMap.contains(role.getRoleUid())) continue; // Skip duplicates
					roleMap.add(role.getRoleUid());
					roles.add(new RoleWithSource(RoleWithSource.SOURCE_TRANSITIVE, role.getRoleUid(), role.getDomainId(), role.getName(), role.getDescription()));
				}
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return roles;
	}
	
	public List<ORolePermission> listRolePermissions(String roleUid) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			RolePermissionDAO dao = RolePermissionDAO.getInstance();
			return dao.selectByRoleUid(con, roleUid);
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public EntityPermissions extractPermissions(Connection con, String roleUid) throws WTException {
		RolePermissionDAO rolperdao = RolePermissionDAO.getInstance();
		
		List<ORolePermission> operms = rolperdao.selectByRoleUid(con, roleUid);
		ArrayList<ORolePermission> othersPerms = new ArrayList<>();
		ArrayList<ORolePermission> servicesPerms = new ArrayList<>();
		for(ORolePermission operm : operms) {
			if(operm.getInstance().equals("*")) {
				othersPerms.add(operm);
			} else {
				if(operm.getServiceId().equals(CoreManifest.ID) && operm.getKey().equals("SERVICE") && operm.getAction().equals("ACCESS")) {
					servicesPerms.add(operm);
				}
			}
		}
		
		return new EntityPermissions(othersPerms, servicesPerms);
	}
	
	public UserProfile.PersonalInfo userPersonalInfo(UserProfileId pid) throws WTException {
		synchronized(cacheUserToPersonalInfo) {
			if(!cacheUserToPersonalInfo.containsKey(pid)) {
				UserProfile.PersonalInfo upi = getUserPersonalInfo(pid);
				if(upi == null) throw new WTException("UserPersonalInfo not found [{0}]", pid.toString());
				cacheUserToPersonalInfo.put(pid, upi);
				return upi;
			} else {
				return cacheUserToPersonalInfo.get(pid);
			}
		}
	}
	
	public UserProfile.Data userData(UserProfileId pid) throws WTException {
		synchronized(cacheUserToData) {
			if(!cacheUserToData.containsKey(pid)) {
				final UserProfile.Data ud = getUserData(pid);
				if (ud == null) return null;
				cacheUserToData.put(pid, ud);
				return ud;
			} else {
				return cacheUserToData.get(pid);
			}
		}
	}
	
	public void ensureProfileDomain(String domainId) throws AuthException {
		if (domainId == null) return;
		UserProfileId runPid = RunContext.getRunProfileId();
		if (RunContext.isWebTopAdmin(runPid)) return;
		if (!runPid.hasDomain(domainId)) throw new AuthException("Running profile's domain [{0}] does not match with passed one [{1}]", runPid.getDomainId(), domainId);
	}
	
	public UserProfileId guessUserProfileIdByProfileUsername(String profileUsername) throws WTException {
		UserProfileId iaPid = null;
		try {
			iaPid = new UserProfileId(profileUsername);
		} catch(UnsupportedOperationException ex) {
			return null;
		}
		
		String domainId = domainInternetNameToDomainId(iaPid.getDomain());
		if (domainId == null) return null;
		ensureProfileDomain(domainId);
		return new UserProfileId(domainId, iaPid.getUser());
	}
	
	public UserProfileId guessUserProfileIdByPersonalAddress(String personalAddress) throws WTException {
		UserInfoDAO uiDao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			List<UserId> uids = uiDao.viewByEmail(con, personalAddress);
			if (uids.isEmpty()) return null;
			
			UserId uid = uids.get(0);
			ensureProfileDomain(uid.getDomainId());
			return new UserProfileId(uid.getDomainId(), uid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public UserProfile.Data userDataByEmail(String emailAddress) throws WTException {
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			List<UserId> uids = uidao.viewByEmail(con, emailAddress);
			if (uids.isEmpty()) return null;
			
			UserId uid = uids.get(0);
			UserProfileId pid = new UserProfileId(uid.getDomainId(), uid.getUserId());
			return userData(pid);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public String userToUid(UserProfileId pid) {
		return userToUid(pid,true);
	}
	
	public String userToUid(UserProfileId pid, boolean mandatory) {
		synchronized(lock1) {
			if(!cacheUserToUserUid.containsKey(pid)) {
				if (mandatory)
					throw new WTRuntimeException("[userToUidCache] Cache miss on key {0}", pid.toString());
				return null;
			}
			return cacheUserToUserUid.get(pid);
		}
	}
	
	public UserProfileId uidToUser(String uid) {
		synchronized(lock1) {
			if(!cacheUserUidToUser.containsKey(uid)) throw new WTRuntimeException("[uidToUserCache] Cache miss on key {0}", uid);
			return cacheUserUidToUser.get(uid);
		}
	}
	
	public String groupToUid(UserProfileId pid) {
		synchronized(lock2) {
			if(!cacheGroupToGroupUid.containsKey(pid)) throw new WTRuntimeException("[groupToUidCache] Cache miss on key {0}", pid.toString());
			return cacheGroupToGroupUid.get(pid);
		}
	}
	
	public UserProfileId uidToGroup(String uid) {
		synchronized(lock2) {
			if(!cacheGroupUidToGroup.containsKey(uid)) throw new WTRuntimeException("[uidToGroupCache] Cache miss on key {0}", uid);
			return cacheGroupUidToGroup.get(uid);
		}
	}
	
	private OGroup doGroupInsert(Connection con, String domainId, String groupId, String displayName) throws DAOException, WTException {
		GroupEntity ge = new GroupEntity();
		ge.setGroupId(groupId);
		ge.setDisplayName(displayName);
		return doGroupInsert(con, domainId, ge);
	}
	
	private OGroup doGroupInsert(Connection con, String domainId, GroupEntity group) throws DAOException, WTException {
		GroupDAO dao = GroupDAO.getInstance();
		
		// Insert Group record
		logger.debug("Inserting group... [{}]", group.getGroupId());
		OGroup ogroup = new OGroup();
		ogroup.setDomainId(domainId);
		ogroup.setGroupId(group.getGroupId());
		ogroup.setEnabled(true);
		ogroup.setGroupUid(IdentifierUtils.getUUID());
		ogroup.setDisplayName(group.getDisplayName());
		ogroup.setSecret(null);
		dao.insert(con, ogroup);
		
		logger.debug("Inserting users associations...");
		HashSet<String> usedUserUids = new HashSet<>();
		for (AssignedUser assiUser : group.getAssignedUsers()) {
			final String userUid = userToUid(new UserProfileId(group.getDomainId(), assiUser.getUserId()));
			if (!usedUserUids.contains(userUid)) { // Avoid userUid duplicates
				doInsertUserAssociation(con, userUid, ogroup.getGroupUid());
				usedUserUids.add(userUid);
			}
		}
		
		logger.debug("Inserting roles associations");
		HashSet<String> usedRoleUids = new HashSet<>();
		for(AssignedRole assiRole : group.getAssignedRoles()) {
			final String roleUid = assiRole.getRoleUid();
			if (!usedRoleUids.contains(roleUid)) { // Avoid roles duplicates
				doInsertRoleAssociation(con, ogroup.getGroupUid(), roleUid);
				usedRoleUids.add(roleUid);
			}
		}
		
		// Insert permissions
		logger.debug("Inserting permissions");
		for(ORolePermission perm : group.getPermissions()) {
			doInsertPermission(con, ogroup.getGroupUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
		}
		for(ORolePermission perm : group.getServicesPermissions()) {
			doInsertPermission(con, ogroup.getGroupUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
		}
		
		return ogroup;
	}
	
	private void doGroupUpdate(Connection con, GroupEntity group) throws DAOException, WTException {
		GroupDAO dao = GroupDAO.getInstance();
		UserAssociationDAO uadao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		
		GroupEntity oldGroup = getGroupEntity(con, group.getProfileId());
		if(oldGroup == null) throw new WTException("Group not found [{0}]", group.getProfileId().toString());
		
		logger.debug("Updating group");
		OGroup ogroup = new OGroup();
		ogroup.setDomainId(group.getDomainId());
		ogroup.setGroupId(group.getGroupId());
		ogroup.setDisplayName(group.getDisplayName());
		dao.update(con, ogroup);
		
		logger.debug("Updating users associations");
		LangUtils.CollectionChangeSet<AssignedUser> changeSet1 = LangUtils.getCollectionChanges(oldGroup.getAssignedUsers(), group.getAssignedUsers());
		for(AssignedUser assiUser : changeSet1.deleted) {
			uadao.deleteById(con, assiUser.getUserAssociationId());
		}
		for(AssignedUser assiUser : changeSet1.inserted) {
			final String userUid = userToUid(new UserProfileId(group.getDomainId(), assiUser.getUserId()));
			doInsertUserAssociation(con, userUid, oldGroup.getGroupUid());
		}
		
		logger.debug("Updating roles associations");
		LangUtils.CollectionChangeSet<AssignedRole> changeSet2 = LangUtils.getCollectionChanges(oldGroup.getAssignedRoles(), group.getAssignedRoles());
		for(AssignedRole assiRole : changeSet2.deleted) {
			rolassdao.deleteById(con, assiRole.getRoleAssociationId());
		}
		for(AssignedRole assiRole : changeSet2.inserted) {
			doInsertRoleAssociation(con, oldGroup.getGroupUid(), assiRole.getRoleUid());
		}

		logger.debug("Updating permissions");
		LangUtils.CollectionChangeSet<ORolePermission> changeSet3 = LangUtils.getCollectionChanges(oldGroup.getPermissions(), group.getPermissions());
		for(ORolePermission perm : changeSet3.deleted) {
			rpdao.deleteById(con, perm.getRolePermissionId());
		}
		for(ORolePermission perm : changeSet3.inserted) {
			doInsertPermission(con, oldGroup.getGroupUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
		}

		LangUtils.CollectionChangeSet<ORolePermission> changeSet4 = LangUtils.getCollectionChanges(oldGroup.getServicesPermissions(), group.getServicesPermissions());
		for(ORolePermission perm : changeSet4.deleted) {
			rpdao.deleteById(con, perm.getRolePermissionId());
		}
		for(ORolePermission perm : changeSet4.inserted) {
			doInsertPermission(con, oldGroup.getGroupUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
		}
	}
	
	private AuthUser createAuthUser(UserEntity user) {
		return new AuthUser(user.getUserId(), user.getDisplayName(), user.getFirstName(), user.getLastName(), null);
	}
	
	private OUser doUserInsert(Connection con, ODomain domain, UserEntity user) throws DAOException, WTException {
		UserDAO udao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		
		InternetAddress email = InternetAddressUtils.toInternetAddress(user.getUserId(), domain.getInternetName(), null);
		if(email == null) throw new WTException("Cannot create a valid email address [{0}, {1}]", user.getUserId(), domain.getInternetName());
		
		// Insert User record
		logger.debug("Inserting user");
		OUser ouser = new OUser();
		ouser.setDomainId(domain.getDomainId());
		ouser.setUserId(user.getUserId());
		ouser.setEnabled(user.getEnabled());
		ouser.setUserUid(IdentifierUtils.getUUID());
		ouser.setDisplayName(user.getDisplayName());
		ouser.setSecret(generateSecretKey());
		udao.insert(con, ouser);
		
		// Insert UserInfo record
		logger.debug("Inserting userInfo");
		OUserInfo oui = new OUserInfo();
		oui.setDomainId(domain.getDomainId());
		oui.setUserId(user.getUserId());
		oui.setFirstName(user.getFirstName());
		oui.setLastName(user.getLastName());
		oui.setEmail(email.getAddress());
		uidao.insert(con, oui);
		
		logger.debug("Inserting groups associations");
		HashSet<String> usedGroupUids = new HashSet<>();
		for(AssignedGroup assiGroup : user.getAssignedGroups()) {
			final String groupUid = groupToUid(new UserProfileId(user.getDomainId(), assiGroup.getGroupId()));
			if (!usedGroupUids.contains(groupUid)) {
				// Due to built-in assigned groups, collection of assigned
				// groups can contain duplicates; so skip them.
				doInsertUserAssociation(con, ouser.getUserUid(), groupUid);
				usedGroupUids.add(groupUid);
			}
		}
		
		logger.debug("Inserting roles associations");
		HashSet<String> usedRoleUids = new HashSet<>();
		for(AssignedRole assiRole : user.getAssignedRoles()) {
			final String roleUid = assiRole.getRoleUid();
			if (!usedRoleUids.contains(roleUid)) {
				// Due to built-in assigned roles, collection of assigned
				// roles can contain duplicates; so skip them.
				doInsertRoleAssociation(con, ouser.getUserUid(), roleUid);
				usedRoleUids.add(roleUid);
			}
		}
		
		// Insert permissions
		logger.debug("Inserting permissions");
		for(ORolePermission perm : user.getPermissions()) {
			doInsertPermission(con, ouser.getUserUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
		}
		for(ORolePermission perm : user.getServicesPermissions()) {
			doInsertPermission(con, ouser.getUserUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
		}
		
		return ouser;
	}
	
	private void doUserUpdate(Connection con, UserEntity user) throws DAOException, WTException {
		UserDAO udao = UserDAO.getInstance();
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		UserAssociationDAO uadao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		
		UserEntity oldUser = getUserEntity(con, user.getProfileId());
		if(oldUser == null) throw new WTException("User not found [{0}]", user.getProfileId().toString());
		
		logger.debug("Updating user");
		OUser ouser = new OUser();
		ouser.setDomainId(user.getDomainId());
		ouser.setUserId(user.getUserId());
		ouser.setEnabled(user.getEnabled());
		ouser.setDisplayName(user.getDisplayName());
		udao.updateEnabledDisplayName(con, ouser);

		logger.debug("Updating userInfo");
		OUserInfo ouseri = new OUserInfo();
		ouseri.setDomainId(user.getDomainId());
		ouseri.setUserId(user.getUserId());
		ouseri.setFirstName(user.getFirstName());
		ouseri.setLastName(user.getLastName());
		uidao.updateFirstLastName(con, ouseri);
		
		logger.debug("Updating groups associations");
		LangUtils.CollectionChangeSet<AssignedGroup> changeSet1 = LangUtils.getCollectionChanges(oldUser.getAssignedGroups(), user.getAssignedGroups());
		for(AssignedGroup assiGroup : changeSet1.deleted) {
			uadao.deleteById(con, assiGroup.getUserAssociationId());
		}
		for(AssignedGroup assiGroup : changeSet1.inserted) {
			final String groupUid = groupToUid(new UserProfileId(user.getDomainId(), assiGroup.getGroupId()));
			doInsertUserAssociation(con, oldUser.getUserUid(), groupUid);
		}
		
		logger.debug("Updating roles associations");
		LangUtils.CollectionChangeSet<AssignedRole> changeSet2 = LangUtils.getCollectionChanges(oldUser.getAssignedRoles(), user.getAssignedRoles());
		for(AssignedRole assiRole : changeSet2.deleted) {
			rolassdao.deleteById(con, assiRole.getRoleAssociationId());
		}
		for(AssignedRole assiRole : changeSet2.inserted) {
			doInsertRoleAssociation(con, oldUser.getUserUid(), assiRole.getRoleUid());
		}

		logger.debug("Updating permissions");
		LangUtils.CollectionChangeSet<ORolePermission> changeSet3 = LangUtils.getCollectionChanges(oldUser.getPermissions(), user.getPermissions());
		for(ORolePermission perm : changeSet3.deleted) {
			rpdao.deleteById(con, perm.getRolePermissionId());
		}
		for(ORolePermission perm : changeSet3.inserted) {
			doInsertPermission(con, oldUser.getUserUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
		}

		LangUtils.CollectionChangeSet<ORolePermission> changeSet4 = LangUtils.getCollectionChanges(oldUser.getServicesPermissions(), user.getServicesPermissions());
		for(ORolePermission perm : changeSet4.deleted) {
			rpdao.deleteById(con, perm.getRolePermissionId());
		}
		for(ORolePermission perm : changeSet4.inserted) {
			doInsertPermission(con, oldUser.getUserUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
		}
	}
	
	private void fillDomain(ODomain o, DomainEntity domain) throws WTException {
		o.setDomainId(domain.getDomainId());
		o.setInternetName(domain.getInternetName());
		o.setEnabled(domain.getEnabled());
		o.setDescription(domain.getDescription());
		o.setUserAutoCreation(domain.getUserAutoCreation());
		o.setDirUri(domain.getDirUri().toString());
		
		String scheme = domain.getDirUri().getScheme();
		if (scheme.equals(WebTopDirectory.SCHEME)) {
			o.setDirConnectionSecurity(null);
			o.setDirAdmin(null);
			o.setDirPassword(null);
			
		} else if (scheme.equals(LdapWebTopDirectory.SCHEME)) {
			o.setDirConnectionSecurity(EnumUtils.getName(domain.getDirConnSecurity()));
			o.setDirAdmin(domain.getDirAdmin());
			setDirPassword(o, domain.getDirPassword());
			
		} else if (scheme.equals(LdapDirectory.SCHEME)) {
			o.setDirConnectionSecurity(EnumUtils.getName(domain.getDirConnSecurity()));
			o.setDirAdmin(domain.getDirAdmin());
			setDirPassword(o, domain.getDirPassword());
			
			o.setDirPasswordPolicy(false);
		} else if (scheme.equals(ImapDirectory.SCHEME)) {
			o.setDirConnectionSecurity(EnumUtils.getName(domain.getDirConnSecurity()));
			o.setDirAdmin(null);
			o.setDirPassword(null);
			
		} else if (scheme.equals(SmbDirectory.SCHEME) || scheme.equals(SftpDirectory.SCHEME)) {
			o.setDirConnectionSecurity(null);
			o.setDirAdmin(null);
			o.setDirPassword(null);
			
		} else if (scheme.equals(ADDirectory.SCHEME)) {
			o.setDirConnectionSecurity(EnumUtils.getName(domain.getDirConnSecurity()));
			o.setDirAdmin(domain.getDirAdmin());
			setDirPassword(o, domain.getDirPassword());
			
		} else if (scheme.equals(LdapNethDirectory.SCHEME)) {
			o.setDirConnectionSecurity(EnumUtils.getName(domain.getDirConnSecurity()));
			o.setDirAdmin(domain.getDirAdmin());
			setDirPassword(o, domain.getDirPassword());
		}
		o.setDirCaseSensitive(domain.getDirCaseSensitive());
		if (domain.getDirParameters() instanceof ParamsLdapDirectory) {
			o.setDirParameters(LangUtils.serialize(domain.getDirParameters(), ParamsLdapDirectory.class));
		} else {
			o.setDirParameters(null);
		}
		AppManagerUtils.fillODomain(o, domain.getPasswordPolicies());
	}
	
	private OUserAssociation doInsertUserAssociation(Connection con, String userUid, String groupUid) throws WTException {
		UserAssociationDAO uadao = UserAssociationDAO.getInstance();
		
		OUserAssociation oua = new OUserAssociation();
		oua.setUserAssociationId(uadao.getSequence(con).intValue());
		oua.setUserUid(userUid);
		oua.setGroupUid(groupUid);
		uadao.insert(con, oua);
		return oua;
	}
	
	private ORoleAssociation doInsertRoleAssociation(Connection con, String userUid, String roleUid) throws WTException {
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		
		ORoleAssociation ora = new ORoleAssociation();
		ora.setRoleAssociationId(rolassdao.getSequence(con).intValue());
		ora.setUserUid(userUid);
		ora.setRoleUid(roleUid);
		rolassdao.insert(con, ora);
		return ora;
	}
	
	private ORolePermission doInsertPermission(Connection con, String roleUid, String serviceId, String key, String action, String instance) throws WTException {
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		
		ORolePermission perm = new ORolePermission();
		perm.setRolePermissionId(rpdao.getSequence(con).intValue());
		perm.setRoleUid(roleUid);
		perm.setServiceId(serviceId);
		perm.setKey(key);
		perm.setAction(action);
		perm.setInstance(instance);
		
		rpdao.insert(con, perm);
		return perm;
	}
	
	private UserProfile.Data getUserData(UserProfileId pid) throws WTException {
		OUser ouser = getUser(pid);
		if (ouser == null) return null;
		
		String internetName = WT.getDomainInternetName(pid.getDomainId());
		CoreUserSettings cus = new CoreUserSettings(pid);
		UserProfile.PersonalInfo upi = userPersonalInfo(pid);
		
		DomainAccount internetAccount = new DomainAccount(internetName, pid.getUserId());
		InternetAddress profileIa = InternetAddressUtils.toInternetAddress(pid.getUserId(), internetName, ouser.getDisplayName());
		InternetAddress personalIa = InternetAddressUtils.toInternetAddress(upi.getEmail(), ouser.getDisplayName());
		if (personalIa == null) {
			personalIa = profileIa;
			logger.warn("User does not have a valid email in personal info. Check it! [{}]", pid.toString());
		}
		return new UserProfile.Data(internetAccount, ouser.getDisplayName(), cus.getLanguageTag(), cus.getTimezone(), 
				cus.getStartDay(), cus.getShortDateFormat(), cus.getLongDateFormat(), cus.getShortTimeFormat(), cus.getLongTimeFormat(), 
				profileIa, personalIa);
	}
	
	private OUserInfo createUserInfo(UserProfile.PersonalInfo upi) {
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
	
	private String decDirPassword(String ep) {
		return PasswordUtils.decryptDES(ep, new String(new char[]{'p','a','s','s','w','o','r','d'}));
	}
	
	private String encDirPassword(String dp) {
		return PasswordUtils.encryptDES(dp, new String(new char[]{'p','a','s','s','w','o','r','d'}));
	}
	
	private void setDirPassword(ODomain o, String dp) {
		o.setDirPassword(encDirPassword(dp));
	}
	
	private class CacheDomainInfo extends AbstractBulkCache {
		private HashMap<String, String> publicNameToDomainId = new HashMap<>();
		private HashMap<String, String> domainInternetNameToDomainId = new HashMap<>();
		private HashMap<String, String> domainIdToDomainInternetName = new HashMap<>();
		private HashMap<String, String> publicInternetNameToDomainId = new HashMap<>();
		private HashMap<String, String> domainIdToPublicInternetName = new HashMap<>();
		
		@Override
		protected void internalBuildCache() {
			DomainDAO domDao = DomainDAO.getInstance();
			Connection con = null;
			
			try {
				logger.debug("[DomainInfoCache] Building cache...");
				con = wta.getConnectionManager().getConnection();
				HashMap<String, String> hmPublicNameToDomainId = new HashMap<>();
				HashMap<String, String> hmDomainInternetNameToDomainId = new HashMap<>();
				HashMap<String, String> hmDomainIdToDomainInternetName = new HashMap<>();
				HashMap<String, String> hmPublicInternetNameToDomainId = new HashMap<>();
				HashMap<String, String> hmDomainIdToPublicInternetName = new HashMap<>();
				for (ODomain odomain : domDao.selectEnabled(con)) {
					final String domainId = odomain.getDomainId();
					
					logger.debug("[DomainInfoCache] Working on '{}'", domainId);
					String pubName = domainIdToPublicName(domainId);
					hmPublicNameToDomainId.put(pubName, domainId);
					logger.trace("[DomainInfoCache] {} -> {}", pubName, domainId);
					
					hmDomainInternetNameToDomainId.put(odomain.getInternetName(), domainId);
					logger.trace("[DomainInfoCache] {} -> {}", odomain.getInternetName(), domainId);
					
					hmDomainIdToDomainInternetName.put(domainId, odomain.getInternetName());
					logger.trace("[DomainInfoCache] {} -> {}", domainId, odomain.getInternetName());
					
					String pubInternetName = odomain.getInternetName();
					CoreServiceSettings dss = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, domainId);
					URI publicUri = URIUtils.createURIQuietly(dss.getPublicBaseUrl());
					if (publicUri != null && !StringUtils.isBlank(publicUri.getHost())) {
						pubInternetName = publicUri.getHost();
					}
					
					String oldPubInternetName = hmPublicInternetNameToDomainId.put(pubInternetName, domainId);
					logger.trace("[DomainInfoCache] {} -> {}", pubInternetName, domainId);
					
					hmDomainIdToPublicInternetName.put(domainId, pubInternetName);
					logger.trace("[DomainInfoCache] {} -> {}", domainId, pubInternetName);
					
					if (oldPubInternetName != null) {
						logger.warn("[DomainInfoCache] publicInternetName -> domainId : duplicated association for {}", pubInternetName);
						logger.warn("[DomainInfoCache] [{} -> {}] overridden!", pubInternetName, oldPubInternetName);
					}
				}
				this.publicNameToDomainId = hmPublicNameToDomainId;
				this.domainInternetNameToDomainId = hmDomainInternetNameToDomainId;
				this.domainIdToDomainInternetName = hmDomainIdToDomainInternetName;
				this.publicInternetNameToDomainId = hmPublicInternetNameToDomainId;
				this.domainIdToPublicInternetName = hmDomainIdToPublicInternetName;
				logger.debug("[DomainInfoCache] Cached {} domains", publicNameToDomainId.size());
				
			} catch(Throwable t) {
				DbUtils.closeQuietly(con);
				if (this.getInitCount() == 0) {
					throw new WTRuntimeException(t, "[DomainInfoCache] Unable to build cache");
				} else {
					logger.error("[DomainInfoCache] Unable to build cache", t);
				}
			}
		}

		@Override
		protected void internalCleanupCache() {
			logger.debug("[DomainInfoCache] Cleaning-up cache...");
			this.publicNameToDomainId = new HashMap<>();
			this.domainInternetNameToDomainId = new HashMap<>();
			this.domainIdToDomainInternetName = new HashMap<>();
			this.publicInternetNameToDomainId = new HashMap<>();
			this.domainIdToPublicInternetName = new HashMap<>();
		}
		
		public int getSize() {
			return publicNameToDomainId.size();
		}
		
		public String publicNameToDomainId(String publicName) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.publicNameToDomainId.get(publicName);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainInternetNameToDomainId(String domainInternetName) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.domainInternetNameToDomainId.get(domainInternetName);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainIdToDomainInternetName(String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.domainIdToDomainInternetName.get(domainId);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String publicInternetNameToDomainId(String publicInternetName) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.publicInternetNameToDomainId.get(publicInternetName);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainIdToPublicInternetName(String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.domainIdToPublicInternetName.get(domainId);
			} finally {
				this.unlockRead(stamp);
			}
		}
	}
	
	private void cleanupUserCache() {
		synchronized(cacheUserToData) {
			cacheUserToData.clear();
		}
		synchronized(cacheUserToPersonalInfo) {
			cacheUserToPersonalInfo.clear();
		}
	}
	
	private void addToUserCache(UserProfileId pid, UserProfile.Data userData) {
		synchronized(cacheUserToData) {
			cacheUserToData.put(pid, userData);
		}
	}
	
	private void addToUserCache(UserProfileId pid, UserProfile.PersonalInfo userPersonalInfo) {
		synchronized(cacheUserToPersonalInfo) {
			cacheUserToPersonalInfo.put(pid, userPersonalInfo);
		}
	}
	
	private void removeFromUserCache(UserProfileId pid) {
		synchronized(cacheUserToData) {
			cacheUserToData.remove(pid);
		}
		synchronized(cacheUserToPersonalInfo) {
			cacheUserToPersonalInfo.remove(pid);
		}
	}
	
	private void initUserUidCache() {
		Connection con = null;
		
		try {
			synchronized(lock1) {
				UserDAO dao = UserDAO.getInstance();
				
				con = wta.getConnectionManager().getConnection();
				List<UserUid> uids = dao.viewAllUids(con);
				cleanupUserUidCache();
				for(UserUid uid : uids) {
					addToUserUidCache(uid);
				}
			}
		} catch(SQLException ex) {
			throw new WTRuntimeException(ex, "Unable to init user UID cache");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void cleanupUserUidCache() {
		synchronized(lock1) {
			cacheUserToUserUid.clear();
			cacheUserUidToUser.clear();
		}
	}
	
	private void addToUserUidCache(UserUid uid) {
		synchronized(lock1) {
			UserProfileId pid = new UserProfileId(uid.getDomainId(), uid.getUserId());
			cacheUserToUserUid.put(pid, uid.getUserUid());
			cacheUserUidToUser.put(uid.getUserUid(), pid);
		}
	}
	
	private void removeFromUserUidCache(UserProfileId pid) {
		synchronized(lock1) {
			if(cacheUserToUserUid.containsKey(pid)) {
				String uid = cacheUserToUserUid.remove(pid);
				cacheUserUidToUser.remove(uid);
			}
		}
	}
	
	private void initGroupUidCache() {
		Connection con = null;
		
		try {
			synchronized(lock2) {
				GroupDAO dao = GroupDAO.getInstance();
				
				con = wta.getConnectionManager().getConnection();
				List<GroupUid> uids = dao.viewAllUids(con);
				cleanupGroupUidCache();
				for(GroupUid uid : uids) {
					addToGroupUidCache(uid);
				}
			}
		} catch(SQLException ex) {
			throw new WTRuntimeException(ex, "Unable to init group UID cache");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void cleanupGroupUidCache() {
		synchronized(lock2) {
			cacheGroupToGroupUid.clear();
			cacheGroupUidToGroup.clear();
		}
	}
	
	private void addToGroupUidCache(GroupUid uid) {
		synchronized(lock2) {
			UserProfileId pid = new UserProfileId(uid.getDomainId(), uid.getUserId());
			cacheGroupToGroupUid.put(pid, uid.getUserUid());
			cacheGroupUidToGroup.put(uid.getUserUid(), pid);
		}
	}
	
	private void removeFromGroupUidCache(UserProfileId pid) {
		synchronized(lock2) {
			if(cacheGroupToGroupUid.containsKey(pid)) {
				String uid = cacheGroupToGroupUid.remove(pid);
				cacheGroupUidToGroup.remove(uid);
			}
		}
	}
	
    private void createCyrusUser(String login,String domainId) throws WTCyrusException {
		String host=wta.getSettingsManager().getServiceSetting(domainId, "com.sonicle.webtop.mail", BaseServiceSettings.DEFAULT_PREFIX+"host");
		int port=Integer.parseInt(wta.getSettingsManager().getServiceSetting(domainId, "com.sonicle.webtop.mail", BaseServiceSettings.DEFAULT_PREFIX+"port"));
		String protocol=wta.getSettingsManager().getServiceSetting(domainId, "com.sonicle.webtop.mail", BaseServiceSettings.DEFAULT_PREFIX+"protocol");
		String adminuser=wta.getSettingsManager().getServiceSetting(domainId, "com.sonicle.webtop.mail", "admin.user");
		String adminpass=wta.getSettingsManager().getServiceSetting(domainId, "com.sonicle.webtop.mail", "admin.password");
		Store s=getCyrusStore(host,port,protocol,adminuser,adminpass);
		createCyrusMailbox(login, s);
		setCyrusAcl(login, login, s);
		setCyrusAcl(login, adminuser, s);
		try { s.close(); } catch(Exception exc) { }
    }
    
    private Store getCyrusStore(String host,int port,String protocol,String user,String psw) throws WTCyrusException {
        Properties props = new Properties(wta.getProperties());
		props.setProperty("mail.store.protocol", protocol);
		props.setProperty("mail.store.port", ""+port);
        Session session = Session.getInstance(props, null);
		try {
			Store store = session.getStore(protocol);
			store.connect(host,user,psw);  
			return store;
		} catch(Exception exc) {
			throw new WTCyrusException(exc);
		}
	}
	
    private void createCyrusMailbox(String login, Store store) throws WTCyrusException {
		try {
			char sep=store.getDefaultFolder().getSeparator();
            Folder c=store.getFolder("user"+sep+login);
            if (!c.exists())
                c.create(Folder.HOLDS_FOLDERS);
		} catch(Exception exc) {
			throw new WTCyrusException(exc);
		}
    }
	
    public void setCyrusAcl(String login, String acllogin, Store store) throws WTCyrusException {
        try{
			char sep=store.getDefaultFolder().getSeparator();
            Folder f= store.getFolder("user"+sep+login);
            IMAPFolder folder=(IMAPFolder)f;
            Rights r=new Rights("lrswipcda");
            ACL a=new ACL(acllogin,r);
            folder.addACL(a); 
        } catch(Exception exc){
			throw new WTCyrusException(exc);
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
	
	public static class EntityPermissions {
		public ArrayList<ORolePermission> others;
		public ArrayList<ORolePermission> services;
		
		public EntityPermissions(ArrayList<ORolePermission> others, ArrayList<ORolePermission> services) {
			this.others = others;
			this.services = services;
		}
	}
}
