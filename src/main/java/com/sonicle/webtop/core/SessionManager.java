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
package com.sonicle.webtop.core;

import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.servlet.ServletHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpSession;
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
	
	public static final String ATTRIBUTE = "webtopsession";
	private WebTopApp wta = null;
	private final HashMap<String, WebTopSession> sessions = new HashMap<>();
	private final HashMap<String, ProfileSids> profileSidsCache = new HashMap<>();
	
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
		if(!sessions.isEmpty() || !profileSidsCache.isEmpty()) logger.warn("Internal structures should be empty... Why is this not true?");
		sessions.clear();
		profileSidsCache.clear();
		wta = null;
		logger.info("SessionManager destroyed");
	}
	
	public WebTopSession createSession(HttpSession httpSession) {
		String sid = ServletHelper.getSessionID(httpSession);
		
		try {
			WebTopSession wts = new WebTopSession(httpSession);
			httpSession.setAttribute(ATTRIBUTE, wts);
			logger.info("WTS created [{}]", sid);
			return wts;
			
		} catch(Exception ex) {
			logger.error("Error creating WTS [{}]", sid, ex);
			return null;
		}
	}
	
	public void destroySession(HttpSession httpSession) {
		String sid = ServletHelper.getSessionID(httpSession);
		
		try {
			WebTopSession wts = (WebTopSession)(httpSession.getAttribute(ATTRIBUTE));
			if(wts != null) {
				httpSession.removeAttribute(ATTRIBUTE);
				wts.destroy();
				logger.info("WTS destroyed [{}]", sid);
			}
		} catch(Exception ex) {
			logger.error("Error destroying WTS [{}]", sid, ex);
		}
	}
	
	void registerSession(WebTopSession session) throws Exception {
		String sid = session.getId();
		
		synchronized(sessions) {
			if(sessions.containsKey(sid)) {
				logger.error("Session [{}] is already registered", sid);
				throw new WTException("Session [{0}] is already registered", sid);
			} else {
				if(session.getUserProfile() == null) {
					logger.error("Session [{}] is not bound to a user", sid);
					throw new WTException("Session [{0}] is not bound to a user", sid);
				} else {
					String pid = session.getUserProfile().getStringId();
					sessions.put(sid, session);
					if(profileSidsCache.get(pid) == null) profileSidsCache.put(pid, new ProfileSids());
					profileSidsCache.get(pid).add(sid);
					logger.debug("Session registered [{}, {}]", pid, sid);
				}
			}
		}
	}
	
	void unregisterSession(WebTopSession session) throws Exception {
		String sid = session.getId();
		
		synchronized(sessions) {
			if(sessions.containsKey(sid)) {
				if(session.getUserProfile() == null) {
					logger.error("Session [{}] is not bound to a user", sid);
					throw new WTException("Session [{0}] is not bound to a user", sid);
				} else {
					String pid = session.getUserProfile().getStringId();
					sessions.remove(sid);
					if(profileSidsCache.get(pid) != null) {
						profileSidsCache.get(pid).remove(sid);
						if(profileSidsCache.get(pid).isEmpty()) profileSidsCache.remove(pid);
					}
					logger.debug("Session unregistered [{}, {}]", pid, sid);
				}
			}
		}
	}
	
	public WebTopSession getSession(HttpSession httpSession) {
		synchronized(sessions) {
			return sessions.get(httpSession.getId());
		}
	}
	
	public List<WebTopSession> getSessions(UserProfile.Id profileId) {
		List<WebTopSession> list = new ArrayList<>();
		synchronized(sessions) {
			String pid = profileId.toString();
			if(profileSidsCache.get(pid) != null) {
				for(String sid : profileSidsCache.get(pid)) {
					list.add(sessions.get(sid));
				}
			}
		}
		return list;
	}
	
	private static class ProfileSids extends HashSet<String> {
		public ProfileSids() {
			super();
		}
	}
	
	
	
	
	
	
	
	/*
	protected void registerSession(String httpSessionId, WebTopSession session) {
		synchronized(sessions) {
			if(sessions.containsKey(httpSessionId)) {
				logger.warn("Session [{}] is already registered", httpSessionId);
			} else {
				sessions.put(httpSessionId, session);
			}
		}
	}
	
	protected void unregisterSession(WebTopSession session) {
		String sid = session.getId();
		synchronized(sessions) {
			if(sessions.containsKey(sid)) {
				WebTopSession removed = sessions.remove(sid);
				if(removed == null) logger.warn("Session not registered [{}]", sid);
				
				// Removes cached pid->sid targetting session just unregistered
				for(Map.Entry<String, String> entry : pidToSidCache.entrySet()) {
					if(StringUtils.equals(sid, entry.getValue())) {
						pidToSidCache.remove(entry.getKey());
					}
				}
			}
		}
	}
	
	public WebTopSession getSession(HttpSession httpSession) {
		synchronized(sessions) {
			return sessions.get(httpSession.getId());
		}
	}
	
	public WebTopSession getSession(UserProfile.Id profileId) {
		synchronized(sessions) {
			if() {
				
			}
		}
	}
	*/
}
