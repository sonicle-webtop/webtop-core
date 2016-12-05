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
	
	session: true,
	
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
	
	
	/**
	 * @event viewload
	 * @param {WTA.sdk.ModelView} this
	 * @param {Boolean} success Whether the operation was successful or not.
	 * @param {Ext.data.Model} model The loaded model.
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
	
	viewModel: {
		data: {
			_fieldTitle: '',
			_modeTitle: ''
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
		if(me.autoToolbar) me.initTBar();
		me.callParent(arguments);
		me.on('modelsave', me.onModelSave);
		me.on('modelload', me.onModelLoad);
	},
	
	initTBar: function() {
		var me = this, items = [];
		
		me.addAction('saveClose', {
			text: WT.res('act-saveClose.lbl'),
			iconCls: 'wt-icon-saveClose-xs',
			handler: function() {
				me.saveView(true);
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
							me.saveView(false);
						}
					}),
					me.getAction('saveClose')
				],
				handler: function() {
					me.getAction('saveClose').execute();
				}
			});
		}
		WTA.Util.applyTbItems(me, 'top', items, false);
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
				om = me.mode;
		
		if(!me.validModeRe.test(value)) return;
		me.mode = value;
		if(me.isMode(me.MODE_VIEW)) {
			me.promptConfirm = false;
		}
		if(me.mode !== om) me.onModeChange(value, om);
	},
	
	/**
	 * Checks if current mode match within the passed one.
	 * @param {String} mode Mode value to check.
	 * @returns {Boolean} True if specified mode is currently active, False otherwise.
	 */
	isMode: function(mode) {
		return (this.mode === mode);
	},
	
	loadView: function() {
		var me = this;
		me.wait();
		me.loadModel({
			data: me.opts.data
		});
	},
	
	saveView: function(closeAfter) {
		var me = this, ok;
		me.wait();
		ok = me.saveModel({
			pass: {
				closeAfter: closeAfter
			}
		});
		if(!ok) me.unwait();
	},
	
	onModelLoad: function(s, success, model) {
		var me = this;
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
	
	onModeChange: function(nm, om) {
		var me = this;
		if(me.getModeTitle() === true) {
			me.getVM().set('_modeTitle', me.formatModeTitle(me.mode));
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
		return Ext.String.format(this.fieldTitleFormat, s || '');
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
	}
});
