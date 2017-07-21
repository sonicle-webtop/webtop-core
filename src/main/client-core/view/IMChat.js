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
Ext.define('Sonicle.webtop.core.view.IMChat', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.picker.Emoji',
		'Sonicle.webtop.core.ux.grid.column.ChatMessage',
		'Sonicle.webtop.core.model.IMMessageGrid'
	],
	
	dockableConfig: {
		iconCls: 'wt-icon-im-chat-xs',
		width: 420,
		height: 450
	},
	promptConfirm: false,
	
	myUserId: null,
	chatId: null,
	chatName: null,
	
	groupChat: false,
	
	viewModel: {
		data: {
			historyDate: null
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		cfg.myUserId = 'matteo@sonicle.com';
		if (Ext.isEmpty(cfg.chatId)) Ext.raise('Config `chatId` is mandatory.');
		if (Ext.isEmpty(cfg.chatName)) Ext.raise('Config `chatName` is mandatory.');
		me.groupChat = WTA.ux.IMPanel.isGroupChat(cfg.chatId);
		me.callParent([cfg]);
		me.setViewTitle(cfg.chatName);
	},
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			tbar: ['->', {
				xtype: 'button',
				tooltip: 'Allega',
				iconCls: 'wt-icon-im-attach-xs',
				handler: function() {
					
				}
			}, {
				xtype: 'button',
				tooltip: me.mys.res('imchat.btn-history.tip'),
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
			}],
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
				}/*, {
					xtype: 'button',
					ui: 'default-toolbar',
					glyph: 'xf118@FontAwesome',
					arrowVisible: false,
					menu: {
						xtype: 'soemojimenu',
						hideOnClick: false,
						pickerConfig: {
							recentsText: me.mys.res('soemojipicker.recents.tip'),
							peopleText: me.mys.res('soemojipicker.people.tip'),
							natureText: me.mys.res('soemojipicker.nature.tip'),
							foodsText: me.mys.res('soemojipicker.foods.tip'),
							activityText: me.mys.res('soemojipicker.activity.tip'),
							placesText: me.mys.res('soemojipicker.places.tip'),
							objectsText: me.mys.res('soemojipicker.objects.tip'),
							symbolsText: me.mys.res('soemojipicker.symbols.tip'),
							flagsText: me.mys.res('soemojipicker.flags.tip')
						},
						listeners: {
							select: function(s, emoji) {
								var fld = me.lref('fldmessage');
								fld.setValue(fld.getValue()+emoji);
							}
						}
					}
				}*/, ' ', {
					xtype: 'textarea',
					reference: 'fldmessage',
					emptyText: me.mys.res('imchat.fld-message.emp'),
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
					tooltip: me.mys.res('imchat.btn-send.tip'),
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
					proxy: WTF.apiProxy(WT.ID, 'ManageIMMessages', 'data', {
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
					emptyText: WT.res('imchat.gphistory.emp')
				},
				store: {
					model: 'Sonicle.webtop.core.model.IMMessageGrid',
					proxy: WTF.apiProxy(WT.ID, 'ManageIMMessages', 'data', {
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
			recentsText: me.mys.res('soemojipicker.recents.tip'),
			peopleText: me.mys.res('soemojipicker.people.tip'),
			natureText: me.mys.res('soemojipicker.nature.tip'),
			foodsText: me.mys.res('soemojipicker.foods.tip'),
			activityText: me.mys.res('soemojipicker.activity.tip'),
			placesText: me.mys.res('soemojipicker.places.tip'),
			objectsText: me.mys.res('soemojipicker.objects.tip'),
			symbolsText: me.mys.res('soemojipicker.symbols.tip'),
			flagsText: me.mys.res('soemojipicker.flags.tip'),
			listeners: {
				select: function(s, emoji) {
					var fld = me.lref('fldmessage');
					fld.setValue(fld.getValue()+emoji);
				}
			}
		});
		
		me.on('viewshow', me.onViewShow);
	},
	
	onViewShow: function(s) {
		this.lref('fldmessage').focus(true);
	},
	
	sendMessage: function(text) {
		var me = this;
		me.lref('card').getLayout().setActiveItem(me.lref('gplast'));
		if (Ext.isEmpty(text)) return;
		
		WT.ajaxReq(me.mys.ID, 'ManageIMChat', {
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
	
	privates: {
		addMessage: function(data) {
			var gp = this.lref('gplast'),
				sto = gp.getStore(),
				rec;
			rec = sto.add(sto.createModel(data))[0];
			gp.getView().focusRow(rec);
		},
		
		generateTitle: function() {
			var me = this;
			if (!me.groupChat) {
				return me.concatFriendNames();
			} else {
				return !Ext.isEmpty(me.chatName) ? me.chatName : me.concatFriendNames();
			}
		},
		
		concatFriendNames: function() {
			var arr = [];
			Ext.iterate(this.friends, function(fri) {
				arr.push(!Ext.isEmpty(fri.name) ? fri.name : fri.id);
			});
			return arr.join(', ');
		}
	}
});
