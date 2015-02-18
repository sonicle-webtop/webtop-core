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
Ext.define('Sonicle.form.RadioGroup', {
	extend: 'Ext.form.RadioGroup',
	alias: 'widget.soradiogroup',
	
	singleValue: true,
	
	/**
	 * If {@link #singleValue} config is True, this method overrides default 
	 * implementation setting the value of the radio with corresponding 
	 * {@link Ext.form.field.Radio#name name} and {@link Ext.form.field.Radio#inputValue inputValue}.
	 * @param {Object} value The map from names to values to be set or a single value.
	 * @returns {Sonicle.form.RadioGroup} this
	 */
	setValue: function(value) {
		var me = this, first, formId;
		if(!me.singleValue) {
			return me.callParent(arguments);
			
		} else {
			Ext.suspendLayouts();
			first = me.items.first();
			formId = first ? first.getFormId() : null;
			me.items.each(function(item) {
				if((item.inputValue === value) && (item.getFormId() === formId)) {
					item.setValue(true);
					return false;
				}
			});
			Ext.resumeLayouts(true);
			return me;
		}
	},
	
	/**
	 * If {@link #singleValue} config is True, overrides default 
	 * {@link Ext.form.CheckboxGroup#getModelData} implementation returning a 
	 * data object containing the {@link Ext.form.field.Radio#inputValue inputValue} 
	 * of the first checked radio.
	 */
	getModelData: function() {
		var me = this, first, formId, data = {};
		if(!me.singleValue) {
			return me.callParent(arguments);
			
		} else {
			first = me.items.first();
			formId = first ? first.getFormId() : null;
			me.items.each(function(item) {
				if((item.getFormId() === formId) && (item.getValue() === true)) {
					data[me.name] = item.inputValue;
				}
			});
			return data;
		}
	}
});
