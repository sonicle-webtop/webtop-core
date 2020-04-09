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
Ext.define('Sonicle.webtop.core.ux.panel.CustomFields', {
	alternateClassName: 'WTA.ux.panel.CustomFields',
	extend: 'WTA.ux.panel.Panel',
	alias: 'widget.wtcustomfieldspanel',
	requires: [
		'Sonicle.plugin.FieldTooltip',
		'Sonicle.webtop.core.ux.data.CustomFieldValueModel'
	],
	mixins: [
		'WTA.mixin.Waitable'
	],
	
	layout: 'border',
	
	/**
	 * @cfg {Number} defaultLabelWidth
	 * The default value to be used as width of the {@link #fieldLabel} in pixels.
	 */
	defaultLabelWidth: 120,
	
	viewModel: {},
	
	constructor: function(cfg) {
		var me = this,
				icfg = me.getInitialConfig();
		
		if (me.defaultLabelWidth) {
			cfg.defaults = Ext.merge(icfg.defaults || {}, {
				labelWidth: me.defaultLabelWidth
			});
		}
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
	},
	
	setStore: function(store) {
		var me = this,
				vm = me.getViewModel(),
				stores = {};
		if (vm) {
			if (store) stores['values'] = {source: store, model: 'Sonicle.webtop.core.ux.data.CustomFieldValueModel'};
			vm.setStores(stores);
			if (store) {
				store.each(function(rec) {
					vm.set(me.buildFieldValueName(rec.getId()), rec.getValue());
				});
			}
		}
	},
	
	getStore: function() {
		var vm = this.getViewModel();
		return vm ? vm.getStores('values') : null;
	},
	
	setFieldsDefs: function(rawDefs) {
		var me = this,
				defObj = Ext.JSON.decode(rawDefs, true),
				createEmpty = true,
				items = [], formulas = {};
		
		if (defObj) {
			Ext.iterate(defObj.panels, function(panel, indx) {
				var pitems = [];
				pitems.push({
					xtype: 'soformseparator',
					title: panel.title
				});
				Ext.iterate(panel.fields, function(fieldId) {
					if (!defObj.fields[fieldId]) return;
					var cfObj = me.createCustomFieldDef(indx, defObj.fields[fieldId]);
					Ext.merge(formulas, cfObj.formulas);
					pitems.push(cfObj.fieldCfg);
				});
				if (pitems.length > 1) Ext.Array.push(items, pitems);
			});
			createEmpty = items.length === 0;
		}
		
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
	
	isValid: function() {
		var pnl = this.getComponent(0);
		return pnl.isXType('form') ? pnl.isValid() : true;
	},
	
	createFormPanelCfg: function(items) {
		return {
			xtype: 'wtform',
			region: 'center',
			scrollable: true,
			items: items
		};
	},
	
	createEmptyItemCfg: function() {
		return {
			xtype: 'wtpanel',
			region: 'center',
			layout: {
				type: 'vbox',
				pack: 'center',
				align: 'middle'
			},
			items: [{
				xtype: 'label',
				text: WT.res('customFieldsPanel.empty.tit'),
				cls: 'wt-theme-text-tit',
				style: 'font-size:1.2em'
			}, {
				xtype: 'label',
				text: WT.res('customFieldsPanel.empty.txt'),
				cls: 'wt-theme-text-sub',
				style: 'font-size:0.9em'
			}]
		};
	},
	
	createEmptyPanelItem: function() {
		return {
			xtype: 'container',
			layout: {
				type: 'vbox',
				pack: 'center',
				align: 'middle'
			},
			items: [{
				xtype: 'label',
				text: WT.res('customFieldsPanel.empty.tit'),
				cls: 'wt-theme-text-tit',
				style: 'font-size:1.2em'
			}, {
				xtype: 'label',
				text: WT.res('customFieldsPanel.empty.txt'),
				cls: 'wt-theme-text-sub',
				style: 'font-size:0.9em'
			}]
		};
	},
	
	createCustomFieldDef: function(panelId, field) {
		var me = this,
				appPro = WTU.applyProp,
				parseWidth = function(s) {
					return Number.parseInt(s) + me.defaultLabelWidth;
				},
				ftype = field.type,
				fprops = field.props || {},
				showTip = !Ext.isEmpty(field.desc),
				label = field.label,
				foObj = {}, cfgObj = {}, otype, cfg, fo;
		
		//if (showTip) label += ' <i class="fa fa-info-circle" aria-hidden="true"></i>';
		
		if (Sonicle.String.isIn(ftype, ['text', 'textarea', 'number', 'date', 'time', 'combobox'])) {
			cfg = {
				fieldLabel: label,
				msgTarget: 'side',
				allowBlank: !(fprops['required'] === 'true')
			};
			appPro(cfg, false, fprops, 'emptyText');
			
		} else {
			cfg = {};
		}
		
		if (showTip) {
			Ext.apply(cfg, {
				tooltip: field.desc,
				plugins: [{ptype: 'sofieldtooltip', tooltipTarget: 'label'}]
			});
		}
		
		if ('text' === ftype) {
			otype = 'text';
			fo = me.buildFieldFormulaName(panelId, field.id, otype);
			Ext.apply(cfg, {
				xtype: 'textfield',
				bind: '{' + fo + '}'
			});
			appPro(cfg, false, fprops, 'minLength');
			appPro(cfg, false, fprops, 'maxLength');
			appPro(cfg, false, fprops, 'width', Number.parseInt);
			appPro(cfg, false, fprops, 'anchor');
			foObj[otype] = {};
			foObj[otype][fo] = me.createCustomFieldFormula(field);
			cfgObj[otype] = cfg;
			
		} else if ('textarea' === ftype) {
			otype = 'textarea';
			fo = me.buildFieldFormulaName(panelId, field.id, otype);
			Ext.apply(cfg, {
				xtype: 'textareafield',
				bind: '{' + fo + '}'
			});
			appPro(cfg, false, fprops, 'minLength');
			appPro(cfg, false, fprops, 'maxLength');
			appPro(cfg, false, fprops, 'width', Number.parseInt);
			appPro(cfg, false, fprops, 'anchor');
			appPro(cfg, false, fprops, 'autoGrow', 'grow');
			foObj[otype] = {};
			foObj[otype][fo] = me.createCustomFieldFormula(field);
			cfgObj[otype] = cfg;
			
		} else if ('number' === ftype) {
			otype = 'number';
			fo = me.buildFieldFormulaName(panelId, field.id, otype);
			Ext.apply(cfg, {
				xtype: 'numberfield',
				bind: '{' + fo + '}'
			});
			appPro(cfg, false, fprops, 'minValue');
			appPro(cfg, false, fprops, 'maxValue');
			appPro(cfg, false, fprops, 'allowDecimals');
			appPro(cfg, false, fprops, 'width', Number.parseInt);
			appPro(cfg, false, fprops, 'anchor');
			foObj[otype] = {};
			foObj[otype][fo] = me.createCustomFieldFormula(field);
			cfgObj[otype] = cfg;
			
		} else if ('date' === ftype || 'datetime' === ftype) {
			otype = 'date';
			fo = me.buildFieldFormulaName(panelId, field.id, otype);
			Ext.apply(cfg, {
				xtype: 'datefield',
				bind: '{' + fo + '}',
				startDay: WT.getStartDay(),
				format: WT.getShortDateFmt()
			});
			foObj[otype] = {};
			foObj[otype][fo] = me.createCustomFieldFormula(field);
			cfgObj[otype] = cfg;
			
		} else if ('time' === ftype || 'datetime' === ftype) {
			otype = 'time';
			fo = me.buildFieldFormulaName(panelId, field.id, otype);
			Ext.apply(cfg, {
				xtype: 'timefield',
				bind: '{' + fo + '}',
				format: WT.getShortTimeFmt()
			});
			foObj[otype] = {};
			foObj[otype][fo] = me.createCustomFieldFormula(field);
			cfgObj[otype] = cfg;
			
		} else if ('datetime' === ftype) {
			Ext.apply(cfg, {
				xtype: 'fieldcontainer',
				fieldLabel: label,
				combineErrors: true,
				msgTarget: 'side',
				layout: 'hbox',
				defaults: {
					//margin: '0 10 0 0'
					hideLabel: true,
					flex: 1
				},
				items: [
					Ext.apply(cfgObj['date'], {
						allowBlank: cfg.allowBlank
						//margin: '0 5 0 0'
						//width: 105
					}),
					Ext.apply(cfgObj['time'], {
						allowBlank: cfg.allowBlank,
						padding: '0 0 0 10'
						//margin: '0 5 0 0'
						//width: 80
					})
				]
			});
			cfgObj['datetime'] = cfg;
			
		} else if ('combobox' === ftype) {
			otype = 'combobox';
			fo = me.buildFieldFormulaName(panelId, field.id, otype);
			Ext.apply(cfg, WTF[fprops['queryable'] === 'true' ? 'localCombo' : 'lookupCombo']('field1', 'field2', {
				bind: '{' + fo + '}',
				store: field.values
			}));
			if (cfg.allowBlank) {
				Ext.apply(cfg, {
					triggers: {
						clear: WTF.clearTrigger()
					}
				});
			}
			appPro(cfg, false, fprops, 'width', Number.parseInt);
			appPro(cfg, false, fprops, 'anchor');
			foObj[otype] = {};
			foObj[otype][fo] = me.createCustomFieldFormula(field);
			cfgObj[otype] = cfg;
			
		} else if ('checkbox' === ftype) {
			otype = 'checkbox';
			fo = me.buildFieldFormulaName(panelId, field.id, otype);
			Ext.apply(cfg, {
				xtype: 'checkbox',
				bind: '{' + fo + '}',
				hideEmptyLabel: true,
				boxLabel: label
			});
			foObj[otype] = {};
			foObj[otype][fo] = me.createCustomFieldFormula(field);
			cfgObj[otype] = cfg;
		}
		
		return {
			formulas: foObj[ftype],
			fieldCfg: cfgObj[ftype]
		};
	},
	
	createCustomFieldFormula: function(field) {
		var valueName = this.buildFieldValueName(field.id);
		if ('datetime' === field.type) {
			//TODO: add support
			/*
			return {
				bind: {bindTo: '{' + valueName + '}'},
				get: function(val) {
					return val;
				},
				set: function(val) {
					if (val !== null) 
					
					this.set(valueName, val);
					var sto = this.getStore('values'), rec;
					if (sto) {
						rec = sto.getById(field.id);
						if (rec) rec.setValue(val);
					}
				}
			};
			*/
			
		} else {
			return {
				bind: {bindTo: '{' + valueName + '}'},
				get: function(val) {
					return val;
				},
				set: function(val) {
					this.set(valueName, val);
					var sto = this.getStore('values'), rec;
					if (sto) {
						rec = sto.getById(field.id);
						if (rec) rec.setValue(val);
					}
				}
			};
		}
	},
	
	/*
	setFieldsDefs: function(rawDefs) {
		var me = this,
				defObj = Ext.JSON.decode(rawDefs, true),
				items = [], formulas = {}, pitems;
		
		if (!defObj) return;
		
		Ext.iterate(defObj.panels, function(panel, indx) {
			pitems = [];
			pitems.push({
				xtype: 'soformseparator',
				title: panel.title
			});
			Ext.iterate(panel.fields, function(fieldId) {
				if (!defObj.fields[fieldId]) return;
				var cfObj = me.createCustomFieldDef(indx, defObj.fields[fieldId]);
				Ext.merge(formulas, cfObj.formulas);
				pitems.push(cfObj.fieldCfg);
			});
			if (pitems.length > 1) Ext.Array.push(items, pitems);
		});
		
		me.removeAll();
		me.getViewModel().setFormulas(formulas);
		me.add(items);
		
		if (items.length === 0) {
			me.add(me.createEmptyPanelItem());
			me.setLayout('fit');
			me.setScrollable(false);
		}
	},
	*/
	
	privates: {
		buildFieldValueName: function(fieldId) {
			return 'value-' + fieldId;
		},
		
		buildFieldFormulaName: function(panelId, fieldId, type) {
			return type + Ext.String.leftPad(panelId, 2, '0') + fieldId;
		}
	}
});
