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

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malbinola
 */
public class SharePermsFolder extends SharePerms {
	public static final String[] ACTIONS = new String[]{
		AuthResource.ACTION_READ,
		AuthResource.ACTION_UPDATE,
		AuthResource.ACTION_DELETE
	};
	
	public SharePermsFolder(String... actions) {
		super(actions);
	}
	
	public SharePermsFolder(String[] actions, boolean[] bools) {
		super(actions, bools);
	}
	
	@Override
	public void parse(String[] actions, boolean[] bools) {
		if(actions.length != bools.length) throw new IllegalArgumentException("Passed arrays must have same lenght");
		for(int i=0; i<actions.length; i++) {
			if(bools[i]) parse(actions[i]);
		}
	}
	
	@Override
	public void parse(String... actions) {
		for(String action : actions) {
			if(StringUtils.equalsIgnoreCase(action, "READ"))
				mask |= READ;
			else if(StringUtils.equalsIgnoreCase(action, "UPDATE"))
				mask |= UPDATE;
			else if(StringUtils.equalsIgnoreCase(action, "DELETE"))
				mask |= DELETE;
			else if(action.equals("*")) {
				mask |= READ;
				mask |= UPDATE;
				mask |= DELETE;
			}
				
			else throw new IllegalArgumentException("Invalid action " + action);
		}
	}
	
	public boolean implies(String... actions) {
		return implies(new SharePermsFolder(actions));
	}
	
	public static SharePermsFolder full() {
		return new SharePermsFolder("READ", "UPDATE", "DELETE");
	}
	
	/*
	public void merge(SharePermission permission) {
		if (!(permission instanceof FolderPermission)) throw new IllegalArgumentException("");
		FolderPermission p = (FolderPermission)permission;
		
		 
	}
	*/
}
