/* 
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.app.launcher.MoreLinksButton', {
	alternateClassName: 'WTA.ux.app.launcher.MoreLinksButton',
	extend: 'WTA.ux.app.launcher.NavButton',
	requires: [
		'Sonicle.URLMgr'
	],
	mixins: [
		'Ext.util.StoreHolder',
		'Sonicle.mixin.DDPickerHolder'
	],
	
	componentCls: 'wt-launcher-morelinksbutton',
	baseIconName: 'navMoreLinks',
	arrowVisible: false,
	
	ddPickerMatchOwnerWidth: true,
	ddPickerAlign: 'bl-br?',
	
	constructor: function(cfg) {
		cfg.serviceId = WT.ID;
		cfg.text = 'more links';
		cfg.store = {
			autoLoad: true,
			fields: [
				{name: 'icon', type: 'string'},
				{name: 'text', type: 'string'},
				{name: 'href', type: 'string'}
			]
		};
		this.callParent([cfg]);
		this.enableToggle = true;
	},
	
	initComponent: function() {
		var me = this,
			store = me.store;
		me.bindStore(store || 'ext-empty-store', true);
		me.callParent(arguments);
	},
	
	onDestroy: function() {
		var me = this;
		me.bindStore(null);
		me.callParent();
	},
	
	getOwnerCmp: function() {
		return this;
	},
	
	getOwnerBodyEl: function() {
		return this.btnEl;
	},
	
	getOwnerAlignEl: function() {
		return this.btnEl;
	},
	
	createDDPicker: function(id) {
		var me = this,
			winCfg = {
				xtype: 'window',
				closeAction: 'hide',
				referenceHolder: true,
				layout: 'fit',
				header: false,
				resizable: false,
				items: {
					xtype: 'soboundlist',
					cls: me.componentCls + '-list',
					//cls: 'wt-morelinksbutton-list',
					reference: 'list',
					disableFocusSaving: true,
					store: me.store,
					displayField: 'text',
					iconField: 'icon',
					iconMode: 'src'
				},
				minWidth: 300
			},
			picker,
			list;
		
		if (me.ddPickerWidth) winCfg.width = me.ddPickerWidth;
		picker = Ext.create(winCfg);
		list = picker.lookupReference('list');
		
		list.on({
			scope: me,
			itemclick: me.onListPickerItemClick
		});
		/*
		picker.on({
			close: 'onSearchPickerCancel',
			scope: me
		});
		*/
		
		return picker;
	},
	
	maybeShowMenu: function() {
		var me = this;
		if (me.isDDPickerExpanded('list')) {
			me.collapseDDPicker();
		} else {
			me.expandDDPicker('list');
		}
	},
	
	onListPickerItemClick: function(s, rec, itm, idx) {
		s.setSelection(null);
		Sonicle.URLMgr.open(rec.get('href'), true);
		this.collapseDDPicker();
	},
	
	addLink: function(link) {
		var store = this.store;
		store.add({
			icon: link.icon,
			text: link.text,
			href: link.href
		});
	},
	
	onExpandDDPicker: function() {
		this.setPressed(true);
	},
	
	onCollapseDDPicker: function() {
		this.setPressed(false);
	}
});
