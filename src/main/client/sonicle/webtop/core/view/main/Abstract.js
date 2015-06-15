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
Ext.define('Sonicle.webtop.core.view.main.Abstract', {
	alternateClassName: 'WT.view.main.Abstract',
	extend: 'Ext.container.Viewport',
	requires: [
		'WT.view.main.AbstractC',
		'WT.ux.ServiceButton'
	],
	controller: Ext.create('WT.view.main.AbstractC'),
	
	layout: 'border',
	referenceHolder: true,
	
	config: {
		totalServices: -1
	},
	
	westCmp: Ext.emptyFn,
	centerCmp: Ext.emptyFn,
	getSide: Ext.emptyFn,
	getToolStack: Ext.emptyFn,
	getMainStack: Ext.emptyFn,
	addServiceButton: Ext.emptyFn,
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.addToRegion('north', me.northCmp());
		me.addToRegion('west', me.westCmp());
		me.addToRegion('center', me.centerCmp());
		//me.addToRegion('east', me.eastCmp());
		//me.addToRegion('south', me.southCmp());
	},
	
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
	},
	
	northCmp: function() {
		return {
			xtype: 'container',
			referenceHolder: true,
			layout: 'border',
			height: 35,
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
								iconCls: 'wt-menu-feedback'
								//glyph: 0xf1d8
							}, {
								itemId: 'whatsnew',
								tooltip: WT.res('menu.whatsnew.tip'),
								iconCls: 'wt-menu-whatsnew'
								//glyph: 0xf0eb
							}, {
								itemId: 'options',
								tooltip: WT.res('menu.options.tip'),
								iconCls: 'wt-menu-options'
								//glyph: 0xf013
							}, {
								//xtype: 'container'
								itemId: 'help',
								tooltip: WT.res('menu.help.tip'),
								iconCls: 'wt-menu-help'
							}, {
								itemId: 'logout',
								colspan: 2,
								scale: 'small',
								tooltip: WT.res('menu.logout.tip'),
								iconCls: 'wt-menu-logout'
								//glyph: 0xf011
							}]
						}]
					}
				}]
			}]
		};
	},
	
	/**
	 * Adds specified actions to wiewport's layout.
	 * @param {Ext.Action[]} acts Service actions to add.
	 */
	addServiceNewActions: function(acts) {
		var me = this,
				north = me.lookupReference('north'),
				newtb = north.lookupReference('newtb'),
				newbtn = newtb.lookupReference('newbtn');
		
		if(!newbtn) {
			newbtn = newtb.add(Ext.create({
				xtype: 'splitbutton',
				reference: 'newbtn',
				text: WT.res('new.btn-new.lbl'),
				menu: [],
				handler: 'onNewActionButtonClick'
			}));
		}
		
		var menu = newbtn.getMenu();
		Ext.each(acts, function(act) {
			menu.add(act);
		});
	}
});
