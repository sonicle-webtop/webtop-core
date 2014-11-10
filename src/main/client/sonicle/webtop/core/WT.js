Ext.define('Sonicle.webtop.core.WT', {
	singleton: true,
	alternateClassName: 'WT',
	
	/**
	 * @property
	 * Core service ID.
	 */
	ID: 'com.sonicle.webtop.core',
	/**
	 * @property
	 * Core service short ID.
	 */
	XID: 'wt',
	/**
	 * @property
	 * Core service namespace.
	 */
	NS: 'Sonicle.webtop.core',
	
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
	 * Gets the initial setting value bound to key.
	 * @param {String} key The key.
	 * @return {Mixed} Setting value.
	 */
	getInitialSetting: function(key) {
		var is = WTStartup.initialSettings[this.ID] || {};
		return is[key];
	},
	
	/**
	 * Returns a string resource.
	 * @param {String} id The service ID.
	 * @param {String} key The resource key.
	 * @returns {String} The value.
	 */
	res: function(id, key) {
		if(arguments.length === 1) {
			key = id;
			id = WT.NS;
		}
		if(id === WT.NS) {
			return WT.strings[key];
		} else {
			var inst = WT.getApp().getService(id);
			//if(inst === null) return null;
			return (inst) ? inst.res(key) : null;
		}
	},
	
	/**
	 * Builds CSS class name namespacing it using service xid.
	 * @param {String} xid Service short ID.
	 * @param {String} name The CSS class name part.
	 * @return {String} The concatenated CSS class name.
	 */
	cssCls: function(xid, name) {
		return Ext.String.format('{0}-{1}', xid, name);
	},
	
	/**
	 * Builds CSS class name for icons namespacing it using service xid.
	 * For example, using 'service' as name, it will return '{xid}-icon-service'.
	 * Using 'service-l' as name it will return '{xid}-icon-service-l'.
	 * Likewise, using 'service' as name and 'l' as size it will return the
	 * same value: '{xid}-icon-service-l'.
	 * @param {String} xid Service short ID.
	 * @param {String} name The icon name part.
	 * @param {String} [size] Icon size (one of xs,s,m,l).
	 * @return {String} The concatenated CSS class name.
	 */
	cssIconCls: function(xid, name, size) {
		if(size === undefined) {
			return Ext.String.format('{0}-icon-{1}', xid, name);
		} else {
			return Ext.String.format('{0}-icon-{1}-{2}', xid, name, size);
		}
	},
	
	isXType: function(obj, xtype) {
		if(!Ext.isObject(obj)) return false;
		if(!Ext.isFunction(obj.isXType)) return false;
		return obj.isXType(xtype);
	},
	
	isAction: function(obj) {
		if(!Ext.isObject(obj)) return false;
		return (obj.isAction && Ext.isFunction(obj.execute));
	},
	
	proxy: function(svc, act, rootp) {
		return {
			type: 'ajax',
			url: 'service-request',
			extraParams: {
				service: svc,
				action: act
			},
			reader: {
				type: 'json',
				rootProperty: rootp,
				messageProperty: 'message'
			},
			listeners: {
				exception: function(proxy, request, operation, eOpts) {
					//TODO: intl. user error message plus details
					WT.error('Error during action "'+act+'" on service "'+svc+'"',"Ajax Error");
				}
			}
		};
	},
	
	apiProxy: function(svc, act, rootp) {
		return {
			type: 'ajax',
			api: {
				create: 'service-request?crud=create',
				read: 'service-request?crud=read',
				update: 'service-request?crud=update',
				destroy: 'service-request?crud=delete'
			},
			extraParams: {
				service: svc,
				action: act
			},
			reader: {
				type: 'json',
				rootProperty: rootp,
				messageProperty: 'message'
			}
		};
	},
	
	componentLoader: function(svc, act, opts) {
		if(!opts) opts = {};
		return {
			url: 'service-request',
			params: Ext.applyIf({
				service: svc,
				action: act
			}, opts.params || {}),
			contentType: 'html',
			loadMask: true
		};
	},
	
	ajaxReq: function(svc, act, opts) {
		var me = this;
		if(!opts) opts = {};
		var fn = opts.callback, scope = opts.scope;
		var options = {
			url: 'service-request',
			method: 'POST',
			params: Ext.applyIf({
				service: svc,
				action: act
			}, opts.params || {}),
			headers: {"Content-Type": "application/x-www-form-urlencoded; charset=utf-8"},
			success: function(resp, opts) {
				var obj = Ext.decode(resp.responseText);
				Ext.callback(fn, scope || me, [obj.success, obj, opts]);
			},
			failure: function(resp, opts) {
				Ext.callback(fn, scope || me, [false, null, opts]);
			},
			scope: me
		};
		if(opts.timeout) Ext.apply(options, {timeout: opts.timeout});
		Ext.Ajax.request(options);
	},
	
	/**
	 * Loads a CSS file by adding in the page a new link element.
	 * @param {String} url The URL from which to load the CSS.
	 */
	loadCss: function(url) {
		var me = this;
		if(!me.loadedCss) me.loadedCss = {};
		if(!me.loadedCss[url]) {
			var doc = window.document;
			var link = doc.createElement('link');
			link.rel = 'stylesheet';
			link.type = 'text/css';
			link.href = url;
			doc.getElementsByTagName('head')[0].appendChild(link);
			me.loadedCss[url] = url;
		}
	},
	
	/**
	 * Asynchronously loads the specified script URL and calls the supplied 
	 * callbacks. A success flag is passed to callback function in order to
	 * determine operation result.
	 * @param {String} url The URL from which to load the script.
	 * @param {Function} cb The callback to call.
	 * @param {Object} scope The scope (this) for the supplied callbacks.
	 */
	loadScriptAsync: function(url,cb,scope) {
		var me = this;
		Ext.Loader.loadScript({
			url: 'resources/'+url,
			onLoad: function() {
				Ext.callback(cb, scope || me, [true]);
			},
			onError: function() {
				Ext.callback(cb, scope || me, [false]);
			},
			scope: me
		});
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
	},
	
	info: function(msg, tit) {
		Ext.Msg.show({
			title: tit || WT.res('info'),
			message: msg,
			buttons: Ext.MessageBox.OK,
			icon: Ext.MessageBox.INFO
		});
	},
	
	warn: function(msg, tit) {
		Ext.Msg.show({
			title: tit || WT.res('warning'),
			message: msg,
			buttons: Ext.MessageBox.OK,
			icon: Ext.MessageBox.WARNING
		});
	},
	
	error: function(msg, tit) {
		Ext.Msg.show({
			title: tit || WT.res('error'),
			message: msg,
			buttons: Ext.MessageBox.OK,
			icon: Ext.MessageBox.ERROR
		});
	},
	
	wsMsg: function(service, action, config) {
		return Ext.JSON.encode(Ext.apply(config||{},{ service: service, action: action }));
	},
	
	/*
	 * Builds the src url of a themed image for a service
	 * 
	 * @param {String} sid The service id
	 * @param {String} relPath The relative icon path
	 * @return {String} the imageUrl
	 */
	imageUrl: function(sid, relPath) {
		return Ext.String.format('resources/{0}/laf/{1}/{2}',sid,WTStartup.laf,relPath);
	},
	
	/*
	 * Builds the img tag of a themed image for a service
	 * 
	 * @param {String} sid The service id
	 * @param {String} relPath The relative icon path
	 * @param {int} width The icon width
	 * @param {int} height The icon height
	 * @param {String} [others] other custom tag properties
	 * @return {String} the complete image tag
	 */
	imageTag: function(sid,relPath,width,height,others) {
		var src=this.imageUrl(sid,relPath);
		return Ext.String.format('<img src="{0}" width={1} height={2} {3} >',src,width,height,others||'');
	},
	
	/*
	 * Builds the img tag of a core generic image
	 * 
	 * @param {String} relPath The relative icon path
	 * @param {int} width The icon width
	 * @param {int} height The icon height
	 * @param {String} [others] other custom tag properties
	 * @return {String} the complete image tag
	 */
	coreImageTag: function(relPath,width,height,others) {
		var src=this.imageUrl(WT.ID,relPath);
		return Ext.String.format('<img src="{0}" width={1} height={2} {3} >',src,width,height,others||'');
	},
	
	/*
	 * Builds the src url of a global image
	 * 
	 * @param {String} relPath The relative icon path
	 * @return {String} the imageUrl
	 */
	globalImageUrl: function(relPath) {
		return Ext.String.format('resources/{0}/images/{1}',WT.ID,relPath);
	},
	
	/*
	 * Builds the img tag of a core generic image
	 * 
	 * @param {String} relPath The relative icon path
	 * @param {int} width The icon width
	 * @param {int} height The icon height
	 * @param {String} [others] other custom tag properties
	 * @return {String} the complete image tag
	 */
	globalImageTag: function(relPath,width,height,others) {
		var src=this.globalImageUrl(relPath);
		return Ext.String.format('<img src="{0}" width={1} height={2} {3} >',src,width,height,others||'');
	},
	
	/*
	 * Build human readable version of integer number
	 * @param {Integer} value The integer number.
	 * @return {String} A human readable string.
	 */
	getSizeString: function(value) {
		var s = value;
		value = parseInt(value/1024);
		if (value > 0) {
			if (value < 1024) s = value + "KB";
			else s = parseInt(value/1024) + "MB";
		}
		return s;
	}
	
});
