/*
 * webtop-vfs is a WebTop Service developed by Sonicle S.r.l.
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
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.core;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.ipstack.IPLookupResponse;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.util.NotificationHelper;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class TplHelper {
	
	public static String buildOtpCodeEmail(Locale locale, String verificationCode) throws IOException, TemplateException {
		String source = NotificationHelper.buildSource(locale, CoreManifest.ID);
		String bodyMessage = WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_EMAIL_OTPCODEVERIFICATION_BODY_MESSAGE);
		bodyMessage = MessageFormat.format(bodyMessage, verificationCode);
		return NotificationHelper.buildDefaultBodyTplForNoReplay(locale, source, null, bodyMessage);
	}
	
	public static String buildOtpCodeBody(Locale locale, String verificationCode, int minutes) throws IOException, TemplateException {
		MapItem i18n = new MapItem();
		i18n.put("code", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.otpCode.code"));
		i18n.put("here", WT.lookupFormattedResource(CoreManifest.ID, locale, "tpl.email.otpCode.here", minutes));
		i18n.put("warn", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.otpCode.warn"));
		
		MapItem otp = new MapItem();
		otp.put("code", verificationCode);
		
		MapItem vars = new MapItem();
		vars.put("i18n", i18n);
		vars.put("otp", otp);
		
		return WT.buildTemplate(CoreManifest.ID, "tpl/email/otpCode-body.html", vars);
	}
	
	public static String buildNewDeviceNoticeBody(String account, DateTime timestamp, ReadableUserAgent userAgent, String ipAddress, IPLookupResponse ipLookup, Locale locale, DateTimeZone timezone, String dateFormat, String timeFormat) throws IOException, TemplateException {
		DateTimeFormatter fmt = DateTimeUtils.createFormatter(dateFormat + " " + timeFormat, timezone);
		
		String location = null;
		if (ipLookup != null) {
			location = StringUtils.join(Arrays.asList(ipLookup.getCountryName(), ipLookup.getContinentCode()), ", ");
		}
		
		MapItem i18n = new MapItem()
			.add("title", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.title"))
			.add("message", WT.lookupFormattedResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.message", WT.getPlatformName()))
			.add("time", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.time"))
			.add("device", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.device"))
			.add("ipAddress", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.ipAddress"))
			.add("location", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.location"))
			.add("action1", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.action1"))
			.add("action2", WT.lookupResource(CoreManifest.ID, locale, "tpl.email.newDevice.body.action2"));
		
		MapItem access = new MapItem()
			.add("account", account)
			.add("time", fmt.print(timestamp))
			.add("timezone", timezone.toString())
			.add("device", userAgent.getOperatingSystem().getName() + " (" + userAgent.getName() + ")")
			.add("ipAddress", ipAddress)
			.add("location", location);
		
		return WT.buildTemplate(CoreManifest.ID, "tpl/email/newDeviceNotice-body.html", new MapItem().add("i18n", i18n).add("access", access));
	}
	
	public static String buildDeviceSyncCheckEmail(Locale locale) throws IOException, TemplateException {
		String source = NotificationHelper.buildSource(locale, CoreManifest.ID);
		String bodyMessage = WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_EMAIL_DEVICESYNCCHECK_BODY_MESSAGE);
		return NotificationHelper.buildDefaultBodyTplForNoReplay(locale, source, null, bodyMessage);
	}
}
