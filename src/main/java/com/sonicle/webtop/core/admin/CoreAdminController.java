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
package com.sonicle.webtop.core.admin;

import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.events.LicenseUpdateEvent;
import com.sonicle.webtop.core.products.MailBridgeProduct;
import com.sonicle.webtop.core.sdk.BaseController;
import java.util.Properties;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreAdminController extends BaseController {
	public static final Logger logger = WT.getLogger(CoreAdminController.class);
	
	public CoreAdminController() {
		super();
	}
	
	@Handler
	public void onLicenseUpdateEvent(LicenseUpdateEvent event) {
		if (event.getProduct() instanceof MailBridgeProduct) {
			String domainId = event.getDomainId();
			String host = null;
			int port = 25;
			boolean auth = false;
			if (LicenseUpdateEvent.Type.ACTIVATE.equals(event.getType())) {
				Properties props = WebTopApp.getInstanceProperties();
				host = WebTopProps.getMailBridgeSMTPHost(props);
				port = WebTopProps.getMailBridgeSMTPPort(props);
				auth = WebTopProps.getMailBridgeSMTPAuth(props);
				SettingsManager sm = WebTopApp.getInstance().getSettingsManager();
				sm.setServiceSetting(domainId, CoreManifest.ID, CoreSettings.SMTP_HOST, host);
				sm.setServiceSetting(domainId, CoreManifest.ID, CoreSettings.SMTP_PORT, new Integer(port));
				sm.setServiceSetting(domainId, CoreManifest.ID, CoreSettings.SMTP_AUTH, new Boolean(auth));
				sm.clearDomainSettingsCache();
			}
			else if (LicenseUpdateEvent.Type.DEACTIVATE.equals(event.getType())) {
				SettingsManager sm = WebTopApp.getInstance().getSettingsManager();
				sm.deleteServiceSetting(domainId, CoreManifest.ID, CoreSettings.SMTP_HOST);
				sm.deleteServiceSetting(domainId, CoreManifest.ID, CoreSettings.SMTP_PORT);
				sm.deleteServiceSetting(domainId, CoreManifest.ID, CoreSettings.SMTP_AUTH);
				sm.clearDomainSettingsCache();
			}
			if (host!=null) {
			}
		}			
	}
	
}
