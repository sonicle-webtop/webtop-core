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

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.ShiroFilter;

/**
 *
 * @author malbinola
 */
public class WTShiroFilter extends ShiroFilter {

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
}
