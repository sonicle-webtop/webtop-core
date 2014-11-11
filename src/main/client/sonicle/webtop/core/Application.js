
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Ext.ux.WebSocketManager',
		'Ext.ux.WebSocket',
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ServiceDescriptor'
	].concat(WTStartup.appRequires || []),
	views: [
		'Sonicle.webtop.core.view.Viewport'
	],
	refs: {
		viewport: 'viewport'
	},
	
	services: null,
	currentService: null,
	
	constructor: function() {
		var me = this;
		me.services = Ext.create('Ext.util.Collection');
		me.callParent(arguments);
	},
	
	init: function() {
		var me = this;
		WT.Log.debug('application:init');
		Ext.tip.QuickTipManager.init();
		Ext.setGlyphFontFamily('FontAwesome');
		
		// Loads service descriptors from startup object
		var obj = null;
		Ext.each(WTStartup.services, function(cfg) {
			obj = Ext.create('WT.ServiceDescriptor', {
				id: cfg.id,
				xid: cfg.xid,
				ns: cfg.ns,
				path: cfg.path,
				className: cfg.className,
				name: cfg.name,
				description: cfg.description,
				version: cfg.version,
				build: cfg.build,
				company: cfg.company
			});
			WT.loadCss(obj.getPath()+'/laf/'+WTStartup.laf+'/service.css');
			WT.loadCss(obj.getPath()+'/laf/'+WTStartup.laf+'/service-'+WTStartup.theme+'.css');
			me.services.add(obj);
		}, me);
		
		// Inits webSocket
		me.initWebSocket();
	},
	
	launch: function() {
		var me = this;
		
		// Creates main viewport
		me.viewport = me.getView('Sonicle.webtop.core.view.Viewport').create();
		var vc = me.viewport.getController();
		
		// Inits loaded services and activate the default one
		var count = 0, first = null;
		me.services.each(function(desc) {
			if(desc.initService()) {
				count++;
				var svc = desc.getInstance();
				vc.addServiceButton(desc);
				if(svc.hasNewActions()) vc.addServiceNewActions(svc.getNewActions());
				if(count === 1) first = desc.getId();
			}
		});
		if(first) me.activateService(first);
		
		// If necessary, show whatsnew
		if(WT.getInitialSetting('isWhatsnewNeeded')) {
			vc.buildWhatsnewWnd(false);
		}
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
	 * Returns a service descriptor.
	 * @param {String} id The service ID.
	 * @returns {WT.ServiceDescriptor} The instance or undefined if not found. 
	 */
	getDescriptor: function(id) {
		return this.services.get(id);
	},
	
	/**
	 * Activates (shows) specified service.
	 * @param {String} id The service ID.
	 */
	activateService: function(id) {
		var me = this;
		var svc = me.getService(id);
		if(!svc) return;
		var wpc = me.getViewport().getController();
		wpc.addServiceCmp(svc);
		me.currentService = id;
		if(wpc.activateService(svc)) svc.fireEvent('activate');
	},
	
	initWebSocket: function() {

		var websocket = Ext.create ('Ext.ux.WebSocket', {
			url: 'ws://'+window.location.hostname+':'+window.location.port+window.location.pathname+"wsmanager",
			listeners: {
				open: function(ws) {
					console.log('Sending ticket to websocket: '+WTStartup.encAuthTicket);
					ws.send(WT.wsMsg("com.sonicle.webtop.core","ticket",{
						userId: WTStartup.userId,
						domainId: WTStartup.domainId,
						encAuthTicket: WTStartup.encAuthTicket
					}));
				} ,
				close: function(ws) {
					console.log('The websocket is closed!');
				} ,
				error: function(ws, error) {
					Ext.Error.raise(error);
				} ,
				message: function(ws, msg) {
					var obj=Ext.JSON.decode(msg,true);
					if (obj && obj.service) {
						var svc=WT.getApp().getService(obj.service);
						if (svc) {
							svc.websocketMessage(obj);
						} else {
							console.log('No service for websocket message: '+msg);
						}
					} else {
						console.log('Invalid websocket message: '+msg);
					}
				}
			}
		});		
		
	}
	
});
