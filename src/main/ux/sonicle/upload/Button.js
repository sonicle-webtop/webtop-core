/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.upload.Button', {
	extend: 'Ext.button.Button',
	alias: 'widget.souploadbutton',
	requires: [
		'Sonicle.upload.Uploader'
	],
	
	constructor: function(cfg) {
		var me = this;
		cfg = cfg || {};
		cfg.uploaderConfig = Ext.applyIf(cfg.uploaderConfig || {}, {
			browseButton: cfg.id || Ext.id(me)
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this, e;
		
		me.callParent(arguments);
		me.uploader = Ext.create('Sonicle.upload.Uploader', me, me.initialConfig.uploaderConfig);
		
		if(me.uploader.getDropElement() && (e = Ext.getCmp(me.uploader.getDropElement()))) {
			e.addListener('afterRender', function() {
				me.uploader.init();
			}, {single: true});
		} else {
			me.on('afterrender', function() {
				me.uploader.init();
			}, {single: true});
		}
		
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
