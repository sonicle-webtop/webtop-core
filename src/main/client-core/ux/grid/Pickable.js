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
Ext.define('Sonicle.webtop.core.ux.grid.Pickable', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtpickablegrid',
	requires: [
		'Sonicle.picker.List',
		'Sonicle.grid.column.Lookup',
		'WTA.ux.PickerWindow'
	],
	
	/**
	 * @cfg {Object} lookupConfig
	 * 
	 * @cfg {String} lookupConfig.model
	 * @cfg {String} lookupConfig.service
	 * @cfg {String} lookupConfig.action
	 * @cfg {String} lookupConfig.rootp
	 * @cfg {String} lookupConfig.opts
	 */
	
	/**
	 * @cfg {Object} columnConfig
	 * 
	 * @cfg {String} columnConfig.dataIndex
	 * @cfg {String} columnConfig.displayField
	 */
	
	/**
	 * @cfg {Object} pickerConfig
	 * 
	 * @cfg {String} pickerConfig.titleKey
	 * @cfg {String} lookupConfig.searchTextKey
	 * @cfg {String} lookupConfig.valueField
	 * @cfg {String} lookupConfig.displayField
	 */
	
	/**
	 * @cfg {String} emptyTextKey
	 */
	
	/**
	 * @cfg {String} sid
	 * The service ID.
	 */
	
	/*
	 * @private
	 * @readonly
	 */
	lookupStore: null,
	
	/*
	 * @private
	 * @readonly
	 */
	picker: null,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this, lkCfg, colCfg;
		
		me.lookupConfig = me.lookupConfig || {};
		me.lookupConfig = lkCfg = Ext.applyIf(me.lookupConfig, {
			service: me.sid
		});
		
		me.columnConfig = colCfg = me.columnConfig || {};
		
		me.pickerConfig = me.pickerConfig || {};
		me.pickerConfig = Ext.applyIf(me.pickerConfig, {
			enableGrouping: false
		});
		
		me.lookupStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: lkCfg.model,
			proxy: WTF.proxy(lkCfg.service, lkCfg.action, lkCfg.rootp, lkCfg.opts)
		});
		
		me.selModel = {
			type: 'rowmodel'
		};
		
		if (!Ext.isEmpty(me.emptyTextKey)) {
			me.viewConfig = Ext.apply(me.viewConfig || {}, {
				deferEmptyText: false,
				emptyText: WT.res(me.sid, me.emptyTextKey)
			});
		}
		
		me.hideHeaders = true;
		me.columns = [{
			xtype: 'solookupcolumn',
			dataIndex: colCfg.dataIndex,
			store: me.lookupStore,
			displayField: colCfg.displayField,
			flex: 1
		}, {
			xtype: 'actioncolumn',
			align: 'center',
			width: 50,
			items: [{
				iconCls: 'fa fa-minus-circle',
				tooltip: WT.res('act-remove.lbl'),
				handler: function (gp, ri) {
					gp.getStore().removeAt(ri);
				}
			}]
		}];
	
		me.tools = me.tools || [];
		me.tools.push({
			type: 'plus',
			callback: me.onAddClick,
			scope: me
		});
		
		me.callParent(arguments);
	},
	
	destroy: function() {
		var me = this;
		me.callParent();
		me.picker = null;
		me.lookupStore = null;
	},
	
	createPicker: function () {
		var me = this,
				pkCfg = me.pickerConfig;
		return Ext.create({
			xtype: 'wtpickerwindow',
			title: WT.res(me.sid, pkCfg.titleKey),
			height: 350,
			items: [{
				xtype: 'solistpicker',
				store: me.lookupStore,
				enableGrouping: pkCfg.enableGrouping,
				valueField: pkCfg.valueField,
				displayField: pkCfg.displayField,
				searchField: pkCfg.displayField,
				emptyText: WT.res('grid.emptyText'),
				searchText: WT.res(WT.ID, pkCfg.searchTextKey),
				okText: WT.res('act-ok.lbl'),
				cancelText: WT.res('act-cancel.lbl'),
				listeners: {
					cancelclick: function () {
						if (me.picker) me.picker.close();
					}
				},
				handler: me.onPickerPick,
				scope: me
			}]
		});
	},
	
	privates: {
		onAddClick: function () {
			var me = this;
			me.lookupStore.clearFilter();
			me.picker = me.createPicker();
			me.picker.show();
		},
		
		onRemoveClick: function () {
			var me = this,
					rec = me.getSelection()[0];
			if (rec) me.getStore().remove(rec);
		},
		
		onPickerPick: function (s, val, rec) {
			var me = this;
			me.fireEvent('pick', me, val, rec);
			me.picker.close();
			me.picker = null;
		}
	}
});
