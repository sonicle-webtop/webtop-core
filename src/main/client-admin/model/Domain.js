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
Ext.define('Sonicle.webtop.core.admin.model.Domain', {
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.data.writer.Json',
		'Sonicle.data.validator.Username'
	],
	proxy: WTF.apiProxy('com.sonicle.webtop.core.admin', 'ManageDomains', 'data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
		}
	}),
	
	fields: [
		WTF.field('domainId', 'string', false, {
			validators: ['sousername']
		}),
		WTF.field('internetName', 'string', false),
		WTF.field('enabled', 'boolean', false, {defaultValue: true}),
		WTF.field('description', 'string', false),
		WTF.field('userAutoCreation', 'boolean', false, {defaultValue: false}),
		WTF.field('dirScheme', 'string', false),
		WTF.field('dirHost', 'string', true),
		WTF.field('dirPort', 'int', true),
		WTF.field('dirAdmin', 'string', true),
		WTF.field('dirPassword', 'string', true),
		WTF.field('dirConnSecurity', 'string', true, {defaultValue: 'null'}),
		WTF.field('dirCaseSensitive', 'boolean', false, {defaultValue: false}),
		WTF.field('dirPasswordPolicy', 'boolean', false, {defaultValue: false}),
		WTF.field('ldapLoginDn', 'string', true),
		WTF.field('ldapLoginFilter', 'string', true),
		WTF.field('ldapUserDn', 'string', true),
		WTF.field('ldapUserFilter', 'string', true),
		WTF.field('ldapUserFirstnameField', 'string', true),
		WTF.field('ldapUserLastnameField', 'string', true),
		WTF.field('ldapUserDisplayNameField', 'string', true)
	],
	
	refreshValidatorsForDirScheme: function() {
		var me = this;
		switch(me.get('dirScheme')) {
			case 'webtop':
				me.setFieldValidators('dirHost');
				me.setFieldValidators('dirAdmin');
				me.setFieldValidators('dirPassword');
				me.setFieldValidators('ldapLoginDn');
				me.setFieldValidators('ldapUserDn');
				me.setFieldValidators('ldapUserFirstnameField');
				me.setFieldValidators('ldapUserLastnameField');
				me.setFieldValidators('ldapUserDisplayNameField');
				break;
			case 'ldapwebtop':
				me.setFieldValidators('dirHost', ['presence']);
				me.setFieldValidators('dirAdmin', ['presence']);
				me.setFieldValidators('dirPassword', ['presence']);
				me.setFieldValidators('ldapLoginDn');
				me.setFieldValidators('ldapUserDn');
				me.setFieldValidators('ldapUserFirstnameField');
				me.setFieldValidators('ldapUserLastnameField');
				me.setFieldValidators('ldapUserDisplayNameField');
				break;
			case 'ldap':
				me.setFieldValidators('dirHost', ['presence']);
				me.setFieldValidators('dirAdmin', ['presence']);
				me.setFieldValidators('dirPassword', ['presence']);
				me.setFieldValidators('ldapLoginDn', ['presence']);
				me.setFieldValidators('ldapUserDn', ['presence']);
				me.setFieldValidators('ldapUserFirstnameField', ['presence']);
				me.setFieldValidators('ldapUserLastnameField', ['presence']);
				me.setFieldValidators('ldapUserDisplayNameField', ['presence']);
				break;
			case 'ad':
				me.setFieldValidators('dirHost', ['presence']);
				me.setFieldValidators('dirAdmin', ['presence']);
				me.setFieldValidators('dirPassword', ['presence']);
				me.setFieldValidators('ldapLoginDn', ['presence']);
				me.setFieldValidators('ldapUserDn', ['presence']);
				me.setFieldValidators('ldapUserFirstnameField');
				me.setFieldValidators('ldapUserLastnameField');
				me.setFieldValidators('ldapUserDisplayNameField');
				break;
			case 'ldapneth':
				me.setFieldValidators('dirHost', ['presence']);
				me.setFieldValidators('dirAdmin', ['presence']);
				me.setFieldValidators('dirPassword', ['presence']);
				me.setFieldValidators('ldapLoginDn', ['presence']);
				me.setFieldValidators('ldapUserDn', ['presence']);
				me.setFieldValidators('ldapUserFirstnameField');
				me.setFieldValidators('ldapUserLastnameField');
				me.setFieldValidators('ldapUserDisplayNameField');
				break;
			case 'imap':
				me.setFieldValidators('dirHost', ['presence']);
				me.setFieldValidators('dirAdmin');
				me.setFieldValidators('dirPassword');
				me.setFieldValidators('ldapLoginDn');
				me.setFieldValidators('ldapUserDn');
				me.setFieldValidators('ldapUserFirstnameField');
				me.setFieldValidators('ldapUserLastnameField');
				me.setFieldValidators('ldapUserDisplayNameField');
				break;
			case 'smb':
				me.setFieldValidators('dirHost', ['presence']);
				me.setFieldValidators('dirAdmin');
				me.setFieldValidators('dirPassword');
				me.setFieldValidators('ldapLoginDn');
				me.setFieldValidators('ldapUserDn');
				me.setFieldValidators('ldapUserFirstnameField');
				me.setFieldValidators('ldapUserLastnameField');
				me.setFieldValidators('ldapUserDisplayNameField');
				break;
			case 'sftp':
				me.setFieldValidators('dirHost', ['presence']);
				me.setFieldValidators('dirAdmin');
				me.setFieldValidators('dirPassword');
				me.setFieldValidators('ldapLoginDn');
				me.setFieldValidators('ldapUserDn');
				me.setFieldValidators('ldapUserFirstnameField');
				me.setFieldValidators('ldapUserLastnameField');
				me.setFieldValidators('ldapUserDisplayNameField');
				break;
		}
		me.getValidation(true);
	}
});
