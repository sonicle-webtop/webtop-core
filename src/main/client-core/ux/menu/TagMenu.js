/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.menu.TagMenu', {
	alternateClassName: 'WTA.ux.menu.TagMenu',
	extend: 'Sonicle.menu.StoreMenu',
	alias: 'widget.wttagmenu',
	uses: [
		'Sonicle.menu.TagItem'
	],
	
	/**
	 * @cfg {Function} restoreSelectedTags
	 * A function that must return an array of tag IDs to select.
	 * 
	 * @cfg {Object} restoreSelectedTags.menuData
	 */
	restoreSelectedTags: Ext.emptyFn,
	
	/**
	 * @event tagclick
	 * Fires when a tag item is clicked.
	 * @param {WTA.ux.menu.TagMenu} this
	 * @param {String} id The ID of clicked tag item.
	 * @param {Boolean} checked The checked status of tag item.
	 * @param {Ext.Component} item The menu item that was clicked.
	 * @param {Ext.event.Event} e The underlying {@link Ext.event.Event}.
	 */
	
	useItemIdPrefix: true,
	textField: 'name',
	tagField: 'id',
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.store) cfg.store = WT.getTagsStore();
		if (!cfg.itemCfgCreator) {
			cfg.itemCfgCreator = function(rec) {
				var cfg = {
						xclass: 'Sonicle.menu.TagItem',
						color: rec.get('color'),
						hideOnClick: true
					};
				if (rec.get('personal')) {
					cfg.text = rec.get('name') + '<span class="wt-source">&nbsp;(' + WT.res('tags.gp.personal.true') + ')</span>';
				}
				return cfg;
			};
		}
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.on('beforeshow', me.onBeforeShow, me);
		me.on('click', me.onItemClick, me);
	},
	
	destroy: function() {
		var me = this;
		me.un('beforeshow', me.onBeforeShow, me);
		me.un('click', me.onItemClick, me);
		me.callParent();
	},
	
	privates: {
		onBeforeShow: function(s) {
			s.setCheckedItems(Ext.callback(s.restoreSelectedTags, s, [s.parentMenu ? s.parentMenu.menuData : null]) || []);
		},

		onItemClick: function(s, itm, e) {
			if (itm.tag) this.fireEvent('tagclick', s, itm.tag, itm.checked, itm, e);
		}
	}
});
