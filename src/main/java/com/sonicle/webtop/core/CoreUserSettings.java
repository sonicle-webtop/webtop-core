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

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfile;

/**
 *
 * @author malbinola
 */
public class CoreUserSettings extends BaseUserSettings {
	
	public CoreUserSettings(String domainId, String userId) {
		super(domainId, userId, CoreManifest.ID);
	}
	
    public CoreUserSettings(String domainId, String userId, String serviceId) {
        super(domainId, userId, serviceId);
    }
	
	/**
	 * [string]
	 * Theme name
	 */
	public static final String THEME = "theme";
	public static final String DEFAULT_THEME = "crisp";
	
	/**
	 * [string]
	 * Layout
	 */
	public static final String LAYOUT = "layout";
	public static final String DEFAULT_LAYOUT = "default";
	
	/**
	 * [string]
	 * Look and feel
	 */
	public static final String LAF = "laf";
	public static final String DEFAULT_LAF = "default";
	
	/**
	 * [boolean]
	 * Right-to-left mode
	 */
	public static final String RTL = "rtl";
	public static final boolean DEFAULT_RTL = false;
	
	/**
	 * [int]
	 * Week start day (0:sunday, 1:monday)
	 */
	public static final String START_DAY = "i18n.startDay";
	public static final int DEFAULT_START_DAY = 1;
	
	/**
	 * [string]
	 * Short date format pattern
	 */
	public static final String SHORT_DATE_FORMAT = "i18n.format.date.short";
	
	/**
	 * [string]
	 * Long date format pattern
	 */
	public static final String LONG_DATE_FORMAT = "i18n.format.date.long";
	
	/**
	 * [string]
	 * Short time format pattern
	 */
	public static final String SHORT_TIME_FORMAT = "i18n.format.time.short";
	
	/**
	 * [string]
	 * Long time format pattern
	 */
	public static final String LONG_TIME_FORMAT = "i18n.format.time.long";
	
	/**
	 * [string][*]
	 * Saves width of tool component
	 */
	public static final String VIEWPORT_TOOL_WIDTH = "viewport.tool.width";
	
	/**
	 * [boolean]
	 * Specifies if whatsnew window must be shown after a service upgrade
	 */
	public static final String WHATSNEW_ENABLED = "whatsnew.enabled";
	
	/**
	 * [string][*]
	 * Saves last seen service version for whatsnew handling
	 */
	public static final String WHATSNEW_VERSION = "whatsnew.version";
	
	//public static final String PROFILEDATA_EDITABLE = "profiledata.editable";
	//public static final String OTP_ENABLED = "otp.enabled";
	/**
	 * [boolean]
	 * Specifies if TFA is required.
	 */
	public static final String TFA_MANDATORY = "tfa.mandatory";
	/**
	 * [string]
	 * Specifies delivery method. One of: NONE, EMAIL, GOOGLEAUTH.
	 */
	public static final String TFA_DELIVERY = "tfa.delivery";
	public static final String TFA_DELIVERY_EMAIL = "email";
	public static final String TFA_DELIVERY_GOOGLEAUTH = "googleauth";
	/**
	 * [string]
	 * Specifies generated secret string within googleauth delivery.
	 */
	public static final String TFA_SECRET = "tfa.secret";
	/**
	 * [string]
	 * Specifies choosen email address within email delivery.
	 */
	public static final String TFA_EMAILADDRESS = "tfa.emailaddress";
	public static final String TFA_TRUSTED_DEVICE = "tfa.trusteddevice";
	
	public String getTheme() {
		return getString(THEME, DEFAULT_THEME);
	}
	
	public boolean setTheme(String value) {
		return setString(THEME, value);
	}
	
	public String getLayout() {
		return getString(LAYOUT, DEFAULT_LAYOUT);
	}
	
	public boolean setLayout(String value) {
		return setString(LAYOUT, value);
	}
	
	public String getLookAndFeel() {
		return getString(LAF, DEFAULT_LAF);
	}
	
	public boolean setLookAndFeel(String value) {
		return setString(LAF, value);
	}
	
	public boolean getRightToLeft() {
		return getBoolean(RTL, DEFAULT_RTL);
	}
	
	public Integer getStartDay() {
		return getInteger(START_DAY, DEFAULT_START_DAY);
	}
	
	public boolean setStartDay(Integer value) {
		return setInteger(START_DAY, value);
	}
	
	public String getShortDateFormat() {
		return getString(SHORT_DATE_FORMAT, "MM/dd/yyyy");
	}
	
	public boolean setShortDateFormat(String value) {
		return setString(SHORT_DATE_FORMAT, value);
	}
	
	public String getLongDateFormat() {
		return getString(LONG_DATE_FORMAT, "MM/dd/yyyy");
	}
	
	public boolean setLongDateFormat(String value) {
		return setString(LONG_DATE_FORMAT, value);
	}
	
	public String getShortTimeFormat() {
		return getString(SHORT_TIME_FORMAT, "HH:mm");
	}
	
	public boolean setShortTimeFormat(String value) {
		return setString(SHORT_TIME_FORMAT, value);
	}
	
	public String getLongTimeFormat() {
		return getString(LONG_TIME_FORMAT, "HH:mm");
	}
	
	public boolean setLongTimeFormat(String value) {
		return setString(LONG_TIME_FORMAT, value);
	}
	
	public Integer getViewportToolWidth() {
		return LangUtils.value(getSetting(CoreUserSettings.VIEWPORT_TOOL_WIDTH), Integer.class);
	}
	
	public boolean setViewportToolWidth(Integer value) {
		return setInteger(CoreUserSettings.VIEWPORT_TOOL_WIDTH, value);
	}
	
	public boolean getWhatsnewEnabled() {
		return getBoolean(WHATSNEW_ENABLED, true);
	}
	
	public boolean getTFAMandatory() {
		return getBoolean(CoreUserSettings.TFA_MANDATORY, false);
	}
	
	public boolean setTFAMandatory(boolean value) {
		return setBoolean(CoreUserSettings.TFA_MANDATORY, value);
	}
	
	public String getTFADelivery() {
		return getString(CoreUserSettings.TFA_DELIVERY, null);
	}
	
	public boolean setTFADelivery(String value) {
		return setString(CoreUserSettings.TFA_DELIVERY, value);
	}
	
	public String getTFASecret() {
		return getString(CoreUserSettings.TFA_SECRET, null);
	}
	
	public boolean setTFASecret(String value) {
		return setString(CoreUserSettings.TFA_SECRET, value);
	}
	
	public String getTFAEmailAddress() {
		return getString(CoreUserSettings.TFA_EMAILADDRESS, null);
	}
	
	public boolean setTFAEmailAddress(String value) {
		return setString(CoreUserSettings.TFA_EMAILADDRESS, value);
	}
	
	
	
	
	public static String getWhatsnewVersion(SettingsManager setm, UserProfile profile, String serviceId) {
		return setm.getUserSetting(profile, serviceId, CoreUserSettings.WHATSNEW_VERSION);
	}
	
	public static boolean setWhatsnewVersion(SettingsManager setm, UserProfile profile, String serviceId, String value) {
		return setm.setUserSetting(profile, serviceId, CoreUserSettings.WHATSNEW_VERSION, value);
	}
	
	
}
