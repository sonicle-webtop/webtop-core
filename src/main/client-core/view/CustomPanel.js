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
Ext.define('Sonicle.webtop.core.view.CustomPanel', {
	alternateClassName: 'WTA.view.CustomPanel',
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.String',
		'Sonicle.form.field.Tag',
		'Sonicle.grid.column.Action',
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.feature.RowLookup',
		'Sonicle.grid.plugin.DDOrdering',
		'Sonicle.picker.List',
		'WTA.ux.PickerWindow',
		'Sonicle.webtop.core.model.CustomFieldLkp',
		'Sonicle.webtop.core.model.CustomPanel'
	],
	
	dockableConfig: {
		title: '{customPanel.tit}',
		iconCls: 'wt-icon-customPanel',
		width: 450,
		height: 520
	},
	modelName: 'Sonicle.webtop.core.model.CustomPanel',
	fieldTitle: 'name',
	
	/**
	 * @cfg {String} serviceId
	 * Target service ID for which managing fields.
	 */
	serviceId: null,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			foMainTitle: WTF.foRecordTwoWay('record', 'titleI18n', 'txt', WT.getLanguage()),
			foTags: WTF.foTwoWay('record', 'tags', function(v) {
					return Sonicle.String.split(v, '|');
				}, function(v) {
					return Sonicle.String.join('|', v);
			})
		});
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		me.callParent(arguments);
		
		me.lookupStore = Ext.create('Ext.data.JsonStore', {
			autoLoad: true,
			model: 'Sonicle.webtop.core.model.CustomFieldLkp',
			proxy: WTF.apiProxy(me.mys.ID, 'LookupCustomFields', null, {
				extraParams: {
					serviceId: me.serviceId
				}
			})
		});
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				{
					xtype: 'wtfieldspanel',
					reference: 'pnlmain',
					modelValidation: true,
					defaults: {
						labelWidth: 120
					},
					items: [
						{
							xtype: 'textfield',
							reference: 'fldname',
							bind: '{record.name}',
							fieldLabel: me.res('customPanel.fld-name.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textareafield',
							bind: '{record.description}',
							fieldLabel: me.res('customPanel.fld-description.lbl'),
							anchor: '100%'
						}, {
							xtype: 'textfield',
							bind: '{foMainTitle}',
							fieldLabel: me.res('customPanel.fld-title.lbl', WT.getLanguage()),
							anchor: '100%'
						}, {
							xtype: 'sotagfield',
							bind: '{foTags}',
							store: WT.getTagsStore(),
							valueField: 'id',
							displayField: 'name',
							colorField: 'color',
							createNewOnEnter: false,
							createNewOnBlur: false,
							filterPickList: true,
							forceSelection: true,
							fieldLabel: me.res('customPanel.fld-tags.lbl'),
							emptyText: me.res('customPanel.fld-tags.emp'),
							anchor: '100%'
						}
					]
				}, {
					xtype: 'wttabpanel',
					reference: 'tabmain',
					items: [
						{
							xtype: 'gridpanel',
							title: me.mys.res('customPanel.fields.tit'),
							border: false,
							bind: '{record.assocFields}',
							viewConfig: {
								deferEmptyText: false,
								emptyText: WT.res('grid.emp'),
								plugins: [
									{
										ptype: 'sogridviewddordering',
										orderField: 'order'
									}
								]
							},
							features: [
								{
									ftype: 'sorowlookup',
									lookupStore: me.lookupStore
								}
							],
							columns: [
								{
									xtype: 'rownumberer'
								}, {
									xtype: 'soiconcolumn',
									dataIndex: 'id',
									hideText: false,
									getText: function(val, rec) {
										return rec.lookupRecord ? rec.lookupRecord.get('type') : val;
									},
									getIconCls: function(val, rec) {
										return rec.lookupRecord ? me.mys.cssIconCls('customField-'+rec.lookupRecord.get('type')) : val;
									},
									iconSize: WTU.imgSizeToPx('xs'),
									header: me.res('customPanel.gp-fields.type.lbl'),
									flex: 1
								}, {
									dataIndex: 'id',
									header: me.res('customPanel.gp-fields.name.lbl'),
									renderer: function(val, meta, rec) {
										return rec.lookupRecord ? rec.lookupRecord.get('name') : val;
									},
									flex: 1
								}, {
									xtype: 'soactioncolumn',
									items: [
										{
											iconCls: 'fa fa-trash',
											tooltip: WT.res('act-remove.lbl'),
											handler: function(g, ridx) {
												var rec = g.getStore().getAt(ridx);
												me.getModel().assocFields().remove(rec);
											}
										}
									]

								}
							],
							tbar: [
								me.addAct('add', {
									text: WT.res('act-add.lbl'),
									tooltip: null,
									iconCls: 'wt-icon-add-xs',
									handler: function() {
										me.showFieldPicker();
									}
								})
							]
						}
					],
					flex: 1
				}
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	privates: {
		onViewLoad: function(s, success) {
			var me = this,
					mo = me.getModel();

			me.lref('fldname').focus(true);
		},
		
		onFieldPickerPick: function(s, vals, recs) {
			var me = this, 
					fsto = me.getModel().assocFields();
			
			fsto.add({
				id: vals[0],
				order: fsto.getCount()
			});
			me.fieldPicker.close();
			me.fieldPicker = null;
		},
		
		showFieldPicker: function() {
			var me = this;
			me.fieldPicker = me.createFieldPicker();
			me.fieldPicker.show();
		},
		
		createFieldPicker: function() {
			var me = this;
			return Ext.create({
				xtype: 'wtpickerwindow',
				title: me.res('customPanel.fieldPicker.tit'),
				height: 350,
				items: [{
					xtype: 'solistpicker',
					store: me.lookupStore,
					valueField: 'id',
					displayField: 'name',
					searchField: 'name',
					emptyText: WT.res('grid.emp'),
					searchText: WT.res('textfield.search.emp'),
					okText: WT.res('act-ok.lbl'),
					cancelText: WT.res('act-cancel.lbl'),
					listeners: {
						cancelclick: function() {
							if (me.fieldPicker) me.fieldPicker.close();
						}
					},
					handler: me.onFieldPickerPick,
					scope: me
				}]
			});
		}
	}
});
