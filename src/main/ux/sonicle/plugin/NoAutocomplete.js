/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.plugin.NoAutocomplete', {
	extend: 'Ext.plugin.Abstract',
	alias: 'plugin.sonoautocomplete',
	
	attValue: 'nope',
	//attValue: 'off'
	
	init: function(field) {
		var me = this;
		me.setCmp(field);
		field.on('render', me.onCmpRender, me, {single: true});
	},
	
	onCmpRender: function(s) {
		var me = this,
				el = me.getCmp().getEl(), attrs;
		if(el) {
			attrs = {autocomplete: me.attValue};
			//if(!me.enabled) Ext.apply(attrs, {readOnly: true});
			el.set(attrs);
		}
	}
});
