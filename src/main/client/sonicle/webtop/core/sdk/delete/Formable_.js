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
Ext.define('Sonicle.webtop.core.sdk.Formable', {
	extend: 'Ext.form.Panel',
	
	mixins: [
		'WT.sdk.mixin.Waitable'
	],
	
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
	
	
	/****************************************/
	/*	Event Handlers
	/****************************************/
	
	onModeChange: function(nm,om) {
		this._updateWndTitle(nm);
		this.fireEvent('modechange', this, nm, om);
	},
	
	/****************************************/
	/*	Public Methods (Get&Set)
	/****************************************/
	
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
	
	/****************************************/
	/*	Public Methods
	/****************************************/
	
	beginNew: function(data, params) {
		if(!Ext.isDefined(data)) data = {};
		if(!Ext.isDefined(params)) params = {};
		this.setMode(this.MODE_NEW);
		this.getForm().setValues(data);
		
		// Override this!
	},
	
	beginView: function(data) {
		if(!Ext.isDefined(data)) data = {};
		this.setMode(this.MODE_VIEW);
		//this.getForm().setValues(data);
		
		// Override this!
	},
	
	beginEdit: function(data) {
		if(!Ext.isDefined(data)) data = {};
		this.setMode(this.MODE_EDIT);
		//this.getForm().setValues(data);
		
		// Override this!
	},
	
	/**
	 * Loads the configured form (formPanel component with itemId:'fpnl').
	 * @param {String} action The action param.
	 * @param {Object} params Optional request params (service param is automatically taken).
	 */
	loadForm: function(action, params) {
		params = params || {};
		this.wait();
		if(this.crud) params = Ext.apply(params, {crud: 'read'});
		var form = this.getForm();
		form.load({
			method: 'POST',
			url: 'ServiceRequest',
			params: Ext.apply({
				service: this.ms.getName(),
				action: action
			}, params),
			success: function(f, a) {
				this.unwait();
				this.loadSuccess(f, a);
			},
			failure: function(f, a) {
				this.unwait();
				this.loadFailure(f, a);
			},
			scope: this
		});
	},
	
	/**
	 * Form load success handler. (Override is usually necessary!)
	 * Performs commons actions after a load success.
	 * 
	 * @param {Ext.form.BasicForm} f The form.
	 * @param {Ext.form.Action} a The action.
	 */
	loadSuccess: function(f,a) {
		// Override this!
	},
	
	/**
	 * Form load failure handler. (Override this if necessary!)
	 * Performs commons actions after a load failure.
	 * 
	 * @param {Ext.form.BasicForm} f The form.
	 * @param {Ext.form.Action} a The action.
	 */
	loadFailure: function(f,a) {
		if(a.result) {
			WT.error(a.result.message);
		} else {
			WT.error('Load failure!');
		}
	},
	
	/**
	 * Submits the configured form (formPanel component with itemId:'fpnl').
	 * 
	 * @param {String} action The action param.
	 * @param {Object} params Optional request params (service param is automatically taken).
	 * @param {Boolean} close Specify to close the panel after a success.
	 */
	submitForm: function(action, params, close) {
		var me = this;
		params = params || {};
		this.wait();
		if(this.crud) params = Ext.apply(params, {crud: (this.isEditMode()) ? 'update' : 'create'});
		var form = this.getForm();
		form.submit({
			method: 'POST',
			url: 'ServiceRequest',
			params: Ext.apply({
				service: this.ms.getName(),
				action: action
			}, params),
			success: function(f, a) {
				this.unwait();
				this.submitSuccess(f, a, close);
				//this.submitSuccess.apply(scope, f, a, close);
			},
			failure: function(f, a) {
				this.unwait();
				this.submitFailure(f, a);
				//this.submitFailure.apply(scope, f, a);
			},
			scope: this
		});
	},
	
	/**
	 * Form submit success handler. (Override this if necessary!)
	 * Performs commons actions after a submit success.
	 * 
	 * @param {Ext.form.BasicForm} f The form.
	 * @param {Ext.form.Action} a The action.
	 * @param {Boolean} close Specify to close the panel after a success.
	 */
	submitSuccess: function(f, a, close) {
		this.fireEvent('save', this);
		if(close) this.close(false);
	},
	
	/**
	 * Form submit failure handler. (Override this if necessary!)
	 * Performs commons actions after a submit failure.
	 * 
	 * @param {Ext.form.BasicForm} f The form.
	 * @param {Ext.form.Action} a The action.
	 */
	submitFailure: function(f, a) {
		if(a.result) {
			WT.error(a.result.message);
		} else {
			WT.error('Submit failure!');
		}
	},
	
	/****************************************/
	/*	Public Methods (Utility)
	/****************************************/
	
	/**
	 * Checks if form fields are dirty (aka edited).
	 *
	 * @param {Array} fields (optional) Array of fields to check.
	 */
	areFieldsDirty: function(fields) {
		//return this.getForm().isDirty();
		if(fields === undefined) fields = this.fields;
		return this._isFormDirty(this.getForm(), fields);
	},
	
	/**
	 * Checks if form fields are valid.
	 *
	 * @param {Array} fields (optional) Array of fields to check.
	 */
	areFieldsValid: function(fields) {
		if(fields === undefined) fields = this.fields;
		return this._isFormValid(this.getForm(), fields);
	},
	
	/**
	 * Set a new mode.
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
	
	/**
	 * Check if mode match within the passed one.
	 * @param {String} mode Mode value to be cheched.
	 * @return {Boolean}
	 */
	isMode: function(mode) {
		return (this.mode === mode);
	},
	
	/**
	 * Returns true if current mode is VIEW.
	 * @return {Boolean}
	 */
	isViewMode: function() {
		return (this.mode === this.MODE_VIEW);
	},
	
	/**
	 * Returns true if current mode is NEW.
	 * @return {Boolean}
	 */
	isNewMode: function() {
		return (this.mode === this.MODE_NEW);
	},
	
	/**
	 * Returns true if current mode is EDIT.
	 * @return {Boolean}
	 */
	isEditMode: function() {
		return (this.mode === this.MODE_EDIT);
	},
	
	/****************************************/
	/*	Private Methods
	/****************************************/
	
	_updateWndTitle: function(mode) {
		if(this.wndInited) {
			var ct = this.ownerCt;
			var tit = (ct.title == null) ? '' : ct.title;
			var last = tit.lastIndexOf(':');
			var tit1 = (last == -1) ? tit : tit.substring(0, last);
			var tit2 = null;
			switch(mode) {
				case this.MODE_VIEW:
					tit2 = WT.res('a-view.lbl');
					break;
				case this.MODE_NEW:
					tit2 = WT.res('a-add.lbl');
					break;
				case this.MODE_EDIT:
					tit2 = WT.res('a-edit.lbl');
					break;
			}
			ct.setTitle(String.format('{0}: {1}', tit1, tit2));
		}
	},
	
	_promoteValue: function(id) {
		var fld = this.getForm().findField(id);
		fld.originalValue = fld.getValue();
	},
	
	/**
	 * Convenience method to re-apply the initial value in order to correctly
	 * show configured display field. Sometimes (depending on timing) field's
	 * value is set before store gets loaded; in this cases the wrong value
	 * is displayed to the user.
	 * We need to programmatically call a setValue after the first load.
	 * 
	 * @param {String} id The field id to search for (specify either a id, dataIndex, name or hiddenName).
	 */
	_applyCbo1LoadTrick: function(id) {
		var cbo = this.getField(id);
		var sto = cbo.getStore();
		if(sto.firstload === undefined) {
			sto.firstload = true;
			var val = cbo.getValue();
			if(val) cbo.setValue(val);
		}
	},
	
	_isFormDirty: function(form, fields) {
		if(!Ext.isArray(fields)) fields = [];
		if(fields.length == 0) return form.isDirty();
		var fld = null;
		var dirty = false;
		Ext.iterate(fields, function(id) {
			fld = form.findField(id);
			if(fld) dirty = fld.isDirty();
			if(dirty) return false; // breaks iteration
		}, this);
		return dirty;
	},
	
	_isFormValid: function(form, fields) {
		if(!Ext.isArray(fields)) fields = [];
		if(fields.length == 0) return form.isValid();
		
		var fld = null;
		var valid = true;
		Ext.iterate(fields, function(id) {
			fld = form.findField(id);
			if (fld == null) if (console) console.warn("fld not found: "+id);
			valid = valid & fld.isValid();
		}, this);
		return valid;
	}
	
});
