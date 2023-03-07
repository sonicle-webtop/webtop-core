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
Ext.define('Sonicle.webtop.core.admin.view.DomainResources', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.admin.model.GridResource'
	],
	uses: [
		'Sonicle.webtop.core.admin.view.Resource'
	],
	
	/**
	 * @cfg {String} domainId
	 * The bound domain ID for this entity.
	 */
	domainId: null,
	
	/**
	 * @cfg {String} domainName
	 * The primary domain-name of the bound domain ID.
	 */
	domainName: null,
	
	dockableConfig: {
		title: '{domainResources.tit}',
		iconCls: 'wt-icon-resources'
	},
	actionsResPrefix: 'domainResources',
	
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
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.admin.model.GridResource',
				proxy: WTF.proxy(me.mys.ID, 'ManageDomainResources', null, {
					extraParams: {
						domainId: me.domainId,
						crud: 'read'
					},
					writer: {
						allowSingle: false // Always wraps records into an array
					}
				})
			},
			viewConfig: {
				getRowClass: function(rec) {
					return rec.get('available') === false ? 'wt-text-striked wt-theme-text-error' : '';
				}
			},
			columns: [
				{
					xtype: 'rownumberer'
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'type',
					getIconCls: function(v, rec) {
						return 'wt-icon-resource-'+v;
					},
					getTip: function(v, rec) {
						return WT.res('store.resourceType.'+v);
					},
					iconSize: WTU.imgSizeToPx('xs'),
					header: WTF.headerWithGlyphIcon('fas fa-store'),
					width: 40
				}, {
					dataIndex: 'name',
					header: me.res('domainResources.gp.name.lbl'),
					flex: 1
				}, {
					dataIndex: 'displayName',
					header: me.res('domainResources.gp.displayName.lbl'),
					flex: 2
				}, {
					dataIndex: 'email',
					header: me.res('domainResources.gp.email.lbl'),
					flex: 2
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'far fa-edit',
							tooltip: WT.res('act-edit.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.editResourceUI(rec);
							}
						}, {
							iconCls: 'far fa-trash-alt',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteResourceUI(rec);
							}
						}
					]
				}
			],
			tbar: [
				me.addAct('add', {
					tooltip: null,
					iconCls: null,
					handler: function() {
						me.addResourceUI();
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
					me.editResourceUI(rec);
				}
			}
		});
	},
	
	addResource: function(domainId, domainName, opts) {
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.Resource', {
				swapReturn: true,
				viewCfg: {
					domainId: domainId,
					domainName: domainName
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
	
	editResource: function(domainId, domainName, resourceName, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.mys.ID, 'view.Resource', {
				swapReturn: true,
				viewCfg: {
					domainId: domainId,
					domainName: domainName
				}
			});

		vw.on('viewsave', function(s, success, model, op) {
			Ext.callback(opts.callback, opts.scope || me, [success, model, op]);
		});
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: resourceName
				}
			});
		});
	},
	
	deleteResource: function(domainId, resourceId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainResource', {
			params: {
				crud: 'delete',
				domainId: domainId,
				id: resourceId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	privates: {
		addResourceUI: function() {
			var me = this,
				gp = me.lref('gp');

			me.addResource(me.domainId, me.domainName, {
				callback: function(success, model, op) {
					WT.handleMessage(success, op);
					if (success) gp.getStore().load();
				}
			});
		},

		editResourceUI: function(rec) {
			var me = this,
				gp = me.lref('gp');

			me.editResource(me.domainId, me.domainName, rec.getId(), {
				callback: function(success, model, op) {
					WT.handleMessage(success, op);
					if (success) gp.getStore().load();
				}
			});
		},

		deleteResourceUI: function(rec) {
			var me = this;
			WT.confirm(me.res('resource.confirm.delete', rec.get('name')), function(bid) {
				if (bid === 'yes') {
					me.wait();
					me.deleteResource(me.domainId, rec.getId(), {
						callback: function(success, data, json) {
							me.unwait();
							if (success) me.lref('gp').getStore().load();
							WT.handleError(success, json);
							WT.handleMessage(success, json);
						}
					});
				}
			}, me);
		}
	}
});
