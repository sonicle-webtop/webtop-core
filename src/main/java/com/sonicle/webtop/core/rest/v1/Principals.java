/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.rest.v1;

import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.swagger.v1.api.PrincipalsApi;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiPrincipalInfo;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class Principals extends PrincipalsApi {

	@Override
	public Response getPrincipalInfo(String profileUsername, List<String> permRefs) {
		UserProfileId pid = WT.guessProfileIdByAuthAddress(profileUsername);
		if (pid == null) return respErrorNotFound();
		UserProfile.Data ud = WT.getUserData(pid);
		if (ud == null) return respErrorNotFound();
		
		ArrayList<Boolean> evalPermRefs = null;
		if (permRefs != null) {
			evalPermRefs = new ArrayList<>(permRefs.size());
			for (String permRef : permRefs) {
				evalPermRefs.add(RunContext.isPermitted(true, permRef));
			}
		}
		return respOk(createApiPrincipalInfo(pid, profileUsername, ud, evalPermRefs));
	}
	
	private ApiPrincipalInfo createApiPrincipalInfo(UserProfileId profileId, String profileUsername, UserProfile.Data data, ArrayList<Boolean> evalPermRefs) {
		return new ApiPrincipalInfo()
				.profileId(profileId.toString())
				.profileUsername(profileUsername)
				.displayName(StringUtils.defaultIfBlank(data.getDisplayName(), profileId.getUserId()))
				.emailAddress(data.getPersonalEmailAddress())
				.timezoneId(data.getTimeZoneId())
				.languageTag(data.getLanguageTag())
				.evalPermRefs(evalPermRefs);
	}

	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
				.code(status.getStatusCode())
				.description(message);
	}
}
