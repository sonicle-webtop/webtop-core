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
	extend: 'Sonicle.webtop.core.sdk.BaseService',
	alternateClassName: 'WTA.sdk.Service',
	requires: [
		'WTA.sdk.model.ServiceVars'
	],
	statics: {
		TOOLBAR_REF_NAME: 'tbcmp',
		TOOL_REF_NAME: 'toolcmp',
		MAIN_REF_NAME: 'maincmp',
		ACTION_GROUP_NEW: 'new',
		ACTION_GROUP_TOOLBOX: 'toolbox',
		PUSHMSG_PREFIX: 'pmsg-'
	},
	
	/**
	 * @property {Object} perms
	 * A object carring user's permission.
	 */
	perms: null,
	
	/**
	 * @private
	 * @property {Number} activationCount
	 * Nuumber of activations of this service.
	 */
	activationCount: 0,
	
	/**
	 * @event activate
	 * Fires after the Service has been activated.
	 */
	
	/**
	 * @event beforeunload
	 * Fires before unloading the entire application window.
	 * Returning `false` from an event listener can halt the unloading process.
	 */
	
	constructor: function(cfg) {
		var me = this;
		me.callParent(arguments);
		
		me.perms = cfg.permsData;
		delete cfg.permsData;
		
		// Creates options using configured model
		try {
			me.vars = Ext.create(cfg.serviceVarsClassName, cfg.varsData);
			
		} catch(err) {
			Ext.log.warn(Ext.String.format('Unable to instantiale specified model [{0}], using default one.', cfg.serviceVarsClassName));
			me.options = Ext.create('WTA.sdk.model.ServiceVars', cfg.varsData);
		}
	},
	
	/**
	 * Returns true if this service is currently activated (displayed)
	 * @return {Boolean}
	 */
	isActive: function() {
		return WT.getActiveService() === this.ID;
	},
	
	getActivationCount: function() {
		return this.activationCount;
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
	 * Returns the toolbar component associated to this service.
	 * @return {Ext.Toolbar}
	 */
	getToolbar: function() {
		return this.getRef(WTA.sdk.Service.TOOLBAR_REF_NAME);
	},
	
	/**
	 * Sets the toolbar component associated to this service.
	 * @param {Ext.toolbar.Toolbar} cmp The toolbar.
	 */
	setToolbar: function(cmp) {
		if(cmp) this.addRef(WTA.sdk.Service.TOOLBAR_REF_NAME, cmp);
	},
	
	/**
	 * Returns the tool (side) component associated to this service.
	 * @return {Ext.Component}
	 */
	getToolComponent: function() {
		return this.getRef(WTA.sdk.Service.TOOL_REF_NAME);
	},
	
	/**
	 * Sets the tool (side) component associated to this service.
	 * @param {Ext.Panel} cmp The tool component.
	 * @return {Ext.Component} The added component.
	 */
	setToolComponent: function(cmp) {
		if(cmp) return this.addRef(WTA.sdk.Service.TOOL_REF_NAME, cmp);
		return undefined;
	},
	
	/**
	 * Returns the main (center) component associated to this service.
	 * @return {Ext.Panel}
	 */
	getMainComponent: function() {
		return this.getRef(WTA.sdk.Service.MAIN_REF_NAME);
	},
	
	/**
	 * Sets the main (center) component associated to this service.
	 * @param {Ext.Component} cmp The main component.
	 */
	setMainComponent: function(cmp) {
		if(cmp) this.addRef(WTA.sdk.Service.MAIN_REF_NAME, cmp);
	},
	
	/**
	 * Adds an action into 'new' group.
	 * @param {String} name The action name.
	 * @param {Object/Ext.Action} obj Action instance or config.
	 * @return {WTA.ux.Action} The Action that were added.
	 */
	addNewAction: function(name, obj) {
		return this.addAct(WTA.sdk.Service.ACTION_GROUP_NEW, name, obj);
	},
	
	/**
	 * Checks if there are some actions under 'new' group.
	 * @return {Boolean} True if so.
	 */
	hasNewActions: function() {
		var acts = this.getActs(WTA.sdk.Service.ACTION_GROUP_NEW);
		return !Ext.Object.isEmpty(acts);
	},
	
	/**
	 * Returns action into 'new' group.
	 * @return {Array} An array of actions.
	 */
	getNewActions: function() {
		var acts = this.getActs(WTA.sdk.Service.ACTION_GROUP_NEW);
		return (acts) ? Ext.Object.getValues(acts) : [];
	},
	
	/**
	 * Returns action into 'new' group.
	 * @return {Array} An array of actions.
	 */
	getToolboxActions: function() {
		var acts = this.getActs(WTA.sdk.Service.ACTION_GROUP_TOOLBOX);
		return (acts) ? Ext.Object.getValues(acts) : [];
	},
	
	/**
	 * Performs cleanup of uploaded file that have specified tag value.
	 * @param {String} tag Reference value
	 */
	cleanupUploadedFiles: function(tag) {
		WT.ajaxReq(this.ID, 'CleanupUploadedFiles', {
			params: {
				tag: tag
			}
		});
	},
	
	/**
	 * @deprecated use {@link #onPushMessage} instead
	 * Shorthand for {@link #addPushMessageListener}.
	 * @inheritdoc WTA.sdk.Service#addPushMessageListener
	 */
	onMessage: function(action, fn, scope, options) {
		Ext.log.warn(Ext.String.format("[WT.{0}] Method \'onMessage\' is deprecated. Use \'onPushMessage\' instead.", Sonicle.String.substrAfterLast(this.ID, '.')));
		return this.addPushMessageListener(action, fn, scope, options);
	},
	
	/**
	 * Shorthand for {@link #addPushMessageListener}.
	 * @inheritdoc WTA.sdk.Service#addPushMessageListener
	 */
	onPushMessage: function(action, fn, scope, options) {
		return this.addPushMessageListener(action, fn, scope, options);
	},
	
	/**
	 * Shorthand for {@link #removePushMessageListener}.
	 * @inheritdoc WTA.sdk.Service#removePushMessageListener
	 */
	unPushMessage: function(action, fn, scope) {
		this.removePushMessageListener(action, fn, scope);
	},
	
	/**
	 * Appends an handler to this object that listen for push-messages.
	 * @param {String} action The push action name.
	 * @param {Function/String} [fn] The method the event invokes or the *name* of the method within the specified `scope`.
	 * @param {Object} [scope] The scope (`this` reference) in which the handler function is executed.
	 * @param {Object} [options] An object containing handler configuration.
	 */
	addPushMessageListener: function(action, fn, scope, options) {
		return this.addListener(WTA.sdk.Service.PUSHMSG_PREFIX + action, fn, scope, options);
		//this.addListener('msg-' + action, fn, scope, options);
	},
	
	/**
	 * Removes an handler that was listening for push-messages.
	 * @param {String} action The push action name.
	 * @param {Function} fn The handler function originally specified.
	 * @param {type} scope  The scope originally specified for the handler.
	 */
	removePushMessageListener: function(action, fn, scope) {
		this.removeListener(WTA.sdk.Service.PUSHMSG_PREFIX + action, fn, scope);
		//this.removeListener('msg-' + action, fn, scope);
	},
	
	/**
	 * Builds the event name for push messages: the name is built prefixing the
	 * action of the push message with 'msg-', making it a classic event name
	 * that can be used with observable objects.
	 * Free feel to override this to implement custom naming logic.
	 * @param {Object} msg The message object:
	 * @param {Object} msg.service Service ID.
	 * @param {Object} msg.action Message action.
	 * @param {Object} msg.payload Message payload.
	 * @returns {String} Event name.
	 */
	buildPushMessageEventName: function(msg) {
		return WTA.sdk.Service.PUSHMSG_PREFIX + msg.action;
		//return 'msg-' + msg.action;
	},
	
	/**
	 * 
	 * Framework callback for notifying messages arriving from server.
	 * @param {Object} msg The message object:
	 * @param {Object} msg.service Service ID.
	 * @param {Object} msg.action Message action.
	 * @param {Object} msg.payload Message payload.
	 */
	handlePushMessage: function(msg) {
		this.fireEventArgs(this.buildPushMessageEventName(msg), [msg, msg.payload]);
	}
});
