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

import com.sonicle.webtop.core.app.AbstractPlatformService;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.helpers.MessageFormatter;

/**
 *
 * @author malbinola
 */
public abstract class BaseRestApiResource extends AbstractPlatformService {
	
	public BaseRestApiResource() {
		super();
	}
	
	/**
	 * Gets WebTop Service manifest class.
	 * @return The manifest.
	 */
	public final ServiceManifest getManifest() {
		return WT.getManifest(SERVICE_ID);
	}
	
	
	public UserProfileId getTargetProfileId(String targetProfileId) {
		Subject subject = RunContext.getSubject();
		if (RunContext.isSysAdmin(subject)) {
			return StringUtils.isBlank(targetProfileId) ? null : new UserProfileId(targetProfileId);
		} else {
			return StringUtils.isBlank(targetProfileId) ? RunContext.getRunProfileId(subject) : new UserProfileId(targetProfileId);
		}
	}
	
	protected abstract Object createErrorEntity(Response.Status status, String message);
	
	public Response respOkNoContent() {
		return respOk(Response.Status.NO_CONTENT);
	}
	
	public Response respOkCreated() {
		return respOkCreated(null);
	}
	
	public Response respOkCreated(Object entity) {
		return respOk(Response.Status.CREATED, entity);
	}
	
	public Response respOk() {
		return respOk(null, null);
	}
	
	public Response respOk(Response.Status status) {
		return respOk(status, null);
	}
	
	public Response respOk(Object entity) {
		return respOk(null, entity);
	}
	
	public Response respOk(Response.Status status, Object entity) {
		final Response.ResponseBuilder resp = (status != null) ? Response.status(status) : Response.ok();
		if (entity != null) resp.entity(entity);
		return resp.build();
	}
	
	public Response respErrorBadRequest() {
		return respErrorBadRequest(null);
	}
	
	public Response respErrorBadRequest(String message, Object... arguments) {
		return respError(Response.Status.BAD_REQUEST, message, arguments);
	}
	
	public Response respErrorNotFound() {
		return respErrorNotFound(null);
	}
	
	public Response respErrorNotFound(String message, Object... arguments) {
		return respError(Response.Status.NOT_FOUND, message, arguments);
	}
	
	public Response respErrorNotAllowed() {
		return respErrorNotAllowed(null);
	}
	
	public Response respErrorNotAllowed(String message, Object... arguments) {
		return respError(Response.Status.METHOD_NOT_ALLOWED, message, arguments);
	}
	
	public Response respError(Throwable t) {
		return respError(null, t.getMessage());
	}
	
	public Response respError(Response.Status status, String message, Object... arguments) {
		final Response.Status respStatus = (status != null) ? status : Response.Status.INTERNAL_SERVER_ERROR;
		final Response.ResponseBuilder resp = Response.status(respStatus);
		if (!StringUtils.isBlank(message)) {
			if ((arguments != null) && (arguments.length > 0)) {
				resp.entity(createErrorEntity(respStatus, MessageFormatter.arrayFormat(message, arguments).getMessage()));
				//resp.entity(MessageFormatter.arrayFormat(message, arguments).getMessage());
			} else {
				resp.entity(createErrorEntity(respStatus, message));
				//resp.entity(message);
			}
		}
		return resp.build();
	}
}
