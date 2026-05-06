/*
 * Copyright (C) 2026 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2026 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.view.AIReport', {
	extend: 'WTA.sdk.UIView',

	/**
	 * @cfg {Object} [data]
	 * Initial data values.
	 *
	 * @cfg {String} [data.cadence] One of `daily`, `weekly`, `monthly`.
	 * @cfg {String} [data.email] Destination email address.
	 */

	/**
	 * @event viewok
	 * Fires when the user confirms the dialog.
	 * @param {Sonicle.webtop.core.admin.view.AIReport} this
	 * @param {Object} data Final data values: `{cadence, email}`.
	 */

	/**
	 * @event viewtest
	 * Fires when the user clicks the Test button.
	 * @param {Sonicle.webtop.core.admin.view.AIReport} this
	 * @param {Object} data Current data values: `{cadence, email}`.
	 */

	dockableConfig: {
		title: '{aiReport.tit}',
		iconCls: 'wt-icon-generate',
		width: 360,
		height: 180,
		modal: true
	},
	promptConfirm: false,

	viewModel: {
		data: {
			data: {
				cadence: 'daily',
				email: null,
				current: null
			}
		}
	},
	defaultButton: 'btnok',

	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				vm = me.getVM();

		if (ic.data) vm.set('data', ic.data);
		me.callParent(arguments);

		var current = vm.get('data').current;
		me.add({
			region: 'center',
			xtype: 'wtform',
			bodyPadding: 10,
			items: [
				WTF.lookupCombo('id', 'desc', {
					hidden: current,
					bind: '{data.cadence}',
					store: {
						autoLoad: true,
						fields: ['id', 'desc'],
						data: [
							['none',    me.mys.res('aiReport.cadence.none')],
							['daily',   me.mys.res('aiReport.cadence.daily')],
							['weekly',  me.mys.res('aiReport.cadence.weekly')],
							['monthly', me.mys.res('aiReport.cadence.monthly')]
						]
					},
					editable: false,
					allowBlank: false,
					fieldLabel: me.mys.res('aiReport.fld-cadence.lbl'),
					anchor: '100%'
				}),
				{
					xtype: 'textfield',
					reference: 'fldemail',
					bind: '{data.email}',
					vtype: 'email',
					allowBlank: false,
					fieldLabel: me.mys.res('aiReport.fld-email.lbl'),
					anchor: '100%'
				}
			],
			buttons: [
				{
					ui: '{tertiary}',
					text: WT.res('act-cancel.lbl'),
					handler: function() {
						me.closeView(false);
					}
				}, {
					hidden: current,
					ui: '{secondary}',
					formBind: true,
					text: me.mys.res('aiReport.act-test.lbl'),
					handler: function() {
						me.testView();
					}
				}, {
					reference: 'btnok',
					ui: '{primary}',
					formBind: true,
					text: WT.res('act-ok.lbl'),
					handler: function() {
						me.okView();
					}
				}
			]
		});
		me.on('viewshow', me.onViewShow);
	},

	okView: function() {
		var me = this,
				vm = me.getVM();
		me.fireEvent('viewok', me, vm.get('data'));
		me.closeView(false);
	},

	testView: function() {
		var me = this,
				vm = me.getVM();
		me.fireEvent('viewtest', me, vm.get('data'));
	},

	privates: {
		onViewShow: function() {
			this.lref('fldemail').focus(true);
		}
	}
});
