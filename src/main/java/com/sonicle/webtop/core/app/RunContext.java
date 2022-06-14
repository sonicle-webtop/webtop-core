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
import com.sonicle.webtop.core.app.shiro.WTRealm;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.subject.WebSubject;

/**
 *
 * @author malbinola
 */
public class RunContext {
	
	public static UserProfileId getSysAdminProfileId() {
		return new UserProfileId(WebTopManager.SYSADMIN_DOMAINID, WebTopManager.SYSADMIN_USERID);
	}
	
	public static UserProfileId buildDomainAdminProfileId(final String domainId) {
		return new UserProfileId(Check.notEmpty(domainId, "domainId"), WebTopManager.DOMAINADMIN_USERID);
	}
	
	public static Subject buildSubject(final SecurityManager securityManager, final UserProfileId profileId) {
		return new Subject.Builder(securityManager)
			.principals(buildPrincipalCollection(profileId.getDomainId(), profileId.getUserId()))
			.buildSubject();
	}
	
	static PrincipalCollection buildPrincipalCollection(final UserProfileId profileId) {
		Subject subject = getSubject();
		if ((subject != null) && profileId.equals(getRunProfileId(subject))) {
			return subject.getPrincipals();
		} else {
			return buildPrincipalCollection(profileId.getDomainId(), profileId.getUserId());
		}
	}
	
	static SimplePrincipalCollection buildPrincipalCollection(final String domainId, final String userId) {
		Principal principal = new Principal(domainId, userId);
		return new SimplePrincipalCollection(principal, WTRealm.NAME);
		//return new SimplePrincipalCollection(principal, "com.sonicle.webtop.core.shiro.WTRealm");
	}
	
	public static WebSubject buildWebSubject(final SecurityManager securityManager, final ServletRequest request, final ServletResponse response, final UserProfileId profileId) {
		WebSubject.Builder builder = new WebSubject.Builder(securityManager, request, response);
		builder.principals(buildPrincipalCollection(profileId.getDomainId(), profileId.getUserId()));
		return builder.buildWebSubject();
	}
	
	/**
	 * Gets the currently executing Subject.
	 * @return The Subject
	 */
	public static Subject getSubject() {
		return SecurityUtils.getSubject();
	}
	
	/**
	 * Gets the currently executing Principal.
	 * @return The Principal
	 */
	public static Principal getPrincipal() {
		return getPrincipal(getSubject());
	}
	
	/**
	 * Extracts the Principal from the specified Subject.
	 * @param subject The Subject to work on it.
	 * @return Subject's Principal
	 */
	public static Principal getPrincipal(final Subject subject) {
		return (subject == null) ? null : (Principal)subject.getPrincipal();
	}
	
	/**
	 * Checks if the currently executing Subject has been impersonated.
	 * @return True if impersonated, false otherwise
	 */
	public static boolean isImpersonated() {
		try {
			return isImpersonated(getSubject());
		} catch (UnavailableSecurityManagerException ex) {
			return false;
		}
	}
	
	/**
	 * Checks if the specified Subject has been been impersonated.
	 * @param subject The Subject to work on it.
	 * @return True if impersonated, false otherwise
	 */
	public static boolean isImpersonated(final Subject subject) {
		if (subject == null) return false;
		Principal principal = (Principal)subject.getPrincipal();
		if (principal == null) return false;
		return principal.isImpersonated();
	}
	
	/**
	 * Gets the profile ID of the currently executing Subject.
	 * @return Subject's profile ID
	 */
	public static UserProfileId getRunProfileId() {
		return getRunProfileId(getSubject());
	}
	
	/**
	 * Gets the profile ID of the specified Subject.
	 * @param subject The Subject to work on it.
	 * @return Subject's profile ID
	 */
	public static UserProfileId getRunProfileId(final Subject subject) {
		Principal principal = getPrincipal(subject);
		return (principal == null) ? null : new UserProfileId(principal.getName());
	}
	
	/**
	 * Checks if the currently executing Subject has the specified role.
	 * @param role Role UID to check.
	 * @return True if role is satisfied, false otherwise
	 */
	public static boolean hasRole(final String role) {
		Subject subject = getSubject();
		return (subject != null) ? hasRole(subject.getPrincipals(), role) : false;
	}
	
	/**
	 * Checks if the currently executing Subject has all the specified roles.
	 * @param roles Role UIDs to check at same time.
	 * @return True if all role are satisfied, false otherwise
	 */
	public static boolean hasAllRoles(final Collection<String> roles) {
		Subject subject = getSubject();
		return (subject != null) ? hasAllRoles(subject.getPrincipals(), roles) : false;
	}
	
