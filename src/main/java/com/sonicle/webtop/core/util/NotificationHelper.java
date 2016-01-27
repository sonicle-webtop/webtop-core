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
package com.sonicle.webtop.core.util;

import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreManifest;
import com.sonicle.webtop.core.WT;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class NotificationHelper {
	
	public static String buildSource(Locale locale, String serviceId) {
		String platform = "WebTop"; //TODO: valutare se rendere dinamico per rebranding
		if(serviceId.equals(CoreManifest.ID)) {
			return platform;
		} else {
			String serviceName = WT.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_NAME);
			return MessageFormat.format("{0} {1}", platform, serviceName);
		}
	}
	
	public static String buildSubject(Locale locale, String serviceId, String title) {
		return buildSubject(locale, serviceId, title, null);
	}
	
	public static String buildSubject(Locale locale, String serviceId, String title, String reference) {
		String source = buildSource(locale, serviceId);
		if(StringUtils.isBlank(reference)) {
			return MessageFormat.format("{0} - {1}", source, title);
		} else {
			return MessageFormat.format("{0} - {1} [{2}]", source, title, reference);
		}
	}
	
	public static String buildNoReplayTpl(Locale locale, String source, String bodyHeader, String bodyMessage) throws IOException, TemplateException {
		Map map = generateNoReplayTplStrings(locale, source, bodyHeader, bodyMessage);
		return WT.buildTemplate("tpl_emailNotification.html", map);
	}
	
	public static Map<String, String> generateNoReplayTplStrings(Locale locale, String source, String bodyHeader, String bodyMessage) {
		HashMap<String, String> map = new HashMap<>();
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("bodyMessage", StringUtils.defaultString(bodyMessage));
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_HEADER), source));
		map.put("footerMessage", WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_MESSAGE));
		return map;
	}
	
	public static String buildNotificationTpl(Locale locale, String source, String recipientEmail, String bodyHeader, String bodyMessage, String why) throws IOException, TemplateException {
		Map map = generateNotificationTplStrings(locale, source, recipientEmail, bodyHeader, bodyMessage, why);
		return WT.buildTemplate("tpl_emailNotification.html", map);
	}
	
	public static Map<String, String> generateNotificationTplStrings(Locale locale, String source, String recipientEmail, String bodyHeader, String bodyMessage, String why) {
		HashMap<String, String> map = new HashMap<>();
		map.put("recipientEmail", StringUtils.defaultString(recipientEmail));
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("bodyMessage", StringUtils.defaultString(bodyMessage));
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_HEADER), source));
		map.put("footerMessage", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_MESSAGE), recipientEmail, why));
		return map;
	}
}
