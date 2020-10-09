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
package com.sonicle.webtop.core.admin;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.LicenseManager;
import com.sonicle.webtop.core.app.LogbackPropertyDefiner;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.OLicense;
import com.sonicle.webtop.core.config.bol.OPecBridgeFetcher;
import com.sonicle.webtop.core.config.bol.OPecBridgeRelay;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUpgradeStatement;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.model.DirectoryUser;
import com.sonicle.webtop.core.bol.model.DomainEntity;
import com.sonicle.webtop.core.bol.model.DomainSetting;
import com.sonicle.webtop.core.bol.model.GroupEntity;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleEntity;
import com.sonicle.webtop.core.bol.model.SessionInfo;
import com.sonicle.webtop.core.bol.model.SystemSetting;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.config.dal.PecBridgeFetcherDAO;
import com.sonicle.webtop.core.config.dal.PecBridgeRelayDAO;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.dal.UpgradeStatementDAO;
import com.sonicle.webtop.core.model.License;
import com.sonicle.webtop.core.model.LoggerEntry;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.model.PublicImage;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.vfs.IVfsManager;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreAdminManager extends BaseManager {
	private static final Logger logger = WT.getLogger(CoreAdminManager.class);
	private WebTopApp wta = null;
	
	public CoreAdminManager(WebTopApp wta, boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		this.wta = wta;
	}
	
	public boolean isOnlineSession(String sessionId) {
		return wta.getSessionManager().isOnline(sessionId);
	}
	
	public OSettingDb getSettingInfo(String serviceId, String key) {
		SettingsManager setm = wta.getSettingsManager();
		return setm.getSettingInfo(serviceId, key);
	}
	
	/**
	 * Lists all system settings.
	 * @param includeHidden True to also return settings marked as hidden.
	 * @return The settings list.
	 */
	public List<SystemSetting> listSystemSettings(boolean includeHidden) {
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setm = wta.getSettingsManager();
		return setm.listSettings(includeHidden);
	}
	
	/**
	 * Updates (or inserts) a system setting for a specific service.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	public boolean updateSystemSetting(String serviceId, String key, Object value) {
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setm = wta.getSettingsManager();
		return setm.setServiceSetting(serviceId, key, value);
	}
	
	/**
	 * Clears a system setting.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteSystemSetting(String serviceId, String key) {
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setm = wta.getSettingsManager();
		return setm.deleteServiceSetting(serviceId, key);
	}
	
	public DomainEntity getDomain(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.getDomainEntity(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot get domain [{0}]", domainId);
		}
	}
	
	public void addDomain(DomainEntity domain) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.addDomain(domain);
			IVfsManager vfs = (IVfsManager)WT.getServiceManager("com.sonicle.webtop.vfs");
			if (vfs != null) {
				vfs.addBuiltInStoreDomainImages(domain.getDomainId());
			}
			
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot add domain");
		}
	}
	
	public void initDomainWithDefaults(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.initDomainWithDefaults(domainId);
			wtmgr.initDomainHomeFolder(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot init domain");
		}
	}
	
	public void updateDomain(DomainEntity domain) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.updateDomain(domain);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot update domain [{0}]", domain.getDomainId());
		}
	}
	
	public void deleteDomain(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.deleteDomain(domainId);
			IVfsManager vfs = (IVfsManager)WT.getServiceManager("com.sonicle.webtop.vfs");
			if (vfs != null) {
				vfs.deleteBuiltInStoreDomainImages(domainId);
			}
			
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot delete domain [{0}]", domainId);
		}
	}
	
	/**
	 * Lists all settings for a specific platform domain.
	 * @param domainId The domain ID.
	 * @param includeHidden True to also return settings marked as hidden.
	 * @return The settings list.
	 */
	public List<DomainSetting> listDomainSettings(String domainId, boolean includeHidden) {
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		SettingsManager setm = wta.getSettingsManager();
		return setm.listSettings(domainId, includeHidden);
	}
	
	/**
	 * Updates (or inserts) a domain setting for a specific service.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	public boolean updateDomainSetting(String domainId, String serviceId, String key, Object value) {
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		SettingsManager setm = wta.getSettingsManager();
		return setm.setServiceSetting(domainId, serviceId, key, value);
	}
	
	/**
	 * Clears a domain setting.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteDomainSetting(String domainId, String serviceId, String key) {
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		SettingsManager setm = wta.getSettingsManager();
		return setm.deleteServiceSetting(domainId, serviceId, key);
	}
	
	public List<PublicImage> listDomainPublicImages(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.listDomainPublicImages(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list domain's public images [{0}]", domainId);
		}
	}
	
	public List<OGroup> listGroups(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.listGroups(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups [{0}]", domainId);
		}
	}
	
	public GroupEntity getGroup(UserProfileId pid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.getGroupEntity(pid);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to get group [{0}]", pid.toString());
		}
	}
	
	public void addGroup(GroupEntity group) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.addGroup(group);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to add group [{0}]", group.getProfileId().toString());
		}
	}
	
	public void updateGroup(GroupEntity group) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.updateGroup(group);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to update group [{0}]", group.getProfileId().toString());
		}
	}
	
	public void deleteGroup(UserProfileId pid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.deleteGroup(pid);
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to delete group [{0}]", pid.toString());
		}
	}
	
	public void refreshDomainCache() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		wtMgr.initDomainCache();
	}
	
	public void cleanupLicenseCache() throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.cleanupLicenseCache();
	}
	
	public void checkOnlineAvailability() throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.checkOnlineAvailability();
	}
	
	public List<ServiceLicense> listLicenses(String domainId) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		return licMgr.listLicenses(domainId);
	}
	
	public ServiceLicense getLicense(String domainId, ProductId productId) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		return licMgr.getLicense(domainId, productId);
	}
	
	public void addLicense(License license, boolean autoActivate) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.addLicense(license, autoActivate);
	}
	
	public void changeLicense(String domainId, ProductId productId, String newString, String activatedString) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.changeLicense(domainId, productId, newString, activatedString, false);
	}
	
	public void modifyLicense(String domainId, ProductId productId, String modificationKey, String modifiedString) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.modifyLicense(domainId, productId, modificationKey, modifiedString);
	}
	
	public void updateLicenseAutoLease(String domainId, ProductId productId, boolean autoLease) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.updateLicenseAutoLease(domainId, productId, autoLease);
	}
	
	public void deleteLicense(String domainId, ProductId productId) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.deleteLicense(domainId, productId, false);
	}
	
	public void activateLicense(String domainId, ProductId productId, String activatedString) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.activateLicense(domainId, productId, activatedString);
	}
	
	public void deactivateLicense(String domainId, ProductId productId, boolean offline) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.deactivateLicense(domainId, productId, offline);
	}
	
	public void assignLicenseLease(String domainId, ProductId productId, Collection<String> userIds) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.assignLicenseLease(domainId, productId, userIds);
	}
	
	/*
	public void autoAssignLicenseLease(String domainId, ProductId productId, String userId) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.assignLicenseLease(domainId, productId, userId, null);
	}
	*/
	
	public void revokeLicenseLease(String domainId, ProductId productId, Collection<String> userIds) throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		licMgr.revokeLicenseLease(domainId, productId, userIds);
	}
	
	public List<DirectoryUser> listDirectoryUsers(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			ODomain domain = wtmgr.getDomain(domainId);
			List<DirectoryUser> items = wtmgr.listDirectoryUsers(domain);
			Collections.sort(items, (DirectoryUser o1, DirectoryUser o2) -> o1.getDirUser().userId.compareTo(o2.getDirUser().userId));
			return items;
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list directory users [{0}]", domainId);
		}
	}
	
	public List<OUser> listUsers(String domainId, boolean enabledOnly) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.listUsers(domainId, enabledOnly);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list users [{0}]", domainId);
		}
	}
	
	public UserEntity getUser(UserProfileId pid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.getUserEntity(pid);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to get user [{0}]", pid.toString());
		}
	}
	
	/*
	public OUser getUser(UserProfileId pid) throws WTException {
		UserManager wtmgr = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.getUser(pid);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to get user [{0}]", pid.toString());
		}
	}
	*/
	
	public void addUser(UserEntity user, boolean updatePassord, char[] password) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.addUser(true, user, updatePassord, password);
		} catch(WTException ex) {
			throw new WTException(ex, "Unable to add user [{0}]", user.getProfileId().toString());
		}
	}
	
	public void updateUser(UserEntity user) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.updateUser(user);
			wtmgr.cleanUserProfileCache(user.getProfileId());
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to update user [{0}]", user.getProfileId().toString());
		}
	}
	
	public boolean updateUser(UserProfileId pid, boolean enabled) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.updateUser(pid, enabled);
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to update user [{0}]", pid.toString());
		}
	}
	
	public void updateUserPassword(UserProfileId pid, char[] newPassword) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.updateUserPassword(pid, null, newPassword);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to change user password [{0}]", pid.toString());
		}
	}
	
	public void updateUserEmailDomain(List<UserProfileId> pids, String newEmailDomain) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.updateUserEmailDomain(pids, newEmailDomain);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to change email domain [{}]", newEmailDomain);
		}
	}
	
	public void deleteUser(boolean deep, UserProfileId pid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.deleteUser(pid, deep);
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to delete user [{0}]", pid.toString());
		}
	}
	
	/**
	 * Lists domain real roles (those defined as indipendent role).
	 * @param domainId The domain ID.
	 * @return The role list.
	 * @throws WTException If something goes wrong.
	 */
	public List<Role> listRoles(String domainId) throws WTException {
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
	 * @param domainId The domain ID.
	 * @return The role list.
	 * @throws WTException If something goes wrong.
	 */
	public List<Role> listUsersRoles(String domainId) throws WTException {
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
	 * @param domainId The domain ID.
	 * @return The role list.
	 * @throws WTException If something goes wrong.
	 */
	public List<Role> listGroupsRoles(String domainId) throws WTException {
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		WebTopManager wtmgr = wta.getWebTopManager();
		try {
			return wtmgr.listGroupsRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups roles [{0}]", domainId);
		}
	}
	
	public RoleEntity getRole(String uid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		String domainId = wtmgr.getRoleDomain(uid);
		if(domainId == null) throw new WTException("Role not found [{0}]", uid);
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			return wtmgr.getRole(uid);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot get role [{0}]", uid);
		}
	}
	
	public void addRole(RoleEntity role) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.addRole(role);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot add role");
		}
	}
	
	public void updateRole(RoleEntity role) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.updateRole(role);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot update role [{0}]", role.getRoleUid());
		}
	}
	
	public void deleteRole(String uid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		String domainId = wtmgr.getRoleDomain(uid);
		if(domainId == null) throw new WTException("Role not found [{0}]", uid);
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		
		try {
			wtmgr.deleteRole(uid);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot delete role [{0}]", uid);
		}
	}
	
	/**
	 * Lists configured PecBridge fetchers for the specified domain.
	 * @param domainId The domain ID.
	 * @return The fetcher list.
	 * @throws WTException If something goes wrong.
	 */
	public List<OPecBridgeFetcher> listPecBridgeFetchers(String domainId) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		try {
			String internetName = WT.getDomainInternetName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.selectByContext(con, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OPecBridgeFetcher getPecBridgeFetcher(int fetcherId) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OPecBridgeFetcher fetcher = dao.select(con, fetcherId);
			if (fetcher != null) {
				UserProfileId pid = new UserProfileId(fetcher.getWebtopProfileId());
				//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
				ensureUserDomain(pid.getDomainId());
			}
			return fetcher;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addPecBridgeFetcher(OPecBridgeFetcher fetcher) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		UserProfileId pid = new UserProfileId(fetcher.getWebtopProfileId());
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(pid.getDomainId());
		
		try {
			String internetName = WT.getDomainInternetName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data ud = WT.getUserData(pid);
			if (ud == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			con = WT.getConnection(SERVICE_ID, false);
			fetcher.setContext(internetName);
			fetcher.setForwardAddress(ud.getProfileEmailAddress());
			fetcher.setFetcherId(dao.getSequence(con).intValue());
			dao.insert(con, fetcher);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updatePecBridgeFetcher(OPecBridgeFetcher fetcher) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		UserProfileId pid = new UserProfileId(fetcher.getWebtopProfileId());
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(pid.getDomainId());
		
		try {
			String internetName = WT.getDomainInternetName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data ud = WT.getUserData(pid);
			if (ud == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			con = WT.getConnection(SERVICE_ID, false);
			fetcher.setForwardAddress(ud.getProfileEmailAddress());
			dao.update(con, fetcher);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deletePecBridgeFetcher(String domainId, int fetcherId) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		try {
			String internetName = WT.getDomainInternetName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.deleteByIdContext(con, fetcherId, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Lists configured PecBridge relays for the specified domain.
	 * @param domainId The domain ID.
	 * @return The relay list.
	 * @throws WTException If something goes wrong.
	 */
	public List<OPecBridgeRelay> listPecBridgeRelays(String domainId) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		try {
			String internetName = WT.getDomainInternetName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.selectByContext(con, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OPecBridgeRelay getPecBridgeRelay(int relayId) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OPecBridgeRelay relay = dao.select(con, relayId);
			if (relay != null) {
				UserProfileId pid = new UserProfileId(relay.getWebtopProfileId());
				//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
				ensureUserDomain(pid.getDomainId());
			}
			return relay;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addPecBridgeRelay(OPecBridgeRelay relay) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		UserProfileId pid = new UserProfileId(relay.getWebtopProfileId());
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(pid.getDomainId());
		
		try {
			String internetName = WT.getDomainInternetName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data ud = WT.getUserData(pid);
			if (ud == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			//TODO: aggiornare email del profilo?
			//int ret = WebTopDb.updateUserEmail(con, domainId, tokens[0], matcher);
			//if(ret != 1) throw new Exception("User's email not updated");
			
			con = WT.getConnection(SERVICE_ID, false);
			relay.setContext(internetName);
			relay.setRelayId(dao.getSequence(con).intValue());
			relay.setDebug(false);
			dao.insert(con, relay);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updatePecBridgeRelay(OPecBridgeRelay relay) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		UserProfileId pid = new UserProfileId(relay.getWebtopProfileId());
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(pid.getDomainId());
		
		try {
			String internetName = WT.getDomainInternetName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data ud = WT.getUserData(pid);
			if (ud == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			//TODO: aggiornare email del profilo?
			//int ret = WebTopDb.updateUserEmail(con, domainId, tokens[0], matcher);
			//if(ret != 1) throw new Exception("User's email not updated");
			
			con = WT.getConnection(SERVICE_ID, false);
			dao.update(con, relay);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deletePecBridgeRelay(String domainId, int relayId) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		try {
			String internetName = WT.getDomainInternetName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.deleteByIdContext(con, relayId, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, LoggerEntry> listLoggers() throws WTException {
		RunContext.ensureIsSysAdmin();
		
		String etcPath = wta.getEtcPath();
		File overrideFile = new File(etcPath, LogbackPropertyDefiner.OVERRIDE_FILENAME);
		
		try {
			LinkedHashMap<String, LoggerEntry> items = new LinkedHashMap<>();
			
			Map<String, ch.qos.logback.classic.Logger> effectiveLoggers = LogbackHelper.getLoggers(true);
			for (ch.qos.logback.classic.Logger effLogger : effectiveLoggers.values()) {
				final LoggerEntry le = LogbackHelper.asLoggerEntry(effLogger);
				items.put(le.getName(), le);
			}
			
			Map<String, LogbackHelper.LoggerNode> includedLoggers = overrideFile.exists() ? LogbackHelper.readIncludedLoggers(overrideFile) : new LinkedHashMap<>();
			for (LogbackHelper.LoggerNode inclLogger : includedLoggers.values()) {
				if (items.containsKey(inclLogger.name)) {
					items.get(inclLogger.name).setOverrideLevel(inclLogger.level);
				} else {
					items.put(inclLogger.name, new LoggerEntry(inclLogger.name, null, inclLogger.level));
				}
			}
			
			LinkedHashMap<String, LoggerEntry> sortedItems = items.entrySet().stream()
					.sorted((o1, o2) -> {
						return StringUtils.equalsIgnoreCase(o1.getKey(), "ROOT") ? 1 : o1.getKey().compareTo(o2.getKey());
					})
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x,y) -> {throw new AssertionError();}, LinkedHashMap::new));
			
			return sortedItems;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	public LoggerEntry updateLogger(String name, LoggerEntry.Level level) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		if ("ROOT".equals(name)) throw new WTException("Root logger configuration cannot be modified");
		
		String etcPath = wta.getEtcPath();
		if (etcPath == null) throw new WTException("Configuration directory ({}) not defined", WebTopProps.PROP_ETC_DIR);
		File overrideFile = new File(etcPath, LogbackPropertyDefiner.OVERRIDE_FILENAME);
		
		try {
			LoggerEntry ret = null;
			boolean fileExists = overrideFile.exists();
			Map<String, ch.qos.logback.classic.Logger> effectiveLoggers = LogbackHelper.getLoggers(true);
			Map<String, LogbackHelper.LoggerNode> includedLoggers = fileExists ? LogbackHelper.readIncludedLoggers(overrideFile) : new LinkedHashMap<>();
			
			if (level == null) { // Null level means *remove* logger
				includedLoggers.remove(name);
				
			} else {
				includedLoggers.put(name, new LogbackHelper.LoggerNode(name, level));
				if (effectiveLoggers.containsKey(name)) {
					ret = LogbackHelper.asLoggerEntry(effectiveLoggers.get(name));
					ret.setOverrideLevel(level);
				} else {
					ret = new LoggerEntry(name, null, level);
				}
			}
			
			LogbackHelper.writeIncludedLoggers(overrideFile, includedLoggers.values());
			if (!fileExists) LogbackHelper.reloadConfiguration();
			
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	public List<OUpgradeStatement> listLastUpgradeStatements() throws WTException {
		UpgradeStatementDAO upgdao = UpgradeStatementDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsSysAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			String upgradeTag = upgdao.selectLastTag(con);
			return upgdao.selectByTag(con, upgradeTag);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean executeUpgradeStatement(OUpgradeStatement statement, boolean ignoreErrors) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		ServiceManager srvMgr = wta.getServiceManager();
		return srvMgr.executeUpgradeStatement(statement, ignoreErrors);
	}
	
	public void skipUpgradeStatement(OUpgradeStatement statement) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		ServiceManager srvMgr = wta.getServiceManager();
		srvMgr.skipUpgradeStatement(statement);
	}
	
	public void setMaintenanceMode(boolean active) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		ServiceManager srvMgr = wta.getServiceManager();
		srvMgr.setMaintenance(CoreManifest.ID, active);
	}
}
