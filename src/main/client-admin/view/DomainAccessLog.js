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
		'Sonicle.webtop.core.admin.model.GridDomainAccessLog',
		'WTA.ux.field.Search'
	],
	
	/**
	 * @cfg {String} domainId
	 * Target domain ID.
	 */
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
		var me = this,
				geoAvail = WT.getVar('wtGeolocationProvider');
		me.callParent(arguments);
		me.initActions();
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			border: false,
			store: {
				type: 'buffered',
				autoLoad: false,
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
					property: 'timestamp',
					direction: 'DESC'
				}],
				pageSize: 50,
				leadingBufferZone: 50,
				trailingBufferZone: 50
			},
			viewConfig: {
				enableTextSelection: true,
				emptyText: me.mys.res('domainAccessLog.gp.emptyText.lbl')
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
					header: WTF.headerWithGlyphIcon('far fa-check-square'),
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
					dataIndex: 'timestamp',
					format: WT.getShortDateFmt() + ' ' +  WT.getLongTimeFmt(),
					sortable: true,
					groupable: false,
					align: 'center',
					header: me.res('domainAccessLog.gp.date.lbl'),
					width: 180
				}, {
					dataIndex: 'duration',
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
								model: 'Sonicle.webtop.core.admin.model.GridDomainAccessLogDetail',
								proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainAccessLogDetail', null, {
									extraParams: {
										sessionId: '{record.sessionId}',
										domainId: me.domainId,
										userId: '{record.userId}'
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
								width: 180
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
								width: 250
							}, {
								dataIndex: 'ipAddress',
								sortable: false,
								groupable: false,
								align: 'center',
								emptyCellText: '-',
								header: me.res('domainAccessLog.gp.details.ipAddress.lbl'),
								width: 150
							}, {
								dataIndex: 'geoInfo',
								renderer: function(v, meta, rec) {
									if (v === true) {
										return rec.genGeoCountryFlagMarkup() + '&nbsp;' + Ext.String.htmlEncode(rec.genGeoDescription());
									} else {
										meta.tdCls = 'wt-text-off wt-theme-text-color-off';
										var key = geoAvail && rec.get('isAddressPublic') ? 'domainAccessLog.gp.details.geoInfo.avail.emp' : 'domainAccessLog.gp.details.geoInfo.unavail.emp';
										return '(' + me.res(key) + ')';
									}
								},
								header: me.res('domainAccessLog.gp.details.geoInfo.lbl'),
								flex: 1
							}, {
								xtype: 'soactioncolumn',
								items: [
									{
										iconCls: 'wt-glyph-earth',
										tooltip: me.mys.res('domainAccessLog.gp.act-geolocateIp.tip'),
										handler: function(g, ridx) {
											var sto = g.getStore(),
													rec = sto.getAt(ridx),
													ip = rec.get('ipAddress');
											
											me.wait();
											me.mys.geolocateIPs(ip, {
												callback: function(success, data) {
													me.unwait();
													if (success) {
														sto.each(function(rec1) {
															if (rec1.get('ipAddress') === ip) rec1.setGeoData(data[0]);
														});
													}
												}
											});
										},
										isActionDisabled: function(s, ridx, cidx, itm, rec) {
											if (!geoAvail || rec.get('geoInfo') === true) return true;
											return !rec.get('isAddressPublic');
										}
									}, {
										iconCls: 'wt-glyph-clipboard-copy',
										tooltip: me.mys.res('domainAccessLog.gp.act-copy.tip'),
										handler: function(g, ridx) {
											var rec = g.getStore().getAt(ridx);
											Sonicle.ClipboardMgr.copy(rec.toString());
											WT.toast(WT.res('toast.info.copied'));
										}
									}
								]
							}
						]
					}
				}
			],
			tbar: [
				{
					xtype: 'sofieldhgroup',
					items: [
						{
							xtype: 'socombobutton',
							ui: 'default-toolbar',
							tooltip: me.res('domainAccessLog.viewOptions.tip'),
							showText: true,
							menu: {
								items: [
									{
										itemId: 'today',
										text: me.res('domainAccessLog.viewOptions.today.tip.tit'),
										tooltip: me.res('domainAccessLog.viewOptions.today.tip.txt'),
										checked: true
									}, {
										itemId: 'prev7',
										text: me.res('domainAccessLog.viewOptions.prev7.tip.tit'),
										tooltip: me.res('domainAccessLog.viewOptions.prev7.tip.txt')
									}, {
										itemId: 'prev14',
										text: me.res('domainAccessLog.viewOptions.prev14.tip.tit'),
										tooltip: me.res('domainAccessLog.viewOptions.prev14.tip.txt')
									}, {
										itemId: 'prev30',
										text: me.res('domainAccessLog.viewOptions.prev30.tip.tit'),
										tooltip: me.res('domainAccessLog.viewOptions.prev30.tip.txt')
									}, {
										itemId: 'prev90',
										text: me.res('domainAccessLog.viewOptions.prev90.tip.tit'),
										tooltip: me.res('domainAccessLog.viewOptions.prev90.tip.txt')
									}
								]
							},
							changeHandler: function(s, menuItem) {
								var itemId = menuItem.getItemId(), offset;
								if (itemId === 'prev7') offset = 7;
								else if (itemId === 'prev14') offset = 14;
								else if (itemId === 'prev30') offset = 30;
								else if (itemId === 'prev90') offset = 90;
								else offset = 0;
								me.prepareSearchDates(offset);
							}
						}
					],
					width: 180
				},
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
							name: 'ip',
							type: 'string',
							label: me.res('domainAccessLog.fldsearch.ip.lbl')
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
		me.on('afterrender', function() {
			me.prepareSearchDates(0);
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
	},
	
	prepareSearchDates: function(offset) {
		var me = this,
				dt = new Date(),
				dateFormat = 'Y-m-d',
				toDate = Ext.Date.format(dt, dateFormat),
				fromDate;
		
		if (offset%30 == 0) {
			fromDate = Ext.Date.format(Sonicle.Date.add(dt, { months: -(offset/30) }, true), dateFormat);
		} else {
			fromDate = Ext.Date.format(Sonicle.Date.add(dt, { days: -offset }, true), dateFormat);
		}
		
		me.setSearchDates(fromDate, toDate);
	},
	
	setSearchDates: function(fromDate, toDate) {
		var me = this,
				fldSearch = me.lref('fldsearch'),
				searchString = fldSearch.getSearchString();
		
		if (fromDate) {
			searchString.removeKeyword('dateFrom', false);
			searchString.addEntry('dateFrom', fromDate, false);
		}
		
		if (toDate) {
			searchString.removeKeyword('dateTo', false);
			searchString.addEntry('dateTo', toDate, false);
		}
		
		fldSearch.setSearchString(searchString, true);
	}
	
	/*
	geolocateIpAddress: function(ips, opts) {
		opts = opts || {};
		var me = this,
				addrs = Ext.Array.from(ips),
				fn = opts.callback,
				scope = opts.scope;
		
		if (Ext.isEmpty(opts.apiKey)) Ext.raise('missin apikey');
		
		Ext.Ajax.request({
			method: 'GET',
			url: 'http:/'+'/api.ipstack.com/' + addrs.join(','),
			useDefaultXhrHeader : false,
			params: Ext.applyIf({
				access_key: opts.apiKey,
				output: 'json',
				fields: 'main,location.country_flag'
			}, opts.params || {}),
			success: function(resp, opts) {
				var json = Ext.decode(resp.responseText);
				Ext.callback(fn, scope || me, [true, json, opts]);
			},
			failure: function(resp, opts) {
				Ext.callback(fn, scope || me, [false, {}, opts]);
			}
		});
	}
	*/
});