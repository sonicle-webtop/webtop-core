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

Ext.define('Sonicle.webtop.core.admin.model.GridDomainAccessLogDetail', {
	extend: 'Ext.data.Model',
	
	identifier: 'negativestring',
	fields: [
		WTF.roField('timestamp', 'date'),
		WTF.roField('action', 'string'),
		WTF.roField('ipAddress', 'string'),
		WTF.roField('isAddressPublic', 'boolean'),
		WTF.field('geoInfo', 'boolean', false, {defaultValue: false, persist: false}),
		WTF.field('_geoFlagUrl', 'string', true, {persist: false}),
		WTF.field('_geoLat', 'string', true, {persist: false}),
		WTF.field('_geoLong', 'string', true, {persist: false}),
		WTF.field('_geoContinentCode', 'string', true, {persist: false}),
		WTF.field('_geoCountryCode', 'string', true, {persist: false}),
		WTF.field('_geoCountryName', 'string', true, {persist: false}),
		WTF.field('_geoAddress', 'string', true, {persist: false})
	],
	
	setGeoData: function(json) {
		this.set({
			geoInfo: true,
			_geoFlagUrl: json.countryFlag,
			_geoContinentCode: json.continentCode,
			_geoContinentName: json.continentName,
			_geoCountryCode: json.countryCode,
			_geoCountryName: json.countryName,
			_geoRegionName: json.regionName,
			_geoCity: json.city,
			_geoZip: json.zip,
			_geoLat: json.latitude,
			_geoLong: json.longitude
		});
		/*
		this.set({
			geoInfo: true,
			_geoFlagUrl: json.location.country_flag,
			_geoContinentCode: json.continent_code,
			_geoContinentName: json.continent_name,
			_geoCountryCode: json.country_code,
			_geoCountryName: json.country_name,
			_geoRegionName: json.region_name,
			_geoCity: json.city,
			_geoZip: json.zip,
			_geoLat: json.latitude,
			_geoLong: json.longitude
		});
		*/
	},
	
	genGeoDescription: function() {
		var me = this;
		return me.get('geoInfo') !== true ? '' : Sonicle.String.join(', ', 
			me.get('_geoContinentName'),
			me.get('_geoCountryName'),
			me.get('_geoRegionName'),
			me.get('_geoCity')
		);
	},
	
	genGeoCountryFlagMarkup: function() {
		var me = this, s = '';
		if (me.get('geoInfo') === true) {
			var henc = Ext.String.htmlEncode,
					url = me.get('_geoFlagUrl'),
					country = me.get('_geoCountryCode');
			
			return '<img src="' + henc(url) + '" alt="' + henc(country) + '" width="16" data-qtip="' + henc(country) + '"/>';
		}
		return s;
	},
	
	toString: function() {
		var me = this,
				SoS = Sonicle.String;
		return SoS.join(', ',
			Ext.Date.format(me.get('timestamp'), 'Y-m-d H:i:s'),
			me.get('action'),
			me.get('ipAddress'),
			SoS.join(' ', me.get('_geoContinentCode'), me.get('_geoContinentName')),
			SoS.join(' ', me.get('_geoCountryCode'), me.get('_geoCountryName')),
			SoS.deflt(me.get('_geoRegionName'), ''),
			SoS.deflt(me.get('_geoCity'), ''),
			SoS.deflt(me.get('_geoZip'), ''),
			SoS.deflt(me.get('_geoLat'), ''),
			SoS.deflt(me.get('_geoLong'), '')
		);
	}
});
