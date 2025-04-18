/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.Group', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.VMUtils',
		'Sonicle.data.validator.Username',
		'Sonicle.plugin.FieldAvailabilityCheck',
		'Sonicle.plugin.NoAutocomplete',
		'WTA.model.AclSubjectLkp',
		'WTA.ux.panel.Tab',
		'Sonicle.webtop.core.admin.ux.AclSubjectGrid',
		'Sonicle.webtop.core.admin.ux.SubjectServiceGrid',
		'Sonicle.webtop.core.admin.ux.SubjectPermissionGrid'
	],
	
	dockableConfig: {
		title: '{group.tit}',
		iconCls: 'wtadm-icon-group',
		width: 480,
		height: 480
	},
	fieldTitle: 'groupId',
	modelName: 'Sonicle.webtop.core.admin.model.Group',
	returnModelExtraParams: function() {
		return {
			domainId: this.domainId
		};
	},
	focusField: {'new': 'fldgroupid', 'edit': 'flddescription'},
	
	/**
	 * @cfg {String} domainId
	 * The bound domain ID for this entity.
	 */
	domainId: null,
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foIsNew: WTF.foIsEqual('_mode', null, me.MODE_NEW)
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.userSubjectStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.AclSubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupAclSubjects', null, {
				extraParams: {
					domainId: me.domainId,
					users: true
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
					scrollable: true,
					autoPadding: 'ts',
					modelValidation: true,
					defaults: {
						labelAlign: 'top',
						labelSeparator: ''
					},
					items: [
						{
							xtype: 'textfield',
							reference: 'fldgroupid',
							bind: {
								value: '{record.groupId}',
								disabled: '{!foIsNew}'
							},
							disabled: true,
							maskRe: Sonicle.data.validator.Username.maskRe,
							fieldLabel: me.res('group.fld-groupId.lbl'),
							plugins: [
								'sonoautocomplete',
								{
									ptype: 'sofieldavailabilitycheck',
									baseIconCls: 'wt-opacity-50',
									availableTooltipText: WT.res('sofieldavailabilitycheck.availableTooltipText'),
									unavailableTooltipText: WT.res('sofieldavailabilitycheck.unavailableTooltipText'),
									checkAvailability: function(value, done) {
										if (me.getModel().getModified('groupId') === undefined) return false;
										WT.ajaxReq(me.mys.ID, 'ManageDomainGroup', {
											params: {
												crud: 'check',
												domainId: me.domainId,
												group: value
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
							xtype: 'textareafield',
							reference: 'flddescription',
							bind: '{record.description}',
							fieldLabel: me.res('group.fld-description.lbl'),
							anchor: '100%'
						}
					]
				}, {
					xtype: 'wttabpanel',
					autoMargin: 'bs',
					border: true,
					flex: 1,
					activeTab: 0,
					items: [
						{
							xtype: 'wtadmaclsubjectgrid',
							bind: '{record.assignedUsers}',
							border: false,
							title: me.res('group.assignedUsers.tit'),
							iconCls: 'wtadm-icon-users',
							lookupStore: me.userSubjectStore,
							recordCreatorFn: function(value) {
								return {sid: value};
							},
							emptyText: me.res('wtadmaclsubjectgrid.users.emp'),
							pickerTitle: me.res('wtadmaclsubjectgrid.picker.users.tit')
						}, {
							xtype: 'wtadmaclsubjectgrid',
							bind: '{record.assignedRoles}',
							border: false,
							title: me.res('group.assignedRoles.tit'),
							iconCls: 'wtadm-icon-roles',
							lookupStore: me.roleSubjectStore,
							recordCreatorFn: function(value) {
								return {sid: value};
							},
							emptyText: me.res('wtadmaclsubjectgrid.roles.emp'),
							pickerTitle: me.res('wtadmaclsubjectgrid.picker.roles.tit')
						}, {
							xtype: 'wtadmsubjectservicegrid',
							bind: '{record.allowedServices}',
							border: false,
							title: me.res('group.allowedServices.tit'),
							iconCls: 'wtadm-icon-service-module',
							recordCreatorFn: function(value) {
								return {serviceId: value};
							}
						}, {
							xtype: 'wtadmsubjectpermissiongrid',
							bind: '{record.permissions}',
							border: false,
							title: me.res('group.permissions.tit'),
							iconCls: 'wtadm-icon-permission',
							recordCreatorFn: function(serviceId, context, action) {
								return {string: Sonicle.String.join(':', serviceId, context, action)};
							}
							//recordCreatorFn: function(value) {
							//	return {string: value};
							//}
						}
					]
				}
			]
		});
		
		me.on('viewinvalid', me.onViewInvalid);
	},
	
	onViewInvalid: function(s, mo, errs) {
		WTU.updateFieldsErrors(this.lref('pnlmain'), errs);
	}
});
