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

import com.sonicle.webtop.core.model.ServicePermission;
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
	public static final String VERSION = "5.0.19";
	public static final String BUILD_DATE = "2017-09-27";
	public static final String CONTROLLER_CLASSNAME = "com.sonicle.webtop.core.CoreController";
	public static final String MANAGER_CLASSNAME = "com.sonicle.webtop.core.CoreManager";
	public static final String REST_API_CLASSNAME = "com.sonicle.webtop.core.CoreRestApi";
	public static final String PRIVATE_SERVICE_CLASSNAME = "com.sonicle.webtop.core.Service";
	public static final String PRIVATE_SERVICE_JS_CLASSNAME = "Service";
	public static final String PRIVATE_SERVICEVARS_MODEL_JS_CLASSNAME = "model.ServiceVars";
	public static final String PUBLIC_SERVICE_CLASSNAME = "com.sonicle.webtop.core.PublicService";
	public static final String PUBLIC_SERVICE_JS_CLASSNAME = "PublicService";
	public static final String PUBLIC_SERVICEVARS_MODEL_JS_CLASSNAME = "model.PublicServiceVars";
	public static final String JOB_SERVICE_CLASSNAME = "com.sonicle.webtop.core.JobService";
	public static final String USEROPTIONS_SERVICE_CLASSNAME = "com.sonicle.webtop.core.UserOptionsService";
	public static final String USEROPTIONS_VIEW_JS_CLASSNAME = "view.UserOptions";
	public static final String USEROPTIONS_MODEL_JS_CLASSNAME = "model.UserOptions";
	public static final String COMPANY = "Sonicle S.r.l.";
	public static final String COMPANY_EMAIL = "sonicle@sonicle.com";
	public static final String COMPANY_WEBSITE = "http://www.sonicle.com";
	public static final String SUPPORT_EMAIL = "sonicle@sonicle.com";
	public static final String DATA_SOURCE_NAME = "webtop";
	
	CoreManifest() {
		id = ID;
		xid = XID;
		javaPackage = JAVA_PACKAGE;
		jsPackage = JS_PACKAGE;
		version = new ServiceVersion(VERSION);
		buildDate = BUILD_DATE;
		controllerClassName = CONTROLLER_CLASSNAME;
		managerClassName = MANAGER_CLASSNAME;
		restApiEndpoints.put("", new RestApiEndpoint(REST_API_CLASSNAME, ""));
		privateServiceClassName = PRIVATE_SERVICE_CLASSNAME;
		privateServiceJsClassName = PRIVATE_SERVICE_JS_CLASSNAME;
		privateServiceVarsModelJsClassName = PRIVATE_SERVICEVARS_MODEL_JS_CLASSNAME;
		publicServiceClassName = PUBLIC_SERVICE_CLASSNAME;
		publicServiceJsClassName = PUBLIC_SERVICE_JS_CLASSNAME;
		publicServiceVarsModelJsClassName = PUBLIC_SERVICEVARS_MODEL_JS_CLASSNAME;
		jobServiceClassName = JOB_SERVICE_CLASSNAME;
		userOptionsServiceClassName = USEROPTIONS_SERVICE_CLASSNAME;
		userOptionsViewJsClassName = USEROPTIONS_VIEW_JS_CLASSNAME;
		userOptionsModelJsClassName = USEROPTIONS_MODEL_JS_CLASSNAME;
		company = COMPANY;
		companyEmail = COMPANY_EMAIL;
		companyWebSite = COMPANY_WEBSITE;
		supportEmail = SUPPORT_EMAIL;
		
		/*
			SYSADMIN (internal)
			Marks sysadmin
			- ACCESS: can manage system
		*/
		
		/*
			WTADMIN (internal)
			Mark WebTop admins (users that can act as admins)
			- ACCESS: can manage webtop palform
		*/
		permissions.add(new ServicePermission("WTADMIN", new String[]{ServicePermission.ACTION_ACCESS}));
		
		/*
			SERVICE (internal)
			- ACCESS
			- CONFIGURE
		*/
		
		/*
			PASSWORD
			- MANAGE: allow the user to change its password (auth-directory must support it)
		*/
		permissions.add(new ServicePermission("PASSWORD", new String[]{ServicePermission.ACTION_MANAGE}));
		
		/*
			FEEDBACK
			Feedback insertion
			- MANAGE: allow access to the form
		*/
		permissions.add(new ServicePermission("FEEDBACK", new String[]{ServicePermission.ACTION_MANAGE}));
		
		/*
			ACTIVITIES
			Activities management
			- MANAGE: allow access to the form
		*/
		permissions.add(new ServicePermission("ACTIVITIES", new String[]{ServicePermission.ACTION_MANAGE}));
		
		/*
			CAUSALS
			Causals management
			- MANAGE: allow access to the form
		*/
		permissions.add(new ServicePermission("CAUSALS", new String[]{ServicePermission.ACTION_MANAGE}));
		
		/*
			USER_PROFILE_INFO
			User profile tab in user options
			- WRITE: allow user to update/change its data
		*/
		permissions.add(new ServicePermission("USER_PROFILE_INFO", new String[]{ServicePermission.ACTION_MANAGE}));
		
		/*
			DEVICES_SYNC
			Device synchroniztion
			- ACCESS: ability to sync data with devices
		*/
		permissions.add(new ServicePermission("DEVICES_SYNC", new String[]{ServicePermission.ACTION_ACCESS}));
		
		/*
			WEBCHAT
			Device synchroniztion
			- ACCESS: ability to sync data with devices
		*/
		permissions.add(new ServicePermission("WEBCHAT", new String[]{ServicePermission.ACTION_ACCESS}));
	}
}
