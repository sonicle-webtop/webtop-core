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
	alternateClassName: 'WTA.view.main.Abstract',
	extend: 'Ext.container.Viewport',
	requires: [
		'WTA.view.main.AbstractC',
		'WTA.ux.NotificationButton',
		'WTA.ux.TaskBar',
		'WTA.ux.ServiceButton',
		'WTA.ux.ServiceButtonPortal',
		'WTA.ux.ViewWindow',
		'WTA.ux.IMButton',
		'WTA.ux.IMPanel',
		'WTA.ux.data.BadgeNotificationStore',
		'Sonicle.webtop.core.model.IMFriendGrid'
	],
	mixins: [
		'Sonicle.mixin.RefHolder'
	],
	controller: Ext.create('WTA.view.main.AbstractC'),
	
	layout: 'border',
	referenceHolder: true,
	
	viewModel: {
		stores: {
			notifications: {
				type: 'wtbadgenotification'
			}
		}
	},
	
	config: {
		servicesCount: -1
	},
	
	fixedToolsCount: 0,
	
	getPortalButton: Ext.emptyFn,
	getCollapsible: Ext.emptyFn,
	getToolsCard: Ext.emptyFn,
	getMainCard: Ext.emptyFn,
	addServiceButton: Ext.emptyFn,
	createWestCmp: Ext.emptyFn,
	createCenterCmp: Ext.emptyFn,
	
	constructor: function(cfg) {
		var me = this;
		me.mixins.refholder.constructor.call(me, cfg);
		me.callParent([cfg]);
	},
	
	destroy: function() {
		var me = this;
		me.mixins.refholder.destroy.call(me);
		me.notificationStore = null;
		me.callParent();
	},
	
	initComponent: function() {
		var me = this, cmp;
		
		me.addRef('cxmTaskBar', Ext.create({
			xtype: 'menu',
			items: [{
				itemId: 'restore',
				text: 'Restore',
				handler: 'onTaskBarButtonContextClick'
			}, {
				itemId: 'minimize',
				text: 'Minimize',
				handler: 'onTaskBarButtonContextClick'
			}, {
				itemId: 'maximize',
				text: 'Maximize',
				handler: 'onTaskBarButtonContextClick'
			},
			'-',
			{
				itemId: 'close',
				text: 'Close',
				handler: 'onTaskBarButtonContextClick'
			}],
			listeners: {
				beforeshow: function(s) {
					var win = Ext.ComponentManager.get(s.menuData.winId),
							items = s.items;
					items.get('restore').setDisabled(!win.canRestore());
					items.get('minimize').setDisabled((win.canMinimize()) ? !win.canMinimize() : true);
					items.get('maximize').setDisabled((win.canMaximize()) ? !win.canMaximize() : true);
				}
			}
		}));
		
		me.callParent(arguments);
		
		me.addToRegion('north', me.createNorthCmp());
		me.addToRegion('south', me.createSouthCmp());
		if (WT.getVar('imEnabled') === true) {
			me.addToRegion('east', me.createEastCmp());
		}
		
		cmp = me.createWestCmp();
		if (cmp) me.addToRegion('west', cmp);
		cmp = me.createCenterCmp();
		if (cmp) me.addToRegion('center', cmp);
	},
	
	getWest: function() {
		return this.lookupReference('west');
	},
	
	getCenter: function() {
		return this.lookupReference('center');
	},
	
	getTaskBar: function() {
		return this.lookupReference('south');
	},
	
	createNorthCmp: function() {
		var me = this,
				acts = WT.getApp().getService(WT.ID).getToolboxActions(),
				toolsCount = acts.length,
				toolMnuItms = [],
				menuTbItms = [];
		
		me.fixedToolsCount = toolsCount;
		if (toolsCount > 0) {
			Ext.iterate(acts, function(act) {
				toolMnuItms.push(act);
			});
			toolMnuItms.push('-');
		}
		
		menuTbItms.push({
			xtype: 'button',
			reference: 'toolboxbtn',
			iconCls: 'wt-menu-tools',
			tooltip: WT.res('menu.tools.lbl'),
			menu: toolMnuItms
		}, '-');
		if (WT.getVar('imEnabled') === true) {
			menuTbItms.push({
				xtype: 'wtimbutton',
				reference: 'imbtn',
				presenceStatus: 'offline', // We start offline, the real status will be set on init
				statusMessage: WT.getVar('imStatusMessage'),
				listeners: {
					click: 'onIMClick'
				}
				//width: 48
			});
		}
		menuTbItms.push({
			xtype: 'wtnotificationbutton',
			bind: {
				store: '{notifications}',
				listeners: {
					callbackService: 'onCallbackService'
				}
			}
		}, /*{
			xtype: 'button',
			glyph: 0xf0c9,
			menu: {
				items: [{
					xtype: 'panel',
					layout: 'anchor',
					border: false,
					items: [{
						xtype: 'label',
						text: WT.getVar('userDisplayName'),
						cls: 'wt-menu-userdetails-main'
					}, {
						xtype: 'label',
						text: WT.getVar('userId'),
						cls: 'wt-menu-userdetails-sub'
					}],
					width: 150
				}]
			}
		},*/ {
			xtype: 'button',
			glyph: 0xf0c9,
			arrowVisible: false,
			menu: {
				xtype: 'menu',
				plain: true,
				items: [{
						xtype: 'label',
						text: WT.getVar('userDisplayName'),
						cls: 'wt-menu-userdetails-main'
					}, {
						xtype: 'label',
						text: WT.getVar('userId'),
						cls: 'wt-menu-userdetails-sub'
				}, '-', {
					xtype: 'buttongroup',
					ui: WTA.ThemeMgr.getBase(WT.getTheme()) === 'classic' ? 'default-panel' : 'default',
					bodyCls: 'wt-menu-bgroup-body',
					columns: 2,
					defaults: {
						xtype: 'button',
						scale: 'large',
						iconAlign: 'center',
						handler: 'onMenuButtonClick'
					},
					items: [{
						itemId: 'feedback',
						tooltip: WT.res('menu.feedback.tip'),
						disabled: !WT.isPermitted('FEEDBACK', 'MANAGE'),
						iconCls: 'wt-menu-feedback',
						width: '4em',
						height: '4em'
					}, {
						itemId: 'whatsnew',
						tooltip: WT.res('menu.whatsnew.tip'),
						disabled: !WT.getVar('wtWhatsnewEnabled'),
						iconCls: 'wt-menu-whatsnew',
						width: '4em',
						height: '4em'
					}, {
						itemId: 'options',
						tooltip: WT.res('menu.options.tip'),
						iconCls: 'wt-menu-options',
						width: '4em',
						height: '4em'
					}, {
						itemId: 'addons',
						tooltip: WT.res('menu.addons.tip'),
						iconCls: 'wt-menu-addons',
						width: '4em',
						height: '4em'
					}/*, {
						itemId: 'help',
						tooltip: WT.res('menu.help.tip'),
						iconCls: 'wt-menu-help',
						width: '4em',
						height: '4em'
					}*/, {
						itemId: 'logout',
						colspan: 2,
						width: '100%',
						scale: 'small',
						tooltip: WT.res('menu.logout.tip'),
						iconCls: 'wt-menu-logout'
					}]
				}]


				/*
				items: [{
					xtype: 'buttongroup',
					columns: 2,
					/*
					defaults: {
						xtype: 'button',
						scale: 'large',
						iconAlign: 'center',
						width: '100%',
						handler: 'onMenuButtonClick'
					},

					items: [{
						xtype: 'label',
						text: 'Matteo Albinola',
						cls: 'wt-menu-user',
						colspan: 2
					}, {
						xtype: 'label',
						text: 'matteo.albinola@sonicle.com',
						cls: 'wt-menu-useremail',
						colspan: 2
					}, {
						xtype: 'button',
						itemId: 'feedback',
						scale: 'large',
						iconAlign: 'center',
						tooltip: WT.res('menu.feedback.tip'),
						disabled: !WT.isPermitted('FEEDBACK', 'MANAGE'),
						iconCls: 'wt-menu-feedback',
						width: '100%',
						handler: 'onMenuButtonClick'
					}, {
						xtype: 'button',
						itemId: 'whatsnew',
						scale: 'large',
						iconAlign: 'center',
						tooltip: WT.res('menu.whatsnew.tip'),
						disabled: !WT.getVar('wtWhatsnewEnabled'),
						iconCls: 'wt-menu-whatsnew',
						width: '100%',
						handler: 'onMenuButtonClick'
					}, {
						xtype: 'button',
						itemId: 'options',
						scale: 'large',
						iconAlign: 'center',
						tooltip: WT.res('menu.options.tip'),
						iconCls: 'wt-menu-options',
						width: '100%',
						handler: 'onMenuButtonClick'
					}, {
						xtype: 'button',
						itemId: 'help',
						scale: 'large',
						iconAlign: 'center',
						tooltip: WT.res('menu.help.tip'),
						iconCls: 'wt-menu-help',
						width: '100%',
						handler: 'onMenuButtonClick'
					}, {
						xtype: 'button',
						itemId: 'logout',
						colspan: 2,
						scale: 'small',
						tooltip: WT.res('menu.logout.tip'),
						iconCls: 'wt-menu-logout',
						width: '100%',
						handler: 'onMenuButtonClick'
					}]
				}]

			*/
			}
		});
		
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
				items: [{
					xtype: 'splitbutton',
					reference: 'newbtn',
					text: WT.res('new.btn-new.lbl'),
					menu: [],
					handler: 'onNewActionButtonClick'
				}]
			}, {
				xtype: 'container',
				region: 'center',
				reference: 'servicetb',
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
				items: menuTbItms
			}]
		};
	},
	
	createSouthCmp: function() {
		return this.createTaskBar({
			border: '1 0 0 0',
			height: 35
		});
	},
	
	createEastCmp: function() {
		return {
			xtype: 'wtimpanel',
			presenceStatus: 'offline',
			//presenceStatus: WT.getVar('imPresenceStatus'),
			statusMessage: WT.getVar('imStatusMessage'),
			listeners: {
				presencestatusselect: 'onIMPanelPresenceStatusSelect',
				frienddblclick: 'onIMPanelFriendDblClick',
				chatdblclick: 'onIMPanelChatDblClick',
				addgroupchatclick: 'onIMPanelAddGroupChatClick'
			}
		};
	},
	
	createPortalButton: function(cfg) {
		return Ext.apply({
			xclass: 'WTA.ux.ServiceButtonPortal',
			sid: WT.ID,
			handler: 'onPortalButtonClick'
		}, cfg || {});
	},
	
	createTaskBar: function(cfg) {
		return Ext.apply({
			xtype: 'wttaskbar',
			items: [],
			listeners: {
				buttonclick: 'onTaskBarButtonClick',
				buttoncontextmenu: 'onTaskBarButtonContextMenu'
			}
		}, cfg || {});
	},
	
	/**
	 * Adds specified actions to wiewport's layout.
	 * @param {Ext.Action[]} acts Service actions to add.
	 */
	addNewActions: function(acts) {
		var me = this,
				north = me.lookupReference('north'),
				newtb = north.lookupReference('newtb'),
				newbtn = newtb.lookupReference('newbtn');
		
		var menu = newbtn.getMenu();
		Ext.each(acts, function(act) {
			menu.add(act);
		});
	},
	
	/*
	 * @private
	 * Adds passet config to chosen layout region.
	 * @param {String} region Border layout region
	 * @param {Ext.Component} cmp The component to add
	 */
	addToRegion: function(region, cmp) {
		var me = this;
		if (cmp) {
			if (cmp.isComponent) {
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
