/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 * http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
 */
Ext.define('Sonicle.UUID', {
	singleton: true,
	
	lut: null,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		// Defines lookup table
		me.lut = [];
		for(var i=0; i<256; i++) {
			me.lut[i] = (i<16?'0':'')+(i).toString(16);
		}
	},
	
	generate: function() {
		var me = this,
				t = me.lut,
				d0 = Math.random()*0xffffffff|0,
				d1 = Math.random()*0xffffffff|0,
				d2 = Math.random()*0xffffffff|0,
				d3 = Math.random()*0xffffffff|0;
		return t[d0 & 0xff] + t[d0 >> 8 & 0xff] + t[d0 >> 16 & 0xff] + t[d0 >> 24 & 0xff] + '-' +
				t[d1 & 0xff] + t[d1 >> 8 & 0xff] + '-' + t[d1 >> 16 & 0x0f | 0x40] + t[d1 >> 24 & 0xff] + '-' +
				t[d2 & 0x3f | 0x80] + t[d2 >> 8 & 0xff] + '-' + t[d2 >> 16 & 0xff] + t[d2 >> 24 & 0xff] +
				t[d3 & 0xff] + t[d3 >> 8 & 0xff] + t[d3 >> 16 & 0xff] + t[d3 >> 24 & 0xff];
	}
});

