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
public final class MailBridgeProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WT-CORE-MAILBRIDGE";
	public static final String PRODUCT_NAME = "MailBridge";
	public static final String PUBLIC_KEY =
		"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
		"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
		"0047bf692e4901b01ef6f4787b24ef0bf86cbc6d7ca9075e749a4f3feecG\n" +
		"02818100c054262dda08a328da61c687050d1049f3b92ca30c4afb18fe14\n" +
		"0bceb5b6f9d98e0d45641f1a55612283223f34d540af13d0a76749e58660\n" +
		"39a7e1f1a1a32a201691b71108b80066829e9f673ecd7769d51770b368c6\n" +
		"2ea5991696ad3d15b2ac03RSA4102413SHA512withRSA4b3a9fdbfc705dc\n" +
		"c5977ccffa2ebef83d91eb9ccff7f0e91b842c68b73d0a1a30203010001";
				
	public MailBridgeProduct(String domainId) {
		super(domainId, HardwareIDSource.DOMAIN_INTERNET_NAME);
	}
	
	@Override
	public String getLicenseQuantityType() {
		return "accounts";
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
}
