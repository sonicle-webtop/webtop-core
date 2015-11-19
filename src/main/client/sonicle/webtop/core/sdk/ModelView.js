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
	mixins: [
		'WT.mixin.HasModel'
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
		 * @cfg {String} fieldTitleFormat
		 * Formatting string used to insert a model's field in title.
		 */
		fieldTitleFormat: '{0} [{1}]',
		
		/**
		 * @cgf {Boolean} [modeTitle = false]
		 * Pass as `false` to prevent current operative mode from being included in title.
		 */
		modeTitle: true,
		
		/**
		 * @cfg {String} modeTitleFormat
		 * Formatting string used to insert current operative mode in title.
		 */
		modeTitleFormat: '{0}: {1}'
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
	
	constructor: function(config) {
		var me = this;
		
		if(me.getInitialConfig('viewModel') === undefined) {
			me.config.viewModel = Ext.create('Ext.app.ViewModel');
		}
		this.callParent([config]);
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
		me.loadView();
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
		me.loadView();
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
		me.loadView();
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
		s.unwait();
		if(s.fieldTitle) s.updateTitle();
		s.fireEvent('viewload', s, success, model);
	},
	
	onModelSave: function(s, op, success, model, pass) {
		s.unwait();
		s.fireEvent('viewsave', s, success, model);
		if(success) {
			if(pass.closeAfter) s.closeView(false);
		} else {
			WT.error(op.getError());
		}
	},
	
	onModeChange: function(nm, om) {
		var me = this;
		if(me.modeTitle) me.updateTitle();
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
	
	updateTitle: function() {
		var me = this,
				title = me.getTitle(),
				model, arg1;
		
		if((me.fieldTitle === true) || Ext.isString(me.fieldTitle)) {
			model = me.getModel();
			if(model) {
				arg1 = Ext.isString(me.fieldTitle) ? model.get(me.fieldTitle) : model.getId();
				title = Ext.String.format(me.fieldTitleFormat, title, (!Ext.isEmpty(arg1) ? arg1+'' : ''));
			}
		}
		
		if(me.modeTitle === true) {
			switch(me.mode) {
				case me.MODE_VIEW:
					arg1 = WT.res('act-view.lbl');
					break;
				case me.MODE_NEW:
					arg1 = WT.res('act-new.lbl');
					break;
				case me.MODE_EDIT:
					arg1 = WT.res('act-edit.lbl');
					break;
				default:
					arg1 = null;
			}
			if(arg1) title = Ext.String.format(me.modeTitleFormat, title, arg1);
		}
		me.setViewTitle(title);
	}
});
