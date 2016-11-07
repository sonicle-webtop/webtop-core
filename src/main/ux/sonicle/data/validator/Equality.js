/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.data.validator.Equality', {
	extend:'Ext.data.validator.Validator',
	alias: 'data.validator.soequality',
	
	type: 'soequality',
	
	config: {
		/**
		 * @cfg {String} message 
		 * The error message to return when the value is not specified.
		 */
		message: 'Does not match `{0}` field',
		
		/**
		 * @cfg {String} equalField
		 * The field's name to match its value.
		 */
		equalField: null,
		
		/**
		 * @cfg {String} fieldLabel
		 * Label representing the above field.
		 */
		fieldLabel: ''
	},
	
	validate: function(v,rec) {
		var me = this,
				name = me.getEqualField();
		if(Ext.isString(name)) {
			return (v === rec.get(name)) ? true : Ext.String.format(me.getMessage(), me.getFieldLabel());
		} else {
			return false;
		}
	}
});
