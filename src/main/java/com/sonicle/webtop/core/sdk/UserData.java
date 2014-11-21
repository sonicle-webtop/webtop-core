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

import com.sonicle.commons.LangUtils;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author malbinola
 */
public class UserData {
	
	public String title = null;
	public String firstName = null;
	public String lastName = null;
	public String address = null;
	public String postalCode = null;
	public String city = null;
	public String state = null;
	public String country = null;
	public String email = null;
	public String mobile = null;
	public String telephone = null;
	public String fax = null;
	public String company = null;
	public String function = null;
	public String workEmail = null;
	public String workMobile = null;
	public String workTelephone = null;
	public String workFax = null;
	public String custom1 = null;
	public String custom2 = null;
	public String custom3 = null;
	
	public UserData() {
		
	}
	
	public Map getMap() {
		HashMap<String, String> map = new HashMap();
		map.put("title", title);
		map.put("firstName", firstName);
		map.put("lastName", lastName);
		map.put("address", address);
		map.put("postalCode", postalCode);
		map.put("city", city);
		map.put("state", state);
		map.put("country", country);
		map.put("email", email);
		map.put("mobile", mobile);
		map.put("telephone", telephone);
		map.put("fax", fax);
		map.put("company", company);
		map.put("function", function);
		map.put("workEmail", workEmail);
		map.put("workMobile", workMobile);
		map.put("workTelephone", workTelephone);
		map.put("workFax", workFax);
		map.put("custom1", custom1);
		map.put("custom2", custom2);
		map.put("custom3", custom3);
		return map;
	}
	
	public void setMap(Map<String, Object> map) {
		title = String.valueOf(LangUtils.ifValue(map, "title", title));
		firstName =  String.valueOf(LangUtils.ifValue(map, "firstName", firstName));
		lastName =  String.valueOf(LangUtils.ifValue(map, "lastName", lastName));
		address =  String.valueOf(LangUtils.ifValue(map, "address", address));
		postalCode =  String.valueOf(LangUtils.ifValue(map, "postalCode", postalCode));
		city =  String.valueOf(LangUtils.ifValue(map, "city", city));
		state =  String.valueOf(LangUtils.ifValue(map, "state", state));
		country =  String.valueOf(LangUtils.ifValue(map, "country", country));
		email =  String.valueOf(LangUtils.ifValue(map, "email", email));
		mobile =  String.valueOf(LangUtils.ifValue(map, "mobile", mobile));
		telephone =  String.valueOf(LangUtils.ifValue(map, "telephone", telephone));
		fax =  String.valueOf(LangUtils.ifValue(map, "fax", fax));
		company =  String.valueOf(LangUtils.ifValue(map, "company", company));
		function =  String.valueOf(LangUtils.ifValue(map, "function", function));
		workEmail =  String.valueOf(LangUtils.ifValue(map, "workEmail", workEmail));
		workMobile =  String.valueOf(LangUtils.ifValue(map, "workMobile", workMobile));
		workTelephone =  String.valueOf(LangUtils.ifValue(map, "workTelephone", workTelephone));
		workFax =  String.valueOf(LangUtils.ifValue(map, "workFax", workFax));
		custom1 =  String.valueOf(LangUtils.ifValue(map, "custom1", custom1));
		custom2 =  String.valueOf(LangUtils.ifValue(map, "custom2", custom2));
		custom3 =  String.valueOf(LangUtils.ifValue(map, "custom3", custom3));
	}
}