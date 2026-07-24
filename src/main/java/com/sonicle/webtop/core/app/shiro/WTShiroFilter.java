/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.shiro;

import com.sonicle.commons.web.CommonHttpServletRequest;
import com.sonicle.commons.web.RequestId;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.ContextLoader;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.servlet.PrivateRequest;
import com.sonicle.webtop.core.app.shiro.filter.RequestDumper;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WTShiroFilter extends ShiroFilter {
	private static final Logger LOGGER_REQDUMP = (Logger) LoggerFactory.getLogger(RequestDumper.class);
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WTShiroFilter.class);

	//REQDBG: temporary instrumentation to locate intermittent pre-handler stalls
	//(service handlers log fast while the client waits, with no trace server-side).
	//Mail service-requests get a stamped bracket across the whole dispatch chain
	//(filter entry -> subject/session bound -> servlet -> handler); anything else
	//is only reported once at completion when the whole traversal exceeded 1s.
	//If a stall recurs and NO "filter entry" line appears for it, the request
	//never reached the container: look client-side (browser conn pool, proxy).
	public static final String ATTR_REQDBG_ID = "wt.reqdbg.id";
	public static final String ATTR_REQDBG_T0 = "wt.reqdbg.t0";
	private static final AtomicLong REQDBG_COUNTER = new AtomicLong();

	public static void reqdbg(ServletRequest request, String stage) {
		Object id = request.getAttribute(ATTR_REQDBG_ID);
		if (id == null) return;
		long t0 = (Long) request.getAttribute(ATTR_REQDBG_T0);
		LOGGER.info("[REQDBG {}] {}: +{}ms", id, stage, System.currentTimeMillis() - t0);
	}

	public static void reqdbgTag(ServletRequest request, long t0) {
		request.setAttribute(ATTR_REQDBG_ID, "REQ" + REQDBG_COUNTER.incrementAndGet());
		request.setAttribute(ATTR_REQDBG_T0, t0);
	}

	@Override
	protected void executeChain(ServletRequest request, ServletResponse response, FilterChain origChain) throws IOException, ServletException {
		WebTopApp wta = ContextLoader.getWebTopApp(request.getServletContext());
		if (wta == null || !wta.isStateReady()) {
			ServletUtils.sendError(WebUtils.toHttp(response), 503, "Application not ready");
			return;
		}
		reqdbg(request, "subject/session bound, entering chain");
		super.executeChain(request, response, origChain);
	}
	
	@Override
	protected ServletRequest wrapServletRequest(HttpServletRequest orig) {
		if (LOGGER_REQDUMP.isTraceEnabled()) {
			CommonHttpServletRequest wrapped = new CommonHttpServletRequest(orig);
			wrapped.addHeader(ServletUtils.HEADER_X_REQUEST_ID, RequestId.generateNew() + "-" + orig.getMethod());
			return super.wrapServletRequest(wrapped);
			
		} else {
			return super.wrapServletRequest(orig);
		}
	}
	
	/*
	@Override
	protected void executeChain(ServletRequest request, ServletResponse response, FilterChain origChain) throws IOException, ServletException {
		super.executeChain(request, response, origChain);
	}
	*/
	
	@Override
	protected ServletResponse prepareServletResponse(ServletRequest request, ServletResponse response, FilterChain chain) { 
		if (response instanceof HttpServletResponse) {
			((HttpServletResponse)response).setHeader("X-Robots-Tag", "none"); // https://developers.google.com/webmasters/control-crawl-index/docs/robots_meta_tag
			
			// Some security-related headers... here only for reference: evaluate them before enabling!
			//((HttpServletResponse)response).setHeader("Referrer-Policy", "no-referrer"); // https://www.w3.org/TR/referrer-policy/
			//((HttpServletResponse)response).setHeader("X-Content-Type-Options", "nosniff"); // Disable sniffing the content type for IE
			//((HttpServletResponse)response).setHeader("X-Download-Options", "noopen"); // https://msdn.microsoft.com/en-us/library/jj542450(v=vs.85).aspx
			//((HttpServletResponse)response).setHeader("X-Frame-Options", "SAMEORIGIN"); // Disallow iFraming from other domains
			//((HttpServletResponse)response).setHeader("X-Permitted-Cross-Domain-Policies", "none"); // https://www.adobe.com/devnet/adobe-media-server/articles/cross-domain-xml-for-streaming.html
			//((HttpServletResponse)response).setHeader("X-XSS-Protection", "1; mode=block"); // Enforce browser based XSS filters
		}
		return super.prepareServletResponse(request, response, chain);
	}
	
	/*
	@Override
	protected WebSubject createSubject(ServletRequest request, ServletResponse response) {
		String servletPath = ((HttpServletRequest)request).getServletPath();
		if (StringUtils.equals(servletPath, "/"+PublicRequest.URL)) {
			return createAdminSubject(getSecurityManager(), request, response);
		} else {
			return super.createSubject(request, response);
		}
	}
	
	private WebSubject createAdminSubject(SecurityManager securityManager, ServletRequest request, ServletResponse response) {
		UserProfileId profileId = new UserProfileId(WebTopManager.SYSADMIN_DOMAINID, WebTopManager.SYSADMIN_USERID);
		return RunContext.buildWebSubject(securityManager, request, response, profileId);
	}
	*/

	@Override
	protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws ServletException, IOException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		if (StringUtils.isBlank(request.getCharacterEncoding())) {
			request.setCharacterEncoding("UTF-8");
		}
		final long t0 = System.currentTimeMillis();
		final String uri = request.getRequestURI();
		final String query = request.getQueryString();
		boolean tagged = false;
		if (uri != null && (uri.endsWith(PrivateRequest.URL) || uri.endsWith(PrivateRequest.URL_LEGACY))
				&& query != null && query.contains("com.sonicle.webtop.mail")) {
			reqdbgTag(request, t0);
			tagged = true;
			LOGGER.info("[REQDBG {}] filter entry: {} {}?{}", request.getAttribute(ATTR_REQDBG_ID), request.getMethod(), uri, StringUtils.abbreviate(query, 160));
		}
		try {
			super.doFilterInternal(servletRequest, servletResponse, chain);
		} finally {
			long elapsed = System.currentTimeMillis() - t0;
			if (tagged) {
				LOGGER.info("[REQDBG {}] === TOTAL filter enter->exit ===: {}ms", request.getAttribute(ATTR_REQDBG_ID), elapsed);
			} else if (elapsed > 1000) {
				LOGGER.info("[REQDBG slow-request] {}ms: {} {}?{}", elapsed, request.getMethod(), uri, StringUtils.abbreviate(query, 160));
			}
		}
	}
	
	
}
