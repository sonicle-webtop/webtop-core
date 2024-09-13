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
		'Sonicle.Utils',
		'Sonicle.webtop.core.admin.model.AdminNode'
	],
	uses: [
		'Sonicle.webtop.core.admin.view.Settings',
		'Sonicle.webtop.core.admin.view.Domain',
		'Sonicle.webtop.core.admin.view.DomainSettings',
		'Sonicle.webtop.core.admin.view.DomainGroups',
		'Sonicle.webtop.core.admin.view.DomainUsers',
		'Sonicle.webtop.core.admin.view.DomainResources',
		'Sonicle.webtop.core.admin.view.DomainRoles',
		'Sonicle.webtop.core.admin.view.DbUpgrader',
		'Sonicle.webtop.core.admin.view.DomainAccessLog',
		'Sonicle.webtop.core.admin.view.DomainLicenses',
		'Sonicle.webtop.core.admin.view.DomainLauncherLinks',
		'Sonicle.webtop.core.admin.view.License',
		'Sonicle.webtop.core.admin.view.PecBridge',
		'Sonicle.webtop.core.admin.view.PecBridgeFetcher',
		'Sonicle.webtop.core.admin.view.LogViewer',
		'Sonicle.webtop.core.admin.view.Loggers'
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
			items: [
				{
					xtype: 'sotogglebutton',
					reference: 'btnmaintenance',
					offIconCls: 'wtadm-icon-maintenance-off',
					onIconCls: 'wtadm-icon-maintenance-on',
					onText: me.res('btn-maintenance.on.lbl'),
					offText: me.res('btn-maintenance.off.lbl'),
					onTooltip: me.res('btn-maintenance.tip'),
					offTooltip: me.res('btn-maintenance.tip'),
					pressed: maint,
					toggleHandler: function(s, pressed) {
						me.setMaintenanceFlagUI(pressed);
					}
				}
			]
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
				columns: [
					{
						xtype: 'treecolumn',
						dataIndex: '_type',
						renderer: function(val, meta, rec) {
							if (val === 'domain') {
								return rec.get('text');
								
							} else {
								return me.res('node.' + val + '.lbl');
							}
						},
						flex: 1
					}
				],
				
				/*
				columns2: [{
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
				*/
				
				listeners: {
					itemclick: function(s, rec, itm, i, e) {
						var type = rec.get('_type');
						if (type === 'settings') {
							me.showSettingsUI(rec);
						} else if (type === 'daccesslog') {
							me.showDomainAccessLogUI(rec.parentNode.parentNode, rec);
						} else if (type === 'dsettings') {
							me.showDomainSettingsUI(rec.parentNode, rec);
						} else if (type === 'dgroups') {
							me.showDomainGroupsUI(rec.parentNode, rec);
						} else if (type === 'dusers') {
							me.showDomainUsersUI(rec.parentNode, rec);
						} else if (type === 'droles') {
							me.showDomainRolesUI(rec.parentNode, rec);
						} else if (type === 'dresources') {
							me.showDomainResourcesUI(rec.parentNode, rec);	
						} else if (type === 'dlicenses') {
							me.showDomainLicensesUI(rec.parentNode, rec);
						} else if (type === 'ddatasources') {
							me.showDomainDataSourcesUI(rec.parentNode, rec);	
						} else if (type === 'dlauncherlinks') {
							me.showDomainLauncherLinksUI(rec.parentNode, rec);
						} else if (type === 'dpecbridge') {
							me.showPecBridgeUI(rec.parentNode, rec);
						} else if (type === 'dbupgrader') {
							me.showDbUpgraderUI(rec);
						} else if (type === 'logging') {
							me.showLoggersUI(rec);
						} else if (type === 'logsviewer') {
							me.showLogViewerUI(rec);
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
							Sonicle.Utils.showContextMenu(e, me.getRef('cxmDomains'), {node: rec});
						} else if(type === 'domain') {
							Sonicle.Utils.showContextMenu(e, me.getRef('cxmDomain'), {node: rec});
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
		me.addAct('initCheckDomain', {
			ignoreSize: true,
			tooltip: null,
			handler: function() {
				var node = me.getCurrentDomainNode();
				if(node) me.initCheckDomainUI(node);
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
				'-',
				me.getAct('initCheckDomain'),
				'-',
				me.getAct('deleteDomain')
			],
			listeners: {
				beforeshow: function(s) {
					me.updateDisabled('editDomain');
					me.updateDisabled('initCheckDomain');
					me.updateDisabled('deleteDomain');
				}
			}
		}));
	},
	
	getCurrentDomainNode: function() {
		var sel = this.trAdmin().getSelection();
		return sel.length > 0 ? sel[0] : null;
	},
	
	setMaintenanceFlagUI: function(status) {
		var me = this,
			s = status ? 'on' : 'off',
			reset = function(state) {
				me.btnMaintenance().toggle(state, true);
			};
		
		WT.confirmOk(me.res('btn-maintenance.confirm.'+s), function(bid) {
			if (bid === 'ok') {
				me.setMaintenanceFlag(status, {
					callback: function(success, json) {
						WT.handleMessage(success, json);
						if (success) {
							WT.toast(me.res('btn-maintenance.info.'+s));
						} else {
							reset(!status);
						}
					}
				});
			} else {
				reset(!status);
			}
		}, me, {
			title: me.res('btn-maintenance.confirm.tit'),
			okText: me.res('btn-maintenance.confirm.'+s+'.ok')
		});
	},
	
	setMaintenanceFlag: function(status, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetMaintenanceFlag', {
			params: {
				value: status
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	addDomain: function(opts) {
		opts = opts || {};
		var me = this,
			vct = WT.createView(me.ID, 'view.Domain');
		
		vct.getView().on('viewsave', function(s, success, model, op) {
			Ext.callback(opts.callback, opts.scope || me, [success, model, op]);
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
		
		vct.getView().on('viewsave', function(s, success, model, op) {
			Ext.callback(opts.callback, opts.scope || me, [success, model, op]);
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
		WT.ajaxReq(me.ID, 'ManageDomain', {
			params: {
				crud: 'delete',
				id: domainId,
				deep: opts.deep
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	initDomain: function(domainId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomain', {
			params: {
				crud: 'init',
				id: domainId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	lookupDomainPwdPolicies: function(domainId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageDomain', {
			params: {
				crud: 'policies',
				id: domainId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	geolocateIPs: function(ips, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(WT.ID, 'GeolocateIP', {
			params: {
				ips: Ext.Array.from(ips)
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
	
	
	/*
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
	
	addUser: function(askForPassword, pwdPolicies, domainId, userId, firstName, lastName, displayName, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.User', {
					viewCfg: {
						domainId: domainId,
						askForPassword: askForPassword,
						policies: pwdPolicies
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
	
	
	
	*/
	
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
					keepCopy: true
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
	
	privates: {
		
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

		showLoggersUI: function(node) {
			var me = this,
				itemId = WTU.forItemId(node.getId());

			me.showTab(itemId, function() {
				return Ext.create('Sonicle.webtop.core.admin.view.Loggers', {
					mys: me,
					itemId: itemId,
					closable: true
				});
			});
		},

		showLogViewerUI: function(node) {
			var me = this,
				itemId = WTU.forItemId(node.getId());

			me.showTab(itemId, function() {
				return Ext.create('Sonicle.webtop.core.admin.view.LogViewer', {
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
					domainId: domNode.get('_domainId'),
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
					domainId: domNode.get('_domainId'),
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
					domainId: domNode.get('_domainId'),
					dirScheme: domNode.get('_dirScheme'),
					dirCapPasswordWrite: domNode.get('_dirCapPasswordWrite'),
					dirCapUsersWrite: domNode.get('_dirCapUsersWrite'),
					closable: true
				});
			});
		},

		showDomainResourcesUI: function(domNode, node) {
			var me = this,
				itemId = WTU.forItemId(node.getId());

			me.showTab(itemId, function() {
				return Ext.create('Sonicle.webtop.core.admin.view.DomainResources', {
					mys: me,
					itemId: itemId,
					domainId: domNode.get('_domainId'),
					domainName: domNode.get('_domainName'),
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
					domainId: domNode.get('_domainId'),
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
					domainId: domNode.get('_domainId'),
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
					domainId: domNode.get('_domainId'),
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
					domainId: domNode.get('_domainId'),
					closable: true
				});
			});
		},

		showDomainDataSourcesUI: function(domNode, node) {
			var me = this,
				itemId = WTU.forItemId(node.getId());

			me.showTab(itemId, function() {
				return Ext.create('Sonicle.webtop.core.admin.view.DomainDataSources', {
					mys: me,
					itemId: itemId,
					domainId: domNode.get('_domainId'),
					closable: true
				});
			});
		},

		showDomainAccessLogUI: function(domNode, node) {
			var me = this,
				itemId = WTU.forItemId(node.getId());

			me.showTab(itemId, function() {
				return Ext.create('Sonicle.webtop.core.admin.view.DomainAccessLog', {
					mys: me,
					itemId: itemId,
					domainId: domNode.get('_domainId'),
					closable: true
				});
			});
		},
		
		initCheckDomainUI: function(node) {
			var me = this;
			WT.confirmOk(me.res('domain.confirm.init.check', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
				if (bid === 'ok') {
					//me.wait();
					me.initDomain(node.get('_domainId'), {
						callback: function(success, data, json) {
							//me.unwait();
							WT.handleError(success, json);
							if (success && !WT.containsResponseMessage(json)) {
								WT.info(me.res('domain.info.init.ok'));
							} else {
								WT.handleMessage(success, json);
							}
						}
					});
				}
			}, me, {
				title: me.res('domain.tit'),
				okText: me.res('domain.confirm.init.check.ok')
			});
		},
		
		addDomainUI: function() {
			var me = this,
				doInitFn = function(domainId) {
					me.initDomain(domainId, {
						callback: function(success, data, json) {
							WT.handleError(success, json);
							if (success && !WT.containsResponseMessage(json)) {
								WT.info(me.res('domain.info.init.ok'));
							} else {
								WT.handleMessage(success, json);
							}
						}
					});
				};
			me.addDomain({
				callback: function(success, mo, op) {
					if (success) {
						var domainId = mo.get('domainId'),
							hasRespMsg = WT.containsResponseMessage(op);
						
						me.loadTreeNode('domains');
						if (hasRespMsg) {
							WT.handleMessage(success, op, 'warn', {
								fn: function() {
									WT.confirm(me.res('domain.confirm.init.anyway'), function(bid) {
										if (bid === 'yes') doInitFn(domainId);
									}, me, {
										yesText: me.res('domain.confirm.init.anyway.yes')
									});
								}
							});
						} else {
							doInitFn(domainId);
						}
					}
				}
			});
		},
		
		editDomainUI: function(node) {
			this.editDomain(node.get('_domainId'), {
				callback: function(success, mo, op) {
					WT.handleMessage(success, op);
					if (success) {
						//WT.handleOperationMessage(op);
						this.loadTreeNode('domains');
					}
				}
			});
		},

		deleteDomainUI: function(node) {
			var me = this,
				doFn = function(deep) {
					//me.wait();
					me.deleteDomain(node.get('_domainId'), {
						deep: deep,
						callback: function(success, data, json) {
							//me.unwait();
							if (success) node.remove();
							WT.handleError(success, json);
							WT.handleMessage(success, json);
						}
					});
				};
			
			WT.confirmDelete(me.res('domain.confirm.delete', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
				if (bid === 'ok') {
					if (node.get('_dirCapUsersWrite') === true) {
						WT.confirmYNC(me.res('domain.confirm.delete.deep'), function(bid2) {
							if (Sonicle.String.isIn(bid2, ['yes', 'no'])) doFn(bid2 === 'yes');
						}, me, {
							yesText: me.res('domain.confirm.delete.deep.yes'),
							noText: me.res('domain.confirm.delete.deep.no')
						});
					} else {
						doFn(false);
					}
				}
			}, me);
		},
		
		showTab: function(itemId, createFn) {
			var me = this,
				pnl = me.getMainComponent(),
				tab;

			tab = pnl.getComponent(itemId);
			if (!tab) tab = pnl.add(createFn());
			pnl.setActiveTab(tab);
		},

		loadTreeNode: function(node) {
			var me = this,
				sto = me.trAdmin().getStore(),
				no;
			if (node && node.isNode) {
				no = node;
			} else {
				no = sto.getNodeById(node);
			}
			if (no) sto.load({node: no});
		}
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
			case 'initCheckDomain':
			case 'deleteDomain':
				sel = me.getCurrentDomainNode();
				return sel ? me.getVar('modeSingleDomain') : true;
			case 'addDomain':
				return me.getVar('modeSingleDomain');
				//if(!me.isPermitted('STORE_OTHER', 'CREATE')) return true;
		}
	}
});
