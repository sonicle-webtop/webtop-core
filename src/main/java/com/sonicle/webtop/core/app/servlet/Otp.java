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
package com.sonicle.webtop.core.app.servlet;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.OTPManager;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.util.LoggerUtils;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Otp extends AbstractServlet {
	public static final String URL = "/otp"; // Shiro.ini must reflect this URI!
	private static final Logger logger = WT.getLogger(Otp.class);
	public static final String WTSPROP_OTP_CONFIG = "OTPCONFIG";
	public static final String WTSPROP_OTP_TRIES = "OTPTRIES";
	public static final String WTSPROP_OTP_VERIFIED = "OTPVERIFIED";
	
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoggerUtils.setContextDC(RunContext.getRunProfileId());
		WebTopApp wta = getWebTopApp(request);
		WebTopSession wts = SessionContext.getCurrent(false);
		
		try {
			UserProfileId pid = wts.getUserProfile().getId();
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
			Locale locale = wts.getLocale();
			
			boolean skip = skipOTP(wta, pid, request);
			if (skip) throw new SkipException();
			
			OTPManager otpm = wta.getOTPManager();
			String deliveryMode = EnumUtils.toSerializedName(otpm.getDeliveryMode(pid));
			OTPManager.Config config = null;
			if (!wts.hasProperty(CoreManifest.ID, WTSPROP_OTP_CONFIG)) {
				config = otpm.prepareCheckCode(pid);
				wts.setProperty(CoreManifest.ID, WTSPROP_OTP_CONFIG, config); // Save for later...
				wts.setProperty(CoreManifest.ID, WTSPROP_OTP_TRIES, 0); // Save for later...
				
				writePage(wta, css, locale, deliveryMode, null, response);
				
			} else {
				config = (OTPManager.Config)wts.getProperty(CoreManifest.ID, WTSPROP_OTP_CONFIG);
				Integer tries = (Integer)wts.getProperty(CoreManifest.ID, WTSPROP_OTP_TRIES);
				if (tries == null) throw new NoMoreTriesException();
				tries++;
				
				int userCode = ServletUtils.getIntParameter(request, "wtcode", 0);
				if (otpm.checkCode(pid, config, userCode)) {
					if (css.getOTPDeviceTrustEnabled()) {
						boolean trust = ServletUtils.getBooleanParameter(request, "wttrust", false);
						if (trust) {
							String userAgent = ServletUtils.getUserAgent(request);
							JsTrustedDevice js = otpm.trustThisDevice(pid, userAgent);
							otpm.writeTrustedDeviceCookie(pid, response, new TrustedDeviceCookie(js));
						}
					}
					wts.clearProperty(CoreManifest.ID, WTSPROP_OTP_CONFIG);
					wts.clearProperty(CoreManifest.ID, WTSPROP_OTP_TRIES);
					throw new SkipException();
					
				} else {
					if (tries >= 3) throw new NoMoreTriesException();
					wts.setProperty(CoreManifest.ID, WTSPROP_OTP_TRIES, tries); // Save for later...
					String failureMessage = wta.lookupResource(locale, CoreLocaleKey.TPL_OTP_ERROR_FAILURE, true);
					writePage(wta, css, locale, deliveryMode, failureMessage, response);
				}
			}
			
		} catch(NoMoreTriesException ex) {
			if (wts != null) wts.clearProperty(CoreManifest.ID, WTSPROP_OTP_VERIFIED);
			ServletUtils.forwardRequest(request, response, Logout.URL);
		} catch(SkipException ex) {
			if (wts != null) wts.setProperty(CoreManifest.ID, WTSPROP_OTP_VERIFIED, true);
			ServletUtils.forwardRequest(request, response, UIPrivate.URL);
		} catch(Exception ex) {
			logger.error("Error", ex);
			//TODO: pagina di errore
		}
	}
	
	private boolean skipOTP(WebTopApp wta, UserProfileId pid, HttpServletRequest request) {
		OTPManager otpm = wta.getOTPManager();
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());
		
		// Tests enabling parameters
		boolean sysEnabled = css.getOTPEnabled(), profileEnabled = otpm.isEnabled(pid);
		if (!sysEnabled || !profileEnabled) { //TODO: valutare se escludere admin
			logger.debug("OTP check skipped [{}, {}]", sysEnabled, profileEnabled);
			return true;
		}
		
		String remoteIP = ServletUtils.getClientIP(request);
		logger.debug("Checking OTP from remote address {}", remoteIP);
		
		// Checks if request comes from a configured trusted network and skip check
		if (otpm.isTrusted(pid, remoteIP)) {
			logger.debug("OTP check skipped: request comes from a trusted address.");
			return true;
		}
		
		// Checks cookie that marks a trusted device
		JsTrustedDevice td = null;
		TrustedDeviceCookie cookie = otpm.readTrustedDeviceCookie(pid, request);
		if ((cookie != null) && otpm.isThisDeviceTrusted(pid, cookie)) {
			td = otpm.getTrustedDevice(pid, cookie.deviceId);
		}
		
		if (td != null) {
			logger.debug("OTP check skipped: request comes from a trusted device [{}]", td.deviceId);
			return true;
		}
		
		return false;
	}
	
	private void writePage(WebTopApp wta, CoreServiceSettings css, Locale locale, String deliveryMode, String failureMessage, HttpServletResponse response) throws IOException, TemplateException {
		Map tplMap = new HashMap();
		AbstractServlet.fillPageVars(tplMap, locale, null);
		AbstractServlet.fillSystemVars(tplMap, wta, locale, false, false);
		tplMap.put("showFailure", !StringUtils.isBlank(failureMessage));
		tplMap.put("failureMessage", failureMessage);
		tplMap.put("helpTitle", wta.lookupResource(locale, CoreLocaleKey.TPL_OTP_HELPTITLE, true));
		tplMap.put("deliveryTitle", wta.lookupResource(locale, CoreLocaleKey.TPL_OTP_DELIVERY_TITLE, true));
		tplMap.put("deliveryMode", deliveryMode);
		tplMap.put("deliveryInfo", wta.lookupResource(locale, MessageFormat.format(CoreLocaleKey.TPL_OTP_DELIVERY_INFO, deliveryMode), true));
		tplMap.put("codePlaceholder", wta.lookupResource(locale, CoreLocaleKey.TPL_OTP_CODE_PLACEHOLDER, true));
		tplMap.put("submitLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_OTP_SUBMIT_LABEL, true));
		tplMap.put("trustLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_OTP_TRUST_LABEL, true));
		tplMap.put("showTrustCheckbox", css.getOTPDeviceTrustEnabled());
		
		ServletUtils.setHtmlContentType(response);
		ServletUtils.setCacheControlPrivate(response);
		
		// Load and build template
		Template tpl = WT.loadTemplate(CoreManifest.ID, "tpl/page/otp.html");
		tpl.process(tplMap, response.getWriter());
	}
	
	private static class NoMoreTriesException extends Exception {}

	private static class SkipException extends Exception {}
}
