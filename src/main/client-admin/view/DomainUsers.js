/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.DomainUsers', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.Data',
		'Sonicle.Utils',
		'Sonicle.webtop.core.admin.model.GridUser'
	],
	uses: [
		'Sonicle.webtop.core.admin.ux.UserDeleteScopeConfirmBox',
		'Sonicle.webtop.core.admin.view.User',
		'Sonicle.webtop.core.admin.view.Options'
	],
	
	/**
	 * @cfg {String} domainId
	 * Target domain ID.
	 */
	domainId: null,
	
	dirScheme: null,
	dirCapPasswordWrite: false,
	dirCapUsersWrite: false,
	
	/**
	 * @private
	 * @property {Object} pwdPolicies 
	 */
	
	dockableConfig: {
		title: '{domainUsers.tit}',
		iconCls: 'wtadm-icon-users'
	},
	actionsResPrefix: 'domainUsers',
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
		me.callParent([cfg]);
		
		if (!cfg.title) {
			me.setBind({
				title: Ext.String.format('[{0}] ', cfg.domainId || '') + '{_viewTitle}'
			});
		}
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.initActions();
		me.initCxm();
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.admin.model.GridUser',
				proxy: WTF.proxy(me.mys.ID, 'ManageDomainUsers', null, {
					extraParams: {
						domainId: me.domainId,
						crud: 'read'
					},
					writer: {
						allowSingle: false // Always wraps records into an array
					}
				}),
				listeners: {
					remove: function(s, recs) {
						// Fix for updating selection
						me.lref('gp').getSelectionModel().deselect(recs);
					}
				}
			},
			viewConfig: {
				getRowClass: function(rec) {
					if (rec.get('exist') === false) return 'wt-theme-text-lighter2';
					if (rec.get('enabled') === false) return 'wt-text-striked wt-theme-text-error';
					return '';
				}
			},
			selModel: {
				type: 'rowmodel',
				mode : 'MULTI'
			},
			columns: [
				{
					xtype: 'rownumberer'	
				}, {
					dataIndex: 'userId',
					header: me.mys.res('domainUsers.gp.userId.lbl'),
					flex: 1
				}, {
					dataIndex: 'displayName',
					header: me.mys.res('domainUsers.gp.displayName.lbl'),
					flex: 2
				}, {
					dataIndex: 'userSid',
					header: me.res('domainUsers.gp.userSid.lbl'),
					tdCls: 'x-selectable',
					hidden: true,
					flex: 1
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'exist',
					header: WTF.headerWithGlyphIcon('fas fa-user'),
					getIconCls: function(v,rec) {
						return v ? 'wt-icon-ok' : '';
					},
					getTip: function(v) {
						return v ? me.mys.res('domainUsers.gp.exist.tip', WT.getPlatformName()) : null;
					},
					iconSize: WTU.imgSizeToPx('xs'),
					width: 40
				}, {
					dataIndex: 'dirDisplayName',
					header: me.mys.res('domainUsers.gp.dirDisplayName.lbl'),
					flex: 2
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'fas fa-key',
							tooltip: WT.res('act-changePassword.lbl'),
							hidden: !me.dirCapPasswordWrite,
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return !me.dirCapPasswordWrite || !rec.get('exist');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.changePasswordUI(rec);
							}
						}, {
							iconCls: 'fas fa-user-plus',
							tooltip: WT.res('act-add.lbl'),
							isActionHidden: function(s, ridx, cidx, itm, rec) {
								return rec.get('exist');
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return rec.get('exist');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.addUserUI(rec);
							}
						}, {
							iconCls: 'far fa-edit',
							tooltip: WT.res('act-edit.lbl'),
							isActionHidden: function(s, ridx, cidx, itm, rec) {
								return !rec.get('exist');
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return !rec.get('exist');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.editUserUI(rec);
							}
						}, {
							iconCls: 'fas fa-cog',
							tooltip: WT.res('opts.tit'),
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return !rec.get('exist');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.editOptionsUI(rec);
							}
						}, {
							iconCls: 'far fa-trash-alt',
							tooltip: WT.res('act-remove.lbl'),
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return !rec.get('exist');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteUserUI(rec);
							}
						}
					]
				}
			],
			tbar: [
				me.addAct('add', {
					text: WT.res('act-add.lbl'),
					tooltip: null,
					iconCls: null,
					menu: [
						me.addAct('addEmpty', {
							tooltip: null,
							disabled: !me.dirCapUsersWrite,
							handler: function() {
								me.addUserUI(null);
							}
						}),
						me.getAct('addImport')
					]
				}),
				'-',
				me.addAct('enable', {
					text: WT.res('act-enable.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-item-enable',
					disabled: true,
					handler: function() {
						var sel = me.getSelectedUsers();
						if (sel.length > 0) me.updateUserStatusUI(sel[0], true);
					}
				}),
				me.addAct('disable', {
					text: WT.res('act-disable.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-item-disable',
					disabled: true,
					handler: function() {
						var sel = me.getSelectedUsers();
						if (sel.length > 0) me.updateUserStatusUI(sel[0], false);
					}
				}),
				'->',
				me.addAct('refresh', {
					text: null,
					tooltip: WT.res('act-refresh.lbl'),
					iconCls: 'wt-icon-refresh',
					handler: function() {
						me.lref('gp').getStore().load();
					}
				})
			],
			listeners: {
				rowdblclick: function(s, rec) {
					if (rec.get('exist') === true) {
						me.editUserUI(rec);
					} else {
						me.addUserUI(rec);
					}
				},
				rowcontextmenu: function(s, rec, itm, i, e) {
					Sonicle.Utils.showContextMenu(e, me.getRef('cxmUserRow'));
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function() {
			me.updateDisabled('addImport');
			me.updateDisabled('edit');
			me.updateDisabled('changePassword');
			me.updateDisabled('editOptions');
			me.updateDisabled('updateEmailDomain');
			me.updateDisabled('enable');
			me.updateDisabled('disable');
		});
	},
	
	addUser: function(domainId, userId, displayName, firstName, lastName, opts) {
		opts = opts || {};
		if (!opts.pwdPolicies) Ext.raise('Missing pwdPolicies');
		if (!Ext.isBoolean(opts.askForPassword)) Ext.raise('Missing askForPassword');
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.User', {
				swapReturn: true,
				viewCfg: {
					domainId: domainId,
					policies: opts.pwdPolicies,
					askForPassword: opts.askForPassword
				}
			});

		vw.on('viewsave', function(s, success, model, op) {
			Ext.callback(opts.callback, opts.scope || me, [success, model, op]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {
					userId: userId,
					enabled: true,
					displayName: displayName,
					firstName: firstName,
					lastName: lastName
				}
			});
		});
	},
	
	editUser: function(domainId, userId, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.User', {
				swapReturn: true,
				viewCfg: {
					domainId: domainId
				}
			});

		vw.on('viewsave', function(s, success, model, op) {
			Ext.callback(opts.callback, opts.scope || me, [success, model, op]);
		});
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: userId
				}
			});
		});
	},
	
	updateUsersStatus: function(domainId, userIds, enable, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainUsers', {
			params: {
				crud: enable ? 'enable' : 'disable',
				domainId: domainId,
				ids: Sonicle.Utils.toJSONArray(Ext.Array.from(userIds))
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	updateUsersEmailDomain: function(domainId, userIds, domainPart, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainUsers', {
			params: {
				crud: 'updateEmailDomain',
				domainId: domainId,
				ids: Sonicle.Utils.toJSONArray(Ext.Array.from(userIds)),
				domainPart: domainPart
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	deleteUser: function(domainId, userId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainUser', {
			params: {
				crud: 'delete',
				domainId: domainId,
				id: userId,
				deep: opts.deep
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},	
	
	privates: {
		initActions: function() {
			var me = this;
			me.addAct('addImport', {
				tooltip: null,
				disabled: true,
				handler: function() {
					var sel = me.getSelectedUsers();
					if (sel.length > 0) me.addUserUI(sel[0]);
				}
			});
			me.addAct('edit', {
				text: WT.res('act-edit.lbl'),
				tooltip: null,
				handler: function() {
					var sel = me.getSelectedUsers();
					if (sel.length > 0) me.editUserUI(sel[0]);
				}
			});
			me.addAct('changePassword', {
				text: WT.res('act-changePassword.lbl'),
				tooltip: null,
				iconCls: 'wt-icon-changePassword',
				disabled: true,
				handler: function() {
					var sel = me.getSelectedUsers();
					if (sel.length > 0) me.changePasswordUI(sel[0]);
				}
			});
			me.addAct('editOptions', {
				text: WT.res('opts.tit'),
				tooltip: null,
				iconCls: 'wt-icon-options',
				disabled: true,
				handler: function() {
					var sel = me.getSelectedUsers();
					if (sel.length > 0) me.editOptionsUI(sel[0]);
				}
			});
			me.addAct('updateEmailDomain', {
				tooltip: null,
				handler: function() {
					var sel = me.getSelectedUsers();
					me.updateEmailDomainUI(sel);
				}
			});
		},

		initCxm: function() {
			var me = this;
			me.addRef('cxmUserRow', Ext.create({
				xtype: 'menu',
				items: [
					me.getAct('addImport'),
					me.getAct('edit'),
					'-',
					me.getAct('changePassword'),
					'-',
					me.getAct('editOptions'),
					me.getAct('updateEmailDomain')
				]
			}));
		},
		
		getSelectedUsers: function() {
			return this.lref('gp').getSelection();
		},
	
		updateDisabled: function(action) {
			var me = this,
				dis = me.isDisabled(action);
			me.setActDisabled(action, dis);
		},
		
		isDisabled: function(action) {
			var me = this, sel;
			switch(action) {
				case 'addImport':
					sel = me.getSelectedUsers();
					if (sel.length === 1) {
						return sel[0].get('exist');
					}
					return true;
				case 'edit':
				case 'editOptions':
					sel = me.getSelectedUsers();
					if (sel.length === 1) {
						return !sel[0].get('exist');
					}
					return true;
				case 'updateEmailDomain':
					sel = me.getSelectedUsers();
					if (sel.length > 0) {
						return me.countByExist(sel) !== sel.length;
					}
					return true;
				case 'changePassword':
					if (!me.dirCapPasswordWrite) return true;
					sel = me.getSelectedUsers();
					if (sel.length === 1) {
						return (me.dirScheme === 'webtop') && !sel[0].get('exist');
					}
					return true;
				case 'enable':
					sel = me.getSelectedUsers();
					if (sel.length === 1) {
						return !sel[0].get('exist') || sel[0].get('enabled') === true;
					}
					return true;
				case 'disable':
					sel = me.getSelectedUsers();
					if (sel.length === 1) {
						return !sel[0].get('exist') || !(sel[0].get('enabled') === true);
					}
					return true;
			}
		},
		
		countByExist: function(recs) {
			var i = 0;
			Ext.iterate(recs, function(rec) {
				if (rec.get('exist')) i++;
			});
			return i;
		},
		
		addUserUI: function(rec) {
			var me = this,
				copy = rec && (rec.get('exist') !== true),
				userId = copy ? rec.get('userId') : null,
				ddn = copy ? rec.get('dirDisplayName') : null,
				dfn = copy ? rec.get('dirFirstName') : null,
				dln = copy ? rec.get('dirLastName') : null;

			me.mys.lookupDomainPwdPolicies(me.domainId, {
				callback: function(success, data) {
					if (success) {
						me.addUser(me.domainId, userId, ddn, dfn, dln, {
							pwdPolicies: data,
							askForPassword: !copy,
							callback: function(success2) {
								if (success2) {
									me.lref('gp').getStore().load();
								}
							}
						});
					} else {
						WT.error('Password policies not loaded');
					}
				}
			});
		},
		
		editUserUI: function(rec) {
			var me = this,
				gp = me.lref('gp');
			
			me.editUser(me.domainId, rec.getId(), {
				callback: function(success, model) {
					if (success) gp.getStore().load();
				}
			});
		},
		
		updateUserStatusUI: function(recs, enabled) {
			var me = this,
				arecs = Ext.Array.from(recs),
				ids = Sonicle.Data.collectValues(arecs),
				doFn = function() {
					me.wait();
					me.updateUsersStatus(me.domainId, ids, enabled, {
						callback: function(success, data, json) {
							me.unwait();
							if (success) {
								Ext.iterate(arecs, function(rec) {
									rec.set('enabled', enabled);
								});
								me.updateDisabled('enable');
								me.updateDisabled('disable');
							} else {
								me.lref('gp').getStore().load();
							}
							WT.handleError(success, json);
						}
					});
				};
			
			if (enabled) {
				doFn();
			} else {
				WT.confirm(me.res('domainUsers.confirm.disable'), function(bid) {
					if (bid === 'yes') doFn();
				}, me);
			}
		},
		
		changePasswordUI: function(rec) {
			var me = this;
			me.mys.lookupDomainPwdPolicies(me.domainId, {
				callback: function(success, data) {
					if (success) {
						me.showChangePassword(data, WT.buildProfileId(me.domainId, rec.getId()));
					} else {
						WT.error('Password policies not loaded');
					}
				}
			});
		},
		
		editOptionsUI: function(rec) {
			var me = this,
				vw = WT.createView(me.mys.ID, 'view.Options', {
					swapReturn: true,
					viewCfg: {
						profileId: WT.buildProfileId(me.domainId, rec.getId()),
						profileDisplayName: rec.get('displayName')
					}
				});
			vw.showView();
		},
		
		deleteUserUI: function(rec) {
			var me = this,
				doFn = function(deep) {
					var key = deep ? 'domainUsers.confirm.delete.deep' : 'domainUsers.confirm.delete';
					WT.confirm(me.res(key), function(bid) {
						if (bid === 'yes') {
							me.wait();
							me.deleteUser(me.domainId, rec.getId(), {
								deep: deep,
								callback: function(success, data, json) {
									me.unwait();
									if (success) {
										if (deep) {
											me.lref('gp').getStore().remove(rec);
										} else {
											me.lref('gp').getStore().load();
										}
									}
									WT.handleError(success, json);
									WT.handleMessage(success, json);
								}
							});
						}
					}, me);
				};
			
			if (!me.dirCapUsersWrite) {
				doFn(false);
			} else {
				me.confirmDeleteScope(function(bid, value) {
					if (bid === 'ok') doFn(value === 'deep');
				}, me);
			}
		},
		
		updateEmailDomainUI: function(recs) {
			var me = this,
				arecs = Ext.Array.from(recs),
				ids = Sonicle.Data.collectValues(arecs);

			WT.prompt(me.res('domainUsers.prompt.updateEmailDomain'), {
				title: me.res('domainUsers.act-updateEmailDomain.lbl'),
				fn: function(bid, value, cfg) {
					if (bid === 'ok') {
						if (Ext.isEmpty(value)) {
							Ext.MessageBox.show(Ext.apply({}, {msg: cfg.msg}, cfg));
						} else {
							me.wait();
							me.updateUsersEmailDomain(me.domainId, ids, value, {
								callback: function(success, data, json) {
									me.unwait();
									WT.handleError(success, json);
								}
							});
						}
					}
				}
			});
		},
		
		showChangePassword: function(policies, profileId) {
			var me = this,
				vw = WT.createView(me.mys.ID, 'view.ChangePassword', {
					swapReturn: true,
					viewCfg: {
						showOldPassword: false,
						policies: policies,
						profileId: profileId
					}
				});
			vw.showView();
		},
		
		confirmDeleteScope: function(cb, scope) {
			var me = this;
			WT.confirm(me.res('domainUsers.confirm.delete.scope'), cb, scope, {
				buttons: Ext.Msg.OKCANCEL,
				instClass: 'Sonicle.webtop.core.admin.ux.UserDeleteScopeConfirmBox',
				instConfig: {
					thisText: me.res('domainUsers.confirm.delete.scope.this.txt'),
					deepText: me.res('domainUsers.confirm.delete.scope.deep.txt')
				},
				config: {
					value: 'this'
				}
			});
		}
	}
});
