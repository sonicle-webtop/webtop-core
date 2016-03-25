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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author malbinola
 */
public class MemoryExcelFileReader extends FileRowsReader {
	protected boolean binary = false;
	protected String sheet = null;
	protected DataFormatter fmt = null;
	
	public MemoryExcelFileReader() {
		this.fmt = new DataFormatter();
	}
	
	public MemoryExcelFileReader(boolean binary) {
		this();
		this.binary = binary;
	}
	
	public String getSheet() {
		return this.sheet;
	}
	
	public void setSheet(String sheet) {
		this.sheet = sheet;
	}
	
	public List<String> listSheets(File file) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return listSheets(fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	public List<String> listSheets(InputStream is) throws IOException {
		ArrayList<String> sheets = new ArrayList<>();
		Workbook wb = createWorkbook(is);
		for(int i=0; i<wb.getNumberOfSheets(); i++) {
			sheets.add(wb.getSheetName(i));
		}
		return sheets;
	}
	
	@Override
	public HashMap<String, String> listColumnNames(File file) throws IOException, UnsupportedOperationException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return listColumnNames(fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	public HashMap<String, String> listColumnNames(InputStream is) throws IOException, UnsupportedOperationException {
		HashMap<String, String> hm = new LinkedHashMap<>();
		
		Workbook wb = createWorkbook(is);
		if(wb.getNumberOfSheets() == 0) throw new UnsupportedOperationException("At least one sheet is required");
		Sheet sh = getSheet(wb);
		if(sh == null) throw new UnsupportedOperationException("Unable to find desired sheet");
		
		String name = null;
		Row hrow = sh.getRow(headersRow-1);
		for(Cell cell : hrow) {
			if(headersRow == firstDataRow) {
				name = "col_" + CellReference.convertNumToColString(cell.getColumnIndex());
			} else {
				name = fmt.formatCellValue(cell);
				if(StringUtils.isBlank(name)) name = "col_" + CellReference.convertNumToColString(cell.getColumnIndex());
			}
			hm.put(name.toLowerCase(), name);
		}
		
		return hm;
	}
	
	public HashMap<String, Integer> listColumnIndexes(File file) throws IOException, UnsupportedOperationException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return listColumnIndexes(fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	public HashMap<String, Integer> listColumnIndexes(InputStream is) throws IOException, UnsupportedOperationException {
		HashMap<String, Integer> hm = new LinkedHashMap<>();
		
		Workbook wb = createWorkbook(is);
		if(wb.getNumberOfSheets() == 0) throw new UnsupportedOperationException("At least one sheet is required");
		Sheet sh = getSheet(wb);
		if(sh == null) throw new UnsupportedOperationException("Unable to find desired sheet");
		
		String name = null;
		Row hrow = sh.getRow(headersRow-1);
		for(Cell cell : hrow) {
			if(headersRow == firstDataRow) {
				name = "col_" + CellReference.convertNumToColString(cell.getColumnIndex());
			} else {
				name = fmt.formatCellValue(cell);
				if(StringUtils.isBlank(name)) name = "col_" + CellReference.convertNumToColString(cell.getColumnIndex());
			}
			hm.put(name.toLowerCase(), cell.getColumnIndex());
		}
		
		return hm;
	}
	
	protected Workbook createWorkbook(InputStream is) throws IOException {
		return (binary) ? new HSSFWorkbook(is) : new XSSFWorkbook(is);
	}
	
	protected Sheet getSheet(Workbook wb) {
		if(StringUtils.isBlank(sheet)) {
			return wb.getSheetAt(0);
		} else {
			return wb.getSheet(sheet);
		}
	}
}
