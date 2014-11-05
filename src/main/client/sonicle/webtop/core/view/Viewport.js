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
	requires: [
		'Sonicle.webtop.core.view.ViewportC',
		'Sonicle.webtop.core.model.Theme',
		'Sonicle.webtop.core.SvcButton'
	],
	controller: Ext.create('Sonicle.webtop.core.view.ViewportC'),
	layout: 'border',
	
	svctb: null,
	svctool: null,
	svcmain: null,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		var header = Ext.create({
			xtype: 'container',
			region: 'north',
			itemId: 'header',
			layout: 'border',
			height: 40,
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
						editable: false,
						store: {
							model: 'Sonicle.webtop.core.model.Theme',
							proxy: WT.proxy('com.sonicle.webtop.core', 'GetThemes', 'themes')
						},
						valueField: 'id',
						displayField: 'description',
						listeners: {
							scope: this,
							select: function(c,r,o) {
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
					}]
				}, {
					xtype: 'container',
					region: 'center',
					itemId: 'svctb',
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
		me.svctb = header.queryById('svctb');
		me.add(header);
		
		var launcher = Ext.create({
			xtype: 'toolbar',
			region: 'west',
			itemId: 'launcher',
			cls: 'wt-launcher',
			border: false,
			vertical: true
		});
		WT.getApp().services.each(function(desc) {
			launcher.add(me.createSvcButton(desc));
		}, me);
		me.add(launcher);
		
		me.svcwp = Ext.create({
			xtype: 'container',
			region: 'center',
			itemId: 'svcwp',
			layout: 'card'
		});
		me.add(me.svcwp);
		
		
		
		/*
		var center = Ext.create({
			xtype: 'container',
			region: 'center',
			itemId: 'center',
			layout: 'border',
			defaults: {
				split: true,
				collapsible: true
			},
			items: [{
					xtype: 'container',
					region: 'west',
					itemId: 'svctool',
					layout: 'card',
					width: 200
				}, {
					xtype: 'container',
					region: 'center',
					itemId: 'svcmain',
					layout: 'card',
					collapsible: false
				}
			]
		});
		me.svctool = center.queryById('svctool');
		me.svcmain = center.queryById('svcmain');
		me.add(center);
		*/
	},
	
	createSvc: function() {
		
	},
	
	createSvcButton: function(desc) {
		// Defines tooltips
		var tip = {title: desc.getName()};
		if(WTStartup.isadmin) { // TODO: gestire tooltip per admin
			var build = desc.getBuild();
			Ext.apply(tip, {
				text: Ext.String.format('v.{0}{1} - {2}', desc.getVersion(), Ext.isEmpty(build) ? '' : '('+build+')', desc.getCompany())
			});
		} else {
			Ext.apply(tip, {
				text: Ext.String.format('v.{0} - {1}', desc.getVersion(), desc.getCompany())
			});
		}
		
		var inst = inst = desc.getInstance();
		return Ext.create({
			xtype: 'wtsvcbutton',
			scale: 'large',
			itemId: inst.id,
			iconCls: inst.cssIconCls('service-m'),
			tooltip: tip,
			handler: 'onLauncherButtonClick'
		});
	}
});
