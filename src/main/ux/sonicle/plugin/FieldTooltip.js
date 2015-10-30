/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
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
