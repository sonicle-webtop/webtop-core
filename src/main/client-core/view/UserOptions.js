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
Ext.define('Sonicle.webtop.core.view.UserOptions', {
	alternateClassName: 'WTA.view.UserOptions',
	extend: 'WTA.sdk.UserOptionsView',
	requires: [
		'Sonicle.String',
		'Sonicle.panel.Markdown',
		'WTA.model.Simple',
		'WTA.store.HeaderScale',
		'WTA.store.DesktopNotification',
		'WTA.store.OTPDelivery',
		'WTA.store.Timezone',
		'WTA.store.StartDay',
		'WTA.store.DateFmtShort',
		'WTA.store.DateFmtLong',
		'WTA.store.TimeFmtShort',
		'WTA.store.TimeFmtLong'
	],
	uses: [
		'Sonicle.webtop.core.view.ChangePassword'
	],
	
	//overridable properties to influence UI
	mainPasswordButtonPack: 'center',
	upiNicknameHidden: false,
	upiGenderHidden: false,
	upiFaxHidden: false,
	upiPagerHidden: false,
	
	viewModel: {
		formulas: {
			isOTPActive: WTF.foIsEmpty('record', 'otpDelivery', true),
			syncAlertEnabled: WTF.checkboxBind('record', 'syncAlertEnabled'),
			imSoundOnFriendConnect: WTF.checkboxBind('record', 'imSoundOnFriendConnect'),
			imSoundOnFriendDisconnect: WTF.checkboxBind('record', 'imSoundOnFriendDisconnect'),
			imSoundOnMessageReceived: WTF.checkboxBind('record', 'imSoundOnMessageReceived'),
			imSoundOnMessageSent: WTF.checkboxBind('record', 'imSoundOnMessageSent')
		}
	},
	
	initComponent: function() {
		var me = this, 
				NtfMgr = Sonicle.DesktopNotificationMgr,
				vm;
		me.callParent(arguments);
		
		vm = me.getViewModel();
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, {
			foIsMyProfile: function() {
				return me.isProfileSelf();
			},
			foCanManagePassword: {
				bind: {
					permPasswordManage: '{record.permPasswordManage}',
					dirCapPasswordWrite: '{record.dirCapPasswordWrite}'
				},
				get: function(get) {
					if (me.isAdminOnBehalf()) return false;
					if (WT.isAdmin()) return true;
					return get['dirCapPasswordWrite'] && get['permPasswordManage'];
				}
			},
			foCanManageUpi: WTF.foGetFn('record', 'permUpiManage', function(v) {
				if (WT.isAdmin() || me.isAdminOnBehalf()) return true;
				return v;
			})
		}));
		
		me.add({
			xtype: 'wtopttabsection',
			title: WT.res('opts.main.tit'),
			items: [{
				xtype: 'textfield',
				bind: '{record.id}',
				disabled: true,
				fieldLabel: WT.res('opts.main.fld-profile.lbl'),
				width: 380
			}, {
				xtype: 'textfield',
				bind: '{record.displayName}',
				fieldLabel: WT.res('opts.main.fld-displayName.lbl'),
				width: 380,
				needLogin: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'sospacer'
			}, {
				xtype: 'formseparator'
			},
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.layout}',
				store: {
					autoLoad: true,
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(me.ID, 'LookupLayouts', 'layouts')
				},
				fieldLabel: WT.res('opts.main.fld-layout.lbl'),
				width: 380,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			}),
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.ui}',
				store: {
					autoLoad: true,
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(me.ID, 'LookupUIPresets', 'data')
				},
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				fieldLabel: WT.res('opts.main.fld-ui.lbl'),
				emptyText: WT.res('opts.main.fld-ui.emp'),
				width: 380,
				needReload: true
			}),
			/*
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.theme}',
				store: {
					autoLoad: true,
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(me.ID, 'LookupThemes', 'themes')
				},
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				fieldLabel: WT.res('opts.main.fld-theme.lbl'),
				width: 140+150,
				needReload: true
			}), 
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.laf}',
				store: {
					autoLoad: true,
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(me.ID, 'LookupLAFs', 'lafs')
				},
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				fieldLabel: WT.res('opts.main.fld-laf.lbl'),
				width: 140+150,
				needReload: true
			}),
			*/
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.headerScale}',
				store: Ext.create('WTA.store.HeaderScale', {
					autoLoad: true
				}),
				fieldLabel: WT.res('opts.main.fld-headerScale.lbl'),
				width: 380,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			}),
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.startupService}',
				store: {
					autoLoad: true,
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(me.ID, 'LookupStartupServices', null, {
						extraParams: {
							optionsProfile: me.profileId
						}
					})
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: WT.res('opts.main.fld-startupService.lbl'),
				emptyText: WT.res('opts.main.fld-startupService.emp'),
				width: 380,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}), {
				xtype: 'sospacer'
			}, {
				xtype: 'formseparator'
			}, {
				xtype: 'fieldcontainer',
				fieldLabel: WT.res('opts.main.fld-desktopNotification.lbl'),
				layout: 'hbox',
				defaults: {
					margin: '0 10 20 0'
				},
				items: [
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.desktopNotification}',
						store: Ext.create('WTA.store.DesktopNotification', {
							autoLoad: true
						}),
						disabled: !NtfMgr.isSupported || (NtfMgr.permissionLevel() === NtfMgr.PERM_DENIED),
						width: 195,
						listeners: {
							select: function(s,rec) {
								if(rec.get('id') === 'always' || rec.get('id') === 'auto') {
									NtfMgr.ensureAuthorization();
								}
							},
							blur: {
								fn: me.onBlurAutoSave,
								scope: me
							}
						}
					}), {
						xtype: 'button',
						ui: '{tertiary|default}',
						text: WT.res('opts.main.btn-desktopNotificationCheck.lbl'),
						tooltip: WT.res('opts.main.btn-desktopNotificationCheck.tip'),
						//iconCls: 'wt-icon-browser-checkPermission',
						handler: function() {
							var plevel = NtfMgr.permissionLevel();
							if (plevel === NtfMgr.PERM_DENIED) {
								WT.warn(WT.res('info.browser.permission.notification.denied'));
							} else if (plevel === NtfMgr.PERM_GRANTED) {
								WT.info(WT.res('info.browser.permission.notification.granted'));
							} else {
								NtfMgr.ensureAuthorization();
							}
						}
					}
				]
			}, /*{
				xtype: 'sospacer'
			}, {
				xtype: 'sospacer'
			}, */{
				xtype: 'container',
				layout: {
					type: 'hbox',
					pack: me.mainPasswordButtonPack,
					align: 'middle'
				},
				items: [{
					xtype: 'button',
					ui: '{secondary|default}',
					bind: {
						disabled: '{!foCanManagePassword}'
					},
					disabled: true,
					hidden: !me.isProfileSelf(),
					text: WT.res('opts.main.btn-changePassword.lbl'),
					width: 250,
					handler: function() {
						me.changePasswordUI();
					}
				}]
			}]
		}, {
			xtype: 'wtopttabsection',
			title: WT.res('opts.i18n.tit'),
			items: [WTF.localCombo('id', 'desc', {
				bind: '{record.language}',
				store: {
					autoLoad: true,
					model: 'WTA.model.Simple',
					proxy: WTF.proxy(me.ID, 'LookupLanguages', 'languages')
				},
				fieldLabel: WT.res('opts.i18n.fld-language.lbl'),
				width: 400,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needLogin: true
			}), 
			WTF.localCombo('id', 'desc', {
				bind: '{record.timezone}',
				store: Ext.create('WTA.store.Timezone', {
					autoLoad: true
				}),
				fieldLabel: WT.res('opts.i18n.fld-timezone.lbl'),
				width: 400,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needLogin: true
			}), {
				xtype: 'sospacer'
			}, {
				xtype: 'formseparator'
			}, 
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.startDay}',
				store: Ext.create('WTA.store.StartDay', {
					autoLoad: true
				}),
				fieldLabel: WT.res('opts.i18n.fld-startDay.lbl'),
				width: 400,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			}),
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.shortDateFormat}',
				store: Ext.create('WTA.store.DateFmtShort', {
					autoLoad: true
				}),
				fieldLabel: WT.res('opts.i18n.fld-shortDateFormat.lbl'),
				width: 400,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			}),
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.longDateFormat}',
				store: Ext.create('WTA.store.DateFmtLong', {
					autoLoad: true
				}),
				fieldLabel: WT.res('opts.i18n.fld-longDateFormat.lbl'),
				width: 400,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			}),
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.shortTimeFormat}',
				store: Ext.create('WTA.store.TimeFmtShort', {
					autoLoad: true
				}),
				fieldLabel: WT.res('opts.i18n.fld-shortTimeFormat.lbl'),
				width: 400,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			}),
			WTF.lookupCombo('id', 'desc', {
				bind: '{record.longTimeFormat}',
				store: Ext.create('WTA.store.TimeFmtLong', {
					autoLoad: true
				}),
				fieldLabel: WT.res('opts.i18n.fld-longTimeFormat.lbl'),
				width: 400,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				},
				needReload: true
			})]
		}, {
			xtype: 'wtopttabsection',
			title: WT.res('opts.upi.tit'),
			bind: {
				permStatus: '{record.permUpiManage}'
			},
			plugins: [{
				ptype: 'wttabpermstatus',
				enabled: !me.isProfileSysAdmin(),
				isAdmin: WT.isAdmin() || me.isAdminOnBehalf(),
				info: 'USER_PROFILE_INFO:MANAGE'
			}],
			items: [{
				xtype: 'textfield',
				bind: {
					value: '{record.upiTitle}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-title.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiFirstName}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-firstName.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiLastName}',
					disabled: '{!foCanManageUpi}'
				},
				width: 400,
				fieldLabel: WT.res('opts.upi.fld-lastName.lbl'),
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiNickname}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-nickname.lbl'),
				width: 400,
				needReload: true,
				hidden: me.upiNicknameHidden,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, 
			WTF.localCombo('id', 'desc', {
				bind: {
					value: '{record.upiGender}',
					disabled: '{!foCanManageUpi}'
				},
				hidden: me.upiGenderHidden,
				autoLoadOnValue: true,
				store: Ext.create('Sonicle.webtop.core.store.Gender'),
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: WT.res('opts.upi.fld-gender.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}), {
				xtype: 'textfield',
				bind: {
					value: '{record.upiEmail}',
					disabled: '{!foCanManageUpi}'
				},
				width: 400,
				fieldLabel: WT.res('opts.upi.fld-email.lbl'),
				needLogin: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiMobile}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-mobile.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiTelephone}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-telephone.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiFax}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-fax.lbl'),
				width: 400,
				needReload: true,
				hidden: me.upiFaxHidden,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiPager}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-pager.lbl'),
				width: 400,
				needReload: true,
				hidden: me.upiPagerHidden,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiAddress}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-address.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiCity}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-city.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiPostalCode}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-postalCode.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiState}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-state.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiCountry}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-country.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiCompany}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-company.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiFunction}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-function.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiCustom1}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-custom1.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiCustom2}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-custom2.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'textfield',
				bind: {
					value: '{record.upiCustom3}',
					disabled: '{!foCanManageUpi}'
				},
				fieldLabel: WT.res('opts.upi.fld-custom3.lbl'),
				width: 400,
				needReload: true,
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}]
		}, {
			xtype: 'wtopttabsection',
			title: WT.res('opts.otp.tit'),
			disabled: !WT.getVar('wtOtpEnabled'),
			items: [{
				xtype: 'container',
				layout: 'form',
				items: [{
					xtype : 'fieldcontainer',
					layout: 'hbox',
					fieldLabel: WT.res('opts.otp.fld-delivery.lbl'),
					items: [
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.otpDelivery}',
						store: Ext.create('WTA.store.OTPDelivery', {
							autoLoad: true
						}),
						readOnly: true,
						emptyText: WT.res('word.none.female'),
						width: 250
					}), {
						xtype: 'sospacer',
						vertical: false
					}, {
						xtype: 'button',
						ui: '{secondary|toolbar}',
						bind: {
							hidden: '{record.otpEnabled}'
						},
						text: WT.res('act-activate.lbl'),
						menu: [{
							itemId: 'email',
							text: WT.res('store.otpdelivery.email'),
							handler: function() {
								me.activateOTP('email');
							},
							scope: me
						}, {
							itemId: 'googleauth',
							text: WT.res('store.otpdelivery.googleauth'),
							handler: function() {
								me.activateOTP('googleauth');
							},
							scope: me
						}]
					}, {
						xtype: 'button',
						ui: '{secondary|toolbar}',
						bind: {
							hidden: '{!record.otpEnabled}'
						},
						text: WT.res('act-deactivate.lbl'),
						handler: function() {
							me.deactivateOTP();
						},
						scope: me
					}]
				}]
			}, {
				xtype: 'container',
				reference: 'delivery',
				layout: 'card',
				items: [{
					xtype: 'container',
					itemId: 'none'
				}, {
					xtype: 'container',
					itemId: 'googleauth',
					items: [{
						xtype: 'component',
						padding: '0 5 0 5',
						html: WT.res('opts.otp.googleauth.html')
					}]
				}, {
					xtype: 'container',
					itemId: 'email',
					items: [{
						xtype: 'component',
						padding: '0 5 0 5',
						html: WT.res('opts.otp.email.html')
					}, {
						xtype: 'container',
						layout: 'form',
						items: [{
							xtype: 'displayfield',
							bind: '{record.otpEmailAddress}',
							fieldLabel: WT.res('opts.otp.email.fld-emailaddress.lbl')
						}]
					}]
				}]
			}, {
				xtype: 'container',
				reference: 'thisdevice',
				layout: 'card',
				items: [{
					xtype: 'container',
					itemId: 'trusted',
					layout: 'form',
					items: [{
						xtype: 'fieldset',
						layout: 'form',
						items: [{
							xtype: 'component',
							html: WT.res('opts.otp.thisdevice.trusted.html')
						}, {
							xtype: 'sospacer'
						}, {
							xtype: 'button',
							ui: '{secondary|toolbar}',
							bind: {
								disabled: '{!foIsMyProfile}'
							},
							text: WT.res('opts.otp.btn-untrustthis.lbl'),
							handler: function() {
								me.untrustThisOTP();
							}
						}]
					}]
				}, {
					xtype: 'container',
					itemId: 'nottrusted',
					layout: 'form',
					items: [{
						xtype: 'fieldset',
						title: WT.res('opts.otp.thisdevice.nottrusted.tit'),
						items: [{
							xtype: 'component',
							html: WT.res('opts.otp.thisdevice.nottrusted.html')
						}]
					}]
				}]
			}, {
				xtype: 'container',
				layout: 'form',
				items: [{
					xtype: 'fieldset',
					title: WT.res('opts.otp.otherdevices.tit'),
					items: [{
						xtype: 'component',
						html: WT.res('opts.otp.otherdevices.html')
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'button',
						ui: '{secondary|toolbar}',
						bind: {
							disabled: '{!foIsMyProfile}'
						},
						text: WT.res('opts.otp.btn-untrustother.lbl'),
						handler: function() {
							me.untrustOtherOTP();
						}
					}]
				}]
			}]
		}, {
			xtype: 'wtopttabsection',
			title: WT.res('opts.sync.tit'),
			hidden: me.isProfileSysAdmin(),
			bind: {
				permStatus: '{record.permSyncDevicesAccess}'
			},
			plugins: [{
				ptype: 'wttabpermstatus',
				//enabled: !WT.isSysAdmin() || me.isAdminOnBehalf(),
				enabled: !me.isProfileSysAdmin(),
				isAdmin: WT.isAdmin() || me.isAdminOnBehalf(),
				info: 'DEVICES_SYNC:ACCESS'
			}],
			layout: 'vbox',
			items: [
				{
					xtype: 'soformseparator',
					title:  WT.res('opts.sync.eas.tit')
					//xtype: 'sotext',
					//text:  WT.res('opts.sync.eas.tit'),
					//cls: 'wt-form-body-title'
				}, {
					xtype: 'wtfieldspanel',
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								{
									xtype: 'checkbox',
									reference: 'fldsyncalertenabled', // Publishes field into viewmodel...
									bind: '{syncAlertEnabled}',
									boxLabel: WT.res('opts.sync.fld-syncAlertEnabled.lbl'),
									hideEmptyLabel: true,
									listeners: {
										change: {
											fn: function(s) {
												//TODO: workaround...il modello veniva salvato prima dell'aggionamento
												Ext.defer(function() {
													me.onBlurAutoSave(s);
												}, 200);
											},
											scope: me
										}
									}
								}, {
									xtype: 'sohspacer'
								}, {
									xtype: 'numberfield',
									bind: {
										value: '{record.syncAlertTolerance}',
										disabled: '{!fldsyncalertenabled.checked}'
									},
									minValue: 1,
									maxValue: 30,
									fieldLabel: WT.res('opts.sync.fld-syncAlertTolerance.lbl'),
									labelWidth: 120,
									labelAlign: 'right',
									width: 180,
									listeners: {
										blur: {
											fn: me.onBlurAutoSave,
											scope: me
										}
									}
								}
							]
						}
					]
				},
				me.createSyncGridCfg({
					reference: 'gpsync',
					flex: 1,
					width: '100%'
				})
			],
			listeners: {
				activate: {
					fn: function() {
						me.refreshSyncDevices();
					},
					single: true
				}
			}
		}, {
			xtype: 'wtopttabsection',
			title: WT.res('opts.im.tit'),
			hidden: me.isProfileSysAdmin(),
			bind: {
				permStatus: '{record.permWebchatAccess}'
			},
			plugins: [{
				ptype: 'wttabpermstatus',
				enabled: !me.isProfileSysAdmin(),
				isAdmin: WT.isAdmin() || me.isAdminOnBehalf(),
				info: 'WEBCHAT:ACCESS'
			}],
			items: [{
				xtype: 'sobytesfield',
				bind: '{record.imUploadMaxFileSize}',
				disabled: !(WT.isAdmin() || me.isAdminOnBehalf()),
				fieldLabel: WT.res(me.ID, 'opts.im.fld-imUploadMaxFileSize.lbl'),
				width: 280,
				permStatus: false,
				plugins: [{
					ptype: 'wtadminfieldpermstatus',
					isAdmin: WT.isAdmin() || me.isAdminOnBehalf()
				}],
				listeners: {
					blur: {
						fn: me.onBlurAutoSave,
						scope: me
					}
				}
			}, {
				xtype: 'soformseparator',
				title: WT.res('opts.im.sound.tit')
			}, {
				xtype: 'checkbox',
				bind: '{imSoundOnFriendConnect}',
				hideEmptyLabel: false,
				boxLabel: WT.res(me.ID, 'opts.im.fld-soundOnFriendConnect.lbl'),
				labelWidth: 20,
				listeners: {
					change: {
						fn: function(s) {
							//TODO: workaround...il modello veniva salvato prima dell'aggionamento
							Ext.defer(function() {
								me.onBlurAutoSave(s);
							}, 200);
						},
						scope: me
					}
				}
			}, {
				xtype: 'checkbox',
				bind: '{imSoundOnFriendDisconnect}',
				hideEmptyLabel: false,
				boxLabel: WT.res(me.ID, 'opts.im.fld-soundOnFriendDisconnect.lbl'),
				labelWidth: 20,
				listeners: {
					change: {
						fn: function(s) {
							//TODO: workaround...il modello veniva salvato prima dell'aggionamento
							Ext.defer(function() {
								me.onBlurAutoSave(s);
							}, 200);
						},
						scope: me
					}
				}
			}, {
				xtype: 'checkbox',
				bind: '{imSoundOnMessageReceived}',
				hideEmptyLabel: false,
				boxLabel: WT.res(me.ID, 'opts.im.fld-soundOnMessageReceived.lbl'),
				labelWidth: 20,
				listeners: {
					change: {
						fn: function(s) {
							//TODO: workaround...il modello veniva salvato prima dell'aggionamento
							Ext.defer(function() {
								me.onBlurAutoSave(s);
							}, 200);
						},
						scope: me
					}
				}
			}, {
				xtype: 'checkbox',
				bind: '{imSoundOnMessageSent}',
				hideEmptyLabel: false,
				boxLabel: WT.res(me.ID, 'opts.im.fld-soundOnMessageSent.lbl'),
				labelWidth: 20,
				listeners: {
					change: {
						fn: function(s) {
							//TODO: workaround...il modello veniva salvato prima dell'aggionamento
							Ext.defer(function() {
								me.onBlurAutoSave(s);
							}, 200);
						},
						scope: me
					}
				}
			}]
		}, {
			xtype: 'wtopttabsection',
			title: WT.res('opts.pbx.tit'),
			hidden: me.isProfileSysAdmin(),
			items: [
				{
					xtype: 'soformseparator',
					title: WT.res('opts.pbx.service.tit')
				}, {
					xtype: 'label',
					text: WT.getVar("pbxConfigured")?WT.res('opts.pbx.nethvoice.tit'):WT.res('opts.pbx.unconfigured.tit')
				}, {
					xtype: 'sospacer'
				}, {
					xtype: 'textfield',
					bind: {
						value: '{record.pbxUsername}'
					},
					disabled: !WT.getVar("pbxConfigured"),
					plugins: 'sonoautocomplete',
					fieldLabel: WT.res('opts.pbx.fld-username.lbl'),
					width: 440,
					emptyText: WT.res('opts.pbx.fld-username-empty.lbl'),
					submitEmptyText: false,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}, {
					xtype: 'sopasswordfield',
					bind: {
						value: '{record.pbxPassword}'
					},
					disabled: !WT.getVar("pbxConfigured"),
					plugins: 'sonoautocomplete',
					//inputType: 'password',
					fieldLabel: WT.res('opts.pbx.fld-password.lbl'),
					width: 440,
					emptyText: WT.res('opts.pbx.fld-password-empty.lbl'),
					submitEmptyText: false,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}, {
					xtype: 'sospacer',
					mult: 2
				}, {
					xtype: 'soformseparator',
					title: WT.res('opts.sms.service.tit')
				}, {
					xtype: 'label',
					text: WT.getVar("smsConfigured")?WT.res('opts.sms.'+WT.getVar("smsProvider")+'.tit'):WT.res('opts.sms.unconfigured.tit')
				}, {
					xtype: 'sospacer'
				}, {
					xtype: 'textfield',
					bind: {
						value: '{record.smsSender}'
					},
					disabled: !WT.getVar("smsConfigured"),
					plugins: 'sonoautocomplete',
					fieldLabel: WT.res('opts.sms.fld-sender.lbl'),
					tooltip: WT.res('opts.sms.fld-sender.tip'),
					width: 440,
					emptyText: WT.res('opts.sms.fld-sender-empty.lbl'),
					submitEmptyText: false,
					listeners: { blur: { fn: me.onBlurAutoSave, scope: me } }
				}, {
					xtype: 'fieldcontainer',
					fieldLabel: '',
					hideEmptyLabel: false,
					layout: 'hbox',
					defaults: {
						margin: '0 0 0 0'
					},
					items: [
						{
							xtype: 'label',
							disabled: !WT.getVar("smsConfigured"),
							html: WT.res('opts.sms.fld-sender.html')
						}
					]
				}
			]
		}, {
			xtype: 'wtopttabsection',
			title: WT.res('opts.about.tit'),
			layout: 'fit',
			items: [
				{
					xtype: 'somarkdownpanel',
					border: false
				}
			],
			listeners: {
				activate: function(s) {
					var md = s.getComponent(0);
					if (!md.markdownHash) {
						s.wait();
						WT.ajaxReq(WT.ID, 'GetAboutInfo', {
							params: {
								optionsProfile: me.profileId
							},
							callback: function(success, json) {
								s.unwait();
								if (success) md.setMarkdown(json.data);
							}
						});
					}
				}
			}
		});
		vm.bind('{record.otpDelivery}', me.onOTPDeliveryChanged, me);
		vm.bind('{record.otpDeviceIsTrusted}', me.onOTPDeviceIsTrusted, me);
	},
	
	changePasswordUI: function() {
		// Password change from this panel can only be performed by the user 
		// itself. Parameters are so taken from the environment because they
		// refers to the same user.
		var me = this,
				vct = WT.createView(me.ID, 'view.ChangePassword', {
					viewCfg: {
						showOldPassword: true,
						profileId: me.profileId,
						policies: Ext.JSON.decode(me.getModel().get('dirPasswordPolicies'), true)
					}
				});
		vct.show();
	},
	
	onOTPDeliveryChanged: function(val) {
		var tab = this.lref('delivery');
		tab.getLayout().setActiveItem(Sonicle.String.deflt(val, 'none'));
	},
	
	onOTPDeviceIsTrusted: function(val) {
		var me = this,
				tab = me.lref('thisdevice'), tit;
		tab.getLayout().setActiveItem(val === true ? 'trusted' : 'nottrusted');
		if(val === true) {
			tit = Ext.String.format(WT.res('opts.otp.thisdevice.trusted.tit'), me.getModel().get('otpDeviceTrustedOn'));
			tab.getComponent('trusted').getComponent(0).setTitle(tit);
		}
	},
	
	activateOTP: function(delivery) {
		var me = this,
				view = (delivery === 'email') ? 'view.OTPSetupEmail' : 'view.OTPSetupGoogleAuth',
				vw = WT.createView(me.ID, view, {
					viewCfg: {
						profileId: me.profileId
					}
				});
		
		vw.getView().on('wizardcompleted', function(s) {
			me.loadModel();
		});
		vw.show();
	},
	
	deactivateOTP: function() {
		var me = this;
		WT.confirm(WT.res('confirm.areyousure'), function(bid) {
			if(bid === 'yes') {
				WT.ajaxReq(WT.ID, 'ManageOTP', {
					params: {
						operation: 'deactivate',
						profileId: me.profileId
					},
					callback: function(success) {
						if(success) me.loadModel();
					}
				});
			}
		});
	},
	
	untrustThisOTP: function() {
		var me = this;
		WT.confirm(WT.res('confirm.areyousure'), function(bid) {
			if(bid === 'yes') {
				WT.ajaxReq(WT.ID, 'ManageOTP', {
					params: {operation: 'untrustthis'},
					callback: function(success) {
						if(success) me.loadModel();
					}
				});
			}
		});
	},
	
	untrustOtherOTP: function() {
		WT.confirm(WT.res('confirm.areyousure'), function(bid) {
			if(bid === 'yes') {
				WT.ajaxReq(WT.ID, 'ManageOTP', {
					params: {operation: 'untrustothers'}
				});
			}
		});
	},
	
	refreshSyncDevices: function() {
		this.lref('gpsync').getStore().load();
	},
	
	showSyncDeviceInfo: function(rec) {
		var me = this;
		me.wait();
		WT.ajaxReq(WT.ID, 'ManageSyncDevices', {
			params: {
				crud: 'info',
				optionsProfile: me.profileId,
				cid: rec.getId()
			},
			callback: function(success, obj) {
				me.unwait();
				if (success) {
					WT.msg(obj.data, {
						title: WT.res('opts.sync.details.tit')
					});
				}
			}
		});
	},
	
	deleteSyncDevice: function(recs) {
		var me = this,
				grid = me.lref('gpsync'),
				sto = grid.getStore();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if (bid === 'yes') {
				sto.remove(recs[0]);
			}
		}, me);
	},
	
	privates: {
		createSyncGridCfg: function(cfg) {
			var me = this;
			return Ext.merge({
				xtype: 'gridpanel',
				border: true,
				store: {
					autoSync: true,
					model: 'WTA.ux.data.EmptyModel',
					proxy: WTF.apiProxy(me.ID, 'ManageSyncDevices', 'data', {
						extraParams: {
							optionsProfile: me.profileId
						}
					})
				},
				viewConfig: {
					deferEmptyText: false,
					emptyText: WT.res('opts.sync.gp-sync.emp')
				},
				columns: [
					{
						dataIndex: 'device',
						header: WT.res('opts.sync.gp-sync.device.lbl'),
						flex: 1
					}, {
						dataIndex: 'lastSync',
						xtype: 'datecolumn',
						format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
						header: WT.res('opts.sync.gp-sync.lastSync.lbl'),
						flex: 1
					}, {
						xtype: 'soactioncolumn',
						items: [
							{
								iconCls: 'fas fa-info-circle',
								tooltip: WT.res('opts.sync.btn-showDeviceInfo.tip'),
								handler: function(view, ridx, cidx, itm, e, rec) {
									me.showSyncDeviceInfo(rec);
								}
							}, {
								iconCls: 'fas fa-trash',
								tooltip: WT.res('act-delete.lbl'),
								handler: function(view, ridx, cidx, itm, e, rec) {
									me.deleteSyncDevice(rec);
								}
							}
						]
					}
				],
				tbar: {
					border: false,
					items: [
						'->',
						me.addAct('refreshSyncDevices', {
							ui: '{secondary|default}',
							text: null,
							tooltip: WT.res('act-refresh.lbl'),
							iconCls: 'wt-icon-refresh',
							handler: function() {
								me.refreshSyncDevices();
							}
						})
					]
				}
			}, cfg);
		}
	}
});
