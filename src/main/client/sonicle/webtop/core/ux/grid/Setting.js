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
	alternateClassName: 'WT.ux.grid.Setting',
	extend: 'Ext.grid.Panel',
	alias: 'widget.wtsettinggrid',
	
	requires: [
		'WT.ux.grid.SettingHeaderContainer'
	],
	uses: [
		'Ext.grid.plugin.CellEditing',
		'Ext.grid.plugin.RowExpander',
		'Ext.XTemplate',
		'Ext.grid.CellEditor',
		'Ext.form.field.Date',
		'Ext.form.field.Text',
		'Ext.form.field.Number',
		'Ext.form.field.ComboBox'
	],
	
	groupField: 'serviceId',
	groupText: 'Servizio',
	keyField: 'key',
	keyText: 'Chiave',
	valueField: 'value',
	valueText: 'Valore',
	typeField: 'type',
	typeText: 'Tipo',
	helpField: 'help',
	clicksToEdit: 2,
	
	gridCls: 'wt-setting-grid',
	
	initComponent : function() {
		var me = this;
		
		me.addCls(me.gridCls);
		me.plugins = me.plugins || [];
		me.plugins.push({
			ptype: 'cellediting',
			clicksToEdit: me.clicksToEdit
		});
		
		me.plugins.push({
			ptype: 'rowexpander',
			expandOnEnter: false,
			expandOnDblClick: false,
			rowBodyTpl : new Ext.XTemplate(
					'<p><b>Tipo dato:</b> {'+me.typeField+'}</p>',
					'<p>{'+me.helpField+':this.formatHelp}</p>',
			{
				formatHelp: function(v) {
					return v;
				}
			})
		});
		
		me.features = me.features || [];
		me.features.push({
			ftype:'grouping',
			collapsible: false,
			collapseTip: null // Fix wrong tooltip display if collapsible=false
		});
		
		me.selModel = {
			type: 'rowmodel',
			onCellSelect: function(position) {
				position.column = me.valueColumn;
				position.colIdx = me.valueColumn.getVisibleIndex();
				return this.self.prototype.onCellSelect.call(this, position);
			}
		};
		
		me.columns = new WT.ux.grid.SettingHeaderContainer(me, me.store);
		me.callParent();
		
		// Inject a custom implementation of walkCells which only goes up or down
		me.getView().walkCells = this.walkCells;
		
		// Set up our default editor set for the 4 atomic data types 
		me.editors = {
			'string': new Ext.grid.CellEditor({field: new Ext.form.field.Text({selectOnFocus: true})}),
			'boolean' : new Ext.grid.CellEditor({field: new Ext.form.field.ComboBox({
				editable: false,
				store: [[true, me.headerCt.trueText], [false, me.headerCt.falseText]]
			})}),
			'number': new Ext.grid.CellEditor({field: new Ext.form.field.Number({selectOnFocus: true})}),
			'date': new Ext.grid.CellEditor({field: new Ext.form.field.Date({selectOnFocus: true})})
		};
	},
	
	beforeDestroy: function() {
		var me = this;
		me.callParent();
		me.destroyEditors(me.editors);
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
