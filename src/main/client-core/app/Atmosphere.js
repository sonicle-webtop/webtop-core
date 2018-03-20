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
Ext.define('Sonicle.webtop.core.app.Atmosphere', {
	alternateClassName: 'WTA.Atmosphere',
	singleton: true,
	mixins: [
		'Ext.mixin.Observable'
	],
	
	config: {
		/**
		 * @cfg {String} url
		 * The URL for push connection.
		 */
		url: null,
		
		/**
		 * @cfg {Boolean} eventsDebug
		 * If active, displays debugging info on subsockets callbacks.
		 */
		eventsDebug: false,
		
		/**
		 * @cfg {Integer} clientHeartbeatInterval
		 * The time interval (in millis) between each heartbeat calls.
		 * If set to -1, function will be completely disabled.
		 */
		clientHeartbeatInterval: 60*1000
	},
	
	subSocket: null,
	serverUnreachable: 0,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent(cfg);
		me.mixins.observable.constructor.call(me, cfg);
	},
	
	connect: function(force) {
		var me = this,
				atm = atmosphere;
		if (force) me.disconnect();
		if (me.subSocket === null) {
			me.subSocket = atm.subscribe(me.createRequest());
		}	
	},
	
	disconnect: function() {
		var me = this,
				atm = atmosphere,
				sock = me.subSocket;
		if (sock !== null) {
			me.subSocket = null;
			atm.unsubscribeUrl(sock.getUrl());
			me.stopHBTask();
		}
	},
	
	createRequest: function() {
		var me = this;
		return {
			shared: false,
			url: me.getUrl(),
			logLevel: 'debug',
			contentType: 'application/json; charset=UTF-8',
			suspend: true,
			enableProtocol: true,
			trackMessageLength : true,
			transport: 'websocket',
			//transport: 'long-polling',
			fallbackTransport: 'long-polling',
			timeout: 300*1000, // The maximum time a connection stay opened when no message (or event) are sent or received.
			connectTimeout: -1, // The connect timeout. If the client fails to connect, the fallbackTransport will be used.
			reconnectInterval: 0, // The interval before an attempt to reconnect will be made.
			pollingInterval: 0, // Reconnect interval when long-polling transport is used and a response received.
			maxReconnectOnClose: Number.MAX_VALUE,
			reconnectOnServerError: true,
			onOpen: function(resp) {
				if (me.getEventsDebug()) console.log('onOpen ['+resp.status+', '+resp.state+']');
				
				if (!me.subSocket) return;
				me.updateSrvUnreachable(true);
				if (me.isWebsocket(resp)) {
					delete me.ws1stReconnect;
					me.initWebsocketParams(this);
				} else if (me.isLongPolling(resp)) {
					me.initLongPollingParams(this);
				}
				if (me.isHBNeeded(resp)) {
					me.startHBTask();
				}
			},
			onReopen: function(req, resp) {
				if (me.getEventsDebug()) console.log('onReopen ['+resp.status+', '+resp.state+']');
				
				if (!me.subSocket) return;
				if (me.isWebsocket(resp)) {
					me.updateSrvUnreachable(true);
				} else if (me.isLongPolling(resp)) {
					if (resp.status === 200) {
						me.updateSrvUnreachable(true);
					}
				}
				if (me.isHBNeeded(resp)) {
					me.startHBTask();
				}
			},
			onClose: function(resp) {
				if (me.getEventsDebug()) console.log('onClose ['+resp.status+', '+resp.state+']');
				
				if (!me.subSocket) return;
				me.stopHBTask();
			},
			onReconnect: function(req, resp) {
				if (me.getEventsDebug()) console.log('onReconnect ['+resp.status+', '+resp.state+']');
				
				if (!me.subSocket) return;
				if (me.isWebsocket(resp)) {
					if (me.ws1stReconnect === true) {
						me.updateSrvUnreachable();
					} else {
						me.ws1stReconnect = true;
					}
				} else if (me.isLongPolling(resp)) {
					if (resp.status === 500) {
						req.reconnectInterval = 5*1000;
						req.maxReconnectOnClose = 2*120;
						me.updateSrvUnreachable();
					} else if (resp.status === 204) {
						me.updateSrvUnreachable();
					}
				}
			},
			onClientTimeout: function(resp) {
				if (me.getEventsDebug()) console.log('onClientTimeout ['+resp.status+', '+resp.state+']');
			},
			onTransportFailure: function(err, req) {
				if (me.getEventsDebug()) console.log('onTransportFailure');
			},
			onMessage: function(resp) {
				//console.log('onMessage');
				me.handleMessages(resp.responseBody);
			},
			onError: function(resp) {
				if (me.getEventsDebug()) console.log('onError ['+resp.status+', '+resp.state+']');
				
				if (!me.subSocket) return;
				if (resp.state === 'error') {
					if (resp.status === 0) {
						// Handle net::ERR_INTERNET_DISCONNECTED error
						me.updateSrvUnreachable();
					}
				}
			}
		};
	},
	
	initWebsocketParams: function(req) {
		// The amount of time in order to switch to fallback transport
		req.connectTimeout = -1;
		// The amount of time between each reconnect tries
		req.reconnectInterval = 5*1000;
		// Max unsuccesful retries
		req.maxReconnectOnClose = 2*120;
	},
	
	initLongPollingParams: function(req) {
		// The amount of time in order to cancel current pending request and open a new one
		req.connectTimeout = 30*1000;
		// Immediate reconnect after each cycle open/pending/close (changed in case of fallback)
		req.reconnectInterval = 0;
		// Max unsuccesful retries
		req.maxReconnectOnClose = Number.MAX_VALUE;
	},
	
	handleMessages: function(raw) {
		var obj = Ext.JSON.decode(raw, true);
		if (Ext.isArray(obj)) this.fireEventArgs('receive', [this, obj]);
	},
	
	isHBNeeded: function(resp) {
		var me = this;
		if (me.isWebsocket(resp)) {
			return true;
		} else {
			return me.isLongPolling(resp) && (resp.request.connectTimeout < 0);
		}
	},
	
	startHBTask: function() {
		var me = this,
				ival = me.getClientHeartbeatInterval();
		if (!Ext.isDefined(me.hbTask) && (ival > 0)) {
			me.hbTask = Ext.TaskManager.start({
				run: function () {
					if (me.subSocket) me.subSocket.push('X');
				},
				interval: ival
			});
		}
	},
	
	stopHBTask: function() {
		var me = this;
		if (Ext.isDefined(me.hbTask)) {
			Ext.TaskManager.stop(me.hbTask);
			delete me.hbTask;
		}
	},
	
	updateSrvUnreachable: function(reset) {
		var me = this;
		if (reset === true) {
			if (me.serverUnreachable > 0) {
				me.fireEventArgs('serveronline', [me]);
			}
			me.serverUnreachable = 0;
		} else {
			me.serverUnreachable++;
			if (me.serverUnreachable === 1) {
				me.fireEventArgs('serverunreachable', [me]);
			}
		}
	},
	
	isServerUnreachable: function() {
		return this.serverUnreachable > 0;
	},
	
	privates: {
		isWebsocket: function(resp) {
			return (resp.transport === 'websocket');
		},

		isLongPolling: function(resp) {
			return (resp.transport === 'long-polling');
		}
	}
});
