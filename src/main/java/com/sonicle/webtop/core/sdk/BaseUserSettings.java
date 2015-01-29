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
import com.sonicle.webtop.core.SettingsManager;
import com.sonicle.webtop.core.WebTopApp;

/**
 *
 * @author gbulfon
 */
public abstract class BaseUserSettings {
    
    private SettingsManager setm;
    protected String serviceId;
    protected String domainId;
    protected String userId;
    
    private BaseUserSettings() {
        
    }
    
    public BaseUserSettings(String domainId, String userId, String serviceId) {
        this.setm = WebTopApp.getInstance().getSettingsManager();
        this.domainId = domainId;
        this.userId = userId;
        this.serviceId = serviceId;
    }
	
	public boolean clearUserSetting(String key) {
		return setm.deleteUserSetting(domainId, userId, serviceId, key);
	}
    
	public String getUserSetting(String key) {
		return setm.getUserSetting(domainId, userId, serviceId, key);
	}
	
	public boolean setUserSetting(String key, Object value) {
		return setm.setUserSetting(domainId, userId, serviceId, key, value);
	}

	public String getUserSetting(String key, String defaultValue) {
		return LangUtils.value(setm.getUserSetting(domainId, userId, serviceId, key), defaultValue);
	}

	public int getUserSetting(String key, int defaultValue) {
		return LangUtils.value(setm.getUserSetting(domainId, userId, serviceId, key), defaultValue);
	}

	public boolean getUserSetting(String key, boolean defaultValue) {
		return LangUtils.value(setm.getUserSetting(domainId, userId, serviceId, key), defaultValue);
	}
	
	public <T>T getUserSetting(String key, T defaultValue, Class<T> type) {
		return LangUtils.value(setm.getUserSetting(domainId, userId, serviceId, key), defaultValue, type);
	}
}
