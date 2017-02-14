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
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.FakeInput',
		'Sonicle.form.Spacer',
		'Sonicle.form.field.Password',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.webtop.core.admin.ux.GroupGrid',
		'Sonicle.webtop.core.admin.ux.RoleGrid',
		'Sonicle.webtop.core.admin.ux.RoleServiceGrid',
		'Sonicle.webtop.core.admin.ux.RolePermissionGrid'
	],
	
	dockableConfig: {
		title: '{user.tit}',
		iconCls: 'wtadm-icon-user-xs',
		width: 650,
		height: 500
	},
	fieldTitle: 'userId',
	modelName: 'Sonicle.webtop.core.admin.model.User',
	
	domainId: null,
	askForPassword: true,
	passwordPolicy: false,
	
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
				xtype: 'wtfieldspanel',
				reference: 'pnlmain',
				modelValidation: true,
				defaults: {
					labelWidth: 120
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
					xtype: 'sopasswordfield',
					reference: 'fldpassword',
					bind: '{record.password}',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('user.fld-password.lbl'),
					width: 300
				}, {
					xtype: 'sopasswordfield',
					reference: 'fldpassword2',
					bind: '{record.password2}',
					plugins: 'sonoautocomplete',
					eye: false,
					hideEmptyLabel: false,
					emptyText: me.mys.res('user.fld-password2.emp'),
					width: 300
				}, {
					xtype: 'checkbox',
					bind: '{foEnabled}',
					hideEmptyLabel: false,
					boxLabel: me.mys.res('user.fld-enabled.lbl')
				}, {
					xtype: 'textfield',
					bind: '{record.displayName}',
					fieldLabel: me.mys.res('user.fld-displayName.lbl'),
					width: 400
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
				}]
			}, {
				xtype: 'tabpanel',
				flex: 1,
				activeTab: 0,
				items: [{
					xtype: 'wtadmgroupgrid',
					title: me.mys.res('user.assignedGroups.tit'),
					iconCls: 'wtadm-icon-groups-xs',
					bind: {
						store: '{record.assignedGroups}'
					},
					domainId: me.domainId,
					listeners: {
						pick: function(s, val) {
							var mo = me.getModel();
							mo.assignedGroups().add({
								_fk: mo.getId(),
								groupId: val
							});
						}
					}
				}, {
					xtype: 'wtadmrolegrid',
					title: me.mys.res('user.assignedRoles.tit'),
					iconCls: 'wtadm-icon-roles-xs',
					bind: {
						store: '{record.assignedRoles}'
					},
					domainId: me.domainId,
					listeners: {
						pick: function(s, val) {
							var mo = me.getModel();
							mo.assignedRoles().add({
								_fk: mo.getId(),
								roleUid: val
							});
						}
					}
				}, {
					xtype: 'wtadmroleservicegrid',
					title: me.mys.res('user.assignedServices.tit'),
					iconCls: 'wtadm-icon-service-module-xs',
					bind: {
						store: '{record.assignedServices}'
					},
					listeners: {
						pick: function(s, val) {
							var mo = me.getModel();
							mo.assignedServices().add({
								_fk: mo.getId(),
								serviceId: val
							});
						}
					}
				}, {
					xtype: 'wtadmrolepermissiongrid',
					title: me.mys.res('user.permissions.tit'),
					iconCls: 'wtadm-icon-permission-xs',
					bind: {
						store: '{record.permissions}'
					},
					listeners: {
						pick: function(s, serviceId, groupName, action) {
							var mo = me.getModel();
							mo.permissions().add({
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
		me.on('viewinvalid', me.onViewInvalid);
	},
	
	onViewLoad: function(s, success) {
		if (!success) return;
		var me = this,
				flduserid = me.lref('flduserid'),
				fldpassword = me.lref('fldpassword'),
				fldpassword2 = me.lref('fldpassword2'),
				mo;
		if (me.isMode(me.MODE_NEW)) {
			mo = me.getModel();
			flduserid.setDisabled(false);
			if (me.askForPassword) {
				mo.setFieldValidators('password', [
					'presence', {
					type: 'sopassword',
					complex: me.passwordPolicy
				}]);
				mo.setFieldValidators('password2', [
					'presence', {
					type: 'soequality',
					equalField: 'password',
					fieldLabel: me.mys.res('user.fld-password.lbl')
				}]);
				mo.getValidation(true);
				fldpassword.setHidden(false);
				fldpassword2.setHidden(false);
			} else {
				fldpassword.setHidden(true);
				fldpassword2.setHidden(true);
			}	
			flduserid.focus(true);
			
		} else {
			flduserid.setDisabled(true);
			fldpassword.setHidden(true);
			fldpassword2.setHidden(true);
		}
	},
	
	onViewInvalid: function(s, mo, errs) {
		WTU.updateFieldsErrors(this.lref('pnlmain'), errs);
	}
});
