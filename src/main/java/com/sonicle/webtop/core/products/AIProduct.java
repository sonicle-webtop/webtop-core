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
public final class AIProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WT-CORE-AI";
	public static final String PRODUCT_NAME = "A.I.";
	public static final String PUBLIC_KEY = 
		"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
		"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
		"0041b1721a847c8d72f4b61cf304d956a5593d6b9dc240e7622af260084G\n" +
		"02818100813f0c792b9be59965df421c4fdca1b979292168e42dfc5d3098\n" +
		"925c7c369c1e76640c236e8c9f4d17e52d1c62cae183d094e1d52c2fae0c\n" +
		"6d59a38274fca9cce04d2ceb2071c2d95e32aa2a4917c8cfdb904d56d58c\n" +
		"d94d5443d973e09921be03RSA4102413SHA512withRSA1368c8a003bad48\n" +
		"fe39a1ac6eb3f1faba328f2bc1cc8b16d4fa33250076476770203010001";
	
	//Trial 90 days
	private static String BUILTIN_LICENSE_STRING =
		//"# Core - A.I. License (id: 1778140391230)\n" +
		"72c8f193d1588cc36971baca3f538ce715070a22ac8faa8a4b1b5b42642a\n" +
		"cde6581e9c5dfca07fdfa3346bcb7b3b3289debee79ed74fc1884b75937a\n" +
		"919cbefe9d0f020b7e9386e27e22afe05a0d11298a2a6c492ab2dddf21fd\n" +
		"40e9b78f1c1b2bb088d48bc5741710272a1bb3cb5e04eb8bba769bc3cc58\n" +
		"f4e0880a4a5be9ba4992c567661d02f1ea18610c4fb3e80f94c5d9e296f0\n" +
		"4f7e168bf254bb98fc444e117c11de8f95c21e0bb5923ef0a5e7368cb434\n" +
		"b90e972b224ce868b9fe501fe07301bbac8ce9ed903c456c10691b5cf25c\n" +
		"c0e14aa41c6c0e75d4e9f5c353d7c3a91d2ef1988e41968de734e0423bce\n" +
		"7f42dd74176f27246514e4fdfbbd7f68ab6433068746375c85da2c03f9c8\n" +
		"e88d721e51d16fd13d66573b361cec936e41616fd03af1f3cc7525cf76f5\n" +
		"ea89affcd7875d788304c521d8427f7920c94fd337ff49ae7d9a9492ac5e\n" +
		"59bcc9b31c0fbc0db467ce88f5d537a96152104f631163e57dc48e80deba\n" +
		"2c7302bfc4d0b2d04b997cb2860603e7042beeccc5807cf084db1aa51d50\n" +
		"19ee284d9bf368437cbfc363f25dba2d7c2e9497688465203afbba4f7511\n" +
		"a149027c";
	
	//Trial 1 day
/*	private static String BUILTIN_LICENSE_STRING =
		//"# Core - A.I. License (id: 1778160183204)\n" +
		"72c8f193d1588cc36971baca3f538ce715070a22ac8faa8a4b1b5b42642a\n" +
		"cde6581e9c5dfca07fdf6519a779030bc464c3c4b1fd2ab49a02a246c114\n" +
		"16b2db5e537c34f28c302e25a8e513ffcfcb5c0f186ffb768a866a9d5870\n" +
		"eea6f4c7a8a67e4264d899a4956e4907b881b1e65147b3dad63e89764561\n" +
		"dbc5b47553349f89d9a9fd6dda2ce3c75130a39722c60436edda810723e8\n" +
		"dc56769cb5b03ba7b6db380bbbbebe0ded8dbd3a9a9b4fb0ace6d83c2898\n" +
		"4fe13b6a52a00b89945f90f09f09272aa3b740128501c206fcc1b2072a9e\n" +
		"ef269cd5876178e4dee6d302c89eea9dc5531f453ca5327987beee3c26d2\n" +
		"d3b94b8086b21672bc4d555ca97a17f74395672d32b96a94e3ed4e3398ae\n" +
		"00ab08971f84a3966d9c5e0219f39f679c78f26efe0382a47a6ca88f51c4\n" +
		"c165d1c1fe1f5c684fa55f448a2385ba9a4af2f7e3fcad5a1e6e7f9970ec\n" +
		"41c51e37b19bd797289d89cfb53f5054a244ca143d2f6ea442317d5ee7fc\n" +
		"d7e0392a261b271ea5b2090c3cc36f2a7e75fc665d45ad1b010a0813d3bd\n" +
		"b9eea75d63ad9619318df11ff8a925bb75d5ba90363ea4a2f784";*/
	
	public AIProduct(String domainId) {
		super(domainId, HardwareIDSource.DOMAIN_INTERNET_NAME);
	}
	
	@Override
	public String getLicenseQuantityType() {
		return LQT_USERS;
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

	@Override
	public String getBuiltInHardwareId() {
		return createHardwareIdString(domainId, HardwareIDSource.DOMAIN_INTERNET_NAME);
	}
	
	
}
