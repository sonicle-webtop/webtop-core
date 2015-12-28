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
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.WT;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.dal.UserDAO;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import javax.mail.internet.InternetAddress;
import net.sf.qualitycheck.Check;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class UserProfile {
	
	private static final Logger logger = WT.getLogger(UserProfile.class);
	private final CoreManager core;
	private final Principal principal;
	private OUser user;
	private UserPersonalInfo personalInfo;
	
	public UserProfile(CoreManager core, Principal principal) {
		this.core = core;
		this.principal = principal;
		
		try {
			logger.debug("Initializing UserProfile");
			loadDetails();
		} catch(Throwable t) {
			logger.error("Unable to initialize UserProfile", t);
			//throw new Exception("Unable to initialize UserProfile", t);
		}
	}
	
	private void loadDetails() throws Exception {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			
			// Retrieves corresponding user using principal details
			user = udao.selectByDomainUser(con, principal.getDomainId(), principal.getUserId());
			if(user == null) throw new WTException("Unable to find a user for principal [{0}, {1}]", principal.getDomainId(), principal.getUserId());
			
			// If necessary, compute secret key and updates it
			if(StringUtils.isEmpty(user.getSecret())) {
				logger.debug("Building new secret key");
				String secret = "0123456789101112";
				try {
					secret = generateSecretKey();
				} catch(NoSuchAlgorithmException ex) { /* Do nothing... */ }
				user.setSecret(secret);
				udao.updateSecretByDomainUser(con, user.getDomainId(), user.getUserId(), secret);
			}
			
			// Retrieves user-info
			UserPersonalInfo upi = core.getUserPersonalInfo(getId());
			if(upi != null) personalInfo = upi;
			
		} catch(Exception ex) {
			DbUtils.closeQuietly(con);
			throw ex;
		}
	}
	
	public void refresh() throws Exception {
		loadDetails();
	}
	
	public UserProfile.Id getId() {
		return new UserProfile.Id(principal.getName());
	}
	
	public String getStringId() {
		return principal.getName();
	}
	
	public String getUserId() {
		return principal.getUserId();
	}
	
	public String getDomainId() {
		return principal.getDomainId();
	}
	
	public String getMailcardId() {
		AuthenticationDomain ad = principal.getAuthenticationDomain();
		return MessageFormat.format("{0}@{1}", principal.getUserId(), ad.getDomain());
	}
	
	public String getSecret() {
		return user.getSecret();
	}
	
	public Locale getLocale() {
		return user.getLocale();
	}
	
	public String getLanguageTag() {
		return user.getLanguageTag();
	}
	
	public DateTimeZone getTimeZone() {
		return user.getTimeZone();
	}
	
	public String getDisplayName() {
		return user.getDisplayName();
	}
	
	public String getEmailAddress() {
		return personalInfo.getEmail();
	}
	
	public String getCompleteEmailAddress() {
		try {
			return new InternetAddress(getEmailAddress(), getDisplayName()).toString();
		} catch(UnsupportedEncodingException ex) {
			logger.error("Unable to build complete email address", ex);
			return null;
		}
	}

	public Principal getPrincipal() {
		return principal;
	}
	
	public static class Data {
		private String displayName;
		private String languageTag;
		private Locale locale;
		private String timezoneId;
		private DateTimeZone timezone;
		private InternetAddress email;
		
		public Data() {}
		
		public Data(OUser user, InternetAddress email) {
			displayName = user.getDisplayName();
			languageTag = user.getLanguageTag();
			locale = user.getLocale();
			timezoneId = user.getTimezone();
			timezone = user.getTimeZone();
			this.email = email;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getLanguageTag() {
			return languageTag;
		}
		
		public Locale getLocale() {
			return locale;
		}

		public String getTimezoneId() {
			return timezoneId;
		}
		
		public DateTimeZone getTimezone() {
			return timezone;
		}
		
		public InternetAddress getEmail() {
			return email;
		}
	}
	
	public static class Id {
		private final String domainId;
		private final String userId;
		
		public Id(String id) {
			String[] tokens = StringUtils.split(id, "@", 2);
			if(tokens.length != 2) throw new WTRuntimeException("Unable to parse specified profileId [{0}]", id);
			this.domainId = tokens[1];
			this.userId = tokens[0];
		}
		
		public Id(String domainId, String userId) {
			this.domainId = Check.notNull(domainId);
			this.userId = Check.notNull(userId);
		}
		
		public String getDomainId() {
			return domainId;
		}
		
		public String getUserId() {
			return userId;
		}
		
		@Override
		public String toString() {
			return userId + "@" + domainId;
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder()
				.append(domainId)
				.append(userId)
				.toHashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Id == false) return false;
			if(this == obj) return true;
			final Id otherObject = (Id) obj;
			return new EqualsBuilder()
				.append(domainId, otherObject.domainId)
				.append(userId, otherObject.userId)
				.isEquals();
		}
	}
	
	private String generateSecretKey() throws NoSuchAlgorithmException {
		byte[] buffer = new byte[80/8];
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.nextBytes(buffer);
		byte[] secretKey = Arrays.copyOf(buffer, 80/8);
		byte[] encodedKey = new Base32().encode(secretKey);
		return new String(encodedKey);
	}
}
