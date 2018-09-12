/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sonicle.webtop.core;


import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.security.otp.provider.SonicleAuth;
import static com.sonicle.webtop.core.CoreSettings.*;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.sdk.BaseServiceSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.joda.time.LocalTime;

/**
 * @author matteo
 */
public class CoreServiceSettings extends BaseServiceSettings {
	
	// NB: please do not provide a constructor that defaults serviceId 
	// because there are some keys that can be applied widely across services
	
	public CoreServiceSettings(SettingsManager setMgr, String serviceId, String domainId) {
		super(setMgr, serviceId, domainId);
	}
	
	public CoreServiceSettings(String serviceId, String domainId) {
		super(serviceId, domainId);
	}
	
	public String getPhpPath() {
		return PathUtils.ensureTrailingSeparator(getString(PHP_PATH, null));
	}
	
	public String getZPushPath() {
		return PathUtils.ensureTrailingSeparator(getString(ZPUSH_PATH, null));
	}
	
	public String getHomePath() {
		return PathUtils.ensureTrailingSeparator(getString(HOME_PATH, null));
	}
	
	public String getPublicBaseUrl() {
		return getString(PUBLIC_BASE_URL, null);
	}
	
	public String getDavServerBaseUrl() {
		return getString(DAVSERVER_BASE_URL, null);
	}
	
	public Boolean getDocumentServerEnabled() {
		return getBoolean(DOCUMENT_SERVER_ENABLED, false);
	}	
	
	public String getDocumentServerPublicUrl() {
		return getString(DOCUMENT_SERVER_PUBLIC_URL, null);
	}
	
	public String getDocumentServerLoopbackUrl() {
		return getString(DOCUMENT_SERVER_LOOPBACK_URL, null);
	}
	
	public String getDocumentServerSecretOut() {
		return getString(DOCUMENT_SERVER_SECRET_OUT, null);
	}
	
	public String getDocumentServerSecretIn() {
		return getString(DOCUMENT_SERVER_SECRET_IN, null);
	}
	
	public Boolean getHasPecBridgeManagement() {
		return getBoolean(CONFIG_PECBRIDGE_MANAGEMENT, false);
	}
	
	public Boolean getHasFetchmailManagement() {
		return getBoolean(CONFIG_FETCHMAIL_MANAGEMENT, false);
	}
	
	public String getDropboxAppKey() {
		return getString(DROPBOX_APP_KEY, null);
	}

	public String getDropboxAppSecret() {
		return getString(DROPBOX_APP_SECRET, null);
	}
	
	public String getGoogleDriveClientID() {
		return getString(GOOGLE_DRIVE_CLIENT_ID, null);
	}
	
	public String getGoogleDriveClientSecret() {
		return getString(GOOGLE_DRIVE_CLIENT_SECRET, null);
	}
	
	public String getAddonNotifierUrl() {
		return getString(ADDON_NOTIFIER_URL, null);
	}
	
	public String getSMTPHost() {
        return getString(SMTP_HOST, "localhost");
    }
    
    public int getSMTPPort() {
        return getInteger(SMTP_PORT, 25);
    }
	
	public boolean isSMTPStartTLS() {
		return getBoolean(SMTP_STARTTLS, false);
	}
	
	public boolean isSMTPAuthentication() {
		return getBoolean(SMTP_AUTH, false);
	}
	
	public ICEServersList getWebRTC_ICEServers() {
		ICEServersList value = getObject(WEBRTC_ICE_SERVERS, null, ICEServersList.class);
        return value;
    }
    
	public String getXMPPHost() {
        return getString(XMPP_HOST, "localhost");
    }
    
    public int getXMPPPort() {
        return getInteger(XMPP_PORT, 5222);
    }
	
	public String getXMPPMucSubdomain() {
        return getString(XMPP_MUC_SUBDOMAIN, "conference");
    }
	
	public String getXMPPBoshUrl() {
        return getString(XMPP_BOSH_URL, null);
    }
	
	public boolean getOTPEnabled() {
		return getBoolean(OTP_ENABLED, false);
	}
	
