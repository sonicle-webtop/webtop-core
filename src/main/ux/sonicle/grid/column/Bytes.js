/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.grid.column.Bytes', {
	extend: 'Ext.grid.column.Column',
	alias: 'widget.sobytescolumn',
	
	requires: ['Sonicle.String'],
	producesHTML: false,
	
	/**
	 * @cfg {Boolean} siUnits
	 * Whether to use the SI units labels or the binary ones.
	 */
	siUnits: false,
	
	/**
	 * @cfg {String} [unitSeparator]
	 * Separator to use between value and unit.
	 */
	unitSeparator: ' ',
	
	defaultRenderer: function(value) {
		return (value === -1) ? '' : Sonicle.String.humanReadableSize(value, {siUnits: this.siUnits, unitSeparator: this.unitSeparator});
	},
	
	updater: function(cell, value) {
		cell.firstChild.innerHTML = Ext.grid.column.Number.prototype.defaultRenderer.call(this, value);
	}
});
