/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.FakeInput', {
	extend: 'Ext.Component',
	alias: 'widget.sofakeinput',
	
	/**
	 * @cfg {String} inputType
	 * The type attribute for input fields.
	 * In this case are only useful: 'text' or 'password' values.
	 */
	type: 'text',
	
	initComponent: function() {
		var me = this;
		
		me.autoEl = {
			tag: 'input',
			type: me.type,
			name: me.name || me.getId()
		};
		me.callParent(arguments);
		me.on('afterrender', function(s) {
			Ext.defer(function() {
				s.getEl().setStyle('display', 'none');
			}, 10);
		}, me, {single: true});
	}
});
