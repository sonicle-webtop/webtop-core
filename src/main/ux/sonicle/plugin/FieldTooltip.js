/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.plugin.FieldTooltip', {
	extend: 'Ext.plugin.Abstract',
	alias: 'plugin.sofieldtooltip',
	
	/**
	 * @cfg {String/Object} tooltip
	 * This configuration option is to be applied to the **field `object`** 
	 * that uses {@link Sonicle.plugin.FieldTooltip this plugin}.
	 * It can be a string to be used as innerHTML (html tags are accepted) or 
	 * QuickTips config object.
	 */
	
	/**
	 * @cfg {String} tooltipType
	 * The type of tooltip to use. Either 'qtip' for QuickTips or 'title' 
	 * for title attribute.
	 */
	tooltipType: 'qtip',
	
	init: function(field) {
		var me = this;
		me.setCmp(field);
		field.on('render', me.onCmpRender, me, {single: true});
	},
	
	destroy: function() {
		var me = this,
				cmp = me.getCmp();
		if(cmp.rendered) {
			me.clearTip();
		}
	},
	
	onCmpRender: function(s) {
		var me = this,
				cmp = me.getCmp();
		if(cmp.tooltip) {
			me.setTooltip(cmp.tooltip, true);
		}
	},
	
	/**
	 * Sets the tooltip for this Button.
	 * @param {String/Object} tooltip This may be:
	 * 
	 *	- **String** : A string to be used as innerHTML (html tags are accepted) to show in a tooltip
	 *	- **Object** : A configuration object for {@link Ext.tip.QuickTipManager#register}.
	 */
	setTooltip: function(tooltip, initial) {
		var me = this,
				cmp = me.getCmp();
		if(cmp.rendered) {
			if(!initial || !tooltip) me.clearTip();
			if(tooltip) {
				if(Ext.quickTipsActive && Ext.isObject(tooltip)) {
					Ext.tip.QuickTipManager.register(Ext.apply({
						target: cmp.inputEl.id
					}, tooltip));
					cmp.tooltip = tooltip;
				} else {
					cmp.inputEl.dom.setAttribute(me.getTipAttr(), tooltip);
				}
			}
		} else {
			cmp.tooltip = tooltip;
		}
	},
	
	/**
	 * @private
	 */
	clearTip: function() {
		var me = this,
				cmp = me.getCmp(),
				el = cmp.inputEl;
		if(Ext.quickTipsActive && Ext.isObject(cmp.tooltip)) {
			Ext.tip.QuickTipManager.unregister(el);
		} else {
			el.dom.removeAttribute(me.getTipAttr());
		}
	},
	
	getTipAttr: function() {
		return this.tooltipType === 'qtip' ? 'data-qtip' : 'title';
	}
});
