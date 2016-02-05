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
	
	preventDefault: false,

	// Button element *looks* focused but it should never really receive focus itself,
	// and with it being a <div></div> we don't need to render tabindex attribute at all
	tabIndex: null,
	
	autoEl: {
		tag: 'div',
		unselectable: 'on'
	},
	
	config: {
		uploaderAutoInit: true
	},
	
	uploader: null,
	
	initComponent: function() {
		var me = this, e;
		me.callParent(arguments);
		
		me.uploader = Ext.create('Sonicle.upload.Uploader', me, me.initialConfig.uploaderConfig);
		if(me.getUploaderAutoInit()) {
			if(me.uploader.getDropElement() && (e = Ext.getCmp(me.uploader.getDropElement()))) {
				e.addListener('afterRender', function() {
					me.initUploader();
				}, {single: true});
			} else {
				me.on('afterrender', function() {
					me.initUploader();
				}, {single: true});
			}
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
