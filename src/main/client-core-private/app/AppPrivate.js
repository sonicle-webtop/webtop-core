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
Ext.define('Sonicle.webtop.core.app.AppPrivate', {
	extend: 'Sonicle.webtop.core.app.AppBase',
	requires: [
		'Sonicle.webtop.core.app.WTPrivate'
	],
	uses: [
		'Sonicle.DesktopNotificationMgr',
		'Sonicle.PageActivityMonitor',
		'Sonicle.Sound',
		'Sonicle.webtop.core.app.DescriptorPrivate',
		'Sonicle.webtop.core.app.PushManager',
		'Sonicle.webtop.core.sdk.Service'
	],
	views: [
		Ext.String.format('WTA.view.main.{0}', WTS.layoutClassName)
	],
	refs: {
		viewport: 'viewport'
	},
	autoCreateViewport: false,
	
	currentService: null,
	
	kaTask: null,
	seTask: null,
	
	constructor: function() {
		var me = this;
		WT.app = me;
		me.callParent(arguments);
	},
	
	launch: function() {
		var me = this,
				SoSnd = Sonicle.Sound;
		
		SoSnd.setPath('resources/com.sonicle.webtop.core/0.0.0/resources/sounds/');
		SoSnd.add([
			{alias: 'wt-im-connect', name: 'im-connect'},
			{alias: 'wt-im-disconnect', name: 'im-disconnect'},
			{alias: 'wt-im-receive', name: 'im-receive'},
			{alias: 'wt-im-send', name: 'im-send'},
			{alias: 'wt-call-ringing', name: 'call-ringing'},
			{alias: 'wt-call-dialing', name: 'call-dialing'},
			{alias: 'wt-call-ending', name: 'call-ending'}
		]);
		
		Ext.iterate(WTS.roles, function(role) {
			me.roles[role] = true;
		});
		
		// Initialize services' descriptors
		me.initDescriptors();
		Ext.iterate(WTS.services, function(obj, idx) {
			var desc = me.descriptors.get(obj.id);
			if (!desc) Ext.raise('This should never happen (famous last words)');
			
			desc.setOrder(idx);
			//desc.setMaintenance(obj.maintenance);
			desc.setServiceClassName(obj.serviceCN);
			desc.setServiceVarsClassName(obj.serviceVarsCN);
			desc.setUserOptions(obj.userOptions);
			desc.setPortletClassNames(obj.portletCNs);
			
			me.services.push(obj.id);
		});
		
		//TODO: portare il metodo onRequiresLoaded direttamente qui!
		me.onRequiresLoaded.call(me);
	},
	
	onRequiresLoaded: function() {
		var me = this,
				sids = me.getServices(),
				cdesc, vp, vpc;
		
		// Instantiates core service
		cdesc = me.getDescriptor(sids[0]);
		if (!cdesc.getInstance()) Ext.raise('Unable to instantiate core');
		
		// Creates main viewport
		vp = me.viewport = me.getView(me.views[0]).create({
			servicesCount: sids.length-1 //TODO: calcolare il numero di servizi visibili
		});
		vpc = me.viewport.getController();
		
		// Instantiates remaining services
		for (var i=1; i<sids.length; i++) {
			me.getDescriptor(sids[i]).getInstance();
		}
		
		Ext.iterate(sids, function(sid) {
			var desc = me.getDescriptor(sid);
			if (desc.initService()) {
				var svc = desc.getInstance();
				vpc.addServiceButton(desc);
				if (svc.hasNewActions()) vpc.addNewActions(svc.getNewActions());
			}
		});
		
		var llinks = Ext.JSON.decode(WT.getVar('wtLauncherLinks'), true);
		if (Ext.isArray(llinks)) {
			Ext.iterate(llinks, function(link) {
				if (!Ext.isObject(link)) return;
				if (!link.hasOwnProperty('href') || !link.hasOwnProperty('icon')) return;
				vpc.addLinkButton(link);
			});
		}
		
		// Sets startup service
		var deflt = me.findDefaultService();
		me.activateService(deflt);
		
		// If necessary, show whatsnew
		if (WT.getVar('isWhatsnewNeeded')) {
			vpc.showWhatsnew(false);
		}
		
		WTA.PushManager.setUrl(me.pushUrl + '/' + WT.getSessionId());
		//WTA.PushManager.setEventsDebug(true);
		WTA.PushManager.on({
			receive: function(s,messages) {
				Ext.each(messages, function(msg) {
					if (msg && msg.service) {
						var svc = me.getService(msg.service);
						if(svc) svc.handleMessage(msg);
					}
				});
			},
			connectionlost: function() {
				WT.showBadgeNotification(WT.ID, {
					tag: 'connlost',
					title: WT.res('not.conn.lost.tit'),
					body: WT.res('not.conn.lost.body')
				});
			},
			connectionrestored: function() {
				WT.showBadgeNotification(WT.ID, {
					tag: 'connrestored',
					title: WT.res('not.conn.restored.tit'),
					body: WT.res('not.conn.restored.body')
				});
			},
			servererror: function(s, status) {
				if (status === 401) {
					WT.confirm(WT.res('warn.conn.forbidden'), function(bid) {
						if (bid === 'ok') WT.logout();
					}, me, {
						itemId: 'pushservererror',
						title: WT.res('warning'),
						icon: Ext.Msg.WARNING,
						buttons: Ext.Msg.OK,
						config: {
							buttonText: {
								ok: WT.res('word.continue')
							}
						}
					});
				} else if (status >= 500) {
					WT.confirm(WT.res('warn.conn.error', status), function(bid) {
						if (bid === 'ok') WTA.PushManager.connect();
					}, me, {
						itemId: 'pushservererror',
						title: WT.res('warning'),
						icon: Ext.Msg.WARNING,
						buttons: Ext.Msg.OK,
						config: {
							buttonText: {
								ok: WT.res('word.reconnect')
							}
						}
					});
				}
			}
		});
		WTA.PushManager.connect();
		/*
		Sonicle.PageActivityMonitor.on('change', function(s, idle) {
			console.log('ActivityMonitor: ' + (idle ? 'user is idle' : 'user is working'));
		});
		*/
		Sonicle.PageActivityMonitor.start();
		Sonicle.DesktopNotificationMgr.on('requestpermission', function() {
			WT.msg(WT.res('info.browser.permission.notification'));
		});
		
		me.hideLoadingLayer();
	},
	
	createServiceDescriptor: function(cfg) {
		return Ext.create('WTA.DescriptorPrivate', cfg);
	},
	
	getViewportController: function() {
		return this.viewport.getController();
	},
	
	connWarnTask: function(stop) {
		var me = this;
		if (stop === true) {
			if (Ext.isDefined(me.cwTask)) {
				Ext.TaskManager.stop(me.cwTask);
				delete me.cwTask;
				
				WT.showBadgeNotification(WT.ID, {
					tag: 'connrestored',
					title: WT.res('not.conn.restored.tit'),
					body: WT.res('not.conn.restored.body')
				});
			}
		} else {
			if (!Ext.isDefined(me.cwTask)) {
				me.cwTask = Ext.TaskManager.start({
					run: function(count) {
						WT.showBadgeNotification(WT.ID, {
							tag: 'connlost',
							title: WT.res('not.conn.lost.tit'),
							body: WT.res('not.conn.lost.body')
						});
					},
					interval: 10*1000,
					fireOnStart: false
				});
			}
		}
	},
	
	logout: function() {
		WTA.PushManager.disconnect();
		WT.logout();
	},
	
	/**
	 * Activates (shows) specified service.
	 * @param {String} id The service ID.
	 */
	activateService: function(id) {
		var me = this,
				vpc = me.getViewport().getController(),
				inst = me.getService(id);
				
		if (!inst) return;
		vpc.addService(inst);
		me.currentService = id;
		if (vpc.activateService(inst)) {
			inst.activationCount++;
			Ext.state.Manager.set(WT.buildStateId('lastservice'), id);
			inst.fireEvent('activate');
		}
	},
	
	hideLoadingLayer: function() {
		Ext.fly('wt-loading').animate({
			to: {opacity: 0},
			duration: 200,
			remove: true
		});
		Ext.fly('wt-loading-mask').animate({
			to: {opacity: 0.4},
			easing: 'bounceOut',
			duration: 1000,
			remove: true
		});
		
		/*
		var el = Ext.get('wt-loading'),
				box = el.getBox();
		el.animate({
			to: {opacity: 0},
			duration: 2500,
			remove: true
		});
		Ext.get('wt-loading-mask').animate({
			to: {
				x: box.x,
				y: box.y,
				width: box.width,
				height: box.height,
				opacity: 0
			},
			easing: 'bounceOut',
			duration: 5000,
			remove: true
		});
		*/
	
		/*
		     var loadingMask = Ext.get('loading-mask');
     var loading = Ext.get('loading');

     //  Hide loading message
     loading.fadeOut({ duration: 0.2, remove: true });

     //  Hide loading mask
     loadingMask.setOpacity(0.9);
     loadingMask.shift({
          xy: loading.getXY(),
          width: loading.getWidth(),
          height: loading.getHeight(),
          remove: true,
          duration: 1,
          opacity: 0.1,
          easing: 'bounceOut'
     });
		*/
	},
	
	findDefaultService: function() {
		var me = this,
				arr = [WT.getVar('startupService'), Ext.state.Manager.get(WT.buildStateId('lastservice'))],
				desc;
		
		for (var i=0; i<arr.length; i++) {
			if (!Ext.isEmpty(arr[i])) {
				desc = me.getDescriptor(arr[i]);
				if (desc && desc.isServiceInited()) return arr[i]; 
			}
		}
		return WT.ID;
	},
	
	/**
	 * Returns the Service API interface.
	 * @param {String} id The service ID.
	 * @returns {Object} The service API object or null if service is not valid.
	 */
	getServiceApi: function(id) {
		var svc = this.getService(id);
		return svc ? svc.getApiInstance() : null;
	},
	
	log: function(msg, level) {
		if (arguments.length === 1) {
			level = 'debug';
		}
		WT.ajaxReq(WT.ID, 'LogMessage', {
			timeout: 10*1000,
			params: {
				level: level,
				message: msg
			}
		});
	}
});
