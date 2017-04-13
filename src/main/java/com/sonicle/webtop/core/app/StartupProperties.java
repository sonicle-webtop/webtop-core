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

import com.sonicle.commons.LangUtils;
import java.util.Properties;

/**
 *
 * @author malbinola
 */
public class StartupProperties extends Properties {
		public static final String PROP_EXTJS_DEBUG = "extJsDebug";
		public static final String PROP_SO_EXT_DEV_MODE = "soExtDevMode";
		public static final String PROP_DEV_MODE = "devMode";
		public static final String PROP_DEBUG_MODE = "debugMode";
		public static final String PROP_SCHEDULER_DISABLED = "schedulerDisabled";
		public static final String PROP_WEBAPPS_CONFIG_PATH = "webappsConfigPath";
		
		public StartupProperties() {
			super();
		}
		
		StartupProperties(Properties defaults) {
			super(defaults);
		}
		
		public boolean getExtJsDebug() {
			return LangUtils.value(getProperty(PROP_EXTJS_DEBUG, null), false);
		}

		public boolean getSonicleExtJsExtensionsDevMode() {
			return LangUtils.value(getProperty(PROP_SO_EXT_DEV_MODE, null), false);
		}

		public boolean getDevMode() {
			return LangUtils.value(getProperty(PROP_DEV_MODE, null), false);
		}

		public boolean getDebugMode() {
			return LangUtils.value(getProperty(PROP_DEBUG_MODE, null), false);
		}

		public boolean getSchedulerDisabled() {
			return LangUtils.value(getProperty(PROP_SCHEDULER_DISABLED, null), false);
		}
		
		public String getWebappsConfigPath() {
			return LangUtils.value(getProperty(PROP_WEBAPPS_CONFIG_PATH, null), (String)null);
		}

		@Override
		public synchronized Object setProperty(String key, String value) {
			throw new UnsupportedOperationException("Method disabled");
		}
}
