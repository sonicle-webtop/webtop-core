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
Ext.define('Sonicle.webtop.core.view.ViewportC', {
	extend: 'Ext.app.ViewController',
	
	tbmap: null,
	wpmap: null,
	
	constructor: function() {
		var me = this;
		me.tbmap = {};
		me.wpmap = {};
		me.callParent(arguments);
	},
	
	onLauncherButtonClick: function(s) {
		WT.getApp().activateService(s.getItemId());
	},
	
	onMenuButtonClick: function(s) {
		switch(s.getItemId()) {
			case 'logout':
				document.location = 'logout';
				break;
			case 'feedback':
				this.buildFeedbackWnd();
				break;
			case 'whatsnew':
				this.buildWhatsnewWnd();
				break;
			default:
				alert('Hai premuto il bottone '+s.getItemId());
		}
	},
	
	onToolResize: function(s, w) {
		WT.ajaxReq(WT.ID, 'SetToolComponentWidth', {
			params: {
				width: w
			}
		});
	},
	
	/**
	 * Adds passed components to wiewport's layout.
	 * @param {WT.sdk.Service} svc The service instance.
	 */
	addServiceCmp: function(svc) {
		var me = this;
		var w = me.getView();
		if(me.hasServiceCmp(svc.id)) return;
		
		// Gets service components
		var tb = null, tool = null, main = null;
		if(Ext.isFunction(svc.getToolbar)) {
			tb = svc.getToolbar.call(svc);
		}
		if(Ext.isFunction(svc.getToolComponent)) {
			tool = svc.getToolComponent.call(svc);
		}
		if(Ext.isFunction(svc.getMainComponent)) {
			main = svc.getMainComponent.call(svc);
		}
		
		if(!tb || !tb.isXType('toolbar')) {
			tb = Ext.create({xtype: 'toolbar'});
		}
		if(!tool || !tool.isXType('panel')) {
			tool = Ext.create({xtype: 'panel', region: 'west', split:true, collapsible: true, width: 200, minWidth: 100});
		} else {
			tool.setRegion('west');
			tool.split = true;
			tool.setCollapsible(true);
			tool.setMinWidth(100);
			tool.setWidth(svc.getInitialSetting('viewport.tool.width') || 200);
		}
		if(!main || !main.isXType('panel')) {
			main = Ext.create({xtype: 'panel', region: 'center', split:true});
		} else {
			main.setRegion('center');
			main.split = true;
		}
		tb.svcId = tool.svcId = main.svcId = svc.id;
		tool.on('resize', 'onToolResize');
		
		var wp = Ext.create({
			xtype: 'container',
			layout: 'border',
			items: [tool, main]
		});
		
		me.tbmap[svc.id] = tb.getId();
		me.wpmap[svc.id] = wp.getId();
		w.svctb.add(tb);
		w.svcwp.add(wp);
	},
	
	/**
	 * Checks if wiewport's layout contains service components.
	 * @param {String} id The service ID.
	 * @returns {Boolean} True if service's components have already been added.
	 */
	hasServiceCmp: function(id) {
		var me = this;
		return (me.tbmap[id] && me.wpmap[id]);
	},
	
	/**
	 * Activates specified service.
	 * @param {String} id The service ID.
	 */
	showService: function(id) {
		var me = this;
		var w = me.getView();
		w.svctb.getLayout().setActiveItem(me.tbmap[id]);
		w.svcwp.getLayout().setActiveItem(me.wpmap[id]);
	},
	
	buildFeedbackWnd: function() {
		var wnd = Ext.create({
			xtype: 'window',
			layout: 'fit',
			height: 320,
			width: 590,
			items: [
				Ext.create('Sonicle.webtop.core.view.Feedback')
			]
		});
		if(wnd) wnd.show();
	},
	
	buildWhatsnewWnd: function() {
		var wnd = Ext.create({
			xtype: 'window',
			layout: 'fit',
			height: 500,
			width: 600,
			items: [
				Ext.create('Sonicle.webtop.core.view.Whatsnew')
			]
		});
		if(wnd) wnd.show();
	}
});
