/* 
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.picker.CustomFieldExpr', {
	alternateClassName: 'WTA.ux.picker.CustomFieldExpr',
	extend: 'Sonicle.form.field.pickerpanel.Editor',
	requires: [
		'Sonicle.String',
		'Sonicle.VMUtils',
		'Sonicle.form.field.CodeEditor'
	],
	layout: 'border',
	
	multiline: false,
	width: 420,
	height: 320,
	
	viewModel: {
		data: {
			data: {
				expr: null
			}
		}
	},
	
	keyMapEnabled: false, // Disable keymap to avoid issues with code-editor
	
	constructor: function(cfg) {
		var me = this;
		cfg.okText = WT.res('word.ok');
		cfg.cancelText = WT.res('word.cancel');
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.add({
			xtype: 'wtfieldspanel',
			region: 'center',
			defaults: {
				labelAlign: 'top'
			},
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'sotext',
					hidden: !me.multiline,
					iconType: 'info',
					cls: 'wt-theme-text-lighter1',
					margin: '0 0 5 0',
					text: WT.res(WT.ID, 'customFieldExpr.hint.multiline', '\';\'+')
				}, {
					xtype: 'socodeditor',
					reference: 'fldexpr',
					bind: '{data.expr}',
					editor: {
						lineWrapping: true
					},
					validator: function(v) {
						return WTA.ux.picker.CustomFieldExpr.validateExpr(v, me.multiline);
					},
					listeners: {
						validitychange: function(s, valid) {
							var fld = me.lookupReference('fldstatus');
							fld.setValue(valid ? null : s.getErrors());
							fld.setHidden(valid);
						}
					},
					flex: 1
				}, {
					xtype: 'displayfield',
					reference: 'fldstatus',
					fieldCls: 'x-form-invalid-under',
					hidden: true
				}
			]
		});
	},
	
	applyValue: function(v) {
		if (!Ext.isString(v)) return;
		return v;
	},
	
	updateValue: function(nv, ov) {
		Sonicle.VMUtils.setData(this.getViewModel(), {expr: nv});
	},
	
	syncValue: function() {
		var data = Sonicle.VMUtils.getData(this.getViewModel(), ['expr']);
		this.setValue(data.expr);
	},
	
	privates: {
		
	},
	
	statics: {
		validateExpr: function(s, multiline) {
			var WCFE = WTA.ux.picker.CustomFieldExpr;
			if (multiline) {
				var exprs = Sonicle.String.split(s, ';\n'), i, res;
				for (i=0; i<exprs.length; i++) {
					res = WCFE.compileExpr(exprs[i]);
					if (res !== true) return '[expr #' + (i+1) + '] ' + res;
				}
				return true;
			} else {
				return WCFE.compileExpr(s);
			}
		},
		
		compileExpr: function(s) {
			if (!Ext.isEmpty(s)) {
				try {
					new Jexl.Jexl().compile(s);
					return true;
				} catch (err) {
					return err.message;
				}
			}
			return true;
		},
		
		evalExpr: function(s, multiline, jexl, context, silent) {
			if (silent === undefined) silent = false;
			var exprs = (multiline === true) ? Sonicle.String.split(s, ';\n') : [s],
				ret = new Array(exprs.length),
				i;
			for (i=0; i<exprs.length; i++) {
				try {
					ret[i] = jexl.evalSync(exprs[i], context);
				} catch (err) {
					ret[i] = silent ? false : err;
				}
			}
			return ret;
		}
	}
});
