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
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.l4j.AbstractProduct;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.model.ProductId;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class BaseServiceProduct extends AbstractProduct {
	public final String SERVICE_ID;
	protected final String domainId;
	protected final HardwareIDSource hwIdSource;
	
	public BaseServiceProduct(String domainId, HardwareIDSource hwIdSource) {
		super(createHardwareIdString(domainId, hwIdSource));
		SERVICE_ID = WT.findServiceId(this.getClass());
		this.domainId = domainId;
		this.hwIdSource = hwIdSource;
	}
	
	public String getDomainId() {
		return domainId;
	}
	
	public ProductId getProductId() {
		return ProductId.build(SERVICE_ID, this.getProductCode());
	}
	
	@Override
	public ProductLicense.LicenseType getLicenseType() {
		return ProductLicense.LicenseType.LICENSE_TEXT;
	}

	@Override
	public ProductLicense.ActivationLicenseType getActivationReturnType() {
		return ProductLicense.ActivationLicenseType.LICENSE_TEXT;
	}
	
	@Override
	public String getInternalHiddenString() {
		return null;
	}
	
	@Override
	public String getLicenseServer() {
		return null;
	}

	@Override
	public String getBuiltInLicenseString() {
		return null;
	}
	
	public final String getBuiltInHardwareId() {
		return createHardwareIdString(domainId, HardwareIDSource.DOMAIN_ID);
	}
	
	private static String createHardwareIdString(String domainId, HardwareIDSource hwIdSource) {
		String s = null;
		if (HardwareIDSource.DOMAIN_ID.equals(hwIdSource)) {
			s = domainId;
		} else if (HardwareIDSource.DOMAIN_INTERNET_NAME.equals(hwIdSource)) {
			s = WT.getPrimaryDomainName(domainId);
		}
		return StringUtils.replace(s, ".", "-");
	}
	
	protected static enum HardwareIDSource {
		NONE, DOMAIN_ID, DOMAIN_INTERNET_NAME
	}
}
