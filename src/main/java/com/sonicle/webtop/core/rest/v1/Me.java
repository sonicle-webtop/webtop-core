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
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.model.Recipient;
import com.sonicle.webtop.core.model.RecipientFieldType;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.core.model.TagBase;
import com.sonicle.webtop.core.model.TagListOption;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.swagger.v1.api.MeApi;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiPlatformService;
import com.sonicle.webtop.core.swagger.v1.model.ApiRecipient;
import com.sonicle.webtop.core.swagger.v1.model.ApiTag;
import com.sonicle.webtop.core.swagger.v1.model.ApiTagBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Me extends MeApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(Me.class);
	
	private CoreManager getManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private CoreManager getManager(UserProfileId targetProfileId) {
		CoreManager manager = WT.getCoreManager(targetProfileId);
		manager.setSoftwareName("rest");
		return manager;
	}
	
	@Override
	public Response getPlatformServices() {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getPlatformServices()", RunContext.getRunProfileId());
		}
		
		try {
			Set<String> ids = manager.listAllowedServices();
			ArrayList<ApiPlatformService> items = new ArrayList<>(ids.size());
			for (String id : ids) {
				items.add(MeApiUtils.fillApiPlatformService(new ApiPlatformService(), id));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getPlatformServices()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}
	
	private BitFlags<TagListOption> setTagListOptions(BitFlags<TagListOption> options, String visibility) {
		if ("private".equalsIgnoreCase(visibility)) options.set(TagListOption.VISIBILITY_PRIVATE);
		if ("shared".equalsIgnoreCase(visibility)) options.set(TagListOption.VISIBILITY_SHARED);
		return options;
	}

	@Override
	public Response listTags(String visibility) {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listTags()", RunContext.getRunProfileId());
		}
		
		try {
			Map<String, Tag> tags = manager.listTags(setTagListOptions(BitFlags.noneOf(TagListOption.class), visibility));
			ArrayList<ApiTag> items = new ArrayList<>(tags.size());
			for (Tag tag : tags.values()) {
				items.add(MeApiUtils.fillApiTag(new ApiTag(), tag));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listTags()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response getTag(String tagId) {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getTag({})", RunContext.getRunProfileId(), tagId);
		}
		
		try {
			Tag tag = manager.getTag(tagId);
			if (tag == null) return respErrorNotFound();
			
			return respOk(MeApiUtils.fillApiTag(new ApiTag(), tag));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getTag({})", RunContext.getRunProfileId(), tagId, t);
			return respError(t);
		}
	}

	@Override
	public Response addTag(ApiTagBase body) {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addTag()", RunContext.getRunProfileId());
		}
		
		try {
			TagBase tag = MeApiUtils.fillTagBase(new TagBase(), body);
			Tag newTag = manager.addTag(tag);
			return respOkCreated(MeApiUtils.fillApiTag(new ApiTag(), newTag));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] addTag({})", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response updateTag(String tagId, ApiTagBase body) {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateTag({})", RunContext.getRunProfileId(), tagId);
		}
		
		try {
			Tag tag = manager.getTag(tagId);
			if (tag == null) return respErrorNotFound();
			
			MeApiUtils.fillTagBase(tag, body);
			manager.updateTag(tag.getTagId(), tag);
			return respOk();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] updateTag({})", RunContext.getRunProfileId(), tagId, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteTag(String tagId) {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteTag({})", RunContext.getRunProfileId(), tagId);
		}
		
		try {
			manager.deleteTag(tagId);
			return respOkNoContent();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] deleteTag({})", RunContext.getRunProfileId(), tagId, t);
			return respError(t);
		}
	}

	@Override
	public Response getRecipientsProviderSources() {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getRecipientsProviderSources()", RunContext.getRunProfileId());
		}
		
		try {
			return respOk(manager.listRecipientsProviderIDs());
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getRecipientsProviderSources()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response listRecipients(String addressType, String query, Integer maxResults, Long listOptions) {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listRecipients()", RunContext.getRunProfileId());
		}
		
		try {
			RecipientFieldType fieldType = EnumUtils.forSerializedName(addressType, RecipientFieldType.EMAIL, RecipientFieldType.class);
			BitFlags<CoreManager.ListRecipientsOption> options = MeApiUtils.toListRecipientsOption(listOptions);
			List<Recipient> recipients = manager.listRecipients(fieldType, query, maxResults, options);
			
			ArrayList<ApiRecipient> items = new ArrayList<>(recipients.size());
			for (Recipient recipient : recipients) {
				items.add(MeApiUtils.fillApiRecipient(new ApiRecipient(), recipient));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listRecipients()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}

	@Override
	public Response listRecipientsOfSources(String sourceProviders, String addressType, String query, Integer maxResults) {
		CoreManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] listRecipientsOfSources()", RunContext.getRunProfileId());
		}
		
		try {
			RecipientFieldType fieldType = EnumUtils.forSerializedName(addressType, RecipientFieldType.EMAIL, RecipientFieldType.class);
			Set<String> sources = ApiUtils.parseStringSet(sourceProviders);
			List<Recipient> recipients = manager.listRecipients(sources, fieldType, query, maxResults);
			
			ArrayList<ApiRecipient> items = new ArrayList<>(recipients.size());
			for (Recipient recipient : recipients) {
				items.add(MeApiUtils.fillApiRecipient(new ApiRecipient(), recipient));
			}
			return respOk(items);
			
		} catch (Throwable t) {
			LOGGER.error("[{}] listRecipientsOfSources()", RunContext.getRunProfileId(), t);
			return respError(t);
		}
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
			.code(status.getStatusCode())
			.description(message);
	}
}
