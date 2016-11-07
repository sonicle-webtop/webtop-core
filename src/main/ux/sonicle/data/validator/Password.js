/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.data.validator.Password', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.sopassword',
	
	type: 'sopassword',
	
	config: {
		/**
		 * @cfg {String} simpleMessage 
		 * The error message to return when simple validation fails.
		 */
		simpleMessage: 'Password must be at least 1 character long',
		
		/**
		 * @cfg {String} complexMessage 
		 * The error message to return when complex validation fails.
		 */
		complexMessage: 'Password must length at least 8 characters and must meet at least three of the following four categories: uppercase latin letters, lowercase latin letters, numbers, special characters',
		
		complex: false,
		
		simpleRe: /^[\s\S]{1,128}$/,
		complexLenRe: /^[\s\S]{8,128}$/,
		complexLwLRe: /.*[a-z].*/,
		complexUpLRe: /.*[A-Z].*/,
		complexNumRe: /.*[0-9].*/,
		complexSpeRe: /.*[^a-zA-Z0-9].*/
	},
	
	validate: function(v,rec) {
		var me = this, count;
		if(this.getComplex()) {
			count = 0;
			if(me.getComplexLenRe().test(v)) {
				if(me.getComplexLwLRe().test(v)) count++;
				if(me.getComplexUpLRe().test(v)) count++;
				if(me.getComplexNumRe().test(v)) count++;
				if(me.getComplexSpeRe().test(v)) count++;
			}
			return (count >= 3) ? true : me.getComplexMessage();
		} else {
			return me.getSimpleRe().test(v) ? true : me.getSimpleMessage();
		}
	}
});
