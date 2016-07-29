/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.toolbar.Upload', {
	extend: 'Ext.toolbar.Toolbar',
	alias: 'widget.souploadtoolbar',
	requires: [
		'Sonicle.Bytes',
		'Sonicle.upload.Button'
	],
	
	
	
	initComponent: function() {
		var me = this;
		
		me.add({
			xtype: 'souploadbutton',
			reference: 'btnupload',
			text: null,
			tooltip: me.res('act-uploadFile.tip'),
			iconCls: me.cssIconCls('uploadFile', 'xs'),
			uploaderConfig: WTF.uploader(me.ID, 'UploadStoreFile', {
				maxFileSize: me.getOption('privateUploadMaxFileSize'),
				extraParams: {
					fileId: null
				},
				listeners: {
					invalidfilesize: function() {
						WT.warn(WT.res(WT.ID, 'error.upload.sizeexceeded', Sonicle.Bytes.format(me.getOption('privateUploadMaxFileSize'))));
					},
					overallprogress: function(s, perc) {
						console.log(perc);
					}
				}
			})
		});
	},
	
	
});
