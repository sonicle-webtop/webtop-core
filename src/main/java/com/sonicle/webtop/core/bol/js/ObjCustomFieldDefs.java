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
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomPanel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public class ObjCustomFieldDefs {
	public Collection<Panel> panels;
	public Map<String, Field> fields;
	
	public ObjCustomFieldDefs(Collection<Panel> panels, Collection<Field> fields) {
		this.panels = panels;
		this.fields = fields.stream()
			.filter(item -> item.id != null)
			.collect(Collectors.toMap(item -> item.id, item -> item, (ov, nv) -> nv, HashMap<String, Field>::new));
	}
	
	public static String toJson(ObjCustomFieldDefs value) {
		return JsonResult.gson().toJson(value, ObjCustomFieldDefs.class);
	}
	
	public static class Panel {
		public String id;
		public String title;
		public Set<String> fields;
		public HashMap<String, String> props;
		public Boolean important;
		
		public Panel(CustomPanel fieldPanel, String languageTag) {
			id = fieldPanel.getPanelId();
			title = StringUtils.defaultIfBlank(fieldPanel.getTitleI18n().get(languageTag), fieldPanel.getName());
			fields = fieldPanel.getFields();
			props = new HashMap<>(fieldPanel.getProps().size());
			for (Map.Entry<String, String> entry : fieldPanel.getProps().entrySet()) {
				props.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public static class Field {
		public String id;
		public String name;
		public String label;
		public String desc;
		public String type;
		public Boolean required;
		public HashMap<String, String> props;
		public ArrayList<String[]> values;
		
		public Field(CustomField field, String languageTag) {
			id = field.getFieldId();
			name = field.getName();
			label = StringUtils.defaultIfBlank(field.getLabelI18n().get(languageTag), field.getName());
			desc = field.getDescription();
			type = EnumUtils.toSerializedName(field.getType());
			props = new HashMap<>(field.getProps().size());
			for (Map.Entry<String, String> entry : field.getProps().entrySet()) {
				props.put(entry.getKey(), entry.getValue());
			}
			values = new ArrayList<>(field.getValues().size());
			for (Map.Entry<String, String> entry : field.getValues().entrySet()) {
				values.add(new String[]{entry.getKey(), entry.getValue()});
			}
		}
	}
	
	public static class FieldsList extends ArrayList<Field> {

		public static String toJson(FieldsList value) {
			return JsonResult.gson().toJson(value, FieldsList.class);
		}
	}
}
