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
package com.sonicle.webtop.core.admin;

import com.sonicle.commons.beans.PageInfo;
import com.sonicle.commons.beans.SortInfo;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.qbuilders.conditions.Condition;
import com.sonicle.commons.time.DateTimeRange;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.DataSourcesManager;
import com.sonicle.webtop.core.app.LicenseManager;
import com.sonicle.webtop.core.app.LogbackPropertyDefiner;
import com.sonicle.webtop.core.app.ProductRegistry;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.io.dbutils.FilterableArrayListHandler;
import com.sonicle.webtop.core.app.io.dbutils.RowsAndCols;
import com.sonicle.webtop.core.app.model.ApiKey;
import com.sonicle.webtop.core.app.model.ApiKeyBase;
import com.sonicle.webtop.core.app.model.ApiKeyNew;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.app.model.DomainGetOption;
import com.sonicle.webtop.core.app.model.DomainUpdateOption;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.sdk.ResultVoid;
import com.sonicle.webtop.core.app.sdk.WTConnectionException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.app.util.LogbackHelper;
import com.sonicle.webtop.core.bol.VDomainAccessLog;
import com.sonicle.webtop.core.bol.ODomainAccessLogDetail;
import com.sonicle.webtop.core.config.bol.OPecBridgeFetcher;
import com.sonicle.webtop.core.config.bol.OPecBridgeRelay;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUpgradeStatement;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.config.dal.PecBridgeFetcherDAO;
import com.sonicle.webtop.core.config.dal.PecBridgeRelayDAO;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DomainAccessLogDAO;
import com.sonicle.webtop.core.dal.DomainAccessLogPredicateVisitor;
import com.sonicle.webtop.core.dal.UpgradeStatementDAO;
import com.sonicle.webtop.core.model.DataSource;
import com.sonicle.webtop.core.model.DataSourceBase;
import com.sonicle.webtop.core.model.DataSourcePooled;
import com.sonicle.webtop.core.model.DataSourceQuery;
import com.sonicle.webtop.core.model.DataSourceQueryBase;
import com.sonicle.webtop.core.model.DataSourceType;
import com.sonicle.webtop.core.model.DomainAccessLog;
import com.sonicle.webtop.core.model.DomainAccessLogDetail;
import com.sonicle.webtop.core.model.DomainAccessLogQuery;
import com.sonicle.webtop.core.model.ListDomainAccessLogDetailResult;
import com.sonicle.webtop.core.model.ListDomainAccessLogResult;
import com.sonicle.webtop.core.model.LoggerEntry;
import com.sonicle.webtop.core.model.PublicImage;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceBase;
import com.sonicle.webtop.core.app.model.GenericSubject;
import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.GroupBase;
import com.sonicle.webtop.core.app.model.GroupGetOption;
import com.sonicle.webtop.core.app.model.GroupUpdateOption;
import com.sonicle.webtop.core.app.model.LicenseBase;
import com.sonicle.webtop.core.app.model.LicenseComputedStatus;
import com.sonicle.webtop.core.app.model.LicenseListOption;
import com.sonicle.webtop.core.app.model.PlatformUser;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.app.model.ResourceUpdateOption;
import com.sonicle.webtop.core.app.model.Role;
import com.sonicle.webtop.core.app.model.RoleBase;
import com.sonicle.webtop.core.app.model.RoleGetOption;
import com.sonicle.webtop.core.app.model.RoleUpdateOption;
import com.sonicle.webtop.core.app.model.SubjectGetOption;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserBase;
import com.sonicle.webtop.core.app.model.UserGetOption;
import com.sonicle.webtop.core.app.model.UserUpdateOption;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.SettingEntry;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.qualitycheck.Check;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreAdminManager extends BaseManager {
	private static final Logger logger = WT.getLogger(CoreAdminManager.class);
	private WebTopApp wta = null;
	
	public CoreAdminManager(WebTopApp wta, boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		this.wta = wta;
	}
	
	public void updateSysAdminPassword(final char[] newPassword) throws WTException {
		RunContext.ensureIsSysAdmin();
		wta.getWebTopManager().updateSysAdminPassword(newPassword);
	}
	
	public boolean isOnlineSession(String sessionId) {
		return wta.getSessionManager().isOnlineQuietly(sessionId);
	}
	
	public OSettingDb getSettingInfo(String serviceId, String key) {
		SettingsManager setm = wta.getSettingsManager();
		return setm.getSettingInfo(serviceId, key);
	}
	
	/**
	 * Lists all System settings.
	 * @param includeHidden Set to `true` also return hidden settings.
	 * @return List of settings
	 */
	public List<SettingEntry> listSystemSettings(boolean includeHidden) {
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setMgr = wta.getSettingsManager();
		return setMgr.listSettings(includeHidden);
	}
	
	/**
	 * Gets a System setting for specific service.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The value
	 */
	public String getSystemSetting(final String serviceId, final String key) {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(key, "key");
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setMgr = wta.getSettingsManager();
		return setMgr.getServiceSetting(serviceId, key);
	}
	
	/**
	 * Updates (or inserts) a System setting for a specific service.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	public boolean updateSystemSetting(final String serviceId, final String key, final Object value) {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(key, "key");
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setMgr = wta.getSettingsManager();
		return setMgr.setServiceSetting(serviceId, key, value);
	}
	
	/**
	 * Clears a System setting for a specific service.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteSystemSetting(String serviceId, String key) {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(key, "key");
		RunContext.ensureIsSysAdmin();
		
		SettingsManager setMgr = wta.getSettingsManager();
		return setMgr.deleteServiceSetting(serviceId, key);
	}
	
	public void cleanupSettingsCache() throws WTException {
		SettingsManager setMgr = wta.getSettingsManager();
		
		RunContext.ensureIsWebTopAdmin();
		setMgr.clearSettingsCache();
		setMgr.dumpCacheStats();
	}
	
	public Map<String, Domain> listDomains(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "scheme");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		Map<String, Domain> domains = wtMgr.listDomains(enabled);
		if (RunContext.isSysAdmin()) {
			return domains;
		} else {
			Map<String, Domain> map = new LinkedHashMap<>();
			for (Domain domain : domains.values()) {
				if (RunContext.isWebTopDomainAdmin(domain.getDomainId())) {
					map.put(domain.getDomainId(), domain);
					break;
				}
			}
			return map;
		}
	}
	
	public boolean checkDomainIdAvailability(final String domainIdToCheck) throws WTException {
		Check.notEmpty(domainIdToCheck, "domainIdToCheck");
		WebTopManager wtMgr = wta.getWebTopManager();

		RunContext.ensureIsSysAdmin();
		return wtMgr.checkDomainIdAvailability(domainIdToCheck);
	}
	
	public Domain getDomain(final BitFlags<DomainGetOption> options) throws WTException {
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.getDomain(targetPid.getDomainId(), options);
	}
	
	public DomainBase.PasswordPolicies getDomainPasswordPolicies() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.getDomainPasswordPolicies(targetPid.getDomainId());
	}
	
	public Result<Domain> addDomain(final String domainId, final DomainBase domain) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(domain, "domain");
		WebTopManager wtMgr = wta.getWebTopManager();

		RunContext.ensureIsSysAdmin();
		return wtMgr.addDomain(domainId, domain);
	}
	
	public boolean existsDomain() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.existsDomainId(targetPid.getDomainId());
	}
	
	public Result<Domain> updateDomain(final DomainBase domain, final BitFlags<DomainUpdateOption> options) throws WTException {
		Check.notNull(domain, "domain");
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.updateDomain(targetPid.getDomainId(), domain, options);
	}
	
	public ResultVoid deleteDomain(final boolean deep) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();

		RunContext.ensureIsSysAdmin();
		return wtMgr.deleteDomain(domainId, deep);
	}
	
	public ResultVoid initDomain() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();

		RunContext.ensureIsSysAdmin();
		return wtMgr.initDomain(domainId);
	}
	
	@Deprecated
	public void refreshDomainCache() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		RunContext.ensureIsWebTopAdmin();
		wtMgr.initDomainCache();
	}
	
	/**
	 * Lists all Domain (platform) settings.
	 * @param includeHidden Set to `true` also return hidden settings.
	 * @return List of settings
	 */
	public List<SettingEntry> listDomainSettings(final boolean includeHidden) {
		SettingsManager setMgr = wta.getSettingsManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return setMgr.listSettings(targetPid.getDomainId(), includeHidden);
	}
	
	/**
	 * Gets a Domain (platform) setting for specific service.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return The value
	 */
	public String getDomainSetting(final String serviceId, final String key) {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(key, "key");
		SettingsManager setMgr = wta.getSettingsManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return setMgr.getServiceSetting(targetPid.getDomainId(), serviceId, key);
	}
	
	/**
	 * Updates (or inserts) a Domain (platform) setting for a specific service.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @param value The value to set.
	 * @return True if setting was succesfully written, otherwise false.
	 */
	public boolean updateDomainSetting(final String serviceId, final String key, Object value) {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(key, "key");
		SettingsManager setMgr = wta.getSettingsManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		boolean ret = setMgr.setServiceSetting(targetPid.getDomainId(), serviceId, key, value);
		if (ret && CoreManifest.ID.equals(serviceId) && "public.url".equals(key)) {
			WebTopManager wtMgr = wta.getWebTopManager();
			wtMgr.initDomainCache();
		}
		return ret;
	}
	
	/**
	 * Clears a Domain (platform) setting for a specific service.
	 * @param serviceId The service ID.
	 * @param key The name of the setting.
	 * @return True if setting was succesfully deleted, otherwise false.
	 */
	public boolean deleteDomainSetting(final String serviceId, final String key) {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(key, "key");
		SettingsManager setMgr = wta.getSettingsManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		boolean ret = setMgr.deleteServiceSetting(targetPid.getDomainId(), serviceId, key);
		if (ret && CoreManifest.ID.equals(serviceId) && "public.url".equals(key)) {
			WebTopManager wtMgr = wta.getWebTopManager();
			wtMgr.initDomainCache();
		}
		return ret;
	}
	
	public void cleanupDomainSettingsCache() throws WTException {
		SettingsManager setMgr = wta.getSettingsManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		setMgr.clearDomainSettingsCache(targetPid.getDomainId());
		setMgr.dumpCacheStats();
	}
	
	public void cleanupUserSettingsCache() throws WTException {
		cleanupUserSettingsCache(null);
	}
	
	public void cleanupUserSettingsCache(final String userId) throws WTException {
		SettingsManager setMgr = wta.getSettingsManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		setMgr.clearUserSettingsCache(targetPid.getDomainId(), userId);
		setMgr.dumpCacheStats();
	}
	
	public List<PublicImage> listDomainPublicImages(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		RunContext.ensureWebTopDomainAdmin(domainId);
		
		try {
			return wtmgr.listDomainPublicImages(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list domain's public images [{0}]", domainId);
		}
	}
	
	public Map<String, Group> listGroups() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.listGroups(targetPid.getDomainId());
	}
	
	public boolean checkGroupIdAvailability(final String groupIdToCheck) throws WTException {
		Check.notEmpty(groupIdToCheck, "groupIdToCheck");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.checkGroupIdAvailability(targetPid.getDomainId(), groupIdToCheck);
	}
	
	public Group getGroup(final String groupId, final BitFlags<GroupGetOption> options) throws WTException {
		Check.notEmpty(groupId, "groupId");
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.getGroup(targetPid.getDomainId(), groupId, options);
	}
	
	public Result<Group> addGroup(final String groupId, final GroupBase group, final BitFlags<GroupUpdateOption> options) throws WTException {
		Check.notEmpty(groupId, "groupId");
		Check.notNull(group, "group");
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.addGroup(targetPid.getDomainId(), groupId, group, options);
	}
	
	public ResultVoid updateGroup(final String groupId, final GroupBase group, final BitFlags<GroupUpdateOption> options) throws WTException {
		Check.notEmpty(groupId, "groupId");
		Check.notNull(group, "group");
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.updateGroup(targetPid.getDomainId(), groupId, group, options);
	}
	
	public ResultVoid deleteGroup(final String groupId) throws WTException {
		Check.notEmpty(groupId, "groupId");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.deleteGroup(targetPid.getDomainId(), groupId);
	}
	
	public Map<String, PlatformUser> listPlatformUsers(final WebTopManager.PlatformUsersPerspective perspective) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.listPlatformUsers(targetPid.getDomainId(), perspective);
	}
	
	public Map<String, User> listUsers(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.listUsers(targetPid.getDomainId(), enabled);
	}
	
	public boolean checkUserIdAvailability(final String userIdToCheck) throws WTException {
		Check.notEmpty(userIdToCheck, "userIdToCheck");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.checkUserIdAvailability(targetPid.getDomainId(), userIdToCheck);
	}
	
	public User getUser(final String userId, final BitFlags<UserGetOption> options) throws WTException {
		Check.notEmpty(userId, "userId");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.getUser(targetPid.getDomainId(), userId, options);
	}
	
	public Result<User> addUser(final String userId, final UserBase user, final boolean setPassword, final char[] password, final BitFlags<UserUpdateOption> options) throws WTException {
		Check.notEmpty(userId, "userId");
		Check.notNull(user, "user");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.addUser(targetPid.getDomainId(), userId, user, true, setPassword, password, options);
	}
	
	public ResultVoid updateUser(final String userId, final UserBase user, final BitFlags<UserUpdateOption> options) throws WTException {
		Check.notEmpty(userId, "userId");
		Check.notNull(user, "user");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.updateUser(targetPid.getDomainId(), userId, user, options);
	}
	
	public void updateUserPassword(final String userId, final char[] newPassword, final boolean forceChangeUponLogin) throws WTException {
		Check.notEmpty(userId, "userId");
		Check.notNull(newPassword, "newPassword");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		wtMgr.updateUserPassword(targetPid.getDomainId(), userId, null, newPassword, forceChangeUponLogin);
	}
	
	public void updateUserStatus(final String userId, final boolean enabled) throws WTException {
		Check.notEmpty(userId, "userId");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		wtMgr.updateUserStatus(targetPid.getDomainId(), userId, enabled);
	}
	
	public void bulkUpdatePersonalEmailDomain(final Set<String> userIds, final String newDomainPart) throws WTException {
		Check.notEmpty(userIds, "userIds");
		Check.notEmpty(newDomainPart, "newDomainPart");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		wtMgr.bulkUpdatePersonalEmailDomain(targetPid.getDomainId(), userIds, newDomainPart);
	}
	
	public ResultVoid deleteUser(final String userId, final boolean deep) throws WTException {
		Check.notEmpty(userId, "userId");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.deleteUser(targetPid.getDomainId(), userId, deep);
	}
	
	public Map<String, Resource> listResources(final EnabledCond enabled) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.listResources(targetPid.getDomainId(), enabled);
	}
	
	public boolean checkResourceIdAvailability(final String resourceIdToCheck) throws WTException {
		Check.notEmpty(resourceIdToCheck, "resourceIdToCheck");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.checkResourceIdAvailability(targetPid.getDomainId(), resourceIdToCheck);
	}
	
	public Resource getResource(final String resourceId, final BitFlags<ResourceGetOption> options) throws WTException {
		Check.notEmpty(resourceId, "resourceId");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.getResource(targetPid.getDomainId(), resourceId, options);
	}
	
	public Result<Resource> addResource(final String resourceId, final ResourceBase resource, final BitFlags<ResourceUpdateOption> options) throws WTException {
		Check.notEmpty(resourceId, "resourceId");
		Check.notNull(resource, "resource");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.addResource(targetPid.getDomainId(), resourceId, resource, options);
	}
	
	public ResultVoid updateResource(final String resourceId, final ResourceBase resource, final BitFlags<ResourceUpdateOption> options) throws WTException {
		Check.notEmpty(resourceId, "resourceId");
		Check.notNull(resource, "resource");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.updateResource(targetPid.getDomainId(), resourceId, resource, options);
	}
	
	public ResultVoid deleteResource(final String resourceId) throws WTException {
		Check.notEmpty(resourceId, "resourceId");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.deleteResource(targetPid.getDomainId(), resourceId);
	}
	
	public Map<String, Role> listRoles() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.listRoles(targetPid.getDomainId());
	}
	
	public boolean checkRoleIdAvailability(final String roleIdToCheck) throws WTException {
		Check.notEmpty(roleIdToCheck, "roleIdToCheck");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.checkRoleIdAvailability(targetPid.getDomainId(), roleIdToCheck);
	}
	
	public Role getRole(final String roleId, final BitFlags<RoleGetOption> options) throws WTException {
		Check.notEmpty(roleId, "roleId");
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.getRole(targetPid.getDomainId(), roleId, options);
	}
	
	public Result<Role> addRole(final String roleId, final RoleBase role, final BitFlags<RoleUpdateOption> options) throws WTException {
		Check.notEmpty(roleId, "roleId");
		Check.notNull(role, "role");
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.addRole(targetPid.getDomainId(), roleId, role, options);
	}
	
	public ResultVoid updateRole(final String roleId, final RoleBase role, final BitFlags<RoleUpdateOption> options) throws WTException {
		Check.notEmpty(roleId, "roleId");
		Check.notNull(role, "role");
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.updateRole(targetPid.getDomainId(), roleId, role, options);
	}
	
	public ResultVoid deleteRole(final String roleId) throws WTException {
		Check.notEmpty(roleId, "roleId");
		WebTopManager wtMgr = wta.getWebTopManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.deleteRole(targetPid.getDomainId(), roleId);
	}
	
	public Map<String, GenericSubject> listSubjects(final boolean users, final boolean resources, final boolean groups, final boolean roles, final boolean useProfileIdAsKey) throws WTException {
		final UserProfileId targetPid = ensureProfileDomain(RunContext.AdminScope.DOMAINADMIN);
		BitFlags<SubjectGetOption> options = new BitFlags<>(SubjectGetOption.class);
		if (users) options.set(SubjectGetOption.USERS);
		if (resources) options.set(SubjectGetOption.RESOURCES);
		if (groups) options.set(SubjectGetOption.GROUPS);
		if (roles) options.set(SubjectGetOption.ROLES);
		if (useProfileIdAsKey) options.set(SubjectGetOption.PID_AS_KEY);
		return wta.getWebTopManager().listSubjects(targetPid.getDomainId(), options);
	}
	
	public void cleanupLicenseCache() throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();

		ensureWebTopDomainAdmin();
		licMgr.cleanupLicenseCache();
	}
	
	public void checkOnlineAvailability() throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();

		ensureWebTopDomainAdmin();
		licMgr.checkOnlineAvailability();
	}
	
	public List<ServiceLicense> listLicenses(final BitFlags<LicenseListOption> options) throws WTException {
		Check.notNull(options, "options");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return licMgr.listLicenses(targetPid.getDomainId(), options);
	}
	
	public ServiceLicense getLicense(final String productCode) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return licMgr.getLicense(targetPid.getDomainId(), productCode);
	}
	
	/**
	 * @deprecated use getLicense (without domain) instead
	 */
	@Deprecated public ServiceLicense getLicense(final String domainId, final String productCode) throws WTException {
		Check.notNull(productCode, "productCode");
		Check.notNull(domainId, "domainId");
		LicenseManager licMgr = wta.getLicenseManager();
		
		RunContext.ensureWebTopDomainAdmin(domainId);
		return licMgr.getLicense(domainId, productCode);
	}

	public String computeLicenseActivationHardwareID() throws WTException {
		LicenseManager licMgr = wta.getLicenseManager();
		return licMgr.computeActivationHardwareID();
	}
	
	public BitFlags<LicenseComputedStatus> getLicenseStatus(final String productCode, final String hardwareID) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return licMgr.getLicenseStatus(targetPid.getDomainId(), productCode, hardwareID);
	}
	
	public ProductLicense getProductLicense(final String productCode) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		BaseServiceProduct serviceProduct = ProductRegistry.getInstance().getServiceProduct(productCode, targetPid.getDomainId());
		if (serviceProduct == null) throw new WTNotFoundException("Product not found [{}]", productCode);
		return licMgr.getProductLicense(serviceProduct);
	}
	
	public void addLicense(final String productCode, final LicenseBase license, final boolean autoActivate) throws WTException {
		Check.notEmpty(productCode, "productCode");
		Check.notNull(license, "license");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.addLicense(targetPid.getDomainId(), productCode, license, autoActivate);
	}
	
	public void changeLicense(final String productCode, final String newString, final String activatedString) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.changeLicense(targetPid.getDomainId(), productCode, newString, activatedString, false);
	}
	
	public void modifyLicense(final String productCode, final String modificationKey, final String modifiedString) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.modifyLicense(targetPid.getDomainId(), productCode, modificationKey, modifiedString);
	}
	
	public void updateLicenseAutoLease(final String productCode, final boolean autoLease) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.updateLicenseAutoLease(targetPid.getDomainId(), productCode, autoLease);
	}
	
	public void deleteLicense(final String productCode, final boolean force) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.deleteLicense(targetPid.getDomainId(), productCode, force);
	}
	
	public void activateLicense(final String productCode, String activatedString) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.activateLicense(targetPid.getDomainId(), productCode, activatedString);
	}
	
	public void deactivateLicense(final String productCode, boolean offline) throws WTException {
		Check.notEmpty(productCode, "productCode");
		LicenseManager licMgr = wta.getLicenseManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.deactivateLicense(targetPid.getDomainId(), productCode, offline);
	}
	
	public void assignLicenseLease(final String productCode, final Set<String> userIds) throws WTException {
		Check.notEmpty(productCode, "productCode");
		Check.notEmpty(userIds, "userIds");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.assignLicenseLease(targetPid.getDomainId(), productCode, userIds);
	}
	
	/*
	public void autoAssignLicenseLease(final ProductId productId, final String userId) throws WTException {
		Check.notNull(productId, "productId");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.assignLicenseLease(targetPid.getDomainId(), productId, userId, null);
	}
	*/
	
	public void revokeLicenseLease(final String productCode, final Set<String> userIds) throws WTException {
		Check.notEmpty(productCode, "productCode");
		Check.notEmpty(userIds, "userIds");
		LicenseManager licMgr = wta.getLicenseManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		licMgr.revokeLicenseLease(targetPid.getDomainId(), productCode, userIds);
	}
	
	public Map<String, DataSourceType> listDataSourceTypes() throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.listDataSourceTypes(targetPid.getDomainId());
	}
	
	public Map<String, DataSourcePooled> listDataSources() throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.listDataSources(targetPid.getDomainId());
	}
	
	public boolean checkDataSourceFriendlyIdAvailability(final String friendlyIdToCheck)throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.checkDataSourceFriendlyIdAvailability(targetPid.getDomainId(), friendlyIdToCheck);
	}
	
	public DataSource getDataSource(final String dataSourceId) throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.getDataSource(targetPid.getDomainId(), dataSourceId);
	}
	
	public DataSourcePooled.PoolStatus getDataSourcePoolStatus(final String dataSourceId) throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.getDataSourcePoolStatus(targetPid.getDomainId(), dataSourceId);
	}
	
	public DataSource addDataSource(final DataSourceBase dataSource) throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.addDataSource(targetPid.getDomainId(), dataSource);
	}
	
	public void updateDataSource(final String dataSourceId, final DataSourceBase dataSource, final boolean setPassword) throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		dsMgr.updateDataSource(targetPid.getDomainId(), dataSourceId, dataSource, setPassword);
	}
	
	public void deleteDataSource(final String dataSourceId) throws WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		dsMgr.deleteDataSource(targetPid.getDomainId(), dataSourceId);
	}
	
	public void checkDataSourceConnection(final String dataSourceId) throws WTConnectionException, WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		dsMgr.checkDataSourceConnection(targetPid.getDomainId(), dataSourceId);
	}
	
	public void checkDataSourceConnection(final String dataSourceType, final String serverName, final Integer serverPort, final String databaseName, final String username, final String password, final Map<String, String> props) throws WTConnectionException, WTException {
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		dsMgr.checkDataSourceConnection(targetPid.getDomainId(), dataSourceType, serverName, serverPort, databaseName, username, password, props);
	}
	
	public DataSourceQuery getDataSourceQuery(final String queryId) throws WTException {
		Check.notEmpty(queryId, "queryId");
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.getDataSourceQuery(targetPid.getDomainId(), queryId);
	}
	
	public DataSourceQuery addDataSourceQuery(final String dataSourceId, final DataSourceQueryBase query) throws WTException {
		Check.notEmpty(dataSourceId, "dataSourceId");
		DataSourcesManager dsMgr = wta.getDataSourcesManager();

		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return dsMgr.addDataSourceQuery(targetPid.getDomainId(), dataSourceId, query);
	}
	
	public void updateDataSourceQuery(final String queryId, final DataSourceQueryBase query) throws WTException {
		Check.notEmpty(queryId, "queryId");
		Check.notNull(query, "query");
		DataSourcesManager dsMgr = wta.getDataSourcesManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		dsMgr.updateDataSourceQuery(targetPid.getDomainId(), queryId, query);
	}
	
	public void deleteDataSourceQuery(final String queryId) throws WTException {
		Check.notEmpty(queryId, "queryId");
		DataSourcesManager dsMgr = wta.getDataSourcesManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		dsMgr.deleteDataSourceQuery(targetPid.getDomainId(), queryId);
	}
	
	public DataSourceBase.ExecuteQueryResult<RowsAndCols> executeDataSourceRawQuery(final String dataSourceId, final String rawSql, final Map<String, String> placeholdersValues, final PageInfo pagination, final boolean debugReport) throws WTException {
		return executeDataSourceRawQuery(dataSourceId, rawSql, placeholdersValues, pagination, debugReport, new FilterableArrayListHandler());
	}
	
	public <T> DataSourceBase.ExecuteQueryResult<T> executeDataSourceRawQuery(final String dataSourceId, final String rawSql, final Map<String, String> placeholdersValues, final PageInfo pagination, final boolean debugReport, final ResultSetHandler<T> resultSetHandler) throws WTException {
		Check.notEmpty(dataSourceId, "dataSourceId");
		Check.notNull(resultSetHandler, "resultSetHandler");
		
		try {
			final UserProfileId targetPid = ensureProfileDomain(RunContext.AdminScope.DOMAINADMIN);
			
			DataSourcesManager dsMgr = wta.getDataSourcesManager();
			// Here in admin manager we do not have a real target user, so 
			// currentUserId will be taken from incoming sqlPlaceholders.
			DataSourcesManager.QueryPlaceholders placeholders = new DataSourcesManager.QueryPlaceholders(targetPid.getDomainId(), null, placeholdersValues);
			return dsMgr.executeRawQuery(targetPid.getDomainId(), dataSourceId, rawSql, placeholders, pagination, debugReport, resultSetHandler, null);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	public Map<String, ApiKey> listApiKeys() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.listApiKeys(targetPid.getDomainId());
	}
	
	public ApiKey getApiKey(final String apiKeyId) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.getApiKey(targetPid.getDomainId(), apiKeyId);
	}
	
	public ApiKeyNew createApiKey(final ApiKeyBase apiKey) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		return wtMgr.createApiKey(targetPid.getDomainId(), apiKey);
	}
	
	public void updateApiKeyDetails(final String apiKeyId, final ApiKeyBase apiKey) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		wtMgr.updateApiKeyDetails(targetPid.getDomainId(), apiKeyId, apiKey);
	}
	
	public void deleteApiKey(final String apiKeyId) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		final UserProfileId targetPid = ensureWebTopDomainAdmin();
		wtMgr.deleteApiKey(targetPid.getDomainId(), apiKeyId);
	}
	
	
	
	
	
	/**
	 * Lists configured PecBridge fetchers for the specified domain.
	 * @param domainId The domain ID.
	 * @return The fetcher list.
	 * @throws WTException If something goes wrong.
	 */
	public List<OPecBridgeFetcher> listPecBridgeFetchers(String domainId) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureWebTopDomainAdmin(domainId);
		
		try {
			String internetName = WT.getPrimaryDomainName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.selectByContext(con, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OPecBridgeFetcher getPecBridgeFetcher(int fetcherId) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OPecBridgeFetcher fetcher = dao.select(con, fetcherId);
			if (fetcher != null) {
				UserProfileId pid = new UserProfileId(fetcher.getWebtopProfileId());
				RunContext.ensureWebTopDomainAdmin(pid.getDomainId());
			}
			return fetcher;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addPecBridgeFetcher(OPecBridgeFetcher fetcher) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		UserProfileId pid = new UserProfileId(fetcher.getWebtopProfileId());
		RunContext.ensureWebTopDomainAdmin(pid.getDomainId());
		
		try {
			String internetName = WT.getPrimaryDomainName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data pdata = WT.getProfileData(pid);
			if (pdata == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			con = WT.getConnection(SERVICE_ID, false);
			fetcher.setContext(internetName);
			fetcher.setForwardAddress(pdata.getProfileEmailAddress());
			fetcher.setFetcherId(dao.getSequence(con).intValue());
			dao.insert(con, fetcher);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updatePecBridgeFetcher(OPecBridgeFetcher fetcher) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		UserProfileId pid = new UserProfileId(fetcher.getWebtopProfileId());
		RunContext.ensureWebTopDomainAdmin(pid.getDomainId());
		
		try {
			String internetName = WT.getPrimaryDomainName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data pdata = WT.getProfileData(pid);
			if (pdata == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			con = WT.getConnection(SERVICE_ID, false);
			fetcher.setForwardAddress(pdata.getProfileEmailAddress());
			dao.update(con, fetcher);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deletePecBridgeFetcher(String domainId, int fetcherId) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureWebTopDomainAdmin(domainId);
		
		try {
			String internetName = WT.getPrimaryDomainName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.deleteByIdContext(con, fetcherId, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updatePecBridgeFetcherAuthState(String webtopProfileId, String state) throws WTException {
		PecBridgeFetcherDAO dao = PecBridgeFetcherDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			dao.updateAuthStateByWebtopProfileId(con, webtopProfileId, state);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	/**
	 * Lists configured PecBridge relays for the specified domain.
	 * @param domainId The domain ID.
	 * @return The relay list.
	 * @throws WTException If something goes wrong.
	 */
	public List<OPecBridgeRelay> listPecBridgeRelays(String domainId) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureWebTopDomainAdmin(domainId);
		
		try {
			String internetName = WT.getPrimaryDomainName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.selectByContext(con, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OPecBridgeRelay getPecBridgeRelay(int relayId) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OPecBridgeRelay relay = dao.select(con, relayId);
			if (relay != null) {
				UserProfileId pid = new UserProfileId(relay.getWebtopProfileId());
				RunContext.ensureWebTopDomainAdmin(pid.getDomainId());
			}
			return relay;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addPecBridgeRelay(OPecBridgeRelay relay) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		UserProfileId pid = new UserProfileId(relay.getWebtopProfileId());
		RunContext.ensureWebTopDomainAdmin(pid.getDomainId());
		
		try {
			String internetName = WT.getPrimaryDomainName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data pdata = WT.getProfileData(pid);
			if (pdata == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			//TODO: aggiornare email del profilo?
			//int ret = WebTopDb.updateUserEmail(con, domainId, tokens[0], matcher);
			//if(ret != 1) throw new Exception("User's email not updated");
			
			con = WT.getConnection(SERVICE_ID, false);
			relay.setContext(internetName);
			relay.setRelayId(dao.getSequence(con).intValue());
			relay.setDebug(false);
			dao.insert(con, relay);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updatePecBridgeRelay(OPecBridgeRelay relay) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		UserProfileId pid = new UserProfileId(relay.getWebtopProfileId());
		RunContext.ensureWebTopDomainAdmin(pid.getDomainId());
		
		try {
			String internetName = WT.getPrimaryDomainName(pid.getDomainId());
			if (internetName == null) throw new WTException("Domain not found [{0}]", pid.getDomainId());
			UserProfile.Data pdata = WT.getProfileData(pid);
			if (pdata == null) throw new WTException("User-data not found [{0}]", pid.toString());
			
			//TODO: aggiornare email del profilo?
			//int ret = WebTopDb.updateUserEmail(con, domainId, tokens[0], matcher);
			//if(ret != 1) throw new Exception("User's email not updated");
			
			con = WT.getConnection(SERVICE_ID, false);
			dao.update(con, relay);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deletePecBridgeRelay(String domainId, int relayId) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureWebTopDomainAdmin(domainId);
		
		try {
			String internetName = WT.getPrimaryDomainName(domainId);
			if (internetName == null) throw new WTException();
			
			con = WT.getConnection(SERVICE_ID);
			return dao.deleteByIdContext(con, relayId, internetName);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updatePecBridgeRelayAuthState(String webtopProfileId, String state) throws WTException {
		PecBridgeRelayDAO dao = PecBridgeRelayDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsWebTopAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			dao.updateAuthStateByWebtopProfileId(con, webtopProfileId, state);
			
			DbUtils.commitQuietly(con);
		
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	public InputStream getLogFileContent(final long from, final long count) throws WTException, IOException {
		RunContext.ensureIsSysAdmin();
		
		String logFileBasename = LogbackHelper.getLogFileBasename(wta.getProperties(), WebTopApp.getWebappName());
		if (StringUtils.isBlank(logFileBasename)) throw new WTException("Log file basename not configured");
		String logFilename = logFileBasename + ".log";
		InputStream is = LogbackHelper.getLogFileStream(logFilename, from, count);
		if (is == null) throw new WTException("File '{}' not configured or accessible. Maybe console appender is active.", logFilename);
		return is;
	}
	
	public Map<String, LoggerEntry> listLoggers() throws WTException {
		RunContext.ensureIsSysAdmin();
		
		String etcPath = wta.getEtcPath();
		File overrideFile = new File(etcPath, LogbackPropertyDefiner.OVERRIDE_FILENAME);
		
		try {
			LinkedHashMap<String, LoggerEntry> items = new LinkedHashMap<>();
			
			Map<String, ch.qos.logback.classic.Logger> effectiveLoggers = LogbackHelper.getLoggers(true);
			for (ch.qos.logback.classic.Logger effLogger : effectiveLoggers.values()) {
				final LoggerEntry le = LogbackHelper.asLoggerEntry(effLogger);
				items.put(le.getName(), le);
			}
			
			Map<String, LogbackHelper.LoggerNode> includedLoggers = overrideFile.exists() ? LogbackHelper.readIncludedLoggers(overrideFile) : new LinkedHashMap<>();
			for (LogbackHelper.LoggerNode inclLogger : includedLoggers.values()) {
				if (items.containsKey(inclLogger.name)) {
					items.get(inclLogger.name).setOverrideLevel(inclLogger.level);
				} else {
					items.put(inclLogger.name, new LoggerEntry(inclLogger.name, null, inclLogger.level));
				}
			}
			
			LinkedHashMap<String, LoggerEntry> sortedItems = items.entrySet().stream()
					.sorted((o1, o2) -> {
						return StringUtils.equalsIgnoreCase(o1.getKey(), "ROOT") ? 1 : o1.getKey().compareTo(o2.getKey());
					})
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x,y) -> {throw new AssertionError();}, LinkedHashMap::new));
			
			return sortedItems;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	public LoggerEntry updateLogger(String name, LoggerEntry.Level level) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		if ("ROOT".equals(name)) throw new WTException("Root logger configuration cannot be modified");
		
		String etcPath = wta.getEtcPath();
		if (etcPath == null) throw new WTException("Configuration directory ({}) not defined", WebTopProps.PROP_ETC_DIR);
		File overrideFile = new File(etcPath, LogbackPropertyDefiner.OVERRIDE_FILENAME);
		
		try {
			LoggerEntry ret = null;
			boolean fileExists = overrideFile.exists();
			Map<String, ch.qos.logback.classic.Logger> effectiveLoggers = LogbackHelper.getLoggers(true);
			Map<String, LogbackHelper.LoggerNode> includedLoggers = fileExists ? LogbackHelper.readIncludedLoggers(overrideFile) : new LinkedHashMap<>();
			
			if (level == null) { // Null level means *remove* logger
				includedLoggers.remove(name);
				
			} else {
				includedLoggers.put(name, new LogbackHelper.LoggerNode(name, level));
				if (effectiveLoggers.containsKey(name)) {
					ret = LogbackHelper.asLoggerEntry(effectiveLoggers.get(name));
					ret.setOverrideLevel(level);
				} else {
					ret = new LoggerEntry(name, null, level);
				}
			}
			
			LogbackHelper.writeIncludedLoggers(overrideFile, includedLoggers.values());
			if (!fileExists) LogbackHelper.reloadConfiguration();
			
			return ret;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	public List<OUpgradeStatement> listLastUpgradeStatements() throws WTException {
		UpgradeStatementDAO upgdao = UpgradeStatementDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsSysAdmin();
		
		try {
			con = WT.getConnection(SERVICE_ID);
			String upgradeTag = upgdao.selectLastTag(con);
			return upgdao.selectByTag(con, upgradeTag);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ListDomainAccessLogResult listAccessLog(String domainId, DateTimeRange range, Condition<DomainAccessLogQuery> conditionPredicate, SortInfo sortInfo, int page, int limit, boolean returnFullCount) throws WTException {
		Check.notEmpty(domainId, "domainId");
		Check.notNull(range, "range");
		Check.notNull(conditionPredicate, "conditionPredicate");
		DomainAccessLogDAO alDao = DomainAccessLogDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsSysAdmin();
		
		try {
			org.jooq.Condition condition = BaseDAO.createCondition(conditionPredicate, new DomainAccessLogPredicateVisitor()
				.withIgnoreCase(true)
				.withForceStringLikeComparison(true)
			);
			
			Integer offset = ManagerUtils.toOffset(page, limit);			
			con = WT.getConnection(SERVICE_ID);
			
			Integer fullCount = null;
			if (returnFullCount) fullCount = alDao.countByDomainCondition(con, domainId, range.from, range.to, condition);
			ArrayList<DomainAccessLog> items = new ArrayList<>();
			for (VDomainAccessLog vdal : alDao.selectByDomainCondition(con, domainId, range.from, range.to, condition, sortInfo, limit, offset)) {
				items.add(ManagerUtils.fillDomainAccessLog(new DomainAccessLog(),  vdal));
			}
			return new ListDomainAccessLogResult(items, fullCount);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public ListDomainAccessLogDetailResult listAccessLogDetail(String sessionId, String domainId, String userId, boolean returnFullCount) throws WTException {
		DomainAccessLogDAO domainAccLogDao = DomainAccessLogDAO.getInstance();
		Connection con = null;
		
		RunContext.ensureIsSysAdmin();
		
		try {			
			con = WT.getConnection(SERVICE_ID);
			
			Integer fullCount = null;
			if (returnFullCount) fullCount = domainAccLogDao.countDetailBySessionId(con, sessionId, domainId, userId);
			
			ArrayList<DomainAccessLogDetail> items = new ArrayList<>();
			for (ODomainAccessLogDetail logDetail : domainAccLogDao.getDetailBySessionId(con, sessionId, domainId, userId)) {
				items.add(ManagerUtils.createDomainAccessLogDetail(logDetail));
			}
			
			return new ListDomainAccessLogDetailResult(items, fullCount);
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean executeUpgradeStatement(OUpgradeStatement statement, boolean ignoreErrors) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		ServiceManager srvMgr = wta.getServiceManager();
		return srvMgr.executeUpgradeStatement(statement, ignoreErrors);
	}
	
	public void skipUpgradeStatement(OUpgradeStatement statement) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		ServiceManager srvMgr = wta.getServiceManager();
		srvMgr.skipUpgradeStatement(statement);
	}
	
	public void setMaintenanceMode(boolean active) throws WTException {
		RunContext.ensureIsSysAdmin();
		
		ServiceManager srvMgr = wta.getServiceManager();
		srvMgr.setMaintenance(CoreManifest.ID, active);
	}
}
