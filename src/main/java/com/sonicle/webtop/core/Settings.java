/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sonicle.webtop.core;

import com.sonicle.commons.LangUtils;
import java.util.Locale;

/**
 *
 * @author matteo
 */
public class Settings {
	
	public static final String DEFAULT_LANGUAGE = "default.language";
	public static final String DEFAULT_COUNTRY = "default.country";
	public static final String MAINTENANCE = "maintenance";
	public static final String SECRET = "secret";
	public static final String DB_INIT_ENABLED = "db.init.enabled";
	public static final String DB_UPGRADE_ENABLED = "db.upgrade.enabled";
	public static final String WHATSNEW_ENABLED = "whatsnew.enabled";
	public static final String WHATSNEW_VERSION = "whatsnew.version";
	public static final String MANIFEST_VERSION = "manifest.version";
	public static final String MANIFEST_SUPPORT_EMAIL = "manifest.support.email";
	public static final String PROFILEDATA_EDITABLE = "profiledata.editable";
	public static final String PROFILEDATA_PROVIDER = "profiledata.provider";
	public static final String OTP_ENABLED = "otp.enabled";
	public static final String OTP_TRUST_ADDRESSES = "otp.trust.addresses";
	public static final String OTP_TRUST_DEVICE_ENABLED = "otp.trust.device.enabled";
	public static final String OTP_TRUST_DEVICE_DURATION = "otp.trust.device.duration";
	public static final String OTP_SONICLEAUTH_INTERVAL = "otp.sonicleauth.interval";
	public static final String OTP_EMAILADDRESS = "otp.emailaddress";
	public static final String OTP_DELIVERY = "otp.delivery";
	public static final String OTP_DELIVERY_EMAIL = "email";
	public static final String OTP_DELIVERY_GOOGLEAUTH = "googleauth";
	public static final String OTP_SECRET = "otp.secret";
	public static final String OTP_TRUSTED_DEVICE = "otp.trusteddevice";
	public static final String DROPBOX_APP_KEY = "dropbox.appkey";
	public static final String DROPBOX_APP_SECRET = "dropbox.appsecret";
	public static final String GOOGLE_DRIVE_CLIENT_ID = "googledrive.clientid";
	public static final String GOOGLE_DRIVE_CLIENT_SECRET = "googledrive.clientsecret";
	
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
}