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
package com.sonicle.webtop.core.bol.js;

import com.license4j.LicenseText;
import com.sonicle.commons.l4j.AbstractProduct;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.l4j.ProductLicense.LicenseObject;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.util.ProductUtils;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.sdk.BaseDomainServiceProduct;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import java.util.HashMap;

/**
 *
 * @author malbinola
 */
public class JsGridDomainLicense {
	public String serviceId;
	public String productId;
	public String productDetails;
	public String license;
	public boolean valid;
	
	public JsGridDomainLicense(ServiceLicense license) {
		serviceId = license.getServiceId();
		productId = license.getProductId();
		this.license = license.getLicenseText();
		valid = false;
		productDetails = buildDetails(license);
	}
	
	private String buildDetails(ServiceLicense license) {
		ServiceManifest manifest = WT.getManifest(license.getServiceId());
		if (manifest == null) return "[ product not found ]";
		ServiceManifest.Product manifestProduct = manifest.getProduct(license.getProductId());
		if (manifestProduct == null) return "[ invalid product id ]";
		AbstractProduct product = ProductUtils.getProduct(manifestProduct.className, license.getInternetName());
		if (product == null) return "[ product not found ]";
		
		boolean perDomain=(product instanceof BaseDomainServiceProduct);
		ProductLicense pl = new ProductLicense(
				ProductLicense.LicenseType.LICENSE_TEXT,
				ProductLicense.ActivationLicenseType.OFF_NO_ACTIVATION,
				product,
				license.getLicenseText()
		);
		
		LicenseObject lo=pl.validate();
		valid=lo.isValid();
		if (valid) {
			LicenseText ltext=lo.getLicense().getLicenseText();
			String s=product.getProductName();
			HashMap<String,String> features=ltext.getCustomSignedFeatures();
			for(String key: features.keySet()) {
				String value=features.get(key);
				s+=" - "+key+": "+value;
			}
			if (perDomain) s+=" - domain: "+license.getInternetName();
			s+=" - licensed to "+ltext.getUserRegisteredTo();
			return s;
		} else {
			return "[ invalid license ]";
		}
	}
}
