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
		data: {
			data: {
				preset: null
			}
		}
	},
	focusField: 'fldsubject',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foIsOrigin: WTF.foIsEqual('record', 'type', 'O'),
			foFolderRightsLabel: WTF.foGetFn('record', 'type', function(v) {
				var wildcard = (v === 'O') ? '.root' : '';
				return WT.res('folderSharing.folderRights'+wildcard+'.lbl');
			}),
			foItemsRightsLabel: WTF.foGetFn('record', 'type', function(v) {
				var wildcard = (v === 'O') ? '.root' : '';
				return WT.res('folderSharing.itemsRights'+wildcard+'.lbl');
			}),
			foShowOriginRights: WTF.foMultiGetFn(undefined, ['gprights.selection', 'data.preset', 'foIsOrigin'], function(v) {
				if (!v['gprights.selection'] || v['data.preset'] !== 'custom') return false;
				return !!v['foIsOrigin'];
			}),
			foShowFolderRights: WTF.foMultiGetFn(undefined, ['gprights.selection', 'data.preset'], function(v) {
				if (!v['gprights.selection'] || v['data.preset'] !== 'custom') return false;
				return true;
			}),
			foShowItemsRights: WTF.foMultiGetFn(undefined, ['gprights.selection', 'data.preset'], function(v) {
				if (!v['gprights.selection'] || v['data.preset'] !== 'custom') return false;
				return true;
			})
		});
	},
	
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
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'wtfieldspanel',
					paddingTop: true,
					paddingSides: true,
					flex: 1,
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
									text: WT.res('folderSharing.gp-rights.subjectName.lbl'),
									flex: 1
								}, {
									xtype: 'solookupcolumn',
									dataIndex: 'subjectSid',
									store: me.aclSubjectStore,
									displayField: 'desc',
									text: WT.res('folderSharing.gp-rights.subjectDisplayName.lbl'),
									flex: 1
								}, {
									xtype: 'soactioncolumn',
									items: [
										{
											iconCls: 'wt-glyph-clone',
											tooltip: WT.res('act-clone.lbl'),
											handler: function(g, ridx) {
												var rec = g.getStore().getAt(ridx);
												me.cloneRightsUI(rec);
											}
										}, {
											iconCls: 'wt-glyph-delete',
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
								selectionchange: function(s, recs) {
									if (!Ext.isEmpty(recs)) {
										me.getVM().set('data.preset', recs[0].guessPreset());
									}
								}
							}
						}
					]
				}, {
					xtype: 'wtfieldspanel',
					paddingTop: true,
					paddingSides: true,
					defaults: {
						labelWidth: 140
					},
					items: [
						{
							xtype: 'fieldcontainer',
							reference: 'itemsrights',
							bind: {
								hidden: '{!foShowItemsRights}',
								fieldLabel: '{foItemsRightsLabel}'
							},
							layout: 'hbox',
							defaults: {
								width: 100
							},
							items: [
								{
									xtype: 'checkbox',
									bind: '{gprights.selection.itemsCreate}',
									boxLabel: WT.res('folderSharing.fld-itemsCreate.lbl')
								}, {
									xtype: 'checkbox',
									bind: '{gprights.selection.itemsUpdate}',
									boxLabel: WT.res('folderSharing.fld-itemsUpdate.lbl')
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
								hidden: '{!foShowFolderRights}',
								fieldLabel: '{foFolderRightsLabel}'
							},
							layout: 'hbox',
							defaults: {
								width: 100
							},
							items: [
								{
									xtype: 'checkbox',
									bind: '{gprights.selection.folderRead}',
									boxLabel: WT.res('folderSharing.fld-folderRead.lbl')
								}, {
									xtype: 'checkbox',
									bind: '{gprights.selection.folderUpdate}',
									boxLabel: WT.res('folderSharing.fld-folderUpdate.lbl')
								}, {
									xtype: 'checkbox',
									bind: '{gprights.selection.folderDelete}',
									boxLabel: WT.res('folderSharing.fld-folderDelete.lbl')
								}, {
									xtype: 'checkbox',
									bind: {
										value: '{gprights.selection.folderManage}',
										hidden: '{!foShowOriginRights}'
									},
									boxLabel: WT.res('folderSharing.fld-folderManage.lbl')
								}
							]
						}
					]
				}, {
					xtype: 'wtfieldspanel',
					paddingSides: true,
					defaults: {
						labelWidth: 140
					},
					items: [
						WTF.lookupCombo('id', 'desc', {
							reference: 'cbopresets',
							bind: {
								value: '{data.preset}',
								disabled: '{!gprights.selection}'
							},
							store: {
								type: 'array',
								autoLoad: true,
								fields: [
									{name: 'id', type: 'string'},
									{name: 'desc', type: 'string'},
									{name: 'info', type: 'string'}
								],
								data: [
									['ro', WT.res('folderSharing.cbo-presets.ro.lbl'), WT.res('folderSharing.cbo-presets.ro.tip')],
									['rw', WT.res('folderSharing.cbo-presets.rw.lbl'), WT.res('folderSharing.cbo-presets.rw.tip')],
									['full', WT.res('folderSharing.cbo-presets.full.lbl'), WT.res('folderSharing.cbo-presets.full.tip')],
									// admin item will be filtered out in non root sharing (see viewload)...
									['admin', WT.res('folderSharing.cbo-presets.admin.lbl'), WT.res('folderSharing.cbo-presets.admin.tip')],
									['custom', WT.res('folderSharing.cbo-presets.custom.lbl'), WT.res('folderSharing.cbo-presets.custom.tip')]
								]
							},
							matchFieldWidth: false,
							listConfig: {
								getInnerTpl: function(displayField) {
									return '{'+displayField+'}</br>'
										+ '<span class="wt-text-off wt-theme-text-color-off">{info}</span>';
								},
								width: 400
							},
							fieldLabel: WT.res('folderSharing.cbo-presets.lbl'),
							listeners: {
								select: function(s, rec) {
									var preset = rec.get('id'), recs;
									if ('custom' !== preset) {
										recs = me.lref('gprights').getSelection();
										if (!Ext.isEmpty(recs)) {
											recs[0].applyPreset(preset);
										}
									}
								}
							},
							width: 300
						})
					]
				}
			]
		});
		me.on('viewload', me.onViewLoad);
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
		onViewLoad: function(s, success) {
			var me = this,
				mo = me.getModel(),
				cbo;
			
			if (success) {
				if (mo.get('type') !== 'O') {
					cbo = me.lref('cbopresets');
					if (cbo) {
						cbo.getStore().addFilter(function(item) {
							return item.get('id') !== 'admin';
						});
					}
				}
			}
		},
		
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
