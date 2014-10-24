Ext.define('Sonicle.webtop.core.WT', {
	singleton: true,
	alternateClassName: 'WT',
	
	statics: {
		ID: 'com.sonicle.webtop.core',
		XID: 'wt',
		NS: 'Sonicle.webtop.core'
	},
	
	strings: null,
	loadedCss: null,
	
	/**
	 * Returns the application.
	 * This is shorthand reference to Sonicle.webtop.core.getApplication().
	 * @returns {Sonicle.webtop.core.Application} The resulting object.
	 */
	getApp: function() {
		return Sonicle.webtop.core.getApplication();
	},
	
	preNs: function(ns, cn) {
		if(arguments.length === 1) {
			//return 'Sonicle.webtop.core.'+cn;
			return WT.NS + '.' + cn;
		} else {
			return ns + '.' + cn;
		}
	},
	
	/**
	 * Returns a string resource.
	 * @param {String} svc The service id.
	 * @param {String} key The resource key.
	 * @returns {String} The value.
	 */
	res: function(svc, key) {
		if(arguments.length === 1) {
			key = svc;
			svc = WT.NS;
		}
		if(svc === WT.NS) {
			return WT.strings[key];
		} else {
			var inst = WT.getApp().getService();
			if(inst === null) return null;
			return inst.res(key);
		}
	},
	
	ajaxProxy: function(svc, act, rootp) {
		return {
			type: 'ajax',
			url: 'service-request',
			extraParams: {
				service: svc,
				action: act
			},
			reader: {
				type: 'json',
				rootProperty: rootp
			}
		};
	},
	
	/**
	 * Loads a CSS file by adding in the page a new link element.
	 * @param {String} href The CSS href url.
	 */
	loadCss: function(href) {
		var me = this;
		if(!me.loadedCss) me.loadedCss = {};
		if(!me.loadedCss[href]) {
			var doc = window.document;
			var link = doc.createElement('link');
			link.rel = 'stylesheet';
			link.type = 'text/css';
			link.href = href;
			doc.getElementsByTagName('head')[0].appendChild(link);
			me.loadedCss[href] = href;
		}
	},
	
	/**
	 * Decodes (parses) a properties text to an object.
	 * @param {String} text The properties string.
	 * @returns {Object} The resulting object.
	 */
	decodeProps: function(text) {
		var i1, i2 = -1, line, ieq;
		var hm = {}, key, val;
		var done = false;
		while(!done) {
			i1 = i2+1;
			i2 = text.indexOf('\n', i1);
			line = null;
			if(i2 < 0) {
				if(i1 < text.length) line = text.substring(i1, text.length);
				done = true;
			} else {
				line = text.substring(i1, i2);
			}
			if(line) {
				ieq = line.indexOf('=');
				if(ieq < 0) continue;
				key = line.substring(0, ieq);
				val = line.substring(ieq+1);
				hm[key] = val;
			}
		}
		return hm;
	}
});
