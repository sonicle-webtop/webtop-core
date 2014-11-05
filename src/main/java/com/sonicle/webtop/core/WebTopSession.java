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

import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.bol.js.JsWTStartup;
import com.sonicle.webtop.core.sdk.FullEnvironment;
import com.sonicle.webtop.core.sdk.BasicEnvironment;
import com.sonicle.webtop.core.sdk.Environment;
import com.sonicle.webtop.core.sdk.Service;
import com.sonicle.webtop.core.sdk.WebSocketMessage;
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.ws.WebSocketManager;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class WebTopSession {
	
	private static final Logger logger = WebTopApp.getLogger(WebTopSession.class);
	public static final String ATTRIBUTE = "webtopsession";
	private WebTopApp wta = null;
	private boolean initialized = false;
	private UserProfile profile = null;
	private Locale locale = null;
	private String refererURI = null;
	private ReadableUserAgent userAgentInfo = null;
	private CoreServiceSettings coreServiceSettings = null;
	private CoreUserSettings coreUserSettings = null;
	private Environment basicEnv = null;
	private CoreEnvironment fullEnv = null;
	private final LinkedHashMap<String, Service> services = new LinkedHashMap<>();
	private WebSocketManager wsm=null;
	
	WebTopSession(HttpSession session) {
		wta = WebTopApp.get(session.getServletContext());
	}
	
	void destroy() {
		ServiceManager svcm = wta.getServiceManager();
		
		// Cleanup services
		synchronized(services) {
			for(Service instance : services.values()) {
				svcm.cleanupDefaultService(instance);
			}
			services.clear();
		}
	}
	
	/**
	 * Called from servlet package in order to init environment
	 * @param request 
	 * @throws java.lang.Exception 
	 */
	public synchronized void checkEnvironment(HttpServletRequest request) throws Exception {
		if(!initialized) {
			initializeEnvironment(request);
		} else {
			logger.debug("Environment aready initialized");
		}
	}
	
	private void initializeEnvironment(HttpServletRequest request) throws Exception {
		ServiceManager svcm = wta.getServiceManager();
		Principal principal = (Principal)SecurityUtils.getSubject().getPrincipal();
		
		logger.debug("Creating environment for {}", principal.getName());
		
		refererURI = ServletHelper.getReferer(request);
		locale = ServletHelper.homogenizeLocale(request);
		userAgentInfo = wta.getUserAgentInfo(ServletHelper.getUserAgent(request));
		
		// Defines useful instances (NB: keep new order)
		coreServiceSettings = new CoreServiceSettings(principal.getDomainId(), CoreManifest.ID);
		coreUserSettings = new CoreUserSettings(principal.getDomainId(), principal.getUserId(), CoreManifest.ID);
		basicEnv = new Environment(wta, this);
		fullEnv = new CoreEnvironment(wta, this);
		profile = new UserProfile(fullEnv, principal);
		
		// Instantiates services
		Service instance = null;
		List<String> serviceIds = wta.getManager().getUserServices(profile);
		int count = 0;
		// TODO: order services list
		for(String serviceId : serviceIds) {
			//TODO: check if service is allowed for user
			// Creates new instance
			if(svcm.hasFullRights(serviceId)) {
				instance = svcm.instantiateService(serviceId, basicEnv, fullEnv);
			} else {
				instance = svcm.instantiateService(serviceId, basicEnv, null);
			}
			if(instance != null) {
				addService(instance);
				count++;
			}
		}
		
		logger.debug("Instantiated {} services", count);
		initialized = true;
	}
	
	private void addService(Service service) {
		String serviceId = service.getManifest().getId();
		synchronized(services) {
			if(services.containsKey(serviceId)) throw new RuntimeException("Cannot add service twice");
			services.put(serviceId, service);
		}
	}
	
	public Service getServiceById(String serviceId) {
		synchronized(services) {
			if(!services.containsKey(serviceId)) throw new RuntimeException(MessageFormat.format("No service with ID: '{0}'", serviceId));
			return services.get(serviceId);
		}
	}
	
	public List<String> getServices() {
		synchronized(services) {
			return Arrays.asList(services.keySet().toArray(new String[services.size()]));
		}
	}
	
	public JsWTStartup.Settings getInitialSettings(String serviceId) {
		Service svc = getServiceById(serviceId);
		
		// Gets initial settings from instantiated service
		HashMap<String, Object> hm = null;
		try {
			WebTopApp.setServiceLoggerDC(serviceId);
			hm = svc.returnInitialSettings();
		} catch(Exception ex) {
			logger.error("returnInitialSettings method returns errors", ex);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
		
		JsWTStartup.Settings is = new JsWTStartup.Settings();
		if(hm != null) is.putAll(hm);
		
		if(!serviceId.equals(CoreManifest.ID)) {
			is.put(CoreUserSettings.VIEWPORT_TOOL_WIDTH, CoreUserSettings.getViewportToolWidth(wta.getSettingsManager(), profile, serviceId));
		}
		return is;
	}
	
	/**
	 * Gets parsed user-agent info.
	 * @return A readable ReadableUserAgent object. 
	 */
	public ReadableUserAgent getUserAgent() {
		return userAgentInfo;
	}
	
	/**
	 * Gets the user profile associated to the session.
	 * @return The UserProfile.
	 */
	public UserProfile getUserProfile() {
		return profile;
	}
	
	/**
	 * Return current locale.
	 * It can be the UserProfile's locale or the locale specified during
	 * the initial HTTP request to the server.
	 * @return The locale.
	 */
	public Locale getLocale() {
		if(profile != null) {
			return profile.getLocale();
		} else {
			return locale;
		}
	}
	public CoreServiceSettings getCoreServiceSettings() {
		return coreServiceSettings;
	}
	
	public CoreUserSettings getCoreUserSettings() {
		return coreUserSettings;
	}
	
	public String getRefererURI() {
		return refererURI;
	}

	public String getTheme() {
		return coreUserSettings.getTheme();
	}
	
	public void setTheme(String value) {
		wta.getSettingsManager().setUserSetting(profile, CoreManifest.ID, CoreUserSettings.THEME, value);
	}
	
	public String getLookAndFeel() {
		return coreUserSettings.getLookAndFeel();
	}
	
	public void setLookAndFeel(String value) {
		wta.getSettingsManager().setUserSetting(profile, CoreManifest.ID, CoreUserSettings.LAF, value);
	}
	
	public boolean getRTL() {
		return coreUserSettings.getRTL();
	}
	
	public void setRTL(String value) {
		wta.getSettingsManager().setUserSetting(profile, CoreManifest.ID, CoreUserSettings.RTL, value);
	}
	
	public void setViewportToolWidth(String serviceId, Integer value) {
		CoreUserSettings.setViewportToolWidth(wta.getSettingsManager(), profile, serviceId, value);
	}
	
	public void test() {
		logger.debug("TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
	}
	
	public void setWebSocketManager(WebSocketManager wsm) {
		this.wsm=wsm;
	}
	
	public void sendWebSocketMessage(WebSocketMessage wsmessage) throws IOException {
		if (this.wsm!=null) {
			this.wsm.sendMessage(wsmessage);
		}
	}
	
	
	
	/**
	 * Gets WebTopSession object stored as session's attribute.
	 * @param request The http request
	 * @return WebTopSession object
	 */
	public static WebTopSession get(HttpServletRequest request) {
		return get(request.getSession());
	}
	
	/**
	 * Gets WebTopSession object stored as session's attribute.
	 * @param session The http session
	 * @return WebTopSession object
	 */
	public static WebTopSession get(HttpSession session) {
		return (WebTopSession)(session.getAttribute(ATTRIBUTE));
	}
}
