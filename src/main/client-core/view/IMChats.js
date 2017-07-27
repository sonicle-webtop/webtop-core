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
	requires: [
		'Sonicle.webtop.core.ux.IMChat'
	],
	
	dockableConfig: {
		iconCls: 'wt-icon-im-chat-xs',
		width: 450,
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
			items: [],
			listeners: {
				remove: me.onTabChatsRemove,
				tabchange: me.onTabChatsTabChange,
				scope: me
			}
		});
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
	
	newChat: function(chatId, chatName) {
		this.addChat(chatId, chatName, false);
	},
	
	newChatMessage: function(chatId, chatName, fromId, fromNick, msgTimestamp, msgAction, msgUid, msgText) {
		var me = this,
				map = me.chatMap,
				pnl = map[chatId];
		if (!pnl) {
			me.addChat(chatId, chatName, true);
		} else {
			pnl.newMessage(msgUid, fromId, fromNick, msgTimestamp, msgAction, msgText);
			if (!me.isChatActive(chatId)) pnl.setHotMarker(true);
		}
	},
	
	setChatFriendPresence: function(chatId, status) {
		var me = this,
				map = me.chatMap,
				pnl = map[chatId];
		if (pnl) pnl.setFriendPresence(status);
	},
	
	privates: {
		tabChats: function() {
			return this.lookupReference('tabchats');
		},
		
		onTabChatsRemove: function(s, tab) {
			var me = this;
			delete me.chatMap[tab.getChatId()];
			if (me.tabChats().items.getCount() === 0) {
				me.closeView(false);
			}
		},

		onTabChatsTabChange: function(s, ntab) {
			var me = this;
			me.setViewTitle(ntab.getTitle());
			me.mys.clearChatNotification(ntab.getChatId());
		},
		
		addChat: function(chatId, chatName, hotMarker) {
			var me = this,
					tab = me.tabChats(),
					map = me.chatMap;
			if (!map[chatId]) {
				map[chatId] = tab.add({
					xtype: 'wtimchat',
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
