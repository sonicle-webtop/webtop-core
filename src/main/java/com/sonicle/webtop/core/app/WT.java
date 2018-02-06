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
package com.sonicle.webtop.core.app;

import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.servlet.PublicRequest;
import com.sonicle.webtop.core.util.LoggerUtils;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.Reflection;

/**
 *
 * @author malbinola
 */
public class WT {
	private static final Logger logger = getLogger(WT.class);
	static final HashMap<String, ServiceManifest> manifestCache = new HashMap<>();
	private static final HashMap<String, String> cnameToServiceIdCache = new HashMap<>();
	
	public static final Locale LOCALE_ENGLISH = new Locale("en", "EN");
	
	private static WebTopApp getWTA() {
		return WebTopApp.getInstance();
	}
	
	public static boolean isLatestWebApp() {
		return getWTA().isLatest();
	}
	
	public static String getPlatformName() {
		return getWTA().getPlatformName();
	}
	
	public static Charset getSystemCharset() {
		return getWTA().getSystemCharset();
	}
	
	public static DateTimeZone getSystemTimeZone() {
		return getWTA().getSystemTimeZone();
	}
	
	public static List<AppLocale> getInstalledLocales() {
		return getWTA().getI18nManager().getLocales();
	}
	
	public static List<TimeZone> getTimezones() {
		return getWTA().getI18nManager().getTimezones();
	}
	
	public static InternetAddress buildDomainInternetAddress(String domainId, String local, String personal) {
		String internetName = WT.getDomainInternetName(domainId);
		return InternetAddressUtils.toInternetAddress(local, internetName, personal);
	}
	
	public static InternetAddress getNoReplyAddress(String domainId) {
		return buildDomainInternetAddress(domainId, "webtop-noreply", null);
	}
	
	public static InternetAddress getNotificationAddress(String domainId) {
		return buildDomainInternetAddress(domainId, "webtop-notification", null);
	}
	
	public static ServiceManifest getManifest(String serviceId) {
		return getWTA().getServiceManager().getManifest(serviceId);
	}
	
	public static ServiceManifest getManifest(Class clazz) {
		String sid = findServiceId(clazz);
		return (sid != null) ? getManifest(sid) : null;
	}
	
	public static String getPublicBaseUrl(String domainId) {
		return getWTA().getPublicBaseUrl(domainId);
	}
	
	public static String getDomainPublicName(String domainId) {
		return getWTA().getWebTopManager().domainIdToPublicName(domainId);
	}
	
	public static String getServicePublicName(String serviceId) {
		return getWTA().getServiceManager().getPublicName(serviceId);
	}
	
	public static String getServicePublicUrl(String domainId, String serviceId) {
		final String baseUrl = getPublicBaseUrl(domainId);
		final String domainPublicName = getDomainPublicName(domainId);
		final String servicePublicName = getServicePublicName(serviceId);
		return PathUtils.concatPathParts(baseUrl, PublicRequest.URL, domainPublicName, servicePublicName);
	}
	
	public static String getPublicImagesUrl(String domainId) {
		final String baseUrl = getPublicBaseUrl(domainId);
		final String domainPublicName = getDomainPublicName(domainId);
		return PathUtils.concatPathParts(baseUrl, "resources", domainPublicName, "images/");
	}
	
	/*
	public static ServiceManifest findManifest(Class clazz) {
		String cname = clazz.getName();
		synchronized(manifestCache) {
			if(cnameToServiceIdCache.containsKey(cname)) {
				return manifestCache.get(cnameToServiceIdCache.get(cname));
			} else {
				for(String sid : manifestCache.keySet()) {
					if(StringUtils.startsWith(cname, sid)) {
						cnameToServiceIdCache.put(cname, sid);
						return manifestCache.get(sid);
					}
				}
			}
		}
		return null;
	}
	*/
	
