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
/**
 * viewport:
 * 
 *      +-----------------------------------------+
 *      |           announcement-bar              |
 *      +-----------------------------------------+
 *      |                                         |
 *      |                                         |
 *      |                                         |
 *      |             content-wrap                |
 *      |                                         |
 *      |                                         |
 *      |                                         |
 *      |                                         |
 *      +-----------------------------------------+
 *
 * content-wrap:
 * 
 *      +-----------------------------------------+
 *      |                 t-dock                  |
 *      +---+---------------------------------+---+
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      | ( |                                 | ( |
 *      | u |                                 | u |
 *      | n |                                 | n |
 *      | u |                                 | u |
 *      | s |          center-content         | s |
 *      | e |                                 | e |
 *      | d |                                 | d |
 *      | ) |                                 | ) |
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      +---+---------------------------------+---+
 *      |                 b-dock                  |
 *      +-----------------------------------------+
 * 
 * avaiable CSS classes:
 * 
 * + wt-viewport-contentwrap
 * + wt-viewport-contentwrap-body
 */
Ext.define('Sonicle.webtop.core.public.viewport.Abstract', {
	alternateClassName: 'WTA.public.viewport.Abstract',
	extend: 'Ext.container.Viewport',
	mixins: [
		'Sonicle.mixin.RefHolder'
	],
	requires: [
		'Sonicle.Announcement',
		'WTA.public.viewport.ViewController'
	],
	uses: [
		'WTA.ux.panel.PanelContainer'
	],
	controller: Ext.create('WTA.public.viewport.ViewController'),
	cls: 'wt-viewport-public',
	
	referenceHolder: true,
	layout: {
		type: 'vbox',
		align: 'stretch'
	},
	
	returnTopDockCfg: Ext.emptyFn,
	returnBottomDockCfg: Ext.emptyFn,
	returnCenterContentCfg: Ext.emptyFn,
	
	constructor: function(cfg) {
		var me = this;
		me.mixins.refholder.constructor.call(me, cfg);
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
			contentWrap;
		me.callParent(arguments);
		
		// Announcement bar
		Sonicle.Announcement.register(me.add({
			xtype: 'soannouncementbar'
		}));
		
		contentWrap = me.add(me.applyMoreCfg(me.createContentWrapCfg(), {width: '100%', flex: 1}));
		
		me.addAsDockedTo(contentWrap, me.returnTopDockCfg(), 'top', 'tdock');
		me.addAsDockedTo(contentWrap, me.returnBottomDockCfg(), 'bottom', 'bdock');
		me.addAsRegionTo(contentWrap, me.returnCenterContentCfg(), 'center', 'ccontent');
	},
	
	privates: {
		centerContentCmp: function() {
			return this.lookupReference('ccontent');
		},
		
		createContentWrapCfg: function() {
			return {
				xtype: 'wtpanelct',
				reference: 'contentwrap',
				border: false,
				cls: 'wt-viewport-contentwrap',
				bodyCls: 'wt-viewport-contentwrap-body',
				layout: 'border'
			};
		},
		
		createDummyMainCfg: function() {
			return {
				xtype: 'wtpanelct',
				layout: 'center',
				bodyStyle: 'text-align:center;',
				items: [
					{
						xtype: 'wtpanel',
						html: 'This is your Main area, you should put some content here.<br>'
							+ 'Call <i>setMainComponent()</i> method in your service <i>init()</i> callback to achieve this.'
					}
				]
			};
		},
		
		applyMoreCfg: function(cfg, moreCfg) {
			return cfg ? Ext.merge(cfg, moreCfg || {}) : cfg;
		},
		
		addAsRegionTo: function(parent, cmp, region, reference) {
			if (cmp && parent) {
				if (cmp.isComponent) {
					cmp.setRegion(region);
					cmp.setReference(reference);
				} else {
					Ext.apply(cmp, {
						region: region,
						reference: reference,
						referenceHolder: true
					});
				}
				parent.add(cmp);
			}
		},
		
		addAsDockedTo: function(parent, cmp, dock, reference) {
			if (cmp && parent) {
				if (cmp.isComponent) {
					cmp.setDock(dock);
					cmp.setReference(reference);
				} else {
					Ext.apply(cmp, {
						dock: dock,
						reference: reference,
						referenceHolder: true
					});
				}
				parent.addDocked(cmp);
			}
		}
	}
});