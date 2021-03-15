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
	
	/*
	materialPalette: [
		// Red (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'FFEBEE', 'FFCDD2', 'EF9A9A', 'E57373', 'EF5350', 'F44336', 'E53935', 'D32F2F', 'C62828', 'B71C1C', 'FF8A80', 'FF5252', 'FF1744', 'D50000',
		// Pink (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'FCE4EC', 'F8BBD0', 'F48FB1', 'F06292', 'EC407A', 'E91E63', 'D81B60', 'C2185B', 'AD1457', '880E4F', 'FF80AB', 'FF4081', 'F50057', 'C51162',
		// Purple (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'F3E5F5', 'E1BEE7', 'CE93D8', 'BA68C8', 'AB47BC', '9C27B0', '8E24AA', '7B1FA2', '6A1B9A', '4A148C', 'EA80FC', 'E040FB', 'D500F9', 'AA00FF',
		// Deep Purple (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'EDE7F6', 'D1C4E9', 'B39DDB', '9575CD', '7E57C2', '673AB7', '5E35B1', '512DA8', '4527A0', '311B92', 'B388FF', '7C4DFF', '651FFF', '6200EA',
		// Indigo (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'E8EAF6', 'C5CAE9', '9FA8DA', '7986CB', '5C6BC0', '3F51B5', '3949AB', '303F9F', '283593', '1A237E', '8C9EFF', '536DFE', '3D5AFE', '304FFE',
		// Blue (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'E3F2FD', 'BBDEFB', '90CAF9', '64B5F6', '42A5F5', '2196F3', '1E88E5', '1976D2', '1565C0', '0D47A1', '82B1FF', '448AFF', '2979FF', '2962FF',
		// Light Blue (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'E1F5FE', 'B3E5FC', '81D4FA', '4FC3F7', '29B6F6', '03A9F4', '039BE5', '0288D1', '0277BD', '01579B', '80D8FF', '40C4FF', '00B0FF', '0091EA',
		// Cyan (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'E0F7FA', 'B2EBF2', '80DEEA', '4DD0E1', '26C6DA', '00BCD4', '00ACC1', '0097A7', '00838F', '006064', '84FFFF', '18FFFF', '00E5FF', '00B8D4',
		// Teal (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'E0F2F1', 'B2DFDB', '80CBC4', '4DB6AC', '26A69A', '009688', '00897B', '00796B', '00695C', '004D40', 'A7FFEB', '64FFDA', '1DE9B6', '00BFA5',
		// Green (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'E8F5E9', 'C8E6C9', 'A5D6A7', '81C784', '66BB6A', '4CAF50', '43A047', '388E3C', '2E7D32', '1B5E20', 'B9F6CA', '69F0AE', '00E676', '00C853',
		// Light Green (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'F1F8E9', 'DCEDC8', 'C5E1A5', 'AED581', '9CCC65', '8BC34A', '7CB342', '689F38', '558B2F', '33691E', 'CCFF90', 'B2FF59', '76FF03', '64DD17',
		// Lime (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'F9FBE7', 'F0F4C3', 'E6EE9C', 'DCE775', 'D4E157', 'CDDC39', 'C0CA33', 'AFB42B', '9E9D24', '827717', 'F4FF81', 'EEFF41', 'C6FF00', 'AEEA00',
		// Yellow (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'FFFDE7', 'FFF9C4', 'FFF59D', 'FFF176', 'FFEE58', 'FFEB3B', 'FDD835', 'FBC02D', 'F9A825', 'F57F17', 'FFFF8D', 'FFFF00', 'FFEA00', 'FFD600',
		// Amber (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'FFF8E1', 'FFECB3', 'FFE082', 'FFD54F', 'FFCA28', 'FFC107', 'FFB300', 'FFA000', 'FF8F00', 'FF6F00', 'FFE57F', 'FFD740', 'FFC400', 'FFAB00',
		// Orange (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'FFF3E0', 'FFE0B2', 'FFCC80', 'FFB74D', 'FFA726', 'FF9800', 'FB8C00', 'F57C00', 'EF6C00', 'E65100', 'FFD180', 'FFAB40', 'FF9100', 'FF6D00',
		// Deep Orange (50,100,200,300,400,500,600,700,800,900,A100,A200,A400,A700)
		'FBE9E7', 'FFCCBC', 'FFAB91', 'FF8A65', 'FF7043', 'FF5722', 'F4511E', 'E64A19', 'D84315', 'BF360C', 'FF9E80', 'FF6E40', 'FF3D00', 'DD2C00',
		// Brown (50,100,200,300,400,500,600,700,800,900)
		'EFEBE9', 'D7CCC8', 'BCAAA4', 'A1887F', '8D6E63', '795548', '6D4C41', '5D4037', '4E342E', '3E2723',
		// Grey (50,100,200,300,400,500,600,700,800,900)
		'FAFAFA', 'F5F5F5', 'EEEEEE', 'E0E0E0', 'BDBDBD', '9E9E9E', '757575', '616161', '424242', '212121',
		// Blue Grey (50,100,200,300,400,500,600,700,800,900)
		'ECEFF1', 'CFD8DC', 'B0BEC5', '90A4AE', '78909C', '607D8B', '546E7A', '455A64', '37474F', '263238'
	],
	*/
	
	paletteDefault: [ // 11 columns (from material colors 100 -> 900)
		// Red | Pink | Purple | Indigo | Blue | Green | Light Green| Yellow | Orange | Brown | Grey
		// (each column dark->light bottom-up)
		'FFCDD2', 'F8BBD0', 'E1BEE7', 'C5CAE9', 'BBDEFB', 'C8E6C9', 'DCEDC8', 'FFF9C4', 'FFE0B2','D7CCC8', 'F5F5F5',
		'EF9A9A', 'F48FB1', 'CE93D8', '9FA8DA', '90CAF9', 'A5D6A7', 'C5E1A5', 'FFF59D', 'FFCC80', 'BCAAA4', 'EEEEEE',
		'E57373', 'F06292', 'BA68C8', '7986CB', '64B5F6', '81C784', 'AED581', 'FFF176', 'FFB74D', 'A1887F', 'E0E0E0',
		'EF5350', 'EC407A', 'AB47BC', '5C6BC0', '42A5F5', '66BB6A', '9CCC65', 'FFEE58', 'FFA726', '8D6E63', 'BDBDBD',
		'F44336', 'E91E63', '9C27B0', '3F51B5', '2196F3', '4CAF50', '8BC34A', 'FFEB3B', 'FF9800', '795548', '9E9E9E',
		'E53935', 'D81B60', '8E24AA', '3949AB', '1E88E5', '43A047', '7CB342', 'FDD835', 'FB8C00', '6D4C41', '757575',
		'D32F2F', 'C2185B', '7B1FA2', '303F9F', '1976D2', '388E3C', '689F38', 'FBC02D', 'F57C00', '5D4037', '616161',
		'C62828', 'AD1457', '6A1B9A', '283593', '1565C0', '2E7D32', '558B2F', 'F9A825', 'EF6C00', '4E342E', '424242',
		'B71C1C', '880E4F', '4A148C', '1A237E', '0D47A1', '1B5E20', '33691E', 'F57F17', 'E65100', '3E2723', '212121'
	],
	
	paletteLight: [ // 11 columns (from material colors 50, 100, 200)
		// Red | Pink | Purple | Indigo | Blue | Green | Light Green| Yellow | Orange | Brown | Grey
		// (each column dark->light bottom-up)
		'FFEBEE', 'FCE4EC', 'F3E5F5', 'E8EAF6', 'E3F2FD', 'E8F5E9', 'F1F8E9', 'FFFDE7', 'FFF3E0', 'EFEBE9', 'FAFAFA',
		'FFCDD2', 'F8BBD0', 'E1BEE7', 'C5CAE9', 'BBDEFB', 'C8E6C9', 'DCEDC8', 'FFF9C4', 'FFE0B2', 'D7CCC8', 'F5F5F5',
		'EF9A9A', 'F48FB1', 'CE93D8', '9FA8DA', '90CAF9', 'A5D6A7', 'C5E1A5', 'FFF59D', 'FFCC80', 'BCAAA4', 'EEEEEE'
	],
	
	paletteHtml: [ // 8 columns
		// Black to white (dark->light left-to-right)
		'000000', '444444', '666666', '999999', 'CCCCCC', 'EEEEEE', 'F3F3F3', 'FFFFFF',
		// Conventional colors
		'FF0000', 'FF9900', 'FFFF00', '00FF00', '00FFFF', '0000FF', '9900FF', 'FF00FF',
		// Color shades (each column dark->light bottom-up)
		'F4CCCC', 'FCE5CD', 'FFF2CC', 'D9EAD3', 'D0E0E3', 'CFE2F3', 'D9D2E9', 'EAD1DC',
		'EA9999', 'F9CB9C', 'FFE599', 'B6D7A8', 'A2C4C9', '9FC5E8', 'B4A7D6', 'D5A6BD',
		'E06666', 'F6B26B', 'FFD996', '93C47D', '76A5AF', '6FA8DC', '8E7CC3', 'C27BA0',
		'CC0000', 'E69138', 'F1C232', '6AA84F', '45818E', '3D85C6', '674EA7', 'A64D79',
		'990000', 'B45F06', 'BF9000', '38761D', '134F5C', '0B5394', '351C75', '741B47',
		'660000', '783F04', '7F6000', '274E13', '0C343D', '073763', '20124D', '4C1130'
	],
	
	paletteLegacy: [ // 8 columns
		'AC725E','D06B64','F83A22','FA573C','FF7537','FFAD46','FAD165','FBE983',
		'4986E7','9FC6E7','9FE1E7','92E1C0','42D692','16A765','7BD148','B3DC6C',
		'9A9CFF','B99AFF','A47AE2','CD74E6','F691B2','CCA6AC','CABDBF','C2C2C2',
		'FFFFFF'
	],
	
	getColorPalette: function(name) {
		switch (name) {
			case 'html':
				return this.paletteHtml;
			case 'light':
				return this.paletteLight;
			case 'legacy':
				return this.paletteLegacy;
			default:
				return this.paletteDefault;
		}
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
	 * Finds, if present, VIEW_TAG identifier defined statically in view Class.
	 * @param {String} sid The service ID.
	 * @param {String} name The view's name.
	 * @returns {String} Unique Tag value, or undefined if not.
	 */
	findViewTag: function(sid, name) {
		var app = this.getApp(),
				desc = app.getDescriptor(sid);
		if (!desc) Ext.Error.raise('Service descriptor not found ['+sid+']');
		return app.getViewportController().findServiceViewTag(desc.preNs(name));
	},
	
	/**
	 * Check if the displayable view is already registered.
	 * @param {String} sid The service ID.
	 * @param {String} tag The unique view Tag.
	 * @returns {Boolean}
	 */
	hasView: function(sid, tag) {
		var app = this.getApp(),
				desc = app.getDescriptor(sid);
		if (!desc) Ext.Error.raise('Service descriptor not found ['+sid+']');
		return app.getViewportController().hasServiceView(desc, tag);
	},
	
	/**
	 * Creates a displayable view.
	 * @param {String} sid The service ID.
	 * @param {String} name The class name or alias.
	 * @param {Object} opts
	 * @param {String} opts.tag
	 * @param {Object} opts.viewCfg
	 * @param {Object} opts.containerCfg
	 * @param {Object} opts.preventDuplicates
	 * @returns {WTA.sdk.UIView|Ext.window.Window} The view or view's parent container.
	 */
	createView: function(sid, name, opts) {
		opts = opts || {};
		var app = this.getApp(),
				desc = app.getDescriptor(sid);
		if (!desc) Ext.Error.raise('Service descriptor not found ['+sid+']');
		return app.getViewportController().createServiceView(desc, name, opts);
	},
	
	/**
	 * Gets, if present, the displayable view related to specified Tag.
	 * @param {String} sid The service ID.
	 * @param {String} tag The unique view Tag.
	 * @returns {WTA.sdk.UIView} The displayable view.
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
	 * Returns the configured meeting provider.
	 * @returns {String} Meeting provider ID.
	 */
	getMeetingProvider: function() {
		return WT.getVar('wtMeetingProvider');
	},
	
	/**
	 * Returns the config object for the configured provider.
	 * @returns {Object}
	 */
	getMeetingConfig: function() {
		return Ext.JSON.decode(WT.getVar('wtMeetingConfig'), true) || {};
	},
	
	/**
	 * Returns a RegExp suitable to test if an URL refers to a meeting URL.
	 * @returns {RegExp} The regex or null in case of no URLs configured.
	 */
	getMeetingProvidersURLsRegExp: function() {
		var SoS = Sonicle.String,
				arr = SoS.parseKVArray(WT.getVar('wtPopMeetingProviders'), null),
				url = this.getMeetingConfig().url,
				i, urls = [], esc;
		if (!Ext.isEmpty(url)) urls.push(SoS.regexQuote(url));
		if (Ext.isArray(arr)) {
			for (i=0; i<arr.length; i++) {
				url = arr[i][1];
				if (!Ext.isEmpty(url)) {
					esc = SoS.regexQuote(url);
					// Make domain wildcards working: escaped wildcard text needs to
					// be replaced with the right regex token able to match any subdomains
					urls.push(esc.replace('\\*\\.', '(?:[\\w\\d-]+\\.)?'));
				}
			}
		}
		return arr.length > 0 ? new RegExp('^(?:' + SoS.join('|', urls) + ')', 'i') : null;
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
		if (!Ext.isEmpty(subject)) opts.subject = decodeURIComponent(subject);
		if (!Ext.isEmpty(body)) {
			opts.content = decodeURIComponent(body);
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
	
	/**
	 * Returns a reference (chained) to Tags store.
	 * @param {Object} cfg Custom config to apply to returned chained store.
	 * @returns {Ext.data.Store}
	 */
	getTagsStore: function(cfg) {
		var svc = WT.getApp().getService('com.sonicle.webtop.core');
		return svc ? Ext.create('Ext.data.ChainedStore', Ext.apply(cfg || {}, {source: svc.tagsStore})) : undefined;
	},
	
	/**
	 * Display a confirmation box to select tags and operation to apply.
	 * @param {Function} cb The callback to call.
	 * @param {Object} scope The scope (this) for the supplied callbacks.
	 */
	confirmSelectTags: function(cb, scope) {
		var svc = WT.getApp().getService('com.sonicle.webtop.core');
		if (svc) svc.confirmSelectTags(cb, scope);
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
