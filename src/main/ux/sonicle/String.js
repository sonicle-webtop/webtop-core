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
Ext.define('Sonicle.String', {
	singleton: true,
	
	/**
	 * 
	 * @param {type} sep 
	 * @param {Mixed...} values String values to join.
	 * @returns {String} The joined string.
	 */
	join: function(sep, values) {
		sep = sep || '';
		var i, s = '';
		for(i=1; i<arguments.length; i++) {
			if(Ext.isEmpty(arguments[i])) continue;
			s = s.concat(arguments[i] || '', (i === arguments.length-1) ? '' : sep);
		}
		return Ext.String.trim(s);
	},
	
	coalesce: function(values) {
		for(var i=0; i<arguments.length; i++) {
			if((arguments[i] !== null) && (arguments[i] !== undefined)) return arguments[i];
		}
		return null;
	},
	
	/*
	 * Converts passed value in bytes in a human readable format.(eg. like '10 KB' or '100 MB')
	 * @param {int} bytes The value in bytes
	 * @param {Boolean} [opts.si] Whether to use the SI multiple (1000) or binary one (1024)
	 * @param {Boolean} [opts.siUnits] Whether to use the SI units labels or binary ones
	 * @param {String} [opts.unitSeparator] Separator to use between value and unit
	 * @return {String} The formatted string
	 */
	humanReadableSize: function(bytes, opts) {
		opts = opts || {};
		opts.unitSeparator = opts.unitSeparator || ' ';
		var thresh = (opts.si) ? 1000 : 1024,
				units = (opts.siUnits) ? ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'] : ['kB','MB','GB','TB','PB','EB','ZB','YB'],
				u;
		if(Math.abs(bytes) < thresh) return bytes + opts.unitSeparator + 'B';
		
		u = -1;
		do {
			bytes /= thresh;
			++u;
		} while(Math.abs(bytes) >= thresh && u < units.length - 1);
		return bytes.toFixed(1) + opts.unitSeparator + units[u];
		
		/*
		var s = bytes;
		bytes = parseInt(bytes/1024);
		if(bytes > 0) {
			if(bytes < 1024) {
				s = bytes + "KB";
			} else {
				s = parseInt(bytes/1024) + "MB";
			}
		}
		return s;
		*/
	}
});
