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
Ext.define('Sonicle.webtop.core.mixin.HasModel', {
	alternateClassName: 'WT.mixin.HasModel',
	extend: 'Ext.Mixin',
	mixinConfig: {
		id: 'hasmodel',
		on: {
			initComponent: 'initComponent'
		}
	},
	
	config: {
		/**
		 * @cfg {String} modelProperty
		 * Name of viewModel's property in which {@link WT.sdk.ModelView#model attached model} 
		 * data will be stored. Defaults to 'record'. (See {@link Ext.app.ViewModel#links})
		 */
		modelProperty: 'record',
		
		/**
		 * @cfg {String} modelName
		 * Name of the {@link Ext.data.Model Model} associated with this view.
		 */
		modelName: null,
		
		modelIdProperty: null
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		if(!vm) Ext.Error.raise('ViewModel need to be defined');
		if(!Ext.isString(me.getModelProperty())) Ext.Error.raise('Specify a valid model property');
		if(!me.getModelName()) Ext.Error.raise('Specify a valid model name');
		
		// Pushes some built-in formulas
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, me._builtInFormulas(me.getModelProperty())));
		
		// If necessary, guess model id property name
		if(!Ext.isString(me.getModelIdProperty())) {
			var model = Ext.create(me.getModelName());
			me.setModelIdProperty(model.getIdProperty());
			model.destroy();
		}
	},
	
	/**
	 * @private
	 * @property {String} linkedModelIdField
	 * Stores {@link Ext.data.Model#idProperty idField's name}) determined from attached model.
	 */
	//inkedModelIdField: null,
	
	/*
	constructor: function(cfg) {
		var me = this,
				cf = me.config,
				vm = cf.viewModel || me.viewModel,
				cfgModel = cf.modelName || cfg.modelName; // modelName can be passed in constructor...
		
		if(vm && !vm.isViewModel) {
			Ext.Error.raise('Create a new viewModel instance instead of using config');
		}
		
		// If necessary defines viewModel, then apply built-in formulas...
		if(!vm) {
			vm = Ext.create('Ext.app.ViewModel');
			me.viewModel = vm;
		}
		vm.setFormulas(Ext.apply(vm.getFormulas() || {}, me._builtInFormulas(cf.modelProperty)));
		
		// Guess model idField name
		if(Ext.isString(cfgModel)) {
			var model = Ext.create(cfgModel);
			me.linkedModelIdField = model.getIdProperty();
			model.destroy();
		} else {
			Ext.Error.raise('Specify a model name');
		}
		
		me.callParent([cfg]);
	},
	
	*/
	
	destroy: function() {
		
	},
	
	/**
	 * Returns the attached model.
	 * @returns {Ext.data.Model}
	 */
	getModel: function() {
		return this.getVMData()[this.getModelProperty()];
	},
	
	getModelStatus: function() {
		return this.getVMData().status;
	},
	
	/**
	 * Loads configured model.
	 * @param {Object} opts 
	 * @param {Object} opts.data Initial data
	 * @param {Object} opts.pass Custom parameters to pass to events callbacks
	 * @param {Function} [opts.callback] The callback function to call
	 * @param {Object} [opts.scope] The scope (this) for the supplied callback
	 */
	loadModel: function(opts) {
		opts = opts || {};
		var me = this,
				model = me.getModel(),
				data, vm, linkName, idProp, id;
		
		if(model) { // Model already linked
			me.fireEvent('beforemodelload', me, opts.pass);
			model.load({
				callback: function(rec, op, success) {
					me.fireEvent('modelload', me, success, model, opts.pass);
					Ext.callback(opts.callback, opts.scope||me, [ success, model ]);
				},
				scope: me
			});
			
		} else { // Model is not linked
			data = opts.data || {};
			vm = me.getViewModel();
			linkName = me.getModelProperty();
			idProp = me.getModelIdProperty();
			id = data[idProp];
			
			// Due to there is no callback on linkTo method, we need to register a
			// binding handler that will be called (once) when the viewmodel will
			// be populated.
			vm.bind({
				bindTo: '{'+linkName+'}',
				single: true
			}, function() {
				var mdl = me.getModel(),
						reader = mdl.getProxy().getReader(),
						success = (mdl.phantom) ? true : reader.getSuccess(reader.rawData || {});
				me.fireEvent('modelload', me, success, mdl, opts.pass);
				Ext.callback(opts.callback, opts.scope||me, [ success, mdl ]);
			});
			
			// Apply linking...
			me.fireEvent('beforemodelload', me, opts.pass);
			if(Ext.isEmpty(id)) { // New instance
				// Defines a viewmodel link, creating an empty (phantom) model
				vm.linkTo(linkName, {
					type: me.getModelName(),
					create: true
				});
				// Apply initial data resetting dirty flag
				model = vm.get(linkName);
				model.set(data, {
					dirty: false
				});
				model.setAssociated(data); // Using our custom Sonicle.data.Model!
				
			} else { // Load an instance (an id is required)
				if(!id) Ext.Error.raise('A value for idProperty ['+idProp+'] needs to be defined in passed data');
				vm.linkTo(linkName, {
					type: me.getModelName(),
					id: id
				});
			}
		}
	},
	
	/**
	 * Saves configured model.
	 * @param {Object} opts 
	 * @param {Object} opts.pass Custom parameters to pass to events callbacks
	 * @param {Function} [opts.callback] The callback function to call
	 * @param {Object} [opts.scope] The scope (this) for the supplied callback
	 * @returns {Boolean} 'true' if the async operation started, 'false' otherwise
	 */
	saveModel: function(opts) {
		opts = opts || {};
		var me = this,
				model = me.getModel(),
				proxy;
		
		if(model && model.isValid()) {
			me.fireEvent('beforemodelsave', me, model, opts.pass);
			model.save({
				callback: function(rec, op, success) {
					me.fireEvent('modelsave', me, op, success, model, opts.pass);
					Ext.callback(opts.callback, opts.scope||me, [ success, model ]);
				},
				scope: me
			});
			return true;
		} else {
			return false;
		}
	},
	
	privates: {
		_builtInFormulas: function(modelProperty) {
			return {
				status: {
					bind: {
						bindTo: '{'+modelProperty+'}',
						deep: true
					},
					get: function(model) {
						var obj = {
							//FIXME: l'aggiornamento di una associatons non notifica il cambio di stato
							dirty: (model && model.isDirty) ? model.isDirty() : false,
							valid: (model && model.isModel) ? model.isValid() : false
						};
						obj.cleanAndValid = !obj.dirty && obj.valid;
						return obj;
					}
				}
			};
		}
	}
});
