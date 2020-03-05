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
Ext.define('Sonicle.webtop.core.admin.view.Settings', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.menu.StoreMenu',
		'WTA.ux.grid.Setting',
		'Sonicle.webtop.core.model.ServiceLkp',
		'Sonicle.webtop.core.admin.model.SystemSetting'
	],
	
	dockableConfig: {
		title: '{settings.tit}',
		iconCls: 'wtadm-icon-settings'
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtsettinggrid',
			reference: 'gp',
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.core.admin.model.SystemSetting',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageSystemSettings', null, {
					writer: {
						allowSingle: false // Always wraps records into an array
					}
				}),
				groupField: 'serviceId',
				listeners: {
					remove: function(s, recs) {
						// Fix for updating selection
						me.lref('gp').getSelectionModel().deselect(recs);
					}
				}
			},
			tbar: [{
					xtype: 'splitbutton',
					text: me.mys.res('settings.act-add.lbl'),
					iconCls: 'wt-icon-add-xs',
					handler: function(s) {
						s.maybeShowMenu();
					},
					menu: {
						xtype: 'sostoremenu',
						store: {
							autoLoad: true,
							model: 'Sonicle.webtop.core.model.ServiceLkp',
							proxy: WTF.proxy(WT.ID, 'LookupServices')
						},
						textField: 'label',
						listeners: {
							click: function(s,itm) {
								me.addSettingUI(itm.getItemId());
							}
						}
					}
				},
				me.addAct('remove', {
					text: WT.res('act-remove.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-remove-xs',
					disabled: true,
					handler: function() {
						var rec = me.lref('gp').getSelection()[0];
						if(rec) me.deleteSettingUI(rec);
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
			]
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function(sel) {
			me.getAct('remove').setDisabled((sel) ? false : true);
		});
	},
	
	addSettingUI: function(serviceId) {
		var gp = this.lref('gp'),
				ed = gp.findPlugin('cellediting'),
				sto = gp.getStore(),
				indx = sto.findExact('serviceId', serviceId),
				rec;
		if (indx < 0) indx = 0;
		ed.cancelEdit();
		rec = sto.insert(indx, sto.createModel({
			serviceId: serviceId,
			key: null,
			value: null
		}))[0];
		ed.startEditByPosition({row: sto.indexOf(rec), column: gp.keyColumn.getIndex()});
	},
	
	deleteSettingUI: function(rec) {
		var me = this,
				sto = me.lref('gp').getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if (bid === 'yes') sto.remove(rec);
		}, me);
	}
});
