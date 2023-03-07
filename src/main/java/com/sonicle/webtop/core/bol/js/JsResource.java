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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceBase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author malbinola
 */
public class JsResource {
	public String id;
	public String name;
	public Boolean available;
	public String type;
	public String displayName;
	public String email;
	//public Integer capacity;
	//public String location;
	public String managerSid;
	public ArrayList<AclSubject> allowedSids;
	
	public JsResource(Resource item) {
		this.id = item.getResourceId();
		this.name = item.getResourceId();
		this.available = item.getEnabled();
		this.type = EnumUtils.toSerializedName(item.getType());
		this.displayName = item.getDisplayName();
		this.email = item.getEmail();
		//this.capacity = item.getCapacity();
		//this.location = item.getLocation();
		this.managerSid = item.getManagerSubject();
		this.allowedSids = new ArrayList<>();
		for (String sid : item.getAllowedSubjects()) {
			this.allowedSids.add(new AclSubject(sid));
		}
	}
	
	public static ResourceBase createResourceForAdd(JsResource js) {
		ResourceBase item = new ResourceBase();
		item.setEnabled(js.available);
		item.setType(EnumUtils.forSerializedName(js.type, ResourceBase.Type.class));
		item.setDisplayName(js.displayName);
		item.setEmail(js.email);
		//item.setCapacity(js.capacity);
		//item.setLocation(js.location);
		item.setManagerSubject(js.managerSid);
		Set<String> allowedUids = new HashSet<>();
		for (AclSubject rs : js.allowedSids) {
			allowedUids.add(rs.sid);
		}
		item.setAllowedSubjects(allowedUids);
		return item;
	}
	
	public static ResourceBase createResourceForUpdate(JsResource js) {
		ResourceBase item = new ResourceBase();
		item.setEnabled(js.available);
		item.setType(EnumUtils.forSerializedName(js.type, ResourceBase.Type.class));
		item.setDisplayName(js.displayName);
		item.setEmail(js.email);
		//item.setCapacity(js.capacity);
		//item.setLocation(js.location);
		item.setManagerSubject(js.managerSid);
		Set<String> allowedUids = new HashSet<>();
		for (AclSubject rs : js.allowedSids) {
			allowedUids.add(rs.sid);
		}
		item.setAllowedSubjects(allowedUids);
		return item;
	}
	
	public static class AclSubject {
		public String sid;
		
		public AclSubject(String sid) {
			this.sid = sid;
		}
	}
}
