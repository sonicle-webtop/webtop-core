/*
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 *
 * @author malbinola
 */
public class JsCustomFieldValue {
	public String id;
	public String ftype;
	public String vtype;
	public String st;
	public Double nu;
	public Boolean bo;
	public String da;
	public String ti;
	public String dt;
	
	public JsCustomFieldValue(CustomField.Type fieldType, String fieldId) {
		id = fieldId;
		ftype = EnumUtils.toSerializedName(fieldType);
		vtype = JsCustomFieldValue.toVType(fieldType);
	}
	
	public JsCustomFieldValue(CustomField.Type fieldType, CustomFieldValue fieldValue, DateTimeZone userTimezone) {
		this(fieldType, fieldValue.getFieldId());
		Object value = fieldValue.getValue(fieldType);
		if (value != null) {
			if (value instanceof String) {
				st = (String)value;
			} else if (value instanceof Double) {
				nu = (Double)value;
			} else if (value instanceof Boolean) {
				bo = (Boolean)value;
			} else if (value instanceof LocalDate) {
				da = DateTimeUtils.createYmdFormatter(DateTimeZone.UTC).print((LocalDate)value);
			} else if (value instanceof LocalTime) {
				ti = DateTimeUtils.createHmsFormatter(DateTimeZone.UTC).print((LocalTime)value);
			} else if (value instanceof DateTime) {
				dt = DateTimeUtils.createYmdHmsFormatter(userTimezone).print((DateTime)value);
			}
		}
	}
	
	public CustomFieldValue toCustomFieldValue(DateTimeZone userTimezone) {
		CustomField.Type type = EnumUtils.forSerializedName(ftype, CustomField.Type.class);
		CustomFieldValue obj = new CustomFieldValue();
		
		obj.setFieldId(id);
		if ("st".equals(vtype)) {
			obj.setValue(type, st);
		} else if ("nu".equals(vtype)) {
			obj.setValue(type, nu);
		} else if ("bo".equals(vtype)) {
			obj.setValue(type, bo);
		} else if ("da".equals(vtype)) {
			obj.setValue(type, DateTimeUtils.createYmdFormatter(DateTimeZone.UTC).parseLocalDate(da));
		} else if ("ti".equals(vtype)) {
			obj.setValue(type, DateTimeUtils.createHmsFormatter(DateTimeZone.UTC).parseLocalTime(ti));
		} else if ("dt".equals(vtype)) {
			obj.setValue(type, DateTimeUtils.createYmdHmsFormatter(userTimezone).parseLocalDate(dt));
		}
		
		return obj;
	}
	
	public static String toVType(CustomField.Type fieldType) {
		if (CustomField.Type.TEXT.equals(fieldType) || CustomField.Type.TEXTAREA.equals(fieldType)) {
			return "st";
		} else if (CustomField.Type.NUMBER.equals(fieldType)) {
			return "nu";
		} else if (CustomField.Type.DATE.equals(fieldType)) {
			return "da";
		} else if (CustomField.Type.TIME.equals(fieldType)) {
			return "ti";
		} else if (CustomField.Type.DATE_TIME.equals(fieldType)) {
			return "dt";
		} else if (CustomField.Type.COMBOBOX.equals(fieldType)) {
			return "st";
		} else if (CustomField.Type.CHECKBOX.equals(fieldType)) {
			return "bo";
		} else {
			return null;
		}
	}
}
