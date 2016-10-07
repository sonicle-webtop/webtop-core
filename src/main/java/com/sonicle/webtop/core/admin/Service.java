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
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.CorePrivateEnvironment;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopApp;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.OSettingDb;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.model.DomainSetting;
import com.sonicle.webtop.core.bol.model.SystemSetting;
import com.sonicle.webtop.core.sdk.BaseService;
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
	private CoreManager manager;
	
	@Override
	public void initialize() throws Exception {
		manager = WT.getCoreManager();
	}

	@Override
	public void cleanup() throws Exception {
		manager = null;
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
		node.setIconClass("wta-icon-domain-xs");
		node.put("_type", NTYPE_DOMAIN);
		node.put("_domainId", domain.getDomainId());
		node.put("_internetDomain", domain.getDomainName());
		return node;
	}
	
	private ExtTreeNode createDomainChildNode(String parentId, String text, String iconClass, String type, String domainId) {
		CompositeId cid = new CompositeId(parentId, type);
		ExtTreeNode node = new ExtTreeNode(cid.toString(), text, true);
		node.setIconClass(iconClass);
		node.put("_type", type);
		node.put("_domainId", domainId);
		return node;
	}
	
	public void processManageAdminTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Locale locale = getEnv().getWebTopSession().getLocale();
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
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_SETTINGS), "wta-icon-settings-xs", NTYPE_SETTINGS, cid.getToken(1)));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_GROUPS), "wta-icon-domainGroups-xs", NTYPE_GROUPS, cid.getToken(1)));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_USERS), "wta-icon-domainUsers-xs", NTYPE_USERS, cid.getToken(1)));
							children.add(createDomainChildNode(nodeId, lookupResource(CoreAdminLocale.TREE_ADMIN_DOMAIN_ROLES), "wta-icon-domainRoles-xs", NTYPE_ROLES, cid.getToken(1)));
						} else { // Availbale webtop domains
							for(ODomain domain : manager.listDomains(false)) {
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
	
	public void processLookupInstalledServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Locale locale = getEnv().getWebTopSession().getLocale();
		ArrayList<JsSimple> items = new ArrayList<>();
		
		for(String id : manager.listInstalledServices()) {
			items.add(new JsSimple(id, WT.lookupResource(id, locale, BaseService.RESOURCE_SERVICE_NAME)));
		}
		new JsonResult(items).printTo(out);
	}
	
	public void processManageSystemSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<SystemSetting> items = manager.listSystemSettings(false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				if(!manager.updateSystemSetting(setting.serviceId, setting.key, setting.value)) {
					throw new WTException("Cannot insert setting [{0}, {1}]", setting.serviceId, setting.key);
				}
				
				OSettingDb info = manager.getSettingInfo(setting.serviceId, setting.key);
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

				if(!manager.updateSystemSetting(sid, setting.key, setting.value)) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				}
				if(!StringUtils.equals(key, setting.key)) {
					manager.deleteSystemSetting(sid, key);
				}
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<SystemSetting.List> pl = ServletUtils.getPayloadAsList(request, SystemSetting.List.class);
				SystemSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!manager.deleteSystemSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{0}, {1}]", sid, key);
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSettings", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
	
	public void processManageDomainSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String domainId = ServletUtils.getStringParameter(request, "domainId", true);
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<DomainSetting> items = manager.listDomainSettings(domainId, false);
				new JsonResult(items, items.size()).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				if(!manager.updateSystemSetting(setting.serviceId, setting.key, setting.value)) {
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

				if(!manager.updateSystemSetting(sid, setting.key, setting.value)) {
					throw new WTException("Cannot update setting [{0}, {1}]", sid, key);
				}
				if(!StringUtils.equals(key, setting.key)) {
					manager.deleteSystemSetting(sid, key);
				}
					
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<DomainSetting.List> pl = ServletUtils.getPayloadAsList(request, DomainSetting.List.class);
				DomainSetting setting = pl.data.get(0);
				
				final CompositeId ci = new CompositeId(2).parse(setting.id);
				final String sid = ci.getToken(0);
				final String key = ci.getToken(1);

				if(!manager.deleteSystemSetting(sid, key)) {
					throw new WTException("Cannot delete setting [{0}, {1}]", sid, key);
				}
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSettings", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
}
