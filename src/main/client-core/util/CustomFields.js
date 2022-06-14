/* 
 * Copyright (C) 2022 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.app.util.CustomFields', {
	singleton: true,
	alternateClassName: ['WTA.util.CustomFields'],
	requires: [
		'Sonicle.String',
		'Sonicle.Utils'
	],
	
	/**
	 * 
	 * @param {String[]|String} tags Array or pipe-separated list of WebTop's tag IDs.
	 * @param {Boolean} cfInitialData
	 * @param {Object} opts An object containing configuration.
	 * @param {String} opts.serviceId The service ID to perform request against to.
	 * @param {String} [opts.actionName] The action name to call. Defaults to 'GetCustomFieldsDefsData'.
	 * @param {String} [opts.idParam] The entity ID param name. Defaults to 'id'.
	 * @param {String} [opts.idField] The entity ID field name to get the ID value above. Defaults to Model's ID field.
	 * @param {Ext.data.Model} opts.model The main data-model.
	 * @param {Sonicle.webtop.core.ux.panel.CustomFieldsBase} opts.cfPanel The Custom Fields panel.
	 */
	reloadCustomFields: function(tags, cfInitialData, opts) {
		opts = opts || {};
		var SoO = Sonicle.Object,
			cfpnl = opts.cfPanel,
			mo = opts.model,
			entityId = mo.phantom ? null : (Ext.isString(opts.idField) ? mo.get(opts.idField) : mo.getId());
		
		cfpnl.wait();
		this.getCustomFieldsDefsData(tags, entityId, {
			serviceId: opts.serviceId,
			idParam: opts.idParam,
			callback: function(success, json) {
				if (success) {
					// Extracts name property from definition object and builds a name->id map 
					var sto = mo.cvalues();
					Ext.iterate(json.data.cvalues, function(cval) {
						var rec = sto.getById(cval.id);
						if (!rec) {
							rec = sto.add(cval)[0];
						} else {
							rec.set(cval);
						}
					});
					if (Ext.isObject(cfInitialData)) {
						var fmap = SoO.swapKV(SoO.pluck((Ext.JSON.decode(json.data.cfdefs, true) || {}).fields, 'name'));
						Ext.iterate(cfInitialData, function(key, value) {
							var rec = sto.getById(fmap[key]);
							if (rec) rec.setValue(value);
						});
					}
					mo.set('_cfdefs', json.data.cfdefs);
					cfpnl.setStore(mo.cvalues());
				}
				cfpnl.unwait();
			}
		});
	},
	
	/**
	 * Retrieves CustomField definition for specified Tags.
	 * @param {String[]|String} tags Array or pipe-separated list of WebTop's tag IDs.
	 * @param {Mixed} entityId The entity ID value that refers to Model.s
	 * @param {Object} opts An object containing configuration.
	 * @param {String} opts.serviceId The service ID to perform request against to.
	 * @param {String} [opts.actionName] The action name to call. Defaults to 'GetCustomFieldsDefsData'.
	 * @param {String} [opts.idParam] The entity ID param name. Defaults to 'id'.
	 */
	getCustomFieldsDefsData: function(tags, entityId, opts) {
		opts = opts || {};
		var me = this,
			tarr = Ext.isString(tags) ? Sonicle.String.split(tags, '|') : tags,
			params = {
				tags: Sonicle.Utils.toJSONArray(tarr || [])
			};
			
		params[opts.idParam || 'id'] = entityId;
		WT.ajaxReq(opts.serviceId, opts.actionName || 'GetCustomFieldsDefsData', {
			params: params,
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	}
});
