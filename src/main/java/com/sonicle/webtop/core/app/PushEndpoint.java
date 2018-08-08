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

import java.io.IOException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.PathParam;
import org.atmosphere.config.service.Post;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.interceptor.IdleResourceInterceptor;
import org.atmosphere.interceptor.JavaScriptProtocol;
import org.atmosphere.interceptor.ShiroInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
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
			com.sonicle.webtop.core.app.atmosphere.ContentTypeInterceptor.class,
			AtmosphereResourceLifecycleInterceptor.class,
			TrackMessageSizeInterceptor.class,
			IdleResourceInterceptor.class,
			SuspendTrackerInterceptor.class,
			JavaScriptProtocol.class,
			HeartbeatInterceptor.class,
			ShiroInterceptor.class
		}
)
public class PushEndpoint {
	private static final Logger logger = LoggerFactory.getLogger(PushEndpoint.class);
	public static final String URL = "/push"; // This must reflect web.xml!
	
	@PathParam("sessionId")
	private String sessionId;
	
	@Ready
	public void onReady(AtmosphereResource resource) {
		if (logger.isTraceEnabled()) logger.trace("onReady [{}, {}]", sessionId, resource.uuid());
	}
	
	@Disconnect
	public void onDisconnect(AtmosphereResourceEvent event) {
		if (logger.isTraceEnabled()) logger.trace("onDisconnect [{}, {}]", sessionId, event.getResource().uuid());
	}
	
	@Post
	public void onPost(AtmosphereResource resource) {
		if (logger.isTraceEnabled()) logger.trace("onPost [{}, {}]", sessionId, resource.uuid());
		
		if (!isSessionValid(resource)) {
			logger.warn("No session available for push channel. Ignoring request! [{}]", sessionId);
			return;
		}
		
		AtmosphereRequest request = resource.getRequest();
		try {
			String line = request.getReader().readLine().trim();
			if (StringUtils.equals(line, "X")) {
				invokeOnHeartbeat(sessionId, resource);
			}
		} catch(IOException ex) {
			logger.error("Error reading", ex);
		}
	}
	
	protected void invokeOnHeartbeat(String sessionId, AtmosphereResource resource) throws IOException {
		SessionManager sessionManager = getSessionManager();
		if (sessionManager != null) {
			sessionManager.onPushResourceHeartbeat(sessionId, resource);
		} else {
			logger.error("SessionManager is null");
		}
	}
	
	private boolean isSessionValid(AtmosphereResource resource) {
		HttpSession session = resource.session(false);
		return (session == null) ? false : StringUtils.equals(session.getId(), sessionId);
	}
	
	private SessionManager getSessionManager() {
		WebTopApp app = WebTopApp.getInstance();
		if (app != null) return app.getSessionManager();
		logger.warn("WebTopApp is null");
		return null;
	}
}
