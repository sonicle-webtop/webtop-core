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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sonicle.commons.AlgoUtils;
import com.sonicle.commons.ClassUtils;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.cache.AbstractBulkCache;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.flags.BitFlagsEnum;
import com.sonicle.commons.time.DateTimeUtils;
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
import com.sonicle.webtop.core.app.events.DomainUpdateEvent;
import com.sonicle.webtop.core.app.events.GroupUpdateEvent;
import com.sonicle.webtop.core.app.events.ResourceAvailabilityChangeEvent;
import com.sonicle.webtop.core.app.events.ResourceUpdateEvent;
import com.sonicle.webtop.core.app.events.UserAvailabilityChangeEvent;
import com.sonicle.webtop.core.app.events.UserUpdateEvent;
import com.sonicle.webtop.core.app.model.AclSubjectGetOption;
import com.sonicle.webtop.core.app.model.DirectoryUser;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.app.model.DomainGetOption;
import com.sonicle.webtop.core.app.model.DomainUpdateOption;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.sdk.EventBase;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.sdk.ResultVoid;
import com.sonicle.webtop.core.app.sdk.WTMultiCauseWarnException;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.sdk.WTPwdPolicyException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.OResource;
import com.sonicle.webtop.core.bol.ORole;
import com.sonicle.webtop.core.bol.ORoleAssociation;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.OUserAssociation;
import com.sonicle.webtop.core.bol.OUserInfo;
import com.sonicle.webtop.core.bol.ProfileIdentifier;
import com.sonicle.webtop.core.bol.VResource;
import com.sonicle.webtop.core.bol.VUserData;
import com.sonicle.webtop.core.model.DomainEntity;
import com.sonicle.webtop.core.model.ParamsLdapDirectory;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
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
import com.sonicle.webtop.core.dal.ResourceDAO;
import com.sonicle.webtop.core.dal.UserAssociationDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.dal.UserInfoDAO;
import com.sonicle.webtop.core.dal.UserSettingDAO;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.model.PublicImage;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceBase;
import com.sonicle.webtop.core.app.model.GenericSubject;
import com.sonicle.webtop.core.model.SubjectPid;
import com.sonicle.webtop.core.model.SubjectSid;
import com.sonicle.webtop.core.app.model.FolderShareOriginFolders;
import com.sonicle.webtop.core.app.model.FolderShareOrigin;
import com.sonicle.webtop.core.app.model.FolderSharing;
import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.GroupBase;
import com.sonicle.webtop.core.app.model.GroupGetOption;
import com.sonicle.webtop.core.app.model.GroupUpdateOption;
import com.sonicle.webtop.core.app.model.HomedThrowable;
import com.sonicle.webtop.core.app.model.PermissionString;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.app.model.ResourcePermissions;
import com.sonicle.webtop.core.app.model.ResourceUpdateOption;
import com.sonicle.webtop.core.app.model.Role;
import com.sonicle.webtop.core.app.model.RoleBase;
import com.sonicle.webtop.core.app.model.RoleGetOption;
import com.sonicle.webtop.core.app.model.RoleUpdateOption;
import com.sonicle.webtop.core.app.model.ServicePermissionString;
import com.sonicle.webtop.core.app.model.ShareOrigin;
import com.sonicle.webtop.core.app.model.Sharing;
import com.sonicle.webtop.core.app.model.SubjectGetOption;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserBase;
import com.sonicle.webtop.core.app.model.UserGetOption;
import com.sonicle.webtop.core.app.model.UserUpdateOption;
import com.sonicle.webtop.core.app.shiro.ShiroUtils;
import com.sonicle.webtop.core.app.shiro.WTRealm;
import com.sonicle.webtop.core.bol.VUser;
import com.sonicle.webtop.core.dal.CustomFieldDAO;
import com.sonicle.webtop.core.dal.CustomPanelDAO;
import com.sonicle.webtop.core.dal.DataSourceDAO;
import com.sonicle.webtop.core.dal.IMChatDAO;
import com.sonicle.webtop.core.dal.IMMessageDAO;
import com.sonicle.webtop.core.dal.LicenseDAO;
import com.sonicle.webtop.core.dal.MasterDataDAO;
import com.sonicle.webtop.core.dal.TagDAO;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
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
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.mail.internet.InternetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class WebTopManager {
	private static final Logger LOGGER = WT.getLogger(WebTopManager.class);
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
		LOGGER.info("Initialized");
		return instance;
	}
	
	private WebTopApp wta = null;
	public static final String INTERNETNAME_LOCAL = "local";
	public static final String SYSADMIN_ROLESID = "__SYSADMIN__"; // NB: Reflect any update to client-side too (see WTPrivate.js) !!!
	public static final String WTADMIN_ROLESID = "__WTADMIN__"; // NB: Reflect any update to client-side too (see WTPrivate.js) !!!
	public static final String IMPERSONATED_USER_ROLESID = "__IMPERSONATED_USER__"; // NB: Reflect any update to client-side too (see WTPrivate.js) !!!
	public static final String SYSADMIN_DOMAINID = "*";
	public static final String SYSADMIN_USERID = "admin";
	//public static final String SYSADMIN_PSTRING = ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "SYSADMIN"), "ACCESS", "*");
	//public static final String WTADMIN_PSTRING = ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), "ACCESS", "*");
	public static final String DOMAINADMIN_USERID = "admin";
	//public static final String GROUPID_ADMINS = "admins";
	public static final String GROUPID_USERS = "users";
	public static final String GROUPID_PEC_ACCOUNTS = "pec-accounts"; //@Deprecated
	public static final String DOMAINADMIN_ROLEID = "domain-admin"; // Role for managing permissions for Domain admin user
	public static final String PECACCOUNT_ROLEID = "pec-account"; // Role for track users for PEC accounts
	public static final Set<String> BUILT_IN_ROLES = Stream.of(DOMAINADMIN_ROLEID, PECACCOUNT_ROLEID).collect(Collectors.toSet());
	public static final Set<String> BUILT_IN_GROUPS = Stream.of(GROUPID_USERS, GROUPID_PEC_ACCOUNTS).collect(Collectors.toSet());
	
	private final CacheDomainInfo domainCache = new CacheDomainInfo();
	private final KeyedReentrantLocks<String> lockSecretGet = new KeyedReentrantLocks<>();
	private final SubjectSidCache subjectSidCache = new SubjectSidCache();
	private final LoadingCache<UserProfileId, UserProfile.PersonalInfo> profileToPersonalInfoCache = Caffeine.newBuilder().build(new ProfilePersonalInfoCacheLoader());
	private final LoadingCache<UserProfileId, UserProfile.Data> profileToDataCache = Caffeine.newBuilder().build(new ProfileDataCacheLoader());
	private final Object foldersLock = new Object();
	
	/**
	 * Used transitively by aliseoweb, vfs
	 * @deprecated use listDomains instead
	 */
	@Deprecated
	public List<ODomain> OLD_listDomains(boolean enabledOnly) throws WTException {
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
	
	/**
	 * Used transitively by aliseoweb
	 * @deprecated use getDomain instead
	 */
	@Deprecated
	public ODomain OLD_getDomain(String domainId) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return domDao.selectById(con, domainId);
			
		} catch(SQLException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * @deprecated used transitively in aliseoweb, drm
	 */
	@Deprecated
	public List<OUser> listUsers(String domainId, boolean enabledOnly) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			if(enabledOnly) {
				return dao.selectEnabledByDomain(con, domainId);
			} else {
				return dao.selectByDomain_OLD(con, domainId);
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Deprecated
	public List<UserProfileId> listUserProfileIdsByEmail(String emailAddress) throws WTException {
		UserInfoDAO uidao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			List<ProfileIdentifier> uids = uidao.viewByEmail(con, emailAddress);
			ArrayList<UserProfileId> items = new ArrayList<>();
			for(ProfileIdentifier uid : uids) {
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
	
	@Deprecated
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
	
	@Deprecated
	public CheckUserResult checkUser(UserProfileId pid) throws WTException {
		return checkUser(pid.getDomainId(), pid.getUserId());
	}
	
	@Deprecated
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
	
	@Deprecated
	public boolean updateUserDisplayName(UserProfileId pid, String displayName) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return dao.updateDisplayNameByProfile(con, pid.getDomainId(), pid.getUserId(), displayName) == 1;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Deprecated
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
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private WebTopManager(WebTopApp wta) {
		this.wta = wta;
		domainCache.init();
		subjectSidCache.init();
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		subjectSidCache.cleanup();
		cleanupProfileCache();
		domainCache.clear();
		wta = null;
		LOGGER.info("Cleaned up");
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
				Domain domain = getDomain(profileId.getDomainId(), DomainGetOption.internalDefaultFlags());
				if (domain == null) throw new WTNotFoundException("Domain not found [{}]", profileId.getDomainId());
				return createAuthenticationDomain(domain);
			}
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid URI");
		}
	}
	
	public AuthenticationDomain createAuthenticationDomain(Domain domain) {
		return new AuthenticationDomain(domain.getDomainId(), 
			domain.getAuthDomainName(), 
			domain.getDirUri(), 
			domain.getDirCaseSensitive(),
			domain.getDirAdmin(), 
			(domain.getDirPassword() != null) ? domain.getDirPassword().toCharArray() : null, 
			domain.getDirConnSecurity(),
			domain.getDirRawParameters()
		);
	}
	
	public AuthenticationDomain createAuthenticationDomain(ODomain domain) throws URISyntaxException {
		return new AuthenticationDomain(domain.getDomainId(), 
				domain.getInternetName(), 
				domain.getDirUri(), 
				domain.getDirCaseSensitive(),
				domain.getDirAdmin(), 
				// When creating from ODomain, password needs to be decrypted!
				(domain.getDirPassword() != null) ? decDirPassword(domain.getDirPassword()).toCharArray() : null, 
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
	
	/**
	 * Converts given Profile ID (userId@internalDomainId) to authentication 
	 * Profile ID in the form: userId@domainInternetName.
	 * @param profileId Profile ID (internal)
	 * @return
	 * @throws WTException 
	 */
	public UserProfileId toAuthProfileId(final UserProfileId profileId) throws WTException {
		Check.notNull(profileId, "profileId");
		String authInternetName = domainIdToAuthDomainName(profileId.getDomainId());
		if (authInternetName == null) throw new WTException("Cannot find internet-name for '{}'", profileId.getDomainId());
		return new UserProfileId(authInternetName, profileId.getUserId());
	}
	
	/**
	 * @deprecated Left here to satisfy compatibility with images lookup in ResourceRequest servlet!
	 */
	@Deprecated
	public String legacyInternetNameToDomain(String internetName) {
		String onlyOneDomainId = domainCache.authDomainNameToDomainIdIfOnlyOne(internetName);
		if (onlyOneDomainId != null) return onlyOneDomainId;
		
		for(int i=2; i<255; i++) {
			final int iOfNDot = StringUtils.lastOrdinalIndexOf(internetName, ".", i);
			final String key = StringUtils.substring(internetName, iOfNDot+1);
			String domainId = domainCache.authDomainNameToDomainId(key);
			if (domainId != null) return domainId;
			if (iOfNDot == -1) break; 
		}
		return null;
	}
	
	public boolean existsDomainId(final String domainId) {
		return domainCache.exists(domainId);
	}
	
	public String domainIdToDomainPublicId(final String domainId) {
		return AlgoUtils.adler32Hex(domainId);
	}
	
	public String domainPublicIdToDomainId(String domainPublicId) {
		return domainCache.publicIdToDomainId(domainPublicId);
	}
	
	/**
	 * Lookup the authentication domain-name from passed domain ID.
	 * @param domainId The domain ID to decode.
	 * @return 
	 */
	public String domainIdToAuthDomainName(final String domainId) {
		if (StringUtils.equals(domainId, SYSADMIN_DOMAINID)) {
			return INTERNETNAME_LOCAL;
		} else {
			return domainCache.domainIdToAuthDomainName(domainId);
		}
	}
	
	/**
	 * Lookup a domain ID from its authentication domain-name.
	 * @param authDomainName The authentication domain-name to find the corresponding domain ID.
	 * @return 
	 */
	public String authDomainNameToDomainId(final String authDomainName) {
		return domainCache.authDomainNameToDomainId(authDomainName);
	}
	
	/**
	 * Lookup the domain-name from passed domain ID, usually the same of above authentication domain-name.
	 * @param domainId The domain ID to decode.
	 * @return 
	 */
	public String domainIdToDomainName(final String domainId) {
		if (StringUtils.equals(domainId, SYSADMIN_DOMAINID)) {
			return INTERNETNAME_LOCAL;
		} else {
			return domainCache.domainIdToDomainName(domainId);
		}
	}
	
	/**
	 * Lookup a domain ID from its domain-name.
	 * @param domainName The domain-name to find the corresponding domain ID.
	 * @return 
	 */
	public String domainNameToDomainId(final String domainName) {
		return domainCache.domainNameToDomainId(domainName);
	}
	
	public String publicFqdnToDomainId(String publicFqdn, boolean strict) {
		if (strict) {
			return domainCache.publicFqdnToDomainId(publicFqdn);
		} else {
			for (int i=0; i<255; i++) {
				String name = publicFqdn;
				if (i > 0) {
					final int iOfNDot = StringUtils.ordinalIndexOf(publicFqdn, ".", i);
					if (iOfNDot == -1) break;
					name = StringUtils.substring(publicFqdn, iOfNDot+1);
				}
				final String domainId = domainCache.publicFqdnToDomainId(name);
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
	
	public String lookupSubjectSidQuietly(final UserProfileId subjectProfileId, final GenericSubject.Type... validTypes) {
		Check.notNull(subjectProfileId, "subjectProfileId");
		return subjectSidCache.getSid(subjectProfileId, validTypes);
	}
	
	public String lookupSubjectSid(final UserProfileId subjectProfileId, final GenericSubject.Type... validTypes) throws WTNotFoundException {
		Check.notNull(subjectProfileId, "subjectProfileId");
		final String uid = subjectSidCache.getSid(subjectProfileId, validTypes);
		if (uid == null) throw new WTNotFoundException("SID not found for '{}'", subjectProfileId);
		return uid;
	}
	
	public UserProfileId lookupSubjectProfileQuietly(final String subjectSid, final GenericSubject.Type... validTypes) {
		Check.notNull(subjectSid, "subjectSid");
		Check.notEmpty(validTypes, "validTypes");
		return subjectSidCache.getPid(subjectSid, validTypes);
	}
	
	public UserProfileId lookupSubjectProfile(final String subjectSid, final GenericSubject.Type... validTypes) throws WTException {
		Check.notNull(subjectSid, "subjectSid");
		Check.notEmpty(validTypes, "validTypes");
		final UserProfileId pid = subjectSidCache.getPid(subjectSid, validTypes);
		if (pid == null) throw new WTException("ProfileId not found for Sid '{}'", subjectSid);
		return pid;
	}
	
	public UserProfile.PersonalInfo lookupProfilePersonalInfo(final UserProfileId profileId, final boolean throwIfNull) throws WTException {
		Check.notNull(profileId, "profileId");
		UserProfile.PersonalInfo pinfo = null;
		if (SYSADMIN_DOMAINID.equals(profileId.getDomainId()) || domainCache.exists(profileId.getDomainId())) {
			pinfo = profileToPersonalInfoCache.get(profileId);
		}
		if (throwIfNull && pinfo == null) throw new WTException("Profile's PersonalInfo not found for '{}'", profileId);
		return pinfo;
	}
	
	public UserProfile.Data lookupProfileData(final UserProfileId profileId, final boolean throwIfNull) throws WTException {
		Check.notNull(profileId, "profileId");
		UserProfile.Data data = null;
		if (SYSADMIN_DOMAINID.equals(profileId.getDomainId()) || domainCache.exists(profileId.getDomainId())) {
			data = profileToDataCache.get(profileId);
		}
		if (throwIfNull && data == null) throw new WTException("Profile's Data not found for '{}'", profileId);
		return data;
	}
	
	public void clearProfileCache(final UserProfileId profileId) {
		Check.notNull(profileId, "profileId");
		profileToDataCache.invalidate(profileId);
		profileToPersonalInfoCache.invalidate(profileId);
	}
	
	/**
	 * Sets System Administrator password
	 * @param newPassword
	 * @throws WTException 
	 */
	public void updateSysAdminPassword(final char[] newPassword) throws WTException {
		Check.notNull(newPassword, "newPassword");
		DirectoryManager dirManager = DirectoryManager.getManager();
		
		try {
			AuthenticationDomain authAd = createSysAdminAuthenticationDomain();
			DirectoryOptions opts = wta.createDirectoryOptions(authAd);
			AbstractDirectory directory = dirManager.getDirectory(authAd.getDirUri().getScheme());
			if (directory == null) throw new WTException("Directory not supported [{}]", authAd.getDirUri().getScheme());
			if (!directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) throw new WTException("Password update not supported [{}]", authAd.getDirUri().getScheme());
			
			directory.updateUserPassword(opts, SYSADMIN_DOMAINID, SYSADMIN_USERID, newPassword);
			
		} catch (URISyntaxException | DirectoryException ex) {
			throw new WTException(ex);
		}
	}
	
	public void initDomainCache() {
		domainCache.init();
	}
	
	public void checkDomains() {
		boolean needsCacheReload = false;
		
		LOGGER.debug("Checking domains...");
		try {
			LOGGER.debug("[{}] Checking home folders...", WebTopManager.SYSADMIN_DOMAINID);
			doDomainInitHomeFolder(WebTopManager.SYSADMIN_DOMAINID);
			
			Set<String> domainIds = listDomainIds(EnabledCond.ANY_STATE);
			for (String domainId : domainIds) {
				LOGGER.debug("[{}] Checking home folders...", domainId);
				doDomainInitHomeFolder(domainId);
				DomainInitResult result = doDomainInitCheck(domainId);
				if (result.subjectCacheUpdated) needsCacheReload = true;
			}
			
		} catch (Exception ex) {
			throw new WTRuntimeException(ex, "Unable to verify domains");
		}
		
		if (needsCacheReload) {
			subjectSidCache.cleanup();
			subjectSidCache.init();
		}
	}
	
	/**
	 * List configured domain IDs according to specified options.
	 * @param enabled
	 * @return
	 * @throws WTException 
	 */
	public Set<String> listDomainIds(final EnabledCond enabled) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return domDao.selectIdsByEnabled(con, enabled);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, Domain> listDomains(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		// This only fill basic domain info: NO directory data will be present!
		
		try {
			con = wta.getConnectionManager().getConnection();
			List<ODomain> odomains = domDao.selectBasicByEnabled(con, enabled);
			Map<String, Domain> items = new LinkedHashMap<>(odomains.size());
			for (ODomain odomain : odomains) {
				items.put(odomain.getDomainId(), AppManagerUtils.fillDomain(new Domain(), odomain));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean checkDomainIdAvailability(final String domainIdToCheck) throws WTException {
		Check.notEmpty(domainIdToCheck, "domainIdToCheck");
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return domDao.idIsAvailable(con, domainIdToCheck);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Domain getDomain(final String domainId, final BitFlags<DomainGetOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(options, "options");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doDomainGet(con, domainId, DomainProcessOpt.fromDomainGetOptions(options));
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public DomainBase.PasswordPolicies getDomainPasswordPolicies(final String domainId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			if (Principal.xisAdminDomain(domainId)) {
				return new DomainBase.PasswordPolicies(null, null, null, null, null, null, null);
				
			} else {
				con = wta.getConnectionManager().getConnection();
				ODomain odomain = domDao.selectPasswordPoliciesById(con, domainId);
				return AppManagerUtils.createDomainPasswordPolicies2(odomain);
			}	
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Result<Domain> addDomain(final String domainId, final DomainBase domain) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(domain, "domain");
		Connection con = null;
		
		Domain newDomain = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			doDomainInsert(con, domainId, domain);
			doDomainInitHomeFolder(domainId);
			DbUtils.commitQuietly(con);
			
			newDomain = doDomainGet(con, domainId, BitFlags.allOf(DomainProcessOpt.class));
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		domainCache.init();
		
		EventBus.PostResult postResult = fireEvent(new DomainUpdateEvent(DomainUpdateEvent.Type.CREATE, domainId), true, true);
		return new Result(newDomain, postResult.getHandlerErrorsCauses());
		/*
		// To create an error for testing purposes...
		ArrayList<HomedThrowable> errors = new ArrayList<>(postResult.getHandlerErrorsCauses());
		errors.add(new HomedThrowable(CoreManifest.ID, new WTException("Error testttttttttttttttttttttt")));
		return new Result(newDomain, errors);
		*/
	}
	
	public ResultVoid initDomain(final String domainId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		ODomain odomain = null;
		try {
			con = wta.getConnectionManager().getConnection();
			odomain = domDao.selectBasicById(con, domainId);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (odomain == null) throw new WTNotFoundException("Domain not found [{}]", domainId);
		DomainInitResult initResult = doDomainInitCheck(domainId);
		
		return new ResultVoid(initResult.errors);
	}
	
	public Result<Domain> updateDomain(final String domainId, final DomainBase domain, final BitFlags<DomainUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(domain, "domain");
		DomainDAO domDao = DomainDAO.getInstance();
		Connection con = null;
		
		ODomain before = null;
		Boolean newEnabled = null;
		Domain updatedDomain = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			before = domDao.selectBasicById(con, domainId);
			DomainUpdateResult res = doDomainUpdate(con, domainId, domain, options.has(DomainUpdateOption.DIRECTORY_PASSWORD), DomainProcessOpt.fromDomainUpdateOptions(options));
			DbUtils.commitQuietly(con);
			
			if (!before.getEnabled().equals(res.odomain.getEnabled())) newEnabled = res.odomain.getEnabled();
			updatedDomain = doDomainGet(con, domainId, BitFlags.allOf(DomainProcessOpt.class));
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		domainCache.init();
		
		EventBus.PostResult postResult = fireEvent(new DomainUpdateEvent(ResourceUpdateEvent.Type.UPDATE, domainId), true, true);
		if (newEnabled != null && before != null) {
			//fireEvent(new DomainAvailabilityChangeEvent(newEnabled, resourcePid, EnumUtils.forSerializedName(before.getCustom1(), ResourceBase.Type.class)));
		}
		return new Result(updatedDomain, postResult.getHandlerErrorsCauses());
	}
	
	public ResultVoid deleteDomain(final String domainId, final boolean updateDirectory) throws WTException {
		Check.notEmpty(domainId, "domainId");
		UserDAO useDao = UserDAO.getInstance();
		ResourceDAO resDao = ResourceDAO.getInstance();
		Connection con = null;
		
		Domain oldDomain = null;
		Set<String> oldUserIds = null;
		Set<String> oldResourcesIds = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			oldDomain = doDomainGet(con, domainId, BitFlags.with(DomainProcessOpt.PROCESS_DIRECTORY));
			if (oldDomain == null) throw new WTNotFoundException("Domain not found [{}]", domainId);
			oldUserIds = useDao.selectIdsByDomainEnabled(con, domainId, EnabledCond.ANY_STATE);
			oldResourcesIds = resDao.selectIdsByDomainEnabled(con, domainId, EnabledCond.ANY_STATE);
			doDomainDelete(con, domainId);
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		domainCache.init();
		subjectSidCache.cleanupByDomain(domainId);
		cleanupProfileCache();
		
		ArrayList<HomedThrowable> errors = new ArrayList<>();
		if (oldUserIds != null) {
			if (updateDirectory) {
				try {
					AuthenticationDomain ad = createAuthenticationDomain(oldDomain);
					AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
					DirectoryOptions opts = wta.createDirectoryOptions(ad);
					if (directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
						Set<String> notFounds = new LinkedHashSet<>();
						for (String userId : oldUserIds) {
							try {
								directory.deleteUser(opts, domainId, userId);
							} catch (DirectoryException ex1) {
								if (isDirectoryNameNotFoundException(ex1)) {
									// Do not throw if error refers to an entry not-found... simply keep track of the user!
									notFounds.add(userId);
								} else {
									throw ex1;
								}
							}
						}
						if (!notFounds.isEmpty()) {
							if (notFounds.size() == oldUserIds.size()) {
								errors.add(new HomedThrowable(CoreManifest.ID, new WTException("No user cleared from Directory")));
							} else {
								errors.add(new HomedThrowable(CoreManifest.ID, new WTException("Some users were not cleared from Directory: {}", notFounds.size())));
								LOGGER.warn("[{}] Some users were not removed from Directory: {}", domainId, StringUtils.join(notFounds, ", "));
							}
						}
					}
				} catch (Exception ex) {
					LOGGER.error("[{}] Error on bulk-cleaning users from Directory", ex, domainId);
					errors.add(new HomedThrowable(CoreManifest.ID, new WTException(ex, "Error during bulk-cleaning users from Directory")));
				}
			}
			/*
			for (String userId : oldUserIds) {
				//TODO: fire event?
			}
			*/
		}
			
		if (oldResourcesIds != null) {
			/*
			for (String resourceId : oldResourceIds) {
				//TODO: fire event?
			}
			*/
		}
		
		EventBus.PostResult postResult = fireEvent(new DomainUpdateEvent(ResourceUpdateEvent.Type.DELETE, domainId), true, true);		
		if (postResult.hasHandlerErrors()) errors.addAll(postResult.getHandlerErrorsCauses());
		
		// Cleanup file-system
		doDomainDeleteHomeFolder(domainId);
		
		return new ResultVoid(errors);
	}
	
	public List<DirectoryUser> listDirectoryUsers(final String domainId) throws WTException {
		UserDAO useDao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			Domain domain = doDomainGet(con, domainId, BitFlags.allOf(DomainProcessOpt.class));
			if (domain == null) throw new WTNotFoundException("Domain not found [{}]", domainId);
			
			AuthenticationDomain ad = createAuthenticationDomain(domain);
			AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
			DirectoryOptions opts = wta.createDirectoryOptions(ad);
			
			Map<String, VUser> vusers = useDao.selectByDomainEnabled(con, domain.getDomainId(), EnabledCond.ANY_STATE);
			ArrayList<DirectoryUser> items = new ArrayList<>();
			if (directory.hasCapability(DirectoryCapability.USERS_READ)) {
				for (AuthUser authUser : directory.listUsers(opts, domain.getDomainId())) {
					items.add(new DirectoryUser(authUser, vusers.get(authUser.userId)));
				}
			} else {
				throw new WTException("Directory not readable");
			}
			return items;
			
		} catch(DirectoryException ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Set<String> listUserIds(final String domainId, final EnabledCond enabled) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(enabled, "enabled");
		UserDAO useDao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return useDao.selectIdsByDomainEnabled(con, domainId, enabled);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, User> listUsers(final String domainId, final EnabledCond enabled) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(enabled, "enabled");
		UserDAO useDao = UserDAO.getInstance();
		Connection con = null;
		
		// This only fill basic user info: NO assignations and permissions related info will be returned!
		
		try {
			con = wta.getConnectionManager().getConnection();
			Collection<VUser> vusers = useDao.selectByDomainEnabled(con, domainId, enabled).values();
			Map<String, User> items = new LinkedHashMap<>(vusers.size());
			for (VUser vuser : vusers) {
				items.put(vuser.getUserId(), AppManagerUtils.fillUser(new User(), vuser));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean checkUserIdAvailability(final String domainId, final String userIdToCheck) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userIdToCheck, "userIdToCheck");
		UserDAO useDao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return useDao.idIsAvailableByDomain(con, domainId, userIdToCheck);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public User getUser(final String domainId, final String userId, final BitFlags<UserGetOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		Check.notNull(options, "options");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doUserGet(con, domainId, userId, UserProcessOpt.fromUserGetOptions(options));
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public String getUserSecret(final String domainId, final String userId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		UserDAO useDao = UserDAO.getInstance();
		UserProfileId userPid = new UserProfileId(domainId, userId);
		
		try {
			lockSecretGet.lock(userPid.toString());
			Connection con = null;
			
			try {
				con = wta.getConnectionManager().getConnection();
				OUser ouser = useDao.selectSecretByProfile(con, domainId, userId);
				if (ouser == null) throw new WTNotFoundException("User not found [{}]", userPid);

				String secret = ouser.getSecret();
				if (StringUtils.isBlank(secret)) {
					secret = OUser.generateSecretKey();				
					if (useDao.updateSecretByProfile(con, domainId, userId, secret) != 1) {
						throw new WTException("Unable to set generated secret [{}]", userPid);
					}
				}
				return secret;
				
			} catch (Exception ex) {
				throw ExceptionUtils.wrapThrowable(ex);
			} finally {
				DbUtils.closeQuietly(con);
			}

		} finally {
			lockSecretGet.unlock(userPid.toString());
		}
	}
	
	public Result<User> addUser(final String domainId, final String userId, final UserBase user, final boolean updateDirectory, final boolean setPassword, final char[] password, final BitFlags<UserUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		Check.notNull(user, "user");
		Check.notNull(options, "options");
		
		// Injects default Group (keep attention to subject ID mode)
		final String defaultGroup;
		if (options.has(UserUpdateOption.SUBJECTS_AS_SID)) {
			defaultGroup = lookupSubjectSid(new UserProfileId(domainId, GROUPID_USERS), GenericSubject.Type.GROUP);
		} else {
			defaultGroup = new UserProfileId(domainId, GROUPID_USERS).toString();
		}
		return addUser(domainId, userId, user, updateDirectory, setPassword, password, options, defaultGroup);
	}
	
	private Result<User> addUser(final String domainId, final String userId, final UserBase user, final boolean updateDirectory, final boolean setPassword, final char[] password, final BitFlags<UserUpdateOption> options, final String defaultGroup) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		Check.notNull(user, "user");
		Check.notNull(options, "options");
		UserProfileId userPid = new UserProfileId(domainId, userId);
		Connection con = null;
		
		User newUser = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			Domain domain = doDomainGet(con, userPid.getDomainId(), BitFlags.allOf(DomainProcessOpt.class));
			if (domain == null) throw new WTException("Domain not found [{}]", userPid.getDomainId());
			AuthenticationDomain ad = createAuthenticationDomain(domain);
			
			String userSid = null; // Internally auto-generated
			UserInsertResult result = null;
			if (updateDirectory) {
				AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
				DirectoryOptions opts = wta.createDirectoryOptions(ad);
				
				if (directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
					if (!directory.validateUsername(opts, userPid.getUserId())) {
						throw new WTException("Username does not satisfy directory requirements [{}]", ad.getDirUri().getScheme());
					}
				}
				char[] appliedPassword = null;
				if (setPassword && directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
					wta.setDirectoryOptionsPasswordPolicies(ad, opts, domain.getPasswordPolicies());
					if (password == null) {
						appliedPassword = directory.generatePassword(opts);
					} else {
						appliedPassword = password;
						int ret = directory.validatePasswordPolicy(opts, userPid.getUserId(), appliedPassword);
						if (ret != 0) {
							throw new WTPwdPolicyException(ret, "Password does not satisfy directory policy [{}, {}]", ad.getDirUri().getScheme(), ret);
						}
					}
				}
				
				result = doUserInsert(con, userPid.getDomainId(), userPid.getUserId(), userSid, user, UserProcessOpt.fromUserUpdateOptions(options), defaultGroup);
				
				// Insert user in directory (if necessary)
				if (directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
					LOGGER.debug("Adding user into directory...");
					try {
						final AuthUser authUser = new AuthUser(userPid.getUserId(), result.ouser.getDisplayName(), result.ouserinfo.getFirstName(), result.ouserinfo.getLastName(), null);
						directory.addUser(opts, userPid.getDomainId(), authUser);
					} catch (EntryException ex1) {
						LOGGER.warn("Insertion skipped: user already exists [{}]", userPid.getUserId());
					}
				}
				if (setPassword && directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
					LOGGER.debug("Setting user password");
					directory.updateUserPassword(opts, userPid.getDomainId(), userPid.getUserId(), appliedPassword);
					new CoreUserSettings(wta.getSettingsManager(), userPid).setPasswordLastChange(DateTimeUtils.now());
				}
				
			} else {
				result = doUserInsert(con, userPid.getDomainId(), userPid.getUserId(), userSid, user, UserProcessOpt.fromUserUpdateOptions(options), defaultGroup);
			}
			
			DbUtils.commitQuietly(con);
			
			subjectSidCache.add(result.ouser.getProfileId(), result.ouser.getUserSid(), GenericSubject.Type.USER);
			newUser = doUserGet(con, userPid.getDomainId(), userPid.getUserId(), UserProcessOpt.fromUserUpdateOptions(options));
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Fire events
		EventBus.PostResult postResult = fireEvent(new UserUpdateEvent(UserUpdateEvent.Type.CREATE, userPid), true, true);
		//TODO: fire UserAvailabilityChangeEvent based on enabled change
		// @Deprecated
		// -> START Backward compatibility section: performs legacy actions
		List<Throwable> errors = wta.getServiceManager().invokeOnUserAdded(userPid);
		if (!errors.isEmpty()) throw new WTMultiCauseWarnException(errors, "Errors in user related listeners");
		// <- END Backward compatibility section
		
		return new Result(newUser, postResult.getHandlerErrorsCauses());
	}
	
	public ResultVoid updateUser(final String domainId, final String userId, final UserBase user, final BitFlags<UserUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		Check.notNull(user, "user");
		UserProfileId userPid = new UserProfileId(domainId, userId);
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			doUserUpdate(con, userPid.getDomainId(), userPid.getUserId(), user, UserProcessOpt.fromUserUpdateOptions(options));
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
 			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Fire events
		EventBus.PostResult postResult = fireEvent(new UserUpdateEvent(UserUpdateEvent.Type.UPDATE, userPid), true, true);
		
		return new ResultVoid(postResult.getHandlerErrorsCauses());
	}
	
	public void updateUserStatus(final String domainId, final String userId, final boolean enabled) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		UserProfileId userPid = new UserProfileId(domainId, userId);
		UserDAO useDao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			boolean ret = useDao.updateEnabledByProfile(con, domainId, userId, enabled) == 1;
			if (!ret) throw new WTNotFoundException("User not found [{}]", userPid);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Fire events
		EventBus.PostResult postResult = fireEvent(new UserAvailabilityChangeEvent(enabled, userPid), false, true);
	}
	
	public void updateUserPassword(final String domainId, final String userId, final char[] oldPassword, final char[] newPassword) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		Check.notNull(newPassword, "newPassword");
		UserProfileId userPid = new UserProfileId(domainId, userId);
		AuthenticationDomain ad = createAuthenticationDomain(userPid);
		
		try {
			AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
			if (!directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) {
				throw new WTException("Directory has no write capability");
			}
			
			DirectoryOptions opts = wta.createDirectoryOptions(ad);
			DomainBase.PasswordPolicies pwdPolicies = getDomainPasswordPolicies(userPid.getDomainId());
			wta.setDirectoryOptionsPasswordPolicies(ad, opts, pwdPolicies);
			int ret = directory.validatePasswordPolicy(opts, userPid.getUserId(), newPassword);
			if (ret == 0 && oldPassword != null && pwdPolicies.getAvoidOldSimilarity()) {
				int similarityThres = 5;
				if (StringUtils.getLevenshteinDistance(new String(oldPassword), new String(newPassword)) < similarityThres) ret = 41;
			}
			if (ret != 0) {
				throw new WTPwdPolicyException(ret, "Password does not satisfy directory policy [{}]", ret);
			}
			
			if (oldPassword != null) {
				directory.updateUserPassword(opts, userPid.getDomainId(), userPid.getUserId(), oldPassword, newPassword);
			} else {
				directory.updateUserPassword(opts, userPid.getDomainId(), userPid.getUserId(), newPassword);
			}

			CoreUserSettings cus = new CoreUserSettings(userPid);
			cus.setPasswordLastChange(DateTimeUtils.now());
			cus.setPasswordForceChange(false);
			
		} catch (DirectoryException ex) {
			if (isDirectoryNameNotFoundException(ex)) {
				throw new WTNotFoundException(ex, "User not found [{}]", userPid);
			} else {
				throw new WTException(ex, "Directory error");
			}
		}
	}
	
	public ResultVoid deleteUser(final String domainId, final String userId, final boolean updateDirectory) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		UserProfileId userPid = new UserProfileId(domainId, userId);
		Connection con = null;
		
		String userSid = subjectSidCache.getSid(userPid, GenericSubject.Type.USER);
		if (userSid == null) throw new WTNotFoundException("User not found [{}]", userPid);
		
		// Make sure that some data are in cache before permanent user deletion (useful for events below)
		lookupProfilePersonalInfo(userPid, false);
		lookupProfileData(userPid, false);
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			LOGGER.debug("Deleting user '{}'", userId);
			doUserDelete(con, userPid.getDomainId(), userPid.getUserId());
			
			if (updateDirectory) {
				ODomain odomain = doODomainGet(con, userPid.getDomainId());
				if (odomain == null) throw new WTException("Domain not found [{}]", userPid.getDomainId());
				AuthenticationDomain ad = createAuthenticationDomain(odomain);
				AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
				DirectoryOptions opts = wta.createDirectoryOptions(ad);
				
				if (directory.hasCapability(DirectoryCapability.USERS_WRITE)) {
					directory.deleteUser(opts, userPid.getDomainId(), userPid.getUserId());
				}
			}
			
			DbUtils.commitQuietly(con);
			
		} catch (DirectoryException ex) {
			DbUtils.rollbackQuietly(con);
			if (isDirectoryNameNotFoundException(ex)) {
				throw new WTNotFoundException(ex, "User not found [{}]", userPid);
			} else {
				throw ExceptionUtils.wrapThrowable(ex);
			}
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		wta.getLicenseManager().revokeLicenseLease(userPid);
		
		// Fire events
		EventBus.PostResult postResult = fireEvent(new UserUpdateEvent(UserUpdateEvent.Type.DELETE, userPid), true, true);
		// -> START Backward compatibility section: performs legacy actions
		wta.getServiceManager().invokeOnUserRemoved(userPid);
		// <- END Backward compatibility section
		// Clear settings
		wta.getSettingsManager().deleteUserSettings(userPid);
		// Clear any cached data
		subjectSidCache.remove(userPid, GenericSubject.Type.USER);
		clearProfileCache(userPid);
		
		return new ResultVoid(postResult.getHandlerErrorsCauses());
	}
	
	public boolean isUserPasswordChangeNeeded(final String domainId, final String userId, final char[] password) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		UserProfileId userPid = new UserProfileId(domainId, userId);
		if (Principal.xisAdmin(userPid.getDomainId(), userPid.getUserId())) return false;
		
		AuthenticationDomain ad = createAuthenticationDomain(userPid);
		AbstractDirectory directory = getAuthDirectory(ad.getDirUri());
		if (!directory.hasCapability(DirectoryCapability.PASSWORD_WRITE)) return false;
		
		DomainBase.PasswordPolicies pwdPolicies = getDomainPasswordPolicies(userPid.getDomainId());
		if (pwdPolicies.getVerifyAtLogin() || pwdPolicies.getExpiration() != null) {
			if (pwdPolicies.getVerifyAtLogin()) {
				DirectoryOptions opts = wta.createDirectoryOptions(ad);
				wta.setDirectoryOptionsPasswordPolicies(ad, opts, pwdPolicies);
				int ret = directory.validatePasswordPolicy(opts, userPid.getUserId(), password);
				if (ret != 0) return true;
			}
			
			if (pwdPolicies.getExpiration() != null) {
				CoreUserSettings cus = new CoreUserSettings(userPid);
				DateTime lastChange = cus.getPasswordLastChange();
				// NB: No last-change timestamp means password change needed!
				if (lastChange == null || DateTimeUtils.datesBetween(lastChange, DateTimeUtils.now().toDateTime(DateTimeZone.UTC)) > pwdPolicies.getExpiration()) return true;
			}
		}
		
		CoreUserSettings cus = new CoreUserSettings(userPid);
		if (cus.getPasswordForceChange()) return true;
		return false;
	}
	
	public int bulkUpdatePersonalEmailDomain(final String domainId, final Collection<String> userIds, String newPersonalEmailDomain) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(userIds, "userIds");
		UserInfoDAO uiDao = UserInfoDAO.getInstance();
		Connection con = null;
		
		int ret = -1;
		try {
			if (userIds.isEmpty()) return -1;
			con = wta.getConnectionManager().getConnection(false);
			
			String newEmailDomain = "@" + StringUtils.removeStart(newPersonalEmailDomain, "@");
			ret = uiDao.updateEmailDomainByProfiles(con, domainId, userIds, newEmailDomain);
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Clear any cached data
		for (String userId : userIds) clearProfileCache(new UserProfileId(domainId, userId));
		
		return ret;
	}
	
	public Set<String> listResourceIds(final String domainId, final EnabledCond enabled) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(enabled, "enabled");
		ResourceDAO resDao = ResourceDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return resDao.selectIdsByDomainEnabled(con, domainId, enabled);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, Resource> listResources(final String domainId, final EnabledCond enabled) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(enabled, "enabled");
		ResourceDAO resDao = ResourceDAO.getInstance();
		Connection con = null;
		
		// This only fill basic user info: NO assignations and permissions related info will be returned!
		
		try {
			con = wta.getConnectionManager().getConnection();
			List<VResource> vres = resDao.selectByDomainEnabled(con, domainId, enabled);
			Map<String, Resource> items = new LinkedHashMap<>(vres.size());
			for (VResource vre : vres) {
				items.put(vre.getUserId(), AppManagerUtils.fillResource(new Resource(), vre));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean checkResourceIdAvailability(final String domainId, final String resourceIdToCheck) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceIdToCheck, "resourceIdToCheck");
		ResourceDAO resDao = ResourceDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return resDao.idIsAvailableByDomain(con, domainId, resourceIdToCheck);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Resource getResource(final String domainId, final String resourceId, final BitFlags<ResourceGetOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceId, "resourceId");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doResourceGet(con, domainId, resourceId, ResourceProcessOpt.fromResourceGetOptions(options));
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean getResourceEnabled(final String domainId, final String resourceId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceId, "resourceId");
		ResourceDAO resDao = ResourceDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			Boolean enabled = resDao.selectEnabledByProfile(con, domainId, resourceId);
			if (enabled == null) throw new WTNotFoundException("Resource not found [{}@{}]", resourceId, domainId);
			return enabled;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ResourcePermissions getResourcePermissions(final String domainId, final String resourceId, final boolean subjectsAsSID) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceId, "resourceId");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doResourcePermissionsGet(con, domainId, resourceId, subjectsAsSID);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Result<Resource> addResource(final String domainId, final String resourceId, final ResourceBase resource, final BitFlags<ResourceUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceId, "resourceId");
		Check.notNull(resource, "resource");
		UserProfileId resourcePid = new UserProfileId(domainId, resourceId);
		Connection con = null;
		
		Resource newResource = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			String resourceSid = null; // Internally auto-generated
			ResourceUpdateResult res = doResourceInsert(con, resourcePid.getDomainId(), resourcePid.getUserId(), resourceSid, resource, ResourceProcessOpt.fromResourceUpdateOptions(options));
			DbUtils.commitQuietly(con);
			
			if (res.changedPermissionSids != null) clearAuthorizationInfo(domainId, res.changedPermissionSids, options.has(ResourceUpdateOption.SUBJECTS_AS_SID));
			subjectSidCache.add(res.ouser.getProfileId(), res.ouser.getUserSid(), GenericSubject.Type.RESOURCE);
			newResource = doResourceGet(con, resourcePid.getDomainId(), resourcePid.getUserId(), ResourceProcessOpt.fromResourceUpdateOptions(options));
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		EventBus.PostResult postResult = fireEvent(new ResourceUpdateEvent(ResourceUpdateEvent.Type.CREATE, resourcePid), true, true);
		return new Result(newResource, postResult.getHandlerErrorsCauses());
	}
	
	public ResultVoid updateResource(final String domainId, final String resourceId, final ResourceBase resource, final BitFlags<ResourceUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceId, "resourceId");
		Check.notNull(resource, "resource");
		ResourceDAO resDao = ResourceDAO.getInstance();
		UserProfileId resourcePid = new UserProfileId(domainId, resourceId);
		Connection con = null;
		
		VUserData before = null;
		Boolean newEnabled = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			before = resDao.viewDataByProfile(con, domainId, resourceId);
			ResourceUpdateResult res = doResourceUpdate(con, resourcePid.getDomainId(), resourcePid.getUserId(), resource, ResourceProcessOpt.fromResourceUpdateOptions(options));
			DbUtils.commitQuietly(con);
			
			if (res.changedPermissionSids != null) clearAuthorizationInfo(domainId, res.changedPermissionSids, options.has(ResourceUpdateOption.SUBJECTS_AS_SID));
			if (!before.getEnabled().equals(res.ouser.getEnabled())) newEnabled = res.ouser.getEnabled();
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		EventBus.PostResult postResult = fireEvent(new ResourceUpdateEvent(ResourceUpdateEvent.Type.UPDATE, resourcePid), true, true);
		if (newEnabled != null && before != null) {
			fireEvent(new ResourceAvailabilityChangeEvent(newEnabled, resourcePid, EnumUtils.forSerializedName(before.getCustom1(), ResourceBase.Type.class)));
		}
		return new ResultVoid(postResult.getHandlerErrorsCauses());
	}
	
	public void updateResourceAvailability(final String domainId, final String resourceId, final boolean enabled) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceId, "resourceId");
		UserProfileId resourcePid = new UserProfileId(domainId, resourceId);
		ResourceDAO resDao = ResourceDAO.getInstance();
		Connection con = null;
		
		VUserData before = null;
		Boolean newEnabled = null;
		try {
			con = wta.getConnectionManager().getConnection();
			before = resDao.viewDataByProfile(con, domainId, resourceId);
			boolean ret = resDao.updateEnabledByProfile(con, domainId, resourceId, enabled) == 1;
			if (!ret) throw new WTNotFoundException("Resource not found [{}]", resourcePid);
			if (!before.getEnabled().equals(enabled)) newEnabled = enabled;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (newEnabled != null && before != null) {
			fireEvent(new ResourceAvailabilityChangeEvent(newEnabled, resourcePid, EnumUtils.forSerializedName(before.getCustom1(), ResourceBase.Type.class)));
		}
	}
	
	public ResultVoid deleteResource(final String domainId, final String resourceId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(resourceId, "resourceId");
		UserProfileId resourcePid = new UserProfileId(domainId, resourceId);
		Connection con = null;
		
		// Make sure that some data are in cache before permanent resource deletion (useful for events below)
		lookupProfilePersonalInfo(resourcePid, false);
		lookupProfileData(resourcePid, false);
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			doResourceDelete(con, resourcePid.getDomainId(), resourcePid.getUserId());
			
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		EventBus.PostResult postResult = fireEvent(new ResourceUpdateEvent(ResourceUpdateEvent.Type.DELETE, resourcePid), true, true);
		subjectSidCache.remove(resourcePid, GenericSubject.Type.RESOURCE);
		clearProfileCache(resourcePid); // Clear any cached data
		
		return new ResultVoid(postResult.getHandlerErrorsCauses());
	}
	
	public Set<String> listGroupIds(final String domainId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		GroupDAO grpDao = GroupDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return grpDao.selectIdsByDomain(con, domainId);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, Group> listGroups(final String domainId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		GroupDAO grpDao = GroupDAO.getInstance();
		Connection con = null;
		
		// This only fill basic user info: NO assignations and permissions related info will be returned!
		
		try {
			con = wta.getConnectionManager().getConnection();
			List<OGroup> ogroups = grpDao.selectByDomain(con, domainId);
			Map<String, Group> items = new LinkedHashMap<>(ogroups.size());
			for (OGroup ogroup : ogroups) {
				items.put(ogroup.getUserId(), AppManagerUtils.fillGroup(new Group(BUILT_IN_GROUPS.contains(ogroup.getUserId())), ogroup));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean checkGroupIdAvailability(final String domainId, final String groupIdToCheck) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(groupIdToCheck, "groupIdToCheck");
		GroupDAO grpDao = GroupDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return grpDao.idIsAvailableByDomain(con, domainId, groupIdToCheck);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Group getGroup(final String domainId, final String groupId, final BitFlags<GroupGetOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(groupId, "groupId");
		Check.notNull(options, "options");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doGroupGet(con, domainId, groupId, GroupProcessOpt.fromGroupGetOptions(options));
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Result<Group> addGroup(final String domainId, final String groupId, final GroupBase group, final BitFlags<GroupUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(groupId, "groupId");
		Check.notNull(group, "group");
		Check.notNull(options, "options");
		UserProfileId groupPid = new UserProfileId(domainId, groupId);
		Connection con = null;
		
		Group newGroup = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			String groupSid = null; // Internally auto-generated
			GroupInsertResult result = doGroupInsert(con, groupPid.getDomainId(), groupPid.getUserId(), groupSid, group, GroupProcessOpt.fromGroupUpdateOptions(options));
			
			DbUtils.commitQuietly(con);
			
			subjectSidCache.add(result.ogroup.getProfileId(), result.ogroup.getGroupSid(), GenericSubject.Type.GROUP);
			newGroup = doGroupGet(con, groupPid.getDomainId(), groupPid.getUserId(), GroupProcessOpt.fromGroupUpdateOptions(options));
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Fire events
		EventBus.PostResult postResult = fireEvent(new GroupUpdateEvent(GroupUpdateEvent.Type.CREATE, groupPid), true, true);
		
		return new Result(newGroup, postResult.getHandlerErrorsCauses());
	}
	
	public ResultVoid updateGroup(final String domainId, final String groupId, final GroupBase group, final BitFlags<GroupUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(groupId, "groupId");
		Check.notNull(group, "group");
		Check.notNull(options, "options");
		UserProfileId groupPid = new UserProfileId(domainId, groupId);
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			doGroupUpdate(con, groupPid.getDomainId(), groupPid.getUserId(), group, GroupProcessOpt.fromGroupUpdateOptions(options));
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Fire events
		EventBus.PostResult postResult = fireEvent(new GroupUpdateEvent(GroupUpdateEvent.Type.UPDATE, groupPid), true, true);
		
		return new ResultVoid(postResult.getHandlerErrorsCauses());
	}
	
	public ResultVoid deleteGroup(final String domainId, final String groupId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(groupId, "groupId");
		UserProfileId groupPid = new UserProfileId(domainId, groupId);
		Connection con = null;
		
		try {
			if (BUILT_IN_GROUPS.contains(groupId)) throw new WTException("Built-in group cannot be removed");
			con = wta.getConnectionManager().getConnection(false);
			LOGGER.debug("Deleting group '{}'", groupPid.getUserId());
			doGroupDelete(con, groupPid.getDomainId(), groupPid.getUserId());
			
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		// Fire events
		EventBus.PostResult postResult = fireEvent(new GroupUpdateEvent(GroupUpdateEvent.Type.DELETE, groupPid), true, true);
		
		return new ResultVoid(postResult.getHandlerErrorsCauses());
	}
	
	public Map<String, Role> listRoles(final String domainId) throws WTException {
		Check.notEmpty(domainId, "domainId");
		RoleDAO rolDao = RoleDAO.getInstance();
		Connection con = null;
		
		// This only fill basic user info: NO assignations and permissions related info will be returned!
		
		try {
			con = wta.getConnectionManager().getConnection();
			List<ORole> oroles = rolDao.selectByDomain(con, domainId);
			Map<String, Role> items = new LinkedHashMap<>(oroles.size());
			for (ORole orole : oroles) {
				items.put(orole.getRoleSid(), AppManagerUtils.fillRole(new Role(false), orole));
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean checkRoleIdAvailability(final String domainId, final String roleIdToCheck) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(roleIdToCheck, "roleIdToCheck");
		RoleDAO rolDao = RoleDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return rolDao.idIsAvailableByDomain(con, domainId, roleIdToCheck);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Role getRole(final String domainId, final String roleSid, final BitFlags<RoleGetOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(roleSid, "roleSid");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return doRoleGet(con, domainId, roleSid, RoleProcessOpt.fromRoleGetOptions(options));
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Result<Role> addRole(final String domainId, final String roleId, final RoleBase role, final BitFlags<RoleUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(roleId, "roleId");
		Check.notNull(role, "role");
		UserProfileId rolePid = new UserProfileId(domainId, roleId);
		Connection con = null;
		
		Role newRole = null;
		try {
			con = wta.getConnectionManager().getConnection(false);
			
			String roleSid = null; // Internally auto-generated
			RoleInsertResult result = doRoleInsert(con, rolePid.getDomainId(), rolePid.getUserId(), roleSid, role, RoleProcessOpt.fromRoleUpdateOptions(options));
			
			DbUtils.commitQuietly(con);
			
			subjectSidCache.add(result.orole.getProfileId(), result.orole.getRoleSid(), GenericSubject.Type.ROLE);
			newRole = doRoleGet(con, rolePid.getDomainId(), result.orole.getRoleSid(), RoleProcessOpt.fromRoleUpdateOptions(options));
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		return new Result(newRole);
	}
	
	public ResultVoid updateRole(final String domainId, final String roleSid, final RoleBase role, final BitFlags<RoleUpdateOption> options) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(roleSid, "roleSid");
		Check.notNull(role, "role");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			doRoleUpdate(con, domainId, roleSid, role, RoleProcessOpt.fromRoleUpdateOptions(options));
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		return new ResultVoid();
	}
	
	public ResultVoid deleteRole(final String domainId, final String roleSid) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(roleSid, "roleSid");
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(false);
			LOGGER.debug("Deleting role '{}'", roleSid);
			doRoleDelete(con, domainId, roleSid);
			
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		return new ResultVoid();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
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
	
	
	
	public List<PublicImage> listDomainPublicImages(String domainId) throws WTException {
		String path = wta.getFileSystem().getImagesPath(domainId);
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
	
	
	
	
	
	
	
	
	
	/*
	public Set<GenericSubject> expandSubjectsToUsers2(final String domainId, final Collection<String> subjects, final boolean subjectsArePids, final boolean returnPids, boolean transitive) throws WTException {
		Set<GenericSubject> items = new LinkedHashSet<>();
		
		BitFlags<SubjectGetOption> options = BitFlags.with(SubjectGetOption.USERS, SubjectGetOption.GROUPS);
		if (subjectsArePids) options.set(SubjectGetOption.PID_AS_KEY);
		Map<String, GenericSubject> map = listSubjects(domainId, options);
		
		for (String subject : subjects) {
			GenericSubject gs = map.get(subject);
			if (gs != null) items.add(gs);
		}
		return items;
	}
	*/
	
	public Set<UserProfileId> expandSubjectsToUserProfiles(final String domainId, final Collection<String> subjects, final boolean subjectsAsSID) throws WTException {
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		Connection con = null;

		try {
			con = wta.getConnectionManager().getConnection();
			
			Set<UserProfileId> items = new LinkedHashSet<>();
			for (String subject : subjects) {
				String subjectSid = null;
				if (!subjectsAsSID) {
					final UserProfileId pid = UserProfileId.parseQuielty(subject);
					if (pid != null) subjectSid = subjectSidCache.getSid(pid, GenericSubject.Type.GROUP, GenericSubject.Type.USER);
				} else {
					subjectSid = subject;
				}

				if (subjectSid == null) {
					LOGGER.trace("");
					continue;
				}

				final GenericSubject.Type subjectType = subjectSidCache.getSidType(subjectSid);
				if (GenericSubject.Type.USER.equals(subjectType)) { // Already a User, use as is...
					final UserProfileId pid;
					if (!subjectsAsSID) {
						pid = UserProfileId.parseQuielty(subject); // subject was already parsed as Pid successfully
					} else {
						pid = subjectSidCache.getPid(subjectSid, GenericSubject.Type.USER);
					}
					if (pid != null && pid.hasDomain(domainId)) items.add(pid);
					
				} else if (GenericSubject.Type.GROUP.equals(subjectType)) { // Subject is group: expand to users...
					Set<String> userSids = uasDao.viewUserSidsByGroup(con, subjectSid);
					for (String userSid : userSids) {
						final UserProfileId pid = subjectSidCache.getPid(userSid, GenericSubject.Type.USER);
						if (pid != null && pid.hasDomain(domainId)) items.add(pid);
					}
				}
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, GenericSubject> listSubjects(final String domainId, final BitFlags<SubjectGetOption> options) throws WTException {
		UserDAO useDao = UserDAO.getInstance();
		ResourceDAO resDao = ResourceDAO.getInstance();
		GroupDAO groDao = GroupDAO.getInstance();
		RoleDAO rolDao = RoleDAO.getInstance();
		Connection con = null;
		
		try {
			boolean sidsAsKeys = !options.has(SubjectGetOption.PID_AS_KEY);
			con = wta.getConnectionManager().getConnection();
			
			LinkedHashMap<String, GenericSubject> items = new LinkedHashMap<>();
			if (options.has(SubjectGetOption.USERS)) {
				for (OUser ouser: useDao.selectEnabledByDomain(con, domainId)) {
					items.put(sidsAsKeys ? ouser.getUserSid() : ouser.getUserId(), new GenericSubject(ouser));
				}
			}
			if (options.has(SubjectGetOption.RESOURCES)) {
				for (VResource vres: resDao.selectByDomainEnabled(con, domainId, EnabledCond.ENABLED_ONLY)) {
					items.put(sidsAsKeys ? vres.getResourceSid() : vres.getResourceId(), new GenericSubject(vres));
				}
			}
			if (options.has(SubjectGetOption.GROUPS)) {
				for (OGroup ogroup: groDao.selectByDomain(con, domainId)) {
					items.put(sidsAsKeys ? ogroup.getGroupSid() : ogroup.getGroupId(), new GenericSubject(ogroup));
				}
			}
			if (options.has(SubjectGetOption.ROLES)) {
				for (ORole orole: rolDao.selectByDomain(con, domainId)) {
					items.put(sidsAsKeys ? orole.getRoleSid() : orole.getRoleId(), new GenericSubject(orole));
				}
			}
			return items;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> getComputedRolesAsStringByUser(UserProfileId pid, boolean self, boolean transitive) throws WTException {
		ArrayList<String> sids = new ArrayList<>();
		Set<RoleWithSource> roles = getComputedRolesByUser(pid, self, transitive);
		for(RoleWithSource role : roles) {
			sids.add(role.getRoleUid());
		}
		return sids;
	}
	
	public Set<RoleWithSource> getComputedRolesByUser(UserProfileId pid, boolean self, boolean transitive) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		Connection con = null;
		HashSet<String> roleMap = new HashSet<>();
		LinkedHashSet<RoleWithSource> roles = new LinkedHashSet<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			String userSid = usrm.lookupSubjectSid(pid, GenericSubject.Type.USER);
			
			if(self) {
				UserDAO usedao = UserDAO.getInstance();
				OUser user = usedao.selectBySid(con, userSid);
				roles.add(new RoleWithSource(RoleWithSource.SOURCE_USER, userSid, user.getDomainId(), pid.getUserId(), user.getDisplayName()));
			}
			
			RoleDAO roldao = RoleDAO.getInstance();
			
			// Gets by group
			List<ORole> groles = roldao.selectFromGroupsByUser(con, userSid);
			for(ORole role : groles) {
				if(roleMap.contains(role.getRoleSid())) continue; // Skip duplicates
				roleMap.add(role.getRoleSid());
				roles.add(new RoleWithSource(RoleWithSource.SOURCE_GROUP, role.getRoleSid(), role.getDomainId(), role.getName(), role.getDescription()));
			}
			
			// Gets direct assigned roles
			List<ORole> droles = roldao.selectDirectByUser(con, userSid);
			for(ORole role : droles) {
				if(roleMap.contains(role.getRoleSid())) continue; // Skip duplicates
				roleMap.add(role.getRoleSid());
				roles.add(new RoleWithSource(RoleWithSource.SOURCE_ROLE, role.getRoleSid(), role.getDomainId(), role.getName(), role.getDescription()));
			}
			
			// Get transivite roles (belonging to groups)
			if(transitive) {
				List<ORole> troles = roldao.selectTransitiveFromGroupsByUser(con, userSid);
				for(ORole role : troles) {
					if(roleMap.contains(role.getRoleSid())) continue; // Skip duplicates
					roleMap.add(role.getRoleSid());
					roles.add(new RoleWithSource(RoleWithSource.SOURCE_TRANSITIVE, role.getRoleSid(), role.getDomainId(), role.getName(), role.getDescription()));
				}
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return roles;
	}
	
	/**
	 * @deprecated Create new method with string result
	 */
	@Deprecated
	public List<ORolePermission> listRolePermissions(String roleSid) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			RolePermissionDAO dao = RolePermissionDAO.getInstance();
			return dao.selectByRoleSid(con, roleSid);
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public EntityPermissions extractPermissions(Connection con, String roleSid) throws WTException {
		RolePermissionDAO rolperdao = RolePermissionDAO.getInstance();
		
		List<ORolePermission> operms = rolperdao.selectByRoleSid(con, roleSid);
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
	
	public UserProfileId guessProfileIdAuthenticationAddress(final String authAddress) throws WTException {
		UserProfileId pid = UserProfileId.parseQuielty(authAddress);
		if (pid == null) return null;
		String domainId = authDomainNameToDomainId(pid.getDomain());
		if (domainId == null) return null;
		ensureProfileDomain(domainId);
		return new UserProfileId(domainId, pid.getUser());
	}
	
	public UserProfileId guessProfileIdByPersonalAddress(final String personalAddress) throws WTException {
		Check.notNull(personalAddress, "personalAddress");
		UserInfoDAO uiDao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			List<ProfileIdentifier> pids = uiDao.viewByEmail(con, personalAddress);
			if (pids.isEmpty()) return null;
			
			ProfileIdentifier pid = pids.get(0);
			ensureProfileDomain(pid.getDomainId());
			return new UserProfileId(pid.getDomainId(), pid.getUserId());
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Set<UserProfileId> guessProfileIdsByPersonalAddress(final String personalAddress) throws WTException {
		Check.notNull(personalAddress, "personalAddress");
		UserInfoDAO uiDao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			List<ProfileIdentifier> pids = uiDao.viewByEmail(con, personalAddress);
			return pids.stream()
				.filter((pid) -> ensureProfileDomainQuietly(pid.getDomainId()))
				.map((pid) -> pid.toProfileId())
				.collect(Collectors.toSet());
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Lists Share origins for specified profileId.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return A list of origin objects
	 * @throws WTException 
	 */
	public List<ShareOrigin> listShareOrigins(final UserProfileId targetProfileId, final String serviceId, final String context) throws WTException {
		return listShareOrigins(targetProfileId, null, serviceId, context);
	}
	
	/**
	 * Lists Share origins for specified profileId.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param targetPermissionKeys The permission-keys involved in the lookup.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return A list of origin objects
	 * @throws WTException 
	 */
	public List<ShareOrigin> listShareOrigins(final UserProfileId targetProfileId, final Collection<String> targetPermissionKeys, final String serviceId, final String context) throws WTException {
		Check.notNull(targetProfileId, "targetProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		ShareDAO shaDao = ShareDAO.getInstance();
		Connection con = null;
		
		// Define lookup targets
		final String targetSubjectSid = subjectSidCache.getSid(targetProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		
		try {
			con = WT.getCoreConnection();
			final Set<String> originSids;
			if (targetPermissionKeys == null) {
				originSids = shaDao.viewOriginatingSidsByServiceKey(con, serviceId, context);
			} else {
				final List<String> targetSubjectSids = getComputedRolesAsStringByUser(targetProfileId, true, true);
				originSids = shaDao.viewOriginatingSidsByRoleServiceKey(con, serviceId, context, targetSubjectSids, targetPermissionKeys);
			}
			
			final ArrayList<ShareOrigin> origins = new ArrayList<>();
			for (String uid : originSids) {
				if (uid.equals(targetSubjectSid)) continue; // Skip self				
				UserProfileId originProfileId = subjectSidCache.getPid(uid, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
				if (originProfileId == null) {
					LOGGER.warn("Unable to lookup USER/RESOURCE profileId for uid '{}'", uid);
				} else {
					UserProfile.Data udata = lookupProfileData(originProfileId, true);
					origins.add(new ShareOrigin(originProfileId, udata.getDisplayName()));
				}
			}
			
			Collections.sort(origins, (ShareOrigin so1, ShareOrigin so2) -> so1.getDisplayName().compareTo(so2.getDisplayName()));
			return origins;
			
		} catch (Exception ex) {
			LOGGER.error("Unable to compute share origins for '{}' [{}, {}]", targetProfileId, serviceId, context);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * 
	 * @param targetProfileId The Profile ID under investigation.
	 * @param targetPermissionKeys The permission-keys involved in the lookup.
	 * @param originProfileId The Profile ID of the origin.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return
	 * @throws WTException 
	 */
	public Set<String> getShareOriginInstances(final UserProfileId targetProfileId, final Collection<String> targetPermissionKeys, final UserProfileId originProfileId, final String serviceId, final String context) throws WTException {
		Check.notNull(targetProfileId, "targetProfileId");
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		ShareDAO shaDao = ShareDAO.getInstance();
		Connection con = null;
		
		// Define lookup targets
		final String originSubjectSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		final List<String> targetSubjectSids = getComputedRolesAsStringByUser(targetProfileId, true, true);
		
		try {
			con = WT.getCoreConnection();
			return shaDao.viewInstancesByOriginServiceContextPermissions(con, originSubjectSid, serviceId, context, targetSubjectSids, targetPermissionKeys);
			
		} catch (Exception ex) {
			LOGGER.error("Unable to compute share origin-instances for '{}' [{}, {}, {}]", targetProfileId, originSubjectSid, serviceId, context);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Returns Share configurations related to an instance of a Origin.
	 * @param <T>
	 * @param originProfileId The originating Share's profileId.
	 * @param serviceId The related service ID.
	 * @param context The context-name of the Share.
	 * @param instance The identifier of the entity that is (or being) shared.
	 * @param permissionKey The permission-key to target.
	 * @param typeOfData Type of Data object in configuration. Set to <null> to not process Data.
	 * @return A set of rights for involved subject IDs (SIDs).
	 * @throws WTException 
	 */
	public <T> Map<String, Sharing.SubjectConfiguration> getShareConfigurations(final UserProfileId originProfileId, final String serviceId, final String context, final String instance, final String permissionKey, final Class<T> typeOfData) throws WTException {
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notEmpty(instance, "instance");
		Check.notEmpty(permissionKey, "permissionKey");
		Connection con = null;
		
		try {
			String originSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
			con = WT.getCoreConnection();
			return doShareConfigurationsGet(con, originSid, serviceId, context, instance, permissionKey, typeOfData);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Applies a set of Share configurations related to an instance of a Origin.
	 * @param <T>
	 * @param originProfileId The originating Share's profileId.
	 * @param serviceId The related service ID.
	 * @param context The context-name of the Share.
	 * @param instance The identifier of the entity that is (or being) shared.
	 * @param permissionKey The permission-key to target.
	 * @param configurations A set of configurations: one for each involved subject ID.
	 * @param typeOfData Type of Data object in configuration. Set to <null> to not process Data.
	 * @throws WTException 
	 */
	public <T> void updateShareConfigurations(final UserProfileId originProfileId, final String serviceId, final String context, final String instance, final String permissionKey, final Set<Sharing.SubjectConfiguration> configurations, final Class<T> typeOfData) throws WTException {
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notEmpty(instance, "instance");
		Check.notEmpty(permissionKey, "permissionKey");
		Check.notNull(configurations, "configurations");
		Connection con = null;
		
		try {
			String originSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
			con = WT.getCoreConnection();
			doShareConfigurationsUpdate(con, originSid, serviceId, context, instance, permissionKey, configurations, typeOfData);
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Lists FolderShare (Root->Folder two-level sharing) origins for specified profileId.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return A list of origin objects
	 * @throws WTException 
	 */
	public List<ShareOrigin> listFolderShareOrigins(final UserProfileId targetProfileId, final String serviceId, final String context) throws WTException {
		Check.notEmpty(context, "context");
		
		// In order to find share origins, we need to look for any folders 
		// of at least a permission, then collect their owner uids.
		// So, we look into permissions table returning any row that have any 
		// possible share keys ("*_FOLDER@SHARE" or "*_ITEMS@SHARE") and 
		// satisfying a set of subject uids. Then we can get a list of unique 
		// uids (from shares table) that originating the share.
		
		final List<String> TARGET_PERMISSION_KEYS = buildFolderShareKeys(context);
		return listShareOrigins(targetProfileId, TARGET_PERMISSION_KEYS, serviceId, context);
	}
	
	/**
	 * Lists FolderShare (Root->Folder two-level sharing) origins for specified profileId.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return A list of origin objects
	 * @throws WTException 
	 */
	public List<FolderShareOrigin> listFolderShareOrigins_OLD(final UserProfileId targetProfileId, final String serviceId, final String context) throws WTException {
		Check.notNull(targetProfileId, "targetProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		ShareDAO shaDao = ShareDAO.getInstance();
		Connection con = null;
		
		// In order to find share origins, we need to look for any folders 
		// of at least a permission, then collect their owner uids.
		// So, we look into permissions table returning any row that have any 
		// possible share keys ("*_FOLDER@SHARE" or "*_ITEMS@SHARE") and 
		// satisfying a set of subject uids. Then we can get a list of unique 
		// uids (from shares table) that originating the share.
		
		// Define lookup targets
		final String targetSubjectSid = subjectSidCache.getSid(targetProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		final List<String> targetSubjectSids = getComputedRolesAsStringByUser(targetProfileId, true, true);
		final List<String> TARGET_PERMISSION_KEYS = buildFolderShareKeys(context);
		
		try {
			con = WT.getCoreConnection();
			Set<String> originSids = shaDao.viewOriginatingSidsByRoleServiceKey(con, serviceId, context, targetSubjectSids, TARGET_PERMISSION_KEYS);
			ArrayList<FolderShareOrigin> origins = new ArrayList<>();
			for (String uid : originSids) {
				if (uid.equals(targetSubjectSid)) continue; // Skip self				
				UserProfileId rootProfileId = subjectSidCache.getPid(uid, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
				if (rootProfileId == null) {
					LOGGER.warn("Unable to lookup USER/RESOURCE profileId for uid '{}'", uid);
				} else {
					UserProfile.Data udata = lookupProfileData(rootProfileId, true);
					origins.add(new FolderShareOrigin(rootProfileId, udata.getDisplayName()));
				}
			}
			
			Collections.sort(origins, (FolderShareOrigin so1, FolderShareOrigin so2) -> so1.getDisplayName().compareTo(so2.getDisplayName()));
			return origins;
			
		} catch (Exception ex) {
			LOGGER.error("Unable to compute folder-share origins for '{}' [{}, {}]", targetProfileId, serviceId, context);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Get FolderShare (Root->Folder two-level sharing) Folders for specified profileId.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param originProfileId The source Origin ID of the share.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return
	 * @throws WTException 
	 */
	public FolderShareOriginFolders getFolderShareOriginFolders(final UserProfileId targetProfileId, final UserProfileId originProfileId, final String serviceId, final String context) throws WTException {
		Check.notEmpty(context, "context");
		
		// In order to find share instances, we need to look for any folders 
		// of at least a permission, then collect their instance ids.
		// So, we look into permissions table returning any row that have any 
		// possible share keys ("*_FOLDER@SHARE" or "*_ITEMS@SHARE") and 
		// satisfying a set of subject uids. Then we can get a list of unique 
		// instances (from shares table) that originating the share.
		
		final List<String> TARGET_PERMISSION_KEYS = buildFolderShareKeys(context);
		Set<String> instances = getShareOriginInstances(targetProfileId, TARGET_PERMISSION_KEYS, originProfileId, serviceId, context);
		boolean wildcardFound = instances.remove(FolderSharing.INSTANCE_WILDCARD);
		
		return new FolderShareOriginFolders(instances, wildcardFound);
	}
	
	/**
	 * Get FolderShare (Root->Folder two-level sharing) Folders for specified profileId.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param originProfileId The source Origin ID of the share.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return
	 * @throws WTException 
	 */
	public FolderShareOriginFolders getFolderShareOriginFolders_OLD(final UserProfileId targetProfileId, final UserProfileId originProfileId, final String serviceId, final String context) throws WTException {
		Check.notNull(targetProfileId, "targetProfileId");
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		ShareDAO shaDao = ShareDAO.getInstance();
		Connection con = null;
		
		// In order to find share instances, we need to look for any folders 
		// of at least a permission, then collect their instance ids.
		// So, we look into permissions table returning any row that have any 
		// possible share keys ("*_FOLDER@SHARE" or "*_ITEMS@SHARE") and 
		// satisfying a set of subject uids. Then we can get a list of unique 
		// instances (from shares table) that originating the share.
		
		// Define lookup targets
		final String originSubjectSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		//final String targetSubjectSid = aclSubjectCache.getSid(targetProfileId);
		final List<String> targetSubjectSids = getComputedRolesAsStringByUser(targetProfileId, true, true);
		final List<String> TARGET_PERMISSION_KEYS = buildFolderShareKeys(context);
		
		try {
			con = WT.getCoreConnection();
			Set<String> instances = shaDao.viewInstancesByOriginServiceContextPermissions(con, originSubjectSid, serviceId, context, targetSubjectSids, TARGET_PERMISSION_KEYS);
			boolean wildcardFound = instances.remove(FolderSharing.INSTANCE_WILDCARD);
			return new FolderShareOriginFolders(instances, wildcardFound);
			
		} catch (Exception ex) {
			LOGGER.error("Unable to compute folder-share folders for '{}' [{}, {}, {}]", targetProfileId, originSubjectSid, serviceId, context);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Evaluates passed FolderShare rights, described under-the-hood by actions, against profile's permissions.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param originProfileId The source Origin ID of the share.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param scope Specifies the depth of the analysis: at top with wildcard or down with a specific folder.
	 * @param throwOnMissingShare Set to `false` to NOT throw WTNotFoundException if share at requested scope is not available.
	 * @param target Specifies the target of the actions: folder or folder's items.
	 * @param actions The permission actions to check.
	 * @return
	 * @throws com.sonicle.webtop.core.app.sdk.WTNotFoundException
	 * @throws WTException 
	 */
	public boolean[] evaluateFolderSharePermission(final UserProfileId targetProfileId, final UserProfileId originProfileId, final String serviceId, final String context, final FolderSharing.Scope scope, final boolean throwOnMissingShare, final FolderShare.EvalTarget target, final String[] actions) throws WTNotFoundException, WTException {
		Check.notNull(targetProfileId, "targetProfileId");
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notNull(scope, "scope");
		Check.notNull(target, "target");
		Check.notNull(actions, "actions");
		ShareDAO shaDao = ShareDAO.getInstance();
		Connection con = null;
		
		// Define lookup targets
		final String originSubjectSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		final String shareInstance;
		if (scope instanceof FolderSharing.WildcardScope) {
			shareInstance = FolderSharing.INSTANCE_WILDCARD;
			
		} else if (scope instanceof FolderSharing.FolderScope) {
			shareInstance = ((FolderSharing.FolderScope)scope).getFolderId();

		} else {
			throw new IllegalArgumentException("Unsupported scope");
		}
		
		String permissionKey = null;
		if (FolderShare.EvalTarget.FOLDER.equals(target)) {
			permissionKey = FolderShare.buildFolderPermissionKey(context);
		} else if (FolderShare.EvalTarget.FOLDER_ITEMS.equals(target)) {
			permissionKey = FolderShare.buildItemsPermissionKey(context);
		}
		
		try {
			con = WT.getCoreConnection();
			Integer shareId = shaDao.selectIdByUserServiceKeyInstance(con, originSubjectSid, serviceId, context, shareInstance);
			if (shareId == null) {
				if (!throwOnMissingShare) return null;
				throw new WTNotFoundException("Share not found [{}, {}, {}, {}]", originSubjectSid, serviceId, context, shareInstance);
			}
			
			boolean[] bools = new boolean[actions.length];
			for (int i=0; i<actions.length; i++) {
				bools[i] = RunContext.isPermitted(true, targetProfileId, serviceId, permissionKey, actions[i], String.valueOf(shareId));
				if (LOGGER.isTraceEnabled()) LOGGER.trace("SharePermission eval: [{}] for [{}] -> {}", ServicePermission.permissionString(ServicePermission.namespacedName(serviceId, permissionKey), actions[i], String.valueOf(shareId)), targetProfileId, bools[i]);
			}
			return bools;
			
		} catch (Exception ex) {
			LOGGER.error("Unable to evaluate permissions for '{}' [{}, {}, {}, {}]", targetProfileId, originSubjectSid, serviceId, context, shareInstance);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Evaluates all available FolderShare rights against profile's permissions.
	 * @param targetProfileId The Profile ID under investigation.
	 * @param originProfileId The source Origin ID of the share.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param scope Specifies the depth of the analysis: at top with wildcard or down with a specific folder.
	 * @param throwOnMissingShare Set to `false` to NOT throw WTNotFoundException if share at requested scope is not available.
	 * @return
	 * @throws WTException 
	 */
	public FolderShare.Permissions evaluateFolderSharePermissions(final UserProfileId targetProfileId, final UserProfileId originProfileId, final String serviceId, final String context, final FolderSharing.Scope scope, final boolean throwOnMissingShare) throws WTException {
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notNull(scope, "scope");
		ShareDAO shaDao = ShareDAO.getInstance();
		Connection con = null;
		
		final String FOLDER_PERMISSION_KEY = FolderShare.buildFolderPermissionKey(context);
		final String ITEMS_PERMISSION_KEY = FolderShare.buildItemsPermissionKey(context);
		
		final String originSubjectSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		boolean skipWildcardReservedRights = true;
		final String shareInstance;
		if (scope instanceof FolderSharing.WildcardScope) {
			skipWildcardReservedRights = false;
			shareInstance = FolderSharing.INSTANCE_WILDCARD;

		} else if (scope instanceof FolderSharing.FolderScope) {
			shareInstance = ((FolderSharing.FolderScope)scope).getFolderId();

		} else {
			throw new IllegalArgumentException("Unsupported scope");
		}
		
		try {
			con = WT.getCoreConnection();
			Integer shareId = shaDao.selectIdByUserServiceKeyInstance(con, originSubjectSid, serviceId, context, shareInstance);
			if (shareId == null) {
				if (!throwOnMissingShare) return null;
				throw new WTNotFoundException("Share not found [{}, {}, {}, {}]", originSubjectSid, serviceId, context, shareInstance);
			}
			
			FolderShare.FolderPermissions folderPerms = new FolderShare.FolderPermissions();
			for (FolderShare.FolderRight right : EnumUtils.allTypesOf(FolderShare.FolderRight.class)) {
				if (skipWildcardReservedRights && right.isReservedForWildcard()) continue;
				if (RunContext.isPermitted(true, targetProfileId, serviceId, FOLDER_PERMISSION_KEY, right.name(), String.valueOf(shareId))) {
					folderPerms.set(right);
				}
			}
			FolderShare.ItemsPermissions itemsPerms = new FolderShare.ItemsPermissions();
			for (FolderShare.ItemsRight right : EnumUtils.allTypesOf(FolderShare.ItemsRight.class)) {
				if (RunContext.isPermitted(true, targetProfileId, serviceId, ITEMS_PERMISSION_KEY, right.name(), String.valueOf(shareId))) {
					itemsPerms.set(right);
				}
			}
			return new FolderShare.Permissions(folderPerms, itemsPerms);
			
		} catch (Exception ex) {
			LOGGER.error("Unable to evaluate permissions for '{}' [{}, {}, {}, {}]", targetProfileId, originSubjectSid, serviceId, context, shareInstance);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Returns a list of rights applied to a specific FolderShare (Root->Folder two-level sharing).This is suitable for completing sharing panel where all permissions for 
 a shared entity are grouped together in a single representation.
	 * @param <T>
	 * @param originProfileId The FolderShare origin profile to be checked.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param scope The level to evaluate: wildcard or folder-specific.
	 * @param typeOfData Type of Data object in configuration. Set to <null> to not process Data.
	 * @return A collection of rights.
	 * @throws WTException 
	 */
	public <T> Set<FolderSharing.SubjectConfiguration> getFolderShareConfigurations(final UserProfileId originProfileId, final String serviceId, final String context, final FolderSharing.Scope scope, final Class<T> typeOfData) throws WTException {
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notNull(scope, "scope");
		Connection con = null;
		
		try {
			String originSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
			con = WT.getCoreConnection();
			return doFolderShareConfigurationsGet(con, originSid, serviceId, context, scope, typeOfData);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Updates the set of rights associated to a FolderShare (Root->Folder two-level sharing).
	 * @param <T>
	 * @param originProfileId The FolderShare origin profile to be updated.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param scope The level on which perform the operation: wildcard or folder-specific.
	 * @param configurations A set of configurations: one for each involved subject ID.
	 * @param typeOfData Type of Data object in configuration. Set to <null> to not process Data.
	 * @throws WTException 
	 */
	public <T> void updateFolderShareConfigurations(final UserProfileId originProfileId, final String serviceId, final String context, final FolderSharing.Scope scope, final Set<FolderSharing.SubjectConfiguration> configurations, final Class<T> typeOfData) throws WTException {
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notNull(scope, "level");
		Check.notNull(configurations, "configurations");
		Connection con = null;
		
		try {
			String originSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
			con = WT.getCoreConnection();
			doFolderShareConfigurationUpdate(con, originSid, serviceId, context, scope, configurations, typeOfData);
			DbUtils.commitQuietly(con);
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Gets stored Share data involved in sharing process between origin and target.
	 * @param <T>
	 * @param targetProfileId The Share target profile whose data needs to be returned.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The Share origin profile to be updated.
	 * @param instance The identifier of the entity that actually shared.
	 * @param dataType The Class type of saved raw data.
	 * @param throwOnMissingShare Set to `false` to return null instead of throwing an exception on missing share.
	 * @return Deserializes data object of specified type.
	 * @throws WTNotFoundException If share is missing and throwOnMissingShare is set
	 * @throws WTException 
	 */
	public <T> T getShareData(final UserProfileId targetProfileId, final String serviceId, final String context, final UserProfileId originProfileId, final String instance, Class<T> dataType, final boolean throwOnMissingShare) throws WTNotFoundException, WTException {
		Check.notNull(targetProfileId, "targetProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(instance, "instance");
		ShareDAO shaDao = ShareDAO.getInstance();
		ShareDataDAO shadDao = ShareDataDAO.getInstance();
		Connection con = null;
		
		// Define lookup targets
		final String targetSubjectSid = subjectSidCache.getSid(targetProfileId, GenericSubject.Type.GROUP, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE, GenericSubject.Type.ROLE);
		final String originSubjectSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		
		try {
			con = WT.getCoreConnection();
			Integer shareId = shaDao.selectIdByUserServiceKeyInstance(con, originSubjectSid, serviceId, context, instance);
			if (shareId == null) {
				if (!throwOnMissingShare) return null;
				throw new WTNotFoundException("Share lookup failed for instance '{}' [{}, {}, {}]", instance, originSubjectSid, serviceId, context);
			}
			
			String rawData = shadDao.selectValueByShareUser(con, shareId, targetSubjectSid);
			return rawData != null ? LangUtils.deserialize(rawData, null, dataType) : null;
			
		} catch (Exception ex) {
			LOGGER.error("Unable to get share data for '{}' [{}, {}, {}, {}]", targetProfileId, originSubjectSid, serviceId, context, instance);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Gets stored FolderShare (Root->Folder two-level sharing) data involved in sharing process between origin and target.
	 * @param <T>
	 * @param targetProfileId The Share target profile whose data needs to be returned.
	 * @param originProfileId The Share origin profile to be updated.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param scope The level on which perform the operation: wildcard or folder-specific.
	 * @param dataType The Class type of saved raw data.
	 * @param throwOnMissingShare Set to `false` to return null instead of throwing an exception on missing share.
	 * @return
	 * @throws WTNotFoundException If share is missing and throwOnMissingShare is set
	 * @throws WTException 
	 */
	public <T> T getFolderShareData(final UserProfileId targetProfileId, final UserProfileId originProfileId, final String serviceId, final String context, final FolderSharing.Scope scope, Class<T> dataType, final boolean throwOnMissingShare) throws WTNotFoundException, WTException {
		Check.notNull(scope, "scope");
		
		final String instance;
		if (scope instanceof FolderSharing.WildcardScope) {
			instance = FolderSharing.INSTANCE_WILDCARD;
		} else if (scope instanceof FolderSharing.FolderScope) {
			instance = ((FolderSharing.FolderScope)scope).getFolderId();
		} else {
			throw new IllegalArgumentException("Unsupported scope");
		}
		
		return getShareData(targetProfileId, serviceId, context, originProfileId, instance, dataType, throwOnMissingShare);
	}
	
	/**
	 * Stores passed Share data object.
	 * @param <T>
	 * @param targetProfileId The Share target profile whose data needs to be returned.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The Share origin profile to be updated.
	 * @param instance The identifier of the entity that actually shared.
	 * @param data The data object.
	 * @param dataType The Class type of passed data.
	 * @param throwOnMissingShare Set to `false` to return null instead of throwing an exception on missing share.
	 * @return
	 * @throws WTNotFoundException If share is missing and throwOnMissingShare is set
	 * @throws WTException 
	 */
	public <T> boolean updateShareData(final UserProfileId targetProfileId, final String serviceId, final String context, final UserProfileId originProfileId, final String instance, T data, Class<T> dataType, final boolean throwOnMissingShare) throws WTNotFoundException, WTException {
		Check.notNull(targetProfileId, "targetProfileId");
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notNull(originProfileId, "originProfileId");
		Check.notEmpty(instance, "instance");
		ShareDAO shaDao = ShareDAO.getInstance();
		ShareDataDAO shadDao = ShareDataDAO.getInstance();
		Connection con = null;
		
		// Define lookup targets
		final String targetSubjectSid = subjectSidCache.getSid(targetProfileId, GenericSubject.Type.GROUP, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE, GenericSubject.Type.ROLE);
		final String originSubjectSid = subjectSidCache.getSid(originProfileId, GenericSubject.Type.USER, GenericSubject.Type.RESOURCE);
		
		try {
			con = WT.getCoreConnection();
			Integer shareId = shaDao.selectIdByUserServiceKeyInstance(con, originSubjectSid, serviceId, context, instance);
			if (shareId == null) {
				if (throwOnMissingShare) throw new WTNotFoundException("Share lookup failed for instance '{}' [{}, {}, {}]", instance, originSubjectSid, serviceId, context);
				return false;
				
			} else {
				String rawData = LangUtils.serialize(data, dataType);
				int ret = shadDao.update(con, shareId, targetSubjectSid, rawData);
				if (ret == 0) ret = shadDao.insert(con, shareId, targetSubjectSid, rawData);
				return ret > 0;
			}
			
		} catch (Exception ex) {
			LOGGER.error("Unable to set share data for '{}' [{}, {}, {}, {}]", targetProfileId, originSubjectSid, serviceId, context, instance);
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Stores passed FolderShare (Root->Folder two-level sharing) data object.
	 * @param <T>
	 * @param targetProfileId The Share target profile whose data needs to be returned.
	 * @param originProfileId The Share origin profile to be updated.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param scope The level on which perform the operation: wildcard or folder-specific.
	 * @param data The data object.
	 * @param dataType The Class type of passed data.
	 * @param throwOnMissingShare Set to `false` to return null instead of throwing an exception on missing share.
	 * @return
	 * @throws WTNotFoundException If share is missing and throwOnMissingShare is set
	 * @throws WTException 
	 */
	public <T> boolean updateFolderShareData(final UserProfileId targetProfileId, final UserProfileId originProfileId, final String serviceId, final String context, final FolderSharing.Scope scope, T data, Class<T> dataType, final boolean throwOnMissingShare) throws WTNotFoundException, WTException {
		Check.notNull(scope, "scope");
		
		final String instance;
		if (scope instanceof FolderSharing.WildcardScope) {
			instance = FolderSharing.INSTANCE_WILDCARD;
		} else if (scope instanceof FolderSharing.FolderScope) {
			instance = ((FolderSharing.FolderScope)scope).getFolderId();
		} else {
			throw new IllegalArgumentException("Unsupported scope");
		}
		
		return updateShareData(targetProfileId, serviceId, context, originProfileId, instance, data, dataType, throwOnMissingShare);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	private ODomain doODomainGet(Connection con, String domainId) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		return domDao.selectById(con, domainId);
	}
	
	private static enum DomainProcessOpt implements BitFlagsEnum<DomainProcessOpt> {
		PROCESS_DIRECTORY(1 << 1);
		
		private int mask = 0;
		private DomainProcessOpt(int mask) { this.mask = mask; }
		@Override
		public long mask() { return this.mask; }
		
		public static BitFlags<DomainProcessOpt> fromDomainGetOptions(BitFlags<DomainGetOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(DomainProcessOpt.class, options);
		}
		
		public static BitFlags<DomainProcessOpt> fromDomainUpdateOptions(BitFlags<DomainUpdateOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(DomainProcessOpt.class, options);
		}
	}
	
	private Domain doDomainGet(final Connection con, final String domainId, final BitFlags<DomainProcessOpt> options) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		
		ODomain odomain = options.has(DomainProcessOpt.PROCESS_DIRECTORY) ? domDao.selectById(con, domainId) : domDao.selectBasicById(con, domainId);
		if (odomain == null) return null;
		
		Domain domain = AppManagerUtils.fillDomain(new Domain(), odomain);
		decryptDomainDirPassword(domain);
		return domain;
	}
	
	private ODomain doDomainInsert(final Connection con, final String domainId, final DomainBase domain) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		
		encryptDomainDirPassword(domain);
		ODomain odomain = AppManagerUtils.fillODomain(new ODomain(), domain);
		odomain.setDomainId(domainId);
		ODomain.fillDefaultsForInsert(odomain);
		ODomain.validate(odomain);
		
		domDao.insert(con, odomain);
		return odomain;
	}
	
	private DomainUpdateResult doDomainUpdate(final Connection con, final String domainId, final DomainBase domain, final boolean setDirPassword, final BitFlags<DomainProcessOpt> options) throws WTException {
		DomainDAO domDao = DomainDAO.getInstance();
		
		if (setDirPassword) encryptDomainDirPassword(domain);
		ODomain odomain = AppManagerUtils.fillODomain(new ODomain(), domain);
		odomain.setDomainId(domainId);
		ODomain.fillDefaultsForUpdate(odomain);
		ODomain.validate(odomain);
		
		boolean ret = (options.has(DomainProcessOpt.PROCESS_DIRECTORY) ? domDao.update(con, odomain, setDirPassword) : domDao.updateBasic(con, odomain)) == 1;
		if (!ret) throw new WTNotFoundException("Domain not found [{}]", domainId);
		return new DomainUpdateResult(odomain);
	}
	
	private void doDomainDelete(final Connection con, final String domainId) {
		LOGGER.debug("[{}] Deleting activities...", domainId);
		ActivityDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting causals...", domainId);
		CausalDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting master-data...", domainId);
		MasterDataDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting IM data...", domainId);
		IMMessageDAO.getInstance().deleteByDomain(con, domainId);
		IMChatDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting CustomFields data...", domainId);
		CustomPanelDAO.getInstance().deleteByDomain(con, domainId);
		CustomFieldDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting tags...", domainId);
		TagDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting sharing data...", domainId);
		ShareDataDAO.getInstance().deleteByDomain(con, domainId);
		ShareDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting permissions...", domainId);
		RolePermissionDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting roles...", domainId);
		RoleAssociationDAO.getInstance().deleteByDomain(con, domainId);
		RoleDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting groups/users...", domainId);
		UserAssociationDAO.getInstance().deleteByDomain(con, domainId);
		UserInfoDAO.getInstance().deleteByDomain(con, domainId);
		UserDAO.getInstance().deleteByDomain(con, domainId);
		GroupDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting settings (user+domain)...", domainId);
		UserSettingDAO.getInstance().deleteByDomain(con, domainId);
		DomainSettingDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting management tables...", domainId);
		AutosaveDAO.getInstance().deleteByDomain(con, domainId);
		ServiceStoreEntryDAO.getInstance().deleteByDomain(con, domainId);
		SnoozedReminderDAO.getInstance().deleteByDomain(con, domainId);
		MessageQueueDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting DataSources...", domainId);
		DataSourceDAO.getInstance().deleteByDomain(con, domainId);
		LOGGER.debug("[{}] Deleting Licenses...", domainId);
		LicenseDAO.getInstance().deleteByDomain(con, domainId);
		
		//LOGGER.debug("[{}] Deleting logs...", domainId);
		//AuditLogDAO.getInstance().deleteByDomain(con, domainId);
		//DomainAccessLogDAO.getInstance().deleteByDomain(con, domainId);
		
		LOGGER.debug("[{}] Deleting domain...", domainId);
		DomainDAO.getInstance().deleteById(con, domainId);
	}
	
	private class DomainUpdateResult {
		public final ODomain odomain;
		
		public DomainUpdateResult(ODomain odomain) {
			this.odomain = odomain;
		}
	}
	
	private class DomainInitResult {
		public final boolean subjectCacheUpdated;
		public final Result<User> adminResult;
		public final ArrayList<HomedThrowable> errors;
		
		public DomainInitResult(boolean subjectCacheUpdated, Result<User> adminResult, ArrayList<HomedThrowable> errors) {
			this.subjectCacheUpdated = subjectCacheUpdated;
			this.adminResult = adminResult;
			this.errors = errors;
		}
	}
	
	private DomainInitResult doDomainInitCheck(final String domainId) throws WTException {
		RoleDAO rolDao = RoleDAO.getInstance();
		GroupDAO grpDao = GroupDAO.getInstance();
		UserDAO usrDao = UserDAO.getInstance();
		Connection con = null;
		ArrayList<HomedThrowable> errors = new ArrayList<>();
		boolean cacheUpdated = false;
		Result<User> domainAdminResult = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
		
			// Prepare built-in roles
			LOGGER.debug("[{}] Checking built-in roles...", domainId);
			Map<String, ORole> roles = rolDao.selectByDomainIn(con, domainId, BUILT_IN_ROLES);
			if (!roles.containsKey(DOMAINADMIN_ROLEID)) {
				try {
					RoleBase role = new RoleBase();
					role.setDescription("Describes rights of the Domain Administrator");
					role.setAllowedServiceIds(LangUtils.asSet("com.sonicle.webtop.core.admin", "com.sonicle.webtop.vfs"));
					BitFlags<RoleProcessOpt> options = BitFlags.with(RoleProcessOpt.PROCESS_SERVICEPERMISSIONS);
					RoleInsertResult result = doRoleInsert(con, domainId, DOMAINADMIN_ROLEID, null, role, options);
					subjectSidCache.add(result.orole.getProfileId(), result.orole.getRoleSid(), GenericSubject.Type.ROLE);
					cacheUpdated = true;

				} catch (Exception ex) {
					LOGGER.error("[{}] Unable to add built-in '{}' role", domainId, DOMAINADMIN_ROLEID, ex);
					errors.add(new HomedThrowable(CoreManifest.ID, new WTException(ex, "Unable to add built-in '{}' role", DOMAINADMIN_ROLEID)));
				}	
			}
			if (!roles.containsKey(PECACCOUNT_ROLEID)) {
				try {
					RoleBase role = new RoleBase();
					role.setDescription("Represents an account used as PEC container.");
					//role.setAllowedServiceIds(LangUtils.asSet("com.sonicle.webtop.core.admin", "com.sonicle.webtop.vfs"));
					RoleInsertResult result = doRoleInsert(con, domainId, PECACCOUNT_ROLEID, null, role, BitFlags.noneOf(RoleProcessOpt.class));
					subjectSidCache.add(result.orole.getProfileId(), result.orole.getRoleSid(), GenericSubject.Type.ROLE);
					cacheUpdated = true;

				} catch (Exception ex) {
					LOGGER.error("[{}] Unable to add built-in '{}' role", domainId, PECACCOUNT_ROLEID, ex);
					errors.add(new HomedThrowable(CoreManifest.ID, new WTException(ex, "Unable to add built-in '{}' role", PECACCOUNT_ROLEID)));
				}
			}
			
			// Prepare built-in groups
			LOGGER.debug("[{}] Checking built-in groups...", domainId);
			Map<String, OGroup> groups = grpDao.selectByDomainIn(con, domainId, BUILT_IN_GROUPS);
			/*
			if (!groups.containsKey(GROUPID_ADMINS)) {
				try {
					GroupInsertResult result = doGroupInsert(con, domainId, GROUPID_ADMINS, "Admins");
					subjectSidCache2.add(result.ogroup.getProfileId(), result.ogroup.getGroupSid(), GenericSubject.Type.GROUP);
					cacheUpdated = true;

				} catch (Exception ex) {
					LOGGER.error("[{}] Unable to add built-in '{}' group", domainId, GROUPID_ADMINS, ex);
					errors.add(new HomedThrowable(CoreManifest.ID, new WTException(ex, "Unable to add built-in '{}' group", GROUPID_ADMINS)));
				}	
			}
			*/
			if (!groups.containsKey(GROUPID_USERS)) {
				try {
					GroupInsertResult result = doGroupInsert(con, domainId, GROUPID_USERS, "Users");
					subjectSidCache.add(result.ogroup.getProfileId(), result.ogroup.getGroupSid(), GenericSubject.Type.GROUP);
					cacheUpdated = true;

				} catch (Exception ex) {
					LOGGER.error("[{}] Unable to add built-in '{}' group", domainId, GROUPID_USERS, ex);
					errors.add(new HomedThrowable(CoreManifest.ID, new WTException(ex, "Unable to add built-in '{}' group", GROUPID_USERS)));
				}
			}
			if (!groups.containsKey(GROUPID_PEC_ACCOUNTS)) {
				try {
					GroupInsertResult result = doGroupInsert(con, domainId, GROUPID_PEC_ACCOUNTS, "Groups all PEC Accounts");
					subjectSidCache.add(result.ogroup.getProfileId(), result.ogroup.getGroupSid(), GenericSubject.Type.GROUP);
					cacheUpdated = true;

				} catch (Exception ex) {
					LOGGER.error("[{}] Unable to add built-in '{}' group", domainId, GROUPID_PEC_ACCOUNTS, ex);
					errors.add(new HomedThrowable(CoreManifest.ID, new WTException(ex, "Unable to add built-in '{}' group", GROUPID_PEC_ACCOUNTS)));
				}
			}
			
			// Prepare built-in (domain) admin user
			LOGGER.debug("[{}] Checking built-in domain admin...", domainId);
			if (!usrDao.existByDomainUser(con, domainId, DOMAINADMIN_USERID)) {
				try {
					UserBase user = new UserBase();
					user.setEnabled(true);
					user.setFirstName("Admin");
					user.setLastName(domainId);
					user.setDisplayName(user.getFirstName() + " (" + domainId + ")");
					//user.setAssignedGroups(LangUtils.asSet(new UserProfileId(domainId, GROUPID_USERS).toString()));
					//user.getAssignedGroups().add(new UserProfileId(domainId, GROUPID_USERS).toString());
					user.setAssignedRoles(LangUtils.asSet(new UserProfileId(domainId, DOMAINADMIN_ROLEID).toString()));
					BitFlags<UserUpdateOption> options = BitFlags.with(UserUpdateOption.ROLE_ASSOCIATIONS);
					domainAdminResult = addUser(domainId, DOMAINADMIN_USERID, user, true, true, null, options, null);
					//domainAdminResult = addUser(domainId, DOMAINADMIN_USERID, user, true, true, null, UserUpdateOption.internalDefaultFlags().unset(UserUpdateOption.SUBJECTS_AS_SID));
					cacheUpdated = true;

				} catch (Exception ex) {
					LOGGER.error("[{}] Unable to add built-in '{}' user", domainId, DOMAINADMIN_USERID, ex);
					errors.add(new HomedThrowable(CoreManifest.ID, new WTException(ex, "Unable to add built-in '{}' user", DOMAINADMIN_USERID)));
				}
			}
			
		} catch (Exception ex) {
			errors.add(new HomedThrowable(CoreManifest.ID, ex));
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		return new DomainInitResult(cacheUpdated, domainAdminResult, errors);
	}
	
	private void doDomainInitHomeFolder(final String domainId) throws SecurityException {
		synchronized(foldersLock) {
			// Main folder (/domains/{domainId})
			File domainDir = new File(wta.getFileSystem().getHomePath(domainId));
			if (!domainDir.exists()) domainDir.mkdir();
			
			// Internal folders...
			File tempDir = new File(wta.getFileSystem().getTempPath(domainId));
			if (!tempDir.exists()) tempDir.mkdir();
			
			File imagesDir = new File(wta.getFileSystem().getImagesPath(domainId));
			if (!imagesDir.exists()) imagesDir.mkdir();
			
			for (String sid : wta.getServiceManager().listRegisteredServices()) {
				File svcDir = new File(wta.getFileSystem().getServiceHomePath(domainId, sid));
				if (!svcDir.exists()) svcDir.mkdir();
			}
		}	
	}
	
	private void doDomainDeleteHomeFolder(final String domainId) {
		synchronized(foldersLock) {
			// Deletes logically domain folder by simply historycizing it...
			// Main folder (/domains/{domainId})
			File domainDir = new File(wta.getFileSystem().getHomePath(domainId));
			if (domainDir.exists()) {
				String nowSuffix = DateTimeUtils.print(DateTimeUtils.createFormatter("yyyyMMddHHmmssSSS", DateTimeZone.UTC), DateTimeUtils.now(true));
				String deletedDomainDirString = wta.getFileSystem().getHomePath(domainId + "." + nowSuffix);
				File deletedDomainDir = new File(deletedDomainDirString);
				if (!deletedDomainDir.exists()) {
					domainDir.renameTo(deletedDomainDir);
					LOGGER.warn("Domain folder deletion can be a lengthy operation. Please delete '{}' folder manually.", deletedDomainDirString);
				}
			}
		}
	}
	
	private static enum UserProcessOpt implements BitFlagsEnum<UserProcessOpt> {
		SUBJECTS_AS_SID(1 << 0), PROCESS_GROUPASSOCIATIONS(1 << 1), PROCESS_ROLEASSOCIATIONS(1 << 2), PROCESS_PERMISSIONS(1 << 3), PROCESS_SERVICEPERMISSIONS(1 << 4);
		
		private int mask = 0;
		private UserProcessOpt(int mask) { this.mask = mask; }
		@Override
		public long mask() { return this.mask; }
		
		public static BitFlags<UserProcessOpt> fromUserGetOptions(BitFlags<UserGetOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(UserProcessOpt.class, options);
		}
		
		public static BitFlags<UserProcessOpt> fromUserUpdateOptions(BitFlags<UserUpdateOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(UserProcessOpt.class, options);
		}
	}
	
	private User doUserGet(final Connection con, final String domainId, final String userId, final BitFlags<UserProcessOpt> options) throws WTException {
		UserDAO useDao = UserDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		final boolean subjectsAsSID = options.has(UserProcessOpt.SUBJECTS_AS_SID);
		
		VUser vuser = useDao.selectByProfile(con, domainId, userId);
		if (vuser == null) return null;
		
		Set<String> assignedGroups = null;
		if (options.has(UserProcessOpt.PROCESS_GROUPASSOCIATIONS)) {
			assignedGroups = uasDao.viewGroupSidsByUser(con, vuser.getUserSid());
			if (!subjectsAsSID) {
				assignedGroups = assignedGroups.stream()
					.map((userSid) -> subjectSidCache.getPid(userSid, GenericSubject.Type.GROUP))
					.filter((userPid) -> userPid != null)
					.map((userPid) -> userPid.toString())
					.collect(Collectors.toSet());
			}
		}
		Set<String> assignedRoles = null;
		if (options.has(UserProcessOpt.PROCESS_ROLEASSOCIATIONS)) {
			assignedRoles = rasDao.viewRoleSidsBySubject(con, vuser.getUserSid());
			if (!subjectsAsSID) {
				assignedRoles = assignedRoles.stream()
					.map((roleSid) -> subjectSidCache.getPid(roleSid, GenericSubject.Type.ROLE))
					.filter((rolePid) -> rolePid != null)
					.map((rolePid) -> rolePid.toString())
					.collect(Collectors.toSet());
			}
		}
		
		Set<PermissionString> permissions = null;
		if (options.has(UserProcessOpt.PROCESS_PERMISSIONS)) {
			Map<Integer, ORolePermission> map = prmDao.viewSubjectEntriesBySubjectCondition(con, vuser.getUserSid(), RolePermissionDAO.createServicePermissionAndKeysExcludeCondition(buildFolderShareKeysALL("%")));
			permissions = map.values().stream()
				.map((orperm) -> toPermissionString(orperm))
				.collect(Collectors.toSet());
		}
		
		Set<String> allowedServiceIds = null;
		if (options.has(UserProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Map<Integer, ORolePermission> map = prmDao.viewSubjectEntriesBySubjectCondition(con, vuser.getUserSid(), RolePermissionDAO.createServicePermissionOnlyCondition());
			allowedServiceIds = map.values().stream()
				.map((orperm) -> orperm.getInstance())
				.collect(Collectors.toSet());
		}
		
		User user = AppManagerUtils.fillUser(new User(), vuser);
		user.setAssignedGroups(assignedGroups);
		user.setAssignedRoles(assignedRoles);
		user.setPermissions(permissions);
		user.setAllowedServiceIds(allowedServiceIds);
		return user;
	}
	
	private UserInsertResult doUserInsert(final Connection con, final String domainId, final String userId, final String userSid, final UserBase user, final BitFlags<UserProcessOpt> options, final String defaultGroup) throws WTException {
		UserDAO useDao = UserDAO.getInstance();
		UserInfoDAO uinDao = UserInfoDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		final boolean subjectsAsSID = options.has(UserProcessOpt.SUBJECTS_AS_SID);
		
		OUserInfo ouin = AppManagerUtils.fillOUserInfo(new OUserInfo(), user);
		ouin.setDomainId(domainId);
		ouin.setUserId(userId);
		ouin.sanitize();
		OUser.fillDefaultsForInsert(ouin, userId, domainIdToDomainName(domainId));
		
		// Make sure set email is valid
		if (InternetAddressUtils.toInternetAddress(ouin.getEmail()) == null) throw new WTException("Invalid personal email address: '{}'", ouin.getEmail());
		
		OUser ouse = AppManagerUtils.fillOUser(new OUser(), user);
		ouse.setDomainId(domainId);
		ouse.setUserId(userId);
		ouse.setUserSid(userSid);
		ouse.sanitize();
		OUser.fillDefaultsForInsert(ouse, ouin.getFirstName(), ouin.getLastName());
		
		uinDao.insert(con, ouin);
		useDao.insert(con, ouse);
		
		if (options.has(UserProcessOpt.PROCESS_GROUPASSOCIATIONS) || !StringUtils.isBlank(defaultGroup)) {
			if (!StringUtils.isBlank(defaultGroup)) {
				if (user.getAssignedGroups() != null) {
					user.getAssignedGroups().add(defaultGroup);
				} else {
					user.setAssignedGroups(LangUtils.asSet(defaultGroup));
				}
			}
			Check.notNull(user.getAssignedGroups(), "assignedGroups");
			Set<String> assigned = parseSubjectsAsSids(user.getAssignedGroups(), !subjectsAsSID, domainId, GenericSubject.Type.GROUP);
			uasDao.batchInsert(con, ouse.getUserSid(), assigned);
		}
		if (options.has(UserProcessOpt.PROCESS_ROLEASSOCIATIONS) && (user.getAssignedRoles() != null)) {
			Check.notNull(user.getAssignedRoles(), "assignedRoles");
			Set<String> assigned = parseSubjectsAsSids(user.getAssignedRoles(), !subjectsAsSID, domainId, GenericSubject.Type.ROLE);
			rasDao.batchInsert(con, ouse.getUserSid(), assigned);
		}
		if (options.has(UserProcessOpt.PROCESS_PERMISSIONS) && (user.getPermissions() != null)) {
			Check.notNull(user.getPermissions(), "permissions");
			int[] ret1 = permsDao.batchInsertOfSubject(con, ouse.getUserSid(), toPermissionBatchInsertEntries(user.getPermissions()));
		}
		if (options.has(UserProcessOpt.PROCESS_SERVICEPERMISSIONS) && (user.getAllowedServiceIds() != null)) {
			Check.notNull(user.getAllowedServiceIds(), "allowedServiceIds");
			int[] ret2 = permsDao.batchInsertOfSubject(con, ouse.getUserSid(), toServicePermissionBatchInsertEntries(user.getAllowedServiceIds()));
		}
		
		return new UserInsertResult(ouse, ouin);
	}
	
	private void doUserUpdate(final Connection con, final String domainId, final String userId, final UserBase user, final BitFlags<UserProcessOpt> options) throws WTException {
		UserDAO useDao = UserDAO.getInstance();
		UserInfoDAO uinDao = UserInfoDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		UserProfileId userPid = new UserProfileId(domainId, userId);
		final boolean subjectsAsSID = options.has(UserProcessOpt.SUBJECTS_AS_SID);
		String userSid = lookupSubjectSid(userPid, GenericSubject.Type.USER);
		
		OUserInfo ouin = AppManagerUtils.fillOUserInfo(new OUserInfo(), user);
		ouin.setDomainId(domainId);
		ouin.setUserId(userId);
		
		OUser ouse = AppManagerUtils.fillOUser(new OUser(), user);
		ouse.setDomainId(domainId);
		ouse.setUserId(userId);
		
		boolean ret = uinDao.updateFirstLastName(con, ouin) == 1;
		if (ret) ret = useDao.updateEnabledDisplayName(con, ouse) == 1;
		if (!ret) throw new WTNotFoundException("User not found [{}]", userPid);
		
		if (options.has(UserProcessOpt.PROCESS_GROUPASSOCIATIONS)) {
			Check.notNull(user.getAssignedGroups(), "assignedGroups");
			Set<String> assigned = parseSubjectsAsSids(user.getAssignedGroups(), !subjectsAsSID, domainId, GenericSubject.Type.GROUP);
			LangUtils.CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(uasDao.viewGroupSidsByUser(con, userSid), assigned);
			uasDao.batchInsert(con, userSid, changeSet.inserted);
			uasDao.deleteByUserGroups(con, userSid, changeSet.deleted);
		}
		if (options.has(UserProcessOpt.PROCESS_ROLEASSOCIATIONS)) {
			Check.notNull(user.getAssignedRoles(), "assignedRoles");
			Set<String> assigned = parseSubjectsAsSids(user.getAssignedRoles(), !subjectsAsSID, domainId, GenericSubject.Type.ROLE);
			LangUtils.CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(rasDao.viewRoleSidsBySubject(con, userSid), assigned);
			rasDao.batchInsert(con, userSid, changeSet.inserted);
			rasDao.deleteBySubjectRolesSids(con, userSid, changeSet.deleted);
		}
		if (options.has(UserProcessOpt.PROCESS_PERMISSIONS)) {
			Check.notNull(user.getPermissions(), "permissions");
			Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, userSid, RolePermissionDAO.createServicePermissionAndKeysExcludeCondition(buildFolderShareKeysALL("%"))).entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					(entry) -> toSubjectEntry(entry.getValue())
				));
			//Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, userSid, RolePermissionDAO.createServicePermissionExcludeCondition());
			LangUtils.CollectionChangeSet<RolePermissionDAO.SubjectEntry> changeSet = LangUtils.getCollectionChanges(map.values(), toPermissionBatchInsertEntries(user.getPermissions()));
			prmDao.batchInsertOfSubject(con, userSid, changeSet.inserted);
			if (!changeSet.deleted.isEmpty()) {
				Map<RolePermissionDAO.SubjectEntry, Integer> inversedMap = map.entrySet().stream()
					.collect(Collectors.toMap(Entry::getValue, Entry::getKey));
				List<Integer> ids = changeSet.deleted.stream()
					.map((subjectEntry) -> inversedMap.get(subjectEntry))
					.filter((id) -> id != null)
					.collect(Collectors.toList());
				prmDao.deleteByIds(con, ids);
			}
			
		}
		if (options.has(UserProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Check.notNull(user.getAllowedServiceIds(), "allowedServiceIds");
			Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, userSid, RolePermissionDAO.createServicePermissionOnlyCondition()).entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					(entry) -> toSubjectEntry(entry.getValue())
				));
			//Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, userSid, RolePermissionDAO.createServicePermissionOnlyCondition());
			LangUtils.CollectionChangeSet<RolePermissionDAO.SubjectEntry> changeSet = LangUtils.getCollectionChanges(map.values(), toServicePermissionBatchInsertEntries(user.getAllowedServiceIds()));
			prmDao.batchInsertOfSubject(con, userSid, changeSet.inserted);
			if (!changeSet.deleted.isEmpty()) {
				Map<RolePermissionDAO.SubjectEntry, Integer> inversedMap = map.entrySet().stream()
					.collect(Collectors.toMap(Entry::getValue, Entry::getKey));
				List<Integer> ids = changeSet.deleted.stream()
					.map((subjectEntry) -> inversedMap.get(subjectEntry))
					.filter((id) -> id != null)
					.collect(Collectors.toList());
				prmDao.deleteByIds(con, ids);
			}
		}
	}
	
	private boolean doUserDelete(final Connection con, final String domainId, final String userId) throws WTNotFoundException {
		UserDAO useDao = UserDAO.getInstance();
		UserInfoDAO uinDao = UserInfoDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		
		UserProfileId userPid = new UserProfileId(domainId, userId);
		String userSid = subjectSidCache.getSid(userPid, GenericSubject.Type.USER);
		if (userSid == null) throw new WTNotFoundException("User UID not found [{}@{}]", userId, domainId);
		
		LOGGER.debug("[{}] Deleting permissions of '{}'...", userId, userSid);
		prmDao.deleteBySubject(con, userSid);
		LOGGER.debug("[{}] Deleting groups-associations of '{}'...", userId, userSid);
		uasDao.deleteByUser(con, userSid);
		LOGGER.debug("[{}] Deleting roles-associations of '{}'...", userId, userSid);
		rasDao.deleteBySubject(con, userSid);
		LOGGER.debug("[{}] Deleting domain user '{}'...", userId, userPid.toString());
		uinDao.deleteByDomainUser(con, domainId, userId);
		return useDao.deleteByDomainUser(con, domainId, userId) == 1;
	}
	
	private static class UserInsertResult {
		public final OUser ouser;
		public final OUserInfo ouserinfo;
		
		public UserInsertResult(OUser ouser, OUserInfo ouserinfo) {
			this.ouser = ouser;
			this.ouserinfo = ouserinfo;
		}
	}
	
	private static enum GroupProcessOpt implements BitFlagsEnum<GroupProcessOpt> {
		SUBJECTS_AS_SID(1 << 0), PROCESS_USERASSOCIATIONS(1 << 1), PROCESS_ROLEASSOCIATIONS(1 << 2), PROCESS_PERMISSIONS(1 << 3), PROCESS_SERVICEPERMISSIONS(1 << 4);
		
		private int mask = 0;
		private GroupProcessOpt(int mask) { this.mask = mask; }
		@Override
		public long mask() { return this.mask; }
		
		public static BitFlags<GroupProcessOpt> fromGroupGetOptions(BitFlags<GroupGetOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(GroupProcessOpt.class, options);
		}
		
		public static BitFlags<GroupProcessOpt> fromGroupUpdateOptions(BitFlags<GroupUpdateOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(GroupProcessOpt.class, options);
		}
	}
	
	private Group doGroupGet(final Connection con, final String domainId, final String groupId, final BitFlags<GroupProcessOpt> options) throws WTException {
		GroupDAO grpDao = GroupDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		final boolean subjectsAsSID = options.has(GroupProcessOpt.SUBJECTS_AS_SID);
		
		OGroup ogroup = grpDao.selectByProfile(con, domainId, groupId);
		if (ogroup == null) return null;
		
		Set<String> assignedUsers = null;
		if (options.has(GroupProcessOpt.PROCESS_USERASSOCIATIONS)) {
			assignedUsers = uasDao.viewUserSidsByGroup(con, ogroup.getGroupSid());
			if (!subjectsAsSID) {
				assignedUsers = assignedUsers.stream()
					.map((userSid) -> subjectSidCache.getPid(userSid, GenericSubject.Type.USER))
					.filter((userPid) -> userPid != null)
					.map((userPid) -> userPid.toString())
					.collect(Collectors.toSet());
			}
		}
		Set<String> assignedRoles = null;
		if (options.has(GroupProcessOpt.PROCESS_ROLEASSOCIATIONS)) {
			assignedRoles = rasDao.viewRoleSidsBySubject(con, ogroup.getGroupSid());
			if (!subjectsAsSID) {
				assignedRoles = assignedRoles.stream()
					.map((roleSid) -> subjectSidCache.getPid(roleSid, GenericSubject.Type.ROLE))
					.filter((rolePid) -> rolePid != null)
					.map((rolePid) -> rolePid.toString())
					.collect(Collectors.toSet());
			}
		}
		
		Set<PermissionString> permissions = null;
		if (options.has(GroupProcessOpt.PROCESS_PERMISSIONS)) {
			Map<Integer, ORolePermission> map = prmDao.viewSubjectEntriesBySubjectCondition(con, ogroup.getGroupSid(), RolePermissionDAO.createServicePermissionAndKeysExcludeCondition(buildFolderShareKeysALL("%")));
			permissions = map.values().stream()
				.map((orperm) -> toPermissionString(orperm))
				.collect(Collectors.toSet());
		}
		
		Set<String> allowedServiceIds = null;
		if (options.has(GroupProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Map<Integer, ORolePermission> map = prmDao.viewSubjectEntriesBySubjectCondition(con, ogroup.getGroupSid(), RolePermissionDAO.createServicePermissionOnlyCondition());
			allowedServiceIds = map.values().stream()
				.map((orperm) -> orperm.getInstance())
				.collect(Collectors.toSet());
		}
		
		Group group = AppManagerUtils.fillGroup(new Group(BUILT_IN_GROUPS.contains(groupId)), ogroup);
		group.setAssignedUsers(assignedUsers);
		group.setAssignedRoles(assignedRoles);
		group.setPermissions(permissions);
		group.setAllowedServiceIds(allowedServiceIds);
		return group;
	}
	
	private GroupInsertResult doGroupInsert(final Connection con, final String domainId, final String groupId, final String description) throws WTException {
		GroupBase group = new GroupBase();
		group.setDescription(description);
		return doGroupInsert(con, domainId, groupId, null, group, BitFlags.noneOf(GroupProcessOpt.class));
	}
	
	private GroupInsertResult doGroupInsert(final Connection con, final String domainId, final String groupId, final String groupSid, final GroupBase group, final BitFlags<GroupProcessOpt> options) throws WTException {
		GroupDAO grpDao = GroupDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		final boolean subjectsAsSID = options.has(GroupProcessOpt.SUBJECTS_AS_SID);
		
		OGroup ogroup = AppManagerUtils.fillOGroup(new OGroup(), group);
		ogroup.setDomainId(domainId);
		ogroup.setUserId(groupId);
		ogroup.setGroupSid(groupSid);
		OGroup.fillDefaultsForInsert(ogroup);
		OGroup.validate(ogroup);
		
		grpDao.insert(con, ogroup);
		if (options.has(GroupProcessOpt.PROCESS_USERASSOCIATIONS)) {
			Check.notNull(group.getAssignedUsers(), "assignedUsers");
			Set<String> assigned = parseSubjectsAsSids(group.getAssignedUsers(), !subjectsAsSID, domainId, GenericSubject.Type.USER);
			uasDao.batchInsert(con, assigned, ogroup.getGroupSid());
		}
		if (options.has(GroupProcessOpt.PROCESS_ROLEASSOCIATIONS)) {
			Check.notNull(group.getAssignedRoles(), "assignedRoles");
			Set<String> assigned = parseSubjectsAsSids(group.getAssignedRoles(), !subjectsAsSID, domainId, GenericSubject.Type.ROLE);
			rasDao.batchInsert(con, ogroup.getGroupSid(), assigned);
		}
		if (options.has(GroupProcessOpt.PROCESS_PERMISSIONS)) {
			Check.notNull(group.getPermissions(), "permissions");
			int[] ret1 = permsDao.batchInsertOfSubject(con, ogroup.getGroupSid(), toPermissionBatchInsertEntries(group.getPermissions()));
		}
		if (options.has(GroupProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Check.notNull(group.getAllowedServiceIds(), "allowedServiceIds");
			int[] ret2 = permsDao.batchInsertOfSubject(con, ogroup.getGroupSid(), toServicePermissionBatchInsertEntries(group.getAllowedServiceIds()));
		}
		
		return new GroupInsertResult(ogroup);
	}
	
	private void doGroupUpdate(final Connection con, final String domainId, final String groupId, final GroupBase group, final BitFlags<GroupProcessOpt> options) throws WTException {
		GroupDAO grpDao = GroupDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		UserProfileId groupPid = new UserProfileId(domainId, groupId);
		
		final boolean subjectsAsSID = options.has(GroupProcessOpt.SUBJECTS_AS_SID);
		String groupSid = lookupSubjectSid(groupPid, GenericSubject.Type.GROUP);
		
		boolean ret = grpDao.updateDescriptionByProfile(con, domainId, groupId, group.getDescription()) == 1;
		if (!ret) throw new WTNotFoundException("Group not found [{}]", groupPid);
		
		if (options.has(GroupProcessOpt.PROCESS_USERASSOCIATIONS)) {
			Check.notNull(group.getAssignedUsers(), "assignedUsers");
			Set<String> assigned = parseSubjectsAsSids(group.getAssignedUsers(), !subjectsAsSID, domainId, GenericSubject.Type.USER);
			LangUtils.CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(uasDao.viewUserSidsByGroup(con, groupSid), assigned);
			uasDao.batchInsert(con, changeSet.inserted, groupSid);
			uasDao.deleteByGroupUsers(con, groupSid, changeSet.deleted);
		}
		if (options.has(GroupProcessOpt.PROCESS_ROLEASSOCIATIONS)) {
			Check.notNull(group.getAssignedRoles(), "assignedRoles");
			Set<String> assigned = parseSubjectsAsSids(group.getAssignedRoles(), !subjectsAsSID, domainId, GenericSubject.Type.ROLE);
			LangUtils.CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(rasDao.viewRoleSidsBySubject(con, groupSid), assigned);
			rasDao.batchInsert(con, groupSid, changeSet.inserted);
			rasDao.deleteBySubjectRolesSids(con, groupSid, changeSet.deleted);
		}
		if (options.has(GroupProcessOpt.PROCESS_PERMISSIONS)) {
			Check.notNull(group.getPermissions(), "permissions");
			Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, groupSid, RolePermissionDAO.createServicePermissionAndKeysExcludeCondition(buildFolderShareKeysALL("%"))).entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					(entry) -> toSubjectEntry(entry.getValue())
				));
			LangUtils.CollectionChangeSet<RolePermissionDAO.SubjectEntry> changeSet = LangUtils.getCollectionChanges(map.values(), toPermissionBatchInsertEntries(group.getPermissions()));
			prmDao.batchInsertOfSubject(con, groupSid, changeSet.inserted);
			if (!changeSet.deleted.isEmpty()) {
				Map<RolePermissionDAO.SubjectEntry, Integer> inversedMap = map.entrySet().stream()
					.collect(Collectors.toMap(Entry::getValue, Entry::getKey));
				List<Integer> ids = changeSet.deleted.stream()
					.map((subjectEntry) -> inversedMap.get(subjectEntry))
					.filter((id) -> id != null)
					.collect(Collectors.toList());
				prmDao.deleteByIds(con, ids);
			}
			
		}
		if (options.has(GroupProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Check.notNull(group.getAllowedServiceIds(), "allowedServiceIds");
			Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, groupSid, RolePermissionDAO.createServicePermissionOnlyCondition()).entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					(entry) -> toSubjectEntry(entry.getValue())
				));
			LangUtils.CollectionChangeSet<RolePermissionDAO.SubjectEntry> changeSet = LangUtils.getCollectionChanges(map.values(), toServicePermissionBatchInsertEntries(group.getAllowedServiceIds()));
			prmDao.batchInsertOfSubject(con, groupSid, changeSet.inserted);
			if (!changeSet.deleted.isEmpty()) {
				Map<RolePermissionDAO.SubjectEntry, Integer> inversedMap = map.entrySet().stream()
					.collect(Collectors.toMap(Entry::getValue, Entry::getKey));
				List<Integer> ids = changeSet.deleted.stream()
					.map((subjectEntry) -> inversedMap.get(subjectEntry))
					.filter((id) -> id != null)
					.collect(Collectors.toList());
				prmDao.deleteByIds(con, ids);
			}
		}
	}
	
	private boolean doGroupDelete(final Connection con, final String domainId, final String groupId) throws WTNotFoundException {
		GroupDAO grpDao = GroupDAO.getInstance();
		UserAssociationDAO uasDao = UserAssociationDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		
		UserProfileId groupPid = new UserProfileId(domainId, groupId);
		String groupSid = subjectSidCache.getSid(groupPid, GenericSubject.Type.GROUP);
		if (groupSid == null) throw new WTNotFoundException("Group UID not found [{}]", groupPid);
		
		LOGGER.debug("[{}] Deleting permissions of '{}'...", groupPid, groupSid);
		prmDao.deleteBySubject(con, groupSid);
		LOGGER.debug("[{}] Deleting groups-associations of '{}'...", groupPid, groupSid);
		uasDao.deleteByGroup(con, groupSid);
		LOGGER.debug("[{}] Deleting roles-associations of '{}'...", groupPid, groupSid);
		rasDao.deleteBySubject(con, groupSid);
		LOGGER.debug("[{}] Deleting domain user...", groupPid);
		return grpDao.deleteByProfile(con, domainId, groupId) == 1;
	}
	
	private static class GroupInsertResult {
		public final OGroup ogroup;
		
		public GroupInsertResult(OGroup ogroup) {
			this.ogroup = ogroup;
		}
	}
	
	private static enum RoleProcessOpt implements BitFlagsEnum<RoleProcessOpt> {
		SUBJECTS_AS_SID(1 << 0), PROCESS_SUBJECTASSOCIATIONS(1 << 1), PROCESS_PERMISSIONS(1 << 3), PROCESS_SERVICEPERMISSIONS(1 << 4);//, SUBJECTS_AS_PIDS(1 << 16);
		
		private int mask = 0;
		private RoleProcessOpt(int mask) { this.mask = mask; }
		@Override
		public long mask() { return this.mask; }
		
		public static BitFlags<RoleProcessOpt> fromRoleGetOptions(BitFlags<RoleGetOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(RoleProcessOpt.class, options);
		}
		
		public static BitFlags<RoleProcessOpt> fromRoleUpdateOptions(BitFlags<RoleUpdateOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(RoleProcessOpt.class, options);
		}
	}
	
	private Role doRoleGet(final Connection con, final String domainId, final String roleId, final BitFlags<RoleProcessOpt> options) throws WTException {
		RoleDAO rolDao = RoleDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		final boolean subjectsAsSID = options.has(RoleProcessOpt.SUBJECTS_AS_SID);
		ORole orole = rolDao.selectByProfile(con, domainId, roleId);
		if (orole == null) return null;
		
		Set<String> assignedSubjects = null;
		if (options.has(RoleProcessOpt.PROCESS_SUBJECTASSOCIATIONS)) {
			assignedSubjects = rasDao.viewRoleSidsBySubject(con, orole.getRoleSid());
			if (!subjectsAsSID) {
				assignedSubjects = assignedSubjects.stream()
					.map((roleSid) -> subjectSidCache.getPid(roleSid, GenericSubject.Type.USER, GenericSubject.Type.GROUP))
					.filter((rolePid) -> rolePid != null)
					.map((rolePid) -> rolePid.toString())
					.collect(Collectors.toSet());
			}
		}
		
		Set<PermissionString> permissions = null;
		if (options.has(RoleProcessOpt.PROCESS_PERMISSIONS)) {
			Map<Integer, ORolePermission> map = prmDao.viewSubjectEntriesBySubjectCondition(con, orole.getRoleSid(), RolePermissionDAO.createServicePermissionAndKeysExcludeCondition(buildFolderShareKeysALL("%")));
			permissions = map.values().stream()
				.map((orperm) -> toPermissionString(orperm))
				.collect(Collectors.toSet());
		}
		
		Set<String> allowedServiceIds = null;
		if (options.has(RoleProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Map<Integer, ORolePermission> map = prmDao.viewSubjectEntriesBySubjectCondition(con, orole.getRoleSid(), RolePermissionDAO.createServicePermissionOnlyCondition());
			allowedServiceIds = map.values().stream()
				.map((orperm) -> orperm.getInstance())
				.collect(Collectors.toSet());
		}
		
		Role role = AppManagerUtils.fillRole(new Role(false), orole);
		role.setAssignedSubjects(assignedSubjects);
		role.setPermissions(permissions);
		role.setAllowedServiceIds(allowedServiceIds);
		return role;
	}
	
	private RoleInsertResult doRoleInsert(final Connection con, final String domainId, final String roleId, final String roleSid, final RoleBase role, final BitFlags<RoleProcessOpt> options) throws WTException {
		RoleDAO rolDao = RoleDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		final boolean subjectsAsSID = options.has(RoleProcessOpt.SUBJECTS_AS_SID);
		
		ORole orole = AppManagerUtils.fillORole(new ORole(), role);
		orole.setDomainId(domainId);
		orole.setRoleId(roleId);
		orole.setRoleSid(roleSid);
		ORole.fillDefaultsForInsert(orole);
		ORole.validate(orole);
		
		rolDao.insert(con, orole);
		if (options.has(RoleProcessOpt.PROCESS_SUBJECTASSOCIATIONS)) {
			Check.notNull(role.getAssignedSubjects(), "assignedSubjects");
			Set<String> assigned = parseSubjectsAsSids(role.getAssignedSubjects(), !subjectsAsSID, domainId, GenericSubject.Type.USER, GenericSubject.Type.GROUP);
			rasDao.batchInsert(con, assigned, orole.getRoleSid());
		}
		if (options.has(RoleProcessOpt.PROCESS_PERMISSIONS)) {
			Check.notNull(role.getPermissions(), "permissions");
			int[] ret1 = permsDao.batchInsertOfSubject(con, orole.getRoleSid(), toPermissionBatchInsertEntries(role.getPermissions()));
		}
		if (options.has(RoleProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Check.notNull(role.getAllowedServiceIds(), "allowedServiceIds");
			int[] ret2 = permsDao.batchInsertOfSubject(con, orole.getRoleSid(), toServicePermissionBatchInsertEntries(role.getAllowedServiceIds()));
		}
		
		return new RoleInsertResult(orole);
	}
	
	private void doRoleUpdate(final Connection con, final String domainId, final String roleId, final RoleBase role, final BitFlags<RoleProcessOpt> options) throws WTException {
		RoleDAO rolDao = RoleDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		UserProfileId rolePid = new UserProfileId(domainId, roleId);
		final boolean subjectsAsSID = options.has(RoleProcessOpt.SUBJECTS_AS_SID);
		String roleSid = lookupSubjectSid(rolePid, GenericSubject.Type.ROLE);
		
		boolean ret = rolDao.updateDescriptionByProfile(con, domainId, roleId, role.getDescription()) == 1;
		if (!ret) throw new WTNotFoundException("Role not found [{}]", rolePid);
		
		if (options.has(RoleProcessOpt.PROCESS_SUBJECTASSOCIATIONS)) {
			Check.notNull(role.getAssignedSubjects(), "assignedSubjects");
			Set<String> assigned = parseSubjectsAsSids(role.getAssignedSubjects(), !subjectsAsSID, domainId, GenericSubject.Type.USER, GenericSubject.Type.GROUP);
			LangUtils.CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(rasDao.viewSubjectSidsByRole(con, roleSid), assigned);
			rasDao.batchInsert(con, changeSet.inserted, roleSid);
			rasDao.deleteBySubjectsRoleSid(con, changeSet.deleted, roleSid);
		}
		if (options.has(RoleProcessOpt.PROCESS_PERMISSIONS)) {
			Check.notNull(role.getPermissions(), "permissions");
			Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, roleSid, RolePermissionDAO.createServicePermissionAndKeysExcludeCondition(buildFolderShareKeysALL("%"))).entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					(entry) -> toSubjectEntry(entry.getValue())
				));
			LangUtils.CollectionChangeSet<RolePermissionDAO.SubjectEntry> changeSet = LangUtils.getCollectionChanges(map.values(), toPermissionBatchInsertEntries(role.getPermissions()));
			prmDao.batchInsertOfSubject(con, roleSid, changeSet.inserted);
			if (!changeSet.deleted.isEmpty()) {
				Map<RolePermissionDAO.SubjectEntry, Integer> inversedMap = map.entrySet().stream()
					.collect(Collectors.toMap(Entry::getValue, Entry::getKey));
				List<Integer> ids = changeSet.deleted.stream()
					.map((subjectEntry) -> inversedMap.get(subjectEntry))
					.filter((id) -> id != null)
					.collect(Collectors.toList());
				prmDao.deleteByIds(con, ids);
			}
			
		}
		if (options.has(RoleProcessOpt.PROCESS_SERVICEPERMISSIONS)) {
			Check.notNull(role.getAllowedServiceIds(), "allowedServiceIds");
			Map<Integer, RolePermissionDAO.SubjectEntry> map = prmDao.viewSubjectEntriesBySubjectCondition(con, roleSid, RolePermissionDAO.createServicePermissionOnlyCondition()).entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					(entry) -> toSubjectEntry(entry.getValue())
				));
			LangUtils.CollectionChangeSet<RolePermissionDAO.SubjectEntry> changeSet = LangUtils.getCollectionChanges(map.values(), toServicePermissionBatchInsertEntries(role.getAllowedServiceIds()));
			prmDao.batchInsertOfSubject(con, roleSid, changeSet.inserted);
			if (!changeSet.deleted.isEmpty()) {
				Map<RolePermissionDAO.SubjectEntry, Integer> inversedMap = map.entrySet().stream()
					.collect(Collectors.toMap(Entry::getValue, Entry::getKey));
				List<Integer> ids = changeSet.deleted.stream()
					.map((subjectEntry) -> inversedMap.get(subjectEntry))
					.filter((id) -> id != null)
					.collect(Collectors.toList());
				prmDao.deleteByIds(con, ids);
			}
		}
	}
	
	private boolean doRoleDelete(final Connection con, final String domainId, final String roleId) throws WTNotFoundException {
		RoleDAO rolDao = RoleDAO.getInstance();
		RoleAssociationDAO rasDao = RoleAssociationDAO.getInstance();
		RolePermissionDAO prmDao = RolePermissionDAO.getInstance();
		
		UserProfileId rolePid = new UserProfileId(domainId, roleId);
		String roleSid = subjectSidCache.getSid(rolePid, GenericSubject.Type.ROLE);
		if (roleSid == null) throw new WTNotFoundException("Role UID not found [{}]", rolePid);
		
		LOGGER.debug("[{}] Deleting permissions of '{}'...", rolePid, roleSid);
		prmDao.deleteBySubject(con, roleSid);
		LOGGER.debug("[{}] Deleting roles-associations of '{}'...", rolePid, roleSid);
		rasDao.deleteByRole(con, roleSid);
		LOGGER.debug("[{}] Deleting domain role '{}'...", rolePid, roleSid);
		return rolDao.deleteBySid(con, roleSid) == 1;
	}
	
	private static class RoleInsertResult {
		public final ORole orole;
		
		public RoleInsertResult(ORole orole) {
			this.orole = orole;
		}
	}
	
	private static enum ResourceProcessOpt implements BitFlagsEnum<ResourceProcessOpt> {
		SUBJECTS_AS_SID(1 << 0), PROCESS_PERMISSIONS(1 << 1);
		
		private int mask = 0;
		private ResourceProcessOpt(int mask) { this.mask = mask; }
		@Override
		public long mask() { return this.mask; }
		
		public static BitFlags<ResourceProcessOpt> fromResourceGetOptions(BitFlags<ResourceGetOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(ResourceProcessOpt.class, options);
		}
		
		public static BitFlags<ResourceProcessOpt> fromResourceUpdateOptions(BitFlags<ResourceUpdateOption> options) {
			// Flag values are compatible for now, simpy instantiate new object...
			return BitFlags.newFrom(ResourceProcessOpt.class, options);
		}
	}
	
	private Resource doResourceGet(final Connection con, final String domainId, final String resourceId, final BitFlags<ResourceProcessOpt> options) throws WTException {
		ResourceDAO resDao = ResourceDAO.getInstance();
		final boolean subjectsAsSID = options.has(ResourceProcessOpt.SUBJECTS_AS_SID);
		
		VResource vre = resDao.selectByProfile(con, domainId, resourceId);
		if (vre == null) return null;
		
		ResourcePermissions permissions = null;
		if (options.has(ResourceProcessOpt.PROCESS_PERMISSIONS)) {
			permissions = doResourcePermissionsGet(con, domainId, resourceId, subjectsAsSID);
		}
		
		Resource resource = AppManagerUtils.fillResource(new Resource(), vre);
		if (permissions != null) {
			resource.setManagerSubject(permissions.getManagerSubject());
			resource.setAllowedSubjects(permissions.getAllowedSubjects());
		}
		return resource;
	}
	
	private ResourcePermissions doResourcePermissionsGet(final Connection con, final String domainId, final String resourceId, final boolean subjectsAsSID) {
		final UserProfileId resourcePid = new UserProfileId(domainId, resourceId);
		String manager = null;
		Set<String> alloweds = new LinkedHashSet<>();
		String originSid = subjectSidCache.getSid(resourcePid, GenericSubject.Type.RESOURCE);
		
		Set<FolderSharing.SubjectConfiguration> configurations = doFolderShareConfigurationsGet(con, originSid, CoreManifest.ID, "RESOURCE", FolderSharing.Scope.wildcard(), null);
		for (FolderSharing.SubjectConfiguration configuration : configurations) {
			if (hasResourceManagerPermissions(configuration.getFolderPermissions(), configuration.getItemsPermissions())) {
				if (manager == null) { // Safe-check, should not happen to have 2 managers... in that case take the first!
					manager = (!subjectsAsSID) ? subjectSidCache.getPidAsString(configuration.getSubjectSid(), GenericSubject.Type.USER, GenericSubject.Type.GROUP) : configuration.getSubjectSid();
				}
			} else if (configuration.getFolderPermissions().has(FolderShare.FolderRight.READ)) {
				final String allowed = (!subjectsAsSID) ? subjectSidCache.getPidAsString(configuration.getSubjectSid(), GenericSubject.Type.USER, GenericSubject.Type.GROUP) : configuration.getSubjectSid();
				if (allowed != null) alloweds.add(allowed);
			}
		}
		return new ResourcePermissions(manager, alloweds);
	}
	
	private ResourceUpdateResult doResourceInsert(final Connection con, final String domainId, final String resourceId, final String resourceSid, final ResourceBase resource, final BitFlags<ResourceProcessOpt> options) throws WTException {
		ResourceDAO resDao = ResourceDAO.getInstance();
		final boolean subjectsAsSID = options.has(ResourceProcessOpt.SUBJECTS_AS_SID);
		
		OUserInfo ouin = AppManagerUtils.fillOUserInfo(new OUserInfo(), resource);
		ouin.setDomainId(domainId);
		ouin.setUserId(resourceId);
		OResource.fillDefaultsForInsert(ouin, resourceId, domainIdToDomainName(domainId));
		OResource.validate(ouin);
		
		OUser ouse = AppManagerUtils.fillOUser(new OUser(), resource);
		ouse.setDomainId(domainId);
		ouse.setUserId(resourceId);
		ouse.setUserSid(resourceSid);
		OResource.fillDefaultsForInsert(ouse);
		OResource.validate(ouse);
		
		resDao.insert(con, ouin);
		resDao.insert(con, ouse);
		
		Set<String> newPermissionSids = new HashSet<>();
		if (options.has(ResourceProcessOpt.PROCESS_PERMISSIONS)) {
			Check.notNull(resource.getAllowedSubjects(), "allowedSubjects");
			
			// Update sharing
			Set<FolderSharing.SubjectConfiguration> configurations = new LinkedHashSet<>();
			if (!StringUtils.isBlank(resource.getManagerSubject())) {
				// This is treated as collection here, but extracted first below!
				Set<String> managers = parseSubjectsAsSids(Arrays.asList(resource.getManagerSubject()), !subjectsAsSID, domainId, GenericSubject.Type.USER, GenericSubject.Type.GROUP);
				if (!managers.isEmpty()) {
					final String managerSid = managers.iterator().next();
					newPermissionSids.add(managerSid);
					configurations.add(new FolderSharing.SubjectConfiguration(managerSid, buildResourceFolderPermissions(true), buildResourceItemsPermissions(true)));	
				}
			}
			Set<String> alloweds = parseSubjectsAsSids(resource.getAllowedSubjects(), !subjectsAsSID, domainId, GenericSubject.Type.USER, GenericSubject.Type.GROUP);
			for (String sid : alloweds) {
				newPermissionSids.add(sid);
				configurations.add(new FolderSharing.SubjectConfiguration(sid, buildResourceFolderPermissions(false)));
			}
			doFolderShareConfigurationUpdate(con, ouse.getUserSid(), CoreManifest.ID, "RESOURCE", FolderSharing.Scope.wildcard(), configurations, null);
		}
		
		return new ResourceUpdateResult(ouse, ouin, newPermissionSids);
	}
	
	private ResourceUpdateResult doResourceUpdate(final Connection con, final String domainId, final String resourceId, final ResourceBase resource, final BitFlags<ResourceProcessOpt> options) throws WTNotFoundException {
		ResourceDAO resDao = ResourceDAO.getInstance();
		final boolean subjectsAsSID = options.has(ResourceProcessOpt.SUBJECTS_AS_SID);
		
		OUserInfo ouin = AppManagerUtils.fillOUserInfo(new OUserInfo(), resource);
		ouin.setDomainId(domainId);
		ouin.setUserId(resourceId);
		OResource.fillDefaultsForInsert(ouin, resourceId, domainIdToDomainName(domainId));
		OResource.validate(ouin);
		
		OUser ouse = AppManagerUtils.fillOUser(new OUser(), resource);
		ouse.setDomainId(domainId);
		ouse.setUserId(resourceId);
		OResource.fillDefaultsForInsert(ouse);
		OResource.validate(ouse);
		
		boolean ret = resDao.update(con, ouin) == 1;
		if (ret) ret = resDao.update(con, ouse) == 1;
		if (!ret) throw new WTNotFoundException("Resource not found [{}@{}]", resourceId, domainId);
		
		Set<String> oldPermissionSids = null;
		Set<String> newPermissionSids = null;
		if (options.has(ResourceProcessOpt.PROCESS_PERMISSIONS)) {
			Check.notNull(resource.getAllowedSubjects(), "allowedSubjects");
			oldPermissionSids = doResourcePermissionsGet(con, domainId, resourceId, subjectsAsSID).getAllSubjects();
			newPermissionSids = new HashSet<>();
			
			// Update sharing
			Set<FolderSharing.SubjectConfiguration> configurations = new LinkedHashSet<>();
			if (!StringUtils.isBlank(resource.getManagerSubject())) {
				// This is treated as collection here, but extracted first below!
				Set<String> managers = parseSubjectsAsSids(Arrays.asList(resource.getManagerSubject()), !subjectsAsSID, domainId, GenericSubject.Type.USER, GenericSubject.Type.GROUP);
				if (!managers.isEmpty()) {
					// READ right for manager is implicit, managers can obviously manage resource fully!
					// Manager has also CREATE, DELETE, UPDATE right in order to maintain requests.
					final String managerSid = managers.iterator().next();
					newPermissionSids.add(managerSid);
					configurations.add(new FolderSharing.SubjectConfiguration(managerSid, buildResourceFolderPermissions(true), buildResourceItemsPermissions(true)));	
				}
			}
			Set<String> alloweds = parseSubjectsAsSids(resource.getAllowedSubjects(), !subjectsAsSID, domainId, GenericSubject.Type.USER, GenericSubject.Type.GROUP);
			for (String sid : alloweds) {
				// READ right allows to access the resource, any allowed subject must have it!
				newPermissionSids.add(sid);
				configurations.add(new FolderSharing.SubjectConfiguration(sid, buildResourceFolderPermissions(false)));
			}
			String originSid = subjectSidCache.getSid(ouse.getProfileId(), GenericSubject.Type.RESOURCE);
			doFolderShareConfigurationUpdate(con, originSid, CoreManifest.ID, "RESOURCE", FolderSharing.Scope.wildcard(), configurations, null);
		}
		
		return new ResourceUpdateResult(ouse, ouin, LangUtils.computeChangeSet(oldPermissionSids, newPermissionSids).getMoved());
	}
	
	private void doResourceDelete(final Connection con, final String domainId, final String resourceId) throws WTNotFoundException {
		UserInfoDAO uinDao = UserInfoDAO.getInstance();
		ResourceDAO resDao = ResourceDAO.getInstance();
		
		// Remove sharing
		String originSid = subjectSidCache.getSid(new UserProfileId(domainId, resourceId), GenericSubject.Type.RESOURCE);
		doFolderShareSharingRightsDelete(con, originSid, CoreManifest.ID, "RESOURCE", FolderSharing.Scope.wildcard());
		
		uinDao.deleteByDomainUser(con, domainId, resourceId);
		boolean ret = resDao.deleteByDomainId(con, domainId, resourceId) == 1;
		if (!ret) throw new WTNotFoundException("Resource not found [{}@{}]", resourceId, domainId);
	}
	
	private static boolean hasResourceManagerPermissions(final FolderShare.FolderPermissions folderPermissions, final FolderShare.ItemsPermissions itemsPermissions) {
		if (!folderPermissions.has(FolderShare.FolderRight.READ)) return false;
		if (!itemsPermissions.has(FolderShare.ItemsRight.CREATE)) return false;
		if (!itemsPermissions.has(FolderShare.ItemsRight.DELETE)) return false;
		if (!itemsPermissions.has(FolderShare.ItemsRight.UPDATE)) return false;
		return true;
	}
	
	private static class ResourceUpdateResult {
		public final OUser ouser;
		public final OUserInfo ouserinfo;
		public final Set<String> changedPermissionSids;
		
		public ResourceUpdateResult(OUser ouser, OUserInfo ouserinfo, Set<String> changedPermissionSids) {
			this.ouser = ouser;
			this.ouserinfo = ouserinfo;
			this.changedPermissionSids = changedPermissionSids;
		}
	}
	
	/**
	 * Checks if passed SID is really valid: has a matching PID with a specified domain ID
	 */
	private boolean evalSidConstraints(String sid, String constrainDomainId, GenericSubject.Type... validTypes) {
		UserProfileId pid = subjectSidCache.getPid(sid, validTypes);
		if (pid == null) return false;
		return pid.hasDomain(constrainDomainId);
	}
	
	/**
	 * Checks if passed PID is really valid: has the specified domain ID and a matching SID
	 */
	private boolean evalPidConstraints(UserProfileId pid, String constrainDomainId, GenericSubject.Type... validTypes) {
		if (pid == null || !pid.hasDomain(constrainDomainId)) return false;
		String sid = subjectSidCache.getSid(pid, validTypes);
		return sid != null;
	}
	
	public Set<String> parseSubjectsAsStringPids(Collection<String> subjects, boolean subjectsAsPids, String validDomainId, GenericSubject.Type... validTypes) {
		return parseSubjectsAsPids(subjects, subjectsAsPids, validDomainId, validTypes)
			.stream()
			.map((pid) -> pid.toString())
			.collect(Collectors.toSet());
	}
	
	public Set<String> parseSubjectsAsStringLocals(Collection<String> subjects, boolean subjectsAsPids, String validDomainId, GenericSubject.Type... validTypes) {
		return parseSubjectsAsPids(subjects, subjectsAsPids, validDomainId, validTypes)
			.stream()
			.map((pid) -> pid.getLocal())
			.collect(Collectors.toSet());
	}
	
	public Set<UserProfileId> parseSubjectsAsPids(Collection<String> subjects, boolean subjectsAsPids, String validDomainId, GenericSubject.Type... validTypes) {
		if (subjectsAsPids) {
			return subjects.stream()
				// parses each value as ProfileId
				.map((spid) -> UserProfileId.parseQuielty(spid, validDomainId))
				// Filter out non-matching values from source (eventually they will be deleted when processing changes)
				.filter((pid) -> evalPidConstraints(pid, validDomainId, validTypes))
				.collect(Collectors.toSet());
			
		} else {
			// If passed subjects are sids...
			return subjects.stream()
				// skipping NULLs and objects with non-matching domain
				.filter((sid) -> (sid != null))
				// then lookup related PID
				.map((sid) -> subjectSidCache.getPid(sid, validTypes))
				// making sure that match was successful
				.filter((sid) -> (sid != null))
				.collect(Collectors.toSet());
		}
	}
	
	private Set<String> parseSubjectsAsSids(Collection<String> subjects, boolean subjectsAsPids, String validDomainId, GenericSubject.Type... validTypes) {
		if (subjectsAsPids) {
			// If passed subjects are pids...
			return subjects.stream()
				// parses each value as ProfileId
				.map((spid) -> UserProfileId.parseQuielty(spid, validDomainId))
				// skipping NULLs and objects with non-matching domain
				.filter((pid) -> (pid != null && pid.hasDomain(validDomainId)))
				// then lookup related SID
				.map((pid) -> subjectSidCache.getSid(pid, validTypes))
				// making sure that match was successful
				.filter((sid) -> (sid != null))
				.collect(Collectors.toSet());
		} else {
			return subjects.stream()
				// Filter out non-matching values from source (eventually they will be deleted when processing changes)
				.filter((sid) -> evalSidConstraints(sid, validDomainId, validTypes))
				.collect(Collectors.toSet());
		}
	}
	
	private static FolderShare.FolderPermissions buildResourceFolderPermissions(final boolean manager) {
		FolderShare.FolderPermissions perms = new FolderShare.FolderPermissions();
		perms.set(FolderShare.FolderRight.READ);
		return perms;
	}
	
	private static FolderShare.ItemsPermissions buildResourceItemsPermissions(final boolean manager) {
		FolderShare.ItemsPermissions perms = null;
		if (manager) {
			perms = new FolderShare.ItemsPermissions();
			perms.set(FolderShare.ItemsRight.CREATE);
			perms.set(FolderShare.ItemsRight.DELETE);
			perms.set(FolderShare.ItemsRight.UPDATE);	
		}
		return perms;
	}
	
	private void clearAuthorizationInfo(String domainId, Collection<String> subjects, boolean subjectsAsSID) throws WTException {
		WTRealm realm = (WTRealm)ShiroUtils.getRealmByName(WTRealm.NAME);
		for (UserProfileId profileId : expandSubjectsToUserProfiles(domainId, subjects, subjectsAsSID)) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] Clearing authz-info for profile '{}'", domainId, profileId);
			RunContext.clearCachedAuthorizationInfo(realm, RunContext.buildPrincipalCollection(profileId.getDomainId(), profileId.getUserId()));
		}
	}
	
	private OShare doShareInsert(Connection con, String originSid, String serviceId, String shareKey, String instance) {
		ShareDAO shaDao = ShareDAO.getInstance();
		
		OShare oshare = new OShare();
		oshare.setUserUid(originSid);
		oshare.setServiceId(serviceId);
		oshare.setKey(shareKey);
		oshare.setInstance(instance);
		oshare.setShareId(shaDao.getSequence(con).intValue());
		boolean ret = shaDao.insert(con, oshare) == 1;
		return !ret ? null : oshare;
	}
	
	private <T> Map<String, Sharing.SubjectConfiguration> doShareConfigurationsGet(final Connection con, final String originSid, final String serviceId, final String shareContext, final String shareInstance, final String permissionKey, final Class<T> typeOfData) throws DAOException {
		ShareDAO shaDao = ShareDAO.getInstance();
		ShareDataDAO shadDao = ShareDataDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		
		Map<String, Sharing.SubjectConfiguration> items = new LinkedHashMap<>();
		OShare oshare = shaDao.selectByUserServiceKeyInstance(con, originSid, serviceId, shareContext, shareInstance);
		if (oshare != null) {
			Map<String, List<ORolePermission>> permissionMap = permsDao.groupSubjectsByByServiceKeysInstance(con, serviceId, Arrays.asList(permissionKey), oshare.getShareIdAsString());
			Map<String, String> dataMap = shadDao.mapByShare(con, oshare.getShareId());
			LinkedHashSet<String> subjectSids = new LinkedHashSet<>();
			subjectSids.addAll(permissionMap.keySet());
			subjectSids.addAll(dataMap.keySet());
			for (String subjectSid : subjectSids) {
				if (permissionMap.containsKey(subjectSid)) {
					items.put(subjectSid, toSharingSubjectConfiguration(subjectSid, permissionMap.get(subjectSid), dataMap.get(subjectSid), typeOfData));
				}
			}
		}
		return items;
	}
	
	public <T> void doShareConfigurationsUpdate(final Connection con, final String originSid, final String serviceId, final String shareContext, final String shareInstance, final String permissionKey, final Set<Sharing.SubjectConfiguration> configurations, Class<T> typeOfData) throws DAOException {
		ShareDAO shaDao = ShareDAO.getInstance();
		ShareDataDAO shadDao = ShareDataDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		
		OShare oshare = shaDao.selectByUserServiceKeyInstance(con, originSid, serviceId, shareContext, shareInstance);
		if (oshare != null) {
			permsDao.deleteByServiceKeysInstance(con, serviceId, Arrays.asList(permissionKey), oshare.getShareIdAsString());
		} else {
			oshare = doShareInsert(con, originSid, serviceId, shareContext, shareInstance);
		}
		if (!configurations.isEmpty()) {
			// Update permissions
			Set<RolePermissionDAO.ServiceEntry> entries = toSharePermissionBatchInsertEntries(permissionKey, oshare.getShareIdAsString(), configurations);
			int[] ret = permsDao.batchInsertOfService(con, serviceId, entries);
			// Update data
			if (typeOfData != null) {
				for (Sharing.SubjectConfiguration configuration : configurations) {
					final String rawData = configuration.getRawData(typeOfData);
					if (StringUtils.isEmpty(rawData)) {
						shadDao.deleteByShareUser(con, oshare.getShareId(), configuration.getSubjectSid());
					} else {
						int ret1 = shadDao.update(con, oshare.getShareId(), configuration.getSubjectSid(), rawData);
						if (ret1 == 0) ret1 = shadDao.insert(con, oshare.getShareId(), configuration.getSubjectSid(), rawData);
					}
				}
			}
		}
	}
	
	private <T> Set<FolderSharing.SubjectConfiguration> doFolderShareConfigurationsGet(final Connection con, final String originSid, final String serviceId, final String shareContext, final FolderSharing.Scope level, final Class<T> typeOfData) throws DAOException {
		ShareDAO shaDao = ShareDAO.getInstance();
		ShareDataDAO shadDao = ShareDataDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		
		final List<String> TARGET_PERMISSION_KEYS = Arrays.asList(
			FolderShare.buildFolderPermissionKey(shareContext),
			FolderShare.buildItemsPermissionKey(shareContext)
		);

		boolean skipOriginPerms = true;
		String shareInstance = null;
		if (level instanceof FolderSharing.WildcardScope) {
			skipOriginPerms = false;
			shareInstance = FolderSharing.INSTANCE_WILDCARD;

		} else if (level instanceof FolderSharing.FolderScope) {
			FolderSharing.FolderScope flevel = ((FolderSharing.FolderScope)level);
			shareInstance = flevel.getFolderId();

		} else {
			throw new IllegalArgumentException("Unsupported level");
		}
		
		Set<FolderSharing.SubjectConfiguration> items = new LinkedHashSet<>();
		OShare oshare = shaDao.selectByUserServiceKeyInstance(con, originSid, serviceId, shareContext, shareInstance);
		if (oshare != null) {
			Map<String, List<ORolePermission>> permissionMap = permsDao.groupSubjectsByByServiceKeysInstance(con, serviceId, TARGET_PERMISSION_KEYS, oshare.getShareIdAsString());
			Map<String, String> dataMap = shadDao.mapByShare(con, oshare.getShareId());
			LinkedHashSet<String> subjectSids = new LinkedHashSet<>();
			subjectSids.addAll(permissionMap.keySet());
			subjectSids.addAll(dataMap.keySet());
			
			for (String subjectSid : subjectSids) {
				if (permissionMap.containsKey(subjectSid)) {
					items.add(toSharingSubjectConfiguration(subjectSid, permissionMap.get(subjectSid), dataMap.get(subjectSid), typeOfData, shareContext, skipOriginPerms));
				}
			}
		}
		return items;
	}
	
	public <T> void doFolderShareConfigurationUpdate(final Connection con, final String originSid, final String serviceId, final String shareContext, final FolderSharing.Scope level, final Set<FolderSharing.SubjectConfiguration> configurations, final Class<T> typeOfData) throws DAOException {
		ShareDAO shaDao = ShareDAO.getInstance();
		ShareDataDAO shadDao = ShareDataDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		
		final List<String> TARGET_PERMISSION_KEYS = buildFolderShareKeys(shareContext);

		boolean skipOriginPerms = true;
		String shareInstance = null;
		OShare oshare = null;
		if (level instanceof FolderSharing.WildcardScope) {
			skipOriginPerms = false;
			shareInstance = FolderSharing.INSTANCE_WILDCARD;

		} else if (level instanceof FolderSharing.FolderScope) {
			FolderSharing.FolderScope flevel = ((FolderSharing.FolderScope)level);
			shareInstance = flevel.getFolderId();

		} else {
			throw new IllegalArgumentException("Unsupported level");
		}

		oshare = shaDao.selectByUserServiceKeyInstance(con, originSid, serviceId, shareContext, shareInstance);
		if (oshare != null) {
			permsDao.deleteByServiceKeysInstance(con, serviceId, TARGET_PERMISSION_KEYS, oshare.getShareIdAsString());
		} else {
			oshare = doShareInsert(con, originSid, serviceId, shareContext, shareInstance);
		}
		if (!configurations.isEmpty()) {
			// Update permissions
			Set<RolePermissionDAO.ServiceEntry> entries = toSharePermissionBatchInsertEntries(shareContext, oshare.getShareIdAsString(), configurations, skipOriginPerms);
			int[] ret = permsDao.batchInsertOfService(con, serviceId, entries);
			// Update data
			//TODO...
		}
	}
	
	public void doFolderShareSharingRightsDelete(final Connection con, final String originSid, final String serviceId, final String context, final FolderSharing.Scope scope) throws DAOException {
		ShareDAO shaDao = ShareDAO.getInstance();
		RolePermissionDAO permsDao = RolePermissionDAO.getInstance();
		
		final List<String> TARGET_PERMISSION_KEYS = buildFolderShareKeys(context);
		
		String shareInstance = null;
		if (scope instanceof FolderSharing.WildcardScope) {
			shareInstance = FolderSharing.INSTANCE_WILDCARD;

		} else if (scope instanceof FolderSharing.FolderScope) {
			FolderSharing.FolderScope flevel = ((FolderSharing.FolderScope)scope);
			shareInstance = flevel.getFolderId();

		} else {
			throw new IllegalArgumentException("Unsupported level");
		}
		
		Set<Integer> deleted = shaDao.deleteByUserServiceKeyInstance(con, originSid, serviceId, context, shareInstance);
		if (!deleted.isEmpty()) {
			Set<String> instances = deleted.stream().map(id -> String.valueOf(id)).collect(Collectors.toSet());
			permsDao.deleteByServiceKeysInstances(con, serviceId, TARGET_PERMISSION_KEYS, instances);
		}
	}
	
	private <T> Sharing.SubjectConfiguration toSharingSubjectConfiguration(String subjectSid, Collection<ORolePermission> operms, String rawData, Class<T> typeOfData) {
		Set<String> actions = new LinkedHashSet<>();
		for (ORolePermission operm : operms) {
			actions.add(operm.getAction());
		}
		if (typeOfData != null) {
			return new Sharing.SubjectConfiguration(subjectSid, actions, rawData, typeOfData);
		} else {
			return new Sharing.SubjectConfiguration(subjectSid, actions);
		}
	}
	
	private <T> FolderSharing.SubjectConfiguration toSharingSubjectConfiguration(String subjectSid, Collection<ORolePermission> operms, String rawData, Class<T> typeOfData, String shareContext, boolean skipWildcardReservedRights) {
		final String FOLDER_PERMISSION_KEY = FolderShare.buildFolderPermissionKey(shareContext);
		final String ITEMS_PERMISSION_KEY = FolderShare.buildItemsPermissionKey(shareContext);
		
		FolderShare.FolderPermissions folderPerms = new FolderShare.FolderPermissions();
		FolderShare.ItemsPermissions itemsPerms = new FolderShare.ItemsPermissions();
		for (ORolePermission osubperm : operms) {
			if (FOLDER_PERMISSION_KEY.equals(osubperm.getKey())) {
				FolderShare.FolderRight right = EnumUtils.forName(osubperm.getAction(), FolderShare.FolderRight.class);
				if (right != null) {
					if (!skipWildcardReservedRights || !right.isReservedForWildcard()) folderPerms.set(right);
				}
				
			} else if (ITEMS_PERMISSION_KEY.equals(osubperm.getKey())) {
				FolderShare.ItemsRight right = EnumUtils.forName(osubperm.getAction(), FolderShare.ItemsRight.class);
				if (right != null) itemsPerms.set(right);
			}
		}
		if (typeOfData != null) {
			return new FolderSharing.SubjectConfiguration(subjectSid, folderPerms, itemsPerms, rawData, typeOfData);
		} else {
			return new FolderSharing.SubjectConfiguration(subjectSid, folderPerms, itemsPerms);
		}
	}
	
	private Set<RolePermissionDAO.ServiceEntry> toSharePermissionBatchInsertEntries(String permissionKey, String instance, Collection<Sharing.SubjectConfiguration> configurations) {
		LinkedHashSet<RolePermissionDAO.ServiceEntry> items = null;
		for (Sharing.SubjectConfiguration configuration : configurations) {
			if (items == null) items = new LinkedHashSet<>(configurations.size());
			
			Set<String> set = configuration.getActions();
			for (String action : set) {
				items.add(new RolePermissionDAO.ServiceEntry(configuration.getSubjectSid(), permissionKey, action, instance));
			}
		}
		return items;
	}
	
	private Set<RolePermissionDAO.ServiceEntry> toSharePermissionBatchInsertEntries(String shareContext, String shareInstance, Collection<FolderSharing.SubjectConfiguration> configurations, boolean skipOriginOnlyPerms) {
		final String FOLDER_PERMISSION_KEY = FolderShare.buildFolderPermissionKey(shareContext);
		final String ITEMS_PERMISSION_KEY = FolderShare.buildItemsPermissionKey(shareContext);
		
		LinkedHashSet<RolePermissionDAO.ServiceEntry> items = null;
		for (FolderSharing.SubjectConfiguration configuration : configurations) {
			if (items == null) items = new LinkedHashSet<>(configurations.size());
			for (String right : FolderShare.toRightNames(configuration.getFolderPermissions())) {
				if (!skipOriginOnlyPerms || !FolderShare.FolderRight.isReservedForWildcard(right)) {
					items.add(new RolePermissionDAO.ServiceEntry(configuration.getSubjectSid(), FOLDER_PERMISSION_KEY, right, shareInstance));
				}
			}
			for (String right : FolderShare.toRightNames(configuration.getItemsPermissions())) {
				items.add(new RolePermissionDAO.ServiceEntry(configuration.getSubjectSid(), ITEMS_PERMISSION_KEY, right, shareInstance));
			}
		}
		return items;
	}
	
	private List<String> buildFolderShareKeys(String context) {
		return Arrays.asList(
			FolderShare.buildFolderPermissionKey(context),
			FolderShare.buildItemsPermissionKey(context)
		);
	}
	
	/**
	 * @deprecated use buildFolderShareKeys instead when OLD sharing was completely removed!
	 */
	@Deprecated
	private List<String> buildFolderShareKeysALL(String context) {
		return Arrays.asList(
			FolderShare.buildFolderPermissionKey(context),
			FolderShare.buildItemsPermissionKey(context),
			"%@SHARE_ROOT",
			"%@SHARE_ELEMENTS",
			"%@SHARE_FOLDER"
		);
	}
	
	private static boolean isDirectoryNameNotFoundException(final DirectoryException ex) {
		return LangUtils.hasFollowingCauseChain(ex, org.ldaptive.LdapException.class, javax.naming.NameNotFoundException.class);
	}
	
	private PermissionString toPermissionString(ORolePermission orperm) {
		return PermissionString.build(orperm.getServiceId(), orperm.getContext(), orperm.getAction(), orperm.getInstance());
	}
	
	private RolePermissionDAO.SubjectEntry toSubjectEntry(ORolePermission orperm) {
		return new RolePermissionDAO.SubjectEntry(orperm.getServiceId(), orperm.getContext(), orperm.getAction(), orperm.getInstance());
	}
	
	private Set<RolePermissionDAO.SubjectEntry> toPermissionBatchInsertEntries(Set<PermissionString> permissions) {
		LinkedHashSet<RolePermissionDAO.SubjectEntry> items = new LinkedHashSet<>(permissions.size());
		for (PermissionString permission : permissions) {
			if (permission == null) continue;
			items.add(new RolePermissionDAO.SubjectEntry(permission.getServiceId(), permission.getContext(), permission.getAction(), permission.getInstance()));
		}
		return items;
	}
	
	private Set<RolePermissionDAO.SubjectEntry> toServicePermissionBatchInsertEntries(Set<String> serviceIds) {
		LinkedHashSet<RolePermissionDAO.SubjectEntry> items = new LinkedHashSet<>(serviceIds.size());
		for (String serviceId : serviceIds) {
			if (serviceId == null) continue;
			final ServicePermissionString sps = ServicePermissionString.build(serviceId);
			items.add(new RolePermissionDAO.SubjectEntry(sps.getServiceId(), sps.getContext(), sps.getAction(), sps.getInstance()));
		}
		return items;
	}
	
	private UserProfile.PersonalInfo doGetProfilePersonalInfo(UserProfileId profileId) throws WTException {
		UserInfoDAO dao = UserInfoDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			OUserInfo oui = dao.selectByDomainUser(con, profileId.getDomainId(), profileId.getUserId());
			return (oui == null) ? null : new UserProfile.PersonalInfo(oui);
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private UserProfile.Data doGetProfileData(final UserProfileId profileId) throws WTException {
		UserDAO useDao = UserDAO.getInstance();
		ResourceDAO resDao = ResourceDAO.getInstance();
		Connection con = null;
		
		VUserData vuser = null;
		try {
			con = wta.getConnectionManager().getConnection();
			vuser = useDao.viewDataByProfile(con, profileId.getDomainId(), profileId.getUserId());
			if (vuser == null) vuser = resDao.viewDataByProfile(con, profileId.getDomainId(), profileId.getUserId());
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		if (vuser == null) return null;
		
		String authDomainName = WT.getAuthDomainName(profileId.getDomainId());
		CoreUserSettings cus = new CoreUserSettings(profileId);
		UserProfile.PersonalInfo pinfo = lookupProfilePersonalInfo(profileId, true);
		
		DomainAccount internetAccount = new DomainAccount(authDomainName, profileId.getUserId());
		InternetAddress profileIa = InternetAddressUtils.toInternetAddress(profileId.getUserId(), authDomainName, vuser.getDisplayName());
		InternetAddress personalIa = InternetAddressUtils.toInternetAddress(pinfo.getEmail(), vuser.getDisplayName());
		if (personalIa == null) {
			personalIa = profileIa;
			LOGGER.warn("User does not have a valid email in personal info. Check it! [{}]", profileId.toString());
		}
		return new UserProfile.Data(internetAccount, vuser.getDisplayName(), cus.getLanguageTag(), cus.getTimezone(), 
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
	
	private EventBus.PostResult fireEvent(EventBase event) {
		return fireEvent(event, false, false);
	}
	
	private EventBus.PostResult fireEvent(EventBase event, boolean trackHandlerErrors, boolean logTrackedErrors) {
		EventBus.PostResult result = wta.getEventBus().postNow(event, trackHandlerErrors);
		if (logTrackedErrors) logEventBusHandlerErrors(result);
		return result;
	}
	
	private void logEventBusHandlerErrors(EventBus.PostResult result) {
		if (result.hasHandlerErrors()) {
			for (EventBus.HandlerError error : result.getHandlerErrors()) {
				Class clazz = error.getHandlerMethodDeclaringClass();
				String serviceId = WT.findServiceId(clazz);
				LOGGER.error("[{}] {} -> {}", serviceId, ClassUtils.getSimpleClassName(clazz), error.getHandlerMethodName(), error.getDeepestCause());
			}
		}	
	}
	
	private String decDirPassword(String ep) {
		if (ep == null) return null;
		return PasswordUtils.decryptDES(ep, new String(new char[]{'p','a','s','s','w','o','r','d'}));
	}
	
	private String encDirPassword(String dp) {
		if (dp == null) return null;
		return PasswordUtils.encryptDES(dp, new String(new char[]{'p','a','s','s','w','o','r','d'}));
	}
	
	private void setDirPassword(ODomain o, String dp) {
		o.setDirPassword(encDirPassword(dp));
	}
	
	private void encryptDomainDirPassword(DomainBase domain) {
		final String scheme = domain.getDirScheme();
		if (LdapWebTopDirectory.SCHEME.equals(scheme)
				|| LdapDirectory.SCHEME.equals(scheme)
				|| ADDirectory.SCHEME.equals(scheme)
				|| LdapNethDirectory.SCHEME.equals(scheme)) {
			
			domain.setDirPassword(encDirPassword(domain.getDirPassword()));
		}
	}
	
	private void decryptDomainDirPassword(DomainBase domain) {
		final String scheme = domain.getDirScheme();
		if (LdapWebTopDirectory.SCHEME.equals(scheme)
				|| LdapDirectory.SCHEME.equals(scheme)
				|| ADDirectory.SCHEME.equals(scheme)
				|| LdapNethDirectory.SCHEME.equals(scheme)) {
			
			domain.setDirPassword(decDirPassword(domain.getDirPassword()));
		}
	}
	
	private boolean ensureProfileDomainQuietly(String domainId) throws AuthException {
		try {
			ensureProfileDomain(domainId);
			return true;
		} catch (AuthException ex) {
			return false;
		}
	}
	
	private void ensureProfileDomain(String domainId) throws AuthException {
		if (domainId == null) return;
		UserProfileId runPid = RunContext.getRunProfileId();
		if (RunContext.isWebTopAdmin(runPid)) return;
		if (!runPid.hasDomain(domainId)) throw new AuthException("Running profile's domain [{0}] does not match with passed one [{1}]", runPid.getDomainId(), domainId);
	}
	
	private class CacheDomainInfo extends AbstractBulkCache {
		
		private class Data {
			public final String domainId;
			public final String publicId;
			public final String authDomainName;
			public final String domainName;
			public final String publicFqdn;
			
			public Data(String domainId, String publicId, String authDomainName, String domainName, String publicFqdn) {
				this.domainId = domainId;
				this.publicId = publicId;
				this.authDomainName = authDomainName;
				this.domainName = domainName;
				this.publicFqdn = publicFqdn;
			}
		}
		
		private HashMap<String, Data> byDomainId = new HashMap<>();
		private HashMap<String, Data> byPublicId = new HashMap<>();
		private HashMap<String, Data> byAuthDomainName = new HashMap<>(); // InternetName -> AuthDomainName
		private HashMap<String, Data> byDomainName = new HashMap<>(); // InternetName -> DomainName
		private HashMap<String, Data> byPublicFqdn = new HashMap<>(); // PublicInternetName -> publicFQDN
		
		@Override
		protected void internalBuildCache() {
			DomainDAO domDao = DomainDAO.getInstance();
			Connection con = null;
			
			try {
				LOGGER.debug("[DomainInfoCache] Building cache...");
				HashMap<String, Data> _byDomainId = new HashMap<>();
				HashMap<String, Data> _byPublicId = new HashMap<>();
				HashMap<String, Data> _byAuthDomainName = new HashMap<>();
				HashMap<String, Data> _byDomainName = new HashMap<>();
				HashMap<String, Data> _byPublicFqdn = new HashMap<>();
				
				con = wta.getConnectionManager().getConnection();
				for (ODomain odomain : domDao.selectInfo(con)) {
					final Boolean enabled = odomain.getEnabled();
					final String publicInternetName;
					CoreServiceSettings dss = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, odomain.getDomainId());
					URI publicUri = URIUtils.createURIQuietly(dss.getPublicBaseUrl());
					if (publicUri != null && !StringUtils.isBlank(publicUri.getHost())) {
						publicInternetName = publicUri.getHost();
					} else {
						publicInternetName = null;
					}
					
					final Data data = new Data(
						odomain.getDomainId(),
						domainIdToDomainPublicId(odomain.getDomainId()),
						odomain.getAuthDomainName(),
						odomain.getDomainName(),
						publicInternetName
					);
					
					LOGGER.debug("[DomainInfoCache] Working on '{}'", data.domainId);
					_byDomainId.put(data.domainId, data);
					// Public name (scrambled id)
					_byPublicId.put(data.publicId, data);
					LOGGER.trace("[DomainInfoCache] {} -> {}", data.publicId, data.domainId);
					// Authentication DomainName
					if (enabled) {
						_byAuthDomainName.put(data.authDomainName, data);
						LOGGER.trace("[DomainInfoCache] {} -> {}", data.authDomainName, data.domainId);
					}
					// Default DomainName
					if (enabled) {
						_byDomainName.put(data.domainName, data);
						LOGGER.trace("[DomainInfoCache] {} -> {}", data.domainName, data.domainId);
					}
					// Public FQDN (host.domain.tld)
					if (enabled) {
						Data oldData = _byPublicFqdn.put(data.publicFqdn, data);
						LOGGER.trace("[DomainInfoCache] {} -> {}", data.publicFqdn, data.domainId);
						if (oldData != null) {
							LOGGER.warn("[DomainInfoCache] publicFQDN -> domainId : duplicated association for {}", data.publicFqdn);
							LOGGER.warn("[DomainInfoCache] {} overrides {}!", data.domainId, oldData.domainId);
						}
					}
				}
				this.byDomainId = _byDomainId;
				this.byPublicId = _byPublicId;
				this.byAuthDomainName = _byAuthDomainName;
				this.byDomainName = _byDomainName;
				this.byPublicFqdn = _byPublicFqdn;
				LOGGER.debug("[DomainInfoCache] Cached {} domains", byDomainId.size());
				
			} catch (Exception ex) {
				DbUtils.closeQuietly(con);
				if (this.getInitCount() == 0) {
					throw new WTRuntimeException(ex, "[DomainInfoCache] Unable to build cache");
				} else {
					LOGGER.error("[DomainInfoCache] Unable to build cache", ex);
				}
			}
		}
		
		@Override
		protected void internalCleanupCache() {
			LOGGER.debug("[DomainInfoCache] Cleaning-up cache...");
			this.byDomainId = new HashMap<>();
			this.byPublicId = new HashMap<>();
			this.byAuthDomainName = new HashMap<>();
			this.byDomainName = new HashMap<>();
			this.byPublicFqdn = new HashMap<>();
		}
		
		public int getSize() {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return byDomainId.size();
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public boolean exists(final String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.byDomainId.containsKey(domainId);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String publicIdToDomainId(final String publicId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				final Data data = this.byPublicId.get(publicId);
				return data != null ? data.domainId : null;
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainIdToAuthDomainName(final String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				final Data data = this.byDomainId.get(domainId);
				return data != null ? data.authDomainName : null;
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String authDomainNameToDomainIdIfOnlyOne(final String authDomainName) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				if (this.byAuthDomainName.size() == 1) {
					Map.Entry<String, Data> entry = this.byAuthDomainName.entrySet().iterator().next();
					return entry.getValue().domainId;
				} else {
					return null;
				}
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String authDomainNameToDomainId(final String authDomainName) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				final Data data = this.byAuthDomainName.get(authDomainName);
				return data != null ? data.domainId : null;
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainIdToDomainName(final String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				final Data data = this.byDomainId.get(domainId);
				return data != null ? data.domainName : null;
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainNameToDomainId(final String domainName) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				final Data data = this.byDomainName.get(domainName);
				return data != null ? data.domainId : null;
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String publicFqdnToDomainId(String publicFqdn) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				final Data data = this.byPublicFqdn.get(publicFqdn);
				return data != null ? data.domainId : null;
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainIdToPublicFqdn(String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				final Data data = this.byDomainId.get(domainId);
				return data != null ? data.publicFqdn : null;
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		/*
		private HashMap<String, String> publicNameToDomainId = new HashMap<>();
		private HashMap<String, String> authDomainNameToDomainId = new HashMap<>();
		private HashMap<String, String> domainIdToAuthDomainName = new HashMap<>();
		private HashMap<String, String> domainNameToDomainId = new HashMap<>();
		private HashMap<String, String> domainIdToDomainName = new HashMap<>();
		private HashMap<String, String> publicInternetNameToDomainId = new HashMap<>();
		private HashMap<String, String> domainIdToPublicInternetName = new HashMap<>();
		
		@Override
		protected void internalBuildCache() {
			DomainDAO domDao = DomainDAO.getInstance();
			Connection con = null;
			
			try {
				LOGGER.debug("[DomainInfoCache] Building cache...");
				con = wta.getConnectionManager().getConnection();
				HashMap<String, String> hmPublicNameToDomainId = new HashMap<>();
				HashMap<String, String> hmDomainIdToAuthDomainName = new HashMap<>();
				HashMap<String, String> hmAuthDomainNameToDomainId = new HashMap<>();
				
				HashMap<String, String> hmDomainIdToDomainName = new HashMap<>();
				HashMap<String, String> hmDomainNameToDomainId = new HashMap<>();
				
				HashMap<String, String> hmDomainIdToPublicInternetName = new HashMap<>();
				HashMap<String, String> hmPublicInternetNameToDomainId = new HashMap<>();
				
				for (ODomain odomain : domDao.selectInfo(con)) {
					final String domainId = odomain.getDomainId();
					final Boolean enabled = odomain.getEnabled();
					final String authDomainName = odomain.getAuthDomainName();
					final String domainName = odomain.getDomainName();
					
					LOGGER.debug("[DomainInfoCache] Working on '{}'", domainId);
					
					// Public name (scrambled id)
					String pubName = domainIdToPublicName(domainId);
					hmPublicNameToDomainId.put(pubName, domainId);
					LOGGER.trace("[DomainInfoCache] {} -> {}", pubName, domainId);
					
					// Authentication DomainName
					hmDomainIdToAuthDomainName.put(domainId, authDomainName);
					LOGGER.trace("[DomainInfoCache] {} -> {}", domainId, authDomainName);
					if (enabled) {
						hmAuthDomainNameToDomainId.put(authDomainName, domainId);
						LOGGER.trace("[DomainInfoCache] {} -> {}", authDomainName, domainId);
					}
					
					// Default DomainName (eg. for email addresses)
					hmDomainIdToDomainName.put(domainId, domainName);
					LOGGER.trace("[DomainInfoCache] {} -> {}", domainId, domainName);
					if (enabled) {
						hmDomainNameToDomainId.put(domainName, domainId);
						LOGGER.trace("[DomainInfoCache] {} -> {}", domainName, domainId);
					}
					
					// Public FQDN (host.domain.tld)
					CoreServiceSettings dss = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, domainId);
					URI publicUri = URIUtils.createURIQuietly(dss.getPublicBaseUrl());
					if (publicUri != null && !StringUtils.isBlank(publicUri.getHost())) {
						String pubInternetName = publicUri.getHost();
						hmDomainIdToPublicInternetName.put(domainId, pubInternetName);
						LOGGER.trace("[DomainInfoCache] {} -> {}", domainId, pubInternetName);
						if (enabled) {
							String oldPubInternetName = hmPublicInternetNameToDomainId.put(pubInternetName, domainId);
							LOGGER.trace("[DomainInfoCache] {} -> {}", pubInternetName, domainId);

							if (oldPubInternetName != null) {
								LOGGER.warn("[DomainInfoCache] publicInternetName -> domainId : duplicated association for {}", pubInternetName);
								LOGGER.warn("[DomainInfoCache] [{} -> {}] overridden!", pubInternetName, oldPubInternetName);
							}
						}
					}
				}
				
				//for (Map.Entry<String, String> entry : domDao.selectEnabledInternetNames(con).entrySet()) {
				//	final String domainId = entry.getKey();
				//	final String internetName = entry.getValue();
				//	
				//	LOGGER.debug("[DomainInfoCache] Working on '{}'", domainId);
				//	String pubName = domainIdToPublicName(domainId);
				//	hmPublicNameToDomainId.put(pubName, domainId);
				//	LOGGER.trace("[DomainInfoCache] {} -> {}", pubName, domainId);
				//	
				//	hmDomainInternetNameToDomainId.put(internetName, domainId);
				//	LOGGER.trace("[DomainInfoCache] {} -> {}", internetName, domainId);
				//	
				//	hmDomainIdToDomainInternetName.put(domainId, internetName);
				//	LOGGER.trace("[DomainInfoCache] {} -> {}", domainId, internetName);
				//	
				//	String pubInternetName = entry.getValue();
				//	CoreServiceSettings dss = new CoreServiceSettings(wta.getSettingsManager(), CoreManifest.ID, domainId);
				//	URI publicUri = URIUtils.createURIQuietly(dss.getPublicBaseUrl());
				//	if (publicUri != null && !StringUtils.isBlank(publicUri.getHost())) {
				//		pubInternetName = publicUri.getHost();
				//	}
				//	
				//	String oldPubInternetName = hmPublicInternetNameToDomainId.put(pubInternetName, domainId);
				//	LOGGER.trace("[DomainInfoCache] {} -> {}", pubInternetName, domainId);
				//	
				//	hmDomainIdToPublicInternetName.put(domainId, pubInternetName);
				//	LOGGER.trace("[DomainInfoCache] {} -> {}", domainId, pubInternetName);
				//	
				//	if (oldPubInternetName != null) {
				//		LOGGER.warn("[DomainInfoCache] publicInternetName -> domainId : duplicated association for {}", pubInternetName);
				//		LOGGER.warn("[DomainInfoCache] [{} -> {}] overridden!", pubInternetName, oldPubInternetName);
				//	}
				//}
				this.publicNameToDomainId = hmPublicNameToDomainId;
				this.domainIdToAuthDomainName = hmDomainIdToAuthDomainName;
				this.authDomainNameToDomainId = hmAuthDomainNameToDomainId;
				this.domainIdToPublicInternetName = hmDomainIdToPublicInternetName;
				this.publicInternetNameToDomainId = hmPublicInternetNameToDomainId;
				LOGGER.debug("[DomainInfoCache] Cached {} domains", publicNameToDomainId.size());
				
			} catch (Exception ex) {
				DbUtils.closeQuietly(con);
				if (this.getInitCount() == 0) {
					throw new WTRuntimeException(ex, "[DomainInfoCache] Unable to build cache");
				} else {
					LOGGER.error("[DomainInfoCache] Unable to build cache", ex);
				}
			}
		}
		
		@Override
		protected void internalCleanupCache() {
			LOGGER.debug("[DomainInfoCache] Cleaning-up cache...");
			this.publicNameToDomainId = new HashMap<>();
			this.authDomainNameToDomainId = new HashMap<>();
			this.domainIdToAuthDomainName = new HashMap<>();
			
			this.domainNameToDomainId = new HashMap<>();
			this.domainIdToDomainName = new HashMap<>();
			
			this.publicInternetNameToDomainId = new HashMap<>();
			this.domainIdToPublicInternetName = new HashMap<>();
		}
		
		public int getSize() {
			return publicNameToDomainId.size();
		}
		
		public boolean exists(final String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.domainIdToAuthDomainName.containsKey(domainId);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String publicNameToDomainId(final String publicName) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.publicNameToDomainId.get(publicName);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String domainIdToAuthDomainName(final String domainId) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.domainIdToAuthDomainName.get(domainId);
			} finally {
				this.unlockRead(stamp);
			}
		}
		
		public String authDomainNameToDomainId(final String authFQDN) {
			this.internalCheckBeforeGetDoNotLockThis();
			long stamp = this.readLock();
			try {
				return this.authDomainNameToDomainId.get(authFQDN);
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
		*/
	}
	
	/*
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
	*/
	
	private void cleanupProfileCache() {
		profileToDataCache.cleanUp();
		profileToPersonalInfoCache.cleanUp();
	}
	
	private class ProfileDataCacheLoader implements CacheLoader<UserProfileId, UserProfile.Data> {

		@Override
		public UserProfile.Data load(UserProfileId k) throws Exception {
			try {
				LOGGER.trace("[UserProfileDataCache] Loading... [{}]", k);
				return doGetProfileData(k);
				
			} catch (Exception ex) {
				LOGGER.error("[UserProfileDataCache] Unable to lookup [{}]", k, ex);
				return null;
			}
		}
	}
	
	private class ProfilePersonalInfoCacheLoader implements CacheLoader<UserProfileId, UserProfile.PersonalInfo> {

		@Override
		public UserProfile.PersonalInfo load(UserProfileId k) throws Exception {
			try {
				LOGGER.trace("[UserProfilePersonalInfoCache] Loading... [{}]", k);
				return doGetProfilePersonalInfo(k);
				
			} catch (Exception ex) {
				LOGGER.error("[UserProfilePersonalInfoCache] Unable to lookup [{}]", k, ex);
				return null;
			}
		}
	}
	
	private class SubjectSidCache {
		private final LoadingCache<String, Optional<SubjectSid>> userPidToSid = Caffeine.newBuilder().build(new PidToSidLoader());
		private final LoadingCache<String, Optional<SubjectSid>> rolePidToSid = Caffeine.newBuilder().build(new PidToSidLoader());
		private final LoadingCache<String, Optional<SubjectPid>> sidToPid = Caffeine.newBuilder().build(new SidToPidLoader());
	
		public void init() {
			UserDAO useDao = UserDAO.getInstance();
			RoleDAO rolDao = RoleDAO.getInstance();
			Connection con = null;

			try {
				LOGGER.trace("[Subject Sid <-> Pid Cache] Loading all...");
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				// Load data from users/resocurces/groups
				List<OUser> ousers = useDao.selectAllAsSubjects(con);
				for (OUser ouser : ousers) {
					sidToPid.put(ouser.getUserSid(), Optional.ofNullable(new SubjectPid(ouser)));
					userPidToSid.put(ouser.getProfileId().toString(), Optional.ofNullable(new SubjectSid(ouser)));
				}
				// Load data from roles
				List<ORole> oroles = rolDao.selectAllAsSubjects(con);
				for (ORole orole : oroles) {
					sidToPid.put(orole.getRoleSid(), Optional.ofNullable(new SubjectPid(orole)));
					rolePidToSid.put(orole.getProfileId().toString(), Optional.ofNullable(new SubjectSid(orole)));
				}

			} catch (Throwable t) {
				LOGGER.error("[Subject Sid <-> Pid Cache] Unable to load all", t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
		
		public void cleanup() {
			sidToPid.cleanUp();
			userPidToSid.cleanUp();
			rolePidToSid.cleanUp();
		}
		
		public void cleanupByDomain(final String domainId) {
			Check.notNull(domainId, "domainId");
			
			Set<String> uids = new LinkedHashSet<>();
			Set<String> userPids = new LinkedHashSet<>();
			Set<String> rolePids = new LinkedHashSet<>();
			userPidToSid.asMap().forEach((k, v) -> {
				if (k.equals("@" + domainId)) {
					userPids.add(k);
					if (v.isPresent()) uids.add(v.get().getSid());
				}
			});
			rolePidToSid.asMap().forEach((k, v) -> {
				if (k.equals("@" + domainId)) {
					rolePids.add(k);
					if (v.isPresent()) uids.add(v.get().getSid());
				}
			});
			sidToPid.invalidateAll(uids);
			userPidToSid.invalidateAll(userPids);
			rolePidToSid.invalidateAll(rolePids);
		}
		
		public void add(final UserProfileId pid, final String sid, final GenericSubject.Type type) {
			Check.notNull(pid, "pid");
			Check.notEmpty(sid, "sid");
			Check.notNull(type, "type");
			
			sidToPid.put(sid, Optional.ofNullable(new SubjectPid(type, pid.toString())));
			if (GenericSubject.Type.ROLE.equals(type)) {
				rolePidToSid.put(pid.toString(), Optional.ofNullable(new SubjectSid(type, sid)));
			} else {
				userPidToSid.put(pid.toString(), Optional.ofNullable(new SubjectSid(type, sid)));
			}
		}
		
		public void remove(final UserProfileId pid, final GenericSubject.Type type) {
			Check.notNull(pid, "pid");
			Check.notNull(type, "type");
			
			Optional<SubjectSid> opt = null;
			if (GenericSubject.Type.ROLE.equals(type)) {
				opt = rolePidToSid.get(pid.toString());
			} else {
				opt = userPidToSid.get(pid.toString());
			}
			if ((opt != null) && opt.isPresent()) sidToPid.invalidate(opt.get().getSid());
			if (GenericSubject.Type.ROLE.equals(type)) {
				rolePidToSid.invalidate(pid.toString());
			} else {
				userPidToSid.invalidate(pid.toString());
			}
		}
		
		public GenericSubject.Type getSidType(final String sid) {
			Check.notNull(sid, "sid");
			
			Optional<SubjectPid> opt = sidToPid.get(sid);
			if (!opt.isPresent()) {
				return null;
			} else {
				return opt.get().getType();
			}
		}
		
		public UserProfileId getPid(final String sid, final GenericSubject.Type... validTypes) {
			Check.notNull(sid, "sid");
			
			Optional<SubjectPid> opt = sidToPid.get(sid);
			if (!opt.isPresent()) {
				return null;
			} else {
				SubjectPid sp = opt.get();
				Set<GenericSubject.Type> constraints = toSet(validTypes);
				if (constraints != null) {
					return constraints.contains(sp.getType()) ? sp.getProfileId() : null;
				} else {
					return sp.getProfileId();
				}
			}
		}
		
		public String getPidAsString(final String sid, final GenericSubject.Type... validTypes) {
			UserProfileId pid = getPid(sid, validTypes);
			return (pid != null) ? pid.toString() : null;
		}
		
		public String getSid(final UserProfileId pid, final GenericSubject.Type... validTypes) {
			Check.notNull(pid, "pid");
			Check.notEmpty(validTypes, "validTypes");
			
			SubjectSid ss = null;
			Set<GenericSubject.Type> constraints = toSet( validTypes);
			if (constraints.size() > 1 || !constraints.contains(GenericSubject.Type.ROLE)) {
				Optional<SubjectSid> opt = userPidToSid.get(pid.toString());
				if ((opt != null) && opt.isPresent()) ss = opt.get();
			}
			if ((ss == null) && constraints.contains(GenericSubject.Type.ROLE)) {
				Optional<SubjectSid> opt = rolePidToSid.get(pid.toString());
				if ((opt != null) && opt.isPresent()) ss = opt.get();
			}
			
			if (ss == null) {
				return null;
			} else {
				return constraints.contains(ss.getType()) ? ss.getSid() : null;
			}
		}
		
		private Set<GenericSubject.Type> toSet(GenericSubject.Type... types) {
			return (types != null) ? new LinkedHashSet<>(Arrays.asList(types)) : new LinkedHashSet<>(0);
		}
		
		private Set<GenericSubject.Type> toTypeSet(GenericSubject.Type type, GenericSubject.Type... moreTypes) {
			Set<GenericSubject.Type> set = null;
			if (type != null) {
				set = new LinkedHashSet<>();
				set.add(type);
				if (moreTypes != null) {
					for (GenericSubject.Type more : moreTypes) set.add(more);
				}
			}
			return set;
		}
		
		private class PidToSidLoader implements CacheLoader<String, Optional<SubjectSid>> {
			
			@Override
			public Optional<SubjectSid> load(String k) throws Exception {
				UserDAO useDao = UserDAO.getInstance();
				RoleDAO rolDao = RoleDAO.getInstance();
				Connection con = null;

				try {
					LOGGER.trace("[SubjectPidToSidCache] Loading... [{}]", k);
					SubjectSid value = null;

					UserProfileId pid = new UserProfileId(k);
					con = wta.getConnectionManager().getConnection(CoreManifest.ID);
					// Load data from users/resocurces/groups
					OUser ouser = useDao.selectAsSubjectByDomainId(con, pid.getDomainId(), pid.getUserId());
					if (ouser != null) value = new SubjectSid(ouser);
					// Load data from roles
					ORole orole = rolDao.selectAsSubjectByDomainName(con, pid.getDomainId(), pid.getUserId());
					if (orole != null) value = new SubjectSid(orole);

					return Optional.ofNullable(value);

				} catch (Throwable t) {
					LOGGER.error("[SubjectPidToSidCache] Unable to load [{}]", k, t);
					return null;
				} finally {
					DbUtils.closeQuietly(con);
				}
			}
		}

		private class SidToPidLoader implements CacheLoader<String, Optional<SubjectPid>> {

			@Override
			public Optional<SubjectPid> load(String k) throws Exception {
				UserDAO useDao = UserDAO.getInstance();
				RoleDAO rolDao = RoleDAO.getInstance();
				Connection con = null;

				try {
					LOGGER.trace("[SubjectSidToPidCache] Loading... [{}]", k);
					SubjectPid value = null;

					con = wta.getConnectionManager().getConnection(CoreManifest.ID);
					// Load data from users/resocurces/groups
					OUser ouser = useDao.selectAsSubjectBySidEnabled(con, k, EnabledCond.ANY_STATE);
					if (ouser != null) value = new SubjectPid(ouser);
					// Load data from roles
					ORole orole = rolDao.selectAsSubjectBySid(con, k);
					if (orole != null) value = new SubjectPid(orole);

					return Optional.ofNullable(value);

				} catch (Exception ex) {
					LOGGER.error("[SubjectSidToPidCache] Unable to load [{}]", k, ex);
					return null;
				} finally {
					DbUtils.closeQuietly(con);
				}
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
	
	public static class EntityPermissions {
		public ArrayList<ORolePermission> others;
		public ArrayList<ORolePermission> services;
		
		public EntityPermissions(ArrayList<ORolePermission> others, ArrayList<ORolePermission> services) {
			this.others = others;
			this.services = services;
		}
	}
}
