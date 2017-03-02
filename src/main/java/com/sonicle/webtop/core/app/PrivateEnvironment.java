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
package com.sonicle.webtop.core.app;

import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.List;

/**
 *
 * @author malbinola
 */
public class PrivateEnvironment extends AbstractEnvironment {
	//private final static Logger logger = WT.getLogger(SessionEnvironment.class);
	protected final CoreServiceSettings css;
	protected final CoreUserSettings cus;
	protected final String csrf;

	public PrivateEnvironment(WebTopSession wts) {
		super(wts);
		csrf = RunContext.getCSRFToken();
		css = new CoreServiceSettings(CoreManifest.ID, wts.getProfileDomainId());
		UserProfileId pid = wts.getProfileId();
		cus = (pid != null) ? new CoreUserSettings(pid) : null;
	}

	public UserProfile getProfile() {
		return wts.getUserProfile();
	}
	
	public UserProfileId getProfileId() {
		return wts.getProfileId();
	}
	
	public CoreServiceSettings getCoreServiceSettings() {
		return css;
	}
	
	public CoreUserSettings getCoreUserSettings() {
		return cus;
	}
	
	public String getSessionRefererUri() {
		return wts.getRefererURI();
	}
	
	public void notify(ServiceMessage message) {
		wts.notify(message);
	}
	
	public void notify(List<ServiceMessage> messages) {
		wts.notify(messages);
	}
	
	public String getSecurityToken() {
		//TODO: valore di ritorno provvisorio, rimuovere in seguito!
		return csrf;
	}
}
