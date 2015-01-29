/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.Spacer', {
	extend: 'Ext.Component',
	alias: ['widget.sospacer', 'widget.spacer'],
	
	autoEl: 'div',
	
	/**
	 * @cfg {Boolean} vertical [vertical=true]
	 * `true` to apply space vertically, `false` to render it horizontally.
	 */
	vertical: true,
	
	/**
	 * @cfg {Number} mult
	 * Coefficient that will be multiplied to the base 10px unit. Resulting
	 * value will be assigned to component's width or height, according to
	 * {@link #vertical} value.
	 */
	mult: 1,
	
	constructor: function(cfg) {
		var me = this, vert = cfg.vertical;
		if(Ext.isDefined(vert) && Ext.isBoolean(vert)) me.vertical = vert;
		if(Ext.isNumber(cfg.mult)) me.mult = cfg.mult;
		if(me.vertical) {
			me.height = me.mult * 10;
		} else {
			me.width = me.mult * 10;
		}
		me.callParent(arguments);
	}
});
