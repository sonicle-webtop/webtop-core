/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.data.BaseModel', {
	alternateClassName: 'WTA.ux.data.BaseModel',
	extend: 'Sonicle.data.Model',
	
	identifier: 'negative',
	
	schema: {
		namespace: 'Sonicle.webtop.core.model',
		proxy: {
			type: 'memory',
			extraParams: {
				service: 'no.service',
				action: 'no.action'
			},
			reader: {
				type: 'json',
				rootProperty: '{entityName:lowercase}',
				messageProperty: 'message'
			},
			writer: {
				type: 'json'
			}
			
			/*
			type: 'ajax',
			api: {
				create: 'service-request?crud=create',
				read: 'service-request?crud=read',
				update: 'service-request?crud=update',
				destroy: 'service-request?crud=destroy'
			},
			extraParams: {
				service: 'com.sonicle.webtop.core',
				action: '{entityName}'
			},
			reader: {
				type: 'json',
				rootProperty: '{entityName:lowercase}',
				messageProperty: 'message'
			}
			*/
			//writer: {
			//	type: 'json',
			//	expandData: true,
			//	nameProperty: 'mapping'
			//}
		}
	},
	
	constructor: function(cfg) {
		this.callParent([cfg]);
		this._pendingCompile = [];
	},
	
	setExtraParams: function(params) {
		var proxy = this.getProxy();
		if (proxy) WTU.applyExtraParams(proxy, params);
	},
	
	/**
	 * Sets the specified field only if its (current) value is null.
	 * @param {String} field The name of the field to update
	 * @param {Mixed} value The value to set
	 */
	setIfNull: function(field, value) {
		if (this.get(field) === null) {
			this.set(field, value);
		}
	},
	
	/**
	 * Sets the date part only into the specified field.
	 * If null, the field will be initialized using the current date value.
	 * Passed field name must refer to a date field.
	 * @param {String} field The name of the field to update
	 * @param {Date} date The value from which copy the date part
	 * @returns {Date} The value set
	 */
	setDatePart: function(field, date) {
		var me = this,
				v = me.get(field) || new Date(), dt;
		dt = !Ext.isDate(date) ? null : Sonicle.Date.copyDate(date, v);
		me.set(field, dt);
		return dt;
	},
	
	/**
	 * Sets the time part only into the specified field.
	 * If null, the field will be initialized using the current date value.
	 * Passed field name must refer to a date field.
	 * @param {String} field The name of the field to update
	 * @param {Date} date The value from which copy the time part
	 * @returns {Date} The value set
	 */
	setTimePart: function(field, date) {
		var me = this,
				v = me.get(field) || new Date(), dt;
		dt = !Ext.isDate(date) ? null : Sonicle.Date.copyTime(date, v);
		me.set(field, dt);
		return dt;
	}
});
