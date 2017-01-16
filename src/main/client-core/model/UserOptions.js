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
	extend: 'WTA.sdk.model.UserOptions',
	
	proxy: WT.optionsProxy(WT.ID),
	fields: [
		WTF.field('displayName', 'string', false),
		WTF.field('theme', 'string', false),
		WTF.field('layout', 'string', false),
		WTF.field('laf', 'string', false),
		WTF.field('desktopNotification', 'string', false),
		WTF.field('language', 'string', false),
		WTF.field('timezone', 'string', false),
		WTF.field('startDay', 'int', false),
		WTF.field('shortDateFormat', 'string', false),
		WTF.field('longDateFormat', 'string', false),
		WTF.field('shortTimeFormat', 'string', false),
		WTF.field('longTimeFormat', 'string', false),
		WTF.field('upiTitle', 'string', true),
		WTF.field('upiFirstName', 'string', true),
		WTF.field('upiLastName', 'string', true),
		WTF.field('upiNickname', 'string', true),
		WTF.field('upiGender', 'string', true),
		WTF.field('upiEmail', 'string', true),
		WTF.field('upiTelephone', 'string', true),
		WTF.field('upiFax', 'string', true),
		WTF.field('upiPager', 'string', true),
		WTF.field('upiMobile', 'string', true),
		WTF.field('upiAddress', 'string', true),
		WTF.field('upiCity', 'string', true),
		WTF.field('upiPostalCode', 'string', true),
		WTF.field('upiState', 'string', true),
		WTF.field('upiCountry', 'string', true),
		WTF.field('upiCompany', 'string', true),
		WTF.field('upiFunction', 'string', true),
		WTF.field('upiCustom1', 'string', true),
		WTF.field('upiCustom2', 'string', true),
		WTF.field('upiCustom3', 'string', true),
		
		WTF.field('syncAlertEnabled', 'boolean', false),
		WTF.field('syncAlertTolerance', 'int', false),
		
		WTF.roField('otpDelivery', 'string'),
		WTF.roField('otpEmailAddress', 'string'),
		WTF.roField('otpDeviceIsTrusted', 'boolean'),
		WTF.roField('otpDeviceTrustedOn', 'string'),
		
		WTF.roField('canManageUpi', 'boolean'),
		WTF.roField('canSyncDevices', 'boolean')
	]
});
