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
Ext.define('Sonicle.webtop.core.view.Whatsnew', {
	alternateClassName: 'WTA.view.Whatsnew',
	extend: 'WTA.sdk.DockableView',
	
	dockableConfig: {
		title: '{whatsnew.tit}',
		iconCls: 'wt-icon-whatsnew',
		width: 800,
		height: 500
	},
	promptConfirm: false,
	full: true,
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			fbar: [me.addRef('hide', Ext.create({
					xtype: 'checkbox',
					value: true,
					boxLabel: WT.res('whatsnew.fld-hide.lbl'),
					hidden: me.full
				})), '->', {
					xtype: 'button',
					text: WT.res('act-close.lbl'),
					handler: function() {
						//var tb = me.getDockedItems('toolbar[dock="bottom"]')[0];
						//if(tb.getComponent('hide').getValue()) me.turnOff();
						if(me.getRef('hide').getValue()) {
							me.turnOff();
						}
						me.closeView();
					}
			}]
		});
		me.callParent(arguments);
		
		me.add(me.addRef('wntab', Ext.create({
			xtype: 'tabpanel',
			region: 'center',
			plain: true,
			defaults: {
				autoScroll: true,
				bodyPadding: 5,
				bodyCls: 'wt-whatsnew'
			},
			listeners: {
				tabchange: function(s,nt,ot) {
					var iframe=nt.items.getAt(0);
					if(!iframe.html && iframe.loader) iframe.loader.load();
				}
			},
			items: []
		})));
		me.on('afterrender', function() {
			me.loadTabs();
		}, me, {single: true});
	},
	
	loadTabs: function() {
		var me = this,
				tab = me.getRef('wntab');
		
		tab.removeAll(true);
		WT.ajaxReq(me.mys.ID, 'GetWhatsnewTabs', {
			params: {full: me.full},
			callback: function(success, json) {
				if(success) {
					Ext.each(json['data'], function(itm) {
						tab.add(me.createTab(itm, me.full));
					}, me);
					tab.updateLayout();
					if(tab.items.getCount() > 0) tab.setActiveTab(0);
				}
			}
		});
	},
	
	createTab: function(wn, full) {
		return Ext.create('Ext.panel.Panel', {
			itemId: wn.id,
			title: wn.title,
			layout: 'fit',
			items: [
				Ext.create('Ext.ux.IFrame',{
					src: WTF.processUrl(this.mys.ID, 'GetWhatsnewHTML', {
							id: wn.id,
							full: full,
							nowriter: true
						})
				})
			]
		});
	},
	
	turnOff: function() {
		WT.ajaxReq(this.mys.ID, 'TurnOffWhatsnew');
	}
});
