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
Ext.define('Sonicle.webtop.core.admin.ux.PermissionPicker', {
	extend: 'Ext.panel.Panel',
	alias: ['widget.wtadmpermissionpicker'],
	requires: [
		'Sonicle.util.DistinctFilter',
		'Sonicle.webtop.core.model.ServiceLkp',
		'Sonicle.webtop.core.model.ServicePermissionLkp'
	],
	
	layout: 'anchor',
	referenceHolder: true,
	viewModel: true,
	bodyPadding: 5,
	defaults: {anchor: '100%'},
	
	/**
	 * @cfg {Function} handler
	 * Optional. A function that will handle the pick event of this picker.
	 * The handler is passed the following parameters:
	 *   - `picker` : WTA.ux.PermissionPicker
	 * This component.
	 *   - `serviceId` : String
	 * The `serviceId` field.
	 *   - `groupName` : String
	 * The `groupName` field.
	 *   - `action` : String
	 * The `action` field.
	 */
	
	/**
	 * @cfg {Object} scope 
	 * The scope (`this` reference) in which the `{@link #handler}` function will be called.
	 * Defaults to this ListPicker instance.
	 */
	
	/**
	 * @event cancelclick
	 * Fires when the cancel button is pressed.
	 * @param {Sonicle.picker.List} this
	 */
	
	/**
	 * @event okclick
	 * Fires when the ok button is pressed.
	 * @param {Sonicle.picker.List} this
	 */
	
	/**
     * @event pick
     * Fires when a permission is picked.
	 * @param {WTA.ux.PermissionPicker} this
	 * @param {String} serviceId The `serviceId` field.
	 * @param {String} groupName The `groupName` field.
	 * @param {String} action The `action` field.
     */
	
	initComponent: function() {
		var me = this;
		
		me.buttons = [{
			text: WT.res('act-ok.lbl'),
			handler: function() {
				me.fireEvent('okclick', me);
				if (me.isValid()) me.firePick();
			}
		}, {
			text: WT.res('act-cancel.lbl'),
			handler: function() {
				me.fireEvent('cancelclick', me);
			}
		}];
		
		me.callParent(arguments);
		
		me.add([
			WTF.localCombo('id', 'label', {
				reference: 'fldservice',
				publishes: 'value',
				allowBlank: false,
				anyMatch: true,
				store: {
					autoLoad: true,
					model: 'Sonicle.webtop.core.model.ServiceLkp',
					proxy: WTF.proxy(WT.ID, 'LookupServices')
				},
				listeners: {
					beforeselect: function() {
						me.lookupReference('fldgroupname').getFilters().getByKey('distinctGroup').resetDistinctValues();
					}
				},
				fieldLabel: WT.res(WT.ID + '.admin', 'wtadmpermissionpicker.serviceId.lbl')
			}),
			WTF.localCombo('groupName', 'groupName', {
				reference: 'fldgroupname',
				publishes: 'value',
				allowBlank: false,
				anyMatch: true,
				store: {
					autoLoad: true,
					model: 'Sonicle.webtop.core.model.ServicePermissionLkp',
					proxy: WTF.proxy(WT.ID, 'LookupServicesPermissions')
				},
				bind: {
					disabled: '{!fldservice.value}',
					filters: [
						{
							property: 'serviceId',
							value: '{fldservice.value}'
						},
						new Sonicle.util.DistinctFilter({
							id: 'distinctGroup',
							filterFn: function(item) {
								return this.filterIfDistinct(item.get('groupName'));
							}
						})
					]
				},
				disabled: true,
				fieldLabel: WT.res(WT.ID + '.admin', 'wtadmpermissionpicker.groupName.lbl')
			}),
			WTF.localCombo('action', 'action', {
				reference: 'fldaction',
				allowBlank: false,
				anyMatch: true,
				store: {
					autoLoad: true,
					model: 'Sonicle.webtop.core.model.ServicePermissionLkp',
					proxy: WTF.proxy(WT.ID, 'LookupServicesPermissions')
				},
				bind: {
					disabled: '{!fldgroupname.value}',
					filters: [
						{
							property: 'serviceId',
							value: '{fldservice.value}'
						}, {
							property: 'groupName',
							value: '{fldgroupname.value}'
						}
					]
				},
				disabled: true,
				fieldLabel: WT.res(WT.ID + '.admin', 'wtadmpermissionpicker.action.lbl')
			})
		]);
	},
	
	firePick: function() {
		var me = this,
				sid = me.lookupReference('fldservice').getValue(),
				gname = me.lookupReference('fldgroupname').getValue(),
				act = me.lookupReference('fldaction').getValue(),
				handler = me.handler;
		me.fireEvent('pick', me, sid, gname, act);
		if (handler) handler.call(me.scope || me, me, sid, gname, act);
	},
	
	isValid: function() {
		var me = this;
		if (!me.lookupReference('fldservice').isValid()) return false;
		if (!me.lookupReference('fldgroupname').isValid()) return false;
		if (!me.lookupReference('fldaction').isValid()) return false;
		return true;
	}
});
