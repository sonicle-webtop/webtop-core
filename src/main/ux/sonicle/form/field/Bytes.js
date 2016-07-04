/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.field.Bytes', {
	extend:'Ext.form.field.Text',
	alias: 'widget.sobytesfield',
	requires: [
		'Sonicle.String'
	],
	
	/**
	 * @cfg {String} maxText
	 * Error text to display if the maximum value validation fails.
	 */
	maxText: 'The maximum value for this field is {0}',
	
	/**
	 * @cfg {String} nabText
	 * Error text to display if the value is not a valid number of bytes.
	 */
	nabText: '{0} is not a valid number of bytes',
	
	/**
	 * @cfg {String} negativeText
	 * Error text to display if the value is negative.
	 */
	negativeText : 'The value cannot be negative',
	
	/**
	 * @cfg {Number} maxValue The maximum allowed value in bytes.
	 * Will be used by the field's validation logic.
	 * Defaults to Number.MAX_VALUE.
	 */
	maxValue: Number.MAX_VALUE,
	
	initComponent : function() {
		var me = this;
		me.setMaxValue(me.maxValue);
		me.callParent(arguments);
	},
	
	initValue: function() {
		var me = this,
				value = me.value;
		// If a String value was supplied, try to convert it to a proper bytes number
		if(Ext.isString(value)) me.value = me.rawToValue(value);
		me.callParent(arguments);
	},
	
	setValue: function(value) {
		var me = this,
				bind, valueBind;
		
		if (me.hasFocus) {
			bind = me.getBind();
			valueBind = bind && bind.value;
			if (valueBind && valueBind.syncing && value === me.value) return me;
		}
		return me.callParent([value]);
	},
	
	setMaxValue: function(value) {
		var me = this,
				max = (Ext.isString(value) ? me.parseBytes(value) : value);
		me.maxValue = max || Number.MAX_VALUE;
	},
	
	getErrors: function(value) {
		value = arguments.length > 0 ? value : this.processRawValue(this.getRawValue());
		var me = this,
				sformat = Ext.String.format,
				bformat = Sonicle.Bytes.format,
				errors = me.callParent([value]),
				bytes;
		
		if(value.length < 1) { // if it's blank and textfield didn't flag it then it's valid 
			return errors;
		}
		
		bytes = me.parseBytes(value);
		if (!bytes) {
			errors.push(sformat(me.nabText, value));
			return errors;
		}
		
		if (bytes < 0) {
			errors.push(me.negativeText);
		} else if (bytes > me.maxValue) {
			errors.push(sformat(me.maxText, bformat(me.maxValue)));
		}
		return errors;
	},
	
	rawToValue: function(rawValue) {
		return this.parseBytes(rawValue) || rawValue || null;
	},
	
	valueToRaw: function(value) {
		return Sonicle.Bytes.format(this.parseBytes(value));
	},
	
	/**
	 * @private
	 */
	parseBytes: function(value) {
		if(!value || Ext.isNumber(value)) return value;
		return Sonicle.Bytes.parse(value);
	},
	
	onBlur: function(e) {
		var me = this,
				v = me.rawToValue(me.getRawValue());
		
		if(!Ext.isEmpty(v)) me.setValue(v);
		me.callParent([e]);
	}
});
