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
Ext.define('Sonicle.webtop.core.ux.picker.CFDataSourceDataDependsOn', {
	alternateClassName: 'WTA.ux.picker.CFDataSourceDataDependsOn',
	extend: 'Sonicle.form.field.pickerpanel.Editor',
	requires: [
		'Sonicle.String',
		'Sonicle.VMUtils',
		'Sonicle.webtop.core.ux.panel.CustomFieldsBase'
	],
	
	/**
	 * @cfg {Ext.data.Store} fieldsStore
	 * The Store that parentField combo should use as its data source
	 */
	fieldsStore: null,
	
	/**
	 * @cfg {String} fieldsStoreValueField
	 * The underlying {@link Ext.data.Field#name data field name} of {@link #fieldsStore} to bind as ID value.
	 */
	fieldsStoreValueField: 'name',
	
	/**
	 * @cfg {String} fieldsStoreDisplayField
	 * The underlying {@link Ext.data.Field#name data field name} of {@link #fieldsStore} to bind as display value.
	 */
	fieldsStoreDisplayField: 'name',
	
	/**
	 * @cfg {Ext.data.Store} placeholderStore
	 * The Store that placeholderName combo should use as its data source
	 */
	placeholderStore: null,
	
	/**
	 * @cfg {String} placeholderStoreValueField
	 * The underlying {@link Ext.data.Field#name data field name} of {@link #placeholderStore} to bind as ID value.
	 */
	placeholderStoreValueField: 'name',
	
	/**
	 * @cfg {String} placeholderStoreDisplayField
	 * The underlying {@link Ext.data.Field#name data field name} of {@link #placeholderStore} to bind as display value.
	 */
	placeholderStoreDisplayField: 'name',
	
	viewModel: {
		data: {
			data: {
				parentField: null,
				placeholder: null
			}
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		cfg.okText = WT.res('word.ok');
		cfg.cancelText = WT.res('word.cancel');
		me.callParent([cfg]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.add({
			xtype: 'wtfieldspanel',
			defaults: {
				labelAlign: 'top'
			},
			items: [
				WTF.localCombo(me.fieldsStoreValueField, me.fieldsStoreDisplayField, {
					reference: 'parentField',
					bind: '{data.parentField}',
					autoLoadOnValue: true,
					store: me.fieldsStore,
					allowBlank: false,
					fieldLabel: WT.res('customFieldDataSourceDataDependsOn.parentField.lbl'),
					anchor: '100%'
				}),
				WTF.localCombo(me.placeholderStoreValueField, me.placeholderStoreDisplayField, {
					reference: 'placeholder',
					bind: '{data.placeholder}',
					autoLoadOnValue: true,
					autoLoadOnQuery: true,
					store: me.placeholderStore,
					allowBlank: false,
					fieldLabel: WT.res('customFieldDataSourceDataDependsOn.placeholder.lbl'),
					anchor: '100%'
				})
			]
		});
	},
	
	applyValue: function(v) {
		if (!Ext.isString(v)) return;
		return v;
	},
	
	updateValue: function(nv, ov) {
		var data = Ext.JSON.decode(nv, true);
		Sonicle.VMUtils.setData(this.getViewModel(), data, ['parentField', 'placeholder']);
	},
	
	syncValue: function() {
		var data = Sonicle.VMUtils.getData(this.getViewModel(), ['parentField', 'placeholder']);
		this.setValue(Ext.JSON.encode(data));
	},
	
	privates: {
		doValidate: function() {
			var me = this, fld, ret = true;
			Ext.iterate(['parentField', 'placeholder'], function(name) {
				fld = me.lookupReference(name);
				if (fld) {
					ret = fld.validate();
				} else {
					ret = false;
				}
				if (!ret) return;
			});
			return ret;
		}
	},
	
	statics: {
		format: function(data, fieldsStore) {
			var parentField = data.parentField;
			if (fieldsStore) {
				var rec = fieldsStore.getById(parentField);
				if (rec) parentField = rec.get('name');
			}
			return Sonicle.String.join(' ', parentField + ' (value)', '->', data.placeholder);
		}
	}
});
