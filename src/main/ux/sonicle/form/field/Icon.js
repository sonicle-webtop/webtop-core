/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
Ext.define('Sonicle.form.field.Icon', {
	extend: 'Ext.form.field.Base',
	alias: ['widget.soiconfield'],
	requires: [
		'Ext.XTemplate'
	],
	
	ariaRole: 'img',
	focusable: false,
	maskOnDisable: false,
	
	fieldSubTpl: [
		'<div id="{id}" role="{role}" {inputAttrTpl}',
		'<tpl if="fieldStyle"> style="{fieldStyle}"</tpl>',
		' class="{fieldCls} {fieldCls}-{ui} {value}"></div>',
		{
			compiled: true,
			disableFormats: true
		}
	],
	
	fieldCls: Ext.baseCSSPrefix + 'form-display-field',
	fieldBodyCls: Ext.baseCSSPrefix + 'form-display-field-body',
	
	isValid: Ext.returnTrue,
	validate: Ext.returnTrue,
	
	/*
	 * @private
	 * @cfg {Boolean} readOnly
	 */
	readOnly: true,
	
	submitValue: false,
	
	/*
	 * @cfg {Number} width
	 * The width in pixel of the icon.
	 */
	width: 16,
	
	/*
	 * @cfg {Number} height
	 * The height in pixel of the icon.
	 */
	height: 16,
	
	/**
	 * @cfg {String} clsFormat
	 * Formatting pattern string used to builc class name to apply.
	 * The `{0}` placeholder refers to field's value.
	 * 
	 * For example, if you want to add a suffix or a prefix you can set it to:
	 * 
	 *		`prefix-{0}-suffix`
	 */
	clsFormat: '{0}',
	
	/**
	 * @cfg {Function} convertFn
	 * A function to convert the value before building the cls name.
	 */
	
	getValue: function() {
		return this.value;
	},
	
	getRawValue: function() {
		return this.rawValue;
	},
	
	setRawValue: function(value) {
		var me = this;
		
		value = Ext.valueFrom(value, '');
		me.rawValue = value;
		if(me.rendered) {
			me.inputEl.setCls(value);
			me.updateLayout();
		}
		return value;
	},
	
	valueToRaw: function(value) {
		return Ext.valueFrom(this.getClsValue(value), '');
	},
	
	getSubTplData: function(fieldData) {
		var me = this,
				ret = me.callParent(arguments),
				styles;
		
		ret.value = me.getClsValue();
		styles = Ext.dom.Helper.generateStyles({
			width: me.width+'px',
			height: me.height+'px',
			backgroundRepeat: 'no-repeat'
		});
		ret.fieldStyle = Ext.isEmpty(ret.fieldStyle) ? ret.fieldStyle + styles : styles;
		return ret;
	},
	
	/**
	 * @private
	 * Builds CSS class to be applied.
	 */
	getClsValue: function(value) {
		var me = this, val;
		if(Ext.isEmpty(value)) return '';
		if(Ext.isFunction(me.convertFn)) {
			val = me.convertFn.call(me.scope || me, value, me);
		} else {
			val = value;
		}
		return Ext.String.format(me.clsFormat, val || '');
	}
});
