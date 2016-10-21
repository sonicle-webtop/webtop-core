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

import com.sonicle.security.Principal;
import com.sonicle.security.SonicleLogin;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.AuthManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.login.LoginException;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 *
 * @author malbinola
 */
public class WTRealm extends AuthorizingRealm {
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		
		try {
			WebTopApp.logger.debug("doGetAuthenticationInfo - {}", token.getPrincipal());
			return loadAuthenticationInfo(token);
			
		} catch(Exception ex) {
			throw new AuthorizationException(ex);
		}
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
	
	protected AuthenticationInfo loadAuthenticationInfo(AuthenticationToken token) throws LoginException, SQLException {
		WebTopApp wta = WebTopApp.getInstance();
		SonicleLogin login = new SonicleLogin(wta.getConnectionManager().getDataSource());
		
		if(token instanceof UsernamePasswordDomainToken) {
			UsernamePasswordDomainToken upt = (UsernamePasswordDomainToken)token;
			//logger.debug("isRememberMe={}",upt.isRememberMe());
			char[] password = (char[])token.getCredentials();
			WebTopApp.logger.debug("validating user {}", (String)token.getPrincipal());
			Principal p = login.validateUser((String)token.getPrincipal() + "@" + upt.getDomain(), password);
			/*
			ArrayList<GroupPrincipal> groups = p.getGroups();
			for(GroupPrincipal group: groups) {
				WebTopApp.logger.debug("user {} is in group {}",p.getSubjectId(),group.getSubjectId());
			}
			*/
			return new WebTopAuthenticationInfo(p, password, this.getName());
			
		} else {
			String username = (String)token.getPrincipal();
			char[] password = (char[])token.getCredentials();
			boolean canBeToken = (password.length == 36);
			//TODO: completare l'implementazione aggiungendo la login tramite token
			WebTopApp.logger.debug("validating user {}", username);
			Principal principal = login.validateUser(username, password);
			return new WebTopAuthenticationInfo(principal, password, getName());
		}
	}
	
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
		
		Set<RoleWithSource> userRoles = autm.getRolesByUser(pid, true, true);
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
}
