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
Ext.define('Sonicle.webtop.core.admin.model.User', {
	extend: 'WT.ux.data.BaseModel',
	requires: [
		'Sonicle.data.writer.Json',
		'Sonicle.data.validator.Equality',
		'Sonicle.data.validator.Username',
		'Sonicle.data.validator.Password',
		'Sonicle.webtop.core.admin.model.AssignedGroup',
		'Sonicle.webtop.core.admin.model.AssignedRole',
		'Sonicle.webtop.core.admin.model.AssignedService',
		'Sonicle.webtop.core.admin.model.RolePermission'
	],
	proxy: WTF.apiProxy('com.sonicle.webtop.core.admin', 'ManageUsers', 'data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
		}
	}),
	
	identifier: 'negativestring',
	idProperty: 'profileId',
	fields: [
		WTF.field('profileId', 'string', false),
		WTF.field('domainId', 'string', false),
		WTF.field('userId', 'string', false, {
			validators: ['sousername']
		}),
		WTF.field('enabled', 'boolean', true),
		WTF.field('password', 'string', true),
		WTF.field('password2', 'string', true),
		WTF.field('firstName', 'string', false),
		WTF.field('lastName', 'string', false),
		WTF.field('displayName', 'string', false)
	],
	hasMany: [
		WTF.hasMany('assignedGroups', 'Sonicle.webtop.core.admin.model.AssignedGroup'),
		WTF.hasMany('assignedRoles', 'Sonicle.webtop.core.admin.model.AssignedRole'),
		WTF.hasMany('assignedServices', 'Sonicle.webtop.core.admin.model.AssignedService'),
		WTF.hasMany('permissions', 'Sonicle.webtop.core.admin.model.RolePermission')
	]
});
