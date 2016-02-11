/*
 * Sonicle ExtJs UX
 * Copyright (C) 2014 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.trigger.Clear', {
	extend: 'Sonicle.form.trigger.Hideable',
	alias: 'trigger.soclear',
	
	hideOn: 'empty',
	hideWhenMouseOut: false,
	
	/**
	 * @cfg {Boolean} clearOnEscape
	 * Clears the textfield/combobox when the escape (ESC) key is pressed
	 */
	clearOnEscape: false,
	
	/**
     * @event clear
     * Fires when the underlying component input field's value has been cleared using this trigger.
	 * @param {Ext.form.field.Text} this
     */
	
	cls: Ext.baseCSSPrefix + 'form-clear-trigger',
	
	initEvents: function() {
		var me = this, 
				cmp = me.field;
		
		me.callParent();
		if (me.clearOnEscape) {
			me.addManagedListener(cmp.inputEl, 'keydown', function(e) {
				if (e.getKey() === Ext.event.Event.ESC) {
					if (cmp.isExpanded) return;
					me.handler(cmp);
					e.stopEvent();
				}
			}, me);
		}
	},

	handler: function(cmp) {
		if (Ext.isFunction(cmp.clearValue)) {
			cmp.clearValue();
		} else {
			cmp.setValue(null);
		}
		cmp.fireEvent('clear', cmp);
	}
});
