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
Ext.define('Sonicle.webtop.core.view.OTPSetupGoogleAuth', {
	extend: 'WT.sdk.WizardView',
	requires: [
		'Sonicle.form.field.Image',
		'Sonicle.form.field.DisplayImage',
		'Sonicle.form.field.Icon'
	],
	
	confirmMsg: WT.res('wizard.confirm.close'),
	dockableConfig: {
		title: '{otp.setup.googleauth.tit}',
		width: 450,
		height: 400,
		modal: true
	},
	useTrail: true,
	
	viewModel: {
		data: {
			profileId: null,
			image: null,
			code: null
		}
	},
	
	constructor: function(config) {
		var me = this;
		me.pages = ['step1','step2','step3','step4'];
		me.callParent([config]);
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig();
		
		if(!Ext.isEmpty(ic.profileId)) me.getVM().set('profileId', ic.profileId);
		if(!Ext.isEmpty(ic.address)) me.getVM().set('address', ic.address);
		me.callParent(arguments);
		me.on('beforenavigate', me.onBeforeNavigate);
	},
	
	createPages: function(path) {
		return [{
			itemId: 'step1',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step1.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step1.txt')
			}]
		}, {
			itemId: 'step2',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step2.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step2.txt')
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'sodisplayimagefield',
				bind: '{image}',
				imageUrl: WTF.processBinUrl(WT.ID, 'GetOTPGoogleAuthQRCode'),
				imageWidth: 200,
				imageHeight: 200
			}]
		}, {
			itemId: 'step3',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step3.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step3.txt')
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'textfield',
				bind: '{code}',
				allowBlank: false,
				width: 200,
				fieldLabel: WT.res('otp.setup.googleauth.fld-code.lbl')
			}]
		}, {
			itemId: 'step4',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step4.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: WT.res('otp.setup.googleauth.step4.txt')
			}]
		}];
	},
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if(dir === -1) return;
		var me = this,
				pcmp = me.getPageCmp(pp),
				vm = me.getVM();
		
		if(pp === 'step1') {
			WT.ajaxReq(WT.ID, 'ManageOTP', {
				params: {
					operation: 'configure',
					delivery: 'googleauth',
					profileId: vm.get('profileId')
				},
				callback: function(success, json) {
					if(success) {
						me.onNavigate(np);
						vm.set('image', 1);
					} else WT.error(json.message);
				}
			});
			return false;
			
		} else if(pp === 'step3') {
			WT.ajaxReq(WT.ID, 'ManageOTP', {
				params: {
					operation: 'activate',
					profileId: vm.get('profileId'),
					code: vm.get('code')
				},
				callback: function(success, json) {
					if(success) me.onNavigate(np);
					else WT.error(json.message);
				}
			});
			return false;
		}
	}
});
