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

import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.UserDAO;
import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author malbinola
 */
public final class UserProfile {
	
	private final WebTopApp wta;
	private final Principal principal;
	private OUser user;
	
	public UserProfile(WebTopApp wta, Principal principal) {
		this.wta = wta;
		this.principal = principal;
		initialize();
	}
	
	private void initialize() {
		
		// TODO: complete this!
		//UserDAO udao = UserDAO.getInstance();
		//udao.selectByDomainUser(wta.getConnectionManager().getConnection(), null, null);
	}
	
	public String getId() {
		return principal.getName();
	}
	
	public String getUserId() {
		return principal.getUserId();
	}
	
	public String getDomainId() {
		return principal.getDomainId();
	}
	
	public String getMailcardId() {
		AuthenticationDomain ad = principal.getAuthenticationDomain();
		return MessageFormat.format("{0}@{1}", principal.getUserId(), ad.getDomain());
	}
	
	public Locale getLocale() {
		return new Locale("it", "IT");
		//return new Locale(user.getLocale());
	}
}
