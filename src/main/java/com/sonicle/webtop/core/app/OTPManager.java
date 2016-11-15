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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
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
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUserSetting;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.InternetAddress;
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
		if(initialized) throw new RuntimeException("Initialization already done");
		OTPManager otpm = new OTPManager(wta);
		initialized = true;
		logger.info("OTPManager initialized");
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
		logger.info("OTPManager destroyed");
	}
	
	public boolean isEnabled(UserProfile.Id pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return StringUtils.isBlank(cus.getOTPDelivery()) ? false : cus.getOTPEnabled();
	}
	
	public String getDeliveryMode(UserProfile.Id pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return cus.getOTPDelivery();
	}
	
	public String getEmailAddress(UserProfile.Id pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return cus.getOTPEmailAddress();
	}
	
	private String getSecret(UserProfile.Id pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		return cus.getOTPSecret();
	}
	
	public void deactivate(UserProfile.Id pid) {
		CoreUserSettings cus = new CoreUserSettings(pid);
		cus.clear(CoreSettings.OTP_SECRET);
		cus.clear(CoreSettings.OTP_EMAILADDRESS);
		cus.clear(CoreSettings.OTP_DELIVERY);
		cus.setOTPEnabled(false);
	}
	
	public EmailConfig configureEmail(UserProfile.Id pid, String emailAddress) throws WTException {
		SonicleAuth sa = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
		UserProfile.Data ud = wta.getUserManager().userData(pid);
		if(ud.getEmail() == null) throw new WTException("Valid email address is required");
		OTPKey otp = sa.generateCredentials(ud.getEmail().getAddress());
		
		//TODO: find a way to send email
		//wta.sendOtpMail(wts, profile.getWebTopDomain(), profile, profile.getLocale(), emailAddress, String.valueOf(otp.getVerificationCode()));
		return new EmailConfig(otp, emailAddress);
	}
	
	public GoogleAuthConfig configureGoogleAuth(UserProfile.Id pid, int qrCodeSize) throws WTException {
		GoogleAuth ga = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
		OTPKey otp = ga.generateCredentials();
		byte[] qrcode = generateGoogleAuthQRCode(pid, otp, qrCodeSize);
		return new GoogleAuthConfig(otp, qrcode);
	}
	
	public boolean activate(UserProfile.Id pid, Config config, int code) throws WTException {
		CoreUserSettings cus = new CoreUserSettings(pid);
		
		if(config instanceof EmailConfig) {
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
			long interval = css.getOTPProviderSonicleAuthKVI();
			
			if(te.check(code, config.otp.getVerificationCode(), Long.valueOf(config.otp.getKey()), interval)) {
				cus.setOTPEmailAddress(((EmailConfig)config).emailAddress);
				cus.setOTPDelivery(CoreSettings.OTP_DELIVERY_EMAIL);
				cus.setOTPEnabled(true);
				return true;
			} else {
				return false;
			}
		} else if(config instanceof GoogleAuthConfig) {
			GoogleAuth ga = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
			
			if(ga.check(code, config.otp.getKey())) {
				cus.setOTPSecret(config.otp.getKey());
				cus.setOTPDelivery(CoreSettings.OTP_DELIVERY_GOOGLEAUTH);
				cus.setOTPEnabled(true);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	private byte[] generateGoogleAuthQRCode(UserProfile.Id pid, OTPKey otp, int size) throws WTException {
		ODomain domain = wta.getUserManager().getDomain(pid.getDomainId());
		if(domain == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
		
		String issuer = URIUtils.encodeQuietly(MessageFormat.format("{0} ({1})", WT.getPlatformName(), domain.getDomainName()));
		InternetAddress ia = MailUtils.buildInternetAddress(pid.getUserId(), domain.getDomainName(), null);
		if(ia == null) throw new WTException("Unable to build account address");
		
		String uri = GoogleAuthOTPKey.buildAuthenticatorURI(issuer, otp.getKey(), ia.getAddress());
		logger.debug("Generating OPT QRCode for {}", uri);
		return QRCode.from(uri).withSize(size, size).stream().toByteArray();
	}
	
	public Config prepareCheckCode(UserProfile.Id pid) throws WTException {
		String deliveryMode = getDeliveryMode(pid);
		if(deliveryMode.equals(CoreSettings.OTP_DELIVERY_EMAIL)) {
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			UserProfile.Data ud = wta.getUserManager().userData(pid);
			if(ud.getEmail() == null) throw new WTException("Valid email address is required");
			
			OTPKey otp = te.generateCredentials(ud.getEmail().getAddress());
			//TODO: find a way to send email
			//wta.sendOtpMail(wts, profile.getWebTopDomain(), profile, profile.getLocale(), getEmailAddress(profile), String.valueOf(otp.getVerificationCode()));
			logger.debug("Verification code {}", otp.getVerificationCode());
			return new Config(CoreSettings.OTP_DELIVERY_EMAIL, otp);
		} else {
			return new Config(CoreSettings.OTP_DELIVERY_GOOGLEAUTH, null);
		}
	}
	
	public boolean checkCode(UserProfile.Id pid, Config data, int code) {
		String deliveryMode = getDeliveryMode(pid);
		if(deliveryMode.equals(CoreSettings.OTP_DELIVERY_EMAIL)) {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			long interval = css.getOTPProviderSonicleAuthKVI();
			return te.check(code, data.otp.getVerificationCode(), Long.valueOf(data.otp.getKey()), interval);
		} else {
			GoogleAuth ga = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
			return ga.check(code, getSecret(pid));
		}
	}
	
	public void registerTrustedDevice(UserProfile.Id pid, JsTrustedDevice td) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreSettings.OTP_TRUSTED_DEVICE + "@" + td.deviceId;
		sm.setUserSetting(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, key, JsonResult.gson.toJson(td));
	}
	
	public boolean removeTrustedDevice(UserProfile.Id pid, String deviceId) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreSettings.OTP_TRUSTED_DEVICE + "@" + deviceId;
		return sm.deleteUserSetting(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, key);
	}
	
	public JsTrustedDevice getTrustedDevice(UserProfile.Id pid, String deviceId) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreSettings.OTP_TRUSTED_DEVICE + "@" + deviceId;
		return LangUtils.value(sm.getUserSetting(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, key), null, JsTrustedDevice.class);
	}
	
	public ArrayList<JsTrustedDevice> listTrustedDevices(UserProfile.Id pid) {
		SettingsManager sm = wta.getSettingsManager();
		List<OUserSetting> items = sm.getUserSettings(pid.getDomainId(), pid.getUserId(), CoreManifest.ID, CoreSettings.OTP_TRUSTED_DEVICE+"%");
		return JsTrustedDevice.asList(items);
	}
	
	public JsTrustedDevice trustThisDevice(UserProfile.Id pid, String userAgentHeader) {
		String deviceId = DigestUtils.shaHex(UUID.randomUUID().toString() + userAgentHeader);
		long now = new Date().getTime();
		JsTrustedDevice td = new JsTrustedDevice(deviceId, DomainAccount.buildName(pid.getDomainId(), pid.getUserId()), now, userAgentHeader);
		registerTrustedDevice(pid, td);
		return td;
	}
	
	public boolean isThisDeviceTrusted(UserProfile.Id pid, TrustedDeviceCookie tdc) {
		if(tdc == null) return false;
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
		
		// Checks (if enabled) cookie duration
		int duration = css.getOTPDeviceTrustDuration();
		if(duration > 0) {
			long now = new Date().getTime();
			long expires = tdc.timestamp + TimeUnit.DAYS.toMillis(duration);
			if(now > expires) {
				logger.trace("Device cookie expired [{}days, {} > {}]", duration, now, expires);
				return false;
			}
		}
		
		// Checks if device is registered
		JsTrustedDevice td = getTrustedDevice(pid, tdc.deviceId);
		if(td == null) {
			logger.trace("Device ID not registered before [{}]", tdc.deviceId);
			return false;
		}
		
		// Checks account match
		if(!td.account.equals(tdc.account)) {
			logger.trace("Device ID not bound to the right account [{} != {}]", tdc.account, td.account);
			return false;
		}
		return true;
	}
	
	public boolean isTrusted(UserProfile.Id pid, String remoteIP) {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
		String addresses = css.getOTPTrustedAddresses();
		if(addresses != null) {
			String[] cidrs = SettingsManager.asArray(addresses);
			try {
				boolean inRange = IPUtils.isIPInRange(cidrs, remoteIP);
				if(inRange) return true;
			} catch(Exception ex) {
				logger.error("Problem performing IP range check", ex);
			}
		}
		return false;
	}
	
	public void clearTrustedDeviceCookie(UserProfile.Id pid, HttpServletResponse response) {
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(pid.getDomainId(), pid.getUserId()));
		ServletUtils.eraseCookie(response, name);
	}
	
	public TrustedDeviceCookie readTrustedDeviceCookie(UserProfile.Id pid, HttpServletRequest request) {
		String secret = getSecret(pid);
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(pid.getDomainId(), pid.getUserId()));
		return ServletUtils.getEncryptedCookie(secret, request, name, TrustedDeviceCookie.class);
	}
	
	public void writeTrustedDeviceCookie(UserProfile.Id pid, HttpServletResponse response, TrustedDeviceCookie tdc) {
		String secret = getSecret(pid);
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(pid.getDomainId(), pid.getUserId()));
		int duration = 60*60*24*365*2; // 2 years
		ServletUtils.setEncryptedCookie(secret, response, name, tdc, TrustedDeviceCookie.class, duration);
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
			super(CoreSettings.OTP_DELIVERY_EMAIL, otp);
			this.emailAddress = emailAddress;
		}
	}
	
	public static class GoogleAuthConfig extends Config {
		public byte[] qrcode;
		
		private GoogleAuthConfig(OTPKey otp, byte[] qrcode) {
			super(CoreSettings.OTP_DELIVERY_GOOGLEAUTH, otp);
			this.qrcode = qrcode;
		}
	}
}
