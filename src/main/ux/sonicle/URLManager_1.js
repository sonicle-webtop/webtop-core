/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.URLManager', {
	singleton: true,
	requires: [
		'Sonicle.UUID'
	],
	
	downloads: Ext.create('Ext.util.HashMap'),
	cooTask: null,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		me.runCookieTask();
	},
	
	destroy: function() {
		this.shutdownCookieTask();
	},
	
	open: function(url) {
		window.open(url, '_blank');
	},
	
	/**
	 * Downloads passed URL under the hood.
	 * An invisible iframe is created on-the-fly in order to trigger the 
	 * resource download. HTTP response headers should set the 
	 * content-disposition header to 'attachment'.
	 * @param {String} url The resource URL
	 */
	download: function(url) {
		var me = this;
		me.cleanupDownloads();
		me.createDownloadIframe(url);
	},
	
	/**
	 * @private
	 * @param {String} url
	 */
	createDownloadIframe: function(url) {
		var me= this,
				eid = Ext.id(),
				did = Sonicle.UUID.generate(),
				src = Ext.String.urlAppend(url, Ext.Object.toQueryString({did: did})),
				el, dom, id, did;
		
		dom = Ext.dom.Helper.createDom("<iframe style='display:none' src='" + src + "'></iframe>");
		el = Ext.get(dom);
		this.downloads.add(did, eid);
		console.log('downloading '+did);
		/*
		if(!Ext.isChrome) {
			el.on('load', function(s) {
				console.log('load '+s.id);
				me.removeDownloadIframe(s.id);
			}, me);
		}
		*/
		el.appendTo(Ext.getBody());
	},
	
	removeDownloadIframe: function(id) {
		var me = this;
		if(!me.downloads.containsKey(id)) return;
		console.log('removing '+id);
		me.downloads.removeAtKey(id);
		try { Ext.fly(id).destroy(); } catch(err) { /* Do nothing... */ };
	},
	
	cleanupDownloads: function() {
		var me = this,
				ids = me.downloads.getKeys(),
				coo;
		
		Ext.iterate(ids, function(id) {
			coo = Ext.Util.cookies.get(Ext.String.format('download.{1}', id));
			console.log(coo);
			if(coo) me.removeDownloadIframe(id);
		});
	},
	
	runCookieTask: function() {
		var me = this;
		if(!Ext.isDefined(me.cooTask)) {
			me.cooTask = Ext.TaskManager.start({
				run: function() {
					me.cleanupDownloads();
				},
				interval: 60000
			});
		}
	},
	
	shutdownCookieTask: function() {
		var me = this;
		if(Ext.isDefined(me.cooTask)) {
			Ext.TaskManager.stop(me.cooTask);
			delete me.cooTask;
		}
	}
});
