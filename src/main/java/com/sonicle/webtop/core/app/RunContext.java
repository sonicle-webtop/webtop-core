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
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfile;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.mgt.SecurityManager;

/**
 *
 * @author malbinola
 */
public class RunContext {
	
	public static Subject getSubject() {
		return SecurityUtils.getSubject();
	}
	
	public static Session getSession() {
		return getSession(getSubject());
	}
	
	public static Session getSession(Subject subject) {
		return (subject == null) ? null : (Session)subject.getSession(false);
	}
	
	public static String getSessionId() {
		Session session = getSession();
		return (session != null) ? session.getId().toString() : null;
	}
	
	public static UserProfile.Id getRunProfileId() {
		return getRunProfileId(getSubject());
	}
	
	public static UserProfile.Id getRunProfileId(Subject subject) {
		if(subject == null) return null;
		if(subject.getPrincipal() == null) return null;
		return new UserProfile.Id(((Principal)subject.getPrincipal()).getName());
	}
	
	public static String getCSRFToken() {
		return getCSRFToken(getSession());
	}
	
	public static String getCSRFToken(Session session) {
		return (session == null) ? null : SessionManager.getCSRFToken(session);
	}
	
	public static WebTopSession getWebTopSession() {
		Session session = getSession();
		return (session == null) ? null : SessionManager.getWebTopSession(session);
	}
	
	public static boolean isPermitted(String serviceId, String key) {
		return isPermitted(getSubject(), serviceId, key);
	}
	
	public static boolean isPermitted(String serviceId, String key, String action) {
		return isPermitted(getSubject(), serviceId, key, action);
	}
	
	public static boolean isPermitted(String serviceId, String key, String action, String instance) {
		return isPermitted(getSubject(), serviceId, key, action, instance);
	}
	
	public static boolean isPermitted(Subject subject, String serviceId, String key) {
		PrincipalCollection principals = subject.getPrincipals();
		return !principals.isEmpty() && isPermitted(principals, serviceId, key);
	}
	
	public static boolean isPermitted(Subject subject, String serviceId, String key, String action) {
		PrincipalCollection principals = subject.getPrincipals();
		return !principals.isEmpty() && isPermitted(principals, serviceId, key, action);
	}
	
	public static boolean isPermitted(Subject subject, String serviceId, String key, String action, String instance) {
		PrincipalCollection principals = subject.getPrincipals();
		return !principals.isEmpty() && isPermitted(principals, serviceId, key, action, instance);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String serviceId, String key) {
		return isPermitted(buildPrincipalCollection(profileId), serviceId, key);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String serviceId, String key, String action) {
		return isPermitted(buildPrincipalCollection(profileId), serviceId, key, action);
	}
	
	public static boolean isPermitted(UserProfile.Id profileId, String serviceId, String key, String action, String instance) {
		return isPermitted(buildPrincipalCollection(profileId), serviceId, key, action, instance);
	}
	
	public static boolean isSysAdmin() {
		return isSysAdmin(getSubject());
	}
	
	public static boolean isWebTopAdmin() {
		return isWebTopAdmin(getSubject());
	}
	
	public static boolean isSysAdmin(Subject subject) {
		PrincipalCollection principals = subject.getPrincipals();
		return !principals.isEmpty() && isSysAdmin(principals);
	}
	
	public static boolean isWebTopAdmin(Subject subject) {
		PrincipalCollection principals = subject.getPrincipals();
		return !principals.isEmpty() && isWebTopAdmin(principals);
	}
	
	public static boolean isSysAdmin(UserProfile.Id profileId) {
		return isSysAdmin(buildPrincipalCollection(profileId));
	}
	
	public static boolean isWebTopAdmin(UserProfile.Id profileId) {
		return isWebTopAdmin(buildPrincipalCollection(profileId));
	}
	
	public static void ensureIsPermitted(String serviceId, String key) throws AuthException {
		ensureIsPermitted(getSubject(), serviceId, key);
	}
	
	public static void ensureIsPermitted(String serviceId, String key, String action) throws AuthException {
		ensureIsPermitted(getSubject(), serviceId, key, action);
	}
	
	public static void ensureIsPermitted(String serviceId, String key, String action, String instance) throws AuthException {
		ensureIsPermitted(getSubject(), serviceId, key, action, instance);
	}
	
	public static void ensureIsPermitted(Subject subject, String serviceId, String key) throws AuthException {
		ensureIsPermitted(subject.getPrincipals(), serviceId, key);
	}
	
	public static void ensureIsPermitted(Subject subject, String serviceId, String key, String action) throws AuthException {
		ensureIsPermitted(subject.getPrincipals(), serviceId, key, action);
	}
	
