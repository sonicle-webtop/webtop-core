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
import com.sonicle.commons.l4j.DomainBasedProduct;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.l4j.ProductLicense.LicenseObject;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OLicense;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * @author malbinola
 */
public class JsGridDomainLicense {
	
	public String productId;
	public String productDetails;
	public String license;
	public boolean valid;
	
	public JsGridDomainLicense(OLicense o) {
		productId = o.getProductId();
		license = o.getLicense();
		valid = false;
		
		productDetails="[ invalid product id ]";
		String keys[]=productId.split(":");
		if (keys.length==2) {
			ServiceManifest mft=WT.getManifest(keys[0]);
			if (mft!=null) {
				ServiceManifest.Product mftProduct=mft.getProduct(keys[1]);
				try {
					Class clazz=Class.forName(mftProduct.className);
					AbstractProduct product=null;
					boolean perDomain=DomainBasedProduct.class.isAssignableFrom(clazz);
					if (perDomain) {
						product=(AbstractProduct)clazz.getDeclaredConstructor(String.class).newInstance(o.getInternetDomain());
					} else {
						product=(AbstractProduct)Class.forName(mftProduct.className).newInstance();
					}
					ProductLicense pl = new ProductLicense(
							ProductLicense.LicenseType.LICENSE_TEXT,
							ProductLicense.ActivationLicenseType.OFF_NO_ACTIVATION,
							product,
							license
					);
					LicenseObject lo=pl.validate();
					valid=lo.isValid();
					if (valid) {
						LicenseText ltext=lo.getLicense().getLicenseText();
						productDetails=ltext.getLicenseProductName();
						HashMap<String,String> features=ltext.getCustomSignedFeatures();
						for(String key: features.keySet()) {
							String value=features.get(key);
							productDetails+=" - "+key+": "+value;
						}
						if (perDomain) productDetails+=" - domain: "+o.getInternetDomain();
						productDetails+=" - licensed to "+ltext.getUserRegisteredTo();
					} else {
						productDetails="[ invalid license ]";
					}
				} catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exc) {
					productDetails="[ product not found ]";
				}
			}
		}		
	}	
}