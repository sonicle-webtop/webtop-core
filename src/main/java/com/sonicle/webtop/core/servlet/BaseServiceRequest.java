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
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.LoggerUtils;
import java.util.Arrays;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class BaseServiceRequest extends AbstractServlet {
	
	protected String[] splitPath(String pathInfo) throws MalformedURLException {
		String[] tokens = StringUtils.split(pathInfo, "/", 2);
		if(tokens.length != 2) throw new MalformedURLException("URL does not esplicitate service ID");
		return tokens;
	}
	
	protected MethodInfo getMethod(Class clazz, String service, String action, boolean nowriter, Class<?>... args) throws WTException {
		String methodName = null;
		ArrayList<Class<?>> classArgs = new ArrayList<>();
		classArgs.add(HttpServletRequest.class);
		classArgs.add(HttpServletResponse.class);
		
		if(nowriter) {
			methodName = MessageFormat.format("process{0}", action);
		} else {
			if(StringUtils.isEmpty(action)) {
				action = "DefaultAction";
				nowriter = true;
			}
			//action = StringUtils.isEmpty(action) ? "DefaultAction" : action;
			methodName = MessageFormat.format("process{0}", action);
			if(!nowriter) classArgs.add(PrintWriter.class);
		}
		if(args.length > 0) classArgs.addAll(Arrays.asList(args));
		
		try {
			return new MethodInfo(clazz.getMethod(methodName, classArgs.toArray(new Class<?>[classArgs.size()])), nowriter);
			//return clazz.getMethod(methodName, classArgs.toArray(new Class<?>[classArgs.size()]));
		} catch(NoSuchMethodException ex) {
			if(nowriter) {
				throw new WTException("Service {0} has no action with name {1} [{2}(request,response,...) not found in {3}]", service, action, methodName, clazz.getName());
			} else {
				throw new WTException("Service {0} has no action with name {1} [{2}(request,response,out,...) not found in {3}]", service, action, methodName, clazz.getName());
			}
		}
	}
	
	protected void invokeMethod(Object instance, MethodInfo methodInfo, String service, HttpServletRequest request, HttpServletResponse response, Object... args) throws Exception {
		PrintWriter out = null;
		try {
			try {
				ArrayList<Object> invokeArgs = new ArrayList<>();
				invokeArgs.add(request);
				invokeArgs.add(response);
				if (!methodInfo.nowriter) {
					ServletUtils.setJsonContentType(response);
					ServletUtils.setCacheControlPrivateNoCache(response);
					out = response.getWriter();
					invokeArgs.add(out);
				}
				if (args.length > 0) invokeArgs.addAll(Arrays.asList(args));
				LoggerUtils.setContextDC(service);
				methodInfo.method.invoke(instance, invokeArgs.toArray());
				
			} finally {
				if(out != null) out.flush();
				LoggerUtils.clearContextServiceDC();
			}
		} catch(Exception ex) {
			throw new Exception("Error during method invocation", ex);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	/*
	protected void invokeMethod22(Object instance, MethodInfo methodInfo, String service, HttpServletRequest request, HttpServletResponse response, Object... args) throws Exception {
		try {
			HttpServletResponseWrapper wrappedResponse = null;
			
			try {
				ArrayList<Object> invokeArgs = new ArrayList<>();
				invokeArgs.add(request);
				if (!methodInfo.nowriter) {
					ServletUtils.setCacheControlPrivateNoCache(response);
					ServletUtils.setJsonContentType(response);
					if (ServletUtils.acceptsDeflate(request)) {
						wrappedResponse = new GZipResponseWrapper(response);
					} else {
						wrappedResponse = new WriterResponseWrapper(response);
					}
					invokeArgs.add(wrappedResponse);
					invokeArgs.add(wrappedResponse.getWriter());
					
				} else {
					invokeArgs.add(response);
				}
				
				if (args.length > 0) invokeArgs.addAll(Arrays.asList(args));
				LoggerUtils.setContextDC(service);
				methodInfo.method.invoke(instance, invokeArgs.toArray());
				
			} finally {
				if (wrappedResponse != null) ((ClosableServletResponse)wrappedResponse).close();
			}
		} catch(Throwable t) {
			throw new Exception("Error during method invocation", t);
		}
	}
	*/
	
	public static class MethodInfo {
		public Method method;
		public boolean nowriter;
		
		public MethodInfo() {};
		
		public MethodInfo(Method method, boolean nowriter) {
			this.method = method;
			this.nowriter = nowriter;
		}
	}
}
