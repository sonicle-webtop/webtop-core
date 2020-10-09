/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.LoggerEditor', {
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.webtop.core.admin.store.LoggerLevel'
	],
	
	dockableConfig: {
		title: '{loggerEditor.tit}',
		width: 530,
		height: 150,
		modal: true,
		minimizable: false,
		maximizable: false
	},
	promptConfirm: false,
	
	viewModel: {
		data: {
			result: 'cancel',
			data: {
				name: null,
				level: 'ERROR'
			}
		}
	},
	defaultButton: 'btnok',
	
	/**
	 * @cfg {Object} data
	 * Initial data values: name, level;
	*/
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			isValid: WTF.foIsEmpty('data', 'name', true)
		});
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				vm = me.getVM();
		
		if (ic.data) vm.set('data', ic.data);
		Ext.apply(me, {
			buttons: [
				{
					reference: 'btnok',
					bind: {
						disabled: '{!isValid}'
					},
					text: WT.res('act-ok.lbl'),
					handler: function() {
						me.okView();
					}
				}, {
					text: WT.res('act-cancel.lbl'),
					handler: function() {
						me.closeView(false);
					}
				}
			]
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			items: [
				{
					xtype: 'textfield',
					reference: 'fldname',
					bind: '{data.name}',
					allowBlank: false,
					fieldLabel: me.mys.res('loggerEditor.fld-name.lbl'),
					anchor: '100%'
				}, 
				WTF.lookupCombo('id', 'desc', {
					bind: '{data.level}',
					store: {
						type: 'wtloggerlevel'
					},
					fieldLabel: me.mys.res('loggerEditor.fld-level.lbl'),
					anchor: '100%'
				})
			]
		});
		me.on('viewshow', function() {
			me.lref('fldname').focus();
		}, me, {single: true});
	},
	
	okView: function() {
		var me = this,
				vm = me.getVM();
		vm.set('result', 'ok');
		me.fireEvent('viewok', me, vm.get('data.name'), vm.get('data.level'));
		me.closeView(false);
	}
});
