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
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.webtop.core.bol.AssignedRole;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.ORole;
import com.sonicle.webtop.core.bol.ORoleAssociation;
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
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
	
	public AbstractDirectory getAuthDirectory(String authUri) throws WTException {
		try {
			return getAuthDirectory(new URI(authUri));
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid authentication URI [{0}]", authUri);
		}
	}
	
	public AbstractDirectory getAuthDirectory(URI authUri) throws WTException {
		DirectoryManager dirManager = DirectoryManager.getManager();
		AbstractDirectory directory = dirManager.getDirectory(authUri.getScheme());
		if(directory == null) throw new WTException("Directory not supported [{0}]", authUri.getScheme());
		return directory;
	}
	
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
		Connection con = null;
		
		try {
			con = WT.getConnection(CoreManifest.ID);
			return listAssignedRoles(con, userUid);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	List<AssignedRole> listAssignedRoles(Connection con, String userUid) throws WTException {
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		
		try {
			return rolassdao.viewAssignedByUser(con, userUid);
			
		} catch(DAOException ex) {
			throw new WTException(ex, "DB error");
		}
	}
	
	void deleteRoleAssociation(Connection con, int roleAssociationId) throws WTException {
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		rolassdao.deleteById(con, roleAssociationId);
	}
	
	void deleteRoleAssociationByUser(Connection con, String userUid) throws WTException {
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		rolassdao.deleteByUser(con, userUid);
	}
	
	void addRoleAssociation(Connection con, String userUid, String roleUid) throws WTException {
		RoleAssociationDAO rolassdao = RoleAssociationDAO.getInstance();
		
		ORoleAssociation ora = new ORoleAssociation();
		ora.setRoleAssociationId(rolassdao.getSequence(con).intValue());
		ora.setUserUid(userUid);
		ora.setRoleUid(roleUid);
		rolassdao.insert(con, ora);
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
				addPermission(con, orole.getRoleUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
			}
			for(ORolePermission perm : role.getServicesPermissions()) {
				addPermission(con, orole.getRoleUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
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
			
			CollectionChangeSet<ORolePermission> changeSet1 = LangUtils.getCollectionChanges(oldRole.getPermissions(), role.getPermissions());
			for(ORolePermission perm : changeSet1.deleted) {
				rolperdao.deleteById(con, perm.getRolePermissionId());
			}
			for(ORolePermission perm : changeSet1.inserted) {
				addPermission(con, oldRole.getRoleUid(), perm.getServiceId(), perm.getKey(), perm.getAction(), "*");
			}
			
			CollectionChangeSet<ORolePermission> changeSet2 = LangUtils.getCollectionChanges(oldRole.getServicesPermissions(), role.getServicesPermissions());
			for(ORolePermission perm : changeSet2.deleted) {
				rolperdao.deleteById(con, perm.getRolePermissionId());
			}
			for(ORolePermission perm : changeSet2.inserted) {
				addPermission(con, oldRole.getRoleUid(), CoreManifest.ID, "SERVICE", ServicePermission.ACTION_ACCESS, perm.getInstance());
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
	
	public List<String> getComputedRolesAsStringByUser(UserProfile.Id pid, boolean self, boolean transitive) throws WTException {
		ArrayList<String> uids = new ArrayList<>();
		Set<RoleWithSource> roles = getComputedRolesByUser(pid, self, transitive);
		for(RoleWithSource role : roles) {
			uids.add(role.getRoleUid());
		}
		return uids;
	}
	
	public Set<RoleWithSource> getComputedRolesByUser(UserProfile.Id pid, boolean self, boolean transitive) throws WTException {
		UserManager usrm = wta.getUserManager();
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
	
	
	public ORolePermission addPermission(Connection con, UserProfile.Id pid, String serviceId, String key, String action, String instance) throws WTException {
		UserManager usrm = wta.getUserManager();
		return addPermission(con, usrm.userToUid(pid), serviceId, key, action, instance);
	}
	
	public ORolePermission addPermission(Connection con, String roleUid, String serviceId, String key, String action, String instance) throws WTException {
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
	
	public int deletePermission(Connection con, int permissionId) throws DAOException {
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		return rpdao.deleteById(con, permissionId);
	}
	
	public int deletePermissionByRole(Connection con, String roleUid) throws DAOException {
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		return rpdao.deleteByRole(con, roleUid);
	}
	
	public int deletePermissionBy(Connection con, UserProfile.Id pid, String serviceId, String key, String action, String instance) throws DAOException {
		UserManager usrm = wta.getUserManager();
		return deletePermissionBy(con, usrm.userToUid(pid), serviceId, key, action, instance);
	}
	
	public int deletePermissionBy(Connection con, String roleUid, String serviceId, String key, String action, String instance) throws DAOException {
		RolePermissionDAO rpdao = RolePermissionDAO.getInstance();
		return rpdao.deleteByRoleServiceKeyActionInstance(con, roleUid, serviceId, key, action, instance);
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
	
	public static class EntityPermissions {
		public ArrayList<ORolePermission> others;
		public ArrayList<ORolePermission> services;
		
		public EntityPermissions(ArrayList<ORolePermission> others, ArrayList<ORolePermission> services) {
			this.others = others;
			this.services = services;
		}
	}
}
