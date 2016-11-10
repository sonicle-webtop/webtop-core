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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.webtop.core.bol.AssignedGroup;
import com.sonicle.webtop.core.bol.AssignedRole;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.model.UserEntity;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author malbinola
 */
public class JsUser {
	public String profileId;
	public String domainId;
	public String userId;
	public Boolean enabled;
	public String password; // Only useful during insertion
	public String password2; // Only useful during insertion
	public String firstName;
	public String lastName;
	public String displayName;
	public List<Group> assignedGroups = new ArrayList<>();
	public List<Role> assignedRoles = new ArrayList<>();
	public ArrayList<Service> assignedServices = new ArrayList<>();
	public ArrayList<Permission> permissions = new ArrayList<>();
	
	public JsUser() {}
	
	public JsUser(UserEntity o) {
		profileId = o.getProfileId().toString();
		domainId = o.getDomainId();
		userId = o.getUserId();
		enabled = o.getEnabled();
		password = null;
		password2 = null;
		firstName = o.getFirstName();
		lastName = o.getLastName();
		displayName = o.getDisplayName();
		
		for(AssignedGroup assiGroup : o.getAssignedGroups()) {
			assignedGroups.add(new Group(profileId, assiGroup));
		}
		for(AssignedRole assiRole : o.getAssignedRoles()) {
			assignedRoles.add(new Role(profileId, assiRole));
		}
		for(ORolePermission perm : o.getServicesPermissions()) {
			assignedServices.add(new Service(profileId, perm));
		}
		for(ORolePermission perm : o.getPermissions()) {
			permissions.add(new Permission(profileId, perm));
		}
	}
	
	public static class Group extends JsFkModel {
		public Integer associationId;
		public String groupId;
		
		public Group() {}
		
		public Group(String fk, AssignedGroup o) {
			super(fk);
			associationId = o.getUserAssociationId();
			groupId = o.getGroupId();
		}
	}
	
	public static class Role extends JsFkModel {
		public Integer associationId;
		public String roleUid;
		
		public Role() {}
		
		public Role(String fk, AssignedRole o) {
			super(fk);
			associationId = o.getRoleAssociationId();
			roleUid = o.getRoleUid();
		}
	}
	
	public static class Service extends JsFkModel {
		public Integer permissionId;
		public String serviceId;
		
		public Service() {}
		
		public Service(String fk, ORolePermission o) {
			super(fk);
			permissionId = o.getRolePermissionId();
			serviceId = o.getInstance();
		}
	}
	
	public static class Permission extends JsFkModel {
		public Integer rolePermissionId;
		public String serviceId;
		public String groupName;
		public String action;
		public String instance;
		
		public Permission() {}
		
		public Permission(String fk, ORolePermission o) {
			super(fk);
			rolePermissionId = o.getRolePermissionId();
			serviceId = o.getServiceId();
			groupName = o.getKey();
			action = o.getAction();
			instance = o.getInstance();
		}
	}
	
	public static UserEntity buildUserEntity(JsUser js) {
		UserEntity ue = new UserEntity();
		ue.setDomainId(js.domainId);
		ue.setUserId(js.userId);
		ue.setEnabled(js.enabled);
		ue.setFirstName(js.firstName);
		ue.setLastName(js.lastName);
		ue.setDisplayName(js.displayName);
		
		for(Group js1 : js.assignedGroups) {
			final AssignedGroup o1 = new AssignedGroup();
			o1.setUserAssociationId(js1.associationId);
			o1.setGroupId(js1.groupId);
			
			ue.getAssignedGroups().add(o1);
		}
		for(Role js1 : js.assignedRoles) {
			final AssignedRole ar = new AssignedRole();
			ar.setRoleAssociationId(js1.associationId);
			ar.setRoleUid(js1.roleUid);
			
			ue.getAssignedRoles().add(ar);
		}
		for(Service js1 : js.assignedServices) {
			final ORolePermission rp = new ORolePermission();
			rp.setRolePermissionId(js1.permissionId);
			rp.setInstance(js1.serviceId);
			
			ue.getServicesPermissions().add(rp);
		}
		for(Permission js1 : js.permissions) {
			final ORolePermission rp = new ORolePermission();
			rp.setRolePermissionId(js1.rolePermissionId);
			rp.setServiceId(js1.serviceId);
			rp.setKey(js1.groupName);
			rp.setAction(js1.action);
			rp.setInstance(js1.instance);
			
			ue.getPermissions().add(rp);
		}
		
		return ue;
	}
}
