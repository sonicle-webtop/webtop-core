/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app.util;

import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.WT;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class EmailNotification {
	protected AbstractBuilder builder;
	
	protected EmailNotification(AbstractBuilder builder) {
		this.builder = builder;
	}
	
	protected HashMap<String, String> buildTplStrings() {
		HashMap<String, String> map = new HashMap<>();
		
		// Colored message
		if (!StringUtils.isBlank(builder.redMessage)) {
			map.put("redMessage", builder.redMessage);
		} else if (!StringUtils.isBlank(builder.yellowMessage)) {
			map.put("yellowMessage", builder.yellowMessage);
		} else if (!StringUtils.isBlank(builder.greenMessage)) {
			map.put("greenMessage", builder.greenMessage);
		} else if (!StringUtils.isBlank(builder.greyMessage)) {
			map.put("greyMessage", builder.greyMessage);
		}
		
		// Body
		map.put("bodyHeader", StringUtils.defaultString(builder.bodyHeader));
		if (!StringUtils.isBlank(builder.bodyRaw)) {
			map.put("customBody", builder.bodyRaw);
		} else if (!StringUtils.isBlank(builder.bodyMessage)) {
			map.put("bodyMessage", builder.bodyMessage);
		}
		
		// Footer
		map.put("footerHeader", StringUtils.defaultString(builder.footerHeader));
		map.put("footerMessage", StringUtils.defaultString(builder.footerMessage));
		
		return map;
	}
	
	public String write() throws IOException, TemplateException {
		HashMap<String, Object> data = new HashMap<>();
		data.put("notification", buildTplStrings());
		
		if (builder instanceof BecauseBuilder) {
			data.put("recipientEmail", StringUtils.defaultString(((BecauseBuilder)builder).recipientEmail));
		}
		
		return WT.buildTemplate("tpl/email/notification.html", data);
	}
	
	public static String buildSource(Locale locale, String serviceId) {
		if (serviceId.equals(CoreManifest.ID)) {
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
		if (StringUtils.isBlank(reference)) {
			return MessageFormat.format("[{0}] {1}", source, title);
		} else {
			return MessageFormat.format("[{0}] {1} [{2}]", source, title, reference);
		}
	}
	
	public static class NoReplyBuilder extends AbstractBuilder<NoReplyBuilder> {
		
		public EmailNotification build(Locale locale, String source) {
			if (!StringUtils.isBlank(customFooterHeaderPattern)) {
				footerHeader(MessageFormat.format(customFooterHeaderPattern, source));
			} else {
				footerHeader(MessageFormat.format(getDefaultFooterHeaderPattern(locale), source));
			}
			if (!StringUtils.isBlank(customFooterMessagePattern)) {
				footerMessage(customFooterMessagePattern);
			} else {
				footerMessage(getDefaultFooterMessagePattern(locale));
			}
			return build();
		}
		
		public String getDefaultFooterHeaderPattern(Locale locale) {
			return WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_HEADER);
		}
		
		public String getDefaultFooterMessagePattern(Locale locale) {
			return WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_NOREPLY_FOOTER_MESSAGE);
		}
	}
	
	public static class BecauseBuilder extends AbstractBuilder<BecauseBuilder> {
		private String recipientEmail;
		
		public BecauseBuilder recipientEmail(String recipientEmail) {
			this.recipientEmail = recipientEmail;
			return this;
		}
		
		public EmailNotification build(Locale locale, String source, String becauseString, String recipientEmail) {
			recipientEmail(StringUtils.defaultString(recipientEmail));
			if (!StringUtils.isBlank(customFooterHeaderPattern)) {
				footerHeader(MessageFormat.format(customFooterHeaderPattern, source));
			} else {
				footerHeader(MessageFormat.format(getDefaultFooterHeaderPattern(locale), source));
			}
			if (!StringUtils.isBlank(customFooterMessagePattern)) {
				footerMessage(MessageFormat.format(customFooterMessagePattern, recipientEmail, becauseString));
			} else {
				footerMessage(MessageFormat.format(getDefaultFooterMessagePattern(locale), recipientEmail, becauseString));
			}
			return build();
		}
		
		public String getDefaultFooterHeaderPattern(Locale locale) {
			return WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_HEADER);
		}
		
		public String getDefaultFooterMessagePattern(Locale locale) {
			return WT.lookupResource(CoreManifest.ID, locale, CoreLocaleKey.TPL_NOTIFICATION_FOOTER_MESSAGE);
		}
	}
	
	public static class DefaultBuilder extends AbstractBuilder<DefaultBuilder> {}
	
	public static abstract class AbstractBuilder<T extends AbstractBuilder> {
		protected String customFooterHeaderPattern;
		protected String customFooterMessagePattern;
		private String greyMessage;
		private String greenMessage;
		private String yellowMessage;
		private String redMessage;
		private String bodyHeader;
		private String bodyMessage;
		private String bodyRaw;
		private String footerHeader;
		private String footerMessage;
		
		public T customFooterHeaderPattern(String customFooterHeaderPattern) {
			this.customFooterHeaderPattern = customFooterHeaderPattern;
			return (T)this;
		}
		
		public T customFooterMessagePattern(String customFooterMessagePattern) {
			this.customFooterMessagePattern = customFooterMessagePattern;
			return (T)this;
		}
		
		public T greyMessage(String greyMessage) {
			this.greyMessage = greyMessage;
			return (T)this;
		}
		
		public T greenMessage(String greenMessage) {
			this.greenMessage = greenMessage;
			return (T)this;
		}
		
		public T yellowMessage(String yellowMessage) {
			this.yellowMessage = yellowMessage;
			return (T)this;
		}
		
		public T redMessage(String redMessage) {
			this.redMessage = redMessage;
			return (T)this;
		}
		
		public T bodyHeader(String bodyHeader) {
			this.bodyHeader = bodyHeader;
			return (T)this;
		}
		
		public T bodyMessage(String bodyMessage) {
			this.bodyMessage = bodyMessage;
			return (T)this;
		}
		
		public T bodyRaw(String bodyRaw) {
			this.bodyRaw = bodyRaw;
			return (T)this;
		}
		
		public T footerHeader(String footerHeader) {
			this.footerHeader = footerHeader;
			return (T)this;
		}
		
		public T footerMessage(String footerMessage) {
			this.footerMessage = footerMessage;
			return (T)this;
		}
		
		public T withSimpleBody(String bodyHeader, String bodyMessage) {
			bodyHeader(StringUtils.defaultString(bodyHeader));
			bodyMessage(StringUtils.defaultString(bodyMessage));
			return (T)this;
		}
		
		public T withCustomBody(String bodyHeader, String customBodyHtml) {
			bodyHeader(StringUtils.defaultString(bodyHeader));
			bodyRaw(StringUtils.defaultString(customBodyHtml));
			return (T)this;
		}
		
		public EmailNotification build() {
			return new EmailNotification(this);
		}
	}
}
