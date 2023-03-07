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
import com.sonicle.security.PasswordUtils;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.app.model.LdapDirectoryParams;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malbinola
 */
public class JsDomain {
	public String id;
	public String domainId;
	public Boolean enabled;
	public String displayName;
	public String authDomainName;
	public String domainName;
	public Boolean userAutoCreation;
	public String dirScheme;
	public String dirHost;
	public Integer dirPort;
	public String dirAdmin;
	public String dirPassword;
	public String dirPassword_r;
	public String dirPassword_h;
	public String dirConnSecurity;
	public Boolean dirCaseSensitive;
	public Boolean dirPasswordPolicy;
	public String ldapLoginDn;
	public String ldapLoginFilter;
	public String ldapUserDn;
	public String ldapUserFilter;
	public String ldapUserIdField;
	public String ldapUserFirstnameField;
	public String ldapUserLastnameField;
	public String ldapUserDisplayNameField;
	public Short pwdMinLength;
	public Boolean pwdComplexity;
	public Boolean pwdAvoidConsecutiveChars;
	public Boolean pwdAvoidOldSimilarity;
	public Boolean pwdAvoidUsernameSimilarity;
	public Short pwdExpiration;
	public Boolean pwdVerifyAtLogin;
	
	public JsDomain() {}
	
	public JsDomain(Domain item) {
		this.id = item.getDomainId();
		this.domainId = item.getDomainId();
		this.enabled = item.getEnabled();
		this.displayName = item.getDisplayName();
		this.authDomainName = item.getAuthDomainName();
		this.domainName = item.getDomainName();
		this.userAutoCreation = item.getUserAutoCreation();
		this.dirScheme = item.getDirUri().getScheme();
		this.dirHost = item.getDirUri().getHost();
		this.dirPort = URIUtils.getPort(item.getDirUri());
		this.dirAdmin = item.getDirAdmin();
		final String[] redacted = PasswordUtils.redact(item.getDirPassword());
		this.dirPassword = redacted[0];
		this.dirPassword_r = redacted[0];
		this.dirPassword_h = redacted[1];
		this.dirConnSecurity = StringUtils.defaultIfBlank(EnumUtils.getName(item.getDirConnSecurity()), "null");
		this.dirCaseSensitive = item.getDirCaseSensitive();
		if (LdapDirectoryParams.class.equals(item.getDirRawParametersClass())) {
			final LdapDirectoryParams params = item.readDirRawParameters(LdapDirectoryParams.class);
			this.ldapLoginDn = params.loginDn;
			this.ldapLoginFilter = params.loginFilter;
			this.ldapUserDn = params.userDn;
			this.ldapUserFilter = params.userFilter;
			this.ldapUserIdField = params.userIdField;
			this.ldapUserFirstnameField = params.userFirstnameField;
			this.ldapUserLastnameField = params.userLastnameField;
			this.ldapUserDisplayNameField = params.userDisplayNameField;
		} else {
			this.ldapLoginDn = null;
			this.ldapLoginFilter = null;
			this.ldapUserDn = null;
			this.ldapUserFilter = null;
			this.ldapUserIdField = null;
			this.ldapUserFirstnameField = null;
			this.ldapUserLastnameField = null;
			this.ldapUserDisplayNameField = null;
		}
		final DomainBase.PasswordPolicies policies = item.getPasswordPolicies();
		if (policies != null) {
			this.pwdMinLength = policies.getMinLength();
			this.pwdComplexity = policies.getComplexity();
			this.pwdAvoidConsecutiveChars = policies.getAvoidConsecutiveChars();
			this.pwdAvoidOldSimilarity = policies.getAvoidOldSimilarity();
			this.pwdAvoidUsernameSimilarity = policies.getAvoidUsernameSimilarity();
			this.pwdExpiration = policies.getExpiration();
			this.pwdVerifyAtLogin = policies.getVerifyAtLogin();
		} else {
			this.pwdMinLength = null;
			this.pwdComplexity = null;
			this.pwdAvoidConsecutiveChars = null;
			this.pwdAvoidOldSimilarity = null;
			this.pwdAvoidUsernameSimilarity = null;
			this.pwdExpiration = null;
			this.pwdVerifyAtLogin = null;
		}
	}
	
