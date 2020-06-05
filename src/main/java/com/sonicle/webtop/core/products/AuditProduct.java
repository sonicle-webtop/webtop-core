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

import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;

/**
 *
 * @author gbulfon
 */
public class AuditProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WTP-COR-AUDIT";
	public static final String PRODUCT_NAME = "Audit";
	public static final String PUBLIC_KEY = 
			"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
			"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
			"004d40494481762c4e9810f93837d3264f4c55426614aef41dd887b9ac7G\n" +
			"02818100a40666a215deb320004cd687e300c92855d73155da5f2c7a1fb4\n" +
			"ca9b9090dc37c2dd420be35762a43cdc0b046fa3cde5a068deaa8af01356\n" +
			"1954d63b6531437cd5e4c0e7ca344df0e334572a861e11b0ff89aade050b\n" +
			"60c071c53b5d6c546cfc03RSA4102413SHA512withRSA7132300d18fe9f8\n" +
			"c74a1a5ec945999d64d8cf80fcf7047ce1117160c2e42dc090203010001";
	
	
	
	/*
	public static final String PRODUCT_ID = "audit";
	public static final String PRODUCT_NAME = "Users Activity Audit";
	public static final String PUBLIC_KEY = 
			"30819f300d06092a864886f70d010101050003818d003081893032301006\n"+
			"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n"+
			"0042d4fa0190ca06c8b6aac3b08d87b4bf375b4c91f8db40d7363628246G\n"+
			"02818100815cde722fc927b64632d42af291d695b83ce11dcadae039b3a1\n"+
			"20e03e3d2013b3d4ce990fb9384b8e09884704ec06b957dcf2654646074d\n"+
			"04c15edd2f20a6fea9d576b80a16185b4caecc735bf02f730edaadef8e1c\n"+
			"2341a6903b2a1d9f935d03RSA4102413SHA512withRSA9e223cfa0ece427\n"+
			"ba122b2394500bd81fc1232954e8376d6a1473dc0819beaf50203010001";
	*/
	
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
		//return PUBLIC_KEY;
		return "30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
			"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
			"004d40494481762c4e9810f93837d3264f4c55426614aef41dd887b9ac7G\n" +
			"02818100a40666a215deb320004cd687e300c92855d73155da5f2c7a1fb4\n" +
			"ca9b9090dc37c2dd420be35762a43cdc0b046fa3cde5a068deaa8af01356\n" +
			"1954d63b6531437cd5e4c0e7ca344df0e334572a861e11b0ff89aade050b\n" +
			"60c071c53b5d6c546cfc03RSA4102413SHA512withRSA7132300d18fe9f8\n" +
			"c74a1a5ec945999d64d8cf80fcf7047ce1117160c2e42dc090203010001";
	}
	
	@Override
	public String getLicenseServer() {
		return "http://localhost:28080/algas/";
	}
}
