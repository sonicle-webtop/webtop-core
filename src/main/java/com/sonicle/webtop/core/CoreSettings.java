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

/**
 *
 * @author malbinola
 */
public class CoreSettings {
	
	/**
	 * [system]
	 * [string]
	 * Defines server path in which PHP is installed
	 */
	public static final String PHP_PATH = "php.path";
	
	/**
	 * [system]
	 * [string]
	 * Defines server path in which ZPUSH is installed
	 */
	public static final String ZPUSH_PATH = "zpush.path";
	
	/**
	 * [system]
	 * [string][templatable]
	 * Defines platform home path
	 */
	public static final String HOME_PATH = "home.path";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines how the webapp is reachable from outside
	 */
	public static final String PUBLIC_BASE_URL = "public.url";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines how the dav-server is reachable from outside
	 */
	public static final String DAVSERVER_BASE_URL = "davserver.url";
	
	/**
	 * [domain+system]
	 * [boolean]
	 * Defines if PECBridge management is enabled
	 */
	public static final String CONFIG_PECBRIDGE_MANAGEMENT = "config.pecbridge.management";
	
	/**
	 * [domain+system]
	 * [boolean]
	 * Defines if Fetchmail management is enabled
	 */
	public static final String CONFIG_FETCHMAIL_MANAGEMENT = "config.fetchmail.management";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines Dropbox API credentials
	 */
	public static final String DROPBOX_APP_KEY = "dropbox.appkey";
	public static final String DROPBOX_APP_SECRET = "dropbox.appsecret";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines GoogleDrive API credentials
	 */
	public static final String GOOGLE_DRIVE_CLIENT_ID = "googledrive.clientid";
	public static final String GOOGLE_DRIVE_CLIENT_SECRET = "googledrive.clientsecret";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines the download URL for the Notifier addon
	 */
	public static final String ADDON_NOTIFIER_URL = "addon.notifier.url";
	
	/**
	 * [domain+system]
	 * [long]
	 * Maximum file size for uploads
	 */
	//public static final String UPLOAD_MAXFILESIZE = "upload.maxfilesize";/////////////////////////////////////////////////+default
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines SMTP server host
	 */
	public static final String SMTP_HOST = "smtp.host";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines SMTP server port
	 */
	public static final String SMTP_PORT = "smtp.port";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines XMPP server host
	 */
	public static final String XMPP_HOST = "xmpp.host";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines XMPP server port
	 */
	public static final String XMPP_PORT = "xmpp.port";
	
	/**
	 * [domain+system]
	 * [string]
	 * Defines XMPP MultiUserChat subdomain
	 */
	public static final String XMPP_MUC_SUBDOMAIN = "xmpp.muc.subdomain";
	
	/**
	 * [domain+system][*]
	 * [string]
	 * Activate syslog
	 */
	public static final String SYSLOG_ENABLED = "syslog.enabled";
	
	/**
	 * [system]
	 * [string]
	 * Defines system default language locale
	 */
	public static final String SYSTEM_LANGUAGE = "system.language";
	
	/**
	 * [system]
	 * [string]
	 * Defines system default country locale
	 */
	public static final String SYSTEM_COUNTRY = "system.country";
	
	/**
	 * [system]
	 * [string]
	 * Defines the URI to access to tomcat manager
	 */
	public static final String TOMCAT_MANAGER_URI = "tomcat.manager.uri";
	
	/**
	 * [system]
	 * [boolean]
	 * Indicates whether to forcedly hide domain selection from login page
	 */
	public static final String LOGIN_HIDE_DOMAINS = "login.domains.hide";
	
	/**
	 * [system]
	 * [boolean]
	 * Indicates whether to forcedly hide footer system details (os, appserver, 
	 * java) from login page.
	 */
	public static final String LOGIN_HIDE_SYSTEMINFO = "login.systeminfo.hide";
	
	/**
	 * [system]
	 * [boolean]
	 * Indicates whether to forcedly hide deployed webapp name.
	 */
	public static final String LOGIN_HIDE_WEBAPPNAME = "login.webappname.hide";
	
	/**
	 * [user+domain+system]
	 * [boolean]
	 * 2FA enabled status
	 */
	public static final String OTP_ENABLED = "otp.enabled";
	
	/**
	 * [system]
	 * [long]
	 * Overrides default provider key validation interval (KVI).
	 */
	public static final String OTP_PROVIDER_SONICLEAUTH_KVI = "otp.provider.sonicleauth.kvi";
	
	/**
	 * [domain+system]
	 * [string[]]
	 */
	public static final String OTP_TRUST_ADDRESSES = "otp.trust.addresses";
	/**
	 * [domain+system]
	 * [boolean]
	 */
	public static final String OTP_TRUST_DEVICE_ENABLED = "otp.trust.device.enabled";
	
	/**
	 * [domain+system]
	 * [integer]
	 */
	public static final String OTP_TRUST_DEVICE_DURATION = "otp.trust.device.duration";
	
