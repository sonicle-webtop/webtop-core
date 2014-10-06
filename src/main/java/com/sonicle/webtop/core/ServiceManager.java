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

import com.sonicle.webtop.core.api.ServiceManifest;
import com.sonicle.webtop.core.api.ServiceVersion;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
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
	private final LinkedHashMap<String, ServiceDescriptor> services = new LinkedHashMap();
	
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
	}
	
	private void init() {
		
		
		// Progamatically add the core's manifest
		initService(new Manifest());
		
		// Loads services' manifest files from classpath
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
		
		
	}
	
	private void initService(ServiceManifest manifest) {
		ServiceDescriptor descriptor = null;
		
		String serviceId = manifest.getId();
		synchronized(services) {
			if(services.containsKey(serviceId)) throw new RuntimeException("Service already registered");
			descriptor = new ServiceDescriptor(manifest);
			services.put(serviceId, descriptor);
		}
	}
	
	
	/**
	 * Returns the list of avaiable service IDs.
	 * @return 
	 */
	public List<String> getServices() {
		synchronized(services) {
			return Arrays.asList(services.keySet().toArray(new String[0]));
		}
	}
	
	public ServiceManifest getManifest(String serviceId) {
		synchronized(services) {
			if(!services.containsKey(serviceId)) return null;
			return services.get(serviceId).getManifest();
		}
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
		List<HierarchicalConfiguration> services = config.configurationsAt("service");
		for(HierarchicalConfiguration service : services) {
			try {
				manifest = new ServiceManifest(
					service.getString("id"),
					service.getString("className"),
					service.getString("publicClassName"),
					service.getString("deamonClassName"),
					new ServiceVersion(service.getString("version")),
					service.getString("buildDate"),
					service.getString("company"),
					service.getString("companyEmail"),
					service.getString("companyWebSite"),
					service.getString("supportEmail")
				);
				manifests.add(manifest);
				
			} catch(Exception ex) {
				logger.warn("Service descriptor skipped due to errors", ex);
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
