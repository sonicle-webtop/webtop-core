/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 * Inspired by:
 * 
 * 
 * http://craigsworks.com/projects/forums/showthread.php?tid=1986
 * http://iamceege.github.io/tooltipster/
 * http://jsfiddle.net/shmshd12/kv4f2bf0/
 * https://stackoverflow.com/questions/33593539/how-to-make-a-tooltip-for-scrollbar
 * 
 */
Ext.define('Sonicle.webtop.contacts.ux.ScrollTooltip', {
	//alternateClassName: 'WTA.ux.grid.feature.ScrollTooltip',
	extend: 'Ext.grid.feature.Feature',
	alias: 'feature.wtscrolltooltip',
	requires: [
		
	],
	
	init: function(grid) {
		var me = this,
				view = me.view,
				scrlbl = view.getScrollable();
		me.callParent(arguments);
		if (scrlbl) {
			scrlbl.on('scroll', me.onScroll, me);
		}
		view.on('refresh', me.onViewRefresh);
	},
	
	onViewRefresh: function(view) {
		console.log(view.all);
		
		
	},
	
	onScroll: function(s, x, y, eo) {
		console.log('scroll x:'+x+' y:'+y);
		
		var view = this.view,
				buffRender = view.bufferedRenderer,
				rows = view.all,
				storeCount = buffRender.store.getCount(),
				viewSize = buffRender.viewSize
				;
		
		console.log(Math.max(0, Math.min(rows.startIndex, storeCount - viewSize)));
	}
});
