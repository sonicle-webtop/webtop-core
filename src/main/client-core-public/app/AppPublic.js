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
Ext.define('Sonicle.webtop.core.app.AppPublic', {
	extend: 'Sonicle.webtop.core.app.AppBase',
	requires: [
		'Sonicle.String',
		'Sonicle.Date',
		'Sonicle.PageMgr',
		'Sonicle.URLMgr',
		'Sonicle.PrintMgr',
		'Sonicle.DesktopNotificationMgr',
		'Sonicle.upload.Uploader',
		'Sonicle.data.proxy.Ajax',
		'Sonicle.data.identifier.NegativeString',
		'Sonicle.form.field.VTypes',
		'Sonicle.plugin.EnterKeyPlugin',
		'Sonicle.plugin.FieldTooltip',
		
		'Sonicle.webtop.core.ux.data.BaseModel',
		'Sonicle.webtop.core.ux.data.EmptyModel',
		'Sonicle.webtop.core.ux.data.SimpleModel',
		'Sonicle.webtop.core.ux.data.ArrayStore',
		'Sonicle.webtop.core.ux.panel.Panel',
		'Sonicle.webtop.core.ux.panel.Fields',
		'Sonicle.webtop.core.ux.panel.Form',
		'Sonicle.webtop.core.ux.panel.Tab',
		
		'Sonicle.webtop.core.app.WT',
		'Sonicle.webtop.core.app.FileTypes',
		'Sonicle.webtop.core.app.Factory',
		'Sonicle.webtop.core.app.Util',
		'Sonicle.webtop.core.app.Log',
		'Sonicle.webtop.core.app.ThemeMgr',
		
		'Sonicle.webtop.core.app.WTPublic',
		'Sonicle.webtop.core.app.DescriptorPublic'
		
	].concat(WTS.appRequires || []),
	views: [
		'WTA.view.pub.Viewport'
	],
	refs: {
		viewport: 'viewport'
	},
	
	constructor: function() {
		this.callParent(arguments);
	},
	
	init: function() {
		WTA.Log.debug('application:init');
		Ext.tip.QuickTipManager.init();
		Ext.setGlyphFontFamily('FontAwesome');
		Ext.themeName = WTS.servicesVars[0].theme;
		Ext.getDoc().on('contextmenu', function(e) {
			e.preventDefault(); // Disable browser context if no context menu is defined
		});
		
		// Inits state provider
		if(Ext.util.LocalStorage.supported) {
			Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());
		} else {
			Ext.state.Manager.setProvider(new Ext.state.CookieProvider({
				expires: new Date(Ext.Date.now() + (1000*60*60*24*90)) // 90 days
			}));
		}
		WTA.FileTypes.init(WTS.fileTypes);
	},
	
	launch: function() {
		var me = this, desc;
		
		// Loads service descriptors from startup object
		Ext.each(WTS.services, function(obj) {
			desc = Ext.create('WTA.DescriptorPublic', {
				index: obj.index,
				maintenance: obj.maintenance,
				id: obj.id,
				xid: obj.xid,
				ns: obj.ns,
				path: obj.path,
				serviceClassName: obj.serviceClassName,
				serviceVarsClassName: obj.serviceVarsClassName,
				localeClassName: obj.localeClassName,
				name: obj.name,
				description: obj.description,
				company: obj.company
			});
			
			me.locales.add(obj.id, Ext.create(obj.localeClassName));
			me.services.add(desc);
		}, me);
		
		//TODO: portare il metodo onRequiresLoaded direttamente qui!
		me.onRequiresLoaded.call(me);
	},
	
	onRequiresLoaded: function() {
		var me = this, cdesc, pdesc, vp;
		
		// Instantiates core service
		cdesc = me.services.getAt(0);
		cdesc.getInstance();
		cdesc.initService();
		
		// Creates main viewport
		vp = me.viewport = me.getView(me.views[0]).create();
		
		// Inits the only public service
		pdesc = me.services.getAt(1);
		if(pdesc.initService()) {
			vp.addService(pdesc.getInstance());
		}
	},
	
	/**
	 * Returns a service instance.
	 * @param {String} id The service ID.
	 * @returns {WTA.sdk.Service} The instance or null if not found. 
	 */
	getService: function(id) {
		var desc = this.getDescriptor(id);
		return (desc) ? desc.getInstance() : null;
	}
});
