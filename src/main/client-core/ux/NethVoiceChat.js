/* 
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
/**
 * NethVoice chat (chat-island widget) inside WebTop.
 * 
 * Mounts the widget once the viewport is rendered, with a CTI token from the
 * core service (GetNethVoiceChatToken), and talks to it only through window
 * CustomEvents (see the chat-island README). While pinned, the chat heads live
 * in the viewport's right rail and the open conversation in its east panel,
 * as in the NethVoice CTI; unpinned, the island floats bottom-right.
 */
Ext.define('Sonicle.webtop.core.ux.NethVoiceChat', {
	alternateClassName: 'WTA.ux.NethVoiceChat',
	mixins: [
		'Ext.mixin.Observable'
	],
	
	/**
	 * @cfg {String} widgetVersion
	 * The chat-island widget bundled in core resources (vendor/chat-island/<version>).
	 */
	widgetVersion: '0.1.72',
	
	/**
	 * @cfg {WTA.private.viewport.BaseAbstract} viewport
	 * The viewport that owns the rail and the panel slots.
	 */
	viewport: null,
	
	pinned: true,
	status: null,
	unread: 0,
	
	/**
	 * @event status
	 * @param {WTA.ux.NethVoiceChat} this
	 * @param {String} status connecting, online, offline, error, unauthorized
	 */
	/**
	 * @event unread
	 * @param {WTA.ux.NethVoiceChat} this
	 * @param {Number} total
	 */
	/**
	 * @event conversations
	 * @param {WTA.ux.NethVoiceChat} this
	 * @param {Object[]} conversations
	 * @param {Boolean} ready
	 */
	
	constructor: function(cfg) {
		var me = this;
		me.viewport = cfg.viewport;
		me.mixins.observable.constructor.call(me, {});
		me.pinned = WT.getVar('nvChatPinned') !== false;
		Ext.util.CSS.createStyleSheet([
				// Floating, the island must stay above ExtJS windows (their z-index starts at 19000).
				'.chat-island-root.ci-fixed{z-index:29000;}',
				// One chat icon everywhere: the Messages launcher bubble (solid, lines cut out), painted in the text color.
				':root{--wt-nvchat-mask:url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0naHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmcnIHZpZXdCb3g9JzAgMCAyNCAyNCc+PG1hc2sgaWQ9J20nPjxyZWN0IHdpZHRoPScyNCcgaGVpZ2h0PScyNCcgZmlsbD0nI2ZmZicvPjxwYXRoIGQ9J002LjI1IDdoMTEuNU02LjI1IDEwLjI1aDExLjVNNi4yNSAxMy41aDcuNScgc3Ryb2tlPScjMDAwJyBzdHJva2Utd2lkdGg9JzEuNicgc3Ryb2tlLWxpbmVjYXA9J3JvdW5kJy8+PC9tYXNrPjxwYXRoIG1hc2s9J3VybCgjbSknIGQ9J000IDJoMTZhMyAzIDAgMCAxIDMgM3Y5LjVhMyAzIDAgMCAxLTMgM2gtOS4xbC00LjUgMy45YS42NS42NSAwIDAgMS0xLjA3LS41VjE3LjVINGEzIDMgMCAwIDEtMy0zVjVhMyAzIDAgMCAxIDMtM3onIGZpbGw9JyMwMDAnIHN0cm9rZT0nIzAwMCcgc3Ryb2tlLXdpZHRoPScxLjYnIHN0cm9rZS1saW5lam9pbj0ncm91bmQnLz48L3N2Zz4=);}',
				".wt-icon-nvchat:before{content:'';display:inline-block;width:1em;height:1em;vertical-align:-0.125em;background-color:currentColor;-webkit-mask:var(--wt-nvchat-mask) center/contain no-repeat;mask:var(--wt-nvchat-mask) center/contain no-repeat;}.wt-icon-nvchat{background-image:none !important;}",
				// Focus as WebTop's fields (nethesis LAF): a 1px border in its focus color instead of the island's 2px accent ring.
				'html .chat-island-root .focus-within\\:ci-ring-2:focus-within, html .chat-island-root input.focus\\:ci-ring-2:focus, html .chat-island-root textarea.focus\\:ci-ring-2:focus{--tw-ring-shadow:0 0 #0000 !important;border-color:#cb5e30 !important;}',
				'html .chat-island-root.ci-dark .focus-within\\:ci-ring-2:focus-within, html .chat-island-root.ci-dark input.focus\\:ci-ring-2:focus, html .chat-island-root.ci-dark textarea.focus\\:ci-ring-2:focus{border-color:#fa8839 !important;}',
				'html .chat-island-root .focus-visible\\:ci-ring-2:focus-visible, html .chat-island-root button.focus\\:ci-ring-2:focus{--tw-ring-color:#cb5e30 !important;}',
				'html .chat-island-root.ci-dark .focus-visible\\:ci-ring-2:focus-visible, html .chat-island-root.ci-dark button.focus\\:ci-ring-2:focus{--tw-ring-color:#fa8839 !important;}',
				// The heads' tooltips open to the left of the rail: above the collapsed chat panel's bar next to it.
				'.wt-viewport-nvchat-rail{z-index:5;overflow:visible !important;}',
				// The rail sits right under the topbar, nothing above it to divide from.
				'.wt-nvchat-rail-slot .chat-island-root .ci-flex-col > .ci-border-t.ci-w-6:first-child{display:none;}',
				// WebTop's accent instead of the CTI's emerald. The island's primary is 700 in light (hover 600) and 500 in dark (hover 400):
				// WebTop's buttons are #c2410c (hover #9a3412) in light, #f97316 (hover #fdba74) in dark.
				'html .chat-island-root{--ci-accent-50:255 247 237;--ci-accent-300:253 186 116;--ci-accent-400:251 146 60;--ci-accent-500:234 88 12;--ci-accent-600:154 52 18;--ci-accent-700:194 65 12;--ci-accent-950:67 20 7;}',
				'html .chat-island-root.ci-dark{--ci-accent-300:254 215 170;--ci-accent-400:253 186 116;--ci-accent-500:249 115 22;--ci-accent-600:249 115 22;--ci-accent-700:234 88 12;}'
		].join(''), 'wt-nvchat-css');
		me.listenIsland();
		me.fetchConfig(function(config) {
			if (config) me.mount(config);
		});
	},
	
	/**
	 * Opens the chat with an operator or a group address.
	 * @param {String} username
	 */
	openChat: function(username) {
		this.send('chat-island-open', {username: username});
	},
	
	/**
	 * Opens the new chat picker.
	 */
	newChat: function() {
		this.send('chat-island-new');
	},
	
	/**
	 * Asks the island for the conversation list (answered by the conversations event).
	 */
	requestConversations: function() {
		this.send('chat-island-conversations-request');
	},
	
	/**
	 * Searches every conversation's archive (answered by the searchresults event).
	 * @param {String} query
	 */
	search: function(query) {
		this.send('chat-island-search', {query: query});
	},
	
	/**
	 * Purges a conversation, or leaves (destroys, when owned) a group.
	 * @param {String} username
	 */
	deleteChat: function(username) {
		this.send('chat-island-delete', {username: username});
	},
	
	/**
	 * Chats with these people, picked by email (a contact, a sender, the recipients of a thread, the attendees).
	 * Colleagues on the chat (users of this domain who are CTI operators) get a direct chat (one) or a group (more);
	 * with anyone from outside, a chat open to guests, colleagues as its members: its link goes to the callback.
	 * @param {Object[]} people Items with `email` and `name`.
	 * @param {Object} [opts]
	 * @param {String} [opts.title] The group or guest chat name (default: the people's names).
	 * @param {Function} [opts.onLink] Called with (link, info) when a guest link was made: info has room, expires, external.
	 * @param {Object} [opts.scope]
	 */
	chatWith: function(people, opts) {
		var me = this, emails = [], names = {};
		opts = opts || {};
		Ext.each(Ext.Array.from(people), function(p) {
			var email = Ext.isString(p) ? p : (p && p.email);
			if (Ext.isEmpty(email)) return;
			emails.push(email);
			names[String(email).toLowerCase()] = (p && p.name) || email;
		});
		// opts.link: always a chat with a link (as the mail editor's "Add chat"), even with no recipients yet.
		if (emails.length === 0) {
			if (opts.link) me.inviteGuests(opts.title, [], function(link, info) {
				Ext.callback(opts.onLink, opts.scope || me, [link, Ext.apply(info, {external: [], internal: []})]);
			});
			return;
		}
		WT.ajaxReq(WT.ID, 'ResolveNethVoiceChatPeers', {
			params: {emails: Ext.JSON.encode(emails)},
			callback: function(success, json) {
				var d = success && json ? json.data : null, members, title;
				if (!d) {
					WT.error(json && json.message ? json.message : WT.res('nvchat.error.resolve'));
					return;
				}
				members = Ext.Array.pluck(d.internal, 'username');
				title = opts.title || Ext.Array.map(d.internal.concat(d.external), function(p) {
					return names[p.email] || p.name || p.email;
				}).join(', ').substring(0, 64);
				if (d.external.length > 0 || opts.link) {
					me.inviteGuests(title, members, function(link, info) {
						Ext.callback(opts.onLink, opts.scope || me, [link, Ext.apply(info, {external: d.external, internal: d.internal})]);
					});
				} else if (members.length === 1) {
					me.openChat(members[0]);
				} else if (members.length > 1) {
					me.send('chat-island-group-create', {name: title, members: members});
				} else {
					WT.warn(WT.res('nvchat.warn.nobody'));
				}
			}
		});
	},
	
	/**
	 * A chat with people outside: these colleagues as members (none: just me), its invite link to the callback.
	 */
	inviteGuests: function(title, members, cb) {
		var me = this,
			id = Ext.id(null, 'nvguest-'),
			on = function(e) {
				var d = e.detail || {};
				if (d.id !== id) return;
				window.removeEventListener('chat-island-guest-link', on);
				if (d.error || !d.link) {
					WT.error(WT.res('nvchat.error.guest') + (d.error ? ' (' + d.error + ')' : ''));
				} else {
					Ext.callback(cb, me, [d.link, {room: d.room, expires: d.expires}]);
				}
			};
		window.addEventListener('chat-island-guest-link', on);
		me.send('chat-island-guest-invite', {id: id, name: title, members: members});
	},
	
	setPinned: function(pinned, save) {
		var me = this;
		me.pinned = pinned;
		me.viewport.showNVChatRail(pinned);
		if (!pinned) me.viewport.showNVChatPanel(false);
		me.announceSlots();
		if (save) WT.ajaxReq(WT.ID, 'SetNethVoiceChatPinned', {params: {pinned: pinned}});
	},
	
	privates: {
		widgetUrl: function(file) {
			return 'resources/' + WT.ID + '/0.0.0/resources/vendor/chat-island/' + this.widgetVersion + '/' + file;
		},
		
		send: function(name, detail) {
			window.dispatchEvent(new CustomEvent(name, {detail: detail}));
		},
		
		/**
		 * Gets the widget config: Base64 of <cti host>:<username>:<token>.
		 */
		fetchConfig: function(cb) {
			var me = this;
			WT.ajaxReq(WT.ID, 'GetNethVoiceChatToken', {
				callback: function(success, json) {
					var d = success && json ? json.data : null;
					if (!d || !d.token) {
						if (window.console) console.warn('[nvchat] no CTI token: ' + (json ? json.message : ''));
						Ext.callback(cb, me, [null]);
						return;
					}
					Ext.callback(cb, me, [window.btoa(d.host + ':' + d.username + ':' + d.token)]);
				}
			});
		},
		
		mount: function(config) {
			var me = this,
				div = document.createElement('div'),
				lang = String(WT.getVar('language') || 'en').substring(0, 2);
			
			WT.loadCss(me.widgetUrl('index.widget.css'));
			div.className = 'chat-island';
			div.setAttribute('data-config', config);
			// Pinned, the island follows the host's `dark` class on <html>: WebTop's color scheme.
			// Floating, it keeps the theme picked in its own settings (no data-theme, that would win over it).
			Ext.fly(document.documentElement).toggleCls('dark', WT.getVar('colorScheme') === 'dark');
			div.setAttribute('data-language', lang);
			div.setAttribute('data-pinnable', 'true');
			// WebTop's compact density (account menu): the island tightens its spacing too
			if (WT.getVar('uiDensity') === 'compact') div.setAttribute('data-density', 'compact');
			div.setAttribute('data-new-chat-button', 'false');
			document.body.appendChild(div);
			me.el = div;
			// The widget mounts on the .chat-island elements present when its script runs.
			Ext.Loader.loadScript({
				url: me.widgetUrl('index.widget.js'),
				onLoad: function() {
					me.announceSlots();
					me.requestConversations();
				},
				onError: function() {
					if (window.console) console.error('[nvchat] unable to load the chat-island widget');
				},
				scope: me
			});
		},
		
		listenIsland: function() {
			var me = this,
				on = function(name, fn) {
					window.addEventListener(name, function(e) {
						fn.call(me, e.detail || {});
					});
				};
			
			on('chat-island-status', function(d) {
				me.status = d.status;
				// The island may come up after the slots: announce them again.
				me.announceSlots();
				if (d.status === 'unauthorized') me.onRefused();
				me.fireEvent('status', me, d.status);
			});
			on('chat-island-unread', function(d) {
				me.unread = d.total || 0;
				me.fireEvent('unread', me, me.unread);
			});
			on('chat-island-conversations', function(d) {
				me.fireEvent('conversations', me, d.conversations || [], !!d.ready);
			});
			on('chat-island-search-results', function(d) {
				me.fireEvent('searchresults', me, d.query, d.results || []);
			});
			on('chat-island-window', function(d) {
				// The panel exists only while a conversation or the picker is open.
				if (me.pinned) me.viewport.showNVChatPanel(!!(d.open || d.picker));
			});
			on('chat-island-pin', function(d) {
				me.setPinned(!!d.pinned, true);
			});
			// The chat's Call button: WebTop's click-to-call (the NethVoice PBX provider) rings the user's phone, then the number.
			on('chat-island-call', function(d) {
				if (d.number) WT.handleCallNumber(d.number);
			});
		},
		
		/**
		 * The server refused the token: a new one in data-config reconnects in place.
		 * At most once a minute, the island retries by itself in between.
		 */
		onRefused: function() {
			var me = this, now = Date.now();
			if (me.lastRefresh && (now - me.lastRefresh) < 60000) return;
			me.lastRefresh = now;
			me.fetchConfig(function(config) {
				if (config && me.el) me.el.setAttribute('data-config', config);
			});
		},
		
		announceSlots: function() {
			var me = this, vp = me.viewport;
			me.send('chat-island-rail-target', {el: me.pinned ? vp.getNVChatRailEl() : null});
			me.send('chat-island-pin-target', {el: me.pinned ? vp.getNVChatPanelEl() : null});
		}
	}
});
