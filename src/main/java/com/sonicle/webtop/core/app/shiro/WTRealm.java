/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.shiro;

import com.sonicle.commons.flags.BitFlags;
import com.sonicle.security.Principal;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory.AuthUser;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserBase;
import com.sonicle.webtop.core.app.model.UserGetOption;
import com.sonicle.webtop.core.app.model.UserUpdateOption;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WTRealm extends AuthorizingRealm {
	public static final String NAME = "wtrealm";
	private final static Logger logger = (Logger)LoggerFactory.getLogger(WTRealm.class);
	private final Object lock1 = new Object();
	
	@Override
	public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
		// Exposes clearCachedAuthorizationInfo in order to clear cached data from management code.
		super.clearCachedAuthorizationInfo(principals);
	}
	
	@Override
	public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
		// Exposes clearCachedAuthenticationInfo in order to clear cached data from management code.
		super.clearCachedAuthenticationInfo(principals);
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		// Override default implementation to support both UsernamePasswordToken and BearerToken classes.
		return token != null &&
			(getAuthenticationTokenClass().isAssignableFrom(UsernamePasswordToken.class)
				|| getAuthenticationTokenClass().isAssignableFrom(BearerToken.class));
	}
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if (token instanceof UsernamePasswordToken) {
			UsernamePasswordToken upt = (UsernamePasswordToken)token;
			
			String domainId = null;
			if (token instanceof UsernamePasswordDomainToken) {
				domainId = ((UsernamePasswordDomainToken)token).getDomain();
			}
			//logger.debug("isRememberMe={}",upt.isRememberMe());
			
			String sprincipal = (String)upt.getPrincipal();
			String internetDomain = StringUtils.lowerCase(StringUtils.substringAfterLast(sprincipal, "@"));
			String username = StringUtils.substringBeforeLast(sprincipal, "@");
			logger.trace("doGetAuthenticationInfo [{}, {}, {}]", domainId, internetDomain, username);
			
			Principal principal = authenticateUser(domainId, internetDomain, username, upt.getPassword());
			
			// Update token with new values resulting from authentication
			if (token instanceof UsernamePasswordDomainToken) {
				((UsernamePasswordDomainToken)token).setDomain(principal.getDomainId());
			}
			upt.setUsername(principal.getUserId());
			
			return new WTAuthenticationInfo(principal, upt.getPassword(), this.getName());
		
		} else if (token instanceof BearerToken) {
			BearerToken bt = (BearerToken)token;
			
			Principal principal = authenticateWithToken(bt.getToken());
			if (principal != null) return new WTAuthenticationInfo(principal, bt.getToken().toCharArray(), this.getName());
		}
		return null;
	}
	
	private Principal authenticateWithToken(String token) throws AuthenticationException {
		WebTopApp wta = WebTopApp.getInstance();
		WebTopManager wtMgr = wta.getWebTopManager();
		final String provisioningApiToken = WebTopProps.getProvisioningApiToken(WebTopApp.getInstanceProperties());
		
		if (provisioningApiToken != null && provisioningApiToken.equals(token)) {
			// Support access leveraging on a pre-shared token, suitable for 
			// provisioning application configuration through APIs.
			
			try {
				AuthenticationDomain priAd = wtMgr.createSysAdminAuthenticationDomain();
				Principal principal = new Principal(priAd, false, priAd.getDomainId(), WebTopManager.SYSADMIN_USERID, null);
				principal.setDisplayName(WebTopManager.SYSADMIN_USERID);
				return principal;
				
			} catch (URISyntaxException ex) {
				throw new AuthenticationException(ex);
			}
		}
		return null;
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if (principals == null) throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
		
		try {
			Principal principal = (Principal)principals.getPrimaryPrincipal();
			logger.trace("doGetAuthorizationInfo [{}]", principal);
			return loadAuthorizationInfo(principal);
			
		} catch(Exception ex) {
			throw new AuthorizationException(ex);
		}
	}
	
	/*
	 * Protect caching of impersonated authorization from original user authorization cache
	 */
	@Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
		if (principals.getPrimaryPrincipal() instanceof com.sonicle.security.Principal) {
			com.sonicle.security.Principal sprincipal=(com.sonicle.security.Principal)principals.getPrimaryPrincipal();
			if (sprincipal.isImpersonated()) {
				return "admin!"+sprincipal.getUserId()+"@"+sprincipal.getDomainId();
			}
		}
        return principals;
    }

	private Principal authenticateUser(String domainId, String internetDomain, String username, char[] password) throws AuthenticationException {
		WebTopApp wta = WebTopApp.getInstance();
		WebTopManager wtMgr = wta.getWebTopManager();
		AuthenticationDomain authAd = null, priAd = null;
		boolean autoCreate = false, impersonate = false;
		
		try {
			DirectoryManager dirManager = DirectoryManager.getManager();
			
			// Defines authentication domains for the auth phase and for 
			// building the right principal
			logger.debug("Building the authentication domain");
			if (isSysAdmin(internetDomain, username)) {
				impersonate = false;
				authAd = priAd = wtMgr.createSysAdminAuthenticationDomain();
				
			} else {
				if (wta.isInMaintenance()) throw new MaintenanceException("Maintenance is active. Only sys-admin can login.");
				ODomain domain = null;
				if (!StringUtils.isBlank(internetDomain)) {
					List<ODomain> domains = wtMgr.listByInternetDomain(internetDomain);
					if (domains.isEmpty()) throw new WTException("No enabled domains match specified internet domain [{}]", internetDomain);
					if (domains.size() != 1) throw new WTException("Multiple domains match specified internet domain [{}]", internetDomain);
					domain = domains.get(0);
				} else {
					domain = wtMgr.OLD_getDomain(domainId);
					if ((domain == null) || !domain.getEnabled()) throw new WTException("Domain not found [{}]", domainId);
				}
				
				if (isSysAdminImpersonate(username)) {
					impersonate = true;
					authAd = wtMgr.createSysAdminAuthenticationDomain();
					priAd = wtMgr.createAuthenticationDomain(domain);
				} else if (isDomainAdminImpersonate(username)) {
					impersonate = true;
					authAd = priAd = wtMgr.createAuthenticationDomain(domain);
				} else {
					impersonate = false;
					authAd = priAd = wtMgr.createAuthenticationDomain(domain);
				}
				autoCreate = domain.getUserAutoCreation();
			}
			
			DirectoryOptions opts = wta.createDirectoryOptions(authAd);
			AbstractDirectory directory = dirManager.getDirectory(authAd.getDirUri().getScheme());
			if (directory == null) throw new WTException("Directory not supported [{}]", authAd.getDirUri().getScheme());
			
			// Prepare principal for authentication
			String authUsername = impersonate ? "admin" : directory.sanitizeUsername(opts, username);
			Principal authPrincipal = new Principal(authAd, impersonate, authAd.getDomainId(), authUsername, password);
			logger.debug("Authenticating principal [{}, {}]", authPrincipal.getDomainId(), authPrincipal.getUserId());
			
			AuthUser authUser = null;
			try {
				authUser = directory.authenticate(opts, authPrincipal);
			} catch (DirectoryException ex1) {
				logger.trace("Unable to authenticate principal: {}", authPrincipal.toString(), ex1);
				throw new AuthenticationException(ex1);
			}
			
			// If we are here, directory has successfully authenticated the user, provided credentials are correct!
			
			// Now build the right principal according to impersonate status...
			Principal principal = null;
			if (impersonate) { // User is impersonated
				String impUsername = sanitizeImpersonateUsername(username);
				principal = new Principal(priAd, impersonate, priAd.getDomainId(), impUsername, password);
				
				// !!! Impersonation needs that the User is already present, otherwise we cannot continue!
				// Yes, you cannot leverage on User auto-creation feature during impersonation.
				
				UserProfileId pid = new UserProfileId(principal.getDomainId(), principal.getUserId());
				User user = wtMgr.getUser(pid.getDomainId(), pid.getUserId(), BitFlags.noneOf(UserGetOption.class));
				if (user == null) throw new WTException("User not found [{}]", pid.toString());
				principal.setDisplayName(user.getDisplayName());
				
			} else { // User NOT impersonated (authentication result points to the right userId)
				principal = new Principal(priAd, impersonate, priAd.getDomainId(), authUser.userId, password);
				principal.setDisplayName(StringUtils.defaultIfBlank(authUser.displayName, authUser.userId));
			}
			
			if (autoCreate) principal.pushDirectoryEntry(authUser);
			return principal;
		
		} catch (URISyntaxException | WTException ex) {
			logger.error("Authentication error", ex);
			throw new AuthenticationException(ex);
		}	
	}
	
	public void checkUser(Principal principal) throws WTException {
		WebTopApp wta = WebTopApp.getInstance();
		WebTopManager wtMgr = wta.getWebTopManager();
		AuthUser userEntry = principal.popDirectoryEntry();
		
		//TODO: improve with a keyed lock
		synchronized (lock1) {
			WebTopManager.CheckUserResult chk = wtMgr.checkUser(principal.getDomainId(), principal.getUserId());
			if (!chk.exist) {
				if (userEntry != null) {
					logger.debug("Creating user [{}]", principal.getSubjectId());
					Result<User> result = wtMgr.addUser(principal.getDomainId(), userEntry.userId, createUserBase(userEntry), false, false, null, UserUpdateOption.internalDefaultFlags());
					if (result.hasExceptions()) {
						logger.warn("User configuration may not have been fully completed. Please check log details above. [{}]", principal.getSubjectId());
					}
					
				} else {
					throw new WTException("User does not exist [{}]", principal.getSubjectId());
				}
				
			} else if (chk.exist && !chk.enabled) {
				throw new WTException("User is disabled [{}]", principal.getSubjectId());
			}
		}
	}
	
	private WTAuthorizationInfo loadAuthorizationInfo(Principal principal) throws Exception {
		WebTopApp wta = WebTopApp.getInstance();
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId pid = new UserProfileId(principal.getDomainId(), principal.getUserId());
		
		HashSet<String> roles = new HashSet<>();
		HashSet<String> perms = new HashSet<>();
		
		if (Principal.xisAdmin(pid.toString())) {
			roles.add(WebTopManager.SYSADMIN_ROLESID);
			roles.add(WebTopManager.WTADMIN_ROLESID);
			//perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "SYSADMIN"), ServicePermission.ACTION_ACCESS, "*"));
			//perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), ServicePermission.ACTION_ACCESS, "*"));
		} else if (principal.isImpersonated()) {
			roles.add(WebTopManager.IMPERSONATED_USER_ROLESID);
			//perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), ServicePermission.ACTION_ACCESS, "*"));
		}
		
		// Force core private service permission for any principal
		String authRes = ServicePermission.namespacedName(CoreManifest.ID, "SERVICE");
		perms.add(ServicePermission.permissionString(authRes, ServicePermission.ACTION_ACCESS, CoreManifest.ID));
		
		Set<RoleWithSource> userRoles = wtMgr.getComputedRolesByUser(pid, true, true);
		for(RoleWithSource role : userRoles) {
			roles.add(role.getRoleUid());

			List<ORolePermission> rolePerms = wtMgr.listRolePermissions(role.getRoleUid());
			for(ORolePermission perm : rolePerms) {
				// Generate resource namespaced name:
				// resource "TEST" for service "com.sonicle.webtop.core" 
				// will become "com.sonicle.webtop.core.TEST"
				authRes = ServicePermission.namespacedName(perm.getServiceId(), perm.getKey());
				// Generate permission string that shiro can understand 
				// under the form: {resource}:{action}:{instance}
				perms.add(ServicePermission.permissionString(authRes, perm.getAction(), perm.getInstance()));
			}
		}
		return new WTAuthorizationInfo(roles, perms);
	}
	
	private boolean isSysAdmin(String internetDomain, String username) {
		return StringUtils.equals(StringUtils.lowerCase(username), "admin") && StringUtils.isBlank(internetDomain);
	}
	
	private String sanitizeImpersonateUsername(String username) {
		String s = StringUtils.removeStart(username, "admin!");
		return StringUtils.removeStart(s, "admin$");
	}
	
	private boolean isSysAdminImpersonate(String username) {
		return StringUtils.startsWith(StringUtils.lowerCase(username), "admin!");
	}
	
	private boolean isDomainAdminImpersonate(String username) {
		return StringUtils.startsWith(StringUtils.lowerCase(username), "admin$");
	}
	
	private UserBase createUserBase(AuthUser authUser) {
		UserBase user = new UserBase();
		user.setEnabled(true);
		user.setFirstName(authUser.firstName);
		user.setLastName(authUser.lastName);
		user.setDisplayName(authUser.displayName);
		return user;
	}
}
