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
package com.sonicle.webtop.core.bol.js;

import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.extjs.Ext6Manifest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author malbinola
 */
public abstract class JsWTS {
	public Ext6Manifest appManifest = new Ext6Manifest();
	public String appType;
	public String themeName;
	public String platformName;
	public String contextPath;
	public String fileTypes;
	public HashMap<String, String> appPaths = new HashMap<>();
	public ArrayList<String> appRequires = new ArrayList<>();
	public Map<String, JsWTS.Manifest> manifests = new LinkedHashMap<>();
	public ArrayList<JsWTS.Service> services = new ArrayList<>();
	public ArrayList<JsWTS.XLocale> locales = new ArrayList<>();
	
	public String toJson() {
		return JsonResult.gson().toJson(this);
	}
	
	public abstract Manifest createManifestInstance();
	public abstract Service createServiceInstance();
	
	public static class Manifest {
		public String xid;
		public String ns;
		public String path;
		public String name;
		public String description;
		public String company;
		public String localeCN;
		public Boolean maintenance;
	}
	
	public static class Service {
		public String id;
		public String serviceCN;
		public String serviceVarsCN;
	}
	
	public static class XLocale {
		public final String sid;
		public final String localeClassName;
		
		public XLocale(String sid, String localeClassName) {
			this.sid = sid;
			this.localeClassName = localeClassName;
		}
	}
}
