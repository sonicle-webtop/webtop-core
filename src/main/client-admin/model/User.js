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
	extend: 'WTA.ux.data.BaseModel',
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
	proxy: WTF.apiProxy('com.sonicle.webtop.core.admin', 'ManageUser', 'data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
		}
	}),
	
	validatePassword: false,
	passwordPolicy: false,
	
	identifier: 'negativestring',
	idProperty: 'profileId',
	fields: [
		WTF.field('profileId', 'string', false),
		WTF.field('domainId', 'string', false),
		WTF.field('userId', 'string', false, {
			validators: ['sousername']
		}),
		WTF.field('enabled', 'boolean', true),
		//WTF.field('password', 'string', true),
		//WTF.field('password2', 'string', true),
		WTF.field('password', 'string', true, {
			validators: ['wtadmuserpassword']
		}),
		WTF.field('password2', 'string', true, {
			validators: ['wtadmuserpassword2']
		}),
		WTF.field('displayName', 'string', true),
		WTF.field('firstName', 'string', true),
		WTF.field('lastName', 'string', true)
	],
	hasMany: [
		WTF.hasMany('assignedGroups', 'Sonicle.webtop.core.admin.model.AssignedGroup'),
		WTF.hasMany('assignedRoles', 'Sonicle.webtop.core.admin.model.AssignedRole'),
		WTF.hasMany('assignedServices', 'Sonicle.webtop.core.admin.model.AssignedService'),
		WTF.hasMany('permissions', 'Sonicle.webtop.core.admin.model.RolePermission')
	],
	
	buildDisplayName: function() {
		var soString = Sonicle.String, s;
		s = soString.deflt(this.get('firstName'), '') + ' ' + soString.deflt(this.get('lastName'), '');
		return Ext.String.trim(s);
	}
});
Ext.define('Sonicle.webtop.core.admin.model.VUserPassword', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.wtadmuserpassword',
	
	constructor: function(cfg) {
		var me = this;
		me.vtors = {};
		me.callParent([cfg]);
		me.vtors['pres'] = Ext.create('Ext.data.validator.Presence');
		me.vtors['pass'] = Ext.create('Sonicle.data.validator.Password', {
			complex: false
		});
		me.vtors['cpass'] = Ext.create('Sonicle.data.validator.Password', {
			complex: true
		});
	},
	
	validate: function(v, rec) {
		var me = this, ret;
		if (rec.validatePassword) {
			ret = me.vtors['pres'].validate(v, rec);
			if (ret !== true) return ret;
			ret = rec.passwordPolicy ? me.vtors['cpass'].validate(v, rec) : me.vtors['pass'].validate(v, rec);
			if (ret !== true) return ret;
		}
		return true;
	}
});
Ext.define('Sonicle.webtop.core.admin.model.VUserPassword2', {
	extend: 'Ext.data.validator.Validator',
	alias: 'data.validator.wtadmuserpassword2',
	
	constructor: function(cfg) {
		var me = this;
		me.vtors = {};
		me.callParent([cfg]);
		me.vtors['pres'] = Ext.create('Ext.data.validator.Presence');
		me.vtors['equa'] = Ext.create('Sonicle.data.validator.Equality', {
			equalField: 'password',
			fieldLabel: ''
		});
	},
	
	validate: function(v, rec) {
		var me = this, ret;
		if (rec.validatePassword) {
			ret = me.vtors['pres'].validate(v, rec);
			if (ret !== true) return ret;
			ret = me.vtors['equa'].validate(v, rec);
			if (ret !== true) return ret;
		}
		return true;
	}
});
