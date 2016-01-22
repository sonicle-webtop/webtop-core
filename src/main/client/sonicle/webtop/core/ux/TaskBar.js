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
Ext.define('Sonicle.webtop.core.ux.TaskBar', {
	alternateClassName: 'WT.ux.TaskBar',
	extend: 'Ext.toolbar.Toolbar',
	alias: ['widget.wttaskbar'],
	requires: [
		'WT.ux.TaskBarButton'
	],
	
	layout: {
		overflowHandler: 'Scroller'
	},
	
	/**
	 * @property {String} active
	 * Currently active service
	 */
	active: null,
	
	itemIds: Ext.create('Ext.util.HashMap'),
	
	destroy: function() {
		var me = this;
		me.callParent(arguments);
		me.itemIds.destroy();
		me.itemIds = null;
	},
	
	afterLayout: function() {
		var me = this;
		me.callParent(arguments);
		me.el.on('contextmenu', me.onButtonContextMenu, me);
	},
	
	getButton: function(itemId) {
		return this.getComponent(itemId);
	},
	
	activateService: function(svc) {
		var me = this,
				id = svc.ID;
		// If already active...exits
		if(me.active === id) return false;
		me.updateButtonVisibility(me.active, false);
		me.updateButtonVisibility(id, true);
		me.active = id;
	},
	
	addButton: function(win) {
		var me = this;
		me.add(me.createButton(win));
	},
	
	removeButton: function(win) {
		var me = this,
				cmp = me.getButton(win.getId());
		if(cmp) me.remove(cmp);
	},
	
	updateButtonTitle: function(win) {
		var me = this,
				cmp = me.getButton(win.getId());
		if(cmp) {
			cmp.setText(Ext.util.Format.ellipsis(win.getTitle(), 20));
			cmp.setTooltip(win.getTitle());
		}
	},
	
	activateButton: function(win) {
		var me = this,
				cmp = me.getButton(win.getId());
		if(cmp) cmp.toggle(true);
	},
	
	
	
	
	
	
	
	
	
	
	createButton: function(win) {
		var me = this,
				dockCfg = win.getDockableConfig();
		return Ext.create({
			xtype: 'wttaskbarbutton',
			itemId: win.getId(),
			text: Ext.util.Format.ellipsis(win.getTitle(), 20),
			tooltip: win.getTitle(),
			iconCls: win.getIconCls(),
			listeners: {
				click: me.onButtonClick,
				contextmenu: me.onButtonContextMenu,
				scope: me
			},
			constrainToService: dockCfg.constrainToService
		});
	},
	
	onButtonClick: function(s, e) {
		if(s.isComponent && s.isXType('wttaskbarbutton')) {
			this.fireEvent('buttonclick', this, s, e);
		}
	},
	
	onButtonContextMenu: function(s, e) {
		if(s.isComponent && s.isXType('wttaskbarbutton')) {
			this.fireEvent('buttoncontextmenu', this, s, e);
		}
	},
	
	updateButtonVisibility: function(sid, visible) {
		var me = this,
				ids = me.getCachedIds(sid),
				cmp;
		
		Ext.iterate(ids, function(id) {
			cmp = me.getTaskButton(id);
			if(cmp && cmp.constrainToService) cmp.setHidden(!visible); 
		});
	},
	
	cacheId: function(sid, itemId) {
		var me = this;
		if(me.itemIds.containsKey(sid)) {
			me.itemIds.get(sid).push(itemId);
		} else {
			me.itemIds.add(sid, [itemId]);
		}
	},
	
	uncacheId: function(sid, itemId) {
		var me = this;
		if(me.itemIds.containsKey(sid)) {
			me.itemIds.get(sid).remove(itemId);
		}
	},
	
	getCachedIds: function(sid) {
		var me = this;
		if(me.itemIds.containsKey(sid)) {
			return me.itemIds.get(sid);
		}
	}
});
