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
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class BaseRequestServlet extends HttpServlet {
	
	protected String[] splitPath(String pathInfo) throws MalformedURLException {
		String[] tokens = StringUtils.split(pathInfo, "/", 2);
		if(tokens.length != 2) throw new MalformedURLException("URL does not esplicitate service ID");
		return tokens;
	}
	
	protected void invokeMethod(Object instance, Method method, String service, boolean nowriter, HttpServletRequest request, HttpServletResponse response) throws Exception {
		PrintWriter out = null;
		try {
			try {
				WebTopApp.setServiceLoggerDC(service);
				if(nowriter) {
					method.invoke(instance, request, response);
				} else {
					out = response.getWriter();
					ServletUtils.setCacheControlHeaderPrivateNoCache(response);
					ServletUtils.setJsonContentTypeHeader(response);
					method.invoke(instance, request, response, out);
				}
			} finally {
				if(out != null) out.flush();
				WebTopApp.unsetServiceLoggerDC();
			}
		} catch(Exception ex) {
			throw new Exception("Error during method invocation", ex);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	protected Method getMethod(Class clazz, String service, String action, boolean nowriter) throws WTException {
		String methodName = null;
		if(nowriter) {
			methodName = MessageFormat.format("process{0}", action);
			try {
				return clazz.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
			} catch(NoSuchMethodException ex) {
				throw new WTException("Service {0} has no action with name {1} [{2}(request,response) not found in {3}]", service, action, methodName, clazz.getName());
			}
		} else {
			action = StringUtils.isEmpty(action) ? "DefaultAction" : action;
			methodName = MessageFormat.format("process{0}", action);
			try {
				return clazz.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class, PrintWriter.class);
			} catch(NoSuchMethodException ex) {
				throw new WTException("Service {0} has no action with name {1} [{2}(request,response,out) not found in {3}]", service, action, methodName, clazz.getName());
			}
		}
	}
}
