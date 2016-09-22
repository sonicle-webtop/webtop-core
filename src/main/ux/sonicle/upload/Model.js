/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.upload.Model', {
	extend: 'Ext.data.Model',
	fields: [
		{name: 'id', type: 'string'},
		{name: 'name', type: 'string'},
		{name: 'size', type: 'int'},
		{name: 'percent', type: 'int'},
		{name: 'progress', type: 'number', depends: 'percent',
			convert: function(v,rec) {
				return rec.get('percent') * 0.01;
			}
		},
		{name: 'status', type: 'int'},
		{name: 'loaded', type: 'int'},
		{name: 'uploadId', type: 'string'}
	]
});
