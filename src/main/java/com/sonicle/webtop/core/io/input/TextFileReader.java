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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author malbinola
 */
public class TextFileReader extends FileRowsReader {
	public static final String UNNAMED_COL_PREFIX = "COL-";
	public static final String FIELD_DELIMITER_TAB = "tab";
	public static final String FIELD_DELIMITER_COMMA = "comma";
	public static final String FIELD_DELIMITER_SPACE = "space";
	public static final String FIELD_DELIMITER_SEMICOLON = "semicolon";
	public static final String TEXT_QUALIFIER_SINGLE_QUOTE = "quote";
	public static final String TEXT_QUALIFIER_DOUBLE_QUOTE = "dblquote";
	public static final String RECORD_SEPATATOR_CR = "cr";
	public static final String RECORD_SEPATATOR_LF = "lf";
	public static final String RECORD_SEPATATOR_CRLF = "crlf";
	
	protected CsvPreference pref;
	protected Charset charset = Charset.defaultCharset();
	
	public TextFileReader(CsvPreference pref) {
		this.pref = pref;
	}
	
	public TextFileReader(CsvPreference pref, String charsetName) {
		this.pref = pref;
		this.charset = Charset.forName(charsetName);
	}
	
	@Override
	public Map<String, String> listColumnNames(File file) throws IOException, UnsupportedOperationException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return listColumnNames(fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	public Map<String, String> listColumnNames(InputStream is) throws IOException, UnsupportedOperationException {
		HashMap<String, String> hm = new LinkedHashMap<>();
		CsvListReader lr = new CsvListReader(new InputStreamReader(is, charset), pref);
		
		String name = null;
		List<String> line = null;
		while ((line = lr.read()) != null) {
			if (lr.getLineNumber() == headersRow) {
				for (int i=0; i<line.size(); i++) {
					if (headersRow == firstDataRow) {
						name = UNNAMED_COL_PREFIX + i+1;
					} else {
						name = StringUtils.defaultIfBlank(line.get(i), UNNAMED_COL_PREFIX + i+1);
					}
					hm.put(toColumnNameKey(name), name);
				}
				break;
			}
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
		CsvListReader lr = new CsvListReader(new InputStreamReader(is, charset), pref);
		
		String name = null;
		List<String> line = null;
		while ((line = lr.read()) != null) {
			if (lr.getLineNumber() == headersRow) {
				for (int i=0; i<line.size(); i++) {
					if (headersRow == firstDataRow) {
						name = UNNAMED_COL_PREFIX + i+1;
					} else {
						name = StringUtils.defaultIfBlank(line.get(i), UNNAMED_COL_PREFIX + i+1);
					}
					hm.put(name, i);
				}
				break;
			}
		}
		
		return hm;
	}
	
	public static CsvPreference buildCsvPreference(String fieldDelimiter, String recordSeparator) {
		return buildCsvPreference(fieldDelimiter, recordSeparator, null);
	}
	
	public static CsvPreference buildCsvPreference(String fieldDelimiter, String recordSeparator, String textQualifier) {
		int delimiterChar;
		char quoteChar;
		String endOfLineSymbols;
		
		if (fieldDelimiter.equals(FIELD_DELIMITER_TAB)) {
			delimiterChar = ((int)'\t');
		} else if (fieldDelimiter.equals(FIELD_DELIMITER_COMMA)) {
			delimiterChar = ((int)',');
		} else if (fieldDelimiter.equals(FIELD_DELIMITER_SPACE)) {
			delimiterChar = ((int)' ');
		} else if (fieldDelimiter.equals(FIELD_DELIMITER_SEMICOLON)) {
			delimiterChar = ((int)';');
		} else {
			throw new UnsupportedOperationException("Field delimiter not supported [" + fieldDelimiter + "]");
		}
		
		if (recordSeparator.equals(RECORD_SEPATATOR_CR)) {
			endOfLineSymbols = "\r";
		} else if (recordSeparator.equals(RECORD_SEPATATOR_LF)) {
			endOfLineSymbols = "\n";
		} else if (recordSeparator.equals(RECORD_SEPATATOR_CRLF)) {
			endOfLineSymbols = "\r\n";
		} else {
			throw new UnsupportedOperationException("Record separator not supported [" + recordSeparator + "]");
		}
		
		if (textQualifier == null) {
			quoteChar = ' ';
		} else if (textQualifier.equals(TEXT_QUALIFIER_SINGLE_QUOTE)) {
			quoteChar = '\'';
		} else if (textQualifier.equals(TEXT_QUALIFIER_DOUBLE_QUOTE)) {
			quoteChar = '"';
		} else {
			throw new UnsupportedOperationException("Text qualifier not supported [" + textQualifier + "]");
		}
		
		return new CsvPreference.Builder(quoteChar, delimiterChar, endOfLineSymbols).build();
	}
}
