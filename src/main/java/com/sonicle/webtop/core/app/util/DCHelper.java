/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.util;

import com.sonicle.webtop.core.sdk.UserProfileId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 *
 * @author malbinola
 */
public class DCHelper {
	//private static final String VAR_APPNAME = "appname";
	private static final String VAR_REQUEST = "request";
	private static final String VAR_SERVICE = "service";
	private static final String VAR_USER = "user";
	private static final String VAR_USER_SERVICE = "user-service";
	private static final String VAR_AUTO = "auto";
	private static final String VAR_CUSTOM = "custom";
	//private static final String DEFAULT_APPNAME = "noapp";
	private static final String DEFAULT_SERVICE = "core";
	private static final String DEFAULT_USER = "system";
	
	public static String getServiceVariable() {
		return StringUtils.defaultIfBlank(MDC.get(VAR_SERVICE), DEFAULT_SERVICE);
	}
	
	public static String getUserVariable() {
		return StringUtils.defaultIfBlank(MDC.get(VAR_USER), DEFAULT_USER);
	}
	
	public static String getUserServiceVariable() {
		return StringUtils.defaultString(MDC.get(VAR_USER_SERVICE));
	}
	
	public static String getCustomVariable() {
		return StringUtils.defaultIfBlank(MDC.get(VAR_CUSTOM), null);
	}
	
	public static void initDC() {
		internalClearDC();
	}
	
	public static void clearDC() {
		internalClearDC();
	}
	
	public static void setRequestDC(final String request) {
		MDC.put(VAR_REQUEST, request);
		internalSetAutoDC();
	}
	
	public static void clearRequestDC() {
		MDC.remove(VAR_REQUEST);
		internalSetAutoDC();
	}
	
	public static void setContextDC(final UserProfileId profile) {
		MDC.put(VAR_USER, profile.getUserId());
		internalSetAutoDC();
	}
	
	public static void setContextDC(final String service) {
		MDC.put(VAR_SERVICE, service);
		internalSetAutoDC();
	}
	
	public static void setContextDC(final UserProfileId profile, final String service) {
		MDC.put(VAR_USER, profile.getUserId());
		MDC.put(VAR_SERVICE, service);
		internalSetAutoDC();
	}
	
	public static void clearContextServiceDC() {
		MDC.put(VAR_SERVICE, DEFAULT_SERVICE);
		MDC.remove(VAR_CUSTOM);
		internalSetAutoDC();
	}
	
	public static void setCustomDC(final String custom) {
		MDC.put(VAR_CUSTOM, custom);
		internalSetAutoDC();
	}
	
	public static void clearCustomDC() {
		MDC.remove(VAR_CUSTOM);
		internalSetAutoDC();
	}
	
	/**
	 * Resets variables in diagnostic-context.
	 */
	private static void internalClearDC() {
		MDC.put(VAR_USER, DEFAULT_USER);
		MDC.put(VAR_SERVICE, DEFAULT_SERVICE);
		MDC.remove(VAR_CUSTOM);
		internalSetAutoDC();
	}
	
	/**
	 * Computes values of automatic variables diagnostic-context.
	 */
	private static void internalSetAutoDC() {
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
