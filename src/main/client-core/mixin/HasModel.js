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
	alternateClassName: 'WTA.mixin.HasModel',
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
		 * Name of viewModel's property in which {@link WTA.sdk.ModelView#model attached model} 
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
	
	/**
	 * @event beforemodelload
	 * @param {Object} this
	 * @param {Object} pass Custom parameters to pass back.
	 */
	
	/**
	 * @event modelload
	 * @param {Object} this
	 * @param {Boolean} success True if the operation succeeded.
	 * @param {Ext.data.Model} model The loaded model.
	 * @param {Ext.data.operation.Operation} op The operation performed.
	 * @param {Object} pass Custom parameters to pass back.
	 */
	
	/**
	 * @event beforemodelvalidate
	 * @param {Object} this
	 * @param {Ext.data.Model} model The saved model.
	 * @param {Object} pass Custom parameters to pass back.
	 */
	
	/**
	 * @event beforemodelsave
	 * @param {Object} this
	 * @param {Ext.data.Model} model The saved model.
	 * @param {Object} pass Custom parameters to pass back.
	 */
	
	/**
	 * @event modelsave
	 * @param {Object} this
	 * @param {Boolean} success True if the operation succeeded.
	 * @param {Ext.data.Model} model The saved model.
	 * @param {Ext.data.operation.Operation} op The operation performed.
	 * @param {Object} pass Custom parameters to pass back.
	 */
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		if(!vm) Ext.Error.raise('ViewModel need to be defined');
		if(!Ext.isString(me.getModelProperty())) Ext.Error.raise('Specify a valid model property');
		if(!me.getModelName()) Ext.Error.raise('Specify a valid model name');
		
		// Pushes some built-in formulas
		//vm.setFormulas(Ext.apply(vm.getFormulas() || {}, me._builtInFormulas(me.getModelProperty())));
		
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
	 * @param {Object} opts.data Initial data (typically )
	 * @param {Boolean} [opts.dirty=false] The value of dirty flag to keep after loading.
	 * @param {Object} [opts.pass] Custom parameters to pass to events callbacks
	 * @param {Function} [opts.callback] The callback function to call
	 * @param {Object} [opts.scope] The scope (this) for the supplied callback
	 */
	loadModel: function(opts) {
		opts = opts || {};
		var me = this,
				model = me.getModel(),
				dirty = Ext.isBoolean(opts.dirty) ? opts.dirty : false,
				data, vm, linkName, idProp, id;
		
		if(model) { // Model already linked
			me.fireEvent('beforemodelload', me, opts.pass);
			model.load({
				callback: function(rec, op, success) {
					me.onModelLoad(success, model, undefined, opts.pass);
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
						prx = mdl.getProxy(),
						reader = prx.getReader(),
						success = (mdl.phantom) ? true : reader.getSuccess(reader.rawData || {});
				
				if (success) {
					if (mdl.getProxy().type === 'memory') {
						mdl.set(opts.data || {}, {dirty: dirty});
					} else {
						if (dirty) mdl.dirty = true;
					}
				}
				me.onModelLoad(success, mdl, undefined, opts.pass);
				Ext.callback(opts.callback, opts.scope || me, [success, mdl]);
			});
			
			// Apply linking...
			me.fireEvent('beforemodelload', me, opts.pass);
			if(Ext.isEmpty(id)) { // New instance
				// Defines a viewmodel link, creating an empty (phantom) model
				vm.linkTo(linkName, {
					type: me.getModelName(),
					create: true
				});
				// Apply initial data setting dirty flag
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
	
	onModelLoad: function(success, model, op, pass) {
		this.fireEvent('modelload', this, success, model, op, pass);
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
				model = me.getModel();
		
		if (model) {
			me.fireEvent('beforemodelvalidate', me, model, opts.pass);
			if(model.isValid()) {
				me.fireEvent('beforemodelsave', me, model, opts.pass);
				model.save({
					callback: function(rec, op, success) {
						me.onModelSave(success, model, op, opts.pass);
						Ext.callback(opts.callback, opts.scope || me, [success, model]);
					},
					scope: me
				});
				return true;
			}
		}
		return false;
	},
	
	onModelSave: function(success, model, op, pass) {
		this.fireEvent('modelsave', this, success, model, op, pass);
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
