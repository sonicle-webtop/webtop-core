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
package com.sonicle.webtop.core.userinfo.provider;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.bol.OUserInfo;
import com.sonicle.webtop.core.dal.UserInfoDAO;
import com.sonicle.webtop.core.sdk.interfaces.IConnectionProvider;
import com.sonicle.webtop.core.userinfo.UserInfoProviderBase;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingReader;
import java.sql.Connection;

/**
 *
 * @author malbinola
 */
public class WebTopUserInfoProvider extends UserInfoProviderBase {
	
	public WebTopUserInfoProvider(IConnectionProvider connectionManager, IServiceSettingReader settingsManager) {
		super(connectionManager, settingsManager);
	}
	
	@Override
	public boolean canWrite() {
		return true;
	}
	
	@Override
	public boolean addUser(String domainId, String userId) {
		Connection con = null;
		
		try {
			con = conp.getConnection();
			UserInfoDAO uidao = UserInfoDAO.getInstance();
			uidao.insert(con, domainId, userId);
			return true;

		} catch (Exception ex) {
			logger.error("Error adding user", ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public boolean deleteUser(String domainId, String userId) {
		Connection con = null;
		
		try {
			con = conp.getConnection();
			UserInfoDAO uidao = UserInfoDAO.getInstance();
			uidao.deleteByDomainUser(con, domainId, userId);
			return true;

		} catch (Exception ex) {
			logger.error("Error deleting user", ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	@Override
	public UserPersonalInfo getInfo(String domainId, String userId) {
		Connection con = null;
		
		try {
			con = conp.getConnection();
			UserInfoDAO uidao = UserInfoDAO.getInstance();
			return toUserPersonalInfo(uidao.selectByDomainUser(con, domainId, userId));

		} catch (Exception ex) {
			logger.error("Error reading user info", ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	@Override
	public boolean setInfo(String domainId, String userId, UserPersonalInfo info) {
		Connection con = null;
		
		try {
			con = conp.getConnection();
			UserInfoDAO uidao = UserInfoDAO.getInstance();
			return (uidao.update(con, fromUserPersonalInfo(domainId, userId, info)) > 0);

		} catch (Exception ex) {
			logger.error("Error writing user info", ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private UserPersonalInfo toUserPersonalInfo(OUserInfo info) {
		if(info == null) return null;
		UserPersonalInfo upi = new UserPersonalInfo();
		upi.setTitle(info.getTitle());
		upi.setFirstName(info.getFirstName());
		upi.setLastName(info.getLastName());
		upi.setNickname(info.getNickname());
		upi.setGender(info.getGender());
		upi.setEmail(info.getEmail());
		upi.setTelephone(info.getTelephone());
		upi.setFax(info.getFax());
		upi.setPager(info.getPager());
		upi.setMobile(info.getMobile());
		upi.setAddress(info.getAddress());
		upi.setCity(info.getCity());
		upi.setPostalCode(info.getPostalCode());
		upi.setState(info.getState());
		upi.setCountry(info.getCountry());
		upi.setCompany(info.getCompany());
		upi.setFunction(info.getFunction());
		upi.setCustom01(info.getCustom1());
		upi.setCustom02(info.getCustom2());
		upi.setCustom03(info.getCustom3());
		return upi;
	}
	
	private OUserInfo fromUserPersonalInfo(String domainId, String userId, UserPersonalInfo info) {
		if(info == null) return null;
		OUserInfo ui = new OUserInfo();
		ui.setDomainId(domainId);
		ui.setUserId(userId);
		ui.setTitle(info.getTitle());
		ui.setFirstName(info.getFirstName());
		ui.setLastName(info.getLastName());
		ui.setNickname(info.getNickname());
		ui.setGender(info.getGender());
		ui.setEmail(info.getEmail());
		ui.setTelephone(info.getTelephone());
		ui.setFax(info.getFax());
		ui.setPager(info.getPager());
		ui.setMobile(info.getMobile());
		ui.setAddress(info.getAddress());
		ui.setCity(info.getCity());
		ui.setPostalCode(info.getPostalCode());
		ui.setState(info.getState());
		ui.setCountry(info.getCountry());
		ui.setCompany(info.getCompany());
		ui.setFunction(info.getFunction());
		ui.setCustom1(info.getCustom01());
		ui.setCustom2(info.getCustom02());
		ui.setCustom3(info.getCustom03());
		return ui;
	}
}