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

import com.sonicle.commons.PathUtils;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.BaseAbstractService;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.servlet.PublicServiceRequest;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author malbinola
 */
public abstract class BasePublicService extends BaseAbstractService {
	private boolean configured = false;
	private Subject subject;
	
	public final void configure(Subject subject) {
		if(configured) return;
		configured = true;
		this.subject = Check.notNull(subject);
	}
	
	public abstract void processDefaultAction(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception;
	
	public ServiceVars returnServiceVars() {
		return null;
	}
	
	public void writePage(String baseUrl, Map vars, Locale locale, PrintWriter out) throws IOException, TemplateException {
		AbstractServlet.fillPageVars(vars, WT.getPlatformName(), PathUtils.ensureTrailingSeparator(baseUrl));
		AbstractServlet.fillIncludeVars(vars, locale, DEFAULTVAR_THEME, DEFAULTVAR_LAF, false, WebTopApp.getPropExtDebug());
		Template tpl = WT.loadTemplate(CoreManifest.ID, "public.html");
		tpl.process(vars, out);
	}
	
	protected String getPublicResourcesBaseUrl() {
		return PublicServiceRequest.PUBLIC_RESOURCES;
	}
	
	
	
	protected PublicPath parsePathInfo(String pathInfo) throws MalformedURLException {
		String[] tokens = StringUtils.split(pathInfo, "/", 3);
		if(tokens.length < 2) throw new MalformedURLException("Invalid URL");
		return new PublicPath(tokens[0], tokens[1], (tokens.length == 2) ? null : tokens[2]);
	}
	
	public static class PublicPath {
		public final String publicName;
		public final String context;
		public final String relative;
		
		public PublicPath(String publicName, String context, String relative) {
			this.publicName = publicName;
			this.context = context;
			this.relative = relative;
		}
	}
	
	public static class ServiceVars extends HashMap<String, Object> {
		public ServiceVars() {
			super();
		}
	}
}
