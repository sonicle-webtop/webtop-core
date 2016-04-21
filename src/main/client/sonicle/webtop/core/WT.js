Ext.define('Sonicle.webtop.core.WT', {
	alternateClassName: 'WT',
	singleton: true,
	
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
	
	app: null,
	
	palette: [
		'AC725E','D06B64','F83A22','FA573C','FF7537','FFAD46','FAD165','FBE983',
		'4986E7','9FC6E7','9FE1E7','92E1C0','42D692','16A765','7BD148','B3DC6C',
		'9A9CFF','B99AFF','A47AE2','CD74E6','F691B2','CCA6AC','CABDBF','C2C2C2',
		'FFFFFF'
	],
	
	filetypes: {
		pdf:    'acrobat',
		wav:    'audio',
		mp3:    'audio',
		aiff:   'audio',
		au:     'audio',
		wma:    'audio',
		ogg:    'audio',
		bin:    'binary',
		bmp:    'bmp',
		tar:    'compressed',
		zip:    'compressed',
		gz:     'compressed',
		z:      'compressed',
		doc:    'document',
		eml:    'envelope',
		png:    'gif',
		gif:    'gif',
		html:   'html',
		jpg:    'jpeg',
		jpeg:   'jpeg',
		ppt:    'presentation',
		xls:    'spreadsheet',
		txt:    'text',
		csv:    'text',
		tif:    'tif',
		wmv:    'video',
		avi:    'video',
		divx:   'video',
		mpeg:   'video',
		mp4:    'video',
		mov:    'video',
		asf:    'video'
	},
	
	loadedCss: null,
	
	constructor: function(cfg) {
		var me = this;
		me.loadedCss = {};
		me.callParent(cfg);
	},
	
	/**
	 * Returns the application.
	 * This is shorthand reference to Sonicle.webtop.core.getApplication().
	 * @returns {Sonicle.webtop.core.Application} The resulting object.
	 */
	getApp: function() {
		return this.app;
		//return Sonicle.webtop.core.getApplication();
	},
	
	getColorPalette: function() {
		return this.palette;
	},
	
	reload: function() {
		window.location.reload();
	},
	
	logout: function() {
		window.location = 'logout';
	},
	
	/**
	 * Convenience method for prepending namespace to class name.
	 * @param {String} [ns] The namespace. If not specified, WT.NS is used.
	 * @param {String} cn The class name.
	 * @returns {String} The resulting string.
	 */
	preNs: function(ns, cn) {
		if(arguments.length === 1) {
			cn = ns;
			ns = WT.NS;
		}
		return ns + '.' + cn;
	},
	
	/**
	 * Checks against a resource if specified action is allowed.
	 * @param {String} [id] The service ID
	 * @return {String} Corresponding service's XID
	 */
	findXid: function(id) {
		var desc = this.getApp().getDescriptor(id);
		return (desc) ? desc.getXid() : null;
	},
	
	/**
	 * Checks against a resource if specified action is allowed.
	 * @param {String} [id] The service ID.
	 * @param {String} resource The resource name.
	 * @param {String} action The action name.
	 * @return {Boolean} 'True' if action is allowed, 'False' otherwise.
	 */
	isPermitted: function(id, resource, action) {
		if(arguments.length === 2) {
			action = resource;
			resource = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.isPermitted(resource, action);
	},
	
	/**
	 * Gets the initial setting value bound to key.
	 * @param {String} [id] The service ID.
	 * @param {String} key The key.
	 * @return {Mixed} Setting value.
	 */
	getOption: function(id, key) {
		if(arguments.length === 1) {
			key = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.getOption(key);
	},
	
	/**
	 * Gets the initial setting value object bound to key.
	 * @param {String} [id] The service ID.
	 * @param {String} key The key.
	 * @return {Mixed} Setting value object.
	 */
	getOptionAsObject: function(id,key) {
		if(arguments.length === 1) {
			key = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.getOptionAsObject(key);
	},
	
	/**
	 * Gets the initial setting value bound to key.
	 * @param {String} [id] The service ID.
	 * @param {Object} opts Key/Value pairs object.
	 * @return {Mixed} Setting value.
	 */
	setOptions: function(id, opts) {
		if(arguments.length === 1) {
			opts = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.setOptions(opts);
	},
	
	/**
	 * Returns a string resource.
	 * If id and key are both filled, any other arguments will be used in
	 * conjunction with {@link Ext.String#format} method in order to replace
	 * tokens defined in resource string.
	 * @param {String} [id] The service ID.
	 * @param {String} key The resource key.
	 * @param {Mixed...} [values] The values to use within {@link Ext.String#format} method.
	 * @returns {String} The (formatted) value.
	 */
	res: function(id, key) {
		if(arguments.length === 1) {
			key = id;
			id = WT.ID;
		}
		
		var loc = WT.getApp().getLocale(id);
		if(!loc) return undefined;
		if(arguments.length > 2) {
			var str = loc.strings[key],
					args = Ext.Array.slice(arguments, 2);
			return (Ext.isDefined(str)) ? Ext.String.format(str, args) : loc.strings[key];
		} else {
			return loc.strings[key];
		}
	},
	
	/**
	 * Utility function to return a resource string or string itself.
	 * If passed string is a valid resource template (see below), 
	 * passed value will be evaluated and {@link #res} return value 
	 * will be returned as result.
	 * Resource template string can be: '{abc}' or '{abc@com.sonicle.webtop.myservice}'.
	 * The first one points at the resource with key 'abc' in the service 
	 * 'com.sonicle.webtop.core', while the second in service 'com.sonicle.webtop.myservice'.
	 * @param {String} [id] The service ID.
	 * @param {String} str The string.
	 * @returns {String} The value.
	 */
	resTpl: function(id, str) {
		if(arguments.length === 1) {
			str = id;
			id = WT.ID;
		}
		if(Ext.isString(str) && str.startsWith('{') && str.endsWith('}')) {
			var s = str.substr(1, str.length-2),
					tokens = s.split('@');
			return WT.res((tokens.length === 2) ? tokens[1] : id, tokens[0]);
		} else {
			return str; // No template defined...
		}
	},
	
	optionsProxy: function(svc) {
		return WTF.apiProxy(svc, 'UserOptions', 'data', {
			extraParams: {
				options: true
			}
		});
	},
	
	componentLoader: function(svc, act, opts) {
		if(!opts) opts = {};
		return {
			url: WTF.requestBaseUrl(),
			params: Ext.applyIf({
				service: svc,
				action: act
			}, opts.params || {}),
			contentType: 'html',
			loadMask: true
		};
	},
	
	/**
	 * Makes an Ajax request to server.
	 * @param {String} svc The service ID.
	 * @param {String} act The service action to call.
	 * @param {Object} [opts] Config options.
	 * @param {Object} [opts.params] Any custom params.
	 * @param {Function} [opts.callback] The callback function to call.
	 * @param {Boolean} opts.callback.success
	 * @param {Object} opts.callback.json
	 * @param {Object} opts.callback.opts
	 * @param {Object} [opts.scope] The scope (this) for the supplied callbacks.
	 */
	ajaxReq: function(svc, act, opts) {
		opts = opts || {};
		var me = this,
				fn = opts.callback, 
				scope = opts.scope, 
				sfn = opts.success, 
				ffn = opts.failure,
				hdrs = {};
		
		var obj = {
			method: 'POST',
			url: WTF.requestBaseUrl(),
			params: Ext.applyIf({
				service: svc,
				action: act
			}, opts.params || {}),
			success: function(resp, opts) {
				var obj = Ext.decode(resp.responseText);
				if(sfn) Ext.callback(sfn, scope || me, [opts]);
				Ext.callback(fn, scope || me, [obj.success, obj, opts]);
			},
			failure: function(resp, opts) {
				if(ffn) Ext.callback(ffn, scope || me, [opts]);
				Ext.callback(fn, scope || me, [false, null, opts]);
			},
			scope: me
		};
		if(opts.timeout) Ext.apply(obj, {timeout: opts.timeout});
		if(opts.jsonData) {
			Ext.apply(obj, {jsonData: opts.jsonData});
			hdrs['Content-Type'] = 'application/json';
		} else {
			hdrs['Content-Type'] = 'application/x-www-form-urlencoded; charset=utf-8';
		}
		Ext.apply(obj, {headers: hdrs});
		//headers: {"Content-Type": "application/x-www-form-urlencoded; charset=utf-8"},
		Ext.Ajax.request(obj);
	},
	
	/**
	 * Loads a CSS file by adding in the page a new link element.
	 * @param {String} url The URL from which to load the CSS.
	 */
	loadCss: function(url) {
		var me = this;
		//if(!me.loadedCss) me.loadedCss = {};
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
	 * Displays a message.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} opts.title A custom title.
	 * @param {Number} opts.buttons A custom bitwise button specifier.
	 * @param {Object} opts.config A custom {@link Ext.MessageBox} config.
	 */
	msg: function(msg, opts) {
		opts = opts || {};
		Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('info'),
			message: msg,
			buttons: opts.buttons || Ext.MessageBox.OK
		}, opts.config || {}));
	},
	
	/**
	 * Displays a message prompting for input.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing prompt configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} opts.title A custom title.
	 * @param {Function} [opts.fn] The callback function invoked after the message box is closed.
	 * @param {Object} [opts.scope=window] The scope (this reference) in which the callback is executed.
	 * @param {Boolean/Number} [opts.multiline=false] The scope (this reference) in which the callback is executed.
	 * @param {String} [opts.value=''] Default value of the text input element
	 */
	prompt: function(msg, opts) {
		opts = opts || {};
		Ext.Msg.prompt(
			opts.title || WT.res('prompt'),
			msg,
			opts.fn,
			opts.scope,
			opts.multiline,
			opts.value
		);
	},
	
	/**
	 * Displays an information message.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} opts.title A custom title.
	 * @param {Number} opts.buttons A custom bitwise button specifier.
	 * @param {Object} opts.config A custom {@link Ext.MessageBox} config.
	 */
	info: function(msg, opts) {
		opts = opts || {};
		Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('info'),
			message: msg,
			buttons: opts.buttons || Ext.MessageBox.OK,
			icon: Ext.MessageBox.INFO
		}, opts.config || {}));
	},
	
	/**
	 * Displays a warning message.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} opts.title A custom title.
	 * @param {Number} opts.buttons A custom bitwise button specifier.
	 * @param {Object} opts.config A custom {@link Ext.MessageBox} config.
	 */
	warn: function(msg, opts) {
		opts = opts || {};
		Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('warning'),
			message: msg,
			buttons: opts.buttons || Ext.MessageBox.OK,
			icon: Ext.MessageBox.WARNING
		}, opts.config || {}));
	},
	
	/**
	 * Displays an error message.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} opts.title A custom title.
	 * @param {Number} opts.buttons A custom bitwise button specifier.
	 * @param {Object} opts.config A custom {@link Ext.MessageBox} config.
	 */
	error: function(msg, opts) {
		opts = opts || {};
		Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('error'),
			message: msg,
			buttons: opts.buttons || Ext.MessageBox.OK,
			icon: Ext.MessageBox.ERROR
		}, opts.config || {}));
	},
	
	/**
	 * Displays a confirm message using classic YES+NO buttons.
	 * @param {String} msg The message to display.
	 * @param {Function} cb A callback function which is called after a choice.
	 * @param {String} cb.buttonId The ID of the button pressed.
	 * @param {Object} scope The scope (`this` reference) in which the function will be executed.
	 * @param {Object} [opts] Config options.
	 */
	confirm: function(msg, cb, scope, opts) {
		opts = opts || {};
		Ext.Msg.show({
			title: opts.title || WT.res('confirm'),
			message: msg,
			buttons: opts.buttons || Ext.Msg.YESNO,
			icon: Ext.Msg.QUESTION,
			fn: function(bid) {
				Ext.callback(cb, scope, [bid]);
			}
		});
	},
	
	/**
	 * Displays a confirm message using YES+NO+CANCEL buttons.
	 * @param {String} msg The message to display.
	 * @param {Function} cb A callback function which is called after a choice.
	 * @param {String} cb.buttonId The ID of the button pressed.
	 * @param {Object} scope The scope (`this` reference) in which the function will be executed.
	 * @param {Object} [opts] Config options.
	 */
	confirmYNC: function(msg, cb, scope, opts) {
		this.confirm(msg, cb, scope, Ext.apply({
			buttons: Ext.Msg.YESNOCANCEL
		}, opts));
	},
	
	/**
	 * Shows a desktop notification using browser.
	 * @param {String} sid The service ID.
	 * @param {Object} [opts] Config options.
	 * @param {Number} [opts.autoClose=5000] Auto close timeout in millis.
	 * @param {String} opts.title Notification title.
	 * @param {String} opts.body Notification body.
	 * @returns {Object} A wrapper containing a close() method to hide the notification. 
	 */
	showDesktopNotification: function(sid, opts) {
		opts = opts || {};
		var dn = WT.getOption('desktopNotification'),
				NtfMgr = Sonicle.DesktopNotificationMgr,
				desc, ico;
		
		if(dn === 'always' || (dn === 'auto' && !PageMgr.isHidden())) {
			desc = WT.getApp().getDescriptor(sid);
			ico = Ext.isIE ? 'wt.ico' : 'wt_32.png';
			return NtfMgr.notify(opts.title, {
				autoClose: opts.autoClose || 5000,
				icon: WTF.globalImageUrl(ico),
				body: opts.body || desc.getName()
			});
		}
		return;
	},
	
	/**
	 * Checks and if necessary display an authorization 
	 * request for using desktop notifications.
	 */
	checkDesktopNotificationAuth: function() {
		var dn = WT.getOption('desktopNotification');
		if(dn === 'always' || dn === 'auto') {
			Sonicle.DesktopNotificationMgr.ensureAuthorization();
		}
	},
	
	/**
	 * Convenience function that registers to contextmenu event of the provided
	 * component; when the event fires it automatically displays specified menu.
	 * Context data will be filled in best way as possible.
	 * @param {Ext.Component} cmp The component.
	 * @param {Ext.menu.Menu} menu Menu component to show during event.
	 */
	registerContextMenu: function(cmp, menu) {
		if(!menu || !menu.isXType('menu')) return;
		if(cmp.isXType('dataview')) {
			cmp.on('itemcontextmenu', function(s,rec,itm,i,e) {
				WT.showContextMenu(e, menu, {
					record: rec,
					item: itm,
					index: i
				});
			});
		}
	},
	
	/**
	 * Displays specified contextmenu in a centralized way.
	 * Any previous visible menu will be hide automatically.
	 * @param {Ext.event.Event} evt The raw event object.
	 * @param {Ext.menu.Menu} menu The menu component.
	 * @param {Object} data Useful data to pass (data will be saved into menu.menuData property).
	 */
	showContextMenu: function(evt, menu, data) {
		var me = this;
		evt.stopEvent();
		me.hideContextMenu();
		if(!menu || !menu.isXType('menu')) return;
		menu.menuData = data || {};
		me.contextMenu = menu;
		menu.on('hide', function(s) {
			s.menuData = {};
			me.contextMenu = null;
		}, me, {single: true});
		menu.showAt(evt.getXY());
	},
	
	/**
	 * Hides currently visible context menu.
	 */
	hideContextMenu: function() {
		var cxm = this.contextMenu;
		if(cxm) cxm.hide();
	},
	
	/**
	 * Returns context menu data previously saved into menu.menuData property.
	 * @returns {Object} The data object.
	 */
	getContextMenuData: function() {
		var cxm = this.contextMenu;
		return (cxm) ? cxm.menuData : null;
	},
	
	/**
	 * Creates a displayable view.
	 * @param {String} sid The service ID.
	 * @param {String} name The class name or alias.
	 * @param {Object} opts
	 * @param {Object} opts.viewCfg
	 * @param {Object} opts.containerCfg
	 * @returns {Ext.window.Window} The container.
	 */
	createView: function(sid, name, opts) {
		opts = opts || {};
		var svc = this.getApp().getService(sid);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+sid+']');
		return this.getApp().viewport.getController().createView(svc, name, opts);
	},
	
	/**
	 * Returns the ID of currently active (displayed) service.
	 * @returns {String}
	 */
	getActiveService: function() {
		return this.getApp().viewport.getController().active;
	},
	
	/**
	 * Returns the theme in use.
	 * Value is taken from core options 'theme'.
	 * @returns {String} The theme value.
	 */
	getTheme: function() {
		return WT.getOption('theme');
	},
	
	/**
	 * Returns if a touch theme is in use.
	 * @returns {Boolean}
	 */
	isTouchTheme: function() {
		return (this.getTheme().indexOf('touch') !== -1);
	},
	
	/**
	 * Returns the layout in use.
	 * Value is taken from core options 'layout'.
	 * @returns {String} The layout value.
	 */
	getLayout: function() {
		return WT.getOption('layout');
	},
	
	/**
	 * Returns the look&feel in use.
	 * Value is taken from core options 'laf'.
	 * @returns {String} The laf value.
	 */
	getLaf: function() {
		return WT.getOption('laf');
	},
	
	/**
	 * Returns the startDay in use (0=Sunday, 1=Monday).
	 * Value is taken from core options 'startDay'.
	 * @returns {Integer} The startDay value.
	 */
	getStartDay: function() {
		return WT.getOption('startDay');
	},
	
	/**
	 * Returns the timezone in use.
	 * Value is taken from core options 'timezone'.
	 * @returns {String} The timezone ID.
	 */
	getTimezone: function() {
		return WT.getOption('timezone');
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a short date. Remember that original option value follows 
	 * Java style patterns. Value is taken from core options 'shortDateFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getShortDateFmt: function() {
		var fmt = WT.getOption('shortDateFormat');
		return (Ext.isEmpty(fmt)) ? 'd/m/Y' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a long date. Remember that original option value follows 
	 * Java style patterns. Value is taken from core options 'longDateFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getLongDateFmt: function() {
		var fmt = WT.getOption('longDateFormat');
		return (Ext.isEmpty(fmt)) ? 'd/m/Y' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a short time. Remember that original option value follows 
	 * Java style patterns. Value is taken from core options 'shortTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getShortTimeFmt: function() {
		//g:i A', e.g., '3:15 PM'. For 24-hour time format try 'H:i'
		var fmt = WT.getOption('shortTimeFormat');
		return (Ext.isEmpty(fmt)) ? 'H:i' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a long time. Remember that original option value follows 
	 * Java style patterns. Value is taken from core options 'longTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getLongTimeFmt: function() {
		var fmt = WT.getOption('longTimeFormat');
		return (Ext.isEmpty(fmt)) ? 'H:i:s' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date+time format representing a short date+time.
	 * @returns {String} ExtJs format string.
	 */
	getShortDateTimeFmt: function() {
		return WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(); 
	},
	
	/**
	 * Returns if 24h time is in use.
	 * Value is taken from core options 'use24HourTime'.
	 * @returns {Boolean}
	 */
	getUse24HourTime: function() {
		return WT.getOption('use24HourTime');
	},
	
	print: function(html) {
		Sonicle.PrintMgr.print(html);
	}
});
