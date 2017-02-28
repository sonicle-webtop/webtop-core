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
Ext.define('Sonicle.webtop.core.admin.view.DomainGroups', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.admin.model.GridDomainGroup'
	],
	
	domainId: null,
	passwordPolicy: false,
	authCapPasswordWrite: false,
	authCapUsersWrite: false,
	
	dockableConfig: {
		title: '{domainGroups.tit}',
		iconCls: 'wtadm-icon-groups-xs'
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
				model: 'Sonicle.webtop.core.admin.model.GridDomainGroup',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainGroups', 'groups', {
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
			columns: [{
				xtype: 'rownumberer'	
			}, {
				dataIndex: 'groupId',
				header: me.mys.res('domainGroups.gp.groupId.lbl'),
				flex: 1
			}, {
				dataIndex: 'displayName',
				header: me.mys.res('domainGroups.gp.displayName.lbl'),
				flex: 2
			}],
			tbar: [
				me.addAction('add', {
					text: WT.res('act-add.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						me.addGroupUI(null);
					}
				}),
				me.addAction('remove', {
					text: WT.res('act-remove.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-remove-xs',
					disabled: true,
					handler: function() {
						var rec = me.getSelectedGroup();
						if(rec) me.deleteGroupUI(rec);
					}
				}),
				'->',
				me.addAction('refresh', {
					text: null,
					tooltip: WT.res('act-refresh.lbl'),
					iconCls: 'wt-icon-refresh-xs',
					handler: function() {
						me.lref('gp').getStore().load();
					}
				})
			],
			listeners: {
				rowdblclick: function(s, rec) {
					me.editGroupUI(rec);
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function() {
			me.updateDisabled('remove');
		});
	},
	
	addGroupUI: function(rec) {
		var me = this;
		me.mys.addGroup(me.domainId, {
			callback: function(success) {
				if(success) {
					me.lref('gp').getStore().load();
				}
			}
		});
	},
	
	editGroupUI: function(rec) {
		var me = this,
				pid = rec.get('profileId');
		me.mys.editGroup(pid, {
			callback: function(success) {
				if(success) {
					me.lref('gp').getStore().load();
				}
			}
		});
	},
	
	deleteGroupUI: function(rec) {
		var me = this;
		
		WT.confirm(me.mys.res('domainGroups.confirm.delete'), function(bid) {
			if(bid === 'yes') {
				me.mys.deleteGroups([rec.get('profileId')], {
					callback: function(success) {
						if(success) {
							me.lref('gp').getStore().remove(rec);
						}
					}
				});
			}
		}, me);	
	},
	
	getSelectedGroup: function() {
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
			case 'remove':
				sel = me.getSelectedGroup();
				if(sel) {
					return false;
				} else {
					return true;
				}
		}
	}
});
