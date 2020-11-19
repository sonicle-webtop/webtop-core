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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class TMCEPremiumProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WT-CORE-TMCEPREMIUM";
	public static final String PRODUCT_NAME = "TinyMCE Premium";
	public static final String PUBLIC_KEY = 
		"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
		"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
		"00428bcd0226f07a5182b63b209b6f99b4b977b4c56a1e38c09b74a2efaG\n" +
		"028181009e855474fdac660bfddde39ef432ce168c4a36d148db4b8fe36f\n" +
		"e18d595b0dbea278c500fcb08784a82b8051a7534baf19b0ffd8f063ce99\n" +
		"a224c85b25b8ca5222b334ec66d8e798926c22567e05389b48ff25532bfc\n" +
		"b59a322aba9609a65b0803RSA4102413SHA512withRSAa045212276cf9ec\n" +
		"7d55018f0dd09467a7d5f730096d2409762bee07b7f7d43190203010001";
	private static String BUILTIN_LICENSE_STRING = null;
	private static Boolean INSTALLED = null;
	
	public TMCEPremiumProduct(String domainId) {
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
		return BUILTIN_LICENSE_STRING;
	}
	
	public static boolean installed() {
		return INSTALLED;
	}
	
	static {
		try {
			Class clazz = Class.forName("com.sonicle.webtop.core.products.TMCEPremiumPlugins");
			try {
				String value = (String)clazz.getField("BUILTIN_LICENSE_STRING").get(null);
				if (!StringUtils.isBlank(value)) {
					BUILTIN_LICENSE_STRING = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
				}
			} catch (Throwable t1) {}
			INSTALLED = true;
		} catch (Throwable t) {
			INSTALLED = false;
		}
	}
}
