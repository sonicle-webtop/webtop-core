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
Ext.define('Sonicle.webtop.core.admin.ux.RolePermissionGrid', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtadmrolepermissiongrid',
	requires: [
		'Sonicle.webtop.core.admin.ux.PermissionPicker'
	],
	
	initComponent: function() {
		var me = this;
		
		me.selModel = {
			type: 'rowmodel'
		};
		
		if(!me.viewConfig) {
			me.viewConfig = {
				deferEmptyText: false,
				emptyText: WT.res(WT.ID + '.admin', 'wtadmrolepermissiongrid.emp')
			};
		}
		
		if(!me.columns) {
			//me.hideHeaders = false;
			me.columns = [{
				dataIndex: 'serviceId',
				header: WT.res(WT.ID + '.admin', 'wtadmpermissionpicker.serviceId.lbl'),
				flex: 1
			}, {
				dataIndex: 'groupName',
				header: WT.res(WT.ID + '.admin', 'wtadmpermissionpicker.groupName.lbl'),
				flex: 1
			}, {
				dataIndex: 'action',
				header: WT.res(WT.ID + '.admin', 'wtadmpermissionpicker.action.lbl'),
				flex: 1
			}];
		}
		
		me.initActions();
		me.tbar = me.tbar || [];
		me.tbar.push(
			me.addAct,
			me.removeAction
		);
		
		me.callParent(arguments);
		me.on('selectionchange', me.onSelectionChange, me);
	},
	
	initActions: function() {
		var me = this;
		me.addAct = new Ext.Action({
			text: WT.res('act-add.lbl'),
			iconCls: 'wt-icon-add',
			handler: me.onAddClick,
			scope: me
		});
		me.removeAction = new Ext.Action({
			text: WT.res('act-remove.lbl'),
			iconCls: 'wt-icon-remove',
			disabled: true,
			handler: me.onRemoveClick,
			scope: me
		});
	},
	
	createPicker: function() {
		var me = this;
		return Ext.create({
			xtype: 'wtpickerwindow',
			title: WT.res(WT.ID + '.admin', 'wtadmrolepermissiongrid.picker.tit'),
			width: 500,
			items: [{
				xtype: 'wtadmpermissionpicker',
				listeners: {
					cancelclick: function() {
						if(me.picker) me.picker.close();
					}
				},
				handler: me.onPickerPick,
				scope: me
			}]
		});
	},
	
	bindStore: function(store, initial) {
		var me = this;
		me.callParent(arguments);
		if(store) {
			store.on('remove', me.onStoreRemove, me);
		}
	},
	
	unbindStore: function() {
		var me = this;
		if(me.store) {
			me.store.un('remove', me.onStoreRemove, me);
		}
		me.callParent(arguments);
	},
	
	privates: {
		onSelectionChange: function(s, sel) {
			this.removeAction.setDisabled(sel.length === 0);
		},
		
		onStoreRemove: function(s, recs) {
			this.getSelectionModel().deselect(recs); // Fix for updating selection
		},
		
		onAddClick: function() {
			var me = this;
			me.picker = me.createPicker();
			me.picker.show();
		},
		
		onRemoveClick: function() {
			var me = this,
					rec = me.getSelection()[0];
			if(rec) me.getStore().remove(rec);
		},
		
		onPickerPick: function(s, serviceId, groupName, action) {
			var me = this;
			me.fireEvent('pick', me, serviceId, groupName, action);
			me.picker.close();
			me.picker = null;
		}
	}
});
