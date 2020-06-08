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
	public static final String PRODUCT_ID = "SNCL-WTP-COR-CUSFL";
	public static final String PRODUCT_NAME = "Custom Fields";
	public static final String PUBLIC_KEY = 
			"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
			"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
			"0043f22e44f9367e953712d59fe49ef8bea9e4b31719b616be528a6a4d3G\n" +
			"02818100aff87d2fe3c275bb5a2ac28b4a8ddac5524504e299af0f3dcc9e\n" +
			"51581305ace7639daf2b7fb4010c3f35ffe5286e81de930b63af2f09f521\n" +
			"f7803d3ae06091f15cd14bd5a2c38f5cfbee11932420c9eeb1c28357b7d4\n" +
			"2d8d137393c6b467c6bd03RSA4102413SHA512withRSA73ce2e85464c965\n" +
			"7230a680aad116804e1d7242ccf8e18e2f6f2a52880222783020301000";
	
	/*
	public static final String PRODUCT_ID = "SNCLCORECUSTOMFIELDS";
	public static final String PRODUCT_NAME = "Custom Fields";
	public static final String PUBLIC_KEY = 
			"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
			"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
			"00408de5495896cd8825f418d27c3ac7e6b09548bd717a45ba68a37d26eG\n" +
			"02818100b7c7019cbc5b61614f24873955d87f022e4de7c1db94a4b97ffb\n" +
			"bad8aa53a9cb9a1d0e08ad9e297f914bef6c9686c46140f85fb24a88b447\n" +
			"45d6f9d158b588b19a4a4abcf311ea56786ea9ba69ef7ded5131408f53e1\n" +
			"a05e830ac6ea6d0f902b03RSA4102413SHA512withRSA74e9379bbd23eb8\n" +
			"b529091803fffa6c76818cc1710e46ae00b61d2a4c60d5f850203010001";
	*/
	
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
		return "30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
"0043f22e44f9367e953712d59fe49ef8bea9e4b31719b616be528a6a4d3G\n" +
"02818100aff87d2fe3c275bb5a2ac28b4a8ddac5524504e299af0f3dcc9e\n" +
"51581305ace7639daf2b7fb4010c3f35ffe5286e81de930b63af2f09f521\n" +
"f7803d3ae06091f15cd14bd5a2c38f5cfbee11932420c9eeb1c28357b7d4\n" +
"2d8d137393c6b467c6bd03RSA4102413SHA512withRSA73ce2e85464c965\n" +
"7230a680aad116804e1d7242ccf8e18e2f6f2a528802227830203010001";
	}

	@Override
	public String getLicenseServer() {
		return "http://localhost:18080/algas/";
	}
}
