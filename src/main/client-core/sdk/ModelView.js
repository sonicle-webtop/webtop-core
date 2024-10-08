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
	alternateClassName: 'WTA.sdk.ModelView',
	extend: 'WTA.sdk.DockableView',
	mixins: [
		'WTA.mixin.HasModel'
	],
	uses: [
		'Sonicle.form.Separator'
	],
	
	session: false,
	
	config: {
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
		 * @cgf {Boolean/String} [fieldTitle = false]
		 * Pass as `false` to prevent model's field from being included in title.
		 * Pass as string (field's name) to specify a field different from model's id.
		 */
		fieldTitle: false,
		
		/**
		 * @cgf {Boolean} [modeTitle = false]
		 * Pass as `false` to prevent current operative mode from being included in title.
		 */
		modeTitle: true
	},
	
	/**
	 * @cfg {String} fieldTitleFormat
	 * Formatting string used to insert a model's field in title.
	 */
	fieldTitleFormat: ' [{0}]',
	
	/**
	 * @cfg {String} modeTitleFormat
	 * Formatting string used to insert current operative mode in title.
	 */
	modeTitleFormat: ': {0}',
	
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
	
	validModeRe: /^new|edit|view$/i,
	
	/**
	 * @readonly
	 * @property {Boolean} modelLoading
	 * True if the model is being loaded.
	 */
	modelLoading: false,
	
	/**
	 * @private
	 * @property {Object} opts
	 * Options passed during a begin call.
	 */
	opts: null,
	
	/**
	 * @cfg {String/Object} focusField
	 * The {@link Ext.Component#reference reference name} of the default 
	 * field to be focused when view is loaded. Or an object that specifies 
	 * that name for each view opening MODE (uses as object's keys).
	 */
	
	/**
	 * @event viewload
	 * @param {WTA.sdk.ModelView} this
	 * @param {Boolean} success Whether the operation was successful or not.
	 * @param {Ext.data.Model} model The loaded model.
	 */
	
	/*
	 * @event beforeviewsave
	 * @param {WTA.sdk.ModelView} this
	 * @param {Ext.data.Model} model The saved model.
	 */
	
	/*
	 * @event viewsave
	 * @param {WTA.sdk.ModelView} this
	 * @param {Boolean} success Whether the operation was successful or not.
	 * @param {Ext.data.Model} model The saved model.
	 */
	
	/**
	 * @event modechange
	 * @param {WTA.sdk.ModelView} this
	 * @param {String} nm The current (activated) mode
	 * @param {String} om The previous mode
	 */
	
	topToolbarCls: 'wt-modelview-toolbar',
	topToolbar1Cls: 'wt-modelview-toolbar wt-modelview-toolbar-first',
	
	viewModel: {
		data: {
			_fieldTitle: '',
			_modeTitle: '',
			_mode: null
		}
	},
	
	constructor: function(cfg) {
		var me = this, vm;
		
		if(me.getInitialConfig('viewModel') === undefined) {
			me.config.viewModel = Ext.create('Ext.app.ViewModel');
		}
		me.callParent([cfg]);
		
		if(!cfg.title) {
			me.setBind({
				title: '{_viewTitle}{_fieldTitle}{_modeTitle}'
			});
		}
		
		vm = me.getVM();
		if(me.getFieldTitle()) {
			vm.set('_fieldTitle', me.formatFieldTitle(null));
		}
		if(me.getModeTitle()) {
			vm.set('_modeTitle', me.formatModeTitle(null));
		}
	},
	
	initComponent: function() {
		var me = this;
		if (me.autoToolbar) me.initTBar();
		me.callParent(arguments);
		//me.on('modelsave', me.onModelSave);
		//me.on('modelload', me.onModelLoad);
	},
	
	initTBar: function() {
		var me = this,
			SoU = Sonicle.Utils;
		
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'top', [
			me.createTopToolbar1Cfg()
		]);
	},
	
	/**
	 * Inits desired mode.
	 * @param {String} mode
	 * @param {Object} opts
	 * @param {Object} opts.data
	 */
	begin: function(mode, opts) {
		var me = this;
		if([me.MODE_NEW, me.MODE_VIEW, me.MODE_EDIT].indexOf(mode) === -1) return;
		
		me.opts = opts || {};
		me.setMode(mode);
		me.loadView();
	},
	
	/**
	 * Loads defined model and sets NEW mode.
	 * @param {Object} opts
	 * @param {Object} opts.data
	 * @param {Boolean} [opts.dirty=false]
	 */
	beginNew: function(opts) {
		this.begin(this.MODE_NEW, opts);
	},
	
	/**
	 * Loads defined model and sets VIEW mode.
	 * @param {Object} opts
	 * @param {Object} opts.data
	 */
	beginView: function(opts) {
		this.begin(this.MODE_VIEW, opts);
	},
	
	/**
	 * Loads defined model and sets EDIT mode.
	 * @param {Object} opts
	 * @param {Object} opts.data
	 */
	beginEdit: function(opts) {
		this.begin(this.MODE_EDIT, opts);
	},
	
	/**
	 * Sets a operative mode.
	 * @param {String} value The mode to set.
	 */
	setMode: function(value) {
		var me = this,
				vm = me.getVM(),
				old = vm.get('_mode');
		
		if (!me.validModeRe.test(value)) return;
		vm.set('_mode', value);
		if (value === me.MODE_VIEW) {
			me.promptConfirm = false;
		}
		if (value !== old) me.onModeChange(value, old);
	},
	
	getMode: function() {
		return this.getVM().get('_mode');
	},
	
	/**
	 * Checks if current mode match within the passed one.
	 * @param {String} mode Mode value to check.
	 * @returns {Boolean} True if specified mode is currently active, False otherwise.
	 */
	isMode: function(mode) {
		return this.getMode() === mode;
	},
	
	loadView: function() {
		var me = this;
		me.modelLoading = true;
		me.wait();
		me.loadModel({
			data: me.opts.data,
			dirty: me.opts.dirty
		});
	},
	
	/**
	 * Persists view's data, saving the underlying model.
	 * @param {Boolean} [closeAfter] `true` to close view after successful operation.
	 * @param {Object} opts An object containing configuration.
	 * @param {Function} [opts.callback] The callback function to {@link WTA.mixin.HasModel#saveModel} opts.
	 * @param {Object} [opts.scope] The scope (this) for the supplied callback
	 */
	saveView: function(closeAfter, opts) {
		opts = opts || {};
		var me = this,
				mo = me.getModel(), ok;
		if (me.fireEvent('beforeviewsave', me, mo) !== false) {
			me.wait();
			ok = me.saveModel({
				pass: {
					closeAfter: closeAfter
				},
				callback: opts.callback,
				scope: opts.scope
			});
			if (!ok) {
				me.unwait();
				if (mo) me.fireEvent('viewinvalid', me, mo, mo.getValidation().getErrors());
			}
		}
	},
	
	onModelLoad: function(success, model, op, pass) {
		var me = this;
		me.mixins.hasmodel.onModelLoad.call(me, success, model, op, pass);
		me.modelLoading = false;
		me.unwait();
		if ((me.getFieldTitle() === true) || Ext.isString(me.getFieldTitle())) {
			me.getVM().set('_fieldTitle', me.formatFieldTitle(me.getModel()));
		}
		me.fireEvent('viewload', me, success, model, op);
	},
	
	onModelSave: function(success, model, op, pass) {
		var me = this;
		me.mixins.hasmodel.onModelSave.call(me, success, model, op, pass);
		me.unwait();
		me.fireEvent('viewsave', me, success, model, op);
		if (success) {
			if (pass && pass.closeAfter) me.closeView(false);
		}
	},
	
	/*
	onModelLoad: function(s, success, model) {
		var me = this;
		me.modelLoading = false;
		s.unwait();
		if((me.getFieldTitle() === true) || Ext.isString(me.getFieldTitle())) {
			me.getVM().set('_fieldTitle', me.formatFieldTitle(me.getModel()));
		}
		s.fireEvent('viewload', s, success, model);
	},
	
	onModelSave: function(s, op, success, model, pass) {
		s.unwait();
		s.fireEvent('viewsave', s, success, model);
		if(success) {
			if(pass.closeAfter) s.closeView(false);
		}
	},
	*/
	
	onModeChange: function(nm, om) {
		var me = this;
		if (me.getModeTitle() === true) {
			me.getVM().set('_modeTitle', me.formatModeTitle(me.getMode()));
		}
		me.fireEvent('modechange', me, nm, om);
	},
	
	onConfirmView: function() {
		// User chose to save dirty values before close.
		// Do save, signalling to close the view after a succesful operation.
		this.saveView(true);
	},
	
	canCloseView: function() {
		// Returns false to stop view closing and to display a confirm message.
		var model = this.getModel();
		if(model && model.isDirty()) return false; // Using our custom Sonicle.data.Model!
		return true;
	},
	
	formatFieldTitle: function(model) {
		var me = this, s = null;
		if(model) {
			if(Ext.isString(me.getFieldTitle())) {
				s = model.get(me.getFieldTitle());
			} else if(me.getFieldTitle() === true) {
				s = model.getId();
			}
		}
		return Ext.String.format(this.fieldTitleFormat, Sonicle.String.htmlEncode(s || ''));
	},
	
	formatModeTitle: function(mode) {
		var me = this, s = null;
		switch(mode) {
			case me.MODE_VIEW:
				s = WT.res('act-view.lbl');
				break;
			case me.MODE_NEW:
				s = WT.res('act-new.lbl');
				break;
			case me.MODE_EDIT:
				s = WT.res('act-edit.lbl');
				break;
		}
		return Ext.String.format(this.modeTitleFormat, s || '');
	},
	
	privates: {
		applyFocusOnFocusField: function(focusField) {
			var me = this;
			if (Ext.isString(focusField)) {
				me.callParent(arguments);
			} else if (Ext.isObject(focusField)) {
				var doFocusIf = function(ff, mode) {
						if (Ext.isString(ff[mode])) {
							var cmp = me.lref(ff[mode]);
							if (cmp) cmp.focus(true);
						}
					};
				if (me.isMode(me.MODE_VIEW)) doFocusIf(focusField, me.MODE_VIEW);
				if (me.isMode(me.MODE_NEW)) doFocusIf(focusField, me.MODE_NEW);
				if (me.isMode(me.MODE_EDIT)) doFocusIf(focusField, me.MODE_EDIT);
			}
		},
		
		onSaveCloseHandler: function(s, e) {
			this.saveView(true);
		},
		
		onSaveHandler: function(s, e) {
			this.saveView(false);
		},
		
		/**
		 * Builds a suitable top-toolbar specifying a positioning type.
		 */
		createTopToolbarXCfg: function(items, type, cfg) {
			var me = this,
				cls = me.topToolbarCls,
				tb;
			
			if (Ext.isArray(items) && !Ext.isEmpty(items)) {
				if ('first' === type) cls += ' '+me.topToolbarCls+'-first';
				if ('last' === type) cls += ' '+me.topToolbarCls+'-last';
				tb = Ext.apply({
					xtype: 'toolbar',
					cls: cls,
					items: items
				}, cfg);
			}
			return tb;
		},
		
		/**
		 * Builds a suitable divider for top-toolbar.
		 */
		createTopToolbarsDividerCfg: function(cfg) {
			return Ext.apply({
				xtype: 'soformseparator',
				cls: this.topToolbarCls+'-divider'
			}, cfg);
		},
		
		createTopToolbar1Cfg: function(moreItems) {
			var me = this,
				pbtnPos = WT.getViewportProperties().viewPrimaryButtonPosition,
				pbtn = me.initPrimaryButton(),
				items;

			items = moreItems || [];
			if (pbtnPos === 'tr') {
				Ext.Array.push(items, ['->', pbtn]);
			} else {
				Ext.Array.insert(items, 0, [pbtn, '-']);
			}
			return me.createTopToolbarXCfg(items, 'first');
		},
		
		initPrimaryButton: function() {
			var me = this;
			if (!me.showSave) {
				return me.addAct('saveClose', me.createSaveCloseActionCfg());
			} else {
				return me.createPrimarySplitButtonCfg({
					menu: [
						me.addAct('save', me.createSaveActionCfg()),
						me.addAct('saveClose', me.createSaveCloseActionCfg(true))
					]
				});
			}
		},
		
		createPrimarySplitButtonCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'splitbutton',
				ui: '{primary}',
				text: WT.res('act-save.lbl'),
				tooltip: WT.res('act-saveClose.lbl'),
				iconCls: 'wt-icon-saveClose',
				handler: function() {
					me.getAct('saveClose').execute();
				}
			}, cfg);
		},
		
		createSaveCloseActionCfg: function(insideSplit, cfg) {
			var me = this;
			return Ext.apply({
				ui: insideSplit ? '{fallback}' : '{primary}',
				text: WT.res(insideSplit ? 'act-saveClose.lbl' : 'act-save.lbl'),
				tooltip: insideSplit ? undefined : WT.res('act-saveClose.lbl'),
				iconCls: 'wt-icon-saveClose',
				handler: me.onSaveCloseHandler,
				scope: me
			}, cfg);
		},
		
		createSaveActionCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				text: WT.res('act-save.lbl'),
				tooltip: null,
				iconCls: 'wt-icon-save',
				handler: me.onSaveHandler,
				scope: me
			}, cfg);
		}
	}
});
