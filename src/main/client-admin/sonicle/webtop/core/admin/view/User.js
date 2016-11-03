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
Ext.define('Sonicle.webtop.core.admin.view.User', {
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.FakeInput',
		'Sonicle.form.Spacer',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.webtop.core.ux.grid.RolePermissions',
		'Sonicle.webtop.core.ux.grid.RoleSvcPermissions'
	],
	
	dockableConfig: {
		title: '{user.tit}',
		iconCls: 'wta-icon-user-xs',
		width: 650,
		height: 500
	},
	fieldTitle: 'userId',
	modelName: 'Sonicle.webtop.core.admin.model.User',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foEnabled: WTF.checkboxBind('record', 'enabled')
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
					labelWidth: 100
				},
				items: [{
					xtype: 'sofakeinput' // Disable Chrome autofill
				}, {
					xtype: 'sofakeinput', // Disable Chrome autofill
					type: 'password'
				}, {
					xtype: 'textfield',
					reference: 'flduserid',
					bind: '{record.userId}',
					disabled: true,
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('user.fld-userId.lbl'),
					width: 300
				}, {
					xtype: 'textfield',
					reference: 'fldpassword',
					bind: '{record.password}',
					inputType: 'password',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('user.fld-password.lbl'),
					width: 300
				}, {
					xtype: 'textfield',
					reference: 'fldpassword2',
					bind: '{record.password2}',
					inputType: 'password',
					plugins: 'sonoautocomplete',
					hideEmptyLabel: false,
					emptyText: me.mys.res('user.fld-password2.emp'),
					width: 300
				}, {
					xtype: 'sospacer'
				}, {
					xtype: 'checkbox',
					bind: '{foEnabled}',
					hideEmptyLabel: false,
					boxLabel: me.mys.res('user.fld-enabled.lbl')
				}, {
					xtype: 'textfield',
					bind: '{record.firstName}',
					fieldLabel: me.mys.res('user.fld-firstName.lbl'),
					width: 400
				}, {
					xtype: 'textfield',
					bind: '{record.lastName}',
					fieldLabel: me.mys.res('user.fld-lastName.lbl'),
					width: 400
				}, {
					xtype: 'textfield',
					bind: '{record.displayName}',
					fieldLabel: me.mys.res('user.fld-displayName.lbl'),
					width: 400
				}]
			}, {
				xtype: 'tabpanel',
				flex: 1,
				activeTab: 0,
				items: [{
					xtype: 'wtrolesvcpermissionsgrid',
					title: me.mys.res('user.servicesPerms.tit'),
					iconCls: 'wt-icon-service-module-xs',
					bind: {
						store: '{record.servicesPerms}'
					},
					listeners: {
						pick: function(s, val) {
							var mo = me.getModel();
							mo.servicesPerms().add({
								_fk: mo.getId(),
								serviceId: '',
								groupName: '',
								action: '',
								instance: val
							});
						}
					}
				}, {
					xtype: 'wtrolepermissionsgrid',
					title: me.mys.res('user.othersPerms.tit'),
					iconCls: 'wt-icon-permission-xs',
					bind: {
						store: '{record.othersPerms}'
					},
					listeners: {
						pick: function(s, serviceId, groupName, action) {
							var mo = me.getModel();
							mo.othersPerms().add({
								_fk: mo.getId(),
								serviceId: serviceId,
								groupName: groupName,
								action: action,
								instance: '*'
							});
						}
					}
				}]
			}]
		});
		
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		if(me.isMode(me.MODE_NEW)) {
			me.lref('flduserid').setDisabled(false);
			me.lref('fldpassword').setHidden(false);
			me.lref('fldpassword2').setHidden(false);
			me.lref('flduserid').focus(true);
		} else {
			me.lref('flduserid').setDisabled(true);
			me.lref('fldpassword').setHidden(true);
			me.lref('fldpassword2').setHidden(true);
		}
	}
});
