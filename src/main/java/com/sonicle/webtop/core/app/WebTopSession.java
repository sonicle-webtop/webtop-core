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

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.bol.js.JsWTSPrivate;
import com.sonicle.webtop.core.bol.js.JsWTSPublic;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.ServiceSharePermission;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.servlet.Otp;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class WebTopSession {
	private static final Logger logger = WT.getLogger(WebTopSession.class);
	public static final String PROP_REQUEST_DUMP = "REQUESTDUMP";
	
	private Session session;
	private final WebTopApp wta;
	private final PropertyBag props = new PropertyBag();
	private int initLevel = 0;
	private UserProfile profile = null;
	private PrivateEnvironment privateEnv = null;
	private CorePrivateEnvironment privateCoreEnv = null;
	private PublicEnvironment publicEnv = null;
	private final HashMap<String, BaseManager> managers = new HashMap<>();
	private List<String> allowedServices = null;
	private final LinkedHashMap<String, BaseService> privateServices = new LinkedHashMap<>();
	private final LinkedHashMap<String, BasePublicService> publicServices = new LinkedHashMap<>();
	private final HashMap<String, UploadedFile> uploads = new HashMap<>();
	private SessionComManager comm = null;
	
	WebTopSession(WebTopApp wta, Session session) {
		this.wta = wta;
		this.session = session;
	}
	
	void cleanup() throws Exception {
		initLevel = -1;
		
		emptyPrivateServices();
		emptyPublicServices();
		emptyServiceManagers();
		
		// Cleanup uploads
		String domainId = getProfileDomainId();
		if(domainId != null) {
			synchronized(uploads) {
				for(UploadedFile upf : uploads.values()) {
					if(!upf.isVirtual()) wta.deleteTempFile(domainId, upf.getUploadId());
				}
				uploads.clear();
			}
		}
	}
	
	/**
	 * Returns the associated user session.
	 * @return Session object
	 */
	public Session getSession() {
		return session;
	}
	
	/**
	 * Returns the session ID.
	 * @return Session unique identifier
	 */
	public String getId() {
		return session.getId().toString();
	}
	
	public Object setProperty(String serviceId, String key, Object value) {
		synchronized(props) {
			props.set(serviceId+"@"+key, value);
			return value;
		}
	}
	
	public Object getProperty(String serviceId, String key) {
		synchronized(props) {
			return props.get(serviceId+"@"+key);
		}
	}
	
	public Object popProperty(String serviceId, String key) {
		synchronized(props) {
			if(hasProperty(serviceId, key)) {
				Object value = props.get(serviceId+"@"+key);
				clearProperty(serviceId, key);
				return value;
			} else {
				return null;
			}
		}
	}
	
	public void clearProperty(String serviceId, String key) {
		synchronized(props) {
			props.clear(serviceId+"@"+key);
		}
	}
	
	public boolean hasProperty(String serviceId, String key) {
		synchronized(props) {
			return props.has(serviceId+"@"+key);
		}
	}
	
	/**
	 * Returns client's IP address.
	 * @return The network address. 
	 */
	public String getRemoteIP() {
		return SessionManager.getClientIP(session);
	}
	
	/**
	 * Returns plain client's user-agent info.
	 * @return Bowser user-agent. 
	 */
	public String getPlainUserAgent() {
		return SessionManager.getClientUserAgent(session);
	}
	
	/**
	 * Returns parsed client's user-agent info.
	 * @return A readable ReadableUserAgent object. 
	 */
	public ReadableUserAgent getUserAgent() {
		return SessionManager.getClientReadableUserAgent(session);
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
			return SessionManager.getClientLocale(session);
		}
	}
	
	public String getRefererURI() {
		return SessionManager.getRefererUri(session);
	}
	
	public boolean isReady() {
		return initLevel == 2;
	}
	
	/**
	 * Gets the user profile associated to the session.
	 * @return The UserProfile.
	 */
	public UserProfile getUserProfile() {
		return profile;
	}
	
	/**
	 * Gets the UserProfile's ID.
	 * Note that this can be null if the user is not authenticated. (eg. public area)
	 * @return The UserProfile's ID
	 */
	public UserProfile.Id getProfileId() {
		return (profile == null) ? null : profile.getId();
	}
	
	/**
	 * Gets Profile's Domain ID.
	 * Note that if the user is not authenticated the virtual domain ID is returned instead.
	 * @return The profile's domain ID
	 */
	public String getProfileDomainId() {
		if(profile == null) {
			//TODO: restituire l'id del dominio decodificato dall'host
			return null;
		} else {
			return profile.getDomainId();
		}
	}
	
	/**
	 * Gets Profile's User ID.
	 * Note that this can be null if the user is not authenticated. (eg. public area)
	 * @return The profile's user ID
	 */
	public String getProfileUserId() {
		return (profile == null) ? null : profile.getUserId();
	}
	
	public synchronized void initPrivate(HttpServletRequest request) throws WTException {
		if(initLevel < 0) return;
		if(initLevel == 0) internalInitPrivate(request);
	}
	
	public synchronized void initPrivateEnvironment(HttpServletRequest request) throws WTException {
		if(initLevel < 0) return;
		if(initLevel == 0) throw new WTException("You need to call initPrivate() before calling this method!");
		if(initLevel == 1) internalInitPrivateEnvironment(request);
	}
	
	public synchronized void initPublicEnvironment(HttpServletRequest request, String publicServiceId) throws WTException {
		internalInitPublicEnvironment(request, publicServiceId);
	}
	
	private void internalInitPrivate(HttpServletRequest request) throws WTException {
		ServiceManager svcm = wta.getServiceManager();
		Principal principal = (Principal)SecurityUtils.getSubject().getPrincipal();
		
		Subject subject = RunContext.getSubject();
		UserProfile.Id profileId = RunContext.getProfileId(subject);
		
		emptyServiceManagers();
		
		CoreManager core = svcm.instantiateCoreManager(false, profileId);
		cacheServiceManager(CoreManifest.ID, core);
		
		// Defines useful instances (NB: keep code assignment order!!!)
		profile = new UserProfile(core, principal);
		
		boolean otpEnabled = wta.getOTPManager().isEnabled(profile.getId());
		if(!otpEnabled) setProperty(CoreManifest.ID, Otp.WTSPROP_OTP_VERIFIED, true);
		
		initLevel = 1;
	}
	
	private void internalInitPrivateEnvironment(HttpServletRequest request) throws WTException {
		// Calling method MUST be synchronized!
		ServiceManager svcm = wta.getServiceManager();
		SessionManager sesm = wta.getSessionManager();
		CoreManager core = WT.getCoreManager(profile.getId());
		String sessionId = getId();
		
		privateCoreEnv = new CorePrivateEnvironment(wta, this);
		privateEnv = new PrivateEnvironment(this);
		
		wta.getLogManager().write(profile.getId(), CoreManifest.ID, "AUTHENTICATED", null, request, getId(), null);
		sesm.registerWebTopSession(sessionId, this);
		comm = new SessionComManager(sesm, sessionId, profile.getId());
		
		allowedServices = core.listAllowedServices();
		
		BaseManager managerInst = null;
		BaseService privateInst = null;
		for(String serviceId : allowedServices) {
			ServiceDescriptor descriptor = svcm.getDescriptor(serviceId);
			// Manager
			// Skip core service... its manager has already been instantiated above (see: internalInitPrivate)
			if(!serviceId.equals(CoreManifest.ID)) {
				if(descriptor.hasManager()) {
					managerInst = svcm.instantiateServiceManager(serviceId, false, profile.getId());
					if(managerInst != null) {
						cacheServiceManager(serviceId, managerInst);
					}
				}
			}
			// PrivateService
			if(descriptor.hasPrivateService()) {
				// Creates new instance
				if(svcm.hasFullRights(serviceId)) {
					privateInst = svcm.instantiatePrivateService(serviceId, privateCoreEnv);
				} else {
					privateInst = svcm.instantiatePrivateService(serviceId, privateEnv);
				}
				if(privateInst != null) {
					cachePrivateService(privateInst);
				}
			}
		}
		
		logger.debug("Instantiated {} managers", managers.size());
		logger.debug("Instantiated {} private services", privateServices.size());
		
		/*
		// Instantiates services
		BaseService instance = null;
		List<String> serviceIds = core.listPrivateServices();
		int count = 0;
		// TODO: ordinamento lista servizi (scelta dall'utente?)
		for(String serviceId : serviceIds) {
			// Creates new instance
			if(svcm.hasFullRights(serviceId)) {
				instance = svcm.instantiatePrivateService(serviceId, sessionId, new CoreEnvironment(wta, this));
			} else {
				instance = svcm.instantiatePrivateService(serviceId, sessionId, new Environment(this));
			}
			if(instance != null) {
				registerPrivateService(instance);
				count++;
			}
		}
		
		logger.debug("Instantiated {} services", count);
		*/
		
		initLevel = 2;
	}
	
	private void internalInitPublicEnvironment(HttpServletRequest request, String publicServiceId) throws WTException {
		ServiceManager svcm = wta.getServiceManager();
		
		if(!isServiceManagerCached(CoreManifest.ID)) {
			CoreManager core = svcm.instantiateCoreManager(false, RunContext.getProfileId());
			cacheServiceManager(CoreManifest.ID, core);
		}
		if(publicEnv == null) publicEnv = new PublicEnvironment(this);
		
		int managersCount = 0, publicCount = 0;
		BaseManager managerInst = null;
		BasePublicService publicInst = null;
		String[] serviceIds = new String[]{CoreManifest.ID, publicServiceId};
		for(String serviceId : serviceIds) {
			ServiceDescriptor descriptor = svcm.getDescriptor(serviceId);
			// Manager
			if(!serviceId.equals(CoreManifest.ID)) {
				if(descriptor.hasManager() && !isServiceManagerCached(serviceId)) {
					managerInst = svcm.instantiateServiceManager(serviceId, true, RunContext.getProfileId());
					if(managerInst != null) {
						cacheServiceManager(serviceId, managerInst);
						managersCount++;
					}
				}
			}
			// PublicService
			if(descriptor.hasPublicService() && !isPublicServiceCached(serviceId)) {
				publicInst = svcm.instantiatePublicService(serviceId, publicEnv);
				if(publicInst != null) {
					cachePublicService(publicInst);
					publicCount++;
				}
			}
		}
		
		logger.debug("Instantiated {} managers", managersCount);
		logger.debug("Instantiated {} public services", publicCount);
	}
	
	
	private void cacheServiceManager(String serviceId, BaseManager manager) {
		synchronized(managers) {
			if(managers.containsKey(serviceId)) throw new WTRuntimeException("Cannot add manager twice");
			managers.put(serviceId, manager);
		}
	}
	
	public boolean isServiceManagerCached(String serviceId) {
		synchronized(managers) {
			return managers.containsKey(serviceId);
		}
	}
	
	public BaseManager getServiceManager(String serviceId) {
		synchronized(managers) {
			if(!managers.containsKey(serviceId)) return null;
			return managers.get(serviceId);
		}
	}
	
	private void emptyServiceManagers() {
		synchronized(managers) {
			managers.clear();
		}
	}
	
	/**
	 * Stores private service instance into this session.
	 * @param service 
	 */
	private void cachePrivateService(BaseService service) {
		String serviceId = service.getManifest().getId();
		synchronized(privateServices) {
			if(privateServices.containsKey(serviceId)) throw new WTRuntimeException("Cannot add private service twice");
			privateServices.put(serviceId, service);
		}
	}
	
	/**
	 * Gets a private service instance by ID.
	 * @param serviceId The service ID.
	 * @return The service instance, if found.
	 */
	public BaseService getPrivateServiceById(String serviceId) {
		if(!isReady()) return null;
		synchronized(privateServices) {
			if(!privateServices.containsKey(serviceId)) throw new WTRuntimeException("No private service with ID [{0}]", serviceId);
			return privateServices.get(serviceId);
		}
	}
	
	/**
	 * Gets instantiated services list.
	 * @return A list of service ids.
	 */
	public List<String> getPrivateServices() {
		if(!isReady()) return null;
		synchronized(privateServices) {
			return Arrays.asList(privateServices.keySet().toArray(new String[privateServices.size()]));
		}
	}
	
	private void emptyPrivateServices() {
		ServiceManager svcm = wta.getServiceManager();
		synchronized(privateServices) {
			for(BaseService instance : privateServices.values()) {
				svcm.cleanupPrivateService(instance);
			}
			privateServices.clear();
		}
	}
	
	/**
	 * Stores public service instance into this session.
	 * @param service 
	 */
	private void cachePublicService(BasePublicService service) {
		String serviceId = service.getManifest().getId();
		synchronized(publicServices) {
			if(publicServices.containsKey(serviceId)) throw new WTRuntimeException("Cannot add public service twice");
			publicServices.put(serviceId, service);
		}
	}
	
	/**
	 * Checks if a public service instance exists.
	 * @param serviceId The service ID.
	 * @return True if instance is present, false otherwise.
	 */
	public boolean isPublicServiceCached(String serviceId) {
		synchronized(publicServices) {
			return publicServices.containsKey(serviceId);
		}
	}
	
	/**
	 * Gets a public service instance by ID.
	 * @param serviceId The service ID.
	 * @return The service instance, if found.
	 */
	public BasePublicService getPublicServiceById(String serviceId) {
		synchronized(publicServices) {
			if(!publicServices.containsKey(serviceId)) throw new WTRuntimeException("No public service with ID [{0}]", serviceId);
			return publicServices.get(serviceId);
		}
	}
	
	/**
	 * Gets instantiated public services list.
	 * @return A list of service ids.
	 */
	public List<String> getPublicServices() {
		synchronized(publicServices) {
			return Arrays.asList(publicServices.keySet().toArray(new String[publicServices.size()]));
		}
	}
	
	private void emptyPublicServices() {
		ServiceManager svcm = wta.getServiceManager();
		synchronized(publicServices) {
			for(BasePublicService instance : publicServices.values()) {
				svcm.cleanupPublicService(instance);
			}
			publicServices.clear();
		}
	}
	
	public void fillStartup(JsWTSPrivate js, String layout) {
		if(!isReady()) return;
		
		Locale locale = getLocale();
		js.securityToken = RunContext.getCSRFToken();
		js.layoutClassName = StringUtils.capitalize(layout);
		js.fileTypes = wta.getFileTypes().toString();
		
		// Evaluate services
		JsWTSPrivate.Service last = null;
		String deflt = null;
		for(String serviceId : getPrivateServices()) {
			fillStartupForService(js, serviceId, locale);
			last = js.services.get(js.services.size()-1);
			//TODO: gestire la manutenzione
			if((deflt == null) && !last.id.equals(CoreManifest.ID) && !last.maintenance) {
				// Candidate startup (default) service must not be in maintenance
				// and id should not be equal to core service!
				deflt = last.id;
			}
		}
		js.defaultService = deflt;
	}
	
	private JsWTSPrivate.Service fillStartupForService(JsWTSPrivate js, String serviceId, Locale locale) {
		ServiceManager svcm = wta.getServiceManager();
		ServiceDescriptor sdesc = svcm.getDescriptor(serviceId);
		ServiceManifest manifest = sdesc.getManifest();
		Subject subject = RunContext.getSubject();
		
		JsWTSPrivate.Permissions perms = new JsWTSPrivate.Permissions();
		
		// Generates service auth permissions
		for(ServicePermission perm : manifest.getDeclaredPermissions()) {
			if(perm instanceof ServiceSharePermission) continue;
			
			JsWTSPrivate.Actions acts = new JsWTSPrivate.Actions();
			for(String act : perm.getActions()) {
				if(RunContext.isPermitted(subject, serviceId, perm.getGroupName(), act)) {
					acts.put(act, true);
				}
			}
			if(!acts.isEmpty()) perms.put(perm.getGroupName(), acts);
		}
		
		if(svcm.isCoreService(serviceId)) {
			// Defines paths and requires
			js.appRequires.add(manifest.getPrivateServiceJsClassName(true));
			js.appRequires.add(manifest.getLocaleJsClassName(locale, true));
		} else {
			// Defines paths and requires
			js.appPaths.put(manifest.getJsPackageName(), manifest.getJsBaseUrl());
			js.appRequires.add(manifest.getPrivateServiceJsClassName(true));
			js.appRequires.add(manifest.getLocaleJsClassName(locale, true));
		}
		
		// Completes service info
		JsWTSPrivate.Service jssvc = new JsWTSPrivate.Service();
		jssvc.index = js.services.size();
		jssvc.id = manifest.getId();
		jssvc.xid = manifest.getXId();
		jssvc.ns = manifest.getJsPackageName();
		jssvc.path = manifest.getJsBaseUrl();
		jssvc.localeClassName = manifest.getLocaleJsClassName(locale, true);
		jssvc.serviceClassName = manifest.getPrivateServiceJsClassName(true);
		jssvc.serviceVarsClassName = manifest.getPrivateServiceVarsModelJsClassName(true);
		if(sdesc.hasUserOptionsService()) {
			jssvc.userOptions = new JsWTSPrivate.ServiceUserOptions(
				manifest.getUserOptionsViewJsClassName(true),
				manifest.getUserOptionsModelJsClassName(true)
			);
		}
		jssvc.name = StringEscapeUtils.escapeJson(wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_NAME));
		jssvc.description = StringEscapeUtils.escapeJson(wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_DESCRIPTION));
		jssvc.version = manifest.getVersion().toString();
		jssvc.build = manifest.getBuildDate();
		jssvc.company = StringEscapeUtils.escapeJson(manifest.getCompany());
		jssvc.maintenance = svcm.isInMaintenance(serviceId);
		
		js.services.add(jssvc);
		js.servicesVars.add(getServiceVars(serviceId));
		js.servicesPerms.add(perms);
		
		return jssvc;
	}
	
	private JsWTSPrivate.Vars getServiceVars(String serviceId) {
		BaseService svc = getPrivateServiceById(serviceId);
		BaseService.ServiceVars vars = null;
		
		// Retrieves initial vars from instantiated service
		try {
			WebTopApp.setServiceLoggerDC(serviceId);
			vars = svc.returnServiceVars();
		} catch(Exception ex) {
			logger.error("returnServiceVars method returns errors", ex);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
		
		JsWTSPrivate.Vars is = new JsWTSPrivate.Vars();
		if(vars != null) is.putAll(vars);
		
		// Built-in settings
		if(serviceId.equals(CoreManifest.ID)) {
			//is.put("authTicket", generateAuthTicket());
			is.put("isWhatsnewNeeded", isWhatsnewNeeded());
		} else {
			CoreUserSettings cus = new CoreUserSettings(serviceId, profile.getId());
			is.put("viewportToolWidth", cus.getViewportToolWidth());
		}
		return is;
	}
	
	public void fillStartup(JsWTSPublic js, String publicServiceId) {
		Locale locale = getLocale();
		js.fileTypes = wta.getFileTypes().toString();
		fillStartupForPublicService(js, CoreManifest.ID, locale);
		fillStartupForPublicService(js, publicServiceId, locale);
	}
	
	private JsWTSPublic.Service fillStartupForPublicService(JsWTSPublic js, String serviceId, Locale locale) {
		ServiceManager svcm = wta.getServiceManager();
		ServiceDescriptor sdesc = svcm.getDescriptor(serviceId);
		ServiceManifest manifest = sdesc.getManifest();
		
		if(svcm.isCoreService(serviceId)) {
			// Defines paths and requires
			js.appRequires.add(manifest.getPublicServiceJsClassName(true));
			js.appRequires.add(manifest.getLocaleJsClassName(locale, true));
		} else {
			// Defines paths and requires
			js.appPaths.put(manifest.getJsPackageName(), manifest.getJsBaseUrl());
			js.appRequires.add(manifest.getPublicServiceJsClassName(true));
			js.appRequires.add(manifest.getLocaleJsClassName(locale, true));
		}
		
		// Completes service info
		JsWTSPublic.Service jssvc = new JsWTSPublic.Service();
		jssvc.index = js.services.size();
		jssvc.id = manifest.getId();
		jssvc.xid = manifest.getXId();
		jssvc.ns = manifest.getJsPackageName();
		jssvc.path = manifest.getJsBaseUrl();
		jssvc.localeClassName = manifest.getLocaleJsClassName(locale, true);
		jssvc.serviceClassName = manifest.getPublicServiceJsClassName(true);
		jssvc.serviceVarsClassName = manifest.getPublicServiceVarsModelJsClassName(true);
		jssvc.name = StringEscapeUtils.escapeJson(wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_NAME));
		jssvc.description = StringEscapeUtils.escapeJson(wta.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_DESCRIPTION));
		jssvc.company = StringEscapeUtils.escapeJson(manifest.getCompany());
		jssvc.maintenance = svcm.isInMaintenance(serviceId);
		
		js.services.add(jssvc);
		js.servicesVars.add(getPublicServiceVars(serviceId));
		
		return jssvc;
	}
	
	private JsWTSPublic.Vars getPublicServiceVars(String serviceId) {
		BasePublicService svc = getPublicServiceById(serviceId);
		BasePublicService.ServiceVars vars = null;
		
		// Retrieves initial vars from instantiated service
		if(svc != null) {
			try {
				WebTopApp.setServiceLoggerDC(serviceId);
				vars = svc.returnServiceVars();
			} catch(Exception ex) {
				logger.error("returnServiceVars method returns errors", ex);
			} finally {
				WebTopApp.unsetServiceLoggerDC();
			}
		}
		
		JsWTSPublic.Vars is = new JsWTSPublic.Vars();
		if(vars != null) is.putAll(vars);
		return is;
	}
	
	private boolean isWhatsnewNeeded() {
		ServiceManager svcm = wta.getServiceManager();
		boolean needWhatsnew = false;
		for(String serviceId : getPrivateServices()) {
			needWhatsnew = needWhatsnew | svcm.needWhatsnew(serviceId, profile);
		}
		return needWhatsnew;
	}
	
	public boolean needWhatsnew(String serviceId, UserProfile profile) {
		if(!isReady()) return false;
		ServiceManager svcm = wta.getServiceManager();
		return svcm.needWhatsnew(serviceId, profile);
	}
	
	public String getWhatsnewHtml(String serviceId, UserProfile profile, boolean full) {
		if(!isReady()) return null;
		ServiceManager svcm = wta.getServiceManager();
		return svcm.getWhatsnew(serviceId, profile, full);
	}
	
	public void resetWhatsnew(String serviceId, UserProfile profile) {
		if(!isReady()) return;
		ServiceManager svcm = wta.getServiceManager();
		svcm.resetWhatsnew(serviceId, profile);
	}
	
	public void nofity(ServiceMessage message) {
		if(!isReady()) return;
		comm.nofity(message);
	}
	
	public void nofity(List<ServiceMessage> messages) {
		if(!isReady()) return;
		comm.nofity(messages);
	}
	
	public List<ServiceMessage> getEnqueuedMessages() {
		if(!isReady()) return null;
		return comm.popEnqueuedMessages();
	}
	
	public void addUploadedFile(UploadedFile uploadedFile) {
		if(!isReady()) return;
		synchronized(uploads) {
			uploads.put(uploadedFile.getUploadId(), uploadedFile);
		}
	}
	
	public UploadedFile getUploadedFile(String uploadId) {
		if(!isReady()) return null;
		synchronized(uploads) {
			return uploads.get(uploadId);
		}
	}
	
	/**
	 * Checks if there is an uploaded file entry with specified ID.
	 * @param uploadId Uploaded file ID
	 * @return True if present, false otherwise.
	 */
	public boolean hasUploadedFile(String uploadId) {
		if(!isReady()) return false;
		synchronized(uploads) {
			return uploads.containsKey(uploadId);
		}
	}
	
	/**
	 * Removes the uploaded file entry from the storage.
	 * @param uploadedFile The entry to remove
	 * @param deleteTempFile True to remove also corresponding fisical file from Temp
	 */
	public void removeUploadedFile(UploadedFile uploadedFile, boolean deleteTempFile) {
		removeUploadedFile(uploadedFile.getUploadId(), deleteTempFile);
	}
	
	/**
	 * Removes the uploaded file entry from the storage.
	 * @param uploadId Uploaded file ID
	 * @param deleteTempFile True to remove also corresponding physical file from Temp
	 */
	public void removeUploadedFile(String uploadId, boolean deleteTempFile) {
		if(!isReady()) return;
		synchronized(uploads) {
			UploadedFile upf = uploads.get(uploadId);
			if(upf != null) {
				if(deleteTempFile && !upf.isVirtual()) {
					String domainId = getProfileDomainId();
					try {
						wta.deleteTempFile(domainId, uploadId);
					} catch(WTException ex) { /* Do nothing... */ }
				}
				uploads.remove(uploadId);
			}
		}
	}
	
	/**
	 * Remove uploaded files by tag value.
	 * Files will be also deleted from Temp directory.
	 * @param tag 
	 */
	public void removeUploadedFileByTag(String tag) {
		if(!isReady()) return;
		synchronized(uploads) {
			Iterator<Map.Entry<String, UploadedFile>> it = uploads.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, UploadedFile> entry = it.next();
				if(StringUtils.equals(entry.getValue().getTag(), tag)) {
					if(!entry.getValue().isVirtual()) {
						String domainId = getProfileDomainId();
						try {
							wta.deleteTempFile(domainId, entry.getValue().getUploadId());
						} catch(WTException ex) { /* Do nothing... */ }
					}
					it.remove();
				}
			}
		}
	}
	
	public static class UploadedFile {
		private final boolean virtual;
		private final String serviceId;
		private final String uploadId;
		private final String tag;
		private final String filename;
		private final long size;
		private final String mediaType;
		private final DateTime uploadedOn;
		private HashMap<String,Object> properties=null;
		
		public UploadedFile(boolean virtual, String serviceId, String uploadId, String tag, String filename, long size, String mediaType) {
			this.virtual = virtual;
			this.serviceId = serviceId;
			this.uploadId = uploadId;
			this.tag = tag;
			this.filename = filename;
			this.size = size;
			this.mediaType = mediaType;
			this.uploadedOn = DateTimeUtils.now(true);
		}
		
		public boolean isVirtual() {
			return virtual;
		}
		
		public String getServiceId() {
			return serviceId;
		}

		public String getUploadId() {
			return uploadId;
		}
		
		public String getTag() {
			return tag;
		}

		public String getFilename() {
			return filename;
		}

		public Long getSize() {
			return size;
		}

		public String getMediaType() {
			return mediaType;
		}

		public DateTime getUploadedOn() {
			return uploadedOn;
		}
		
		public File getFile() throws WTException {
			return new File(WT.getTempFolder(), getUploadId());
		}
		
		public void setProperty(String key, Object value) {
			if (properties==null) properties=new HashMap();
			properties.put(key, value);
		}
		
		public Object getProperty(String key) {
			if (properties==null) return null;
			return properties.get(key);
		}
	}
}
