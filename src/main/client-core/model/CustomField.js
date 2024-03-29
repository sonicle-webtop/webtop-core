/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.model.CustomField', {
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.String',
		'Sonicle.data.writer.Json',
		'Sonicle.webtop.core.model.CustomFieldProp',
		'Sonicle.webtop.core.model.CustomFieldValue',
		'Sonicle.webtop.core.model.I18nValue'
	],
	proxy: WTF.apiProxy(WT.ID, 'ManageCustomField', 'data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
			//writeChanges: true
		}
	}),
	
	identifier: 'negativestring',
	idProperty: 'id',
	fields: [
		WTF.field('id', 'string'),
		WTF.field('domainId', 'string', true),
		WTF.field('serviceId', 'string', false),
		WTF.field('fieldId', 'string', true),
		WTF.field('name', 'string', false, {
			validators: [{
				type: 'format',
				matcher: /^[_a-zA-Z0-9\-]+$/
			}]
		}),
		WTF.field('description', 'string', true),
		WTF.field('type', 'string', false),
		WTF.field('searchable', 'boolean', false, {defaultValue: false}),
		WTF.field('previewable', 'boolean', false, {defaultValue: false}),
		WTF.field('queryId', 'string', true, {
			validators: [{
				type: 'sopresence',
				ifField: 'type',
				ifFieldValues: ['comboboxds', 'tagds']
			}]
		})
	],
	hasMany: [
		WTF.hasMany('props', 'Sonicle.webtop.core.model.CustomFieldProp'),
		WTF.hasMany('values', 'Sonicle.webtop.core.model.CustomFieldValue'),
		WTF.hasMany('labelI18n', 'Sonicle.webtop.core.model.I18nValue')
	],
	
	isDataBindableType: function() {
		return Sonicle.String.isIn(this.get('type'), ['comboboxds', 'tagds']);
	}
});
