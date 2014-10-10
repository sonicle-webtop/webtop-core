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

import com.sonicle.commons.web.servlet.ServletUtils;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.WebTopSession;
import com.sonicle.webtop.core.sdk.Service;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author malbinola
 */
public class ServiceRequest extends HttpServlet {
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopSession wts = WebTopSession.get(request);
		
		try {
			WebTopApp.logger.trace("Servlet: ServiceRequest [{}]", ServletHelper.getSessionID(request));
			String service = ServletUtils.getStringParameter(request, "service", true);
			String action = ServletUtils.getStringParameter(request, "action", true);
			Boolean nowriter = ServletUtils.getBooleanParameter(request, "nowriter", false);
			
			// Retrieves instantiated service
			Service instance = wts.getServiceById(service);
			
			// Gets right method
			Method method = null;
			String methodName = MessageFormat.format("process{0}", action);
			if(nowriter) {
				try {
					method = instance.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
				} catch(NoSuchMethodException ex) {
					throw new Exception(MessageFormat.format("Service '{0}' has no action with name '{1}' [{2}(request,response) not found in {3}]", service, action, methodName, instance.getManifest().getClassName()));
				}
			} else {
				try {
					method = instance.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class, PrintWriter.class);
				} catch(NoSuchMethodException ex) {
					throw new Exception(MessageFormat.format("Service '{0}' has no action with name '{1}' [{2}(request,response,out) not found in {3}]", service, action, methodName, instance.getManifest().getClassName()));
				}
			}
			
			// Invoking method...
			PrintWriter out = null;
			try {
				try {
					WebTopApp.setServiceLoggerDC(service);
					if(nowriter) {
						method.invoke(instance, request, response);
					} else {
						out = response.getWriter();
						method.invoke(instance, request, response, out);
						ServletHelper.setCacheControl(response);
						ServletHelper.setPageContentType(response);
					}
				} finally {
					WebTopApp.unsetServiceLoggerDC();
				}
			} catch(Exception ex) {
				throw new Exception("Error durin method invocation", ex);
			} finally {
				IOUtils.closeQuietly(out);
			}
			
		} catch(Exception ex) {
			WebTopApp.logger.warn("Error in serviceRequest servlet", ex);
			throw new ServletException(ex.getMessage());
		} finally {
			WebTopApp.clearLoggerDC();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
}
