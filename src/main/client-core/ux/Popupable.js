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
Ext.define('Sonicle.webtop.core.ux.Popupable', {
	extend: 'Ext.Mixin',
	mixinConfig: {
		id: 'popupable'
	},
	
	/**
	 * @cfg {String} popupAlign
	 * The {@link Ext.util.Positionable#alignTo alignment position} with which to align the popup. Defaults to "tl-bl?"
	 */
	popupAlign: 'tl-bl?',
	
	/**
	 * @cfg {Number[]} popupOffset
	 * An offset [x,y] to use in addition to the {@link #popupAlign} when positioning the popup.
	 * Defaults to undefined.
	 */
	
	createPopup: Ext.emptyFn,
	onPopupExpand: Ext.emptyFn,
	onPopupCollapse: Ext.emptyFn,
	
	
	getPopup: function() {
		var me = this, pup = me.popup;
		if (!pup) {
			me.creatingPopup = true;
			me.popup = pup = me.createPopup();
			pup.ownerCmp = me;
			delete me.creatingPopup;
		}
		return pup;
	},
	
	expandPopup: function() {
		var me = this,
				bodyEl, pup, doc;
		
		if (me.rendered && !me.isExpanded && !me.isDestroyed) {
			bodyEl = me.bodyEl;
			pup = me.getPopup();
			doc = Ext.getDoc();
			pup.setMaxHeight(pup.initialConfig.maxHeight);
			
			pup.show();
			me.isExpanded = true;
			me.doAlignPopup();
			
			// Collapse on touch outside this component tree.
			// Because touch platforms do not focus document.body on touch
			// so no focusleave would occur to trigger a collapse.
			me.touchListeners = doc.on({
				// Do not translate on non-touch platforms.
				// mousedown will blur the field.
				translate: false,
				touchstart: me.onTouchStart,
				scope: me,
				delegated: false,
				destroyable: true
			});
			
			// Scrolling of anything which causes this field to move should collapse
			me.scrollListeners = Ext.on({
				scroll: me.onGlobalScroll,
				scope: me,
				destroyable: true
			});
			
			me.fireEvent('popupexpand', me);
			me.onPopupExpand();
		}
	},
	
	alignPopup: function() {
		var me = this, pup;
		if (!me.isDestroyed) {
			pup = me.getPopup();
			if (pup.isVisible() && pup.isFloating()) {
				me.doAlignPopup();
			}
		}
	},
	
	doAlignPopup: function() {
		var me = this;
		me.popup.el.alignTo(me, me.popupAlign, me.popupOffset);
	},
	
	collapsePopup: function() {
		var me = this, pup = me.popup;
		if (me.isExpanded && !me.isDestroyed && !me.destroying) {
			pup.hide();
			me.isExpanded = false;
			
			// remove event listeners
			me.touchListeners.destroy();
			me.scrollListeners.destroy();
			
			me.fireEvent('popupcollapse', me);
			me.onPopupCollapse();
		}
	},
	
		
	
	onFocusLeave: function(e) {
		this.callParent([e]);
		this.collapsePopup();
	},
	
	beforeDestroy : function() {
		var me = this;
		
		me.callParent();
		if (me.popup) {
			me.popup = null;
		}
	},
	
	privates: {
		onTouchStart: function(e) {
			var me = this;
			// If what was mousedowned on is outside of this Field, and is not focusable, then collapse.
			// If it is focusable, this Field will blur and collapse anyway.
			if (!me.isDestroyed && !e.within(me.bodyEl, false, true) && !me.owns(e.target) && !Ext.fly(e.target).isFocusable()) {
				me.collapsePopup();
			}
		},
		
		onGlobalScroll: function(scroller) {
			var me = this;
			// Collapse if the scroll is anywhere but inside the picker
			if (me.popup && !me.popup.owns(scroller.getElement())) {
				me.collapsePopup();
			}
		}
	}
});
