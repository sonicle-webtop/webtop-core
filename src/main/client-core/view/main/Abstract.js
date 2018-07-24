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
	uses: [
		'WTA.view.Options'
	],
	mixins: [
		'Sonicle.mixin.RefHolder'
	],
	controller: Ext.create('WTA.view.main.AbstractC'),
	
	layout: 'fit',
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
		var me = this;
		
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
		
		var ldock = me.createLeftDockCmp(),
			rdock = me.createRightDockCmp(),
			bstyle = {
				borderTopWidth: '1px !important',
				borderBottomWidth: '1px !important'
			},
			cmp0;
		
		if (ldock) {
			bstyle = Ext.apply(bstyle, {
				borderLeftWidth: '1px !important'
			});
		}
		if (rdock) {
			bstyle = Ext.apply(bstyle, {
				borderRightWidth: '1px !important'
			});
		}
		cmp0 = me.add({
			xtype: 'panel',
			border: false,
			bodyStyle: bstyle,
			bodyCls: 'wt-viewport-body',
			layout: 'border'
		});
		
		me.addAsDocked(cmp0, me.createTopDockCmp(), 'top', 'tdock');
		me.addAsDocked(cmp0, me.createBottomDockCmp(), 'bottom', 'bdock');
		me.addAsDocked(cmp0, ldock, 'left', 'ldock');
		me.addAsDocked(cmp0, rdock, 'right', 'rdock');
		if (WT.getVar('imEnabled') === true) {
			me.addAsRegion(cmp0, me.createEastCmp(), 'east');
		}
		me.addAsRegion(cmp0, me.createCenterCmp(), 'center');
	},
	
	getCollapsible: function() {
		return this.getToolsCard();
	},
	
	getToolsCard: function() {
		return this.centerCmp().lookupReference('tool');
	},
	
	getMainsCard: function() {
		return this.centerCmp().lookupReference('main');
	},
	
	getTaskBar: function() {
		return this.bottomDockCmp().getComponent(0);
	},
	
	getPortalButton: Ext.emptyFn,
	
	addServiceButton: Ext.emptyFn,
	
	topDockCmp: function() {
		return this.lookupReference('tdock');
	},
	
	bottomDockCmp: function() {
		return this.lookupReference('bdock');
	},
	
	leftDockCmp: function() {
		return this.lookupReference('ldock');
	},
	
	rightDockCmp: function() {
		return this.lookupReference('rdock');
	},
	
	centerCmp: function() {
		return this.lookupReference('center');
	},
	
	createTopDockCmp: function() {
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
			menu: toolMnuItms,
			hidden: toolsCount===0
		}/*, '-'*/);
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
					}, {
						itemId: 'whatsnew',
						tooltip: WT.res('menu.whatsnew.tip'),
						disabled: !WT.getVar('wtWhatsnewEnabled'),
						iconCls: 'wt-menu-whatsnew',
						width: '4em',
						height: '4em'
					}, {
						itemId: 'feedback',
						tooltip: WT.res('menu.feedback.tip'),
						disabled: !WT.isPermitted('FEEDBACK', 'MANAGE'),
						iconCls: 'wt-menu-feedback',
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
			height: 48,
			items: [{
				xtype: 'toolbar',
				region: 'west',
				reference: 'newtb',
				referenceHolder: true,
				cls: 'wt-vieport-dock',
				border: false,
				defaults: {
					scale: 'medium'
				},			
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
					cls: 'wt-vieport-dock',
					border: false,
					padding: 0
				}
			}, {
				xtype: 'toolbar',
				region: 'east',
				reference: 'menutb',
				cls: 'wt-vieport-dock',
				border: false,
				defaults: {
					scale: 'medium'
				},			
				style: {
					paddingTop: 0,
					paddingBottom: 0
				},
				items: menuTbItms
			}]
		};
	},
	
	createBottomDockCmp: function() {
		return {
			xtype: 'container',
			layout: 'hbox',
			items: [
				this.createTaskBar({
					region: 'center',
					border: false,
					cls: 'wt-vieport-dock',
					height: '100%',
					flex: 1
				})
			],
			minHeight: 35
		};
	},
	
	createLeftDockCmp: Ext.emptyFn,
	
	createRightDockCmp: Ext.emptyFn,
	
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
	
	createCenterCmp: function() {
		return {
			xtype: 'container',
			layout: 'border',
			items: [{
				region: 'west',
				xtype: 'panel',
				reference: 'tool',
				split: true,
				collapsible: true,
				border: false,
				width: 200,
				minWidth: 100,
				layout: 'card',
				items: [],
				listeners: {
					resize: 'onToolResize'
				}
			}, {
				region: 'center',
				xtype: 'container',
				reference: 'main',
				layout: 'card',
				items: []
			}]
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
				tdock = me.topDockCmp(),
				newtb = tdock.lookupReference('newtb'),
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
	},
	
	addAsRegion: function(parent, cmp, region) {
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
			parent.add(cmp);
		}
	},
	
	addAsDocked: function(parent, cmp, dock, reference) {
		if (cmp) {
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
});
