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
		'Sonicle.webtop.core.admin.model.DomainAccessLog',
		'Sonicle.webtop.core.admin.model.DomainAccessLogDetail',
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
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			store: {
				type: 'buffered',
				autoLoad: true,
				model: 'Sonicle.webtop.core.admin.model.DomainAccessLog',
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
				getRowClass: function(record, rowIndex, rowParam, store) {
					if (record.data.failure) {
						return record.data.authenticated ? 'wtadm-domainAccessLog-warning' : 'wtadm-domainAccessLog-danger';
					}
				},
				enableTextSelection: true
			},
			columns: [{
				xtype: 'rownumberer',
				width: '80 px'
			}, {
				dataIndex: 'sessionId',
				sortable: true,
				groupable: false,
				header: me.res('domainAccessLog.sessionId.lbl'),
				flex: 3
			}, {
				dataIndex: 'userId',
				align: 'center',
				sortable: true,
				groupable: false,
				header: me.res('domainAccessLog.userId.lbl'),
				flex: 2,
				renderer: function(value) {
					return '?' === value ? '-' : value;
				}
			}, {
				xtype: 'datecolumn',
				format: WT.getShortDateFmt() + ' ' +  WT.getLongTimeFmt(),
				align: 'center',
				dataIndex: 'date',
				sortable: true,
				groupable: false,
				header: me.res('domainAccessLog.date.lbl'),
				flex: 2
			}, {
				dataIndex: 'minutes',
				align: 'center',
				sortable: true,
				groupable: false,
				header: me.res('domainAccessLog.minutes.lbl'),
				emptyCellText: '-',
				flex: 2
			}, {
				xtype: 'booleancolumn',
				trueText: me.res('domainAccessLog.yes.lbl'),
				falseText: me.res('domainAccessLog.no.lbl'),
				align: 'center',
				dataIndex: 'authenticated',
				sortable: true,
				groupable: false,
				header: me.res('domainAccessLog.authenticated.lbl'),
				flex: 2
			}, {
				align: 'center',
				dataIndex: 'loginErrors',
				sortable: true,
				groupable: false,
				header: me.res('domainAccessLog.loginErrors.lbl'),
				flex: 2
			}],
			plugins: [
				{
					ptype: 'rowwidget',
					widget: {
						xtype: 'grid',
						title: me.mys.res('domainAccessLogDetail.tit'),
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
						columns: [{
							xtype: 'datecolumn',
							format: WT.getShortDateFmt() + ' ' +  WT.getLongTimeFmt(),
							dataIndex: 'timestamp',
							align: 'center',
							sortable: false,
							groupable: false,
							header: me.res('domainAccessLogDetail.date.lbl'),
							emptyCellText: '-',
							flex: 2
						}, {
							dataIndex: 'action',
							align: 'center',
							sortable: false,
							groupable: false,
							header: me.res('domainAccessLogDetail.action.lbl'),
							emptyCellText: '-',
							renderer: WTF.resColRenderer({
								id: me.mys.ID,
								key: 'domainAccessLogDetail.action',
								keepcase: true
							}),
							flex: 2
						}, {
							dataIndex: 'ipAddress',
							align: 'center',
							sortable: false,
							groupable: false,
							header: me.res('domainAccessLogDetail.ipAddress.lbl'),
							emptyCellText: '-',
							flex: 2
						}]
					}
				}
			],
			tbar: [
				{
					xtype: 'toolbar',
					referenceHolder: true,
					flex: 1,
					items: [
						'->',
						{
							xtype: 'wtsearchfield',
							reference: 'fldsearch',
							highlightKeywords: ['sessionId', 'userId'],
							fields: [
								{
									name: 'session',
									type: 'string',
									label: me.res('domainAccesslLog.src-field.session.lbl')
								}, {
									name: 'user',
									type: 'string',
									label: me.res('domainAccesslLog.src-field.user.lbl')
								}, {
									name: 'dateFrom',
									type: 'date',
									labelAlign: 'left',
									label: me.res('domainAccesslLog.src-field.dateFrom.lbl')
								}, {
									name: 'dateTo',
									type: 'date',
									labelAlign: 'left',
									label: me.res('domainAccesslLog.src-field.dateTo.lbl')
								}, {
									name: 'minDuration',
									type: 'number',
									label: me.res('domainAccesslLog.src-field.minDuration.lbl')
								}, {
									name: 'maxDuration',
									type: 'number',
									label: me.res('domainAccesslLog.src-field.maxDuration.lbl')
								}, {
									name: 'authenticated',
									type: 'boolean',
									boolKeyword: 'is',
									label: me.res('domainAccesslLog.src-field.authenticated.lbl')
								}, {
									name: 'failure',
									type: 'boolean',
									boolKeyword: 'is',
									label: me.res('domainAccesslLog.src-field.failure.lbl')
								}
							],
							tooltip: me.res('domainAccessLog.src-field.tip'),
							searchTooltip: me.res('domainAccessLog.src-field.tip'),
							emptyText: me.res('domainAccessLog.src-field.emp'),
							listeners: {
								query: function(s, value, qObj) {
									me.queryDomainAccessLog(qObj);
								}
							}
						},
						'->',
						me.getAct('refresh')
					]
				}
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

