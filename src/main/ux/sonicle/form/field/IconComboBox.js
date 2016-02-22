/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.field.IconComboBox', {
	extend: 'Ext.form.field.ComboBox',
	alias: ['widget.soiconcombo', 'widget.soiconcombobox'],
	
	/**
	 * @cfg {String} iconClsField
	 * The underlying {@link Ext.data.Field#name data field name} to bind as icon class.
	 */
	iconClsField: 'iconCls',
	
	/**
	 * @private
	 * Keeps track of latest applied icon class.
	 */
	lastCls: null,
	
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
		var me = this, cls;
		if(me.icon) {
			if(me.lastCls) me.icon.removeCls(me.lastCls);
			cls = me.getIconClsByValue(nv);
			if(!Ext.isEmpty(cls)) {
				me.lastCls = cls;
				me.icon.addCls(cls);
			}
		}
	},
	
	onBindStore: function(store, initial){
		var me = this;
		me.callParent(arguments);
		if(store && store.autoCreated) {
			me.iconClsField = !store.expanded ? 'field3' : 'field2';
		}
	},
	
	updateEditable: function(editable, oldEditable) {
		var me = this;
		me.callParent(arguments);
		if(!editable) {
			me.icon.on('click', me.onTriggerClick, me);
		} else {
			me.icon.un('click', me.onTriggerClick, me);
		}
	}
});
