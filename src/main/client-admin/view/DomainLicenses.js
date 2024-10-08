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
Ext.define('Sonicle.webtop.core.admin.view.DomainLicenses', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.String',
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.webtop.core.model.SubjectLkp',
		'Sonicle.webtop.core.admin.model.GridDomainLicenses'
	],
	uses: [
		'Sonicle.picker.List',
		'Sonicle.webtop.core.admin.view.License',
		'Sonicle.webtop.core.admin.view.LicenseActivatorWiz'
	],
	
	/**
	 * @cfg {String} domainId
	 * Target domain ID.
	 */
	domainId: null,
	
	dockableConfig: {
		title: '{domainLicenses.tit}',
		iconCls: 'wtadm-icon-licenses'
	},
	actionsResPrefix: 'domainLicenses',
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
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
		
		me.usersLkpStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.SubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupSubjects', null, {
				extraParams: {
					domainId: me.domainId,
					users: true
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
			border: false,
			store: {
				autoLoad: true,
				autoSync: true,
				model: 'Sonicle.webtop.core.admin.model.GridDomainLicenses',
				proxy: WTF.apiProxy(me.mys.ID, 'ManageDomainLicenses', null, {
					extraParams: {
						domainId: me.domainId
					},
					writer: {
						allowSingle: false // Always wraps records into an array
					}
				})
			},
			columns: [
				{
					xtype: 'rownumberer'	
				}, {
					xtype: 'solookupcolumn',
					dataIndex: 'serviceId',
					header: me.res('domainLicenses.gp.service.lbl'),
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.core.model.ServiceLkp',
						proxy: WTF.proxy(WT.ID, 'LookupServices')
					},
					displayField: 'desc',
					tooltipField: 'id',
					flex: 1
				}, {
					dataIndex: 'productName',
					header: me.res('domainLicenses.gp.product.lbl'),
					flex: 1
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'status',
					header: WTF.headerWithGlyphIcon('far fa-check-square'),
					getIconCls: function(v, rec) {
						if (rec.isValid()) {
							if (rec.isExpireSoon()) {
								return 'wt-icon-okwarn';
							} else {
								return 'wt-icon-ok';
							}
						} else if (rec.isActivationPening()) {
							return 'wt-icon-ok-yellow';
						} else { // invalid...
							return 'wt-icon-critical';
						}
					},
					getTip: function(v, rec) {
						var s = 'invalid';
						if (rec.isValid()) {
							if (rec.isExpireSoon()) {
								s = 'valid-warn';
							} else {
								s = 'valid';
							}
						} else if (rec.isActivationPening()) {
							s = 'pending';
						} else if (rec.isExpired()) {
							s = 'expired';
						}
						return me.res('domainLicenses.gp.status.'+s+'.tip');
					},
					iconSize: WTU.imgSizeToPx('xs'),
					width: 40
				}, /*{
					xtype: 'soiconcolumn',
					dataIndex: 'valid',
					header: WTF.headerWithGlyphIcon('far fa-check-square'),
					getIconCls: function(v, rec) {
						var status = rec.getStatus();
						switch(status) {
							case 'valid':
								return 'wt-icon-ok';
							case 'valid-warn':
								return 'wt-icon-okwarn';
							case 'pending':
								return 'wt-icon-ok-yellow';
							default:
								return 'wt-icon-critical';
						}
					},
					getTip: function(v, rec) {
						return me.res('domainLicenses.gp.status.'+rec.getStatus()+'.tip');
					},
					iconSize: WTU.imgSizeToPx('xs'),
					width: 40
				},*/ {
					xtype: 'datecolumn',
					dataIndex: 'expiry',
					format: WT.getShortDateFmt(),
					header: me.res('domainLicenses.gp.expiry.lbl'),
					//emptyCellText: '\u221e',
					usingDefaultRenderer: true, // Necessary for renderer usage below
					renderer : function(v, meta, rec) {
						if (rec.isExpired()) meta.tdCls = 'wt-theme-color-error';
						//if (rec.get('expired')) meta.tdCls = 'wt-theme-color-error';
						return Ext.isEmpty(v) ? '<span style="font-size:larger;">&#8734;</span>' : this.defaultRenderer(v);
					},
					align: 'center',
					width: 100
				}, {
					dataIndex: 'leasesCount',
					header: me.res('domainLicenses.gp.lease.lbl'),
					renderer : function(v, meta, rec) {
						if (!rec.get('quantityTypeUsers')) return '';
						var max = rec.get('maxLease');
						if (max > -1) {
							if (v >= max) meta.tdCls = 'wt-theme-color-error';
							return v + ' / ' + max;
						} else {
							return '<span style="font-size:larger;">&#8734;</span>';
						}
					},
					align: 'center',
					width: 80
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'autoLease',
					header: WTF.headerWithGlyphIcon('wtadm-glyph-licence-autolease'),
					getIconCls: function(v, rec) {
						if (rec.isLeaseUnbounded()||!rec.get('quantityTypeUsers')) return 'wt-pointer-default';
						return v ? 'wtadm-icon-licenseAutoLease-on' : 'wtadm-icon-licenseAutoLease-off';
					},
					getTip: function(v, rec) {
						if (rec.isLeaseUnbounded()||!rec.get('quantityTypeUsers')) return '';
						return {title: me.res('domainLicenses.gp.autoLease.'+v+'.tip.tit'), text: me.res('domainLicenses.gp.autoLease.'+v+'.tip.txt')};
						//return Sonicle.String.htmlEncodeLineBreaks(me.res('domainLicenses.gp.autoLease.'+v+'.tip'));
					},
					handler: function(w, ridx, cidx, e, rec) {
						if (rec.isLeaseUnbounded()) return;
						var rec = w.getStore().getAt(ridx),
								prw = w.grid.findPlugin('rowwidget'),
								isExp = prw.recordsExpanded[rec.internalId];

						// Circumvent probable ExtJs bug, collapse rowwidget if it was expanded before operation...
						if (isExp) prw.toggleRow(ridx, rec);

						rec.set('autoLease', !rec.get('autoLease'));
					},
					iconSize: WTU.imgSizeToPx('xs'),
					width: 40
				}, {
					xtype: 'templatecolumn',
					header: me.res('domainLicenses.gp.details.lbl'),
					tpl: new Ext.XTemplate(
						'{[this.buildDetails(values)]}',
						{
							buildDetails: function(values) {
								var s = '';
								if (values['builtIn'] === true) {
									s += '<span class="wt-text-off wt-theme-text-color-off">' + me.res('domainLicenses.gp.details.builtin') + '</span>' ;
								} else {
									if (!Ext.isEmpty(values['hwId'])) {
										if (s.length !== 0) s += ' | ';
										s += values['hwId'];
									}
									if (values['maxLease'] > -1) {
										if (s.length !== 0) s += ' | ';
										s += Ext.String.format(me.res('domainLicenses.gp.details.'+values['quantityType']), values['maxLease']);
									}
									if (!Ext.isEmpty(values['regTo'])) {
										if (s.length !== 0) s += ' | ';
										s += Ext.String.format(me.res('domainLicenses.gp.details.regto'), values['regTo']);
									}
								}
								return s;
							}
						}
					),
					flex: 3
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'wtadm-glyph-user-add',
							tooltip: me.mys.res('act-assignLicenseLease.lbl'),
							handler: function(view, ridx, cidx, itm, e, rec) {
								me.assignLicenseLeaseUI(rec);
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isBuiltIn()) return true;
								if (!rec.get('quantityTypeUsers')) return true;
								if (!rec.isLeaseUnbounded()) {
									return rec.get('leasesCount') >= rec.get('maxLease');
								}
								return true;
							}
						}, {
							iconCls: 'wtadm-glyph-licence-activate',
							tooltip: me.mys.res('act-activateLicense.lbl'),
							handler: function(view, ridx, cidx, itm, e, rec) {
								me.activateLicenseUI(rec);
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isBuiltIn()) return true;
								return rec.isActivated();
							},
							getClass: function(v, meta, rec) {
								var cls = 'wtadm-glyph-licence-activate ';
								if (rec.isActivated()) cls += (Ext.baseCSSPrefix + 'hidden-display');
								return cls;
							}
						}, {
							iconCls: 'wtadm-glyph-licence-deactivate',
							tooltip: me.mys.res('act-deactivateLicense.lbl'),
							handler: function(view, ridx, cidx, itm, e, rec) {
								me.deactivateLicenseUI(rec);
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								if (rec.isBuiltIn()) return true;
								return !rec.isActivated();
							},
							getClass: function(v, meta, rec) {
								var cls = 'wtadm-glyph-licence-deactivate ';
								if (!rec.isActivated()) cls += (Ext.baseCSSPrefix + 'hidden-display');
								return cls;
							}
						}, {
							iconCls: 'wt-glyph-edit',
							tooltip: me.mys.res('act-changeLicense.lbl'),
							handler: function(view, ridx, cidx, itm, e, rec) {
								me.changeLicenseUI(rec);
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return rec.isBuiltIn();
							}
						}, {
							iconCls: 'wt-glyph-delete',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(view, ridx, cidx, itm, e, rec) {
								me.deleteLicenseUI(rec);
							},
							isActionDisabled: function(s, ridx, cidx, itm, rec) {
								return rec.isBuiltIn();
							}
						}
					]
				}
			],
			plugins: [
				{
					ptype: 'rowwidget',
					isRowExpanderHidden: function(record, rowIndex, colIndex, store, view) {
						return !record.get("quantityTypeUsers");
					},
					widget: {
						xtype: 'grid',
						autoLoad: true,
						title: me.mys.res('domainLicenses.gp.leases.tit'),
						bind: {
							store: '{record.leases}'
							//title: 'Orders for {record.name}'
						},
						viewConfig: {
							emptyText: me.mys.res('domainLicenses.gp.leases.emp')
						},
						columns: [
							{
								xtype: 'solookupcolumn',
								dataIndex: 'userId',
								store: me.usersLkpStore,
								displayField: 'labelNameWithDN',
								flex: 1
							}, {
								xtype: 'soactioncolumn',
								items: [
									{
										iconCls: 'fas fa-user-times',
										tooltip: me.mys.res('act-revokeLicenseLease.lbl'),
										handler: function(g, ridx) {
											var rec = g.getStore().getAt(ridx);
											me.revokeLicenseLeaseUI(rec);
										}
									}
								]
							}
						]
					}
				}
			],
			tbar: [
				me.addAct('add', {
					tooltip: null,
					iconCls: 'wt-icon-add',
					ui: '{tertiary|toolbar}',
					handler: function() {
						me.addLicenseUI();
					}
				}),
				'->',
				me.addAct('cleanup', {
					text: null,
					tooltip: {title: me.mys.res('domainLicenses.act-cleanupCache.lbl'), text: me.mys.res('domainLicenses.act-cleanupCache.tip')},
					iconCls: 'wt-icon-cleanup',
					handler: function() {
						me.wait();
						me.cleanupCache({
							callback: function(success) {
								me.unwait();
								if (success) WT.toast(me.res('domainLicenses.info.cacheCleared'));
							}
						});
					}
				}),
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
	
	cleanupCache: function(opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainLicenses', {
			params: {
				crud: 'cleanup',
				domainId: me.domainId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	privates: {
		addLicenseUI: function() {
			var me = this,
					vw = WT.createView(me.mys.ID, 'view.License', {
						swapReturn: true,
						viewCfg: {
							data: {
								domainId: me.domainId
							}
						}
					});

			vw.on('viewok', function(s, data) {
				me.lref('gp').getStore().load();
			});
			vw.showView();
		},
		
		changeLicenseUI: function(rec) {
			var me = this,
					gp = me.lref('gp'), vw;
			
			if (rec.isActivated()) {
				WT.warn(me.mys.res('domainLicenses.warn.activeonchange'));
				
			} else {
				vw = WT.createView(me.mys.ID, 'view.LicenseActivatorWiz', {
					swapReturn: true,
					viewCfg: {
						type: 'change',
						moreTitle: Ext.String.format('[{0} - {1}]', rec.get('productName'), rec.get('productCode')),
						data: {
							domainId: me.domainId,
							productId: rec.get('id')
						}
					}
				});
				vw.on('wizardcompleted', function(s) {
					gp.getStore().load();
				});
				vw.showView();
			}
		},

		deleteLicenseUI: function(rec) {
			var me = this;
			
			if (rec.isActivated()) {
				WT.warn(me.res('domainLicenses.warn.activeondelete'));
			} else {
				WT.confirmDelete(me.res('domainLicenses.confirm.delete', rec.get('productCode')), function(bid) {
					if (bid === 'ok') {
						me.mys.deleteLicense(me.domainId, rec.get('id'), {
							callback: function(success, data, json) {
								if (success) me.lref('gp').getStore().remove(rec);
								WT.handleError(success, json);
							}
						});
					}
				}, me);	
			}
		},
		
		activateLicenseUI: function(rec) {
			var me = this,
					gp = me.lref('gp'),
					vw = WT.createView(me.mys.ID, 'view.LicenseActivatorWiz', {
						swapReturn: true,
						viewCfg: {
							type: 'activation',
							moreTitle: Ext.String.format('[{0} - {1}]', rec.get('productName'), rec.get('productCode')),
							data: {
								domainId: me.domainId,
								productId: rec.get('id')
							}
						}
					});

			vw.on('wizardcompleted', function(s) {
				gp.getStore().load();
			});
			vw.showView();
		},
		
		deactivateLicenseUI: function(rec) {
			var me = this,
					gp = me.lref('gp'),
					vw = WT.createView(me.mys.ID, 'view.LicenseActivatorWiz', {
						swapReturn: true,
						viewCfg: {
							type: 'deactivation',
							moreTitle: Ext.String.format('[{0} - {1}]', rec.get('productName'), rec.get('productCode')),
							data: {
								domainId: me.domainId,
								productId: rec.get('id')
							}
						}
					});

			vw.on('wizardcompleted', function(s) {
				gp.getStore().load();
			});
			vw.showView();
		},

		assignLicenseLeaseUI: function(rec) {
			var me = this,
					gp = me.lref('gp'),
					prw = gp.findPlugin('rowwidget'),
					wasExp = prw.recordsExpanded[rec.internalId],
					usedUsers = Sonicle.Data.collectValues(rec.leases()),
					handler = function(s, vals, recs) {
						me.wait();
						me.mys.assignLicenseLease(me.domainId, rec.get('id'), vals, {
							callback: function(success, data, json) {
								me.unwait();
								if (success) {
									// Circumvent probable ExtJs bug, reopen rowwidget if it was expanded before operation...
									gp.getStore().load({
										callback: function() {
											if (wasExp) {
												// We must look for new record by id, reload alters previous passed instance!
												var nrec = gp.getStore().getById(rec.getId());
												if (nrec) prw.toggleRow(me.getRowIndexByRecord(gp, nrec), nrec);
											}
										}
									});
									/*
									var sto = rec.leases();
									sto.add(sto.createModel({
										userId: userId,
										serviceId: rec.get('serviceId'),
										productCode: rec.get('productCode')
									}));
									rec.updateLeaseCount();
									*/
								} else {
									WT.error(json.message);
								}
							}
						});
						me.userPicker.close();
						me.userPicker = null;
					};
			me.userPicker = me.createUserPicker(rec.get('maxLease') - usedUsers.length, handler);
			me.userPicker.getComponent(0).setSkipValues(usedUsers);
			me.userPicker.show();
		},

		revokeLicenseLeaseUI: function(rec) {
			var me = this,
					gp = me.lref('gp'),
					prw = gp.findPlugin('rowwidget'),
					prec = rec.store.getAssociatedEntity(),
					wasExp = prw.recordsExpanded[prec.internalId];

			WT.confirmDelete(me.res('domainLicenses.confirm.revoke', rec.get('userId')), function(bid) {
				if (bid === 'ok') {
					me.wait();
					me.mys.revokeLicenseLease(me.domainId, prec.get('id'), [rec.get('userId')], {
						callback: function(success, data, json) {
							me.unwait();
							if (success) {
								// Circumvent probable ExtJs bug, reopen rowwidget if it was expanded before operation...
								gp.getStore().load({
									callback: function() {
										if (wasExp) {
											// We must look for new record by id, reload alters previous passed instance!
											var nrec = gp.getStore().getById(prec.getId());
											if (nrec) prw.toggleRow(me.getRowIndexByRecord(gp, nrec), nrec);
										}
									}
								});
								//rec.store.remove(rec);
								//prec.updateLeaseCount();
							}
							WT.handleError(success, json);
						}
					});
				}
			}, me);
		},
		
		getRowIndexByRecord: function(grid, record) {
			var view = grid.getView(),
					node = view.getNodeByRecord(record);
			return view.indexOf(node);
		},
				
		getSelectedLicense: function() {
			var sel = this.lref('gp').getSelection();
			return sel.length === 1 ? sel[0] : null;
		},

		/*
		updateDisabled: function(action) {
			var me = this,
					dis = me.isDisabled(action);
			me.setActDisabled(action, dis);
		},
		*/

		createUserPicker: function(maxSelections, handler) {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: me.res('domainLicenses.userPicker.tit'),
				height: 350,
				items: [{
					xtype: 'solistpicker',
					store: me.usersLkpStore,
					valueField: 'id',
					displayField: 'labelNameWithDN',
					searchField: 'search',
					emptyText: WT.res('grid.emp'),
					searchText: WT.res('textfield.search.emp'),
					selectedText: WT.res('grid.selected.lbl'),
					okText: WT.res('act-ok.lbl'),
					cancelText: WT.res('act-cancel.lbl'),
					allowMultiSelection: true,
					maxSelections: maxSelections,
					listeners: {
						cancelclick: function() {
							if (me.userPicker) me.userPicker.close();
						}
					},
					handler: handler,
					scope: me
				}]
			});
		}

		/*
		isDisabled: function(action) {
			var me = this, sel;
			switch(action) {
				case 'remove':
					sel = me.getSelectedLicense();
					if(sel) {
						return false;
					} else {
						return true;
					}
			}
		}
		*/
	}
});
