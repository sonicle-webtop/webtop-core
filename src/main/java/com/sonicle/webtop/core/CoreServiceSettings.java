/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sonicle.webtop.core;


import com.sonicle.commons.LangUtils;
import com.sonicle.security.otp.provider.SonicleAuth;
import com.sonicle.webtop.core.sdk.BaseServiceSettings;
import java.util.Locale;

/**
 * @author matteo
 */
public class CoreServiceSettings extends BaseServiceSettings {
	
	// NB: please do not provide a constructor that defaults serviceId 
	// because there are some keys that can be applied widely across services
	
	public CoreServiceSettings(String domainId, String serviceId) {
		super(domainId, serviceId);
	}
	
	public static final String TMP = "tmp";
	
	/**
	 * [string][system]
	 * Defines server path in which PHP is installed
	 */
	public static final String PHP_PATH = "php.path";
	
	/**
	 * [string][system]
	 * Defines server path in which ZPUSH is installed
	 */
	public static final String ZPUSH_PATH = "zpush.path";
	
	/**
	 * [string][system]
	 * Defines system temp path in which temporarly store files
	 */
	public static final String SYSTEM_PATH_TEMP = "system.path.temp";
	
	/**
	 * [string][system]
	 * Defines system public path in which store public services resources
	 */
	public static final String SYSTEM_PATH_PUBLIC = "system.path.public";
	
	/**
	 * [string][system]
	 * Defines system default language locale
	 */
	public static final String SYSTEM_LANGUAGE = "system.language";
	
	/**
	 * [string][system]
	 * Defines system default country locale
	 */
	public static final String SYSTEM_COUNTRY = "system.country";
	
	/**
	 * [boolean][system]
	 * Indicates whether to forcedly hide domain selection from login page
	 */
	public static final String LOGIN_HIDE_DOMAINS = "login.domains.hide";
	
	/**
	 * [boolean][system]
	 * Indicates whether to forcedly hide footprint details from login page
	 */
	public static final String LOGIN_HIDE_FOOTPRINT = "login.footprint.hide";
	
	/**
	 * [boolean][system][*]
	 * Indicates if system is under maintenance
	 */
	public static final String MAINTENANCE = "maintenance";
	
	/**
	 * [string][system][*]
	 * Stores installed service version
	 */
	public static final String MANIFEST_VERSION = "manifest.version";
	
	/**
	 * [string][system][*]
	 * Specifies the service public name to use typically in public URLs.
	 * If specified it overrides the generated one (see ServiceManager).
	 */
	public static final String PUBLIC_NAME = "public.name";
	
	/**
	 * [string][system+domain]
	 * Specifies if users can send feedbacks throught interface.
	 */
	public static final String FEEDBACK_ENABLED = "feedback.enabled";
	
	/**
	 * [string][system+domain]
	 * Specifies if what's new visualization to users is active
	 */
	public static final String WHATSNEW_ENABLED = "whatsnew.enabled";
	
	/**
	 * [string][system+domain][*]
	 * Overrides support email address
	 */
	//public static final String MANIFEST_SUPPORT_EMAIL = "manifest.support.email";
	/**
	 * [boolean][system]
	 */
	//public static final String DB_INIT_ENABLED = "db.init.enabled";
	/**
	 * [boolean][system]
	 */
	//public static final String DB_UPGRADE_ENABLED = "db.upgrade.enabled";
	
	/**
	 * [string][system]
	 */
	public static final String USERINFO_PROVIDER = "userinfo.provider";
	
	/**
	 * [boolean][system+domain]
	 * 2FA enables status on the platform
	 */
	public static final String OTP_ENABLED = "otp.enabled";
	
	/**
	 * [long][system]
	 * Overrides default provider key validation interval (KVI).
	 */
	public static final String OTP_PROVIDER_SONICLEAUTH_KVI = "otp.provider.sonicleauth.kvi";
	
	/**
	 * [string[]][system+domain]
	 */
	public static final String OTP_TRUST_ADDRESSES = "otp.trust.addresses";
	/**
	 * [boolean][system+domain]
	 */
	public static final String OTP_TRUST_DEVICE_ENABLED = "otp.trust.device.enabled";
	
	/**
	 * [integer][system+domain]
	 */
	public static final String OTP_TRUST_DEVICE_DURATION = "otp.trust.device.duration";
	
	/**
	 * [string][system]
	 */
	public static final String SYNC_DEVICES_SHELL_URI = "sync.devices.shell.uri";
	
	//public static final String DROPBOX_APP_KEY = "dropbox.appkey";
	//public static final String DROPBOX_APP_SECRET = "dropbox.appsecret";
	//public static final String GOOGLE_DRIVE_CLIENT_ID = "googledrive.clientid";
	//public static final String GOOGLE_DRIVE_CLIENT_SECRET = "googledrive.clientsecret";
	//public static final String DEFAULT_LANGUAGE = "default.language";
	//public static final String DEFAULT_COUNTRY = "default.country";
	
	public String getTempPath() {
		return getString(TMP, null);
	}
	
	public String getPhpPath() {
		return getString(PHP_PATH, null);
	}
	
	public String getZPushPath() {
		return getString(ZPUSH_PATH, null);
	}
	
	public String getSystemTempPath() {
		return getString(SYSTEM_PATH_TEMP, null);
	}
	
