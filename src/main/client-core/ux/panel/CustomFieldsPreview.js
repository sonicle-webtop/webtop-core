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
Ext.define('Sonicle.webtop.core.ux.panel.CustomFieldsPreview', {
	alternateClassName: 'WTA.ux.panel.CustomFieldsPreview',
	extend: 'Sonicle.webtop.core.ux.panel.CustomFieldsBase',
	alias: 'widget.wtcfieldspreviewpanel',
	
	viewModel: {
		shows: {}
	},
	
	constructor: function(cfg) {
		var me = this;
		cfg.emptyItemTitle = WT.res('wtcfieldspreviewpanel.empty.tit');
		cfg.emptyItemText = WT.res('wtcfieldspreviewpanel.empty.txt');
		me.callParent([cfg]);
	},
	
	/*
	applyCValues: function(vm, fields, values) {
		this.callParent(arguments);
		vm.set('shows', this.buildShowsData(fields));
	},
	 */
	
	createCustomFieldDef: function(panelId, field, cmpId) {
		var me = this,
			SoO = Sonicle.Object,
			SoS = Sonicle.String,
			ftype = field.type,
			fprops = field.props || {},
			flabel = field.label,
			valFoName = me.buildFieldFormulaName('val', panelId, field.id, ftype),
			//hidFoName = me.buildFieldFormulaName('hid', panelId, field.id, ftype),dependsOn = {
			dependsOn = {
				data: Ext.JSON.decode(fprops['dataDependsOn'], true)
			},
			formulas = {},
			cfg = {
				itemId: cmpId,
				readOnly: true,
				editable: false,
				labelAlign: 'top',
				anchor: '100%'
			};
		
		if ('text' === ftype) {
			Ext.apply(cfg, {
				xtype: 'textfield',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				fieldLabel: flabel
			});
			
		} else if ('textarea' === ftype) {
			Ext.apply(cfg, {
				xtype: 'textareafield',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				autoGrow: 'grow',
				fieldLabel: flabel
			});
			
		} else if ('number' === ftype) {
			Ext.apply(cfg, {
				xtype: 'numberfield',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				fieldLabel: flabel
			});
			SoO.copyProp(cfg, false, fprops, 'allowDecimals');
			
		} else if ('date' === ftype) {
			Ext.apply(cfg, {
				xtype: 'datefield',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				startDay: WT.getStartDay(),
				format: WT.getShortDateFmt(),
				fieldLabel: flabel
			});
			
		} else if ('time' === ftype) {
			Ext.apply(cfg, {
				xtype: 'timefield',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				format: WT.getShortTimeFmt(),
				fieldLabel: flabel
			});
			
		} else if ('checkbox' === ftype) {
			Ext.apply(cfg, {
				xtype: 'checkbox',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				hideEmptyLabel: true,
				boxLabel: flabel
			});
			
		} else if ('combobox' === ftype) {
			Ext.apply(cfg, {
				xtype: 'combo',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				typeAhead: false,
				forceSelection: true,
				selectOnFocus: false,
				store: field.values,
				valueField: 'field1',
				displayField: 'field2',
				fieldLabel: flabel
			});
			
		} else if ('comboboxds' === ftype) {
			var pageSize = Sonicle.webtop.core.ux.panel.CustomFieldsBase.parseAsPageSize(fprops['pageSize']);
			Ext.apply(cfg, {
				xtype: 'combo',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				typeAhead: false,
				forceSelection: true,
				selectOnFocus: false,
				autoLoadOnValue: true,
				pageSize: pageSize || 0,
				store: {
					type: 'array',
					proxy: WTF.proxy(WT.ID, 'CustomFieldDataSourceQuery', null, {
						extraParams: {
							fieldServiceId: me.serviceId,
							fieldId: field.id
						}
					}),
					fields: ['field1', 'field2'],
					listeners: Ext.isObject(dependsOn.data) ? {beforeload: me.generateDependsOnBeforeLoadListener(ftype, dependsOn.data)} : {}
				},
				valueField: 'field1',
				displayField: 'field2',
				fieldLabel: flabel
			});
			
		} else if ('tag' === ftype) {
			Ext.apply(cfg, {
				xtype: 'sotagfield',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				createNewOnEnter: false,
				createNewOnBlur: false,
				selectOnFocus: false,
				store: field.values,
				valueField: 'field1',
				displayField: 'field2',
				fieldLabel: flabel
			});
			
		} else if ('tagds' === ftype) {
			Ext.apply(cfg, {
				xtype: 'sotagfield',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				createNewOnEnter: false,
				createNewOnBlur: false,
				selectOnFocus: false,
				autoLoadOnValue: true,
				store: {
					type: 'array',
					proxy: WTF.proxy(WT.ID, 'CustomFieldDataSourceQuery', null, {
						extraParams: {
							fieldServiceId: me.serviceId,
							fieldId: field.id
						}
					}),
					fields: ['field1', 'field2'],
					listeners: Ext.isObject(dependsOn.data) ? {beforeload: me.generateDependsOnBeforeLoadListener(ftype, dependsOn.data)} : {}
				},
				valueField: 'field1',
				displayField: 'field2',
				fieldLabel: flabel
			});
			
		} else if ('contactpicker' === ftype && WT.hasService('com.sonicle.webtop.contacts')) {
			var categoryIds = SoS.split(fprops['contactPickerCategoryIds'], ',');
			Ext.apply(cfg, {
				xtype: 'combobox',
				bind: {
					value: '{' + valFoName + '}'
					//hidden: '{'+ hidFoName + '}'
				},
				typeAhead: false,
				forceSelection: true,
				selectOnFocus: false,
				autoLoadOnValue: true,
				autoLoadOnQuery: true,
				pageSize: 50,
				store: {
					model: 'Sonicle.webtop.contacts.model.ContactLkp', // Model MUST be loaded by Contact's service!
					proxy: WTF.proxy('com.sonicle.webtop.contacts', 'CustomFieldContactPicker', null, {
						extraParams: SoO.setProp({}, 'categoryIds', !Ext.isEmpty(categoryIds) ? Sonicle.Utils.toJSONArray(categoryIds) : undefined, true)
					}),
					listeners: Ext.isObject(dependsOn.data) ? {beforeload: me.generateDependsOnBeforeLoadListener(ftype, dependsOn.data)} : {}
				},
				valueField: 'id',
				displayField: 'displayName',
				fieldLabel: flabel
			});
			
		} else {
			cfg = Ext.apply(cfg, {
				fieldLabel: flabel
			});
		}
		
		SoO.setProp(formulas, valFoName, me.createFieldValueFormula(field));
		//SoO.setProp(formulas, hidFoName, me.createFieldHiddenFormula(field));
		return {
			formulas: formulas,
			fieldCfg: cfg
		};
	}
	
	/*
	privates: {
		generateDependsOnBeforeLoadListener: function(fid, ftype, dependsOn) {
			var me = this,
				WTCFB = Sonicle.webtop.core.ux.panel.CustomFieldsBase;
			
			if ('contactpicker' === ftype) {
				return function(s) {
					var fieldId = me.getFieldIdByName(dependsOn.parentField),
						vm = me.getViewModel();
					if (fieldId) {
						Ext.callback(WTCFB.generateContactPickerStoreOnBeforeLoadDoFn(dependsOn.keyword), s, [s, vm.get(me.buildValueBindName(fieldId)), vm.get(me.buildValueBindName(fid))]);
					}
				};
			} else {
				return me.callParent(arguments);
			}
		}
	}
	*/
});
