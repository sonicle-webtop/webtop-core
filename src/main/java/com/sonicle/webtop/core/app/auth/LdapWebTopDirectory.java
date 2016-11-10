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

import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.security.auth.directory.LdapDirectory;
import static com.sonicle.webtop.core.app.auth.WebTopDirectory.SCHEME;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;
import org.ldaptive.LdapAttribute;

/**
 *
 * @author malbinola
 */
public class LdapWebTopDirectory extends LdapDirectory {
	public static final String SCHEME = "ldapwebtop";
	public static final Pattern PATTERN_PASSWORD_LENGTH = Pattern.compile("^[\\s\\S]{8,128}$");
	public static final Pattern PATTERN_PASSWORD_ULETTERS = Pattern.compile(".*[A-Z].*");
	public static final Pattern PATTERN_PASSWORD_LLETTERS = Pattern.compile(".*[a-z].*");
	public static final Pattern PATTERN_PASSWORD_NUMBERS = Pattern.compile(".*[0-9].*");
	public static final Pattern PATTERN_PASSWORD_SPECIAL = Pattern.compile(".*[^a-zA-Z0-9].*");
	
	public URI buildUri(String host, Integer port) throws URISyntaxException {
		int iport = (port == null) ? LdapWebTopConfigBuilder.DEFAULT_PORT : port;
		return new URI(SCHEME, null, null, iport, LdapWebTopConfigBuilder.DEFAULT_USERS_DN, null, null);
	}
	
	@Override
	public LdapWebTopConfigBuilder getConfigBuilder() {
		return LdapWebTopConfigBuilder.getInstance();
	}
	
	@Override
	public boolean validatePasswordPolicy(DirectoryOptions opts, char[] password) {
		int count = 0;
		final String cs = new String(password);
		if(PATTERN_PASSWORD_LENGTH.matcher(cs).matches()) {
			if(PATTERN_PASSWORD_ULETTERS.matcher(cs).matches()) count++;
			if(PATTERN_PASSWORD_LLETTERS.matcher(cs).matches()) count++;
			if(PATTERN_PASSWORD_NUMBERS.matcher(cs).matches()) count++;
			if(PATTERN_PASSWORD_SPECIAL.matcher(cs).matches()) count++;
		}
		return count >= 3;
	}
	
	@Override
	protected List<LdapAttribute> createLdapAddAttrs(UserEntry userEntry) throws DirectoryException {
		List<LdapAttribute> attrs = super.createLdapAddAttrs(userEntry);
		LdapAttribute objectClass = new LdapAttribute("objectClass");
		objectClass.addStringValue("inetOrgPerson", "top");
		attrs.add(objectClass);
		return attrs;
	}
}
