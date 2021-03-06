/*
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OLicense;
import com.sonicle.webtop.core.bol.OLicenseLease;
import com.sonicle.webtop.core.bol.OMessageQueue;
import com.sonicle.webtop.core.model.DomainEntity;
import com.sonicle.webtop.core.model.License;
import com.sonicle.webtop.core.model.ParamsLdapDirectory;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.ServiceLicenseLease;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import com.sonicle.webtop.core.sdk.UserProfileId;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author malbinola
 */
public class AppManagerUtils {
	
	static <T extends OMessageQueue> T fillOMessageQueue(T tgt, UserProfileId profileId, String messageType, String messageData) {
		if ((tgt != null)) {
			tgt.setDomainId(profileId.getDomainId());
			tgt.setUserId(profileId.getUserId());
			tgt.setMessageType(messageType);
			tgt.setMessageRaw(messageData);
		}
		return tgt;
	}
	
	static <T extends DomainEntity> T fillDomainEntity(T tgt, ODomain src) throws URISyntaxException {
		if ((tgt != null) && (src != null)) {
			tgt.setDomainId(src.getDomainId());
			tgt.setInternetName(src.getInternetName());
			tgt.setEnabled(src.getEnabled());
			tgt.setDescription(src.getDescription());
			tgt.setUserAutoCreation(src.getUserAutoCreation());
			tgt.setDirUri(new URI(src.getDirUri()));
			tgt.setDirAdmin(src.getDirAdmin());
			tgt.setDirPassword(src.getDirPassword());
			tgt.setDirConnSecurity(EnumUtils.getEnum(src.getDirConnectionSecurity(), com.sonicle.security.ConnectionSecurity.class));
			tgt.setDirCaseSensitive(src.getDirCaseSensitive());
			String scheme = tgt.getDirUri().getScheme();
			if (scheme.equals(com.sonicle.security.auth.directory.LdapDirectory.SCHEME) || scheme.equals(com.sonicle.security.auth.directory.ADDirectory.SCHEME) || scheme.equals(com.sonicle.security.auth.directory.LdapNethDirectory.SCHEME)) {
				tgt.setDirParameters(LangUtils.deserialize(src.getDirParameters(), new ParamsLdapDirectory(), ParamsLdapDirectory.class));
			} else {
				tgt.setDirParameters(null);
			}
			tgt.setPasswordPolicies(createDomainPasswordPolicies(src));
		}
		return tgt;
	}
	
	static DomainEntity.PasswordPolicies createDomainPasswordPolicies(ODomain src) {
		if (src != null) {
			return new DomainEntity.PasswordPolicies(
				src.getDirPwdPolicyMinLength(),
				src.getDirPwdPolicyComplexity(),
				src.getDirPwdPolicyAvoidConsecutiveChars(),
				src.getDirPwdPolicyAvoidOldSimilarity(),
				src.getDirPwdPolicyAvoidUsernameSimilarity(),
				src.getDirPwdPolicyExpiration(),
				src.getDirPwdPolicyVerifyAtLogin()
			);
		}
		return null;
	}
	
	static <T extends ODomain> T fillODomain(T tgt, DomainEntity.PasswordPolicies src) {
		if ((tgt != null) && (src != null)) {
			tgt.setDirPwdPolicyComplexity(src.getComplexity());
			tgt.setDirPwdPolicyMinLength(src.getMinLength());
			tgt.setDirPwdPolicyAvoidConsecutiveChars(src.getAvoidConsecutiveChars());
			tgt.setDirPwdPolicyAvoidOldSimilarity(src.getAvoidOldSimilarity());
			tgt.setDirPwdPolicyAvoidUsernameSimilarity(src.getAvoidUsernameSimilarity());
			tgt.setDirPwdPolicyExpiration(src.getExpiration());
			tgt.setDirPwdPolicyVerifyAtLogin(src.getVerifyAtLogin());
		}
		return tgt;
	}
	
