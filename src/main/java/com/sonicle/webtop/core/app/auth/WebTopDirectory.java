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

import com.sonicle.commons.RegexUtils;
import com.sonicle.security.CredentialAlgorithm;
import com.sonicle.security.Principal;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.webtop.core.app.UserManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class WebTopDirectory extends AbstractDirectory {
	private final static Logger logger = (Logger)LoggerFactory.getLogger(WebTopDirectory.class);
	public static final Pattern PATTERN_USERNAME = Pattern.compile("^" + RegexUtils.MATCH_USERNAME + "$");
	public static final Pattern PATTERN_PASSWORD_LENGTH = Pattern.compile("^[\\s\\S]{8,128}$");
	public static final Pattern PATTERN_PASSWORD_ULETTERS = Pattern.compile(".*[A-Z].*");
	public static final Pattern PATTERN_PASSWORD_LLETTERS = Pattern.compile(".*[a-z].*");
	public static final Pattern PATTERN_PASSWORD_NUMBERS = Pattern.compile(".*[0-9].*");
	public static final Pattern PATTERN_PASSWORD_SPECIAL = Pattern.compile(".*[^a-zA-Z0-9].*");
	
	@Override
	public WebTopConfigBuilder getConfigBuilder() {
		return WebTopConfigBuilder.getInstance();
	}
	
	@Override
	public String sanitizeUsername(DirectoryOptions opts, String username) {
		WebTopConfigBuilder builder = getConfigBuilder();
		return builder.getIsCaseSensitive(opts) ? username : StringUtils.lowerCase(username);
	}

	@Override
	public boolean validateUsername(DirectoryOptions opts, String username) {
		return PATTERN_USERNAME.matcher(username).matches();
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
	
	protected UserEntry createUserEntry(OUser ouser) {
		UserEntry userEntry = new UserEntry();
		userEntry.userId = ouser.getUserId();
		userEntry.firstName = null;
		userEntry.lastName = null;
		userEntry.displayName = ouser.getDisplayName();
		return userEntry;
	}

	@Override
	public UserEntry authenticate(DirectoryOptions opts, Principal principal) throws DirectoryException {
		WebTopConfigBuilder builder = getConfigBuilder();
		WebTopApp wta = builder.getWebTopApp(opts);
		
		try {
			UserManager usem = wta.getUserManager();
			UserProfile.Id pid = new UserProfile.Id(principal.getDomainId(), principal.getUserId());
			OUser ouser = usem.getUser(pid);
			if(ouser == null) throw new DirectoryException("User not found [{0}]", pid.toString());
			
			CredentialAlgorithm algo = CredentialAlgorithm.valueOf(ouser.getPasswordType());
			boolean result = CredentialAlgorithm.compare(algo, new String(principal.getPassword()), ouser.getPassword());
			if(!result) throw new DirectoryException("Provided password is not valid");
			
			return createUserEntry(ouser);
			
		} catch(WTException ex) {
			throw new DirectoryException(ex);
		}
	}
	
	@Override
	public void updateUserPassword(DirectoryOptions opts, String userId, char[] newPassword) throws DirectoryException {
		updateUserPassword(opts, userId, null, newPassword);
	}
	
	@Override
	public void updateUserPassword(DirectoryOptions opts, String userId, char[] oldPassword, char[] newPassword) throws DirectoryException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Override
	public List<UserEntry> listUsers(DirectoryOptions opts) throws DirectoryException {
		throw new UnsupportedOperationException("Not supported on this directory");
	}
	
	@Override
	public void addUser(DirectoryOptions opts, UserEntry entry) throws DirectoryException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void updateUser(DirectoryOptions opts, UserEntry entry) throws DirectoryException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void deleteUser(DirectoryOptions opts, String userId) throws DirectoryException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<String> listGroups(DirectoryOptions opts) throws DirectoryException {
		throw new UnsupportedOperationException("Not supported on this directory");
	}
}
