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
Ext.define('Sonicle.webtop.core.admin.view.Role', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.webtop.core.admin.ux.RoleServiceGrid',
		'Sonicle.webtop.core.admin.ux.RolePermissionGrid'
	],
	
	dockableConfig: {
		title: '{role.tit}',
		iconCls: 'wtadm-icon-role-xs',
		width: 650,
		height: 500
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.core.admin.model.Role',
	
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
					xtype: 'textfield',
					reference: 'fldname',
					bind: '{record.name}',
					fieldLabel: me.mys.res('role.fld-name.lbl'),
					width: 300
				}, {
					xtype: 'textareafield',
					bind: '{record.description}',
					fieldLabel: me.mys.res('role.fld-description.lbl'),
					anchor: '100%'
				}]
			}, {
				xtype: 'tabpanel',
				flex: 1,
				activeTab: 0,
				items: [{
					xtype: 'wtadmroleservicegrid',
					title: me.mys.res('role.assignedServices.tit'),
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
					title: me.mys.res('role.permissions.tit'),
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
		if(!success) return;
		var me = this;
		me.lref('fldname').focus(true);
	},
	
	onViewInvalid: function(s, mo, errs) {
		WTU.updateFieldsErrors(this.lref('pnlmain'), errs);
	}
});
