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

import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.model.UILayout;
import com.sonicle.webtop.core.model.UILookAndFeel;
import com.sonicle.webtop.core.model.UITheme;
import com.sonicle.webtop.core.sdk.BaseRestApiEndpoint;
import com.sonicle.webtop.core.sdk.WTException;
import java.util.ArrayList;
import java.util.Collection;
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
public class RestApiEndpoint extends BaseRestApiEndpoint {
	private static final Logger logger = WT.getLogger(RestApiEndpoint.class);
	
	public RestApiEndpoint() {
		super();
	}
	
	@GET
	@Path("/themes")
	@Produces({MediaType.APPLICATION_JSON})
	public Response themesList() throws WTException {
		CoreManager coreMgr = getManager();
		
		List<JsSimple> items = new ArrayList<>();
		for (UITheme theme : coreMgr.listUIThemes().values()) {
			items.add(new JsSimple(theme.getId(), theme.getName()));
		}
		return ok(items);
	}
	
	@GET
	@Path("/layouts")
	@Produces({MediaType.APPLICATION_JSON})
	public Response layoutsList() throws WTException {
		CoreManager coreMgr = getManager();
		
		List<JsSimple> items = new ArrayList<>();
		for (UILayout layout : coreMgr.listUILayouts()) {
			items.add(new JsSimple(layout.getId(), layout.getName()));
		}
		return ok(items);
	}
	
	@GET
	@Path("/lafs")
	@Produces({MediaType.APPLICATION_JSON})
	public Response lafsList() throws WTException {
		CoreManager coreMgr = getManager();
		
		List<JsSimple> items = new ArrayList<>();
		for (UILookAndFeel laf : coreMgr.listUILookAndFeels().values()) {
			items.add(new JsSimple(laf.getId(), laf.getName()));
		}
		return ok(items);
	}
	
	@GET
	@Path("/domains")
	@Produces({MediaType.APPLICATION_JSON})
	public Response listDomains() throws WTException {
		CoreManager core = getManager();
		Collection<Domain> items = core.listDomains(EnabledCond.ENABLED_ONLY).values();
		return ok(items);
	}
	
	@GET
	@Path("/me/devicesSynchronization/enabled")
	@Produces({MediaType.APPLICATION_JSON})
	public Response isDeviceSynchronizationEnabled() throws WTException {
		boolean bool = RunContext.isPermitted(true, SERVICE_ID, "DEVICES_SYNC");
		return ok(new MapItem().add("response", bool));
	}
	
	private CoreManager getManager() {
		return WT.getCoreManager();
	}
}
