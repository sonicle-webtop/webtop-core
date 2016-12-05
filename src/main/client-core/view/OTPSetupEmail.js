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
Ext.define('Sonicle.webtop.core.view.OTPSetupEmail', {
	extend: 'WTA.sdk.WizardView',
	requires: [
		'WTA.ux.panel.Form'
	],
	
	dockableConfig: {
		title: '{otp.setup.email.tit}',
		width: 450,
		height: 250,
		modal: true
	},
	useTrail: true,
	
	viewModel: {
		data: {
			profileId: null,
			address: null,
			code: null
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
				ic = me.getInitialConfig();
		
		if(!Ext.isEmpty(ic.profileId)) me.getVM().set('profileId', ic.profileId);
		if(!Ext.isEmpty(ic.address)) me.getVM().set('address', ic.address);
		me.callParent(arguments);
		me.on('beforenavigate', me.onBeforeNavigate);
	},
	
	initPages: function() {
		return ['step1','step2','end'];
	},
	
	createPages: function(path) {
		return [{
			itemId: 'step1',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('otp.setup.email.step1.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: WT.res('otp.setup.email.step1.txt')
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				items: [{
					xtype: 'textfield',
					bind: '{address}',
					allowBlank: false,
					width: 350,
					fieldLabel: WT.res('otp.setup.email.fld-address.lbl')
				}]
			}]
		}, {
			itemId: 'step2',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('otp.setup.email.step2.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: WT.res('otp.setup.email.step2.txt')
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'wtform',
				defaults: {
					labelWidth: 120
				},
				items: [{
					xtype: 'textfield',
					bind: '{code}',
					allowBlank: false,
					width: 250,
					fieldLabel: WT.res('otp.setup.email.fld-code.lbl')
				}]
			}]
		}, {
			itemId: 'end',
			xtype: 'wtwizardpage',
			items: [{
				xtype: 'label',
				html: WT.res('otp.setup.email.step3.tit'),
				cls: 'x-window-header-title-default'
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'label',
				html: WT.res('otp.setup.email.step3.txt')
			}]
		}];
	},
	
	onBeforeNavigate: function(s, dir, np, pp) {
		if(dir === -1) return;
		var me = this,
				ret = true,
				ppcmp = me.getPageCmp(pp),
				vm = me.getVM();
		
		if(pp === 'step1') {
			ret = ppcmp.down('wtform').isValid();
			if(!ret) return false;
			
			WT.ajaxReq(WT.ID, 'ManageOTP', {
				params: {
					operation: 'configure',
					delivery: 'email',
					profileId: vm.get('profileId'),
					address: vm.get('address')
				},
				callback: function(success, json) {
					if(success) me.onNavigate(np);
					else WT.error(json.message);
				}
			});
			return false;
			
		} else if(pp === 'step2') {
			ret = ppcmp.down('wtform').isValid();
			if(!ret) return false;
			
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
