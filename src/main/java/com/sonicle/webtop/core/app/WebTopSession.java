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

import com.sonicle.webtop.core.app.sdk.BaseDocEditorDocumentHandler;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.mail.StoreUtils;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.app.servlet.UIPrivate;
import com.sonicle.webtop.core.bol.OAutosave;
import com.sonicle.webtop.core.bol.js.JsWTS;
import com.sonicle.webtop.core.bol.js.JsWTSPrivate;
import com.sonicle.webtop.core.bol.js.JsWTSPublic;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.model.ServiceSharePermission;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.app.servlet.Otp;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.msg.AutosaveSM;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sonicle.webtop.core.util.LoggerUtils;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.uadetector.ReadableDeviceCategory;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
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
	
	private final WebTopApp wta;
	private HttpSession session;
	private final String csrfToken;
	private Boolean jsDebugEnabled;
	
	private final Object lock0 = new Object();
	private ReadableUserAgent readableUserAgent = null;
	private final PropertyBag propsBag = new PropertyBag();
	private int initLevel = 0;
	private UserProfile profile = null;
	private PrivateEnvironment privateEnv = null;
	private CorePrivateEnvironment privateCoreEnv = null;
	private PublicEnvironment publicEnv = null;
	private final LinkedHashMap<String, BaseManager> managers = new LinkedHashMap<>();
	private Set<String> allowedServices = null;
	private final LinkedHashMap<String, BaseService> privateServices = new LinkedHashMap<>();
	private final LinkedHashMap<String, BasePublicService> publicServices = new LinkedHashMap<>();
	private final HashMap<String, UploadedFile> uploads = new HashMap<>();
	private final Object lock1 = new Object();
	private jakarta.mail.Session mailSession = null;
	
	public WebTopSession(HttpSession session) {
		this(WebTopApp.getInstance(), session);
	}
	
	WebTopSession(WebTopApp wta, HttpSession session) {
		this.wta = wta;
		this.session = session;
		this.csrfToken = IdentifierUtils.getCRSFToken();
		this.jsDebugEnabled = WebTopProps.getJsDebug(wta.getProperties());
	}
	
	synchronized void cleanup() throws Exception {
		initLevel = -1;
		
		emptyPrivateServices();
		emptyPublicServices();
		emptyServiceManagers();
		
		// Cleanup uploads
		String domainId = getProfileDomainId();
		if(domainId != null) {
			synchronized(uploads) {
				for(UploadedFile upf : uploads.values()) {
					if(!upf.isVirtual()) wta.getFileSystem().deleteTempFile(domainId, upf.getUploadId());
				}
				uploads.clear();
			}
		}
		
		DocEditorManager docEdMgr = wta.getDocEditorManager();
		if (docEdMgr != null) docEdMgr.cleanupOnSessionDestroy(session.getId());
	}
	
	// TODO: rimuovere metodi deprecati
	
	/**
	 * @deprecated use {@link #getClientRemoteIP()} instead.
	 * @return 
	 */
	@Deprecated
	public String getRemoteIP() {
		return getClientRemoteIP();
	}
	
	/**
	 * @deprecated use {@link #getClientPlainUserAgent()} instead.
	 * @return 
	 */
	@Deprecated
	public String getPlainUserAgent() {
		return getClientPlainUserAgent();
	}
	
	
	
	/**
	 * Returns the associated user session.
	 * @return Session object
	 */
	public HttpSession getSession() {
		return session;
	}
	
	/**
	 * Convenience method to get genenated CSRF token.
	 * @return Generated CSRF token
	 */
	public String getCSRFToken() {
		return csrfToken;
	}
	
	/**
	 * Convenience method to get WebTop genenated client identifier.
	 * @return WebTop client identifier
	 */
	public String getClientTrackingID() {
		return SessionContext.getWebTopClientID(session);
	}
	
	/**
	 * Convenience method to get client's IP address.
	 * @return The network address
	 */
	public String getClientRemoteIP() {
		return SessionContext.getClientRemoteIP(session);
	}
	
	/**
	 * Convenience method to get client's browser URL.
	 * @return The network address
	 */
	public String getClientUrl() {
		return SessionContext.getClientUrl(session);
	}
	
	/**
	 * Convenience method to get client's plain user-agent info.
	 * @return user-agent info
	 */
	public String getClientPlainUserAgent() {
		return SessionContext.getClientPlainUserAgent(session);
	}
	
	/**
	 * Convenience method to get the referer-uri.
	 * @return The referer-uri
	 */
	public String getRefererUri() {
		return SessionContext.getRefererUri(session);
	}
	
	/**
	 * Returns the session ID.
	 * @return Session unique identifier
	 */
	public String getId() {
		return session.getId().toString();
	}
	
	/**
	 * Returns current configuration for js debug.
	 * @return 
	 */
	public boolean isJsDebugEnabled() {
		return jsDebugEnabled;
	}
	
	/**
	 * Updates value for js debug mode.
	 * @param jsDebugEnabled 
	 */
	public void setJsDebugEnabled(boolean jsDebugEnabled) {
		this.jsDebugEnabled = jsDebugEnabled;
	}
	
	/**
	 * Return current locale.
	 * It can be the UserProfile's locale or the locale specified during
	 * the initial HTTP request to the server.
	 * @return The locale.
	 */
	public Locale getLocale() {
		if (profile != null) {
			return profile.getLocale();
		} else {
			return SessionContext.getClientLocale(session);
		}
	}
	
	/**
	 * Associates an object to this session, using the key specified.
	 * @param serviceId The service ID to which the object is bound.
	 * @param key The key to which the object is mapped.
	 * @param value The object to be mapped.
	 * @return The object just associated.
	 */
	public Object setProperty(String serviceId, String key, Object value) {
		synchronized(propsBag) {
			propsBag.set(serviceId+"@"+key, value);
			return value;
		}
	}
	
	/**
	 * Returns the object to which the specified key is mapped, or null if no object is mapped under the key.
	 * @param serviceId The service ID to which the object is mapped.
	 * @param key The key to which the object is mapped.
	 * @return If found, the object to which the specified key is mapped.
	 */
	public Object getProperty(String serviceId, String key) {
		synchronized(propsBag) {
			return propsBag.get(serviceId+"@"+key);
		}
	}
	
	/**
	 * Returns the object to which the specified key is mapped, or null if no object is mapped under the key.
	 * Mapping will be cleared when the object is returned.
	 * @param serviceId The service ID to which the object is mapped.
	 * @param key The key to which the object is mapped.
	 * @return If found, the object to which the specified key is mapped.
	 */
	public Object popProperty(String serviceId, String key) {
		synchronized(propsBag) {
			if(hasProperty(serviceId, key)) {
				Object value = propsBag.get(serviceId+"@"+key);
				clearProperty(serviceId, key);
				return value;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Clears the object mapped to the specified key.
	 * @param serviceId The service ID to which the object is mapped.
	 * @param key The key to which the object is mapped.
	 */
	public void clearProperty(String serviceId, String key) {
		synchronized(propsBag) {
			propsBag.clear(serviceId+"@"+key);
		}
	}
	
	/**
	 * Checks if this session contains a mapping for the specified key.
	 * @param serviceId The service ID to which the object is mapped.
	 * @param key The key to which the object is mapped.
	 * @return True if a mapping is found, false otherwise.
	 */
	public boolean hasProperty(String serviceId, String key) {
		synchronized(propsBag) {
			return propsBag.has(serviceId+"@"+key);
		}
	}
	
	/**
	 * Checks if this session contains a mapping for the specified key,
	 * otherwise throws an exception.
	 * @param serviceId The service ID to which the object is mapped.
	 * @param key The key to which the object is mapped.
	 * @throws WTException 
	 */
	public void hasPropertyOrThrow(String serviceId, String key) throws WTException {
		if (!hasProperty(serviceId, key)) throw new WTException("Missing session property [{0}, {1}]", serviceId, key);
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
	public UserProfileId getProfileId() {
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
	
	private void internalInitPrivate(HttpServletRequest request) throws WTException {
		// Synchronization on caller method!
		ServiceManager svcm = wta.getServiceManager();
		Principal principal = (Principal)SecurityUtils.getSubject().getPrincipal();
		
		Subject subject = RunContext.getSubject();
		UserProfileId profileId = RunContext.getRunProfileId(subject);
		session.setAttribute(SessionManager.ATTRIBUTE_GUESSING_USERNAME, profileId.toString());
		
		emptyServiceManagers();
		
		CoreManager core = svcm.instantiateCoreManager(false, profileId);
		cacheServiceManager(CoreManifest.ID, core);
		CoreAdminManager coreadmin = svcm.instantiateCoreAdminManager(false, profileId);
		cacheServiceManager(CoreAdminManifest.ID, coreadmin);
		
		// Defines useful instances (NB: keep code assignment order!!!)
		profile = new UserProfile(core, principal);
		
		boolean passwordChangeNeeded = wta.getWebTopManager().isUserPasswordChangeNeeded(profileId.getDomainId(), profile.getUserId(), principal.getPassword());
		if (passwordChangeNeeded && !principal.isImpersonated()) setProperty(CoreManifest.ID, UIPrivate.WTSPROP_PASSWORD_CHANGEUPONLOGIN, true);
		
		boolean otpEnabled = wta.getOTPManager().isEnabled(profile.getId());
		if (!otpEnabled || principal.isImpersonated()) {
			setProperty(CoreManifest.ID, Otp.WTSPROP_OTP_VERIFIED, true);
		} else {
			setProperty(CoreManifest.ID, Otp.WTSPROP_OTP_PENDING, true);
		}
		
		initLevel = 1;
	}
	
	public void internalCleanupPrivateEnvironment() {
		// Synchronization on caller method!
		
		allowedServices = null;
		
		
		
		privateCoreEnv = null;
		privateEnv = null;
	}
	
	private Set<String> listAllowedPrivateServices(ServiceManager svcm) {
		LinkedHashSet<String> ids = new LinkedHashSet<>();
		if (RunContext.isSysAdmin()) {
			ids.add(CoreManifest.ID);
			ids.add(CoreAdminManifest.ID);
			ids.add("com.sonicle.webtop.vfs");
			
		} else {
			for (String id : svcm.listRegisteredServices()) {
				if (RunContext.isPermitted(true, CoreManifest.ID, "SERVICE", "ACCESS", id)) ids.add(id);
			}
		}
		return ids;
	}
	
	private void internalInitPrivateEnvironment(HttpServletRequest request) throws WTException {
		// Synchronization on caller method!
		ServiceManager svcm = wta.getServiceManager();
		SessionManager sesm = wta.getSessionManager();
		CoreManager core = WT.getCoreManager(profile.getId());
		
		privateCoreEnv = new CorePrivateEnvironment(wta, this);
		privateEnv = new PrivateEnvironment(this);
		
		UserProfileId profileId = profile.getId();
		String sessionId = getId();
		String clientIp = SessionContext.getClientRemoteIP(session);
		AuditLogManager.logAuth(LogbackHelper.Level.DEBUG, clientIp, sessionId, profileId, "AUTHENTICATED");
		wta.getAuditLogManager().write(profileId, null, sessionId, CoreManifest.ID, "AUTH", "AUTHENTICATED", null, JsonResult.gson().toJson(new MapItem().add("ip", clientIp)));
		
		sesm.registerWebTopSession(this);
		allowedServices = listAllowedPrivateServices(svcm);
		
		// Create and cache managers
		BaseManager managerInst = null;
		for (String serviceId : allowedServices) {
			ServiceDescriptor descriptor = svcm.getDescriptor(serviceId);
			if (descriptor == null) throw new WTRuntimeException("Descriptor not found for service [{}]", serviceId);
			
			// Manager
			// Skip core service... its manager has already been instantiated above (see: internalInitPrivate)
			if (!serviceId.equals(CoreManifest.ID) && !serviceId.equals(CoreAdminManifest.ID)) {
				if (descriptor.hasManager() && !isServiceManagerCached(serviceId)) {
					managerInst = svcm.instantiateServiceManager(serviceId, false, profile.getId());
					if (managerInst != null) {
						cacheServiceManager(serviceId, managerInst);
					}
				}
			}
		}
		
		// Create private services
		BaseService privateInst = null;
		for (String serviceId : allowedServices) {
			ServiceDescriptor descriptor = svcm.getDescriptor(serviceId);
			
			// Service initialization
			svcm.prepareProfile(serviceId, profile.getId());
			
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
		initLevel = 2;
		
		// Retrieve and push any auto-save data
		if (!RunContext.isImpersonated()) {
			String cid = getClientTrackingID();
			boolean mine = core.hasMyAutosaveData(cid);
			List<OAutosave> odata = core.listOfflineOthersAutosaveData(cid);
			boolean others = (odata == null) ? false : odata.size() > 0;
			if (mine || others) this.notify(new AutosaveSM(mine,others));
		}
		
		// Call ready() method for each instantiated service
		synchronized (privateServices) {
			for (BaseService instance : privateServices.values()) {
				svcm.privateServiceCallReady(instance);
			}
		}
	}
	
	public synchronized void initPublicEnvironment(HttpServletRequest request, String publicServiceId) throws WTException {
		internalInitPublicEnvironment(request, publicServiceId);
	}
	
	public void internalCleanupPublicEnvironment() {
		// Synchronization on caller method!
		emptyPublicServices();
		publicEnv = null;
	}
	
	private void internalInitPublicEnvironment(HttpServletRequest request, String publicServiceId) throws WTException {
		// Synchronization on caller method!
		
		if(isPublicServiceCached(publicServiceId)) return;
		ServiceManager svcm = wta.getServiceManager();
		
		if(!isServiceManagerCached(CoreManifest.ID)) {
			CoreManager core = svcm.instantiateCoreManager(true, RunContext.getRunProfileId());
			cacheServiceManager(CoreManifest.ID, core);
		}
		if(publicEnv == null) publicEnv = new PublicEnvironment(this);
		
		int managersCount = 0, publicCount = 0;
		String[] serviceIds = new String[]{CoreManifest.ID, publicServiceId};
		
		BaseManager managerInst = null;
		for(String serviceId : serviceIds) {
			ServiceDescriptor descriptor = svcm.getDescriptor(serviceId);
			
			// Manager (skip core)
			if(!serviceId.equals(CoreManifest.ID)) {
				if(descriptor.hasManager() && !isServiceManagerCached(serviceId)) {
					managerInst = svcm.instantiateServiceManager(serviceId, true, RunContext.getRunProfileId());
					if(managerInst != null) {
						cacheServiceManager(serviceId, managerInst);
						managersCount++;
					}
				}
			}
		}
		
		BasePublicService publicInst = null;
		for(String serviceId : serviceIds) {
			ServiceDescriptor descriptor = svcm.getDescriptor(serviceId);
			
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
	
	public boolean isServiceAllowed(String serviceId) {
		return allowedServices.contains(serviceId);
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
	
	public jakarta.mail.Session getMailSession() {
		synchronized(lock1) {
			UserProfileId pid = getProfileId();
			if (pid != null && mailSession == null) {
				CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
				String smtphost=css.getSMTPHost();
				int smtpport=css.getSMTPPort();
				boolean starttls=css.isSMTPStartTLS();
				boolean auth=css.isSMTPAuthentication();
				Properties props = new Properties(wta.getProperties());
				//props.setProperty("mail.socket.debug", "true");
				//props.setProperty("mail.imap.parse.debug", "true");
				props.setProperty("mail.smtp.host", smtphost);
				props.setProperty("mail.smtp.port", ""+smtpport);
				if (starttls) {
					props.put("mail.smtp.starttls.enable","true");
					props.put("mail.smtp.ssl.trust","*");
					props.put("mail.smtp.ssl.checkserveridentity", "false");
				}
				props.setProperty("mail.imaps.ssl.trust", "*");
				StoreUtils.useExtendedFolderClasses(props);
				props.setProperty("mail.imap.enableimapevents", "true"); // Support idle events
				Authenticator authenticator=null;
				if (auth) {
					props.setProperty("mail.smtp.auth", "true");
					authenticator=new Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							Principal principal = (Principal)SecurityUtils.getSubject().getPrincipal();							
							String login=principal.getFullInternetName();
							String password=new String(principal.getPassword());
							//logger.info("getPasswordAuthentication: "+login+" / *****");
							return new PasswordAuthentication(login,password);
						}

					};
				}
				mailSession = jakarta.mail.Session.getInstance(props, authenticator);
			}
		}
		return mailSession;
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
	
	public List<String> getPrivateServices() {
		return getPrivateServices(false);
	}
	
	/**
	 * Gets instantiated services list.
	 * @param sortByOrder True to sort the list using chosen order
	 * @return A list of service ids.
	 */
	public List<String> getPrivateServices(boolean sortByOrder) {
		if (!isReady()) return null;
		synchronized(privateServices) {
			List<String> ids = Arrays.asList(privateServices.keySet().toArray(new String[privateServices.size()]));
			if (sortByOrder) {
				CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, getUserProfile().getDomainId());
				sortServiceIdsByOrder(css.getServicesOrder(), ids);
			}
			return ids;
		}
	}
	
	private void emptyPrivateServices() {
		ServiceManager svcm = wta.getServiceManager();
		synchronized (privateServices) {
			for (BaseService instance : privateServices.values()) {
				svcm.privateServiceCallCleanup(instance);
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
	private boolean isPublicServiceCached(String serviceId) {
		return publicServices.containsKey(serviceId);
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
	
	public void fillStartup(JsWTSPrivate js) {
		if(!isReady()) return;
		
		ServiceManager svcm = wta.getServiceManager();
		ServiceManifest coreManifest = svcm.getManifest(CoreManifest.ID);
		CoreUserSettings cus = new CoreUserSettings(CoreManifest.ID, profile.getId());
		String theme = cus.getTheme(), layout = cus.getLayout(), lookAndFeel = cus.getLookAndFeel();
		
		ReadableUserAgent readableUa = WebTopApp.getUserAgentInfo(getClientPlainUserAgent());
		ReadableDeviceCategory.Category deviceCategory = ReadableDeviceCategory.Category.UNKNOWN;
		if (readableUa != null) deviceCategory = readableUa.getDeviceCategory().getCategory();
		if (ReadableDeviceCategory.Category.SMARTPHONE.equals(deviceCategory) || ReadableDeviceCategory.Category.TABLET.equals(deviceCategory)) {
			if (theme.equals("crisp") || theme.equals("neptune")) {
				theme += "-touch";
			} else {
				theme = "crisp-touch";
			}
		}
		Locale locale = getLocale();
		
		// New HTMLEditor activation flag: temporary until full transition!!!
		boolean useNewHTMLEditor = cus.getUseNewHTMLEditor();
		fillAppReferences(js, locale, theme, false, useNewHTMLEditor);
		js.layoutClassName = StringUtils.capitalize(layout);
		
		List<String> privateSids = getPrivateServices(true);
				
		// Include Core references
		//js.appManifest.name = coreManifest.getJsPackageName();
		fillServiceManifest(js, coreManifest, locale, svcm.isInMaintenance(coreManifest.getId()));
		fillCoreServiceJsReferences(svcm.isInDevMode(CoreManifest.ID), "private", js, coreManifest, locale);
		fillServiceCssReferences(js, coreManifest, theme, lookAndFeel);
		
		// Include other services references
		if (RunContext.isWebTopAdmin()) {
			for (String sid : svcm.listRegisteredServices()) {
				if (sid.equals(CoreManifest.ID)) continue;
				ServiceDescriptor descriptor = svcm.getDescriptor(sid);
				fillServiceReferences(svcm, js, descriptor, locale, theme, lookAndFeel);
			}
		} else {
			for (String sid : privateSids) {
				if (sid.equals(CoreManifest.ID)) continue;
				ServiceDescriptor descriptor = svcm.getDescriptor(sid);
				fillServiceReferences(svcm, js, descriptor, locale, theme, lookAndFeel);
			}
		}
		
		fillRolesMap(js.roles);
		
		// Evaluate services
		for (String sid : privateSids) {
			ServiceDescriptor descriptor = svcm.getDescriptor(sid);
			//fillServiceReferences(svcm, js, descriptor, locale, theme, lookAndFeel);
			fillStartupForService(svcm, js, descriptor, locale);
		}
	}
	
	private void fillRolesMap(HashSet<String> roles) {
		Subject subject = RunContext.getSubject();
		pushIfSubjectHasRole(roles, subject, WebTopManager.SYSADMIN_ROLESID);
		pushIfSubjectHasRole(roles, subject, WebTopManager.WTADMIN_ROLESID);
		pushIfSubjectHasRole(roles, subject, WebTopManager.IMPERSONATED_USER_ROLESID);
	}
	
	private void fillServiceReferences(ServiceManager svcm, JsWTS js, ServiceDescriptor descriptor, Locale locale, String theme, String lookAndFeel) {
		ServiceManifest manifest = descriptor.getManifest();
		if (manifest.getId().equals(CoreManifest.ID)) throw new WTRuntimeException("Core service's references should not be added in this way");
		
		fillServiceManifest(js, manifest, locale, svcm.isInMaintenance(manifest.getId()));
		// Includes service references
		fillServiceJsReferences(svcm.isInDevMode(manifest.getId()), js, manifest, locale);
		// Includes service stylesheet references
		fillServiceCssReferences(js, manifest, theme, lookAndFeel);
	}
	
	private void fillStartupForService(ServiceManager svcm, JsWTSPrivate js, ServiceDescriptor descriptor, Locale locale) {
		ServiceManifest manifest = descriptor.getManifest();
		Subject subject = RunContext.getSubject();
		
		// Generates service auth permissions
		JsWTSPrivate.Permissions perms = new JsWTSPrivate.Permissions();
		for (ServicePermission perm : manifest.getDeclaredPermissions()) {
			if (perm instanceof ServiceSharePermission) continue;
			
			JsWTSPrivate.Actions acts = new JsWTSPrivate.Actions();
			for (String act : perm.getActions()) {
				if (RunContext.isPermitted(true, subject, manifest.getId(), perm.getGroupName(), act)) {
					acts.put(act, true);
				}
			}
			if (!acts.isEmpty()) perms.put(perm.getGroupName(), acts);
		}
		
		JsWTSPrivate.PrivateService jssvc2 = (JsWTSPrivate.PrivateService)js.createServiceInstance();
		jssvc2.id = manifest.getId();
		jssvc2.serviceCN = manifest.getPrivateServiceJsClassName(true);
		jssvc2.serviceVarsCN = manifest.getPrivateServiceVarsModelJsClassName(true);
		if (descriptor.hasUserOptionsService()) {
			jssvc2.userOptions = new JsWTSPrivate.ServiceUserOptions(
				manifest.getUserOptionsViewJsClassName(true),
				manifest.getUserOptionsModelJsClassName(true)
			);
		}
		for(ServiceManifest.Portlet portlet : manifest.getPortlets()) {
			jssvc2.portletCNs.add(portlet.jsClassName);
		}
		js.services.add(jssvc2);
		js.servicesVars.add(getServiceVars(manifest.getId()));
		js.servicesPerms.add(perms);
	}
	
	private JsWTSPrivate.Vars getServiceVars(String serviceId) {
		BaseService svc = getPrivateServiceById(serviceId);
		BaseService.ServiceVars vars = null;
		
		// Retrieves initial vars from instantiated service
		try {
			LoggerUtils.setContextDC(serviceId);
			vars = svc.returnServiceVars();
		} catch(Exception ex) {
			logger.error("returnServiceVars method returns errors", ex);
		} finally {
			LoggerUtils.clearContextServiceDC();
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
		ServiceManager svcm = wta.getServiceManager();
		ServiceManifest coreManifest = svcm.getManifest(CoreManifest.ID);
		String theme = "crisp";
		
		ReadableUserAgent readableUa = WebTopApp.getUserAgentInfo(getClientPlainUserAgent());
		ReadableDeviceCategory.Category deviceCategory = ReadableDeviceCategory.Category.UNKNOWN;
		if (readableUa != null) deviceCategory = readableUa.getDeviceCategory().getCategory();
		if (ReadableDeviceCategory.Category.SMARTPHONE.equals(deviceCategory) || ReadableDeviceCategory.Category.TABLET.equals(deviceCategory)) {
			theme += "-touch";
		}

		fillAppReferences(js, locale, theme, false, false);
		
		// Include Core references
		//js.appManifest.name = coreManifest.getJsPackageName();
		fillServiceManifest(js, coreManifest, locale, svcm.isInMaintenance(coreManifest.getId()));
		fillCoreServiceJsReferences(true/*svcm.isInDevMode(CoreManifest.ID)*/, "public", js, coreManifest, locale);
		fillServiceCssReferences(js, coreManifest, theme, "default");
		
		fillStartupForPublicService(js, CoreManifest.ID, locale);
		fillStartupForPublicService(js, publicServiceId, locale);
	}
	
	private void fillStartupForPublicService(JsWTSPublic js, String serviceId, Locale locale) {
		ServiceManager svcm = wta.getServiceManager();
		ServiceDescriptor sdesc = svcm.getDescriptor(serviceId);
		ServiceManifest manifest = sdesc.getManifest();
		
		// Fill application manifest with service references (NOTE: core service is skipped here!)
		if (!serviceId.equals(CoreManifest.ID)) {
			fillServiceManifest(js, manifest, locale, svcm.isInMaintenance(serviceId));
			// Includes service references
			fillServiceJsReferences(true/*svcm.isInDevMode(serviceId)*/, js, manifest, locale);
			// Includes service stylesheet references
			fillServiceCssReferences(js, manifest, "crisp", "default");
		}
		
		JsWTSPublic.PublicService jssvc2 = (JsWTSPublic.PublicService)js.createServiceInstance();
		jssvc2.id = manifest.getId();
		jssvc2.serviceCN = manifest.getPublicServiceJsClassName(true);
		jssvc2.serviceVarsCN = manifest.getPublicServiceVarsModelJsClassName(true);
		js.services.add(jssvc2);
		js.servicesVars.add(getPublicServiceVars(serviceId));
	}
	
	private JsWTSPublic.Vars getPublicServiceVars(String serviceId) {
		BasePublicService svc = getPublicServiceById(serviceId);
		BasePublicService.ServiceVars vars = null;
		
		// Retrieves initial vars from instantiated service
		if(svc != null) {
			try {
				LoggerUtils.setContextDC(serviceId);
				vars = svc.returnServiceVars();
			} catch(Exception ex) {
				logger.error("returnServiceVars method returns errors", ex);
			} finally {
				LoggerUtils.clearContextServiceDC();
			}
		}
		
		JsWTSPublic.Vars is = new JsWTSPublic.Vars();
		if(vars != null) is.putAll(vars);
		return is;
	}
	
	private void fillAppReferences(JsWTS js, Locale locale, String theme, boolean rtl, boolean useNewHTMLEditor) {
		js.appManifest.name = "Sonicle.webtop.core.app.App";
		js.appManifest.id = "5ae25afe-182c-466c-a6ad-0a3af0ee74b5";
		js.appManifest.theme = theme;
		js.themeName = theme;
		js.platformName = wta.getPlatformName();
		js.fileTypes = wta.getFileTypes().toString();
		fillExtJsReferences(js, locale, theme, rtl, useNewHTMLEditor);
	}
	
	private void fillExtJsReferences(JsWTS js, Locale locale, String theme, boolean rtl, boolean useNewHTMLEditor) {
		js.appManifest.framework = "ext";
		js.appManifest.toolkit = "classic";
		
		//TODO: rendere dinamico il caricamento delle librerie, permettendo ai servizi di aggiungere le loro
		
		// Do not replace 0.0.0 with the real version, it limits server traffic.
		final String VENDOR_PATH = "resources/com.sonicle.webtop.core/0.0.0/resources/vendor";
		final String LIBS_PATH = "resources/com.sonicle.webtop.core/0.0.0/resources/libs";
		
		// Include external libraries references
		js.appManifest.addJs(VENDOR_PATH + "/jquery/3.3.1/" + "jquery.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/spark-md5/3.0.0/" + "spark-md5.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/js-emoji/3.4.1/" + "emoji.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/ion.sound/3.0.7/" + "ion.sound.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/linkify/2.1.6/" + "linkify.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/linkify/2.1.6/" + "linkify-string.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/screenfull/3.3.2/" + "screenfull.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/atmosphere/2.3.9/" + "atmosphere.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/jsxc/3.4.0/" + "jsxc.dep.js");
		if (useNewHTMLEditor) {
			js.appManifest.addJs(VENDOR_PATH + "/tinymce/5.7.0/" + "tinymce.min.js");
		} else {
			js.appManifest.addJs(VENDOR_PATH + "/tinymce/4.3.12/" + "tinymce.min.js");
		}
		js.appManifest.addJs(VENDOR_PATH + "/plupload/2.3.6/" + "plupload.full.min.js"); // Remember to update paths in Factory.js
		js.appManifest.addJs(VENDOR_PATH + "/rrule/2.1.0/" + "rrule.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/markjs/8.11.1/" + "mark.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/search-string/3.1.0/" + "search-string.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/jsdifflib/1.1.0/" + "jsdifflib.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/guess-language/" + "_languageData.js");
		js.appManifest.addJs(VENDOR_PATH + "/guess-language/" + "guessLanguage.js");
		js.appManifest.addJs(VENDOR_PATH + "/showdown/1.9.1/" + "showdown.min.js");
		js.appManifest.addCss(VENDOR_PATH + "/github-markdown/4.0.0/" + "github-markdown.min.css");
		js.appManifest.addJs(VENDOR_PATH + "/codemirror/5.65.2/" + "codemirror.min.js");
		js.appManifest.addJs(VENDOR_PATH + "/codemirror/5.65.2/mode/sql/" + "sql.min.js");
		js.appManifest.addCss(VENDOR_PATH + "/codemirror/5.65.2/" + "codemirror.min.css");
		js.appManifest.addJs(VENDOR_PATH + "/jexl/2.3.0/" + "jexl.min.js");
		
		// Uncomment these lines to load debug versions of the libraries ----->
		//js.appManifest.addJs(VENDOR_PATH + "/jsxc/3.4.0/" + "jsxc.dep.js");
		//js.appManifest.addJs(VENDOR_PATH + "/tinymce/5.6.2/" + "tinymce.js");
		//js.appManifest.addJs(VENDOR_PATH + "/tinymce/4.3.12/" + "tinymce.js");
		//js.appManifest.addJs(VENDOR_PATH + "/plupload/2.3.6/" + "moxie.js");
		//js.appManifest.addJs(VENDOR_PATH + "/plupload/2.3.6/" + "plupload.dev.js");
		// <-------------------------------------------------------------------
		//js.appManifest.addJs(VENDOR_PATH + "/ckeditor/" + "ckeditor.js");
		//js.appManifest.addJs(VENDOR_PATH + "/cke5/20.0.0/" + "ckeditor.js");
		
		// Include ExtJs references
		final String EXTJS_PATH = "resources/client/extjs/";
		String extRtl = rtl ? "-rtl" : "";
		String extDebug = WebTopProps.getExtJsDebug(wta.getProperties()) ? "-debug" : "";
		String extTheme = theme;
		String extThemeName = StringUtils.removeEnd(theme, "-touch");
		String extBaseTheme = UIBoot.getBaseExtJSTheme(extThemeName);
		String extLang = "-" + locale.getLanguage();
		
		// Main: library + locales
		js.appManifest.addJs(EXTJS_PATH + "ext-all" + extRtl + extDebug + ".js");
		js.appManifest.addJs(EXTJS_PATH + js.appManifest.toolkit + "/locale/" + "locale" + extLang + extDebug + ".js");
		// Themes: library (overrides) + styles
		js.appManifest.addJs(EXTJS_PATH + js.appManifest.toolkit + "/" + "theme-" + extTheme + "/" + "theme-" + extTheme + extDebug + ".js");
		js.appManifest.addCss(EXTJS_PATH + js.appManifest.toolkit + "/" + "theme-" + extTheme + "/resources/" + "theme-" + extTheme + "-all" + extRtl + extDebug + ".css");
		// Charts: library + styles
		js.appManifest.addJs(EXTJS_PATH + "packages/charts/" + js.appManifest.toolkit + "/" + "charts" + extDebug + ".js");
		js.appManifest.addCss(EXTJS_PATH + "packages/charts/" + js.appManifest.toolkit + "/" + extBaseTheme + "/resources/" + "charts-all" + extRtl + extDebug + ".css");
		// UX: library + styles
		js.appManifest.addJs(EXTJS_PATH + "packages/ux/" + js.appManifest.toolkit + "/" + "ux" + extDebug + ".js");
		js.appManifest.addCss(EXTJS_PATH + "packages/ux/" + js.appManifest.toolkit + "/" + extBaseTheme + "/resources/" + "ux-all" + extRtl + extDebug + ".css");
		// Fonts: styles
		js.appManifest.addCss(EXTJS_PATH + "packages/font-awesome/resources/" + "font-awesome-all" + extRtl + extDebug + ".css");
		js.appManifest.addCss(EXTJS_PATH + "packages/font-ext/resources/" + "font-ext-all" + extRtl + extDebug + ".css");
		js.appManifest.addCss(EXTJS_PATH + "packages/font-pictos/resources/" + "font-pictos-all" + extRtl + extDebug + ".css");
		// Sonicle ExtJs Extensions
		if (WebTopProps.getSoExtJsExtensionsDevMode(wta.getProperties())) {
			js.appManifest.addPath("Sonicle", EXTJS_PATH + "packages/sonicle-extensions/src");
		} else {
			js.appManifest.addJs(EXTJS_PATH + "packages/sonicle-extensions/" + "sonicle-extensions" + extDebug + ".js");
		}
		js.appManifest.addCss(EXTJS_PATH + "packages/sonicle-extensions/" + extThemeName + "/resources/" + "sonicle-extensions-all" + extRtl + extDebug + ".css");
		
		// Override default Ext error handling in order to avoid application hang.
		// NB: This is only necessary when using ExtJs debug file!
		if (WebTopProps.getExtJsDebug(wta.getProperties()))
			js.appManifest.addJs(LIBS_PATH + "/" + "ext-override-errors.js");
	}
	
	private void fillCoreServiceJsReferences(boolean devMode, String target, JsWTS js, ServiceManifest manifest, Locale locale) {
		String targetSuffix = "-" + target;
		if (devMode) {
			String jsFileName = (js instanceof JsWTSPublic) ? manifest.getPrivateServiceJsFileName() : manifest.getPrivateServiceJsFileName();
			js.appManifest.addJs(manifest.getPackageSrcUrl() + "/app/WT.js");
			js.appManifest.addJs(manifest.getPackageSrcUrl() + "/app/Factory.js");
			js.appManifest.addJs(manifest.getPackageSrcUrl() + "/app/Util.js");
			js.appManifest.addJs(manifest.getPackageBaseUrl() + "/src/app" + targetSuffix + ".js"); // App file (private or public)
			js.appManifest.addJs(manifest.getPackageSrcUrl() + "/" + jsFileName); // Service js class
			js.appManifest.addJs(manifest.getPackageBaseUrl() + "/" + manifest.getLocaleJsFileName(locale)); // Service's locale js class
			js.appManifest.paths.put(manifest.getJsPackageName(), manifest.getPackageSrcUrl()); // Namespace -> url path mapping
			js.appManifest.paths.put("WTA", manifest.getPackageSrcUrl()); // Short namespace (WTA) -> url path mapping
			
		} else {
			js.appManifest.addJs(manifest.getPackageBaseUrl() + "/" + manifest.getId() + targetSuffix + ".js"); // Service concatenated js
			js.appManifest.addJs(manifest.getPackageBaseUrl() + "/" + manifest.getLocaleJsFileName(locale)); // Service's locale js class
		}
		js.locales.add(new JsWTS.XLocale(manifest.getId(), manifest.getLocaleJsClassName(locale, true)));
	}
	
	private void fillServiceManifest(JsWTS js, ServiceManifest manifest, Locale locale, boolean maintenance) {
		JsWTS.Manifest jsman = js.createManifestInstance();
		
		jsman.xid = manifest.getXId();
		jsman.ns = manifest.getJsPackageName();
		jsman.path = manifest.getJsBaseUrl(false);
		jsman.name = StringEscapeUtils.escapeJson(wta.lookupResource(manifest.getId(), locale, CoreLocaleKey.SERVICE_NAME));
		jsman.description = StringEscapeUtils.escapeJson(wta.lookupResource(manifest.getId(), locale, CoreLocaleKey.SERVICE_DESCRIPTION));
		jsman.company = StringEscapeUtils.escapeJson(manifest.getCompany());
		jsman.localeCN = manifest.getLocaleJsClassName(locale, true);
		jsman.maintenance = maintenance;
		
		if (jsman instanceof JsWTSPrivate.PrivateManifest) {
			((JsWTSPrivate.PrivateManifest)jsman).version = manifest.getVersion().toString();
			((JsWTSPrivate.PrivateManifest)jsman).build = manifest.getBuildDate();
		}
		
		js.manifests.put(manifest.getId(), jsman);
		
		//JsWTS.Manifest jsman = new JsWTS.Manifest();
		//jsman.xid = manifest.getXId();
		//jsman.ns = manifest.getJsPackageName();
		//jsman.path = manifest.getJsBaseUrl(false);
		//jsman.name = StringEscapeUtils.escapeJson(wta.lookupResource(manifest.getId(), locale, CoreLocaleKey.SERVICE_NAME));
		//jsman.description = StringEscapeUtils.escapeJson(wta.lookupResource(manifest.getId(), locale, CoreLocaleKey.SERVICE_DESCRIPTION));
		//jsman.version = manifest.getVersion().toString();
		//jsman.build = manifest.getBuildDate();
		//jsman.company = StringEscapeUtils.escapeJson(manifest.getCompany());
		//jsman.localeClassName = manifest.getLocaleJsClassName(locale, true);
		//js.manifests.put(manifest.getId(), jsman);
		
		//String localizedName = wta.lookupResource(manifest.getId(), locale, CoreLocaleKey.SERVICE_NAME);
		//String localizedDescription = wta.lookupResource(manifest.getId(), locale, CoreLocaleKey.SERVICE_DESCRIPTION);
		//js.manifests.put(manifest.getId(), new JsWTS.Manifest(manifest, localizedName, localizedDescription, manifest.getLocaleJsClassName(locale, true)));
	}
	
	private void fillServiceJsReferences(boolean devMode, JsWTS js, ServiceManifest manifest, Locale locale) {
		if (devMode) {
			String jsFileName = (js instanceof JsWTSPublic) ? manifest.getPrivateServiceJsFileName() : manifest.getPrivateServiceJsFileName();
			js.appManifest.paths.put(manifest.getJsPackageName(), manifest.getPackageSrcUrl()); // Namespace -> url path mapping
			js.appManifest.addJs(manifest.getPackageSrcUrl() + "/" + jsFileName); // Service js class
			js.appManifest.addJs(manifest.getPackageBaseUrl() + "/" + manifest.getLocaleJsFileName(locale)); // Service's locale js class
		} else {
			js.appManifest.addJs(manifest.getPackageBaseUrl() + "/" + manifest.getBundleJsFileName()); // Service concatenated js
			js.appManifest.addJs(manifest.getPackageBaseUrl() + "/" + manifest.getLocaleJsFileName(locale)); // Service's locale js class
		}
		js.locales.add(new JsWTS.XLocale(manifest.getId(), manifest.getLocaleJsClassName(locale, true)));
	}
	
	private void fillServiceCssReferences(JsWTS js, ServiceManifest manifest, String theme, String lookAndFeel) {
		js.appManifest.addCss(manifest.getPackageLookAndFeelUrl(lookAndFeel) + "/" + "service.css");
		js.appManifest.addCss(manifest.getPackageLookAndFeelUrl(lookAndFeel) + "/" + "service-" + theme + ".css");
	}
	
	private List<String> sortServiceIdsByOrder(final CoreServiceSettings.ServicesOrder so, List<String> ids) {
		Collections.sort(ids, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int i1 = serviceIdToOrderIndex(so, o1);
				int i2 = serviceIdToOrderIndex(so, o2);
				if (i1 < i2) {
					return -1;
				} else if (i1 > i2) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		return ids;
	}
	
	private int serviceIdToOrderIndex(CoreServiceSettings.ServicesOrder so, String serviceId) {
		if (StringUtils.equals(serviceId, CoreManifest.ID)) {
			return -2;
		} else if (StringUtils.equals(serviceId, CoreAdminManifest.ID)) {
			return -1;
		} else {
			int i = so.indexOf(serviceId);
			return (i != -1) ? i : 99;
		}
	}
	
	private void pushIfSubjectHasRole(HashSet<String> roles, Subject subject, String hasRole) {
		if (RunContext.hasRole(subject, hasRole)) roles.add(hasRole);
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
		svcm.resetWhatsnew(serviceId, profile.getId());
	}
	
	public void notify(ServiceMessage message) {
		if (!isReady()) return;
		wta.getSessionManager().push(getId(), message, false);
	}
	
	public void notify(List<ServiceMessage> messages) {
		if (!isReady()) return;
		wta.getSessionManager().push(getId(), messages, false);
	}
	
	public void addUploadedFile(UploadedFile uploadedFile) {
		if (!isReady()) return;
		synchronized(uploads) {
			uploads.put(uploadedFile.getUploadId(), uploadedFile);
		}
	}
	
	public UploadedFile getUploadedFile(String uploadId) {
		if (!isReady()) return null;
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
						wta.getFileSystem().deleteTempFile(domainId, uploadId);
					} catch(IOException ex) { /* Do nothing... */ }
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
							wta.getFileSystem().deleteTempFile(domainId, entry.getValue().getUploadId());
						} catch(IOException ex) { /* Do nothing... */ }
					}
					it.remove();
				}
			}
		}
	}
	
	public DocEditorManager.DocumentConfig prepareDocumentEditing(BaseDocEditorDocumentHandler docHandler, String filename, long lastModifiedTime) throws WTException, FileSystemException {
		return wta.getDocEditorManager().registerDocumentHandler(getId(), docHandler, filename, lastModifiedTime);
	}
	
	public void finalizeDocumentEditing(String editingId) {
		wta.getDocEditorManager().unregisterDocumentHandler(editingId);
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
