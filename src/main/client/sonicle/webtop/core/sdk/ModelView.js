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
Ext.define('Sonicle.webtop.core.sdk.ModelView', {
	alternateClassName: 'WT.sdk.ModelView',
	extend: 'WT.sdk.DockableView',
	requires: [
		'Sonicle.form.Panel'
	],
	
	config: {
		modelProperty: 'record',
		model: null,
		
		/**
		 * @cgf {String} formRefKey
		 * Key to use within {@link WT.mixin.RefStorer#getRef} method in order
		 * to retrieve the form component of this view.
		 */
		formRefKey: 'form',
		
		/**
		 * @cfg {Boolean} autoToolbar
		 * True to automatically define a top toolbar with actions for saving
		 * and closing the view (Save/Save&Close buttons).
		 */
		autoToolbar: true,
		
		/**
		 * @cfg {Boolean} showSave
		 * True to define also a Save button in addition to the Save&Close one.
		 */
		showSave: false,
		
		/**
		 * @cgf {Boolean/String} [autoTitle = true]
		 * True to enable automatic title update based on current operative
		 * mode; False to disable the automation.
		 * It can be a string (form field name) in order to replace operative 
		 * mode string within specified field's value.
		 */
		autoTitle: true,
		
		/**
		 * @cfg {String} titleFormat
		 * Formatting string to use during title update.
		 */
		titleFormat: '{0}: {1}'
	},
	
	/**
	 * @property {String} MODE_NEW
	 * Special constant value representing 'new' operative mode.
	 */
	MODE_NEW: 'new',
	
	/**
	 * @property {String} MODE_EDIT
	 * Special constant value representing 'edit' operative mode.
	 */
	MODE_EDIT: 'edit',
	
	/**
	 * @property {String} MODE_VIEW
	 * Special constant value representing 'view' operative mode.
	 */
	MODE_VIEW: 'view',
	
	/**
	 * @private
	 * @property {String} mode
	 * Form operative mode.
	 */
	mode: null,
	
	/**
	 * @private
	 * @property {Object} opts
	 * Options passed during a begin call.
	 */
	opts: null,
	
	linkedModelIdField: null,
	
	viewModel: {},
	
	constructor: function(config) {
		var me = this,
				cfg = me.config;
		
		// Defines a viewModel if not exists
		me.config.viewModel = cfg.viewModel || {};
		
		// Apply some built-in formulas...
		me.config.viewModel.formulas = Ext.apply(cfg.viewModel.formulas || {}, {
			status: {
				bind: {
					bindTo: '{'+cfg.modelProperty+'}',
					deep: true
				},
				get: function(model) {
					var obj = {
						dirty: model ? model.dirty : false,
						valid: model && model.isModel ? model.isValid() : false
					};
					obj.dirtyAndValid = obj.dirty && obj.valid;
					return obj;
				}
			}
		});
		
		// Guess model idField name
		if(Ext.isString(cfg.model)) {
			var model = Ext.create(cfg.model);
			me.linkedModelIdField = model.getIdProperty();
			model.destroy();
		} else {
			me.linkedModelIdField = cfg.model.getIdProperty();
		}
		
		me.callParent([config]);
	},
	
	initComponent: function() {
		var me = this;
		
		if(me.autoToolbar) me.initTBar();
		me.callParent(arguments);
		me.on('beforeviewclose', me.onBeforeViewClose);
	},
	
	initTBar: function() {
		var me = this, items = [];
		
		me.addAction('saveClose', {
			text: WT.res('act-saveClose.lbl'),
			iconCls: 'wt-icon-saveClose-xs',
			handler: function() {
				me.doSave(true);
			}
		});
		
		if(!me.showSave) {
			items.push(me.getAction('saveClose'));
		} else {
			items.push({
				xtype: 'splitbutton',
				text: WT.res('act-saveClose.lbl'),
				iconCls: 'wt-icon-saveClose-xs',
				menu: [
					me.addAction('save', {
						text: WT.res('act-save.lbl'),
						iconCls: 'wt-icon-save-xs',
						handler: function() {
							me.doSave(false);
						}
					}),
					me.getAction('saveClose')
				],
				handler: function() {
					me.getAction('saveClose').execute();
				}
			});
		}
		WT.Util.applyTbItems(me, 'top', items, false);
	},
	
	/**
	 * Loads defined form and sets NEW mode.
	 * @param {Object} opts
	 * @param {Object} opts.data
	 */
	beginNew: function(opts) {
		var me = this;
		me.opts = opts || {};
		me.setMode(me.MODE_NEW);
		me.doLoad(me.opts.data);
	},
	
	/**
	 * Loads defined form and sets VIEW mode.
	 * @param {Object} opts
	 * @param {Object} opts.data
	 */
	beginView: function(opts) {
		var me = this;
		me.opts = opts || {};
		me.setMode(me.MODE_VIEW);
		me.doLoad(me.opts.data);
	},
	
	/**
	 * Loads defined form and sets EDIT mode.
	 * @param {Object} opts
	 * @param {Object} opts.data
	 */
	beginEdit: function(opts) {
		var me = this;
		me.opts = opts || {};
		me.setMode(me.MODE_EDIT);
		me.doLoad(me.opts.data);
	},
	
	getFormCmp: function() {
		return this.getRef(this.formRefKey);
	},
	
	/**
	 * Sets a operative mode.
	 * @param {String} value The mode to set.
	 */
	setMode: function(value) {
		var me = this,
				om = me.mode;
		switch(value) {
			case me.MODE_NEW:
			case me.MODE_VIEW:
			case me.MODE_EDIT:
				me.mode = value;
				break;
			default:
				return;
		}
		if(me.mode !== om) me.onModeChange(value, om);
	},
	
	/**
	 * Checks if current mode match within the passed one.
	 * @param {type} mode Mode value to check.
	 * @returns {Boolean} True if specified mode is currently active, False otherwise.
	 */
	isMode: function(mode) {
		return (this.mode === mode);
	},
	
	getVMData: function() {
		return this.getViewModel().data;
	},
	
	getModel: function() {
		return this.getVMData()[this.getModelProperty()];
	},
	
	getModelStatus: function() {
		return this.getVMData().status;
	},
	
	doLoad: function(data) {
		data = data || {};
		var me = this,
				vm = me.getViewModel(),
				linkName = me.getModelProperty(),
				id = data[me.linkedModelIdField];
		
		vm.bind({
			bindTo: '{'+linkName+'}',
			single: true
		}, function(model) {
			var reader = model.getProxy().getReader(),
					success = (model.phantom) ? true : reader.getSuccess(reader.rawData || {});
			me.fireEvent('viewload', me, success, model);
		});
		
		if(Ext.isEmpty(id)) {
			vm.linkTo(me.modelProperty, {
				type: me.model,
				create: true
			});
			vm.get(me.modelProperty).set(data, {
				dirty: false
			});
		} else {
			vm.linkTo(me.modelProperty, {
				type: me.model,
				id: id
			});
		}
	},
	
	doSave: function(closeAfter) {
		var me = this,
				model = me.getModel();
		
		if(this.isModelValid()) {
			model.save({
				callback: function(rec, op, success) {
					me.fireEvent('viewsave', me, success, model);
					if(success) {
						if(closeAfter) me.closeView(false);
					} else {
						WT.error(op.getError());
					}
				},
				scope: me
			});
		}
	},
	
	onModeChange: function(nm, om) {
		var me = this;
		if(me.autoTitle) me.updateTitle();
		me.fireEvent('modechange', me, nm, om);
	},
	
	onConfirmView: function() {
		// User chose to save dirty values before close.
		// Do save, signalling to close the view after a succesful operation.
		this.doSave(true);
	},
	
	onBeforeViewClose: function() {
		// Returns false to stop view closing and to display a confirm message.
		if(this.isModelDirty()) return false;
	},
	
	isModelDirty: function() {
		return this.getModelStatus().dirty;
	},
	
	isModelValid: function() {
		return this.getModelStatus().valid;
	},
	
	updateTitle: function() {
		var me = this, mtit;
		if(this.ctInited) {
			if(Ext.isString(me.autoTitle)) {
				mtit = me.getFormCmp().getFieldValue(me.autoTitle) || '';
			} else {
				switch(me.mode) {
					case me.MODE_VIEW:
						mtit = WT.res('act-view.lbl');
						break;
					case me.MODE_NEW:
						mtit = WT.res('act-new.lbl');
						break;
					case me.MODE_EDIT:
						mtit = WT.res('act-edit.lbl');
						break;
				}
			}
			me.ownerCt.setTitle(Ext.String.format(me.titleFormat, me.title, mtit));
		}
	}
});
