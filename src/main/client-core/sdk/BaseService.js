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
Ext.define('Sonicle.webtop.core.sdk.BaseService', {
	mixins: [
		'Ext.mixin.Observable',
		'Sonicle.mixin.RefHolder',
		'WTA.mixin.ActHolder'
	],
	requires: [
		'Sonicle.String'
	],
	
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
	 * @property {WTA.sdk.model.ServiceVars} options
	 * A model representing service's options pushed at startup time.
	 */
	vars: null,
	
	/**
	 * @method
	 * Called automatically when the service is initialized by the framework.
	 * This is the principal hook point.
	 */
	init: Ext.emptyFn,
	
	/**
	 * Override to return the Service API instance.
	 * @param {String} id The service ID.
	 * @returns {Object} The service API object or null if service is not valid.
	 */
	getApiInstance: Ext.emptyFn,
	
	/**
	 * @method
	 * Called automatically when receiving autosave data.
	 */
	autosaveRestore: Ext.emptyFn,
	
	notificationCallback: Ext.emptyFn,
	
	constructor: function(cfg) {
		var me = this;
		me.ID = cfg.ID;
		me.XID = cfg.XID;
		me.initConfig(cfg);
		me.mixins.observable.constructor.call(me, cfg);
		me.mixins.refholder.constructor.call(me, cfg);
		me.mixins.wtactholder.constructor.call(me, cfg);
		me.callParent(arguments);
	},
	
	destroy: function() {
		var me = this;
		me.mixins.refholder.destroy.call(me);
		me.mixins.wtactholder.destroy.call(me);
		me.callParent();
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
	
	/**
	 * Prepends service's namespace to passed class name.
	 * @param {String} cn The class name.
	 * @return {String}
	 */
	preNs: function(cn) {
		return WT.preNs(WT.getApp().getDescriptor(this.ID).getNs(), cn);
	},
	
	/**
	 * Builds a namespaced {@link Ext.state.Stateful#stateId state ID} for state management purposes.
	 * @param {String} name The component or unique reference name.
	 * @return {String} The state ID
	 */
	buildStateId: function(name) {
		return WT.buildStateId(this.XID, name);
	},
	
	/**
	 * Clears (if possible) all saved state data related to this service.
	 */
	clearState: function() {
		var XSM = Ext.state.Manager,
				prov = XSM.getProvider(),
				prefix = this.XID + '-';
		
		if (prov.store) {
			Ext.iterate(prov.store.getKeys(), function(key) {
				if (Ext.String.startsWith(key, prefix)) XSM.clear(key);
			});
		} else {
			Ext.log.warn("Clearing is not supported on current state provider");
		}
	},
	
	/**
	 * Returns an option defined during startup set.
	 * Some built-in options are defined in model {@link WTA.sdk.model.BaseServiceVars}.
	 * @param {String} key The option key.
	 * @param {Mixed} [ifEmpty] The fallback value
	 * @return {Mixed} The option value.
	 */
	getVar: function(key, ifEmpty) {
		var v = this.vars.get(key);
		return (arguments.length === 2) ? Sonicle.String.deflt(v, ifEmpty) : v;
	},
	
	/**
	 * Returns an option as an object defined during startup set.
	 * Some built-in options are defined in model {@link WTA.sdk.model.BaseServiceVars}.
	 * @param {String} key The option key.
	 * @return {Mixed} The option value object.
	 */
	getVarAsObject: function(key) {
		return Ext.JSON.decode(this.getVar(key,'{}'));
	},
	
	/**
	 * Sets one of startup option set.
	 * Updates are only valid for client, no server sync is done using this method.
	 * @param {Object} opts Key/Value pairs object.
	 */
	setVar: function(key, value) {
		var o = {};
		o[key] = value;
		this.setVars(o);
	},
	
	/**
	 * Sets one of startup option set.
	 * Updates are only valid for client, no server sync is done using this method.
	 * @param {Object} opts Key/Value pairs object.
	 */
	setVars: function(opts) {
		var me = this;
		opts = opts || {};
		me.vars.beginEdit();
		Ext.iterate(opts, function(k, v) {
			if (me.vars.get(k) === 'undefined') return;
			me.vars.set(k, v);
		});
		me.vars.endEdit();
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
		if(arguments.length === 1) {
			return WT.res(me.ID, key);
		} else {
			return WT.res.apply(me, [me.ID, key].concat(Ext.Array.slice(arguments, 1)));
		}
	},
	
	/**
	 * Returns the localized string associated to the passed key template.
	 * @param {String} key The resource key template.
	 * @return {String} The localized string or template itself if parsing goes wrong.
	 */
	resTpl: function(tpl) {
		return WT.resTpl(this.ID, tpl);
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
	 * Builds the URL of a resource file this service.
	 * @param {String} relPath The relative resource path.
	 * @return {String} The URL
	 */
	fileUrl: function(relPath) {
		return WTF.fileUrl(this.ID, relPath);
	},
	
	/*
	 * Builds the URL of a themed resource file this service.
	 * @param {String} relPath The relative resource path.
	 * @return {String} The URL
	 */
	resourceUrl: function(relPath) {
		return WTF.resourceUrl(this.ID, relPath);
	},
	
	//private
	privateInit: function() {
		var me = this;
		me.onPushMessage('autosaveRestore', me._onAutosaveRestore, me);
	},
	
	privates: {
		_onAutosaveRestore: function(msg) {
			var me = this,
				pl = msg.payload,
				ret = me.autosaveRestore(pl.value);

			if (ret === true) {
				WT.ajaxReq(WT.ID, 'RemoveAutosave', {
					params: {
						webtopClientId: pl.webtopClientId,
						serviceId: pl.serviceId,
						context: pl.context,
						key: pl.key
					},
					callback: function(success, json) {
						WT.handleError(success, json);
					}
				});
			}
		}
	}
});
