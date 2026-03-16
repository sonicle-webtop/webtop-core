/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.WebTopApp;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class HealthCheck extends AbstractServlet {
	public static final String URL = "/healthcheck"; // Shiro.ini must reflect this URI!
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(HealthCheck.class);
	private boolean httpStatusIndicator = true;
	
	/*
	public boolean getHttpStatusIndicator() {
		return this.httpStatusIndicator;
	}

	public void setHttpStatusIndicator(boolean httpStatusIndicator) {
		this.httpStatusIndicator = httpStatusIndicator;
	}
	*/

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletUtils.sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	@Override
	protected void processGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = getWebTopApp(request);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] processGetOrPost", ServletUtils.getRequestID(request));
		
		try {
			boolean httpStatus = ServletUtils.getBooleanParameter(request, "httpStatusIndicator", this.httpStatusIndicator);
			//boolean prettyPrint = ServletUtils.getBooleanParameter(httpRequest, "pretty", false);
			
			if (wta == null) {
				ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				
			} else {
				final WebTopApp.AppState state = wta.getState();
				final boolean maintenance = wta.isInMaintenance();
				int responseStatus = HttpServletResponse.SC_OK;
				
				if (maintenance) {
					if (httpStatus) responseStatus = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
					//response.setHeader("Retry-After", "120");
				} else if (WebTopApp.AppState.STARTING.equals(state)) {
					//response.setHeader("Retry-After", "120")
					if (httpStatus) responseStatus = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
				} else if (WebTopApp.AppState.READY.equals(state)) {
					if (httpStatus) responseStatus = HttpServletResponse.SC_OK;
				} else {
					if (httpStatus) responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				ServletUtils.setCachingNotAllowed(response);
				ServletUtils.setStatus(response, responseStatus);
				ServletUtils.writeJsonResponse(response, new Payload(state, maintenance));
			}	
			
		} catch (Exception ex) {
			LOGGER.error("[{}] Error processGetOrPost", ServletUtils.getRequestID(request), ex);
			if (!(ex instanceof ServletException) && !(ex instanceof IOException)) {
				throw new ServletException(ex);
			}
		}
	}
	
	public static class Payload {
		public final String status;
		public final boolean maintenance;
		
		public Payload(WebTopApp.AppState state, boolean maintenance) {
			this.status = EnumUtils.getName(state);
			this.maintenance = maintenance;
		}
	}
}
