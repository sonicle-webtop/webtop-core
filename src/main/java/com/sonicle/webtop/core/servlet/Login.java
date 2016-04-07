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
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.ODomain;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class Login extends HttpServlet {
	public static final String ATTRIBUTE_LOGINFAILURE = "loginFailure";
	public static final String LOGINFAILURE_INVALID = "invalid";
	public static final String LOGINFAILURE_MAINTENANCE = "maintenance";
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = WebTopApp.get(request);
		String sessionId = ServletHelper.getSessionID(request);
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
		CoreManager core = new CoreManager(wta.createAdminRunContext(sessionId), wta);
		
		try {
			WebTopApp.logger.trace("Servlet: login [{}]", sessionId);
			Locale locale = ServletHelper.homogenizeLocale(request);
			
			//SettingsManager sm = wta.getSettingsManager();
			//ServiceManifest manifest = wta.getServiceManifest(ServicesManager.MAIN_SERVICE_ID);
			//boolean maintenance = LangUtils.value(sm.getServiceSetting(ServicesManager.MAIN_SERVICE_ID, Settings.MAINTENANCE), false);
			boolean maintenance = true;
			
			// Defines messages...
			String maintenanceMessage = (maintenance) ? wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_MAINTENANCE, true) : null;
			
			// Defines failure message
			String failureAttribute = ServletUtils.getStringAttribute(request, ATTRIBUTE_LOGINFAILURE);
			String failureMessage = null;
			if(failureAttribute != null) {
				switch (failureAttribute) {
					case Login.LOGINFAILURE_INVALID:
						failureMessage = wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_ERROR_FAILURE, true);
						break;
					case Login.LOGINFAILURE_MAINTENANCE:
						failureMessage = wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_ERROR_MAINTENANCE, true);
						break;
				}
			}
			
			// Prepare domains list
			List<ODomain> enabledDomains = core.listDomains(true);
			List<HtmlSelect> domains = new ArrayList<>();
			if(enabledDomains.size() > 1) domains.add(new HtmlSelect("", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_DOMAIN_PROMPT, true)));
			for(ODomain dom : enabledDomains) {
				domains.add(new HtmlSelect(dom.getDomainId(), dom.getDescription()));
			}
			
			buildPage(wta, css, locale, domains, maintenanceMessage, failureMessage, response);
			
		} catch(Exception ex) {
			WebTopApp.logger.error("Error in login servlet!", ex);
		} finally {
			ServletHelper.setCacheControl(response);
			ServletHelper.setPageContentType(response);
			WebTopApp.clearLoggerDC();
		}
	}
	
	private void buildPage(WebTopApp wta, CoreServiceSettings css, Locale locale, List<HtmlSelect> domains, String maintenanceMessage, String failureMessage, HttpServletResponse response) throws IOException, TemplateException {
		boolean showDomain = (css.getHideLoginDomains()) ? false : (domains.size() > 1);
		
		Map tplMap = new HashMap();
		ServletHelper.fillPageVars(tplMap, locale, wta);
		ServletHelper.fillSystemVars(tplMap, locale, wta);
		tplMap.put("showFootprint", !css.getHideLoginFootprint());
		tplMap.put("showMaintenance", !StringUtils.isBlank(maintenanceMessage));
		tplMap.put("maintenanceMessage", maintenanceMessage);
		tplMap.put("showFailure", !StringUtils.isBlank(failureMessage));
		tplMap.put("failureMessage", failureMessage);
		tplMap.put("usernamePlaceholder", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_USERNAME_PLACEHOLDER, true));
		tplMap.put("passwordPlaceholder", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_PASSWORD_PLACEHOLDER, true));
		tplMap.put("domainLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_DOMAIN_LABEL, true));
		tplMap.put("submitLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_SUBMIT_LABEL, true));
		tplMap.put("showDomain", showDomain);
		tplMap.put("domains", domains);
		
		// Load and build template
		Template tpl = wta.loadTemplate("com/sonicle/webtop/core/login.html");
		tpl.process(tplMap, response.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
	
	public static class HtmlSelect {
		private String value;
		private String description;

		public HtmlSelect() {}

		public HtmlSelect(String value, String description) {
			this.value = value;
			this.description = description;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}
