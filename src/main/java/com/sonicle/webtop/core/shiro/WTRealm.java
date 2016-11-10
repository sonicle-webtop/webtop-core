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
package com.sonicle.webtop.core.shiro;

import com.sonicle.security.PasswordUtils;
import com.sonicle.security.Principal;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory.UserEntry;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.security.auth.directory.LdapConfigBuilder;
import com.sonicle.security.auth.directory.LdapNethConfigBuilder;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.AuthManager;
import com.sonicle.webtop.core.app.UserManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.auth.WebTopConfigBuilder;
import com.sonicle.webtop.core.app.auth.LdapWebTopConfigBuilder;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
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
	private final static Logger logger = (Logger)LoggerFactory.getLogger(WTRealm.class);
	private final Object lock1 = new Object();
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if(token instanceof UsernamePasswordDomainToken) {
			UsernamePasswordDomainToken upt = (UsernamePasswordDomainToken)token;
			//logger.debug("isRememberMe={}",upt.isRememberMe());
			
			String domainId = upt.getDomain();
			String sprincipal = (String)upt.getPrincipal();
			String internetDomain = StringUtils.lowerCase(StringUtils.substringAfterLast(sprincipal, "@"));
			String username = StringUtils.substringBeforeLast(sprincipal, "@");
			logger.debug("doGetAuthenticationInfo [{}, {}, {}]", domainId, internetDomain, username);
			
			Principal principal = authenticateUser(domainId, internetDomain, username, upt.getPassword());
			return new WebTopAuthenticationInfo(principal, upt.getPassword(), this.getName());
		} else {
			return null;
		}
	}
	
	private boolean isSysAdmin(String internetDomain, String username) {
		return StringUtils.equals(StringUtils.lowerCase(username), "admin") && StringUtils.isBlank(internetDomain);
	}
	
	private Principal authenticateUser(String domainId, String internetDomain, String username, char[] password) throws AuthenticationException {
		WebTopApp wta = WebTopApp.getInstance();
		UserManager usem = wta.getUserManager();
		AuthenticationDomain ad = null;
		boolean autoCreate = false;
		
		try {
			DirectoryManager dirManager = DirectoryManager.getManager();
			
			logger.debug("Building the authentication domain");
			if(isSysAdmin(internetDomain, username)) {
				ad = new AuthenticationDomain("*", null, "webtop://localhost", null, null);
				
			} else {
				ODomain domain = null;
				if(!StringUtils.isBlank(internetDomain)) {
					List<ODomain> domains = usem.listByInternetDomain(internetDomain);
					if(domains.isEmpty()) throw new WTException("No enabled domains match specified internet domain [{0}]", internetDomain);
					if(domains.size() != 1) throw new WTException("Multiple domains match specified internet domain [{0}]", internetDomain);
					domain = domains.get(0);
				} else {
					domain = usem.getDomain(domainId);
					if((domain == null) || !domain.getEnabled()) throw new WTException("Domain not found [{0}]", domainId);
				}
				ad = new AuthenticationDomain(domain);
				autoCreate = domain.getUserAutoCreation();
			}
			
			DirectoryOptions opts = createDirectoryOptions(wta, ad);
			AbstractDirectory directory = dirManager.getDirectory(ad.getAuthUri().getScheme());
			if(directory == null) throw new WTException("Directory not supported [{0}]", ad.getAuthUri().getScheme());
			
			String sntzUsername = directory.sanitizeUsername(opts, username);
			logger.debug("Authenticating principal [{}, {}]", ad.getDomainId(), sntzUsername);
			Principal principal = new Principal(ad, ad.getDomainId(), sntzUsername, password);
			UserEntry userEntry = directory.authenticate(opts, principal);
			principal.setDisplayName(userEntry.displayName);
			
			UserManager.CheckUserResult chk = usem.checkUser(principal.getDomainId(), principal.getUserId());
			if(autoCreate && !chk.exist) {
				logger.debug("Creating user [{}]", principal.getSubjectId());
				synchronized(lock1) {
					usem.addUser(createUserEntity(principal.getDomainId(), userEntry));
				}
			} else if(!chk.exist) {
				throw new WTException("User does not exist [{0}]", principal.getSubjectId());
			} else if(chk.exist && !chk.enabled) {
				throw new WTException("User is disabled [{0}]", principal.getSubjectId());
			}
			
			return principal;
			
		} catch(URISyntaxException | WTException | DirectoryException ex) {
			logger.error("Authentication error", ex);
			throw new AuthenticationException(ex);
		}	
	}
	
	private UserEntity createUserEntity(String domainId, UserEntry userEntry) {
		UserEntity ue = new UserEntity();
		ue.setDomainId(domainId);
		ue.setUserId(userEntry.userId);
		ue.setEnabled(true);
		ue.setFirstName(userEntry.firstName);
		ue.setLastName(userEntry.lastName);
		ue.setDisplayName(userEntry.displayName);
		return ue;
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if(principals == null) throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
		
		try {
			Principal principal = (Principal)principals.getPrimaryPrincipal();
			WebTopApp.logger.debug("doGetAuthorizationInfo - {}", principal);
			return loadAuthorizationInfo(principal);
			
		} catch(Exception ex) {
			throw new AuthorizationException(ex);
		}
	}
	
	
	/*
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		try {
			WebTopApp.logger.debug("doGetAuthenticationInfo - {}", token.getPrincipal());
			return loadAuthenticationInfo(token);
			
		} catch(Exception ex) {
			throw new AuthorizationException(ex);
		}
	}
	*/
	
