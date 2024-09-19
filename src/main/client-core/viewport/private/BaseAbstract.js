/* 
 * Copyright (C) 2023 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2023 Sonicle S.r.l.".
 */
/**
 * avaiable CSS classes:
 * 
 * wt-viewport-content
 * -
 * wt-viewport-topbar
 * wt-viewport-topbar-watermark-bottom
 * wt-tool
 * wt-tool-bg
 * wt-tool-hd-wrap
 * wt-tool-hd
 * wt-tool-hd-icon
 * wt-tool-hd-title
 * wt-tool-collapse-button
 * wt-viewport-taskbar
 */
Ext.define('Sonicle.webtop.core.viewport.private.BaseAbstract', {
	alternateClassName: 'WTA.viewport.private.BaseAbstract',
	extend: 'WTA.viewport.private.Abstract',
	requires: [
		'Sonicle.button.Avatar',
		'Sonicle.toolbar.Image',
		'WTA.ux.NotificationButton',
		
		'WTA.ux.ViewWindow',
		'WTA.ux.IMButton',
		'WTA.ux.IMPanel',
		'WTA.ux.app.ServicesTools',
		'WTA.ux.app.taskbar.Bar',
		'WTA.ux.app.launcher.PortalButton',
		'WTA.ux.app.launcher.ServiceButton',
		
		'WTA.ux.app.launcher.MoreLinksButton',
		'WTA.ux.app.launcher.LinkButton',
		
		'WTA.ux.data.BadgeNotificationStore',
		'Sonicle.webtop.core.model.IMFriendGrid'
	],
	uses: [
		'Sonicle.Utils',
		'WTA.view.Options'
	],
	
	viewModel: {
		stores: {
			notifications: {
				type: 'wtbadgenotification'
			}
		}
	},
	
	getLauncher: function() {
		Ext.raise('Please implement \'getLauncher\'');
	},
	
	getPortalButton: function() {
		return this.getServiceButton(WT.ID);
	},
	
	getServiceButton: function(serviceId) {
		return this.getLauncher().getComponent(serviceId);
	},
	
	getTaskbar: function() {
		Ext.raise('Please implement \'getTaskbar\'');
	},
	
	getNewButtons: function() {
		return [this.topDockCmp().lookupReference('newtb').lookupReference('newbtn')];
	},
	
	getToolRegion: function() {
		return this.centerContentCmp().lookupReference('tools');
	},
	
	getMainRegion: function() {
		return this.centerContentCmp().lookupReference('mains');
	},
	
	addServiceButton: function(desc) {
		Ext.raise('Please implement \'addServiceButton\'');
	},
	
	addLinkButton: function(link) {
		Ext.raise('Please implement \'addLinkButton\'');
	},
	
	addNewActions: function(actions) {
		var newbtns = this.getNewButtons();
		Ext.each(newbtns, function(newbtn) {
			if (actions.length==0) newbtn.setHidden(true);
			else {
				Ext.each(actions, function(action) {
					newbtn.getMenu().add(action);
				});
			};
		});
	},
	
	addToolbarItem: function(desc, toolbar) {
		var card = this.topDockCmp().lookupReference('servicetb');
		card.add(toolbar);
		return toolbar;
	},
	
	addMainRegionItem: function(desc, main) {
		var card = this.getMainRegion();
		card.add(main);
		return main;
	},
	
	addToolRegionItem: function(desc, tool, moreHdItems, toolboxItems) {
		var me = this,
			card = me.getToolRegion(),
			tdocked = me.createToolRegionItemTopDockCfg(desc, moreHdItems, toolboxItems),
			bdocked = me.createToolRegionItemBottomDockCfg(desc); // Optional bottom-docked
		
		tool.setTitle(desc.getName());
		Sonicle.Utils.removePanelHeader(tool);
		tool.setBorder(false);
		tool.addCls('wt-tool');
		tool.addBodyCls('wt-tool-bg');
		tool.addDocked(Ext.apply(tdocked, {dock: 'top', referenceHolder: true}), 0);
		if (bdocked) tool.addDocked(Ext.apply(bdocked, {dock: 'bottom', referenceHolder: true}));
		card.add(tool);
		return tool;
	},
	
	returnTopDockCfg: function() {
		var me = this;
		
		return {
			xtype: 'container',
			referenceHolder: true,
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'container',
					layout: {
						type: 'hbox',
						align: 'middle'
					},
					cls: 'wt-viewport-topbar',
					height: me.topbarHeight(),
					items: me.createViewportTopbarItemsCfg(),
					width: '100%',
					flex: 1
				}, {
					xtype: 'component',
					autoEl: 'div',
					cls: 'wt-viewport-topbar-watermark-bottom'
				}
			]
		};
	},
	
	returnEastContentCfg: function() {
		if (WT.getVar('imEnabled') === true) {
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
		} else {
			return undefined;
		}
	},
	
	returnCenterContentCfg: function() {
		var me = this, size = WTA.ux.app.ServicesTools.calcToolSize();
		return {
			xtype: 'container',
			layout: 'border',
			items: [
				{
					region: 'west',
					xclass: 'WTA.ux.app.ServicesTools',
					reference: 'tools',
					stateful: true,
					stateId: WT.buildStateId('pnltool'),
					border: false,
					header: false,
					headerPosition: 'bottom', // Set placeholder's expand icon on the bottom
					maintainTitlePosition: true, // Set placeholder's expand icon on the bottom
					collapsible: false,
					collapsed: WT.plTags.desktop ? false : true,
					split: {
						performCollapse: false // Hide collapse button on the splitter-bar
					},
					layout: 'card',
					items: [],
					width: size.width,
					minWidth: me.getProperties().toolSplitterMinWidth
				}, {
					region: 'center',
					xtype: 'container',
					reference: 'mains',
					layout: 'card',
					items: []
				}
			]
		};
	},
	
	getProperties: function() {
		var me = this;
		return {
			topbarScale: me.topbarItemsScale(),
			topbarHeight: me.topbarHeight(),
			bottombarHeight: me.bottombarHeight(),
			toolHeaderHeight: me.toolHeaderHeight(),
			toolHeaderSeparatorHeight: me.toolHeaderSeparatorHeight(),
			toolHeaderBarScale: me.toolHeaderItemsScale(),
			toolSplitterMinWidth: me.toolSplitterMinWidth(),
			viewPrimaryButtonPosition: me.viewPrimaryButtonPosition()
			// NB: do NOT move getViewConstrainRegion and getViewMaximizeInsets 
			// otherwise you get js errors due to unready state of some components!
		};
	},
	
	getViewConstrainRegion: Ext.emptyFn,
	
	/*
	getViewConstrainRegion: function() {
		return Sonicle.Utils.relaxConstrainRegion(this.centerContentCmp().getConstrainRegion(), {left: true});
	},
	*/
	
	getViewMaximizeInsets: function() {
		var centerRegion = this.centerContentCmp().getRegion();
		return {
			top: centerRegion.top,
			left: 0,
			bottom: -this.bottomDockCmp().getHeight(),
			right: 0
		};
	},
	
	privates: {
		navbarItemsScale: function() {
			return 'medium';
		},
		
		topbarHeight: function() {
			return 48;
		},
		
		bottombarHeight: function() {
			return 35;
		},

		topbarItemsScale: function() {
			return 'medium';
		},
		
		toolHeaderHeight: function() {
			return 44;
		},
		
		toolHeaderSeparatorHeight: function() {
			return 0;
		},

		toolHeaderItemsScale: function() {
			return 'small';
		},
		
		toolSplitterMinWidth: function() {
			return 200;
		},
		
		viewPrimaryButtonPosition: function() {
			return 'tl';
		},
		
		measureToolHeaderHeight: function() {
			var card = this.getToolRegion(),
				tool0 = card.getComponent(0),
				dock0, height;
			if (tool0) {
				dock0 = tool0.getDockedComponent(0);
				if (dock0) height = dock0.getHeight();
			}
			return height;
		},
		
		doAddServiceButton: function(targetCmp, desc) {
			// Always inserts items at last-2 in order to support vertical centering
			targetCmp.insert(targetCmp.items.getCount()-2, {
				xclass: 'WTA.ux.app.launcher.ServiceButton',
				sid: desc.getId(),
				allowDepress: false,
				toggleGroup: this.launcherToggleGroup(),
				scale: this.navbarItemsScale(),
				toggleHandler: 'onLauncherButtonClick'
			});
		},
		
		doAddLinkButton: function(targetCmp, link) {
			var more = targetCmp.getComponent('morelinks');
			if (more) {
				more.setHidden(false);
				more.addLink(link);
			}
		},
		
		createViewportTopbarItemsCfg: function() {
			var me = this,
				menuTbItms = ['->'];
			
			if (!WT.plTags.phone && !Ext.isEmpty(WT.getMeetingProvider()) && WT.isPermitted(WT.ID, 'MEETING', 'CREATE')) {
				menuTbItms.push({
					xtype: 'button',
					reference: 'meetingbtn',
					iconCls: 'wt-icon-meeting',
					tooltip: WT.res('act-addNewMeeting.lbl'),
					handler: function() {
						var svc = WT.getApp().getService(WT.ID);
						svc.showNewMeetingUI();
					}
				});
			}
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
			menuTbItms.push(
				{
					xtype: 'wtnotificationbutton',
					bind: {
						store: '{notifications}',
						listeners: {
							callbackService: 'onCallbackService'
						}
					}
				},
				Ext.apply(me.createAvatarButtonCfg(), {
					fullName: WT.getVar('userDisplayName'),
					menu: {
						xtype: 'menu',
						plain: true,
						items: [
							{
								xtype: 'label',
								cls: 'wt-accountmenu-label1',
								text: WT.getVar('userDisplayName')
							}, {
								xtype: 'label',
								cls: 'wt-accountmenu-label2',
								text: WT.getVar('userProfileEmail')
							},
							'-',
							{
								itemId: 'whatsnew',
								text: WT.res('accountmenu.whatsnew.lbl'),
								iconCls: 'wt-icon-whatsnew',
								hidden: !WT.getVar('wtWhatsnewEnabled'),
								handler: 'onAccountMenuButtonClick'
							}, {
								itemId: 'options',
								text: WT.res('accountmenu.options.lbl'),
								iconCls: 'wt-icon-options',
								handler: 'onAccountMenuButtonClick'
							}, /*{
								itemId: 'addons',
								text: WT.res('accountmenu.addons.lbl'),
								handler: 'onAccountMenuButtonClick'
							},{
								itemId: 'feedback',
								text: WT.res('accountmenu.feedback.lbl'),
								hidden: !WT.isPermitted('FEEDBACK', 'MANAGE'),
								handler: 'onAccountMenuButtonClick'
							},*/
							'-',
							{
								itemId: 'logout',
								text: WT.res('accountmenu.logout.lbl'),
								iconCls: 'fas fa-arrow-right-from-bracket',
								handler: 'onAccountMenuButtonClick'
							}
						]
					}
				})
			);
			
			return [
				{
					xtype: 'toolbar',
					reference: 'newtb',
					referenceHolder: true,
					cls: 'wt-viewport-topbar',
					border: false,
					defaults: {
						scale: 'medium'
					},			
					style: {
						paddingTop: 0,
						paddingBottom: 0
					},
					items: [
						{
							xtype: 'splitbutton',
							reference: 'newbtn',
							text: WT.plTags.desktop ? WT.res('new.btn-new.lbl') : null,
							menu: [],
							handler: 'onNewActionButtonClick'
						}
					]
				}, {
					xtype: 'container',
					reference: 'servicetb',
					layout: 'card',
					defaults: {
						cls: 'wt-viewport-topbar',
						border: false,
						padding: 0
					},
					flex: 1
				}, {
					xtype: 'toolbar',
					reference: 'menutb',
					cls: 'wt-viewport-topbar',
					border: false,		
					style: {
						paddingTop: 0,
						paddingBottom: 0
					},
					items: menuTbItms
				}
			];
		},
		
		createAvatarButtonCfg: function() {
			return {
				xtype: 'soavatarbutton',
				scale: this.topbarItemsScale()
			};
		},
		
		createMoreLinksButtonCfg: function() {
			return {
				xclass: 'WTA.ux.app.launcher.MoreLinksButton',
				itemId: 'morelinks',
				tooltip: WT.res('wtmorelinksbutton.tip'),
				hidden: true
			};
		},
		
		createPortalButtonCfg: function() {
			return {
				xclass: 'WTA.ux.app.launcher.PortalButton',
				sid: WT.ID,
				toggleGroup: this.launcherToggleGroup(),
				handler: 'onPortalButtonClick'
			};
		},
		
		createToolRegionItemTopDockCfg: function(desc, moreHdItems, toolboxItems) {
			var me = this;
			Ext.iterate(moreHdItems, function(item) {
				if (item.isInstance) {
					item.setScale(me.toolHeaderItemsScale());
				} else {
					Ext.apply(item, {scale: me.toolHeaderItemsScale()});
				}
			});
			
			return {
				xtype: 'container',
				cls: 'wt-tool-hd-wrap wt-tool-bg',
				hidden: false,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				items: [
					{
						xtype: 'toolbar',
						border: false,
						cls: 'wt-tool-hd wt-tool-bg',
						items: Ext.Array.push(me.createToolRegionItemBaseHdToolbarItems(desc.getName(), toolboxItems), moreHdItems || [])
					}
				],
				height: me.toolHeaderHeight()
			};
		},
		
		createToolRegionItemBottomDockCfg: function(desc) {
			return undefined;
		},
		
		createToolRegionItemCollapseButton: function() {
			var me = this;
			return {
				xtype: 'tool',
				type: 'collapse-' + Ext.Component.DIRECTION_LEFT,
				cls: 'wt-tool-collapse-button',
				tooltip: WT.res('act-collapse.lbl'),
				handler: function() {
					me.getToolRegion().collapse();
				}
			};
		},
		
		createToolRegionItemBaseHdToolbarItems: function(name, toolboxItems) {
			//TODO: [new UI] what about core's actions (causals + activities)
			/*
			acts = WT.getApp().getService(WT.ID).getToolboxActions();
			*/
			
			return [
				{
					xtype: 'button',
					ui: 'default-toolbar',
					reference: 'toolboxbtn',
					iconCls: 'fas fa-bars',
					tooltip: WT.res('toolboxbtn.tip'),
					arrowVisible: false,
					menu: toolboxItems
				}, {
					xtype: 'tbtext',
					html: Sonicle.String.htmlEncode(name),
					style: 'padding-left:0px;user-select:none;',
					cls: 'wt-tool-hd-title'
				}
			];
		},
		
		createTaskBarCfg: function() {
			return {
				xtype: 'wttaskbar',
				cls: 'wt-viewport-taskbar',
				border: false,
				items: [],
				listeners: {
					buttonclick: 'onTaskBarButtonClick',
					buttoncontextmenu: 'onTaskBarButtonContextMenu'
				}
			};
		}
	}
});
