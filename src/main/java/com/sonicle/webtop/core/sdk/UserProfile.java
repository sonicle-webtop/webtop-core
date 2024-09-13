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

import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.security.AuthenticationDomain;
import com.sonicle.security.DomainAccount;
import com.sonicle.security.Principal;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.OUserInfo;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.model.ProfileI18n;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class UserProfile {
	private static final Logger LOGGER = WT.getLogger(UserProfile.class);
	private final CoreManager core;
	private final Principal principal;
	private OUser user;
	
	public UserProfile(CoreManager core, Principal principal) {
		this.core = core;
		this.principal = principal;
		
		try {
			LOGGER.debug("[{}] Initializing UserProfile...", principal.getUserId());
			loadDetails();
			LOGGER.debug("[{}] UserProfile loaded", principal.getUserId());
		} catch(Throwable t) {
			LOGGER.error("Error creating UserProfile", t);
		}
	}
	
	private void loadDetails() throws WTException {
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			UserDAO udao = UserDAO.getInstance();
			
			// Retrieves corresponding user using principal details
			user = udao.selectByDomainUser(con, principal.getDomainId(), principal.getUserId());
			if (user == null) throw new WTException("Unable to find a user for principal [{0}, {1}]", principal.getDomainId(), principal.getUserId());
			
			// If necessary, compute secret key and updates it
			if (StringUtils.isEmpty(user.getSecret())) {
				LOGGER.debug("Building new secret key");
				String secret = "0123456789101112";
				try {
					secret = generateSecretKey();
				} catch(NoSuchAlgorithmException ex) { /* Do nothing... */ }
				user.setSecret(secret);
				udao.updateSecretByProfile(con, user.getDomainId(), user.getUserId(), secret);
			}
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void refresh() throws Exception {
		loadDetails();
	}
	
	public Principal getPrincipal() {
		return principal;
	}
	
	public UserProfileId getId() {
		return new UserProfileId(principal.getName());
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
		return MessageFormat.format("{0}@{1}", principal.getUserId(), ad.getInternetName());
	}
	
	public String getSecret() {
		return user.getSecret();
	}
	
	public Data getData() {
		return WT.getUserData(getId());
	}
	
	public Locale getLocale() {
		return WT.getUserData(getId()).getLocale();
	}
	
	public String getLanguageTag() {
		return WT.getUserData(getId()).getLanguageTag();
	}
	
	public DateTimeZone getTimeZone() {
		return WT.getUserData(getId()).getTimeZone();
	}
	
	public String getDisplayName() {
		return WT.getUserData(getId()).getDisplayName();
	}
	
	public String getProfileEmailAddress() {
		return WT.getUserData(getId()).getProfileEmailAddress();
	}
	
	/**
	 * @deprecated use getPersonalEmailAddress instead
	 */
	@Deprecated
	public String getEmailAddress() {
		return this.getPersonalEmailAddress();
	}
	
	public String getPersonalEmailAddress() {
		return WT.getUserData(getId()).getPersonalEmailAddress();
	}
	
	public String getFullEmailAddress() {
		return WT.getUserData(getId()).getFullEmailAddress();
	}
	
	public static class Data {
		private DomainAccount internetAccount;
		private String displayName;
		private String languageTag;
		private Locale locale;
		private String timezoneId;
		private DateTimeZone timezone;
		private int startDay;
		private String shortDateFormat;
		private String longDateFormat;
		private String shortTimeFormat;
		private String longTimeFormat;
		private InternetAddress profileEmail;
		private InternetAddress personalEmail;
		
		public Data() {}
		
		public Data(DomainAccount internetAccount, String displayName, String languageTag, String timezone, int startDay, String shortDateFormat, String longDateFormat, String shortTimeFormat, String longTimeFormat, InternetAddress profileEmail, InternetAddress personalEmail) {
			this.internetAccount = internetAccount;
			this.displayName = displayName;
			this.languageTag = languageTag;
			this.locale = LangUtils.languageTagToLocale(languageTag);
			this.timezoneId = timezone;
			this.timezone = DateTimeZone.forID(timezone);
			this.startDay = startDay;
			this.shortDateFormat = shortDateFormat;
			this.longDateFormat = longDateFormat;
			this.shortTimeFormat = shortTimeFormat;
			this.longTimeFormat = longTimeFormat;
			this.profileEmail = profileEmail;
			this.personalEmail = personalEmail;
		}
		
		public DomainAccount getInternetAccount() {
			return internetAccount;
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

		public String getTimeZoneId() {
			return timezoneId;
		}
		
		public DateTimeZone getTimeZone() {
			return timezone;
		}
		
		public int getStartDay() {
			return startDay;
		}
		
		public String getShortDateFormat() {
			return shortDateFormat;
		}
		
		public String getLongDateFormat() {
			return longDateFormat;
		}
		
		public String getShortTimeFormat() {
			return shortTimeFormat;
		}
		
		public String getLongTimeFormat() {
			return longTimeFormat;
		}
		
		public InternetAddress getProfileEmail() {
			return profileEmail;
		}
		
		public String getProfileEmailAddress() {
			if (profileEmail == null) return null;
			return profileEmail.getAddress();
		}
		
		public String getProfileFullEmailAddress() {
			return InternetAddressUtils.toFullAddress(profileEmail);
		}
		
		public InternetAddress getPersonalEmail() {
			return personalEmail;
		}
		
		public String getPersonalEmailAddress() {
			if (personalEmail == null) return null;
			return personalEmail.getAddress();
		}
		
		public String getPersonalFullEmailAddress() {
			return InternetAddressUtils.toFullAddress(personalEmail);
		}
		
		public ProfileI18n toProfileI18n() {
			return toProfileI18n(false);
		}
		
		public ProfileI18n toProfileI18n(boolean longDateTimeFormat) {
			if (longDateTimeFormat) {
				return new ProfileI18n(this.getLocale(), this.getTimeZone(), this.getLongDateFormat(), this.getLongTimeFormat());
			} else {
				return new ProfileI18n(this.getLocale(), this.getTimeZone(), this.getShortDateFormat(), this.getShortTimeFormat());
			}
		}
		
		/**
		 * @return 
		 * @deprecated use {@link #getPersonalEmail()} instead.
		 */
		@Deprecated
		public InternetAddress getEmail() {
			return getPersonalEmail();
		}
		
		/**
		 * @return 
		 * @deprecated use {@link #getPersonalEmailAddress()} instead.
		 */
		@Deprecated
		public String getEmailAddress() {
			return getPersonalEmailAddress();
		}
		
		/**
		 * @return 
		 * @deprecated use {@link #getPersonalFullEmailAddress()} instead.
		 */
		@Deprecated
		public String getFullEmailAddress() {
			return getPersonalFullEmailAddress();
		}
	}
	
	public static class PersonalInfo {
		private String title = null;
		private String firstName = null;
		private String lastName = null;
		private String nickname = null;
		private String gender = null;
		private String email = null;
		private String telephone = null;
		private String fax = null;
		private String pager = null;
		private String mobile = null;
		private String address = null;
		private String city = null;
		private String postalCode = null;
		private String state = null;
		private String country = null;
		private String company = null;
		private String function = null;
		private String custom01 = null;
		private String custom02 = null;
		private String custom03 = null;
		
		public PersonalInfo() {}
		
		public PersonalInfo(OUserInfo o) {
			setTitle(o.getTitle());
			setFirstName(o.getFirstName());
			setLastName(o.getLastName());
			setNickname(o.getNickname());
			setGender(o.getGender());
			setEmail(o.getEmail());
			setTelephone(o.getTelephone());
			setFax(o.getFax());
			setPager(o.getPager());
			setMobile(o.getMobile());
			setAddress(o.getAddress());
			setCity(o.getCity());
			setPostalCode(o.getPostalCode());
			setState(o.getState());
			setCountry(o.getCountry());
			setCompany(o.getCompany());
			setFunction(o.getFunction());
			setCustom01(o.getCustom1());
			setCustom02(o.getCustom2());
			setCustom03(o.getCustom3());
		}
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getTelephone() {
			return telephone;
		}

		public void setTelephone(String telephone) {
			this.telephone = telephone;
		}

		public String getFax() {
			return fax;
		}

		public void setFax(String fax) {
			this.fax = fax;
		}

		public String getPager() {
			return pager;
		}

		public void setPager(String pager) {
			this.pager = pager;
		}

		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobile) {
			this.mobile = mobile;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getPostalCode() {
			return postalCode;
		}

		public void setPostalCode(String postalCode) {
			this.postalCode = postalCode;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getCompany() {
			return company;
		}

		public void setCompany(String company) {
			this.company = company;
		}

		public String getFunction() {
			return function;
		}

		public void setFunction(String function) {
			this.function = function;
		}

		public String getCustom01() {
			return custom01;
		}

		public void setCustom01(String custom01) {
			this.custom01 = custom01;
		}

		public String getCustom02() {
			return custom02;
		}

		public void setCustom02(String custom02) {
			this.custom02 = custom02;
		}

		public String getCustom03() {
			return custom03;
		}

		public void setCustom03(String custom03) {
			this.custom03 = custom03;
		}
		
		public Map toMap() {
			HashMap<String, String> map = new HashMap();
			map.put("title", title);
			map.put("firstName", firstName);
			map.put("lastName", lastName);
			map.put("nickname", nickname);
			map.put("gender", gender);
			map.put("email", email);
			map.put("telephone", telephone);
			map.put("fax", fax);
			map.put("pager", pager);
			map.put("mobile", mobile);
			map.put("address", address);
			map.put("city", city);
			map.put("postalCode", postalCode);
			map.put("state", state);
			map.put("country", country);
			map.put("company", company);
			map.put("function", function);
			map.put("custom1", custom01);
			map.put("custom2", custom02);
			map.put("custom3", custom03);
			return map;
		}

		public void setValues(Map<String, Object> map) {
			title = String.valueOf(LangUtils.ifValue(map, "title", title));
			firstName =  String.valueOf(LangUtils.ifValue(map, "firstName", firstName));
			lastName =  String.valueOf(LangUtils.ifValue(map, "lastName", lastName));
			nickname =  String.valueOf(LangUtils.ifValue(map, "nickname", nickname));
			gender =  String.valueOf(LangUtils.ifValue(map, "gender", gender));
			email =  String.valueOf(LangUtils.ifValue(map, "email", email));
			telephone =  String.valueOf(LangUtils.ifValue(map, "telephone", telephone));
			fax =  String.valueOf(LangUtils.ifValue(map, "fax", fax));
			pager =  String.valueOf(LangUtils.ifValue(map, "pager", pager));
			mobile =  String.valueOf(LangUtils.ifValue(map, "mobile", mobile));
			address =  String.valueOf(LangUtils.ifValue(map, "address", address));
			city =  String.valueOf(LangUtils.ifValue(map, "city", city));
			postalCode =  String.valueOf(LangUtils.ifValue(map, "postalCode", postalCode));
			state =  String.valueOf(LangUtils.ifValue(map, "state", state));
			country =  String.valueOf(LangUtils.ifValue(map, "country", country));
			company =  String.valueOf(LangUtils.ifValue(map, "company", company));
			function =  String.valueOf(LangUtils.ifValue(map, "function", function));
			custom01 =  String.valueOf(LangUtils.ifValue(map, "custom1", custom01));
			custom02 =  String.valueOf(LangUtils.ifValue(map, "custom2", custom02));
			custom03 =  String.valueOf(LangUtils.ifValue(map, "custom3", custom03));
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
