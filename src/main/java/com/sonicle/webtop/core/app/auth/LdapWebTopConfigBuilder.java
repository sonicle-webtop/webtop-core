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
package com.sonicle.webtop.core.app.auth;

import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.security.auth.directory.AbstractLdapConfigBuilder;
import com.sonicle.webtop.core.app.WebTopApp;

/**
 *
 * @author malbinola
 */
public final class LdapWebTopConfigBuilder extends AbstractLdapConfigBuilder {
	private static final LdapWebTopConfigBuilder BUILDER = new LdapWebTopConfigBuilder();
	private static final String WTA = "wta";
	private static final String PASSWORD_POLICY_SIMILARITY_TOKENSIZE = "passwordPolicySimilarityTokenSize";
	private static final String PASSWORD_POLICY_USERNAMESIMILARITY = "passwordPolicyUsernameSimilarity";
	private static final String PASSWORD_POLICY_COMPLEXITY = "passwordPolicyComplexity";
	private static final String PASSWORD_POLICY_MINLENGTH = "passwordPolicyMinLength";
	private static final String PASSWORD_POLICY_NOCONSECUTIVECHARS = "passwordPolicyNoConsecutiveChars";
	private static final String PASSWORD_POLICY_SIMILARITY_LEVENTHRES = "passwordPolicySimilarityLevenThres";
	public static final String DEFAULT_HOST = "localhost";
	public static final Integer DEFAULT_PORT = 389;
	public static final String DEFAULT_USER_FIRSTNAME_FIELD = "givenName";
	public static final String DEFAULT_USER_LASTNAME_FIELD = "sn";
	public static final String DEFAULT_USER_DISPLAYNAME_FIELD = "cn";
	
	public static LdapWebTopConfigBuilder getInstance() {
		return BUILDER;
	}
	
	public WebTopApp getWebTopApp(DirectoryOptions opts) {
		return (WebTopApp)getParam(opts, WTA);
	}
	
	public void setWebTopApp(DirectoryOptions opts, WebTopApp wta) {
		setParam(opts, WTA, wta);
	}
	
	public Short getPasswordPolicySimilarityLevenThres(DirectoryOptions opts) {
		return (Short)getParam(opts, PASSWORD_POLICY_SIMILARITY_LEVENTHRES);
	}
	
	public void setPasswordPolicySimilarityLevenThres(DirectoryOptions opts, Short passwordPolicySimilarityLevenThres) {
		setParam(opts, PASSWORD_POLICY_SIMILARITY_LEVENTHRES, passwordPolicySimilarityLevenThres);
	}
	
	public Short getPasswordPolicySimilarityTokenSize(DirectoryOptions opts) {
		return (Short)getParam(opts, PASSWORD_POLICY_SIMILARITY_LEVENTHRES);
	}
	
	public void setPasswordPolicySimilarityTokenSize(DirectoryOptions opts, Short passwordPolicySimilarityTokenSize) {
		setParam(opts, PASSWORD_POLICY_SIMILARITY_TOKENSIZE, passwordPolicySimilarityTokenSize);
	}
	
	public Short getPasswordPolicyMinLength(DirectoryOptions opts) {
		return (Short)getParam(opts, PASSWORD_POLICY_MINLENGTH);
	}
	
	public void setPasswordPolicyMinLength(DirectoryOptions opts, Short passwordPolicyMinLength) {
		setParam(opts, PASSWORD_POLICY_MINLENGTH, passwordPolicyMinLength);
	}
	
	public Boolean getPasswordPolicyComplexity(DirectoryOptions opts) {
		return (Boolean)getParam(opts, PASSWORD_POLICY_COMPLEXITY);
	}
	
	public void setPasswordPolicyComplexity(DirectoryOptions opts, Boolean passwordPolicyComplexity) {
		setParam(opts, PASSWORD_POLICY_COMPLEXITY, passwordPolicyComplexity);
	}
	
	public Boolean getPasswordPolicyNoConsecutiveChars(DirectoryOptions opts) {
		return (Boolean)getParam(opts, PASSWORD_POLICY_NOCONSECUTIVECHARS);
	}
	
	public void setPasswordPolicyNoConsecutiveChars(DirectoryOptions opts, Boolean passwordPolicyNoConsecutiveChars) {
		setParam(opts, PASSWORD_POLICY_NOCONSECUTIVECHARS, passwordPolicyNoConsecutiveChars);
	}
	
	public Boolean getPasswordPolicyUsernameSimilarity(DirectoryOptions opts) {
		return (Boolean)getParam(opts, PASSWORD_POLICY_USERNAMESIMILARITY);
	}
	
	public void setPasswordPolicyUsernameSimilarity(DirectoryOptions opts, Boolean passwordPolicyUsernameSimilarity) {
		setParam(opts, PASSWORD_POLICY_USERNAMESIMILARITY, passwordPolicyUsernameSimilarity);
	}
	
	public void setSpecificAdminDn(DirectoryOptions opts, String adminUsername, String internetName) {
		setAdminDn(opts, "cn=" + adminUsername + "," + toDn(internetName));
	}
	
	public void setSpecificLoginDn(DirectoryOptions opts, String internetName) {
		setLoginDn(opts, "ou=people," + toDn(internetName));
	}
	
	public void setSpecificUserDn(DirectoryOptions opts, String internetName) {
		setUserDn(opts, "ou=people," + toDn(internetName));
	}
	
	@Override
	public String getHost(DirectoryOptions opts) {
		return getString(opts, PARAM_HOST, DEFAULT_HOST);
	}
	
	@Override
	public int getPort(DirectoryOptions opts) {
		return getInteger(opts, PARAM_PORT, DEFAULT_PORT);
	}
	
	@Override
	public String getUserFirstnameField(DirectoryOptions opts) {
		return getString(opts, PARAM_USER_FIRSTNAME_FIELD, DEFAULT_USER_FIRSTNAME_FIELD);
	}
	
	@Override
	public String getUserLastnameField(DirectoryOptions opts) {
		return getString(opts, PARAM_USER_LASTNAME_FIELD, DEFAULT_USER_LASTNAME_FIELD);
	}
	
	@Override
	public String getUserDisplayNameField(DirectoryOptions opts) {
		return getString(opts, PARAM_USER_DISPLAYNAME_FIELD, DEFAULT_USER_DISPLAYNAME_FIELD);
	}
}
