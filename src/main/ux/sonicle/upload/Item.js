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
	
	config: {
		uploaderAutoInit: true
	},
	
	uploader: null,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.uploader = Ext.create('Sonicle.upload.Uploader', me, me.initialConfig.uploaderConfig);
		if(me.getUploaderAutoInit()) {
			me.on('afterrender', function() {
				me.uploader.setBrowseButton(me.getId());
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
	},
	
	initUploader: function() {
		var me = this;
		me.uploader.setBrowseButton(me.getId());
		me.uploader.init();
	}
});
