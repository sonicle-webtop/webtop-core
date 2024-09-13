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
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.mail.PropsBuilder;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.app.sdk.WTEmailSendException;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.app.servlet.PublicRequest;
import com.sonicle.webtop.core.app.servlet.ResourceRequest;
import com.sonicle.webtop.core.app.model.GenericSubject;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import com.sonicle.webtop.core.util.LoggerUtils;
import com.sonicle.webtop.core.util.RRuleStringify;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Set;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WT {
	private static final Logger logger = getLogger(WT.class);
	private static final Map<String, ServiceManifest> manifestCache = new TreeMap<>();
	private static final Map<String, String> cnameToServiceIdCache = new HashMap<>();
	private static final Map<String, ProductLicense> productCache = new HashMap<>();
	
	public static final Locale LOCALE_ENGLISH = new Locale("en", "EN");
	
	static void registerManifest(String serviceId, ServiceManifest manifest) {
		manifestCache.put(serviceId, manifest);
	}
	
	/**
	 * @deprecated Use getAuthDomainName instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static String getDomainInternetName(final String domainId) {
		return getAuthDomainName(domainId);
	}
	
	/**
	 * @deprecated Use getProfileData instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static UserProfile.Data getUserData(UserProfileId profileId) {
		return getProfileData(profileId);
	}
	
	/**
	 * @deprecated Use getProfilePersonalInfo instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static UserProfile.PersonalInfo getUserPersonalInfo(UserProfileId profileId) {
		return getProfilePersonalInfo(profileId);
	}
	
	/**
	 * @deprecated Use guessProfileDataByPersonalAddress instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static UserProfile.Data guessUserData(String emailAddress) {
		return guessProfileDataByPersonalAddress(emailAddress);
	}
	
	/**
	 * @deprecated Use getProfilePersonalAddress instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static InternetAddress getUserPersonalEmail(UserProfileId profileId) {
		return getProfilePersonalAddress(profileId);
	}
	
	/**
	 * @deprecated Use getProfileAddress instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static InternetAddress getUserProfileEmail(UserProfileId profileId) {
		return getProfileAddress(profileId);
	}
	
	/**
	 * @deprecated Use guessProfileIdByPersonalAddress instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static UserProfileId guessUserProfileIdByEmailAddress(String personalAddress) {
		return guessProfileIdByPersonalAddress(personalAddress);
	}
	
	/**
	 * @deprecated Use guessProfileIdByAuthAddress instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static UserProfileId guessUserProfileIdProfileUsername(String profileUsername) {
		return guessProfileIdByAuthAddress(profileUsername);
	}
	
	/**
	 * @deprecated use getGroupUidOfPecAccounts instead (will be removed in v.5.17.0)
	 */
	@Deprecated
	public static String getGroupUidForPecAccounts(String domainId) {
		return getGroupSidOfPecAccounts(domainId);
	}
	
	/**
	 * @deprecated use findDomainIdByDomainPublicId instead
	 */
	@Deprecated
	public static String findDomainIdByPublicName(String domainPublicId) {
		return findDomainIdByDomainPublicId(domainPublicId);
	}
	
	public static String findServiceId(final Class clazz) {
		return findServiceId(clazz.getName());
	}
	
	public static String findServiceId(final String className) {
		synchronized (cnameToServiceIdCache) {
			if (cnameToServiceIdCache.containsKey(className)) {
				return cnameToServiceIdCache.get(className);
			} else {
				String matchingSid = null;
				for (String sid : manifestCache.keySet()) {
					if (className.startsWith(sid)) matchingSid = sid;
				}
				if (matchingSid != null) cnameToServiceIdCache.put(className, matchingSid);
				return matchingSid;
			}
		}
	}
	
	private static WebTopApp getWTA() {
		return WebTopApp.getInstance();
	}
	
	public static Properties getProperties() {
		return getWTA().getProperties();
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
	
	public static boolean isInMaintenance() {
		return getWTA().isInMaintenance();
	}
	
	//TODO: check usages
	public static InternetAddress buildDomainInternetAddress(String domainId, String local, String personal) {
		String authDomainName = WT.getAuthDomainName(domainId);
		return InternetAddressUtils.toInternetAddress(local, authDomainName, personal);
	}
	
	public static InternetAddress getNoReplyAddress(String domainId) {
		String sender = "webtop-noreply";
		if (WebTopManager.SYSADMIN_DOMAINID.equals(domainId)) {
			String domain = null;
			try {
				Set<String> domainIds = getWTA().getWebTopManager().listDomainIds(EnabledCond.ENABLED_ONLY);
				if (domainIds!=null && !domainIds.isEmpty()) {
					String id = domainIds.iterator().next();
					domain = WT.getPrimaryDomainName(id);
				}
			} catch(WTException exc) { }
			if (domain != null) sender += "@"+domain;
			return InternetAddressUtils.toInternetAddress(sender, null);
		} else {
			return buildDomainInternetAddress(domainId, sender, null);
		}
	}
	
	public static InternetAddress getNotificationAddress(String domainId) {
		return buildDomainInternetAddress(domainId, "webtop-notification", null);
	}
	
	public static ServiceManifest.Product getManifestProduct(String serviceId, String productCode) {
		ServiceManifest manifest = getManifest(serviceId);
		return (manifest == null) ? null : manifest.getProduct(productCode);
	}
	
	public static ServiceManifest getManifest(String serviceId) {
		return getWTA().getServiceManager().getManifest(serviceId);
	}
	
	public static ServiceManifest getManifest(Class clazz) {
		String sid = findServiceId(clazz);
		return (sid != null) ? getManifest(sid) : null;
	}
	
	public static String getPublicBaseUrl(String domainId) {
		//TODO: evaluate to add such sort of caching of this lookup
		return getWTA().getPublicBaseUrl(domainId);
	}
	
	public static String getPublicContextPath(String domainId) {
		try {
			String baseUrl = getPublicBaseUrl(domainId);
			return baseUrl != null ? PathUtils.ensureTrailingSeparator(new URL(baseUrl).getPath()) : null;
		} catch(MalformedURLException ex) {
			return null;
		}
	}
	
	public static String getDavServerBaseUrl(String domainId) {
		return getWTA().getDavServerBaseUrl(domainId);
	}
	
	public static String getDomainPublicName(String domainId) {
		return getWTA().getWebTopManager().domainIdToDomainPublicId(domainId);
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
	
	public static String getServiceLafUrl(String domainId, String serviceId, String laf) {
		final String baseUrl = getPublicBaseUrl(domainId);
		return PathUtils.concatPathParts(baseUrl, ResourceRequest.URL, serviceId, "0.0.0/laf/", laf);
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
	
	public static String findDomainIdByDomainPublicId(String domainPublicId) {
		return getWTA().getWebTopManager().domainPublicIdToDomainId(domainPublicId);
	}
	
	public static String findDomainIdByAuthDomainName(final String authDomainName) {
		return getWTA().getWebTopManager().authDomainNameToDomainId(authDomainName);
	}
	
	/**
	 * Returns the UID of the built-in group 'users'.
	 * @param domainId The domain ID
	 * @return the UID or null if group was not found
	 */
	public static String getGroupSidOfUsers(String domainId) {
		final UserProfileId pid = new UserProfileId(domainId, WebTopManager.GROUPID_USERS);
		return getWTA().getWebTopManager().lookupSubjectSidQuietly(pid, GenericSubject.Type.GROUP);
	}
	
	/**
	 * Returns the UID of the built-in group 'pec-accounts'.
	 * @param domainId The domain ID
	 * @return the UID or null if group was not found
	 */
	public static String getGroupSidOfPecAccounts(String domainId) {
		final UserProfileId pid = new UserProfileId(domainId, WebTopManager.GROUPID_PEC_ACCOUNTS);
		return getWTA().getWebTopManager().lookupSubjectSidQuietly(pid, GenericSubject.Type.GROUP);
	}
	
	/**
	 * Executes passed runnable using SysAdmin subject.
	 * @param runnable 
	 */
	public static void runPrivileged(Runnable runnable) {
		getWTA().getAdminSubject().execute(runnable);
	}
	
	/**
	 * Executes passed callable using SysAdmin subject.
	 * @param <V> The return type
	 * @param callable The callable to run
	 * @return Return object
	 */
	public static <V> V runPrivileged(Callable<V> callable) {
		return getWTA().getAdminSubject().execute(callable);
	}
	
	public static CoreManager getCoreManager() {
		WebTopSession wts = SessionContext.getCurrent(false);
		CoreManager manager = null;
		if (wts != null) {
			manager = (CoreManager)wts.getServiceManager(CoreManifest.ID);
		}
		if (manager == null) {
			// For admin subject during application startup
			manager = getWTA().getServiceManager().instantiateCoreManager(false, RunContext.getRunProfileId());
		}
		return manager;
		/*
		if(wts != null) {
			return (CoreManager)wts.getServiceManager(CoreManifest.ID);
		} else {
			// For admin subject during application startup
			return getWTA().getServiceManager().instantiateCoreManager(false, RunContext.getRunProfileId());
		}
		*/
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
		BaseManager manager = null;
		if (wts != null) {
			manager = wts.getServiceManager(serviceId);
		}
		if (manager == null) {
			manager = getWTA().getServiceManager().instantiateServiceManager(serviceId, true, RunContext.getRunProfileId());
		}
		return manager;
		/*
		if (wts != null) {
			return wts.getServiceManager(serviceId);
		} else {
			return getWTA().getServiceManager().instantiateServiceManager(serviceId, true, RunContext.getRunProfileId());
		}
		*/
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
		return getWTA().getFileSystem().getImagesPath(domainId);
	}
	
	public static String getServiceHomePath(String domainId, String serviceId) {
		//TODO: eliminare questo metodo dopo la modifica della lettura dei model nel servizio di posta
		return getWTA().getFileSystem().getServiceHomePath(domainId, serviceId);
	}
	
	public static String getServiceHomePath(String serviceId, UserProfileId profileId) {
		return getWTA().getFileSystem().getServiceHomePath(profileId.getDomain(), serviceId);
	}
	
	public static String getTempPath() {
		UserProfileId runPid = RunContext.getRunProfileId();
		return getWTA().getFileSystem().getTempPath(runPid.getDomain());
	}
	
	public static File getTempFolder() throws WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		try {
			return getWTA().getFileSystem().getTempFolder(runPid.getDomain());
		} catch (IOException ex) {
			throw new WTException(ex);
		}
	}
	
	public static File createTempFile() throws WTException {
		return createTempFile(null, null);
	}
	
	public static File createTempFile(String prefix) throws WTException {
		return createTempFile(prefix, null);
	}
	
	public static File createTempFile(String prefix, String extension) throws WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		try {
			return getWTA().getFileSystem().createTempFile(runPid.getDomain(), prefix, extension);
		} catch (IOException ex) {
			throw new WTException(ex);
		}
	}
	
	public static boolean deleteTempFile(File file) throws WTException {
		return deleteTempFile(file.getName());
	}
	
	public static boolean deleteTempFile(String filename) throws WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		try {
			return getWTA().getFileSystem().deleteTempFile(runPid.getDomain(), filename);
		} catch (IOException ex) {
			throw new WTException(ex);
		}
	}
	
	public static String buildTempFilename() {
		return getWTA().getFileSystem().buildTempFilename(null, null);
	}
	
	public static String buildTempFilename(String prefix, String suffix) {
		return getWTA().getFileSystem().buildTempFilename(prefix, suffix);
	}
	
	public static Template loadTemplate(String serviceId, String relativePath) throws IOException {
		return getWTA().loadTemplate(serviceId, relativePath);
	}
	
	public static String buildTemplate(String template, Map data) throws IOException, TemplateException {
		return buildTemplate(CoreManifest.ID, template, data);
	}
	
	public static String buildTemplate(String serviceId, String templateRelativePath, Map data) throws IOException, TemplateException {
		data.put("statics", BeansWrapper.getDefaultInstance().getStaticModels());
		Template tpl = WT.loadTemplate(serviceId, templateRelativePath);
		Writer writer = new StringWriter();
		tpl.process(data, writer);
		return writer.toString();
	}
	
	public static void writeTemplate(String serviceId, String templateRelativePath, Map data, Writer out) throws IOException, TemplateException {
		data.put("statics", BeansWrapper.getDefaultInstance().getStaticModels());
		WT.loadTemplate(serviceId, templateRelativePath).process(data, out);
	}
	
	public static void generateReportToStream(AbstractReport report, AbstractReport.OutputType outputType, OutputStream outputStream) throws JRException, WTException {
		UserProfileId runPid = RunContext.getRunProfileId();
		getWTA().getReportManager().generateToStream(runPid.getDomain(), report, outputType, outputStream);
	}
	
	/**
	 * Gets the domain-name configured for the passed domain.
	 * @param domainId The target domain ID.
	 * @return Primary domain-name.
	 */
	public static String getPrimaryDomainName(final String domainId) {
		//if (StringUtils.isBlank(domainId)) return null;
		try {
			return getWTA().getWebTopManager().domainIdToDomainName(domainId);
		} catch (Exception ex) {
			logger.warn("Unable to get domain-name (primary) for domain [{}]", domainId, ex);
			return null;
		}
	}
	
	/**
	 * Gets the authentication domain-name configured for the passed domain.
	 * @param domainId The target domain ID.
	 * @return Authentication domain-name.
	 */
	public static String getAuthDomainName(final String domainId) {
		//if (StringUtils.isBlank(domainId)) return null;
		try {
			return getWTA().getWebTopManager().domainIdToAuthDomainName(domainId);
		} catch (Exception ex) {
			logger.warn("Unable to get domain-name (authentication) for domain [{}]", domainId, ex);
			return null;
		}
	}
	
	/**
	 * Tries to lookup the profile ID of the passed internet address.
	 * NB: The local part is not checked against existent usernames, only domain is verified.
	 * @param authAddress The authentication (email) address (eg. {user}@{domain})
	 * @return Profile ID or null
	 */
	public static UserProfileId guessProfileIdByAuthAddress(final String authAddress) {
		if (StringUtils.isBlank(authAddress)) return null;
		try {
			return getWTA().getWebTopManager().guessProfileIdAuthenticationAddress(authAddress);
		} catch (Exception ex) {
			logger.trace("Unable to get ProfileId [{}]", authAddress, ex);
			return null;
		}
	}
	
	/**
	 * Tries to lookup the profile ID of the passed internet address.
	 * @param personalAddress The personal (email) address (eg. {user}@{domain})
	 * @return Profile ID or null
	 */
	public static UserProfileId guessProfileIdByPersonalAddress(final String personalAddress) {
		if (StringUtils.isBlank(personalAddress)) return null;
		try {
			return getWTA().getWebTopManager().guessProfileIdByPersonalAddress(personalAddress);
		} catch (Exception ex) {
			logger.trace("Unable to get ProfileId [{}]", personalAddress, ex);
			return null;
		}
	}
	
	/**
	 * Gets the PersonalInfo object associated to the passed profile ID.
	 * @param profileId The target profile ID to lookup.
	 * @return PersonalInfo object or null
	 */
	public static UserProfile.PersonalInfo getProfilePersonalInfo(final UserProfileId profileId) {
		try {
			return getWTA().getWebTopManager().lookupProfilePersonalInfo(profileId, true);
		} catch (Exception ex) {
			logger.trace("Unable to get PersonalInfo [{}]", profileId.toString(), ex);
			return null;
		}
	}
	
	/**
	 * Gets the Data object associated to the passed profile ID.
	 * @param profileId The target profile ID to lookup.
	 * @return Data object or null
	 */
	public static UserProfile.Data getProfileData(final UserProfileId profileId) {
		if (profileId == null) return null;
		try {
			return getWTA().getWebTopManager().lookupProfileData(profileId, true);
		} catch (Exception ex) {
			logger.trace("Unable to get ProfileData [{}]", profileId.toString(), ex);
			return null;
		}
	}
	
	/**
	 * Gets the Data object associated to the passed profile ID.
	 * @param personalAddress The personal (email) address (eg. {user}@{domain})
	 * @return Data object or null
	 */
	public static UserProfile.Data guessProfileDataByPersonalAddress(final String personalAddress) {
		try {
			final WebTopManager wtMgr = getWTA().getWebTopManager();
			UserProfileId pid = wtMgr.guessProfileIdByPersonalAddress(personalAddress);
			return (pid != null) ? wtMgr.lookupProfileData(pid, false) : null;
		} catch (Exception ex) {
			logger.trace("Unable to get ProfileData [{}]", personalAddress, ex);
			return null;
		}
	}
	
	/**
	 * Gets the personal (email) address associated to the passed profile ID.
	 * @param profileId The target profile ID to lookup.
	 * @return Personal internet address or null
	 */
	public static InternetAddress getProfilePersonalAddress(final UserProfileId profileId) {
		UserProfile.Data ud = getProfileData(profileId);
		return (ud != null) ? ud.getPersonalEmail(): null;
	}
	
	/**
	 * Gets the profile/authentication (email) address associated to the passed profile ID.
	 * @param profileId The target profile ID to lookup.
	 * @return Profile internet address or null
	 */
	public static InternetAddress getProfileAddress(final UserProfileId profileId) {
		UserProfile.Data ud = getProfileData(profileId);
		return (ud != null) ? ud.getProfileEmail(): null;
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
	
	public static String lookupFormattedResource(String serviceId, Locale locale, String key, Object... arguments) {
		return MessageFormat.format(LangUtils.escapeMessageFormat(getWTA().lookupResource(serviceId, locale, key)), arguments);
	}
	
	public static PropsBuilder getMailSessionPropsBuilder(final boolean forTransport, final boolean forStore) {
		return getWTA().getMailBasePropsBuilder(forTransport, forStore);
	}
	
	public static Session getGlobalMailSession(UserProfileId pid) {
		return getGlobalMailSession(pid.getDomainId());
	}
	
	public static Session getGlobalMailSession(String domainId) {
		return getWTA().getGlobalMailSession(domainId);
	}
	
	public static <C extends Enum<C>, A extends Enum<A>> void writeAuditLog(final String softwareName, final String serviceId, final C context, final A action, final Object reference, final Object data) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return;
		logMgr.write(RunContext.getRunProfileId(), softwareName, SessionContext.getCurrentId(), serviceId, context, action, reference, data);
	}
	
	public static void writeAuditLog(final String softwareName, final String serviceId, final String context, final String action, final String reference, final String data) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return;
		logMgr.write(RunContext.getRunProfileId(), softwareName, SessionContext.getCurrentId(), serviceId, context, action, reference, data);
	}
	
	public static <C extends Enum<C>, A extends Enum<A>> void writeAuditLog(final String softwareName, final String serviceId, final C context, final A action, final Collection<AuditReferenceDataEntry> entries) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return;
		logMgr.write(RunContext.getRunProfileId(), softwareName, SessionContext.getCurrentId(), serviceId, context, action, entries);
	}
	
	public static void writeAuditLog(final String softwareName, final String serviceId, final String context, final String action, final Collection<AuditReferenceDataEntry> entries) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return;
		logMgr.write(RunContext.getRunProfileId(), softwareName, SessionContext.getCurrentId(), serviceId, context, action, entries);
	}
	
	public static <C extends Enum<C>, A extends Enum<A>> AuditLogManager.Batch auditLogGetBatch(final String softwareName, final String serviceId, final C context, final A action) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return null;
		return logMgr.getBatch(RunContext.getRunProfileId(), softwareName, SessionContext.getCurrentId(), serviceId, context, action);
	}
	
	public static AuditLogManager.Batch auditLogGetBatch(final String softwareName, final String serviceId, final String context, final String action) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return null;
		return logMgr.getBatch(RunContext.getRunProfileId(), softwareName, SessionContext.getCurrentId(), serviceId, context, action);
	}
	
	public static <C extends Enum<C>> void auditLogRebaseReference(final String serviceId, final C context, final Object oldReference, final Object newReference) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return;
		logMgr.rebaseReference(RunContext.getRunProfileId(), serviceId, context, oldReference, newReference);
	}
	
	public static void auditLogRebaseReference(final String serviceId, final String context, final String oldReference, final String newReference) {
		AuditLogManager logMgr = getWTA().getAuditLogManager();
		if (logMgr == null) return;
		logMgr.rebaseReference(RunContext.getRunProfileId(), serviceId, context, oldReference, newReference);
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
	
	public static void sendEmailMessage(final UserProfileId sendingProfileId, final MimeMessage message) throws WTEmailSendException {
		getWTA().sendEmailMessage(sendingProfileId, message, null);
	}
	
	public static void sendEmailMessage(final UserProfileId sendingProfileId, final MimeMessage message, final String moveToFolderAfterSent) throws WTEmailSendException {
		getWTA().sendEmailMessage(sendingProfileId, message, moveToFolderAfterSent);
	}
	
	public static void sendEmailMessage(final UserProfileId sendingProfileId, final EmailMessage message) throws WTEmailSendException {
		getWTA().sendEmailMessage(sendingProfileId, message, null);
	}
	
	public static void sendEmailMessage(final UserProfileId sendingProfileId, final EmailMessage message, final String moveToFolderAfterSent) throws WTEmailSendException {
		getWTA().sendEmailMessage(sendingProfileId, message, moveToFolderAfterSent);
	}
	
	@Deprecated
	public static void sendEmail(Session session, boolean rich, String from, String to, String subject, String body) throws MessagingException {
		sendEmail(session, rich, from, new String[]{to}, null, null, subject, body);
	}
	
	@Deprecated
	public static void sendEmail(Session session, boolean rich, InternetAddress from, InternetAddress to, String subject, String body) throws MessagingException {
		sendEmail(session, rich, from, new InternetAddress[]{to}, null, null, subject, body, null);
	}
	
	@Deprecated
	public static void sendEmail(Session session, boolean rich, 
			String from, String[] to, String[] cc, String[] bcc, 
				String subject, String body) throws MessagingException {
		
		getWTA().sendEmail(session, rich, from, to, cc, bcc, subject, body);
	}
	
	@Deprecated
	public static void sendEmail(Session session, boolean rich, 
			InternetAddress from, InternetAddress[] to, InternetAddress[] cc, InternetAddress[] bcc, 
				String subject, String body, MimeBodyPart[] parts) throws MessagingException {
		
		getWTA().sendEmail(session, rich, from, to, cc, bcc, subject, body, parts);
	}
	
	@Deprecated
	public static void sendEmail(Session session, boolean rich, InternetAddress from, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String subject, String body, Collection<MimeBodyPart> parts) throws MessagingException {
		getWTA().sendEmail(session, rich, from, to, cc, bcc, subject, body, parts);
	}
	
	@Deprecated
	public static void sendEmail(Session session, String from, Collection<String> to, Collection<String> cc, Collection<String> bcc, String subject, MimeMultipart part) throws MessagingException {
		getWTA().sendEmail(session, from, to, cc, bcc, subject, part);
	}
	
	@Deprecated
	public static void sendEmail(Session session, InternetAddress from, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String subject, MimeMultipart part) throws MessagingException {
		getWTA().sendEmail(session, from, to, cc, bcc, subject, part);
	}
	
	public static RRuleStringify.Strings getRRuleStringifyStrings(Locale locale) {
		RRuleStringify.Strings strings = new RRuleStringify.Strings(locale);
		strings.freqSecondly = WT.lookupCoreResource(locale, "rr.stringify.freq.secondly");
		strings.freqHourly = WT.lookupCoreResource(locale, "rr.stringify.freq.hourly");
		strings.freqDaily = WT.lookupCoreResource(locale, "rr.stringify.freq.daily");
		strings.freqWeekly = WT.lookupCoreResource(locale, "rr.stringify.freq.weekly");
		strings.freqMonthly = WT.lookupCoreResource(locale, "rr.stringify.freq.monthly");
		strings.freqYearly = WT.lookupCoreResource(locale, "rr.stringify.freq.yearly");
		strings.onEvery = WT.lookupCoreResource(locale, "rr.stringify.onEvery");
		strings.day = WT.lookupCoreResource(locale, "rr.stringify.day");
		strings.days = WT.lookupCoreResource(locale, "rr.stringify.days");
		strings.weekday = WT.lookupCoreResource(locale, "rr.stringify.weekday");
		strings.weekdays = WT.lookupCoreResource(locale, "rr.stringify.weekdays");
		strings.weekend = WT.lookupCoreResource(locale, "rr.stringify.weekend");
		strings.week = WT.lookupCoreResource(locale, "rr.stringify.week");
		strings.weeks = WT.lookupCoreResource(locale, "rr.stringify.weeks");
		strings.month = WT.lookupCoreResource(locale, "rr.stringify.month");
		strings.months = WT.lookupCoreResource(locale, "rr.stringify.months");
		strings.year = WT.lookupCoreResource(locale, "rr.stringify.year");
		strings.years = WT.lookupCoreResource(locale, "rr.stringify.years");
		strings.and = WT.lookupCoreResource(locale, "rr.stringify.and");
		strings.on = WT.lookupCoreResource(locale, "rr.stringify.on");
		strings.of = WT.lookupCoreResource(locale, "rr.stringify.of");
		strings.onThe = WT.lookupCoreResource(locale, "rr.stringify.onThe");
		strings.onTheLast = WT.lookupCoreResource(locale, "rr.stringify.onTheLast");
		strings.onThe2ndLast = WT.lookupCoreResource(locale, "rr.stringify.onThe2ndLast");
		strings.time = WT.lookupCoreResource(locale, "rr.stringify.time");
		strings.times = WT.lookupCoreResource(locale, "rr.stringify.times");
		strings.endsBy = WT.lookupCoreResource(locale, "rr.stringify.endsBy");
		strings.nth1st = WT.lookupCoreResource(locale, "rr.stringify.nth.1st");
		strings.nth2nd = WT.lookupCoreResource(locale, "rr.stringify.nth.2nd");
		strings.nth3rd = WT.lookupCoreResource(locale, "rr.stringify.nth.3rd");
		strings.nth4th = WT.lookupCoreResource(locale, "rr.stringify.nth.4th");
		return strings;
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
	
	public static ProductLicense findProductLicense(final BaseServiceProduct serviceProduct) {
		if (serviceProduct == null) return null;
		return getWTA().getLicenseManager().getProductLicense(serviceProduct);
	}
	
	public static ProductLicense findProductLicense(final String productCode, final String domainId) {
		BaseServiceProduct serviceProduct = ProductRegistry.getInstance().getServiceProduct(productCode, domainId);
		return findProductLicense(serviceProduct);
	}
	
	public static BaseServiceProduct findServiceProduct(final String productCode, final String domainId) {
		Check.notEmpty(productCode, "productCode");
		Check.notEmpty(domainId, "domainId");
		return ProductRegistry.getInstance().getServiceProduct(productCode, domainId);
	}
	
	/**
	 * Checks if the specified product is licensed. 
	 * License base validity will be verified.
	 * @param product The product to check.
	 * @return True if a license is installed and valid, false otherwise.
	 */
	public static boolean isLicensed(final BaseServiceProduct product) {
		if (product == null) return false;
		return getWTA().getLicenseManager().checkLicense(product);
	}
	
	/**
	 * Checks if the specified product is licensed and the user has an 
	 * activation (if supported) that allows to use it.
	 * @param product The product to check.
	 * @param userId The user to check.
	 * @return A number with following logic:
	 *   2 success (lease limit exceeded)
	 *   1 success
	 *   0 not installed/missing license
	 *  -1 validation issues (status not valid, missing activation, hwid mismatch)
	 *  -2 lease assignment issues (no left or insert errors)
	 *  -3 auto-lease deactivated
	 */
	public static int isLicensed(final BaseServiceProduct product, final String userId) {
		if (product == null) return 0;
		return getWTA().getLicenseManager().checkLicenseLease(product, userId);
	}
	
	/**
	 * Checks if specified product is not licensed for usage failure error.
	 * @param product The product to check.
	 * @param userId The user to check.
	 * @return True if usage is the cause of failure
	 */
	public static boolean isLicenseUsageFail(final BaseServiceProduct product, final String userId) {
		if (product != null) {
			int ret = isLicensed(product, userId);
			return (ret == 2) || (ret == -1) || (ret == -2);
		}
		return false;
	}
}