	public static String findServiceId(Class clazz) {
		String cname = clazz.getName();
		synchronized(cnameToServiceIdCache) {
			if(cnameToServiceIdCache.containsKey(cname)) {
				return cnameToServiceIdCache.get(cname);
			} else {
				String matchingSid = null;
				for(String sid : manifestCache.keySet()) {
					// Stopping to the first match is not enought, we have to
					// check all installed services
					if(StringUtils.startsWith(cname, sid)) matchingSid = sid;
				}
				if(matchingSid != null) cnameToServiceIdCache.put(cname, matchingSid);
				return matchingSid;
			}
		}
	}
	
	public static String findDomainIdByPublicName(String domainPublicName) {
		return getWTA().getWebTopManager().publicNameToDomainId(domainPublicName);
	}
	
	public static String findDomainIdByInternetName(String internetName) {
		return getWTA().getWebTopManager().internetNameToDomain(internetName);
	}
	
	public static CoreManager getCoreManager() {
		WebTopSession wts = SessionContext.getCurrent(false);
		if(wts != null) {
			return (CoreManager)wts.getServiceManager(CoreManifest.ID);
		} else {
			// For admin subject during application startup
			return getWTA().getServiceManager().instantiateCoreManager(false, RunContext.getRunProfileId());
		}
		//return RunContext.getWebTopSession().getCoreManager();
	}
	
	public static CoreManager getCoreManager(UserProfileId targetProfileId) {
		return getCoreManager(true, targetProfileId);
	}
	
	public static CoreManager getCoreManager(boolean fastInit, UserProfileId targetProfileId) {
		if(targetProfileId.equals(RunContext.getRunProfileId())) {
			return getCoreManager();
		} else {
			return getWTA().getServiceManager().instantiateCoreManager(fastInit, targetProfileId);
		}
	}
	
	public static CoreAdminManager getCoreAdminManager(UserProfileId targetProfileId) {
		//TODO: verificare
		return getWTA().getServiceManager().instantiateCoreAdminManager(false, targetProfileId);
	}
	
	public static BaseManager getServiceManager(String serviceId) {
		WebTopSession wts = SessionContext.getCurrent(false);
		if (wts != null) {
			return wts.getServiceManager(serviceId);
		} else {
			return getWTA().getServiceManager().instantiateServiceManager(serviceId, true, RunContext.getRunProfileId());
		}
		//return RunContext.getWebTopSession().getServiceManager(serviceId);
	}
	
	public static BaseManager getServiceManager(String serviceId, UserProfileId targetProfileId) {
		return getServiceManager(serviceId, true, targetProfileId);
	}
	
	public static BaseManager getServiceManager(String serviceId, boolean fastInit, UserProfileId targetProfileId) {
		if (targetProfileId.equals(RunContext.getRunProfileId())) {
			return getServiceManager(serviceId);
		} else {
			return getWTA().getServiceManager().instantiateServiceManager(serviceId, fastInit, targetProfileId);
		}
	}
	
