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
Ext.define('Sonicle.webtop.core.admin.view.DomainGroups', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.admin.model.GridGroup'
	],
	uses: [
		'Sonicle.webtop.core.admin.view.Group'
	],
	
	/**
	 * @cfg {String} domainId
	 * The bound domain ID for this entity.
	 */
	domainId: null,
	
	dockableConfig: {
		title: '{domainGroups.tit}',
		iconCls: 'wtadm-icon-groups'
	},
	actionsResPrefix: 'domainGroups',
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
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
			border: false,
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.admin.model.GridGroup',
				proxy: WTF.proxy(me.mys.ID, 'ManageDomainGroups', null, {
					extraParams: {
						domainId: me.domainId,
						crud: 'read'
					},
					writer: {
						allowSingle: false // Always wraps records into an array
					}
				})
			},
			columns: [
				{
					xtype: 'rownumberer'
				}, {
					dataIndex: 'groupId',
					header: me.res('domainGroups.gp.groupId.lbl'),
					flex: 1
				}, {
					dataIndex: 'description',
					header: me.res('domainGroups.gp.description.lbl'),
					flex: 2
				}, {
					dataIndex: 'groupSid',
					header: me.res('domainGroups.gp.groupSid.lbl'),
					tdCls: 'x-selectable',
					hidden: true,
					flex: 1
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'wt-glyph-edit',
							tooltip: WT.res('act-edit.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.editGroupUI(rec);
							}
						}, {
							iconCls: 'wt-glyph-delete',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteGroupUI(rec);
							}
						}
					]
				}
			],
			tbar: [
				me.addAct('add', {
					tooltip: null,
					iconCls: 'wt-icon-add',
					handler: function() {
						me.addGroupUI();
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
					me.editGroupUI(rec);
				}
			}
		});
	},
	
	addGroup: function(domainId, opts) {
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.Group', {
				swapReturn: true,
				viewCfg: {
					domainId: domainId
				}
			});

		vw.on('viewsave', function(s, success, model, op) {
			Ext.callback(opts.callback, opts.scope || me, [success, model, op]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {}
			});
		});
	},
	
	editGroup: function(domainId, groupId, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.Group', {
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
					id: groupId
				}
			});
		});
	},
	
	deleteGroup: function(domainId, groupId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainGroup', {
			params: {
				crud: 'delete',
				domainId: domainId,
				id: groupId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	privates: {
		addGroupUI: function() {
			var me = this,
				gp = me.lref('gp');

			me.addGroup(me.domainId, {
				callback: function(success, model, op) {
					WT.handleMessage(success, op);
					if (success) gp.getStore().load();
				}
			});
		},

		editGroupUI: function(rec) {
			var me = this,
				gp = me.lref('gp');

			me.editGroup(me.domainId, rec.getId(), {
				callback: function(success, model, op) {
					WT.handleMessage(success, op);
					if (success) gp.getStore().load();
				}
			});
		},

		deleteGroupUI: function(rec) {
			var me = this;
			WT.confirmDelete(me.res('domainGroups.confirm.delete', rec.get('groupId')), function(bid) {
				if (bid === 'ok') {
					me.wait();
					me.deleteGroup(me.domainId, rec.getId(), {
						callback: function(success, data, json) {
							me.unwait();
							if (success) me.lref('gp').getStore().remove(rec);
							WT.handleError(success, json);
							WT.handleMessage(success, json);
						}
					});
				}
			}, me);
		}
	}
});
