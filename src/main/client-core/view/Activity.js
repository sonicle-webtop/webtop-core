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
Ext.define('Sonicle.webtop.core.view.Activity', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.webtop.core.model.Activity'
	],
	
	dockableConfig: {
		title: '{activity.tit}',
		iconCls: 'wt-icon-activity',
		width: 430,
		height: 230
	},
	modelName: 'Sonicle.webtop.core.model.Activity',
	viewModel: {
		formulas: {
			readOnly: WTF.checkboxBind('record', 'readOnly')
		}
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			layout: 'anchor',
			modelValidation: true,
			items: [{
					xtype: 'textfield',
					reference: 'flddescription',
					bind: '{record.description}',
					fieldLabel: me.mys.res('activity.fld-description.lbl'),
					anchor: '100%'
				}, {
					xtype: 'textfield',
					bind: '{record.externalId}',
					fieldLabel: me.mys.res('activity.fld-externalId.lbl'),
					width: 250
				}, {
					xtype: 'checkbox',
					bind: '{readOnly}',
					hideEmptyLabel: false,
					boxLabel: me.mys.res('activity.fld-readOnly.lbl')
				},
				WTF.localCombo('id', 'desc', {
					reference: 'user',
					bind: '{record.userId}',
					store: {
						autoLoad: true,
						model: 'WTA.model.Simple',
						proxy: WTF.proxy(me.mys.ID, 'LookupDomainUsers', 'users', {
							extraParams: {wildcard: true}
						})
					},
					fieldLabel: me.mys.res('activity.fld-user.lbl'),
					anchor: '100%'
				}),
				me.mys.hasAudit() ? {
					xtype: 'statusbar',
					items: [
						me.addAct('activityAuditLog', {
							text: null,
							tooltip: WT.res('act-auditLog.lbl'),
							iconCls: 'fas fa-history',
							handler: function() {
								me.mys.openAuditUI(me.getModel().getId(), 'ACTIVITY');
							},
							scope: me
						})
					]
				} : null
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		
		if (me.mys.hasAudit()) {
			if (me.isMode(me.MODE_NEW)) {
				me.getAct('activityAuditLog').setDisabled(true);
			} else {
				me.getAct('activityAuditLog').setDisabled(false);
			}
		}
		
		me.lref('flddescription').focus(true);
	}
});
