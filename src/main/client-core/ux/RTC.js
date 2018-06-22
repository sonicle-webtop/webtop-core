/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.webtop.core.ux.RTC', {
	alternateClassName: 'WTA.ux.RTC',
	extend: 'Ext.Component',
	alias: ['widget.sortc'],
	uses: [
		'Sonicle.FullscreenMgr'
	],
	
	notSupportedText: 'Sorry, Web RTC is not available in your browser!',
	
	videoWrapCls: 'so-'+'rtc-videoWrap',
	localVideoCls: 'so-'+'rtc-lvideo',
	remoteVideoCls: 'so-'+'rtc-rvideo',
	controlBarCls: 'so-'+'rtc-controlbar',
	hangUpButtonCls: 'so-'+'rtc-button-hangup',
	screenShareButtonCls: 'so-'+'rtc-button-screenshare',
	fullscreenButtonCls: 'so-'+'rtc-button-fullscreen',
	
	renderTpl: [
		'<div id="{id}-videoWrap" data-ref="videoWrap" class="{videoWrapCls}">',
			'<video id="{id}-rvideo" data-ref="rvideo" class="{rvideoCls}" autoplay="true">{notSupportedText}</video>',
			'<video id="{id}-lvideo" data-ref="lvideo" class="{lvideoCls}" muted="true" volume="0" autoplay="true">{notSupportedText}</video>',
			'<div id="{id}-controlBar" data-ref="controlBar" class="{controlBarCls}">',
				'<div>',
					'<div class="{controlBarCls}-button {hangUpButtonCls}"></div>',
					'<div class="{controlBarCls}-button {screenShareButtonCls}"></div>',
					'<div class="{controlBarCls}-button {fullscreenButtonCls}"></div>',
				'</div>',
			'</div>',
		'</div>'
	],
	childEls: ['videoWrap', 'rvideo', 'lvideo', 'controlBar'],
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		Sonicle.FullscreenMgr.on('change', me.onFullscreenChange, me);
	},
	
	destroy: function() {
		var me = this;
		Sonicle.FullscreenMgr.un('change', me.onFullscreenChange);
		me.callParent();	
	},
	
	onResize: function(w, h, ow) {
		var me = this,
				cbcls = me.controlBarCls,
				tcls = cbcls + '-top',
				bcls = cbcls + '-bottom';
		me.callParent(arguments);
		if (me.controlBar) {
			if (w < 640 && (ow === undefined || ow >= 640)) {
				me.cbLastCls = tcls;
				me.controlBar.replaceCls(bcls, tcls);
			} else if (w >= 640 && (ow === undefined || ow < 640)) {
				me.cbLastCls = bcls;
				me.controlBar.replaceCls(tcls, bcls);
			}
		}	
	},

	onRender: function() {
		var me = this, bEl;
		me.callParent();
		if (me.controlBar) {
			bEl = me.controlBar.down('.'+me.hangUpButtonCls);
			if (bEl) bEl.on('click', me.onHangUpClick, me);
			bEl = me.controlBar.down('.'+me.screenShareButtonCls);
			if (bEl) bEl.on('click', me.onScreenShareClick, me);
			bEl = me.controlBar.down('.'+me.fullscreenButtonCls);
			if (bEl) bEl.on('click', me.onFullscreenClick, me);
		}
	},
	
	onFullscreenChange: function(s, isFullscreen) {
		var me = this,
				cbcls = me.controlBarCls,
				tcls = cbcls + '-top',
				bcls = cbcls + '-bottom';
		if (me.controlBar) {
			if (isFullscreen) {
				me.controlBar.replaceCls(tcls, bcls);
			} else {
				me.controlBar.removeCls(tcls);
				me.controlBar.removeCls(bcls);
				me.controlBar.addCls(me.cbLastCls);
			}
		}
	},
	
	initRenderData: function() {
		var me = this;
		return Ext.apply(me.callParent(), {
			videoWrapCls: me.videoWrapCls,
			rvideoCls: me.remoteVideoCls,
			lvideoCls: me.localVideoCls,
			controlBarCls: me.controlBarCls,
			hangUpButtonCls: me.hangUpButtonCls,
			screenShareButtonCls: me.screenShareButtonCls,
			fullscreenButtonCls: me.fullscreenButtonCls,
			notSupportedText: me.notSupportedText
		});
	},
	
	getLocalVideoId: function() {
		return this.lvideo ? this.lvideo.id : null;
	},
	
	getRemoteVideoId: function() {
		return this.rvideo ? this.rvideo.id : null;
	},
	
	setLocalVideoUrl: function(url) {
		if (this.lvideo) this.lvideo.set({src: url});
	},
	
	setRemoteVideoUrl: function(url) {
		if (this.rvideo) this.rvideo.set({src: url});
	},
	
	onHangUpClick: function(s) {
		this.fireEvent('controlbuttonclick', this, 'hangup', s);
	},
	
	onScreenShareClick: function(s) {
		this.fireEvent('controlbuttonclick', this, 'screenshare', s);
	},
	
	onFullscreenClick: function(s) {
		var me = this;
		if (me.fireEvent('controlbuttonclick', me, 'fullscreen', s)) {
			Sonicle.FullscreenMgr.request(me.videoWrap.dom);
		}
	}
});