	public String getSystemPublicPath() {
		return getString(SYSTEM_PATH_PUBLIC, null);
	}
	
	public Boolean getHideLoginDomains() {
		return getBoolean(LOGIN_HIDE_DOMAINS, false);
	}
	
	public Boolean getHideLoginFootprint() {
		return getBoolean(LOGIN_HIDE_FOOTPRINT, false);
	}
	
	public String getUserInfoProvider() {
		return getString(USERINFO_PROVIDER, "WebTop");
	}
	
	public Boolean getFeedbackEnabled() {
		return getBoolean(FEEDBACK_ENABLED, true);
	}
	
	public Boolean getWhatsnewEnabled() {
		return getBoolean(WHATSNEW_ENABLED, true);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public boolean getOTPEnabled() {
		return getBoolean(OTP_ENABLED, false);
	}
	
	public long getOTPProviderSonicleAuthKVI() {
		return getLong(OTP_PROVIDER_SONICLEAUTH_KVI, SonicleAuth.DEFAULT_KEY_VALIDATION_INTERVAL);
	}
	
	public String getOTPTrustedAddresses() {
		return getString(OTP_TRUST_ADDRESSES, null);
	}
	
	public boolean getOTPDeviceTrustEnabled() {
		return getBoolean(OTP_TRUST_DEVICE_ENABLED, true);
	}
	
	public int getOTPDeviceTrustDuration() {
		return getInteger(OTP_TRUST_DEVICE_DURATION, 0);
	}
	
	public String getSyncDevicesShellUri() {
		return getString(SYNC_DEVICES_SHELL_URI, "sh://localhost");
	}
	
	
	/*
	public static String getLanguage(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(Manifest.ID, DEFAULT_LANGUAGE), "it");
	}
	
	public static String getCountry(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(Manifest.ID, DEFAULT_COUNTRY), "IT");
	}
	
	public static Locale getLocale(SettingsManager setm) {
		String language = getLanguage(setm);
		String country = getCountry(setm);
		return new Locale(language, country);
	}
	
	public static boolean getDBInitEnabled(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(Manifest.ID, Settings.DB_INIT_ENABLED), true);
	}
	
	public static boolean getDBUpgradeEnabled(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(Manifest.ID, Settings.DB_UPGRADE_ENABLED), true);
	}
	
	public static boolean getWhatsnewEnabled(SettingsManager setm, String idDomain) {
		return LangUtils.value(setm.getServiceSetting(idDomain, Manifest.ID, Settings.WHATSNEW_ENABLED), false);
	}
	
	public static String getDropboxAppKey(SettingsManager setm) {
		return setm.getServiceSetting(Manifest.ID, Settings.DROPBOX_APP_KEY);
	}
	
	public static String getDropboxAppSecret(SettingsManager setm) {
		return setm.getServiceSetting(Manifest.ID, Settings.DROPBOX_APP_SECRET);
	}
	
	public static String getGoogleDriveClientID(SettingsManager setm) {
		return setm.getServiceSetting(Manifest.ID, Settings.GOOGLE_DRIVE_CLIENT_ID);
	}
	
	public static String getGoogleDriveClientSecret(SettingsManager setm) {
		return setm.getServiceSetting(Manifest.ID, Settings.GOOGLE_DRIVE_CLIENT_SECRET);
	}
	*/
	
	public static String getSystemLanguage(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(CoreManifest.ID, CoreServiceSettings.SYSTEM_LANGUAGE), "it");
	}
	
	public static String getSystemCountry(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(CoreManifest.ID, CoreServiceSettings.SYSTEM_COUNTRY), "IT");
	}
	
	public static Locale getSystemLocale(SettingsManager setm) {
		return new Locale(getSystemLanguage(setm), getSystemCountry(setm));
	}
	
	public String getDefaultTheme() {
		return getString(DEFAULT_PREFIX + CoreUserSettings.THEME, "crisp");
	}
	
	public String getDefaultLayout() {
		return getString(DEFAULT_PREFIX + CoreUserSettings.LAYOUT, "default");
	}
	
	public String getDefaultLaf() {
		return getString(DEFAULT_PREFIX + CoreUserSettings.LAF, "default");
	}
	
	public boolean getDefaultRtl() {
		return getBoolean(DEFAULT_PREFIX + CoreUserSettings.RTL, false);
	}
	
	public int getDefaultStartDay() {
		return getInteger(DEFAULT_PREFIX + CoreUserSettings.START_DAY, CoreUserSettings.START_DAY_MONDAY);
	}
	
	public String getDefaultShortDateFormat() {
		return getString(DEFAULT_PREFIX + CoreUserSettings.SHORT_DATE_FORMAT, "dd/MM/yyyy");
	}
	
	public String getDefaultLongDateFormat() {
		return getString(DEFAULT_PREFIX + CoreUserSettings.LONG_DATE_FORMAT, "dd MMM yyyy");
	}
	
	public String getDefaultShortTimeFormat() {
		return getString(DEFAULT_PREFIX + CoreUserSettings.SHORT_TIME_FORMAT, "HH:mm");
	}
	
	public String getDefaultLongTimeFormat() {
		return getString(DEFAULT_PREFIX + CoreUserSettings.LONG_TIME_FORMAT, "HH:mm:ss");
	}
}
