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
import com.sonicle.webtop.core.sdk.BaseRestApi;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.versioning.ResourceNotFoundException;
import com.sonicle.webtop.core.versioning.Whatsnew;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
class ServiceDescriptor {
	private static final Logger logger = WT.getLogger(ServiceDescriptor.class);
	private ServiceManifest manifest = null;
	private Class controllerClass = null;
	private Class managerClass = null;
	private Class restApiClass = null;
	private Class privateServiceClass = null;
	private Class publicServiceClass = null;
	private Class jobServiceClass = null;
	private Class userOptionsServiceClass = null;
	private boolean upgraded = false;
	private final HashMap<String, Whatsnew> whatsnewCache = new HashMap<>();

	public ServiceDescriptor(ServiceManifest manifest) {
		this.manifest = manifest;
		
		// Loads controller class
		String className = manifest.getControllerClassName();
		if(!StringUtils.isBlank(className)) {
			controllerClass = loadClass(className, BaseController.class, "Controller");
		}
		// Loads manager class
		className = manifest.getManagerClassName();
		if(!StringUtils.isBlank(className)) {
			managerClass = loadClass(className, BaseManager.class, "Manager");
		}
		// Loads api service class
		className = manifest.getRestApiClassName();
		if(!StringUtils.isBlank(className)) {
			restApiClass = loadClass(className, BaseRestApi.class, "RestApi");
		}
		// Loads (private) service class
		className = manifest.getPrivateServiceClassName();
		if(!StringUtils.isBlank(className)) {
			privateServiceClass = loadClass(className, BaseService.class, "Service");
		}
		// Loads public service class
		className = manifest.getPublicServiceClassName();
		if(!StringUtils.isBlank(className)) {
			publicServiceClass = loadClass(className, BasePublicService.class, "PublicService");
		}
		// Loads job service class
		className = manifest.getJobServiceClassName();
		if(!StringUtils.isBlank(className)) {
			jobServiceClass = loadClass(className, BaseJobService.class, "JobService");
		}
		// Loads userOptions service class
		className = manifest.getUserOptionsServiceClassName();
		if(!StringUtils.isBlank(className)) {
			userOptionsServiceClass = loadClass(className, BaseUserOptionsService.class, "UserOptionsService");
		}
	}

	public ServiceManifest getManifest() {
		return manifest;
	}
	
	public Class getControllerClass() {
		return controllerClass;
	}
	
	public boolean doesControllerImplements(Class clazz) {
		return implementsInterface(controllerClass, clazz);
	}
	
	public boolean hasManager() {
		return (managerClass != null);
	}

	public Class getManagerClass() {
		return managerClass;
	}
	
	public boolean hasRestApi() {
		return (restApiClass != null);
	}

	public Class getRestApiClass() {
		return restApiClass;
	}

	public boolean hasPrivateService() {
		return (privateServiceClass != null);
	}

	public Class getPrivateServiceClass() {
		return privateServiceClass;
	}
	
	public boolean hasUserOptionsService() {
		return (userOptionsServiceClass != null);
	}

	public Class getUserOptionsServiceClass() {
		return userOptionsServiceClass;
	}

	public boolean hasPublicService() {
		return (publicServiceClass != null);
	}

	public Class getPublicServiceClass() {
		return publicServiceClass;
	}

	public boolean hasJobService() {
		return (jobServiceClass != null);
	}

	public Class getJobServiceClass() {
		return jobServiceClass;
	}

	public boolean isUpgraded() {
		return upgraded;
	}

	public void setUpgraded(boolean upgraded) {
		this.upgraded = upgraded;
	}
	
	private Class loadClass(String className, Class apiClass, String description) {
		Class clazz = null;
		
		try {
			clazz = Class.forName(className);
			if(!apiClass.isAssignableFrom(clazz)) throw new ClassCastException();
			return clazz;

		} catch(ClassNotFoundException ex) {
			logger.debug("{} class not found [{}]", description, className);
		} catch(ClassCastException ex) {
			logger.warn("A valid {} class must extends '{}' class", description, apiClass.toString());
		} catch(Throwable t) {
			logger.error("Unable to load class [{}]", className, t);
		}
		return null;
	}
	
	private boolean implementsInterface(Class clazz, Class interfaceClass) {
		if(clazz == null) return false;
		if(interfaceClass == null) return false;
		return interfaceClass.isAssignableFrom(clazz);
	}
	
	/**
	 * Loads what's new file for specified service.
	 * Contents will be taken and interpreted starting from desired version number.
	 * @param serviceId Service ID
	 * @param locale Locale of contents (ex. it_IT)
	 * @param fromVersion Starting version
	 * @return HTML translated representation of loaded file.
	 */
	public synchronized String getWhatsnew(Locale locale, ServiceVersion fromVersion) {
		String resName = null;
		Whatsnew wn = null;
		
		try {
			String slocale = locale.toString();
			if(whatsnewCache.containsKey(slocale)) {
				logger.trace("Getting whatsnew from cache [{}, {}]", manifest.getId(), slocale);
				wn = whatsnewCache.get(slocale);
			} else {
				resName = MessageFormat.format("/{0}/whatsnew/{1}.txt", LangUtils.packageToPath(manifest.getId()), slocale);
				logger.debug("Loading whatsnew [{}, {}, ver. >= {}]", manifest.getId(), resName, fromVersion);
				wn = new Whatsnew(resName, defineWhatsnewVariables());
				whatsnewCache.put(slocale, wn);
			}
			return wn.toHtml(fromVersion, manifest.getVersion());
			
		} catch(ResourceNotFoundException ex) {
			logger.trace("Whatsnew file not available for service [{}]", manifest.getId());
		} catch(IOException ex) {
			logger.trace(ex.getMessage());
		} catch(Exception ex) {
			logger.error("Error getting whatsnew for service {}", manifest.getId(), ex);
		}
		return StringUtils.EMPTY;
	}
	
	private HashMap<String, String> defineWhatsnewVariables() {
		HashMap<String, String> variables = new HashMap<>();
		// Defines variables to place substitutions in whatsnew html
		variables.put("WHATSNEW_URL", MessageFormat.format("resources/{0}/whatsnew", manifest.getJarPath()));
		return variables;
	}
}
