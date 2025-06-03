/*
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.rest.v1;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.security.ConnectionSecurity;
import com.sonicle.webtop.core.app.model.ApiKey;
import com.sonicle.webtop.core.app.model.ApiKeyBase;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.app.model.DomainUpdateOption;
import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.GroupBase;
import com.sonicle.webtop.core.app.model.GroupUpdateOption;
import com.sonicle.webtop.core.app.model.HomedThrowable;
import com.sonicle.webtop.core.app.model.LdapDirectoryParams;
import com.sonicle.webtop.core.app.model.LicenseBase;
import com.sonicle.webtop.core.app.model.LicenseExInfo;
import com.sonicle.webtop.core.app.model.PermissionString;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceBase;
import com.sonicle.webtop.core.app.model.ResourceUpdateOption;
import com.sonicle.webtop.core.app.model.Role;
import com.sonicle.webtop.core.app.model.RoleBase;
import com.sonicle.webtop.core.app.model.RoleUpdateOption;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserBase;
import com.sonicle.webtop.core.app.model.UserUpdateOption;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.sdk.ResultVoid;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.ServiceLicenseLease;
import com.sonicle.webtop.core.sdk.BaseRestApiUtils;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKey;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKeyBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiDirectoryPasswordPolicies;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomain;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainBaseDirRawParameters;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroup;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiHomedException;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicense;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseLease;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseOfflineReqInfo;
import com.sonicle.webtop.core.swagger.v1.model.ApiResource;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiResultExceptions;
import com.sonicle.webtop.core.swagger.v1.model.ApiRole;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiSettingEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiUser;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserBase;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author malbinola
 */
public class ApiUtils extends BaseRestApiUtils {
	
	public static ApiSettingEntry createApiSettingEntry(com.sonicle.webtop.core.model.SettingEntry setting) {
		return ApiUtils.createApiSettingEntry(setting.getServiceId(), setting.getKey(), setting.getValue());
	}
	
	public static ApiSettingEntry createApiSettingEntry(String serviceId, String key, String value) {
		return new ApiSettingEntry()
			.serviceId(serviceId)
			.key(key)
			.value(value);
	}
	
	public static DomainBase fillDomainBase(final DomainBase tgt, final ApiDomainBase src) throws URISyntaxException {
		tgt.setEnabled(src.getEnabled());
		tgt.setDisplayName(src.getDisplayName());
		tgt.setAuthDomainName(src.getAuthDomainName());
		tgt.setDomainName(src.getDomainName());
		tgt.setUserAutoCreation(src.getUserAutoCreation());
		tgt.setDirUri(URIUtils.createURI(src.getDirUri()));
		tgt.setDirAdmin(src.getDirAdmin());
		tgt.setDirPassword(src.getDirPassword());
		tgt.setDirConnSecurity(EnumUtils.forName(src.getDirConnSecurity(), ConnectionSecurity.class));
		tgt.setDirCaseSensitive(src.getDirCaseSensitive());
		if (LdapDirectoryParams.class.equals(tgt.getDirRawParametersClass())) {
			tgt.writeDirRawParameters(ApiUtils.createLdapDirectoryParams(src.getDirRawParameters()), LdapDirectoryParams.class);
		} else {
			tgt.setDirRawParameters(null);
		}
		tgt.setPasswordPolicies(ApiUtils.createPasswordPolicies(src.getPasswordPolicies()));
		return tgt;
	}
	
