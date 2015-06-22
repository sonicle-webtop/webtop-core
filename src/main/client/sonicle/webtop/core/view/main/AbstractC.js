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
	alternateClassName: 'WT.view.main.AbstractC',
	extend: 'Ext.app.ViewController',
	
	svctbmap: null,
	toolmap: null,
	mainmap: null,
	
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
		me.callParent(arguments);
	},
	
	/**
	 * Adds passed components to wiewport's layout.
	 * @param {WT.sdk.Service} svc The service instance.
	 */
	addServiceCmps: function(svc) {
		var me = this,
				id = svc.ID,
				north = null,
				tb = null, tool = null, main = null;
		
		if(me.svctbmap[id]) return; // Checks if service has been already added
		
		// Retrieves service toolbar
		if(Ext.isFunction(svc.getToolbar)) {
			tb = svc.getToolbar.call(svc);
		}
		if(!tb || !tb.isToolbar) {
			tb = Ext.create({xtype: 'toolbar'});
		}
		me.svctbmap[id] = tb.getId();
		north = me.lookupReference('north');
		north.lookupReference('svctb').add(tb);
		
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
	 * @param {WT.sdk.Service} svc The service instance.
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
		me.setActiveServiceComponents(svc);
		// -------------------
		me.active = id;
		me.isActivating = false;
		return true;
	},
	
	/**
	 * Shows specified service new action as default.
	 * @param {WT.sdk.Service} svc The service instance.
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
	
	setActiveServiceToolbar: function(svc) {
		var me = this,
				north = me.lookupReference('north'),
				svctb = north.lookupReference('svctb');
		
		svctb.getLayout().setActiveItem(me.svctbmap[svc.ID]);
	},
	
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
		side.setWidth(svc.getOption('viewportToolWidth') || 200);
		mStack.getLayout().setActiveItem(me.mainmap[svc.ID]);
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
					if(success) WT.setOptions(active, {'viewportToolWidth': w});
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
				document.location = 'logout';
				break;
			case 'feedback':
				wnd = me.buildFeedbackWnd();
				wnd.show(false, function() {
					wnd.getComponent(0).beginNew();
				});
				break;
			case 'whatsnew':
				wnd = me.buildWhatsnewWnd(true);
				wnd.show();
				break;
			case 'options':
				me.buildOptionsWnd2();
				break;
			default:
				alert('Hai premuto il bottone '+s.getItemId());
		}
	},
	
	buildOptionsWnd2: function() {
		var wnd = Ext.create({
			xtype: 'window',
			layout: 'fit',
			height: 500,
			width: 750,
			items: [
				Ext.create('WT.view.Options')
			]
		});
		if(wnd) wnd.show();
	},
	
	buildOptionsWnd: function() {
		var wnd = Ext.create({
			xtype: 'window',
			layout: 'fit',
			height: 500,
			width: 700,
			items: [{
				xtype: 'container',
				layout: 'card',
				activeItem: 'core',
				items: [
					Ext.create('WT.view.CoreOptions', {
						itemId: 'core',
						autoScroll: true,
						maxWidth: 600
					})
				]
			}, {
				xtype: 'panel'
			}]
		});
		if(wnd) wnd.show();
	},
	
	buildFeedbackWnd: function() {
		var wnd = Ext.create({
			xtype: 'window',
			layout: 'fit',
			width: 590,
			height: 320,
			items: [
				Ext.create('Sonicle.webtop.core.view.Feedback', {
					mys: WT.app.getService('com.sonicle.webtop.core')
				})
			]
		});
		return wnd;
	},
	
	buildWhatsnewWnd: function(full) {
		var wnd = Ext.create({
			xtype: 'window',
			layout: 'fit',
			width: 600,
			height: 500,
			items: [
				Ext.create('Sonicle.webtop.core.view.Whatsnew', {
					mys: WT.app.getService('com.sonicle.webtop.core'),
					full: full
				})
			]
		});
		return wnd;
	}
});
