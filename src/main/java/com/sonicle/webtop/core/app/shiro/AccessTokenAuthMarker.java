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
package com.sonicle.webtop.core.app.shiro;

import java.io.Serializable;

/**
 * Marker added to the Shiro principal collection when the Subject authenticated
 * through the user-session ACCESS-TOKEN flow (Bearer token without
 * X-Auth-Username — the WebTop Connect mobile-app flow, see
 * {@link AuthTokenAccessToken} / WTRealm.authenticateAccessToken).
 *
 * <p>Living in the principal collection (not a thread/request flag) it survives
 * Shiro sessions and authentication caching, so
 * RunContext.isAuthenticatedByAccessToken() answers correctly on every request
 * of the authenticated Subject. Used to route ONLY app callers to the shared
 * per-user managers (hybrid SESSIONLESS_ONLY scope), while api-key
 * integrations and basic-auth DAV/EAS keep per-call throwaway managers.</p>
 *
 * @author gbulfon
 */
public final class AccessTokenAuthMarker implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final AccessTokenAuthMarker INSTANCE = new AccessTokenAuthMarker();

	private AccessTokenAuthMarker() {}

	@Override
	public String toString() {
		return "accessTokenAuth";
	}
}
