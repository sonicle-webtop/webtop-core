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
package com.sonicle.webtop.core.io;

import java.util.Locale;
import java.util.TimeZone;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class ReportConfig {
	private final Locale locale;
	private final TimeZone timezone;
	private final boolean hasResourceBundle;
	private final String generatedBy;
	private final String printedBy;
	private final String dateFormatShort;
	private final String dateFormatLong;
	private final String timeFormatShort;
	private final String timeFormatLong;
	
	protected ReportConfig(Builder builder) {
		locale = builder.locale;
		timezone = builder.timezone;
		hasResourceBundle = builder.haveResourceBundle;
		generatedBy = builder.generatedBy;
		printedBy = builder.printedBy;
		dateFormatShort = builder.dateFormatShort;
		dateFormatLong = builder.dateFormatLong;
		timeFormatShort = builder.timeFormatShort;
		timeFormatLong = builder.timeFormatLong;
	}
	
	public Locale getLocale() {
		return this.locale;
	}
	
	public TimeZone getTimeZone() {
		return this.timezone;
	}
	
	public boolean getHasResourceBundle() {
		return this.hasResourceBundle;
	}
	
	public String getGeneratedBy() {
		return this.generatedBy;
	}
	
	public String getPrintedBy() {
		return this.printedBy;
	}
	
	public String getDateFormatShort() {
		return this.dateFormatShort;
	}
	
	public String getDateFormatLong() {
		return this.dateFormatLong;
	}
	
	public String getTimeFormatShort() {
		return this.timeFormatShort;
	}
	
	public String getTimeFormatLong() {
		return this.timeFormatLong;
	}
	
	public static class Builder<T extends Builder> {
		private Locale locale;
		private TimeZone timezone;
		private boolean haveResourceBundle;
		private String generatedBy;
		private String printedBy;
		private String dateFormatShort;
		private String dateFormatLong;
		private String timeFormatShort;
		private String timeFormatLong;
		
		public Builder() {
			locale = Locale.ENGLISH;
			timezone = DateTimeZone.UTC.toTimeZone();
			haveResourceBundle = false;
			dateFormatShort = "yyyy-MM-dd";
			dateFormatLong = "MMM dd, yyyy";
			timeFormatShort = "HH:mm";
			timeFormatLong = "HH:mm:ss";
		}
		
		public T useLocale(Locale locale) {
			this.locale = locale;
			return (T)this;
		}

		public T useTimeZone(TimeZone timezone) {
			this.timezone = timezone;
			return (T)this;
		}

		public T haveResourceBundle(boolean haveResourceBundle) {
			this.haveResourceBundle = haveResourceBundle;
			return (T)this;
		}

		public T generatedBy(String generatedBy) {
			this.generatedBy = generatedBy;
			return (T)this;
		}

		public T printedBy(String printedBy) {
			this.printedBy = printedBy;
			return (T)this;
		}

		public T dateFormatShort(String dateFormatShort) {
			this.dateFormatShort = dateFormatShort;
			return (T)this;
		}

		public T dateFormatLong(String dateFormatLong) {
			this.dateFormatLong = dateFormatLong;
			return (T)this;
		}

		public T timeFormatShort(String timeFormatShort) {
			this.timeFormatShort = timeFormatShort;
			return (T)this;
		}

		public T timeFormatLong(String timeFormatLong) {
			this.timeFormatLong = timeFormatLong;
			return (T)this;
		}
		
		public ReportConfig build() {
			return new ReportConfig(this);
		}
	}
}
