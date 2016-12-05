
Ext.Loader.setConfig('disableCaching', false);
Ext.application({
    name: 'Sonicle.webtop.core',
	extend: 'Sonicle.webtop.core.app.AppPrivate',
	
	appProperty: 'instance',
	//appFolder: WTS.appPaths['Sonicle.webtop.core'],
	/*
	paths: Ext.applyIf({
		'Ext.ux': 'resources/extjs/ux'
		//'Sonicle': 'resources/client/extjs/packages/sonicle-extensions/src',
		//'WT': WTS.appPaths['Sonicle.webtop.core']
		//'WT.ux': WTS.services[0].path + '/ux'
		//'WT.overrides': WTS.services[0].path + '/overrides'
	}, WTS.appPaths || {}),
	*/
	autoCreateViewport: false
});
