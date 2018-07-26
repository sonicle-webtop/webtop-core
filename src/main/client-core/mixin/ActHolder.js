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
Ext.define('Sonicle.webtop.core.mixin.ActHolder', {
	alternateClassName: 'WTA.mixin.ActHolder',
	extend: 'Sonicle.mixin.ActHolder',
	
	mixinConfig: {
		id: 'wtactholder'
	},
	
	/**
	 * Gets an action and wraps it into the specified component.
	 * Group can be specified setting name as: 'name@group'.
	 * If not provided, 'default' group is used.
	 * @param {String} name The action name.
	 * @param {button|menuitem} xtype The component XType.
	 * @param {Object} cfg An optional config to be applied to the component.
	 * @return {Ext.Component} The component.
	 */
	getActAs: function(name, xtype, cfg) {
		var act = this.getAct(name), obj;
		if (act) {
			obj = Ext.create('widget.'+xtype, act);
			if (cfg) obj.setConfig(cfg);
			return obj;
		} else {
			return null;
		}
	},
	
	/**
	 * Creates an Action instance.
	 * @param {String} group The action group.
	 * @param {String} name The action name.
	 * @param {Object} cfg The action config.
	 * @returns {WTA.ux.Action} The Action just created.
	 */
	createAct: function(group, name, cfg) {
		var me = this;
		/*
		var txt = Ext.isDefined(obj.text) ? obj.txt : me._buildText(obj.ID, name),
				tip = Ext.isDefined(obj.tooltip) ? obj.tooltip : me._buildTip(obj.ID, name);
		delete obj.ID;
		delete obj.XID;
		*/
		/*
		act = Ext.create('WTA.ux.Action', Ext.applyIf({
			text: Ext.isDefined(obj.text) ? obj.text : me._buildText(null, name),
			tooltip: Ext.isDefined(obj.tooltip) ? obj.tooltip : me._buildTip(null, name),
			iconCls: Ext.isDefined(obj.iconCls) ? obj.iconCls : me._buildIconCls(name),
			handler: obj.handler,
			scope: obj.scope || this
		}, obj));
		*/
		var txt = Ext.isDefined(cfg.text) ? cfg.text : me._buildText(null, name),
				tip = Ext.isDefined(cfg.tooltip) ? cfg.tooltip : me._buildTip(null, name),
				cls = Ext.isDefined(cfg.iconCls) ? cfg.iconCls : me._buildIconCls(name, cfg.ignoreSize),
				cb = cfg.handler,
				sco = cfg.scope || this;

		delete cfg.text;
		delete cfg.tooltip;
		delete cfg.iconCls;
		delete cfg.handler;
		delete cfg.scope;

		return Ext.create('WTA.ux.Action', Ext.apply({
			text: txt,
			tooltip: tip,
			iconCls: cls,
			handler: cb,
			scope: sco || this
		}, cfg));
	},
	
	/**
	 * Sets disabled state for an action from the specified group.
	 * If not provided, 'default' group is used.
	 * @param {String} [group] The action group.
	 * @param {String} name The action name.
	 * @param {Boolean} disabled The disabled state.
	 * @return {WTA.ux.Action} The action.
	 */
	setActDisabled: function(group, name, disabled) {
		var me = this, act;
		if(arguments.length === 2) {
			disabled = name;
			name = group;
			group = Sonicle.mixin.ActHolder.DEFAULT_GROUP;
		}
		act = me.getAct(group, name);
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
	_buildIconCls: function(name, ignoreSize) {
		return (ignoreSize === true) ? WTF.cssIconCls(this._guessSvcXId(), name) : WTF.cssIconCls(this._guessSvcXId(), name, 'xs');
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
