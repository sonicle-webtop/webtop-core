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
Ext.define('Sonicle.webtop.core.admin.view.License', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.webtop.core.admin.model.ProductLkp',
		'Sonicle.form.Spacer'
	],
	
	dockableConfig: {
		title: '{license.tit}',
		iconCls: 'wtadm-icon-license',
		width: 650,
		height: 450
	},
	fieldTitle: 'productId',
	modelName: 'Sonicle.webtop.core.admin.model.License',
	
	domainId: null,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			xtype: 'wtfieldspanel',
			region: 'center',
			reference: 'pnlmain',
			modelValidation: true,
			defaults: {
				labelWidth: 120
			},
			items: [
				WTF.localCombo('id', 'label', {
					reference: 'fldservice',
					bind: '{record.serviceId}',
					anyMatch: true,
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.core.model.ServiceLkp',
						proxy: WTF.proxy(WT.ID, 'LookupServices')
					},
					fieldLabel: me.mys.res('license.fld-serviceId.lbl'),
					width: 400
				}),
				WTF.localCombo('productId', 'label', {
					reference: 'fldproductid',
					anyMatch: true,
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.core.admin.model.ProductLkp',
						proxy: WTF.proxy(me.mys.ID, 'LookupServicesProducts')
					},
					bind: {
						value: '{record.productId}',
						disabled: '{!record.serviceId}',
						filters: [{
							property: 'serviceId',
							value: '{record.serviceId}'
						}]
					},
					disabled: true,
					fieldLabel: me.mys.res('license.fld-productId.lbl'),
					width: 400
				}),
				{
					xtype: 'fieldcontainer',
					fieldLabel: me.mys.res('license.fld-license.lbl'),
					layout: {
						type: 'vbox',
						align: 'end'
					},
					items: [
						{
							xtype: 'textarea',
							reference: 'fldlicense',
							bind: '{record.license}',
							width: "100%",
							height: 250
						},
						Ext.create({
							xtype:'souploadbutton',
							tooltip: null,
							text: me.res('license.btn-upload.lbl'),
							uploaderConfig: WTF.uploader(me.mys.ID,'UploadLicense',{
								mimeTypes: [
								 {title: "License", extensions: "lic"}
								]
							}),
							listeners: {
								beforeupload: function(s,file) {
								},
								uploadcomplete: function(s,fok,ffailed) {
								},
								uploaderror: function(s, file, cause) {
								},
								uploadprogress: function(s,file) {
								},
								fileuploaded: function(s,file,resp) {
									WT.ajaxReq(me.mys.ID, 'GetUploadedLicense', {
										params: {
											uploadId: resp.data.uploadId,
										},
										callback: function(success,json) {
											if (json.license) {
												me.lref('fldlicense').setValue(json.license);
											} else {
												WT.error(json.text);
											}
										}
									});							
								}
							}
						})
					]
				}
			]
		});
		
		me.on('viewload', me.onViewLoad);
		me.on('viewinvalid', me.onViewInvalid);
	},
	
	onViewLoad: function(s, success) {
/*		if (!success) return;
		var me = this,
				fldproductid = me.lref('fldproductid');
		if (me.isMode(me.MODE_NEW)) {
			fldproductid.setDisabled(false);
			fldproductid.focus(true);
		} else {
			fldproductid.setDisabled(true);
		}*/
	},
	
	onViewInvalid: function(s, mo, errs) {
		WTU.updateFieldsErrors(this.lref('pnlmain'), errs);
	}
});
