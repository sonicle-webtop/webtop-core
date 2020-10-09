/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import ch.qos.logback.core.PropertyDefinerBase;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.helpers.MessageFormatter;

/**
 *
 * @author malbinola
 */
public class LogbackPropertyDefiner extends PropertyDefinerBase {
	private static final Properties properties = new Properties();
	public static final String PROP_LOG_TARGET = "log.target";
	public static final String PROP_LOG_DIR = "log.dir";
	public static final String PROP_LOG_FILE_BASENAME = "log.file.basename";
	public static final String PROP_LOG_FILE_POLICY = "log.file.policy";
	public static final String PROP_OVERRIDE_DIR = "override.dir";
	public static final String OVERRIDE_FILENAME = "logback-override.xml";
	
	public static void setPropertyValue(boolean doNotSetBlankValues, String key, String value) {
		if (!doNotSetBlankValues || !StringUtils.isBlank(value)) {
			properties.setProperty(key, value);
		}
	}
	
	public static String getPropertyValue(String key) {
		return properties.getProperty(key);
	}
	
	private String key;
	private String defaultValue;
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getPropertyValue() {
		if (key != null) {
			return properties.getProperty(key, defaultValue);
		} else {
			printToSystemOut("ERROR Missing <key> element, no property to lookup.");
			return null;
		}
	}
	
	private static void printToSystemOut(String message, Object... arguments) {
		System.out.println(MessageFormatter.arrayFormat(message, arguments).getMessage());
	}
}
