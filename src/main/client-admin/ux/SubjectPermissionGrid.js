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
Ext.define('Sonicle.webtop.core.admin.ux.SubjectPermissionGrid', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtadmsubjectpermissiongrid',
	requires: [
		'Sonicle.Object',
		'Sonicle.grid.column.Lookup'
	],
	
	/**
	 * @cfg {Function} recordCreatorFn
	 * A custom function that returns record data object, properly filled, 
	 * to be added to bound store.
	 */
	
	initComponent: function() {
		var me = this;
		if (!Ext.isFunction(me.recordCreatorFn)) Ext.raise('recordCreatorFn is mandatory');
		
		me.selModel = {
			type: 'rowmodel'
		};
		if (!me.viewConfig) {
			me.viewConfig = {
				deferEmptyText: false,
				emptyText: WT.res(WT.ID + '.admin', 'wtadmsubjectpermissiongrid.emp')
			};
		}
		
		if (!me.columns) {
			//me.hideHeaders = true;
			me.columns = [
				{
					dataIndex: 'serviceId',
					header: WT.res(WT.ID + '.admin', 'wtadmsubjectpermissiongrid.serviceId.lbl'),
					flex: 1
				}, {
					dataIndex: 'context',
					header: WT.res(WT.ID + '.admin', 'wtadmsubjectpermissiongrid.context.lbl'),
					flex: 1
				}, {
					dataIndex: 'action',
					header: WT.res(WT.ID + '.admin', 'wtadmsubjectpermissiongrid.action.lbl'),
					flex: 1
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'far fa-trash-alt',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var sto = g.getStore(),
									rec = sto.getAt(ridx);
								sto.remove(rec);
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
		delete me.picker;
		me.callParent();
	},
	
	privates: {
		showPicker: function() {
			var me = this;
			//var me = this,
			//	usedSubjects = Sonicle.Data.collectValues(me.getStore());
			me.picker = me.createPicker();
			//me.picker.getComponent(0).setSkipValues(usedSubjects);
			me.picker.show();
		},
		
		createPicker: function() {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: WT.res(WT.ID + '.admin', 'wtadmsubjectpermissiongrid.picker.tit'),
				width: 500,
				items: [
					{
						xtype: 'wtadmpermissionpicker',
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
		
		onPickerPick: function(s, serviceId, context, action) {
			var me = this,
				sto = me.getStore();
			sto.add(me.recordCreatorFn(serviceId, context, action));
			
			me.fireEvent('pick', me, serviceId, context, action);
			me.picker.close();
			me.picker = null;
		}
	}
});
