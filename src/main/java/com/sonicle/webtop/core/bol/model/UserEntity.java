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
package com.sonicle.webtop.core.bol.model;

import com.sonicle.webtop.core.bol.AssignedGroup;
import com.sonicle.webtop.core.bol.AssignedRole;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.OUserInfo;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author malbinola
 */
public class UserEntity {
	private String domainId;
	private String userId;
	private Boolean enabled;
	private String userUid;
	private String firstName;
	private String lastName;
	private String displayName;
	private List<AssignedGroup> assingedGroups = new ArrayList<>();
	private List<AssignedRole> assignedRoles = new ArrayList<>();
	private List<ORolePermission> permissions = new ArrayList<>();
	private List<ORolePermission> servicesPermissions = new ArrayList<>();
	
	public UserEntity() {}
	
	public UserEntity(OUser o, OUserInfo oi) {
		domainId = o.getDomainId();
		userId = o.getUserId();
		enabled = o.getEnabled();
		userUid = o.getUserUid();
		firstName = oi.getFirstName();
		lastName = oi.getLastName();
		displayName = o.getDisplayName();
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public UserProfileId getProfileId() {
		return new UserProfileId(getDomainId(), getUserId());
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getUserUid() {
		return userUid;
	}

	public void setUserUid(String userUid) {
		this.userUid = userUid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public List<AssignedGroup> getAssignedGroups() {
		return assingedGroups;
	}

	public void setAssignedGroups(List<AssignedGroup> groups) {
		this.assingedGroups = groups;
	}
	
	public List<AssignedRole> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(List<AssignedRole> roles) {
		this.assignedRoles = roles;
	}
	
	public List<ORolePermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<ORolePermission> permissions) {
		this.permissions = permissions;
	}

	public List<ORolePermission> getServicesPermissions() {
		return servicesPermissions;
	}

	public void setServicesPermissions(List<ORolePermission> servicesPermissions) {
		this.servicesPermissions = servicesPermissions;
	}
}
