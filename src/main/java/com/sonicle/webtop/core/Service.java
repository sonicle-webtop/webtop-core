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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.MailUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.security.Principal;
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
import com.sonicle.webtop.core.app.ws.IMChatRoomAdded;
import com.sonicle.webtop.core.app.ws.IMChatRoomMessageReceived;
import com.sonicle.webtop.core.app.ws.IMChatRoomUpdated;
import com.sonicle.webtop.core.app.ws.IMUpdateFriendPresence;
import com.sonicle.webtop.core.bol.OAutosave;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsActivityLkp;
import com.sonicle.webtop.core.bol.js.JsAutosave;
import com.sonicle.webtop.core.bol.js.JsCausalLkp;
import com.sonicle.webtop.core.bol.js.JsCustomerSupplierLkp;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsFeedback;
import com.sonicle.webtop.core.bol.js.JsGridIMChat;
import com.sonicle.webtop.core.bol.js.JsGridIMFriend;
import com.sonicle.webtop.core.bol.js.JsGridSync;
import com.sonicle.webtop.core.bol.js.JsGridIMMessage;
import com.sonicle.webtop.core.bol.js.JsGroupChat;
import com.sonicle.webtop.core.bol.js.JsInternetAddress;
import com.sonicle.webtop.core.bol.js.JsPublicImage;
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
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.model.Activity;
import com.sonicle.webtop.core.model.Causal;
import com.sonicle.webtop.core.model.CausalExt;
import com.sonicle.webtop.core.model.IMChat;
import com.sonicle.webtop.core.model.IMMessage;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.xmpp.Friend;
import com.sonicle.webtop.core.xmpp.FriendPresence;
import com.sonicle.webtop.core.xmpp.ChatMessage;
import com.sonicle.webtop.core.xmpp.ChatRoom;
import com.sonicle.webtop.core.xmpp.ConversationHistory;
import com.sonicle.webtop.core.xmpp.InstantChatRoom;
import com.sonicle.webtop.core.xmpp.GroupChatRoom;
import com.sonicle.webtop.core.xmpp.PresenceStatus;
import com.sonicle.webtop.core.xmpp.XMPPClient;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.slf4j.Logger;
import com.sonicle.webtop.core.xmpp.XMPPClientListener;
import com.sonicle.webtop.core.xmpp.XMPPHelper;
import java.util.HashMap;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.jxmpp.jid.EntityFullJid;

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
	private XMPPClient xmppCli;
	
	/*
	private WebTopApp getApp() {
		return ((CorePrivateEnvironment)getEnv()).getApp();
	}
	*/
	
	@Override
	public void initialize() throws Exception {
		final UserProfile profile = getEnv().getProfile();
		UserProfileId pid = profile.getId();
		coreMgr = getCoreManager();
		ss = new CoreServiceSettings(SERVICE_ID, pid.getDomainId());
		us = new CoreUserSettings(pid);
		
		Principal principal = profile.getPrincipal();
		if (!principal.isImpersonated()) {
			ConversationHistory history = new ConversationHistory();
			for(IMChat chat : coreMgr.listIMChats()) {
				if (!chat.getIsGroupChat()) {
					history.addChat(createDirectChatRoom(chat));
				} else {
					final List<String> stanzaIDs = coreMgr.listIMMessageStanzaIDs(chat.getChatJid());
					history.addChat(createGroupChatRoom(chat), stanzaIDs);
				}
			}
			
			final String xmppResource = getWts().getId() + "@" + WT.getPlatformName();
			final String internetName = WT.getDomainInternetName(pid.getDomainId());
			XMPPTCPConnectionConfiguration.Builder builder = XMPPHelper.setupConfigBuilder(ss.getXMPPHost(), ss.getXMPPPort(), internetName, principal.getUserId(), new String(principal.getPassword()), xmppResource);
			final String nickname = profile.getDisplayName();
			xmppCli = new XMPPClient(builder, ss.getXMPPMucSubdomain(), nickname, new XMPPServiceListenerImpl(), history);
		}
	}
	
	private CoreManager getCoreManager() {
		return (CoreManager)WT.getServiceManager(SERVICE_ID);
	}
	
	private CoreAdminManager getCoreAdminManager() {
		return (CoreAdminManager)WT.getServiceManager(CoreAdminManifest.ID);
	}

	@Override
	public void cleanup() throws Exception {
		if (xmppCli != null) {
			xmppCli.disconnect();
		}
	}

	@Override
	public ServiceVars returnServiceVars() {
		UserProfile profile = getEnv().getProfile();
		ServiceVars co = new ServiceVars();
		
		boolean domainPasswordPolicy = false;
		boolean dirCapPasswordWrite = false;
		try {
			if (profile.getDomainId().equals(WebTopManager.SYSADMIN_DOMAINID)) {
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
		co.put("imEnabled", !RunContext.isWebTopAdmin() && RunContext.isPermitted(CoreManifest.ID, "WEBCHAT", "ACCESS"));
		co.put("imPresenceStatus", EnumUtils.toSerializedName(us.getIMPresenceStatus()));
		co.put("imStatusMessage", us.getIMStatusMessage());
		co.put("imSoundOnFriendConnect", us.getIMSoundOnFriendConnect());
		co.put("imSoundOnFriendDisconnect", us.getIMSoundOnFriendDisconnect());
		co.put("imSoundOnMessageReceived", us.getIMSoundOnMessageReceived());
		co.put("imSoundOnMessageSent", us.getIMSoundOnMessageSent());
		
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
				final String id = fullId ? new UserProfileId(user.getDomainId(), user.getUserId()).toString() : user.getUserId();
				items.add(new JsSimple(id, JsSimple.description(user.getDisplayName(), user.getUserId())));
			}
			
			new JsonResult("users", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupUsers", ex);
			new JsonResult(false, "Unable to lookup users").printTo(out);
		}
	}
	
	public void processLookupActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsActivityLkp> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			UserProfileId pid = new UserProfileId(profileId);
			
			//TODO: tradurre campo descrizione in base al locale dell'utente
			List<Activity> acts = null;
			if (coreMgr.hasSameTargetProfile(pid)) {
				acts = coreMgr.listLiveActivities();
			} else {
				CoreManager mgr = WT.getCoreManager(true, pid);
				acts = mgr.listLiveActivities();
			}
			for(Activity act : acts) {
				items.add(new JsActivityLkp(act));
			}
			new JsonResult(items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupActivities", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupCausals(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCausalLkp> items = new ArrayList<>();
		
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			String masterDataId = ServletUtils.getStringParameter(request, "masterDataId", null);
			UserProfileId pid = new UserProfileId(profileId);
			
			//TODO: tradurre campo descrizione in base al locale dell'utente
			List<Causal> caus = null;
			if (coreMgr.hasSameTargetProfile(pid)) {
				caus = coreMgr.listLiveCausals(masterDataId);
			} else {
				CoreManager mgr = WT.getCoreManager(true, pid);
				caus = mgr.listLiveCausals(masterDataId);
			}
			for(Causal cau : caus) {
				items.add(new JsCausalLkp(cau));
			}
			new JsonResult(items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupCausals", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupCustomersSuppliers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCustomerSupplierLkp> items = new ArrayList<>();
		
		try {
			String query = ServletUtils.getStringParameter(request, "query", "");
			
			String[] types = new String[]{
				EnumUtils.toSerializedName(MasterData.Type.CUSTOMER),
				EnumUtils.toSerializedName(MasterData.Type.SUPPLIER)
			};
			List<MasterData> entries = coreMgr.listMasterDataByLike(types, "%" + query + "%");
			for(MasterData entry : entries) {
				items.add(new JsCustomerSupplierLkp(entry));
			}
			
			new JsonResult(items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupCustomersSuppliers", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupStatisticCustomersSuppliers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCustomerSupplierLkp> items = new ArrayList<>();
		
		try {
			String parentMasterDataId = ServletUtils.getStringParameter(request, "parentMasterDataId", null);
			String query = ServletUtils.getStringParameter(request, "query", "");
			
			String[] types = new String[]{
				EnumUtils.toSerializedName(MasterData.Type.CUSTOMER),
				EnumUtils.toSerializedName(MasterData.Type.SUPPLIER)
			};
			List<MasterData> entries = coreMgr.listMasterDataByParentLike(parentMasterDataId, types, "%" + query + "%");
			for(MasterData entry : entries) {
				final ArrayList<String> tokens = new ArrayList<>(3);
				if (!StringUtils.isEmpty(entry.getAddress())) {
					tokens.add(entry.getAddress());
				}
				if (!StringUtils.isEmpty(entry.getCity())) {
					tokens.add(entry.getCity());
				}
				if (!StringUtils.isEmpty(entry.getCountry())) {
					tokens.add(entry.getCountry());
				}
				final String address = StringUtils.join(tokens, ", ");
				String description = null;
				if (StringUtils.isEmpty(address)) {
					description = entry.getDescription();
				} else {
					description = MessageFormat.format("{0} ({1})", entry.getDescription(), address);
				}
				items.add(new JsCustomerSupplierLkp(entry, description));
			}
			
			new JsonResult(items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupStatisticCustomersSuppliers", ex);
			new JsonResult(ex).printTo(out);
		}
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
	
	public void processManageActivities(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<Activity> items =  coreMgr.listAllLiveActivities();
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, Activity> pl = ServletUtils.getPayload(request, Activity.class);
				coreMgr.deleteActivity(pl.data.getActivityId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageCausals", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageActivity(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				Activity item = coreMgr.getActivity(id);
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, Activity> pl = ServletUtils.getPayload(request, Activity.class);
				coreMgr.addActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, Activity> pl = ServletUtils.getPayload(request, Activity.class);
				coreMgr.updateActivity(pl.data);
				new JsonResult().printTo(out);	
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageCausal", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageCausals(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<CausalExt> items =  coreMgr.listAllLiveCausals();
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, CausalExt> pl = ServletUtils.getPayload(request, CausalExt.class);
				coreMgr.deleteCausal(pl.data.getCausalId());
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageCausals", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageCausal(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				Causal item = coreMgr.getCausal(id);
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, Causal> pl = ServletUtils.getPayload(request, Causal.class);
				coreMgr.addCausal(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, Causal> pl = ServletUtils.getPayload(request, Causal.class);
				coreMgr.updateCausal(pl.data);
				new JsonResult().printTo(out);	
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageCausal", ex);
			new JsonResult(ex).printTo(out);
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
			
			UserProfileId targetPid = new UserProfileId(id);
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
		UserProfileId pid = getEnv().getProfile().getId();
		CoreManager corem = null;
		
		try {
			String operation = ServletUtils.getStringParameter(request, "operation", true);
			if(operation.equals("configure") || operation.equals("activate") || operation.equals("deactivate")) {
				// These work only on a target user!
				String profileId = ServletUtils.getStringParameter(request, "profileId", true);
				
				UserProfileId targetPid = new UserProfileId(profileId);
				corem = (targetPid.equals(coreMgr.getTargetProfileId())) ? coreMgr : WT.getCoreManager(targetPid);
				
				if(operation.equals("configure")) {
					String deliveryMode = ServletUtils.getStringParameter(request, "delivery", true);
					if(deliveryMode.equals(CoreSettings.OTP_DELIVERY_EMAIL)) {
						String address = ServletUtils.getStringParameter(request, "address", true);
						InternetAddress ia = MailUtils.buildInternetAddress(address, null);
						if(!MailUtils.isAddressValid(ia)) throw new WTException("Email address not valid"); //TODO: messaggio in lingua
						
						OTPManager.EmailConfig config = corem.otpConfigureUsingEmail(address);
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
	
	/*
	private void sendOtpCodeEmail(UserProfileId pid, Locale locale, InternetAddress to, String verificationCode) {
		try {
			String bodyHeader = WT.lookupResource(SERVICE_ID, locale, CoreLocaleKey.TPL_EMAIL_OTPCODEVERIFICATION_BODY_HEADER);
			String subject = NotificationHelper.buildSubject(locale, SERVICE_ID, bodyHeader);
			String html = TplHelper.buildOtpCodeVerificationEmail(locale, verificationCode);
			
			InternetAddress from = WT.getNotificationAddress(pid.getDomainId());
			if(from == null) throw new WTException("Error building sender address");
			WT.sendEmail(WT.getGlobalMailSession(pid), true, from, to, subject, html);

		} catch(IOException | TemplateException ex) {
			logger.error("Unable to build email template", ex);
		} catch(Exception ex) {
			logger.error("Unable to send email", ex);
		}
	}
	*/
	
	/*
	private void sendOtpCodeEmail2(OSharingLink olink, String path, String ipAddress, String userAgent) throws WTException {
		final String BHD_KEY = (olink.getLinkType().equals(SharingLink.TYPE_DOWNLOAD)) ? VfsLocale.TPL_EMAIL_SHARINGLINKUSAGE_BODY_HEADER_DL : VfsLocale.TPL_EMAIL_SHARINGLINKUSAGE_BODY_HEADER_UL;
		UserProfileId pid = olink.getProfileId();
		
		//TODO: rendere relativa la path del file rispetto allo Store???
		try {
			UserProfile.Data ud = WT.getUserData(olink.getProfileId());
			String bodyHeader = lookupResource(ud.getLocale(), BHD_KEY);
			String source = NotificationHelper.buildSource(ud.getLocale(), SERVICE_ID);
			String subject = TplHelper.buildLinkUsageEmailSubject(ud.getLocale(), bodyHeader);
			String customBody = TplHelper.buildLinkUsageBodyTpl(ud.getLocale(), olink.getSharingLinkId(), PathUtils.getFileName(olink.getFilePath()), path, ipAddress, userAgent);
			String html = NotificationHelper.buildCustomBodyTplForNoReplay(ud.getLocale(), source, bodyHeader, customBody);

			//InternetAddress from = WT.buildDomainInternetAddress(pid.getDomainId(), "webtop-notification", null);
			//if(from == null) throw new WTException("Error building sender address");
			InternetAddress from = WT.getNotificationAddress(pid.getDomainId());
			InternetAddress to = ud.getEmail();
			if(to == null) throw new WTException("Error building destination address");
			WT.sendEmail(getMailSession(), true, from, to, subject, html);

		} catch(IOException | TemplateException ex) {
			logger.error("Unable to build email template", ex);
		} catch(Exception ex) {
			logger.error("Unable to send email", ex);
		}
	}
	*/
	
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
			String domainId=getWts().getProfileId().getDomainId();
			String path=WT.getDomainImagesPath(domainId);
			ArrayList<JsPublicImage> items=new ArrayList<>();
			File dir=new File(path);
			int id=0;
			for(File file: dir.listFiles()) {
				String name=file.getName();
				String url=PathUtils.concatPathParts(WT.getPublicImagesUrl(domainId),name);
				items.add(new JsPublicImage("img"+(++id),name,url));
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
	
	/*
	private List<String> queryDomains() {
		List<String> domains = new ArrayList<>();
		if(RunContext.isWebTopAdmin()) domains.add("*");
		domains.add(getWts().getProfileId().getDomainId());
		return domains;
	}
	*/
	
	
	
	public void processManageIM(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals("init")) {
				if (xmppCli != null) {
					xmppCli.updatePresence(us.getIMPresenceStatus(), "Hey there! I'm on WebTop");

					new JsonResult().printTo(out);
				}
				
			} else if (crud.equals("presence")) {
				String presenceStatus = ServletUtils.getStringParameter(request, "presenceStatus", null);
				String statusMessage = ServletUtils.getStringParameter(request, "statusMessage", null);
				
				PresenceStatus ps = EnumUtils.forSerializedName(presenceStatus, PresenceStatus.class);
				if (ps == null) ps = PresenceStatus.ONLINE;
				if (statusMessage == null) statusMessage = "Hey there! I'm on WebTop";

				if (xmppCli != null) {
					xmppCli.updatePresence(ps, statusMessage);
					us.setIMPresenceStatus(ps);
					us.setIMStatusMessage(statusMessage);

					new JsonResult().printTo(out);
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageIM", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupIMFriends(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			if (xmppCli != null) {
				for(Friend friend : xmppCli.listFriends()) {
					items.add(new JsSimple(friend.getId(), friend.getNickame()));
				}
			}
			new JsonResult(items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupIMFriends", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGridIMFriends(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				List<JsGridIMFriend> items = new ArrayList<>();
				if (xmppCli != null) {
					for(Friend friend : xmppCli.listFriends()) {
						items.add(new JsGridIMFriend(friend, friend.getInstantChatJid()));
					}
				}	
				new JsonResult(items, items.size()).printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageGridIMFriends", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGridIMChats(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				List<JsGridIMChat> items = new ArrayList<>();
				
				if (xmppCli != null) {
					for(ChatRoom chat : xmppCli.listChats()) {
						items.add(new JsGridIMChat(chat));
					}
				} else {
					for(IMChat chat : coreMgr.listIMChats()) {
						items.add(new JsGridIMChat(chat));
					}
				}
					
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsGridIMChat> pl = ServletUtils.getPayload(request, JsGridIMChat.class);
				
				if (xmppCli != null) {
					xmppCli.forgetChat(pl.data.id);
				}
				coreMgr.deleteIMChat(pl.data.id);
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageGridIMChats", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageIMChat(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals("prepare")) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				ServletUtils.StringArray withUsers = ServletUtils.getObjectParameter(request, "withUsers", ServletUtils.StringArray.class, true);
				
				if (xmppCli != null) {
					// Ensure chat object is ready in XMPPClient
					if (XMPPClient.isGroupChat(chatId)) {
						String chatName = ServletUtils.getStringParameter(request, "chatName", true);


					} else {
						EntityBareJid chatWithJid = XMPPHelper.asEntityBareJid(withUsers.get(0));
						xmppCli.newInstantChat(chatWithJid);
					}
					
					new JsonResult().printTo(out);
				} else {
					throw new WTException("XMPPClient not available");
				}
				
			} else if (crud.equals("presence")) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				
				if (xmppCli != null) {
					if (!XMPPClient.isInstantChat(chatId)) throw new WTException("Presence feature non available for a grupchat");
					
					String presenceStatus = null;
					FriendPresence presence = xmppCli.getChatPresence(chatId);
					if (presence == null) {
						presenceStatus = EnumUtils.toSerializedName(PresenceStatus.OFFLINE);
					} else {
						presenceStatus = EnumUtils.toSerializedName(presence.getPresenceStatus());
					}
					
					new JsonResult(presenceStatus).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
				}
				
			} else if (crud.equals("send")) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				String text = ServletUtils.getStringParameter(request, "text", true);
				
				if (xmppCli != null) {
					EntityBareJid chatJid = XMPPHelper.asEntityBareJid(chatId);
					ChatMessage message = xmppCli.sendMessage(chatJid, text);
					if (message == null) throw new Exception("Message is null");

					new JsonResult(new JsGridIMMessage(true, message, getEnv().getProfile().getTimeZone())).printTo(out);
				} else {
					throw new WTException("XMPPClient not available");
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageIMChat", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGridIMMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		DateTimeZone utz = up.getTimeZone();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				String date = ServletUtils.getStringParameter(request, "date", null);
				
				boolean history;
				LocalDate ld = null;
				if (date == null) {
					history = false;
					ld = DateTime.now().withZone(utz).toLocalDate();
				} else {
					history = true;
					ld = DateTimeUtils.parseYmdHmsWithZone(date, "00:00:00", utz).toLocalDate();
				}
				
				if (xmppCli != null) {
					EntityBareJid myJid = xmppCli.getUserJid().asEntityBareJid();
					
					LocalDate lastDate = null;
					HashMap<String, String> cacheNicks = new HashMap<>();
					List<JsGridIMMessage> items = new ArrayList<>();
					for(IMMessage mes : coreMgr.listIMMessages(chatId, ld)) {
						if (!mes.getDate().equals(lastDate)) {
							lastDate = mes.getDate();
							items.add(JsGridIMMessage.asDateSeparator(mes.getMessageUid() + "!", ld));
						}
						
						if (!cacheNicks.containsKey(mes.getSenderJid())) {
							if (xmppCli.isAuthenticated()) {
								cacheNicks.put(mes.getSenderJid(), xmppCli.getFriendNickname(XMPPHelper.asEntityBareJid(mes.getSenderJid())));
							} else {
								cacheNicks.put(mes.getSenderJid(), XMPPHelper.buildGuessedString(mes.getSenderJid()));
							}
						}
						
						final String nick = cacheNicks.get(mes.getSenderJid());
						items.add(new JsGridIMMessage(myJid.equals(mes.getSenderJid()), mes, nick, utz));
					}
					new JsonResult(items, items.size()).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageGridIMMessages", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGroupChat(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				
				ChatRoom chat = xmppCli.getChat(id);
				List<String> partecipants = xmppCli.getChatPartecipants(id);
				new JsonResult(new JsGroupChat((GroupChatRoom)chat, partecipants)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsGroupChat> pl = ServletUtils.getPayload(request, JsGroupChat.class);
				
				if (xmppCli != null) {
					ArrayList<EntityBareJid> withUsers = new ArrayList<>();
					for(JsGroupChat.Partecipant partecipant : pl.data.partecipants) {
						withUsers.add(XMPPHelper.asEntityBareJid(partecipant.friendId));
					}
					EntityBareJid chatId = xmppCli.newGroupChat(pl.data.name, withUsers);
					pl.data.id = chatId.asEntityBareJidString();
					
					new JsonResult(pl.data).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
				}
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsGroupChat> pl = ServletUtils.getPayload(request, JsGroupChat.class);
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageGroupChat", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	private GroupChatRoom createGroupChatRoom(IMChat chat) {
		final EntityBareJid chatJid = XMPPHelper.asEntityBareJid(chat.getChatJid());
		final EntityBareJid ownerJid = XMPPHelper.asEntityBareJid(chat.getOwnerJid());
		return new GroupChatRoom(chatJid, ownerJid, chat.getName(), chat.getLastSeenActivity());
	}
	
	private InstantChatRoom createDirectChatRoom(IMChat chat) {
		final EntityBareJid chatJid = XMPPHelper.asEntityBareJid(chat.getChatJid());
		final EntityBareJid ownerJid = XMPPHelper.asEntityBareJid(chat.getOwnerJid());
		final EntityBareJid withJid = XMPPHelper.asEntityBareJid(chat.getWithJid());
		return new InstantChatRoom(chatJid, ownerJid, chat.getName(), chat.getLastSeenActivity(), withJid);
	}
	
	private class XMPPServiceListenerImpl implements XMPPClientListener {
		
		@Override
		public void onFriendPresenceChanged(Jid jid, FriendPresence presence, FriendPresence bestPresence) {
			logger.debug("presenceChanged {}", jid.toString());
			final EntityBareJid targetJid = jid.asEntityBareJidIfPossible();
			final String presenceStatus = EnumUtils.toSerializedName(bestPresence.getPresenceStatus());
			getWts().notify(new IMUpdateFriendPresence(targetJid.toString(), bestPresence.getInstantChatJid(), presenceStatus, bestPresence.getStatusMessage()));
		}
		
		@Override
		public void onChatRoomUpdated(ChatRoom chatRoom) {
			getWts().notify(new IMChatRoomUpdated(chatRoom.getChatJid().toString(), chatRoom.getName()));
		}
		
		@Override
		public void onChatRoomAdded(ChatRoom chatRoom) {
			if (chatRoom instanceof InstantChatRoom) {
				InstantChatRoom dcr = (InstantChatRoom)chatRoom;
				logger.debug("Adding direct chat room [{}, {}]", dcr.getChatJid().toString(), dcr.getOwnerJid().toString());
				
				try {
					IMChat hchat = new IMChat();
					hchat.setChatJid(dcr.getChatJid().toString());
					hchat.setOwnerJid(dcr.getOwnerJid().toString());
					hchat.setName(dcr.getName());
					hchat.setIsGroupChat(false);
					hchat.setWithJid(dcr.getWithJid().toString());
					coreMgr.addIMChat(hchat);
					
				} catch(WTException ex) {
					logger.error("Error saving direct chat [{}]", ex, dcr.getChatJid().toString());
				}
				
			} else if (chatRoom instanceof GroupChatRoom) {
				GroupChatRoom gcr = (GroupChatRoom)chatRoom;
				logger.debug("Adding group chat room [{}, {}]", gcr.getChatJid().toString(), gcr.getOwnerJid().toString());
				
				try {
					IMChat hchat = new IMChat();
					hchat.setChatJid(gcr.getChatJid().toString());
					hchat.setOwnerJid(gcr.getOwnerJid().toString());
					hchat.setName(gcr.getName());
					hchat.setIsGroupChat(true);
					coreMgr.addIMChat(hchat);
					
				} catch(WTException ex) {
					logger.error("Error saving group chat [{}]", ex, gcr.getChatJid().toString());
				}
			}
			getWts().notify(new IMChatRoomAdded(chatRoom.getChatJid().toString(), chatRoom.getName()));
		}
		
		@Override
		public void onChatRoomRemoved(EntityBareJid chatJid) {
			
		}
		
		@Override
		public void onChatRoomMessageSent(ChatRoom chatRoom, ChatMessage message) {
			logger.debug("Message sent {}, {}, {}", chatRoom.getChatJid().toString(), message.getStanzaId(), message.getRawMessage().getBody());
			
			try {
				IMMessage hmes = new IMMessage();
				hmes.setChatJid(chatRoom.getChatJid().toString());
				hmes.setSenderJid(message.getFromUser().toString());
				hmes.setSenderResource(message.getFromUserResource());
				hmes.setDate(message.getTimestamp().withZone(getEnv().getProfile().getTimeZone()).toLocalDate());
				hmes.setTimestamp(message.getTimestamp());
				hmes.setAction(IMMessage.Action.NONE);
				hmes.setText(message.getText());
				hmes.setMessageUid(message.getMessageUid());
				hmes.setStanzaId(message.getStanzaId());
				coreMgr.addIMMessage(hmes);
				coreMgr.updateIMChatLastSeenActivity(chatRoom.getChatJid().toString(), chatRoom.getLastSeenActivity());
				
			} catch(WTException ex) {
				logger.error("Error saving chat message [{}, {}]", ex, chatRoom.getChatJid().toString(), message.getMessageUid());
			}
		}
		
		@Override
		public void onChatRoomMessageReceived(ChatRoom chatRoom, ChatMessage message) {
			DateTimeZone utz = getEnv().getProfile().getTimeZone();
			DateTimeFormatter fmt = DateTimeUtils.createYmdHmsFormatter(utz);
			if (chatRoom instanceof InstantChatRoom) {
				InstantChatRoom dcr = (InstantChatRoom)chatRoom;
				logger.debug("Incoming message from instant chat room [{}, {}]", dcr.getChatJid().toString(), dcr.getName());
				getWts().notify(new IMChatRoomMessageReceived(dcr.getChatJid().toString(), dcr.getName(), message.getFromUser().toString(), message.getFromUserNickname(), fmt.print(message.getTimestamp()), message.getMessageUid(), message.getText()));
				
			} else if (chatRoom instanceof GroupChatRoom) {
				GroupChatRoom gcr = (GroupChatRoom)chatRoom;
				logger.debug("Incoming message from group chat room [{}, {}]", gcr.getChatJid().toString(), gcr.getName());
				getWts().notify(new IMChatRoomMessageReceived(gcr.getChatJid().toString(), gcr.getName(), message.getFromUser().toString(), message.getFromUserNickname(), fmt.print(message.getTimestamp()), message.getMessageUid(), message.getText()));
			}
			
			try {
				IMMessage hmes = new IMMessage();
				hmes.setChatJid(chatRoom.getChatJid().toString());
				hmes.setSenderJid(message.getFromUser().toString());
				hmes.setSenderResource(message.getFromUserResource());
				hmes.setDate(message.getTimestamp().withZone(utz).toLocalDate());
				hmes.setTimestamp(message.getTimestamp());
				hmes.setAction(IMMessage.Action.NONE);
				hmes.setText(message.getText());
				hmes.setMessageUid(message.getMessageUid());
				hmes.setStanzaId(message.getStanzaId());
				coreMgr.addIMMessage(hmes);
				coreMgr.updateIMChatLastSeenActivity(chatRoom.getChatJid().toString(), chatRoom.getLastSeenActivity());

			} catch(WTException ex) {
				logger.error("Error saving chat message [{}, {}]", ex, chatRoom.getChatJid().toString(), message.getMessageUid());
			}
		}
		
		@Override
		public void friendsAdded(Collection<Jid> jids) {
			logger.debug("{}", jids.toString());
		}

		@Override
		public void friendsUpdated(Collection<Jid> jids) {
			logger.debug("{}", jids.toString());
		}

		@Override
		public void friendsDeleted(Collection<Jid> jids) {
			logger.debug("{}", jids.toString());
		}

		@Override
		public void onChatRoomParticipantJoined(ChatRoom chatRoom, EntityFullJid participant) {
			logger.debug("{} joins group chat {}", participant.toString(), chatRoom.getChatJid().toString());
		}

		@Override
		public void onChatRoomParticipantLeft(ChatRoom chatRoom, EntityFullJid participant, boolean kicked) {
			logger.debug("{} leaves group chat {}", participant.toString(), chatRoom.getChatJid().toString());
		}
	}
}
