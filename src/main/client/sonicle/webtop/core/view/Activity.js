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
	extend: 'WT.sdk.ModelView',
	requires: [
		//'Ext.ux.form.trigger.Clear'
	],
	
	title: '@activity.tit',
	iconCls: 'wt-icon-activity-xs',
	model: 'Sonicle.webtop.core.model.Activity',
	viewModel: {
		formulas: {
			readOnly: WTF.checkboxBind('record', 'readOnly')
		}
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add(me.addRef('main', Ext.create({
			region: 'center',
			xtype: 'form',
			layout: 'anchor',
			modelValidation: true,
			bodyPadding: 5,
			defaults: {
				labelWidth: 100
			},
			items: [{
				xtype: 'combo',
				bind: '{record.userId}',
				typeAhead: true,
				queryMode: 'local',
				forceSelection: true,
				selectOnFocus: true,
				store: {
					autoLoad: true,
					model: 'WT.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupUsers', 'users', {
						extraParams: {wildcard: true}
					})
				},
				valueField: 'id',
				displayField: 'desc',
				fieldLabel: me.mys.res('event.fld-userId.lbl')
			}, {
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: me.mys.res('calendar.fld-description.lbl'),
				anchor: '100%'
			}, {
				xtype: 'checkbox',
				bind: '{readOnly}',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('calendar.fld-readOnly.lbl')
			}]
		})));
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				main = me.getRef('main');
		
		main.getComponent('domainId').focus(true);
	}
});
