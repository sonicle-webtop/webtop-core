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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.bol.OMessageQueue;
import com.sonicle.webtop.core.dal.MessageQueueDAO;
import com.sonicle.webtop.core.io.FileResource;
import com.sonicle.webtop.core.io.JarFileResource;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.shiro.WTRealm;
import com.sonicle.webtop.core.util.IdentifierUtils;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
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
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 *
 * @author malbinola
 */
public final class WebTopApp {
	public static final String ATTRIBUTE = "webtopapp";
	public static final Logger logger = WT.getLogger(WebTopApp.class);
	private static final Object lock1 = new Object();
	private static final Object lock2 = new Object();
	
	private static WebTopApp instance = null;
	
	/**
	 * Initialization method. This method should be called once.
	 * @param context ServletContext instance.
	 */
	public static void initialize(ServletContext context) {
		synchronized(lock1) {
			if(instance != null) throw new RuntimeException("Initialization already done");
			instance = new WebTopApp(context);
		}
		
		ThreadState threadState = new SubjectThreadState(instance.getAdminSubject());
		threadState.bind();
		try {
			instance.afterInit();
		} finally {
			threadState.clear();
		}
	}
	
	public static WebTopApp getInstance() {
		synchronized(lock1) {
			return instance;
		}
	}
	
	private final ServletContext servletContext;
	private final String systemInfo;
	private final Charset systemCharset;
	private DateTimeZone systemTimeZone;
	private Locale systemLocale;
	
	private Subject adminSubject;
	private Timer adminTouchTimer = null;
	
	private Configuration freemarkerCfg = null;
	private I18nManager i18nm = null;
	//private ComponentsManager comm = null;
	private ConnectionManager conm = null;
	private LogManager logm = null;
	private UserManager usrm = null;
	private AuthManager autm = null;
	private SettingsManager setm = null;
	private ServiceManager svcm = null;
	private SessionManager sesm = null;
	private final HashMap<String,CoreServiceSettings> cssCache = new HashMap();
	private OTPManager otpm = null;
	private ReportManager rptm = null;
	private Scheduler scheduler = null;
	private final HashMap<String,Session> sessionCache = new HashMap();
	private static final HashMap<String, ReadableUserAgent> userAgentsCache =  new HashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param @param context ServletContext instance.
	 */
	private WebTopApp(ServletContext context) {
		servletContext = context;
		systemInfo = buildSystemInfo();
		systemCharset = Charset.forName("UTF-8");
		systemTimeZone = DateTimeZone.getDefault();
		
		logger.info("wtdebug = {}", getPropWTDebug());
		logger.info("extdebug = {}", getPropExtDebug());
		logger.info("scheduler.disabled = {}", getPropSchedulerDisabled());
		
		String webappName = getWebAppName();
		logger.info("WTA initialization started [{}]", webappName);
		
		init1();
		ThreadState threadState = new SubjectThreadState(getAdminSubject());
		threadState.bind();
		try {
			init2();
		} finally {
			threadState.clear();
		}
		
		logger.info("WTA initialization completed [{}]", webappName);
	}
	
	private void init1() {
		conm = ConnectionManager.initialize(this); // Connection Manager
		sesm = SessionManager.initialize(this); // Session Manager
		
		// Auth Manager
		DefaultSecurityManager sm = new DefaultSecurityManager(new WTRealm());
		SecurityUtils.setSecurityManager(sm);
		autm = AuthManager.initialize(this);
	}
	
