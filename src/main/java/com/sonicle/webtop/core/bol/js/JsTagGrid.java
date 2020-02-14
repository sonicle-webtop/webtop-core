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

import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.model.Tag;
import java.util.ArrayList;

/**
 *
 * @author malbinola
 */
public class JsTagGrid {
	public String id;
	public Boolean builtIn;
	public String name;
	public String color;
	
	public JsTagGrid(Tag tag) {
		id = tag.getTagId();
		builtIn = tag.getBuiltIn();
		name = tag.getName();
		color = tag.getColor();
	}
	
	public Tag toTag() {
		Tag item = new Tag();
		
		item.setTagId(id);
		item.setBuiltIn(builtIn);
		item.setName(name);
		item.setColor(color);
		
		return item;
	}
	
	public static class List extends ArrayList<JsTagGrid> {
		public static JsTagGrid.List fromJson(String value) {
			return JsonResult.gson.fromJson(value, JsTagGrid.List.class);
		}

		public static String toJson(JsTagGrid.List value) {
			return JsonResult.gson.toJson(value, JsTagGrid.List.class);
		}
	}
}
