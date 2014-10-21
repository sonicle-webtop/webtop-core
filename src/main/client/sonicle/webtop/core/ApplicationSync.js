
Ext.define('Sonicle.webtop.core.ApplicationSync', {
	extend: 'Ext.app.Application',
	requires: [
		'Sonicle.webtop.core.WT',
		'Sonicle.webtop.core.Log',
		'Sonicle.webtop.core.ServiceDescriptor'
	],
	views: [
		'Sonicle.webtop.core.view.Viewport'
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
		
		Ext.each(WTStartup.services, function(cfg) {
			me.loadService(cfg);
		}, me);
	},
	
	loadService: function(cfg) {
		var desc = null, name = null;
		try {
			desc = Ext.create('WT.ServiceDescriptor', {
				id: cfg.id,
				name: cfg.name,
				description: cfg.description,
				version: cfg.version,
				build: cfg.build,
				company: cfg.company,
				//iconCls: null,
				className: cfg.className
			});

			// Register service paths into Ext classloader
			Ext.Loader.setPath(desc.getNs(), desc.getBaseUrl());
			
			/*
			name = desc.getClassName();
			Ext.syncRequire(name);
			if(!Ext.ClassManager.get(name)) throw new Error('Unable to load '+name);
			console.log(Ext.ClassManager.get(name));

			name = desc.getNs()+'.Locale_it_IT';
			Ext.syncRequire(name);
			console.log(Ext.ClassManager.get(name));
			*/
			
			var urls = [];
			urls.push(Ext.Loader.getPath(desc.getClassName()));
			urls.push(Ext.Loader.getPath(desc.getNs()+'.Locale_it_IT'));
			Ext.Loader.loadScriptsSync(urls);

			console.log('service loaded '+desc.getId());
			me.services.add(desc);

		} catch(e) {
			console.log(e);
		}
	},
	
	launch: function () {
		var me = this;
		console.log('launch');
		
		me.viewport = me.getView('Sonicle.webtop.core.view.Viewport').create();
		
		// Inits loaded services and activate the default one
		me.services.each(function(desc,i) {
			desc.initService();
			if(i == 0) me.activateService(desc.getId());
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
	}
});
