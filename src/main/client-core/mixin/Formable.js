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
Ext.define('Sonicle.webtop.core.mixin.Formable', {
	extend: 'Ext.Mixin',
	alternateClassName: 'WTA.mixin.Formable',
	
	config: {
		
		/**
		* @cfg {String} formItemId Specifies the form panel to look form.
		*/
	   formId: 'fpnl',
	   
	   /**
		* @cfg {Boolean} crud If true, during load and submit operations
		* a crud (specifying the action) param will be included into the request.
		*/
	   crud: true
	},
	
	statics: {
		MODE_NEW: 'new',
		MODE_EDIT: 'edit',
		MODE_VIEW: 'view'
	},
	
	/**
	 * @property {String}
	 * Indicates current editing mode: NEW, EDIT and VIEW.
	 * @readonly
	 */
	mode: null,
	
	onModeChange: function(nm,om) {
		//this._updateWndTitle(nm);
		this.fireEvent('modechange', this, nm, om);
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
	
	/**
	 * Set a new mode.
	 *
	 * @param {String} mode Mode value to be set.
	 */
	setMode: function(mode) {
		var omode = this.mode;
		switch(mode) {
			case this.MODE_EDIT:
			case this.MODE_NEW:
			case this.MODE_VIEW:
				this.mode = mode;
		}
		this.onModeChange(this.mode, omode);
	},
	
	beginNew: function(id, values) {
		var me = this;
		if(!values) values = {};
		
		me.setMode(me.MODE_NEW);
		var rec = Ext.create(me.getFpnl().model, values);
		me.getForm().loadRecord(rec);
	},
	
	beginView: function(id) {
		var me = this;
		me.setMode(me.MODE_VIEW);
		me.loadForm(id);
	},
	
	beginEdit: function(id, values) {
		var me = this;
		if(!values) values = {};
		me.setMode(me.MODE_EDIT);
		//me.getForm().setValues(values);
		
		// Override this!
	},
	
	loadForm: function(id) {
		var me = this;
		var form = me.getForm();
		var rec = Ext.create(me.getFpnl().model);
		rec.load(id, {
			success: function(rec, op) {
				console.log('successsssssssssssssssss');
				form.loadRecord(rec);
			},
			failure: function(rec, op) {
				console.log('failureeeeeeeeeeeeeeeeee');
			},
			scope: me
		});
	},
	
	saveForm: function() {
		
	}
	
});
