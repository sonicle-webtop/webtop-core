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
	 * @property {Boolean} isSupported
	 */
	isSupported: null,
	/**
	 * @private
	 * @property {Number} ieSeed
	 */
	ieSeed: -1,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		me.ieSeed = Math.floor((Math.random()*10) +1),
		me.isSupported = me.checkSupport();
	},
	
	ensureAuthorization: function() {
		var me = this;
		if(me.isSupported && me.permissionLevel() === me.PERM_DEFAULT) {
			me.requestPermission();
		}
	},
	
	notify: function(title, opts) {
		opts = opts || {};
		var me = this, 
				auto = opts.autoClose || me.getAutoClose(),
				ntf, ntfWrapper;
		
		if(!me.isSupported) return;
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
	
	requestPermission: function(callback) {
		var me = this,
				win = window, cbFn;
		
		if(!me.isSupported) return;
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
	
	createNotification: function(title, opts) {
		var me = this,
				win = window, ntf = null;
		opts = opts || {};
		
		if(!me.isSupported) return;
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
		
		if(!me.isSupported) return;
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
	
	checkSupport: function() {
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
	},
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	permissionLevel22: function() {
		var me = this,
				win = window, arr;
		if((me.method === 'notification') && win.Notification.permissionLevel) { /* Safari 6 */
			return win.Notification.permissionLevel();
		} else if((me.method === 'notification') && win.Notification.permission) { /* Chrome (23+) */
			return win.Notification.permission;
		} else if((me.method === 'webkit') && win.webkitNotifications.checkPermission) { /* FF with html5Notifications plugin installed */
			arr = [me.PERM_GRANTED, me.PERM_DEFAULT, me.PERM_DENIED];
			return arr[win.webkitNotifications.checkPermission()];
		} else if(me.method === 'navigator') { /* Firefox Mobile */
			return me.PERM_GRANTED;
		} else if(me.method === 'ie') { /* IE9+ */
			return win.external.msIsSiteMode() ? me.PERM_GRANTED : me.PERM_DEFAULT;
		} else {
			return;
		}
	},
	
	createNotification22: function(title, opts) {
		var me = this,
				win = window, not;
		opts = opts || {};
		if(me.method === 'notification') { /* Safari 6, Chrome (23+) */
			not = new win.Notification(title, {
				/**
				 * The notification's icon - For Chrome in Windows, Linux & Chrome OS
				 */
				
				/**
				 * The notification’s subtitle.
				 */
				body: opts.body || '',
				/**
				 * The notification’s unique identifier.
				 * This prevents duplicate entries from appearing if the user has multiple instances of your website open at once.
				 */
				tag: opts.tag || ''
			});
		} else if(me.method === 'webkit') { /* FF with html5Notifications plugin installed */
			not = win.webkitNotifications.createNotification(opts.icon, title, opts.body);
			not.show();
		} else if(me.method === 'navigator') { /* Firefox Mobile */
			not = navigator.mozNotification.createNotification(title, opts.body, opts.icon);
			not.show();
		} else if(me.method === 'ie') { /* IE9+ */
			win.external.msSiteModeClearIconOverlay(); // Clears any previous notification
			win.external.msSiteModeSetIconOverlay((isString(options.icon) ? options.icon : options.icon.x16), title);
			win.external.msSiteModeActivate();
		}
		return not;
	},
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	checkSupport222: function() {
		var win = window;
		
		try {
			if(Ext.isChrome && Ext.chromeVersion < 22) { /* Chrome <22 */
				return !!navigator.webkitNotifications;
			} else if(Ext.isChrome && Ext.chromeVersion >= 22) { /* Chrome 22+ */
				return !!win.Notification;
			} else if(Ext.isGecko && Ext.firefoxVersion < 22) { /* FF <22 or FF + html5notifications plugin */
				return !!(navigator.mozNotification || win.webkitNotifications);
			} else if(Ext.isGecko && Ext.firefoxVersion >= 22) { /* FF 22+ */
				return !!win.Notification;
			} else if(Ext.isGecko) { /* FF + html5notifications plugin */
				return !!win.webkitNotifications
			}
		} catch(e) {}
		
			
		
		try { /* Safari, Chrome */
			if(!!win.Notification) method = 'notification';
		} catch(e) {}
		if(method != null) return method;
		try { /* Chrome & ff-html5notifications plugin */
			if(!!win.webkitNotifications) method = 'webkit';
		} catch(e) {}
		if(method != null) return method;
		try { /* Firefox Mobile */
			if(!!navigator.mozNotification) method = 'navigator';
		} catch(e) {}
		if(method != null) return method;
		try { /* IE9+ */
			if(win.external && win.external.msIsSiteMode() !== undefined) method = 'ie';
		} catch(e) {}
		return method;
	},
	
	detectMethod22: function() {
		var win = window,
				method = null;
		try { /* Safari, Chrome */
			if(!!win.Notification) method = 'notification';
		} catch(e) {}
		if(method != null) return method;
		try { /* Chrome & ff-html5notifications plugin */
			if(!!win.webkitNotifications) method = 'webkit';
		} catch(e) {}
		if(method != null) return method;
		try { /* Firefox Mobile */
			if(!!navigator.mozNotification) method = 'navigator';
		} catch(e) {}
		if(method != null) return method;
		try { /* IE9+ */
			if(win.external && win.external.msIsSiteMode() !== undefined) method = 'ie';
		} catch(e) {}
		return method;
	},
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Checks is browser supports desktop notifications.
	 * @returns {Boolean} true if browser is enabled, false otherwise.
	 */
	isSupported22: function() {
		try {
			var win = window;
			return !!(win.Notification /* Safari, Chrome */
					|| win.webkitNotifications /* Chrome & ff-html5notifications plugin */
					|| navigator.mozNotification /* Firefox Mobile */
					|| (win.external && win.external.msIsSiteMode() !== undefined) /* IE9+ */
					);
			/**
			 * We cannot detect if msIsSiteMode method exists, as it is
			 * a method of host object. In IE check for existing method of host
			 * object returns undefined. So, we try to run it - if it runs
			 * successfully - then it is IE9+, if not - an exceptions is thrown.
			 */
		} catch (e) {
			return false;
		}
	}
	
});
