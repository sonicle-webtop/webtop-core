Ext.define('Sonicle.webtop.core.WT', {
	singleton: true,
	alternateClassName: 'WT',
	
	getApp: function() {
		return Sonicle.webtop.core.getApplication();
	},
	
	preNs: function(ns,cn) {
		if(arguments.length == 1) {
			return 'Sonicle.webtop.core.'+cn;
		} else {
			return ns+'.'+cn;
		}
	}
});
