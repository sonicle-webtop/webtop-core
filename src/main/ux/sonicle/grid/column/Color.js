/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.grid.column.Color', {
	extend: 'Ext.grid.column.Column',
	alias: 'widget.socolorcolumn',
	
	tdCls: 'so-'+'grid-cell-colorcolumn',
	innerCls: 'so-'+'grid-cell-inner-colorcolumn',
	
	/**
	 * @cfg {String} colorField
	 * The fieldName for getting the CSS color to apply to the marker.
	 * To determine the color dynamically, configure the column with a `getColor` function.
	 */
	colorField: null,
	
	/**
	 * @cfg {Function} getColor
	 * A function which returns the CSS color to apply to the marker.
	 */
	getColor: null,
	
	/**
	 * @cfg {String} displayField
	 * The fieldName for getting the value to display next to the color marker.
	 */
	displayField: null,
	
	/**
	 * @cfg {square|circle} [geometry=square]
	 * Sets the color marker geomerty.
	 */
	geometry: 'square',
	
	constructor: function() {
		this.scope = this;
		this.callParent(arguments);
	},
	
	defaultRenderer: function(value, cellValues) {
		var me = this,
				cssPrefix = 'so-',
				cls = cssPrefix + 'grid-colorcolumn',
				rec = cellValues ? cellValues.record : null,
				color = me.evalValue(me.getColor, me.colorField, value, rec),
				display = me.evalValue(null, me.displayField, value, rec, '');
				style = {};
		
		if(me.geometry === 'circle') {
			Ext.apply(style, {
				borderRadius: '50%'
			});
		}
		if(color) {
			Ext.apply(style, {
				backgroundColor: color
			});
		}
		return '<div class="' + cls + '" style="' + Ext.dom.Helper.generateStyles(style) + '"></div>' + display;
	},
	
	evalValue: function(getFn, field, value, rec, fallback) {
		if(rec && Ext.isFunction(getFn)) {
			return getFn.apply(this, [value, rec]);
		} else if(rec && !Ext.isEmpty(field)) {
			return rec.get(field);
		} else {
			return (fallback === undefined) ? value : fallback;
		}
	}
});
