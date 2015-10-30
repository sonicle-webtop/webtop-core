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
Ext.define('Sonicle.plugin.EnterKeyPlugin', {
	extend: 'Ext.AbstractPlugin',
	alias: 'plugin.soenterkeyplugin',
	
	/**
	 * @property
	 * @readonly
	 * Field on which this plugin is bounded to
	 */
	field: null,
	
	/**
	 * @property
	 * @readonly
	 * Convenient flag that indicates if bounded field is a picker
	 */
	isPicker: false,
	
	/**
	 * @event enterkey
	 * Fires when the ENTER confirmation key is pressed on the field.
	 * @param {Ext.form.field.Text} field The field that generates the event
	 * @param {Ext.event.Event} e The original event object
	 */
	
	init: function(cmp) {
		var me = this;
		me.field = cmp;
		me.isPicker = cmp.isXType('pickerfield');
		cmp.on('specialkey', me.onSpecialKey, me);
	},
	
	destroy: function() {
		var me = this;
		me.field.un('specialkey', me.onSpecialKey, me);
		me.field = null;
	},
	
	onSpecialKey: function(s, e) {
		var me = this;
		if(e.getKey() === e.ENTER) {
			// If field is picker, prevent default behaviour if list is expanded
			if(me.isPicker && me.field.isExpanded) return;
			me.field.fireEvent('enterkey', s, e);
		}
	}
});
