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
		'Sonicle.webtop.core.ux.data.CustomFieldValueModel'
	],
	uses: [
		'Sonicle.Utils'
	],
	mixins: [
		'WTA.mixin.Waitable'
	],
	
	//layout: 'border',
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
	
	createCustomFieldDef: Ext.emptyFn,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		if (me.fieldDefs) {
			me.setFieldsDefs(me.fieldDefs);
		}
	},
	
	buildShowsData: function(fieldsWithValue) {
		var me = this, shows = {};
		if (Ext.isArray(me.defFields)) {
			Ext.iterate(me.defFields, function(fieldId) {
				shows[fieldId] = fieldsWithValue.indexOf(fieldId) !== -1;
			});
		}
		return shows;
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
	
	getStore: function() {
		var vm = this.getViewModel();
		return vm ? vm.getStores('cvalues') : null;
	},
	
	setFieldsDefs: function(rawDefs) {
		var me = this,
				defObj = Ext.JSON.decode(rawDefs, true),
				createEmpty = true,
				items = [], formulas = {}, defFields = [];
		
		if (defObj) {
			Ext.iterate(defObj.panels, function(panel, indx) {
				var pitems = [];
				pitems.push({
					xtype: 'soformseparator',
					title: panel.title
				});
				Ext.iterate(panel.fields, function(fieldId) {
					if (!defObj.fields[fieldId]) return;
					var ret = me.createCustomFieldDef(indx, defObj.fields[fieldId]);
					if (ret) {
						Ext.merge(formulas, ret.formulas);
						pitems.push(ret.fieldCfg);
						defFields.push(fieldId);
					}
				});
				if (pitems.length > 1) Ext.Array.push(items, pitems);
			});
			createEmpty = items.length === 0;
		}
		
		me.defFields = defFields;
		Ext.suspendLayouts();
        me.removeAll();
		Ext.defer(function() { // Run async in order to avoid raise of "Cannot have multiple center regions..."
			if (createEmpty) {
				me.add(me.createEmptyItemCfg());
			} else {
				me.getViewModel().setFormulas(formulas);
				me.add(me.createFormPanelCfg(items));
			}
			Ext.resumeLayouts(true);
		}, 0);
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
				cls: 'wt-theme-text-tit',
				style: 'font-size:1.2em'
			}, {
				xtype: 'label',
				text: this.emptyItemText,
				cls: 'wt-theme-text-sub',
				style: 'font-size:0.9em'
			}]
		};
	},
	
	createFormPanelCfg: function(items) {
		return {
			xtype: 'wtform',
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
	
	privates: {
		buildValueBindName: function(fieldId) {
			return 'values.' + fieldId;
		},
		
		buildShowBindName: function(fieldId) {
			return 'shows.' + fieldId;
		},
		
		buildFieldFormulaName: function(prefix, panelId, fieldId, ftype) {
			return 'fo-' + prefix + '-' + ftype + Ext.String.leftPad(panelId, 2, '0') + fieldId;
		}
	}
});
