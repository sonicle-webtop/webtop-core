/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.rest.v1;

import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.config.bol.OPecBridgeFetcher;
import com.sonicle.webtop.core.config.bol.OPecBridgeRelay;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.products.MailBridgeProduct;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.swagger.v1.api.MailBridgeApi;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiFetcher;
import com.sonicle.webtop.core.swagger.v1.model.ApiRelay;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class MailBridge extends MailBridgeApi {
	private static final Logger logger = LoggerFactory.getLogger(MailBridge.class);
	
	@Override
	public Response mailBridgeListFetchers(String domainId) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ServiceLicense license = adminMgr.getLicense(domainId, MailBridgeProduct.PRODUCT_ID);
			List<ApiFetcher> items = new ArrayList<>();
			if (license != null && license.getActivationHwId() != null) {
				Integer qta = null; //no api mode, no license management, no qta limit
				boolean isSoftTW = false;
				if (WebTopProps.isMailBridgeModeApi(WebTopApp.getInstanceProperties())) {
					qta = ServiceLicense.computeLicenseQuantity(license);
					isSoftTW = ServiceLicense.isInsideSoftTimeWindow(license);
				}
				for (OPecBridgeFetcher fetcher : adminMgr.listPecBridgeFetchers(domainId)) {
					if (!fetcher.getEnabled()) continue;
					//Add only until we have licensed items or if inside soft time window or not api mode
					if (isSoftTW || qta == null || items.size() < qta) items.add(createApiFetcher(fetcher));
				}
			}
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] mailBridgeListFetchers()", currentProfileId, ex);
			return respError(ex);
		}
	}
	
	@Override
	public Response mailBridgeListRelays(String domainId) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		
		try {
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ServiceLicense license = adminMgr.getLicense(domainId, MailBridgeProduct.PRODUCT_ID);
			List<ApiRelay> items = new ArrayList<>();
			if (license != null && license.getActivationHwId() != null) {
				Integer qta = null; //no api mode, no license management, no qta limit
				boolean isSoftTW = false;
				if (WebTopProps.isMailBridgeModeApi(WebTopApp.getInstanceProperties())) {
					qta = ServiceLicense.computeLicenseQuantity(license);
					isSoftTW = ServiceLicense.isInsideSoftTimeWindow(license);
				}
				for (OPecBridgeRelay relay : adminMgr.listPecBridgeRelays(domainId)) {
					if (!relay.getEnabled()) continue;
					//Add only until we have licensed items or if inside soft time window or not api mode
					if (isSoftTW || qta == null || items.size() < qta) items.add(createApiRelay(relay));
				}
			}
			return respOk(items);
			
		} catch(Exception ex) {
			logger.error("[{}] mailBridgeListFetchers()", currentProfileId, ex);
			return respError(ex);
		}
	}
	
	@Override
	public Response mailBridgeUpdateFetcherAuthState(String webtopProfileId, String state, String body) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		
		try {
			String sv[]  = StringUtils.split(webtopProfileId, '@');
			if (sv==null || sv.length<2) throw new WTException("Invalid Webtop Profile ID");
			String domainId = sv[1];
			
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ServiceLicense license = adminMgr.getLicense(domainId, MailBridgeProduct.PRODUCT_ID);
			if (license == null || license.getActivationHwId() == null) throw new WTException("Invalid MailBrdige license");
			
			adminMgr.updatePecBridgeFetcherAuthState(webtopProfileId, state);
			
			return respOk();
			
		} catch(Exception ex) {
			logger.error("[{}] mailBridgeListFetchers()", currentProfileId, ex);
			return respError(ex);
		}
	}	
	
	@Override
	public Response mailBridgeUpdateRelayAuthState(String webtopProfileId, String state, String body) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		
		try {
			String sv[]  = StringUtils.split(webtopProfileId, '@');
			if (sv==null || sv.length<2) throw new WTException("Invalid Webtop Profile ID");
			String domainId = sv[1];
			
			CoreAdminManager adminMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ServiceLicense license = adminMgr.getLicense(domainId, MailBridgeProduct.PRODUCT_ID);
			if (license == null || license.getActivationHwId() == null) throw new WTException("Invalid MailBrdige license");
			
			adminMgr.updatePecBridgeRelayAuthState(webtopProfileId, state);
			
			return respOk();
			
		} catch(Exception ex) {
			logger.error("[{}] mailBridgeListFetchers()", currentProfileId, ex);
			return respError(ex);
		}
	}	

	private ApiFetcher createApiFetcher(OPecBridgeFetcher fetcher) {
		return new ApiFetcher()
				.authState(fetcher.getAuthState())
				.deleteOnForward(fetcher.getDeleteOnForward())
				.forwardAddress(fetcher.getForwardAddress())
				.host(fetcher.getHost())
				.password(fetcher.getPassword())
				.port(fetcher.getPort().intValue())
				.protocol(fetcher.getProtocol())
				.username(fetcher.getUsername())
				.webtopProfileId(fetcher.getWebtopProfileId());
	}
	
	private ApiRelay createApiRelay(OPecBridgeRelay relay) {
		return new ApiRelay()
				.authState(relay.getAuthState())
				.debug(relay.getDebug())
				.host(relay.getHost())
				.matcher(relay.getMatcher())
				.password(relay.getPassword())
				.port(relay.getPort().intValue())
				.protocol(relay.getProtocol())
				.ssl(true)
				.starttls(false)
				.username(relay.getUsername())
				.webtopProfileId(relay.getWebtopProfileId());
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
				.code(status.getStatusCode())
				.description(message);
	}
}
