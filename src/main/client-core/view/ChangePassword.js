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
Ext.define('Sonicle.webtop.core.view.ChangePassword', {
	extend: 'WTA.sdk.DockableView',
	requires: [
		'Sonicle.form.field.Password',
		'Sonicle.plugin.NoAutocomplete'
	],
	
	dockableConfig: {
		title: '{changePassword.tit@com.sonicle.webtop.core}',
		iconCls: 'wt-icon-changePassword-xs',
		width: 320,
		height: 165,
		modal: true,
		minimizable: false
	},
	promptConfirm: false,
	
	showOldPassword: false,
	passwordPolicy: false,
	profileId: null,
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			buttons: [{
				text: WT.res('act-ok.lbl'),
				handler: me.onOkClick,
				scope: me
			}, {
				text: WT.res('act-cancel.lbl'),
				handler: me.onCancelClick,
				scope: me
			}]
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			defaults: {
				labelWidth: 80,
				labelAlign: 'right'
			},
			items: [{
				xtype: 'sopasswordfield',
				reference: 'fldoldpassword',
				allowBlank: false,
				hidden: !me.showOldPassword,
				plugins: 'sonoautocomplete',
				fieldLabel: WT.res('changePassword.fld-oldPassword.lbl'),
				anchor: '100%'
			}, {
				xtype: 'sopasswordfield',
				reference: 'fldnewpassword',
				allowBlank: false,
				vtype: me.passwordPolicy ? 'complexPassword' : 'simplePassword',
				plugins: 'sonoautocomplete',
				fieldLabel: WT.res('changePassword.fld-newPassword.lbl'),
				anchor: '100%'
			}, {
				xtype: 'sopasswordfield',
				reference: 'fldnewpassword2',
				allowBlank: false,
				plugins: 'sonoautocomplete',
				eye: false,
				hideEmptyLabel: false,
				emptyText: WT.res('changePassword.fld-newPassword2.emp'),
				anchor: '100%',
				validator: function(v) {
					return (me.lref('fldnewpassword').getValue() === v) ? true : WT.res('changePassword.error.newPassword2');
				}
			}]
		});
		
		me.on('viewshow', me.onViewShow);
	},
	
	onViewShow: function() {
		var me = this;
		if(me.showOldPassword) {
			me.lref('fldoldpassword').focus();
		} else {
			me.lref('fldnewpassword').focus();
		}
	},
	
	onOkClick: function() {
		var me = this,
				op = me.lref('fldoldpassword'),
				np = me.lref('fldnewpassword');
		if(me.showOldPassword && !op.isValid()) return;
		if(!np.isValid() || !me.lref('fldnewpassword2').isValid()) return;
		me.doPasswordChange(me.showOldPassword ? op.getValue() : null, np.getValue());
	},
	
	onCancelClick: function() {
		this.closeView(false);
	},
	
	doPasswordChange: function(op, np) {
		var me = this;
		me.mys.changeUserPassword(op, np, {
			callback: function(success) {
				if(success) {
					me.closeView(false);
				} else {
					WT.error(WT.res('changePassword.error'));
				}
			}
		});
	}
});
