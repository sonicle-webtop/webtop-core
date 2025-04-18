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
Ext.define('Sonicle.webtop.core.admin.view.PecBridge', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Ext.grid.column.Column',
		'Sonicle.webtop.core.admin.model.GridPecBridgeFetcher',
		'Sonicle.webtop.core.admin.model.GridPecBridgeRelay'
	],
	
	/**
	 * @cfg {String} domainId
	 * Target domain ID.
	 */
	domainId: null,
	
	dockableConfig: {
		title: '{pecBridge.tit}',
		iconCls: 'wtadm-icon-pecBridge'
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
		
		me.lookupStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.SubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupSubjects', null, {
				extraParams: {
					domainId: me.domainId,
					users: true,
					fullId: true
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [{
				xtype: 'grid',
				reference: 'gpfetchers',
				title: me.mys.res('pecBridge.gpfetchers.tit'),
				border: false,
				store: {
					autoLoad: true,
					autoSync: true,
					model: 'Sonicle.webtop.core.admin.model.GridPecBridgeFetcher',
					proxy: WTF.apiProxy(me.mys.ID, 'ManagePecBridgeFetchers', 'fetchers', {
						extraParams: {
							domainId: me.domainId
						},
						writer: {
							allowSingle: false // Always wraps records into an array
						}
					}),
					listeners: {
						remove: function(s, recs) {
							//resync message in meta data
							s.load();
						},
						load: function(s, recs) {
							var meta = s.getProxy().getReader().metaData;
							if (meta && Ext.isDefined(meta.message)) me.fetchersMessageLabel.setText(meta.message);
						}
					}
				},
				viewConfig: {
					getRowClass: function(rec) {
						var cls = rec.get('licensed') === false ? 'wt-text-italic wt-text-striked wt-theme-color-error' :
							rec.get('enabled') === false ? 'wt-text-striked wt-text-off wt-theme-text-color-off' : '';
						return cls;
					}
				},
				columns: [
					{
						xtype: 'rownumberer'
					}, {
						xtype: 'solookupcolumn',
						dataIndex: 'forwardProfile',
						store: me.lookupStore,
						displayField: 'labelNameWithDN',
						header: me.mys.res('pecBridge.gpfetchers.forwardProfile.lbl'),
						flex: 1
					}, {
						dataIndex: 'forwardAddress',
						header: me.mys.res('pecBridge.gpfetchers.forwardAddress.lbl'),
						flex: 1
					}, {
						dataIndex: 'host',
						header: me.mys.res('pecBridge.gpfetchers.host.lbl'),
						flex: 1
					},{
						xtype: 'soiconcolumn',
						dataIndex: 'authState',
						getIconCls: function(v,rec) {
							return 'wtadm-icon-pecBridge-authState-'+v;
						},
						getTip: function(v) {
							return me.mys.res('pecBridge.authState.tip.'+v);
						},
						iconSize: WTU.imgSizeToPx('xs'),
						width: 40
					}, {
						xtype: 'soactioncolumn',
						items: [
							{
								iconCls: 'wt-glyph-delete',
								tooltip: WT.res('act-remove.lbl'),
								handler: function(view, ridx, cidx, itm, e, rec) {
									me.deleteFetcherUI(rec);
								}
							}
						]
					}
				],
				tbar: [
					me.addAct('addFetcher', {
						text: WT.res('act-add.lbl'),
						tooltip: null,
						iconCls: 'wt-icon-add',
						ui: '{tertiary|toolbar}',
						handler: function() {
							me.addFetcherUI();
						}
					}),
					'->',
					me.fetchersMessageLabel = Ext.create({
						xtype: 'label',
						text: '',
						cls: 'wt-theme-color-error'
					}),
					'->',
					me.addAct('refreshFetchers', {
						text: null,
						tooltip: WT.res('act-refresh.lbl'),
						iconCls: 'wt-icon-refresh',
						handler: function() {
							me.lref('gpfetchers').getStore().load();
						}
					})
				],
				listeners: {
					rowdblclick: function(s, rec) {
						me.editFetcherUI(rec);
					}
				},
				flex: 1
			}, {
				xtype: 'grid',
				reference: 'gprelays',
				title: me.mys.res('pecBridge.gprelays.tit'),
				border: false,
				store: {
					autoLoad: true,
					autoSync: true,
					model: 'Sonicle.webtop.core.admin.model.GridPecBridgeRelay',
					proxy: WTF.apiProxy(me.mys.ID, 'ManagePecBridgeRelays', 'relays', {
						extraParams: {
							domainId: me.domainId
						},
						writer: {
							allowSingle: false // Always wraps records into an array
						}
					}),
					listeners: {
						remove: function(s, recs) {
							//resync message in meta data
							s.load();
						},
						load: function(s, recs) {
							var meta = s.getProxy().getReader().metaData;
							if (meta && Ext.isDefined(meta.message)) me.relaysMessageLabel.setText(meta.message);
						}
					}
				},
				viewConfig: {
					getRowClass: function(rec) {
						var cls = rec.get('licensed') === false ? 'wt-text-italic wt-text-striked wt-theme-color-error' :
							rec.get('enabled') === false ? 'wt-text-striked wt-text-off wt-theme-text-color-off' : '';
						return cls;
					}
				},
				columns: [
					{
						xtype: 'rownumberer'
					}, {
						xtype: 'solookupcolumn',
						dataIndex: 'pecProfile',
						store: me.lookupStore,
						displayField: 'labelNameWithDN',
						header: me.mys.res('pecBridge.gprelays.pecProfile.lbl'),
						flex: 1
					}, {
						dataIndex: 'pecAddress',
						header: me.mys.res('pecBridge.gprelays.pecAddress.lbl'),
						flex: 1
					}, {
						dataIndex: 'host',
						header: me.mys.res('pecBridge.gprelays.host.lbl'),
						flex: 1
					},{
						xtype: 'soiconcolumn',
						dataIndex: 'authState',
						getIconCls: function(v,rec) {
							return 'wtadm-icon-pecBridge-authState-'+v;
						},
						getTip: function(v) {
							return me.mys.res('pecBridge.authState.tip.'+v);
						},
						iconSize: WTU.imgSizeToPx('xs'),
						width: 40
					}, {
						xtype: 'soactioncolumn',
						items: [
							{
								iconCls: 'wt-glyph-delete',
								tooltip: WT.res('act-remove.lbl'),
								handler: function(view, ridx, cidx, itm, e, rec) {
									me.deleteRelayUI(rec);
								}
							}
						]
					}
				],
				tbar: [
					me.addAct('addRelay', {
						text: WT.res('act-add.lbl'),
						tooltip: null,
						iconCls: 'wt-icon-add',
						ui: '{tertiary|toolbar}',
						handler: function() {
							me.addRelayUI();
						}
					}),
					'->',
					me.relaysMessageLabel = Ext.create({
						xtype: 'label',
						text: '',
						cls: 'wt-theme-color-error'
					}),
					'->',
					me.addAct('refreshRelays', {
						text: null,
						tooltip: WT.res('act-refresh.lbl'),
						iconCls: 'wt-icon-refresh',
						handler: function() {
							me.lref('gprelays').getStore().load();
						}
					})
				],
				listeners: {
					rowdblclick: function(s, rec) {
						me.editRelayUI(rec);
					}
				},
				flex: 1
			}]
		});
	},
	
	addFetcherUI: function() {
		var me = this;
		me.mys.addPecBridgeFetcher(me.domainId, {
			callback: function(success) {
				if(success) {
					me.lref('gpfetchers').getStore().load();
				}
			}
		});
	},
	
	editFetcherUI: function(rec) {
		var me = this,
				fetcherId = rec.get('fetcherId');
		me.mys.editPecBridgeFetcher(fetcherId, {
			callback: function(success) {
				if(success) {
					me.lref('gpfetchers').getStore().load();
				}
			}
		});
	},
	
	deleteFetcherUI: function(rec) {
		var me = this,
				sto = me.lref('gpfetchers').getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				sto.remove(rec);
			}
		}, me);
	},
	
	addRelayUI: function() {
		var me = this;
		me.mys.addPecBridgeRelay(me.domainId, {
			callback: function(success) {
				if(success) {
					me.lref('gprelays').getStore().load();
				}
			}
		});
	},
	
	editRelayUI: function(rec) {
		var me = this,
				fetcherId = rec.get('relayId');
		me.mys.editPecBridgeRelay(fetcherId, {
			callback: function(success) {
				if(success) {
					me.lref('gprelays').getStore().load();
				}
			}
		});
	},
	
	deleteRelayUI: function(rec) {
		var me = this,
				sto = me.lref('gprelays').getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				sto.remove(rec);
			}
		}, me);
	}
});
