/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.mail.StoreHostParams;
import com.sonicle.mail.StoreProtocol;
import com.sonicle.security.Principal;
import static com.sonicle.webtop.core.app.DummyMailSettings.*;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfileId;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class DummyMailUserSettings extends BaseUserSettings {
	private DummyMailServiceSettings mss;
	
	public DummyMailUserSettings(UserProfileId profileId) {
		super("com.sonicle.webtop.mail", profileId);
		this.mss = new DummyMailServiceSettings(profileId.getDomainId());
	}
	
	/*
	public StoreHostParams getMailboxHostDefinition(final boolean impersonate, final Principal principal) {
		final char[] cpass = principal.getPassword();
		return getMailboxHostDefinition(impersonate, principal.getFullInternetName(), (cpass != null) ? new String(cpass) : null);
	}
	*/
	
	public StoreHostParams getMailboxHostDefinition(final boolean impersonate, final String defaultUsername, final String defaultPassword) {
		StoreHostParams shp = new StoreHostParams(getHost(), getPort(), StoreProtocol.parse(getProtocol(), false));
		
		String username = StringUtils.defaultIfBlank(getUsername(), defaultUsername);
		String password = StringUtils.defaultIfBlank(getPassword(), defaultPassword);
		if (impersonate) {
			if (!StringUtils.isBlank(mss.getNethTopVmailSecret())) {
				shp.withUsername(username);
				shp.withVMAILImpersonate(mss.getNethTopVmailSecret());
			} else if (!StringUtils.isBlank(mss.getAdminUser())) {
				shp.withUsername(username);
				shp.withSASLImpersonate(mss.getAdminUser(), mss.getAdminPassword());
			}
		} else {
			shp.withUsername(username);
			shp.withPassword(password);
		}
		return shp;
	}
	
	public String getHost() {
		String s=getString(HOST,null);
		if (s==null) s=mss.getDefaultHost();
		return s;
	}
	
	public int getPort() {
		Integer i=getInteger(PORT,null);
		if (i==null) i=mss.getDefaultPort();
		return i;
	}
	
	public String getProtocol() {
		String s=getString(PROTOCOL,null);
		if (s==null) s=mss.getDefaultProtocol();
		return s;
	}
	
	public String getUsername() {
		return getString(USERNAME,null);
	}
	
	public String getPassword() {
		return getString(PASSWORD,null);
	}
}
