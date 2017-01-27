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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.security.ConnectionSecurity;
import com.sonicle.security.auth.directory.ADDirectory;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.LdapDirectory;
import com.sonicle.security.auth.directory.LdapNethDirectory;
import com.sonicle.webtop.core.bol.model.DomainEntity;
import com.sonicle.webtop.core.bol.model.ParamsLdapDirectory;
import java.net.URISyntaxException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malbinola
 */
public class JsDomain {
	public String domainId;
	public String internetName;
	public Boolean enabled;
	public String description;
	public Boolean userAutoCreation;
	public String dirScheme;
	public String dirHost;
	public Integer dirPort;
	public String dirAdmin;
	public String dirPassword;
	public String dirConnSecurity;
	public Boolean dirCaseSensitive;
	public Boolean dirPasswordPolicy;
	public String ldapLoginDn;
	public String ldapLoginFilter;
	public String ldapUserDn;
	public String ldapUserFilter;
	public String ldapUserFirstnameField;
	public String ldapUserLastnameField;
	public String ldapUserDisplayNameField;
	
	public JsDomain() {}
	
	public JsDomain(DomainEntity o) throws URISyntaxException {
		domainId = o.getDomainId();
		internetName = o.getInternetName();
		enabled = o.getEnabled();
		description = o.getDescription();
		userAutoCreation = o.getUserAutoCreation();
		dirScheme = o.getDirUri().getScheme();
		dirHost = o.getDirUri().getHost();
		dirPort = URIUtils.getPort(o.getDirUri());
		dirAdmin = o.getDirAdmin();
		dirPassword = o.getDirPassword();
		dirConnSecurity = StringUtils.defaultIfBlank(EnumUtils.getName(o.getDirConnSecurity()), "null");
		dirCaseSensitive = o.getDirCaseSensitive();
		dirPasswordPolicy = o.getDirPasswordPolicy();
		if (o.getDirParameters() instanceof ParamsLdapDirectory) {
			ParamsLdapDirectory params = (ParamsLdapDirectory)o.getDirParameters();
			ldapLoginDn = params.loginDn;
			ldapLoginFilter = params.loginFilter;
			ldapUserDn = params.userDn;
			ldapUserFilter = params.userFilter;
			ldapUserFirstnameField = params.userFirstnameField;
			ldapUserLastnameField = params.userLastnameField;
			ldapUserDisplayNameField = params.userDisplayNameField;
		} else {
			ldapLoginDn = null;
			ldapLoginFilter = null;
			ldapUserDn = null;
			ldapUserFilter = null;
			ldapUserFirstnameField = null;
			ldapUserLastnameField = null;
			ldapUserDisplayNameField = null;
		}
	}
	
	public static DomainEntity buildDomainEntity(JsDomain js, AbstractDirectory dir) throws URISyntaxException {
		DomainEntity de = new DomainEntity();
		de.setDomainId(js.domainId);
		de.setInternetName(js.internetName);
		de.setEnabled(js.enabled);
		de.setDescription(js.description);
		de.setUserAutoCreation(js.userAutoCreation);
		de.setDirUri(dir.buildUri(js.dirHost, js.dirPort, null));
		de.setDirAdmin(js.dirAdmin);
		de.setDirPassword(js.dirPassword);
		de.setDirConnSecurity(EnumUtils.getEnum(ConnectionSecurity.class, js.dirConnSecurity));
		de.setDirCaseSensitive(js.dirCaseSensitive);
		de.setDirPasswordPolicy(js.dirPasswordPolicy);
		String scheme = de.getDirUri().getScheme();
		if (scheme.equals(LdapDirectory.SCHEME) || scheme.equals(ADDirectory.SCHEME) || scheme.equals(LdapNethDirectory.SCHEME)) {
			ParamsLdapDirectory params =  new ParamsLdapDirectory();
			params.loginDn = js.ldapLoginDn;
			params.loginFilter = js.ldapLoginFilter;
			params.userDn = js.ldapUserDn;
			params.userFilter = js.ldapUserFilter;
			params.userFirstnameField = js.ldapUserFirstnameField;
			params.userLastnameField = js.ldapUserLastnameField;
			params.userDisplayNameField = js.ldapUserDisplayNameField;
			de.setDirParameters(params);
		} else {
			de.setDirParameters(null);
		}
		return de;
	}
}
