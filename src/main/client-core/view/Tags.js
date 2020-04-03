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
Ext.define('Sonicle.webtop.core.view.Tags', {
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.webtop.core.model.Tag'
	],
	uses: [
		'Sonicle.webtop.core.view.TagEditor'
	],
	
	/**
	 * @cfg {Boolean} [enableSelection=true]
	 * Enable or disable items selection.
	 */
	enableSelection: true,
	
	/**
	 * @cfg {Object} [data]
	 * An object containing initial data values.
	 * 
	 * @cfg {String[]} [data.selection] Selected tag IDs.
	*/
	
	/**
	 * @event viewok
	 * Fires when view is confirmed
	 * @param {Sonicle.webtop.core.view.Tags} this This view.
	 * @param {Object} data An object containing final data values.
	 * 
	 * @param {String[]} data.selection
	 */
	
	dockableConfig: {
		title: '{tags.tit}',
		width: 320,
		height: 350,
		modal: true
	},
	promptConfirm: false,
	
	viewModel: {
		data: {
			data: {
				selection: null
			}
		}
	},
	defaultButton: 'btnok',
	
	/**
	 * @readonly
	 * @property {Number} syncCount
	 */
	syncCount: 0,
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				vm = me.getVM(),
				butt, tbar;
		
		if (ic.data) vm.set('data', ic.data);
		me.callParent(arguments);
		
		if (me.enableSelection) {
			butt = [
				{
					reference: 'btnok',
					formBind: true,
					text: WT.res('act-apply.lbl'),
					handler: function() {
						me.okView();
					}
				}, {
					text: WT.res('tags.new.lbl'),
					disabled: !WT.isPermitted(me.mys.ID, 'TAGS', 'MANAGE'),
					handler: function() {
						me.addTagUI();
					}
				}
			];
		} else {
			tbar = [
				me.addAct('add', {
					text: WT.res('act-add.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-add-xs',
					disabled: !WT.isPermitted(me.mys.ID, 'TAGS', 'MANAGE'),
					handler: function() {
						me.addTagUI();
					}
				}),
				'->',
				me.addAct('refresh', {
					text: '',
					tooltip: WT.res('act-refresh.lbl'),
					iconCls: 'wt-icon-refresh',
					handler: function() {
						me.lref('gp').getStore().load();
					}
				})
			];
		}
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			border: false,
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.core.model.Tag',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageTags', null, {
					writer: {
						allowSingle: false
					}
				}),
				sorters: [{property: 'builtIn', direction: 'DESC'}, 'name'],
				listeners: {
					load: function(s, recs) {
						if (s.loadCount === 1) {
							var sel = me.getVM().get('data.selection'), selRecs = [];
							if (sel && Ext.isArray(sel)) {
								Ext.iterate(recs, function(rec) {
									if (sel.indexOf(rec.getId()) !== -1) selRecs.push(rec);
								});
								me.lref('gp').getSelectionModel().select(selRecs, false, false);
							}
						}	
					}
				}
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: me.mys.res('grid.emp')
			},
			selModel: {
				type: me.enableSelection ? 'checkboxmodel' : 'rowmodel'
			},
			columns: [{
					dataIndex: 'name',
					renderer: function(val, meta, rec) {
						var SoS = Sonicle.String;
						return '<i class="fa fa-tag" aria-hidden="true" style="font-size:1.2em;color:' + SoS.deflt(rec.get('color'), '') + '"></i>&nbsp;&nbsp;' + SoS.deflt(rec.get('name'), '');
					},
					flex: 1
				}, {
					xtype: 'soactioncolumn',
					items: [{
						iconCls: 'fa fa-edit',
						tooltip: WT.res('act-edit.lbl'),
						handler: function(g, ridx) {
							var rec = g.getStore().getAt(ridx);
							me.editTagUI(rec);
						},
						isDisabled: function(s, ridx, cidx, itm, rec) {
							return !WT.isPermitted(me.mys.ID, 'TAGS', 'MANAGE') || rec.get('builtIn');
						}
					}, {
						iconCls: 'fa fa-trash',
						tooltip: WT.res('act-remove.lbl'),
						handler: function(g, ridx) {
							var rec = g.getStore().getAt(ridx);
							me.deleteTagUI(rec);
						},
						isDisabled: function(s, ridx, cidx, itm, rec) {
							return !WT.isPermitted(me.mys.ID, 'TAGS', 'MANAGE') || rec.get('builtIn');
						}
					}]
			}],
			tbar: tbar,
			buttons: butt
		});
	},
	
	okView: function() {
		var me = this,
			vm = me.getVM(),
				gp = me.lref('gp');
		if (me.enableSelection) vm.set('data.selection', WTU.collectIds(gp.getSelection()));
		me.fireEvent('viewok', me, vm.get('data'));
		me.closeView(false);
	},
	
	addTag: function(opts) {
		var me = this,
				pal = WT.getColorPalette('default'),
				rndColor = pal[Math.floor(Math.random() * pal.length)],
				vw = WT.createView(me.mys.ID, 'view.TagEditor', {
					swapReturn: true,
					viewCfg: {
						data: {
							color: Sonicle.String.prepend(rndColor, '#', true)
						},
						invalidNames: me.collectUsedNames()
					}
				});
		
		vw.on('viewok', function(s, data) {
			Ext.callback(opts.callback, opts.scope || me, [data]);
		});
		vw.showView();
	},
	
	editTag: function(id, name, color, opts) {
		var me = this,
				vw = WT.createView(me.mys.ID, 'view.TagEditor', {
					swapReturn: true,
					viewCfg: {
						mode: 'edit',
						data: {
							id: id,
							name: name,
							color: color
						},
						invalidNames: me.collectUsedNames(name)
					}
				});
		
		vw.on('viewok', function(s, data) {
			Ext.callback(opts.callback, opts.scope || me, [data]);
		});
		vw.showView();
	},
	
	privates: {
		syncChanges: function() {
			var me = this,
					sto = me.lref('gp').getStore();
			if (WT.isPermitted(me.mys.ID, 'TAGS', 'MANAGE') && WTU.needsSync(sto)) {
				sto.sync({
					success: function() {
						me.syncCount++;
					},
					failure: function() {
						sto.reload();
					}
				});
			}
		},
		
		collectUsedNames: function(skipName) {
			var gp = this.lref('gp'),
					sto = gp.getStore(),
					names = [], name;
			
			sto.each(function(rec) {
				name = rec.get('name');
				if (!Ext.isEmpty(skipName) && name === skipName) return;
				names.push(name);
			});
			return names;
		},
		
		addTagUI: function() {
			var me = this;
			me.addTag({
				callback: function(data) {
					var gp = me.lref('gp'),
							sto = gp.getStore(),
							added;
					added = sto.add(sto.createModel({
						name: data.name,
						color: data.color
					}));
					if (me.enableSelection) gp.getSelectionModel().select(added, true);
					me.syncChanges();
				}
			});
		},

		editTagUI: function(rec) {
			var me = this;
			me.editTag(rec.getId(), rec.get('name'), rec.get('color'), {
				callback: function(data) {
					var rec = me.lref('gp').getStore().getById(data.id);
					if (rec) {
						rec.set({name: data.name, color: data.color});
						me.syncChanges();
					}
				}
			});
		},

		deleteTagUI: function(rec) {
			var me = this,
					sto = me.lref('gp').getStore();

			WT.confirm(me.mys.res('tags.confirm.delete'), function(bid) {
				if (bid === 'yes') {
					sto.remove(rec);
					me.syncChanges();
				}
			}, me);
		}
	}
});

