
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Locale_'+WTStartup.locale,
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ServiceDescriptor',
		'Ext.ux.WebSocketManager',
		'Ext.ux.WebSocket'
		
	],
	views: [
		'Sonicle.webtop.core.view.Viewport'
	],
	refs: {
		viewport: 'viewport'
	},
	
	queue: null,
	services: null,
	
	constructor: function(cfg) {
		var me = this;
		me.queue = [];
		me.services = Ext.create('Ext.util.Collection');
		me.callParent(arguments);
	},
	
	init: function() {
		var me = this;
		WT.Log.debug('application:init');
		Ext.tip.QuickTipManager.init();
		Ext.setGlyphFontFamily('FontAwesome');
		
		// Loads service descriptors from startup object
		// and defines a loading queue
		me.queue = me.loadDescriptors().reverse();
		
		// Launch loading process...
		me.loadServices();
		
		me.runWebSocket();
	},
	
	buildUI: function() {
		var me = this;
		WT.Log.debug('application:buildUI');
		// Creates main viewport
		me.viewport = me.getView('Sonicle.webtop.core.view.Viewport').create();
		
		// Inits loaded services and activate the default one
		me.services.each(function(desc,i) {
			desc.initService();
			if(i === 0) me.activateService(desc.getId());
		});
	},
	
	/**
	 * Loads services configuration from the startup object
	 * defining a list of service descriptors.
	 * @private
	 * @returns {Array} Array of descriptors.
	 */
	loadDescriptors: function() {
		var obj = null, arr = [];
		Ext.each(WTStartup.services, function(cfg) {
			obj = Ext.create('WT.ServiceDescriptor', {
				id: cfg.id,
				xid: cfg.xid,
				name: cfg.name,
				description: cfg.description,
				version: cfg.version,
				build: cfg.build,
				company: cfg.company,
				className: cfg.className
			});
			arr.push(obj);
		}, this);
		return arr;
	},
	
	/**
	 * Starts the loading process using the previously defined queue.
	 * @private
	 */
	loadServices: function() {
		var me = this;
		if(me.queue.length === 0) { // Queue is empty!
			me.buildUI();
		} else { // Queue contains elements...
			// Pops and loads the last descriptor
			var desc = me.queue.pop();
			me.loadServicesWorker(desc);
		}
	},
	
	/**
	 * This is the async loading worker function.
	 * It tries to load service resources and on success it
	 * stores current descriptor into service collection.
	 * @private
	 * @param {WT.core.ServiceDescriptor} desc The service descriptor.
	 */
	loadServicesWorker: function(desc) {
		console.log('loadServicesWorker');
		var me = this, urls = [];
		
		// Register service paths into Ext classloader
		Ext.Loader.setPath(desc.getNs(), desc.getBaseUrl());
		
		// Defines urls to load
		WT.loadCss(desc.getBaseUrl()+'/laf/'+WTStartup.laf+'/service.css');
		WT.loadCss(desc.getBaseUrl()+'/laf/'+WTStartup.laf+'/service-'+WTStartup.theme+'.css');
		urls.push(Ext.Loader.getPath(desc.getClassName()));
		urls.push(Ext.Loader.getPath(desc.getNs()+'.Locale_'+WTStartup.locale));
		
		// Launch loader...
		console.log(urls);
		Ext.Loader.loadScript({
			url: urls,
			onLoad: function() {
				console.log('service loaded '+desc.getId());
				me.services.add(desc);
				me.loadServices();
			},
			onError: function() {
				console.log('Error loading service '+desc.getId());
				me.loadServices();
			},
			scope: me
		});
	},
	
	/**
	 * Returns a service instance.
	 * @param {String} svc The service id.
	 * @returns {WT.sdk.Service} The instance or null if the instance
	 * was not found. 
	 */
	getService: function(svc) {
		var desc = this.getDescriptor(svc);
		return (desc) ? desc.getInstance() : null;
	},
	
	getDescriptor: function(svcId) {
		return this.services.get(svcId);
	},
	
	activateService: function(svcId) {
		var svc = this.getService(svcId);
		if(!svc) return;
		var wpc = this.getViewport().getController();
		var cmp = null;
		
		if(Ext.isFunction(svc.getToolbar)) {
			cmp = svc.getToolbar.call(svc);
			if(!cmp) {
				cmp = Ext.create({xtype: 'toolbar'});
				svc.setToolbar(cmp);
			}
			wpc.setServiceToolbar(cmp);
		}
		if(Ext.isFunction(svc.getToolComponent)) {
			cmp = svc.getToolComponent.call(svc);
			if(!cmp) {
				cmp = Ext.create({xtype: 'panel', width: 150});
				svc.setToolComponent(cmp);
			}
			wpc.setServiceToolCmp(cmp);
		}
		if(Ext.isFunction(svc.getMainComponent)) {
			cmp = svc.getMainComponent.call(svc);
			if(!cmp) {
				cmp = Ext.create({xtype: 'panel'});
				svc.setMainComponent(cmp);
			}
			wpc.setServiceMainCmp(cmp);
		}
	},
	
	runWebSocket: function() {

		var websocket = Ext.create ('Ext.ux.WebSocket', {
			url: 'ws://'+window.location.hostname+':'+window.location.port+window.location.pathname+"wsmanager",
			listeners: {
				open: function (ws) {
					console.log ('Sending ticket to websocket: '+WTStartup.encAuthTicket);
					var config={
						service: "com.sonicle.webtop.core",
						action: "ticket",
						
						userId: WTStartup.userId,
						domainId: WTStartup.domainId,
						encAuthTicket: WTStartup.encAuthTicket
					};
					ws.send(Ext.JSON.encode(config));
				} ,
				close: function (ws) {
					console.log ('The websocket is closed!');
				} ,
				error: function (ws, error) {
					Ext.Error.raise (error);
				} ,
				message: function (ws, message) {
					console.log ('A new message is arrived: ' + message);
				}
			}
		});		
		
	}
});
