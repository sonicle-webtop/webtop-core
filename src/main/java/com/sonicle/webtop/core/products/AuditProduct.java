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
 * @author gbulfon
 */
public class AuditProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WT-CORE-AUDIT";
	public static final String PRODUCT_NAME = "Audit";
	public static final String PUBLIC_KEY = 
		"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
		"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
		"004a05c798a1abd2ca69b06e04a6dfc3b73d93a085af9d3c315cadc71afG\n" +
		"02818100c62569c6238817532b0577532b05f19ed4d69861c5115bd7a6b9\n" +
		"9a0b988ad6b5cfdf5466bd45cb1122d847a0959e373efe48cb04285b65fa\n" +
		"7c46671f148c3b5b45919ec0b5db5ea8e19a5fcca4e105171ed68023f838\n" +
		"394fa0c54fe3dd70ac0d03RSA4102413SHA512withRSA588acfd5fb4c60e\n" +
		"16de7585674878abb7cb38511a07ec76f8d9542be987005230203010001";
	public static final String LICENSE_SERVER = null;
	
	public AuditProduct(String domainInternetName) {
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
