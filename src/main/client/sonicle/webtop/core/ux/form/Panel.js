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
Ext.define('Sonicle.webtop.core.ux.form.Panel', {
	alternateClassName: 'WT.ux.form.Panel',
	extend: 'Ext.form.Panel',
	alias: 'widget.wtform',
	
	trackResetOnLoad: true,
	
	model: null,
	idField: null,
	
	loadForm: function(id) {
		var me = this;
		
		me.fireEvent('beforeLoad', me);
		var opts = {
			callback: function(rec, op, success) {
				if(success) me.bindModel(rec);
				me.fireEvent('load', me, op, success);
			},
			scope: me
		};
		if(Ext.isString(me.model)) {
			if(id) me.setFieldValue(me.idField, id, true);
			if(!me.isFieldEmpty(me.idField)) {
				Ext.ClassManager.get(me.model).load(me.getFieldValue(me.idField), opts);
			} else {
				me.bindModel(Ext.create(me.model, {}));
			}
		} else {
			me.model.load(opts);
		}
	},
	
	bindModel: function(model) {
		var me = this;
		me.model = model;
		me.getForm().loadRecord(model);
	},
	
	saveForm: function() {
		var me = this;
		var fo = me.getForm();
		me.fireEvent('beforeSave', me);
		if(fo.isDirty()) fo.updateRecord(me.getRecord());
		me.model.save({
			callback: function(rec, op, success) {
				if(success) {
					me.bindModel(rec);
				} else {
					WT.error(op.getError());
				}
				me.fireEvent('save', me, op, success);
			},
			scope: me
		});
	},
	
	/**
	 * Find a specific Ext.form.field.Field in this form.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @return {Ext.form.field.Field} The first matching field, or `null` if none was found.
	 */
	getField: function(id) {
		return this.getForm().findField(id);
	},
	
	/**
	 * Gets value of a specific Ext.form.field.Field in this form.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @return {Mixed} Field value.
	 */
	getFieldValue: function(id) {
		var fld = this.getField(id);
		if(!fld) return undefined;
		if (fld.isXType('radiogroup')) {
			return fld.getValue().value;
		} else {
			return fld.getValue();
		}
	},
	
	/**
	 * Sets value for a specific Ext.form.field.Field in this form.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @param {Mixed} value The field value.
	 */
	setFieldValue: function(id, value, silent) {
		silent = silent || false;
		var fld = this.getField(id);
		if(silent && !fld) return;
		fld.setValue(value);
	},
	
	/**
	 * Checks if a specific field is empty.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @return {Bolean}
	 */
	isFieldEmpty: function(id) {
		return Ext.isEmpty(this.getFieldValue(id));
	}
});
