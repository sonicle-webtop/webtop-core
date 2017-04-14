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
import java.util.Collection;
import java.util.HashMap;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
		
@ServerEndpoint(value = "/"+WsPushEndpoint.URL, configurator = WsPushEndpointConfigurator.class)
public class WsPushEndpoint {
	public static final String URL = "push"; // This must reflect web.xml!
	private static final Logger logger = WT.getLogger(WsPushEndpoint.class);
	private static final MultiValueMap sessions = MultiValueMap.decorate(new HashMap<String, Session>());
	private WebTopApp wta;
	
	private String getSessionId(Session session) {
		return (String) session.getUserProperties().get("sessionId");
	}
	
	@OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
		wta = WebTopApp.getInstance();
		try {
			String sid = getSessionId(session);
			synchronized(sessions) {
				sessions.put(sid, session);
			}
			wta.getSessionManager().wsSessionOpened(sid, session);
			logger.trace("Connection opened [{}]", sid);
		} catch(Exception ex) {
			throw new IOException(ex);
		}
    }
	
	@OnClose
	public void onClose(Session session) throws IOException {
		try {
			String sid = getSessionId(session);
			synchronized(sessions) {
				sessions.remove(sid, session);
			}
			wta.getSessionManager().wsSessionClosed(sid);
			logger.trace("Connection closed [{}]", sid);
		} catch(Exception ex) {
			throw new IOException(ex);
		}
	}
	
	@OnMessage
	public void onMessage(String json) {
		// Communication is only mono-directional, from server -> client
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		logger.warn("Websocket Error", t);
	}
	
	public static boolean hasSessions(String sid) {
		synchronized(sessions) {
			return sessions.containsKey(sid);
		}
	}
	
	private static Collection<Session> getSessionsBySid(String sid) {
		synchronized(sessions) {
			if(sessions.containsKey(sid)) {
				return (Collection<Session>)sessions.get(sid);
			}
		}
		return null;
	}
	
	public static void send(String sid, String rawMessage) throws IOException {
		Collection<Session> sessionsBySid = getSessionsBySid(sid);
		if(sessionsBySid != null) {
			for(Session session : sessionsBySid) send(session, rawMessage);
		}
	}
	
	public static void send(Session session, String rawMessage) throws IOException {
		synchronized(session) {
			if(session.isOpen()) {
				if(!rawMessage.isEmpty() && !rawMessage.equals("[]")) {
					session.getBasicRemote().sendText(rawMessage);
				}
			} else {
				throw new IOException("Session is not available");
			}
		}
	}
}
