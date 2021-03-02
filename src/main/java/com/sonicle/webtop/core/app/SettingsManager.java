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
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */

package com.sonicle.webtop.core.app;

import com.sonicle.commons.cache.AbstractPassiveExpiringMap;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.CId;
import com.sonicle.webtop.core.bol.DomainSettingRow;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingReader;
import com.sonicle.webtop.core.bol.ODomainSetting;
import com.sonicle.webtop.core.bol.OSetting;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUserSetting;
import com.sonicle.webtop.core.bol.SettingRow;
import com.sonicle.webtop.core.bol.model.DomainSetting;
import com.sonicle.webtop.core.bol.model.SystemSetting;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DomainSettingDAO;
import com.sonicle.webtop.core.dal.SettingDAO;
import com.sonicle.webtop.core.dal.SettingDbDAO;
import com.sonicle.webtop.core.dal.UserSettingDAO;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingManager;
import com.sonicle.webtop.core.sdk.interfaces.ISettingManager;
import com.sonicle.webtop.core.sdk.interfaces.IUserSettingManager;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class SettingsManager implements IServiceSettingReader, IServiceSettingManager, IUserSettingManager, ISettingManager {
	private static final Logger logger = WT.getLogger(SettingsManager.class);
	private static boolean initialized = false;
	
	/**
	 * Initialization method. This method should be called once.
	 * 
	 * @param wta WebTopApp instance.
	 * @return The instance.
	 */
	public static synchronized SettingsManager initialize(WebTopApp wta) {
		if (initialized) throw new RuntimeException("Initialization already done");
		SettingsManager setm = new SettingsManager(wta);
		initialized = true;
		logger.info("Initialized");
		return setm;
	}
	
	public static String[] asArray(String value) {
		return StringUtils.split(value, ",");
	}
	
	private WebTopApp wta = null;
	private AtomicBoolean useSettingsCaching = new AtomicBoolean(true);
	private AtomicBoolean useUserSettingsCaching = new AtomicBoolean(false);
	private final SettingsCache cacheSettings = new SettingsCache(1, TimeUnit.MINUTES);
	private final DomainSettingsCache cacheDomainSettings = new DomainSettingsCache(1, TimeUnit.MINUTES);
	private final UserSettingsCache cacheUserSettings = new UserSettingsCache(1, TimeUnit.MINUTES);
	
	/**
	 * Private constructor.
	 * Instances of this class must be created using static initialize method.
	 * @param wta WebTopApp instance.
	 */
	private SettingsManager(WebTopApp wta) {
		this.wta = wta;
	}
	
	/**
	 * Performs cleanup process.
	 */
	void cleanup() {
		wta = null;
		logger.info("Cleaned up");
	}
	
	/**
	 * Gets the setting value indicated by the specified key.
	 * Returns a null value if the key is not found.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getServiceSetting(final String serviceId, final String key) {
		return getSetting(serviceId, key);
	}
	
	/**
	 * Gets the setting value indicated by the specified key using priority path:
	 *  1 - Setting by domain/service/key
	 *  2 - Setting by service/key
	 * Returns a null value if the key is not found.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getServiceSetting(final String domainId, final String serviceId, final String key) {
		String value = getSetting(domainId, serviceId, key);
		if (value != null) return value;
		return getSetting(serviceId, key);
	}
	
	/**
	 * Sets the setting value indicated by the specified key.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	@Override
	public boolean setServiceSetting(String serviceId, String key, Object value) {
		return setSetting(serviceId, key, value);
	}
	
	/**
	 * Sets the setting value indicated by the specified key. 
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.s
	 */
	@Override
	public boolean setServiceSetting(String domainId, String serviceId, String key, Object value) {
		return setSetting(domainId, serviceId, key, value);
	}
	
	/**
	 * Deletes the setting (system) value targeted by the specified key.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteServiceSetting(final String serviceId, final String key) {
		return deleteSetting(serviceId, key);
	}
	
	/**
	 * Deletes the setting (domain) value targeted by the specified key.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteServiceSetting(final String domainId, final String serviceId, final String key) {
		return deleteSetting(domainId, serviceId, key);
	}
	
	/**
	 * Gets the setting value indicated by the specified key using priority path:
	 *  1 - UserSetting by domain/service/user/key
	 *  2 - ServiceSetting by domain/service/key
	 *  3 - ServiceSetting by service/key
	 * Returns a null value if the key is not found.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getUserSetting(String domainId, String userId, String serviceId, String key) {
		String value = getSetting(domainId, userId, serviceId, key);
		if (value != null) return value;
		return getServiceSetting(domainId, serviceId, key);
	}
	
	/**
	 * Gets the setting value indicated by the specified key.
	 * Returns a null value if the key is not found.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The string value of the setting.
	 */
	@Override
	public String getUserSetting(UserProfileId profileId, String serviceId, String key) {
		return getUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, key);
	}
	
	/**
	 * Gets the setting values compliant to the specified key.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting. (treated as LIKE query)
	 * @return List of settings.
	 */
	@Override
	public List<OUserSetting> getUserSettings(UserProfileId profileId, String serviceId, String key) {
		return getUserSettings(profileId.getDomainId(), profileId.getUserId(), serviceId, key);
	}
	
	/**
	 * Gets the setting values compliant to the specified key.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting. (treated as LIKE query)
	 * @return List of setting values.
	 */
	@Override
	public List<OUserSetting> getUserSettings(String domainId, String userId, String serviceId, String key) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			return dao.selectByDomainServiceUserKeyLike(con, domainId, userId, serviceId, key);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings (user) [{}, {}, {}, {}]", domainId, userId, serviceId, key, ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<UserProfileId> listProfilesWith(String serviceId, String key, Object value) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		ArrayList<UserProfileId> profiles = new ArrayList<>();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			List<OUserSetting> sets = dao.selectByServiceKeyValue(con, serviceId, key, valueToString(value));
			for(OUserSetting set : sets) {
				profiles.add(new UserProfileId(set.getDomainId(), set.getUserId()));
			}
		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings (user) [{}, {}, {}]", serviceId, key, String.valueOf(value), ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return profiles;
	}
	
	public List<OUserSetting> getUserSettings(String serviceId, String key, Object value) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			return dao.selectByServiceKeyValue(con, serviceId, key, valueToString(value));

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings (user) [{}, {}]", serviceId, key, ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Sets the setting value indicated by the specified key.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	@Override
	public boolean setUserSetting(UserProfileId profileId, String serviceId, String key, Object value) {
		return setUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, key, value);
	}
	
	/**
	 * Sets the setting value indicated by the specified key.
	 * @param domainId The domain ID.
	 * @param serviceId The service ID.
	 * @param userId The user ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	@Override
	public boolean setUserSetting(String domainId, String userId, String serviceId, String key, Object value) {
		if (value!=null) {
			UserSettingDAO dao = UserSettingDAO.getInstance();
			Connection con = null;
			OUserSetting item = null;

			try {
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				item = new OUserSetting();
				item.setDomainId(domainId);
				item.setUserId(userId);
				item.setServiceId(serviceId);
				item.setKey(key);
				item.setValue(valueToString(value));

				int ret = dao.update(con, item);
				if(ret == 0) ret = dao.insert(con, item);
				return true;

			} catch (Exception ex) {
				WebTopApp.logger.error("Unable to set setting (user) [{}, {}, {}, {}]", domainId, userId, serviceId, key, ex);
				return false;
			} finally {
				DbUtils.closeQuietly(con);
			}
		} else {
			deleteUserSetting(domainId, userId, serviceId, key);
			return true;
		}
	}
	
	/**
	 * Deletes the setting value indicated by the specified key.
	 * @param profileId The profile ID to extract domain and user information.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	@Override
	public boolean deleteUserSetting(UserProfileId profileId, String serviceId, String key) {
		return deleteUserSetting(profileId.getDomainId(), profileId.getUserId(), serviceId, key);
	}
	
	/**
	 * Deletes the setting value indicated by the specified key.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	@Override
	public boolean deleteUserSetting(String domainId, String userId, String serviceId, String key) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			int ret = dao.deleteByDomainServiceUserKey(con, domainId, userId, serviceId, key);
			return (ret > 0);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to delete setting (user) [{}, {}, {}, {}]", domainId, userId, serviceId, key, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean clearUserSettings(String domainId, String userId) {
		UserSettingDAO dao = UserSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			int ret = dao.deleteByDomainUser(con, domainId, userId);
			return (ret > 0);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to clear settings (user) [{}, {}]", domainId, userId, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SystemSetting> listSettings(boolean hidden) {
		ArrayList<SystemSetting> items = new ArrayList<>();
		SettingDAO dao = SettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			for(SettingRow setting : dao.selectAll(con, hidden)) {
				items.add(new SystemSetting(setting));
			}
			return items;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings", ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<DomainSetting> listSettings(String domainId, boolean hidden) {
		ArrayList<DomainSetting> items = new ArrayList<>();
		DomainSettingDAO dao = DomainSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			for(DomainSettingRow setting : dao.selectAll(con, hidden)) {
				items.add(new DomainSetting(setting));
			}
			return items;

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read settings", ex);
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OSettingDb getSettingInfo(String serviceId, String key) {
		SettingDbDAO dao = SettingDbDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			return dao.selectByServiceKey(con, serviceId, key);

		} catch (Exception ex) {
			WebTopApp.logger.error("Unable to read setting info", ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String getSetting(String serviceId, String key) {
		if (useSettingsCaching.get()) {
			logger.trace("Looking-up Setting from Cache... [{}, {}]", serviceId, key);
			return cacheSettings.get(serviceId + "|" + key);
			
		} else {
			Connection con = null;
			
			try {
				logger.trace("Reading Setting from DB... [{}, {}]", serviceId, key);
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doSettingGet(con, serviceId, key);
				
			} catch(Throwable t) {
				logger.error("Unable to read Setting [{}, {}]", serviceId, key, t);
				throw new RuntimeException(t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private String getSetting(String domainId, String serviceId, String key) {
		if (useSettingsCaching.get()) {
			logger.trace("Looking-up DomainSetting from Cache... [{}, {}, {}]", domainId, serviceId, key);
			return cacheDomainSettings.get(domainId + "|" + serviceId + "|" + key);
			
		} else {
			Connection con = null;
			
			try {
				logger.trace("Reading DomainSetting from DB... [{}, {}, {}]", domainId, serviceId, key);
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doDomainSettingGet(con, domainId, serviceId, key);
				
			} catch(Throwable t) {
				logger.error("Unable to read DomainSetting [{}, {}, {}]", domainId, serviceId, key, t);
				throw new RuntimeException(t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private String getSetting(String domainId, String userId, String serviceId, String key) {
		if (useUserSettingsCaching.get()) {
			logger.trace("Looking-up UserSetting from Cache... [{}, {}, {}, {}]", domainId, userId, serviceId, key);
			return cacheUserSettings.get(domainId + "|" + userId + "|" + serviceId + "|" + key);
			
		} else {
			Connection con = null;
			
			try {
				logger.trace("Reading UserSetting from DB... [{}, {}, {}, {}]", domainId, userId, serviceId, key);
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doUserSettingGet(con, domainId, userId, serviceId, key);
				
			} catch(Throwable t) {
				logger.error("Unable to read UserSetting [{}, {}, {}, {}]", domainId, userId, serviceId, key, t);
				throw new RuntimeException(t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private boolean setSetting(String serviceId, String key, Object value) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			boolean ret = doSettingUpsert(con, serviceId, key, value);
			//TODO: evaluate wether to lock by full-key these both actions
			if (useSettingsCaching.get()) {
				cacheSettings.remove(serviceId + "|" + key);
			}
			return ret;
			
		} catch(Throwable t) {
			logger.error("Unable to set Setting [{}, {}]", serviceId, key, t);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean setSetting(String domainId, String serviceId, String key, Object value) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			boolean ret = doDomainSettingUpsert(con, domainId, serviceId, key, value);
			//TODO: evaluate wether to lock by full-key these both actions
			if (useSettingsCaching.get()) {
				cacheSettings.remove(domainId + "|" + serviceId + "|" + key);
			}
			return ret;
			
		} catch(Throwable t) {
			logger.error("Unable to set DomainSetting [{}, {}, {}]", domainId, serviceId, key, t);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean deleteSetting(String serviceId, String key) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			boolean ret = doSettingDelete(con, serviceId, key) > 0;
			//TODO: evaluate wether to lock by full-key these both actions
			if (useSettingsCaching.get()) {
				cacheSettings.remove(serviceId + "|" + key);
			}
			return ret;

		} catch(Throwable t) {
			logger.error("Unable to delete Setting [{}, {}]", serviceId, key, t);
			throw new RuntimeException(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private boolean deleteSetting(String domainId, String serviceId, String key) {
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			boolean ret = doDomainSettingDelete(con, domainId, serviceId, key) > 0;
			//TODO: evaluate wether to lock by full-key these both actions
			if (useSettingsCaching.get()) {
				cacheDomainSettings.remove(domainId + "|" + serviceId + "|" + key);
			}
			return ret;

		} catch(Throwable t) {
			logger.error("Unable to delete DomainSetting [{}, {}, {}]", domainId, serviceId, key, t);
			throw new RuntimeException(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String valueToString(Object value) {
		return (value == null) ? null : String.valueOf(value);
	}
	
	private String doSettingGet(Connection con, String serviceId, String key) throws DAOException {
		SettingDAO setDao = SettingDAO.getInstance();
		
		OSetting item = setDao.selectByServiceKey(con, serviceId, key);
		return (item != null) ? StringUtils.defaultString(item.getValue()) : null;
	}
	
	private boolean doSettingUpsert(Connection con, String serviceId, String key, Object value) throws DAOException {
		SettingDAO setDao = SettingDAO.getInstance();
		
		OSetting item = new OSetting();
		item.setServiceId(serviceId);
		item.setKey(key);
		item.setValue(valueToString(value));
		
		int ret = setDao.update(con, item);
		if (ret == 0) ret = setDao.insert(con, item);
		return ret > 0;
	}
	
	private int doSettingDelete(Connection con, String serviceId, String key) throws DAOException {
		SettingDAO setDao = SettingDAO.getInstance();
		return setDao.deleteByServiceKey(con, serviceId, key);
	}
	
	private String doDomainSettingGet(Connection con, String domainId, String serviceId, String key) throws DAOException {
		DomainSettingDAO setDao = DomainSettingDAO.getInstance();
		
		ODomainSetting item = setDao.selectByDomainServiceKey(con, domainId, serviceId, key);
		return (item != null) ? StringUtils.defaultString(item.getValue()) : null;
	}
	
	private boolean doDomainSettingUpsert(Connection con, String domainId, String serviceId, String key, Object value) throws DAOException {
		DomainSettingDAO setDao = DomainSettingDAO.getInstance();
		
		ODomainSetting item = new ODomainSetting();
		item.setDomainId(domainId);
		item.setServiceId(serviceId);
		item.setKey(key);
		item.setValue(valueToString(value));
		
		int ret = setDao.update(con, item);
		if (ret == 0) ret = setDao.insert(con, item);
		return ret > 0;
	}
	
	private int doDomainSettingDelete(Connection con, String domainId, String serviceId, String key) throws DAOException {
		DomainSettingDAO setDao = DomainSettingDAO.getInstance();
		return setDao.deleteByDomainServiceKey(con, domainId, serviceId, key);
	}
	
	private String doUserSettingGet(Connection con, String domainId, String userId, String serviceId, String key) throws DAOException {
		UserSettingDAO setDao = UserSettingDAO.getInstance();
		
		OUserSetting item = setDao.selectByDomainUserServiceKey(con, domainId, userId, serviceId, key);
		return (item != null) ? StringUtils.defaultString(item.getValue()) : null;
	}
	
	private int doUserSettingDelete(Connection con, String domainId, String userId, String serviceId, String key) throws DAOException {
		UserSettingDAO setDao = UserSettingDAO.getInstance();
		return setDao.deleteByDomainServiceUserKey(con, domainId, userId, serviceId, key);
	}
	
	private class SettingsCache extends AbstractPassiveExpiringMap<String, String> {
		
		public SettingsCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit, true);
		}
		
		@Override
		protected String internalGetValue(String key) {
			CId cid = new CId(key, 2);
			Connection con = null;
			
			try {
				logger.trace("[SettingsCache] Reading Setting from DB... [{}, {}]", cid.getToken(0), cid.getToken(1));
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doSettingGet(con, cid.getToken(0), cid.getToken(1));
				
			} catch(Throwable t) {
				logger.error("[SettingsCache] Unable to read Setting [{}, {}]", cid.getToken(0), cid.getToken(1), t);
				return null;
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private class DomainSettingsCache extends AbstractPassiveExpiringMap<String, String> {
		
		public DomainSettingsCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit, true);
		}
		
		@Override
		protected String internalGetValue(String key) {
			CId cid = new CId(key, 3);
			Connection con = null;
			
			try {
				logger.trace("[DomainSettingsCache] Reading DomainSetting from DB... [{}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2));
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doDomainSettingGet(con, cid.getToken(0), cid.getToken(1), cid.getToken(2));
				
			} catch(Throwable t) {
				logger.error("[DomainSettingsCache] Unable to read DomainSetting [{}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2), t);
				return null;
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private class UserSettingsCache extends AbstractPassiveExpiringMap<String, String> {
		
		public UserSettingsCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit, true);
		}
		
		@Override
		protected String internalGetValue(String key) {
			CId cid = new CId(key, 4);
			Connection con = null;
			
			try {
				logger.trace("[UserSettingsCache] Reading UserSetting from DB... [{}, {}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2), cid.getToken(3));
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doUserSettingGet(con, cid.getToken(0), cid.getToken(1), cid.getToken(2), cid.getToken(3));
				
			} catch(Throwable t) {
				logger.error("[UserSettingsCache] Unable to read UserSetting [{}, {}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2), cid.getToken(3), t);
				return null;
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
}
