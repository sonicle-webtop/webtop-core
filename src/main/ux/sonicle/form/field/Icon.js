/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
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
