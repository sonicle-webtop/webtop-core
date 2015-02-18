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
Ext.define('Sonicle.form.field.Palette', {
	extend: 'Ext.form.field.Picker',
	alias: ['widget.sopalettefield'],
	
	editable: false,
	regex: /^\#[0-9A-F]{6}$/i,
	invalidText: "Colors must be in hex format (like #FFFFFF)",
	matchFieldWidth: false,
	
	/**
	 * @property {String[]} colors
	 * An array of 6-digit color hex code strings (without the # symbol). This array can contain any number of colors,
	 * and each hex code should be unique. You can override individual colors if needed.
	 * Defaults to.
	 */
	colors: null,
	
	afterRender: function() {
		var me = this;
		me.callParent();
		me.updateColor(me.value);
	},
	
	setValue: function(color) {
		var me = this;
		me.callParent(arguments);
		me.updateColor(color);
	},
	
	updateColor: function(color) {
		var el = this.inputEl;
		if(el && !Ext.isEmpty(color)) {
			el.setStyle({
				color: color,
				backgroundColor: color,
				boxShadow: '0px 0px 0px 1px white inset'
			});
		}
	},
	
	createPicker: function() {
		var me = this, cfg = {};
		if(Ext.isArray(me.colors)) {
			cfg = Ext.apply(cfg, {
				colors: me.colors
			});
		}
		return Ext.create(Ext.apply(cfg, {
			xtype: 'colorpicker',
			pickerField: me,
			floating: true,
			focusable: false, // Key events are listened from the input field which is never blurred
			minWidth: 195,
			maxWidth: 195,
			listeners: {
				select: function() {
					me.collapse();
				}
			}
		}));
	},
			
	onExpand: function() {
		var value = this.getValue();
		if(value) this.picker.select(value, true);
	},
	
	onCollapse: function() {
		// Picker does not prepend #, let's add it!
		this.setValue('#'+this.picker.getValue());
	}
});
