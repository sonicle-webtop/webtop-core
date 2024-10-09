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
Ext.define('Sonicle.webtop.core.admin.view.Resource', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.VMUtils',
		'Sonicle.data.validator.Username',
		'Sonicle.form.trigger.Clear',
		'Sonicle.form.field.ComboBox',
		'Sonicle.plugin.FieldAvailabilityCheck',
		'WTA.model.AclSubjectLkp',
		'Sonicle.webtop.core.admin.ux.AclSubjectGrid',
		'Sonicle.webtop.core.admin.model.Resource',
		'Sonicle.webtop.core.store.ResourceType'
	],
	
	dockableConfig: {
		title: '{resource.tit}',
		iconCls: 'wt-icon-resource',
		width: 450,
		height: 550
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.core.admin.model.Resource',
	returnModelExtraParams: function() {
		return {
			domainId: this.domainId
		};
	},
	focusField: {'new': 'fldname', 'edit': 'flddname'},
	
	/**
	 * @cfg {String} domainId
	 * The bound domain ID for this entity.
	 */
	domainId: null,
	
	/**
	 * @cfg {String} domainName
	 * The primary domain-name of the bound domain ID.
	 */
	domainName: 'example.com',
	
	constructor: function(cfg) {
		var me = this;
		if (!cfg.domainId) Ext.raise('domainId is mandatory');
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foIsNew: WTF.foIsEqual('_mode', null, me.MODE_NEW),
			foAvailable: WTF.checkboxBind('record', 'available')
		});
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.aclSubjectStore = Ext.create('Ext.data.Store', {
			autoLoad: true,
			model: 'WTA.model.AclSubjectLkp',
			proxy: WTF.proxy(me.mys.ID, 'LookupAclSubjects', null, {
				extraParams: {
					domainId: me.domainId,
					users: true,
					groups: true
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
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
					reference: 'fldname',
					bind: {
						value: '{record.name}',
						disabled: '{!foIsNew}'
					},
					disabled: true,
					maskRe: Sonicle.data.validator.Username.maskRe,
					fieldLabel: me.res('resource.fld-name.lbl'),
					plugins: [
						{
							ptype: 'sofieldavailabilitycheck',
							baseIconCls: 'wt-opacity-50',
							availableTooltipText: WT.res('sofieldavailabilitycheck.availableTooltipText'),
							unavailableTooltipText: WT.res('sofieldavailabilitycheck.unavailableTooltipText'),
							checkAvailability: function(value, done) {
								if (me.getModel().getModified('name') === undefined) return false;
								WT.ajaxReq(me.mys.ID, 'ManageDomainResource', {
									params: {
										crud: 'check',
										domainId: me.domainId,
										resource: value
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
					xtype: 'textfield',
					reference: 'flddname',
					bind: '{record.displayName}',
					fieldLabel: me.res('resource.fld-displayName.lbl'),
					anchor: '100%'
				},
				WTF.lookupCombo('id', 'desc', {
					bind: {
						value: '{record.type}',
						disabled: '{!foIsNew}'
					},
					store: {
						xclass: 'Sonicle.webtop.core.store.ResourceType',
						autoLoad: true
					},
					fieldLabel: me.res('resource.fld-type.lbl'),
					emptyText: me.res('resource.fld-type.emp'),
					anchor: '100%'
				}),
				{
					xtype: 'checkbox',
					bind: '{foAvailable}',
					hideEmptyLabel: false,
					boxLabel: me.res('resource.fld-available.lbl')
				}, {
					xtype: 'textfield',
					bind: {
						value: '{record.email}',
						emptyText: '{record.name}' + '@' + me.domainName
					},
					fieldLabel: me.res('resource.fld-email.lbl'),
					anchor: '100%'
				}, {
					xtype: 'soformseparator',
					title: me.res('resource.permissions.tit')
				}, {
					xtype: 'sotext',
					cls: 'wt-theme-text-color-off',
					text: me.res('resource.permissions.info')
				}, {
					xtype: 'wtadmaclsubjectgrid',
					bind: '{record.allowedSids}',
					lookupStore: me.aclSubjectStore,
					recordCreatorFn: function(value) {
						return {sid: value};
					},
					border: true,
					height: 160
				}, {
					xtype: 'sovspacer'
				},
				WTF.localCombo('id', 'name', {
					xtype: 'socombo',
					bind: '{record.managerSid}',
					store: me.aclSubjectStore,
					//sourceField: 'name',
					iconField: 'icon',
					triggers: {
						clear: WTF.clearTrigger()
					},
					emptyText: me.res('resource.fld-managerRole.emp'),
					fieldLabel: me.res('resource.fld-managerRole.lbl'),
					anchor: '100%'
				})
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		var me = this,
			mo = me.getModel();
		
		mo.getValidation(true);
	}
});
