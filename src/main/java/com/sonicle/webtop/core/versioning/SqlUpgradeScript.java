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

import com.sonicle.webtop.core.sdk.ServiceVersion;
import java.io.BufferedReader;
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
 * @author matteo
 */
public class SqlUpgradeScript {
	
	private static final Pattern PATTERN_FILENAME = Pattern.compile("^(([0-9]+(?:\\.[0-9]+){1,2})_([0-9]+))@(.+)\\.sql$");
	private static final Pattern PATTERN_LINE_COMMENT = Pattern.compile("^--(.)+$");
	
	private String resourceName = null;
	private String name = null;
	private ServiceVersion version = null;
	private String id = null;
	private String dataSource = null;
	private ArrayList<UpgradeLine> statements = null;
	private String sql = null;
	
	public static ServiceVersion extractVersion(String resourceName) {
		//String filename = StringUtils.substring(resourceName, StringUtils.lastIndexOf(resourceName, "/")+1);
		Matcher matcher = PATTERN_FILENAME.matcher(resourceName);
		if(!matcher.matches()) throw new UnsupportedOperationException(MessageFormat.format("Bad resource name [{0}]", resourceName));
		return new ServiceVersion(matcher.group(2));
	}
	
	public SqlUpgradeScript(Class clazz, String resourceName) throws IOException, UnsupportedOperationException {
		InputStream is = null;
		BufferedReader br = null;
		
		try {
			String filename = StringUtils.substring(resourceName, StringUtils.lastIndexOf(resourceName, "/")+1);
			Matcher matcher = PATTERN_FILENAME.matcher(filename);
			if(!matcher.matches()) throw new UnsupportedOperationException(MessageFormat.format("Bad resource name [{0}]", filename));
			
			this.resourceName = resourceName;
			this.name = matcher.group(1);
			this.version = new ServiceVersion(matcher.group(2));
			this.id = matcher.group(3);
			this.dataSource = matcher.group(4);
			
			is = clazz.getResourceAsStream(resourceName);
			if(is == null) throw new ResourceNotFoundException("Null InputStream!");
			readFile(new InputStreamReader(is, "ISO-8859-15"), true);
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
	
	public String getName() {
		return name;
	}
	
	public ServiceVersion getVersion() {
		return version;
	}
	
	public String getId() {
		return id;
	}
	
	public String getDataSource() {
		return dataSource;
	}
	
	public ArrayList<UpgradeLine> getStatements() {
		return statements;
	}
	
	/*
	public ArrayList<String> getStatements() {
		String[] semitokens = StringUtils.splitByWholeSeparator(this.sql, "; ");
		ArrayList<String> statements = new ArrayList<String>();
		String[] comtokens = null;
		for(String semitoken: semitokens) {
			if(!StringUtils.isEmpty(semitoken)) {
				comtokens = StringUtils.splitByWholeSeparator(semitoken, "/ ");
				statements.add((comtokens.length == 1) ? comtokens[0] : comtokens[1]);
				//statements.add(semitoken);
			}
		}
		return statements;
		//return StringUtils.splitByWholeSeparator(this.sql, "; ");
	}
	*/
	
	private void readFile(BufferedReader reader) throws IOException {
		String line = null;
		StringBuilder sb = new StringBuilder();
		while((line = reader.readLine()) != null) {
			line = StringUtils.trim(line);
			if(!StringUtils.isEmpty(line)) {
				sb.append(line);
				sb.append(" ");
			}
		}
		this.sql = sb.toString();
	}
	
	private void readFile(InputStreamReader readable, boolean flatNewLines) throws IOException {
		this.statements = new ArrayList<UpgradeLine>();
		String lines[] = null;
		StringBuilder sb = null, sbsql = null;
		
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
					if(AnnotationLine.matches(line)) {
						statements.add(new AnnotationLine(line));
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
				
				
				//if(AnnotationLine.matchesRequireAdmin(block)) {
				//	statements.add(new AnnotationLine(block));
				//} else {
				//	if(block.startsWith("/*!") && block.endsWith("*/")) {
				//		int i = block.indexOf(' ');
				//		block = block.substring(i + 1, block.length() - " */".length());
				//	}
				/*	
					lines = StringUtils.split(block, "\n");
					sb = new StringBuilder();
					sbsql = new StringBuilder();
					for(String line: lines) {
						if(CommentLine.matches(line)) {
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
				*/
			}
		}
	}
}