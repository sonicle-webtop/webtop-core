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
Ext.define('Sonicle.form.field.IconComboBox', {
	extend: 'Ext.form.field.ComboBox',
	alias: ['widget.soiconcombo', 'widget.soiconcombobox'],
	
	/**
	 * @cfg {String} iconClsField
	 * The underlying {@link Ext.data.Field#name data field name} to bind as icon class.
	 */
	iconClsField: 'iconCls',
	
	initComponent: function() {
		var me = this;
		
		me.listConfig = Ext.apply(this.listConfig || {}, {
			getInnerTpl: me.getListItemTpl
		});
		me.callParent(arguments);
	},
	
	/**
	 * Overrides default implementation of {@link Ext.form.field.ComboBox#afterRender}.
	 */
	afterRender: function() {
		var me = this;
		me.callParent(arguments);
		
		me.wrap = me.el.down('.x-form-text-wrap');
		me.wrap.addCls('so-icon-combo');
		Ext.DomHelper.append(me.wrap, {
			tag: 'i', cls: 'so-picker-icon so-picker-main-icon'
		});
		me.icon = me.el.down('.so-picker-icon');
	},
	
	/**
	 * Overrides default implementation of {@link Ext.form.field.Field#onChange}.
	 */
	onChange: function(newVal, oldVal) {
		var me = this;
		me.updateIconClass(newVal, oldVal);
		me.callParent(arguments);
	},
	
	/**
	 * @private
	 * Returns modified inner template.
	 */
	getListItemTpl: function(displayField){
		var picker = this.pickerField;
		return '<div class="x-combo-list-item">'
			+ '<i class="so-picker-icon {'+picker.iconClsField+'}">&#160;</i>'
			+ '{'+displayField+'}'
			+ '</div>';
	},
	
	/**
	 * @private
	 * Gets iconClass for specified value.
	 */
	getIconClsByValue: function(value) {
		var me = this,
				rec = me.findRecordByValue(value);
		return (rec) ? rec.get(me.iconClsField) : '';
	},
	
	/**
	 * @private
	 * Replaces old iconCls with the new one.
	 */
	updateIconClass: function(nv, ov) {
		var me = this,
				nCls = me.getIconClsByValue(nv),
				oCls = me.getIconClsByValue(ov);
		if(me.icon) me.icon.replaceCls(oCls, nCls);
	}
});
