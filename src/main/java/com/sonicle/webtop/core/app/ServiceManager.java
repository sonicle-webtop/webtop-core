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
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.sdk.BaseRestApi;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobService.TaskDefinition;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.Environment;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ServiceManager {
	private static final Logger logger = WT.getLogger(ServiceManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @param scheduler Scheduler instance.
	 * @return The instance.
	 */
	public static synchronized ServiceManager initialize(WebTopApp wta, Scheduler scheduler) {
		if(initialized) throw new RuntimeException("Initialization already done");
		ServiceManager svcm = new ServiceManager(wta, scheduler);
		initialized = true;
		logger.info("ServiceManager initialized");
		return svcm;
	}
	
	public static final String SERVICES_DESCRIPTOR_RESOURCE = "META-INF/webtop-services.xml";
	private WebTopApp wta = null;
	private Scheduler scheduler = null;
	private final Object lock = new Object();
	private final LinkedHashMap<String, ServiceDescriptor> descriptors = new LinkedHashMap<>();
	private final HashMap<String, String> xidToServiceId = new HashMap<>();
	private final HashMap<String, String> serviceIdToJsPath = new HashMap<>();
	private final LinkedHashMap<String, BaseController> controllers = new LinkedHashMap<>();
	private final HashMap<String, String> publicNameToServiceId = new HashMap<>();
	private final LinkedHashMap<String, BasePublicService> publicServices = new LinkedHashMap<>();
	private final LinkedHashMap<String, BaseJobService> jobServices = new LinkedHashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private ServiceManager(WebTopApp wta, Scheduler scheduler) {
		this.wta = wta;
		this.scheduler = scheduler;
		init();
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		
		// Cleanup public/job services
		BasePublicService publicInst = null;
		BaseJobService jobInst = null;
		for(String serviceId : listPrivateServices()) {
			// Cleanup public service
			publicInst = publicServices.remove(serviceId);
			if(publicInst != null) cleanupPublicService(publicInst);
			// Cleanup job service
			//TODO: effettuare lo shutdown dei task
			jobInst = jobServices.remove(serviceId);
			if(jobInst != null) cleanupJobService(jobInst);
		}
		
		jobServices.clear();
		publicServices.clear();
		descriptors.clear();
		xidToServiceId.clear();
		serviceIdToJsPath.clear();
		publicNameToServiceId.clear();
		scheduler = null;
		wta = null;
		logger.info("ServiceManager destroyed");
	}
	
	private void init() {
		
		// Progamatically register the core's manifest
		registerService(new CoreManifest());
		
		// Loads services' manifest files from classpath
		logger.debug("Starting services discovery...");
		ArrayList<ServiceManifest> manifests = null;
		try {
			manifests = discoverServices();
			Collections.sort(manifests, new Comparator<ServiceManifest>() {
				@Override
				public int compare(ServiceManifest o1, ServiceManifest o2) {
					//return o1.getId().compareTo(o2.getId());
					return o2.getId().compareTo(o1.getId());
				}
			});
		} catch (IOException ex) {
			throw new RuntimeException("Error during services discovery", ex);
		}
		
		// Register discovered services
		for(ServiceManifest manifest : manifests) {
			registerService(manifest);
			createController(manifest.getId());
		}
		
		// Initialize public/job services
		int okPublics = 0, failPublics = 0, okJobs = 0, failJobs = 0;
		for(String serviceId : listPrivateServices()) {
			if(getDescriptor(serviceId).hasPublicService()) {
				if(!isInMaintenance(serviceId)) {
					if(createPublicService(serviceId)) {
						okPublics++;
					} else {
						failPublics++;
						//TODO: invalidare startup servizio, public non inizializzato?
					}
				}
			}
			if(getDescriptor(serviceId).hasJobService()) {
				if(!isInMaintenance(serviceId)) {
					if(createJobService(serviceId)) {
						okJobs++;
					} else {
						failJobs++;
						//TODO: invalidare startup servizio, job non inizializzato?
					}
				}
			}
		}
		logger.debug("Instantiated {} of {} public services", okPublics, (okPublics+failPublics));
		logger.debug("Instantiated {} of {} job services", okJobs, (okJobs+failJobs));
		//postponeDeamonsInitialization(); // Postpone initialization because init methods can require WebTopApp, not set yet!
	}
	
	public void onWebTopAppInit() {
		
		// Inits public services
		synchronized(publicServices) {
			for(Entry<String, BasePublicService> entry : publicServices.entrySet()) {
				initializePublicService(entry.getValue());
			}
		}
		
		// Inits job services
		synchronized(jobServices) {
			for(Entry<String, BaseJobService> entry : jobServices.entrySet()) {
				initializeJobService(entry.getValue());
			}
		}
	}
	
	public String getServiceJsPath(String serviceId) {
		return serviceIdToJsPath.get(serviceId);
	}
	
	public String getServiceIdByPublicName(String publicName) {
		return publicNameToServiceId.get(publicName);
	}
	
	/**
	 * Gets descriptor for a specified service.
	 * @param serviceId The service ID.
	 * @return Service descriptor object.
	 */
	ServiceDescriptor getDescriptor(String serviceId) {
		synchronized(lock) {
			if(!descriptors.containsKey(serviceId)) return null;
			return descriptors.get(serviceId);
		}
	}
	
	public boolean isCoreService(String serviceId) {
		return serviceId.equals(CoreManifest.ID);
	}
	
	public boolean hasFullRights(String serviceId) {
		if(serviceId.equals(CoreManifest.ID)) return true;
		return false;
	}
	
	public boolean isInMaintenance(String serviceId) {
		SettingsManager setm = wta.getSettingsManager();
		return LangUtils.value(setm.getServiceSetting(serviceId, CoreServiceSettings.MAINTENANCE), false);
	}
	
	public void setMaintenance(String serviceId, boolean maintenance) {
		SettingsManager setm = wta.getSettingsManager();
		setm.setServiceSetting(serviceId,CoreServiceSettings.MAINTENANCE, maintenance);
	}
	
	/**
	 * Gets manifest for a specified service.
	 * @param serviceId The service ID.
	 * @return Service manifest object.
	 */
	public ServiceManifest getManifest(String serviceId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(descr == null) return null;
		return descr.getManifest();
	}
	
	/**
	 * Lists IDs of registered services.
	 * @return List of services' IDs.
	 */
	public List<String> listRegisteredServices() {
		synchronized(lock) {
			return Arrays.asList(descriptors.keySet().toArray(new String[descriptors.size()]));
		}
	}
	
	public BaseController getController(String serviceId) {
		synchronized(controllers) {
			if(!controllers.containsKey(serviceId)) throw new WTRuntimeException("Unable to get controller for service [{0}]", serviceId);
			return controllers.get(serviceId);
		}
	}
	
	/**
	 * Lists IDs of services which Controller implements specified class.
	 * @param clazz The interface class to implement
	 * @return List of services' IDs
	 */
	public List<String> listServicesWhichControllerImplements(Class clazz) {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : descriptors.values()) {
				if(descr.doesControllerImplements(clazz)) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Lists IDs of services that have api implementation.
	 * @return List of services' IDs.
	 */
	public List<String> listServicesWithRestApi() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : descriptors.values()) {
				if(descr.hasRestApi()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Lists IDs of services that have private implementation.
	 * @return List of services' IDs.
	 */
	public List<String> listPrivateServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : descriptors.values()) {
				if(descr.hasPrivateService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Lists IDs of services that have public implementation.
	 * @return List of services' IDs.
	 */
	public List<String> listPublicServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : descriptors.values()) {
				if(descr.hasPublicService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Lists IDs of services that have job implementation.
	 * @return List of services' IDs.
	 */
	public List<String> listJobServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : descriptors.values()) {
				if(descr.hasJobService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Lists IDs of services that have userOption implementation.
	 * @return List of services' IDs.
	 */
	public List<String> listUserOptionServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : descriptors.values()) {
				if(descr.hasUserOptionsService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	public BasePublicService getPublicService(String serviceId) {
		synchronized(publicServices) {
			if(!publicServices.containsKey(serviceId)) throw new WTRuntimeException("No public service with ID: '{0}'", serviceId);
			return publicServices.get(serviceId);
		}
	}
	
	/**
	 * Checks if a specific service needs to show whatsnew for passed user.
	 * @param serviceId The service ID.
	 * @param profile The user profile.
	 * @return True if so, false otherwise.
	 */
	public boolean needWhatsnew(String serviceId, UserProfile profile) {
		SettingsManager setm = wta.getSettingsManager();
		ServiceVersion manifestVer = null, userVer = null;
		
		// Gets current service's version info and last version for this user
		ServiceDescriptor desc = getDescriptor(serviceId);
		manifestVer = desc.getManifest().getVersion();
		userVer = new ServiceVersion(CoreUserSettings.getWhatsnewVersion(setm, profile, serviceId));
		
		boolean notseen = (manifestVer.compareTo(userVer) > 0);
		boolean show = false;
		if(notseen) {
			String html = desc.getWhatsnew(profile.getLocale(), userVer);
			if(StringUtils.isEmpty(html)) {
				// If content is empty, updates whatsnew version for the user;
				// it basically realign versions in user-settings.
				logger.trace("Whatsnew empty [{}]", serviceId);
				resetWhatsnew(serviceId, profile);
			} else {
				show = true;
			}
		}
		logger.debug("Need to show whatsnew? {} [{}]", show, serviceId);
		return show;
	}
	
	/**
	 * Loads whatsnew file for specified service.
	 * If full parameter is true, all version paragraphs will be loaded;
	 * otherwise current version only.
	 * @param serviceId Service ID
	 * @param profile The user profile.
	 * @param full True to extract all version paragraphs.
	 * @return HTML translated representation of loaded file.
	 */
	public String getWhatsnew(String serviceId, UserProfile profile, boolean full) {
		ServiceVersion fromVersion = null;
		ServiceDescriptor desc = getDescriptor(serviceId);
		if(!full) {
			SettingsManager setm = wta.getSettingsManager();
			fromVersion = new ServiceVersion(CoreUserSettings.getWhatsnewVersion(setm, profile, serviceId));
		}
		return desc.getWhatsnew(profile.getLocale(), fromVersion);
	}
	
	/**
	 * Resets whatsnew updating service's version in user-setting
	 * to the current one.
	 * @param serviceId The service ID.
	 * @param profile The user profile.
	 */
	public synchronized void resetWhatsnew(String serviceId, UserProfile profile) {
		SettingsManager setm = wta.getSettingsManager();
		ServiceVersion manifestVer = null;
		
		// Gets current service's version info
		manifestVer = getManifest(serviceId).getVersion();
		CoreUserSettings.setWhatsnewVersion(setm, profile, serviceId, manifestVer.toString());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void scheduleAllJobServicesTasks() {
		if(!wta.isLastVersion()) return; // Make sure we are in latest webapp
		synchronized(jobServices) {
			for(Entry<String, BaseJobService> entry : jobServices.entrySet()) {
				scheduleJobServiceTasks(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void unscheduleAllJobServicesTasks() {
		synchronized(jobServices) {
			for(Entry<String, BaseJobService> entry : jobServices.entrySet()) {
				unscheduleJobServiceTasks(entry.getKey());
			}
		}
	}
	
	public boolean canExecuteTaskWork(JobKey taskKey) {
		if(wta.isLastVersion()) {
			return true;
		} else {
			unscheduleAllJobServicesTasks();
			return false;
		}
	}
	
	private boolean createController(String serviceId) {
		synchronized(controllers) {
			if(controllers.containsKey(serviceId)) throw new RuntimeException("Cannot add controller twice");
			BaseController inst = instantiateController(serviceId);
			if(inst != null) {
				controllers.put(serviceId, inst);
				/*
				for(Class<?> clazz : ((AbstractController)inst).getRegisteredClasses()) {
					try {
						wta.getComponentsManager().register(serviceId, clazz);
					} catch(Throwable t) {
						logger.error("Unable to instantiate component class [{}]", clazz.getCanonicalName());
					}
				}
				*/
				return true;
			} else {
				return false;
			}
		}
	}
	
	private BaseController instantiateController(String serviceId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		
		try {
			return (BaseController)descr.getControllerClass().newInstance();
		} catch(Throwable t) {
			logger.error("Controller: instantiation failure [{}]", descr.getManifest().getControllerClassName(), t);
			return null;
		}
	}
	
	public CoreManager instantiateCoreManager(UserProfile.Id targetProfileId) {
		return new CoreManager(wta, targetProfileId);
	}
	
	public BaseManager instantiateServiceManager(String serviceId, UserProfile.Id targetProfileId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasManager()) return null;
		
		try {
			Class clazz = descr.getManagerClass();
			Constructor<BaseManager> constructor = clazz.getConstructor(UserProfile.Id.class);
			return constructor.newInstance(targetProfileId);
		} catch(Throwable t) {
			logger.error("Manager: instantiation failure [{}]", descr.getManifest().getManagerClassName(), t);
			return null;
		}
	}
	
	public BaseRestApi instantiateRestApi(String serviceId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		
		try {
			Class clazz = descr.getRestApiClass();
			Constructor<BaseRestApi> constructor = clazz.getConstructor();
			return constructor.newInstance();
		} catch(Throwable t) {
			logger.error("RestApi: instantiation failure [{}]", descr.getManifest().getRestApiClassName(), t);
			return null;
		}
	}
	
	public BaseService instantiatePrivateService(String serviceId, String sessionId, Environment env) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasPrivateService()) throw new RuntimeException("Service has no default class");
		
		// Creates service instance
		BaseService instance = null;
		try {
			instance = (BaseService)descr.getPrivateServiceClass().newInstance();
		} catch(Throwable t) {
			logger.error("PrivateService: instantiation failure [{}]", descr.getManifest().getPrivateServiceClassName(), t);
			return null;
		}
		instance.configure(env);
		
		// Calls initialization method
		try {
			WebTopApp.setServiceLoggerDC(serviceId);
			instance.initialize();
		} catch(Throwable t) {
			logger.error("PrivateService: initialize() throws errors [{}]", instance.getClass().getCanonicalName(), t);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
		
		return instance;
	}
	
	public void cleanupPrivateService(BaseService instance) {
		try {
			WebTopApp.setServiceLoggerDC(instance.getManifest().getId());
			instance.cleanup();
		} catch(Throwable t) {
			logger.error("PrivateService: cleanup() throws errors [{}]", instance.getClass().getCanonicalName(), t);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
	}
	
	public BaseUserOptionsService instantiateUserOptionsService(UserProfile sessionProfile, String sessionId, String serviceId, UserProfile.Id targetProfileId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasUserOptionsService()) throw new RuntimeException("Service has no userOptions service class");
		
		BaseUserOptionsService instance = null;
		try {
			instance = (BaseUserOptionsService)descr.getUserOptionsServiceClass().newInstance();
		} catch(Throwable t) {
			logger.error("UserOptions: instantiation failure [{}]", descr.getManifest().getUserOptionsServiceClassName(), t);
			return null;
		}
		instance.configure(sessionProfile, targetProfileId);
		return instance;
	}
	
	private boolean createPublicService(String serviceId) {
		synchronized(publicServices) {
			if(publicServices.containsKey(serviceId)) throw new RuntimeException("Cannot add public service twice");
			BasePublicService inst = instantiatePublicService(serviceId);
			if(inst != null) {
				publicServices.put(serviceId, inst);
				return true;
			} else {
				return false;
			}
		}
	}
	
	private BasePublicService instantiatePublicService(String serviceId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasPublicService()) throw new RuntimeException("Service has no public class");
		
		BasePublicService instance = null;
		try {
			instance = (BasePublicService)descr.getPublicServiceClass().newInstance();
		} catch(Throwable t) {
			logger.error("PublicService: instantiation failure [{}]", descr.getManifest().getPublicServiceClassName(), t);
			return null;
		}
		instance.configure(wta.getAdminSubject());
		return instance;
	}
	
	private void initializePublicService(BasePublicService instance) {
		try {
			WebTopApp.setServiceLoggerDC(instance.SERVICE_ID);
			instance.initialize();
		} catch(Throwable t) {
			logger.error("PublicService: initialize() throws errors [{}]", instance.getClass().getCanonicalName(), t);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
	}
	
	private void cleanupPublicService(BasePublicService instance) {
		try {
			WebTopApp.setServiceLoggerDC(instance.getManifest().getId());
			instance.cleanup();
		} catch(Throwable t) {
			logger.error("PublicService: cleanup() throws errors [{}]", instance.getClass().getCanonicalName(), t);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
	}
	
	private boolean createJobService(String serviceId) {
		synchronized(jobServices) {
			if(jobServices.containsKey(serviceId)) throw new RuntimeException("Cannot add job service twice");
			BaseJobService inst = instantiateJobService(serviceId);
			if(inst != null) {
				jobServices.put(serviceId, inst);
				return true;
			} else {
				return false;
			}
		}
	}
	
	private BaseJobService instantiateJobService(String serviceId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasJobService()) throw new RuntimeException("Service has no job class");
		
		BaseJobService instance = null;
		try {
			instance = (BaseJobService)descr.getJobServiceClass().newInstance();
		} catch(Throwable t) {
			logger.error("JobService: instantiation failure [{}]", descr.getManifest().getJobServiceClassName(), t);
			return null;
		}
		instance.configure(wta.getAdminSubject());
		return instance;
	}
	
	private void initializeJobService(BaseJobService instance) {
		try {
			WebTopApp.setServiceLoggerDC(instance.SERVICE_ID);
			instance.initialize();
		} catch(Throwable t) {
			logger.error("JobService: initialize() throws errors [{}]", t);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
	}
	
	private void cleanupJobService(BaseJobService instance) {
		try {
			WebTopApp.setServiceLoggerDC(instance.getManifest().getId());
			instance.cleanup();
		} catch(Throwable t) {
			logger.error("JobService: cleanup() throws errors [{}]", t);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
	}
	
	
	
	
	
	
	
	
	
	private ArrayList<ServiceManifest> discoverServices() throws IOException {
		ClassLoader cl = LangUtils.findClassLoader(getClass());
		
		// Scans classpath looking for service descriptor files
		Enumeration<URL> enumResources = null;
		try {
			enumResources = cl.getResources(SERVICES_DESCRIPTOR_RESOURCE);
		} catch(IOException ex) {
			throw ex;
		}
		
		// Parses and splits descriptor files into a single manifest file for each service
		ArrayList<ServiceManifest> manifests = new ArrayList();
		while(enumResources.hasMoreElements()) {
			URL url = enumResources.nextElement();
			try {
				manifests.addAll(parseDescriptor(url));
			} catch(ConfigurationException ex) {
				logger.error("Error while reading descriptor [{}]", url.toString(), ex);
			}
		}
		return manifests;
	}
	
	private ArrayList<ServiceManifest> parseDescriptor(final URL descriptorUri) throws ConfigurationException {
		ArrayList<ServiceManifest> manifests = new ArrayList();
		ServiceManifest manifest = null;
		
		logger.trace("Parsing descriptor [{}]", descriptorUri.toString());
		XMLConfiguration config = new XMLConfiguration(descriptorUri);
		List<HierarchicalConfiguration> elServices = config.configurationsAt("service");
		for(HierarchicalConfiguration elService : elServices) {
			try {
				manifest = new ServiceManifest(elService);
				manifests.add(manifest);
				
			} catch(Exception ex) {
				logger.warn("Service descriptor skipped. Cause: {}", ex.getMessage());
			}
		}
		return manifests;
	}
	
	private void registerService(ServiceManifest manifest) {
		ConnectionManager conm = wta.getConnectionManager();
		ServiceDescriptor desc = null;
		String serviceId = manifest.getId();
		String xid = manifest.getXId();
		boolean maintenance = false;
		
		logger.info("Registering service [{}]", serviceId);
		synchronized(lock) {
			if(descriptors.containsKey(serviceId)) throw new WTRuntimeException("Service ID is already registered [{0}]", serviceId);	
			if(xidToServiceId.containsKey(xid)) throw new WTRuntimeException("Service XID (short ID) is already bound to a service [{0} -> {1}]", xid, xidToServiceId.get(xid));
			
			desc = new ServiceDescriptor(manifest);
			logger.info("[private:{}, public:{}, job:{}, userOptions:{}]", desc.hasPrivateService(), desc.hasPublicService(), desc.hasJobService(), desc.hasUserOptionsService());
			
			// Register service's dataSources
			try {
				DataSourcesConfig config = conm.getConfiguration();
				DataSourcesConfig.HikariConfigMap sources = config.getSources(serviceId);
				if(sources != null) {
					logger.debug("Registering {} dataSources", sources.size());
					// If service provides its own sources, register them...
					for(Entry<String, HikariConfig> entry : sources.entrySet()) {
						if(!conm.isRegistered(serviceId, entry.getKey())) {
							conm.registerDataSource(serviceId, entry.getKey(), entry.getValue());
						}
					}
				} else {
					logger.debug("No custom dataSources defined");
				}
				
			} catch(Exception ex) {
				throw new WTRuntimeException(ex, "Error registering service dataSources");
			}
			
			boolean upgraded = upgradeCheck(manifest);
			desc.setUpgraded(upgraded);
			if(upgraded) {
				// Force whatsnew pre-cache
				desc.getWhatsnew(wta.getSystemLocale(), manifest.getOldVersion());
			}

			// If already in maintenance, keeps it active
			if(!isInMaintenance(serviceId)) {
				// ...otherwise sets it!
				setMaintenance(serviceId, maintenance);
			}
			
			descriptors.put(serviceId, desc);
			xidToServiceId.put(xid, serviceId);
			serviceIdToJsPath.put(serviceId, manifest.getJsPath());
			
			String publicName = generatePublicName(serviceId);
			if(publicNameToServiceId.containsKey(publicName)) {
				logger.warn("Service public name [{}] conflict! [{} hides {}]", publicName, serviceId, publicNameToServiceId.get(publicName));
				//TODO: valutare se portare il servizio in manutenzione
			}
			publicNameToServiceId.put(publicName, serviceId);
			
			// Adds service references into static map in order to facilitate ID lookup
			WT.manifestCache.put(serviceId, manifest);
		}
	}
	
	private String generatePublicName(String serviceId) {
		SettingsManager setm = wta.getSettingsManager();
		String overriddenPublicName = setm.getServiceSetting(serviceId, CoreServiceSettings.PUBLIC_NAME);
		
		if(!StringUtils.isEmpty(overriddenPublicName)) {
			return overriddenPublicName;
		} else {
			String[] tokens = StringUtils.split(serviceId, ".");
			return (tokens.length > 0) ? tokens[tokens.length-1] : serviceId;
		}
	}
	
	private boolean upgradeCheck(ServiceManifest manifest) {
		SettingsManager setm = wta.getSettingsManager();
		ServiceVersion manifestVer = null, currentVer = null;
		
		// Gets current service's version info
		manifestVer = manifest.getVersion();
		currentVer = new ServiceVersion(setm.getServiceSetting(manifest.getId(), CoreServiceSettings.MANIFEST_VERSION));
		
		// Upgrade check!
		if(manifestVer.compareTo(currentVer) > 0) {
			logger.info("Upgraded! [{} -> {}] Updating version setting...", currentVer.toString(), manifestVer.toString());
			manifest.setOldVersion(currentVer);
			setm.setServiceSetting(manifest.getId(), CoreServiceSettings.MANIFEST_VERSION, manifestVer.toString());
			return true;
		} else {
			logger.info("Not upgraded! [{} = {}]", manifestVer.toString(), currentVer.toString());
			return false;
		}
	}
	
	
	
	private void postponeJobsInitialization() {
		Thread engine = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					synchronized(jobServices) {
						for(Entry<String, BaseJobService> entry : jobServices.entrySet()) {
							initializeJobService(entry.getValue());
						}
					}	
				} catch (InterruptedException ex) { /* Do nothing... */	}
			}
		});
		engine.start();		
	}
	
	private JobDetail createJobTask(String serviceId, BaseJobService service, TaskDefinition taskDef) {
		String classBaseName = taskDef.clazz.getSimpleName();
		
		JobDataMap data = (taskDef.data != null) ? taskDef.data : new JobDataMap();
		data.put("jobService", service);
		JobBuilder jb = JobBuilder.newJob(taskDef.clazz)
				.usingJobData(data)
				.withIdentity(classBaseName, serviceId);
		if(!StringUtils.isEmpty(taskDef.description)) jb.withDescription(taskDef.description);
		return jb.build();
	}
	
	private Trigger createJobTaskTrigger(String serviceId, TaskDefinition taskDef) {
		String classBaseName = taskDef.clazz.getSimpleName();
		
		TriggerBuilder tb = taskDef.trigger.getTriggerBuilder()
				.withIdentity(classBaseName, serviceId)
				.startNow();
		return tb.build();
	}
	
	private void scheduleJobServiceTasks(String serviceId, BaseJobService service) {
		List<TaskDefinition> taskDefs = null;
		JobDetail jobDetail = null;
		Trigger trigger = null;
		
		try {
			// Gets task definitions from base service definition
			try {
				WebTopApp.setServiceLoggerDC(serviceId);
				taskDefs = service.returnTasks();
			} catch(Exception ex) {
				logger.error("JobService method returns errors [returnTask()]", ex);
				throw ex;
			} finally {
				WebTopApp.unsetServiceLoggerDC();
			}
			if(taskDefs != null) {
				unscheduleJobServiceTasks(serviceId);
				
				// Schedule job defining its trigger and details
				for(TaskDefinition taskDef : taskDefs) {
					jobDetail = createJobTask(serviceId, service, taskDef);
					trigger = createJobTaskTrigger(serviceId, taskDef);
					scheduler.scheduleJob(jobDetail, trigger);
					//scheduler.scheduleJob(jobDetail, taskDef.trigger);
					logger.debug("Task scheduled [{}]", jobDetail.getKey().toString());
				}
			}
			
		} catch(SchedulerException ex) {
			logger.error("Error scheduling task [{}]", jobDetail.getKey().toString(), ex);
		} catch(Exception ex) {
			logger.error("Error instantiating task [{}]", jobDetail.getKey().toString(), ex);
		}
	}
	
	private void unscheduleJobServiceTasks(String serviceId) {
		try {
			Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(serviceId));
			scheduler.deleteJobs(new ArrayList<>(keys));
			logger.debug("Deleted tasks for group [{}]", serviceId);
			
		} catch(SchedulerException ex) {
			logger.error("Error deleting tasks for group [{}]", serviceId, ex);
		}
	}
}