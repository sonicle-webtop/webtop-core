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
package com.sonicle.webtop.core;

import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsUserOptions;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.sdk.BaseUserOptionsService;
import com.sonicle.webtop.core.sdk.JsOptions;
import com.sonicle.webtop.core.sdk.UserData;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.userdata.UserDataProviderBase;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class UserOptionsService extends BaseUserOptionsService {
	
	public static final Logger logger = WT.getLogger(UserOptionsService.class);
	
	public void processUserOptions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			
			con = getCoreConnection();
			CoreServiceSettings ss = new CoreServiceSettings(getDomainId(), CoreManifest.ID);
			CoreUserSettings us = new CoreUserSettings(getDomainId(), getUserId());
			UserDAO udao = UserDAO.getInstance();
			OUser user = udao.selectByDomainUser(con, getDomainId(), getUserId());
			if(user == null) throw new WTException("Unable to find a user [{0}, {1}]", getDomainId(), getUserId());
			
			UserDataProviderBase udp = getUserDataProvider();
			UserData ud = udp.getUserData(user.getDomainId(), user.getUserId());
			
			if(crud.equals("read")) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				// main
				JsOptions main = new JsOptions();
				main.put("displayName", user.getDisplayName());
				main.put("rtl", us.getRightToLeft());
				main.put("theme", us.getTheme());
				main.put("layout", us.getLayout());
				main.put("laf", us.getLookAndFeel());
				
				// i18n
				JsOptions i18n = new JsOptions();
				i18n.put("locale", user.getLocale());
				i18n.put("timezone", user.getTimezone());
				i18n.put("startDay", us.getStartDay());
				i18n.put("shortDateFormat", us.getShortDateFormat());
				i18n.put("longDateFormat", us.getLongDateFormat());
				i18n.put("shortTimeFormat", us.getShortTimeFormat());
				i18n.put("longTimeFormat", us.getLongTimeFormat());
				
				// TFA
				JsOptions tfa = new JsOptions();
				tfa.put("enabled", ss.getTFAEnabled());
				tfa.put("deviceTrustEnabled", ss.getTFADeviceTrustEnabled());
				tfa.put("mandatory", us.getTFAMandatory());
				tfa.put("delivery", us.getTFADelivery());
				tfa.put("emailAddress", us.getTFAEmailAddress());
				
				// TFA - trusted device
				TFAManager tfam = WebTopApp.getInstance().getTFAManager(); //TODO: avoid this
				boolean isTrusted = false;
				String trustedOn = null;
				TrustedDeviceCookie tdc = tfam.readTrustedDeviceCookie(getDomainId(), getUserId(), user.getSecret(), request);
				if(tfam.isThisDeviceTrusted(getDomainId(), getUserId(), tdc)) {
					JsTrustedDevice td = tfam.getTrustedDevice(getDomainId(), getUserId(), tdc.deviceId);
					if(td != null) {
						isTrusted = true;
						trustedOn = td.getISOTimestamp();
					}
				}
				tfa.put("isTrusted", isTrusted);
				tfa.put("trustedOn", trustedOn);
				
				
				JsOptions opts = new JsOptions();
				opts.put("id", id);
				opts.putAll(main);
				opts.putAll(i18n);
				opts.putPrefixed("tfa", tfa);
				opts.putPrefixed("usd", ud.getMap());
				new JsonResult(opts).printTo(out);
				
			} else if(crud.equals("update")) {
				Payload<MapItem, JsUserOptions> pl = ServletUtils.getPayload(request, JsUserOptions.class);
				
				// main
				if(pl.map.has("displayName")) user.setDisplayName(pl.data.displayName);
				if(pl.map.has("theme")) us.setTheme(pl.data.theme);
				if(pl.map.has("layout")) us.setLayout(pl.data.layout);
				if(pl.map.has("laf")) us.setLookAndFeel(pl.data.laf);
				
				// i18n
				if(pl.map.has("locale")) user.setLanguageTag(pl.data.locale);
				if(pl.map.has("timezone")) user.setTimezone(pl.data.timezone);
				if(pl.map.has("startDay")) us.setStartDay(pl.data.startDay);
				if(pl.map.has("shortDateFormat")) us.setShortDateFormat(pl.data.shortDateFormat);
				if(pl.map.has("longDateFormat")) us.setLongDateFormat(pl.data.longDateFormat);
				if(pl.map.has("shortTimeFormat")) us.setShortTimeFormat(pl.data.shortTimeFormat);
				if(pl.map.has("longTimeFormat")) us.setLongTimeFormat(pl.data.longTimeFormat);
				
				udao.update(con, user);
				
				// TFA
				//TODO: gestire salvataggio TFA
				/*
				if(pl.map.has("mandatory")) {
					//TODO: do check using shiro
					if(getSessionProfile().isWebTopAdmin()) {
						us.setTFAMandatory(pl.data.getBoolean("mandatory"));
					}
				}
				*/
				
				// UserData
				if(udp.canWrite()) {
					if(pl.map.has("usdTitle")) ud.title = pl.data.usdTitle;
					if(pl.map.has("usdFirstName")) ud.firstName = pl.data.usdFirstName;
					if(pl.map.has("usdFirstName")) ud.lastName = pl.data.usdFirstName;
					if(pl.map.has("usdEmail")) ud.email = pl.data.usdEmail;
					if(pl.map.has("usdMobile")) ud.mobile = pl.data.usdMobile;
					if(pl.map.has("usdTelephone")) ud.telephone = pl.data.usdTelephone;
					if(pl.map.has("usdFax")) ud.fax = pl.data.usdFax;
					if(pl.map.has("usdAddress")) ud.address = pl.data.usdAddress;
					if(pl.map.has("usdPostalCode")) ud.postalCode = pl.data.usdPostalCode;
					if(pl.map.has("usdCity")) ud.city = pl.data.usdCity;
					if(pl.map.has("usdState")) ud.state = pl.data.usdState;
					if(pl.map.has("usdCountry")) ud.country = pl.data.usdCountry;
					if(pl.map.has("usdCompany")) ud.company = pl.data.usdCompany;
					if(pl.map.has("usdFunction")) ud.function = pl.data.usdFunction;
					if(pl.map.has("usdWorkEmail")) ud.workEmail = pl.data.usdWorkEmail;
					if(pl.map.has("usdWorkMobile")) ud.workMobile = pl.data.usdWorkMobile;
					if(pl.map.has("usdWorkTelephone")) ud.workTelephone = pl.data.usdWorkTelephone;
					if(pl.map.has("usdWorkFax")) ud.workFax = pl.data.usdWorkFax;
					if(pl.map.has("usdCustom1")) ud.custom1 = pl.data.usdCustom1;
					if(pl.map.has("usdCustom2")) ud.custom2 = pl.data.usdCustom2;
					if(pl.map.has("usdCustom3")) ud.custom3 = pl.data.usdCustom3;
					udp.setUserData(getDomainId(), getUserId(), ud);
				}
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error executing action UserOptions", ex);
			new JsonResult(false).printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processDisableTFA(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			TFAManager tfam = WebTopApp.getInstance().getTFAManager(); //TODO: avoid this
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action DisableTFA", ex);
			new JsonResult(false).printTo(out);
		}
	}
}
