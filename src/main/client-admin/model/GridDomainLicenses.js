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
Ext.define('Sonicle.webtop.core.admin.model.GridDomainLicenses', {
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.webtop.core.admin.model.GridLicenseLease'
	],
	
	idProperty: 'id',
	fields: [
		WTF.field('id', 'string', false),
		WTF.roField('serviceId', 'string'),
		WTF.roField('productCode', 'string'),
		WTF.roField('valid', 'boolean'),
		WTF.roField('expired', 'boolean'),
		WTF.roField('expiry', 'date', false, {dateFormat: 'Y-m-d'}),
		WTF.roField('expireSoon', 'boolean'),
		WTF.roField('leaseAvail', 'int'),
		WTF.roField('hwId', 'string'),
		WTF.roField('regTo', 'string'),
		WTF.field('autoLease', 'boolean', false),
		WTF.field('leaseCount', 'int', false, {persist: false})
	],
	
	updateLeaseCount: function() {
		this.set('leaseCount', this.leases().getCount());
	},
	
	isLeaseUnbounded: function() {
		return this.get('leaseAvail') === -1;
	},
	
	getStatus: function() {
		var me = this;
		if (!(me.get('valid') === true)) {
			return (me.get('expired') === true) ? 'expired' : 'invalid';
		} else if ((me.get('expireSoon') === true) || (!me.isLeaseUnbounded() && (me.get('leaseCount') >= Math.ceil(me.get('leaseAvail') * 0.8)))) {
			return 'warn';
		} else {
			return 'valid';
		}
	},
	
	hasMany: [
		WTF.hasMany('leases', 'Sonicle.webtop.core.admin.model.GridLicenseLease')
	]
});
