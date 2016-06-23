/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.field.Link', {
	extend: 'Ext.form.field.Display',
	alias: ['widget.solinkfield'],
	
	/**
	 * @cfg {String} displayText
	 * Text to display as link.
	 */
	displayText: null,
	
	/**
	 * @cfg {Boolean} disableNavigation
	 * True to disable link navigation on click.
	 */
	disableNavigation: false,
	
	renderer: function(v,fld) {
		var href = fld.disableNavigation ? 'javascript:Ext.EmptyFn' : v,
				txt = Ext.isEmpty(fld.displayText) ? v : fld.displayText;
		return '<a href="'+href+'" target="_blank">'+txt+'</a>';
	}
});
