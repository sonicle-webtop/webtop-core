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
		 * @cfg {Boolean} [eventsDebug=false]
		 * If active, displays debugging info on subsockets callbacks.
		 */
		eventsDebug: false,
		
		/**
		 * @cfg {Integer} [clientHeartbeatInterval=60000]
		 * The time interval (in millis) between each heartbeat calls.
		 * If set to -1, function will be completely disabled.
		 */
		clientHeartbeatInterval: 60*1000,
		
		/**
		 * @cfg {Integer} [connectionLostTimeout=25000]
		 * The time (in millis) within which connectionlost event is fired 
		 * after the server connection is really lost.
		 * If set to -1, function will be completely disabled.
		 */
		connectionLostTimeout: 25*1000
	},
	
	subSocket: null,
	linkStatus: 'down',
	offlineCount: 0,
	
	/**
	 * @event connect
	 * Fires when a connect is invoked.
	 * @param {WTA.Atmosphere} this
	 */
	
	/**
	 * @event disconnect
	 * Fires when a disconnect is invoked.
	 * @param {WTA.Atmosphere} this
	 */
	
	/**
	 * @event subsocketevent
	 * Fires when a subsocket event is fired and {@link #eventsDebug} is `true`.
	 * @param {WTA.Atmosphere} this
	 * @param {String} event The internal event name.
	 * @param {String} transport The internal transport used.
	 * @param {Number} status The internal response status.
	 * @param {String} status The internal response state.
	 */
	
	/**
	 * @event receive
	 * Fires when a message is received.
	 * @param {WTA.Atmosphere} this
	 * @param {Object} message The json message.
	 */
	
	/**
	 * @event connectionlost
	 * Fires when link is down and after {@link #connectionLostTimeout connectionLostTimeout} is expired.
	 * @param {WTA.Atmosphere} this
	 */
	
	/**
	 * @event connectionrestored
	 * Fires when link connection is restored and if {@link #connectionlost connectionLost event} has been fired in past.
	 * @param {WTA.Atmosphere} this
	 */
	
	/**
	 * @event linkstatuschange
	 * Fires when server connection status changes.
	 * @param {WTA.Atmosphere} this
	 * @param {Integer} offlineCount Online->Offline transitions counts.
	 */
	
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
			me.fireEventArgs('connect', [me]);
			if (!me.lastReq) {
				me.lastReq = me.createRequest();
			} else {
				me.initTransportParams(me.lastReq);
			}
			me.subSocket = atm.subscribe(me.lastReq);
		}	
	},
	
	disconnect: function() {
		var me = this,
				atm = atmosphere,
				sock = me.subSocket;
		if (sock) {
			atm.unsubscribeUrl(sock.getUrl());
			me.stopHBTask();
			me.subSocket = me.lastReq = null;
			me.fireEventArgs('disconnect', [me]);
		}
		me.resetConnLostTimeout();
		me.offlineCount = 0;
	},
	
	getLinkStatus: function() {
		return this.linkStatus;
	},
	
	privates: {
		isWS: function(transport) {
			return transport === this.self.T_WS;
		},
		
		isLP: function(transport) {
			return transport === this.self.T_LP;
		},
		
		startHBTask: function() {
			var me = this,
					ival = me.getClientHeartbeatInterval();
			if (!Ext.isDefined(me.hbTask) && (ival > 0)) {
				me.hbTask = Ext.TaskManager.start({
					run: function () {
						if (me.subSocket) me.subSocket.push('X');
					},
					interval: ival,
					fireOnStart: true
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
		
		handleMessages: function(raw) {
			var obj = Ext.JSON.decode(raw, true);
			if (Ext.isArray(obj)) this.fireEventArgs('receive', [this, obj]);
		},
		
		linkUp: function() {
			var me = this,
					debug = me.getEventsDebug(),
					wasLost = me.tmConnLostFired;
			
			me.linkStatus = 'up';
			me.resetConnLostTimeout();
			if (debug) console.log('Firing linkstatuschange ['+me.linkStatus+', '+me.offlineCount+']');
			me.fireEventArgs('linkstatuschange', [me, me.linkStatus, me.offlineCount]);
			
			if (wasLost === true) {
				if (debug) console.log('Firing connectionrestored');
				me.fireEventArgs('connectionrestored', [me]);
			}
		},
		
		linkDown: function() {
			var me = this,
					debug = me.getEventsDebug(),
					millis = me.getConnectionLostTimeout();
			
			me.linkStatus = 'down';
			me.offlineCount++;
			if (debug) console.log('Firing linkstatuschange ['+me.linkStatus+', '+me.offlineCount+']');
			me.fireEventArgs('linkstatuschange', [me, me.offlineCount]);
			
			if (!Ext.isDefined(me.tmConnLost) && (millis > 0)) {
				me.tmConnLostFired = false;
				me.tmConnLost = setTimeout(function() {
					if (debug) console.log('Firing connectionlost');
					me.tmConnLostFired = true;
					me.fireEventArgs('connectionlost', [me]);
				}, millis);
			}
		},
		
		resetConnLostTimeout: function() {
			var me = this;
			if (Ext.isDefined(me.tmConnLost)) {
				clearTimeout(me.tmConnLost);
				delete me.tmConnLost;
				delete me.tmConnLostFired;
			}
		},
		
		initTransportParams: function(req) {
			var me = this;
			return Ext.apply(req, {
				transport: me.self.T_WS,
				//transport: me.self.T_LP,
				fallbackTransport: me.self.T_LP,
				timeout: 300*1000, // The maximum time a connection stay opened when no message are sent or received.
				// NB: this is used as long-polling connection timeout (after that request is cancelled) 
				// and also as first timeout in order to switch to fallback trasport.
				connectTimeout: 30*1000, // The connect timeout. If the client fails to connect, the fallbackTransport will be used.
				reconnectInterval: 5*1000, // The interval in milliseconds before an attempt to reconnect will be made.
				maxRequest: -1, // The maximum number of requests that will be executed.
				maxReconnectOnClose: Number.MAX_VALUE, // The maximum reconnect after a connection is marked as 'dead'.
				pollingInterval: 0 // The reconnect interval when long-polling transport is used and a response received.
			});
		},

		createRequest: function() {
			var me = this;
			return me.initTransportParams({
				shared: false,
				url: me.getUrl(),
				logLevel: 'debug',
				contentType: 'application/json; charset=UTF-8',
				suspend: true, // Suspend the request, always reconnect if the connection gets closed.
				enableProtocol: true,
				trackMessageLength: true,
				reconnectOnServerError: false, // If the server respond with a status code higher than 300, onError will be called instead of reconnect.
				//enableXDR: false,

				onOpen: function(resp) {
					if (me.getEventsDebug()) {
						console.log('onOpen ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'open', resp.transport, resp.status, resp.state]);
					}

					// Dump some parameters, this is required if you want to call subscribe(request) again.
					me.lastReq.uuid = resp.request.uuid;
					me.lastReq.transport = resp.request.transport;
					
					if (resp.transport === 'long-polling') {
						resp.request.connectTimeout = 30*1000;
					}

					me.startHBTask();
					me.linkUp();
				},
				onReopen: function(req, resp) {
					if (me.getEventsDebug()) {
						console.log('onReopen ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'reopen', resp.transport, resp.status, resp.state]);
					}
				},
				onClose: function(resp) {
					if (me.getEventsDebug()) {
						console.log('onClose ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'close', resp.transport, resp.status, resp.state]);
					}

					me.stopHBTask();
					if (resp.status === 408 && resp.state === 'unsubscribe') {
						me.linkDown();
					}
				},
				onReconnect: function(req, resp) {
					if (me.getEventsDebug()) {
						console.log('onReconnect ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'reconnect', resp.transport, resp.status, resp.state]);
					}
				},
				onClientTimeout: function(req) {
					if (me.getEventsDebug()) {
						console.log('onClientTimeout ['+req.transport+']');
						me.fireEventArgs('subsocketevent', [me, 'clientTimeout', req.transport, '', '']);
					}

					setTimeout(function() {
						if (me.subSocket) {
							if (me.getEventsDebug()) console.log('reSubscribing');
							me.subSocket = me.subSocket.subscribe(me.lastReq);
						}
					}, me.lastReq.reconnectInterval);
				},
				onTransportFailure: function(err, req) {
					if (me.getEventsDebug()) {
						console.log('onTransportFailure ['+req.transport+']');
						me.fireEventArgs('subsocketevent', [me, 'transportFailure', req.transport, '', '']);
					}
				},
				onMessage: function(resp) {
					//console.log('onMessage');
					me.handleMessages(resp.responseBody);
				},
				onError: function(resp) {
					if (me.getEventsDebug()) {
						console.log('onError ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'error', resp.transport, resp.status, resp.state]);
					}

					if (resp.state === 'error') {
						// Dump resp data before doing any actions, it avoid data overwrite!
						var sta = resp.status,
								tra = resp.transport;

						if (sta === 401) me.disconnect();
						// status = 0 --> net::ERR_INTERNET_DISCONNECTED error
						if (sta > 0) me.fireEventArgs('servererror', [me, sta, tra]);
					}
				}
			});
		}
	},
	
	statics: {
		T_NO: 'none',
		T_WS: 'websocket',
		T_LP: 'long-polling'
	}
});
