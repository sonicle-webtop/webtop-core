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
Ext.define('Sonicle.webtop.core.model.UserOptions', {
	extend: 'WT.sdk.model.UserOptions',
	
	proxy: WT.optionsProxy('com.sonicle.webtop.core'),
	fields: [
		'displayName',
		'locale',
		'theme',
		'laf',
		'usdTitle',
		'usdFirstName',
		'usdLastName',
		'usdEmail',
		'usdMobile',
		'usdTelephone',
		'usdFax',
		'usdAddress',
		'usdPostalCode',
		'usdCity',
		'usdState',
		'usdCountry',
		'usdCompany',
		'usdFunction',
		'usdWorkEmail',
		'usdWorkMobile',
		'usdWorkTelephone',
		'usdWorkFax',
		'usdCustom1',
		'usdCustom2',
		'usdCustom3',
		'tfaEnabled',
		'tfaDelivery',
		'tfaEmailAddress',
		'tfaIsTrusted',
		'tfaTrustedOn'
		
		
		/*
		{name: 'usdTitle', mapping: 'userData.title'},
		{name: 'usdFirstName', mapping: 'userData.firstName'},
		{name: 'usdLastName', mapping: 'userData.lastName'},
		{name: 'usdEmail', mapping: 'userData.email'},
		{name: 'usdMobile', mapping: 'userData.mobile'},
		{name: 'usdTelephone', mapping: 'userData.telephone'},
		{name: 'usdFax', mapping: 'userData.fax'},
		{name: 'usdAddress', mapping: 'userData.address'},
		{name: 'usdPostalCode', mapping: 'userData.postalCode'},
		{name: 'usdCity', mapping: 'userData.city'},
		{name: 'usdState', mapping: 'userData.state'},
		{name: 'usdCountry', mapping: 'userData.country'},
		{name: 'usdCompany', mapping: 'userData.company'},
		{name: 'usdFunction', mapping: 'userData.function'},
		{name: 'usdWorkEmail', mapping: 'userData.workEmail'},
		{name: 'usdWorkMobile', mapping: 'userData.workMobile'},
		{name: 'usdWorkTelephone', mapping: 'userData.workTelephone'},
		{name: 'usdWorkFax', mapping: 'userData.workFax'},
		{name: 'usdCustom1', mapping: 'userData.custom1'},
		{name: 'usdCustom2', mapping: 'userData.custom2'},
		{name: 'usdCustom3', mapping: 'userData.custom3'},
		*/
		
	]
});
