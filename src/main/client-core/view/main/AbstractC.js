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
Ext.define('Sonicle.webtop.core.view.main.AbstractC', {
	alternateClassName: 'WTA.view.main.AbstractC',
	extend: 'Ext.app.ViewController',
	
	svctbmap: null,
	toolmap: null,
	mainmap: null,
	viewCtsById: null,
	viewCtsByUTag: null,
	
	/**
	 * @property {Boolean} isActivating
	 * Tracks period during service transition between one to other.
	 */
	isActivating: false,
	
	/**
	 * @property {String} active
	 * Currently active service
	 */
	active: null,
	
	constructor: function() {
		var me = this;
		me.svctbmap = {};
		me.toolmap = {};
		me.mainmap = {};
		me.viewCtsById = {};
		me.viewCtsByUTag = {};
		me.callParent(arguments);
	},
	
	destroy: function() {
		var me = this;
		me.callParent(arguments);
		me.svctbmap = null;
		me.toolmap = null;
		me.mainmap = null;
		me.viewCtsById = null;
		me.viewCtsByUTag = null;
	},
	
	/**
	 * Adds passed components to wiewport's layout.
	 * @param {WTA.sdk.Service} svc The service instance.
	 */
	addServiceCmps: function(svc) {
		var me = this,
				north = null,
				tb = null, tool = null, main = null;
		
		if(me.svctbmap[svc.ID]) return; // Checks if service has been already added
		
		// Retrieves service toolbar
		if(Ext.isFunction(svc.getToolbar)) {
			tb = svc.getToolbar.call(svc);
		}
		if(!tb || !tb.isToolbar) {
			tb = Ext.create({xtype: 'toolbar'});
		}
		me.svctbmap[svc.ID] = tb.getId();
		north = me.lookupReference('north');
		north.lookupReference('servicetb').add(tb);
		
		// Retrieves service components
		if(Ext.isFunction(svc.getToolComponent)) {
			tool = svc.getToolComponent.call(svc);
		}
		if(Ext.isFunction(svc.getMainComponent)) {
			main = svc.getMainComponent.call(svc);
		}
		me.addServiceComponents(svc, tool, main);
	},
	
	addServiceComponents: function(svc, tool, main) {
		var me = this,
				vw = me.getView(),
				tStack = vw.getToolStack(),
				mStack = vw.getMainStack();
		
		if(!tool || !tool.isPanel) {
			tool = Ext.create({xtype: 'panel', header: false});
		} else {
			WTU.removeHeader(tool);
		}
		me.toolmap[svc.ID] = tool.getId();
		tStack.add(tool);
		
		if(!main || !main.isXType('container')) {
			main = Ext.create({xtype: 'panel'});
		}
		me.mainmap[svc.ID] = main.getId();
		mStack.add(main);
	},
	
	/**
	 * Shows specified service components.
	 * @param {WTA.sdk.Service} svc The service instance.
	 * @return {Boolean} True if components have been switched, false if already active.
	 */
	activateService: function(svc) {
		var me = this,
				id = svc.ID;
		// If already active...exits
		if(me.active === id) return false;
		me.isActivating = true;
		// Activate components
		me.setActiveServiceToolbar(svc);
		if(svc.hasNewActions()) me.setActiveNewAction(svc);
		me.setActiveToolboxAction(svc);
		me.setActiveServiceComponents(svc);
		me.setActiveServiceViews(svc);
		// -------------------
		me.active = id;
		me.isActivating = false;
		return true;
	},
	
	/**
	 * @private
	 * Sets specified service newAction as default.
	 * @param {WTA.sdk.Service} svc The service instance.
	 */
	setActiveNewAction: function(svc) {
		var me = this,
				north = me.lookupReference('north'),
				newtb = north.lookupReference('newtb'),
				newbtn = newtb.lookupReference('newbtn'),
				first;
		
		if(newbtn) {
			first = svc.getNewActions()[0];
			newbtn.activeAction = first;
			newbtn.setTooltip(first.getText());
			newbtn.setIconCls(first.getIconCls());
		}
	},
	
	setActiveToolboxAction: function(svc) {
		var me = this,
				toolsCount = me.getView().fixedToolsCount,
				north = me.lookupReference('north'),
				toolboxbtn = north.lookupReference('toolboxbtn'),
				menu = toolboxbtn ? toolboxbtn.menu : null,
				sidx = (toolsCount === 0) ? 0 : toolsCount+1,
				acts;
		
		if(menu) {
			WTU.removeItems(menu, sidx);
			acts = svc.getToolboxActions();
			Ext.iterate(acts, function(act) {
				menu.add(act);
			});
		}
	},
	
	/**
	 * @private
	 * Shows active service toolbar.
	 * @param {WTA.sdk.Service} svc The service instance.
	 */
	setActiveServiceToolbar: function(svc) {
		var me = this,
				north = me.lookupReference('north'),
				svctb = north.lookupReference('servicetb');
		
		svctb.getLayout().setActiveItem(me.svctbmap[svc.ID]);
	},
	
	/**
	 * @private
	 * Shows active service components.
	 * @param {WTA.sdk.Service} svc The service instance.
	 */
	setActiveServiceComponents: function(svc) {
		var me = this,
				vw = me.getView(),
				side = vw.getSide(),
				tStack = vw.getToolStack(),
				mStack = vw.getMainStack(),
				active;
		
		tStack.getLayout().setActiveItem(me.toolmap[svc.ID]);
		active = tStack.getLayout().getActiveItem();
		if(active) side.setTitle(active.getTitle());
		side.setWidth(svc.getVar('viewportToolWidth') || 200);
		mStack.getLayout().setActiveItem(me.mainmap[svc.ID]);
	},
	
	/**
	 * @private
	 * Shows active service views (if present).
	 * @param {WTA.sdk.Service} svc The service instance.
	 */
	setActiveServiceViews: function(svc) {
		var me = this,
				vw = me.getView(),
				taskbar = vw.getTaskBar();
		taskbar.setActiveService(svc.ID);
	},
	
	getIMButton: function() {
		var north = this.lookupReference('north');
		return north.lookupReference('imbtn');
	},
	
	getIMPanel: function() {
		return this.lookupReference('east');
	},
	
	onToolResize: function(s, w) {
		var me = this,
				active = me.active;
		if((me.isActivating === false) && active) {
			WT.ajaxReq(active, 'SetToolComponentWidth', {
				params: {
					width: w
				},
				callback: function(success) {
					// Updates option locally...
					if(success) WT.setVars(active, {'viewportToolWidth': w});
				}
			});
		}
	},
	
	onLauncherButtonClick: function(s) {
		WT.getApp().activateService(s.getItemId());
	},
	
	onNewActionButtonClick: function(s) {
		var act = s.activeAction;
		if(act) act.execute();
	},
	
	onMenuButtonClick: function(s) {
		var me = this, wnd;
		switch(s.getItemId()) {
			case 'logout':
				WT.logout();
				break;
			case 'feedback':
				me.showFeedback();
				break;
			case 'whatsnew':
				me.showWhatsnew();
				break;
			case 'addons':
				me.showAddons();
				break;
			case 'options':
				me.showOptions();
				break;
		}
	},
	
	/*
	onToolsMenuClick: function(s) {
		var core = WT.getApp().getService(WT.ID);
		switch(s.getItemId()) {
			case 'activities':
				core.showActivities();
				break;
			case 'causals':
				core.showCausals();
				break;
		}
	},
	*/
	
	onIMClick: function(s) {
		var east = this.lookupReference('east');
		east.toggleCollapse();
	},
	
	onIMPanelPresenceStatusChange: function(s, status) {
		var mys = WT.getApp().getService(WT.ID);
		mys.updateIMPresenceStatusUI(status);
	},
	
	onIMPanelFriendDblClick: function(s, friendId, friendNick, chatId) {
		var mys = WT.getApp().getService(WT.ID);
		mys.openChatRoomUI(chatId, friendNick);
	},
	
	onIMPanelChatDblClick: function(s, chatId, name) {
		var mys = WT.getApp().getService(WT.ID);
		mys.openChatRoomUI(chatId, name);
	},
	
	onIMPanelAddGroupChatClick: function(s) {
		var mys = WT.getApp().getService(WT.ID);
		mys.addGroupChat({
			callback: function(success, rec) {
				if (success) {
					mys.openChatRoomUI(rec.get('id'), rec.get('name'));
				}
			}
		});
	},
	
	onTaskBarButtonClick: function(s, btn, e) {
		var winId = btn.getItemId(),
				win = Ext.ComponentManager.get(btn.getItemId()),
				awin = Ext.WindowManager.getActive();
		if(win.hidden) {
			btn.disable();
			win.show(null, function() {
				btn.enable();
			});
		} else if(awin && (awin.getId() === winId)) {
			btn.disable();
			win.on('hide', function() {
				btn.enable();
			}, null, {single: true});
			win.minimize();
		} else {
			win.toFront();
		}
	},
	
	onTaskBarButtonContextMenu: function(s, btn, e) {
		WT.showContextMenu(e, this.getView().getRef('cxmTaskBar'), {winId: btn.getItemId()});
	},
	
	onTaskBarButtonContextClick: function(s) {
		var win = Ext.ComponentManager.get(WT.getContextMenuData().winId);
		switch(s.getItemId()) {
			case 'restore':
				if(win.isVisible()) {
					win.restore();
					win.toFront();
				} else {
					win.show();
				}
				break;
			case 'minimize':
				win.minimize();
				break;
			case 'maximize':
				win.maximize();
				win.toFront();
				break;
			case 'close':
				win.close();
				break;
		}
	},
	
	onCallbackService: function(s, rec) {
		var svc = WT.getApp().getService(rec.get('serviceId'));
		if (svc) svc.notificationCallback('badge', rec.getId(), Ext.JSON.decode(rec.get('data'), true));
	},
	
	createServiceView: function(svc, viewName, opts) {
		opts = opts || {};
		var me = this,
				tag = Ext.isEmpty(opts.tag) ? null : opts.tag,
				view, dockCfg, utag, win;
		
		opts.viewCfg = Ext.merge(opts.viewCfg || {}, {
			mys: svc,
			tag: tag
		});
		view = Ext.create(svc.preNs(viewName), opts.viewCfg);
		//dockCfg = Ext.merge(view.getDockableConfig(), opts.dockCfg || {});
		//view.setDockableConfig(dockCfg);
		dockCfg = view.getDockableConfig();
		utag = me.generateUTag(svc.ID, tag);
		
		win = Ext.create(Ext.apply({
			xtype: 'wtviewwindow',
			layout: 'fit',
			utag: utag,
			width: dockCfg.width,
			height: dockCfg.height,
			modal: dockCfg.modal,
			minimizable: dockCfg.minimizable,
			maximizable: dockCfg.maximizable,
			items: [view]
		}));
		me.viewCtsById[win.getId()] = win;
		if (utag) me.viewCtsByUTag[utag] = win;
		me.toggleWinListeners(win, 'on');
		me.getView().getTaskBar().addButton(win);
		return win;
	},
	
	hasServiceView: function(svc, tag) {
		var map = this.viewCtsByUTag,
				utag = this.generateUTag(svc.ID, tag);
		return map.hasOwnProperty(utag) && map[utag] !== undefined;
	},
	
	getServiceView: function(svc, tag) {
		var map = this.viewCtsByUTag,
				utag = this.generateUTag(svc.ID, tag);
		return map.hasOwnProperty(utag) ? map[utag] : undefined;
	},
	
	showBadgeNotification: function(svc, notification) {
		var sto = this.getStore('notifications'),
				rec = sto.getById(notification.id);
		if (rec === null) {
			sto.add(sto.createModel(notification));
		} else {
			rec.set(notification);
		}
	},
	
	clearBadgeNotification: function(svc, notificationTag) {
		var sto = this.getStore('notifications'),
				rec = sto.getById(notificationTag);
		if (rec !== null) sto.remove(rec);
	},
	
	showOptions: function() {
		var me = this,
				mys = WT.getApp().getService(WT.ID);
		
		me.createServiceView(mys, 'view.Options', {
			viewCfg: {
				profileId: WT.getVar('profileId')
			}
		}).show();
	},
	
	showAddons: function() {
		var me = this,
				mys = WT.getApp().getService(WT.ID);
		
		me.createServiceView(mys, 'view.Addons').show();
	},
	
	showWhatsnew: function(full) {
		var me = this,
				mys = WT.getApp().getService(WT.ID);
		
		me.createServiceView(mys, 'view.Whatsnew', {
			viewCfg: {
				full: full
			}
		}).show();
	},
	
	showFeedback: function() {
		var me = this,
				mys = WT.getApp().getService(WT.ID),
				vct;
		
		vct = me.createServiceView(mys, 'view.Feedback');
		vct.show(false, function() {
			vct.getComponent(0).beginNew();
		});
	},
	
	privates: {
		
		toggleWinListeners: function(win, fn) {
			var me = this;
			win[fn]('activate',  me.onWinActivate, me);
			win[fn]('hide',  me.onWinHide, me);
			win[fn]('destroy',  me.onWinDestroy, me);
			win[fn]('titlechange',  me.onWinTitleChange, me);
		},
		
		onWinActivate: function(s) {
			this.getView().getTaskBar().toggleButton(s, true);
		},

		onWinHide: function(s) {
			this.getView().getTaskBar().toggleButton(s, false);
		},

		onWinTitleChange: function(s) {
			this.getView().getTaskBar().updateButtonTitle(s);
		},

		onWinDestroy: function(s) {
			var me = this, utag = s.getUTag();
			me.getView().getTaskBar().removeButton(s);
			me.toggleWinListeners(s, 'un');
			delete me.viewCtsByUTag[utag];
			delete me.viewCtsById[s.getId()];
		},
		
		generateUTag: function(sid, tag) {
			return tag ? Sonicle.Crypto.md5(tag + '@' + sid) : null;
		}
	}
});
