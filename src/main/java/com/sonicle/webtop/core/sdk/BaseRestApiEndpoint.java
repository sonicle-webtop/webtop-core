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

import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.app.AbstractPlatformService;
import com.sonicle.webtop.core.app.WT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author malbinola
 */
public abstract class BaseRestApiEndpoint extends AbstractPlatformService {
	
	public BaseRestApiEndpoint() {
		super();
	}
	
	/**
	 * Gets WebTop Service manifest class.
	 * @return The manifest.
	 */
	public final ServiceManifest getManifest() {
		return WT.getManifest(SERVICE_ID);
	}
	
	protected Response respOk(Object data) {
		return Response.ok(JsonResult.GSON.toJson(data)).build();
	}
	
	protected Response respStatus(Response.Status status) {
		return Response.status(status).build();
	}
	
	protected Response respError() {
		return respError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null);
	}
	
	protected Response respError(int status) {
		return respError(status, null);
	}
	
	protected Response respError(int status, String message) {
		if (StringUtils.isBlank(message)) {
			return Response.status(status)
					.entity(new MapItem())
					.type(MediaType.APPLICATION_JSON)
					.build();
		} else {
			return Response.status(status)
					.entity(new MapItem().add("message", message))
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
	}
	
	/**
	 * @deprecated Use respondOk instead
	 */
	protected Response ok(Object data) {
		return respOk(data);
	}
	
	/**
	 * @deprecated Use respondError instead
	 */
	protected Response error() {
		return error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null);
	}
	
	/**
	 * @deprecated Use respondError instead
	 */
	protected Response error(int status) {
		return error(status, null);
	}
	
	/**
	 * @deprecated Use respondError instead
	 */
	protected Response error(int status, String message) {
		return respError(status, message);
	}
}
