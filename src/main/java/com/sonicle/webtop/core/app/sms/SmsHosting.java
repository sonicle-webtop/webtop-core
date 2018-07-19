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
package com.sonicle.webtop.core.app.sms;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.Locale;

/**
 *
 * @author gabriele.bulfon
 */
public class SmsHosting extends SmsProvider {
	
	private static final String WEBREST_SEND = "/sms/send";
	
	public SmsHosting(Locale locale, String webrestURL) {
		super(locale,webrestURL);
	}

	@Override
	public boolean send(String fromName, String fromNumber, String number, String text, String username, char[] password) throws WTException {
		boolean success=false;
		try {
			fromName=sanitizeFromName(fromName);
			fromNumber=sanitizeFromNumber(fromNumber);
			
			HttpResponse<JsonNode> resp=Unirest.post(webrestURL+WEBREST_SEND)
				.basicAuth(username, new String(password))
				.field("from",(fromNumber!=null && fromNumber.length()>0)?fromNumber:fromName)
				.field("to",number)
				.field("text",text)
				.asJson();
			switch(resp.getStatus()) {
				case 200:
					success=true;
					break;
					
				case 400:
					String errorMsg=resp.getBody().getObject().getString("errorMsg");
					String excMsg=errorMsg;
					if (errorMsg.equals("BAD_TEXT")) excMsg=WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_BAD_TEXT);
					else if (errorMsg.equals("BAD_FROM")) excMsg=WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_BAD_FROM);
					else if (errorMsg.equals("BAD_CREDIT")) excMsg=WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_BAD_CREDIT);
					else if (errorMsg.equals("NO_VALID_RECIPIENT")) excMsg=WT.lookupCoreResource(locale, CoreLocaleKey.SMS_ERROR_INVALID_RECIPIENT);
					throw new WTException(excMsg);
					
				case 401:
					throw new WTException("Invalid credentials");
					
				case 500:
					throw new WTException("Generic error");
			}
		} catch(UnirestException exc) {
			throw new WTException(exc.getMessage());
		}
		return success;
	}
	
	
}
