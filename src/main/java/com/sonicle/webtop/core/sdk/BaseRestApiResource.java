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
import com.sonicle.webtop.core.app.AbstractPlatformService;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.ClassUtils;
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
	
	protected UserProfileId getTargetProfileId(final String targetProfileId) {
		Subject subject = RunContext.getSubject();
		if (RunContext.isSysAdmin(subject)) {
			return StringUtils.isBlank(targetProfileId) ? null : new UserProfileId(targetProfileId);
		} else {
			return StringUtils.isBlank(targetProfileId) ? RunContext.getRunProfileId(subject) : new UserProfileId(targetProfileId);
		}
	}
	
	protected String userIdOrDefault(final String userId) {
		return BaseRestApiUtils.parseUserId(userId, RunContext.getRunProfileId().getUserId());
	}
	
	protected abstract Object createErrorEntity(Response.Status status, String message);
	
	protected Response respOkNoContent() {
		return respOk(Response.Status.NO_CONTENT);
	}
	
	protected Response respOkCreated() {
		return respOkCreated(null);
	}
	
	protected Response respOkCreated(final Object entity) {
		return respOk(Response.Status.CREATED, entity, null);
	}
	
	protected Response respOk() {
		return respOk(null, null, null);
	}
	
	protected Response respOk(final Response.Status status) {
		return respOk(status, null, null);
	}
	
	protected Response respOk(final Object entity) {
		return respOk(null, entity, null);
	}
	
	protected Response respOk(final Object entity, final String mediaType) {
		return respOk(null, entity, mediaType);
	}
	
	protected Response respOk(final Response.Status status, final Object entity, final String mediaType) {
		final Response.ResponseBuilder resp = (status != null) ? Response.status(status) : Response.ok();
		if (entity != null) resp.entity(entity);
		if (mediaType != null) resp.type(mediaType);
		return resp.build();
	}
	
	protected Response respErrorBadRequest() {
		return respErrorBadRequest(null);
	}
	
	protected Response respErrorBadRequest(final String message, final Object... arguments) {
		return respError(Response.Status.BAD_REQUEST, message, arguments);
	}
	
	protected Response respErrorNotFound() {
		return respErrorNotFound(null);
	}
	
	protected Response respErrorNotFound(final String message, final Object... arguments) {
		return respError(Response.Status.NOT_FOUND, message, arguments);
	}
	
	protected Response respErrorNotAllowed() {
		return respErrorNotAllowed(null);
	}
	
	protected Response respErrorNotAllowed(final String message, final Object... arguments) {
		return respError(Response.Status.METHOD_NOT_ALLOWED, message, arguments);
	}
	
	protected Response respErrorForbidden(final String message, final Object... arguments) {
		return respError(Response.Status.FORBIDDEN, message, arguments);
	}
	
	protected Response respError(final Throwable t) {
		if (t instanceof AuthException) {
			return respErrorForbidden(t.getMessage());
		} else if (t instanceof WTNotFoundException) {
			return respErrorNotFound(t.getMessage());
		} else if (t instanceof WTParseException) {
			return respErrorBadRequest(t.getMessage());
		}  else {
			if (t != null) {
				if ("net.sf.qualitycheck.exception".equals(ClassUtils.getPackageName(LangUtils.getDeepestCause(t).getClass()))) {
					return respErrorBadRequest(t.getMessage());
				} else {
					// Exceptions can have null message (eg. NPE) so in this cases
					// fill message with the full-name of the exception Class
					final String message = t.getMessage();
					return respError(null, (message != null) ? message : t.getClass().getCanonicalName());
				}
			} else {
				return respError(null);
			}
		}
	}
	
	protected Response respError(final Response.Status status, final String message, final Object... arguments) {
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
