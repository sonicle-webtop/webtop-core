/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.upload.Model', {
	extend: 'Ext.data.Model',
	fields: [
		'id',
		'loaded',
		'name',
		'size',
		'percent',
		'status'
	]
});
