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
		
		uuid: 0,
		
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
		connectionWarnTimeout: 10*1000,
		
		/**
		 * @cfg {Integer} clientHeartbeatInterval
		 * The time interval (in millis) between each heartbeat calls.
		 * If set to -1, function will be completely disabled.
		 */
		clientHeartbeatInterval: 60*1000
	},
	
	socket: null,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent(cfg);
		me.mixins.observable.constructor.call(me, cfg);
	},
	
	connect: function() {
		var me = this,
				atm = atmosphere;
		if (me.socket === null) {
			me.socket = atm.subscribe(me.createRequest());
		}	
	},
	
	disconnect: function() {
		var me = this;
		if (me.socket !== null) {
			me.socket.unsubscribe();
			me.socket = null;
		}
	},
	
	createRequest: function() {
		var me = this;
		return {
			url: me.getUrl(),
			//uuid: me.getUuid(),
			//shared: true,
			//logLevel: 'debug',
			contentType: 'application/json',
			transport: 'websocket',
			//transport: 'long-polling',
			fallbackTransport: 'long-polling',
			connectTimeout: 10*1000,
			trackMessageLength : true,
			reconnectInterval: 10*1000,
			onOpen: function(resp) {
				//console.log('onOpen');
				var trnsp = resp.transport;
				if ((trnsp === 'websocket') || (trnsp === 'long-polling')) {
					if (resp.request.async === true) {
						me.startHBTask();
					}
					me.clearConnLostTimeout();
					me.clearConnWarnTimeout();
				}
			},
			onReopen: function(resp) {
				//console.log('onReopen');
				var trnsp = resp.transport;
				if ((trnsp === 'websocket') || (trnsp === 'long-polling')) {
					if (resp.async === true) {
						me.startHBTask();
					}
					me.clearConnLostTimeout();
					me.clearConnWarnTimeout();
				}
			},
			onClose: function(resp) {
				//console.log('onClose');
				var trnsp = resp.transport;
				if ((trnsp === 'websocket') || (trnsp === 'long-polling')) {
					me.stopHBTask();
					if (resp.transport === 'websocket') {

					}
					me.startConnLostTimeout();
				}
			},
			onReconnect: function(req, resp) {
				//console.log('onReconnect');
			},
			onClientTimeout: function(resp) {
				//console.log('onClientTimeout');
			},
			onTransportFailure: function(err, req) {
				//console.log('onTransportFailure');
			},
			onMessage: function(resp) {
				//console.log('onMessage');
				me.handleMessages(resp.responseBody);
			},
			onError: function(resp) {
				//console.log('onError');
			}
		};
	},
	
	startConnLostTimeout: function() {
		var me = this;
		me.clearConnLostTimeout();
		me.connLost = setTimeout(function() {
			me.fireEventArgs('connectionlost', [me]);
			me.startConnWarnTimeout();
		}, me.getConnectionLostTimeout());
	},
	
	clearConnLostTimeout: function() {
		var me = this;
		if (me.connLost) {
			clearTimeout(me.connLost);
			me.connLost = null;
		}
	},
	
	startConnWarnTimeout: function() {
		var me = this;
		me.clearConnWarnTimeout();
		me.connWarn = setTimeout(function() {
			var fn = function() {
				if (me.connWarn) me.startConnWarnTimeout();
			};
			me.fireEventArgs('connectionwarn', [me, fn]);
		}, me.getConnectionWarnTimeout());
	},
	
	clearConnWarnTimeout: function() {
		var me = this;
		if (me.connWarn) {
			clearTimeout(me.connWarn);
			me.connWarn = null;
		}
	},
	
	startHBTask: function() {
		var me = this,
				ival = me.getClientHeartbeatInterval();
		if (!Ext.isDefined(me.hbTask) && (ival > 0)) {
			me.hbTask = Ext.TaskManager.start({
				run: function () {
					if (me.socket) me.socket.push('X');
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
	
	handleMessages: function(raw) {
		var obj = Ext.JSON.decode(raw, true);
		if (Ext.isArray(obj)) this.fireEventArgs('receive', [this, obj]);
	}
});