	public static LdapDirectoryParams createLdapDirectoryParams(ApiDomainBaseDirRawParameters js) {
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
	
	public static DomainBase.PasswordPolicies createPasswordPolicies(ApiDirectoryPasswordPolicies policies) {
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
	
	public static ApiDomainEntry fillApiDomainEntry(final ApiDomainEntry tgt, final Domain src, final String publicUrl) {
		tgt.setDomainId(src.getDomainId());
		tgt.setEnabled(src.getEnabled());
		tgt.setDisplayName(src.getDisplayName());
		tgt.setAuthDomainName(src.getAuthDomainName());
		tgt.setDomainName(src.getDomainName());
		tgt.setPublicURL(publicUrl);
		tgt.setUserAutoCreation(src.getUserAutoCreation());
		tgt.setDirUri(src.getDirUri().toString());
		return tgt;
	}
	
	public static ApiDomain fillApiDomain(final ApiDomain tgt, final Domain src, final String publicUrl) {
		fillApiDomainBase(tgt, src, publicUrl);
		tgt.setDomainId(src.getDomainId());
		return tgt;
	}
	
	public static ApiDomainBase fillApiDomainBase(final ApiDomainBase tgt, final DomainBase src, final String publicUrl) {
		tgt.setEnabled(src.getEnabled());
		tgt.setDisplayName(src.getDisplayName());
		tgt.setAuthDomainName(src.getAuthDomainName());
		tgt.setDomainName(src.getDomainName());
		tgt.setPublicURL(publicUrl);
		tgt.setUserAutoCreation(src.getUserAutoCreation());
		tgt.setDirUri(src.getDirUri().toString());
		tgt.setDirAdmin(src.getDirAdmin());
		tgt.setDirPassword(src.getDirPassword());
		tgt.setDirConnSecurity(ApiDomain.DirConnSecurityEnum.fromValue(EnumUtils.getName(src.getDirConnSecurity(), ApiDomain.DirConnSecurityEnum.OFF.name())));
		tgt.setDirCaseSensitive(src.getDirCaseSensitive());
		tgt.setDirRawParameters(ApiUtils.createApiDomainBaseDirRawParameters(src));
		tgt.setPasswordPolicies(ApiUtils.createApiDirectoryPasswordPolicies(src.getPasswordPolicies()));
		return tgt;
	}
	
	public static ApiDomainBaseDirRawParameters createApiDomainBaseDirRawParameters(DomainBase domain) {
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
	
	public static ApiDirectoryPasswordPolicies createApiDirectoryPasswordPolicies(DomainBase.PasswordPolicies policies) {
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
	
	public static BitFlags<DomainUpdateOption> toDomainUpdateOption(Long options) {
		if (options == null) {
			return BitFlags.noneOf(DomainUpdateOption.class);
		} else {
			return BitFlags.newFrom(DomainUpdateOption.class, options);
		}
	}
	
	public static GroupBase fillGroupBase(final GroupBase tgt, final ApiGroupBase src, final String domainId, final boolean subjectsAsSID) {
		tgt.setDescription(src.getDescription());
		tgt.setAssignedUsers(asSubjects(src.getAssignedUsers(), domainId, subjectsAsSID));
		tgt.setAssignedRoles(asSubjects(src.getAssignedRoles(), domainId, subjectsAsSID));
		tgt.setPermissions(asPermissionStrings(src.getPermissions()));
		tgt.setAllowedServiceIds(LangUtils.defaultSet(src.getAllowedServiceIds(), null));
		return tgt;
	}
	
	public static ApiGroup fillApiGroup(final ApiGroup tgt, final Group src) {
		fillApiGroupBase(tgt, src);
		tgt.setGroupId(src.getGroupId());
		tgt.setGroupSid(src.getGroupSid());
		tgt.setBuiltIn(src.isBuiltIn());
		return tgt;
	}
	
	public static ApiGroupBase fillApiGroupBase(final ApiGroupBase tgt, final GroupBase src) {
		tgt.setDescription(src.getDescription());
		tgt.setAssignedUsers(LangUtils.defaultList(src.getAssignedUsers(), null));
		tgt.setAssignedRoles(LangUtils.defaultList(src.getAssignedRoles(), null));
		tgt.setPermissions(asApiPermissionStrings(src.getPermissions()));
		tgt.setAllowedServiceIds(LangUtils.defaultList(src.getAllowedServiceIds(), null));
		return tgt;
	}
	
	public static BitFlags<GroupUpdateOption> toGroupUpdateOption(Long options) {
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
	
	public static UserBase fillUserBase(final UserBase tgt, final ApiUserBase src, final String domainId, final boolean subjectsAsSID) {
		tgt.setEnabled(src.getEnabled());
		tgt.setDisplayName(src.getDisplayName());
		tgt.setFirstName(src.getFirstName());
		tgt.setLastName(src.getLastName());
		tgt.setAssignedGroups(asSubjects(src.getAssignedGroups(), domainId, subjectsAsSID));
		tgt.setAssignedRoles(asSubjects(src.getAssignedRoles(), domainId, subjectsAsSID));
		tgt.setPermissions(asPermissionStrings(src.getPermissions()));
		tgt.setAllowedServiceIds(LangUtils.defaultSet(src.getAllowedServiceIds(), null));
		return tgt;
	}
	
	public static ApiUser fillApiUser(final ApiUser tgt, final User src) {
		fillApiUserBase(tgt, src);
		tgt.setUserId(src.getUserId());
		tgt.setUserSid(src.getUserSid());
		return tgt;
	}
	
	public static ApiUserBase fillApiUserBase(final ApiUserBase tgt, final UserBase src) {
		tgt.setEnabled(src.getEnabled());
		tgt.setDisplayName(src.getDisplayName());
		tgt.setFirstName(src.getFirstName());
		tgt.setLastName(src.getLastName());
		tgt.setAssignedGroups(LangUtils.defaultList(src.getAssignedGroups(), null));
		tgt.setAssignedRoles(LangUtils.defaultList(src.getAssignedRoles(), null));
		tgt.setPermissions(asApiPermissionStrings(src.getPermissions()));
		tgt.setAllowedServiceIds(LangUtils.defaultList(src.getAllowedServiceIds(), null));
		return tgt;
	}
	
	public static BitFlags<UserUpdateOption> toUserUpdateOption(Long options) {
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
	
	public static RoleBase fillRoleBase(final RoleBase tgt, final ApiRoleBase src, final String domainId, final boolean subjectsAsSID) {
		tgt.setDescription(src.getDescription());
		tgt.setPermissions(asPermissionStrings(src.getPermissions()));
		tgt.setAllowedServiceIds(LangUtils.defaultSet(src.getAllowedServiceIds(), null));
		return tgt;
	}
	
	public static ApiRole fillApiRole(final ApiRole tgt, final Role src) {
		fillApiRoleBase(tgt, src);
		tgt.setRoleId(src.getRoleId());
		tgt.setRoleSid(src.getRoleSid());
		return tgt;
	}
	
	public static ApiRoleBase fillApiRoleBase(final ApiRoleBase tgt, final RoleBase src) {
		tgt.setDescription(src.getDescription());
		tgt.setPermissions(asApiPermissionStrings(src.getPermissions()));
		tgt.setAllowedServiceIds(LangUtils.defaultList(src.getAllowedServiceIds(), null));
		return tgt;
	}
	
	public static BitFlags<RoleUpdateOption> toRoleUpdateOption(Long options) {
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
	
	public static ResourceBase fillResourceBase(final ResourceBase tgt, final String domainId, final ApiResourceBase src, final boolean subjectsAsSID) {
		tgt.setEnabled(src.getEnabled());
		tgt.setType(EnumUtils.forSerializedName(src.getType().value(), ResourceBase.Type.class));
		tgt.setDisplayName(src.getDisplayName());
		tgt.setEmail(src.getEmail());
		tgt.setManagerSubject(asSubject(src.getManagerSubject(), domainId, subjectsAsSID));
		tgt.setAllowedSubjects(asSubjects(src.getAllowedSubjects(), domainId, subjectsAsSID));
		return tgt;
	}
	
	public static ApiResource fillApiResource(final ApiResource tgt, final Resource src) {
		fillApiResourceBase(tgt, src);
		tgt.setResourceId(src.getResourceId());
		tgt.setResourceSid(src.getResourceSid());
		return tgt;
	}
	
	public static ApiResourceBase fillApiResourceBase(final ApiResourceBase tgt, final ResourceBase src) {
		tgt.setEnabled(src.getEnabled());
		tgt.setType(ApiResource.TypeEnum.fromValue(EnumUtils.toSerializedName(src.getType())));
		tgt.setDisplayName(src.getDisplayName());
		tgt.setEmail(src.getEmail());
		tgt.setManagerSubject(src.getManagerSubject());
		tgt.setAllowedSubjects(LangUtils.defaultList(src.getAllowedSubjects(), null));
		return tgt;
	}
	
	public static BitFlags<ResourceUpdateOption> toResourceUpdateOption(Long options) {
		if (options == null) {
			return BitFlags.with(
				ResourceUpdateOption.PERMISSIONS
			);
		} else {
			return BitFlags.newFrom(ResourceUpdateOption.class, options);
		}
	}
	
	public static LicenseBase fillLicenseBase(final LicenseBase tgt, final ApiLicenseBase src) {
		tgt.setLicenseString(src.getLicenseString());
		tgt.setActivatedLicenseString(src.getActivatedLicenseString());
		tgt.setAutoLease(LangUtils.value(src.getAutoLease(), false));
		return tgt;
	}
	
	public static ApiLicense fillApiLicense(final ApiLicense tgt, final ServiceLicense src) {
		fillApiLicenseBase(tgt, src);
		final LicenseExInfo exInfo = src.getExtendedInfo();
		tgt.setProductCode(src.getProductCode());
		tgt.setOwningServiceId(src.getOwningServiceId());
		tgt.setBuiltIn(src.getBuiltIn());
		tgt.setRevisionTimestamp(JodaTimeUtils.printISO(src.getRevisionTimestamp()));
		tgt.setActivationTimestamp(JodaTimeUtils.printISO( src.getActivationTimestamp()));
		tgt.setActivationHwId(src.getActivationHwId());
		tgt.setExpirationDate(JodaTimeUtils.print(JodaTimeUtils.ISO_LOCALDATE_FMT, src.getExpirationDate()));
		tgt.setStatus(exInfo != null ? exInfo.getStatus().getValue() : null);
		tgt.setMaxLease(LangUtils.value(src.getQuantity(), -1));
		tgt.setLeases(ApiUtils.createApiLicenseLeaseList(src.getLeases()));
		return tgt;
	}
	
	public static ApiLicenseBase fillApiLicenseBase(final ApiLicenseBase tgt, final ServiceLicense src) {
		tgt.setLicenseString(src.getLicenseString());
		tgt.setActivatedLicenseString(src.getActivatedLicenseString());
		tgt.setAutoLease(src.getAutoLease());
		return tgt;
	}
	
	public static List<ApiLicenseLease> createApiLicenseLeaseList(final Map<String, ServiceLicenseLease> leases) {
		ArrayList<ApiLicenseLease> items = null;
		if (leases != null) {
			items = new ArrayList<>(leases.size());
			for (ServiceLicenseLease lease : leases.values()) {
				items.add(new ApiLicenseLease()
					.userId(lease.getUserId())
					.timestamp(JodaTimeUtils.printISO(lease.getLeaseTimestamp()))
				);
			}
		}
		return items;
	}
	
	public static ApiLicenseOfflineReqInfo createApiLicenseOfflineReqInfo(final ProductLicense.RequestInfo info) {
		return new ApiLicenseOfflineReqInfo()
			.url(info.url)
			.requestString(info.request)
			.hardwareId(info.hardwareId);
	}
	
	public static ApiKeyBase fillApiKeyBase(final ApiKeyBase tgt, final Set<String> fields2set, final ApiApiKeyBase src) {
		if (shouldSet(fields2set, "name")) tgt.setName(src.getName());
		if (shouldSet(fields2set, "description")) tgt.setDescription(src.getDescription());
		if (shouldSet(fields2set, "expiresAt")) tgt.setExpiresAt(JodaTimeUtils.parseDateTimeISO(src.getExpiresAt()));
		return tgt;
	}
	
	public static ApiApiKey fillApiKey(final ApiApiKey tgt, final Set<String> fields2set, final ApiKey src) {
		fillApiKeyBase(tgt, fields2set, src);
		tgt.id(String.valueOf(src.getApiKeyId()));
		tgt.createdAt(JodaTimeUtils.print(JodaTimeUtils.ISO_DATETIME_FMT, src.getCreationTimestamp()));
		tgt.updatedAt(JodaTimeUtils.print(JodaTimeUtils.ISO_DATETIME_FMT, src.getRevisionTimestamp()));
		return tgt;
	}
	
	public static ApiApiKeyBase fillApiKeyBase(final ApiApiKeyBase tgt, final Set<String> fields2set, final ApiKeyBase src) {
		if (shouldSet(fields2set, "name")) tgt.setName(src.getName());
		if (shouldSet(fields2set, "description")) tgt.setDescription(src.getDescription());
		if (shouldSet(fields2set, "shortToken")) tgt.setShortToken(src.getShortToken());
		if (shouldSet(fields2set, "longToken")) tgt.setLongToken(src.getLongToken());
		if (shouldSet(fields2set, "expiresAt")) tgt.setExpiresAt(JodaTimeUtils.printISO(src.getExpiresAt()));
		return tgt;
	}
	
	public static List<String> asApiPermissionStrings(final Collection<PermissionString> permissionStrings) {
		if (permissionStrings == null) return null;
		return permissionStrings.stream()
			.map((ps) -> ps.toString())
			.filter((ps) -> ps != null)
			.collect(Collectors.toList());
	}
	
	public static Set<PermissionString> asPermissionStrings(final Collection<String> strings) {
		if (strings == null) return null;
		return strings.stream()
			.map((s) -> PermissionString.parseQuietly(s))
			.filter((ps) -> ps != null)
			.collect(Collectors.toSet());
	}
	
	public static Set<String> asSubjects(final Collection<String> strings, final String domainId, final boolean subjectsAsSID) {
		if (strings == null) return null;
		if (subjectsAsSID) {
			return new LinkedHashSet<>(strings);
		} else {
			Set<String> newStrings = new LinkedHashSet<>(strings.size());
			for (String string : strings) newStrings.add(asSubject(string, domainId, subjectsAsSID));
			return newStrings;
		}
	}
	
	public static String asSubject(final String string, final String domainId, final boolean subjectsAsSID) {
		if (subjectsAsSID) {
			return string;
		} else {
			return parseStringAsSubjectProfile(string, domainId);
		}
	}
	
	public static String parseStringAsSubjectProfile(final String string, final String defaultDomainId) {
		if (StringUtils.isBlank(string)) return null;
		final UserProfileId profileId = UserProfileId.parseQuielty(string);
		final String userId = (profileId != null) ? profileId.getUserId() : string;
		return UserProfileId.buildFullyQualifiedName(defaultDomainId, userId);
	}
	
	public static ApiResultExceptions createApiResultExceptions(final ResultVoid result) {
		ApiResultExceptions item = new ApiResultExceptions();
		item.setExceptions(ApiUtils.createApiHomedExceptionList(result));
		return item;
	}
	
	public static List<ApiHomedException> createApiHomedExceptionList(final Result<?> result) {
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
}