	public static void ensureIsPermitted(Subject subject, String serviceId, String key, String action, String instance) throws AuthException {
		ensureIsPermitted(subject.getPrincipals(), serviceId, key, action, instance);
	}
	
	public static void ensureIsPermitted(UserProfile.Id profileId, String serviceId, String key) throws AuthException {
		ensureIsPermitted(buildPrincipalCollection(profileId), serviceId, key);
	}
	
	public static void ensureIsPermitted(UserProfile.Id profileId, String serviceId, String key, String action) throws AuthException {
		ensureIsPermitted(buildPrincipalCollection(profileId), serviceId, key, action);
	}
	
	public static void ensureIsPermitted(UserProfile.Id profileId, String serviceId, String key, String action, String instance) throws AuthException {
		ensureIsPermitted(buildPrincipalCollection(profileId), serviceId, key, action, instance);
	}
		
	public static void ensureIsSysAdmin() throws AuthException {
		ensureIsSysAdmin(getSubject());
	}
	
	public static void ensureIsWebTopAdmin() throws AuthException {
		ensureIsWebTopAdmin(getSubject());
	}
	
	public static void ensureIsSysAdmin(Subject subject) throws AuthException {
		ensureIsSysAdmin(subject.getPrincipals());
	}
	
	public static void ensureIsWebTopAdmin(Subject subject) throws AuthException {
		ensureIsWebTopAdmin(subject.getPrincipals());
	}
	
	public static void ensureIsSysAdmin(UserProfile.Id profileId) throws AuthException {
		ensureIsSysAdmin(buildPrincipalCollection(profileId));
	}
	
	public static void ensureIsWebTopAdmin(UserProfile.Id profileId) throws AuthException {
		ensureIsWebTopAdmin(buildPrincipalCollection(profileId));
	}
	
	static PrincipalCollection buildPrincipalCollection(UserProfile.Id pid) {
		Subject subject = getSubject();
		if((subject != null) && pid.equals(getRunProfileId(subject))) {
			return subject.getPrincipals();
		} else {
			Principal principal = new Principal(pid.getDomainId(), pid.getUserId());
			return new SimplePrincipalCollection(principal, "com.sonicle.webtop.core.shiro.WTRealm");
		}
	}
	
	private static boolean isPermitted(PrincipalCollection principals, String serviceId, String key) {
		return isPermitted(principals, serviceId, key, "ACCESS", "*");
	}
	
	private static boolean isPermitted(PrincipalCollection principals, String serviceId, String key, String action) {
		return isPermitted(principals, serviceId, key, action, "*");
	}
	
	private static boolean isPermitted(PrincipalCollection principals, String serviceId, String key, String action, String instance) {
		SecurityManager manager = SecurityUtils.getSecurityManager();
		if(manager.isPermitted(principals, AuthManager.WTADMIN_PSTRING)) return true;
		return manager.isPermitted(principals, ServicePermission.permissionString(ServicePermission.namespacedName(serviceId, key), action, instance));
	}
	
	private static boolean isSysAdmin(PrincipalCollection principals) {
		SecurityManager manager = SecurityUtils.getSecurityManager();
		return manager.isPermitted(principals, AuthManager.SYSADMIN_PSTRING);
	}
	
	private static boolean isWebTopAdmin(PrincipalCollection principals) {
		SecurityManager manager = SecurityUtils.getSecurityManager();
		return manager.isPermitted(principals, AuthManager.WTADMIN_PSTRING);
	}
	
	private static void ensureIsPermitted(PrincipalCollection principals, String serviceId, String key) throws AuthException {
		if(!isPermitted(principals, serviceId, key)) throw new AuthException("ACCESS permission on {0} is required", key);
	}
	
	private static void ensureIsPermitted(PrincipalCollection principals, String serviceId, String key, String action) throws AuthException {
		if(!isPermitted(principals, serviceId, key, action)) throw new AuthException("{0} permission on {1} is required", action, key);
	}
	
	private static void ensureIsPermitted(PrincipalCollection principals, String serviceId, String key, String action, String instance) throws AuthException {
		if(!isPermitted(principals, serviceId, key, action, instance)) throw new AuthException("{0} permission on {1}@{2} is required", action, key, instance);
	}
	
	private static void ensureIsSysAdmin(PrincipalCollection principals) throws AuthException {
		if(!isSysAdmin(principals)) throw new AuthException("SysAdmin is required");
	}
	
	private static void ensureIsWebTopAdmin(PrincipalCollection principals) throws AuthException {
		if(!isWebTopAdmin(principals)) throw new AuthException("WebTopAdmin is required");
	}
}
