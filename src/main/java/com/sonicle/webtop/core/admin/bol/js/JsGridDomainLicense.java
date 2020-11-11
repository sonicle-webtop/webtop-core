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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.l4j.AbstractProduct;
import com.sonicle.commons.l4j.HardwareID;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.l4j.ProductLicense.LicenseInfo;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.util.ProductUtils;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.ServiceLicenseLease;
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
	public String serviceId;
	public String productCode;
	public String productName;
	public boolean valid;
	public boolean activated;
	public boolean expired;
	public String expiry;
	public boolean expireSoon;
	public String hwId;
	public String regTo;
	public Boolean autoLease;
	public Integer maxLease;
	public Integer leasesCount;
	public ArrayList<Lease> leases;
	
	public JsGridDomainLicense(ServiceLicense license, DateTimeZone profileTz) {
		id = license.getProductId().toString();
		serviceId = license.getProductId().getServiceId();
		productCode = license.getProductId().getProductCode();
		
		AbstractProduct prod = ProductUtils.getProduct(license.getProductId(), license.getDomainId());
		if (prod != null) productName = prod.getProductName();
		ProductLicense prodLic = prod != null ? new ProductLicense(prod) : null;
		
		valid = false;
		activated = false;
		expired = false;
		expiry = null;
		expireSoon = false;
		maxLease = -1;
		
		if (prodLic != null) {
			prodLic.setLicenseString(license.getLicenseString());
			prodLic.setActivationCustomHardwareId(LangUtils.joinStrings("!", HardwareID.getHardwareIDFromHostName(), HardwareID.getHardwareIDFromEthernetAddress(true)));
			prodLic.setActivatedLicenseString(license.getActivatedLicenseString());
			
			LicenseInfo li = prodLic.validate(true);
			LicenseInfo ali = prodLic.validate(false);
			
			valid = (li.getLicenseID() == ali.getLicenseID()) && ali.isValid();
			activated = ali.isActivationCompleted();
			expired = li.isExpired();
			if (valid || expired) {
				LocalDate expiryDate = li.getExpirationDate();
				if (expiryDate != null) expiry = DateTimeUtils.createYmdFormatter(profileTz).print(expiryDate);
				expireSoon = li.isExpiringSoon();
				hwId = li.getHardwareID();
				regTo = buildRegTo(li);
			}
			if (li.getQuantity() != null) maxLease = li.getQuantity();
		}
		
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
	
	private String buildRegTo(LicenseInfo li) {
		String s = li.getUserRegisteredTo();
		if (!StringUtils.isBlank(li.getUserCompany())) {
			s += " (" + li.getUserCompany() + ")";
		}
		return s;
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
