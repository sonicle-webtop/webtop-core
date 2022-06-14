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
Ext.define('Sonicle.webtop.core.ux.panel.CustomFieldsEditor', {
	alternateClassName: 'WTA.ux.panel.CustomFieldsEditor',
	extend: 'Sonicle.webtop.core.ux.panel.CustomFieldsBase',
	alias: 'widget.wtcfieldseditorpanel',
	requires: [
		'Sonicle.String',
		'Sonicle.plugin.FieldTooltip',
		'Sonicle.form.field.Tag'
	],
	
	constructor: function(cfg) {
		var me = this;
		cfg.emptyItemTitle = WT.res('wtcfieldseditorpanel.empty.tit');
		cfg.emptyItemText = WT.res('wtcfieldseditorpanel.empty.txt');
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		if (me.mainView) {
			me.mainView.on('beforemodelvalidate', me.onMainBeforeModelValidate, me);
		}
	},
	
	doDestroy: function() {
		var me = this;
		if (me.mainView) {
			me.mainView.un('beforemodelvalidate', me.onMainBeforeModelValidate, me);
		}
		me.callParent();
	},
	
	isValid: function() {
		var pnl = this.getComponent(0);
		return pnl.isXType('form') ? pnl.isValid() : true;
	},
	
	createCustomFieldDef: function(panelId, field, cmpId) {
		var me = this,
			SoS = Sonicle.String,
			SoO = Sonicle.Object,
			ftype = field.type,
			fprops = field.props || {},
			flabel = field.label,
			//showTip = !Ext.isEmpty(field.desc),
			//parseWidth = function(s) { return Number.parseInt(s) + me.defaultLabelWidth; },
			dependsOn = {
				data: Ext.JSON.decode(fprops['dataDependsOn'], true)
			},
			disabledExpr = fprops['disabledExpr'],
			onChangeExpr = fprops['onChangeExpr'],
			formulas = {},
			bind = {},
			listeners = {scope: me},
			cfg,
			foName;
		
		//if (showTip) label += ' <i class="fas fa-info-circle" aria-hidden="true"></i>';
		
		// Append main value binding formula
		foName = me.buildFieldFormulaName('val', panelId, field.id, ftype);
		SoO.setProp(formulas, foName, me.createFieldValueFormula(field));
		bind['value'] = '{' + foName + '}';
		
		// If necessary, append disabled formula
		if (Ext.isString(disabledExpr)) {
			foName = me.buildFieldFormulaName('isDisabled', panelId, field.id, ftype);
			SoO.setProp(formulas, foName, me.createFieldExprFormula(field, disabledExpr));
			bind['disabled'] = '{' + foName + '}';
		}
		// If necessary, append select listener
		if (Ext.isString(onChangeExpr) && SoS.isIn(ftype, ['date', 'time', 'combobox', 'comboboxds', 'tag', 'tagds', 'contactpicker'])) {
			listeners['select'] = function(s, rec) {
				me.evaluateExpr(onChangeExpr, true, {functions: true, transforms: true});
			};
			listeners['clear'] = function(s, rec) {
				me.evaluateExpr(onChangeExpr, true, {functions: true, transforms: true});
			};
		}
		
		if ('text' === ftype) {
			cfg = {
				xtype: 'textfield',
				reference: cmpId,
				bind: bind,
				allowBlank: !(fprops['required'] === 'true'),
				msgTarget: 'side',
				fieldLabel: flabel,
				listeners: listeners
			};
			SoO.copyProp(cfg, false, fprops, 'emptyText');
			SoO.copyProp(cfg, false, fprops, 'minLength');
			SoO.copyProp(cfg, false, fprops, 'maxLength');
			SoO.copyProp(cfg, false, fprops, 'width', Number.parseInt);
			SoO.copyProp(cfg, false, fprops, 'anchor');
			
		} else if ('textarea' === ftype) {
			cfg = {
				xtype: 'textareafield',
				reference: cmpId,
				bind: bind,
				allowBlank: !(fprops['required'] === 'true'),
				msgTarget: 'side',
				fieldLabel: flabel,
				listeners: listeners
			};
			SoO.copyProp(cfg, false, fprops, 'emptyText');
			SoO.copyProp(cfg, false, fprops, 'minLength');
			SoO.copyProp(cfg, false, fprops, 'maxLength');
			SoO.copyProp(cfg, false, fprops, 'width', Number.parseInt);
			SoO.copyProp(cfg, false, fprops, 'anchor');
			SoO.copyProp(cfg, false, fprops, 'autoGrow', 'grow');
			
		} else if ('number' === ftype) {
			cfg = {
				xtype: 'numberfield',
				reference: cmpId,
				bind: bind,
				allowBlank: !(fprops['required'] === 'true'),
				msgTarget: 'side',
				fieldLabel: flabel,
				listeners: listeners
			};
			SoO.copyProp(cfg, false, fprops, 'emptyText');
			SoO.copyProp(cfg, false, fprops, 'minValue');
			SoO.copyProp(cfg, false, fprops, 'maxValue');
			SoO.copyProp(cfg, false, fprops, 'allowDecimals');
			SoO.copyProp(cfg, false, fprops, 'width', Number.parseInt);
			SoO.copyProp(cfg, false, fprops, 'anchor');
			
		} else if ('date' === ftype) {
			cfg = {
				xtype: 'datefield',
				reference: cmpId,
				bind: bind,
				startDay: WT.getStartDay(),
				format: WT.getShortDateFmt(),
				allowBlank: !(fprops['required'] === 'true'),
				msgTarget: 'side',
				fieldLabel: flabel,
				listeners: listeners
			};
			SoO.copyProp(cfg, false, fprops, 'emptyText');
			
		} else if ('time' === ftype) {
			cfg = {
				xtype: 'timefield',
				itemId: reference,
				bind: bind,
				format: WT.getShortTimeFmt(),
				allowBlank: !(fprops['required'] === 'true'),
				msgTarget: 'side',
				fieldLabel: flabel,
				listeners: listeners
			};
			SoO.copyProp(cfg, false, fprops, 'emptyText');
			
		} else if ('checkbox' === ftype) {
			cfg = {
				xtype: 'checkbox',
				reference: cmpId,
				bind: bind,
				hideEmptyLabel: true,
				boxLabel: flabel,
				listeners: listeners
			};
		
		} else if (SoS.isIn(ftype, ['combobox', 'comboboxds'])) {
			var pageSize = Sonicle.webtop.core.ux.panel.CustomFieldsBase.parseAsPageSize(fprops['pageSize']),
				queryable = fprops['queryable'] === 'true';
			
			cfg = {
				xtype: 'combo',
				reference: cmpId,
				bind: bind,
				valueField: 'field1',
				displayField: 'field2',
				allowBlank: !(fprops['required'] === 'true'),
				msgTarget: 'side',
				fieldLabel: flabel,
				listeners: listeners
			};
			
			if (pageSize !== null) { // Remote pagination
				Ext.apply(cfg, {
					typeAhead: true,
					queryMode: 'remote',
					forceSelection: true,
					selectOnFocus: true,
					triggerAction: 'all',
					pageSize: pageSize
					//https://fiddle.sencha.com/#view/editor&fiddle/22im
				});
				
			} else if (queryable) { // Combo with local filtering and forced selection
				Ext.apply(cfg, {
					typeAhead: true,
					queryMode: 'local',
					forceSelection: true,
					selectOnFocus: true,
					triggerAction: 'all'
				});
				
			} else { // Combo with forced selection and NO filtering capabilities
				Ext.apply(cfg, {
					editable: false,
					typeAhead: false,
					forceSelection: true,
					triggerAction: 'all'
				});
			}
			
			if ('comboboxds' === ftype) {
				Ext.apply(cfg, {
					autoLoadOnValue: true,
					autoLoadOnQuery: true,
					store: {
						type: 'array',
						//autoLoad: true,
						proxy: WTF.proxy(WT.ID, 'CustomFieldDataSourceQuery', null, {
							extraParams: {
								fieldServiceId: me.serviceId,
								fieldId: field.id,
								pagination: pageSize !== null
							}
						}),
						fields: ['field1', 'field2'],
						listeners: Ext.isObject(dependsOn.data) ? {beforeload: me.generateDependsOnBeforeLoadListener(ftype, dependsOn.data)} : {}
					}
				});
			} else {
				Ext.apply(cfg, {
					store: field.values
				});
			}
			if (cfg.allowBlank) {
				Ext.apply(cfg, {
					triggers: {
						clear: WTF.clearTrigger()
					}
				});
			}
			SoO.copyProp(cfg, false, fprops, 'emptyText');
			SoO.copyProp(cfg, false, fprops, 'width', Number.parseInt);
			SoO.copyProp(cfg, false, fprops, 'anchor');
			
		} else if (SoS.isIn(ftype, ['tag', 'tagds'])) {
			cfg = {
				xtype: 'sotagfield',
				reference: cmpId,
				bind: bind,
				valueField: 'field1',
				displayField: 'field2',
				createNewOnEnter: false,
				createNewOnBlur: false,
				filterPickList: true,
				forceSelection: true,
				allowBlank: !(fprops['required'] === 'true'),
				msgTarget: 'side',
				fieldLabel: flabel,
				listeners: listeners
			};
			if ('tagds' === ftype) {
				Ext.apply(cfg, {
					autoLoadOnValue: true,
					store: {
						type: 'array',
						autoLoad: true,
						proxy: WTF.proxy(WT.ID, 'CustomFieldDataSourceQuery', null, {
							extraParams: {
								fieldServiceId: me.serviceId,
								fieldId: field.id
							}
						}),
						fields: ['field1', 'field2'],
						listeners: Ext.isObject(dependsOn.data) ? {beforeload: me.generateDependsOnBeforeLoadListener(ftype, dependsOn.data)} : {}
					}
				});
			} else {
				Ext.apply(cfg, {
					store: field.values
				});
			}
			SoO.copyProp(cfg, false, fprops, 'emptyText');
			SoO.copyProp(cfg, false, fprops, 'width', Number.parseInt);
			SoO.copyProp(cfg, false, fprops, 'anchor');
			
		} else if ('contactpicker' === ftype && WT.hasService('com.sonicle.webtop.contacts')) {
			var categoryIds = SoS.split(fprops['contactPickerCategoryIds'], ','),
				displayTpl = fprops['displayTpl'],
				fieldCfg = {
					xtype: 'combobox',
					reference: cmpId,
					bind: bind,
					typeAhead: true,
					queryMode: 'remote',
					forceSelection: true,
					selectOnFocus: true,
					triggerAction: 'all',
					pageSize: 50,
					autoLoadOnValue: true,
					autoLoadOnQuery: true,
					valueField: 'id',
					displayField: 'name',
					store: {
						//autoLoad: true,
						model: 'Sonicle.webtop.contacts.model.ContactLkp', // Model MUST be loaded by Contact's service!
						proxy: WTF.proxy('com.sonicle.webtop.contacts', 'CustomFieldContactPicker', null, {
							extraParams: SoO.setProp({}, 'categoryIds', !Ext.isEmpty(categoryIds) ? Sonicle.Utils.toJSONArray(categoryIds) : undefined, true)
						}),
						listeners: Ext.isObject(dependsOn.data) ? {beforeload: me.generateDependsOnBeforeLoadListener(ftype, dependsOn.data)} : {}
					},
					listConfig: SoO.applyPairs({}, [Ext.isString(displayTpl) ? 'itemTpl' : null], [displayTpl]),
					triggers: {
						clear: WTF.clearTrigger()
					},
					listeners: listeners
				};
			SoO.copyProp(fieldCfg, false, fprops, 'emptyText');
			
			if (SoO.booleanValue(fprops['contactPickerNewButton'], true) === true) {
				var apiDataExpr = fprops['contactPickerAddContactApiDataExpr'];
				// Wraps field into a field container for button
				cfg = {
					xtype: 'fieldcontainer',
					layout: {
						type: 'hbox',
						padding: '0 0 1 0' // fixes classic-theme bottom border issue
					},
					items: [
						Ext.apply(fieldCfg, {
							margin: '0 5 0 0',
							flex: 1
						}),
						{
							xtype: 'button',
							ui: 'default-toolbar',
							tooltip: WT.res('store.customFieldType.contactpicker.add.tip'),
							iconCls: 'wt-icon-customField-contactpicker-add',
							handler: function() {
								var capi = WT.getServiceApi('com.sonicle.webtop.contacts');
								if (capi) {
									var data = {};
									if (Ext.isString(apiDataExpr)) {
										Ext.apply(data, SoO.objectValue(me.evaluateExpr(apiDataExpr, false, {silent: true, functions: true})[0], {}));
									}
									SoO.setProp(data, 'categoryId', !Ext.isEmpty(categoryIds) ? categoryIds[0] : undefined, true);
									capi.addContact(data, {
										dirty: true,
										callback: function(success) {
											if (success) {
												var cmp = me.getFieldsContainer().lookupReference(cmpId);
												if (cmp) cmp.getStore().reload();
											}
										}
									});
								}
							}
						}
					],
					fieldLabel: flabel
				};
				SoO.copyProp(cfg, false, fprops, 'width', Number.parseInt);
				SoO.copyProp(cfg, false, fprops, 'anchor');
				
			} else {
				cfg = Ext.apply(fieldCfg, {
					fieldLabel: flabel
				});
				SoO.copyProp(cfg, false, fprops, 'width', Number.parseInt);
				SoO.copyProp(cfg, false, fprops, 'anchor');
			}	
		}
		
		return {
			formulas: formulas,
			fieldCfg: cfg,
			dependsOn: dependsOn
		};
	},
	
	privates: {
		onMainBeforeModelValidate: function(s) {
			var me = this;
			if (me.cfCache) {
				Ext.iterate(me.cfCache.onBeforeSaveExprs, function(expr) {
					me.evaluateExpr(expr, true, {functions: true, transforms: true});
				});
			}
		},
		
		analyzeDefObject: function(defObj) {
			var result = this.callParent(arguments), obsExprs = [], expr;
			if (defObj && result) {
				// Collect exprs to run on beforeSave
				Ext.iterate(defObj.panels, function(panel, indx) {
					expr = panel.props['onBeforeSaveExpr'];
					if (Ext.isString(expr)) obsExprs.push(expr);
				});
				result.onBeforeSaveExprs = obsExprs;
			}
			return result;
		},

		cacheDefObjectResult: function(result) {
			var me = this;
			me.callParent(arguments);
			if (me.cfCache) {
				me.cfCache.onBeforeSaveExprs = result.onBeforeSaveExprs;
			}
		},
		
		createFieldValueFormula: function(field) {
			var bind = this.buildValueBindName(field.id);
			return Ext.apply(this.callParent(arguments), {
				set: function(val) {
					this.set(bind, val);
					var sto = this.getStore('cvalues'), rec;
					if (sto) {
						rec = sto.getById(field.id);
						if (rec) rec.setValue(val);
					}
				}
			});
		},

		createFieldExprFormula: function(field, expr) {
			var me = this;
			return {
				bind: {bindTo: '{values}', deep: true},
				get: function(val) {
					return Sonicle.Object.booleanValue(me.evaluateExpr(expr, false, {silent: true, functions: true})[0]);
				}
			};
		}
	}
});
