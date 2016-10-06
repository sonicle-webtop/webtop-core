/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.menu.StoreMenu', {
	extend: 'Ext.menu.Menu',
	alias: 'widget.sostoremenu',
	
	mixins: [
		'Ext.util.StoreHolder'
	],
	
	config: {
		textField: 'text'
	},
	
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
			if(store.autoCreated) this.textField = 'field1';
		}
	},
	
	/**
	 * See {@link Ext.util.StoreHolder StoreHolder}.
	 */
	getStoreListeners: function(store, o) {
		var me = this;
		return {
			datachanged: me.onStoreDataChanged,
			load: me.onStoreLoad
		};
	},
	
	/**
	 * private
	 */
	_loadMenuItems: function() {
		var me = this;
		if(me.store && !me.store.loaded) me.store.load();
	},
	
	onStoreDataChanged: function() {
		this.updateMenuItems();
	},
	
	onStoreLoad: function(store, records, success) {
		if(success) this.updateMenuItems();
	},
	
	updateMenuItems: function() {
		var me = this,
				textField = me.getTextField();
		
		if(me.store) {
			Ext.suspendLayouts();
			me.removeAll();
			me.store.each(function(rec) {
				me.add({
					itemId: rec.getId(),
					text: rec.get(textField)
				});
			});
			Ext.resumeLayouts(true);
		}
	}
});
