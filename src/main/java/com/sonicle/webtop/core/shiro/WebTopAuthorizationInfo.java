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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.security.GroupPrincipal;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.WebTopApp;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;

/**
 *
 * @author gbulfon
 */
public class WebTopAuthorizationInfo implements AuthorizationInfo {
	
	private DataSource ds;
	private Principal principal;
	private Set<String> roles=new LinkedHashSet<>();
	private Set<String> stringPermissions;
    private Set<Permission> objectPermissions;
	
	public WebTopAuthorizationInfo(DataSource ds, Principal p) {
		this.ds=ds;
		this.principal=p;
	}
	
	@Override
	public Collection<String> getRoles() {
		return roles;
	}
	
	@Override
	public Collection<String> getStringPermissions() {
		return stringPermissions;
	}

	@Override
	public Collection<Permission> getObjectPermissions() {
		return objectPermissions;
	}
	
	protected void fillRoles() {
		roles=new LinkedHashSet<>();
		String user_id=principal.getSubjectId();
		
		Connection con=null;
		try {
			con=ds.getConnection();

			/*
			List<OUserRole> uroles=UserRoleDAO.getInstance().selectByUserId(con, principal.getDomainId(),user_id);
			for(OUserRole urole: uroles) {
				String roleId=urole.getRoleId();
				roles.add(roleId);
				WebTopApp.logger.debug("added role {}",roleId);
			}
			*/

			for(GroupPrincipal gp: principal.getGroups()) {
				if (gp.equals("admins")) {
					roles.add("admin");
				}
				/*
				List<OGroupRole> groles=null;GroupRoleDAO.getInstance().selectByGroupId(con, gp.getDomainId(),gp.getSubjectId());
				for(OGroupRole grole: groles) {
					String roleId=grole.getRoleId();
					roles.add(roleId);
					WebTopApp.logger.debug("added role {} from group {}",roleId,gp.getSubjectId());
				}
				*/
			}
		} catch(SQLException exc) {
			WebTopApp.logger.error("error getting roles for user {}@{}",principal.getSubjectId(),principal.getDomainId(),exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	protected void fillStringPermissions() {
		Connection con=null;
		stringPermissions=new LinkedHashSet<>();
		try {
			con=ds.getConnection();
			for(String role: roles) {
				/*
				List<ORolePermission> rperms=RolePermissionDAO.getInstance().selectByRoleId(con, principal.getDomainId(), role);
				for(ORolePermission rperm: rperms) {
					String sperm=rperm.getPermission();
					stringPermissions.add(sperm);
					WebTopApp.logger.debug("added permission {} from role {}",sperm,role);
				}
				*/
			}
		} catch(SQLException exc) {
			WebTopApp.logger.error("error filling permissions for user {}@{}",principal.getSubjectId(),principal.getDomainId(),exc);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

}
