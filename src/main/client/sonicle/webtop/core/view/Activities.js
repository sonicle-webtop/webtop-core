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
	extend: 'WT.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.model.ActivityGrid',
		'Sonicle.webtop.core.view.Activity'
	],
	
	dockableConfig: {
		title: '@activities.tit',
		iconCls: 'wt-icon-activity-xs',
		width: 600,
		height: 400
	},
	promptConfirm: false,
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			tbar: [
				me.addAction('add', {
					text: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						me.addActivity(WT.getOption('domainId'));
					}
				}),
				me.addAction('remove', {
					text: WT.res('act-remove.lbl'),
					iconCls: 'wt-icon-remove-xs',
					disabled: true,
					handler: function() {
						var sm = me.lookupReference('gp').getSelectionModel();
						me.deleteActivity(sm.getSelection());
					}
				})
			]
		});
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
				proxy: WTF.apiProxy(me.mys.ID, 'ManageActivities', 'activities')
			},
			columns: [{
				xtype: 'rownumberer'	
			}, {
				dataIndex: 'activityId',
				renderer: WTF.iconColRenderer({
					iconField: function(rec) {
						return (rec.get('readOnly')) ? ['activity-ro', me.mys.res('activities.gp.readOnly.true')] : null;
					},
					xid: me.mys.XID,
					size: 'xs'
				}),
				header: '',
				width: 30
			}, {
				dataIndex: 'description',
				header: me.mys.res('activities.gp.description.lbl'),
				width: 200
			}, {
				dataIndex: 'externalId',
				header: me.mys.res('activities.gp.externalId.lbl'),
				width: 100
			}, {
				xtype: 'templatecolumn',
				header: me.mys.res('activities.gp.user.lbl'),
				flex: 1,
				tpl: '{userDescription} ({userId})'
			}, {
				xtype: 'templatecolumn',
				header: me.mys.res('activities.gp.domain.lbl'),
				flex: 1,
				hidden: true,
				tpl: '{domainDescription} ({domainId})'
			}],
			listeners: {
				rowdblclick: function(s, rec) {
					me.editActivity(rec.get('activityId'));
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function(sel) {
			me.getAction('remove').setDisabled((sel) ? false : true);
		});
	},
	
	addActivity: function(domainId) {
		var me = this,
				vwc = this._createActivityView();
		
		vwc.getComponent(0).on('viewsave', me.onActivityViewSave, me);
		vwc.show(false, function() {
			vwc.getComponent(0).beginNew({
				data: {
					domainId: domainId,
					userId: '*'
				}
			});
		});
	},
	
	editActivity: function(id) {
		var me = this,
				vw = this._createActivityView();
		
		vw.getComponent(0).on('viewsave', me.onActivityViewSave, me);
		vw.show(false, function() {
			vw.getComponent(0).beginEdit({
				data: {
					activityId: id
				}
			});
		});
	},
	
	deleteActivity: function(rec) {
		var me = this,
				grid = me.lookupReference('gp'),
				sto = grid.getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				sto.remove(rec);
			}
		}, me);
	},
	
	onActivityViewSave: function(s) {
		this.lookupReference('gp').getStore().load();
	},
	
	_createActivityView: function(cfg) {
		return WT.createView(this.mys.ID, 'view.Activity', {
			viewCfg: cfg,
			modal: true
		});
	}
});
