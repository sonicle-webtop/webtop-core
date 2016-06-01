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
public class UserPersonalInfo {
	private String title = null;
	private String firstName = null;
	private String lastName = null;
	private String nickname = null;
	private String gender = null;
	private String email = null;
	private String telephone = null;
	private String fax = null;
	private String pager = null;
	private String mobile = null;
	private String address = null;
	private String city = null;
	private String postalCode = null;
	private String state = null;
	private String country = null;
	private String company = null;
	private String function = null;
	private String custom1 = null;
	private String custom2 = null;
	private String custom3 = null;
	
	public UserPersonalInfo() {}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getPager() {
		return pager;
	}

	public void setPager(String pager) {
		this.pager = pager;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getCustom1() {
		return custom1;
	}

	public void setCustom1(String custom1) {
		this.custom1 = custom1;
	}

	public String getCustom2() {
		return custom2;
	}

	public void setCustom2(String custom2) {
		this.custom2 = custom2;
	}

	public String getCustom3() {
		return custom3;
	}

	public void setCustom3(String custom3) {
		this.custom3 = custom3;
	}
	
	
	
	public Map toMap() {
		HashMap<String, String> map = new HashMap();
		map.put("title", title);
		map.put("firstName", firstName);
		map.put("lastName", lastName);
		map.put("nickname", nickname);
		map.put("gender", gender);
		map.put("email", email);
		map.put("telephone", telephone);
		map.put("fax", fax);
		map.put("pager", pager);
		map.put("mobile", mobile);
		map.put("address", address);
		map.put("city", city);
		map.put("postalCode", postalCode);
		map.put("state", state);
		map.put("country", country);
		map.put("company", company);
		map.put("function", function);
		map.put("custom1", custom1);
		map.put("custom2", custom2);
		map.put("custom3", custom3);
		return map;
	}
	
	public void setValues(Map<String, Object> map) {
		title = String.valueOf(LangUtils.ifValue(map, "title", title));
		firstName =  String.valueOf(LangUtils.ifValue(map, "firstName", firstName));
		lastName =  String.valueOf(LangUtils.ifValue(map, "lastName", lastName));
		nickname =  String.valueOf(LangUtils.ifValue(map, "nickname", nickname));
		gender =  String.valueOf(LangUtils.ifValue(map, "gender", gender));
		email =  String.valueOf(LangUtils.ifValue(map, "email", email));
		telephone =  String.valueOf(LangUtils.ifValue(map, "telephone", telephone));
		fax =  String.valueOf(LangUtils.ifValue(map, "fax", fax));
		pager =  String.valueOf(LangUtils.ifValue(map, "pager", pager));
		mobile =  String.valueOf(LangUtils.ifValue(map, "mobile", mobile));
		address =  String.valueOf(LangUtils.ifValue(map, "address", address));
		city =  String.valueOf(LangUtils.ifValue(map, "city", city));
		postalCode =  String.valueOf(LangUtils.ifValue(map, "postalCode", postalCode));
		state =  String.valueOf(LangUtils.ifValue(map, "state", state));
		country =  String.valueOf(LangUtils.ifValue(map, "country", country));
		company =  String.valueOf(LangUtils.ifValue(map, "company", company));
		function =  String.valueOf(LangUtils.ifValue(map, "function", function));
		custom1 =  String.valueOf(LangUtils.ifValue(map, "custom1", custom1));
		custom2 =  String.valueOf(LangUtils.ifValue(map, "custom2", custom2));
		custom3 =  String.valueOf(LangUtils.ifValue(map, "custom3", custom3));
	}
}