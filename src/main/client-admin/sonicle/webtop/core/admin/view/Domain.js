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
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.form.Spacer',
		'Sonicle.form.field.Password',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.webtop.core.admin.model.Domain',
		'Sonicle.webtop.core.admin.store.AuthConnSecurity',
		'Sonicle.webtop.core.admin.store.AuthScheme'
	],
	
	dockableConfig: {
		title: '{domain.tit}',
		iconCls: 'wta-icon-domain-xs',
		width: 500,
		height: 500
	},
	fieldTitle: 'domainId',
	modelName: 'Sonicle.webtop.core.admin.model.Domain',
	
	domainId: null,
	passwordPolicy: false,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foEnabled: WTF.checkboxBind('record', 'enabled'),
			foUserAutoCreation: WTF.checkboxBind('record', 'userAutoCreation'),
			foAuthCaseSensitive: WTF.checkboxBind('record', 'authCaseSensitive'),
			foAuthPasswordPolicy: WTF.checkboxBind('record', 'authPasswordPolicy')
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
					bind: '{record.authScheme}',
					allowBlank: false,
					store: Ext.create('Sonicle.webtop.core.admin.store.AuthScheme', {
						autoLoad: true
					}),
					fieldLabel: me.mys.res('domain.fld-authScheme.lbl'),
					width: 400
				})]
			}, {
				xtype: 'container',
				layout: 'card',
				reference: 'pnlauth',
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
						bind: '{foAuthCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authCaseSensitive.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foAuthPasswordPolicy}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authPasswordPolicy.lbl')
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
							bind: '{record.authHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.authPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.mys.res('domain.fld-authPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-authHost.lbl')
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.authConnSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.AuthConnSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-authConnSecurity.lbl'),
						width: 230
					}),
					{
						xtype: 'textfield',
						bind: '{record.authUsername}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-authUsername.lbl'),
						width: 300
					}, {
						xtype: 'sopasswordfield',
						bind: '{record.authPassword}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-authPassword.lbl'),
						width: 300
					}, {
						xtype: 'checkbox',
						bind: '{foAuthCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authCaseSensitive.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foAuthPasswordPolicy}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authPasswordPolicy.lbl')
					}]
				}, {
					xtype: 'wtform',
					itemId: 'ldap',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
						xtype: 'fieldcontainer',
						layout: 'hbox',
						items: [{
							xtype: 'textfield',
							bind: '{record.authHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.authPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.mys.res('domain.fld-authPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-authHost.lbl')
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.authConnSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.AuthConnSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-authConnSecurity.lbl'),
						width: 230
					}),
					{
						xtype: 'textfield',
						bind: '{record.authPath}',
						fieldLabel: me.mys.res('domain.fld-authPath.lbl'),
						anchor: '100%'
					}, {
						xtype: 'textfield',
						bind: '{record.authUsername}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-authUsername.lbl'),
						width: 300
					}, {
						xtype: 'sopasswordfield',
						bind: '{record.authPassword}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-authPassword.lbl'),
						width: 300
					}, {
						xtype: 'checkbox',
						bind: '{foAuthCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authCaseSensitive.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foAuthPasswordPolicy}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authPasswordPolicy.lbl')
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
							bind: '{record.authHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.authPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.mys.res('domain.fld-authPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-authHost.lbl')
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.authConnSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.AuthConnSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-authConnSecurity.lbl'),
						width: 200
					}),
					{
						xtype: 'checkbox',
						bind: '{foAuthCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authCaseSensitive.lbl')
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
							bind: '{record.authHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.authPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.mys.res('domain.fld-authPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-authHost.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foAuthCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authCaseSensitive.lbl')
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
							bind: '{record.authHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.authPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.mys.res('domain.fld-authPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-authHost.lbl')
					}, {
						xtype: 'checkbox',
						bind: '{foAuthCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authCaseSensitive.lbl')
					}]
				}, {
					xtype: 'wtform',
					itemId: 'ad',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
							
					}]
				}, {
					xtype: 'wtform',
					itemId: 'ldapneth',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [{
						xtype: 'fieldcontainer',
						layout: 'hbox',
						items: [{
							xtype: 'textfield',
							bind: '{record.authHost}',
							width: 160
						}, {
							xtype: 'displayfield',
							value: '&nbsp;:&nbsp;'
						}, {
							xtype: 'numberfield',
							bind: '{record.authPort}',
							hideTrigger: true,
							minValue: 1,
							maxValue: 65000,
							width: 60,
							emptyText: me.mys.res('domain.fld-authPort.emp')
						}],
						fieldLabel: me.mys.res('domain.fld-authHost.lbl')
					},
					WTF.lookupCombo('id', 'desc', {
						bind: '{record.authConnSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.AuthConnSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-authConnSecurity.lbl'),
						width: 230
					}),
					{
						xtype: 'textfield',
						bind: '{record.authUsername}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-authUsername.lbl'),
						width: 300
					}, {
						xtype: 'sopasswordfield',
						bind: '{record.authPassword}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-authPassword.lbl'),
						width: 300
					}, {
						xtype: 'checkbox',
						bind: '{foAuthCaseSensitive}',
						hideEmptyLabel: false,
						boxLabel: me.mys.res('domain.fld-authCaseSensitive.lbl')
					}]
				}]
			}]
		});
		me.on('viewload', me.onViewLoad);
		me.getVM().bind('{record.authScheme}', me.onSchemeChanged, me);
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
		var mo = this.getModel(), scheme = mo.get('authScheme');
		this.lref('pnlauth').setActiveItem(Ext.isEmpty(scheme) ? 'empty' : scheme);
		this.updateValidators(mo);
	},
	
	updateValidators: function(mo) {
		switch(mo.get('authScheme')) {
			case 'webtop':
				mo.getField('authHost').constructValidators([]);
				mo.getField('authPath').constructValidators([]);
				mo.getField('authUsername').constructValidators([]);
				mo.getField('authPassword').constructValidators([]);
				break;
			case 'ldapwebtop':
				mo.getField('authHost').constructValidators(['presence']);
				mo.getField('authPath').constructValidators([]);
				mo.getField('authUsername').constructValidators(['presence']);
				mo.getField('authPassword').constructValidators(['presence']);
				break;
			case 'ldap':
				mo.getField('authHost').constructValidators(['presence']);
				mo.getField('authPath').constructValidators(['presence']);
				mo.getField('authUsername').constructValidators(['presence']);
				mo.getField('authPassword').constructValidators(['presence']);
				break;
			case 'imap':
				mo.getField('authHost').constructValidators(['presence']);
				mo.getField('authPath').constructValidators([]);
				mo.getField('authUsername').constructValidators([]);
				mo.getField('authPassword').constructValidators([]);
				break;
			case 'smb':
				mo.getField('authHost').constructValidators(['presence']);
				mo.getField('authPath').constructValidators([]);
				mo.getField('authUsername').constructValidators([]);
				mo.getField('authPassword').constructValidators([]);
				break;
			case 'sftp':
				mo.getField('authHost').constructValidators(['presence']);
				mo.getField('authPath').constructValidators([]);
				mo.getField('authUsername').constructValidators([]);
				mo.getField('authPassword').constructValidators([]);
				break;
			case 'ad':
				mo.getField('authHost').constructValidators([]);
				mo.getField('authPath').constructValidators([]);
				mo.getField('authUsername').constructValidators([]);
				mo.getField('authPassword').constructValidators([]);
				break;
			case 'ldapneth':
				mo.getField('authHost').constructValidators(['presence']);
				mo.getField('authPath').constructValidators([]);
				mo.getField('authUsername').constructValidators(['presence']);
				mo.getField('authPassword').constructValidators(['presence']);
				break;
		}
	}
});
