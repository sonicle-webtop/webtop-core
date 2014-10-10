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
package com.sonicle.webtop.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author matteo
 */
public class SqlScript {
	
	private ArrayList<String> statements = null;
	
	public SqlScript(Class clazz, String resourceName) throws IOException, UnsupportedOperationException {
		InputStream is = null;
		
		try {
			is = clazz.getResourceAsStream(resourceName);
			if(is == null) throw new ResourceNotFoundException("Null InputStream!");
			readFile(new InputStreamReader(is, "ISO-8859-15"));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	public ArrayList<String> getStatements() {
		return statements;
	}
	
	private void readFile(InputStreamReader readable) throws IOException {
		this.statements = new ArrayList<String>();
		String lines[] = null;
		StringBuilder sbsql = null;
		
		Scanner s = new Scanner(readable);
		s.useDelimiter("(;( )?(\r)?\n)");
		while (s.hasNext()) {
			String block = s.next();
			block = StringUtils.replace(block, "\r", "");
			if(!StringUtils.isEmpty(block)) {
				// Remove remaining ; at the end of the block (only if this block is the last one)
				if(!s.hasNext() && StringUtils.endsWith(block, ";")) block = StringUtils.left(block, block.length()-1);
				
				sbsql = new StringBuilder();
				lines = StringUtils.split(block, "\n");
				for(String line: lines) {
					if(CommentLine.matches(line)) continue;	
					sbsql.append(StringUtils.trim(line));
					sbsql.append(" ");
				}
				if(sbsql.length() > 0) statements.add(sbsql.toString());
			}
		}
	}
}
