Ext.define('Sonicle.webtop.core.WT', {
	singleton: true,
	alternateClassName: 'WT',
	
	statics: {
		CORE_ID: 'com.sonicle.webtop.core',
		CORE_NS: 'Sonicle.webtop.core'
	},
	
	strings: null,
	
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
			return WT.CORE_NS + '.' + cn;
		} else {
			return ns + '.' + cn;
		}
	},
	
	/**
	 * This is shorthand reference to getResource() method.
	 * @param {String} svc The service id.
	 * @param {String} key The resource key.
	 * @returns {String} The value.
	 */
	res: function(svc, key) {
		if(arguments.length === 1) {
			key = svc;
			svc = WT.CORE_ID;
		}
		if(svc === WT.CORE_ID) {
			return WT.strings[key];
		} else {
			var inst = WT.getApp().getService();
			if(inst === null) return null;
			return inst.res(key);
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