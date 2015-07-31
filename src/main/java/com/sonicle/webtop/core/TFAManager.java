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
package com.sonicle.webtop.core;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.net.IPUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.URLUtils;
import com.sonicle.security.DomainAccount;
import com.sonicle.security.Principal;
import com.sonicle.security.otp.OTPKey;
import com.sonicle.security.otp.OTPProviderFactory;
import com.sonicle.security.otp.provider.GoogleAuth;
import com.sonicle.security.otp.provider.GoogleAuthOTPKey;
import com.sonicle.security.otp.provider.SonicleAuth;
import com.sonicle.webtop.core.bol.OUserSetting;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class TFAManager {
	
	private static final Logger logger = WT.getLogger(TFAManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	static synchronized TFAManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		TFAManager tfam = new TFAManager(wta);
		initialized = true;
		logger.info("TFAManager initialized");
		return tfam;
	}
	
	private WebTopApp wta = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private TFAManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		logger.info("TFAManager destroyed");
	}
	
	/*
	public boolean isEnabled(UserProfile profile) {
		SettingsManager sm = wta.getSettingsManager();
		return LangUtils.value(sm.getUserSetting(profile, CoreManifest.ID, Settings.OTP_ENABLED), false);
	}
	
	public String getDeliveryMode(UserProfile profile) {
		SettingsManager sm = wta.getSettingsManager();
		return sm.getUserSetting(profile, CoreManifest.ID, Settings.OTP_DELIVERY);
	}
	
	public String getEmailAddress(UserProfile profile) {
		SettingsManager sm = wta.getSettingsManager();
		return sm.getUserSetting(profile, CoreManifest.ID, Settings.OTP_EMAILADDRESS);
	}
	
	public String getSecret(UserProfile profile) {
		SettingsManager sm = wta.getSettingsManager();
		return sm.getUserSetting(profile, CoreManifest.ID, Settings.OTP_SECRET);
	}
	*/
	
	boolean isTrusted(UserProfile profile, String remoteIP) {
		CoreServiceSettings css = new CoreServiceSettings(profile.getDomainId(), CoreManifest.ID);
		SettingsManager sm = wta.getSettingsManager();
		
		String addresses = css.getTFATrustedAddresses();
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
	
	void initTFAUsingEmail(WebTopSession wts, String emailAddress) {
		UserProfile profile = wts.getUserProfile();
		
		SonicleAuth sa = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
		OTPKey otp = sa.generateCredentials(profile.getEmailAddress());
		//TODO: find a way to send email
		//wta.sendOtpMail(wts, profile.getWebTopDomain(), profile, profile.getLocale(), emailAddress, String.valueOf(otp.getVerificationCode()));
		wts.setProperty(CoreUserSettings.TFA_EMAILADDRESS, emailAddress); // Save for later...
		wts.setProperty("OTPSA", otp); // Save for later...
	}
	
	void initTFAUsingGoogleAuth(WebTopSession wts) {
		GoogleAuth ga = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
		OTPKey otp = ga.generateCredentials();
		wts.setProperty("OTPGA", otp); // Save for later...
	}
	
	ByteArrayOutputStream generateGoogleAuthQRCodeImage(WebTopSession wts, int size) {
		UserProfile profile = wts.getUserProfile();
		
		OTPKey otp = (OTPKey)wts.getProperty("OTPGA");
		String domain = profile.getPrincipal().getAuthenticationDomain().getDomain();
		String issuer = URLUtils.encodeQuietly(MessageFormat.format("WebTop ({0})", domain));
		
		String uri = GoogleAuthOTPKey.buildAuthenticatorURI(issuer, otp.getKey(), profile.getPrincipal().getName());
		logger.debug("Generating OTP QRCode for {}", uri);
		return QRCode.from(uri).withSize(size, size).stream();
	}
	
	public boolean activateTFA(WebTopSession wts, String deliveryMode, int code) {
		UserProfile profile = wts.getUserProfile();
		CoreServiceSettings css = new CoreServiceSettings(profile.getDomainId(), CoreManifest.ID);
		CoreUserSettings cus = new CoreUserSettings(profile.getId());
		
		boolean valid = false;
		if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_EMAIL)) {
			OTPKey otp = (OTPKey)wts.getProperty("OTPSA");
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			long interval = css.getOTPProviderSonicleAuthKVI();
			valid = te.check(code, otp.getVerificationCode(), Long.valueOf(otp.getKey()), interval);

		} else if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_GOOGLEAUTH)) {
			OTPKey otp = (OTPKey)wts.getProperty("OTPGA");
			GoogleAuth ga = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
			valid = ga.check(code, otp.getKey());
		}
		
		if(valid) {
			if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_EMAIL)) {
				cus.setTFAEmailAddress((String)wts.getProperty(CoreUserSettings.TFA_EMAILADDRESS));
				wts.clearProperty(CoreUserSettings.TFA_EMAILADDRESS);
				wts.clearProperty("OTPSA");
			} else if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_GOOGLEAUTH)) {
				OTPKey otp = (OTPKey)wts.getProperty("OTPGA");
				cus.setTFASecret(otp.getKey());
				wts.clearProperty("OTPGA");
			}
			cus.setTFADelivery(deliveryMode);
			//sm.setUserSetting(profile, CoreManifest.ID, Settings.OTP_ENABLED, true);
		}
		return valid;
	}
	
	void beforeCodeCheck(WebTopSession wts) {
		UserProfile profile = wts.getUserProfile();
		CoreUserSettings cus = new CoreUserSettings(profile.getId());
		OTPKey otp = null;
		
		String deliveryMode = cus.getTFADelivery();
		if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_EMAIL)) {
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			otp = te.generateCredentials(profile.getEmailAddress());
			//TODO: find a way to send email
			//wta.sendOtpMail(wts, profile.getWebTopDomain(), profile, profile.getLocale(), getEmailAddress(profile), String.valueOf(otp.getVerificationCode()));
			logger.debug("Verification code {}", otp.getVerificationCode());
			wts.setProperty("OTPSA", otp); // Save for later...

		} else if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_GOOGLEAUTH)) {
			// Nothing to do...
		}
	}
	
	boolean checkCode(WebTopSession wts, int code) {
		UserProfile profile = wts.getUserProfile();
		CoreServiceSettings css = new CoreServiceSettings(profile.getDomainId(), CoreManifest.ID);
		CoreUserSettings cus = new CoreUserSettings(profile.getId());
		OTPKey otp = null;
		
		boolean valid = false;
		String deliveryMode = cus.getTFADelivery();
		if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_EMAIL)) {
			SonicleAuth te = (SonicleAuth)OTPProviderFactory.getInstance("SonicleAuth");
			otp = (OTPKey)wts.getProperty("OTPSA");
			long interval = css.getOTPProviderSonicleAuthKVI();
			valid = te.check(code, otp.getVerificationCode(), Long.valueOf(otp.getKey()), interval);
			
		} else if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_GOOGLEAUTH)) {
			GoogleAuth ga = (GoogleAuth)OTPProviderFactory.getInstance("GoogleAuth");
			valid = ga.check(code, cus.getTFASecret());
		}
		return valid;
	}
	
	void registerTrustedDevice(String domainId, String userId, JsTrustedDevice td) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreUserSettings.TFA_TRUSTED_DEVICE + "@" + td.deviceId;
		sm.setUserSetting(domainId, userId, CoreManifest.ID, key, JsonResult.gson.toJson(td));
	}
	
	boolean removeTrustedDevice(UserProfile profile, String deviceId) {
		return removeTrustedDevice(profile.getDomainId(), profile.getUserId(), deviceId);
	}
	
	boolean removeTrustedDevice(String domainId, String userId, String deviceId) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreUserSettings.TFA_TRUSTED_DEVICE + "@" + deviceId;
		return sm.deleteUserSetting(domainId, userId, CoreManifest.ID, key);
	}
	
	JsTrustedDevice getTrustedDevice(String domainId, String userId, String deviceId) {
		SettingsManager sm = wta.getSettingsManager();
		String key = CoreUserSettings.TFA_TRUSTED_DEVICE + "@" + deviceId;
		return LangUtils.value(sm.getUserSetting(domainId, userId, CoreManifest.ID, key), null, JsTrustedDevice.class);
	}
	
	ArrayList<JsTrustedDevice> getTrustedDevices(UserProfile profile) {
		return getTrustedDevices(profile.getDomainId(), profile.getUserId());
	}
	
	ArrayList<JsTrustedDevice> getTrustedDevices(String domainId, String userId) {
		SettingsManager sm = wta.getSettingsManager();
		List<OUserSetting> items = sm.getUserSettings(domainId, userId, CoreManifest.ID, CoreUserSettings.TFA_TRUSTED_DEVICE+"%");
		return JsTrustedDevice.asList(items);
	}
	
	JsTrustedDevice trustThisDevice(String domainId, String userId, String userAgentHeader) {
		String deviceId = DigestUtils.shaHex(UUID.randomUUID().toString() + userAgentHeader);
		long now = new Date().getTime();
		JsTrustedDevice td = new JsTrustedDevice(deviceId, DomainAccount.buildName(domainId, userId), now, userAgentHeader);
		registerTrustedDevice(domainId, userId, td);
		return td;
	}
	
	boolean isThisDeviceTrusted(String domainId, String userId, TrustedDeviceCookie tdc) {
		if(tdc == null) return false;
		CoreServiceSettings css = new CoreServiceSettings(domainId, CoreManifest.ID);
		
		// Checks (if enabled) cookie duration
		int duration = css.getTFADeviceTrustDuration();
		if(duration > 0) {
			long now = new Date().getTime();
			long expires = tdc.timestamp + TimeUnit.DAYS.toMillis(duration);
			if(now > expires) {
				logger.trace("Device cookie expired [{}days, {} > {}]", duration, now, expires);
				return false;
			}
		}
		
		// Checks if device is registered
		JsTrustedDevice td = getTrustedDevice(domainId, userId, tdc.deviceId);
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
	
	void clearTrustedDeviceCookie(UserProfile profile, HttpServletResponse response) {
		clearTrustedDeviceCookie(profile.getDomainId(), profile.getUserId(), response);
	}
	
	void clearTrustedDeviceCookie(String domainId, String userId, HttpServletResponse response) {
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(domainId, userId));
		ServletUtils.eraseCookie(response, name);
	}
	
	TrustedDeviceCookie readTrustedDeviceCookie(UserProfile profile, HttpServletRequest request) {
		return readTrustedDeviceCookie(profile.getDomainId(), profile.getUserId(), profile.getSecret(), request);
	}
	
	TrustedDeviceCookie readTrustedDeviceCookie(String domainId, String userId, String secret, HttpServletRequest request) {
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(domainId, userId));
		return ServletUtils.getEncryptedCookie(secret, request, name, TrustedDeviceCookie.class);
	}
	
	void writeTrustedDeviceCookie(UserProfile profile, HttpServletResponse response, TrustedDeviceCookie tdc) {
		writeTrustedDeviceCookie(profile.getDomainId(), profile.getUserId(), profile.getSecret(), response, tdc);
	}
	
	void writeTrustedDeviceCookie(String domainId, String userId, String secret, HttpServletResponse response, TrustedDeviceCookie tdc) {
		String name = MessageFormat.format("TD_{0}", Principal.buildHashedName(domainId, userId));
		int duration = 60*60*24*365*2; // 2 years
		ServletUtils.setEncryptedCookie(secret, response, name, tdc, TrustedDeviceCookie.class, duration);
	}
	
	void disableTFA(String domainId, String userId) {
		CoreUserSettings cus = new CoreUserSettings(domainId, userId);
		//cus.clearUserSetting(CoreUserSettings.TFA_ENABLED);
		cus.clear(CoreUserSettings.TFA_SECRET);
		cus.clear(CoreUserSettings.TFA_EMAILADDRESS);
		cus.clear(CoreUserSettings.TFA_DELIVERY);
	}
}
