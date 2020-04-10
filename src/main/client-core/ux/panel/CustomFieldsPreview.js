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
	
	constructor: function(cfg) {
		var me = this;
		
		cfg.emptyItemTitle = WT.res('wtcfieldspreviewpanel.empty.tit');
		cfg.emptyItemText = WT.res('wtcfieldspreviewpanel.empty.txt');
		me.callParent([cfg]);
	},
	
	createCustomFieldDef: function(panelId, field) {
		var me = this,
				SU = Sonicle.Utils,
				ftype = field.type,
				fprops = field.props || {},
				flabel = field.label,
				foName = me.buildFieldFormulaName(panelId, field.id, ftype),
				fo = me.createCustomFieldFormula(field),
				cfg = {
					readOnly: true,
					editable: false,
					labelAlign: 'top',
					anchor: '100%'
				};
		
		if ('text' === ftype) {
			Ext.apply(cfg, {
				xtype: 'textfield',
				bind: '{' + foName + '}',
				fieldLabel: flabel
			});
			
		} else if ('textarea' === ftype) {
			Ext.apply(cfg, {
				xtype: 'textareafield',
				bind: '{' + foName + '}',
				autoGrow: 'grow',
				fieldLabel: flabel
			});
			
		} else if ('number' === ftype) {
			Ext.apply(cfg, {
				xtype: 'numberfield',
				bind: '{' + foName + '}',
				fieldLabel: flabel
			});
			SU.applyProp(cfg, false, fprops, 'allowDecimals');
			
		} else if ('date' === ftype) {
			Ext.apply(cfg, {
				xtype: 'datefield',
				bind: '{' + foName + '}',
				startDay: WT.getStartDay(),
				format: WT.getShortDateFmt(),
				fieldLabel: flabel
			});
			
		} else if ('time' === ftype) {
			Ext.apply(cfg, {
				xtype: 'timefield',
				bind: '{' + foName + '}',
				format: WT.getShortTimeFmt(),
				fieldLabel: flabel
			});
			
		} else if ('combobox' === ftype) {
			Ext.apply(cfg, WTF.lookupCombo('field1', 'field2', {
				bind: '{' + foName + '}',
				store: field.values,
				fieldLabel: flabel
			}));
			
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
	}
});
