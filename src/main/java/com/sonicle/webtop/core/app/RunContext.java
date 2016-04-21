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

import com.sonicle.security.Principal;
import com.sonicle.webtop.core.bol.model.AuthResource;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.shiro.WTSubject;
import com.sonicle.webtop.core.util.SessionUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author malbinola
 */
public class RunContext {
	private final String serviceId;
	private final UserProfile.Id profile;
	private final String sessionId;
	
	public RunContext(String serviceId) {
		this.serviceId = serviceId;
		profile = null;
		sessionId = null;
	}
	
	RunContext(String serviceId, UserProfile.Id profile, String sessionId) {
		this.serviceId = serviceId;
		this.profile = profile;
		this.sessionId = sessionId;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public UserProfile.Id getProfileId() {
		return profile;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public static void ensureIsSysAdmin() {
		if(!isSysAdmin()) throw new AuthException("SysAdmin is required");
	}
	
	public static void ensureIsWebTopAdmin() {
		if(!isWebTopAdmin()) throw new AuthException("WebTopAdmin is required");
	}
	
	public static boolean isSysAdmin() {
		WTSubject subject = SessionUtils.getSubject();
		return subject.isPermitted(AuthManager.SYSADMIN_PSTRING);
	}
	
	public static boolean isWebTopAdmin() {
		WTSubject subject = SessionUtils.getSubject();
		return subject.isPermitted(AuthManager.WTADMIN_PSTRING);
	}
	
	public static boolean isPermitted(String authResource) {
		return isPermitted(SessionUtils.getSubject(), authResource);
	}
	
	public static boolean isPermitted(String authResource, String action) {
		return isPermitted(SessionUtils.getSubject(), authResource, action);
	}
	
	public static boolean isPermitted(String authResource, String action, String instance) {
		return isPermitted(SessionUtils.getSubject(), authResource, action, instance);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String authResource) {
		return isPermitted(buildSubject(profileId), authResource);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String authResource, String action) {
		return isPermitted(buildSubject(profileId), authResource, action);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String authResource, String action, String instance) {
		return isPermitted(buildSubject(profileId), authResource, action, instance);
	}
	
	public static boolean isPermitted(Subject subject, String authResource) {
		return isPermitted(subject, authResource, "ACCESS", "*");
	}
	
	public static boolean isPermitted(Subject subject, String authResource, String action) {
		return isPermitted(subject, authResource, action, "*");
	}
	
	public static boolean isPermitted(Subject subject, String authResource, String action, String instance) {
		if(subject.isPermitted(AuthManager.WTADMIN_PSTRING)) return true;
		return subject.isPermitted(AuthResource.permissionString(authResource, action, instance));
	}
	
	private static Subject buildSubject(UserProfile.Id pid) {
		Principal principal = new Principal(pid.getDomainId(), pid.getUserId());
		PrincipalCollection principals = new SimplePrincipalCollection(principal, "com.sonicle.webtop.core.shiro.WTRealm");
		return new Subject.Builder().principals(principals).buildSubject();
	}
}
