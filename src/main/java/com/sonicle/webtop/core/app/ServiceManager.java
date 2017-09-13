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
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.db.StatementUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.bol.OUpgradeStatement;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.UpgradeStatementDAO;
import com.sonicle.webtop.core.sdk.BaseRestApi;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseJobService.TaskDefinition;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.sdk.interfaces.IControllerHandlesProfiles;
import com.sonicle.webtop.core.util.LoggerUtils;
import com.sonicle.webtop.core.versioning.AnnotationLine;
import com.sonicle.webtop.core.versioning.BaseScriptLine;
import com.sonicle.webtop.core.versioning.DataSourceAnnotationLine;
import com.sonicle.webtop.core.versioning.IgnoreErrorsAnnotationLine;
import com.sonicle.webtop.core.versioning.RequireAdminAnnotationLine;
import com.sonicle.webtop.core.versioning.SqlLine;
import com.sonicle.webtop.core.versioning.SqlUpgradeScript;
import com.zaxxer.hikari.HikariConfig;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
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
	private final Object lock1 = new Object();
	private final Object lock2 = new Object();
	
	private final LinkedHashMap<String, ServiceDescriptor> descriptors = new LinkedHashMap<>();
	private final HashMap<String, String> xidToServiceId = new HashMap<>();
	private final HashMap<String, String> serviceIdToJsPath = new HashMap<>();
	private final LinkedHashMap<String, BaseController> controllers = new LinkedHashMap<>();
	private final HashMap<String, String> serviceIdToPublicName = new HashMap<>();
	private final HashMap<String, String> publicNameToServiceId = new HashMap<>();
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
			// Cleanup job service
			//TODO: effettuare lo shutdown dei task
			jobInst = jobServices.remove(serviceId);
			if(jobInst != null) cleanupJobService(jobInst);
		}
		
		jobServices.clear();
		descriptors.clear();
		xidToServiceId.clear();
		serviceIdToJsPath.clear();
		serviceIdToPublicName.clear();
		publicNameToServiceId.clear();
		scheduler = null;
		wta = null;
		logger.info("ServiceManager destroyed");
	}
	
	private String getUpgradeTag() {
		UpgradeStatementDAO upgdao = UpgradeStatementDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			String lastTag = upgdao.selectLastTag(con);
			int pendingUpgrades = upgdao.countPendingByTagType(con, lastTag, OUpgradeStatement.STATEMENT_TYPE_SQL);
			if (pendingUpgrades == 0) {
				return String.valueOf(DateTimeUtils.now(true).getMillis());
			} else {
				return lastTag;
			}
		} catch(SQLException | DAOException ex) {
			logger.error("Unable to determine upgrade-tag", ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void init() {
		
		// Programmatically register Core's manifest
		// This call must be the first because it performs main dataSource registration
		ServiceDescriptor coreDesc = registerService(new CoreManifest());
		
		boolean dbInitEnabled = isDbInitEnabled();
		boolean dbAutoUpgrade = isDbAutoUpgrade();
		boolean maintenanceDisabled = isMaintenanceDisabled();
		boolean requireAdmin = false;
		
		// Always insert the maintenance flag. If useless 
		// (requireAdmin=false) it will be removed at the end of the
		// initialization process. In case of unexpected errors, we are
		// sure that clients cannot connect before admin intervention.
		boolean oldMaintenance = isInMaintenance(CoreManifest.ID);
		if (!oldMaintenance) setMaintenance(CoreManifest.ID, true);

		// Defines a proper upgrade-tag
		String upgradeTag = getUpgradeTag();
		logger.info("Database upgrades will be appended to {}", upgradeTag);
		
		// Upgrade database (core)
		if (coreDesc.isUpgraded()) {
			requireAdmin = requireAdmin | upgradeServiceDb(coreDesc, upgradeTag, dbAutoUpgrade);
		}
		
		// Programmatically register the core admin's manifest
		registerService(new CoreAdminManifest());
		
		// Loads services' manifest files from classpath
		logger.debug("Starting services discovery...");
		ArrayList<ServiceManifest> manifests = null;
		try {
			manifests = discoverServices();
			Collections.sort(manifests, new Comparator<ServiceManifest>() {
				@Override
				public int compare(ServiceManifest o1, ServiceManifest o2) {
					return o1.getId().compareTo(o2.getId()); // a->z
					//return o2.getId().compareTo(o1.getId()); // z->a
				}
			});
		} catch (IOException ex) {
			throw new RuntimeException("Error during services discovery", ex);
		}

		// Register discovered services
		for(ServiceManifest manifest : manifests) {
			String sid = manifest.getId();
			ServiceDescriptor desc = registerService(manifest);
			
			// Init database
			if (dbInitEnabled) {
				//TODO: implementare inizializzazione database
			}

			// Upgrade database
			if (desc.isUpgraded()) {
				requireAdmin = requireAdmin | upgradeServiceDb(desc, upgradeTag, dbAutoUpgrade);
			}
			
			// Inits classes
			createController(sid);
		}
		
		// Handle post db-scripts
		for (String serviceId : listRegisteredServices()) {
			ServiceDescriptor desc = getDescriptor(serviceId);
			requireAdmin = requireAdmin | postUpgradeServiceDb(desc, upgradeTag, dbAutoUpgrade);
		}
		
		if (requireAdmin) {
			logger.warn("SysAdmin intervention is needed!");
		} else {
			// Admin support is not needed. Restore the maintenance value 
			// read before startup. This prevents from overriding changes made 
			// by a previous application startup
			if (!oldMaintenance) {
				logger.debug("Maintenance mode disabled");
				setMaintenance(CoreManifest.ID, false);
			} else {
				logger.debug("Maintenance mode keep enabled");
			}
		}
		
		// Forcedly turn-off maintenance
		if (maintenanceDisabled) {
			logger.debug("Maintenance forcedly disabled (independently from startup state)");
			wta.getSettingsManager().setServiceSetting(CoreManifest.ID, CoreSettings.MAINTENANCE, false);
		}
		
		// Instantiate job services
		int okJobs = 0, failJobs = 0;
		for(String serviceId : listPrivateServices()) {
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
		logger.debug("Instantiated {} of {} job services", okJobs, (okJobs+failJobs));
	}
	
	/**
	 * Initialize each JobService instance.
	 * Postpone this methos call, WebTopApp instance must be set!
	 */
	public void initializeJobServices() {
		// Inits job services
		synchronized(jobServices) {
			for(Entry<String, BaseJobService> entry : jobServices.entrySet()) {
				initializeJobService(entry.getValue());
			}
		}
	}
	
	/**
	 * Returns if this manager contains a descriptor for the specified service.
	 * @param serviceId The service ID.
	 * @return True, if service is valid, false otherwise.
	 */
	public boolean hasService(String serviceId) {
		return descriptors.containsKey(serviceId);
	}
	
	public String getServiceJsPath(String serviceId) {
		return serviceIdToJsPath.get(serviceId);
	}
	
	public String getPublicName(String serviceId) {
		return serviceIdToPublicName.get(serviceId);
	}
	
	/**
	 * Returns the service ID by its public-name (if available).
	 * @param publicName The service's public-name.
	 * @return The service ID if found, null otherwise.
	 */
	public String getServiceIdByPublicName(String publicName) {
		return publicNameToServiceId.get(publicName);
	}
	
	/**
	 * Gets descriptor for a specified service.
	 * @param serviceId The service ID.
	 * @return Service descriptor object.
	 */
	ServiceDescriptor getDescriptor(String serviceId) {
		synchronized(lock1) {
			if(!descriptors.containsKey(serviceId)) return null;
			return descriptors.get(serviceId);
		}
	}
	
	public boolean isCoreService(String serviceId) {
		return serviceId.equals(CoreManifest.ID);
	}
	
	public boolean hasFullRights(String serviceId) {
		return isCoreService(serviceId);
	}
	
	private boolean isDbInitEnabled() {
		SettingsManager setMgr = wta.getSettingsManager();
		return LangUtils.value(setMgr.getServiceSetting(CoreManifest.ID, CoreSettings.DB_INIT_ENABLED), true);
	}
	
	private boolean isDbAutoUpgrade() {
		SettingsManager setMgr = wta.getSettingsManager();
		return LangUtils.value(setMgr.getServiceSetting(CoreManifest.ID, CoreSettings.DB_UPGRADE_AUTO), true);
	}
	
	private boolean isMaintenanceDisabled() {
		SettingsManager setMgr = wta.getSettingsManager();
		return LangUtils.value(setMgr.getServiceSetting(CoreManifest.ID, CoreSettings.MAINTENANCE_ENABLED), false);
	}
	
	public boolean isInMaintenance(String serviceId) {
		SettingsManager setMgr = wta.getSettingsManager();
		return LangUtils.value(setMgr.getServiceSetting(serviceId, CoreSettings.MAINTENANCE), false);
	}
	
	public boolean isInDevMode(String serviceId) {
		Boolean bool = LangUtils.value(wta.getSettingsManager().getServiceSetting(serviceId, CoreSettings.DEV_MODE), (Boolean)null);
		return (bool == null) ? wta.getStartupProperties().getDevMode() : bool;
	}
	
	public void setMaintenance(String serviceId, boolean maintenance) {
		SettingsManager setMgr = wta.getSettingsManager();
		setMgr.setServiceSetting(serviceId, CoreSettings.MAINTENANCE, maintenance);
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
		synchronized(lock1) {
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
		synchronized(lock1) {
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
		synchronized(lock1) {
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
		synchronized(lock1) {
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
		synchronized(lock1) {
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
		synchronized(lock1) {
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
		synchronized(lock1) {
			for(ServiceDescriptor descr : descriptors.values()) {
				if(descr.hasUserOptionsService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Checks if a specific user is ready for the specified service.
	 * Firstly it will check initialization flag, if not present it
	 * initializes the user calling the addProfile method implementation
	 * related to IControllerHandlesProfiles interface (if present).
	 * At the end of the process the user is initialized.
	 * Lastly, if above initialization will not take place and the service
	 * is upgraded for the user, the upgradeProfile method implementation
	 * related to the IControllerHandlesProfiles interface will be called.
	 * @param serviceId The service ID.
	 * @param profileId The user ID.
	 */
	public void prepareProfile(String serviceId, UserProfileId profileId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		
		if (descr.doesControllerImplements(IControllerHandlesProfiles.class)) {
			BaseController instance = getController(serviceId);
			IControllerHandlesProfiles controller = (IControllerHandlesProfiles)instance;
			
			if (!checkAndSetProfileInitialization(serviceId, profileId)) {
				logger.debug("Initializing profile for service [{}]", serviceId);
				try {
					LoggerUtils.setContextDC(instance.SERVICE_ID);
					controller.addProfile(profileId);
				} catch(Throwable t) {
					//TODO: valutare se ritornare un booleano per verifica
					logger.error("Controller: addProfile() throws errors", t);
				} finally {
					LoggerUtils.clearContextServiceDC();
				}
				
			} else {
				ProfileVersionEvaluationResult res = evaluateProfileVersion(descr.getManifest(), profileId);
				if (res.upgraded) {
					logger.debug("Upgrading profile for service [{}]", serviceId);
					try {
						LoggerUtils.setContextDC(instance.SERVICE_ID);
						controller.upgradeProfile(profileId, res.currentVersion, res.lastSeenVersion);
					} catch(Throwable t) {
						//TODO: valutare se ritornare un booleano per verifica
						logger.error("Controller: addProfile() throws errors", t);
					} finally {
						LoggerUtils.clearContextServiceDC();
					}
				}
			}
		}
	}
	
	/**
	 * Cleanup the specified user calling the controller implementation 
	 * (if present) related to IControllerHandlesProfiles interface.
	 * @param serviceId The service ID.
	 * @param profileId The user profile ID.
	 */
	public void cleanupProfile(String serviceId, UserProfileId profileId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(descr.doesControllerImplements(IControllerHandlesProfiles.class)) {
			BaseController instance = getController(serviceId);
			IControllerHandlesProfiles controller = (IControllerHandlesProfiles)instance;
			
			logger.debug("Cleaning-up profile for service [{}]", serviceId);
			try {
				LoggerUtils.setContextDC(instance.SERVICE_ID);
				controller.removeProfile(profileId, false);
			} catch(Throwable t) {
				logger.error("Controller: removeProfile() throws errors", t);
			} finally {
				LoggerUtils.clearContextServiceDC();
			}
		}
	}
	
	/**
	 * Returns current initialization user-setting for a specific user.
	 * If false, this method immediately set it to true value.
	 * @param serviceId The service ID.
	 * @param profileId The user profile ID.
	 * @return The original value read before any update.
	 */
	private boolean checkAndSetProfileInitialization(String serviceId, UserProfileId profileId) {
		synchronized(lock2) {
			SettingsManager setMgr = wta.getSettingsManager();
			boolean value = LangUtils.value(setMgr.getUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, CoreSettings.INITIALIZED), false);
			if (!value) {
				setMgr.setUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, CoreSettings.INITIALIZED, true);
			}
			return value;
		}
	}	
	
	public ProfileVersionEvaluationResult evaluateProfileVersion(String serviceId, UserProfileId profileId) {
		ServiceDescriptor desc = getDescriptor(serviceId);
		return evaluateProfileVersion(desc.getManifest(), profileId);
	}
	
	public ProfileVersionEvaluationResult evaluateProfileVersion(ServiceManifest manifest, UserProfileId profileId) {
		SettingsManager setm = wta.getSettingsManager();
		ServiceVersion manifestVer = manifest.getVersion();
		ServiceVersion userVer = new ServiceVersion(CoreUserSettings.getWhatsnewVersion(setm, profileId, manifest.getId()));
		boolean upgraded = (manifestVer.compareTo(userVer) > 0);
		return new ProfileVersionEvaluationResult(manifestVer, userVer, upgraded);
	}
	
	/**
	 * Checks if a specific service needs to show whatsnew for passed user.
	 * @param serviceId The service ID.
	 * @param profile The user profile.
	 * @return True if so, false otherwise.
	 */
	public boolean needWhatsnew(String serviceId, UserProfile profile) {
		ServiceDescriptor desc = getDescriptor(serviceId);
		
		// Gets current service's version info and last version for this user
		ProfileVersionEvaluationResult res = evaluateProfileVersion(desc.getManifest(), profile.getId());
		
		boolean show = false;
		if(res.upgraded) {
			String html = desc.getWhatsnew(profile.getLocale(), res.lastSeenVersion);
			if(StringUtils.isEmpty(html)) {
				// If content is empty, updates whatsnew version for the user;
				// it basically realign versions in user-settings.
				logger.trace("Whatsnew empty [{}]", serviceId);
				resetWhatsnew(serviceId, profile.getId());
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
			fromVersion = new ServiceVersion(CoreUserSettings.getWhatsnewVersion(setm, profile.getId(), serviceId));
		}
		return desc.getWhatsnew(profile.getLocale(), fromVersion);
	}
	
	/**
	 * Resets whatsnew updating service's version in user-setting
	 * to the current one.
	 * @param serviceId The service ID.
	 * @param profileId The user profile ID.
	 */
	public void resetWhatsnew(String serviceId, UserProfileId profileId) {
		SettingsManager setm = wta.getSettingsManager();
		ServiceVersion manifestVer = null;
		
		// Gets current service's version info
		manifestVer = getManifest(serviceId).getVersion();
		CoreUserSettings.setWhatsnewVersion(setm, profileId, serviceId, manifestVer.toString());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void scheduleAllJobServicesTasks() {
		if(!wta.isLatest()) return; // Make sure we are in latest webapp
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
		if(wta.isLatest()) {
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
	
	public CoreManager instantiateCoreManager(boolean fastInit, UserProfileId targetProfileId) {
		return new CoreManager(wta, fastInit, targetProfileId);
	}
	
	public CoreAdminManager instantiateCoreAdminManager(boolean fastInit, UserProfileId targetProfileId) {
		return new CoreAdminManager(wta, fastInit, targetProfileId);
	}
	
	public BaseManager instantiateServiceManager(String serviceId, boolean fastInit, UserProfileId targetProfileId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasManager()) return null;
		
		try {
			Class clazz = descr.getManagerClass();
			Constructor<BaseManager> constructor = clazz.getConstructor(boolean.class, UserProfileId.class);
			return constructor.newInstance(fastInit, targetProfileId);
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
	
	public BaseService instantiatePrivateService(String serviceId, PrivateEnvironment environment) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasPrivateService()) throw new RuntimeException("Service has no default class");
		
		// Creates service instance
		BaseService instance = null;
		try {
			instance = (BaseService)descr.getPrivateServiceClass().newInstance();
		} catch(Throwable t) {
			logger.error("PrivateService: instantiation failure [{}]", t, descr.getManifest().getPrivateServiceClassName());
			return null;
		}
		instance.configure(environment);
		
		// Calls initialization method
		logger.trace("PrivateService: calling initialize() [{}]", serviceId);
		try {
			LoggerUtils.setContextDC(serviceId);
			instance.initialize();
		} catch(Throwable t) {
			logger.error("PrivateService: initialize() throws errors [{}]", t, instance.getClass().getCanonicalName());
		} finally {
			LoggerUtils.clearContextServiceDC();
		}
		
		return instance;
	}
	
	public void cleanupPrivateService(BaseService instance) {
		logger.trace("PrivateService: calling cleanup() [{}]", instance.SERVICE_ID);
		try {
			LoggerUtils.setContextDC(instance.SERVICE_ID);
			instance.cleanup();
		} catch(Throwable t) {
			logger.error("PrivateService: cleanup() throws errors [{}]", t, instance.getClass().getCanonicalName());
		} finally {
			LoggerUtils.clearContextServiceDC();
		}
	}
	
	public BaseUserOptionsService instantiateUserOptionsService(UserProfile sessionProfile, String sessionId, String serviceId, UserProfileId targetProfileId) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasUserOptionsService()) throw new RuntimeException("Service has no userOptions service class");
		
		BaseUserOptionsService instance = null;
		try {
			instance = (BaseUserOptionsService)descr.getUserOptionsServiceClass().newInstance();
		} catch(Throwable t) {
			logger.error("UserOptions: instantiation failure [{}]", t, descr.getManifest().getUserOptionsServiceClassName());
			return null;
		}
		instance.configure(sessionProfile, targetProfileId);
		return instance;
	}
	
	public BasePublicService instantiatePublicService(String serviceId, PublicEnvironment environment) {
		ServiceDescriptor descr = getDescriptor(serviceId);
		if(!descr.hasPublicService()) throw new WTRuntimeException("Service [{}] has no public class", serviceId);
		
		BasePublicService instance = null;
		try {
			instance = (BasePublicService)descr.getPublicServiceClass().newInstance();
		} catch(Throwable t) {
			logger.error("PublicService: instantiation failure [{}]", t, descr.getManifest().getPublicServiceClassName());
			return null;
		}
		instance.configure(environment);
		
		// Calls initialization method
		logger.trace("PrivateService: calling initialize() [{}]", serviceId);
		try {
			LoggerUtils.setContextDC(instance.SERVICE_ID);
			instance.initialize();
		} catch(Throwable t) {
			logger.error("PublicService: initialize() throws errors [{}]", t, instance.getClass().getCanonicalName());
		} finally {
			LoggerUtils.clearContextServiceDC();
		}
		
		return instance;
	}
	
	public void cleanupPublicService(BasePublicService instance) {
		logger.trace("PublicService: calling cleanup() [{}]", instance.SERVICE_ID);
		try {
			LoggerUtils.setContextDC(instance.SERVICE_ID);
			instance.cleanup();
		} catch(Throwable t) {
			logger.error("PublicService: cleanup() throws errors [{}]", t, instance.getClass().getCanonicalName());
		} finally {
			LoggerUtils.clearContextServiceDC();
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
			logger.error("JobService: instantiation failure [{}]", t, descr.getManifest().getJobServiceClassName());
			return null;
		}
		instance.configure(wta.getAdminSubject());
		return instance;
	}
	
	private void initializeJobService(BaseJobService instance) {
		logger.trace("JobService: calling initialize() [{}]", instance.SERVICE_ID);
		try {
			LoggerUtils.setContextDC(instance.SERVICE_ID);
			instance.initialize();
		} catch(Throwable t) {
			logger.error("JobService: initialize() throws errors [{}]", t, instance.getClass().getCanonicalName());
		} finally {
			LoggerUtils.clearContextServiceDC();
		}
	}
	
	private void cleanupJobService(BaseJobService instance) {
		logger.trace("JobService: calling cleanup() [{}]", instance.SERVICE_ID);
		try {
			LoggerUtils.setContextDC(instance.getManifest().getId());
			instance.cleanup();
		} catch(Throwable t) {
			logger.error("JobService: cleanup() throws errors [{}]", t, instance.getClass().getCanonicalName());
		} finally {
			LoggerUtils.clearContextServiceDC();
		}
	}
	
	
	
	
	
	
	
	private String generatePublicName(String serviceId) {
		SettingsManager setm = wta.getSettingsManager();
		String overriddenPublicName = setm.getServiceSetting(serviceId, CoreSettings.PUBLIC_NAME);
		
		if(!StringUtils.isEmpty(overriddenPublicName)) {
			return overriddenPublicName;
		} else {
			String[] tokens = StringUtils.split(serviceId, ".");
			return (tokens.length > 0) ? tokens[tokens.length-1] : serviceId;
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
	
	private ServiceDescriptor registerService(ServiceManifest manifest) {
		ConnectionManager conMgr = wta.getConnectionManager();
		ServiceDescriptor desc = null;
		String serviceId = manifest.getId();
		String xid = manifest.getXId();
		
		logger.info("Registering service [{}]", serviceId);
		synchronized(lock1) {
			if(descriptors.containsKey(serviceId)) throw new WTRuntimeException("Service ID is already registered [{0}]", serviceId);	
			if(xidToServiceId.containsKey(xid)) throw new WTRuntimeException("Service XID (short ID) is already bound to a service [{0} -> {1}]", xid, xidToServiceId.get(xid));
			
			desc = new ServiceDescriptor(manifest);
			logger.info("[private:{}, public:{}, job:{}, userOptions:{}]", desc.hasPrivateService(), desc.hasPublicService(), desc.hasJobService(), desc.hasUserOptionsService());
			
			// Register service's dataSources
			try {
				DataSourcesConfig config = conMgr.getConfiguration();
				DataSourcesConfig.HikariConfigMap sources = config.getSources(serviceId);
				if(sources != null) {
					logger.debug("Registering {} dataSources", sources.size());
					// If service provides its own sources, register them...
					for(Entry<String, HikariConfig> entry : sources.entrySet()) {
						if(!conMgr.isRegistered(serviceId, entry.getKey())) {
							conMgr.registerDataSource(serviceId, entry.getKey(), entry.getValue());
						}
					}
				} else {
					logger.debug("No custom dataSources defined");
				}
				
			} catch(Exception ex) {
				throw new WTRuntimeException(ex, "Error registering service dataSources");
			}
			
			// Upgrade check
			boolean upgraded = upgradeCheck(manifest);
			desc.setUpgraded(upgraded);
			if(upgraded) {
				// Force whatsnew pre-caching
				desc.getWhatsnew(wta.getSystemLocale(), manifest.getOldVersion());
			}

			descriptors.put(serviceId, desc);
			xidToServiceId.put(xid, serviceId);
			serviceIdToJsPath.put(serviceId, manifest.getJsPath());
			
			String publicName = generatePublicName(serviceId);
			if(publicNameToServiceId.containsKey(publicName)) {
				logger.warn("Service public name [{}] conflict! [{} hides {}]", publicName, serviceId, publicNameToServiceId.get(publicName));
				//TODO: valutare se portare il servizio in manutenzione
			}
			serviceIdToPublicName.put(serviceId, publicName);
			publicNameToServiceId.put(publicName, serviceId);
			
			// Adds service references into static map in order to facilitate ID lookup
			WT.manifestCache.put(serviceId, manifest);
			
			return desc;
		}
	}
	
	private boolean upgradeServiceDb(ServiceDescriptor desc, String upgradeTag, boolean autoUpdate) {
		ArrayList<SqlUpgradeScript> scripts = desc.getUpgradeScripts();
		return upgradeServiceDb(desc, scripts, upgradeTag, autoUpdate);
	}
	
	private boolean upgradeServiceDb(ServiceDescriptor desc, ArrayList<SqlUpgradeScript> scripts, String upgradeTag, boolean autoUpdate) {
		ConnectionManager conMgr = wta.getConnectionManager();
		UpgradeStatementDAO upgdao = UpgradeStatementDAO.getInstance();
		boolean requireAdmin = false;
		Connection con = null;
		
		try {
			if (!scripts.isEmpty()) {
				String serviceId = desc.getManifest().getId();
				
				// Transforms each statement into a specific object
				List<OUpgradeStatement> stmts = new ArrayList<>();
				short sequence = 0;
				for(SqlUpgradeScript script: scripts) {
					// Extracts and inserts statements
					ArrayList<BaseScriptLine> scriptStatements = script.getStatements();
					String targetDataSource = null;
					logger.trace("Script {}: found {} statement/s", script.getFileName(), scriptStatements.size());
					for(BaseScriptLine statement: scriptStatements) {
						sequence++;
						String stmtType, stmtDs;
						if (statement instanceof AnnotationLine) {
							stmtDs = null;
							stmtType = OUpgradeStatement.STATEMENT_TYPE_ANNOTATION;
							if (statement instanceof DataSourceAnnotationLine) {
								targetDataSource = ((DataSourceAnnotationLine) statement).getTargetDataSource();
							}
						} else if (statement instanceof SqlLine) {
							stmtDs = targetDataSource;
							stmtType = OUpgradeStatement.STATEMENT_TYPE_SQL;
						} else {
							stmtDs = null;
							stmtType = OUpgradeStatement.STATEMENT_TYPE_COMMENT;
						}
						stmts.add(new OUpgradeStatement(upgradeTag, serviceId, sequence, script.getFileName(), stmtDs, stmtType, statement.getText()));
					}
				}
				
				// Inserts extracted statements into a dedicated table
				con = conMgr.getConnection();
				Integer maxId = upgdao.maxId(con);
				int count = upgdao.batchInsert(con, stmts);
				if(count != sequence) throw new WTException("Statements insertion not fully completed [total: {0}, inserted: {1}]", sequence, count);
				
				// Reads all inserted statements (NB: id needs to be refreshed because it comes from a db sequence)
				stmts = upgdao.selectFromIdByTagService(con, maxId == null ? -1 : maxId, upgradeTag, serviceId);
				if(stmts.isEmpty()) {
					logger.debug("DB upgrade is not necessary [{}]", serviceId);
				} else {
					if(!autoUpdate) { // Manual
						requireAdmin = true;

					} else { // Auto: Runs statements...
						HashMap<String, Connection> conCache = new HashMap<>();
						try {
							logger.debug("Executing upgrade statements [{}, {}]", serviceId, stmts.size());
							boolean ignoreErrors = false;
							for(OUpgradeStatement stmt: stmts) {
								
								if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_ANNOTATION)) {
									if (RequireAdminAnnotationLine.matches(stmt.getStatementBody())) {
										logger.trace("[{}, {}]: {}", stmt.getServiceId(), stmt.getSequenceNo(), stmt.getStatementBody());
										requireAdmin = true;
										break; // Stops iteration!
									} else if (IgnoreErrorsAnnotationLine.matches(stmt.getStatementBody())) {
										ignoreErrors = true; // Sets value! (for next sql statement)
									}
									
								} else if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_SQL)) {
									final String sds = stmt.getStatementDataSource();
									if (StringUtils.isBlank(sds)) throw new WTException("Statement does not provide a DataSource [{0}]", stmt.getUpgradeStatementId());
									if (!conCache.containsKey(sds)) {
										conCache.put(sds, getUpgradeStatementConnection(conMgr, stmt));
									}
									boolean ret = executeUpgradeStatement(conCache.get(sds), stmt, ignoreErrors);
									try {
										upgdao.update(con, stmt);
									} catch(DAOException ex) {
										throw new WTException("Unable to update statement status!", ex);
									}
									if (!ret) { // In case of errors...
										requireAdmin = true;
										break; // Stops iteration!
									}
									ignoreErrors = false; // Resets value!
									
								} else {
									logger.trace("[{}, {}]: !!! {}", stmt.getServiceId(), stmt.getSequenceNo(), StringUtils.replace(stmt.getStatementBody(), "\n", " "));
								}
							}
						} finally {
							if (!conCache.isEmpty()) {
								logger.trace("Closing connections [{}]", conCache.size());
								Iterator<Entry<String, Connection>> it = conCache.entrySet().iterator();
								while(it.hasNext()) {
									final Entry<String, Connection> entry = it.next();
									DbUtils.closeQuietly(entry.getValue());
									it.remove();
								}
							}
						}
					}
				}
			}
		} catch(Throwable t) {
			requireAdmin = true;
			logger.error("Error handling upgrade script", t);
		} finally {
			DbUtils.closeQuietly(con);
			return requireAdmin;
		}
	}
	
	private boolean postUpgradeServiceDb(ServiceDescriptor desc, String upgradeTag, boolean autoUpdate) {
		ArrayList<SqlUpgradeScript> scripts = getPostDbScripts(desc.getManifest().getId());
		return upgradeServiceDb(desc, scripts, upgradeTag, autoUpdate);
	}
	
	private Connection getUpgradeStatementConnection(ConnectionManager conMgr, OUpgradeStatement statement) throws SQLException, WTException {
		String[] tokens = StringUtils.split(statement.getStatementDataSource(), "@", 2);
		if (tokens.length != 2) throw new WTException("Invalid DataSource [{0}]. It must in the following format: name@namespace", statement.getStatementDataSource());
		String namespace = tokens[1];
		String dataSourceName = tokens[0];
		return conMgr.getFallbackConnection(namespace, dataSourceName);
	}
	
	public boolean executeUpgradeStatement(OUpgradeStatement statement, boolean ignoreErrors) throws WTException {
		ConnectionManager conMgr = wta.getConnectionManager();
		UpgradeStatementDAO upgdao = UpgradeStatementDAO.getInstance();
		Connection con = null, targetCon = null;
		
		try {
			targetCon = getUpgradeStatementConnection(conMgr, statement);
			boolean ret = executeUpgradeStatement(targetCon, statement, ignoreErrors);
			try {
				con = conMgr.getConnection();
				upgdao.update(con, statement);
			} catch(DAOException ex) {
				throw new Exception("Unable to update statement status!", ex);
			}
			return ret;
			
		} catch(Throwable t) {
			throw new WTException(t);
		} finally {
			DbUtils.closeQuietly(con);
			DbUtils.closeQuietly(targetCon);
		}
	}
	
	public void skipUpgradeStatement(OUpgradeStatement statement) throws WTException {
		ConnectionManager conMgr = wta.getConnectionManager();
		UpgradeStatementDAO upgdao = UpgradeStatementDAO.getInstance();
		Connection con = null;
		
		try {
			con = conMgr.getConnection();
			statement.setRunStatus(OUpgradeStatement.RUN_STATUS_SKIPPED);
			logger.trace("[{}, {}]: {}", statement.getServiceId(), statement.getSequenceNo(), statement.getStatementBody());
			upgdao.update(con, statement);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException("Unable to update statement status!", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean executeUpgradeStatement(Connection statementCon, OUpgradeStatement statement, boolean ignoreErrors) throws Throwable {
		Statement stmt = null;
		
		try {
			logger.trace("[{}]: {}", statement.getUpgradeStatementId(), statement.getStatementBody());
			stmt = statementCon.createStatement();
			statement.setRunTimestamp(DateTimeUtils.now(true));
			int ret = stmt.executeUpdate(statement.getStatementBody());
			statement.setRunStatus(OUpgradeStatement.RUN_STATUS_OK);
			statement.setRunMessage(MessageFormat.format("Affected rows: {0}", ret));
			return true;
			
		} catch(SQLException ex) {
			if(!ignoreErrors) {
				statement.setRunStatus(OUpgradeStatement.RUN_STATUS_ERROR);
				statement.setRunMessage(ex.getMessage());
				logger.trace("{}", statement.getRunMessage());
				return false;
			} else {
				statement.setRunStatus(OUpgradeStatement.RUN_STATUS_WARNING);
				statement.setRunMessage(ex.getMessage());
				return true;
			}
		} finally {
			StatementUtils.closeQuietly(stmt);
		}
	}
	
	private boolean upgradeCheck(ServiceManifest manifest) {
		SettingsManager setm = wta.getSettingsManager();
		ServiceVersion manifestVer = null, currentVer = null;
		
		// Gets current service's version info
		manifestVer = manifest.getVersion();
		currentVer = new ServiceVersion(setm.getServiceSetting(manifest.getId(), CoreSettings.MANIFEST_VERSION));
		
		// Upgrade check!
		if(manifestVer.compareTo(currentVer) > 0) {
			logger.info("Upgrade found! [{} -> {}] Updating version setting...", currentVer.toString(), manifestVer.toString());
			manifest.setOldVersion(currentVer);
			setm.setServiceSetting(manifest.getId(), CoreSettings.MANIFEST_VERSION, manifestVer.toString());
			return true;
		} else {
			logger.info("Not upgraded! [{} = {}]", manifestVer.toString(), currentVer.toString());
			return false;
		}
	}
	
	private ArrayList<SqlUpgradeScript> getPostDbScripts(String serviceId) {
		ArrayList<SqlUpgradeScript> scripts = new ArrayList<>();
		FilenameFilter sqlFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("sql");
			}
		};
		
		try {
			String postPath = wta.getDbScriptsPostPath(serviceId);
			logger.debug("Looking for post db-scripts in [{}]", postPath);
			File postDir = new File(postPath);
			if (postDir.exists()) {
				File[] files = postDir.listFiles(sqlFilter);
				if(files != null) {
					for (File file: files) {
						try {
							logger.debug("Reading post db-scripts [{}]", file.getName());
							SqlUpgradeScript script = new SqlUpgradeScript(file);
							scripts.add(script);

						} catch(Exception ex1) {
							logger.warn(ex1.getMessage());
							// increment error counter...
						}
					}
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error loading post db-scripts", ex);
		}
		return scripts;
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
				LoggerUtils.setContextDC(serviceId);
				taskDefs = service.returnTasks();
			} catch(Exception ex) {
				logger.error("JobService method returns errors [returnTask()]", ex);
				throw ex;
			} finally {
				LoggerUtils.clearContextServiceDC();
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
	
	public static class ProfileVersionEvaluationResult {
		public final ServiceVersion currentVersion;
		public final ServiceVersion lastSeenVersion;
		public final boolean upgraded;
		
		public ProfileVersionEvaluationResult(ServiceVersion currentVersion, ServiceVersion lastSeenVersion, boolean upgraded) {
			this.currentVersion = currentVersion;
			this.lastSeenVersion = lastSeenVersion;
			this.upgraded = upgraded;
		}
	}
}
