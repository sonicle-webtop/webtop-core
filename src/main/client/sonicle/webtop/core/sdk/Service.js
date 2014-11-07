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
	mixins: [
		'Ext.mixin.Observable',
		'WT.sdk.mixin.ActionStorer'
	],
	
	statics: {
		NEW_ACTION_GROUP: 'new'
	},
	
	ID: null,
	XID: null,
	strings: null,
	tbcmp: null,
	toolcmp: null,
	maincmp: null,
	wsactions: null,
	wsscopes: null,
	
	/**
	 * @method
	 * Called automatically when the service is initialized by the framework.
	 */
	init: Ext.emptyFn,
	
	/**
	 * @event activate
	 * Fires after the Service has been activated.
	 */
	
	constructor: function(id, xid) {
		var me = this;
		me.ID = id;
		me.XID = xid;
		me.wsactions = {};
		me.wsscopes = {};
		me.callParent(arguments);
	},
	
	/**
	 * Gets the initial setting value bound to key.
	 * @param {String} key The key.
	 * @return {Mixed} Setting value.
	 */
	getInitialSetting: function(key) {
		var is = WTStartup.initialSettings[this.ID] || {};
		return is[key];
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
	
	/**
	 * Returns the toolbar component associated to this service.
	 * @return {Ext.Toolbar}
	 */
	getToolbar: function() {
		return this.tbcmp;
	},
	
	/**
	 * Sets the toolbar component associated to this service.
	 * @param {Ext.toolbar.Toolbar} cmp The toolbar.
	 */
	setToolbar: function(cmp) {
		if(cmp) this.tbcmp = cmp;
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
	 * @param {Ext.Panel} cmp The tool component.
	 */
	setToolComponent: function(cmp) {
		if(cmp) this.toolcmp = cmp;
	},
	
	/**
	 * Returns the main (center) component associated to this service.
	 * @return {Ext.Panel}
	 */
	getMainComponent: function() {
		return this.maincmp;
	},
	
	/**
	 * Sets the main (center) component associated to this service.
	 * @param {Ext.Component} cmp The main component.
	 */
	setMainComponent: function(cmp) {
		if(cmp) this.maincmp = cmp;
	},
	
	/**
	 * Adds an action into 'new' group.
	 * @param {String} name The action name.
	 * @param {Object/Ext.Action} obj Action instance or config.
	 * @return {WT.ux.Action} The Action that were added.
	 */
	addNewAction: function(name, obj) {
		return this.addAction(WT.sdk.Service.NEW_ACTION_GROUP, name, obj);
	},
	
	/**
	 * Checks if there are some actions under 'new' group.
	 * @return {Boolean} True if so.
	 */
	hasNewActions: function() {
		var acts = this.getActions(WT.sdk.Service.NEW_ACTION_GROUP);
		return !Ext.Object.isEmpty(acts);
	},
	
	/**
	 * Returns action into 'new' group.
	 * @return {Array} An array of actions.
	 */
	getNewActions: function() {
		var acts = this.getActions(WT.sdk.Service.NEW_ACTION_GROUP);
		return (acts) ? Ext.Object.getValues(acts) : [];
	},
	
	/**
	 * Builds CSS class name namespacing it using service xid.
	 * @param {String} name The CSS class name part.
	 * @return {String} The concatenated CSS class name.
	 */
	cssCls: function(name) {
		return WT.cssCls(this.XID, name);
	},
	
	/**
	 * Builds CSS class name for icons namespacing it using service xid.
	 * For example, using 'service' as name, it will return '{xid}-icon-service'.
	 * Using 'service-l' as name it will return '{xid}-icon-service-l'.
	 * Likewise, using 'service' as name and 'l' as size it will return the
	 * same value: '{xid}-icon-service-l'.
	 * @param {String} name The icon name part.
	 * @param {String} [size] Icon size (one of xs,s,m,l).
	 * @return {String} The concatenated CSS class name.
	 */
	cssIconCls: function(name, size) {
		return WT.cssIconCls(this.XID, name, size);
	},
	
	/*
	 * Builds the src url of a themed image for this service
	 * 
	 * @param {String} relPath The relative icon path
	 * @return {String} the imageUrl
	 */
	imageUrl: function(relPath) {
		return WT.imageUrl(this.ID, relPath);
	},
	
	/*
	 * Builds the img tag of a themed image for this service
	 * 
	 * @param {String} relPath The relative icon path
	 * @param {int} width The icon width
	 * @param {int} height The icon height
	 * @param {String} [others] other custom tag properties
	 * @return {String} the complete image tag
	 */
	imageTag: function(relPath,width,height,others) {
		return WT.imageTag(this.ID, relPath, width, height, others);
	},
	
	/*
	 * Map a websocket action name to a callback function
	 * @param {String} action The action name
	 * @param {Function} callback The function for this action
	 * @param {Object} scope The scope for this callback
	 */
	addWSAction: function(action, callback, scope) {
		this.wsactions[action] = callback;
		this.wsscopes[action] = scope;
	},
	
	/*
	 * Get the mapped function for a websocket action name
	 * The passed function will be called with a config object
	 * rapresenting the complete websocket message:
	 * 
	 *   {
	 *     service: [service-id],
	 *     action: [action-name],
	 *     ...[sepcific action data]...
	 *   }
	 *   
	 * @param {String} action the action name
	 * @return {Function} callback function for this action
	 */
	getWSAction: function(action) {
		return this.wsactions[action];
	},
	
	/**
	 * Callback for WebSocket messages arriving from the server.
	 * Finds the mapped action function and call it.
	 * @param {Object} cfg The message config object.
	 */
	websocketMessage: function(cfg) {
		var me  = this;
		console.log('MailService: received message with action '+cfg.action);
		var fn = me.wsactions[cfg.action];
		var scope = me.wsscopes[cfg.action] || me;
		Ext.callback(fn, scope, [cfg]);
	}
});