//	 
//	
//	
//	
//	
//	
//	protected AuthenticationInfo loadAuthenticationInfo(AuthenticationToken token) throws LoginException, SQLException {
//		WebTopApp wta = WebTopApp.getInstance();
//		SonicleLogin login = new SonicleLogin(wta.getConnectionManager().getDataSource());
//		
//		if(token instanceof UsernamePasswordDomainToken) {
//			UsernamePasswordDomainToken upt = (UsernamePasswordDomainToken)token;
//			//logger.debug("isRememberMe={}",upt.isRememberMe());
//			char[] password = (char[])token.getCredentials();
//			WebTopApp.logger.debug("validating user {}", (String)token.getPrincipal());
//			Principal p = login.validateUser((String)token.getPrincipal() + "@" + upt.getDomain(), password);
//			/*
//			ArrayList<GroupPrincipal> groups = p.getGroups();
//			for(GroupPrincipal group: groups) {
//				WebTopApp.logger.debug("user {} is in group {}",p.getSubjectId(),group.getSubjectId());
//			}
//			*/
//			return new WebTopAuthenticationInfo(p, password, this.getName());
//			
//		} else {
//			String username = (String)token.getPrincipal();
//			char[] password = (char[])token.getCredentials();
//			boolean canBeToken = (password.length == 36);
//			//TODO: completare l'implementazione aggiungendo la login tramite token
//			WebTopApp.logger.debug("validating user {}", username);
//			Principal principal = login.validateUser(username, password);
//			return new WebTopAuthenticationInfo(principal, password, getName());
//		}
//	}
	
	protected WTAuthorizationInfo loadAuthorizationInfo(Principal principal) throws Exception {
		WebTopApp wta = WebTopApp.getInstance();
		AuthManager autm = wta.getAuthManager();
		UserProfile.Id pid = new UserProfile.Id(principal.getDomainId(), principal.getUserId());
		
		HashSet<String> roles = new HashSet<>();
		HashSet<String> perms = new HashSet<>();
		
		if(Principal.xisAdmin(pid.toString())) {
			perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "SYSADMIN"), ServicePermission.ACTION_ACCESS, "*"));
			perms.add(ServicePermission.permissionString(ServicePermission.namespacedName(CoreManifest.ID, "WTADMIN"), ServicePermission.ACTION_ACCESS, "*"));
		}
		
		// Force core private service permission for any principal
		String authRes = ServicePermission.namespacedName(CoreManifest.ID, "SERVICE");
		perms.add(ServicePermission.permissionString(authRes, ServicePermission.ACTION_ACCESS, CoreManifest.ID));
		
		Set<RoleWithSource> userRoles = autm.getComputedRolesByUser(pid, true, true);
		for(RoleWithSource role : userRoles) {
			roles.add(role.getRoleUid());

			List<ORolePermission> rolePerms = autm.listRolePermissions(role.getRoleUid());
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
	
	public static DirectoryOptions createDirectoryOptions(WebTopApp wta, AuthenticationDomain ad) {
		DirectoryOptions opts = new DirectoryOptions();
		URI authUri = ad.getAuthUri();
		switch(authUri.getScheme()) {
			case "webtop":
				WebTopConfigBuilder wtbui = new WebTopConfigBuilder();
				wtbui.setWebTopApp(opts, wta);
				break;
			case "ldap":
				LdapConfigBuilder lbui = new LdapConfigBuilder();
				lbui.setHost(opts, authUri.getHost());
				lbui.setPort(opts, authUri.getPort());
				lbui.setUsersDn(opts, authUri.getPath());
				break;
			case "ldapwebtop":
				LdapWebTopConfigBuilder wtlbui = new LdapWebTopConfigBuilder();
				wtlbui.setHost(opts, authUri.getHost());
				wtlbui.setPort(opts, authUri.getPort());
				wtlbui.setBaseDn(opts, LdapConfigBuilder.toDn(ad.getInternetDomain()));
				wtlbui.setAdminUsername(opts, ad.getAuthUsername());
				wtlbui.setAdminPassword(opts, PasswordUtils.decryptDES(new String(ad.getAuthPassword()), "password").toCharArray());
				break;
			case "ldapneth":
				LdapNethConfigBuilder ntlbui = new LdapNethConfigBuilder();
				ntlbui.setHost(opts, authUri.getHost());
				ntlbui.setPort(opts, authUri.getPort());
				ntlbui.setBaseDn(opts, LdapConfigBuilder.toDn(ad.getInternetDomain()));
				ntlbui.setAdminUsername(opts, ad.getAuthUsername());
				ntlbui.setAdminPassword(opts, PasswordUtils.decryptDES(new String(ad.getAuthPassword()), "password").toCharArray());
				break;
		}
		return opts;
	}
}
