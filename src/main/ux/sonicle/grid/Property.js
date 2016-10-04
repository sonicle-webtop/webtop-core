/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.grid.Property', {
	extend: 'Ext.grid.property.Grid',
	alias: 'widget.sopropertygrid',
	
	/**
	 * @cfg {String} typeField
	 * The name of the field from the property store to use as the type field name.
	 */
	typeField: 'type',
	
	inferTypes: false,
	
	configure: function(config) {
		var me = this,
				store = me.store,
				i = 0,
				len = me.store.getCount(),
				nameField = me.nameField,
				valueField = me.valueField,
				typeField = me.typeField,
				name, type, rec;
		
		me.callParent(arguments);
		
		if(!Ext.isEmpty(typeField)) {
			for (; i < len; ++i) {
				rec = store.getAt(i);
				name = rec.get(nameField);
				type = rec.get(typeField);
				me.setConfigProp(name, 'type', type);
			}
		}
	}
});
