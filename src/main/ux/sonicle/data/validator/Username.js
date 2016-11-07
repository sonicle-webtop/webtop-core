/*
 * Sonicle ExtJs UX
 * Copyright (C) 2015 Sonicle S.r.l.
 * sonicle@sonicle.com
 * http://www.sonicle.com
 */
Ext.define('Sonicle.data.validator.Username', {
	extend: 'Ext.data.validator.Format',
	alias: 'data.validator.sousername',
	
	type: 'sousername',
	
	config: {
		
		/**
		 * @cfg {RegExp} matcher
		 * A matcher to check for username.
		 */
		matcher: /^[a-z][a-z0-9\.\-\_]+$/i
	}
});
