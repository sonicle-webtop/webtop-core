/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.PageMgr', {
	singleton: true,
	mixins: [
		'Ext.mixin.Observable'
	],
	
	/**
	 * @readonly
	 * @property {Boolean} visibilityApi
	 */
	visibilityApi: false,
	
	/**
	 * @readonly
	 * @property {String} hiddenProp
	 */
	hiddenProp: null,
	
	/**
	 * @readonly
	 * @property {String} visibilityEvent
	 */
	visibilityEvent: null,
	
	/**
	 * @event visibilitychange
	 * Fires when page visibility changes.
	 */
	
	constructor: function(cfg) {
		var me = this, api;
		me.initConfig(cfg);
		me.mixins.observable.constructor.call(me, cfg);
		me.callParent([cfg]);
		
		api = me.checkVisibilityApi();
		if(api) {
			visibilityApi = true;
			me.hiddenProp = api[0];
			me.visibilityEvent = api[1];
			document.addEventListener(me.visibilityEvent, me.onVisibilityChange.bind(me));
		}
	},
	
	onVisibilityChange: function() {
		this.fireEvent('visibilitychange');
	},
	
	isHidden: function() {
		var me = this;
		return !me.visibilityApi ? false : document[me.hiddenProp];
	},
	
	checkVisibilityApi: function() {
		try {
			if('hidden' in document) {
				return ['hidden','visibilitychange'];
			} else {
				var prefixes = ['webkit','moz','ms','o'];
				for(var i=0; i<prefixes.length; i++) {
					if((document[prefixes[i]+'Hidden']) in document) {
						return [prefixes[i]+'Hidden', prefixes[i]+'visibilitychange'];
					}
				}
				return false;
			}
		} catch (e) {
			return false;
		}
	}
});
