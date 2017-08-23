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
Ext.define('Sonicle.webtop.core.ux.ChatMemberGrid', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtchatmembergrid',
	requires: [
		'Sonicle.picker.List',
		'Sonicle.grid.column.Lookup',
		'WTA.ux.PickerWindow',
		'Sonicle.webtop.core.model.ChatMemberLkp'
	],
	
	/*
	 * @private
	 */
	lookupStore: null,
	
	initComponent: function() {
		var me = this;
		
		me.lookupStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'Sonicle.webtop.core.model.ChatMemberLkp',
			proxy: WTF.proxy(WT.ID, 'LookupChatMembers', 'data')
		});
		
		me.selModel = {
			type: 'rowmodel'
		};
		
		if (!me.viewConfig) {
			me.viewConfig = {
				deferEmptyText: false,
				emptyText: WT.res(WT.ID, 'wtchatmembergrid.emp')
			};
		}
		
		me.hideHeaders = true;
		me.columns = [{
			xtype: 'solookupcolumn',
			dataIndex: 'friendId',
			store: me.lookupStore,
			displayField: 'label',
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
	
	createPicker: function () {
		var me = this;
		return Ext.create({
			xtype: 'wtpickerwindow',
			title: WT.res(WT.ID, 'wtchatmembergrid.picker.tit'),
			height: 350,
			items: [{
				xtype: 'solistpicker',
				store: me.lookupStore,
				enableGrouping: false,
				valueField: 'id',
				displayField: 'desc',
				searchField: 'desc',
				emptyText: WT.res('grid.emp'),
				searchText: WT.res('textfield.search.emp'),
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
