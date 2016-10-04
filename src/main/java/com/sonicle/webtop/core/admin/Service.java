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
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.ODomain;
import com.sonicle.webtop.core.bol.model.Setting;
import com.sonicle.webtop.core.sdk.BaseService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String nodeId = ServletUtils.getStringParameter(request, "node", true);
				
				if(nodeId.equals("root")) { // Admin roots...
					children.add(createTreeNode(NTYPE_SETTINGS, NTYPE_SETTINGS, "Proprietà", true, "wta-icon-settings-xs"));
					children.add(createTreeNode(NTYPE_DOMAINS, NTYPE_DOMAINS, "Domini", false, "wta-icon-domains-xs"));
					children.add(createTreeNode(NTYPE_DBUPGRADER, NTYPE_DBUPGRADER, "DB Upgrade", true, "wta-icon-dbUpgrader-xs"));
				} else {
					CompositeId cid = new CompositeId(3).parse(nodeId, true);
					if(cid.getToken(0).equals("domains")) {
						if(cid.hasToken(1)) {
							children.add(createDomainChildNode(nodeId, "Gruppi", "wta-icon-domainGroups-xs", NTYPE_GROUPS, cid.getToken(1)));
							children.add(createDomainChildNode(nodeId, "Utenti", "wta-icon-domainUsers-xs", NTYPE_USERS, cid.getToken(1)));
							children.add(createDomainChildNode(nodeId, "Ruoli", "wta-icon-domainRoles-xs", NTYPE_ROLES, cid.getToken(1)));
							children.add(createDomainChildNode(nodeId, "Proprietà", "wta-icon-domainSettings-xs", NTYPE_SETTINGS, cid.getToken(1)));
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
	
	public void processManageSettings(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				List<Setting> items = manager.listSystemSettings();
				new JsonResult("settings", items, items.size()).printTo(out);
				
				/*
				Integer id = ServletUtils.getIntParameter(request, "id", null);
				if(id == null) {
					List<ActivityGrid> items = core.listLiveActivities(queryDomains());
					new JsonResult("activities", items, items.size()).printTo(out);
				} else {
					OActivity item = core.getActivity(id);
					new JsonResult(item).printTo(out);
				}
				*/
				
			}/* else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.addActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.updateActivity(pl.data);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, OActivity> pl = ServletUtils.getPayload(request, OActivity.class);
				core.deleteActivity(pl.data.getActivityId());
				new JsonResult().printTo(out);
			}*/
			
		} catch(Exception ex) {
			logger.error("Error in ManageActivities", ex);
			new JsonResult(false, "Error").printTo(out);
			
		}
	}
}
