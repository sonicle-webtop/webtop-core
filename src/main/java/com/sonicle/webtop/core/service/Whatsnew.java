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

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.sdk.ServiceVersion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author matteo
 */
class Whatsnew {
	
	private final Pattern PATTER_LINE = Pattern.compile("^(\\$|%|@|\\*|!)(.*)");
	private Class clazz = null;
	private String resourceName = null;
	private ServiceVersion version = null;
	private String listMode = "none";
	private boolean listItem = false;
	private final LinkedHashMap<String, String> htmls = new LinkedHashMap<>();
	
	public Whatsnew(Class clazz, String resourceName) {
		this.clazz = clazz;
		this.resourceName = resourceName;
	}
	
	private void update(ServiceVersion fromVersion, ServiceVersion toVersion) throws IOException {
		InputStream is = null;
		BufferedReader br = null;
		
		if(fromVersion == null) fromVersion = new ServiceVersion();
		if((version != null) && (fromVersion.compareTo(version) >= 0)) return;
		try {
			is = clazz.getResourceAsStream(resourceName);
			if(is == null) throw new ResourceNotFoundException();
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-15"));
			readFile(br, fromVersion, toVersion);
			version = fromVersion;
			
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(is);
		}
	}

	private void readFile(BufferedReader reader, ServiceVersion fromVersion, ServiceVersion toVersion) throws IOException {
		Matcher matcher = null;
		String line = null, marker = null, text = null;
		ServiceVersion lineVersion = null;
		
		StringBuilder sb = null;
		boolean skip = false;
		int lineNo = 0;
		while((line = reader.readLine()) != null) {
			lineNo++;
			line = StringUtils.trim(line);
			matcher = PATTER_LINE.matcher(line);
			if(matcher.matches()) {
				marker = matcher.group(1);
				text = StringUtils.trim(matcher.group(2));
				text = LangUtils.escapeHtmlAccentsAndSymbols(text);
				
				if(marker.equals("$")) { // Version marker
					// Closes previous version's section
					if(sb != null) {
						this.closeList(sb);
						htmls.put(lineVersion.toString(), sb.toString());
						sb = null;
					}
					
					lineVersion = new ServiceVersion(text);
					if(lineVersion.getValue() == null) throw new IOException(MessageFormat.format("Bad version [{0}] at line {1}", text, lineNo));
					
					skip = htmls.containsKey(lineVersion.toString()); // Skip all versions already processed!
					if(skip) continue;
					if(lineVersion.compareTo(fromVersion) <= 0) break; // Skip all version sections below fromVersion (included)
					if(lineVersion.compareTo(toVersion) > 0) break; // Skip all version sections after toVersion
					sb = new StringBuilder(); // Prepares a new version section
					
				} else {
					if(skip) continue;
					if(sb == null) continue; // An active builder is required!
					if(marker.equals("%")) { // Version title
						this.closeList(sb);
						sb.append(MessageFormat.format("<div class='wntitle'>{0}</div>\n", text));

					} else if(marker.equals("@")) { // Version sub-title
						this.closeList(sb);
						sb.append(MessageFormat.format("<div class='wnsubtitle'>{0}</div>\n", text));

					} else if(marker.equals("!")) { // Free text
						this.closeList(sb);
						sb.append(MessageFormat.format("<div class='wnfreetext'>{0}</div>\n", text));

					} else if(marker.equals("*")) { // Unordered list
						this.closeListItem(sb);
						this.openList(sb, "unordered");
						this.openListItem(sb);
						sb.append(text);

					} else { // No special markers
						this.closeList(sb);
						sb.append(text);
						sb.append("\n");
					}
				}
			} else {
				if(skip) continue;
				if(sb == null) continue; // An active builder is required!
				sb.append(" ");
				sb.append(LangUtils.escapeHtmlAccentsAndSymbols(line));
			}
		}
		
		// Closes last version's section
		if(sb != null) {
			this.closeList(sb);
			htmls.put(lineVersion.toString(), sb.toString());
			sb = null;
		}
	}

	private void openList(StringBuilder sb, String mode) {
		if(!listMode.equals(mode)) {
			if(mode.equals("unordered")) {
				sb.append("<div class='wnlist'><ul>\n");
			}
			listMode = mode;
		}
	}

	private void closeList(StringBuilder sb) {
		if(listItem) {
			sb.append("</li>");
			listItem = false;
		}
		if(listMode.equals("unordered")) {
			sb.append("</ul></div>\n");
		}
		listMode = "none";
	}
	
	private void openListItem(StringBuilder sb) {
		listItem = true;
		sb.append("<li>\n");
	}
	
	private void closeListItem(StringBuilder sb) {
		if(listItem) {
			sb.append("</li>");
			listItem = false;
		}
	}
	
	public String toHtml(ServiceVersion fromVersion, ServiceVersion toVersion) throws IOException {
		update(fromVersion, toVersion);
		return buildHtml(fromVersion, toVersion);
	}
	
	private String buildHtml(ServiceVersion fromVersion, ServiceVersion toVersion) {
		if(fromVersion == null) fromVersion = new ServiceVersion();
		StringBuilder sb = new StringBuilder();
		ServiceVersion vers = null;
		for(String key: htmls.keySet()) {
			vers = new ServiceVersion(key);
			if(vers.compareTo(fromVersion) <= 0) break; // Skip all version sections below fromVersion (included)
			if(vers.compareTo(toVersion) > 0) break; // Skip all version sections after toVersion
			sb.append(htmls.get(key));
		}
		return sb.toString();
	}
}
