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

import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
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
		if(serviceId.equals(CoreManifest.ID)) {
			return WT.getPlatformName();
		} else {
			String serviceName = WT.lookupResource(serviceId, locale, CoreLocaleKey.SERVICE_NAME);
			return WT.getPlatformName() + " " + serviceName;
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
	
	/**
	 * Creates strings map (No-Reply version) for notification (simple body) template
	 * @param locale
	 * @param source
	 * @param bodyHeader
	 * @param bodyMessage
	 * @return 
	 */
	public static Map<String, String> createNoReplaySimpleTplStrings(Locale locale, String source, String bodyHeader, String bodyMessage) {
		HashMap<String, String> map = new HashMap<>();
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("bodyMessage", StringUtils.defaultString(bodyMessage));
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_HEADER), source));
		map.put("footerMessage", WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_MESSAGE));
		return map;
	}
	
	/**
	 * Creates strings map (No-Reply version) for notification (complex body) template
	 * @param locale
	 * @param source
	 * @param bodyHeader
	 * @param complexBody
	 * @return 
	 */
	public static Map<String, String> createNoReplayComplexTplStrings(Locale locale, String source, String bodyHeader, String complexBody) {
		HashMap<String, String> map = new HashMap<>();
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("complexBody", StringUtils.defaultString(complexBody));
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_HEADER), source));
		map.put("footerMessage", WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_MESSAGE));
		return map;
	}
	
	/**
	 * Creates strings map for notification (simple body) template
	 * @param locale
	 * @param source
	 * @param recipientEmail
	 * @param bodyHeader
	 * @param bodyMessage
	 * @param becauseString
	 * @return 
	 */
	public static Map<String, String> createSimpleTplStrings(Locale locale, String source, String recipientEmail, String bodyHeader, String bodyMessage, String becauseString) {
		HashMap<String, String> map = new HashMap<>();
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("bodyMessage", StringUtils.defaultString(bodyMessage));
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_HEADER), source));
		map.put("footerMessage", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_MESSAGE), recipientEmail, becauseString));
		return map;
	}
	
	/**
	 * Creates strings map for notification (simple body) template
	 * @param locale
	 * @param source
	 * @param recipientEmail
	 * @param bodyHeader
	 * @param complexBody
	 * @param becauseString
	 * @return 
	 */
	public static Map<String, String> createComplexTplStrings(Locale locale, String source, String recipientEmail, String bodyHeader, String complexBody, String becauseString) {
		HashMap<String, String> map = new HashMap<>();
		map.put("bodyHeader", StringUtils.defaultString(bodyHeader));
		map.put("complexBody", StringUtils.defaultString(complexBody));
		map.put("footerHeader", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_HEADER), source));
		map.put("footerMessage", MessageFormat.format(WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_MESSAGE), recipientEmail, becauseString));
		return map;
	}
	
	/**
	 * Builds notification template (No-Reply version).
	 * According to complexBody parameter, body can be a simple message or a compiled template (in case of complex).
	 * @param locale
	 * @param complexBody
	 * @param source
	 * @param bodyHeader
	 * @param body
	 * @return
	 * @throws IOException
	 * @throws TemplateException 
	 */
	public static String buildNoReplayTpl(Locale locale, boolean complexBody, String source, String bodyHeader, String body) throws IOException, TemplateException {
		MapItem notMap = new MapItem();
		if(complexBody) {
			notMap.putAll(createNoReplayComplexTplStrings(locale, source, bodyHeader, body));
		} else {
			notMap.putAll(createNoReplaySimpleTplStrings(locale, source, bodyHeader, body));
		}
		
		MapItem map = new MapItem();
		map.put("notification", notMap);
		
		if(complexBody) {
			return WT.buildTemplate("tpl/email/notification-complexbody.html", map);
		} else {
			return WT.buildTemplate("tpl/email/notification-simplebody.html", map);
		}
	}
	
	/**
	 * Builds notification template (No-Reply version).
	 * According to complexBody parameter, body can be a simple message or a compiled template (in case of complex).
	 * @param locale
	 * @param complexBody
	 * @param source
	 * @param recipientEmail
	 * @param bodyHeader
	 * @param body
	 * @param becauseString
	 * @return
	 * @throws IOException
	 * @throws TemplateException 
	 */
	public static String buildTpl(Locale locale, boolean complexBody, String source, String recipientEmail, String bodyHeader, String body, String becauseString) throws IOException, TemplateException {
		MapItem notMap = new MapItem();
		if(complexBody) {
			notMap.putAll(createComplexTplStrings(locale, source, recipientEmail, bodyHeader, body, becauseString));
		} else {
			notMap.putAll(createSimpleTplStrings(locale, source, recipientEmail, bodyHeader, body, becauseString));
		}
		
		MapItem map = new MapItem();
		map.put("notification", notMap);
		map.put("recipientEmail", StringUtils.defaultString(recipientEmail));
		
		if(complexBody) {
			return WT.buildTemplate("tpl/email/notification-complexbody.html", map);
		} else {
			return WT.buildTemplate("tpl/email/notification-simplebody.html", map);
		}
	}
}
