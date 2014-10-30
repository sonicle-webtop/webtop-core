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
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.userdata.UserDataProviderBase;
import com.sonicle.webtop.core.userdata.UserDataProviderFactory;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author malbinola
 */
public class WebTopApp {
	
	public final static Logger logger = WebTopApp.getLogger(WebTopApp.class);
	public static final String ATTRIBUTE = "webtopapp";
	private static final Object lock = new Object();
	private static WebTopApp instance = null;
	
	public static void initialize(ServletContext context) {
		synchronized(lock) {
			if(instance != null) throw new RuntimeException("WebTopApp initialization already done!");
			instance = new WebTopApp(context);
		}
	}
	
	public static WebTopApp getInstance() {
		synchronized(lock) {
			return instance;
		}
	}
	
	private final ServletContext servletContext;
	private final String webappName;
	private final String systemInfo;
	private final Locale systemLocale;
	private Configuration freemarkerCfg = null;
	private ConnectionManager conm = null;
	private SettingsManager setm = null;
	private ServiceManager svcm = null;
	private static final HashMap<String, ReadableUserAgent> userAgentsCache =  new HashMap<>();
	
	private WebTopApp(ServletContext context) {
		servletContext = context;
		webappName = ServletHelper.getWebAppName(context);
		systemInfo = buildSystemInfo();
		
		logger.info("WTA initialization started [{}]", webappName);
		
		// Template Engine
		logger.info("Initializing template engine.");
		freemarkerCfg = new Configuration();
		freemarkerCfg.setClassForTemplateLoading(this.getClass(), "/");
		freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
		freemarkerCfg.setDefaultEncoding("UTF-8");
		
		// Connection Manager
		conm = ConnectionManager.initialize(this);
		try {
			conm.registerJdbc4DataSource(CoreManifest.ID, "org.postgresql.ds.PGSimpleDataSource", "www.sonicle.com", null, "webtop5", "sonicle", "sonicle");
		} catch (SQLException ex) {
			logger.error("Error registeting default connection", ex);
		}
		// Settings Manager
		setm = SettingsManager.initialize(this);
		// Service Manager
		svcm = ServiceManager.initialize(this);
		
		systemLocale = CoreServiceSettings.getSystemLocale(setm);
		
		logger.info("WTA initialization completed [{}]", webappName);
	}
	
	public void destroy() {
		logger.info("WTA shutdown started [{}]", webappName);
		
		// Connection Manager
		conm.cleanup();
		conm = null;
		logger.info("ConnectionManager destroyed.");
		
		logger.info("WTA shutdown completed [{}]", webappName);
	}
	
	/**
	 * Returns webapp's name as configured in the application server.
	 * @return Webapp's name
	 */
	public String getWebAppName() {
		return webappName;
	}
	
	public String getServerInfo() {
		return servletContext.getServerInfo();
	}
	
	public String getSystemInfo() {
		return systemInfo;
	}
	
	public Locale getSystemLocale() {
		return systemLocale;
	}
	
	public Template loadTemplate(String path) throws IOException {
		return freemarkerCfg.getTemplate(path);
	}
	
	/**
	 * Parses a User-Agent HTTP Header string looking for useful client information.
	 * @param userAgentHeader HTTP Header string.
	 * @return Object representation of the parsed string.
	 */
	public ReadableUserAgent getUserAgentInfo(String userAgentHeader) {
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
	
	/**
	 * Returns the SettingsManager.
	 * @return SettingsManager instance.
	 */
	public SettingsManager getSettingsManager() {
		return setm;
	}
	
	/**
	 * Returns the ConnectionManager.
	 * @return ConnectionManager instance.
	 */
	public ConnectionManager getConnectionManager() {
		return conm;
	}
	
	/**
	 * Returns the ServiceManager.
	 * @return ServiceManager instance.
	 */
	public ServiceManager getServiceManager() {
		return svcm;
	}
	
	public CoreManager getManager() {
		return new CoreManager(this);
	}
	
	public String lookupResource(Locale locale, String key) {
		return lookupResource(CoreManifest.ID, locale, key, false);
	}
	
	public String lookupResource(Locale locale, String key, boolean escapeHtml) {
		return lookupResource(CoreManifest.ID, locale, key, escapeHtml);
	}
	
	public String lookupResource(String serviceId, Locale locale, String key) {
		return lookupResource(serviceId, locale, key, false);
	}
	
	public String lookupResource(String serviceId, Locale locale, String key, boolean escapeHtml) {
		String baseName = MessageFormat.format("{0}/locale", StringUtils.replace(serviceId, ".", "/"));
		String value = "";
		
		try {
			value = ResourceBundle.getBundle(baseName, locale).getString(key);
			if(escapeHtml) value = StringEscapeUtils.escapeHtml4(value);
		} catch(MissingResourceException ex) {
			logger.warn("Missing resource [{}, {}, {}]", baseName, locale.toString(), key, ex);
		} finally {
			return value;
		}
	}
	
	public String lookupAndFormatResource(Locale locale, String key, boolean escapeHtml, Object... arguments) {
		return lookupAndFormatResource(CoreManifest.ID, locale, key, escapeHtml, arguments);
	}
	
	public String lookupAndFormatResource(String serviceId, Locale locale, String key, boolean escapeHtml, Object... arguments) {
		String value = lookupResource(serviceId, locale, key, escapeHtml);
		return MessageFormat.format(value, arguments);
	}
	
	public String getCustomProperty(String name) {
		return null;
	}
	
	/**
	 * Returns a logger name valid for internal classes.
	 * @param clazz
	 * @return The logger name.
	 */
	public static String getLoggerName(Class clazz) {
		return clazz.getName();
	}
	
	/**
	 * Prepares a logger instance for desired class.
	 * @param clazz
	 * @return The logger instance.
	 */
	public static Logger getLogger(Class clazz) {
		return (Logger) LoggerFactory.getLogger(WebTopApp.getLoggerName(clazz));
	}
	
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
