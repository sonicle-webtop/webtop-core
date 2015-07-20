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
import com.sonicle.webtop.core.dal.GroupRoleDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.UserGroupDAO;
import com.sonicle.webtop.core.dal.UserRoleDAO;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class SystemManager {
	
	private static final Logger logger = WT.getLogger(TFAManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	static synchronized SystemManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		SystemManager sysm = new SystemManager(wta);
		initialized = true;
		logger.info("SystemManager initialized");
		return sysm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private SystemManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		logger.info("SystemManager destroyed");
	}
	
	
	
	public List<Role> getRolesForGroup(String domainId, String groupId) throws Exception {
		Connection con = null;
		ArrayList<Role> roles = new ArrayList<>();
		
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
	
	public List<Role> getRolesForUser(String domainId, String userId, boolean self, boolean transitive) throws Exception {
		Connection con = null;
		HashSet<String> roleMap = new HashSet<>();
		ArrayList<Role> roles = new ArrayList<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			if(self) {
				roles.add(new Role(userId, userId));
			}
			
			// Gets direct assigned roles
			UserRoleDAO urdao = UserRoleDAO.getInstance();
			List<UserRole> userRoles = urdao.viewByDomainUser(con, domainId, userId);
			for(UserRole role : userRoles) {
				if(roleMap.contains(role.getRoleId())) continue; // Skip duplicate roles
				roleMap.add(role.getRoleId());
				roles.add(new Role(role.getRoleId(), role.getRoleDescription()));
			}
			
			if(transitive) {
				// Get transivite roles (belonging to groups)
				List<Role> groupRoles = null;
				UserGroupDAO ugdao = UserGroupDAO.getInstance();
				List<OUserGroup> groups = ugdao.selectByDomainUser(con, domainId, userId);
				for(OUserGroup ug : groups) {
					groupRoles = getRolesForGroup(domainId, ug.getGroupId());
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
	
	public boolean isPermitted(String resource) {
		return isPermitted(resource, "ACCESS", "*");
	}
	
	public boolean isPermitted(String resource, String action) {
		return isPermitted(resource, action, "*");
	}
	
	public boolean isPermitted(String resource, String action, String instance) {
		Subject currentUser = SecurityUtils.getSubject();
		Principal principal = (Principal)currentUser.getPrincipal();
		if(principal.isAdmin()) return true; // Avoids permission check for WebTop admin
		return currentUser.isPermitted(AuthResource.permissionString(resource, action, instance));
	}
	
	public boolean hasRole(String roleName) {
		Subject currentUser = SecurityUtils.getSubject();
		return currentUser.hasRole(roleName);
	}
}
