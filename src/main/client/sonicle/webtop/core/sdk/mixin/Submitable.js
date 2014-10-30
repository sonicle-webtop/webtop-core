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
Ext.define('Sonicle.webtop.core.sdk.mixin.Submitable', {
	extend: 'Ext.Mixin',
	alternateClassName: 'WT.sdk.mixin.Submitable',
	
	config: {
		
		/**
		* @cfg {String} formItemId Specifies the form panel to look form.
		*/
	   formId: 'fpnl'
	},
	
	/**
	 * Returns form component.
	 * @return {Ext.form.Panel}
	 */
	getFpnl: function() {
		return this.getComponent(this.getFormId());
	},
	
	/**
	 * Gets form management object.
	 * @return {Ext.form.Basic} The management object.
	 */
	getForm: function() {
		return this.getFpnl().getForm();
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
	setFieldValue: function(id, value) {
		this.getField(id).setValue(value);
	},
	
	/**
	 * Checks if a specific field is empty.
	 * @param {String} id The value to search for (specify either a id, dataIndex, name or hiddenName).
	 * @return {Bolean}
	 */
	isFieldEmpty: function(id) {
		return Ext.isEmpty(this.getFieldValue(id));
	},
	
	submitForm: function() {
		var me = this;
		var fo = me.getForm();
		if(!fo.isValid()) return;
		var rec = Ext.create(me.getFormModel());
		me.wait();
		fo.updateRecord(rec);
		rec.save({
			callback: function(rec, op, success) {
				me.unwait();
				if(me.fireEvent('submit', me, op, success)) {
					if(!success) WT.error(op.getError());
				}
			},
			scope: me
		});
	},
	
	getFormModel: function() {
		var model = this.getFpnl().model;
		if(Ext.isEmpty(model)) Ext.Error.raise("You need to configure form's 'model' property");
		return model;
	}
});
