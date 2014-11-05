/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sonicle.webtop.core;


import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.sdk.BaseServiceSettings;
import java.util.Locale;

/**
 *
 * @author matteo
 */
public class CoreServiceSettings extends BaseServiceSettings {
	
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
	 * Indicates if system is under maintenance
	 */
	public static final String MAINTENANCE = "maintenance";
	/**
	 * [string][system][*]
	 * Stores installed service version
	 */
	public static final String MANIFEST_VERSION = "manifest.version";
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
	public static final String USERDATA_PROVIDER = "userdata.provider";
	
	public static final String TMP = "tmp";
	
	//public static final String OTP_ENABLED = "otp.enabled";
	//public static final String OTP_TRUST_ADDRESSES = "otp.trust.addresses";
	//public static final String OTP_TRUST_DEVICE_ENABLED = "otp.trust.device.enabled";
	//public static final String OTP_TRUST_DEVICE_DURATION = "otp.trust.device.duration";
	//public static final String DROPBOX_APP_KEY = "dropbox.appkey";
	//public static final String DROPBOX_APP_SECRET = "dropbox.appsecret";
	//public static final String GOOGLE_DRIVE_CLIENT_ID = "googledrive.clientid";
	//public static final String GOOGLE_DRIVE_CLIENT_SECRET = "googledrive.clientsecret";
	//public static final String DEFAULT_LANGUAGE = "default.language";
	//public static final String DEFAULT_COUNTRY = "default.country";
	
	public CoreServiceSettings(String domainId, String serviceId) {
		super(domainId, serviceId);
	}
	
	public String getUserDataProvider() {
		return LangUtils.value(getServiceSetting(CoreServiceSettings.USERDATA_PROVIDER), "WebTop");
	}
	
	public String getTempPath() {
		return getServiceSetting(TMP);
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
	
	public static boolean getOTPEnabled(SettingsManager setm, String domainId) {
		return LangUtils.value(setm.getServiceSetting(domainId, Manifest.ID, Settings.OTP_ENABLED), false);
	}
	
	public static String getOTPTrustedAddresses(SettingsManager setm, String domainId) {
		return LangUtils.value(setm.getServiceSetting(domainId, Manifest.ID, Settings.OTP_TRUST_ADDRESSES), (String)null);
	}
	
	public static boolean getOTPDeviceTrustEnabled(SettingsManager setm, String domainId) {
		return LangUtils.value(setm.getServiceSetting(domainId, Manifest.ID, Settings.OTP_TRUST_DEVICE_ENABLED), true);
	}
	
	public static int getOTPDeviceTrustDuration(SettingsManager setm, String domainId) {
		return LangUtils.value(setm.getServiceSetting(domainId, Manifest.ID, Settings.OTP_TRUST_DEVICE_DURATION), 0);
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
}
