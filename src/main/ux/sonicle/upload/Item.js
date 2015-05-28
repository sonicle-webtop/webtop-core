/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.upload.Item', {
	extend: 'Ext.menu.Item',
	alias: 'widget.souploadmenuitem',
	requires: [
		'Sonicle.upload.Uploader'
	],
	
	constructor: function(cfg) {
		var me = this;
		cfg = cfg || {};
		cfg.uploaderConfig = Ext.applyIf(cfg.uploaderConfig || {}, {
			browseButton: cfg.id || Ext.id(me),
			dropElement: null
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		
		me.callParent(arguments);
		me.uploader = Ext.create('Sonicle.upload.Uploader', me, me.initialConfig.uploaderConfig);
		me.on('afterrender', function() {
				me.uploader.init();
		}, {single: true});
		
		me.relayEvents(me.uploader, [
			'beforestart',
			'uploadready',
			'uploadstarted',
			'uploadcomplete',
			'uploaderror',
			'filesadded',
			'beforeupload',
			'fileuploaded',
			'updateprogress',
			'uploadprogress',
			'storeempty'
		]);
	}
});
