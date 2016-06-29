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
	
	colorField: null,
	displayField: null,
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
				color = me.evalField(me.colorField, value, rec),
				display = me.evalField(me.displayField, '', rec),
				//color = Ext.isEmpty(me.colorField) ? value : rec.get(me.colorField),
				//display = Ext.isEmpty(me.displayField) ? '' : rec.get(me.displayField),
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
	
	evalField: function(field, value, rec) {
		if(Ext.isFunction(field)) {
			return field.bind(this, value, rec)();
		} else if(rec && !Ext.isEmpty(field)) {
			return rec.get(field);
		} else {
			return value;
		}
	}
});
