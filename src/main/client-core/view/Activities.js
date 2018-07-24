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
Ext.define('Sonicle.webtop.core.view.Activities', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Lookup',
		'Sonicle.webtop.core.model.ActivityGrid'
	],
	uses: [
		'Sonicle.webtop.core.view.Activity'
	],
	
	dockableConfig: {
		title: '{activities.tit}',
		iconCls: 'wt-icon-activity-xs',
		width: 600,
		height: 400,
		modal: true
	},
	promptConfirm: false,
	
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
				model: 'Sonicle.webtop.core.model.ActivityGrid',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageActivities')
			},
			columns: [{
				xtype: 'rownumberer'	
			}, {
				xtype: 'soiconcolumn',
				dataIndex: 'activityId',
				getIconCls: function(v,rec) {
					return rec.get('readOnly') ? me.mys.cssIconCls('activity-ro', 'xs') : null;
				},
				getTip: function(v,rec) {
					return rec.get('readOnly') ? me.mys.res('activities.gp.readOnly.true') : null;
				},
				iconSize: WTU.imgSizeToPx('xs'),
				header: '',
				width: 30
			}, {
				dataIndex: 'description',
				header: me.mys.res('activities.gp.description.lbl'),
				flex: 2
			}, {
				xtype: 'solookupcolumn',
				dataIndex: 'userId',
				header: me.mys.res('activities.gp.user.lbl'),
				store: {
					autoLoad: true,
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(me.mys.ID, 'LookupDomainUsers', 'users', {
						extraParams: {wildcard: true}
					})
				},
				displayField: 'desc',
				flex: 2
			}, {
				dataIndex: 'externalId',
				header: me.mys.res('activities.gp.externalId.lbl'),
				width: 100
			}],
			tbar: [
				me.addAct('add', {
					text: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						me.addActivity();
					}
				}),
				me.addAct('remove', {
					text: WT.res('act-remove.lbl'),
					iconCls: 'wt-icon-remove-xs',
					disabled: true,
					handler: function() {
						var sm = me.lref('gp').getSelectionModel();
						me.deleteActivity(sm.getSelection()[0]);
					}
				}),
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
					me.editActivity(rec.get('activityId'));
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function(sel) {
			me.getAct('remove').setDisabled((sel) ? false : true);
		});
	},
	
	addActivity: function() {
		var me = this,
				vct = WT.createView(me.mys.ID, 'view.Activity');
		
		vct.getComponent(0).on('viewsave', me.onActivityViewSave, me);
		vct.show(false, function() {
			vct.getComponent(0).beginNew({
				data: {
					userId: '*'
				}
			});
		});
	},
	
	editActivity: function(id) {
		var me = this,
				vct = WT.createView(me.mys.ID, 'view.Activity');
		
		vct.getComponent(0).on('viewsave', me.onActivityViewSave, me);
		vct.show(false, function() {
			vct.getComponent(0).beginEdit({
				data: {
					activityId: id
				}
			});
		});
	},
	
	deleteActivity: function(rec) {
		var me = this,
				grid = me.lref('gp'),
				sto = grid.getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if (bid === 'yes') sto.remove(rec);
		}, me);
	},
	
	onActivityViewSave: function(s) {
		this.lref('gp').getStore().load();
	}
});
