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
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.security.DomainAccount;
import com.sonicle.webtop.core.bol.ActivityGrid;
import com.sonicle.webtop.core.bol.CausalGrid;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomer;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsActivity;
import com.sonicle.webtop.core.bol.js.JsCausal;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsFeedback;
import com.sonicle.webtop.core.bol.js.JsGridSync;
import com.sonicle.webtop.core.bol.js.JsGridSync.JsGridSyncList;
import com.sonicle.webtop.core.bol.js.JsReminderAlert;
import com.sonicle.webtop.core.bol.js.JsRole;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsWhatsnewTab;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.dal.CustomerDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
	private CoreManager core;
	private CoreUserSettings us;
	
	private WebTopApp getApp() {
		return ((CoreEnvironment)getEnv()).getApp();
	}
	
	@Override
	public void initialize() throws Exception {
		UserProfile profile = getEnv().getProfile();
		core = new CoreManager(getRunContext(), getApp());
		us = new CoreUserSettings(profile.getId());
	}

	@Override
	public void cleanup() throws Exception {
		
	}

	@Override
	public HashMap<String, Object> returnClientOptions() {
		UserProfile profile = getEnv().getProfile();
		HashMap<String, Object> hm = new HashMap<>();
		
		hm.put("profileId", profile.getStringId());
		hm.put("domainId", profile.getDomainId());
		hm.put("userId", profile.getUserId());
		
		hm.put("theme", us.getTheme());
		hm.put("layout", us.getLayout());
		hm.put("laf", us.getLookAndFeel());
		hm.put("language", profile.getLanguageTag());
		hm.put("timezone", profile.getTimeZone().getID());
		hm.put("startDay", us.getStartDay());
		hm.put("shortDateFormat", us.getShortDateFormat());
		hm.put("longDateFormat", us.getLongDateFormat());
		hm.put("shortTimeFormat", us.getShortTimeFormat());
		hm.put("longTimeFormat", us.getLongTimeFormat());
		
		hm.put("tfaEnabled", core.getTFAManager().isEnabled(profile.getDomainId()));
		hm.put("upiProviderWritable", core.isUserInfoProviderWritable());
		
		return hm;
	}
	
	 
	
	public void processLookupLanguages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		LinkedHashMap<String, JsSimple> items = new LinkedHashMap<>();
		Locale locale = ((CoreEnvironment)getEnv()).getSession().getLocale();
		Locale loc = null;
		String lang = null;
		
		try {
			for(AppLocale apploc : WT.getInstalledLocales()) {
				loc = apploc.getLocale();
				lang = loc.getLanguage();
				if(!items.containsKey(lang)) {
					//items.put(lang, new JsSimple(lang, loc.getDisplayLanguage(locale)));
					items.put(lang, new JsSimple(apploc.getId(), apploc.getLocale().getDisplayName(locale)));
				}
			}
			new JsonResult("languages", items.values(), items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupLanguages", ex);
			new JsonResult(false, "Unable to lookup languages").printTo(out);
		}
	}
	
	public void processLookupTimezones(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {	
			String normId = null;
			int off;
			for(TimeZone tz : WT.getTimezones()) {
				normId = StringUtils.replace(tz.getID(), "_", " ");
				off = tz.getRawOffset()/3600000;
				items.add(new JsSimple(tz.getID(), MessageFormat.format("{0} (GMT{1}{2})", normId, (off<0) ? "-" : "+", Math.abs(off))));
			}
			new JsonResult("timezones", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupTimezones", ex);
			new JsonResult(false, "Unable to lookup timezones").printTo(out);
		}
	}
	
	public void processLookupThemes(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			//TODO: handle themes dinamically
			items.add(new JsSimple("aria", "Aria"));
			items.add(new JsSimple("classic", "Classic"));
			items.add(new JsSimple("crisp", "Crisp"));
			items.add(new JsSimple("crisp-touch", "Crisp Touch"));
			items.add(new JsSimple("gray", "Gray"));
			items.add(new JsSimple("neptune", "Neptune"));
			items.add(new JsSimple("neptune-touch", "Neptune Touch"));
			
			new JsonResult("themes", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action LookupThemes", ex);
			new JsonResult(false, "Unable to lookup themes").printTo(out);
		}
	}
	
	public void processLookupLayouts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			items.add(new JsSimple("default", "WebTop"));
			items.add(new JsSimple("stacked", "Outlook 2007/2003"));
			items.add(new JsSimple("queued", "Mozilla"));
			new JsonResult("layouts", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action LookupLayouts", ex);
			new JsonResult(false, "Unable to lookup layouts").printTo(out);
		}
	}
	
	public void processLookupLAFs(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			//TODO: handle lafs dinamically
			items.add(new JsSimple("default", "WebTop"));
			new JsonResult("lafs", items, items.size()).printTo(out);

		} catch (Exception ex) {
			logger.error("Error executing action LookupLAFs", ex);
			new JsonResult(false, "Unable to lookup look&feels").printTo(out);
		}
	}
	
	public void processLookupDomains(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			
			if(WT.isSysAdmin(up.getId())) {
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
			
			new JsonResult("domains", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupDomains", ex);
			new JsonResult(false, "Unable to lookup domains").printTo(out);
		}
	}
	
	public void processLookupDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsRole> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean users = ServletUtils.getBooleanParameter(request, "users", true);
			boolean groups = ServletUtils.getBooleanParameter(request, "groups", true);
			String domainId = ServletUtils.getStringParameter(request, "domainId", null);
			
			if(!WT.isSysAdmin(up.getId())) {
				domainId = up.getDomainId();
			}
			
			if(wildcard) items.add(JsRole.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			List<Role> roles = core.listRoles(domainId, users, groups);
			for(Role role : roles) {
				items.add(new JsRole(role));
			}
			
			new JsonResult("roles", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupDomainRoles", ex);
			new JsonResult(false, "Unable to lookup roles").printTo(out);
		}
	}
	
	public void processLookupDomainUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			String domainId = ServletUtils.getStringParameter(request, "domainId", null);
			
			List<OUser> users = null;
			if(WT.isSysAdmin(up.getId())) {
				if(!StringUtils.isEmpty(domainId)) {
					users = core.listUsers(domainId, true);
				} else {
					users = core.listUsers(true);
				}
			} else {
				// Domain users can only see users belonging to their own domain
				users = core.listUsers(up.getDomainId(), true);
			}
			
			if(wildcard) items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			for(OUser user : users) {
				items.add(new JsSimple(user.getUserId(), JsSimple.description(user.getDisplayName(), user.getUserId())));
			}
			
			new JsonResult("users", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupUsers", ex);
			new JsonResult(false, "Unable to lookup users").printTo(out);
		}
	}
	
	public void processLookupActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsActivity> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			UserProfile.Id pid = new UserProfile.Id(profileId);
			
			//TODO: tradurre campo descrizione in base al locale dell'utente
			List<OActivity> activities = core.listLiveActivities(pid);
			for(OActivity activity : activities) {
				items.add(new JsActivity(activity));
			}
			
			new JsonResult("activities", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupActivities", ex);
			new JsonResult(false, "Unable to lookup activities").printTo(out);
		}
	}
	
	public void processManageActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				if(id == null) {
					List<ActivityGrid> items = core.listLiveActivities(queryDomains(up));
					new JsonResult("activities", items, items.size()).printTo(out);
				} else {
					OActivity item = core.getActivity(id);
					new JsonResult(item).printTo(out);
				}
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.insertActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.updateActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.deleteActivity(pl.data.getActivityId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error executing action ManageActivities", ex);
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
			List<OCausal> causals = core.listLiveCausals( pid, customerId);
			for(OCausal causal : causals) {
				items.add(new JsCausal(causal));
			}
			new JsonResult("causals", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupCausals", ex);
			new JsonResult(false, "Unable to lookup causals").printTo(out);
		}
	}
	
	public void processManageCausals(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				if(id == null) {
					List<CausalGrid> items =  core.listLiveCausals(queryDomains(up));
					new JsonResult("causals", items, items.size()).printTo(out);
				} else {
					OCausal item = core.getCausal(id);
					new JsonResult(item).printTo(out);
				}
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				core.insertCausal( pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				core.updateCausal(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OCausal> pl = ServletUtils.getPayload(request, OCausal.class);
				core.deleteCausal(pl.data.getCausalId());
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
			
			List<OCustomer> customers = core.listCustomersByLike("%" + query + "%");
			for(OCustomer customer : customers) {
				items.add(new JsSimple(customer.getCustomerId(), customer.getDescription()));
			}
			new JsonResult("customers", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action LookupCustomers", ex);
			new JsonResult(false, "Unable to lookup customers").printTo(out);
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
			logger.error("Error executing action LookupStatisticCustomers", ex);
			new JsonResult(false, "Unable to lookup statistic customers").printTo(out);
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
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			
			List<UserOptionsServiceData> data = core.getUserOptionServicesForUser(new UserProfile.Id(id));
			/*
			//TODO: aggiornare l'implementazione
			data.add(new UserOptionsServiceData("com.sonicle.webtop.core", "wt", "WebTop Services", "Sonicle.webtop.core.view.CoreOptions"));
			if(!UserProfile.isWebTopAdmin(id)) data.add(new UserOptionsServiceData("com.sonicle.webtop.calendar", "wtcal", "Calendario", "Sonicle.webtop.calendar.CalendarOptions"));
			if(!UserProfile.isWebTopAdmin(id)) data.add(new UserOptionsServiceData("com.sonicle.webtop.mail", "wtmail", "Posta Elettronica", "Sonicle.webtop.mail.MailOptions"));
			*/
			new JsonResult(data).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetOptionsServices", ex);
			new JsonResult(false, "Unable to get option services").printTo(out);
		}
	}
	
	public void processLookupSessionServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		Locale locale = wts.getLocale();
		
		ArrayList<JsSimple> items = new ArrayList<>();
		List<String> ids = wts.getServices();
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
			logger.error("Error executing action Feedback", ex);
			new JsonResult(false, "Unable to send feedback report.").printTo(out);
		}
	}
	
	public void processGetWhatsnewTabs(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		ArrayList<JsWhatsnewTab> tabs = null;
		JsWhatsnewTab tab = null;
		String html = null;
		UserProfile profile = getEnv().getProfile();
		
		try {
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			tabs = new ArrayList<>();
			List<String> ids = wts.getServices();
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
			logger.error("Error executing action GetWhatsnewTabs", ex);
			new JsonResult(false, "Unable to get What's New info.").printTo(out);
		}
	}
	
	public void processGetWhatsnewHTML(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			
			String html = wts.getWhatsnewHtml(id, getEnv().getProfile(), full);
			out.println(html);
			
		} catch (Exception ex) {
			logger.error("Error executing action GetWhatsnewHTML", ex);
		}
	}
	
	public void processTurnOffWhatsnew(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		
		try {
			UserProfile profile = getEnv().getProfile();
			List<String> ids = wts.getServices();
			for(String id : ids) {
				wts.resetWhatsnew(id, profile);
			}
			
		} catch (Exception ex) {
			logger.error("Error executing action TurnOffWhatsnew", ex);
		} finally {
			new JsonResult().printTo(out);
		}
	}
	
	public void processPostponeReminder(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String now = ServletUtils.getStringParameter(request, "now", true);
			Integer postpone = ServletUtils.getIntParameter(request, "postpone", 5);
			PayloadAsList<JsReminderAlert.List> pl = ServletUtils.getPayloadAsList(request, JsReminderAlert.List.class);
			
			DateTimeFormatter fmt = DateTimeUtils.createYmdHmsFormatter(getEnv().getProfile().getTimeZone());
			DateTime remindOn = fmt.parseDateTime(now).plusMinutes(postpone);
			
			for(JsReminderAlert reminder : pl.data) {
				core.postponeReminder(getEnv().getProfileId(), reminder, remindOn);
			}
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error executing PosponeReminder", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageTFA(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		UserProfile profile = getEnv().getProfile();
		TFAManager tfam = core.getTFAManager();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals("generate")) {
				String deliveryMode = ServletUtils.getStringParameter(request, "delivery", true);
				if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_EMAIL)) {
					String email = ServletUtils.getStringParameter(request, "emailAddress", true);
					tfam.initTFAUsingEmail(wts, email);
					
				} else if(deliveryMode.equals(CoreUserSettings.TFA_DELIVERY_GOOGLEAUTH)) {
					tfam.initTFAUsingGoogleAuth(wts);
				}
				new JsonResult(true).printTo(out);
				
			} else if(crud.equals("activate")) {
				String deliveryMode = ServletUtils.getStringParameter(request, "delivery", true);
				Integer code = ServletUtils.getIntParameter(request, "code", true);
				boolean enabled = tfam.activateTFA(wts, deliveryMode, code);
				new JsonResult(enabled).printTo(out);
				
			} else if(crud.equals("untrustthis")) {
				TrustedDeviceCookie tdc = tfam.readTrustedDeviceCookie(profile, request);
				if(tdc == null) throw new Exception("This device is already untrusted");
				tfam.removeTrustedDevice(profile, tdc.deviceId);
				tfam.clearTrustedDeviceCookie(profile, response);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("untrustothers")) {
				TrustedDeviceCookie thistdc = tfam.readTrustedDeviceCookie(profile, request);
				ArrayList<JsTrustedDevice> tds = tfam.getTrustedDevices(profile);
				for(JsTrustedDevice td: tds) {
					if((thistdc != null) && (td.deviceId.equals(thistdc.deviceId))) continue;
					tfam.removeTrustedDevice(profile, td.deviceId);
				}
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error executing action ManageTFA", ex);
			new JsonResult(false, "Error managing TFA").printTo(out);
		}
	}
	
	public void processManageSyncDevices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		WebTopSession wts = ((CoreEnvironment)getEnv()).getSession();
		UserProfile profile = getEnv().getProfile();
		
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				ArrayList<JsGridSync> items = new ArrayList<>();
				List<SyncDevice> devices = core.listZPushDevices();
				for(SyncDevice device : devices) {
					items.add(new JsGridSync(device.device, device.user, device.info));
				}
				new JsonResult(items).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				//PayloadAsList<JsGridSyncList> pl = ServletUtils.getPayloadAsList(request, JsGridSyncList.class);
				Payload<MapItem, JsGridSync> pl = ServletUtils.getPayload(request, JsGridSync.class);
				CompositeId cid = new CompositeId(pl.data.id);
				
				core.deleteZPushDevice(cid.getToken(0), cid.getToken(1));
				new JsonResult().printTo(out);
				
			} else if(crud.equals("info")) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				CompositeId cid = new CompositeId(id);
				
				String info = core.getZPushDetailedInfo(cid.getToken(0), cid.getToken(1), "</br>");
				new JsonResult(info).printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageSyncDevices", ex);
			new JsonResult(false, "Error in ManageSyncDevices").printTo(out);
		}
	}
	
	
	
	private List<String> queryDomains(UserProfile profile) {
		List<String> domains = new ArrayList<>();
		if(WT.isWebTopAdmin(profile.getId())) domains.add("*");
		domains.add(profile.getDomainId());
		return domains;
	}
	
	
	
	
	
	public void processServerEvents(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<ServiceMessage> messages = new ArrayList();
		
		try {
			messages = ((CoreEnvironment)getEnv()).getSession().getEnqueuedMessages();
			
		} catch (Exception ex) {
			logger.error("Error executing action ServerEvents", ex);
		} finally {
			new JsonResult(JsonResult.gson.toJson(messages)).printTo(out);
		}
	}
}
