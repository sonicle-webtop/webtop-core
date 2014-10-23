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
Ext.define('Sonicle.webtop.core.sdk.Service', {
	alternateClassName: 'WT.sdk.Service',
	
	id: null,
	xid: null,
	
	/**
	 * @method
	 * Called automatically when the service is initialized by the framework.
	 */
	init: Ext.emptyFn,
	
	/**
	 * @method
	 * Called automatically when the service is activated.
	 */
	activate: Ext.emptyFn,
	
	strings: null,
	tb: null,
	toolcmp: null,
	maincmp: null,
	
	constructor: function(cfg) {
		var me = this;
		me.id = cfg.id;
		me.xid = cfg.xid;
		me.callParent(arguments);
	},
	
	/**
	 * Returns the toolbar component associated to this service.
	 * @return {Ext.Toolbar}
	 */
	getToolbar: function() {
		return this.tb;
	},
	
	/**
	 * Sets the toolbar component associated to this service.
	 * @param {Ext.Toolbar} cmp The toolbar.
	 */
	setToolbar: function(cmp) {
		if(Ext.isDefined(cmp)) {
			this.tb = cmp;
		}
	},
	
	/**
	 * Returns the tool (side) component associated to this service.
	 * @return {Ext.Component}
	 */
	getToolComponent: function() {
		return this.toolcmp;
	},
	
	/**
	 * Sets the tool (side) component associated to this service.
	 * @param {Ext.Component} cmp The tool component.
	 */
	setToolComponent: function(cmp) {
		if(Ext.isDefined(cmp)) {
			this.toolcmp = cmp;
		}
	},
	
	/**
	 * Returns the main (center) component associated to this service.
	 * @return {Ext.Component}
	 */
	getMainComponent: function() {
		return this.maincmp;
	},
	
	/**
	 *  Sets the main (center) component associated to this service.
	 * @param {Ext.Component} cmp The main component.
	 */
	setMainComponent: function(cmp) {
		if(Ext.isDefined(cmp)) {
			this.maincmp = cmp;
		}
	},
	
	/**
	 * Returns the localized string associated to the key.
	 * @param {String} key The key.
	 * @return {String} The translated string, or null if not found.
	 */
	res: function(key) {
		if(!this.strings) return undefined;
		return this.strings[key];
	},
	
	cssIcon: function(name, size) {
		if(arguments.length === 2) {
			return Ext.String.format('{0}-ico-{1}-{2}', this.xid, name, size);
		} else {
			return Ext.String.format('{0}-ico-{1}', this.xid, name);
		}
	}
});
