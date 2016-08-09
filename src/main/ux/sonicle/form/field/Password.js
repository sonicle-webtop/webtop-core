/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.field.Password', {
	extend:'Ext.form.field.Text',
	alias: 'widget.sopasswordfield',
	
	config: {
		eyeTriggerCls: 'so-passwordfield-trigger-eye',
		eyeTriggerClsOn: 'fa fa-eye-slash',
		eyeTriggerClsOff: 'fa fa-eye'
	},
	
	inputType: 'password',
	eye: false,
	
	constructor: function(cfg) {
		var me = this, 
				icfg = Ext.apply({}, cfg, me.getInitialConfig()), 
				triggers = {};
		
		triggers = Ext.apply(triggers, {
			eye: {
				weight: -1,
				cls: icfg.eyeTriggerCls + ' ' + icfg.eyeTriggerClsOff,
				handler: me.onEyeClick
			}
		});
		Ext.apply(cfg, {triggers: triggers});
		me.callParent([cfg]);
	},
	
	onEyeClick: function(s) {
		var me = this,
				onCls = me.eyeTriggerClsOn,
				offCls = me.eyeTriggerClsOff,
				tri = me.triggers['eye'],
				tel = tri ? tri.getEl() : null;
		me.eye = !me.eye;
		if(tri) {
			tel.removeCls(onCls);
			tel.removeCls(offCls);
			tel.addCls(me.eye ? onCls : offCls);
		}
		if(me.inputEl) me.inputEl.set({type: me.eye ? 'text' : 'password'});
	}
});
