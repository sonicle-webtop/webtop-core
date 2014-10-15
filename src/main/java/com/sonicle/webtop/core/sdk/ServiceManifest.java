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

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ServiceManifest {
	
	protected String id;
	protected String className;
	protected String jsClassName;
	protected String publicClassName;
	protected String deamonClassName;
	protected Boolean hidden;
	protected ServiceVersion version;
	protected ServiceVersion oldVersion;
	protected String buildDate;
	protected String company;
	protected String companyEmail;
	protected String companyWebSite;
	protected String supportEmail;
	protected String dataSourceName;
	protected String[] initCheckTables;
	
	public ServiceManifest() {
		version = new ServiceVersion();
		oldVersion = new ServiceVersion();
		buildDate = StringUtils.EMPTY;
		company = "Unknown Company";
		companyEmail = "sonicle@sonicle.com";
		companyWebSite = "http://www.sonicle.com";
		supportEmail = "sonicle@sonicle.com";
	}
	
	public ServiceManifest(String id, 
		String className, String jsClassName, String publicClassName, String deamonClassName, 
		Boolean hidden, ServiceVersion version, String buildDate, String company, 
		String companyEmail, String companyWebSite, String supportEmail) throws Exception {
		super();
		
		if(StringUtils.isEmpty(id)) throw new Exception("Invalid value for property [id]");
		this.id = id.toLowerCase();
		
		boolean noclass = StringUtils.isEmpty(className) && StringUtils.isEmpty(publicClassName) & StringUtils.isEmpty(deamonClassName);
		//TODO: Enable check or not?
		//if(noclass) throw new Exception("You need to fill at least a service class");
		this.className = className;
		this.jsClassName = jsClassName;
		this.publicClassName = publicClassName;
		this.deamonClassName = deamonClassName;
		this.hidden = hidden;
		if(version.isUndefined()) throw new Exception("Invalid value for property [version]");
		this.version = version;
		if(!StringUtils.isEmpty(buildDate)) this.buildDate = buildDate;
		if(!StringUtils.isEmpty(company)) this.company = company;
		if(!StringUtils.isEmpty(companyEmail)) this.companyEmail = companyEmail;
		if(!StringUtils.isEmpty(companyWebSite)) this.companyWebSite = companyWebSite;
		if(!StringUtils.isEmpty(supportEmail)) this.supportEmail = supportEmail;
	}
	
	public String getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}
	
	public String getJsClassName() {
		return jsClassName;
	}

	public String getPublicClassName() {
		return publicClassName;
	}

	public String getDeamonClassName() {
		return deamonClassName;
	}
	
	public ServiceVersion getVersion() {
		return version;
	}
	
	public ServiceVersion getOldVersion() {
		return oldVersion;
	}
	
	public void setOldVersion(ServiceVersion value) {
		oldVersion = value;
	}
	
	public String getBuildDate() {
		return buildDate;
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
	
	public String getDataSourceName() {
		return dataSourceName;
	}
	
	public String[] getInitCheckTables() {
		return initCheckTables;
	}
}
