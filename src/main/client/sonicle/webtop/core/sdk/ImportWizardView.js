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
Ext.define('Sonicle.webtop.core.sdk.ImportWizardView', {
	extend: 'WT.sdk.WizardView',
	requires: [
		'Sonicle.upload.Field',
		'WT.ux.data.ValueModel',
		'WT.ux.panel.Form',
		'WT.model.ImportMapping',
		'WT.store.TxtLineSeparator',
		'WT.store.TxtTextQualifier',
		'WT.store.TxtEncoding'
	],
	
	config: {
		textAction: 'ImportText',
		excelAction: 'ImportExcel'
	},
	
	dockableConfig: {
		title: 'ImportWizardView',
		iconCls: 'wt-icon-causal-xs',
		width: 480,
		height: 380
	},
	
	constructor: function(config) {
		var me = this;
		me.pages = {
			txt: ['upload','s1','s2','mappings','mode','end'],
			xls: ['upload','s1','mappings','mode','end'],
			xlsx: ['upload','s1','mappings','mode','end']
		};
		me.callParent([config]);
		me.on('beforenavigate', me.onBeforeNavigate);
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getVM();
		
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, {
			delgroup: WTF.radioGroupBind(null, 'delimiter', me.getId()+'-delgroup'),
			modegroup: WTF.radioGroupBind(null, 'importmode', me.getId()+'-modegroup')
		}));
		
		me.callParent(arguments);
	},
	
	initPathPage: function() {
		var me = this;
		me.getVM().set('path', 'txt');
		me.add(me.createPathPage(
			WT.res('importwiz.path.tit'), 
			WT.res('importwiz.path.fld-path.tit'), [
			{value: 'txt', label: WT.res('importwiz.path.fld-path.txt')},
			{value: 'xls', label: WT.res('importwiz.path.fld-path.xls')},
			{value: 'xlsx', label: WT.res('importwiz.path.fld-path.xlsx')}
		]));
		me.onNavigate('path');
	},
	
	createUploadPage: function(path, mimeTypes) {
		var me = this;
		return {
			itemId: 'upload',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('importwiz.upload.tit')
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				defaults: {
					labelWidth: 120
				},
				items: [{
					xtype: 'souploadfield',
					bind: '{file}',
					buttonConfig: {
						uploaderConfig: WTF.uploader(me.mys.ID, 'ImportWizard', {
							mimeTypes: mimeTypes,
							listeners: {
								uploadstarted: function(up) {
									me.wait();
								},
								uploadcomplete: function(up) {
									me.unwait();
								},
								uploaderror: function(up) {
									me.unwait();
								}
							}
						})
					},
					width: 400,
					buttonText: WT.res('importwiz.upload.fld-file.button.lbl'),
					fieldLabel: WT.res('importwiz.upload.fld-file.lbl'),
					allowBlank: false
				}]
			}]
		};
	},
	
	createMappingsPage: function(path, action) {
		var me = this;
		
		return {
			itemId: 'mappings',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('importwiz.mappings.tit')
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'gridpanel',
				reference: 'gp',
				store: {
					model: 'Sonicle.webtop.core.model.ImportMapping',
					proxy: WTF.proxy(me.mys.ID, action, 'mappings', {
						extraParams: {op: 'mappings'}
					}),
					listeners: {
						beforeload: function(s,op) {
							WTU.applyExtraParams(op.getProxy(), me.buildMappingsEP(path));
						}
					}
				},
				columns: [{
					dataIndex: 'target',
					header: WT.res('importwiz.mappings.gp.target.lbl'),
					editor: {
						readOnly: true
					},
					flex: 1
				}, {
					dataIndex: 'source',
					editor: Ext.create(WTF.localCombo('id', 'id', {
						allowBlank: false,
						store: {
							autoLoad: false,
							model: 'WT.ux.data.ValueModel',
							proxy: WTF.proxy(me.mys.ID, action, 'columns', {
								extraParams: {op: 'columns'}
							}),
							listeners: {
								beforeload: function(s,op) {
									WTU.applyExtraParams(op.getProxy(), me.buildMappingsEP(path));
								}
							}
						}
					})),
					header: WT.res('importwiz.mappings.gp.source.lbl'),
					flex: 1
				}],
				selModel: 'cellmodel',
				plugins: {
					ptype: 'cellediting',
					clicksToEdit: 1
				},
				border: true
			}]
		};
	},
	
	createModePage: function(path, fieldItems) {
		var me = this,
				items = [];
		
		Ext.iterate(fieldItems, function(obj,i) {
			items.push({
				name: me.getId()+'-modegroup',
				inputValue: obj.value,
				boxLabel: obj.label
			});
		});
		
		return {
			itemId: 'mode',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('importwiz.mode.tit')
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				items: [{
					xtype: 'fieldset',
					title: WT.res('importwiz.mode.fld-importmode.lbl'),
					items: [{
						xtype: 'radiogroup',
						bind: {
							value: '{modegroup}'
						},
						columns: 1,
						items: items
					}]
				}]
			}]
		};
	},
	
	createPages: function(path) {
		var me = this;
		if(path === 'txt') {
			me.getVM().set('delimiter', 'comma');
			me.getVM().set('lineseparator', 'crlf');
			me.getVM().set('textqualifier', null);
			me.getVM().set('encoding', 'UTF-8');
			me.getVM().set('headersrow', 1);
			me.getVM().set('firstdatarow', 2);
			me.getVM().set('lastdatarow', null);
			me.getVM().set('importmode', 'append');
			
			return [
				me.createUploadPage(path, [
					{title: 'Text files', extensions: 'csv,txt'}
				]),
				me.createMappingsPage(path, me.textAction),
				me.createModePage(path, [
					{value: 'append', label: WT.res('importwiz.mode.fld-importmode.append')},
					{value: 'copy', label: WT.res('importwiz.mode.fld-importmode.copy')}
				]),
				me.createEndPage(path),
				{
					itemId: 's1',
					xtype: 'wtwizardpage',
					items: [{
						xtype: 'label',
						html: WT.res('importwiz.txt.s1.tit')
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'wtform',
						defaults: {
							labelWidth: 120
						},
						items: [{
								xtype: 'fieldset',
								title: WT.res('importwiz.txt.s1.fld-delimiter.lbl'),
								items: [{
									xtype: 'radiogroup',
									bind: {
										value: '{delgroup}'
									},
									columns: 1,
									defaults: {
										name: me.getId()+'-delgroup'
									},
									items: [
										{inputValue: 'comma', boxLabel: WT.res('importwiz.txt.s1.fld-delimiter.comma')},
										{inputValue: 'semicolon', boxLabel: WT.res('importwiz.txt.s1.fld-delimiter.semicolon')},
										{inputValue: 'tab', boxLabel: WT.res('importwiz.txt.s1.fld-delimiter.tab')}
									]
								}]
							},
							WTF.lookupCombo('id', 'desc', {
								bind: '{lineseparator}',
								store: Ext.create('WT.store.TxtLineSeparator', {
									autoLoad: true
								}),
								width: 300,
								fieldLabel: WT.res('importwiz.txt.s1.fld-lineseparator.lbl'),
								allowBlank: false
							}),
							WTF.lookupCombo('id', 'desc', {
								bind: '{textqualifier}',
								store: Ext.create('WT.store.TxtTextQualifier', {
									autoLoad: true
								}),
								triggers: {
									clear: WTF.clearTrigger()
								},
								width: 220,
								fieldLabel: WT.res('importwiz.txt.s1.fld-textqualifier.lbl'),
								emptyText: WT.res('word.none.male')
							}),
							WTF.localCombo('id', 'desc', {
								bind: '{encoding}',
								store: Ext.create('WT.store.TxtEncoding', {
									autoLoad: true
								}),
								width: 300,
								fieldLabel: WT.res('importwiz.txt.s1.fld-encoding.lbl'),
								allowBlank: false
							})
						]
					}]
				}, {
					itemId: 's2',
					xtype: 'wtwizardpage',
					defaults: {
						labelWidth: 120
					},
					items: [{
						xtype: 'label',
						html: WT.res('importwiz.txt.s2.tit')
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'wtform',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'numberfield',
							bind: '{headersrow}',
							minValue: 1,
							width: 200,
							fieldLabel: WT.res('importwiz.txt.s2.fld-headersrow.lbl'),
							allowBlank: false
						}, {
							xtype: 'numberfield',
							bind: '{firstdatarow}',
							minValue: 1,
							width: 200,
							fieldLabel: WT.res('importwiz.txt.s2.fld-firstdatarow.lbl'),
							allowBlank: false
						}, {
							xtype: 'numberfield',
							bind: '{lastdatarow}',
							minValue: 1,
							width: 200,
							fieldLabel: WT.res('importwiz.txt.s2.fld-lastdatarow.lbl')
						}]
					}]
				}
			];
		} else if(path === 'xls' || path === 'xlsx') {
			me.getVM().set('sheet', null);
			me.getVM().set('headersrow', 1);
			me.getVM().set('firstdatarow', 2);
			me.getVM().set('lastdatarow', null);
			me.getVM().set('importmode', 'append');
			
			var mm = null;
			if(path === 'xls') {
				mm = {title: 'Excel files', extensions: 'xls'};
			} else {
				mm = {title: 'Excel files (2007 or later)', extensions: 'xlsx'};
			}
			
			return [
				me.createUploadPage(path, [mm]),
				me.createMappingsPage(path, me.excelAction),
				me.createModePage(path, [
					{value: 'append', label: WT.res('importwiz.mode.fld-importmode.append')},
					{value: 'copy', label: WT.res('importwiz.mode.fld-importmode.copy')}
				]),
				me.createEndPage(path),
				{
					itemId: 's1',
					xtype: 'wtwizardpage',
					items: [{
						xtype: 'label',
						html: WT.res('importwiz.xls.s1.tit')
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'wtform',
						defaults: {
							labelWidth: 120
						},
						items: [
							WTF.localCombo('id', 'id', {
								bind: '{sheet}',
								reference: 'fldsheet',
								store: {
									model: 'WT.ux.data.ValueModel',
									proxy: WTF.proxy(me.mys.ID, me.excelAction, 'sheets', {
										extraParams: {op: 'sheets'}
									}),
									listeners: {
										beforeload: function(s,op) {
											WTU.applyExtraParams(op.getProxy(), me.buildSheetsEP(path));
										}
									}
								},
								valueField: 'id',
								displayField: 'id',
								width: 300,
								fieldLabel: WT.res('importwiz.xls.s1.fld-sheet.lbl'),
								allowBlank: false
							}), {
								xtype: 'numberfield',
								bind: '{headersrow}',
								minValue: 1,
								width: 200,
								fieldLabel: WT.res('importwiz.xls.s1.fld-headersrow.lbl'),
								allowBlank: false
							}, {
								xtype: 'numberfield',
								bind: '{firstdatarow}',
								minValue: 1,
								width: 200,
								fieldLabel: WT.res('importwiz.xls.s1.fld-firstdatarow.lbl'),
								allowBlank: false
							}, {
								xtype: 'numberfield',
								bind: '{lastdatarow}',
								minValue: 1,
								width: 200,
								fieldLabel: WT.res('importwiz.xls.s1.fld-lastdatarow.lbl')
							}
						]
					}]
				}
			];
		}
	},
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if(dir === -1) return;
		var me = this,
				ret = true,
				vm = me.getVM(),
				path = vm.get('path'),
				ppcmp = me.getPageCmp(pp),
				npcmp = me.getPageCmp(np);
		
		if(path === 'txt') {
			if(pp === 'upload') {
				ret = ppcmp.down('wtform').isValid();
			} else if(pp === 's1') {
				ret = ppcmp.down('wtform').isValid();
			} else if(pp == 's2') {
				ret = ppcmp.down('wtform').isValid();
			}
			if(!ret) return false;
			
			if(np === 'mappings') {
				npcmp.lref('gp').getStore().load();
				npcmp.lref('gp').getColumns()[1].getEditor().getStore().load();
			}
			
		} else if(path === 'xls' || path === 'xlsx') {
			if(pp === 'upload') {
				ret = ppcmp.down('wtform').isValid();
			} else if(pp === 's1') {
				ret = ppcmp.down('wtform').isValid();
			}
			if(!ret) return false;
			
			if(np === 's1') {
				npcmp.lref('fldsheet').getStore().load();
			} else if(np === 'mappings') {
				npcmp.lref('gp').getStore().load();
				npcmp.lref('gp').getColumns()[1].getEditor().getStore().load();
			}
		}
		return true;
	},
	
	buildSheetsEP: function(path) {
		var vm = this.getVM();
		
		if(path === 'xls' || path === 'xlsx') {
			return {
				path: path,
				uploadId: vm.get('file'),
				headersRow: vm.get('headersrow'),
				firstDataRow: vm.get('firstdatarow'),
				lastDataRow: vm.get('lastdatarow')
			};
		}
	},
	
	buildMappingsEP: function(path) {
		var vm = this.getVM();
		
		if(path === 'txt') {
			return {
				path: path,
				uploadId: vm.get('file'),
				encoding: vm.get('encoding'),
				delimiter: vm.get('delimiter'),
				lineSeparator: vm.get('lineseparator'),
				textQualifier: vm.get('textqualifier'),
				headersRow: vm.get('headersrow'),
				firstDataRow: vm.get('firstdatarow'),
				lastDataRow: vm.get('lastdatarow')
			};
		} else if(path === 'xls' || path === 'xlsx') {
			return {
				path: path,
				uploadId: vm.get('file'),
				headersRow: vm.get('headersrow'),
				firstDataRow: vm.get('firstdatarow'),
				lastDataRow: vm.get('lastdatarow'),
				sheet: vm.get('sheet')
			};
		}
	},
	
	 buildDoEP: function(path) {
		var vm = this.getVM(),
				mappings = null;
		
		if(path === 'txt') {
			return {
				path: path,
				uploadId: vm.get('file'),
				encoding: vm.get('encoding'),
				delimiter: vm.get('delimiter'),
				lineSeparator: vm.get('lineseparator'),
				textQualifier: vm.get('textqualifier'),
				headersRow: vm.get('headersrow'),
				firstDataRow: vm.get('firstdatarow'),
				lastDataRow: vm.get('lastdatarow'),
				mappings: mappings,
				importMode: vm.get('importmode')
			};
		} else if(path === 'xls' || path === 'xlsx') {
			return {
				path: path,
				uploadId: vm.get('file'),
				headersRow: vm.get('headersrow'),
				firstDataRow: vm.get('firstdatarow'),
				lastDataRow: vm.get('lastdatarow'),
				sheet: vm.get('sheet'),
				mappings: mappings,
				importMode: vm.get('importmode')
			};
		}
	}
});
