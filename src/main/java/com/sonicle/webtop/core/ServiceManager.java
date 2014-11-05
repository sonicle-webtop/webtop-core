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
import com.sonicle.webtop.core.sdk.Environment;
import com.sonicle.webtop.core.sdk.InsufficientRightsException;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.Service;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ServiceManager {
	
	private static final Logger logger = WebTopApp.getLogger(ServiceManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized ServiceManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		ServiceManager svcm = new ServiceManager(wta);
		initialized = true;
		logger.info("ServiceManager initialized.");
		return svcm;
	}
	
	public static final String SERVICES_DESCRIPTOR_RESOURCE = "META-INF/webtop-services.xml";
	private WebTopApp wta = null;
	private final Object lock = new Object();
	private final LinkedHashMap<String, ServiceDescriptor> services = new LinkedHashMap<>();
	private final HashMap<String, String> xidMappings = new HashMap<>();
	private final HashMap<String, String> jsPathMappings = new HashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private ServiceManager(WebTopApp wta) {
		this.wta = wta;
		init();
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		services.clear();
		xidMappings.clear();
		jsPathMappings.clear();
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
					return o1.getId().compareTo(o2.getId());
				}
			});
		} catch (IOException ex) {
			throw new RuntimeException("Error during services discovery", ex);
		}
		
		// Register discovered services
		for(ServiceManifest manifest : manifests) {
			registerService(manifest);
		}
	}
	
	public boolean isMaintenance(String serviceId) {
		SettingsManager setm = wta.getSettingsManager();
		return LangUtils.value(setm.getServiceSetting(serviceId, CoreServiceSettings.MAINTENANCE), false);
	}
	
	public void setMaintenance(String serviceId, boolean maintenance) {
		SettingsManager setm = wta.getSettingsManager();
		setm.setServiceSetting(serviceId,CoreServiceSettings.MAINTENANCE, maintenance);
	}
	
	public Service instantiateService(String serviceId, Environment basicEnv, CoreEnvironment fullEnv) {
		ServiceDescriptor descr = getService(serviceId);
		if(!descr.hasDefaultService()) throw new RuntimeException("Service has no default class");
		
		// Creates service instance
		Service instance = null;
		try {
			instance = (Service)descr.getDefaultClass().newInstance();
		} catch(Exception ex) {
			logger.error("Error instantiating service [{}]", descr.getManifest().getClassName(), ex);
			return null;
		}
		instance.configure(basicEnv, fullEnv);
		
		// Calls initialization method
		try {
			WebTopApp.setServiceLoggerDC(serviceId);
			instance.initialize();
		} catch(InsufficientRightsException ex) {
			/* Do nothing... */
		} catch(Throwable ex) {
			logger.error("Initialization method returns errors", ex);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
		
		return instance;
	}
	
	public void cleanupDefaultService(Service instance) {
		// Calls cleanup method
		try {
			WebTopApp.setServiceLoggerDC(instance.getManifest().getId());
			instance.cleanup();
		} catch(Exception ex) {
			logger.error("Cleanup method returns errors", ex);
		} finally {
			WebTopApp.unsetServiceLoggerDC();
		}
	}
	
	public String getServiceJsPath(String serviceId) {
		return jsPathMappings.get(serviceId);
	}
	
	/**
	 * Returns registered services.
	 * @return List of service IDs.
	 */
	public List<String> getRegisteredServices() {
		synchronized(lock) {
			return Arrays.asList(services.keySet().toArray(new String[services.size()]));
		}
	}
	
	/**
	 * Lists discovered services.
	 * @return List of registered services.
	 */
	public List<String> getServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : services.values()) {
				if(descr.hasDefaultService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Lists discovered deamon services.
	 * @return List of registered deamon services.
	 */
	public List<String> getDeamonServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(lock) {
			for(ServiceDescriptor descr : services.values()) {
				if(descr.hasDeamonService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	/**
	 * Gets descriptor for a specified service.
	 * @param serviceId The service ID.
	 * @return Service descriptor object.
	 */
	ServiceDescriptor getService(String serviceId) {
		synchronized(lock) {
			if(!services.containsKey(serviceId)) return null;
			return services.get(serviceId);
		}
	}
	
	/**
	 * Gets manifest for a specified service.
	 * @param serviceId The service ID.
	 * @return Service manifest object.
	 */
	public ServiceManifest getManifest(String serviceId) {
		ServiceDescriptor descr = getService(serviceId);
		if(descr == null) return null;
		return descr.getManifest();
	}
	
	public boolean hasFullRights(String serviceId) {
		if(serviceId.equals(CoreManifest.ID)) return true;
		return false;
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
		ServiceDescriptor desc = getService(serviceId);
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
		ServiceDescriptor desc = getService(serviceId);
		if(!full) {
			SettingsManager setm = wta.getSettingsManager();
			fromVersion = new ServiceVersion(CoreUserSettings.getWhatsnewVersion(setm, profile, serviceId));
		}
		return desc.getWhatsnew(profile.getLocale(), fromVersion);
	}
	
	private void registerService(ServiceManifest manifest) {
		ServiceDescriptor descr = null;
		String serviceId = manifest.getId();
		String xid = manifest.getXId();
		boolean maintenance = false;
		
		logger.debug("Registering service [{}]", serviceId);
		synchronized(lock) {
			//TODO: check if xid is not duplicated
			if(services.containsKey(serviceId)) throw new WTRuntimeException("Service ID is already registered [{0}]", serviceId);	
			if(xidMappings.containsKey(xid)) throw new WTRuntimeException("Service XID (short ID) is already bound to a service [{0} -> {1}]", xid, xidMappings.get(xid));
			descr = new ServiceDescriptor(manifest);
			logger.debug("[default:{}, public:{}, deamon:{}]", descr.hasDefaultService(), descr.hasPublicService(), descr.hasDeamonService());

			boolean upgraded = upgradeCheck(manifest);
			descr.setUpgraded(upgraded);
			if(upgraded) {
				// Force whatsnew pre-cache
				descr.getWhatsnew(wta.getSystemLocale(), manifest.getOldVersion());
			}

			// If already in maintenance, keeps it active
			if(!isMaintenance(serviceId)) {
				// ...otherwise sets it!
				setMaintenance(serviceId, maintenance);
			}
			
			services.put(serviceId, descr);
			xidMappings.put(xid, serviceId);
			jsPathMappings.put(serviceId, manifest.getJsPath());
			
			// Adds service references into static map for facilitate ID lookup 
			Environment.addManifestMap(manifest.getClassName(), manifest);
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
				manifest = new ServiceManifest(
					elService.getString("id"),
					elService.getString("xid"),
					elService.getString("className"),
					elService.getString("jsClassName"),
					elService.getString("publicClassName"),
					elService.getString("deamonClassName"),
					elService.getBoolean("hidden", false),
					new ServiceVersion(elService.getString("version")),
					elService.getString("buildDate"),
					elService.getString("company"),
					elService.getString("companyEmail"),
					elService.getString("companyWebSite"),
					elService.getString("supportEmail")
				);
				manifests.add(manifest);
				
			} catch(Exception ex) {
				logger.warn("Service descriptor skipped. Cause: {}", ex.getMessage());
			}
		}
		return manifests;
	}
}
