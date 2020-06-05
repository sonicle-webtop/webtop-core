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
package com.sonicle.webtop.core;

import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerServiceHooks;
import com.sonicle.webtop.core.app.sdk.interfaces.IControllerUserEvents;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.core.sdk.BaseController;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreController extends BaseController implements IControllerServiceHooks, IControllerUserEvents {
	public static final Logger logger = WT.getLogger(CoreController.class);
	
	public CoreController() {
		super();
	}
	
	@Override
	public void initProfile(ServiceVersion current, UserProfileId profileId) throws WTException {
		addBuiltinThunderbirdTags(profileId);
	}
	
	@Override
	public void onUserAdded(UserProfileId profileId) throws WTException {}

	@Override
	public void onUserRemoved(UserProfileId profileId) throws WTException {
		CoreManager manager = WT.getCoreManager(true, profileId);
		manager.eraseData(true);
	}
	
	private static final String[][] BULTIN_THUNDERBIRD_TAGS = {
		{"$label1","#E53935"},
		{"$label2","#FF9800"},
		{"$label3","#43A047"},
		{"$label4","#3F51B5"},
		{"$label5","#9C27B0"}
	};
	
	private void addBuiltinThunderbirdTags(UserProfileId profileId) {
		for(String[] builtinThunderbirdTag: BULTIN_THUNDERBIRD_TAGS) {
			try {
				String id=builtinThunderbirdTag[0];
				String color=builtinThunderbirdTag[1];
				Tag tag=new Tag();
				tag.setBuiltIn(false);
				tag.setColor(color);
				tag.setDomainId(profileId.getDomainId());
				tag.setExternalId(id);
				tag.setName(WT.lookupResource(SERVICE_ID,WT.getUserData(profileId).getLocale(), CoreLocaleKey.TAGS_LABEL(id)));
				tag.setPersonal(true);
				WT.getCoreManager().addTag(tag);
			} catch(Exception exc) {
				logger.error("error creating builtin Thunderbird tags",exc);
			}
		}
	}
	
	@Override
	public void upgradeProfile(ServiceVersion current, UserProfileId profileId, ServiceVersion profileLastSeen) throws WTException {
	}
	
}
