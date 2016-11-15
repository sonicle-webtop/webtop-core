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
		'Sonicle.webtop.core.admin.store.DirConSecurity',
		'Sonicle.webtop.core.admin.store.DirScheme'
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
			foDirCaseSensitive: WTF.checkboxBind('record', 'dirCaseSensitive'),
			foDirPasswordPolicy: WTF.checkboxBind('record', 'dirPasswordPolicy'),
			foUserAutoCreation: WTF.checkboxBind('record', 'userAutoCreation')
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
					bind: '{record.displayName}',
					fieldLabel: me.mys.res('domain.fld-displayName.lbl'),
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
				bind: {
					activeItem: '{record.dirScheme}'
				},
				flex: 1,
				items: [{
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
						bind: '{record.dirConSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirConSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-dirConSecurity.lbl'),
						width: 230
					}),
					{
						xtype: 'textfield',
						bind: '{record.dirUsername}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-dirUsername.lbl'),
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
						bind: '{record.dirConSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirConSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-dirConSecurity.lbl'),
						width: 230
					}),
					{
						xtype: 'textfield',
						bind: '{record.dirPath}',
						fieldLabel: me.mys.res('domain.fld-dirPath.lbl'),
						anchor: '100%'
					}, {
						xtype: 'textfield',
						bind: '{record.dirUsername}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-dirUsername.lbl'),
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
						bind: '{record.dirConSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirConSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-dirConSecurity.lbl'),
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
						bind: '{record.dirConSecurity}',
						allowBlank: false,
						store: Ext.create('Sonicle.webtop.core.admin.store.DirConSecurity', {
							autoLoad: true
						}),
						fieldLabel: me.mys.res('domain.fld-dirConSecurity.lbl'),
						width: 230
					}),
					{
						xtype: 'textfield',
						bind: '{record.dirUsername}',
						plugins: 'sonoautocomplete',
						fieldLabel: me.mys.res('domain.fld-dirUsername.lbl'),
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
		this.updateValidators(this.getModel());
	},
	
	updateValidators: function(mo) {
		switch(mo.get('dirScheme')) {
			case 'webtop':
				mo.getField('dirHost').constructValidators([]);
				mo.getField('dirPath').constructValidators([]);
				mo.getField('dirUsername').constructValidators([]);
				mo.getField('dirPassword').constructValidators([]);
				break;
			case 'ldapwebtop':
				mo.getField('dirHost').constructValidators(['presence']);
				mo.getField('dirPath').constructValidators([]);
				mo.getField('dirUsername').constructValidators(['presence']);
				mo.getField('dirPassword').constructValidators(['presence']);
				break;
			case 'ldap':
				mo.getField('dirHost').constructValidators(['presence']);
				mo.getField('dirPath').constructValidators(['presence']);
				mo.getField('dirUsername').constructValidators(['presence']);
				mo.getField('dirPassword').constructValidators(['presence']);
				break;
			case 'imap':
				mo.getField('dirHost').constructValidators(['presence']);
				mo.getField('dirPath').constructValidators([]);
				mo.getField('dirUsername').constructValidators([]);
				mo.getField('dirPassword').constructValidators([]);
				break;
			case 'smb':
				mo.getField('dirHost').constructValidators(['presence']);
				mo.getField('dirPath').constructValidators([]);
				mo.getField('dirUsername').constructValidators([]);
				mo.getField('dirPassword').constructValidators([]);
				break;
			case 'sftp':
				mo.getField('dirHost').constructValidators(['presence']);
				mo.getField('dirPath').constructValidators([]);
				mo.getField('dirUsername').constructValidators([]);
				mo.getField('dirPassword').constructValidators([]);
				break;
			case 'ad':
				mo.getField('dirHost').constructValidators([]);
				mo.getField('dirPath').constructValidators([]);
				mo.getField('dirUsername').constructValidators([]);
				mo.getField('dirPassword').constructValidators([]);
				break;
			case 'ldapneth':
				mo.getField('dirHost').constructValidators(['presence']);
				mo.getField('dirPath').constructValidators([]);
				mo.getField('dirUsername').constructValidators(['presence']);
				mo.getField('dirPassword').constructValidators(['presence']);
				break;
		}
	}
});
