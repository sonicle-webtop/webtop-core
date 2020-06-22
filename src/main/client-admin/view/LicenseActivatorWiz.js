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
Ext.define('Sonicle.webtop.core.admin.view.LicenseActivatorWiz', {
	extend: 'WTA.sdk.WizardView',
	requires: [
		'Sonicle.String',
		'WTA.ux.panel.Form'
	],
	
	dockableConfig: {
		iconCls: 'wtadm-icon-licenseActivatorWiz',
		width: 550,
		height: 400
	},
	useTrail: true,
	
	type: 'activation',
	moreTitle: '',
	viewModel: {
		data: {
			data: {
				domainId: null,	
				productId: null,
				response: null
			},
			reqdata: {
				url: null,
				request: null,
				hardwareId: null
			},
			mode: 'online'
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		Ext.apply(cfg, {
			confirmMsg: WT.res('wizard.confirm.close')
		});
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				vm = me.getVM();
		
		if (ic.data) vm.set('data', ic.data);
		me.setViewTitle(me.mys.res('licenseActivatorWiz.'+me.type+'.tit') + ' ' + me.moreTitle);
		me.callParent(arguments);
		me.on('beforenavigate', me.onBeforeNavigate);
	},
	
	addPathPage: function() {
		var me = this;
		me.getVM().set('path', 'online');
		me.add(me.createPathPage(
			me.mys.res('licenseActivatorWiz.path.'+me.type+'.tit'), 
			me.mys.res('licenseActivatorWiz.path.fld-path.tit'), 
			[
				{value: 'online', label: me.mys.res('licenseActivatorWiz.path.fld-path.online')},
				{value: 'offline', label: me.mys.res('licenseActivatorWiz.path.fld-path.offline')}
			]
		));
		me.onNavigate('path');
	},
	
	initPages: function() {
		var me = this;
		if (me.type === 'change') {
			return {
				online: ['input', 'info', 'finish'],
				offline: ['input', 'request', 'confirm', 'finish']
			};
		} else {
			return {
				online: ['info', 'finish'],
				offline: ['request', 'confirm', 'finish']
			};
		}
	},
	
	formatPageTitle: function(s, type, path) {
		return Ext.String.format(s, this.mys.res('licenseActivatorWiz.'+type) + ' ' + this.mys.res('licenseActivatorWiz.'+path));
	},
	
	createPages: function(path) {
		var me = this,
				SoS = Sonicle.String,
				XS = Ext.String,
				vm = me.getVM(),
				pages = [];
		
		if (me.type === 'change') {
			pages.push({
				xtype: 'wtwizardpage',
				itemId: 'input',
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				items: [
					{
						xtype: 'label',
						html: me.formatPageTitle(me.mys.res('licenseActivatorWiz.change.input.tit'), me.type, path),
						cls: 'x-window-header-title-default'
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'label',
						html: SoS.htmlEncodeLineBreaks(me.mys.res('licenseActivatorWiz.'+me.type+'.input.txt'))
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'textarea',
						reference: 'fldmodstring',
						bind: '{data.modString}',
						allowBlank: false,
						selectOnFocus: true,
						emptyText: me.mys.res('licenseActivatorWiz.'+me.type+'.fld-modString.emp'),
						flex: 1
					}, {
						xtype: 'fieldcontainer',
						layout: {type:'hbox', pack: 'end'},
						items: [
							{
								xtype: 'souploadbutton',
								tooltip: me.mys.res('licenseActivatorWiz.btn-load.tip'),
								ui: 'default-toolbar',
								iconCls: 'fa fa-upload',
								uploaderConfig: WTF.uploader(me.mys.ID, 'LicenseWizUploadFile', {
									mimeTypes: [
										{title: 'License', extensions: 'lic,l4j'}
									]
								}),
								listeners: {
									fileuploaded: function(s, file, resp) {
										WT.ajaxReq(me.mys.ID, 'LicenseWizLoadFromFile', {
											params: {
												uploadId: resp.data.uploadId
											},
											callback: function(success, json) {
												if (success) {
													me.getVM().set('data.modString', json.data);
												} else {
													WT.error(json.message);
												}
											}
										});
									}
								}
							}
						]
					}
				]
			});
		}
		
		if (path === 'online') {
			return Ext.Array.push(pages, [
				{
					xtype: 'wtwizardpage',
					itemId: 'info',
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					items: [
						{
							xtype: 'label',
							html: me.formatPageTitle(me.mys.res('licenseActivatorWiz.info.tit'), me.type, path),
							cls: 'x-window-header-title-default'
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'label',
							html: Sonicle.String.htmlEncodeLineBreaks(me.mys.res('licenseActivatorWiz.info.online.'+me.type+'.txt'))
						}
					]
				}, {
					xtype: 'wtwizardpage',
					itemId: 'finish',
					items: [
						{
							xtype: 'label',
							html: me.formatPageTitle(me.mys.res('licenseActivatorWiz.finish.tit'), me.type, path),
							cls: 'x-window-header-title-default'
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'label',
							html: Sonicle.String.htmlEncodeLineBreaks(me.mys.res('licenseActivatorWiz.finish.'+me.type+'.txt'))
						}
					]
				}
			]);
			
		} else if (path === 'offline') {
			me.loadRequestDataUI();
			return Ext.Array.push(pages, [
				{
					xtype: 'wtwizardpage',
					itemId: 'request',
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					items: [
						{
							xtype: 'label',
							html: me.formatPageTitle(me.mys.res('licenseActivatorWiz.request.tit'), me.type, path),
							cls: 'x-window-header-title-default'
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'label',
							html: Sonicle.String.htmlEncodeLineBreaks(me.mys.res('licenseActivatorWiz.request.offline.'+me.type+'.txt'))
						}, {
							xtype: 'solinkfield',
							bind: '{reqdata.url}'
						}, {
							xtype: 'textarea',
							region: 'center',
							bind: '{reqdata.request}',
							editable: false,
							selectOnFocus: true,
							labelAlign: 'top',
							fieldLabel: me.mys.res('licenseActivatorWiz.request.fld-request.lbl'),
							flex: 1
						}, {
							xtype: 'fieldcontainer',
							layout: {type:'hbox', pack: 'end'},
							items: [
								{
									xtype: 'button',
									tooltip: me.mys.res('licenseActivatorWiz.btn-save.tip'),
									ui: 'default-toolbar',
									iconCls: 'fa fa-download',
									handler: function() {
										Sonicle.URLMgr.downloadFile(WTF.processBinUrl(me.mys.ID, 'LicenseWizSaveToFile', {
											domainId: vm.get('data.domainId'),
											productId: vm.get('data.productId'),
											type: me.type
										}));
									}
								}
							]
						}, {
							xtype: 'textfield',
							bind: '{reqdata.hardwareId}',
							editable: false,
							selectOnFocus: true,
							fieldLabel: me.mys.res('licenseActivatorWiz.request.fld-hwid.lbl')
						}
					]
				}, {
					xtype: 'wtwizardpage',
					itemId: 'confirm',
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					items: [
						{
							xtype: 'label',
							html: me.formatPageTitle(me.mys.res('licenseActivatorWiz.confirm.tit'), me.type, path),
							cls: 'x-window-header-title-default'
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'label',
							html: Sonicle.String.htmlEncodeLineBreaks(me.mys.res('licenseActivatorWiz.confirm.offline.'+me.type+'.txt'))
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'textarea',
							reference: 'fldresponse',
							hidden: me.type === 'deactivation',
							bind: '{data.response}',
							allowBlank: false,
							selectOnFocus: true,
							labelAlign: 'top',
							fieldLabel: me.mys.res('licenseActivatorWiz.confirm.fld-response.lbl'),
							flex: 1
						}, {
							xtype: 'fieldcontainer',
							hidden: me.type === 'deactivation',
							layout: {type:'hbox', pack: 'end'},
							items: [
								{
									xtype: 'souploadbutton',
									tooltip: me.mys.res('licenseActivatorWiz.btn-load.tip'),
									ui: 'default-toolbar',
									iconCls: 'fa fa-upload',
									uploaderConfig: WTF.uploader(me.mys.ID, 'LicenseWizUploadFile', {
										mimeTypes: [
											{title: 'License', extensions: 'lic,l4j'}
										]
									}),
									listeners: {
										fileuploaded: function(s, file, resp) {
											WT.ajaxReq(me.mys.ID, 'LicenseWizLoadFromFile', {
												params: {
													uploadId: resp.data.uploadId
												},
												callback: function(success, json) {
													if (success) {
														me.getVM().set('data.response', json.data);
													} else {
														WT.error(json.message);
													}
												}
											});
										}
									}
								}
							]
						}
					]
				}, {
					xtype: 'wtwizardpage',
					itemId: 'finish',
					items: [
						{
							xtype: 'label',
							html: me.formatPageTitle(me.mys.res('licenseActivatorWiz.finish.tit'), me.type, path),
							cls: 'x-window-header-title-default'
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'label',
							html: Sonicle.String.htmlEncodeLineBreaks(me.mys.res('licenseActivatorWiz.finish.'+me.type+'.txt'))
						}
					]
				}
			]);
		}
	},
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if (dir === -1) return;
		var me = this,
				vm = me.getVM(),
				path = vm.get('path'),
				ppcmp = me.getPageCmp(pp);
		
		if (pp === 'input') {
			if (!ppcmp.lref('fldmodstring').isValid()) return false;
			
		} else if (path === 'online') {
			if (pp === 'info') {
				me.executeUI(null, function(success, data, json) {
					if (success) me.onNavigate(np);
				});
				return false;
			}
			
		} else if (path === 'offline') {
			if (pp === 'confirm') {
				if (me.type === 'activation') {
					if (!ppcmp.lref('fldresponse').isValid()) return false;
					me.executeUI(vm.get('data.response'), function(success, data, json) {
						if (success) me.onNavigate(np);
					});
					return false;
					
				} else {
					me.executeUI('dummyoffline', function(success, data, json) {
						if (success) me.onNavigate(np);
					});
					return false;
				}
			}
		}
	},
	
	loadRequestDataUI: function() {
		var me = this,
				vm = me.getVM(),
				domainId = vm.get('data.domainId'),
				productId = vm.get('data.productId'),
				cb = function(success, data, json) {
					me.unwait();
					if (success) {
						vm.set('reqdata', data);
					} else {
						WT.error(json.message);
					}
				};
		
		me.wait();
		if (me.type === 'change') {
			me.mys.getLicenseActivatorReqInfo('activation', domainId, productId, {callback: cb});
		} else if (me.type === 'activation' || me.type === 'deactivation') {
			me.mys.getLicenseActivatorReqInfo(me.type, domainId, productId, {callback: cb});
		} else {
			me.mys.getLicenseModifyReqInfo(domainId, productId, vm.get('data.modString'), {callback: cb});
		}
	},
	
	executeUI: function(response, callback) {
		var me = this,
				vm = me.getVM(),
				domainId = vm.get('data.domainId'),
				productId = vm.get('data.productId'),
				cb = function(success, data, json) {
					me.unwait();
					var newSuccess = success;
					if (!success) {
						/*
						var SoS = Sonicle.String,
								obj = WT.extractMessage(me.mys.ID, data),
								msg = obj.message;
						if (msg && SoS.contains(obj.key, 'serverunreachable')) {
							msg = msg + '\n' + me.mys.res('license.err.serverunreachable.'+me.type);
						}
						WT.error(SoS.coalesce(msg, json.message));
						*/
						var SoS = Sonicle.String,
								obj = WT.extractMessage(me.mys.ID, data);
						if (obj.message && SoS.contains(obj.key, 'serverunreachable')) {
							WT.error(obj.message + '\n' + me.mys.res('license.err.serverunreachable.'+me.type));
						} else if (obj.message && SoS.contains(obj.key, 'notfound')) {
							WT.warn(obj.message);
							newSuccess = true;
						} else {
							WT.error(SoS.coalesce(obj.message, json.message));
						}
					}
					Ext.callback(callback, me, [newSuccess, json.data, json]);
				};
		
		me.wait();
		if (me.type === 'change') {
			me.mys.changeLicense(domainId, productId, vm.get('data.modString'), response, {callback: cb});
		} else if (me.type === 'activation') {
			me.mys.activateLicense(domainId, productId, response, {callback: cb});
		} else {
			me.mys.deactivateLicense(domainId, productId, response, {callback: cb});
		}
	}
});
