
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Ext.ux.WebSocketManager',
		'Ext.ux.WebSocket',
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ServiceDescriptor'
	].concat(WTS.appRequires || []),
	views: [
		'Sonicle.webtop.core.view.Viewport'
	],
	refs: {
		viewport: 'viewport'
	},
	
	services: null,
	currentService: null,
	
	kaTask: null,
	seTask: null,
	
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
		var desc = null;
		Ext.each(WTS.services, function(obj) {
			desc = Ext.create('WT.ServiceDescriptor', {
				id: obj.id,
				xid: obj.xid,
				ns: obj.ns,
				path: obj.path,
				version: obj.version,
				build: obj.build,
				className: obj.className,
				optionsClassName: obj.optionsClassName,
				name: obj.name,
				description: obj.description,
				company: obj.company
			});
			WT.loadCss(desc.getPath()+'/laf/'+WTS.laf+'/service.css');
			WT.loadCss(desc.getPath()+'/laf/'+WTS.laf+'/service-'+WTS.theme+'.css');
			me.services.add(desc);
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
		var vpc = me.getViewport().getController();
		vpc.addServiceCmp(svc);
		me.currentService = id;
		if(vpc.activateService(svc)) svc.fireEvent('activate');
	},
	
	initWebSocket: function() {

		var websocket = Ext.create ('Ext.ux.WebSocket', {
			url: 'ws://'+window.location.hostname+':'+window.location.port+window.location.pathname+"xwsmanager",
			autoReconnect: true ,
			autoReconnectInterval: 10000,
			listeners: {
				open: function(ws) {
					var me=WT.getApp();
					console.log('Sending ticket to websocket: '+WTS.encAuthTicket);
					ws.send(WT.wsMsg("com.sonicle.webtop.core","ticket",{
						userId: WTS.userId,
						domainId: WTS.domainId,
						encAuthTicket: WTS.encAuthTicket
					}));
					//websocket is working
					//kill any server events task and run http session keep alive
					me.killServerEvents();
					me.runKeepAliveTask();
				} ,
				close: function(ws) {
					console.log('The websocket is closed!');
				} ,
				error: function(ws, error) {
					var me=WT.getApp();
					//websocket is not working
					//kill any keep alive task and run http server events instead
					me.killKeepAliveTask();
					me.runServerEvents();
				} ,
				message: function(ws, msg) {
					WT.getApp().handleWSMessage(msg);
				}
			}
		});		
		
	},
	
	handleWSMessage: function(msg) {
		var obj=Ext.JSON.decode(msg,true);
		if (obj && obj.service) {
			var svc=this.getService(obj.service);
			if (svc) {
				svc.websocketMessage(obj);
			} else {
				console.log('No service for websocket message: '+msg);
			}
		} else {
			console.log('Invalid websocket message: '+msg);
		}
	},
	
	runKeepAliveTask: function() {
		if (!this.kaTask) {
			var task = { 
				run: function() {
					Ext.Ajax.request({
						url: 'session-keep-alive',
						method: 'GET'
					});
				},
				interval: 60000 
			};
			this.kaTask=Ext.TaskManager.start(task);
		} else {
			console.log("keep alive task already running");
		}
	},
	
	killKeepAliveTask: function() {
		if (this.kaTask) {
			console.log("Killing keep alive task");
			this.kaTask.destroy();
			this.kaTask=null;
		}
	},
	
	runServerEvents: function() {
		if (!this.seTask) {
			var task = { 
				run: function() {
					WT.ajaxReq(WT.ID,"ServerEvents", {
						callback: function(success, o) {
							if(success) {
								if (o.data) {
									this.handleWSMessage(o.data);
								} else {
									console.log("no server events in queue");
								}
							}
						}
					});
				},
				scope: this,
				interval: 30000 
			};
			this.seTask=Ext.TaskManager.start(task);
		} else {
			console.log("server events task already running");
		}
	},
	
	killServerEventsTask: function() {
		if (this.seTask) {
			console.log("Killing server events task");
			this.seTask.destroy();
			this.seTask=null;
		}
	}
	
	

});
