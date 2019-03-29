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
Ext.define('Sonicle.webtop.core.app.WTPrivate', {
	override: 'Sonicle.webtop.core.app.WT',
	
	palette: [
		'AC725E','D06B64','F83A22','FA573C','FF7537','FFAD46','FAD165','FBE983',
		'4986E7','9FC6E7','9FE1E7','92E1C0','42D692','16A765','7BD148','B3DC6C',
		'9A9CFF','B99AFF','A47AE2','CD74E6','F691B2','CCA6AC','CABDBF','C2C2C2',
		'FFFFFF'
	],
	
	getColorPalette: function() {
		return this.palette;
	},
	
	logout: function() {
		window.location = 'logout';
	},
	
	/**
	 * Activates (switch focus) the specified service.
	 * @param {String} sid The service ID.
	 */
	activateService: function(sid) {
		this.getApp().activateService(sid);
	},
	
	/**
	 * Returns the ID of currently active (displayed) service.
	 * @returns {String}
	 */
	getActiveService: function() {
		return this.getApp().viewport.getController().active;
	},
	
	/**
	 * Returns the Service API interface.
	 * @param {String} sid The service ID.
	 * @returns {Object} The service API object.
	 */
	getServiceApi: function(sid) {
		return this.getApp().getServiceApi(sid);
	},
	
	/**
	 * Returns `true` if this current profile is the SysAdmin.
	 * @returns {Boolean}
	 */
	isSysAdmin: function() {
		return this.hasRole('SYSADMIN');
	},
	
	/**
	 * Returns `true` if this current profile has admin rights.
	 * @returns {Boolean}
	 */
	isAdmin: function() {
		return this.hasRole('WTADMIN');
	},
	
	/**
	 * @deprecated
	 */
	isWTAdmin: function() {
		return this.isAdmin();
	},
	
	/**
	 * Returns `true` if this current profile has been impersonated.
	 * @returns {Boolean}
	 */
	isProfileImpersonated: function() {
		return this.hasRole('IMPERSONATED_USER');
	},
	
	isProfileActingAsAdmin: function() {
		return this.isWTAdmin() || this.isProfileImpersonated();
	},
	
	hasRole: function(role) {
		return this.getApp().hasRoles([role])[0];
	},
	
	hasRoles: function(roles) {
		return this.getApp().hasRoles(roles);
	},
	
	hasAllRoles: function(roles) {
		return this.getApp().hasAllRoles(roles);
	},
	
	/**
	 * Checks against a resource if specified action is allowed.
	 * @param {String} [sid] The service ID.
	 * @param {String} resource The resource name.
	 * @param {String} action The action name.
	 * @return {Boolean} 'True' if action is allowed, 'False' otherwise.
	 */
	isPermitted: function(sid, resource, action) {
		if (arguments.length === 2) {
			action = resource;
			resource = sid;
			sid = WT.ID;
		}
		var svc = this.getApp().getService(sid);
		if (!svc) Ext.Error.raise('Service not found ['+sid+']');
		return svc.isPermitted(resource, action);
	},
	
	/*
	isSysAdmin: function() {
		return this.isPermitted('SYSADMIN', 'ACCESS');
	},
	
	isWTAdmin: function() {
		return this.isPermitted('WTADMIN', 'ACCESS');
	},
	*/
	
	/**
	 * Creates a displayable view.
	 * @param {String} sid The service ID.
	 * @param {String} name The class name or alias.
	 * @param {Object} opts
	 * @param {String} opts.tag
	 * @param {Object} opts.viewCfg
	 * @param {Object} opts.containerCfg
	 * @returns {Ext.window.Window} The container containing WebTop view.
	 */
	createView: function(sid, name, opts) {
		opts = opts || {};
		var app = this.getApp(),
				desc = app.getDescriptor(sid);
		if (!desc) Ext.Error.raise('Service descriptor not found ['+sid+']');
		return app.getViewportController().createServiceView(desc, name, opts);
	},
	
	/**
	 * 
	 * @param {String} sid The service ID.
	 * @param {String} tag
	 * @returns {Boolean}
	 */
	hasView: function(sid, tag) {
		var app = this.getApp(),
				desc = app.getDescriptor(sid);
		if (!desc) Ext.Error.raise('Service descriptor not found ['+sid+']');
		return app.getViewportController().hasServiceView(desc, tag);
	},
	
	/**
	 * 
	 * @param {String} sid The service ID.
	 * @param {String} tag
	 * @returns {Ext.window.Window} The container containing WebTop view.
	 */
	getView: function(sid, tag) {
		var app = this.getApp(),
				desc = app.getDescriptor(sid);
		if (!desc) Ext.Error.raise('Service descriptor not found ['+sid+']');
		return app.getViewportController().getServiceView(desc, tag);
	},
	
	/**
	 * Displays a notification to the user. Firstly it tries with a desktop
	 * notification, if disabled or not available then a badge notification
	 * will be used instead.
	 * @param {String} sid The service ID.
	 * @param {Boolean} fallback
	 * @param {Object} notification The notification object.
	 * @param {Object} [opts] Config options.
	 */
	showNotification: function(sid, fallback, notification, opts) {
		var me = this, ret;
		ret = me.showDesktopNotification(sid, notification, opts);
		if (fallback && ret !== undefined) {
			return ret;
		} else {
			return me.showBadgeNotification(sid, notification, opts);
		}
	},
	
	/**
	 * Checks and if necessary display an authorization 
	 * request for using desktop notifications.
	 */
	checkDesktopNotificationAuth: function() {
		var dn = WT.getVar('desktopNotification');
		if (dn === 'always' || dn === 'auto') {
			Sonicle.DesktopNotificationMgr.ensureAuthorization();
		}
	},
	
	/**
	 * Shows a desktop notification using browser.
	 * @param {String} sid The service ID.
	 * @param {Object} notification The notification object.
	 * 
	 * This object is defined as:
	 * 
	 * @param {String} [notification.tag] ID string that allows to link 
	 * notifications together in order to avoid flooding of similar entries.
	 * If not specified, a unique ID will be generated.
	 * @param {String} notification.title The title.
	 * @param {String} [notification.body] The body.
	 * @param {Mixed} [notification.data] Custom data passed in case of callback.
	 * 
	 * @param {Object} [opts] Config options.
	 * 
	 * @param {Number} [opts.autoClose=5000] Auto close timeout in millis.
	 * @param {Boolean} [opts.callbackService=false]
	 * 
	 * @returns {Object} A wrapper containing a close() method to hide the notification. 
	 */
	showDesktopNotification: function(sid, notification, opts) {
		opts = opts || {};
		var PMgr = Sonicle.PageMgr,
				DeskNotifMgr = Sonicle.DesktopNotificationMgr,
				dn = WT.getVar('desktopNotification'),
				callbk = Ext.isBoolean(opts.callbackService) ? opts.callbackService : false,
				cbFn = null,
				svc, callbk;
		
		if (dn === 'always' || (dn === 'auto' && PMgr.isHidden())) {
			if (Ext.isEmpty(notification.title)) Ext.Error.raise('Title is mandatory');
			svc = this.getApp().getService(sid);
			if (!svc) Ext.Error.raise('Service not found ['+sid+']');
			
			if (callbk) {
				cbFn = function(ntf) {
					svc.notificationCallback('desktop', ntf.tag, ntf.data);
				};
			}
			
			//ico = Ext.isIE ? 'wt.ico' : 'wt_32.png';
			return DeskNotifMgr.notify(notification.title, {
				tag: !Ext.isEmpty(notification.tag) ? notification.tag : Sonicle.UUID.v1(),
				icon: WTF.globalImageUrl('wt.ico'),
				body: notification.body || svc.getName(),
				data: notification.data,
				autoClose: opts.autoClose || 5000,
				clickCallback: cbFn
			});
		}
		return undefined;
	},
	
	/**
	 * 
	 * @param {String} sid The service ID.
	 * @param {Object} notification The notification object.
	 * 
	 * This object is defined as:
	 * 
	 * @param {String} [notification.tag] ID string that allows to link 
	 * notifications together in order to avoid flooding of similar entries.
	 * If not specified, a unique ID will be generated.
	 * @param {String} notification.title The title.
	 * @param {String} [notification.body] The body.
	 * @param {Mixed} [notification.data] Custom data passed back during callbacks.
	 * 
	 * @param {Object} [opts] Config options.
	 * 
	 * This object may contain any of the following properties:
	 * 
	 * @param {Boolean} [opts.autoClear=true] Specifies whether to clear the 
	 * notification on double-click on it. Default to `true`.
	 * @param {Boolean} [opts.callbackService=false] Specifies whether to notify 
	 * the service on double-click on the notification. Default to `false`.
	 */
	showBadgeNotification: function(sid, notification, opts) {
		opts = opts || {};
		var svc = this.getApp().getService(sid),
				autCle = Ext.isBoolean(opts.autoClear) ? opts.autoClear : true,
				callbk = Ext.isBoolean(opts.callbackService) ? opts.callbackService : false;
		
		if (Ext.isEmpty(notification.tag)) Ext.Error.raise('Tag is mandatory');
		if (Ext.isEmpty(notification.title)) Ext.Error.raise('Title is mandatory');
		if (!svc) Ext.Error.raise('Service not found ['+sid+']');
		return this.getApp().viewport.getController().showBadgeNotification(svc, {
			tag: notification.tag,
			serviceId: sid,
			iconCls: Ext.isEmpty(notification.iconCls) ? svc.cssIconCls('service', 'm') : notification.iconCls,
			title: notification.title,
			body: notification.body,
			data: Ext.isDefined(notification.data) ? Ext.JSON.encode(notification.data) : null,
			autoClear: autCle,
			callbackService: callbk
		});
	},
	
	/**
	 * 
	 * @param {String} sid The service ID.
	 * @param {String} notificationTag The notification identifier.
	 */
	clearBadgeNotification: function(sid, notificationTag) {
		return this.getApp().viewport.getController().clearBadgeNotification(sid, notificationTag);
	},
	
	/**
	 * Returns the layout in use.
	 * Value is taken from core variable 'layout'.
	 * @returns {String} The layout value.
	 */
	getLayout: function() {
		return WT.getVar('layout');
	},
	
	/**
	 * Returns the scale in use, or 'small' if missing.
	 * Value is taken from core variable 'viewportHeaderScale'.
	 * @returns {String} The scale value (small, medium, large).
	 */
	getHeaderScale: function() {
		return WT.getVar('viewportHeaderScale') || 'small';
	},
	
	/**
	 * Return the language in use.
	 * @returns {String} The language.
	 */
	getLanguage: function() {
		return WT.getVar('language');
	},
	
	/**
	 * Return the language in use.
	 * @returns {String} The language.
	 */
	getLanguageCode: function() {
		return WT.getVar('language').split('_')[0];
	},
	
	/**
	 * Return the language in use.
	 * @returns {String} The language.
	 */
	getLanguageCountry: function() {
		return WT.getVar('language').split('_')[1];
	},
	
	/**
	 * Returns the timezone in use.
	 * Value is taken from core variable 'timezone'.
	 * @returns {String} The timezone ID.
	 */
	getTimezone: function() {
		return WT.getVar('timezone');
	},
	
	/**
	 * Returns the startDay in use (0=Sunday, 1=Monday).
	 * Value is taken from core variable 'startDay'.
	 * @returns {Integer} The startDay value.
	 */
	getStartDay: function() {
		return WT.getVar('startDay');
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a short date. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'shortDateFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getShortDateFmt: function() {
		var fmt = WT.getVar('shortDateFormat');
		return (Ext.isEmpty(fmt)) ? 'd/m/Y' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a short time. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'shortTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getShortTimeFmt: function() {
		//g:i A', e.g., '3:15 PM'. For 24-hour time format try 'H:i'
		var fmt = WT.getVar('shortTimeFormat');
		return (Ext.isEmpty(fmt)) ? 'H:i' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (in ExtJs {@link Ext.Date} style) 
	 * representing a date + time in short form. Value is taken from core 
	 * variable 'shortDateFormat' and 'shortTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getShortDateTimeFmt: function() {
		return this.getShortDateFmt() + ' ' + this.getShortTimeFmt();
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a long date. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'longDateFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getLongDateFmt: function() {
		var fmt = WT.getVar('longDateFormat');
		return (Ext.isEmpty(fmt)) ? 'd/m/Y' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (already in ExtJs {@link Ext.Date} style) 
	 * representing a long time. Remember that original option value follows 
	 * Java style patterns. Value is taken from core variable 'longTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getLongTimeFmt: function() {
		var fmt = WT.getVar('longTimeFormat');
		return (Ext.isEmpty(fmt)) ? 'H:i:s' : Sonicle.Date.toExtFormat(fmt);
	},
	
	/**
	 * Returns the date format string (in ExtJs {@link Ext.Date} style) 
	 * representing a date + time in long form. Value is taken from core 
	 * variable 'longDateFormat' and 'longTimeFormat'.
	 * @returns {String} ExtJs format string.
	 */
	getLongDateTimeFmt: function() {
		return WT.getLongDateFmt() + ' ' + WT.getLongTimeFmt(); 
	},
	
	/**
	 * Returns if 24h time is in use.
	 * Value is taken from core variable 'use24HourTime'.
	 * @returns {Boolean}
	 */
	getUse24HourTime: function() {
		return WT.getVar('use24HourTime');
	},
	
	/**
	 * Convenience function to start a new message using mail service, if present.
	 * At the moment, WT will search for the mail service and start a new email.
	 * @param {String/String[]} recipients A single or a list of recipients.
	 * @param {String} subject The message subject.
	 * @param {String} [body] The message body. Optional.
	 */
	handleNewMailMessage: function(recipients, subject, body) {
		var svc = WT.getApp().getService('com.sonicle.webtop.mail');
		if (!svc) return;
		
		var arr = Ext.isArray(recipients) ? recipients : [recipients],
				opts = {format: svc.getVar('format')},
				rcpts = [];
		Ext.iterate(arr, function(rcpt) {
			if (!Ext.isEmpty(rcpt)) {
				rcpts.push({rtype: 'to', email: rcpt});
			}
		});
		opts.recipients = rcpts;
		if (!Ext.isEmpty(subject)) opts.subject = subject;
		if (!Ext.isEmpty(body)) {
			opts.content = body;
			opts.contentAfter = false;
		}
		svc.startNewMessage(svc.currentFolder, opts);
	},
	
	/**
	 * @deprecated Use {@link #handleNewMailMessage} instead.
	 */
	handleMailAddress: function(address,subject,body) {
		Ext.log.warn("[WT.core] WT.handleMailAddress is deprecated, please use WT.handleNewMailMessage instead.");
		this.handleNewMailMessage.apply(this, arguments);
	},
	
	/**
	 * Convenience function to run a phone call through the configured pbx.
	 * @param {String} number The number to call.
	 */
	handlePbxCall: function(number) {
		var svc = WT.getApp().getService('com.sonicle.webtop.core');
		if (!svc) return;
		svc.handlePbxCall(number);
	},
	
	/**
	 * Convenience function to send an SMS through the configured SMS provider.
	 * @param {String} number The destination number.
	 * @param {String} text The SMS text.
	 */
	handleSendSMS: function(name, number, text) {
		var svc = WT.getApp().getService('com.sonicle.webtop.core');
		if (!svc) return;
		svc.handleSendSMS(name, number, text);
	},
	
	print: function(html) {
		Sonicle.PrintMgr.print(html);
	},
	
	componentLoader: function(svc, act, opts) {
		if(!opts) opts = {};
		return {
			url: WTF.requestBaseUrl(),
			params: Ext.applyIf({
				service: svc,
				action: act
			}, opts.params || {}),
			contentType: 'html',
			loadMask: true
		};
	}
});
