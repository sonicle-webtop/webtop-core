
Ext.define('Sonicle.webtop.core.app.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Ext.ux.WebSocketManager',
		'Ext.ux.WebSocket',
		'Sonicle.String',
		'Sonicle.Date',
		'Sonicle.PageMgr',
		'Sonicle.URLMgr',
		'Sonicle.PrintMgr',
		'Sonicle.DesktopNotificationMgr',
		'Sonicle.upload.Uploader',
		'Sonicle.data.proxy.Ajax',
		'Sonicle.data.identifier.NegativeString',
		'Sonicle.plugin.EnterKeyPlugin',
		'Sonicle.plugin.FieldTooltip',
		
		'Sonicle.webtop.core.ux.data.BaseModel',
		'Sonicle.webtop.core.ux.data.EmptyModel',
		'Sonicle.webtop.core.ux.data.SimpleModel',
		'Sonicle.webtop.core.ux.data.ArrayStore',
		
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.app.FileTypes',
		'Sonicle.webtop.core.app.Factory',
		'Sonicle.webtop.core.app.Util',
		'Sonicle.webtop.core.app.Log',
		'Sonicle.webtop.core.app.ThemeMgr',
		'Sonicle.webtop.core.app.ComManager',
		'Sonicle.webtop.core.app.ServiceDescriptor',
		
		'Sonicle.webtop.core.ux.panel.Panel',
		'Sonicle.webtop.core.ux.panel.Fields',
		'Sonicle.webtop.core.ux.panel.Form',
		'Sonicle.webtop.core.ux.panel.Tab'
		
	].concat(WTS.appRequires || []),
	views: [
		Ext.String.format('WT.view.main.{0}', WTS.layoutClassName)
	],
	refs: {
		viewport: 'viewport'
	},
	
	/**
	 * @property {Ext.util.HashMap} locales
	 * A collection of locale classes.
	 */
	locales: null,
	
	/**
	 * @property {Ext.util.Collection} services
	 * A collection of service descriptors.
	 */
	services: null,
	currentService: null,
	
	kaTask: null,
	seTask: null,
	
	constructor: function() {
		var me = this;
		WT.app = me;
		me.locales = Ext.create('Ext.util.HashMap');
		me.services = Ext.create('Ext.util.Collection');
		me.callParent(arguments);
	},
	
	init: function() {
		WT.Log.debug('application:init');
		Ext.tip.QuickTipManager.init();
		Ext.setGlyphFontFamily('FontAwesome');
		Ext.themeName = WTS.servicesVars[0].theme;
		Ext.getDoc().on('contextmenu', function(e) {
			e.preventDefault(); // Disable browser context if no context menu is defined
		});
		
		// Inits state provider
		if(Ext.util.LocalStorage.supported) {
			Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());
		} else {
			Ext.state.Manager.setProvider(new Ext.state.CookieProvider({
				expires: new Date(Ext.Date.now() + (1000*60*60*24*90)) // 90 days
			}));
		}
		
		WT.FileTypes.init(WTS.fileTypes);
	},
	
	removeLoading: function() {
		Ext.fly('wt-loading').animate({
			to: {opacity: 0},
			duration: 200,
			remove: true
		});
		Ext.fly('wt-loading-mask').animate({
			to: {opacity: 0.4},
			easing: 'bounceOut',
			duration: 1000,
			remove: true
		});
		
		/*
		var el = Ext.get('wt-loading'),
				box = el.getBox();
		el.animate({
			to: {opacity: 0},
			duration: 2500,
			remove: true
		});
		Ext.get('wt-loading-mask').animate({
			to: {
				x: box.x,
				y: box.y,
				width: box.width,
				height: box.height,
				opacity: 0
			},
			easing: 'bounceOut',
			duration: 5000,
			remove: true
		});
		*/
	
		/*
		     var loadingMask = Ext.get('loading-mask');
     var loading = Ext.get('loading');

     //  Hide loading message
     loading.fadeOut({ duration: 0.2, remove: true });

     //  Hide loading mask
     loadingMask.setOpacity(0.9);
     loadingMask.shift({
          xy: loading.getXY(),
          width: loading.getWidth(),
          height: loading.getHeight(),
          remove: true,
          duration: 1,
          opacity: 0.1,
          easing: 'bounceOut'
     });
		*/
	},
	
	launch: function() {
		var me = this,
				sv = WTS.servicesVars[0],
				desc, deps = [];
		
		// Loads service descriptors from startup object
		Ext.each(WTS.services, function(obj) {
			desc = Ext.create('WT.ServiceDescriptor', {
				index: obj.index,
				maintenance: obj.maintenance,
				id: obj.id,
				xid: obj.xid,
				ns: obj.ns,
				path: obj.path,
				version: obj.version,
				build: obj.build,
				serviceClassName: obj.serviceClassName,
				serviceVarsClassName: obj.serviceVarsClassName,
				localeClassName: obj.localeClassName,
				userOptions: obj.userOptions,
				name: obj.name,
				description: obj.description,
				company: obj.company
			});
			
			WT.loadCss(desc.getPath()+'/laf/'+sv['laf']+'/service.css');
			WT.loadCss(desc.getPath()+'/laf/'+sv['laf']+'/service-override.css');
			WT.loadCss(desc.getPath()+'/laf/'+sv['laf']+'/service-'+sv['theme']+'.css');
			WT.loadCss(desc.getPath()+'/laf/'+sv['laf']+'/service-override-'+sv['theme']+'.css');
			
			me.locales.add(obj.id, Ext.create(obj.localeClassName));
			me.services.add(desc);
		}, me);
		
		
		
		//Ext.require(deps, me.onRequiresLoaded, me);
		me.onRequiresLoaded.call(me);
	},
	
	onRequiresLoaded: function() {
		var me = this,
				def = null, vp, vpc;
		
		// Instantiates core service
		var cdesc = me.services.getAt(0);
		cdesc.getInstance();
		cdesc.initService();
		
		// Creates main viewport
		vp = me.viewport = me.getView(me.views[0]).create({
			servicesCount: me.services.count()-1 //TODO: calcolare il numero di servizi visibili
		});
		vpc = me.viewport.getController();
		
		// Inits loaded services and activate the default one
		Ext.each(me.getDescriptors(), function(desc) {
			if(!desc.getMaintenance()) {
				if(desc.initService()) {
					var svc = desc.getInstance();
					vp.addServiceButton(desc);
					if(svc.hasNewActions()) vp.addServiceNewActions(svc.getNewActions());
					// Saves first succesfully activated service for later displaying default
					if(def === null) def = desc.getId();
				}
			} else {
				//TODO: show grayed button
			}
		});
		
		// Sets default service
		if(WTS.defaultService) {
			var desc = me.getDescriptor(WTS.defaultService);
			if(desc.isInited()) def = WTS.defaultService;
		}
		if(def !== null) me.activateService(def);
		
		// If necessary, show whatsnew
		if(WT.getVar('isWhatsnewNeeded')) {
			vpc.showWhatsnew(false);
		}
		
		// Inits messages (webSocket/ServerEvents)
		WT.ComManager.on('receive', function(s,messages) {
			Ext.each(messages, function(msg) {
				if (msg && msg.service) {
					var svc = me.getService(msg.service);
					if(svc) svc.handleMessage(msg);
				}
			});
		});
		WT.ComManager.on('connectionlost', function(s) {
			WT.warn(WT.res('connectionlost'));
		});
		WT.ComManager.connect();
		
		me.removeLoading();
	},
	
	/**
	 * Returns desired locale instance.
	 * @param {String} id The service ID.
	 * @returns {WT.Locale}
	 */
	getLocale: function(id) {
		return this.locales.get(id);
	},
	
	/**
	 * Returns loaded service descriptors.
	 * @param {Boolean} [skip] False to include core descriptor. Default to true.
	 * @returns {WT.ServiceDescriptor[]}
	 */
	getDescriptors: function(skip) {
		if(!Ext.isDefined(skip)) skip = true;
		var ret = [];
		this.services.each(function(desc) {
			if(!skip || (desc.getIndex() !== 0)) { // Skip core descriptor at index 0
				Ext.Array.push(ret, desc);
			}
		});
		return ret;
	},
	
	/**
	 * Returns a service descriptor.
	 * @param {String} id The service ID.
	 * @returns {WT.ServiceDescriptor} The instance or undefined if not found. 
	 */
	getDescriptor: function(id) {
		return this.services.get(id);
	},
	
	/**
	 * Returns a service instance.
	 * @param {String} id The service ID.
	 * @returns {WT.sdk.Service} The instance or null if not found. 
	 */
	getService: function(id) {
		var desc = this.getDescriptor(id);
		return (desc) ? desc.getInstance() : null;
	},
	
	/**
	 * Activates (shows) specified service.
	 * @param {String} id The service ID.
	 */
	activateService: function(id) {
		var me = this;
		var svc = me.getService(id);
		if(!svc) return;
		var vpc = me.getViewport().getController();
		vpc.addServiceCmps(svc);
		me.currentService = id;
		if(vpc.activateService(svc)) svc.fireEvent('activate');
	}
});

Ext.override(Ext.data.proxy.Server, {
	constructor: function(cfg) {
		this.callOverridden([cfg]);
		this.addListener('exception', function(s,resp,op) {
			if(resp.status === 401) WT.reload();
		});
	}
});

Ext.override(Ext.menu.Item, {
	onClick: function(e) {
		e.menuData = WT.getContextMenuData();
		return this.callParent([e]);
	}
});
