
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Ext.ux.WebSocketManager',
		'Ext.ux.WebSocket',
		'Sonicle.Date',
		'Sonicle.data.identifier.NegativeString',
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Util',
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ComManager',
		'Sonicle.webtop.core.ServiceDescriptor'
	].concat(WTS.appRequires || []),
	views: [
		'Sonicle.webtop.core.view.Viewport'
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
		var me = WT.app = this;
		me.locales = Ext.create('Ext.util.HashMap');
		me.services = Ext.create('Ext.util.Collection');
		me.callParent(arguments);
	},
	
	init: function() {
		//var me = this;
		WT.Log.debug('application:init');
		Ext.tip.QuickTipManager.init();
		Ext.setGlyphFontFamily('FontAwesome');
	},
	
	launch: function() {
		var me = this;
		
		// Loads service descriptors from startup object
		var co = WTS.servicesOptions[0], desc = null, deps = [];
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
				localeClassName: obj.localeClassName,
				userOptions: obj.userOptions,
				name: obj.name,
				description: obj.description,
				company: obj.company
			});
			
			WT.loadCss(desc.getPath()+'/laf/'+co['laf']+'/service.css');
			WT.loadCss(desc.getPath()+'/laf/'+co['laf']+'/service-override.css');
			WT.loadCss(desc.getPath()+'/laf/'+co['laf']+'/service-'+co['theme']+'.css');
			WT.loadCss(desc.getPath()+'/laf/'+co['laf']+'/service-override-'+co['theme']+'.css');
			
			me.locales.add(obj.id, Ext.create(obj.localeClassName));
			me.services.add(desc);
		}, me);
		
		// Instantiates core service
		var cdesc = me.services.getAt(0);
		cdesc.getInstance();
		
		//Ext.require(deps, me.onRequiresLoaded, me);
		me.onRequiresLoaded.call(me);
	},
	
	onRequiresLoaded: function() {
		var me = this,
				def = null, vpc;
		
		// Creates main viewport
		me.viewport = me.getView('Sonicle.webtop.core.view.Viewport').create();
		vpc = me.viewport.getController();
		
		// Inits loaded services and activate the default one
		Ext.each(me.getDescriptors(), function(desc) {
			if(!desc.getMaintenance()) {
				if(desc.initService()) {
					var svc = desc.getInstance();
					vpc.addServiceButton(desc);
					if(svc.hasNewActions()) vpc.addServiceNewActions(svc.getNewActions());
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
		if(WT.getOption('isWhatsnewNeeded')) {
			vpc.buildWhatsnewWnd(false);
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
		vpc.addServiceCmp(svc);
		me.currentService = id;
		if(vpc.activateService(svc)) svc.fireEvent('activate');
	}
});
