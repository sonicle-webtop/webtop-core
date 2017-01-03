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
Ext.define('Sonicle.webtop.core.sdk.Sharing', {
	alternateClassName: 'WTA.sdk.Sharing',
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.webtop.core.model.RoleLkp',
		'Sonicle.form.field.SourceComboBox'
	],
	
	dockableConfig: {
		title: '{sharing.tit@com.sonicle.webtop.core}',
		iconCls: 'wt-icon-sharing-xs',
		width: 450,
		height: 380
	},
	modeTitle: false,
	
	/**
	 * @cfg {String} modelName
	 * Please provide a value for this config.
	 * For more info see {@link WTA.sdk.ModelView#modelName}.
	 */
	
	/**
	 * @cfg {String} fieldTitle
	 * Please provide a value for this config.
	 * For more info see {@link WTA.sdk.ModelView#fieldTitle}.
	 */
	
	viewModel: {
		formulas: {
			isRoot: {
				bind: {bindTo: '{record.level}'},
				get: function(val) {
					return val === 0;
				}
			},
			asterisk: {
				bind: {bindTo: '{record.level}'},
				get: function(val) {
					return (val === 0) ? '*' : '';
				}
			}
		}
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'north',
			xtype: 'wtfieldspanel',
			height: 30,
			items: [
				WTF.localCombo('id', 'desc', {
					xtype: 'sosourcecombo',
					reference: 'fldrole',
					store: {
						autoLoad: true,
						model: 'WTA.model.RoleLkp',
						proxy: WTF.proxy(WT.ID, 'LookupDomainRoles', 'roles')
					},
					sourceField: 'sourceLabel',
					anchor: '100%',
					emptyText: WT.res('sharing.fld-role.lbl'),
					listeners: {
						select: function(s, rec) {
							var model = me.addRights(rec.get('id'));
							me.lref('gprights').setSelection(model);
							s.setValue(null);
						}
					}
				})]
		});
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			layout: 'fit',
			items: [{
				xtype: 'gridpanel',
				reference: 'gprights',
				bind: {
					store: '{record.rights}'
				},
				border: true,
				columns: [{
					xtype: 'solookupcolumn',
					dataIndex: 'roleUid',
					store: {
						autoLoad: true,
						model: 'WTA.ux.data.SimpleModel',
						proxy: WTF.proxy(WT.ID, 'LookupDomainRoles', 'roles')
					},
					displayField: 'desc',
					header: WT.res('sharing.gp-rights.role.lbl'),
					flex: 1
				}],
				tbar: [
					me.addAction('deleteRights', {
						text: WT.res('act-delete.lbl'),
						tooltip: null,
						iconCls: 'wt-icon-delete-xs',
						handler: function() {
							var sm = me.lref('gprights').getSelectionModel();
							me.deleteRights(sm.getSelection());
						},
						disabled: true
					})
				],
				listeners: {
					selectionchange: function(s,recs) {
						me.getAction('deleteRights').setDisabled(!recs.length);
						me.lref('elementsperms').setDisabled(!recs.length);
						me.lref('folderperms').setDisabled(!recs.length);
						me.lref('rootperms').setDisabled(!recs.length);
					}
				}
			}]
		});
		me.add({
			region: 'south',
			xtype: 'wtfieldspanel',
			height: 100,
			defaults: {
				labelWidth: 140
			},
			items: [{
				xtype: 'fieldcontainer',
				reference: 'elementsperms',
				bind: {
					fieldLabel: WT.res('sharing.elementsrights.lbl') + '{asterisk}'
				},
				disabled: true,
				layout: 'hbox',
				items: [{
					xtype: 'checkbox',
					bind: '{gprights.selection.elementsCreate}',
					boxLabel: WT.res('sharing.fld-elementsCreate.lbl'),
					width: 100
				}, {
					xtype: 'checkbox',
					bind: '{gprights.selection.elementsUpdate}',
					boxLabel: WT.res('sharing.fld-elementsUpdate.lbl'),
					width: 100
				}, {
					xtype: 'checkbox',
					bind: '{gprights.selection.elementsDelete}',
					boxLabel: WT.res('sharing.fld-elementsDelete.lbl')
				}]
			}, {
				xtype: 'fieldcontainer',
				reference: 'folderperms',
				bind: {
					fieldLabel: WT.res('sharing.folderrights.lbl') + '{asterisk}'
				},
				disabled: true,
				layout: 'hbox',
				items: [{
					xtype: 'checkbox',
					bind: '{gprights.selection.folderRead}',
					boxLabel: WT.res('sharing.fld-folderRead.lbl'),
					width: 100
				}, {
					xtype: 'checkbox',
					bind: '{gprights.selection.folderUpdate}',
					boxLabel: WT.res('sharing.fld-folderUpdate.lbl'),
					width: 100
				}, {
					xtype: 'checkbox',
					bind: '{gprights.selection.folderDelete}',
					boxLabel: WT.res('sharing.fld-folderDelete.lbl')
				}]
			}, {
				xtype: 'fieldcontainer',
				reference: 'rootperms',
				bind: {
					hidden: '{!isRoot}'
				},
				disabled: true,
				layout: 'hbox',
				fieldLabel: WT.res('sharing.rootrights.lbl'),
				items: [{
					xtype: 'checkbox',
					bind: '{gprights.selection.rootManage}',
					boxLabel: WT.res('sharing.fld-rootManage.lbl')
				}]
			}]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		me.lref('fldrole').focus(true);
	},
	
	addRights: function(roleUid) {
		var me = this,
				grid = me.lref('gprights'),
				sto = grid.getStore(),
				rec;
		
		if(sto.indexOfId(roleUid) !== -1) return null;
		/*
		rec = sto.createModel({
			roleUid: roleUid,
			folderRead: true
		});
		sto.add(rec);
		*/
		rec = sto.add({
			roleUid: roleUid,
			folderRead: true
		})[0];
		return rec;
	},
	
	deleteRights: function(rec) {
		var me = this,
				grid = me.lref('gprights');
		
		grid.getStore().remove(rec);
	}
});
