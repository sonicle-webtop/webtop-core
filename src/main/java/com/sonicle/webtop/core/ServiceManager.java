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
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.Service;
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
import sun.security.jca.ServiceId;

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
	private final LinkedHashMap<String, ServiceDescriptor> services = new LinkedHashMap<>();
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
		jsPathMappings.clear();
	}
	
	private void init() {
		
		// Progamatically register the core's manifest
		registerService(new Manifest());
		
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
	
	public Service instantiateService(String serviceId, Environment environment) {
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
		instance.configure(descr.getManifest(), environment);
		
		// Calls initialization method
		try {
			WebTopApp.setServiceLoggerDC(serviceId);
			instance.initialize(environment);
		} catch(Exception ex) {
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
	
	public String getServiceIdByJsPath(String jsPath) {
		jsPath = StringUtils.removeStart(jsPath, "/");
		jsPath = StringUtils.removeEnd(jsPath, "/");
		return jsPathMappings.get(jsPath);
	}
	
	private void registerService(ServiceManifest manifest) {
		ServiceDescriptor descr = null;
		String serviceId = manifest.getId();
		boolean maintenance = false;
		
		logger.debug("Registering service [{}]", serviceId);
		synchronized(services) {
			if(services.containsKey(serviceId)) throw new RuntimeException("Service already registered");
			descr = new ServiceDescriptor(manifest);
			logger.debug("[default:{}, public:{}, deamon:{}]", descr.hasDefaultService(), descr.hasPublicService(), descr.hasDeamonService());

			boolean upgraded = upgradeCheck(manifest);
			descr.setUpgraded(upgraded);
			if(upgraded) {
				
			}

			// If already in maintenance, keeps it active
			if(!isMaintenance(serviceId)) {
				// ...otherwise sets it!
				setMaintenance(serviceId, maintenance);
			}
			
			services.put(serviceId, descr);
			jsPathMappings.put(manifest.getJsPath(), serviceId);
		}
	}
	
	/**
	 * Loads what's new file for specified service.
	 * Contents will be taken and interpreted starting from desired version number.
	 * 
	 * @param serviceId Service ID
	 * @param languageTag Language tag of contents (ex. it_IT)
	 * @param fromVersion Starting version
	 * @return HTML translated representation of loaded file.
	 */
	/*
	public synchronized String getWhatsnew(String serviceId, String languageTag, ServiceVersion fromVersion) {
		String resName = null;
		Whatsnew wn = null;
		
		try {
			Class clazz = getServiceClass(serviceId);
			String key = MessageFormat.format("{0}|{1}", serviceId, languageTag);
			if(whatsnewCache.containsKey(key)) {
				WebTopApp.logger.trace("Getting whatsnew from cache [{}, {}]", serviceId, key);
				wn = whatsnewCache.get(key);
			} else {
				resName = MessageFormat.format("/webtop/res/{0}/whatsnew/{1}.txt", serviceId, languageTag);
				WebTopApp.logger.debug("Loading whatsnew [{}, {}, ver. >= {}]", serviceId, resName, fromVersion);
				wn = new Whatsnew(clazz, resName);
				whatsnewCache.put(key, wn);
			}
			Version manifestVer = getManifest(serviceId).getVersion();
			return wn.toHtml(fromVersion, manifestVer);
			
		} catch(ResourceNotFoundException ex) {
			WebTopApp.logger.trace("Whatsnew file not available for service [{}]", serviceId);
		} catch(IOException ex) {
			WebTopApp.logger.trace(ex.getMessage());
		} catch(Exception ex) {
			WebTopApp.logger.error("Error getting whatsnew for service {}", serviceId, ex);
		}
		return StringUtils.EMPTY;
	}
	*/
	
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
	
	/**
	 * Returns registered services.
	 * @return List of service IDs.
	 */
	public List<String> getRegisteredServices() {
		synchronized(services) {
			return Arrays.asList(services.keySet().toArray(new String[services.size()]));
		}
	}
	
	public List<String> getServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(services) {
			for(ServiceDescriptor descr : services.values()) {
				if(descr.hasDefaultService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	public List<String> getDeamonServices() {
		ArrayList<String> list = new ArrayList<>();
		synchronized(services) {
			for(ServiceDescriptor descr : services.values()) {
				if(descr.hasDeamonService()) list.add(descr.getManifest().getId());
			}
		}
		return list;
	}
	
	ServiceDescriptor getService(String serviceId) {
		synchronized(services) {
			if(!services.containsKey(serviceId)) return null;
			return services.get(serviceId);
		}
	}
	
	public ServiceManifest getManifest(String serviceId) {
		ServiceDescriptor descr = getService(serviceId);
		if(descr == null) return null;
		return descr.getManifest();
	}
	
	public boolean hasFullRights(String serviceId) {
		if(serviceId.equals(Manifest.ID)) return true;
		return false;
	}
	
	private ArrayList<ServiceManifest> discoverServices() throws IOException {
		ClassLoader cl = findClassLoader();
		
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
	
	private ClassLoader findClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null) cl = getClass().getClassLoader();
		return cl;
	}
}
