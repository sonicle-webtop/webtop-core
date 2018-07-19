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

import com.sonicle.webtop.core.app.pbx.*;
import com.google.gson.annotations.SerializedName;
import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.Locale;

/**
 *
 * @author gabriele.bulfon
 */
public abstract class SmsProvider {
	
	public static enum SmsProviderName {
		@SerializedName("smshosting") SMSHOSTING,
		@SerializedName("twilio") TWILIO
	}
	
	public static final int FROM_NAME_MAX_LENGTH = 11;
	public static final int FROM_NUMBER_MAX_LENGTH = 16;
	
	protected String webrestURL;
	protected Locale locale;
	
	public SmsProvider(Locale locale, String webrestURL) {
		if (webrestURL.endsWith("/")) webrestURL=webrestURL.substring(0,webrestURL.length()-1);
		this.webrestURL=webrestURL;
		this.locale=locale;
	}
	
	public static final SmsProvider getInstance(Locale locale, String providerName, UserProfileId pid) {
		SmsProvider provider=null;
		if (SmsProviderName.SMSHOSTING.equals(EnumUtils.forSerializedName(providerName, SmsProviderName.class))) {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());		
			provider=new SmsHosting(locale, css.getSmsWebrestURL());
		}
		else if (SmsProviderName.TWILIO.equals(EnumUtils.forSerializedName(providerName, SmsProviderName.class))) {
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());		
			provider=new SmsHosting(locale, css.getSmsWebrestURL());
			
		}
		return provider;
	}
	
	public static String sanitizeFromName(String fromName) {
		if (fromName!=null && fromName.length()>FROM_NAME_MAX_LENGTH)
			fromName=fromName.substring(0,FROM_NAME_MAX_LENGTH);
		return fromName;
	}
	
	public static String sanitizeFromNumber(String fromNumber) {
		if (fromNumber!=null && fromNumber.length()>FROM_NUMBER_MAX_LENGTH)
			fromNumber=fromNumber.substring(0,FROM_NUMBER_MAX_LENGTH);
		return fromNumber;
	}
	
	public abstract boolean send(String fromName, String fromNumber, String number, String text, String username, char password[]) throws WTException;
	
}
