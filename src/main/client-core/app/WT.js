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
		//return WTS.baseUrl;
	},
	
	/**
	 * Returns the base URL of the public images for the current domain.
	 * @returns {String}
	 */
	getPublicImagesUrl: function() {
		return WT.getAppBaseUrl()+"/resources/"+WT.getVar("domainInternetName")+"/images";
		//return WTS.baseUrl;
	},
	
	/**
	 * Returns the Push URL for websocket.
	 * @returns {String}
	 */
	getWsPushUrl: function() {
		return this.getApp().wsPushUrl;
		//return WTS.wsPushUrl;
	},
	
	/**
	 * Returns the security token used to protect calls against CSRF attaks.
	 * @returns {String}
	 */
	getSecurityToken: function() {
		return WTS.securityToken;
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
			opts = id;
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
		var ExArr = Ext.Array,
				loc = WT.getApp().getLocale(id);
		
		if(!loc) return undefined;
		if(arguments.length > 2) {
			var str = loc.strings[key],
					args = ExArr.merge([str], ExArr.slice(arguments, 2));
			return Ext.isDefined(str) ? Ext.String.format.apply(this, args) : loc.strings[key];
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
		var dn = WT.getVar('desktopNotification'),
				NtfMgr = Sonicle.DesktopNotificationMgr,
				desc, ico;
		
		if(dn === 'always' || (dn === 'auto' && !PageMgr.isHidden())) {
			desc = WT.getApp().getDescriptor(sid);
			//ico = Ext.isIE ? 'wt.ico' : 'wt_32.png';
			return NtfMgr.notify(opts.title, {
				autoClose: opts.autoClose || 5000,
				icon: WTF.globalImageUrl('wt.ico'),
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
		var dn = WT.getVar('desktopNotification');
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
	 * Makes an Ajax request to server.
	 * @param {String} svc The service ID.
	 * @param {String} act The service action to call.
	 * @param {Object} [opts] Config options.
	 * @param {Object} [opts.params] Any custom params.
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
	
	handleRequestError: function(sid, act, req, op) {
		if(!req.aborted) {
			if(req.status === 200) {
				var msg = op.getError();
				if(!Ext.isEmpty(msg)) {
					WT.error(msg);
				} else {
					WT.error(WT.res(WT.ID, 'error.request.action', act, sid));
				}
			}
		}
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
	},
	
	/**
	 * Returns the Service API interface.
	 * @param {String} id The service ID.
	 * @returns {Object} The service API object.
	 */
	getServiceAPI: function(id) {
		return WT.getApp().getServiceAPI(id);
	}
});
