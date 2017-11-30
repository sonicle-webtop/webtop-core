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
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WebTopApp;

/**
 *
 * @author malbinola
 */
public abstract class BaseServiceSettings extends BaseSettings {
	public static final String DEFAULT_PREFIX = "default.";
	private SettingsManager setMgr;
	protected String serviceId;
	protected String domainId;

	public BaseServiceSettings(SettingsManager setMgr, String serviceId, String domainId) {
		this.setMgr = setMgr;
		this.serviceId = serviceId;
		this.domainId = domainId;
	}

	public BaseServiceSettings(String serviceId, String domainId) {
		setMgr = WebTopApp.getInstance().getSettingsManager();
		this.serviceId = serviceId;
		this.domainId = domainId;
	}
	
	public CoreServiceSettings createCoreServiceSettings() {
		return new CoreServiceSettings(CoreManifest.ID, domainId);
	}
	
	@Override
	public String getSetting(String key) {
		return setMgr.getServiceSetting(domainId, serviceId, key);
	}
	
	@Override
	public boolean setSetting(String key, Object value) {
		return setMgr.setServiceSetting(domainId, serviceId, key, value);
	}
	
	@Override
	public String getString(String key, String defaultValue) {
		return LangUtils.value(setMgr.getServiceSetting(domainId, serviceId, key), defaultValue);
	}
	
	@Override
	public boolean setString(String key, String value) {
		return setMgr.setServiceSetting(domainId, serviceId, key, value);
	}
	
	@Override
	public Boolean getBoolean(String key, Boolean defaultValue) {
		return LangUtils.value(setMgr.getServiceSetting(domainId, serviceId, key), defaultValue);
	}
	
	@Override
	public boolean setBoolean(String key, Boolean value) {
		return setMgr.setServiceSetting(domainId, serviceId, key, value);
	}
	
	@Override
	public Integer getInteger(String key, Integer defaultValue) {
		return LangUtils.value(setMgr.getServiceSetting(domainId, serviceId, key), defaultValue);
	}
	
	@Override
	public boolean setInteger(String key, Integer value) {
		return setMgr.setServiceSetting(domainId, serviceId, key, value);
	}
	
	@Override
	public Long getLong(String key, Long defaultValue) {
		return LangUtils.value(setMgr.getServiceSetting(domainId, serviceId, key), defaultValue);
	}
	
	@Override
	public boolean setLong(String key, Long value) {
		return setMgr.setServiceSetting(domainId, serviceId, key, value);
	}
}
