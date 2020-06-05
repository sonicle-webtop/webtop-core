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
Ext.define('Sonicle.webtop.core.admin.view.LicenseLease', {
	extend: 'WTA.sdk.OkView',
	requires: [
		'Sonicle.String',
		'Sonicle.upload.Button'
	],
	
	dockableConfig: {
		iconCls: 'wtadm-icon-license',
		width: 550,
		height: 350
	},
	
	type: 'activation',
	
	viewModel: {
		data: {
			data: {
				domainId: null,
				serviceId: null,
				productCode: null,
				userId: null,
				string: null
			},
			mode: 'online'
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
		me.setViewTitle(me.mys.res('licenseLease.'+me.type+'.tit'));
		WTU.applyFormulas(vm, {
			foMode: WTF.radioGroupBind(null, 'mode', me.getId()+'-mode'),
			foIsModeAuto: WTF.foIsEqual(null, 'mode', 'online')
		});
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
						WTF.localCombo('id', 'desc', {
							xtype: 'socombobox',
							bind: '{data.userId}',
							allowBlank: false,
							store: {
								autoLoad: true,
								model: 'WTA.model.Simple',
								proxy: WTF.proxy(me.mys.ID, 'LookupDomainUsers', 'users', {
									extraParams: {domainId: me.getVM().get('data.domainId')}
								})
							},
							sourceField: 'id',
							listConfig: {
								sourceCls: 'wt-source'
							},
							disabled: 'deactivation' === me.type,
							fieldLabel: me.mys.res('licenseLease.fld-user.lbl'),
							anchor: '100%'
						}),
						{
							xtype: 'radiogroup',
							bind: {
								value: '{foMode}'
							},
							columns: 3,
							defaults: {
								name: me.getId()+'-mode'
							},
							items: [
								{inputValue: 'online', boxLabel: me.mys.res('licenseLease.fld-mode.online')},
								{inputValue: 'offline', boxLabel: me.mys.res('licenseLease.fld-mode.offline'), disabled: 'deactivation' === me.type}
							],
							fieldLabel: me.mys.res('licenseLease.fld-mode.lbl')
						}
					]
				}, {
					xtype: 'wttabpanel',
					reference: 'tpnlmain',
					bind: {
						disabled: '{foIsModeAuto}'
					},
					disabled: true,
					items: [
						{
							xtype: 'wtfieldspanel',
							title: me.mys.res('licenseLease.'+me.type+'.tit'),
							defaults: {
								labelAlign: 'top'
							},
							items: [
								{
									xtype: 'fieldcontainer',
									layout: {
										type: 'hbox',
										padding: '0 0 1 0' // fixes classic-theme bottom border issue
									},
									items: [
										{
											xtype: 'textarea',
											reference: 'fldstring',
											bind: '{data.string}',
											emptyText: me.mys.res('licenseLease.fld-string.'+me.type+'.emp.dummy'),
											allowBlank: true,
											selectOnFocus: true,
											margin: '0 5 0 0',
											flex: 1,
											height: '100%'
										}, {
											xtype: 'souploadbutton',
											tooltip: me.mys.res('license.btn-upload.tip'),
											ui: 'default-toolbar',
											iconCls: 'fa fa-upload',
											uploaderConfig: WTF.uploader(me.mys.ID, 'UploadLicense', {
												mimeTypes: [
													{title: 'License', extensions: 'lic'}
												]
											}),
											listeners: {
												fileuploaded: function(s,file,resp) {
													WT.ajaxReq(me.mys.ID, 'GetUploadedLicense', {
														params: {
															uploadId: resp.data.uploadId
														},
														callback: function(success,json) {
															if (json.license) {
																me.lref('fldstring').setValue(json.license);
															} else {
																WT.error(json.text);
															}
														}
													});							
												}
											}
										}
									],
									anchor: '100% 100%'
								}
							]
						}, {
							xtype: 'wtfieldspanel',
							layout: 'vbox',
							title: me.mys.res('licenseLease.request.tit'),
							hidden: true, // TODO: disabled until we complete impl.
							defaults: {
								labelAlign: 'top'
							},
							items: [
								{
									xtype: 'textfield',
									editable: false,
									selectOnFocus: true,
									fieldLabel: me.mys.res('licenseLease.fld-hwid.lbl'),
									width: '100%'
								}, {
									xtype: 'textarea',
									editable: false,
									selectOnFocus: true,
									fieldLabel: me.mys.res('licenseLease.fld-request.lbl'),
									width: '100%',
									flex: 1
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
						if ('activation' === me.type) {
							me.assignLicenseLeaseUI();
						} else {
							me.revokeLicenseLeaseUI();
						}
						
					}
				}, {
					text: WT.res('act-cancel.lbl'),
					handler: function() {
						me.closeView(false);
					}
				}
			]
		});
		
		vm.bind('{mode}', function(nv, ov) {
			var fld = me.lref('fldstring');
			fld.allowBlank = ('online' === nv);
			fld.isValid();
		});
	},
	
	viewokArgs: function(vm) {
		return [vm.get('data.userId')];
	},
	
	privates: {
		assignLicenseLeaseUI: function() {
			var me = this,
					vm = me.getVM();
			me.wait();
			me.mys.assignLicenseLease(vm.get('data.domainId'), vm.get('data.serviceId'), vm.get('data.productCode'), vm.get('data.userId'), vm.get('data.string'), {
				callback: function(success, data, json) {
					me.unwait();
					if (success) {
						me.okView();
					} else {
						WT.error(json.message);
					}
				}
			});
		},
		
		revokeLicenseLeaseUI: function() {
			var me = this,
					vm = me.getVM();
			me.wait();
			me.mys.revokeLicenseLease(vm.get('data.domainId'), vm.get('data.serviceId'), vm.get('data.productCode'), vm.get('data.userId'), vm.get('data.string'), {
				callback: function(success, data, json) {
					me.unwait();
					if (success) {
						me.okView();
					} else {
						WT.error(json.message);
					}
				}
			});
		}
	}
});
