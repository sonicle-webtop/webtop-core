/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.grid.column.Lookup', {
	extend: 'Ext.grid.column.Column',
	alias: 'widget.solookupcolumn',
	
	requires: [
		
	],
	mixins: [
		'Ext.util.StoreHolder'
	],
	producesHTML: false,
	
	displayField: '',
	
	initComponent: function() {
		var me = this;
		me.bindStore(me.store || 'ext-empty-store', true, true);
		me.callParent(arguments);
	},
	
	/**
	 * Binds a store to this instance.
	 * @param {Ext.data.AbstractStore/String} [store] The store to bind or ID of the store.
	 * When no store given (or when `null` or `undefined` passed), unbinds the existing store.
	 */
	bindStore: function(store, /* private */ initial) {
		var me = this;
		me.mixins.storeholder.bindStore.call(me, store, initial);
		store = me.getStore();
	},
	
	/**
	 * See {@link Ext.util.StoreHolder StoreHolder}.
	 */
	onBindStore: function(store, initial) {
		// We're being bound, not unbound...
		if(store) {
			if(store.autoCreated) this.boldDateField = 'field1';
		}
	},
	
	defaultRenderer: function(value) {
		return this._storeValue(value);
	},
	
	updater: function(cell, value) {
		cell.firstChild.innerHTML = this._storeValue(value);
	},
	
	_storeValue: function(value) {
		var mo = this.getStore().getById(value);
		return mo ? mo.get(this.displayField) : '?';
	}
});
