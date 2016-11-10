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
Ext.define('Sonicle.webtop.core.admin.view.DomainRoles', {
	extend: 'WT.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.admin.model.GridDomainRole'
	],
	
	domainId: null,
	
	dockableConfig: {
		title: '{domainRoles.tit}',
		iconCls: 'wta-icon-roles-xs'
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
				model: 'Sonicle.webtop.core.admin.model.GridDomainRole',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainRoles', 'roles', {
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
				dataIndex: 'name',
				header: me.mys.res('domainRoles.gp.name.lbl'),
				flex: 1
			}, {
				dataIndex: 'description',
				header: me.mys.res('domainRoles.gp.description.lbl'),
				flex: 2
			}, {
				dataIndex: 'roleUid',
				header: me.mys.res('domainRoles.gp.roleUid.lbl'),
				tdCls: 'x-selectable',
				hidden: true,
				flex: 2
			}],
			tbar: [
				me.addAction('add', {
					text: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						me.addRoleUI(me.domainId);
					}
				}),
				me.addAction('remove', {
					text: WT.res('act-remove.lbl'),
					iconCls: 'wt-icon-remove-xs',
					disabled: true,
					handler: function() {
						var rec = me.lref('gp').getSelection()[0];
						if(rec) me.deleteRoleUI(rec);
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
					me.editRoleUI(rec);
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function(sel) {
			me.getAction('remove').setDisabled((sel) ? false : true);
		});
	},
	
	addRoleUI: function(domainId) {
		var me = this;
		me.mys.addRole(domainId, {
			callback: function(success) {
				if(success) {
					me.lref('gp').getStore().load();
				}
			}
		});
	},
	
	editRoleUI: function(rec) {
		var me = this,
				roleUid = rec.get('roleUid');
		me.mys.editRole(roleUid, {
			callback: function(success) {
				if(success) {
					me.lref('gp').getStore().load();
				}
			}
		});
	},
	
	deleteRoleUI: function(rec) {
		var me = this,
				sto = me.lref('gp').getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				sto.remove(rec);
			}
		}, me);
	}
});
