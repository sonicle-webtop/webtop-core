/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.rest.v1;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreManager.ListRecipientsOption;
import com.sonicle.webtop.core.model.Recipient;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.core.model.TagBase;
import com.sonicle.webtop.core.sdk.BaseRestApiUtils;
import com.sonicle.webtop.core.swagger.v1.model.ApiPlatformService;
import com.sonicle.webtop.core.swagger.v1.model.ApiRecipient;
import com.sonicle.webtop.core.swagger.v1.model.ApiTag;
import com.sonicle.webtop.core.swagger.v1.model.ApiTagBase;

/**
 *
 * @author malbinola
 */
public class MeApiUtils extends BaseRestApiUtils {
	
	public static ApiPlatformService fillApiPlatformService(final ApiPlatformService tgt, final String src) {
		tgt.setId(src);
		return tgt;
	}
	
	public static TagBase fillTagBase(final TagBase tgt, final ApiTagBase src) {
		tgt.setVisibility(EnumUtils.forSerializedName(src.getVisibility().value(), TagBase.Visibility.class));
		tgt.setBuiltIn(src.getBuiltIn());
		tgt.setName(src.getName());
		tgt.setColor(src.getColor());
		tgt.setExternalId(src.getExternalId());
		return tgt;
	}
	
	public static ApiTag fillApiTag(final ApiTag tgt, final Tag src) {
		fillApiTagBase(tgt, src);
		tgt.setId(src.getTagId());
		return tgt;
	}
	
	public static ApiTagBase fillApiTagBase(final ApiTagBase tgt, final TagBase src) {
		tgt.setVisibility(ApiTag.VisibilityEnum.fromValue(EnumUtils.toSerializedName(src.getVisibility())));
		tgt.setBuiltIn(src.getBuiltIn());
		tgt.setName(src.getName());
		tgt.setColor(src.getColor());
		tgt.setExternalId(src.getExternalId());
		return tgt;
	}
	
	public static ApiRecipient fillApiRecipient(final ApiRecipient tgt, final Recipient src) {
		tgt.setSourceId(src.getProviderId());
		tgt.setSourceName(src.getProviderName());
		tgt.setOrigin(src.getOrigin());
		tgt.setRecipientId(src.getRecipientId());
		tgt.setAddress(src.getAddress());
		tgt.setPersonal(src.getPersonal());
		tgt.setRcptType(ApiRecipient.RcptTypeEnum.fromValue(EnumUtils.toSerializedName(src.getRcptType())));
		return tgt;
	}
	
	public static BitFlags<ListRecipientsOption> toListRecipientsOption(Long options) {
		if (options == null) {
			return BitFlags.noneOf(ListRecipientsOption.class);
		} else {
			return BitFlags.newFrom(ListRecipientsOption.class, options);
		}
	}
}
