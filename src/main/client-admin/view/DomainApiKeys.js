/* 
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.DomainApiKeys', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.String',
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.webtop.core.admin.model.GridDomainApiKey'
	],
	uses: [
		'Sonicle.webtop.core.ux.CopyBox'
	],
	
	dockableConfig: {
		title: '{domainApiKeys.tit}',
		iconCls: 'wtadm-icon-apiKeys'
	},
	actionsResPrefix: 'domainApiKeys',
	
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
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			border: false,
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.core.admin.model.GridDomainApiKey',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainApiKeys', null, {
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
					header: me.res('domainApiKeys.gp.name.lbl'),
					flex: 1
				}, {
					dataIndex: 'tokenPrefix',
					header: me.res('domainApiKeys.gp.tokenPrefix.lbl'),
					flex: 1
				}, {
					dataIndex: 'description',
					header: me.res('domainApiKeys.gp.description.lbl'),
					flex: 2
				}, {
					xtype: 'datecolumn',
					dataIndex: 'expireAt',
					header: me.res('domainApiKeys.gp.expireAt.lbl'),
					format: WT.getShortDateTimeFmt(),
					usingDefaultRenderer: true, // Necessary for renderer usage below
					renderer : function(v, meta, rec) {
						if (rec.isExpired()) meta.tdCls = 'wt-color-error';
						return Ext.isEmpty(v) ? '<span style="font-size:larger;">&#8734;</span>' : this.defaultRenderer(v);
					},
					align: 'center',
					width: 150
				}, {
					xtype: 'datecolumn',
					dataIndex: 'createdAt',
					header: me.res('domainApiKeys.gp.createdAt.lbl'),
					format: WT.getShortDateFmt(),
					align: 'center',
					width: 100
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'wt-icon-edit',
							tooltip: WT.res('act-edit.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.editApiKeyUI(rec);
							}
						}, {
							iconCls: 'wt-icon-trash',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteApiKeyUI(rec);
							}
						}
					]
				}
			],
			tbar: [
				me.addAct('addApiKey', {
					ui: '{secondary|toolbar}',
					tooltip: null,
					iconCls: 'wt-icon-add',
					handler: function() {
						me.addApiKeyUI();
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
					me.editApiKeyUI(rec);
				}
			}
		});
	},
	
	addApiKey: function(domainId, opts) {
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.ApiKey', {
				swapReturn: true,
				viewCfg: {
					dialogMode: true,
					domainId: domainId,
					title: me.res('domainApiKeys.act-addApiKey.lbl'),
					saveActionText: me.res('domainApiKeys.act-addApiKey.saveAction.lbl'),
					saveActionTooltip: me.res('domainApiKeys.act-addApiKey.saveAction.tip')
				}
			});

		vw.on('viewsave', function(s, success, model, op) {
			var apiKeyString = success ? op.getResponse().responseJson.metaData['apiKeyString'] : null;
			Ext.callback(opts.callback, opts.scope || me, [success, model, apiKeyString]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {}
			});
		});
	},
	
	editApiKey: function(domainId, apiKeyId, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.ApiKey', {
				swapReturn: true,
				preventDuplicates: true,
				tagSuffix: apiKeyId,
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
					id: apiKeyId
				}
			});
		});
	},
	
	deleteApiKey: function(domainId, apiKeyId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainApiKey', {
			params: {
				crud: 'delete',
				domainId: domainId,
				id: apiKeyId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	privates: {
		addApiKeyUI: function() {
			var me = this;
			me.addApiKey(me.domainId, {
				callback: function(success, model, apiKeyString) {
					if (success) {
						me.showGeneratedApiKeyUI(apiKeyString, {
							callback: function() {
								me.lref('gp').getStore().load();
							}
						});
					}
				}
			});
		},
		
		showGeneratedApiKeyUI: function(apiKey, opts) {
			var me = this;
			WT.prompt(me.mys.res('apiKey.show.msg'), {
				title: me.mys.res('apiKey.show.tit'),
				okText: me.mys.res('apiKey.show.okText'),
				buttons: Ext.Msg.OK,
				instClass: 'Sonicle.webtop.core.ux.CopyBox',
				instConfig: {
					explainText: me.mys.res('apiKey.show.txt'),
					nowTooltip: WT.res('meetingBox.nowTooltip')
				},
				config: {
					value: apiKey
				},
				fn: function(bid, value, cfg) {
					Ext.callback(opts.callback, opts.scope || me, [bid === 'ok', value]);
				}
			});	
		},
		
		editApiKeyUI: function(rec) {
			var me = this;
			me.editApiKey(me.domainId, rec.get('id'), {
				callback: function(success) {
					if (success) me.lref('gp').getStore().load();
				}
			});
		},
		
		deleteApiKeyUI: function(rec) {
			var me = this;
			WT.confirmDelete(me.mys.res('apiKey.confirm.delete', rec.get('name')), function(bid) {
				if (bid === 'ok') {
					me.lref('gp').getStore().remove(rec);
				}
			}, me);
		}
	}
});
