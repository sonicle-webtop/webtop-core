/* 
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.User', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.FakeInput',
		'Sonicle.form.Spacer',
		'Sonicle.form.field.Password',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.webtop.core.admin.ux.AclSubjectGrid',
		'Sonicle.webtop.core.admin.ux.SubjectServiceGrid',
		'Sonicle.webtop.core.admin.ux.SubjectPermissionGrid'
	],
	mixins: [
		'WTA.mixin.PwdPolicies'
	],
	
	dockableConfig: {
		title: '{user.tit}',
		iconCls: 'wtadm-icon-user',
		width: 550,
		height: 550
	},
	fieldTitle: 'userId',
	modelName: 'Sonicle.webtop.core.admin.model.User',
	returnModelExtraParams: function() {
		return {
			domainId: this.domainId
		};
	},
	focusField: {'new': 'flduserid', 'edit': 'flddname'},
	
	/**
	 * @cfg {String} domainId
	 * The bound domain ID for this entity.
	 */
	domainId: null,
	
	askForPassword: true,
	policies: null,
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foIsNew: WTF.foIsEqual('_mode', null, me.MODE_NEW),
			foEnabled: WTF.checkboxBind('record', 'enabled'),
			foPswDisabled: WTF.foGetFn('_mode', null, function(val) {
				return val !== me.MODE_NEW || !me.askForPassword;
			})
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.groupSubjectStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.AclSubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupAclSubjects', null, {
				extraParams: {
					domainId: me.domainId,
					groups: true
				}
			})
		});
		me.roleSubjectStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.AclSubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupAclSubjects', null, {
				extraParams: {
					domainId: me.domainId,
					roles: true
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'wtfieldspanel',
					reference: 'pnlmain',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [
						{
							xtype: 'sofakeinput' // Disable Chrome autofill
						}, {
							xtype: 'sofakeinput', // Disable Chrome autofill
							type: 'password'
						}, {
							xtype: 'textfield',
							reference: 'flduserid',
							bind: {
								value: '{record.userId}',
								disabled: '{!foIsNew}'
							},
							disabled: true,
							maskRe: Sonicle.data.validator.Username.maskRe,
							fieldLabel: me.res('user.fld-userId.lbl'),
							plugins: [
								'sonoautocomplete',
								{
									ptype: 'sofieldavailabilitycheck',
									baseIconCls: 'wt-opacity-50',
									availableTooltipText: WT.res('sofieldavailabilitycheck.availableTooltipText'),
									unavailableTooltipText: WT.res('sofieldavailabilitycheck.unavailableTooltipText'),
									checkAvailability: function(value, done) {
										if (me.getModel().getModified('userId') === undefined) return false;
										WT.ajaxReq(me.mys.ID, 'ManageDomainUser', {
											params: {
												crud: 'check',
												domainId: me.domainId,
												user: value
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
							xtype: 'sopasswordfield',
							reference: 'fldpassword',
							bind: {
								value: '{record.password}',
								hidden: '{foPswDisabled}'
							},
							maxLength: 128,
							plugins: 'sonoautocomplete',
							fieldLabel: me.res('user.fld-password.lbl'),
							anchor: '100%'
						}, {
							xtype: 'sopasswordfield',
							reference: 'fldpassword2',
							bind: {
								value: '{record.password2}',
								hidden: '{foPswDisabled}'
							},
							maxLength: 128,
							plugins: 'sonoautocomplete',
							eye: false,
							hideEmptyLabel: false,
							emptyText: me.res('user.fld-password2.emp'),
							anchor: '100%'
						}, {
							xtype: 'checkbox',
							bind: '{foEnabled}',
							hideEmptyLabel: false,
							boxLabel: me.res('user.fld-enabled.lbl')
						}, {
							xtype: 'textfield',
							bind: '{record.firstName}',
							fieldLabel: me.res('user.fld-firstName.lbl'),
							anchor: '100%',
							listeners: {
								blur: function() {
									me.lref('flddname').setEmptyText(me.getModel().buildDisplayName());
								}
							}
						}, {
							xtype: 'textfield',
							bind: '{record.lastName}',
							fieldLabel: me.res('user.fld-lastName.lbl'),
							anchor: '100%',
							listeners: {
								blur: function() {
									me.lref('flddname').setEmptyText(me.getModel().buildDisplayName());
								}
							}
						}, {
							xtype: 'textfield',
							reference: 'flddname',
							bind: '{record.displayName}',
							fieldLabel: me.res('user.fld-displayName.lbl'),
							anchor: '100%',
							listeners: {
								blur: function(s) {
									s.setEmptyText(me.getModel().buildDisplayName());
								}
							}
						}
					]
				}, {
					xtype: 'tabpanel',
					flex: 1,
					activeTab: 0,
					items: [
						{
							xtype: 'wtadmaclsubjectgrid',
							title: me.res('user.assignedGroups.tit'),
							iconCls: 'wtadm-icon-groups',
							bind: '{record.assignedGroups}',
							lookupStore: me.groupSubjectStore,
							recordCreatorFn: function(value) {
								return {sid: value};
							},
							emptyText: me.res('wtadmaclsubjectgrid.groups.emp'),
							pickerTitle: me.res('wtadmaclsubjectgrid.picker.groups.tit')
						}, {
							xtype: 'wtadmaclsubjectgrid',
							title: me.res('user.assignedRoles.tit'),
							iconCls: 'wtadm-icon-roles',
							bind: '{record.assignedRoles}',
							lookupStore: me.roleSubjectStore,
							recordCreatorFn: function(value) {
								return {sid: value};
							},
							emptyText: me.res('wtadmaclsubjectgrid.roles.emp'),
							pickerTitle: me.res('wtadmaclsubjectgrid.picker.roles.tit')
						}, {
							xtype: 'wtadmsubjectservicegrid',
							title: me.res('user.allowedServices.tit'),
							iconCls: 'wtadm-icon-service-module',
							bind: '{record.allowedServices}',
							recordCreatorFn: function(value) {
								return {serviceId: value};
							}
						}, {
							xtype: 'wtadmsubjectpermissiongrid',
							title: me.res('user.permissions.tit'),
							iconCls: 'wtadm-icon-permission',
							bind: '{record.permissions}',
							recordCreatorFn: function(serviceId, context, action) {
								return {string: Sonicle.String.join(':', serviceId, context, action)};
							}
							/*
							recordCreatorFn: function(value) {
								return {string: value};
							}
							*/
						}
					]
				}
			]
		});
		
		me.on('viewload', me.onViewLoad);
		me.on('viewinvalid', me.onViewInvalid);
	},
	
	privates: {
		onViewLoad: function(s, success) {
			var me = this,
				mo = me.getModel();

			if (mo) mo.passwordFieldLabel = me.res('user.fld-password.lbl');
			if (me.isMode(me.MODE_NEW)) {
				if (me.askForPassword) {
					mo.validatePassword = me.askForPassword;
					mo.pwdPolicies = me.policies;
				}
			}
			mo.getValidation(true);
		},
		
		onViewInvalid: function(s, mo, errs) {
			WTU.updateFieldsErrors(this.lref('pnlmain'), errs);
		}
	}
});
