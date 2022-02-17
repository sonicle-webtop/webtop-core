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
Ext.define('Sonicle.webtop.core.admin.view.DomainLauncherLinks', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.grid.plugin.DDOrdering',
		'Sonicle.webtop.core.admin.model.GridDomainLauncherLink'
	],
	
	domainId: null,
	
	dockableConfig: {
		title: '{domainLauncherLinks.tit}',
		iconCls: 'wtadm-icon-launcherLinks'
	},
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		if (!cfg.title) {
			me.setBind({
				title: Ext.String.format('[{0}] ', cfg.domainId || '') + '{_viewTitle}'
			});
		}
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.core.admin.model.GridDomainLauncherLink',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainLauncherLinks', null, {
					extraParams: {
						domainId: me.domainId
					},
					writer: {
						allowSingle: false
					}
				})
			},
			viewConfig: {
				deferEmptyText: false,
				plugins: [{
					ptype: 'sogridviewddordering',
					orderField: 'order'
				}]
			},
			selModel: 'cellmodel',
			plugins: {
				ptype: 'cellediting',
				clicksToEdit: 2,
				autoEncode: true
			},
			columns: [
				{
					xtype: 'rownumberer'	
				}, {
					dataIndex: 'text',
					header: me.mys.res('domainLauncherLinks.gp.name.lbl'),
					flex: 1,
					editor: {
						field: {
							xtype: 'textfield',
							allowBlank: false
						}
					}
				}, {
					dataIndex: 'href',
					header: me.mys.res('domainLauncherLinks.gp.href.lbl'),
					flex: 2,
					editor: {
						field: {
							xtype: 'textfield',
							allowBlank: false
						}
					}
				}, {
					dataIndex: 'icon',
					header: me.mys.res('domainLauncherLinks.gp.icon.lbl'),
					flex: 2,
					renderer: function(value) {
						return Ext.isEmpty(value) ? '' : '<img src="' + value + '" style="width:24px; height:24px"/>';
					},
					editor: {
						field: {
							xtype: 'textfield',
							allowBlank: false
						}
					}
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'far fa-trash-alt',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteLauncherLinkUI(rec);
							}
						}
					]
				}
			],
			tbar: [
				me.addAct('add', {
					text: WT.res('act-add.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-add',
					handler: function() {
						me.addLauncherLinkUI();
					}
				}),
				'->',
				me.addAct('refresh', {
					text: null,
					tooltip: WT.res('act-refresh.lbl'),
					iconCls: 'wt-icon-refresh',
					handler: function() {
						me.lref('gp').getStore().load();
					}
				})
			]
		});
	},
	
	addLauncherLinkUI: function() {
		var gp = this.lref('gp'),
				ed = gp.findPlugin('cellediting'),
				col = gp.getColumnManager().getHeaderByDataIndex('text').getIndex(),
				sto = gp.getStore(),
				rec;
		ed.cancelEdit();
		rec = sto.add(sto.createModel({order: sto.getCount()}))[0];
		ed.startEditByPosition({row: sto.indexOf(rec), column: col});
	},
	
	deleteLauncherLinkUI: function(rec) {
		var me = this,
				sto = me.lref('gp').getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if (bid === 'yes') sto.remove(rec);
		}, me);
	}
});
