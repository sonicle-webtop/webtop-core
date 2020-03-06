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

import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.util.ArrayList;
import java.util.Collection;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Universe;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
class PushConnection {
	private final MessageStorage messageStorage;
	private final String sessionId;
	private final UserProfileId profileId;
	private final String broadcasterPath;
	private final Object readyLock = new Object();
	private boolean hasBeenReady;
	
	public PushConnection(MessageStorage messageStorage, String sessionId, UserProfileId profileId) {
		this.messageStorage = messageStorage;
		this.sessionId = sessionId;
		this.profileId = profileId;
		this.broadcasterPath = PushEndpoint.URL + "/" + sessionId;
		this.hasBeenReady = false;
	}
	
	public void cleanup() {
		Broadcaster bc = getBroadcaster(Universe.broadcasterFactory());
		if (bc != null) bc.destroy();
	}
	
	public void ready() {
		synchronized (readyLock) {
			if (!hasBeenReady) {
				writeOnBroadcast(messageStorage.resumeMessages(profileId));
				hasBeenReady = true;
			}
		}
	}
	
	public boolean send(Collection<ServiceMessage> messages, boolean important) {
		return send(messages, important, false);
	}
	
	public boolean send(Collection<ServiceMessage> messages, boolean important, boolean skipStoring) {
		// We persist messages only if flag is enabled and connection has never 
		// been online yet. Messages will be cached in broadcaster cache until
		// the connection will be available.
		if (hasBeenReady || !important) {
			writeOnBroadcast(messages);
			return true;
		} else {
			synchronized (readyLock) {
				// Check hasBeenReady again, it may be changed before entering synchronized section
				if (hasBeenReady) {
					writeOnBroadcast(messages);
					return true;
				} else {
					if (!skipStoring) messageStorage.persistMessages(profileId, messages);
					return false;
				}
			}
		}
	}
	
	private void writeOnBroadcast(Collection<ServiceMessage> messages) {
		if (!messages.isEmpty()) {
			getBroadcaster(Universe.broadcasterFactory()).broadcast(preparePayload(messages));
		}
	}
	
	private String preparePayload(Collection<ServiceMessage> messages) {
		return StringUtils.replace(JsonResult.gson.toJson(messages), "|", "\\u007c");
	}
	
	private Broadcaster getBroadcaster(BroadcasterFactory factory) {
		return factory.lookup(broadcasterPath, true);
	}
	
	interface MessageStorage {
		public ArrayList<ServiceMessage> resumeMessages(UserProfileId profileId);
		public void persistMessages(UserProfileId profileId, Collection<ServiceMessage> messages);
	}
}
