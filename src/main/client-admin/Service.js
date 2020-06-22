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
Ext.define('Sonicle.webtop.core.admin.Service', {
	extend: 'WTA.sdk.Service',
	requires: [
		'Sonicle.webtop.core.admin.model.AdminNode',
		'Sonicle.webtop.core.admin.view.Settings',
		'Sonicle.webtop.core.admin.view.Domain',
		'Sonicle.webtop.core.admin.view.DomainSettings',
		'Sonicle.webtop.core.admin.view.DomainGroups',
		'Sonicle.webtop.core.admin.view.DomainUsers',
		'Sonicle.webtop.core.admin.view.DomainRoles',
		'Sonicle.webtop.core.admin.view.DomainLauncherLinks',
		'Sonicle.webtop.core.admin.view.PecBridge',
		'Sonicle.webtop.core.admin.view.PecBridgeFetcher',
		'Sonicle.webtop.core.admin.view.DomainLicenses',
		'Sonicle.webtop.core.admin.view.DbUpgrader'
	],
	uses: [
		'Sonicle.webtop.core.admin.view.License',
		'Sonicle.webtop.core.admin.view.LicenseLease'
	],
	
	init: function() {
		var me = this,
				maint = WT.getApp().getDescriptor(WT.ID).getMaintenance();
		
		me.initActions();
		me.initCxm();
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,
			iconCls: '',
			items: [{
				xtype: 'button',
				reference: 'btnmaintenance',
				text: me.res(maint ? 'btn-maintenance.on.lbl' : 'btn-maintenance.off.lbl'),
				enableToggle: true,
				pressed: maint,
				iconCls: maint ? 'wtadm-icon-maintenance-on' : 'wtadm-icon-maintenance-off',
				toggleHandler: function(s,state) {
					me.setMaintenanceFlag(state);
				},
				listeners: {
					toggle: function(s, pressed) {
						s.setText(me.res(pressed ? 'btn-maintenance.on.lbl' : 'btn-maintenance.off.lbl'));
						s.setIconCls(pressed ? 'wtadm-icon-maintenance-on' : 'wtadm-icon-maintenance-off');
					}
				}
			}]
		}));
		
		me.setToolComponent(Ext.create({
			xtype: 'panel',
			layout: 'border',
			referenceHolder: true,
			title: me.getName(),
			items: [{
				region: 'center',
				xtype: 'treepanel',
				reference: 'tradmin',
				border: false,
				useArrows: true,
				rootVisible: false,
				store: {
					autoLoad: true,
					autoSync: true,
					model: 'Sonicle.webtop.core.admin.model.AdminNode',
					proxy: WTF.apiProxy(me.ID, 'ManageAdminTree', 'children', {
						writer: {
							allowSingle: false // Always wraps records into an array
						}
					}),
					root: {
						id: 'root',
						expanded: true
					}
				},
				hideHeaders: true,
				columns: [{
					xtype: 'treecolumn',
					dataIndex: '_type',
					flex: 1,
					renderer: function(val, meta, rec) {
						if (val === 'domain') {
							return rec.get('text');
						} else {
							var s = val;
							if (val === 'settings' && !Ext.isEmpty(rec.get('_domainId'))) {
								s += '.domain';
							}
							return me.res('node.type.'+s);
						}
					}
				}],
				listeners: {
					itemclick: function(s, rec, itm, i, e) {
						var type = rec.get('_type');
						if (type === 'settings') {
							if (!Ext.isEmpty(rec.get('_domainId'))) {
								me.showDomainSettingsUI(rec.parentNode, rec);
							} else {
								me.showSettingsUI(rec);
							}
						} else if (type === 'groups') {
							me.showDomainGroupsUI(rec.parentNode, rec);
						} else if (type === 'users') {
							me.showDomainUsersUI(rec.parentNode, rec);
						} else if (type === 'roles') {
							me.showDomainRolesUI(rec.parentNode, rec);
						} else if (type === 'launcherlinks') {
							me.showDomainLauncherLinksUI(rec.parentNode, rec);
						} else if (type === 'pecbridge') {
							me.showPecBridgeUI(rec.parentNode, rec);
						} else if (type === 'licenses') {
							me.showDomainLicensesUI(rec.parentNode, rec);
						} else if (type === 'dbupgrader') {
							me.showDbUpgraderUI(rec);
						}
					},
					itemdblclick: function(s, rec, itm, i, e) {
						var type = rec.get('_type');
						if (type === 'domain') {
							me.editDomainUI(rec);
						}
					},
					itemcontextmenu: function(s, rec, itm, i, e) {
						var type = rec.get('_type');
						if(type === 'domains') {
							WT.showContextMenu(e, me.getRef('cxmDomains'), {node: rec});
						} else if(type === 'domain') {
							WT.showContextMenu(e, me.getRef('cxmDomain'), {node: rec});
						}
					}
				}
			}]
		}));
		
		me.setMainComponent(Ext.create({
			xtype: 'tabpanel'
		}));
	},
	
	btnMaintenance: function() {
		return this.getToolbar().lookupReference('btnmaintenance');
	},
	
	trAdmin: function() {
		return this.getToolComponent().lookupReference('tradmin');
	},
	
	initActions: function() {
		var me = this;
		me.addAct('addDomain', {
			ignoreSize: true,
			tooltip: null,
			handler: function() {
				me.addDomainUI();
			}
		});
		me.addAct('editDomain', {
			ignoreSize: true,
			tooltip: null,
			handler: function() {
				var node = me.getCurrentDomainNode();
				if(node) me.editDomainUI(node);
			}
		});
		me.addAct('deleteDomain', {
			ignoreSize: true,
			tooltip: null,
			handler: function() {
				var node = me.getCurrentDomainNode();
				if(node) me.deleteDomainUI(node);
			}
		});
	},
	
	initCxm: function() {
		var me = this;
		me.addRef('cxmDomains', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('addDomain')
			],
			listeners: {
				beforeshow: function(s) {
					me.updateDisabled('addDomain');
				}
			}
		}));
		me.addRef('cxmDomain', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('editDomain'),
				me.getAct('deleteDomain'),
				me.getAct('addDomain')
			],
			listeners: {
				beforeshow: function(s) {
					me.updateDisabled('editDomain');
					me.updateDisabled('deleteDomain');
					me.updateDisabled('addDomain');
				}
			}
		}));
	},
	
	getCurrentDomainNode: function() {
		var sel = this.trAdmin().getSelection();
		return sel.length > 0 ? sel[0] : null;
	},
	
	setMaintenanceFlag: function(active) {
		var me = this;
		//me.wait();
		WT.ajaxReq(me.ID, 'SetMaintenanceFlag', {
			params: {value: active},
			callback: function(success, o) {
				//me.unwait();
				if(success) {
					me.btnMaintenance().toggle(active);
					WT.info(me.res(active ? 'btn-maintenance.info.on' : 'btn-maintenance.info.off'));
				} else {
					me.btnMaintenance().toggle(!active, true);
					WT.error(o.message);
				}
			}
		});
	},
	
	showSettingsUI: function(node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.Settings', {
				mys: me,
				itemId: itemId,
				closable: true
			});
		});
	},
	
	showDbUpgraderUI: function(node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.DbUpgrader', {
				mys: me,
				itemId: itemId,
				closable: true
			});
		});
	},
	
	showDomainSettingsUI: function(domNode, node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.DomainSettings', {
				mys: me,
				itemId: itemId,
				domainId: node.get('_domainId'),
				closable: true
			});
		});
	},
	
	showDomainGroupsUI: function(domNode, node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.DomainGroups', {
				mys: me,
				itemId: itemId,
				domainId: node.get('_domainId'),
				closable: true
			});
		});
	},
	
	showDomainUsersUI: function(domNode, node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.DomainUsers', {
				mys: me,
				itemId: itemId,
				domainId: node.get('_domainId'),
				passwordPolicy: domNode.get('_passwordPolicy'),
				dirScheme: domNode.get('_dirScheme'),
				dirCapPasswordWrite: domNode.get('_dirCapPasswordWrite'),
				dirCapUsersWrite: domNode.get('_dirCapUsersWrite'),
				closable: true
			});
		});
	},
	
	showDomainRolesUI: function(domNode, node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.DomainRoles', {
				mys: me,
				itemId: itemId,
				domainId: node.get('_domainId'),
				closable: true
			});
		});
	},
	
	showDomainLauncherLinksUI: function(domNode, node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.DomainLauncherLinks', {
				mys: me,
				itemId: itemId,
				domainId: node.get('_domainId'),
				closable: true
			});
		});
	},
	
	showPecBridgeUI: function(domNode, node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.PecBridge', {
				mys: me,
				itemId: itemId,
				domainId: node.get('_domainId'),
				closable: true
			});
		});
	},
	
	showDomainLicensesUI: function(domNode, node) {
		var me = this,
				itemId = WTU.forItemId(node.getId());
		
		me.showTab(itemId, function() {
			return Ext.create('Sonicle.webtop.core.admin.view.DomainLicenses', {
				mys: me,
				itemId: itemId,
				domainId: node.get('_domainId'),
				closable: true
			});
		});
	},
	
	addDomainUI: function() {
		var me = this;
		me.addDomain({
			callback: function(success, mo) {
				if(success) {
					me.loadTreeNode('domains');
					me.initDomain(mo.get('domainId'), {
						callback: function(success) {
							if(success) WT.info(me.res('domain.info.init'));
						}
					});
				}
			}
		});
	},
	
	editDomainUI: function(node) {
		this.editDomain(node.get('_domainId'), {
			callback: function(success) {
				if(success) this.loadTreeNode('domains');
			}
		});
	},
	
	deleteDomainUI: function(node) {
		var me = this;
		WT.confirm(me.res('domain.confirm.delete', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if(bid === 'yes') {
				me.deleteDomain(node.get('_domainId'), {
					callback: function(success) {
						if(success) node.remove();
					}
				});
			}
		});
	},
	
	addDomain: function(opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Domain');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					enabled: true,
					userAutoCreation: false,
					authCaseSensitive: false,
					authPasswordPolicy: true
				}
			});
		});
	},
	
	editDomain: function(domainId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Domain');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					id: domainId
				}
			});
		});
	},
	
	deleteDomain: function(domainId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomains', {
			params: {
				crud: 'delete',
				domainId: domainId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	initDomain: function(domainId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomains', {
			params: {
				crud: 'init',
				domainId: domainId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	addGroup: function(domainId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Group', {
					viewCfg: {
						domainId: domainId
					}
				});
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					domainId: domainId
				}
			});
		});
	},
	
	editGroup: function(profileId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Group', {
					viewCfg: {
						domainId: WT.fromPid(profileId).domainId
					}
				});
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					profileId: profileId
				}
			});
		});
	},
	
	deleteGroups: function(profileIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomainGroups', {
			params: {
				crud: 'delete',
				profileIds: WTU.arrayAsParam(profileIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	addLicense: function(domainId, productId, string, activateNow, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'create',
				domainId: domainId,
				productId: productId,
				string: string,
				activate: activateNow
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	changeLicense: function(domainId, productId, nstring, astring, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'change',
				domainId: domainId,
				productId: productId,
				nstring: nstring,
				astring: astring
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	deleteLicense: function(domainId, productId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'delete',
				domainId: domainId,
				productId: productId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	getLicenseModifyReqInfo: function(domainId, productId, modKey, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'modreqinfo',
				domainId: domainId,
				productId: productId,
				modKey: modKey
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	getLicenseActivatorReqInfo: function(type, domainId, productId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'actreqinfo',
				domainId: domainId,
				productId: productId,
				type: type
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	modifyLicense: function(domainId, productId, modKey, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'modify',
				domainId: domainId,
				productId: productId,
				modKey: modKey
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	activateLicense: function(domainId, productId, astring, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'activate',
				domainId: domainId,
				productId: productId,
				astring: astring
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	deactivateLicense: function(domainId, productId, dstring, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'deactivate',
				domainId: domainId,
				productId: productId,
				dstring: dstring
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	assignLicenseLease: function(domainId, productId, userIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'assignlease',
				domainId: domainId,
				productId: productId,
				userIds: userIds
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	revokeLicenseLease: function(domainId, productId, userIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageLicense', {
			params: {
				crud: 'revokelease',
				domainId: domainId,
				productId: productId,
				userIds: userIds
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	addUser: function(askForPassword, passwordPolicy, domainId, userId, firstName, lastName, displayName, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.User', {
					viewCfg: {
						domainId: domainId,
						askForPassword: askForPassword,
						passwordPolicy: passwordPolicy
					}
				});
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					domainId: domainId,
					userId: userId,
					enabled: true,
					firstName: firstName,
					lastName: lastName,
					displayName: displayName
				}
			});
		});
	},
	
	editUser: function(profileId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.User', {
					viewCfg: {
						domainId: WT.fromPid(profileId).domainId
					}
				});
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					profileId: profileId
				}
			});
		});
	},
	
	updateUsersStatus: function(profileIds, enabled, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomainUsers', {
			params: {
				crud: enabled ? 'enable' : 'disable',
				profileIds: WTU.arrayAsParam(profileIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	changeUserPassword: function(profileId, oldPassword, newPassword, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ChangeUserPassword', {
			params: {
				profileId: profileId,
				oldPassword: oldPassword,
				newPassword: newPassword
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateUsersEmailDomain: function(profileIds, domainPart, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomainUsers', {
			params: {
				crud: 'updateEmailDomain',
				profileIds: WTU.arrayAsParam(profileIds),
				domainPart: domainPart
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	deleteUsers: function(deep, profileIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomainUsers', {
			params: {
				crud: 'delete',
				deep: deep,
				profileIds: WTU.arrayAsParam(profileIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	addRole: function(domainId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Role');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					domainId: domainId
				}
			});
		});
	},
	
	editRole: function(roleUid, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Role');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					roleUid: roleUid
				}
			});
		});
	},
	
	addPecBridgeFetcher: function(domainId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.PecBridgeFetcher');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					domainId: domainId,
					keepCopy: false
				}
			});
		});
	},
	
	editPecBridgeFetcher: function(fetcherId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.PecBridgeFetcher');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					fetcherId: fetcherId
				}
			});
		});
	},
	
	addPecBridgeRelay: function(domainId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.PecBridgeRelay');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					domainId: domainId
				}
			});
		});
	},
	
	editPecBridgeRelay: function(relayId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.PecBridgeRelay');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					relayId: relayId
				}
			});
		});
	},
	
	showTab: function(itemId, createFn) {
		var me = this,
				pnl = me.getMainComponent(),
				tab;
		
		tab = pnl.getComponent(itemId);
		if(!tab) tab = pnl.add(createFn());
		pnl.setActiveTab(tab);
	},
	
	loadTreeNode: function(node) {
		var me = this,
				sto = me.trAdmin().getStore(),
				no;
		if(node && node.isNode) {
			no = node;
		} else {
			no = sto.getNodeById(node);
		}
		if(no) sto.load({node: no});
	},
	
	/**
	 * @private
	 */
	updateDisabled: function(action) {
		var me = this,
				dis = me.isDisabled(action);
		me.setActDisabled(action, dis);
	},
	
	/**
	 * @private
	 */
	isDisabled: function(action) {
		var me = this, sel;
		switch(action) {
			case 'editDomain':
				sel = me.getCurrentDomainNode();
				return sel ? false : true;
			case 'deleteDomain':
				sel = me.getCurrentDomainNode();
				return sel ? false : true;
			case 'addDomain':
				return false;
				//if(!me.isPermitted('STORE_OTHER', 'CREATE')) return true;
		}
	}
});
