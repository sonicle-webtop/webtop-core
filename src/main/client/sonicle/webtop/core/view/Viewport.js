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
Ext.define('Sonicle.webtop.core.view.Viewport', {
	extend: 'Ext.container.Viewport',
	layout: 'border',
	
	requires: [
		'Sonicle.webtop.core.view.ViewportController'
	],
	controller: Ext.create('Sonicle.webtop.core.view.ViewportController'),
	
	svctbs: null,
	svctools: null,
	svcmains: null,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		var header = Ext.create({
			xtype: 'container',
			region: 'north',
			itemId: 'header',
			layout: 'border',
			height: 40,
			/*
			defaults: {
				cls: 'wt-header'
			},
			*/
			items: [
				{
					xtype: 'toolbar',
					region: 'west',
					itemId: 'newtb',
					cls: 'wt-header',
					border: false,
					style: {
						paddingTop: 0,
						paddingBottom: 0
					},
					items: [{
							xtype: 'combo',
							fieldLabel: 'Select Theme',
							width: 300,
							store: Ext.create('Ext.data.Store', {
								fields: ['id', 'description'],
								data: [
									{"id": "aria", "description": "Aria"},
									{"id": "neptune", "description": "Neptune"},
									{"id": "classic", "description": "Classic"},
									{"id": "gray", "description": "Gray"},
									{"id": "neptune-touch", "description": "Neptune Touch"},
									{"id": "crisp", "description": "Crisp"},
									{"id": "crisp-touch", "description": "Crisp Touch"}
									//...
								]
							}),
							queryMode: 'local',
							displayField: 'description',
							valueField: 'id',
							editable: false,
							listeners: {
								scope: this,
								'select': function (c, r, o) {
									Ext.Ajax.request({
										url: 'service-request',
										params: {
											service: 'com.sonicle.webtop.core',
											action: 'SetTheme',
											theme: r[0].get('id')
										},
										success: function (r) {
											window.location.reload();
										}
									});
								}
							}
						}
					]
				}, {
					xtype: 'container',
					region: 'center',
					itemId: 'svctbs',
					layout: 'card',
					defaults: {
						cls: 'wt-header',
						border: false,
						padding: 0
					}
				}, {
					xtype: 'toolbar',
					region: 'east',
					itemId: 'menutb',
					cls: 'wt-header',
					border: false,
					style: {
						paddingTop: 0,
						paddingBottom: 0
					},
					items: [{
							xtype: 'button',
							glyph: 0xf0c9,
							menu: {
								xtype: 'menu',
								plain: true,
								width: 150,
								items: [{
										xtype: 'buttongroup',
										columns: 2,
										defaults: {
											xtype: 'button',
											scale: 'large',
											iconAlign: 'center',
											width: '100%',
											handler: 'onMenuButtonClick'
										},
										items: [{
												itemId: 'feedback',
												tooltip: WT.res('menu.feedback.tip'),
												glyph: 0xf1d8
											}, {
												itemId: 'whatsnew',
												tooltip: WT.res('menu.whatsnew.tip'),
												glyph: 0xf0eb
											}, {
												itemId: 'settings',
												tooltip: WT.res('menu.settings.tip'),
												glyph: 0xf013
											}, {
												xtype: 'container'
											}, {
												itemId: 'logout',
												colspan: 2,
												scale: 'small',
												tooltip: WT.res('menu.logout.tip'),
												glyph: 0xf011
											}
										]
									}
								]
							}
						}
					]
				}
			]
		});
		me.svctbs = header.queryById('svctbs');
		me.add(header);
		
		var navtb = Ext.create({
			xtype: 'toolbar',
			region: 'west',
			itemId: 'navtb',
			cls: 'wt-nav',
			border: false,
			vertical: true
		});
		WT.getApp().services.each(function(desc) {
			navtb.add(me.createSvcButton(desc));
		}, me);
		me.add(navtb);
		
		var center = Ext.create({
			xtype: 'container',
			region: 'center',
			itemId: 'center',
			layout: 'border',
			defaults: {
				split: true
			},
			items: [{
					xtype: 'container',
					region: 'west',
					itemId: 'svctools',
					layout: 'card',
					collapsible: true
				}, {
					xtype: 'container',
					region: 'center',
					itemId: 'svcmains',
					layout: 'card'
				}
			]
		});
		me.svctools = center.queryById('svctools');
		me.svcmains = center.queryById('svcmains');
		me.add(center);
	},
	
	createSvcButton: function(desc) {
		var inst = inst = desc.getInstance();
		return Ext.create({
			xtype: 'button',
			scale: 'large',
			itemId: inst.id,
			iconCls: inst.cssIconCls('service-m'),
			tooltip: desc.getName(),
			handler: 'onNavTbButtonClick'
		});
	}
});
