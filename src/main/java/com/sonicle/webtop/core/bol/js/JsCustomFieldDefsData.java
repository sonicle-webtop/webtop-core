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

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.model.CustomPanel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class JsCustomFieldDefsData {
	public String cfdefs;
	public ArrayList<ObjCustomFieldValue> cvalues;
	
	public JsCustomFieldDefsData(Collection<CustomPanel> customPanels, Map<String, CustomField> customFields, Map<String, CustomFieldValue> customFieldValues, String profileLanguageTag, DateTimeZone profileTz) {
		cvalues = new ArrayList<>();
		ArrayList<ObjCustomFieldDefs.Panel> panels = new ArrayList<>();
		for (CustomPanel panel : customPanels) {
			panels.add(new ObjCustomFieldDefs.Panel(panel, profileLanguageTag));
		}
		ArrayList<ObjCustomFieldDefs.Field> fields = new ArrayList<>();
		for (CustomField field : customFields.values()) {
			CustomFieldValue cvalue = (customFieldValues != null) ? customFieldValues.get(field.getFieldId()) : null;
			cvalues.add(cvalue != null ? new ObjCustomFieldValue(field.getType(), cvalue, profileTz) : new ObjCustomFieldValue(field.getType(), field.getFieldId()));
			fields.add(new ObjCustomFieldDefs.Field(field, profileLanguageTag));
		}
		cfdefs = LangUtils.serialize(new ObjCustomFieldDefs(panels, fields), ObjCustomFieldDefs.class);
	}
}
