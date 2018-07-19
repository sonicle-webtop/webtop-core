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

import com.sonicle.webtop.core.app.util.ClassHelper;
import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.sdk.BaseRestApiEndpoint;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.BasePublicService;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.BaseJobService;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.versioning.ResourceNotFoundException;
import com.sonicle.webtop.core.versioning.SqlUpgradeScript;
import com.sonicle.webtop.core.versioning.Whatsnew;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
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
	private Class privateServiceClass = null;
	private Class publicServiceClass = null;
	private Class jobServiceClass = null;
	private Class userOptionsServiceClass = null;
	private final List<ApiEndpointClass> restApiEndpointClasses = new ArrayList<>();
	private final List<OpenApiDefinition> openApiDefinitions = new ArrayList<>();
	private boolean upgraded = false;
	private final HashMap<String, Whatsnew> whatsnewCache = new HashMap<>();

	public ServiceDescriptor(ServiceManifest manifest) {
		this.manifest = manifest;
		
		controllerClass = ClassHelper.loadClass(true, manifest.getControllerClassName(), BaseController.class, "Controller");
		managerClass = ClassHelper.loadClass(true, manifest.getManagerClassName(), BaseManager.class, "Manager");
		privateServiceClass = ClassHelper.loadClass(true, manifest.getPrivateServiceClassName(), BaseService.class, "Service");
		publicServiceClass = ClassHelper.loadClass(true, manifest.getPublicServiceClassName(), BasePublicService.class, "PublicService");
		jobServiceClass = ClassHelper.loadClass(true, manifest.getJobServiceClassName(), BaseJobService.class, "JobService");
		userOptionsServiceClass = ClassHelper.loadClass(true, manifest.getUserOptionsServiceClassName(), BaseUserOptionsService.class, "UserOptionsService");
		
		for (ServiceManifest.RestApiEndpoint rae : manifest.getApiEndpoints()) {
			final Class clazz = ClassHelper.loadClass(false, rae.className, BaseRestApiEndpoint.class, "RestApiEndpoint");
			if (clazz != null) {
				restApiEndpointClasses.add(new ApiEndpointClass(clazz, sanitizeApiEndpointPath(manifest.getId(), rae.path)));
			}
		}
		for (ServiceManifest.RestApi ra : manifest.getRestApis()) {
			Set<Class<?>> classes = findSubTypesClassesOf(ra.implPackage, com.sonicle.webtop.core.sdk.BaseRestApiResource.class);
			openApiDefinitions.add(new OpenApiDefinition(ra.oasFilePath, ra.context, ra.implPackage, classes));
		}
	}

	public ServiceManifest getManifest() {
		return manifest;
	}
	
	public boolean hasController() {
		return (controllerClass != null);
	}
	
	public Class getControllerClass() {
		return controllerClass;
	}
	
	public boolean doesControllerImplements(Class clazz) {
		if (!hasController()) return false;
		return ClassHelper.isImplementingInterface(controllerClass, clazz);
	}
	
	public boolean hasManager() {
		return (managerClass != null);
	}

	public Class getManagerClass() {
		return managerClass;
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
	
	public boolean hasRestApiEndpoints() {
		return !restApiEndpointClasses.isEmpty();
	}
	
	public List<ApiEndpointClass> getRestApiEndpoints() {
		return restApiEndpointClasses;
	}
	
	public boolean hasOpenApiDefinitions() {
		return !openApiDefinitions.isEmpty();
	}
	
	public List<OpenApiDefinition> getOpenApiDefinitions() {
		return openApiDefinitions;
	}

	public boolean isUpgraded() {
		return upgraded;
	}

	public void setUpgraded(boolean upgraded) {
		this.upgraded = upgraded;
	}
	
	
	
	/**
	 * Loads what's new file for specified service.
	 * Contents will be taken and interpreted starting from desired version number.
	 * @param serviceId Service ID
	 * @param locale Locale of contents (ex. it_IT)
	 * @param fromVersion Starting version
	 * @return HTML translated representation of loaded file.
	 */
	public String getWhatsnew(Locale locale, ServiceVersion fromVersion) {
		String resName = null;
		Whatsnew wn = null;
		
		synchronized(whatsnewCache) {
			try {
				String slocale = locale.toString();
				if(whatsnewCache.containsKey(slocale)) {
					logger.trace("Getting whatsnew from cache [{}, {}]", manifest.getId(), slocale);
					wn = whatsnewCache.get(slocale);
				} else {
					resName = MessageFormat.format("/{0}/meta/whatsnew/{1}.txt", LangUtils.packageToPath(manifest.getId()), slocale);
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
	}
	
	public ArrayList<SqlUpgradeScript> getUpgradeScripts() {
		ArrayList<SqlUpgradeScript> scripts = new ArrayList<>();
		String resName = null;
		
		try {
			String pkgPath = MessageFormat.format("{0}/meta/db/", LangUtils.packageToPath(manifest.getId()));
			ServiceVersion fileVersion = null;
			// List all .sql files in dedicated package and defines the right set
			for(String file: LangUtils.listPackageFiles(getClass(), pkgPath)) {
				try {
					if (StringUtils.startsWithIgnoreCase(file, "init")) continue;
					fileVersion = SqlUpgradeScript.extractVersion(file);
					if (fileVersion.compareTo(manifest.getOldVersion()) <= 0) continue; // Skip all version sections below oldVersion (included)
					if (fileVersion.compareTo(manifest.getVersion()) > 0) continue; // Skip all version sections after manifestVersion
					
					resName = "/" + pkgPath + file;
					logger.debug("Reading upgrade script [{}]", resName);
					scripts.add(new SqlUpgradeScript(resName));
					
				} catch(IOException | UnsupportedOperationException ex1) {
					logger.warn(ex1.getMessage());
					//TODO: increment error counter...
				}
			}
			
			// Filename ordering may not be valid, so reorder the set
			// using the version information
			Collections.sort(scripts, new Comparator<SqlUpgradeScript>() {
				@Override
				public int compare(SqlUpgradeScript o1, SqlUpgradeScript o2) {
					return o1.getFileVersion().compareTo(o2.getFileVersion());
				}
			});
			
		} catch(Exception ex) {
			logger.error("Error loading upgrade scripts", ex);
		}
		//TODO: return also error counter...
		return scripts;
	}
	
	public Info buildOpenApiInfo(OpenApiDefinition apiDefinition) {
		String title = WT.lookupResource(manifest.getId(), WT.LOCALE_ENGLISH, BaseService.RESOURCE_SERVICE_NAME);
		Contact contact = null;
		if (!StringUtils.isBlank(manifest.getSupportEmail())) {
			new Contact().email(manifest.getSupportEmail());
		}
		
		return new Info()
				.title(title + " Rest API")
				.version("v1")
				.contact(contact);
	}
	
	private String sanitizeApiEndpointPath(String serviceId, String path) {
		if (StringUtils.isBlank(path)) {
			return serviceId;
		} else {
			return serviceId + "/" + StringUtils.removeStart(path, "/");
		}
	}
	
	private HashMap<String, String> defineWhatsnewVariables() {
		HashMap<String, String> variables = new HashMap<>();
		// Defines variables to place substitutions in whatsnew html
		variables.put("WHATSNEW_URL", MessageFormat.format("resources/{0}/whatsnew", manifest.getJarPath()));
		return variables;
	}
	
	private Set<Class<?>> findSubTypesClassesOf(String targetPackage, Class subTypeOf) {
		Set<Class<?>> output = new HashSet<>();
		
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage(targetPackage))
				.setScanners(new SubTypesScanner())
				.filterInputsBy(new FilterBuilder().includePackage(targetPackage)));
		
		Set<Class<?>> classes = reflections.getSubTypesOf(subTypeOf);
		for (Class<?> clazz : classes) {
			if (clazz.getPackage().getName().startsWith(targetPackage)) {
				output.add(clazz);
			}
		}
		return output;
	}
	
	
	public static class ApiEndpointClass {
		public final Class clazz;
		public final String path;
		
		public ApiEndpointClass(Class clazz, String path) {
			this.clazz = clazz;
			this.path = path;
		}
	}
	
	public static class OpenApiDefinition {
		public final String oasFile;
		public final String context;
		public final String implPackage;
		public final Set<Class<?>> resourceClasses;
		
		public OpenApiDefinition(String oasFile, String context, String implPackage, Set<Class<?>> resourceClasses) {
			this.oasFile = oasFile;
			this.context = context;
			this.implPackage = implPackage;
			this.resourceClasses = resourceClasses;
		}
	}
}
