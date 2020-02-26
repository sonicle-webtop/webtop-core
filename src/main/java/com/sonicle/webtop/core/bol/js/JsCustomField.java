/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.webtop.core.model.CustomField;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author malbinola
 */
public class JsCustomField {
	public String id;
	public String domainId;
	public String serviceId;
	public String fieldId;
	public String name;
	public String description;
	public String type;
	public ArrayList<Prop> props;
	public ArrayList<Value> values;
	public ArrayList<I18nValue> labelI18n;
	
	public JsCustomField(CustomField field) {
		id = new CompositeId(field.getServiceId(), field.getFieldId()).toString();
		domainId = field.getDomainId();
		serviceId = field.getServiceId();
		fieldId = field.getFieldId();
		name = field.getName();
		description = field.getDescription();
		type = EnumUtils.toSerializedName(field.getType());
		props = new ArrayList<>(field.getProps().size());
		for (Map.Entry<String, String> entry : field.getProps().entrySet()) {
			props.add(new Prop(entry.getKey(), entry.getValue()));
		}
		values = new ArrayList<>(field.getValues().size());
		for (Map.Entry<String, String> entry : field.getValues().entrySet()) {
			values.add(new Value(entry.getKey(), entry.getValue(), values.size()));
		}
		labelI18n = new ArrayList<>(field.getLabelI18n().size());
		for (Map.Entry<String, String> entry : field.getLabelI18n().entrySet()) {
			labelI18n.add(new I18nValue(entry.getKey(), entry.getValue()));
		}
	}
	
	public CustomField toCustomField() {
		CustomField field = new CustomField();
		
		field.setDomainId(domainId);
		field.setServiceId(serviceId);
		field.setFieldId(fieldId);
		field.setName(name);
		field.setDescription(description);
		field.setType(EnumUtils.forSerializedName(type, CustomField.Type.class));
		if ((props != null) && !props.isEmpty()) {
			field.setProps(
				props.stream()
					.filter(item -> (item.name != null) && (item.value != null))
					.collect(Collectors.toMap(item -> item.name, item -> item.value, (ov, nv) -> nv, CustomField.Props::new))
			);
		}
		if ((values != null) && !values.isEmpty()) {
			field.setValues(
				values.stream()
					.filter(item -> (item.key != null) && (item.desc != null))
					.sorted((v1, v2) -> {
						return v1.order.compareTo(v2.order);
					})
					.collect(Collectors.toMap(item -> item.key, item -> item.desc, (ov, nv) -> nv, CustomField.Values::new))
			);	
		}
		if ((labelI18n != null) && !labelI18n.isEmpty()) {
			field.setLabelI18n(
				labelI18n.stream()
					.filter(item -> (item.tag != null) && (item.txt != null))
					.collect(Collectors.toMap(item -> item.tag, item -> item.txt, (ov, nv) -> nv, CustomField.LabelI18n::new))
			);
		}
		
		return field;
	}
	
	public static class Prop {
		public String name;
		public String value;
		
		public Prop(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	public static class Value {
		public String key;
		public String desc;
		public Integer order;
		
		public Value(String key, String desc, int order) {
			this.key = key;
			this.desc = desc;
			this.order = order;
		}
	}
	
	public static class I18nValue {
		public String tag;
		public String txt;
		
		public I18nValue(String tag, String txt) {
			this.tag = tag;
			this.txt = txt;
		}
	}
}
