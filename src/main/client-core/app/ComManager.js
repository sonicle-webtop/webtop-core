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
Ext.define('Sonicle.webtop.core.app.ComManager', {
	alternateClassName: 'WTA.ComManager',
	singleton: true,
	uses: [
		'Sonicle.WebSocket'
	],
	mixins: [
		'Ext.mixin.Observable'
	],
	
	statics: {
		MODE_WS: 'ws',
		MODE_FP: 'fp'
	},
	
	config: {
		
		/**
		 * @cfg {Integer} wsReconnectInterval
		 * Amount of time (in millis) between each websocket reconnect tries.
		 */
		wsReconnectInterval: 10*1000,
		
		/**
		 * @cfg {Integer} wsKeepAliveInterval
		 * Amount of time (in millis) between each keep-alive server calls.
		 */
		wsKeepAliveInterval: 60*1000,
		
		/**
		 * @cfg {Integer} fpPollInterval
		 * Amount of time (in millis) between each fast-poll server calls.
		 */
		fpPollInterval: 5*1000,
		
		/**
		 * @cfg {Integer} connectionLostTimeout
		 * Amount of time (in millis) after fire the connectionlost event.
		 * Note that this event will fire only one time after the connection problem.
		 */
		connectionLostTimeout: 60*1000,
		
		/**
		 * @cfg {Boolean} connectionWarn
		 * True to display a connection warning alert after the {@link #connectionWarnTimeout}.
		 * Note that the alert will be displayed many times to the user,
		 * according to the value of the timeout.
		 */
		connectionWarn: true,
		
		/**
		 * @cfg {Integer} connectionWarnTimeout
		 * Amount of time (in millis) after display a connection warning alert.
		 */
		connectionWarnTimeout: 60*1000,
		
		/**
		 * @cfg {String} connectionWarnMsg
		 * Text to display in connection warning alert.
		 */
		connectionWarnMsg: 'Internet connection is unstable'
	},
	
	mode: null,
	firsterror: 0,
	lasterror: 0,
	lastwarn: 0,
	lostfired: false,
	
	constructor: function(config) {
		var me = this;
		me.callParent(config);
		me.mixins.observable.constructor.call(me, config);
	},
	
	connect: function(cfg) {
		var me = this;
		cfg = cfg || {};
		me.initConfig(cfg);
		
		me.mode = undefined;
		if(me.isWSSupported()) {
			me.initWebSocket();
		} else {
			me.initFastPoll();
		}
	},
	
	isWSSupported: function() {
		return ("WebSocket" in window);
	},
	
	initWebSocket: function() {
		var me = this;
		
		me.ws = Ext.create('Sonicle.WebSocket', {
			url: WT.getWsPushUrl(),
			autoReconnect: true,
			autoReconnectInterval: me.getWsReconnectInterval(),
			listeners: {
				open: function(ws) {
					//console.log('ws.open');
					if (me.mode === undefined) {
						me.mode = me.self.MODE_WS;
						me.shutdownFastPoll(); // Ensure fast-poll is down
						me.resetError();
						me.runKeepAliveTask();
					}
				},
				close: function(ws) {
					//console.log('ws.close');
					if (me.mode === undefined) {
						return; // Disabling event!
					} else {
						//console.log('ws status: '+ws.getStatus());
					}
				},
				message: function(ws, msg) {
					//console.log('ws.message');
					me.handleMessages(msg);
				},
				error: function(ws, err) {
					//console.log('ws.error');
					if (me.mode === undefined) {
						// Websocket not available on server!
						// Close it and do failover on fast-poll...
						ws.close();
						me.initFastPoll();
					} else {
						me.shutdownKeepAliveTask();
						me.dumpError();
						me.connectionLostCheck();
						if(ws.getStatus() === ws.OPEN) {
							//console.log('ws status: '+ws.getStatus());
						}
					}
				}
			}
		});
	},
	
	runKeepAliveTask: function() {
		//console.log('runKeepAliveTask');
		var me = this;
		if(!Ext.isDefined(me.kaTask)) {
			me.kaTask = Ext.TaskManager.start({
				run: function () {
					Ext.Ajax.request({
						url: 'keep-alive',
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
	
	initFastPoll: function() {
		var me = this;
		if(!Ext.isDefined(me.fpTask)) {
			me.mode = me.self.MODE_FP;
			me.shutdownKeepAliveTask();
			me.fpTask = Ext.TaskManager.start({
				run: function () {
					//console.log('calling ServerEvents');
					WT.ajaxReq(WT.ID, 'ServerEvents', {
						success: function() {
							me.resetError();
						},
						failure: function(resp) {
							if (resp.status === 0) {
								me.dumpError();
								me.connectionLostCheck();
							}
						},
						callback: function(success, o) {
							if(success) {
								if(o.data) me.handleMessages(o.data);
							}
						}
					});
				},
				interval: me.getFpPollInterval()
			});
		}
	},
	
	shutdownFastPoll: function() {
		var me = this;
		if(Ext.isDefined(me.fpTask)) {
			Ext.TaskManager.stop(me.fpTask);
			delete me.fpTask;
		}
	},
	
	resetError: function() {
		var me = this;
		me.firsterror = 0;
		me.lasterror = 0;
		me.lastwarn = 0;
		me.lostfired = false;
	},
	
	dumpError: function() {
		//console.log('dumpError');
		var me = this;
		if (me.firsterror === 0) me.firsterror = Date.now();
		if (me.lastwarn === 0) me.lastwarn = Date.now();
		me.lasterror = Date.now();
	},
	
	connectionLostCheck: function() {
		//console.log('connectionLostCheck');
		var me = this,
				gelapsed = me.lasterror - me.firsterror,
				welapsed = me.lasterror - me.lastwarn;
		
		if (!me.lostfired && (gelapsed >= me.getConnectionLostTimeout())) {
			me.lostfired = true;
			//console.log('fire connectionlost');
			me.fireEventArgs('connectionlost', [me, gelapsed, me.firsterror]);
		}
		if (me.getConnectionWarn() && !me.warning && (welapsed >= me.getConnectionWarnTimeout())) {
			//console.log('warn connection lost');
			me.warning = true;
			me.fireEventArgs('connectionlostwarn', [me, gelapsed, me.firsterror]);
			WT.warn(me.getConnectionWarnMsg(), {
				config: {
					fn: function() {
						me.lastwarn = Date.now();
						delete me.warning;
					}
				}
			});
		}
	},
	
	handleMessages: function(raw) {
		var obj = Ext.JSON.decode(raw, true);
		if(Ext.isArray(obj)) this.fireEventArgs('receive', [this, obj]);
	},
	
	buildMsg: function(service, action, config) {
		return Ext.JSON.encode(Ext.apply(config||{},{ service: service, action: action }));
	}
});
