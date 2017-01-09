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
	
	domainId: null,
	passwordPolicy: false,
	authCapPasswordWrite: false,
	authCapUsersWrite: false,
	
	dockableConfig: {
		title: '{domainUsers.tit}',
		iconCls: 'wta-icon-users-xs'
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
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			store: {
				autoLoad: true,
				autoSync: true,
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
					return rec.get('enabled') === false ? 'wta-gpusers-row-disabled' : '';
				}
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
				me.addAction('add', {
					text: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add-xs',
					menu: [
						me.addAction('addEmpty', {
							text: me.mys.res('domainUsers.act-addEmpty.lbl'),
							disabled: !me.authCapUsersWrite,
							handler: function() {
								me.addUserUI(null);
							}
						}),
						me.addAction('addImport', {
							text: me.mys.res('domainUsers.act-addImport.lbl'),
							disabled: true,
							handler: function() {
								var rec = me.getSelectedUser();
								if(rec) me.addUserUI(rec);
							}
						})
					]
				}),
				me.addAction('remove', {
					text: WT.res('act-remove.lbl'),
					iconCls: 'wt-icon-remove-xs',
					disabled: true,
					menu: [
						me.addAction('removeClean', {
							text: me.mys.res('domainUsers.act-removeClean.lbl'),
							handler: function() {
								var rec = me.getSelectedUser();
								if(rec) me.deleteUserUI(false, rec);
							}
						}),
						me.addAction('removeDeep', {
							text: me.mys.res('domainUsers.act-removeDeep.lbl'),
							handler: function() {
								var rec = me.getSelectedUser();
								if(rec) me.deleteUserUI(true, rec);
							}
						})
					]
				}),
				me.addAction('changePassword', {
					text: WT.res('act-changePassword.lbl'),
					iconCls: 'wt-icon-changePassword-xs',
					disabled: true,
					handler: function() {
						var rec = me.getSelectedUser();
						if(rec) me.changePasswordUI(rec);
					}
				}),
				'-',
				me.addAction('enable', {
					text: WT.res('act-enable.lbl'),
					iconCls: 'wt-icon-item-enable-xs',
					disabled: true,
					handler: function() {
						var rec = me.getSelectedUser();
						if(rec) me.updateUserStatusUI(rec, true);
					}
				}),
				me.addAction('disable', {
					text: WT.res('act-disable.lbl'),
					iconCls: 'wt-icon-item-disable-xs',
					disabled: true,
					handler: function() {
						var rec = me.getSelectedUser();
						if(rec) me.updateUserStatusUI(rec, false);
					}
				}),
				'->',
				me.addAction('refresh', {
					text: '',
					tooltip: WT.res('act-refresh.lbl'),
					iconCls: 'wt-icon-refresh-xs',
					handler: function() {
						me.lref('gp').getStore().load();
					}
				})
			],
			listeners: {
				rowdblclick: function(s, rec) {
					if(rec.get('exist') === true) {
						me.editUserUI(rec);
					} else {
						me.addUserUI(rec);
					}
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function() {
			me.updateDisabled('addImport');
			me.updateDisabled('remove');
			me.updateDisabled('changePassword');
			me.updateDisabled('enable');
			me.updateDisabled('disable');
		});
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
					if(success) {
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
				if(bid === 'yes') doFn();
			}, me);
		}
	},
	
	getSelectedUser: function() {
		var sel = this.lref('gp').getSelection();
		return sel.length === 1 ? sel[0] : null;
	},
	
	updateDisabled: function(action) {
		var me = this,
				dis = me.isDisabled(action);
		me.setActionDisabled(action, dis);
	},
	
	/**
	 * @private
	 */
	isDisabled: function(action) {
		var me = this, sel;
		switch(action) {
			case 'addImport':
				sel = me.getSelectedUser();
				if(sel) {
					return sel.get('exist');
				} else {
					return true;
				}
			case 'remove':
				sel = me.getSelectedUser();
				if(sel) {
					return !sel.get('exist');
				} else {
					return true;
				}
			case 'changePassword':
				if(!me.authCapPasswordWrite) return true;
				sel = me.getSelectedUser();
				if(sel) {
					return !sel.get('exist');
				} else {
					return true;
				}
			case 'enable':
				sel = me.getSelectedUser();
				if(sel) {
					return !sel.get('exist') || sel.get('enabled') === true;
				} else {
					return true;
				}
			case 'disable':
				sel = me.getSelectedUser();
				if(sel) {
					return !sel.get('exist') || !(sel.get('enabled') === true);
				} else {
					return true;
				}
		}
	}
});
