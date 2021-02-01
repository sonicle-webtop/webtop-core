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
Ext.define('Sonicle.webtop.core.admin.view.DomainAccessLog', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.data.BufferedStore',
		'Sonicle.webtop.core.admin.model.GridDomainAccessLog',
		'WTA.ux.field.Search'
	],
	
	domainId: null,
	
	dockableConfig: {
		title: '{domainAccessLog.tit}',
		iconCls: 'wtadm-icon-accesslog'
	},
	
	constructor: function(cfg) {
		var me = this;
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
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			store: {
				type: 'buffered',
				autoLoad: true,
				model: 'Sonicle.webtop.core.admin.model.GridDomainAccessLog',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainAccessLog', null, {
					extraParams: {
						domainId: me.domainId
					},
					writer: {
						allowSingle: false // Always wraps records into an array
					}
				}),
				sorters: [{
					property: 'date',
					direction: 'DESC'
				}],
				pageSize: 50,
				leadingBufferZone: 50,
				trailingBufferZone: 50
			},
			viewConfig: {
				enableTextSelection: true
			},
			columns: [
				{
					xtype: 'rownumberer'
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'failure',
					getIconCls: function(v, rec) {
						var sta = rec.getStatus();
						return sta === 'ok' ? 'wt-icon-ok' : (sta === 'warn' ? 'wt-icon-warn' : 'wt-icon-warn-red');
					},
					getTip: function(v, rec) {
						var sta = rec.getStatus();
						return me.mys.res('domainAccessLog.gp.status.'+sta+'.tip');
					},
					iconSize: WTU.imgSizeToPx('xs'),
					header: WTF.headerWithGlyphIcon('fa fa-check-square-o'),
					width: 40
				}, {
					dataIndex: 'sessionId',
					sortable: true,
					groupable: false,
					header: me.res('domainAccessLog.gp.sessionId.lbl'),
					flex: 3
				}, {
					dataIndex: 'userId',
					sortable: true,
					groupable: false,
					align: 'center',
					renderer: function(value) {
						return '?' === value ? '-' : value;
					},
					header: me.res('domainAccessLog.gp.userId.lbl'),
					flex: 2
				}, {
					xtype: 'datecolumn',
					dataIndex: 'date',
					format: WT.getShortDateFmt() + ' ' +  WT.getLongTimeFmt(),
					sortable: true,
					groupable: false,
					align: 'center',
					header: me.res('domainAccessLog.gp.date.lbl'),
					flex: 2
				}, {
					dataIndex: 'minutes',
					emptyCellText: '-',
					sortable: true,
					groupable: false,
					align: 'center',
					header: me.res('domainAccessLog.gp.minutes.lbl'),
					flex: 2
				}, {
					xtype: 'booleancolumn',
					dataIndex: 'authenticated',
					trueText: WT.res('word.yes'),
					falseText: WT.res('word.no'),
					align: 'center',
					sortable: true,
					groupable: false,
					header: me.res('domainAccessLog.gp.authenticated.lbl'),
					flex: 2
				}, {
					align: 'center',
					dataIndex: 'loginErrors',
					sortable: true,
					groupable: false,
					header: me.res('domainAccessLog.gp.loginErrors.lbl'),
					flex: 2
				}
			],
			plugins: [
				{
					ptype: 'rowwidget',
					widget: {
						xtype: 'grid',
						title: me.mys.res('domainAccessLog.gp.details.tit'),
						bind: {
							store: {
								autoLoad: true,
								model: 'Sonicle.webtop.core.admin.model.DomainAccessLogDetail',
								proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainAccessLogDetail', null, {
									extraParams: {
										sessionId: '{record.sessionId}',
										domainId: me.domainId,
										userId: '{record.userId}'
									},
									writer: {
										allowSingle: false // Always wraps records into an array
									}
								}),
								listeners: {
									beforeload: function() {
										me.lref('gp').setLoading(true);
									},
									load: function() {
										me.lref('gp').setLoading(false);
									}
								}
							}
						},
						columns: [
							{
								xtype: 'datecolumn',
								dataIndex: 'timestamp',
								sortable: false,
								groupable: false,
								align: 'center',
								format: WT.getShortDateFmt() + ' ' +  WT.getLongTimeFmt(),
								header: me.res('domainAccessLog.gp.details.date.lbl'),
								flex: 2
							}, {
								dataIndex: 'action',
								sortable: false,
								groupable: false,
								align: 'center',
								emptyCellText: '-',
								header: me.res('domainAccessLog.gp.details.action.lbl'),
								renderer: WTF.resColRenderer({
									id: me.mys.ID,
									key: 'domainAccessLog.gp.details.action',
									keepcase: true
								}),
								flex: 2
							}, {
								dataIndex: 'ipAddress',
								sortable: false,
								groupable: false,
								align: 'center',
								emptyCellText: '-',
								header: me.res('domainAccessLog.gp.details.ipAddress.lbl'),
								flex: 2
							}
						]
					}
				}
			],
			tbar: [
				'->',
				{
					xtype: 'wtsearchfield',
					reference: 'fldsearch',
					highlightKeywords: ['session', 'user'],
					fields: [
						{
							name: 'session',
							type: 'string',
							label: me.res('domainAccessLog.fldsearch.session.lbl')
						}, {
							name: 'user',
							type: 'string',
							label: me.res('domainAccessLog.fldsearch.user.lbl')
						}, {
							name: 'dateFrom',
							type: 'date',
							labelAlign: 'left',
							label: me.res('domainAccessLog.fldsearch.dateFrom.lbl')
						}, {
							name: 'dateTo',
							type: 'date',
							labelAlign: 'left',
							label: me.res('domainAccessLog.fldsearch.dateTo.lbl')
						}, {
							name: 'minDuration',
							type: 'number',
							label: me.res('domainAccessLog.fldsearch.minDuration.lbl')
						}, {
							name: 'maxDuration',
							type: 'number',
							label: me.res('domainAccessLog.fldsearch.maxDuration.lbl')
						}, {
							name: 'authenticated',
							type: 'boolean',
							boolKeyword: 'is',
							label: me.res('domainAccessLog.fldsearch.authenticated.lbl')
						}, {
							name: 'failure',
							type: 'boolean',
							boolKeyword: 'is',
							label: me.res('domainAccessLog.fldsearch.failure.lbl')
						}
					],
					tooltip: me.res('domainAccessLog.fldsearch.tip'),
					searchTooltip: me.res('domainAccessLog.fldsearch.tip'),
					emptyText: me.res('domainAccessLog.fldsearch.emp'),
					listeners: {
						query: function(s, value, qObj) {
							me.queryDomainAccessLog(qObj);
						}
					}
				},
				'->',
				me.getAct('refresh')
			]
		});
	},
	
	initActions: function() {
		var me = this;
		
		me.addAct('refresh', {
			text: null,
			tooltip: WT.res('act-refresh.lbl'),
			iconCls: 'wt-icon-refresh',
			handler: function() {
				me.reloadDomainAccessLog();
			}
		});
	},
	
	queryDomainAccessLog: function(query) {
		var me = this,
			isString = Ext.isString(query),
			obj = {
				allText: isString ? query : query.anyText,
				conditions: isString ? [] : query.conditionArray
			};
		me.reloadDomainAccessLog({query: Ext.JSON.encode(obj)});
	},
	
	reloadDomainAccessLog: function(opts) {
		opts = opts || {};
		var me = this, sto, pars = {};
		
		sto = me.lref('gp').getStore();
		if (opts.query !== undefined) Ext.apply(pars, {query: opts.query});
		WTU.loadWithExtraParams(sto, pars);
	}
});