	private void init2() {
		// Locale Manager
		//TODO: caricare dinamicamente le lingue installate nel sistema
		String[] tags = new String[]{"it_IT", "en_EN"};
		i18nm = I18nManager.initialize(this, tags);
		
		// Template Engine
		logger.info("Initializing template engine.");
		freemarkerCfg = new Configuration();
		freemarkerCfg.setClassForTemplateLoading(this.getClass(), "/");
		freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
		freemarkerCfg.setDefaultEncoding(getSystemCharset().name());
		
		//comm = ComponentsManager.initialize(this); // Components Manager
		logm = LogManager.initialize(this); // Log Manager
		usrm = UserManager.initialize(this); // User Manager
		
		setm = SettingsManager.initialize(this); // Settings Manager
		systemLocale = CoreServiceSettings.getSystemLocale(setm); // System locale
		otpm = OTPManager.initialize(this); // OTP Manager
		rptm = ReportManager.initialize(this); // Report Manager
		
		// Scheduler (services manager requires this component for jobs)
		try {
			//TODO: gestire le opzioni di configurazione dello scheduler
			SchedulerFactory sf = new StdSchedulerFactory();
			scheduler = sf.getScheduler();
			if(WebTopApp.getPropSchedulerDisabled()) {
				logger.warn("Scheduler startup disabled");
			} else {
				scheduler.start();
			}
		} catch(SchedulerException ex) {
			throw new WTRuntimeException(ex, "Error starting scheduler");
		}
		
		svcm = ServiceManager.initialize(this, scheduler); // Service Manager
	}
	
	public void destroy() {
		String webappName = getWebAppName();
		logger.info("WTA shutdown started [{}]", webappName);
		
		// Destroy admin session
		if(adminTouchTimer != null) adminTouchTimer.cancel();
		synchronized(lock2) {
			adminSubject.logout();
		}
		
		// Service Manager
		svcm.cleanup();
		svcm = null;
		// Session Manager
		sesm.cleanup();
		sesm = null;
		// Scheduler
		try {
			scheduler.shutdown(true);
			scheduler = null;
		} catch(SchedulerException ex) {
			throw new WTRuntimeException(ex, "Error cleaning-up scheduler");
		}
		// Report Manager
		rptm.cleanup();
		rptm = null;
		// OTP Manager
		otpm.cleanup();
		otpm = null;
		// Settings Manager
		setm.cleanup();
		setm = null;
		// Auth Manager
		autm.cleanup();
		autm = null;
		// User Manager
		usrm.cleanup();
		usrm = null;
		// Connection Manager
		conm.cleanup();
		conm = null;
		// I18nManager Manager
		//I18nManager.cleanup();
		i18nm = null;
		
		logger.info("WTA shutdown completed [{}]", webappName);
	}
	
	private void afterInit() {
		svcm.onWebTopAppInit();
		
		// Define delayed init
		new Timer("delayedInit").schedule(new TimerTask() {
			@Override
			public void run() {
				delayedInit();
			}
		}, 5000);	
	}
	
	private void delayedInit() {
		if(svcm != null) { // <- Check to avoid nullpointerexception in development during redeploy
			try {
				logger.debug("Scheduling JobServices tasks...");
				svcm.scheduleAllJobServicesTasks();
				if(!scheduler.isStarted()) logger.warn("Tasks succesfully scheduled but scheduler is not running");
			} catch (SchedulerException ex) {
				logger.error("Error", ex);
			}
		}
	}
	
