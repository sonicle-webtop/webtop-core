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
	//viewCtsById: null,
	//viewCtsByUTag: null,
	
	viewsMap: null,
	viewsByCt: null,
	viewsByTag: null,
	floatingWins: null,
	floatingWinsMap: null,
	
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
		//me.viewCtsById = {};
		//me.viewCtsByUTag = {};
		
		me.viewsMap = {};
		me.viewsByCt = {};
		me.viewsByTag = {};
		me.floatingWins = [];
		me.floatingWinsMap = {};
		me.callParent(arguments);
	},
	
	destroy: function() {
		var me = this;
		me.callParent(arguments);
		me.svctbmap = null;
		me.toolmap = null;
		me.mainmap = null;
		//me.viewCtsById = null;
		//me.viewCtsByUTag = null;
		
		me.viewsMap = null;
		me.viewsByCt = null;
		me.viewsByTag = null;
		me.floatingWins = null;
		me.floatingWinsMap = null;
	},
	
	getIMButton: function() {
		var north = this.lookupReference('north');
		return north.lookupReference('imbtn');
	},
	
	getIMPanel: function() {
		return this.lookupReference('east');
	},
	
	/**
	 * Adds passed components to wiewport's layout.
	 * @param {WTA.sdk.Service} svc The service instance.
	 */
	addService: function(svc) {
		var me = this,
				north = null,
				tb = null, tool = null, main = null;
		
		if (me.svctbmap[svc.ID]) return; // Checks if service has been already added
		north = me.lookupReference('north');
		
		// Retrieves toolbar component
		if (Ext.isFunction(svc.getToolbar)) tb = svc.getToolbar.call(svc);
		if (!tb || !tb.isToolbar) {
			tb = Ext.create({xtype: 'toolbar'});
		}
		me.svctbmap[svc.ID] = tb.getId();
		north.lookupReference('servicetb').add(tb);
		
		// Retrieves tool/main component
		if (Ext.isFunction(svc.getToolComponent)) tool = svc.getToolComponent.call(svc);
		if (Ext.isFunction(svc.getMainComponent)) main = svc.getMainComponent.call(svc);
		me.addServiceComponents(svc, tool, main);
	},
	
	addServiceComponents: function(svc, tool, main) {
		var me = this,
				vw = me.getView(),
				tools = vw.getToolsCard(),
				mains = vw.getMainCard();
		
		if (!tool || !tool.isPanel) {
			tool = Ext.create({xtype: 'wtpanel', header: false});
		} else {
			WTU.removeHeader(tool);
		}
		me.toolmap[svc.ID] = tool.getId();
		tools.add(tool);
		
		if (!main || !main.isXType('container')) {
			main = Ext.create({xtype: 'wtpanel'});
		}
		me.mainmap[svc.ID] = main.getId();
		mains.add(main);
	},
	
	addServiceButton: function(desc) {
		if (desc.getId() !== WT.ID) {
			this.getView().addServiceButton(desc);
		}
	},
	
	addNewActions: function(acts) {
		this.getView().addNewActions(acts);
	},
	
	/**
	 * Shows specified service components.
	 * @param {WTA.sdk.Service} svc The service instance.
	 * @return {Boolean} True if components have been switched, false if already active.
	 */
	activateService: function(svc) {
		var me = this,
				id = svc.ID;
		
		if (me.active === id) return false;
		me.isActivating = true;
		
		// Activate components
		me.setActiveToolbar(svc);
		if (svc.hasNewActions()) me.setActiveNewActions(svc);
		me.setActiveToolboxAction(svc);
		me.setActiveComponents(svc);
		me.setActiveOnTaskbar(svc);
		
		me.active = id;
		me.isActivating = false;
		return true;
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
	
	onPortalButtonClick: function(s) {
		WT.getApp().activateService(WT.ID);
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
				WT.getApp().logout();
				break;
			case 'feedback':
				me.showFeedback();
				break;
			case 'whatsnew':
				me.showWhatsnew(true);
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
	
	onIMPanelPresenceStatusSelect: function(s, status) {
		var me = this,
				mys = WT.getApp().getService(WT.ID);
		mys.updateIMPresenceStatus(status, {
			callback: function(success) {
				if (success) {
					WT.setVar('imPresenceStatus', status);
					s.setPresenceStatus(status);
					me.getIMButton().setPresenceStatus(status);
				} else {
					s.setPresenceStatus(s.getPresenceStatus());
				}
			}
		});
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
		var me = this,
				mys = WT.getApp().getService(WT.ID);
		mys.addGroupChat({
			callback: function(success, rec) {
				if (success) {
					me.getIMPanel().loadChats();
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
	
	onTaskBarButtonContextClick: function(s,e) {
		var win = Ext.ComponentManager.get(e.menuData.winId);
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
	
	createServiceView: function(desc, viewName, opts) {
		opts = opts || {};
		var me = this,
				svcId = desc.getId(),
				svcInst = desc.getInstance(false),
				tag = Ext.isEmpty(opts.tag) ? null : opts.tag,
				floating = (opts.floating === true),
				swapReturn = (opts.swapReturn === true),
				utag = me.generateUTag(svcId, tag),
				view, win;
		
		if (!swapReturn && !floating) {
			Ext.log.warn("[WT.core] You are creating view '" + viewName + "' using a deprecated way. Consider adding 'swapReturn: true' in passed opts for returning the view instead of the container. Then call showView() on it.");
		}
		
		opts.viewCfg = Ext.merge(opts.viewCfg || {}, {
			mys: svcInst ? svcInst : svcId,
			tag: tag
		});
		
		var view = Ext.create(desc.preNs(viewName), opts.viewCfg),
				dockCfg = view.getDockableConfig();
		
		win = Ext.create(Ext.apply({
			xtype: 'wtviewwindow',
			layout: 'fit',
			utag: utag,
			constrain: true,
			width: dockCfg.width,
			height: dockCfg.height,
			modal: dockCfg.modal,
			minimizable: floating ? false : dockCfg.minimizable,
			maximizable: floating ? false : dockCfg.maximizable,
			resizable: floating ? false : true,
			draggable: floating ? false : true,
			defaultAlign: floating ? 'br-br' : 'c-c',
			tools: dockCfg.tools || [],
			items: [view]
		}));
		
		me.viewsMap[view.getId()] = true;
		me.viewsByCt[win.getId()] = view.getId();
		if (utag) me.viewsByTag[utag] = view.getId();
		me.toggleWinListeners(win, 'on');
		if (floating) {
			me.floatingWins.push(win.getId());
			me.floatingWinsMap[win.getId()] = true;
		} else {
			me.getView().getTaskBar().addButton(win);
		}
		return (swapReturn || floating) ? view : win;
	},
	
	hasServiceView: function(desc, tag) {
		var map = this.viewsByTag,
				utag = this.generateUTag(desc.getId(), tag);
		return map.hasOwnProperty(utag) && map[utag] !== undefined;
	},
	
	getServiceView: function(desc, tag) {
		var map = this.viewsByTag,
				utag = this.generateUTag(desc.getId(), tag);
		if (map.hasOwnProperty(utag)) {
			return Ext.getCmp(map[utag]);
		}
	},
	
	/*
	createServiceView_OLD: function(desc, viewName, opts) {
		opts = opts || {};
		var me = this,
				svcId = desc.getId(),
				svcInst = desc.getInstance(false),
				tag = Ext.isEmpty(opts.tag) ? null : opts.tag,
				view, dockCfg, utag, win;
		
		opts.viewCfg = Ext.merge(opts.viewCfg || {}, {
			mys: svcInst ? svcInst : svcId,
			tag: tag
		});
		view = Ext.create(desc.preNs(viewName), opts.viewCfg);
		//dockCfg = Ext.merge(view.getDockableConfig(), opts.dockCfg || {});
		//view.setDockableConfig(dockCfg);
		dockCfg = view.getDockableConfig();
		utag = me.generateUTag(svcId, tag);
		
		win = Ext.create(Ext.apply({
			xtype: 'wtviewwindow',
			layout: 'fit',
			utag: utag,
			constrain: true,
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
	
	hasServiceView_OLD: function(desc, tag) {
		var map = this.viewCtsByUTag,
				utag = this.generateUTag(desc.getId(), tag);
		return map.hasOwnProperty(utag) && map[utag] !== undefined;
	},
	
	getServiceView_OLD: function(desc, tag) {
		var map = this.viewCtsByUTag,
				utag = this.generateUTag(desc.getId(), tag);
		return map.hasOwnProperty(utag) ? map[utag] : undefined;
	},
	*/
	
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
		this.createServiceView(WT.getApp().getDescriptor(WT.ID), 'view.Options', {
			viewCfg: {
				profileId: WT.getVar('profileId')
			}
		}).show();
	},
	
	showAddons: function() {
		this.createServiceView(WT.getApp().getDescriptor(WT.ID), 'view.Addons').show();
	},
	
	showWhatsnew: function(full) {
		this.createServiceView(WT.getApp().getDescriptor(WT.ID), 'view.Whatsnew', {
			viewCfg: {
				full: full
			}
		}).show();
	},
	
	showFeedback: function() {
		var vct = this.createServiceView(WT.getApp().getDescriptor(WT.ID), 'view.Feedback');
		vct.show(false, function() {
			vct.getView().beginNew();
		});
	},
	
	privates: {
		setActiveNewActions: function(svc) {
			var me = this,
					north = me.lookupReference('north'),
					newtb = north.lookupReference('newtb'),
					newbtn = newtb.lookupReference('newbtn'),
					first;

			if (newbtn) {
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

			if (menu) {
				WTU.removeItems(menu, sidx);
				acts = svc.getToolboxActions();
				Ext.iterate(acts, function(act) {
					menu.add(act);
				});
			}
		},
		
		setActiveToolbar: function(svc) {
			var me = this,
					north = me.lookupReference('north'),
					svctb = north.lookupReference('servicetb');

			svctb.getLayout().setActiveItem(me.svctbmap[svc.ID]);
		},
		
		setActiveComponents: function(svc) {
			var me = this,
					vw = me.getView(),
					side = vw.getCollapsible(),
					tools = vw.getToolsCard(),
					mains = vw.getMainCard(),
					active;
			
			tools.getLayout().setActiveItem(me.toolmap[svc.ID]);
			active = tools.getLayout().getActiveItem();
			if (active) side.setTitle(active.getTitle());
			side.setWidth(svc.getVar('viewportToolWidth') || 200);
			mains.getLayout().setActiveItem(me.mainmap[svc.ID]);
		},
		
		setActiveOnTaskbar: function(svc) {
			var me = this,
					vw = me.getView(),
					taskbar = vw.getTaskBar();
			taskbar.setActiveService(svc.ID);
		},
		
		toggleWinListeners: function(win, fn) {
			var me = this;
			win[fn]('show',  me.onWinShow, me, {single: true});
			win[fn]('activate',  me.onWinActivate, me);
			win[fn]('hide',  me.onWinHide, me);
			win[fn]('destroy',  me.onWinDestroy, me);
			win[fn]('titlechange',  me.onWinTitleChange, me);
		},
		
		onWinShow: function(s) {
			var me = this;
			if (me.floatingWinsMap[s.getId()] === true) {
				me.arrangeFloatingWins();
			}
		},
		
		onWinActivate: function(s) {
			var me = this;
			if (me.floatingWinsMap[s.getId()] !== true) {
				me.getView().getTaskBar().toggleButton(s, true);
			}
		},

		onWinHide: function(s) {
			var me = this;
			if (me.floatingWinsMap[s.getId()] !== true) {
				me.getView().getTaskBar().toggleButton(s, false);
			}
		},

		onWinTitleChange: function(s) {
			var me = this;
			if (me.floatingWinsMap[s.getId()] !== true) {
				me.getView().getTaskBar().updateButtonTitle(s);
			}
		},

		onWinDestroy: function(s) {
			var me = this,
				winId = s.getId(),
				viewId = me.viewsByCt[winId],
				utag = s.getUTag(),
				floating = me.floatingWinsMap[winId] === true;
			
			if (!viewId) Ext.Error.raise('View not found by Ct ['+winId+']');
			if (!floating) {
				me.getView().getTaskBar().removeButton(s);
			}
			me.toggleWinListeners(s, 'un');
			
			Ext.Array.remove(me.floatingWins, winId);
			delete me.floatingWinsMap[winId];
			delete me.viewsByTag[utag];
			delete me.viewsByCt[winId];
			delete me.viewsMap[viewId];
			if (floating) {
				me.arrangeFloatingWins();
			}
		},
		
		arrangeFloatingWins: function() {
			var me = this,
					targetEl = Ext.getBody(),
					availWidth = targetEl.getWidth(true),
					arr = me.floatingWins,
					taskbarHeight = me.getView().getTaskBar().getHeight(),
					padding = {x: 10, y: taskbarHeight || 20},
					xspacing = 10, lineoff = 20,
					xoff = padding.x, yoff = padding.y,
					win;
			
			for (var i=0; i<arr.length; i++) {
				win = Ext.getCmp(arr[i]);
				if (!win) Ext.Error.raise('Window not found ['+arr[i]+']');
				if (win.getWidth() > availWidth) Ext.Error.raise('Window is too big ['+arr[i]+']');
				
				if ((xoff + win.getWidth() + xspacing) > availWidth) {
					xoff = padding.x;
					yoff += lineoff;
					
				}
				win.alignTo(targetEl, 'br-br', [-xoff, -yoff]);
				xoff += (win.getWidth() + xspacing);
			}
		},
		
		/*
		toggleWinListeners_OLD: function(win, fn) {
			var me = this;
			win[fn]('activate',  me.onWinActivate, me);
			win[fn]('hide',  me.onWinHide, me);
			win[fn]('destroy',  me.onWinDestroy, me);
			win[fn]('titlechange',  me.onWinTitleChange, me);
		},
		
		onWinActivate_OLD: function(s) {
			this.getView().getTaskBar().toggleButton(s, true);
		},

		onWinHide_OLD: function(s) {
			this.getView().getTaskBar().toggleButton(s, false);
		},

		onWinTitleChange_OLD: function(s) {
			this.getView().getTaskBar().updateButtonTitle(s);
		},

		onWinDestroy_OLD: function(s) {
			var me = this, utag = s.getUTag();
			me.getView().getTaskBar().removeButton(s);
			me.toggleWinListeners(s, 'un');
			delete me.viewCtsByUTag[utag];
			delete me.viewCtsById[s.getId()];
		},
		*/
		
		generateUTag: function(sid, tag) {
			return tag ? Sonicle.Crypto.md5(tag + '@' + sid) : null;
		}
	}
});
