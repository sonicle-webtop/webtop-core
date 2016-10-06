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
Ext.define('Sonicle.webtop.core.ux.grid.SettingHeaderContainer', {
	alternateClassName: 'WT.ux.grid.SettingHeaderContainer',
	extend: 'Ext.grid.header.Container',
	
	trueText: 'true',
	falseText: 'false',
	
	constructor :function(gp, sto) {
		var me = this;
		me.grid = gp;
		me.store = sto;
		me.callParent([{
			isRootHeader: true,
			items: [{
				itemId: gp.groupField,
				header: gp.groupText,
				dataIndex: gp.groupField,
				scope: me,
				sortable: false,
				menuDisabled: true,
				flex: 1
			}, {
				itemId: gp.keyField,
				header: gp.keyText,
				dataIndex: gp.keyField,
				editor: {
					xtype: 'textfield',
					selectOnFocus: true,
					allowBlank: false
				},
				scope: me,
				menuDisabled: true,
				flex: 1
			}, {
				itemId: gp.valueField,
				header: gp.valueText,
				dataIndex: gp.valueField,
				renderer: me.cellRenderer,
				getEditor: me.getCellEditor.bind(me),
				scope: me,
				menuDisabled: true,
				flex: 2
			}]
		}]);
		me.grid.keyColumn = me.items.getAt(1);
		me.grid.valueColumn = me.items.getAt(2);
	},
	
	getCellEditor: function(rec){
		return this.grid.getCellEditor(rec, this);
	},
	
	cellRenderer: function(val, meta, rec) {
		var me = this,
				grid = me.grid,
				type = rec.get(grid.typeField),
				result = val;
		
		if(type === 'boolean') {
			result = me.booleanRenderer(val);
		} else if(type === 'date') {
			result = me.dateRenderer(val);
		}
		return Ext.util.Format.htmlEncode(result);
	},
	
	booleanRenderer: function(val) {
		return this[val ? 'trueText' : 'falseText'];
	},
	
	dateRenderer: function(val) {
		return Ext.util.Format.date(val, this.format);
	}
});
