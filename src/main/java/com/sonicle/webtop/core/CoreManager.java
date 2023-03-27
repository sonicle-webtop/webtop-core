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

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.sonicle.commons.BitFlag;
import com.sonicle.commons.BitFlagEnum;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.URIUtils;
import com.sonicle.commons.beans.PageInfo;
import com.sonicle.commons.beans.VirtualAddress;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.JsonUtils;
import com.sonicle.commons.web.json.ipstack.IPLookupResponse;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.DataSourcesManager;
import com.sonicle.webtop.core.app.OTPManager;
import com.sonicle.webtop.core.app.ServiceManager;
import com.sonicle.webtop.core.app.SessionManager;
import com.sonicle.webtop.core.app.SettingsManager;
import com.sonicle.webtop.core.app.WebTopManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.io.dbutils.FilterInfo;
import com.sonicle.webtop.core.app.io.dbutils.FilterableArrayListHandler;
import com.sonicle.webtop.core.app.io.dbutils.RowsAndCols;
import com.sonicle.webtop.core.app.model.AclSubjectGetOption;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainBase;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.pbx.PbxProvider;
import com.sonicle.webtop.core.app.provider.RecipientsProviderBase;
import com.sonicle.webtop.core.app.sdk.ChangedEvent;
import com.sonicle.webtop.core.app.sdk.EventListener;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.sms.SmsProvider;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.bol.VCausal;
import com.sonicle.webtop.core.bol.OActivity;
import com.sonicle.webtop.core.bol.OAuditLog;
import com.sonicle.webtop.core.bol.OAutosave;
import com.sonicle.webtop.core.bol.OCausal;
import com.sonicle.webtop.core.bol.OCustomField;
import com.sonicle.webtop.core.bol.OCustomPanel;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.OIMChat;
import com.sonicle.webtop.core.bol.OIMMessage;
import com.sonicle.webtop.core.bol.OMasterData;
import com.sonicle.webtop.core.bol.OSnoozedReminder;
import com.sonicle.webtop.core.bol.ORolePermission;
import com.sonicle.webtop.core.bol.OServiceStoreEntry;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.OShareData;
import com.sonicle.webtop.core.bol.OTag;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.VCustomField;
import com.sonicle.webtop.core.bol.VCustomPanel;
import com.sonicle.webtop.core.bol.events.TagChangedEvent;
import com.sonicle.webtop.core.model.ServicePermission;
import com.sonicle.webtop.core.model.ServiceSharePermission;
import com.sonicle.webtop.core.model.SharePermsElements;
import com.sonicle.webtop.core.model.SharePermsFolder;
import com.sonicle.webtop.core.model.IncomingShareRoot;
import com.sonicle.webtop.core.model.Recipient;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.SyncDevice;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.dal.ActivityDAO;
import com.sonicle.webtop.core.dal.AuditLogDAO;
import com.sonicle.webtop.core.dal.AutosaveDAO;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.CausalDAO;
import com.sonicle.webtop.core.dal.CustomFieldDAO;
import com.sonicle.webtop.core.dal.CustomPanelDAO;
import com.sonicle.webtop.core.dal.CustomPanelFieldDAO;
import com.sonicle.webtop.core.dal.CustomPanelTagDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.IMChatDAO;
import com.sonicle.webtop.core.dal.IMMessageDAO;
import com.sonicle.webtop.core.dal.MasterDataDAO;
import com.sonicle.webtop.core.dal.SnoozedReminderDAO;
import com.sonicle.webtop.core.dal.RolePermissionDAO;
import com.sonicle.webtop.core.dal.ServiceStoreEntryDAO;
import com.sonicle.webtop.core.dal.ShareDAO;
import com.sonicle.webtop.core.dal.ShareDataDAO;
import com.sonicle.webtop.core.dal.TagDAO;
import com.sonicle.webtop.core.dal.UserDAO;
import com.sonicle.webtop.core.app.model.GenericSubject;
import com.sonicle.webtop.core.model.Activity;
import com.sonicle.webtop.core.model.AuditLog;
import com.sonicle.webtop.core.model.Causal;
import com.sonicle.webtop.core.model.CausalExt;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldBase;
import com.sonicle.webtop.core.model.CustomFieldEx;
import com.sonicle.webtop.core.model.CustomPanel;
import com.sonicle.webtop.core.model.CustomPanelBase;
import com.sonicle.webtop.core.model.DataSourceQuery;
import com.sonicle.webtop.core.model.DataSourceBase;
import com.sonicle.webtop.core.model.DataSourcePooled;
import com.sonicle.webtop.core.model.DomainEntity;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.app.model.FolderShareOriginFolders;
import com.sonicle.webtop.core.model.IMChat;
import com.sonicle.webtop.core.model.IMMessage;
import com.sonicle.webtop.core.model.ListTagsOpt;
import com.sonicle.webtop.core.model.UILookAndFeel;
import com.sonicle.webtop.core.model.MasterData;
import com.sonicle.webtop.core.model.MasterDataLookup;
import com.sonicle.webtop.core.model.Meeting;
import com.sonicle.webtop.core.model.PublicImage;
import com.sonicle.webtop.core.model.RecipientFieldType;
import com.sonicle.webtop.core.app.model.FolderSharing;
import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.app.model.ResourcePermissions;
import com.sonicle.webtop.core.app.model.ShareOrigin;
import com.sonicle.webtop.core.app.model.SubjectGetOption;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserGetOption;
import com.sonicle.webtop.core.model.Tag;
import com.sonicle.webtop.core.model.UILayout;
import com.sonicle.webtop.core.model.UITheme;
import com.sonicle.webtop.core.products.CustomFieldsProduct;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.EventManager;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.interfaces.IRecipientsProvidersSource;
import com.sonicle.webtop.core.util.ZPushManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.sf.qualitycheck.Check;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class CoreManager extends BaseManager {
	private static final Logger logger = WT.getLogger(CoreManager.class);
	
	public static final String RECIPIENT_PROVIDER_AUTO_SOURCE_ID = "auto";
	public static final String RECIPIENT_PROVIDER_WEBTOP_SOURCE_ID = "webtop";

	private static final EventManager eventManager = new EventManager();
	private WebTopApp wta = null;
	private final HashSet<String> cacheReady = new HashSet<>();
	private final ArrayList<String> cacheAllowedServices = new ArrayList<>();
	private final LinkedHashMap<String, RecipientsProviderBase> cacheProfileRecipientsProvider = new LinkedHashMap<>();
	
	private PbxProvider pbx=null;
	private SmsProvider sms=null;
	
	public final CustomFieldsProduct CUSTOM_FIELD_PRODUCT;
	private final boolean cfieldsLicensed;
	private static final int MAX_CFIELDS_FREE = 6*2/4; // -> 3
	private boolean webtopRcptProviderEnabled = true;
	private boolean autoRcptProviderEnabled = true;
	
	/**
	 * @deprecated use lookupProfilePersonalInfo instead (will be removed in v.5.16.0)
	 */
	@Deprecated
	public UserProfile.PersonalInfo getUserPersonalInfo(UserProfileId pid) throws WTException {
		return lookupProfilePersonalInfo(pid);
	}
	
	/**
	 * @deprecated use lookupUserSid instead (will be removed in v.5.16.0)
	 */
	@Deprecated
	public String getUserUid(UserProfileId profileId) throws WTException {
		return lookupUserSid(profileId);
	}
	
	/**
	 * @deprecated use lookupUserProfileIdBySid instead (will be removed in v.5.16.0)
	 */
	@Deprecated
	public UserProfileId userUidToProfileId(String userUid) {
		try {
			return lookupUserProfileIdBySid(userUid);
		} catch (WTException ex) {
			return null;
		}
	}
	
	public CoreManager(WebTopApp wta, boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		this.wta = wta;
		
		if (targetProfileId != null && !RunContext.isSysAdmin()) {
			CUSTOM_FIELD_PRODUCT = new CustomFieldsProduct(targetProfileId.getDomainId());
			cfieldsLicensed = WT.isLicensed(CUSTOM_FIELD_PRODUCT, targetProfileId.getUserId()) > 0;
			
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, targetProfileId.getDomainId());
			webtopRcptProviderEnabled = css.getRecipientWebTopProviderEnabled();
			autoRcptProviderEnabled = css.getRecipientAutoProviderEnabled();
			
		} else {
			CUSTOM_FIELD_PRODUCT = null;
			cfieldsLicensed = false;
		}
			
		if(!fastInit) {
			//initAllowedServices();
		}
	}
	
	public void addListener(final EventListener listener) {
		eventManager.addListener(listener);
	}
	
	public void removeListener(final EventListener listener) {
		eventManager.removeListener(listener);
	}
	
	/*
	private void initAllowedServices() {
		synchronized(cacheAllowedServices) {
			cacheAllowedServices.addAll(doListAllowedServices());
			cacheReady.add("cacheAllowedServices");
		}
	}
	*/
	
	private void initPbx() {
		if (pbx==null) {
			UserProfileId pid=getTargetProfileId();
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());		
			String provider=css.getPbxProvider();
			if (provider!=null) {
				pbx=PbxProvider.getInstance(provider, pid);
			}
		}
	}

	private void initSms() {
		if (sms==null) {
			UserProfileId pid=getTargetProfileId();
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, pid.getDomainId());		
			String provider=css.getSmsProvider();
			if (provider!=null) {
				sms=SmsProvider.getInstance(getLocale(), provider, pid);
			}
		}
	}

	public ServiceManager getServiceManager() {
		ensureCallerService(SERVICE_ID, "getServiceManager");
		return wta.getServiceManager();
	}
	
	public OTPManager getOTPManager() {
		ensureCallerService(SERVICE_ID, "getOTPManager");
		return wta.getOTPManager();
	}
	
	public final int getCustomFieldsMaxNo() {
		return cfieldsLicensed ? -1 : MAX_CFIELDS_FREE;
	}
	
	public IPLookupResponse getIPGeolocationData(final String ipAddress) throws WTException {
		return wta.getAuditLogManager().getIPGeolocationData(getTargetProfileId().getDomainId(), ipAddress);
	}
	
	public Map<String, UITheme> listUIThemes() throws WTException {
		LinkedHashMap<String, UITheme> themes = new LinkedHashMap<>();
		
		// Add built-in themes
		themes.put("crisp", new UITheme("crisp", "Crisp", true));
		themes.put("triton", new UITheme("triton", "Triton", false));
		themes.put("neptune", new UITheme("neptune", "Neptune", true));
		themes.put("aria", new UITheme("aria", "Aria", false));
		//themes.put("graphite", new UITheme("graphite", "Graphite", false));
		//themes.put("material", new UITheme("material", "Material", false));
		themes.put("classic", new UITheme("classic", "Classic", false));
		themes.put("gray", new UITheme("gray", "Gray", false));
		
		// Then load extra ones...
		//TODO: maybe improve this of dynamic discovery using such sort of file descriptor
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, getTargetProfileId().getDomainId());
		for (Map.Entry<String, String> entry : css.getThemesExtra().entrySet()) {
			final String themeId = entry.getKey();
			if (!themes.containsKey(themeId) && !StringUtils.isBlank(themeId)) {
				final String themeName = StringUtils.defaultIfBlank(entry.getValue(), themeId);
				themes.put(themeId, new UITheme(themeId, themeName, false));
			} else {
				logger.debug("Ignoring extra-theme: invalid ID or already in use [{}]", themeId);
			}
		}
		return themes;
	}
	
	public List<UILayout> listUILayouts() throws WTException {
		return Arrays.asList(
			new UILayout("default", lookupResource(getLocale(), "layout.default")),
			new UILayout("compact", lookupResource(getLocale(), "layout.compact"))
		);
	}
	
	public Map<String, UILookAndFeel> listUILookAndFeels() throws WTException {
		LinkedHashMap<String, UILookAndFeel> lafs = new LinkedHashMap<>();
		
		// Add built-in LAFs
		lafs.put("default", new UILookAndFeel("default", WT.getPlatformName()));
		
		// Then load extra ones...
		//TODO: maybe improve this of dynamic discovery using such sort of file descriptor
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, getTargetProfileId().getDomainId());
		for (Map.Entry<String, String> entry : css.getLAFsExtra().entrySet()) {
			final String lafId = entry.getKey();
			if (!lafs.containsKey(lafId) && !StringUtils.isBlank(lafId)) {
				final String lafName = StringUtils.defaultIfBlank(entry.getValue(), lafId);
				lafs.put(lafId, new UILookAndFeel(lafId, lafName));
			} else {
				logger.debug("Ignoring extra-laf: invalid ID or already in use [{}]", lafId);
			}
		}
		
		return lafs;
	}
	
	/**
	 * Returns target domain's authentication Directory.
	 * @return The authentication Directory
	 * @throws WTException 
	 */
	public AbstractDirectory getAuthDirectory() throws WTException {
		UserProfileId pid = getTargetProfileId();
		// SysAdmin can access all, others are locked on their domains
		if (!RunContext.isSysAdmin()) ensureProfileDomain(pid.getDomainId());
		return wta.getWebTopManager().getAuthDirectory(pid);
	}
	
	public String getAuthDirectoryScheme() throws WTException {
		AbstractDirectory dir = getAuthDirectory();
		return dir != null ? dir.getScheme() : null;
	}
	
	@Deprecated
	public AbstractDirectory getAuthDirectory(String domainId) throws WTException {
		ODomain domain = getDomain(domainId);
		if(domain == null) throw new WTException("Domain not found [{0}]", domainId);
		
		return getAuthDirectory(domain);
	}
	
	@Deprecated
	public AbstractDirectory getAuthDirectory(ODomain domain) throws WTException {
		if (RunContext.isSysAdmin()) {
			return wta.getWebTopManager().getAuthDirectory(domain.getDirUri());
		} else {
			ensureUserDomain(domain.getDomainId());
			return wta.getWebTopManager().getAuthDirectory(domain.getDirUri());
		}
	}
	
	public Set<String> listInstalledServices() {
		ServiceManager svcMgr = wta.getServiceManager();
		return svcMgr.listRegisteredServices();
	}
	
	public List<ServicePermission> listServicePermissions(String serviceId) throws WTException {
		ServiceManager svcMgr = wta.getServiceManager();
		List<ServicePermission> perms = svcMgr.getDeclaredPermissions(serviceId);
		if (perms == null) throw new WTException("Service not found [{0}]", serviceId);
		return perms;
	}
	
	/**
	 * Lists configured domain IDs according to specified options.
	 * @param enabled
	 * @return
	 * @throws WTException 
	 */
	public Set<String> listDomainIds(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		Set<String> domainIds = wtMgr.listDomainIds(enabled);
		if (RunContext.isSysAdmin()) {
			return domainIds;
		} else {
			Set<String> set = new LinkedHashSet<>();
			for (String domainId : domainIds) {
				if (RunContext.isWebTopDomainAdmin(domainId)) {
					set.add(domainId);
					break;
				}
			}
			return set;
		}
	}
	
	public Map<String, Domain> listDomains(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		Map<String, Domain> domains = wtMgr.listDomains(enabled);
		if (RunContext.isSysAdmin()) {
			return domains;
		} else {
			Map<String, Domain> map = new LinkedHashMap<>();
			for (Domain domain : domains.values()) {
				if (RunContext.isWebTopDomainAdmin(domain.getDomainId())) {
					map.put(domain.getDomainId(), domain);
					break;
				}
			}
			return map;
		}
	}
	
	/**
	 * Used by aliseoweb, vfs
	 * @deprecated use listDomains instead
	 */
	@Deprecated
	public List<ODomain> listDomains(boolean enabledOnly) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.OLD_listDomains(enabledOnly);
		} else {
			ODomain domain = wtmgr.OLD_getDomain(RunContext.getRunProfileId().getDomain());
			return domain.getEnabled() ? Arrays.asList(domain) : new ArrayList<>();
		}
	}
	
	//TODO: create new method of a model instance in return
	@Deprecated
	public ODomain getDomain() throws WTException {
		return getDomain(getTargetProfileId().getDomainId());
	}
	
	/**
	 * Used by aliseoweb
	 * @deprecated use getDomain instead
	 */
	@Deprecated
	public ODomain getDomain(String domainId) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if (RunContext.isSysAdmin()) {
			return wtmgr.OLD_getDomain(domainId);
		} else {
			ensureUserDomain(domainId);
			return wtmgr.OLD_getDomain(domainId);
		}
	}
	
	public DomainBase.PasswordPolicies getDomainPasswordPolicies() throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();
		
		// SysAdmin can access all, others are locked on their domains
		if (!RunContext.isSysAdmin()) ensureProfileDomain(domainId);
		return wtMgr.getDomainPasswordPolicies(domainId);
	}
	
	public List<PublicImage> listDomainPublicImages() throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();
		return wtmgr.listDomainPublicImages(domainId);
	}
	
	/**
	 * Lists avaiable resources IDs according to specified options.
	 * @param enabled
	 * @return
	 * @throws WTException 
	 */
	public Set<String> listResourceIds(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		String targetDomainId = getTargetProfileId().getDomainId();
		ensureProfileDomain(targetDomainId);
		return wtMgr.listResourceIds(targetDomainId, enabled);
	}
	
	/**
	 * Lists avaiable resources according to specified options.
	 * @param enabled
	 * @return
	 * @throws WTException 
	 */
	public Map<String, Resource> listResources(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		String targetDomainId = getTargetProfileId().getDomainId();
		ensureProfileDomain(targetDomainId);
		return wtMgr.listResources(targetDomainId, enabled);
	}
	
	/**
	 * Get resource of specified ID.
	 * @param resourceId
	 * @param options
	 * @return
	 * @throws WTException 
	 */
	public Resource getResource(final String resourceId, final BitFlags<ResourceGetOption> options) throws WTException {
		Check.notEmpty(resourceId, "resourceId");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		String targetDomainId = getTargetProfileId().getDomainId();
		ensureProfileDomain(targetDomainId);
		return wtMgr.getResource(targetDomainId, resourceId, options);
	}
	
	/**
	 * Get resource's enables status.
	 * @param resourceId
	 * @return
	 * @throws WTException 
	 */
	public boolean getResourceEnabled(final String resourceId) throws WTException {
		Check.notEmpty(resourceId, "resourceId");
		WebTopManager wtMgr = wta.getWebTopManager();
		
		String targetDomainId = getTargetProfileId().getDomainId();
		ensureProfileDomain(targetDomainId);
		return wtMgr.getResourceEnabled(targetDomainId, resourceId);
	}
	
	/**
	 * Get resource's permission configuration.
	 * @param resourceId
	 * @param subjectsAsSID
	 * @return
	 * @throws WTException 
	 */
	public ResourcePermissions getResourcePermissions(final String resourceId, final boolean subjectsAsSID) throws WTException {
		Check.notEmpty(resourceId, "resourceId");
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();

		RunContext.ensureIsWebTopDomainAdmin(domainId);
		return wtMgr.getResourcePermissions(domainId, resourceId, subjectsAsSID);
	}
	
	/**
	 * Lists domain real roles (those defined as indipendent role).
	 * Target domain ID is taken from manager targetProfile's domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	/*
	public List<Role> listRoles() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		WebTopManager wtmgr = wta.getWebTopManager();
		try {
			return wtmgr.listRoles_OLD(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list roles [{0}]", domainId);
		}
	}
	*/
	
	/**
	 * Lists domain users roles (those coming from a user).
	 * Target domain ID is taken from manager targetProfile's domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	/*
	public List<Role> listUsersRoles() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		WebTopManager wtmgr = wta.getWebTopManager();
		try {
			return wtmgr.listUsersRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list users roles [{0}]", domainId);
		}
	}
	*/
	
	/**
	 * Lists domain groups roles (those coming from a group).
	 * Target domain ID is taken from manager targetProfile's domain ID.
	 * @return The role list.
	 * @throws WTException If something go wrong.
	 */
	/*
	public List<Role> listGroupsRoles() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		
		//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
		ensureUserDomain(domainId);
		
		WebTopManager wtmgr = wta.getWebTopManager();
		try {
			return wtmgr.listGroupsRoles(domainId);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups roles [{0}]", domainId);
		}
	}
	*/
	
	public Map<String, GenericSubject> listSubjects(final boolean users, final boolean resources, final boolean groups, final boolean roles, final boolean useProfileIdAsKey) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();
		
		ensureProfileDomain();
		BitFlags<SubjectGetOption> options = new BitFlags<>(SubjectGetOption.class);
		if (users) options.set(SubjectGetOption.USERS);
		if (resources) options.set(SubjectGetOption.RESOURCES);
		if (groups) options.set(SubjectGetOption.GROUPS);
		if (roles) options.set(SubjectGetOption.ROLES);
		if (useProfileIdAsKey) options.set(SubjectGetOption.PID_AS_KEY);
		return wtMgr.listSubjects(domainId, options);
	}
	
	/**
	 * Used in aliseoweb, drm
	 * @deprecated use listSubjects, listUserIds, listUserProfileIds or listUsers instead
	 */
	@Deprecated
	public List<OUser> listUsers(boolean enabledOnly) throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		WebTopManager wtmgr = wta.getWebTopManager();
		
		try {
			return wtmgr.listUsers(domainId, enabledOnly);
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list users [{0}]", domainId);
		}
	}
	
	public List<UserProfileId> listUserIdsByEmail(String emailAddress) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		return wtmgr.listUserProfileIdsByEmail(emailAddress);
	}
	
	public Set<String> listUserIds(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();
		
		ensureProfileDomain(domainId);
		return wtMgr.listUserIds(domainId, enabled);
	}
	
	public Set<UserProfileId> listUserProfileIds(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();
		
		ensureProfileDomain(domainId);
		return wtMgr.listUserIds(domainId, enabled)
			.stream()
			.map((userId) -> new UserProfileId(domainId, userId))
			.collect(Collectors.toSet());
	}
	
	public Map<String, User> listUsers(final EnabledCond enabled) throws WTException {
		Check.notNull(enabled, "enabled");
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();
		
		ensureProfileDomain(domainId);
		return wtMgr.listUsers(domainId, enabled);
	}
	
	public User getUser(final BitFlags<UserGetOption> options) throws WTException {
		Check.notNull(options, "options");
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId pid = getTargetProfileId();
		
		ensureProfileDomain(pid.getDomainId());
		return wtMgr.getUser(pid.getDomainId(), pid.getUserId(), options);
	}
	
	public Set<UserProfileId> expandSubjectsToUserProfiles(final Collection<String> subjects, final boolean subjectsAsSID) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		String domainId = getTargetProfileId().getDomainId();
		
		RunContext.ensureIsWebTopDomainAdmin(domainId);
		return wtMgr.expandSubjectsToUserProfiles(domainId, subjects, subjectsAsSID);
	}
	
	/**
	 * Used by aliseoweb service
	 * @deprecated 
	 */
	@Deprecated
	public OUser getUser(UserProfileId pid) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.getUser(pid);
		} else {
			//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
			ensureUserDomain(pid.getDomainId());
			return wtmgr.getUser(pid);
		}
	}
	
	/**
	 * Returns current target profile Data.
	 * @return
	 * @throws WTException 
	 */
	public UserProfile.Data getUserData() throws WTException {
		return wta.getWebTopManager().lookupProfileData(getTargetProfileId(), true);
	}
	
	/**
	 * Returns current target profile SID.
	 * @return
	 * @throws WTException 
	 */
	public String getUserSid() throws WTException {
		return lookupUserSid(getTargetProfileId());
	}
	
	/**
	 * Returns the SID associated to passed profile ID of a user.
	 * @param profileId The target profile ID.
	 * @return
	 * @throws WTException 
	 */
	public String lookupUserSid(final UserProfileId profileId) throws WTException {
		Check.notNull(profileId, "profileId");
		if (!RunContext.isSysAdmin()) ensureUserDomain(profileId.getDomainId());
		return wta.getWebTopManager().lookupSubjectSid(profileId, GenericSubject.Type.USER);
	}
	
	/**
	 * Returns the profile ID related to the passed SID of a user.
	 * @param userSid The target SID.
	 * @return
	 * @throws WTException 
	 */
	public UserProfileId lookupUserProfileIdBySid(final String userSid) throws WTException {
		Check.notNull(userSid, "userSid");
		UserProfileId pid = wta.getWebTopManager().lookupSubjectProfile(userSid, GenericSubject.Type.USER);
		if (pid != null) ensureUserDomain(pid.getDomainId());
		return pid;
	}
	
	public UserProfile.PersonalInfo getProfilePersonalInfo() throws WTException {
		return lookupProfilePersonalInfo(getTargetProfileId());
	}
	
	public UserProfile.PersonalInfo lookupProfilePersonalInfo(final UserProfileId profileId) throws WTException {
		if (!RunContext.isSysAdmin()) ensureUserDomain(profileId.getDomainId());
		return wta.getWebTopManager().lookupProfilePersonalInfo(profileId, true);
	}
	
	public boolean updateUserDisplayName(String displayName) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.updateUserDisplayName(getTargetProfileId(), displayName);
		} else {
			//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
			ensureUser();
			return wtmgr.updateUserDisplayName(getTargetProfileId(), displayName);
		}
	}
	
	public boolean updateUserPersonalInfo(UserProfile.PersonalInfo userPersonalInfo) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		
		if(RunContext.isSysAdmin()) {
			return wtmgr.updateUserPersonalInfo(getTargetProfileId(), userPersonalInfo);
		} else {
			//TODO: permettere la chiamata per l'admin di dominio (admin@dominio)
			ensureProfile();
			return wtmgr.updateUserPersonalInfo(getTargetProfileId(), userPersonalInfo);
		}
	}
	
	public void updateUserPassword(char[] oldPassword, char[] newPassword) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		
		try {
			ensureProfile();
			if (oldPassword == null) throw new WTException("Old password must be provided");
			wtMgr.updateUserPassword(getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(), oldPassword, newPassword);
			
		} catch (Exception ex) {
			throw new WTException(ex, "Unable to change user password [{0}]", getTargetProfileId().toString());
		}
	}
	
	public void cleanUserProfileCache() {
		ensureCallerService(SERVICE_ID, "cleanupUserProfileCache");
		wta.getWebTopManager().clearProfileCache(getTargetProfileId());
	}
	
	/**
	 * @deprecated
	 */
	@Deprecated
	public List<OGroup> listGroups() throws WTException {
		String domainId = getTargetProfileId().getDomainId();
		WebTopManager wtmgr = wta.getWebTopManager();
		
		try {
			ArrayList<OGroup> items = new ArrayList<>();
			for (Group group : wtmgr.listGroups(domainId).values()) {
				OGroup ogroup = new OGroup();
				ogroup.setGroupId(group.getGroupId());
				ogroup.setDisplayName(group.getGroupId());
				items.add(ogroup);
			}
			return items;
		} catch(Exception ex) {
			throw new WTException(ex, "Unable to list groups [{0}]", domainId);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Set<String> listAllowedServices() {
		LinkedHashSet<String> ids = new LinkedHashSet<>();
		
		UserProfileId targetPid = getTargetProfileId();
		ServiceManager svcm = wta.getServiceManager();
		for (String id : svcm.listRegisteredServices()) {
			if (RunContext.isPermitted(true, targetPid, SERVICE_ID, "SERVICE", "ACCESS", id)) ids.add(id);
		}
		return ids;
	}
	
	/**
	 * Returns UserOption services data for current target user.
	 */
	public List<UserOptionsServiceData> getAllowedUserOptionServices() {
		ArrayList<UserOptionsServiceData> items = new ArrayList<>();
		
		UserProfileId targetPid = getTargetProfileId();
		ServiceManager svcm = wta.getServiceManager();
		for (String serviceId : svcm.listUserOptionServices()) {
			if (RunContext.isPermitted(true, targetPid, SERVICE_ID, "SERVICE", "ACCESS", serviceId)) {
				UserOptionsServiceData uosd = new UserOptionsServiceData(svcm.getManifest(serviceId));
				uosd.name = wta.lookupResource(serviceId, getLocale(), CoreLocaleKey.SERVICE_NAME);
				items.add(uosd);
			}
		}
		
		return items;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

	
	
	
	private void buildProfileRecipientsProviderCache() {
		for(String serviceId : listAllowedServices()) {
			BaseManager manager = WT.getServiceManager(serviceId, true, getTargetProfileId());
			if(manager instanceof IRecipientsProvidersSource) {
				List<RecipientsProviderBase> providers = ((IRecipientsProvidersSource)manager).returnRecipientsProviders();
				if(providers == null) continue;
				
				for(RecipientsProviderBase provider : providers) {
					final CompositeId cid = new CompositeId().setTokens(serviceId, provider.getId());
					cacheProfileRecipientsProvider.put(cid.toString(), provider);
				}
			}
		}
	}
	
	private LinkedHashMap<String, RecipientsProviderBase> getProfileRecipientsProviders() {
		synchronized(cacheProfileRecipientsProvider) {
			if(!cacheReady.contains("cacheProfileRecipientsProvider")) {
				buildProfileRecipientsProviderCache();
				cacheReady.add("cacheProfileRecipientsProvider");
			}
			return cacheProfileRecipientsProvider;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	/*
	public String getUserCompleteEmailAddress(UserProfileId pid) throws Exception {
		String address = getUserEmailAddress(pid);
		String displayName = getUserDisplayName(pid);
		return new InternetAddress(address, displayName).toUnicodeString();
	}
	*/
	
	public List<UserProfileId> listProfilesWithSetting(String serviceId, String key, Object value) throws WTException {
		SettingsManager setm = wta.getSettingsManager();
		return setm.listProfilesWith(serviceId, key, value);
	}
	
	public List<Activity> listAllLiveActivities() throws WTException {
		ActivityDAO actDao = ActivityDAO.getInstance();
		ArrayList<Activity> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			for(OActivity oact : actDao.selectLiveByDomain(con, getTargetProfileId().getDomainId())) {
				items.add(ManagerUtils.createActivity(oact));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<Activity> listLiveActivities() throws WTException {
		ActivityDAO actDao = ActivityDAO.getInstance();
		ArrayList<Activity> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			for(OActivity oact : actDao.selectLiveByDomainUser(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId())) {
				items.add(ManagerUtils.createActivity(oact));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Activity getActivity(int activityId) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			OActivity oact = dao.select(con, activityId);
			return ManagerUtils.createActivity(oact);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Activity addActivity(Activity activity) throws WTException {
		Connection con = null;
		
		try {
			ensureProfileDomain(activity.getDomainId());
			RunContext.ensureIsPermitted(false, SERVICE_ID, "ACTIVITIES", "MANAGE");
			
			con = WT.getCoreConnection();
			Activity ret = doActivityUpdate(true, con, activity);
			
			if (isAuditEnabled()) {
				auditLogWrite(
					AuditContext.ACTIVITY,
					AuditAction.CREATE,
					ret.getActivityId(),
					JsonUtils.toJson("description", ret.getDescription())
				);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Activity updateActivity(Activity activity) throws WTException {
		Connection con = null;
		
		try {
			ensureProfileDomain(activity.getDomainId());
			RunContext.ensureIsPermitted(false, SERVICE_ID, "ACTIVITIES", "MANAGE");
			
			con = WT.getCoreConnection();
			Activity ret = doActivityUpdate(false, con, activity);
			if (ret == null) throw new WTNotFoundException("Activity not found [{}]", activity.getActivityId());
			
			if (isAuditEnabled()) {
				auditLogWrite(
					AuditContext.ACTIVITY,
					AuditAction.UPDATE,
					ret.getActivityId(),
					JsonUtils.toJson("description", ret.getDescription())
				);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean deleteActivity(int activityId) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		Connection con = null;
		
		try {
			Activity act = getActivity(activityId);
			if (act == null) throw new WTNotFoundException("Activity not found [{}]", activityId);
			ensureProfileDomain(act.getDomainId());
			RunContext.ensureIsPermitted(false, SERVICE_ID, "ACTIVITIES", "MANAGE");
			
			con = WT.getCoreConnection();
			boolean ret = dao.logicDelete(con, activityId) == 1;
			if (!ret) throw new WTNotFoundException("Activity not found [{}]", activityId);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.ACTIVITY, AuditAction.DELETE, activityId, null);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<CausalExt> listAllLiveCausals() throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		ArrayList<CausalExt> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			List<VCausal> vcaus = dao.viewLiveByDomain(con, getTargetProfileId().getDomainId());
			for(VCausal vcai : vcaus) {
				items.add(createCausalExt(vcai));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<Causal> listLiveCausals(String masterDataId) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		ArrayList<Causal> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			List<OCausal> ocaus = null;
			if (!StringUtils.isBlank(masterDataId)) {
				ocaus = dao.selectLiveByDomainUserMasterData(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId(), masterDataId);
			} else {
				ocaus = dao.selectLiveByDomainUser(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			}
			for(OCausal ocau : ocaus) {
				items.add(ManagerUtils.createCausal(ocau));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Causal getCausal(int causalId) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			OCausal ocal = dao.select(con, causalId);
			return ManagerUtils.createCausal(ocal);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Causal addCausal(Causal causal) throws WTException {
		Connection con = null;
		
		try {
			ensureProfileDomain(causal.getDomainId());
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CAUSALS", "MANAGE");
			
			con = WT.getCoreConnection();
			Causal ret = doCausalUpdate(true, con, causal);
			
			if (isAuditEnabled()) {
				auditLogWrite(
					AuditContext.CAUSAL,
					AuditAction.CREATE,
					ret.getCausalId(),
					JsonUtils.toJson("description", ret.getDescription())
				);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Causal updateCausal(Causal causal) throws WTException {
		Connection con = null;
		
		try {
			ensureProfileDomain(causal.getDomainId());
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CAUSALS", "MANAGE");
			
			con = WT.getCoreConnection();
			Causal ret = doCausalUpdate(false, con, causal);
			if (ret == null) throw new WTNotFoundException("Causal not found [{}]", causal.getCausalId());
			
			if (isAuditEnabled()) {
				auditLogWrite(
					AuditContext.CAUSAL,
					AuditAction.UPDATE,
					ret.getCausalId(),
					JsonUtils.toJson("description", ret.getDescription())
				);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean deleteCausal(int causalId) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		Connection con = null;
		
		try {
			Causal cau = getCausal(causalId);
			if (cau == null) throw new WTNotFoundException("Causal not found [{}]", causalId);
			ensureProfileDomain(cau.getDomainId());
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CAUSALS", "MANAGE");
			
			con = WT.getCoreConnection();
			boolean ret = dao.logicDelete(con, causalId) == 1;
			if (!ret) throw new WTNotFoundException("Causal not found [{}]", causalId);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CAUSAL, AuditAction.DELETE, causalId, null);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, MasterDataLookup> lookupMasterData(Collection<String> masterDataIds) throws WTException {
		MasterDataDAO masDao = MasterDataDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			LinkedHashMap<String, MasterDataLookup> items = new LinkedHashMap<>();
			for (OMasterData omd : masDao.viewByDomainIn(con, getTargetProfileId().getDomainId(), masterDataIds)) {
				items.put(omd.getMasterDataId(), ManagerUtils.createMasterDataLookup(omd));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, MasterData> listMasterDataIn(Collection<String> masterDataIds) throws WTException {
		MasterDataDAO masDao = MasterDataDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			LinkedHashMap<String, MasterData> items = new LinkedHashMap<>();
			for (OMasterData omd : masDao.viewByIdsDomain(con, masterDataIds, getTargetProfileId().getDomainId())) {
				items.put(omd.getMasterDataId(), ManagerUtils.createMasterData(omd));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, MasterData> listMasterData(Collection<String> masterDataTypes) throws WTException {
		return listMasterData(masterDataTypes, null);
	}
	
	public Map<String, MasterData> listMasterData(Collection<String> masterDataTypes, String pattern) throws WTException {
		MasterDataDAO masDao = MasterDataDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			LinkedHashMap<String, MasterData> items = new LinkedHashMap<>();
			for (OMasterData omd : masDao.viewParentsByDomainTypePattern(con, getTargetProfileId().getDomainId(), masterDataTypes, pattern)) {
				items.put(omd.getMasterDataId(), ManagerUtils.createMasterData(omd));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, MasterData> listChildrenMasterData(Collection<String> masterDataTypes) throws WTException {
		MasterDataDAO masDao = MasterDataDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			LinkedHashMap<String, MasterData> items = new LinkedHashMap<>();
			for (OMasterData omd : masDao.viewChildrenByDomainType(con, getTargetProfileId().getDomainId(), masterDataTypes)) {
				items.put(omd.getMasterDataId(), ManagerUtils.createMasterData(omd));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<MasterData> listChildrenMasterData(String parentId, Collection<String> masterDataTypes) throws WTException {
		return listChildrenMasterData(parentId, masterDataTypes, null);
	}
	
	public List<MasterData> listChildrenMasterData(String parentId, Collection<String> masterDataTypes, String pattern) throws WTException {
		MasterDataDAO masDao = MasterDataDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			ArrayList<MasterData> items = new ArrayList<>();
			for (OMasterData omas : masDao.viewChildrenByDomainParentTypePattern(con, getTargetProfileId().getDomainId(), parentId, masterDataTypes, pattern)) {
				items.add(ManagerUtils.createMasterData(omas));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public MasterData getMasterData(String masterDataId) throws WTException {
		MasterDataDAO masDao = MasterDataDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			OMasterData omas = masDao.selectByDomainId(con, getTargetProfileId().getDomainId(), masterDataId);
			return ManagerUtils.createMasterData(omas);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Set<String> listTagIds() throws WTException {
		return listTagIds(ListTagsOpt.ALL);
	}
	
	public Set<String> listTagIds(final EnumSet<ListTagsOpt> options) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			return tagDao.selectIdsByDomainOwners(con, targetDomainId, tagOptionsToUserIds(options));
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, List<String>> listTagIdsByName() throws WTException {
		return listTagIdsByName(ListTagsOpt.ALL);
	}
	
	public Map<String, List<String>> listTagIdsByName(final EnumSet<ListTagsOpt> options) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			return tagDao.groupIdsByDomainOwners(con, targetDomainId, tagOptionsToUserIds(options));
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Map<String, String> listTagNamesById() throws WTException {
		return listTagNamesById(ListTagsOpt.ALL);
	}
	
	public Map<String, String> listTagNamesById(final EnumSet<ListTagsOpt> options) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			return tagDao.mapNamesByDomainOwners(con, targetDomainId, tagOptionsToUserIds(options));
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private Collection<String> tagOptionsToUserIds(EnumSet<ListTagsOpt> options) {
		ArrayList<String> owners = new ArrayList<>();
		if (options.contains(ListTagsOpt.SHARED)) {
			owners.add(OTag.OWNER_NONE);
		}
		if (options.contains(ListTagsOpt.PRIVATE)) {
			owners.add(getTargetProfileId().getUserId());
		}
		return owners;
	}
	
	public Map<String, Tag> listTags() throws WTException {
		return listTags(ListTagsOpt.ALL);
	}
	
	public Map<String, Tag> listTags(final EnumSet<ListTagsOpt> options) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			LinkedHashMap<String, Tag> items = new LinkedHashMap<>();
			for (OTag otag : tagDao.selectByDomainOwners(con, targetDomainId, tagOptionsToUserIds(options)).values()) {
				items.put(otag.getTagId(), ManagerUtils.fillTag(new Tag(), otag));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Tag getTag(final String tagId) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			OTag otag = tagDao.selectByDomainTag(con, targetDomainId, tagId);
			return otag == null ? null : ManagerUtils.fillTag(new Tag(), otag);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Tag addTag(final Tag tag) throws WTException {
		Connection con = null;
		
		try {
			// We just want to make sure alerting external code that domainId, if present, is consistent!
			if (tag.getDomainId() != null) ensureTargetProfileDomain(tag.getDomainId());
			tag.setDomainId(getTargetProfileId().getDomainId());
			tag.setBuiltIn(false);
			
			ensureProfileDomain(tag.getDomainId());
			if (!Tag.Visibility.PRIVATE.equals(tag.getVisibility())) {
				RunContext.ensureIsPermitted(false, SERVICE_ID, "TAGS", "MANAGE");
			}
			
			con = WT.getConnection(SERVICE_ID);
			Tag ret = doTagUpdate(true, con, tag);
			
			eventManager.fireEvent(new TagChangedEvent(this, ChangedEvent.Operation.CREATE));
			HashMap<String, String> tagDetails = new HashMap<>();
			tagDetails.put("description", ret.getName());
			tagDetails.put("color", ret.getColor());
			
			if (isAuditEnabled()) {
				auditLogWrite(
					AuditContext.TAG,
					AuditAction.CREATE,
					ret.getTagId(),
					JsonResult.gson().toJson(tagDetails)
				);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public Tag updateTag(final Tag tag) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		try {
			// We just want to make sure alerting external code that domainId, if present, is consistent!
			if (tag.getDomainId() != null) ensureTargetProfileDomain(tag.getDomainId());
			tag.setDomainId(getTargetProfileId().getDomainId());
			
			ensureProfileDomain(tag.getDomainId());
			
			con = WT.getConnection(SERVICE_ID);
			String oldOwnerId = tagDao.selectOwnerByDomainTag(con, tag.getDomainId(), tag.getTagId());
			if (OTag.isOwnerNone(oldOwnerId)) {
				RunContext.ensureIsPermitted(false, SERVICE_ID, "TAGS", "MANAGE");
			}
			
			if (OTag.isOwnerNone(oldOwnerId) && Tag.Visibility.PRIVATE.equals(tag.getVisibility())) {
				throw new WTException("Public tag '{}' cannot become private", tag.getTagId());
			}
			
			Tag ret = doTagUpdate(false, con, tag);
			if (ret == null) throw new WTNotFoundException("Tag not found [{}]", tag.getTagId());
			
			eventManager.fireEvent(new TagChangedEvent(this, ChangedEvent.Operation.UPDATE));
			HashMap<String, String> tagDetails = new HashMap<>();
			tagDetails.put("description", ret.getName());
			tagDetails.put("color", ret.getColor());
			
			if (isAuditEnabled()) {
				auditLogWrite(
					AuditContext.TAG,
					AuditAction.UPDATE,
					ret.getTagId(),
					JsonResult.gson().toJson(tagDetails)
				);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteTag(final String tagId) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		try {	
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			String oldOwnerId = tagDao.selectOwnerByDomainTag(con, targetDomainId, tagId);
			if (OTag.isOwnerNone(oldOwnerId)) {
				RunContext.ensureIsPermitted(false, SERVICE_ID, "TAGS", "MANAGE");
			}
			
			boolean ret = doTagDelete(con, targetDomainId, tagId);
			if (!ret) throw new WTNotFoundException("Tag not found [{}]", tagId);
			
			eventManager.fireEvent(new TagChangedEvent(this, ChangedEvent.Operation.DELETE));
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.TAG, AuditAction.DELETE, tagId, null);
			}
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public HashMap<String,List<String>> compareTags(List<String> oldTags, List<String> newTags) {
		HashMap<String, List<String>> audit = new HashMap<>();
		
		if (!oldTags.isEmpty() && !newTags.isEmpty()) {
			for (int i = 0; i < oldTags.size(); i++) {
				int index = newTags.indexOf(oldTags.get(i));
				if (index > -1) {
					oldTags.remove(i);
					newTags.remove(index);
					i--;
				}
			}
		}

		if (!newTags.isEmpty()) audit.put("set", newTags);
		if (!oldTags.isEmpty()) audit.put("unset", oldTags);
		
		return audit;
	}
	
	/**
	 * Lists all CustomPanels of specified Service.
	 * @param serviceId The owning Service ID.
	 * @return A map of panels by their IDs.
	 * @throws WTException 
	 */
	public Map<String, CustomPanel> listCustomPanels(final String serviceId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		CustomPanelDAO cupDao = CustomPanelDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			LinkedHashMap<String, CustomPanel> items = new LinkedHashMap<>();
			for (VCustomPanel vcpanel : cupDao.viewByDomainService(con, targetDomainId, serviceId).values()) {
				Set<String> fields = new LinkedHashSet(new CompositeId().parse(vcpanel.getCustomFieldIds()).getTokens());
				Set<String> tags = new LinkedHashSet(new CompositeId().parse(vcpanel.getTagIds()).getTokens());
				items.put(vcpanel.getCustomPanelId(), ManagerUtils.createCustomPanel(vcpanel, fields, tags));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Lists all CustomPanels of a Service used-by the specified set of Tags.
	 * @param serviceId The owning Service ID.
	 * @param tagIds A collection of Tag IDs.
	 * @return A map of panels by their IDs.
	 * @throws WTException 
	 */
	public Map<String, CustomPanel> listCustomPanelsUsedBy(final String serviceId, final Collection<String> tagIds) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notNull(tagIds, "tagIds");
		CustomPanelDAO cupDao = CustomPanelDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			LinkedHashMap<String, CustomPanel> items = new LinkedHashMap<>();
			for (VCustomPanel vcpanel : cupDao.viewUsedByDomainServiceTags(con, targetDomainId, serviceId, tagIds, null, null, getCustomFieldsMaxNo()).values()) {
				Set<String> fields = new LinkedHashSet(new CompositeId().parse(vcpanel.getCustomFieldIds()).getTokens());
				Set<String> tags = new LinkedHashSet(new CompositeId().parse(vcpanel.getTagIds()).getTokens());
				items.put(vcpanel.getCustomPanelId(), ManagerUtils.createCustomPanel(vcpanel, fields, tags));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Gets the CustomPanel of the specified ID.
	 * @param serviceId The owning Service ID.
	 * @param panelId The CustomPanel ID.
	 * @return CustomPanel object or null if not found.
	 * @throws WTException 
	 */
	public CustomPanel getCustomPanel(final String serviceId, final String panelId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(panelId, "panelId");
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			return doCustomPanelGet(con, targetDomainId, serviceId, panelId);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Add new CustomPanel for the specified Service.
	 * @param serviceId The owning Service ID.
	 * @param customPanel Data to set.
	 * @return The added CustomPanel.
	 * @throws WTException 
	 */
	public CustomPanel addCustomPanel(final String serviceId, final CustomPanelBase customPanel) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notNull(customPanel, "customPanel");
		Connection con = null;
		
		try {
			String domainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(domainId);
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CUSTOM_FIELDS", "MANAGE");
			
			con = WT.getCoreConnection();
			CustomPanel ret = doCustomPanelUpdate(con, domainId, serviceId, null, customPanel);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CUSTOMPANEL, AuditAction.CREATE, new CompositeId(ret.getServiceId(), ret.getPanelId()).toString(), null);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Updates specified CustomPanel.
	 * @param serviceId The owning Service ID.
	 * @param panelId The CustomPanel ID.
	 * @param customPanel New data to set.
	 * @return The updated CustomPanel.
	 * @throws WTException 
	 */
	public CustomPanel updateCustomPanel(final String serviceId, final String panelId, final CustomPanelBase customPanel) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(panelId, "panelId");
		Connection con = null;
		
		try {
			String domainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(domainId);
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CUSTOM_FIELDS", "MANAGE");
			
			con = WT.getCoreConnection();
			CustomPanel ret = doCustomPanelUpdate(con, domainId, serviceId, panelId, customPanel);
			if (ret == null) throw new WTNotFoundException("Custom-panel not found [{}, {}]", serviceId, panelId);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CUSTOMPANEL, AuditAction.UPDATE, new CompositeId(ret.getServiceId(), ret.getPanelId()).toString(), null);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Updates specified CustomPanel's ordering number of the provided one.
	 * @param serviceId The owning Service ID.
	 * @param panelId The CustomPanel ID.
	 * @param newOrder New order position.
	 * @throws WTException 
	 */
	public void updateCustomPanelOrder(final String serviceId, final String panelId, final short newOrder) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(panelId, "panelId");
		CustomPanelDAO cupDao = CustomPanelDAO.getInstance();
		Connection con = null;
		
		try {	
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CUSTOM_FIELDS", "MANAGE");
			
			con = WT.getCoreConnection();
			boolean ret = cupDao.updateOrder(con, targetDomainId, serviceId, panelId, newOrder) == 1;
			if (!ret) throw new WTNotFoundException("Custom-panel not found [{}, {}]", serviceId, panelId);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CUSTOMPANEL, AuditAction.UPDATE, new CompositeId(serviceId, panelId).toString(), null);
			}
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Deletes the specified CustomPanel, removing any associations of CustomField(s).
	 * @param serviceId The owning Service ID.
	 * @param panelId The CustomPanel ID.
	 * @throws WTException 
	 */
	public void deleteCustomPanel(final String serviceId, final String panelId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(panelId, "panelId");
		CustomPanelDAO cupDao = CustomPanelDAO.getInstance();
		Connection con = null;
		
		try {	
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CUSTOM_FIELDS", "MANAGE");
			
			con = WT.getCoreConnection();
			boolean ret = cupDao.deleteByDomainServicePanel(con, targetDomainId, serviceId, panelId) == 1;
			if (!ret) throw new WTNotFoundException("Custom-panel not found [{}, {}]", serviceId, panelId);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CUSTOMPANEL, AuditAction.DELETE, new CompositeId(serviceId, panelId).toString(), null);
			}
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Returns the list of CustomField types related to the specified Service.
	 * @param serviceId The owning Service ID.
	 * @return Map of fields by their IDs.
	 * @throws WTException 
	 */
	public Map<String, CustomField.Type> listCustomFieldTypesById(final String serviceId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		return listCustomFieldTypesById(serviceId, BitFlag.none());
	}
	
	/**
	 * Returns the list of CustomField types related to the specified Service.
	 * @param serviceId The owning Service ID.
	 * @param options Listing options.
	 * @return Map of fields types by field IDs.
	 * @throws WTException 
	 */
	public Map<String, CustomField.Type> listCustomFieldTypesById(final String serviceId, final BitFlag<CustomFieldListOptions> options) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notNull(options, "options");
		CustomFieldDAO cufDao = CustomFieldDAO.getInstance();
		Connection con = null;
		
		try {
			if (options.has(CustomFieldListOptions.PREVIEWABLE)) throw new IllegalArgumentException("Option PREVIEWABLE is not supported here");
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			Boolean searchable = options.has(CustomFieldListOptions.SEARCHABLE) ? true : null;
			LinkedHashMap<String, CustomField.Type> items = new LinkedHashMap<>();
			for (Map.Entry<String, String> entry : cufDao.viewOnlineTypeByDomainServiceSearchable(con, targetDomainId, serviceId, searchable).entrySet()) {
				CustomField.Type type = EnumUtils.forSerializedName(entry.getValue(), CustomField.Type.class);
				if (type != null) items.put(entry.getKey(), type);
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Returns the list of CustomFields related to the specified Service.
	 * @param serviceId The owning Service ID.
	 * @return Map of fields by field IDs.
	 * @throws WTException 
	 */
	public Map<String, CustomFieldEx> listCustomFields(final String serviceId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		return listCustomFields(serviceId, BitFlag.none());
	}
	
	/**
	 * Returns the list of CustomFields related to the specified Service.
	 * @param serviceId The owning Service ID.
	 * @param options Listing options.
	 * @return Map of fields by field IDs.
	 * @throws WTException 
	 */
	public Map<String, CustomFieldEx> listCustomFields(final String serviceId, final BitFlag<CustomFieldListOptions> options) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notNull(options, "options");
		CustomFieldDAO cufDao = CustomFieldDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			Boolean searchable = options.has(CustomFieldListOptions.SEARCHABLE) ? true : null;
			Boolean previewable = options.has(CustomFieldListOptions.PREVIEWABLE) ? true : null;
			LinkedHashMap<String, CustomFieldEx> items = new LinkedHashMap<>();
			for (VCustomField vcfield : cufDao.viewOnlineByDomainServiceSearchablePreviewable(con, targetDomainId, serviceId, searchable, previewable, getCustomFieldsMaxNo()).values()) {
				//items.put(vcfield.getCustomFieldId(), ManagerUtils.createCustomField(vcfield));
				items.put(vcfield.getCustomFieldId(), ManagerUtils.fillCustomFieldEx(new CustomFieldEx(), vcfield));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Returns the list of CustomField IDs related to the specified Service.
	 * @param serviceId The owning Service ID.
	 * @param options Listing options.
	 * @return Set of CustomField IDs.
	 * @throws WTException 
	 */
	public Set<String> listCustomFieldIds(final String serviceId, final BitFlag<CustomFieldListOptions> options) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notNull(options, "options");
		CustomFieldDAO cufDao = CustomFieldDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			Boolean searchable = options.has(CustomFieldListOptions.SEARCHABLE) ? true : null;
			Boolean previewable = options.has(CustomFieldListOptions.PREVIEWABLE) ? true : null;
			return cufDao.viewOnlineIdsByDomainServiceSearchablePreviewable(con, targetDomainId, serviceId, searchable, previewable, getCustomFieldsMaxNo());
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Gets the CustomField of the specified ID.
	 * @param serviceId The owning Service ID.
	 * @param fieldId The CustomField ID.
	 * @return CustomField object or null if not found.
	 * @throws WTException 
	 */
	public CustomField getCustomField(final String serviceId, final String fieldId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(fieldId, "fieldId");
		CustomFieldDAO cufDao = CustomFieldDAO.getInstance();
		Connection con = null;
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			con = WT.getConnection(SERVICE_ID);
			OCustomField ofield = cufDao.selectByDomainService(con, targetDomainId, serviceId, fieldId);
			return ofield == null ? null : ManagerUtils.fillCustomField(new CustomField(), ofield);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Checks if provided CustomField name is already taken (unavailable) or not.
	 * @param serviceId The owning Service ID.
	 * @param name The name to check.
	 * @return `true` if name is available for usage
	 * @throws WTException 
	 */
	public boolean checkCustomFieldNameAvailability(final String serviceId, final String name) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(name, "name");
		CustomFieldDAO cufDao = CustomFieldDAO.getInstance();
		Connection con = null;
		
		try {
			String domainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(domainId);
			
			con = WT.getCoreConnection();
			return cufDao.nameIsAvailableByDomainService(con, domainId, serviceId, name);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Add new CustomField for the specified Service.
	 * @param serviceId The owning Service ID.
	 * @param customField Data to set.
	 * @return The added CustomField.
	 * @throws WTException 
	 */
	public CustomField addCustomField(final String serviceId, final CustomFieldBase customField) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notNull(customField, "customField");
		Connection con = null;
		
		try {
			String domainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(domainId);
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CUSTOM_FIELDS", "MANAGE");
			
			con = WT.getCoreConnection();
			CustomField ret = doCustomFieldUpdate(con, domainId, serviceId, null, customField);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CUSTOMFIELD, AuditAction.CREATE, new CompositeId(ret.getServiceId(), ret.getFieldId()).toString(), null);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Updates specified CustomField.
	 * @param serviceId The owning Service ID.
	 * @param fieldId The CustomField ID.
	 * @param customField New data to set.
	 * @return The updated CustomField.
	 * @throws WTException 
	 */
	public CustomField updateCustomField(final String serviceId, final String fieldId, final CustomFieldBase customField) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(fieldId, "fieldId");
		Check.notNull(customField, "customField");
		Connection con = null;
		
		try {
			String domainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(domainId);
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CUSTOM_FIELDS", "MANAGE");
			
			con = WT.getCoreConnection();
			CustomField ret = doCustomFieldUpdate(con, domainId, serviceId, fieldId, customField);
			if (ret == null) throw new WTNotFoundException("Custom-field not found [{}, {}]", serviceId, fieldId);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CUSTOMFIELD, AuditAction.UPDATE, new CompositeId(ret.getServiceId(), ret.getFieldId()).toString(), null);
			}
			
			return ret;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Deletes the specified CustomField, removing associations of any CustomPanel(s).
	 * @param serviceId The owning Service ID.
	 * @param fieldId The CustomField ID.
	 * @throws WTException 
	 */
	public void deleteCustomField(final String serviceId, final String fieldId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(fieldId, "fieldId");
		CustomFieldDAO cufDao = CustomFieldDAO.getInstance();
		CustomPanelFieldDAO cupfDao = CustomPanelFieldDAO.getInstance();
		Connection con = null;
		
		try {	
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			RunContext.ensureIsPermitted(false, SERVICE_ID, "CUSTOM_FIELDS", "MANAGE");
			
			con = WT.getCoreConnection(false);
			boolean ret = cufDao.logicDeleteByDomainServiceId(con, targetDomainId, serviceId, fieldId, BaseDAO.createRevisionTimestamp()) == 1;
			if (!ret) throw new WTNotFoundException("Custom-field not found [{}, {}]", serviceId, fieldId);
			cupfDao.deleteByField(con, fieldId);
			
			if (isAuditEnabled()) {
				auditLogWrite(AuditContext.CUSTOMFIELD, AuditAction.DELETE, new CompositeId(serviceId, fieldId).toString(), null);
			}
			DbUtils.commitQuietly(con);
			
		} catch (Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * Executes the DataSource Query linked to the specified CustomField.
	 * No exception will be thrown for query related, any errors will be reported in the returned result object.
	 * @param fieldServiceId The owning Service ID.
	 * @param fieldId The CustomField ID.
	 * @param placeholdersValues Map of placeholder/parameters values to substitute before execution; it can be `null`.
	 * @param pagination Pagination info; it can be `null`.
	 * @param filter Filtering info; it can be `null`.
	 * @return Query result object.
	 * @throws WTException 
	 */
	public DataSourceBase.ExecuteQueryResult<RowsAndCols> executeCustomFieldDataSourceQuery(final String fieldServiceId, final String fieldId, final Map<String, String> placeholdersValues, final PageInfo pagination, final FilterInfo filter) throws WTException {
		Check.notEmpty(fieldServiceId, "fieldServiceId");
		Check.notEmpty(fieldId, "fieldId");
		
		// Rights not checked here: they will be checked inside getCustomField call
		CustomField field = getCustomField(fieldServiceId, fieldId);
		if (field == null) throw new WTException("Custom-field not found [{}, {}]", fieldId, fieldServiceId);
		if (!field.isDataBindableType()) throw new WTException("Custom-field not applicable within a data-source [{}, {}]", fieldId, fieldServiceId);
		if (StringUtils.isBlank(field.getQueryId())) throw new WTException("Custom-field does not have a query set [{}, {}]", fieldId, fieldServiceId);
		
		String valueField = field.getProps().get("valueField");
		if (StringUtils.isBlank(valueField)) throw new WTException("Custom-field does not have a valid valueField [{}, {}]", fieldId, fieldServiceId);
		String displayField = field.getProps().get("displayField");
		if (StringUtils.isBlank(displayField)) displayField = valueField;
		
		DataSourcesManager dsMgr = wta.getDataSourcesManager();
		Set<String> cols = new LinkedHashSet<>(Arrays.asList(valueField, displayField));
		DataSourcesManager.FilterClause filterClause = null;
		if (filter != null) {
			if (filter.isQuery) {
				filterClause = new DataSourcesManager.FilterClause(Arrays.asList(valueField, displayField), filter);
			} else {
				filterClause = new DataSourcesManager.FilterClause(Arrays.asList(valueField), filter);
			}
		}
		DataSourcesManager.QueryPlaceholders placeholders = new DataSourcesManager.QueryPlaceholders(field.getDomainId(), getTargetProfileId().getUserId(), placeholdersValues);
		return dsMgr.executeQuery(field.getDomainId(), field.getQueryId(), placeholders, pagination, false, new FilterableArrayListHandler(cols), filterClause, !cfieldsLicensed ? 10 : null);
	}
	
	/**
	 * Executes the DataSource Query targeted by the specified ID.
	 * @param dataSourceQueryId The query ID.
	 * @param pagination Pagination info; it can be `null`.
	 * @param debugReport `true` to provide a detailed debug method in the result messages.
	 * @return Query result object.
	 * @throws WTException 
	 */
	public DataSourceBase.ExecuteQueryResult<RowsAndCols> executeDataSourceQuery(final String dataSourceQueryId, final PageInfo pagination, final boolean debugReport) throws WTException {
		Check.notEmpty(dataSourceQueryId, "dataSourceQueryId");
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			DataSourcesManager dsMgr = wta.getDataSourcesManager();
			DataSourcesManager.QueryPlaceholders placeholders = new DataSourcesManager.QueryPlaceholders(targetDomainId, getTargetProfileId().getUserId());
			return dsMgr.executeQuery(targetDomainId, dataSourceQueryId, placeholders, pagination, debugReport, new FilterableArrayListHandler(), null, !cfieldsLicensed ? 10 : null);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	/**
	 * Returns a set of columns names guessed from DataSource Query execution result.
	 * @param dataSourceQueryId The query ID.
	 * @return Column names set.
	 * @throws WTException 
	 */
	public Set<String> guessDataSourceQueryColumns(final String dataSourceQueryId) throws WTException {
		Check.notEmpty(dataSourceQueryId, "dataSourceQueryId");
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			DataSourcesManager dsMgr = wta.getDataSourcesManager();
			DataSourcesManager.QueryPlaceholders placeholders = new DataSourcesManager.QueryPlaceholders(targetDomainId, getTargetProfileId().getUserId());
			return dsMgr.guessQueryColumns(targetDomainId, dataSourceQueryId, placeholders);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	/**
	 * Returns a set of placeholder names found in DataSource Query SQL definition.
	 * @param dataSourceQueryId The query ID.
	 * @return Placeholder names set.
	 * @throws WTException 
	 */
	public Set<String> extractDataSourceQueryPlaceholders(final String dataSourceQueryId) throws WTException {
		Check.notEmpty(dataSourceQueryId, "dataSourceQueryId");
		
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			DataSourcesManager dsMgr = wta.getDataSourcesManager();
			return dsMgr.extractQueryPlaceholders(targetDomainId, dataSourceQueryId);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	/**
	 * Returns a list of available DataSources.
	 * @return Map of data-sources by their IDs.
	 * @throws WTException 
	 */
	public Map<String, DataSourcePooled> listDataSources() throws WTException {
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			DataSourcesManager dsMgr = wta.getDataSourcesManager();
			return dsMgr.listDataSources(targetDomainId);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	/**
	 * Returns a list of available DataSource Queries.
	 * @return Map of queries by their IDs.
	 * @throws WTException 
	 */
	public Map<String, DataSourceQuery> listDataSourceQueries() throws WTException {
		try {
			String targetDomainId = getTargetProfileId().getDomainId();
			ensureProfileDomain(targetDomainId);
			
			DataSourcesManager dsMgr = wta.getDataSourcesManager();
			return dsMgr.listDataSourceQueries(targetDomainId);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		}
	}
	
	public Meeting createMeeting(final String roomName) throws WTException {
		return createMeeting(roomName, null);
	}
	
	public Meeting createMeeting(final String roomName, final Locale locale) throws WTException {
		RunContext.ensureIsPermitted(false, SERVICE_ID, "MEETING", "CREATE");
		
		Locale targetLocale = locale == null ? getLocale() : locale;
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, getTargetProfileId().getDomainId());	
		Meeting.Provider provider = css.getMeetingProvider();
		
		try {
			if (Meeting.Provider.JITSI.equals(provider)) {
				CoreSettings.MeetingJitsiConfig config = (CoreSettings.MeetingJitsiConfig)css.getMeetingProviderConfig(provider);
				if (config.url == null) throw new WTException("Invalid configuration for '{}' meeting provider. [url is missing]", provider);
				
				String meetingId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, Arrays.copyOfRange(NanoIdUtils.DEFAULT_ALPHABET, 2, 63), NanoIdUtils.DEFAULT_SIZE);
				if (config.prependUsernameToMeetingId) {
					meetingId = StringUtils.replace(getTargetProfileId().getUserId(), ".", "_") + "_" + meetingId;
				}
				
				Meeting.Builder builder = new Meeting.Builder();
				builder.withProvider(provider);
				builder.withId(meetingId);
				builder.withRoomName(roomName);
				
				URIBuilder linkBuilder = new URIBuilder(config.url);
				URIUtils.appendPath(linkBuilder, meetingId);
				if (!StringUtils.isBlank(roomName)) {
					linkBuilder.setFragment("config.callDisplayName=\"" + roomName + "\"");
				}
				URI linkUri = linkBuilder.build();
				builder.withLink(linkUri);
				
				String dollarJitsi = StringUtils.defaultIfBlank(config.name, "Jitsi");
				builder.withShareEmbedTexts(new Meeting.ShareEmbedTexts(
					StringUtils.replace(WT.lookupResource(SERVICE_ID, targetLocale, "meeting.jitsi.share.info"), "$jitsi", dollarJitsi),
					StringUtils.replace(WT.lookupResource(SERVICE_ID, targetLocale, "meeting.jitsi.share.subject"), "$jitsi", dollarJitsi),
					StringUtils.replace(WT.lookupResource(SERVICE_ID, targetLocale, "meeting.jitsi.share.unscheduled.description"), "$jitsi", dollarJitsi),
					StringUtils.replace(WT.lookupResource(SERVICE_ID, targetLocale, "meeting.jitsi.share.scheduled.description"), "$jitsi", dollarJitsi)	
				));
				
				return builder.build();
				
			} else {
				throw new WTException("Meeting provider not supported [{}]", css.getString(CoreSettings.MEETING_PROVIDER, null));
			}
		} catch (URISyntaxException ex) {
			throw new WTException(ex);
		}
	}
	
	private CustomPanel doCustomPanelGet(Connection con, String domainId, String serviceId, String customPanelId) {
		CustomPanelDAO cupDao = CustomPanelDAO.getInstance();
		CustomPanelFieldDAO cupfDao = CustomPanelFieldDAO.getInstance();
		CustomPanelTagDAO cuptDao = CustomPanelTagDAO.getInstance();
		
		OCustomPanel opanel = cupDao.selectByDomainService(con, domainId, serviceId, customPanelId);
		if (opanel == null) return null;
		Set<String> fieldIds = cupfDao.selectFieldsByPanel(con, customPanelId);
		Set<String> tagIds = cuptDao.selectTagsByPanel(con, customPanelId);
		
		return ManagerUtils.createCustomPanel(opanel, fieldIds, tagIds);
	}
	
	private CustomPanel doCustomPanelUpdate(Connection con, String domainId, String serviceId, String panelId, CustomPanelBase panel) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		CustomPanelDAO cupDao = CustomPanelDAO.getInstance();
		CustomPanelFieldDAO cupfDao = CustomPanelFieldDAO.getInstance();
		CustomPanelTagDAO cuptDao = CustomPanelTagDAO.getInstance();
		
		OCustomPanel opanel = ManagerUtils.fillOCustomPanel(new OCustomPanel(), panel);
		opanel.setDomainId(domainId);
		opanel.setServiceId(serviceId);
		
		int ret;
		if (panelId == null) {
			opanel.setCustomPanelId(cupDao.generateCustomPanelId());
			ret = cupDao.insert(con, opanel);
			if (panel.getFields() != null) {
				cupfDao.batchInsert(con, opanel.getCustomPanelId(), panel.getFields());
			}
			if (panel.getTags() != null) {
				Set<String> validTagIds = tagDao.selectIdsByDomainOwners(con, opanel.getDomainId(), Arrays.asList(OTag.OWNER_NONE));
				for (String tagId : panel.getTags()) {
					if (!validTagIds.contains(tagId)) throw new WTException("Tag '{}' is personal, therefore not usable within panels.", tagId);
				}
				cuptDao.batchInsert(con, opanel.getCustomPanelId(), panel.getTags());
			}
			
		} else {
			opanel.setCustomPanelId(panelId);
			ret = cupDao.update(con, opanel);
			cupfDao.deleteByPanel(con, opanel.getCustomPanelId());
			if (panel.getFields() != null) {
				cupfDao.batchInsert(con, opanel.getCustomPanelId(), panel.getFields());
			}
			cuptDao.deleteByPanel(con, opanel.getCustomPanelId());
			if (panel.getTags() != null) {
				Set<String> validTagIds = tagDao.selectIdsByDomainOwners(con, opanel.getDomainId(), Arrays.asList(OTag.OWNER_NONE));
				for (String tagId : panel.getTags()) {
					if (!validTagIds.contains(tagId)) throw new WTException("Tag '{}' is personal, therefore not usable within panels.", tagId);
				}
				cuptDao.batchInsert(con, opanel.getCustomPanelId(), panel.getTags());
			}
		}
		
		return (ret == 1) ? ManagerUtils.createCustomPanel(opanel, panel.getFields(), panel.getTags()) : null;
	}
	
	private CustomField doCustomFieldUpdate(Connection con, String domainId, String serviceId, String fieldId, CustomFieldBase customField) throws WTException {
		CustomFieldDAO cufDao = CustomFieldDAO.getInstance();
		
		OCustomField ofield = ManagerUtils.fillOCustomField(new OCustomField(), customField);
		ofield.setDomainId(domainId);
		ofield.setServiceId(serviceId);
		ManagerUtils.validate(ofield);
		
		int ret;
		if (fieldId == null) {
			ofield.setCustomFieldId(cufDao.generateCustomFieldId());
			ret = cufDao.insert(con, ofield, BaseDAO.createRevisionTimestamp());
		} else {
			ofield.setCustomFieldId(fieldId);
			ret = cufDao.update(con, ofield, BaseDAO.createRevisionTimestamp());
		}
		
		return (ret == 1) ? ManagerUtils.fillCustomField(new CustomField(), ofield) : null;
	}
	
	public List<AuditLog> listAuditLog(final String serviceId, final String context, final String action, final String referenceId) throws WTException {
		Check.notEmpty(serviceId, "serviceId");
		Check.notEmpty(context, "context");
		Check.notEmpty(referenceId, "referenceId");
		AuditLogDAO logDao = AuditLogDAO.getInstance();
		ArrayList<AuditLog> items = new ArrayList<>();
		Connection con = null;
		
		try {
			String domainId = getTargetProfileId().getDomainId();
			con = WT.getCoreConnection();
			for (OAuditLog olog : logDao.selectByReferenceId(con, domainId, serviceId, context, action, referenceId)) {
				items.add(createAuditLog(domainId, olog));
			}
			return items;
			
		} catch (SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private AuditLog createAuditLog(String domainId, OAuditLog olog) {
		UserProfileId uid = new UserProfileId(domainId, olog.getUserId());
		DateTimeZone userTz = DateTimeZone.forID(WT.getUserData(uid).getTimeZoneId());
		DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter(userTz);
		
		AuditLog log = new AuditLog();
		log.setAuditLogId(olog.getAuditLogId());
		log.setTimestamp(ymdhmsZoneFmt.print(olog.getTimestamp()));
		log.setUserId(olog.getUserId());
		log.setUserName(WT.getUserData(uid).getDisplayName());
		log.setServiceId(olog.getServiceId());
		log.setContext(olog.getContext());
		log.setAction(olog.getAction());
		log.setReferenceId(olog.getReferenceId());
		log.setSessionId(olog.getSessionId());
		log.setData(olog.getData());
		
		return log;
	}
	
	public List<IMChat> listIMChats(boolean skipUnavailable) throws WTException {
		IMChatDAO dao = IMChatDAO.getInstance();
		ArrayList<IMChat> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			List<String> revStatuses = null;
			if (skipUnavailable) {
				revStatuses = Arrays.asList(
					EnumUtils.toSerializedName(IMChat.RevisionStatus.MODIFIED)
				);
			} else {
				revStatuses = Arrays.asList(
					EnumUtils.toSerializedName(IMChat.RevisionStatus.MODIFIED),
					EnumUtils.toSerializedName(IMChat.RevisionStatus.UNAVAILABLE)
				);
			}
			List<OIMChat> ochats = dao.selectByProfileRevStatus(con, getTargetProfileId(), revStatuses);
			for(OIMChat ocha : ochats) {
				items.add(createIMChat(ocha));
			}
			return items;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public IMChat getIMChat(String chatJid) throws WTException {
		IMChatDAO dao = IMChatDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OIMChat ocha = dao.selectAliveByProfileChat(con, getTargetProfileId(), chatJid);
			return (ocha != null) ? createIMChat(ocha) : null;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public IMChat addIMChat(IMChat chat) throws WTException {
		IMChatDAO dao = IMChatDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OIMChat ocha = createOIMChat(chat);
			ocha.setId(dao.getSequence(con).intValue());
			ocha.setDomainId(getTargetProfileId().getDomainId());
			ocha.setUserId(getTargetProfileId().getUserId());
			dao.insert(con, ocha, createRevisionTimestamp());	
			
			return createIMChat(ocha);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean updateIMChatLastSeenActivity(String chatJid) throws WTException {
		return updateIMChatLastSeenActivity(chatJid, createRevisionTimestamp());
	}
	
	public boolean updateIMChatLastSeenActivity(String chatJid, DateTime lastSeenActivity) throws WTException {
		IMChatDAO dao = IMChatDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.updateLastActivityByProfileChat(con, getTargetProfileId(), chatJid, lastSeenActivity, createRevisionTimestamp()) == 1;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean updateIMChatAvailablity(String chatJid, boolean available) throws WTException {
		IMChatDAO dao = IMChatDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			IMChat.RevisionStatus revisionStatus = available ? IMChat.RevisionStatus.MODIFIED : IMChat.RevisionStatus.UNAVAILABLE;
			return dao.updateRevisionStatusByProfileChat(con, getTargetProfileId(), chatJid, createRevisionTimestamp(), revisionStatus) == 1;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public int deleteIMChat(String chatJid) throws WTException {
		IMChatDAO chaDao = IMChatDAO.getInstance();
		IMMessageDAO mesDao = IMMessageDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection(false);
			
			int ret = chaDao.logicDeleteByProfileChat(con, getTargetProfileId(), chatJid, createRevisionTimestamp());
			mesDao.deleteByProfileChat(con, getTargetProfileId(), chatJid);
			DbUtils.commitQuietly(con);
			return ret;
			
		} catch (Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<LocalDate> listIMMessageDates(String chatJid, int year, DateTimeZone timezone) throws WTException {
		IMMessageDAO imesDao = IMMessageDAO.getInstance();
		ArrayList<LocalDate> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			List<DateTime> dts = imesDao.selectDatesByProfileChatYear(con, getTargetProfileId(), chatJid, year, timezone);
			for(DateTime dt : dts) {
				items.add(dt.withZone(timezone).toLocalDate());
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<IMMessage> listIMMessages(String chatJid, LocalDate date, DateTimeZone timezone, boolean byDelivery) throws WTException {
		IMMessageDAO imesDao = IMMessageDAO.getInstance();
		ArrayList<IMMessage> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			List<OIMMessage> omess = imesDao.selectByProfileChatDate(con, getTargetProfileId(), chatJid, date, timezone, byDelivery);
			for(OIMMessage omes : omess) {
				items.add(createIMMessage(omes));
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<IMMessage> findIMMessagesByQuery(String chatJid, String query, DateTimeZone timezone) throws WTException {
		IMMessageDAO imesDao = IMMessageDAO.getInstance();
		ArrayList<IMMessage> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			List<OIMMessage> omess = null;
			if (query == null) {
				omess = imesDao.findByProfileChat(con, getTargetProfileId(), chatJid);
			} else {
				omess = imesDao.findByProfileChatLike(con, getTargetProfileId(), chatJid, query);
			}
			for(OIMMessage omes : omess) {
				items.add(createIMMessage(omes));
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> listIMMessageStanzaIDs(String chatJid) throws WTException {
		IMMessageDAO imesDao = IMMessageDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return imesDao.selectStanzaIDsByProfileChat(con, getTargetProfileId(), chatJid);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addIMMessage(IMMessage message) throws WTException {
		IMMessageDAO imesDao = IMMessageDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OIMMessage omes = createOIMMessage(message);
			omes.setId(imesDao.getSequence(con).intValue());
			omes.setDomainId(getTargetProfileId().getDomainId());
			omes.setUserId(getTargetProfileId().getUserId());
			imesDao.insert(con, omes);	
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OTPManager.EmailConfig otpConfigureUsingEmail(String emailAddress) throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		return otp.configureEmail(getTargetProfileId(), emailAddress);
	}
	
	public OTPManager.GoogleAuthConfig otpConfigureUsingGoogleAuth(int qrCodeSize) throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		return otp.configureGoogleAuth(getTargetProfileId(), qrCodeSize);
	}
	
	public boolean otpActivate(OTPManager.Config config, String code) throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		return otp.activate(getTargetProfileId(), config, code);
	}
	
	public void otpDeactivate() throws WTException {
		//TODO: controllo accessi
		ensureUser();
		OTPManager otp = getOTPManager();
		otp.deactivate(getTargetProfileId());
	}
	
	public OTPManager.Config otpPrepareVerifyCode() throws WTException {
		ensureCallerService(SERVICE_ID, "otpPrepareVerifyCode");
		OTPManager otp = getOTPManager();
		return otp.prepareCheckCode(getTargetProfileId());
	}
	
	public boolean otpVerifyCode(OTPManager.Config params, String code) throws WTException {
		ensureCallerService(SERVICE_ID, "otpVerifyCode");
		OTPManager otp = getOTPManager();
		return otp.checkCode(getTargetProfileId(), params, code);
	}
	
	/**
	 * Enumerates Share origins.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param targetPermissionKeys The permission-keys involved in the lookup.
	 * @return
	 * @throws WTException 
	 */
	public List<ShareOrigin> listShareOrigins(final String serviceId, final String context, final Collection<String> targetPermissionKeys) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId targetPid = getTargetProfileId();
		return wtMgr.listShareOrigins(targetPid, targetPermissionKeys, serviceId, context);
	}
	
	/**
	 * Enumerates Share instances related to specified permission-key.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The source profileId of the share we are getting folders.
	 * @param targetPermissionKeys The permission-keys involved in the lookup.
	 * @return
	 * @throws WTException 
	 */
	public Set<String> getShareOriginInstances(final String serviceId, final String context, final UserProfileId originProfileId, final Collection<String> targetPermissionKeys) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId targetPid = getTargetProfileId();
		return wtMgr.getShareOriginInstances(targetPid, targetPermissionKeys, originProfileId, serviceId, context);
	}
	
	/**
	 * Gets Share configurations related to an instance of a Origin.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The origin profileId of which getting rights.
	 * @param instance The identifier of the shared-entity involved in the lookup.
	 * @param permissionKey The permission-keys involved in the lookup.
	 * @return
	 * @throws WTException 
	 */
	public Set<com.sonicle.webtop.core.app.model.Sharing.SubjectConfiguration> getShareSubjectConfiguration(final String serviceId, final String context, final UserProfileId originProfileId, final String instance, final String permissionKey) throws WTException {
		return getShareSubjectConfiguration(serviceId, context, originProfileId, instance, permissionKey, null);
	}
	
	/**
	 * Gets Share configurations related to an instance of a Origin.
	 * @param <T>
	 * @param serviceId The related service ID.
	 * @param context The context-name of the Share.
	 * @param originProfileId The originating Share's profileId.
	 * @param instance The identifier of the shared-entity involved in the lookup.
	 * @param permissionKey The permission-keys involved in the lookup.
	 * @param typeOfData
	 * @return
	 * @throws WTException 
	 */
	public <T> Set<com.sonicle.webtop.core.app.model.Sharing.SubjectConfiguration> getShareSubjectConfiguration(final String serviceId, final String context, final UserProfileId originProfileId, final String instance, final String permissionKey, final Class<T> typeOfData) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		return wtMgr.getShareConfigurations(originProfileId, serviceId, context, instance, permissionKey, typeOfData);
	}
	
	/**
	 * Applies a set of Share configurations related to an instance of a Origin.
	 * @param <T>
	 * @param serviceId The related service ID.
	 * @param context The context-name of the Share.
	 * @param originProfileId The originating Share's profileId.
	 * @param instance The identifier of the shared-entity involved in the lookup.
	 * @param permissionKey The permission-keys involved in the lookup.
	 * @param configurations A set of configurations: one for each involved Subject ID.
	 * @param typeOfData
	 * @throws WTException 
	 */
	public <T> void updateShareConfigurations(final String serviceId, final String context, final UserProfileId originProfileId, final String instance, final String permissionKey, final Set<com.sonicle.webtop.core.app.model.Sharing.SubjectConfiguration> configurations, final Class<T> typeOfData) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		wtMgr.updateShareConfigurations(originProfileId, serviceId, context, instance, permissionKey, configurations, typeOfData);
	}
	
	/**
	 * Enumerates FolderShare origins.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @return
	 * @throws WTException 
	 */
	public List<ShareOrigin> listFolderShareOrigins(final String serviceId, final String context) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId targetPid = getTargetProfileId();
		return wtMgr.listFolderShareOrigins(targetPid, serviceId, context);
	}
	
	/**
	 * Enumerates FolderShare folders.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The source profileId of the share we are getting folders.
	 * @return
	 * @throws WTException 
	 */
	public FolderShareOriginFolders getFolderShareOriginFolders(final String serviceId, final String context, final UserProfileId originProfileId) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId targetPid = getTargetProfileId();
		return wtMgr.getFolderShareOriginFolders(targetPid, originProfileId, serviceId, context);
	}
	
	/**
	 * Evaluates passed FolderShare right, described under-the-hood by action, against profile's permissions.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The source profileId of the share we are checking permissions.
	 * @param scope 
	 * @param throwOnMissingShare Set to `false` to NOT throw WTNotFoundException if share at requested scope is not available.
	 * @param target Permission selector: folder or elements
	 * @param action Permission action to evaluate.
	 * @return
	 * @throws WTException 
	 */
	public Boolean evaluateFolderSharePermission(final String serviceId, final String context, final UserProfileId originProfileId, final FolderSharing.Scope scope, final boolean throwOnMissingShare, final FolderShare.EvalTarget target, final String action) throws WTException {
		boolean[] bools = evaluateFolderSharePermission(serviceId, context, originProfileId, scope, throwOnMissingShare, target, new String[]{action});
		return bools != null ? bools[0] : null;
	}
	
	/**
	 * Evaluates passed FolderShare rights, described under-the-hood by actions, against profile's permissions.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The source profileId of the share we are checking permissions.
	 * @param scope 
	 * @param throwOnMissingShare Set to `false` to NOT throw WTNotFoundException if share at requested scope is not available.
	 * @param target Permission selector: folder or elements
	 * @param actions Permission actions to evaluate.
	 * @return
	 * @throws WTException 
	 */
	public boolean[] evaluateFolderSharePermission(final String serviceId, final String context, final UserProfileId originProfileId, final FolderSharing.Scope scope, final boolean throwOnMissingShare, final FolderShare.EvalTarget target, final String[] actions) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId targetPid = getTargetProfileId();
		return wtMgr.evaluateFolderSharePermission(targetPid, originProfileId, serviceId, context, scope, throwOnMissingShare, target, actions);
	}
	
	/**
	 * Evaluates all available FolderShare rights against profile's permissions.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The source profileId of the share we are checking permissions.
	 * @param scope
	 * @param throwOnMissingShare Set to `false` to NOT throw WTNotFoundException if share at requested scope is not available.
	 * @return
	 * @throws WTException 
	 */
	public FolderShare.Permissions evaluateFolderSharePermissions(final String serviceId, final String context, final UserProfileId originProfileId, final FolderSharing.Scope scope, final boolean throwOnMissingShare) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		UserProfileId targetPid = getTargetProfileId();
		return wtMgr.evaluateFolderSharePermissions(targetPid, originProfileId, serviceId, context, scope, throwOnMissingShare);
	}
	
	/**
	 * Gets FolderShare rights of an origin.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The origin profileId of which getting rights.
	 * @param scope The scope of the search: wildcard for root target or folder for specific instance.
	 * @return
	 * @throws WTException 
	 */
	public Set<FolderSharing.SubjectConfiguration> getFolderShareConfigurations(final String serviceId, final String context, final UserProfileId originProfileId, final FolderSharing.Scope scope) throws WTException {
		return getFolderShareConfigurations(serviceId, context, originProfileId, scope, null);
	}
	
	/**
	 * Gets FolderShare rights of an origin.
	 * @param <T>
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The origin profileId of which getting rights.
	 * @param scope The scope of the search: wildcard for root target or folder for specific instance.
	 * @param typeOfData Type of Data object in configuration.
	 * @return
	 * @throws WTException 
	 */
	public <T> Set<FolderSharing.SubjectConfiguration> getFolderShareConfigurations(final String serviceId, final String context, final UserProfileId originProfileId, final FolderSharing.Scope scope, final Class<T> typeOfData) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		return wtMgr.getFolderShareConfigurations(originProfileId, serviceId, context, scope, typeOfData);
	}
	
	/**
	 * Sets FolderShare rights of an origin.
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The origin profileId of which setting rights.
	 * @param scope The scope of the search: wildcard for root target or folder for specific instance.
	 * @param configurations The rights collection to set.
	 * @throws WTException 
	 */
	public void updateFolderShareConfigurations(final String serviceId, final String context, final UserProfileId originProfileId, final FolderSharing.Scope scope, final Set<FolderSharing.SubjectConfiguration> configurations) throws WTException {
		updateFolderShareConfigurations(serviceId, context, originProfileId, scope, configurations, null);
	}
	
	/**
	 * Sets FolderShare rights of an origin.
	 * @param <T>
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The origin profileId of which setting rights.
	 * @param scope The scope of the search: wildcard for root target or folder for specific instance.
	 * @param configurations The rights collection to set.
	 * @param typeOfData Type of Data object in configuration.
	 * @throws WTException 
	 */
	public <T> void updateFolderShareConfigurations(final String serviceId, final String context, final UserProfileId originProfileId, final FolderSharing.Scope scope, final Set<FolderSharing.SubjectConfiguration> configurations, final Class<T> typeOfData) throws WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		wtMgr.updateFolderShareConfigurations(originProfileId, serviceId, context, scope, configurations, typeOfData);
	}
	
	/**
	 * Gets stored Share data involved in sharing between origin to target.
	 * @param <T>
	 * @param targetProfileId
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The origin profileId of the sharing.
	 * @param instance The identifier of the shared-entity involved in the lookup.
	 * @param dataType The Class type of saved raw data.
	 * @param throwOnMissingShare Set to `false` to return null instead of throwing an exception on missing share.
	 * @return
	 * @throws WTNotFoundException
	 * @throws WTException 
	 */
	public <T> T getShareData(final UserProfileId targetProfileId, final String serviceId, final String context, final UserProfileId originProfileId, final String instance, Class<T> dataType, final boolean throwOnMissingShare) throws WTNotFoundException, WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		return wtMgr.getShareData(targetProfileId, serviceId, context, originProfileId, instance, dataType, throwOnMissingShare);
	}
	
	/**
	 * Stores passed Share data within sharing between origin to target.
	 * @param <T>
	 * @param targetProfileId
	 * @param serviceId The related service ID.
	 * @param context The context-name (or groupName) of the share.
	 * @param originProfileId The origin profileId of the sharing.
	 * @param instance The identifier of the shared-entity involved in the lookup.
	 * @param data The data object.
	 * @param dataType The Class type of passed data.
	 * @param throwOnMissingShare Set to `false` to return null instead of throwing an exception on missing share.
	 * @return
	 * @throws WTNotFoundException
	 * @throws WTException 
	 */
	public <T> boolean updateShareData(final UserProfileId targetProfileId, final String serviceId, final String context, final UserProfileId originProfileId, final String instance, T data, Class<T> dataType, final boolean throwOnMissingShare) throws WTNotFoundException, WTException {
		WebTopManager wtMgr = wta.getWebTopManager();
		return wtMgr.updateShareData(targetProfileId, serviceId, context, originProfileId, instance, data, dataType, throwOnMissingShare);
	}
	
	/**
	 * Check if an SMS provider has been configured.
	 * @return true if the SMS instance has been configured, false otherwise
	 */
	
	public boolean smsConfigured() {
		initSms();
		return sms!=null;
	}
	
	public SmsProvider smsGetProvider() {
		initSms();
		return sms;
	}
	
	/**
	 * Send SMS through the configured SMS provider.
	 * @param number The destination number
	 * @param text The SMS text
	 * @return true if the SMS was accpeted by the provider, false otherwise
	 */
	
	public void smsSend(String number, String text) throws WTException {
		initSms();
		if (sms==null) {
			throw new WTException("SMS not initialized");
		}
		UserProfileId targetPid = getTargetProfileId();
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, targetPid.getDomainId());	
		CoreUserSettings us = new CoreUserSettings(targetPid);
		
		//use common service settings or webtop username/password
		String username=css.getSmsWebrestUser();
		if (username==null) username=targetPid.getUserId();
		String spassword=css.getSmsWebrestPassword();
		char[] password=spassword!=null?spassword.toCharArray():RunContext.getPrincipal().getPassword();
		
		String sender=css.getSmsSender();
		String userSender=us.getSmsSender();
		if (userSender!=null && userSender.trim().length()>0) sender=userSender;
		
		if (sender==null) sender=wta.getPlatformName();
		
		boolean isAlpha=StringUtils.isAlpha(sender);
		String fromMobile=isAlpha?null:sender;
		String fromName=isAlpha?sender:null;
		sms.send(fromName, fromMobile, number, text, username, password);
	}
	
	/**
	 * Check if a PBX has been configured.
	 * @return true if the PBX instance has been configured, false otherwise
	 */
	
	public boolean pbxConfigured() {
		initPbx();
		return pbx!=null;
	}
	
	/**
	 * Get the configured PBX provider instance.
	 * @return The PBX provider instance
	 */
	
	public PbxProvider pbxGetProvider() {
		initPbx();
		return pbx;
	}
	
	/**
	 * Run PBX call through the configured PBX provider.
	 * @param number The number to call
	 * @return true if the call was accpeted by the provider, false otherwise
	 */
	
	public void pbxCall(String number) throws WTException {
		initPbx();
		if (pbx==null) {
			throw new WTException("Pbx not initialized");
		}
		UserProfileId targetPid = getTargetProfileId();
		CoreUserSettings us = new CoreUserSettings(targetPid);
		
		//use webtop username/password or from user settings
		String username=us.getPbxUsername();
		if (username==null) username=targetPid.getUserId();
		String spassword=us.getPbxPassword();
		char[] password=spassword!=null?spassword.toCharArray():RunContext.getPrincipal().getPassword();
		
		pbx.call(number, username, password);
	}
	
	/**
	 * @deprecated use listFolderShareOrigins instead
	 */
	@Deprecated
	public List<IncomingShareRoot> listIncomingShareRoots(String serviceId, String groupName) throws WTException {
		WebTopManager wtmgr = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		UserDAO usedao = UserDAO.getInstance();
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			String profileUid = wtmgr.lookupSubjectSid(targetPid, GenericSubject.Type.USER);
			List<String> roleUids = wtmgr.getComputedRolesAsStringByUser(targetPid, true, true);
			
			String rootKey = OShare.buildRootKey(groupName);
			String folderKey = OShare.buildFolderKey(groupName);
			String rootPermissionKey = ServiceSharePermission.buildRootPermissionKey(groupName);
			String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
			String elementsPermissionKey = ServiceSharePermission.buildElementsPermissionKey(groupName);
			
			con = WT.getCoreConnection();
			
			// In order to find incoming root, we need to pass through folders
			// that have at least a permission, getting incoming uids.
			// We look into permission returning each share instance that have 
			// "*@SHARE_FOLDER" as key and satisfies a set of roles. Then we can
			// get a list of unique uids (from shares table) that owns the share.
			List<String> permissionKeys = Arrays.asList(rootPermissionKey, folderPermissionKey, elementsPermissionKey);
			List<String> originUids = shadao.viewOriginByRoleServiceKey(con, roleUids, serviceId, folderKey, permissionKeys);
			ArrayList<IncomingShareRoot> roots = new ArrayList<>();
			for (String uid : originUids) {
				if (uid.equals(profileUid)) continue; // Skip self role
				
				// Foreach incoming uid we have to find the root share and then
				// test if READ right is allowed
				
				OShare root = shadao.selectByUserServiceKeyInstance2(con, uid, serviceId, rootKey, OShare.INSTANCE_ROOT);
				if (root == null) continue;
				OUser user = usedao.selectBySid(con, uid);
				if (user == null) continue;
				
				roots.add(new IncomingShareRoot(root.getShareId().toString(), wtmgr.lookupSubjectProfile(root.getUserUid(), GenericSubject.Type.USER), user.getDisplayName()));
			}
			Collections.sort(roots, (IncomingShareRoot ish1, IncomingShareRoot ish2) -> ish1.getDescription().compareTo(ish2.getDescription()));
			return roots;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "Unable to list share roots for {0}", targetPid.toString());
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/**
	 * @deprecated use getFolderShareOriginFolders instead
	 */
	@Deprecated
	public List<OShare> listIncomingShareFolders(String rootShareId, String groupName) throws WTException {
		ShareDAO shadao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OShare rootShare = shadao.selectById(con, Integer.valueOf(rootShareId));
			if(rootShare == null) throw new WTException("Unable to find root share [{0}]", rootShareId);
			
			String folderShareKey = OShare.buildFolderKey(groupName);
			String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
			
			ArrayList<OShare> folders = new ArrayList<>();
			List<OShare> shares = shadao.selectByUserServiceKey(con, rootShare.getUserUid(), rootShare.getServiceId(), folderShareKey);
			for(OShare share : shares) {
				if(RunContext.isPermitted(true, getTargetProfileId(), rootShare.getServiceId(), folderPermissionKey, ServicePermission.ACTION_READ, share.getShareId().toString())) {
					folders.add(share);
				}
			}
			return folders;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public <T>T getIncomingShareFolderData(String shareId, Class<T> type) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		ShareDataDAO shddao = ShareDataDAO.getInstance();
		Connection con = null;
		
		try {
			String profileUid = usrm.lookupSubjectSid(getTargetProfileId(), GenericSubject.Type.USER);
			con = WT.getCoreConnection();
			
			OShare share = shadao.selectById(con, Integer.valueOf(shareId));
			if(share == null) throw new WTException("Unable to find share [{0}]", shareId);
			if(!areActionsPermittedOnShare(share, ServiceSharePermission.TARGET_FOLDER, new String[]{ServicePermission.ACTION_READ})[0]) {
				throw new WTException("Share not accessible [{0}]", shareId);
			}
			
			OShareData data = shddao.selectByShareUser(con, Integer.valueOf(shareId), profileUid);
			if(data != null) {
				return LangUtils.value(data.getValue(), null, type);
			} else {
				return null;
			}
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Deprecated
	public boolean[] areActionsPermittedOnShare(String shareId, String permissionTarget, String[] actions) throws WTException {
		ShareDAO shadao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OShare share = shadao.selectById(con, Integer.valueOf(shareId));
			if(share == null) throw new WTException("Unable to find share [{0}]", shareId);
			return areActionsPermittedOnShare(share, permissionTarget, actions);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Deprecated
	public boolean[] areActionsPermittedOnShare(OShare share, String permissionTarget, String[] actions) throws WTException {
		String instance = String.valueOf(share.getShareId());
		String groupName = OShare.extractGroupNameFromKey(share.getKey());
		String permKey = ServiceSharePermission.buildPermissionKey(permissionTarget, groupName);
		
		UserProfileId targetPid = getTargetProfileId();
		boolean[] perms = new boolean[actions.length];
		for(int i=0; i<actions.length; i++) {
			perms[i] = RunContext.isPermitted(true, targetPid, share.getServiceId(), permKey, actions[i], instance);
		}
		return perms;
	}
	
	@Deprecated
	public boolean isShareRootPermitted(String shareId, String action) throws WTException {
		return areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ROOT, new String[]{action})[0];
	}
	
	@Deprecated
	public boolean isShareFolderPermitted(String shareId, String action) throws WTException {
		return areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_FOLDER, new String[]{action})[0];
	}
	
	@Deprecated
	public boolean isShareElementsPermitted(String shareId, String action) throws WTException {
		return areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ELEMENTS, new String[]{action})[0];
	}
	
	@Deprecated
	public SharePermsRoot getShareRootPermissions(String shareId) throws WTException {
		boolean[] bools = areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ROOT, SharePermsRoot.ACTIONS);
		return new SharePermsRoot(SharePermsRoot.ACTIONS, bools);
	}
	
	@Deprecated
	public SharePermsFolder getShareFolderPermissions(String shareId) throws WTException {
		boolean[] bools = areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_FOLDER, SharePermsFolder.ACTIONS);
		return new SharePermsFolder(SharePermsFolder.ACTIONS, bools);
	}
	
	@Deprecated
	public SharePermsElements getShareElementsPermissions(String shareId) throws WTException {
		boolean[] bools = areActionsPermittedOnShare(shareId, ServiceSharePermission.TARGET_ELEMENTS, SharePermsElements.ACTIONS);
		return new SharePermsElements(SharePermsElements.ACTIONS, bools);
	}
	
	@Deprecated
	public Sharing getSharing(String serviceId, String groupName, String shareId) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		String rootShareKey = OShare.buildRootKey(groupName);
		String folderShareKey = OShare.buildFolderKey(groupName);
		String rootPermissionKey = ServiceSharePermission.buildRootPermissionKey(groupName);
		String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
		String elementsPermissionKey = ServiceSharePermission.buildElementsPermissionKey(groupName);
		
		try {
			CompositeId cid = new CompositeId().parse(shareId);
			int level = cid.getSize()-1;
			String rootId = cid.getToken(0);
			
			con = WT.getCoreConnection();
			
			// Retrieves the root share
			OShare rootShare = null;
			if(rootId.equals("0")) {
				String puid = usrm.lookupSubjectSid(getTargetProfileId(), GenericSubject.Type.USER);
				rootShare = shadao.selectByUserServiceKeyInstance2(con, puid, serviceId, rootShareKey, OShare.INSTANCE_ROOT);
			} else {
				rootShare = shadao.selectById(con, Integer.valueOf(rootId));
			}
			
			Sharing outshare = new Sharing();
			outshare.setId(shareId);
			outshare.setLevel(level);
			
			if(rootShare != null) { // A rootShare must be defined in order to continue...
				if(level == 0) {
					LinkedHashSet<String> roleUids = new LinkedHashSet<>();
					roleUids.addAll(listRoles(serviceId, rootPermissionKey, rootShare.getShareId().toString()));
					
					OShare folderShare = shadao.selectByUserServiceKeyInstance2(con, rootShare.getUserUid(), serviceId, folderShareKey, OShare.INSTANCE_WILDCARD);
					if(folderShare != null) roleUids.addAll(listRoles(serviceId, folderPermissionKey, folderShare.getShareId().toString()));

					for(String roleUid : roleUids) {
						// Root...
						SharePermsRoot rperms = new SharePermsRoot();
						for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, rootPermissionKey, rootShare.getShareId().toString())) {
							rperms.parse(perm.getAction());
						}
						// Folder...
						SharePermsFolder fperms = new SharePermsFolder();
						if(folderShare != null) {
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, folderPermissionKey, folderShare.getShareId().toString())) {
								fperms.parse(perm.getAction());
							}
						}
							
						// Elements...
						SharePermsElements eperms = new SharePermsElements();
						if(folderShare != null) {
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, elementsPermissionKey, folderShare.getShareId().toString())) {
								eperms.parse(perm.getAction());
							}
						}
						outshare.getRights().add(new Sharing.RoleRights(roleUid, rperms, fperms, eperms));
					}


				} else if(level == 1) {
					String folderId = cid.getToken(1);
					OShare folderShare = shadao.selectByUserServiceKeyInstance2(con, rootShare.getUserUid(), serviceId, folderShareKey, folderId);

					if(folderShare != null) {
						List<String> roleUids = listRoles(serviceId, folderPermissionKey, folderShare.getShareId().toString());
						for(String roleUid : roleUids) {
							// Folder...
							SharePermsFolder fperms = new SharePermsFolder();
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, folderPermissionKey, folderShare.getShareId().toString())) {
								fperms.parse(perm.getAction());
							}
							// Elements...
							SharePermsElements eperms = new SharePermsElements();
							for(ORolePermission perm : rpedao.selectByRoleServiceKeyInstance(con, roleUid, serviceId, elementsPermissionKey, folderShare.getShareId().toString())) {
								eperms.parse(perm.getAction());
							}
							outshare.getRights().add(new Sharing.RoleRights(roleUid, null, fperms, eperms));
						}
					}
				}
			}
			
			return outshare;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Deprecated
	public void updateSharing(String serviceId, String groupName, Sharing sharing) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO shadao = ShareDAO.getInstance();
		Connection con = null;
		
		// Sharing is handled at two levels: root(0) and folder(1).
		// The first level tracks permissions associated to any instances 
		// of items in the groupName; the second tracks permissions related to
		// a specific item instance instead.
		// If for example we are talking about addressbook contacts we can 
		// define the contact category as a groupName.
		// Of course we can have many categories: Work, Home, Prospect, etc.
		// So, looking at sharing, the root level register permissions valid 
		// for any items of the groupName (Work, Home, ...).
		// At next level, we have permissions for only the Work category.
		
		try {
			String puid = usrm.lookupSubjectSid(getTargetProfileId(), GenericSubject.Type.USER);
			
			// Parses the sharing ID as a composite key:
			// - "0"		for root share
			// - "0|{id}"	for folder share
			CompositeId cid = new CompositeId().parse(sharing.getId());
			int level = cid.getSize()-1;
			String rootId = cid.getToken(0);
			
			String rootKey = OShare.buildRootKey(groupName);
			String folderKey = OShare.buildFolderKey(groupName);
			String rootPermRes = ServiceSharePermission.buildRootPermissionKey(groupName);
			String folderPermRes = ServiceSharePermission.buildFolderPermissionKey(groupName);
			String elementsPermRes = ServiceSharePermission.buildElementsPermissionKey(groupName);
			
			con = WT.getCoreConnection();
			
			// Retrieves the root share
			OShare rootShare = null;
			if(rootId.equals("0")) {
				rootShare = shadao.selectByUserServiceKeyInstance2(con, puid, serviceId, rootKey, OShare.INSTANCE_ROOT);
			} else {
				rootShare = shadao.selectById(con, Integer.valueOf(rootId));
			}
			if(rootShare == null) rootShare = addRootShare(con, puid, serviceId, rootKey);
			
			if(level == 0) {
				OShare folderShare = shadao.selectByUserServiceKeyInstance2(con, rootShare.getUserUid(), serviceId, folderKey, OShare.INSTANCE_WILDCARD);
				
				if(!sharing.getRights().isEmpty()) {
					removeRootSharePermissions(con, rootShare.getShareId().toString(), serviceId, groupName);
					if(folderShare == null) {
						folderShare = addFolderShare(con, rootShare.getUserUid(), serviceId, folderKey, OShare.INSTANCE_WILDCARD);
					} else { // Folder isn't new (and we have some rights)...
						// Removes all rights belonging to this folder share
						removeFolderSharePermissions(con, folderShare.getShareId().toString(), serviceId, groupName);
					}
					
					// Adds permissions according to specified rights...
					for(Sharing.RoleRights rr : sharing.getRights()) {
						if(rr.rootManage) addSharePermission(con, rr.roleUid, serviceId, rootPermRes, "MANAGE", rootShare.getShareId().toString());
						if(rr.folderRead) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "READ", folderShare.getShareId().toString());
						if(rr.folderUpdate) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.folderDelete) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "DELETE", folderShare.getShareId().toString());
						if(rr.elementsCreate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "CREATE", folderShare.getShareId().toString());
						if(rr.elementsUpdate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.elementsDelete) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "DELETE", folderShare.getShareId().toString());
					}
					
				} else {
					// If defines, removes folder share and its rights
					if(folderShare != null) removeFolderShare(con, folderShare.getShareId().toString(), serviceId, groupName);
				}
				
			} else if(level == 1) {
				String folderId = cid.getToken(1);
				OShare folderShare = shadao.selectByUserServiceKeyInstance2(con, rootShare.getUserUid(), serviceId, folderKey, folderId);
				
				if(!sharing.getRights().isEmpty()) {
					if(folderShare == null) {
						folderShare = addFolderShare(con, rootShare.getUserUid(), serviceId, folderKey, folderId);
					} else { // Folder isn't new (and we have some rights)...
						// Removes all rights belonging to this folder share
						removeFolderSharePermissions(con, folderShare.getShareId().toString(), serviceId, groupName);
					}

					// Adds permissions according to specified rights...
					for(Sharing.RoleRights rr : sharing.getRights()) {
						//if(rr.rootManage) addSharePermission(con, rr.roleUid, serviceId, rootPermRes, "MANAGE", rootShare.getShareId().toString());
						if(rr.folderRead) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "READ", folderShare.getShareId().toString());
						if(rr.folderUpdate) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.folderDelete) addSharePermission(con, rr.roleUid, serviceId, folderPermRes, "DELETE", folderShare.getShareId().toString());
						if(rr.elementsCreate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "CREATE", folderShare.getShareId().toString());
						if(rr.elementsUpdate) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "UPDATE", folderShare.getShareId().toString());
						if(rr.elementsDelete) addSharePermission(con, rr.roleUid, serviceId, elementsPermRes, "DELETE", folderShare.getShareId().toString());
					}

				} else { // No rights specified for any role...
					// If defines, removes folder share and its rights
					if(folderShare != null) removeFolderShare(con, folderShare.getShareId().toString(), serviceId, groupName);
				}
			}
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "Unable to update share rights");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ORolePermission addSharePermission(Connection con, String roleUid, String service, String key, String action, String instance) throws DAOException {
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		ORolePermission rp = new ORolePermission();
		rp.setRoleUid(roleUid);
		rp.setServiceId(service);
		rp.setKey(key);
		rp.setAction(action);
		rp.setInstance(instance);
		rp.setRolePermissionId(rpedao.getSequence(con).intValue());
		rpedao.insert(con, rp);
		return rp;
	}
	
	private void removeRootSharePermissions(Connection con, String shareId, String serviceId, String groupName) throws DAOException {
		String rootPermissionKey = ServiceSharePermission.buildRootPermissionKey(groupName);
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		rpedao.deleteByServiceKeyInstance(con, serviceId, rootPermissionKey, shareId);
	}
	
	private void removeFolderShare(Connection con, String shareId, String serviceId, String groupName) throws DAOException {
		ShareDAO shadao = ShareDAO.getInstance();
		
		// 1 - Deletes main folder share record
		shadao.deleteById(con, Integer.valueOf(shareId));
		
		// 2 - Deletes any permission related to folder share
		removeFolderSharePermissions(con, shareId, serviceId, groupName);
	}
	
	private void removeFolderSharePermissions(Connection con, String shareId, String serviceId, String groupName) throws DAOException {
		String folderPermissionKey = ServiceSharePermission.buildFolderPermissionKey(groupName);
		String elementsPermissionKey = ServiceSharePermission.buildElementsPermissionKey(groupName);
		RolePermissionDAO rpedao = RolePermissionDAO.getInstance();
		rpedao.deleteByServiceKeyInstance(con, serviceId, folderPermissionKey, shareId);
		rpedao.deleteByServiceKeyInstance(con, serviceId, elementsPermissionKey, shareId);
	}
	
	private OShare addRootShare(Connection con, String userUid, String serviceId, String shareKey) throws DAOException {
		ShareDAO dao = ShareDAO.getInstance();
		OShare share = new OShare();
		share.setUserUid(userUid);
		share.setServiceId(serviceId);
		share.setKey(shareKey);
		share.setInstance(OShare.INSTANCE_ROOT);
		share.setShareId(dao.getSequence(con).intValue());
		dao.insert(con, share);
		return share;
	}
	
	private OShare addFolderShare(Connection con, String userUid, String serviceId, String shareKey, String instance) throws DAOException {
		ShareDAO dao = ShareDAO.getInstance();
		OShare share = new OShare();
		share.setUserUid(userUid);
		share.setServiceId(serviceId);
		share.setKey(shareKey);
		share.setInstance(instance);
		share.setShareId(dao.getSequence(con).intValue());
		dao.insert(con, share);
		return share;
	}
			
	public List<OShare> listShareByOwner(UserProfileId pid, String serviceId, String shareKey) throws WTException {
		WebTopManager usrm = wta.getWebTopManager();
		ShareDAO dao = ShareDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			String uuid = usrm.lookupSubjectSid(pid, GenericSubject.Type.USER);
			return dao.selectByUserServiceKey(con, uuid, serviceId, shareKey);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<String> listRoles(String serviceId, String permissionKey, String instance) throws WTException {
		RolePermissionDAO dao = RolePermissionDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return dao.selectRolesByServiceKeyInstance(con, serviceId, permissionKey, instance);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OSnoozedReminder snoozeReminder(ReminderInApp reminder, DateTime remindOn) throws WTException {
		SnoozedReminderDAO dao = SnoozedReminderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			
			OSnoozedReminder item = new OSnoozedReminder();
			item.setDomainId(reminder.getProfileId().getDomain());
			item.setUserId(reminder.getProfileId().getUserId());
			item.setServiceId(reminder.getServiceId());
			item.setType(reminder.getType());
			item.setInstanceId(reminder.getInstanceId());
			item.setRemindOn(remindOn);
			item.setTitle(reminder.getTitle());
			item.setDate(reminder.getDate());
			item.setTimezone(reminder.getTimezone());
			
			item.setSnoozedReminderId(dao.getSequence(con).intValue());
			dao.insert(con, item);
			return item;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB Error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OSnoozedReminder> listExpiredSnoozedReminders(DateTime greaterInstant) throws WTException {
		SnoozedReminderDAO dao = SnoozedReminderDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			con.setAutoCommit(false);
			List<OSnoozedReminder> items = dao.selectExpiredForUpdateByInstant(con, greaterInstant);
			for(OSnoozedReminder item : items) {
				dao.delete(con, item.getSnoozedReminderId());
			}
			DbUtils.commitQuietly(con);
			return items;
			
		} catch (Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OServiceStoreEntry> listServiceStoreEntriesByQuery(String serviceId, String context, String query, int max) {
		ServiceStoreEntryDAO sseDao = ServiceStoreEntryDAO.getInstance();
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			if (StringUtils.isBlank(query)) {
				return sseDao.selectKeyValueByLimit(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, max);
			} else {
				String newQuery = StringUtils.upperCase(StringUtils.trim(query));
				return sseDao.selectKeyValueByLikeKeyLimit(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, "%"+newQuery+"%", max);
			}
		
		} catch(SQLException | DAOException ex) {
			logger.error("Error querying servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, query, ex);
			return new ArrayList<>();
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OServiceStoreEntry getServiceStoreEntry(String serviceId, String context, String key) {
		ServiceStoreEntryDAO sseDao = ServiceStoreEntryDAO.getInstance();
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			return sseDao.select(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, key);
		} catch(SQLException | DAOException ex) {
			logger.error("Error querying servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, key, ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addServiceStoreEntry(String serviceId, String context, String key, String value) {
		ServiceStoreEntryDAO sseDao = ServiceStoreEntryDAO.getInstance();
		UserProfileId targetPid = getTargetProfileId();
		OServiceStoreEntry osse = null;
		Connection con = null;
		
		try {
			if (StringUtils.isBlank(value)) return;
			con = WT.getCoreConnection();
			osse = sseDao.select(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, key);
			if (osse != null) {
				sseDao.update(con, osse.getDomainId(), osse.getUserId(), osse.getServiceId(), osse.getContext(), key, value);
			} else {
				osse = new OServiceStoreEntry();
				osse.setDomainId(targetPid.getDomainId());
				osse.setUserId(targetPid.getUserId());
				osse.setServiceId(serviceId);
				osse.setContext(context);
				osse.setKey(key);
				osse.setValue(value);
				osse.setFrequency(1);
				sseDao.insert(con, osse);
			}
		} catch(SQLException | DAOException ex) {
			logger.error("Error adding servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, OServiceStoreEntry.sanitizeKey(key), ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry() {
		ServiceStoreEntryDAO sseDao = ServiceStoreEntryDAO.getInstance();
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			sseDao.deleteByDomainUser(con, targetPid.getDomainId(), targetPid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting servicestore entry [{}]", targetPid, ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(String serviceId) {
		ServiceStoreEntryDAO sseDao = ServiceStoreEntryDAO.getInstance();
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			sseDao.deleteByDomainUserService(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId);
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting servicestore entry [{}, {}]", targetPid, serviceId, ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteServiceStoreEntry(String serviceId, String context, String key) {
		ServiceStoreEntryDAO sseDao = ServiceStoreEntryDAO.getInstance();
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			sseDao.delete(con, targetPid.getDomainId(), targetPid.getUserId(), serviceId, context, key);
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting servicestore entry [{}, {}, {}, {}]", targetPid, serviceId, context, key, ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateMyAutosaveData(String webtopClientId, String serviceId, String context, String key, String value) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			OAutosave data = asdao.select(con, targetPid.getDomainId(), targetPid.getUserId(), webtopClientId, serviceId, context, key);
			
			if(data != null) {
				data.setValue(value);
				asdao.update(con, data);
			} else {
				data = new OAutosave();
				data.setDomainId(targetPid.getDomainId());
				data.setUserId(targetPid.getUserId());
				data.setWebtopClientId(webtopClientId);
				data.setServiceId(serviceId);
				data.setContext(context);
				data.setKey(StringUtils.upperCase(key));
				data.setValue(value);
				asdao.insert(con, data);
			}
			
		} catch(SQLException | DAOException ex) {
			logger.error("Error adding autosave entry [{}, {}, {}, {}]", targetPid, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteOfflineOthersAutosaveData(String notWebtopClientId) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			List<OAutosave> items=listOfflineOthersAutosaveData(notWebtopClientId);
			for(OAutosave item: items) {
				asdao.deleteByKey(con, targetPid.getDomainId(), targetPid.getUserId(),item.getWebtopClientId(),item.getServiceId(),item.getContext(),item.getKey());
			}
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting autosave entry [{}, !{}]", targetPid, notWebtopClientId, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}

	}
	
	public void deleteMyAutosaveData(String webtopClientId) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			asdao.deleteByWebtopClientId(con, targetPid.getDomainId(), targetPid.getUserId(), webtopClientId);
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting autosave entry [{}, {}]", targetPid, webtopClientId, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteMyAutosaveData(String webtopClientId, String serviceId) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			asdao.deleteByService(con, targetPid.getDomainId(), targetPid.getUserId(), webtopClientId, serviceId);
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting autosave entry [{}, {}, {}]", targetPid, webtopClientId, serviceId, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteMyAutosaveData(String webtopClientId, String serviceId, String context) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			asdao.deleteByContext(con, targetPid.getDomainId(), targetPid.getUserId(), webtopClientId, serviceId, context);
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting autosave entry [{}, {}, {}, {}]", targetPid, webtopClientId, serviceId, context, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteMyAutosaveData(String webtopClientId, String serviceId, String context, String key) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			asdao.deleteByKey(con, targetPid.getDomainId(), targetPid.getUserId(), webtopClientId, serviceId, context, key);
		} catch(SQLException | DAOException ex) {
			logger.error("Error deleting autosave entry [{}, {}, {}, {}, {}]", targetPid, webtopClientId, serviceId, context, key, ex);
			
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OAutosave> listMyAutosaveData(String webtopClientId) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			return asdao.selectMineByUserServices(con, targetPid.getDomainId(), targetPid.getUserId(), webtopClientId, listAllowedServices());
		} catch(SQLException | DAOException ex) {
			logger.error("Error selecting autosave entry [{}]", targetPid, ex);
			return new ArrayList<>();
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public boolean hasMyAutosaveData(String webtopClientId) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			return asdao.countMineByUserServices(con, targetPid.getDomainId(), targetPid.getUserId(), webtopClientId, listAllowedServices()) > 0;
		} catch(SQLException | DAOException ex) {
			logger.error("Error selecting autosave entry [{}]", targetPid, ex);
			return false;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public List<OAutosave> listOfflineOthersAutosaveData(String notWebtopClientId) {
		UserProfileId targetPid = getTargetProfileId();
		Connection con = null;
		
		try {
			SessionManager sm=wta.getSessionManager();
			//wta.getSessionManager().isOnline(targetPid, notWebtopClientId);
			con = WT.getCoreConnection();
			AutosaveDAO asdao = AutosaveDAO.getInstance();
			List<OAutosave> data=asdao.selectOthersByUserServices(con, targetPid.getDomainId(), targetPid.getUserId(), notWebtopClientId, listAllowedServices());
			List<OAutosave> rdata=new ArrayList<>();
			for(OAutosave as: data) {
				if (!sm.isOnline(new UserProfileId(as.getDomainId(),as.getUserId()), as.getWebtopClientId()))
					rdata.add(as);
			}
			return rdata;
		} catch(SQLException | DAOException ex) {
			logger.error("Error selecting autosave entry [{}]", targetPid, ex);
			return null;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void autoLearnInternetRecipient(String email) {
		addServiceStoreEntry(SERVICE_ID, "recipients", email, email);
	}
	
	public RecipientsProviderBase getProfileRecipientsProvider(String sourceId) {
		return getProfileRecipientsProviders().get(sourceId);
	}
	
	/**
	 * Returns the available source IDs.
	 * @return
	 * @throws WTException 
	 */
	public List<String> listRecipientProviderSourceIds() throws WTException {
		return new ArrayList<>(getProfileRecipientsProviders().keySet());
	}
	
	/**
	 * Returns a list of recipients beloging to a specified type.
	 * The search will include all available sources; including also the 
	 * automatic ({@link #RECIPIENT_PROVIDER_AUTO_SOURCE_ID}) one used to store
	 * the auto-learn texts, and the ({@link #RECIPIENT_PROVIDER_WEBTOP_SOURCE_ID})
	 * one containing internal webtop users
	 * @param fieldType The desired recipient type.
	 * @param queryText A text to filter out returned results.
	 * @param max Max number of results.
	 * @param builtInProvidersAtTheEnd True add built-in providers (AUTO and WEBTOP) results at the end, false otherwise.
	 * @return
	 * @throws WTException 
	 */
	public List<Recipient> listProviderRecipients(RecipientFieldType fieldType, String queryText, int max, boolean builtInProvidersAtTheEnd, boolean includeAuto, boolean includeWebTop) throws WTException {
		final ArrayList<String> ids = new ArrayList<>();
		if (!builtInProvidersAtTheEnd) {
			if (autoRcptProviderEnabled && includeAuto) ids.add(RECIPIENT_PROVIDER_AUTO_SOURCE_ID);
			if (webtopRcptProviderEnabled && includeWebTop) ids.add(RECIPIENT_PROVIDER_WEBTOP_SOURCE_ID);
		}
		ids.addAll(listRecipientProviderSourceIds());
		if (builtInProvidersAtTheEnd) {
			if (autoRcptProviderEnabled && includeAuto) ids.add(RECIPIENT_PROVIDER_AUTO_SOURCE_ID);
			if (webtopRcptProviderEnabled && includeWebTop) ids.add(RECIPIENT_PROVIDER_WEBTOP_SOURCE_ID);
		}
		return listProviderRecipients(fieldType, ids, queryText, max);
	}
	
	/**
	 * Returns a list of recipients beloging to a specified type.
	 * @param fieldType The desired recipient type.
	 * @param sourceIds A collection of sources in which look for.
	 * @param queryText A text to filter out returned results.
	 * @param max Max number of results.
	 * @return
	 * @throws WTException 
	 */
	public List<Recipient> listProviderRecipients(RecipientFieldType fieldType, Collection<String> sourceIds, String queryText, int max) throws WTException {
		ArrayList<Recipient> items = new ArrayList<>();
		
		int remaining = max;
		for (String soId : sourceIds) {
			List<Recipient> recipients = null;
			if (RECIPIENT_PROVIDER_AUTO_SOURCE_ID.equals(soId)) {
				if (!fieldType.equals(RecipientFieldType.LIST)) {
					recipients = new ArrayList<>();
					//TODO: Find a way to handle other RecipientFieldTypes
					if (fieldType.equals(RecipientFieldType.EMAIL)) {
						final List<OServiceStoreEntry> entries = listServiceStoreEntriesByQuery(SERVICE_ID, "recipients", queryText, remaining);
						for(OServiceStoreEntry entry: entries) {
							final InternetAddress ia = InternetAddressUtils.toInternetAddress(entry.getValue());
							if (ia!=null) recipients.add(new Recipient(RECIPIENT_PROVIDER_AUTO_SOURCE_ID, lookupResource(getLocale(), CoreLocaleKey.INTERNETRECIPIENT_AUTO), RECIPIENT_PROVIDER_AUTO_SOURCE_ID, ia.getPersonal(), ia.getAddress()));
						}
					}
				}
			} else if (RECIPIENT_PROVIDER_WEBTOP_SOURCE_ID.equals(soId)) {
				if (!fieldType.equals(RecipientFieldType.LIST)) {
					WebTopManager wtMgr = wta.getWebTopManager();
					recipients = new ArrayList<>();
					//TODO: Find a way to handle other RecipientFieldTypes
					if (fieldType.equals(RecipientFieldType.EMAIL)) {
						final String domainId = getTargetProfileId().getDomainId();
						for (String userId : wtMgr.listUserIds(domainId, EnabledCond.ENABLED_ONLY)) {
							final UserProfile.Data userData = WT.getUserData(new UserProfileId(domainId, userId));
							if (userData != null) {
								if (StringUtils.containsIgnoreCase(userData.getDisplayName(), queryText) || StringUtils.containsIgnoreCase(userData.getPersonalEmailAddress(), queryText)) {
									recipients.add(
										new Recipient(
											RECIPIENT_PROVIDER_WEBTOP_SOURCE_ID, 
											lookupResource(getLocale(), CoreLocaleKey.INTERNETRECIPIENT_WEBTOP), 
											RECIPIENT_PROVIDER_AUTO_SOURCE_ID, 
											userData.getDisplayName(), 
											userData.getPersonalEmailAddress()
										)
									);
								}
							}
						}
					}
				}
			} else {
				final RecipientsProviderBase provider = getProfileRecipientsProviders().get(soId);
				if (provider == null) continue;
				
				try {
					recipients = provider.getRecipients(fieldType, queryText, remaining);
				} catch(Throwable t) {
					logger.error("Error calling RecipientProvider [{}]", t, soId);
				}
				if (recipients == null) continue;
			}
			
			if (recipients!=null)
				for(Recipient recipient : recipients) {
					remaining--;
					if (remaining < 0) break; 
					recipient.setSource(soId); // Force composed id!
					items.add(recipient);
				}
			if (remaining <= 0) break;
		}
		return items;
	}
	
	/**
	 * Expands a virtualRecipient address into a real set of recipients.
	 * @param virtualRecipientAddress
	 * @return
	 * @throws WTException 
	 */
	public List<Recipient> expandVirtualProviderRecipient(String virtualRecipientAddress) throws WTException {
		ArrayList<Recipient> items = new ArrayList<>();
		VirtualAddress va = new VirtualAddress(virtualRecipientAddress);
		
		for (String soId : listRecipientProviderSourceIds()) {
			final RecipientsProviderBase provider = getProfileRecipientsProviders().get(soId);
			if (provider == null) continue;
			if (!StringUtils.isBlank(va.getDomain()) && !StringUtils.startsWith(soId, va.getDomain())) {
				continue;
			}
			
			List<Recipient> recipients = null;
			try {
				recipients = provider.expandToRecipients(va.getLocal());
			} catch(Throwable t) {
				logger.error("Error calling RecipientProvider [{}]", t, soId);
			}
			if (recipients == null) continue;
			for (Recipient recipient : recipients) {
				recipient.setSource(soId);
				items.add(recipient);
			}
		}
		return items;
	}
	
	public List<SyncDevice> listZPushDevices() throws WTException {
		try {
			WebTopManager wtMgr = wta.getWebTopManager();
			ZPushManager zpush = createZPushManager();
			
			boolean noFilter = false, domainMatch = false;
			String match = null;
			UserProfileId targetPid = getTargetProfileId();
			if (RunContext.isSysAdmin()) {
				if (UserProfileId.isWildcardUser(targetPid)) {
					domainMatch = true;
					match = "@" + wtMgr.domainIdToAuthDomainName(targetPid.getDomainId());
				} else {
					match = wtMgr.toAuthProfileId(targetPid).toString();
				}
			} else {
				match = wtMgr.toAuthProfileId(targetPid).toString();
			}
			
			ArrayList<SyncDevice> devices = new ArrayList<>();
			List<ZPushManager.LastsyncRecord> recs = zpush.listDevices();
			for (ZPushManager.LastsyncRecord rec : recs) {
				if (noFilter || StringUtils.equalsIgnoreCase(rec.synchronizedUser, match) || (domainMatch && StringUtils.endsWithIgnoreCase(rec.synchronizedUser, match))) {
					devices.add(new SyncDevice(rec.device, rec.synchronizedUser, rec.lastSyncTime));
				}
			}
			
			return devices;
			
		} catch(Exception ex) {
			logger.error("Error listing zpush devices",ex);
			throw new WTException(ex);
		}
	}
	
	public void deleteZPushDevice(String deviceId) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		try {
			WebTopManager wtMgr = wta.getWebTopManager();
			ZPushManager zpush = createZPushManager();
			if (RunContext.isSysAdmin()) {
				if (UserProfileId.isWildcardUser(targetPid)) {
					zpush.removeDevice(deviceId);
				} else {
					zpush.removeUserDevice(wtMgr.toAuthProfileId(targetPid).toString(), deviceId);
				}
			} else {
				zpush.removeUserDevice(wtMgr.toAuthProfileId(targetPid).toString(), deviceId);
			}
			
		} catch(Exception ex) {
			throw new WTException(ex);
		}
	}
	
	public String getZPushDetailedInfo(String deviceId, String lineSep) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		try {
			WebTopManager wtMgr = wta.getWebTopManager();
			ZPushManager zpush = createZPushManager();
			ensureProfile(true);
			return zpush.getDetailedInfo(deviceId, wtMgr.toAuthProfileId(targetPid).toString(), lineSep);
			
		} catch(Exception ex) {
			throw new WTException(ex);
		}	
	}
	
	public void eraseData(boolean deep) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		Connection con = null;
		
		UserProfileId pid = getTargetProfileId();
		//TODO: controllo permessi
		
		try {
			con = WT.getConnection(SERVICE_ID);
			tagDao.deleteByProfile(con, pid.getDomainId(), pid.getUserId());
			
			eventManager.fireEvent(new TagChangedEvent(this, ChangedEvent.Operation.DELETE));
//			if (isAuditEnabled()) {
//				auditLogWrite(AuditContext.TAG, AuditAction.DELETE, "*", pid);
//			}
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private ZPushManager createZPushManager() throws WTException {
		CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, "*");
		try {
			URI uri = new URI(css.getDevicesSyncShellUri());
			return new ZPushManager(css.getPhpPath(), css.getZPushPath(), uri);
		} catch(URISyntaxException ex) {
			throw new WTException(ex, "Invalid URI");
		}
	}
	
	private Activity doActivityUpdate(boolean insert, Connection con, Activity act) throws WTException {
		ActivityDAO dao = ActivityDAO.getInstance();
		
		OActivity oact = createOActivity(act);
		if (oact.getDomainId() == null) oact.setDomainId(getTargetProfileId().getDomainId());
		
		int ret = -1;
		if (insert) {
			oact.setActivityId(dao.getSequence(con).intValue());
			oact.setRevisionStatus(EnumUtils.toSerializedName(Activity.RevisionStatus.MODIFIED));
			ret = dao.insert(con, oact);
		} else {
			ret = dao.update(con, oact);
		}
		
		return (ret == 1) ? ManagerUtils.createActivity(oact) : null;
	}
	
	private Causal doCausalUpdate(boolean insert, Connection con, Causal cau) throws WTException {
		CausalDAO dao = CausalDAO.getInstance();
		
		OCausal ocau = createOCausal(cau);
		if (ocau.getDomainId() == null) ocau.setDomainId(getTargetProfileId().getDomainId());
		
		int ret = -1;
		if (insert) {
			ocau.setCausalId(dao.getSequence(con).intValue());
			ocau.setRevisionStatus(EnumUtils.toSerializedName(Activity.RevisionStatus.MODIFIED));
			ret = dao.insert(con, ocau);
		} else {
			ret = dao.update(con, ocau);
		}
		
		return (ret == 1) ? ManagerUtils.createCausal(ocau) : null;
	}
	
	private Tag doTagUpdate(boolean insert, Connection con, Tag tag) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		
		OTag otag = ManagerUtils.createOTag(tag, getTargetProfileId().getUserId());
		if (otag.getDomainId() == null) otag.setDomainId(getTargetProfileId().getDomainId());
		
		int ret = -1;
		if (insert) {
			otag.setTagId(tagDao.generateTagId());
			ret = tagDao.insert(con, otag);
		} else {
			ret = tagDao.update(con, otag);
		}
		
		return (ret == 1) ? ManagerUtils.fillTag(new Tag(), otag) : null;
	}
	
	private boolean doTagDelete(Connection con, String domainId, String tagId) throws WTException {
		TagDAO tagDao = TagDAO.getInstance();
		return tagDao.deleteByDomainId(con, domainId, tagId) == 1;
	}
	
	private OActivity createOActivity(Activity cau) {
		if (cau == null) return null;
		OActivity ocau = new OActivity();
		ocau.setActivityId(cau.getActivityId());
		ocau.setDomainId(cau.getDomainId());
		ocau.setUserId(cau.getUserId());
		ocau.setRevisionStatus(EnumUtils.toSerializedName(cau.getRevisionStatus()));
		ocau.setDescription(cau.getDescription());
		ocau.setReadOnly(cau.getReadOnly());
		ocau.setExternalId(cau.getExternalId());
		return ocau;
	}
	
	
	
	private OCausal createOCausal(Causal cau) {
		if (cau == null) return null;
		OCausal ocau = new OCausal();
		ocau.setCausalId(cau.getCausalId());
		ocau.setDomainId(cau.getDomainId());
		ocau.setUserId(cau.getUserId());
		ocau.setMasterDataId(cau.getMasterDataId());
		ocau.setRevisionStatus(EnumUtils.toSerializedName(cau.getRevisionStatus()));
		ocau.setDescription(cau.getDescription());
		ocau.setReadOnly(cau.getReadOnly());
		ocau.setExternalId(cau.getExternalId());
		return ocau;
	}
	
	private CausalExt createCausalExt(VCausal vcau) {
		if (vcau == null) return null;
		CausalExt cau = new CausalExt();
		cau.setCausalId(vcau.getCausalId());
		cau.setDomainId(vcau.getDomainId());
		cau.setUserId(vcau.getUserId());
		cau.setMasterDataId(vcau.getMasterDataId());
		cau.setRevisionStatus(EnumUtils.forSerializedName(vcau.getRevisionStatus(), Causal.RevisionStatus.class));
		cau.setDescription(vcau.getDescription());
		cau.setReadOnly(vcau.getReadOnly());
		cau.setExternalId(vcau.getExternalId());
		cau.setMasterDataDescription(vcau.getMasterDataDescription());
		return cau;
	}
	
	private OIMChat createOIMChat(IMChat cha) {
		if (cha == null) return null;
		OIMChat ocha = new OIMChat();
		ocha.setId(cha.getId());
		ocha.setDomainId(cha.getDomainId());
		ocha.setUserId(cha.getUserId());
		ocha.setChatJid(cha.getChatJid());
		ocha.setRevisionStatus(EnumUtils.toSerializedName(cha.getRevisionStatus()));
		ocha.setRevisionTimestamp(cha.getRevisionTimestamp());
		ocha.setOwnerJid(cha.getOwnerJid());
		ocha.setName(cha.getName());
		ocha.setIsGroupChat(cha.getIsGroupChat());
		ocha.setLastSeenActivity(cha.getLastSeenActivity());
		ocha.setWithJid(cha.getWithJid());
		return ocha;
	}
	
	private IMChat createIMChat(OIMChat ocha) {
		if (ocha == null) return null;
		IMChat cha = new IMChat();
		cha.setId(ocha.getId());
		cha.setDomainId(ocha.getDomainId());
		cha.setUserId(ocha.getUserId());
		cha.setRevisionStatus(EnumUtils.forSerializedName(ocha.getRevisionStatus(), IMChat.RevisionStatus.class));
		cha.setRevisionTimestamp(ocha.getRevisionTimestamp());
		cha.setChatJid(ocha.getChatJid());
		cha.setOwnerJid(ocha.getOwnerJid());
		cha.setName(ocha.getName());
		cha.setIsGroupChat(ocha.getIsGroupChat());
		cha.setLastSeenActivity(ocha.getLastSeenActivity());
		cha.setWithJid(ocha.getWithJid());
		return cha;
	}
	
	private OIMMessage createOIMMessage(IMMessage mes) {
		if (mes == null) return null;
		OIMMessage omes = new OIMMessage();
		omes.setId(mes.getId());
		omes.setDomainId(mes.getDomainId());
		omes.setUserId(mes.getUserId());
		omes.setChatJid(mes.getChatJid());
		omes.setSenderJid(mes.getSenderJid());
		omes.setSenderResource(mes.getSenderResource());
		omes.setTimestamp(mes.getTimestamp());
		omes.setDeliveryTimestamp(mes.getDeliveryTimestamp());
		omes.setAction(EnumUtils.toSerializedName(mes.getAction()));
		omes.setText(mes.getText());
		omes.setData(mes.getData());
		omes.setMessageUid(mes.getMessageUid());
		omes.setStanzaId(mes.getStanzaId());
		return omes;
	}
	
	private IMMessage createIMMessage(OIMMessage omes) {
		if (omes == null) return null;
		IMMessage mes = new IMMessage();
		mes.setId(omes.getId());
		mes.setDomainId(omes.getDomainId());
		mes.setUserId(omes.getUserId());
		mes.setChatJid(omes.getChatJid());
		mes.setSenderJid(omes.getSenderJid());
		mes.setSenderResource(omes.getSenderResource());
		mes.setTimestamp(omes.getTimestamp());
		mes.setDeliveryTimestamp(omes.getDeliveryTimestamp());
		mes.setAction(EnumUtils.forSerializedName(omes.getAction(), IMMessage.Action.class));
		mes.setText(omes.getText());
		mes.setData(omes.getData());
		mes.setMessageUid(omes.getMessageUid());
		mes.setStanzaId(omes.getStanzaId());
		return mes;
	}
	
	private DateTime createRevisionTimestamp() {
		return DateTime.now(DateTimeZone.UTC);
	}
	
	private enum AuditContext {
		ACTIVITY, CAUSAL, TAG, CUSTOMPANEL, CUSTOMFIELD
	}
	
	private enum AuditAction {
		CREATE, UPDATE, DELETE, MOVE
	}
	
	/**
	 * @deprecated move to BitFlagsEnum interface
	 */
	@Deprecated
	public static enum CustomFieldListOptions implements BitFlagEnum {
		SEARCHABLE(1), PREVIEWABLE(2);
		
		private int value = 0;
		private CustomFieldListOptions(int value) { this.value = value; }
		@Override
		public int value() { return this.value; }
	}
}
