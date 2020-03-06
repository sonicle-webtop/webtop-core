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

import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.LangUtils;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import javax.servlet.http.HttpSession;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class SessionManager implements PushConnection.MessageStorage {
	private final static Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);
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
		LOGGER.info("Initialized");
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
		LOGGER.info("Cleaned up");
	}
	
	public void onContainerSessionCreated(HttpSession session) {
		session.setAttribute(SessionManager.ATTRIBUTE_WEBTOP_SESSION, new WebTopSession(wta, session));
	}
	
	public void onContainerSessionDestroyed(HttpSession session) {
		WebTopSession webtopSession = SessionContext.getWebTopSession(session);
		if (webtopSession != null) {
			String sessionId = session.getId(); // webtopSession.getId();
			String clientTrackingId = webtopSession.getClientTrackingID();
			UserProfileId profileId = webtopSession.getProfileId(); // Extract userProfile info before cleaning session!
			
			long stamp = lock.writeLock();
			try {
				onlineSessions.remove(sessionId);
				PushConnection pushCon = pushConnections.remove(sessionId);
				if (pushCon != null) pushCon.ready();
				if (profileId != null) {
					if (profileSidsCache.containsKey(profileId)) {
						// List at key may have not been prepared. Incase of 
						// active OPT configuration session is effectively 
						// only after code validation.
						profileSidsCache.get(profileId).remove(sessionId);
					}
					onlineClienTrackingIds.remove(profileId.toString() + "|" + clientTrackingId);
				}
				if (LOGGER.isTraceEnabled()) LOGGER.trace("Session unregistered [{}, {}]", sessionId, profileId);
				
			} finally {
				lock.unlockWrite(stamp);
			}
			
			try {
				webtopSession.cleanup();
			} catch(Throwable t) {
				LOGGER.error("Error destroying session", t);
			}
			
			if (profileId != null) {
				AuditLogManager auditLogMgr = wta.getAuditLogManager();
				if (auditLogMgr != null) auditLogMgr.write(profileId, sessionId, CoreManifest.ID, "AUTH", "LOGOUT", null, null);
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
						if (LOGGER.isTraceEnabled()) LOGGER.trace("Session touched [{}]", session.getId());
					}
				}
			}
			
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	public void onPushResourceReady(String sessionId, AtmosphereResource resource) {
		long stamp = lock.readLock();
		try {
			PushConnection pushCon = pushConnections.get(sessionId);
			if (pushCon != null) pushCon.ready();
			
		} finally {
			lock.unlockRead(stamp);
		}
	}
	
	void registerWebTopSession(WebTopSession webtopSession) throws WTException {
		String sessionId = webtopSession.getId();
		
		long stamp = lock.writeLock();
		try {
			UserProfileId profileId = webtopSession.getProfileId();
			if (profileId == null) throw new WTException("Session is not bound to a user [{}]", sessionId);

			onlineSessions.put(sessionId, webtopSession);
			pushConnections.put(sessionId, new PushConnection(this, sessionId, profileId));
			if (profileSidsCache.get(profileId) == null) profileSidsCache.put(profileId, new ProfileSids());
			profileSidsCache.get(profileId).add(sessionId);
			onlineClienTrackingIds.add(profileId.toString() + "|" + webtopSession.getClientTrackingID());
			
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Session registered [{}, {}]", sessionId, webtopSession.getProfileId());
			
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
	
	public void push(String sessionId, ServiceMessage message, boolean important) {
		push(sessionId, Arrays.asList(message), important);
	}
	
	public void push(UserProfileId profileId, ServiceMessage message, boolean important) {
		push(profileId, Arrays.asList(message), important);
	}
	
	public void push(String sessionId, Collection<ServiceMessage> messages, boolean important) {
		Thread t = new Thread(LangUtils.formatMessage("internalPush-{}", sessionId)) {
			@Override
			public void run() {
				try {
					long stamp = lock.tryReadLock(10, TimeUnit.SECONDS);
					try {
						internalPush(sessionId, messages, important);
					} finally {
						lock.unlockRead(stamp);
					}
				} catch(InterruptedException ex) {
					LOGGER.error("Unable to acquire readLock [{}]", ex, sessionId);
					if (important) {
						LOGGER.warn("Cannot detect the user for session '{}', {} messages lost!", sessionId, messages.size());
					}
				}
			}
		};
		t.start();
	}
	
	public void push(UserProfileId profileId, Collection<ServiceMessage> messages, boolean important) {
		Thread t = new Thread(LangUtils.formatMessage("internalPush-{}", profileId)) {
			@Override
			public void run() {
				try {
					long stamp = lock.tryReadLock(10, TimeUnit.SECONDS);
					try {
						internalPush(profileId, messages, important);
					} finally {
						lock.unlockRead(stamp);
					}
				} catch (InterruptedException ex) {
					LOGGER.error("Unable to acquire readLock [{}]", ex, profileId);
					if (important) {
						persistMessages(profileId, messages);
					}
				}
			}
		};
		t.start();
	}
	
	private void internalPush(UserProfileId profileId, Collection<ServiceMessage> messages, boolean important) {
		ProfileSids sessionIds = profileSidsCache.get(profileId);
		if ((sessionIds != null) && !sessionIds.isEmpty()) {
			// A user-profile can have multiple linked sessions, so send same 
			// messages to each one available. Messages can be persisted only
			// by the last in case we have no succesfull deliveries at evaluation
			// time.
			boolean sent = false;
			Iterator it = profileSidsCache.get(profileId).iterator();
			while (it.hasNext()) {
				final String sessionId = (String)it.next();
				PushConnection pushCon = pushConnections.get(sessionId);
				if (pushCon != null) sent = sent || pushCon.send(messages, important, it.hasNext() ? true : sent);
			}
			
		} else {
			// No session found for user-profile, persist messages if necessary.
			if (important) {
				persistMessages(profileId, messages);
			}
		}
	}
	
	private void internalPush(String sessionId, Collection<ServiceMessage> messages, boolean enqueueIfOffline) {
		PushConnection pushCon = pushConnections.get(sessionId);
		if (pushCon != null) pushCon.send(messages, enqueueIfOffline);
	}
	
	@Override
	public ArrayList<ServiceMessage> resumeMessages(UserProfileId profileId) {
		ArrayList<ServiceMessage> messages = new ArrayList<>();
		MessageQueueDAO mqDao = MessageQueueDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			String processId = IdentifierUtils.getUUIDRandom(); // Mark user's messages as "under process" and gets them
			int ret = mqDao.updatePidIfNullByDomainUser(con, profileId.getDomainId(), profileId.getUserId(), processId);
			if (ret > 0) {
				if (LOGGER.isTraceEnabled()) LOGGER.trace("Resuming {} messages for user '{}' [{}]", ret, profileId, processId);
				List<OMessageQueue> queued = mqDao.selectByPid(con, processId);
				if (!queued.isEmpty()) {
					Class clazz = null;
					for (OMessageQueue message : queued) {
						try {
							clazz = Class.forName(message.getMessageType());
							messages.add((ServiceMessage)JsonResult.gson.fromJson(message.getMessageRaw(), clazz));
						} catch(Throwable t1) {
							LOGGER.warn("Unable to unserialize message '{}' for '{}'", message.getMessageType(), profileId, t1);
						}
					}
					mqDao.deleteByPid(con, processId);
				}
			}
			
		} catch(Throwable t) {
			LOGGER.error("Unable to resume messages for '{}'", profileId, t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return messages;
	}
	
	@Override
	public void persistMessages(UserProfileId profileId, Collection<ServiceMessage> messages) {
		MessageQueueDAO mqDao = MessageQueueDAO.getInstance();
		Connection con = null;
		
		try {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Persisting {} messages for user '{}'", messages.size(), profileId.toString());
			con = WT.getCoreConnection();
			ArrayList<OMessageQueue> omqs = new ArrayList<>(messages.size());
			for (ServiceMessage message : messages) {
				omqs.add(AppManagerUtils.fillOMessageQueue(new OMessageQueue(), profileId, message.getClass().getName(), JsonResult.gson.toJson(message)));
			}
			mqDao.batchInsert(con, omqs);
			
		} catch(Throwable t) {
			LOGGER.error("Unable to persist messages for '{}'", profileId, t);
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
