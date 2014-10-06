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
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUserSetting;
import java.sql.Connection;
import org.jooq.DSLContext;
import static com.sonicle.webtop.core.jooq.Tables.*;
import com.sonicle.webtop.core.jooq.tables.records.*;
import java.util.List;
import org.jooq.impl.DSL;

/**
 *
 * @author malbinola
 */
public class UserSettingDAO {
	
	private final static UserSettingDAO INSTANCE = new UserSettingDAO();
	public static UserSettingDAO getInstance() {
		return INSTANCE;
	}
	
	public List<OUserSetting> selectByDomainUserService(Connection con, String domainId, String userId, String serviceId) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		return dsl
			.select()
			.from(SETTINGS_USER)
			.where(SETTINGS_USER.DOMAIN_ID.equal(domainId)
				.and(SETTINGS_USER.USER_ID.equal(userId))
				.and(SETTINGS_USER.SERVICE_ID.equal(serviceId))
			)
			.orderBy(
				SETTINGS_USER.KEY
			)
			.fetchInto(OUserSetting.class);
	}
	
	public List<OUserSetting> selectByDomainServiceUserKeyLike(Connection con, String domainId, String userId, String serviceId, String keyLike) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		return dsl
			.select()
			.from(SETTINGS_USER)
			.where(SETTINGS_USER.DOMAIN_ID.equal(domainId)
				.and(SETTINGS_USER.USER_ID.equal(userId))
				.and(SETTINGS_USER.SERVICE_ID.equal(serviceId))
				.and(SETTINGS_USER.KEY.like(keyLike))
			)
			.orderBy(
				SETTINGS_USER.KEY
			)
			.fetchInto(OUserSetting.class);
	}
	
	public OUserSetting selectByDomainUserServiceKey(Connection con, String domainId, String userId, String serviceId, String key) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		return dsl
			.select()
			.from(SETTINGS_USER)
			.where(SETTINGS_USER.DOMAIN_ID.equal(domainId)
				.and(SETTINGS_USER.USER_ID.equal(userId))
				.and(SETTINGS_USER.SERVICE_ID.equal(serviceId))
				.and(SETTINGS_USER.KEY.equal(key))
			)
			.fetchOneInto(OUserSetting.class);
	}
	
	public int insert(Connection con, OUserSetting item) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		SettingsUserRecord record = dsl.newRecord(SETTINGS_USER, item);
		return dsl
			.insertInto(SETTINGS_USER)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OUserSetting item) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		SettingsUserRecord record = dsl.newRecord(SETTINGS_USER, item);
		return dsl
			.update(SETTINGS_USER)
			.set(record)
			.where(SETTINGS_USER.DOMAIN_ID.equal(item.getDomainId())
				.and(SETTINGS_USER.USER_ID.equal(item.getUserId()))
				.and(SETTINGS_USER.SERVICE_ID.equal(item.getServiceId()))
				.and(SETTINGS_USER.KEY.equal(item.getKey()))
			)
			.execute();
	}
	
	public int deleteByDomainServiceUserKey(Connection con, String domainId, String userId, String serviceId, String key) {
		DSLContext dsl = DSL.using(con, WebTopApp.getSQLDialect());
		return dsl
			.delete(SETTINGS_USER)
			.where(SETTINGS_USER.DOMAIN_ID.equal(domainId)
				.and(SETTINGS_USER.USER_ID.equal(userId))
				.and(SETTINGS_USER.SERVICE_ID.equal(serviceId))
				.and(SETTINGS_USER.KEY.equal(key))
			)
			.execute();
	}
}
