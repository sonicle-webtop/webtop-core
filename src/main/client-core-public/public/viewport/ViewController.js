/* 
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.public.viewport.ViewController', {
	alternateClassName: 'WTA.public.viewport.ViewController',
	extend: 'Ext.app.ViewController',
	requires: [
		'WTA.sdk.PublicService',
		'WTA.ux.panel.Panel'
	],
	
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
	 * @param {WTA.sdk.PublicService} svc The service instance.
	 */
	addService: function(svc) {
		if (this.mainmap[svc.ID]) return; // Checks if service has been already added
		var me = this,
			main = svc.getRef(WTA.sdk.PublicService.MAIN_REF_NAME);
		
		if (!main) main = svc.createServiceMain();
		
		me.addServiceComponents(svc, main);
	},
	
	privates: {
		addServiceComponents: function(svc, main) {
			var me = this,
				view = me.getView(),
				desc = WT.getApp().getDescriptor(svc.ID);
			
			if (!Ext.isDefined(main) || !main.isXType('container')) {
				main = Ext.create(view.createDummyMainCfg());
			} else if (!main.isInstance) {
				main = Ext.create(main);
			}
			view.addMainAreaItem(desc, main);
			me.mainmap[svc.ID] = main.getId();
		}
	}
});