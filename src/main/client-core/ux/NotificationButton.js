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
	/*
	requires: [
		'WTA.ux.grid.Notification'
	],
	*/
	uses: [
		'WTA.model.Notification'
	],
	
	arrowVisible: false,
	iconCls: 'wt-icon-notification-xs',
	
	initComponent: function() {
		var me = this;
		me.store = Ext.create('Ext.data.Store', {
			model: 'WTA.model.Notification',
			proxy: {
				type: 'memory',
				reader: 'json'
			}
		});
		
		me.store.add([
			me.store.createModel({
				serviceId: 'com.sonicle.webtop.core',
				iconCls: 'wt-icon-service-m',
				title: 'Notifica 1',
				notifyService: true,
				autoRemove: true
			}),
			me.store.createModel({
				serviceId: 'com.sonicle.webtop.mail',
				iconCls: 'wtmail-icon-service-m',
				title: 'Notifica 2',
				notifyService: true,
				autoRemove: false
			}),
			me.store.createModel({
				serviceId: 'com.sonicle.webtop.vfs',
				iconCls: 'wtvfs-icon-service-m',
				title: 'Notifica 3',
				notifyService: false,
				autoRemove: true
			}),
			me.store.createModel({
				serviceId: 'com.sonicle.webtop.valendar',
				iconCls: 'wtcal-icon-service-m',
				title: 'Notifica 4',
				notifyService: false,
				autoRemove: false
			})
		]);
			
		
		Ext.apply(me, {
			menu: {
				xtype: 'menu',
				plain: true,
				layout: 'fit',
				forceLayout: true,
				items: [{
					xtype: 'grid',
					title: 'Notifiche',
					store: me.store,
					selModel: {
						type: 'rowmodel'
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
						xtype:'actioncolumn',
						items: [{
							iconCls: 'wt-icon-notification-remove-xs',
							tooltip: 'Rimuovi',
							handler: function(s, rindx, cindx, itm, e) {
								//s.getStore().removeAt(rindx);
								e.stopPropagation();
								Ext.defer(function() {
									var sto = me.store;
									sto.remove(sto.getAt(rindx));
								}, 100);
								//var sto = me.store;
								//sto.remove(sto.getAt(rindx));
								
							}
						}],
						width: 30
					}],
					bbar: ['->', {
						text: 'Rimuovi tutte',
						handler: function() {
							me.store.removeAll();
						}
					}],
					listeners: {
						rowdblclick: function(s, rec, el, rindx, e) {
							e.stopEvent();
							me.store.remove(rec);
							/*
							if (rec.get('autoRemove')) me.store.remove(rec);
							if (rec.get('notifyService') === true) {
								me.fireEvent('notifyService', me, rec);
							} else {
								e.stopPropagation();
							}
							*/
						}
					}
				}],
				height: 300,
				width: 250
			}
		});
		me.callParent(arguments);
	}
});
