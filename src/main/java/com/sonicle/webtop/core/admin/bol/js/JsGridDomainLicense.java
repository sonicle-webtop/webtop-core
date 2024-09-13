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
package com.sonicle.webtop.core.admin.bol.js;

import com.sonicle.commons.FlagUtils;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.l4j.ProductLicense.LicenseInfo;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.ProductRegistry;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.ServiceLicenseLease;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import java.util.ArrayList;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.jooq.tools.StringUtils;

/**
 *
 * @author malbinola
 */
public class JsGridDomainLicense {
	public String id;
	public boolean builtIn;
	public String serviceId;
	public String productCode;
	public String productName;
	public long status;
	public String expiry;
	public String hwId;
	public String regTo;
	public Boolean autoLease;
	public Integer maxLease;
	public Integer leasesCount;
	public Boolean quantityTypeUsers;
	public String quantityType;
	public ArrayList<Lease> leases;
	
	public JsGridDomainLicense(String domainId, ServiceLicense license, DateTimeZone profileTz) {
		id = ProductId.build(license.getOwningServiceId(), license.getProductCode()).toString();
		builtIn = license.getBuiltIn();
		serviceId = license.getOwningServiceId();
		productCode = license.getProductCode();
		productName = license.getProductCode();
		BaseServiceProduct bsp = ProductRegistry.getInstance().getServiceProduct(productCode, domainId);
		ProductRegistry.ProductEntry pe = ProductRegistry.getInstance().getProduct(productCode);
		if (pe != null) productName = pe.getName();
		
		status = 0;
		expiry = null;
		maxLease = -1;
		
		if (license.getExtendedInfo() != null) {
			status = license.getExtendedInfo().getStatus().getValue();
			hwId = license.getExtendedInfo().getHardwareID();
			regTo = license.getExtendedInfo().getRegisteredTo();
		}
		
		LocalDate expiryDate = license.getExpirationDate();
		if (expiryDate != null) expiry = DateTimeUtils.createYmdFormatter(profileTz).print(expiryDate);
		quantityTypeUsers = bsp.isLicenseQuantityTypeUsers();
		quantityType = bsp.getLicenseQuantityType();
		if (license.getQuantity() != null) maxLease = license.getQuantity();
		
		autoLease = license.getAutoLease();
		DateTimeFormatter dmyHmsFmt = DateTimeUtils.createYmdHmsFormatter(profileTz);
		leases = new ArrayList<>();
		for (ServiceLicenseLease lease : license.getLeases().values()) {
			Lease jsl = new Lease();
			jsl.userId = lease.getUserId();
			jsl.leasedOn = DateTimeUtils.print(dmyHmsFmt, lease.getLeaseTimestamp());
			jsl.origin = lease.getLeaseOrigin();
			leases.add(jsl);
		}
		leasesCount = leases.size();
	}
	
	public static class Lease {
		public String userId;
		public String leasedOn;
		public ServiceLicenseLease.LeaseOrigin origin;
	}
	
	public static class List extends ArrayList<JsGridDomainLicense> {
		public List() {
			super();
		}
	}
}
