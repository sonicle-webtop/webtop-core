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

import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.bol.model.SessionInfo;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.servlet.ServletHelper;
import com.sonicle.webtop.core.util.IdentifierUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.uadetector.ReadableUserAgent;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.web.subject.support.WebDelegatingSubject;
import org.joda.time.DateTime;
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
	
	public static final String ATTRIBUTE_WEBTOP_SESSION = "wts";
	public static final String ATTRIBUTE_CSRF_TOKEN = "csrf";
	public static final String ATTRIBUTE_CLIENT_IP = "clientIp";
	public static final String ATTRIBUTE_CLIENT_USERAGENT = "clientUA";
	public static final String ATTRIBUTE_CLIENT_RA_USERAGENT = "clientReadableUA";
	public static final String ATTRIBUTE_CLIENT_LOCALE = "clientLocale";
	public static final String ATTRIBUTE_REFERER_URI = "refererUri";
	
	private WebTopApp wta = null;
	private final HashMap<String, WebTopSession> onlineSessions = new HashMap<>();
	private final HashMap<String, Integer> wsPushSessions = new HashMap<>();
	private final HashMap<UserProfile.Id, ProfileSids> profileSidsCache = new HashMap<>();
	
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
		// Internal structures should be empty during this method call.
		// Sessions listeners should have called session destroy!
		if(!onlineSessions.isEmpty() || !profileSidsCache.isEmpty()) logger.warn("Internal structures should be empty... Why is this not true?");
		onlineSessions.clear();
		wsPushSessions.clear();
		profileSidsCache.clear();
		wta = null;
		logger.info("SessionManager destroyed");
	}
	
	public static WebTopSession getWebTopSession(Session session) {
		return (WebTopSession)session.getAttribute(ATTRIBUTE_WEBTOP_SESSION);
	}
	
	public static String getCSRFToken(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_CSRF_TOKEN);
	}
	
	public static String getClientIP(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_CLIENT_IP);
	}
	
	public static String getClientUserAgent(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_CLIENT_USERAGENT);
	}
	
	public static ReadableUserAgent getClientReadableUserAgent(Session session) {
		return (ReadableUserAgent)session.getAttribute(ATTRIBUTE_CLIENT_USERAGENT);
	}
	
	public static Locale getClientLocale(Session session) {
		return (Locale)session.getAttribute(ATTRIBUTE_CLIENT_LOCALE);
	}
	
	public static String getRefererUri(Session session) {
		return (String)session.getAttribute(ATTRIBUTE_REFERER_URI);
	}
	
	private void dumpRequestData(Session session, HttpServletRequest request) {
		
	}
	
	void shiroSessionStarted(Session session) {
		try {
			startShiroSession(session);
		} catch (Exception ex) {
			logger.error("Error starting session [{}]", session.getId().toString(), ex);
		}
	}
	
	void shiroSessionStopped(Session session) {
		try {
			stopShiroSession(session);
		} catch (Exception ex) {
			logger.error("Error stopping session [{}]", session.getId().toString(), ex);
		}
	}
	
	void shiroSessionExpired(Session session) {
		try {
			stopShiroSession(session);
		} catch (Exception ex) {
			logger.error("Error expiring session [{}]", session.getId().toString(), ex);
		}
	}
	
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
	
	private void startShiroSession(Session session) throws Exception {
		WebTopSession wts = new WebTopSession(wta, session);
		session.setAttribute(ATTRIBUTE_WEBTOP_SESSION, wts);
		session.setAttribute(ATTRIBUTE_CSRF_TOKEN, IdentifierUtils.getCRSFToken());
		
		// Dump into session some client data
		if(session.getAttribute("dump") == null) {
			HttpServletRequest request = (HttpServletRequest)((WebDelegatingSubject)SecurityUtils.getSubject()).getServletRequest();
			session.setAttribute("dump", true);
			String ua = ServletUtils.getUserAgent(request);
			session.setAttribute(ATTRIBUTE_CLIENT_IP, ServletUtils.getClientIP(request));
			session.setAttribute(ATTRIBUTE_CLIENT_USERAGENT, ua);
			session.setAttribute(ATTRIBUTE_CLIENT_RA_USERAGENT, WebTopApp.getUserAgentInfo(ua));
			session.setAttribute(ATTRIBUTE_CLIENT_LOCALE, ServletHelper.homogenizeLocale(request));
			session.setAttribute(ATTRIBUTE_REFERER_URI, ServletUtils.getReferer(request));
		}
		logger.trace("WTS created [{}]", session.getId());
	}
	
	private void stopShiroSession(Session session) throws Exception {
		WebTopSession wts = SessionManager.getWebTopSession(session);
		if(wts == null) throw new Exception("WTS is null");
		
		String sid = session.getId().toString();
		UserProfile.Id pid = wts.getProfileId(); // Extract userProfile info before cleaning session!
		wts.cleanup();
		if(pid != null) {
			unregisterWebTopSession(sid, pid);
			wta.getLogManager().write(pid, CoreManifest.ID, "LOGOUT", null, getClientIP(session), getClientUserAgent(session), sid, null);
		}
		logger.trace("WTS destroyed [{}]", sid);
	}
	
	void registerWebTopSession(String sid, WebTopSession wts) throws WTException {
		synchronized(onlineSessions) {
			if(onlineSessions.containsKey(sid)) throw new WTException("Session [{0}] is already registered", sid);
			UserProfile.Id pid = wts.getUserProfile().getId();
			if(pid == null) throw new WTException("Session [{0}] is not bound to a user", sid);
			onlineSessions.put(sid, wts);
			if(profileSidsCache.get(pid) == null) profileSidsCache.put(pid, new ProfileSids());
			profileSidsCache.get(pid).add(sid);
			logger.trace("Session registered [{}, {}]", sid, pid);
		}
	}
	
	private void unregisterWebTopSession(String sessionId, UserProfile.Id profileId) throws WTException {
		synchronized(onlineSessions) {
			if(onlineSessions.containsKey(sessionId)) {
				if(profileId != null) {
					if(profileSidsCache.get(profileId) != null) {
						profileSidsCache.get(profileId).remove(sessionId);
						if(profileSidsCache.get(profileId).isEmpty()) profileSidsCache.remove(profileId);
					}
					onlineSessions.remove(sessionId);
				} else {
					logger.warn("Session [{}] is not bound to a user", sessionId);
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
	
	public WebTopSession getWebTopSession(String sessionId) {
		synchronized(onlineSessions) {
			return onlineSessions.get(sessionId);
		}
	}
	
	public List<WebTopSession> getWebTopSessions(UserProfile.Id profileId) {
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
	
	public Session getSession(String sessionId) throws SessionException {
		DefaultSessionKey sk = new DefaultSessionKey(sessionId);
		return SecurityUtils.getSecurityManager().getSession(sk);
	}
	
	public List<SessionInfo> listOnlineSessions() {
		ArrayList<SessionInfo> items = new ArrayList<>();
		synchronized(onlineSessions) {
			DateTime now = DateTime.now();
			for(Map.Entry<String, WebTopSession> entry : onlineSessions.entrySet()) {
				WebTopSession wts = entry.getValue();
				int count = countWsPushSessions(wts.getId());
				items.add(new SessionInfo(now, wts.getSession(), wts.getUserProfile().getId(), count));
			}
		}
		return items;
	}
	
	public void invalidateSession(String sessionId) throws SessionException {
		Session session = getSession(sessionId);
		if(session != null) session.stop();
	}
	
	private static class ProfileSids extends HashSet<String> {
		public ProfileSids() {
			super();
		}
	}
}
