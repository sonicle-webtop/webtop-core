
Ext.application({
    name: 'Sonicle.webtop.core',
	extend: 'Sonicle.webtop.core.Application',
	
    appFolder: 'resources/com.sonicle.webtop.core',
	paths: {
		'Ext.ux': 'resources/extjs/ux',
		'WT': 'resources/com.sonicle.webtop.core'
	},
	autoCreateViewport: false
});
