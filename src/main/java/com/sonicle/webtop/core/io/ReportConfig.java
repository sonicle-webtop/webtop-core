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

/**
 *
 * @author malbinola
 */
public class ReportConfig {
	protected Locale locale;
	protected TimeZone timezone;
	protected boolean hasResourceBundle;
	protected String generatedBy;
	protected String printedBy;
	protected String dateFormatShort = "dd/MM/yyyy";
	protected String dateFormatLong;
	protected String timeFormatShort = "HH:mm";
	protected String timeFormatLong;
	
	public Locale getLocale() {
		return this.locale;
	}
	
	public ReportConfig setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}
	
	public TimeZone getTimeZone() {
		return this.timezone;
	}
	
	public ReportConfig setTimeZone(TimeZone timezone) {
		this.timezone = timezone;
		return this;
	}
	
	public boolean getHasResourceBundle() {
		return this.hasResourceBundle;
	}
	
	public ReportConfig setHasResourceBundle(boolean hasResourceBundle) {
		this.hasResourceBundle = hasResourceBundle;
		return this;
	}
	
	public String getGeneratedBy() {
		return this.generatedBy;
	}
	
	public ReportConfig setGeneratedBy(String generatedBy) {
		this.generatedBy = generatedBy;
		return this;
	}
	
	public String getPrintedBy() {
		return this.printedBy;
	}
	
	public ReportConfig setPrintedBy(String printedBy) {
		this.printedBy = printedBy;
		return this;
	}
	
	public String getDateFormatShort() {
		return this.dateFormatShort;
	}
	
	public ReportConfig setDateFormatShort(String dateFormatShort) {
		this.dateFormatShort = dateFormatShort;
		return this;
	}
	
	public String getDateFormatLong() {
		return this.dateFormatLong;
	}
	
	public ReportConfig setDateFormatLong(String dateFormatLong) {
		this.dateFormatLong = dateFormatLong;
		return this;
	}
	
	public String getTimeFormatShort() {
		return this.timeFormatShort;
	}
	
	public ReportConfig setTimeFormatShort(String timeFormatShort) {
		this.timeFormatShort = timeFormatShort;
		return this;
	}
	
	public String getTimeFormatLong() {
		return this.timeFormatLong;
	}
	
	public ReportConfig setTimeFormatLong(String timeFormatLong) {
		this.timeFormatLong = timeFormatLong;
		return this;
	}
}
