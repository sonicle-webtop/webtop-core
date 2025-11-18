/*
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.rest.v1;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.ApiKey;
import com.sonicle.webtop.core.app.model.ApiKeyBase;
import com.sonicle.webtop.core.app.model.ApiKeyNew;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.app.model.DomainGetOption;
import com.sonicle.webtop.core.app.model.DomainUpdateOption;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.GroupBase;
import com.sonicle.webtop.core.app.model.GroupGetOption;
import com.sonicle.webtop.core.app.model.GroupUpdateOption;
import com.sonicle.webtop.core.app.model.LicenseBase;
import com.sonicle.webtop.core.app.model.LicenseListOption;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceBase;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.app.model.ResourceUpdateOption;
import com.sonicle.webtop.core.app.model.Role;
import com.sonicle.webtop.core.app.model.RoleBase;
import com.sonicle.webtop.core.app.model.RoleGetOption;
import com.sonicle.webtop.core.app.model.RoleUpdateOption;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserBase;
import com.sonicle.webtop.core.app.model.UserGetOption;
import com.sonicle.webtop.core.app.model.UserUpdateOption;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.sdk.ResultVoid;
import com.sonicle.webtop.core.app.sdk.WTLicenseActivationException;
import com.sonicle.webtop.core.app.sdk.WTLicenseMismatchException;
import com.sonicle.webtop.core.app.sdk.WTLicenseValidationException;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.sdk.WTPwdPolicyException;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.swagger.v1.api.AdminApi;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddDomain201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddGroup201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddResource201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddRole201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddUser201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKey;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKeyBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKeyGenerated;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomain;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroup;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicense;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiResource;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiRole;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiSettingEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiUser;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserBase;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import net.sf.qualitycheck.Check;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Admin extends AdminApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(Admin.class);

	@Override
	public Response setAdminPassword(String body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] setAdminPassword()", RunContext.getRunProfileId());
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
			adminMgr.updateSysAdminPassword(LangUtils.value(body, (char[])null));
			return respOk();
			
		} catch (WTPwdPolicyException ex) {
			return respErrorBadRequest(ex.getMessage());
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] setAdminPassword()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response listSettings(String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listSettings({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			List<com.sonicle.webtop.core.model.SettingEntry> settings;
			if (!StringUtils.isBlank(domainId)) {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
				if (!adminMgr.existsDomain()) throw new WTNotFoundException("Domain not found [{}]", domainId);
				settings = adminMgr.listDomainSettings(false);
			} else {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
				settings = adminMgr.listSystemSettings(false);
			}
			
			List<ApiSettingEntry> items = new ArrayList<>();
			for (com.sonicle.webtop.core.model.SettingEntry setting : settings) {
				items.add(ApiUtils.createApiSettingEntry(setting));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listSettings({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}

	@Override
	public Response getSetting(String serviceId, String key, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getSetting({}, {}, {})", RunContext.getRunProfileId(), domainId, serviceId, key);
		}
		
		try {
			String value;
			if (!StringUtils.isBlank(domainId)) {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
				if (!adminMgr.existsDomain()) throw new WTNotFoundException("Domain not found [{}]", domainId);
				value = adminMgr.getDomainSetting(serviceId, key);
			} else {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
				value = adminMgr.getSystemSetting(serviceId, key);
			}
			return respOk(ApiUtils.createApiSettingEntry(serviceId, key, value));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getSetting({}, {}, {})", RunContext.getRunProfileId(), domainId, serviceId, key, t);
			return respError(t);
		}
	}

	@Override
	public Response updateSetting(String serviceId, String key, String domainId, String body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateSetting({}, {}, {})", RunContext.getRunProfileId(), domainId, serviceId, key);
		}
		
		try {
			boolean result;
			if (!StringUtils.isBlank(domainId)) {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
				if (!adminMgr.existsDomain()) throw new WTNotFoundException("Domain not found [{}]", domainId);
				result = adminMgr.updateDomainSetting(serviceId, key, body);
			} else {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
				result = adminMgr.updateSystemSetting(serviceId, key, body);
			}
			if (!result) throw new WTException("Value cannot be updated");
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateSetting({}, {}, {})", RunContext.getRunProfileId(), domainId, serviceId, key, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteSetting(String serviceId, String key, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteSetting({}, {}, {})", RunContext.getRunProfileId(), domainId, serviceId, key);
		}
		
		try {
			boolean result;
			if (!StringUtils.isBlank(domainId)) {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
				if (!adminMgr.existsDomain()) throw new WTNotFoundException("Domain not found [{}]", domainId);
				result = adminMgr.deleteDomainSetting(serviceId, key);
			} else {
				CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
				result = adminMgr.deleteSystemSetting(serviceId, key);
			}
			return result ? respOk() : respErrorNotFound();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteSetting({}, {}, {})", RunContext.getRunProfileId(), domainId, serviceId, key, t);
			return respError(t);
		}
	}
	
	@Override
	public Response listDomains() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listDomains()", RunContext.getRunProfileId());
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
			List<ApiDomainEntry> items = new ArrayList<>();
			for (Domain domain : adminMgr.listDomains(EnabledCond.ANY_STATE).values()) {
				String publicUrl = adminMgr.getDomainSetting(CoreManifest.ID, "public.url");
				items.add(ApiUtils.fillApiDomainEntry(new ApiDomainEntry(), domain, publicUrl));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listDomains()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response getDomain(String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getDomain({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<DomainGetOption> options = BitFlags.with(
				DomainGetOption.DIRECTORY_DATA
			);
			Domain domain = adminMgr.getDomain(options);
			if (domain == null) throw new WTNotFoundException("Domain not found [{}]", domainId);
			String publicUrl = adminMgr.getDomainSetting(CoreManifest.ID, "public.url");
			return respOk(ApiUtils.fillApiDomain(new ApiDomain(), domain, publicUrl));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getDomain({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}

	@Override
	public Response addDomain(ApiDomain body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addDomain()", RunContext.getRunProfileId());
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(body.getDomainId()));
			Result<Domain> result = adminMgr.addDomain(body.getDomainId(), ApiUtils.fillDomainBase(new DomainBase(), body));
			adminMgr.updateDomainSetting(CoreManifest.ID, "public.url", body.getPublicURL());
			adminMgr.initDomain();
			
			String publicUrl = adminMgr.getDomainSetting(CoreManifest.ID, "public.url");
			ApiAddDomain201Response response = new ApiAddDomain201Response();
			response.setValue(ApiUtils.fillApiDomain(new ApiDomain(), result.getObject(), publicUrl));
			response.setExceptions(ApiUtils.createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] addDomain()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response updateDomain(String domainId, Long updateOptions, ApiDomainBase body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateDomain({}, {})", RunContext.getRunProfileId(), domainId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<DomainUpdateOption> options = ApiUtils.toDomainUpdateOption(updateOptions);
			adminMgr.updateDomain(ApiUtils.fillDomainBase(new DomainBase(), body), options);
			adminMgr.updateDomainSetting(CoreManifest.ID, "public.url", body.getPublicURL());
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateDomain({}, {})", RunContext.getRunProfileId(), domainId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteDomain(String domainId, Boolean deep) {
		boolean deepDelete = true;
		if (deep != null) deepDelete = deep;
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteDomain({}, {})", RunContext.getRunProfileId(), domainId, deep);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteDomain(deepDelete);
			return respOk(ApiUtils.createApiResultExceptions(result));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteDomain({}, {})", RunContext.getRunProfileId(), domainId, deep, t);
			return respError(t);
		}
	}

	@Override
	public Response listGroups(String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listGroups({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiGroup> items = new ArrayList<>();
			for (Group group : adminMgr.listGroups().values()) {
				items.add(ApiUtils.fillApiGroup(new ApiGroup(), group));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listGroups({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}

	@Override
	public Response getGroup(String groupId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getGroup({}, {})", RunContext.getRunProfileId(), domainId, groupId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<GroupGetOption> options = BitFlags.with(
				GroupGetOption.USER_ASSOCIATIONS,
				GroupGetOption.ROLE_ASSOCIATIONS,
				GroupGetOption.PERMISSIONS,
				GroupGetOption.SERVICE_PERMISSIONS
			);
			Group group = adminMgr.getGroup(groupId, options);
			if (group == null) throw new WTNotFoundException("Group not found [{}, {}]", domainId, groupId);
			return respOk(ApiUtils.fillApiGroup(new ApiGroup(), group));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getGroup({}, {})", RunContext.getRunProfileId(), domainId, groupId, t);
			return respError(t);
		}
	}

	@Override
	public Response addGroup(String domainId, Long updateOptions, ApiGroupAdd body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateGroup({}, {})", RunContext.getRunProfileId(), domainId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<GroupUpdateOption> options = ApiUtils.toGroupUpdateOption(updateOptions);
			Result<Group> result = adminMgr.addGroup(body.getGroupId(), ApiUtils.fillGroupBase(new GroupBase(), body, domainId, options.has(GroupUpdateOption.SUBJECTS_AS_SID)), options);
			
			ApiAddGroup201Response response = new ApiAddGroup201Response();
			response.setValue(ApiUtils.fillApiGroup(new ApiGroup(), result.getObject()));
			response.setExceptions(ApiUtils.createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateGroup({}, {})", RunContext.getRunProfileId(), domainId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response updateGroup(String groupId, String domainId, Long updateOptions, ApiGroupBase body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateGroup({}, {}, {})", RunContext.getRunProfileId(), domainId, groupId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<GroupUpdateOption> options = ApiUtils.toGroupUpdateOption(updateOptions);
			adminMgr.updateGroup(groupId, ApiUtils.fillGroupBase(new GroupBase(), body, domainId, options.has(GroupUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateGroup({}, {}, {})", RunContext.getRunProfileId(), domainId, groupId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteGroup(String groupId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteGroup({}, {})", RunContext.getRunProfileId(), domainId, groupId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteGroup(groupId);
			return respOk(ApiUtils.createApiResultExceptions(result));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteGroup({}, {})", RunContext.getRunProfileId(), domainId, groupId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response listUsers(String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listUsers({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiUser> items = new ArrayList<>();
			for (User user : adminMgr.listUsers(EnabledCond.ANY_STATE).values()) {
				items.add(ApiUtils.fillApiUser(new ApiUser(), user));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listUsers({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}

	@Override
	public Response getUser(String userId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getUser({}, {})", RunContext.getRunProfileId(), domainId, userId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<UserGetOption> options = BitFlags.with(
				UserGetOption.GROUP_ASSOCIATIONS,
				UserGetOption.ROLE_ASSOCIATIONS,
				UserGetOption.PERMISSIONS,
				UserGetOption.SERVICE_PERMISSIONS
			);
			User user = adminMgr.getUser(userId, options);
			if (user == null) throw new WTNotFoundException("User not found [{}, {}]", domainId, userId);
			return respOk(ApiUtils.fillApiUser(new ApiUser(), user));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getUser({}, {})", RunContext.getRunProfileId(), domainId, userId, t);
			return respError(t);
		}
	}

	@Override
	public Response addUser(String domainId, Long updateOptions, ApiUserAdd body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addUser({}, {})", RunContext.getRunProfileId(), domainId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<UserUpdateOption> options = ApiUtils.toUserUpdateOption(updateOptions);
			Result<User> result = adminMgr.addUser(body.getUserId(), ApiUtils.fillUserBase(new UserBase(), body, domainId, options.has(UserUpdateOption.SUBJECTS_AS_SID)), true, body.getPassword().toCharArray(), options);
			
			ApiAddUser201Response response = new ApiAddUser201Response();
			response.setValue(ApiUtils.fillApiUser(new ApiUser(), result.getObject()));
			response.setExceptions(ApiUtils.createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] addUser({}, {})", RunContext.getRunProfileId(), domainId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response updateUser(String userId, String domainId, Long updateOptions, ApiUserBase body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateUser({}, {}, {})", RunContext.getRunProfileId(), domainId, userId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<UserUpdateOption> options = ApiUtils.toUserUpdateOption(updateOptions);
			adminMgr.updateUser(userId, ApiUtils.fillUserBase(new UserBase(), body, domainId, options.has(UserUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateUser({}, {}, {})", RunContext.getRunProfileId(), domainId, userId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteUser(String userId, String domainId, Boolean deep) {
		boolean deepDelete = true;
		if (deep != null) deepDelete = deep;
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteUser({}, {}, {})", RunContext.getRunProfileId(), domainId, userId, deep);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteUser(userId, deepDelete);
			return respOk(ApiUtils.createApiResultExceptions(result));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteUser({}, {}, {})", RunContext.getRunProfileId(), domainId, userId, deep, t);
			return respError(t);
		}
	}
	
	@Override
	public Response setUserPassword(String userId, String domainId, String body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] setUserPassword({}, {}, {})", RunContext.getRunProfileId(), domainId, userId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.updateUserPassword(userId, LangUtils.value(body, (char[])null), false);
			return respOk();
			
		} catch (WTPwdPolicyException ex) {
			return respErrorBadRequest(ex.getMessage());
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] setUserPassword({}, {}, {})", RunContext.getRunProfileId(), domainId, userId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response listResources(String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listResources({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiResource> items = new ArrayList<>();
			for (Resource resource : adminMgr.listResources(EnabledCond.ANY_STATE).values()) {
				items.add(ApiUtils.fillApiResource(new ApiResource(), resource));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listResources({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}

	@Override
	public Response getResource(String resourceId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getResource({}, {})", RunContext.getRunProfileId(), domainId, resourceId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<ResourceGetOption> options = BitFlags.with(
				ResourceGetOption.PERMISSIONS
			);
			Resource resource = adminMgr.getResource(resourceId, options);
			if (resource == null) throw new WTNotFoundException("Resource not found [{}, {}]", domainId, resourceId);
			return respOkCreated(ApiUtils.fillApiResource(new ApiResource(), resource));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getResource({}, {})", RunContext.getRunProfileId(), domainId, resourceId, t);
			return respError(t);
		}
	}

	@Override
	public Response addResource(String domainId, Long updateOptions, ApiResourceAdd body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addResource({}, {})", RunContext.getRunProfileId(), domainId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<ResourceUpdateOption> options = ApiUtils.toResourceUpdateOption(updateOptions);
			Result<Resource> result = adminMgr.addResource(body.getResourceId(), ApiUtils.fillResourceBase(new ResourceBase(), domainId, body, options.has(ResourceUpdateOption.SUBJECTS_AS_SID)), options);
			
			ApiAddResource201Response response = new ApiAddResource201Response();
			response.setValue(ApiUtils.fillApiResource(new ApiResource(), result.getObject()));
			response.setExceptions(ApiUtils.createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] addResource({}, {})", RunContext.getRunProfileId(), domainId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response updateResource(String resourceId, String domainId, Long updateOptions, ApiResourceBase body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateResource({}, {}, {})", RunContext.getRunProfileId(), domainId, resourceId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<ResourceUpdateOption> options = ApiUtils.toResourceUpdateOption(updateOptions);
			adminMgr.updateResource(resourceId, ApiUtils.fillResourceBase(new ResourceBase(), domainId, body, options.has(ResourceUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateResource({}, {}, {})", RunContext.getRunProfileId(), domainId, resourceId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteResource(String resourceId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteResource({}, {})", RunContext.getRunProfileId(), domainId, resourceId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteResource(resourceId);
			return respOk(ApiUtils.createApiResultExceptions(result));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteResource({}, {})", RunContext.getRunProfileId(), domainId, resourceId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response listRoles(String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listRoles({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiRole> items = new ArrayList<>();
			for (Role role : adminMgr.listRoles().values()) {
				items.add(ApiUtils.fillApiRole(new ApiRole(), role));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listRoles({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}

	@Override
	public Response getRole(String roleId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getRole({}, {})", RunContext.getRunProfileId(), domainId, roleId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<RoleGetOption> options = BitFlags.with(
				RoleGetOption.PERMISSIONS,
				RoleGetOption.SERVICE_PERMISSIONS
			);
			Role role = adminMgr.getRole(roleId, options);
			if (role == null) throw new WTNotFoundException("Role not found [{}, {}]", domainId, roleId);
			return respOk(ApiUtils.fillApiRole(new ApiRole(), role));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getRole({}, {})", RunContext.getRunProfileId(), domainId, roleId, t);
			return respError(t);
		}
	}

	@Override
	public Response addRole(String domainId, Long updateOptions, ApiRoleAdd body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addRole({}, {})", RunContext.getRunProfileId(), domainId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<RoleUpdateOption> options = ApiUtils.toRoleUpdateOption(updateOptions);
			Result<Role> result = adminMgr.addRole(body.getRoleId(), ApiUtils.fillRoleBase(new RoleBase(), body, domainId, options.has(RoleUpdateOption.SUBJECTS_AS_SID)), options);
			
			ApiAddRole201Response response = new ApiAddRole201Response();
			response.setValue(ApiUtils.fillApiRole(new ApiRole(), result.getObject()));
			response.setExceptions(ApiUtils.createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] addRole({}, {})", RunContext.getRunProfileId(), domainId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response updateRole(String roleId, String domainId, Long updateOptions, ApiRoleBase body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateRole({}, {}, {})", RunContext.getRunProfileId(), domainId, roleId, updateOptions);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<RoleUpdateOption> options = ApiUtils.toRoleUpdateOption(updateOptions);
			adminMgr.updateRole(roleId, ApiUtils.fillRoleBase(new RoleBase(), body, domainId, options.has(RoleUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateRole({}, {}, {})", RunContext.getRunProfileId(), domainId, roleId, updateOptions, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteRole(String roleId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteRole({}, {})", RunContext.getRunProfileId(), domainId, roleId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteRole(roleId);
			return respOk(ApiUtils.createApiResultExceptions(result));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteRole({}, {})", RunContext.getRunProfileId(), domainId, roleId, t);
			return respError(t);
		}
	}

	@Override
	public Response listLicenses(String domainId, Boolean includeBuiltin) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listLicenses({}, {})", RunContext.getRunProfileId(), domainId, includeBuiltin);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<LicenseListOption> options = BitFlags.with(LicenseListOption.EXTENDED_INFO);
			if (LangUtils.value(includeBuiltin, true)) options.set(LicenseListOption.INCLUDE_BUILTIN);
			List<ApiLicense> items = new ArrayList<>();
			for (ServiceLicense license : adminMgr.listLicenses(options)) {
				items.add(ApiUtils.fillApiLicense(new ApiLicense(), license));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listLicenses({}, {})", RunContext.getRunProfileId(), domainId, includeBuiltin, t);
			return respError(t);
		}
	}

	@Override
	public Response addLicense(String productCode, String domainId, Boolean autoActivate, ApiLicenseAdd body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addLicense({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, autoActivate);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.addLicense(productCode, ApiUtils.fillLicenseBase(new LicenseBase(), body), LangUtils.value(autoActivate, true));
			if (LangUtils.isNotEmpty(body.getAssignedLeases())) {
				adminMgr.assignLicenseLease(productCode, LangUtils.asSet(body.getAssignedLeases()));
			}
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] addLicense({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, autoActivate, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteLicense(String productCode, String domainId, Boolean force) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteLicense({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, force);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.deleteLicense(productCode, LangUtils.value(force, false));
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteLicense({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, force, t);
			return respError(t);
		}
	}

	@Override
	public Response assignLicenseLease(String productCode, String domainId, List<String> body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] assignLicenseLease({}, {})", RunContext.getRunProfileId(), domainId, productCode);
		}
		
		try {
			Check.notEmpty(body, "body");
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.assignLicenseLease(productCode, LangUtils.asSet(body));
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] assignLicenseLease({}, {})", RunContext.getRunProfileId(), domainId, productCode, t);
			return respError(t);
		}
	}
	
	@Override
	public Response revokeLicenseLease(String productCode, String domainId, List<String> body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] revokeLicenseLease({}, {})", RunContext.getRunProfileId(), domainId, productCode);
		}
		
		try {
			Check.notEmpty(body, "body");
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.revokeLicenseLease(productCode, LangUtils.asSet(body));
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] revokeLicenseLease({}, {})", RunContext.getRunProfileId(), domainId, productCode, t);
			return respError(t);
		}
	}

	@Override
	public Response getLicenseOfflineReqInfo(String productCode, String domainId, Boolean deactivation) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getLicenseOfflineReqInfo({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, deactivation);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ProductLicense productLicense = adminMgr.getProductLicense(productCode);
			if (productLicense == null) throw new WTNotFoundException("Product not found [{}]", productCode);
			if (LangUtils.value(deactivation, false) == true) {
				// Deactivation info are NOT always returned: they are null when product is already activated!
				ProductLicense.RequestInfo info = productLicense.getManualDeactivationRequestInfo();
				if (info != null) {
					return respErrorBadRequest("Product is already deactivated");
				} else {
					return respOk(ApiUtils.createApiLicenseOfflineReqInfo(info));
				}
			} else {
				// Activation info are always returned, also when product is already activated!
				return respOk(ApiUtils.createApiLicenseOfflineReqInfo(productLicense.getManualActivationRequestInfo()));
			}
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getLicenseOfflineReqInfo({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, deactivation, t);
			return respError(t);
		}
	}

	@Override
	public Response activateLicense(String productCode, String domainId, String body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] activateLicense({}, {})", RunContext.getRunProfileId(), domainId, productCode);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			try {
				adminMgr.activateLicense(productCode, body);
				return respOk();
			} catch(WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
				return respError(ex);
			}	
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] activateLicense({}, {})", RunContext.getRunProfileId(), domainId, productCode, t);
			return respError(t);
		}
	}

	@Override
	public Response deactivateLicense(String productCode, String domainId, Boolean offline) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deactivateLicense({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, offline);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			try {
				adminMgr.deactivateLicense(productCode, LangUtils.value(offline, false) == true);
				return respOk();
			} catch (WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
				return respError(ex);
			}
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deactivateLicense({}, {}, {})", RunContext.getRunProfileId(), domainId, productCode, offline, t);
			return respError(t);
		}
	}

	@Override
	public Response listApiKeys(String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listApiKeys({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			List<ApiApiKey> items = new ArrayList<>();
			for (ApiKey apiKey : adminMgr.listApiKeys().values()) {
				items.add(ApiUtils.fillApiKey(new ApiApiKey(), null, apiKey));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] listApiKeys({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response getApiKey(String apikeyId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getApiKey({}, {})", RunContext.getRunProfileId(), domainId, apikeyId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			ApiKey apiKey = adminMgr.getApiKey(apikeyId);
			if (apiKey == null) return respErrorNotFound();
			
			return respOk(ApiUtils.fillApiKey(new ApiApiKey(), null, apiKey));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] getApiKey({}, {})", RunContext.getRunProfileId(), domainId, apikeyId, t);
			return respError(t);
		}
	}

	@Override
	public Response generateApiKey(String domainId, ApiApiKeyBase body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] generateApiKey({})", RunContext.getRunProfileId(), domainId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			ApiKeyBase apiKey = ApiUtils.fillApiKeyBase(new ApiKeyBase(), null, body);
			ApiKeyNew apiKeyNew = adminMgr.createApiKey(apiKey);
			
			ApiApiKeyGenerated apiKeyGenerated = new ApiApiKeyGenerated();
			apiKeyGenerated.secretToken(apiKeyNew.getApiKeyString());
			return respOkCreated(ApiUtils.fillApiKeyBase(apiKeyGenerated, null, apiKeyNew));
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] generateApiKey({})", RunContext.getRunProfileId(), domainId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateApiKey(String apikeyId, String domainId, ApiApiKeyBase body) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateApiKey({}, {})", RunContext.getRunProfileId(), domainId, apikeyId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			ApiKeyBase apiKey = ApiUtils.fillApiKeyBase(new ApiKeyBase(), null, body);
			adminMgr.updateApiKeyDetails(apikeyId, apiKey);
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] updateApiKey({}, {})", RunContext.getRunProfileId(), domainId, apikeyId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response deleteApiKey(String apikeyId, String domainId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteApiKey({}, {})", RunContext.getRunProfileId(), domainId, apikeyId);
		}
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			adminMgr.deleteApiKey(apikeyId);
			return respOk();
			
		} catch (Throwable t) {
			if (shouldLogError(t)) LOGGER.error("[{}] deleteApiKey({})", RunContext.getRunProfileId(), apikeyId, t);
			return respError(t);
		}
	}

	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
			.code(status.getStatusCode())
			.description(message);
	}
}
