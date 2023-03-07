/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.util;

import com.sonicle.mail.MimeUtils;
import com.sonicle.mail.email.CalendarMethod;
import com.sonicle.mail.email.ContentTransferEncoding;
import com.sonicle.mail.email.EmailMessage;
import com.sonicle.mail.email.EmailMessageBuilder;
import com.sonicle.webtop.core.TplHelper;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import com.sonicle.webtop.core.sdk.WTException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Locale;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.parameter.PartStat;

/**
 * https://stackoverflow.com/questions/51516325/sending-calendar-events-invitations-using-ical4j-library-java-spring
 * https://www.rfc-editor.org/rfc/rfc5546#section-3.2.2
 * @author malbinola
 */
public class ICalendarHelper {
	
	public static EmailMessage prepareICalendarReply(final String prodId, final Calendar icalRequest, final InternetAddress forAddress, final InternetAddress organizerAddress, final PartStat response, final Locale locale) throws WTException {
		//final String prodId = ICalendarUtils.buildProdId(WT.getPlatformName() + " Mail");
		// Prepare reply
		final Calendar icalReply;
		try {
			icalReply = ICalendarUtils.buildInvitationReply(icalRequest, prodId, forAddress, response);
		} catch (URISyntaxException | ParseException | IOException ex) {
			throw new WTParseException(ex, "Unable to build reply from source request");
		}
		
		// Converts prepared reply into text
		final String calendarText;
		try {
			calendarText = ICalendarUtils.print(icalReply);
		} catch (IOException ex) {
			throw new WTException(ex, "Unable to generate reply text");
		}
		
		// Prepare iCalendar attachment
		final String attFilename = ICalendarUtils.buildICalendarAttachmentFilename(WT.getPlatformName());
		final ByteArrayDataSource attData;
		try {
			attData = new ByteArrayDataSource(calendarText, MimeUtils.CTYPE_APPLICATION_ICS);
		} catch (IOException ex) {
			throw new WTException(ex, "Unable to generate reply attachment");
		}
		
		final String summary = ICalendarUtils.getSummary(ICalendarUtils.getVEvent(icalRequest));
		return EmailMessageBuilder.startingBlank()
			.from(forAddress)
			.to(organizerAddress)
			.withSubject(TplHelper.buildEventInvitationReplyEmailSubject(locale, response, summary))
			.withCalendarText(CalendarMethod.REPLY, calendarText)
			.withAttachment(attData, attFilename, ContentTransferEncoding.BASE_64)
			.build();
	}
}
