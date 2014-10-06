
Ext.define('com.sonicle.webtop.core.js.Application', {
	extend: 'Ext.app.Application',
	requires: [
		'com.sonicle.webtop.core.js.WT'
	],
	
	launch: function () {
		Ext.ns('WT');
		WT = com.sonicle.webtop.core.js.WT;
		console.log('application started');
		com.sonicle.webtop.core.js.WT.aalleerrtt();
		WT.aalleerrtt();
	}
});
