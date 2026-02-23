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
package com.sonicle.webtop.core.app;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.net.IPUtils;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.security.CryptoUtils;
import com.sonicle.security.MacAlgorithm;
import com.sonicle.security.Principal;
import com.sonicle.security.otp.OTPKey;
import com.sonicle.security.otp.OTPProviderFactory;
import com.sonicle.security.otp.provider.GoogleAuth;
import com.sonicle.security.otp.provider.GoogleAuthOTPKey;
import com.sonicle.security.otp.provider.SonicleAuth;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.CoreSettings.OtpDeliveryMode;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.TplHelper;
import com.sonicle.webtop.core.app.model.TDTokenInfo;
import com.sonicle.webtop.core.app.model.TDTokenIssued;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.util.EmailNotification;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OTrustedDevice;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.TrustedDeviceDAO;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import net.glxn.qrgen.javase.QRCode;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class OTPManager extends AbstractAppManager<OTPManager> {
	private static final Logger LOGGER = WT.getLogger(OTPManager.class);
	
	OTPManager(WebTopApp wta) {
		super(wta);
	}
	
	@Override
	protected Logger doGetLogger() {
		return LOGGER;
	}
	
	@Override
	protected void doAppManagerCleanup() {
		
	}
	
	public boolean isEnabled(UserProfileId pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return (cus.getOTPDelivery() == null) ? false : cus.getOTPEnabled();
		//return StringUtils.isBlank(cus.getOTPDelivery()) ? false : cus.getOTPEnabled();
	}
	
	public OtpDeliveryMode getDeliveryMode(UserProfileId pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return cus.getOTPDelivery();
	}
	
	public String getEmailAddress(UserProfileId pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return cus.getOTPEmailAddress();
	}
	
	private String getSecret(UserProfileId pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return cus.getOTPSecret();
	}
	
	public void deactivate(UserProfileId pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		cus.clear(CoreSettings.OTP_SECRET);
		cus.clear(CoreSettings.OTP_EMAILADDRESS);
		cus.clear(CoreSettings.OTP_DELIVERY);
		cus.setOTPEnabled(false);
	}
	
	public EmailConfig configureEmail(UserProfileId pid, String emailAddress) throws WTException {
		SonicleAuth sa = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
		UserProfile.Data ud = getWebTopApp().getWebTopManager().lookupProfileData(pid, true);
		
		InternetAddress to = InternetAddressUtils.toInternetAddress(emailAddress);
		if (to == null) throw new WTException("Invalid destination address [{}]", emailAddress);
		OTPKey otp = sa.generateCredentials();
		sendCodeEmail(pid, ud.getLocale(), to, otp.getVerificationCode());
		
		return new EmailConfig(otp, emailAddress);
	}
	
	public GoogleAuthConfig configureGoogleAuth(UserProfileId pid, int qrCodeSize) throws WTException {
		GoogleAuth ga = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
		OTPKey otp = ga.generateCredentials();
		byte[] qrcode = generateGoogleAuthQRCode(pid, otp, qrCodeSize);
		return new GoogleAuthConfig(otp, qrcode);
	}
	
	public boolean activate(UserProfileId pid, Config config, String code) throws WTException {
		CoreUserSettings cus = new CoreUserSettings(pid);
		
		if (config instanceof EmailConfig) {
			SonicleAuth provider = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
			int interval = css.getOTPProviderSonicleAuthKVI();
			
			if (provider.check(code, config.otp.getVerificationCode(), config.otp.getVerificationCodeTimestamp(), interval)) {
				cus.setOTPEmailAddress(((EmailConfig)config).emailAddress);
				cus.setOTPSecret(config.otp.getSecretKey());
				cus.setOTPDelivery(OtpDeliveryMode.EMAIL);
				cus.setOTPEnabled(true);
				return true;
			} else {
				return false;
			}
		} else if(config instanceof GoogleAuthConfig) {
			GoogleAuth provider = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
			
			if (provider.check(code, config.otp.getSecretKey())) {
				cus.setOTPSecret(config.otp.getSecretKey());
				cus.setOTPDelivery(OtpDeliveryMode.GOOGLEAUTH);
				cus.setOTPEnabled(true);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	private byte[] generateGoogleAuthQRCode(UserProfileId pid, OTPKey otp, int size) throws WTException {
		String internetName;
		String userId=pid.getUserId();
		
		if (Principal.xisAdminDomain(pid.getDomainId())) {
			//admin user takes personal email
			String address=WT.getUserData(pid).getPersonalEmailAddress();
			if (!StringUtils.isEmpty(address)) {
				int ix=address.indexOf("@");
				internetName=address.substring(ix+1);
				userId=address.substring(0,ix);
			}
			else throw new WTException("Email not present for admin user");
		} else {
			//normal user composes email from user id and internet domain
			ODomain domain = getWebTopApp().getWebTopManager().OLD_getDomain(pid.getDomainId());
			if (domain == null) throw new WTException("Domain not found [{}]", pid.getDomainId());
			internetName=domain.getInternetName();
		}
		
		String issuer = URIUtils.encodeQuietly(MessageFormat.format("{0} ({1})", WT.getPlatformName(), internetName));
		InternetAddress ia = InternetAddressUtils.toInternetAddress(userId, internetName, null);
		if (ia == null) throw new WTException("Unable to build account address");
		
		String uri = GoogleAuthOTPKey.buildAuthenticatorURI(issuer, otp.getSecretKey(), ia.getAddress());
		LOGGER.debug("Generating OPT QRCode for {}", uri);
		return QRCode.from(uri).withSize(size, size).stream().toByteArray();
	}
	
	public Config prepareCheckCode(UserProfileId pid) throws WTException {
		OtpDeliveryMode deliveryMode = getDeliveryMode(pid);
		if (OtpDeliveryMode.EMAIL.equals(deliveryMode)) {
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			UserProfile.Data ud = getWebTopApp().getWebTopManager().lookupProfileData(pid, true);
			
			String emailAddress = getEmailAddress(pid);
			InternetAddress to = InternetAddressUtils.toInternetAddress(emailAddress);
			if (to == null) throw new WTException("Invalid destination address [{}]", emailAddress);
			OTPKey otp = te.generateCredentials();
			sendCodeEmail(pid, ud.getLocale(), to, otp.getVerificationCode());
			
			return new Config(EnumUtils.toSerializedName(OtpDeliveryMode.EMAIL), otp);
		} else {
			return new Config(EnumUtils.toSerializedName(OtpDeliveryMode.GOOGLEAUTH), null);
		}
	}
	
	public boolean checkCode(UserProfileId pid, Config data, String code) {
		if (StringUtils.isBlank(code)) return false;
		OtpDeliveryMode deliveryMode = getDeliveryMode(pid);
		if (OtpDeliveryMode.EMAIL.equals(deliveryMode)) {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
			SonicleAuth provider = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			int interval = css.getOTPProviderSonicleAuthKVI();
			return provider.check(code, data.otp.getVerificationCode(), data.otp.getVerificationCodeTimestamp(), interval);
		} else {
			GoogleAuth provider = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
			return provider.check(code, getSecret(pid));
		}
	}
	
	private TDTokenIssued createToken(String domainId, String userId, DateTime creationInstant, int daysDuration, byte[] secretKey) throws NoSuchAlgorithmException {
		Duration ttl = (daysDuration > 0) ? Duration.standardDays(daysDuration) : null;
		String token = CryptoUtils.generateBase64RandomToken(32); // 32 bytes -> 256 bit
			
		return new TDTokenIssued(
			new UserProfileId(domainId, userId),
			token,
			createTokenHash(token, secretKey),
			creationInstant,
			(ttl != null) ? creationInstant.plus(ttl) : null,
			ttl,
			null
		);
	}
	
	private String createTokenHash(String token, byte[] secretKey) {
		return CryptoUtils.computeMac(token.toCharArray(), MacAlgorithm.HMAC_SHA256, secretKey);
	}
	
	public TDTokenIssued trustThisDevice(final UserProfileId profileId, final String clientIdentifier, final String clientIpAddress, final String clientUserAgentString) throws WTException {
		Check.notNull(profileId, "profileId");
		TrustedDeviceDAO tdeDao = TrustedDeviceDAO.getInstance();
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, profileId.getDomainId());
		Connection con = null;
		
		try {
			byte[] key = getWebTopApp().getSecretKey();
			DateTime issueNow = JodaTimeUtils.now();
			int daysDuration = css.getOTPDeviceTrustDuration();
			TDTokenIssued newToken = createToken(profileId.getDomainId(), profileId.getUserId(), issueNow, daysDuration, key);
			
			OTrustedDevice ormt = new OTrustedDevice();
			ormt.setDomainId(profileId.getDomainId());
			ormt.setUserId(profileId.getUserId());
			ormt.setToken(newToken.getTokenHash());
			ormt.setClientIdentifier(clientIdentifier);
			ormt.setExpiresAt(newToken.getExpiry());
			ormt.setRevoked(false);
			ormt.setClientIpAddress(clientIpAddress);
			ormt.setClientUserAgent(StringUtils.isBlank(clientUserAgentString) ? null : Integer.toHexString(clientUserAgentString.hashCode()));
			
			con = getConnection(true);
			tdeDao.insert(con, ormt, BaseDAO.createRevisionTimestamp());
			LOGGER.debug("TDToken created successfully [{}]", newToken.getTokenPlain());
			
			return newToken;
			
		} catch (Exception ex) {
			throw ExceptionUtils.wrapThrowable(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public String getTrustedDeviceCookieNameSuffix(final UserProfileId profileId) {
		String secret = lookupUserSecret(profileId);
		return (secret == null) ? null : CryptoUtils.computeMac(profileId.toString().toCharArray(), MacAlgorithm.HMAC_SHA256, CryptoUtils.toUTF8ByteArray(secret));
	}
	
	public Result<TDTokenInfo> isDeviceTrusted(final UserProfileId profileId, final String token, final String clientIdentifier, final boolean updateLastUsed) {
		Check.notNull(profileId, "profileId");
		Check.notEmpty(token, "token");
		Check.notEmpty(clientIdentifier, "clientIdentifier");
		TrustedDeviceDAO tdeDao = TrustedDeviceDAO.getInstance();
		Connection con = null;
		
		try {
			byte[] key = getWebTopApp().getSecretKey();
			DateTime now = JodaTimeUtils.now();
			boolean alwaysEnforceDuration = false;
			String tokenHash = createTokenHash(token, key);
			
			con = getConnection(true);
			OTrustedDevice ormt = tdeDao.selectByToken(con, tokenHash);
			if (ormt == null) {
				LOGGER.debug("TDToken not found [{}]", token);
				return new Result<>(null);

			} else {
				if (ormt.getRevoked()) {
					LOGGER.debug("TDToken is revoked [{}]", ormt.getToken());
					return new Result<>(null);

				} else if (!profileId.equals(ormt.getProfileId())) {
					LOGGER.debug("TDToken user association mismatch, revoking... [{}]", ormt.getToken());
					doTDTokenRevoke(con, ormt);
					return new Result<>(null);

				} else if ((ormt.getExpiresAt() != null) && now.isAfter(ormt.getExpiresAt())) {
					LOGGER.debug("TDToken is expired, revoking... [{}]", ormt.getToken());
					doTDTokenRevoke(con, ormt);
					return new Result<>(null);
					
				} else if (!clientIdentifier.equals(ormt.getClientIdentifier())) {
					LOGGER.debug("TDToken clientID mismatch, revoking... [{}, {} != {}]", ormt.getToken(), clientIdentifier, ormt.getClientIdentifier());
					doTDTokenRevoke(con, ormt);
					return new Result<>(null);
					
				} else if (alwaysEnforceDuration) { // Finally, checks if
					CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, profileId.getDomainId());
					int daysDuration = css.getOTPDeviceTrustDuration();
					
					if (now.isAfter(ormt.getCreationTimestamp().plusDays(daysDuration))) {
						LOGGER.debug("TDToken is expired (enforce duration), revoking... [{}]", ormt.getToken());
						doTDTokenRevoke(con, ormt);
						return new Result<>(null);
					}
				}
			}
			
			if (updateLastUsed && tdeDao.updateUsageById(con, ormt.getTrustedDeviceId(), now) != 1) {
				LOGGER.warn("TDToken: usage update failure [{}, {}]", ormt.getTrustedDeviceId(), ormt.getToken());
			}
			
			return new Result<>(new TDTokenInfo(
				ormt.getProfileId(),
				tokenHash,
				ormt.getCreationTimestamp(),
				ormt.getExpiresAt(),
				ormt.getLastUsedAt() // Do NOT return now at last-used
			));
			
		} catch (Exception ex) {
			return new Result<>(null);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean revokeThisTrustedDevice(final UserProfileId profileId, final String thisClientIdentifier) {
		Check.notNull(profileId, "profileId");
		Check.notEmpty(thisClientIdentifier, "thisClientIdentifier");
		TrustedDeviceDAO tdeDao = TrustedDeviceDAO.getInstance();
		Connection con = null;
		
		try {
			con = getConnection(true);
			int ret = tdeDao.revokeThisByPidClient(con, profileId.getDomainId(), profileId.getUserId(), thisClientIdentifier, BaseDAO.createRevisionTimestamp());
			return ret > 0;
			
		} catch (Exception ex) {
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean revokeOtherTrustedDevices(final UserProfileId profileId, final String thisClientIdentifier) {
		Check.notNull(profileId, "profileId");
		Check.notEmpty(thisClientIdentifier, "thisClientIdentifier");
		TrustedDeviceDAO tdeDao = TrustedDeviceDAO.getInstance();
		Connection con = null;
		
		try {
			con = getConnection(true);
			int ret = tdeDao.revokeOthersByPidClient(con, profileId.getDomainId(), profileId.getUserId(), thisClientIdentifier, BaseDAO.createRevisionTimestamp());
			return ret > 0;
			
		} catch (Exception ex) {
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean doTDTokenRevoke(Connection con, OTrustedDevice ormt) {
		TrustedDeviceDAO tdeDao = TrustedDeviceDAO.getInstance();
		
		int ret = tdeDao.revokeById(con, ormt.getTrustedDeviceId(), BaseDAO.createRevisionTimestamp());
		if (ret != 1) {
			LOGGER.error("TDToken: revoke failure [{}, {}]", ormt.getTrustedDeviceId(), ormt.getToken());
			return false;
		} else {
			return true;
		}
	}
	
	private String lookupUserSecret(final UserProfileId profileId) {
		try {
			return getWebTopApp().getWebTopManager()
				.getUserSecret(profileId.getDomainId(), profileId.getUserId());

		} catch (Exception ex) {
			LOGGER.error("[DeviceId] Unable to get secret for '{}'", profileId, ex);
			return null;
		}
	}
	
	public boolean isIPTrusted(UserProfileId pid, String remoteIp) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
		String addresses = css.getOTPTrustedAddresses();
		if (addresses != null) {
			String[] cidrs = SettingsManager.asArray(addresses);
			try {
				boolean inRange = IPUtils.isIPInRange(cidrs, remoteIp);
				if (inRange) return true;
			} catch(Exception ex) {
				LOGGER.error("Problem performing IP range check", ex);
			}
		}
		return false;
	}
	
	private void sendCodeEmail(UserProfileId pid, Locale locale, InternetAddress to, String code) throws WTException {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
		
		try {
			int interval = css.getOTPProviderSonicleAuthKVI();
			int minutes = (int)Math.floor(interval/60);
			
			String subject = EmailNotification.buildSubject(locale, CoreManifest.ID, WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_EMAIL_OTPCODEVERIFICATION_BODY_HEADER));
			String customBodyHtml = TplHelper.buildOtpCodeBody(locale, code, minutes);
			String html = new EmailNotification.NoReplyBuilder()
					.withCustomBody(null, customBodyHtml)
					.build(locale, EmailNotification.buildSource(locale, CoreManifest.ID)).write();
			
			InternetAddress from = WT.getNoReplyAddress(pid.getDomainId());
			if (from == null) throw new WTException("Error building sender address");
			WT.sendEmail(WT.getGlobalMailSession(pid), true, from, to, subject, html);

		} catch(IOException | TemplateException ex) {
			LOGGER.error("Unable to build email template", ex);
		} catch(MessagingException ex) {
			LOGGER.error("Unable to send email", ex);
		}
	}
	
	public static class Config {
		public String delivery;
		public OTPKey otp;
		
		private Config(String delivery, OTPKey otp) {
			this.delivery = delivery;
			this.otp = otp;
		}
	}
	
	public static class EmailConfig extends Config {
		public String emailAddress;
		
		private EmailConfig(OTPKey otp, String emailAddress) {
			super(EnumUtils.toSerializedName(OtpDeliveryMode.EMAIL), otp);
			this.emailAddress = emailAddress;
		}
	}
	
	public static class GoogleAuthConfig extends Config {
		public byte[] qrcode;
		
		private GoogleAuthConfig(OTPKey otp, byte[] qrcode) {
			super(EnumUtils.toSerializedName(OtpDeliveryMode.GOOGLEAUTH), otp);
			this.qrcode = qrcode;
		}
	}
}