	static ServiceLicense createServiceLicense(OLicense src) {
		if (src == null) return null;
		return fillServiceLicense(new ServiceLicense(), src);
	}
	
	static ServiceLicense createServiceLicense(OLicense src, Collection<OLicenseLease> oleases) {
		if (src == null) return null;
		ServiceLicense tgt = fillServiceLicense(new ServiceLicense(), src);
		tgt.setLeases(AppManagerUtils.createServiceLicenseLeaseList(oleases));
		return tgt;
	}
	
	static ServiceLicense createBuiltInServiceLicense(String domainId, BaseServiceProduct product) {
		ServiceLicense tgt = new ServiceLicense();
		tgt.setBuiltIn(true);
		tgt.setDomainId(domainId);
		tgt.setProductId(product.getProductId());
		tgt.setLicenseString(product.getBuiltInLicenseString());
		tgt.setRevisionTimestamp(null);
		tgt.setActivatedLicenseString(null);
		tgt.setActivationTimestamp(null);
		tgt.setActivationHwId(null);
		tgt.setExpirationDate(null);
		tgt.setQuantity(null);
		tgt.setAutoLease(false);
		return tgt;
	}
	
	static <T extends ServiceLicense> T fillServiceLicense(T tgt, OLicense src) {
		if ((tgt != null) && (src != null)) {
			tgt.setBuiltIn(false);
			tgt.setDomainId(src.getDomainId());
			tgt.setProductId(ProductId.build(src.getServiceId(), src.getProductCode()));
			tgt.setLicenseString(src.getString());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setActivatedLicenseString(src.getActivatedString());
			tgt.setActivationTimestamp(src.getActivationTimestamp());
			tgt.setActivationHwId(src.getActivationHwId());
			tgt.setExpirationDate(src.getExpirationDate());
			tgt.setQuantity(src.getQuantity());
			tgt.setAutoLease(src.getAutoLease());
		}
		return tgt;
	}
	
	static Map<String, ServiceLicenseLease> createServiceLicenseLeaseMap(Collection<OLicenseLease> items) {
		LinkedHashMap<String, ServiceLicenseLease> map = new LinkedHashMap<>(items.size());
		for (OLicenseLease item : items) {
			map.put(item.getUserId(), fillServiceLicenseLease(new ServiceLicenseLease(), item));
		}
		return map;
	}
	
	static List<ServiceLicenseLease> createServiceLicenseLeaseList(Collection<OLicenseLease> items) {
		ArrayList<ServiceLicenseLease> list = new ArrayList<>(items.size());
		for (OLicenseLease item : items) {
			list.add(fillServiceLicenseLease(new ServiceLicenseLease(), item));
		}
		return list;
	}
	
	static OLicense createOLicense(License src) {
		if (src == null) return null;
		return fillOLicense(new OLicense(), src);
	}
	
	static <T extends OLicense, S extends License> T fillOLicense(T tgt, S src) {
		if ((tgt != null) && (src != null)) {
			tgt.setDomainId(src.getDomainId());
			tgt.setServiceId(src.getProductId().getServiceId());
			tgt.setProductCode(src.getProductId().getProductCode());
			tgt.setString(src.getLicenseString());
			tgt.setActivatedString(src.getActivatedLicenseString());
			tgt.setAutoLease(src.getAutoLease());
		}
		return tgt;
	}
	
	static <T extends ServiceLicenseLease> T fillServiceLicenseLease(T tgt, OLicenseLease src) {
		if ((tgt != null) && (src != null)) {
			tgt.setUserId(src.getUserId());
			tgt.setLeaseTimestamp(src.getLeaseTimestamp());
			tgt.setLeaseOrigin(EnumUtils.forSerializedName(src.getLeaseOrigin(), ServiceLicenseLease.LeaseOrigin.class));
		}
		return tgt;
	}
}
