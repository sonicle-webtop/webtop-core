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
package com.sonicle.webtop.core.util;

import com.sonicle.commons.shell.Shell;
import com.sonicle.webtop.core.app.WT;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class ZPushManager {
	private static final Logger logger = WT.getLogger(ZPushManager.class);
	private final String phpPath;
	private final String zpushPath;
	private final URI uri;
	
	public ZPushManager(String phpPath, String zpushPath, URI uri) {
		this.phpPath = phpPath;
		this.zpushPath = zpushPath;
		this.uri = uri;
	}
	
	public List<ListRecord> list() throws Exception {
		Shell shell = null;
		List<String> lines = null;
		
		try {
			String cmd = "list";
			shell = new Shell(uri);
			lines = runAdminCommand(shell, cmd);
			return parseListOutput(lines);
			
		} finally {
			if (shell != null) shell.close();
		}
	}
	
	public List<LastsyncRecord> listDevices() throws Exception {
		Shell shell = null;
		List<String> lines = null;
		
		try {
			String cmd = "lastsync";
			shell = new Shell(uri);
			lines = runAdminCommand(shell, cmd);
			return parseLastsyncOutput(lines);
			
		} finally {
			if (shell != null) shell.close();
		}
	}
	
	public String getDetailedInfo(String user, String lineSep) throws Exception {
		Shell shell = null;
		List<String> lines = null;
		
		try {
			String cmd = MessageFormat.format("list -u {0}", user);
			shell = new Shell(uri);
			lines = runAdminCommand(shell, cmd);
			return parseListDevicesOfUserOutput(lines, lineSep);
			
		} finally {
			if (shell != null) shell.close();
		}
	}
	
	public String getDetailedInfo(String device, String user, String lineSep) throws Exception {
		Shell shell = null;
		List<String> lines = null;
		
		try {
			String cmd = MessageFormat.format("list -d {0} -u {1}", device, user);
			shell = new Shell(uri);
			lines = runAdminCommand(shell, cmd);
			return parseListDevicesOfUserOutput(lines, lineSep);
			
		} finally {
			if (shell != null) shell.close();
		}
	}
	
	public void removeUser(String user) throws Exception {
		Shell shell = null;
		List<String> lines = null;
		
		try {
			String cmd = MessageFormat.format("remove -u {0}", user);
			shell = new Shell(uri);
			lines = runAdminCommand(shell, cmd);
			
		} finally {
			if (shell != null) shell.close();
		}
	}
	
	public void removeDevice(String device) throws Exception {
		Shell shell = null;
		List<String> lines = null;
		
		try {
			String cmd = MessageFormat.format("remove -d {0}", device);
			shell = new Shell(uri);
			lines = runAdminCommand(shell, cmd);
			
		} finally {
			if (shell != null) shell.close();
		}
	}
	
	public void removeUserDevice(String user, String device) throws Exception {
		Shell shell = null;
		List<String> lines = null;
		
		try {
			String cmd = MessageFormat.format("remove -u {0} -d {1}", user, device);
			shell = new Shell(uri);
			lines = runAdminCommand(shell, cmd);
			
		} finally {
			if (shell != null) shell.close();
		}
	}
	
	private List<String> runAdminCommand(Shell shell, String cmd) throws Exception {
		String shellCmd = buildShellCommand(cmd);
		logger.debug("Executing command [{}]", shellCmd);
		return shell.execute(shellCmd);
	}
	
	private String buildShellCommand(String zpushCmd) {
		return MessageFormat.format("{0}php {1}z-push-admin.php -a {2}", phpPath, zpushPath, zpushCmd);
	}
	
	private List<ListRecord> parseListOutput(List<String> lines) {
		ArrayList<ListRecord> items = new ArrayList<>();
		
		int lineNo = 0, dataLine = -1;
		for (String line : lines) {
			lineNo++;
			if (StringUtils.containsIgnoreCase(line, "All synchronized devices")) {
				dataLine = lineNo +4;
			}
			if ((dataLine != -1) && (lineNo >= dataLine) && !StringUtils.isBlank(StringUtils.trim(line))) {
				String[] tokens = StringUtils.split(line, " ", 2);
				String device = StringUtils.trim(tokens[0]);
				String users = StringUtils.trim(tokens[1]);
				items.add(new ListRecord(device, StringUtils.split(users, ",")));
			}
		}
		return items;
	}
	
	private String parseListDevicesOfUserOutput(List<String> lines, String lineSeparator) {
		StringBuilder sb = new StringBuilder();
		int lineNo = 0, dataLine = -1;
		for (String line : lines) {
			lineNo++;
			if (StringUtils.containsIgnoreCase(line, "Synchronized by user")) {
				dataLine = lineNo;
			}
			if ((dataLine != -1) && (lineNo >= dataLine)) {
				sb.append(line);
				sb.append(lineSeparator);
			}	
		}
		return sb.toString();
	}
	
	private List<LastsyncRecord> parseLastsyncOutput(List<String> lines) {
		ArrayList<LastsyncRecord> items = new ArrayList<>();
		
		int lineNo = 0, dataLine = -1;
		for (String line : lines) {
			lineNo++;
			if (StringUtils.containsIgnoreCase(line, "Device id")
					&& StringUtils.containsIgnoreCase(line, "Synchronized user")
					&& StringUtils.containsIgnoreCase(line, "Last sync time")) {
				dataLine = lineNo +2;
			}
			if ((dataLine != -1) && (lineNo >= dataLine) && !StringUtils.isBlank(StringUtils.trim(line))) {
				String[] tokens = StringUtils.split(line, " ", 3);
				String device = StringUtils.trim(tokens[0]);
				String user = StringUtils.trim(tokens[1]);
				String lastSync = StringUtils.trim(StringUtils.left(tokens[2], 16));
				items.add(new LastsyncRecord(device, user, "never".equalsIgnoreCase(lastSync) ? null : lastSync));
			}
		}
		return items;
	}
	
	public static class ListRecord {
		public final String device;
		public final String[] synchronizedUsers;
		
		public ListRecord(String device, String[] synchronizedUsers) {
			this.device = device;
			this.synchronizedUsers = synchronizedUsers;
		}
	}
	
	public static class LastsyncRecord {
		public final String device;
		public final String synchronizedUser;
		public final String lastSyncTime;
		
		public LastsyncRecord(String device, String synchronizedUser, String lastSyncTime) {
			this.device = device;
			this.synchronizedUser = synchronizedUser;
			this.lastSyncTime = lastSyncTime;
		}
	}
}