	/**
	 * [system]
	 * [string]
	 */
	public static final String DEVICES_SYNC_SHELL_URI = "devices.sync.shell.uri";
	
	/**
	 * [system]
	 * [time(hh:mm)]
	 * Time instant at which check devices syncronization status 
	 * in order to send notification alerts.
	 */
	public static final String DEVICES_SYNC_CHECK_TIME = "devices.sync.check.time";
	
	/**
	 * [system]
	 * [boolean]
	 * Indicates if maintenance management is active or not.
	 * This should be used only to disable this feature, active by default.
	 */
	public static final String MAINTENANCE_ENABLED = "maintenance.enabled";
	
	/**
	 * [system]
	 * [boolean]
	 * Indicates if database initialization is active or not.
	 * This should be used only to disable this feature, active by default.
	 */
	public static final String DB_INIT_ENABLED = "db.init.enabled";
	
	/**
	 * [system]
	 * [boolean]
	 * Indicates if database automatic upgrade is active or not.
	 * This should be used only to disable this feature, active by default.
	 */
	public static final String DB_UPGRADE_AUTO = "db.upgrade.auto";
	
	/**
	 * [domain+system]
	 * [string]
	 * Specifies if what's new visualization to users is enabled.
	 */
	public static final String WHATSNEW_ENABLED = "whatsnew.enabled";
	
	/**
	 * [system][*]
	 * [boolean]
	 * Indicates whether DevMode is active for a service.
	 */
	public static final String DEV_MODE = "devmode";
	
	/**
	 * [system][*]
	 * [boolean]
	 * Indicates whether a service is in maintenance or not.
	 */
	public static final String MAINTENANCE = "maintenance";
	
	/**
	 * [system][*]
	 * [string]
	 * Stores installed service version
	 */
	public static final String MANIFEST_VERSION = "manifest.version";
	
	/**
	 * [system][*]
	 * [string]
	 * Specifies the service public name to use typically in public URLs.
	 * If specified it overrides the generated one (see ServiceManager).
	 */
	public static final String PUBLIC_NAME = "public.name";
	
	/**
	 * [domain+system]
	 * [string]
	 */
	public static final String FAX_FILETYPES = "fax.filetypes";
	
	/**
	 * [domain+system]
	 * [int]
	 */
	public static final String FAX_MAXRECIPIENTS = "fax.maxrecipients";
	
	/**
	 * [domain+system]
	 * [string]
	 */
	public static final String FAX_PATTERN = "fax.pattern";
	
	/**
	 * [domain+system]
	 * [string]
	 */
	public static final String FAX_SUBJECT = "fax.subject";
	
	/**
	 * [domain+system]
	 * [string]
	 */
	public static final String FAX_SMTP_HOST = "fax.smtp.host";
	
	/**
	 * [domain+system]
	 * [int]
	 */
	public static final String FAX_SMTP_PORT = "fax.smtp.port";
	
	/**
	 * [string][system+domain][*]
	 * Overrides support email address
	 */
	//public static final String MANIFEST_SUPPORT_EMAIL = "manifest.support.email";
	
	//public static final String DEFAULT_LANGUAGE = "default.language";
	//public static final String DEFAULT_COUNTRY = "default.country";
	
	
	
	
	
	/**
	 * [domain+system+user][default]
	 * [string]
	 * 
	 */
	public static final String SERVICES_ORDER = "services.order";
	
	/**
	 * [user][default]
	 * [string]
	 * Theme name
	 */
	public static final String THEME = "theme";
	
	/**
	 * [user][default]
	 * [string]
	 * Layout
	 */
	public static final String LAYOUT = "layout";
	
	/**
	 * [user][default]
	 * [string]
	 * Look and feel
	 */
	public static final String LAF = "laf";
	
	/**
	 * [user][default]
	 * [boolean]
	 * Right-to-left mode
	 */
	public static final String RTL = "rtl";
	
	/**
	 * [user][default]
	 * [string]
	 * Preferred service ID to set after sign-in
	 */
	public static final String STARTUP_SERVICE = "startup.service";
	
	/**
	 * [user][default]
	 * [string]
	 * Desktop notification
	 */
	public static final String DESKTOP_NOTIFICATION = "notifications.desktop";
	public static final String DESKTOP_NOTIFICATION_NEVER = "never";
	public static final String DESKTOP_NOTIFICATION_ALWAYS = "always";
	public static final String DESKTOP_NOTIFICATION_AUTO = "auto";
	
	/**
	 * [user][default]
	 * [string]
	 * Language tag (es. it_IT, en_EN)
	 */
	public static final String LANGUAGE_TAG = "i18n.languageTag";
	
	/**
	 * [user][default]
	 * [string]
	 * Timezone
	 */
	public static final String TIMEZONE = "i18n.timezone";
	
	/**
	 * [user][user][default]
	 * [int]
	 * Week start day (0:sunday, 1:monday)
	 */
	public static final String START_DAY = "i18n.startDay";
	public static final int START_DAY_SUNDAY = 0;
	public static final int START_DAY_MONDAY = 1;
	
