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

import com.sonicle.security.Principal;
import com.sonicle.webtop.core.api.Environment;
import com.sonicle.webtop.core.api.Service;
import com.sonicle.webtop.core.servlet.ServletHelper;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class WebTopSession {
	
	private static final Logger logger = WebTopApp.getLogger(WebTopSession.class);
	public static final String ATTRIBUTE = "webtopsession";
	private WebTopApp wta = null;
	private boolean initialized = false;
	private UserProfile profile = null;
	private ReadableUserAgent userAgentInfo = null;
	private final LinkedHashMap<String, Service> services = new LinkedHashMap<>();
	
	WebTopSession(HttpSession session) {
		wta = WebTopApp.get(session.getServletContext());
	}
	
	void destroy() {
		ServiceManager svcm = wta.getServiceManager();
		
		// Cleanup services
		synchronized(services) {
			for(Service instance : services.values()) {
				svcm.cleanupDefaultService(instance);
			}
			services.clear();
		}
	}
	
	/**
	 * Called from servlet package in order to init environment
	 * @param request 
	 */
	public synchronized void checkEnvironment(HttpServletRequest request) {
		if(!initialized) {
			initializeEnvironment(request);
		} else {
			logger.debug("Environment aready initialized");
		}
	}
	
	public void test() {
		logger.debug("TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
	}
	
	
	
	
	
	
	private void initializeEnvironment(HttpServletRequest request) {
		ServiceManager svcm = wta.getServiceManager();
		Principal principal = (Principal)SecurityUtils.getSubject().getPrincipal();
		
		logger.debug("Creating environment for {}", principal.getName());
		
		UserProfile up = new UserProfile(principal);
		ReadableUserAgent uai = wta.getUserAgentInfo(ServletHelper.getUserAgent(request));
		
		// Instantiates services
		Service instance = null;
		List<String> serviceIds = svcm.getServices();
		//TODO: order services list
		int count = 0;
		Environment e = null;
		for(String serviceId : serviceIds) {
			//TODO: check if service is allowed for user
			
			// Instantiate right Environment
			if(svcm.hasFullRights(serviceId)) {
				e = new CoreEnvironment(wta, this, profile, userAgentInfo); 
			} else {
				e = new Environment(wta, this, profile, userAgentInfo);
			}
			
			// Creates new instance
			instance = svcm.instantiateService(serviceId, e);
			if(instance != null) {
				addService(instance);
				count++;
			}
		}
		logger.debug("Instantiated {} services", count);
		profile = up;
		userAgentInfo = uai;
		initialized = true;
	}
	
	private void addService(Service service) {
		String serviceId = service.getManifest().getId();
		synchronized(services) {
			if(services.containsKey(serviceId)) throw new RuntimeException("Cannot add service twice");
			services.put(serviceId, service);
		}
	}
	
	public Service getServiceById(String serviceId) {
		synchronized(services) {
			if(!services.containsKey(serviceId)) throw new RuntimeException(MessageFormat.format("No service with ID: '{0}'", serviceId));
			return services.get(serviceId);
		}
	}
	
	/**
	 * Gets WebTopSession object stored as session's attribute.
	 * @param request The http request
	 * @return WebTopSession object
	 */
	public static WebTopSession get(HttpServletRequest request) {
		return get(request.getSession());
	}
	
	/**
	 * Gets WebTopSession object stored as session's attribute.
	 * @param session The http session
	 * @return WebTopSession object
	 */
	static WebTopSession get(HttpSession session) {
		return (WebTopSession)(session.getAttribute(ATTRIBUTE));
	}
}
