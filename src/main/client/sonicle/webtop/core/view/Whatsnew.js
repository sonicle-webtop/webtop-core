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
	extend: 'WT.sdk.BaseView',
	
	layout: 'border',
	full: true,
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			items: [{
					xtype: 'tabpanel',
					region: 'center',
					itemId: 'wntab',
					plain: true,
					defaults: {
						autoScroll: true,
						bodyPadding: 5,
						bodyCls: 'wt-whatsnew'
					},
					listeners: {
						tabchange: function(s,nt,ot) {
							if(!nt.html && nt.loader) nt.loader.load();
						}
					},
					items: []
			}],
			fbar: [{
					xtype: 'checkbox',
					itemId: 'hide',
					value: true,
					boxLabel: WT.res('whatsnew.f-hide.lbl')
				}, '->', {
					xtype: 'button',
					text: WT.res('whatsnew.b-close.lbl'),
					handler: function() {
						var tb = me.getDockedItems('toolbar[dock="bottom"]')[0];
						if(tb.getComponent('hide').getValue()) me.turnOff();
						me.close();
					}
			}]
		});
		me.callParent(arguments);
		me.on('afterrender', function() {
			me.loadTabs();
		}, me, {single: true});
	},
	
	loadTabs: function() {
		var me = this;
		var tab = me.getComponent('wntab');
		tab.removeAll(true);
		WT.ajaxReq('com.sonicle.webtop.core', 'GetWhatsnewTabs', {
			params: {full: me.full},
			callback: function(success, o) {
				if(success) {
					Ext.each(o.data, function(itm) {
						tab.add(me.createTab(itm));
					}, me);
					tab.doLayout();
					if(tab.items.getCount() > 0) tab.setActiveTab(0);
				}
			}
		});
	},
	
	createTab: function(wn) {
		return Ext.create('Ext.panel.Panel', {
			itemId: wn.id,
			title: wn.title,
			loader: WT.componentLoader('com.sonicle.webtop.core', 'GetWhatsnewHTML', {
				params: {
					id: wn.id
				}
			})
		});
	},
	
	turnOff: function() {
		WT.ajaxReq('com.sonicle.webtop.core', 'TurnOffWhatsnew');
	}
});
