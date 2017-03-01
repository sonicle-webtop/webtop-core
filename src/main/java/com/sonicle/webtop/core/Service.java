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

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.webtop.core.admin.CoreAdminManager;
import com.sonicle.webtop.core.app.CoreAdminManifest;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.CorePrivateEnvironment;
import com.sonicle.webtop.core.app.OTPManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.provider.RecipientsProviderBase;
import com.sonicle.webtop.core.bol.VActivity;
import com.sonicle.webtop.core.bol.CausalGrid;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OAutosave;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsActivity;
import com.sonicle.webtop.core.bol.js.JsAutosave;
import com.sonicle.webtop.core.bol.js.JsCausal;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsFeedback;
import com.sonicle.webtop.core.bol.js.JsGridSync;
import com.sonicle.webtop.core.bol.js.JsInternetAddress;
import com.sonicle.webtop.core.bol.js.JsReminderInApp;
import com.sonicle.webtop.core.bol.js.JsRoleLkp;
import com.sonicle.webtop.core.bol.js.JsServicePermissionLkp;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsWhatsnewTab;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.bol.model.InternetRecipient;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.bol.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTLocalizedException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TimeZone;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	private static final Logger logger = WT.getLogger(Service.class);
	public static final String WTSPROP_OTP_SETUP = "OTPSETUP";
	
	private CoreManager coreMgr;
	private CoreServiceSettings ss;
	private CoreUserSettings us;
	
	/*
	private WebTopApp getApp() {
		return ((CorePrivateEnvironment)getEnv()).getApp();
	}
	*/
	
	@Override
	public void initialize() throws Exception {
		coreMgr = getCoreManager();
		ss = new CoreServiceSettings(SERVICE_ID, getEnv().getProfileId().getDomainId());
		us = new CoreUserSettings(getEnv().getProfileId());
	}
	
	private CoreManager getCoreManager() {
		return (CoreManager)WT.getServiceManager(SERVICE_ID);
	}
	
	private CoreAdminManager getCoreAdminManager() {
		return (CoreAdminManager)WT.getServiceManager(CoreAdminManifest.ID);
	}

	@Override
	public void cleanup() throws Exception {
		
	}

	@Override
	public ServiceVars returnServiceVars() {
		UserProfile profile = getEnv().getProfile();
		ServiceVars co = new ServiceVars();
		
		boolean domainPasswordPolicy = false;
		boolean dirCapPasswordWrite = false;
		try {
			if (profile.getDomainId().equals(WebTopManager.DOMAINID_SYSADMIN)) {
				dirCapPasswordWrite = true;
			} else {
				ODomain domain = coreMgr.getDomain();
				if (domain != null) {
					domainPasswordPolicy = domain.getDirPasswordPolicy();
					AbstractDirectory dir = coreMgr.getAuthDirectory(domain);
					dirCapPasswordWrite = dir.hasCapability(DirectoryCapability.PASSWORD_WRITE);
				}
			}
		} catch(WTException ex) {}
		
		co.put("wtAddonNotifier", addonNotifier());
		co.put("wtWhatsnewEnabled", ss.getWhatsnewEnabled());
		co.put("wtOtpEnabled", ss.getOTPEnabled());
		co.put("wtUploadMaxFileSize", ss.getUploadMaxFileSize());
		co.put("domainPasswordPolicy", domainPasswordPolicy);
		co.put("domainDirCapPasswordWrite", dirCapPasswordWrite);
		co.put("domainInternetName", WT.getDomainInternetName(profile.getDomainId()));
		co.put("profileId", profile.getStringId());
		co.put("domainId", profile.getDomainId());
		co.put("userId", profile.getUserId());
		co.put("userDisplayName", profile.getDisplayName());
		
		co.put("theme", us.getTheme());
		co.put("laf", us.getLookAndFeel());
		co.put("layout", us.getLayout());
		co.put("desktopNotification", us.getDesktopNotification());
		
		co.put("language", us.getLanguageTag());
		co.put("timezone", us.getTimezone());
		co.put("startDay", us.getStartDay());
		co.put("shortDateFormat", us.getShortDateFormat());
		co.put("longDateFormat", us.getLongDateFormat());
		co.put("shortTimeFormat", us.getShortTimeFormat());
		co.put("longTimeFormat", us.getLongTimeFormat());
		
		return co;
	}
	
	private String addonNotifier() {
		String url = ss.getAddonNotifierUrl();
		if (!StringUtils.isBlank(url)) return url;
		return LangUtils.classForNameQuietly("com.sonicle.webtop.addons.AddOns") ? "true" : "false";
	}
	
	private WebTopSession getWts() {
		return getEnv().getWebTopSession();
	}
	
	public void processChangeUserPassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			char[] oldPassword = ServletUtils.getStringParameter(request, "oldPassword", true).toCharArray();
			char[] newPassword = ServletUtils.getStringParameter(request, "newPassword", true).toCharArray();
			
			coreMgr.updateUserPassword(oldPassword, newPassword);
			
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in ChangeUserPassword", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupLanguages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		LinkedHashMap<String, JsSimple> items = new LinkedHashMap<>();
		Locale locale = getEnv().getWebTopSession().getLocale();
		
		try {
			for(AppLocale apploc : WT.getInstalledLocales()) {
				final Locale loc = apploc.getLocale();
				final String lang = loc.getLanguage();
				if(!items.containsKey(lang)) {
					//items.put(lang, new JsSimple(lang, loc.getDisplayLanguage(locale)));
					items.put(lang, new JsSimple(apploc.getId(), apploc.getLocale().getDisplayName(locale)));
				}
			}
			new JsonResult("languages", items.values(), items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupLanguages", ex);
			new JsonResult(false, "Unable to lookup languages").printTo(out);
		}
	}
	
	public void processLookupTimezones(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			int off;
			for(TimeZone tz : WT.getTimezones()) {
				final String normId = StringUtils.replace(tz.getID(), "_", " ");
				off = tz.getRawOffset()/3600000;
				items.add(new JsSimple(tz.getID(), MessageFormat.format("{0} (GMT{1}{2})", normId, (off<0) ? "-" : "+", Math.abs(off))));
			}
			new JsonResult("timezones", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupTimezones", ex);
			new JsonResult(false, "Unable to lookup timezones").printTo(out);
		}
	}
	
	public void processLookupThemes(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = null;
		
		try {
			items = coreMgr.listThemes();
			new JsonResult("themes", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error in LookupThemes", ex);
			new JsonResult(false, "Unable to lookup themes").printTo(out);
		}
	}
	
	public void processLookupLayouts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = null;
		
		try {
			items = coreMgr.listLayouts();
			new JsonResult("layouts", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error in LookupLayouts", ex);
			new JsonResult(false, "Unable to lookup layouts").printTo(out);
		}
	}
	
	public void processLookupLAFs(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = null;
		
		try {
			items = coreMgr.listLAFs();
			new JsonResult("lafs", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error in LookupLAFs", ex);
			new JsonResult(false, "Unable to lookup look&feels").printTo(out);
		}
	}
	
	public void processLookupTextEncodings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			SortedMap<String, Charset> charsets = Charset.availableCharsets();
			for(Charset charset : charsets.values()) {
				if(!charset.canEncode()) continue;
				items.add(new JsSimple(charset.name(), charset.displayName(Locale.ENGLISH)));
			}
			new JsonResult("encodings", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error in LookupTextEncodings", ex);
			new JsonResult(false, "Unable to lookup available text encodings").printTo(out);
		}
	}
	
	public void processLookupServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Locale locale = getEnv().getWebTopSession().getLocale();
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			Boolean assignableOnly = ServletUtils.getBooleanParameter(request, "assignableOnly", false);
			
			for(String sid : coreMgr.listInstalledServices()) {
				if(assignableOnly && sid.equals(CoreManifest.ID)) continue;
				items.add(new JsSimple(sid, WT.lookupResource(sid, locale, BaseService.RESOURCE_SERVICE_NAME)));
			}
			new JsonResult(items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupServices", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupServicesPermissions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsServicePermissionLkp> items = new ArrayList<>();
		
		try {
			for(String sid : coreMgr.listInstalledServices()) {
				for(ServicePermission perm : coreMgr.listServicePermissions(sid)) {
					for(String action : perm.getActions()) {
						items.add(new JsServicePermissionLkp(sid, perm.getGroupName(), action));
					}
				}
			}
			new JsonResult(items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupServicesPermissions", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	
	
	
	
	
	
	
	
	
	public void processLookupDomains(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			/*
			if(getRunContext().isSysAdmin()) {
				// WebTopAdmin can access to all domains
				if(wildcard) items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
				List<ODomain> domains = core.listDomains(true);
				for(ODomain domain : domains) {
					items.add(new JsSimple(domain.getDomainId(), JsSimple.description(domain.getDescription(), domain.getDomainId())));
				}
			} else {
				// Domain users can only access to their domain
				ODomain domain = core.getDomain(up.getDomainId());
				items.add(new JsSimple(domain.getDomainId(), JsSimple.description(domain.getDescription(), domain.getDomainId())));
			}
			*/
			
			if(wildcard && RunContext.isSysAdmin()) {
				items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			}
			List<ODomain> domains = coreMgr.listDomains(true);
			for(ODomain domain : domains) {
				items.add(new JsSimple(domain.getDomainId(), JsSimple.description(domain.getDescription(), domain.getDomainId())));
			}
			new JsonResult("domains", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupDomains", ex);
			new JsonResult(false, "Unable to lookup domains").printTo(out);
		}
	}
	
	public void processLookupDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsRoleLkp> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean users = ServletUtils.getBooleanParameter(request, "users", true);
			boolean groups = ServletUtils.getBooleanParameter(request, "groups", true);
			boolean roles = ServletUtils.getBooleanParameter(request, "roles", true);
			
			if(wildcard) items.add(JsRoleLkp.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			if(users) {
				for(Role role : coreMgr.listUsersRoles()) {
					items.add(new JsRoleLkp(role, RoleWithSource.SOURCE_USER));
				}
			}
			if(groups) {
				for(Role role : coreMgr.listGroupsRoles()) {
					items.add(new JsRoleLkp(role, RoleWithSource.SOURCE_GROUP));
				}
			}
			if (roles) {
				for(Role role : coreMgr.listRoles()) {
					items.add(new JsRoleLkp(role, RoleWithSource.SOURCE_ROLE));
				}
			}
			
			new JsonResult("roles", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupDomainRoles", ex);
			new JsonResult(false, "Unable to lookup roles").printTo(out);
		}
	}
	
	public void processLookupDomainUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean fullId = ServletUtils.getBooleanParameter(request, "fullId", false);
			String domainId = ServletUtils.getStringParameter(request, "domainId", null);
			
			List<OUser> users;
			if(RunContext.isSysAdmin()) {
				if(!StringUtils.isEmpty(domainId)) {
					CoreAdminManager coreAdmMgr = getCoreAdminManager();
					users = coreAdmMgr.listUsers(domainId, true);
				} else {
					users = coreMgr.listUsers(true);
				}
			} else { // Domain users can only see users belonging to their own domain
				users = coreMgr.listUsers(true);
			}
			
			if(wildcard) items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			for(OUser user : users) {
				final String id = fullId ? new UserProfile.Id(user.getDomainId(), user.getUserId()).toString() : user.getUserId();
				items.add(new JsSimple(id, JsSimple.description(user.getDisplayName(), user.getUserId())));
			}
			
			new JsonResult("users", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupUsers", ex);
			new JsonResult(false, "Unable to lookup users").printTo(out);
		}
	}
	
	public void processLookupActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsActivity> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			UserProfile.Id pid = new UserProfile.Id(profileId);
			
			//TODO: tradurre campo descrizione in base al locale dell'utente
			List<OActivity> activities = coreMgr.listLiveActivities(pid);
			for(OActivity activity : activities) {
				items.add(new JsActivity(activity));
			}
			
			new JsonResult("activities", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupActivities", ex);
			new JsonResult(false, "Unable to lookup activities").printTo(out);
		}
	}
	
	public void processManageActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				if(id == null) {
					List<VActivity> items = coreMgr.listLiveActivities(queryDomains());
					new JsonResult("activities", items, items.size()).printTo(out);
				} else {
					OActivity item = coreMgr.getActivity(id);
					new JsonResult(item).printTo(out);
				}
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				coreMgr.addActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				coreMgr.updateActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				coreMgr.deleteActivity(pl.data.getActivityId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageActivities", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
	
	public void processLookupCausals(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCausal> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			String customerId = ServletUtils.getStringParameter(request, "customerId", null);
			UserProfile.Id pid = new UserProfile.Id(profileId);
			
			//TODO: tradurre campo descrizione in base al locale dell'utente
			List<OCausal> causals = coreMgr.listLiveCausals( pid, customerId);
			for(OCausal causal : causals) {
				items.add(new JsCausal(causal));
			}
			new JsonResult("causals", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupCausals", ex);
			new JsonResult(false, "Unable to lookup causals").printTo(out);
		}
	}
	
	public void processManageCausals(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				if(id == null) {
					List<CausalGrid> items =  coreMgr.listLiveCausals(queryDomains());
					new JsonResult("causals", items, items.size()).printTo(out);
				} else {
					OCausal item = coreMgr.getCausal(id);
					new JsonResult(item).printTo(out);
				}
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				coreMgr.addCausal(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				coreMgr.updateCausal(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				coreMgr.deleteCausal(pl.data.getCausalId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageCausals", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupCustomers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			String query = ServletUtils.getStringParameter(request, "query", "");
			
			List<OCustomer> customers = coreMgr.listCustomersByLike("%" + query + "%");
			for(OCustomer customer : customers) {
				items.add(new JsSimple(customer.getCustomerId(), customer.getDescription()));
			}
			new JsonResult("customers", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupCustomers", ex);
			new JsonResult(false, "Error in LookupCustomers").printTo(out);
		}
	}
	
	public void processLookupStatisticCustomers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		List<JsSimple> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			String parentCustomerId = ServletUtils.getStringParameter(request, "parentCustomerId", null);
			String query = ServletUtils.getStringParameter(request, "query", "");
			UserProfile.Id pid = new UserProfile.Id(profileId);
			CustomerDAO cdao = CustomerDAO.getInstance();
			con = WT.getCoreConnection();
			
			//TODO: spostare recupero nel manager
			List<OCustomer> customers = cdao.viewByParentDomainLike(con, parentCustomerId, pid.getDomainId(), "%" + query + "%");
			ArrayList<String> parts = null;
			String address = null, description;
			for(OCustomer customer : customers) {
				parts = new ArrayList<>();
				if(!StringUtils.isEmpty(customer.getAddress())) {
					parts.add(customer.getAddress());
				}
				if(!StringUtils.isEmpty(customer.getCity())) {
					parts.add(customer.getCity());
				}
				if(!StringUtils.isEmpty(customer.getCountry())) {
					parts.add(customer.getCountry());
				}
				address = StringUtils.join(parts, ", ");
				if(StringUtils.isEmpty(address)) {
					description = customer.getDescription();
				} else {
					description = MessageFormat.format("{0} ({1})", customer.getDescription(), address);
				}
				items.add(new JsSimple(customer.getCustomerId(), description));
			}
			
			new JsonResult("customers", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupStatisticCustomers", ex);
			new JsonResult(false, "Error in LookupStatisticCustomers").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	
	
	
	
	
	
	
	
	/*
	public void processGetOptionsUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con = null;
		UserProfile up = getEnv().getProfile();
		
		try {
			ArrayList<JsSimple> data = new ArrayList<>();
			if(up.isWebTopAdmin()) {
				con = WT.getCoreConnection();
				UserDAO udao = UserDAO.getInstance();
				List<OUser> users = udao.selectAll(con);
				String id = null, descr = null;
				for(OUser user : users) {
					id = DomainAccount.buildName(user.getDomainId(), user.getUserId());
					descr = MessageFormat.format("{0} ({1})", user.getDisplayName(), id);
					data.add(new JsSimple(id, descr));
				}
				
			} else {
				//TODO: maybe define a permission to other users to control others options
				data.add(new JsSimple(up.getStringId(), up.getDisplayName()));
			}
			new JsonResult("users", data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetOptionsUsers", ex);
			new JsonResult(false, "Unable to get users").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	public void processGetOptionsServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<UserOptionsServiceData> data = null;
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			
			UserProfile.Id targetPid = new UserProfile.Id(id);
			if(getWts().getProfileId().equals(targetPid)) {
				data = coreMgr.listUserOptionServices();
			} else {
				CoreManager xcore = WT.getCoreManager(targetPid);
				data = xcore.listUserOptionServices();
			}
			
			/*
			//TODO: aggiornare l'implementazione
			data.add(new UserOptionsServiceData("com.sonicle.webtop.core", "wt", "WebTop Services", "Sonicle.webtop.core.view.CoreOptions"));
			if(!UserProfile.isWebTopAdmin(id)) data.add(new UserOptionsServiceData("com.sonicle.webtop.calendar", "wtcal", "Calendario", "Sonicle.webtop.calendar.CalendarOptions"));
			if(!UserProfile.isWebTopAdmin(id)) data.add(new UserOptionsServiceData("com.sonicle.webtop.mail", "wtmail", "Posta Elettronica", "Sonicle.webtop.mail.MailOptions"));
			*/
			new JsonResult(data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in GetOptionsServices", ex);
			new JsonResult(false, "Error in GetOptionsServices").printTo(out);
		}
	}
	
	public void processLookupSessionServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = getEnv().getWebTopSession();
		Locale locale = wts.getLocale();
		
		ArrayList<JsSimple> items = new ArrayList<>();
		List<String> ids = wts.getPrivateServices();
		for(String id : ids) {
			items.add(new JsSimple(id, WT.lookupResource(id, locale, BaseService.RESOURCE_SERVICE_NAME)));
		}
		new JsonResult("services", items).printTo(out);
	}
	
	public void processFeedback(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			Payload<MapItem, JsFeedback> pl = ServletUtils.getPayload(request, JsFeedback.class);
			
			logger.debug("message: {}", pl.data.message);
			Thread.sleep(4000);
			new JsonResult().printTo(out);
			//new JsonResult(false, "Erroreeeeeeeeeeeeeeeeeeeeeee").printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in action Feedback", ex);
			new JsonResult(false, "Unable to send feedback report.").printTo(out);
		}
	}
	
	public void processGetWhatsnewTabs(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CorePrivateEnvironment)getEnv()).getSession();
		ArrayList<JsWhatsnewTab> tabs = null;
		JsWhatsnewTab tab = null;
		String html = null;
		UserProfile profile = getEnv().getProfile();
		
		try {
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			tabs = new ArrayList<>();
			List<String> ids = wts.getPrivateServices();
			for(String id : ids) {
				if(full || wts.needWhatsnew(id, profile)) {
					html = wts.getWhatsnewHtml(id, profile, full);
					if(!StringUtils.isEmpty(html)) {
						tab = new JsWhatsnewTab(id);
						tab.title = WT.lookupResource(id, profile.getLocale(), CoreLocaleKey.SERVICE_NAME);
						tabs.add(tab);
					}
				}
			}
			new JsonResult(tabs).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in GetWhatsnewTabs", ex);
			new JsonResult(false, "Error in GetWhatsnewTabs").printTo(out);
		}
	}
	
	public void processGetWhatsnewHTML(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CorePrivateEnvironment)getEnv()).getSession();
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			String html = wts.getWhatsnewHtml(id, getEnv().getProfile(), full);
			out.println(html);
			
		} catch (Exception ex) {
			logger.error("Error in GetWhatsnewHTML", ex);
		}
	}
	
	public void processTurnOffWhatsnew(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CorePrivateEnvironment)getEnv()).getSession();
		
		try {
			UserProfile profile = getEnv().getProfile();
			List<String> ids = wts.getPrivateServices();
			for(String id : ids) {
				wts.resetWhatsnew(id, profile);
			}
			
		} catch (Exception ex) {
			logger.error("Error in TurnOffWhatsnew", ex);
		} finally {
			new JsonResult().printTo(out);
		}
	}
	
	public void processDownloadAddon(HttpServletRequest request, HttpServletResponse response) {
		try {
			String addonId = ServletUtils.getStringParameter(request, "addonId", true);
			
			if (addonId.equals("notifier")) {
				InputStream is = null;
				try {
					if (!LangUtils.classForNameQuietly("com.sonicle.webtop.addons.AddOns")) {
						throw new WTException("Notifier not available");
					}
					is = this.getClass().getResourceAsStream("/webtop/addons/webtop.exe");
					OutputStream os = response.getOutputStream();
					ServletUtils.setFileStreamHeadersForceDownload(response, "notifier.exe");
					IOUtils.copy(is, os);
				} finally {
					IOUtils.closeQuietly(is);
				}
				
			} else {
				throw new WTException("Unknown addon [{0}]", addonId);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action DownloadFiles", ex);
			ServletUtils.writeError(response, HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public void processSnoozeReminder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			Integer snooze = ServletUtils.getIntParameter(request, "snooze", 5);
			PayloadAsList<JsReminderInApp.List> pl = ServletUtils.getPayloadAsList(request, JsReminderInApp.List.class);
			
			DateTime remindOn = DateTimeUtils.now(false).plusMinutes(snooze);
			for(JsReminderInApp js : pl.data) {
				coreMgr.snoozeReminder(JsReminderInApp.createReminderInApp(getEnv().getProfileId(), js), remindOn);
			}
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in SnoozeReminder", ex);
			new JsonResult(false, "Error in SnoozeReminder").printTo(out);
		}
	}
	
	public void processManageOTP(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CorePrivateEnvironment)getEnv()).getSession();
		UserProfile.Id pid = getEnv().getProfile().getId();
		CoreManager corem = null;
		
		try {
			String operation = ServletUtils.getStringParameter(request, "operation", true);
			if(operation.equals("configure") || operation.equals("activate") || operation.equals("deactivate")) {
				// These work only on a target user!
				String profileId = ServletUtils.getStringParameter(request, "profileId", true);
				
				UserProfile.Id targetPid = new UserProfile.Id(profileId);
				corem = (targetPid.equals(coreMgr.getTargetProfileId())) ? coreMgr : WT.getCoreManager(targetPid);
				
				if(operation.equals("configure")) {
					String deliveryMode = ServletUtils.getStringParameter(request, "delivery", true);
					if(deliveryMode.equals(CoreSettings.OTP_DELIVERY_EMAIL)) {
						String address = ServletUtils.getStringParameter(request, "address", true);
						InternetAddress ia = MailUtils.buildInternetAddress(address, null);
						if(!MailUtils.isAddressValid(ia)) throw new WTException("Email address not valid"); //TODO: messaggio in lingua
						
						OTPManager.EmailConfig config = corem.otpConfigureUsingEmail(address);
						//TODO: email send
						logger.debug("{}", config.otp.getVerificationCode());
						wts.setProperty(SERVICE_ID, WTSPROP_OTP_SETUP, config);

					} else if(deliveryMode.equals(CoreSettings.OTP_DELIVERY_GOOGLEAUTH)) {
						OTPManager.GoogleAuthConfig config = corem.otpConfigureUsingGoogleAuth(200);
						wts.setProperty(SERVICE_ID, WTSPROP_OTP_SETUP, config);
					}
					new JsonResult(true).printTo(out);
					
				} else if(operation.equals("activate")) {
					int code = ServletUtils.getIntParameter(request, "code", true);

					OTPManager.Config config = (OTPManager.Config)wts.getProperty(SERVICE_ID, WTSPROP_OTP_SETUP);
					boolean enabled = corem.otpActivate(config, code);
					/*
					if (!enabled) {
						throw new WTLocalizedException(lookupResource(CoreLocaleKey.OTP_SETUP_ERROR_CODE), "Invalid code");
					}
					*/
					if(!enabled) throw new WTException("Invalid code"); //TODO: messaggio in lingua
					wts.clearProperty(SERVICE_ID, WTSPROP_OTP_SETUP);
					
					new JsonResult().printTo(out);

				} else if(operation.equals("deactivate")) {
					corem.otpDeactivate();
					new JsonResult().printTo(out);

				}
			} else if(operation.equals("untrustthis")) {
				// This works only on current session user!
				OTPManager otpm = coreMgr.getOTPManager();
				TrustedDeviceCookie tdc = otpm.readTrustedDeviceCookie(pid, request);
				if(tdc != null) {
					otpm.removeTrustedDevice(pid, tdc.deviceId);
					otpm.clearTrustedDeviceCookie(pid, response);
				}
				new JsonResult().printTo(out);
				
			} else if(operation.equals("untrustothers")) {
				// This works only on current session user!
				OTPManager otpm = coreMgr.getOTPManager();
				TrustedDeviceCookie thistdc = otpm.readTrustedDeviceCookie(pid, request);
				List<JsTrustedDevice> tds = otpm.listTrustedDevices(pid);
				for(JsTrustedDevice td: tds) {
					if((thistdc != null) && (td.deviceId.equals(thistdc.deviceId))) continue;
					otpm.removeTrustedDevice(pid, td.deviceId);
				}
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageOTP", ex);
			new JsonResult(false, "Error in ManageOTP").printTo(out);
		}
	}
	
	public void processGetOTPGoogleAuthQRCode(HttpServletRequest request, HttpServletResponse response) {
		WebTopSession wts = ((CorePrivateEnvironment)getEnv()).getSession();
		
		try {
			OTPManager.GoogleAuthConfig config = (OTPManager.GoogleAuthConfig)wts.getProperty(SERVICE_ID, WTSPROP_OTP_SETUP);
			ServletUtils.writeContent(response, config.qrcode, config.qrcode.length, "image/png");
			
		} catch (Exception ex) {
			logger.error("Error in GetOTPGoogleAuthQRCode", ex);
		}
	}
	
	public void processManageSyncDevices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager pidCoreMgr = WT.getCoreManager(getWts().getProfileId());
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				DateTimeFormatter fmt = JsGridSync.createFormatter(pidCoreMgr.getUserData().getTimeZone());
				List<SyncDevice> devices = coreMgr.listZPushDevices();
				ArrayList<JsGridSync> items = new ArrayList<>();
				for(SyncDevice device : devices) {
					items.add(new JsGridSync(device.device, device.user, device.lastSync, fmt));
				}
				new JsonResult(items).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				//PayloadAsList<JsGridSyncList> pl = ServletUtils.getPayloadAsList(request, JsGridSyncList.class);
				Payload<MapItem, JsGridSync> pl = ServletUtils.getPayload(request, JsGridSync.class);
				CompositeId cid = new CompositeId().parse(pl.data.id);
				
				pidCoreMgr.deleteZPushDevice(cid.getToken(0), cid.getToken(1));
				new JsonResult().printTo(out);
				
			} else if(crud.equals("info")) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				CompositeId cid = new CompositeId().parse(id);
				
				String info = pidCoreMgr.getZPushDetailedInfo(cid.getToken(0), cid.getToken(1), "</br>");
				new JsonResult(info).printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageSyncDevices", ex);
			new JsonResult(false, "Error in ManageSyncDevices").printTo(out);
		}
	}
		
	public void processListInternetRecipientsSources(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			List<String> srcids=coreMgr.listInternetRecipientsSources();
			ArrayList<JsSimple> srcs=new ArrayList<>();
			for(String srcid: srcids) {
				RecipientsProviderBase provider = coreMgr.getProfileRecipientsProvider(srcid);
				srcs.add(new JsSimple(srcid,provider.getDescription()));
			}
			new JsonResult("sources", srcs, srcs.size()).printTo(out);
		} catch (Exception ex) {
			logger.error("Error in ListInternetRecipientsSource", ex);
			new JsonResult(false, "Error in ListInternetRecipientsSources").printTo(out);
		}
	}
	
	public void processManageInternetRecipients(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<InternetRecipient> items = null;
		
		try {
			ArrayList<String> sources = ServletUtils.getStringParameters(request, "sources");
			String crud = ServletUtils.getStringParameter(request, "crud", Crud.READ);
			String query = ServletUtils.getStringParameter(request, "query", "");
			if (crud.equals(Crud.READ)) {
				int limit = ServletUtils.getIntParameter(request, "limit", 100);
				if (limit==0) limit=Integer.MAX_VALUE;

				if(sources.isEmpty()) {
					items = coreMgr.listInternetRecipients(query, limit);
				} else {
					items = coreMgr.listInternetRecipients(sources, query, limit);
				}
				new JsonResult("recipients", items, items.size()).printTo(out);
			}
			else if (crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsInternetAddress> pl = ServletUtils.getPayload(request, JsInternetAddress.class);
				if (pl.data.source.isEmpty()) {
					coreMgr.deleteServiceStoreEntry(SERVICE_ID, "recipients", pl.data.address.toUpperCase());
					new JsonResult().printTo(out);
				} else {
					throw new Exception("Cannot delete from source "+pl.data.source+" ["+pl.data.sourceName+"]");
				}
			}

		} catch (Exception ex) {
			logger.error("Error in ManageInternetRecipients", ex);
			new JsonResult(false, "Error in ManageInternetRecipients").printTo(out);
		}
	}
	
	
	public void processListDomainPublicImages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String path=WT.getDomainImagesPath(getWts().getProfileId().getDomainId());
			ArrayList<JsSimple> items=new ArrayList<>();
			File dir=new File(path);
			for(File file: dir.listFiles()) {
				items.add(new JsSimple(file.getName(),file.getName()));
			}
			new JsonResult("images", items, items.size()).printTo(out);
		} catch (Exception ex) {
			logger.error("Error in processListDomainPublicImages", ex);
			new JsonResult(false, "Error in processListDomainPublicImages").printTo(out);
		}
	}
	
	public void processRestoreAutosave(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con=null;
		try {
			con=getConnection();
			boolean mine=ServletUtils.getBooleanParameter(request, "mine", true);
			String webtopClientId=RunContext.getWebTopClientID();
			List<OAutosave> items;
			if (mine) items=coreMgr.listMyAutosaveData(webtopClientId);
			else items=coreMgr.listOfflineOthersAutosaveData(webtopClientId);
			
			for(OAutosave item: items) {
				getWts().notify(new ServiceMessage(item.getServiceId(),"autosaveRestore",
						new JsAutosave(
								item.getDomainId(),
								item.getUserId(),
								item.getWebtopClientId(),
								item.getServiceId(),
								item.getContext(),
								item.getKey(),
								item.getValue()
						)
				));
			}
			new JsonResult().printTo(out);
		} catch (Exception ex) {
			logger.error("Error in processRestoreAutosave", ex);
			new JsonResult(false, "Error in processRestoreAutosave").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void processRemoveAutosave(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con=null;
		try {
			con=getConnection();
			boolean allOthers=ServletUtils.getBooleanParameter(request, "allOthers", false);
			
			if (allOthers) coreMgr.deleteOfflineOthersAutosaveData(RunContext.getWebTopClientID());
			else {
				boolean allMine=ServletUtils.getBooleanParameter(request, "allMine", false);
				if (allMine) coreMgr.deleteMyAutosaveData(RunContext.getWebTopClientID());
				else {
					String serviceId=ServletUtils.getStringParameter(request, "serviceId", true);
					String context=ServletUtils.getStringParameter(request, "context", true);
					String key=ServletUtils.getStringParameter(request, "key", true);
					String webtopClientId=ServletUtils.getStringParameter(request, "webtopClientId", true);
					
					coreMgr.deleteMyAutosaveData(webtopClientId, serviceId, context, key);
				}
			}
			new JsonResult().printTo(out);
		} catch (Exception ex) {
			logger.error("Error in processRestoreAutosave", ex);
			new JsonResult(false, "Error in processRestoreAutosave").printTo(out);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private List<String> queryDomains() {
		List<String> domains = new ArrayList<>();
		if(RunContext.isWebTopAdmin()) domains.add("*");
		domains.add(getWts().getProfileId().getDomainId());
		return domains;
	}
	
	
	
	
	
	public void processServerEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<ServiceMessage> messages = new ArrayList();
		
		try {
			messages = ((CorePrivateEnvironment)getEnv()).getSession().getEnqueuedMessages();
			
		} catch (Exception ex) {
			logger.error("Error executing action ServerEvents", ex);
		} finally {
			new JsonResult(JsonResult.gson.toJson(messages)).printTo(out);
		}
	}
}
