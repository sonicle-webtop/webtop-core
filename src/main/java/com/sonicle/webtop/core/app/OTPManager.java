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
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.net.IPUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.security.DomainAccount;
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
import com.sonicle.webtop.core.app.util.EmailNotification;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUserSetting;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class OTPManager {
	private static final Logger logger = WT.getLogger(OTPManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	static synchronized OTPManager initialize(WebTopApp wta) {
		if (initialized) throw new RuntimeException("Initialization already done");
		OTPManager otpm = new OTPManager(wta);
		initialized = true;
		logger.info("Initialized");
		return otpm;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private OTPManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		logger.info("Cleaned up");
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
		UserProfile.Data ud = wta.getWebTopManager().lookupProfileData(pid, true);
		
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
			ODomain domain = wta.getWebTopManager().OLD_getDomain(pid.getDomainId());
			if (domain == null) throw new WTException("Domain not found [{}]", pid.getDomainId());
			internetName=domain.getInternetName();
		}
		
		String issuer = URIUtils.encodeQuietly(MessageFormat.format("{0} ({1})", WT.getPlatformName(), internetName));
		InternetAddress ia = InternetAddressUtils.toInternetAddress(userId, internetName, null);
		if (ia == null) throw new WTException("Unable to build account address");
		
		String uri = GoogleAuthOTPKey.buildAuthenticatorURI(issuer, otp.getSecretKey(), ia.getAddress());
		logger.debug("Generating OPT QRCode for {}", uri);
		return QRCode.from(uri).withSize(size, size).stream().toByteArray();
	}
	
	public Config prepareCheckCode(UserProfileId pid) throws WTException {
		OtpDeliveryMode deliveryMode = getDeliveryMode(pid);
		if (OtpDeliveryMode.EMAIL.equals(deliveryMode)) {
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			UserProfile.Data ud = wta.getWebTopManager().lookupProfileData(pid, true);
			
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
	
	public void registerTrustedDevice(UserProfileId pid, JsTrustedDevice td) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreSettings.OTP_TRUSTED_DEVICE + "@" + td.deviceId;
		sm.setUserSetting(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, key, JsonResult.gson().toJson(td));
	}
	
	public boolean removeTrustedDevice(UserProfileId pid, String deviceId) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreSettings.OTP_TRUSTED_DEVICE + "@" + deviceId;
		return sm.deleteUserSetting(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, key);
	}
	
	public JsTrustedDevice getTrustedDevice(UserProfileId pid, String deviceId) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreSettings.OTP_TRUSTED_DEVICE + "@" + deviceId;
		return LangUtils.value(sm.getUserSetting(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, key), null, JsTrustedDevice.class);
	}
	
	public ArrayList<JsTrustedDevice> listTrustedDevices(UserProfileId pid) {
		SettingsManager sm = wta.getSettingsManager();
		List<OUserSetting> items = sm.getUserSettings(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, CoreSettings.OTP_TRUSTED_DEVICE+"%");
		return JsTrustedDevice.asList(items);
	}
	
	public JsTrustedDevice trustThisDevice(UserProfileId pid, String userAgentHeader) {
		String deviceId = DigestUtils.shaHex(UUID.randomUUID().toString() + userAgentHeader);
		long now = new Date().getTime();
		JsTrustedDevice td = new JsTrustedDevice(deviceId, DomainAccount.buildName(pid.getDomainId(), pid.getUserId()), now, userAgentHeader);
		registerTrustedDevice(pid, td);
		return td;
	}
	
	public boolean isThisDeviceTrusted(UserProfileId pid, TrustedDeviceCookie tdc) {
		if (tdc == null) return false;
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
		
		// Checks (if enabled) cookie duration
		int duration = css.getOTPDeviceTrustDuration();
		if (duration > 0) {
			long now = new Date().getTime();
			long expires = tdc.timestamp + TimeUnit.DAYS.toMillis(duration);
			if (now > expires) {
				logger.trace("Device cookie expired [{}days, {} > {}]", duration, now, expires);
				return false;
			}
		}
		
		// Checks if device is registered
		JsTrustedDevice td = getTrustedDevice(pid, tdc.deviceId);
		if (td == null) {
			logger.trace("Device ID not registered before [{}]", tdc.deviceId);
			return false;
		}
		
		// Checks account match
		if (!td.account.equals(tdc.account)) {
			logger.trace("Device ID not bound to the right account [{} != {}]", tdc.account, td.account);
			return false;
		}
		return true;
	}
	
	public boolean isTrusted(UserProfileId pid, String remoteIp) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
		String addresses = css.getOTPTrustedAddresses();
		if (addresses != null) {
			String[] cidrs = SettingsManager.asArray(addresses);
			try {
				boolean inRange = IPUtils.isIPInRange(cidrs, remoteIp);
				if (inRange) return true;
			} catch(Exception ex) {
				logger.error("Problem performing IP range check", ex);
			}
		}
		return false;
	}
	
	public void clearTrustedDeviceCookie(UserProfileId pid, HttpServletResponse response) {
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(pid.getDomainId(), pid.getUserId()));
		ServletUtils.eraseCookie(response, name);
	}
	
	public TrustedDeviceCookie readTrustedDeviceCookie(UserProfileId pid, HttpServletRequest request) {
		String secret = getSecret(pid);
		if (StringUtils.isBlank(secret)) {
			logger.warn("Missing OTP secret for user '{}'", pid.toString());
			return null;
		}
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(pid.getDomainId(), pid.getUserId()));
		return ServletUtils.getEncryptedCookie(secret, request, name, TrustedDeviceCookie.class);
	}
	
	public boolean writeTrustedDeviceCookie(UserProfileId pid, HttpServletResponse response, TrustedDeviceCookie tdc) {
		String secret = getSecret(pid);
		if (StringUtils.isBlank(secret)) {
			logger.warn("Missing OTP secret for user '{}'", pid.toString());
			return false;
		}
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(pid.getDomainId(), pid.getUserId()));
		int duration = 60*60*24*365*2; // 2 years
		ServletUtils.setEncryptedCookie(secret, response, name, tdc, TrustedDeviceCookie.class, duration);
		return true;
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
			logger.error("Unable to build email template", ex);
		} catch(MessagingException ex) {
			logger.error("Unable to send email", ex);
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
