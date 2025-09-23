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
Ext.define('Sonicle.webtop.core.viewport.private.ViewController', {
	alternateClassName: 'WTA.viewport.private.ViewController',
	extend: 'Ext.app.ViewController',
	requires: [
		'Sonicle.Crypto',
		'Sonicle.Utils'
	],
	
	svctbmap: null,
	toolmap: null,
	mainmap: null,
	
	viewsScaleFactor: {width: 1.0, height: 1.0},
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
		me.viewsScaleFactor = null;
		me.viewsMap = null;
		me.viewsByCt = null;
		me.viewsByTag = null;
		me.floatingWins = null;
		me.floatingWinsMap = null;
	},
	
	getIMButton: function() {
		var tdock = this.getView().topDockCmp();
		return tdock?tdock.lookupReference('imbtn'):null;
	},
	
	getIMPanel: function() {
		return this.getView().eastContentCmp();
	},
	
	addService: function(svc) {
		if (this.svctbmap[svc.ID]) return; // Checks if service has been already added
		var me = this,
			toolbar = svc.getRef(WTA.sdk.Service.TOOLBAR_REF_NAME),
			toolHdItems = svc.createServiceToolHeaderItems(),
			tool = svc.getRef(WTA.sdk.Service.TOOL_REF_NAME),
			main = svc.getRef(WTA.sdk.Service.MAIN_REF_NAME),
			toolboxActions = svc.getToolboxActions();
		
		if (!toolbar) toolbar = svc.createServiceToolbar();
		if (!tool) tool = svc.createServiceTool();
		if (!main) main = svc.createServiceMain();
		
		me.addServiceComponents(svc, toolbar, tool, toolHdItems, main, toolboxActions);
	},
	
	addServiceComponents: function(svc, toolbar, tool, toolHdItems, main, toolboxItems) {
		var me = this,
			view = me.getView(),
			desc = WT.getApp().getDescriptor(svc.ID);
		
		if (!toolbar || !toolbar.isToolbar) toolbar = Ext.create(Ext.apply(toolbar || {}, {xtype: 'toolbar'}));
		view.addToolbarItem(desc, toolbar);
		me.svctbmap[svc.ID] = toolbar.getId();
		
		if (Ext.isDefined(tool)) {
			if (!tool.isInstance) tool = Ext.create(tool);//Ext.create(Ext.apply(tool || {}, {border: false}));
			if (tool.isXType('panel')) {
				view.addToolRegionItem(desc, tool, toolHdItems, toolboxItems);
				me.toolmap[svc.ID] = tool.getId();
			}
		}
		if (!Ext.isDefined(main) || !main.isXType('container')) {
			main = Ext.create(me.createDummyMain());
		} else if (!main.isInstance) {
			main = Ext.create(main);
		}
		view.addMainRegionItem(desc, main);
		me.mainmap[svc.ID] = main.getId();
	},
	
	/**
	 * Adds passed components to wiewport's layout.
	 * @param {WTA.sdk.Service} svc The service instance.
	 */
	addService__old: function(svc) {
		var me = this,
				tdock = me.getView().topDockCmp(),
				tb = null, tool = null, main = null;
		
		if (me.svctbmap[svc.ID]) return; // Checks if service has been already added
		
		// Retrieves toolbar component
		if (Ext.isFunction(svc.getToolbar)) tb = svc.getToolbar.call(svc);
		if (!tb || !tb.isToolbar) {
			tb = Ext.create({xtype: 'toolbar'});
		}
		me.svctbmap[svc.ID] = tb.getId();
		tdock.lookupReference('servicetb').add(tb);
		
		// Retrieves tool/main component
		if (Ext.isFunction(svc.getToolComponent)) tool = svc.getToolComponent.call(svc);
		if (Ext.isFunction(svc.getMainComponent)) main = svc.getMainComponent.call(svc);
		me.addServiceComponents__old(svc, tool, main);
	},
	
	addServiceComponents__old: function(svc, tool, main) {
		var me = this,
				vw = me.getView(),
				tools = vw.getToolRegion(),
				mains = vw.getMainRegion();
		
		if (Ext.isDefined(tool) && tool.isXType('container')) {
			WTU.removeHeader(tool);
			me.toolmap[svc.ID] = tool.getId();
			tools.add(tool);
		}
		if (!Ext.isDefined(main) || !main.isXType('container')) {
			main = Ext.create(me.createDummyMain());
		}
		me.mainmap[svc.ID] = main.getId();
		mains.add(main);
	},
	
	addServiceButton: function(desc) {
		if (desc.getId() !== WT.ID) {
			this.getView().addServiceButton(desc);
		}
	},
	
	addLinkButton: function(link) {
		this.getView().addLinkButton(link);
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
			id = svc.ID,
			btn;
		
		if (me.active === id) return false;
		me.isActivating = true;
		
		// Activate components
		btn = me.getView().getServiceButton(id);
		if (btn) btn.setPressed(true);
		me.setActiveToolbar(svc);
		if (svc.hasNewActions()) me.setActiveNewActions(svc);
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
	
	onAccountMenuButtonClick: function(s) {
		var me = this;
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
		var im = this.getIMPanel();
		if (im) im.toggleCollapse();
	},
	
	onIMPanelPresenceStatusSelect: function(s, status) {
		var me = this,
				mys = WT.getApp().getService(WT.ID);
		mys.updateIMPresenceStatus(status, {
			callback: function(success) {
				if (success) {
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
		
		if (win.isHidden()) {
			btn.disable();
			win.show(null, function() {
				btn.enable();
			});
			
		} else if (awin && (awin.getId() === winId)) {
			btn.disable();
			win.on('hide', function() {
				btn.enable();
			}, null, {single: true});
			win.minimize();
			
		} else {
			win.toFront();
		}
	},
	
	onTaskBarButtonContextClick: function(s,e) {
		var win = Ext.ComponentManager.get(e.menuData.winId);
		switch(s.getItemId()) {
			case 'restore':
				if (win.isHidden()) {
					win.show();
				} else {
					win.restore();
					win.toFront();
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
	
	onTaskBarButtonContextMenu: function(s, btn, e) {
		Sonicle.Utils.showContextMenu(e, this.getView().getRef('cxmTaskBar'), {winId: btn.getItemId()});
	},
	
	onCallbackService: function(s, rec) {
		var svc = WT.getApp().getService(rec.get('serviceId'));
		if (svc) svc.notificationCallback('badge', rec.getId(), Ext.JSON.decode(rec.get('data'), true));
	},
	
	countServiceViews: function() {
		return Ext.Object.getSize(this.viewsMap);
	},
	
	findServiceViewTag: function(cname, viewName) {
		var clazz = Ext.ClassManager.get(cname),
			vtag = clazz ? clazz.VIEW_TAG : null;
		if (Ext.isEmpty(vtag)) vtag = viewName.replaceAll('.', '').toLowerCase();
		return vtag;
	},
	
	hasServiceView: function(desc, tag) {
		var map = this.viewsByTag,
				utag = this.generateUTag(desc.getId(), tag);
		return map.hasOwnProperty(utag) && map[utag] !== undefined;
	},
	
	createServiceView: function(desc, viewName, opts) {
		opts = opts || {};
		var me = this,
			svcId = desc.getId(),
			svcInst = desc.getInstance(false),
			cname = desc.preNs(viewName),
			tag = (!Ext.isEmpty(opts.tag) ? opts.tag : me.findServiceViewTag(cname, viewName)) + (!Ext.isEmpty(opts.tagSuffix) ? opts.tagSuffix : ''),
			preventDup = (opts.preventDuplicates === true),
			floating = (opts.floating === true),
			swapReturn = (opts.swapReturn === true),
			utag = me.generateUTag(svcId, tag),
			view, win;
		
		if (!swapReturn && !floating) {
			Ext.log.warn("[WT.core] You are creating view '" + viewName + "' using a deprecated way. Consider adding 'swapReturn: true' in passed opts for returning the view instead of the container. Then call showView() on it.");
		}
		if (preventDup && !swapReturn) {
			Ext.log.warn("[WT.core] 'preventDuplicates: true' is supported only using 'swapReturn: true', return value may be wrong.");
		}
		if (preventDup && Ext.isEmpty(tag)) {
			Ext.log.warn("[WT.core] A value for 'tag' need to be specified using 'preventDuplicates: true'.");
		}
		
		if (preventDup && (view = me.getServiceView(desc, tag))) {
			return view;
		}
		
		opts.viewCfg = Ext.merge(opts.viewCfg || {}, {
			mys: svcInst ? svcInst : svcId,
			tag: tag
		});
		
		view = Ext.create(cname, opts.viewCfg);
		var viewport = me.getView(),
			dockCfg = view.getDockableConfig(),
			autoScale = dockCfg.autoScale === true,
			width = autoScale ? me.calcServiceViewWidth(dockCfg.width) : dockCfg.width,
			height = autoScale ? me.calcServiceViewHeight(dockCfg.height) : dockCfg.height,
			ctCfg = {};
		
		if (WT.plTags.desktop) {
			var constrainRegion = viewport.getViewConstrainRegion(),
				maximizeInsets = viewport.getViewMaximizeInsets(),
				align = floating ? 'br-br' : /*viewportProps.viewDefaultAlign ||*/ 'c-c';
			
			if (!floating) {
				if ('side' === dockCfg.dockPosition) {
					align = 'tr-tr';
					height = '100%';
				}
			}
			
			ctCfg = Ext.apply(ctCfg, {
				focusOnToFront: (dockCfg.focusOnShow === undefined) ? (floating ? false : true) : dockCfg.focusOnShow,
				minimizable: floating ? false : dockCfg.minimizable,
				maximizable: floating ? false : dockCfg.maximizable,
				maximizeInsets: maximizeInsets,
				resizable: floating ? false : true,
				draggable: floating ? false : true,
				maximized: floating ? false : dockCfg.maximized,
				minimized: floating ? false : dockCfg.minimized,
				closable: floating ? false : (Ext.isDefined(dockCfg.closable) ? dockCfg.closable:true),
				defaultAlign: align,
				width: width,
				height: height
			});
			if (constrainRegion) {
				Ext.apply(ctCfg, {
					constrainTo: constrainRegion,
					maxWidth: constrainRegion.width,
					maxHeight: constrainRegion.height
				});
			}
			
		} else {
			var align = 'c-c';
			ctCfg = Ext.apply(ctCfg, {
				focusOnToFront: dockCfg.focusOnShow,
				minimizable: dockCfg.minimizable,
				maximizable: false,
				resizable: false,
				draggable: false,
				maximized: true,
				minimized: false,
				defaultAlign: align,
				width: width,
				height: height
			});
		}
		
		win = Ext.create(Ext.apply(ctCfg, {
			xtype: 'wtviewwindow',
			layout: 'fit',
			utag: utag,
			constrain: true,
			modal: dockCfg.modal,
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
			var tb = me.getView().getTaskbar();
			if (tb) tb.addButton(win);
		}
		return (swapReturn || floating) ? view : win;
	},
	
	getServiceView: function(desc, tag) {
		var map = this.viewsByTag,
			utag = this.generateUTag(desc.getId(), tag);
		if (map.hasOwnProperty(utag)) {
			return Ext.getCmp(map[utag]);
		}
	},
	
	/**
	 * Calculates view's width scaling it according to configured scale factor.
	 * @param {Number} width
	 * @returns {Integer} The scaled width value
	 */
	calcServiceViewWidth: function(width) {
		return Math.round(width * this.viewsScaleFactor.width);
	},
	
	/**
	 * Calculates view's height scaling it according to configured scale factor.
	 * @param {Number} height
	 * @returns {Integer} The scaled height value
	 */
	calcServiceViewHeight: function(height) {
		return Math.round(height * this.viewsScaleFactor.height);
	},
	
	/**
	 * Adds a user notification. If a previous notification is found with 
	 * same tag the notification will be updated.
	 * @param {String} svc Service ID.
	 * @param {Object} notification Notification object.
	 */
	showBadgeNotification: function(svc, notification) {
		var sto = this.getStore('notifications'),
			rec = sto.getById(notification.tag);
		if (rec === null) {
			sto.add(sto.createModel(notification));
		} else {
			rec.set(notification);
		}
	},
	
	/**
	 * Removes a user notification.
	 * @param {String} svc Service ID.
	 * @param {String} tag Notification tag to clear.
	 */
	clearBadgeNotification: function(svc, tag) {
		var sto = this.getStore('notifications'),
				rec = sto.getById(tag);
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
			var newbtns = this.getView().getNewButtons(),
				first;
			Ext.each(newbtns, function(newbtn) {
				first = svc.getNewActions()[0];
				newbtn.activeAction = first;
				newbtn.setTooltip(first.getText());
				newbtn.setIconCls(first.getIconCls());
			});
		},
		
		setActiveToolbar: function(svc) {
			var me = this,
				tdock = me.getView().topDockCmp(),
				svctb = tdock.lookupReference('servicetb');

			svctb.getLayout().setActiveItem(me.svctbmap[svc.ID]);
		},
		
		setActiveComponents: function(svc) {
			var me = this,
				vw = me.getView(),
				tools = vw.getToolRegion(),
				mains = vw.getMainRegion(),
				cmp;
			
			cmp = tools.getComponent(me.toolmap[svc.ID]);
			tools.setActiveTool(svc, cmp);
			mains.getLayout().setActiveItem(me.mainmap[svc.ID]);
		},
		
		setActiveOnTaskbar: function(svc) {
			var me = this,
				vw = me.getView(),
				taskbar = vw.getTaskbar();
			if (taskbar) taskbar.setActiveService(svc.ID);
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
				var tb = me.getView().getTaskbar();
				if (tb) tb.toggleButton(s, true);
			}
		},

		onWinHide: function(s) {
			var me = this;
			if (me.floatingWinsMap[s.getId()] !== true) {
				var tb = me.getView().getTaskbar();
				if (tb) tb.toggleButton(s, false);
			}
		},

		onWinTitleChange: function(s) {
			var me = this;
			if (me.floatingWinsMap[s.getId()] !== true) {
				var tb = me.getView().getTaskbar();
				if (tb) tb.updateButtonTitle(s);
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
				var tb = me.getView().getTaskbar();
				if (tb) tb.removeButton(s);
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
				tb = me.getView().getTaskbar(),
				taskbarHeight = tb ? tb.getHeight() : 0,
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
		
		createDummyMain: function() {
			return {
				xtype: 'wtpanel',
				layout: 'center',
				bodyStyle: 'text-align:center;',
				items: [{
					xtype: 'wtpanel',
					html: 'This is your Main area, you should put some content here.<br>' +
							'Call <i>setMainComponent()</i> method in your service <i>init()</i> callback to achieve this.'
				}]
			};
		},
		
		generateUTag: function(sid, tag) {
			return tag ? Sonicle.Crypto.md5Hex(tag + '@' + sid) : null;
		}
	}
});
