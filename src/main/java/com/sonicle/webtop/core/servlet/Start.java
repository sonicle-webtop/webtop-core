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

import com.sonicle.security.Principal;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.WebTopSession;
import com.sonicle.webtop.core.bol.js.JsWTStartup;
import com.sonicle.webtop.core.sdk.Encryption;
import com.sonicle.webtop.core.sdk.UserProfile;
import freemarker.template.Template;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author malbinola
 */
public class Start extends HttpServlet {
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		WebTopSession wts = WebTopSession.get(request);
		CoreManager manager = wta.getManager();
		
		try {
			WebTopApp.logger.trace("Servlet: Start [{}]", ServletHelper.getSessionID(request));
			wts.checkEnvironment(request);
			Locale locale = wts.getLocale();
			String theme = wts.getTheme();
			String lookAndFeel = wts.getLookAndFeel();
			WebTopApp.logger.trace("locale:   {}", locale);
			
			Subject currentUser=SecurityUtils.getSubject();
			String user_id=((Principal)currentUser.getPrincipal()).getSubjectId();
			WebTopApp.logger.trace("user {} is permitted mail service: {}",user_id,currentUser.isPermitted("service:com.sonicle.webtop.mail:access"));
			WebTopApp.logger.trace("user {} is permitted mail service: {}",user_id,currentUser.isPermitted("service:com.sonicle.webtop.calendar:access"));
			boolean isAdmin=currentUser.hasRole("admin");
			WebTopApp.logger.trace("user {} is admin: {}",user_id,isAdmin);
			
			Map tplMap = new HashMap();
			tplMap.put("theme", theme);
			tplMap.put("laf", lookAndFeel);
			tplMap.put("rtl", String.valueOf(wts.getRTL()));
			tplMap.put("debug", "false");
			ServletHelper.fillPageVars(tplMap, locale, wta);
			
			UserProfile p=wts.getUserProfile();
			String ticket=request.getSession().getId();
			WebTopApp.logger.trace("Generated ticket = {}",ticket);
			String encTicket=Encryption.cipher(ticket, p.getSecret());
			WebTopApp.logger.trace("Encoded ticket = {}",encTicket);
			
			// Fill client startup variables
			JsWTStartup jswt = new JsWTStartup();
			jswt.locale = locale.toString();
			jswt.theme = theme;
			jswt.laf = lookAndFeel;
			jswt.encAuthTicket = encTicket;
			jswt.domainId=p.getDomainId();
			jswt.userId=p.getUserId();
			for(String serviceId : wts.getServices()) {
				//if(serviceId.equals(CoreManifest.ID)) continue;
				manager.fillForService(jswt, serviceId, locale);
				jswt.initialSettings.put(serviceId, wts.getInitialSettings(serviceId));
			}
			tplMap.put("WTStartup", JsonResult.gson.toJson(jswt));
			
			// Load and build template
			Template tpl = wta.loadTemplate("com/sonicle/webtop/core/start.html");
			tpl.process(tplMap, response.getWriter());
			
		} catch(Exception ex) {
			WebTopApp.logger.error("Error in start servlet!", ex);
		} finally {
			ServletHelper.setCacheControl(response);
			ServletHelper.setPageContentType(response);
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
