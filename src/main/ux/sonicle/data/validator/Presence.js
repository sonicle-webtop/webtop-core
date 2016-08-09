/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.data.validator.Presence', {
	extend:'Ext.data.validator.Presence',
	alias: 'data.validator.sopresence',
	
	/**
	 * @cfg {Function} skip
	 * Return 'true' to disable validation basing on custom logic.
	 */
	skip: null,
	
	validate: function(v,rec) {
		var me = this;
		if(Ext.isFunction(me.skip) && me.skip(rec)) return true;
		return me.callParent(arguments);
	}
});
