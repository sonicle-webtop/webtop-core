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
Ext.define('Sonicle.webtop.core.sdk.mixin.ActionStorer', {
	alternateClassName: 'WT.sdk.mixin.ActionStorer',
	extend: 'Ext.Mixin',
	
	DEFAULT_GROUP: 'default',
	actions_: null,
	
	mixinConfig: {
		extended: function (baseClass, derivedClass, classBody) {
			classBody.actions_ = {};
		}
	},
	
	/**
	 * Adds an action into the specified group.
	 * @param {String} [group] The action group.
	 * @param {String} name The action name.
	 * @param {type} obj
	 * @return {WT.ux.Action}
	 */
	addAction: function(group, name, obj) {
		var me = this;
		if(arguments.length === 2) {
			obj = name;
			name = group;
			group = me.DEFAULT_GROUP;
		}
		if(!me.actions_[group]) me.actions_[group] = {};
		
		var act = null;
		if(WT.isAction(obj)) { // Action is already instantiated
			act = obj;
		} else { // Instantiate action using config
			var cfg = {
				handler: obj.handler
			};
			if(obj.text) cfg.text = obj.text;
			if(obj.iconCls) cfg.iconCls = obj.iconCls;
			if(obj.scope) cfg.scope = obj.scope;
			act = Ext.create('WT.ux.Action', cfg);
		}
		me.actions_[group][name] = act;
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
		if(!me.actions_[group]) return undefined;
		return me.actions_[group][name];
	},
	
	/**
	 * Gets all actions belonging to group.
	 * @param {String} group The action group.
	 * @returns {Object} The actions map.
	 */
	getActions: function(group) {
		return this.actions_[group];
	}
});
