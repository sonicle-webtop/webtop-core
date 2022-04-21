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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public abstract class BaseSettings {
	
	public abstract String getSetting(String key);
	public abstract boolean setSetting(String key, Object value);
	public abstract String getString(String key, String defaultValue);
	public abstract boolean setString(String key, String value);
	public abstract Boolean getBoolean(String key, Boolean defaultValue);
	public abstract boolean setBoolean(String key, Boolean value);
	public abstract Integer getInteger(String key, Integer defaultValue);
	public abstract boolean setInteger(String key, Integer value);
	public abstract Long getLong(String key, Long defaultValue);
	public abstract boolean setLong(String key, Long value);
	public abstract Float getFloat(String key, Float defaultValue);
	public abstract boolean setFloat(String key, Float value);
	
	public <T>T getObject(String key, T defaultValue, Class<T> type) {
		String value = getString(key, null);
		return (value == null) ? defaultValue : LangUtils.deserialize(value, defaultValue, type);
	}
	
	public <T> boolean setObject(String key, T value, Class<T> type) {
		String s = LangUtils.serialize(value, type);
		if(s == null) return false;
		return setString(key, s);
	}
	
	public LocalTime getTime(String key, String defaultValue, String pattern) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.getDefault());
		LocalTime lt = (defaultValue == null) ? null : LocalTime.parse(defaultValue, dtf);
		return getTime(key, lt, pattern);
	}
	
	public LocalTime getTime(String key, LocalTime defaultValue, String pattern) {
		DateTimeFormatter dtf = DateTimeUtils.createFormatter(pattern);
		String value = getString(key, null);
		return (value == null) ? defaultValue : LocalTime.parse(value, dtf);
	}
	
	public boolean setTime(String key, LocalTime value, String pattern) {
		DateTimeFormatter dtf = DateTimeUtils.createFormatter(pattern);
		return setString(key, (value == null) ? null : dtf.print(value));
	}
	
	public LocalDate getDate(String key, String defaultValue, String pattern) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.getDefault());
		LocalDate lt = (defaultValue == null) ? null : LocalDate.parse(defaultValue, dtf);
		return getDate(key, lt, pattern);
	}
	
	public LocalDate getDate(String key, LocalDate defaultValue, String pattern) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.getDefault());
		String value = getString(key, null);
		return (value == null) ? defaultValue : LocalDate.parse(value, dtf);
	}
	
	public DateTime getDateTime(String key, DateTime defaultValue) {
		return getDateTime(key, defaultValue, DateTimeZone.UTC);
	}
	
	public DateTime getDateTime(String key, DateTime defaultValue, DateTimeZone dtz) {
		DateTimeFormatter dtf = DateTimeUtils.createYmdHmsFormatter(dtz);
		String value = getString(key, null);
		return (value == null) ? defaultValue : DateTime.parse(value, dtf);
	}
	
	public boolean setDateTime(String key, DateTime value) {
		return setDateTime(key, value, DateTimeZone.UTC);
	}
	
	public boolean setDateTime(String key, DateTime value, DateTimeZone dtz) {
		DateTimeFormatter dtf = DateTimeUtils.createYmdHmsFormatter(dtz);
		return setString(key, (value == null) ? null : dtf.print(value));
	}
	
	public <E extends Enum<E>> E getEnum(String key, E defaultValue, Class<E> enumClass) {
		E e = EnumUtils.forSerializedName(getString(key, null), null, enumClass);
		return (e != null) ? e : defaultValue;
	}
	
	public <E extends Enum<E>> boolean setEnum(String key, E en) {
		return setString(key, EnumUtils.toSerializedName(en));
	}
}
