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
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.AbstractEnvironmentService;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.PublicEnvironment;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.bol.js.JsWTSPublic;
import com.sonicle.webtop.core.servlet.PublicServiceRequest;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class BasePublicService extends AbstractEnvironmentService<PublicEnvironment> {
	
	public abstract void processDefaultAction(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public ServiceVars returnServiceVars() {
		return null;
	}
	
	public void writePage(HttpServletResponse response, WebTopSession wts, JsWTSPublic.Vars serviceVars, String contextPath) throws IOException, TemplateException {
		Map vars = new HashMap();
		JsWTSPublic jswts = new JsWTSPublic();
		jswts.contextPath = contextPath;
		wts.fillStartup(jswts, SERVICE_ID);
		jswts.servicesVars.get(1).putAll(serviceVars);
		vars.put("WTS", LangUtils.unescapeUnicodeBackslashes(jswts.toJson()));
		writePage(response, contextPath, vars, wts.getLocale());
		ServletUtils.setCacheControlPrivate(response);
		ServletUtils.setHtmlContentType(response);
	}
	
	public void writePage(HttpServletResponse response, String contextPath, Map vars, Locale locale) throws IOException, TemplateException {
		AbstractServlet.fillPageVars(vars, locale, contextPath);
		Template tpl = WT.loadTemplate(CoreManifest.ID, "tpl/page/public.html");
		tpl.process(vars, response.getWriter());
	}
	
	protected String getPublicResourcesBaseUrl() {
		return PublicServiceRequest.PUBLIC_RESOURCES;
	}
	
	public static class PublicPath extends UrlPathTokens {
		
		public PublicPath(String pathInfo) throws MalformedURLException {
			super(StringUtils.split(pathInfo, "/", 3));
			if(tokens.length < 2) throw new MalformedURLException("Invalid URL");
		}
		
		public String getPublicName() {
			return getTokenAt(0);
		}
		
		public String getContext() {
			return getTokenAt(1);
		}
		
		public String getRemainingPath() {
			return getTokenAt(2);
		}
	}
	
	public static class UrlPathTokens {
		public final String[] tokens;
		
		public UrlPathTokens(String[] tokens) {
			this.tokens = tokens;
		}
		
		public String getTokenAt(int index) {
			return (index < tokens.length) ? tokens[index] : null;
		}
	}
	
	public static class ServiceVars extends HashMap<String, Object> {
		public ServiceVars() {
			super();
		}
	}
}
