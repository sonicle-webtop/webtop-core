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
Ext.define('Sonicle.webtop.core.ux.IMChatXX', {
	alternateClassName: 'WTA.ux.IMChatXX',
	extend: 'Ext.panel.Panel',
	alias: ['widget.wtimchatxx'],
	requires: [
		'Sonicle.picker.Emoji',
		'Sonicle.picker.RemoteDate',
		'Sonicle.plugin.FileDrop',
		'Sonicle.upload.Button',
		'WTA.ux.UploadBar',
		'Sonicle.webtop.core.ux.grid.column.ChatMessage',
		'Sonicle.webtop.core.model.IMMessageGrid',
		'Sonicle.webtop.core.model.IMChatSearchGrid'
	],
	mixins: [
		'WTA.mixin.PanelUtil',
		'WTA.mixin.Waitable'
	],
	
	config: {
		hotMarker: false
	},
	
	chatId: null,
	chatName: null,
	groupChat: false,
	dateFormat: null,
	timeFormat: null,
	RTCJid: null,
	
	layout: 'border',
	referenceHolder: true,
	viewModel: {
		data: {
			chatDate: null,
			lastTimestamp: null
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		if (Ext.isEmpty(cfg.chatId)) Ext.raise('Config `chatId` is mandatory.');
		if (Ext.isEmpty(cfg.chatName)) Ext.raise('Config `chatName` is mandatory.');
		me.groupChat = WTA.ux.IMPanel.isGroupChat(cfg.chatId);
		me.dateFormat = WT.getShortDateFmt();
		me.timeFormat = WT.getShortTimeFmt();
		
		Ext.apply(cfg || {}, {
			title: cfg.chatName,
			iconCls: me.self.buildIconCls(me.groupChat, Ext.isBoolean(cfg.hotMarker) ? cfg.hotMarker : false)
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
				gptodayId = Ext.id(null, 'gridpanel'),
				tbarItms = [],
				haveRTC=WTA.RTCManager.connected();
		
		me.scrollTask = new Ext.util.DelayedTask(me.onScrollTask, me);
		me.setHotMarker(me.hotMarker);
		if (!me.groupChat) {
			tbarItms.push({
				xtype: 'tbtext',
				html: WT.res('wtimchat.tbi-status.lbl')
			}, {
				reference: 'itmpresence',
				text: '',
				iconCls: '',
				focusable: false,
				disabled: true
			});
		}
		tbarItms.push('->');
		if (!me.groupChat) {
			tbarItms.push({
				xtype: 'button',
				tooltip: haveRTC?WT.res('wtimchat.btn-audiocall.tip'):WT.res('rtc.unconfigured'),
				iconCls: 'wt-icon-audio-call',
				disabled: !haveRTC || !me.hasFriendId(),
				handler: function() {
					WTA.RTCManager.startCall(me.getFriendId(),me.RTCJid);
				}
			},{
				xtype: 'button',
				tooltip: haveRTC?WT.res('wtimchat.btn-videocall.tip'):WT.res('rtc.unconfigured'),
				iconCls: 'wt-icon-video-call',
				disabled: !haveRTC || !me.hasFriendId(),
				handler: function() {
					WTA.RTCManager.startCall(me.getFriendId(),me.RTCJid,true);
				}
			},'-');
		}
		tbarItms.push({
			xtype: 'souploadbutton',
			tooltip: WT.res('wtimchat.btn-attach.tip'),
			iconCls: 'wt-icon-attach-xs',
			uploaderConfig: WTF.uploader(WT.ID, 'UploadWebChatFile', {
				extraParams: {
					chatId: me.chatId
				},
				dropElement: gptodayId,
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
					me.lref('card').getLayout().setActiveItem(me.lref('gptoday'));
					me.addMessage(json.data);
					if (WT.getVar('imSoundOnMessageSent')) {
						Sonicle.Sound.play('wt-im-sent');
					}
				},
				uploadprogress: function(s, file, percent) {
					me.wait(Ext.String.format('{0}: {1}%', file.name, percent), true);
					//me.waitUpdate(Ext.String.format('{0}: {1}%', file.name, percent));
				}
			}
		}, '-', {
			xtype: 'button',
			tooltip: WT.res('wtimchat.gpchatsearch.tit'),
			iconCls: 'wt-icon-search-xs',
			handler: function() {
				me.lref('gpchatsearch').toggleCollapse();
			}
		}, {
			xtype: 'splitbutton',
			tooltip: WT.res('wtimchat.btn-history.tip'),
			iconCls: 'wt-icon-history-xs',
			menu: {
				xtype: 'datemenu',
				pickerCfg: {
					xtype: 'soremotedatepicker',
					ajaxUrl: WTF.requestBaseUrl(),
					ajaxExtraParams: {
						service: WT.ID,
						action: 'ManageIMChat',
						crud: 'dates',
						chatId: me.chatId
					}
				},
				listeners: {
					beforeshow: function(s) {
						var date = me.getVM().get('chatDate');
						if (date && s.picker) s.picker.setValue(date);
					},
					select: function(s, date) {
						me.showDate(date);
					}
				}
			},
			handler: function() {
				me.showDate(new Date());
			}
		});
		
		Ext.apply(me, {
			tbar: tbarItms,
			bbar: {
				xtype: 'toolbar',
				ui: 'footer',
				items: [{
					xtype: 'button',
					ui: 'default-toolbar',
					glyph: 'xf118@FontAwesome',
					enableToggle: true,
					toggleHandler: function(s, state) {
						var cmp = me.lref('pnlemojis');
						if (state) {
							s.setGlyph('xf078@FontAwesome');
							cmp.expand();
						} else {
							s.setGlyph('xf118@FontAwesome');
							cmp.collapse();
						}
					}
				}, ' ', {
					xtype: 'textarea',
					reference: 'fldmessage',
					emptyText: WT.res('wtimchat.fld-message.emp'),
					grow: true,
					growMin: 40,
					enterIsSpecial: true,
					listeners: {
						specialkey: function(s, e) {
							if (!e.shiftKey && (e.getKey() === e.ENTER)) {
								e.stopEvent();
								me.sendMessage(s.getValue());
								s.setValue(null);
							}
						}
					},
					flex: 1
				}, ' ', {
					xtype: 'button',
					ui: 'default-toolbar',
					tooltip: WT.res('wtimchat.btn-send.tip'),
					glyph: 'xf1d9@FontAwesome',
					handler: function() {
						var fld = me.lref('fldmessage');
						me.sendMessage(fld.getValue());
						fld.setValue(null);
					}
				}, ' ']
			}
		});
		me.callParent(arguments);
		
		me.add([{
			region: 'center',
			xtype: 'container',
			layout: 'card',
			reference: 'card',
			activeItem: 0,
			items: [{
				xtype: 'grid',
				id: gptodayId,
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
				plugins: [{
					ptype: 'sofiledrop',
					text: WT.res('sofiledrop.text')
				}]
			}, {
				xtype: 'grid',
				reference: 'gphistory',
				border: false,
				rowLines: false,
				viewConfig: {
					markDirty: false,
					stripeRows: false,
					enableTextSelection: true,
					deferEmptyText: false,
					emptyText: WT.res('wtimchat.gphistory.emp')
				},
				store: {
					model: 'Sonicle.webtop.core.model.IMMessageGrid',
					proxy: WTF.apiProxy(WT.ID, 'ManageGridIMChatMessages', 'data', {
						extraParams: {
							chatId: me.chatId
						}
					}),
					listeners: {
						beforeload: function(s) {
							WTU.applyExtraParams(s, {
								date: Ext.Date.format(me.getVM().get('chatDate'), 'Y-m-d')
							});
						}
					}
				},
				columns: [{
					xtype: 'wtchatmessagecolumn',
					dataIndex: 'id',
					dateFormat: WT.getShortDateFmt(),
					flex: 1
				}]
			}]
		}, {
			region: 'east',
			xtype: 'gridpanel',
			reference: 'gpchatsearch',
			title: WT.res('wtimchat.gpchatsearch.tit'),
			border: true,
			collapsed: true,
			collapsible: true,
			store: {
				autoLoad: false,
				model: 'Sonicle.webtop.core.model.IMChatSearchGrid',
				proxy: WTF.apiProxy(WT.ID, 'ManageGridIMChatSearch', 'data', {
					extraParams: {
						chatId: me.chatId
					}
				}),
				groupField: 'date',
				groupDir: 'DESC',
				//sorters: ['date', 'timestamp'],
				listeners: {
					beforeload: function(s) {
						s.getProxy().abort();
					}
				}
			},
			columns: [{
				dataIndex: 'id',
				renderer: function(val, meta, rec, rIdx, colIdx, sto) {
					var html = '';
					html += Ext.String.htmlEncode(Ext.Date.format(rec.get('timestamp'), me.dateFormat + ' ' + me.timeFormat));
					html += '<br>';
					html += Ext.String.htmlEncode(rec.get('fromNick'));
					html += '<br>';
					html += '<span style="font-size:0.9em;color:grey;">';
					html += Ext.String.htmlEncode(rec.get('text'));
					html += '</span>';
					return html; 
				},
				flex: 1
			}],
			features: [{
				ftype: 'grouping',
				groupHeaderTpl: [
					'{groupValue:this.formatValue} ({children.length})',
					{
						format: me.dateFormat,
						formatValue: function(v) {
							return Ext.Date.format(v, this.format);
						}
					}
				]
			}],
			dockedItems: [{
				xtype: 'textfield',
				dock: 'top',
				reference: 'fldchatsearch',
				hideFieldLabel: true,
				emptyText: WT.res('textfield.search.emp'),
				triggers: {
					clear: {
						type: 'soclear'
					}
				},
				listeners: {
					change: {
						fn: function(s) {
							this.searchChat(s.getValue());
						},
						scope: me,
						options: {buffer: 300}
					}
				}
			}],
			listeners: {
				beforeexpand: function() {
					me.lref('pnlemojis').collapse();
				},
				expand: function() {
					me.lref('fldchatsearch').focus(true, 100);
				},
				rowdblclick: function(s, rec) {
					me.showDate(rec.get('date'), rec.get('id'));
				}
			},
			width: '40%'
		}, {
			region: 'south',
			xtype: 'soemojipicker',
			reference: 'pnlemojis',
			header: false,
			collapsed: true,
			collapsible: true,
			collapseMode: 'placeholder',
			placeholder: { xtype: 'component', width: 0},
			recentsText: WT.res('soemojipicker.recents.tip'),
			peopleText: WT.res('soemojipicker.people.tip'),
			natureText: WT.res('soemojipicker.nature.tip'),
			foodsText: WT.res('soemojipicker.foods.tip'),
			activityText: WT.res('soemojipicker.activity.tip'),
			placesText: WT.res('soemojipicker.places.tip'),
			objectsText: WT.res('soemojipicker.objects.tip'),
			symbolsText: WT.res('soemojipicker.symbols.tip'),
			flagsText: WT.res('soemojipicker.flags.tip'),
			listeners: {
				select: function(s, emoji) {
					var fld = me.lref('fldmessage');
					fld.setValue(fld.getValue()+emoji);
				}
			}
		}]);
		
		me.on('afterrender', me.onAfterrender, me, {single: true});
		me.on('activate', me.onActivate);
		if (!me.groupChat) me.refreshFriendPresence();
	},
	
	destroy: function() {
		var me = this;
		me.callParent();
		if (me.scrollTask) {
			me.scrollTask.cancel();
			me.scrollTask = null;
		}
	},
	
	getFriendId: function() {
		return WT.getApp().getService(WT.ID)
				.getVPController().getIMPanel()
				.lookupFriendByChatId(this.chatId);
	},
	
	hasFriendId: function() {
		return this.getFriendId()!=null;
	},
	
	getChatId: function() {
		return this.chatId;
	},
	
	showDate: function(date, messageId) {
		var me = this,
				lay = me.lref('card').getLayout(),
				focusMessage = function(grid, id) {
					var rec = grid.getStore().getById(id);
					if (rec) {
						grid.getView().focusRow(rec);
						grid.setSelection(rec);
					}
				},
				gp;
		
		me.getVM().set('chatDate', date);
		if (Sonicle.Date.isToday(date)) {
			gp = me.lref('gptoday');
			lay.setActiveItem(gp);
			if (messageId) focusMessage(gp, messageId);
		} else {
			gp = me.lref('gphistory');
			lay.setActiveItem(gp);
			gp.getStore().load({
				callback: function(recs, op, success) {
					if (success && messageId) focusMessage(gp, messageId);
				}
			});
		}
	},
	
	sendMessage: function(text) {
		var me = this;
		me.lref('card').getLayout().setActiveItem(me.lref('gptoday'));
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
					if (WT.getVar('imSoundOnMessageSent')) {
						Sonicle.Sound.play('wt-im-sent');
					}
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
	
	setFriendPresence: function(status) {
		var me = this,
				btn = me.lref('itmpresence');
		if (btn) {
			btn.setText(WT.res('im.pstatus.'+status));
			btn.setIconCls(WTF.cssIconCls(WT.XID, 'im-pstatus-'+status));
		}
	},
	
	setRTCJid: function(jid) {
		this.RTCJid=jid;
	},
	
	refreshFriendPresence: function() {
		var me = this;
		WT.ajaxReq(WT.ID, 'ManageIMChat', {
			params: {
				crud: 'presence',
				chatId: me.chatId
			},
			callback: function(success, json) {
				if (success) {
					me.setFriendPresence(json.data.presenceStatus);
					me.setRTCJid(json.data.friendFullId+"RTC");
				}
			}
		});
	},
	
	updateHotMarker: function(nv) {
		var me = this;
		me.setIconCls(me.self.buildIconCls(me.groupChat, nv));
	},
	
	searchChat: function(query) {
		var me = this,
				gp = me.lref('gpchatsearch');
		WTU.loadWithExtraParams(gp.getStore(), {query: query});
	},
	
	privates: {
		onAfterrender: function(s) {
			s.lref('fldmessage').focus(true, true);
		},

		onActivate: function(s) {
			if (s.scrollOnActivate) {
				s.scrollOnActivate = false;
				s.scrollToEnd();
			}
			s.setHotMarker(false);
			s.lref('fldmessage').focus(true, true);
		},
		
		addMessage: function(data) {
			var me = this,
				gp = me.lref('gptoday'),
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
			this.scrollViewToEnd(this.lref('gptoday').getView());
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
		}
	},
	
	statics: {
		buildIconCls: function(group, hot) {
			var ico = group ? 'im-gchat' : 'im-ichat';
			if (hot) ico += '-hot';
			return WTF.cssIconCls(WT.XID, ico, 'xs');
		}
	}
});
