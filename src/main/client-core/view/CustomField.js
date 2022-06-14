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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.view.CustomField', {
	alternateClassName: 'WTA.view.CustomField',
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.Object',
		'Sonicle.String',
		'Sonicle.Utils',
		'Sonicle.grid.column.Action',
		'Sonicle.grid.plugin.DDOrdering',
		'Sonicle.form.field.ComboBox',
		'Sonicle.form.field.pickerpanel.Picker',
		'Sonicle.plugin.FieldChangeDetector',
		'Sonicle.plugin.FieldAvailabilityCheck',
		'WTA.model.DataSourceQueryLkp',
		'Sonicle.webtop.core.model.CustomField',
		'Sonicle.webtop.core.model.CustomFieldLkp',
		'Sonicle.webtop.core.model.CustomFieldExpMemberLkp',
		'Sonicle.webtop.core.store.CustomFieldType'
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
		title: '{customField.tit}',
		iconCls: 'wt-icon-customField',
		width: 450,
		height: 520
	},
	modelName: 'Sonicle.webtop.core.model.CustomField',
	fieldTitle: 'name',
	
	constructor: function(cfg) {
		var me = this;
		Ext.merge(cfg, {
			dockableConfig: {
				title: '[' + Sonicle.String.deflt(cfg.serviceName, cfg.serviceId) + '] ' + WT.res('customField.tit')
			}
		});
		me.callParent([cfg]);
		
		me.queryStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.DataSourceQueryLkp',
			proxy: WTF.proxy(WT.ID, 'LookupDataSourceQueries'),
			listeners: {
				load: function(s) {
					if (s.loadCount === 1) {
						var mo = me.getModel();
						if (mo && mo.isDataBindableType()) {
							// Make sure that query IDs are properly decoded in grid
							me.lref('tabmain').getComponent('props').getView().refresh();
						}
					}
				}
			}
		});
		me.queryFieldsStore = Ext.create('Ext.data.ArrayStore', {
			proxy: WTF.proxy(WT.ID, 'LookupDataSourceQueryFields'),
			fields: ['name'],
			listeners: {
				beforeload: function(s) {
					var mo = me.getModel(), prec;
					if (mo && (prec = mo.props().getById('queryId'))) {
						WTU.applyExtraParams(s, {
							queryId: prec.get('value')
						});
					}
				}
			}
		});
		me.queryPlaceholdersStore = Ext.create('Ext.data.ArrayStore', {
			proxy: WTF.proxy(WT.ID, 'LookupDataSourceQueryPlaceholders'),
			fields: ['name'],
			listeners: {
				beforeload: function(s) {
					var mo = me.getModel(), prec;
					if (mo && (prec = mo.props().getById('queryId'))) {
						WTU.applyExtraParams(s, {
							queryId: prec.get('value')
						});
					}
				}
			}
		});
		me.dependsOnFieldStore = Ext.create('Ext.data.JsonStore', {
			autoLoad: true,
			model: 'Sonicle.webtop.core.model.CustomFieldLkp',
			proxy: WTF.proxy(WT.ID, 'LookupCustomFields', null, {
				extraParams: {
					serviceId: me.serviceId
				}
			})
		});
		
		WTU.applyFormulas(me.getVM(), {
			//searchable: WTF.checkboxBind('record', 'searchable'),
			//previewable: WTF.checkboxBind('record', 'previewable'),
			foEnableValues: WTF.foGetFn('record', 'type', function(v) {
				return Ext.isEmpty(v) ? false : ['combobox', 'radios', 'tag'].indexOf(v) !== -1;
			}),
			foMainLabel: WTF.foRecordTwoWay('record', 'labelI18n', 'txt', WT.getLanguage())
		});
		
		me.typeProps = {
			'text': me.propsBuilder(
				'searchable', 'previewable', 'required', 'minLength', 'maxLength', 'maskRe', 'validationRe', 'emptyText', 'width', 'anchor', 'disabledExpr'
			),
			'textarea': me.propsBuilder(
				'searchable', 'previewable', 'required', 'minLength', 'maxLength', 'autoGrow', 'emptyText', 'width', 'anchor', 'disabledExpr'
			),
			'number': me.propsBuilder(
				'searchable', 'previewable', 'required', 'minValue', 'maxValue', 'allowDecimals', 'emptyText', 'width', 'anchor', 'disabledExpr'
			),
			'date': me.propsBuilder(
				'searchable', 'previewable', 'required', 'disabledExpr', 'onChangeExpr'
			),
			'time': me.propsBuilder(
				'searchable', 'previewable', 'required', 'disabledExpr', 'onChangeExpr'
			),
			'combobox': me.propsBuilder(
				'searchable', 'previewable', 'required', 'queryable', 'emptyText', 'width', 'anchor', 'disabledExpr', 'onChangeExpr'
			),
			'comboboxds': me.propsBuilder(
				'searchable', 'previewable', 'required', 'queryable', 'emptyText', 'width', 'anchor', 'queryId', 'valueField', 'displayField', 'pageSize', 'dataDependsOn', 'disabledExpr', 'onChangeExpr'
			),
			'tag': me.propsBuilder(
				'searchable', 'previewable', 'required', 'emptyText', 'width', 'anchor', 'disabledExpr', 'onChangeExpr'
			),
			'tagds': me.propsBuilder(
				'searchable', 'previewable', 'required', 'emptyText', 'width', 'anchor', 'queryId', 'valueField', 'displayField', 'disabledExpr', 'onChangeExpr'
			),
			'contactpicker': me.propsBuilder(
				'searchable', 'previewable', 'required', 'emptyText', 'contactPickerNewButton', 'displayTpl', 'width', 'anchor', 'dataDependsOn', 'disabledExpr', 'onChangeExpr', 'contactPickerCategoryIds', 'contactPickerAddContactApiDataExpr'
			)
		};
	},
	
	propsBuilder: function(names) {
		var me = this, obj = {}, name;
		Ext.iterate(arguments, function(prop) {
			name = prop;
			//TODO: support overrides!
			//if (Ext.isObject(prop)) {...
			if ('searchable' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.searchable'),
					type: 'boolean',
					renderer: me.booleanYesNoRenderer(),
					editor: me.booleanYesNoEditor(),
					defaultValue: false
				};
			} else if ('previewable' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.previewable'),
					type: 'boolean',
					renderer: me.booleanYesNoRenderer(),
					editor: me.booleanYesNoEditor(),
					defaultValue: false
				};
			} else if ('queryable' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.queryable'),
					type: 'boolean',
					renderer: me.booleanYesNoRenderer(),
					editor: me.booleanYesNoEditor(),
					defaultValue: false
				};
			} else if ('required' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.required'),
					type: 'boolean',
					renderer: me.booleanYesNoRenderer(),
					editor: me.booleanYesNoEditor(),
					defaultValue: false
				};
			} else if ('emptyText' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.emptyText'),
					type: 'string',
					defaultValue: null
				};
			} else if ('width' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.width'),
					type: 'number',
					defaultValue: null
				};
			} else if ('anchor' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.anchor'),
					type: 'string',
					defaultValue: null
				};
			} else if ('autoGrow' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.autoGrow'),
					type: 'boolean',
					renderer: me.booleanYesNoRenderer(),
					editor: me.booleanYesNoEditor(),
					defaultValue: false
				};
			} else if ('minLength' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.minLength'),
					type: 'number',
					defaultValue: null
				};
			} else if ('maxLength' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.maxLength'),
					type: 'number',
					defaultValue: null
				};
			} else if ('minValue' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.minValue'),
					type: 'number',
					defaultValue: null
				};
			} else if ('maxValue' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.maxValue'),
					type: 'number',
					defaultValue: null
				};
			} else if ('allowDecimals' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.allowDecimals'),
					type: 'boolean',
					renderer: me.booleanYesNoRenderer(),
					editor: me.booleanYesNoEditor(),
					defaultValue: false
				};
			} else if ('maskRe' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.maskRe'),
					type: 'string',
					defaultValue: null
				};
			} else if ('validationRe' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.validationRe'),
					type: 'string',
					defaultValue: null
				};
			} else if ('queryId' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.queryId'),
					type: 'string',
					renderer: function(v) {
						var rec = me.queryStore.getById(v);
						return rec ? Ext.htmlEncode(rec.get('desc')) : me.emptyTextRenderer('customField.gp-props.queryId.emp')(v);
					},
					editor: WTF.localCombo('id', 'desc', {
						xtype: 'socombobox',
						store: me.queryStore,
						sourceField: 'dsName'
					}),
					defaultValue: null
				};
			} else if ('valueField' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.valueField'),
					type: 'string',
					editor: WTF.localCombo('name', 'name', {
						forceSelection: false,
						autoLoadOnQuery: true,
						store: me.queryFieldsStore
					}),
					defaultValue: null
				};
			} else if ('displayField' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.displayField'),
					type: 'string',
					editor: WTF.localCombo('name', 'name', {
						forceSelection: false,
						autoLoadOnQuery: true,
						store: me.queryFieldsStore
					}),
					defaultValue: null
				};
			} else if ('displayTpl' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.displayTpl'),
					type: 'string',
					defaultValue: null
				};
			} else if ('dataDependsOn' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.dataDependsOn'),
					type: 'string',
					editable: false,
					getRenderer: Ext.pass(me.dataDependsOnRenderer, [false]),
					getEditor: Ext.pass(me.dataDependsOnEditor, [me]),
					defaultValue: null
				};
			} else if ('pageSize' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.pageSize'),
					type: 'number',
					renderer: me.emptyTextRenderer('customField.gp-props.pageSize.emp'),
					editor: {
						xtype: 'numberfield',
						minValue: 1,
						maxValue: 1000,
						allowDecimals: false
					},
					defaultValue: null
				};
			} else if ('disabledExpr' === name) {
				// https://github.com/EricSmekens/jsep
				// https://github.com/TomFrost/Jexl
				// https://stackoverflow.com/questions/15590702/how-to-get-minified-output-with-browserify
				// https://stackoverflow.com/questions/16172035/browserify-use-module-exports-if-required-otherwise-expose-global
				// https://pegjs.org/online
				// https://docs.rukovoditel.net/index.php?p=72
				// https://www.youtube.com/watch?v=Qzpjq008cBY
				// https://docs.microsoft.com/en-us/power-apps/maker/canvas-apps/controls/control-drop-down
				obj[name] = {
					displayName: me.res('customField.gp-props.disabledExpr'),
					type: 'string',
					renderer: me.exprRenderer(),
					editor: {
						xtype: 'sopickerpanelfield',
						matchFieldWidth: false,
						renderer: me.exprRenderer(true),
						pickerEditorClass: 'WTA.ux.picker.CustomFieldExpr',
						pickerEditorConfig: {
							multiline: false
						}
					},
					defaultValue: null
				};
			} else if ('onChangeExpr' === name) {
				// https://github.com/EricSmekens/jsep
				// https://github.com/TomFrost/Jexl
				obj[name] = {
					displayName: me.res('customField.gp-props.onChangeExpr'),
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
				};
			} else if ('contactPickerCategoryIds' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.contactPickerCategoryIds'),
					type: 'string',
					editor: {
						xtype: 'tagfield',
						store: {
							model: 'WTA.ux.data.ValueModel'
						},
						getValueMode: 'string',
						autoLoadOnValue: true,
						createNewOnEnter: true,
						createNewOnBlur: true,
						filterPickList: false,
						forceSelection: false,
						hideTrigger: true
					},
					defaultValue: null
				};
			} else if ('contactPickerNewButton' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.contactPickerNewButton'),
					type: 'boolean',
					renderer: me.booleanYesNoRenderer(),
					editor: me.booleanYesNoEditor(),
					defaultValue: true
				};
			} else if ('contactPickerAddContactApiDataExpr' === name) {
				obj[name] = {
					displayName: me.res('customField.gp-props.contactPickerAddContactApiDataExpr'),
					type: 'string',
					renderer: me.exprRenderer(),
					editor: {
						xtype: 'sopickerpanelfield',
						matchFieldWidth: false,
						renderer: me.exprRenderer(true),
						pickerEditorClass: 'WTA.ux.picker.CustomFieldExpr',
						pickerEditorConfig: {
							multiline: false
						}
					},
					defaultValue: null
				};
			}
		});
		return obj;
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		Ext.apply(me, {
        	bbar: {
        		xtype: 'statusbar',
        		items: [
					me.mys.hasAuditUI() ? me.addAct('customFieldAuditLog', {
						text: null,
						tooltip: WT.res('act-auditLog.lbl'),
						iconCls: 'fas fa-history',
						handler: function() {
							me.mys.openAuditUI(me.getModel().getId(), 'CUSTOMFIELD');
						},
						scope: me
					}) : null,
					WTF.recordInfoButton({
						getTooltip: function() {
							return Ext.String.format('ID: {0}', Sonicle.String.coalesce(me.getModel().get('fieldId'), '?'));
						}
					})
        		]
        	}
        });
		
		me.callParent(arguments);
		
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
					defaults: {
						labelWidth: 120
					},
					items: [
						{
							xtype: 'textfield',
							reference: 'fldname',
							bind: '{record.name}',
							fieldLabel: me.mys.res('customField.fld-name.lbl'),
							emptyText: me.mys.res('customField.fld-name.emp'),
							plugins: [
								{
									ptype: 'sofieldchangedetector',
									handler: function() {
										if (me.isMode(me.MODE_EDIT) && me.getModel().getModified('name') !== undefined) {
											WT.warn(me.res('customField.warn.rename'));
										}
									}
								}, {
									ptype: 'sofieldavailabilitycheck',
									baseIconCls: 'wt-opacity-50',
									availableTooltipText: WT.res('sofieldavailabilitycheck.availableTooltipText'),
									unavailableTooltipText: WT.res('sofieldavailabilitycheck.unavailableTooltipText'),
									checkAvailability: function(value, done) {
										if (me.getModel().getModified('name') === undefined) return false;
										WT.ajaxReq(WT.ID, 'ManageCustomField', {
											params: {
												crud: 'check',
												fieldServiceId: me.serviceId,
												name: value
											},
											callback: function(success, json) {
												done(success ? json.data : json.message);
											}
										});
									}
								}
							],
							anchor: '100%'
						}, {
							xtype: 'textareafield',
							bind: '{record.description}',
							fieldLabel: me.mys.res('customField.fld-description.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{foMainLabel}',
							fieldLabel: me.res('customField.fld-label.lbl', WT.getLanguage()),
							anchor: '100%'
						}, 
						WTF.lookupCombo('id', 'desc', {
							xtype: 'socombo',
							bind: '{record.type}',
							store: {
								type: 'wtcustomfieldtype',
								autoLoad: true
							},
							iconField: 'icon',
							fieldLabel: me.mys.res('customField.fld-type.lbl'),
							emptyText: me.mys.res('customField.fld-type.emp'),
							anchor: '100%'
						})
					]
				}, {
					xtype: 'wttabpanel',
					reference: 'tabmain',
					items: [
						{
							xtype: 'propertygrid',
							itemId: 'props',
							title: me.res('customField.props.tit'),
							bind: {
								store: '{record.props}'
							},
							viewConfig: {
								deferEmptyText: false,
								emptyText: me.res('customField.gp-props.emp')
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
						}, {
							xtype: 'gridpanel',
							itemId: 'values',
							title: me.mys.res('customField.values.tit'),
							bind: {
								store: '{record.values}',
								disabled: '{!foEnableValues}'
							},
							viewConfig: {
								deferEmptyText: false,
								plugins: [{
									ptype: 'sogridviewddordering',
									orderField: 'order'
								}]
							},
							plugins: [{
								id: 'cellediting',
								ptype: 'cellediting',
								clicksToEdit: 2
							}],
							columns: [
								{
									dataIndex: 'key',
									header: me.res('customField.gp-values.key.lbl'),
									editor: 'textfield',
									flex: 1
								}, {
									dataIndex: 'desc',
									header: me.res('customField.gp-values.desc.lbl'),
									editor: 'textfield',
									flex: 1
								}, {
									xtype: 'soactioncolumn',
									items: [
										{
											iconCls: 'far fa-trash-alt',
											tooltip: WT.res('act-remove.lbl'),
											handler: function(g, ridx) {
												var rec = g.getStore().getAt(ridx);
												me.deleteValueUI(rec);
											}
										}
									],
									width: 50
								}
							],
							listeners: {
								validateedit: function(ed, ctx) {
									if (ctx.field === 'key') {
										var aed = ed.activeEditor,
												idx = ctx.grid.getStore().find('key', ctx.value);
										if ((idx !== -1) && (idx !== ctx.rowIdx)) {
											//WT.warn(me.mys.res('customField.gp-values.warn.duplicate'), {
											//	fn: function() {
											//		if (aed) aed.field.focus();
											//	}
											//});
											return false;
										}
									}
								}
							},
							tbar: [
								me.addAct('addValue', {
									text: WT.res('act-add.lbl'),
									tooltip: null,
									iconCls: 'wt-icon-add',
									handler: function() {
										me.addValueUI();
									}
								})
							],
							border: false
						}
						
					],
					flex: 1
				}
			]
		});
		me.on('viewload', me.onViewLoad);
		vm.bind('{record.type}', me.onTypeChange, me);
		vm.bind('{foEnableValues}', me.onEnableValuesChange, me);
	},
	
	doDestroy: function() {
		var me = this,
			mo = me.getModel();
		if (mo) mo.props().on('update', me.onPropsStoreUpdate, me);
		me.callParent();
	},
	
	addValueUI: function() {
		var me = this,
				mo = me.getModel(),
				sto = mo ? mo.values() : null,
				gp = me.lref('tabmain').getComponent('values'),
				ed = gp.getPlugin('cellediting');
		
		ed.cancelEdit();
		sto.add(sto.createModel({key: null, desc: null, order: sto.getCount()}));
		ed.startEditByPosition({row: sto.getCount()-1, column: 0});
	},
	
	deleteValueUI: function(rec) {
		var me = this,
				mo = me.getModel();
		if (mo) mo.values().remove(rec);
	},
	
	privates: {
		onViewLoad: function(s, success) {
			var me = this,
				SoO = Sonicle.Object,
				mo = me.getModel(),
				props, prec;
			
			if (success) {
				me.updatePropView('hier');
				props = mo.props();
				if (me.isMode(me.MODE_EDIT)) {
					prec = props.getById('searchable');
					if (prec) prec.set('value', SoO.stringValue(mo.get('searchable')), {dirty: false});
					prec = props.getById('previewable');
					if (prec) prec.set('value', SoO.stringValue(mo.get('previewable')), {dirty: false});
					prec = props.getById('queryId');
					if (prec) prec.set('value', SoO.stringValue(mo.get('queryId')), {dirty: false});
				}
				props.on('update', me.onPropsStoreUpdate, me);
			}
			
			if (me.mys.hasAuditUI()) {
				if (me.isMode(me.MODE_NEW)) {
					me.getAct('customFieldAuditLog').setDisabled(true);
				} else {
					me.getAct('customFieldAuditLog').setDisabled(false);
				}
			}
			me.lref('fldname').focus(true);
		},
		
		onTypeChange: function(nv, ov) {
			var me = this,
				mo = me.getModel(),
				pgrid = me.lref('tabmain').getComponent('props'),
				sourceConfig = me.generateSourceConfig(nv);
			
			Sonicle.Utils.configurePropertyGrid(pgrid, sourceConfig, mo ? mo.props() : null);
			if (me.isMode(me.MODE_EDIT) && ov) {
				if (['text', 'textarea'].indexOf(nv) !== -1 && ['text', 'textarea'].indexOf(ov) === -1) {
					WT.warn(me.mys.res('customField.fld-type.confirm'));
				}
			}
		},
		
		onPropsStoreUpdate: function(s, rec, op) {
			var me = this;
			if (op === Ext.data.Model.EDIT) {
				var name = rec.get('name'),
					value = rec.get('value'),
					mo = me.getModel();
				if ('searchable' === name) {
					mo.set(name, value);
					if (value === true) {
						WT.info(me.res('customField.info.searchable') + (me.hasParentDependency() ? '\n' + me.res('customField.info.searchable.dependent') : ''));
					}
				} else if ('previewable' === name) {
					mo.set(name, value);
				} else if ('queryId' === name) {
					mo.set(name, value);
					if (Ext.isEmpty(value)) {
						me.queryFieldsStore.removeAll(true);
						me.queryPlaceholdersStore.removeAll(true);
					} else {
						me.queryFieldsStore.load();
						me.queryPlaceholdersStore.load();
					}
				}
			}
		},

		onEnableValuesChange: function(nv, ov) {
			if (!nv) this.lref('tabmain').getLayout().setActiveItem('props');
		},
		
		hasParentDependency: function() {
			var mo = this.getModel(), prec;
			if (mo) {
				prec = mo.props().getById('dataDependsOn');
				if (prec) return Ext.isObject(Ext.JSON.decode(prec.get('value'), true));
			}
			return false;
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
						var CFP = Sonicle.webtop.core.model.CustomFieldProp,
								ord1 = CFP.toGroupOrdinal(r1.get('group')),
								ord2 = CFP.toGroupOrdinal(r2.get('group'));
						return (ord1 > ord2) ? 1 : (ord1 === ord2 ? 0 : -1);
					}
				});
			}
		},

		generateSourceConfig: function(type) {
			var me = this,
				props = me.typeProps[type],
				sourceConfig = {}, obj;

			Ext.Object.each(props, function(propName, propData) {
				obj = {};
				['displayName', 'type', 'defaultValue', 'editor', 'renderer', 'getRenderer', 'getEditor'].forEach(function(name) {
					if ('getRenderer' === name) {
						if (Ext.isFunction(propData[name])) obj['renderer'] = propData[name](type);
					} else if ('getEditor' === name) {
						if (Ext.isFunction(propData[name])) obj['editor'] = propData[name](type);
					} else {
						if (propData[name] !== undefined) obj[name] = propData[name];
					}
				});
				sourceConfig[propName] = obj;
			});
			return sourceConfig;
		},
		
		dataDependsOnEditor: function(me, type) {
			var SoS = Sonicle.String;
			if (SoS.isIn(type, ['comboboxds', 'tagds'])) {
				return {
					xtype: 'sopickerpanelfield',
					renderer: me.dataDependsOnRenderer(true, type),
					pickerEditorClass: 'WTA.ux.picker.CFDataSourceDataDependsOn',
					pickerEditorConfig: {
						fieldsStore: me.dependsOnFieldStore,
						placeholderStore: me.queryPlaceholdersStore
					}
				};
			} else if (type === 'contactpicker') {
				return {
					xtype: 'sopickerpanelfield',
					renderer: me.dataDependsOnRenderer(true, type),
					pickerEditorClass: 'WTA.ux.picker.CFContactPickerDataDependsOn',
					pickerEditorConfig: {
						fieldsStore: me.dependsOnFieldStore
					}
				};
			} else {
				return false;
			}
		},
		
		dataDependsOnRenderer: function(editor, type) {
			if (editor === true) {
				return function(v) {
					return WT.res('customField.gp-props.dataDependsOn.ed.display');
				};
			} else {
				var SoS = Sonicle.String;
				if (SoS.isIn(type, ['comboboxds', 'tagds'])) {
					return function(v) {
						var data = Ext.JSON.decode(v, true);
						return Ext.htmlEncode(data ? WTA.ux.picker.CFDataSourceDataDependsOn.format(data) : v);
					};
				} else if (type === 'contactpicker') {
					return function(v) {
						var data = Ext.JSON.decode(v, true);
						return Ext.htmlEncode(data ? WTA.ux.picker.CFContactPickerDataDependsOn.format(data) : v);
					};
				} else {
					return function(v) {
						return Ext.htmlEncode(v);
					};
				}
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
					return '<span class="wt-theme-text-lighter2" style="font-size:0.9em;">' + Ext.htmlEncode(me.res(key)) + '</span>';
				} else {
					return v;
				}
			};
		}
	}
});
