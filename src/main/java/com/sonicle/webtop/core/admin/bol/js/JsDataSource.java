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

import com.sonicle.commons.LangUtils;
import com.sonicle.security.PasswordUtils;
import com.sonicle.webtop.core.model.DataSource;
import com.sonicle.webtop.core.model.DataSourceBase;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malbinola
 */
public class JsDataSource {
	public String id;
	public String name;
	public String description;
	public String type;
	public String serverName;
	public String serverPort;
	public String databaseName;
	public String username;
	public String password;
	public String rpassword;
	public String hpassword;
	public String driverProps;
	public String poolProps;
	
	public JsDataSource(DataSource dataSource) {
		this.id = dataSource.getDataSourceId();
		this.name = dataSource.getName();
		this.description = dataSource.getDescription();
		this.type = dataSource.getType();
		this.serverName = dataSource.getServerName();
		this.serverPort = dataSource.getServerPort() != null ? String.valueOf(dataSource.getServerPort()) : null;
		this.databaseName = dataSource.getDatabaseName();
		this.username = dataSource.getUsername();
		String[] redacted = PasswordUtils.redact(dataSource.getPassword());
		this.password = redacted[0];
		this.rpassword = redacted[0];
		this.hpassword = redacted[1];
		this.driverProps = dataSource.getDriverPropsAsString();
		this.poolProps = dataSource.getPoolPropsAsString();
	}
	
	public static DataSourceBase createDataSourceForAdd(JsDataSource js) {
		DataSourceBase item = new DataSourceBase();
		item.setName(js.name);
		item.setDescription(js.description);
		item.setType(js.type);
		item.setServerName(js.serverName);
		item.setServerPort(LangUtils.value(js.serverPort, (Integer)null));
		item.setDatabaseName(js.databaseName);
		item.setUsername(js.username);
		item.setPassword(js.password);
		item.setDriverProps(LangUtils.parseStringAsKeyValueMap(js.driverProps));
		item.setPoolProps(LangUtils.parseStringAsKeyValueMap(js.poolProps));
		return item;
	}
	
	public static DataSourceBase createDataSourceForUpdate(JsDataSource js, UpdateReturn ret) {
		DataSourceBase item = new DataSourceBase();
		item.setName(js.name);
		item.setDescription(js.description);
		item.setType(js.type);
		item.setServerName(js.serverName);
		item.setServerPort(LangUtils.value(js.serverPort, (Integer)null));
		item.setDatabaseName(js.databaseName);
		item.setUsername(js.username);
		if (StringUtils.isBlank(js.rpassword) || (!StringUtils.equals(js.password, js.rpassword) && !StringUtils.equals(PasswordUtils.redact(js.password)[1], js.hpassword))) {
			ret.passwordChanged = true;
			item.setPassword(js.password);
		} else {
			item.setPassword(null);
		}
		item.setDriverProps(LangUtils.parseStringAsKeyValueMap(js.driverProps));
		item.setPoolProps(LangUtils.parseStringAsKeyValueMap(js.poolProps));
		return item;
	}
	
	public static class UpdateReturn {
		public boolean passwordChanged = false;
	}
}
