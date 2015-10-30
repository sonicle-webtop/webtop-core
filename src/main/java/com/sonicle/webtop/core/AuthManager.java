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
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.bol.GroupRole;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OUserGroup;
import com.sonicle.webtop.core.bol.UserRole;
import com.sonicle.webtop.core.bol.model.AuthResource;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.GroupRoleDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.UserGroupDAO;
import com.sonicle.webtop.core.dal.UserRoleDAO;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class AuthManager {
	public static final String ROLE_SYSADMIN = "sysadmin";
	private static final Logger logger = WT.getLogger(TFAManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	static synchronized AuthManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		AuthManager autm = new AuthManager(wta);
		initialized = true;
		logger.info("AuthManager initialized");
		return autm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private AuthManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		logger.info("AuthManager destroyed");
	}
	
	public ORolePermission addPermission(Connection con, UserProfile.Id roleId, String serviceId, String resource, String action, String instance) throws WTException {
		ORolePermission perm = new ORolePermission();
		perm.setDomainId(roleId.getDomainId());
		perm.setRoleId(roleId.getUserId());
		perm.setServiceId(serviceId);
		perm.setResource(resource);
		perm.setAction(action);
		perm.setInstance(instance);
		
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		perm.setUid(rpdao.getSequence(con).intValue());
		rpdao.insert(con, perm);
		return perm;
	}
	
	public void permissionDelete(Connection con, UserProfile.Id roleId, String serviceId, String resource, String action, String instance) throws WTException {
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		rpdao.deleteByDomainRoleServiceResourceActionInstance(con, roleId.getDomainId(), roleId.getUserId(), serviceId, resource, action, instance);
	}
	
	public Set<Role> getRolesForGroup(String domainId, String groupId) throws Exception {
		Connection con = null;
		LinkedHashSet<Role> roles = new LinkedHashSet<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			GroupRoleDAO grdao = GroupRoleDAO.getInstance();
			List<GroupRole> groupRoles = grdao.viewByGroup(con, domainId, groupId);
			for(GroupRole role : groupRoles) {
				roles.add(new Role(role.getRoleId(), role.getRoleDescription()));
			}
		
		} finally {
			DbUtils.closeQuietly(con);
		}
		return roles;
	}
	
	public Set<Role> getRolesForUser(UserProfile.Id pid, boolean self, boolean transitive) throws Exception {
		Connection con = null;
		HashSet<String> roleMap = new HashSet<>();
		LinkedHashSet<Role> roles = new LinkedHashSet<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			if(Principal.xisAdmin(pid.toString())) {
				// Built-in role that marks users that have admin rights
				roles.add(new Role(ROLE_SYSADMIN, ROLE_SYSADMIN));
			}
			
			if(self) {
				roles.add(new Role(pid.getUserId(), pid.getUserId()));
			}
			
			// Gets direct assigned roles
			UserRoleDAO urdao = UserRoleDAO.getInstance();
			List<UserRole> userRoles = urdao.viewByDomainUser(con, pid.getDomainId(), pid.getUserId());
			for(UserRole role : userRoles) {
				if(roleMap.contains(role.getRoleId())) continue; // Skip duplicate roles
				roleMap.add(role.getRoleId());
				roles.add(new Role(role.getRoleId(), role.getRoleDescription()));
			}
			
			if(transitive) {
				// Get transivite roles (belonging to groups)
				Set<Role> groupRoles = null;
				UserGroupDAO ugdao = UserGroupDAO.getInstance();
				List<OUserGroup> groups = ugdao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
				for(OUserGroup ug : groups) {
					groupRoles = getRolesForGroup(pid.getDomainId(), ug.getGroupId());
					for(Role role : groupRoles) {
						if(roleMap.contains(role.getId())) continue; // Skip duplicate roles
						roleMap.add(role.getId());
						roles.add(new Role(role.getId(), role.getDescription()));
					}
				}
			}
		
		} finally {
			DbUtils.closeQuietly(con);
		}
		return roles;
	}
	
	public List<ORolePermission> getRolePermissions(String domainId, String roleId) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			RolePermissionDAO pdao = RolePermissionDAO.getInstance();
			return pdao.selectByDomainRole(con, domainId, roleId);
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean isPermitted(UserProfile.Id pid, String resource) {
		return isPermitted(pid, resource, "ACCESS", "*");
	}
	
	public boolean isPermitted(UserProfile.Id pid, String resource, String action) {
		return isPermitted(pid, resource, action, "*");
	}
	
	public boolean isPermitted(UserProfile.Id pid, String resource, String action, String instance) {
		Subject subject = SecurityUtils.getSubject(); // Current user
		if(!StringUtils.equals(((Principal)subject.getPrincipal()).getName(), pid.toString())) {
			// Requested subject is not the current one
			//TODO: instantiate a principal on-the-fly
		}
		
		/*
		if(subject.hasRole(ROLE_WTADMIN)) {
			return true; // Skip permission check for WebTop admin
		} else {
			return subject.isPermitted(AuthResource.permissionString(resource, action, instance));
		}
		*/
		return subject.isPermitted(AuthResource.permissionString(resource, action, instance));
	}
	
	public void ensureIsPermitted(UserProfile.Id pid, String resource) {
		if(!isPermitted(pid, resource)) throw new AuthException("ACCESS permission on {0} is required", resource);
	}
	
	public void ensureIsPermitted(UserProfile.Id pid, String resource, String action) {
		if(!isPermitted(pid, resource, action)) throw new AuthException("{0} permission on {1} is required", action, resource);
	}
	
	public void ensureIsPermitted(UserProfile.Id pid, String resource, String action, String instance) {
		if(!isPermitted(pid, resource, action, instance)) throw new AuthException("{0} permission on {1}@{2} is required", action, resource, instance);
	}
	
	public boolean hasRole(UserProfile.Id pid, String roleName) {
		Subject subject = SecurityUtils.getSubject(); // Current user
		if(!StringUtils.equals(((Principal)subject.getPrincipal()).getName(), pid.toString())) {
			// Requested subject is not the current one
			//TODO: instantiate a principal on-the-fly
		}
		return subject.hasRole(roleName);
	}
	
	public void ensureHasRole(UserProfile.Id pid, String roleName) {
		if(!hasRole(pid, roleName)) throw new AuthException("{0} role is required", roleName);
	}
}