	/**
	 * Checks if the passed Subject has the specified role.
	 * @param subject The Subject to check.
	 * @param role Role UID to check.
	 * @return True if role is satisfied, false otherwise
	 */
	public static boolean hasRole(final Subject subject, final String role) {
		return hasRole(subject.getPrincipals(), role);
	}
	
	/**
	 * Checks if the passed Subject has all the specified roles.
	 * @param subject The Subject to check.
	 * @param roles Role UIDs to check at same time.
	 * @return True if all role are satisfied, false otherwise
	 */
	public static boolean hasAllRoles(final Subject subject, final Collection<String> roles) {
		return hasAllRoles(subject.getPrincipals(), roles);
	}
	
	/**
	 * Checks if the passed profile ID has the specified role.
	 * @param profileId The profile ID to check.
	 * @param role Role UID to check.
	 * @return True if role is satisfied, false otherwise
	 */
	public static boolean hasRole(final UserProfileId profileId, final String role) {
		return hasRole(buildPrincipalCollection(profileId), role);
	}
	
	/**
	 * Checks if specified profile ID has all the specified roles.
	 * @param profileId The profile ID to check.
	 * @param roles Role UIDs to check at same time.
	 * @return True if all role are satisfied, false otherwise
	 */
	public static boolean hasAllRoles(final UserProfileId profileId, final Collection<String> roles) {
		return hasAllRoles(buildPrincipalCollection(profileId), roles);
	}
	
	/**
	 * Checks if executing Subject is the System Administrator.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isSysAdmin() {
		return isSysAdmin(getSubject());
	}
	
	/**
	 * Checks if specified Subject is the System Administrator.
	 * @param subject The Subject to be evaluated.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isSysAdmin(final Subject subject) {
		return isSysAdmin(subject.getPrincipals());
	}
	
	/**
	 * Checks if specified Profile refers to System Administrator.
	 * @param profileId The Profile to be evaluated.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isSysAdmin(final UserProfileId profileId) {
		return isSysAdmin(buildPrincipalCollection(profileId));
	}
	
	/**
	 * Checks if executing Subject has role of WebTop Administrator.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isWebTopAdmin() {
		return isWebTopAdmin(getSubject());
	}
	
	/**
	 * Checks if specified Subject has role of WebTop Administrator.
	 * @param subject The Subject to be evaluated.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isWebTopAdmin(final Subject subject) {
		return isWebTopAdmin(subject.getPrincipals());
	}
	
	/**
	 * Checks if specified Profile has role of WebTop Administrator.
	 * @param profileId The Profile to be evaluated.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isWebTopAdmin(final UserProfileId profileId) {
		return isWebTopAdmin(buildPrincipalCollection(profileId));
	}
	
	/**
	 * Make sure that executing Subject is the System Administrator, 
	 * otherwise a dedicated exception is thrown.
	 * @throws AuthException
	 */
	public static void ensureIsSysAdmin() throws AuthException {
		ensureIsSysAdmin(getSubject());
	}
	
	/**
	 * Make sure that specified Subject is the System Administrator, 
	 * otherwise a dedicated exception is thrown.
	 * @param subject The Subject to be evaluated.
	 * @throws AuthException 
	 */
	public static void ensureIsSysAdmin(final Subject subject) throws AuthException {
		ensureIsSysAdmin(subject.getPrincipals());
	}
	
	/**
	 * Make sure that specified Profile is the System Administrator, 
	 * otherwise a dedicated exception is thrown.
	 * @param profileId The Profile to be evaluated.
	 * @throws AuthException 
	 */
	public static void ensureIsSysAdmin(final UserProfileId profileId) throws AuthException {
		ensureIsSysAdmin(buildPrincipalCollection(profileId));
	}
	
	/**
	 * Make sure that executing Subject has role of WebTop Administrator, 
	 * otherwise a dedicated exception is thrown.
	 * @throws AuthException
	 */
	public static void ensureIsWebTopAdmin() throws AuthException {
		ensureIsWebTopAdmin(getSubject());
	}
	
	/**
	 * Make sure that specified Subject has role of WebTop Administrator, 
	 * otherwise a dedicated exception is thrown.
	 * @param subject The Subject to be evaluated.
	 * @throws AuthException 
	 */
	public static void ensureIsWebTopAdmin(final Subject subject) throws AuthException {
		ensureIsWebTopAdmin(subject.getPrincipals());
	}
	
	/**
	 * Make sure that specified Profile has role of WebTop Administrator, 
	 * otherwise a dedicated exception is thrown.
	 * @param profileId The Profile to be evaluated.
	 * @throws AuthException 
	 */
	public static void ensureIsWebTopAdmin(final UserProfileId profileId) throws AuthException {
		ensureIsWebTopAdmin(buildPrincipalCollection(profileId));
	}
	
