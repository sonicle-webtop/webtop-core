/* 
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.view.CustomPanels', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.plugin.DDOrdering',
		'WTA.ux.grid.column.Action',
		'Sonicle.webtop.core.model.CustomPanelGrid'
	],
	uses: [
		'Sonicle.DataUtils',
		'Sonicle.webtop.core.view.CustomPanel'
	],
	
	/**
	 * @cfg {String} serviceId
	 * Target service ID for which managing fields.
	 */
	serviceId: null,
	
	/**
	 * @cfg {String} serviceName
	 * Target service display name for displaying in title.
	 */
	serviceName: null,
	
	dockableConfig: {
		title: '{customPanels.tit}',
		iconCls: 'wt-icon-customPanel',
		width: 600,
		height: 400
		//modal: true
	},
	promptConfirm: false,
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.serviceId) {
			Ext.raise('serviceId is mandatory');
		}
		Ext.merge(cfg, {
			dockableConfig: {
				title: '[' + Sonicle.String.deflt(cfg.serviceName, cfg.serviceId) + '] ' + WT.res('customPanels.tit')
			}
		});
		me.callParent([cfg]);
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
				autoSync: true,
				model: 'Sonicle.webtop.core.model.CustomPanelGrid',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageCustomPanels', 'data', {
					extraParams: {
						targetServiceId: me.serviceId
					}
				})
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: WT.res('grid.emp'),
				plugins: [{
					ptype: 'sogridviewddordering',
					orderField: 'order'
				}]
			},
			columns: {
				defaults: {
					sortable: false
				},
				items: [
					{
						xtype: 'rownumberer'
					}, {
						dataIndex: 'name',
						header: me.res('customPanels.gp.name.lbl'),
						renderer: function(val, meta, rec) {
							var desc = rec.get('description'),
							        tip = desc ? Ext.String.htmlEncode(desc) : '';
						    meta.tdAttr = 'data-qtip="' + tip + '"';
						    return val;
						},
						flex: 1
					}, {
						xtype: 'sotagcolumn',
						dataIndex: 'tags',
						header: me.res('customPanels.gp.tags.lbl'),
						tagsStore: WT.getTagsStore(),
						emptyText: WT.res(me.serviceId, 'customPanels.gp.tags.emp'),
						emptyCls: 'wt-theme-text-greyed',
						flex: 1
					}, {
						dataIndex: 'fieldsCount',
						header: me.res('customPanels.gp.has.lbl'),
						renderer: function(val, meta, rec) {
							var key = 'customPanels.gp.has.';
							return me.res(key + (val === 1 ? 'field' : 'fields'), val);
						},
						align: 'left',
						width: 80
					}, {
						xtype: 'soactioncolumn',
						items: [
							{
								iconCls: 'fa fa-clone',
								tooltip: WT.res('act-clone.lbl'),
								handler: function(g, ridx) {
									var rec = g.getStore().getAt(ridx);
									me.cloneCustomPanelUI(rec);
								}
							}, {
								iconCls: 'fa fa-trash',
								tooltip: WT.res('act-remove.lbl'),
								handler: function(g, ridx) {
									var rec = g.getStore().getAt(ridx);
									me.deleteCustomPanelUI(rec);
								}
							}
						]
					}
				]		
			},
			tbar: [
				me.addAct('add', {
					text: WT.res('act-add.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						me.addCustomPanelUI();
					}
				}),
				'->',
				me.addAct('refresh', {
					text: '',
					tooltip: WT.res('act-refresh.lbl'),
					iconCls: 'wt-icon-refresh',
					handler: function() {
						me.lref('gp').getStore().load();
					}
				})
			],
			listeners: {
				rowdblclick: function(s, rec) {
					me.editCustomPanelUI(rec);
				}
			}
		});
	},
	
	addCustomPanel: function(serviceId, data, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.mys.ID, 'view.CustomPanel', {
					swapReturn: true,
					viewCfg: {
						serviceId: me.serviceId,
						serviceName: me.serviceName
					}
				});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function(s) {
			vw.begin('new', {
				data: Ext.apply(data || {}, {
					serviceId: serviceId
				}, {
					order: -1
				})
			});
		});
	},
	
	editCustomPanel: function(cid, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.mys.ID, 'view.CustomPanel', {
					swapReturn: true,
					viewCfg: {
						serviceId: me.serviceId,
						serviceName: me.serviceName
					}
				});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function(s) {
			vw.begin('edit', {
				data: {
					id: cid
				}
			});
		});
	},
	
	privates: {
		addCustomPanelUI: function() {
			var me = this;
			me.addCustomPanel(me.serviceId, null, {
				callback: function() {
					me.lref('gp').getStore().load();
				}
			});
		},

		editCustomPanelUI: function(rec) {
			var me = this;
			me.editCustomPanel(rec.getId(), {
				callback: function() {
					me.lref('gp').getStore().load();
				}
			});
		},
		
		cloneCustomPanelUI: function(rec) {
			var me = this;
			WT.ajaxReq(me.mys.ID, 'ManageCustomPanel', {
				params: {
					crud: 'read',
					id: rec.getId()
				},
				callback: function(success, json) {
					if (success) {
						var data = Ext.apply(json.data, {
							id: undefined,
							fieldId: undefined,
							name: Sonicle.DataUtils.getDuplValue(me.lref('gp').getStore(), 'name', json.data.name)
						});
						me.addCustomPanel(me.serviceId,  Sonicle.Utils.applyIfDefined({}, data), {
							callback: function() {
								me.lref('gp').getStore().load();
							}
						});
					}
				}
			});
		},

		deleteCustomPanelUI: function(rec) {
			var me = this,
					grid = me.lref('gp'),
					sto = grid.getStore();

			WT.confirm(me.res('customPanels.confirm.delete'), function(bid) {
				if (bid === 'yes') sto.remove(rec);
			}, me);
		}
	}
});
