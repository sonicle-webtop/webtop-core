/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.view.CustomPanel', {
	alternateClassName: 'WTA.view.CustomPanel',
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.Data',
		'Sonicle.Object',
		'Sonicle.String',
		'Sonicle.Utils',
		'Sonicle.form.field.LabelTag',
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.feature.RowLookup',
		'Sonicle.grid.plugin.DDOrdering',
		'Sonicle.form.field.pickerpanel.Picker',
		'Sonicle.picker.List',
		'Sonicle.plugin.FieldTooltip',
		'WTA.ux.PickerWindow',
		'Sonicle.webtop.core.model.CustomFieldLkp',
		'Sonicle.webtop.core.model.CustomPanel'
	],
	uses: [
		'WTA.ux.picker.CFDataSourceDataDependsOn',
		'WTA.ux.picker.CFContactPickerDataDependsOn',
		'WTA.ux.picker.CustomFieldExpr'
	],
	
	/**
	 * @cfg {String} serviceId
	 * Target service ID for which managing fields.
	 */
	serviceId: null,
	
	/**
	 * @cfg {String} serviceName
	 * Target service display name for displaying in title.
	 */
	serviceName: null,
	
	dockableConfig: {
		title: '{customPanel.tit}',
		iconCls: 'wt-icon-customPanel',
		width: 450,
		height: 520
	},
	modelName: 'Sonicle.webtop.core.model.CustomPanel',
	fieldTitle: 'name',
	autoToolbar: false,
	
	constructor: function(cfg) {
		var me = this;
		Ext.merge(cfg, {
			dockableConfig: {
				title: '[' + Sonicle.String.deflt(cfg.serviceName, cfg.serviceId) + '] ' + WT.res('customPanel.tit')
			}
		});
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foMainTitle: WTF.foRecordTwoWay('record', 'titleI18n', 'txt', WT.getLanguage()),
			foTags: WTF.foFieldTwoWay('record', 'tags', function(v) {
					return Sonicle.String.split(v, '|');
				}, function(v) {
					return Sonicle.String.join('|', v);
			})
		});
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		Ext.apply(me, {
			tbar: [
				me.addAct('saveClose', {
					text: WT.res('act-saveClose.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-saveClose',
					handler: function() {
						me.saveView(true);
					}
				}),
				'-',
				me.addAct('tags', {
					text: null,
					tooltip: me.mys.res('act-manageTags.lbl'),
					iconCls: 'wt-icon-tags',
					disabled: !WT.isPermitted(me.mys.ID, 'TAGS', 'MANAGE'),
					handler: function() {
						me.showManageTagsUI();
					}
				})
			]
		});
		me.callParent(arguments);
		
		me.lookupStore = Ext.create('Ext.data.JsonStore', {
			autoLoad: true,
			model: 'Sonicle.webtop.core.model.CustomFieldLkp',
			proxy: WTF.apiProxy(me.mys.ID, 'LookupCustomFields', null, {
				extraParams: {
					serviceId: me.serviceId
				}
			})
		});
		
		var propviewGroup = Ext.id(null, 'custfield-propview-');
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
					reference: 'pnlmain',
					modelValidation: true,
					items: [
						{
							xtype: 'textfield',
							reference: 'fldname',
							bind: '{record.name}',
							fieldLabel: me.res('customPanel.fld-name.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textareafield',
							bind: '{record.description}',
							fieldLabel: me.res('customPanel.fld-description.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{foMainTitle}',
							fieldLabel: me.res('customPanel.fld-title.lbl', WT.getLanguage()),
							anchor: '100%'
						}, {
							xtype: 'fieldcontainer',
							layout: {
								type: 'hbox',
								padding: '0 0 1 0' // fixes classic-theme bottom border issue
							},
							items: [
								{
									xtype: 'solabeltagfield',
									bind: '{foTags}',
									store: me.getTagsStore(),
									valueField: 'id',
									displayField: 'name',
									colorField: 'color',
									createNewOnEnter: false,
									createNewOnBlur: false,
									filterPickList: true,
									forceSelection: true,
									emptyText: WT.res(me.serviceId, 'customPanel.fld-tags.emp'),
									margin: '0 5 0 0',
									flex: 1
								}, {
									xtype: 'button',
									ui: 'default-toolbar',
									tooltip: me.mys.res('act-addTag.tip'),
									iconCls: 'wt-icon-addTag',
									disabled: !WT.isPermitted(me.mys.ID, 'TAGS', 'MANAGE'),
									handler: function() {
										me.addTagUI();
									}
								}
							],
							plugins: [{ptype: 'sofieldtooltip', tooltipTarget: 'label'}],
							fieldLabel: me.res('customPanel.fld-tags.lbl'),
							tooltip: me.res('customPanel.fld-tags.tip'),
						    anchor: '100%'
						}
					]
				}, {
					xtype: 'wttabpanel',
					reference: 'tabmain',
					items: [
						{
							xtype: 'gridpanel',
							title: me.mys.res('customPanel.fields.tit'),
							border: false,
							bind: '{record.assocFields}',
							viewConfig: {
								deferEmptyText: false,
								emptyText: WT.res('grid.emp'),
								plugins: [
									{
										ptype: 'sogridviewddordering',
										orderField: 'order'
									}
								]
							},
							features: [
								{
									ftype: 'sorowlookup',
									store: me.lookupStore
								}
							],
							columns: [
								{
									xtype: 'rownumberer'
								}, {
									xtype: 'soiconcolumn',
									dataIndex: 'id',
									hideText: false,
									getText: function(val, rec) {
										var lrec = rec.lookupRecord();
										return lrec ? me.res('store.customFieldType.'+lrec.get('type')) : val;
									},
									getIconCls: function(val, rec) {
										var lrec = rec.lookupRecord();
										return lrec ? me.mys.cssIconCls('customField-'+lrec.get('type')) : val;
									},
									iconSize: WTU.imgSizeToPx('xs'),
									header: me.res('customPanel.gp-fields.type.lbl'),
									flex: 1
								}, {
									dataIndex: 'id',
									header: me.res('customPanel.gp-fields.name.lbl'),
									renderer: function(val, meta, rec) {
										var lrec = rec.lookupRecord();
										return lrec ? lrec.get('name') : val;
									},
									flex: 1
								}, {
									xtype: 'soactioncolumn',
									items: [
										{
											iconCls: 'far fa-trash-alt',
											tooltip: WT.res('act-remove.lbl'),
											handler: function(g, ridx) {
												var rec = g.getStore().getAt(ridx);
												me.getModel().assocFields().remove(rec);
											}
										}
									]

								}
							],
							tbar: [
								me.addAct('add', {
									text: WT.res('act-add.lbl'),
									tooltip: null,
									handler: function() {
										me.showFieldPicker();
									}
								})
							]
						}, {
							xtype: 'propertygrid',
							itemId: 'props',
							title: me.res('customPanel.props.tit'),
							bind: {
								store: '{record.props}'
							},
							viewConfig: {
								deferEmptyText: false,
								emptyText: me.res('customPanel.gp-props.emp')
							},
							features: [
								{
									ftype: 'grouping',
									groupHeaderTpl: [
										'{name:this.nameRes}',
										{
											nameRes: function(name) {
												return me.res('customField.gp-props.group.'+name);
											}
										}
									]
								}
							],
							tbar: [
								{
									xtype: 'sotogglebutton',
									toggleGroup: propviewGroup,
									offIconCls: 'wt-icon-propView-hier-gray',
									onIconCls: 'wt-icon-propView-hier',
									allowDepress: false,
									pressed: true,
									toggleHandler: function(s, pressed) {
										if (pressed) me.updatePropView('hier');
									}
								}, {
									xtype: 'sotogglebutton',
									toggleGroup: propviewGroup,
									offIconCls: 'wt-icon-propView-sorted-gray',
									onIconCls: 'wt-icon-propView-sorted',
									allowDepress: false,
									pressed: false,
									toggleHandler: function(s, pressed) {
										if (pressed) me.updatePropView('sorted');
									}
								}
							],
							nameColumnWidth: 180,
							inferTypes: false,
							sortableColumns: false,
							border: false,
							flex: 1
						}
					],
					flex: 1
				}, me.mys.hasAuditUI() ? {
					xtype: 'statusbar',
					items: [
						me.addAct('customPanelAuditLog', {
							text: null,
							tooltip: WT.res('act-auditLog.lbl'),
							iconCls: 'fas fa-history',
							handler: function() {
								me.mys.openAuditUI(me.getModel().getId(), 'CUSTOMPANEL');
							},
							scope: me
						})
					]
				} : null
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	addTag: function(opts) {
		var me = this,
				pal = WT.getColorPalette('default'),
				rndColor = pal[Math.floor(Math.random() * pal.length)],
				vw = WT.createView(me.mys.ID, 'view.TagEditor', {
					swapReturn: true,
					viewCfg: {
						data: {
							visibility: 'shared',
							color: Sonicle.String.prepend(rndColor, '#', true)
						},
						visibilityEditable: false
					}
				});
		
		vw.on('viewok', function(s, data) {
			Ext.callback(opts.callback, opts.scope || me, [data]);
		});
		vw.showView();
	},
	
	privates: {
		getTagsStore: function() {
			return WT.getTagsStore({filters: [{id: 'sharedFilter', property: 'visibility', value: 'shared'}]});
		},
		
		onViewLoad: function(s, success) {
			var me = this,
				mo = me.getModel();
			
			me.lref('fldname').focus(true);
			if (success) {
				Sonicle.Utils.configurePropertyGrid(me.lref('tabmain').getComponent('props'), {
					'priority': {
						displayName: me.res('customPanel.gp-props.priority'),
						type: 'boolean',
						renderer: me.booleanYesNoRenderer(),
						editor: me.booleanYesNoEditor(),
						defaultValue: false
					},
					'onBeforeSaveExpr': {
						displayName: me.res('customPanel.gp-props.onBeforeSaveExpr'),
						type: 'string',
						renderer: me.exprRenderer(),
						editor: {
							xtype: 'sopickerpanelfield',
							matchFieldWidth: false,
							renderer: me.exprRenderer(true),
							pickerEditorClass: 'WTA.ux.picker.CustomFieldExpr',
							pickerEditorConfig: {
								multiline: true
							}
						},
						defaultValue: null
					}
				}, mo ? mo.props() : null);
			}
			if (me.mys.hasAuditUI()) {
				if (me.isMode(me.MODE_NEW)) {
					me.getAct('customPanelAuditLog').setDisabled(true);
				} else {
					me.getAct('customPanelAuditLog').setDisabled(false);
				}
			}
		},
		
		onFieldPickerPick: function(s, vals, recs) {
			var me = this, 
					fsto = me.getModel().assocFields();
			
			Ext.iterate(vals, function(val) {
				fsto.add({
					id: val,
					order: fsto.getCount()
				});
			});
			me.fieldPicker.close();
			me.fieldPicker = null;
		},
		
		addTagUI: function() {
			var me = this;
			me.addTag({
				callback: function(data) {
					WT.ajaxReq(me.mys.ID, 'ManageTags', {
						params: {
							crud: 'create'
						},
						jsonData: [{name: data.name, color: data.color, visibility: data.visibility}],
						callback: function(success, json) {
							if (!success) WT.error(me.mys.res('customPanel.error.newtag'));
						}
					});
				}
			});
		},
		
		showManageTagsUI: function() {
			WT.createView(this.mys.ID, 'view.Tags', {
				swapReturn: true,
				viewCfg: {
					enableSelection: false
				}
			}).showView();
		},
		
		showFieldPicker: function() {
			var me = this,
				usedFields = Sonicle.Data.collectValues(me.getModel().assocFields());
			me.fieldPicker = me.createFieldPicker();
			me.fieldPicker.getComponent(0).setSkipValues(usedFields);
			me.fieldPicker.show();
		},
		
		createFieldPicker: function() {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: me.res('customPanel.fieldPicker.tit'),
				height: 350,
				items: [{
					xtype: 'solistpicker',
					store: me.lookupStore,
					valueField: 'id',
					displayField: 'name',
					searchField: 'name',
					emptyText: WT.res('grid.emp'),
					searchText: WT.res('textfield.search.emp'),
					selectedText: WT.res('grid.selected.lbl'),
					okText: WT.res('act-ok.lbl'),
					cancelText: WT.res('act-cancel.lbl'),
					allowMultiSelection: true,
					listeners: {
						cancelclick: function() {
							if (me.fieldPicker) me.fieldPicker.close();
						}
					},
					handler: me.onFieldPickerPick,
					scope: me
				}]
			});
		},
		
		updatePropView: function(view) {
			var me = this,
				sto = me.getModel().props();
			if ('sorted' === view) {
				sto.setGrouper(null);
				sto.sort({
					direction: 'ASC',
					sorterFn: function(r1, r2) {
						var res = function(rec) { return me.res('customField.gp-props.'+rec.get('name')); },
							ord1 = res(r1),
							ord2 = res(r2);
						return (ord1 > ord2) ? 1 : (ord1 === ord2 ? 0 : -1);
					}
				});
			} else {
				sto.sort({
					direction: 'ASC',
					sorterFn: function(r1, r2) {
						var ord1 = r1.get('index'),
							ord2 = r2.get('index');
						return (ord1 > ord2) ? 1 : (ord1 === ord2 ? 0 : -1);
					}
				});
				sto.setGrouper({
					property: 'group',
					direction: 'ASC',
					sorterFn: function(r1, r2) {
						var CFP = Sonicle.webtop.core.model.CustomPanelProp,
								ord1 = CFP.toGroupOrdinal(r1.get('group')),
								ord2 = CFP.toGroupOrdinal(r2.get('group'));
						return (ord1 > ord2) ? 1 : (ord1 === ord2 ? 0 : -1);
					}
				});
			}
		},
		
		exprRenderer: function(editor) {
			if (editor === true) {
				return function(v) {
					return Ext.isEmpty(v) ? '' : WT.res('customField.gp-props.expr.ed.display');
				};
			} else {
				return function(v) {
					return Ext.htmlEncode(Ext.isEmpty(v) ? '' : WT.res('customField.gp-props.expr.display'));
				};
			}
		},
		
		booleanYesNoRenderer: function() {
			return function(v) {
				var bool = Sonicle.Object.booleanValue(v);
				return Ext.htmlEncode(bool === true ? WT.res('word.yes') : WT.res('word.no'));
			};
		},
		
		booleanYesNoEditor: function() {
			return {
				xtype: 'combobox',
				editable: false,
				store: [[true, WT.res('word.yes')], [false, WT.res('word.no')]]
			};
		},
		
		emptyTextRenderer: function(key) {
			var me = this;
			return function(v) {
				if (Ext.isEmpty(v)) {
					return '<span class="wt-text-off wt-theme-text-color-off">' + Ext.htmlEncode(me.res(key)) + '</span>';
				} else {
					return v;
				}
			};
		}
	}
});
