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

import com.sonicle.commons.web.json.RestJsonResult;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.model.SessionInfo;
import com.sonicle.webtop.core.sdk.BaseApiService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.SessionUtils;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
@Path("com.sonicle.webtop.core")
public class CoreRestApi extends BaseApiService {
	private static final Logger logger = WT.getLogger(CoreRestApi.class);
	
	@GET
	@Path("/sessions")
	@Produces({MediaType.APPLICATION_JSON})
	public Response sessionsList() {
		try {
			CoreManager core = WT.getCoreManager(getRunContext());
			List<SessionInfo> items = core.listSessions();
			return Response.ok(new RestJsonResult(items, items.size()).print()).build();
			
		} catch(WTException ex) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/themes")
	@Produces({MediaType.APPLICATION_JSON})
	public Response themesList() {
		try {
			logger.debug("{}", SessionUtils.getSubject().toString());
			
			CoreManager core = WT.getCoreManager(getRunContext());
			List<JsSimple> items = core.listThemes();
			return Response.ok(new RestJsonResult(items, items.size()).print()).build();
			
		} catch(WTException ex) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/layouts")
	@Produces({MediaType.APPLICATION_JSON})
	public Response layoutsList() {
		try {
			CoreManager core = WT.getCoreManager(getRunContext());
			List<JsSimple> items = core.listLayouts();
			return Response.ok(new RestJsonResult(items, items.size()).print()).build();
			
		} catch(WTException ex) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/lafs")
	@Produces({MediaType.APPLICATION_JSON})
	public Response lafsList() {
		try {
			CoreManager core = WT.getCoreManager(getRunContext());
			List<JsSimple> items = core.listLAFs();
			return Response.ok(new RestJsonResult(items, items.size()).print()).build();
			
		} catch(WTException ex) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/domains")
	@Produces({MediaType.APPLICATION_JSON})
	public Response domainsList() {
		try {
			CoreManager core = WT.getCoreManager(getRunContext());
			List<ODomain> items = core.listDomains(true);
			return Response.ok(new RestJsonResult(items, items.size()).print()).build();
			
		} catch(WTException ex) {
			return Response.serverError().build();
		}
	}
	
	private RunContext getRunContext() {
		//return null;
		//TODO: valutare come generare il runcontext
		return new RunContext("com.sonicle.webtop.core", new UserProfile.Id("sonicleldap", "matteo.albinola"), null);
	}
}
