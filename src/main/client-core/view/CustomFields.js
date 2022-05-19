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
Ext.define('Sonicle.webtop.core.view.CustomFields', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.String',
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.webtop.core.model.CustomFieldGrid'
	],
	uses: [
		'Sonicle.Data',
		'Sonicle.webtop.core.view.CustomField'
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
		title: '{customFields.tit}',
		iconCls: 'wt-icon-customField',
		width: 850,
		height: 500
		//modal: true
	},
	promptConfirm: false,
	
	viewModel: {
		data: {
			gpstorecount: -1
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.serviceId) {
			Ext.raise('serviceId is mandatory');
		}
		me.fieldsLimit = WT.getVar('customFieldsLimit') || 5;
		Ext.merge(cfg, {
			dockableConfig: {
				title: '[' + Sonicle.String.deflt(cfg.serviceName, cfg.serviceId) + '] ' + WT.res('customFields.tit')
			}
		});
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foAddEnabled: WTF.foGetFn('gpstorecount', null, function(v) {
				return (me.fieldsLimit === -1) ? true : v < me.fieldsLimit;
			})
		});
	},
	
	initComponent: function() {
		var me = this;
		
		if (me.fieldsLimit !== -1) {
			Ext.apply(me, {
				bbar: {
					xtype: 'statusbar',
					items: [
						{
							xtype: 'tbtext',
							html: me.res('customFields.free.txt', me.fieldsLimit)
						}, {
							xtype: 'button',
							iconCls: 'fas fa-info-circle',
							handler: function() {
								WT.info(me.res('customFields.info.free', me.fieldsLimit));
							}
						}
					]
				}
			});
		}
		
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			border: false,
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.core.model.CustomFieldGrid',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageCustomFields', 'data', {
					extraParams: {
						targetServiceId: me.serviceId
					}
				}),
				listeners: {
					load: function(s) {
						me.getVM().set('gpstorecount', s.getCount());
					},
					datachanged: function(s) {
						me.getVM().set('gpstorecount', s.getCount());
					}
				}
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: WT.res('grid.emp')
			},
			columns: [{
					xtype: 'rownumberer'
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'type',
					header: me.res('customFields.gp.type.lbl'),
					hideText: false,
					getText: function(v) {
						return me.res('store.customFieldType.'+v);
					},
					getIconCls: function(v) {
						return me.mys.cssIconCls('customField-'+v);
					},
					iconSize: WTU.imgSizeToPx('xs'),
					flex: 1
				}, {
					dataIndex: 'name',
					header: me.res('customFields.gp.name.lbl'),
					flex: 1
				}, {
					dataIndex: 'description',
					header: me.res('customFields.gp.description.lbl'),
					flex: 2
				}, {
					xtype: 'checkcolumn',
					dataIndex: 'searchable',
					header: WTF.headerWithGlyphIcon('fas fa-binoculars'),
					tooltip: me.res('customFields.gp.searchable.lbl'),
					disabled: true,
					disabledCls : '',
					width: 50
				}, {
					xtype: 'checkcolumn',
					dataIndex: 'previewable',
					header: WTF.headerWithGlyphIcon('far fa-newspaper'),
					tooltip: me.res('customFields.gp.previewable.lbl'),
					disabled: true,
					disabledCls : '',
					width: 50
				}, {
					dataIndex: 'panelsCount',
					header: me.res('customFields.gp.usedby.lbl'),
					renderer: function(val, meta, rec) {
						var key = 'customFields.gp.usedby.';
						return me.res(key + (val === 1 ? 'panel' : 'panels'), val);
					},
					align: 'left',
					width: 100
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'far fa-clone',
							tooltip: WT.res('act-clone.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.cloneCustomFieldUI(rec);
							}
						}, {
							iconCls: 'far fa-trash-alt',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteCustomFieldUI(rec);
							}
						}
					]
				}
			],
			tbar: [
				{
					xtype: 'button',
					bind: {
						disabled: '{!foAddEnabled}'
					},
					text: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add',
					handler: function() {
						me.addCustomFieldUI();
					}
				},
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
					me.editCustomFieldUI(rec);
				}
			}
		});
	},
	
	addCustomField: function(serviceId, data, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.mys.ID, 'view.CustomField', {
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
				})
			});
		});
	},
	
	editCustomField: function(cid, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.mys.ID, 'view.CustomField', {
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
		addCustomFieldUI: function() {
			var me = this;
			me.addCustomField(me.serviceId, null, {
				callback: function() {
					me.lref('gp').getStore().load();
				}
			});
		},

		editCustomFieldUI: function(rec) {
			var me = this;
			me.editCustomField(rec.getId(), {
				callback: function() {
					me.lref('gp').getStore().load();
				}
			});
		},
		
		cloneCustomFieldUI: function(rec) {
			var me = this;
			WT.ajaxReq(me.mys.ID, 'ManageCustomField', {
				params: {
					crud: 'read',
					id: rec.getId()
				},
				callback: function(success, json) {
					if (success) {
						var data = Ext.apply(json.data, {
							id: undefined,
							fieldId: undefined,
							name: Sonicle.Data.getDuplValue(me.lref('gp').getStore(), 'name', json.data.name)
						});
						me.addCustomField(me.serviceId, Sonicle.Utils.applyIfDefined({}, data), {
							callback: function() {
								me.lref('gp').getStore().load();
							}
						});
					}
				}
			});
		},

		deleteCustomFieldUI: function(rec) {
			var me = this,
					grid = me.lref('gp'),
					sto = grid.getStore();

			WT.confirm(me.res('customFields.confirm.delete'), function(bid) {
				if (bid === 'yes') sto.remove(rec);
			}, me);
		}
	},
	
	statics: {
		VIEW_TAG: 'cfields'
	}
});
