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
Ext.define('Sonicle.webtop.core.ComManager', {
	alternateClassName: 'WT.ComManager',
	singleton: true,
	mixins: [
		'Ext.mixin.Observable'
	],
	config: {
		wsAuthTicket: '',
		wsReconnectInterval: 10000,
		wsKeepAliveInterval: 60000,
		seInterval: 30000
	},
	
	mode: null,
	ws: null,
	
	MODE_UNKNOWN: 'unknown',
	MODE_WS: 'ws',
	MODE_SE: 'se',
	
	connect: function(cfg) {
		var me = this;
		cfg = cfg || {};
		me.initConfig(cfg);
		
		me.mode = me.MODE_UNKNOWN;
		if(me.isWSSupported) {
			me.initWebSocket();
		} else {
			console.log('WebSockets are not supported');
			me.initServerEvents();
		}
	},
	
	isWSSupported: function() {
		return ("WebSocket" in window);
	},
	
	initWebSocket: function() {
		var me = this;
		
		me.ws = Ext.create('Ext.ux.WebSocket', {
			url: 'ws://'+window.location.hostname+':'+window.location.port+window.location.pathname+"wsmanager",
			autoReconnect: true ,
			autoReconnectInterval: 10000,
			listeners: {
				open: function(ws) {
					//console.log('ws.open');
					if(me.mode === me.MODE_UNKNOWN) {
						me.mode = me.MODE_WS;
						me.shutdownServerEvents(); // Ensure SE are down
						me.runKeepAliveTask();
					}
					
					ws.send(WT.wsMsg(WT.ID, 'ticket', {
						userId: WTS.userId,
						domainId: WTS.domainId,
						principal: WTS.principal,
						encAuthTicket: me.getWsAuthTicket()
					}));
				},
				close: function(ws) {
					//console.log('ws.close');
					if(me.mode === me.MODE_UNKNOWN) return; // Disabling event!
					
					console.log('ws lost... maybe a connection problem');
				},
				message: function(ws, msg) {
					//console.log('ws.message');
					me.handleMessages(msg);
				},
				error: function(ws, err) {
					console.log('ws.error');
					if(me.mode === me.MODE_UNKNOWN) {
						// Websocket not available!
						// Close it and do failover on server-events...
						ws.close();
						me.initServerEvents();
					} else {
						console.log('ws status: '+ws.getStatus());
					}
				}
			}
		});
	},
	
	runKeepAliveTask: function() {
		var me = this;
		if(!Ext.isDefined(me.kaTask)) {
			me.kaTask = Ext.TaskManager.start({
				run: function () {
					console.log('calling keep-alive');
					Ext.Ajax.request({
						url: 'session-keep-alive',
						method: 'GET'
					});
				},
				interval: me.getWsKeepAliveInterval()
			});
		}
	},
	
	shutdownKeepAliveTask: function() {
		var me = this;
		if(Ext.isDefined(me.kaTask)) {
			Ext.TaskManager.stop(me.kaTask);
			delete me.kaTask;
		}
	},
	
	initServerEvents: function() {
		var me = this;
		if(!Ext.isDefined(me.seTask)) {
			me.mode = me.MODE_SE;
			me.shutdownKeepAliveTask();
			me.seTask = Ext.TaskManager.start({
				run: function () {
					console.log('calling ServerEvents');
					WT.ajaxReq(WT.ID, 'ServerEvents', {
						callback: function(success, o) {
							if(success) {
								if(o.data) me.handleMessages(o.data);
							}
						}
					});
				},
				interval: me.getSeInterval()
			});
		}
	},
	
	shutdownServerEvents: function() {
		var me = this;
		if(Ext.isDefined(me.seTask)) {
			Ext.TaskManager.stop(me.seTask);
			delete me.seTask;
		}
	},
	
	handleMessages: function(raw) {
		var obj = Ext.JSON.decode(raw, true);
		if(Ext.isArray(obj)) this.fireEvent('receive', this, obj);
	},
	
	buildMsg: function(service, action, config) {
		return Ext.JSON.encode(Ext.apply(config||{},{ service: service, action: action }));
	}
});
