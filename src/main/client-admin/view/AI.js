/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.AI', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.admin.model.GridAI'
	],
	uses: [
		'Sonicle.webtop.core.admin.view.AIReport'
	],
	
	dockableConfig: {
		title: '{ai.tit}',
		iconCls: 'wt-icon-ai'
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		var title = me.res('ai.configuration.tit');
		if (!me.hasAI) title += " - <span style='color:red'>"+me.res('ai.configuration.inactive.tit')+"</span>";
			
		me.add({
			region: 'north',
			xtype: 'wtform',
			title: title,
			layout: { type: 'table', columns: 2, tableAttrs: { style: 'width: 500px' } },
			height: 200,
			fieldDefaults: {
				labelAlign: 'right',
				labelWidth: 150,
			},
			items: [
				WTF.lookupCombo('id', 'desc', {
						reference: 'cbprovider',
						fieldLabel: me.res('ai.provider.lbl'),
						width: 300,
						value: me.provider,
						colspan: 1,
						store: {
							autoLoad: true,
							fields: ['id', 'desc'],
							data: [
								['',    me.res('ai.provider.none')],
								['openai',   me.res('ai.provider.openai')],
								['claude',  me.res('ai.provider.claude')]
							]
						}
				}),/*{
					xtype: 'label',
					text: ' ',
					width: 24
				},*/ {
					xtype: 'textfield',
					fieldLabel: me.res('ai.model.lbl'),
					reference: 'txtmodel',
					width: 400,
					colspan: 1,
					emptyText: '(default)',
					value: me.model
				}, {
					xtype: 'textfield',
					fieldLabel: me.res('ai.apikey.lbl'),
					reference: 'txtapikey',
					width: 400,
					colspan: 2,
					value: me.apikey
				},/*{
					xtype: 'label',
					text: ' ',
					width: 24
				},*/ {
					xtype: 'numberfield',
					reference: 'nmbquota',
					fieldLabel: WT.res('opts.ai.fld-max-tokens.lbl'),
					emptyText: WT.res('opts.ai.fld-max-tokens.emp'),
					minValue: 0,
					width: 300,
					value: me.quota,
					hideTrigger: false,
					keyNavEnabled: false,
					mouseWheelEnabled: false,
					colspan: 2
				},/*{
					xtype: 'label',
					text: ' ',
					colspan: 1
				},*/ {
					xtype: 'button',
					text: WT.res('act-save.lbl'),
					style: 'margin-left: 154px',
					handler: function() {
						me.saveConfiguration();
					}
				}
			]
		}, 
		{
			xtype: 'wtpanel',
			region: 'center',
			layout: 'border',
			title: 'Report',
			items: [
				{
					region: 'center',
					xtype: 'grid',
					reference: 'gp',
					border: false,
					features: [{
						ftype: 'summary'
						// dock: 'bottom'  // optional, pins the row to the bottom even when scrolling
					}],
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.core.admin.model.GridAI',
						proxy: WTF.proxy(me.mys.ID, 'ManageDomainAI', null, {
							extraParams: {
								domainId: me.domainId,
								crud: 'read',
								view: 'today'
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
							dataIndex: 'userName',
							header: me.res('ai.gp.userName.lbl'),
							flex: 1
						}, {
							dataIndex: 'displayName',
							header: me.res('ai.gp.displayName.lbl'),
							flex: 1
						}, {
							dataIndex: 'promptTokens',
							header: me.res('ai.gp.promptTokens.lbl'),
							align: 'right',
							xtype: 'numbercolumn',
							format: '0,000',
							summaryType: 'sum',
							summaryRenderer: function(value) {
								return '<b>' + Ext.util.Format.number(value, '0,000') + '</b>';
							},
							flex: 1
						}, {
							dataIndex: 'completionTokens',
							header: me.res('ai.gp.completionTokens.lbl'),
							align: 'right',
							xtype: 'numbercolumn',
							format: '0,000',
							summaryType: 'sum',
							summaryRenderer: function(value) {
								return '<b>' + Ext.util.Format.number(value, '0,000') + '</b>';
							},
							flex: 1
						}, {
							dataIndex: 'totalTokens',
							header: me.res('ai.gp.totalTokens.lbl'),
							align: 'right',
							xtype: 'numbercolumn',
							format: '0,000',
							summaryType: 'sum',
							summaryRenderer: function(value) {
								return '<b>' + Ext.util.Format.number(value, '0,000') + '</b>';
							},
							flex: 1
						}
					],
					tbar: [
						WTF.lookupCombo('id', 'desc', {
							reference: 'cbview',
							store: {
								autoLoad: true,
								fields: ['id', 'desc'],
								data: [
									['today', me.mys.res('ai.today')],
									['thisweek', me.mys.res('ai.thisweek')],
									['lastweek', me.mys.res('ai.lastweek')],
									['thismonth', me.mys.res('ai.thismonth')],
									['lastmonth', me.mys.res('ai.lastmonth')],
								]
							},
							value: 'today',
							fieldLabel: me.mys.res('ai.view.lbl'),
							labelAlign: 'right',
							width: 100+140,
							listeners: {
								select: function(s, rec) {
									Sonicle.Data.loadWithExtraParams(
										me.lref('gp').getStore(),
										{ view: rec.get('id') }
									);
								}
							}
						}),
						'-',
						me.addAct('refresh', {
							text: null,
							tooltip: WT.res('act-refresh.lbl'),
							iconCls: 'wt-icon-refresh',
							handler: function() {
								me.lref('gp').getStore().load();
							}
						}),
						me.addAct('share', {
							text: null,
							tooltip: WT.res('act-share.lbl'),
							iconCls: 'wt-icon-share',
							handler: function() {
								me.shareUI();
							}
						}),
						'->',
						me.addAct('report', {
							text: null,
							tooltip: WT.res('act-generate.lbl'),
							iconCls: 'wt-icon-generate',
							handler: function() {
								me.addReportUI();
							}
						})

					]
				}				
			]
		});
	},
	
	shareUI: function() {
		this.addReportUI(true);
	},
	
	addReportUI: function(current) {
		var me = this;
		me.wait();
		WT.ajaxReq(me.mys.ID, 'ManageAIReport', {
			params: {
				crud: 'read',
				domainId: me.domainId
			},
			callback: function(success, json) {
				me.unwait();
				if (!success) {
					WT.error(json.message);
					return;
				}
				me.openReportUI(json.data || {}, current);
			}
		});
	},

	openReportUI: function(data, current) {
		var me = this,
				vw = WT.createView(me.mys.ID, 'view.AIReport', {
					swapReturn: true,
					viewCfg: {
						dockableConfig: {
							iconCls: current ? 'wt-icon-share' : 'wt-icon-generate'
						},
						data: {
							cadence: data.cadence || 'none',
							email: data.email || '',
							current: current
						}
					}
				});

		vw.on('viewok', function(s, data) {
			if (!current) me.saveReport(data);
			else me.sendReport(data);
		});
		vw.on('viewtest', function(s, data) {
			me.testReport(data);
		});
		vw.showView();
	},

	saveReport: function(data) {
		var me = this;
		me.wait();
		WT.ajaxReq(me.mys.ID, 'ManageAIReport', {
			params: {
				crud: 'update',
				domainId: me.domainId,
				cadence: data.cadence,
				email: data.email
			},
			callback: function(success, json) {
				me.unwait();
				if (success) {
					WT.toast(me.mys.res('aiReport.info.saved'));
				} else {
					WT.error(json.message);
				}
			}
		});
	},

	testReport: function(data) {
		var me = this;
		me.wait();
		WT.ajaxReq(me.mys.ID, 'ManageAIReport', {
			params: {
				crud: 'test',
				domainId: me.domainId,
				email: data.email
			},
			callback: function(success, json) {
				me.unwait();
				if (success) {
					WT.info(me.mys.res('aiReport.info.tested'));
				} else {
					WT.error(json.message);
				}
			}
		});
	},
	
	sendReport: function(data) {
		var me = this;
		me.wait();
		WT.ajaxReq(me.mys.ID, 'ManageDomainAI', {
			params: {
				crud: 'send',
				domainId: me.domainId,
				view: me.lref('cbview').getValue(),
				email: data.email
			},
			callback: function(success, json) {
				me.unwait();
				if (success) {
					WT.toast(me.mys.res('aiReport.info.sent'));
				} else {
					WT.error(json.message);
				}
			}
		});
	},
	
	saveConfiguration: function() {
		var me = this;
		me.wait();
		WT.ajaxReq(me.mys.ID, 'ManageDomainAIConfiguration', {
			params: {
				crud: 'update',
				domainId: me.domainId,
				provider: me.lref('cbprovider').getValue(),
				model: me.lref('txtmodel').getValue(),
				apikey: me.lref('txtapikey').getValue(),
				quota: me.lref('nmbquota').getValue()
			},
			callback: function(success, json) {
				me.unwait();
				if (success) {
					WT.toast(me.mys.res('ai.configuration.saved'));
				} else {
					WT.error(json.message);
				}
			}
		});
	}
	
});
