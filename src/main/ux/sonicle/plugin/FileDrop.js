/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.plugin.FileDrop', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.filedrop',
	
	init: function(cmp) {
		var me = this;
		me.setCmp(cmp);
		cmp.on({
			element: 'el',
			drop: me.onDrop,
			dragenter: me.onDragEnter,
			dragleave: me.onDragLeave,
			scope: me
		});
	},
	
	destroy: function() {
		var me = this,
			el = this.getCmp().getEl();
		if(el) {
			el.un({
				drop: me.onDrop,
				dragenter: me.onDragEnter,
				dragleave: me.onDragLeave
			});
		}
	},
	
	first: false,
	second: false,
	
	reset: function() {
		this.first = false;
		this.second = false;
	},
	
	onDrop: function(e) {
		this.reset();
	},
	
	onDragEnter: function(e) {
		var me = this, el;
		e.stopEvent();
		if(me.first) {
			me.second = true;
		} else {
			me.first = true;
			el = this.getCmp().getEl();
			if(el) el.addCls('');
		}
		//800500200
	},
	
	onDragLeave: function(e) {
		var me = this, el;
		e.stopEvent();
		if(me.second) {
			me.second = false;
		} else if (me.first) {
			me.first = false;
		}
		if(!me.first && !me.second) {
			el = this.getCmp().getEl();
			if(el) el.removeCls('');
			//this.getCmp().unmask();
		}
	}
});
