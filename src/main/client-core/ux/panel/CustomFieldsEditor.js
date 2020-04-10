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
		'Sonicle.plugin.FieldTooltip'
	],
	
	constructor: function(cfg) {
		var me = this;
		
		cfg.emptyItemTitle = WT.res('wtcfieldseditorpanel.empty.tit');
		cfg.emptyItemText = WT.res('wtcfieldseditorpanel.empty.txt');
		me.callParent([cfg]);
	},
	
	isValid: function() {
		var pnl = this.getComponent(0);
		return pnl.isXType('form') ? pnl.isValid() : true;
	},
	
	createCustomFieldDef: function(panelId, field) {
		var me = this,
				SU = Sonicle.Utils,
				ftype = field.type,
				fprops = field.props || {},
				flabel = field.label,
				//showTip = !Ext.isEmpty(field.desc),
				foName = me.buildFieldFormulaName(panelId, field.id, ftype),
				fo = me.createCustomFieldFormula(field),
				//parseWidth = function(s) { return Number.parseInt(s) + me.defaultLabelWidth; },
				cfg;
		
		//if (showTip) label += ' <i class="fa fa-info-circle" aria-hidden="true"></i>';
		
		if (Sonicle.String.isIn(ftype, ['text', 'textarea', 'number', 'date', 'time', 'combobox'])) {
			cfg = {
				fieldLabel: flabel,
				msgTarget: 'side',
				allowBlank: !(fprops['required'] === 'true')
			};
			SU.applyProp(cfg, false, fprops, 'emptyText');
			
		} else {
			cfg = {};
		}
		
		if ('text' === ftype) {
			Ext.apply(cfg, {
				xtype: 'textfield',
				bind: '{' + foName + '}'
			});
			SU.applyProp(cfg, false, fprops, 'minLength');
			SU.applyProp(cfg, false, fprops, 'maxLength');
			SU.applyProp(cfg, false, fprops, 'width', Number.parseInt);
			SU.applyProp(cfg, false, fprops, 'anchor');
			
		} else if ('textarea' === ftype) {
			Ext.apply(cfg, {
				xtype: 'textareafield',
				bind: '{' + foName + '}'
			});
			SU.applyProp(cfg, false, fprops, 'minLength');
			SU.applyProp(cfg, false, fprops, 'maxLength');
			SU.applyProp(cfg, false, fprops, 'width', Number.parseInt);
			SU.applyProp(cfg, false, fprops, 'anchor');
			SU.applyProp(cfg, false, fprops, 'autoGrow', 'grow');
			
		} else if ('number' === ftype) {
			Ext.apply(cfg, {
				xtype: 'numberfield',
				bind: '{' + foName + '}'
			});
			SU.applyProp(cfg, false, fprops, 'minValue');
			SU.applyProp(cfg, false, fprops, 'maxValue');
			SU.applyProp(cfg, false, fprops, 'allowDecimals');
			SU.applyProp(cfg, false, fprops, 'width', Number.parseInt);
			SU.applyProp(cfg, false, fprops, 'anchor');
			
		} else if ('date' === ftype) {
			Ext.apply(cfg, {
				xtype: 'datefield',
				bind: '{' + foName + '}',
				startDay: WT.getStartDay(),
				format: WT.getShortDateFmt()
			});
			
		} else if ('time' === ftype) {
			Ext.apply(cfg, {
				xtype: 'timefield',
				bind: '{' + foName + '}',
				format: WT.getShortTimeFmt()
			});
			
		} else if ('combobox' === ftype) {
			Ext.apply(cfg, WTF[fprops['queryable'] === 'true' ? 'localCombo' : 'lookupCombo']('field1', 'field2', {
				bind: '{' + foName + '}',
				store: field.values
			}));
			if (cfg.allowBlank) {
				Ext.apply(cfg, {
					triggers: {
						clear: WTF.clearTrigger()
					}
				});
			}
			SU.applyProp(cfg, false, fprops, 'width', Number.parseInt);
			SU.applyProp(cfg, false, fprops, 'anchor');
			
		} else if ('checkbox' === ftype) {
			Ext.apply(cfg, {
				xtype: 'checkbox',
				bind: '{' + foName + '}',
				hideEmptyLabel: true,
				boxLabel: flabel
			});
		}
		
		return {
			formulas: SU.setProp({}, foName, fo),
			fieldCfg: cfg
		};
	},
	
	createCustomFieldFormula: function(field) {
		var valueName = this.buildFieldValueName(field.id);
		return Ext.apply(this.callParent(arguments), {
			set: function(val) {
				this.set(valueName, val);
				var sto = this.getStore('values'), rec;
				if (sto) {
					rec = sto.getById(field.id);
					if (rec) rec.setValue(val);
				}
			}
		});
	}
	
	/*
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
	*/
});
