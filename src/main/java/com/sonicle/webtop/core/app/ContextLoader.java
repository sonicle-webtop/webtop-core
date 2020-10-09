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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.PropUtils;
import com.sonicle.commons.web.ContextUtils;
import com.sonicle.webtop.core.app.servlet.RestApi;
import com.sonicle.webtop.core.app.shiro.filter.JWTSignatureVerifier;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import java.net.URL;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.cpr.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class ContextLoader {
	private static final Logger logger = WT.getLogger(ContextLoader.class);
	public static final String WEBAPPNAME_ATTRIBUTE_KEY = "wtwebappname";
	public static final String WEBTOPAPP_ATTRIBUTE_KEY = "wtapp";
	
	public static String getWabappName(ServletContext servletContext) {
		return (String)servletContext.getAttribute(WEBAPPNAME_ATTRIBUTE_KEY);
	}
	
	public void initLogging(ServletContext servletContext, String webappFullName, Properties properties) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		ClassLoader classLoader = Loader.getClassLoaderOfObject(this);
		
		// Preparing logback props
		String logTarget = WebTopProps.getLogTarget(properties);
		String logDir = WebTopProps.getLogDir(properties);
		logDir = expandLogDirVariables(logDir, webappFullName);
		String logFileBasename = PropUtils.isDefined(properties, WebTopProps.PROP_LOG_FILE_BASENAME) ? WebTopProps.getLogFileBasename(properties) : null;
		if (StringUtils.isBlank(logFileBasename)) logFileBasename = webappFullName;
		String logFilePolicy = WebTopProps.getLogFilePolicy(properties);
		String etcDir = WebTopProps.getEtcDir(properties);
		String overrideDir = StringUtils.isBlank(etcDir) ? null : PathUtils.concatPaths(etcDir, ContextUtils.stripWebappVersion(webappFullName));
		
		LogbackPropertyDefiner.setPropertyValue(true, LogbackPropertyDefiner.PROP_LOG_TARGET, logTarget);
		LogbackPropertyDefiner.setPropertyValue(true, LogbackPropertyDefiner.PROP_LOG_DIR, logDir);
		LogbackPropertyDefiner.setPropertyValue(true, LogbackPropertyDefiner.PROP_LOG_FILE_BASENAME, logFileBasename);
		LogbackPropertyDefiner.setPropertyValue(true, LogbackPropertyDefiner.PROP_LOG_FILE_POLICY, logFilePolicy);
		LogbackPropertyDefiner.setPropertyValue(true, LogbackPropertyDefiner.PROP_OVERRIDE_DIR, overrideDir);
		
		// Dump PropertyDefiner props
		LogbackHelper.printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackPropertyDefiner.PROP_LOG_TARGET, LogbackPropertyDefiner.getPropertyValue(LogbackPropertyDefiner.PROP_LOG_TARGET));
		LogbackHelper.printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackPropertyDefiner.PROP_LOG_DIR, LogbackPropertyDefiner.getPropertyValue(LogbackPropertyDefiner.PROP_LOG_DIR));
		LogbackHelper.printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackPropertyDefiner.PROP_LOG_FILE_BASENAME, LogbackPropertyDefiner.getPropertyValue(LogbackPropertyDefiner.PROP_LOG_FILE_BASENAME));
		LogbackHelper.printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackPropertyDefiner.PROP_LOG_FILE_POLICY, LogbackPropertyDefiner.getPropertyValue(LogbackPropertyDefiner.PROP_LOG_FILE_POLICY));
		LogbackHelper.printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackPropertyDefiner.PROP_OVERRIDE_DIR, LogbackPropertyDefiner.getPropertyValue(LogbackPropertyDefiner.PROP_OVERRIDE_DIR));
		
		// Locate logback configuration file:
		// 1 - look into custom webappsConfig directory (see findURLOfCustomConfigurationFile)
		//		1.1 look for '/path/to/webappsConfig/myWebappFullName/logback.xml'
		//		1.2 look for '/path/to/webappsConfig/logback.xml'
		// 2 - fallback on default methods (see findURLOfDefaultConfigurationFile)
		//		2.1 look for 'logback.configurationFile' system property
		//		2.2 look for 'logback.xml' in classpath
		URL logbackFileUrl = LogbackHelper.findURLOfCustomConfigurationFile(WebTopProps.getEtcDir(properties), webappFullName);
		if (logbackFileUrl == null) {
			logbackFileUrl = LogbackHelper.findURLOfDefaultConfigurationFile(classLoader);
		}
		
		// Reload logback configuration
		try {
			LogbackHelper.loadConfiguration(loggerContext, logbackFileUrl);
			LogbackHelper.printToSystemOut("[{}] Logback: using configuration file at '{}'", webappFullName, logbackFileUrl.toString());
		} catch(JoranException ex) {
			LogbackHelper.printToSystemOut("[{}] Unable to reload logback configuration", webappFullName);
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
	}
	
	public void initApp(ServletContext servletContext, String webappFullName, Properties properties) throws IllegalStateException {
		servletContext.setAttribute(WEBAPPNAME_ATTRIBUTE_KEY, webappFullName);
		if (servletContext.getAttribute(WEBTOPAPP_ATTRIBUTE_KEY) != null) {
			throw new IllegalStateException("There is already a WebTop application associated with the current ServletContext.");
		}
		
		try {
			WebTopApp wta = new WebTopApp(servletContext, properties);
			wta.boot();
			servletContext.setAttribute(WEBTOPAPP_ATTRIBUTE_KEY, wta);
			servletContext.setAttribute(JWTSignatureVerifier.SECRET_CONTEXT_ATTRIBUTE, wta.getDocumentServerSecretIn());
			
			Dynamic atmosphereServlet = servletContext.addServlet("AtmosphereServlet", com.sonicle.webtop.core.app.atmosphere.AtmosphereServlet.class);
			atmosphereServlet.setInitParameter(ApplicationConfig.ANALYTICS, "false");
			atmosphereServlet.setInitParameter(ApplicationConfig.SCHEDULER_THREADPOOL_MAXSIZE, String.valueOf(WebTopProps.getAtmosphereMaxSchedulerThreads(properties)));
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_SHARABLE_THREAD_POOLS, "true");
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_MESSAGE_PROCESSING_THREADPOOL_MAXSIZE, String.valueOf(WebTopProps.getAtmosphereMaxProcessingThreads(properties)));
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_ASYNC_WRITE_THREADPOOL_MAXSIZE, String.valueOf(WebTopProps.getAtmosphereMaxWriteThreads(properties)));
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_LIFECYCLE_POLICY, "BroadcasterLifeCyclePolicy.EMPTY");
			atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_COMET_SUPPORT, "org.atmosphere.container.JSR356AsyncSupport");
			//atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_COMET_SUPPORT, "org.atmosphere.container.Tomcat7CometSupport");
			atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_SESSION_SUPPORT, "true");
			atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_SESSION_CREATE, "false");
			atmosphereServlet.setInitParameter(ApplicationConfig.HEARTBEAT_INTERVAL_IN_SECONDS, "10");
			atmosphereServlet.setInitParameter(ApplicationConfig.HEARTBEAT_PADDING_CHAR, "X");
			atmosphereServlet.setInitParameter(ApplicationConfig.CLIENT_HEARTBEAT_INTERVAL_IN_SECONDS, "60");
			//atmosphereServlet.setInitParameter(ApplicationConfig.ATMOSPHERERESOURCE_INTERCEPTOR_TIMEOUT, "60");
			
			atmosphereServlet.setLoadOnStartup(1);
			atmosphereServlet.setAsyncSupported(true);
			atmosphereServlet.addMapping(com.sonicle.webtop.core.app.atmosphere.AtmosphereServlet.URL + "/*");
			
			Dynamic resourcesServlet = servletContext.addServlet("ResourcesServlet", com.sonicle.webtop.core.app.servlet.ResourceRequest.class);
			resourcesServlet.setLoadOnStartup(1);
			resourcesServlet.addMapping(com.sonicle.webtop.core.app.servlet.ResourceRequest.URL + "/*");
			
			Dynamic loginServlet = servletContext.addServlet("LoginServlet", com.sonicle.webtop.core.app.servlet.Login.class);
			loginServlet.setLoadOnStartup(1);
			loginServlet.addMapping(com.sonicle.webtop.core.app.servlet.Login.URL + "/*");
			
			Dynamic logoutServlet = servletContext.addServlet("LogoutServlet", com.sonicle.webtop.core.app.servlet.Logout.class);
			logoutServlet.setLoadOnStartup(1);
			logoutServlet.addMapping(com.sonicle.webtop.core.app.servlet.Logout.URL + "/*");
			
			Dynamic otpServlet = servletContext.addServlet("OtpServlet", com.sonicle.webtop.core.app.servlet.Otp.class);
			otpServlet.setLoadOnStartup(1);
			otpServlet.addMapping(com.sonicle.webtop.core.app.servlet.Otp.URL + "/*");
			
			Dynamic uiPrivateServlet = servletContext.addServlet("UIPrivateServlet", com.sonicle.webtop.core.app.servlet.UIPrivate.class);
			uiPrivateServlet.setLoadOnStartup(1);
			uiPrivateServlet.addMapping(com.sonicle.webtop.core.app.servlet.UIPrivate.URL + "/*");
			
			Dynamic privateRequestServlet = servletContext.addServlet("PrivateRequestServlet", com.sonicle.webtop.core.app.servlet.PrivateRequest.class);
			privateRequestServlet.setLoadOnStartup(1);
			privateRequestServlet.addMapping(com.sonicle.webtop.core.app.servlet.PrivateRequest.URL + "/*");
			privateRequestServlet.addMapping(com.sonicle.webtop.core.app.servlet.PrivateRequest.URL_LEGACY + "/*");
			
			Dynamic publicRequestServlet = servletContext.addServlet("PublicRequestServlet", com.sonicle.webtop.core.app.servlet.PublicRequest.class);
			publicRequestServlet.setLoadOnStartup(1);
			publicRequestServlet.addMapping(com.sonicle.webtop.core.app.servlet.PublicRequest.URL + "/*");
			
			Dynamic docEditorServlet = servletContext.addServlet("DocEditorServlet", com.sonicle.webtop.core.app.servlet.DocEditor.class);
			docEditorServlet.setLoadOnStartup(1);
			docEditorServlet.addMapping(com.sonicle.webtop.core.app.servlet.DocEditor.URL + "/*");
			
			// Adds RestApiServlets dynamically
			ServiceManager svcMgr = wta.getServiceManager();
			for (String serviceId : svcMgr.listRegisteredServices()) {
				ServiceDescriptor desc = svcMgr.getDescriptor(serviceId);
				
				if (desc.hasOpenApiDefinitions() || desc.hasRestApiEndpoints()) {
					addRestApiServlet(servletContext, desc);
				}
			}
			
		} catch(Throwable t) {
			servletContext.removeAttribute(WEBTOPAPP_ATTRIBUTE_KEY);
			logger.error("Error initializing WTA [{}]", webappFullName, t);
		}
	}
	
	private void addRestApiServlet(ServletContext servletContext, ServiceDescriptor desc) {
		String serviceId = desc.getManifest().getId();
		String name = serviceId + "@RestApiServlet";
		String path = com.sonicle.webtop.core.app.servlet.RestApi.URL + "/" + serviceId + "/*";
		
		logger.debug("Adding RestApi servlet [{}] -> [{}]", name, path);
		Dynamic servlet = servletContext.addServlet(name, com.sonicle.webtop.core.app.servlet.RestApi.class);
		servlet.setInitParameter("javax.ws.rs.Application", "com.sonicle.webtop.core.app.JaxRsServiceApplication");
		servlet.setInitParameter("com.sun.jersey.config.feature.DisableWADL", "true");
		servlet.setInitParameter(RestApi.INIT_PARAM_WEBTOP_SERVICE_ID, serviceId);
		servlet.setLoadOnStartup(2);
		servlet.setAsyncSupported(true);
		servlet.addMapping(path);
	}
	
	protected void destroyApp(ServletContext servletContext) {
		final String appname = getWabappName(servletContext);
		try {
			WebTopApp.get(servletContext).shutdown();
			
		} catch(Throwable t) {
			logger.error("Error destroying WTA [{}]", appname, t);
		} finally {
			servletContext.removeAttribute(WEBTOPAPP_ATTRIBUTE_KEY);
		}
	}
	
	private String expandLogDirVariables(String logDir, String webappFullName) {
		if (logDir == null) return logDir;
		String s = StringUtils.replace(logDir, "${WEBAPP_FULLNAME}", webappFullName);
		s = StringUtils.replace(s, "${WEBAPP_NAME}", ContextUtils.stripWebappVersion(webappFullName));
		return s;
	}
}
