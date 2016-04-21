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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.bol.OSnoozedReminder;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.UserProfile;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsReminderInApp {
	public String serviceId;
	public String type;
	public String instanceId;
	public String title;
	public String date;
	public String timezone;
	
	public JsReminderInApp(ReminderInApp rem) {
		//DateTimeFormatter ymdhmsFmt = DateTimeUtils.createYmdHmsFormatter(DateTimeZone.forID(rem.getTimezone()));
		DateTimeZone tz = DateTimeZone.forID(rem.getTimezone());
		serviceId = rem.getServiceId();
		type = rem.getType();
		instanceId = rem.getInstanceId();
		title = rem.getTitle();
		date = DateTimeUtils.printYmdHmsWithZone(rem.getDate(), tz);
		//if(rem.getDate() != null) date = ymdhmsFmt.print(rem.getDate());
		timezone = rem.getTimezone();
	}
	
	public JsReminderInApp(OSnoozedReminder rem) {
		//DateTimeFormatter ymdhmsFmt = DateTimeUtils.createYmdHmsFormatter(rem.getDateTimeZone());
		DateTimeZone tz = DateTimeZone.forID(rem.getTimezone());
		serviceId = rem.getServiceId();
		type = rem.getType();
		instanceId = rem.getInstanceId();
		title = rem.getTitle();
		date = DateTimeUtils.printYmdHmsWithZone(rem.getDate(), tz);
		//if(rem.getDate() != null) date = ymdhmsFmt.print(rem.getDate());
		timezone = rem.getTimezone();
	}
	
	public static ReminderInApp createReminderInApp(UserProfile.Id profileId, JsReminderInApp js) {
		ReminderInApp rem = new ReminderInApp(js.serviceId, profileId, js.type, js.instanceId);
		rem.setTitle(js.title);
		rem.setDate(DateTimeUtils.parseYmdHmsWithZone(js.date, DateTimeZone.forID(js.timezone)));
		rem.setTimezone(js.timezone);
		return rem;
	}
	
	public static class List extends ArrayList<JsReminderInApp> {
		public List() {
			super();
		}
	}
}
