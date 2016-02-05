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
				me.initUploader();
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
	
	destroy: function() {
		var me = this;
		if(me.uploader) {
			me.uploader.destroy();
			me.uploader = null;
		}
		me.callParent();
	},
	
	initUploader: function(buttonId) {
		var me = this;
		buttonId = buttonId || me.getId();
		me.uploader.setBrowseButton(buttonId);
		me.uploader.init();
	}
});
