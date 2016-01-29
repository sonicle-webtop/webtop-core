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

import com.sonicle.webtop.core.sdk.bol.js.JsUserOptionsBase;

/**
 *
 * @author malbinola
 */
public class JsUserOptions extends JsUserOptionsBase {
	public String displayName;
	public String theme;
	public String layout;
	public String laf;
	public String language;
	public String timezone;
	public Integer startDay;
	public String shortDateFormat;
	public String longDateFormat;
	public String shortTimeFormat;
	public String longTimeFormat;
	
	public String upiTitle;
	public String upiFirstName;
	public String upiLastName;
	public String upiNickname;
	public String upiGender;
	public String upiEmail;
	public String upiTelephone;
	public String upiFax;
	public String upiPager;
	public String upiMobile;
	public String upiAddress;
	public String upiPostalCode;
	public String upiCity;
	public String upiState;
	public String upiCountry;
	public String upiCompany;
	public String upiFunction;
	public String upiCustom1;
	public String upiCustom2;
	public String upiCustom3;
	
	public Boolean syncAlertEnabled;
	public Integer syncAlertTolerance;
	
	public Boolean otpEnabled;
	public String otpDelivery;
	public String otpEmailAddress;
	public Boolean otpDeviceIsTrusted;
	public String otpDeviceTrustedOn;
	
	public Boolean canManageUpi; // Read-only
	public Boolean canSyncDevices; // Read-only
	
	public JsUserOptions() {
		super();
	}
	
	public JsUserOptions(String id) {
		super(id);
	}
}
