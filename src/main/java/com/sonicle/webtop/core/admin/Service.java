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

import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.security.auth.DirectoryManager;
import com.sonicle.security.auth.directory.AbstractDirectory;
import com.sonicle.security.auth.directory.DirectoryCapability;
import com.sonicle.webtop.core.CoreLocaleKey;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.CorePrivateEnvironment;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OGroup;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.js.JsDomain;
import com.sonicle.webtop.core.bol.js.JsGridDomainRole;
import com.sonicle.webtop.core.bol.js.JsGridDomainUser;
import com.sonicle.webtop.core.bol.js.JsRole;
import com.sonicle.webtop.core.bol.js.JsRoleLkp;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsUser;
import com.sonicle.webtop.core.bol.model.DirectoryUser;
import com.sonicle.webtop.core.bol.model.DomainEntity;
import com.sonicle.webtop.core.bol.model.DomainSetting;
import com.sonicle.webtop.core.bol.model.Role;
import com.sonicle.webtop.core.bol.model.RoleEntity;
import com.sonicle.webtop.core.bol.model.RoleWithSource;
import com.sonicle.webtop.core.bol.model.SystemSetting;
import com.sonicle.webtop.core.bol.model.UserEntity;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	private static final Logger logger = WT.getLogger(Service.class);
	private CoreManager core;
	private CoreAdminManager coreadm;
	
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
	
	private ExtTreeNode createTreeNode(String id, String type, String text, boolean leaf, String iconClass) {
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_type", type);
		node.setIconClass(iconClass);
		return node;
	}
	
	public void processLookupDomainGroups(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			boolean wildcard = ServletUtils.getBooleanParameter(request, "wildcard", false);
			boolean uid = ServletUtils.getBooleanParameter(request, "uid", false);
			
			if(wildcard) items.add(JsSimple.wildcard(lookupResource(up.getLocale(), CoreLocaleKey.WORD_ALL_MALE)));
			for(OGroup group : coreadm.listGroups(domainId)) {
				items.add(new JsSimple(uid ? group.getUserUid() : group.getGroupId(), group.getDisplayName()));
			}
			
			new JsonResult("groups", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupDomainGroups", ex);
			new JsonResult(false, "Unable to lookup groups").printTo(out);
		}
	}
	
	public void processLookupDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsRoleLkp> items = new ArrayList<>();
		UserProfile up = getEnv().getProfile();
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			for(Role role : coreadm.listRoles(domainId)) {
				items.add(new JsRoleLkp(role, RoleWithSource.SOURCE_ROLE));
			}
			
			new JsonResult("roles", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupDomainRoles", ex);
			new JsonResult(false, "Unable to lookup roles").printTo(out);
		}
	}
	
	
	
	
	
	
	
	
	
	
	private static final String NTYPE_SETTINGS = "settings";
	private static final String NTYPE_DOMAINS = "domains";
	private static final String NTYPE_DOMAIN = "domain";
	private static final String NTYPE_GROUPS = "groups";
	private static final String NTYPE_USERS = "users";
	private static final String NTYPE_ROLES = "roles";
	private static final String NTYPE_DBUPGRADER = "dbupgrader";
	
	private ExtTreeNode createDomainNode(String parentId, ODomain domain) {
		CompositeId cid = new CompositeId(parentId, domain.getDomainId());
		ExtTreeNode node = new ExtTreeNode(cid.toString(), domain.getDescription(), false);
		node.setIconClass(domain.getEnabled() ? "wta-icon-domain-xs" : "wta-icon-domain-disabled-xs");
		node.put("_type", NTYPE_DOMAIN);
		node.put("_domainId", domain.getDomainId());
		//node.put("_internetDomain", domain.getInternetName());
		//node.put("_dirCapPasswordWrite", dirCapPasswordWrite);
		//node.put("_dirCapUsersWrite", dirCapUsersWrite);
		return node;
	}
	
	private ExtTreeNode createDomainChildNode(String parentId, String text, String iconClass, String type, String domainId, boolean passwordPolicy, boolean authCapPasswordWrite, boolean authCapUsersWrite) {
		CompositeId cid = new CompositeId(parentId, type);
		ExtTreeNode node = new ExtTreeNode(cid.toString(), text, true);
		node.setIconClass(iconClass);
		node.put("_type", type);
		node.put("_domainId", domainId);
		node.put("_passwordPolicy", passwordPolicy);
		node.put("_authCapPasswordWrite", authCapPasswordWrite);
		node.put("_authCapUsersWrite", authCapUsersWrite);
		return node;
	}
	
	public void processManageAdminTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Locale locale = getEnv().getWebTopSession().getLocale();
		//DirectoryManager dirMgr = DirectoryManager.getManager();
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String nodeId = ServletUtils.getStringParameter(request, "node", true);
				
				if(nodeId.equals("root")) { // Admin roots...
					children.add(createTreeNode(NTYPE_SETTINGS, NTYPE_SETTINGS, lookupResource(CoreAdminLocale.TREE_ADMIN_SETTINGS), true, "wta-icon-settings-xs"));
					children.add(createTreeNode(NTYPE_DOMAINS, NTYPE_DOMAINS, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAINS), false, "wta-icon-domains-xs"));
					children.add(createTreeNode(NTYPE_DBUPGRADER, NTYPE_DBUPGRADER, lookupResource(CoreAdminLocale.TREE_ADMIN_DBUPGRADER), true, "wta-icon-dbUpgrader-xs"));
				} else {
					CompositeId cid = new CompositeId(3).parse(nodeId, true);
					if(cid.getToken(0).equals("domains")) {
						if(cid.hasToken(1)) {
							String domainId = cid.getToken(1);
							ODomain domain = core.getDomain(domainId);
							AbstractDirectory dir = core.getAuthDirectory(domain);
							boolean passwordPolicy = domain.getAuthPasswordPolicy();
							boolean dirCapPasswordWrite = dir.hasCapability(DirectoryCapability.PASSWORD_WRITE);
							boolean dirCapUsersWrite = dir.hasCapability(DirectoryCapability.USERS_WRITE);
							
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_SETTINGS), "wta-icon-settings-xs", NTYPE_SETTINGS, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_GROUPS), "wta-icon-groups-xs", NTYPE_GROUPS, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_USERS), "wta-icon-users-xs", NTYPE_USERS, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_ROLES), "wta-icon-roles-xs", NTYPE_ROLES, domainId, passwordPolicy, dirCapPasswordWrite, dirCapUsersWrite));
						
						} else { // Availbale webtop domains
							for(ODomain domain : core.listDomains(false)) {
								children.add(createDomainNode(nodeId, domain));
							}
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
			if(crud.equals(Crud.READ)) {
				List<SystemSetting> items = coreadm.listSystemSettings(false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				if(!coreadm.updateSystemSetting(setting.serviceId, setting.key, setting.value)) {
					throw new WTException("Cannot insert setting [{0}, {1}]", setting.serviceId, setting.key);
				}
				
				OSettingDb info = coreadm.getSettingInfo(setting.serviceId, setting.key);
				if(info != null) {
					setting = new SystemSetting(setting.serviceId, setting.key, setting.value, info.getType(), info.getHelp());
				} else {
					setting = new SystemSetting(setting.serviceId, setting.key, setting.value, null, null);
				}
				new JsonResult(setting).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.updateSystemSetting(sid, setting.key, setting.value)) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				}
				if(!StringUtils.equals(key, setting.key)) {
					coreadm.deleteSystemSetting(sid, key);
				}
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.deleteSystemSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{0}, {1}]", sid, key);
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSettings", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomains(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				DomainEntity domain = coreadm.getDomain(id);
				new JsonResult(new JsDomain(domain)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsDomain> pl = ServletUtils.getPayload(request, JsDomain.class);
				AbstractDirectory dir = DirectoryManager.getManager().getDirectory(pl.data.authScheme);
				coreadm.addDomain(JsDomain.buildDomainEntity(pl.data, dir));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsDomain> pl = ServletUtils.getPayload(request, JsDomain.class);
				AbstractDirectory dir = DirectoryManager.getManager().getDirectory(pl.data.authScheme);
				coreadm.updateDomain(JsDomain.buildDomainEntity(pl.data, dir));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				coreadm.deleteDomain(domainId);
				new JsonResult().printTo(out);
				
			} else if(crud.equals("init")) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				coreadm.initDomainWithDefaults(domainId);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageDomains", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<DomainSetting> items = coreadm.listDomainSettings(domainId, false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				if(!coreadm.updateSystemSetting(setting.serviceId, setting.key, setting.value)) {
					throw new WTException("Cannot insert setting [{0}, {1}]", setting.serviceId, setting.key);
				}
				setting = new DomainSetting(setting.domainId, setting.serviceId, setting.key, setting.value, null, null);
				new JsonResult(setting).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.updateSystemSetting(sid, setting.key, setting.value)) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				}
				if(!StringUtils.equals(key, setting.key)) {
					coreadm.deleteSystemSetting(sid, key);
				}
					
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!coreadm.deleteSystemSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{0}, {1}]", sid, key);
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSettings", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				
				List<JsGridDomainUser> items = new ArrayList<>();
				for(DirectoryUser dirUser : coreadm.listDirectoryUsers(domainId)) {
					items.add(new JsGridDomainUser(dirUser));
				}
				new JsonResult("users", items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				boolean deep = ServletUtils.getBooleanParameter(request, "deep", false);
				ServletUtils.StringArray profileIds = ServletUtils.getObjectParameter(request, "profileIds", ServletUtils.StringArray.class, true);
				
				UserProfile.Id pid = new UserProfile.Id(profileIds.get(0));
				coreadm.deleteUser(deep, pid);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals("enable") || crud.equals("disable")) {
				ServletUtils.StringArray profileIds = ServletUtils.getObjectParameter(request, "profileIds", ServletUtils.StringArray.class, true);
				
				UserProfile.Id pid = new UserProfile.Id(profileIds.get(0));
				coreadm.updateUser(pid, crud.equals("enable"));
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageDomainUsers", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				
				UserProfile.Id pid = new UserProfile.Id(id);
				UserEntity user = coreadm.getUser(pid);
				new JsonResult(new JsUser(user)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsUser> pl = ServletUtils.getPayload(request, JsUser.class);
				coreadm.addUser(JsUser.buildUserEntity(pl.data), pl.data.password.toCharArray());
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsUser> pl = ServletUtils.getPayload(request, JsUser.class);
				coreadm.updateUser(JsUser.buildUserEntity(pl.data));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageUsers", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processChangeUserPassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String profileId = ServletUtils.getStringParameter(request, "profileId", true);
			char[] newPassword = ServletUtils.getStringParameter(request, "newPassword", true).toCharArray();
			
			UserProfile.Id pid = new UserProfile.Id(profileId);
			coreadm.updateUserPassword(pid, newPassword);
			
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in ChangeUserPassword", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageDomainRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String domainId = ServletUtils.getStringParameter(request, "domainId", true);
				
				List<JsGridDomainRole> items = new ArrayList<>();
				for(Role role : coreadm.listRoles(domainId)) {
					items.add(new JsGridDomainRole(role));
				}
				new JsonResult("roles", items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<JsGridDomainRole.List> pl = ServletUtils.getPayloadAsList(request, JsGridDomainRole.List.class);
				JsGridDomainRole role = pl.data.get(0);
				
				coreadm.deleteRole(role.roleUid);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageDomainRoles", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageRoles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", null);
				RoleEntity role = coreadm.getRole(id);
				new JsonResult(new JsRole(role)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsRole> pl = ServletUtils.getPayload(request, JsRole.class);
				coreadm.addRole(JsRole.buildRoleEntity(pl.data));
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsRole> pl = ServletUtils.getPayload(request, JsRole.class);
				coreadm.updateRole(JsRole.buildRoleEntity(pl.data));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageRoles", ex);
			new JsonResult(ex).printTo(out);
		}
	}
}
