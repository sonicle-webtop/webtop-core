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
Ext.define('Sonicle.webtop.core.admin.Service', {
	extend: 'WT.sdk.Service',
	requires: [
		'Sonicle.webtop.core.admin.model.AdminNode',
		'Sonicle.webtop.core.admin.view.Settings'
	],
	
	init: function() {
		var me = this;
		
		
		me.setToolComponent(Ext.create({
			xtype: 'panel',
			layout: 'border',
			referenceHolder: true,
			title: '',
			items: [{
				region: 'center',
				xtype: 'treepanel',
				reference: 'tradmin',
				border: false,
				useArrows: true,
				rootVisible: false,
				store: {
					autoLoad: true,
					autoSync: true,
					model: 'Sonicle.webtop.core.admin.model.AdminNode',
					proxy: WTF.apiProxy(me.ID, 'ManageAdminTree', 'children', {
						writer: {
							allowSingle: false // Always wraps records into an array
						}
					}),
					root: {
						id: 'root',
						expanded: true
					}
				},
				hideHeaders: true,
				listeners: {
					selectionchange: function(s, rec) {
						console.log('selectionchange');
						/*
						var type = (rec.length === 1) ? rec[0].get('_type') : null;
						if(type === 'folder') {
							me.setCurNode(rec[0].getId());
						} else if(type === 'file') {
							me.setCurNode(rec[0].getId());
						} else {
							me.setCurNode(null);
						}
						*/
					},
					itemclick: function(s, rec, itm, i, e) {
						console.log('itemclick');
						
						var type = rec.get('_type');
						if(type === 'settings') {
							me.showSettings(rec);
						}
						
					},
					itemcontextmenu: function(s, rec, itm, i, e) {
						console.log('itemcontextmenu');
						/*
						var type = rec.get('_type');
						if(type === 'root') {
							me.setCurNode(rec.get('id'));
							WT.showContextMenu(e, me.getRef('cxmRootStore'), {node: rec});
						} else if(type === 'folder') {
							me.setCurNode(rec.get('id'));
							WT.showContextMenu(e, me.getRef('cxmStore'), {node: rec});
						} else if(type === 'file') {
							me.setCurNode(rec.get('id'));
							WT.showContextMenu(e, me.getRef('cxmFile'), {node: rec});
						} else {
							me.setCurNode(null);
						}
						*/
					}
				}
			}]
		}));
		
		me.setMainComponent(Ext.create({
			xtype: 'tabpanel'
		}));
	},
	
	showSettings: function(node) {
		var me = this,
				pnl = me.getMainComponent(),
				tab;
		
		tab = pnl.getComponent(node.getId());
		if(!tab) {
			pnl.add(Ext.create('Sonicle.webtop.core.admin.view.Settings', {
				mys: me,
				itemId: node.getId(),
				closable: true
			}));
		}
		pnl.setActiveTab(tab);
	}
});