	/**
	 * Checks if executing Subject has role of WebTop Administrator 
	 * and belongs to specified Domain.
	 * @param domainId The Domain to be evaluated.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isWebTopDomainAdmin(final String domainId) {
		return isWebTopDomainAdmin(getSubject(), domainId);
	}
	
	/**
	 * Checks if passed Subject has role of WebTop Administrator 
	 * and belongs to specified Domain.
	 * @param subject The Subject to be evaluated.
	 * @param domainId The Domain to be evaluated.
	 * @return `true` if evaluation passed, `false` otherwise
	 */
	public static boolean isWebTopDomainAdmin(final Subject subject, final String domainId) {
		Check.notNull(subject, "subject");
		Check.notEmpty(domainId, "domainId");
		return isSysAdmin(subject) || (isWebTopAdmin(subject) && getRunProfileId(subject).hasDomain(domainId));
	}
	
	/**
	 * Make sure that executing Subject has role of WebTop Administrator 
	 * and belongs to specified Domain.
	 * @param domainId The Domain to be evaluated.
	 * @throws AuthException 
	 */
	public static void ensureIsWebTopDomainAdmin(final String domainId) throws AuthException {
		ensureIsWebTopDomainAdmin(getSubject(), domainId);
	}
	
	/**
	 * Make sure that specified Subject has role of WebTop Administrator 
	 * and belongs to specified Domain.
	 * @param subject The Subject to be evaluated.
	 * @param domainId The Domain to be evaluated.
	 * @throws AuthException 
	 */
	public static void ensureIsWebTopDomainAdmin(final Subject subject, final String domainId) throws AuthException {
		Check.notNull(subject, "subject");
		Check.notEmpty(domainId, "domainId");
		ensureIsWebTopAdmin(subject.getPrincipals());
		if (!isSysAdmin(subject) && !getRunProfileId(subject).hasDomain(domainId)) throw new AuthException("WebTopAdmin must belong to '{}' domain", domainId);
	}
	
	public static boolean isPermitted(boolean strict, String ref) {
		String[] tokens = StringUtils.split(ref, "/", 3);
		if (tokens.length < 2) return false;
		if (tokens.length == 2) {
			return isPermitted(strict, tokens[0], tokens[1]);
		} else {
			return isPermitted(strict, tokens[0], tokens[1], tokens[2]);
		}
	}
	
	public static boolean isPermitted(boolean strict, String serviceId, String key) {
		return isPermitted(strict, getSubject(), serviceId, key);
	}
	
	public static boolean isPermitted(boolean strict, String serviceId, String key, String action) {
		return isPermitted(strict, getSubject(), serviceId, key, action);
	}
	
	public static boolean isPermitted(boolean strict, String serviceId, String key, String action, String instance) {
		return isPermitted(strict, getSubject(), serviceId, key, action, instance);
	}
	
	public static boolean isPermitted(boolean strict, Subject subject, String serviceId, String key) {
		return isPermitted(strict, subject.getPrincipals(), serviceId, key);
	}
	
	public static boolean isPermitted(boolean strict, Subject subject, String serviceId, String key, String action) {
		return isPermitted(strict, subject.getPrincipals(), serviceId, key, action);
	}
	
	public static boolean isPermitted(boolean strict, Subject subject, String serviceId, String key, String action, String instance) {
		return isPermitted(strict, subject.getPrincipals(), serviceId, key, action, instance);
	}
	
	public static boolean isPermitted(boolean strict, UserProfileId profileId, String serviceId, String key) {
		return isPermitted(strict, buildPrincipalCollection(profileId), serviceId, key);
	}
	
	public static boolean isPermitted(boolean strict, UserProfileId profileId, String serviceId, String key, String action) {
		return isPermitted(strict, buildPrincipalCollection(profileId), serviceId, key, action);
	}
	
	public static boolean isPermitted(boolean strict, UserProfileId profileId, String serviceId, String key, String action, String instance) {
		return isPermitted(strict, buildPrincipalCollection(profileId), serviceId, key, action, instance);
	}
	
	public static void ensureIsPermitted(boolean strict, String serviceId, String key) throws AuthException {
		ensureIsPermitted(strict, getSubject(), serviceId, key);
	}
	
	public static void ensureIsPermitted(boolean strict, String serviceId, String key, String action) throws AuthException {
		ensureIsPermitted(strict, getSubject(), serviceId, key, action);
	}
	
	public static void ensureIsPermitted(boolean strict, String serviceId, String key, String action, String instance) throws AuthException {
		ensureIsPermitted(strict, getSubject(), serviceId, key, action, instance);
	}
	
	public static void ensureIsPermitted(boolean strict, Subject subject, String serviceId, String key) throws AuthException {
		ensureIsPermitted(strict, subject.getPrincipals(), serviceId, key);
	}
	
