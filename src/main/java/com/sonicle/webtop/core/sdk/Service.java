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
package com.sonicle.webtop.core.sdk;

import com.sonicle.webtop.core.WebTopApp;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public abstract class Service {
	
	public static final String RESOURCE_SERVICE_NAME = "service.name";
	public static final String RESOURCE_SERVICE_DESCRIPTION = "service.name";
	private boolean configured = false;
	private ServiceManifest manifest;
	private Environment env;
	
	public abstract void initialize(Environment api);
	public abstract void cleanup();
	
	public final void configure(ServiceManifest manifest, Environment env) {
		if(configured) return;
		configured = true;
		this.manifest = manifest;
		this.env = env;
	}
	
	public ServiceManifest getManifest() {
		return manifest;
	}
	
	public Environment getEnv() {
		return env;
	}
	
	public AdvancedEnvironment getAdvancedEnv() {
		return (AdvancedEnvironment)env;
	}
	
	public String getName(Locale locale) {
		return env.lookupResource(manifest.getId(), locale, RESOURCE_SERVICE_NAME);
	}
	
	public String getDescription(Locale locale) {
		return env.lookupResource(manifest.getId(), locale, RESOURCE_SERVICE_DESCRIPTION);
	}
	
	/**
	 * Returns a valid logger instance properly configured by WebTop 
	 * environment. Logger name is computed starting from specified class name.
	 * @param clazz A class.
	 * @return A logger instance.
	 */
	public static Logger getLogger(Class clazz) {
		return (Logger) LoggerFactory.getLogger(clazz);
	}
	
	/**
	 * (logger) Apply a custom diagnostic context (DC) to the default one.
	 * Passed value is associated to the key 'custom' of current DC.
	 * @param diagnosticContext Custom diagnostic context string value to append.
	 */
	public static void applyLoggerDC(String diagnosticContext) {
		WebTopApp.setServiceCustomLoggerDC(diagnosticContext);
	}
	
	/**
	 * (logger) Removes custom diagnostic context restoring the default one.
	 * Same behaviour calling: applyLoggerDC(null)
	 */
	public static void clearLoggerDC() {
		WebTopApp.unsetServiceCustomLoggerDC();
	}
}
