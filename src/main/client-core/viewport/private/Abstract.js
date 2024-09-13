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
 * viewport:
 * 
 *      +-----------------------------------------+
 *      |           announcement-bar              |
 *      +---+-------------------------------------+
 *      |   |                                     |
 *      | d |                                     |
 *      | r |                                     |
 *      | a |          content-wrap               |
 *      | w |                                     |
 *      | e |                                     |
 *      | r |                                     |
 *      |   |                                     |
 *      +---+-------------------------------------+
 *
 * content-wrap:
 * 
 *      +-----------------------------------------+
 *      |                 t-dock                  |
 *      +---+---------------------------------+---+
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      | l |                                 | r |
 *      | - |                                 | - |
 *      | d |                                 | d |
 *      | o |             content             | o |
 *      | c |                                 | c |
 *      | k |                                 | k |
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      +---+---------------------------------+---+
 *      |                 b-dock                  |
 *      +-----------------------------------------+
 * 
 * content:
 * 
 *      +-----------------------------------------+
 *      |                (unused)                 |
 *      +---+---------------------------------+---+
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      | ( |                                 |   |
 *      | u |                                 |   |
 *      | n |                                 |   |
 *      | u |                                 | e |
 *      | s |             center              | a |
 *      | e |                                 | s |
 *      | d |                                 | t |
 *      | ) |                                 |   |
 *      |   |                                 |   |
 *      |   |                                 |   |
 *      +---+---------------------------------+---+
 *      |                (unused)                 |
 *      +-----------------------------------------+
 * 
 * avaiable CSS classes:
 * 
 * wt-viewport-content
 * wt-theme-text-header1 (to be reviewed)
 */
Ext.define('Sonicle.webtop.core.viewport.private.Abstract', {
	alternateClassName: 'WTA.viewport.private.Abstract',
	extend: 'Ext.container.Viewport',
	mixins: [
		'Sonicle.mixin.RefHolder'
	],
	requires: [
		'Sonicle.Announcement',
		'WTA.viewport.private.ViewController'
	],
	controller: Ext.create('WTA.viewport.private.ViewController'),
	
	layout: {
		type: 'vbox',
		align: 'stretch'
	},
	referenceHolder: true,
	
	miniMode: false,
	
	returnDrawerCfg: Ext.emptyFn,
	returnTopDockCfg: Ext.emptyFn,
	returnBottomDockCfg: Ext.emptyFn,
	returnLeftDockCfg: Ext.emptyFn,
	returnRightDockCfg: Ext.emptyFn,
	returnEastContentCfg: Ext.emptyFn,
	returnCenterContentCfg: Ext.emptyFn,
	
	constructor: function(cfg) {
		var me = this;
		me.mixins.refholder.constructor.call(me, cfg);
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
			drawerWrap, drawerCfg, contentWrap;
		
		me.addRef('cxmTaskBar', Ext.create({
			xtype: 'menu',
			items: [
				{
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
				}
			],
			listeners: {
				beforeshow: function(s) {
					var ctxData = Sonicle.Utils.getContextMenuData(),
						win = Ext.ComponentManager.get(ctxData.winId);
					
					s.getComponent('restore').setDisabled(win ? !win.isRestorable() : true);
					s.getComponent('minimize').setDisabled(win ? !win.isMinimizable() : true);
					s.getComponent('maximize').setDisabled(win ? !win.isMaximizable() : true);
				}
			}
		}));
		
		me.callParent(arguments);
		
		// Announcement bar
		Sonicle.Announcement.register(me.add({
			xtype: 'soannouncementbar'
		}));
		
		//TODO: restore borders: see bstyle in original Abstract
		
		drawerCfg = me.returnDrawerCfg();
		if (drawerCfg) {
			drawerCfg = me.applyMoreCfg(drawerCfg, {
				reference: 'drawer',
				//cls: 'wt-viewport-drawer',
				height: '100%'
			});
			drawerWrap = me.add({
				xtype: 'container',
				layout: 'hbox',
				items: [
					drawerCfg,
					me.applyMoreCfg(me.createContentWrapCfg(), {height: '100%', flex: 1})
				],
				width: '100%',
				flex: 1
			});
			contentWrap = drawerWrap.getComponent(1);
		} else {
			contentWrap = me.add(me.applyMoreCfg(me.createContentWrapCfg(), {width: '100%', flex: 1}));
		}
		
		if (me.miniMode) {
			me.addAsRegion(me.createMiniModeCenterContentCfg(), contentWrap, 'center', 'ccontent');
		} else {
			me.addAsDocked(me.returnTopDockCfg(), contentWrap, 'top', 'tdock');
			me.addAsDocked(me.returnBottomDockCfg(), contentWrap, 'bottom', 'bdock');
			me.addAsDocked(me.returnLeftDockCfg(), contentWrap, 'left', 'ldock');
			me.addAsDocked(me.returnRightDockCfg(), contentWrap, 'right', 'rdock');
			me.addAsRegion(me.returnEastContentCfg(), contentWrap, 'east', 'econtent');
			me.addAsRegion(me.returnCenterContentCfg(), contentWrap, 'center', 'ccontent');
		}
	},
	
	privates: {
		drawerCmp: function() {
			return this.lookupReference('drawer');
		},

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

		eastContentCmp: function() {
			return this.lookupReference('econtent');
		},

		centerContentCmp: function() {
			return this.lookupReference('ccontent');
		},
		
		createContentWrapCfg: function() {
			return {
				xtype: 'panel',
				reference: 'contentwrap',
				border: false,
				//bodyStyle: bstyle, ??????????????????????
				bodyCls: 'wt-viewport-content',
				layout: 'border'
			};
		},
		
		createMiniModeCenterContentCfg: function() {
			return {
				xtype: 'container',
				layout: {
					type: 'vbox',
					pack: 'center',
					align: 'middle'
				},
				items: [
					{
						xtype: 'label',
						text: WT.res('viewport.minimode.message'),
						cls: 'wt-theme-text-header1',
						style: 'font-size:1.2em'
					}
				]
			};
		},
		
		launcherToggleGroup: function() {
			return this.getId() + '-launcher';
		},

		applyMoreCfg: function(cfg, moreCfg) {
			return cfg ? Ext.merge(cfg, moreCfg || {}) : cfg;
		},
		
		addAsRegion: function(cmp, parent, region, reference) {
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
		
		addAsDocked: function(cmp, parent, dock, reference) {
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
