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
	 * [string]
	 * Defines webtop home path
	 */
	public static final String HOME_PATH = "home.path";
	
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
	 * [long]
	 * Maximum file size for uploads
	 */
	public static final String UPLOAD_MAXFILESIZE = "upload.maxfilesize";
	
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
	 * [boolean]
	 * Indicates whether to forcedly hide domain selection from login page
	 */
	public static final String LOGIN_HIDE_DOMAINS = "login.domains.hide";
	
	/**
	 * [system]
	 * [boolean]
	 * Indicates whether to forcedly hide footprint details from login page
	 */
	public static final String LOGIN_HIDE_FOOTPRINT = "login.footprint.hide";
	
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
	 * [domain+system]
	 * [string]
	 * Specifies if what's new visualization to users is active
	 */
	public static final String WHATSNEW_ENABLED = "whatsnew.enabled";
	
	
	
	
	
	/**
	 * [system][*]
	 * [boolean]
	 * Indicates if system is under maintenance
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
	 * [system]
	 * [string]
	 */
	public static final String USERINFO_PROVIDER = "userinfo.provider";
	
	//public static final String DEFAULT_LANGUAGE = "default.language";
	//public static final String DEFAULT_COUNTRY = "default.country";
	
	
	
	
	
	
	
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
	 * Desktop notification
	 */
	public static final String DESKTOP_NOTIFICATION = "notifications.desktop";
	public static final String DESKTOP_NOTIFICATION_NEVER = "never";
	public static final String DESKTOP_NOTIFICATION_ALWAYS = "always";
	public static final String DESKTOP_NOTIFICATION_BACKGROUND = "background";
	
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
	 * [user]
	 * [boolean]
	 * Activates debug mode. If activated, client js files will be passed in  
	 * plain text, so they are readable.
	 */
	public static final String SYSTEM_DEBUG = "system.debug";
	
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
	 * Specified the maximum difference between current time and last-sync time 
	 * before sending a notification alert
	 */
	public static final String DEVICES_SYNC_ALERT_TOLERANCE = "devices.sync.alert.tolerance";
}
