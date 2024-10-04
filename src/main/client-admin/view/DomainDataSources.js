/* 
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.DomainDataSources', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.String',
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.webtop.core.admin.model.DataSourceTypeLkp',
		'Sonicle.webtop.core.admin.model.GridDomainDataSource'
	],
	uses: [
		'Sonicle.picker.List',
		'Sonicle.webtop.core.admin.view.DataSource',
		'Sonicle.webtop.core.admin.view.DataSourceQuery'
	],
	
	dockableConfig: {
		title: '{domainDataSources.tit}',
		iconCls: 'wtadm-icon-dataSources'
	},
	actionsResPrefix: 'domainDataSources',
	
	/**
	 * @cfg {String} domainId
	 * Target domain ID.
	 */
	domainId: null,
	
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
		
		me.dsTypesLkpStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: me.mys.preNs('model.DataSourceTypeLkp'),
			proxy: WTF.proxy(me.mys.ID, 'LookupDataSourceTypes', null, {
				extraParams: {
					domainId: me.domainId
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			border: false,
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.core.admin.model.GridDomainDataSource',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainDataSources', null, {
					extraParams: {
						domainId: me.domainId
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
					dataIndex: 'name',
					header: me.res('domainDataSources.gp.name.lbl'),
					flex: 1
				}, {
					xtype: 'solookupcolumn',
					dataIndex: 'type',
					header: me.res('domainDataSources.gp.type.lbl'),
					store: {
						autoLoad: true,
						model: me.mys.preNs('model.DataSourceTypeLkp'),
						proxy: WTF.proxy(me.mys.ID, 'LookupDataSourceTypes', null, {
							extraParams: {
								domainId: me.domainId
							}
						})
					},
					displayField: 'desc',
					tooltipField: 'id',
					width: 150
				}, {
					dataIndex: 'server',
					header: me.res('domainDataSources.gp.server.lbl'),
					maxWidth: 150,
					flex: 1
				}, {
					dataIndex: 'username',
					header: me.res('domainDataSources.gp.username.lbl'),
					maxWidth: 200,
					flex: 1
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'poolStatus',
					header: me.res('domainDataSources.gp.poolState.lbl'),
					getIconCls: function(v) {
						return 'wt-icon-status-' + (v === 'up' ? v + '-blue' : v + '-gray');
					},
					getTip: function(v, rec) {
						var state = me.res('domainDataSources.gp.poolState.' + v);
						return me.res('domainDataSources.gp.poolState.tip', state, rec.getPoolActive(), rec.getPoolSize());
					},
					getText: function(v, rec) {
						return rec.getPoolActive() + ' / ' + rec.getPoolSize();
					},
					hideText: false,
					iconSize: 16,
				    width: 100
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'far fa-plus-square',
							tooltip: me.res('domainDataSources.act-addDataSourceQuery.tip'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.addQueryUI(rec);
							}
						}, {
							iconCls: 'fas fa-bolt',
							tooltip: me.res('domainDataSources.act-testConnection.tip'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.testDataSourceUI(rec);
							}
						}, {
							iconCls: 'fas fa-edit',
							tooltip: WT.res('act-edit.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.editDataSourceUI(rec);
							}
						}, {
							iconCls: 'fas fa-trash',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteDataSourceUI(rec);
							}
						}
					]
				}
			],
			plugins: [
				{
					ptype: 'rowwidget',
					widget: {
						xtype: 'grid',
						autoLoad: true,
						title: me.res('domainDataSources.gp.queries.tit'),
						bind: {
							store: '{record.queries}'
							//title: 'Queries for {record.name}'
						},
						viewConfig: {
							emptyText: me.res('domainDataSources.gp.queries.emp')
						},
						columns: [
							{
								dataIndex: 'name',
								flex: 1
							}, {
								dataIndex: 'description',
								renderer : function(v, meta, rec) {
									meta.tdCls = 'wt-theme-text-color-off';
									return v;
								},
								flex: 2
							}, {
								xtype: 'soactioncolumn',
								items: [
									{
										iconCls: 'wt-glyph-edit',
										tooltip: WT.res('act-edit.lbl'),
										handler: function(view, ridx, cidx, itm, e, rec) {
											me.editQueryUI(rec);
										}
									}, {
										iconCls: 'wt-glyph-delete',
										tooltip: WT.res('act-remove.lbl'),
										handler: function(view, ridx, cidx, itm, e, rec) {
											me.deleteQueryUI(rec);
										}
									}
								]
							}
						],
						listeners: {
							rowdblclick: function(s, rec) {
								me.editQueryUI(rec);
							}
						}
					}
				}
			],
			tbar: [
				me.addAct('addDataSource', {
					tooltip: null,
					iconCls: 'wt-icon-add',
					ui: '{tertiary|toolbar}',
					handler: function() {
						me.addDataSourceUI();
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
					me.editDataSourceUI(rec);
				}
			}
		});
	},
	
	addDataSource: function(domainId, opts) {
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.DataSource', {
				swapReturn: true,
				viewCfg: {
					domainId: domainId
				}
			});

		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {}
			});
		});
	},
	
	editDataSource: function(domainId, dataSourceId, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.DataSource', {
				swapReturn: true,
				preventDuplicates: true,
				tagSuffix: dataSourceId,
				viewCfg: {
					domainId: domainId
				}
			});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: dataSourceId
				}
			});
		});
	},
	
	deleteDataSource: function(domainId, dataSourceId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainDataSource', {
			params: {
				crud: 'delete',
				domainId: domainId,
				id: dataSourceId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	testDataSource: function(domainId, dataSourceId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainDataSource', {
			params: {
				crud: 'test',
				domainId: domainId,
				id: dataSourceId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	addDataSourceQuery: function(domainId, dataSourceId, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.DataSourceQuery', {
				swapReturn: true,
				viewCfg: {
					domainId: domainId,
					dataSourceName: opts.dataSourceName
				}
			});

		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {
					dataSourceId: dataSourceId
				}
			});
		});
	},
	
	editDataSourceQuery: function(domainId, queryId, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.DataSourceQuery', {
				swapReturn: true,
				preventDuplicates: true,
				tagSuffix: queryId,
				viewCfg: {
					domainId: domainId
				}
			});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: queryId
				}
			});
		});
	},
	
	deleteDataSourceQuery: function(domainId, queryId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainDataSourceQuery', {
			params: {
				crud: 'delete',
				domainId: domainId,
				id: queryId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	privates: {
		addDataSourceUI: function() {
			var me = this;
			me.addDataSource(me.domainId, {
				callback: function(success) {
					if (success) me.lref('gp').getStore().load();
				}
			});
		},
		
		editDataSourceUI: function(rec) {
			var me = this;
			me.editDataSource(me.domainId, rec.get('id'), {
				callback: function(success) {
					if (success) me.lref('gp').getStore().load();
				}
			});
		},
		
		deleteDataSourceUI: function(rec) {
			var me = this,
				key = rec.areQueriesInUse() ? 'dataSource.confirm.delete.inuse' : 'dataSource.confirm.delete';
			WT.confirmDelete(me.res(key, rec.get('name')), function(bid) {
				if (bid === 'ok') {
					me.lref('gp').getStore().remove(rec);
				}
			}, me);
		},
		
		testDataSourceUI: function(rec) {
			var me = this;
			me.wait();
			me.testDataSource(me.domainId, rec.get('id'), {
				callback: function(success, json) {
					me.unwait();
					if (success) {
						WT.info(me.res('dataSource.info.check'));
						if (rec.get('poolStatus') !== 'up') me.lref('gp').getStore().load();
					} else {
						WT.error(me.res('dataSource.error.check', json.message));
					}
				}
			});	
		},
		
		addQueryUI: function(rec) {
			var me = this,
				gp = me.lref('gp');

			me.addDataSourceQuery(me.domainId, rec.getId(), {
				//TODO: find rowwidget parent and get data-source name
				dataSourceName: null,
				callback: function(success, model) {
					if (success) gp.getStore().load();
				}
			});
		},
		
		editQueryUI: function(rec) {
			var me = this,
				gp = me.lref('gp');
			
			me.editDataSourceQuery(me.domainId, rec.getId(), {
				callback: function(success, model) {
					if (success) gp.getStore().load();
				}
			});
		},
		
		deleteQueryUI: function(rec) {
			var me = this,
				key = rec.isInUse() ? 'dataSourceQuery.confirm.delete.inuse' : 'dataSourceQuery.confirm.delete';
			WT.confirmDelete(me.res(key, rec.get('name')), function(bid) {
				if (bid === 'ok') {
					me.wait();
					me.deleteDataSourceQuery(me.domainId, rec.getId(), {
						callback: function(success, data, json) {
							me.unwait();
							if (success) rec.store.remove(rec);
							WT.handleError(success, json);
						}
					});
				}
			}, me);	
		},
		
		getRowIndexByRecord: function(grid, record) {
			var view = grid.getView(),
					node = view.getNodeByRecord(record);
			return view.indexOf(node);
		}
	}
});
