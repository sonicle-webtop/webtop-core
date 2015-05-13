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
import com.sonicle.webtop.core.bol.js.JsWTS;
import com.sonicle.webtop.core.sdk.CoreLocaleKey;
import com.sonicle.webtop.core.sdk.Environment;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.servlet.ServletHelper;
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
	
	public static final String ATTRIBUTE = "webtopsession";
	private static final Logger logger = WT.getLogger(WebTopSession.class);
	
	private final HttpSession httpSession;
	private final WebTopApp wta;
	private boolean initialized = false;
	private final HashMap<String, Object> properties = new HashMap<>();
	private UserProfile profile = null;
	private String refererURI = null;
	private Locale userAgentLocale = null;
	private ReadableUserAgent userAgentInfo = null;
	private CoreServiceSettings coreServiceSettings = null;
	private CoreUserSettings coreUserSettings = null;
	private Environment basicEnv = null;
	private CoreEnvironment fullEnv = null;
	private final LinkedHashMap<String, BaseService> services = new LinkedHashMap<>();
	private SessionComManager comm = null;
	
	WebTopSession(HttpSession session) {
		httpSession = session;
		wta = WebTopApp.get(session.getServletContext());
	}
	
	void destroy() throws Exception {
		ServiceManager svcm = wta.getServiceManager();
		
		// Cleanup services
		synchronized(services) {
			for(BaseService instance : services.values()) {
				svcm.cleanupService(instance);
			}
			services.clear();
		}
		// Unregister this session
		wta.getSessionManager().unregisterSession(this);
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
	
	/**
	 * Returns the session ID.
	 * @return HttpSession unique identifier.
	 */
	public String getId() {
		return httpSession.getId();
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
			return userAgentLocale;
		}
	}
	
	public String getRefererURI() {
		return refererURI;
	}
	
	/**
	 * Sets a property into session hashmap.
	 * @param key The property key.
	 * @param value The property value.
	 */
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	/**
	 * Gets a property value from session hashmap.
	 * @param key The property key.
	 * @return Requested property value.
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	/**
	 * Checks if session contains specified property.
	 * @param key The property key.
	 * @return True if property is found, false otherwise.
	 */
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}
	
	/**
	 * Clears/removes specified property.
	 * @param key The property key.
	 */
	public void clearProperty(String key) {
		properties.remove(key);
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
		userAgentLocale = ServletHelper.homogenizeLocale(request);
		userAgentInfo = wta.getUserAgentInfo(ServletHelper.getUserAgent(request));
		
		// Defines useful instances (NB: keep new order)
		coreServiceSettings = new CoreServiceSettings(principal.getDomainId(), CoreManifest.ID);
		coreUserSettings = new CoreUserSettings(principal.getDomainId(), principal.getUserId(), CoreManifest.ID);
		basicEnv = new Environment(wta, this);
		fullEnv = new CoreEnvironment(wta, this);
		profile = new UserProfile(fullEnv, principal);
		
		// Creates communication manager and registers this active session
		comm = new SessionComManager(profile.getId());
		wta.getSessionManager().registerSession(this);
		
		// Instantiates services
		BaseService instance = null;
		List<String> serviceIds = wta.getManager().getUserServices(profile);
		int count = 0;
		// TODO: order services list
		for(String serviceId : serviceIds) {
			//TODO: check if service is allowed for user
			if(!svcm.getDescriptor(serviceId).hasDefaultService()) continue;
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
	
	/**
	 * Stores service instance into this session.
	 * @param service 
	 */
	private void addService(BaseService service) {
		String serviceId = service.getManifest().getId();
		synchronized(services) {
			if(services.containsKey(serviceId)) throw new RuntimeException("Cannot add service twice");
			services.put(serviceId, service);
		}
	}
	
	/**
	 * Gets a service instance by ID.
	 * @param serviceId The service ID.
	 * @return The service instance, if found.
	 */
	public BaseService getServiceById(String serviceId) {
		synchronized(services) {
			if(!services.containsKey(serviceId)) throw new RuntimeException(MessageFormat.format("No service with ID: '{0}'", serviceId));
			return services.get(serviceId);
		}
	}
	
	/**
	 * Gets instantiated services list.
	 * @return A list of service ids.
	 */
	public List<String> getServices() {
		synchronized(services) {
			return Arrays.asList(services.keySet().toArray(new String[services.size()]));
		}
	}
	
	public void fillServices(JsWTS js) {
		JsWTS.Service last = null;
		String deflt = null;
		int index;
		for(String serviceId : getServices()) {
			fillStartupForService(js, serviceId);
			index = js.services.size()-1; // Last inserted
			last = js.services.get(index);
			last.index = index; // Position is (for convenience) also saved inside!
			if((deflt == null) && !last.id.equals(CoreManifest.ID) && !last.maintenance) {
				// Candidate startup (default) service must not be in maintenance
				// and id should not be equal to core service!
				deflt = last.id;
			}
		}
		js.defaultService = deflt;
	}
	
	private JsWTS.Service fillStartupForService(JsWTS js, String serviceId) {
		ServiceManager svcm = wta.getServiceManager();
		ServiceDescriptor sdesc = svcm.getDescriptor(serviceId);
		ServiceManifest manifest = sdesc.getManifest();
		Locale locale = getLocale();
		
		// Defines paths and requires
		if(serviceId.equals(CoreManifest.ID)) {
			js.appRequires.add(manifest.getServiceJsClassName(true));
			js.appRequires.add(manifest.getLocaleJsClassName(locale, true));
		} else {
			js.appRequires.add(manifest.getServiceJsClassName(true));
			js.appRequires.add(manifest.getLocaleJsClassName(locale, true));
			js.appPaths.put(manifest.getJsPackageName(), manifest.getJsBaseUrl());
		}
		
		// Completes service info
		JsWTS.Service jssvc = new JsWTS.Service();
		jssvc.id = manifest.getId();
		jssvc.xid = manifest.getXId();
		jssvc.ns = manifest.getJsPackageName();
		jssvc.path = manifest.getJsBaseUrl();
		jssvc.localeClassName = manifest.getLocaleJsClassName(locale, true);
		jssvc.serviceClassName = manifest.getServiceJsClassName(true);
		jssvc.clientOptionsClassName = manifest.getClientOptionsModelJsClassName(true);
		if(sdesc.hasUserOptionsService()) {
			jssvc.userOptions = new JsWTS.ServiceUserOptions(
				manifest.getUserOptionsViewJsClassName(true),
				manifest.getUserOptionsModelJsClassName(true)
			);
		}
		jssvc.name = wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_NAME);
		jssvc.description = wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_DESCRIPTION);
		jssvc.version = manifest.getVersion().toString();
		jssvc.build = manifest.getBuildDate();
		jssvc.company = manifest.getCompany();
		jssvc.maintenance = svcm.isInMaintenance(serviceId);
		js.services.add(jssvc);
		js.servicesOptions.add(getClientOptions(serviceId));
		
		return jssvc;
	}
	
	private JsWTS.Settings getClientOptions(String serviceId) {
		BaseService svc = getServiceById(serviceId);
		
		// Gets initial settings from instantiated service
		HashMap<String, Object> hm = null;
		try {
			WebTopApp.setServiceLoggerDC(serviceId);
			hm = svc.returnClientOptions();
		} catch(Exception ex) {
			logger.error("returnStartupOptions method returns errors", ex);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
		
		JsWTS.Settings is = new JsWTS.Settings();
		if(hm != null) is.putAll(hm);
		
		// Built-in settings
		if(serviceId.equals(CoreManifest.ID)) {
			//is.put("authTicket", generateAuthTicket());
			is.put("isWhatsnewNeeded", isWhatsnewNeeded());
		} else {
			CoreUserSettings cus = new CoreUserSettings(profile.getDomainId(), profile.getUserId(), serviceId);
			is.put("viewportToolWidth", cus.getViewportToolWidth());
		}
		return is;
	}
	
	private boolean isWhatsnewNeeded() {
		ServiceManager svcm = wta.getServiceManager();
		boolean needWhatsnew = false;
		for(String serviceId : getServices()) {
			needWhatsnew = needWhatsnew | svcm.needWhatsnew(serviceId, profile);
		}
		return needWhatsnew;
	}
	
	public CoreServiceSettings getCoreServiceSettings() {
		return coreServiceSettings;
	}
	
	public CoreUserSettings getCoreUserSettings() {
		return coreUserSettings;
	}
	
	public void setWebSocketEndpoint(WebSocket wsm) {
		comm.setWebSocketEndpoint(wsm);
	}
	
	public void nofity(ServiceMessage message) {
		comm.nofity(message);
	}
	
	public void nofity(List<ServiceMessage> messages) {
		comm.nofity(messages);
	}
	
	public List<ServiceMessage> getEnqueuedMessages() {
		return comm.popEnqueuedMessages();
	}
}
