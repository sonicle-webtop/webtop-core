/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
/**
 * NethVoice messages: the NethVoice chat conversations, as the CTI's Messages page.
 * The data comes from the chat-island the core mounts (WTA.ux.NethVoiceChat):
 * a row opens the conversation in the island, the menu deletes it or leaves the group.
 */
Ext.define('Sonicle.webtop.core.messages.Service', {
	extend: 'WTA.sdk.Service',
	requires: [
		'WTA.ux.field.Search',
		'Sonicle.webtop.core.messages.model.ServiceVars'
	],
	
	query: '',
	kind: 'all', // the filter picked in the tool panel: all, unread, chat, group, external
	found: null, // peer -> {body, count}: archive hits for the current query
	
	init: function() {
		var me = this;
		me.found = {};
		// The orange New button of the tool header (and an item in every New menu).
		me.addNewAction('newChat', {
			iconCls: 'wtmsg-icon-newChat',
			handler: function() {
				var chat = me.getChat();
				if (chat) chat.newChat();
			}
		});
		me.setToolComponent(Ext.create({
			xtype: 'panel',
			layout: 'fit',
			title: me.getName(),
			items: [{
				xtype: 'grid',
				reference: 'gpfilters',
				cls: 'wtmsg-filters',
				hideHeaders: true,
				rowLines: false,
				border: false,
				store: {
					fields: ['id', 'text', 'icon', 'count'],
					// Icons as the Mail folder tree's (Font Awesome solid)
					data: [
						{id: 'all', text: me.res('filter.all'), icon: 'fa-comments', count: 0},
						{id: 'unread', text: me.res('filter.unread'), icon: 'fa-comment-dots', count: 0},
						{id: 'chat', text: me.res('filter.chat'), icon: 'fa-user', count: 0},
						{id: 'group', text: me.res('filter.group'), icon: 'fa-users', count: 0},
						// The chats with people outside the PBX: the ones with the External badge
						{id: 'external', text: me.res('filter.external'), icon: 'fa-globe', count: 0}
					]
				},
				columns: [{
					dataIndex: 'text',
					flex: 1,
					renderer: function(v, meta, rec) {
						return '<i class="fas ' + rec.get('icon') + ' wtmsg-filter-ico"></i>' + Ext.String.htmlEncode(v) + (rec.get('count') ? '<span class="wtmsg-filter-count">' + rec.get('count') + '</span>' : '');
					}
				}],
				listeners: {
					viewready: function(g) {
						g.getSelectionModel().select(0);
					},
					select: function(sm, rec) {
						me.kind = rec.get('id');
						me.applyFilter();
					}
				}
			}]
		}));
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,
			items: [
				'->',
				{
					xtype: 'wtsearchfield',
					reference: 'fldsearch',
					emptyText: me.res('search.emp'),
					favoritesButton: false,
					// Plain text search: no advanced fields, no suggestions, so no picker to open
					// (opening it would clear the text and leave the list filtered).
					triggers: {
						picker: {
							hidden: true
						}
					},
					onTriggerClick: Ext.emptyFn,
					onInputElClick: Ext.emptyFn,
					listeners: {
						query: function(s, value) {
							me.onQuery(value);
						},
						change: {
							fn: function(s, value) {
								me.onQuery(value);
							},
							buffer: 300
						}
					}
				},
				'->'
			]
		}));
		me.setMainComponent(Ext.create({
			xtype: 'grid',
			reference: 'gpconversations',
			cls: 'wtmsg-grid',
			border: false,
			store: {
				fields: ['peer', 'kind', 'name', 'avatar', 'online', 'mobile', 'inactive', 'owner', 'unread', 'members', 'last', 'muted', 'guests', 'locked', 'presence'],
				data: []
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: me.res('gpconversations.emp')
			},
			columns: [{
				text: me.res('gpconversations.conversation.lbl'),
				dataIndex: 'name',
				flex: 2,
				renderer: function(v, meta, rec) {
					return me.renderConversation(rec);
				}
			}, {
				text: me.res('gpconversations.last.lbl'),
				dataIndex: 'last',
				flex: 3,
				renderer: function(v, meta, rec) {
					return me.renderLast(rec);
				}
			}, {
				text: me.res('gpconversations.date.lbl'),
				dataIndex: 'last',
				width: 280,
				renderer: function(v) {
					return (v && v.ts) ? '<span class="wtmsg-when">' + Ext.String.htmlEncode(me.formatWhen(v.ts)) + '</span>' : '';
				}
			}, {
				// As the CTI: an "Open chat" call to action, the other actions in a kebab menu.
				width: 170,
				align: 'right',
				sortable: false,
				menuDisabled: true,
				renderer: function() {
					return '<span class="wtmsg-cta"><i class="fas fa-comment-alt"></i>' + Ext.String.htmlEncode(me.res('act-openChat.lbl')) + '</span>'
						+ '<span class="wtmsg-more fas fa-ellipsis-v"></span>';
				}
			}],
			listeners: {
				rowclick: function(g, rec, el, ri, e) {
					if (e.getTarget('.wtmsg-more')) me.showMoreMenu(rec, e);
					else me.openChat(rec);
				}
			}
		}));
		// The chat is mounted by the viewport, which exists before the services.
		me.bindChat();
		me.on('activate', me.labelNewButton, me);
	},
	
	getChat: function() {
		var vp = WT.getApp().viewport;
		return (vp && vp.getNVChat) ? vp.getNVChat() : null;
	},
	
	privates: {
		bindChat: function() {
			var me = this, chat = me.getChat();
			if (!chat) {
				Ext.defer(me.bindChat, 500, me);
				return;
			}
			chat.on('conversations', me.onConversations, me);
			chat.on('searchresults', me.onSearchResults, me);
			chat.on('unread', me.onUnread, me);
			me.onUnread(chat, chat.unread);
			chat.requestConversations();
		},
		
		/**
		 * The nethesis LAF gives each tool panel its own New button: here it reads "New", as in the other services.
		 * (With a single, shared New button nothing is found in the tool panel and nothing changes.)
		 */
		labelNewButton: function() {
			var tool = this.getToolComponent(),
				hd = tool ? tool.getDockedItems()[0] : null,
				btn = (hd && hd.lookupReference) ? hd.lookupReference('newbtn') : null,
				toolbox = (hd && hd.lookupReference) ? hd.lookupReference('toolboxbtn') : null;
			if (btn) btn.setText(WT.res('act-new.lbl'));
			// The toolbox menu has no items here.
			if (toolbox) toolbox.setHidden(true);
		},
		
		showMoreMenu: function(rec, e) {
			var me = this,
				lbl = me.isOwnedGroup(rec) ? 'act-destroyGroup.lbl' : (rec.get('kind') === 'group' ? 'act-leaveGroup.lbl' : 'act-deleteChat.lbl');
			Ext.create('Ext.menu.Menu', {
				items: [{text: me.res(lbl), iconCls: 'wt-icon-trash', handler: function() { me.confirmDelete(rec); }}],
				listeners: {hide: function(m) { Ext.defer(m.destroy, 10, m); }}
			}).showBy(e.getTarget('.wtmsg-more'), 'tr-br?');
		},
		
		gp: function() {
			return this.getMainComponent();
		},
		
		onConversations: function(chat, conversations) {
			var me = this, sto = me.gp().getStore(), list = conversations || [],
				counts = {all: list.length, unread: 0, chat: 0, group: 0, external: 0},
				fsto = me.getToolComponent().down('grid').getStore();
			Ext.each(list, function(c) {
				if (c.unread) counts.unread++;
				if (c.guests) counts.external++;
				counts[c.kind === 'group' ? 'group' : 'chat']++;
			});
			fsto.each(function(rec) {
				rec.set('count', rec.get('id') === 'all' ? 0 : counts[rec.get('id')], {commit: true});
			});
			sto.loadData(list);
			me.applyFilter();
		},
		
		onSearchResults: function(chat, query, results) {
			var me = this, found = {};
			if (query !== me.query) return;
			Ext.each(results, function(h) {
				if (found[h.peer]) {
					found[h.peer].count++;
				} else {
					found[h.peer] = {body: h.body, count: 1, id: h.id}; // newest first: the chat opens at this one
				}
			});
			me.found = found;
			me.applyFilter();
		},
		
		onUnread: function(chat, total) {
			var btn = WT.getApp().viewport.getServiceButton(this.ID), el, badge;
			if (!btn || !btn.rendered) return;
			el = btn.getEl();
			badge = el.down('.wtmsg-badge');
			if (!badge) badge = el.createChild({tag: 'span', cls: 'wtmsg-badge'});
			badge.setHtml(total > 99 ? '99+' : String(total || ''));
			badge.setVisible(total > 0);
		},
		
		onQuery: function(value) {
			var me = this, chat = me.getChat(), q = Ext.String.trim(value || '');
			if (q === me.query && q !== '') return;
			me.query = q;
			me.found = {};
			me.applyFilter();
			// Two characters at least: the whole archive too.
			if (chat && q.length >= 2) chat.search(q);
		},
		
		applyFilter: function() {
			var me = this, sto = me.gp().getStore(), lq = me.query.toLowerCase(), kind = me.kind;
			sto.clearFilter(true);
			if (lq || kind !== 'all') {
				sto.filterBy(function(rec) {
					var last = rec.get('last');
					if (kind === 'unread' && !rec.get('unread')) return false;
					if (kind === 'group' && rec.get('kind') !== 'group') return false;
					if (kind === 'chat' && rec.get('kind') === 'group') return false;
					if (kind === 'external' && !rec.get('guests')) return false;
					return !lq
						|| String(rec.get('name') || '').toLowerCase().indexOf(lq) !== -1
						|| String(rec.get('peer') || '').indexOf(lq) !== -1
						|| (last && String(last.body || '').toLowerCase().indexOf(lq) !== -1)
						|| !!me.found[rec.get('peer')];
				});
			}
			me.gp().getView().refresh();
			me.highlight();
		},
		
		highlight: function() {
			var fld = this.getToolbar().lookupReference('fldsearch'), gp = this.gp();
			if (fld && fld.highlight && gp.rendered) fld.highlight(gp.getEl(), '.x-grid-item-container');
		},
		
		openChat: function(rec) {
			var me = this, chat = me.getChat(), hit = rec ? me.found[rec.get('peer')] : null;
			// As the CTI: from a search, the chat opens with it filled in, at the message found
			if (chat && rec) chat.openChat(rec.get('peer'), hit && me.query.length >= 2 ? {query: me.query, id: hit.id} : undefined);
		},
		
		isOwnedGroup: function(rec) {
			return rec.get('kind') === 'group' && rec.get('owner') === true;
		},
		
		confirmDelete: function(rec) {
			var me = this,
				key = me.isOwnedGroup(rec) ? 'confirm.destroyGroup' : (rec.get('kind') === 'group' ? 'confirm.leaveGroup' : 'confirm.deleteChat');
			WT.confirm(Ext.String.format(me.res(key), Ext.String.htmlEncode(rec.get('name'))), function(bid) {
				var chat = me.getChat();
				if (bid === 'yes' && chat) chat.deleteChat(rec.get('peer'));
			}, me);
		},
		
		renderConversation: function(rec) {
			var me = this,
				enc = Ext.String.htmlEncode,
				name = rec.get('name') || rec.get('peer'),
				group = rec.get('kind') === 'group',
				avatar = rec.get('avatar'),
				queue = group && String(name).indexOf('Queue - ') === 0,
				words = Ext.Array.filter(String(queue ? name.slice(8) : name).split(/[^0-9A-Za-z\u00C0-\u024F]+/), function(w) { return !!w; }),
				// As the CTI: a group of one word takes its first two letters, otherwise the first letter of two words
				initials = (group && words.length === 1 ? words[0].slice(0, 2) : Ext.Array.map(words.slice(0, 2), function(w) { return w.charAt(0); }).join('')).toUpperCase(),
				count = (rec.get('members') || []).length,
				sub = group ? Ext.String.format(me.res(count === 1 ? 'gpconversations.member' : 'gpconversations.members'), count)
					: (rec.get('inactive') ? me.res('gpconversations.inactive') : '@' + rec.get('peer')),
				// As the island: a colleague without a picture gets the CTI's headset; groups blue (CTI operator group), indigo (queue) or gray
				look = !group ? ' wtmsg-avatar-headset' : (String(rec.get('peer')).indexOf('cti-') === 0 ? ' wtmsg-avatar-blue' : (queue ? ' wtmsg-avatar-indigo' : '')),
				pic = avatar ? '<img class="wtmsg-avatar" src="' + enc(avatar) + '"/>' : '<span class="wtmsg-avatar wtmsg-avatar-initials' + look + '">' + (group ? enc(initials) : '') + '</span>',
				// As the island's dot: DND black, busy or ringing red, green when on the chat, on the mobile app or online for the CTI
				presence = rec.get('presence'),
				state = presence === 'dnd' ? 'wtmsg-presence-dnd' : (Ext.Array.contains(['busy', 'ringing', 'incoming'], presence) ? 'wtmsg-presence-busy'
					: (rec.get('online') || rec.get('mobile') || presence === 'online' ? 'wtmsg-presence-on' : '')),
				dot = group ? '' : '<span class="wtmsg-presence ' + state + '"></span>',
				// As the CTI: the island's External badge, with a lock once its link no longer lets anyone in; then the muted bell
				tags = (rec.get('guests') ? '<span class="wtmsg-tag">' + (rec.get('locked') ? '<i class="fas fa-lock"></i>' : '') + enc(me.res('gpconversations.external')) + '</span>' : '')
					+ (rec.get('muted') ? '<i class="fas fa-bell-slash wtmsg-muted" title="' + enc(me.res('gpconversations.muted')) + '"></i>' : '');
			return '<div class="wtmsg-conv"><span class="wtmsg-pic">' + pic + dot + '</span>'
				+ '<span class="wtmsg-names"><span class="wtmsg-name' + (rec.get('unread') ? ' wtmsg-name-unread' : '') + '">' + enc(name) + '</span>' + tags
				+ '<br/><span class="wtmsg-sub">' + enc(sub) + '</span></span></div>';
		},
		
		/**
		 * As the CTI: only the part matching the search highlighted, the rest encoded as it is.
		 */
		mark: function(text) {
			var enc = Ext.String.htmlEncode, q = this.query || '';
			if (q.length < 2) return enc(text);
			return Ext.Array.map(String(text).split(new RegExp('(' + Ext.String.escapeRegex(q) + ')', 'gi')), function(p, i) {
				return i % 2 ? '<span class="wtmsg-hit">' + enc(p) + '</span>' : enc(p);
			}).join('');
		},
		
		renderLast: function(rec) {
			var me = this, enc = Ext.String.htmlEncode,
				hit = me.found[rec.get('peer')],
				last = rec.get('last'), who = '', text;
			if (hit) return '<span class="wtmsg-last">' + me.mark(hit.body) + '</span>' + (hit.count > 1 ? ' <span class="wtmsg-sub">+' + (hit.count - 1) + '</span>' : '');
			if (!last) return '';
			if (last.mine) {
				who = me.res('gpconversations.you') + ': ';
			} else if (rec.get('kind') === 'group' && last.nick) {
				who = last.nick + ': ';
			}
			text = last.body || (last.oob ? me.res('gpconversations.attachment') : '');
			// As the CTI: unread chats get a stronger preview and the count after it
			return '<span class="wtmsg-last' + (rec.get('unread') ? ' wtmsg-last-unread' : '') + '">' + enc(who + text) + '</span>'
				+ (rec.get('unread') ? '<span class="wtmsg-unread">' + rec.get('unread') + '</span>' : '');
		},
		
		formatWhen: function(ts) {
			var me = this, d = new Date(ts), mins = Math.floor((Date.now() - ts) / 60000), rel;
			if (mins < 1) {
				rel = me.res('when.now');
			} else if (mins < 60) {
				rel = Ext.String.format(me.res('when.minutes'), mins);
			} else if (mins < 1440) {
				rel = Ext.String.format(me.res('when.hours'), Math.floor(mins / 60));
			} else {
				rel = Ext.String.format(me.res('when.days'), Math.floor(mins / 1440));
			}
			return rel + ' (' + Ext.Date.format(d, WT.getShortDateTimeFmt()) + ')';
		}
	}
});
