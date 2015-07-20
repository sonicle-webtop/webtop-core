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
import com.sonicle.webtop.core.CoreAuthKey;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.SystemManager;
import com.sonicle.webtop.core.WebTopApp;
import com.sonicle.webtop.core.bol.OPermission;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.model.AuthResource;
import com.sonicle.webtop.core.bol.model.Role;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import javax.security.auth.login.LoginException;
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
		
		UsernamePasswordDomainToken upt = (UsernamePasswordDomainToken)token;
		//logger.debug("isRememberMe={}",upt.isRememberMe());
		char[] creds = (char[])token.getCredentials();
		WebTopApp.logger.debug("validating user {}", (String)token.getPrincipal());
		Principal p = login.validateUser((String)token.getPrincipal() + "@" + upt.getDomain(), creds);
		/*
		ArrayList<GroupPrincipal> groups = p.getGroups();
		for(GroupPrincipal group: groups) {
			WebTopApp.logger.debug("user {} is in group {}",p.getSubjectId(),group.getSubjectId());
		}
		*/
		return new WebTopAuthenticationInfo(p, creds, this.getName());
	}
	
	protected WTAuthorizationInfo loadAuthorizationInfo(Principal principal) throws Exception {
		WebTopApp wta = WebTopApp.getInstance();
		SystemManager sysm = wta.getSystemManager();
		
		HashSet<String> roles = new HashSet<>();
		HashSet<String> perms = new HashSet<>();
		
		// Force core private service permission for any principal
		String resource = AuthResource.namespacedName(CoreManifest.ID, CoreAuthKey.RES_SERVICE);
		perms.add(AuthResource.permissionString(resource, CoreAuthKey.ACT_SERVICE_ACCESS, CoreManifest.ID));
		
		List<Role> userRoles = sysm.getRolesForUser(principal.getDomainId(), principal.getUserId(), true, true);
		for(Role role : userRoles) {
			roles.add(role.getId());
			
			List<ORolePermission> rolePerms = sysm.getRolePermissions(principal.getDomainId(), role.getId());
			for(ORolePermission perm : rolePerms) {
				// Generate resource namespaced name:
				// resource "TEST" for service "com.sonicle.webtop.core" 
				// will become "com.sonicle.webtop.core.TEST"
				resource = AuthResource.namespacedName(perm.getServiceId(), perm.getResource());
				// Generate permission string that shiro can undestand 
				// under the form: {resource}:{action}:{instance}
				perms.add(AuthResource.permissionString(resource, perm.getAction(), perm.getInstance()));
			}
		}
		
		return new WTAuthorizationInfo(roles, perms);
	}
}
