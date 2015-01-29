/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.Panel', {
	extend: 'Ext.form.Panel',
	alias: 'widget.soform',
	
	trackResetOnLoad: true,
	
	model: null,
	idField: null,
	
	loadForm: function(id) {
		var me = this;
		
		if(Ext.isEmpty(me.model)) return;
		me.fireEvent('beforeLoad', me);
		var opts = {
			callback: function(rec, op, success) {
				if(success) me.bindModel(rec);
				me.fireEvent('load', me, success, op);
			},
			scope: me
		};
		if(Ext.isString(me.model)) {
			if(id) me.setFieldValue(me.idField, id, true);
			if(!me.isFieldEmpty(me.idField)) {
				// Loads model with provided id
				Ext.ClassManager.get(me.model).load(me.getFieldValue(me.idField), opts);
			} else {
				// Creates an empty model (no id provided)
				me.bindModel(Ext.create(me.model, {}));
			}
		} else {
			// Model is a ready instance...use it simply!
			me.model.load(opts);
		}
	},
	
	bindModel: function(model) {
		var me = this;
		me.model = model;
		//me.getForm().loadRecord(model);
		me.loadRecord(model);
	},
	
	saveForm: function() {
		var me = this;
		//var fo = me.getForm();
		if(Ext.isEmpty(me.model)) return;
		me.fireEvent('beforeSave', me);
		//if(fo.isDirty()) fo.updateRecord(me.getRecord());
		if(me.getForm().isDirty()) me.updateRecord(me.getRecord());
		me.model.save({
			id: me.getFieldValue(me.idField),
			callback: function(rec, op, success) {
				if(success) {
					me.bindModel(rec);
				} else {
					WT.error(op.getError());
				}
				me.fireEvent('save', me, success, op);
			},
			scope: me
		});
	},
	
	loadRecord: function(record) {
		var me = this, obj;
		me.callParent(arguments);
		Ext.iterate(record.associations, function(ent) {
			obj = {};
			Ext.iterate(record[ent].getData(), function(fld,val) {
				obj[ent+'.'+fld] = val;
			});
			me.getForm().setValues(obj);
		});
	},
	
	updateRecord: function(record) {
		var me = this;
		var values = me.getForm().getFieldValues(), arec, obj, name;
		Ext.iterate(record.associations, function(entity) {
			arec = record[entity];
			obj = {};
			Ext.iterate(arec, function(fld,val) {
				name = entity+'.'+fld;
				if(values.hasOwnProperty(name)) obj[fld] = values[name];
			});
			arec.beginEdit();
			arec.set(obj);
			arec.endEdit();
		});
		return me.callParent(arguments);
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
