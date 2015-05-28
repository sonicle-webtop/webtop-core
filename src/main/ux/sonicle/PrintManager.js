/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.PrintManager', {
	singleton: true,
	
	constructor: function(cfg) {
		var me = this;
		
		me.callParent([cfg]);
		
		if(!me.printer) {
			me.printer = Ext.create({
				xtype: 'component',
				id: 'printer',
				style: 'display:none',
				html: {
					tag: 'iframe',
					id: 'printer-iframe',
					width: '100%',
					height: '100%',
					frameborder: '0'
				},
				renderTo: Ext.getBody()
			});
		}
	},
	
	destroy: function() {
		Ext.destroy(this.printer);
		this.printer = undefined;
	},
	
	print: function(html) {
		var me = this,
				iframe = me.printer.getEl().down('#printer-iframe');
		if(me.printing) {
			Ext.log.warn('Printer is busy. Retry later...');
			return;
		}
		
		me.printing = true;
		iframe.dom.contentDocument.open();
		iframe.dom.contentDocument.write(html);
		iframe.dom.contentDocument.close;
		Ext.defer(function() {
			iframe.dom.contentWindow.focus();
			iframe.dom.contentWindow.print();
			me.printing = false;
		}, 500);
	}
});
