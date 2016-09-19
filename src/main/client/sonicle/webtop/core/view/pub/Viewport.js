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
Ext.define('Sonicle.webtop.core.view.pub.Viewport', {
	alternateClassName: 'WT.view.pub.Viewport',
	extend: 'Ext.container.Viewport',
	
	layout: 'border',
	referenceHolder: true,
	
	mainmap: null,
	
	constructor: function() {
		var me = this;
		me.mainmap = {};
		me.callParent(arguments);
	},
	
	destroy: function() {
		var me = this;
		me.callParent(arguments);
		me.mainmap = null;
	},
	
	/**
	 * Adds passed service to wiewport's layout.
	 * @param {WT.sdk.PublicService} svc The service instance.
	 */
	addService: function(svc) {
		var me = this,
				id = svc.ID,
				main = null;
		
		if(me.mainmap[id]) return; // Checks if service has been already added
		
		// Retrieves service components
		if(Ext.isFunction(svc.getMainComponent)) {
			main = svc.getMainComponent.call(svc);
		}
		me.addServiceComponents(svc, main);
	},
	
	addServiceComponents: function(svc, main) {
		var me = this;
		
		if(!main || !main.isXType('container')) {
			main = Ext.create({xtype: 'panel'});
		}
		me.mainmap[svc.ID] = main.getId();
		me.addToRegion('center', main);
	},
	
	/*
	 * @private
	 * Adds passed config to chosen layout region.
	 * @param {String} region Border layout region
	 * @param {Ext.Component} cmp The component to add
	 */
	addToRegion: function(region, cmp) {
		var me = this;
		if(cmp) {
			if(cmp.isComponent) {
				cmp.setRegion(region);
				cmp.setReference(region);
			} else {
				Ext.apply(cmp, {
					region: region,
					reference: region,
					referenceHolder: true
				});
			}
			me.add(cmp);
		}
	}
});
