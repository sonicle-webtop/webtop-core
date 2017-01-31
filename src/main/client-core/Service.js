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
Ext.define('Sonicle.webtop.core.Service', {
	extend: 'WTA.sdk.Service',
	requires: [
		'Sonicle.webtop.core.model.ServiceVars',
		'Sonicle.webtop.core.view.Activities',
		'Sonicle.webtop.core.view.Causals'
	],
	
	vwrem: null,
	
	init: function() {
		var me = this;
		WT.checkDesktopNotificationAuth();
		
		me.onMessage('reminderNotify', function(msg) {
			var pl = msg.payload;
			me.showReminder(pl);
			WT.showDesktopNotification(pl.serviceId, {
				title: pl.title
			});
		});
		
		me.onMessage('autosaveNotify', function(msg) {
			var me=this, pl = msg.payload;
			
			if (pl.mine) {
				me.notifyMyAutosave(pl);
			}
			else if (pl.others) {
				me.notifyOthersAutosave(pl);
			}
			
		});
	},
	
	notifyMyAutosave: function(pl) {
		var me=this;
		Ext.Msg.show({
			title: WT.res('warning'),
			msg: WT.res('autosave.mine-notification.msg'),
			buttons: Ext.MessageBox.YESNOCANCEL,
			buttonText: {
				yes: WT.res('word.yes'),
				no: WT.res('word.no'),
				cancel: WT.res('act-remove-all.lbl')
			},
			icon: Ext.Msg.QUESTION,
			fn: function(bid) {
				if (bid === 'no') {
					if (pl.others) me.notifyOthersAutosave(pl);
					return;
				}
				if (bid === 'cancel' ) {
					WT.ajaxReq(me.ID, "RemoveAutosave", {
						params: {
							allMine: true
						},
						callback: function(success,json) {
							if (success) {
								if (pl.others) me.notifyOthersAutosave(pl);
							} else {
								WT.error(json.text);
							}
						}
					});
				} else {
					WT.ajaxReq(me.ID, "RestoreAutosave", {
						params: {
							mine: true
						},
						callback: function(success,json) {
							if (success) {
								if (pl.others) me.notifyOthersAutosave(pl);
							} else {
								WT.error(json.text);
							}
						}
					});
				}
			}
		});
	},
	
	notifyOthersAutosave: function() {
		var me=this;
		Ext.Msg.show({
			title: WT.res('warning'),
			msg: WT.res('autosave.others-notification.msg'),
			buttons: Ext.MessageBox.YESNOCANCEL,
			buttonText: {
				yes: WT.res('word.yes'),
				no: WT.res('word.no'),
				cancel: WT.res('act-remove-all.lbl')
			},
			icon: Ext.Msg.QUESTION,
			fn: function(bid) {
				if (bid === 'no') return;
				if (bid === 'cancel' ) {
					WT.ajaxReq(me.ID, "RemoveAutosave", {
						params: {
							allOthers: true
						},
						callback: function(success,json) {
							if (!success) {
								WT.error(json.text);
							}
						}
					});
				} else {
					WT.ajaxReq(me.ID, "RestoreAutosave", {
						params: {
							mine: false
						},
						callback: function(success,json) {
							if (!success) {
								WT.error(json.text);
							}
						}
					});
				}
			}
		});
	},
	
	showActivities: function() {
		WT.createView(this.ID, 'view.Activities').show();
	},
	
	showCausals: function() {
		WT.createView(this.ID, 'view.Causals').show();
	},
	
	showReminder: function(data) {
		var me = this;
		
		if(me.vwrem) {
			me.vwrem.getView().addReminder(data);
		} else {
			me.vwrem = WT.createView(me.ID, 'view.Reminder');
			me.vwrem.on('close', function() {
				me.vwrem = null;
			}, {single: true});
			me.vwrem.show(false, function() {
				Ext.defer(function() {
					me.vwrem.getView().addReminder(data);
				}, 200);
			});
		}
	},
	
	changeUserPassword: function(oldPassword, newPassword, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ChangeUserPassword', {
			params: {
				oldPassword: oldPassword,
				newPassword: newPassword
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	}
});
