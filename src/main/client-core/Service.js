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
		'Sonicle.webtop.core.app.RTCManager'
	],
	uses: [
		'Sonicle.webtop.core.view.IMQuickChat',
		'Sonicle.webtop.core.view.IMChats',
		'Sonicle.webtop.core.view.Activities',
		'Sonicle.webtop.core.view.Causals'
	],
	
	vwrem: null,
	
	init: function() {
		var me = this, dparts = {}, cont = [];
		WT.checkDesktopNotificationAuth();
		me.initActions();
		
		Ext.iterate(WT.getApp().getDescriptors(false), function(desc) {
			Ext.iterate(desc.getPortletClassNames(), function(cname) {
				dparts[WTU.idfy(cname)] = {
					xclass: cname,
					sid: desc.getId()
				};
				cont.push({
					type: WTU.idfy(cname),
					columnIndex: cont.length,
					height: 300
				});
			});
		});
		//cont = Ext.JSON.decode(me.getVar('portalContent'), true) || [];
		
		me.setMainComponent(Ext.create({
			xtype: 'dashboard',
			stateful: false,
			bodyCls: 'wt-portal',
			columnWidths: [
				0.25,0.25,0.25,0.25
			],
			parts: dparts,
			defaultContent: cont
		}));
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,
			layout: {
				pack: 'center'
			},
			items: [{
				xtype: 'textfield',
				reference: 'fldgsearch',
				emptyText: me.res('gsearch.emp'),
				plugins: ['sofieldtooltip'],
				triggers: {
					clear: WTF.clearTrigger(),
					search: {
						cls: Ext.baseCSSPrefix + 'form-search-trigger',
						handler: function(s) {
							me.queryPortlets(s.getValue());
						}
					}
				},
				listeners: {
					specialkey: function(s, e) {
						if (e.getKey() === e.ENTER) {
							me.queryPortlets(s.getValue());
						}
					},
					clear: function() {
						me.queryPortlets(null);
					}
				},
				width: '50%'
			}]
		}));
		
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
		
		if (WT.getVar('imEnabled')) {
			me.onMessage('imFriendsUpdated', function(msg) {
				me.getVPController().getIMPanel().loadFriends();
			});
			me.onMessage('imFriendPresenceUpdated', function(msg) {
				var pl = msg.payload;
				me.getVPController().getIMPanel().updateFriendPresence(pl.id, pl.presenceStatus, pl.statusMessage);
				me.setChatRoomFriendPresenceUI(pl.chatId, pl.friendFullId, pl.presenceStatus);
			});
			me.onMessage('imChatRoomAdded', function(msg) {
				var pl = msg.payload;
				me.getVPController().getIMPanel().loadChats();
				if (!pl.self) me.showIMChatAddedNotification(pl.chatId, pl.chatName, pl.ownerId, pl.ownerNick);
			});
			me.onMessage('imChatRoomRemoved', function(msg) {
				me.getVPController().getIMPanel().loadChats();
			});
			me.onMessage('imChatRoomClosed', function(msg) {
				var pl = msg.payload;
				me.showIMChatClosedNotification(pl.chatId, pl.chatName, pl.ownerId, pl.ownerNick);
			});
			me.onMessage('imChatRoomUpdated', function(msg) {
				var pl = msg.payload;
				me.getVPController().getIMPanel().updateChatName(pl.chatId, pl.chatName);
			});
			me.onMessage('imChatRoomMessageReceived', function(msg) {
				var pl = msg.payload,
					ts = Ext.Date.parse(pl.ts, 'Y-m-d H:i:s', true);
				me.newChatRoomMessageUI(pl.chatId, pl.chatName, pl.fromId, pl.fromNick, ts, pl.uid, pl.action, pl.text, pl.data);
			});

			Ext.defer(function() {
				me.initIM({
					callback: function(success, json) {
						if (success) {
							var pnlIm = me.getVPController().getIMPanel(),
								ps = json.data ? json.data.presenceStatus : null;
							if (ps) {
								pnlIm.setPresenceStatus(json.data.presenceStatus);
								me.getVPController().getIMButton().setPresenceStatus(ps);
							}
							pnlIm.loadFriends();
							
							//if BOSH url configured, intialize RTC
							var boshUrl=WT.getVar('boshUrl');
							var iceServers=WT.getVar('iceServers');
							if (boshUrl) {
								me.initRTCManager(boshUrl,iceServers,json.data.userId,json.data.password, json.data.userJid+"RTC");
							}
							
						} else {
							if (!Ext.isEmpty(json.message)) WT.warn(json.message);
						}
					}
				});
			}, 1000);
		}
		
		me.on('activate', function() {
			var me = this,
				dboard = me.getMainComponent(), pbody;
				
			for(var i=0;i<dboard.items.items.length;i+=2) {
				pbody = dboard.items.items[i].items.items[0].items.items[0];
				if (pbody) pbody.refresh();
			}
			me.getToolbar().lookupReference('fldgsearch').focus(true, 400);
		});
	},
	
	queryPortlets: function(s) {
		var me=this,
			db=me.getMainComponent();
	
		for(i=0;i<db.items.items.length;i+=2) {
			var portletBody=db.items.items[i].items.items[0].items.items[0];
			if (!s || s.trim().length===0) portletBody.recents();
			else portletBody.search(s);
		}
	},
	
	notificationCallback: function(type, tag, data) {
		var me = this;
		if (Ext.String.startsWith(tag, me.self.NOTAG_IM_NEWMSG)) {
			me.openChatRoomUI(data.chatId, data.chatName);
		} else if (Ext.String.startsWith(tag, me.self.NOTAG_IM_CHAT)) {
			me.openChatRoomUI(data.chatId, data.chatName);
		}
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
	
	clearIMNewMsgNotification: function(chatId) {
		var me = this;
		me.getVPController().getIMPanel().updateChatHotMarker(chatId, false);
		WT.clearBadgeNotification(me.ID, me.self.noTagIMNewMsg(chatId));
	},
	
	showIMNewMsgNotification: function(chatId, title, body, data, opts) {
		opts = opts || {};
		var me = this;
		if (opts.hotMarker === true) me.getVPController().getIMPanel().updateChatHotMarker(chatId, true);
		if (opts.sound === true && WT.getVar('imSoundOnMessageReceived')) opts.play = 'wt-im-receive';
		me.showIMNotification(me.self.noTagIMNewMsg(chatId), title, body, data, opts);
	},
	
	clearIMChatNotification: function(chatId) {
		WT.clearBadgeNotification(this.ID, this.self.noTagIMChat(chatId));
	},
	
	showIMChatAddedNotification: function(chatId, chatName, ownerId, ownerNick) {
		var me = this,
				title = Ext.String.format('{0}: {1}', chatName, ownerNick),
				body = me.res('not.im.chatadded.body', ownerNick),
				data = {chatId: chatId, chatName: chatName, ownerId: ownerId};
		me.showIMNotification(me.self.noTagIMChat(chatId), title, body, data, {badge: true});
	},
	
	showIMChatClosedNotification: function(chatId, chatName, ownerId, ownerNick) {
		var me = this,
				title = Ext.String.format('{0}: {1}', chatName, ownerNick),
				body = me.res('not.im.chatclosed.body', ownerNick),
				data = {chatId: chatId, chatName: chatName, ownerId: ownerId};
		me.showIMNotification(me.self.noTagIMChat(chatId), title, body, data, {badge: true});
	},
	
	showIMNotification: function(tag, title, body, data, opts) {
		opts = opts || {};
		var me = this;
		if (opts.badge === true) {
			WT.showBadgeNotification(me.ID, {
				tag: tag,
				title: title,
				iconCls: me.cssIconCls('im-chat', 'm'),
				body: body,
				data: data
			}, {callbackService: (opts.callbackService === undefined) ? true : opts.callbackService});
		}
		if (opts.desktop === true) {
			WT.showDesktopNotification(me.ID, {
				tag: tag,
				title: title,
				body: body,
				data: data
			}, {callbackService: (opts.callbackService === undefined) ? true : opts.callbackService});
		}
		if (opts.play) {
			Sonicle.Sound.play(opts.play);
		}
	},
	
	openChatRoomUI: function(chatId, chatName, extended) {
		var me = this, vw;
		me.clearIMNewMsgNotification(chatId);
		me.clearIMChatNotification(chatId);
		
		if (extended === true) {
			if (vw = WT.getView(me.ID, me.self.vwTagIMChats())) {
				vw.openChat(chatId, chatName);
				vw.showView();
			} else {
				me.createIMChatsView().showView(function() {
					this.openChat(chatId, chatName);
				});
			}
		} else if ((vw = WT.getView(me.ID, me.self.vwTagIMChats())) && vw.hasChat(chatId)) {
			vw.openChat(chatId, chatName);
			vw.showView();
		} else if (vw = WT.getView(me.ID, me.self.vwTagIMChat(chatId))) {
			vw.showView();
		} else {
			me.createIMQuickChatView(true, chatId, chatName).showView();
			//me.createIMQuickChatView(chatId, chatName).showView();
		}
		
		
		/*
		if (vw = WT.getView(me.ID, me.self.vwTagIMChat(chatId))) {
			vw.showView();
		} else {
			if (vw = WT.getView(me.ID, me.self.vwTagIMChats())) {
				vw.openChat(chatId, chatName);
				vw.showView();
			} else {
				me.createIMChatsView().showView(function() {
					this.openChat(chatId, chatName);
				});
			}
		}
		*/
		
		//me.createIMQuickChatView(chatId, chatName).showView();
		
		/*
		var me = this, vct = WT.getView(me.ID, me.self.vwTagIMChats());
		me.clearIMNewMsgNotification(chatId);
		me.clearIMChatNotification(chatId);
		if (!vct) {
			vct = me.createIMChatsView();
			vct.show(false, function() {
				vct.getView().openChat(chatId, chatName);
			});
		} else {
			vct.getView().openChat(chatId, chatName);
			vct.show();
		}
		*/
	},
	
	setChatRoomFriendPresenceUI: function(chatId, friendFullId, status) {
		var me = this, vw;
		if (vw = WT.getView(me.ID, me.self.vwTagIMChat(chatId))) {
			vw.setFriendPresence(friendFullId, status);
		} else if (vw = WT.getView(me.ID, me.self.vwTagIMChats())) {
			vw.setChatFriendPresence(chatId, friendFullId, status);
		}
		/*
		var me = this, vct = WT.getView(me.ID, me.self.vwTagIMChats());
		if (vct) vct.getView().setChatFriendPresence(chatId, status);
		*/
	},
	
	newChatRoomMessageUI: function(chatId, chatName, fromId, fromNick, timestamp, uid, action, text, data) {
		var me = this,
			status = WT.getVar('imPresenceStatus'),
			isGroup = WTA.ux.IMPanel.isGroupChat(chatId),
			noBody = isGroup ? (fromNick + ': ' + text) : text,
			noData = {chatId: chatId, chatName: chatName},
			vw;
		
		if (vw = WT.getView(me.ID, me.self.vwTagIMChat(chatId))) { // Target quick-chat is open
			vw.newMessage(uid, fromId, fromNick, timestamp, action, text, data);
			me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {desktop: true, sound: true});
			
		} else if (vw = WT.getView(me.ID, me.self.vwTagIMChats())) { // Chats window is open...
			vw.newChatMessage(chatId, chatName, fromId, fromNick, timestamp, uid, action, text, data);
			if (vw.isChatActive(chatId)) { // ...and target chat tab is already active
				me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {desktop: true, sound: true});
				
			} else { // ...and target chat tab is NOT active
				me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {desktop: true, badge: true, sound: true});
			}
		} else { // No chat windows are open
			if (WTA.ux.IMPanel.isStatusOnline(status, true)) {
				me.createIMQuickChatView(false, chatId, chatName, true).showView();
				me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {desktop: true, sound: true});
				
			} else {
				me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {desktop: true, badge: true});
			}
		}
		
		
		/*
		var me = this,
				vct = WT.getView(me.ID, me.self.vwTagIMChats()),
				isGroup = WTA.ux.IMPanel.isGroupChat(chatId),
				noBody = isGroup ? (fromNick + ': ' + text) : text,
				noData = {chatId: chatId, chatName: chatName},
				vw;
		
		if (vct) {
			//TODO: valutare se far lampeggiare il bottone nella taskbar se minimizzata
			vw = vct.getView();
			vw.newChatMessage(chatId, chatName, fromId, fromNick, timestamp, uid, action, text, data);
			if (!vw.isChatActive(chatId)) {
				me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {badge: true, sound: true});
			} else {
				me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {sound: true});
			}
			
		} else {
			me.showIMNewMsgNotification(chatId, chatName, noBody, noData, {hotMarker: true, badge: true, desktop: true, sound: true});
		}
		*/
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
	
	initRTCManager: function(boshUrl,iceServers,jid,pass,fullJid) {
		var me=this;
		WTA.RTCManager.setBoshUrl(boshUrl);
		WTA.RTCManager.setICEServers(iceServers);
		WTA.RTCManager.initConnection();
		me.doRTCConnect(jid,pass,fullJid);
	},
	
	doRTCConnect: function(jid,pass,fullJid) {
		var me=this;
	
		WTA.RTCManager.connect(fullJid,pass,function(status, condition) {
			console.log(" connect callback, condition="+condition);
			switch (status) {
				case Strophe.Status.CONNECTING:
					console.log("connecting");
					break;
				case Strophe.Status.CONNECTED:
					console.log("connected");
					break;
				case Strophe.Status.ATTACHED:
					console.log("attached");
					break;
				case Strophe.Status.DISCONNECTED:
					console.log("disconnected");
					if (condition==="conflict") {
						WT.showBadgeNotification(WT.ID,{
							tag: 'rtc-conflict',
							title: 'RTC conflict',
							body: jid+' logged in from a different device. RTC audio/video calls disabled from this device'
						});
					} else {
						Ext.defer(function() {
							 me.doRTCConnect(jid,pass,fullJid);
						},2000);
					}
					break;
				case Strophe.Status.CONNFAIL:
					console.log("connfail");
					break;
				case Strophe.Status.AUTHFAIL:
					console.log("authfail");
					break;
			}
		});
	},
	
	updatePortalContent: function(content, opts) {
		var me = this;
		WT.ajaxReq(me.ID, 'UpdatePortalContent', {
			params: {
				content: content
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
				if (success) WT.setVar('imPresenceStatus', status);
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
	
	addGroupChat: function(opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.GroupChat');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {}
			});
		});
	},
	
	privates: {
		createIMChatsView: function() {
			return WT.createView(this.ID, 'view.IMChats', {
				tag: this.self.vwTagIMChats(),
				swapReturn: true
			});
		},
		
		createIMQuickChatView: function(focusOnShow, chatId, chatName, hotMarker) {
			var me = this;
			var vw = WT.createView(me.ID, 'view.IMQuickChat', {
				tag: me.self.vwTagIMChat(chatId),
				floating: true,
				viewCfg: {
					dockableConfig: {
						//title: chatName,
						focusOnShow: focusOnShow
					},
					chatId: chatId,
					chatName: chatName,
					hotMarker: Ext.isDefined(hotMarker) ? hotMarker : false
				}
			});
			
			vw.on('unpinchat', function(s, chatId, chatName) {
				s.on('viewclose', function() {
					me.openChatRoomUI(chatId, chatName, true);
					//Ext.defer(me.openChatRoomUI, 100, me, [chatId, chatName, true]);
				}, {single: true});
			}, {single: true});
			
			return vw;
		},
		
		createIMChatView: function(chatId, chatName) {
			return WT.createView(this.ID, 'view.IMChat', {
				tag: this.self.vwTagIMChat(chatId),
				viewCfg: {
					chatId: chatId,
					chatName: chatName
				}
			});
		}
	},
	
	statics: {
		NOTAG_IM_NEWMSG: 'imnewmsg-',
		NOTAG_IM_CHAT: 'imchat-',
		
		noTagIMNewMsg: function(chatId) {
			return this.NOTAG_IM_NEWMSG + chatId;
		},
		
		noTagIMChat: function(chatId) {
			return this.NOTAG_IM_CHAT + chatId;
		},
		
		vwTagIMChats: function() {
			return 'imchats';
		},
		
		vwTagIMChat: function(chatId) {
			return 'imchat-' + chatId;
		}
	}
});
