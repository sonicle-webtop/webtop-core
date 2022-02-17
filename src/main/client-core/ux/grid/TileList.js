/* 
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.grid.TileList', {
	alternateClassName: 'WTA.ux.grid.TileList',
	extend: 'Ext.grid.Panel',
	alias: 'widget.wttilelist',
	requires: [
		'Sonicle.grid.column.Clipboard'
	],
	
	border: false,
	rowLines: false,
	hideHeaders: true,
	disableSelection: true,
	cls: 'wt-tilelist',
	
	/**
	 * @cfg {String} valueField
	 * The underlying {@link Ext.data.Field#name data field name} that targets value.
	 */
	valueField: 'value',
	
	/**
	 * @cfg {String} labelField
	 * The underlying {@link Ext.data.Field#name data field name} that targets label.
	 */
	labelField: 'label',
	
	/**
	 * @cfg {Boolean} linkifyValue
	 * Set to `true` to add link Class to value.
	 */
	linkifyValue: false,
	
	clipboardIconCls: 'far fa-clone',
	
	clipboardTooltipText: 'Copy to clipboard',
	labelTexts: undefined,
	
	initComponent: function() {
		var me = this;
		me.columns = me._createColumns();
		me.callParent(arguments);
		me.on('cellclick', me._onCellClick, me);
	},
	
	_createColumns: function() {
		var me = this;
		return [
			{
				xtype: 'templatecolumn',
				tpl: [
					'<div class="wt-cell-caption wt-theme-text-subtitle" style="padding-bottom:3px">{[this.getLabel(values)]}</div>',
					'<span class="{[this.getValueCls()]}">{[this.getValue(values)]}</span>',
					{
						getLabel: function(values) {
							var lbl = !Ext.isEmpty(me.labelField) ? values[me.labelField] : null;
							if (me.labelTexts === undefined) {
								return this.value(lbl);
							} else {
								return lbl ? this.value(me.labelTexts[lbl]) : null;
							}
							//return Ext.isEmpty(me.labelField) ? '' : this.value(me.labelTexts[values[me.labelField]]);
						},
						getValueCls: function() {
							return me.linkifyValue ? 'wt-theme-text-hyperlink' : '';
						},
						getValue: function(values) {
							return Ext.isEmpty(me.valueField) ? '' : this.value(values[me.valueField]);
						},
						value: function(s) {
							return !Ext.isEmpty(s) ? Ext.String.htmlEncode(s) : '';
						}
					}
				],
				flex: 1
			}, {
				xtype: 'actioncolumn',
				align: 'center',
				hidden: WT.plTags.mobile,
				items: [
					{
						iconCls: me.clipboardIconCls,
						tooltip: me.clipboardTooltipText,
						handler: function(g, ridx) {
							var rec = g.getStore().getAt(ridx),
									value = rec.get(me.valueField);
							Sonicle.ClipboardMgr.copy(value);
							me.fireEvent('cellvaluecopy', me, value, rec.get(me.labelField));
						}
					}
				],
				width: 30
			}
		];
	},
	
	_onCellClick: function(s, td, cidx, rec) {
		var me = this;
		if (cidx === 0) {
			me.fireEvent('cellvalueclick', me, rec.get(me.valueField), rec);
		}
	}
});
