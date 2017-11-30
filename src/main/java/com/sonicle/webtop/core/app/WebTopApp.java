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

import com.sonicle.commons.http.HttpClientUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.manager.TomcatManager;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.PasswordUtils;
import com.sonicle.security.Principal;
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
import com.sonicle.webtop.core.bol.OMessageQueue;
import com.sonicle.webtop.core.bol.model.ParamsLdapDirectory;
import com.sonicle.webtop.core.dal.MessageQueueDAO;
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
import java.sql.Connection;
import java.text.MessageFormat;
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
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.joda.time.DateTime;
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
	
	private static Subject buildSubject(UserProfileId pid) {
		return buildSubject(pid, null);
	}
	
	private static Subject buildSubject(UserProfileId pid, String sessionId) {
		Principal principal = new Principal(pid.getDomainId(), pid.getUserId());
		PrincipalCollection principals = new SimplePrincipalCollection(principal, "com.sonicle.webtop.core.shiro.WTRealm");
		if(StringUtils.isBlank(sessionId)) {
			return new Subject.Builder().principals(principals).buildSubject();
		} else {
			return new Subject.Builder().principals(principals).sessionId(sessionId).buildSubject();
		}
	}
	
	Subject getAdminSubject() {
		return adminSubject;
	}
	
	/**
	 * Start method. This method should be called once.
	 * @param context ServletContext instance.
	 */
	public static void start(ServletContext context) {
		synchronized(lockInstance) {
			if(instance != null) throw new RuntimeException("Application must be started once");
			SecurityUtils.setSecurityManager(new DefaultSecurityManager(new WTRealm()));
			Subject adminSubject = buildSubject(new UserProfileId(WebTopManager.SYSADMIN_DOMAINID, WebTopManager.SYSADMIN_USERID));
			
			ThreadState threadState = new SubjectThreadState(adminSubject);
			try {
				threadState.bind();
				instance = new WebTopApp(context, adminSubject);
			} finally {
				threadState.clear();
			}
			
			new Timer("onAppReady").schedule(new TimerTask() {
				@Override
				public void run() {
					ThreadState threadState = new SubjectThreadState(instance.getAdminSubject());
					try {
						LoggerUtils.initDC(instance.getWebappName());
						threadState.bind();
						instance.onAppReady();
					} catch(InterruptedException ex) {
						// Do nothing...
					} finally {
						threadState.clear();
						LoggerUtils.clearDC();
					}
				}
			}, 5000);
		}
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
	private final String osInfo;
	private final Charset systemCharset;
	private DateTimeZone systemTimeZone;
	private Locale systemLocale;
	private final StartupProperties startupProperties;
	
	private TomcatManager tomcat = null;
	private final String webappName;
	private final String webappConfigPath;
	private boolean webappIsLatest;
	private Timer webappVersionCheckTimer = null;
	
	private Subject adminSubject;
	private final Object lockAdminSubject = new Object();
	private Timer adminTouchTimer = null;
	
	private MediaTypes mediaTypes = null;
	private FileTypes fileTypes = null;
	private Configuration freemarkerCfg = null;
	private I18nManager i18nmgr = null;
	private ConnectionManager conmgr = null;
	private LogManager logmgr = null;
	private WebTopManager wtmgr = null;
	private SettingsManager setmgr = null;
	private ServiceManager svcm = null;
	private SessionManager sesmgr = null;
	private OTPManager optmgr = null;
	private ReportManager rptmgr = null;
	private Scheduler scheduler = null;
	private final HashMap<String, Session> cacheMailSessionByDomain = new HashMap<>();
	private static final HashMap<String, ReadableUserAgent> cacheUserAgents =  new HashMap<>(); //TODO: decidere politica conservazion
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static start method.
	 */
	private WebTopApp(ServletContext context, Subject adminSubject) {
		this.servletContext = context;
		this.adminSubject = adminSubject;
		this.osInfo = OSInfo.build();
		this.systemCharset = Charset.forName("UTF-8");
		this.systemTimeZone = DateTimeZone.getDefault();
		
		// Ignore SSL checks for old commons-http components.
		// This is required in order to avoid error when accessing WebDAV 
		// secured servers throught vfs2.
		Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
		
		System.setProperty("net.fortuna.ical4j.timezone.update.enabled", String.valueOf(false));
		ICalendarUtils.setUnfoldingRelaxed(true);
		ICalendarUtils.setParsingRelaxed(true);
		ICalendarUtils.setValidationRelaxed(true);
		ICalendarUtils.setCompatibilityOutlook(true);
		ICalendarUtils.setCompatibilityNotes(true);
		
		startupProperties = createStartupProperties();
		logger.info("webtop.extJsDebug = {}", startupProperties.getExtJsDebug());
		logger.info("webtop.soExtDevMode = {}", startupProperties.getSonicleExtJsExtensionsDevMode());
		logger.info("webtop.devMode = {}", startupProperties.getDevMode());
		logger.info("webtop.debugMode = {}", startupProperties.getDebugMode());
		logger.info("webtop.schedulerSisabled = {}", startupProperties.getSchedulerDisabled());
		logger.info("webtop.webappsConfigPath = {}", startupProperties.getWebappsConfigPath());
		
		//logger.info("getContextPath: {}", context.getContextPath());
		//logger.info("getServletContextName: {}", context.getServletContextName());
		//logger.info("getVirtualServerName: {}", context.getVirtualServerName());
		
		this.webappName = ServletUtils.getWebappName(context);
		if (StringUtils.isBlank(startupProperties.getWebappsConfigPath())) {
			this.webappConfigPath = null;
		} else {
			this.webappConfigPath = PathUtils.concatPaths(startupProperties.getWebappsConfigPath(), ServletUtils.getWebappName(context, true));
		}
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
		this.conmgr = ConnectionManager.initialize(this, webappConfigPath); // Connection Manager
		this.setmgr = SettingsManager.initialize(this); // Settings Manager
		this.sesmgr = SessionManager.initialize(this); // Session Manager
		
		// Checks home directory
		logger.info("Checking home structure...");
		File homeDir = new File(getHomePath());
		if (!homeDir.exists()) throw new WTRuntimeException("Configured home directory not found [{0}]", homeDir.toString());
		checkHomeStructure();
		
		this.mediaTypes = MediaTypes.init(conmgr);
		this.fileTypes = FileTypes.init(conmgr);
		
		// Locale Manager
		//TODO: caricare dinamicamente le lingue installate nel sistema
		String[] tags = new String[]{"it_IT", "en_EN", "es_ES"};
		this.i18nmgr = I18nManager.initialize(this, tags);
		
		// Template Engine
		logger.info("Initializing template engine");
		this.freemarkerCfg = new Configuration();
		this.freemarkerCfg.setClassForTemplateLoading(this.getClass(), "/");
		this.freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
		this.freemarkerCfg.setDefaultEncoding(getSystemCharset().name());
		
		//comm = ComponentsManager.initialize(this); // Components Manager
		this.logmgr = LogManager.initialize(this); // Log Manager
		this.wtmgr = WebTopManager.initialize(this); // WT Manager
		
		this.systemLocale = CoreServiceSettings.getSystemLocale(setmgr); // System locale
		this.optmgr = OTPManager.initialize(this); // OTP Manager
		this.rptmgr = ReportManager.initialize(this); // Report Manager
		
		// Scheduler (services manager requires this component for jobs)
		try {
			//TODO: gestire le opzioni di configurazione dello scheduler
			this.scheduler = new StdSchedulerFactory().getScheduler();
			if (startupProperties.getSchedulerDisabled()) {
				logger.warn("Scheduler startup forcibly disabled");
			} else {
				this.scheduler.start();
			}
		} catch(SchedulerException ex) {
			throw new WTRuntimeException(ex, "Unable to start scheduler");
		}
		
		this.svcm = ServiceManager.initialize(this, this.scheduler); // Service Manager
		
		
		
		org.apache.shiro.session.Session session = adminSubject.getSession(false);
		logger.info("Admin session created [{}]", session.getId().toString());
		scheduleAdminTouchTask(session.getTimeout());
		
		logger.info("WTA initialization completed [{}]", webappName);
	}
	
	public void destroy() {
		logger.info("WTA shutdown started [{}]", webappName);
		
		// Destroy timers
		if(webappVersionCheckTimer != null) webappVersionCheckTimer.cancel();
		if(adminTouchTimer != null) adminTouchTimer.cancel();
		
		tomcat = null;
		
		// Service Manager
		svcm.cleanup();
		svcm = null;
		// Session Manager
		sesmgr.cleanup();
		sesmgr = null;
		// Scheduler
		try {
			scheduler.shutdown(true);
			scheduler = null;
		} catch(SchedulerException ex) {
			throw new WTRuntimeException(ex, "Error cleaning-up scheduler");
		}
		// Report Manager
		rptmgr.cleanup();
		rptmgr = null;
		// OTP Manager
		optmgr.cleanup();
		optmgr = null;
		// Settings Manager
		setmgr.cleanup();
		setmgr = null;
		// Auth Manager
		//autm.cleanup();
		//autm = null;
		// User Manager
		wtmgr.cleanup();
		wtmgr = null;
		// Connection Manager
		conmgr.cleanup();
		conmgr = null;
		// I18nManager Manager
		//I18nManager.cleanup();
		i18nmgr = null;
		
		// Destroy admin session
		synchronized(lockAdminSubject) {
			adminSubject.logout();
		}
		
		logger.info("WTA shutdown completed [{}]", webappName);
	}
	
	private void onAppReady() throws InterruptedException {
		logger.trace("onAppReady...");
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
			String tomcatUri = CoreServiceSettings.getTomcatManagerUri(setmgr);
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
			
			svcm.initializeJobServices();
			if (isLatest()) {
				logger.debug("Sleeping for 60sec for avoid concurrency");
				Thread.sleep(60000);
				if (isLatest()) {
					try {
						logger.info("Scheduling JobServices tasks...");
						svcm.scheduleAllJobServicesTasks();
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
		wtmgr.initDomainHomeFolder(WebTopManager.SYSADMIN_DOMAINID);
		
		List<ODomain> domains = wtmgr.listDomains(false);
		for (ODomain domain : domains) {
			try {
				wtmgr.initDomainHomeFolder(domain.getDomainId());
			} catch(SecurityException ex) {
				logger.warn("Unable to check domain home [{}]", domain.getDomainId(), ex);
			}
		}
	}
	
	private void scheduleAdminTouchTask(long sessionTimeout) {
		long period = (sessionTimeout < 600000) ? sessionTimeout/2 : (long)(sessionTimeout*0.9);
		adminTouchTimer = new Timer("adminTouch");
		adminTouchTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				onAdminTouch();
			}
		}, period, period);
		logger.info("adminTouch task scheduled [{}sec]", period/1000);
	}
	
	private void onAdminTouch() {
		logger.trace("onAdminTouch...");
		synchronized(lockAdminSubject) {
			if(adminSubject != null) {
				org.apache.shiro.session.Session session = adminSubject.getSession(false);
				if(session != null) {
					session.touch();
					logger.trace("Renewalling admin session [{}]", session.getLastAccessTime());
				} else {
					logger.warn("Admin session not found");
				}
			}
		}
	}
	
	private void scheduleWebappVersionCheckTask() {
		long period = 60000;
		webappVersionCheckTimer = new Timer("webappVersionCheck");
		adminTouchTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				ThreadState threadState = new SubjectThreadState(instance.getAdminSubject());
				try {
					threadState.bind();
					instance.onWebappVersionCheck();
				} finally {
					threadState.clear();
				}
			}
		}, period, period);
		logger.info("webappVersionCheck task scheduled [{}sec]", period/1000);
	}
	
	private void onWebappVersionCheck() {
		logger.trace("onWebappVersionCheck...");
		if (tomcat == null) return;
		
		logger.trace("Checking webapp version...");
		boolean oldLatest = webappIsLatest;
		webappIsLatest = checkIsLastestWebapp(webappName);
		if (webappIsLatest && !oldLatest) {
			logger.info("Webapp [{}] is the latest", webappName);
			svcm.scheduleAllJobServicesTasks();
		} else if (!webappIsLatest && oldLatest) {
			logger.info("Webapp [{}] is NO more the latest", webappName);
		} else {
			logger.trace("No version changes found!");
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
	
	
	
	
	
	public Subject bindAdminSubjectToSession(String sessionId) {
		return buildSubject(new UserProfileId(WebTopManager.SYSADMIN_DOMAINID, WebTopManager.SYSADMIN_USERID), sessionId);
	}
	
	private CoreServiceSettings getCoreServiceSettings() {
		return new CoreServiceSettings(setmgr, CoreManifest.ID, WebTopManager.SYSADMIN_DOMAINID);
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
		return startupProperties;
	}
	
	/**
	 * Returns webapp's name as configured in the application server.
	 * @return Webapp's name
	 */
	public String getWebappName() {
		return webappName;
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
		return i18nmgr;
	}
	
	/**
	 * Returns the ConnectionManager.
	 * @return ConnectionManager instance.
	 */
	public ConnectionManager getConnectionManager() {
		return conmgr;
	}
	
	/**
	 * Returns the SettingsManager.
	 * @return SettingsManager instance.
	 */
	public SettingsManager getSettingsManager() {
		return setmgr;
	}
	
	/**
	 * Returns the LogManager.
	 * @return UserManager instance.
	 */
	public LogManager getLogManager() {
		return logmgr;
	}
	
	/**
	 * Returns the WebTopManager.
	 * @return WebTopManager instance.
	 */
	public WebTopManager getWebTopManager() {
		return wtmgr;
	}
	
	/**
	 * Returns the ServiceManager.
	 * @return ServiceManager instance.
	 */
	public ServiceManager getServiceManager() {
		return svcm;
	}
	
	/**
	 * Returns the OTPManager.
	 * @return OTPManager instance.
	 */
	public OTPManager getOTPManager() {
		return optmgr;
	}
	
	/**
	 * Returns the ReportManager.
	 * @return ReportManager instance.
	 */
	public ReportManager getReportManager() {
		return rptmgr;
	}
	
	/**
	 * Returns the SessionManager.
	 * @return SessionManager instance.
	 */
	public SessionManager getSessionManager() {
		return sesmgr;
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
		
		InternetAddress iafrom=MailUtils.buildInternetAddress(from);
		InternetAddress iato[]=null;
		InternetAddress iacc[]=null;
		InternetAddress iabcc[]=null;
		
        if (to!=null) {
			iato=new InternetAddress[to.length];
			int i=0;
            for(String addr: to) {
                iato[i++]=MailUtils.buildInternetAddress(addr);
            }
		}
		
        if (cc!=null) {
			iacc=new InternetAddress[cc.length];
			int i=0;
            for(String addr: cc) {
                iacc[i++]=MailUtils.buildInternetAddress(addr);
            }
		}
		
        if (bcc!=null) {
			iabcc=new InternetAddress[bcc.length];
			int i=0;
            for(String addr: bcc) {
                iabcc[i++]=MailUtils.buildInternetAddress(addr);
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
			mbp2.setText(body, "UTF-8");
			mbp2.setHeader("Content-type", "text/html");
			MimeBodyPart mbp1=new MimeBodyPart();
			mbp1.setText(MailUtils.htmlToText(MailUtils.htmlunescapesource(body)));
			mbp1.setHeader("Content-type", "text/plain");
			alternative.addBodyPart(mbp1);
			alternative.addBodyPart(mbp2);
			MimeBodyPart altbody=new MimeBodyPart();
			altbody.setContent(alternative);
			mp.addBodyPart(altbody);
		} else {
			MimeBodyPart mbp1=new MimeBodyPart();
			mbp1.setText(body);
			mbp1.setHeader("Content-type", "text/plain");
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
	
	public void notify(UserProfileId profileId, List<ServiceMessage> messages, boolean enqueueIfOffline) {
		List<WebTopSession> sessions = sesmgr.getWebTopSessions(profileId);
		if(!sessions.isEmpty()) {
			for(WebTopSession session : sessions) {
				session.notify(messages);
			}
		} else { // No user active sessions found!
			if(enqueueIfOffline) {
				Connection con = null;
				
				try {
					MessageQueueDAO mqdao = MessageQueueDAO.getInstance();
					con = conmgr.getConnection();
					OMessageQueue queued = null;
					for(ServiceMessage message : messages) {
						queued = new OMessageQueue();
						queued.setQueueId(mqdao.getSequence(con).intValue());
						queued.setDomainId(profileId.getDomainId());
						queued.setUserId(profileId.getUserId());
						queued.setMessageType(message.getClass().getName());
						queued.setMessageRaw(JsonResult.gson.toJson(message));
						queued.setQueuedOn(DateTime.now(DateTimeZone.UTC));
						mqdao.insert(con, queued);
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				} finally {
					DbUtils.closeQuietly(con);
				}
			}
		}
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
	
	/**
	 * Gets WebTopApp object stored as context's attribute.
	 * @param request The http request
	 * @return WebTopApp object
	 */
	public static WebTopApp get(HttpServletRequest request) {
		return get(request.getSession().getServletContext());
	}
	
	/**
	 * Gets WebTopApp object stored as context's attribute.
	 * @param context The servlet context
	 * @return WebTopApp object
	 */
	static WebTopApp get(ServletContext context) {
		return (WebTopApp) context.getAttribute(ContextLoader.WEBTOPAPP_ATTRIBUTE_KEY);
	}
}
