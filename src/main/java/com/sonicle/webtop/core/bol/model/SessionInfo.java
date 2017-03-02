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
package com.sonicle.webtop.core.bol.model;

import com.sonicle.webtop.core.sdk.UserProfileId;
import org.apache.shiro.session.Session;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 *
 * @author malbinola
 */
public class SessionInfo {
	public String sessionId;
	public Integer timeout;
	public DateTime creationTime;
	public DateTime lastAccessTime;
	public Integer usedTime;
	public Integer ttl;
	public String profileId;
	public int pushSessionsCount;
	
	public SessionInfo() {}
	
	public SessionInfo(DateTime now, Session session, UserProfileId profileId, int pushSessionsCount) {
		this.sessionId = session.getId().toString();
		this.timeout = (session.getTimeout() < 0) ? -1 : (int)session.getTimeout()/1000;
		this.creationTime = new DateTime(session.getStartTimestamp());
		this.lastAccessTime = new DateTime(session.getLastAccessTime());
		this.usedTime = Math.abs(Seconds.secondsBetween(creationTime, now).getSeconds());
		this.ttl = (timeout < 0) ? -1 : timeout - Math.abs(Seconds.secondsBetween(lastAccessTime, now).getSeconds());
		this.profileId = profileId.toString();
		this.pushSessionsCount = pushSessionsCount;
	}
}
