
Ext.define('Sonicle.webtop.core.Application', {
	extend: 'Ext.app.Application',
	alternateClassName: 'WT.App',
	requires: [
		'Sonicle.webtop.core.WT'
	],
	
	init: function() {
		console.log('application:init');
	},
	
	launch: function () {
		console.log('application:launch');
		//Sonicle.webtop.core.js.WT.aalleerrtt();
		//WT.aalleerrtt();
	}
});
