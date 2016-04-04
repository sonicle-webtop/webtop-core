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
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.webtop.core.bol.OContentType;
import com.sonicle.webtop.core.bol.model.AuthResource;
import com.sonicle.webtop.core.dal.ContentTypeDAO;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
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
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WT {
	static final HashMap<String, ServiceManifest> manifestCache = new HashMap<>();
	private static final HashMap<String, String> cnameToServiceIdCache = new HashMap<>();
	
	private static WebTopApp getWTA() {
		return WebTopApp.getInstance();
	}
	
	public static boolean isLatestWebApp() {
		return getWTA().isLastVersion();
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
	
	public static ServiceManifest getManifest(String serviceId) {
		return getWTA().getServiceManager().getManifest(serviceId);
	}
	
	public static InternetAddress buildDomainInternetAddress(String domainId, String local, String personal) {
		String internetName = WT.getDomainInternetName(domainId);
		return MailUtils.buildInternetAddress(local, internetName, personal);
	}
	
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
	
	public static String findServiceId(Class clazz) {
		String cname = clazz.getName();
		synchronized(cnameToServiceIdCache) {
			if(cnameToServiceIdCache.containsKey(cname)) {
				return cnameToServiceIdCache.get(cname);
			} else {
				for(String sid : manifestCache.keySet()) {
					if(StringUtils.startsWith(cname, sid)) {
						cnameToServiceIdCache.put(cname, sid);
						return sid;
					}
				}
			}
		}
		return null;
	}
	
	public static CoreManager getCoreManager(RunContext context) {
		return new CoreManager(context, getWTA());
	}
	
	public static CoreManager getCoreManager(RunContext context, UserProfile.Id targetProfileId) {
		return new CoreManager(context, targetProfileId, getWTA());
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
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getDataSource(serviceId, dataSourceName);
	}
	
	public static Connection getCoreConnection() throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getConnection();
	}
	
	public static Connection getConnection(String serviceId) throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		if (conm.isRegistered(serviceId, ConnectionManager.DEFAULT_DATASOURCE)) {
			return conm.getConnection(serviceId, ConnectionManager.DEFAULT_DATASOURCE);
		} else {
			return conm.getConnection();
		}
	}
	
	public static Connection getConnection(String serviceId, String dataSourceName) throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getConnection(serviceId, dataSourceName);
	}
	
	public static Connection getConnection(ServiceManifest manifest) throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		if (conm.isRegistered(manifest.getId(), ConnectionManager.DEFAULT_DATASOURCE)) {
			return conm.getConnection(manifest.getId(), ConnectionManager.DEFAULT_DATASOURCE);
		} else {
			return conm.getConnection();
		}
	}
	
	public static Connection getConnection(ServiceManifest manifest, String dataSourceName) throws SQLException {
		ConnectionManager conm = getWTA().getConnectionManager();
		return conm.getConnection(manifest.getId(), dataSourceName);
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
	
	public static String getDomainInternetName(String domainId) {
		try {
			return getWTA().getUserManager().getDomainInternetName(domainId);
		} catch(WTException ex) {
			//TODO: logging
			return null;
		}
	}
	
	public static UserProfile.Data getUserData(UserProfile.Id profileId) {
		try {
			return getWTA().getUserManager().userData(profileId);
		} catch(WTException ex) {
			//TODO: logging
			return null;
		}
	}
	
	public static UserPersonalInfo getUserPersonalInfo(UserProfile.Id profileId) {
		try {
			return getWTA().getUserManager().userPersonalInfo(profileId);
		} catch(WTException ex) {
			//TODO: logging
			return null;
		}
	}
	
	public static boolean isSysAdmin(UserProfile.Id profileId) {
		return getWTA().getAuthManager().isSysAdmin(profileId);
	}
	
	public static boolean isWebTopAdmin(UserProfile.Id profileId) {
		return getWTA().getAuthManager().isWebTopAdmin(profileId);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String serviceId, String resource) {
		return getWTA().getAuthManager().isPermitted(profileId, AuthResource.namespacedName(serviceId, resource));
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String serviceId, String resource, String action) {
		return getWTA().getAuthManager().isPermitted(profileId, AuthResource.namespacedName(serviceId, resource), action);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String serviceId, String resource, String action, String instanceId) {
		return getWTA().getAuthManager().isPermitted(profileId, AuthResource.namespacedName(serviceId, resource), action, instanceId);
	}
	
	public static void ensureIsPermitted(UserProfile.Id profileId, String serviceId, String resource) {
		getWTA().getAuthManager().ensureIsPermitted(profileId, AuthResource.namespacedName(serviceId, resource));
	}
	
	public static void ensureIsPermitted(UserProfile.Id profileId, String serviceId, String resource, String action) {
		getWTA().getAuthManager().ensureIsPermitted(profileId, AuthResource.namespacedName(serviceId, resource), action);
	}
	
	public static void ensureIsPermitted(UserProfile.Id profileId, String serviceId, String resource, String action, String instanceId) {
		getWTA().getAuthManager().ensureIsPermitted(profileId, AuthResource.namespacedName(serviceId, resource), action, instanceId);
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
		getWTA().getReportManager().generateToStream(report, outputType, outputStream);
	}
	
	public static void nofity(UserProfile.Id profileId, ServiceMessage message) {
		nofity(profileId, message, false);
	}
	
	public static void nofity(UserProfile.Id profileId, ServiceMessage message, boolean enqueueIfOffline) {
		nofity(profileId, Arrays.asList(new ServiceMessage[]{message}), enqueueIfOffline);
	}
	
	public static void nofity(UserProfile.Id profileId, List<ServiceMessage> messages) {
		nofity(profileId, messages, false);
	}
	
	public static void nofity(UserProfile.Id profileId, List<ServiceMessage> messages, boolean enqueueIfOffline) {
		getWTA().notify(profileId, messages, enqueueIfOffline);
	}
	
	public static void sendEmail(boolean rich, InternetAddress from, InternetAddress to, String subject, String body) {
		sendEmail(rich, from, new InternetAddress[]{to}, null, null, subject, body, null);
	}
	
	public static void sendEmail(boolean rich, InternetAddress from, InternetAddress[] to, InternetAddress[] cc, InternetAddress[] bcc, String subject, String body, Part[] parts) {
		//TODO: gestire invio email e implementari segnature diverse
		WebTopApp.logger.debug("Invio email da {} a {}", from.toString(), to.toString());
		WebTopApp.logger.debug("{}", subject);
		//WebTopApp.logger.debug("{}", body);
	}
	
	
	public static String generateUUID() {
		return getWTA().generateUUID();
	}
	
	public static String buildTempFilename() {
		return getWTA().buildTempFilename(null, null);
	}
	
	public static String buildTempFilename(String prefix, String suffix) {
		return getWTA().buildTempFilename(prefix, suffix);
	}
	
	public static String getSystemTempPath() {
		return getWTA().getSystemTempPath();
	}
	
	public static File getTempFolder() throws WTException {
		return getWTA().getTempFolder();
	}
	
	public static File createTempFile() throws WTException {
		return createTempFile(null, null);
	}
	
	public static File createTempFile(String prefix, String suffix) throws WTException {
		return getWTA().createTempFile(prefix, suffix);
	}
	
	public static boolean deleteTempFile(String filename) throws WTException {
		return getWTA().deleteTempFile(filename);
	}
	
	/**
	 * Retrieves MediaType associated to a file extension from the local table.
	 * @deprecated Use {@link #getMediaType()} instead.
	 * @param extension The file extension.
	 * @return MediaType string or null if no entry is present
	 */
	@Deprecated
	public static String getContentType(String extension) {
		return getMediaType(extension);
	}
	
	/**
	 * Retrieves MediaType associated to a file extension from the local table.
	 * @param extension The file extension.
	 * @return MediaType string or null if no entry is present
	 */
	public static String getMediaType(String extension) {
		String ctype = null;
		Connection con = null;
		
        try {
			extension = StringUtils.lowerCase(extension);
            con = getCoreConnection();
			OContentType oct = ContentTypeDAO.getInstance().selectByExtension(con, extension);
            if(oct != null) ctype = oct.getContentType();
        } catch(SQLException ex) {
			//logger.error("Error looking up content type for extension {}",extension,exc);
        } finally {
            DbUtils.closeQuietly(con);
        }
        return ctype;
	}
	
	public static String getExtension(String mediaType) {
		String ext = null;
		Connection con = null;
		
		try {
			mediaType = StringUtils.lowerCase(mediaType);
			con = getCoreConnection();
			OContentType oct = ContentTypeDAO.getInstance().selectByContentType(con, mediaType);
			if(oct != null) ext = oct.getExtension();
		} catch(SQLException ex) {
			
		} finally {
			DbUtils.closeQuietly(con);
		}
		return ext;
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
	 * (logger) Apply a custom diagnostic context (DC) to the default one.
	 * Passed value is associated to the key 'custom' of current DC.
	 * @param diagnosticContext Custom diagnostic context string value to append.
	 */
	public static void applyLoggerDC(String diagnosticContext) {
		WebTopApp.setServiceCustomLoggerDC(diagnosticContext);
	}
	
	/**
	 * (logger) Removes custom diagnostic context restoring the default one.
	 * Same behaviour calling: applyLoggerDC(null)
	 */
	public static void clearLoggerDC() {
		WebTopApp.unsetServiceCustomLoggerDC();
	}
}
