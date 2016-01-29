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
	requires: [
		'WT.sdk.model.ClientOptions'
	],
	mixins: [
		'Ext.mixin.Observable',
		'WT.mixin.RefStorer',
		'WT.mixin.ActionStorer'
	],
	statics: {
		TOOLBAR_REF_NAME: 'tbcmp',
		TOOL_REF_NAME: 'toolcmp',
		MAIN_REF_NAME: 'maincmp',
		ACTION_GROUP_NEW: 'new',
		ACTION_GROUP_TOOLBOX: 'toolbox'
	},
	
	/**
	 * @property {String} ID
	 * Service ID.
	 */
	ID: null,
	
	/**
	 * @property {String} XID
	 * Service short ID.
	 */
	XID: null,
	
	/**
	 * @property {Object} perms
	 * A object carring user's permission.
	 */
	perms: null,
	
	/**
	 * @property {WT.sdk.model.ClientOptions} options
	 * A model representing service's options pushed at startup time.
	 */
	options: null,
	
	/**
	 * @private
	 * @property {Object} msgListeners
	 * A map that holds message listeners defined by service.
	 */
	msgListeners: null,
	
	/**
	 * @method
	 * Called automatically when the service is initialized by the framework.
	 * This is the principal hook point.
	 */
	init: Ext.emptyFn,
	
	/**
	 * @event activate
	 * Fires after the Service has been activated.
	 */
	
	constructor: function(cfg) {
		var me = this;
		me.ID = cfg.ID;
		me.XID = cfg.XID;
		me.msgListeners = {};
		me.initConfig(cfg);
		me.mixins.observable.constructor.call(me, cfg);
		me.mixins.refstorer.constructor.call(me, cfg);
		me.mixins.actionstorer.constructor.call(me, cfg);
		me.callParent(arguments);
		
		me.perms = cfg.permsData;
		delete cfg.permsData;
		
		// Creates options using configured model
		try {
			me.options = Ext.create(cfg.clientOptionsClassName, cfg.optionsData);
			
		} catch(err) {
			Ext.log.warn(Ext.String.format('Unable to instantiale specified model [{0}], using default one.', cfg.clientOptionsClassName));
			me.options = Ext.create('WT.sdk.model.ClientOptions', cfg.optionsData);
		}
	},
	
	/**
	 * Returns service's name.
	 * @return {String}
	 */
	getName: function() {
		return WT.getApp().getDescriptor(this.ID).getName();
	},
	
	/**
	 * Returns service's description.
	 * @return {String}
	 */
	getDescription: function() {
		return WT.getApp().getDescriptor(this.ID).getDescription();
	},
	
	preNs: function(cn) {
		return WT.preNs(WT.getApp().getDescriptor(this.ID).getNs(), cn);
	},
	
	/**
	 * Returns true if this service is currently activated (displayed)
	 * @return {Boolean}
	 */
	isActive: function() {
		return WT.getActiveService() === this.ID;
	},
	
	/**
	 * Checks against a resource if specified action is allowed.
	 * @param {String} resource The resource name.
	 * @param {String} action The action name.
	 * @return {Boolean} 'True' if action is allowed, 'False' otherwise.
	 */
	isPermitted: function(resource, action) {
		var r = this.perms[resource];
		return (r) ? (r[action] === true) : false;
	},
	
	/**
	 * Returns an option defined during startup set.
	 * Some built-in options are defined in model {@link WT.sdk.model.ClientOptions}.
	 * @param {String} key The option key.
	 * @param {Mixed} [ifEmpty] The fallback value
	 * @return {Mixed} The option value.
	 */
	getOption: function(key, ifEmpty) {
		var v = this.options.get(key);
		return (arguments.length === 2) ? WTU.deflt(v, ifEmpty) : v;
	},
	
	/**
	 * Returns an option as an object defined during startup set.
	 * Some built-in options are defined in model {@link WT.sdk.model.ClientOptions}.
	 * @param {String} key The option key.
	 * @return {Mixed} The option value object.
	 */
	getOptionAsObject: function(key) {
		return Ext.JSON.decode(this.getOption(key,'{}'));
	},
	
	
	/**
	 * Sets one of startup option set.
	 * Updates are only valid for client, no server sync is done using this method.
	 * @param {Object} opts Key/Value pairs object.
	 */
	setOptions: function(opts) {
		var me = this;
		opts = opts || {};
		me.options.beginEdit();
		Ext.iterate(opts, function(k, v) {
			me.options.set(k, v);
		});
		me.options.endEdit();
	},
	
	/**
	 * Returns the localized string associated to the key.
	 * Values arguments will be used to replace tokens in source string.
	 * @param {String} key The key.
	 * @param {Mixed...} [values] The values to use within {@link Ext.String#format} method.
	 * @return {String} The translated (formatted) string, or undefined if not found.
	 */
	res: function(key) {
		var me = this,
				eArr = Ext.Array;
		if(arguments.length === 1) {
			return WT.res(me.ID, key);
		} else {
			var args = eArr.slice(arguments, 1);
			return WT.res.apply(me, eArr.merge([me.ID, key], args));
		}
	},
	
	/**
	 * Returns the toolbar component associated to this service.
	 * @return {Ext.Toolbar}
	 */
	getToolbar: function() {
		return this.getRef(WT.sdk.Service.TOOLBAR_REF_NAME);
	},
	
	/**
	 * Sets the toolbar component associated to this service.
	 * @param {Ext.toolbar.Toolbar} cmp The toolbar.
	 */
	setToolbar: function(cmp) {
		if(cmp) this.addRef(WT.sdk.Service.TOOLBAR_REF_NAME, cmp);
	},
	
	/**
	 * Returns the tool (side) component associated to this service.
	 * @return {Ext.Component}
	 */
	getToolComponent: function() {
		return this.getRef(WT.sdk.Service.TOOL_REF_NAME);
	},
	
	/**
	 * Sets the tool (side) component associated to this service.
	 * @param {Ext.Panel} cmp The tool component.
	 * @return {Ext.Component} The added component.
	 */
	setToolComponent: function(cmp) {
		if(cmp) return this.addRef(WT.sdk.Service.TOOL_REF_NAME, cmp);
		return undefined;
	},
	
	/**
	 * Returns the main (center) component associated to this service.
	 * @return {Ext.Panel}
	 */
	getMainComponent: function() {
		return this.getRef(WT.sdk.Service.MAIN_REF_NAME);
	},
	
	/**
	 * Sets the main (center) component associated to this service.
	 * @param {Ext.Component} cmp The main component.
	 */
	setMainComponent: function(cmp) {
		if(cmp) this.addRef(WT.sdk.Service.MAIN_REF_NAME, cmp);
	},
	
	/**
	 * Adds an action into 'new' group.
	 * @param {String} name The action name.
	 * @param {Object/Ext.Action} obj Action instance or config.
	 * @return {WT.ux.Action} The Action that were added.
	 */
	addNewAction: function(name, obj) {
		return this.addAction(WT.sdk.Service.ACTION_GROUP_NEW, name, obj);
	},
	
	/**
	 * Checks if there are some actions under 'new' group.
	 * @return {Boolean} True if so.
	 */
	hasNewActions: function() {
		var acts = this.getActions(WT.sdk.Service.ACTION_GROUP_NEW);
		return !Ext.Object.isEmpty(acts);
	},
	
	/**
	 * Returns action into 'new' group.
	 * @return {Array} An array of actions.
	 */
	getNewActions: function() {
		var acts = this.getActions(WT.sdk.Service.ACTION_GROUP_NEW);
		return (acts) ? Ext.Object.getValues(acts) : [];
	},
	
	/**
	 * Returns action into 'new' group.
	 * @return {Array} An array of actions.
	 */
	getToolboxActions: function() {
		var acts = this.getActions(WT.sdk.Service.ACTION_GROUP_TOOLBOX);
		return (acts) ? Ext.Object.getValues(acts) : [];
	},
	
	/**
	 * Builds CSS class name namespacing it using service xid.
	 * @param {String} name The CSS class name part.
	 * @return {String} The concatenated CSS class name.
	 */
	cssCls: function(name) {
		return WTF.cssCls(this.XID, name);
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
		return WTF.cssIconCls(this.XID, name, size);
	},
	
	/*
	 * Builds the src url of a themed image for this service
	 * @param {String} relPath The relative icon path
	 * @return {String} the imageUrl
	 */
	imageUrl: function(relPath) {
		return WTF.resourceUrl(this.ID, relPath);
	},
	
	/**
	 * Shorthand for {@link WT.sdk.Service#addMessageListener}.
	 * @param {String} action The action name
	 * @param {Function} callback The function for this action
	 * @param {Object} scope The scope for this callback
	 */
	onMessage: function(action, fn, scope) {
		this.addMessageListener(action, fn, scope);
	},
	
	/*
	 * Maps a message action to a specific callback function.
	 * @param {String} action The action name
	 * @param {Function} callback The function for this action
	 * @param {Object} scope The scope for this callback
	 */
	addMessageListener: function(action, fn, scope) {
		this.msgListeners[action] = {
			fn: fn,
			scope: scope || this
		};
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
	//getMessageAction: function(action) {
	//	return this.wsactions[action];
	//},
	
	/**
	 * @private
	 * Callback for messages arriving from the server.
	 * It finds the mapped action function and calls it.
	 * @param {Object} msg The message data.
	 */
	handleMessage: function(msg) {
		var lis = this.msgListeners[msg.action];
		if(lis) Ext.callback(lis.fn, lis.scope, [msg]);
	}
});
