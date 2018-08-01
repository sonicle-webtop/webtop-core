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
package com.sonicle.webtop.core.app.sms;

import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.WT;
import static com.sonicle.webtop.core.app.sms.SmsProvider.sanitizeFromNumber;
import com.sonicle.webtop.core.sdk.WTException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author gabriele.bulfon
 */
public class Twilio extends SmsProvider {
	// Base URL is not useful here, using Twilio java api seems that treat endpoint URL updates automatically!
	private static final String BASE_URL = "https://api.twilio.com/2010-04-01/Account";
	
	public Twilio(Locale locale, String baseUrl) {
		super(locale, StringUtils.defaultIfBlank(baseUrl, BASE_URL));
	}

	@Override
	public boolean send(String fromName, String fromNumber, String number, String text, String username, char[] password) throws WTException {
		boolean success=false;
		try {
			com.twilio.Twilio.init(username, new String(password));
			//fromName=sanitizeFromName(fromName);
			fromNumber=sanitizeFromNumber(fromNumber);
			if (fromNumber==null)
				throw new WTException(WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_BAD_FROM));
			
			PhoneNumber pnTo=new PhoneNumber(number);
			PhoneNumber pnFrom=new PhoneNumber(fromNumber);
			Message sms=Message.creator(pnTo, pnFrom, text).create();
			Message.Status status=sms.getStatus();
			if (status==status.FAILED || status==status.UNDELIVERED) {
				int err=sms.getErrorCode();
				String excMsg=sms.getErrorMessage();
				switch(err) {
					case 63001:
						excMsg="Invalid credentials";
						break;
					
					case 63002:
					case 63007:
						excMsg=WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_BAD_FROM);
						break;
						
					case 63003:
						excMsg=WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_INVALID_RECIPIENT);
						break;
						
					case 63005:
					case 63006:
						excMsg=WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_BAD_TEXT);
						break;
						
					
				}
			} else {
				success=true;
			}
		} catch(Exception exc) {
			throw new WTException(exc.getMessage());
		}
		return success;
	}
	
	
}