	public static DataSource getCoreDataSource() throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getDataSource();
	}
	
	public static DataSource getDataSource(String serviceId) throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		if (conm.isRegistered(serviceId, ConnectionManager.DEFAULT_DATASOURCE)) {
			return conm.getDataSource(serviceId, ConnectionManager.DEFAULT_DATASOURCE);
		} else {
			return conm.getDataSource();
		}
	}
	
	public static DataSource getDataSource(String serviceId, String dataSourceName) throws SQLException {
		return getWTA().getConnectionManager().getDataSource(serviceId, dataSourceName);
	}
	
	public static Connection getCoreConnection() throws SQLException {
		return getWTA().getConnectionManager().getConnection();
	}
	
	public static Connection getCoreConnection(boolean autoCommit) throws SQLException {
		return getWTA().getConnectionManager().getConnection(autoCommit);
	}
	
	public static Connection getConnection(String serviceId) throws SQLException {
		return getWTA().getConnectionManager().getFallbackConnection(serviceId);
	}
	
	public static Connection getConnection(String serviceId, boolean autoCommit) throws SQLException {
		return getWTA().getConnectionManager().getFallbackConnection(serviceId, autoCommit);
	}
	
	public static Connection getConnection(String serviceId, String dataSourceName) throws SQLException {
		return getWTA().getConnectionManager().getConnection(serviceId, dataSourceName);
	}
	
	public static Connection getConnection(String serviceId, String dataSourceName, boolean autoCommit) throws SQLException {
		return getWTA().getConnectionManager().getConnection(serviceId, dataSourceName, autoCommit);
	}
	
	public static String getDomainImagesPath(String domainId) {
		return getWTA().getImagesPath(domainId);
	}
	
	public static String getServiceHomePath(String domainId, String serviceId) {
		//TODO: eliminare questo metodo dopo la modifica della lettura dei model nel servizio di posta
		return getWTA().getServiceHomePath(domainId, serviceId);
	}
	
	public static String getServiceHomePath(String serviceId, UserProfileId profileId) {
		return getWTA().getServiceHomePath(profileId.getDomain(), serviceId);
	}
	
	public static String getTempPath() {
		UserProfileId runPid = RunContext.getRunProfileId();
		return getWTA().getTempPath(runPid.getDomain());
	}
	
	public static File getTempFolder() throws WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		return getWTA().getTempFolder(runPid.getDomain());
	}
	
	public static File createTempFile() throws WTException {
		return createTempFile(null, null);
	}
	
	public static File createTempFile(String prefix, String suffix) throws WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		return getWTA().createTempFile(runPid.getDomain(), prefix, suffix);
	}
	
	public static boolean deleteTempFile(File file) throws WTException {
		return deleteTempFile(file.getName());
	}
	
	public static boolean deleteTempFile(String filename) throws WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		return getWTA().deleteTempFile(runPid.getDomain(), filename);
	}
	
	public static String buildTempFilename() {
		return getWTA().buildTempFilename(null, null);
	}
	
	public static String buildTempFilename(String prefix, String suffix) {
		return getWTA().buildTempFilename(prefix, suffix);
	}
	
	public static Template loadTemplate(String serviceId, String relativePath) throws IOException {
		String path = LangUtils.joinPaths(LangUtils.packageToPath(serviceId), relativePath);
		return getWTA().loadTemplate(path);
	}
	
	public static String buildTemplate(String template, Object data) throws IOException, TemplateException {
		return buildTemplate(CoreManifest.ID, template, data);
	}
	
	public static String buildTemplate(String serviceId, String templateRelativePath, Object data) throws IOException, TemplateException {
		Template tpl = WT.loadTemplate(serviceId, templateRelativePath);
		Writer writer = new StringWriter();
		tpl.process(data, writer);
		return writer.toString();
	}
	
	public static void generateReportToStream(AbstractReport report, AbstractReport.OutputType outputType, OutputStream outputStream) throws JRException, WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		getWTA().getReportManager().generateToStream(runPid.getDomain(), report, outputType, outputStream);
	}
	
	public static String getDomainInternetName(String domainId) {
		try {
			return getWTA().getWebTopManager().getDomainInternetName(domainId);
		} catch(Exception ex) {
			logger.warn("Unable to get DomainInternetName [{}]", domainId, ex);
			return null;
		}
	}
	
	public static UserProfile.Data getUserData(UserProfileId profileId) {
		if (profileId == null) return null;
		try {
			return getWTA().getWebTopManager().userData(profileId);
		} catch(Exception ex) {
			logger.warn("Unable to get UserData [{}]", profileId.toString(), ex);
			return null;
		}
	}
	
	public static UserProfile.Data guessUserData(String emailAddress) {
		try {
			return getWTA().getWebTopManager().userDataByEmail(emailAddress);
		} catch(WTException ex) {
			logger.warn("Unable to guess UserData [{}]", emailAddress, ex);
			return null;
		}
	}
	
	public static UserProfile.PersonalInfo getUserPersonalInfo(UserProfileId profileId) {
		try {
			return getWTA().getWebTopManager().userPersonalInfo(profileId);
		} catch(WTException ex) {
			logger.warn("Unable to get PersonalInfo [{}]", profileId.toString(), ex);
			return null;
		}
	}
	
	public static String lookupCoreResource(Locale locale, String key) {
		return getWTA().lookupResource(CoreManifest.ID, locale, key);
	}
	
	public static String lookupResource(String serviceId, Locale locale, String key) {
		return getWTA().lookupResource(serviceId, locale, key);
	}
	
	public static String lookupResource(String serviceId, Locale locale, String key, boolean escapeHtml) {
		return getWTA().lookupResource(serviceId, locale, key, escapeHtml);
	}
	
	public static Session getGlobalMailSession(UserProfileId pid) {
		return getGlobalMailSession(pid.getDomainId());
	}
	
	public static Session getGlobalMailSession(String domainId) {
		return getWTA().getGlobalMailSession(domainId);
	}
	
	public static boolean writeLog(String action, String softwareName, String remoteIp, String userAgent, String sessionId, String data) {
		String callerServiceId = WT.findServiceId(Reflection.getCallerClass(3));
		return getWTA().getLogManager().write(RunContext.getRunProfileId(), callerServiceId, action, softwareName, remoteIp, userAgent, sessionId, data);
	}
	
	public static boolean writeLog(String action, String softwareName, String data) {
		String callerServiceId = WT.findServiceId(Reflection.getCallerClass(3));
		return getWTA().getLogManager().write(RunContext.getRunProfileId(), callerServiceId, action, softwareName, null, null, null, data);
	}
	
	public static void notify(UserProfileId profileId, ServiceMessage message) {
		WT.notify(profileId, message, false);
	}
	
	public static void notify(UserProfileId profileId, ServiceMessage message, boolean enqueueIfOffline) {
		notify(profileId, Arrays.asList(new ServiceMessage[]{message}), enqueueIfOffline);
	}
	
	public static void notify(UserProfileId profileId, List<ServiceMessage> messages) {
		notify(profileId, messages, false);
	}
	
	public static void notify(UserProfileId profileId, List<ServiceMessage> messages, boolean enqueueIfOffline) {
		getWTA().notify(profileId, messages, enqueueIfOffline);
	}
	
	public static void sendEmail(Session session, boolean rich, String from, String to, String subject, String body) throws MessagingException {
		sendEmail(session, rich, from, new String[]{to}, null, null, subject, body);
	}
	
	public static void sendEmail(Session session, boolean rich, InternetAddress from, InternetAddress to, String subject, String body) throws MessagingException {
		sendEmail(session, rich, from, new InternetAddress[]{to}, null, null, subject, body, null);
	}
	
	public static void sendEmail(Session session, boolean rich, 
			String from, String[] to, String[] cc, String[] bcc, 
				String subject, String body) throws MessagingException {
		
		getWTA().sendEmail(session, rich, from, to, cc, bcc, subject, body);
	}
	
	public static void sendEmail(Session session, boolean rich, 
			InternetAddress from, InternetAddress[] to, InternetAddress[] cc, InternetAddress[] bcc, 
				String subject, String body, MimeBodyPart[] parts) throws MessagingException {
		
		getWTA().sendEmail(session, rich, from, to, cc, bcc, subject, body, parts);
	}
	
	/**
	 * Retrieves MediaType associated to a file extension from the local table.
	 * @param extension The file extension.
	 * @return MediaType string or null if no entry is present
	 */
	public static String getOverriddenMediaType(String extension) {
		return getWTA().getMediaTypes().getMediaType(extension);
	}
	
	/**
	 * Returns a valid logger instance properly configured by WebTop 
	 * environment. Logger name is computed starting from specified class name.
	 * @param clazz A class.
	 * @return A logger instance.
	 */
	public static Logger getLogger(Class clazz) {
		return (Logger) LoggerFactory.getLogger(clazz);
	}
	
	/**
	 * Sets a value for custom diagnostic context variable.
	 * Passed value is associated to the key 'custom' of current DC.
	 * @param value Variable value to set.
	 */
	public static void setLoggerDC(String value) {
		LoggerUtils.setCustomDC(value);
	}
	
	/**
	 * Clears the custom diagnostic context variable.
	 */
	public static void clearLoggerDC() {
		LoggerUtils.clearCustomDC();
	}
}
