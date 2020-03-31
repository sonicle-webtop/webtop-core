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

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

/**
 *
 * @author malbinola
 */
public class XlsxColumnsHandler extends XlsxBaseHandler implements SheetContentsHandler {
	protected ColumnMapper mapper;
	protected boolean isInRange = false;
	public LinkedHashMap<String, String> columnsMapping;
	public HashMap<String, Integer> columnIndexes;

	public XlsxColumnsHandler(InputStream is, int headersRow, int firstDataRow, int lastDataRow, ColumnMapper mapper) {
		super(is, headersRow, firstDataRow, lastDataRow);
		this.mapper = mapper;
	}

	@Override
	public void startRow(int i) {
		row = i;
		isHeader = (row == headersRow);
		if (isHeader) {
			columnsMapping = new LinkedHashMap<>();
			columnIndexes = new HashMap<>();
		}
		isInRange = ((row >= firstDataRow) && ((lastDataRow == -1) || (row <= lastDataRow)));
	}

	@Override
	public void endRow(int i) {
		if (isHeader) close();
	}

	@Override
	public void cell(String cellReference, String formattedValue, XSSFComment comment) {
		if (isHeader) {
			String name = (headersRow == firstDataRow) ? cellReference : StringUtils.defaultIfBlank(formattedValue, cellReference);
			if (mapper != null) columnsMapping.put(mapper.mapColumnName(name), name);
			final int col = new CellReference(cellReference).getCol();
			columnIndexes.put(name, col);

			/*
			String name = null;
			if(headersRow == firstDataRow) {
				columnNames.put(cellReference, cellReference);
			} else {
				if(!StringUtils.isBlank(cellReference)) {
					columnNames.put(cellReference.toLowerCase(), cellReference);
				} else {
					columnNames.put(cellReference, cellReference);
				}
			}
			columnNames.put(name.toLowerCase(), name);
			*/
		}
	}

	@Override
	public void headerFooter(String string, boolean bln, String string1) {}
	
	public static interface ColumnMapper {
		public String mapColumnName(String columnName);
	}
}
