/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core;

import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceVersion;

/**
 *
 * @author malbinola
 */
public class CoreManifest extends ServiceManifest {
	
	public static final String ID = "com.sonicle.webtop.core";
	public static final String XID = "wt";
	public static final String JAVA_PACKAGE = "com.sonicle.webtop.core";
	public static final String JS_PACKAGE = "Sonicle.webtop.core";
	public static final String VERSION = "5.0.0";
	public static final String BUILD_DATE = "07/10/2014";
	public static final String SERVICE_CLASS_NAME = "com.sonicle.webtop.core.CoreService";
	public static final String OPTIONS_CLASS_NAME = "com.sonicle.webtop.core.CoreOptions";
	public static final String SERVICE_JS_CLASS_NAME = "Sonicle.webtop.core.WT";
	public static final String OPTIONS_JS_CLASS_NAME = "Sonicle.webtop.core.view.CoreOptions";
	public static final String COMPANY = "Sonicle S.r.l.";
	public static final String COMPANY_EMAIL = "sonicle@sonicle.com";
	public static final String COMPANY_WEBSITE = "http://www.sonicle.com";
	public static final String SUPPORT_EMAIL = "sonicle@sonicle.com";
	public static final String DATA_SOURCE_NAME = "webtop";
	public static final String[] INIT_CHECK_TABLES = new String[]{"upgrade_statements"/*,"settings","domains"*/};
	
	public CoreManifest() {
		id = ID;
		xid = XID;
		javaPackage = JAVA_PACKAGE;
		jsPackage = JS_PACKAGE;
		version = new ServiceVersion(VERSION);
		buildDate = BUILD_DATE;
		serviceClassName = SERVICE_CLASS_NAME;
		optionsClassName = OPTIONS_CLASS_NAME;
		// This is not a real js service, it's only used 
		// to store class for client-side ovveriding purposes.
		serviceJsClassName = SERVICE_JS_CLASS_NAME;
		optionsJsClassName = OPTIONS_JS_CLASS_NAME;
		company = COMPANY;
		companyEmail = COMPANY_EMAIL;
		companyWebSite = COMPANY_WEBSITE;
		supportEmail = SUPPORT_EMAIL;
		dataSourceName = DATA_SOURCE_NAME;
		initCheckTables = INIT_CHECK_TABLES;
	}
}
