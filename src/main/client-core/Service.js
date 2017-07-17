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
		'Sonicle.webtop.core.view.IMChat',
		'Sonicle.webtop.core.view.Activities',
		'Sonicle.webtop.core.view.Causals'
	],
	
	vwrem: null,
	
	init: function() {
		var me = this;
		WT.checkDesktopNotificationAuth();
		me.initActions();
		
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
		
		me.onMessage('imUpdateFriendPresence', function(msg) {
			var me = this, pl = msg.payload;
			me.getVPController().getIMPanel().updateFriendPresence(pl.id, pl.presenceStatus, pl.statusMessage);
		});
		me.onMessage('imChatRoomMessage', function(msg) {
			var me = this, pl = msg.payload,
				ts = Ext.Date.parse(pl.timestamp, 'Y-m-d H:i:s', true);
			me.newIMChatMessage(pl.chatId, pl.fromId, pl.fromNick, ts, 'none', pl.msgUid, pl.msgText);
		});
		
		Ext.defer(function() {
			me.initIM({
				callback: function(success) {
					if (success) me.getVPController().getIMPanel().loadFriends();
				}
			});
		}, 1000);
	},
	
	getVP: function() {
		return WT.getApp().viewport;
	},
	
	getVPController: function() {
		return this.getVP().getController();
	},
	
	initActions: function() {
		var me = this;
		if (me.isPermitted('ACTIVITIES', 'MANAGE')) {
			me.addAct('toolbox', 'manageActivities', {
				text: WT.res('activities.tit'),
				tooltip: null,
				iconCls: 'wt-icon-activity-xs',
				handler: function() {
					me.showActivities();
				}
			});
		}
		if (me.isPermitted('CAUSALS', 'MANAGE')) {
			me.addAct('toolbox', 'manageCausals', {
				text: WT.res('causals.tit'),
				tooltip: null,
				iconCls: 'wt-icon-causal-xs',
				handler: function() {
					me.showCausals();
				}
			});
		}
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
	
	updateIMPresenceStatusUI: function(status) {
		var me = this;
		me.updateIMPresenceStatus(status, {
			callback: function(success) {
				if (success) me.getVPController().getIMButton().setPresenceStatus(status);
			}
		});
	},
	
	updateIMStatusMessageUI: function(message) {
		var me = this;
		me.updateIMStatusMessage(message, {
			callback: function(success) {
				if (success) me.getVPController().updateIMStatusMessage(message);
			}
		});
	},
	
	newIMChatMessage: function(chatId, fromId, fromNick, timestamp, action, msgUid, msgText) {
		var me = this, vct = WT.getView(me.ID, me.imChatTag(chatId)), data, ret;
		if (vct) {
			// blink chat window if minimized...
			vct.getView().newMessage(msgUid, fromId, fromNick, timestamp, action, msgText);
		} else {
			// notify with indicator/desktop notification
			WT.showNotification(me.ID, {
				title: fromNick,
				body: msgText,
				data: {
					chatId: chatId
				}
			});
		}
	},
	
	openIMChatUI: function(chatId, chatName) {
		var me = this, vct = WT.getView(me.ID, me.imChatTag(chatId));
		if (!vct) {
			vct = me.createIMChatView(chatId, chatName);
			vct.show();
		} else {
			vct.show();
		}
	},
	
	addIMChatUI: function(chatId, chatName, friendIds) {
		var me = this, vct = WT.getView(me.ID, me.imChatTag(chatId));
		if (!vct) {
			me.prepareIMChat(chatId, friendIds, {
				callback: function(success, json) {
					if (success) {
						me.getVPController().getIMPanel().newChat(chatId, chatName);
						vct = me.createIMChatView(chatId, chatName);
						vct.show();
					} else {
						WT.error(json.text);
					}
				}
			});
		} else {
			vct.show();
		}
	},
	
	initIM: function(opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageIM', {
			params: {
				crud: 'init'
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateIMPresenceStatus: function(status, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageIM', {
			params: {
				crud: 'presence',
				presenceStatus: status
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateIMStatusMessage: function(message, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageIM', {
			params: {
				crud: 'presence',
				statusMessage: message
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	prepareIMChat: function(chatId, withUsers, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(this.ID, 'ManageIMChat', {
			params: {
				crud: 'prepare',
				chatId: chatId,
				chatName: null,
				withUsers: WTU.arrayAsParam(withUsers)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
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
	},
	
	privates: {
		imChatTag: function(chatId) {
			return 'imchat-' + chatId;
		},
		
		createIMChatView: function(chatId, chatName) {
			return WT.createView(this.ID, 'view.IMChat', {
				tag: this.imChatTag(chatId),
				viewCfg: {
					chatId: chatId,
					chatName: chatName
				}
			});
		}
	}
});
