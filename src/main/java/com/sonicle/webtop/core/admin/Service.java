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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.license4j.ActivationStatus;
import com.license4j.ValidationStatus;
import com.sonicle.commons.AlgoUtils.MD5HashBuilder;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.beans.PageInfo;
import com.sonicle.commons.beans.SortInfo;
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.commons.time.DateTimeRange;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CId;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.bean.QueryObj;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.commons.web.json.extjs.GridMetadata;
import com.sonicle.commons.web.json.extjs.ResultMeta;
import com.sonicle.commons.web.json.extjs.SortMeta;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreServiceSettings;
import com.sonicle.webtop.core.CoreSettings.LauncherLink;
import com.sonicle.webtop.core.admin.bol.js.JsDataSource;
import com.sonicle.webtop.core.admin.bol.js.JsDataSourceQuery;
import com.sonicle.webtop.core.admin.bol.js.JsDomainAccessLog;
import com.sonicle.webtop.core.admin.bol.js.JsDomainAccessLogDetail;
import com.sonicle.webtop.core.admin.bol.js.JsGridResource;
import com.sonicle.webtop.core.admin.bol.js.JsGridRole;
import com.sonicle.webtop.core.admin.bol.js.JsGridUser;
import com.sonicle.webtop.core.admin.bol.js.JsDomainLauncherLink;
import com.sonicle.webtop.core.admin.bol.js.JsGridDomainDataSource;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.CorePrivateEnvironment;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.app.sdk.WTIntegrityException;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.config.bol.OPecBridgeFetcher;
import com.sonicle.webtop.core.config.bol.OPecBridgeRelay;
import com.sonicle.webtop.core.bol.ORunnableUpgradeStatement;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.OUpgradeStatement;
import com.sonicle.webtop.core.bol.OUser;
import com.sonicle.webtop.core.bol.js.JsDomain;
import com.sonicle.webtop.core.admin.bol.js.JsGridDomainLicense;
import com.sonicle.webtop.core.admin.bol.js.JsGridGroup;
import com.sonicle.webtop.core.admin.bol.js.JsGridLogger;
import com.sonicle.webtop.core.admin.bol.js.JsGroup;
import com.sonicle.webtop.core.admin.bol.js.JsRole;
import com.sonicle.webtop.core.admin.bol.js.JsUser;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WebTopProps;
import com.sonicle.webtop.core.app.model.DirectoryUser;
import com.sonicle.webtop.core.app.model.Domain;
import com.sonicle.webtop.core.app.model.DomainGetOption;
import com.sonicle.webtop.core.app.model.DomainUpdateOption;
import com.sonicle.webtop.core.app.model.EnabledCond;
import com.sonicle.webtop.core.app.model.GenericSubject;
import com.sonicle.webtop.core.app.model.Group;
import com.sonicle.webtop.core.app.model.GroupGetOption;
import com.sonicle.webtop.core.app.model.GroupUpdateOption;
import com.sonicle.webtop.core.app.model.LicenseBase;
import com.sonicle.webtop.core.app.model.LicenseListOption;
import com.sonicle.webtop.core.app.sdk.Result;
import com.sonicle.webtop.core.app.sdk.ResultVoid;
import com.sonicle.webtop.core.app.sdk.WTConnectionException;
import com.sonicle.webtop.core.app.sdk.WTLicenseActivationException;
import com.sonicle.webtop.core.app.sdk.WTLicenseException;
import com.sonicle.webtop.core.app.sdk.WTLicenseMismatchException;
import com.sonicle.webtop.core.app.sdk.WTLicenseValidationException;
import com.sonicle.webtop.core.app.sdk.WTUnsupportedOperationException;
import com.sonicle.webtop.core.bol.js.JsGridPecBridgeFetcher;
import com.sonicle.webtop.core.bol.js.JsGridPecBridgeRelay;
import com.sonicle.webtop.core.bol.js.JsGridUpgradeRow;
import com.sonicle.webtop.core.bol.js.JsPecBridgeFetcher;
import com.sonicle.webtop.core.bol.js.JsPecBridgeRelay;
import com.sonicle.webtop.core.bol.js.JsResource;
import com.sonicle.webtop.core.bol.js.JsRoleLkp;
import com.sonicle.webtop.core.bol.js.JsAclSubjectLkp;
import com.sonicle.webtop.core.bol.js.JsServiceProductLkp;
import com.sonicle.webtop.core.bol.js.JsSettingEntry;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.model.UserOptionsServiceData;
import com.sonicle.webtop.core.model.DataSource;
import com.sonicle.webtop.core.model.DataSourceBase;
import com.sonicle.webtop.core.model.DataSourcePooled;
import com.sonicle.webtop.core.model.DataSourceQuery;
import com.sonicle.webtop.core.model.DataSourceType;
import com.sonicle.webtop.core.model.DomainAccessLog;
import com.sonicle.webtop.core.model.DomainAccessLogDetail;
import com.sonicle.webtop.core.model.DomainAccessLogQuery;
import com.sonicle.webtop.core.model.ListDomainAccessLogDetailResult;
import com.sonicle.webtop.core.model.ListDomainAccessLogResult;
import com.sonicle.webtop.core.model.LoggerEntry;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.app.model.Resource;
import com.sonicle.webtop.core.app.model.ResourceGetOption;
import com.sonicle.webtop.core.app.model.ResourceUpdateOption;
import com.sonicle.webtop.core.app.model.Role;
import com.sonicle.webtop.core.app.model.RoleGetOption;
import com.sonicle.webtop.core.app.model.RoleUpdateOption;
import com.sonicle.webtop.core.app.model.User;
import com.sonicle.webtop.core.app.model.UserGetOption;
import com.sonicle.webtop.core.app.model.UserUpdateOption;
import com.sonicle.webtop.core.bol.js.JsDomainPwdPolicies;
import com.sonicle.webtop.core.bol.js.JsSubjectLkp;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.model.ServiceLicense;
import com.sonicle.webtop.core.model.SettingEntry;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.sdk.ServiceManifest.Product;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.versioning.IgnoreErrorsAnnotationLine;
import com.sonicle.webtop.core.versioning.RequireAdminAnnotationLine;
import jakarta.mail.internet.InternetAddress;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.util.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	private static final Logger logger = WT.getLogger(Service.class);
	private CoreManager core;
	private CoreAdminManager coreadm;
	private final Object lock1 = new Object();
	private DbUpgraderEnvironment upgradeEnvironment = null;
	
	@Override
	public void initialize() throws Exception {
		core = WT.getCoreManager();
		coreadm = (CoreAdminManager)WT.getServiceManager(SERVICE_ID);
	}

	@Override
	public void cleanup() throws Exception {
		core = null;
	}
	
	private WebTopApp getWta() {
		return ((CorePrivateEnvironment)getEnv()).getApp();
	}
	
	private DbUpgraderEnvironment getDbUpgraderEnvironment() throws WTException {
		synchronized (lock1) {
			if (this.upgradeEnvironment == null) {
				this.upgradeEnvironment = new DbUpgraderEnvironment(coreadm.listLastUpgradeStatements());
			}
			return this.upgradeEnvironment;
		}	
	}
	
	private ExtTreeNode createTreeNode(String id, String type, String text, boolean leaf, String iconClass) {
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_type", type);
		node.setIconClass(iconClass);
		return node;
	}
	
	public void processGetUserOptionServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String pid = ServletUtils.getStringParameter(request, "pid", true);
			
			UserProfileId targetPid = new UserProfileId(pid);
			CoreManager xcore = WT.getCoreManager(true, targetPid);
			List<UserOptionsServiceData> items = xcore.getAllowedUserOptionServices();
			new JsonResult(items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in GetUserOptionServices", ex);
			new JsonResult(false, "Unable to get option services").printTo(out);
		}
	}
	
	public void processLookupSubjects(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean users = ServletUtils.getBooleanParameter(request, "users", false);
			boolean resources = ServletUtils.getBooleanParameter(request, "resources", false);
			boolean groups = ServletUtils.getBooleanParameter(request, "groups", false);
			boolean roles = ServletUtils.getBooleanParameter(request, "roles", false);
			boolean fullId = ServletUtils.getBooleanParameter(request, "fullId", false);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			List<JsSubjectLkp> items = new ArrayList<>();
			if (wildcard) items.add(JsSubjectLkp.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			for (GenericSubject subject : admMgr.listSubjects(users, resources, groups, roles, true).values()) {
				InternetAddress personalAddress = null;
				if (GenericSubject.Type.USER.equals(subject.getType()) || GenericSubject.Type.RESOURCE.equals(subject.getType())) {
					personalAddress = WT.getProfilePersonalAddress(new UserProfileId(subject.getDomainId(), subject.getName()));
				}
				items.add(new JsSubjectLkp(fullId ? subject.getProfileId().toString() : subject.getName(), subject.getName(), subject.getDisplayName(), (personalAddress != null) ? personalAddress.getAddress() : null, subject.getType()));
			}
			new JsonResult(items).printTo(out);
			
		} catch (Throwable t) {
			logger.error("Error in LookupSubjects", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processLookupAclSubjects(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean users = ServletUtils.getBooleanParameter(request, "users", false);
			boolean resources = ServletUtils.getBooleanParameter(request, "resources", false);
			boolean groups = ServletUtils.getBooleanParameter(request, "groups", false);
			boolean roles = ServletUtils.getBooleanParameter(request, "roles", false);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			
			List<JsAclSubjectLkp> items = new ArrayList<>();
			if (wildcard) items.add(JsAclSubjectLkp.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			for (GenericSubject subject : admMgr.listSubjects(users, resources, groups, roles, false).values()) {
				items.add(new JsAclSubjectLkp(subject.getSid(), subject.getDisplayName(), subject.getName(), EnumUtils.toSerializedName(subject.getType())));
			}
			new JsonResult(items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupAclSubjects", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	
	private static final String NID_SETTINGS = "settings";
	private static final String NID_DOMAINS = "domains";
	private static final String NID_GROUPS = "groups";
	private static final String NID_USERS = "users";
	private static final String NID_ROLES = "roles";
	private static final String NID_RESOURCES = "resources";
	private static final String NID_LAUNCHERLINKS = "launcherlinks";
	private static final String NID_DATASOURCES = "datasources";
	private static final String NID_LICENSES = "licenses";
	private static final String NID_PECBRIDGE = "pecbridge";
	private static final String NID_DBUPGRADER = "dbupgrader";
	private static final String NID_LOGS = "logs";
	private static final String NID_VIEWER = "viewer";
	private static final String NID_AUDIT = "audit";
	private static final String NID_ACCESSLOG = "accesslog";
	
	private ExtTreeNode createDomainNode(String parentId, Domain domain, String dirScheme, boolean dirCapPasswordWrite, boolean dirCapUsersWrite) {
		CId cid = CId.build(parentId, domain.getDomainId());
		ExtTreeNode node = new ExtTreeNode(cid.toString(), domain.getDisplayName(), false);
		node.setIconClass(domain.getEnabled() ? "wtadm-icon-domain" : "wtadm-icon-domain-disabled");
		node.put("_type", "domain");
		node.put("_domainId", domain.getDomainId());
		node.put("_authDomainName", domain.getAuthDomainName());
		node.put("_domainName", domain.getDomainName());
		node.put("_dirScheme", dirScheme);
		node.put("_dirCapPasswordWrite", dirCapPasswordWrite);
		node.put("_dirCapUsersWrite", dirCapUsersWrite);
		return node;
	}
	
	private ExtTreeNode createDomainNode(String parentId, ODomain domain, String dirScheme, boolean dirCapPasswordWrite, boolean dirCapUsersWrite) {
		CompositeId cid = new CompositeId(parentId, domain.getDomainId());
		ExtTreeNode node = new ExtTreeNode(cid.toString(), domain.getDescription(), false);
		node.setIconClass(domain.getEnabled() ? "wtadm-icon-domain" : "wtadm-icon-domain-disabled");
		node.put("_type", "domain");
		node.put("_domainId", domain.getDomainId());
		//TODO: separate email domain from auth domain
		node.put("_authDomain", domain.getInternetName());
		node.put("_emailDomain", domain.getInternetName());
		node.put("_dirScheme", dirScheme);
		node.put("_dirCapPasswordWrite", dirCapPasswordWrite);
		node.put("_dirCapUsersWrite", dirCapUsersWrite);
		return node;
	}
	
	private ExtTreeNode createDomainChildNode(String parentId, String id, String type, String iconClass, String domainId, Boolean dirCapPasswordWrite, Boolean dirCapUsersWrite) {
		CompositeId cid = new CompositeId(parentId, id);
		ExtTreeNode node = new ExtTreeNode(cid.toString(), null, true);
		node.setIconClass(iconClass);
		node.put("_type", type);
		node.put("_domainId", domainId);
		node.put("_dirCapPasswordWrite", dirCapPasswordWrite);
		node.put("_dirCapUsersWrite", dirCapUsersWrite);
		return node;
	}
	
	public void processManageAdminTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String nodeId = ServletUtils.getStringParameter(request, "node", true);
				
				if (nodeId.equals("root")) {
					final boolean sysAdmin = RunContext.isSysAdmin();
					if (sysAdmin) children.add(createTreeNode(NID_SETTINGS, "settings", null, true, "wtadm-icon-settings"));
					children.add(createTreeNode(NID_DOMAINS, "domains", null, false, "wtadm-icon-domains"));
					if (sysAdmin) children.add(createTreeNode(NID_DBUPGRADER, "dbupgrader", null, true, "wtadm-icon-dbUpgrader"));
					if (sysAdmin) children.add(createTreeNode(NID_LOGS, "logging", null, false, "wtadm-icon-logging"));
					
				} else {
					CId cid = new CId(nodeId);
					if (cid.getToken(0).equals(NID_DOMAINS)) {
						if (!cid.hasToken(1)) { // Domain nodes
							for (Domain domain : coreadm.listDomains(EnabledCond.ANY_STATE).values()) {
								final String scheme = domain.getDirScheme();
								AbstractDirectory dir = coreadm.getAuthDirectoryByScheme(scheme);
								boolean dirCapPasswordWrite = dir.hasCapability(DirectoryCapability.PASSWORD_WRITE);
								boolean dirCapUsersWrite = dir.hasCapability(DirectoryCapability.USERS_WRITE);
								children.add(createDomainNode(nodeId, domain, scheme, dirCapPasswordWrite, dirCapUsersWrite));
							}
							
						} else if (cid.hasToken(2)) {
							if (cid.getToken(2).equals(NID_AUDIT)) {
								// domain|<domain-id>|audit
								children.add(createDomainChildNode(nodeId, NID_ACCESSLOG, "daccesslog", "wtadm-icon-accesslog", cid.getToken(1), null, null));
							}
						} else { // Single Domain node
							String domainId = cid.getToken(1);
							
							children.add(createDomainChildNode(nodeId, NID_SETTINGS, "dsettings", "wtadm-icon-settings", null, null, null));
							children.add(createDomainChildNode(nodeId, NID_GROUPS, "dgroups", "wtadm-icon-groups", null, null, null));
							children.add(createDomainChildNode(nodeId, NID_USERS, "dusers", "wtadm-icon-users", null, null, null));
							children.add(createDomainChildNode(nodeId, NID_RESOURCES, "dresources", "wt-icon-resources", null, null, null));
							children.add(createDomainChildNode(nodeId, NID_ROLES, "droles", "wtadm-icon-roles", null, null, null));
							children.add(createDomainChildNode(nodeId, NID_LICENSES, "dlicenses", "wtadm-icon-licenses", null, null, null));
							children.add(createDomainChildNode(nodeId, NID_DATASOURCES, "ddatasources", "wtadm-icon-dataSources", null, null, null));
							children.add(createDomainChildNode(nodeId, NID_LAUNCHERLINKS, "dlauncherlinks", "wtadm-icon-launcherLinks", null, null, null));
							
							CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, domainId);
							if (css.getHasPecBridgeManagement()) {
								children.add(createDomainChildNode(nodeId, NID_PECBRIDGE, "dpecbridge", "wtadm-icon-pecBridge", null, null, null));
							}
							children.add(createTreeNode(CId.build(nodeId, NID_AUDIT).toString(), "daudit", null, false, "wtadm-icon-audit"));
						}
					} else if (cid.getToken(0).equals(NID_LOGS)) {
						if (!cid.hasToken(1)) {
							children.add(createTreeNode(CId.build(NID_LOGS, NID_VIEWER).toString(), "logsviewer", null, true, "wtadm-icon-logViewer"));
						}
					}
				}
				new JsonResult("children", children).printTo(out);
			}
		} catch(Exception ex) {
			logger.error("Error in ManageStoresTree", ex);
		}
	}
	
	public void processManageSystemSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				List<SettingEntry> items = coreadm.listSystemSettings(false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				PayloadAsList<JsSettingEntry.List> pl = ServletUtils.getPayloadAsList(request, JsSettingEntry.List.class);
				SettingEntry setting = pl.data.get(0);
				
				if (!coreadm.updateSystemSetting(setting.getServiceId(), setting.getKey(), setting.getValue())) {
					throw new WTException("Cannot insert setting [{}, {}]", setting.getServiceId(), setting.getKey());
				} else {
					//FIXME: Evaluate to create new field in Domain data for public.url
					if (CoreManifest.ID.equals(setting.getServiceId()) && "public.url".equals(setting.getKey())) {
						coreadm.refreshDomainCache();
					}
				}
				
				OSettingDb info = coreadm.getSettingInfo(setting.getServiceId(), setting.getKey());
				if(info != null) {
					setting = new SettingEntry(setting.getServiceId(), setting.getKey(), setting.getValue(), info.getType(), info.getHelp());
				} else {
					setting = new SettingEntry(setting.getServiceId(), setting.getKey(), setting.getValue(), null, null);
				}
				new JsonResult(setting).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsSettingEntry.List> pl = ServletUtils.getPayloadAsList(request, JsSettingEntry.List.class);
				SettingEntry setting = pl.data.get(0);
				
				final CId cid = new CId(setting.getId(), 2);
				final String sid = cid.getToken(0);
				final String key = cid.getToken(1);

				if (!coreadm.updateSystemSetting(sid, setting.getKey(), setting.getValue())) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				} else {
					//FIXME: Evaluate to create new field in Domain data for public.url
					if (CoreManifest.ID.equals(sid) && "public.url".equals(key)) {
						coreadm.refreshDomainCache();
					}
				}
				if (!StringUtils.equals(key, setting.getKey())) {
					coreadm.deleteSystemSetting(sid, key);
				}
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				PayloadAsList<JsSettingEntry.List> pl = ServletUtils.getPayloadAsList(request, JsSettingEntry.List.class);
				SettingEntry setting = pl.data.get(0);
				
				final CId cid = new CId(setting.getId(), 2);
				final String sid = cid.getToken(0);
				final String key = cid.getToken(1);
				
				if (!coreadm.deleteSystemSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{}, {}]", sid, key);
				} else {
					//FIXME: Evaluate to create new field in Domain data for public.url
					if (CoreManifest.ID.equals(sid) && "public.url".equals(key)) {
						coreadm.refreshDomainCache();
					}
				}
				new JsonResult().printTo(out);
				
			} else if ("cleanup".equals(crud)) {
				coreadm.cleanupSettingsCache();
				coreadm.cleanupDomainSettingsCache();
				new JsonResult().printTo(out);
			}
			
		} catch(Throwable t) {
			logger.error("Error in ManageSystemSettings", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processManageDomain(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(id));
				Domain item = admMgr.getDomain(DomainGetOption.internalDefaultFlags());
				new JsonResult(new JsDomain(item)).printTo(out);
				
			} else if (Crud.CREATE.equals(crud)) {
				Payload<MapItem, JsDomain> pl = ServletUtils.getPayload(request, JsDomain.class);
				
				CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.getSysAdminProfileId());
				Result<Domain> result = admMgr.addDomain(pl.data.domainId, JsDomain.createDomainForAdd(pl.data));
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsDomain> pl = ServletUtils.getPayload(request, JsDomain.class);
				
				CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(pl.data.domainId));
				BitFlags<DomainUpdateOption> options = DomainUpdateOption.internalDefaultFlags();
				JsDomain.CreateForUpdateResult forUpate = JsDomain.createDomainForUpdate(pl.data);
				if (forUpate.passwordChanged) options.set(DomainUpdateOption.DIRECTORY_PASSWORD);
				Result<Domain> result = admMgr.updateDomain(forUpate.item, options);
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.DELETE.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				boolean deep = ServletUtils.getBooleanParameter(request, "deep", true);
				
				CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(id));
				ResultVoid result = admMgr.deleteDomain(deep);
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if ("check".equals(crud)) {
				String domain = ServletUtils.getStringParameter(request, "domain", false);
				
				CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.getSysAdminProfileId());
				boolean available = admMgr.checkDomainIdAvailability(domain);
				new JsonResult(available).printTo(out);
				
			} else if ("init".equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(id));
				ResultVoid result = admMgr.initDomain();
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{domain.info.init.okWithExceptions@com.sonicle.webtop.core.admin}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if ("policies".equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				short levenThres = WebTopProps.getWTDirectorySimilarityLevenThres(WT.getProperties());
				short tokenSize = WebTopProps.getWTDirectorySimilarityTokenSize(WT.getProperties());
				CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(id));
				JsDomainPwdPolicies item = new JsDomainPwdPolicies(levenThres, tokenSize, admMgr.getDomainPasswordPolicies());
				new JsonResult(item).printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomain", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				List<SettingEntry> items = admMgr.listDomainSettings(false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				PayloadAsList<JsSettingEntry.List> pl = ServletUtils.getPayloadAsList(request, JsSettingEntry.List.class);
				SettingEntry setting = pl.data.get(0);
				
				if (!admMgr.updateDomainSetting(setting.getServiceId(), setting.getKey(), setting.getValue())) {
					throw new WTException("Cannot insert setting [{0}, {1}]", setting.getServiceId(), setting.getKey());
				} else {
					//FIXME: Evaluate to create new field in Domain data for public.url
					if (CoreManifest.ID.equals(setting.getServiceId()) && "public.url".equals(setting.getKey())) {
						coreadm.refreshDomainCache();
					}
				}
				setting = new SettingEntry(setting.getServiceId(), setting.getKey(), setting.getValue(), null, null);
				new JsonResult(setting).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsSettingEntry.List> pl = ServletUtils.getPayloadAsList(request, JsSettingEntry.List.class);
				SettingEntry setting = pl.data.get(0);
				
				final CId cid = new CId(setting.getId(), 2);
				final String sid = cid.getToken(0);
				final String key = cid.getToken(1);
				
				if (!admMgr.updateDomainSetting(sid, setting.getKey(), setting.getValue())) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				}
				if (!StringUtils.equals(key, setting.getKey())) {
					admMgr.deleteDomainSetting(sid, key);
				}
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				PayloadAsList<JsSettingEntry.List> pl = ServletUtils.getPayloadAsList(request, JsSettingEntry.List.class);
				SettingEntry setting = pl.data.get(0);
				
				final CId cid = new CId(setting.getId(), 2);
				final String sid = cid.getToken(0);
				final String key = cid.getToken(1);

				if (!admMgr.deleteDomainSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{0}, {1}]", sid, key);
				}
				new JsonResult().printTo(out);
				
			} else if ("cleanup".equals(crud)) {
				boolean users = ServletUtils.getBooleanParameter(request, "users", false);
				if (users) {
					admMgr.cleanupUserSettingsCache();
				} else {
					admMgr.cleanupDomainSettingsCache();
				}
				new JsonResult().printTo(out);
			}
			
		} catch(Throwable t) {
			logger.error("Error in ManageSettings", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processManageDomainGroups(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				List<JsGridGroup> items = new ArrayList<>();
				for (Group group : admMgr.listGroups().values()) {
					items.add(new JsGridGroup(group));
				}
				new JsonResult(items).printTo(out);
				
			} else {
				throw new WTUnsupportedOperationException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainGroups", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainGroup(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				Group item = admMgr.getGroup(id, GroupGetOption.internalDefaultFlags());
				new JsonResult(new JsGroup(item)).printTo(out);
				
			} else if (Crud.CREATE.equals(crud)) {
				Payload<MapItem, JsGroup> pl = ServletUtils.getPayload(request, JsGroup.class);
				
				Result<Group> result = admMgr.addGroup(pl.data.groupId, JsGroup.createGroupForAdd(pl.data), GroupUpdateOption.internalDefaultFlags());
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsGroup> pl = ServletUtils.getPayload(request, JsGroup.class);
				
				ResultVoid result = admMgr.updateGroup(pl.data.groupId, JsGroup.createGroupForUpdate(pl.data), GroupUpdateOption.internalDefaultFlags());
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.DELETE.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				ResultVoid result = admMgr.deleteGroup(id);
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if ("check".equals(crud)) {
				String group = ServletUtils.getStringParameter(request, "group", false);
				
				boolean available = admMgr.checkGroupIdAvailability(group);
				new JsonResult(available).printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainGroup", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				List<JsGridUser> items = new ArrayList<>();
				for (DirectoryUser directoryUser : admMgr.listDirectoryUsers()) {
					items.add(new JsGridUser(directoryUser));
				}
				new JsonResult(items).printTo(out);
				
			} else if ("enable".equals(crud) || "disable".equals(crud)) {
				ServletUtils.StringArray userIds = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				boolean enabled = "enable".equals(crud);
				
				for (String userId : userIds) {
					admMgr.updateUserStatus(userId, enabled);
				}
				new JsonResult().printTo(out);
				
			} else if ("updateEmailDomain".equals(crud)) {
				ServletUtils.StringArray userIds = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				String domainPart = ServletUtils.getStringParameter(request, "domainPart", true);
				
				admMgr.bulkUpdatePersonalEmailDomain(userIds.stream().collect(Collectors.toSet()), domainPart);
				new JsonResult().printTo(out);
				
			} else {
				throw new WTUnsupportedOperationException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainUsers", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processChangeUserPassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			char[] newPassword = ServletUtils.getStringParameter(request, "newPassword", true).toCharArray();
			
			UserProfileId pid = new UserProfileId(profileId);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(pid.getDomainId()));
			admMgr.updateUserPassword(pid.getUserId(), newPassword);
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in ChangeUserPassword", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainUser(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				User item = admMgr.getUser(id, UserGetOption.internalDefaultFlags());
				new JsonResult(new JsUser(item)).printTo(out);
				
			} else if (Crud.CREATE.equals(crud)) {
				Payload<MapItem, JsUser> pl = ServletUtils.getPayload(request, JsUser.class);
				
				Result<User> result = null;
				if (!StringUtils.isBlank(pl.data.password)) {
					result = admMgr.addUser(pl.data.userId, JsUser.createUserForAdd(pl.data), true, pl.data.password.toCharArray(), UserUpdateOption.internalDefaultFlags());
				} else {
					result = admMgr.addUser(pl.data.userId, JsUser.createUserForAdd(pl.data), false, null, UserUpdateOption.internalDefaultFlags());
				}
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsUser> pl = ServletUtils.getPayload(request, JsUser.class);
				
				ResultVoid result = admMgr.updateUser(pl.data.userId, JsUser.createUserForUpdate(pl.data), UserUpdateOption.internalDefaultFlags());
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.DELETE.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				boolean deep = ServletUtils.getBooleanParameter(request, "deep", false);
				
				ResultVoid result = admMgr.deleteUser(id, deep);
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if ("check".equals(crud)) {
				String user = ServletUtils.getStringParameter(request, "user", false);
				
				boolean available = admMgr.checkUserIdAvailability(user);
				new JsonResult(available).printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainUser", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainResources(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				List<JsGridResource> items = new ArrayList<>();
				for (Resource resource : admMgr.listResources(EnabledCond.ANY_STATE).values()) {
					items.add(new JsGridResource(resource));
				}
				new JsonResult(items).printTo(out);
				
			} /*else if (Crud.DELETE.equals(crud)) {
				PayloadAsList<JsDomainGridResource.List> pl = ServletUtils.getPayloadAsList(request, JsDomainGridResource.List.class);
				
				JsDomainGridResource js = pl.data.get(0);
				ResultVoid result = admMgr.deleteResource(js.name);
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{resource.warn.deleteWithExceptions@com.sonicle.webtop.core.admin}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			}*/ else {
				throw new WTUnsupportedOperationException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainResources", ex);
			new JsonResult(ex).printTo(out);
		}
	}

	public void processManageDomainResource(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				Resource item = admMgr.getResource(id, ResourceGetOption.internalDefaultFlags());
				new JsonResult(new JsResource(item)).printTo(out);
				
			} else if (Crud.CREATE.equals(crud)) {
				Payload<MapItem, JsResource> pl = ServletUtils.getPayload(request, JsResource.class);
				
				Result<Resource> result = admMgr.addResource(pl.data.name, JsResource.createResourceForAdd(pl.data), ResourceUpdateOption.internalDefaultFlags());
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsResource> pl = ServletUtils.getPayload(request, JsResource.class);
				
				ResultVoid result = admMgr.updateResource(pl.data.name, JsResource.createResourceForUpdate(pl.data), ResourceUpdateOption.internalDefaultFlags());
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.DELETE.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				ResultVoid result = admMgr.deleteResource(id);
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if ("check".equals(crud)) {
				String resource = ServletUtils.getStringParameter(request, "resource", false);
				
				boolean available = admMgr.checkResourceIdAvailability(resource);
				new JsonResult(available).printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainResource", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				List<JsGridRole> items = new ArrayList<>();
				for (Role role : admMgr.listRoles().values()) {
					items.add(new JsGridRole(role));
				}
				new JsonResult(items).printTo(out);
				
			} else {
				throw new WTUnsupportedOperationException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainRoles", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainRole(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				Role item = admMgr.getRole(id, RoleGetOption.internalDefaultFlags());
				new JsonResult(new JsRole(item)).printTo(out);
				
			} else if (Crud.CREATE.equals(crud)) {
				Payload<MapItem, JsRole> pl = ServletUtils.getPayload(request, JsRole.class);
				
				Result<Role> result = admMgr.addRole(pl.data.roleId, JsRole.createRoleForAdd(pl.data), RoleUpdateOption.internalDefaultFlags());
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsRole> pl = ServletUtils.getPayload(request, JsRole.class);
				
				ResultVoid result = admMgr.updateRole(pl.data.roleId, JsRole.createRoleForUpdate(pl.data), RoleUpdateOption.internalDefaultFlags());
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if (Crud.DELETE.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				ResultVoid result = admMgr.deleteRole(id);
				if (result.hasExceptions()) {
					new JsonResult().withMessageArray("{warn.operation.okWithExceptions@com.sonicle.webtop.core}", result.collectExceptionsMessages()).printTo(out);
				} else {
					new JsonResult().printTo(out);
				}
				
			} else if ("check".equals(crud)) {
				String role = ServletUtils.getStringParameter(request, "role", false);
				
				boolean available = admMgr.checkRoleIdAvailability(role);
				new JsonResult(available).printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainRole", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupServicesProducts(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsServiceProductLkp> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			for (String sid: core.listInstalledServices()) {
				ServiceManifest manifest = WT.getManifest(sid);
				for (Product product : manifest.getProducts()) {
					items.add(new JsServiceProductLkp(sid, WT.lookupResource(sid, up.getLocale(), BaseService.RESOURCE_SERVICE_NAME), product.code, product.name));
				}
			}
			Collections.sort(items, (JsServiceProductLkp js1, JsServiceProductLkp js2) -> js1.productCode.compareTo(js2.productCode));
			new JsonResult(items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupServicesProducts", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainLicenses(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.READ.equals(crud)) {
				List<JsGridDomainLicense> items = new ArrayList<>();
				for (ServiceLicense license : admMgr.listLicenses(LicenseListOption.internalDefaultFlags())) {
					items.add(new JsGridDomainLicense(domainId, license, up.getTimeZone()));
				}
				new JsonResult(items, items.size()).printTo(out);
				
			} else if (Crud.UPDATE.equals(crud)) {
				PayloadAsList<JsGridDomainLicense.List> pl = ServletUtils.getPayloadAsList(request, JsGridDomainLicense.List.class);
				
				JsGridDomainLicense pl0 = pl.data.get(0);
				admMgr.updateLicenseAutoLease(new ProductId(pl0.id).getProductCode(), pl0.autoLease);
				new JsonResult().printTo(out);
				
			} else if ("cleanup".equals(crud)) {
				admMgr.cleanupLicenseCache();
				new JsonResult().printTo(out);
				
			} else if ("check".equals(crud)) {
				admMgr.checkOnlineAvailability();
				new JsonResult().printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
		
		} catch (Exception ex) {
			logger.error("Error in ManageDomainLicenses", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageLicense(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String productId = ServletUtils.getStringParameter(request, "productId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			ProductId prodId = new ProductId(productId);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			if (Crud.CREATE.equals(crud)) {
				String string = ServletUtils.getStringParameter(request, "string", true);
				Boolean activate = ServletUtils.getBooleanParameter(request, "activate", false);
				
				try {
					LicenseBase license = new LicenseBase();
					license.setLicenseString(string);
					admMgr.addLicense(prodId.getProductCode(), license, activate);
					new JsonResult().printTo(out);
					
				} catch(WTIntegrityException ex) {
					throw new WTException(ex, "Product license already present [{}]", prodId.getProductCode());
				} catch(WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
					handleLicenseException(ex, true).printTo(out);
				}
				
			} else if ("change".equals(crud)) {
				String newString = ServletUtils.getStringParameter(request, "nstring", true);
				String activatedString = ServletUtils.getStringParameter(request, "astring", false);
				
				try {
					admMgr.changeLicense(prodId.getProductCode(), newString, activatedString);
					new JsonResult().printTo(out);
					
				} catch(WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
					handleLicenseException(ex, true).printTo(out);
				}
				
			} else if (crud.equals(Crud.DELETE)) {
				admMgr.deleteLicense(prodId.getProductCode(), false);				
				new JsonResult().printTo(out);
				
			} else if ("actreqinfo".equals(crud)) {
				String type = ServletUtils.getStringParameter(request, "type", true);
				
				ProductLicense prodLic = admMgr.getProductLicense(prodId.getProductCode());
				if (prodLic == null) throw new WTException("Unknown product [{}]", productId);
				if ("activation".equals(type)) {
					new JsonResult(prodLic.getManualActivationRequestInfo()).printTo(out);
					
				} else if ("deactivation".equals(type)) {
					new JsonResult(prodLic.getManualDeactivationRequestInfo()).printTo(out);
					
				} else {
					throw new WTException("Unsupported type [{}]", type);
				}
				
			} else if ("activate".equals(crud)) {
				String activatedString = ServletUtils.getStringParameter(request, "astring", false);
				
				try {
					admMgr.activateLicense(prodId.getProductCode(), activatedString);
					new JsonResult().printTo(out);
					
				} catch (WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
					handleLicenseException(ex, true).printTo(out);
				}
				
			}  else if ("deactivate".equals(crud)) {
				String deactivatedString = ServletUtils.getStringParameter(request, "dstring", false);
				
				try {
					admMgr.deactivateLicense(prodId.getProductCode(), "dummyoffline".equals(deactivatedString));
					new JsonResult().printTo(out);
					
				} catch (WTLicenseMismatchException | WTLicenseValidationException | WTLicenseActivationException ex) {
					handleLicenseException(ex, false).printTo(out);
				}
				
			} else if ("assignlease".equals(crud)) {
				ArrayList<String> userIds = ServletUtils.getStringParameters(request, "userIds");
				
				try {
					admMgr.assignLicenseLease(prodId.getProductCode(), LangUtils.asSet(userIds));
				} catch(WTIntegrityException ex) {
					throw new WTException(ex, "User has already been assigned [{}]", prodId.getProductCode());
				}
				new JsonResult().printTo(out);
				
			}  else if ("revokelease".equals(crud)) {
				ArrayList<String> userIds = ServletUtils.getStringParameters(request, "userIds");
				
				admMgr.revokeLicenseLease(prodId.getProductCode(), LangUtils.asSet(userIds));
				new JsonResult().printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageLicense", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	private JsonResult handleLicenseException(WTLicenseException cause, boolean activation) {
		if (cause instanceof WTLicenseMismatchException) {
			return new JsonResult(cause, Arrays.asList("{license.err.mismatch}"));
			
		} else if (cause instanceof WTLicenseValidationException) {
			if (!activation && ValidationStatus.MISMATCH_HARDWARE_ID.equals(((WTLicenseValidationException)cause).getValidationStatus())) {
				return new JsonResult(cause, Arrays.asList("{license.err.hwidmismatch.deactivation}"));
			} else {
				return new JsonResult(cause, Arrays.asList("{license.err.validation}", ((WTLicenseValidationException)cause).getValidationStatus().name()));
			}
			
		} else if (cause instanceof WTLicenseActivationException) {
			if (ActivationStatus.ACTIVATION_SERVER_CONNECTION_ERROR.equals(((WTLicenseActivationException)cause).getActivationStatus())) {
				return new JsonResult(cause, Arrays.asList("{license.err.serverunreachable}"));
			
			} else if (ActivationStatus.ACTIVATION_NOT_FOUND_ON_SERVER.equals(((WTLicenseActivationException)cause).getActivationStatus())) {
				return new JsonResult(cause, Arrays.asList("{license.err.notfound.deactivation}"));
				
			} else {
				return new JsonResult(cause, Arrays.asList(activation ? "{license.err.activation}" : "{license.err.deactivation}", ((WTLicenseActivationException)cause).getActivationStatus().name()));
			}
		} else {
			throw new UnsupportedOperationException("Unsupported cause type");
		}
	}
	
	public void processLicenseWizSaveToFile(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String productId = ServletUtils.getStringParameter(request, "productId", true);
			String type = ServletUtils.getStringParameter(request, "type", true);
			
			ProductId prodId = new ProductId(productId);
			CoreAdminManager admMgr = WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
			ProductLicense prodLic = admMgr.getProductLicense(prodId.getProductCode());
			if (prodLic == null) throw new WTException("Unknown product [{}]", productId);
			
			String s = null;
			if ("activation".equals(type)) {
				s = prodLic.getManualActivationRequestInfo().request;
			} else if ("deactivation".equals(type)) {
				s = prodLic.getManualDeactivationRequestInfo().request;
			} else {
				throw new WTException("Unsupported type [{}]", type);
			}
			
			String filename = prodId.getProductCode() + "_" + type + "-req" + ".l4j";
			ServletUtils.setFileStreamHeadersForceDownload(response, filename);
			ServletUtils.writePlainResponse(response, s);
			
		} catch (Exception ex) {
			logger.error("Error in ActivatorWizSaveToFile", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		}
	}
	
	public void processLicenseWizLoadFromFile(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UploadedFile upfile = null;
		
		try {
			String uploadId = ServletUtils.getStringParameter(request, "uploadId", true);
			
			upfile = getUploadedFileOrThrow(uploadId);
			if (upfile.getSize() > 1048576) throw new WTException("File is too large [{}]", upfile.getSize());
			new JsonResult(FileUtils.readFile(upfile.getFile())).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LicenseWizLoadFromFile", ex);
			new JsonResult(ex).printTo(out);
		} finally {
			if (upfile != null) removeUploadedFile(upfile.getUploadId());
		}
	}
	
	public void processLookupDataSourceTypes(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			CoreAdminManager admMgr = getDomainCoreAdminManager(domainId);
			
			Map<String, DataSourceType> types = admMgr.listDataSourceTypes();
			List<JsSimple> items = new ArrayList<>();
			for (DataSourceType type : types.values()) {
				items.add(new JsSimple(type.getProto(), type.getName()));
			}
			new JsonResult(items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupDataSourceTypes", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainDataSources(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = getDomainCoreAdminManager(domainId);
			
			if (Crud.READ.equals(crud)) {
				List<JsGridDomainDataSource> items = new ArrayList<>();
				for (DataSourcePooled dataSource : admMgr.listDataSources().values()) {
					items.add(new JsGridDomainDataSource(dataSource));
				}
				new JsonResult(items).printTo(out);
				
			} else if (Crud.DELETE.equals(crud)) {
				PayloadAsList<JsGridDomainDataSource.List> pl = ServletUtils.getPayloadAsList(request, JsGridDomainDataSource.List.class);
				for (JsGridDomainDataSource js : pl.data) {
					admMgr.deleteDataSource(js.id);
				}
				new JsonResult().printTo(out);
				
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainDataSources", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainDataSource(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = getDomainCoreAdminManager(domainId);
			
			if (Crud.READ.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				DataSource item = admMgr.getDataSource(id);
				new JsonResult(new JsDataSource(item)).printTo(out);
				
			} else if (Crud.CREATE.equals(crud)) {
				Payload<MapItem, JsDataSource> pl = ServletUtils.getPayload(request, JsDataSource.class);
				
				try {
					admMgr.addDataSource(JsDataSource.createDataSourceForAdd(pl.data));
					new JsonResult().printTo(out);
				} catch (DataSourceBase.WTPoolException ex1) {
					new JsonResult(true, "{dataSource.warn.poolNotReady@com.sonicle.webtop.core.admin}").printTo(out);
				}
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsDataSource> pl = ServletUtils.getPayload(request, JsDataSource.class);
				
				JsDataSource.UpdateReturn ret = new JsDataSource.UpdateReturn();
				DataSourceBase dataSource = JsDataSource.createDataSourceForUpdate(pl.data, ret);
				try {
					admMgr.updateDataSource(pl.data.id, dataSource, ret.passwordChanged);
					new JsonResult().printTo(out);
				} catch (DataSourceBase.WTPoolException ex1) {
					new JsonResult(true, "{dataSource.warn.poolNotReady@com.sonicle.webtop.core.admin}").printTo(out);
				}
				
			} else if (Crud.DELETE.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				admMgr.deleteDataSource(id);
				new JsonResult().printTo(out);
			
			} else if ("test".equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				try {
					admMgr.checkDataSourceConnection(id);
					new JsonResult().printTo(out);
				
				} catch (WTException ex1) {
					if (ex1 instanceof WTConnectionException) {
						new JsonResult(ex1).printTo(out);
					} else if (ex1.getCause() instanceof SQLException) {
						new JsonResult(ex1.getCause()).printTo(out);
					} else {
						throw ex1;
					}
				}
				
			} else if ("testp".equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				String type = ServletUtils.getStringParameter(request, "type", true);
				String serverName = ServletUtils.getStringParameter(request, "serverName", true);
				Integer serverPort = ServletUtils.getIntParameter(request, "serverPort", false);
				String databaseName = ServletUtils.getStringParameter(request, "databaseName", true);
				String username = ServletUtils.getStringParameter(request, "username", false);
				String password = ServletUtils.getStringParameter(request, "password", false);
				String rpassword = ServletUtils.getStringParameter(request, "rpassword", false);
				String driverProps = ServletUtils.getStringParameter(request, "driverProps", false);
				
				String realPassword = password;
				if (StringUtils.equals(password, rpassword) && (id != null)) {
					DataSource dataSource = admMgr.getDataSource(id);
					if (dataSource == null) throw new WTException("Data source not found [{}]", id);
					realPassword = dataSource.getPassword();
				}
				
				try {
					admMgr.checkDataSourceConnection(type, serverName, serverPort, databaseName, username, realPassword, LangUtils.parseStringAsKeyValueMap(driverProps));
					new JsonResult().printTo(out);

				} catch (WTException ex1) {
					if (ex1 instanceof WTConnectionException) {
						new JsonResult(ex1).printTo(out);
					} else if (ex1.getCause() instanceof SQLException) {
						new JsonResult(ex1.getCause()).printTo(out);
					} else {
						throw ex1;
					}
				}
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainDataSource", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainDataSourceQuery(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			CoreAdminManager admMgr = getDomainCoreAdminManager(domainId);
			
			if (Crud.READ.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				DataSourceQuery item = admMgr.getDataSourceQuery( id);
				new JsonResult(new JsDataSourceQuery(item)).printTo(out);
				
			} else if (Crud.CREATE.equals(crud)) {
				Payload<MapItem, JsDataSourceQuery> pl = ServletUtils.getPayload(request, JsDataSourceQuery.class);
				
				admMgr.addDataSourceQuery(pl.data.dataSourceId, JsDataSourceQuery.createDataSourceQueryForAdd(pl.data));
				new JsonResult().printTo(out);
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsDataSourceQuery> pl = ServletUtils.getPayload(request, JsDataSourceQuery.class);
				
				admMgr.updateDataSourceQuery(pl.data.id, JsDataSourceQuery.createDataSourceQueryForUpdate(pl.data));
				new JsonResult().printTo(out);
				
			} else if (Crud.DELETE.equals(crud)) {
				String id = ServletUtils.getStringParameter(request, "id", false);
				
				admMgr.deleteDataSourceQuery(id);
				new JsonResult().printTo(out);
			
			} else {
				throw new WTException("Unsupported operation [{}]", crud);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageDomainDataSourceQuery", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processDataSourceQueryTester(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String dataSourceId = ServletUtils.getStringParameter(request, "dataSourceId", true);
			boolean pagination = ServletUtils.getBooleanParameter(request, "pagination", false);
			Payload<MapItem, MapItem> pl = ServletUtils.getPayload(request, MapItem.class);
			
			PageInfo pagInfo = null;
			if (pagination) {
				int page = ServletUtils.getIntParameter(request, "page", true);
				int limit = ServletUtils.getIntParameter(request, "limit", 25);
				pagInfo = new PageInfo(page, limit, false);
			}
			CoreAdminManager admMgr = getDomainCoreAdminManager(domainId);
			DataSourceBase.ExecuteQueryResult result = admMgr.executeDataSourceRawQuery(dataSourceId, (String)pl.data.get("source"), (Map)pl.data.get("placeholders"), pagInfo, true);
			new JsonResult(result).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in DataSourceQueryTester", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainLauncherLinks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			
			CoreServiceSettings css = new CoreServiceSettings(CoreManifest.ID, domainId);
			if (crud.equals(Crud.READ)) {
				ArrayList<JsDomainLauncherLink> items = new ArrayList<>();
				List<LauncherLink> links = css.getLauncherLinks();
				for (int i=0 ; i<links.size(); i++) {
					items.add(new JsDomainLauncherLink((short)i, links.get(i)));
				}
				Collections.sort(items, (JsDomainLauncherLink js1, JsDomainLauncherLink js2) -> js1.order.compareTo(js2.order));
				
				new JsonResult(items, items.size()).printTo(out);
			
			} else if (crud.equals(Crud.CREATE)) {
				PayloadAsList<JsDomainLauncherLink.List> pl = ServletUtils.getPayloadAsList(request, JsDomainLauncherLink.List.class);
				
				ArrayList<JsDomainLauncherLink> items = new ArrayList<>();
				List<LauncherLink> links = css.getLauncherLinks();
				for (JsDomainLauncherLink jsLink : pl.data) {
					LauncherLink ll = new LauncherLink();
					ll.text = jsLink.text;
					ll.href = jsLink.href;
					ll.icon = jsLink.icon;
					ll.order = jsLink.order;
					links.add(ll);
					items.add(new JsDomainLauncherLink((short)links.indexOf(ll), ll));
				}
				css.setLauncherLinks(links);
				new JsonResult(items).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsDomainLauncherLink.List> pl = ServletUtils.getPayloadAsList(request, JsDomainLauncherLink.List.class);
				
				List<LauncherLink> links = css.getLauncherLinks();
				for (JsDomainLauncherLink jsLink : pl.data) {
					LauncherLink ll = links.get(jsLink.id);
					ll.text = jsLink.text;
					ll.href = jsLink.href;
					ll.icon = jsLink.icon;
					ll.order = jsLink.order;
				}
				css.setLauncherLinks(links);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<JsDomainLauncherLink.List> pl = ServletUtils.getPayloadAsList(request, JsDomainLauncherLink.List.class);
				JsDomainLauncherLink jsLink = pl.data.get(0);
				
				List<LauncherLink> links = css.getLauncherLinks();
				links.remove((int)jsLink.id);
				css.setLauncherLinks(links);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageDomainLauncherLinks", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManagePecBridgeFetchers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				
				List<JsGridPecBridgeFetcher> items = new ArrayList<>();
				for(OPecBridgeFetcher fetcher : coreadm.listPecBridgeFetchers(domainId)) {
					items.add(new JsGridPecBridgeFetcher(fetcher));
				}
				new JsonResult("fetchers", items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				PayloadAsList<JsGridPecBridgeFetcher.List> pl = ServletUtils.getPayloadAsList(request, JsGridPecBridgeFetcher.List.class);
				JsGridPecBridgeFetcher fetcher = pl.data.get(0);
				
				coreadm.deletePecBridgeFetcher(domainId, fetcher.fetcherId);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManagePecBridgeFetchers", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManagePecBridgeFetcher(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				OPecBridgeFetcher fetcher = coreadm.getPecBridgeFetcher(id);
				new JsonResult(new JsPecBridgeFetcher(fetcher)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsPecBridgeFetcher> pl = ServletUtils.getPayload(request, JsPecBridgeFetcher.class);
				coreadm.addPecBridgeFetcher(JsPecBridgeFetcher.buildFetcher(pl.data));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsPecBridgeFetcher> pl = ServletUtils.getPayload(request, JsPecBridgeFetcher.class);
				coreadm.updatePecBridgeFetcher(JsPecBridgeFetcher.buildFetcher(pl.data));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManagePecBridgeFetcher", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManagePecBridgeRelays(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				
				List<JsGridPecBridgeRelay> items = new ArrayList<>();
				for(OPecBridgeRelay relay : coreadm.listPecBridgeRelays(domainId)) {
					items.add(new JsGridPecBridgeRelay(relay));
				}
				new JsonResult("relays", items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				PayloadAsList<JsGridPecBridgeRelay.List> pl = ServletUtils.getPayloadAsList(request, JsGridPecBridgeRelay.List.class);
				JsGridPecBridgeRelay relay = pl.data.get(0);
				
				coreadm.deletePecBridgeRelay(domainId, relay.relayId);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManagePecBridgeRelays", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManagePecBridgeRelay(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				OPecBridgeRelay relay = coreadm.getPecBridgeRelay(id);
				new JsonResult(new JsPecBridgeRelay(relay)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsPecBridgeRelay> pl = ServletUtils.getPayload(request, JsPecBridgeRelay.class);
				coreadm.addPecBridgeRelay(JsPecBridgeRelay.buildRelay(pl.data));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsPecBridgeRelay> pl = ServletUtils.getPayload(request, JsPecBridgeRelay.class);
				coreadm.updatePecBridgeRelay(JsPecBridgeRelay.buildRelay(pl.data));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManagePecBridgeRelay", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDbUpgrades(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			synchronized (lock1) {
				String crud = ServletUtils.getStringParameter(request, "crud", true);
				DbUpgraderEnvironment upEnv = getDbUpgraderEnvironment();
				if (crud.equals(Crud.READ)) {
					// Prepare output
					List<JsGridUpgradeRow> items = new ArrayList<>();
					for(ORunnableUpgradeStatement stmt : upEnv.runnableStmts) {
						items.add(new JsGridUpgradeRow(stmt));
					}
					ORunnableUpgradeStatement next = upEnv.nextStatement();
					Integer nextStmtId = (next != null) ? next.getUpgradeStatementId() : null;
					
					ResultMeta meta = new ResultMeta()
							.set("upgradeTag", upEnv.upgradeTag)
							.set("pendingCount", upEnv.pendingCount)
							.set("okCount", upEnv.okCount)
							.set("errorCount", upEnv.errorCount)
							.set("warningCount", upEnv.warningCount)
							.set("skippedCount", upEnv.skippedCount)
							.set("nextStmtId", nextStmtId);
					new JsonResult(items, meta, items.size()).printTo(out);
					
				} else if (crud.equals("play")) {
					String stmtBody = ServletUtils.getStringParameter(request, "stmtBody", true);
					boolean once = ServletUtils.getBooleanParameter(request, "once", false);
					ArrayList<ORunnableUpgradeStatement> executed = new ArrayList<>();
					
					boolean ret = false;
					while (true) {
						ORunnableUpgradeStatement item = upEnv.nextStatement();
						if (item == null) break;
						
						// Use passed statement body only for the first statement
						if (executed.isEmpty()) item.setStatementBody(stmtBody);
						if (!executed.isEmpty() && item.getRequireAdmin()) break;
						
						String oldRunStatus = item.getRunStatus();
						ret = coreadm.executeUpgradeStatement(item, item.getIgnoreErrors());
						upEnv.updateExecuted(true, item.getRunStatus(), oldRunStatus);
						
						executed.add(item);
						if (!ret) break; // Exits, admin must review execution...
						if (once) break;
					}
					
					// Prepare output
					List<JsGridUpgradeRow> items = new ArrayList<>();
					for(ORunnableUpgradeStatement stmt : executed) {
						items.add(new JsGridUpgradeRow(stmt));
					}
					ORunnableUpgradeStatement next = upEnv.nextStatement();
					Integer nextStmtId = (next != null) ? next.getUpgradeStatementId() : null;
					
					ResultMeta meta = new ResultMeta()
							.set("upgradeTag", upEnv.upgradeTag)
							.set("pendingCount", upEnv.pendingCount)
							.set("okCount", upEnv.okCount)
							.set("errorCount", upEnv.errorCount)
							.set("warningCount", upEnv.warningCount)
							.set("skippedCount", upEnv.skippedCount)
							.set("nextStmtId", nextStmtId);
					new JsonResult(items, meta, items.size()).printTo(out);
					
				} else if (crud.equals("skip")) {
					ORunnableUpgradeStatement stmt = upEnv.nextStatement();
					String oldRunStatus = stmt.getRunStatus();
					coreadm.skipUpgradeStatement(stmt);
					upEnv.updateExecuted(true, stmt.getRunStatus(), oldRunStatus);
					
					// Prepare output
					List<JsGridUpgradeRow> items = new ArrayList<>();
					items.add(new JsGridUpgradeRow(stmt));
					ORunnableUpgradeStatement next = upEnv.nextStatement();
					Integer nextStmtId = (next != null) ? next.getUpgradeStatementId() : null;
					
					ResultMeta meta = new ResultMeta()
							.set("upgradeTag", upEnv.upgradeTag)
							.set("pendingCount", upEnv.pendingCount)
							.set("okCount", upEnv.okCount)
							.set("errorCount", upEnv.errorCount)
							.set("warningCount", upEnv.warningCount)
							.set("skippedCount", upEnv.skippedCount)
							.set("nextStmtId", nextStmtId);
					new JsonResult(items, meta, items.size()).printTo(out);
				}
			}
		} catch(Exception ex) {
			logger.error("Error in ManageDbUpgrades", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageLoggers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				List<JsGridLogger> items = new ArrayList<>();
				for (LoggerEntry loggerEntry : coreadm.listLoggers().values()) {
					items.add(new JsGridLogger(loggerEntry));
				}
				new JsonResult(items, items.size()).printTo(out);
				
			} else if (Crud.UPDATE.equals(crud)) {
				String name = ServletUtils.getStringParameter(request, "name", true);
				LoggerEntry.Level level = ServletUtils.getEnumParameter(request, "level", null, LoggerEntry.Level.class);
				
				LoggerEntry item = coreadm.updateLogger(name, level);
				new JsonResult(item != null ? new JsGridLogger(item) : null).printTo(out);
			}
			
		} catch(Throwable t) {
			logger.error("Error in ManageLoggers", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processGetLogContent(HttpServletRequest request, HttpServletResponse response) {
		
		boolean rawErrorResp = ServletUtils.getBooleanParameter(request, "rawErrorResp", false);
		try {
			Long fromByte = ServletUtils.getLongParameter(request, "fromByte", 0L);
			if (fromByte < 0) fromByte = 0L;
			Long bytesCount = ServletUtils.getLongParameter(request, "bytesCount", false);
			
			InputStream is = null;
			try {
				is = coreadm.getLogFileContent(fromByte, bytesCount);
				ServletUtils.writeFileResponse(response, false, "webtop.log", null, -1, is);
			} finally {
				IOUtils.closeQuietly(is);
			}
			
		} catch(Throwable t) {
			if (t instanceof WTException) {
				logger.debug("Cannot read log file", t);
			} else {
				logger.error("Error in GetLogContent", t);
			}
			if (rawErrorResp) {
				throw new WTRuntimeException(t);
			} else {
				ServletUtils.writeErrorHandlingJs(response, t.getMessage());
			}
		}
	}
	
	public void processSetMaintenanceFlag(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Boolean value = ServletUtils.getBooleanParameter(request, "value", null);
			if (value == null) throw new WTException("Invalid value");
			
			coreadm.setMaintenanceMode(value);
			
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in SetMaintenanceFlag", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	private final Cache<String, Integer> cacheManageGridContactsTotalCount = Caffeine.newBuilder()
		.expireAfterWrite(500, TimeUnit.MILLISECONDS)
		.maximumSize(10)
		.build();
	
	public void processManageDomainAccessLog(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsDomainAccessLog> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				QueryObj queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
				SortMeta.List sortMeta = ServletUtils.getObjectParameter(request, "sort", new SortMeta.List(), SortMeta.List.class);
				int page = ServletUtils.getIntParameter(request, "page", true);
				int limit = ServletUtils.getIntParameter(request, "limit", 50);
				
				DateTime from = null, to = null;
				for (QueryObj.ConditionEntry entry : queryObj.getConditions()) {
					if ("dateFrom".equals(entry.keyword)) {
						from = DateTimeUtils.createYmdFormatter(utz).parseDateTime(entry.value).withTimeAtStartOfDay();
					} else if ("dateTo".equals(entry.keyword)) {
						to = DateTimeUtils.createYmdFormatter(utz).parseDateTime(entry.value).plusDays(1).withTimeAtStartOfDay();
					}
				}
				queryObj.removeCondition("dateFrom");
				queryObj.removeCondition("dateTo");
				
				if (from == null || to == null || Math.abs(Days.daysBetween(from, to).getDays()) > 365) {
					new JsonResult().printTo(out);
				} else {
					SortInfo sortInfo = !sortMeta.isEmpty() ? sortMeta.get(0).toSortInfo() : SortInfo.desc("timestamp");
					
					String reqId = new MD5HashBuilder()
						.append(domainId)
						.append(ServletUtils.getStringParameter(request, "query", null))
						.append(ServletUtils.getStringParameter(request, "sort", null))
						.build();

					Integer cachedTotalCount = cacheManageGridContactsTotalCount.getIfPresent(reqId);
					ListDomainAccessLogResult result = coreadm.listAccessLog(domainId, new DateTimeRange(from, to), DomainAccessLogQuery.createCondition(queryObj, utz), sortInfo, page, limit, cachedTotalCount == null);
					int totalCount;
					if (cachedTotalCount != null) {
						totalCount = cachedTotalCount;
					} else {
						cacheManageGridContactsTotalCount.put(reqId, result.fullCount);
						totalCount = result.fullCount;
					}

					for (DomainAccessLog domainAccLog : result.items) {
						items.add(new JsDomainAccessLog(domainAccLog));
					}
					new JsonResult(items, totalCount)
						.setPage(page)
						.setMetaData(new GridMetadata()
							.setSortInfo(sortInfo)
						)
						.printTo(out);
				}
			}
		} catch(Throwable t) {
			logger.error("Error in ManageDomainAccessLog", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processManageDomainAccessLogDetail(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsDomainAccessLogDetail> items = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			String sessionId = ServletUtils.getStringParameter(request, "sessionId", true);
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String userId = ServletUtils.getStringParameter(request, "userId", true);
			
			if (crud.equals(Crud.READ)) {
				ListDomainAccessLogDetailResult result = coreadm.listAccessLogDetail(sessionId, domainId, userId, true);
				for (DomainAccessLogDetail domainAccLogDetail : result.items) {
					items.add(new JsDomainAccessLogDetail(domainAccLogDetail));
				}
				new JsonResult(items, result.fullCount).printTo(out);
			}
			
		} catch(Throwable t) {
			logger.error("Error in ManageDomainAccessLog", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	private CoreAdminManager getDomainCoreAdminManager(final String domainId) {
		return WT.getCoreAdminManager(RunContext.buildDomainAdminProfileId(domainId));
	}
	
	private static class DbUpgraderEnvironment {
		public String upgradeTag;
		public int totalCount;
		public int pendingCount;
		public int okCount;
		public int errorCount;
		public int warningCount;
		public int skippedCount;
		public final LinkedList<ORunnableUpgradeStatement> runnableStmts;
		public Integer index;
		
		public DbUpgraderEnvironment(List<OUpgradeStatement> stmts) {
			upgradeTag = null;
			pendingCount = 0;
			okCount = 0;
			errorCount = 0;
			warningCount = 0;
			skippedCount = 0;
			runnableStmts = new LinkedList<>();
			index = -1;
			
			boolean ignoreErrors = false, requireAdmin = false;
			String sqlComments = "", annComments = "";
			for (OUpgradeStatement stmt : stmts) {
				if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_ANNOTATION)) {
					if (IgnoreErrorsAnnotationLine.matches(stmt.getStatementBody())) {
						ignoreErrors = true;
						annComments += stmt.getStatementBody() + " ";
						
					} else if (RequireAdminAnnotationLine.matches(stmt.getStatementBody())) {
						requireAdmin = true;
						annComments += stmt.getStatementBody() + " ";
					}
					
				} else if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_COMMENT)) {
					sqlComments = stmt.getStatementBody();
					
				} else if (stmt.getStatementType().equals(OUpgradeStatement.STATEMENT_TYPE_SQL)) {
					String comments = org.apache.commons.lang3.StringUtils.trim(annComments);
					if (!org.apache.commons.lang3.StringUtils.isEmpty(sqlComments)) {
						if (!org.apache.commons.lang3.StringUtils.isEmpty(comments)) comments += "\n";
						comments += sqlComments;
					}
					runnableStmts.add(new ORunnableUpgradeStatement(stmt, requireAdmin, ignoreErrors, StringUtils.trim(comments)));
					requireAdmin = ignoreErrors = false;
					sqlComments = annComments = "";
					
					String runStatus = stmt.getRunStatus();
					if (!StringUtils.isBlank(runStatus)) {
						if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_OK)) {
							okCount++;
						} else if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_ERROR)) {
							errorCount++;
						} else if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_WARNING)) {
							warningCount++;
						} else if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_SKIPPED)) {
							skippedCount++;
						}
					}
				}
			}
			if (!runnableStmts.isEmpty()) upgradeTag = runnableStmts.get(0).getTag();
			recalculatePending();
			index = nextIndex();
		}
		
		public ORunnableUpgradeStatement nextStatement() {
			return index != null ? runnableStmts.get(index) : null;
		}
		
		private Integer nextIndex() {
			Integer next = null;
			if (index != null) {
				int start = (index < 0) ? 0 : index;
				ListIterator<ORunnableUpgradeStatement> liter = runnableStmts.listIterator(start);
				while (liter.hasNext()) {
					final int ni = liter.nextIndex();
					final ORunnableUpgradeStatement item = liter.next();
					if(StringUtils.isEmpty(item.getRunStatus()) || item.getRunStatus().equals(ORunnableUpgradeStatement.RUN_STATUS_ERROR)) {
						next = ni;
						break;
					}
				}
			}
			return next;
		}
		
		public void updateExecuted(boolean success, String runStatus, String oldRunStatus) {
			if (oldRunStatus != null) {
				if (oldRunStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_OK)) {
					okCount--;
				} else if (oldRunStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_ERROR)) {
					errorCount--;
				} else if (oldRunStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_WARNING)) {
					warningCount--;
				} else if (oldRunStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_SKIPPED)) {
					skippedCount--;
				}
			}
			if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_OK)) {
				okCount++;
			} else if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_ERROR)) {
				errorCount++;
			} else if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_WARNING)) {
				warningCount++;
			} else if (runStatus.equals(ORunnableUpgradeStatement.RUN_STATUS_SKIPPED)) {
				skippedCount++;
			}
			
			recalculatePending();
			if (success) index = nextIndex();
		}
		
		private void recalculatePending() {
			pendingCount = runnableStmts.size() - (okCount + warningCount + skippedCount);
		}
	}
}
