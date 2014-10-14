
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ServiceDescriptor'
	],
	
	services: null,
	
	constructor: function(cfg) {
		var me = this;
		me.services = Ext.create('Ext.util.Collection');
		me.callParent(arguments);
	},
	
	init: function() {
		var me = this;
		WT.Log.debug('application:init');
		
		// Load services defined in startup
		Ext.each(WTStartup.services, function(svc) {
			me.addService(svc);
		});
	},
	
	launch: function () {
		var me = this;
		WT.Log.debug('application:launch');
		
		me.services.each(function(desc) {
			desc.initService();
		});
	},
	
	addService: function(cfg) {
		var desc = Ext.create('WT.ServiceDescriptor', {
			id: cfg.id,
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
	}
});
