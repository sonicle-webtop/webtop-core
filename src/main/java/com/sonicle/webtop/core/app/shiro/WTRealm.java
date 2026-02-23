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

import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.security.AuthContext;
import com.sonicle.security.AuthPrincipal;
import com.sonicle.security.Principal;
import com.sonicle.security.DomainAccount;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory.AuthUser;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.auth.DirectoryUtils;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserBase;
import com.sonicle.webtop.core.app.model.UserUpdateOption;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WTRealm extends AuthorizingRealm {
	public static final String NAME = "wtrealm";
	private final static Logger LOGGER = (Logger)LoggerFactory.getLogger(WTRealm.class);
	private final KeyedReentrantLocks<String> lockCheckUser = new KeyedReentrantLocks();
	
	public static String toAuthInternetDomain(final String username) {
		return StringUtils.lowerCase(StringUtils.substringAfterLast(username, "@"));
	}
	
	public static String toAuthDomainId(final String username) {
		return StringUtils.substringAfterLast(username, "@");
	}
	
	public static String toAuthLocalUsername(final String username) {
		return StringUtils.substringBeforeLast(username, "@");
	}
	
	public static boolean isSysAdmin(final String internetDomain, final String localUsername) {
		return StringUtils.equals(StringUtils.lowerCase(localUsername), "admin") && StringUtils.isBlank(internetDomain);
	}
	
	public static boolean isAdminImpersonate(final String localUsername) {
		return isSysAdminImpersonate(localUsername) || isDomainAdminImpersonate(localUsername);
	}
	
	public static boolean isSysAdminImpersonate(final String localUsername) {
		return StringUtils.startsWith(StringUtils.lowerCase(localUsername), "admin!");
	}
	
	public static boolean isDomainAdminImpersonate(final String localUsername) {
		return StringUtils.startsWith(StringUtils.lowerCase(localUsername), "admin$");
	}
	
	private String sanitizeImpersonateUsername(String localUsername) {
		String s = StringUtils.removeStart(localUsername, "admin!");
		return StringUtils.removeStart(s, "admin$");
	}
	
	public static SimplePrincipalCollection createPrincipalCollection(final UserProfileId profileId) {
		Check.notNull(profileId, "profileId");
		return new SimplePrincipalCollection(new Principal(profileId.getDomainId(), profileId.getUserId()), WTRealm.NAME);
	}
	
	public static SimplePrincipalCollection createPrincipalCollection(final String domainId, final String userId) {
		Check.notEmpty(domainId, "domainId");
		Check.notEmpty(userId, "userId");
		return new SimplePrincipalCollection(new Principal(domainId, userId), WTRealm.NAME);
	}
	
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
				|| getAuthenticationTokenClass().isAssignableFrom(BearerToken.class)
				|| getAuthenticationTokenClass().isAssignableFrom(AuthTokenRMe.class));
	}
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if (token instanceof UsernamePasswordToken) {
			UsernamePasswordToken upt = (UsernamePasswordToken)token;
			
			String domainId = null;
			if (token instanceof AuthTokenUsernamePasswordDomain) {
				domainId = ((AuthTokenUsernamePasswordDomain)token).getDomain();
			}
			
			String sprincipal = (String)upt.getPrincipal();
			String authInternetDomain = toAuthInternetDomain(sprincipal);
			String localUsername = toAuthLocalUsername(sprincipal);
			
			LOGGER.trace("doGetAuthenticationInfo [UsernamePasswordDomain, {}, {}, {}]", domainId, authInternetDomain, localUsername);
			Principal principal = authenticateUser(authInternetDomain, domainId, localUsername, upt.getPassword());
			
			// Update token with new values resulting from authentication
			//FIXME: still useful???
			if (token instanceof AuthTokenUsernamePasswordDomain) {
				((AuthTokenUsernamePasswordDomain)token).setDomain(principal.getDomainId());
			}
			upt.setUsername(principal.getUserId());
			
			return new WTAuthenticationInfo(principal, upt.getPassword(), this.getName());
		
		} else if (token instanceof BearerToken) {
			BearerToken bt = (BearerToken)token;
			
			String authInternetDomain = null;
			String localUsername = null;
			if (token instanceof AuthTokenBearer) {
				DomainAccount authAccount = DomainAccount.parseQuietly(((AuthTokenBearer)token).getAuthUsername());
				if (authAccount != null) {
					authInternetDomain = authAccount.getDomain();
					localUsername = authAccount.getLocal();
				}
			}
			
			LOGGER.trace("doGetAuthenticationInfo [Bearer, {}, {}]", authInternetDomain, localUsername);
			Principal principal = authenticateApiUser(authInternetDomain, localUsername, bt.getToken());
			return new WTAuthenticationInfo(principal, bt.getToken().toCharArray(), this.getName());
			
		} else if (token instanceof AuthTokenRMe) {
			AuthTokenRMe rmet = (AuthTokenRMe)token;
			
			String sprincipal = (String)rmet.getPrincipal();
			String authDomainId = toAuthDomainId(sprincipal);
			String localUsername = toAuthLocalUsername(sprincipal);
			
			LOGGER.trace("doGetAuthenticationInfo [RMe, {}, {}]", authDomainId, localUsername);
			Principal principal = rememberUser(authDomainId, localUsername);
			return new WTAuthenticationInfo(principal, rmet.getUsername().toCharArray(), this.getName());
		}
		
		return null;
	}
	
	private Principal authenticateApiUser(final String authInternetDomain, final String localUsername, final String token) throws AuthenticationException {
		if (StringUtils.isBlank(token)) throw new AuthenticationException();
		
		final WebTopApp wta = WebTopApp.getInstance();
		final WebTopManager wtMgr = wta.getWebTopManager();
		
		final String provisioningApiToken = WebTopProps.getProvisioningApiToken(WebTopApp.getInstanceProperties());
		if (provisioningApiToken != null && provisioningApiToken.equals(token)) {
			// Support access leveraging on a pre-shared token, suitable for 
			// provisioning application configuration through APIs.
			final AuthContext acontext = wtMgr.createSysAdminAuthenticationContext();
			Principal principal = new Principal(false, acontext.getDomainId(), WebTopManager.SYSADMIN_USERID);
			principal.setDisplayName(WebTopManager.SYSADMIN_USERID);
			return principal;

		} else {
			try {
				checkMaintenance(wta); // Stop users if system in under maintenance!
				
				// Verify provided ApiKey
				if (!wtMgr.authenticateApiKey(token)) throw new AuthenticationException();
				
				String userDomainId = lookupAuthDomainId(wtMgr, authInternetDomain, null); // This will ensure to get a positive lookup or an exp!
				
				// Prepare Directory
				final AuthContext acontext = wtMgr.lookupAuthenticationContext(userDomainId);
				final AbstractDirectory directory = WebTopManager.getAuthDirectory(acontext);
				final DirectoryOptions opts = DirectoryUtils.createDirectoryOptions(acontext, wta.getConnectionManager());
				
				// Prepare principal for authentication
				final AuthPrincipal authPrincipal = createAuthenticatingPrincipal(acontext, directory, opts, false, localUsername, null);
				
				// Authenticate against directory
				AuthUser authUser = null;
				try {
					// In this case authentication is simply a existence check
					authUser = directory.exist(opts, authPrincipal);
					if (authUser == null) throw new AuthenticationException();
					//TODO: enabled state should be checked?
				} catch (DirectoryException ex1) {
					LOGGER.trace("Unable to chck principal: {}", authPrincipal.toString(), ex1);
					throw new AuthenticationException(ex1);
				}
				
				// If we are here, directory has successfully authenticated the user, provided credentials are valid!
				
				// Now build resulting principal...
				Principal principal = new Principal(false, acontext.getDomainId(), authUser.userId);
				principal.setDisplayName(StringUtils.defaultIfBlank(authUser.displayName, authUser.userId));
				return principal;
				
			} catch (WTException ex) {
				throw new AuthenticationException(ex);
			}
		}
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if (principals == null) throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
		
		try {
			Principal principal = (Principal)principals.getPrimaryPrincipal();
			LOGGER.trace("doGetAuthorizationInfo [{}]", principal);
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
	
	private void checkMaintenance(final WebTopApp wta) throws MaintenanceException {
		if (wta.isInMaintenance()) {
			LOGGER.debug("Maintenance is active, stopping authentication process...");
			throw new MaintenanceException("Maintenance is active. Only sys-admin can login.");
		}
	}
	
	private String lookupAuthDomainId(final WebTopManager wtMgr, final String authInternetDomain, final String authDomainId) throws AuthenticationException {
		String userDomainId = null;
		if (!StringUtils.isBlank(authInternetDomain)) {
			userDomainId = wtMgr.authDomainNameToDomainId(authInternetDomain);
			if (userDomainId == null) {
				LOGGER.debug("Unable to lookup domain ID for '{}': maybe not configured or disabled", authInternetDomain);
				throw new AuthenticationException("Match for internet-name not found or Domain is not enabled");
			}

		} else if (!StringUtils.isBlank(authDomainId)) {
			Boolean enabled = wtMgr.isDomainEnabled(authDomainId);
			if (enabled == null) {
				LOGGER.debug("Domain ID not found '{}': maybe not configured or disabled", authDomainId);
				throw new AuthenticationException("Domain ID not found");
			} else if (!enabled) {
				LOGGER.debug("Required domain ID '{}' is disabled", authDomainId);
				throw new DisabledAccountException("Domain is not enabled");
			}
			userDomainId = authDomainId;

		} else {
			// Both authDomainId and authInternetDomain are empty: this 
			// can occur when no domain suffix (@domainname) is provided 
			// in username field and there are no enabled domains, thus 
			// authDomainId has not a default value.
			LOGGER.debug("No domain for authentication ('authInternetDomain' and 'authDomainId' are both empty)", authDomainId);
			throw new AuthenticationException("No enabled Domains");
		}
		return userDomainId;
	}
	
	private AuthPrincipal createAuthenticatingPrincipal(final AuthContext context, final AbstractDirectory directory, final DirectoryOptions directoryOpts, final boolean impersonate, final String username, final char[] password) {
		final String authUsername = impersonate ? "admin" : directory.sanitizeUsername(directoryOpts, username);
		return new AuthPrincipal(impersonate, context.getDomainId(), authUsername, password);
	}
	
	private Principal authenticateUser(final String authInternetDomain, String authDomainId, final String localUsername, final char[] password) throws AuthenticationException {
		final WebTopApp wta = WebTopApp.getInstance();
		final WebTopManager wtMgr = wta.getWebTopManager();
		
		try {
			// If authDomainId is passed blank, try to autofill it with the ID
			// of the only one enabled domain, if any!
			if (StringUtils.isBlank(authDomainId)) {
				authDomainId = wtMgr.getUniqueEnabledDomainId();
				if (LOGGER.isDebugEnabled() && !StringUtils.isBlank(authDomainId)) {
					LOGGER.debug("authDomainId filled with the only enabled domain [{}]", authDomainId);
				}
			}
			
			AuthContext acontext;
			String userDomainId;
			boolean autoCreate = false, impersonate = false;
			
			// Create the right authenticationDomain according to the 
			// authenticating user, this is needed for building the Principal
			LOGGER.debug("Creating the authentication domain...");
			if (isSysAdmin(authInternetDomain, localUsername)) {
				// Do NOT check for maintenance here: SysAdmins are always allowed to access!
				impersonate = false;
				acontext = wtMgr.createSysAdminAuthenticationContext();
				if (acontext == null) throw new WTException("AuthenticationContext is null");
				userDomainId = acontext.getDomainId();
				LOGGER.debug("AuthenticationDomain for SysAdmin created");
				
			} else {
				checkMaintenance(wta); // Stop users if system in under maintenance!
				userDomainId = lookupAuthDomainId(wtMgr, authInternetDomain, authDomainId);
				
				if (isSysAdminImpersonate(localUsername)) {
					if (!isImpersonateEnabled(userDomainId)) {
						LOGGER.debug("Impersonating '{}' failed: impersonate is disabled!", sanitizeImpersonateUsername(localUsername));
						throw new AuthenticationException("Impersonation disabled");
					}
					impersonate = true;
					acontext = wtMgr.createSysAdminAuthenticationContext();
					if (acontext == null) throw new WTException("AuthenticationContext is null");
					LOGGER.debug("AuthenticationDomain for User (with SysAdmin impersonate) created");
					
				} else if (isDomainAdminImpersonate(localUsername)) {
					if (!isImpersonateEnabled(userDomainId)) {
						LOGGER.debug("Impersonating '{}' failed: impersonate is disabled!", sanitizeImpersonateUsername(localUsername));
						throw new AuthenticationException("Impersonation disabled");
					}
					impersonate = true;
					acontext = wtMgr.lookupAuthenticationContext(userDomainId);
					if (acontext == null) throw new WTException("AuthenticationContext is null");
					LOGGER.debug("AuthenticationDomain for User (with DomainAdmin impersonate) created");
					
				} else {
					impersonate = false;
					acontext = wtMgr.lookupAuthenticationContext(userDomainId);
					if (acontext == null) throw new WTException("AuthenticationContext is null");
					LOGGER.debug("AuthenticationDomain for User created");
				}
				autoCreate = wtMgr.isDomainUserAutoCreationEnabled(userDomainId);
			}
			
			// Prepare directory
			final AbstractDirectory directory = WebTopManager.getAuthDirectory(acontext);
			final DirectoryOptions opts = DirectoryUtils.createDirectoryOptions(acontext, wta.getConnectionManager());
			
			// Prepare principal for authentication
			final AuthPrincipal authPrincipal = createAuthenticatingPrincipal(acontext, directory, opts, impersonate, localUsername, password);
			
			LOGGER.debug("Authenticating principal [{}, {}]", authPrincipal.getDomainId(), authPrincipal.getUserId());
			AuthUser authUser = null;
			try {
				authUser = directory.authenticate(opts, authPrincipal);
			} catch (DirectoryException ex1) {
				LOGGER.trace("Unable to authenticate principal: {}", authPrincipal.toString(), ex1);
				throw new AuthenticationException(ex1);
			}
			
			// If we are here, directory has successfully authenticated the user, provided credentials are correct!
			
			// Now build the right principal according to impersonate status...
			Principal principal = null;
			if (impersonate) { // User is impersonated
				String impUsername = sanitizeImpersonateUsername(localUsername);
				principal = new Principal(impersonate, userDomainId, impUsername);
				UserProfileId impPid = UserProfileId.from(principal);
				principal.setDisplayName(lookupProfileDisplayName(impPid));
				
				// !!! Impersonation needs that the User is already present, otherwise we cannot continue!
				// Yes, you cannot leverage on User auto-creation feature during impersonation.
				
				// Before continue, make sure user has the expected state...
				// Make sure that the user is effectively there and enabled
				WebTopManager.UserStateResult ustate = wtMgr.checkUserState(impPid.getDomainId(), impPid.getUserId());
				if (!ustate.exist) { // We do NOT check enabled state here, impersonation is allowed on disabled users...
					LOGGER.debug("User not existing, stopping authentication... [{}]", impPid);
					throw new AuthenticationException("User not existing");
				}
				
			} else { // User NOT impersonated (authentication result points to the right userId)
				principal = new Principal(impersonate, userDomainId, authUser.userId);
				UserProfileId userPid = UserProfileId.from(principal);
				principal.setDisplayName(StringUtils.defaultIfBlank(authUser.displayName, authUser.userId));
				
				// Cache credentials
				wtMgr.cacheSecretValue(userPid, WebTopManager.PSVKEY_PPW, password);
				
				// The user MUST exists, before continue make sure is there or throw...
				final String key = principal.getID();
				try {
					lockCheckUser.tryLock(key, 60, TimeUnit.SECONDS);
					WebTopManager.UserStateResult chk = wtMgr.checkUserState(principal.getDomainId(), principal.getUserId());
					if (!chk.exist) {
						LOGGER.debug("Creating user [{}]", principal.getSubjectId());
						Result<User> result = wtMgr.addUser(principal.getDomainId(), authUser.userId, createUserBase(authUser), false, false, null, UserUpdateOption.internalDefaultFlags());
						if (result.hasExceptions()) {
							LOGGER.warn("User configuration may not have been fully completed. Please check log details above. [{}]", principal.getSubjectId());
						}

					} else if (chk.exist && !chk.enabled) {
						LOGGER.debug("User disabled, stopping authentication... [{}]", userPid);
						throw new DisabledAccountException("User disabled");
					}

				} catch (InterruptedException ex) {
					throw new WTException("Unable to acquire lock for checking user [{}]", principal.getSubjectId());
				} finally {
					lockCheckUser.unlock(key);
				}
			}
			
			return principal;
			
		} catch (WTException ex) {
			LOGGER.error("Error performing authentication", ex);
			throw new AuthenticationException(ex);
		}
	}
	
	private Principal rememberUser(final String authDomainId, final String localUsername) throws AuthenticationException {
		final WebTopApp wta = WebTopApp.getInstance();
		final WebTopManager wtMgr = wta.getWebTopManager();
		
		try {
			// Create the right authenticationDomain according to the 
			// authenticating user, this is needed for building the Principal
			LOGGER.debug("Creating the authentication domain...");
			
			checkMaintenance(wta); // Stop users if system in under maintenance!
			String userDomainId = lookupAuthDomainId(wtMgr, null, authDomainId);
			
			AuthContext acontext = wtMgr.lookupAuthenticationContext(userDomainId);
			if (acontext == null) throw new WTException("AuthenticationContext is null");
			LOGGER.debug("AuthenticationDomain for User created");
			
			// Lookup password for authentication
			UserProfileId authPid = new UserProfileId(userDomainId, localUsername);
			char[] password = wtMgr.lookupSecretValue(authPid, WebTopManager.PSVKEY_PPW);
			if (password == null) {
				LOGGER.debug("User credentials are not in cache, stopping authentication... [{}]", authPid);
				throw new AuthenticationException("User credentials are not in cache");
			}
			
			// Prepare directory
			final AbstractDirectory directory = WebTopManager.getAuthDirectory(acontext);
			final DirectoryOptions opts = DirectoryUtils.createDirectoryOptions(acontext, wta.getConnectionManager());
			
			// Prepare principal for authentication
			final AuthPrincipal authPrincipal = createAuthenticatingPrincipal(acontext, directory, opts, false, localUsername, password);
			//CryptoUtils.
			
			LOGGER.debug("Authenticating principal [{}, {}]", authPrincipal.getDomainId(), authPrincipal.getUserId());
			AuthUser authUser = null;
			try {
				authUser = directory.authenticate(opts, authPrincipal);
			} catch (DirectoryException ex1) {
				LOGGER.debug("Unable to authenticate principal: {}", authPrincipal.toString(), ex1);
				throw new AuthenticationException(ex1);
			}
			
			// If we are here, directory has successfully authenticated the user!
			
			// Now build the right principal according to impersonate status...
			Principal principal = new Principal(false, userDomainId, authUser.userId);
			UserProfileId remPid = UserProfileId.from(principal);
			principal.setDisplayName(lookupProfileDisplayName(remPid));
			
			// Before continue, make sure user has the expected state...
			// Make sure that the user is effectively there and enabled
			WebTopManager.UserStateResult ustate = wtMgr.checkUserState(remPid.getDomainId(), remPid.getUserId());
			if (!ustate.exist) {
				LOGGER.debug("User not existing, stopping authentication... [{}]", remPid);
				throw new AuthenticationException("User not existing");

			} else if (!ustate.enabled) {
				LOGGER.debug("User disabled, stopping authentication... [{}]", remPid);
				throw new DisabledAccountException("User disabled");
			}
			
			/*
			// Finally, verify that we have its credential in cache
			if (wtMgr.lookupSecretValue(remPid, WebTopManager.PSVKEY_PPW) == null) {
				LOGGER.debug("User credentials are not in cache, stopping authentication... [{}]", remPid);
				throw new AuthenticationException("User credentials are not in cache");
			}
			*/
			
			return principal;
			
		} catch (WTException ex) {
			LOGGER.error("Error performing authentication", ex);
			throw new AuthenticationException(ex);
		}
	}
	
	private String lookupProfileDisplayName(final UserProfileId pid) {
		UserProfile.Data ud = WT.getProfileData(pid);
		return ud != null ? ud.getDisplayName() : pid.getUserId();
	}
	
	private boolean isImpersonateEnabled(final String domainId) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, domainId);
		return css.isImpersonateEnabled();
	}
	
	private WTAuthorizationInfo loadAuthorizationInfo(Principal principal) throws Exception {
		WebTopApp wta = WebTopApp.getInstance();
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId pid = UserProfileId.from(principal);
		
		if (LOGGER.isTraceEnabled()) LOGGER.trace("Getting AuthorizationInfo for '{}'", principal.toString());
		
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
		for (RoleWithSource role : userRoles) {
			roles.add(role.getRoleUid());
			perms.addAll(wtMgr.getRolePermissionStrings(role.getRoleUid()));
		}
		return new WTAuthorizationInfo(roles, perms);
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
