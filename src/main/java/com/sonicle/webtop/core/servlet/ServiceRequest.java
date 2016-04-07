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
package com.sonicle.webtop.core.servlet;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.bol.js.JsUserOptionsBase;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ServiceRequest extends BaseServiceRequest {
	private static final Logger logger = WT.getLogger(ServiceRequest.class);
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		WebTopSession wts = WebTopSession.get(request);
		
		try {
			logger.trace("Servlet: ServiceRequest [{}]", wts.getId());
			String csrf = ServletUtils.getStringParameter(request, "csrf", null);
			String service = ServletUtils.getStringParameter(request, "service", true);
			String action = ServletUtils.getStringParameter(request, "action", true);
			Boolean nowriter = ServletUtils.getBooleanParameter(request, "nowriter", false);
			Boolean options = ServletUtils.getBooleanParameter(request, "options", false);
			
			if(!wta.getSessionManager().checkSecurityToken(wts, csrf)) {
				throw new Exception("Unable to authenticate current request. Provided security token is not valid.");
			}
			
			if(!options) {
				// Retrieves instantiated service
				BaseService instance = wts.getServiceById(service);
				
				// Gets method and invokes it...
				Method method = getMethod(instance.getClass(), service, action, nowriter);
				invokeMethod(instance, method, service, nowriter, request, response);
				
			} else {
				ServiceManager svcm = wta.getServiceManager();
				String id = ServletUtils.getStringParameter(request, "id", null);
				String payload = null;
				
				if(StringUtils.isEmpty(id)) {
					payload = ServletUtils.getPayload(request);
					Payload<MapItem, JsUserOptionsBase> pl = ServletUtils.getPayload(payload, JsUserOptionsBase.class);
					if(pl.map.has("id")) id = pl.data.id;
				}
				if(StringUtils.isEmpty(id)) throw new Exception("No id specified");
				
				// Retrieves instantiated userOptions service (session context away)
				UserProfile.Id pid = new UserProfile.Id(id);
				BaseUserOptionsService instance = svcm.instantiateUserOptionsService(wts.getUserProfile(), wts.getId(), service, pid);
				
				// Gets method and invokes it...
				Method method = getMethod(instance.getClass(), service, action, nowriter, String.class);
				invokeMethod(instance, method, service, nowriter, request, response, payload);
			}
			
		} catch(Exception ex) {
			logger.warn("Error in serviceRequest servlet", ex);
			throw new ServletException(ex.getMessage());
		} finally {
			WebTopApp.clearLoggerDC();
		}
	}
	
	/*
	private void invokeMethod(Object instance, Method method, String service, boolean nowriter, HttpServletRequest request, HttpServletResponse response) throws Exception {
		PrintWriter out = null;
		try {
			try {
				WebTopApp.setServiceLoggerDC(service);
				if(nowriter) {
					method.invoke(instance, request, response);
				} else {
					out = response.getWriter();
					response.setHeader("Cache-Control", "private");
					response.setContentType("text/html;charset=UTF-8");
					method.invoke(instance, request, response, out);
				}
			} finally {
				WebTopApp.unsetServiceLoggerDC();
			}
		} catch(Exception ex) {
			throw new Exception("Error during method invocation", ex);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	*/
	
	/*
	private Method getMethod(Class clazz, String service, String action, boolean nowriter) throws WTException {
		String methodName = MessageFormat.format("process{0}", action);
		if(nowriter) {
			try {
				return clazz.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
			} catch(NoSuchMethodException ex) {
				throw new WTException("Service {0} has no action with name {1} [{2}(request,response) not found in {3}]", service, action, methodName, clazz.getName());
			}
		} else {
			try {
				return clazz.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class, PrintWriter.class);
			} catch(NoSuchMethodException ex) {
				throw new WTException("Service {0} has no action with name {1} [{2}(request,response,out) not found in {3}]", service, action, methodName, clazz.getName());
			}
		}
	}
	*/

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
}
