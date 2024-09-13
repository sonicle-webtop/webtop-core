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
Ext.define('Sonicle.webtop.core.ux.Window', {
	alternateClassName: 'WTA.ux.Window',
	extend: 'Ext.window.Window',
	alias: ['widget.wtwindow'],
	
	/**
	 * @cfg {Object/String} maximizeInsets
	 * An object or a string (in TRBL order) specifying insets from the configured
	 * maximizing parent region.
	 */
	
	/**
	 * @event beforeminimize
	 * Fires before minimizing the window, usually because the user has clicked the minimize tool.
	 * Return false from any listener to stop the minimizing process being completed.
	 * @param {Ext.window.Window} this
	 */
	
	isMinimizable: function() {
		return this.minimizable && !this.isHidden();
	},
	
	isMaximizable: function() {
		return this.maximizable && !this.maximized && !this.isHidden();
	},
	
	isRestorable: function() {
		return this.isHidden() || this.maximized;
	},
	
	minimize: function() {
		var me = this;
		if (me.isMinimizable()) {
			if (me.fireEvent('beforeminimize', me) !== false) {
				me.hide();
				return me.callParent();
			}
		}
		return me;
	},
	
	maximize: function(animate, initial) {
		var me = this,
			maximizeInsets = me.maximizeInsets,
			maximizeDone = !me.maximized && !me.maximizing,
			animateDone = animate || !!me.animateTarget,
			ret = me.callParent(arguments);
		
		if (maximizeDone && !animateDone && maximizeInsets) {
			me.enforceMaximizeInsets(maximizeInsets);
		}
		return ret;
	},
	
	onShow: function() {
		// Called when window size is restored!
		var me = this,
			maximizeInsets = me.maximizeInsets,
			wasMaximized = me.maximized;
		me.callParent(arguments);
		if (wasMaximized && maximizeInsets) {
			me.enforceMaximizeInsets(maximizeInsets);
		}
	},
	
	fitContainer: function(animate) {
		// Called when browser window is resized!
		var me = this,
			maximizeInsets = me.maximizeInsets;
		if (me.maximized && maximizeInsets) {
			me.enforceMaximizeInsets(maximizeInsets, animate);
		} else {
			me.callParent(arguments);
		}
	},
	
	privates: {
		enforceMaximizeInsets: function(insets, animate) {
			insets = Ext.isObject(insets) ? insets : Ext.Element.parseBox(insets);
			var me = this,
				region = me.getConstrainRegion();
			
			region.adjust(insets.top, insets.right, insets.bottom, insets.left);
			me.setBox(region, animate);
			//me.restorePos = [region.x, region.y];
			//me.restoreSize = {width: region.width, height: region.height};
		}
	}
});
