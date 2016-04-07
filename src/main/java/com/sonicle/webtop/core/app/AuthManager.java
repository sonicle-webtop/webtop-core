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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.ORole;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.model.AuthResource;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.GroupDAO;
import com.sonicle.webtop.core.dal.RoleDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class AuthManager {
	private static final Logger logger = WT.getLogger(OTPManager.class);
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
	
	private static final String SYSADMIN_PSTRING = AuthResource.permissionString(AuthResource.namespacedName(CoreManifest.ID, "SYSADMIN"), "ACCESS", "*");
	private static final String WTADMIN_PSTRING = AuthResource.permissionString(AuthResource.namespacedName(CoreManifest.ID, "WTADMIN"), "ACCESS", "*");
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
	
	public List<String> getRolesAsString(UserProfile.Id pid, boolean self, boolean transitive) throws WTException {
		ArrayList<String> uids = new ArrayList<>();
		Set<Role> roles = getRolesForUser(pid, self, transitive);
		for(Role role : roles) {
			uids.add(role.getUid());
		}
		return uids;
	}
	
	public Set<Role> getRolesForUser(UserProfile.Id pid, boolean self, boolean transitive) throws WTException {
		UserManager usrm = wta.getUserManager();
		Connection con = null;
		HashSet<String> roleMap = new HashSet<>();
		LinkedHashSet<Role> roles = new LinkedHashSet<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			String userSid = usrm.userToUid(pid);
			String roleSid = usrm.userToRoleUid(pid);
			
			if(self) {
				UserDAO usedao = UserDAO.getInstance();
				OUser user = usedao.selectByUid(con, userSid);
				roles.add(new Role(roleSid, pid.getUserId(), Role.SOURCE_USER, user.getDisplayName()));
			}
			
			RoleDAO roldao = RoleDAO.getInstance();
			
			// Gets by group
			List<ORole> groles = roldao.selectFromGroupsByUser(con, userSid);
			for(ORole role : groles) {
				if(roleMap.contains(role.getRoleUid())) continue; // Skip duplicates
				roleMap.add(role.getRoleUid());
				roles.add(new Role(role.getRoleUid(), role.getName(), Role.SOURCE_GROUP, role.getDescription()));
			}
			
			// Gets direct assigned roles
			List<ORole> droles = roldao.selectDirectByUser(con, userSid);
			for(ORole role : droles) {
				if(roleMap.contains(role.getRoleUid())) continue; // Skip duplicates
				roleMap.add(role.getRoleUid());
				roles.add(new Role(role.getRoleUid(), role.getName(), Role.SOURCE_ROLE, role.getDescription()));
			}
			
			// Get transivite roles (belonging to groups)
			if(transitive) {
				List<ORole> troles = roldao.selectTransitiveFromGroupsByUser(con, userSid);
				for(ORole role : troles) {
					if(roleMap.contains(role.getRoleUid())) continue; // Skip duplicates
					roleMap.add(role.getRoleUid());
					roles.add(new Role(role.getRoleUid(), role.getName(), Role.SOURCE_TRANSITIVE, role.getDescription()));
				}
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return roles;
	}
	
	public List<Role> listRoles(String domainId, boolean fromUsers, boolean fromGroups) throws WTException {
		Connection con = null;
		ArrayList<Role> roles = new ArrayList<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			if(fromUsers) {
				UserDAO usedao = UserDAO.getInstance();
				List<OUser> users = usedao.selectActiveByDomain(con, domainId);
				for(OUser user: users) {
					roles.add(new Role(user.getRoleUid(), user.getUserId(), Role.SOURCE_USER, user.getDisplayName()));
				}
			}
			if(fromUsers) {
				GroupDAO grpdao = GroupDAO.getInstance();
				List<OGroup> groups = grpdao.selectActiveByDomain(con, domainId);
				for(OGroup group: groups) {
					roles.add(new Role(group.getRoleUid(), group.getUserId(), Role.SOURCE_GROUP, group.getDisplayName()));
				}
			}
			
			RoleDAO roldao = RoleDAO.getInstance();
			List<ORole> eroles = roldao.selectByDomain(con, domainId);
			for(ORole erole : eroles) {
				roles.add(new Role(erole.getRoleUid(), erole.getName(), Role.SOURCE_ROLE, erole.getDescription()));
			}
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return roles;
	}
	
	public ORolePermission addPermission(Connection con, UserProfile.Id pid, String serviceId, String resource, String action, String instance) throws WTException {
		UserManager usrm = wta.getUserManager();
		return addPermission(con, usrm.userToRoleUid(pid), serviceId, resource, action, instance);
	}
	
	public ORolePermission addPermission(Connection con, String roleUid, String serviceId, String resource, String action, String instance) throws WTException {
		ORolePermission perm = new ORolePermission();
		perm.setRoleUid(roleUid);
		perm.setServiceId(serviceId);
		perm.setResource(resource);
		perm.setAction(action);
		perm.setInstance(instance);
		
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		perm.setRolePermissionId(rpdao.getSequence(con).intValue());
		rpdao.insert(con, perm);
		return perm;
	}
	
	public void deletePermission(Connection con, UserProfile.Id pid, String serviceId, String resource, String action, String instance) throws WTException {
		UserManager usrm = wta.getUserManager();
		deletePermission(con, usrm.userToRoleUid(pid), serviceId, resource, action, instance);
	}
	
	public void deletePermission(Connection con, String roleUid, String serviceId, String resource, String action, String instance) throws WTException {
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		rpdao.deleteByRoleServiceResourceActionInstance(con, roleUid, serviceId, resource, action, instance);
	}
	
	public List<ORolePermission> listRolePermissions(String roleSid) throws Exception {
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			RolePermissionDAO dao = RolePermissionDAO.getInstance();
			return dao.selectByRole(con, roleSid);
		
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean isPermitted(UserProfile.Id pid, String authResource) {
		return isPermitted(pid, authResource, "ACCESS", "*");
	}
	
	public boolean isPermitted(UserProfile.Id pid, String authResource, String action) {
		return isPermitted(pid, authResource, action, "*");
	}
	
	public boolean isPermitted(UserProfile.Id pid, String authResource, String action, String instance) {
		Subject subject = getSubject(pid);
		if(subject.isPermitted(WTADMIN_PSTRING)) return true;
		return subject.isPermitted(AuthResource.permissionString(authResource, action, instance));
	}
	
	public boolean isSysAdmin(UserProfile.Id pid) {
		Subject subject = getSubject(pid);
		return subject.isPermitted(SYSADMIN_PSTRING);
	}
	
	public boolean isWebTopAdmin(UserProfile.Id pid) {
		Subject subject = getSubject(pid);
		return subject.isPermitted(WTADMIN_PSTRING);
	}
	
	private Subject getSubject(UserProfile.Id pid) {
		Subject subject = SecurityUtils.getSubject(); // Current user
		if(StringUtils.equals(((Principal)subject.getPrincipal()).getName(), pid.toString())) {
			return subject;
		} else { // Requested subject is not the current one
			//TODO: instantiate a principal on-the-fly
			return buildSubject(pid);
		}
	}
	
	public Subject buildSubject(RunContext context) {
		return buildSubject(context.getProfileId());
	}
	
	private Subject buildSubject(UserProfile.Id pid) {
		Principal principal = new Principal(pid.getDomainId(), pid.getUserId());
		PrincipalCollection principals = new SimplePrincipalCollection(principal, "com.sonicle.webtop.core.shiro.WTRealm");
		return new Subject.Builder().principals(principals).buildSubject();
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
	
	/*
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
	*/
}
