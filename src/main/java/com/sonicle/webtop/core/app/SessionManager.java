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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import javax.servlet.http.HttpSession;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
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
		if (initialized) throw new RuntimeException("Initialization already done");
		SessionManager sesm = new SessionManager(wta);
		initialized = true;
		logger.info("Initialized");
		return sesm;
	}
	
	public static final String ATTRIBUTE_REQUEST_DUMPED = "webtop.REQUEST_DUMPED";
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
	private final StampedLock lock = new StampedLock();
	private final LinkedHashMap<String, WebTopSession> onlineSessions = new LinkedHashMap<>();
	private final HashSet<String> onlineClienTrackingIds = new HashSet<>();
	private final HashMap<UserProfileId, ProfileSids> profileSidsCache = new HashMap<>();
	private final HashMap<String, String> uuidToSessionId = new HashMap<>();
	private final HashMap<String, PushConnection> pushConnections = new HashMap<>();
	
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
	void cleanup() {
		onlineSessions.clear();
		onlineClienTrackingIds.clear();
		profileSidsCache.clear();
		uuidToSessionId.clear();
		pushConnections.clear();
		wta = null;
		logger.info("Cleaned up");
	}
	
	public void onContainerSessionCreated(HttpSession session) {
		session.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_SESSION, new WebTopSession(wta, session));
	}
	
	public void onContainerSessionDestroyed(HttpSession session) {
		WebTopSession webtopSession = SessionContext.getWebTopSession(session);
		if (webtopSession != null) {
			String sessionId = webtopSession.getId();
			String clientTrackingId = webtopSession.getClientTrackingID();
			UserProfileId profileId = webtopSession.getProfileId(); // Extract userProfile info before cleaning session!
			
			long stamp = lock.writeLock();
			try {
				onlineSessions.remove(sessionId);
				pushConnections.remove(sessionId);
				if (profileId != null) {
					if (profileSidsCache.containsKey(profileId)) {
						// List at key may have not been prepared. Incase of 
						// active OPT configuration session is effectively 
						// only after code validation.
						profileSidsCache.get(profileId).remove(sessionId);
					}
					onlineClienTrackingIds.remove(profileId.toString() + "|" + clientTrackingId);
				}
				
				logger.trace("Session unregistered [{}, {}]", sessionId, profileId);
				
			} finally {
				lock.unlockWrite(stamp);
			}
			
			try {
				webtopSession.cleanup();
			} catch(Throwable t) {
				logger.error("Error destroying session", t);
			}
			
			if (profileId != null) {
				LogManager logMgr = wta.getLogManager();
				if (logMgr != null) logMgr.write(profileId, CoreManifest.ID, "LOGOUT", null, SessionContext.getClientRemoteIP(session), SessionContext.getClientPlainUserAgent(session), sessionId, null);
			}
		}
	}
	
	public void onPushResourceHeartbeat(String sessionId, AtmosphereResource resource) {
		long stamp = lock.readLock();
		try {
			if (onlineSessions.containsKey(sessionId)) {
				Subject subject = (Subject)resource.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
				if ((subject != null) && subject.isAuthenticated()) {
					HttpSession session = resource.session(false);
					if (session != null) {
						ServletUtils.touchSession(session);
						logger.trace("Session touched [{}]", session.getId());
					}
				}
			}
			
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	void registerWebTopSession(WebTopSession webtopSession) throws WTException {
		String sessionId = webtopSession.getId();
		
		long stamp = lock.writeLock();
		try {
			UserProfileId profileId = webtopSession.getProfileId();
			if (profileId == null) throw new WTException("Session [{0}] is not bound to a user", sessionId);

			onlineSessions.put(sessionId, webtopSession);
			pushConnections.put(sessionId, new PushConnection(sessionId, listEnqueuedMessages(webtopSession.getProfileId())));
			if (profileSidsCache.get(profileId) == null) profileSidsCache.put(profileId, new ProfileSids());
			profileSidsCache.get(profileId).add(sessionId);
			onlineClienTrackingIds.add(profileId.toString() + "|" + webtopSession.getClientTrackingID());
			
			logger.trace("Session registered [{}, {}]", sessionId, webtopSession.getProfileId());
			
		} finally {
			lock.unlockWrite(stamp);
		}
	}
	
	public WebTopSession getWebTopSession(String sessionId) {
		long stamp = lock.readLock();
		try {
			return onlineSessions.get(sessionId);
			
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	public List<WebTopSession> getWebTopSessions(UserProfileId profileId) {
		List<WebTopSession> list = new ArrayList<>();
		
		long stamp = lock.readLock();
		try {
			if (profileSidsCache.get(profileId) != null) {
				for (String sid : profileSidsCache.get(profileId)) {
					list.add(onlineSessions.get(sid));
				}
			}
			
		} finally {
			lock.unlockRead(stamp);
		}
		return list;
	}
	
	public boolean isOnline(String sessionId) {
		long stamp = lock.readLock();
		try {
			return onlineSessions.containsKey(sessionId);
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	public boolean isOnline(UserProfileId profileId) {
		long stamp = lock.readLock();
		try {
			return profileSidsCache.containsKey(profileId);
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	public boolean isOnline(UserProfileId profileId, String webtopClientId) {
		long stamp = lock.readLock();
		try {
			return onlineClienTrackingIds.contains(profileId.toString() + "|" + webtopClientId);
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	public void push(String sessionId, ServiceMessage message) {
		push(sessionId, Arrays.asList(message));
	}
	
	public void push(UserProfileId profileId, ServiceMessage message, boolean enqueueIfOffline) {
		push(profileId, Arrays.asList(message), enqueueIfOffline);
	}
	
	public void push(String sessionId, Collection<ServiceMessage> messages) {
		Thread t = new Thread("internalPush") {
			@Override
			public void run() {
				try {
					long stamp = lock.tryReadLock(10, TimeUnit.SECONDS);
					try {
						internalPush(sessionId, messages);
					} finally {
						lock.unlockRead(stamp);
					}
				} catch(InterruptedException ex) {
					logger.error("Unable to acquire readLock [{}]", ex, sessionId);
				}
			}
		};
		t.start();
	}
	
	public void push(UserProfileId profileId, Collection<ServiceMessage> messages, boolean enqueueIfOffline) {
		Thread t = new Thread("internalPush") {
			@Override
			public void run() {
				try {
					long stamp = lock.tryReadLock(10, TimeUnit.SECONDS);
					try {
						internalPush(profileId, messages, enqueueIfOffline);
					} finally {
						lock.unlockRead(stamp);
					}
				} catch(InterruptedException ex) {
					logger.error("Unable to acquire readLock [{}]", ex, profileId);
					if (enqueueIfOffline) {
						logger.debug("Persisting {} undelivered push messages for {} on db...", messages.size(), profileId);
						enqueueMessages(profileId, messages);
					}
				}
			}
		};
		t.start();
	}
	
	private void internalPush(String sessionId, Collection<ServiceMessage> messages) {
		if (onlineSessions.containsKey(sessionId)) {
			PushConnection pushCon = pushConnections.get(sessionId);
			if (pushCon != null) {
				pushCon.send(messages);
			} else {
				logger.error("PushConnection not available [{}]", sessionId);
			}
		}
	}
	
	private void internalPush(UserProfileId profileId, Collection<ServiceMessage> messages, boolean enqueueIfOffline) {
		ProfileSids sessionIds = profileSidsCache.get(profileId);
		if ((sessionIds != null) && !sessionIds.isEmpty()) {
			for (String sessionId : profileSidsCache.get(profileId)) {
				internalPush(sessionId, messages);
			}
		} else {
			if (enqueueIfOffline) {
				enqueueMessages(profileId, messages);
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
			for (ServiceMessage message : messages) {
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
	
	private static class ProfileSids extends HashSet<String> {
		public ProfileSids() {
			super();
		}
	}
}
