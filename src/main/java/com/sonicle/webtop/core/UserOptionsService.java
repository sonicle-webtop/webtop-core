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
package com.sonicle.webtop.core;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.OTPManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsUserOptions;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.userinfo.UserInfoProviderBase;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class UserOptionsService extends BaseUserOptionsService {
	public static final Logger logger = WT.getLogger(UserOptionsService.class);
	
	@Override
	public void processUserOptions(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String payload) {
		Connection con = null;
		CoreManager core = WT.getCoreManager();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			
			UserInfoProviderBase provider = core.getUserInfoProvider();
			CoreServiceSettings ss = new CoreServiceSettings(CoreManifest.ID, getTargetDomainId());
			CoreUserSettings us = new CoreUserSettings(getTargetProfileId());
			
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			OUser user = udao.selectByDomainUser(con, getTargetDomainId(), getTargetUserId());
			if(user == null) throw new WTException("Unable to find a user [{0}, {1}]", getTargetDomainId(), getTargetUserId());
			
			UserPersonalInfo upi = provider.getInfo(getTargetDomainId(), getTargetUserId());
				
			if(crud.equals(Crud.READ)) {
				JsUserOptions jso = new JsUserOptions(getTargetProfileId().toString());
				
				// main
				jso.displayName = user.getDisplayName();
				jso.theme = us.getTheme();
				jso.layout = us.getLayout();
				jso.laf = us.getLookAndFeel();
				jso.desktopNotification = us.getDesktopNotification();
				
				// i18n
				jso.language = user.getLanguageTag();
				jso.timezone = user.getTimezone();
				jso.startDay = us.getStartDay();
				jso.shortDateFormat = us.getShortDateFormat();
				jso.longDateFormat = us.getLongDateFormat();
				jso.shortTimeFormat = us.getShortTimeFormat();
				jso.longTimeFormat = us.getLongTimeFormat();
				
				// profileInfo
				jso.canManageUpi = RunContext.isPermitted(getSessionProfile().getId(), CoreManifest.ID, "USER_PROFILE_INFO", "MANAGE");
				jso.upiTitle = upi.getTitle();
				jso.upiFirstName = upi.getFirstName();
				jso.upiLastName = upi.getLastName();
				jso.upiNickname = upi.getNickname();
				jso.upiGender = upi.getGender();
				jso.upiEmail = upi.getEmail();
				jso.upiTelephone = upi.getTelephone();
				jso.upiFax = upi.getFax();
				jso.upiPager = upi.getPager();
				jso.upiMobile = upi.getMobile();
				jso.upiAddress = upi.getAddress();
				jso.upiCity = upi.getCity();
				jso.upiPostalCode = upi.getPostalCode();
				jso.upiState = upi.getState();
				jso.upiCountry = upi.getCountry();
				jso.upiCompany = upi.getCompany();
				jso.upiFunction = upi.getFunction();
				jso.upiCustom1 = upi.getCustom1();
				jso.upiCustom2 = upi.getCustom2();
				jso.upiCustom3 = upi.getCustom3();
				
				// OTP
				OTPManager otpm = core.getOTPManager();
				jso.otpEnabled = otpm.isEnabled(getTargetProfileId());
				jso.otpDelivery = otpm.getDeliveryMode(getTargetProfileId());
				jso.otpEmailAddress = otpm.getEmailAddress(getTargetProfileId());
				
				boolean isTrusted = false;
				String trustedOn = null;
				TrustedDeviceCookie tdc = otpm.readTrustedDeviceCookie(getTargetProfileId(), request);
				if(otpm.isThisDeviceTrusted(getTargetProfileId(), tdc)) {
					JsTrustedDevice td = otpm.getTrustedDevice(getTargetProfileId(), tdc.deviceId);
					if(td != null) {
						isTrusted = true;
						trustedOn = td.getISOTimestamp();
					}
				}
				
				jso.otpDeviceIsTrusted = isTrusted;
				jso.otpDeviceTrustedOn = trustedOn;
				
				// Sync
				jso.canSyncDevices = RunContext.isPermitted(getTargetProfileId(), CoreManifest.ID, "DEVICES_SYNC");
				jso.syncAlertEnabled = us.getDevicesSyncAlertEnabled();
				jso.syncAlertTolerance = us.getDevicesSyncAlertTolerance();
				
				new JsonResult(jso).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsUserOptions> pl = ServletUtils.getPayload(payload, JsUserOptions.class);
				
				// main
				if(pl.map.has("displayName")) user.setDisplayName(pl.data.displayName);
				if(pl.map.has("theme")) us.setTheme(pl.data.theme);
				if(pl.map.has("layout")) us.setLayout(pl.data.layout);
				if(pl.map.has("laf")) us.setLookAndFeel(pl.data.laf);
				if(pl.map.has("desktopNotification")) us.setDesktopNotification(pl.data.desktopNotification);
				
				// i18n
				if(pl.map.has("language")) user.setLanguageTag(pl.data.language);
				if(pl.map.has("timezone")) user.setTimezone(pl.data.timezone);
				if(pl.map.has("startDay")) us.setStartDay(pl.data.startDay);
				if(pl.map.has("shortDateFormat")) us.setShortDateFormat(pl.data.shortDateFormat);
				if(pl.map.has("longDateFormat")) us.setLongDateFormat(pl.data.longDateFormat);
				if(pl.map.has("shortTimeFormat")) us.setShortTimeFormat(pl.data.shortTimeFormat);
				if(pl.map.has("longTimeFormat")) us.setLongTimeFormat(pl.data.longTimeFormat);
				
				// User personal info
				if(provider.canWrite()) {
					if(RunContext.isPermitted(getSessionProfile().getId(), CoreManifest.ID, "USER_PROFILE_INFO", ServicePermission.ACTION_MANAGE)) {
						if(pl.map.has("upiTitle")) upi.setTitle(pl.data.upiTitle);
						if(pl.map.has("upiFirstName")) upi.setFirstName(pl.data.upiFirstName);
						if(pl.map.has("upiLastName")) upi.setLastName(pl.data.upiLastName);
						if(pl.map.has("upiNickname")) upi.setNickname(pl.data.upiNickname);
						if(pl.map.has("upiGender")) upi.setGender(pl.data.upiGender);
						if(pl.map.has("upiEmail")) upi.setEmail(pl.data.upiEmail);
						if(pl.map.has("upiTelephone")) upi.setTelephone(pl.data.upiTelephone);
						if(pl.map.has("upiFax")) upi.setFax(pl.data.upiFax);
						if(pl.map.has("upiPager")) upi.setPager(pl.data.upiPager);
						if(pl.map.has("upiMobile")) upi.setMobile(pl.data.upiMobile);
						if(pl.map.has("upiAddress")) upi.setAddress(pl.data.upiAddress);
						if(pl.map.has("upiCity")) upi.setCity(pl.data.upiCity);
						if(pl.map.has("upiPostalCode")) upi.setPostalCode(pl.data.upiPostalCode);
						if(pl.map.has("upiState")) upi.setState(pl.data.upiState);
						if(pl.map.has("upiCountry")) upi.setCountry(pl.data.upiCountry);
						if(pl.map.has("upiCompany")) upi.setCompany(pl.data.upiCompany);
						if(pl.map.has("upiFunction")) upi.setFunction(pl.data.upiFunction);
						if(pl.map.has("upiCustom1")) upi.setCustom1(pl.data.upiCustom1);
						if(pl.map.has("upiCustom2")) upi.setCustom2(pl.data.upiCustom2);
						if(pl.map.has("upiCustom3")) upi.setCustom3(pl.data.upiCustom3);
						provider.setInfo(getTargetDomainId(), getTargetUserId(), upi);
					}
				}
				
				// sync
				if(pl.map.has("syncAlertEnabled")) us.setDevicesSyncAlertEnabled(pl.data.syncAlertEnabled);
				if(pl.map.has("syncAlertTolerance")) us.setDevicesSyncAlertTolerance(pl.data.syncAlertTolerance);
				
				udao.update(con, user);
				
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error executing action UserOptions", ex);
			new JsonResult(false, "Error").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
/*	public void processDeactivateOTP(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager core = WT.getCoreManager(getRunContext());
		
		try {
			OTPManager otpm = core.getOTPManager();
			otpm.deactivate(getTargetProfileId());
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action DeactivateOTP", ex);
			new JsonResult(false).printTo(out);
		}
	}*/
}
