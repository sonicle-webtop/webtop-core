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

import com.sonicle.webtop.core.app.util.OSInfo;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.web.ContextUtils;
import com.sonicle.commons.web.manager.TomcatManager;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.PasswordUtils;
import com.sonicle.security.auth.directory.ADConfigBuilder;
import com.sonicle.security.auth.directory.ADDirectory;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.security.auth.directory.ImapConfigBuilder;
import com.sonicle.security.auth.directory.ImapDirectory;
import com.sonicle.security.auth.directory.LdapConfigBuilder;
import com.sonicle.security.auth.directory.LdapDirectory;
import com.sonicle.security.auth.directory.LdapNethConfigBuilder;
import com.sonicle.security.auth.directory.LdapNethDirectory;
import com.sonicle.security.auth.directory.SftpConfigBuilder;
import com.sonicle.security.auth.directory.SftpDirectory;
import com.sonicle.security.auth.directory.SmbConfigBuilder;
import com.sonicle.security.auth.directory.SmbDirectory;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.app.auth.LdapWebTopConfigBuilder;
import com.sonicle.webtop.core.app.auth.LdapWebTopDirectory;
import com.sonicle.webtop.core.app.auth.WebTopConfigBuilder;
import com.sonicle.webtop.core.app.auth.WebTopDirectory;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.model.ParamsLdapDirectory;
import com.sonicle.webtop.core.io.FileResource;
import com.sonicle.webtop.core.io.JarFileResource;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.shiro.WTRealm;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sonicle.webtop.core.util.LoggerUtils;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarFile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.http.client.HttpClient;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.joda.time.DateTimeZone;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class WebTopApp {
	public static final Logger logger = WT.getLogger(WebTopApp.class);
	private static WebTopApp instance = null;
	private static final Object lockInstance = new Object();
	
	/**
	 * Gets WebTopApp object stored as context's attribute.
	 * @param request The http request
	 * @return WebTopApp object
	 * @throws javax.servlet.ServletException
	 */
	public static WebTopApp get(HttpServletRequest request) throws ServletException {
		try {
			return get(request.getServletContext());
		} catch(IllegalStateException ex) {
			throw new ServletException(ex.getMessage());
		}
	}
	
	/**
	 * Gets WebTopApp object stored as context's attribute.
	 * @param context The servlet context
	 * @return WebTopApp object
	 * @throws java.lang.IllegalStateException
	 */
	public static WebTopApp get(ServletContext context) throws IllegalStateException {
		WebTopApp wta = (WebTopApp)context.getAttribute(ContextLoader.WEBTOPAPP_ATTRIBUTE_KEY);
		if (wta == null) throw new IllegalStateException("WebTop environment is not loaded correctly. Please see log files for more details.");
		return wta;
	}
	
	public static WebTopApp getInstance() {
		synchronized(lockInstance) {
			return instance;
		}
	}
	
	public static final String DATASOURCES_CONFIG_PATH_PARAM = "dataSourcesConfigPath";
	public static final String DOMAINS_FOLDER = "domains";
	public static final String DBSCRIPTS_FOLDER = "dbscripts";
	public static final String DBSCRIPTS_POST_FOLDER = "post";
	public static final String TEMP_DOMAIN_FOLDER = "temp";
	public static final String IMAGES_DOMAIN_FOLDER = "images";
	public static final String SYSADMIN_DOMAIN_FOLDER = "_";
	
	private final ServletContext servletContext;
	private final String webappName;
	private final String osInfo;
	private final Charset systemCharset;
	private final DateTimeZone systemTimeZone;
	private Locale systemLocale;
	private StartupProperties startupProps;
	private final String webappConfigPath;
	private Subject adminSubject;
	private TomcatManager tomcat = null;
	private boolean webappIsLatest;
	private Timer webappVersionCheckTimer = null;
	
	private MediaTypes mediaTypes = null;
	private FileTypes fileTypes = null;
	private Configuration freemarkerCfg = null;
	private I18nManager i18nMgr = null;
	private ConnectionManager conMgr = null;
	private LogManager logMgr = null;
	private WebTopManager wtMgr = null;
	private SettingsManager setMgr = null;
	private ServiceManager svcMgr = null;
	private SessionManager sesMgr = null;
	private OTPManager otpMgr = null;
	private ReportManager rptMgr = null;
	private Scheduler scheduler = null;
	private final HashMap<String, Session> cacheMailSessionByDomain = new HashMap<>();
	private static final HashMap<String, ReadableUserAgent> cacheUserAgents =  new HashMap<>(); //TODO: decidere politica conservazion
	
	WebTopApp(ServletContext servletContext) {
		this.servletContext = servletContext;
		this.webappName = ContextUtils.getWebappName(servletContext);
		this.osInfo = OSInfo.build();
		this.systemCharset = Charset.forName("UTF-8");
		this.systemTimeZone = DateTimeZone.getDefault();
		
		// Ignore SSL checks for old commons-http components.
		// This is required in order to avoid error when accessing WebDAV 
		// secured servers throught vfs2.
		Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
		
		System.setProperty("net.fortuna.ical4j.timezone.update.enabled", "false");
		System.setProperty("mail.mime.decodetext.strict", "false");
		System.setProperty("mail.mime.decodefilename", "true");
		
		ICalendarUtils.setUnfoldingRelaxed(true);
		ICalendarUtils.setParsingRelaxed(true);
		ICalendarUtils.setValidationRelaxed(true);
		ICalendarUtils.setCompatibilityOutlook(true);
		ICalendarUtils.setCompatibilityNotes(true);
		
		this.startupProps = createStartupProperties();
		logger.info("webtop.extJsDebug = {}", startupProps.getExtJsDebug());
		logger.info("webtop.soExtDevMode = {}", startupProps.getSonicleExtJsExtensionsDevMode());
		logger.info("webtop.devMode = {}", startupProps.getDevMode());
		logger.info("webtop.debugMode = {}", startupProps.getDebugMode());
		logger.info("webtop.schedulerSisabled = {}", startupProps.getSchedulerDisabled());
		logger.info("webtop.webappsConfigPath = {}", startupProps.getWebappsConfigPath());
		
		//logger.info("getContextPath: {}", context.getContextPath());
		//logger.info("getServletContextName: {}", context.getServletContextName());
		//logger.info("getVirtualServerName: {}", context.getVirtualServerName());
		
		if (StringUtils.isBlank(startupProps.getWebappsConfigPath())) {
			this.webappConfigPath = null;
		} else {
			this.webappConfigPath = PathUtils.concatPaths(startupProps.getWebappsConfigPath(), ContextUtils.getWebappName(servletContext, true));
		}
		this.adminSubject = buildSysAdminSubject();
	}
	
	public void init() {
		synchronized(lockInstance) {
			ThreadState threadState = new SubjectThreadState(adminSubject);
			try {
				threadState.bind();
				internalInit();
			} finally {
				threadState.clear();
			}
			instance = this;
		}	
		
		new Timer("onAppReady").schedule(new TimerTask() {
			@Override
			public void run() {
				ThreadState threadState = new SubjectThreadState(adminSubject);
				try {
					LoggerUtils.initDC(webappName);
					threadState.bind();
					onAppReady();
				} catch(InterruptedException ex) {
					// Do nothing...
				} finally {
					threadState.clear();
				}
			}
		}, 5000);
	}
	
	public void destroy() {
		synchronized(lockInstance) {
			ThreadState threadState = new SubjectThreadState(adminSubject);
			try {
				threadState.bind();
				internalDestroy();
			} finally {
				threadState.clear();
			}
			instance = null;
		}
	}
	
	
	
	private Subject buildSysAdminSubject() {
		SecurityUtils.setSecurityManager(new DefaultSecurityManager(new WTRealm()));
		return RunContext.buildSubject(new UserProfileId(WebTopManager.SYSADMIN_DOMAINID, WebTopManager.SYSADMIN_USERID));
	}
	
	Subject getAdminSubject() {
		return adminSubject;
	}
	
	
	
	private void internalInit() {
		this.webappIsLatest = false;
		
		HttpClient httpcli = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			URI uri = new URI("http://www.sonicle.com/images/empty.png");
			httpcli = HttpClientUtils.createBasicHttpClient(HttpClientUtils.configureSSLAcceptAll(), uri);
			HttpClientUtils.get(httpcli, uri, baos);
		} catch(Throwable t) {
		} finally {
			HttpClientUtils.closeQuietly(httpcli);
		}
		
		try {
			initVFSManager();
		} catch(FileSystemException ex) {
			throw new WTRuntimeException(ex, "Error initializing VFS");
		}
		
		logger.info("WTA initialization started [{}]", webappName);
		this.conMgr = ConnectionManager.initialize(this, webappConfigPath); // Connection Manager
		this.setMgr = SettingsManager.initialize(this); // Settings Manager
		this.sesMgr = SessionManager.initialize(this); // Session Manager
		
		// Checks home directory
		logger.info("Checking home structure...");
		File homeDir = new File(getHomePath());
		if (!homeDir.exists()) throw new WTRuntimeException("Configured home directory not found [{0}]", homeDir.toString());
		checkHomeStructure();
		
		this.mediaTypes = MediaTypes.init(conMgr);
		this.fileTypes = FileTypes.init(conMgr);
		
		// Locale Manager
		//TODO: caricare dinamicamente le lingue installate nel sistema
		String[] tags = new String[]{"it_IT", "en_EN", "es_ES", "de_DE"};
		this.i18nMgr = I18nManager.initialize(this, tags);
		
		// Template Engine
		logger.info("Initializing template engine");
		this.freemarkerCfg = new Configuration();
		this.freemarkerCfg.setClassForTemplateLoading(this.getClass(), "/");
		this.freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
		this.freemarkerCfg.setDefaultEncoding(getSystemCharset().name());
		
		//comm = ComponentsManager.initialize(this); // Components Manager
		this.logMgr = LogManager.initialize(this); // Log Manager
		this.wtMgr = WebTopManager.initialize(this); // WT Manager
		
		this.systemLocale = CoreServiceSettings.getSystemLocale(setMgr); // System locale
		this.otpMgr = OTPManager.initialize(this); // OTP Manager
		this.rptMgr = ReportManager.initialize(this); // Report Manager
		
		// Scheduler (services manager requires this component for jobs)
		try {
			//TODO: gestire le opzioni di configurazione dello scheduler
			Properties quartzProps = new Properties();
			quartzProps.put("org.quartz.scheduler.skipUpdateCheck", true);
			quartzProps.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
			quartzProps.put("org.quartz.threadPool.threadCount", "10");
			quartzProps.put("org.quartz.threadPool.threadPriority", "5");
			quartzProps.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
			
			this.scheduler = new StdSchedulerFactory(quartzProps).getScheduler();
			if (startupProps.getSchedulerDisabled()) {
				logger.warn("Scheduler startup forcibly disabled");
			} else {
				this.scheduler.start();
			}
		} catch(SchedulerException ex) {
			throw new WTRuntimeException(ex, "Unable to start scheduler");
		}
		
		this.svcMgr = ServiceManager.initialize(this, this.scheduler); // Service Manager
		
		logger.info("WTA initialization completed [{}]", webappName);
	}
	
	private void internalDestroy() {
		logger.info("WTA shutdown started [{}]", webappName);
		
		clearWebappVersionCheckTask();
		tomcat = null;
		
		// Service Manager
		svcMgr.cleanup();
		svcMgr = null;
		// Session Manager
		sesMgr.cleanup();
		sesMgr = null;
		// Scheduler
		try {
			scheduler.shutdown(true);
			scheduler = null;
		} catch(SchedulerException ex) {
			logger.error("Error shutting-down scheduler", ex);
		}
		// Report Manager
		rptMgr.cleanup();
		rptMgr = null;
		// OTP Manager
		otpMgr.cleanup();
		otpMgr = null;
		// Settings Manager
		setMgr.cleanup();
		setMgr = null;
		// Auth Manager
		//autm.cleanup();
		//autm = null;
		// User Manager
		wtMgr.cleanup();
		wtMgr = null;
		// Connection Manager
		conMgr.cleanup();
		conMgr = null;
		// I18nManager Manager
		i18nMgr.cleanup();
		i18nMgr = null;
		
		logger.info("WTA shutdown completed [{}]", webappName);
	}
	
	private void onAppReady() throws InterruptedException {
		logger.debug("onAppReady...");
		try {
			logger.info("Checking domains homes structure...");
			try {
				checkDomainsHomesStructure();
			} catch(WTException ex) {
				logger.error("Error", ex);
			}
			
			/*
			try {
				initCacheDomainByFQDN();
			} catch(WTException ex) {
				logger.warn("Unable to create domains FQDN cache", ex);
			}
			*/
			
			// Check webapp version
			logger.info("Checking webapp version...");
			//String tomcatUri = "http://tomcat:tomcat@localhost:8084/manager/text";
			String tomcatUri = CoreServiceSettings.getTomcatManagerUri(setMgr);
			if (StringUtils.isBlank(tomcatUri)) {
				logger.warn("No configuration found for TomcatManager [{}]", CoreSettings.TOMCAT_MANAGER_URI);
				this.webappIsLatest = true;
			} else {
				try {
					this.tomcat = new TomcatManager(tomcatUri);
					this.tomcat.testConnection();
					this.webappIsLatest = checkIsLastestWebapp(webappName);
					scheduleWebappVersionCheckTask();
					
				} catch(URISyntaxException ex1) {
					logger.warn("Invalid configuration for TomcatManager [{}]", CoreSettings.TOMCAT_MANAGER_URI);
					this.webappIsLatest = false;
				} catch(Exception ex1) {
					logger.error("Error connecting to TomcatManager", ex1);
					this.webappIsLatest = false;
				}
			}
			if (webappVersionCheckTimer == null) {
				logger.warn("Webapp version automatic check will NOT be performed!");
			}
			if (webappIsLatest) {
				logger.info("This webapp [{}] is the latest", webappName);
			} else {
				logger.info("This webapp [{}] is NOT the latest", webappName);
			}
			
			svcMgr.initializeJobServices();
			if (isLatest()) {
				// Sleep a little bit for avoid concurrency with other webapp checks
				logger.debug("Waiting 60sec before continue...");
				Thread.sleep(60000);
				if (isLatest()) {
					try {
						logger.info("Scheduling JobServices tasks...");
						svcMgr.scheduleAllJobServicesTasks();
						if (!scheduler.isStarted()) logger.warn("Tasks succesfully scheduled but scheduler is not running");
					} catch (SchedulerException ex) {
						logger.error("Error", ex);
					}
				}
			}
			
		} catch(IllegalStateException ex) {
			// Due to NB redeploys in development...simply ignore this!
		}
	}
	
	private void initVFSManager() throws FileSystemException {
		WebTopVFSManager vfsMgr = new WebTopVFSManager();
		vfsMgr.addProvider("file", new org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider());
		vfsMgr.addProvider("ftp", new org.apache.commons.vfs2.provider.ftp.FtpFileProvider());
		vfsMgr.addProvider("ftps", new org.apache.commons.vfs2.provider.ftps.FtpsFileProvider());
		vfsMgr.addProvider("sftp", new org.apache.commons.vfs2.provider.sftp.SftpFileProvider());
		vfsMgr.addProvider("http", new org.apache.commons.vfs2.provider.http.HttpFileProvider());
		vfsMgr.addProvider("https", new org.apache.commons.vfs2.provider.https.HttpsFileProvider());
		vfsMgr.addProvider("dropbox", new com.sonicle.vfs2.provider.dropbox.DbxFileProvider());
		vfsMgr.addProvider("googledrive", new com.sonicle.vfs2.provider.googledrive.GDriveFileProvider());
		vfsMgr.addProvider("webdav", new com.sonicle.vfs2.provider.webdav.WebdavFileProvider());
		vfsMgr.addProvider("webdavs", new com.sonicle.vfs2.provider.webdavs.WebdavsFileProvider());
		vfsMgr.addProvider("smb", new org.apache.commons.vfs2.provider.smb.SmbFileProvider());
		vfsMgr.init();
		VFS.setManager(vfsMgr);
	}
	
	private void checkHomeStructure() {
		try {
			File dbScriptsDir = new File(getDbScriptsPath());
			if (!dbScriptsDir.exists()) {
				dbScriptsDir.mkdir();
				logger.trace("{} created", dbScriptsDir.toString());
			}
			
			File domainsDir = new File(getDomainsPath());
			if (!domainsDir.exists()) {
				domainsDir.mkdir();
				logger.trace("{} created", domainsDir.toString());
			}
			
		} catch(SecurityException ex) {
			throw new WTRuntimeException("Security error", ex);
		}	
	}
	
	private void checkDomainsHomesStructure() throws WTException {
		wtMgr.initDomainHomeFolder(WebTopManager.SYSADMIN_DOMAINID);
		
		List<ODomain> domains = wtMgr.listDomains(false);
		for (ODomain domain : domains) {
			try {
				wtMgr.initDomainHomeFolder(domain.getDomainId());
			} catch(SecurityException ex) {
				logger.warn("Unable to check domain home [{}]", domain.getDomainId(), ex);
			}
		}
	}
	
	private void scheduleWebappVersionCheckTask() {
		long period = 60000;
		webappVersionCheckTimer = new Timer("webappVersionCheck");
		webappVersionCheckTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				ThreadState threadState = new SubjectThreadState(adminSubject);
				try {
					threadState.bind();
					instance.onWebappVersionCheck();
				} finally {
					threadState.clear();
				}
			}
		}, period, period);
		logger.info("Task 'webappVersionCheck' scheduled [{}sec]", period/1000);
	}
	
	private void clearWebappVersionCheckTask() {
		if (webappVersionCheckTimer != null) {
			webappVersionCheckTimer.cancel();
		}
		webappVersionCheckTimer = null;
		logger.info("Task 'webappVersionCheck' destroyed");
	}
	
	private void onWebappVersionCheck() {
		logger.debug("onWebappVersionCheck...");
		if (tomcat == null) return;
		
		logger.info("Checking webapp version...");
		boolean oldLatest = webappIsLatest;
		webappIsLatest = checkIsLastestWebapp(webappName);
		if (webappIsLatest && !oldLatest) {
			logger.info("App instance [{}] is the latest", webappName);
			svcMgr.scheduleAllJobServicesTasks();
		} else if (!webappIsLatest && oldLatest) {
			logger.info("App instance [{}] is NO more the latest", webappName);
		} else {
			logger.debug("No version changes found!");
		}
	}
	
	private boolean checkIsLastestWebapp(String appName) {
		try {
			List<TomcatManager.DeployedApp> apps = tomcat.listDeployedApplications(StringUtils.substringBefore(appName, "##"));
			return !apps.isEmpty() && appName.equals(apps.get(0).name);
			
		} catch(Exception ex) {
			logger.error("Unable to query TomcatManager", ex);
			return false;
		}
	}
	
	
	
	
	
	private CoreServiceSettings getCoreServiceSettings() {
		return new CoreServiceSettings(setMgr, CoreManifest.ID, WebTopManager.SYSADMIN_DOMAINID);
	}
	
	private StartupProperties createStartupProperties() {
		final String PREFIX = "com.sonicle.webtop.";
		Properties props = new Properties();
		String prop = null;
		
		prop = System.getProperty(PREFIX + StartupProperties.PROP_EXTJS_DEBUG, null);
		if (prop == null) prop = System.getProperty(PREFIX + StartupProperties.PROP_EXTJS_DEBUG.toLowerCase(), null);
		if (prop != null) props.setProperty(StartupProperties.PROP_EXTJS_DEBUG, prop);
		
		prop = System.getProperty(PREFIX + StartupProperties.PROP_SO_EXT_DEV_MODE, null);
		if (prop == null) prop = System.getProperty(PREFIX + StartupProperties.PROP_SO_EXT_DEV_MODE.toLowerCase(), null);
		if (prop != null) props.setProperty(StartupProperties.PROP_SO_EXT_DEV_MODE, prop);
		
		prop = System.getProperty(PREFIX + StartupProperties.PROP_DEV_MODE, null);
		if (prop == null) prop = System.getProperty(PREFIX + StartupProperties.PROP_DEV_MODE.toLowerCase(), null);
		if (prop != null) props.setProperty(StartupProperties.PROP_DEV_MODE, prop);
		
		prop = System.getProperty(PREFIX + StartupProperties.PROP_DEBUG_MODE, null);
		if (prop == null) prop = System.getProperty(PREFIX + StartupProperties.PROP_DEBUG_MODE.toLowerCase(), null);
		if (prop != null) props.setProperty(StartupProperties.PROP_DEBUG_MODE, prop);
		
		prop = System.getProperty(PREFIX + StartupProperties.PROP_SCHEDULER_DISABLED, null);
		if (prop == null) prop = System.getProperty(PREFIX + StartupProperties.PROP_SCHEDULER_DISABLED.toLowerCase(), null);
		if (prop != null) props.setProperty(StartupProperties.PROP_SCHEDULER_DISABLED, prop);
		
		prop = System.getProperty(PREFIX + StartupProperties.PROP_WEBAPPS_CONFIG_PATH, null);
		if (prop == null) prop = System.getProperty(PREFIX + StartupProperties.PROP_WEBAPPS_CONFIG_PATH.toLowerCase(), null);
		if (prop != null) props.setProperty(StartupProperties.PROP_WEBAPPS_CONFIG_PATH, prop);
		//context.getInitParameter
		
		return new StartupProperties(props);
	}
	
	/**
	 * Returns webapp's name as configured in the application server.
	 * @return Webapp's name
	 */
	public String getWebappName() {
		return webappName;
	}
	
	public String getAppServerInfo() {
		return servletContext.getServerInfo();
	}
	
	public String getOSInfo() {
		return osInfo;
	}
	
	public Charset getSystemCharset() {
		return systemCharset;
	}
	
	public DateTimeZone getSystemTimeZone() {
		return systemTimeZone;
	}
	
	public Locale getSystemLocale() {
		return systemLocale;
	}
	
	public StartupProperties getStartupProperties() {
		return startupProps;
	}
	
	public String getWebappConfigPath() {
		return webappConfigPath;
	}
	
	/**
	 * Checks if this webapp is the latest version in the application server.
	 * @return True if this is the last version, false otherwise.
	 */
	public boolean isLatest() {
		return webappIsLatest;
	}
	
	public String getPlatformName() {
		//TODO: completare rebranding aggiungendo impostazione per override del nome
		return "WebTop";
	}
	
	public MediaTypes getMediaTypes() {
		return mediaTypes;
	}
	
	public FileTypes getFileTypes() {
		return fileTypes;
	}
	
	/**
	 * Returns the I18nManager.
	 * @return I18nManager instance.
	 */
	public I18nManager getI18nManager() {
		return i18nMgr;
	}
	
	/**
	 * Returns the ConnectionManager.
	 * @return ConnectionManager instance.
	 */
	public ConnectionManager getConnectionManager() {
		return conMgr;
	}
	
	/**
	 * Returns the SettingsManager.
	 * @return SettingsManager instance.
	 */
	public SettingsManager getSettingsManager() {
		return setMgr;
	}
	
	/**
	 * Returns the LogManager.
	 * @return UserManager instance.
	 */
	public LogManager getLogManager() {
		return logMgr;
	}
	
	/**
	 * Returns the WebTopManager.
	 * @return WebTopManager instance.
	 */
	public WebTopManager getWebTopManager() {
		return wtMgr;
	}
	
	/**
	 * Returns the ServiceManager.
	 * @return ServiceManager instance.
	 */
	public ServiceManager getServiceManager() {
		return svcMgr;
	}
	
	/**
	 * Returns the OTPManager.
	 * @return OTPManager instance.
	 */
	public OTPManager getOTPManager() {
		return otpMgr;
	}
	
	/**
	 * Returns the ReportManager.
	 * @return ReportManager instance.
	 */
	public ReportManager getReportManager() {
		return rptMgr;
	}
	
	/**
	 * Returns the SessionManager.
	 * @return SessionManager instance.
	 */
	public SessionManager getSessionManager() {
		return sesMgr;
	}
	
	/**
	 * Parses a User-Agent HTTP Header string looking for useful client information.
	 * @param userAgentHeader HTTP Header string.
	 * @return Object representation of the parsed string.
	 */
	public static ReadableUserAgent getUserAgentInfo(String userAgentHeader) {
		String hash = DigestUtils.md5Hex(userAgentHeader);
		synchronized(cacheUserAgents) {
			if(cacheUserAgents.containsKey(hash)) {
				return cacheUserAgents.get(hash);
			} else {
				UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
				ReadableUserAgent rua = parser.parse(userAgentHeader);
				cacheUserAgents.put(hash, rua);
				return rua;
			}
		}
	}
	
	public String getContextResourcePath(String resource) {
		return servletContext.getRealPath(resource);
	}
	
	/*
	public URL getContextResource(String resource) throws MalformedURLException {
		return servletContext.getResource(resource);
	}
	*/
	
	public Template loadTemplate(String path) throws IOException {
		return freemarkerCfg.getTemplate(path, getSystemCharset().name());
	}
	
	public Template loadTemplate(String path, String encoding) throws IOException {
		return freemarkerCfg.getTemplate(path, encoding);
	}
	
	/**
	 * Return the configured HOME path for the platform.
	 * Path will be followed by the Unix style trailing separator.
	 * @return The HOME path
	 */
	public String getHomePath() {
		CoreServiceSettings css = getCoreServiceSettings();
		return css.getHomePath();
	}
	
	private String getDbScriptsPath() {
		CoreServiceSettings css = getCoreServiceSettings();
		return css.getHomePath() + DBSCRIPTS_FOLDER + "/";
	}
	
	/**
	 * Return the db-scripts HOME path for the passed Service.
	 * @param serviceId The service ID.
	 * @return The path
	 */
	public String getDbScriptsHomePath(String serviceId) {
		CoreServiceSettings css = getCoreServiceSettings();
		return css.getHomePath() + DBSCRIPTS_FOLDER + "/" + serviceId + "/";
	}
	
	/**
	 * Returns the POST db-scripts path for the passed Service.
	 * @param serviceId The service ID.
	 * @return The path
	 */
	public String getDbScriptsPostPath(String serviceId) {
		return getDbScriptsHomePath(serviceId) + DBSCRIPTS_POST_FOLDER + "/";
	}
	
	private String getDomainsPath() {
		CoreServiceSettings css = getCoreServiceSettings();
		return css.getHomePath() + DOMAINS_FOLDER + "/";
	}
	
	/**
	 * Return the HOME path for the passed Domain.
	 * Path will be followed by the Unix style trailing separator.
	 * @param domainId The domain ID.
	 * @return The path
	 */
	public String getHomePath(String domainId) {
		CoreServiceSettings css = getCoreServiceSettings();
		if (StringUtils.equals(domainId, WebTopManager.SYSADMIN_DOMAINID)) {
			return css.getHomePath() + DOMAINS_FOLDER + "/" + SYSADMIN_DOMAIN_FOLDER + "/";
		} else {
			return css.getHomePath() + DOMAINS_FOLDER + "/" + domainId + "/";
		}
	}
	
	/**
	 * Return the TEMP path (relative to the HOME) for the passed Domain.
	 * Path will be followed by the Unix style trailing separator.
	 * @param domainId The domain ID.
	 * @return The path
	 */
	public String getTempPath(String domainId) {
		return getHomePath(domainId) + TEMP_DOMAIN_FOLDER + "/";
	}
	
	public String getImagesPath(String domainId) {
		return getHomePath(domainId) + IMAGES_DOMAIN_FOLDER + "/";
	}
	
	/**
	 * Return service's HOME path for the passed Domain.
	 * Path will be followed by the Unix style trailing separator.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @return The path
	 */
	public String getServiceHomePath(String domainId, String serviceId) {
		return getHomePath(domainId) + serviceId + "/";
	}
	
	public String getPublicPath(String domainId) {
		return getHomePath(domainId) + "public/";
	}
	
	public File getTempFolder(String domainId) throws WTException {
		File tempDir = new File(getTempPath(domainId));
		if(!tempDir.isDirectory() || !tempDir.canWrite()) {
			throw new WTException("Temp folder is not a directory or is write protected");
		}
		return tempDir;
	}
	
	public File createTempFile(String domainId) throws WTException {
		return createTempFile(domainId, null, null);
	}
	
	public File createTempFile(String domainId, String prefix, String suffix) throws WTException {
		return new File(getTempFolder(domainId), buildTempFilename(prefix, suffix));
	}
	
	public boolean deleteTempFile(String domainId, String filename) throws WTException {
		File tempFile = new File(getTempFolder(domainId), filename);
		return tempFile.delete();
	}
	
	public String buildTempFilename() {
		return buildTempFilename(null, null);
	}
	
	public String buildTempFilename(String prefix, String suffix) {
		String uuid = IdentifierUtils.getUUID();
		if(StringUtils.isBlank(suffix)) {
			return MessageFormat.format("{0}{1}", StringUtils.defaultString(prefix), uuid);
		} else {
			return MessageFormat.format("{0}{1}.{2}", StringUtils.defaultString(prefix), uuid, suffix);
		}
	}
	
	public FileResource getFileResource(URL url) throws URISyntaxException, MalformedURLException {
		if(!url.getProtocol().equals("file")) throw new MalformedURLException("Protocol must be 'file'");
		File file = new File(url.toURI());
		if(file.exists() && file.isFile()) {
			return new FileResource(file);
		} else {
			return null;
		}
	}
	
	public JarFileResource getJarResource(URL url) throws URISyntaxException, MalformedURLException, IOException {
		if(!url.getProtocol().equals("jar")) throw new MalformedURLException("Protocol must be 'jar'");
		
		String surl = url.toString();
		int ix = surl.lastIndexOf("!/");
		if (ix < 0) throw new MalformedURLException("URL must contains '!/'");
		
		String jarFileName, jarEntryName;
		try {
			jarFileName = URLDecoder.decode(surl.substring(4 + 5, ix), getSystemCharset().name());
			jarEntryName = surl.substring(ix + 2);
		} catch(UnsupportedEncodingException ex) {
			throw new WTRuntimeException(ex, "{0} encoding not supported", getSystemCharset().name());
		}
		
		File file = new File(jarFileName);
		return new JarFileResource(new JarFile(file), jarEntryName);
	}
	
	public String getPublicBaseUrl(String domainId) {
		return new CoreServiceSettings(CoreManifest.ID, domainId).getPublicBaseUrl();
	}
	
	/**
	 * Returns the localized string for Core service bound to the specified key.
	 * @param locale Desired locale.
	 * @param key Resource key.
	 * @return Localized string
	 */
	public String lookupResource(Locale locale, String key) {
		return lookupResource(CoreManifest.ID, locale, key, false);
	}
	
	/**
	 * Returns the localized string for Core service bound to the specified key.
	 * @param locale Desired locale.
	 * @param key Resource key.
	 * @param escapeHtml True to apply HTML escaping, false otherwise.
	 * @return Localized string
	 */
	public String lookupResource(Locale locale, String key, boolean escapeHtml) {
		return lookupResource(CoreManifest.ID, locale, key, escapeHtml);
	}
	
	/**
	 * Returns the localized string for desired service and bound to the specified key.
	 * @param serviceId The service ID.
	 * @param locale Desired locale.
	 * @param key Resource key.
	 * @return Localized string
	 */
	public String lookupResource(String serviceId, Locale locale, String key) {
		return lookupResource(serviceId, locale, key, false);
	}
	
	/**
	 * Returns the localized string for desired service and bound to the specified key.
	 * @param serviceId The service ID.
	 * @param locale Desired locale.
	 * @param key Resource key.
	 * @param escapeHtml True to apply HTML escaping, false otherwise.
	 * @return Localized string
	 */
	public String lookupResource(String serviceId, Locale locale, String key, boolean escapeHtml) {
		try {
			String baseName = StringUtils.replace(serviceId, ".", "/") + "/locale";
			String value = ResourceBundle.getBundle(baseName, locale).getString(key);
			if(escapeHtml) value = StringEscapeUtils.escapeHtml4(value);
			return value;
			
		} catch(MissingResourceException ex) {
			return key;
			//logger.trace("Missing resource [{}, {}, {}]", baseName, locale.toString(), key, ex);
		}
	}
	
	/**
	 * Returns the localized string for Core service and bound to the specified key.
	 * This method formats returned string using passed arguments.
	 * @param locale Desired locale.
	 * @param key Resource key.
	 * @param escapeHtml True to apply HTML escaping, false otherwise.
	 * @param arguments Arguments to use within MessageFormat.format
	 * @return Localized string
	 */
	public String lookupAndFormatResource(Locale locale, String key, boolean escapeHtml, Object... arguments) {
		return lookupAndFormatResource(CoreManifest.ID, locale, key, escapeHtml, arguments);
	}
	
	/**
	 * Returns the localized string for desired service and bound to the specified key.
	 * This method formats returned string using passed arguments.
	 * @param serviceId The service ID.
	 * @param locale Desired locale.
	 * @param key Resource key.
	 * @param escapeHtml True to apply HTML escaping, false otherwise.
	 * @param arguments Arguments to use within MessageFormat.format
	 * @return Localized string
	 */
	public String lookupAndFormatResource(String serviceId, Locale locale, String key, boolean escapeHtml, Object... arguments) {
		String value = lookupResource(serviceId, locale, key, escapeHtml);
		return MessageFormat.format(value, arguments);
	}
	
	public Session getGlobalMailSession(String domainId) {
		Session session;
		synchronized(cacheMailSessionByDomain) {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, domainId);
			String smtphost=css.getSMTPHost();
			int smtpport=css.getSMTPPort();
			String key=smtphost+":"+smtpport;
			session=cacheMailSessionByDomain.get(key);
			if (session==null) {
				Properties props = new Properties(System.getProperties());
				//props.setProperty("mail.imap.parse.debug", "true");
				props.setProperty("mail.smtp.host", smtphost);
				props.setProperty("mail.smtp.port", ""+smtpport);
				//props.setProperty("mail.socket.debug", "true");
				props.setProperty("mail.imaps.ssl.trust", "*");
				props.setProperty("mail.imap.folder.class", "com.sonicle.mail.imap.SonicleIMAPFolder");
				props.setProperty("mail.imaps.folder.class", "com.sonicle.mail.imap.SonicleIMAPFolder");
				//support idle events
				props.setProperty("mail.imap.enableimapevents", "true");
				
				session=Session.getInstance(props, null);
				cacheMailSessionByDomain.put(key,session);
				
				logger.info("Created javax.mail.Session for "+key);
			}
		}
		return session;
	}
	
	public void sendEmail(javax.mail.Session session, boolean rich, 
			String from, String[] to, String[] cc, String[] bcc, 
			String subject, String body) throws MessagingException {
		
		InternetAddress iafrom=InternetAddressUtils.toInternetAddress(from);
		InternetAddress iato[]=null;
		InternetAddress iacc[]=null;
		InternetAddress iabcc[]=null;
		
        if (to!=null) {
			iato=new InternetAddress[to.length];
			int i=0;
            for(String addr: to) {
                iato[i++]=InternetAddressUtils.toInternetAddress(addr);
            }
		}
		
        if (cc!=null) {
			iacc=new InternetAddress[cc.length];
			int i=0;
            for(String addr: cc) {
                iacc[i++]=InternetAddressUtils.toInternetAddress(addr);
            }
		}
		
        if (bcc!=null) {
			iabcc=new InternetAddress[bcc.length];
			int i=0;
            for(String addr: bcc) {
                iabcc[i++]=InternetAddressUtils.toInternetAddress(addr);
            }
		}
		
		sendEmail(session,rich,iafrom,iato,iacc,iabcc,subject,body,null);
		
	}
	
	public void sendEmail(javax.mail.Session session, boolean rich, 
			InternetAddress from, InternetAddress[] to, InternetAddress[] cc, InternetAddress[] bcc, 
				String subject, String body, MimeBodyPart[] parts) throws MessagingException {
		
		//Session session=getGlobalMailSession(pid.getDomainId());
        MimeMessage msg=new MimeMessage(session);
        try {
          subject=MimeUtility.encodeText(subject);
        } catch(Exception exc) {}
        msg.setSubject(subject);
        msg.addFrom(new InternetAddress[] { from });
        
        if (to!=null)
            for(InternetAddress addr: to) {
                msg.addRecipient(Message.RecipientType.TO, addr);
            }
        
        if (cc!=null)
            for(InternetAddress addr: cc) {
                msg.addRecipient(Message.RecipientType.CC, addr);
            }
        
        if (bcc!=null)
            for(InternetAddress addr: bcc) {
                msg.addRecipient(Message.RecipientType.BCC, addr);
            }
        
        MimeMultipart mp=new MimeMultipart("mixed");
		if (rich) {
			MimeMultipart alternative=new MimeMultipart("alternative");
			MimeBodyPart mbp2=new MimeBodyPart();
			mbp2.setContent(body, MailUtils.buildPartContentType("text/html", "UTF-8"));
			MimeBodyPart mbp1=new MimeBodyPart();
			mbp1.setContent(MailUtils.htmlToText(MailUtils.htmlunescapesource(body)), MailUtils.buildPartContentType("text/plain", "UTF-8"));
			alternative.addBodyPart(mbp1);
			alternative.addBodyPart(mbp2);
			MimeBodyPart altbody=new MimeBodyPart();
			altbody.setContent(alternative);
			mp.addBodyPart(altbody);
		} else {
			MimeBodyPart mbp1=new MimeBodyPart();
			mbp1.setContent(body, MailUtils.buildPartContentType("text/plain", "UTF-8"));
			mp.addBodyPart(mbp1);
		}
		
		if (parts!=null) {
			for(MimeBodyPart part: parts)
				mp.addBodyPart(part);
		}
		
        msg.setContent(mp);
        
        msg.setSentDate(new java.util.Date());
        
        Transport.send(msg);
	}
	
	public void sendEmail(javax.mail.Session session, boolean rich, InternetAddress from, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String subject, String body, Collection<MimeBodyPart> parts) throws MessagingException {
		MimeMultipart mp = new MimeMultipart("mixed");
		if (rich) {
			MimeMultipart alternative = new MimeMultipart("alternative");
			MimeBodyPart mbp2 = new MimeBodyPart();
			mbp2.setContent(body, MailUtils.buildPartContentType("text/html", "UTF-8"));
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setContent(MailUtils.htmlToText(MailUtils.htmlunescapesource(body)), MailUtils.buildPartContentType("text/plain", "UTF-8"));
			alternative.addBodyPart(mbp1);
			alternative.addBodyPart(mbp2);
			MimeBodyPart altbody = new MimeBodyPart();
			altbody.setContent(alternative);
			mp.addBodyPart(altbody);
		} else {
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setContent(body, MailUtils.buildPartContentType("text/plain", "UTF-8"));
			mp.addBodyPart(mbp1);
		}
		sendEmail(session, from, to, cc, bcc, subject, mp);
	}
	
	public void sendEmail(javax.mail.Session session, String from, Collection<String> to, Collection<String> cc, Collection<String> bcc, String subject, MimeMultipart part) throws MessagingException {
		InternetAddress iaFrom = InternetAddressUtils.toInternetAddress(from);
		ArrayList<InternetAddress> iaTo = null;
		ArrayList<InternetAddress> iaCc = null;
		ArrayList<InternetAddress> iaBcc = null;
		
		if (to != null) {
			iaTo = new ArrayList<>(to.size());
			for (String s : to) iaTo.add(InternetAddressUtils.toInternetAddress(s));
		}
        if (cc != null) {
			iaCc = new ArrayList<>(cc.size());
			for (String s : cc) iaCc.add(InternetAddressUtils.toInternetAddress(s));
		}
        if (bcc != null) {
			iaBcc = new ArrayList<>(bcc.size());
			for (String s : bcc) iaBcc.add(InternetAddressUtils.toInternetAddress(s));
		}
		
		sendEmail(session, iaFrom, iaTo, iaCc, iaBcc, subject, part);
	}
	
	public void sendEmail(javax.mail.Session session, InternetAddress from, Collection<InternetAddress> to, Collection<InternetAddress> cc, Collection<InternetAddress> bcc, String subject, MimeMultipart part) throws MessagingException {
		
		try {
			subject = MimeUtility.encodeText(subject);
		} catch (Exception ex) {}
		
		MimeMessage message = new MimeMessage(session);
		message.setSubject(subject);
		message.addFrom(new InternetAddress[] {from});
		
		if (to != null) {
			for(InternetAddress ia: to) message.addRecipient(Message.RecipientType.TO, ia);
		}
		if (cc != null) {
			for(InternetAddress ia: cc) message.addRecipient(Message.RecipientType.CC, ia);
		}
		if (bcc != null) {
			for(InternetAddress ia: bcc) message.addRecipient(Message.RecipientType.BCC, ia);
		}
		
		message.setContent(part);
		message.setSentDate(new java.util.Date());
		Transport.send(message);
	}	
	
	public void notify(UserProfileId profileId, List<ServiceMessage> messages, boolean enqueueIfOffline) {
		sesMgr.push(profileId, messages, enqueueIfOffline);
	}
	
	public DirectoryOptions createDirectoryOptions(AuthenticationDomain ad) {
		DirectoryOptions opts = new DirectoryOptions();
		ParamsLdapDirectory params = null;
		
		URI authUri = ad.getDirUri();
		switch(authUri.getScheme()) {
			case WebTopDirectory.SCHEME:
				WebTopConfigBuilder wt = new WebTopConfigBuilder();
				wt.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				wt.setWebTopApp(opts, this);
				break;
			case LdapWebTopDirectory.SCHEME:
				LdapWebTopConfigBuilder ldapwt = new LdapWebTopConfigBuilder();
				ldapwt.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				ldapwt.setHost(opts, authUri.getHost());
				ldapwt.setPort(opts, authUri.getPort());
				ldapwt.setConnectionSecurity(opts, ad.getDirConnSecurity());
				ldapwt.setSpecificAdminDn(opts, ad.getDirAdmin(), ad.getInternetName());
				ldapwt.setAdminPassword(opts, getDirPassword(ad));
				ldapwt.setSpecificLoginDn(opts, ad.getInternetName());
				ldapwt.setSpecificUserDn(opts, ad.getInternetName());
				break;
			case LdapDirectory.SCHEME:
				params = LangUtils.deserialize(ad.getDirParameters(), new ParamsLdapDirectory(), ParamsLdapDirectory.class);
				LdapConfigBuilder ldap = new LdapConfigBuilder();
				ldap.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				ldap.setHost(opts, authUri.getHost());
				ldap.setPort(opts, authUri.getPort());
				ldap.setConnectionSecurity(opts, ad.getDirConnSecurity());
				ldap.setAdminDn(opts, ad.getDirAdmin());
				ldap.setAdminPassword(opts, getDirPassword(ad));
				ldap.setIsCaseSensitive(opts, webappIsLatest);
				if (!StringUtils.isBlank(params.loginDn)) ldap.setLoginDn(opts, params.loginDn);
				if (!StringUtils.isBlank(params.loginFilter)) ldap.setLoginFilter(opts, params.loginFilter);
				if (!StringUtils.isBlank(params.userDn)) ldap.setUserDn(opts, params.userDn);
				if (!StringUtils.isBlank(params.userFilter)) ldap.setUserFilter(opts, params.userFilter);
				if (!StringUtils.isBlank(params.userIdField)) ldap.setUserIdField(opts, params.userIdField);
				if (!StringUtils.isBlank(params.userFirstnameField)) ldap.setUserFirstnameField(opts, params.userFirstnameField);
				if (!StringUtils.isBlank(params.userLastnameField)) ldap.setUserLastnameField(opts, params.userLastnameField);
				if (!StringUtils.isBlank(params.userDisplayNameField)) ldap.setUserDisplayNameField(opts, params.userDisplayNameField);
				break;
			case LdapNethDirectory.SCHEME:
				params = LangUtils.deserialize(ad.getDirParameters(), new ParamsLdapDirectory(), ParamsLdapDirectory.class);
				LdapNethConfigBuilder ldapnts = new LdapNethConfigBuilder();
				ldapnts.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				ldapnts.setHost(opts, authUri.getHost());
				ldapnts.setPort(opts, authUri.getPort());
				ldapnts.setConnectionSecurity(opts, ad.getDirConnSecurity());
				ldapnts.setAdminDn(opts, ad.getDirAdmin());
				ldapnts.setAdminPassword(opts, getDirPassword(ad));
				if (!StringUtils.isBlank(params.loginDn)) ldapnts.setLoginDn(opts, params.loginDn);
				if (!StringUtils.isBlank(params.loginFilter)) ldapnts.setLoginFilter(opts, params.loginFilter);
				if (!StringUtils.isBlank(params.userDn)) ldapnts.setUserDn(opts, params.userDn);
				if (!StringUtils.isBlank(params.userFilter)) ldapnts.setUserFilter(opts, params.userFilter);
				if (!StringUtils.isBlank(params.userIdField)) ldapnts.setUserIdField(opts, params.userIdField);
				if (!StringUtils.isBlank(params.userFirstnameField)) ldapnts.setUserFirstnameField(opts, params.userFirstnameField);
				if (!StringUtils.isBlank(params.userLastnameField)) ldapnts.setUserLastnameField(opts, params.userLastnameField);
				if (!StringUtils.isBlank(params.userDisplayNameField)) ldapnts.setUserDisplayNameField(opts, params.userDisplayNameField);
				break;
			case ADDirectory.SCHEME:
				params = LangUtils.deserialize(ad.getDirParameters(), new ParamsLdapDirectory(), ParamsLdapDirectory.class);
				ADConfigBuilder adir = new ADConfigBuilder();
				adir.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				adir.setHost(opts, authUri.getHost());
				adir.setPort(opts, authUri.getPort());
				adir.setConnectionSecurity(opts, ad.getDirConnSecurity());
				adir.setAdminDn(opts, ad.getDirAdmin());
				adir.setAdminPassword(opts, getDirPassword(ad));
				if (!StringUtils.isBlank(params.loginDn)) adir.setLoginDn(opts, params.loginDn);
				if (!StringUtils.isBlank(params.loginFilter)) adir.setLoginFilter(opts, params.loginFilter);
				if (!StringUtils.isBlank(params.userDn)) adir.setUserDn(opts, params.userDn);
				if (!StringUtils.isBlank(params.userFilter)) adir.setUserFilter(opts, params.userFilter);
				if (!StringUtils.isBlank(params.userFirstnameField)) adir.setUserFirstnameField(opts, params.userFirstnameField);
				if (!StringUtils.isBlank(params.userLastnameField)) adir.setUserLastnameField(opts, params.userLastnameField);
				if (!StringUtils.isBlank(params.userDisplayNameField)) adir.setUserDisplayNameField(opts, params.userDisplayNameField);
				break;
			case ImapDirectory.SCHEME:
				ImapConfigBuilder imap = new ImapConfigBuilder();
				imap.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				imap.setHost(opts, authUri.getHost());
				imap.setPort(opts, authUri.getPort());
				imap.setConnectionSecurity(opts, ad.getDirConnSecurity());
				break;
			case SmbDirectory.SCHEME:
				SmbConfigBuilder smb = new SmbConfigBuilder();
				smb.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				smb.setHost(opts, authUri.getHost());
				smb.setPort(opts, authUri.getPort());
				break;
			case SftpDirectory.SCHEME:
				SftpConfigBuilder sftp = new SftpConfigBuilder();
				sftp.setIsCaseSensitive(opts, ad.getDirCaseSensitive());
				sftp.setHost(opts, authUri.getHost());
				sftp.setPort(opts, authUri.getPort());
				break;
		}
		return opts;
	}
	
	private char[] getDirPassword(AuthenticationDomain ad) {
		if(ad.getDirPassword() == null) return null;
		String s = PasswordUtils.decryptDES(new String(ad.getDirPassword()), new String(new char[]{'p','a','s','s','w','o','r','d'}));
		return (s != null) ? s.toCharArray() : null;
	}
	
	
}
