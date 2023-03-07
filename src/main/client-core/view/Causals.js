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
Ext.define('Sonicle.webtop.core.view.Causals', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Lookup',
		'Sonicle.webtop.core.model.SubjectLkp',
		'Sonicle.webtop.core.model.CausalGrid'
	],
	uses: [
		'Sonicle.webtop.core.view.Causal'
	],
	
	dockableConfig: {
		title: '{causals.tit}',
		iconCls: 'wt-icon-causal',
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
				model: 'Sonicle.webtop.core.model.CausalGrid',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageCausals')
			},
			columns: [{
				xtype: 'rownumberer'	
			}, {
				xtype: 'soiconcolumn',
				dataIndex: 'causalId',
				getIconCls: function(v,rec) {
					return rec.get('readOnly') ? 'wt-icon-lock' : null;
				},
				getTip: function(v,rec) {
					return rec.get('readOnly') ? me.mys.res('causals.gp.readOnly.true') : null;
				},
				iconSize: WTU.imgSizeToPx('xs'),
				header: '',
				width: 30
			}, {
				dataIndex: 'description',
				header: me.mys.res('causals.gp.description.lbl'),
				flex: 2
			}, {
				xtype: 'solookupcolumn',
				dataIndex: 'userId',
				header: me.mys.res('causals.gp.user.lbl'),
				store: {
					autoLoad: true,
					model: 'WTA.model.SubjectLkp',
					proxy: WTF.proxy(me.mys.ID, 'LookupSubjects', null, {
						extraParams: {
							users: true,
							wildcard: true
						}
					})
				},
				displayField: 'labelNameWithDN',
				flex: 2
			}, {
				dataIndex: 'masterDataDescription',
				header: me.mys.res('causals.gp.masterData.lbl'),
				flex: 2
			}, {
				dataIndex: 'externalId',
				header: me.mys.res('causals.gp.externalId.lbl'),
				flex: 1
			}],
			tbar: [
				me.addAct('add', {
					text: WT.res('act-add.lbl'),
					iconCls: 'wt-icon-add',
					handler: function() {
						me.addCausal();
					}
				}),
				me.addAct('delete', {
					text: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete',
					disabled: true,
					handler: function() {
						var sm = me.lref('gp').getSelectionModel();
						me.deleteCausal(sm.getSelection()[0]);
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
					me.editCausal(rec.get('causalId'));
				}
			}
		});
		
		me.getViewModel().bind({
			bindTo: '{gp.selection}'
		}, function(sel) {
			me.getAct('delete').setDisabled((sel) ? false : true);
		});
	},
	
	addCausal: function() {
		var me = this,
				vct = WT.createView(me.mys.ID, 'view.Causal');
		
		vct.getComponent(0).on('viewsave', me.onCausalViewSave, me);
		vct.show(false, function() {
			vct.getComponent(0).beginNew({
				data: {
					userId: '*'
				}
			});
		});
	},
	
	editCausal: function(id) {
		var me = this,
				vct = WT.createView(me.mys.ID, 'view.Causal');
		
		vct.getComponent(0).on('viewsave', me.onCausalViewSave, me);
		vct.show(false, function() {
			vct.getComponent(0).beginEdit({
				data: {
					causalId: id
				}
			});
		});
	},
	
	deleteCausal: function(rec) {
		var me = this,
				grid = me.lref('gp'),
				sto = grid.getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				sto.remove(rec);
			}
		}, me);
	},
	
	onCausalViewSave: function(s) {
		this.lref('gp').getStore().load();
	}
});
