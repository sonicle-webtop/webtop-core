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
Ext.define('Sonicle.webtop.core.admin.view.DomainUsers', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.admin.model.GridDomainUser'
	],
	uses: [
		'Sonicle.webtop.core.admin.view.Options'
	],
	
	domainId: null,
	passwordPolicy: false,
	authCapPasswordWrite: false,
	authCapUsersWrite: false,
	
	dockableConfig: {
		title: '{domainUsers.tit}',
		iconCls: 'wtadm-icon-users-xs'
	},
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		if(!cfg.title) {
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
				model: 'Sonicle.webtop.core.admin.model.GridDomainUser',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainUsers', 'users', {
					extraParams: {
						domainId: me.domainId
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
					return rec.get('enabled') === false ? 'wtadm-gpusers-row-disabled' : '';
				}
			},
			selModel: {
				type: 'rowmodel',
				mode : 'MULTI'
			},
			columns: [{
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
				xtype: 'soiconcolumn',
				dataIndex: 'exist',
				header: WTF.headerWithGlyphIcon('fa fa-user'),
				getIconCls: function(v,rec) {
					return v ? 'wt-icon-ok-xs' : '';
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
			}],
			tbar: [
				me.addAct('add', {
					text: WT.res('act-add.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-add-xs',
					menu: [
						me.addAct('addEmpty', {
							text: me.mys.res('domainUsers.act-addEmpty.lbl'),
							tooltip: null,
							disabled: !me.authCapUsersWrite,
							handler: function() {
								me.addUserUI(null);
							}
						}),
						me.getAct('addImport')
					]
				}),
				me.addAct('remove', {
					text: WT.res('act-remove.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-remove-xs',
					disabled: true,
					menu: [
						me.addAct('removeClean', {
							text: me.mys.res('domainUsers.act-removeClean.lbl'),
							tooltip: null,
							handler: function() {
								var sel = me.getSelectedUsers();
								if (sel.length > 0) me.deleteUserUI(false, sel[0]);
							}
						}),
						me.addAct('removeDeep', {
							text: me.mys.res('domainUsers.act-removeDeep.lbl'),
							tooltip: null,
							disabled: !me.authCapUsersWrite,
							handler: function() {
								var sel = me.getSelectedUsers();
								if (sel.length > 0) me.deleteUserUI(true, sel[0]);
							}
						})
					]
				}),
				me.getAct('changePassword'),
				'-',
				me.addAct('enable', {
					text: WT.res('act-enable.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-item-enable-xs',
					disabled: true,
					handler: function() {
						var sel = me.getSelectedUsers();
						if (sel.length > 0) me.updateUserStatusUI(sel[0], true);
					}
				}),
				me.addAct('disable', {
					text: WT.res('act-disable.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-item-disable-xs',
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
					WT.showContextMenu(e, me.getRef('cxmUserRow'));
					/*
					var selection = s.getSelection();
					me.getAct('sendContact').setDisabled(false)
					Ext.each(selection,function(sel){
						if(sel.get('isList')){
							me.getAct('sendContact').setDisabled(true)
						}
					});
					WT.showContextMenu(e, me.getRef('cxmGrid'), {
						contact: rec,
						contacts: s.getSelection()
					});
					*/
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function() {
			me.updateDisabled('addImport');
			me.updateDisabled('remove');
			me.updateDisabled('edit');
			me.updateDisabled('changePassword');
			me.updateDisabled('editOptions');
			me.updateDisabled('updateEmailDomain');
			me.updateDisabled('enable');
			me.updateDisabled('disable');
		});
	},
	
	initActions: function() {
		var me = this;
		me.addAct('addImport', {
			text: me.mys.res('domainUsers.act-addImport.lbl'),
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
			iconCls: 'wt-icon-changePassword-xs',
			disabled: true,
			handler: function() {
				var sel = me.getSelectedUsers();
				if (sel.length > 0) me.changePasswordUI(sel[0]);
			}
		});
		me.addAct('editOptions', {
			text: WT.res('opts.tit'),
			tooltip: null,
			iconCls: 'wt-icon-options-xs',
			disabled: true,
			handler: function() {
				var sel = me.getSelectedUsers();
				if (sel.length > 0) me.editOptionsUI(sel[0]);
			}
		});
		me.addAct('updateEmailDomain', {
			text: me.mys.res('domainUsers.act-updateEmailDomain.lbl'),
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
	
	addUserUI: function(rec) {
		var me = this,
				copy = rec && (rec.get('exist') !== true),
				usi = copy ? rec.get('userId') : null,
				fn = copy ? rec.get('dirFirstName') : null,
				ln = copy ? rec.get('dirLastName') : null,
				dn = copy ? rec.get('dirDisplayName') : null;
		me.mys.addUser(!copy, me.passwordPolicy, me.domainId, usi, fn, ln, dn, {
			callback: function(success) {
				if(success) {
					me.lref('gp').getStore().load();
				}
			}
		});
	},
	
	editUserUI: function(rec) {
		var me = this,
				pid = rec.get('profileId');
		me.mys.editUser(pid, {
			callback: function(success) {
				if(success) {
					me.lref('gp').getStore().load();
				}
			}
		});
	},
	
	deleteUserUI: function(deep, rec) {
		var me = this,
				key = deep ? 'domainUsers.confirm.delete.deep' : 'domainUsers.confirm.delete';
		
		WT.confirm(me.mys.res(key), function(bid) {
			if(bid === 'yes') {
				me.mys.deleteUsers(deep, [rec.get('profileId')], {
					callback: function(success) {
						if(success) {
							if(deep) {
								me.lref('gp').getStore().remove(rec);
							} else {
								rec.set('displayName', null);
								rec.set('exist', false);
							}
						}
					}
				});
			}
		}, me);	
	},
	
	changePasswordUI: function(rec) {
		var me = this,
				vct = WT.createView(me.mys.ID, 'view.ChangePassword', {
					viewCfg: {
						showOldPassword: false,
						passwordPolicy: me.passwordPolicy,
						profileId: rec.get('profileId')
					}
				});
		vct.show();
	},
	
	updateUserStatusUI: function(rec, enabled) {
		var me = this, doFn;
		
		doFn = function() {
			me.mys.updateUsersStatus([rec.get('profileId')], enabled, {
				callback: function(success) {
					if (success) {
						rec.set('enabled', enabled);
						me.updateDisabled('enable');
						me.updateDisabled('disable');
					}
				}
			});
		};
		if(enabled) {
			doFn();
		} else {
			WT.confirm(me.mys.res('domainUsers.confirm.disable'), function(bid) {
				if (bid === 'yes') doFn();
			}, me);
		}
	},
	
	editOptionsUI: function(rec) {
		var me = this,
				vw = WT.createView(me.mys.ID, 'view.Options', {
					swapReturn: true,
					viewCfg: {
						profileId: rec.get('profileId'),
						profileDisplayName: rec.get('displayName')
					}
				});
		vw.showView();
	},
	
	updateEmailDomainUI: function(sel) {
		var me = this;
		WT.prompt(me.mys.res('domainUsers.prompt.updateEmailDomain'), {
			title: me.mys.res('domainUsers.act-updateEmailDomain.lbl'),
			fn: function(bid, value, cfg) {
				if (bid === 'ok') {
					if (Ext.isEmpty(value)) {
						Ext.MessageBox.show(Ext.apply({}, {msg: cfg.msg}, cfg));
					} else {
						me.wait(WT.res('wait.changes'));
						me.mys.updateUsersEmailDomain(me.selectionIds(sel), value, {
							callback: function(success) {
								me.unwait();
								if (!success) WT.error(WT.res('error.unexpected'));
							}
						});
					}
				}
			}
		});
	},
	
	getSelectedUsers: function() {
		return this.lref('gp').getSelection();
	},
	
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
			case 'addImport':
				sel = me.getSelectedUsers();
				if (sel.length === 1) {
					return sel[0].get('exist');
				}
				return true;
			case 'remove':
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
				if (!me.authCapPasswordWrite) return true;
				sel = me.getSelectedUsers();
				if (sel.length === 1) {
					return !sel[0].get('exist');
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
	
	privates: {
		selectionIds: function(sel) {
			var ids = [];
			Ext.iterate(sel, function(rec) {
				ids.push(rec.getId());
			});
			return ids;
		},
		
		countByExist: function(recs) {
			var i = 0;
			Ext.iterate(recs, function(rec) {
				if (rec.get('exist')) i++;
			});
			return i;
		}
	}
});
