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
Ext.define('Sonicle.webtop.core.ux.IMChat', {
	alternateClassName: 'WTA.ux.IMChat',
	extend: 'Ext.panel.Panel',
	alias: ['widget.wtimchat'],
	requires: [
		'Sonicle.picker.Emoji',
		'Sonicle.webtop.core.ux.grid.column.ChatMessage',
		'Sonicle.webtop.core.model.IMMessageGrid'
	],
	mixins: [
		'WTA.mixin.PanelUtil'
	],
	
	config: {
		hotMarker: false
	},
	
	chatId: null,
	chatName: null,
	groupChat: false,
	
	layout: 'border',
	referenceHolder: true,
	viewModel: {
		data: {
			historyDate: null
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		if (Ext.isEmpty(cfg.chatId)) Ext.raise('Config `chatId` is mandatory.');
		if (Ext.isEmpty(cfg.chatName)) Ext.raise('Config `chatName` is mandatory.');
		me.groupChat = WTA.ux.IMPanel.isGroupChat(cfg.chatId);
		
		Ext.apply(cfg || {}, {
			title: cfg.chatName,
			iconCls: me.self.buildIconCls(me.groupChat, Ext.isBoolean(cfg.hotMarker) ? cfg.hotMarker : false)
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this, tbarItms = [];
		
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
		tbarItms.push('->', /*{
			xtype: 'button',
			tooltip: 'Allega',
			iconCls: 'wt-icon-im-attach-xs',
			handler: function() {

			}
		},*/ {
			xtype: 'button',
			tooltip: WT.res('wtimchat.btn-history.tip'),
			iconCls: 'wt-icon-im-history-xs',
			menu: {
				xtype: 'datemenu',
				listeners: {
					select: function(s, date) {
						var lay = me.lref('card').getLayout(), gp;
						me.getVM().set('historyDate', Ext.Date.format(date, 'Y-m-d'));
						if (Sonicle.Date.isToday(date)) {
							lay.setActiveItem(me.lref('gplast'));
						} else {
							gp = me.lref('gphistory');
							gp.getStore().load();
							lay.setActiveItem(gp);
						}
					}
				}
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
		me.add({
			region: 'center',
			xtype: 'container',
			layout: 'card',
			reference: 'card',
			activeItem: 0,
			items: [{
				xtype: 'grid',
				reference: 'gplast',
				border: false,
				rowLines: false,
				viewConfig: {
					markDirty: false,
					stripeRows: false
				},
				store: {
					autoLoad: true,
					model: 'Sonicle.webtop.core.model.IMMessageGrid',
					proxy: WTF.apiProxy(WT.ID, 'ManageGridIMMessages', 'data', {
						extraParams: {
							chatId: me.chatId
						}
					}),
					listeners: {
						load: function(s) {
							var rec = s.last();
							if (rec) me.lref('gplast').getView().focusRow(rec);
						}
					}
				},
				columns: [{
					xtype: 'wtchatmessagecolumn',
					dataIndex: 'id',
					dateFormat: WT.getShortDateFmt(),
					flex: 1
				}]
			}, {
				xtype: 'grid',
				reference: 'gphistory',
				border: false,
				rowLines: false,
				viewConfig: {
					markDirty: false,
					stripeRows: false,
					deferEmptyText: false,
					emptyText: WT.res('wtimchat.gphistory.emp')
				},
				store: {
					model: 'Sonicle.webtop.core.model.IMMessageGrid',
					proxy: WTF.apiProxy(WT.ID, 'ManageGridIMMessages', 'data', {
						extraParams: {
							chatId: me.chatId
						}
					}),
					listeners: {
						beforeload: function(s) {
							WTU.applyExtraParams(s, {
								date: me.getVM().get('historyDate')
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
			region: 'south',
			xtype: 'soemojipicker',
			reference: 'pnlemojis',
			header: false,
			collapsed: true,
			collapsible: true,
			collapseMode: 'placeholder',
			placeholder: {
				xtype: 'component',
				width: 0
			},
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
		});
		
		me.on('activate', me.onActivate);
		if (!me.groupChat) me.refreshFriendPresence();
	},
	
	getChatId: function() {
		return this.chatId;
	},
	
	sendMessage: function(text) {
		var me = this;
		me.lref('card').getLayout().setActiveItem(me.lref('gplast'));
		if (Ext.isEmpty(text)) return;
		
		WT.ajaxReq(WT.ID, 'ManageIMChat', {
			params: {
				crud: 'send',
				chatId: me.chatId,
				text: text
			},
			callback: function(success, json) {
				var fld = me.lref('fldmessage');
				if (success) {
					me.addMessage(json.data);
					fld.focus(true);
				} else {
					fld.setValue(text);
				}
			}
		});
	},
	
	newMessage: function(uid, fromId, fromNick, timestamp, action, text) {
		this.addMessage({
			id: uid,
			fromId: fromId,
			fromNick: fromNick,
			timestamp: timestamp,
			action: action,
			text: text,
			fromArchive: false
		});
	},
	
	setFriendPresence: function(status) {
		var me = this,
				btn = me.lref('itmpresence');
		if (btn) {
			btn.setText(WT.res('im.pstatus.'+status));
			btn.setIconCls(WTF.cssIconCls(WT.XID, 'im-pstatus-'+status, 'xs'));
		}
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
					me.setFriendPresence(json.data);
				}
			}
		});
	},
	
	updateHotMarker: function(nv) {
		var me = this;
		me.setIconCls(me.self.buildIconCls(me.groupChat, nv));
	},
	
	privates: {
		onActivate: function(s) {
			s.setHotMarker(false);
			s.lref('fldmessage').focus(true);
		},
		
		addMessage: function(data) {
			var gp = this.lref('gplast'),
				sto = gp.getStore(),
				rec;
			rec = sto.add(sto.createModel(data))[0];
			gp.getView().focusRow(rec);
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
