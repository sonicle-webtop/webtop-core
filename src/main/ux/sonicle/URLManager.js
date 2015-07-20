/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.URLManager', {
	singleton: true,
	
	config: {
		triggerDelay: 100,
		cleanupDelay: 10000
	},
	
	constructor: function(cfg) {
		var me = this;
		me.initConfig(cfg);
		me.callParent([cfg]);
	},
	
	open: function(url) {
		window.open(url, '_blank');
	},
	
	/**
	 * Downloads passed URL without opening any window or tab.
	 * An invisible iframe is created on-the-fly in order to trigger the 
	 * resource download. HTTP response headers should set the 
	 * content-disposition header to 'attachment'.
	 * @param {String} url The resource URL
	 */
	download: function(url) {
		var me = this;
		me.createDownloadIframe(url);
	},
	
	/**
	 * @private
	 * @param {String} url
	 */
	createDownloadIframe: function(url) {
		var me = this;
		
		Ext.defer(function() {
			var el = Ext.get(Ext.dom.Helper.createDom("<iframe style='display:none' src='" + url + "'></iframe>"));
			el.appendTo(Ext.getBody());
			Ext.defer(function() {
				el.destroy();
			}, me.getCleanupDelay() || 1000);
		}, me.getTriggerDelay());
	}
});
