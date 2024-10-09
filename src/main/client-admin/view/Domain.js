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
Ext.define('Sonicle.webtop.core.admin.view.Domain', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.form.Spacer',
		'Sonicle.form.field.Password',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.webtop.core.admin.model.Domain',
		'Sonicle.webtop.core.admin.store.DirConnSecurity',
		'Sonicle.webtop.core.admin.store.DirScheme'
	],
	
	dockableConfig: {
		title: '{domain.tit}',
		iconCls: 'wtadm-icon-domain',
		width: 500,
		height: 600
	},
	fieldTitle: 'domainId',
	modelName: 'Sonicle.webtop.core.admin.model.Domain',
	focusField: {'new': 'flddomainid', 'edit': 'flddisplayname'},
	
	passwordPolicy: false,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foIsNew: WTF.foIsEqual('_mode', null, me.MODE_NEW),
			foEnabled: WTF.checkboxBind('record', 'enabled'),
			foUserAutoCreation: WTF.checkboxBind('record', 'userAutoCreation'),
			foDirCaseSensitive: WTF.checkboxBind('record', 'dirCaseSensitive'),
			foActiveDir: WTF.foDefaultIfEmpty('record', 'dirScheme', 'empty'),
			foPwdComplexity: WTF.checkboxBind('record', 'pwdComplexity'),
			foPwdAvoidConsecutiveChars: WTF.checkboxBind('record', 'pwdAvoidConsecutiveChars'),
			foPwdAvoidOldSimilarity: WTF.checkboxBind('record', 'pwdAvoidOldSimilarity'),
			foPwdAvoidUsernameSimilarity: WTF.checkboxBind('record', 'pwdAvoidUsernameSimilarity'),
			foPwdVerifyAtLogin: WTF.checkboxBind('record', 'pwdVerifyAtLogin')
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [{
				xtype: 'wtfieldspanel',
				reference: 'pnlmain',
				autoPadding: 'ts',
				modelValidation: true,
				defaults: {
					labelWidth: 150
				},
				items: [
					{
						xtype: 'textfield',
						reference: 'flddomainid',
						bind: {
							value: '{record.domainId}',
							disabled: '{!foIsNew}'
						},
						disabled: true,
						maskRe: Sonicle.data.validator.Username.maskRe,
						fieldLabel: me.res('domain.fld-domainId.lbl'),
						plugins: [
							'sonoautocomplete',
							{
								ptype: 'sofieldavailabilitycheck',
								baseIconCls: 'wt-opacity-50',
								availableTooltipText: WT.res('sofieldavailabilitycheck.availableTooltipText'),
								unavailableTooltipText: WT.res('sofieldavailabilitycheck.unavailableTooltipText'),
								checkAvailability: function(value, done) {
									if (me.getModel().getModified('domainId') === undefined) return false;
									WT.ajaxReq(me.mys.ID, 'ManageDomain', {
										params: {
											crud: 'check',
											domain: value
										},
										callback: function(success, json) {
											done(success ? json.data : json.message);
										}
									});
								}
							}
						],
						anchor: '100%'
					}, {
						xtype: 'checkbox',
						bind: '{foEnabled}',
						hideEmptyLabel: false,
						boxLabel: me.res('domain.fld-enabled.lbl')
					}, {
						xtype: 'textfield',
						reference: 'flddisplayname',
						bind: '{record.displayName}',
						fieldLabel: me.res('domain.fld-displayName.lbl'),
						anchor: '100%'
					}, {
						xtype: 'textfield',
						bind: '{record.authDomainName}',
						//fieldLabel: me.res('domain.fld-authDomainName.lbl'),
						fieldLabel: me.res('domain.fld-domainName.lbl'),
						emptyText: 'example.com',
						anchor: '100%'
					}/*, {
						xtype: 'textfield',
						bind: {
							value: '{record.domainName}',
							emptyText: '{record.authDomainName}'
						},
						fieldLabel: me.res('domain.fld-domainName.lbl'),
						emptyText: 'example.com',
						anchor: '100%'
					}*/, {
						xtype: 'checkbox',
						bind: '{foUserAutoCreation}',
						hideEmptyLabel: false,
						boxLabel: me.res('domain.fld-userAutoCreation.lbl')
					}, 
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.dirScheme}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirScheme', {
							autoLoad: true
						}),
						fieldLabel: me.res('domain.fld-dirScheme.lbl'),
						anchor: '100%'
					})
				]
			}, {
				xtype: 'container',
				layout: 'card',
				reference: 'pnldir',
				bind: {
					activeItem: '{foActiveDir}'
				},
				flex: 1,
				items: [
					{
					xtype: 'panel',
					itemId: 'empty'
				}, {
					xtype: 'tabpanel',
					itemId: 'webtop',
					modelValidation: true,
					deferredRender: false,
					items: [
						{
							xtype: 'wtfieldspanel',
							title: me.res('domain.server.tit'),
							scrollable: true,
							autoPadding: 'ts',
							modelValidation: true,
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'checkbox',
									bind: '{foDirCaseSensitive}',
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
								}
							]
						}, {
							title: me.res('domain.pwdPolicies.tit'),
							xtype: 'wtfieldspanel',
							scrollable: true,
							autoPadding: 'ts',
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'numberfield',
									bind: '{record.pwdMinLength}',
									minValue: 8,
									maxValue: 128,
									triggers: {
										clear: WTF.clearTrigger()
									},
									plugins: ['sofieldtooltip'],
									fieldLabel: me.res('domain.fld-pwdMinLength.lbl'),
									tooltip: me.res('domain.fld-pwdMinLength.tip'),
									width: 120+100
								}, {
									xtype: 'checkbox',
									bind: '{foPwdComplexity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdComplexity.lbl'),
									tooltip: me.res('domain.fld-pwdComplexity.tip')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidConsecutiveChars}',
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidConsecutiveChars.lbl')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidOldSimilarity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidOldSimilarity.lbl'),
									tooltip: me.res('domain.fld-pwdAvoidOldSimilarity.tip')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidUsernameSimilarity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidUsernameSimilarity.lbl'),
									tooltip: me.res('domain.fld-pwdAvoidUsernameSimilarity.tip')
								}, {
									xtype: 'numberfield',
									bind: '{record.pwdExpiration}',
									minValue: 1,
									maxValue: 365,
									triggers: {
										clear: WTF.clearTrigger()
									},
									plugins: ['sofieldtooltip'],
									emptyText: WT.res('word.none.female'),
									fieldLabel: me.res('domain.fld-pwdExpiration.lbl'),
									tooltip: me.res('domain.fld-pwdExpiration.tip'),
									width: 120+100
								}, {
									xtype: 'checkbox',
									bind: '{foPwdVerifyAtLogin}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdVerifyAtLogin.lbl'),
									tooltip: me.res('domain.fld-pwdVerifyAtLogin.tip')
								}
							]
						}
					]
				}, {
					xtype: 'tabpanel',
					itemId: 'ldapwebtop',
					modelValidation: true,
					deferredRender: false,
					items: [
						{
							title: me.res('domain.server.tit'),
							xtype: 'wtfieldspanel',
							scrollable: true,
							autoPadding: 'ts',
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'fieldcontainer',
									layout: 'hbox',
									items: [
										{
											xtype: 'textfield',
											bind: '{record.dirHost}',
											width: 160
										}, {
											xtype: 'displayfield',
											value: '&nbsp;:&nbsp;'
										}, {
											xtype: 'numberfield',
											bind: '{record.dirPort}',
											hideTrigger: true,
											minValue: 1,
											maxValue: 65000,
											width: 60,
											emptyText: me.res('domain.fld-dirPort.emp')
										}
									],
									fieldLabel: me.res('domain.fld-dirHost.lbl')
								},
								WTF.lookupCombo('id', 'desc', {
									bind: '{record.dirConnSecurity}',
									allowBlank: false,
									store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
										autoLoad: true
									}),
									fieldLabel: me.res('domain.fld-dirConnSecurity.lbl'),
									width: 230
								}),
								{
									xtype: 'textfield',
									bind: '{record.dirAdmin}',
									plugins: 'sonoautocomplete',
									fieldLabel: me.res('domain.fld-dirAdmin.lbl'),
									width: 300
								}, {
									xtype: 'sopasswordfield',
									bind: '{record.dirPassword}',
									plugins: 'sonoautocomplete',
									fieldLabel: me.res('domain.fld-dirPassword.lbl'),
									width: 300
								}, {
									xtype: 'checkbox',
									bind: '{foDirCaseSensitive}',
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
								}
							]
						}, {
							title: me.res('domain.pwdPolicies.tit'),
							xtype: 'wtfieldspanel',
							scrollable: true,
							autoPadding: 'ts',
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'numberfield',
									bind: '{record.pwdMinLength}',
									minValue: 8,
									maxValue: 128,
									triggers: {
										clear: WTF.clearTrigger()
									},
									plugins: ['sofieldtooltip'],
									fieldLabel: me.res('domain.fld-pwdMinLength.lbl'),
									tooltip: me.res('domain.fld-pwdMinLength.tip'),
									width: 120+100
								}, {
									xtype: 'checkbox',
									bind: '{foPwdComplexity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdComplexity.lbl'),
									tooltip: me.res('domain.fld-pwdComplexity.tip')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidConsecutiveChars}',
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidConsecutiveChars.lbl')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidOldSimilarity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidOldSimilarity.lbl'),
									tooltip: me.res('domain.fld-pwdAvoidOldSimilarity.tip')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidUsernameSimilarity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidUsernameSimilarity.lbl'),
									tooltip: me.res('domain.fld-pwdAvoidUsernameSimilarity.tip')
								}, {
									xtype: 'numberfield',
									bind: '{record.pwdExpiration}',
									minValue: 1,
									maxValue: 365,
									triggers: {
										clear: WTF.clearTrigger()
									},
									plugins: ['sofieldtooltip'],
									emptyText: WT.res('word.none.female'),
									fieldLabel: me.res('domain.fld-pwdExpiration.lbl'),
									tooltip: me.res('domain.fld-pwdExpiration.tip'),
									width: 120+100
								}, {
									xtype: 'checkbox',
									bind: '{foPwdVerifyAtLogin}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdVerifyAtLogin.lbl'),
									tooltip: me.res('domain.fld-pwdVerifyAtLogin.tip')
								}
							]
						}
					]
				}, {
					xtype: 'tabpanel',
					itemId: 'ldap',
					modelValidation: true,
					deferredRender: false,
					items: [
						{
							title: me.res('domain.server.tit'),
							xtype: 'wtfieldspanel',
							scrollable: true,
							autoPadding: 'ts',
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'fieldcontainer',
									layout: 'hbox',
									items: [
										{
											xtype: 'textfield',
											bind: '{record.dirHost}',
											width: 160
										}, {
											xtype: 'displayfield',
											value: '&nbsp;:&nbsp;'
										}, {
											xtype: 'numberfield',
											bind: '{record.dirPort}',
											hideTrigger: true,
											minValue: 1,
											maxValue: 65000,
											width: 60,
											emptyText: me.res('domain.fld-dirPort.emp')
										}
									],
									fieldLabel: me.res('domain.fld-dirHost.lbl')
								},
								WTF.lookupCombo('id', 'desc', {
									bind: '{record.dirConnSecurity}',
									allowBlank: false,
									store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
										autoLoad: true
									}),
									fieldLabel: me.res('domain.fld-dirConnSecurity.lbl'),
									width: 230
								}),
								{
									xtype: 'textfield',
									bind: '{record.dirAdmin}',
									plugins: 'sonoautocomplete',
									fieldLabel: me.res('domain.ldap.fld-dirAdmin.lbl'),
									anchor: '100%'
								}, {
									xtype: 'sopasswordfield',
									bind: '{record.dirPassword}',
									plugins: 'sonoautocomplete',
									fieldLabel: me.res('domain.fld-dirPassword.lbl'),
									width: 300
								}, {
									xtype: 'checkbox',
									bind: '{foDirCaseSensitive}',
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
								}
							]
						}, {
							title: me.res('domain.ldap.login.tit'),
							xtype: 'wtfieldspanel',
							scrollable: true,
							autoPadding: 'ts',
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'textfield',
									bind: '{record.ldapLoginDn}',
									fieldLabel: me.res('domain.ldap.fld-ldapLoginDn.lbl'),
									anchor: '100%'
								}, {
									xtype: 'textfield',
									bind: '{record.ldapLoginFilter}',
									fieldLabel: me.res('domain.ldap.fld-ldapLoginFilter.lbl'),
									anchor: '100%'
								}
							]
						}, {
							title: me.res('domain.ldap.users.tit'),
							xtype: 'wtfieldspanel',
							scrollable: true,
							autoPadding: 'ts',
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'textfield',
									bind: '{record.ldapUserDn}',
									fieldLabel: me.res('domain.ldap.fld-ldapUserDn.lbl'),
									anchor: '100%'
								}, {
									xtype: 'textfield',
									bind: '{record.ldapUserFilter}',
									fieldLabel: me.res('domain.ldap.fld-ldapUserFilter.lbl'),
									anchor: '100%'
								}, {
									xtype: 'textfield',
									bind: '{record.ldapUserIdField}',
									fieldLabel: me.res('domain.ldap.fld-ldapUserIdField.lbl'),
									emptyText: me.res('domain.ldap.fld-ldapUserIdField.emp'),
									width: 330
								}, {
									xtype: 'textfield',
									bind: '{record.ldapUserFirstnameField}',
									fieldLabel: me.res('domain.ldap.fld-ldapUserFirstnameField.lbl'),
									emptyText: me.res('domain.ldap.fld-ldapUserFirstnameField.emp'),
									width: 330
								}, {
									xtype: 'textfield',
									bind: '{record.ldapUserLastnameField}',
									fieldLabel: me.res('domain.ldap.fld-ldapUserLastnameField.lbl'),
									emptyText: me.res('domain.ldap.fld-ldapUserLastnameField.emp'),
									width: 330
								}, {
									xtype: 'textfield',
									bind: '{record.ldapUserDisplayNameField}',
									fieldLabel: me.res('domain.ldap.fld-ldapUserDisplayNameField.lbl'),
									emptyText: me.res('domain.ldap.fld-ldapUserDisplayNameField.emp'),
									width: 330
								}
							]
						}, {
							title: me.res('domain.pwdPolicies.tit'),
							xtype: 'wtfieldspanel',
							scrollable: true,
							autoPadding: 'ts',
							defaults: {
								labelWidth: 120
							},
							items: [
								{
									xtype: 'numberfield',
									bind: '{record.pwdMinLength}',
									minValue: 8,
									maxValue: 128,
									triggers: {
										clear: WTF.clearTrigger()
									},
									plugins: ['sofieldtooltip'],
									fieldLabel: me.res('domain.fld-pwdMinLength.lbl'),
									tooltip: me.res('domain.fld-pwdMinLength.tip'),
									width: 120+100
								}, {
									xtype: 'checkbox',
									bind: '{foPwdComplexity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdComplexity.lbl'),
									tooltip: me.res('domain.fld-pwdComplexity.tip')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidConsecutiveChars}',
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidConsecutiveChars.lbl')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidOldSimilarity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidOldSimilarity.lbl'),
									tooltip: me.res('domain.fld-pwdAvoidOldSimilarity.tip')
								}, {
									xtype: 'checkbox',
									bind: '{foPwdAvoidUsernameSimilarity}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdAvoidUsernameSimilarity.lbl'),
									tooltip: me.res('domain.fld-pwdAvoidUsernameSimilarity.tip')
								}, {
									xtype: 'numberfield',
									bind: '{record.pwdExpiration}',
									minValue: 1,
									maxValue: 365,
									triggers: {
										clear: WTF.clearTrigger()
									},
									plugins: ['sofieldtooltip'],
									emptyText: WT.res('word.none.female'),
									fieldLabel: me.res('domain.fld-pwdExpiration.lbl'),
									tooltip: me.res('domain.fld-pwdExpiration.tip'),
									width: 120+100
								}, {
									xtype: 'checkbox',
									bind: '{foPwdVerifyAtLogin}',
									plugins: ['sofieldtooltip'],
									hideEmptyLabel: false,
									boxLabel: me.res('domain.fld-pwdVerifyAtLogin.lbl'),
									tooltip: me.res('domain.fld-pwdVerifyAtLogin.tip')
								}
							]
						}
					]
				}, {
					xtype: 'tabpanel',
					itemId: 'ldapneth',
					modelValidation: true,
					deferredRender: false,
					items: [{
						title: me.res('domain.server.tit'),
						xtype: 'wtfieldspanel',
						scrollable: true,
						autoPadding: 'ts',
						defaults: {
							labelWidth: 120
						},
						items: [{
								xtype: 'fieldcontainer',
								layout: 'hbox',
								items: [{
									xtype: 'textfield',
									bind: '{record.dirHost}',
									width: 160
								}, {
									xtype: 'displayfield',
									value: '&nbsp;:&nbsp;'
								}, {
									xtype: 'numberfield',
									bind: '{record.dirPort}',
									hideTrigger: true,
									minValue: 1,
									maxValue: 65000,
									width: 60,
									emptyText: me.res('domain.fld-dirPort.emp')
								}],
								fieldLabel: me.res('domain.fld-dirHost.lbl')
							},
							WTF.lookupCombo('id', 'desc', {
								bind: '{record.dirConnSecurity}',
								allowBlank: false,
								store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
									autoLoad: true
								}),
								fieldLabel: me.res('domain.fld-dirConnSecurity.lbl'),
								width: 230
							}),
							{
								xtype: 'textfield',
								bind: '{record.dirAdmin}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.res('domain.ldap.fld-dirAdmin.lbl'),
								anchor: '100%'
							}, {
								xtype: 'sopasswordfield',
								bind: '{record.dirPassword}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.res('domain.fld-dirPassword.lbl'),
								width: 300
							}, {
								xtype: 'checkbox',
								bind: '{foDirCaseSensitive}',
								hideEmptyLabel: false,
								boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
							}
						]
					}, {
						title: me.res('domain.ldap.login.tit'),
						xtype: 'wtfieldspanel',
						scrollable: true,
						autoPadding: 'ts',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapLoginDn}',
							fieldLabel: me.res('domain.ldap.fld-ldapLoginDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapLoginFilter}',
							fieldLabel: me.res('domain.ldap.fld-ldapLoginFilter.lbl'),
							anchor: '100%'
						}]
					}, {
						title: me.res('domain.ldap.users.tit'),
						xtype: 'wtfieldspanel',
						scrollable: true,
						autoPadding: 'ts',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapUserDn}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFilter}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserFilter.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserIdField}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserIdField.lbl'),
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFirstnameField}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserFirstnameField.lbl'),
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserLastnameField}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserLastnameField.lbl'),
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserDisplayNameField}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserDisplayNameField.lbl'),
							width: 330
						}]
					}]
				}, {
					xtype: 'tabpanel',
					itemId: 'ad',
					modelValidation: true,
					deferredRender: false,
					defaults: {
						labelWidth: 120
					},
					items: [{
						title: me.res('domain.server.tit'),
						xtype: 'wtfieldspanel',
						scrollable: true,
						autoPadding: 'ts',
						defaults: {
							labelWidth: 120
						},
						items: [{
								xtype: 'fieldcontainer',
								layout: 'hbox',
								items: [{
									xtype: 'textfield',
									bind: '{record.dirHost}',
									width: 160
								}, {
									xtype: 'displayfield',
									value: '&nbsp;:&nbsp;'
								}, {
									xtype: 'numberfield',
									bind: '{record.dirPort}',
									hideTrigger: true,
									minValue: 1,
									maxValue: 65000,
									width: 60,
									emptyText: me.res('domain.fld-dirPort.emp')
								}],
								fieldLabel: me.res('domain.fld-dirHost.lbl')
							},
							WTF.lookupCombo('id', 'desc', {
								bind: '{record.dirConnSecurity}',
								allowBlank: false,
								store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
									autoLoad: true
								}),
								fieldLabel: me.res('domain.fld-dirConnSecurity.lbl'),
								width: 230
							}),
							{
								xtype: 'textfield',
								bind: '{record.dirAdmin}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.res('domain.ldap.fld-dirAdmin.lbl'),
								anchor: '100%'
							}, {
								xtype: 'sopasswordfield',
								bind: '{record.dirPassword}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.res('domain.fld-dirPassword.lbl'),
								width: 300
							}, {
								xtype: 'checkbox',
								bind: '{foDirCaseSensitive}',
								hideEmptyLabel: false,
								boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
							}
						]
					}, {
						title: me.res('domain.ldap.login.tit'),
						xtype: 'wtfieldspanel',
						scrollable: true,
						autoPadding: 'ts',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapLoginDn}',
							fieldLabel: me.res('domain.ldap.fld-ldapLoginDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapLoginFilter}',
							fieldLabel: me.res('domain.ldap.fld-ldapLoginFilter.lbl'),
							emptyText: '&(objectCategory=person)(objectClass=user)(!(userAccountControl:1.2.840.113556.1.4.803:=2))',
							anchor: '100%'
						}]
					}, {
						title: me.res('domain.ldap.users.tit'),
						xtype: 'wtfieldspanel',
						scrollable: true,
						autoPadding: 'ts',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapUserDn}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFilter}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserFilter.lbl'),
							emptyText: '&(objectClass=person)(objectClass=user)',
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFirstnameField}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserFirstnameField.lbl'),
							emptyText: 'givenName',
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserLastnameField}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserLastnameField.lbl'),
							emptyText: 'sn',
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserDisplayNameField}',
							fieldLabel: me.res('domain.ldap.fld-ldapUserDisplayNameField.lbl'),
							emptyText: 'cn',
							width: 330
						}]
					}]
				}, {
					xtype: 'wtform',
					itemId: 'imap',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
						xtype: 'fieldcontainer',
						layout: 'hbox',
						items: [{
							xtype: 'textfield',
							bind: '{record.dirHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.dirPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.res('domain.fld-dirPort.emp')
						}],
						fieldLabel: me.res('domain.fld-dirHost.lbl')
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.dirConnSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.res('domain.fld-dirConnSecurity.lbl'),
						width: 200
					}),
					{
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
					}]
				}, {
					xtype: 'wtform',
					itemId: 'smb',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
						xtype: 'fieldcontainer',
						layout: 'hbox',
						items: [{
							xtype: 'textfield',
							bind: '{record.dirHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.dirPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.res('domain.fld-dirPort.emp')
						}],
						fieldLabel: me.res('domain.fld-dirHost.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
					}]
				}, {
					xtype: 'wtform',
					itemId: 'sftp',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
						xtype: 'fieldcontainer',
						layout: 'hbox',
						items: [{
							xtype: 'textfield',
							bind: '{record.dirHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.dirPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.res('domain.fld-dirPort.emp')
						}],
						fieldLabel: me.res('domain.fld-dirHost.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.res('domain.fld-dirCaseSensitive.lbl')
					}]
				}]
			}]
		});
		me.on('viewinvalid', me.onViewInvalid);
	},
	
	onViewInvalid: function(s, mo, errs) {
		var me = this;
		WTU.updateFieldsErrors(me.lref('pnlmain'), errs);
		WTU.updateFieldsErrors(me.lref('pnldir').getLayout().getActiveItem(), errs);
	}
});
