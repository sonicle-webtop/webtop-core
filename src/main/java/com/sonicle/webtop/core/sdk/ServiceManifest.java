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
package com.sonicle.webtop.core.sdk;

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.model.ServiceSharePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ServiceManifest {
	protected String id;
	protected String xid;
	protected String javaPackage;
	protected String jsPackage;
	protected ServiceVersion version;
	protected ServiceVersion oldVersion;
	protected String buildDate;
	protected String company;
	protected String companyEmail;
	protected String companyWebSite;
	protected String supportEmail;
	protected String controllerClassName;
	protected String managerClassName;
	protected String restApiClassName;
	protected String privateServiceClassName;
	protected String userOptionsServiceClassName;
	protected String publicServiceClassName;
	protected String jobServiceClassName;
	protected String privateServiceJsClassName;
	protected String privateServiceVarsModelJsClassName;
	protected String publicServiceJsClassName;
	protected String publicServiceVarsModelJsClassName;
	protected String userOptionsViewJsClassName;
	protected String userOptionsModelJsClassName;
	protected Boolean hidden;
	protected ArrayList<ServicePermission> permissions = new ArrayList<>();
	protected ArrayList<Portlet> portlets = new ArrayList<>();
	
	
	public ServiceManifest() {
		version = new ServiceVersion();
		oldVersion = new ServiceVersion();
		buildDate = StringUtils.EMPTY;
		company = "Unknown Company";
		companyEmail = "sonicle@sonicle.com";
		companyWebSite = "http://www.sonicle.com";
		supportEmail = "sonicle@sonicle.com";
	}
	
	public ServiceManifest(HierarchicalConfiguration svcEl) throws Exception {
		
		String pkg = svcEl.getString("package");
		if(StringUtils.isEmpty(pkg)) throw new Exception("Invalid value for property [package]");
		javaPackage = StringUtils.lowerCase(pkg);
		id = javaPackage;
		
		String jspkg = svcEl.getString("jsPackage");
		if(StringUtils.isEmpty(jspkg)) throw new Exception("Invalid value for property [jsPackage]");
		jsPackage = jspkg; // Lowercase allowed!
		
		String sname = svcEl.getString("shortName");
		if(StringUtils.isEmpty(sname)) throw new Exception("Invalid value for property [shortName]");
		xid = sname;
		
		ServiceVersion ver = new ServiceVersion(svcEl.getString("version"));
		if(ver.isUndefined()) throw new Exception("Invalid value for property [version]");
		version = ver;
		
		buildDate = StringUtils.defaultIfBlank(svcEl.getString("buildDate"), null);
		company = StringUtils.defaultIfBlank(svcEl.getString("company"), null);
		companyEmail = StringUtils.defaultIfBlank(svcEl.getString("companyEmail"), null);
		companyWebSite = StringUtils.defaultIfBlank(svcEl.getString("companyWebSite"), null);
		supportEmail = StringUtils.defaultIfBlank(svcEl.getString("supportEmail"), null);
		
		if(!svcEl.containsKey("controllerClassName")) throw new Exception("Invalid value for property [controllerClassName]");
		controllerClassName = LangUtils.buildClassName(javaPackage, StringUtils.defaultIfEmpty(svcEl.getString("controllerClassName"), "Controller"));
		
		if(svcEl.containsKey("managerClassName")) {
			managerClassName = LangUtils.buildClassName(javaPackage, StringUtils.defaultIfEmpty(svcEl.getString("managerClassName"), "Manager"));
		}
		
		if(svcEl.containsKey("restApiClassName")) {
			restApiClassName = LangUtils.buildClassName(javaPackage, StringUtils.defaultIfEmpty(svcEl.getString("restApiClassName"), "RestApi"));
		}
		
		if(svcEl.containsKey("serviceClassName")) {
			String cn = StringUtils.defaultIfEmpty(svcEl.getString("serviceClassName"), "Service");
			privateServiceClassName = LangUtils.buildClassName(javaPackage, cn);
			privateServiceJsClassName = StringUtils.defaultIfEmpty(svcEl.getString("serviceJsClassName"), cn);
			privateServiceVarsModelJsClassName = StringUtils.defaultIfEmpty(svcEl.getString("serviceVarsModelJsClassName"), "model.ServiceVars");
		}
		
		if(svcEl.containsKey("publicServiceClassName")) {
			String cn = StringUtils.defaultIfEmpty(svcEl.getString("publicServiceClassName"), "PublicService");
			publicServiceClassName = LangUtils.buildClassName(javaPackage, StringUtils.defaultIfEmpty(svcEl.getString("publicServiceClassName"), "PublicService"));
			publicServiceJsClassName = StringUtils.defaultIfEmpty(svcEl.getString("publicServiceJsClassName"), cn);
			publicServiceVarsModelJsClassName = StringUtils.defaultIfEmpty(svcEl.getString("publicServiceVarsModelJsClassName"), "model.PublicServiceVars");
		}
		
		if(svcEl.containsKey("jobServiceClassName")) {
			jobServiceClassName = LangUtils.buildClassName(javaPackage, StringUtils.defaultIfEmpty(svcEl.getString("jobServiceClassName"), "JobService"));
		}
		
		if(!svcEl.configurationsAt("userOptions").isEmpty()) {
			userOptionsServiceClassName = LangUtils.buildClassName(javaPackage, StringUtils.defaultIfEmpty(svcEl.getString("userOptions.serviceClassName"), "UserOptionsService"));
			userOptionsViewJsClassName = StringUtils.defaultIfEmpty(svcEl.getString("userOptions.viewJsClassName"), "view.UserOptions");
			userOptionsModelJsClassName = StringUtils.defaultIfEmpty(svcEl.getString("userOptions.modelJsClassName"), "model.UserOptions");
		}
		
		hidden = svcEl.getBoolean("hidden", false);
		
		if (!svcEl.configurationsAt("permissions").isEmpty()) {
			List<HierarchicalConfiguration> elPerms = svcEl.configurationsAt("permissions.permission");
			for(HierarchicalConfiguration elPerm : elPerms) {
				if (elPerm.containsKey("[@group]")) {
					String groupName = elPerm.getString("[@group]");
					if (StringUtils.isEmpty(groupName)) throw new Exception("Permission must have a valid uppercase group name");
					
					if (elPerm.containsKey("[@actions]")) {
						String[] actions = StringUtils.split(elPerm.getString("[@actions]"), ",");
						if (actions.length == 0) throw new Exception("Resource must declare at least 1 action");
						permissions.add(new ServicePermission(groupName, actions));
					} else {
						throw new Exception("Permission must declare actions supported on group");
					}
				}
			}
			
			List<HierarchicalConfiguration> elShPerms = svcEl.configurationsAt("permissions.sharePermission");
			for(HierarchicalConfiguration elShPerm : elShPerms) {
				if (elShPerm.containsKey("[@group]")) {
					String groupName = elShPerm.getString("[@group]");
					if (StringUtils.isEmpty(groupName)) throw new Exception("Permission must have a valid uppercase group name");
					permissions.add(new ServiceSharePermission(groupName));
				}
			}
		}
		
		if (!svcEl.configurationsAt("portlets").isEmpty()) {
			List<HierarchicalConfiguration> elWidgets = svcEl.configurationsAt("portlets.portlet");
			for(HierarchicalConfiguration elWidget : elWidgets) {
				if (elWidget.containsKey("[@jsClassName]")) {
					final String jsClassName = elWidget.getString("[@jsClassName]");
					if (StringUtils.isBlank(jsClassName)) throw new Exception("Invalid value for attribute [portlet->jsClassName]");
					portlets.add(new Portlet(LangUtils.buildClassName(jsPackage, jsClassName)));
				}
			}
		}
	}
	
	/**
	 * Gets specified service ID.
	 * @return The value.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Gets specified service XID (short ID).
	 * @return The value.
	 */
	public String getXId() {
		return xid;
	}
	
	/**
	 * Gets the server-side package name.
	 * (eg. com.sonicle.webtop.core)
	 * @return The value.
	 */
	public String getPackageName() {
		return javaPackage;
	}
	
	/**
	 * Converts the package name into its path representation.
	 * (eg. com.sonicle.webtop.mail -> com/sonicle/webtop/mail)
	 * @return The value.
	 */
	public String getJarPath() {
		return StringUtils.lowerCase(StringUtils.replace(getPackageName(), ".", "/"));
	}
	
	/**
	 * Gets the client-side package name.
	 * (eg. Sonicle.webtop.mail)
	 * @return The value.
	 */
	public String getJsPackageName() {
		return jsPackage;
	}
	
	/**
	 * Converts the js package name into its path representation.
	 * (eg. Sonicle.webtop.mail -> sonicle/webtop/mail)
	 * @return The value.
	 */
	public String getJsPath() {
		return StringUtils.lowerCase(StringUtils.replace(getJsPackageName(), ".", "/"));
	}
	
	/**
	 * Returns current service version.
	 * @return The service version.
	 */
	public ServiceVersion getVersion() {
		return version;
	}
	
	/**
	 * Returns service previous version (before the current one).
	 * @return The service version.
	 */
	public ServiceVersion getOldVersion() {
		return oldVersion;
	}
	
	/**
	 * Sets service previous version.
	 * @param value 
	 */
	public void setOldVersion(ServiceVersion value) {
		oldVersion = value;
	}
	
	/**
	 * Gets the specified build date.
	 * @return The build date (as declared in manifest)
	 */
	public String getBuildDate() {
		return buildDate;
	}
	
	/**
	 * Gets the class name of server-side Controller implementation.
	 * (eg. com.sonicle.webtop.core.CoreController)
	 * @return The class name.
	 */
	public String getControllerClassName() {
		return controllerClassName;
	}
	
	/**
	 * Gets the class name of server-side Manager implementation.
	 * (eg. com.sonicle.webtop.core.CoreManager)
	 * @return The class name.
	 */
	public String getManagerClassName() {
		return managerClassName;
	}
	
	/**
	 * Gets the class name of server-side REST Api implementation.
	 * (eg. com.sonicle.webtop.core.CoreRestApi)
	 * @return The class name.
	 */
	public String getRestApiClassName() {
		return restApiClassName;
	}
	
	/**
	 * Gets the class name of server-side private (authenticated) service implementation.
	 * (eg. com.sonicle.webtop.core.CoreService)
	 * @return The class name.
	 */
	public String getPrivateServiceClassName() {
		return privateServiceClassName;
	}
	
	public String getUserOptionsServiceClassName() {
		return userOptionsServiceClassName;
	}
	
	/**
	 * Gets the class name of server-side public (not authenticated) service implementation.
	 * (eg. com.sonicle.webtop.core.CorePublicService)
	 * @return The class name.
	 */
	public String getPublicServiceClassName() {
		return publicServiceClassName;
	}
	
	/**
	 * Gets the class name of server-side job service implementation.
	 * (eg. com.sonicle.webtop.core.CoreJobService)
	 * @return The class name.
	 */
	public String getJobServiceClassName() {
		return jobServiceClassName;
	}
	
	/**
	 * Gets the class name of client-side private service implementation.
	 * (eg. Sonicle.webtop.mail.MailService)
	 * @param full True to include js package.
	 * @return The class name.
	 */
	public String getPrivateServiceJsClassName(boolean full) {
		return (full) ? LangUtils.buildClassName(jsPackage, privateServiceJsClassName) : privateServiceJsClassName;
	}
	
	public String getPrivateServiceVarsModelJsClassName(boolean full) {
		return (full) ? LangUtils.buildClassName(jsPackage, privateServiceVarsModelJsClassName) : privateServiceVarsModelJsClassName;
	}
	
	/**
	 * Gets the class name of client-side public service implementation.
	 * (eg. Sonicle.webtop.mail.MailService)
	 * @param full True to include js package.
	 * @return The class name.
	 */
	public String getPublicServiceJsClassName(boolean full) {
		return (full) ? LangUtils.buildClassName(jsPackage, publicServiceJsClassName) : publicServiceJsClassName;
	}
	
	public String getPublicServiceVarsModelJsClassName(boolean full) {
		return (full) ? LangUtils.buildClassName(jsPackage, publicServiceVarsModelJsClassName) : publicServiceVarsModelJsClassName;
	}
	
	public String getUserOptionsViewJsClassName(boolean full) {
		return (full) ? LangUtils.buildClassName(jsPackage, userOptionsViewJsClassName) : userOptionsViewJsClassName;
	}
	
	public String getUserOptionsModelJsClassName(boolean full) {
		return (full) ? LangUtils.buildClassName(jsPackage, userOptionsModelJsClassName) : userOptionsModelJsClassName;
	}
	
	/**
	 * Gets the class name of client-side locale object that applies localized strings.
	 * @param locale The required locale.
	 * @param full True to include js package.
	 * @return The class name.
	 */
	public String getLocaleJsClassName(Locale locale, boolean full) {
		String cn = "Locale_" + locale.getLanguage();
		return (full) ? LangUtils.buildClassName(jsPackage, cn) : cn;
	}
	
	/**
	 * Returns the client-side URL path to reach service package base folder.
	 * Eg. "resources/com.sonicle.webtop.mail/1.0.0"
	 * @return 
	 */
	public String getPackageBaseUrl() {
		return "resources/" + getId() + "/" + getVersion().toString();
	}
	
	/**
	 * Returns the client-side URL path to reach service package sources folder.
	 * Eg. "resources/com.sonicle.webtop.mail/1.0.0/src"
	 * @return 
	 */
	public String getPackageSrcUrl() {
		return getPackageBaseUrl() + "/src";
	}
	
	/**
	 * Returns the client-side URL path to reach service package LAF folder.
	 * Eg. "resources/com.sonicle.webtop.mail/1.0.0/laf"
	 * @param lookAndFeel The look&feel name.
	 * @return 
	 */
	public String getPackageLookAndFeelUrl(String lookAndFeel) {
		return getPackageBaseUrl() + "/laf/" + lookAndFeel;
	}
	
	public String getBundleJsFileName() {
		return getId() + ".js";
	}
	
	public String getPrivateServiceJsFileName() {
		return getPrivateServiceJsClassName(false) + ".js";
	}
	
	public String getPublicServiceJsFileName() {
		return getPublicServiceJsClassName(false) + ".js";
	}
	
	public String getLocaleJsFileName(Locale locale) {
		return getLocaleJsClassName(locale, false) + ".js";
	}
	
	public String getJsBaseUrl(boolean devMode) {
		String base = getPackageBaseUrl();
		if (devMode) base += "/src";
		return base;
	}
	
	public String getCompany() {
		return company;
	}
	
	public String getCompanyEmail() {
		return companyEmail;
	}
	
	public String getCompanyWebSite() {
		return companyWebSite;
	}
	
	public String getSupportEmail() {
		return supportEmail;
	}
	
	public ArrayList<ServicePermission> getDeclaredPermissions() {
		return permissions;
	}
	
	public ArrayList<Portlet> getPortlets() {
		return portlets;
	}
	
	public static class Portlet {
		public final String jsClassName;
		
		public Portlet(String jsClassName) {
			this.jsClassName = jsClassName;
		}
	}
}
