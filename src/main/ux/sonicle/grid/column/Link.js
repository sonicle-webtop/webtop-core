/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.grid.column.Link', {
	extend: 'Ext.grid.column.Column',
	alias: 'widget.solinkcolumn',
	
	/**
	 * @event linkclick
	 * Fires when the link is clicked
	 * @param {Sonicle.grid.column.Link} this LinkColumn
	 * @param {Number} rowIndex The row index
	 * @param {Ext.data.Model} record The record that is being clicked
	 */
	
	processEvent: function(type, view, cell, recordIndex, cellIndex, e, record, row) {
		var me = this, ret;
		if((e.type === 'click') && (e.target.tagName === 'A')) {
			me.fireEvent('linkclick', me, recordIndex, record);
		} else {
			ret = me.callParent(arguments);
		}
		return ret;
	},
	
	defaultRenderer: function(value) {
		return '<a href="javascript:Ext.EmptyFn" class="so-grid-linkcolumn">'+value+'</a>';
	},
	
	updater: function(cell, value) {
		cell = Ext.fly(cell);
		Ext.fly(cell.down(this.getView().innerSelector, true).firstChild).setHtml(value);
	}
});

