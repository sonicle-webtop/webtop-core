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
package com.sonicle.webtop.core.service;

import com.sonicle.webtop.core.ServiceManager;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.api.ServiceManifest;
import java.text.MessageFormat;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ServiceDescriptor {
	
	private static final Logger logger = WebTopApp.getLogger(ServiceManager.class);
	public static final String PRIVATE_CLASSNAME = "Service";
	public static final String PUBLIC_CLASSNAME = "PublicService";
	public static final String BACKGROUND_CLASSNAME = "BackgroundService";
	
	protected ServiceManifest manifest = null;
	protected String privateClassName = null;
	protected Class privateClass = null;
	protected String publicClassName = null;
	protected Class publicClass = null;
	protected String backgroundClassName = null;
	protected Class backgroundClass = null;

	public ServiceDescriptor(ServiceManifest manifest) {
		this.manifest = manifest;
		
		// Loads private(default) service class
		try {
			privateClassName = buildClassName(manifest.getId(), PRIVATE_CLASSNAME, manifest.getClassNamePrefix());
			privateClass = Class.forName(privateClassName);
		} catch(ClassNotFoundException ex) {
			logger.debug("Private service class not found [{}]", privateClassName);
		}
		
		// Loads public service class
		try {
			publicClassName = buildClassName(manifest.getId(), PUBLIC_CLASSNAME, manifest.getClassNamePrefix());
			publicClass = Class.forName(publicClassName);
		} catch(ClassNotFoundException ex) {
			logger.debug("Public service class not found [{}]", publicClassName);
		}
		
		// Loads background service class
		try {
			backgroundClassName = buildClassName(manifest.getId(), BACKGROUND_CLASSNAME, manifest.getClassNamePrefix());
			backgroundClass = Class.forName(backgroundClassName);
		} catch(ClassNotFoundException ex) {
			logger.debug("Background service class not found [{}]", backgroundClassName);
		}
	}

	private String buildClassName(String pkg, String classNamePrefix, String className) {
		return MessageFormat.format("{0}.{1}{2}", pkg, classNamePrefix, className);
	}

	public boolean hasPrivate() {
		return (privateClass == null);
	}

	public boolean hasPublic() {
		return (publicClass == null);
	}

	public boolean hasBackground() {
		return (backgroundClass == null);
	}
}
