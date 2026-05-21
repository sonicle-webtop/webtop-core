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
public final class ConnectProduct extends BaseServiceProduct {
	public static final String PRODUCT_ID = "SNCL-WT-CORE-CONNECT";
	public static final String PRODUCT_NAME = "WebTop Connect";
	public static final String PUBLIC_KEY = 
		"30819f300d06092a864886f70d010101050003818d003081893032301006\n" +
		"072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0\n" +
		"0048e5267f56e6146efb53ed85db329c7544de08dc3dbd66c147d9ae1a9G\n" +
		"0281810085e771af0c55176aed81304722445a5708db8b6da4635a6b6c86\n" +
		"c0078b76d6898c5c5d02c9c1ef095c3c3e6341fe5e3b330b0047d0fbb3ab\n" +
		"c88ed7e3811911cce863e07be3f9bc17839e731d84e10e2bc7d9ae65f4c4\n" +
		"c870a50e6587bee8fbba03RSA4102413SHA512withRSAf2b3751c872fba8\n" +
		"578e95dafde965e87bf43d333298b95332b7abc46829253a30203010001";			
	
	//Trial 30 days
	private static String BUILTIN_LICENSE_STRING =
		"# Core - Connect License (id: 1779368085540)\n" +
		"f4409a5ba6b5ead167bc7cb33ca292cd5b2ea9afa2784df357129ee00cf4\n" +
		"cdc59c3ecb9af83ae1fe7d2d65650069bc08dc221f416bc60c72c3ae32b8\n" +
		"7b434afb7f27a50ff323c77563eb70430ded7d5da4428a23c1995b956907\n" +
		"4f919cffa10a10668763d57b2c7597b3d6f745a29f472d378bdb664835c4\n" +
		"233c9b5429214c139b5312dc1ace2fffc24c3dd7c90fb77b64b9a2f42554\n" +
		"4a3c206fc41ad0c61b8ca8f732f063dbbf024de6daffb0a8bd21f92f0187\n" +
		"100aea2ce196b2a6792c568ed7b7829699927ef8ce30df35621bb102f3bb\n" +
		"9146048085c551d8517df44762b0e93aa96ac5b65d8014076c35890be7f3\n" +
		"9a1ab05860304a05e4de9c128d1d8d266eb5205012f63f531c5d0d14b447\n" +
		"c98a8ea4a55b73ea58caa80448a7b7a2ceca54d3ea04fb4f3c8163f37eb9\n" +
		"35583fccc511dfeb5edea3f1984fa55d2d5e7711829263e7cf3466a6c3df\n" +
		"4d633a5e249b8d555ae349221d6003f26bbd71d379c7817afa19c96646e9\n" +
		"20b52fd8eced106025fd2e60e56d29021f7d48f7110551adff4fa9fa8455\n" +
		"f15449f08eb7e77819d6d058884005ecde375694300ce135194631911ff1\n" +
		"830163b9697d423c59f74261";
	
	public ConnectProduct(String domainId) {
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
