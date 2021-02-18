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
Ext.define('Sonicle.webtop.core.view.IMQuickChat', {
	extend: 'WTA.sdk.UIView',
	requires: [
		'WTA.ux.IMMinChat'
	],
	
	/**
	 * @cfg {String} chatId
	 */
	
	/**
	 * @cfg {String} chatName
	 */
	
	/**
	 * @cfg {Boolean} hotMarker
	 */
	
	/**
	 * @event unpinchat
	 * Fires when the unpin tool is clicked.
	 * @param {Sonicle.webtop.core.view.IMQuickChat} this
	 * @param {String} chatId
	 * @param {String} chatName
	 */
	
	dockableConfig: {
		width: 250,
		height: 300
	},
	
	constructor: function(cfg) {
		var me = this;
		Ext.merge(cfg, {
			dockableConfig: {
				title: cfg.chatName
			}
		});
		me.callParent([cfg]);
		me.config.dockableConfig = Ext.apply(me.config.dockableConfig, {
			tools: [{
				type: 'unpin',
				tooltip: me.res('wtimchat.too-unpin.tip'),
				callback: function() {
					me.fireEvent('unpinchat', me, me.chatId, me.chatName);
					me.closeView(false);
				}
			}]
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.applyHotMarker(null, me.hotMarker);
		
		me.add({
			region: 'center',
			xtype: 'wtimminchat',
			chatId: me.chatId,
			chatName: me.chatName,
			hotMarker: me.hotMarker,
			listeners: {
				send: me.onResetHotMarker,
				updatefriendpresence: me.onUpdateFriendPresence,
				scope: me
			}
		});
	},
	
	initCt: function(ct) {
		var me = this;
		me.callParent(arguments);
		if (ct.isXType('window')) {
			ct.on('focusenter', me.onCtFocusEnter, me);
		}
	},
	
	cleanupCt: function(ct) {
		var me = this;
		if (ct.isXType('window')) {
			ct.un('focusenter', me.onCtFocusEnter, me);
		}
		me.callParent(arguments);
	},
	
	setFriendPresence: function(friendFullId, status) {
		var cmp = this.getComponent(0);
		if (cmp) cmp.setFriendPresence(friendFullId, status);
	},
	
	setHotMarker: function(hot) {
		this.applyHotMarker(this.getComponent(0), hot);
	},
	
	newMessage: function(uid, fromId, fromNick, timestamp, action, text, data) {
		var cmp = this.getComponent(0);
		if (cmp) {
			cmp.newMessage(uid, fromId, fromNick, timestamp, action, text, data);
			this.applyHotMarker(cmp, true);
		}
	},
	
	privates: {
		onCtFocusEnter: function() {
			var cmp = this.getComponent(0);
			if (cmp) cmp.messageFld().focus(true, 200);
			this.applyHotMarker(cmp, false);
		},
		
		onResetHotMarker: function(s) {
			this.applyHotMarker(s, false);
		},
		
		onUpdateFriendPresence: function(s, fri, pstatus) {
			if (!s.isGroupChat) {
				Ext.defer(function() {
					this.setTitle(Ext.String.format('{0} ({1})', this.chatName, WTA.ux.IMStatusMenu.statusText(pstatus)));
				}, 200, this);
			}
		},
		
		applyHotMarker: function(cmp, hot) {
			var me = this;
			if (cmp) cmp.setHotMarker(hot);
			me.setIconCls(hot ? 'wt-icon-im-chat-hot' : null);
			if (!hot) me.mys.clearIMNewMsgNotification(me.chatId);
		}
	}
});
