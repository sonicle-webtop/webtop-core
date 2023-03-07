/* 
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.admin.model.User', {
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.String',
		'Sonicle.data.writer.Json',
		'Sonicle.data.validator.Username',
		'Sonicle.webtop.core.admin.model.AclSubject',
		'Sonicle.webtop.core.admin.model.PermissionString',
		'Sonicle.webtop.core.admin.model.AllowedService'
	],
	
	proxy: WTF.apiProxy('com.sonicle.webtop.core.admin', 'ManageDomainUser', 'data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
		}
	}),
	
	validatePassword: false,
	pwdPolicies: null,
	passwordPolicy: false,
	usernameField: 'userId',
	passwordFieldLabel: '',
	
	identifier: 'negativestring',
	idProperty: 'id',
	fields: [
		WTF.field('id', 'string', false),
		WTF.field('userId', 'string', true, {
			validators: ['presence', 'sousername']
		}),
		WTF.field('enabled', 'boolean', false, {defaultValue: true}),
		WTF.field('displayName', 'string', true),
		WTF.field('password', 'string', true, {
			validators: ['wtadm-userpassword']
		}),
		WTF.field('password2', 'string', true, {
			validators: ['wtadm-userpassword2']
		}),
		WTF.field('firstName', 'string', true),
		WTF.field('lastName', 'string', true)
	],
	hasMany: [
		WTF.hasMany('assignedGroups', 'Sonicle.webtop.core.admin.model.AclSubject'),
		WTF.hasMany('assignedRoles', 'Sonicle.webtop.core.admin.model.AclSubject'),
		WTF.hasMany('permissions', 'Sonicle.webtop.core.admin.model.PermissionString'),
		WTF.hasMany('allowedServices', 'Sonicle.webtop.core.admin.model.AllowedService')
	],
	
	buildDisplayName: function() {
		var SoS = Sonicle.String;
		return Ext.String.trim(SoS.deflt(this.get('firstName'), '') + ' ' + SoS.deflt(this.get('lastName'), ''));
	}
});
Ext.define('Sonicle.webtop.core.admin.model.VUserPassword', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.wtadm-userpassword',
	mixins: [
		'WTA.mixin.PwdPolicies'
	],
	
	constructor: function(cfg) {
		var me = this;
		me.vtors = {};
		me.callParent([cfg]);
	},
	
	validate: function(v, rec) {
		var me = this;
		if (rec.validatePassword && Ext.isObject(rec.pwdPolicies)) {
			return me.checkPolicies(v, rec.pwdPolicies, rec.get(rec.usernameField), null);
		}
		return true;
	}
});
Ext.define('Sonicle.webtop.core.admin.model.VUserPassword2', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.wtadm-userpassword2',
	
	constructor: function(cfg) {
		var me = this;
		me.vtors = {};
		me.callParent([cfg]);
		me.vtors['equa'] = Ext.create('Sonicle.data.validator.MatchField', {
			matchField: 'password'
		});
	},
	
	validate: function(v, rec) {
		var me = this;
		if (rec.validatePassword) {
			me.vtors['equa'].setMatchFieldLabel(rec.passwordFieldLabel);
			return me.vtors['equa'].validate(v, rec);
		}
		return true;
	}
});
