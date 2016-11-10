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

import com.sonicle.webtop.core.app.AuthManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.UserManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.model.DirectoryUser;
import com.sonicle.webtop.core.bol.model.DomainEntity;
import com.sonicle.webtop.core.bol.model.DomainSetting;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleEntity;
import com.sonicle.webtop.core.bol.model.SessionInfo;
import com.sonicle.webtop.core.bol.model.SystemSetting;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.List;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreAdminManager extends BaseManager {
	private static final Logger logger = WT.getLogger(CoreAdminManager.class);
	private WebTopApp wta = null;
	
	public CoreAdminManager(WebTopApp wta, boolean fastInit, UserProfile.Id targetProfileId) {
		super(fastInit, targetProfileId);
		this.wta = wta;
	}
	
	public List<SessionInfo> listSessions() throws WTException {
		RunContext.ensureIsSysAdmin();
		return wta.getSessionManager().listOnlineSessions();
	}
	
	public boolean isOnlineSession(String sessionId) {
		return wta.getSessionManager().isOnline(sessionId);
	}
	
	public void invalidateSession(String sessionId) throws WTException {
		RunContext.ensureIsSysAdmin();
		wta.getSessionManager().invalidateSession(sessionId);
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
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			return usem.getDomainEntity(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot get domain [{0}]", domainId);
		}
	}
	
	public void addDomain(DomainEntity domain) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			usem.addDomain(domain);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot add domain");
		}
	}
	
	public void updateDomain(DomainEntity domain) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			usem.updateDomain(domain);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot update domain [{0}]", domain.getDomainId());
		}
	}
	
	public void deleteDomain(String domainId) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			usem.deleteDomain(domainId);
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
		RunContext.ensureIsSysAdmin();
		
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
		RunContext.ensureIsSysAdmin();
		
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
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setm = wta.getSettingsManager();
		return setm.deleteServiceSetting(domainId, serviceId, key);
	}
	
	/**
	 * Lists domain real roles (those defined as indipendent role).
	 * @param domainId The domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	public List<Role> listRoles(String domainId) throws WTException {
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		AuthManager authm = wta.getAuthManager();
		try {
			return authm.listRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list roles [{0}]", domainId);
		}
	}
	
	/**
	 * Lists domain users roles (those coming from a user).
	 * @param domainId The domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	public List<Role> listUsersRoles(String domainId) throws WTException {
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		AuthManager authm = wta.getAuthManager();
		try {
			return authm.listUsersRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list users roles [{0}]", domainId);
		}
	}
	
	/**
	 * Lists domain groups roles (those coming from a group).
	 * @param domainId The domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	public List<Role> listGroupsRoles(String domainId) throws WTException {
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		AuthManager authm = wta.getAuthManager();
		try {
			return authm.listGroupsRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups roles [{0}]", domainId);
		}
	}
	
	public RoleEntity getRole(String uid) throws WTException {
		AuthManager authm = wta.getAuthManager();
		
		String domainId = authm.getRoleDomain(uid);
		if(domainId == null) throw new WTException("Role not found [{0}]", uid);
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			return authm.getRole(uid);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot get role [{0}]", uid);
		}
	}
	
	public void addRole(RoleEntity role) throws WTException {
		AuthManager authm = wta.getAuthManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			authm.addRole(role);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot add role");
		}
	}
	
	public void updateRole(RoleEntity role) throws WTException {
		AuthManager authm = wta.getAuthManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			authm.updateRole(role);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot update role [{0}]", role.getRoleUid());
		}
	}
	
	public void deleteRole(String uid) throws WTException {
		AuthManager authm = wta.getAuthManager();
		
		String domainId = authm.getRoleDomain(uid);
		if(domainId == null) throw new WTException("Role not found [{0}]", uid);
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			authm.deleteRole(uid);
		} catch(Exception ex) {
			throw new WTException(ex, "Cannot delete role [{0}]", uid);
		}
	}
	
	public List<DirectoryUser> listDirectoryUsers(String domainId) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			ODomain domain = usem.getDomain(domainId);
			return usem.listDirectoryUsers(domain);
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list directory users [{0}]", domainId);
		}
	}
	
	public List<OUser> listUsers(String domainId, boolean enabledOnly) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			return usem.listUsers(domainId, enabledOnly);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list users [{0}]", domainId);
		}
	}
	
	public UserEntity getUser(UserProfile.Id pid) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			return usem.getUserEntity(pid);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to get user [{0}]", pid.toString());
		}
	}
	
	/*
	public OUser getUser(UserProfile.Id pid) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			return usem.getUser(pid);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to get user [{0}]", pid.toString());
		}
	}
	*/
	
	public void addUser(UserEntity user, char[] password) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			usem.addUser(true, user, password);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to add user [{0}]", user.getProfileId().toString());
		}
	}
	
	public void updateUser(UserEntity user) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			usem.updateUser(user);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to update user [{0}]", user.getProfileId().toString());
		}
	}
	
	public boolean updateUser(UserProfile.Id pid, boolean enabled) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			return usem.updateUser(pid, enabled);
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to update user [{0}]", pid.toString());
		}
	}
	
	public void updateUserPassword(UserProfile.Id pid, char[] newPassword) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			usem.updateUserPassword(pid, null, newPassword);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to change user password [{0}]", pid.toString());
		}
	}
	
	public void deleteUser(boolean deep, UserProfile.Id pid) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			usem.deleteUser(pid, deep);
			
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to delete user [{0}]", pid.toString());
		}
	}
	
	public List<OGroup> listGroups(String domainId) throws WTException {
		UserManager usem = wta.getUserManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsSysAdmin();
		
		try {
			return usem.listGroups(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups [{0}]", domainId);
		}
	}
}
