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
package com.sonicle.webtop.core.app;

import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceVersion;

/**
 *
 * @author malbinola
 */
public class CoreAdminManifest extends ServiceManifest {
	
	public static final String ID = "com.sonicle.webtop.core.admin";
	public static final String XID = "wtadm";
	public static final String JAVA_PACKAGE = "com.sonicle.webtop.core.admin";
	public static final String JS_PACKAGE = "Sonicle.webtop.core.admin";
	public static final String VERSION = "5.5.6";
	public static final String BUILD_DATE = "2019-04-01";
	public static final String CONTROLLER_CLASSNAME = "com.sonicle.webtop.core.admin.Controller";
	public static final String MANAGER_CLASSNAME = "com.sonicle.webtop.core.admin.CoreAdminManager";
	public static final String PRIVATE_SERVICE_CLASSNAME = "com.sonicle.webtop.core.admin.Service";
	public static final String PRIVATE_SERVICE_JS_CLASSNAME = "Service";
	public static final String PRIVATE_SERVICEVARS_MODEL_JS_CLASSNAME = "model.ServiceVars";
	public static final String COMPANY = "Sonicle S.r.l.";
	public static final String COMPANY_EMAIL = "sonicle@sonicle.com";
	public static final String COMPANY_WEBSITE = "http://www.sonicle.com";
	public static final String SUPPORT_EMAIL = "sonicle@sonicle.com";
	
	CoreAdminManifest() {
		id = ID;
		xid = XID;
		javaPackage = JAVA_PACKAGE;
		jsPackage = JS_PACKAGE;
		version = new ServiceVersion(VERSION);
		buildDate = BUILD_DATE;
		controllerClassName = CONTROLLER_CLASSNAME;
		managerClassName = MANAGER_CLASSNAME;
		privateServiceClassName = PRIVATE_SERVICE_CLASSNAME;
		privateServiceJsClassName = PRIVATE_SERVICE_JS_CLASSNAME;
		privateServiceVarsModelJsClassName = PRIVATE_SERVICEVARS_MODEL_JS_CLASSNAME;
		company = COMPANY;
		companyEmail = COMPANY_EMAIL;
		companyWebSite = COMPANY_WEBSITE;
		supportEmail = SUPPORT_EMAIL;
	}
}
