/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.Label', {
	extend: 'Ext.form.Label',
	alias: ['widget.solabel'],
	
	titleCls: Ext.baseCSSPrefix + 'window-header-title-default',
	titleStyle: null,
	hintCls: null,
	hintStyle: 'font-size:9px;',
	
	config: {
		appearance: 'default'
	},
	
	initComponent: function() {
		var me = this,
				appe = me.buildAppearance(me.getAppearance());
		
		if(appe[0]) me.cls = appe[0];
		if(appe[1]) me.style = appe[1];
		me.callParent(arguments);
	},
	
	buildAppearance: function(appe) {
		var me = this;
		if(appe === 'title') {
			return [me.titleCls, me.titleCls];
		} else if(appe === 'hint') {
			return [me.hintCls, me.hintCls];
		} else {
			return [null, null];
		}
	}
});
