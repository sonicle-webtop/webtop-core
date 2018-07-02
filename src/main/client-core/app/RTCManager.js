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
	iceServers: [{url: 'stun:stun.l.google.com:19302'}],
	
	
	vctList: [],
	
	constructor: function(config) {
		var me = this;
		me.callParent(config);
		me.mixins.observable.constructor.call(me, config);
	},
	
	initConnection: function() {
		var me=this;
		me.conn=new Strophe.Connection(me.getBoshUrl());
		me.conn.jingle.setICEServers(me.iceServers);
		
		var manager=me.conn.jingle.manager;
		
		manager.on('incoming',function(session) {
			var type = (session.constructor) ? session.constructor.name : null;
			if (type === 'MediaSession') {
				me.incomingCall(session);
			}
		});
		manager.on('terminated',function(session) {
			console.log("terminated");
			me.terminatedCall(session);
		});
		manager.on('ringing',function(session) {
			me.ringing(session);
		});
		manager.on('peerStreamAdded',function(session,stream) {
			me.remoteStreamAdded(session,stream);
		});
	},
	
	connected: function() {
		return this.conn!=null && this.conn.connected;
	},
	
	connect: function(jid,pass,callback) {
		var me=this;
		me.conn.connect(jid,pass,callback);
	},
	
	startView: function(jidbase, jid, video) {
		var me=this,
			vct=WT.createView(WT.ID, 'view.RTC', {
				viewCfg: {
					dockableConfig: {
						title: WT.res(WT.ID, "rtc.call.tit", (video?WT.res("rtc.video"):WT.res("rtc.audio")), jidbase ),
						iconCls: video?'wt-icon-video-call':'wt-icon-audio-call'
					}
				}
			});
		
		me.vctList.push(vct);
		
		vct.getView().on('viewclose', function(s) {
			if (s.rtcclosing) return;
			
			s.rtcclosing=true;
			//me.conn.jingle.terminate(vct.session.peerID, "Closed by local user", "Closed by user");
			vct.session.end();
			Ext.Array.remove(me.vctList,vct);
			if (vct.session) me.stopStream(vct.session.localStream);
			me.playEnding();
		});
		vct.getView().getRTCComponent().on('controlbuttonclick',function(rtc,action,s) {
			if (action==='hangup') {
				vct.close();
			}
			/*else if (action==='screenshare') {
				me.startScreenSharing(jidbase,jid,vct);
			}*/
		});
		return vct;
	},
	
	startCall: function(jidbase,jid,video) {
		
		var me=this,
			vct=me.startView(jidbase,jid,video);
	
		vct.show(false,function() {
			var rtc=vct.getView().getComponent(0),
				constraints=video?me.getUserMediaConstraints(['audio','video']):me.getUserMediaConstraints(['audio']);
	
			try {
				me.conn.jingle.getUserMedia(constraints, function(err,stream) {
					if (!err) {
						me.conn.jingle.localStream = stream;
						me.attachMediaStream($("#"+rtc.getLocalVideoId()),stream);
	
						me.playDialing();
						var session = me.conn.jingle.initiate(jid);
						session.call = true;
						session.on('change:connectionState', function(sess,state) {
							me.iceConnectionStateChanged(sess,state);
						});
						vct.session=session;
						session.vct=vct;
						session.localStream = stream;
					} else {
						console.log("error getting user media, err="+err);
					}
				});
			} catch (e) {
				console.log("Error getting user media!");
			}
		});
	},
	
	incomingCall: function(session) {
		var me=this,
			video=false;
		
		Ext.each(session.pc.remoteDescription.contents,function(content) {
			if (content.senders==='both' && content.name==='video') video=true;
		});

		session.ring();
		me.startAudioRing();
		me.notifyIncomingSession(session,video);
		
		var toast=WT.toast(
				(video?WT.res('rtc.videocall.incoming.tit'):WT.res('rtc.audiocall.incoming.tit'))+": "+WT.res(WT.ID, 'rtc.call.incoming.text',session.peer.bare),
				{
					buttons: [
						{ glyph: 'xf095@FontAwesome', iconCls: 'wt-color-success', action: 'audio', 
							handler: function() {
								toast.close();
								me.acceptIncomingCall(session);
							}
						}, 
						{ glyph: 'xf03d@FontAwesome', iconCls: 'wt-color-success', action: 'video', 
							handler: function() {
								toast.close();
								me.acceptIncomingCall(session,true);
							}
						}, 
						{ glyph: 'xf095@FontAwesome', iconCls: 'fa-rotate-90 wt-color-alert',
								handler: function() {
									me.stopAudioRing();
									session.decline();
									toast.close();
								}
						}
					],
					autoClose: false
				},{
					listeners: {
						close: function() {
							me.stopAudioRing();
							me.playEnding();
						},
						afterrender: function(t) {
							if (video) t.down('button[action=video]').focus(false, 100);
							else t.down('button[action=audio]').focus(false, 100);
						}
					}					
				}
		);

		session.toast=toast;
		
		
	},
	
	acceptIncomingCall: function(session,video) {
		var me=this,
			vct=me.startView(session.peer.bare,session.peer.full,video);
	
		me.stopAudioRing();
		vct.show(false,function() {
			var rtc=vct.getView().getComponent(0),
				constraints=video?me.getUserMediaConstraints(['audio','video']):me.getUserMediaConstraints(['audio']);
	
			try {
				me.conn.jingle.getUserMedia(constraints, function(err,stream) {
					if (!err) {
						me.conn.jingle.localStream = stream;
						me.attachMediaStream($("#"+rtc.getLocalVideoId()),stream);
						me.attachMediaStream($("#"+rtc.getRemoteVideoId()), session.remoteStream);

						session.addStream(stream);
						session.accept();
						
						session.on('change:connectionState', function(sess,state) {
							me.iceConnectionStateChanged(sess,state);
						});
						vct.session=session;
						session.vct=vct;
						session.localStream = stream;
						
					} else {
						session.decline();
						console.log("error getting user media, err="+err);
					}
				});
			} catch (e) {
				console.log("Error getting user media!");
			}
		});
	},
	
	terminatedCall: function(session) {
		var me=this;
		console.log("terminated session "+session);
		if (session.vct) session.vct.close();
		if (session.toast) session.toast.close();
		me.stopAudioRing();
	},	
	
	startScreenSharing: function(jidbase,jid,vct) {
		
		var me=this;
	
		try {
			me.conn.jingle.getScreenMedia(function(err,stream) {
				if (!err) {
					me.conn.jingle.localStream = stream;
					
					var browser = me.conn.jingle.RTC.browserDetails.browser;
					var browserVersion = me.conn.jingle.RTC.browserDetails.version;
					var constraints;

					if ((browserVersion < 33 && browser === 'firefox') || browser === 'chrome') {
					   constraints = {
						  mandatory: {
							 'OfferToReceiveAudio': false,
							 'OfferToReceiveVideo': false
						  }
					   };
					} else {
					   constraints = {
						  'offerToReceiveAudio': false,
						  'offerToReceiveVideo': false
					   };
					}

					var session = me.conn.jingle.initiate(jid, undefined, constraints);
					session.call = false;

					session.on('change:connectionState', function(sess,state) {
						me.iceConnectionStateChanged(sess,state);
					});
					if (vct) {
						vct.screenSession=session;
						session.vct=vct;
					}
					session.localStream = stream;
				} else {
					console.log("error getting user media, err="+err);
				}
			});
		} catch (e) {
			console.log("Error getting user media!");
		}
	},
	
	ringing: function(session) {
		var me=this;
		me.startAudioRing();
	},
	
	notifyIncomingSession: function(session,video) {
		WT.showDesktopNotification(WT.ID,{
			title: (video?WT.res('rtc.videocall.incoming.tit'):WT.res('rtc.audiocall.incoming.tit')),
			body: WT.res(WT.ID, 'rtc.call.incoming.text',session.peer.bare)
		});
	},
	
	startAudioRing: function() {
		Sonicle.Sound.play('wt-call-ringing', { loop: true });
	},
	
	stopAudioRing: function() {
		Sonicle.Sound.stop('wt-call-ringing');
	},
	
	playDialing: function() {
		Sonicle.Sound.play('wt-call-dialing');
	},
	
	playEnding: function() {
		Sonicle.Sound.play('wt-call-ending');
	},
	
	remoteStreamAdded: function(session,stream) {
		var me=this;
		
		session.remoteStream=stream;
		if (session.vct) {
			me.stopAudioRing();
			var rtc=session.vct.getView().getComponent(0);
			me.attachMediaStream($("#"+rtc.getRemoteVideoId()), stream);
		}
			
	},
	
	iceConnectionStateChanged: function(session,state) {
		console.log("ice connection state change: "+state);
		if (state==='interrupted') {
			if (session.vct) session.vct.close();
			if (session.toast) session.toast.close();
		}
	},
	
	/*stopLocalStream: function() {
		var me=this;
		
		if (me.conn.jingle.localStream) me.stopStream(me.conn.jingle.localStream);
	},*/
	
	stopStream: function(stream) {
		stream.getAudioTracks().forEach(function(track) {
			track.stop();
		});
		stream.getVideoTracks().forEach(function(track) {
			track.stop();
		});
	},
	
	attachMediaStream: function(element, stream) {
		var el = (element instanceof jQuery) ? element.get(0) : element;
		el.srcObject = stream;

		$(element).show();
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
