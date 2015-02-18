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
	idField: null, // Private
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		// Gets model's id property
		if(Ext.isString(cfg.model)) {
			var model = Ext.create(me.model);
			me.idField = model.getIdProperty();
			model.destroy();
		} else {
			me.idField = cfg.model.getIdProperty();
		}
	},
	
	bindModel: function(model) {
		var me = this;
		me.model = model;
		me.loadRecord(model);
	},
	
	loadForm: function(data, opts) {
		var me = this, cb;
		if(Ext.isEmpty(me.model)) return;
		if(!Ext.isObject(data)) {
			var obj = {};
			obj[me.idField] = data;
			data = obj;
		}
		opts = opts || {};
		cb = {fn: opts.callback, scope: opts.scope};
		delete opts.callback;
		delete opts.scope;
		
		var fn = function(rec, op, success) {
			if(success) me.bindModel(rec);
			me.fireEvent('load', me, success, op, opts);
			Ext.callback(cb.fn, cb.scope, [me, success, me.model, op]);
		};
		
		me.fireEvent('beforeload', me); //TODO: evento cancellabile
		if(Ext.isString(me.model)) {
			if(data[me.idField]) me.setFieldValue(me.idField, data[me.idField], true);
			
			if(!me.isFieldEmpty(me.idField)) {
				// Loads model with provided id
				Ext.ClassManager.get(me.model).load(me.getFieldValue(me.idField), {
					callback: fn,
					scope: me
				});
			} else {
				// Creates an empty model (no id provided)
				me.bindModel(Ext.create(me.model, data));
				Ext.callback(cb.fn, cb.scope, [me, true, me.model, null]);
			}
		} else {
			// Model is a ready instance...use it simply!
			me.model.load({
				callback: cb,
				scope: me
			});
		}
	},
	
	saveForm: function(opts) {
		var me = this, cb;
		if(Ext.isEmpty(me.model)) return;
		opts = opts || {};
		opts = opts || {};
		cb = {fn: opts.callback, scope: opts.scope};
		delete opts.callback;
		delete opts.scope;
		
		me.fireEvent('beforesave', me); //TODO: evento cancellabile
		if(me.getForm().isDirty()) me.updateRecord(me.getRecord());
		me.model.save({
			id: me.getFieldValue(me.idField), //TODO: serve? credo di no!
			callback: function(rec, op, success) {
				if(success) {
					me.bindModel(rec);
				} else {
					WT.error(op.getError());
				}
				me.fireEvent('save', me, success, op, opts);
				Ext.callback(cb.fn, cb.scope, [me, success, me.model, op]);
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
