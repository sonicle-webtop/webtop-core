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
package com.sonicle.webtop.core.dal;

import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.bol.OGroupRole;
import static com.sonicle.webtop.core.jooq.Tables.*;
import com.sonicle.webtop.core.jooq.tables.records.GroupsRolesRecord;
import java.sql.Connection;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 *
 * @author gbulfon
 */
public class GroupRoleDAO {
	private final static GroupRoleDAO INSTANCE = new GroupRoleDAO();
	public static GroupRoleDAO getInstance() {
		return INSTANCE;
	}
	
	public List<OGroupRole> selectByGroupId(Connection con, String domainId, String groupId) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		return dsl
			.select()
			.from(GROUPS_ROLES)
			.where(GROUPS_ROLES.DOMAIN_ID.in(domainId,"*")
					.and(GROUPS_ROLES.GROUP_ID.equal(groupId))				
			)
			.fetchInto(OGroupRole.class);
	}
	
	public int insert(Connection con, OGroupRole item) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		GroupsRolesRecord record = dsl.newRecord(GROUPS_ROLES, item);
		return dsl
			.insertInto(GROUPS_ROLES)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OGroupRole item) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		GroupsRolesRecord record = dsl.newRecord(GROUPS_ROLES, item);
		return dsl
			.update(GROUPS_ROLES)
			.set(record)
			.where(GROUPS_ROLES.DOMAIN_ID.equal(item.getDomainId())
					.and(GROUPS_ROLES.GROUP_ID.equal(item.getGroupId()))
						.and(GROUPS_ROLES.ROLE_ID.equal(item.getRoleId()))
			)
			.execute();
	}
	
	public int delete(Connection con, String domainId, String groupId, String roleId) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		return dsl
			.delete(GROUPS_ROLES)
			.where(GROUPS_ROLES.DOMAIN_ID.equal(domainId)
					.and(GROUPS_ROLES.GROUP_ID.equal(groupId))
						.and(GROUPS_ROLES.ROLE_ID.equal(roleId))
			)
			.execute();
	}
}
