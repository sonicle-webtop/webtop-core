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
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.LangUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author malbinola
 */
public class AppLocale {
	
	private final String id;
	private final String shortDatePattern;
	private final String longDatePattern;
	private final String shortTimePattern;
	private final String longTimePattern;
	
	public AppLocale(String languageTag) {
		this.id = LangUtils.homogenizeLocaleId(languageTag);
		
		Locale locale = LangUtils.languageTagToLocale(languageTag);
		shortDatePattern = getDateFormatter(DateFormat.SHORT, locale).toPattern();
		longDatePattern = getDateFormatter(DateFormat.LONG, locale).toPattern();
		shortTimePattern = getTimeFormatter(DateFormat.SHORT, locale).toPattern();
		longTimePattern = getTimeFormatter(DateFormat.LONG, locale).toPattern();
	}
	
	public String getId() {
		return id;
	}
	
	public Locale getLocale() {
		return LangUtils.languageTagToLocale(id);
	}
	
	public String getShortDatePattern() {
		return shortDatePattern;
	}
	
	public String getLongDatePattern() {
		return longDatePattern;
	}
	
	public String getShortTimePattern() {
		return shortTimePattern;
	}
	
	public String getLongTimePattern() {
		return longTimePattern;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(id)
			.toHashCode();
	}
		
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AppLocale == false) return false;
		if(this == obj) return true;
		final AppLocale otherObject = (AppLocale) obj;
		return new EqualsBuilder()
			.append(id, otherObject.id)
			.isEquals();
	}
	
	private SimpleDateFormat getDateFormatter(int style, Locale locale) {
		return (SimpleDateFormat)DateFormat.getDateInstance(style, locale);
	}
	
	private SimpleDateFormat getTimeFormatter(int style, Locale locale) {
		return (SimpleDateFormat)DateFormat.getTimeInstance(style, locale);
	}
}
