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
package com.sonicle.webtop.core.old;

import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.sdk.BaseRestApiEndpoint;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author malbinola
 */
@Path("app")
public class RestApiEndpoint extends BaseRestApiEndpoint {
	private WebTopApp wta = null;

	public RestApiEndpoint(WebTopApp wta) {
		super();
		this.wta = wta;
	}
	
	/*
	@GET
	@Path("/sessions")
	@Produces({MediaType.APPLICATION_JSON})
	public Response listSessions() throws WTException {
		return respOk(getSessionManager().listOnlineSessions());
	}
	
	@GET
	@Path("/sessions/{id}/exist")
	@Produces({MediaType.APPLICATION_JSON})
	public Response existSession(@PathParam("id") String id) throws WTException {
		return respStatus(getSessionManager().isOnline(id) ? Response.Status.OK : Response.Status.NOT_FOUND);
	}
	*/
	
	/*
	@DELETE
	@Path("/sessions/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteSession(@PathParam("id") String id) throws WTException {
		getSessionManager().invalidateSession(id);
		return respOk(new MapItem());
	}
	*/
	
	private SessionManager getSessionManager() throws WTException {
		final SessionManager mgr = wta.getSessionManager();
		if (mgr == null) throw new WTException("Invalid manager");
		return mgr;
	}
	
	private CoreManager getCoreManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private CoreManager getManager(UserProfileId targetProfileId) {
		return (CoreManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
	}
}
