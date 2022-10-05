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

import com.sonicle.commons.PathUtils;
import com.sonicle.commons.PropUtils;
import com.sonicle.commons.web.ContextUtils;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.util.ICalendarUtils;
import java.io.File;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class WebTopProps {
	public static final String WEBTOP_PROPERTIES_FILE = "webtop.properties";
	public static final String PROP_ETC_DIR = "webtop.etc.dir";
	public static final String PROP_LOG_DIR = "webtop.log.dir";
	public static final String PROP_LOG_FILE_BASENAME = "webtop.log.file.basename";
	public static final String PROP_LOG_MAIN_TARGET = "webtop.log.target";
	public static final String PROP_LOG_MAIN_FILE_POLICY = "webtop.log.file.policy";
	public static final String PROP_LOG_AUTH_TARGET = "webtop.log.auth.target";
	public static final String PROP_LOGBACK_SYSLOG_HOST = "webtop.logback.syslog.host";
	public static final String PROP_LOGBACK_SYSLOG_PORT = "webtop.logback.syslog.port";
	public static final String PROP_EXTJS_DEBUG = "webtop.extjs.debug";
	public static final String PROP_JS_DEBUG = "webtop.js.debug";
	public static final String PROP_SOEXT_DEV_MODE = "webtop.soext.devmode";
	public static final String PROP_DEV_MODE = "webtop.devmode";
	public static final String PROP_SCHEDULER_DISABLED = "webtop.scheduler.disabled";
	public static final String PROP_QUARTZ_MAXTHREADS = "webtop.quartz.maxthreads";
	public static final String PROP_ATMO_MAXSCHEDULERTHREADS = "webtop.atmosphere.maxschedulerthreads";
	public static final String PROP_ATMO_MAXPROCESSINGTHREADS = "webtop.atmosphere.maxprocessingthreads";
	public static final String PROP_ATMO_MAXWRITETHREADS = "webtop.atmosphere.maxwritethreads";
	public static final String PROP_TOMCAT_MANAGER_URI = "webtop.tomcat.manager.uri";
	public static final String PROP_WTDIR_SIMILARITY_LEVENTHRES = "webtop.directory.similarity.leventhres";
	public static final String PROP_WTDIR_SIMILARITY_TOKENSIZE = "webtop.directory.similarity.tokensize";
	public static final String PROP_SESSION_FORCESECURECOOKIE = "webtop.session.forcesecurecookie";
	
	public static void init() {
		Properties systemProps = System.getProperties();
		
		copyOldProp(systemProps, "com.sonicle.webtop.webappsConfigPath", PROP_ETC_DIR);
		systemProps.setProperty("net.fortuna.ical4j.timezone.update.enabled", "false");
		//systemProps.setProperty("mail.mime.address.strict", "false"); // If necessary set using -D
		systemProps.setProperty("mail.mime.decodetext.strict", "false");
		systemProps.setProperty("mail.mime.decodefilename", "true");
		
		// In order to avoid connection stucks during reads, force a timeout.
		// As reccomended by Bill Shannon, write timeouts should not be set.
		systemProps.setProperty("mail.imap.connectiontimeout", "60000");
		systemProps.setProperty("mail.imap.timeout", "60000");
		//systemProps.setProperty("mail.imap.writetimeout", "60000");
		systemProps.setProperty("mail.imaps.connectiontimeout", "60000");
		systemProps.setProperty("mail.imaps.timeout", "60000");
		//systemProps.setProperty("mail.imaps.writetimeout", "60000");
		systemProps.setProperty("mail.smtp.connectiontimeout", "60000");
		systemProps.setProperty("mail.smtp.timeout", "60000");
		//systemProps.setProperty("mail.smtp.writetimeout", "60000");
		systemProps.setProperty("mail.smtps.connectiontimeout", "60000");
		systemProps.setProperty("mail.smtps.timeout", "60000");
		//systemProps.setProperty("mail.smtps.writetimeout", "60000");
		
		ICalendarUtils.setUnfoldingRelaxed(systemProps, true);
		ICalendarUtils.setParsingRelaxed(systemProps, true);
		ICalendarUtils.setValidationRelaxed(systemProps, true);
		ICalendarUtils.setCompatibilityOutlook(systemProps, true);
		ICalendarUtils.setCompatibilityNotes(systemProps, true);
	}
	
	public static void load(Properties properties, String webappFullName) {
		String configsDir = System.getProperty(PROP_ETC_DIR);
		if (!StringUtils.isBlank(configsDir)) {
			File propFile1 = new File(PathUtils.concatPaths(configsDir, "/"), WEBTOP_PROPERTIES_FILE);
			if (WebTopApp.logger.isDebugEnabled()) LogbackHelper.printToSystemOut("[{}] Looking for properties at '{}'", webappFullName, propFile1.toString());
			if (PropUtils.loadFromFile(properties, propFile1)) {
				LogbackHelper.printToSystemOut("[{}] Using properties file at '{}'", webappFullName, propFile1.toString());
			}
			File propFile2 = new File(PathUtils.concatPaths(configsDir, ContextUtils.stripWebappVersion(webappFullName) + "/"), WEBTOP_PROPERTIES_FILE);
			if (WebTopApp.logger.isDebugEnabled()) LogbackHelper.printToSystemOut("[{}] Looking for properties at '{}'", webappFullName, propFile2.toString());
			if (PropUtils.loadFromFile(properties, propFile2)) {
				LogbackHelper.printToSystemOut("[{}] Using properties file at '{}'", webappFullName, propFile2.toString());
			}
		} else {
			if (WebTopApp.logger.isDebugEnabled()) LogbackHelper.printToSystemOut("[{}] System property '{}' is missing: custom {} file ignored", webappFullName, PROP_ETC_DIR, WEBTOP_PROPERTIES_FILE);
		}
	}
	
	public static void print(Properties properties) {
		WebTopApp.logger.info("{} = {} [{}]", PROP_ETC_DIR, properties.getProperty(PROP_ETC_DIR), getEtcDir(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_LOG_DIR, properties.getProperty(PROP_LOG_DIR), getLogDir(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_LOG_FILE_BASENAME, properties.getProperty(PROP_LOG_FILE_BASENAME), getLogFileBasename(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_LOG_MAIN_TARGET, properties.getProperty(PROP_LOG_MAIN_TARGET), getLogMainTarget(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_LOG_MAIN_FILE_POLICY, properties.getProperty(PROP_LOG_MAIN_FILE_POLICY), getLogMainFilePolicy(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_LOG_AUTH_TARGET, properties.getProperty(PROP_LOG_AUTH_TARGET), getLogAuthTarget(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_LOGBACK_SYSLOG_HOST, properties.getProperty(PROP_LOGBACK_SYSLOG_HOST), getLogbackSyslogHost(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_LOGBACK_SYSLOG_PORT, properties.getProperty(PROP_LOGBACK_SYSLOG_PORT), getLogbackSyslogPort(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_EXTJS_DEBUG, properties.getProperty(PROP_EXTJS_DEBUG), getExtJsDebug(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_JS_DEBUG, properties.getProperty(PROP_JS_DEBUG), getJsDebug(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_SOEXT_DEV_MODE, properties.getProperty(PROP_SOEXT_DEV_MODE), getSoExtJsExtensionsDevMode(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_DEV_MODE, properties.getProperty(PROP_DEV_MODE), getDevMode(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_SCHEDULER_DISABLED, properties.getProperty(PROP_SCHEDULER_DISABLED), getSchedulerDisabled(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_QUARTZ_MAXTHREADS, properties.getProperty(PROP_QUARTZ_MAXTHREADS), getQuartzMaxThreads(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_ATMO_MAXSCHEDULERTHREADS, properties.getProperty(PROP_ATMO_MAXSCHEDULERTHREADS), getAtmosphereMaxSchedulerThreads(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_ATMO_MAXPROCESSINGTHREADS, properties.getProperty(PROP_ATMO_MAXPROCESSINGTHREADS), getAtmosphereMaxProcessingThreads(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_ATMO_MAXWRITETHREADS, properties.getProperty(PROP_ATMO_MAXWRITETHREADS), getAtmosphereMaxWriteThreads(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_TOMCAT_MANAGER_URI, properties.getProperty(PROP_TOMCAT_MANAGER_URI), getTomcatManagerUri(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_WTDIR_SIMILARITY_LEVENTHRES, properties.getProperty(PROP_WTDIR_SIMILARITY_LEVENTHRES), getWTDirectorySimilarityLevenThres(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_WTDIR_SIMILARITY_TOKENSIZE, properties.getProperty(PROP_WTDIR_SIMILARITY_TOKENSIZE), getWTDirectorySimilarityTokenSize(properties));
		WebTopApp.logger.info("{} = {} [{}]", PROP_SESSION_FORCESECURECOOKIE, properties.getProperty(PROP_SESSION_FORCESECURECOOKIE), getSessionForceSecureCookie(properties));
	}
	
	public static void checkOldPropsUsage(Properties properties) {
		testAndWarnPropUsage(properties, "webtop.webappsConfigPath", PROP_ETC_DIR);
		testAndWarnPropUsage(properties, "com.sonicle.webtop.webappsConfigPath", PROP_ETC_DIR);
		testAndWarnPropUsage(properties, "com.sonicle.webtop.extJsDebug", PROP_EXTJS_DEBUG);
		testAndWarnPropUsage(properties, "com.sonicle.webtop.debugMode", PROP_JS_DEBUG);
		testAndWarnPropUsage(properties, "com.sonicle.webtop.soExtDevMode", PROP_SOEXT_DEV_MODE);
		testAndWarnPropUsage(properties, "com.sonicle.webtop.devMode", PROP_DEV_MODE);
		testAndWarnPropUsage(properties, "com.sonicle.webtop.schedulerDisabled", PROP_SCHEDULER_DISABLED);
	}
	
	/*
	public static String getLogDir() {
		return getLogDir(System.getProperties());
	}
	
	public static String getLogFileBasename() {
		return getLogFileBasename(System.getProperties());
	}
	
	public static String getLogAppender() {
		return getLogAppender(System.getProperties());
	}
	
	public static String getWebappsConfigDir() {
		return getWebappsConfigDir(System.getProperties());
	}
	
	public static boolean getExtJsDebug() {
		return getExtJsDebug(System.getProperties());
	}
	
	public static boolean getJsDebug() {
		return getJsDebug(System.getProperties());
	}
	
	public static boolean getSoExtJsExtensionsDevMode() {
		return getSoExtJsExtensionsDevMode(System.getProperties());
	}
	
	public static boolean getDevMode() {
		return getDevMode(System.getProperties());
	}
	
	public static boolean getSchedulerDisabled() {
		return getSchedulerDisabled(System.getProperties());
	}
	*/
	
	public static String getEtcDir(Properties props) {
		return PropUtils.getStringProperty(props, PROP_ETC_DIR, null);
	}
	
	public static String getLogDir(Properties props) {
		return PropUtils.getStringProperty(props, PROP_LOG_DIR, "/var/log");
	}
	
	public static String getLogFileBasename(Properties props) {
		return PropUtils.getStringProperty(props, PROP_LOG_FILE_BASENAME, "webtop");
	}
	
	public static String getLogMainTarget(Properties props) {
		return PropUtils.getStringProperty(props, PROP_LOG_MAIN_TARGET, "console");
	}
	
	public static String getLogMainFilePolicy(Properties props) {
		return PropUtils.getStringProperty(props, PROP_LOG_MAIN_FILE_POLICY, "rolling");
	}
	
	public static String getLogAuthTarget(Properties props) {
		return PropUtils.getStringProperty(props, PROP_LOG_AUTH_TARGET, "none");
	}
	
	public static String getLogbackSyslogHost(Properties props) {
		return PropUtils.getStringProperty(props, PROP_LOGBACK_SYSLOG_HOST, "localhost");
	}
	
	public static int getLogbackSyslogPort(Properties props) {
		return PropUtils.getIntProperty(props, PROP_LOGBACK_SYSLOG_PORT, 514);
	}
	
	public static boolean getExtJsDebug(Properties props) {
		return PropUtils.getBooleanProperty(props, PROP_EXTJS_DEBUG, false);
	}
	
	public static boolean getJsDebug(Properties props) {
		return PropUtils.getBooleanProperty(props, PROP_JS_DEBUG, false);
	}
	
	public static boolean getSoExtJsExtensionsDevMode(Properties props) {
		return PropUtils.getBooleanProperty(props, PROP_SOEXT_DEV_MODE, false);
	}
	
	public static boolean getDevMode(Properties props) {
		return PropUtils.getBooleanProperty(props, PROP_DEV_MODE, false);
	}
	
	public static boolean getSchedulerDisabled(Properties props) {
		return PropUtils.getBooleanProperty(props, PROP_SCHEDULER_DISABLED, false);
	}
	
	public static int getQuartzMaxThreads(Properties props) {
		return Math.max(5, PropUtils.getIntProperty(props, PROP_QUARTZ_MAXTHREADS, 10));
	}
	
	public static int getAtmosphereMaxSchedulerThreads(Properties props) {
		return Math.max(5, PropUtils.getIntProperty(props, PROP_ATMO_MAXSCHEDULERTHREADS, 10));
	}
	
	public static int getAtmosphereMaxProcessingThreads(Properties props) {
		return Math.max(5, PropUtils.getIntProperty(props, PROP_ATMO_MAXPROCESSINGTHREADS, 10));
	}
	
	public static int getAtmosphereMaxWriteThreads(Properties props) {
		return Math.max(5, PropUtils.getIntProperty(props, PROP_ATMO_MAXWRITETHREADS, 10));
	}
	
	public static String getTomcatManagerUri(Properties props) {
		return PropUtils.getStringProperty(props, PROP_TOMCAT_MANAGER_URI, null);
	}
	
	public static short getWTDirectorySimilarityLevenThres(Properties props) {
		return (short)Math.max(0, PropUtils.getIntProperty(props, PROP_WTDIR_SIMILARITY_LEVENTHRES, 5));
	}
	
	public static short getWTDirectorySimilarityTokenSize(Properties props) {
		return (short)Math.max(0, PropUtils.getIntProperty(props, PROP_WTDIR_SIMILARITY_TOKENSIZE, 4));
	}
	
	public static boolean getSessionForceSecureCookie(Properties props) {
		return PropUtils.getBooleanProperty(props, PROP_SESSION_FORCESECURECOOKIE, false);
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
