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

import ch.qos.logback.classic.LoggerContext;
import com.sonicle.commons.web.ContextUtils;
import com.sonicle.webtop.core.util.LoggerUtils;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class ContextLoader {
	private static final Logger logger = WT.getLogger(ContextLoader.class);
	public static final String WEBAPPNAME_ATTRIBUTE_KEY = "wtwebappname";
	public static final String WEBTOPAPP_ATTRIBUTE_KEY = "wtapp";
	
	public static String getWabappName(ServletContext servletContext) {
		return (String)servletContext.getAttribute(WEBAPPNAME_ATTRIBUTE_KEY);
	}
	
	protected void initApp(ServletContext servletContext) throws IllegalStateException {
		if (servletContext.getAttribute(WEBTOPAPP_ATTRIBUTE_KEY) != null) {
			throw new IllegalStateException("There is already a WebTop application associated with the current ServletContext.");
		}
		
		final String appname = ContextUtils.getWebappName(servletContext);
		servletContext.setAttribute(WEBAPPNAME_ATTRIBUTE_KEY, appname);
		
		try {
			LoggerUtils.initDC(appname);
			WebTopApp wta = new WebTopApp(servletContext);
			wta.init();
			servletContext.setAttribute(WEBTOPAPP_ATTRIBUTE_KEY, WebTopApp.getInstance());
			
		} catch(Throwable t) {
			servletContext.removeAttribute(WEBTOPAPP_ATTRIBUTE_KEY);
			logger.error("Error initializing WTA [{}]", appname, t);
		}
	}
	
	protected void destroyApp(ServletContext servletContext) {
		final String appname = getWabappName(servletContext);
		try {
			LoggerUtils.initDC(appname);
			WebTopApp wta = WebTopApp.get(servletContext);
			if (wta != null) wta.destroy();
			
		} catch(Throwable t) {
			logger.error("Error destroying WTA [{}]", appname, t);
		} finally {
			servletContext.removeAttribute(WEBTOPAPP_ATTRIBUTE_KEY);
		}
		
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		loggerContext.stop();
	}
}
