/* 
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.model.GridDomainAccessLog', {
	extend: 'Ext.data.Model',
	requires: [
		'Sonicle.webtop.core.admin.model.GridDomainAccessLogDetail'
	],
	
	identifier: 'negativestring',
	idProperty: 'sessionId',
	fields: [
		WTF.roField('sessionId', 'string'),
		WTF.roField('userId', 'string'),
		WTF.roField('timestamp', 'date'),
		WTF.roField('duration', 'float'),
		WTF.roField('authenticated', 'boolean'),
		WTF.roField('failure', 'boolean'),
		WTF.roField('loginErrors', 'int')
	],
	
	getStatus: function() {
		var me = this;
		if (me.get('failure') === true) {
			return me.get('authenticated') === true ? 'warn' : 'critical';
		} else {
			return 'ok';
		}
	}
});
