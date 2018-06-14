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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author malbinola
 */
public class JsWTSPrivate extends JsWTS {
	public String sessionId;
	public String securityToken;
	public String pushUrl;
	public String layoutClassName;
	public HashSet<String> roles = new HashSet<>();
	public ArrayList<Vars> servicesVars = new ArrayList<>();
	public ArrayList<Permissions> servicesPerms = new ArrayList<>();
	
	public JsWTSPrivate() {
		this.appType = "private";
	}
	
	public static class ServiceUserOptions {
		public String viewClassName;
		public String modelClassName;
		
		public ServiceUserOptions(String viewClassName, String modelClassName) {
			this.viewClassName = viewClassName;
			this.modelClassName = modelClassName;
		}
	}
	
	@Override
	public Manifest createManifestInstance() {
		return new PrivateManifest();
	}
	
	@Override
	public Service createServiceInstance() {
		return new PrivateService();
	}
	
	public static class Permissions extends HashMap<String, Actions> {}
	
	public static class Actions extends HashMap<String, Object> {}
	
	public static class PrivateManifest extends Manifest {
		public String version;
		public String build;
	}
	
	public static class PrivateService extends Service {
		public ServiceUserOptions userOptions;
		public ArrayList<String> portletCNs = new ArrayList<>();
	}
	
	public static class Vars extends HashMap<String, Object> {}
}
