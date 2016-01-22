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
Ext.define('Sonicle.webtop.core.mixin.ActionStorer', {
	alternateClassName: 'WT.mixin.ActionStorer',
	extend: 'Ext.Mixin',
	mixinConfig: {
		id: 'actionstorer'
		/*
		extended: function (baseClass, derivedClass, classBody) {
			classBody._actions = {};
		}
		*/
	},
	
	DEFAULT_GROUP: 'default',
	_actions: null,
	
	constructor: function(cfg) {
		this._actions = {};
	},
	
	destroy: function() {
		this._actions = null;
	},
	
	/**
	 * Creates an Action instance.
	 * @param {String} name The action name.
	 * @param {Object} obj Action config.
	 * @returns {WT.ux.Action} The Action just created.
	 */
	createAction: function(name, obj) {
		var me = this;
		/*
		var txt = Ext.isDefined(obj.text) ? obj.txt : me._buildText(obj.ID, name),
				tip = Ext.isDefined(obj.tooltip) ? obj.tooltip : me._buildTip(obj.ID, name);
		delete obj.ID;
		delete obj.XID;
		*/
		/*
		act = Ext.create('WT.ux.Action', Ext.applyIf({
			text: Ext.isDefined(obj.text) ? obj.text : me._buildText(null, name),
			tooltip: Ext.isDefined(obj.tooltip) ? obj.tooltip : me._buildTip(null, name),
			iconCls: Ext.isDefined(obj.iconCls) ? obj.iconCls : me._buildIconCls(name),
			handler: obj.handler,
			scope: obj.scope || this
		}, obj));
		*/
		var txt = Ext.isDefined(obj.text) ? obj.text : me._buildText(null, name),
				tip = Ext.isDefined(obj.tooltip) ? obj.tooltip : me._buildTip(null, name),
				cls = Ext.isDefined(obj.iconCls) ? obj.iconCls : me._buildIconCls(name),
				cb = obj.handler,
				sco = obj.scope || this;

		delete obj.text;
		delete obj.tooltip;
		delete obj.iconCls;
		delete obj.handler;
		delete obj.scope;

		return Ext.create('WT.ux.Action', Ext.apply({
			text: txt,
			tooltip: tip,
			iconCls: cls,
			handler: cb,
			scope: sco || this
		}, obj));
	},
	
	/**
	 * Adds an action into the specified group.
	 * @param {String} [group] The action group.
	 * @param {String} name The action name.
	 * @param {Object/Ext.Action} obj Action instance or config.
	 * @return {WT.ux.Action} The Action that were added.
	 */
	addAction: function(group, name, obj) {
		var me = this, act;
		if(arguments.length === 2) {
			obj = name;
			name = group;
			group = me.DEFAULT_GROUP;
		}
		if(!me._actions[group]) me._actions[group] = {};
		
		act = WTU.isAction(obj) ? obj : me.createAction(name, obj);
		me._actions[group][name] = act;
		return act;
	},
	
	/**
	 * Gets an action from the specified group.
	 * If not provided, 'default' group is used.
	 * @param {String} [group] The action group.
	 * @param {String} name The action name.
	 * @return {WT.ux.Action} The action.
	 */
	getAction: function(group, name) {
		var me = this;
		if(arguments.length === 1) {
			name = group;
			group = me.DEFAULT_GROUP;
		}
		if(!me._actions[group]) return undefined;
		return me._actions[group][name];
	},
	
	/**
	 * Gets all actions belonging to group.
	 * @param {String} group The action group.
	 * @returns {Object} The actions map.
	 */
	getActions: function(group) {
		return this._actions[group];
	},
	
	/**
	 * Sets disabled state for an action from the specified group.
	 * If not provided, 'default' group is used.
	 * @param {String} [group] The action group.
	 * @param {String} name The action name.
	 * @param {Boolean} disabled The disabled state.
	 * @return {WT.ux.Action} The action.
	 */
	setActionDisabled: function(group, name, disabled) {
		var me = this, act;
		if(arguments.length === 2) {
			disabled = name;
			name = group;
			group = me.DEFAULT_GROUP;
		}
		act = me.getAction(group, name);
		if(act) act.setDisabled(disabled);
	},
	
	/**
	 * @private
	 * Builds text value
	 */
	_buildText: function(id, name) {
		id = id || this._guessSvcId();
		return WT.res(id, Ext.String.format('act-{0}.lbl', name));
	},
	
	/**
	 * @private
	 * Builds tooltip value
	 */
	_buildTip: function(id, name) {
		id = id || this._guessSvcId();
		return WT.res(id, Ext.String.format('act-{0}.tip', name));
	},
	
	/**
	 * @private
	 * Builds icon class
	 */
	_buildIconCls: function(name) {
		return WTF.cssIconCls(this._guessSvcXId(), name, 'xs');
	},
	
	/**
	 * @private
	 * Tries to guess service ID
	 */
	_guessSvcId: function() {
		var me = this;
		if(me.mys && me.mys.ID) return me.mys.ID;
		if(me.ID) return me.ID;
		return null;
	},
	
	/**
	 * @private
	 * Tries to guess service XID
	 */
	_guessSvcXId: function() {
		var me = this;
		if(me.mys && me.mys.XID) return me.mys.XID;
		if(me.XID) return me.XID;
		return null;
	}
});
