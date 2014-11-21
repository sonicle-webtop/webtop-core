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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 *
 * @author malbinola
 */
public class JsOptions extends HashMap<String, Object> {
	
	public JsOptions() {
		super();
	}

	public String getString(String key) {
		return (String) get(key);
	}
	
	public Boolean getBoolean(String key) {
		return (Boolean) get(key);
	}
	
	public Integer getInteger(String key) {
		return (Integer) get(key);
	}
	
	public Float getFloat(String key) {
		return (Float) get(key);
	}
	
	public Double getDouble(String key) {
		return (Double) get(key);
	}
	
	public Long getLong(String key) {
		return (Long) get(key);
	}
	
	public Date getDate(String key) {
		return (Date) get(key);
	}
	
	/*
	public void putPrefixed(String prefix, Map<? extends String, ? extends Object> m) {
		for(Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
			put(prefix + WordUtils.capitalize(entry.getKey()), entry.getValue());
		}
	}
	*/
	
	public void putPrefixed(String prefix, Map<String, Object> m) {
		for(Map.Entry<String, Object> entry : m.entrySet()) {
			put(prefix + WordUtils.capitalize(entry.getKey()), entry.getValue());
		}
	}
	
	public Map<String, Object> getPrefixed(String prefix) {
		JsOptions map = new JsOptions();
		for(Map.Entry<String, Object> entry : entrySet()) {
			if(StringUtils.startsWith(entry.getKey(), prefix)) {
				map.put(WordUtils.uncapitalize(StringUtils.removeStart(entry.getKey(), prefix)), entry.getValue());
			}
		}
		return map;
	}
}
