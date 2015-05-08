/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.data.Model', {
	extend: 'Ext.data.Model',
	//alias: 'writer.sojson',
	
	setAssociated: function(data) {
		var me = this, 
				asso, sto, assoData;
		
		Ext.iterate(me.associations, function(name) {
			asso = me.associations[name];
			sto = me[asso.getterName]();
			assoData = data[asso.role];
			if(assoData) {
				sto.add(assoData);
			}
		});
	},
	
	isDirty: function() {
		var me = this, 
				dirty = me.dirty, asso, sto;
		
		if(dirty) return true; // If already dirty, return true directly...
		// Otherwise evaluate associations (if present)
		Ext.iterate(me.associations, function(name) {
			asso = me.associations[name];
			sto = me[asso.getterName]();
			if(sto.needsSync) {
				dirty = true;
				return;
			}
		});
		return dirty;
	}
});
