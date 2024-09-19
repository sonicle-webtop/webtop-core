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
Ext.define('Sonicle.webtop.core.ux.panel.CustomFieldsBase', {
	extend: 'WTA.ux.panel.Panel',
	requires: [
		'Sonicle.Object',
		'Sonicle.String',
		'Sonicle.form.field.Tag',
		'Sonicle.webtop.core.ux.data.CustomFieldValueModel'
	],
	uses: [
		'Sonicle.Utils'
	],
	mixins: [
		'WTA.mixin.Waitable'
	],
	
	layout: 'fit',
	
	/**
	 * @cfg {Number} defaultLabelWidth
	 * The default value to be used as width of the {@link #fieldLabel} in pixels.
	 */
	defaultLabelWidth: 120,
	
	emptyItemTitle: '',
	emptyItemText: '',
	
	viewModel: {
		values: {}
	},
	
	/**
	 * @cfg {String} serviceId
	 * Target service ID for which managing fields.
	 */
	serviceId: null,
	
	/**
	 * @cfg {WTA.sdk.ModelView} mainView
	 * Reference to main ModelView owning this custom-fields.
	 * Optional: only needed for evaluating some expression (Jexl) formulas, 
	 * tipically used only in editor components.
	 */
	
	/**
	 * @property {Object} cfCache
	 * @readonly
	 */
	
	/**
	 * @property {Object} jexlCache
	 * @readonly
	 */
	
	createCustomFieldDef: Ext.emptyFn,
	
	initComponent: function() {
		var me = this;
		me.jexlCache = {};
		me.callParent(arguments);
		if (me.fieldDefs) {
			me.setFieldsDefs(me.fieldDefs);
		}
	},
	
	doDestroy: function() {
		var me = this;
		delete me.mainView;
		delete me.cfCache;
		delete me.jexlCache;
		me.callParent();
	},
	
	getStore: function() {
		var vm = this.getViewModel();
		return vm ? vm.getStores('cvalues') : null;
	},
	
	setStore: function(store) {
		var me = this,
			vm = me.getViewModel(),
			stores = {}, fieldsWithValue = [], values = {};
		if (vm) {
			if (store) stores['cvalues'] = {source: store, model: 'Sonicle.webtop.core.ux.data.CustomFieldValueModel'};
			vm.setStores(stores);
			if (store) {
				store.each(function(rec) {
					fieldsWithValue.push(rec.getId());
					values[rec.getId()] = rec.getValue();
				});
			}
			me.applyCValues(vm, fieldsWithValue, values);
		}
	},
	
	applyCValues: function(vm, fields, values) {
		vm.set('values', values);
	},
	
	setFieldsDefs: function(rawDefs) {
		var me = this,
			result = me.analyzeDefObject(Ext.JSON.decode(rawDefs, true)),
			createEmpty = result.items.length === 0;
		
		me.cacheDefObjectResult(result);
		
		Ext.suspendLayouts();
        me.removeAll();
		if (createEmpty) {
			me.add(me.createEmptyItemCfg());
		} else {
			var vm = me.getViewModel(),
				bindFieldIds = Ext.Object.getKeys(me.cfCache.dataDependsMap);
			
			// This relies on bindFieldIds to be generic: for now ids refers only 
			// to data-dependency but maybe in future binding can be wider adopted.
			Ext.iterate(bindFieldIds, function(fieldId) {
				vm.bind('{' + me.buildValueBindName(fieldId) + '}', function(nv, ov, b) {
					if (ov !== undefined) {
						// Handle reloaders (dataDepends)
						var reloader = me.cfCache.reloadersMap[b.stub.name];
						if (reloader) {
							reloader(b.stub.name, nv, me.cfCache.dataDependsMap[b.stub.name]);
						}
					}
				}, me);
			});
			vm.setFormulas(result.formulas);
			me.add(me.createFormPanelCfg(result.items));
		}
		Ext.resumeLayouts(true);
		
		if (result.prioritize) {
			me.fireEvent('prioritize', me);
		}
		
		/*
		Ext.defer(function() { // Run async in order to avoid raise of "Cannot have multiple center regions..."
			if (createEmpty) {
				me.add(me.createEmptyItemCfg());
			} else {
				me.getViewModel().setFormulas(formulas);
				me.add(me.createFormPanelCfg(items));
			}
			Ext.resumeLayouts(true);
		}, 0);
		*/
	},
	
	/**
	 * Gets the container component.
	 * @return {Ext.Component}
	 */
	getFieldsContainer: function() {
		return this.getComponent(0);
	},
	
	privates: {
		analyzeDefObject: function(defObj) {
			var me = this,
				SoO = Sonicle.Object,
				result = {
					formulas: {},
					fieldsMap: {},
					dataDependsMap: {},
					items: []
				};

			if (defObj) {
				Ext.iterate(defObj.panels, function(panel, indx) {
					var pitems = [];
					pitems.push({
						xtype: 'soformseparator',
						title: panel.title
					});
					if (SoO.booleanValue(panel.props['priority'], false) === true) result.prioritize = true;
					Ext.iterate(panel.fields, function(fieldId) {
						var fieldDef = defObj.fields[fieldId], cmpId, cret, mapobj;
						if (!fieldDef) return;

						cmpId = me.buildFieldCmpId(indx, fieldDef.id);
						cret = me.createCustomFieldDef(indx, fieldDef, cmpId);
						if (cret) {
							Ext.merge(result.formulas, cret.formulas);
							pitems.push(cret.fieldCfg);

							mapobj = result.fieldsMap[fieldId] || {id: fieldId, name: fieldDef.name, type: fieldDef.type, cmpIds: []};
							mapobj.cmpIds.push(cmpId);
							result.fieldsMap[fieldId] = mapobj;
							if (cret.dependsOn) {
								if (cret.dependsOn.data) {
									SoO.multiValueMapPut(result.dataDependsMap, cret.dependsOn.data.parentField, fieldId);
								}
							}
						}
					});
					if (pitems.length > 1) Ext.Array.push(result.items, pitems);
				});
			}
			return result;
		},

		cacheDefObjectResult: function(result) {
			var me = this,
				dataDependsMap = {},
				reloadersMap = {};

			// Remap collected data-dependencies with real fieldIds
			dataDependsMap = Sonicle.Object.remap(result.dataDependsMap, true, false, function(key) {
				return me.getFieldIdByName(key, result.fieldsMap);
			}, me);

			// Prepare buffered methods for reloading depending fields
			reloadersMap = {};
			Ext.iterate(dataDependsMap, function(parentId) {
				reloadersMap[parentId] = Ext.Function.createBuffered(me.reloadDependingFields, 100, me);
			});

			me.cfCache = {
				fieldsMap: result.fieldsMap,
				dataDependsMap: dataDependsMap,
				reloadersMap: reloadersMap
			};
		},
		
		createEmptyItemCfg: function() {
			return {
				xtype: 'wtpanel',
				layout: {
					type: 'vbox',
					pack: 'center',
					align: 'middle'
				},
				items: [{
					xtype: 'label',
					text: this.emptyItemTitle,
					cls: 'wt-form-body-title wt-theme-text-color-title'
				}, {
					xtype: 'label',
					text: this.emptyItemText,
					cls: 'wt-form-body-subtitle wt-theme-text-color-subtitle'
				}]
			};
		},

		createFormPanelCfg: function(items) {
			return {
				xtype: 'wtform',
				referenceHolder: true,
				scrollable: true,
				items: items
			};
		},

		createFieldValueFormula: function(field) {
			var bind = this.buildValueBindName(field.id);
			return {
				bind: {bindTo: '{' + bind + '}'},
				get: function(val) {
					return val;
				}
			};
		},

		createFieldHiddenFormula: function(field) {
			var bind = this.buildShowBindName(field.id);
			return {
				bind: {bindTo: '{' + bind + '}'},
				get: function(val) {
					return val === true ? false : true;
				}
			};
		},
		
		getFieldIdByName: function(name, map) {
			var ret;
			Ext.iterate(map || this.cfCache.fieldsMap, function(key, obj) {
				if (name === obj['name']) {
					ret = key;
					return false;
				}
			});
			return ret;
		},
		
		generateDependsOnBeforeLoadListener: function(ftype, dependsOn) {
			var me = this,
				SoS = Sonicle.String,
				WTCFB = Sonicle.webtop.core.ux.panel.CustomFieldsBase;
			
			if (SoS.isIn(ftype, ['comboboxds', 'tagds'])) {
				return function(s) {
					var fieldId = me.getFieldIdByName(dependsOn.parentField);
					if (fieldId) {
						Ext.callback(WTCFB.generateDSFieldsStoreOnBeforeLoadDoFn(dependsOn.placeholder), s, [s, me.getViewModel().get(me.buildValueBindName(fieldId))]);
					}
				};
				
			} else if ('contactpicker' === ftype) {
				return function(s) {
					var fieldId = me.getFieldIdByName(dependsOn.parentField);
					if (fieldId) {
						Ext.callback(WTCFB.generateContactPickerStoreOnBeforeLoadDoFn(dependsOn.keyword), s, [s, me.getViewModel().get(me.buildValueBindName(fieldId))]);
					}
				};
				
			} else {
				return null;
			}
		},
		
		reloadDependingFields: function(parentFieldId, parentValue, childFieldIds) {
			var me = this,
				fcnt = me.getFieldsContainer();
			Ext.iterate(childFieldIds, function(fieldId) {
				var entry = me.cfCache.fieldsMap[fieldId];
				if (entry) {
					Ext.iterate(entry.cmpIds, function(cmpId) {
						var cmp = fcnt.lookupReference(cmpId);
						if (cmp && cmp.getStore) {
							if (Ext.isEmpty(parentValue)) {
								cmp.getStore().clearData();
							} else {
								cmp.getStore().load();
							}
						}
					});
				}
			});
		},
		
		evaluateExpr: function(expr, multiline, opts) {
			opts = opts || {};
			var me = this,
				context = opts.context || {},
				jexl = me.getExprEvaluator({functions: opts.functions, transforms: opts.transforms});

			Ext.iterate(me.cfCache.fieldsMap, function(key, entry) {
				context['$'+entry.name] = key;
			});
			return WTA.ux.picker.CustomFieldExpr.evalExpr(expr, multiline, jexl, context, opts.silent);
		},
		
		getExprEvaluator: function(opts) {
			opts = opts || {};
			var me = this,
				flags = 0,
				key, jexl;

			if (opts.functions === true) flags |= 1;
			if (opts.transforms === true) flags |= 2;
			key = Sonicle.Object.stringValue(flags);
			if (!(jexl = me.jexlCache[key])) {
				jexl = me.jexlCache[key] = new Jexl.Jexl();
				
				// Functions...
				if (opts.functions === true) {
					jexl.addFunction('format', function(pattern, args) {
						return Ext.String.format.apply(this, [pattern].concat(args));
					});
					jexl.addFunction('getMainValue', function(fieldName) {
						return me.exprFnGetMainModelValue(fieldName);
					});
					jexl.addFunction('getValue', function(fieldId) {
						return me.exprFnGetVMValue(fieldId);
					});
					jexl.addFunction('getFieldValue', function(fieldId) {
						var cmp = me.exprFnGetFieldComponent(fieldId);
						if (!cmp) throw '[getFieldValue] Field component "' + fieldId + '" not found';
						return cmp.getValue();
					});
					jexl.addFunction('getFieldDisplayValue', function(fieldId) {
						var cmp = me.exprFnGetFieldComponent(fieldId);
						if (!cmp) throw '[getFieldDisplayValue] Field component "' + fieldId + '" not found';
						return cmp.getRawValue();
					});
					jexl.addFunction('getFieldSelection', function(fieldId) {
						var cmp = me.exprFnGetFieldComponent(fieldId), sel, data;
						if (!cmp) throw '[getFieldSelection] Field component "' + fieldId + '" not found';
						if (Ext.isFunction(cmp.getSelection) && (sel = cmp.getSelection())) {
							data = sel.getData();
						}
						return data;
					});
				}

				// Transforms...
				if (opts.transforms === true) {
					jexl.addTransform('alert', function(message, type, title) {
						var opts = Ext.isString(title) ? {title: title} : undefined;
						if ('info' === type) {
							WT.info(message, opts);
						} else {
							WT.warn(message, opts);
						}
					});
					jexl.addTransform('setFieldValue', function(value, fieldId) {
						var cmp = me.exprFnGetFieldComponent(fieldId);
						if (!cmp) throw '[setFieldValue] Field component "' + fieldId + '" not found';
						cmp.setValue(value);
						return value;
					});
					jexl.addTransform('setMainValue', function(value, fieldName) {
						me.exprFnSetMainModelValue(fieldName, value);
						return value;
					});
				}
			}
			return jexl;
		},
	
		exprFnGetVMValue: function(fieldId) {
			return this.getViewModel().get(this.buildValueBindName(fieldId));
		},

		exprFnGetFieldComponent: function(fieldId) {
			var me = this,
				field = me.cfCache.fieldsMap[fieldId];
			return field ? me.getFieldsContainer().lookupReference(field.cmpIds[0]) : undefined;
		},
		
		exprFnGetMainModelValue: function(fieldId) {
			var mmo = this.mainView ? this.mainView.getModel() : null;
			return mmo ? mmo.get(fieldId) : null;
		},
		
		exprFnSetMainModelValue: function(fieldId, value) {
			var mmo = this.mainView ? this.mainView.getModel() : null;
			return mmo ? mmo.set(fieldId, value) : null;
		},
		
		buildValueBindName: function(fieldId) {
			return 'values.' + fieldId;
		},
		
		buildShowBindName: function(fieldId) {
			return 'shows.' + fieldId;
		},
		
		buildFieldCmpId: function(panelId, fieldId) {
			return 'cmp' + Ext.String.leftPad(panelId, 2, '0') + fieldId;
		},
		
		buildFieldFormulaName: function(prefix, panelId, fieldId, ftype) {
			var cptlz = Ext.String.capitalize;
			return 'fo' + cptlz(prefix) + cptlz(ftype) + Ext.String.leftPad(panelId, 2, '0') + fieldId;
		}
		
		/*
		buildShowsData: function(fieldsWithValue) {
			var me = this, shows = {};
			if (Ext.isArray(me.defFields)) {
				Ext.iterate(me.defFields, function(fieldId) {
					shows[fieldId] = fieldsWithValue.indexOf(fieldId) !== -1;
				});
			}
			return shows;
		}
		*/
	},
	
	statics: {
		parseAsPageSize: function(value) {
			var pageSize = Number.parseInt(value, 10);
			return (Ext.isNumber(pageSize) && pageSize > 0 && pageSize <= 1000) ? pageSize : null;
		},
		
		generateDSFieldsStoreOnBeforeLoadDoFn: function(placeholderName) {
			return function(store, placeholderValue) {
				WTU.applyExtraParams(store, {
					placeholders: Ext.JSON.encode(Sonicle.Object.setProp({}, placeholderName, placeholderValue))
				});
			};
		},

		generateContactPickerStoreOnBeforeLoadDoFn: function(queryObjKeyword) {
			return function(store, queryObjValue) {
				if (queryObjValue) {
					WTU.applyExtraParams(store, {
						queryObj: Ext.JSON.encode({conditions: [{
						keyword: queryObjKeyword,
						value: queryObjValue,
						negated: false
						}]})
					});
				}
			};
		}
		
		/*
		generateContactPickerStoreOnBeforeLoadDoFn: function(queryObjKeyword) {
			return function(store, queryObjValue, idValue) {
				if (queryObjValue) {
					var conds = [];
					if (!Ext.isEmpty(idValue)) {
						conds.push({
							keyword: 'id',
							value: idValue,
							negated: false
						});
					}
					conds.push({
						keyword: queryObjKeyword,
						value: queryObjValue,
						negated: false
					});
					WTU.applyExtraParams(store, {
						queryObj: Ext.JSON.encode({conditions: conds})
					});
				}
			};
		}
		*/
	}
});
