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
	alternateClassName: 'WT.view.Viewport',
	extend: 'Ext.container.Viewport',
	requires: [
		'WT.view.ViewportC',
		'WT.ux.ServiceButton'
	],
	controller: Ext.create('WT.view.ViewportC'),
	layout: 'border',
	
	referenceHolder: true,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		this.add(Ext.create({
			xtype: 'container',
			region: 'north',
			reference: 'header',
			layout: 'border',
			height: 40,
			items: [{
				xtype: 'toolbar',
				region: 'west',
				reference: 'newtb',
				referenceHolder: true,
				cls: 'wt-header',
				border: false,
				style: {
					paddingTop: 0,
					paddingBottom: 0
				},
				items: []
			}, {
				xtype: 'container',
				region: 'center',
				reference: 'svctb',
				layout: 'card',
				defaults: {
					cls: 'wt-header',
					border: false,
					padding: 0
				}
			}, {
				xtype: 'toolbar',
				region: 'east',
				reference: 'menutb',
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
								itemId: 'options',
								tooltip: WT.res('menu.options.tip'),
								glyph: 0xf013
							}, {
								xtype: 'container'
							}, {
								itemId: 'logout',
								colspan: 2,
								scale: 'small',
								tooltip: WT.res('menu.logout.tip'),
								glyph: 0xf011
							}]
						}]
					}
				}]
			}]
		}));
		
		//var launcher = me.add(Ext.create({
		me.add(Ext.create({
			xtype: 'toolbar',
			region: 'west',
			reference: 'launcher',
			cls: 'wt-launcher',
			border: false,
			vertical: true,
			referenceHolder: true
		}));
		
		me.add(Ext.create({
			xtype: 'container',
			region: 'center',
			reference: 'svcwp',
			layout: 'card'
		}));
	},
	
	createServiceButton: function(desc) {
		// Defines tooltips
		var tip = {title: desc.getName()};
		if(WTS.isadmin) { // TODO: gestire tooltip per admin
			var build = desc.getBuild();
			Ext.apply(tip, {
				text: Ext.String.format('v.{0}{1} - {2}', desc.getVersion(), Ext.isEmpty(build) ? '' : '('+build+')', desc.getCompany())
			});
		} else {
			Ext.apply(tip, {
				text: Ext.String.format('v.{0} - {1}', desc.getVersion(), desc.getCompany())
			});
		}
		
		return Ext.create('WT.ux.ServiceButton', {
			scale: 'large',
			itemId: desc.getId(),
			iconCls: WT.cssIconCls(desc.getXid(), 'service-m'),
			tooltip: tip,
			handler: 'onLauncherButtonClick'
		});
	}
});
