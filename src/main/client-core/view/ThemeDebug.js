/* 
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.view.ThemeDebug', {
	extend: 'WTA.sdk.UIView',
	requires: [
		
	],
	
	dockableConfig: {
		title: 'themeDebug',
		iconCls: 'wt-icon-themeDebug',
		width: 800,
		height: 600
	},
	
	initComponent: function() {
		var me = this, gridStore;
		me.callParent(arguments);
		
		gridStore = Ext.create('Ext.data.Store', {
			fields: ['name', 'email', 'phone'],
			data: [
				{"id":1,"name":"Louise Campbell","email":"lcampbell0@google.com.au","phone":"1-(253)731-6972"},
				{"id":2,"name":"Katherine Berry","email":"kberry1@cnet.com","phone":"1-(206)238-5084"},
				{"id":3,"name":"Tina Simpson","email":"tsimpson2@businessweek.com","phone":"1-(206)249-1158"},
				{"id":4,"name":"Sandra Martinez","email":"smartinez3@exblog.jp","phone":"1-(661)502-3234"},
				{"id":5,"name":"Maria Dean","email":"mdean4@themeforest.net","phone":"1-(434)285-2720"},
				{"id":6,"name":"Cynthia Garrett","email":"cgarrett5@yahoo.com","phone":"1-(303)139-0986"},
				{"id":7,"name":"Todd Franklin","email":"tfranklin6@fc2.com","phone":"1-(202)564-3767"},
				{"id":8,"name":"Jeffrey Ortiz","email":"jortiz7@umich.edu","phone":"1-(773)646-9674"},
				{"id":9,"name":"Christina Davis","email":"cdavis8@si.edu","phone":"1-(323)620-7506"},
				{"id":10,"name":"Jonathan Mendoza","email":"jmendoza9@mlb.com","phone":"1-(469)116-3306"},
				{"id":11,"name":"Nicholas Stewart","email":"nstewarta@liveinternet.ru","phone":"1-(281)882-1231"},
				{"id":12,"name":"Louise Garza","email":"lgarzab@omniture.com","phone":"1-(760)976-0159"},
				{"id":13,"name":"Christina White","email":"cwhitec@ted.com","phone":"1-(763)781-3468"},
				{"id":14,"name":"Bonnie Frazier","email":"bfrazierd@google.ru","phone":"1-(312)956-7742"},
				{"id":15,"name":"Karen Gibson","email":"kgibsone@google.pl","phone":"1-(224)554-6193"},
				{"id":16,"name":"Thomas Dunn","email":"tdunnf@slate.com","phone":"1-(901)186-2985"},
				{"id":17,"name":"Bruce Harrison","email":"bharrisong@bizjournals.com","phone":"1-(954)320-8527"},
				{"id":18,"name":"Antonio Clark","email":"aclarkh@fema.gov","phone":"1-(616)572-0605"},
				{"id":19,"name":"Clarence Flores","email":"cfloresi@illinois.edu","phone":"1-(419)923-2800"},
				{"id":20,"name":"Lawrence Mason","email":"lmasonj@github.com","phone":"1-(775)196-5877"}
			]
		});
		
		me.add({
			region: 'west',
			xtype: 'treepanel',
			title: 'Tree',
			animate: true,
			split: true,
			collapsible: true,
			store: Ext.create('Ext.data.TreeStore', {
				root: {
					text: 'Simpsons',
					expanded: true,
					children: [
						{text: 'Lisa', leaf: true},
						{text: 'Bart', leaf: true},
						{text: 'Homer', leaf: true},
						{text: 'Marge', leaf: true}
					]
				}
			}),
			width: 150
		});
		me.add({
			region: 'center',
			xtype: 'wttabpanel',
			activeTab: 0,
			deferredRender: false,
			items: [
				{
					title: 'Tab 1',
					iconCls: 'fa-solid fa-table',
					layout: {
						type: 'border'
					},
					items: [
						{
							xtype: 'grid',
							region: 'center',
							flex: 1,
							store: gridStore,
							height: 450,
							columns: [
								{text: 'Name', dataIndex: 'name', flex: 2},
								{text: 'Email', dataIndex: 'email', flex: 3},
								{text: 'Phone', dataIndex: 'phone', width: 130}
							],
							dockedItems: [
								{
									xtype: 'toolbar',
									items: [
										{
											xtype: 'button',
											text: 'Menu',
											iconCls: 'fa-solid fa-bars',
											menu: [{
												text: 'Option 1'
											}, {
												text: 'Option 2'
											}]
										}, {
											xtype: 'tbseparator'
										}, {
											xtype: 'segmentedbutton',
											items: [{
												text: 'One',
												enableToggle: true
											}, {
												text: 'Two',
												enableToggle: true
											}, {
												text: 'Three',
												enableToggle: true
											}]
										}, {
											xtype: 'button',
											text: 'Four'
										}, {
											xtype: 'tbspacer',
											flex: 1
										}, {
											xtype: 'textfield',
											emptyText: 'Search...'
										}
									]
								}, {
									xtype: 'pagingtoolbar',
									store: gridStore,
									dock: 'bottom'
								}
							]
						}, {
							flex: 1,
							region: 'south',
							split: true,
							collapsible: true,
							bodyPadding: 10,
							title: 'Form',
							layout: 'hbox',
							items: [
								{
									xtype: 'container',
									layout: 'anchor',
									defaults: {
										labelAlign: 'right',
										labelWidth: 80
									},
									items: [
										{
											xtype: 'textfield',
											fieldLabel: 'Text Field',
											value: 'Lorem Ipsum'
										}, {
											xtype: 'combo',
											fieldLabel: 'Combo Box',
											forceSelection: true,
											value: 'Choice One',
											editable: false,
											store: ['Choice One', 'Choice Two', 'Choice Three']
										}, {
											xtype: 'datefield',
											fieldLabel: 'Date',
											value: new Date()
										}, {
											xtype: 'button',
											margin: '0 0 0 85',
											text: 'Button'
										}
									]
								}, {
									xtype: 'container',
									layout: 'anchor',
									defaults: {
										labelAlign: 'right',
										labelWidth: 80
									},
									items: [
										{
											xtype: 'checkbox',
											fieldLabel: 'Checkbox',
											checked: true,
											boxLabel: 'Enabled'
										}, {
											xtype: 'radiogroup',
											fieldLabel: 'Radio',
											items: [
												{
													boxLabel: 'One',
													checked: true,
													margin: '0 10 0 0'
												}, {
													boxLabel: 'Two'
												}
											]
										}, {
											xtype: 'numberfield',
											fieldLabel: 'Number',
											minValue: 0,
											maxValue: 100,
											step: 1,
											value: 1,
											allowDecimals: 0
										}, {
											xtype: 'displayfield',
											fieldLabel: 'Link',
											value: '<a href="#">This is a link</a>'
										}
									]
								}
							]
						}
					]
				}, {
					title: 'Tab 2',
					bodyPadding: 10,
					html: 'There is a theory which states that if ever anyone discovers exactly what the Universe is for and why it is here, it will instantly disappear and be replaced by something even more bizarre and inexplicable. There is another theory which states that this has already happened.'
				}, {
					title: 'Tab 3',
					bodyPadding: 10,
					html: 'Nothing to see here.'
				}, {
					title: 'Tab Buttons',
					bodyPadding: 10,
					items: [
						{
							xtype: 'container',
							layout: 'anchor',
							defaults: {
								labelWidth: 150
							},
							items: [
								{
									xtype: 'fieldcontainer',
									layout: 'hbox',
									defaults: {
										margin: '0 0 0 10'
									},
									items: [
										{
											xtype: 'button',
											text: 'normal',
											iconCls: 'fa-solid fa-star'
										}, {
											xtype: 'button',
											text: 'pressed',
											iconCls: 'fa-solid fa-star',
											pressed: true
										}, {
											xtype: 'button',
											text: 'disabled',
											iconCls: 'fa-solid fa-star',
											disabled: true
										}, {
											xtype: 'button',
											text: 'arrow',
											arrowVisible: true,
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}, {
											xtype: 'splitbutton',
											text: 'split',
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}
									],
									fieldLabel: 'default',
									anchor: '100%'
								}, {
									xtype: 'fieldcontainer',
									layout: 'hbox',
									defaults: {
										margin: '0 0 0 10'
									},
									items: [
										{
											xtype: 'toolbar',
											border: false,
											items: [
												{
													xtype: 'button',
													text: 'normal',
													iconCls: 'fa-solid fa-star'
												}, {
													xtype: 'button',
													text: 'pressed',
													iconCls: 'fa-solid fa-star',
													pressed: true
												}, {
													xtype: 'button',
													text: 'disabled',
													iconCls: 'fa-solid fa-star',
													disabled: true
												}, {
													xtype: 'button',
													text: 'arrow',
													arrowVisible: true,
													menu: {
														items: [
															{text: 'Item 1'},
															{text: 'Item 2'}
														]
													}
												}, {
													xtype: 'splitbutton',
													text: 'split',
													menu: {
														items: [
															{text: 'Item 1'},
															{text: 'Item 2'}
														]
													}
												}
											]
										}	
									],
									fieldLabel: 'toolbar',
									anchor: '100%'
								}, {
									xtype: 'fieldcontainer',
									layout: 'hbox',
									defaults: {
										ui: '{primary}',
										margin: '0 0 0 10'
									},
									items: [
										{
											xtype: 'button',
											text: 'normal',
											iconCls: 'fa-solid fa-star'
										}, {
											xtype: 'button',
											text: 'pressed',
											iconCls: 'fa-solid fa-star',
											pressed: true
										}, {
											xtype: 'button',
											text: 'disabled',
											iconCls: 'fa-solid fa-star',
											disabled: true
										}, {
											xtype: 'button',
											text: 'arrow',
											arrowVisible: true,
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}, {
											xtype: 'splitbutton',
											text: 'split',
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}
									],
									fieldLabel: 'primary',
									anchor: '100%'
								}, {
									xtype: 'fieldcontainer',
									layout: 'hbox',
									defaults: {
										ui: '{secondary}',
										margin: '0 0 0 10'
									},
									items: [
										{
											xtype: 'button',
											text: 'normal',
											iconCls: 'fa-solid fa-star'
										}, {
											xtype: 'button',
											text: 'pressed',
											iconCls: 'fa-solid fa-star',
											pressed: true
										}, {
											xtype: 'button',
											text: 'disabled',
											iconCls: 'fa-solid fa-star',
											disabled: true
										}, {
											xtype: 'button',
											text: 'arrow',
											arrowVisible: true,
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}, {
											xtype: 'splitbutton',
											text: 'split',
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}
									],
									fieldLabel: 'secondary',
									anchor: '100%'
								}, {
									xtype: 'fieldcontainer',
									layout: 'hbox',
									defaults: {
										ui: '{tertiary}',
										margin: '0 0 0 10'
									},
									items: [
										{
											xtype: 'button',
											text: 'normal',
											iconCls: 'fa-solid fa-star'
										}, {
											xtype: 'button',
											text: 'pressed',
											iconCls: 'fa-solid fa-star',
											pressed: true
										}, {
											xtype: 'button',
											text: 'disabled',
											iconCls: 'fa-solid fa-star',
											disabled: true
										}, {
											xtype: 'button',
											text: 'arrow',
											arrowVisible: true,
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}, {
											xtype: 'splitbutton',
											text: 'split',
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}
									],
									fieldLabel: 'tertiary',
									anchor: '100%'
								}, /*{
									xtype: 'fieldcontainer',
									layout: 'hbox',
									defaults: {
										ui: '{icon}',
										margin: '0 0 0 10'
									},
									items: [
										{
											xtype: 'button',
											iconCls: 'fa-solid fa-star'
										}, {
											xtype: 'button',
											iconCls: 'fa-solid fa-star',
											pressed: true
										}, {
											xtype: 'button',
											iconCls: 'fa-solid fa-star',
											disabled: true
										}
									],
									fieldLabel: 'icon',
									anchor: '100%'
								},*/ {
									xtype: 'fieldcontainer',
									layout: 'hbox',
									defaults: {
										ui: '{taskbar}',
										margin: '0 0 0 10'
									},
									items: [
										{
											xtype: 'button',
											iconCls: 'fa-solid fa-star',
											text: 'normal'
										}, {
											xtype: 'button',
											text: 'pressed',
											iconCls: 'fa-solid fa-star',
											pressed: true
										}, {
											xtype: 'button',
											text: 'disabled',
											iconCls: 'fa-solid fa-star',
											disabled: true
										}, {
											xtype: 'button',
											text: 'arrow',
											arrowVisible: true,
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}, {
											xtype: 'splitbutton',
											text: 'split',
											menu: {
												items: [
													{text: 'Item 1'},
													{text: 'Item 2'}
												]
											}
										}
									],
									fieldLabel: 'taskbar',
									anchor: '100%'
								}, {
									xtype: 'fieldcontainer',
									layout: 'hbox',
									items: [
										{
											xtype: 'segmentedbutton',
											defaults: {
												ui: '{segmented}'
											},
											items: [
												{
													text: 'normal 1'
												}, {
													text: 'normal 2'
												}, {
													text: 'normal 3'
												}, {
													text: 'disabled',
													disabled: true
												}
											]
										}
									],
									fieldLabel: 'segmented',
									anchor: '100%'
								}
							]
						}	
					]
				}
			]
		});
	}
});
