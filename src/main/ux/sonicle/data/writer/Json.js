/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.data.writer.Json', {
	extend: 'Ext.data.writer.Json',
	alias: 'writer.sojson',
	
	config: {
		/**
		 * @cfg {Boolean} writeAssociated
		 * 'True' to write associated entities data into json response.
		 */
		writeAssociated: true,
		
		/**
		 * @cfg {Boolean} writeChanges
		 * 'True' to write session {@link Ext.data.Session#getChanges} 
		 * instead of actual associated data.
		 * Only valid if {@link #writeAssociated} is active.
		 */
		writeChanges: false
	},
	
	getRecordData: function(record, operation) {
		var me = this,
				writeAsso = me.getWriteAssociated(),
				writeCha = me.getWriteChanges(),
				data, asso, assoData, sto, cname, cha;
		data = this.callParent(arguments);
		
		if(writeAsso) {
			if(record.session && writeCha) {
				cha = record.session.getChanges();
				Ext.iterate(record.associations, function(name) {
					asso = record.associations[name];
					sto = record[asso.getterName]();
					cname = Ext.getClassName(sto.getModel());
					data[asso.role] = cha[cname];
				});
			} else {
				assoData = record.getAssociatedData();
				Ext.iterate(record.associations, function(name) {
					asso = record.associations[name];
					data[asso.role] = assoData[asso.role];
				});
			}
		}
		return data;
	}
});
