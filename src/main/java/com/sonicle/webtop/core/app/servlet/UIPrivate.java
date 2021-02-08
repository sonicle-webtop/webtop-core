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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CId;
import com.sonicle.security.auth.EntryException;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.app.AbstractServlet;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.PushEndpoint;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionContext;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.sdk.WTPwdPolicyException;
import com.sonicle.webtop.core.bol.js.JsWTSPrivate;
import com.sonicle.webtop.core.model.DomainEntity;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.LoggerUtils;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class UIPrivate extends AbstractServlet {
	public static final String URL = "/ui-private"; // Shiro.ini must reflect this URI!
	private static final Logger logger = WT.getLogger(UIPrivate.class);
	public static final String WTSPROP_PASSWORD_CHANGEUPONLOGIN = "PASSWORD_CHANGEUPONLOGIN";

	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoggerUtils.setContextDC(RunContext.getRunProfileId());
		WebTopApp wta = getWebTopApp(request);
		SettingsManager setm = wta.getSettingsManager();
		WebTopSession wts = SessionContext.getCurrent(false);
		
		try {
			logger.trace("Servlet: ui [{}]", ServletHelper.getSessionID(request));
			
			boolean maintenance = LangUtils.value(setm.getServiceSetting(CoreManifest.ID, CoreSettings.MAINTENANCE), false);
			if (maintenance && false) throw new MaintenanceException();
			
			wts.initPrivate(request);
			UserProfileId pid = wts.getUserProfile().getId();
			
			if (wts.hasProperty(CoreManifest.ID, UIPrivate.WTSPROP_PASSWORD_CHANGEUPONLOGIN)) {
				String password = ServletUtils.getStringParameter(request, "password", null);
				
				boolean writePage = true;
				String failureMessage = null;
				
				try {
					if (!StringUtils.isBlank(password)) {
						if (Arrays.equals(password.toCharArray(), RunContext.getPrincipal().getPassword())) {
							throw new PasswordMustBeDifferent();
						}
						wta.getWebTopManager().updateUserPassword(pid, RunContext.getPrincipal().getPassword(), password.toCharArray());
						((com.sonicle.security.Principal)RunContext.getPrincipal()).setPassword(password.toCharArray());
						wts.clearProperty(CoreManifest.ID, UIPrivate.WTSPROP_PASSWORD_CHANGEUPONLOGIN);
						writePage = false;
					}
				} catch (PasswordMustBeDifferent ex) {
					logger.debug("Password change failure: password matches the current one");
					failureMessage = wta.lookupResource(wts.getLocale(), CoreLocaleKey.TPL_PASSWORD_ERROR_MUSTBEDIFFERENT);
				} catch (WTPwdPolicyException ex) {
					logger.debug("Password change failure: password does not satisfy password policies [{}]", ex.getCode(), ex);
					DomainEntity.PasswordPolicies policies = wta.getWebTopManager().getDomainPasswordPolicies(pid.getDomainId());
					failureMessage = lookupPolicyExceptionCodeMessage(wta, wts.getLocale(), ex.getCode(), policies);
				} catch(WTException | EntryException ex) {
					//TODO: display a centralized error page (like Throwable catch below)
					logger.error("Unable to update password", ex);
					failureMessage = wta.lookupResource(wts.getLocale(), CoreLocaleKey.TPL_PASSWORD_ERROR_UNEXPECTED);
				}
				
				if (writePage) {
					DomainEntity.PasswordPolicies policies = wta.getWebTopManager().getDomainPasswordPolicies(pid.getDomainId());
					writePasswordChangePage(wta, wts.getLocale(), policies, pid.getUserId(), failureMessage, response);
				} else {
					ServletUtils.forwardRequest(request, response, UIPrivate.URL);
				}	
				
			} else if (!wts.hasProperty(CoreManifest.ID, Otp.WTSPROP_OTP_VERIFIED)) {
				//TODO: move OTP management here...
				ServletUtils.forwardRequest(request, response, Otp.URL);
				
			} else {
				//ServletUtils.forwardRequest(request, response, Start.URL);
				wts.initPrivateEnvironment(request);
				writePrivatePage(wta, wts, WT.getPublicContextPath(pid.getDomainId()), response);
			}
			
		} catch(MaintenanceException ex) {
			SecurityUtils.getSubject().logout();
			request.setAttribute(Login.ATTRIBUTE_LOGINFAILURE, Login.LOGINFAILURE_MAINTENANCE);
			ServletUtils.forwardRequest(request, response, "login");
			
		} catch(Throwable t) {
			logger.error("Error", t);
			//TODO: pagina di errore
		}
	}
	
	private String lookupPolicyExceptionCodeMessage(WebTopApp wta, Locale locale, int code, DomainEntity.PasswordPolicies passwordPolicies) {
		switch(code) {
			case 1:
				return wta.lookupAndFormatResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_MINLENGTH, false, passwordPolicies.getMinLength());
			case 2:
				return wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_COMPLEXITY);
			case 3:
				return wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_CONSECUTIVEDUPLCHARS);
			case 4:
				return wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_USERNAMESIMILARITY);
			case 41:
				return wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_PASSWORDSIMILARITY);
			default:
				return wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_UNEXPECTED);
		}
	}
	
	private void writePasswordChangePage(WebTopApp wta, Locale locale, DomainEntity.PasswordPolicies passwordPolicies, String username, String failureMessage, HttpServletResponse response) throws IOException, TemplateException {
		Map tplMap = new HashMap();
		AbstractServlet.fillPageVars(tplMap, locale, null);
		AbstractServlet.fillSystemVars(tplMap, wta, locale, false, false);
		
		tplMap.put("similarityLevenThres", WebTopProps.getWTDirectorySimilarityLevenThres(wta.getProperties()));
		tplMap.put("similarityTokenSize", WebTopProps.getWTDirectorySimilarityTokenSize(wta.getProperties()));
		tplMap.put("checkpolicy", CId.build(
			passwordPolicies.getMinLength() != null ? passwordPolicies.getMinLength() : "",
			passwordPolicies.getComplexity() ? "1" : "",
			passwordPolicies.getAvoidConsecutiveChars() ? "1" : "",
			passwordPolicies.getAvoidUsernameSimilarity() ? "1" : ""
		));
		tplMap.put("username", username);
		tplMap.put("showFailure", !StringUtils.isBlank(failureMessage));
		tplMap.put("failureMessage", failureMessage);
		
		Map i18n = new HashMap();
		i18n.put("mainTitle", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_MAIN_TITLE));
		i18n.put("mainText", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_MAIN_TEXT));
		i18n.put("passwordLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_PASSWORD_LABEL));
		i18n.put("passwordConfirmLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_PASSWORDCONFIRM_LABEL));
		i18n.put("submitLabel", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_SUBMIT_LABEL));
		i18n.put("emptyFieldError", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_EMPTYFIELD));
		i18n.put("passwordPolicyErrorComplexity", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_COMPLEXITY));
		i18n.put("passwordPolicyErrorMinLength", wta.lookupAndFormatResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_MINLENGTH, false, passwordPolicies.getMinLength() != null ? passwordPolicies.getMinLength() : 8));
		i18n.put("passwordPolicyErrorConsecutiveDuplChars", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_CONSECUTIVEDUPLCHARS));
		i18n.put("passwordPolicyErrorUsernameSimilarity", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_USERNAMESIMILARITY));
		i18n.put("passwordConfirmNoMatchError", wta.lookupResource(locale, CoreLocaleKey.TPL_PASSWORD_ERROR_CONFIRMNOTMATCH));
		tplMap.put("i18n", i18n);
		
		ServletUtils.setHtmlContentType(response);
		ServletUtils.setCacheControlPrivate(response);
		
		WT.writeTemplate(CoreManifest.ID, "tpl/page/password.html", tplMap, response.getWriter());
	}
	
	private void writePrivatePage(WebTopApp wta, WebTopSession wts, String baseUrl, HttpServletResponse response)  throws IOException, TemplateException {
		String userTitle = null;
		if (wts.getProfileId() != null) {
			UserProfile.Data ud = WT.getUserData(wts.getProfileId());
			if (ud != null) {
				userTitle = ud.getDisplayName();
			}
		}
		
		Map tplMap = new HashMap();
		AbstractServlet.fillPageVars(tplMap, wts.getLocale(), userTitle, null);
		tplMap.put("loadingMessage", wta.lookupResource(wts.getLocale(), "tpl.start.loading"));
		
		// Startup variables
		JsWTSPrivate jswts = new JsWTSPrivate();
		jswts.sessionId = wts.getId();
		jswts.securityToken = wts.getCSRFToken();
		jswts.contextPath = baseUrl;
		jswts.pushUrl = PathUtils.concatPaths(wts.getClientUrl(), PushEndpoint.URL);
		wts.fillStartup(jswts);
		tplMap.put("WTS", LangUtils.unescapeUnicodeBackslashes(jswts.toJson()));
		
		ServletUtils.setHtmlContentType(response);
		ServletUtils.setCacheControlPrivateNoCache(response);
		
		WT.writeTemplate(CoreManifest.ID, "tpl/page/private.html", tplMap, response.getWriter());
	}
	
	private static class MaintenanceException extends Exception {}
	
	private static class PasswordMustBeDifferent extends Exception {}
}
