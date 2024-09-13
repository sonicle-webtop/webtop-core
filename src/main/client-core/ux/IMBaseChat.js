/* 
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.IMBaseChat', {
	alternateClassName: 'WTA.ux.IMBaseChat',
	extend: 'Ext.panel.Panel',
	requires: [
		'Sonicle.picker.Emoji',
		'Sonicle.plugin.DropMask',
		'Sonicle.upload.Button',
		'WTA.ux.UploadBar',
		'WTA.ux.grid.column.ChatMessage'
	],
	mixins: [
		'WTA.mixin.PanelUtil',
		'WTA.mixin.Waitable'
	],
	
	layout: 'border',
	referenceHolder: true,
	border: false,
	
	config: {
		hotMarker: false
	},
	
	chatId: null,
	chatName: null,
	dateFormat: null,
	timeFormat: null,
	isGroupChat: false,
	
	viewModel: {
		data: {
			chatDate: null,
			lastTimestamp: null,
			friendFullId: null,
			friendStatus: null,
			hot: false
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		if (Ext.isEmpty(cfg.chatId)) Ext.raise('Config `chatId` is mandatory.');
		if (Ext.isEmpty(cfg.chatName)) Ext.raise('Config `chatName` is mandatory.');
		me.isGroupChat = WTA.ux.IMPanel.isGroupChat(cfg.chatId);
		me.dateFormat = WT.getShortDateFmt();
		me.timeFormat = WT.getShortTimeFmt();
		
		Ext.apply(cfg || {}, {
			title: cfg.chatName,
			iconCls: WTA.ux.IMBaseChat.buildIconCls(me.isGroupChat, Ext.isBoolean(cfg.hotMarker) ? cfg.hotMarker : false)
		});
		
		me.callParent([cfg]);
		if (!Ext.isEmpty(cfg.hotMarker)) me.getVM().set('hot', cfg.hotMarker);
		WTU.applyFormulas(me.getViewModel(), {
			foIsRtcEnabled: {
				bind: {bindTo: '{friendStatus}'},
				get: function(val) {
					if (WTA.ux.IMPanel.isStatusOffline(val)) return false;
					if (!WTA.RTCManager.connected()) return false;
					if (Ext.isEmpty(this.get('friendFullId'))) return false;
					return true;
				}
			}
		});
	},
	
	initComponent: function() {
		var me = this;
		me.scrollTask = new Ext.util.DelayedTask(me.onScrollTask, me);
		
		Ext.apply(me, {
			tbar: me.createTBar(),
			bbar: me.createBBar()
		});
		me.callParent(arguments);
		if (!me.isGroupChat) me.refreshFriendPresence();
	},
	
	destroy: function() {
		var me = this;
		me.callParent();
		if (me.scrollTask) {
			me.scrollTask.cancel();
			me.scrollTask = null;
		}
	},
	
	todayCmp: function() {
		return this.lref('gptoday');
	},
	
	messageFld: function() {
		return this.lref('fldmessage');
	},
	
	createTBar: function() {
		return null;
	},
	
	createBBar: function() {
		return null;
	},
	
	createTodayCmp: function(id) {
		var me = this;
		return {
			xtype: 'grid',
			id: id,
			reference: 'gptoday',
			border: false,
			rowLines: false,
			bufferedRenderer: false,
			viewConfig: {
				markDirty: false,
				stripeRows: false,
				enableTextSelection: true,
				listeners: {
					viewready: function(s) {
						me.scrollToEnd();
					},
					scope: me
				}
			},
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.model.IMMessageGrid',
				proxy: WTF.apiProxy(WT.ID, 'ManageGridIMChatMessages', 'data', {
					extraParams: {
						chatId: me.chatId
					}
				}),
				listeners: {
					load: function(s) {
						var rec = s.last();
						me.getVM().set('lastTimestamp', rec ? rec.get('timestamp') : null);
						me.scrollToEnd();
					},
					scope: me
				}
			},
			columns: [{
				xtype: 'wtchatmessagecolumn',
				dataIndex: 'id',
				dateFormat: WT.getShortDateFmt(),
				flex: 1
			}],
			plugins: [
				{
					ptype: 'sodropmask',
					text: WT.res('sofiledrop.text'),
					monitorExtDrag: false,
					shouldSkipMasking: function(dragOp) {
						return !Sonicle.plugin.DropMask.isBrowserFileDrag(dragOp);
					}
				}
			]
		};
	},
	
	createMessageFld: function() {
		var me = this;
		return {
			xtype: 'textarea',
			reference: 'fldmessage',
			emptyText: WT.res('wtimchat.fld-message.emp'),
			enterIsSpecial: true,
			listeners: {
				specialkey: function(s, e) {
					if (!e.shiftKey && (e.getKey() === e.ENTER)) {
						e.stopEvent();
						me.sendMessage(s.getValue());
						s.setValue(null);
					}
				}
			}
		};
	},
	
	createSendBtn: function() {
		var me = this;
		return {
			xtype: 'button',
			ui: 'default-toolbar',
			tooltip: WT.res('wtimchat.btn-send.tip'),
			iconCls: 'far fa-paper-plane',
			handler: function() {
				var fld = me.messageFld();
				me.sendMessage(fld.getValue());
				fld.setValue(null);
			}
		};
	},
	
	createUploadBtn: function(dropElId) {
		var me = this;
		return {
			xtype: 'souploadbutton',
			iconCls: 'wt-icon-attach',
			tooltip: WT.res('wtimchat.btn-attach.tip'),
			uploaderConfig: WTF.uploader(WT.ID, 'UploadWebChatFile', {
				extraParams: {
					chatId: me.chatId
				},
				dropElement: dropElId,
				maxFileSize: WT.getVar('imUploadMaxFileSize')
			}),
			listeners: {
				uploadstarted: function() {
					me.wait();
				},
				uploadcomplete: function() {
					me.unwait();
				},
				uploaderror: function(s, file, cause) {
					me.unwait(true);
					WTA.ux.UploadBar.handleUploadError(s, file, cause);
				},
				fileuploaded: function(s, file, json) {
					me.addUploadMessage(json.data);
					if (WT.getVar('imSoundOnMessageSent')) {
						Sonicle.Sound.play('wt-im-sent');
					}
				},
				uploadprogress: function(s, file, percent) {
					me.wait(Ext.String.format('{0}: {1}%', file.name, percent), true);
					//me.waitUpdate(Ext.String.format('{0}: {1}%', file.name, percent));
				}
			}
		};
	},
	
	createMeetingButton: function() {
		var me = this;
		return {
			xtype: 'button',
			disabled: Ext.isEmpty(WT.getMeetingProvider()) || !WT.isPermitted(WT.ID, 'MEETING', 'CREATE'),
			iconCls: 'wt-icon-newMeeting',
			tooltip: WT.res('act-addNewMeeting.lbl'),
			handler: function() {
				me.wait();
				me.getMeetingLink({
					callback: function(success, data) {
						me.unwait();
						if (success) {
							var fld = me.messageFld(),
									value = fld.getValue();
							if (Ext.isEmpty(value)) {
								fld.setValue(Ext.String.format(data.embedTexts.info, data.link));
							} else {
								fld.setValue(value+' '+data.link);
							}
							fld.focus(false, 50);
						}
					}
				});
			}
		};
	},
	
	createAudioCallButton: function() {
		var me = this;
		return {
			xtype: 'button',
			bind: {
				disabled: '{!foIsRtcEnabled}'
			},
			iconCls: 'wt-icon-audio-call',
			tooltip: WT.res('wtimchat.btn-audiocall.tip'),
			handler: function() {
				me.makeRtcCall(me.getViewModel().get('friendFullId'), false);
			}
		};
	},
	
	createVideoCallButton: function() {
		var me = this;
		return {
			xtype: 'button',
			bind: {
				disabled: '{!foIsRtcEnabled}'
			},
			iconCls: 'wt-icon-video-call',
			tooltip: WT.res('wtimchat.btn-videocall.tip'),
			handler: function() {
				me.makeRtcCall(me.getViewModel().get('friendFullId'), true);
			}
		};
	},
	
	getChatId: function() {
		return this.chatId;
	},
	
	sendMessage: function(text) {
		var me = this;
		if (Ext.isEmpty(text)) return;
		
		WT.ajaxReq(WT.ID, 'ManageIMChat', {
			params: {
				crud: 'send',
				chatId: me.chatId,
				text: text,
				lastSeenDate: Ext.Date.format(me.getVM().get('lastTimestamp'), 'Y-m-d')
			},
			callback: function(success, json) {
				var fld = me.lref('fldmessage');
				if (success) {
					Ext.iterate(json.data, function(item) {
						me.addMessage(item);
					});
					fld.focus(true, 100);
					me.fireSend();
				} else {
					fld.setValue(text);
				}
			}
		});
	},
	
	newMessage: function(uid, fromId, fromNick, timestamp, action, text, data) {
		this.addMessage({
			id: uid,
			fromId: fromId,
			fromNick: fromNick,
			timestamp: timestamp,
			action: action,
			text: text,
			data: data,
			fromArchive: false
		});
	},
	
	addUploadMessage: function(data) {
		this.addMessage(data);
		this.fireSend();
	},
	
	fireSend: function() {
		var me = this;
		me.fireEvent('send', me);
		if (WT.getVar('imSoundOnMessageSent')) {
			Sonicle.Sound.play('wt-im-sent');
		}
	},
	
	setFriendPresence: function(friendFullId, status) {
		var me = this,
				vm = me.getViewModel();
		vm.set('friendFullId', friendFullId);
		vm.set('friendStatus', status);
		me.fireEvent('updatefriendpresence', me, friendFullId, status);
	},
	
	setHotMarker: function(hot) {
		var me = this,
				vm = me.getViewModel();
		vm.set('hot', hot);
	},
	
	privates: {
		addMessage: function(data) {
			var me = this,
				gp = me.todayCmp(),
				sto = gp.getStore(),
				rec;
			rec = sto.add(sto.createModel(data))[0];
			me.getVM().set('lastTimestamp', rec.get('timestamp'));
			me.scrollToEnd();
		},
		
		scrollToEnd: function() {
			var me = this;
			if (me.isVisible()) {
				me.scrollTask.delay(200);
			} else {
				me.scrollOnActivate = true;
			}
		},
		
		onScrollTask: function() {
			/* 1 - Scroll to last record
			var gp = this.lref('gptoday'),
					rec = gp.getStore().last();
			if (rec) this.scrollViewToRecord(gp.getView(), rec);
			*/
			// 2 - Scroll to end
			this.scrollViewToEnd(this.todayCmp().getView());
		},
		
		scrollViewToEnd: function(view) {
			var scroll = view.getScrollable();
			if (view.rendered && scroll) {
				scroll.scrollTo(Infinity, Infinity, false);
			}
		},
		
		scrollViewToRecord: function(view, rec) {
			var scroll = view.getScrollable(),
					row, cell, scroll;
			if (view.rendered) {
				row = view.getRowByRecord(rec);
				if (scroll && row) {
					cell = Ext.fly(row).down(view.getCellSelector(), true);
					if (cell) {
						cell = new Ext.dom.Fly(cell);
						scroll.scrollIntoView(cell);
					}
				}
			}
		},
		
		getMeetingLink: function(opts) {
			opts = opts || {};
			var me = this;	
			WT.ajaxReq(WT.ID, 'ManageMeeting', {
				params: {
					crud: 'create'
				},
				callback: function(success, json) {
					Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
				}
			});
		},
		
		makeRtcCall: function(friendFullId, video) {
			var rtcJid = WTA.RTCManager.buildRtcJid(friendFullId);
			WTA.RTCManager.startCall(rtcJid.split('/')[0], rtcJid, video);
		},
		
		refreshFriendPresence: function() {
			var me = this;
			WT.ajaxReq(WT.ID, 'ManageIMChat', {
				params: {
					crud: 'presence',
					chatId: me.chatId
				},
				callback: function(success, json) {
					if (success) me.setFriendPresence(json.data.friendFullId, json.data.presenceStatus);
				}
			});
		}
	},
	
	statics: {
		buildIconCls: function(group, hot) {
			var ico = group ? 'wt-icon-im-gchat' : 'wt-icon-im-ichat';
			if (hot) ico += '-hot';
			return ico;
		}
	}
});
