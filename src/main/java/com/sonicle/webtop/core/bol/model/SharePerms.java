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

/**
 *
 * @author malbinola
 */
public abstract class SharePerms {
	public static final int CREATE = 1;
	public static final int READ = 2;
	public static final int UPDATE = 4;
	public static final int DELETE = 8;
	public static final int MANAGE = 16;
	protected int mask;
	
	public SharePerms(String... actions) {
		parse(actions);
	}
	
	public SharePerms(String[] actions, boolean[] bools) {
		parse(actions, bools);
	}
	
	abstract protected void parse(String... actions);
	abstract protected void parse(String[] actions, boolean[] bools);
	
	public void merge(SharePerms permission) {
		mask |= permission.mask;
	}
	
	public boolean implies(SharePerms permission) {
		if ((mask & permission.mask) != permission.mask)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if((mask & CREATE) == CREATE) sb.append("c");
		if((mask & READ) == READ) sb.append("r");
		if((mask & UPDATE) == UPDATE) sb.append("u");
		if((mask & DELETE) == DELETE) sb.append("d");
		if((mask & MANAGE) == MANAGE) sb.append("m");
		return sb.toString();
	}
}
