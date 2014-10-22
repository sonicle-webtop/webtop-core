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
package com.sonicle.webtop.core.userdata.provider;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.UserDataDAO;
import com.sonicle.webtop.core.interfaces.IConnectionProvider;
import com.sonicle.webtop.core.userdata.UserDataProviderBase;
import com.sonicle.webtop.core.sdk.UserData;
import com.sonicle.webtop.core.interfaces.IServiceSettingReader;
import java.sql.Connection;

/**
 *
 * @author malbinola
 */
public class WebTopUserDataProvider extends UserDataProviderBase {
	
	public WebTopUserDataProvider(IConnectionProvider connectionManager, IServiceSettingReader settingsManager) {
		super(connectionManager, settingsManager);
	}

	@Override
	public UserData getUserData(String domainId, String userId) {
		Connection con = null;
		
		try {
			con = connectionManager.getConnection();
			UserDataDAO uddao = UserDataDAO.getInstance();
			return toUserData(uddao.selectByDomainUser(con, domainId, userId));

		} catch (Exception ex) {
			logger.error("Error reading user data", ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	@Override
	public boolean setUserData(String domainId, String userId, UserData userData) {
		Connection con = null;
		
		try {
			con = connectionManager.getConnection();
			UserDataDAO uddao = UserDataDAO.getInstance();
			return (uddao.update(con, fromUserData(domainId, userId, userData)) > 0);

		} catch (Exception ex) {
			logger.error("Error writing user data", ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private UserData toUserData(OUser user) {
		if(user == null) return null;
		UserData ud = new UserData();
		ud.title = user.getTitle();
		ud.firstName = user.getFirstName();
		ud.lastName = user.getLastName();
		ud.address = user.getAddress();
		ud.postalCode = user.getPostalCode();
		ud.city = user.getCity();
		ud.state = user.getState();
		ud.country = user.getCountry();
		ud.email = user.getEmail();
		ud.mobile = user.getMobile();
		ud.telephone = user.getTelephone();
		ud.fax = user.getFax();
		ud.company = user.getCompany();
		ud.function = user.getFunction();
		ud.workEmail = user.getWorkEmail();
		ud.workMobile = user.getWorkMobile();
		ud.workTelephone = user.getWorkTelephone();
		ud.workFax = user.getWorkFax();
		ud.custom1 = user.getCustom_1();
		ud.custom2 = user.getCustom_2();
		ud.custom3 = user.getCustom_3();
		return ud;
	}
	
	private OUser fromUserData(String domainId, String userId, UserData userData) {
		if(userData == null) return null;
		OUser user = new OUser();
		user.setDomainId(domainId);
		user.setUserId(userId);
		user.setTitle(userData.title);
		user.setFirstName(userData.firstName);
		user.setLastName(userData.lastName);
		user.setAddress(userData.address);
		user.setPostalCode(userData.postalCode);
		user.setCity(userData.city);
		user.setState(userData.state);
		user.setCountry(userData.country);
		user.setEmail(userData.email);
		user.setMobile(userData.mobile);
		user.setTelephone(userData.telephone);
		user.setFax(userData.fax);
		user.setCompany(userData.company);
		user.setFunction(userData.function);
		user.setWorkEmail(userData.workEmail);
		user.setWorkMobile(userData.workMobile);
		user.setWorkTelephone(userData.workTelephone);
		user.setWorkFax(userData.workFax);
		user.setCustom_1(userData.custom1);
		user.setCustom_2(userData.custom2);
		user.setCustom_3(userData.custom3);
		return user;
	}
}