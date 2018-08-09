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
Ext.define('Sonicle.webtop.core.ux.grid.plugin.RecipientCellEditing', {
	alternateClassName: 'WTA.ux.grid.plugin.RecipientCellEditing',
	extend: 'Ext.grid.plugin.CellEditing',
	alias: 'plugin.wtrcptcellediting',
	uses: [
		'WTA.ux.grid.RecipientCellEditor'
	],
	
	getNavPosition: function(nav) {
		var pos = nav.getPosition();
		if (pos) {
			return pos;
		} else {
			pos = nav.previousPosition;
			nav.setPosition(pos);
			return pos;
		}
		//var ppos = nav.previousPosition;
		//if (ppos && ppos.rowIdx) return ppos;
		//return nav.getPosition();
	},
	
	getGridColDataIndex: function(grid, colIdx) {
		var col = grid.getColumnManager().getHeaderAtIndex(colIdx);
		return col ? col.dataIndex : null;
	},
	
	startEditUp: function(event) {
		var me = this,
				nav = me.view.getNavigationModel(),
				pos = me.getNavPosition(nav),
				newPos;
		
		if (pos) {
			if (pos.rowIdx > 0) {
				newPos = nav.move('up', event);
				if (newPos) {
					nav.setPosition(newPos, null, event);
					me.startEditByPosition({row: newPos.rowIdx, column: 1});
					return true;
				}
			} else {
				me.startEditByPosition({row: pos.rowIdx, column: 1});
				return true;
			}
		}
		return false;
	},
	
	startEditDown: function(event, stopExitFocus) {
		if (stopExitFocus === undefined) stopExitFocus = false;
		var me = this,
				nav = me.view.getNavigationModel(),
				grid = me.view.grid,
				maxIdx = me.view.store.getCount()-1,
				pos = me.getNavPosition(nav),
				newPos;
		
		if (pos) {
			if ((pos.colIdx === 0) && Ext.isEmpty(pos.record.get(me.getGridColDataIndex(grid, 1)))) {
				newPos = nav.move('right', event);
				if (newPos) {
					nav.setPosition(newPos, null, event);
					me.startEditByPosition({row: newPos.rowIdx, column: 1});
				}
			} else {
				if (pos.rowIdx < maxIdx) {
					newPos = nav.move('down', event);
					if (newPos) {
						nav.setPosition(newPos, null, event);
						me.startEditByPosition({row: newPos.rowIdx, column: 1});
						return true;
					}
				} else {
					me.completeEdit();
					if (Ext.isEmpty(pos.record.get(me.getGridColDataIndex(grid, 1)))) {
						if (!stopExitFocus) me.view.grid.fireExitFocus();
					} else {
						grid.addRecipient(pos.record.get(me.getGridColDataIndex(grid, 0)), '');
						nav.setPosition(pos.rowIdx+1, 1, event, true, true);
						me.startEditByPosition({row: pos.rowIdx+1, column: 1});
						return true;
					}
				}
			}	
		}
		return false;
	},
	
	/**
	 * Overrides default implementation of {@link Ext.grid.plugin.CellEditing#getEditor} 
	 * in order to instantiate editor of type {@link Sonicle.webtop.core.ux.grid.RecipientCellEditor}. 
	 */
	getEditor: function (record, column) {
		var me = this,
				editors = me.editors,
				editorId = column.getItemId(),
				editor = editors.getByKey(editorId);

		if (!editor) {
			editor = column.getEditor(record);
			if (!editor) {
				return false;
			}

			// Allow them to specify a CellEditor in the Column
			if (!(editor instanceof Ext.grid.CellEditor)) {
				// Apply the field's editorCfg to the CellEditor config.
				// See Editor#createColumnField. A Column's editor config may
				// be used to specify the CellEditor config if it contains a field property.
				editor = new WTA.ux.grid.RecipientCellEditor(Ext.apply({
					floating: true,
					editorId: editorId,
					field: editor
				}, editor.editorCfg));
			}

			// Add the Editor as a floating child of the grid
			// Prevent this field from being included in an Ext.form.Basic
			// collection, if the grid happens to be used inside a form
			editor.field.excludeForm = true;

			// If the editor is new to this grid, then add it to the grid, and ensure it tells us about its life cycle.
			if (editor.column !== column) {
				editor.column = column;
				column.on('removed', me.onColumnRemoved, me);
			}
			editors.add(editor);
		}

		// Inject an upward link to its owning grid even though it is not an added child.
		editor.ownerCmp = me.grid.ownerGrid;

		if (column.isTreeColumn) {
			editor.isForTree = column.isTreeColumn;
			editor.addCls(Ext.baseCSSPrefix + 'tree-cell-editor');
		}

		// Set the owning grid.
		// This needs to be kept up to date because in a Lockable assembly, an editor
		// needs to swap sides if the column is moved across.
		editor.setGrid(me.grid);

		// Keep upward pointer correct for each use - editors are shared between locking sides
		editor.editingPlugin = me;
		return editor;
	},
	
    fireCompleteEditEvent: function(rec) {
        var me=this;
	
		if (rec) me.fireEvent('completeEdit',rec);
    },
	
    fireStartEditEvent: function(rec) {
        var me=this;
	
		if (rec) me.fireEvent('startEdit',rec);
    }	
	
});
