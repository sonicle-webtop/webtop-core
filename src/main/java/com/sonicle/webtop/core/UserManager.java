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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.UserUid;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DomainDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.UserPersonalInfo;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.userinfo.UserInfoProviderBase;
import com.sonicle.webtop.core.userinfo.UserInfoProviderFactory;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.mail.internet.InternetAddress;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class UserManager {
	private static final Logger logger = WT.getLogger(UserManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	static synchronized UserManager initialize(WebTopApp wta) {
		if(initialized) throw new RuntimeException("Initialization already done");
		UserManager usem = new UserManager(wta);
		initialized = true;
		logger.info("UserManager initialized");
		return usem;
	}
	
	private WebTopApp wta = null;
	private final Object lock1 = new Object();
	private HashMap<UserProfile.Id, UserUidBag> userToUidBagCache = null;
	private final Object lock2 = new Object();
	private HashMap<UserProfile.Id, UserPersonalInfo> userToPersonalInfoCache = null;
	private final Object lock3 = new Object();
	private HashMap<String, UserProfile.Id> uidToUserCache = null;
	private HashMap<String, UserProfile.Id> roleUidToUserCache = null;
	private HashMap<UserProfile.Id, UserProfile.Data> userToBagCache = null;
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private UserManager(WebTopApp wta) {
		this.wta = wta;
		userToUidBagCache = new HashMap<>();
		userToPersonalInfoCache = new HashMap<>();
		uidToUserCache = new HashMap<>();
		roleUidToUserCache = new HashMap<>();
		userToBagCache = new HashMap<>();
		updateCache();
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		synchronized(lock3) {
			userToUidBagCache.clear();
			uidToUserCache.clear();
			roleUidToUserCache.clear();
			userToBagCache.clear();
		}
		logger.info("UserManager destroyed");
	}
	
	void updateCache() {
		synchronized(lock3) {
			try {
				buildUidCache();
			} catch(SQLException ex) {
				logger.error("Unable to build UID cache", ex);
			}
		}
	}
	
	public UserInfoProviderBase getUserInfoProvider() throws WTException {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
		String providerName = css.getUserInfoProvider();
		return UserInfoProviderFactory.getProvider(providerName, wta.getConnectionManager(), wta.getSettingsManager());
	}
	
	public boolean isUserInfoProviderWritable() {
		try {
			return getUserInfoProvider().canWrite();
		} catch(WTException ex) {
			//TODO: logging?
			return false;
		}
	}
	
	public UserProfile.Data userData(UserProfile.Id pid) throws WTException {
		synchronized(lock1) {
			if(!userToBagCache.containsKey(pid)) {
				try {
					OUser user = getUser(pid);
					if(user == null) return null;
					UserPersonalInfo info = userPersonalInfo(pid);
					InternetAddress ia = new InternetAddress(info.getEmail(), user.getDisplayName());
					
					UserProfile.Data data = new UserProfile.Data(user, ia);
					userToBagCache.put(pid, data);
					return data;
				} catch(WTException ex) {
					logger.error("Unable to find user [{}]", pid);
					throw ex;
				} catch (UnsupportedEncodingException ex) {
					logger.error("Unsupported encoding", ex);
					throw new WTException(ex);
				}
			} else {
				return userToBagCache.get(pid);
			}
		}
	}
	
	public UserPersonalInfo userPersonalInfo(UserProfile.Id pid) throws WTException {
		synchronized(lock2) {
			if(!userToPersonalInfoCache.containsKey(pid)) {
				try {
					UserInfoProviderBase uip = getUserInfoProvider();
					UserPersonalInfo info = uip.getInfo(pid.getDomainId(), pid.getUserId());
					userToPersonalInfoCache.put(pid, info);
					return info;
				} catch(WTException ex) {
					logger.error("Unable to find personal info for user [{}]", pid);
					throw ex;
				}	
			} else {
				return userToPersonalInfoCache.get(pid);
			}
		}
	}
	
	public String userToUid(UserProfile.Id pid) {
		synchronized(lock3) {
			if(!userToUidBagCache.containsKey(pid)) throw new WTRuntimeException("[userToSidCache] Cache miss on key {0}", pid.toString());
			return userToUidBagCache.get(pid).userUid;
		}
	}
	
	public String userToRoleUid(UserProfile.Id pid) {
		synchronized(lock3) {
			if(!userToUidBagCache.containsKey(pid)) throw new WTRuntimeException("[userToUidCache] Cache miss on key {0}", pid.toString());
			return userToUidBagCache.get(pid).roleUid;
		}
	}
	
	public UserProfile.Id uidToUser(String uid) {
		synchronized(lock3) {
			if(!uidToUserCache.containsKey(uid)) throw new WTRuntimeException("[uidToUserCache] Cache miss on key {0}", uid);
			return uidToUserCache.get(uid);
		}
	}
	
	public UserProfile.Id roleUidToUser(String uid) {
		synchronized(lock3) {
			if(!roleUidToUserCache.containsKey(uid)) throw new WTRuntimeException("[roleUidToUserCache] Cache miss on key {0}", uid);
			return roleUidToUserCache.get(uid);
		}
	}
	
	public ODomain getDomain(String domainId) throws WTException {
		DomainDAO dao = DomainDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.selectById(con, domainId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OUser getUser(UserProfile.Id pid) throws WTException {
		UserDAO dao = UserDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			return dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public String getInternetUserId(UserProfile.Id pid) throws WTException {
		ODomain domain = getDomain(pid.getDomainId());
		return new UserProfile.Id(domain.getDomainName(), pid.getUserId()).toString();
	}
	
	private void buildUidCache() throws SQLException {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection();
			UserDAO dao = UserDAO.getInstance();
			List<UserUid> uuids = dao.selectAllUids(con);
			
			userToUidBagCache.clear();
			uidToUserCache.clear();
			roleUidToUserCache.clear();
			for(UserUid uuid : uuids) {
				UserProfile.Id pid = new UserProfile.Id(uuid.getDomainId(), uuid.getUserId());
				userToUidBagCache.put(pid, new UserUidBag(uuid.getUserUid(), uuid.getRoleUid()));
				uidToUserCache.put(uuid.getUserUid(), pid);
				roleUidToUserCache.put(uuid.getRoleUid(), pid);
			}
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public static class UserUidBag {
		public String userUid;
		public String roleUid;
		
		public UserUidBag() {}
		
		public UserUidBag(String uid, String roleUid) {
			this.userUid = uid;
			this.roleUid = roleUid;
		}
	}
}
