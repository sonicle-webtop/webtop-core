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
Ext.define('Sonicle.webtop.core.app.RTCManager', {
	alternateClassName: 'WTA.RTCManager',
	singleton: true,
	mixins: [
		'Ext.mixin.Observable'
	],
	
	config: {
		
		/**
		 * @cfg {String} boshUrl
		 * Url of BOSH enabled jabber instance.
		 */
		boshUrl: window.location.origin+"/http-bind/",
		
	},
	
	conn: null,
	RTC: null,
	ice_config: {iceServers: [{url: 'stun:stun.l.google.com:19302'}]},
	
	constructor: function(config) {
		var me = this;
		me.callParent(config);
		me.mixins.observable.constructor.call(me, config);
	},
	
	initConnection: function() {
		var me=this;
		RTC=setupRTC();
		if (RTC) {
			me.conn=new Strophe.Connection(me.getBoshUrl());
			me.conn.jingle.ice_config = me.ice_config;
			me.conn.jingle.pc_constraints = RTC.pc_constraints;
			RTCPeerconnection = RTC.peerconnection;
			if (RTC.browser == 'firefox') {
				me.conn.jingle.media_constraints.mandatory.MozDontOfferDataChannel = true;
			}
			/*$(document).bind('mediaready.jingle', function(event,stream) {				
				console.log("local media ready");
				var lvel=$("#"+me.lvid);
				lvel.muted=true;
				lvel.volume=0;
				me.conn.jingle.localStream = stream;
				//RTC.attachMediaStream(lvel, stream);				
			});*/
			//$(document).bind('remotestreamadded.jingle', function(event, data, sid) {
			//	console.log("remote stream added");
			//	RTC.attachMediaStream($("#"+me.rvid), data.stream)
			//});
			$(document).bind('remotetrackadded.jingle', function(event, data, sid) {
				console.log("remote track added");
				if (me.rvid) RTC.attachMediaStream($("#"+me.rvid), data.streams[0])
			});
			$(document).bind('iceconnectionstatechange.jingle', function(event, sid, sess) {
				var state=sess.peerconnection.iceConnectionState;
				console.log("remote ice connaction state changed to "+state);
				if (state==="disconnected")
					if (me.vct) me.vct.close();
			});
			$(document).bind('callincoming.jingle', function(event, sid) {
				console.log("incoming call - sid="+sid);
				me.answerCall(sid);
			});
			$(document).bind('callterminated.jingle', function(event, sid) {
				console.log("call terminated - sid="+sid);
				if (me.vct) me.vct.close();
			});
		}
	},
	
	connect: function(jid,pass,callback) {
		var me=this;
		me.conn.connect(jid,pass,callback);
		me._fakeStartCall("a@b");
	},
	
	startView: function() {
		var me=this,
			vct=WT.createView(WT.ID, 'view.RTC');
		
		vct.getView().on('viewclose', function() {
			if (vct.session) {
				console.log("closing session");
				vct.session.terminate("closed by user");
				//me.stopStream(me.conn.jingle.localStream);
			}
			else console.log("closing window with no session");
		});
		vct.getView().getRTCComponent().on('controlbuttonclick',function(rtc,action,s) {
			if (action==='hangup') {
				vct.close();
			}
		});
		return vct;
	},
	
	startCall: function(jid,video) {
		var me=this;
		
		me.vct=me.startView();
		me.vct.show(false,function() {
			var rtc=me.vct.getView().getComponent(0);
			me.lvid=rtc.getLocalVideoId(),
			me.rvid=rtc.getRemoteVideoId();
					
			var constraints=video?me.getUserMediaConstraints(['audio','video']):me.getUserMediaConstraints(['audio'])
			
			try {
				RTC.getUserMedia(constraints,function(stream) {
					console.log("local media ready");
					me.conn.jingle.localStream = stream;
					RTC.attachMediaStream($("#"+me.lvid), stream);

					me.vct.session=me.conn.jingle.initiate(jid,me.conn.jid); 
				},function(error) {
					console.error('GUM failed: ', e);
					
				});
			} catch (e) {
				console.error('GUM failed: ', e);
			}
			
		});
		
	},
	
	_fakeStartCall: function(jid,cb) {
		
		if (this._fakeStartCallDone) return;
		
		this._fakeStartCallDone=true;
		
		var me=this;
			constraints=me.getUserMediaConstraints(['audio','video'])
			
		try {
			RTC.getUserMedia(constraints,function(stream) {
				console.log("local media ready");
				me.conn.jingle.localStream = stream;

				me.conn.jingle.initiate(jid,me.conn.jid);
				//me.stopStream(stream);
				
				if (cb!=null) cb();
			},function(error) {
				console.error('GUM failed: ', e);

			});
		} catch (e) {
			console.error('GUM failed: ', e);
		}

	},
	
	stopStream: function(stream) {
		stream.getAudioTracks().forEach(function(track) {
			track.stop();
		});
		stream.getVideoTracks().forEach(function(track) {
			track.stop();
		});
	},
	
	answerCall: function(sid) {
		var me=this,
			sess=me.conn.jingle.sessions[sid];
		
		var toast=WT.toast({
			 html: sess.peerjid,
			 title: WT.res('Incoming call'),
			 width: 300,
			 align: 'br',
			 timeout: 20000,
			 buttons: [
				{ 
					text: 'Answer', 
					handler: function() {
						toast.close();
						console.log("answering sid "+sid);
						me.vct=me.startView();
						me.vct.show(false,function() {
							var rtc=me.vct.getView().getComponent(0);
							me.lvid=rtc.getLocalVideoId(),
							me.rvid=rtc.getRemoteVideoId();

							var constraints=me.getUserMediaConstraints(['audio','video'])
							try {
								RTC.getUserMedia(constraints,function(stream) {
									console.log("local media ready");
									me.conn.jingle.localStream = stream;
									RTC.attachMediaStream($("#"+me.lvid), stream);
									RTC.attachMediaStream($("#"+me.rvid), sess.remoteStream);


									console.log("sendAnswer")
									sess.sendAnswer();
									console.log("accept")
									sess.accept();
									me.vct.session=sess;
									
								},function(error) {
									console.error('GUM failed: ', e);

								});
							} catch (e) {
								console.error('GUM failed: ', e);
							}
						});

						
					}
				},{ 
					text: 'Fuck you',
					handler: function() {
						sess.terminate();
						toast.close();
					}
				}
			 ]
		});
		
		
	},
	
	
	getUserMediaConstraints: function(um, resolution, bandwidth, fps) {
		var constraints = {audio: false, video: false};

		if (um.indexOf('video') >= 0) {
			constraints.video = {mandatory: {}};// same behaviour as true
		}
		if (um.indexOf('audio') >= 0) {
			constraints.audio = {};// same behaviour as true
		}
		if (um.indexOf('screen') >= 0) {
			constraints.video = {
				"mandatory": {
					"chromeMediaSource": "screen"
				}
			};
		}

		if (resolution && !constraints.video) {
			constraints.video = {mandatory: {}};// same behaviour as true
		}
		// see https://code.google.com/p/chromium/issues/detail?id=143631#c9 for list of supported resolutions
		switch (resolution) {
		// 16:9 first
		case '1080':
		case 'fullhd':
			constraints.video.mandatory.minWidth = 1920;
			constraints.video.mandatory.minHeight = 1080;
			break;
		case '720':
		case 'hd':
			constraints.video.mandatory.minWidth = 1280;
			constraints.video.mandatory.minHeight = 720;
			break;
		case '360':
			constraints.video.mandatory.minWidth = 640;
			constraints.video.mandatory.minHeight = 360;
			break;
		case '180':
			constraints.video.mandatory.minWidth = 320;
			constraints.video.mandatory.minHeight = 180;
			break;
			// 4:3
		case '960':
			constraints.video.mandatory.minWidth = 960;
			constraints.video.mandatory.minHeight = 720;
			break;
		case '640':
		case 'vga':
			constraints.video.mandatory.minWidth = 640;
			constraints.video.mandatory.minHeight = 480;
			break;
		case '320':
			constraints.video.mandatory.minWidth = 320;
			constraints.video.mandatory.minHeight = 240;
			break;
		default:
			if (navigator.userAgent.indexOf('Android') != -1) {
				constraints.video.mandatory.minWidth = 320;
				constraints.video.mandatory.minHeight = 240;
				constraints.video.mandatory.maxFrameRate = 15;
			}
			break;
		}

		// take what is configured and try not to be more intelligent
		if (constraints.video.minWidth) constraints.video.maxWidth = constraints.video.minWidth;
		if (constraints.video.minHeight) constraints.video.maxHeight = constraints.video.minHeight;

		if (bandwidth) { // doesn't work currently, see webrtc issue 1846
			if (!constraints.video) constraints.video = {mandatory: {}};//same behaviour as true
			constraints.video.optional = [{bandwidth: bandwidth}];
		}
		if (fps) { // for some cameras it might be necessary to request 30fps
			// so they choose 30fps mjpg over 10fps yuy2
			if (!constraints.video) constraints.video = {mandatory: {}};// same behaviour as tru;
			constraints.video.mandatory.minFrameRate = fps;
		}
		
		return constraints;

	}
	
});
