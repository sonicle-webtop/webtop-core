
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ServiceDescriptor',
		'Ext.ux.WebSocketManager',
		'Ext.ux.WebSocket'
	].concat(WTStartup.appRequires || []),
	views: [
		'Sonicle.webtop.core.view.Viewport'
	],
	refs: {
		viewport: 'viewport'
	},
	
	services: null,
	
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
		
		// Inits loaded services and activate the default one
		me.services.each(function(desc,i) {
			desc.initService();
			if(i === 0) me.activateService(desc.getId());
		});
	},
	
	/**
	 * Returns a service instance.
	 * @param {String} svc The service id.
	 * @returns {WT.sdk.Service} The instance or null if the instance
	 * was not found. 
	 */
	getService: function(id) {
		var desc = this.getDescriptor(id);
		return (desc) ? desc.getInstance() : null;
	},
	
	getDescriptor: function(id) {
		return this.services.get(id);
	},
	
	activateService: function(id) {
		var svc = this.getService(id);
		if(!svc) return;
		var wpc = this.getViewport().getController();
		
		if(!wpc.hasServiceCmp(id)) {
			var tb, tool, main;
			if(Ext.isFunction(svc.getToolbar)) {
				tb = svc.getToolbar.call(svc);
			}
			if(Ext.isFunction(svc.getToolComponent)) {
				tool = svc.getToolComponent.call(svc);
			}
			if(Ext.isFunction(svc.getMainComponent)) {
				main = svc.getMainComponent.call(svc);
			}
			wpc.addServiceCmp(id, tb, tool, main);
		}
		wpc.showService(id);
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
