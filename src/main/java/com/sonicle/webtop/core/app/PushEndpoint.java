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

import com.sonicle.webtop.core.app.atmosphere.UUIDBroadcasterCache;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.Heartbeat;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.PathParam;
import org.atmosphere.config.service.Post;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
@ManagedService(
		path = PushEndpoint.URL + "/{sessionId}",
		broadcasterCache = com.sonicle.webtop.core.app.atmosphere.UUIDBroadcasterCache.class,
		interceptors = {
			// See defaults at: https://github.com/Atmosphere/atmosphere/blob/atmosphere-project-2.4.20/modules/cpr/src/main/java/org/atmosphere/annotation/AnnotationUtil.java
			org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor.class, // Default for @ManagedService
			org.atmosphere.client.TrackMessageSizeInterceptor.class, // Default for @ManagedService
			org.atmosphere.interceptor.SuspendTrackerInterceptor.class, // Default for @ManagedService
			org.atmosphere.config.managed.ManagedServiceInterceptor.class, // Default for @ManagedService
			//org.atmosphere.interceptor.IdleResourceInterceptor.class,
			org.atmosphere.interceptor.HeartbeatInterceptor.class,
			org.atmosphere.interceptor.JavaScriptProtocol.class,
			org.atmosphere.interceptor.ShiroInterceptor.class,
			com.sonicle.webtop.core.app.atmosphere.ContentTypeInterceptor.class
		}
)
public class PushEndpoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(PushEndpoint.class);
	public static final String URL = "/push"; // This must reflect web.xml!
	
	@PathParam("sessionId")
	private String sessionId;
	
	@Ready
	public void onReady(final AtmosphereResource resource) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("onReady [{}, {}]", sessionId, resource.uuid());
		
		Broadcaster broadcaster = resource.getBroadcaster();
		if (broadcaster != null) {
			UUIDBroadcasterCache cache = (UUIDBroadcasterCache)broadcaster.getBroadcasterConfig().getBroadcasterCache();
			if (cache != null) cache.updateResourceReadyState(broadcaster.getID(), resource.uuid(), true);
		}
		
		String guessedSessionId = getSessionId(resource);
		if (StringUtils.equals(guessedSessionId, sessionId)) {
			invokeOnReady(sessionId, resource);
		} else {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Session mismatch, ignoring request! [{} != {}]", sessionId, guessedSessionId);
		}
	}
	
	@Disconnect
	public void onDisconnect(final AtmosphereResourceEvent event) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("onDisconnect [{}, {}]", sessionId, event.getResource().uuid());
		
		final AtmosphereResource resource = event.getResource();
		Broadcaster broadcaster = resource.getBroadcaster();
		if (broadcaster != null) {
			UUIDBroadcasterCache cache = (UUIDBroadcasterCache)broadcaster.getBroadcasterConfig().getBroadcasterCache();
			if (cache != null) cache.updateResourceReadyState(broadcaster.getID(), resource.uuid(), false);
		}
	}
	
	@Post
	public void onPost(final AtmosphereResource resource) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("onPost [{}, {}]", sessionId, resource.uuid());
		
		String guessedSessionId = getSessionId(resource);
		if (StringUtils.equals(guessedSessionId, sessionId)) {
			AtmosphereRequest request = resource.getRequest();
			try {
				String line = request.getReader().readLine().trim();
				if (StringUtils.equals(line, "X")) {
					invokeOnHeartbeat(sessionId, resource);
				}
			} catch(IOException ex) {
				LOGGER.error("Error reading", ex);
			}
		} else {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Session mismatch, ignoring request! [{} != {}]", sessionId, guessedSessionId);
		}
	}
	
	@Heartbeat
	public void onHeartbeat(final AtmosphereResourceEvent event) {
		if (LOGGER.isTraceEnabled()) LOGGER.trace("onHeartbeat [{}, {}]", sessionId, event.getResource().uuid());
		
		String guessedSessionId = getSessionId(event.getResource());
		if (StringUtils.equals(guessedSessionId, sessionId)) {
			invokeOnHeartbeat(sessionId, event.getResource());
		} else {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Session mismatch, ignoring request! [{} != {}]", sessionId, guessedSessionId);
		}
	}
	
	protected void invokeOnReady(String sessionId, AtmosphereResource resource) {
		SessionManager sessionManager = getSessionManager();
		if (sessionManager != null) {
			sessionManager.onPushResourceReady(sessionId, resource);
		} else {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("SessionManager is null");
		}
	}
	
	protected void invokeOnHeartbeat(String sessionId, AtmosphereResource resource) {
		SessionManager sessionManager = getSessionManager();
		if (sessionManager != null) {
			sessionManager.onPushResourceHeartbeat(sessionId, resource);
		} else {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("SessionManager is null");
		}
	}
	
	private String getSessionId(AtmosphereResource resource) {
		HttpSession session = resource.getRequest().getSession(false);
		return (session != null) ? session.getId() : null;
	}
	
	private SessionManager getSessionManager() {
		WebTopApp app = WebTopApp.getInstance();
		return (app != null) ? app.getSessionManager() : null;
	}
}
