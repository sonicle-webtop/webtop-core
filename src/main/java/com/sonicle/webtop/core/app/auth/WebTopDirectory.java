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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.RegexUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.security.CredentialAlgorithm;
import com.sonicle.security.Principal;
import com.sonicle.security.auth.DirectoryException;
import com.sonicle.security.auth.EntryException;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.security.auth.directory.DirectoryOptions;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.OLocalVaultEntry;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.dal.LocalVaultDAO;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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
	public static final String SCHEME = "webtop";
	public static final Pattern PATTERN_USERNAME = Pattern.compile("^" + RegexUtils.MATCH_USERNAME + "$");
	//public static final Pattern PATTERN_PASSWORD_LENGTH = Pattern.compile("^[\\s\\S]{8,128}$");
	public static final Pattern PATTERN_PASSWORD_UALPHA = Pattern.compile(".*[A-Z].*");
	public static final Pattern PATTERN_PASSWORD_LALPHA = Pattern.compile(".*[a-z].*");
	public static final Pattern PATTERN_PASSWORD_NUMBERS = Pattern.compile(".*[0-9].*");
	public static final Pattern PATTERN_PASSWORD_SPECIAL = Pattern.compile(".*[^a-zA-Z0-9].*");
	public static final Pattern PATTERN_PASSWORD_NOCONSECUTIVECHARS = Pattern.compile("^.*(.)\\1.*$");
	
	static final Collection<DirectoryCapability> CAPABILITIES = Collections.unmodifiableCollection(
		EnumSet.of(
			DirectoryCapability.PASSWORD_WRITE,
			DirectoryCapability.USERS_WRITE
		)
	);
	
	@Override
	public WebTopConfigBuilder getConfigBuilder() {
		return WebTopConfigBuilder.getInstance();
	}
	
	@Override
	public String getScheme() {
		return SCHEME;
	}
	
	@Override
	public Collection<DirectoryCapability> getCapabilities() {
		return CAPABILITIES;
	}
	
	@Override
	public URI buildUri(String host, Integer port, String path) throws URISyntaxException {
		// host, port and path can be ignored!
		return new URI(SCHEME, null, "localhost", -1, null, null, null);
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
	public int validatePasswordPolicy(DirectoryOptions opts, String username, char[] password) {
		WebTopConfigBuilder builder = getConfigBuilder();
		final String s = new String(password);
		
		Short minLength = builder.getPasswordPolicyMinLength(opts);
		if (minLength != null && minLength > 0) {
			if (s.length() < minLength) return 1;
		}
		
		boolean complexity = LangUtils.value(builder.getPasswordPolicyComplexity(opts), false);
		if (complexity) {
			int count = 0;
			if (PATTERN_PASSWORD_UALPHA.matcher(s).matches()) count++;
			if (PATTERN_PASSWORD_LALPHA.matcher(s).matches()) count++;
			if (PATTERN_PASSWORD_NUMBERS.matcher(s).matches()) count++;
			if (PATTERN_PASSWORD_SPECIAL.matcher(s).matches()) count++;
			if (count < 3) return 2;
		}
		
		boolean noConsChars = LangUtils.value(builder.getPasswordPolicyNoConsecutiveChars(opts), false);
		if (noConsChars) {
			if (PATTERN_PASSWORD_NOCONSECUTIVECHARS.matcher(s).matches()) {
				return 3;
			}
		}
		
		short similarityLevenThres = LangUtils.value(builder.getPasswordPolicySimilarityLevenThres(opts), (short)5);
		short similarityTokenSize = LangUtils.value(builder.getPasswordPolicySimilarityTokenSize(opts), (short)4);
		boolean usernameSimilarity = LangUtils.value(builder.getPasswordPolicyUsernameSimilarity(opts), false);
		if (usernameSimilarity) {
			if (username == null 
					|| LangUtils.containsSimilarTokens(s, username, similarityTokenSize) 
					|| StringUtils.getLevenshteinDistance(s, username) < similarityLevenThres) return 4;
		}
		
		return 0;
	}
	
	@Override
	public AuthUser authenticate(DirectoryOptions opts, Principal principal) throws DirectoryException {
		WebTopConfigBuilder builder = getConfigBuilder();
		LocalVaultDAO lvdao = LocalVaultDAO.getInstance();
		WebTopApp wta = builder.getWebTopApp(opts);
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			OLocalVaultEntry entry = lvdao.selectByDomainUser(con, principal.getDomainId(), principal.getUserId());
			if (entry == null) throw new DirectoryException("User not found [{0}]", new UserProfileId(principal.getDomainId(), principal.getUserId()).toString());
			
			CredentialAlgorithm algo = CredentialAlgorithm.valueOf(entry.getPasswordType());
			boolean result = CredentialAlgorithm.compare(algo, new String(principal.getPassword()), entry.getPassword());
			if (!result) throw new DirectoryException("Provided password is not valid");
			if (StringUtils.isBlank(new String(principal.getPassword()))) throw new DirectoryException("Cannot authenticate using blank password");
			
			if (algo.equals(CredentialAlgorithm.PLAIN)) {
				logger.debug("Encrypting PLAIN password");
				CredentialAlgorithm newAlgo = CredentialAlgorithm.SHA;
				doUpdatePassword(false, con, principal.getDomainId(), principal.getUserId(), newAlgo, entry.getPassword());
			}
			
			return createUserEntry(principal);
			
		} catch(SQLException | DAOException ex) {
			throw new DirectoryException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public List<AuthUser> listUsers(DirectoryOptions opts, String domainId) throws DirectoryException {
		throw new DirectoryException("Capability not supported");
	}
	
	@Override
	public void addUser(DirectoryOptions opts, String domainId, AuthUser entry) throws EntryException, DirectoryException {
		WebTopConfigBuilder builder = getConfigBuilder();
		LocalVaultDAO lvdao = LocalVaultDAO.getInstance();
		WebTopApp wta = builder.getWebTopApp(opts);
		Connection con = null;
		
		try {
			ensureCapability(DirectoryCapability.USERS_WRITE);
			if(StringUtils.isBlank(entry.userId)) throw new DirectoryException("Missing value for 'userId'");
			
			String uid = sanitizeUsername(opts, entry.userId);
			
			con = wta.getConnectionManager().getConnection();
			OLocalVaultEntry olve = new OLocalVaultEntry();
			olve.setDomainId(domainId);
			olve.setUserId(uid);
			olve.setPasswordType(CredentialAlgorithm.PLAIN.name());
			lvdao.insert(con, olve);
			
		} catch(DAOIntegrityViolationException ex) { 
			throw new EntryException(ex);
		} catch(SQLException | DAOException ex) {
			throw new DirectoryException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateUser(DirectoryOptions opts, String domainId, AuthUser entry) throws DirectoryException {
		// Nothing to update...
	}
	
	@Override
	public void updateUserPassword(DirectoryOptions opts, String domainId, String userId, char[] newPassword) throws DirectoryException {
		updateUserPassword(opts, domainId, userId, null, newPassword);
	}
	
	@Override
	public void updateUserPassword(DirectoryOptions opts, String domainId, String userId, char[] oldPassword, char[] newPassword) throws EntryException, DirectoryException {
		WebTopConfigBuilder builder = getConfigBuilder();
		LocalVaultDAO lvdao = LocalVaultDAO.getInstance();
		WebTopApp wta = builder.getWebTopApp(opts);
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			
			CredentialAlgorithm algo = CredentialAlgorithm.SHA;
			OLocalVaultEntry entry = lvdao.selectByDomainUser(con, domainId, userId);
			
			if (oldPassword != null) {
				if (entry == null) {
					logger.warn("Cannot check oldPassword. Vault entry not found [{}, {}]", domainId, userId);
				} else {
					algo = CredentialAlgorithm.valueOf(entry.getPasswordType());
					boolean result = CredentialAlgorithm.compare(algo, new String(oldPassword), entry.getPassword());
					if(!result) throw new EntryException("Old password does not match the current one");
				}
			}
			
			int ret = doUpdatePassword(false, con, domainId, userId, algo, new String(newPassword));
			if (ret == 0) doUpdatePassword(true, con, domainId, userId, algo, new String(newPassword));
			
		} catch(SQLException | DAOException ex) {
			throw new DirectoryException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteUser(DirectoryOptions opts, String domainId, String userId) throws DirectoryException {
		WebTopConfigBuilder builder = getConfigBuilder();
		LocalVaultDAO lvdao = LocalVaultDAO.getInstance();
		WebTopApp wta = builder.getWebTopApp(opts);
		Connection con = null;
		
		try {
			ensureCapability(DirectoryCapability.USERS_WRITE);
			String uid = sanitizeUsername(opts, userId);
			
			con = wta.getConnectionManager().getConnection();
			lvdao.deleteByDomainUser(con, domainId, uid);
			
		} catch(SQLException | DAOException ex) {
			throw new DirectoryException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	@Override
	public List<String> listGroups(DirectoryOptions opts, String domainId) throws DirectoryException {
		throw new DirectoryException("Capability not supported");
	}
	
	private int doUpdatePassword(boolean insert, Connection con, String domainId, String userId, CredentialAlgorithm algo, String password) throws DAOException {
		return doUpdatePassword(insert, con, domainId, userId, algo.name(), CredentialAlgorithm.encrypt(algo, password));
	}
	
	private int doUpdatePassword(boolean insert, Connection con, String domainId, String userId, String passwordType, String password) throws DAOException {
		LocalVaultDAO lvDao = LocalVaultDAO.getInstance();
		
		if (insert) {
			return lvDao.insert(con, createOLocalVaultEntry(domainId, userId, passwordType, password));
		} else {
			return lvDao.updatePasswordByDomainUser(con, domainId, userId, passwordType, password);
		}
	}
	
	protected OLocalVaultEntry createOLocalVaultEntry(String domainId, String userId, String passwordType, String password) {
		OLocalVaultEntry olve = new OLocalVaultEntry();
		olve.setDomainId(domainId);
		olve.setUserId(userId);
		olve.setPasswordType(passwordType);
		olve.setPassword(password);
		return olve;
	}
	
	protected AuthUser createUserEntry(Principal principal) {
		AuthUser userEntry = new AuthUser();
		userEntry.userId = principal.getUserId();
		userEntry.firstName = null;
		userEntry.lastName = null;
		userEntry.displayName = principal.getDisplayName();
		return userEntry;
	}
}
