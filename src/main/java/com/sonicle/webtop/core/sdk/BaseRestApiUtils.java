/*
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.beans.SortInfo;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import java.text.ParseException;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class BaseRestApiUtils {
	public static final String DEFAULT_ETAG = "19700101000000000";
	public static final DateTimeFormatter ETAG_FMT = DateTimeUtils.createFormatter("yyyyMMddHHmmssSSS", DateTimeZone.UTC);
	public static final DateTimeFormatter ISO_LOCALDATE_FMT = DateTimeUtils.createFormatter("yyyy-MM-dd");
	public static final DateTimeFormatter ISO_LOCALTIME_FMT = DateTimeUtils.createFormatter("HH:mm:ss");
	public static final DateTimeFormatter ISO_DATEDIME_FMT = DateTimeUtils.createFormatter("yyyy-MM-dd'T'HH:mm:ss'Z'", DateTimeZone.UTC);
	
	public static boolean shouldSet(final Set<String> fields2set, final String name) {
		return fields2set == null || fields2set.contains(name);
	}
	
	public static Set<String> parseSet(final String s) {
		return (s == null) ? null : LangUtils.parseStringAsSet(s, ",", true);
	}
	
	public static Set<SortInfo> parseSortInfo(final String s) throws ParseException {
		return SortInfo.parseCollection(parseSet(s));
	}
	
	public static DateTime parseETag(final String etag) {
		return !StringUtils.isBlank(etag) ? ETAG_FMT.parseDateTime(etag) : null;
	}
	
	public static String parseUserId(final String s, final String ifBlank) {
		return StringUtils.isBlank(s) ? ifBlank : s;
	}
	
	public static UserProfileId parseProfileId(final String s, final UserProfileId ifBlank) throws WTParseException {
		if (StringUtils.isBlank(s)) return ifBlank;
		UserProfileId parsed = UserProfileId.parseQuielty(s);
		if (parsed == null) throw new WTParseException("Unable to parse '{}' as ProfileId", s);
		return parsed;
	}
	
	public static String buildETag(final DateTime revisionTimestamp) {
		if (revisionTimestamp != null) {
			return ETAG_FMT.print(revisionTimestamp);
		} else {
			return DEFAULT_ETAG;
		}
	}
	
	public static Integer pageSizeOrDefault(final Integer pageNo, final Integer pageSize) {
		return (pageNo != null && pageSize == null) ? 50 : pageSize;
	}
	
	public static Short toShort(Integer value) {
		return value != null ? value.shortValue() : null;
	}
	
	public static Integer toInteger(Short value) {
		return value != null ? value.intValue() : null;
	}
}