	public static void ensureIsPermitted(boolean strict, Subject subject, String serviceId, String key, String action) throws AuthException {
		ensureIsPermitted(strict, subject.getPrincipals(), serviceId, key, action);
	}
	
	public static void ensureIsPermitted(boolean strict, Subject subject, String serviceId, String key, String action, String instance) throws AuthException {
		ensureIsPermitted(strict, subject.getPrincipals(), serviceId, key, action, instance);
	}
	
	public static void ensureIsPermitted(boolean strict, UserProfileId profileId, String serviceId, String key) throws AuthException {
		ensureIsPermitted(strict, buildPrincipalCollection(profileId), serviceId, key);
	}
	
	public static void ensureIsPermitted(boolean strict, UserProfileId profileId, String serviceId, String key, String action) throws AuthException {
		ensureIsPermitted(strict, buildPrincipalCollection(profileId), serviceId, key, action);
	}
	
	public static void ensureIsPermitted(boolean strict, UserProfileId profileId, String serviceId, String key, String action, String instance) throws AuthException {
		ensureIsPermitted(strict, buildPrincipalCollection(profileId), serviceId, key, action, instance);
	}
	
	private static void ensureIsWebTopAdmin(PrincipalCollection principals) throws AuthException {
		if (!isWebTopAdmin(principals)) throw new AuthException("WebTopAdmin is required");
	}
	
	private static boolean isWebTopAdmin(PrincipalCollection principals) {
		return hasRole(principals, WebTopManager.ROLEUID_WTADMIN);
		/*
		if (principals.isEmpty()) return false;
		SecurityManager manager = SecurityUtils.getSecurityManager();
		return manager.isPermitted(principals, WebTopManager.WTADMIN_PSTRING);
		*/
	}
	
	private static void ensureIsSysAdmin(PrincipalCollection principals) throws AuthException {
		if (!isSysAdmin(principals)) throw new AuthException("SysAdmin is required");
	}
	
	private static boolean isSysAdmin(PrincipalCollection principals) {
		return hasRole(principals, WebTopManager.ROLEUID_SYSADMIN);
		/*
		if (principals.isEmpty()) return false;
		SecurityManager manager = SecurityUtils.getSecurityManager();
		return manager.isPermitted(principals, WebTopManager.SYSADMIN_PSTRING);
		*/
	}
	
	private static boolean hasRole(PrincipalCollection principals, String role) {
		return hasRole(SecurityUtils.getSecurityManager(), principals, role);
	}
	
	private static boolean hasRole(SecurityManager manager, PrincipalCollection principals, String role) {
		return manager.hasRole(principals, role);
	}
	
	private static boolean hasAllRoles(PrincipalCollection principals, Collection<String> roles) {
		return hasAllRoles(SecurityUtils.getSecurityManager(), principals, roles);
	}
	
	private static boolean hasAllRoles(SecurityManager manager, PrincipalCollection principals, Collection<String> roles) {
		return manager.hasAllRoles(principals, roles);
	}
	
	private static void ensureIsPermitted(boolean strict, PrincipalCollection principals, String serviceId, String key) throws AuthException {
		if (!isPermitted(strict, principals, serviceId, key)) throw new AuthException("ACCESS permission on {0} is required", key);
	}
	
	private static void ensureIsPermitted(boolean strict, PrincipalCollection principals, String serviceId, String key, String action) throws AuthException {
		if (!isPermitted(strict, principals, serviceId, key, action)) throw new AuthException("{0} permission on {1} is required", action, key);
	}
	
	private static void ensureIsPermitted(boolean strict, PrincipalCollection principals, String serviceId, String key, String action, String instance) throws AuthException {
		if (!isPermitted(strict, principals, serviceId, key, action, instance)) throw new AuthException("{0} permission on {1}@{2} is required", action, key, instance);
	}
	
	private static boolean isPermitted(boolean strict, PrincipalCollection principals, String serviceId, String key) {
		return isPermitted(strict, principals, serviceId, key, "ACCESS", "*");
	}
	
	private static boolean isPermitted(boolean strict, PrincipalCollection principals, String serviceId, String key, String action) {
		return isPermitted(strict, principals, serviceId, key, action, "*");
	}
	
	private static boolean isPermitted(boolean strict, PrincipalCollection principals, String serviceId, String key, String action, String instance) {
		if (principals.isEmpty()) return false;
		SecurityManager manager = SecurityUtils.getSecurityManager();
		if (!strict && isWebTopAdmin(principals)) return true;
		//if (manager.isPermitted(principals, WebTopManager.WTADMIN_PSTRING)) return true;
		return manager.isPermitted(principals, ServicePermission.permissionString(ServicePermission.namespacedName(serviceId, key), action, instance));
	}
}
