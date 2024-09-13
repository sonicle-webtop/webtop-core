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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.OTPManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserGetOption;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsDomainPwdPolicies;
import com.sonicle.webtop.core.bol.js.JsGridSync;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsUserOptions;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.app.model.UIPreset;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class UserOptionsService extends BaseUserOptionsService {
	public static final Logger LOGGER = WT.getLogger(UserOptionsService.class);
	
	@Override
	public void processUserOptions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			
			CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
			//CoreServiceSettings ss = new CoreServiceSettings(CoreManifest.ID, getTargetDomainId());
			CoreUserSettings us = new CoreUserSettings(getTargetProfileId());
			
			User user = coreMgr.getUser(BitFlags.noneOf(UserGetOption.class));
			if (user == null) throw new WTException("Unable to find a user [{0}, {1}]", getTargetDomainId(), getTargetUserId());
			UserProfile.PersonalInfo upi = coreMgr.getProfilePersonalInfo();
			
			if (crud.equals(Crud.READ)) {
				JsUserOptions jso = new JsUserOptions(getTargetProfileId().toString());
				jso.permPasswordManage = RunContext.isPermitted(true, getTargetProfileId(), CoreManifest.ID, "PASSWORD", "MANAGE");
				jso.permUpiManage = RunContext.isPermitted(true, getTargetProfileId(), CoreManifest.ID, "USER_PROFILE_INFO", "MANAGE");
				jso.permSyncDevicesAccess = RunContext.isPermitted(true, getTargetProfileId(), CoreManifest.ID, "DEVICES_SYNC");
				jso.permWebchatAccess = RunContext.isPermitted(true, getTargetProfileId(), CoreManifest.ID, "WEBCHAT");
				
				jso.dirCapPasswordWrite = false;
				jso.dirPasswordPolicies = null;
				try {
					AbstractDirectory dir = coreMgr.getAuthDirectory();
					jso.dirCapPasswordWrite = dir.hasCapability(DirectoryCapability.PASSWORD_WRITE);
					if (jso.dirCapPasswordWrite) {
						short levenThres = WebTopProps.getWTDirectorySimilarityLevenThres(WT.getProperties());
						short tokenSize = WebTopProps.getWTDirectorySimilarityTokenSize(WT.getProperties());
						jso.dirPasswordPolicies = LangUtils.serialize(new JsDomainPwdPolicies(levenThres, tokenSize, coreMgr.getDomainPasswordPolicies()), JsDomainPwdPolicies.class);
					}
				} catch(WTException ex) {
					LOGGER.error("Unable to get directory capabilities and policies", ex);
				}
				
				// main
				jso.displayName = user.getDisplayName();
				jso.layout = us.getUILayout();
				jso.ui = getUserUIPreset(us);
				//jso.theme = us.getTheme();
				//jso.laf = us.getLookAndFeel();
				jso.headerScale = EnumUtils.toSerializedName(us.getViewportHeaderScale());
				jso.passwordForceChange = us.getPasswordForceChange();
				jso.startupService = sanitizeStartupService(coreMgr, us.getStartupService());
				jso.desktopNotification = EnumUtils.toSerializedName(us.getDesktopNotification());
				
				// i18n
				jso.language = us.getLanguageTag();
				jso.timezone = us.getTimezone();
				jso.startDay = us.getStartDay();
				jso.shortDateFormat = us.getShortDateFormat();
				jso.longDateFormat = us.getLongDateFormat();
				jso.shortTimeFormat = us.getShortTimeFormat();
				jso.longTimeFormat = us.getLongTimeFormat();
				
				// profileInfo
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
				jso.upiCustom1 = upi.getCustom01();
				jso.upiCustom2 = upi.getCustom02();
				jso.upiCustom3 = upi.getCustom03();
				
				// OTP
				OTPManager otpm = coreMgr.getOTPManager();
				jso.otpEnabled = otpm.isEnabled(getTargetProfileId());
				jso.otpDelivery = EnumUtils.toSerializedName(otpm.getDeliveryMode(getTargetProfileId()));
				jso.otpEmailAddress = otpm.getEmailAddress(getTargetProfileId());
				
				boolean isTrusted = false;
				String trustedOn = null;
				TrustedDeviceCookie tdc = otpm.readTrustedDeviceCookie(getTargetProfileId(), request);
				if (otpm.isThisDeviceTrusted(getTargetProfileId(), tdc)) {
					JsTrustedDevice td = otpm.getTrustedDevice(getTargetProfileId(), tdc.deviceId);
					if (td != null) {
						isTrusted = true;
						trustedOn = td.getISOTimestamp();
					}
				}
				
				jso.otpDeviceIsTrusted = isTrusted;
				jso.otpDeviceTrustedOn = trustedOn;
				
				// Sync
				jso.syncAlertEnabled = us.getDevicesSyncAlertEnabled();
				jso.syncAlertTolerance = us.getDevicesSyncAlertTolerance();
				
				// PBX
				jso.pbxUsername = us.getPbxUsername();
				jso.pbxPassword = us.getPbxPassword();
				
				//SMS
				jso.smsSender = us.getSmsSender();
				
				// WebChat
				jso.imUploadMaxFileSize = us.getIMUploadMaxFileSize(true);
				jso.imSoundOnFriendConnect = us.getIMSoundOnFriendConnect();
				jso.imSoundOnFriendDisconnect = us.getIMSoundOnFriendDisconnect();
				jso.imSoundOnMessageReceived = us.getIMSoundOnMessageReceived();
				jso.imSoundOnMessageSent = us.getIMSoundOnMessageSent();
				
				new JsonResult(jso).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsUserOptions> pl = ServletUtils.getPayload(request, JsUserOptions.class);
				boolean upCacheNeedsUpdate = false;
				
				// main
				if (pl.map.has("displayName")) {
					upCacheNeedsUpdate = true;
					coreMgr.updateUserDisplayName(pl.data.displayName);
				}
				if (pl.map.has("layout")) us.setUILayout(pl.data.layout);
				if (pl.map.has("ui")) setUserUIPreset(us, pl.data.ui);
				//if (pl.map.has("theme")) us.setTheme(pl.data.theme);
				//if (pl.map.has("laf")) us.setLookAndFeel(pl.data.laf);
				if (pl.map.has("headerScale")) us.setViewportHeaderScale(pl.data.headerScale);
				if (pl.map.has("passwordForceChange")) us.setPasswordForceChange(pl.data.passwordForceChange);
				if (pl.map.has("startupService")) us.setStartupService(pl.data.startupService);
				if (pl.map.has("desktopNotification")) us.setDesktopNotification(pl.data.desktopNotification);
				
				// i18n
				if (pl.map.has("language")) {
					upCacheNeedsUpdate = true;
					us.setLanguageTag(pl.data.language);
				}
				if (pl.map.has("timezone")) {
					upCacheNeedsUpdate = true;
					us.setTimezone(pl.data.timezone);
				}
				if (pl.map.has("startDay")) us.setStartDay(pl.data.startDay);
				if (pl.map.has("shortDateFormat")) us.setShortDateFormat(pl.data.shortDateFormat);
				if (pl.map.has("longDateFormat")) us.setLongDateFormat(pl.data.longDateFormat);
				if (pl.map.has("shortTimeFormat")) us.setShortTimeFormat(pl.data.shortTimeFormat);
				if (pl.map.has("longTimeFormat")) us.setLongTimeFormat(pl.data.longTimeFormat);
				
				// User personal info
				if (RunContext.isWebTopAdmin() || RunContext.isPermitted(true, getTargetProfileId(), CoreManifest.ID, "USER_PROFILE_INFO", ServicePermission.ACTION_MANAGE)) {
					upCacheNeedsUpdate = true;
					if (pl.map.has("upiTitle")) upi.setTitle(pl.data.upiTitle);
					if (pl.map.has("upiFirstName")) upi.setFirstName(pl.data.upiFirstName);
					if (pl.map.has("upiLastName")) upi.setLastName(pl.data.upiLastName);
					if (pl.map.has("upiNickname")) upi.setNickname(pl.data.upiNickname);
					if (pl.map.has("upiGender")) upi.setGender(pl.data.upiGender);
					if (pl.map.has("upiEmail")) upi.setEmail(pl.data.upiEmail);
					if (pl.map.has("upiTelephone")) upi.setTelephone(pl.data.upiTelephone);
					if (pl.map.has("upiFax")) upi.setFax(pl.data.upiFax);
					if (pl.map.has("upiPager")) upi.setPager(pl.data.upiPager);
					if (pl.map.has("upiMobile")) upi.setMobile(pl.data.upiMobile);
					if (pl.map.has("upiAddress")) upi.setAddress(pl.data.upiAddress);
					if (pl.map.has("upiCity")) upi.setCity(pl.data.upiCity);
					if (pl.map.has("upiPostalCode")) upi.setPostalCode(pl.data.upiPostalCode);
					if (pl.map.has("upiState")) upi.setState(pl.data.upiState);
					if (pl.map.has("upiCountry")) upi.setCountry(pl.data.upiCountry);
					if (pl.map.has("upiCompany")) upi.setCompany(pl.data.upiCompany);
					if (pl.map.has("upiFunction")) upi.setFunction(pl.data.upiFunction);
					if (pl.map.has("upiCustom1")) upi.setCustom01(pl.data.upiCustom1);
					if (pl.map.has("upiCustom2")) upi.setCustom02(pl.data.upiCustom2);
					if (pl.map.has("upiCustom3")) upi.setCustom03(pl.data.upiCustom3);
					coreMgr.updateUserPersonalInfo(upi);
				}
				
				// sync
				if (pl.map.has("syncAlertEnabled")) us.setDevicesSyncAlertEnabled(pl.data.syncAlertEnabled);
				if (pl.map.has("syncAlertTolerance")) us.setDevicesSyncAlertTolerance(pl.data.syncAlertTolerance);
				
				// PBX
				if (pl.map.has("pbxUsername")) us.setPbxUsername(pl.data.pbxUsername);
				if (pl.map.has("pbxPassword")) us.setPbxPassword(pl.data.pbxPassword);
				
				// SMS
				if (pl.map.has("smsSender")) us.setSmsSender(pl.data.smsSender);
				
				// WebChat
				if (pl.map.has("imUploadMaxFileSize")) us.setIMUploadMaxFileSize(pl.data.imUploadMaxFileSize);
				if (pl.map.has("imSoundOnFriendConnect"))  us.setIMSoundOnFriendConnect(pl.data.imSoundOnFriendConnect);
				if (pl.map.has("imSoundOnFriendDisconnect"))  us.setIMSoundOnFriendDisconnect(pl.data.imSoundOnFriendDisconnect);
				if (pl.map.has("imSoundOnMessageReceived"))  us.setIMSoundOnMessageReceived(pl.data.imSoundOnMessageReceived);
				if (pl.map.has("imSoundOnMessageSent"))  us.setIMSoundOnMessageSent(pl.data.imSoundOnMessageSent);
				
				if (upCacheNeedsUpdate) coreMgr.cleanUserProfileCache();
				
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			LOGGER.error("Error executing action UserOptions", ex);
			new JsonResult(false, "Error").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String getUserUIPreset(CoreUserSettings us) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		String theme = us.getUITheme();
		String laf = us.getUILookAndFeel();
		for (UIPreset preset : core.listUIPresets().values()) {
			if (StringUtils.equals(preset.getTheme(), theme) && StringUtils.equals(preset.getLookAndFeel(), laf)) {
				return preset.getId();
			}
		}
		return null;
	}
	
	private void setUserUIPreset(CoreUserSettings us, String ui) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		UIPreset preset = core.listUIPresets().get(ui);
		if (preset != null) {
			us.setUITheme(preset.getTheme());
			us.setUILookAndFeel(preset.getLookAndFeel());
		}
	}
	
	public void processLookupStartupServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		Locale locale = WT.getUserData(getTargetProfileId()).getLocale();
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			for (String sid : core.listAllowedServices()) {
				if (sid.equals(CoreManifest.ID)) {
					items.add(new JsSimple(sid, "Home"));
				} else {
					items.add(new JsSimple(sid, WT.lookupResource(sid, locale, BaseService.RESOURCE_SERVICE_NAME)));
				}
			}
			new JsonResult(items).printTo(out);
			
		} catch(Exception ex) {
			LOGGER.error("Error in LookupStartupServices", ex);
		}
	}
	
	public void processManageSyncDevices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager(true, getTargetProfileId());
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				DateTimeFormatter fmt = JsGridSync.createFormatter(coreMgr.getUserData().getTimeZone());
				List<SyncDevice> devices = coreMgr.listZPushDevices();
				ArrayList<JsGridSync> items = new ArrayList<>();
				for (SyncDevice device : devices) {
					items.add(new JsGridSync(device.device, device.user, device.lastSync, fmt));
				}
				new JsonResult(items).printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsGridSync> pl = ServletUtils.getPayload(request, JsGridSync.class);
				CompositeId cid = new CompositeId().parse(pl.data.id);
				
				coreMgr.deleteZPushDevice(cid.getToken(0));
				new JsonResult().printTo(out);
				
			} else if (crud.equals("info")) {
				String scid = ServletUtils.getStringParameter(request, "cid", true);
				CompositeId cid = new CompositeId().parse(scid);
				
				String info = coreMgr.getZPushDetailedInfo(cid.getToken(0), "</br>");
				new JsonResult(info).printTo(out);
			}
			
		} catch (Throwable t) {
			LOGGER.error("Error in ManageSyncDevices", t);
			new JsonResult(false, "Error in ManageSyncDevices").printTo(out);
		}
	}
	
	/**
	 * Ensures that startup service refers to an allowed service
	 */
	private String sanitizeStartupService(CoreManager coreMgr, String startupService) {
		if (StringUtils.isBlank(startupService)) {
			return null;
		} else {
			return coreMgr.listAllowedServices().contains(startupService) ? startupService : null;
		}
	}
}
