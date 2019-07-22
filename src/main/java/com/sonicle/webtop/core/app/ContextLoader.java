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
import com.sonicle.commons.PropUtils;
import com.sonicle.commons.web.ContextUtils;
import com.sonicle.webtop.core.app.servlet.RestApi;
import com.sonicle.webtop.core.app.shiro.filter.JWTSignatureVerifier;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.cpr.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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
	
	public void initLogging(ServletContext servletContext) {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		String webappFullName = ContextUtils.getWebappFullName(servletContext, false); // Like <context-name>##<context-version>
		ClassLoader classLoader = Loader.getClassLoaderOfObject(this);
		Properties systemProps = System.getProperties();
		
		// Locates logback configuration file: try custom (webappConfig) first, then standard ones
		URL logbackFileUrl = LogbackHelper.findURLOfCustomConfigurationFile(WebTopProps.getWebappsConfigDir(systemProps), webappFullName);
		if (logbackFileUrl == null) {
			logbackFileUrl = LogbackHelper.findURLOfDefaultConfigurationFile(classLoader);
		}
		
		// Preparing logback props
		String logDir = WebTopProps.getLogDir(systemProps);
		logDir = expandLogDirVariables(logDir, webappFullName);
		String logFileBasename = PropUtils.isDefined(systemProps, WebTopProps.LOG_FILE_BASENAME) ? WebTopProps.getLogFileBasename() : null;
		if (StringUtils.isBlank(logFileBasename)) logFileBasename = webappFullName;
		String logAppender = WebTopProps.getLogAppender(systemProps);
		
		try {
			// https://stackoverflow.com/questions/32595740/how-to-specify-file-path-dynamically-in-logback-xml
			// Fill props and write to logback file
			Properties logbackProps = new Properties();
			logbackProps.setProperty(LogbackHelper.PROP_APPENDER, logAppender);
			logbackProps.setProperty(LogbackHelper.PROP_LOG_DIR, logDir);
			logbackProps.setProperty(LogbackHelper.PROP_LOG_FILE_BASENAME, logFileBasename);
			LogbackHelper.writeProperties(Loader.getClassLoaderOfObject(this), logbackProps);
			
			printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackHelper.PROP_APPENDER, logbackProps.getProperty(LogbackHelper.PROP_APPENDER));
			printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackHelper.PROP_LOG_DIR, logbackProps.getProperty(LogbackHelper.PROP_LOG_DIR));
			printToSystemOut("[{}] Logback: using {} = {}", webappFullName, LogbackHelper.PROP_LOG_FILE_BASENAME, logbackProps.getProperty(LogbackHelper.PROP_LOG_FILE_BASENAME));
			
			// Reload configuration
			LogbackHelper.loadConfiguration(loggerContext, logbackFileUrl);
			printToSystemOut("[{}] Logback: using configuration file at '{}'", webappFullName, logbackFileUrl.toString());
			
		} catch(IOException | URISyntaxException ex) {
			printToSystemOut("[{}] Unable to write logback properties file", webappFullName);
			printToSystemOut("{}", ex);
		} catch(JoranException ex) {
			printToSystemOut("[{}] Unable to reload logback configuration", webappFullName);
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
	}
	
	public void initApp(ServletContext servletContext) throws IllegalStateException {
		String webappName = ContextUtils.getWebappFullName(servletContext, false);
		servletContext.setAttribute(WEBAPPNAME_ATTRIBUTE_KEY, webappName);
		if (servletContext.getAttribute(WEBTOPAPP_ATTRIBUTE_KEY) != null) {
			throw new IllegalStateException("There is already a WebTop application associated with the current ServletContext.");
		}
		
		try {
			WebTopApp wta = new WebTopApp(servletContext);
			wta.boot();
			servletContext.setAttribute(WEBTOPAPP_ATTRIBUTE_KEY, wta);
			servletContext.setAttribute(JWTSignatureVerifier.SECRET_CONTEXT_ATTRIBUTE, wta.getDocumentServerSecretIn());
			
			Dynamic atmosphereServlet = servletContext.addServlet("AtmosphereServlet", com.sonicle.webtop.core.app.atmosphere.AtmosphereServlet.class);
			atmosphereServlet.setInitParameter(ApplicationConfig.ANALYTICS, "false");
			atmosphereServlet.setInitParameter(ApplicationConfig.SCHEDULER_THREADPOOL_MAXSIZE, "10");
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_SHARABLE_THREAD_POOLS, "true");
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_MESSAGE_PROCESSING_THREADPOOL_MAXSIZE, "10");
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_ASYNC_WRITE_THREADPOOL_MAXSIZE, "10");
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_CACHE, "com.sonicle.webtop.core.app.atmosphere.UUIDBroadcasterCache");
			atmosphereServlet.setInitParameter(ApplicationConfig.BROADCASTER_LIFECYCLE_POLICY, "BroadcasterLifeCyclePolicy.EMPTY");
			atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_COMET_SUPPORT, "org.atmosphere.container.JSR356AsyncSupport");
			//atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_COMET_SUPPORT, "org.atmosphere.container.Tomcat7CometSupport");
			atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_SESSION_SUPPORT, "true");
			atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_SESSION_CREATE, "false");
			atmosphereServlet.setInitParameter(ApplicationConfig.HEARTBEAT_INTERVAL_IN_SECONDS, "10");
			atmosphereServlet.setInitParameter(ApplicationConfig.HEARTBEAT_PADDING_CHAR, "X");
			
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
			logger.error("Error initializing WTA [{}]", webappName, t);
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
	
	private void printToSystemOut(String message, Object... arguments) {
		System.out.println(MessageFormatter.arrayFormat(message, arguments).getMessage());
	}
	
	private String expandLogDirVariables(String logDir, String webappFullName) {
		if (logDir == null) return logDir;
		String s = StringUtils.replace(logDir, "${WEBAPP_FULLNAME}", webappFullName);
		s = StringUtils.replace(s, "${WEBAPP_NAME}", ContextUtils.stripWebappVersion(webappFullName));
		return s;
	}
}
