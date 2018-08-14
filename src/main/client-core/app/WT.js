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
	 * @param {Object} opts.config A custom {@link Ext.MessageBox} config.
	 */
	msg: function(msg, opts) {
		opts = opts || {};
		return Ext.Msg.show(Ext.apply({
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
		return Ext.Msg.prompt(
			opts.title || WT.res('prompt'),
			msg,
			opts.fn,
			opts.scope,
			opts.multiline,
			opts.value
		);
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
	 * Displays an information message.
	 * @param {String} msg The message to display.
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} opts.title A custom title.
	 * @param {Number} opts.buttons A custom bitwise button specifier.
	 * @param {Boolean} opts.keepLineBreaks True to disable line-breaks to HTML conversion
	 * @param {Object} opts.config A custom {@link Ext.MessageBox} config to be applied directly.
	 */
	info: function(msg, opts) {
		opts = opts || {};
		return Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('info'),
			message: (opts.keepLineBreaks === true) ? msg : Sonicle.String.htmlLineBreaks(msg),
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
	 * @param {Object} opts.config A custom {@link Ext.MessageBox} config to be applied directly.
	 * @param {Boolean} opts.keepLineBreaks True to disable line-breaks to HTML conversion
	 */
	warn: function(msg, opts) {
		opts = opts || {};
		return Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('warning'),
			message: (opts.keepLineBreaks === true) ? msg : Sonicle.String.htmlLineBreaks(msg),
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
	 * @param {String} [opts.title] A custom title.
	 * @param {Number} [opts.buttons] A custom bitwise button specifier.
	 * @param {Boolean} [opts.keepLineBreaks] True to disable line-breaks to HTML conversion.
	 * @param {Boolean} [opts.expandTpl] `False` to disable message template expansion.
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox} config to be applied directly.
	 */
	error: function(msg, opts) {
		opts = opts || {};
		var txt = (!(opts.expandTpl === false) && WT.isResTpl(msg)) ? WT.resTpl(msg) : msg;
		return Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('error'),
			message: (opts.keepLineBreaks === true) ? txt : Sonicle.String.htmlLineBreaks(txt),
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
	 * @param {Object} [opts] An object containing message configuration.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {String} [opts.title] A custom title.
	 * @param {Number} [opts.buttons] A custom bitwise button specifier.
	 * @param {Boolean} [opts.keepLineBreaks] True to disable line-breaks to HTML conversion
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox} config to be applied directly.
	 */
	confirm: function(msg, cb, scope, opts) {
		opts = opts || {};
		return Ext.Msg.show(Ext.apply({
			title: opts.title || WT.res('confirm'),
			message: (opts.keepLineBreaks === true) ? msg : Sonicle.String.htmlLineBreaks(msg),
			buttons: opts.buttons || Ext.Msg.YESNO,
			icon: Ext.Msg.QUESTION,
			fn: function(bid) {
				Ext.callback(cb, scope, [bid]);
			}
		}, opts.config || {}));
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
	 * @param {Object} [opts.config] A custom {@link Ext.MessageBox} config to be applied directly.
	 */
	confirmYNC: function(msg, cb, scope, opts) {
		return this.confirm(msg, cb, scope, Ext.apply({
			buttons: Ext.Msg.YESNOCANCEL
		}, opts));
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
		if (!menu || !menu.isXType('menu')) return;
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
		if (cxm) cxm.hide();
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
	 * Makes an Ajax request to server.
	 * @param {String} svc The service ID.
	 * @param {String} act The service action to call.
	 * @param {Object} [opts] Config options.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {Object} [opts.timeout] The number of milliseconds to wait for a response. Defaults to {@link Ext.Ajax#timeout}.
	 * @param {Object} [opts.params] Extra request params.
	 * @param {Object} [opts.jsonData] Data attached to the request sent as payload.
	 * @param {Function} [opts.callback] The callback function to call.
	 * @param {Boolean} opts.callback.success
	 * @param {Object} opts.callback.json
	 * @param {Object} opts.callback.opts
	 * @param {Object} [opts.scope] The scope (this) for the supplied callback.
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
				var json = Ext.decode(resp.responseText);
				if(sfn) Ext.callback(sfn, scope || me, [resp, opts]);
				Ext.callback(fn, scope || me, [json['success'], json, json['metaData'], opts]);
			},
			failure: function(resp, opts) {
				if(ffn) Ext.callback(ffn, scope || me, [resp, opts]);
				Ext.callback(fn, scope || me, [false, null, null, opts]);
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
	 * Shows a pop-up notification (aka toast).
	 * @param {String} text Text.
	 * @param {Object} [opts] An object containing message configuration.
	 * @param {Object} [toastCfg] Custom toast config.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {Boolean/Number} [opts.autoClose=true] The number of milliseconds before autoclose or `true` to autoclose toast after 3 sec.
	 * @param {Number/String} [opts.width] The width of this component. A numeric value will be interpreted as the number of pixels; a string value will be treated as a CSS value with units.
	 * @param {Boolean} [opts.closable=false] True to display the 'close' tool button and allow the user to close the window.
	 * @params {Object[]} [buttons] Convenience config. Short for 'Bottom Bar'. Button xtype and flex will be applied automatically.
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
						bcfg.iconCls += ' wt-no-opacity';
					} else {
						bcfg.iconCls = 'wt-no-opacity';
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
	}
});
