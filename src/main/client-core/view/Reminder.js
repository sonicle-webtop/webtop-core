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
Ext.define('Sonicle.webtop.core.view.Reminder', {
	alternateClassName: 'WTA.view.Reminder',
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.webtop.core.model.ReminderAlert',
		'Sonicle.webtop.core.store.Snooze'
	],
	
	dockableConfig: {
		title: '{reminder.tit}',
		iconCls: 'wt-icon-reminder-xs',
		width: 450,
		height: 250
	},
	
	viewModel: {
		stores: {
			reminders: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.model.ReminderAlert'
			}
		}
	},
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			fbar: [
				WTF.localCombo('id', 'desc', {
					reference: 'cbosnooze',
					store: Ext.create('Sonicle.webtop.core.store.Snooze', {
						autoLoad: true
					}),
					fieldLabel: me.mys.res('reminder.cbo-snooze.lbl'),
					labelWidth: 70,
					width: 190,
					value: 5
				}), ' ', {
					xtype: 'button',
					text: WT.res('reminder.btn-snooze.lbl'),
					iconCls: 'wt-icon-snooze-xs',
					handler: function() {
						var sm = me.lref('gpreminders').getSelectionModel();
						if (sm.hasSelection()) me.snoozeReminder(sm.getSelection());
					}
				}, '->', {
					xtype: 'button',
					text: WT.res('reminder.btn-ignore.lbl'),
					handler: function() {
						var sm = me.lref('gpreminders').getSelectionModel();
						if (sm.hasSelection()) me.deleteReminder(sm.getSelection());
					}
				}
			]
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			layout: 'fit',
			items: [
				{
					xtype: 'gridpanel',
					reference: 'gpreminders',
					bind: {
						store: '{reminders}'
					},
					border: true,
					selModel: {
						type: 'checkboxmodel',
						mode : 'MULTI'
					},
					columns: [
						{
							xtype: 'soiconcolumn',
							getIconCls: function(v,rec) {
								return WTF.cssIconCls(WT.findXid(rec.get('serviceId')), 'reminder-'+rec.get('type'));
							},
							iconSize: WTU.imgSizeToPx('xs'),
							width: 40
						}, {
							dataIndex: 'title',
							header: WT.res('reminder.gpreminders.title.lbl'),
							flex: 1
						}, {
							dataIndex: 'date',
							xtype: 'datecolumn',
							format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
							header: WT.res('reminder.gpreminders.date.lbl'),
							flex: 1	
						}
					]
				}
			]
		});
	},
	
	addReminder: function(data) {
		if (!Ext.isArray(data)) data = [data];
		var me = this,
				gp = me.lref('gpreminders'),
				sto = me.getViewModel().getStore('reminders'),
				rec;
		
		Ext.iterate(data, function(obj) {
			rec = sto.add(Ext.create('WTA.model.ReminderAlert', obj));
			if (rec) gp.getSelectionModel().select(rec, false, false);
		});
	},
	
	snoozeReminder: function(recs) {
		var me = this,
				cbo = me.lref('cbosnooze'),
				sto = me.getViewModel().getStore('reminders'),
				json = [];
		
		if (recs.length > 0) {
			Ext.iterate(recs, function(rec) {
				json.push(rec.getData({serialize: true}));
			});
			WT.ajaxReq(WT.ID, 'SnoozeReminder', {
				params: {
					now: Ext.Date.format(new Date(), 'Y-m-d H:i:s'),
					snooze: cbo.getValue()
				},
				jsonData: json,
				callback: function(success) {
					if (success) sto.remove(recs);
					if (sto.getCount()===0) me.closeView();
				}
			});
		}
	},
	
	deleteReminder: function(rec) {
		var me = this,
				sto = me.getViewModel().getStore('reminders');
		sto.remove(rec);
		if (sto.getCount()===0) me.closeView();
	}
});
