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
		iconCls: 'wtadm-icon-domain-xs',
		width: 500,
		height: 500
	},
	fieldTitle: 'domainId',
	modelName: 'Sonicle.webtop.core.admin.model.Domain',
	
	passwordPolicy: false,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foEnabled: WTF.checkboxBind('record', 'enabled'),
			foUserAutoCreation: WTF.checkboxBind('record', 'userAutoCreation'),
			foDirCaseSensitive: WTF.checkboxBind('record', 'dirCaseSensitive'),
			foDirPasswordPolicy: WTF.checkboxBind('record', 'dirPasswordPolicy')
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
				xtype: 'wtform',
				modelValidation: true,
				defaults: {
					labelWidth: 120
				},
				items: [{
					xtype: 'textfield',
					reference: 'flddomainid',
					bind: '{record.domainId}',
					disabled: true,
					fieldLabel: me.mys.res('domain.fld-domainId.lbl'),
					width: 300
				}, {
					xtype: 'textfield',
					bind: '{record.internetName}',
					fieldLabel: me.mys.res('domain.fld-internetName.lbl'),
					emptyText: 'example.com',
					width: 400
				}, {
					xtype: 'textfield',
					bind: '{record.description}',
					fieldLabel: me.mys.res('domain.fld-description.lbl'),
					width: 400
				}, {
					xtype: 'checkbox',
					bind: '{foEnabled}',
					hideEmptyLabel: false,
					boxLabel: me.mys.res('domain.fld-enabled.lbl')
				}, {
					xtype: 'checkbox',
					bind: '{foUserAutoCreation}',
					hideEmptyLabel: false,
					boxLabel: me.mys.res('domain.fld-userAutoCreation.lbl')
				}, 
				WTF.lookupCombo('id', 'desc', {
					bind: '{record.dirScheme}',
					allowBlank: false,
					store: Ext.create('Sonicle.webtop.core.admin.store.DirScheme', {
						autoLoad: true
					}),
					fieldLabel: me.mys.res('domain.fld-dirScheme.lbl'),
					width: 400
				})]
			}, {
				xtype: 'container',
				layout: 'card',
				reference: 'pnldir',
				activeItem: 'empty',
				flex: 1,
				items: [{
					xtype: 'panel',
					itemId: 'empty'
				}, {
					xtype: 'wtform',
					itemId: 'webtop',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foDirPasswordPolicy}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-dirPasswordPolicy.lbl')
					}]
				}, {
					xtype: 'wtform',
					itemId: 'ldapwebtop',
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
							emptyText: me.mys.res('domain.fld-dirPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-dirHost.lbl')
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.dirConnSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-dirConnSecurity.lbl'),
						width: 230
					}),
					{
						xtype: 'textfield',
						bind: '{record.dirAdmin}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-dirAdmin.lbl'),
						width: 300
					}, {
						xtype: 'sopasswordfield',
						bind: '{record.dirPassword}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-dirPassword.lbl'),
						width: 300
					}, {
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foDirPasswordPolicy}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-dirPasswordPolicy.lbl')
					}]
				}, {
					xtype: 'tabpanel',
					itemId: 'ldap',
					modelValidation: true,
					items: [{
						title: me.mys.res('domain.ldap.server.tit'),
						xtype: 'wtfieldspanel',
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
									emptyText: me.mys.res('domain.fld-dirPort.emp')
								}],
								fieldLabel: me.mys.res('domain.fld-dirHost.lbl')
							},
							WTF.lookupCombo('id', 'desc', {
								bind: '{record.dirConnSecurity}',
								allowBlank: false,
								store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
									autoLoad: true
								}),
								fieldLabel: me.mys.res('domain.fld-dirConnSecurity.lbl'),
								width: 230
							}),
							{
								xtype: 'textfield',
								bind: '{record.dirAdmin}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.mys.res('domain.ldap.fld-dirAdmin.lbl'),
								anchor: '100%'
							}, {
								xtype: 'sopasswordfield',
								bind: '{record.dirPassword}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.mys.res('domain.fld-dirPassword.lbl'),
								width: 300
							}, {
								xtype: 'checkbox',
								bind: '{foDirCaseSensitive}',
								hideEmptyLabel: false,
								boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
							}, {
								xtype: 'checkbox',
								bind: '{foDirPasswordPolicy}',
								hideEmptyLabel: false,
								boxLabel: me.mys.res('domain.fld-dirPasswordPolicy.lbl')
							}
						]
					}, {
						title: me.mys.res('domain.ldap.login.tit'),
						xtype: 'wtfieldspanel',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapLoginDn}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapLoginDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapLoginFilter}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapLoginFilter.lbl'),
							anchor: '100%'
						}]
					}, {
						title: me.mys.res('domain.ldap.user.tit'),
						xtype: 'wtfieldspanel',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapUserDn}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFilter}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserFilter.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFirstnameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserFirstnameField.lbl'),
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserLastnameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserLastnameField.lbl'),
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserDisplayNameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserDisplayNameField.lbl'),
							width: 330
						}]
					}]
				}, {
					xtype: 'tabpanel',
					itemId: 'ad',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
						title: me.mys.res('domain.ldap.server.tit'),
						xtype: 'wtfieldspanel',
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
									emptyText: me.mys.res('domain.fld-dirPort.emp')
								}],
								fieldLabel: me.mys.res('domain.fld-dirHost.lbl')
							},
							WTF.lookupCombo('id', 'desc', {
								bind: '{record.dirConnSecurity}',
								allowBlank: false,
								store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
									autoLoad: true
								}),
								fieldLabel: me.mys.res('domain.fld-dirConnSecurity.lbl'),
								width: 230
							}),
							{
								xtype: 'textfield',
								bind: '{record.dirAdmin}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.mys.res('domain.ldap.fld-dirAdmin.lbl'),
								anchor: '100%'
							}, {
								xtype: 'sopasswordfield',
								bind: '{record.dirPassword}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.mys.res('domain.fld-dirPassword.lbl'),
								width: 300
							}, {
								xtype: 'checkbox',
								bind: '{foDirCaseSensitive}',
								hideEmptyLabel: false,
								boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
							}, {
								xtype: 'checkbox',
								bind: '{foDirPasswordPolicy}',
								hideEmptyLabel: false,
								boxLabel: me.mys.res('domain.fld-dirPasswordPolicy.lbl')
							}
						]
					}, {
						title: me.mys.res('domain.ldap.login.tit'),
						xtype: 'wtfieldspanel',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapLoginDn}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapLoginDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapLoginFilter}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapLoginFilter.lbl'),
							emptyText: '&(objectCategory=person)(objectClass=user)(!(userAccountControl:1.2.840.113556.1.4.803:=2))',
							anchor: '100%'
						}]
					}, {
						title: me.mys.res('domain.ldap.user.tit'),
						xtype: 'wtfieldspanel',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapUserDn}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserDn.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFilter}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserFilter.lbl'),
							emptyText: '&(objectClass=person)(objectClass=user)',
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFirstnameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserFirstnameField.lbl'),
							emptyText: 'givenName',
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserLastnameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserLastnameField.lbl'),
							emptyText: 'sn',
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserDisplayNameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserDisplayNameField.lbl'),
							emptyText: 'cn',
							width: 330
						}]
					}]
				}, {
					xtype: 'tabpanel',
					itemId: 'ldapneth',
					modelValidation: true,
					items: [{
						title: me.mys.res('domain.ldap.server.tit'),
						xtype: 'wtfieldspanel',
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
									emptyText: me.mys.res('domain.fld-dirPort.emp')
								}],
								fieldLabel: me.mys.res('domain.fld-dirHost.lbl')
							},
							WTF.lookupCombo('id', 'desc', {
								bind: '{record.dirConnSecurity}',
								allowBlank: false,
								store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
									autoLoad: true
								}),
								fieldLabel: me.mys.res('domain.fld-dirConnSecurity.lbl'),
								width: 230
							}),
							{
								xtype: 'textfield',
								bind: '{record.dirAdmin}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.mys.res('domain.ldap.fld-dirAdmin.lbl'),
								anchor: '100%'
							}, {
								xtype: 'sopasswordfield',
								bind: '{record.dirPassword}',
								plugins: 'sonoautocomplete',
								fieldLabel: me.mys.res('domain.fld-dirPassword.lbl'),
								width: 300
							}, {
								xtype: 'checkbox',
								bind: '{foDirCaseSensitive}',
								hideEmptyLabel: false,
								boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
							}, {
								xtype: 'checkbox',
								bind: '{foDirPasswordPolicy}',
								hideEmptyLabel: false,
								boxLabel: me.mys.res('domain.fld-dirPasswordPolicy.lbl')
							}
						]
					}, {
						title: me.mys.res('domain.ldap.login.tit'),
						xtype: 'wtfieldspanel',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapLoginDn}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapLoginDn.lbl'),
							emptyText: 'ou=people,dc={sub1},dc={tld}',
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapLoginFilter}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapLoginFilter.lbl'),
							anchor: '100%'
						}]
					}, {
						title: me.mys.res('domain.ldap.user.tit'),
						xtype: 'wtfieldspanel',
						defaults: {
							labelWidth: 120
						},
						items: [{
							xtype: 'textfield',
							bind: '{record.ldapUserDn}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserDn.lbl'),
							emptyText: 'ou=people,dc={sub1},dc={tld}',
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFilter}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserFilter.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserFirstnameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserFirstnameField.lbl'),
							emptyText: 'givenName',
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserLastnameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserLastnameField.lbl'),
							emptyText: 'sn',
							width: 330
						}, {
							xtype: 'textfield',
							bind: '{record.ldapUserDisplayNameField}',
							fieldLabel: me.mys.res('domain.ldap.fld-ldapUserDisplayNameField.lbl'),
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
							emptyText: me.mys.res('domain.fld-dirPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-dirHost.lbl')
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.dirConnSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirConnSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-dirConnSecurity.lbl'),
						width: 200
					}),
					{
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
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
							emptyText: me.mys.res('domain.fld-dirPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-dirHost.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
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
							emptyText: me.mys.res('domain.fld-dirPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-dirHost.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foDirCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-dirCaseSensitive.lbl')
					}]
				}]
			}]
		});
		me.on('viewload', me.onViewLoad);
		me.getVM().bind('{record.dirScheme}', me.onSchemeChanged, me);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		if(me.isMode(me.MODE_NEW)) {
			me.lref('flddomainid').setDisabled(false);
			me.lref('flddomainid').focus(true);
		} else {
			me.lref('flddomainid').setDisabled(true);
		}
	},
	
	onSchemeChanged: function(v) {
		var mo = this.getModel(), scheme = mo.get('dirScheme');
		this.lref('pnldir').setActiveItem(Ext.isEmpty(scheme) ? 'empty' : scheme);
		mo.refreshValidatorsForDirScheme();
	}
});
