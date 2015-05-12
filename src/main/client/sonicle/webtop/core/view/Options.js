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
Ext.define('Sonicle.webtop.core.view.Options', {
	alternateClassName: 'WT.view.Options',
	extend: 'WT.sdk.BaseView',
	requires: [
		'WT.model.Simple',
		'WT.sdk.UserOptionsView',
		'WT.sdk.UserOptionsController',
		'WT.sdk.OptionTabSection'
	],
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			items: [{
				xtype: 'toolbar',
				region: 'north',
				items: ['->', {
					xtype: 'combo',
					reference: 'users',
					editable: false,
					store: {
						autoLoad: true,
						model: 'WT.model.Simple',
						proxy: WTF.proxy('com.sonicle.webtop.core', 'GetOptionsUsers', 'users')
					},
					valueField: 'id',
					displayField: 'desc',
					width: 300,
					listeners: {
						change: {
							fn: function(s,nv) {
								me.updateGui(nv);
							}
						}
					},
					value: WT.getOption('principal')
				}]
			}, {
				xtype: 'tabpanel',
				region: 'center',
				reference: 'optstab',
				tabPosition: 'left',
				tabRotation: 0,
				maxWidth: 650,
				items: [],
				listeners: {
					tabchange: {
						fn: function(s,tab) {
							var id = me.lookupReference('users').getValue();
							tab.loadForm(id);
						}
					}
				}
			}]
		});
		me.callParent(arguments);		
		this.on('afterrender', this.onAfterRender, this);
	},
	
	onAfterRender: function() {
		var id = this.lookupReference('users').getValue();
		this.updateGui(id);
	},
	
	updateGui: function(id) {
		var me = this, uo = null;
		var data = [];
		me.wait();
		if(id === WT.getOption('principal')) { // User options are being edited by user itself
			var isAdmin = id === 'admin@*';
			Ext.each(WT.getApp().getDescriptors(false), function(desc) {
				if(isAdmin && desc.getIndex() > 0) return false;
				
				uo = desc.getUserOptions();
				if(uo) {
					Ext.Array.push(data, Ext.apply(uo, {
						id: desc.getId(),
						xid: desc.getXid(),
						name: desc.getName()
					}));
				}
			});
			me.createTabs(data);
			me.unwait();
			
		} else {
			WT.ajaxReq(WT.ID, 'GetOptionsServices', {
				params: {id: id},
				callback: function(success, json) {
					if(success) me.createTabs(json.data);
					me.unwait();
				}
			});
		}
	},
	
	createTabs: function(data) {
		var me = this;
		
		// Defines dependencies to load (viewClass and model)
		var dep = [];
		Ext.each(data, function(itm) {
			dep.push(itm.viewClassName);
			dep.push(itm.modelClassName);
		});
		
		Ext.require(dep, function() {
			var tab = me.lookupReference('optstab');
			tab.removeAll(true);
			Ext.each(data, function(itm) {
				tab.add(Ext.create(itm.viewClassName, {
					itemId: itm.id,
					model: itm.modelClassName,
					title: itm.name,
					iconCls: WTF.cssIconCls(itm.xid, 'service', 'xs'),
					ID: itm.id,
					XID: itm.xid
				}));
			});
			tab.setActiveTab(0);
		});
	}
	
	
	
	
	/*
	createTabs: function() {
		Ext.each(WT.getApp().getDescriptors(false), function(desc) {
			var cn = desc.getOptionsClassName();
			if(!Ext.isEmpty(cn)) {
				tab.add(Ext.create(cn, {
					itemId: desc.getId(),
					title: desc.getName(),
					iconCls: WTF.cssIconCls(desc.getXid(), 'service-s')
				}));
			}
		});
	}
	*/
});
