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
Ext.define('Sonicle.webtop.core.view.TagEditor', {
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.String',
		'Sonicle.form.field.Palette'
	],
	
	/**
	 * @cfg {new|edit} [mode=new]
	 */
	mode: 'new',
	
	/**
	 * @cfg {Boolean} [visibilityEditable=false]
	 * `true` to make editable/modifiable visibility options.
	 */
	visibilityEditable: false,
	
	/**
	 * @cfg {String[]} invalidNames
	 * List of names to use as invalid values in name field validation.
	 */
	
	/**
	 * @cfg {Object} [data]
	 * An object containing initial data values.
	 * 
	 * @cfg {String} [data.id] Value for `id` field.
	 * @cfg {String} [data.name] Value for `name` field.
	 * @cfg {String} [data.color] Value for `color` field.
	 * @cfg {String} [data.visibility] Value for `visibility` field.
	*/
	
	/**
	 * @event viewok
	 * Fires when view is confirmed
	 * @param {Sonicle.webtop.core.view.TagEditor} this This view.
	 * @param {Object} data An object containing final data values.
	 * 
	 * @param {String} data.id
	 * @param {String} data.name
	 * @param {String} data.color
	 * @param {String} data.visibility
	 */
	
	dockableConfig: {
		width: 280,
		height: 150,
		modal: true
	},
	
	viewModel: {
		data: {
			data: {
				id: null,
				name: null,
				color: null,
				visibility: null
			}
		}
	},
	defaultButton: 'btnok',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		me.setViewTitle(me.mode === 'edit' ? WT.res('tagEditor.edit.tit') : WT.res('tagEditor.new.tit'));
	},
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig(),
				vm = me.getVM();
		
		if (ic.data) vm.set('data', ic.data);
		WTU.applyFormulas(vm, {
			foVisibility: WTF.radioGroupBind(null, 'data.visibility', me.getId()+'-visibility')
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			bodyPadding: 10,
			items: [
				{
					xtype: 'fieldcontainer',
					layout: {
						type: 'hbox',
						padding: '0 0 1 0' // fixes classic-theme bottom border issue
					},
					items: [
						{
							xtype: 'sopalettefield',
							bind: '{data.color}',
							hideTrigger: true,
							colors: WT.getColorPalette('default'),
							tilesPerRow: 11,
							margin: '0 10 0 0',
							width: 24
						}, {
							xtype: 'textfield',
							reference: 'fldname',
							bind: '{data.name}',
							allowBlank: false,
							validator: function(v) {
								return Ext.isArray(me.invalidNames) ? me.invalidNames.indexOf(v) === -1 : true;
							},
							maxLength: 50,
							flex: 1
						}
					],
					hideLabel: true
				}, {
					xtype: 'radiogroup',
					bind: {
						value: '{foVisibility}'
					},
					disabled: !me.visibilityEditable,
					columns: 2,
					defaults: {
						name: me.getId()+'-visibility'
					},
					items: [
						{inputValue: 'private', boxLabel: me.mys.res('tagEditor.fld-visibility.private')},
						{inputValue: 'shared', boxLabel: me.mys.res('tagEditor.fld-visibility.shared')}
					],
					fieldLabel: me.mys.res('tagEditor.fld-visibility.lbl'),
					labelWidth: 80
				}
			],
			buttons: [
				{
					reference: 'btnok',
					formBind: true,
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
		me.on('viewshow', me.onViewShow);
	},
	
	okView: function() {
		var me = this,
				vm = me.getVM();
		me.fireEvent('viewok', me, vm.get('data'));
		me.closeView(false);
	},
	
	privates: {
		onViewShow: function(s) {
			this.lref('fldname').focus(true);
		}
	}
});
