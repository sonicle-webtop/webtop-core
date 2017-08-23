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
Ext.define('Sonicle.webtop.core.ux.grid.Setting', {
	alternateClassName: 'WTA.ux.grid.Setting',
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtsettinggrid',
	
	requires: [
		'WTA.ux.grid.SettingHeaderContainer'
	],
	uses: [
		'Ext.grid.plugin.CellEditing',
		'Ext.XTemplate',
		'Ext.grid.CellEditor',
		'Ext.form.field.Date',
		'Ext.form.field.Text',
		'Ext.form.field.Number',
		'Ext.form.field.ComboBox'
	],
	
	groupField: 'serviceId',
	keyField: 'key',
	valueField: 'value',
	typeField: 'type',
	helpField: 'help',
	
	groupText: null,
	keyText: null,
	valueText: null,
	typeText: null,
	
	clicksToEdit: 2,
	
	gridCls: 'wt-setting-grid',
	
	initComponent: function() {
		var me = this;
		
		me.groupText = me.groupText || WT.res('wtsettinggrid.group.lbl');
		me.keyText = me.keyText || WT.res('wtsettinggrid.key.lbl');
		me.valueText = me.valueText || WT.res('wtsettinggrid.value.lbl');
		me.typeText = me.typeText || WT.res('wtsettinggrid.type.lbl');
		
		me.addCls(me.gridCls);
		
		me.plugins = me.plugins || [];
		me.plugins.push({
			ptype: 'cellediting',
			clicksToEdit: me.clicksToEdit
		});
		
		me.features = me.features || [];
		me.features.push({
			ftype:'grouping'
		});
		
		me.selModel = {
			type: 'rowmodel',
			onCellSelect: function(position) {
				position.column = me.valueColumn;
				position.colIdx = me.valueColumn.getVisibleIndex();
				return this.self.prototype.onCellSelect.call(this, position);
			}
		};
		
		me.columns = new WTA.ux.grid.SettingHeaderContainer(me, me.store);
		
		me.dockedItems = me.dockedItems || [];
		me.dockedItems.push({
			dock: 'bottom',
			xtype: 'panel',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			itemId: 'details',
			height: 150,
			bodyCls: 'x-panel-header-default',
			bodyPadding: '5 10',
			items: [{
				xtype: 'displayfield',
				itemId: 'title',
				hideEmptyLabel: true
			}, {
				xtype: 'textarea',
				itemId: 'text',
				editable: false,
				hideEmptyLabel: true,
				fieldStyle: 'background:none;',
				flex: 1,
				listeners: {
					afterrender: function(s) {
						s.triggerWrap.setStyle({border: 'none'});
					}
				}
			}]
		});
		
		me.callParent();
		
		// Inject a custom implementation of walkCells which only goes up or down
		me.getView().walkCells = this.walkCells;
		
		// Set up our default editor set for the some data types
		me.editors = {
			'string': new Ext.grid.CellEditor({field: new Ext.form.field.Text({selectOnFocus: true})}),
			'boolean' : new Ext.grid.CellEditor({field: new Ext.form.field.ComboBox({
				editable: false,
				store: [[true, me.headerCt.trueText], [false, me.headerCt.falseText]]
			})}),
			'number': new Ext.grid.CellEditor({field: new Ext.form.field.Number({
					selectOnFocus: true,
					allowDecimals: true
			})}),
			'integer': new Ext.grid.CellEditor({field: new Ext.form.field.Number({
					selectOnFocus: true,
					allowDecimals: false
			})}),
			'date': new Ext.grid.CellEditor({field: new Ext.form.field.Date({selectOnFocus: true})})
		};
	},
	
	beforeDestroy: function() {
		var me = this;
		me.callParent();
		me.destroyEditors(me.editors);
	},
	
	bindStore: function(store, initial) {
		var me = this;
		me.callParent(arguments);
		if(store) {
			me.mon(store, 'update', me.onStoreRecUpdate, me);
		}
	},
	
	onStoreRecUpdate: function(s, rec) {
		var sel = this.getSelection();
		this.updateDetails((sel.length === 1) ? sel[0] : null);
	},
	
	updateBindSelection: function(selModel, selection) {
		var me = this;
		me.callParent(arguments);
		me.updateDetails((selection.length === 1) ? selection[0] : null);
	},
	
	updateDetails: function(rec) {
		var me = this,
				details = me.getDockedComponent('details'),
				title, type, help;
		if(rec) {
			type = rec.get(me.typeField);
			help = rec.get(me.helpField);
			title = '<b>' + rec.get(me.keyField) + '</b>';
			if(!Ext.isEmpty(type)) title += ' :: ' + type;
		}
		details.getComponent('title').setValue(title);
		details.getComponent('text').setValue(help);
	},
	
	// Custom implementation of walkCells which only goes up and down. 
	// Runs in the scope of the TableView 
	walkCells: function (pos, direction, e, preventWrap, verifierFn, scope) {
		var me = this,
				valueColumn = me.ownerCt.valueColumn;
		
		if (direction === 'left') {
			direction = 'up';
		}
		pos = Ext.view.Table.prototype.walkCells.call(me, pos, direction, e, preventWrap, verifierFn, scope);

		// We are only allowed to navigate to the value column.
		pos.column = valueColumn;
		pos.colIdx = valueColumn.getVisibleIndex();
		return pos;
	},
	
	getCellEditor: function(rec, col) {
		var me = this,
				key = rec.get(me.keyField),
				type = rec.get(me.typeField),
				editors = me.editors,
				ed, fld;
		
		switch(type) {
			case 'boolean':
				ed = me.editors['boolean'];
				break;
			case 'number':
				ed = me.editors['number'];
				break;
			case 'integer':
				ed = me.editors['integer'];
				break;
			case 'date':
				ed = me.editors['date'];
				break;
			default:
				ed = editors['string'];
		}
		
		fld = ed.field;
		if (fld && fld.ui === 'default' && !fld.hasOwnProperty('ui')) {
			fld.ui = me.editingPlugin.defaultFieldUI;	
		}
		
		// Give the editor a unique ID because the CellEditing plugin caches them
		ed.editorId = key;
		ed.field.column = me.valueColumn;
		return ed;
	},
	
	destroyEditors: function(editors) {
		for(var ed in editors) {
			if(editors.hasOwnProperty(ed)) Ext.destroy(editors[ed]);
		}
	}
});
