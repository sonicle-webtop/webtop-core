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

import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.webtop.core.model.CustomPanel;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author malbinola
 */
public class JsCustomPanel {
	public String id;
	public String panelId;
	public String domainId;
	public String serviceId;
	public String name;
	public String description;
	public String title;
	public String tags;
	public ArrayList<Field> assocFields;
	public ArrayList<I18nValue> titleI18n;
	
	public JsCustomPanel(CustomPanel panel) {
		id = new CompositeId(panel.getServiceId(), panel.getPanelId()).toString();
		panelId = panel.getPanelId();
		domainId = panel.getDomainId();
		serviceId = panel.getServiceId();
		name = panel.getName();
		description = panel.getDescription();
		tags = new CompositeId(panel.getTags()).toString();
		assocFields = new ArrayList<>(panel.getFields().size());
		for (String fieldId : panel.getFields()) {
			assocFields.add(new Field(fieldId, assocFields.size()));
		}
		titleI18n = new ArrayList<>(panel.getTitleI18n().size());
		for (Map.Entry<String, String> entry : panel.getTitleI18n().entrySet()) {
			titleI18n.add(new JsCustomPanel.I18nValue(entry.getKey(), entry.getValue()));
		}
	}
	
	public CustomPanel toCustomPanel() {
		CustomPanel field = new CustomPanel();
		
		field.setPanelId(panelId);
		field.setDomainId(domainId);
		field.setServiceId(serviceId);
		field.setName(name);
		field.setDescription(description);
		field.setTags(new LinkedHashSet<>(new CompositeId().parse(tags).getTokens()));
		if ((assocFields != null) && !assocFields.isEmpty()) {
			field.setFields(
				assocFields.stream()
					.filter(item -> (item.id != null))
					.sorted((v1, v2) -> {
						return v1.order.compareTo(v2.order);
					})
					.map(item -> item.id)
					.collect(Collectors.toCollection(LinkedHashSet::new))
			);	
		}
		if ((titleI18n != null) && !titleI18n.isEmpty()) {
			field.setTitleI18n(
				titleI18n.stream()
					.filter(item -> (item.tag != null) && (item.txt != null))
					.collect(Collectors.toMap(item -> item.tag, item -> item.txt, (ov, nv) -> nv, CustomPanel.TitleI18n::new))
			);
		}
		
		return field;
	}
	
	public static class Field {
		public String id;
		public Integer order;
		
		public Field(String id, int order) {
			this.id = id;
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
