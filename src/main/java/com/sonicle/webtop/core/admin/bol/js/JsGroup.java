/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.admin.bol.js;

import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.GroupBase;
import com.sonicle.webtop.core.app.model.PermissionString;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author malbinola
 */
public class JsGroup {
	public String id;
	public String groupId;
	public String description;
	public ArrayList<AclSubject> assignedUsers;
	public ArrayList<AclSubject> assignedRoles;
	public ArrayList<Permission> permissions;
	public ArrayList<AllowedService> allowedServices;
	
	public JsGroup(Group item) {
		this.id = item.getGroupId();
		this.groupId = item.getGroupId();
		this.description = item.getDescription();
		this.assignedUsers = new ArrayList<>();
		for (String userUids : item.getAssignedUsers()) {
			this.assignedUsers.add(new AclSubject(userUids));
		}
		this.assignedRoles = new ArrayList<>();
		for (String roleUids : item.getAssignedRoles()) {
			this.assignedRoles.add(new AclSubject(roleUids));
		}
		this.permissions = new ArrayList<>();
		for (PermissionString ps : item.getPermissions()) {
			this.permissions.add(new Permission(ps.toString()));
		}
		this.allowedServices = new ArrayList<>();
		for (String serviceId : item.getAllowedServiceIds()) {
			this.allowedServices.add(new AllowedService(serviceId));
		}
	}
	
	public static GroupBase createGroupForAdd(JsGroup js) {
		GroupBase item = new GroupBase();
		item.setDescription(js.description);
		
		Set<String> assignedGroupSids = new HashSet<>();
		for (AclSubject as : js.assignedUsers) {
			assignedGroupSids.add(as.sid);
		}
		item.setAssignedUsers(assignedGroupSids);
		
		Set<String> assignedRoleSids = new HashSet<>();
		for (AclSubject as : js.assignedRoles) {
			assignedRoleSids.add(as.sid);
		}
		item.setAssignedRoles(assignedRoleSids);
		
		Set<PermissionString> permissionStrings = new HashSet<>();
		for (Permission p : js.permissions) {
			permissionStrings.add(new PermissionString(p.string));
		}
		item.setPermissions(permissionStrings);
		
		Set<String> allowedServiceIds = new HashSet<>();
		for (AllowedService as : js.allowedServices) {
			allowedServiceIds.add(as.serviceId);
		}
		item.setAllowedServiceIds(allowedServiceIds);
		
		return item;
	}
	
	public static GroupBase createGroupForUpdate(JsGroup js) {
		GroupBase item = new GroupBase();
		item.setDescription(js.description);
		
		Set<String> assignedGroupSids = new HashSet<>();
		for (AclSubject as : js.assignedUsers) {
			assignedGroupSids.add(as.sid);
		}
		item.setAssignedUsers(assignedGroupSids);
		
		Set<String> assignedRoleSids = new HashSet<>();
		for (AclSubject as : js.assignedRoles) {
			assignedRoleSids.add(as.sid);
		}
		item.setAssignedRoles(assignedRoleSids);
		
		Set<PermissionString> permissionStrings = new HashSet<>();
		for (Permission p : js.permissions) {
			permissionStrings.add(new PermissionString(p.string));
		}
		item.setPermissions(permissionStrings);
		
		Set<String> allowedServiceIds = new HashSet<>();
		for (AllowedService as : js.allowedServices) {
			allowedServiceIds.add(as.serviceId);
		}
		item.setAllowedServiceIds(allowedServiceIds);
		
		return item;
	}
	
	public static class AclSubject {
		public String sid;
		
		public AclSubject(String sid) {
			this.sid = sid;
		}
	}
	
	public static class Permission {
		public String string;
		
		public Permission(String string) {
			this.string = string;
		}
	}
	
	public static class AllowedService {
		public String serviceId;
		
		public AllowedService(String serviceId) {
			this.serviceId = serviceId;
		}
	}
}
