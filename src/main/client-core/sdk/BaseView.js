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
Ext.define('Sonicle.webtop.core.sdk.BaseView', {
	alternateClassName: 'WTA.sdk.BaseView',
	extend: 'Ext.panel.Panel',
	requires: [
		'Sonicle.form.Separator',
		'Sonicle.form.trigger.Clear',
		'Sonicle.plugin.NoAutocomplete'
	],
	mixins: [
		'Sonicle.mixin.RefHolder',
		'WTA.mixin.ActHolder',
		'WTA.mixin.PanelUtil',
		'WTA.mixin.Waitable'
	],
	
	layout: 'border',
	header: false,
	border: false,
	referenceHolder: true,
	
	/**
	 * @property {String/WTA.sdk.Service} mys
	 * Referenced service ID or direct service instance.
	 */
	mys: null,
	
	constructor: function(cfg) {
		var me = this;
		me.mixins.refholder.constructor.call(me, cfg);
		me.mixins.wtactholder.constructor.call(me, cfg);
		me.callParent([cfg]);
	},
	
	destroy: function() {
		var me = this;
		me.mixins.refholder.destroy.call(me);
		me.mixins.wtactholder.destroy.call(me);
		me.callParent();
	},
	
	/**
	 * Returns the localized string associated to the key.
	 * Values arguments will be used to replace tokens in source string.
	 * @param {String} key The resource key.
	 * @param {Mixed...} [values] The values to use within {@link Ext.String#format} method.
	 * @return {String} The localized (optionally formatted) resource value or '${key}' if not found.
	 */
	res: function(key) {
		var me = this;
		if (Ext.isString(me.mys)) {
			return WT.res.apply(me, [me.mys, key].concat(Ext.Array.slice(arguments, 1)));
		} else {
			return me.mys.res.apply(me, [key].concat(Ext.Array.slice(arguments, 1)));
		}
	}
});
