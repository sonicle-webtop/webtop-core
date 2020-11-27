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
Ext.define('Sonicle.webtop.core.admin.view.License', {
	extend: 'WTA.sdk.OkView',
	requires: [
		'Sonicle.String',
		'Sonicle.form.field.ComboBox',
		'Sonicle.upload.Button',
		'Sonicle.webtop.core.admin.model.ProductLkp'
	],
	
	dockableConfig: {
		title: '{license.tit}',
		iconCls: 'wtadm-icon-license',
		width: 550,
		height: 350
	},
	promptConfirm: false,
	writableOnly: false,
	
	viewModel: {
		data: {
			data: {
				domainId: null,
				productId: null,
				string: null
			}
		}
	},
	defaultButton: 'btnok',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				vm = me.getVM();
		
		if (ic.data) vm.set('data', ic.data);
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			border: false,
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'wtfieldspanel',
					items: [
						WTF.lookupCombo('id', 'productName', {
							xtype: 'socombo',
							bind: '{data.productId}',
							store: {
								autoLoad: true,
								model: me.mys.preNs('model.ProductLkp'),
								proxy: WTF.proxy(me.mys.ID, 'LookupServicesProducts'),
								grouper: {
									property: 'serviceId'
								}
							},
							listConfig: {
								displayField: 'productName',
								groupCls: 'wt-theme-text-greyed',
								sourceCls: 'wt-source'
							},
							sourceField: 'productCode',
							groupField: 'serviceName',
							fieldLabel: me.mys.res('license.fld-product.lbl'),
							emptyText: me.mys.res('license.fld-product.emp'),
							anchor: '100%'
						}),
						{
							xtype: 'checkbox',
							reference: 'fldactivatenow',
							checked: true,
							boxLabel: me.mys.res('license.fld-activateNow.lbl')
						}
					]
				}, {
					xtype: 'wtfieldspanel',
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					defaults: {
						labelAlign: 'top'
					},
					items: [
						{
							xtype: 'textarea',
							bind: '{data.string}',
							allowBlank: false,
							selectOnFocus: true,
							emptyText: me.mys.res('license.fld-string.emp'),
							flex: 1
						}, {
							xtype: 'fieldcontainer',
							layout: {type:'hbox', pack: 'end'},
							items: [
								{
									xtype: 'souploadbutton',
									tooltip: me.mys.res('license.btn-upload.tip'),
									ui: 'default-toolbar',
									iconCls: 'wt-icon-file-upload',
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
														me.getVM().set('data.string', json.data);
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
					],
					flex: 1
				}
			],
			buttons: [
				{
					reference: 'btnok',
					formBind: true,
					text: WT.res('act-ok.lbl'),
					handler: function() {
						me.addLicenseUI();
					}
				}, {
					text: WT.res('act-cancel.lbl'),
					handler: function() {
						me.closeView(false);
					}
				}
			]
		});
	},
	
	privates: {
		addLicenseUI: function() {
			var me = this,
					vm = me.getVM();
			me.wait();
			me.mys.addLicense(vm.get('data.domainId'), vm.get('data.productId'), vm.get('data.string'), me.lref('fldactivatenow').getValue(), {
				callback: function(success, data, json) {
					me.unwait();
					if (success) {
						me.okView();
					} else {
						var SoS = Sonicle.String,
								obj = WT.extractMessage(me.mys.ID, data);
						if (obj.message && SoS.contains(obj.key, 'serverunreachable')) {
							WT.error(obj.message + '\n' + me.mys.res('license.err.serverunreachable.activatenow'));
							me.okView();
						} else if (obj.message && SoS.contains(obj.key, 'activation')) {
							WT.error(obj.message + '\n' + me.mys.res('license.err.activation.activatenow'));
							me.okView();
						} else {
							WT.error(SoS.coalesce(obj.message, json.message));
						}
					}
				}
			});
		}
	}
});
