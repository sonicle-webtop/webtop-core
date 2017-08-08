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
import static com.sonicle.webtop.core.CoreSettings.*;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.xmpp.PresenceStatus;

/**
 *
 * @author malbinola
 */
public class CoreUserSettings extends BaseUserSettings {
	private final CoreServiceSettings ss;
	
	public CoreUserSettings(UserProfileId profileId) {
		super(CoreManifest.ID, profileId);
		ss = new CoreServiceSettings(CoreManifest.ID, profileId.getDomainId());
	}

	public CoreUserSettings(String serviceId, UserProfileId profileId) {
		super(serviceId, profileId);
		ss = new CoreServiceSettings(CoreManifest.ID, profileId.getDomainId());
	}
	
	public String getTheme() {
		String value = getString(THEME, null);
		if(value != null) return value;
		return ss.getDefaultTheme();
	}
	
	public boolean setTheme(String value) {
		return setString(THEME, value);
	}
	
	public String getLayout() {
		String value = getString(LAYOUT, null);
		if(value != null) return value;
		return ss.getDefaultLayout();
	}
	
	public boolean setLayout(String value) {
		return setString(LAYOUT, value);
	}
	
	public String getLookAndFeel() {
		String value = getString(LAF, null);
		if(value != null) return value;
		return ss.getDefaultLaf();
	}
	
	public boolean setLookAndFeel(String value) {
		return setString(LAF, value);
	}
	
	public boolean getRightToLeft() {
		Boolean value = getBoolean(RTL, null);
		if(value != null) return value;
		return ss.getDefaultRtl();
	}
	
	public String getDesktopNotification() {
		String value = getString(DESKTOP_NOTIFICATION, null);
		if(value != null) return value;
		return ss.getDefaultDesktopNotification();
	}
	
	public boolean setDesktopNotification(String value) {
		return setString(DESKTOP_NOTIFICATION, value);
	}
	
	public String getLanguageTag() {
		String value = getString(LANGUAGE_TAG, null);
		if(value != null) return value;
		return ss.getDefaultLanguageTag();
	}
	
	public boolean setLanguageTag(String value) {
		return setString(LANGUAGE_TAG, value);
	}
	
	public String getTimezone() {
		String value = getString(TIMEZONE, null);
		if(value != null) return value;
		return ss.getDefaultTimezone();
	}
	
	public boolean setTimezone(String value) {
		return setString(TIMEZONE, value);
	}
	
	public Integer getStartDay() {
		Integer value = getInteger(START_DAY, null);
		if(value != null) return value;
		return ss.getDefaultStartDay();
	}
	
	public boolean setStartDay(Integer value) {
		return setInteger(START_DAY, value);
	}
	
	public String getShortDateFormat() {
		String value = getString(SHORT_DATE_FORMAT, null);
		if(value != null) return value;
		return ss.getDefaultShortDateFormat();
	}
	
	public boolean setShortDateFormat(String value) {
		return setString(SHORT_DATE_FORMAT, value);
	}
	
	public String getLongDateFormat() {
		String value = getString(LONG_DATE_FORMAT, null);
		if(value != null) return value;
		return ss.getDefaultLongDateFormat();
	}
	
	public boolean setLongDateFormat(String value) {
		return setString(LONG_DATE_FORMAT, value);
	}
	
	public String getShortTimeFormat() {
		String value = getString(SHORT_TIME_FORMAT, null);
		if(value != null) return value;
		return ss.getDefaultShortTimeFormat();
	}
	
	public boolean setShortTimeFormat(String value) {
		return setString(SHORT_TIME_FORMAT, value);
	}
	
	public String getLongTimeFormat() {
		String value = getString(LONG_TIME_FORMAT, null);
		if(value != null) return value;
		return ss.getDefaultLongTimeFormat();
	}
	
	public boolean setLongTimeFormat(String value) {
		return setString(LONG_TIME_FORMAT, value);
	}
	
	public Integer getViewportToolWidth() {
		return LangUtils.value(getSetting(VIEWPORT_TOOL_WIDTH), Integer.class);
	}
	
	public boolean setViewportToolWidth(Integer value) {
		return setInteger(VIEWPORT_TOOL_WIDTH, value);
	}
	
	public boolean getWhatsnewNeeded() {
		return getBoolean(WHATSNEW_NEEDED, true);
	}
	
