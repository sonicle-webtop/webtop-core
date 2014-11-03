
Ext.application({
    name: 'Sonicle.webtop.core',
	extend: 'Sonicle.webtop.core.Application',
	
    appFolder: 'resources/com.sonicle.webtop.core',
	paths: Ext.applyIf({
		'Ext.ux': 'resources/extjs/ux',
		'WT': 'resources/com.sonicle.webtop.core'
	}, WTStartup.appPaths || {}),
	autoCreateViewport: false
});
