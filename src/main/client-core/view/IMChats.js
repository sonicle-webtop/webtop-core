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
Ext.define('Sonicle.webtop.core.view.IMChats', {
	extend: 'WTA.sdk.DockableView',
	uses: [
		'WTA.ux.IMBigChat'
	],
	
	dockableConfig: {
		iconCls: 'wt-icon-im-chat-xs',
		width: 550,
		height: 500
	},
	promptConfirm: false,
	
	chatMap: null,
	
	constructor: function(cfg) {
		this.chatMap = {};
		this.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'tabpanel',
			reference: 'tabchats',
			border: false,
			items: [],
			listeners: {
				remove: me.onTabChatsRemove,
				tabchange: me.onTabChatsTabChange,
				scope: me
			}
		});
	},
	
	initCt: function(ct) {
		var me = this;
		me.callParent(arguments);
		if (ct.isXType('window')) {
			ct.on('focus', me.onCtFocus, me);
		}
	},
	
	cleanupCt: function(ct) {
		var me = this;
		if (ct.isXType('window')) {
			ct.un('focus', me.onCtFocus, me);
		}
		me.callParent(arguments);
	},
	
	hasChat: function(chatId) {
		var map = this.chatMap;
		return map.hasOwnProperty(chatId) && map[chatId] !== undefined;
	},
	
	isChatActive: function(chatId) {
		var me = this,
				tab = me.tabChats().getActiveTab();
		return tab ? (tab.getChatId() === chatId) : false;
	},
	
	openChat: function(chatId, chatName) {
		var me = this,
				pnl = me.addChat(chatId, chatName, false);
		me.tabChats().setActiveTab(pnl);
	},
	
	newChatMessage: function(chatId, chatName, fromId, fromNick, timestamp, uid, action, text, data) {
		var me = this,
				pnl = me.chatMap[chatId];
		if (!pnl) {
			me.addChat(chatId, chatName, true);
		} else {
			pnl.newMessage(uid, fromId, fromNick, timestamp, action, text, data);
			pnl.setHotMarker(true);
		}
	},
	
	setChatFriendPresence: function(chatId, friendFullId, status) {
		var me = this,
				pnl = me.chatMap[chatId];
		if (pnl) pnl.setFriendPresence(friendFullId, status);
	},
	
	privates: {
		onCtFocus: function() {
			var me = this,
					tab = me.tabChats().getActiveTab();
			// When Chats view is already open and a new chat is pinned into,
			// this foucus event steal focus from messageField.
			// We need to force focus again!
			if (tab) tab.messageFld().focus(true, 200);
		},
		
		onTabChatsRemove: function(s, tab) {
			var me = this, ntab;
			delete me.chatMap[tab.getChatId()];
			if (s.items.getCount() === 0) {
				me.closeView(false);
			} else {
				// Also in this case focus is stolen, remove event is fired after
				// the change one and seems that this behaviour causes focus loss.
				// We need to force focus again!
				ntab = s.getActiveTab();
				if (ntab) ntab.messageFld().focus(true, 200);
			}
		},

		onTabChatsTabChange: function(s, ntab) {
			var me = this;
			me.setViewTitle(ntab.getTitle());
			ntab.setHotMarker(false);
			ntab.messageFld().focus(true, 200);
			me.mys.clearIMNewMsgNotification(ntab.getChatId());
		},
		
		tabChats: function() {
			return this.lookupReference('tabchats');
		},
		
		addChat: function(chatId, chatName, hotMarker) {
			var me = this,
					tab = me.tabChats(),
					map = me.chatMap;
			if (!map[chatId]) {
				map[chatId] = tab.add({
					xtype: 'wtimbigchat',
					closable: true,
					chatId: chatId,
					chatName: chatName,
					hotMarker: hotMarker
				});
			}
			return map[chatId];
		}
	}
});
