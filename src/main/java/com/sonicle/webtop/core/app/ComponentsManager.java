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

import com.sonicle.webtop.core.app.provider.RecipientsProviderBase;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.sdk.interfaces.IConnectionProvider;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingReader;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.LinkedHashMap;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ComponentsManager {
	private static final Logger logger = WT.getLogger(ServiceManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized ComponentsManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		ComponentsManager comm = new ComponentsManager(wta);
		initialized = true;
		logger.info("ComponentsManager initialized");
		return comm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private ComponentsManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	public void cleanup() {
		wta = null;
	}
	
	private final HashSet<String> registeredClasses = new HashSet<>();
	private final LinkedHashMap<String, RecipientsProviderBase> recipientsProviders = new LinkedHashMap<>();
	
	public static boolean canBeRegistered(final Class<?> clazz) {
		if(isAssignableTo(clazz, RecipientsProviderBase.class)) return true;
		return false;
	}
	
	private static boolean isAssignableTo(Class clazz, Class baseClass) {
		if(clazz == null) return false;
		return baseClass.isAssignableFrom(clazz);
	}
	
	public void register(ServiceContext context, Class clazz) {
		String className = clazz.getCanonicalName();
		
		if(isAssignableTo(clazz, RecipientsProviderBase.class)) {
			synchronized(recipientsProviders) {
				if(recipientsProviders.containsKey(context.getServiceId())) throw new WTRuntimeException("RecipientsProvider already registered for service [{0}]", context.getServiceId());
				recipientsProviders.put(context.getServiceId(), instantiateRecipientsProvider(clazz));
			}
		} else {
			throw new WTRuntimeException("Class cannot be registered [{0}]", className);
		}
	}
	
	public RecipientsProviderBase getRecipientsProvider(String serviceId) {
		synchronized(registeredClasses) {
			return recipientsProviders.get(serviceId);
		}
	}
	
	/*
	public List<RecipientsProviderBase> getRecipientsProviders() {
		synchronized(registeredClasses) {
			return new ArrayList<>(recipientsProviders.values());
		}
	}
	*/
	
	private RecipientsProviderBase instantiateRecipientsProvider(Class clazz) {
		try {
			Constructor<RecipientsProviderBase> constructor = clazz.getConstructor(IConnectionProvider.class, IServiceSettingReader.class);
			return constructor.newInstance(wta.createCoreServiceContext(), wta.getConnectionManager(), wta.getSettingsManager());
		} catch(Exception ex) {
			logger.error("Error instantiating RecipientsProvider [{}]", clazz.getCanonicalName(), ex);
			throw new WTRuntimeException("Error instantiating RecipientsProvider [{0}]", clazz.getCanonicalName());
		}
	}
}
