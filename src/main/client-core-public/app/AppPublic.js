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
		'Sonicle.webtop.core.app.WTPublic'
	].concat(WTS.appRequires || []),
	uses: [
		'Sonicle.DesktopNotificationMgr',
		'Sonicle.webtop.core.app.DescriptorPublic'
	],
	views: [
		'WTA.view.pub.Viewport'
	],
	refs: {
		viewport: 'viewport'
	},
	autoCreateViewport: false,
	
	constructor: function() {
		this.callParent(arguments);
	},
	
	launch: function() {
		var me = this;
		
		// Initialize services' descriptors
		me.initDescriptors();
		Ext.iterate(WTS.services, function(obj, idx) {
			var desc = me.descriptors.get(obj.id);
			if (!desc) Ext.raise('This should never happen (famous last words)');
			
			desc.setOrder(idx);
			//desc.setMaintenance(obj.maintenance);
			desc.setServiceClassName(obj.serviceCN);
			desc.setServiceVarsClassName(obj.serviceVarsCN);
			
			me.services.push(obj.id);
		});
		
		var sids = me.getServices(),
				vp, desc;
		
		// Initialize core service (this will implicitly creates its instance)
		me.getDescriptor(sids[0]).initService();
		
		// Creates main viewport
		vp = me.viewport = me.getView(me.views[0]).create();
		
		// Instantiates remaining services (the only one remaining)
		desc = me.getDescriptor(sids[1]);
		if (desc.initService()) {
			vp.addService(desc.getInstance());
		}
	},
	
	createServiceDescriptor: function(cfg) {
		return Ext.create('WTA.DescriptorPublic', cfg);
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