	private void scheduleAdminTouchTask(long sessionTimeout) {
		long period = (sessionTimeout < 600000) ? sessionTimeout/2 : (long)(sessionTimeout*0.9);
		logger.trace("Scheduling adminTouch task to renew session every {} sec", period/1000);
		adminTouchTimer = new Timer("adminTouch");
		adminTouchTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				synchronized(lock2) {
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
		}, period, period);
	}
	
	/**
	 * Returns webapp's name as configured in the application server.
	 * @return Webapp's name
	 */
	public String getWebAppName() {
		return ServletHelper.getWebAppName(servletContext);
	}
	
	/**
	 * Checks if this webapp is the latest version in the application server.
	 * @return True if this is the last version, false otherwise.
	 */
	public boolean isLastVersion() {
		String webappName = getWebAppName();
		String webappBaseName = StringUtils.split(webappName, "##")[0];
		String webappPath = servletContext.getRealPath("/");
		String webappsDirPath = webappPath + "/..";
		
		// Cycles webapps folders into application server webapps directory
		// and extract app names that matches with base name.
		File webappsDir = new File(webappsDirPath);
		ListOrderedSet names = new ListOrderedSet();
		for(File file : webappsDir.listFiles()) {
			if(file.isDirectory()) {
				if(StringUtils.startsWith(file.getName(), webappBaseName)) {
					names.add(file.getName());
				}
			}
		}
		
		String last = (String)names.get(names.size()-1);
		return webappName.equals(last);
	}
	
	public String getPlatformName() {
		//TODO: completare rebranding aggiungendo impostazione per override del nome
		return "WebTop";
	}
	
	public String getServerInfo() {
		return servletContext.getServerInfo();
	}
	
	public String getSystemInfo() {
		return systemInfo;
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
	
	/**
	 * Returns the I18nManager.
	 * @return I18nManager instance.
	 */
	public I18nManager getI18nManager() {
		return i18nm;
	}
	
	/**
	 * Returns the ConnectionManager.
	 * @return ConnectionManager instance.
	 */
	public ConnectionManager getConnectionManager() {
		return conm;
	}
			
	/**
	 * Returns the ComponentsManager.
	 * @return ComponentsManager instance.
	 */
	/*
	public ComponentsManager getComponentsManager() {
		return comm;
	}
	*/
	
	/**
	 * Returns the SettingsManager.
	 * @return SettingsManager instance.
	 */
	public SettingsManager getSettingsManager() {
		return setm;
	}
	
	/**
	 * Returns the LogManager.
	 * @return UserManager instance.
	 */
	public LogManager getLogManager() {
		return logm;
	}
	
	/**
	 * Returns the UserManager.
	 * @return UserManager instance.
	 */
	public UserManager getUserManager() {
		return usrm;
	}
	
	/**
	 * Returns the AuthManager.
	 * @return AuthManager instance.
	 */
	public AuthManager getAuthManager() {
		return autm;
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
		return otpm;
	}
	
	/**
	 * Returns the ReportManager.
	 * @return ReportManager instance.
	 */
	public ReportManager getReportManager() {
		return rptm;
	}
	
	/**
	 * Returns the SessionManager.
	 * @return SessionManager instance.
	 */
	public SessionManager getSessionManager() {
		return sesm;
	}
	
	private Subject buildSubject(UserProfile.Id pid) {
		Principal principal = new Principal(pid.getDomainId(), pid.getUserId());
		PrincipalCollection principals = new SimplePrincipalCollection(principal, "com.sonicle.webtop.core.shiro.WTRealm");
		return new Subject.Builder().principals(principals).buildSubject();
	}
	
	Subject getAdminSubject() {
		synchronized(lock2) {
			if(adminSubject == null) {
				adminSubject = buildSubject(new UserProfile.Id("*", "admin"));
				org.apache.shiro.session.Session session = adminSubject.getSession(false);
				logger.info("Admin session created [{}]", session.getId().toString());
				scheduleAdminTouchTask(session.getTimeout());
			}
			return adminSubject;
		}
	}
	
	public CoreManager createCoreManager() {
		return new CoreManager(this);
	}
	
	public CoreManager createCoreManager(UserProfile.Id targetProfileId) {
		return new CoreManager(this, targetProfileId);
	}
	
	/*
	public RunContext createAdminRunContext() {
		return createAdminRunContext(createCoreServiceContext());
	}
	
	public RunContext createAdminRunContext(ServiceContext serviceContext) {
		//UserProfile.Id adminProfile = new UserProfile.Id("*", "admin");
		//Subject adminSubject = ContextUtils.buildSubject(adminProfile, true);
		return new RunContext(serviceContext, getAdminSubject());
	}
	*/
	
	/**
	 * Parses a User-Agent HTTP Header string looking for useful client information.
	 * @param userAgentHeader HTTP Header string.
	 * @return Object representation of the parsed string.
	 */
	public static ReadableUserAgent getUserAgentInfo(String userAgentHeader) {
		synchronized(userAgentsCache) {
			if(userAgentsCache.containsKey(userAgentHeader)) {
				return userAgentsCache.get(userAgentHeader);
			} else {
				UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
				ReadableUserAgent rua = parser.parse(userAgentHeader);
				userAgentsCache.put(userAgentHeader, rua);
				return rua;
			}
		}
	}
	
	public URL getResource(String resource) throws MalformedURLException {
		return servletContext.getResource(resource);
	}
	
	public Template loadTemplate(String path) throws IOException {
		return freemarkerCfg.getTemplate(path, getSystemCharset().name());
	}
	
	public Template loadTemplate(String path, String encoding) throws IOException {
		return freemarkerCfg.getTemplate(path, encoding);
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
		String baseName = MessageFormat.format("{0}/locale", StringUtils.replace(serviceId, ".", "/"));
		String value = "";
		
		try {
			value = ResourceBundle.getBundle(baseName, locale).getString(key);
			//value = new String(value.getBytes("ISO-8859-1"), "UTF-8");
			if(escapeHtml) value = StringEscapeUtils.escapeHtml4(value);
		} catch(MissingResourceException ex) {
			//TODO: abilitare logging
			//logger.trace("Missing resource [{}, {}, {}]", baseName, locale.toString(), key, ex);
		} finally {
			return value;
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
	
	
	
	private String substitutePathVariables(String path, String domainId) {
		return StringUtils.replace(path, "{DOMAIN_ID}", domainId);
	}
	
	public String getHomePath(String domainId) {
		CoreServiceSettings css = getCoreServiceSettings("*");
		return substitutePathVariables(css.getHomePath(), domainId);
	}
	
	public String getTempPath(String domainId) {
		return getHomePath(domainId) + "temp/";
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
	
	
	/*
	
	public String getSystemTempPath() {
		CoreServiceSettings css = getCoreServiceSettings("*");
		String path = css.getSystemTempPath();
		if(StringUtils.isEmpty(path)) {
			path = System.getProperty("java.io.tmpdir");
			logger.warn("System temporary folder not defined. Using default one '{}'", path);
		}
		return path;
	}
	
	public File getTempFolder() throws WTException {
		File tempDir = new File(getSystemTempPath());
		if(!tempDir.isDirectory() || !tempDir.canWrite()) {
			throw new WTException("Temp folder is not a directory or is write protected");
		}
		return tempDir;
	}
	
	public File createTempFile() throws WTException {
		return createTempFile(null, null);
	}
	
	public File createTempFile(String prefix, String suffix) throws WTException {
		File tempDir = getTempFolder();
		return new File(tempDir, buildTempFilename(prefix, suffix));
	}
	
	public boolean deleteTempFile(String filename) throws WTException {
		File tempDir = getTempFolder();
		File tempFile = new File(tempDir, filename);
		return tempFile.delete();
	}
	
	*/
	
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
	
	public void notify(UserProfile.Id profileId, List<ServiceMessage> messages, boolean enqueueIfOffline) {
		List<WebTopSession> sessions = sesm.getWebTopSessions(profileId);
		if(!sessions.isEmpty()) {
			for(WebTopSession session : sessions) {
				session.nofity(messages);
			}
		} else { // No user active sessions found!
			if(enqueueIfOffline) {
				Connection con = null;
				
				try {
					MessageQueueDAO mqdao = MessageQueueDAO.getInstance();
					con = conm.getConnection();
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
	
	public CoreServiceSettings getCoreServiceSettings(String domainId) {
		CoreServiceSettings css;
		synchronized(cssCache) {
			css=cssCache.get(domainId);
			if (css==null) {
				css=new CoreServiceSettings(CoreManifest.ID,domainId);
				cssCache.put(domainId, css);
			}
		}
		return css;
	}
	
	public Session getMailSession(String domainId) {
		Session session;
		synchronized(sessionCache) {
			CoreServiceSettings css=getCoreServiceSettings(domainId);
			String smtphost=css.getSMTPHost();
			int smtpport=css.getSMTPPort();
			String key=smtphost+":"+smtpport;
			session=sessionCache.get(key);
			if (session==null) {
				Properties props = System.getProperties();
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
				sessionCache.put(key,session);
				
				logger.info("Created javax.mail.Session for "+key);
			}
		}
		return session;
	}
	
	public void sendEmail(UserProfile.Id pid, boolean rich, 
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
		
		sendEmail(pid,rich,iafrom,iato,iacc,iabcc,subject,body,null);
		
	}
	
	public void sendEmail(UserProfile.Id pid, boolean rich, 
			InternetAddress from, InternetAddress[] to, InternetAddress[] cc, InternetAddress[] bcc, 
				String subject, String body, MimeBodyPart[] parts) throws MessagingException {
		
		Session session=getMailSession(pid.getDomainId());
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
	
	
	/*
	public List<InternetRecipient> listInternetRecipients(List<String> serviceIds, boolean incGlobal, String incDomainId, UserProfile.Id incProfileId, String text) throws WTException {
		ArrayList<InternetRecipient> items = new ArrayList<>();
		
		for(String sid : serviceIds) {
			RecipientsProviderBase provider = comm.getRecipientsProvider(sid);
			if(provider != null) {
				if(incGlobal && (provider instanceof IGlobalRecipientsProvider)) {
					try {
						items.addAll(((IGlobalRecipientsProvider)provider).getRecipients(text));
					} catch(Throwable t) {
						
					}	
				}
				if(!StringUtils.isBlank(incDomainId) && (provider instanceof IDomainRecipientsProvider)) {
					try {
						items.addAll(((IDomainRecipientsProvider)provider).getRecipients(incDomainId, text));
					} catch(Throwable t) {
						
					}
				}
				if((incProfileId != null) && (provider instanceof IProfileRecipientsProvider)) {
					try {
						items.addAll(((IProfileRecipientsProvider)provider).getRecipients(incProfileId, text));
					} catch(Throwable t) {
						
					}
				}
				
			}
		}
		return items;
	}
	*/
	
	
	
	
	public static boolean getPropSchedulerDisabled() {
		String prop = System.getProperties().getProperty("com.sonicle.webtop.scheduler.disabled");
		return LangUtils.value(prop, false);
		//return System.getProperties().containsKey("com.sonicle.webtop.wtdebug");
	}
	
	public static boolean getPropWTDebug() {
		String prop = System.getProperties().getProperty("com.sonicle.webtop.wtdebug");
		return LangUtils.value(prop, false);
		//return System.getProperties().containsKey("com.sonicle.webtop.wtdebug");
	}
	
	public static boolean getPropExtDebug() {
		String prop = System.getProperties().getProperty("com.sonicle.webtop.extdebug");
		return LangUtils.value(prop, false);
		//return System.getProperties().containsKey("com.sonicle.webtop.extdebug");
	}
	
	/**
	 * Returns a logger name valid for internal classes.
	 * @param clazz
	 * @return The logger name.
	 */
	//public static String getLoggerName(Class clazz) {
	//	return clazz.getName();
	//}
	
	/**
	 * Prepares a logger instance for desired class.
	 * @param clazz
	 * @return The logger instance.
	 */
	//public static Logger getLogger(Class clazz) {
	//	return (Logger) LoggerFactory.getLogger(WebTopApp.getLoggerName(clazz));
	//}
	
	/**
	 * (logger) Initialize diagnostic context (MDC object) with default entries:
	 * - service = core
	 * - username = unknown
	 * It also updates values related to automatic entries ('servuser', 'split').
	 * 
	 * @param appName The application name.
	 */
	public static void initLoggerDC(String appName) {
		MDC.put("appname", appName);
		MDC.put("service", "core");
		MDC.put("username", "nouser");
		WebTopApp.updateAutoLoggerDC();
	}
	
	/**
	 * (logger) Clear all diagnostic context entries.
	 */
	public static void clearLoggerDC() {
		MDC.clear();
	}
	
	/**
	 * (logger) Updates values related to automatic entries ('servuser', 'split').
	 */
	public static void updateAutoLoggerDC() {
		String appname = MDC.get("appname");
		String servuser = MDC.get("service") + "-" + MDC.get("username");
		MDC.put("servuser", servuser);
		String custom = MDC.get("custom");
		MDC.put("split", appname + "_" + (StringUtils.isEmpty(custom) ? servuser : servuser + "-" + custom));
	}
	
	/**
	 * (logger) Sets diagnostic entries related to current session.
	 * @param wts WebTopSession instance.
	 */
	public static void setSessionLoggerDC(WebTopSession wts) {
		/*
		Environment wte = wts.getEnvironment();
		if(wte == null) {
			WebTopApp.logger.debug("wte is null");
		} else {
			UserProfile up = wte.getUserProfile();
			if(up == null) {
				WebTopApp.logger.debug("up is null");
			} else {
				WebTopApp.setSessionLoggerDC(up.getUser());
			}
		}
		*/
		//WebTopApp.setSessionLoggerDC(wts.getEnvironment().getUserProfile().getUser());
	}
	
	/**
	 * (logger) Sets diagnostic entries related to current session.
	 * It also updates values related to automatic entries ('servuser', 'split').
	 * @param username Username/login related to current user.
	 */
	public static void setSessionLoggerDC(String username) {
		String[] tokens = StringUtils.split(username, "@");
		MDC.put("username", tokens[0]);
		WebTopApp.updateAutoLoggerDC();
	}
	
	/**
	 * (logger) Sets diagnostic entries related to a service.
	 * It also updates values related to automatic entries ('servuser', 'split').
	 * @param service Service name.
	 */
	public static void setServiceLoggerDC(String service) {
		MDC.put("service", service);
		WebTopApp.updateAutoLoggerDC();
	}
	
	/**
	 * (logger) Removes diagnostic entries related to a service.
	 * It also updates values related to automatic entries ('servuser', 'split').
	 */
	public static void unsetServiceLoggerDC() {
		MDC.remove("custom");
		MDC.put("service", "core");
		WebTopApp.updateAutoLoggerDC();
	}
	
	/**
	 * (logger) Sets a value for the DC entry ('custom') reserved to services.
	 * It also updates values related to automatic entries ('servuser', 'split').
	 * @param custom 
	 */
	public static void setServiceCustomLoggerDC(String custom) {
		MDC.put("custom", custom);
		WebTopApp.updateAutoLoggerDC();
	}
	
	/**
	 * (logger) Removes custom DC entry ('custom') reserved to services.
	 * It also updates values related to automatic entries ('servuser', 'split').
	 */
	public static void unsetServiceCustomLoggerDC() {
		MDC.remove("custom");
		WebTopApp.updateAutoLoggerDC();
	}
	
	private String buildSystemInfo() {
		String host = getCmdOutput("uname -n");
		String domainName = StringUtils.defaultString(getCmdOutput("domainname"));
		String osName = getCmdOutput("uname -s");
		if(StringUtils.isEmpty(osName)) osName = System.getProperty("os.name");
		String osRelease = getCmdOutput("uname -r");
		if(StringUtils.isEmpty(osRelease)) osRelease = System.getProperty("os.version");
		String osVersion = StringUtils.defaultString(getCmdOutput("uname -v"));
		String osArch = getCmdOutput("uname -m");
		if(StringUtils.isEmpty(osArch)) osArch = System.getProperty("os.arch");
		
		// Builds string
		StringBuilder sb = new StringBuilder();
		if(new File("/sonicle/etc/xstream.conf").exists()) {
			sb.append("Sonicle XStream Server");
			sb.append(" - ");
		}
		sb.append(host);
		if(!StringUtils.isEmpty(domainName)) {
			sb.append(" at ");
			sb.append(domainName);
		}
		sb.append(" - ");
		sb.append(osName);
		sb.append(" ");
		sb.append(osRelease);
		sb.append(" ");
		sb.append(osVersion);
		sb.append(" ");
		sb.append(osArch);
		return sb.toString();
	}
	
	public static String getCmdOutput(String command) {
		String output = null;
		try {
			Process pro = Runtime.getRuntime().exec(command);
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			output = br.readLine();
			pro.waitFor();
		} catch (Throwable th) { /* Do nothing! */ }
		return output;
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
		return (WebTopApp) context.getAttribute(ATTRIBUTE);
	}
}