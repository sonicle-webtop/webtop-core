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
Ext.define('Sonicle.webtop.core.app.PushManager', {
	alternateClassName: 'WTA.PushManager',
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
		 * @cfg {Boolean} [useSuccessfulTransport=true]
		 * In case of transportFailure and after a successful connection in past,
		 * the last working transport will be used as fallback instead of the
		 * configured fallback, in practice locking it on the last used.
		 */
		useSuccessfulTransport: false,
		
		/**
		 * @cfg {Boolean} [eventsDebug=false]
		 * If active, displays debugging info on subsockets callbacks.
		 */
		eventsDebug: false,
		
		/**
		 * @cfg {Number} [buffer=2000]
		 * The number of milliseconds by which to buffer the link-down status
		 * before updating the internal state. This flats any possible down-up
		 * oscillations for eg. in long-polling disconnects.
		 */
		linkDownBuffer: 2*1000,
		
		/**
		 * @cfg {Boolean} [decodeMessage=false]
		 * `true` to apply base64 decoding to arrived raw message.
		 */
		decodeMessage: false,
		
		/**
		 * @cfg {Number} [reconnectMaxRate=1]
		 * Number of maximum reconnects above that a preventive disconnect
		 * will be issued in order to not flood server by requests.
		 * If set to 0 or negative value, function will be completely disabled.
		 */
		reconnectMaxRate: 1,
		
		/**
		 * @cfg {Integer} [connectionLostTimeout=20000]
		 * The time (in millis) within which connectionlost event is fired 
		 * after the server connection is really lost.
		 * If set to -1, function will be completely disabled.
		 */
		connectionLostTimeout: 20*1000
	},
	
	subSocket: null,
	linkStatus: 'down',
	offlineCount: 0,
	
	/**
	 * @event connect
	 * Fires when a connect is invoked.
	 * @param {WTA.PushManager} this
	 */
	
	/**
	 * @event disconnect
	 * Fires when a disconnect is invoked.
	 * @param {WTA.PushManager} this
	 */
	
	/**
	 * @event subsocketevent
	 * Fires when a subsocket event is fired and {@link #eventsDebug} is `true`.
	 * @param {WTA.PushManager} this
	 * @param {String} event The internal event name.
	 * @param {String} transport The internal transport used.
	 * @param {Number} status The internal response status.
	 * @param {String} status The internal response state.
	 */
	
	/**
	 * @event receive
	 * Fires when a message is received.
	 * @param {WTA.PushManager} this
	 * @param {Object} message The json message.
	 */
	
	/**
	 * @event linkstatuschange
	 * Fires when server connection status changes.
	 * @param {WTA.PushManager} this
	 * @param {Integer} offlineCount Online->Offline transitions counts.
	 */
	
	/**
	 * @event connectionlost
	 * Fires when link is down and after {@link #connectionLostTimeout connectionLostTimeout} is expired.
	 * @param {WTA.PushManager} this
	 */
	
	/**
	 * @event connectionrestored
	 * Fires when link connection is restored and if {@link #connectionlost connectionLost event} has been fired in past.
	 * @param {WTA.PushManager} this
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
				me.applyBaseTransportCfg(me.lastReq);
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
			me.subSocket = null;
			me.fireEventArgs('disconnect', [me]);
		}
		me.resetConnLostTimeout();
		delete me.lastReq;
		delete me.lastSuccessfulTransport;
		me.offlineCount = 0;
	},
	
	getLinkStatus: function() {
		return this.linkStatus;
	},
	
	privates: {
		handleMessages: function(raw) {
			var me = this,
					draw = me.getDecodeMessage() ? Ext.util.Base64.decode(raw) : raw,
					obj = Ext.JSON.decode(draw, true);
			if (Ext.isArray(obj)) this.fireEventArgs('receive', [this, obj]);
		},
		
		updateLinkStatus: function(newStatus) {
			var me = this,
					oldStatus = me.linkStatus;
			
			if (newStatus === 'up') {
				if (me.tmDownBuffer) {
					clearTimeout(me.tmDownBuffer);
					delete me.tmDownBuffer;
				}
				if (newStatus !== oldStatus) {
					me.linkStatus = newStatus;
					me.handleLinkStatusChange();
				}
			} else {
				if (newStatus !== oldStatus) {
					if (!Ext.isDefined(me.tmDownBuffer)) {
						me.tmDownBuffer = setTimeout(function() {
							me.linkStatus = newStatus;
							me.offlineCount++;
							me.handleLinkStatusChange();
						}, me.getLinkDownBuffer());
					}
				}
			}
		},
		
		resetConnLostTimeout: function() {
			clearTimeout(this.tmConnLost);
			delete this.tmConnLost;
		},
		
		handleLinkStatusChange: function() {
			var me = this,
					ls = me.linkStatus,
					millis;
			
			me.fireEventArgs('linkstatuschange', [me, me.linkStatus, me.offlineCount]);
			if (ls === 'up') {
				if (me.tmConnLost === true) me.fireEventArgs('connectionrestored', [me]);
				me.resetConnLostTimeout();
				
			} else if (ls === 'down') {
				millis = me.getConnectionLostTimeout();
				if (!Ext.isDefined(me.tmConnLost) && (millis > 0)) {
					me.tmConnLost = setTimeout(function() {
						me.tmConnLost = true;
						me.fireEventArgs('connectionlost', [me]);
					}, millis);
				}
			}
		},
		
		applyBaseTransportCfg: function(req) {
			return Ext.apply(req, {
				transport: 'websocket',
				//transport: 'long-polling',
				fallbackTransport: 'long-polling',
				timeout: 60*1000, // The maximum time a connection stay opened when no message are sent or received.
				connectTimeout: 0*1000, // The connect timeout. If the client fails to connect, the fallbackTransport will be used.
				reconnectInterval: 5*1000, // The interval in milliseconds before an attempt to reconnect will be made.
				maxRequest: -1, // The maximum number of requests that will be executed.
				maxReconnectOnClose: Number.MAX_VALUE, // The maximum reconnect after a connection is marked as 'dead'.
				pollingInterval: 0*1000 // The reconnect interval when long-polling transport is used and a response received.
			});
		},
		
		/*
		applyTransportCfg: function(transport, req) {
			if (transport === 'websocket') {
				return Ext.apply(req, {
					timeout: 300*1000
				});
			} else if (transport === 'long-polling') {
				return Ext.apply(req, {
					timeout: 30*1000
				});
			} else {
				return req;
			}
			return req;
		},
		*/

		createRequest: function() {
			var me = this,
					req = me.applyBaseTransportCfg({
						shared: false,
						url: me.getUrl(),
						logLevel: me.getEventsDebug() ? 'debug' : 'info',
						contentType: 'application/json; charset=UTF-8',
						suspend: true, // Suspend the request, always reconnect if the connection gets closed.
						enableProtocol: true,
						trackMessageLength: true,
						executeCallbackBeforeReconnect: true,
						reconnectOnServerError: false // If the server respond with a status code higher than 300, onError will be called instead of reconnect.
						//enableXDR: false
					});
			
			//me.applyTransportCfg(req.transport, req);
			return Ext.apply(req, {
				callback: function(resp) {
					if (me.getEventsDebug()) {
						console.log('callback ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'callback', resp.transport, resp.status, resp.state]);
					}
					
					if ((resp.status === 408) && (resp.state === 'closedByClient')) {
						me.updateLinkStatus('down');
					}
				},
				onOpen: function(resp) {
					if (me.getEventsDebug()) {
						console.log('onOpen ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'open', resp.transport, resp.status, resp.state]);
					}

					// Dump some parameters, this is required if you want to call subscribe(request) again.
					me.lastReq.uuid = resp.request.uuid;
					me.lastReq.transport = resp.request.transport;
					me.lastSuccessfulTransport = resp.request.transport;
					
					me.updateLinkStatus('up');
				},
				onReopen: function(req, resp) {
					if (me.getEventsDebug()) {
						console.log('onReopen ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'reopen', resp.transport, resp.status, resp.state]);
					}
					
					me.updateLinkStatus('up');
				},
				onClose: function(resp) {
					if (me.getEventsDebug()) {
						console.log('onClose ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'close', resp.transport, resp.status, resp.state]);
					}
					
					me.updateLinkStatus('down');
				},
				onReconnect: function(req, resp) {
					if (me.getEventsDebug()) {
						console.log('onReconnect ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'reconnect', resp.transport, resp.status, resp.state]);
					}
					
					var maxRate = me.getReconnectMaxRate();
					if (maxRate > 0) {
						var now = new Date().getTime();
						if (me.lastReconnectTs && (now - me.lastReconnectTs) < Math.ceil(1000/maxRate)) {
							me.disconnect();
						}
						me.lastReconnectTs = now;
					}
				},
				onFailureToReconnect: function(req, resp) {
					if (me.getEventsDebug()) {
						console.log('onFailureToReconnect ['+resp.transport+', '+resp.status+', '+resp.state+']');
						me.fireEventArgs('subsocketevent', [me, 'failureToReconnect', resp.transport, resp.status, resp.state]);
					}
				},
				onClientTimeout: function(req) {
					if (me.getEventsDebug()) {
						console.log('onClientTimeout ['+req.transport+']');
						me.fireEventArgs('subsocketevent', [me, 'clientTimeout', req.transport, '', '']);
					}

					setTimeout(function() {
						if (me.subSocket && me.lastReq) {
							if (me.getEventsDebug()) console.log('reSubscribing... ['+me.lastReq.transport+']');
							me.subSocket.subscribe(me.lastReq);
						}
					}, me.lastReq.reconnectInterval);
				},
				onTransportFailure: function(err, req) {
					if (me.getEventsDebug()) {
						console.log('onTransportFailure ['+req.transport+']');
						me.fireEventArgs('subsocketevent', [me, 'transportFailure', req.transport, '', '']);
					}
					
					if (me.getUseSuccessfulTransport() && me.lastSuccessfulTransport) {
						req.fallbackTransport = me.lastSuccessfulTransport;
					}
					//me.lastReq = me.applyTransportCfg(req.fallbackTransport, me.lastReq);
					//me.applyTransportCfg(req.fallbackTransport, req);
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
	}
});
