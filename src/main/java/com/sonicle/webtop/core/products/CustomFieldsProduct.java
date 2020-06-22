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
package com.sonicle.webtop.core.products;

import com.sonicle.webtop.core.sdk.BaseServiceProduct;

/**
 *
 * @author malbinola
 */
public class CustomFieldsProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WT-CORE-CFIELDS";
	public static final String PRODUCT_NAME = "Custom Fields";
	public static final String PUBLIC_KEY = 
		"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
		"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
		"00459696f1dd2b75261d8fe1d6b5b19514adbc920382ffc8cf2074e4ab1G\n" +
		"02818100b74232962fc976c7d47ede33d7d987325dc6272b0eb0535e8efd\n" +
		"e775bf2bb11f75daae3713498ce01986a29c1cbea5a936de7545bbdabe69\n" +
		"7f74c833fbda547f25ba1406c0a0b89e31251cb71a51b5998744dd52845d\n" +
		"b91cd8993bda3a14309603RSA4102413SHA512withRSAf25637fba4b5cde\n" +
		"d6dbfbb9c37a3a9c88a3972b5c1bd6c2519b7fd606cc1b4ad0203010001";
	public static final String LICENSE_SERVER = null;
	
	public CustomFieldsProduct(String domainInternetName) {
		super(domainInternetName);
	}
	
	@Override
	public String getProductCode() {
		return PRODUCT_ID;
	}
	
	@Override
	public String getProductName() {
		return PRODUCT_NAME;
	}
	
	@Override
	public String getPublicKey() {
		return PUBLIC_KEY;
	}

	@Override
	public String getLicenseServer() {
		return LICENSE_SERVER;
	}
}
