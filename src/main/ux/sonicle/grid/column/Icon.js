/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.grid.column.Icon', {
	extend: 'Ext.grid.column.Column',
	alias: 'widget.soiconcolumn',
	
	tdCls: 'so-'+'grid-cell-iconcolumn',
	innerCls: 'so-'+'grid-cell-inner-iconcolumn',
	
	iconField: null,
	tipField: null,
	iconSize: 16,
	
	constructor: function() {
		this.scope = this;
		this.callParent(arguments);
	},
	
	defaultRenderer: function(value, cellValues) {
		var me = this,
				cssPrefix = 'so-',
				cls = cssPrefix + 'grid-iconcolumn',
				rec = cellValues.record,
				size = me.iconSize,
				ico = me.evalField(me.iconField, rec),
				ttip = me.evalField(me.tipField, rec);
		
		if(ico) cls += ' ' + ico;
		if(ttip) {
			return '<div title="'+ttip+'" class="'+cls+'" style="width:'+size+'px;height:'+size+'px" />';
		} else {
			return '<div class="'+cls+'" style="width:'+size+'px;height:'+size+'px" />';
		}
	},
	
	evalField: function(field, rec) {
		if(Ext.isFunction(field)) {
			return field.bind(this, rec)();
		} else if(Ext.isString(field)) {
			return rec.get(field);
		} else {
			return null;
		}
	}
});
