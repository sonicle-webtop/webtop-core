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
		clientHeartbeatInterval: 60*1000,
		
		/**
		 * @cfg {Integer} connectionLostTimeout
		 * Amount of time (in millis) after fire the connectionlost event.
		 * Note that this event will fire only one time after the connection problem.
		 */
		connectionLostTimeout: 5*1000,
		
		/**
		 * @cfg {Integer} connectionWarnTimeout
		 * Amount of time (in millis) after display a connection warning alert.
		 */
		connectionWarnTimeout: 10*1000
	},
	
	subSocket: null,
	switchingTransport: false,
	serverUnreachable: 0,
	serverStateInvalid: 0,
	
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
			me.switchingTransport = false;
			me.subSocket = atm.subscribe(me.createRequest());
		}	
	},
	
	disconnect: function() {
		var me = this,
				atm = atmosphere;
		if (me.subSocket !== null) {
			atm.unsubscribeUrl(me.subSocket.getUrl());
			me.subSocket = null;
		}
	},
	
	createRequest: function() {
		var me = this;
		return {
			shared: false,
			url: me.getUrl(),
			logLevel: 'debug',
			//contentType: 'application/json',
			suspend: true,
			enableProtocol: true,
			trackMessageLength : true,
			transport: 'websocket',
			//transport: 'long-polling',
			fallbackTransport: 'long-polling',
			timeout: 300*1000, // The maximum time a connection stay opened when no message (or event) are sent or received.
			//connectTimeout: 10*1000, // The connect timeout. If the client fails to connect, the fallbackTransport will be used.
			//connectTimeout: 30*1000,
			connectTimeout: -1, // The timeout after switch to fallback and also the timeout for long-polling for every close/open.
			reconnectInterval: 0, // The interval before an attempt to reconnect will be made.
			pollingInterval: 5, // Reconnect interval when long-polling transport is used and a response received.
			maxReconnectOnClose: 10,
			reconnectOnServerError: true,
			onOpen: function(resp) {
				if (me.getEventsDebug()) console.log('onOpen ['+resp.status+', '+resp.state+']');
				me.switchingTransport = false;
				me.updateSrvUnreachable(true);
				
				// Sets connect timeout according to current transport
				if (me.isWebsocket(resp)) {
					// The amount of time in order to switch to fallback transport
					this.connectTimeout = -1;
					this.reconnectInterval = 0;
					this.maxReconnectOnClose = 10;
				} else if (me.isLongPolling(resp)) {
					// The amount of time in order to cancel current pending request and open a new one
					this.connectTimeout = 30*1000;
					this.reconnectInterval = 0;
					this.maxReconnectOnClose = Number.MAX_VALUE;
				}
				
				if (me.isHBNeeded(resp)) {
					me.startHBTask();
				}
			},
			onReopen: function(req, resp) {
				if (me.getEventsDebug()) console.log('onReopen ['+resp.status+', '+resp.state+']');
				me.switchingTransport = false;
				me.updateSrvUnreachable(true);
				if (me.isHBNeeded(resp)) {
					me.startHBTask();
				}
			},
			onClose: function(resp) {
				if (me.getEventsDebug()) console.log('onClose ['+resp.status+', '+resp.state+']');
				me.stopHBTask();
				/*
				if (me.isWebsocket(resp)) {
					me.updateSrvUnreachable();
				}
				*/
			},
			onReconnect: function(req, resp) {
				if (me.getEventsDebug()) console.log('onReconnect ['+resp.status+', '+resp.state+']');
				
				if (me.isLongPolling(resp)) {
					if (resp.status === 500) {
						req.reconnectInterval = 5*1000;
						me.updateSrvUnreachable();
					}
				}
				
				/*
				if (me.isLongPolling(resp)) {
					if (resp.status === 204) {
						me.updateSrvStateInvalid();
					} else if (resp.status === 500) {
						me.updateSrvUnreachable();
					}
				}
				*/
			},
			onClientTimeout: function(resp) {
				if (me.getEventsDebug()) console.log('onClientTimeout ['+resp.status+', '+resp.state+']');
			},
			onTransportFailure: function(err, req) {
				if (me.getEventsDebug()) console.log('onTransportFailure');
				me.switchingTransport = true;
			},
			onMessage: function(resp) {
				//console.log('onMessage');
				me.handleMessages(resp.responseBody);
			},
			onError: function(resp) {
				if (me.getEventsDebug()) console.log('onError ['+resp.status+', '+resp.state+']');
				
				if (resp.state === 'error') {
					if (resp.status === 0) {
						me.updateSrvUnreachable();
					} else if ((resp.status === 401) && (resp.status === 404)) {
						me.updateSrvStateInvalid();
					}
				}
				
				/*
				if (me.isLongPolling(resp)) {
					if (resp.status === 0) {
						me.updateSrvUnreachable();
					}
				}
				*/
			}
		};
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
				console.log('fire SERVER-ONLINE');
				me.fireEventArgs('serveronline', [me]);
			}
			me.serverUnreachable = 0;
			me.stopConnLostTimeout();
		} else {
			me.serverUnreachable++;
			if (me.serverUnreachable === 1) {
				me.startConnLostTimeout();
				console.log('fire SERVER-UNREACHABLE');
				me.fireEventArgs('serverunreachable', [me]);
			}
		}
	},
	
	updateSrvStateInvalid: function(reset) {
		var me = this;
		if (reset === true) {
			me.serverStateInvalid = 0;
		} else {
			me.serverStateInvalid++;
			if (me.serverStateInvalid === 1) {
				console.log('fire SERVER-STATE-INVALID');
				me.fireEventArgs('serverstateinvalid', [me]);
			}
		}
	},
	
	startConnLostTimeout: function(reset) {
		var me = this;
		if (reset === true) me.stopConnLostTimeout();
		console.log('switchingTransport='+me.switchingTransport);
		if (!me.connLost) {
			me.connLost = setTimeout(function() {
				me.fireEventArgs('connlost', [me]);
				me.startConnWarnTimeout();
			}, me.getConnectionLostTimeout());
		}
	},
	
	stopConnLostTimeout: function() {
		var me = this;
		if (me.connLost) {
			clearTimeout(me.connLost);
			me.connLost = null;
		}
		me.stopConnWarnTimeout();
	},
	
	startConnWarnTimeout: function() {
		var me = this;
		me.stopConnWarnTimeout();
		me.connWarn = setTimeout(function() {
			var fn = function() {
				if (me.connWarn) me.startConnWarnTimeout();
			};
			me.fireEventArgs('connwarn', [me, fn]);
		}, me.getConnectionWarnTimeout());
	},
	
	stopConnWarnTimeout: function() {
		var me = this;
		if (me.connWarn) {
			clearTimeout(me.connWarn);
			me.connWarn = null;
		}
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
