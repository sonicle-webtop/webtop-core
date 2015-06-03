
Ext.Loader.setConfig('disableCaching', false);
Ext.application({
    name: 'Sonicle.webtop.core',
	extend: 'Sonicle.webtop.core.Application',
	
	appFolder: WTS.services[0].path,
	paths: Ext.applyIf({
		'Ext.ux': 'resources/extjs/ux',
		'Sonicle': 'resources/sonicle',
		'WT': WTS.services[0].path
		//'WT.ux': WTS.services[0].path + '/ux'
		//'WT.overrides': WTS.services[0].path + '/overrides'
	}, WTS.appPaths || {}),
	autoCreateViewport: false
});
