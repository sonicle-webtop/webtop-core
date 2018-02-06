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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereSession;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class PushConnection {
	private final static Logger logger = WT.getLogger(PushConnection.class);
	private final AtmosphereSession session;
	private final ArrayList<ServiceMessage> initialMessages;
	
	public PushConnection(AtmosphereResource resource, Collection<ServiceMessage> initialMessages) {
		this.session = new AtmosphereSession(resource);
		this.initialMessages = new ArrayList<>(initialMessages);
	}
	
	public void close() {
		AtmosphereResource resource = null;
		try {
			resource = session.tryAcquire(1);
		} catch(InterruptedException ex) {}
		if (resource == null) return;
		
		String uuid = resource.uuid();
		try {
			resource.close();
		} catch(IOException ex) {
			logger.error("Error closing atmosphere connection [{}]", ex, uuid);
		}
	}
	
	public void flush() {
		send(new ArrayList<ServiceMessage>(0));
	}
	
	public void send(Collection<ServiceMessage> messages) {
		writeOnResource(messages);
	}
	
	private void writeOnResource(Collection<ServiceMessage> messages) {
		AtmosphereResource resource = null;
		try {
			resource = session.tryAcquire(5);
		} catch(InterruptedException ex) {}
		if (resource == null) return;
		
		if (!initialMessages.isEmpty()) {
			resource.write(JsonResult.gson.toJson(initialMessages));
		}
		if (!messages.isEmpty()) {
			resource.write(JsonResult.gson.toJson(messages));
		}
	}
}