	public static DomainBase createDomainForAdd(JsDomain js) {
		DomainBase item = new DomainBase();
		item.setEnabled(js.enabled);
		item.setDisplayName(js.displayName);
		item.setAuthDomainName(js.authDomainName);
		item.setDomainName(js.domainName);
		item.setUserAutoCreation(js.userAutoCreation);
		item.setDirUri(buildDirectoryURI(js.dirScheme, js.dirHost, js.dirPort));
		item.setDirAdmin(js.dirAdmin);
		item.setDirPassword(js.dirPassword);
		item.setDirConnSecurity(EnumUtils.forName(js.dirConnSecurity, ConnectionSecurity.class));
		item.setDirCaseSensitive(js.dirCaseSensitive);
		if (LdapDirectoryParams.class.equals(item.getDirRawParametersClass())) {
			LdapDirectoryParams params = new LdapDirectoryParams();
			params.loginDn = js.ldapLoginDn;
			params.loginFilter = js.ldapLoginFilter;
			params.userDn = js.ldapUserDn;
			params.userFilter = js.ldapUserFilter;
			params.userIdField = js.ldapUserIdField;
			params.userFirstnameField = js.ldapUserFirstnameField;
			params.userLastnameField = js.ldapUserLastnameField;
			params.userDisplayNameField = js.ldapUserDisplayNameField;
			item.writeDirRawParameters(params, LdapDirectoryParams.class);
		} else {
			item.setDirRawParameters(null);
		}
		item.setPasswordPolicies(new DomainBase.PasswordPolicies(
			js.pwdMinLength,
			js.pwdComplexity,
			js.pwdAvoidConsecutiveChars,
			js.pwdAvoidOldSimilarity,
			js.pwdAvoidUsernameSimilarity,
			js.pwdExpiration,
			js.pwdVerifyAtLogin
		));
		return item;
	}
	
	public static CreateForUpdateResult createDomainForUpdate(JsDomain js) {
		boolean passwordChanged = false;
		DomainBase item = new DomainBase();
		item.setEnabled(js.enabled);
		item.setDisplayName(js.displayName);
		item.setAuthDomainName(js.authDomainName);
		item.setDomainName(js.domainName);
		item.setUserAutoCreation(js.userAutoCreation);
		item.setDirUri(buildDirectoryURI(js.dirScheme, js.dirHost, js.dirPort));
		item.setDirAdmin(js.dirAdmin);
		if (StringUtils.isBlank(js.dirPassword_r) || (!StringUtils.equals(js.dirPassword, js.dirPassword_r) && !StringUtils.equals(PasswordUtils.redact(js.dirPassword)[1], js.dirPassword_h))) {
			passwordChanged = true;
			item.setDirPassword(js.dirPassword);
		} else {
			item.setDirPassword(null);
		}
		item.setDirPassword(js.dirPassword);
		item.setDirConnSecurity(EnumUtils.forName(js.dirConnSecurity, ConnectionSecurity.class));
		item.setDirCaseSensitive(js.dirCaseSensitive);
		if (LdapDirectoryParams.class.equals(item.getDirRawParametersClass())) {
			LdapDirectoryParams params = new LdapDirectoryParams();
			params.loginDn = js.ldapLoginDn;
			params.loginFilter = js.ldapLoginFilter;
			params.userDn = js.ldapUserDn;
			params.userFilter = js.ldapUserFilter;
			params.userIdField = js.ldapUserIdField;
			params.userFirstnameField = js.ldapUserFirstnameField;
			params.userLastnameField = js.ldapUserLastnameField;
			params.userDisplayNameField = js.ldapUserDisplayNameField;
			item.writeDirRawParameters(params, LdapDirectoryParams.class);
		} else {
			item.setDirRawParameters(null);
		}
		item.setPasswordPolicies(new DomainBase.PasswordPolicies(
			js.pwdMinLength,
			js.pwdComplexity,
			js.pwdAvoidConsecutiveChars,
			js.pwdAvoidOldSimilarity,
			js.pwdAvoidUsernameSimilarity,
			js.pwdExpiration,
			js.pwdVerifyAtLogin
		));
		return new CreateForUpdateResult(item, passwordChanged);
	}
	
	public static class CreateForUpdateResult {
		public final DomainBase item;
		public final boolean passwordChanged;
		
		public CreateForUpdateResult(DomainBase item, boolean passwordChanged) {
			this.item = item;
			this.passwordChanged = passwordChanged;
		}
	}
	
	private static AbstractDirectory getAuthDirectoryByScheme(String scheme) {
		final AbstractDirectory directory = DirectoryManager.getManager().getDirectory(scheme);
		if (directory == null) throw new WTRuntimeException("Directory not supported [{}]", scheme);
		return directory;
	}
	
	private static URI buildDirectoryURI(String scheme, String host, Integer port) {
		final AbstractDirectory directory = getAuthDirectoryByScheme(scheme);
		try {
			return directory.buildUri(host, port, null);
		} catch (URISyntaxException ex) {
			throw new WTRuntimeException(ex, "Unable to build URI for '{}' [{}, {}]", scheme, host, port);
		}
	}
}
