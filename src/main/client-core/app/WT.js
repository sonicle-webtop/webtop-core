/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.app.WT', {
	alternateClassName: ['WT'],
	singleton: true,
	
	/**
	 * Core service ID.
	 * @property {String} ID 
	 */
	ID: 'com.sonicle.webtop.core',
	
	/**
	 * Core service short ID.
	 * @property {String} XID 
	 */
	XID: 'wt',
	
	/**
	 * Core service namespace.
	 * @property {String} NS 
	 */
	NS: 'Sonicle.webtop.core',
	
	app: null,
	
	/**
	 * This object is an alias of {@link Ext#platformTags platformTags} properties that describe the current device or platform.
	 * @property {Object} plTags 
	 */
	
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
	
	uiid: function(suffix) {
		return this.getApp().uiid + '-' + suffix;
	},
	
	/**
	 * Returns the plaftorm name (according to re-branding options).
	 * @returns {String}
	 */
	getPlatformName: function() {
		return this.getApp().platformName;
	},
	
	/**
	 * Returns the base URL of the application.
	 * @returns {String}
	 */
	getAppBaseUrl: function() {
		return this.getApp().baseUrl;
	},
	
	/**
	 * Returns the base URL of the public images for the current domain.
	 * @returns {String}
	 */
	getPublicImagesUrl: function() {
		return WT.getAppBaseUrl() + 'resources/' + WT.getVar('domainInternetName') + '/images';
	},
	
	/**
	 * Returns the sessionId.
	 * @returns {String}
	 */
	getSessionId: function() {
		return WTS.sessionId;
	},
	
	/**
	 * Returns the security token used to protect calls against CSRF attaks.
	 * @returns {String}
	 */
	getSecurityToken: function() {
		return WTS.securityToken;
	},
	
	/**
	 * Checks if specified service is loaded.
	 * @param {String} sid The service ID.
	 */
	hasService: function(sid) {
		return this.getApp().hasDescriptor(sid);
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
	 * Convenience method for prepending namespace to class name.
	 * @param {String} [ns] The namespace. If not specified, WT.NS is used.
	 * @param {String} cn The class name.
	 * @returns {String} The resulting string.
	 */
	preNs: function(ns, cn) {
		if (arguments.length === 1) {
			cn = ns;
			ns = WT.NS;
		}
		return ns + '.' + cn;
	},
	
	/**
	 * Builds a state ID useful for saving data into local storage.
	 * @param {String} [xid] The service XID.
	 * @param {String} name The component or unique reference name.
	 * @returns {String} The generated ID
	 */
	buildStateId: function(xid, name) {
		if (arguments.length === 1) {
			name = xid;
			xid = WT.XID;
		}
		return xid + '-' + name;
	},
	
	/**
	 * Checks against a resource if specified action is allowed.
	 * @param {String} [id] The service ID.
	 * @return {String} Corresponding service's XID
	 */
	findXid: function(id) {
		var desc = this.getApp().getDescriptor(id);
		return (desc) ? desc.getXid() : null;
	},
	
	toPid: function(domainId, userId) {
		return userId + '@' + domainId;
	},
	
	fromPid: function(pid) {
		var tks = pid.split('@');
		return {
			domainId: tks[1],
			userId: tks[0]
		};
	},
	
	/**
	 * Gets the initial setting value bound to key.
	 * @param {String} [id] The service ID.
	 * @param {String} key The key.
	 * @return {Mixed} Setting value.
	 */
	getVar: function(id, key) {
		if(arguments.length === 1) {
			key = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.getVar(key);
	},
	
	/**
	 * Gets the initial setting value object bound to key.
	 * @param {String} [id] The service ID.
	 * @param {String} key The key.
	 * @return {Mixed} Setting value object.
	 */
	getVarAsObject: function(id,key) {
		if(arguments.length === 1) {
			key = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.getVarAsObject(key);
	},
	
	/**
	 * Sets the initial setting value bound to key.
	 * @param {String} [id] The service ID.
	 * @param {String} key The key.
	 * @param {Mixed} value The value.
	 * @return {Mixed} Setting value.
	 */
	setVar: function(id, key, value) {
		if(arguments.length === 2) {
			value = key;
			key = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.setVar(key,value);
	},
	
	/**
	 * Sets the initial setting value bound to key.
	 * @param {String} [id] The service ID.
	 * @param {Object} opts Key/Value pairs object.
	 * @return {Mixed} Setting value.
	 */
	setVars: function(id, opts) {
		if(arguments.length === 1) {
			opts = id;
			id = WT.ID;
		}
		var svc = this.getApp().getService(id);
		if(!svc) Ext.Error.raise('Unable to get service with ID ['+id+']');
		return svc.setVars(opts);
	},
	
	/**
	 * Tests if passed key value has the notation of an unmatched key.
	 * @param {String} key The resource key.
	 * @returns {Boolean} True if match, false otherwise.
	 */
	isUnmatchedResKey: function(key) {
		return Ext.String.startsWith(key, '${') && Ext.String.endsWith(key, '}');
	},
	
	/**
	 * Tries to parse passed string into a i18n resource template string.
	 * @param {String} s The string being parsed.
	 * @returns {Object} Result object with `result` and `key` properties;
	 */
	parseResTpl: function(s) {
		var res = Ext.isString(s) && Ext.String.startsWith(s, '{') && Ext.String.endsWith(s, '}');
		return {result: res, key: res ? s.substring(1, s.length-1) : undefined};
	},
	
	/**
	 * Returns the localized string associated to the key.
	 * If id and key are both filled, any other arguments will be used in
	 * conjunction with {@link Ext.String#format} method in order to replace
	 * tokens defined in resource string.
	 * @param {String} [id] The service ID.
	 * @param {String} key The resource key.
	 * @param {Mixed...} [values] The values to use within {@link Ext.String#format} method.
	 * @returns {String} The localized (optionally formatted) resource value or '${key}' if not found.
	 */
	res: function(id, key) {
		if(arguments.length === 1) {
			key = id;
			id = WT.ID;
		}
		var ExArr = Ext.Array,
				loc = WT.getApp().getLocale(id),
				str;
		
		// Returns the key itself whether locale or string are not defined
		if (!loc) return key;
		str = loc.strings[key];
		if (str === undefined) return '${'+key+'}';
		
		if (arguments.length > 2) {
			var args = [str].concat(ExArr.slice(arguments, 2));
			return Ext.isDefined(str) ? Ext.String.format.apply(this, args) : loc.strings[key];
		} else {
			return str;
		}
	},
	
	/**
	 * Checks if passes string has format of a i18n resource template string.
	 * @param {String} str The string to check.
	 * @returns {Boolean} `True` if string follows the template pattern, `false` otherwise.
	 */
	isResTpl: function(str) {
		return Ext.isString(str) && Ext.String.startsWith(str, '{') && Ext.String.endsWith(str, '}');
	},
	
	/**
	 * Extracts a message
	 * @param {String} sid The default service ID in which lookup strings.
	 * @param {Mixed} data The JSON payload data response.
	 * @returns {Object}
	 */
	extractMessage: function(sid, data) {
		var msg, keyTpl;
		if (Ext.isArray(data)) {
			keyTpl = data[0];
			msg = WT.resTpl(sid, keyTpl);
			if (data.length > 1) {
				msg = Ext.String.format.apply(this, [msg].concat(Ext.Array.slice(data, 1)));
			}
		}
		return {
			key: keyTpl,
			message: WT.isUnmatchedResKey(msg) ? null : msg
		};
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
	 * @param {String} tpl The string.
	 * @returns {String} The value.
	 */
	resTpl: function(id, tpl) {
		if (arguments.length === 1) {
			tpl = id;
			id = WT.ID;
		}
		if (this.isResTpl(tpl)) {
			var s = tpl.substr(1, tpl.length-2),
					tokens = s.split('@');
			return WT.res((tokens.length === 2) ? tokens[1] : id, tokens[0]);
		} else {
			return tpl; // No template defined...
		}
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
	 * @param {Function} [opts.fn] A callback function which is called after a choice.
	 * @param {String} opts.fn.buttonId The ID of the button pressed.
	 * @param {String} opts.fn.value Value of the input field if either `prompt` or `multiline` is true.
	 * @param {String} opts.fn.cfg The config object passed during creation.
	 * @param {Object} [opts.scope] The scope (`this` reference) in which the function will be executed.
	 * @param {String} [opts.itemId] The ID assigned to MessageBox, this prevents displaying two messages with same reference.
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox#show} config.
	 * @param {Object} [opts.instConfig] A custom {@link Ext.window.MessageBox} instance config.
	 * 
	 * @returns {Ext.window.MessageBox} The newly created message box instance.
	 */
	msg: function(msg, opts) {
		opts = opts || {};
		var exists = Ext.isString(opts.itemId) ? (Ext.ComponentQuery.query('messagebox#'+opts.itemId).length > 0) : false,
				// Component is destroyed only if X button is pressed, so define a sequenced function in order to properly clear the MessageBox!
				autoDestroyFn = function() { this.destroy(); },
				mbox;
		
		if (exists) {
			return null;
		} else {
			mbox = Ext.create('Ext.window.MessageBox', Ext.apply({itemId: opts.itemId, closeAction: 'destroy'}, opts.instConfig || {}));
			return mbox.show(Ext.apply({
				title: opts.title || WT.res('info'),
				message: msg,
				buttons: opts.buttons || Ext.MessageBox.OK,
				fn: Ext.isFunction(opts.fn) ? Ext.Function.createSequence(opts.fn, autoDestroyFn, mbox) : Ext.Function.bind(autoDestroyFn, mbox),
				scope: opts.scope || mbox
			}, opts.config || {}));
		}
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
	 * @param {Function} [opts.fn] A callback function which is called after a choice.
	 * @param {String} opts.fn.buttonId The ID of the button pressed.
	 * @param {String} opts.fn.value Value of the input field if either `prompt` or `multiline` is true.
	 * @param {String} opts.fn.cfg The config object passed during creation.
	 * @param {Object} [opts.scope] The scope (`this` reference) in which the function will be executed.
	 * @param {String} [opts.itemId] The ID assigned to MessageBox, this prevents displaying two messages with same reference.
	 * @param {Boolean} opts.keepLineBreaks True to disable line-breaks to HTML conversion
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox#show} config.
	 * @param {Object} [opts.instConfig] A custom {@link Ext.window.MessageBox} instance config.
	 * 
	 * @returns {Ext.window.MessageBox} The newly created message box instance.
	 */
	info: function(msg, opts) {
		opts = opts || {};
		var exists = Ext.isString(opts.itemId) ? (Ext.ComponentQuery.query('messagebox#'+opts.itemId).length > 0) : false,
				// Component is destroyed only if X button is pressed, so define a sequenced function in order to properly clear the MessageBox!
				autoDestroyFn = function() { this.destroy(); },
				mbox;
		
		if (exists) {
			return null;
		} else {
			mbox = Ext.create('Ext.window.MessageBox', Ext.apply({itemId: opts.itemId, closeAction: 'destroy'}, opts.instConfig || {}));
			return mbox.show(Ext.apply({
				title: opts.title || WT.res('info'),
				message: (opts.keepLineBreaks === true) ? msg : Sonicle.String.htmlLineBreaks(msg),
				buttons: opts.buttons || Ext.MessageBox.OK,
				icon: Ext.MessageBox.INFO,
				fn: Ext.isFunction(opts.fn) ? Ext.Function.createSequence(opts.fn, autoDestroyFn, mbox) : Ext.Function.bind(autoDestroyFn, mbox),
				scope: opts.scope || mbox
			}, opts.config || {}));
		}
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
	 * @param {Function} [opts.fn] A callback function which is called after a choice.
	 * @param {String} opts.fn.buttonId The ID of the button pressed.
	 * @param {String} opts.fn.value Value of the input field if either `prompt` or `multiline` is true.
	 * @param {String} opts.fn.cfg The config object passed during creation.
	 * @param {Object} [opts.scope] The scope (`this` reference) in which the function will be executed.
	 * @param {String} [opts.itemId] The ID assigned to MessageBox, this prevents displaying two messages with same reference.
	 * @param {Boolean} opts.keepLineBreaks True to disable line-breaks to HTML conversion
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox#show} config.
	 * @param {Object} [opts.instConfig] A custom {@link Ext.window.MessageBox} instance config.
	 * 
	 * @returns {Ext.window.MessageBox} The newly created message box instance.
	 */
	warn: function(msg, opts) {
		opts = opts || {};
		var exists = Ext.isString(opts.itemId) ? (Ext.ComponentQuery.query('messagebox#'+opts.itemId).length > 0) : false,
				// Component is destroyed only if X button is pressed, so define a sequenced function in order to properly clear the MessageBox!
				autoDestroyFn = function() { this.destroy(); },
				mbox;
		
		if (exists) {
			return null;
		} else {
			mbox = Ext.create('Ext.window.MessageBox', Ext.apply({itemId: opts.itemId, closeAction: 'destroy'}, opts.instConfig || {}));
			return mbox.show(Ext.apply({
				title: opts.title || WT.res('warning'),
				message: (opts.keepLineBreaks === true) ? msg : Sonicle.String.htmlLineBreaks(msg),
				buttons: opts.buttons || Ext.MessageBox.OK,
				icon: Ext.MessageBox.WARNING,
				fn: Ext.isFunction(opts.fn) ? Ext.Function.createSequence(opts.fn, autoDestroyFn, mbox) : Ext.Function.bind(autoDestroyFn, mbox),
				scope: opts.scope || mbox
			}, opts.config || {}));
		}
	},
	
	/**
	 * Displays an error message.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} [opts.title] A custom title.
	 * @param {Number} [opts.buttons] A custom bitwise button specifier.
	 * @param {Function} [opts.fn] A callback function which is called after a choice.
	 * @param {String} opts.fn.buttonId The ID of the button pressed.
	 * @param {String} opts.fn.value Value of the input field if either `prompt` or `multiline` is true.
	 * @param {String} opts.fn.cfg The config object passed during creation.
	 * @param {Object} [opts.scope] The scope (`this` reference) in which the function will be executed.
	 * @param {String} [opts.itemId] The ID assigned to MessageBox, this prevents displaying two messages with same reference.
	 * @param {Boolean} [opts.keepLineBreaks] True to disable line-breaks to HTML conversion.
	 * @param {Boolean} [opts.expandTpl] `False` to disable message template expansion.
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox#show} config.
	 * @param {Object} [opts.instConfig] A custom {@link Ext.window.MessageBox} instance config.
	 * 
	 * @returns {Ext.window.MessageBox} The newly created message box instance.
	 */
	error: function(msg, opts) {
		opts = opts || {};
		var txt = (!(opts.expandTpl === false) && WT.isResTpl(msg)) ? WT.resTpl(msg) : msg,
				exists = Ext.isString(opts.itemId) ? (Ext.ComponentQuery.query('messagebox#'+opts.itemId).length > 0) : false,
				// Component is destroyed only if X button is pressed, so define a sequenced function in order to properly clear the MessageBox!
				autoDestroyFn = function() { this.destroy(); },
				mbox;
		
		if (exists) {
			return null;
		} else {
			if (Ext.isEmpty(txt)) {
				WT.getApp().logStackTrace('[W] WT.error() called with NO message.', 3);
				
			} else {
				mbox = Ext.create('Ext.window.MessageBox', Ext.apply({itemId: opts.itemId, closeAction: 'destroy'}, opts.instConfig || {}));
				return mbox.show(Ext.apply({
					title: opts.title || WT.res('error'),
					message: (opts.keepLineBreaks === true) ? txt : Sonicle.String.htmlLineBreaks(txt),
					buttons: opts.buttons || Ext.MessageBox.OK,
					icon: Ext.MessageBox.ERROR,
					fn: Ext.isFunction(opts.fn) ? Ext.Function.createSequence(opts.fn, autoDestroyFn, mbox) : Ext.Function.bind(autoDestroyFn, mbox),
					scope: opts.scope || mbox
				}, opts.config || {}));
			}
		}
	},
	
	/**
	 * Displays a message prompting for input.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing prompt configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} opts.title A custom title.
	 * @param {Function} [opts.fn] A callback function which is called after a choice.
	 * @param {String} opts.fn.buttonId The ID of the button pressed.
	 * @param {String} opts.fn.value Value of the input field if either `prompt` or `multiline` is true.
	 * @param {String} opts.fn.cfg The config object passed during creation.
	 * @param {Object} [opts.scope=window] The scope (this reference) in which the callback is executed.
	 * @param {Boolean/Number} [opts.multiline=false] True to create a multiline textbox using the defaultTextHeight.
	 * @param {String} [opts.value=''] Default value of the text input element.
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox#show} config.
	 * @param {String} [opts.instClass] The full classname of the type of instance to create. Defaults to `Ext.window.MessageBox`.
	 * @param {Object} [opts.instConfig] A custom {@link Ext.window.MessageBox} instance config.
	 * @param {String} [opts.vtype] Pass validation to a standard text field prompt and enable validation and buttons enable/disable automation.
	 * @param {Boolean} [opts.allowBlank] Pass allowBlank config to a standard text field prompt, influencing buttons enable/disable automation.
	 * 
	 * @returns {Ext.window.MessageBox} The newly created message box instance.
	 */
	prompt: function(msg, opts) {
		opts = opts || {};
		var hasXclass = Ext.isString(opts.instClass),
				xclass = hasXclass ? opts.instClass : 'Ext.window.MessageBox',
				exists = Ext.isString(opts.itemId) ? (Ext.ComponentQuery.query('messagebox#'+opts.itemId).length > 0) : false,
				// Component is destroyed only if X button is pressed, so define a sequenced function in order to properly clear the MessageBox!
				autoDestroyFn = function() { this.destroy(); },
				obj, mbox;
		
		if (exists) {
			return null;
		} else {
			mbox = Ext.create(xclass, Ext.apply({itemId: opts.itemId, closeAction: 'destroy'}, opts.instConfig || {}));
			obj = {
				title: opts.title || WT.res('prompt'),
				message: msg,
				buttons: Ext.Msg.OKCANCEL,
				callback: Ext.isFunction(opts.fn) ? Ext.Function.createSequence(opts.fn, autoDestroyFn, mbox) : Ext.Function.bind(autoDestroyFn, mbox),
				scope: opts.scope,
				value: opts.value
			};
			if (Ext.isString(opts.instClass)) {
				Ext.apply(obj, {
					icon: null
				});
			} else {
				Ext.apply(obj, {
					prompt: true,
					minWidth: Ext.Msg.minPromptWidth,
					multiline: opts.multiline
				});
			}
			
			if (!hasXclass && mbox.textField) {
				mbox.textField.vtype = opts.vtype;
				if (opts.allowBlank === false) {
					var isValid = !Ext.isEmpty(opts.value);
					mbox.textField.allowBlank = opts.allowBlank;
					if (mbox.msgButtons.ok) mbox.msgButtons.ok.setDisabled(!isValid);
					if (mbox.msgButtons.yes) mbox.msgButtons.yes.setDisabled(!isValid);
				}
                if (opts.vtype) {
					mbox.textField.on('validitychange', function (e, isValid, o) {
						if (mbox.msgButtons.ok) mbox.msgButtons.ok.setDisabled(!isValid);
						if (mbox.msgButtons.yes) mbox.msgButtons.yes.setDisabled(!isValid);
					});
					mbox.textField.isValid();
                }
			}
			
			return mbox.show(Ext.apply(obj, opts.config || {}));
		}
	},
	
	/**
	 * Displays a confirm message using classic YES+NO buttons.
	 * @param {String} msg The message to display.
	 * @param {Function} fn A callback function which is called after a choice.
	 * @param {String} fn.buttonId The ID of the button pressed.
	 * @param {String} fn.value Value of the input field if either `prompt` or `multiline` is true.
	 * @param {String} fn.cfg The config object passed to show.
	 * @param {Object} scope The scope (`this` reference) in which the function will be executed.
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} [opts.title] A custom title.
	 * @param {Number} [opts.buttons] A custom bitwise button specifier.
	 * @param {Boolean} [opts.keepLineBreaks] True to disable line-breaks to HTML conversion.
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox#show} config.
	 * @param {String} [opts.instClass] The full classname of the type of instance to create. Defaults to `Ext.window.MessageBox`.
	 * @param {Object} [opts.instConfig] A custom {@link Ext.window.MessageBox} instance config.
	 * 
	 * @returns {Ext.window.MessageBox} The newly created message box instance.
	 */
	confirm: function(msg, fn, scope, opts) {
		opts = opts || {};
		var xclass = Ext.isString(opts.instClass) ? opts.instClass : 'Ext.window.MessageBox',
				exists = Ext.isString(opts.itemId) ? (Ext.ComponentQuery.query('messagebox#'+opts.itemId).length > 0) : false,
				// Component is destroyed only if X button is pressed, so define a sequenced function in order to properly clear the MessageBox!
				autoDestroyFn = function() { this.destroy(); },
				callbackFn = function(bid, value, cfg) { Ext.callback(fn, scope, [bid, value, cfg]); },
				mbox;
		
		if (exists) {
			return null;
		} else {
			mbox = Ext.create(xclass, Ext.apply({itemId: opts.itemId, closeAction: 'destroy'}, opts.instConfig || {}));
			return mbox.show(Ext.apply({
				title: opts.title || WT.res('confirm'),
				message: (opts.keepLineBreaks === true) ? msg : Sonicle.String.htmlLineBreaks(msg),
				buttons: opts.buttons || Ext.Msg.YESNO,
				icon: opts.icon || Ext.Msg.QUESTION,
				fn: Ext.Function.createSequence(callbackFn, autoDestroyFn, mbox)
				/*
				fn: function(bid, value, cfg) {
					Ext.callback(cb, scope, [bid, value, cfg]);
				}
				*/
			}, opts.config || {}));
		}
	},
	
	/**
	 * Displays a confirm message using YES+NO+CANCEL buttons.
	 * @param {String} msg The message to display.
	 * @param {Function} cb A callback function which is called after a choice.
	 * @param {String} cb.buttonId The ID of the button pressed.
	 * @param {Object} scope The scope (`this` reference) in which the function will be executed.
	 * @param {Object} [opts] Config options.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} [opts.title] A custom title.
	 * @param {Number} [opts.buttons] A custom bitwise button specifier.
	 * @param {Boolean} [opts.keepLineBreaks] True to disable line-breaks to HTML conversion
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox#show} config.
	 * @param {Object} [opts.instConfig] A custom {@link Ext.window.MessageBox} instance config.
	 * 
	 * @returns {Ext.window.MessageBox} The newly created message box instance.
	 */
	confirmYNC: function(msg, cb, scope, opts) {
		return this.confirm(msg, cb, scope, Ext.apply({
			buttons: Ext.Msg.YESNOCANCEL
		}, opts));
	},
	
	rawMessage: function(rawValue, opts) {
		opts = opts || {};
		if (opts.selectAll === undefined) opts.selectAll = true;
		if (opts.width === undefined) opts.width = 250;
		if (opts.textHeight === undefined) opts.textHeight = 150;
		var msg = Ext.Msg.show({
			title: opts.title,
			message: opts.message,
			buttons: Ext.Msg.OK,
			multiline: true,
			width: opts.width || Ext.Msg.minPromptWidth,
			defaultTextHeight: opts.textHeight || Ext.Msg.defaultTextHeight,
			value: rawValue
		});
		msg.textArea.setEditable(false);
		if (opts.selectAll) msg.textArea.focus(true, 200);
		return msg;
	},
	
	/**
	 * Convenience function that registers to contextmenu event of the provided
	 * component; when the event fires it automatically displays specified menu.
	 * Context data will be filled in best way as possible.
	 * @param {Ext.Component} cmp The component.
	 * @param {Ext.menu.Menu} menu Menu component to show during event.
	 */
	registerContextMenu: function(cmp, menu) {
		if (!menu || !menu.isXType('menu')) return;
		if (cmp.isXType('dataview')) {
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
	 * @deprecated Use {@link Sonicle.Utils#showContextMenu} instead
	 */
	showContextMenu: function(evt, menu, data) {
		Ext.log.warn('WT.showContextMenu is deprecated. Use Sonicle.Utils.showContextMenu instead.');
		return Sonicle.Utils.showContextMenu(evt, menu, data);
	},
	
	/**
	 * @deprecated Use {@link Sonicle.Utils#hideContextMenu} instead
	 */
	hideContextMenu: function() {
		Ext.log.warn('WT.hideContextMenu is deprecated. Use Sonicle.Utils.hideContextMenu instead.');
		return Sonicle.Utils.hideContextMenu();
	},
	
	/**
	 * @deprecated Use {@link Sonicle.Utils#getContextMenuData} instead
	 */
	getContextMenuData: function() {
		Ext.log.warn('WT.getContextMenuData is deprecated. Use Sonicle.Utils.getContextMenuData instead.');
		return Sonicle.Utils.getContextMenuData();
	},
	
	/**
	 * Activates debug mode.
	 */
	debug: function() {
		var me = this;
		me.ajaxReq(me.ID, 'ActivateDebug', {
			callback: function(success, json) {
				if (success) me.info('Debug mode is on. Please press F5 in order to reload files.');
			}
		});
	},
	
	/**
	 * Makes an Ajax request to server.
	 * @param {String} svc The service ID.
	 * @param {String} act The service action to call.
	 * @param {Object} [opts] Config options.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {POST|GET} [opts.method] Sets the method, defaults to `POST`.
	 * @param {Object} [opts.timeout] The number of milliseconds to wait for a response. Defaults to {@link Ext.Ajax#timeout}.
	 * @param {Object} [opts.params] Extra request params.
	 * @param {Object} [opts.jsonData] Data attached to the request sent as payload.
	 * @param {Function} [opts.callback] The callback function to call.
	 * @param {Boolean} opts.callback.success
	 * @param {Object} opts.callback.json
	 * @param {Object} opts.callback.opts
	 * @param {Function} [opts.rawCallback] The raw callback function to call.
	 * @param {Object} [opts.scope] The scope (this) for the supplied callback.
	 */
	ajaxReq: function(svc, act, opts) {
		opts = opts || {};
		var me = this,
				fn = opts.callback, 
				rfn = opts.rawCallback,
				scope = opts.scope, 
				sfn = opts.success, 
				ffn = opts.failure,
				hdrs = {};
		
		var obj = {
			method: opts.method || 'POST',
			url: WTF.requestBaseUrl(),
			params: Ext.applyIf({
				service: svc,
				action: act
			}, opts.params || {}),
			success: function(resp, opts) {
				if (Ext.isFunction(rfn)) {
					Ext.callback(rfn, scope || me, [true, resp, opts]);
				} else {
					var json = Ext.decode(resp.responseText);
					if (sfn) Ext.callback(sfn, scope || me, [resp, opts]);
					Ext.callback(fn, scope || me, [json['success'], json, json['metaData'], opts]);
				}
			},
			failure: function(resp, opts) {
				if (Ext.isFunction(rfn)) {
					Ext.callback(rfn, scope || me, [false, resp, opts]);
				} else {
					if (ffn) Ext.callback(ffn, scope || me, [resp, opts]);
					Ext.callback(fn, scope || me, [false, {}, null, opts]);
				}
			},
			scope: me
		};
		if (opts.timeout) Ext.apply(obj, {timeout: opts.timeout});
		if (opts.jsonData) {
			Ext.apply(obj, {jsonData: opts.jsonData});
			hdrs['Content-Type'] = 'application/json';
		} else {
			hdrs['Content-Type'] = 'application/x-www-form-urlencoded; charset=utf-8';
		}
		Ext.apply(obj, {headers: hdrs});
		//headers: {"Content-Type": "application/x-www-form-urlencoded; charset=utf-8"},
		Ext.Ajax.request(obj);
	},
	
	handleRequestError: function(sid, act, req, op) {
		if (!req.aborted) {
			if (req.status === 200) {
				var msg = op.getError();
				if (!Ext.isEmpty(msg)) {
					WT.error(msg);
				} else {
					WT.error(WT.res(WT.ID, 'error.request.action', act, sid));
				}
			}
		}
	},
	
	handleRequestMetaError: function(metaErr) {
		if (metaErr) {
			if (!Ext.isEmpty(metaErr.text)) {
				WT.error(metaErr.text);
			} else if (!Ext.isEmpty(metaErr.res)) {
				var msg = WT.resTpl(metaErr.res);
				if (msg === metaErr.res) {
					return WT.res(WT.ID, msg);
				} else {
					return msg;
				}
			}
		}
	},
	
	/**
	 * Handles an error by displaying the passed message in case of unsuccessful operation.
	 * @param {Boolean} success Specified whether the operation was successful or not.
	 * @param {Object|String} json A JSON object in which look for `message` property or a direct message String.
	 */
	handleError: function(success, json) {
		if (!success) {
			var msg = Ext.isString(json) ? json : (json ? json.message : null);
			if (!Ext.isEmpty(msg)) {
				WT.error(msg);
			} else {
				if (console && console.warn) console.warn('Method handleError called with NO message');
			}
		}
	},
	
	/**
	 * Shows a pop-up notification (aka toast).
	 * @param {String} text Text.
	 * @param {Object} [opts] An object containing message configuration.
	 * This object may contain any of the following properties:
	 * 
	 * @param {Boolean/Number} [opts.autoClose=true] The number of milliseconds before autoclose or `true` to autoclose toast after 3 sec.
	 * @param {Number/String} [opts.width] The width of this component. A numeric value will be interpreted as the number of pixels; a string value will be treated as a CSS value with units.
	 * @param {Boolean} [opts.closable=false] True to display the 'close' tool button and allow the user to close the window.
	 * @params {Object[]} [opts.buttons] Convenience config. Short for 'Bottom Bar'. Button xtype and flex will be applied automatically.
	 * 
	 * @param {Object} [toastCfg] Custom toast config.
	 * 
	 * @returns {Ext.window.Toast}
	 */
	toast: function(text, opts, toastCfg) {
		opts = opts || {};
		
		// Old style call...gbulfon update your code and remove it when done!
		if (arguments.length === 1 && Ext.isObject(text)) {
			return Ext.toast(text);
		}
		// ------------------------------------------------------------------
		
		var cfg = {
			header: false,
			layout: 'fit',
			bodyPadding: 10,
			align: 't',
			autoClose: true,
			autoCloseDelay: 3000,
			minWidth: 200
		};
		
		if (Ext.isString(text)) {
			Ext.apply(cfg, {
				items: [{
					xtype: 'label',
					text: text
				}]
			});
		}
		if (opts.width) {
			Ext.apply(cfg, {
				width: opts.width
			});
		}
		if (opts.autoClose === false) {
			Ext.apply(cfg, {
				autoClose: false
			});
		} else if (Ext.isNumber(opts.autoClose) && (opts.autoClose > 0)) {
			Ext.apply(cfg, {
				autoClose: true,
				autoCloseDelay: opts.autoClose
			});
		}
		if (opts.closable) {
			Ext.apply(cfg, {
				tbar: ['->', {
					xtype: 'tool',
					type: 'close',
					handler: function() {
						this.findParentByType('toast').close();
					}
				}]
			});
		}
		if (Ext.isArray(opts.buttons)) {
			var items = [];
			Ext.iterate(opts.buttons, function(bcfg) {
				if (bcfg.glyph) {
					if (Ext.isString(bcfg.iconCls)) {
						bcfg.iconCls += ' wt-opacity-100';
					} else {
						bcfg.iconCls = 'wt-opacity-100';
					}
				}
				items.push(Ext.apply({}, bcfg, {
					xtype: 'button',
					flex: 1
				}));
			});
			items.push(' ');
			Ext.apply(cfg, {
				bbar: items
			});
		}
		if (toastCfg) {
			Ext.apply(cfg, toastCfg);
		}
		return Ext.toast(cfg);
	},
	
	reload: function() {
		window.location.reload();
	},
	
	openGoogleMaps: function(opts) {
		//https://blog.merlinox.com/google-maps-url-parameters/
		//https://developers.google.com/maps/documentation/urls/guide
		var params = {api: 1};
		if (!Ext.isEmpty(opts.query)) params.query = opts.query;
		Sonicle.URLMgr.open(Ext.String.urlAppend('https://www.google.com/maps/search/', Ext.Object.toQueryString(params)), true);
	},
	
	/**
	 * Returns the theme in use.
	 * Value is taken from core variable 'theme'.
	 * @returns {String} The theme value.
	 */
	getTheme: function() {
		return WT.getVar('theme');
	},
	
	/**
	 * Returns if a touch theme is in use.
	 * @returns {Boolean}
	 */
	isTouchTheme: function() {
		return (this.getTheme().indexOf('touch') !== -1);
	},
	
	/**
	 * Returns the look&feel in use.
	 * Value is taken from core variable 'laf'.
	 * @returns {String} The laf value.
	 */
	getLaf: function() {
		return WT.getVar('laf');
	},
	
	/**
	 * Returns available font list in the form 'name=family'.
	 * Value is taken from core variable 'wtEditorFonts'.
	 * @returns {String[]} The fonts.
	 */
	getEditorFonts: function() {
		return Sonicle.String.parseArray(WT.getVar('wtEditorFonts'), null, function(name) {
			var ff = Sonicle.CssUtils.findFontFamily(name) || Sonicle.CssUtils.toFontFamily(name);
			return name + '=' + ff;
		});
	},
	
	/**
	 * Returns available font sizes list.
	 * Value is taken from core variable 'wtEditorFontSizes'.
	 * @returns {String[]} The font sizes.
	 */
	getEditorFontSizes: function() {
		return Sonicle.String.parseArray(WT.getVar('wtEditorFontSizes'), null);
	},
	
	/**
	 * Checks if passed URL looks like a popular meeting URL.
	 * @param {String} url The link URL to check.
	 * @returns {Boolean}
	 */
	/*
	isPopularMeetingUrl: function(url) {
		var arr = Sonicle.String.parseKVArray(WT.getVar('wtPopMeetingProviders'), null),
			sWith = function(popUrl) {
				return Sonicle.String.startsWith(url, popUrl, true);
			}, i;
		if (Ext.isArray(arr)) {
			for (i=0; i<arr.length; i++) {
				if (sWith(arr[i][1])) return true;
			}
		}
		return false;
	},
	*/
	
	/**
	 * Checks if passed URL looks like a meeting URL: like those of popular 
	 * online services or that configured as local meeting provider.
	 * @param {String} url The link URL to check.
	 * @returns {Boolean}
	 */
	isMeetingUrl: function(url) {
		var me = this,
				re = me.reMeetingProvUrls;
		if (re === undefined) {
			me.reMeetingProvUrls = re = me.getMeetingProvidersURLsRegExp();
		}
		return re ? re.test(url) : false;
	},
	
	/**
	 * Clears the saved State of the passed component. This will ask the user for
	 * two confirmations: one before the operation (skippable using `silent` parameter) 
	 * and the second after clearing state when page needs a reload. 
	 * @param {Ext.Component} comp The component that implements {@link Ext.state.Stateful Stateful mixin}.
	 * @param {Boolean} silent Set to `true` to NOT ask for initial confirmation.
	 */
	clearState: function(comp, silent) {
		var me = this,
				stateId = comp && Ext.isFunction(comp.getStateId) ? comp.getStateId() : null;
		if (!Ext.isEmpty(comp)) {
			var doClearState = function() {
				Ext.state.Manager.clear(stateId);
				WT.confirm(me.res('confirm.stateClear.needReload'), function(bid) {
					if (bid === 'yes') WT.reload();
				});
			};
			if (silent) {
				doClearState();
			} else {
				WT.confirm(me.res('confirm.component.stateClear'), function(bid) {
					if (bid === 'yes') doClearState();
				});
			}
		}
	}
});
