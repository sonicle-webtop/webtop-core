/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.DesktopNotificationMgr', {
	singleton: true,
	
	config: {
		/**
		 * @cfg {String} direction
		 * Indicates the text direction of the notification.
		 * Supported values are: 'auto' (adopts the browser's language setting 
		 * behaviour), 'ltr' (left to right), 'rtl' (right to left).
		 */
		direction: 'auto',
		
		/**
		 * @cfg {Number} [autoClose=0]
		 * Specifies the default auto-close timeout in millis (0 is disabled).
		 */
		autoClose: 0
	},
	
	PERM_GRANTED: 'granted',
	PERM_DEFAULT: 'default',
	PERM_DENIED: 'denied',
	
	/**
	 * @readonly
	 * @property {Boolean} api
	 */
	api: false,
	
	/**
	 * @private
	 * @property {Number} ieSeed
	 */
	ieSeed: -1,
	
	constructor: function(cfg) {
		var me = this;
		me.initConfig(cfg);
		me.callParent([cfg]);
		me.ieSeed = Math.floor((Math.random()*10) +1),
		me.api = me.checkApi();
	},
	
	/**
	 * Checks if Notification API is supported.
	 * @return {Boolean}
	 */
	isSupported: function() {
		return this.api;
	},
	
	/*
	 * Forces an authorization check.
	 * If permission level is equal to {@link #PERM_DEFAULT default}, 
	 * the authorization window will be prompted to the user.
	 */
	ensureAuthorization: function() {
		var me = this;
		if(me.api && me.permissionLevel() === me.PERM_DEFAULT) {
			me.requestPermission();
		}
	},
	
	/**
	 * Displays a desktop notification.
	 * @param {String} title The notification's title.
	 * @param {Object} opts
	 * @param {String} opts.icon The notification's icon.
	 * For IE needs to be an .ico resource with 16px max, otherwise an image of 32px.
	 * @param {String} [opts.body] The notification’s subtitle.
	 * @param {String} [opts.tag] The notification’s unique identifier.
	 * This prevents duplicate entries from appearing if the user has multiple 
	 * instances of your website open at once (only for Chrome 22+, FF 22+, Safari 6+).
	 * @param {Number} [opts.autoClose] The number of milliseconds after that 
	 * the notification will be automatically closed. Defaults to {@link #autoClose} value.
	 * @return {Object} An object wrapper that defines the close() method.
	 */
	notify: function(title, opts) {
		opts = opts || {};
		var me = this, 
				auto = opts.autoClose || me.getAutoClose(),
				ntf, ntfWrapper;
		
		if(!me.api) return;
		if(!Ext.isString(title)) Ext.Error.raise('Title is required');
		if(!Ext.isDefined(opts.icon)) Ext.Error.raise('Icon is required');
		
		ntf = me.createNotification(title, opts);
		ntfWrapper = me.createCloseWrapper(ntf);
		
		if((auto > 0) && ntf && !ntf.ieSeed && ntf.addEventListener) {
			ntf.addEventListener("show", function() {
				Ext.defer(ntfWrapper.close, auto, me);
			});
		}
		return ntfWrapper;
	},
	
	/**
	 * Displays the authorization window to the user.
	 * @param {Function} [callback] The callback function invoked after the authorization box is closed.
	 */
	requestPermission: function(callback) {
		var me = this,
				win = window, cbFn;
		
		if(!me.api) return;
		cbFn = Ext.isFunction(callback) ? callback : Ext.emptyFn();
		if(win.webkitNotifications && win.webkitNotifications.checkPermission) {
			/**
			 * Chrome 23 supports win.Notification.requestPermission, but it
			 * breaks the browsers, so use the old-webkit-prefixed
			 * win.webkitNotifications.checkPermission instead.
			 * Firefox with html5notifications plugin supports this method
			 * for requesting permissions.
			 */
			win.webkitNotifications.requestPermission(cbFn);
		} else if (win.Notification && win.Notification.requestPermission) {
			win.Notification.requestPermission(cbFn);
		}
	},
	
	/**
	 * @private
	 */
	createCloseWrapper: function(ntf) {
		var win = window;
		return {
			close: function() {
				if(ntf) {
					if(ntf.close) {
						//http://code.google.com/p/ff-html5notifications/issues/detail?id=58
						ntf.close();
					} else if(ntf.cancel) {
						ntf.cancel();
					} else if (win.external && win.external.msIsSiteMode()) {
						if(ntf.ieSeed === this.ieSeed) win.external.msSiteModeClearIconOverlay();
					}
				}
			}
		};
	},
	
	/**
	 * @private
	 */
	createNotification: function(title, opts) {
		var me = this,
				win = window, ntf = null;
		opts = opts || {};
		
		if(!me.api) return;
		if(win.Notification) { /* Chrome 22+, FF 22+, Safari 6+ */
			ntf = new win.Notification(title, {
				/**
				 * The notification's icon - For Chrome in Windows, Linux & Chrome OS
				 */
				icon: Ext.isString(opts.icon) ? opts.icon : opts.icon.x32,
				/**
				 * The notification’s subtitle.
				 */
				body: opts.body || '',
				/**
				 * The notification’s unique identifier.
				 * This prevents duplicate entries from appearing if the user has multiple instances of your website open at once.
				 */
				tag: opts.tag || '',
				dir: me.getDirection()
			});
			
		} else if(win.webkitNotifications) { /* Chrome <22 & FF + html5notifications plugin */
			ntf = win.webkitNotifications.createNotification(opts.icon, title, opts.body || '');
			ntf.show();
			
		} else if(navigator.mozNotification) { /* FF <22 */
			ntf = navigator.mozNotification.createNotification(title, opts.body || '', opts.icon);
			ntf.show();
			
		} else if (win.external && win.external.msIsSiteMode()) { /* IE9+ */
			win.external.msSiteModeClearIconOverlay(); // Clears any previous notification
			win.external.msSiteModeSetIconOverlay((Ext.isString(opts.icon) ? opts.icon : opts.icon.x16), title);
			win.external.msSiteModeActivate();
			ntf = {"ieSeed": ieSeed+1};
		}
		return ntf;
	},
	
	permissionLevel: function() {
		var me = this, win = window, arr;
		
		if(!me.api) return;
		if(win.Notification && win.Notification.permissionLevel) { /* Safari 6+ */
			return win.Notification.permissionLevel;
			
		} else if(win.webkitNotifications && win.webkitNotifications.checkPermission) { /* Chrome <22 & FF + html5notifications plugin */
			arr = [me.PERM_GRANTED, me.PERM_DEFAULT, me.PERM_DENIED];
			arr[win.webkitNotifications.checkPermission()];
			
		} else if(win.Notification && win.Notification.permission) { /* Chrome 32+, FF 22+ */
			return win.Notification.permission;
			
		} else if(navigator.mozNotification) { /* FF <22 */
			return me.PERM_GRANTED;
			
		} else if (win.external && (win.external.msIsSiteMode() !== undefined)) { /* IE9+ */
			return win.external.msIsSiteMode() ? me.PERM_GRANTED : me.PERM_DEFAULT;
			
		} else {
			return;
		}
	},
	
	checkApi: function() {
		try {
			var win = window;
			/**
			 * We cannot detect if msIsSiteMode method exists, as it is
			 * a method of host object. In IE check for existing method of host
			 * object returns undefined. So, we try to run it - if it runs
			 * successfully - then it is IE9+, if not - an exceptions is thrown.
			 */
			return !!(win.Notification /* Chrome 22+, FF 22+, Safari 6+ */
					|| win.webkitNotifications /* Chrome <22 & FF + html5notifications plugin */
					|| navigator.mozNotification /* FF <22 */
					|| (win.external && win.external.msIsSiteMode() !== undefined) /* IE9+ */
					);
		} catch (e) {
			return false;
		}
	}
});
