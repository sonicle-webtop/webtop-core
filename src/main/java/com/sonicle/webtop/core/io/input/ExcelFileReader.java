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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author malbinola
 */
public class ExcelFileReader extends FileRowsReader {
	protected boolean binary = false;
	protected String sheet = null;
	protected DataFormatter fmt = null;
	
	public ExcelFileReader() {
		this.fmt = new DataFormatter();
	}
	
	public ExcelFileReader(boolean binary) {
		this();
		this.binary = binary;
	}
	
	public String getSheet() {
		return this.sheet;
	}
	
	public void setSheet(String sheet) {
		this.sheet = sheet;
	}
	
	public List<String> listSheets(File file) throws IOException, FileReaderException {
		if(binary) {
			return listXlsSheets(file);
		} else {
			return listXlsxSheets(file);
		}
	}
	
	public List<String> listXlsxSheets(File file) throws IOException, FileReaderException {
		ArrayList<String> sheets = new ArrayList<>();
		OPCPackage opc = null;
		
		try {
			opc = OPCPackage.open(file, PackageAccess.READ);
			XSSFReader reader = new XSSFReader(opc);
			XSSFReader.SheetIterator sit = (XSSFReader.SheetIterator) reader.getSheetsData();
			while(sit.hasNext()) {
				InputStream is = null;
				try {
					is = sit.next();
					String name = sit.getSheetName();
					if(name != null) sheets.add(name);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		} catch(OpenXML4JException ex) {
			throw new FileReaderException(ex, "Error opening file");
		} finally {
			IOUtils.closeQuietly(opc);
		}
		return sheets;
	}
	
	public List<String> listXlsSheets(File file) throws IOException, FileReaderException {
		POIFSFileSystem pfs = null;
		InputStream is = null;
		
		try {
			pfs = new POIFSFileSystem(file);
			is = pfs.createDocumentInputStream("Workbook");
			XlsSheetsProcessor processor = new XlsSheetsProcessor(is);
			processor.process();
			return processor.sheetNames;
			
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(pfs);
		}
	}
	
	@Override
	public HashMap<String, String> listColumnNames(File file) throws IOException, FileReaderException {
		if(binary) {
			return listXlsColumnNames(file);
		} else {
			return listXlsxColumnNames(file);
		}
	}
	
	public HashMap<String, String> listXlsxColumnNames(File file) throws IOException, FileReaderException {
		OPCPackage opc = null;
		
		try {
			opc = OPCPackage.open(file, PackageAccess.READ);
			XSSFReader reader = new XSSFReader(opc);
			ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);
			StylesTable styles = reader.getStylesTable();
			
			XlsxColumnsHandler columnsHandler = null;
			XSSFReader.SheetIterator sit = (XSSFReader.SheetIterator) reader.getSheetsData();
			while(sit.hasNext()) {
				InputStream is = null;
				try {
					is = sit.next();
					if(StringUtils.equals(sit.getSheetName(), sheet)) {
						XMLReader xmlReader = SAXHelper.newXMLReader();
						columnsHandler = new XlsxColumnsHandler(is, headersRow, firstDataRow, lastDataRow);
						ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, columnsHandler, fmt, false);
						xmlReader.setContentHandler(handler);
						xmlReader.parse(new InputSource(is));
					}
				} catch(SAXException | ParserConfigurationException ex) {
					throw new FileReaderException(ex, "Error processing file content");
				} catch(NullPointerException ex) {
					// Thrown when stream is forcibly closed. Simply ignore this!
				} finally {
					IOUtils.closeQuietly(is);
				}
				if(columnsHandler != null) break;
			}
			return columnsHandler.columnNames;
			
		} catch(OpenXML4JException | SAXException ex) {
			throw new FileReaderException(ex, "Error opening file");
		} finally {
			IOUtils.closeQuietly(opc);
		}
	}
	
	public HashMap<String, String> listXlsColumnNames(File file) throws IOException, FileReaderException {
		POIFSFileSystem pfs = null;
		InputStream is = null;
		
		try {
			pfs = new POIFSFileSystem(file);
			is = pfs.createDocumentInputStream("Workbook");
			XlsColumnsProcessor processor = new XlsColumnsProcessor(is, headersRow, firstDataRow, lastDataRow, sheet);
			processor.process();
			return processor.columnNames;
			
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(pfs);
		}
	}
	
	public HashMap<String, Integer> listColumnIndexes(File file) throws IOException, FileReaderException {
		if(binary) {
			return listXlsColumnIndexes(file);
		} else {
			return listXlsxColumnIndexes(file);
		}
	}
	
	public HashMap<String, Integer> listXlsxColumnIndexes(File file) throws IOException, FileReaderException {
		OPCPackage opc = null;
		
		try {
			opc = OPCPackage.open(file, PackageAccess.READ);
			XSSFReader reader = new XSSFReader(opc);
			ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);
			StylesTable styles = reader.getStylesTable();
			
			XlsxColumnsHandler columnsHandler = null;
			XSSFReader.SheetIterator sit = (XSSFReader.SheetIterator) reader.getSheetsData();
			while(sit.hasNext()) {
				InputStream is = null;
				try {
					is = sit.next();
					if(StringUtils.equals(sit.getSheetName(), sheet)) {
						XMLReader xmlReader = SAXHelper.newXMLReader();
						columnsHandler = new XlsxColumnsHandler(is, headersRow, firstDataRow, lastDataRow);
						ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, columnsHandler, fmt, false);
						xmlReader.setContentHandler(handler);
						xmlReader.parse(new InputSource(is));
					}
				} catch(SAXException | ParserConfigurationException ex) {
					throw new FileReaderException(ex, "Error processing file content");
				} catch(NullPointerException ex) {
					// Thrown when stream is forcibly closed. Simply ignore this!
				} finally {
					IOUtils.closeQuietly(is);
				}
				if(columnsHandler != null) break;
			}
			return columnsHandler.columnIndexes;
			
		} catch(OpenXML4JException | SAXException ex) {
			throw new FileReaderException(ex, "Error opening file");
		} finally {
			IOUtils.closeQuietly(opc);
		}
	}
	
	public HashMap<String, Integer> listXlsColumnIndexes(File file) throws IOException, FileReaderException {
		POIFSFileSystem pfs = null;
		InputStream is = null;
		
		try {
			pfs = new POIFSFileSystem(file);
			is = pfs.createDocumentInputStream("Workbook");
			XlsColumnsProcessor processor = new XlsColumnsProcessor(is, headersRow, firstDataRow, lastDataRow, sheet);
			processor.process();
			return processor.columnIndexes;
			
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(pfs);
		}
	}
}
