/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.form.field.ColorComboBox', {
	extend: 'Ext.form.field.ComboBox',
	alias: ['widget.socolorcombo', 'widget.socolorcombobox'],
	
	/**
	 * @cfg {square|circle} geometry [geometry=square]
	 * Changes the geometry of the marker that displays the color.
	 */
	geometry: 'square',
	
	/**
	 * @cfg {String} colorField
	 * The underlying {@link Ext.data.Field#name data field name} to bind as color.
	 */
	colorField: 'color',
	
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
		me.wrap.addCls('so-colorcombo');
		Ext.DomHelper.append(me.wrap, {
			tag: 'i', cls: 'so-colorcombo-marker'
		});
		me.marker = me.el.down('.so-colorcombo-marker');
		if(me.marker && !me.editable) {
			me.marker.on('click', me.onTriggerClick, me);
		}
	},
	
	/**
	 * Overrides default implementation of {@link Ext.form.field.Field#onChange}.
	 */
	onChange: function(newVal, oldVal) {
		var me = this;
		me.updateMarker(newVal, oldVal);
		me.callParent(arguments);
	},
	
	/**
	 * @private
	 * Returns modified inner template.
	 */
	getListItemTpl: function(displayField){
		var picker = this.pickerField,
				styles = picker.buildMarkerStyles(picker.geometry, '{'+picker.colorField+'}'),
				style = Ext.dom.Helper.generateStyles(styles);
		
		return '<div class="so-colorcombo x-combo-list-item">'
			+ '<div class="so-colorcombo-marker" style="'+style+'"></div>'
			+ '<span>{'+displayField+'}</span>'
			+ '</div>'
		;
	},
	
	/**
	 * @private
	 */
	buildMarkerStyles: function(geometry, color) {
		var obj = {};
		if(geometry === 'circle') {
			Ext.apply(obj, {
				borderRadius: '50%'
			});
		}
		Ext.apply(obj, {
			backgroundColor: !Ext.isEmpty(color) ? color : ''
		});
		return obj;
	},
	
	/**
	 * @private
	 * Gets iconClass for specified value.
	 */
	getColorByValue: function(value) {
		var me = this,
				rec = me.findRecordByValue(value);
		return (rec) ? rec.get(me.colorField) : '';
	},
	
	/**
	 * @private
	 * Replaces old iconCls with the new one.
	 */
	updateMarker: function(nv, ov) {
		var me = this, color;
		if(me.marker) {
			color = me.getColorByValue(nv);
			me.marker.setStyle(me.buildMarkerStyles(me.geometry, color));
		}
	},
	
	onBindStore: function(store, initial){
		var me = this;
		me.callParent(arguments);
		if(store && store.autoCreated) {
			me.colorField = !store.expanded ? 'field3' : 'field2';
		}
	},
	
	updateEditable: function(editable, oldEditable) {
		var me = this;
		me.callParent(arguments);
		if(me.marker) {
			if(!editable) {
				me.marker.on('click', me.onTriggerClick, me);
			} else {
				me.marker.un('click', me.onTriggerClick, me);
			}
		}
	}
});
