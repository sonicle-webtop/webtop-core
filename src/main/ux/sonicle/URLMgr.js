/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.URLMgr', {
	singleton: true,
	
	config: {
		filenameParameter: 'filename',
		triggerDelay: 100,
		iframeCleanupDelay: 10000
	},
	
	constructor: function(cfg) {
		var me = this;
		me.initConfig(cfg);
		me.callParent([cfg]);
	},
	
	open: function(url, blank) {
		blank = !!blank | true;
		return window.open(url, blank ? '_blank' : '_self');
	},
	
	openFile: function(url, opts) {
		opts = opts || {};
		var me = this;
		me.open(me.urlWithFilename(url, opts.filename), true);
	},
	
	download: function(url, opts) {
		this.downloadFile(url, opts);
	},
	
	/**
	 * Forces passed URL download (without opening any window or tab).
	 * HTTP response should set the content-disposition header to 'attachment'.
	 * @param {String} url The resource URL
	 */
	downloadFile: function(url, opts) {
		opts = opts || {};
		var me = this, ret;
		
		if(opts.iframe) {
			me.downloadUsingIframe(url, opts);
		} else {
			ret = me.downloadUsingVirtualLink(url, opts);
			if(!ret) me.open(me.urlWithFilename(url, opts.filename), true);
		}
	},
	
	/**
	 * @private
	 */
	downloadUsingVirtualLink: function(url, opts) {
		var me = this,
				win = window,
				doc = document,
				fname = opts.filename || '',
				furl = me.urlWithFilename(url, fname),
				el, e, ope;
		
		if(!win.ActiveXObject) { // For non-IE
			if(doc.createEvent) {
				el = doc.createElement('a');
				el.href = furl;
				el.target = '_blank';
				el.download = fname;
				e = doc.createEvent('MouseEvents');
				e.initEvent('click', true, true);
				el.dispatchEvent(e);
				(win.URL || win.webkitURL).revokeObjectURL(el.href);
				return true;
			}
		} else if(!!win.ActiveXObject && doc.execCommand) { // for IE < 11
			ope = me.open(furl, true);
			ope.document.close();
			ope.document.execCommand('SaveAs', true, fname);
			ope.close();
		}
		return false;
	},
	
	/**
	 * @private
	 */
	downloadUsingIframe: function(url, opts) {
		var me = this, el;
		Ext.defer(function() {
			el = Ext.get(Ext.dom.Helper.createDom("<iframe style='display:none' src='" + url + "'></iframe>"));
			el.appendTo(Ext.getBody());
			Ext.defer(function() {
				el.destroy();
			}, opts.cleanupDelay || me.getIframeCleanupDelay());
		}, me.getTriggerDelay());
		return true;
	},
	
	urlWithFilename: function(url, filename) {
		var me = this, o = {};
		if(Ext.isString(filename)) {
			o[me.getFilenameParameter()] = filename;
			return Ext.String.urlAppend(url, Ext.Object.toQueryString(o));
		} else {
			return url;
		}
	}
});