	public boolean getOTPEnabled() {
		return getBoolean(OTP_ENABLED, false);
	}
	
	public boolean setOTPEnabled(boolean value) {
		return setBoolean(OTP_ENABLED, value);
	}
	
	public String getOTPDelivery() {
		return getString(OTP_DELIVERY, null);
	}
	
	public boolean setOTPDelivery(String value) {
		return setString(OTP_DELIVERY, value);
	}
	
	public String getOTPSecret() {
		return getString(OTP_SECRET, null);
	}
	
	public boolean setOTPSecret(String value) {
		return setString(OTP_SECRET, value);
	}
	
	public String getOTPEmailAddress() {
		return getString(OTP_EMAILADDRESS, null);
	}
	
	public boolean setOTPEmailAddress(String value) {
		return setString(OTP_EMAILADDRESS, value);
	}
	
	public boolean getDevicesSyncAlertEnabled() {
		Boolean value = getBoolean(DEVICES_SYNC_ALERT_ENABLED, null);
		return (value != null) ? value : ss.getDefaultDevicesSyncAlertEnabled();
	}
	
	public boolean setDevicesSyncAlertEnabled(Boolean value) {
		return setBoolean(DEVICES_SYNC_ALERT_ENABLED, value);
	}
	
	public int getDevicesSyncAlertTolerance() {
		return getInteger(DEVICES_SYNC_ALERT_TOLERANCE, 7);
	}
	
	public boolean setDevicesSyncAlertTolerance(int value) {
		return setInteger(DEVICES_SYNC_ALERT_TOLERANCE, value);
	}
	
	public String getIMStatusMessage() {
		return getString(IM_STATUS_MESSAGE, null);
	}
	
	public boolean setIMStatusMessage(String value) {
		return setString(IM_STATUS_MESSAGE, value);
	}
	
	public PresenceStatus getIMPresenceStatus() {
		String value = getString(IM_PRESENCE_STATUS, null);
		return EnumUtils.forSerializedName(value, PresenceStatus.ONLINE, PresenceStatus.class);
	}
	
	public boolean setIMPresenceStatus(PresenceStatus value) {
		if (value == null) return false;
		return setString(IM_PRESENCE_STATUS, EnumUtils.toSerializedName(value));
	}
	
	public Integer getIMUploadMaxFileSize() {
		Integer value = getInteger(IM_UPLOAD_MAXFILESIZE, null);
		if (value != null) return value;
		return ss.getIMUploadMaxFileSize();
	}
	
	public boolean setIMUploadMaxFileSize(Integer value) {
		return setInteger(IM_UPLOAD_MAXFILESIZE, value);
	}
	
	public boolean getIMSoundOnFriendConnect() {
		return getBoolean(IM_SOUND_ON_FRIEND_CONNECT, true);
	}
	
	public boolean setIMSoundOnFriendConnect(boolean value) {
		return setBoolean(IM_SOUND_ON_FRIEND_CONNECT, value);
	}
	
	public boolean getIMSoundOnFriendDisconnect() {
		return getBoolean(IM_SOUND_ON_FRIEND_DISCONNECT, false);
	}
	
	public boolean setIMSoundOnFriendDisconnect(boolean value) {
		return setBoolean(IM_SOUND_ON_FRIEND_DISCONNECT, value);
	}
	
	public boolean getIMSoundOnMessageReceived() {
		return getBoolean(IM_SOUND_ON_MESSAGE_RECEIVED, true);
	}
	
	public boolean setIMSoundOnMessageReceived(boolean value) {
		return setBoolean(IM_SOUND_ON_MESSAGE_RECEIVED, value);
	}
	
	public boolean getIMSoundOnMessageSent() {
		return getBoolean(IM_SOUND_ON_MESSAGE_SENT, false);
	}
	
	public boolean setIMSoundOnMessageSent(boolean value) {
		return setBoolean(IM_SOUND_ON_MESSAGE_SENT, value);
	}
	
	public static String getWhatsnewVersion(SettingsManager setm, UserProfileId profileId, String serviceId) {
		return setm.getUserSetting(profileId, serviceId, WHATSNEW_VERSION);
	}
	
	public static boolean setWhatsnewVersion(SettingsManager setm, UserProfileId profileId, String serviceId, String value) {
		return setm.setUserSetting(profileId, serviceId, WHATSNEW_VERSION, value);
	}
}
