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
Ext.define('Sonicle.webtop.core.sdk.SQLQueryEditor', {
	extend: 'WTA.sdk.OkView',
	requires: [
		'Sonicle.plugin.FieldTooltip',
		'Sonicle.form.field.CodeEditor'
	],
	
	dockableConfig: {
		title: '{sqlQueryEditor.tit}',
		iconCls: 'wt-icon-sqlQueryEditor',
		width: 800,
		height: 600,
		minimizable: true,
		maximizable: true
	},
	promptConfirm: false,
	defaultButton: 'btnok',
	
	viewModel: {
		data: {
			data: {
				dataSourceId: null,
				pagination: false,
				pageNumber: 1,
				pageSize: 25,
				rawSql: null
			}
		}
	},
	
	/**
	 * @cfg {String} [text/x-sql|text/x-mysql|text/x-mariadb|text/x-cassandra|text/x-plsql|text/x-mssql|text/x-hive|text/x-pgsql|text/x-gql|text/x-gpsql|text/x-esper]
	 * The dialect mime-type to use for syntax highlighting
	 */
	
	/**
	 * @cfg {Boolean} [showPaginationOptions=true]
	 */
	showPaginationOptions: true,
	
	/**
	 * @cfg {String} [pageParam="page"]
	 * The name of the 'page' parameter to send in a request. Defaults to 'page'.
	 */
	pageParam: 'page',
	
	/**
	 * @cfg {String} [limitParam="limit"]
	 * The name of the 'limit' parameter to send in a request. Defaults to 'limit'.
	 */
	limitParam: 'limit',
	
	/**
	 * @cfg {String} runService
	 * Ajax serviceId definition for query execution.
	 */
	
	/**
	 * @cfg {String} runAction
	 * Ajax action definition for query execution.
	 */
	
	/**
	 * @cfg {Object} runParams
	 * Ajax extra-params definition for above action.
	 */
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			pagination: WTF.checkboxBind(null, 'data.pagination'),
			foCanRun: {
				bind: {bindTo: '{data}', deep: true},
				get: function(data) {
					return !Ext.isEmpty(data.dataSourceId) && !Ext.isEmpty(data.rawSql);
				}
			},
			foDSMissing: WTF.foIsEmpty(null, 'data.dataSourceId')
		});
	},
	
	initComponent: function() {
		var me = this,
				SoVMU = Sonicle.VMUtils,
				ic = me.getInitialConfig(),
				vm = me.getVM(),
				tbar = [];
		
		SoVMU.setInitialData(vm, ic.data, SoVMU.getDataNames(vm));
		//if (ic.data) vm.set('data', ic.data);
		
		tbar.push(
			{
				xtype: 'button',
				bind: {
					disabled: '{!foCanRun}'
				},
				iconCls: 'fas fa-play',
				text: me.res('sqlQueryEditor.btn-run.lbl'),
				handler: function() {
					me.runQueryUI();
				}
			},
			' '
		);
		if (me.showPaginationOptions) {
			tbar.push(
				'->',
				{
					xtype: 'checkbox',
					bind: '{data.pagination}',
					plugins: [
						{
							ptype: 'sofieldtooltip',
							tooltipTarget: 'field+label'
						}
					],
					hideEmptyLabel: true,
					boxLabel: me.mys.res('sqlQueryEditor.fld-pagination.lbl'),
					tooltip: me.res('sqlQueryEditor.fld-pagination.tip')
				}/*, '-', {
					xtype: 'numberfield',
					bind: {
						value: '{data.pageNumber}',
						disabled: '{!data.pagination}'
					},
					plugins: ['sofieldtooltip'],
					hideTrigger: true,
					allowBlank: false,
					allowDecimals: false,
					validateBlank: true,
					minValue: 1,
					labelSeparator: '',
					labelWidth: 20,
					labelAlign: 'right',
					fieldLabel: WTF.headerWithGlyphIcon('fas fa-hashtag'),
					tooltip: me.res('sqlQueryEditor.fld-pageNumber.tip'),
					width: 80
				}, ' ', {
					xtype: 'numberfield',
					bind: {
						value: '{data.pageSize}',
						disabled: '{!data.pagination}'
					},
					plugins: ['sofieldtooltip'],
					hideTrigger: true,
					allowBlank: false,
					allowDecimals: false,
					validateBlank: true,
					minValue: 0,
					maxValue: 1000,
					labelSeparator: '',
					labelWidth: 20,
					labelAlign: 'right',
					fieldLabel: WTF.headerWithGlyphIcon('fas fa-ruler'),
					tooltip: me.res('sqlQueryEditor.fld-pageSize.tip'),
					width: 80
				}*/
			);
		}
		/*
		tbar.push(
			'->',
			{
				xtype: 'tbtext',
				bind: {
					hidden: '{!foDSMissing}'
				},
				html: Ext.String.htmlEncode(me.res('sqlQueryEditor.nodatasource.txt'))
			}
		);
		*/
		
		Ext.apply(me, {
			tbar: tbar,
			buttons: [
				{
					reference: 'btnok',
					text: WT.res('act-ok.lbl'),
					handler: function() {
						me.okView();
					}
				}, {
					text: WT.res('act-cancel.lbl'),
					handler: function() {
						me.closeView(false);
					}
				}
			]
		});
		me.callParent(arguments);
		me.add([
			{
				region: 'center',
				xtype: 'socodeditor',
				bind: '{data.rawSql}',
				editor: {
					mode: me.dialectMime || 'text/x-sql'
					//lineWrapping
				}
			}, {
				region: 'south',
				split: true,
				xtype: 'wttabpanel',
				reference: 'tabmain',
				items: [
					{
						xtype: 'textarea',
						itemId: 'messages',
						title: me.res('sqlQueryEditor.messages.tit'),
						editable: false,
						fieldStyle: 'font-family: Courier, Monaco, monospace;'
					}, {
						xtype: 'gridpanel',
						itemId: 'results',
						title: me.res('sqlQueryEditor.results.tit'),
						viewConfig: {
							deferEmptyText: false,
							emptyText: me.res('sqlQueryEditor.gpresults.emp')
						}
					}
				],
				height: '45%'
			}
		]);
	},
	
	doDestroy: function() {
		delete this.lastPlaceholdersValues;
		this.callParent();
	},
	
	runQueryUI: function() {
		var me = this,
			XA = Ext.Array,
			SoS = Sonicle.String,
			vm = me.getVM(),
			rawSql = vm.get('data.rawSql') || '',
			matches = SoS.regexpExecAll(rawSql, /\{\{([A-Z][A-Z0-9_]*)\}\}/g, 1),
			pNames = XA.unique(XA.filter(matches, function(s) { return !SoS.isIn(s, ['CURRENT_DOMAIN_ID']); }));
		
		if (Ext.isEmpty(pNames)) {
			me.doRunQuery(rawSql, null);
		} else {
			var lastValues = me.lastPlaceholdersValues || {},
				sqlPlaceholders = {},
				promptAndRun = function(names, index) {
					index = index || 0;
					WT.prompt(names[index], {
						title: me.res('sqlQueryEditor.prompt.placeholder.tit'),
						value: lastValues[names[index]] || '',
						fn: function(bid, value) {
							if (bid !== 'cancel') {
								sqlPlaceholders[names[index]] = (value || '');
								if (index+1 < names.length) {
									promptAndRun(names, index+1);
								} else {
									me.doRunQuery(rawSql, sqlPlaceholders);
								}
							}
						}
					});
				};
			promptAndRun(pNames);
		}
	},
	
	privates: {
		doRunQuery: function(source, placeholders) {
			var me = this,
				vm = me.getVM(),
				params = Ext.clone(me.runParams) || {};
			
			if (placeholders) me.lastPlaceholdersValues = placeholders;
			if (vm.get('data.pagination') === true) {
				params['pagination'] = true;
				params[me.pageParam] = vm.get('data.pageNumber');
				params[me.limitParam] = vm.get('data.pageSize');
			}
			
			me.wait();
			WT.ajaxReq(me.runService, me.runAction, {
				params: Ext.apply(params, {
					dataSourceId: vm.get('data.dataSourceId'),
					debugInfo: true
				}),
				jsonData: {
					source: source,
					placeholders: placeholders
				},
				callback: function(success, json) {
					me.unwait();
					WT.handleError(success, json);
					if (success && json) {
						var tab = me.lref('tabmain'),
							msgCmp = tab.getComponent('messages'),
							resCmp = tab.getComponent('results'),
							data = json.data;

						msgCmp.setValue(data.message);
						if (data.success) {
							tab.setActiveItem(resCmp);
							me.reconfigureResults(resCmp, data.resultSet.columns, data.resultSet.rows);
						} else {
							tab.setActiveItem(msgCmp);
						}
					}
				}
			});
		},
		
		reconfigureResults: function(grid, columns, rows) {
			var gcols = [{xtype: 'rownumberer'}],
				i, fname;
			if (Ext.isArray(columns)) {
				for (var i=0; i < columns.length; i++) {
					fname = columns[i];
					gcols.push({text: fname, dataIndex: fname, draggable: false, sortable: false, groupable: false, minWidth: 100, flex: 1});
				}
			}
			grid.reconfigure({
				type: 'array',
				fields: columns,
				data: rows
			}, gcols);
		},
		
		createViewOkData: function(vm) {
			return Ext.merge(this.callParent(arguments), {
				pagination: vm.get('data.pagination'),
				rawSql: vm.get('data.rawSql')
			});
		}
	}
});
