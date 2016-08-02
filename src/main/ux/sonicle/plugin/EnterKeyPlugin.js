/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.plugin.EnterKeyPlugin', {
	extend: 'Ext.AbstractPlugin',
	alias: 'plugin.soenterkeyplugin',
	
	/**
	 * @event enterkey
	 * Fires when the ENTER confirmation key is pressed on the field.
	 * @param {Ext.form.field.Text} field The field that generates the event
	 * @param {Ext.event.Event} e The original event object
	 */
	
	init: function(cmp) {
		var me = this;
		me.setCmp(cmp);
		cmp.on('specialkey', me.onSpecialKey, me);
	},
	
	destroy: function() {
		var me = this;
		me.getCmp().un('specialkey', me.onSpecialKey, me);
		me.setCmp(null);
	},
	
	onSpecialKey: function(s, e) {
		var me = this, cmp;
		if(e.getKey() === e.ENTER) {
			cmp = me.getCmp();
			// If field is picker, prevent default behaviour if list is expanded
			if(cmp.isXType('pickerfield') && cmp.isExpanded) return;
			cmp.fireEvent('enterkey', s, e);
		}
	}
});
