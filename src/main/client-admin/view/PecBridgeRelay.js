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
Ext.define('Sonicle.webtop.core.admin.view.PecBridgeRelay', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.form.field.Password',
		'Sonicle.plugin.NoAutocomplete',
		'Sonicle.webtop.core.admin.store.PecBridgeConnSecurity',
		'Sonicle.webtop.core.admin.model.PecBridgeRelay'
	],
	
	dockableConfig: {
		title: '{pecBridgeRelay.tit}',
		iconCls: 'wta-icon-pecBridge-xs',
		width: 400,
		height: 300
	},
	fieldTitle: 'userId',
	modelName: 'Sonicle.webtop.core.admin.model.PecBridgeRelay',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			modelValidation: true,
			defaults: {
				labelWidth: 120
			},
			items: [
				WTF.localCombo('id', 'desc', {
					reference: 'flduser',
					bind: '{record.userId}',
					store: {
						model: 'WTA.model.Simple',
						proxy: WTF.proxy(WT.ID, 'LookupDomainUsers', 'users'),
						listeners: {
							beforeload: function(s,op) {
								WTU.applyExtraParams(op.getProxy(), {domainId: me.getModel().get('domainId')});
							}
						}
					},
					fieldLabel: me.mys.res('pecBridgeRelay.fld-userId.lbl'),
					anchor: '100%'
				}),
				{
					xtype: 'textfield',
					bind: '{record.pecAddress}',
					fieldLabel: me.mys.res('pecBridgeRelay.fld-pecAddress.lbl'),
					anchor: '100%'
				}, {
					xtype: 'fieldcontainer',
					layout: 'hbox',
					items: [{
						xtype: 'textfield',
						bind: '{record.host}',
						width: 160
					}, {
						xtype: 'displayfield',
						value: '&nbsp;:&nbsp;'
					}, {
						xtype: 'numberfield',
						bind: '{record.port}',
						hideTrigger: true,
						minValue: 1,
						maxValue: 65000,
						width: 70,
						emptyText: me.mys.res('pecBridgeRelay.fld-port.emp')
					}],
					fieldLabel: me.mys.res('pecBridgeRelay.fld-host.lbl')
				}, {
					xtype: 'textfield',
					bind: '{record.username}',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('pecBridgeRelay.fld-username.lbl'),
					width: 300
				}, {
					xtype: 'sopasswordfield',
					bind: '{record.password}',
					plugins: 'sonoautocomplete',
					fieldLabel: me.mys.res('pecBridgeRelay.fld-password.lbl'),
					width: 300
				},
				WTF.lookupCombo('id', 'desc', {
					bind: '{record.connSecurity}',
					allowBlank: false,
					store: Ext.create('Sonicle.webtop.core.admin.store.PecBridgeConnSecurity', {
						autoLoad: true
					}),
					fieldLabel: me.mys.res('pecBridgeRelay.fld-connSecurity.lbl'),
					width: 230
				})
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				flduser = me.lref('flduser');
		
		flduser.getStore().load();
		flduser.focus(true);
	}
});
