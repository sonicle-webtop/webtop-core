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
Ext.define('Sonicle.webtop.core.sdk.UserOptionsView', {
	alternateClassName: 'WTA.sdk.UserOptionsView',
	extend: 'Ext.tab.Panel',
	mixins: [
		'WTA.mixin.ActHolder',
		'WTA.mixin.PanelUtil',
		'WTA.mixin.Waitable',
		'WTA.mixin.HasModel'
	],
	requires: [
		'Sonicle.form.Spacer'
	],
	
	referenceHolder: true,
	tabPosition: 'left',
	tabRotation: 0,
	closable: false,
	tabConfig: {
		textAlign: 'left'
	},
	modelValidation: true,
	
	modelIdProperty: 'id',
	viewModel: {},

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
	
	profileId: null,
	needLogin: false,
	needReload: false,
	
	constructor: function(cfg) {
		var me = this;
		me.mixins.wtactholder.constructor.call(me, cfg);
		me.callParent([cfg]);
	},
	
	destroy: function() {
		var me = this;
		me.mixins.wtactholder.destroy.call(me);
		me.callParent();
	},
	
	onBlurAutoSave: function(s) {
		var me = this, name, model=me.getModel();
		
		if (!model) return;
		
		name = me._extrField(s.getInitialConfig().bind);
		if(s.needLogin || s.needReload) {
			if(me.getModel().isModified(name)) {
				if(s.needLogin) me.needLogin = true;
				if(s.needReload) me.needReload = true;
			}
		}
		if(model.dirty) {
			me.saveModel({
				callback: function(success,model) {
					if (success && me.profileId===WT.getVar('profileId')) WT.setVar(me.ID, name, model.get(name));
				}
			});
		}
	},
	
	/**
	 * @private
	 * Extract the field name from a binding string ('{record.id}' -> 'id')
	 */
	_extrField: function(bind) {
		var valBind = Ext.isString(bind) ? bind : bind.value;
		return valBind.substring(1, valBind.length-1).replace(this.getModelProperty()+'.', '');
	}
});
