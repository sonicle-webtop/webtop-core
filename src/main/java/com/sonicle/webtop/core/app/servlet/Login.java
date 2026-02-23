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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.web.CacheControl;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopProps;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Login extends AbstractServlet {
	public static final String URL = "/login"; // Shiro.ini must reflect this URI!
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Login.class);
	
	public static final String ATTRIBUTE_LOGINFAILURE = "loginFailure";
	public static final String LOGINFAILURE_INVALID = "invalid";
	public static final String LOGINFAILURE_DISABLED = "disabled";
	public static final String LOGINFAILURE_MAINTENANCE = "maintenance";
	
	@Override
	protected void processGetOrPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("X-REQUEST-URI: {}", request.getHeader("X-REQUEST-URI"));
			LOGGER.trace("requestUrl: {}", request.getRequestURL().toString());
			LOGGER.trace("pathInfo: {}", request.getPathInfo());
			LOGGER.trace("baseUrl: {}", getBaseUrl(request));
			LOGGER.trace("forwardServletPath: {}", getRequestForwardServletPath(request));
		}
		*/
		final WebTopApp wta = getWebTopApp(request);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[{}] processGetOrPost", ServletUtils.getRequestID(request));
		
		try {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
			WebTopManager wtMgr = wta.getWebTopManager();
			Locale locale = getLocale(request);
			
			boolean maintenance = wta.getServiceManager().isInMaintenance(CoreManifest.ID);
			
			// Defines messages...
			String maintenanceMessage = (maintenance) ? wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_MAINTENANCE, true) : null;
			
			// Defines failure message
			String failureAttribute = ServletUtils.getStringAttribute(request, ATTRIBUTE_LOGINFAILURE);
			String failureMessage = null;
			if (failureAttribute != null) {
				switch (failureAttribute) {
					case LOGINFAILURE_INVALID:
						failureMessage = wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_ERROR_FAILURE);
						break;
					case LOGINFAILURE_DISABLED:
						failureMessage = wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_ERROR_DISABLED);
						break;
					case LOGINFAILURE_MAINTENANCE:
						failureMessage = wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_ERROR_MAINTENANCE);
						break;
				}
			}
			
			List<HtmlSelect> domains = null;
			if (wtMgr.getEnabledDomainsCount() > 1) {
				domains = new ArrayList<>();
				domains.add(new HtmlSelect("", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_DOMAIN_PLACEHOLDER, true)));
				for (Map.Entry<String, String> entry : wtMgr.getEnabledDomains().entrySet()) {
					domains.add(new HtmlSelect(entry.getKey(), entry.getValue()));
				}
			}
			
			// Disarm prefetch requests to this page
			if (ServletUtils.isSpeculativeRequestToDocument(request)) {
				// This allows to circumvent an issue in Chrome when a
				// bookmark (pointing to the app URL) is hovered or clicked: 
				// instead of performing page loading, restoring the session 
				// using the RMe cookie, the browser simply point to 
				// prefetched page and session restoration won't start.
				ServletUtils.setCachingNotAllowed(response);
				response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
				response.setHeader("Vary", "Purpose, Sec-Purpose, Sec-Fetch-User, Accept");
				
			} else {
				writePage(wta, css, locale, domains, maintenanceMessage, failureMessage, response);
			}
			
		} catch (Exception ex) {
			LOGGER.error("[{}] Error processGetOrPost", ServletUtils.getRequestID(request), ex);
		}
	}
	
	private void writePage(final WebTopApp wta, final CoreServiceSettings css, final Locale locale, final List<HtmlSelect> domains, final String maintenanceMessage, final String failureMessage, final HttpServletResponse response) throws IOException, TemplateException {
		boolean showDomain = domains != null;
		
		MapItem vars = new MapItem();
		AbstractServlet.fillPageVars(vars, locale, null, null, null);
		AbstractServlet.fillSystemVars(vars, wta, locale, !css.getHideLoginSystemInfo(), !css.getHideLoginWebappName());
		AbstractServlet.fillCustomVars(vars, WebTopProps.getLoginTemplateCustomVars(WT.getProperties()));
		
		vars.put("showMaintenance", !StringUtils.isBlank(maintenanceMessage));
		vars.put("maintenanceMessage", maintenanceMessage);
		vars.put("showFailure", !StringUtils.isBlank(failureMessage));
		vars.put("failureMessage", failureMessage);
		vars.put("showDomain", showDomain);
		vars.put("domains", showDomain ? domains : /* allow backward compatibility */ new ArrayList<>());
		vars.put("showRememberMe", css.getLoginRememberMeEnabled());
		vars.put("showPasswordReveal", css.getLoginPasswordRevealEnabled());
		
		Map i18n = new HashMap();
		i18n.put("usernameLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_USERNAME_LABEL));
		i18n.put("usernamePlaceholder", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_USERNAME_PLACEHOLDER));
		i18n.put("passwordLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_PASSWORD_LABEL));
		i18n.put("passwordPlaceholder", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_PASSWORD_PLACEHOLDER));
		i18n.put("domainLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_DOMAIN_LABEL));
		i18n.put("rememberMeLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_REMEMBERME_LABEL));
		i18n.put("submitLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_LOGIN_SUBMIT_LABEL));
		vars.put("usernameLabel", LangUtils.encodeForHTMLContent((String)i18n.get("usernameLabel"))); // DEPRECATED leave for cbackward compatibility!
		vars.put("usernamePlaceholder", LangUtils.encodeForHTMLContent((String)i18n.get("usernamePlaceholder"))); // DEPRECATED leave for cbackward compatibility!
		vars.put("passwordLabel", LangUtils.encodeForHTMLContent((String)i18n.get("passwordLabel"))); // DEPRECATED leave for cbackward compatibility!
		vars.put("passwordPlaceholder", LangUtils.encodeForHTMLContent((String)i18n.get("passwordPlaceholder"))); // DEPRECATED leave for cbackward compatibility!
		vars.put("domainLabel", LangUtils.encodeForHTMLContent((String)i18n.get("domainLabel"))); // DEPRECATED leave for cbackward compatibility!
		vars.put("rememberMeLabel", LangUtils.encodeForHTMLContent((String)i18n.get("rememberMeLabel"))); // DEPRECATED leave for cbackward compatibility!
		vars.put("submitLabel", LangUtils.encodeForHTMLContent((String)i18n.get("submitLabel"))); // DEPRECATED leave for cbackward compatibility!
		vars.put("i18n", i18n);
		
		ServletUtils.setHtmlContentType(response);
		ServletUtils.setCachingNotAllowed(response);
		
		WT.writeTemplate(CoreManifest.ID, "page/login.html", vars, response.getWriter());
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
