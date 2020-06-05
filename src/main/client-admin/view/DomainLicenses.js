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
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.webtop.core.admin.model.DomainUserLkp',
		'Sonicle.webtop.core.admin.model.GridDomainLicenses'
	],
	uses: [
		'Sonicle.String'
	],
	
	domainId: null,
	
	dockableConfig: {
		title: '{domainLicenses.tit}',
		iconCls: 'wtadm-icon-licenses'
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
		
		me.usersLkpStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'Sonicle.webtop.core.admin.model.DomainUserLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupDomainUsers', 'users', {
				extraParams: {domainId: me.domainId}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'grid',
			reference: 'gp',
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
					header: me.mys.res('domainLicenses.gp.serviceId.lbl'),
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.core.model.ServiceLkp',
						proxy: WTF.proxy(WT.ID, 'LookupServices')
					},
					displayField: 'desc',
					tipField: 'id',
					flex: 1
				}, {
					xtype: 'solookupcolumn',
					dataIndex: 'id',
					header: me.mys.res('domainLicenses.gp.productCode.lbl'),
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.core.admin.model.ProductLkp',
						proxy: WTF.proxy(me.mys.ID, 'LookupServicesProducts')					
					},
					displayField: 'productName',
					tipField: 'productCode',
					flex: 1
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'valid',
					header: WTF.headerWithGlyphIcon('fa fa-check-square-o'),
					getIconCls: function(v, rec) {
						var status = rec.getStatus();
						switch(status) {
							case 'valid':
								return 'wt-icon-ok';
							case 'warn':
								return 'wt-icon-ok-warn';
							default:
								return 'wt-icon-critical';
						}
					},
					getTip: function(v, rec) {
						return me.mys.res('domainLicenses.gp.status.'+rec.getStatus()+'.tip');
					},
					iconSize: WTU.imgSizeToPx('xs'),
					width: 40
				}, {
					xtype: 'datecolumn',
					dataIndex: 'expiry',
					format: WT.getShortDateFmt(),
					header: me.mys.res('domainLicenses.gp.expiry.lbl'),
					//emptyCellText: '\u221e',
					width: 100
				}, {
					xtype: 'templatecolumn',
					header: me.mys.res('domainLicenses.gp.lease.lbl'),
					tpl: new Ext.XTemplate(
						'<tpl if="leaseAvail &gt; -1">',
							'{leaseCount} / {leaseAvail}',
						'<tpl else>',
							'<span style="font-size:larger;">&#8734;</span>',
						'</tpl>'
					),
					align: 'center'
				}, {
					xtype: 'soiconcolumn',
					dataIndex: 'autoLease',
					header: WTF.headerWithGlyphIcon('fa fa-cog'),
					getIconCls: function(v, rec) {
						if (rec.isLeaseUnbounded()) return 'wt-pointer-default';
						return v ? 'wtadm-icon-licenseAutoLease-on' : 'wtadm-icon-licenseAutoLease-off';
					},
					getTip: function(v, rec) {
						if (rec.isLeaseUnbounded()) return '';
						return Sonicle.String.htmlEncodeLineBreaks(me.mys.res('domainLicenses.gp.autoLease.'+v+'.tip'));
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
					header: me.mys.res('domainLicenses.gp.details.lbl'),
					tpl: new Ext.XTemplate(
						'{[this.buildDetails(values)]}',
						{
							buildDetails: function(values) {
								var s = '';
								if (!Ext.isEmpty(values['hwId'])) {
									if (s.length !== 0) s += ' | ';
									s += values['hwId'];
								}
								if (values['leaseAvail'] > -1) {
									if (s.length !== 0) s += ' | ';
									s += Ext.String.format(me.mys.res('domainLicenses.gp.details.users'), values['leaseAvail']);
								}
								if (!Ext.isEmpty(values['regTo'])) {
									if (s.length !== 0) s += ' | ';
									s += Ext.String.format(me.mys.res('domainLicenses.gp.details.regto'), values['regTo']);
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
							//iconCls: 'fa fa-link',
							iconCls: 'fa fa-user-plus',
							tooltip: me.mys.res('act-assignLicenseLease.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.assignLicenseLeaseUI(rec);
							},
							isDisabled: function(s, ridx, cidx, itm, rec) {
								if (!rec.isLeaseUnbounded()) {
									return rec.get('leaseCount') >= rec.get('leaseAvail');
								}
								return true;
							}
						}, {
							iconCls: 'fa fa-globe',
							tooltip: me.mys.res('act-updateLicenseOnlineInfo.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.updateOnlineInfoUI(rec);
							}
						}, {
							iconCls: 'fa fa-trash',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteLicenseUI(rec);
							}
						}
					]
				}
			],
			plugins: [
				{
					ptype: 'rowwidget',
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
								displayField: 'label',
								flex: 1
							}, {
								xtype: 'soactioncolumn',
								items: [
									{
										//iconCls: 'fa fa-chain-broken',
										iconCls: 'fa fa-user-times',
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
					text: WT.res('act-add.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-add-xs',
					handler: function() {
						me.addLicenseUI();
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
	
	updateOnlineInfo: function(serviceId, productCode, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.mys.ID, 'ManageDomainLicenses', {
			params: {
				crud: 'pullinfo',
				domainId: me.domainId,
				serviceId: serviceId,
				productCode: productCode
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
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

		deleteLicenseUI: function(rec) {
			var me = this;
			WT.confirm(me.mys.res('domainLicenses.confirm.delete'), function(bid) {
				if (bid === 'yes') {
					me.mys.deleteLicense(me.domainId, rec.get('serviceId'), rec.get('productCode'), {
						callback: function(success, data, json) {
							if (success) {
								me.lref('gp').getStore().remove(rec);
							} else {
								WT.error(json.message);
							}
						}
					});
				}
			}, me);	
		},

		updateOnlineInfoUI: function(rec) {
			var me = this;
			me.updateOnlineInfo(rec.get('serviceId'), rec.get('productCode'), {
				callback: function(success, data, json) {
					if (success) {
						me.lref('gp').getStore().load();
					} else {
						WT.error(json.message);
					}
				}
			});
		},
		
		getRowIndexByRecord: function(grid, record) {
			var view = grid.getView(),
					node = view.getNodeByRecord(record);
			return view.indexOf(node);
		},

		assignLicenseLeaseUI: function(rec) {
			var me = this,
					gp = me.lref('gp'),
					prw = gp.findPlugin('rowwidget'),
					wasExp = prw.recordsExpanded[rec.internalId],
					vw = WT.createView(me.mys.ID, 'view.LicenseLease', {
						swapReturn: true,
						viewCfg: {
							type: 'activation',
							data: {
								domainId: me.domainId,
								serviceId: rec.get('serviceId'),
								productCode: rec.get('productCode')
							}
						}
					});

			vw.on('viewok', function(s, userId) {
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
			});
			vw.showView();
		},

		revokeLicenseLeaseUI: function(rec) {
			var me = this,
					gp = me.lref('gp'),
					prw = gp.findPlugin('rowwidget'),
					prec = rec.store.getAssociatedEntity(),
					wasExp = prw.recordsExpanded[prec.internalId];

			WT.confirm(me.mys.res('domainLicenses.confirm.revoke', rec.get('userId')), function(bid) {
				if (bid === 'yes') {
					me.wait();
					me.mys.revokeLicenseLease(me.domainId, prec.get('serviceId'), prec.get('productCode'), rec.get('userId'), null, {
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
							} else {
								WT.error(json.message);
							}
						}
					});
				}
			}, me);
			
			
			/*
			var me = this,
					gp = me.lref('gp'),
					prw = gp.findPlugin('rowwidget'),
					prec = rec.store.getAssociatedEntity(),
					wasExp = prw.recordsExpanded[prec.internalId],
					vw = WT.createView(me.mys.ID, 'view.LicenseLease', {
						swapReturn: true,
						viewCfg: {
							type: 'deactivation',
							data: {
								domainId: me.domainId,
								serviceId: prec.get('serviceId'),
								productCode: prec.get('productCode'),
								userId: rec.get('userId')
							}
						}
					});

			vw.on('viewok', function(s, userId) {
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
			});
			vw.showView();
			*/
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
