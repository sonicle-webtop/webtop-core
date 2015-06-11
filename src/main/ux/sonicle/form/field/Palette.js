/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.field.Palette', {
	extend: 'Ext.form.field.Picker',
	alias: ['widget.sopalettefield'],
	
	editable: false,
	regex: /^\#[0-9A-F]{6}$/i,
	invalidText: "Colors must be in hex format (like #FFFFFF)",
	matchFieldWidth: false,
	
	/**
	 * @property {String[]} colors
	 * An array of 6-digit color hex code strings (without the # symbol). This array can contain any number of colors,
	 * and each hex code should be unique. You can override individual colors if needed.
	 * Defaults to.
	 */
	colors: null,
	
	afterRender: function() {
		var me = this;
		me.callParent();
		me.updateColor(me.value);
	},
	
	setValue: function(color) {
		var me = this;
		me.callParent(arguments);
		me.updateColor(color);
	},
	
	updateColor: function(color) {
		var el = this.inputEl;
		if(el && !Ext.isEmpty(color)) {
			el.setStyle({
				color: color,
				backgroundColor: color,
				boxShadow: '0px 0px 0px 1px white inset'
			});
		}
	},
	
	createPicker: function() {
		var me = this, cfg = {};
		if(Ext.isArray(me.colors)) {
			cfg = Ext.apply(cfg, {
				colors: me.colors
			});
		}
		return Ext.create(Ext.apply(cfg, {
			xtype: 'colorpicker',
			pickerField: me,
			floating: true,
			focusable: false, // Key events are listened from the input field which is never blurred
			minWidth: 195,
			maxWidth: 195,
			listeners: {
				select: function() {
					me.collapse();
				}
			}
		}));
	},
			
	onExpand: function() {
		var value = this.getValue();
		if(value) this.picker.select(value, true);
	},
	
	onCollapse: function() {
		// Picker does not prepend #, let's add it!
		this.setValue('#'+this.picker.getValue());
	}
});
