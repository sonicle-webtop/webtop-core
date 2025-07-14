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
package com.sonicle.webtop.core.app.util;

import com.sonicle.commons.concurrent.KeyedReentrantLocks;
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
	private static final KeyedReentrantLocks<String> locks = new KeyedReentrantLocks<>();
	
	/*
	public synchronized static String getAppNameVariable() {
		return StringUtils.defaultString(MDC.get(VAR_APPNAME));
	}
	*/
	
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
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			doClearDC();
			//initDC(StringUtils.defaultIfBlank(WebTopApp.getWebappName(), DEFAULT_APPNAME));
		} finally {
			locks.unlock(tid);
		}
	}
	
	public static void clearDC() {
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			doClearDC();
		} finally {
			locks.unlock(tid);
		}
	}
	
	public static void setContextDC(UserProfileId profile) {
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			MDC.put(VAR_USER, profile.getUserId());
			doUpdateAutoDC();
		} finally {
			locks.unlock(tid);
		}
	}
	
	public static void setContextDC(String service) {
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			MDC.put(VAR_SERVICE, service);
			doUpdateAutoDC();
		} finally {
			locks.unlock(tid);
		}
	}
	
	public static void setContextDC(UserProfileId profile, String service) {
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			MDC.put(VAR_USER, profile.getUserId());
			MDC.put(VAR_SERVICE, service);
			doUpdateAutoDC();
		} finally {
			locks.unlock(tid);
		}
	}
	
	public static void clearContextServiceDC() {
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			MDC.put(VAR_SERVICE, DEFAULT_SERVICE);
			MDC.remove(VAR_CUSTOM);
			doUpdateAutoDC();
		} finally {
			locks.unlock(tid);
		}
	}
	
	public static void setCustomDC(String custom) {
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			MDC.put(VAR_CUSTOM, custom);
			doUpdateAutoDC();
		} finally {
			locks.unlock(tid);
		}
	}
	
	public static void clearCustomDC() {
		final String tid = findCurrentThreadID();
		try {
			locks.lock(tid);
			MDC.remove(VAR_CUSTOM);
			doUpdateAutoDC();
		} finally {
			locks.unlock(tid);
		}
	}
	
	private static String findCurrentThreadID() {
		return String.valueOf(Thread.currentThread().getId());
	}
	
	/**
	 * Resets variables in diagnostic-context.
	 */
	private static void doClearDC() {
		MDC.put(VAR_USER, DEFAULT_USER);
		MDC.put(VAR_SERVICE, DEFAULT_SERVICE);
		MDC.remove(VAR_CUSTOM);
		doUpdateAutoDC();
	}
	
	/**
	 * Computes values of automatic variables diagnostic-context.
	 */
	private static void doUpdateAutoDC() {
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
