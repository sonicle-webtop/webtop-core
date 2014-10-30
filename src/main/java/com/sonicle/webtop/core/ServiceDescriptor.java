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
import com.sonicle.webtop.core.sdk.PublicService;
import com.sonicle.webtop.core.sdk.Service;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.DeamonService;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.service.ResourceNotFoundException;
import com.sonicle.webtop.core.service.Whatsnew;
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
	
	private static final Logger logger = WebTopApp.getLogger(ServiceDescriptor.class);
	private ServiceManifest manifest = null;
	private Class defaultClass = null;
	private Class publicClass = null;
	private Class deamonClass = null;
	private boolean upgraded = false;
	private final HashMap<String, Whatsnew> whatsnewCache = new HashMap<>();

	public ServiceDescriptor(ServiceManifest manifest) {
		this.manifest = manifest;

		// Loads default (private) service class
		String className = manifest.getClassName();
		if(!StringUtils.isEmpty(className)) {
			defaultClass = loadServiceClass(className, Service.class, "Service");
		}

		// Loads public service class
		className = manifest.getPublicClassName();
		if(!StringUtils.isEmpty(className)) {
			publicClass = loadServiceClass(className, PublicService.class, "PublicService");
		}

		// Loads deamon service class
		className = manifest.getDeamonClassName();
		if(!StringUtils.isEmpty(className)) {
			deamonClass = loadServiceClass(className, DeamonService.class, "DeamonService");
		}
	}

	public ServiceManifest getManifest() {
		return manifest;
	}

	public boolean hasDefaultService() {
		return (defaultClass != null);
	}

	public Class getDefaultClass() {
		return defaultClass;
	}

	public boolean hasPublicService() {
		return (publicClass != null);
	}

	public Class getPublicClass() {
		return publicClass;
	}

	public boolean hasDeamonService() {
		return (deamonClass != null);
	}

	public Class getDeamonClass() {
		return deamonClass;
	}

	public boolean isUpgraded() {
		return upgraded;
	}

	public void setUpgraded(boolean upgraded) {
		this.upgraded = upgraded;
	}
	
	private Class loadServiceClass(String className, Class apiClass, String description) {
		Class clazz = null;
		
		try {
			clazz = Class.forName(className);
			if(!apiClass.isAssignableFrom(clazz)) throw new ClassCastException();
			return clazz;

		} catch(ClassNotFoundException ex) {
			logger.debug("{} class not found [{}]", description, className);
		} catch(ClassCastException ex) {
			logger.warn("A valid {} class must extends '{}' class", description, apiClass.toString());
		}
		return null;
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
				wn = new Whatsnew(resName);
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
