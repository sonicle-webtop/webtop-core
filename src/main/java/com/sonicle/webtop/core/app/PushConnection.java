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
import java.util.ArrayList;
import java.util.Collection;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Universe;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class PushConnection {
	private final static Logger logger = WT.getLogger(PushConnection.class);
	private final String sessionId;
	private final String broadcasterPath;
	
	public PushConnection(String sessionId, Collection<ServiceMessage> initialMessages) {
		this.sessionId = sessionId;
		this.broadcasterPath = PushEndpoint.URL + "/" + sessionId;
		if (!initialMessages.isEmpty()) {
			writeOnBroadcast(initialMessages);
		}
	}
	
	public boolean flush() {
		return send(new ArrayList<>(0));
	}
	
	public boolean send(Collection<ServiceMessage> messages) {
		return writeOnBroadcast(messages);
	}
	
	private boolean writeOnBroadcast(Collection<ServiceMessage> messages) {
		BroadcasterFactory factory = Universe.broadcasterFactory();
		if (!messages.isEmpty()) {
			getBroadcaster(factory).broadcast(preparePayload(messages));
		}
		return true;
	}
	
	private String preparePayload(Collection<ServiceMessage> messages) {
		return StringUtils.replace(JsonResult.gson.toJson(messages), "|", "\\u007c");
	}
	
	private Broadcaster getBroadcaster(BroadcasterFactory factory) {
		return factory.lookup(broadcasterPath, true);
	}
}
