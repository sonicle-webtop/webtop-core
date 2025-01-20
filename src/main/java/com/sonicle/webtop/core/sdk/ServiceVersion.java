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
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.RegexUtils;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author malbinola
 */
public class ServiceVersion implements Comparable<ServiceVersion> {
	//private static final Pattern PATTERN_VERSION = Pattern.compile("^[0-9]+(\\.[0-9]+){1,2}$");
	private static final Pattern PATTERN_VERSION = Pattern.compile("^" + RegexUtils.MATCH_SW_VERSION + "$");
	private String version = null;
	
	public ServiceVersion() {}
	
	public ServiceVersion(String version) {
		if(StringUtils.isEmpty(version)) return;
		if(PATTERN_VERSION.matcher(version).matches()) {
			this.version = version;
		}
	}
	
	public String getValue() {
		return version;
	}
	
	public boolean isUndefined() {
		return (version == null);
	}

	@Override
	public int compareTo(ServiceVersion o) {
		if(o == null) return 1;
		if((o.getValue() == null) && (this.getValue() == null)) return 0;
		if(o.getValue() == null) return 1;
		if(this.getValue() == null) return -1;
		
		String[] thisTokens = StringUtils.split(this.getValue(), ".");
		String[] thatTokens = StringUtils.split(o.getValue(), ".");
		
		int length = Math.max(thisTokens.length, thatTokens.length);
		for(int i = 0; i < length; i++) {
			int thisToken = i < thisTokens.length ? Integer.parseInt(thisTokens[i]) : 0;
			int thatToken = i < thatTokens.length ? Integer.parseInt(thatTokens[i]) : 0;
			if(thisToken < thatToken) return -1;
			if(thisToken > thatToken) return 1;
		}
		return 0;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		return builder.append(this.version)
			.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(this.getClass() != obj.getClass()) return false;
		return this.compareTo((ServiceVersion) obj) == 0;
	}
	
	@Override
	public String toString() {
		return (!isUndefined()) ? this.version : "X.X.X";
	}
	
	public String getMajor() {
		if(isUndefined()) return null;
		String[] tokens = StringUtils.split(this.version, ".");
		return tokens[0];
	}
	
	public String getMinor() {
		if(isUndefined()) return null;
		String[] tokens = StringUtils.split(this.version, ".");
		return tokens[1];
	}
	
	public String getMaintainance() {
		if(isUndefined()) return null;
		String[] tokens = StringUtils.split(this.version, ".");
		return (tokens.length == 3) ? tokens[2] : "0";
	}
}
