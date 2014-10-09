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

import com.sonicle.security.GroupPrincipal;
import com.sonicle.security.Principal;
import com.sonicle.security.SonicleLogin;
import com.sonicle.webtop.core.Manifest;
import com.sonicle.webtop.core.WebTopApp;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.security.auth.login.LoginException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class WebTopRealm extends AuthorizingRealm {
	
	public static final Logger logger = WebTopApp.getLogger(WebTopRealm.class);
	
	SonicleLogin sonicleLogin=null;
	boolean initialized=false;
	
	public WebTopRealm() throws SQLException {
		super();
		WebTopApp wta = WebTopApp.getInstance();
		sonicleLogin = new SonicleLogin(wta.getConnectionManager().getDataSource(Manifest.ID));
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException {
		try {
			UsernamePasswordDomainToken upt=(UsernamePasswordDomainToken)at;
			
			
			
			//logger.debug("isRememberMe={}",upt.isRememberMe());
			char[] creds=(char[])at.getCredentials();
			logger.debug("{}", (String)at.getPrincipal());
			Principal p=sonicleLogin.validateUser((String)at.getPrincipal()+"@"+upt.getDomain(), creds);
			ArrayList<GroupPrincipal> groups=p.getGroups();
			for(GroupPrincipal group: groups) {
				logger.debug("user "+p.getSubjectId()+" is in group "+group.getSubjectId());
			}
			WebTopAuthenticationInfo authinfo=new WebTopAuthenticationInfo(p,creds,this.getName());
			return authinfo;
		} catch(LoginException exc) {
			exc.printStackTrace();
			throw new AuthenticationException(exc.getMessage());
		} catch(RuntimeException rexc) {
			rexc.printStackTrace();
		}
		return null;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
