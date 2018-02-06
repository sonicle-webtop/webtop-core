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

import com.sonicle.webtop.core.app.AbstractService;
import java.io.IOException;
import java.util.List;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class BaseAsyncEndpoint extends AbstractService implements AtmosphereHandler {
	
	abstract protected void onOpen(AtmosphereResource resource) throws IOException;
	abstract protected void onDisconnect(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException;
	abstract protected void onResume(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException;
	abstract protected void onTimeout(AtmosphereResourceEvent event, AtmosphereResponse response) throws IOException;
	abstract protected void onHeartbeat(AtmosphereResource resource) throws IOException;
	abstract protected void onMessage(AtmosphereResourceEvent event, AtmosphereResponse response, String message) throws IOException;

	@Override
	public void onRequest(AtmosphereResource resource) throws IOException {
		AtmosphereRequest request = resource.getRequest();
		if (request.getMethod().equalsIgnoreCase("GET")) {
			onOpen(resource);
			resource.suspend();
		} else if (request.getMethod().equalsIgnoreCase("POST")) {
			String line = request.getReader().readLine().trim();
			if (StringUtils.equals(line, "X")) {
				onHeartbeat(resource);
			} else {
				resource.getBroadcaster().broadcast(line);
			}
		}
	}

	@Override
	public void onStateChange(AtmosphereResourceEvent event) throws IOException {
		AtmosphereResponse response = ((AtmosphereResourceImpl) event.getResource()).getResponse(false);
		if (event.getMessage() != null && List.class.isAssignableFrom(event.getMessage().getClass())) {
			List<String> messages = List.class.cast(event.getMessage());
			for (String s : messages) {
				onMessage(event, response, s);
			}
		} else if (event.isClosedByApplication() || event.isClosedByClient() || event.isCancelled()) { 
			onDisconnect(event, response);
		} else if (event.isSuspended()) {
			onMessage(event, response, (String) event.getMessage());
		} else if (event.isResuming()) {
			onResume(event, response);
		} else if (event.isResumedOnTimeout()) {
			onTimeout(event, response);
		}
	}

	@Override
	public void destroy() {}
}