	public Boolean getHideLoginDomains() {
		return getBoolean(LOGIN_HIDE_DOMAINS, false);
	}
	
	public Boolean getHideLoginSystemInfo() {
		return getBoolean(LOGIN_HIDE_SYSTEMINFO, false);
	}
	
	public Boolean getHideLoginWebappName() {
		return getBoolean(LOGIN_HIDE_WEBAPPNAME, false);
	}
	
	public boolean getSysLogEnabled() {
		return getBoolean(SYSLOG_ENABLED, false);
	}
	
	public Boolean getWhatsnewEnabled() {
		return getBoolean(WHATSNEW_ENABLED, true);
	}
	
	public boolean getPasswordForceChangeIfPolicyUnmet() {
		return getBoolean(PASSWORD_FORCECHANGE_IFPOLICYUNMET, false);
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
	
	public String getFaxFileTypes() {
		return getString(FAX_FILETYPES, "pdf,txt");
	}
	
	public int getFaxMaxRecipients() {
		return getInteger(FAX_MAXRECIPIENTS, 1);
	}
	
	public String getFaxPattern() {
		return getString(FAX_PATTERN, "{number}@fax.provider.net");
	}
	
	public String getFaxSubject() {
		return getString(FAX_SUBJECT, "");
	}
	
	public String getFaxSMTPHost() {
		return getString(FAX_SMTP_HOST, "localhost");
	}
	
	public int getFaxMaxSMTPPort() {
		return getInteger(FAX_SMTP_PORT, 25);
	}
	
	public String getDevicesSyncShellUri() {
		return getString(DEVICES_SYNC_SHELL_URI, "sh://localhost");
	}
	
	public LocalTime getDevicesSyncCheckTime() {
		return getTime(DEVICES_SYNC_CHECK_TIME, "12:00", "HH:mm");
	}
	
	public String getPbxProvider() {
		return getString(PBX_PROVIDER,null);
	}
	
	public String getNethVoiceWebrestURL() {
		return getString(PBX_PROVIDER_NETHVOICE_WEBREST_URL,null);
	}
	
	public String getSmsProvider() {
		return getString(SMS_PROVIDER,null);
	}
	
	public String getSmsWebrestURL() {
		return getString(SMS_PROVIDER_WEBREST_URL,null);
	}
	
	public String getSmsWebrestUser() {
		return getString(SMS_PROVIDER_WEBREST_USER,null);
	}
	
	public String getSmsWebrestPassword() {
		return getString(SMS_PROVIDER_WEBREST_PASSWORD,null);
	}
	
	public String getSmsSender() {
		return getString(SMS_SENDER,null);
	}
	
	public Long getIMUploadMaxFileSize(boolean fallbackOnDefault) {
		final Long value = getLong(IM_UPLOAD_MAXFILESIZE, null);
		if (fallbackOnDefault && (value == null)) {
			return getDefaultIMUploadMaxFileSize();
		} else {
			return value;
		}
	}
	
	public List<LauncherLink> getLauncherLinks() {
		return getObject(LAUNCHER_LINKS, null, LauncherLink.List.class);
	}
	
	public String getLauncherLinksAsString() {
		return getString(LAUNCHER_LINKS, "[]");
	}
	
	public ServicesOrder getServicesOrder() {
		ServicesOrder value = getObject(SERVICES_ORDER, null, ServicesOrder.class);
		return (value != null) ? value : getDefaultServicesOrder();
	}
	
	public boolean setServicesOrder(ServicesOrder value) {
		return setObject(SERVICES_ORDER, value, ServicesOrder.class);
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
	*/
	
	//TODO: verificare se servono (getSystemLanguage, getSystemCountry)
	public static String getSystemLanguage(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(CoreManifest.ID, SYSTEM_LANGUAGE), "it");
	}
	
	public static String getSystemCountry(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(CoreManifest.ID, SYSTEM_COUNTRY), "IT");
	}
	
	public static Locale getSystemLocale(SettingsManager setm) {
		return new Locale("it", "IT");
	}
	
