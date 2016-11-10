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

import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.ORole;
import com.sonicle.webtop.core.bol.OUser;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author malbinola
 */
public class Role {
	private String roleUid;
	private String domainId;
	private String name;
	private String description;
	
	public Role() {}
	
	public Role(String roleUid, String domainId, String name, String description) {
		this.roleUid = roleUid;
		this.domainId = domainId;
		this.name = name;
		this.description = description;
	}
	
	public Role(ORole o) {
		this.roleUid = o.getRoleUid();
		this.domainId = o.getDomainId();
		this.name = o.getName();
		this.description = o.getDescription();
	}
	
	public Role(OGroup o) {
		this.roleUid = o.getUserUid();
		this.domainId = o.getDomainId();
		this.name = o.getUserId();
		this.description = o.getDisplayName();
	}
	
	public Role(OUser o) {
		this.roleUid = o.getUserUid();
		this.domainId = o.getDomainId();
		this.name = o.getUserId();
		this.description = o.getDisplayName();
	}
	
	public String getRoleUid() {
		return roleUid;
	}

	public void setRoleUid(String roleUid) {
		this.roleUid = roleUid;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(roleUid)
			.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Role == false) return false;
		if(this == obj) return true;
		final Role otherObject = (Role) obj;
		return new EqualsBuilder()
			.append(roleUid, otherObject.roleUid)
			.isEquals();
	}
}
