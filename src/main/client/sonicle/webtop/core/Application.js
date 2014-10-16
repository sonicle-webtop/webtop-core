
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ServiceDescriptor'
	],
	refs: {
		viewport: 'viewport'
	},
	
	services: null,
	
	constructor: function(cfg) {
		var me = this;
		me.services = Ext.create('Ext.util.Collection');
		me.callParent(arguments);
	},
	
	init: function() {
		var me = this;
		WT.Log.debug('application:init');
		Ext.setGlyphFontFamily('FontAwesome');
		
		// Load services defined in startup
		Ext.each(WTStartup.services, function(svc) {
			me.addService(svc);
		});
	},
	
	launch: function () {
		var me = this;
		WT.Log.debug('application:launch');
		
		// Inits loaded services and activate the default one
		me.services.each(function(desc,i) {
			desc.initService();
			if(i == 0) me.activateService(desc.getId());
		});
	},
	
	/*
	 * @private
	 */
	addService: function(cfg) {
		var desc = Ext.create('WT.ServiceDescriptor', {
			id: cfg.id,
			name: cfg.name,
			description: cfg.description,
			version: cfg.version,
			build: cfg.build,
			company: cfg.company,
			//iconCls: null,
			className: cfg.className
		});
		var ns = desc.getNs();
		var path = 'resources/'+desc.getPath();
		Ext.Loader.setPath(ns, path);
		WT.Log.debug('Added loader path [{0}, {1}]', ns, path);
		this.services.add(desc);
	},
	
	getService: function(svcId) {
		var desc = this.getDescriptor(svcId);
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
	}
});
