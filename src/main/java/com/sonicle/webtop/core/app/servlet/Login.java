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
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.PushEndpoint;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Login extends AbstractServlet {
	public static final String URL = "/login"; // Shiro.ini must reflect this URI!
	private static final Logger logger = WT.getLogger(Login.class);
	public static final String ATTRIBUTE_LOGINFAILURE = "loginFailure";
	public static final String LOGINFAILURE_INVALID = "invalid";
	public static final String LOGINFAILURE_MAINTENANCE = "maintenance";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean redirect = false;
		
		if (logger.isTraceEnabled()) {
			logger.trace("X-REQUEST-URI: {}", request.getHeader("X-REQUEST-URI"));
			logger.trace("requestUrl: {}", request.getRequestURL().toString());
			logger.trace("pathInfo: {}", request.getPathInfo());
			logger.trace("baseUrl: {}", getBaseUrl(request));
			logger.trace("forwardServletPath: {}", getRequestForwardServletPath(request));
		}
		
		if (ServletUtils.isForwarded(request)) {
			String forwardServletPath = getRequestForwardServletPath(request);
			if (StringUtils.startsWithIgnoreCase(forwardServletPath, Login.URL)
					|| StringUtils.startsWithIgnoreCase(forwardServletPath, Logout.URL)) {
				redirect = true;
			}
			if (StringUtils.startsWithIgnoreCase(forwardServletPath, PushEndpoint.URL)) {
				WebUtils.getAndClearSavedRequest(request);
				redirect = true;
			}
		} else {
			String requestUrl = request.getRequestURL().toString();
			if (StringUtils.endsWithIgnoreCase(requestUrl, Login.URL)) {
				redirect = true;
			}
		}
		
		if (redirect) {
			WebUtils.issueRedirect(request, response, "/");
		} else {
			super.doGet(request, response);
		}
	}
	
	private String getRequestForwardServletPath(HttpServletRequest request) {
		return (String)request.getAttribute(javax.servlet.RequestDispatcher.FORWARD_SERVLET_PATH);
	}
	
	private String getBaseUrl(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		return url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
	}
	
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebTopApp wta = getWebTopApp(request);
		
		try {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
			WebTopManager usem = wta.getWebTopManager();
			WebTopSession wts = SessionContext.getCurrent();
			Locale locale = wts.getLocale();
			
			boolean maintenance = wta.getServiceManager().isInMaintenance(CoreManifest.ID);
			
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
			List<ODomain> enabledDomains = usem.listDomains(true);
			List<HtmlSelect> domains = new ArrayList<>();
			if(enabledDomains.size() > 1) domains.add(new HtmlSelect("", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_DOMAIN_PROMPT, true)));
			for(ODomain dom : enabledDomains) {
				domains.add(new HtmlSelect(dom.getDomainId(), dom.getDescription()));
			}
			
			writePage(wta, css, locale, domains, maintenanceMessage, failureMessage, response);
			
		} catch(Exception ex) {
			logger.error("Error", ex);
		}
	}
	
	private void writePage(WebTopApp wta, CoreServiceSettings css, Locale locale, List<HtmlSelect> domains, String maintenanceMessage, String failureMessage, HttpServletResponse response) throws IOException, TemplateException {
		boolean showDomain = (css.getHideLoginDomains()) ? false : (domains.size() > 1);
		
		Map tplMap = new HashMap();
		AbstractServlet.fillPageVars(tplMap, locale, null);
		AbstractServlet.fillSystemVars(tplMap, wta, locale, !css.getHideLoginSystemInfo(), !css.getHideLoginWebappName());
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
		
		ServletUtils.setHtmlContentType(response);
		ServletUtils.setCacheControlPrivate(response);
		
		// Load and build template
		Template tpl = WT.loadTemplate(CoreManifest.ID, "tpl/page/login.html");
		tpl.process(tplMap, response.getWriter());
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