	public static String getTomcatManagerUri(SettingsManager setm) {
		return LangUtils.value(setm.getServiceSetting(CoreManifest.ID, TOMCAT_MANAGER_URI), (String)null);
	}
	
	private ServicesOrder getDefaultServicesOrder() {
		ServicesOrder value = getObject(SERVICES_ORDER, null, ServicesOrder.class);
		if (value == null) {
			value = new ServicesOrder();
			value.add("com.sonicle.webtop.core.admin");
			value.add("com.sonicle.webtop.mail");
			value.add("com.sonicle.webtop.calendar");
			value.add("com.sonicle.webtop.contacts");
			value.add("com.sonicle.webtop.tasks");
			value.add("com.sonicle.webtop.vfs");
		}
		return value;
	}
	
	public String getDefaultTheme() {
		return getString(DEFAULT_PREFIX + THEME, "crisp");
	}
	
	public String getDefaultLayout() {
		return getString(DEFAULT_PREFIX + LAYOUT, "default");
	}
	
	public String getDefaultLaf() {
		return getString(DEFAULT_PREFIX + LAF, "default");
	}
	
	public boolean getDefaultRtl() {
		return getBoolean(DEFAULT_PREFIX + RTL, false);
	}
	
	public ViewportHeaderScale getDefaultViewportHeaderScale() {
		return getEnum(DEFAULT_PREFIX + VIEWPORT_HEADER_SCALE, ViewportHeaderScale.SMALL, ViewportHeaderScale.class);
	}
	
	public String getDefaultStartupService() {
		return getString(DEFAULT_PREFIX + STARTUP_SERVICE, null);
	}
	
	public DesktopNotificationMode getDefaultDesktopNotification() {
		return getEnum(DEFAULT_PREFIX + DESKTOP_NOTIFICATION, DesktopNotificationMode.AUTO, DesktopNotificationMode.class);
	}
	
	public String getDefaultLanguageTag() {
		return getString(DEFAULT_PREFIX + LANGUAGE_TAG, "en_EN");
	}
	
	public String getDefaultTimezone() {
		return getString(DEFAULT_PREFIX + TIMEZONE, "Europe/Rome");
	}
	
	public int getDefaultStartDay() {
		return getInteger(DEFAULT_PREFIX + START_DAY, START_DAY_MONDAY);
	}
	
	public String getDefaultShortDateFormat() {
		return getString(DEFAULT_PREFIX + SHORT_DATE_FORMAT, "dd/MM/yyyy");
	}
	
	public String getDefaultLongDateFormat() {
		return getString(DEFAULT_PREFIX + LONG_DATE_FORMAT, "dd MMM yyyy");
	}
	
	public String getDefaultShortTimeFormat() {
		return getString(DEFAULT_PREFIX + SHORT_TIME_FORMAT, "HH:mm");
	}
	
	public String getDefaultLongTimeFormat() {
		return getString(DEFAULT_PREFIX + LONG_TIME_FORMAT, "HH:mm:ss");
	}
	
	public boolean getDefaultDevicesSyncAlertEnabled() {
		return getBoolean(DEFAULT_PREFIX + DEVICES_SYNC_ALERT_ENABLED, false);
	}
	
	public long getDefaultIMUploadMaxFileSize() {
		return getLong(DEFAULT_PREFIX + IM_UPLOAD_MAXFILESIZE, (long)10485760); // 10MB
	}
	
	public static class ServicesOrder extends ArrayList<String> {
		public ServicesOrder() {
			super();
		}
		
		public static ServicesOrder fromJson(String value) {
			return JsonResult.gson.fromJson(value, ServicesOrder.class);
		}
		
		public static String toJson(ServicesOrder value) {
			return JsonResult.gson.toJson(value, ServicesOrder.class);
		}
	}
	
	public static class ICEServer {
		public String url;
		public String username;
		public String credential;
	}
	
	public static class ICEServersList extends ArrayList<ICEServer> {
		public static ICEServersList fromJson(String value) {
			return JsonResult.gson.fromJson(value, ICEServersList.class);
		}
		
		public static String toJson(ICEServersList value) {
			return JsonResult.gson.toJson(value, ICEServersList.class);
		}		
	}
}
