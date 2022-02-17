/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.Loggers', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.String',
		'Sonicle.grid.column.Action',
		'Sonicle.webtop.core.admin.model.GridLogger'
	],
	uses: [
		'Sonicle.webtop.core.admin.view.LoggerEditor'
	],
	
	dockableConfig: {
		title: '{loggers.tit}',
		iconCls: 'wtadm-icon-loggers'
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
				model: 'Sonicle.webtop.core.admin.model.GridLogger',
				proxy: WTF.proxy(me.mys.ID, 'ManageLoggers', null, {
					extraParams: {
						crud: 'read'
					}
				}),
				sorters: [
					{
						sorterFn: function(r1, r2) {
							var n1 = r1.get('name'), n2 = r2.get('name');
							return "root" === n1.toLowerCase() ? 1 : (n1 > n2 ? 1 : (n1 === n2) ? 0 : -1);
						},
						direction: 'ASC'
					}
				]
			},
			columns: [
				{
					dataIndex: 'name',
					header: me.mys.res('loggers.gp.name.lbl'),
					renderer : function(v, meta, rec) {
						var ret = v;
						if (rec.isRoot()) {
							meta.tdCls = 'wt-italic';
							ret = me.mys.res('loggers.gp.name.root');
						}
						if (rec.isPending()) {
							ret += '<span class="wt-theme-text-lighter2" style="font-size:0.8em;">&nbsp;&nbsp;' + me.mys.res('loggers.gp.name.pending') + '</span>' ;
						}
						return ret;
					},
					flex: 1
				}, {
					dataIndex: 'level',
					header: me.mys.res('loggers.gp.level.lbl'),
					renderer : function(v, meta, rec) {
						if (!Ext.isEmpty(rec.get('overLevel'))) {
							meta.tdCls = 'wt-bold';
						}
						return v;
					},
					width: 80
				}, {
					xtype: 'soactioncolumn',
					header: me.mys.res('loggers.gp.actions.lbl'),
					items: [
						{
							iconCls: 'fas fa-paw',
							tooltip: 'TRACE',
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isRoot()) return true;
								return 'TRACE' === rec.get('level');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.setLevelUI(rec, 'TRACE');
							}
						}, {
							iconCls: 'fas fa-bug',
							tooltip: 'DEBUG',
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isRoot()) return true;
								return 'DEBUG' === rec.get('level');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.setLevelUI(rec, 'DEBUG');
							}
						}, {
							iconCls: 'fas fa-info-circle',
							tooltip: 'INFO',
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isRoot()) return true;
								return 'INFO' === rec.get('level');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.setLevelUI(rec, 'INFO');
							}
						}, {
							iconCls: 'fas fa-exclamation-triangle',
							tooltip: 'WARN',
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isRoot()) return true;
								return 'WARN' === rec.get('level');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.setLevelUI(rec, 'WARN');
							}
						}, {
							iconCls: 'fas fa-times-circle',
							tooltip: 'ERROR',
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isRoot()) return true;
								return 'ERROR' === rec.get('level');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.setLevelUI(rec, 'ERROR');
							}
						}, {
							iconCls: 'fas fa-power-off',
							tooltip: 'OFF',
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isRoot()) return true;
								return 'OFF' === rec.get('level');
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.setLevelUI(rec, 'OFF');
							}
						}, {
							iconCls: 'fas fa-undo',
							tooltip: me.mys.res('loggers.gp.actions.restore.tip'),
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isRoot()) return true;
								return Ext.isEmpty(rec.get('overLevel'));
							},
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.setLevelUI(rec, null);
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
						me.addLoggerUI();
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
	
	setLevel: function(name, level, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageLoggers', {
			params: {
				crud: 'update',
				name: name,
				level: level
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	privates: {
		addLoggerUI: function() {
			var me = this,
				vct = me.createLoggerEditor();
			
			vct.on('viewok', function(s, name, level) {
				me.wait();
				me.setLevel(name, level, {
					callback: function(success, data, json) {
						me.unwait();
						if (success) {
							me.lref('gp').getStore().load();
						} else {
							WT.error(json.message);
						}
					}
				});
			});
			vct.showView();
		},
		
		setLevelUI: function(rec, level) {
			var me = this,
					applyLevel = function() {
						me.wait();
						me.setLevel(rec.get('name'), level, {
							callback: function(success, data, json) {
								me.unwait();
								if (success) {
									if (data) {
										rec.set('effLevel', data.effLevel);
										rec.set('overLevel', data.overLevel);
									} else {
										me.lref('gp').getStore().remove(rec);
									}
								} else {
									WT.error(json.message);
								}
							}
						});
					};
			if (level === null) {
				WT.confirm(Ext.String.format(me.mys.res('loggers.confirm.reset'), rec.get('name')), function(bid) {
					if (bid === 'yes') applyLevel();
				}, me);
			} else {
				applyLevel();
			}
		},
		
		createLoggerEditor: function() {
			var me = this;
			return WT.createView(me.mys.ID, 'view.LoggerEditor', {
				swapReturn: true
			});
		}
	}
});
