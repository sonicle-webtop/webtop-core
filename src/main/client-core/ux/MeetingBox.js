/* 
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.MeetingBox', {
	extend: 'WTA.ux.window.CustomPromptMsgBox',
	requires: [
		'Sonicle.Utils',
		'Sonicle.webtop.core.ux.panel.Fields'
	],
	
	hideWhat: false,
	hideDateTime: false,
	defaultFocus: 'what',
	startDay: 1,
	dateFormat: 'Y-m-d',
	timeFormat: 'H:i',
	whatText: '',
	whenText: '',
	nowTooltip: '',
	
	constructor: function(cfg) {
		var me = this,
			icfg = Sonicle.Utils.getConstructorConfigs(me, cfg, [
				{hideWhat: true, hideDateTime: true}
			]);
		if (icfg.hideWhat === true && icfg.hideDateTime === false) cfg.defaultFocus = 'date';
		me.callParent([cfg]);
	},
	
	createCustomPrompt: function() {
		var me = this;
		return {
			xtype: 'wtfieldspanel',
			bodyCls: 'wt-theme-dialog-bg',
			referenceHolder: true,
			defaults: {
				labelAlign: 'top'
			},
			items: [
				{
					xtype: 'textfield',
					itemId: 'what',
					reference: 'what',
					hidden: me.hideWhat,
					enableKeyEvents: true,
					listeners: {
						keydown: me.onPromptKey,
						scope: me
					},
					fieldLabel: me.whatText,
					anchor: '100%'
				}, {
					xtype: 'fieldcontainer',
					hidden: me.hideDateTime,
					layout: 'hbox',
					items: [
						{
							xtype: 'datefield',
							reference: 'date',
							startDay: me.startDay,
							format: me.dateFormat,
							enableKeyEvents: true,
							listeners: {
								keydown: me.onPromptKey,
								scope: me
							},
							margin: '0 5 0 0',
							maxWidth: 120,
							flex: 1
						}, {
							xtype: 'timefield',
							reference: 'time',
							format: me.timeFormat,
							enableKeyEvents: true,
							listeners: {
								keydown: me.onPromptKey,
								scope: me
							},
							margin: '0 5 0 0',
							maxWidth: 90,
							flex: 1
						}, {
							xtype: 'button',
							ui: 'default-toolbar',
							iconCls: 'far fa-clock',
							tooltip: me.nowTooltip,
							handler: function() {
								me.customPrompt.lookupReference('date').setValue(new Date());
								me.customPrompt.lookupReference('time').setValue(new Date());
							}
						}
					],
					fieldLabel: me.whenText,
					anchor: '100%'
				},
				WTF.localCombo('id', 'desc', {
					reference: 'timezone',
					hidden: me.hideDateTime,
					store: {
						type: 'wttimezone',
						autoLoad: true
					},
					enableKeyEvents: true,
					listeners: {
						keydown: me.onPromptKey,
						scope: me
					},
					anchor: '100%'
				})
			],
			width: 300
		};
	},
	
	setCustomPromptValue: function(value) {
		var SoD = Sonicle.Date,
				cp = this.customPrompt;
		if (Ext.isArray(value) && value.length === 3) {
			cp.lookupReference('what').setValue(value[0]);
			cp.lookupReference('date').setValue(SoD.clone(value[1]));
			cp.lookupReference('time').setValue(SoD.clone(value[1]));
			cp.lookupReference('timezone').setValue(value[2]);
		}
	},
	
	getCustomPromptValue: function() {
		var SoD = Sonicle.Date,
				cp = this.customPrompt,
				date = cp.lookupReference('date').getValue(),
				time = cp.lookupReference('time').getValue();
		return [
			cp.lookupReference('what').getValue(),
			(!Ext.isDate(date) || !Ext.isDate(time)) ? null : SoD.copyTime(time, date),
			cp.lookupReference('timezone').getValue()
		];
	}
});