	/**
	 * [user][default]
	 * [string]
	 * Short date format pattern
	 */
	public static final String SHORT_DATE_FORMAT = "i18n.format.date.short";
	
	/**
	 * [user][default]
	 * [string]
	 * Long date format pattern
	 */
	public static final String LONG_DATE_FORMAT = "i18n.format.date.long";
	
	/**
	 * [user][default]
	 * [string]
	 * Short time format pattern
	 */
	public static final String SHORT_TIME_FORMAT = "i18n.format.time.short";
	
	/**
	 * [user][default]
	 * [string]
	 * Long time format pattern
	 */
	public static final String LONG_TIME_FORMAT = "i18n.format.time.long";
	
	/**
	 * [user][*]
	 * [boolean]
	 * Stores if profile has been initialized for a service
	 */
	public static final String INITIALIZED = "initialized";
	
	/**
	 * [user]
	 * [boolean]
	 * Specifies if whatsnew window must be shown after a service upgrade
	 */
	public static final String WHATSNEW_NEEDED = "whatsnew.needed";
	
	/**
	 * [user][*]
	 * [string]
	 * Saves width of tool component
	 */
	public static final String VIEWPORT_TOOL_WIDTH = "viewport.tool.width";
	
	/**
	 * [user][*]
	 * [string]
	 * Saves last seen service version for whatsnew handling
	 */
	public static final String WHATSNEW_VERSION = "whatsnew.version";
	
	/**
	 * [user]
	 * [string]
	 * Specifies delivery method. One of: email, googleauth.
	 */
	public static final String OTP_DELIVERY = "otp.delivery";
	public static final String OTP_DELIVERY_EMAIL = "email";
	public static final String OTP_DELIVERY_GOOGLEAUTH = "googleauth";
	
	/**
	 * [user]
	 * [string]
	 * Specifies generated secret string within googleauth delivery.
	 */
	public static final String OTP_SECRET = "otp.secret";
	
	/**
	 * [user]
	 * [string]
	 * Specifies choosen email address within email delivery.
	 */
	public static final String OTP_EMAILADDRESS = "otp.emailaddress";
	public static final String OTP_TRUSTED_DEVICE = "otp.trusteddevice";
	
	/**
	 * [user][default]
	 * [boolean]
	 * Enables an email alert if device sync is broken 
	 */
	public static final String DEVICES_SYNC_ALERT_ENABLED = "devices.sync.alert.enabled";
	
	/**
	 * [user]
	 * [int] (number of days 1->30)
	 * Specifies the maximum difference between current time and last-sync time 
	 * before sending a notification alert
	 */
	public static final String DEVICES_SYNC_ALERT_TOLERANCE = "devices.sync.alert.tolerance";
	
	/**
	 * [domain+system]
	 * [string]
	 * Specifies the PBX provider, if available. 
	 */
	public static final String PBX_PROVIDER = "pbx.provider";
	
	/**
	 * [domain+system]
	 * [string]
	 * Specifies the PBX NethVoice provider webrest URL, if available. 
	 */
	public static final String PBX_PROVIDER_NETHVOICE_WEBREST_URL = "pbx.provider.nethvoice.webrest.url";
	
	/**
	 * [user]
	 * [string]
	 * Specifies the PBX username, when different from WebTop username. 
	 */
	public static final String PBX_USERNAME = "pbx.username";
	
	/**
	 * [user]
	 * [string]
	 * Specifies the PBX password, when different from WebTop password. 
	 */
	public static final String PBX_PASSWORD = "pbx.password";
	
	/**
	 * [user]
	 * [enum]
	 * Chat presence status
	 */
	public static final String IM_PRESENCE_STATUS = "im.presencestatus";
	
	/**
	 * [user]
	 * [string]
	 * Chat status message
	 */
	public static final String IM_STATUS_MESSAGE = "im.statusmessage";
	
	/**
	 * [user+domain+system]
	 * [int]
	 * Maximum file size for chat uploads
	 */
	public static final String IM_UPLOAD_MAXFILESIZE = "im.upload.maxfilesize";
	
	/**
	 * [user]
	 * [boolean]
	 * Play a sound when a friend connects
	 */
	public static final String IM_SOUND_ON_FRIEND_CONNECT = "im.sound.on.friend.connect";
	
	/**
	 * [user]
	 * [boolean]
	 * Play a sound when a friend disconnects
	 */
	public static final String IM_SOUND_ON_FRIEND_DISCONNECT = "im.sound.on.friend.disconnect";
	
	/**
	 * [user]
	 * [boolean]
	 * Play a sound when a message is received
	 */
	public static final String IM_SOUND_ON_MESSAGE_RECEIVED = "im.sound.on.message.received";
	
	/**
	 * [user]
	 * [boolean]
	 * Play a sound when a message is sent
	 */
	public static final String IM_SOUND_ON_MESSAGE_SENT = "im.sound.on.message.sent";
}
