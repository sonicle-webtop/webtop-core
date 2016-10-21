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
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.ORole;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleEntity;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.GroupDAO;
import com.sonicle.webtop.core.dal.RoleAssociationDAO;
import com.sonicle.webtop.core.dal.RoleDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
	
	public static final String SYSADMIN_PSTRING = ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "SYSADMIN"), "ACCESS", "*");
	public static final String WTADMIN_PSTRING = ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), "ACCESS", "*");
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
	
	public List<Role> listUsersRoles(String domainId) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		ArrayList<Role> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			List<OUser> users = dao.selectActiveByDomain(con, domainId);
			for(OUser user: users) items.add(new Role(user));
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return items;
	}
	
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
	
	/*
	public List<Role2> listRoles(String domainId, boolean fromUsers, boolean fromGroups) throws WTException {
		Connection con = null;
		ArrayList<Role2> roles = new ArrayList<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			if(fromUsers) {
				UserDAO usedao = UserDAO.getInstance();
				List<OUser> users = usedao.selectActiveByDomain(con, domainId);
				for(OUser user: users) roles.add(new Role2(user));
			}
			if(fromUsers) {
				GroupDAO grpdao = GroupDAO.getInstance();
				List<OGroup> groups = grpdao.selectByDomain(con, domainId);
				for(OGroup group: groups) roles.add(new Role2(group));
			}
			
			RoleDAO roldao = RoleDAO.getInstance();
			List<ORole> eroles = roldao.selectByDomain(con, domainId);
			for(ORole erole : eroles) roles.add(new Role2(erole));
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
		return roles;
	}
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
		RolePermissionDAO rolperdao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			
			ORole orole = roldao.selectByUid(con, uid);
			if(orole == null) throw new WTException("Cannot retrieve role [{0}]", uid);
			List<ORolePermission> operms = rolperdao.selectByRole(con, uid);
			
			ArrayList<ORolePermission> perms = new ArrayList<>();
			ArrayList<ORolePermission> svcPerms = new ArrayList<>();
			for(ORolePermission operm : operms) {
				if(operm.getInstance().equals("*")) {
					perms.add(operm);
				} else {
					if(operm.getServiceId().equals(CoreManifest.ID) && operm.getKey().equals("SERVICE") && operm.getAction().equals("ACCESS")) {
						svcPerms.add(operm);
					}
				}
			}
			
			RoleEntity role = new RoleEntity(orole);
			role.setPermissions(perms);
			role.setServicesPermissions(svcPerms);
			
			return role;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addRole(RoleEntity role) throws WTException {
		RoleDAO roldao = RoleDAO.getInstance();
		RolePermissionDAO rolperdao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID, false);
			
			ORole orole = new ORole();
			orole.setRoleUid(UUID.randomUUID().toString());
			orole.setDomainId(role.getDomainId());
			orole.setName(role.getName());
			orole.setDescription(role.getDescription());
			roldao.insert(con, orole);
			
			for(ORolePermission perm : role.getPermissions()) {
				perm.setRolePermissionId(rolperdao.getSequence(con).intValue());
				perm.setRoleUid(orole.getRoleUid());
				perm.setInstance("*");
				rolperdao.insert(con, perm);
			}
			for(ORolePermission perm : role.getServicesPermissions()) {
				perm.setRolePermissionId(rolperdao.getSequence(con).intValue());
				perm.setRoleUid(orole.getRoleUid());
				perm.setServiceId(CoreManifest.ID);
				perm.setKey("SERVICE");
				perm.setAction(ServicePermission.ACTION_ACCESS);
				rolperdao.insert(con, perm);
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
			
			con = WT.getConnection(CoreManifest.ID, false);
			
			ORole orole = new ORole();
			orole.setRoleUid(role.getRoleUid());
			orole.setName(role.getName());
			orole.setDescription(role.getDescription());
			roldao.update(con, orole);
			
			CollectionChangeSet<ORolePermission> changeSet1 = LangUtils.getCollectionChanges(oldRole.getPermissions(), role.getPermissions());
			for(ORolePermission perm : changeSet1.deleted) {
				rolperdao.deleteById(con, perm.getRolePermissionId());
			}
			for(ORolePermission perm : changeSet1.inserted) {
				perm.setRolePermissionId(rolperdao.getSequence(con).intValue());
				perm.setRoleUid(orole.getRoleUid());
				perm.setInstance("*");
				rolperdao.insert(con, perm);
			}
			
			CollectionChangeSet<ORolePermission> changeSet2 = LangUtils.getCollectionChanges(oldRole.getServicesPermissions(), role.getServicesPermissions());
			for(ORolePermission perm : changeSet2.deleted) {
				rolperdao.deleteById(con, perm.getRolePermissionId());
			}
			for(ORolePermission perm : changeSet2.inserted) {
				perm.setRolePermissionId(rolperdao.getSequence(con).intValue());
				perm.setRoleUid(orole.getRoleUid());
				perm.setServiceId(CoreManifest.ID);
				perm.setKey("SERVICE");
				perm.setAction(ServicePermission.ACTION_ACCESS);
				rolperdao.insert(con, perm);
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
	
	public List<String> getRolesAsStringByUser(UserProfile.Id pid, boolean self, boolean transitive) throws WTException {
		ArrayList<String> uids = new ArrayList<>();
		Set<RoleWithSource> roles = getRolesByUser(pid, self, transitive);
		for(RoleWithSource role : roles) {
			uids.add(role.getRoleUid());
		}
		return uids;
	}
	
	public Set<RoleWithSource> getRolesByUser(UserProfile.Id pid, boolean self, boolean transitive) throws WTException {
		UserManager usrm = wta.getUserManager();
		Connection con = null;
		HashSet<String> roleMap = new HashSet<>();
		LinkedHashSet<RoleWithSource> roles = new LinkedHashSet<>();
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			String userUid = usrm.userToUid(pid);
			String userRoleUid = usrm.userToRoleUid(pid);
			
			if(self) {
				UserDAO usedao = UserDAO.getInstance();
				OUser user = usedao.selectByUid(con, userUid);
				roles.add(new RoleWithSource(RoleWithSource.SOURCE_USER, userRoleUid, user.getDomainId(), pid.getUserId(), user.getDisplayName()));
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
	
	
	
	public ORolePermission addPermission(Connection con, UserProfile.Id pid, String serviceId, String key, String action, String instance) throws WTException {
		UserManager usrm = wta.getUserManager();
		return addPermission(con, usrm.userToRoleUid(pid), serviceId, key, action, instance);
	}
	
	public ORolePermission addPermission(Connection con, String roleUid, String serviceId, String key, String action, String instance) throws WTException {
		ORolePermission perm = new ORolePermission();
		perm.setRoleUid(roleUid);
		perm.setServiceId(serviceId);
		perm.setKey(key);
		perm.setAction(action);
		perm.setInstance(instance);
		
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		perm.setRolePermissionId(rpdao.getSequence(con).intValue());
		rpdao.insert(con, perm);
		return perm;
	}
	
	public void deletePermission(Connection con, UserProfile.Id pid, String serviceId, String key, String action, String instance) throws WTException {
		UserManager usrm = wta.getUserManager();
		deletePermission(con, usrm.userToRoleUid(pid), serviceId, key, action, instance);
	}
	
	public void deletePermission(Connection con, String roleUid, String serviceId, String key, String action, String instance) throws WTException {
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		rpdao.deleteByRoleServiceKeyActionInstance(con, roleUid, serviceId, key, action, instance);
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
	
	
	
	/*
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
	*/
	
	
	
	
	
	
	
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
