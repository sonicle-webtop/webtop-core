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

import com.sonicle.commons.l4j.DomainBasedProduct;

/**
 *
 * @author gabriele.bulfon
 */
public class AuditProduct extends DomainBasedProduct {
	
	private static String PRODUCT_ID = "audit";
		
	static String PUBLIC_KEY=
			"30819f300d06092a864886f70d010101050003818d003081893032301006\n"+
			"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n"+
			"0042d4fa0190ca06c8b6aac3b08d87b4bf375b4c91f8db40d7363628246G\n"+
			"02818100815cde722fc927b64632d42af291d695b83ce11dcadae039b3a1\n"+
			"20e03e3d2013b3d4ce990fb9384b8e09884704ec06b957dcf2654646074d\n"+
			"04c15edd2f20a6fea9d576b80a16185b4caecc735bf02f730edaadef8e1c\n"+
			"2341a6903b2a1d9f935d03RSA4102413SHA512withRSA9e223cfa0ece427\n"+
			"ba122b2394500bd81fc1232954e8376d6a1473dc0819beaf50203010001";

	public AuditProduct(String internetDomain) {
		super(internetDomain);
	}

	@Override
	public String getPublicKey() {
		return PUBLIC_KEY;
	}

	@Override
	public String getProductId() {
		return PRODUCT_ID;
	}

}
