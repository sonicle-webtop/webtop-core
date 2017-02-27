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
package com.sonicle.webtop.core.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class OSInfo {
	
	public static String build() {
		String host = getCmdOutput("uname -n");
		String domainName = StringUtils.defaultString(getCmdOutput("domainname"));
		String osName = getCmdOutput("uname -s");
		if(StringUtils.isEmpty(osName)) osName = System.getProperty("os.name");
		String osRelease = getCmdOutput("uname -r");
		if(StringUtils.isEmpty(osRelease)) osRelease = System.getProperty("os.version");
		String osVersion = StringUtils.defaultString(getCmdOutput("uname -v"));
		String osArch = getCmdOutput("uname -m");
		if(StringUtils.isEmpty(osArch)) osArch = System.getProperty("os.arch");
		
		// Builds string
		StringBuilder sb = new StringBuilder();
		if(new File("/sonicle/etc/xstream.conf").exists()) {
			sb.append("Sonicle XStream Server");
			sb.append(" - ");
		}
		sb.append(host);
		if(!StringUtils.isEmpty(domainName)) {
			sb.append(" at ");
			sb.append(domainName);
		}
		sb.append(" - ");
		sb.append(osName);
		sb.append(" ");
		sb.append(osRelease);
		sb.append(" ");
		sb.append(osVersion);
		sb.append(" ");
		sb.append(osArch);
		return sb.toString();
	}
	
	public static String getCmdOutput(String command) {
		String output = null;
		try {
			Process pro = Runtime.getRuntime().exec(command);
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			output = br.readLine();
			pro.waitFor();
		} catch (Throwable th) { /* Do nothing! */ }
		return output;
	}
}
