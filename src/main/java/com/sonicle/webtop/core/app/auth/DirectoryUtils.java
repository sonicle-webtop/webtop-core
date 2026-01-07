/*
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.auth;

import com.sonicle.commons.LangUtils;
import com.sonicle.security.AuthContext;
import com.sonicle.security.auth.directory.ADConfigBuilder;
import com.sonicle.security.auth.directory.ADDirectory;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.security.auth.directory.ImapConfigBuilder;
import com.sonicle.security.auth.directory.ImapDirectory;
import com.sonicle.security.auth.directory.LdapConfigBuilder;
import com.sonicle.security.auth.directory.LdapDirectory;
import com.sonicle.security.auth.directory.LdapNethConfigBuilder;
import com.sonicle.security.auth.directory.LdapNethDirectory;
import com.sonicle.security.auth.directory.SftpConfigBuilder;
import com.sonicle.security.auth.directory.SftpDirectory;
import com.sonicle.security.auth.directory.SmbConfigBuilder;
import com.sonicle.security.auth.directory.SmbDirectory;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.model.ParamsLdapDirectory;
import com.sonicle.webtop.core.sdk.interfaces.IConnectionProvider;
import java.net.URI;
import java.util.Properties;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class DirectoryUtils {
	
	public static DirectoryOptions createDirectoryOptions(final AuthContext context, final IConnectionProvider conProvider) {
		Check.notNull(context, "context");
		DirectoryOptions opts = new DirectoryOptions();
		ParamsLdapDirectory params = null;
		
		URI authUri = context.getDirUri();
		switch (authUri.getScheme()) {
			case WebTopDirectory.SCHEME:
				WebTopConfigBuilder wt = new WebTopConfigBuilder();
				wt.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				wt.setDBConnectionProvider(opts, Check.notNull(conProvider, "conProvider"));
				break;
			case LdapWebTopDirectory.SCHEME:
				LdapWebTopConfigBuilder ldapwt = new LdapWebTopConfigBuilder();
				ldapwt.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				ldapwt.setHost(opts, authUri.getHost());
				ldapwt.setPort(opts, authUri.getPort());
				ldapwt.setConnectionSecurity(opts, context.getDirConnSecurity());
				ldapwt.setSpecificAdminDn(opts, context.getDirAdmin(), context.getInternetName());
				ldapwt.setAdminPassword(opts, context.getDirPassword());
				ldapwt.setSpecificLoginDn(opts, context.getInternetName());
				ldapwt.setSpecificUserDn(opts, context.getInternetName());
				break;
			case LdapDirectory.SCHEME:
				params = LangUtils.deserialize(context.getDirParameters(), new ParamsLdapDirectory(), ParamsLdapDirectory.class);
				LdapConfigBuilder ldap = new LdapConfigBuilder();
				ldap.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				ldap.setHost(opts, authUri.getHost());
				ldap.setPort(opts, authUri.getPort());
				ldap.setConnectionSecurity(opts, context.getDirConnSecurity());
				ldap.setAdminDn(opts, context.getDirAdmin());
				ldap.setAdminPassword(opts, context.getDirPassword());
				if (!StringUtils.isBlank(params.loginDn)) ldap.setLoginDn(opts, params.loginDn);
				if (!StringUtils.isBlank(params.loginFilter)) ldap.setLoginFilter(opts, params.loginFilter);
				if (!StringUtils.isBlank(params.userDn)) ldap.setUserDn(opts, params.userDn);
				if (!StringUtils.isBlank(params.userFilter)) ldap.setUserFilter(opts, params.userFilter);
				if (!StringUtils.isBlank(params.userIdField)) ldap.setUserIdField(opts, params.userIdField);
				if (!StringUtils.isBlank(params.userFirstnameField)) ldap.setUserFirstnameField(opts, params.userFirstnameField);
				if (!StringUtils.isBlank(params.userLastnameField)) ldap.setUserLastnameField(opts, params.userLastnameField);
				if (!StringUtils.isBlank(params.userDisplayNameField)) ldap.setUserDisplayNameField(opts, params.userDisplayNameField);
				break;
			case LdapNethDirectory.SCHEME:
				params = LangUtils.deserialize(context.getDirParameters(), new ParamsLdapDirectory(), ParamsLdapDirectory.class);
				LdapNethConfigBuilder ldapnts = new LdapNethConfigBuilder();
				ldapnts.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				ldapnts.setHost(opts, authUri.getHost());
				ldapnts.setPort(opts, authUri.getPort());
				ldapnts.setConnectionSecurity(opts, context.getDirConnSecurity());
				ldapnts.setAdminDn(opts, context.getDirAdmin());
				ldapnts.setAdminPassword(opts, context.getDirPassword());
				if (!StringUtils.isBlank(params.loginDn)) ldapnts.setLoginDn(opts, params.loginDn);
				if (!StringUtils.isBlank(params.loginFilter)) ldapnts.setLoginFilter(opts, params.loginFilter);
				if (!StringUtils.isBlank(params.userDn)) ldapnts.setUserDn(opts, params.userDn);
				if (!StringUtils.isBlank(params.userFilter)) ldapnts.setUserFilter(opts, params.userFilter);
				if (!StringUtils.isBlank(params.userIdField)) ldapnts.setUserIdField(opts, params.userIdField);
				if (!StringUtils.isBlank(params.userFirstnameField)) ldapnts.setUserFirstnameField(opts, params.userFirstnameField);
				if (!StringUtils.isBlank(params.userLastnameField)) ldapnts.setUserLastnameField(opts, params.userLastnameField);
				if (!StringUtils.isBlank(params.userDisplayNameField)) ldapnts.setUserDisplayNameField(opts, params.userDisplayNameField);
				break;
			case ADDirectory.SCHEME:
				params = LangUtils.deserialize(context.getDirParameters(), new ParamsLdapDirectory(), ParamsLdapDirectory.class);
				ADConfigBuilder adir = new ADConfigBuilder();
				adir.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				adir.setHost(opts, authUri.getHost());
				adir.setPort(opts, authUri.getPort());
				adir.setConnectionSecurity(opts, context.getDirConnSecurity());
				adir.setAdminDn(opts, context.getDirAdmin());
				adir.setAdminPassword(opts, context.getDirPassword());
				if (!StringUtils.isBlank(params.loginDn)) adir.setLoginDn(opts, params.loginDn);
				if (!StringUtils.isBlank(params.loginFilter)) adir.setLoginFilter(opts, params.loginFilter);
				if (!StringUtils.isBlank(params.userDn)) adir.setUserDn(opts, params.userDn);
				if (!StringUtils.isBlank(params.userFilter)) adir.setUserFilter(opts, params.userFilter);
				if (!StringUtils.isBlank(params.userFirstnameField)) adir.setUserFirstnameField(opts, params.userFirstnameField);
				if (!StringUtils.isBlank(params.userLastnameField)) adir.setUserLastnameField(opts, params.userLastnameField);
				if (!StringUtils.isBlank(params.userDisplayNameField)) adir.setUserDisplayNameField(opts, params.userDisplayNameField);
				break;
			case ImapDirectory.SCHEME:
				ImapConfigBuilder imap = new ImapConfigBuilder();
				imap.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				imap.setHost(opts, authUri.getHost());
				imap.setPort(opts, authUri.getPort());
				imap.setConnectionSecurity(opts, context.getDirConnSecurity());
				break;
			case SmbDirectory.SCHEME:
				SmbConfigBuilder smb = new SmbConfigBuilder();
				smb.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				smb.setHost(opts, authUri.getHost());
				smb.setPort(opts, authUri.getPort());
				break;
			case SftpDirectory.SCHEME:
				SftpConfigBuilder sftp = new SftpConfigBuilder();
				sftp.setIsCaseSensitive(opts, context.getDirCaseSensitive());
				sftp.setHost(opts, authUri.getHost());
				sftp.setPort(opts, authUri.getPort());
				break;
		}
		return opts;
	}
	
	public static DirectoryOptions fillDirectoryOptionsPasswordPolicies(final DirectoryOptions opts, final AuthContext context, final DomainBase.PasswordPolicies policies, final Properties wtProps) {
		URI authUri = context.getDirUri();
		switch(authUri.getScheme()) {
			case WebTopDirectory.SCHEME:
				WebTopConfigBuilder wtBuilder = new WebTopConfigBuilder();
				wtBuilder.setPasswordPolicySimilarityLevenThres(opts, WebTopProps.getWTDirectorySimilarityLevenThres(wtProps));
				wtBuilder.setPasswordPolicySimilarityTokenSize(opts, WebTopProps.getWTDirectorySimilarityTokenSize(wtProps));
				wtBuilder.setPasswordPolicyComplexity(opts, policies.getComplexity());
				wtBuilder.setPasswordPolicyMinLength(opts, policies.getMinLength());
				wtBuilder.setPasswordPolicyNoConsecutiveChars(opts, policies.getAvoidConsecutiveChars());
				wtBuilder.setPasswordPolicyUsernameSimilarity(opts, policies.getAvoidUsernameSimilarity());
				break;
			case LdapWebTopDirectory.SCHEME:
				LdapWebTopConfigBuilder lwtBuilder = new LdapWebTopConfigBuilder();
				lwtBuilder.setPasswordPolicySimilarityLevenThres(opts, WebTopProps.getWTDirectorySimilarityLevenThres(wtProps));
				lwtBuilder.setPasswordPolicySimilarityTokenSize(opts, WebTopProps.getWTDirectorySimilarityTokenSize(wtProps));
				lwtBuilder.setPasswordPolicyComplexity(opts, policies.getComplexity());
				lwtBuilder.setPasswordPolicyMinLength(opts, policies.getMinLength());
				lwtBuilder.setPasswordPolicyNoConsecutiveChars(opts, policies.getAvoidConsecutiveChars());
				lwtBuilder.setPasswordPolicyUsernameSimilarity(opts, policies.getAvoidUsernameSimilarity());
				break;
		}
		return opts;
	}
}
