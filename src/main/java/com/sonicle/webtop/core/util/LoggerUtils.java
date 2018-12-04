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
package com.sonicle.webtop.core.util;

import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.sdk.UserProfileId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 *
 * @author malbinola
 */
public class LoggerUtils {
	//private static final String VAR_APPNAME = "appname";
	private static final String VAR_SERVICE = "service";
	private static final String VAR_USER = "user";
	private static final String VAR_USER_SERVICE = "user-service";
	private static final String VAR_AUTO = "auto";
	private static final String VAR_CUSTOM = "custom";
	private static final String DEFAULT_APPNAME = "noapp";
	private static final String DEFAULT_SERVICE = "core";
	private static final String DEFAULT_USER = "system";
	
	/*
	public synchronized static String getAppNameVariable() {
		return StringUtils.defaultString(MDC.get(VAR_APPNAME));
	}
	*/
	
	public synchronized static String getServiceVariable() {
		return StringUtils.defaultIfBlank(MDC.get(VAR_SERVICE), DEFAULT_SERVICE);
	}
	
	public synchronized static String getUserVariable() {
		return StringUtils.defaultIfBlank(MDC.get(VAR_USER), DEFAULT_USER);
	}
	
	public synchronized static String getUserServiceVariable() {
		return StringUtils.defaultString(MDC.get(VAR_USER_SERVICE));
	}
	
	public synchronized static String getCustomVariable() {
		return StringUtils.defaultIfBlank(MDC.get(VAR_CUSTOM), null);
	}
	
	public synchronized static void initDC() {
		clearDC();
		//initDC(StringUtils.defaultIfBlank(WebTopApp.getWebappName(), DEFAULT_APPNAME));
	}
	
	/*
	public synchronized static void initDC(String appName) {
		MDC.put(VAR_APPNAME, appName);
		clearDC();
	}
	*/
	
	public synchronized static void clearDC() {
		MDC.put(VAR_USER, DEFAULT_USER);
		MDC.put(VAR_SERVICE, DEFAULT_SERVICE);
		MDC.remove(VAR_CUSTOM);
		updateAutoDC();
	}
	
	public synchronized static void setContextDC(UserProfileId profile) {
		MDC.put(VAR_USER, profile.getUserId());
		updateAutoDC();
	}
	
	public synchronized static void setContextDC(String service) {
		MDC.put(VAR_SERVICE, service);
		updateAutoDC();
	}
	
	public synchronized static void setContextDC(UserProfileId profile, String service) {
		MDC.put(VAR_USER, profile.getUserId());
		MDC.put(VAR_SERVICE, service);
		updateAutoDC();
	}
	
	public synchronized static void clearContextServiceDC() {
		MDC.put(VAR_SERVICE, DEFAULT_SERVICE);
		MDC.remove(VAR_CUSTOM);
		updateAutoDC();
	}
	
	public synchronized static void setCustomDC(String custom) {
		MDC.put(VAR_CUSTOM, custom);
		updateAutoDC();
	}
	
	public synchronized static void clearCustomDC() {
		MDC.remove(VAR_CUSTOM);
		updateAutoDC();
	}
	
	/**
	 * Recalculate values of automatic variables in diagnostic context.
	 */
	public synchronized static void updateAutoDC() {
		final String userservice = getServiceVariable() + "-" + getUserVariable();
		MDC.put(VAR_USER_SERVICE, userservice);
		final String custom = getCustomVariable();
		if (custom == null) {
			MDC.put(VAR_AUTO, getUserServiceVariable());
		} else {
			MDC.put(VAR_AUTO, getUserServiceVariable() + "-" + custom);
		}
		//MDC.put("split", MDC.get("appname") + "_" + (StringUtils.isEmpty(custom) ? servuser : servuser + "-" + custom));
	}
}
