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
package com.sonicle.webtop.core.versioning;

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class SqlUpgradeScript {
	private static final Pattern PATTERN_JAR_FILENAME = Pattern.compile("^(([0-9]+(?:\\.[0-9]+){1,2})_([0-9]+\\$?))\\.sql$");
	private static final Pattern PATTERN_FILE_FILENAME = Pattern.compile("^(.+)\\.sql$");
	private String resourceName = null;
	private String fileName = null;
	private ServiceVersion fileVersion = null;
	private String fileSequence = null;
	private ArrayList<BaseScriptLine> statements = null;
	
	public static ServiceVersion extractVersion(String resourceName) {
		Matcher matcher = PATTERN_JAR_FILENAME.matcher(resourceName);
		if(!matcher.matches()) throw new UnsupportedOperationException(MessageFormat.format("Bad resource name [{0}]", resourceName));
		return new ServiceVersion(matcher.group(2));
	}
	
	public SqlUpgradeScript(File file) throws IOException, UnsupportedOperationException {
		InputStream is = null;
		FileReader fr = null;
		
		try {
			Matcher matcher = PATTERN_FILE_FILENAME.matcher(file.getName());
			if(!matcher.matches()) throw new UnsupportedOperationException(MessageFormat.format("Bad resource name [{0}]", file.getName()));
			
			this.resourceName = file.getCanonicalPath();
			this.fileName = matcher.group(1);
			this.fileVersion = new ServiceVersion();
			this.fileSequence = "";
			
			boolean rawContent = false;
			if (StringUtils.endsWith(fileName, "$")) {
				rawContent = true;
			}
			
			fr = new FileReader(file);
			readFile(fr, rawContent, true);
			
		} finally {
			IOUtils.closeQuietly(fr);
			IOUtils.closeQuietly(is);
		}
	}
	
	public SqlUpgradeScript(String jarResourceName) throws IOException, UnsupportedOperationException {
		InputStream is = null;
		BufferedReader br = null;
		
		try {
			String filename = StringUtils.substring(jarResourceName, StringUtils.lastIndexOf(jarResourceName, "/")+1);
			Matcher matcher = PATTERN_JAR_FILENAME.matcher(filename);
			if(!matcher.matches()) throw new UnsupportedOperationException(MessageFormat.format("Bad resource name [{0}]", filename));
			
			this.resourceName = jarResourceName;
			this.fileName = matcher.group(1);
			this.fileVersion = new ServiceVersion(matcher.group(2));
			boolean rawContent = false;
			String fs = matcher.group(3);
			if (StringUtils.endsWith(fs, "$")) {
				rawContent = true;
				this.fileSequence = StringUtils.substringBeforeLast(fs, "$");
			} else {
				this.fileSequence = fs;
			}
			this.fileSequence = matcher.group(3);
			
			is = LangUtils.findClassLoader(getClass()).getResourceAsStream(jarResourceName);
			if(is == null) {
				is = getClass().getResourceAsStream(jarResourceName);
				if (is == null) throw new ResourceNotFoundException("Null InputStream!");
			}
			readFile(new InputStreamReader(is, "ISO-8859-15"), rawContent, true);
			//br = new BufferedReader(new InputStreamReader(is, "ISO-8859-15"));
			//readFile(br);
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(is);
		}
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public ServiceVersion getFileVersion() {
		return fileVersion;
	}
	
	public String getFileSequence() {
		return fileSequence;
	}
	
	public ArrayList<BaseScriptLine> getStatements() {
		return statements;
	}
	
	private void readFile(InputStreamReader readable, boolean rawContent, boolean flatNewLines) throws IOException {
		this.statements = new ArrayList<>();
		StringBuilder sb = null, sbsql = null;
		String lines[] = null;
		
		if (rawContent) {
			String line = null;
			sb = new StringBuilder();
			sbsql = new StringBuilder();
			BufferedReader br = new BufferedReader(readable);
			while ((line = br.readLine()) != null) {
				if (!StringUtils.isEmpty(line)) {
					if (AnnotationLine.matches(line)) {
						if (DataSourceAnnotationLine.matches(line)) {
							statements.add(new DataSourceAnnotationLine(line));
						} else {
							throw new IOException("Bad line: " + line);
						}
					} else if(CommentLine.matches(line)) {
						sb.append(line);
						sb.append("\n");
					} else {
						sbsql.append(line);
						sbsql.append(" ");
					}
				}
			}
			if(sb.length() > 0) statements.add(new CommentLine(StringUtils.removeEnd(sb.toString(), "\n")));
			if(sbsql.length() > 0) statements.add(new SqlLine(StringUtils.removeEnd(sbsql.toString(), "\n")));
		
		} else {
			Scanner s = new Scanner(readable);
			s.useDelimiter("(;( )?(\r)?\n)");
			//s.useDelimiter("(;( )?(\r)?\n)|(--\n)");
			while (s.hasNext()) {
				String block = s.next();
				block = StringUtils.replace(block, "\r", "");
				if(!StringUtils.isEmpty(block)) {
					// Remove remaining ; at the end of the block (only if this block is the last one)
					if(!s.hasNext() && StringUtils.endsWith(block, ";")) block = StringUtils.left(block, block.length()-1);

					sb = new StringBuilder();
					sbsql = new StringBuilder();
					lines = StringUtils.split(block, "\n");
					for(String line: lines) {
						if (AnnotationLine.matches(line)) {
							if (DataSourceAnnotationLine.matches(line)) {
								statements.add(new DataSourceAnnotationLine(line));
							} else if (IgnoreErrorsAnnotationLine.matches(line)) {
								statements.add(new IgnoreErrorsAnnotationLine(line));
							} else if (RequireAdminAnnotationLine.matches(line)) {
								statements.add(new RequireAdminAnnotationLine(line));
							} else {
								throw new IOException("Bad line: " + line);
							}
						} else if(CommentLine.matches(line)) {
							sb.append(line);
							sb.append("\n");
						} else {
							sbsql.append(StringUtils.trim(line));
							sbsql.append(" ");
							if(!flatNewLines) sbsql.append("\n");
						}
					}
					if(sb.length() > 0) statements.add(new CommentLine(StringUtils.removeEnd(sb.toString(), "\n")));
					if(sbsql.length() > 0) statements.add(new SqlLine(StringUtils.removeEnd(sbsql.toString(), "\n")));
				}
			}
		}
	}
}