/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.webtop.core.app.model.RMeToken;
import net.sf.uadetector.ReadableUserAgent;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsRMeSessionGrid {
	public String id;
	public String selector;
	public String created;
	public String expires;
	public String lastSeen;
	public String clientId;
	public String device;
	public String deviceCategory;
	public String deviceOS;
	public String deviceType;
	
	public JsRMeSessionGrid(RMeToken rmeToken, ReadableUserAgent rua, DateTimeZone profileTz) {
		DateTimeFormatter ymdhmsFmt = JodaTimeUtils.createFormatterYMDHMS(profileTz);
		
		this.id = String.valueOf(rmeToken.getId());
		this.selector = rmeToken.getSelector();
		this.created = JodaTimeUtils.print(ymdhmsFmt, rmeToken.getIssuedAt());
		this.expires = JodaTimeUtils.print(ymdhmsFmt, rmeToken.getExpiresAt());
		this.lastSeen = JodaTimeUtils.print(ymdhmsFmt, rmeToken.getLastUsedAt());
		this.clientId = rmeToken.getClientId();
		this.device = "";
		if (rua != null) {
			this.device = LangUtils.joinStrings(", ", toDeviceCategoryName(rua), rua.getOperatingSystem().getName(), rua.getName());
			this.deviceCategory = rua.getDeviceCategory().getCategory().toString(); // eg. PERSONAL_COMPUTER
			this.deviceOS = rua.getOperatingSystem().getFamily().toString(); // eg. WINDOWS
			this.deviceType = rua.getType().toString(); // eg. BROWSER
		}
	}
	
	private String toDeviceCategoryName(ReadableUserAgent rua) {
		if ("PERSONAL_COMPUTER".equals(rua.getDeviceCategory().getCategory().toString())) {
			return "PC";
		} else {
			return rua.getDeviceCategory().getName();
		}
	}
}
