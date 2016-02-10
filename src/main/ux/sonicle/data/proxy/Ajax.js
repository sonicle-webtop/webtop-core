/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.data.proxy.Ajax', {
	extend: 'Ext.data.proxy.Ajax',
	alias: 'proxy.soajax',
	
	config: {
		autoAbort: false
	},
	
	doRequest: function(operation) {
		var me = this;
		if(me.getAutoAbort() && me.lastRequest) {
			me.abort();
		}
		return me.callParent(arguments);
	}
});
