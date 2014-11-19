
Ext.application({
    name: 'Sonicle.webtop.core',
	extend: 'Sonicle.webtop.core.Application',
	
    appFolder: 'resources/com.sonicle.webtop.core',
	paths: Ext.applyIf({
		'Ext.ux': 'resources/extjs/ux',
		'WT': 'resources/com.sonicle.webtop.core'
		//'WT.ux': 'resources/com.sonicle.webtop.core/ux'
		//'WT.overrides': 'resources/com.sonicle.webtop.core/overrides'
	}, WTS.appPaths || {}),
	autoCreateViewport: false
});
