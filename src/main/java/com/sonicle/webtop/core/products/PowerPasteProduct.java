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
import java.lang.reflect.Field;

/**
 *
 * @author malbinola
 */
public final class PowerPasteProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WT-CORE-POWERPASTE";
	public static final String PRODUCT_NAME = "PowerPaste";
	public static final String PUBLIC_KEY = 
		"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
		"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
		"0044a8b1c638925f4a6d23c8b7684a529f675ca94f69ae8821457617d61G\n" +
		"02818100b35d13a688f12957ab2c1368543210dfccb0d8abe12e965d2ef6\n" +
		"925d94aad8cf76fd6acd83fd92e50ff06153ad2bd664b03591e0cd39ca72\n" +
		"9b5ed73c7b18392655a6a7191400bd893c9bd4053faaabeccc3f7ea8fcfc\n" +
		"88e7aed8be9e1f1d55b903RSA4102413SHA512withRSA8fc60de9fc2d2b6\n" +
		"e8912bb4a1bc375945638fdfd6a89d510a7e3b1aef5b58f450203010001";
	private static String builtInLicenseString = null;
	private static Boolean exists = null;
	
	public PowerPasteProduct(String domainId) {
		super(domainId, HardwareIDSource.DOMAIN_INTERNET_NAME);
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
	public String getBuiltInLicenseString() {
		check();
		return builtInLicenseString;
	}
	
	public static boolean exists() {
		check();
		return exists;
	}
	
	private static synchronized void check() {
		if (exists == null) {
			try {
				Class clazz = Class.forName("com.sonicle.webtop.core.products.PowerPaste");
				try {
					Field field = clazz.getField("BUILTIN_LICENSE_STRING_SONICLE2");
					String value = (String)field.get(null);
					builtInLicenseString = value;
				} catch (Throwable t1) {
					System.out.println(t1);
				}
				exists = true;
			} catch (Throwable t) {
				exists = false;
			}
		}
	}
}
