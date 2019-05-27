/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.commons.PropUtils;
import java.util.Properties;

/**
 *
 * @author malbinola
 */
public class WebTopProps {
	public static final String LOG_DIR = "webtop.log.dir";
	public static final String LOG_FILE_BASENAME = "webtop.log.file.basename";
	public static final String LOG_APPENDER = "webtop.log.appender";
	public static final String WEBAPPSCONFIG_DIR = "webtop.webappsconfig.dir";
	public static final String EXTJS_DEBUG = "webtop.extjs.debug";
	public static final String JS_DEBUG = "webtop.js.debug";
	public static final String SOEXT_DEV_MODE = "webtop.soext.devmode";
	public static final String DEV_MODE = "webtop.devmode";
	public static final String SCHEDULER_DISABLED = "webtop.scheduler.disabled";	
	
	public static void initOldPropsCompatibility() {
		Properties systemProps = System.getProperties();
		copyOldProp(systemProps, "com.sonicle.webtop.webappsConfigPath", WEBAPPSCONFIG_DIR);
		//copyOldProp(systemProps, "com.sonicle.webtop.extJsDebug", EXTJS_DEBUG);
		//copyOldProp(systemProps, "com.sonicle.webtop.debugMode", JS_DEBUG);
		//copyOldProp(systemProps, "com.sonicle.webtop.soExtDevMode", SOEXT_DEV_MODE);
		//copyOldProp(systemProps, "com.sonicle.webtop.devMode", DEV_MODE);
		//copyOldProp(systemProps, "com.sonicle.webtop.schedulerDisabled", SCHEDULER_DISABLED);
	}
	
	public static void print(Properties props) {
		WebTopApp.logger.info("{} = {}", LOG_DIR, getLogDir(props));
		WebTopApp.logger.info("{} = {}", LOG_FILE_BASENAME, getLogFileBasename(props));
		WebTopApp.logger.info("{} = {}", LOG_APPENDER, getLogAppender(props));
		WebTopApp.logger.info("{} = {}", WEBAPPSCONFIG_DIR, getWebappsConfigDir(props));
		WebTopApp.logger.info("{} = {}", EXTJS_DEBUG, getExtJsDebug(props));
		WebTopApp.logger.info("{} = {}", JS_DEBUG, getJsDebug(props));
		WebTopApp.logger.info("{} = {}", SOEXT_DEV_MODE, getSoExtJsExtensionsDevMode(props));
		WebTopApp.logger.info("{} = {}", DEV_MODE, getDevMode(props));
		WebTopApp.logger.info("{} = {}", SCHEDULER_DISABLED, getSchedulerDisabled(props));
	}
	
	public static void checkOldPropsUsage(Properties props) {
		testAndWarnPropUsage(props, "com.sonicle.webtop.webappsConfigPath", WEBAPPSCONFIG_DIR);
		testAndWarnPropUsage(props, "com.sonicle.webtop.extJsDebug", EXTJS_DEBUG);
		testAndWarnPropUsage(props, "com.sonicle.webtop.debugMode", JS_DEBUG);
		testAndWarnPropUsage(props, "com.sonicle.webtop.soExtDevMode", SOEXT_DEV_MODE);
		testAndWarnPropUsage(props, "com.sonicle.webtop.devMode", DEV_MODE);
		testAndWarnPropUsage(props, "com.sonicle.webtop.schedulerDisabled", SCHEDULER_DISABLED);
	}
	
	public static String getLogDir() {
		return getLogDir(System.getProperties());
	}
	
	public static String getLogDir(Properties props) {
		return PropUtils.getStringProperty(props, LOG_DIR, "/var/log");
	}
	
	public static String getLogFileBasename() {
		return getLogFileBasename(System.getProperties());
	}
	
	public static String getLogFileBasename(Properties props) {
		return PropUtils.getStringProperty(props, LOG_FILE_BASENAME, null);
	}
	
	public static String getLogAppender() {
		return getLogAppender(System.getProperties());
	}
	
	public static String getLogAppender(Properties props) {
		return PropUtils.getStringProperty(props, LOG_APPENDER, "stdout");
	}
	
	public static String getWebappsConfigDir() {
		return getWebappsConfigDir(System.getProperties());
	}
	
	public static String getWebappsConfigDir(Properties props) {
		return PropUtils.getStringProperty(props, WEBAPPSCONFIG_DIR, null);
	}
	
	public static boolean getExtJsDebug() {
		return getExtJsDebug(System.getProperties());
	}
	
	public static boolean getExtJsDebug(Properties props) {
		return PropUtils.getBooleanProperty(props, EXTJS_DEBUG, false);
	}
	
	public static boolean getJsDebug() {
		return getJsDebug(System.getProperties());
	}
	
	public static boolean getJsDebug(Properties props) {
		return PropUtils.getBooleanProperty(props, JS_DEBUG, false);
	}
	
	public static boolean getSoExtJsExtensionsDevMode() {
		return getSoExtJsExtensionsDevMode(System.getProperties());
	}
	
	public static boolean getSoExtJsExtensionsDevMode(Properties props) {
		return PropUtils.getBooleanProperty(props, SOEXT_DEV_MODE, false);
	}
	
	public static boolean getDevMode() {
		return getDevMode(System.getProperties());
	}

	public static boolean getDevMode(Properties props) {
		return PropUtils.getBooleanProperty(props, DEV_MODE, false);
	}
	
	public static boolean getSchedulerDisabled() {
		return getSchedulerDisabled(System.getProperties());
	}
	
	public static boolean getSchedulerDisabled(Properties props) {
		return PropUtils.getBooleanProperty(props, SCHEDULER_DISABLED, false);
	}
	
	private static void copyOldProp(Properties props, String oldKey, String newKey) {
		PropUtils.copy(props, oldKey, props, newKey);
		PropUtils.copy(props, oldKey.toLowerCase(), props, newKey);
	}
	
	private static void testAndWarnPropUsage(Properties props, String oldKey, String newKey) {
		if (PropUtils.isDefined(props, oldKey)) {
			WebTopApp.logger.warn("Property '{}' is deprecated. Please use '{}' instead.", oldKey, newKey);
		}
		if (PropUtils.isDefined(props, oldKey.toLowerCase())) {
			WebTopApp.logger.warn("Property '{}' is deprecated. Please use '{}' instead.", oldKey.toLowerCase(), newKey);
		}
	}
}
