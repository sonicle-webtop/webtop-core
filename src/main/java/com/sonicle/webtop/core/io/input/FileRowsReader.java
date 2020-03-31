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
package com.sonicle.webtop.core.io.input;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class FileRowsReader {
	protected int headersRow = 1;
	protected int firstDataRow = 2;
	protected int lastDataRow = -1;
	protected boolean columnStrictMapping = false;
	
	public abstract Map<String, String> listColumnNames(File file) throws IOException, FileReaderException;
	
	public void setHeadersRow(int headersRow) {
		if(headersRow < 0) {
			this.headersRow = 1;
		} else if (headersRow > this.firstDataRow) {
			this.headersRow = this.firstDataRow;
		} else {
			this.headersRow = headersRow;
		}
	}
	
	public void setFirstDataRow(int firstDataRow) {
		if(firstDataRow < this.headersRow) {
			this.firstDataRow = this.headersRow;
		} else {
			this.firstDataRow = firstDataRow;
		}
	}
	
	public void setLastDataRow(int lastDataRow) {
		if(lastDataRow < this.firstDataRow) {
			this.lastDataRow = this.firstDataRow;
		} else {
			this.lastDataRow = lastDataRow;
		}
	}
	
	public List<FieldMapping> listFieldMappings(File file, String[] targetFields) throws IOException, FileReaderException {
		ArrayList<FieldMapping> mappings = new ArrayList<>();
		Map<String, String> cols = listColumnNames(file);
		
		for (int i=0; i<targetFields.length; i++) {
			String source = null;
			String lower = columnStrictMapping ? targetFields[i] : targetFields[i].toLowerCase();
			if (cols.containsKey(lower)) {
				source = cols.get(lower);
			}
			mappings.add(new FieldMapping(targetFields[i], source));
		}
		
		return mappings;
	}
	
	protected String toColumnNameKey(String name) {
		if (columnStrictMapping) {
			return name;
		} else {
			String s = StringUtils.replace(name.toLowerCase(), " ", "");
			s = StringUtils.replace(s, "_", "");
			s = StringUtils.replace(s, "-", "");
			return s;
		}
	}
	
	public static class FieldMapping {
		public String target;
		public String source;
		
		public FieldMapping() {}
		
		public FieldMapping(String target, String source) {
			this.target = target;
			this.source = source;
		}
	}
}
