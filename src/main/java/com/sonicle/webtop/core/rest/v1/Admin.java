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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.security.ConnectionSecurity;
import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.ProductRegistry;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.app.model.DomainGetOption;
import com.sonicle.webtop.core.app.model.DomainUpdateOption;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.GroupBase;
import com.sonicle.webtop.core.app.model.GroupGetOption;
import com.sonicle.webtop.core.app.model.GroupUpdateOption;
import com.sonicle.webtop.core.app.model.HomedThrowable;
import com.sonicle.webtop.core.app.model.LdapDirectoryParams;
import com.sonicle.webtop.core.app.model.LicenseBase;
import com.sonicle.webtop.core.app.model.PermissionString;
import com.sonicle.webtop.core.app.model.LicenseComputedStatus;
import com.sonicle.webtop.core.app.model.LicenseExInfo;
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
import com.sonicle.webtop.core.model.License;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.ServiceLicenseLease;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.swagger.v1.api.AdminApi;
import com.sonicle.webtop.core.swagger.v1.model.ApiAdminAddDomain201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAdminAddGroup201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAdminAddResource201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAdminAddRole201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAdminAddUser201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiDirectoryPasswordPolicies;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomain;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainBaseDirRawParameters;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroup;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiHomedException;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicense;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseLease;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseOfflineReqInfo;
import com.sonicle.webtop.core.swagger.v1.model.ApiResource;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiResultExceptions;
import com.sonicle.webtop.core.swagger.v1.model.ApiRole;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiSettingEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiUser;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserBase;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import net.sf.qualitycheck.Check;
import org.codehaus.plexus.util.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
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
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
			adminMgr.updateSysAdminPassword(LangUtils.value(body, (char[])null));
			return respOk();
			
		} catch (WTPwdPolicyException ex) {
			return respErrorBadRequest(ex.getMessage());
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response listSettings(String domainId) {
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
				items.add(createApiSettingEntry(setting));
			}
			return respOk(items);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response getSetting(String serviceId, String key, String domainId) {
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
			return respOk(createApiSettingEntry(serviceId, key, value));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response updateSetting(String serviceId, String key, String domainId, String body) {
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
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response deleteSetting(String serviceId, String key, String domainId) {
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
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}
	
	@Override
	public Response adminListDomains() {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.getRunProfileId());
			List<ApiDomainEntry> items = new ArrayList<>();
			for (Domain domain : adminMgr.listDomains(EnabledCond.ANY_STATE).values()) {
				String publicUrl = adminMgr.getDomainSetting(CoreManifest.ID, "public.url");
				items.add(createApiDomainBasic(domain, publicUrl));
			}
			return respOk(items);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminGetDomain(String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<DomainGetOption> options = BitFlags.with(
				DomainGetOption.DIRECTORY_DATA
			);
			Domain domain = adminMgr.getDomain(options);
			if (domain == null) throw new WTNotFoundException("Domain not found [{}]", domainId);
			String publicUrl = adminMgr.getDomainSetting(CoreManifest.ID, "public.url");
			return respOk(createApiDomain(domain, publicUrl));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminAddDomain(ApiDomain body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(body.getDomainId()));
			Result<Domain> result = adminMgr.addDomain(body.getDomainId(), createDomainBase(body));
			adminMgr.updateDomainSetting(CoreManifest.ID, "public.url", body.getPublicURL());
			adminMgr.initDomain();
			
			String publicUrl = adminMgr.getDomainSetting(CoreManifest.ID, "public.url");
			ApiAdminAddDomain201Response response = new ApiAdminAddDomain201Response();
			response.setValue(createApiDomain(result.getObject(), publicUrl));
			response.setExceptions(createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminUpdateDomain(String domainId, Long updateOptions, ApiDomainBase body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<DomainUpdateOption> options = toDomainUpdateOption(updateOptions);
			adminMgr.updateDomain(createDomainBase(body), options);
			adminMgr.updateDomainSetting(CoreManifest.ID, "public.url", body.getPublicURL());
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminDeleteDomain(String domainId, Boolean deep) {
		boolean deepDelete = true;
		if (deep != null) deepDelete = deep;
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteDomain(deepDelete);
			return respOk(createApiResultExceptions(result));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminListGroups(String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiGroup> items = new ArrayList<>();
			for (Group group : adminMgr.listGroups().values()) {
				items.add(createApiGroup(group));
			}
			return respOk(items);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminGetGroup(String groupId, String domainId) {
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
			return respOk(createApiGroup(group));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminAddGroup(String domainId, Long updateOptions, ApiGroupAdd body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<GroupUpdateOption> options = toGroupUpdateOption(updateOptions);
			Result<Group> result = adminMgr.addGroup(body.getGroupId(), createGroupBase(body, domainId, options.has(GroupUpdateOption.SUBJECTS_AS_SID)), options);
			
			ApiAdminAddGroup201Response response = new ApiAdminAddGroup201Response();
			response.setValue(createApiGroup(result.getObject()));
			response.setExceptions(createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminUpdateGroup(String groupId, String domainId, Long updateOptions, ApiGroupBase body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<GroupUpdateOption> options = toGroupUpdateOption(updateOptions);
			adminMgr.updateGroup(groupId, createGroupBase(body, domainId, options.has(GroupUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminDeleteGroup(String groupId, String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteGroup(groupId);
			return respOk(createApiResultExceptions(result));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}
	
	@Override
	public Response adminListUsers(String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiUser> items = new ArrayList<>();
			for (User user : adminMgr.listUsers(EnabledCond.ANY_STATE).values()) {
				items.add(createApiUser(user));
			}
			return respOk(items);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminGetUser(String userId, String domainId) {
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
			return respOk(createApiUser(user));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminAddUser(String domainId, Long updateOptions, ApiUserAdd body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<UserUpdateOption> options = toUserUpdateOption(updateOptions);
			Result<User> result = adminMgr.addUser(body.getUserId(), createUserBase(body, domainId, options.has(UserUpdateOption.SUBJECTS_AS_SID)), true, body.getPassword().toCharArray(), options);
			
			ApiAdminAddUser201Response response = new ApiAdminAddUser201Response();
			response.setValue(createApiUser(result.getObject()));
			response.setExceptions(createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminUpdateUser(String userId, String domainId, Long updateOptions, ApiUserBase body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<UserUpdateOption> options = toUserUpdateOption(updateOptions);
			adminMgr.updateUser(userId, createUserBase(body, domainId, options.has(UserUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminDeleteUser(String userId, String domainId, Boolean deep) {
		boolean deepDelete = true;
		if (deep != null) deepDelete = deep;
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteUser(userId, deepDelete);
			return respOk(createApiResultExceptions(result));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}
	
	@Override
	public Response adminSetUserPassword(String userId, String domainId, String body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.updateUserPassword(userId, LangUtils.value(body, (char[])null));
			return respOk();
			
		} catch (WTPwdPolicyException ex) {
			return respErrorBadRequest(ex.getMessage());
		} catch (Exception ex) {
			return respError(ex);
		}
	}
	
	@Override
	public Response adminListResources(String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiResource> items = new ArrayList<>();
			for (Resource resource : adminMgr.listResources(EnabledCond.ANY_STATE).values()) {
				items.add(createApiResource(resource));
			}
			return respOk(items);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminGetResource(String resourceId, String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<ResourceGetOption> options = BitFlags.with(
				ResourceGetOption.PERMISSIONS
			);
			Resource resource = adminMgr.getResource(resourceId, options);
			if (resource == null) throw new WTNotFoundException("Resource not found [{}, {}]", domainId, resourceId);
			return respOkCreated(createApiResource(resource));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminAddResource(String domainId, Long updateOptions, ApiResourceAdd body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<ResourceUpdateOption> options = toResourceUpdateOption(updateOptions);
			Result<Resource> result = adminMgr.addResource(body.getResourceId(), createResourceBase(body, domainId, options.has(ResourceUpdateOption.SUBJECTS_AS_SID)), options);
			
			ApiAdminAddResource201Response response = new ApiAdminAddResource201Response();
			response.setValue(createApiResource(result.getObject()));
			response.setExceptions(createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminUpdateResource(String resourceId, String domainId, Long updateOptions, ApiResourceBase body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<ResourceUpdateOption> options = toResourceUpdateOption(updateOptions);
			adminMgr.updateResource(resourceId, createResourceBase(body, domainId, options.has(ResourceUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminDeleteResource(String resourceId, String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteResource(resourceId);
			return respOk(createApiResultExceptions(result));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}
	
	@Override
	public Response adminListRoles(String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			List<ApiRole> items = new ArrayList<>();
			for (Role role : adminMgr.listRoles().values()) {
				items.add(createApiRole(role));
			}
			return respOk(items);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminGetRole(String roleId, String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<RoleGetOption> options = BitFlags.with(
				RoleGetOption.PERMISSIONS,
				RoleGetOption.SERVICE_PERMISSIONS
			);
			Role role = adminMgr.getRole(roleId, options);
			if (role == null) throw new WTNotFoundException("Role not found [{}, {}]", domainId, roleId);
			return respOk(createApiRole(role));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminAddRole(String domainId, Long updateOptions, ApiRoleAdd body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<RoleUpdateOption> options = toRoleUpdateOption(updateOptions);
			Result<Role> result = adminMgr.addRole(body.getRoleId(), createRoleBase(body, domainId, options.has(RoleUpdateOption.SUBJECTS_AS_SID)), options);
			
			ApiAdminAddRole201Response response = new ApiAdminAddRole201Response();
			response.setValue(createApiRole(result.getObject()));
			response.setExceptions(createApiHomedExceptionList(result));
			return respOkCreated(response);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminUpdateRole(String roleId, String domainId, Long updateOptions, ApiRoleBase body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<RoleUpdateOption> options = toRoleUpdateOption(updateOptions);
			adminMgr.updateRole(roleId, createRoleBase(body, domainId, options.has(RoleUpdateOption.SUBJECTS_AS_SID)), options);
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminDeleteRole(String roleId, String domainId) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ResultVoid result = adminMgr.deleteRole(roleId);
			return respOk(createApiResultExceptions(result));
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminListLicenses(String domainId, Boolean includeBuiltin) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			BitFlags<LicenseListOption> options = BitFlags.with(LicenseListOption.EXTENDED_INFO);
			if (LangUtils.value(includeBuiltin, true)) options.set(LicenseListOption.INCLUDE_BUILTIN);
			List<ApiLicense> items = new ArrayList<>();
			for (ServiceLicense license : adminMgr.listLicenses(options)) {
				items.add(createApiLicense(license));
			}
			return respOk(items);
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminAddLicense(String productCode, String domainId, Boolean autoActivate, ApiLicenseAdd body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.addLicense(productCode, createLicenseBase(body), LangUtils.value(autoActivate, true));
			if (LangUtils.isNotEmpty(body.getAssignedLeases())) {
				adminMgr.assignLicenseLease(productCode, LangUtils.asSet(body.getAssignedLeases()));
			}
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminDeleteLicense(String productCode, String domainId, Boolean force) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.deleteLicense(productCode, LangUtils.value(force, false));
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminAssignLicenseLease(String productCode, String domainId, List<String> body) {
		try {
			Check.notEmpty(body, "body");
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.assignLicenseLease(productCode, LangUtils.asSet(body));
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}
	
	@Override
	public Response adminRevokeLicenseLease(String productCode, String domainId, List<String> body) {
		try {
			Check.notEmpty(body, "body");
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			adminMgr.revokeLicenseLease(productCode, LangUtils.asSet(body));
			return respOk();
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminGetLicenseOfflineReqInfo(String productCode, String domainId, Boolean deactivation) {
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
					return respOk(createApiLicenseOfflineReqInfo(info));
				}
			} else {
				// Activation info are always returned, also when product is already activated!
				return respOk(createApiLicenseOfflineReqInfo(productLicense.getManualActivationRequestInfo()));
			}
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminActivateLicense(String productCode, String domainId, String body) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			try {
				adminMgr.activateLicense(productCode, body);
				return respOk();
			} catch(WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
				return respError(ex);
			}	
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}

	@Override
	public Response adminDeactivateLicense(String productCode, String domainId, Boolean offline) {
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			try {
				adminMgr.deactivateLicense(productCode, LangUtils.value(offline, false) == true);
				return respOk();
			} catch (WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
				return respError(ex);
			}
			
		} catch (Exception ex) {
			return respError(ex);
		}
	}
	
	private ApiSettingEntry createApiSettingEntry(com.sonicle.webtop.core.model.SettingEntry setting) {
		return createApiSettingEntry(setting.getServiceId(), setting.getKey(), setting.getValue());
	}
	
	private ApiSettingEntry createApiSettingEntry(String serviceId, String key, String value) {
		return new ApiSettingEntry()
			.serviceId(serviceId)
			.key(key)
			.value(value);
	}
	
	private DomainBase createDomainBase(ApiDomain body) throws URISyntaxException {
		DomainBase item = new DomainBase();
		item.setEnabled(body.getEnabled());
		item.setDisplayName(body.getDisplayName());
		item.setAuthDomainName(body.getAuthDomainName());
		item.setDomainName(body.getDomainName());
		item.setUserAutoCreation(body.getUserAutoCreation());
		item.setDirUri(URIUtils.createURI(body.getDirUri()));
		item.setDirAdmin(body.getDirAdmin());
		item.setDirPassword(body.getDirPassword());
		item.setDirConnSecurity(EnumUtils.forName(body.getDirConnSecurity(), ConnectionSecurity.class));
		item.setDirCaseSensitive(body.getDirCaseSensitive());
		if (LdapDirectoryParams.class.equals(item.getDirRawParametersClass())) {
			item.writeDirRawParameters(createLdapDirectoryParams(body.getDirRawParameters()), LdapDirectoryParams.class);
		} else {
			item.setDirRawParameters(null);
		}
		item.setPasswordPolicies(createPasswordPolicies(body.getPasswordPolicies()));
		return item;
	}
	
	private DomainBase createDomainBase(ApiDomainBase body) throws URISyntaxException {
		DomainBase item = new DomainBase();
		item.setEnabled(body.getEnabled());
		item.setDisplayName(body.getDisplayName());
		item.setAuthDomainName(body.getAuthDomainName());
		item.setDomainName(body.getDomainName());
		item.setUserAutoCreation(body.getUserAutoCreation());
		item.setDirUri(URIUtils.createURI(body.getDirUri()));
		item.setDirAdmin(body.getDirAdmin());
		item.setDirPassword(body.getDirPassword());
		item.setDirConnSecurity(EnumUtils.forName(body.getDirConnSecurity(), ConnectionSecurity.class));
		item.setDirCaseSensitive(body.getDirCaseSensitive());
		if (LdapDirectoryParams.class.equals(item.getDirRawParametersClass())) {
			item.writeDirRawParameters(createLdapDirectoryParams(body.getDirRawParameters()), LdapDirectoryParams.class);
		} else {
			item.setDirRawParameters(null);
		}
		item.setPasswordPolicies(createPasswordPolicies(body.getPasswordPolicies()));
		return item;
	}
	
	private DomainBase.PasswordPolicies createPasswordPolicies(ApiDirectoryPasswordPolicies policies) {
		if (policies == null) return null;
		return new DomainBase.PasswordPolicies(
			LangUtils.value(policies.getMinLength(), (Short)null),
			policies.getComplexity(),
			policies.getAvoidConsecutiveChars(),
			policies.getAvoidOldSimilarity(),
			policies.getAvoidUsernameSimilarity(),
			LangUtils.value(policies.getExpiration(), (Short)null),
			policies.getVerifyAtLogin()
		);
	}
	
	private LdapDirectoryParams createLdapDirectoryParams(ApiDomainBaseDirRawParameters js) {
		LdapDirectoryParams params = new LdapDirectoryParams();
		params.loginDn = js.getLoginDn();
		params.loginFilter = js.getLoginFilter();
		params.userDn = js.getUserDn();
		params.userFilter = js.getUserFilter();
		params.userIdField = js.getUserIdField();
		params.userFirstnameField = js.getUserFirstnameField();
		params.userLastnameField = js.getUserLastnameField();
		params.userDisplayNameField = js.getUserDisplayNameField();
		return params;
	}
	
	private ApiDomain createApiDomain(Domain domain, String publicUrl) {
		return new ApiDomain()
			.domainId(domain.getDomainId())
			.enabled(domain.getEnabled())
			.displayName(domain.getDisplayName())
			.authDomainName(domain.getAuthDomainName())
			.domainName(domain.getDomainName())
			.publicURL(publicUrl)
			.userAutoCreation(domain.getUserAutoCreation())
			.dirUri(domain.getDirUri().toString())
			.dirAdmin(domain.getDirAdmin())
			.dirPassword(domain.getDirPassword())
			.dirConnSecurity(ApiDomain.DirConnSecurityEnum.fromValue(EnumUtils.getName(domain.getDirConnSecurity(), ApiDomain.DirConnSecurityEnum.OFF.name())))
			.dirCaseSensitive(domain.getDirCaseSensitive())
			.dirRawParameters(createApiDirectoryPasswordPolicies(domain))
			.passwordPolicies(createApiDirectoryPasswordPolicies(domain.getPasswordPolicies()));
	}
	
	private ApiDomainBaseDirRawParameters createApiDirectoryPasswordPolicies(DomainBase domain) {
		if (LdapDirectoryParams.class.equals(domain.getDirRawParametersClass())) {
			final LdapDirectoryParams params = domain.readDirRawParameters(LdapDirectoryParams.class);
			return new ApiDomainBaseDirRawParameters()
				.loginDn(params.loginDn)
				.loginFilter(params.loginFilter)
				.userDn(params.userDn)
				.userFilter(params.userFilter)
				.userIdField(params.userIdField)
				.userFirstnameField(params.userFirstnameField)
				.userLastnameField(params.userLastnameField)
				.userDisplayNameField(params.userDisplayNameField);
		} else {
			return null;
		}
	}
	
	private ApiDirectoryPasswordPolicies createApiDirectoryPasswordPolicies(DomainBase.PasswordPolicies policies) {
		if (policies == null) return null;
		return new ApiDirectoryPasswordPolicies()
			.minLength(LangUtils.value(policies.getMinLength(), (Integer)null))
			.complexity(policies.getComplexity())
			.avoidConsecutiveChars(policies.getAvoidConsecutiveChars())
			.avoidOldSimilarity(policies.getAvoidOldSimilarity())
 			.avoidUsernameSimilarity(policies.getAvoidUsernameSimilarity())
			.expiration(LangUtils.value(policies.getExpiration(), (Integer)null))
			.verifyAtLogin(policies.getVerifyAtLogin());
	}
	
	private ApiDomainEntry createApiDomainBasic(Domain domain, String publicUrl) {
		return new ApiDomainEntry()
			.domainId(domain.getDomainId())
			.enabled(domain.getEnabled())
			.displayName(domain.getDisplayName())
			.authDomainName(domain.getAuthDomainName())
			.publicURL(publicUrl)
			.domainName(domain.getDomainName())
			.userAutoCreation(domain.getUserAutoCreation())
			.dirUri(domain.getDirUri().toString());
	}
	
	private BitFlags<DomainUpdateOption> toDomainUpdateOption(Long options) {
		if (options == null) {
			return BitFlags.noneOf(DomainUpdateOption.class);
		} else {
			return BitFlags.newFrom(DomainUpdateOption.class, options);
		}
	}
	
	private ApiGroup createApiGroup(Group group) {
		return new ApiGroup()
			.groupId(group.getGroupId())
			.groupSid(group.getGroupSid())
			.builtIn(group.isBuiltIn())
			.description(group.getDescription())
			.assignedUsers(LangUtils.defaultList(group.getAssignedUsers(), null))
			.assignedRoles(LangUtils.defaultList(group.getAssignedRoles(), null))
			.permissions(asApiPermissionStrings(group.getPermissions()))
			.allowedServiceIds(LangUtils.defaultList(group.getAllowedServiceIds(), null));
	}
	
	private GroupBase createGroupBase(ApiGroupAdd body, String domainId, boolean subjectsAsSID) {
		GroupBase item = new GroupBase();
		item.setDescription(body.getDescription());
		item.setAssignedUsers(asSubjects(body.getAssignedUsers(), domainId, subjectsAsSID));
		item.setAssignedRoles(asSubjects(body.getAssignedRoles(), domainId, subjectsAsSID));
		item.setPermissions(asPermissionStrings(body.getPermissions()));
		item.setAllowedServiceIds(LangUtils.defaultSet(body.getAllowedServiceIds(), null));
		return item;
	}
	
	private GroupBase createGroupBase(ApiGroupBase body, String domainId, boolean subjectsAsSID) {
		GroupBase item = new GroupBase();
		item.setDescription(body.getDescription());
		item.setAssignedUsers(asSubjects(body.getAssignedUsers(), domainId, subjectsAsSID));
		item.setAssignedRoles(asSubjects(body.getAssignedRoles(), domainId, subjectsAsSID));
		item.setPermissions(asPermissionStrings(body.getPermissions()));
		item.setAllowedServiceIds(LangUtils.defaultSet(body.getAllowedServiceIds(), null));
		return item;
	}
	
	private BitFlags<GroupUpdateOption> toGroupUpdateOption(Long options) {
		if (options == null) {
			return BitFlags.with(
				GroupUpdateOption.USER_ASSOCIATIONS,
				GroupUpdateOption.ROLE_ASSOCIATIONS,
				GroupUpdateOption.PERMISSIONS,
				GroupUpdateOption.SERVICE_PERMISSIONS
			);
		} else {
			return BitFlags.newFrom(GroupUpdateOption.class, options);
		}
	}
	
	private ApiUser createApiUser(User user) {
		return new ApiUser()
			.userId(user.getUserId())
			.userSid(user.getUserSid())
			.enabled(user.getEnabled())
			.displayName(user.getDisplayName())
			.firstName(user.getFirstName())
			.lastName(user.getLastName())
			.assignedGroups(LangUtils.defaultList(user.getAssignedGroups(), null))
			.assignedRoles(LangUtils.defaultList(user.getAssignedRoles(), null))
			.permissions(asApiPermissionStrings(user.getPermissions()))
			.allowedServiceIds(LangUtils.defaultList(user.getAllowedServiceIds(), null));
	}
	
	private UserBase createUserBase(ApiUserAdd body, String domainId, boolean subjectsAsSID) {
		UserBase item = new UserBase();
		item.setEnabled(body.getEnabled());
		item.setDisplayName(body.getDisplayName());
		item.setFirstName(body.getFirstName());
		item.setLastName(body.getLastName());
		item.setAssignedGroups(asSubjects(body.getAssignedGroups(), domainId, subjectsAsSID));
		item.setAssignedRoles(asSubjects(body.getAssignedRoles(), domainId, subjectsAsSID));
		item.setPermissions(asPermissionStrings(body.getPermissions()));
		item.setAllowedServiceIds(LangUtils.defaultSet(body.getAllowedServiceIds(), null));
		return item;
	}
	
	private UserBase createUserBase(ApiUserBase body, String domainId, boolean subjectsAsSID) {
		UserBase item = new UserBase();
		item.setEnabled(body.getEnabled());
		item.setDisplayName(body.getDisplayName());
		item.setFirstName(body.getFirstName());
		item.setLastName(body.getLastName());
		item.setAssignedGroups(asSubjects(body.getAssignedGroups(), domainId, subjectsAsSID));
		item.setAssignedRoles(asSubjects(body.getAssignedRoles(), domainId, subjectsAsSID));
		item.setPermissions(asPermissionStrings(body.getPermissions()));
		item.setAllowedServiceIds(LangUtils.defaultSet(body.getAllowedServiceIds(), null));
		return item;
	}
	
	private BitFlags<UserUpdateOption> toUserUpdateOption(Long options) {
		if (options == null) {
			return BitFlags.with(
				UserUpdateOption.GROUP_ASSOCIATIONS,
				UserUpdateOption.ROLE_ASSOCIATIONS,
				UserUpdateOption.PERMISSIONS,
				UserUpdateOption.SERVICE_PERMISSIONS
			);
		} else {
			return BitFlags.newFrom(UserUpdateOption.class, options);
		}
	}
	
	private ApiResource createApiResource(Resource resource) {
		return new ApiResource()
			.resourceId(resource.getResourceId())
			.resourceSid(resource.getResourceSid())
			.enabled(resource.getEnabled())
			.type(ApiResource.TypeEnum.fromValue(EnumUtils.toSerializedName(resource.getType())))
			.displayName(resource.getDisplayName())
			.email(resource.getEmail())
			.managerSubject(resource.getManagerSubject())
			.allowedSubjects(LangUtils.defaultList(resource.getAllowedSubjects(), null));
	}
	
	private ResourceBase createResourceBase(ApiResourceAdd body, String domainId, boolean subjectsAsSID) {
		ResourceBase item = new ResourceBase();
		item.setEnabled(body.getEnabled());
		item.setType(EnumUtils.forSerializedName(body.getType().value(), ResourceBase.Type.class));
		item.setDisplayName(body.getDisplayName());
		item.setEmail(body.getEmail());
		item.setManagerSubject(asSubject(body.getManagerSubject(), domainId, subjectsAsSID));
		item.setAllowedSubjects(asSubjects(body.getAllowedSubjects(), domainId, subjectsAsSID));
		return item;
	}
	
	private ResourceBase createResourceBase(ApiResourceBase body, String domainId, boolean subjectsAsSID) {
		ResourceBase item = new ResourceBase();
		item.setEnabled(body.getEnabled());
		item.setType(EnumUtils.forSerializedName(body.getType().value(), ResourceBase.Type.class));
		item.setDisplayName(body.getDisplayName());
		item.setEmail(body.getEmail());
		item.setManagerSubject(asSubject(body.getManagerSubject(), domainId, subjectsAsSID));
		item.setAllowedSubjects(asSubjects(body.getAllowedSubjects(), domainId, subjectsAsSID));
		return item;
	}
	
	private BitFlags<ResourceUpdateOption> toResourceUpdateOption(Long options) {
		if (options == null) {
			return BitFlags.with(
				ResourceUpdateOption.PERMISSIONS
			);
		} else {
			return BitFlags.newFrom(ResourceUpdateOption.class, options);
		}
	}
	
	private ApiRole createApiRole(Role role) {
		return new ApiRole()
			.roleId(role.getRoleId())
			.roleSid(role.getRoleSid())
			.description(role.getDescription())
			.permissions(asApiPermissionStrings(role.getPermissions()))
			.allowedServiceIds(LangUtils.defaultList(role.getAllowedServiceIds(), null));
	}
	
	private RoleBase createRoleBase(ApiRoleAdd body, String domainId, boolean subjectsAsSID) {
		RoleBase item = new RoleBase();
		item.setDescription(body.getDescription());
		item.setPermissions(asPermissionStrings(body.getPermissions()));
		item.setAllowedServiceIds(LangUtils.defaultSet(body.getAllowedServiceIds(), null));
		return item;
	}
	
	private RoleBase createRoleBase(ApiRoleBase body, String domainId, boolean subjectsAsSID) {
		RoleBase item = new RoleBase();
		item.setDescription(body.getDescription());
		item.setPermissions(asPermissionStrings(body.getPermissions()));
		item.setAllowedServiceIds(LangUtils.defaultSet(body.getAllowedServiceIds(), null));
		return item;
	}
	
	private BitFlags<RoleUpdateOption> toRoleUpdateOption(Long options) {
		if (options == null) {
			return BitFlags.with(
				//RoleUpdateOption.SUBJECT_ASSOCIATIONS,
				RoleUpdateOption.PERMISSIONS,
				RoleUpdateOption.SERVICE_PERMISSIONS
			);
		} else {
			return BitFlags.newFrom(RoleUpdateOption.class, options);
		}
	}
	
	private LicenseBase createLicenseBase(ApiLicenseAdd body) {
		LicenseBase item = new LicenseBase();
		item.setLicenseString(body.getLicenseString());
		item.setActivatedLicenseString(body.getActivatedLicenseString());
		item.setAutoLease(LangUtils.value(body.getAutoLease(), false));
		return item;
	}
	
	private static final DateTimeFormatter ISO_DATE_FMT = DateTimeUtils.createFormatter("yyyyMMdd", DateTimeZone.UTC);
	private static final DateTimeFormatter ISO_DATETIME_FMT = DateTimeUtils.createFormatter("yyyyMMdd'T'HHmmss'Z'", DateTimeZone.UTC);
	
	private ApiLicense createApiLicense(ServiceLicense license) {
		final LicenseExInfo exInfo = license.getExtendedInfo();
		return new ApiLicense()
			.productCode(license.getProductCode())
			.owningServiceId(license.getOwningServiceId())
			.builtIn(license.getBuiltIn())
			.revisionTimestamp(DateTimeUtils.print(ISO_DATETIME_FMT, license.getRevisionTimestamp()))
			.activationTimestamp(DateTimeUtils.print(ISO_DATETIME_FMT, license.getActivationTimestamp()))
			.activationHwId(license.getActivationHwId())
			.expirationDate(DateTimeUtils.print(ISO_DATE_FMT, license.getExpirationDate()))
			.status(exInfo != null ? exInfo.getStatus().getValue() : null)
			.maxLease(LangUtils.value(license.getQuantity(), -1))
			.leases(createApiLicenseLeaseList(license.getLeases()));
	}
	
	private List<ApiLicenseLease> createApiLicenseLeaseList(Map<String, ServiceLicenseLease> leases) {
		ArrayList<ApiLicenseLease> items = null;
		if (leases != null) {
			items = new ArrayList<>(leases.size());
			for (ServiceLicenseLease lease : leases.values()) {
				items.add(new ApiLicenseLease()
					.userId(lease.getUserId())
					.timestamp(DateTimeUtils.print(ISO_DATETIME_FMT, lease.getLeaseTimestamp()))
				);
			}
		}
		return items;
	}
	
	private ApiLicenseOfflineReqInfo createApiLicenseOfflineReqInfo(ProductLicense.RequestInfo info) {
		return new ApiLicenseOfflineReqInfo()
			.url(info.url)
			.requestString(info.request)
			.hardwareId(info.hardwareId);
	}
	
	private List<String> asApiPermissionStrings(Collection<PermissionString> permissionStrings) {
		if (permissionStrings == null) return null;
		return permissionStrings.stream()
			.map((ps) -> ps.toString())
			.filter((ps) -> ps != null)
			.collect(Collectors.toList());
	}
	
	private Set<PermissionString> asPermissionStrings(Collection<String> strings) {
		if (strings == null) return null;
		return strings.stream()
			.map((s) -> PermissionString.parseQuietly(s))
			.filter((ps) -> ps != null)
			.collect(Collectors.toSet());
	}
	
	private Set<String> asSubjects(Collection<String> strings, String domainId, boolean subjectsAsSID) {
		if (strings == null) return null;
		if (subjectsAsSID) {
			return new LinkedHashSet<>(strings);
		} else {
			Set<String> newStrings = new LinkedHashSet<>(strings.size());
			for (String string : strings) newStrings.add(asSubject(string, domainId, subjectsAsSID));
			return newStrings;
		}
	}
	
	private String asSubject(String string, String domainId, boolean subjectsAsSID) {
		if (subjectsAsSID) {
			return string;
		} else {
			return parseStringAsSubjectProfile(string, domainId);
		}
	}
	
	private String parseStringAsSubjectProfile(String string, String defaultDomainId) {
		if (StringUtils.isBlank(string)) return null;
		final UserProfileId profileId = UserProfileId.parseQuielty(string);
		final String userId = (profileId != null) ? profileId.getUserId() : string;
		return UserProfileId.buildFullName(defaultDomainId, userId);
	}
	
	private ApiResultExceptions createApiResultExceptions(ResultVoid result) {
		ApiResultExceptions item = new ApiResultExceptions();
		item.setExceptions(createApiHomedExceptionList(result));
		return item;
	}
	
	private List<ApiHomedException> createApiHomedExceptionList(Result<?> result) {
		ArrayList<ApiHomedException> list = null;
		if (result.hasExceptions()) {
			list = new ArrayList<>();
			for (HomedThrowable ht : result.getExceptions()) {
				list.add(new ApiHomedException()
					.serviceId(ht.getServiceId())
					.className(ht.getThrowable().getClass().getCanonicalName())
					.message(ht.getThrowable().getMessage()));
			}
		} else {
			list = new ArrayList<>(0);
		}
		return list;
	}

	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
			.code(status.getStatusCode())
			.description(message);
	}
}
