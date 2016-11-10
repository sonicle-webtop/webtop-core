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

import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.model.RoleEntity;
import java.util.ArrayList;

/**
 *
 * @author malbinola
 */
public class JsRole {
	public String roleUid;
	public String domainId;
	public String name;
	public String description;
	public ArrayList<Service> assignedServices = new ArrayList<>();
	public ArrayList<Permission> permissions = new ArrayList<>();
	
	public JsRole() {}
	
	public JsRole(RoleEntity o) {
		roleUid = o.getRoleUid();
		domainId = o.getDomainId();
		name = o.getName();
		description = o.getDescription();
		
		for(ORolePermission perm : o.getServicesPermissions()) {
			assignedServices.add(new Service(roleUid, perm));
		}
		for(ORolePermission perm : o.getPermissions()) {
			permissions.add(new Permission(roleUid, perm));
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
	
	public static RoleEntity buildRoleEntity(JsRole js) {
		RoleEntity re = new RoleEntity();
		re.setRoleUid(js.roleUid);
		re.setDomainId(js.domainId);
		re.setName(js.name);
		re.setDescription(js.description);
		
		for(Service js1 : js.assignedServices) {
			final ORolePermission rp = new ORolePermission();
			rp.setRolePermissionId(js1.permissionId);
			rp.setInstance(js1.serviceId);
			
			re.getServicesPermissions().add(rp);
		}
		for(Permission js1 : js.permissions) {
			final ORolePermission rp = new ORolePermission();
			rp.setRolePermissionId(js1.rolePermissionId);
			rp.setServiceId(js1.serviceId);
			rp.setKey(js1.groupName);
			rp.setAction(js1.action);
			rp.setInstance(js1.instance);
			
			re.getPermissions().add(rp);
		}
		
		return re;
	}
}
