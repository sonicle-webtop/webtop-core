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
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ParameterException;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.security.Principal;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.webtop.core.CoreSettings.OtpDeliveryMode;
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
import com.sonicle.webtop.core.msg.IMChatRoomAdded;
import com.sonicle.webtop.core.msg.IMChatRoomMessageReceived;
import com.sonicle.webtop.core.msg.IMChatRoomRemoved;
import com.sonicle.webtop.core.msg.IMChatRoomClosed;
import com.sonicle.webtop.core.msg.IMChatRoomUpdated;
import com.sonicle.webtop.core.msg.IMFriendPresenceUpdated;
import com.sonicle.webtop.core.msg.IMFriendsUpdated;
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
import com.sonicle.webtop.core.bol.js.JsGridIMMessage;
import com.sonicle.webtop.core.bol.js.JsGridIMChatSearch;
import com.sonicle.webtop.core.bol.js.JsGroupChat;
import com.sonicle.webtop.core.bol.js.JsIMInit;
import com.sonicle.webtop.core.bol.js.JsIMPresenceStatus;
import com.sonicle.webtop.core.bol.js.JsInternetAddress;
import com.sonicle.webtop.core.bol.js.JsPublicImage;
import com.sonicle.webtop.core.bol.js.JsReminderInApp;
import com.sonicle.webtop.core.bol.js.JsRoleLkp;
import com.sonicle.webtop.core.bol.js.JsServicePermissionLkp;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.bol.js.JsTrustedDevice;
import com.sonicle.webtop.core.bol.js.JsWhatsnewTab;
import com.sonicle.webtop.core.bol.js.TrustedDeviceCookie;
import com.sonicle.webtop.core.model.Recipient;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.model.Activity;
import com.sonicle.webtop.core.model.Causal;
import com.sonicle.webtop.core.model.CausalExt;
import com.sonicle.webtop.core.model.IMChat;
import com.sonicle.webtop.core.model.IMMessage;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.model.PublicImage;
import com.sonicle.webtop.core.model.RecipientFieldType;
import com.sonicle.webtop.core.util.AppLocale;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UploadException;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.interfaces.IServiceUploadStreamListener;
import com.sonicle.webtop.core.xmpp.ChatMember;
import com.sonicle.webtop.core.xmpp.Friend;
import com.sonicle.webtop.core.xmpp.FriendPresence;
import com.sonicle.webtop.core.xmpp.ChatMessage;
import com.sonicle.webtop.core.xmpp.ChatRoom;
import com.sonicle.webtop.core.xmpp.ConversationHistory;
import com.sonicle.webtop.core.xmpp.InstantChatRoom;
import com.sonicle.webtop.core.xmpp.GroupChatRoom;
import com.sonicle.webtop.core.xmpp.PresenceStatus;
import com.sonicle.webtop.core.xmpp.XMPPClient;
import com.sonicle.webtop.core.xmpp.XMPPClientException;
import java.io.InputStream;
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
import com.sonicle.webtop.core.xmpp.packet.OutOfBandData;
import com.sonicle.webtop.vfs.IVfsManager;
import com.sonicle.webtop.vfs.model.SharingLink;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.vfs2.FileObject;
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
		
		registerUploadListener(UPLOAD_CONTEXT_WEBCHAT, new OnUploadCloudFile());
		
		Principal principal = profile.getPrincipal();
		if (!principal.isImpersonated()) {
			ConversationHistory history = new ConversationHistory();
			for(IMChat chat : coreMgr.listIMChats(true)) {
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
		
		//sendAuthMessage(principal.getUserId(),principal.getPassword());
	}
	
	//private void sendAuthMessage(String userId, char password[]) {
	//	getEnv().notify(new AuthMessage(SERVICE_ID,userId,password));		
	//}
	
	private CoreManager getCoreManager() {
		return (CoreManager)WT.getServiceManager(SERVICE_ID);
	}
	
	private CoreAdminManager getCoreAdminManager() {
		return (CoreAdminManager)WT.getServiceManager(CoreAdminManifest.ID);
	}
	
	private IVfsManager getVfsManager() {
		return (IVfsManager)WT.getServiceManager("com.sonicle.webtop.vfs");
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
		
		boolean docServerEnabled = getDocumentServerEnabled();
		co.put("docServerEnabled", docServerEnabled);
		if (docServerEnabled) {
			co.put("docServerPublicUrl", ss.getDocumentServerPublicUrl());
		}
		String boshUrl = ss.getXMPPBoshUrl();
		if (!StringUtils.isBlank(boshUrl)) {
			co.put("boshUrl", boshUrl);
		}
		CoreServiceSettings.ICEServersList iceServers = ss.getWebRTC_ICEServers();
		if (iceServers != null) {
			co.put("iceServers", iceServers);
		}
		
		co.put("wtAddonNotifier", addonNotifier());
		co.put("wtWhatsnewEnabled", ss.getWhatsnewEnabled());
		//co.put("wtForcePasswordChange", ss.getOTPEnabled());
		co.put("wtOtpEnabled", ss.getOTPEnabled());
		co.put("wtLauncherLinks", ss.getLauncherLinksAsString());
		co.put("domainPasswordPolicy", domainPasswordPolicy);
		co.put("domainDirCapPasswordWrite", dirCapPasswordWrite);
		co.put("domainInternetName", WT.getDomainInternetName(profile.getDomainId()));
		co.put("profileId", profile.getStringId());
		co.put("domainId", profile.getDomainId());
		co.put("userId", profile.getUserId());
		co.put("userDisplayName", profile.getDisplayName());
		co.put("editorFonts", ss.getEditorFonts());
		co.put("theme", us.getTheme());
		co.put("laf", us.getLookAndFeel());
		co.put("layout", us.getLayout());
		co.put("viewportHeaderScale", EnumUtils.toSerializedName(us.getViewportHeaderScale()));
		co.put("startupService", us.getStartupService());
		co.put("desktopNotification", us.getDesktopNotification());
		
		co.put("ajaxSpecialTimeout", ss.getAjaxSpecialTimeout());
		co.put("language", us.getLanguageTag());
		co.put("timezone", us.getTimezone());
		co.put("startDay", us.getStartDay());
		co.put("shortDateFormat", us.getShortDateFormat());
		co.put("longDateFormat", us.getLongDateFormat());
		co.put("shortTimeFormat", us.getShortTimeFormat());
		co.put("longTimeFormat", us.getLongTimeFormat());
		co.put("imEnabled", !RunContext.isImpersonated() && RunContext.isPermitted(true, CoreManifest.ID, "WEBCHAT", "ACCESS"));
		co.put("imPresenceStatus", EnumUtils.toSerializedName(us.getIMPresenceStatus()));
		co.put("imStatusMessage", us.getIMStatusMessage());
		co.put("imUploadMaxFileSize", us.getIMUploadMaxFileSize(true));
		co.put("imSoundOnFriendConnect", us.getIMSoundOnFriendConnect());
		co.put("imSoundOnFriendDisconnect", us.getIMSoundOnFriendDisconnect());
		co.put("imSoundOnMessageReceived", us.getIMSoundOnMessageReceived());
		co.put("imSoundOnMessageSent", us.getIMSoundOnMessageSent());
		co.put("pbxConfigured",coreMgr.pbxConfigured());
		if (coreMgr.smsConfigured()) {
			co.put("smsConfigured",true);
			co.put("smsProvider",coreMgr.smsGetProvider().getName());
		}
		
		return co;
	}
	
	private String addonNotifier() {
		String url = ss.getAddonNotifierUrl();
		if (!StringUtils.isBlank(url)) return url;
		return LangUtils.classForNameQuietly("com.sonicle.webtop.addons.AddOns") ? "true" : "false";
	}
	
	private WebTopSession getWts() {
		return getEnv().getSession();
	}
	
	public void processLogMessage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String level = ServletUtils.getStringParameter(request, "level", "debug");
			String message = ServletUtils.getStringParameter(request, "message", true);
			
			if (level.equals("debug")) {
				logger.debug("{}", message);
			} else if (level.equals("info")) {
				logger.info("{}", message);
			} else if (level.equals("warn")) {
				logger.warn("{}", message);
			} else if (level.equals("error")) {
				logger.error("{}", message);
			} else {
				logger.trace("{}", message);
			}
			new JsonResult().printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LogMessage", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupLanguages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		LinkedHashMap<String, JsSimple> items = new LinkedHashMap<>();
		Locale locale = getEnv().getSession().getLocale();
		
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
		Locale locale = getEnv().getSession().getLocale();
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			Boolean assignableOnly = ServletUtils.getBooleanParameter(request, "assignableOnly", false);
			
			for(String sid : coreMgr.listWTInstalledServices()) {
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
			for(String sid : coreMgr.listWTInstalledServices()) {
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
			if (coreMgr.isTargetProfileEqualTo(pid)) {
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
			if (coreMgr.isTargetProfileEqualTo(pid)) {
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
			
			final List<String> types = Arrays.asList(
				EnumUtils.toSerializedName(MasterData.Type.CUSTOMER),
				EnumUtils.toSerializedName(MasterData.Type.SUPPLIER)
			);
			for(MasterData entry : coreMgr.listMasterData(types, "%" + query + "%")) {
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
			
			final List<String> types = Arrays.asList(
				EnumUtils.toSerializedName(MasterData.Type.CUSTOMER),
				EnumUtils.toSerializedName(MasterData.Type.SUPPLIER)
			);
			for(MasterData entry : coreMgr.listChildrenMasterData(parentMasterDataId, types, "%" + query + "%")) {
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
	
	
	/*
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
	*/
	
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
				data = coreMgr.getAllowedUserOptionServices();
			} else {
				CoreManager xcore = WT.getCoreManager(targetPid);
				data = xcore.getAllowedUserOptionServices();
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
		WebTopSession wts = getEnv().getSession();
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
	
	static final String WHATSNEW_STYLES=
		"html * { font-family: Arial !important; }\n"+
		".wt-whatsnew {\n" +
		"	padding: 10px;\n" +
		"	font-family: Helvetica, Arial, sans-serif;\n" +
		"}\n" +
		".wt-whatsnew > div.wt-wntitle:first-child {\n" +
		"	margin-top: 0;\n" +
		"}\n" +
		"div.wt-wntitle {\n" +
		"	margin-top: 1em;\n" +
		"	/*font-size: 220%;*/\n" +
		"	font-size: 150%;\n" +
		"	font-weight: bold;\n" +
		"}\n" +
		"div.wt-wnsubtitle {\n" +
		"	margin-top: 0em;\n" +
		"	/*font-size: 130%;*/\n" +
		"	font-size: 110%;\n" +
		"	font-family: Georgia,\"Times New Roman\",Times,serif;\n" +
		"	font-style: italic;\n" +
		"	color: #797c80;\n" +
		"}\n" +
		"div.wt-wnlist {\n" +
		"	margin-top: 1em;\n" +
		"	/*font-size: 110%;*/\n" +
		"}\n" +
		".wt-wnlist ul {\n" +
		"	padding-left: 30px;\n" +
		"}\n" +
		".wt-wnlist ul li {\n" +
		"	list-style: initial;\n" +
		"}\n" +
		".wt-wnlist ul li h1, .wt-wnlist ul li h2, .wt-wnlist ul li h3 {\n" +
		"	margin-top: .2em;\n" +
		"	font-size: 110%;\n" +
		"	font-weight: bold;\n" +
		"}\n" +
		".wt-wnlist ul li p {\n" +
		"	margin-bottom: .5em;\n" +
		"}\n" +
		".wt-wnlist ul li img {\n" +
		"	padding: .2em;\n" +
		"	margin-bottom: .5em;\n" +
		"	background-color: #fff;\n" +
		"	box-shadow: 2px 2px 5px #e1e1e1;\n" +
		"	border: solid 1px #ccc;\n" +
		"}\n";
	
	public void processGetWhatsnewHTML(HttpServletRequest request, HttpServletResponse response) {
		WebTopSession wts = ((CorePrivateEnvironment)getEnv()).getSession();
		
		try {
			String id = ServletUtils.getStringParameter(request, "id", true);
			boolean full = ServletUtils.getBooleanParameter(request, "full", false);
			String baseUrl=PathUtils.concatPaths(wts.getClientUrl(), "resources/"+id+"/whatsnew/");
			String html = 
					"<html><head><base href=\""+baseUrl+"\"><style>"+WHATSNEW_STYLES+"</style></head><body>"+
					wts.getWhatsnewHtml(id, getEnv().getProfile(), full)+
					"</body><html>";
			ServletUtils.setContentTypeHeader(response, "text/html");
			IOUtils.copy(new StringReader(html), response.getOutputStream());
			
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
					ServletUtils.setFileStreamHeadersForceDownload(response, "notifier.exe");
					IOUtils.copy(is, response.getOutputStream());
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
	
	public void processPbxCall(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String number = ServletUtils.getStringParameter(request, "number", null);

		if (coreMgr.pbxConfigured() && number!=null) {
			try {
				coreMgr.pbxCall(number);
				new JsonResult().printTo(out);
			} catch(WTException exc) {
				logger.error("Error during PBX Call",exc);
				new JsonResult(false,exc.getMessage()).printTo(out);
			}
		} else {
			new JsonResult(false,"No PBX Configured").printTo(out);
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
			if (operation.equals("configure") || operation.equals("activate") || operation.equals("deactivate")) {
				// These work only on a target user!
				String profileId = ServletUtils.getStringParameter(request, "profileId", true);
				
				UserProfileId targetPid = new UserProfileId(profileId);
				corem = (targetPid.equals(coreMgr.getTargetProfileId())) ? coreMgr : WT.getCoreManager(targetPid);
				
				if (operation.equals("configure")) {
					OtpDeliveryMode deliveryMode = ServletUtils.getEnumParameter(request, "delivery", true, OtpDeliveryMode.class);
					if (OtpDeliveryMode.EMAIL.equals(deliveryMode)) {
						String address = ServletUtils.getStringParameter(request, "address", true);
						InternetAddress ia = InternetAddressUtils.toInternetAddress(address, null);
						if (!InternetAddressUtils.isAddressValid(ia)) throw new WTException("Email address not valid"); //TODO: messaggio in lingua
						
						OTPManager.EmailConfig config = corem.otpConfigureUsingEmail(address);
						wts.setProperty(SERVICE_ID, WTSPROP_OTP_SETUP, config);

					} else if (OtpDeliveryMode.GOOGLEAUTH.equals(deliveryMode)) {
						OTPManager.GoogleAuthConfig config = corem.otpConfigureUsingGoogleAuth(200);
						wts.setProperty(SERVICE_ID, WTSPROP_OTP_SETUP, config);
					}
					new JsonResult(true).printTo(out);
					
				} else if (operation.equals("activate")) {
					int code = ServletUtils.getIntParameter(request, "code", true);

					OTPManager.Config config = (OTPManager.Config)wts.getProperty(SERVICE_ID, WTSPROP_OTP_SETUP);
					boolean enabled = corem.otpActivate(config, code);
					/*
					if (!enabled) {
						throw new WTLocalizedException(lookupResource(CoreLocaleKey.OTP_SETUP_ERROR_CODE), "Invalid code");
					}
					*/
					if (!enabled) throw new WTException("Invalid code"); //TODO: messaggio in lingua
					wts.clearProperty(SERVICE_ID, WTSPROP_OTP_SETUP);
					
					new JsonResult().printTo(out);

				} else if (operation.equals("deactivate")) {
					corem.otpDeactivate();
					new JsonResult().printTo(out);

				}
			} else if (operation.equals("untrustthis")) {
				// This works only on current session user!
				OTPManager otpm = coreMgr.getOTPManager();
				TrustedDeviceCookie tdc = otpm.readTrustedDeviceCookie(pid, request);
				if (tdc != null) {
					otpm.removeTrustedDevice(pid, tdc.deviceId);
					otpm.clearTrustedDeviceCookie(pid, response);
				}
				new JsonResult().printTo(out);
				
			} else if (operation.equals("untrustothers")) {
				// This works only on current session user!
				OTPManager otpm = coreMgr.getOTPManager();
				TrustedDeviceCookie thistdc = otpm.readTrustedDeviceCookie(pid, request);
				List<JsTrustedDevice> tds = otpm.listTrustedDevices(pid);
				for(JsTrustedDevice td: tds) {
					if ((thistdc != null) && (td.deviceId.equals(thistdc.deviceId))) continue;
					otpm.removeTrustedDevice(pid, td.deviceId);
				}
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageOTP", ex);
			new JsonResult(false, "Error in ManageOTP").printTo(out);
		}
	}
	
	public void processCleanupDocManagerEditing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String editingId = ServletUtils.getStringParameter(request, "editingId", true);
			getWts().finalizeDocumentEditing(editingId);
			new JsonResult().printTo(out);
			
		} catch(ParameterException ex) {
			logger.error("Error in CleanupDocManagerEditing", ex);
			new JsonResult(ex).printTo(out);
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
		
	public void processListInternetRecipientsSources(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsSimple> items = new ArrayList<>();
		
		try {
			for(String soid : coreMgr.listRecipientProviderSourceIds()) {
				RecipientsProviderBase provider = coreMgr.getProfileRecipientsProvider(soid);
				items.add(new JsSimple(soid, provider.getDescription()));
			}
			new JsonResult("sources", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in ListInternetRecipientsSource", ex);
			new JsonResult(false, "Error in ListInternetRecipientsSources").printTo(out);
		}
	}
	
	public void processManageInternetRecipients(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<Recipient> items = null;
		
		try {
			ArrayList<String> sources = ServletUtils.getStringParameters(request, "sources");
			String crud = ServletUtils.getStringParameter(request, "crud", Crud.READ);
			String query = ServletUtils.getStringParameter(request, "query", "");
			boolean builtInAtTheEnd = ServletUtils.getBooleanParameter(request, "autoLast", false);
			RecipientFieldType rft = ServletUtils.getEnumParameter(request, "rftype", RecipientFieldType.EMAIL, RecipientFieldType.class);
			if (crud.equals(Crud.READ)) {
				int limit = ServletUtils.getIntParameter(request, "limit", 100);
				if (limit==0) limit=Integer.MAX_VALUE;

				if (sources.isEmpty()) {
					items = coreMgr.listProviderRecipients(rft, query, limit, builtInAtTheEnd);
				} else {
					items = coreMgr.listProviderRecipients(rft, sources, query, limit);
				}
				
				if (rft.equals(RecipientFieldType.FAX)) {
					//compile fax email patterns
					for(Recipient r: items) {
						r.setAddress(compileFaxPattern(r));
					}
				}
				
				new JsonResult("recipients", items, items.size()).printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsInternetAddress> pl = ServletUtils.getPayload(request, JsInternetAddress.class);
				if (CoreManager.RECIPIENT_PROVIDER_AUTO_SOURCE_ID.equals(pl.data.source)) {
					String fullAddress = InternetAddressUtils.toFullAddress(pl.data.address, pl.data.personal);
					if (fullAddress != null) {
						coreMgr.deleteServiceStoreEntry(SERVICE_ID, "recipients", fullAddress.toUpperCase());
					}
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
	
	private String compileFaxPattern(Recipient r) {
		String faxAddress=r.getAddress();
		//fix fax number with "+"
        StringBuffer newfax=new StringBuffer();
        for(char c: faxAddress.toCharArray()) {
            if (Character.isDigit(c)) newfax.append(c);
            else if (c=='+') {
                if (newfax.length()==0) newfax.append("00");
            }
        }
		String username=r.getPersonal().toLowerCase().replaceAll(" ", ".");
		String faxPattern=ss.getFaxPattern();
		return faxPattern.replace("{username}", username).replace("{number}", newfax.toString());
	}
	
	public void processListDomainPublicImages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", null);
			
			List<PublicImage> images;
			if (RunContext.isSysAdmin()) {
				if (StringUtils.isBlank(domainId)) throw new WTException();
				CoreAdminManager coreAdmMgr = getCoreAdminManager();
				images = coreAdmMgr.listDomainPublicImages(domainId);
				
			} else { // Domain users can only use images belonging to their own domain
				images = coreMgr.listDomainPublicImages();
			}
			
			ArrayList<JsPublicImage> items = new ArrayList<>(images.size());
			int i = 0;
			for (PublicImage image : images) {
				items.add(new JsPublicImage("img" + i, image));
				i++;
			}
			new JsonResult("images", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in ListDomainPublicImages", ex);
			new JsonResult(false, "Error in ListDomainPublicImages").printTo(out);
		}
	}
	
	public void processRestoreAutosave(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con=null;
		try {
			con=getConnection();
			boolean mine=ServletUtils.getBooleanParameter(request, "mine", true);
			String cid = getWts().getClientTrackingID();
			
			List<OAutosave> items = (mine) ? coreMgr.listMyAutosaveData(cid) : coreMgr.listOfflineOthersAutosaveData(cid);
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
	
	public void processSendSMS(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String number=ServletUtils.getStringParameter(request, "number", true);
			String text=ServletUtils.getStringParameter(request, "text", true);
			coreMgr.smsSend(number,text);
			new JsonResult().printTo(out);
		} catch (Exception ex) {
			logger.error("Error in processSendSMS", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
		}
	}
	
	public void processRemoveAutosave(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Connection con=null;
		try {
			con=getConnection();
			boolean allOthers=ServletUtils.getBooleanParameter(request, "allOthers", false);
			String cid = getWts().getClientTrackingID();
			
			if (allOthers) {
				coreMgr.deleteOfflineOthersAutosaveData(cid);
			} else {
				boolean allMine=ServletUtils.getBooleanParameter(request, "allMine", false);
				if (allMine) {
					coreMgr.deleteMyAutosaveData(cid);
				} else {
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
					PresenceStatus ps = us.getIMPresenceStatus();
					String statusMessage = "Hey there! I'm on WebTop";
							
					try {
						xmppCli.updatePresence(ps, statusMessage);
						
					} catch(XMPPClientException ex1) {
						if (!xmppCli.isConnected()) throw new WTException(ex1, lookupResource(CoreLocaleKey.XMPP_ERROR_CONNECTION));
						if (!xmppCli.isAuthenticated()) throw new WTException(ex1, lookupResource(CoreLocaleKey.XMPP_ERROR_AUTHENTICATION));
						throw ex1;
					}
					Principal p = getEnv().getProfile().getPrincipal();
					new JsonResult(new JsIMInit(ps, statusMessage, p.getUserId()+"@"+p.getAuthenticationDomain().getInternetName(), xmppCli.getUserJid().toString(), p.getPassword())).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
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
					
				} else {
					throw new WTException("XMPPClient not available");
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
				
				/*
				if (xmppCli != null) {
					for(ChatRoom chat : xmppCli.listChats()) {
						items.add(new JsGridIMChat(chat));
					}
				} else {
					for(IMChat chat : coreMgr.listIMChats(true)) {
						items.add(new JsGridIMChat(chat));
					}
				}
				*/
				for(IMChat chat : coreMgr.listIMChats(false)) {
					items.add(new JsGridIMChat(chat));
				}
					
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsGridIMChat> pl = ServletUtils.getPayload(request, JsGridIMChat.class);
				
				if (xmppCli != null) {
					xmppCli.forgetChat(pl.data.id);
				}
				coreMgr.deleteIMChat(pl.data.id);
				
				final IVfsManager vfsMgr = getVfsManager();
				if (vfsMgr != null) {
					final String path = "/" + WEBCHAT_VFS_FOLDER + "/" + pl.data.id + "/";
					int myDocsStoreId = vfsMgr.getMyDocumentsStoreId();
					
					vfsMgr.deleteStoreFile(myDocsStoreId, path);
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageGridIMChats", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageIMChat(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		DateTimeZone utz = up.getTimeZone();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals("presence")) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				
				if (xmppCli != null) {
					if (!XMPPClient.isInstantChat(chatId)) throw new WTException("Presence feature non available for a grupchat");
					
					String friendFullId = null;
					String presenceStatus = null;
					FriendPresence presence = xmppCli.getChatPresence(chatId);
					if (presence == null) {
						presenceStatus = EnumUtils.toSerializedName(PresenceStatus.OFFLINE);
					} else {
						friendFullId = presence.getFriendFullJid();
						presenceStatus = EnumUtils.toSerializedName(presence.getPresenceStatus());
					}
					new JsonResult(new JsIMPresenceStatus(friendFullId, presenceStatus)).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
				}
				
			} else if (crud.equals("dates")) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				int year = ServletUtils.getIntParameter(request, "year", true);
				
				List<String> items = new ArrayList<>();
				List<LocalDate> dates = coreMgr.listIMMessageDates(chatId, year, utz);
				for(LocalDate date : dates) {
					items.add(date.toString());
				}
				
				new JsonResult(items).printTo(out);
				
			} else if (crud.equals("send")) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				String text = ServletUtils.getStringParameter(request, "text", true);
				String lastSeenDate = ServletUtils.getStringParameter(request, "lastSeenDate", null);
				
				LocalDate lastSeen = (lastSeenDate == null) ? null : DateTimeUtils.parseYmdHmsWithZone(lastSeenDate, "00:00:00", utz).toLocalDate();
				
				if (xmppCli != null) {
					EntityBareJid chatJid = XMPPHelper.asEntityBareJid(chatId);
					List<JsGridIMMessage> items = new ArrayList<>();
					
					ChatMessage message = xmppCli.sendTextMessage(chatJid, text);
					if (message == null) throw new Exception("Message is null");
					
					if (lastSeen != null) {
						final LocalDate mesDate = message.getTimestampDate(utz);
						if (!mesDate.equals(lastSeen)) {
							final String msgId = ChatMessage.buildUniqueId(chatJid, "dummy-date", message.getTimestamp());
							items.add(JsGridIMMessage.asDateAction(msgId, mesDate));
						}
					}
					items.add(new JsGridIMMessage(true, message, getEnv().getProfile().getTimeZone()));
					
					new JsonResult(items, items.size()).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
				}
				
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageIMChat", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGridIMChatMessages(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		DateTimeZone utz = up.getTimeZone();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				String date = ServletUtils.getStringParameter(request, "date", null);
				
				final LocalDate nowLd = DateTime.now().withZone(utz).toLocalDate();
				LocalDate ld = (date == null) ? nowLd : DateTimeUtils.parseYmdHmsWithZone(date, "00:00:00", utz).toLocalDate();
				boolean history = (ld.compareTo(nowLd) != 0);
				
				IMChat chat = coreMgr.getIMChat(chatId);
				if (xmppCli != null) {
					List<JsGridIMMessage> items = new ArrayList<>();
					EntityBareJid chatJid = XMPPHelper.asEntityBareJid(chatId);
					EntityBareJid myJid = xmppCli.getUserJid().asEntityBareJid();
					
					final DateTime messageTs = ChatMessage.nowTimestamp();
					LocalDate lastDate = null;
					
					HashMap<String, String> cacheNicks = new HashMap<>();
					cacheNicks.put(myJid.toString(), xmppCli.getUserNickame()); // Fill cache with my data
					List<IMMessage> messages = coreMgr.listIMMessages(chatId, ld, utz, !history);
					
					// Add unavailable warning at the beginning
					if (history && !messages.isEmpty()) {
						if (chat != null && chat.isUnavailable()) {
							final String msgId = ChatMessage.buildUniqueId(chatJid, "dummy-unavailable1", messageTs);
							items.add(JsGridIMMessage.asWarnAction(msgId, messageTs, "unavailable"));
						}
					}
					
					for(IMMessage mes : messages) {
						final LocalDate mesDate = mes.getTimestampDate(utz);
						if (!mesDate.equals(lastDate)) {
							lastDate = mesDate;
							final String msgId = ChatMessage.buildUniqueId(chatJid, "dummy-date", mes.getTimestamp());
							items.add(JsGridIMMessage.asDateAction(msgId, lastDate));
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
					
					// Add unavailable warning at the end
					if (chat != null && chat.isUnavailable()) {
						final String msgId = ChatMessage.buildUniqueId(chatJid, "dummy-unavailable2", messageTs);
						items.add(JsGridIMMessage.asWarnAction(msgId, messageTs, "unavailable"));
					}
					
					new JsonResult(items, items.size()).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageGridIMChatMessages", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGridIMChatSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		DateTimeZone utz = up.getTimeZone();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String chatId = ServletUtils.getStringParameter(request, "chatId", true);
				String query = ServletUtils.getStringParameter(request, "query", null);
				
				if (xmppCli != null) {
					List<JsGridIMChatSearch> items = new ArrayList<>();
					EntityBareJid myJid = xmppCli.getUserJid().asEntityBareJid();
					
					if (query != null) {
						HashMap<String, String> cacheNicks = new HashMap<>();
						cacheNicks.put(myJid.toString(), xmppCli.getUserNickame()); // Fill cache with my data
						List<IMMessage> messages = coreMgr.findIMMessagesByQuery(chatId, "%"+query+"%", utz);
						
						for(IMMessage mes : messages) {
							if (!cacheNicks.containsKey(mes.getSenderJid())) {
								if (xmppCli.isAuthenticated()) {
									cacheNicks.put(mes.getSenderJid(), xmppCli.getFriendNickname(XMPPHelper.asEntityBareJid(mes.getSenderJid())));
								} else {
									cacheNicks.put(mes.getSenderJid(), XMPPHelper.buildGuessedString(mes.getSenderJid()));
								}
							}
							final String nick = cacheNicks.get(mes.getSenderJid());
							items.add(new JsGridIMChatSearch(mes, nick, utz));
						}
					}
					
					new JsonResult(items, items.size()).printTo(out);
					
				} else {
					throw new WTException("XMPPClient not available");
				}
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageGridIMChatSearch", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageGroupChat(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				
				ChatRoom chat = xmppCli.getChat(id);
				List<ChatMember> members = xmppCli.getChatMembers(id);
				new JsonResult(new JsGroupChat((GroupChatRoom)chat, members)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsGroupChat> pl = ServletUtils.getPayload(request, JsGroupChat.class);
				
				if (xmppCli != null) {
					ArrayList<EntityBareJid> withUsers = new ArrayList<>();
					for(JsGroupChat.Member member : pl.data.members) {
						withUsers.add(XMPPHelper.asEntityBareJid(member.friendId));
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
	
	private IMMessage createIMMessage(EntityBareJid chatJid, ChatMessage message) {
		IMMessage mes = new IMMessage();
		mes.setChatJid(chatJid.toString());
		mes.setSenderJid(message.getFromUser().toString());
		mes.setSenderResource(message.getFromUserResource());
		mes.setTimestamp(message.getTimestamp());
		mes.setDeliveryTimestamp(message.getDeliveryTimestamp());
		mes.setAction(IMMessage.Action.NONE);
		mes.setText(message.getText());
		mes.setData(null);
		OutOfBandData oob = message.getOutOfBandExtension();
		if (oob != null) {
			mes.setAction(IMMessage.Action.FILE);
			mes.setData(JsGridIMMessage.toData(message.getText(), oob));
		}
		mes.setMessageUid(message.getMessageUid());
		mes.setStanzaId(message.getStanzaId());
		return mes;
	}
	
	private IMMessage createIMMessage(EntityBareJid chatJid, EntityBareJid senderJid, DateTime timestamp, DateTime deliveryTimestamp, IMMessage.Action action, String text) {
		IMMessage mes = new IMMessage();
		mes.setChatJid(chatJid.toString());
		mes.setSenderJid(senderJid.toString());
		mes.setSenderResource(null);
		mes.setTimestamp(timestamp);
		mes.setDeliveryTimestamp(deliveryTimestamp);
		mes.setAction(action);
		mes.setText(text);
		mes.setData(null);
		mes.setMessageUid(ChatMessage.buildUniqueId(senderJid, timestamp));
		mes.setStanzaId(null);
		return mes;
	}
	
	private class XMPPServiceListenerImpl implements XMPPClientListener {
		
		@Override
		public void onFriendsAdded(Collection<Jid> jids) {
			getWts().notify(new IMFriendsUpdated());
		}

		@Override
		public void onFriendsUpdated(Collection<Jid> jids) {
			getWts().notify(new IMFriendsUpdated());
		}

		@Override
		public void onFriendsDeleted(Collection<Jid> jids) {
			getWts().notify(new IMFriendsUpdated());
		}
		
		@Override
		public void onFriendPresenceChanged(Jid jid, FriendPresence presence, FriendPresence bestPresence) {
			logger.debug("presenceChanged {}", jid.toString());
			final EntityBareJid targetBareJid = jid.asEntityBareJidIfPossible();
			final String presenceStatus = EnumUtils.toSerializedName(bestPresence.getPresenceStatus());
			getWts().notify(new IMFriendPresenceUpdated(targetBareJid.toString(), bestPresence.getFriendFullJid(), bestPresence.getInstantChatBareJid(), presenceStatus, bestPresence.getStatusMessage()));
		}
		
		@Override
		public void onChatRoomUpdated(ChatRoom chatRoom, boolean self) {
			getWts().notify(new IMChatRoomUpdated(chatRoom.getChatJid().toString(), chatRoom.getName()));
		}
		
		@Override
		public void onChatRoomAdded(ChatRoom chatRoom, String ownerNick, boolean self) {
			if (chatRoom instanceof InstantChatRoom) {
				InstantChatRoom dcr = (InstantChatRoom)chatRoom;
				logger.trace("Adding direct chat room [{}, {}]", dcr.getChatJid().toString(), dcr.getOwnerJid().toString());
				
				try {
					IMChat chat = new IMChat();
					chat.setChatJid(dcr.getChatJid().toString());
					chat.setOwnerJid(dcr.getOwnerJid().toString());
					chat.setName(dcr.getName());
					chat.setIsGroupChat(false);
					chat.setWithJid(dcr.getWithJid().toString());
					coreMgr.addIMChat(chat);
					
				} catch(WTException ex) {
					logger.error("Error saving direct chat [{}]", ex, dcr.getChatJid().toString());
				}
				
			} else if (chatRoom instanceof GroupChatRoom) {
				GroupChatRoom gcr = (GroupChatRoom)chatRoom;
				logger.trace("Adding group chat room [{}, {}]", gcr.getChatJid().toString(), gcr.getOwnerJid().toString());
				
				try {
					IMChat chat = new IMChat();
					chat.setChatJid(gcr.getChatJid().toString());
					chat.setOwnerJid(gcr.getOwnerJid().toString());
					chat.setName(gcr.getName());
					chat.setIsGroupChat(true);
					coreMgr.addIMChat(chat);
					
				} catch(WTException ex) {
					logger.error("Error saving group chat [{}]", ex, gcr.getChatJid().toString());
				}
			}
			getWts().notify(new IMChatRoomAdded(chatRoom.getChatJid().toString(), chatRoom.getName(), chatRoom.getOwnerJid().asEntityBareJidString(), ownerNick, self));
		}
		
		@Override
		public void onChatRoomRemoved(EntityBareJid chatJid, String chatName, EntityBareJid ownerJid, String ownerNick) {
			try {
				coreMgr.deleteIMChat(chatJid.asEntityBareJidString());

			} catch(WTException ex) {
				logger.error("Error deleting group chat [{}]", ex, chatJid.toString());
			}
			getWts().notify(new IMChatRoomRemoved(chatJid.toString(), chatName, ownerJid.asEntityBareJidString(), ownerNick));
		}
		
		@Override
		public void onChatRoomUnavailable(ChatRoom chatRoom, String ownerNick) {
			//DateTimeZone utz = getEnv().getProfile().getTimeZone();
			
			try {
				coreMgr.updateIMChatAvailablity(chatRoom.getChatJid().asEntityBareJidString(), false);
				IMMessage mes = createIMMessage(chatRoom.getChatJid(), chatRoom.getOwnerJid(), chatRoom.getLastSeenActivity(), null, IMMessage.Action.CHAT_CLOSE, null);
				coreMgr.addIMMessage(mes);

			} catch(WTException ex) {
				logger.error("Error deleting group chat [{}]", ex, chatRoom.getChatJid().toString());
			}
			
			getWts().notify(new IMChatRoomClosed(chatRoom.getChatJid().toString(), chatRoom.getName(), chatRoom.getOwnerJid().asEntityBareJidString(), ownerNick));
		}
		
		@Override
		public void onChatRoomMessageSent(ChatRoom chatRoom, ChatMessage message) {
			//DateTimeZone utz = getEnv().getProfile().getTimeZone();
			logger.trace("Message sent {}, {}, {}", chatRoom.getChatJid().toString(), message.getStanzaId(), message.getRawMessage().getBody());
			
			try {
				IMMessage mes = createIMMessage(chatRoom.getChatJid(), message);
				coreMgr.addIMMessage(mes);
				coreMgr.updateIMChatLastSeenActivity(chatRoom.getChatJid().toString(), chatRoom.getLastSeenActivity());
				
			} catch(WTException ex) {
				logger.error("Error saving chat message [{}, {}]", ex, chatRoom.getChatJid().toString(), message.getMessageUid());
			}
		}
		
		@Override
		public void onChatRoomMessageReceived(ChatRoom chatRoom, ChatMessage message) {
			DateTimeZone utz = getEnv().getProfile().getTimeZone();
			DateTimeFormatter fmt = DateTimeUtils.createYmdHmsFormatter(utz);
			
			String action = EnumUtils.toSerializedName(IMMessage.Action.NONE);
			String data = null;
			OutOfBandData oob = message.getOutOfBandExtension();
			if (oob != null) {
				action = EnumUtils.toSerializedName(IMMessage.Action.FILE);
				data = JsGridIMMessage.toData(message.getText(), oob);
			}
			getWts().notify(new IMChatRoomMessageReceived(chatRoom.getChatJid().toString(), chatRoom.getName(), message.getFromUser().toString(), message.getFromUserNickname(), fmt.print(message.getTimestamp()), message.getMessageUid(), action, message.getText(), data));
			
			try {
				IMMessage mes = createIMMessage(chatRoom.getChatJid(), message);
				coreMgr.addIMMessage(mes);
				coreMgr.updateIMChatLastSeenActivity(chatRoom.getChatJid().toString(), chatRoom.getLastSeenActivity());

			} catch(WTException ex) {
				logger.error("Error saving chat message [{}, {}]", ex, chatRoom.getChatJid().toString(), message.getMessageUid());
			}
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
	
	public static final String WEBCHAT_VFS_FOLDER = "WebChat";
	private static final String UPLOAD_CONTEXT_WEBCHAT = "UploadWebChatFile";
	
	private class OnUploadCloudFile implements IServiceUploadStreamListener {
		@Override
		public void onUpload(String context, HttpServletRequest request, HashMap<String, String> multipartParams, WebTopSession.UploadedFile file, InputStream is, MapItem responseData) throws UploadException {
			
			if (context.equals(UPLOAD_CONTEXT_WEBCHAT)) {
				final IVfsManager vfsMgr = getVfsManager();
				if (xmppCli == null) throw new UploadException("XMPPClient not available");
				if (vfsMgr == null) throw new UploadException("VfsManager not available");
				
				try {
					String chatId = ServletUtils.getStringParameter(request, "chatId", true);
					
					final String path = "/" + WEBCHAT_VFS_FOLDER + "/" + chatId + "/";
					int myDocsStoreId = vfsMgr.getMyDocumentsStoreId();
					
					FileObject foPath = vfsMgr.getStoreFile(myDocsStoreId, path);
					foPath.createFolder(); // Ensure hierarchy existence
					String newPath = vfsMgr.addStoreFileFromStream(myDocsStoreId, path, file.getFilename(), is, false);
					FileObject foNew = vfsMgr.getStoreFile(myDocsStoreId, newPath);
					
					SharingLink sl = new SharingLink.BuilderForAdd(myDocsStoreId, newPath)
							.permanent()
							.free()
							.silent()
							.build();
					sl = vfsMgr.addDownloadLink(sl);
					URI[] urls = vfsMgr.getSharingLinkPublicURLs(sl);
					
					ChatMessage message = xmppCli.sendFileMessage(chatId, file.getFilename(), urls[2].toString(), file.getMediaType(), foNew.getContent().getSize());
					
					
					JsGridIMMessage js = new JsGridIMMessage(true, message, getEnv().getProfile().getTimeZone());
					responseData.put("id", js.id);
					responseData.put("fromId", js.fromId);
					responseData.put("fromNick", js.fromNick);
					responseData.put("isSent", js.isSent);
					responseData.put("timestamp", js.timestamp);
					responseData.put("action", js.action);
					responseData.put("text", js.text);
					responseData.put("data", js.data);
					responseData.put("fromArchive", js.fromArchive);
					
				} catch(UploadException ex) {
					throw ex;
				} catch(Throwable t) {
					logger.error("Error uploading chat file", t);
					throw new UploadException("Upload failure");
				}
			}
		}
	}
}
