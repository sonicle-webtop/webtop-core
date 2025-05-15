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
Ext.define('Sonicle.webtop.core.sdk.FolderSharingView', {
	alternateClassName: 'WTA.sdk.FolderSharingView',
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.Data',
		'Sonicle.form.field.ComboBox',
		'WTA.model.AclSubjectLkp'
	],
	
	dockableConfig: {
		title: '{folderSharing.tit@com.sonicle.webtop.core}',
		iconCls: 'wt-icon-sharing',
		width: 500,
		height: 480
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
			isOrigin: {
				bind: {bindTo: '{record.type}'},
				get: function(val) {
					return val === 'O';
				}
			},
			wildcard: {
				bind: {bindTo: '{record.type}'},
				get: function(val) {
					return (val === 'O') ? '*' : '';
				}
			}
		}
	},
	focusField: 'fldsubject',
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.aclSubjectStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.AclSubjectLkp',
			proxy: WTF.proxy(WT.ID, 'LookupAclSubjects', null, {
				extraParams: {
					users: true,
					groups: true
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			layout: 'fit',
			items: [
				{
					xtype: 'gridpanel',
					reference: 'gprights',
					bind: {
						store: '{record.rights}'
					},
					border: true,
					columns: [
						{
							xtype: 'solookupcolumn',
							dataIndex: 'subjectSid',
							store: me.aclSubjectStore,
							displayField: 'name',
							header: WT.res('folderSharing.gp-rights.subjectName.lbl'),
							flex: 1
						}, {
							xtype: 'solookupcolumn',
							dataIndex: 'subjectSid',
							store: me.aclSubjectStore,
							displayField: 'desc',
							header: WT.res('folderSharing.gp-rights.subjectDisplayName.lbl'),
							flex: 1
						}, {
							xtype: 'soactioncolumn',
							items: [
								{
									iconCls: 'far fa-clone',
									tooltip: WT.res('act-clone.lbl'),
									handler: function(g, ridx) {
										var rec = g.getStore().getAt(ridx);
										me.cloneRightsUI(rec);
									}
								}, {
									iconCls: 'far fa-trash-alt',
									tooltip: WT.res('act-remove.lbl'),
									handler: function(g, ridx) {
										var rec = g.getStore().getAt(ridx);
										me.deleteRightsUI(rec);
									}
								}
							]
						}
					],
					tbar: [
						{
							text: WT.res('act-add.lbl'),
							ui: '{secondary|toolbar}',
							handler: function() {
								me.addRightsUI();
							}
						}
					],
					listeners: {
						selectionchange: function(s,recs) {
							me.lref('itemsrights').setDisabled(!recs.length);
							me.lref('folderrights').setDisabled(!recs.length);
							me.lref('originrights').setDisabled(!recs.length);
						}
					}
				}
			]
		});
		me.add({
			region: 'south',
			xtype: 'wtfieldspanel',
			paddingTop: true,
			paddingSides: true,
			height: 170,
			defaults: {
				labelWidth: 160
			},
			items: [
				{
					xtype: 'fieldcontainer',
					reference: 'itemsrights',
					bind: {
						fieldLabel: WT.res('folderSharing.itemsRights.lbl') + '{wildcard}'
					},
					disabled: true,
					layout: 'hbox',
					items: [{
						xtype: 'checkbox',
						bind: '{gprights.selection.itemsCreate}',
						boxLabel: WT.res('folderSharing.fld-itemsCreate.lbl'),
						width: 100
					}, {
						xtype: 'checkbox',
						bind: '{gprights.selection.itemsUpdate}',
						boxLabel: WT.res('folderSharing.fld-itemsUpdate.lbl'),
						width: 100
					}, {
						xtype: 'checkbox',
						bind: '{gprights.selection.itemsDelete}',
						boxLabel: WT.res('folderSharing.fld-itemsDelete.lbl')
					}
				]
			}, {
				xtype: 'fieldcontainer',
				reference: 'folderrights',
				bind: {
					fieldLabel: WT.res('folderSharing.folderRights.lbl') + '{wildcard}'
				},
				disabled: true,
				layout: 'hbox',
				items: [
					{
						xtype: 'checkbox',
						bind: '{gprights.selection.folderRead}',
						boxLabel: WT.res('folderSharing.fld-folderRead.lbl'),
						width: 100
					}, {
						xtype: 'checkbox',
						bind: '{gprights.selection.folderUpdate}',
						boxLabel: WT.res('folderSharing.fld-folderUpdate.lbl'),
						width: 100
					}, {
						xtype: 'checkbox',
						bind: '{gprights.selection.folderDelete}',
						boxLabel: WT.res('folderSharing.fld-folderDelete.lbl')
					}
				]
			}, {
				xtype: 'fieldcontainer',
				reference: 'originrights',
				bind: {
					hidden: '{!isOrigin}'
				},
				disabled: true,
				layout: 'hbox',
				fieldLabel: WT.res('folderSharing.originRights.lbl'),
				items: [
					{
						xtype: 'checkbox',
						bind: '{gprights.selection.folderManage}',
						boxLabel: WT.res('folderSharing.fld-folderManage.lbl')
					}
				]
			}]
		});
	},
	
	doDestroy: function() {
		var me = this;
		delete me.subjectPicker;
		delete me.aclSubjectStore;
		me.callParent();
	},
	
	addRightsUI: function() {
		this.showSubjectPicker();
	},
	
	deleteRightsUI: function(rec) {
		var me = this,
			sto = me.lref('gprights').getStore();
		sto.remove(rec);
	},
	
	cloneRightsUI: function(rec) {
		var me = this;
		me.cloneRec = rec;
		me.showSubjectPicker();
	},
	
	privates: {
		addRights: function(subjectSid, cloneRec) {
			var me = this,
				grid = me.lref('gprights'),
				sto = grid.getStore(),
				data, rec;
			
			if (sto.indexOfId(subjectSid) !== -1) return null;
			if (cloneRec) data = Sonicle.Object.remap(cloneRec.getData(), ['folderManage', 'folderRead', 'folderUpdate', 'folderDelete', 'itemsCreate', 'itemsUpdate', 'itemsDelete']);
			rec = sto.add(Ext.apply(data || {}, {
					subjectSid: subjectSid
				}, {
					folderRead: true
			}))[0];
			return rec;
		},
		
		showSubjectPicker: function() {
			var me = this,
				usedSubjects = Sonicle.Data.collectValues(me.lref('gprights').getStore());
			me.subjectPicker = me.createSubjectPicker();
			me.subjectPicker.getComponent(0).setSkipValues(usedSubjects);
			me.subjectPicker.show();
		},
		
		createSubjectPicker: function() {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: WT.res(WT.ID, 'folderSharing.subjectPicker.tit'),
				height: 350,
				items: [
					{
						xtype: 'solistpicker',
						store: {
							xclass: 'Ext.data.ChainedStore',
							source: me.aclSubjectStore
						},
						valueField: 'id',
						displayField: 'name',
						searchField: 'search',
						emptyText: WT.res('grid.emp'),
						searchText: WT.res('textfield.search.emp'),
						selectedText: WT.res('grid.selected.lbl'),
						okText: WT.res('act-ok.lbl'),
						cancelText: WT.res('act-cancel.lbl'),
						allowMultiSelection: true,
						listeners: {
							cancelclick: function() {
								if (me.subjectPicker) me.subjectPicker.close();
							}
						},
						handler: me.onSubjectPickerPick,
						scope: me
					}
				]
			});
		},
		
		onSubjectPickerPick: function(s, values, recs, button) {
			var me = this, lastRec;
			Ext.iterate(values, function(value) {
				lastRec = me.addRights(value, me.cloneRec);
			});
			delete me.cloneRec;
			me.subjectPicker.close();
			me.subjectPicker = null;
			if (lastRec) me.lref('gprights').setSelection(lastRec);
		}
		
		/*
		refreshSubjectFieldFilter: function() {
			var me = this,
				usedSubjects = Sonicle.Data.collectValues(me.lref('gprights').getStore()),
				combo = me.lref('fldsubject'),
				filters = combo.getStore().getFilters(),
				fi = filters.getByKey('used-skip'),
				buildFilterFn = function(valuesToSkip) {
					return function(rec) {
						return valuesToSkip.indexOf(rec.getId()) === -1;
					};
				};
			
			filters.beginUpdate();
			if (fi) filters.remove(fi);
			if (Ext.isArray(usedSubjects)) {
				filters.add(new Ext.util.Filter({
					id: 'used-skip',
					filterFn: buildFilterFn(usedSubjects)
				}));
			}
			filters.endUpdate();
		}
		*/
	}
});
