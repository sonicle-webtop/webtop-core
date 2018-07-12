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
Ext.define('Sonicle.webtop.core.ux.IMPanel', {
	alternateClassName: 'WTA.ux.IMPanel',
	extend: 'Ext.panel.Panel',
	alias: ['widget.wtimpanel'],
	requires: [
		'Sonicle.form.trigger.Clear',
		'Sonicle.grid.column.Icon',
		'Sonicle.webtop.core.ux.IMStatusMenu',
		'Sonicle.webtop.core.model.IMFriendGrid',
		'Sonicle.webtop.core.model.IMChatGrid'
	],
	mixins: [
		'Sonicle.mixin.ActHolder',
		'Sonicle.mixin.RefHolder'
	],
	
	config: {
		/**
		 * @cfg {online|away|dnd|offline} presenceStatus
		 * The selected IM status.
		 */
		presenceStatus: null
	},
	
	/**
	 * @event presencestatusselect
	 */
	
	/**
	 * @event presencestatuschange
	 */
	
	/**
	 * @event addgroupchatclick
	 */
	
	/**
	 * @event frienddblclick
	 */
	
	/**
	 * @event chatdblclick
	 */
	
	layout: 'fit',
	header: false,
	split: false,
	collapsed: true,
	collapsible: true,
	collapseMode: 'placeholder',		
	referenceHolder: true,
	width: 200,
	border: true,
	
	constructor: function(cfg) {
		var me = this;
		Ext.apply(cfg || {}, {
			placeholder: {
				xtype: 'component',
				width: 0
			}
		});
		me.mixins.actholder.constructor.call(me, cfg);
		me.mixins.refholder.constructor.call(me, cfg);
		me.callParent([cfg]);
	},
	
	destroy: function() {
		var me = this;
		me.callParent();
		me.mixins.actholder.destroy.call(me);
		me.mixins.refholder.destroy.call(me);
	},
	
	initComponent: function() {
		var me = this;
		
		me.addAct('openChat', {
			text: WT.res('act-open.lbl'),
			handler: function() {
				var rec = me.gpChats().getSelection()[0];
				if (rec) me.fireChatDblClickFromRec(rec);
			}
		});
		me.addAct('openFriendChat', {
			text: WT.res('wtimpanel.act-openFriendChat.lbl'),
			handler: function() {
				var gp = me.gpFriends(), rec = gp.getSelection()[0];
				if (rec) me.onFriendDblClick(gp, rec);
			}
		});
		me.addAct('deleteChat', {
			text: WT.res('act-delete.lbl'),
			iconCls: WTF.cssIconCls(WT.XID, 'remove', 'xs'),
			handler: function() {
				var rec = me.gpChats().getSelection()[0];
				if (rec) me.removeChatFromRec(rec);
			}
		});
		me.addAct('addGroupChat', {
			text: WT.res('wtimpanel.act-addGroupChat.lbl'),
			//iconCls: WTF.cssIconCls(WT.XID, 'remove', 'xs'),
			handler: function() {
				me.fireEvent('addgroupchatclick', me);
			}
		});
		
		Ext.apply(me, {
			tbar: [{
				xtype: 'tbtext',
				html: WT.res('wtimpanel.mni-status.txt')
			}, {
				reference: 'btnstatus',
				text: WTA.ux.IMStatusMenu.statusText(me.presenceStatus),
				tooltip: WT.res('wtimpanel.btn-status.tip'),
				iconCls: WTA.ux.IMStatusMenu.statusIconCls(me.presenceStatus),
				menu: {
					xtype: 'wtimstatusmenu',
					presenceStatus: me.getPresenceStatus(),
					listeners: {
						presencestatusselect: me.onBtnStatusPresenseSelect,
						scope: me
					}
				}
			}]
		});
		
		me.callParent(arguments);
		
		me.add([{
			xtype: 'tabpanel',
			reference: 'tab',
			activeTab: 'chats',
			border: false,
			items: [{
				xtype: 'gridpanel',
				itemId: 'chats',
				reference: 'gpchats',
				title: WT.res('wtimpanel.chats.tit'),
				border: false,
				viewConfig: {
					markDirty: false,
					deferEmptyText: false,
					emptyText: WT.res('wtimpanel.gpchats.emp')
				},
				store: {
					autoLoad: true,
					autoSync: true,
					model: 'Sonicle.webtop.core.model.IMChatGrid',
					proxy: WTF.apiProxy(WT.ID, 'ManageGridIMChats'),
					sorters: ['name'],
					listeners: {
						load: function(s) {
							if (s.getCount() === 0) me.lookupReference('tab').setActiveTab('friends');
						}
					}
				},
				columns: [{
					xtype: 'soiconcolumn',
					dataIndex: 'id',
					getIconCls: function (v) {
						return WTF.cssIconCls(WT.XID, me.self.isGroupChat(v) ? 'im-gchat' : 'im-ichat', 'xs');
					},
					getTip: function(v) {
						if (me.self.isGroupChat(v)) {
							return WT.res('wtimpanel.gpchats.type.group');
						} else {
							return WT.res('wtimpanel.gpchats.type.instant');
						}
					},
					iconSize: WTU.imgSizeToPx('xs'),
					header: '',
					width: 30
				}, {
					dataIndex: 'name',
					flex: 1
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'hot',
					getIconCls: function(v,rec) {
						return v ? WTF.cssIconCls(WT.XID, 'im-chat-hot', 'xs') : null;
					},
					getTip: function(v,rec) {
						return v ? WT.res(WT.ID, 'wtimpanel.gpchats.hot.true') : null;
					},
					iconSize: WTU.imgSizeToPx('xs'),
					header: '',
					width: 30
				}],
				dockedItems: [{
					xtype: 'textfield',
					dock: 'top',
					hideFieldLabel: true,
					emptyText: WT.res('wtimpanel.gpchats.search.emp'),
					triggers: {
						clear: {
							type: 'soclear'
						}
					},
					listeners: {
						change: {
							fn: me.onChatsSearchChange,
							scope: me,
							options: {buffer: 300}
						}
					}
				}],
				listeners: {
					rowdblclick: function(s, rec) {
						me.fireChatDblClickFromRec(rec);
					},
					rowcontextmenu: function(s, rec, el, rowIdx, e) {
						e.stopEvent();
						me.cxmChat().showAt(e.getXY());
					}
				}
			}, {
				xtype: 'gridpanel',
				itemId: 'friends',
				reference: 'gpfriends',
				title: WT.res('wtimpanel.friends.tit'),
				border: false,
				viewConfig: {
					markDirty: false
				},
				store: {
					model: 'Sonicle.webtop.core.model.IMFriendGrid',
					proxy: WTF.apiProxy(WT.ID, 'ManageGridIMFriends'),
					groupField: 'online',
					groupDir: 'ASC',
					sorters: ['nick']
				},
				columns: [{
					xtype: 'soiconcolumn',
					dataIndex: 'presenceStatus',
					sortable: false,
					menuDisabled: true,
					stopSelection: true,
					getIconCls: function (v, rec) {
						return WTF.cssIconCls(WT.XID, 'im-pstatus-'+v);
					},
					getTip: function(v, rec) {
						return WT.res('im.pstatus.'+v);
					},
					width: 30
				}, {
					dataIndex: 'nick',
					renderer: function(val, meta, rec, rIdx, colIdx, sto) {
						var html = '', sta = rec.get('status');
						html += Ext.String.htmlEncode(val);
						html += '<br>';
						html += '<span style="font-size:0.9em;color:grey;">' + (Ext.isEmpty(sta) ? '&nbsp;' : Ext.String.htmlEncode(sta)) + '</span>';
						return html; 
					},
					flex: 1
				}],
				features: [{
					ftype: 'grouping',
					startCollapsed: true,
					groupHeaderTpl: [
						'{groupValue:this.formatValue} ({children.length})',
						{
							formatValue: function(v) {
								return WT.res('wtimpanel.gpfriends.group.' + ((v === 1) ? 'online' : 'offline'));
							}
						}
					]
				}],
				dockedItems: [{
					xtype: 'textfield',
					dock: 'top',
					hideFieldLabel: true,
					emptyText: WT.res('wtimpanel.gpfriends.search.emp'),
					triggers: {
						clear: {
							type: 'soclear'
						}
					},
					listeners: {
						change: {
							fn: me.onFriendsSearchChange,
							scope: me,
							options: {buffer: 300}
						}
					}
				}],
				listeners: {
					afterrender: function(s) {
						Ext.defer(me.expandOnlineGroup, 200, me, [me.gpFriends()]);
					},
					rowdblclick: function(s, rec) {
						me.onFriendDblClick(s, rec);
					},
					rowcontextmenu: function(s, rec, el, rowIdx, e) {
						e.stopEvent();
						me.cxmFriend().showAt(e.getXY());
					}
				}
			}]
		}]);
	},
	
	updatePresenceStatus: function(nv, ov) {
		var me = this;
		if (!me.isConfiguring) {
			var Menu = WTA.ux.IMStatusMenu,
					btn = me.btnStatus();
			btn.setText(Menu.statusText(nv));
			btn.setIconCls(Menu.statusIconCls(nv));
			btn.menu.setPresenceStatus(nv);
			me.fireEvent('presencestatuschange', me, nv, ov);
		}
	},
	
	cxmChat: function() {
		var me = this;
		return me.getRef('cxmChat') || me.addRef('cxmChat', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('openChat'),
				me.getAct('deleteChat'),
				'-',
				me.getAct('addGroupChat')
			]
		}));
	},
	
	cxmFriend: function() {
		var me = this;
		return me.getRef('cxmFriend') || me.addRef('cxmFriend', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('openFriendChat')
			]
		}));
	},
	
	loadChats: function() {
		this.gpChats().getStore().load();
	},
	
	updateChatName: function(id, name) {
		var sto = this.gpChats().getStore(),
				rec = sto.getById(id);
		if (rec) rec.set('name', name);
	},
	
	updateChatHotMarker: function(id, visible) {
		var sto = this.gpChats().getStore(),
				rec = sto.getById(id);
		if (rec) rec.set('hot', visible);
	},
	
	searchChat: function(query) {
		var sto = this.lookupReference('gpchats').getStore(),
				filters = sto.getFilters();

		if (query) {
			filters.beginUpdate();
			if (filters.getCount() > 0) {
				filters.each(function(fil) {
					fil.setValue(query);
				});
			} else {
				Ext.iterate(['name'], function(field) {
					filters.add(new Ext.util.Filter({
						id: 'search-'+field,
						anyMatch: true,
						caseSensitive: false,
						property: field,
						value: query
					}));
				});
			}
			filters.endUpdate();
		} else {
			sto.clearFilter();
		}
	},
	
	loadFriends: function() {
		this.gpFriends().getStore().load();
	},
	
	updateFriendPresence: function(id, status, message) {
		var me = this,
				sto = me.gpFriends().getStore(),
				rec = sto.getById(id), changed;
		if (rec) {
			changed = rec.get('presenceStatus') !== status;
			rec.set({
				presenceStatus: status,
				statusMessage: message
			});
			
			if (changed && !me.self.isStatusDnD(me.btnStatus().menu.getPresenceStatus())) {
				if (WT.getVar('imSoundOnFriendConnect') && me.self.isStatusOnline(status, true)) {
					Sonicle.Sound.play('wt-im-connect');
				} else if (WT.getVar('imSoundOnFriendDisconnect') && !me.self.isStatusOffline(status)) {
					Sonicle.Sound.play('wt-im-disconnect');
				}
			}
		}
		me.expandOnlineGroup(me.gpFriends());
	},
	
	searchFriend: function(query) {
		var me = this,
				sto = me.gpFriends().getStore(),
				filters = sto.getFilters();

		if (query) {
			filters.beginUpdate();
			if (filters.getCount() > 0) {
				filters.each(function(fil) {
					fil.setValue(query);
				});
			} else {
				Ext.iterate(['nick'], function(field) {
					filters.add(new Ext.util.Filter({
						id: 'search-'+field,
						anyMatch: true,
						caseSensitive: false,
						property: field,
						value: query
					}));
				});
			}
			filters.endUpdate();
		} else {
			sto.clearFilter();
		}
	},
	
	lookupFriendByChatId: function(chatId) {
		var me=this,
			sto=me.gpFriends().getStore(),
			rec=sto.findRecord('dChatId',chatId);
		
		if (rec && rec.get('presenceStatus')==='online')
			return rec.get('id');
		
		return null;
	},
	
	toggleCollapse: function() {
		this.floatCollapsedPanel();
	},
	
	expand: function(animate) {
		return this;
	},
	
	collapse: function(animate) {
		return this;
	},
	
	privates: {
		btnStatus: function() {
			return this.lookupReference('btnstatus');
		},

		gpChats: function() {
			return this.lookupReference('gpchats');
		},

		gpFriends: function() {
			return this.lookupReference('gpfriends');
		},
		
		onBtnStatusPresenseSelect: function(s, status) {
			var me = this;
			me.fireEvent('presencestatusselect', me, status);
		},
		
		onFriendDblClick: function(s, rec) {
			var me = this,
					chatId = rec.get('dChatId'),
					rec2 = me.gpChats().getStore().getById(chatId);
			if (rec2) {
				me.fireChatDblClickFromRec(rec2);
			} else {
				me.fireEvent('frienddblclick', me, rec.get('id'), rec.get('nick'), rec.get('dChatId'));
			}
		},
		
		onFriendsSearchChange: function(s) {
			this.searchFriend(s.getValue());
		},
		
		onChatsSearchChange: function(s) {
			this.searchChat(s.getValue());
		},
		
		fireChatDblClickFromRec: function(rec) {
			rec.set('hot', false);
			this.fireEvent('chatdblclick', this, rec.get('id'), rec.get('name'));
		},
		
		removeChatFromRec: function(rec) {
			WT.confirm(WT.res('wtimpanel.confirm.chat.delete'), function(bid) {
				if (bid === 'yes') rec.drop();
			}, this);
		},
		
		expandOnlineGroup: function(grid) {
			var view = grid.getView(),
					feat = grid.getView().findFeature('grouping');
			if (view.rendered && feat.getGroup('1')) {
				feat.expand('1', {highlight: true});
			}
		}
	},
	
	statics: {
		isStatusOnline: function(pstatus, strict) {
			if (strict === undefined) strict = false;
			return strict ? (pstatus === 'online') : (pstatus !== 'offline');
		},
		
		isStatusDnD: function(pstatus) {
			return pstatus === 'dnd';
		},
		
		isStatusOffline: function(pstatus) {
			return pstatus === 'offline';
		},

		isGroupChat: function(chatId) {
			return chatId.indexOf('@instant.') === -1;
		}
	}
});
