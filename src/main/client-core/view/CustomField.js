/* 
 * Copyright (C) 2019 Sonicle S.r.l.
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
		'Sonicle.grid.plugin.DDOrdering',
		'Sonicle.webtop.core.model.CustomField',
		'Sonicle.webtop.core.store.CustomFieldType'
	],
	
	dockableConfig: {
		title: '{customField.tit}',
		iconCls: 'wt-icon-customField',
		width: 450,
		height: 520
	},
	modelName: 'Sonicle.webtop.core.model.CustomField',
	fieldTitle: 'name',
	viewModel: {
		formulas: {
			foEnableValues: WTF.foGetFn('record', 'type', function(v) {
				return Ext.isEmpty(v) ? false : ['combobox', 'radios'].indexOf(v) !== -1;
			})
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foMainLabel: WTF.foRecordTwoWay('record', 'labelI18n', 'txt', WT.getLanguage())
		});
		
		me.typeProps = {
			'text': {
				required: {
					displayName: me.mys.res('customField.gp-props.required'),
					type: 'boolean',
					defaultValue: false
				},
				minLength: {
					displayName: me.mys.res('customField.gp-props.minLength'),
					type: 'number',
					defaultValue: null
				},
				maxLength: {
					displayName: me.mys.res('customField.gp-props.maxLength'),
					type: 'number',
					defaultValue: null
				},
				emptyText: {
					displayName: me.mys.res('customField.gp-props.emptyText'),
					type: 'string',
					defaultValue: null
				},
				maskRe: {
					displayName: me.mys.res('customField.gp-props.maskRe'),
					type: 'string',
					defaultValue: null
				},
				validationRe: {
					displayName: me.mys.res('customField.gp-props.validationRe'),
					type: 'string',
					defaultValue: null
				},
				width: {
					displayName: me.mys.res('customField.gp-props.width'),
					type: 'number',
					defaultValue: null
				},
				anchor: {
					displayName: me.mys.res('customField.gp-props.anchor'),
					type: 'string',
					defaultValue: null
				}
			},
			'textarea': {
				required: {
					displayName: me.mys.res('customField.gp-props.required'),
					type: 'boolean',
					defaultValue: false
				},
				minLength: {
					displayName: me.mys.res('customField.gp-props.minLength'),
					type: 'number',
					defaultValue: null
				},
				maxLength: {
					displayName: me.mys.res('customField.gp-props.maxLength'),
					type: 'number',
					defaultValue: null
				},
				emptyText: {
					displayName: me.mys.res('customField.gp-props.emptyText'),
					type: 'string',
					defaultValue: null
				},
				width: {
					displayName: me.mys.res('customField.gp-props.width'),
					type: 'number',
					defaultValue: null
				},
				anchor: {
					displayName: me.mys.res('customField.gp-props.anchor'),
					type: 'string',
					defaultValue: null
				},
				autoGrow: {
					displayName: me.mys.res('customField.gp-props.autoGrow'),
					type: 'boolean',
					defaultValue: false
				}
			},
			'number': {
				required: {
					displayName: me.mys.res('customField.gp-props.required'),
					type: 'boolean',
					defaultValue: false
				},
				minValue: {
					displayName: me.mys.res('customField.gp-props.minValue'),
					type: 'number',
					defaultValue: null
				},
				maxValue: {
					displayName: me.mys.res('customField.gp-props.maxValue'),
					type: 'number',
					defaultValue: null
				},
				allowDecimals: {
					displayName: me.mys.res('customField.gp-props.allowDecimals'),
					type: 'boolean',
					defaultValue: false
				},
				emptyText: {
					displayName: me.mys.res('customField.gp-props.emptyText'),
					type: 'string',
					defaultValue: null
				},
				width: {
					displayName: me.mys.res('customField.gp-props.width'),
					type: 'number',
					defaultValue: null
				},
				anchor: {
					displayName: me.mys.res('customField.gp-props.anchor'),
					type: 'string',
					defaultValue: null
				}
			},
			'date': {
				required: {
					displayName: me.mys.res('customField.gp-props.required'),
					type: 'boolean',
					defaultValue: false
				}
			},
			'time': {
				required: {
					displayName: me.mys.res('customField.gp-props.required'),
					type: 'boolean',
					defaultValue: false
				}
			},
			'combobox': {
				required: {
					displayName: me.mys.res('customField.gp-props.required'),
					type: 'boolean',
					defaultValue: false
				},
				queryable: {
					displayName: me.mys.res('customField.gp-props.queryable'),
					type: 'boolean',
					defaultValue: false
				},
				emptyText: {
					displayName: me.mys.res('customField.gp-props.emptyText'),
					type: 'string',
					defaultValue: null
				},
				width: {
					displayName: me.mys.res('customField.gp-props.width'),
					type: 'number',
					defaultValue: null
				},
				anchor: {
					displayName: me.mys.res('customField.gp-props.anchor'),
					type: 'string',
					defaultValue: null
				}
			}
		};
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		me.callParent(arguments);
		
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
							anchor: '100%'
						}, {
							xtype: 'textfield',
							reference: 'fldid',
							bind: '{record.fieldId}',
							fieldLabel: me.mys.res('customField.fld-id.lbl'),
							readOnly: true,
							editable: false,
							anchor: '100%'
						}, {
							xtype: 'textareafield',
							bind: '{record.description}',
							fieldLabel: me.mys.res('customField.fld-description.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{foMainLabel}',
							fieldLabel: me.res('customField.fld-label.lbl'),
							anchor: '100%'
						}, 
						WTF.lookupCombo('id', 'desc', {
							bind: '{record.type}',
							store: {
								type: 'wtcustomfieldtype',
								autoLoad: true
							},
							fieldLabel: me.mys.res('customField.fld-type.lbl'),
							emptyText: me.mys.res('customField.fld-type.emp'),
							anchor: '100%',
							listeners: {
								select: function() {

								}
							}
						})
					]
				}, {
					xtype: 'wttabpanel',
					reference: 'tabmain',
					items: [
						{
							xtype: 'propertygrid',
							itemId: 'props',
							title: me.mys.res('customField.props.tit'),
							bind: {
								store: '{record.props}'
							},
							viewConfig: {
								deferEmptyText: false,
								emptyText: me.mys.res('customField.gp-props.emp')
							},
							nameColumnWidth: 150,
							inferTypes: false,
							sortableColumns: false,
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
									header: me.mys.res('customField.gp-values.key.lbl'),
									editor: 'textfield',
									flex: 1
								}, {
									dataIndex: 'desc',
									header: me.mys.res('customField.gp-values.desc.lbl'),
									editor: 'textfield',
									flex: 1
								}, {
									xtype: 'wtactioncolumn',
									items: [{
										iconCls: 'fa fa-trash',
										tooltip: WT.res('act-remove.lbl'),
										handler: function(g, ridx) {
											var rec = g.getStore().getAt(ridx);
											me.deleteValueUI(rec);
										}
									}],
									width: 50
								}
							],
							listeners: {
								validateedit: function(ed, ctx) {
									if (ctx.field === 'value') {
										var aed = ed.activeEditor,
												idx = ctx.grid.getStore().find('value', ctx.value);
										if ((idx !== -1) && (idx !== ctx.rowIdx)) {
											/*
											WT.warn(me.mys.res('customField.gp-values.warn.duplicate'), {
												fn: function() {
													if (aed) aed.field.focus();
												}
											});
											*/
											return false;
										}
									}
								}
							},
							tbar: [
								me.addAct('addValue', {
									text: WT.res('act-add.lbl'),
									tooltip: null,
									iconCls: 'wt-icon-add-xs',
									handler: function() {
										me.addValueUI();
									}
								})
							]
						}
					],
					flex: 1
				}
			]
		});
		me.on('viewload', me.onViewLoad);
		
		vm.bind('{record.type}', me.onTypeChange, me);
		vm.bind('{foEnableValues}', me.onEnableValuesChange, me);
		/*vm.bind('{foEnableValues}', function(v1,v2) {
			console.log('foEnableValues');
		}, me);
		vm.bind('{record.type}', function(v1,v2) {
			console.log('record.type');
		}, me);
		*/
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
					mo = me.getModel();

			if (me.isMode(me.MODE_NEW)) {	
				me.lref('fldid').setReadOnly(false);
			} else if(me.isMode(me.MODE_VIEW)) {
				me.lref('fldid').setReadOnly(true);
			} else if(me.isMode(me.MODE_EDIT)) {
				me.lref('fldid').setReadOnly(true);
			}

			me.lref('fldname').focus(true);
		},
		
		onTypeChange: function(nv, ov) {
			var me = this,
					mo = me.getModel(),
					sto = mo ? mo.props() : null,
					pgp = me.lref('tabmain').getComponent('props'),
					sourceCfg = me.generateSourceConfig(nv),
					recs, pgp, keys;

			if (sourceCfg) {
				pgp.sourceConfig = sourceCfg;
				pgp.configure(sourceCfg);
			}
			
			if (pgp.sourceConfig && sto) {
				recs = [];
				sto.each(function(rec) {
					if (!Ext.isDefined(sourceCfg[rec.getId()])) recs.push(rec);
				});
				if (recs.length > 0) sto.remove(recs);
				
				recs = [];
				Ext.iterate(pgp.sourceConfig, function(name, obj) {
					if (!sto.getById(name)) {
						recs.push(sto.createModel({name: name, value: obj.defaultValue}));
					}
				});
				if (recs.length > 0) sto.add(recs);
				
				keys = Ext.Object.getAllKeys(pgp.sourceConfig).reverse();
				sto.sort({
					sorterFn: function(rec1, rec2) {
                        var iof1 = keys.indexOf(rec1.getId()),
								iof2 = keys.indexOf(rec2.getId());
						return (iof1 > iof2) ? -1 : ((iof1 < iof2) ? 1 : 0);
					},
					desc: 'ASC'
				});
			}
			
			if (me.isMode(me.MODE_EDIT) && ov) {
				if (['text', 'textarea'].indexOf(nv) !== -1 && ['text', 'textarea'].indexOf(ov) === -1) {
					WT.warn(me.mys.res('customField.fld-type.confirm'));
				}
			}
		},

		onEnableValuesChange: function(nv, ov) {
			if (!nv) this.lref('tabmain').getLayout().setActiveItem('props');
		},

		generateSourceConfig: function(type) {
			var props = this.typeProps[type],
					sourceConfig = {}, obj;

			Ext.Object.each(props, function(propName, propData) {
				obj = {};
				['displayName', 'type', 'defaultValue'].forEach(function(name) {
					if (propData[name] !== undefined) obj[name] = propData[name];
				});
				sourceConfig[propName] = obj;
			});
			return sourceConfig;
		}
	}
});
