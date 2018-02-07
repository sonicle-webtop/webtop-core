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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class SessionManager {
	private static final Logger logger = WT.getLogger(SessionManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized SessionManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		SessionManager sesm = new SessionManager(wta);
		initialized = true;
		logger.info("SessionManager initialized");
		return sesm;
	}
	
	public static final String ATTRIBUTE_CONTEXT_NAME = "webtop.ContextName";
	public static final String ATTRIBUTE_CSRF_TOKEN = "webtop.CSRF";
	public static final String ATTRIBUTE_WEBTOP_CLIENTID = "webtop.clientId";
	public static final String ATTRIBUTE_CLIENT_IP = "webtop.clientIp";
	public static final String ATTRIBUTE_CLIENT_URL = "webtop.clientUrl";
	public static final String ATTRIBUTE_REFERER_URI = "webtop.refererUri";
	public static final String ATTRIBUTE_CLIENT_LOCALE = "webtop.clientLocale";
	public static final String ATTRIBUTE_CLIENT_USERAGENT = "webtop.clientUA";
	public static final String ATTRIBUTE_WEBTOP_SESSION = "webtop.Session";
	public static final String ATTRIBUTE_GUESSING_LOCALE = "Locale";
	public static final String ATTRIBUTE_GUESSING_USERNAME = "UserName";
	
	private WebTopApp wta = null;
	private final Object lock = new Object();
	private final LinkedHashMap<String, WebTopSession> onlineSessions = new LinkedHashMap<>();
	private final HashSet<String> onlineClienTrackingIds = new HashSet<>();
	private final HashMap<UserProfileId, ProfileSids> profileSidsCache = new HashMap<>();
	private final HashMap<String, String> uuidToSessionId = new HashMap<>();
	private final HashMap<String, PushConnectionList> pushConnections = new HashMap<>();
	private static class PushConnectionList extends LinkedHashMap<String, PushConnection> {}
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private SessionManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	protected void cleanup() {
		onlineSessions.clear();
		onlineClienTrackingIds.clear();
		profileSidsCache.clear();
		uuidToSessionId.clear();
		pushConnections.clear();
		wta = null;
		logger.info("SessionManager destroyed");
	}
	
	public void push(String sessionId, ServiceMessage message) {
		push(sessionId, Arrays.asList(message));
	}
	
	public boolean push(String sessionId, Collection<ServiceMessage> messages) {
		synchronized(lock) {
			if (onlineSessions.containsKey(sessionId)) {
				PushConnectionList pushCons = pushConnections.get(sessionId);
				if (pushCons != null) {
					for(PushConnection pushCon : pushCons.values()) {
						pushCon.send(messages);
					}
					return true;
				} else {
					logger.error("PushConnection not available [{}]", sessionId);
				}
			}
			return false;
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
				if (enqueueIfOffline) {
					enqueueMessages(profileId, messages);
				}
			}
		}
	}
	
	void registerWebTopSession(WebTopSession webtopSession) throws WTException {
		String sessionId = webtopSession.getId();
		synchronized(lock) {
			if (onlineSessions.containsKey(sessionId)) throw new WTException("Session [{0}] is already registered", sessionId);
			
			UserProfileId profileId = webtopSession.getProfileId();
			if (profileId == null) throw new WTException("Session [{0}] is not bound to a user", sessionId);

			onlineSessions.put(sessionId, webtopSession);
			pushConnections.put(sessionId, new PushConnectionList());
			if (profileSidsCache.get(profileId) == null) profileSidsCache.put(profileId, new ProfileSids());
			profileSidsCache.get(profileId).add(sessionId);
			onlineClienTrackingIds.add(profileId.toString() + "|" + webtopSession.getClientTrackingID());
			
			logger.trace("Session registered [{}, {}]", sessionId, webtopSession.getProfileId());
		}
	}
	
	public void onContainerSessionDestroyed(HttpSession session) {
		WebTopSession webtopSession = SessionContext.getWebTopSession(session);
		if (webtopSession != null) {
			synchronized(lock) {
				try {
					String sessionId = webtopSession.getId();
					String clientTrackingId = webtopSession.getClientTrackingID();
					UserProfileId profileId = webtopSession.getProfileId(); // Extract userProfile info before cleaning session!
					
					onlineSessions.remove(sessionId);
					pushConnections.remove(sessionId);
					if (profileId != null) {
						profileSidsCache.get(profileId).remove(sessionId);
						onlineClienTrackingIds.remove(profileId.toString() + "|" + clientTrackingId);
					}
					
					webtopSession.cleanup();
					if (profileId != null) {
						wta.getLogManager().write(profileId, CoreManifest.ID, "LOGOUT", null, SessionContext.getClientIP(session), SessionContext.getClientUserAgent(session), sessionId, null);
					}
					
					logger.trace("Session unregistered [{}, {}]", sessionId, profileId);
					
				} catch(Throwable t) {
					logger.error("Error destroying session", t);
				}
			}
		}
	}
	
	public void onPushResourceConnect(AtmosphereResource resource) {
		HttpSession session = resource.session(false);
		if (session != null) {
			String sessionId = session.getId();
			String uuid = resource.uuid();
			
			synchronized(lock) {
				WebTopSession webtopSession = onlineSessions.get(sessionId);
				String oldSessionId = uuidToSessionId.put(uuid, sessionId);
				if (oldSessionId != null) logger.warn("uuid mapped with multiple sessions [{} -> {}, {}]", uuid, oldSessionId, sessionId);
				PushConnection pushCon = new PushConnection(resource, listEnqueuedMessages(webtopSession.getProfileId()));
				pushConnections.get(sessionId).put(uuid, pushCon);
				pushCon.flush();
			}
			logger.trace("Push connection added [{}@{}]", uuid, sessionId);
		}
		///Subject subject = (Subject)resource.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
	}
	
	public void onPushResourceDisconnect(AtmosphereResource resource) {
		HttpSession session = resource.session(false);
		if (session != null) {
			String sessionId = session.getId();
			String uuid = resource.uuid();
			
			synchronized(lock) {
				WebTopSession webtopSession = onlineSessions.get(sessionId);
				if (webtopSession == null) {
					logger.debug("WebTopSession is null"); // Può capitareeeeeeeeeeeeeeeeeeeeeee...ma quando?
				}
				uuidToSessionId.remove(uuid);
				PushConnection pushCon = pushConnections.get(sessionId).remove(uuid);
				if (pushCon != null) {
					pushCon.close();
					logger.trace("Push link closed [{}]", uuid);
				}
			}
			logger.trace("Push connection removed [{}@{}]", uuid, sessionId);
		}
	}
	
	public void onPushResourceHeartbeat(AtmosphereResource resource) {
		HttpSession session = resource.session(false);
		if (session != null) {
			ServletUtils.touchSession(session);
			logger.trace("Push connection heartbeat [{}]", session.getId());
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
	
	public WebTopSession getWebTopSession(String sessionId) {
		return onlineSessions.get(sessionId);
	}
	
	public List<WebTopSession> getWebTopSessions(UserProfileId profileId) {
		List<WebTopSession> list = new ArrayList<>();
		synchronized(lock) {
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
		return onlineClienTrackingIds.contains(profileId.toString() + "|" + webtopClientId);
	}
	
	private static class ProfileSids extends HashSet<String> {
		public ProfileSids() {
			super();
		}
	}
}
