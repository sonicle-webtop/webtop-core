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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.CId;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingReader;
import com.sonicle.webtop.core.bol.ODomainSetting;
import com.sonicle.webtop.core.bol.OSetting;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUserSetting;
import com.sonicle.webtop.core.bol.VDomainSetting;
import com.sonicle.webtop.core.bol.VSetting;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DomainSettingDAO;
import com.sonicle.webtop.core.dal.SettingDAO;
import com.sonicle.webtop.core.dal.SettingDbDAO;
import com.sonicle.webtop.core.dal.UserSettingDAO;
import com.sonicle.webtop.core.model.SettingEntry;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.interfaces.IServiceSettingManager;
import com.sonicle.webtop.core.sdk.interfaces.ISettingManager;
import com.sonicle.webtop.core.sdk.interfaces.IUserSettingManager;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public final class SettingsManager implements IServiceSettingReader, IServiceSettingManager, IUserSettingManager, ISettingManager {
	private static final Logger LOGGER = WT.getLogger(SettingsManager.class);
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
		LOGGER.info("Initialized");
		return setm;
	}
	
	public static String[] asArray(String value) {
		return StringUtils.split(value, ",");
	}
	
	private WebTopApp wta = null;
	private boolean cacheSettings = true;
	private boolean cacheUserSettings = true;
	private final LoadingCache<String, Optional<String>> settingsCache = Caffeine.newBuilder().build(new SettingsCacheLoader());
	private final LoadingCache<String, Optional<String>> domainSettingsCache = Caffeine.newBuilder().build(new DomainSettingsCacheLoader());
	private final LoadingCache<String, Optional<String>> userSettingsCache = Caffeine.newBuilder().build(new UserSettingsCacheLoader());
	
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
		cacheSettings = false;
		cacheUserSettings = false;
		settingsCache.cleanUp();
		domainSettingsCache.cleanUp();
		userSettingsCache.cleanUp();
		LOGGER.info("Cleaned up");
	}
	
	/**
	 * Empties the whole Settings cache.
	 */
	public void clearSettingsCache() {
		LOGGER.trace("[SettingsCache] Cleaning-up...");
		settingsCache.cleanUp();
		LOGGER.trace("[SettingsCache] Clean-up done");
	}
	
	/**
	 * Empties the whole DomainSettings cache.
	 */
	public void clearDomainSettingsCache() {
		clearDomainSettingsCache(null);
	}
	
	/**
	 * Empties the DomainSettings cache by domain ID.
	 * @param domainId The domain ID whose keys will be cleared.
	 */
	public void clearDomainSettingsCache(String domainId) {
		if (domainId == null) {
			LOGGER.trace("[DomainSettingsCache] Cleaning-up... [*]");
			domainSettingsCache.cleanUp();
			LOGGER.trace("[DomainSettingsCache] Clean-up done");
			
		} else {
			String keyStartsWith = domainId + "|";
			LOGGER.trace("[DomainSettingsCache] Cleaning-up... [{}*]", keyStartsWith);
			List<String> keysToRemove = domainSettingsCache.asMap().keySet().stream()
					.filter(key -> StringUtils.startsWith(key, keyStartsWith))
					.collect(Collectors.toList());
			LOGGER.trace("[DomainSettingsCache] Removing {} keys [{}*]", keysToRemove.size(), keyStartsWith);
			if (!keysToRemove.isEmpty()) domainSettingsCache.invalidateAll(keysToRemove);
			LOGGER.trace("[DomainSettingsCache] Clean-up done [{}*]", keyStartsWith);
		}
	}
	
	/**
	 * Empties the whole UserSettings cache.
	 */
	public void clearUserSettingsCache() {
		clearUserSettingsCache(null, null);
	}
	
	/**
	 * Empties UserSettings cache by domain ID and user ID.
	 * @param domainId The domain ID whose keys will be cleared.
	 * @param userId The user ID whose keys will be cleared. If not null the above domain ID needs a valid value.
	 */
	public void clearUserSettingsCache(String domainId, String userId) {
		if (domainId == null && userId == null) {
			LOGGER.trace("[UserSettingsCache] Cleaning-up... [*]");
			userSettingsCache.cleanUp();
			LOGGER.trace("[UserSettingsCache] Clean-up done [*]");
			
		} else if (domainId != null) {
			String keyStartsWith = (userId != null) ? (domainId + "|" + userId + "|") : (domainId + "|");
			LOGGER.trace("[UserSettingsCache] Cleaning-up... [{}*]", keyStartsWith);
			List<String> keysToRemove = userSettingsCache.asMap().keySet().stream()
					.filter(key -> StringUtils.startsWith(key, keyStartsWith))
					.collect(Collectors.toList());
			LOGGER.trace("[UserSettingsCache] Removing {} keys [{}*]", keysToRemove.size(), keyStartsWith);
			if (!keysToRemove.isEmpty()) userSettingsCache.invalidateAll(keysToRemove);
			LOGGER.trace("[UserSettingsCache] Clean-up done [{}*]", keyStartsWith);
		}
		if (LOGGER.isTraceEnabled()) LOGGER.trace("[UserSettingsCache] Clean-up done");
	}
	
	/**
	 * Prints Cache statistic info into log.
	 */
	public void dumpCacheStats() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[SettingsCache] keys: {}", settingsCache.estimatedSize());
			LOGGER.debug("[DomainSettingsCache] keys: {}", domainSettingsCache.estimatedSize());
			LOGGER.debug("[UserSettingsCache] keys: {}", userSettingsCache.estimatedSize());
		}
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
	public String getUserSetting(final String domainId, final String userId, final String serviceId, final String key, boolean priorityPath) {
		String value = getSetting(domainId, userId, serviceId, key);
		if (value != null || !priorityPath) return value;
		return getServiceSetting(domainId, serviceId, key);
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
	public String getUserSetting(final String domainId, final String userId, final String serviceId, final String key) {
		return getUserSetting(domainId, userId, serviceId, key, true);
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
	public String getUserSetting(final UserProfileId profileId, final String serviceId, final String key) {
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
	public List<OUserSetting> getUserSettings(final UserProfileId profileId, final String serviceId, final String key) {
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
	public List<OUserSetting> getUserSettings(final String domainId, final String userId, final String serviceId, final String key) {
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
		UserSettingDAO setDao = UserSettingDAO.getInstance();
		Connection con = null;
		
		String svalue = valueToString(value);
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ArrayList<UserProfileId> profiles = new ArrayList<>();
			for (OUserSetting set : setDao.selectByServiceKeyValue(con, serviceId, key, svalue)) {
				profiles.add(new UserProfileId(set.getDomainId(), set.getUserId()));
			}
			return profiles;
			
		} catch (Throwable t) {
			LOGGER.debug("Unable to get UserSetting [*, *, {}, {} having {}]", serviceId, key, svalue, t);
			throw new RuntimeException(t);
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
		if (value != null) {
			return setSetting(domainId, userId, serviceId, key, value);
		} else {
			deleteSetting(domainId, userId, serviceId, key);
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
	public boolean deleteUserSetting(final String domainId, final String userId, final String serviceId, final String key) {
		return deleteSetting(domainId, userId, serviceId, key);
	}
	
	/**
	 * Deletes any setting belonging to the specified profile.
	 * @param profileId The user profile ID.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteUserSettings(final UserProfileId profileId) {
		return deleteUserSettings(profileId.getDomainId(), profileId.getUserId());
	}
	
	/**
	 * Deletes any setting belonging to the specified user.
	 * @param domainId The domain ID.
	 * @param userId The user ID.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteUserSettings(final String domainId, final String userId) {
		UserSettingDAO setDao = UserSettingDAO.getInstance();
		Connection con = null;
		boolean ret = false;
		
		try {
			LOGGER.trace("Deleting UserSetting... [{}, {}, *, *]", domainId, userId);
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ret = setDao.deleteByDomainUser(con, domainId, userId) > 0;

		} catch(Throwable t) {
			LOGGER.error("Unable to delete UserSetting [{}, {}, *, *]", domainId, userId, t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (ret && cacheUserSettings) {
			clearUserSettingsCache(domainId, userId);
		}
		return ret;
	}
	
	public List<SettingEntry> listSettings(final boolean includeHidden) {
		SettingDAO setDao = SettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ArrayList<SettingEntry> items = new ArrayList<>();
			for (VSetting vset : setDao.view(con, includeHidden)) {
				 items.add(new SettingEntry(vset.getServiceId(), vset.getKey(), vset.getValue(), vset.getType(), vset.getHelp()));
			}
			return items;
			
		} catch (Throwable t) {
			LOGGER.debug("Unable to read settings", t);
			throw new RuntimeException(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<SettingEntry> listSettings(final String domainId, final boolean includeHidden) {
		Check.notEmpty(domainId, "domainId");
		DomainSettingDAO dsetDao = DomainSettingDAO.getInstance();
		Connection con = null;
		
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ArrayList<SettingEntry> items = new ArrayList<>();
			for (VDomainSetting vdset : dsetDao.viewByDomain(con, domainId, includeHidden)) {
				 items.add(new SettingEntry(vdset.getServiceId(), vdset.getKey(), vdset.getValue(), vdset.getType(), vdset.getHelp()));
			}
			return items;
			
		} catch (Throwable t) {
			LOGGER.debug("Unable to read settings", t);
			throw new RuntimeException(t);
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

		} catch (Throwable t) {
			LOGGER.error("Unable to read setting info", t);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private String getSetting(String serviceId, String key) {
		if (cacheSettings) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[SettingsCache] Looking-up... [{}, {}]", serviceId, key);
			return settingsCache.get(serviceId + "|" + key).orElse(null);
			
		} else {
			Connection con = null;
			
			try {
				if (LOGGER.isTraceEnabled()) LOGGER.trace("Looking-up Setting... [{}, {}]", serviceId, key);
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doSettingGet(con, serviceId, key);
				
			} catch(Throwable t) {
				LOGGER.debug("Unable to get Setting [{}, {}]", serviceId, key, t);
				throw new RuntimeException(t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private String getSetting(String domainId, String serviceId, String key) {
		if (cacheSettings) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[DomainSettingsCache] Looking-up... [{}, {}, {}]", domainId, serviceId, key);
			return domainSettingsCache.get(domainId + "|" + serviceId + "|" + key).orElse(null);
			
		} else {
			Connection con = null;
			
			try {
				if (LOGGER.isTraceEnabled()) LOGGER.trace("Looking-up DomainSetting... [{}, {}, {}]", domainId, serviceId, key);
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doDomainSettingGet(con, domainId, serviceId, key);
				
			} catch(Throwable t) {
				LOGGER.debug("Unable to get DomainSetting [{}, {}, {}]", domainId, serviceId, key, t);
				throw new RuntimeException(t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private String getSetting(String domainId, String userId, String serviceId, String key) {
		if (cacheUserSettings) {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("[UserSettingsCache] Looking-up... [{}, {}, {}, {}]", domainId, userId, serviceId, key);
			return userSettingsCache.get(domainId + "|" + userId + "|" + serviceId + "|" + key).orElse(null);
			
		} else {
			Connection con = null;
			
			try {
				if (LOGGER.isTraceEnabled()) LOGGER.trace("Looking-up UserSetting... [{}, {}, {}, {}]", domainId, userId, serviceId, key);
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return doUserSettingGet(con, domainId, userId, serviceId, key);
				
			} catch(Throwable t) {
				LOGGER.debug("Unable to get UserSetting [{}, {}, {}, {}]", domainId, userId, serviceId, key, t);
				throw new RuntimeException(t);
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private boolean setSetting(String serviceId, String key, Object value) {
		Connection con = null;
		
		boolean ret = false;
		String svalue = valueToString(value);
		try {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Updating Setting... [{}, {}]", serviceId, key);
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ret = doSettingUpsert(con, serviceId, key, svalue);
			
		} catch(Throwable t) {
			LOGGER.debug("Unable to set Setting [{}, {}]", serviceId, key, t);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (ret && cacheSettings) {
			LOGGER.trace("[SettingsCache] Updating... [{}, {}]", serviceId, key);
			settingsCache.put(serviceId + "|" + key, Optional.ofNullable(svalue));
		}
		return ret;
	}
	
	private boolean setSetting(String domainId, String serviceId, String key, Object value) {
		Connection con = null;
		
		boolean ret = false;
		String svalue = valueToString(value);
		try {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Updating DomainSetting... [{}, {}, {}]", domainId, serviceId, key);
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ret = doDomainSettingUpsert(con, domainId, serviceId, key, svalue);
			
		} catch(Throwable t) {
			LOGGER.debug("Unable to set DomainSetting [{}, {}, {}]", domainId, serviceId, key, t);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (ret && cacheSettings) {
			LOGGER.trace("[DomainSettingsCache] Updating... [{}, {}, {}]", domainId, serviceId, key);
			domainSettingsCache.put(domainId + "|" + serviceId + "|" + key, Optional.ofNullable(svalue));
		}
		return ret;
	}
	
	private boolean setSetting(String domainId, String userId, String serviceId, String key, Object value) {
		Connection con = null;
		
		boolean ret = false;
		String svalue = valueToString(value);
		try {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Updating UserSetting... [{}, {}, {}, {}]", domainId, userId, serviceId, key);
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ret = doUserSettingUpsert(con, domainId, userId, serviceId, key, svalue);
		
		} catch(Throwable t) {
			LOGGER.debug("Unable to set UserSetting [{}, {}, {}, {}]", domainId, userId, serviceId, key, t);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (ret && cacheUserSettings) {
			LOGGER.trace("[UserSettingsCache] Updating... [{}, {}, {}, {}]", domainId, userId, serviceId, key);
			userSettingsCache.put(domainId + "|" + userId + "|" + serviceId + "|" + key, Optional.ofNullable(svalue));
		}
		return ret;
	}
	
	private boolean deleteSetting(String serviceId, String key) {
		Connection con = null;
		
		boolean ret = false;
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ret = doSettingDelete(con, serviceId, key) > 0;

		} catch(Throwable t) {
			LOGGER.debug("Unable to delete Setting [{}, {}]", serviceId, key, t);
			throw new RuntimeException(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (ret && cacheSettings) {
			settingsCache.invalidate(serviceId + "|" + key);
		}
		return ret;
	}
	
	private boolean deleteSetting(String domainId, String serviceId, String key) {
		Connection con = null;
		
		boolean ret = false;
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ret = doDomainSettingDelete(con, domainId, serviceId, key) > 0;

		} catch(Throwable t) {
			LOGGER.debug("Unable to delete DomainSetting [{}, {}, {}]", domainId, serviceId, key, t);
			throw new RuntimeException(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (ret && cacheSettings) {
			domainSettingsCache.invalidate(domainId + "|" + serviceId + "|" + key);
		}
		return ret;
	}
	
	private boolean deleteSetting(String domainId, String userId, String serviceId, String key) {
		Connection con = null;
		
		boolean ret = false;
		try {
			con = wta.getConnectionManager().getConnection(CoreManifest.ID);
			ret = doUserSettingDelete(con, domainId, userId, serviceId, key) > 0;

		} catch(Throwable t) {
			LOGGER.debug("Unable to delete UserSetting [{}, {}, {}, {}]", domainId, userId, serviceId, key, t);
			throw new RuntimeException(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		
		if (ret && cacheUserSettings) {
			userSettingsCache.invalidate(domainId + "|" + userId + "|" + serviceId + "|" + key);
		}
		return ret;
	}
	
	private String valueToString(Object value) {
		return (value == null) ? null : String.valueOf(value);
	}
	
	private String doSettingGet(Connection con, String serviceId, String key) throws DAOException {
		SettingDAO setDao = SettingDAO.getInstance();
		
		OSetting item = setDao.selectByServiceKey(con, serviceId, key);
		return (item != null) ? StringUtils.defaultString(item.getValue()) : null;
	}
	
	private boolean doSettingUpsert(Connection con, String serviceId, String key, String value) throws DAOException {
		SettingDAO setDao = SettingDAO.getInstance();
		
		OSetting item = new OSetting();
		item.setServiceId(serviceId);
		item.setKey(key);
		item.setValue(value);
		
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
	
	private boolean doDomainSettingUpsert(Connection con, String domainId, String serviceId, String key, String value) throws DAOException {
		DomainSettingDAO setDao = DomainSettingDAO.getInstance();
		
		ODomainSetting item = new ODomainSetting();
		item.setDomainId(domainId);
		item.setServiceId(serviceId);
		item.setKey(key);
		item.setValue(value);
		
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
	
	private boolean doUserSettingUpsert(Connection con, String domainId, String userId, String serviceId, String key, String value) throws DAOException {
		UserSettingDAO setDao = UserSettingDAO.getInstance();
		
		OUserSetting item = new OUserSetting();
		item.setDomainId(domainId);
		item.setUserId(userId);
		item.setServiceId(serviceId);
		item.setKey(key);
		item.setValue(value);
		
		int ret = setDao.update(con, item);
		if (ret == 0) ret = setDao.insert(con, item);
		return ret > 0;
	}
	
	private int doUserSettingDelete(Connection con, String domainId, String userId, String serviceId, String key) throws DAOException {
		UserSettingDAO setDao = UserSettingDAO.getInstance();
		return setDao.deleteByDomainServiceUserKey(con, domainId, userId, serviceId, key);
	}
	
	private class SettingsCacheLoader implements CacheLoader<String, Optional<String>> {

		@Override
		public Optional<String> load(String k) throws Exception {
			CId cid = new CId(k, 2);
			Connection con = null;
			
			try {
				LOGGER.trace("[SettingsCache] Loading... [{}, {}]", cid.getToken(0), cid.getToken(1));
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return Optional.ofNullable(doSettingGet(con, cid.getToken(0), cid.getToken(1)));
				
			} catch(Throwable t) {
				LOGGER.error("[SettingsCache] Unable to load [{}, {}]", cid.getToken(0), cid.getToken(1), t);
				return null;
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private class DomainSettingsCacheLoader implements CacheLoader<String, Optional<String>> {
		
		@Override
		public Optional<String> load(String k) throws Exception {
			CId cid = new CId(k, 3);
			Connection con = null;
			
			try {
				LOGGER.trace("[DomainSettingsCache] Loading... [{}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2));
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return Optional.ofNullable(doDomainSettingGet(con, cid.getToken(0), cid.getToken(1), cid.getToken(2)));
				
			} catch(Throwable t) {
				LOGGER.error("[DomainSettingsCache] Unable to load [{}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2), t);
				return null;
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
	
	private class UserSettingsCacheLoader implements CacheLoader<String, Optional<String>> {
		
		@Override
		public Optional<String> load(String k) throws Exception {
			CId cid = new CId(k, 4);
			Connection con = null;
			
			try {
				LOGGER.trace("[UserSettingsCache] Loading... [{}, {}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2), cid.getToken(3));
				con = wta.getConnectionManager().getConnection(CoreManifest.ID);
				return Optional.ofNullable(doUserSettingGet(con, cid.getToken(0), cid.getToken(1), cid.getToken(2), cid.getToken(3)));
				
			} catch(Throwable t) {
				LOGGER.error("[UserSettingsCache] Unable to load [{}, {}, {}, {}]", cid.getToken(0), cid.getToken(1), cid.getToken(2), cid.getToken(3), t);
				return null;
			} finally {
				DbUtils.closeQuietly(con);
			}
		}
	}
}
