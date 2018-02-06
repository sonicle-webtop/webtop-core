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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.bol.OMessageQueue;
import com.sonicle.webtop.core.dal.MessageQueueDAO;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.atmosphere.cpr.AtmosphereResource;
import org.codehaus.plexus.util.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class SessionManagerShared {
	private static final Logger logger = WT.getLogger(SessionManagerShared.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized SessionManagerShared initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		SessionManagerShared sesm = new SessionManagerShared(wta);
		initialized = true;
		logger.info("SessionManager initialized");
		return sesm;
	}
	
	public static final String ATTRIBUTE_CONTEXT_NAME = "contextName";
	public static final String ATTRIBUTE_CSRF_TOKEN = "csrf";
	public static final String ATTRIBUTE_WEBTOP_CLIENTID = "clientId";
	public static final String ATTRIBUTE_CLIENT_IP = "clientIp";
	public static final String ATTRIBUTE_CLIENT_URL = "clientUrl";
	public static final String ATTRIBUTE_REFERER_URI = "refererUri";
	public static final String ATTRIBUTE_CLIENT_LOCALE = "clientLocale";
	public static final String ATTRIBUTE_CLIENT_USERAGENT = "clientUA";
	public static final String ATTRIBUTE_WEBTOP_SESSION = "wts";
	
	private WebTopApp wta = null;
	private final Object lock = new Object();
	private final LinkedHashMap<String, WebTopSession> onlineSessions = new LinkedHashMap<>();
	private final HashSet<String> onlineWebTopClientIds = new HashSet<>();
	private final HashMap<String, PushConnection> pushConnections = new HashMap<>();
	private final HashMap<String, Integer> wsPushSessions = new HashMap<>();
	private final HashMap<UserProfileId, ProfileSids> profileSidsCache = new HashMap<>();
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private SessionManagerShared(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	protected void cleanup() {
		// Internal structures should be empty during this method call.
		// Sessions listeners should have called session destroy!
		if(!onlineSessions.isEmpty() || !profileSidsCache.isEmpty()) logger.warn("Internal structures should be empty... Why is this not true?");
		onlineSessions.clear();
		wsPushSessions.clear();
		profileSidsCache.clear();
		wta = null;
		logger.info("SessionManager destroyed");
	}
	
	
	
	
	
	
	
	
	
	public void push(String sessionId, ServiceMessage message) {
		push(sessionId, Arrays.asList(message));
	}
	
	public void push(String sessionId, Collection<ServiceMessage> messages) {
		synchronized(lock) {
			if (onlineSessions.containsKey(sessionId)) {
				PushConnection pushCon = pushConnections.get(sessionId);
				if (pushCon != null) {
					pushCon.send(messages);
				} else {
					logger.error("PushConnection not available [{}]", sessionId);
				}
			}
		}
	}
	
	public void push(UserProfileId profileId, ServiceMessage message, boolean enqueueIfOffline) {
		push(profileId, Arrays.asList(message), enqueueIfOffline);
	}
	
	public void push(UserProfileId profileId, Collection<ServiceMessage> messages, boolean enqueueIfOffline) {
		synchronized(lock) {
			ProfileSids sessionIds = profileSidsCache.get(profileId);
			if ((sessionIds != null) && !sessionIds.isEmpty()) {
				for(String sessionId : profileSidsCache.get(profileId)) {
					push(sessionId, messages);
				}
			} else {
				if(enqueueIfOffline) {
					enqueueMessages(profileId, messages);
				}
			}
		}
	}
	
	void registerWebTopSession(WebTopSession webtopSession) throws WTException {
		String sessionId = webtopSession.getId();
		synchronized(lock) {
			if (!onlineSessions.containsKey(sessionId)) {
				internalRegisterSession(sessionId, webtopSession);
			} else {
				throw new WTException("Session [{0}] is already registered", sessionId);
			}
		}
	}
	
	public void onContainerSessionDestroyed(HttpSession session) {
		WebTopSession webtopSession = SessionContext.getWebTopSession(session);
		if (webtopSession != null) {
			synchronized(lock) {
				try {
					String sessionId = webtopSession.getId();
					String clientId = SessionContext.getWebTopClientID(session);
					UserProfileId profileId = webtopSession.getProfileId(); // Extract userProfile info before cleaning session!
					pushConnections.remove(sessionId);
					webtopSession.cleanup();

					if (profileId != null) {
						internalUnregisterSession(sessionId, clientId, profileId);
						wta.getLogManager().write(profileId, CoreManifest.ID, "LOGOUT", null, SessionContext.getClientIP(session), SessionContext.getClientUserAgent(session), sessionId, null);
					}
					logger.trace("WTS destroyed [{}]", sessionId);
				} catch(Throwable t) {
					logger.error("{}", t);
				}
			}
		}
	}
	
	public void onPushResourceHeartbeat(AtmosphereResource resource) {
		HttpSession session = resource.session(false);
		if (session != null) {
			ServletUtils.touchSession(session);
			logger.trace("Push connection heartbeat [{}]", session.getId());
		}
	}
	
	public void onPushResourceConnect(AtmosphereResource resource) {
		HttpSession session = resource.session(false);
		if (session != null) {
			String uuid = resource.uuid();
			String sessionId = session.getId();
			if (!StringUtils.equalsIgnoreCase(sessionId, uuid)) {
				logger.warn("Push uuid is not equal to sessionId [{} <> {}]", uuid, sessionId);
			}
			synchronized(lock) {
				WebTopSession webtopSession = onlineSessions.get(sessionId);
				if ((webtopSession != null) && !pushConnections.containsKey(sessionId)) {
					pushConnections.put(sessionId, new PushConnection(resource, listEnqueuedMessages(webtopSession.getProfileId())));
					logger.trace("Push connection established [{}]", sessionId);
				}
			}
		}
		///Subject subject = (Subject)resource.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
	}
	
	public void onPushResourceDisconnect(AtmosphereResource resource) {
		HttpSession session = resource.session(false);
		if (session != null) {
			String uuid = resource.uuid();
			String sessionId = session.getId();
			if (!StringUtils.equalsIgnoreCase(sessionId, uuid)) {
				logger.warn("Push uuid is not equal to sessionId [{} <> {}]", uuid, sessionId);
			}
			synchronized(lock) {
				WebTopSession webtopSession = onlineSessions.get(sessionId);
				if (webtopSession == null) {
					logger.debug("WebTopSession is null"); // Può capitareeeeeeeeeeeeeeeeeeeeeee
				}
				PushConnection pushCon = pushConnections.remove(sessionId);
				if (pushCon != null) {
					pushCon.close();
					logger.trace("Push connection closed [{}]", sessionId);
				}
			}
		}
	}
	
	private ArrayList<ServiceMessage> listEnqueuedMessages(UserProfileId profileId) {
		ArrayList<ServiceMessage> messages = new ArrayList<>();
		MessageQueueDAO mqDao = MessageQueueDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			// Mark user's messages as "under process" and gets them
			String pid = UUID.randomUUID().toString();
			mqDao.updatePidIfNullByDomainUser(con, profileId.getDomainId(), profileId.getUserId(), pid);
			List<OMessageQueue> queued = mqDao.selectByPid(con, pid);
			
			if (!queued.isEmpty()) {
				Class clazz = null;
				for(OMessageQueue message : queued) {
					try {
						clazz = Class.forName(message.getMessageType());
						messages.add((ServiceMessage)JsonResult.gson.fromJson(message.getMessageRaw(), clazz));
					} catch(Exception ex1) {
						logger.warn("Unable to unserialize message [{}] for [{}]", message.getMessageType(), profileId.toString(), ex1);
					}
				}
				mqDao.deleteByPid(con, pid);
			}
		} catch(Exception ex) {
			logger.error("Error adding offline messages", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return messages;
	}
	
	private void enqueueMessages(UserProfileId profileId, Collection<ServiceMessage> messages) {
		MessageQueueDAO mqDao = MessageQueueDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection(false);
			OMessageQueue queued = null;
			for(ServiceMessage message : messages) {
				queued = new OMessageQueue();
				queued.setQueueId(mqDao.getSequence(con).intValue());
				queued.setDomainId(profileId.getDomainId());
				queued.setUserId(profileId.getUserId());
				queued.setMessageType(message.getClass().getName());
				queued.setMessageRaw(JsonResult.gson.toJson(message));
				queued.setQueuedOn(DateTime.now(DateTimeZone.UTC));
				mqDao.insert(con, queued);
			}
			DbUtils.commitQuietly(con);
			
		} catch(Throwable t) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error enqueuing messages", t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void internalRegisterSession(String sessionId, WebTopSession webtopSession) throws WTException {
		String clientId = webtopSession.getRemoteIP();
		UserProfileId profileId = webtopSession.getProfileId();
		if (profileId == null) throw new WTException("Session [{0}] is not bound to a user", sessionId);
		onlineSessions.put(sessionId, webtopSession);
		onlineWebTopClientIds.add(profileId.toString() + "|" + clientId);
		if (profileSidsCache.get(profileId) == null) profileSidsCache.put(profileId, new ProfileSids());
		profileSidsCache.get(profileId).add(sessionId);
		logger.trace("Session registered [{}, {}]", sessionId, profileId);
	}
	
	private void internalUnregisterSession(String sessionId, String clientId, UserProfileId profileId) throws WTException {
		if (profileId == null) throw new WTException("Session [{0}] is not bound to a user", sessionId);
		if (profileSidsCache.get(profileId) != null) {
			profileSidsCache.get(profileId).remove(sessionId);
		}
		onlineWebTopClientIds.remove(profileId.toString() + "|" + clientId);
		onlineSessions.remove(sessionId);
		logger.trace("Session unregistered [{}, {}]", sessionId, profileId);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	/*
	void wsSessionOpened(String sessionId, javax.websocket.Session ws) throws WTException {
		registerWsPushSession(sessionId, ws);
	}
	
	void wsSessionClosed(String sessionId) throws WTException {
		try {
			unregisterWsPushSession(sessionId);
		} catch (Exception ex) {
			logger.error("Error unregistering push websocket session [{}]", sessionId, ex);
		}
	}
	
	private void stopShiroSession(Session session) throws Exception {
		logger.trace("Shiro session stop [{}]", session.getId());
		WebTopSession wts = WTSessionManager.getWebTopSession(session);
		if (wts != null) {
			String sid = session.getId().toString();
			UserProfileId pid = wts.getProfileId(); // Extract userProfile info before cleaning session!
			wts.cleanup();
			if(pid != null) {
				unregisterWebTopSession(session, pid);
				wta.getLogManager().write(pid, CoreManifest.ID, "LOGOUT", null, WTSessionManager.getClientIP(session), WTSessionManager.getClientUserAgent(session), sid, null);
			}
			logger.trace("WTS destroyed [{}]", sid);
		}
	}
	
	void registerWebTopSession(Session session, WebTopSession wts) throws WTException {
		synchronized(onlineSessions) {
			String sid = session.getId().toString();
			if(onlineSessions.containsKey(sid)) throw new WTException("Session [{0}] is already registered", sid);
			UserProfileId pid = wts.getUserProfile().getId();
			if(pid == null) throw new WTException("Session [{0}] is not bound to a user", sid);
			
			onlineSessions.put(sid, wts);
			String wtcid = WTSessionManager.getWebTopClientID(session);
			onlineWebTopClientIds.add(pid.toString() + "|" + wtcid);
			if(profileSidsCache.get(pid) == null) profileSidsCache.put(pid, new ProfileSids());
			profileSidsCache.get(pid).add(sid);
			logger.trace("Session registered [{}, {}]", sid, pid);
		}
	}
	
	private void unregisterWebTopSession(Session session, UserProfileId profileId) throws WTException {
		synchronized(onlineSessions) {
			String sid = session.getId().toString();
			if(onlineSessions.containsKey(sid)) {
				if(profileId != null) {
					if(profileSidsCache.get(profileId) != null) {
						profileSidsCache.get(profileId).remove(sid);
						if(profileSidsCache.get(profileId).isEmpty()) profileSidsCache.remove(profileId);
					}
					String wtcid = WTSessionManager.getWebTopClientID(session);
					onlineWebTopClientIds.remove(profileId.toString() + "|" + wtcid);
					onlineSessions.remove(sid);
				} else {
					logger.warn("Session [{}] is not bound to a user", sid);
				}
			}
		}
	}
	
	private void registerWsPushSession(String sessionId, javax.websocket.Session ws)  throws WTException {
		synchronized(onlineSessions) {
			if(!onlineSessions.containsKey(sessionId)) throw new WTException("Session [{0}] not found", sessionId);
			
			int count = 0;
			if(wsPushSessions.containsKey(sessionId)) count = wsPushSessions.get(sessionId);
			wsPushSessions.put(sessionId, count+1);
			if(count == 0) {
				WebTopSession wts = onlineSessions.get(sessionId);
				pushData(sessionId, wts.getEnqueuedMessages());
			}
			logger.trace("Push channel associated [{}, count:{}]", sessionId, count+1);
		}
	}
	
	private int countWsPushSessions(String sessionId) {
		synchronized(onlineSessions) {
			return wsPushSessions.containsKey(sessionId) ? wsPushSessions.get(sessionId) : 0;
		}
	}
	
	private void unregisterWsPushSession(String sessionId) throws WTException {
		synchronized(onlineSessions) {
			if(wsPushSessions.containsKey(sessionId)) {
				int count = wsPushSessions.get(sessionId);
				if(count == 1) {
					wsPushSessions.remove(sessionId);
				} else {
					wsPushSessions.put(sessionId, count-1);
				}
				logger.trace("Push channel disconnected [{}, count:{}]", sessionId, count-1);
			}
		}
	}
	
	public boolean pushData(String sessionId, Object data) {
		try {
			if(WsPushEndpoint.hasSessions(sessionId)) {
				WsPushEndpoint.send(sessionId, JsonResult.gson.toJson(data));
				return true;
			}
		} catch(IOException ex) {
			logger.error("Unable to send through push channel", ex);
		}
		return false;
	}
	*/
	
	public WebTopSession getWebTopSession(String sessionId) {
		synchronized(onlineSessions) {
			return onlineSessions.get(sessionId);
		}
	}
	
	public List<WebTopSession> getWebTopSessions(UserProfileId profileId) {
		List<WebTopSession> list = new ArrayList<>();
		synchronized(onlineSessions) {
			if(profileSidsCache.get(profileId) != null) {
				for(String sid : profileSidsCache.get(profileId)) {
					list.add(onlineSessions.get(sid));
				}
			}
		}
		return list;
	}
	
	public boolean isOnline(String sessionId) {
		return onlineSessions.containsKey(sessionId);
	}
	
	public boolean isOnline(UserProfileId profileId, String webtopClientId) {
		return onlineWebTopClientIds.contains(profileId.toString() + "|" + webtopClientId);
	}
	
	private static class ProfileSids extends HashSet<String> {
		public ProfileSids() {
			super();
		}
	}
}
