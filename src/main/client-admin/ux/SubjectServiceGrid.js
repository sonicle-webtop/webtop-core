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
Ext.define('Sonicle.webtop.core.admin.ux.SubjectServiceGrid', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtadmsubjectservicegrid',
	requires: [
		'Sonicle.Object',
		'Sonicle.grid.column.Lookup',
		'Sonicle.webtop.core.admin.ux.PermissionPicker'
	],
	
	storeLookupField: 'serviceId',
	
	/**
	 * @cfg {Function} recordCreatorFn
	 * A custom function that returns record data object, properly filled, 
	 * to be added to bound store.
	 */
	
	initComponent: function() {
		var me = this;
		if (!Ext.isFunction(me.recordCreatorFn)) Ext.raise('recordCreatorFn is mandatory');
		
		me.lookupStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'Sonicle.webtop.core.model.ServiceLkp',
			proxy: WTF.proxy(WT.ID, 'LookupServices', null, {
				extraParams: {assignableOnly: true}
			})
		});
		
		me.selModel = {
			type: 'rowmodel'
		};
		if (!me.viewConfig) {
			me.viewConfig = {
				deferEmptyText: false,
				emptyText: WT.res(WT.ID + '.admin', 'wtadmsubjectservicegrid.emp')
			};
		}
		
		if (!me.columns) {
			me.hideHeaders = true;
			me.columns = [
				{
					xtype: 'solookupcolumn',
					dataIndex: me.storeLookupField,
					store: me.lookupStore,
					displayField: 'label',
					flex: 1
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'wt-glyph-delete',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(view, ridx, cidx, itm, e, rec) {
								view.getStore().remove(rec);
							}
						}
					]
				}
			];
		}
		if (!me.tbar) {
			me.tbar = me.tbar || [];
			me.tbar.push(
				{
					ui: '{tertiary}',
					text: WT.res('act-add.lbl'),
					handler: function() {
						me.showPicker();
					}
				}
			);
		}
		me.callParent(arguments);
	},
	
	doDestroy: function() {
		var me = this;
		delete me.lookupStore;
		delete me.picker;
		me.callParent();
	},
	
	privates: {
		showPicker: function() {
			var me = this,
				usedSubjects = Sonicle.Data.collectValues(me.getStore());
			me.picker = me.createPicker();
			me.picker.getComponent(0).setSkipValues(usedSubjects);
			me.picker.show();
		},
		
		createPicker: function() {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: WT.res(WT.ID + '.admin', 'wtadmsubjectservicegrid.picker.tit'),
				height: 450,
				items: [
					{
						xtype: 'solistpicker',
						store: {
							xclass: 'Ext.data.ChainedStore',
							source: me.lookupStore
						},
						valueField: 'id',
						displayField: 'label',
						searchField: 'label',
						emptyText: WT.res('grid.emp'),
						searchText: WT.res('textfield.search.emp'),
						selectedText: WT.res('grid.selected.lbl'),
						okText: WT.res('act-ok.lbl'),
						cancelText: WT.res('act-cancel.lbl'),
						allowMultiSelection: true,
						listeners: {
							cancelclick: function() {
								if (me.picker) me.picker.close();
							}
						},
						handler: me.onPickerPick,
						scope: me
					}
				]
			});
		},
		
		onPickerPick: function(s, values, recs, button) {
			var me = this,
				sto = me.getStore();
			Ext.iterate(values, function(value) {
				sto.add(me.recordCreatorFn(value));
			});
			
			me.fireEvent('pick', me, values);
			me.picker.close();
			me.picker = null;
		}
	}
});
