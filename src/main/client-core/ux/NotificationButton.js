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
Ext.define('Sonicle.webtop.core.ux.NotificationButton', {
	alternateClassName: 'WTA.ux.NotificationButton',
	extend: 'Ext.button.Button',
	alias: ['widget.wtnotificationbutton'],
	uses: [
		'WTA.model.Notification'
	],
	mixins: [
		'Ext.util.StoreHolder',
		'Sonicle.mixin.BadgeText'
	],
	
	arrowVisible: false,
	iconCls: 'wt-icon-notification-xs',
	
	initComponent: function() {
		var me = this;
		me.bindStore(me.store || 'ext-empty-store', true, true);
		
		Ext.apply(me, {
			badgeAlignOffset: -2,
			menu: {
				xtype: 'menu',
				plain: true,
				layout: 'fit',
				forceLayout: true,
				items: [{
					xtype: 'grid',
					store: me.store,
					selModel: {
						type: 'rowmodel'
					},
					viewConfig: {
						deferEmptyText: false,
						emptyText: 'Hai letto tutte le notifiche'
					},
					hideHeaders: true,
					columns: [{
						xtype: 'soiconcolumn',
						dataIndex: 'iconCls',
						iconSize: 32,
						width: 40
					}, {
						dataIndex: 'title',
						flex: 1
					}, {
						xtype: 'actioncolumn',
						items: [{
							iconCls: 'wt-icon-notification-remove-xs',
							tooltip: 'Rimuovi',
							handler: function(s, rindx) {
								me.removeGridRecord(s, s.getStore().getAt(rindx));
							}
						}],
						width: 30
					}],
					bbar: ['->', {
						text: 'Rimuovi tutte',
						handler: function() {
							me.store.removeAll();
							me.hideMenu();
						}
					}],
					listeners: {
						rowdblclick: function(s, rec) {
							if (rec.get('autoRemove') === true) me.removeGridRecord(s, rec);
							if (rec.get('notifyService') === true) {
								me.fireEvent('notifyService', me, rec);
								me.hideMenu();
							}
						}
					}
				}],
				height: 300,
				width: 250
			}
		});
		
		me.callParent(arguments);
	},
	
	
	
	/**
	 * Binds a store to this instance.
	 * @param {Ext.data.AbstractStore/String} [store] The store to bind or ID of the store.
	 * When no store given (or when `null` or `undefined` passed), unbinds the existing store.
	 */
	bindStore: function(store, /* private */ initial) {
		var me = this;
		me.mixins.storeholder.bindStore.call(me, store, initial);
		store = me.getStore();
		this.setBadgeText(store.getCount());
	},
	
	/**
	 * See {@link Ext.util.StoreHolder StoreHolder}.
	 */
	getStoreListeners: function(store, o) {
		var me = this;
		return {
			scope: me,
			datachanged: me.onStoreDataChanged,
			load: me.onStoreLoad
		};
	},
	
	onStoreDataChanged: function(s) {
		this.setBadgeText(this.formatBadgeText(s.getCount()));
	},
	
	onStoreLoad: function(s, recs, success) {
		this.setBadgeText(this.formatBadgeText(s.getCount()));
	},
	
	privates: {
		/**
		 * @private
		 * Removes the passed record from the grid's store.
		 * This method acts as workaround for avoiding the menu disappearance
		 * after a remove operation against the grid's store. Calling directly
		 * remove() method works only for the last item in the collection.
		 * @param {Ext.grid.Panel} grid
		 * @param {Ext.data.Model} rec
		 */
		removeGridRecord: function(grid, rec) {
			var me = this,
					sto = grid.getStore();
			sto.suspendEvents();
			sto.remove(rec);
			sto.resumeEvents();
			grid.refresh();
			me.setBadgeText(me.formatBadgeText(sto.getCount()));
		},
		
		formatBadgeText: function(count) {
			if (count === 0) {
				return null;
			} else if (count >= 10) {
				return '+9';
			} else {
				return count.toString();
			}
		}
	}
});
